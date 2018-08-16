package com.example.brajcich.ledcontroller;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Robert on 5/17/2018.
 */

public interface LEDBluetoothCallback {

    void onConnectionMade(BluetoothDevice device);
    void onConnectionLost();
}
