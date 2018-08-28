package com.example.brajcich.ledcontroller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.example.brajcich.ledcontroller.Lamp.Phase;

import java.util.List;

/**
 * Created by Robert on 8/27/2018.
 */

public class PhaseListAdapter extends BaseAdapter implements ListAdapter{

    private LayoutInflater layoutInflater;
    private PhaseListItemCallback phaseListItemCallback;
    private Lamp lamp;
    private Context context;

    public PhaseListAdapter(Context c, PhaseListItemCallback cb, Lamp lamp){
        context = c;
        phaseListItemCallback = cb;
        this.lamp = lamp;
        layoutInflater = LayoutInflater.from(c);
    }

    @Override
    public Object getItem(int i) {
        return lamp.getPhaseAt(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getCount() {
        return lamp.getPhaseCount();
    }

    @Override
    public CharSequence[] getAutofillOptions() {
        return new CharSequence[0];
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Phase phase = (Phase) getItem(position);
        View newView = layoutInflater.inflate(R.layout.list_item_phase, parent, false);


        String colorString = "Red: " + Short.toString(phase.color.getRed()) +
                                "   Green: " + Short.toString(phase.color.getGreen()) +
                                "   Blue: " + Short.toString(phase.color.getBlue());

        String timingString = "Hold: " + Integer.toString(phase.holdTime / 10) + " sec   Fade: " + Integer.toString(phase.fadeTime / 10) + " sec";

        ((TextView) newView.findViewById(R.id.textview_phase_color)).setText(colorString);
        ((TextView) newView.findViewById(R.id.textview_phase_timing)).setText(timingString);

        newView.findViewById(R.id.button_add_phase_after).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phaseListItemCallback.onAddPhaseAfterClicked(position);
            }
        });

        newView.findViewById(R.id.button_remove_phase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phaseListItemCallback.onRemovePhaseClicked(position);
            }
        });

        return newView;
    }
}
