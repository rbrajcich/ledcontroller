package com.example.brajcich.ledcontroller;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Robert on 8/19/2018.
 */

public class Lamp {

    private List<Phase> phases;
    private String name;

    public Lamp(String name){
        phases = new LinkedList<>();
        this.name = name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public byte[] getLampTransmission(){
        int buffLength = 3 + (14 * phases.size());
        byte[] descriptor = new byte[buffLength];

        descriptor[0] = '*';
        descriptor[1] = 'L';

        int currentNum = 0;
        for(Phase p : phases){
            System.arraycopy(p.getDescriptor(), 0, descriptor, 2 + (14 * currentNum), 14);
            currentNum++;
        }

        descriptor[buffLength - 1] = '#';

        return descriptor;
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

    public static class Phase {
        public Color color;
        public int holdTime;  // time to display this color (in tenths of seconds)
        public int fadeTime;    // time for fade to next color (0 for instant jump)
        public byte[] descriptor; 

        public Phase(Color c, int displayTime, int transitionTime){
            color = c;
            holdTime = displayTime;
            fadeTime = transitionTime;
            descriptor = new byte[14];

            // copy color bytes in
            System.arraycopy(Color.getHexBytes(color), 0, descriptor, 0, 6);
            
            int temp;

            //copy hold time bytes in
            descriptor[6] = Utility.charToHex(holdTime / 4096);
            temp = holdTime % 4096;
            descriptor[7] = Utility.charToHex(temp / 256);
            temp = temp % 256;
            descriptor[8] = Utility.charToHex(temp / 16);
            temp = temp % 16;
            descriptor[9] = Utility.charToHex(temp);

            //copy fade time bytes in
            descriptor[10] = Utility.charToHex(fadeTime / 4096);
            temp = fadeTime % 4096;
            descriptor[11] = Utility.charToHex(temp / 256);
            temp = fadeTime % 256;
            descriptor[12] = Utility.charToHex(temp / 16);
            temp = temp % 16;
            descriptor[13] = Utility.charToHex(temp);
        }

        public byte[] getDescriptor(){
            return descriptor;                
        }
    }
}
