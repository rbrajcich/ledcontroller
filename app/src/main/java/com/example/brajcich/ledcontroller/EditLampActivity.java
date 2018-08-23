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

public class EditLampActivity extends BluetoothConnectedActivity {

    private Lamp lamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_lamp);

        Intent callingIntent = getIntent();
        lamp = (Lamp) callingIntent.getSerializableExtra("lamp");

        ((EditText) findViewById(R.id.edittext_lamp_name)).setText(lamp.getName());

        /*
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditLampActivity.this, EditPhaseActivity.class);
                startActivity(intent);
            }
        });*/
    }

    private void exitSavingLamp(){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("lamp", lamp);
        resultIntent.putExtra("listIndex", getIntent().getIntExtra("listIndex", 0));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        exitSavingLamp();
    }
}
