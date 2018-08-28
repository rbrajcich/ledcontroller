package com.example.brajcich.ledcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

public class EditLampActivity extends BluetoothConnectedActivity implements PhaseListItemCallback{

    private Lamp lamp;
    private PhaseListAdapter phaseListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_lamp);

        Intent callingIntent = getIntent();
        lamp = (Lamp) callingIntent.getSerializableExtra("lamp");

        // start with the current lamp name in the edittext. NOTE append used to put cursor at end
        ((EditText) findViewById(R.id.edittext_lamp_name)).append(lamp.getName());

        // add the event handler for clicking "save"
        findViewById(R.id.button_save_lamp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitSavingLamp();
            }
        });

        // add the event handler for clicking "cancel"
        findViewById(R.id.button_cancel_lamp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        phaseListAdapter = new PhaseListAdapter(this, this, lamp);
        ListView phaseListView = (ListView) findViewById(R.id.listview_phases);
        phaseListView.setAdapter(phaseListAdapter);

        /*
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditLampActivity.this, EditPhaseActivity.class);
                startActivity(intent);
            }
        });*/
    }

    @Override
    public void onRemovePhaseClicked(int position) {
        lamp.removePhaseAt(position);
        phaseListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAddPhaseAfterClicked(int position) {
        // do nothing
    }

    private void exitSavingLamp(){
        // save the name of the lamp
        String name = ((EditText) findViewById(R.id.edittext_lamp_name)).getText().toString();
        lamp.setName(name);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("lamp", lamp);
        resultIntent.putExtra("listIndex", getIntent().getIntExtra("listIndex", 0));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }

}
