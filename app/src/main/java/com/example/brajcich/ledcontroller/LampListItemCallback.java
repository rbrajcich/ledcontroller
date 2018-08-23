package com.example.brajcich.ledcontroller;

/**
 * Created by Robert on 8/22/2018.
 */

public interface LampListItemCallback {
    void onShowLampClicked(Lamp lamp);
    void onDeleteLampClicked(int position);
}
