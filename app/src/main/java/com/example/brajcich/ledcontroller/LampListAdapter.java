package com.example.brajcich.ledcontroller;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Robert on 8/22/2018.
 */

public class LampListAdapter extends BaseAdapter implements ListAdapter{

    private LayoutInflater layoutInflater;
    private ShowClickHandler showClickHandler;
    private List<Lamp> lamps;
    private Context context;

    public LampListAdapter(Context c, ShowClickHandler s, List<Lamp> l){
        context = c;
        showClickHandler = s;
        lamps = l;
        layoutInflater = LayoutInflater.from(c);
    }

    @Override
    public Object getItem(int i) {
        return lamps.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getCount() {
        return lamps.size();
    }

    @Override
    public CharSequence[] getAutofillOptions() {
        return new CharSequence[0];
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View newView = layoutInflater.inflate(R.layout.list_item_lamp, parent, false);
        ((TextView) newView.findViewById(R.id.textview_lamp_name)).setText(((Lamp) getItem(position)).getName());

        // set up callback to listen for clicks of "SHOW" button
        newView.findViewById(R.id.button_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Lamp l = (Lamp) LampListAdapter.this.getItem(position);
                showClickHandler.onShowClicked(l);
            }
        });

        return newView;
    }
}
