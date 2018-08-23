package com.example.brajcich.ledcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.Set;

public abstract class BluetoothConnectedActivity extends AppCompatActivity {

    protected ConnectionManager connectionManager;
    protected CommunicationManager communicationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connectionManager = ConnectionManager.getInstance(this);
        communicationManager = CommunicationManager.getInstance(connectionManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectionManager.onActivityStarted(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        connectionManager.onActivityStopped(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectionManager.onActivityDestroyed(this);
    }

    protected void updateBluetoothEnabledState(){}
    protected void onConnectionMade(){}
    protected void onConnectionEnded(){}

}
