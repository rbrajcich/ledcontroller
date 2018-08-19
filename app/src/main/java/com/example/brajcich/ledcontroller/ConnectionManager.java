package com.example.brajcich.ledcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class ConnectionManager {

    //BLE identifiers required to access serial data of HM-10 chip
    private static final String SERVICE_UUID_STRING = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String CHARACTERISTIC_UUID_STRING = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private static final int CONNECTION_TIMEOUT_MS = 5000;
    private static final int OUTPUT_BUFFER_SIZE = 200;

    //States that the bluetooth connection can be in
    private int connectionState;
    private static final int STATE_APP_INACTIVE = 0;
    private static final int STATE_BLUETOOTH_DISABLED = 1;
    private static final int STATE_SCANNING = 2;
    private static final int STATE_CONNECTING = 3;
    private static final int STATE_DISCOVERING_SERVICES = 4;
    private static final int STATE_CONNECTED = 5;

    //Identifiers for different callbacks for 'notifyRegistered()'
    private static final int NOTIFY_CONNECTION_MADE = 0;
    private static final int NOTIFY_CONNECTION_ENDED = 1;

    private Context context;
    private Set<BluetoothConnectedActivity> registeredActivities;
    private BluetoothConnectedActivity activeActivity;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice device;
    private BluetoothGatt gatt;
    private ScanHandler scanHandler;
    private GattEventCallback gattCallback;
    private Timer connectTimeoutTimer;
    private CommunicationManager communicationManager;


    private int outputBufferPos = 0;
    private byte[] outputBuffer;
    private boolean writingCharacteristic = false;
    private Object characteristicWriteLock;
    private TransmissionQueue transmissionQueue;

    private static ConnectionManager instance;

    public static boolean isBleSupported(Context c){
        return c.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static ConnectionManager getInstance(BluetoothConnectedActivity act){
        if(instance == null){
            instance = new ConnectionManager(act);
        }else{
            instance.registerActivity(act);
        }

        return instance;
    }

    private ConnectionManager(BluetoothConnectedActivity act){

        //Get reference to context which is used for some necessary function calls
        context = act.getBaseContext();

        //Initialize list of activities and add the initial activity
        registeredActivities = new HashSet<>();
        registeredActivities.add(act);

        //App should have this state until an activity STARTS
        connectionState = STATE_APP_INACTIVE;

        //Register broadcast receiver to get event calls when bluetooth is enabled or disabled
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(new BluetoothStateChangeReceiver(), filter);

        gattCallback = new GattEventCallback();
        transmissionQueue = new TransmissionQueue();
        characteristicWriteLock = new Object();

        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void registerCommunicationManager(CommunicationManager cm){
        communicationManager = cm;
    }

    private void registerActivity(BluetoothConnectedActivity act){
        registeredActivities.add(act);
    }

    public void onActivityStarted(BluetoothConnectedActivity act){
        activeActivity = act;

        if(connectionState == STATE_APP_INACTIVE){ //The app is resuming
            startConnecting();
        }
    }

    public void onActivityStopped(BluetoothConnectedActivity act){
        if(act == activeActivity){ //The app has been suspended
            if(connectionState != STATE_BLUETOOTH_DISABLED) disconnect();
            connectionState = STATE_APP_INACTIVE;
        }
    }

    public void onActivityDestroyed(BluetoothConnectedActivity act){
        registeredActivities.remove(act);
    }

    //Notify the registered Activities using the callback corresponding to the notification code
    private void notifyRegistered(int notification){
        for(BluetoothConnectedActivity act : registeredActivities){
            switch(notification){
                case NOTIFY_CONNECTION_ENDED:
                    act.onConnectionEnded();
                    break;
                case NOTIFY_CONNECTION_MADE:
                    act.onConnectionMade(device);
                    break;
            }
        }
    }

    public boolean isBluetoothEnabled(){
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    //Called whenever bluetooth is enabled or disabled
    private void onBluetoothStateChange(){
        for(BluetoothConnectedActivity act : registeredActivities){
            act.updateBluetoothEnabledState();
        }

        if(connectionState == STATE_BLUETOOTH_DISABLED){
            startConnecting();
        }
    }

    //If bluetooth is enabled, reconnect to device or scan for a new device if unable
    private void startConnecting(){

        if(!isBluetoothEnabled()){
            connectionState = STATE_BLUETOOTH_DISABLED;
            return;
        }

        Log.d("BLUETOOTH_CONN", "Initializing Connection Process");

        if (device == null) { //scan for device
            Log.d("BLUETOOTH_CONN", "No Device Remembered");
            scanForDevice();
        } else { //attempt to connect to device, after time out, scan for other devices
            Log.d("BLUETOOTH_CONN", "Device Remembered");
            attemptConnection();
        }
    }

    //Start a scan for a capable Bluetooth Low Energy device
    private void scanForDevice(){
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
        filterBuilder.setServiceUuid(ParcelUuid.fromString(SERVICE_UUID_STRING));
        filters.add(filterBuilder.build());

        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();

        Log.d("BLUETOOTH_CONN", "Scanning for devices...");
        connectionState = STATE_SCANNING;

        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanHandler = new ScanHandler(scanner);
        scanner.startScan(filters, settingsBuilder.build(), scanHandler);
    }

    //Attempt to connect to the last connected device or the device just found by a scan
    private void attemptConnection(){
        Log.d("BLUETOOTH_CONN", "Attempting to connect...");
        connectionState = STATE_CONNECTING;

        gatt = device.connectGatt(context, false, gattCallback);
        final BluetoothGatt toCancel = gatt;

        //Initialize a timeout if the connection is taking too long
        connectTimeoutTimer = new Timer();
        connectTimeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized(gatt) {
                    if (connectionState < STATE_DISCOVERING_SERVICES) {
                        Log.d("BLUETOOTH_CONN", "Connection timeout");
                        toCancel.close();
                        scanForDevice();
                    }
                }
            }
        }, CONNECTION_TIMEOUT_MS);
    }

    //Cancel all connections and currently attempting connections, including scanning
    public void disconnect(){
        Log.d("BLUETOOTH_CONN", "Terminating scans and connections");

        if(connectTimeoutTimer != null) {
            connectTimeoutTimer.cancel();
        }

        if(connectionState == STATE_SCANNING){
            BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
            scanner.stopScan(scanHandler);
            Log.d("BLUETOOTH_CONN", "Terminated Scan");
        }

        if(gatt != null) {
            gatt.close();
            Log.d("BLUETOOTH_CONN", "Terminated Connection");
        }

        if(connectionState == STATE_CONNECTED){
            notifyRegistered(NOTIFY_CONNECTION_ENDED);
        }
    }

    /*
    public void sendData(byte[] data){
        if(connected){
            transmissionQueue.queueTransmission(data);

            synchronized(characteristicWriteLock){
                if(!writingCharacteristic){
                    writeNextPacket();
                }
            }
        }
    }

    public void changeDeviceName(String name){
        if(name.length() < 13 && name.length() > 0){
            String transmission = "*N" + name + "#";
            try {
                sendData(transmission.getBytes("UTF-8"));
            }catch(Exception e){}
        }
    }

    public void previewColor(Color c){
        if(connected){
            transmissionQueue.showPreview(c);
            synchronized(characteristicWriteLock){
                if(!writingCharacteristic){
                    writeNextPacket();
                }
            }
        }
    }

    public void stopPreview(){

        try {
            String stopString = "*R#";
            sendData(stopString.getBytes("UTF-8"));
        }catch(Exception e){}

    }

    private void writeNextPacket(){
        if(outputBuffer == null){
            outputBuffer = transmissionQueue.pop();
            outputBufferPos = 0;
            if(outputBuffer != null){
                writeFromBuffer();
            }else{
                writingCharacteristic = false;
            }
        }else{
            writeFromBuffer();
        }
    }

    private void writeFromBuffer(){
        BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID_STRING));

        if(service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_STRING));
            byte[] b;

            writingCharacteristic = true;

            if(outputBuffer.length - outputBufferPos <= 20){
                b = Arrays.copyOfRange(outputBuffer, outputBufferPos, outputBuffer.length);
                Log.d("BLUETOOTH_DATA", "Writing char: " + Arrays.toString(b));
                outputBuffer = null;
            }else{
                b = Arrays.copyOfRange(outputBuffer, outputBufferPos, outputBufferPos + 20);
                Log.d("BLUETOOTH_DATA", "Writing char: " + Arrays.toString(b));
                outputBufferPos += 20;
            }

            characteristic.setValue(b);
            gatt.writeCharacteristic(characteristic);
        }
    }
    */

    //Handle events when devices are found by the scan operation
    private class ScanHandler extends ScanCallback {

        private BluetoothLeScanner scanner;
        public ScanHandler(BluetoothLeScanner s){ scanner = s; }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(connectionState == STATE_SCANNING) {
                scanner.stopScan(this);
                device = result.getDevice();
                Log.d("BLUETOOTH_CONN", "Device found: " + device.getName());
                attemptConnection();
            }
        }
    }

    //Handler for connection status events and data transmission events
    private class GattEventCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            synchronized(gatt) {
                if (status == 0 && newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLUETOOTH_CONN", "Connection made");
                    connectionState = STATE_DISCOVERING_SERVICES;
                    gatt.discoverServices();
                }else if (status == 133 && newState == BluetoothProfile.STATE_DISCONNECTED){
                    Log.d("BLUETOOTH_CONN", "Direct Connection Timeout");
                }else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                    Log.d("BLUETOOTH_CONN", "Connection lost");
                    notifyRegistered(NOTIFY_CONNECTION_ENDED);
                    startConnecting();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d("BLUETOOTH_DATA", "Services Discovered");
            connectionState = STATE_CONNECTED;
            notifyRegistered(NOTIFY_CONNECTION_MADE);
            BluetoothGattCharacteristic characteristic = gatt.getService(UUID.fromString(SERVICE_UUID_STRING))
                                                            .getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_STRING));

            gatt.setCharacteristicNotification(characteristic, true);
        }
        /*
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            try {
                Log.d("BLUETOOTH_DATA", "Characteristic changed to: " + new String(characteristic.getValue(), "UTF-8"));
            }catch(Exception e){}
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d("BLUETOOTH_DATA", "Characteristic Written status " + status);
            synchronized(characteristicWriteLock){
                writeNextPacket();
            }
        }*/
    }

    //Handler for changes in bluetooth enabled state
    private class BluetoothStateChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                ConnectionManager.this.onBluetoothStateChange();
            }
        }
    }
}
