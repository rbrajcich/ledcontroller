package com.example.brajcich.ledcontroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert on 8/19/2018.
 */

public class Lamp {

    private List<Phase> phases;
    private String name;

    public Lamp(String name){
        phases = new ArrayList<>();
        this.name = name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public byte[] getDescriptor(){
        return new byte[1];
    }

    public void addPhase(Phase p){
        phases.add(p);
    }

    public void addPhaseAt(int index, Phase p){
        phases.add(index, p);
    }

    public void removePhaseAt(int index){
        phases.remove(index);
    }

    public Phase getPhaseAt(int index){
        return phases.get(index);
    }

    public class Phase {
        public Color color;
        public int displayMillis;  // time to display this color (0 for only 1 solid color)
        public int transitionMillis;    // time for fade to next color (0 for instant jump)

        public Phase(Color c, int displayTime, int transitionTime){
            color = c;
            displayMillis = displayTime;
            transitionMillis = transitionTime;
        }
    }
}
