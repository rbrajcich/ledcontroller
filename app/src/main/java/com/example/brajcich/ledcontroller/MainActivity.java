package com.example.brajcich.ledcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends BluetoothConnectedActivity{

    private boolean abortLaunch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Ensure required prerequisites are present and if not, alert the user
        if(!verifyAppPrerequisites()){
            abortLaunch = true;
            return;
        }

        //Populate the interface
        setContentView(R.layout.activity_main);

        //Trigger update of bluetooth state
        updateBluetoothEnabledState();

        communicationManager.registerListener(this);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditLampActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.lamp1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Lamp testLamp = new Lamp("Lamp-1");
                testLamp.addPhase(new Lamp.Phase(new Color((short) 255, (short) 0, (short) 0), 5, 5));
                testLamp.addPhase(new Lamp.Phase(new Color((short) 0, (short) 255, (short) 0), 5, 5));
                testLamp.addPhase(new Lamp.Phase(new Color((short) 0, (short) 0, (short) 255), 5, 5));

                communicationManager.startLamp(testLamp);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(abortLaunch) return;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(abortLaunch) return;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        communicationManager.removeListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnectionMade(final BluetoothDevice device) {

        final String deviceName = device.getName();
        Log.d("MAIN_ACTIVITY", "Connection Made (IN MAIN)");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView testText = (TextView) findViewById(R.id.test_text);
                testText.setText("Connected to \"" + deviceName + "\"");
            }
        });

    }

    @Override
    public void onConnectionEnded() {

        Log.d("MAIN_ACTIVITY", "Connection Lost (IN MAIN)");

        if(connectionManager.isBluetoothEnabled()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView testText = (TextView) findViewById(R.id.test_text);
                    testText.setText("Connecting...");
                }
            });
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView testText = (TextView) findViewById(R.id.test_text);
                    testText.setText("Bluetooth Disabled");
                }
            });
        }
    }

    @Override
    protected void updateBluetoothEnabledState(){
        if(!connectionManager.isBluetoothEnabled()){
            ((TextView) findViewById(R.id.test_text)).setText("Bluetooth Disabled");
        }else{
            ((TextView) findViewById(R.id.test_text)).setText("Connecting...");

        }
    }

    //returns false if the app can not be started at all due to compatibility issues
    private boolean verifyAppPrerequisites(){
        // Check to determine whether or not BLE is supported on the device.
        if (!ConnectionManager.isBleSupported(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.ble_not_supported_title)
                    .setCancelable(false)
                    .setMessage(R.string.ble_not_supported_message)
                    .setPositiveButton("OK", new AlertDialog.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MainActivity.this.finish();
                        }
                    }).show();

            return false;
        }

        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            Intent intent = new Intent(this, PermissionsActivity.class);
            startActivity(intent);
        }

        return true;
    }

}
