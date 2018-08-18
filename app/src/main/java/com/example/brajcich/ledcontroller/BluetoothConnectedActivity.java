package com.example.brajcich.ledcontroller;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public abstract class BluetoothConnectedActivity extends AppCompatActivity implements LEDBluetoothCallback {

    protected LEDBluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bluetoothManager = LEDBluetoothManager.getInstance(this, this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        super.onCreate(savedInstanceState);
    }

    protected void onBluetoothEnabledOrDisabled(){
        //do nothing by default
    }

    //Create a broadcast receiver to capture events when bluetooth is enabled/disabled
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                BluetoothConnectedActivity.this.onBluetoothEnabledOrDisabled();
            }
        }
    };
}
