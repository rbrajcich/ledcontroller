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
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class LEDBluetoothManager {

    private static final String SERVICE_UUID_STRING = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String CHARACTERISTIC_UUID_STRING = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final int CONNECTION_TIMEOUT_MS = 5000;
    private static final int OUTPUT_BUFFER_SIZE = 200;


    private Context context;
    private LEDBluetoothCallback callback;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice device;
    private BluetoothLeScanner scanner;
    private ScanHandler scanHandler;
    private BluetoothGatt gatt;
    private GattEventCallback gattCallback;
    private Timer connectTimeoutTimer;

    private boolean scanning = false;
    private boolean connected = false;

    private int outputBufferPos = 0;

    private byte[] outputBuffer;
    private boolean writingCharacteristic = false;
    private Object characteristicWriteLock;
    private TransmissionQueue transmissionQueue;

    private static LEDBluetoothManager instance;

    public static LEDBluetoothManager getInstance(Context c, LEDBluetoothCallback cb){
        if(instance == null){
            instance = new LEDBluetoothManager(c, cb);
        }

        return instance;
    }

    private LEDBluetoothManager(Context c, LEDBluetoothCallback cb){
        context = c;
        callback = cb;
        scanHandler = new ScanHandler();
        gattCallback = new GattEventCallback();

        transmissionQueue = new TransmissionQueue();
        characteristicWriteLock = new Object();
    }

    public boolean isBleSupported(){
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean isBluetoothEnabled(){
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    public void initConnection(){

        if(!connected) {
            final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            scanner = mBluetoothAdapter.getBluetoothLeScanner();
            connectTimeoutTimer = new Timer();

            Log.d("BLUETOOTH_CONN", "Initializing Connection Process");

            if (device == null) { //scan for device
                Log.d("BLUETOOTH_CONN", "No Device Remembered");
                scanForDevice();
            } else { //attempt to connect to device, after time out, scan for other devices
                Log.d("BLUETOOTH_CONN", "Device Remembered");
                attemptConnection();
            }
        }
    }

    public void disconnect(){
        Log.d("BLUETOOTH_CONN", "App Closing... terminating scans and connections");

        connectTimeoutTimer.cancel();

        if(scanning){
            scanner.stopScan(scanHandler);
            scanning = false;
            Log.d("BLUETOOTH_CONN", "Terminated Scan");
        }

        if(gatt != null) {
            gatt.close();
            Log.d("BLUETOOTH_CONN", "Terminated Connection");
        }

        if(connected){
            callback.onConnectionLost();
            connected = false;
        }
    }

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

    private void scanForDevice(){
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
        filterBuilder.setServiceUuid(ParcelUuid.fromString(SERVICE_UUID_STRING));
        filters.add(filterBuilder.build());

        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();

        Log.d("BLUETOOTH_CONN", "Scanning for devices...");
        scanning = true;
        scanner.startScan(filters, settingsBuilder.build(), scanHandler);
    }

    private void attemptConnection(){
        Log.d("BLUETOOTH_CONN", "Attempting to connect...");
        gatt = device.connectGatt(context, false, gattCallback);
        final BluetoothGatt toCancel = gatt;

       connectTimeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized(gatt) {
                    if (!connected) {
                        Log.d("BLUETOOTH_CONN", "Connection timeout");
                        toCancel.close();
                        scanForDevice();
                    }
                }
            }
        }, CONNECTION_TIMEOUT_MS);
    }

    private class ScanHandler extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(scanning) {
                scanner.stopScan(this);
                scanning = false;
                device = result.getDevice();
                Log.d("BLUETOOTH_CONN", "Device found: " + device.getName());
                attemptConnection();
            }
        }
    }

    private class GattEventCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            synchronized(gatt) {
                if (status == 0 && newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLUETOOTH_CONN", "Connection made");
                    connected = true;
                    gatt.discoverServices();
                }else if (status == 133 && newState == BluetoothProfile.STATE_DISCONNECTED){
                    Log.d("BLUETOOTH_CONN", "Direct Connection Timeout");
                }else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                    Log.d("BLUETOOTH_CONN", "Connection lost");
                    connected = false;
                    callback.onConnectionLost();
                    attemptConnection();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d("BLUETOOTH_DATA", "Services Discovered");
            callback.onConnectionMade(device);
            BluetoothGattCharacteristic characteristic = gatt.getService(UUID.fromString(SERVICE_UUID_STRING))
                                                            .getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_STRING));

            gatt.setCharacteristicNotification(characteristic, true);
        }

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
        }
    }
}
