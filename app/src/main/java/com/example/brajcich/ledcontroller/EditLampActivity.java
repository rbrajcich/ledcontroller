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
import com.example.brajcich.ledcontroller.Lamp.Phase;

public class EditLampActivity extends BluetoothConnectedActivity implements PhaseListItemCallback{

    private static final int REQUEST_NEW_PHASE = 0;
    private static final int REQUEST_EDIT_PHASE = 1;

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
        editPhase(null, position + 1);
    }

    private void editPhase(Phase p, int index){
        int requestCode = REQUEST_EDIT_PHASE;

        if(p == null){
            p = new Phase(new Color((short) 0, (short) 0, (short) 0), 0, 0);
            requestCode = REQUEST_NEW_PHASE;
        }

        Intent intent = new Intent(EditLampActivity.this, EditPhaseActivity.class);
        intent.putExtra("phase", p);
        intent.putExtra("listIndex", index);
        startActivityForResult(intent, requestCode);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_NEW_PHASE && resultCode == Activity.RESULT_OK){
            Phase p = (Phase) data.getSerializableExtra("phase");
            if(p != null){
                lamp.addPhaseAt(data.getIntExtra("listIndex", 0), p);
                phaseListAdapter.notifyDataSetChanged();
            }
        }else if(requestCode == REQUEST_EDIT_PHASE && resultCode == Activity.RESULT_OK){
            Phase p = (Phase) data.getSerializableExtra("phase");
            int listIndex = data.getIntExtra("listIndex", 0);
            if(p != null){
                lamp.removePhaseAt(listIndex);
                lamp.addPhaseAt(listIndex, p);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }

}
