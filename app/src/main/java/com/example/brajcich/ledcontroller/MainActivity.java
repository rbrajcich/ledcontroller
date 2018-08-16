package com.example.brajcich.ledcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

public class MainActivity extends AppCompatActivity  implements LEDBluetoothCallback{

    private static final int REQUEST_ENABLE_BT = 1;

    private LEDBluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothManager = LEDBluetoothManager.getInstance(this, this);

        // Use this check to determine whether BLE is supported on the device.
        if (!bluetoothManager.isBleSupported()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.ble_not_supported_title)
                        .setCancelable(false)
                        .setMessage(R.string.ble_not_supported_message)
                        .setPositiveButton("OK", new AlertDialog.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainActivity.this.finish();
                            }
                        }).show();
        }else{
            setContentView(R.layout.activity_main);
        }

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

        //checks if bluetooth is enabled. if not, enable it.
        if(!bluetoothManager.isBluetoothEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            bluetoothManager.initConnection();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetoothManager.disconnect();
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

}
