package com.example.brajcich.ledcontroller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Robert on 8/18/2018.
 */

public class CommunicationManager {

    private static CommunicationManager instance;

    public static CommunicationManager getInstance(ConnectionManager cm){
        if(instance == null){
            instance = new CommunicationManager(cm);
        }

        return instance;
    }

    private ConnectionManager connectionManager;
    private TransmissionQueue transmissionQueue;
    private Set<BluetoothConnectedActivity> listeners;

    private boolean writingCharacteristic;
    private boolean bluetoothConnected;

    private byte[] currentPacket;
    private byte[] currentTransmission;
    private int transmissionIndex;
    private Object writeLock;

    private CommunicationManager(ConnectionManager cm){
        connectionManager = cm;
        cm.registerCommunicationManager(this);

        listeners = new HashSet<>();
        transmissionQueue = new TransmissionQueue();

        writingCharacteristic = false;
        bluetoothConnected = false;
        writeLock = new Object();
    }

    public void registerListener(BluetoothConnectedActivity act){
        listeners.add(act);
    }

    public void removeListener(BluetoothConnectedActivity act){
        listeners.remove(act);
    }

    public void previewColor(Color c){
        if(bluetoothConnected){
            transmissionQueue.showPreview(c);
            synchronized (writeLock) {
                attemptPacketWrite();
            }
        }
    }

    public void startLamp(Lamp l){
        addTransmission(l.getLampTransmission());
    }

    public void stopLamp(){
        try{
            String stopString = "*S#";
            addTransmission(stopString.getBytes("UTF-8"));
        }catch(Exception e){}
    }

    public void stopPreview(){
        try {
            String stopString = "*R#";
            addTransmission(stopString.getBytes("UTF-8"));
        }catch(Exception e){}
    }

    //NOTE: Possible future feature, not supported by receiving chip yet
    public void changeDeviceName(String name){
        if(name.length() < 13 && name.length() > 0){
            String transmission = "*N" + name + "#";
            try {
                    addTransmission(transmission.getBytes("UTF-8"));
            }catch(Exception e){}
        }
    }

    private void addTransmission(byte[] data){
        if(bluetoothConnected){
            transmissionQueue.queueTransmission(data);
            synchronized (writeLock){
                attemptPacketWrite();
            }
        }
    }

    private void attemptPacketWrite(){
        if(writingCharacteristic) return; //if we are waiting for the last packet to write, do nothing

        if(currentTransmission == null){
            currentTransmission = transmissionQueue.pop();
            transmissionIndex = 0;

            if(currentTransmission == null) return; //if there was nothing to send in the queue do nothing
        }

        if(currentTransmission.length - transmissionIndex <= 20){ // less than 20 chars left to write
            currentPacket = Arrays.copyOfRange(currentTransmission, transmissionIndex, currentTransmission.length);
        }else{
            currentPacket = Arrays.copyOfRange(currentTransmission, transmissionIndex, transmissionIndex + 20);
        }

        writingCharacteristic = true;
        connectionManager.writePacket(currentPacket);
    }

    public void onConnectionStateChanged(boolean connected){
        bluetoothConnected = connected;
    }

    //Called when the last packet of data was successfully transmitted
    public void onPacketWriteSuccess(){
        synchronized(writeLock) {
            writingCharacteristic = false;
            transmissionIndex += currentPacket.length;
            if (transmissionIndex >= currentTransmission.length) {
                processCompleteTransmission();
            }
            attemptPacketWrite();
        }
    }

    private void processCompleteTransmission(){
        //Test for relevant transmission type to notify listeners
        currentTransmission = null;
    }
}
