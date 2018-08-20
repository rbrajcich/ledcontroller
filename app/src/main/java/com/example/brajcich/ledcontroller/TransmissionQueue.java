package com.example.brajcich.ledcontroller;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Robert on 5/24/2018.
 */

public class TransmissionQueue {

    private Queue<byte[]> queue;
    private Color previewColor;

    public TransmissionQueue(){
        queue = new LinkedList<>();
        previewColor = null;
    }

    public void queueTransmission(byte[] data){
        synchronized(queue) {
            queue.add(data);
        }
    }

    public void showPreview(Color preview){
        synchronized(queue) {
            previewColor = preview;
        }
    }

    public byte[] pop(){
        synchronized(queue) {
            if (previewColor != null) {
                byte[] toReturn = getPreviewTransmission();
                previewColor = null;
                return toReturn;
            } else {
                return queue.poll();
            }
        }
    }

    private byte[] getPreviewTransmission(){
        byte[] b = new byte[9];

        b[0] = '*';
        b[1] = 'P';
        System.arraycopy(Color.getHexBytes(previewColor), 0, b, 2, 6);
        b[8] = '#';

        return b;
    }


}
