package com.example.brajcich.ledcontroller;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BluetoothConnectedActivity implements LampListItemCallback {

    private static final int REQUEST_NEW_LAMP = 0;
    private static final int REQUEST_EDIT_LAMP = 1;

    private boolean abortLaunch = false;
    private List<Lamp> lampList;
    private LampListAdapter lampListAdapter;

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

        //Set up listener for "Add New Lamp" button
        findViewById(R.id.button_new_lamp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editLamp(null, 0);
            }
        });

        lampList = new ArrayList<>();

        Lamp testLamp1 = new Lamp("Blinker");
        testLamp1.addPhase(new Lamp.Phase(new Color((short) 255, (short) 0, (short) 0), 10, 0));
        testLamp1.addPhase(new Lamp.Phase(new Color((short) 0, (short) 255, (short) 0), 10, 0));

        Lamp testLamp2 = new Lamp("Fader");
        testLamp2.addPhase(new Lamp.Phase(new Color((short) 255, (short) 0, (short) 0), 0, 10));
        testLamp2.addPhase(new Lamp.Phase(new Color((short) 0, (short) 255, (short) 0), 0, 10));

        for(int i = 0; i < 10; i++) {
            lampList.add(testLamp1);
            lampList.add(testLamp2);
        }

        lampListAdapter = new LampListAdapter(this, this, lampList);
        ListView lampListView = (ListView) findViewById(R.id.listview_lamps);
        lampListView.setAdapter(lampListAdapter);

        // set up listener for clicks on items in the lamp list
        lampListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                editLamp((Lamp) lampListAdapter.getItem(i), i);
            }
        });
    }

    // opens edit lamp activity for given lamp, or for a new lamp if Lamp is null
    private void editLamp(Lamp l, int listIndex){
        int requestCode = REQUEST_EDIT_LAMP;

        if(l == null){
            l = new Lamp("MyLamp");
            requestCode = REQUEST_NEW_LAMP;
        }

        Intent intent = new Intent(MainActivity.this, EditLampActivity.class);
        intent.putExtra("lamp", l);
        intent.putExtra("listIndex", listIndex);
        startActivityForResult(intent, requestCode);
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
    public void onShowLampClicked(Lamp lamp) {
        communicationManager.startLamp(lamp);
    }

    @Override
    public void onDeleteLampClicked(int position) {
        lampList.remove(position);
        lampListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_NEW_LAMP && resultCode == Activity.RESULT_OK){
            Lamp l = (Lamp) data.getSerializableExtra("lamp");
            if(l != null){
                lampList.add(0, l);
                lampListAdapter.notifyDataSetChanged();
            }
        }else if(requestCode == REQUEST_EDIT_LAMP && resultCode == Activity.RESULT_OK){
            Lamp l = (Lamp) data.getSerializableExtra("lamp");
            int listIndex = data.getIntExtra("listIndex", 0);

            if(l != null){
                lampList.set(listIndex, l);
            }
        }
    }

    @Override
    public void onConnectionMade() {

        Log.d("MAIN_ACTIVITY", "Connection Made (IN MAIN)");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView testText = (TextView) findViewById(R.id.test_text);
                testText.setText("Connected");
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
