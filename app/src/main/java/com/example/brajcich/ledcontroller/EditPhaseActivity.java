package com.example.brajcich.ledcontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;

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
                Color newColor = Color.getColorFromHSV(hue_seek.getProgress()/100d, saturation_seek.getProgress()/100d, brightness_seek.getProgress()/100d);
                getWindow().getDecorView().setBackgroundColor(android.graphics.Color.rgb(newColor.getRed(), newColor.getGreen(), newColor.getBlue()));
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

    }
}
