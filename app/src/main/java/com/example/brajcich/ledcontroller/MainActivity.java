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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.bluetooth.le.ScanSettings.CALLBACK_TYPE_FIRST_MATCH;

public class MainActivity extends BluetoothConnectedActivity{

    private static final int REQUEST_ENABLE_BT = 1;

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
        onBluetoothEnabledOrDisabled();

        TextWatcher textWatcher = new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    short red = Short.parseShort(((EditText) findViewById(R.id.editText)).getText().toString());
                    short green = Short.parseShort(((EditText) findViewById(R.id.editText2)).getText().toString());
                    short blue = Short.parseShort(((EditText) findViewById(R.id.editText3)).getText().toString());

                    bluetoothManager.previewColor(new Color(red, green, blue));
                }catch(Exception e){
                    bluetoothManager.stopPreview();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        ((EditText) findViewById(R.id.editText)).addTextChangedListener(textWatcher);
        ((EditText) findViewById(R.id.editText2)).addTextChangedListener(textWatcher);
        ((EditText) findViewById(R.id.editText3)).addTextChangedListener(textWatcher);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothManager.changeDeviceName(((EditText) findViewById(R.id.editText4)).getText().toString());
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(abortLaunch) return;

        //bluetoothManager.initConnection();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(abortLaunch) return;

        //bluetoothManager.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK){
            bluetoothManager.initConnection();
        }

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
    public void onConnectionLost() {

        Log.d("MAIN_ACTIVITY", "Connection Lost (IN MAIN)");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView testText = (TextView) findViewById(R.id.test_text);
                testText.setText("Connection Lost.");
            }
        });
    }

    @Override
    protected void onBluetoothEnabledOrDisabled(){
        if(!bluetoothManager.isBluetoothEnabled()){
            ((TextView) findViewById(R.id.test_text)).setText("Bluetooth Disabled");
        }else{
            ((TextView) findViewById(R.id.test_text)).setText("Searching...");

        }
    }

    //returns false if the app can not be started at all due to compatibility issues
    private boolean verifyAppPrerequisites(){
        // Check to determine whether or not BLE is supported on the device.
        if (!LEDBluetoothManager.isBleSupported(this)) {
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
