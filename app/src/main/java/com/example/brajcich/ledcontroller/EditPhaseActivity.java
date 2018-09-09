package com.example.brajcich.ledcontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import com.example.brajcich.ledcontroller.Lamp.Phase;

public class EditPhaseActivity extends BluetoothConnectedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_phase);

        final SeekBar hue_seek = (SeekBar) findViewById(R.id.hue_seek);
        final SeekBar saturation_seek = (SeekBar) findViewById(R.id.saturation_seek);
        final SeekBar brightness_seek = (SeekBar) findViewById(R.id.brightness_seek);

        SeekBar.OnSeekBarChangeListener seekBarListener = (new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Color newColor = Color.getColorFromHSV(hue_seek.getProgress()/500d, saturation_seek.getProgress()/500d, brightness_seek.getProgress()/500d);
                communicationManager.previewColor(newColor);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        hue_seek.setOnSeekBarChangeListener(seekBarListener);
        saturation_seek.setOnSeekBarChangeListener(seekBarListener);
        brightness_seek.setOnSeekBarChangeListener(seekBarListener);

        findViewById(R.id.button_save_phase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitSavingPhase();
            }
        });

        findViewById(R.id.button_cancel_phase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void exitSavingPhase(){
        SeekBar hue_seek = (SeekBar) findViewById(R.id.hue_seek);
        SeekBar saturation_seek = (SeekBar) findViewById(R.id.saturation_seek);
        SeekBar brightness_seek = (SeekBar) findViewById(R.id.brightness_seek);

        Color color = Color.getColorFromHSV(hue_seek.getProgress()/500d, saturation_seek.getProgress()/500d, brightness_seek.getProgress()/500d);
        int holdTime = Integer.parseInt(((EditText) findViewById(R.id.edittext_hold_time)).getText().toString());
        int fadeTime = Integer.parseInt(((EditText) findViewById(R.id.edittext_hold_time)).getText().toString());

        Phase p = new Phase(color, holdTime, fadeTime);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("phase", p);
        resultIntent.putExtra("listIndex", getIntent().getIntExtra("listIndex", 0));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }
}
