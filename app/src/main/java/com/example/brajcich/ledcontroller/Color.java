package com.example.brajcich.ledcontroller;

/**
 * Created by Robert on 5/24/2018.
 */

public class Color {
    private short red;
    private short green;
    private short blue;

    public Color(short r, short g, short b){
        red = r;
        green = g;
        blue = b;
    }

    public short getRed() {
        return red;
    }

    public void setRed(short red) {
        this.red = red;
    }

    public short getGreen() {
        return green;
    }

    public void setGreen(short green) {
        this.green = green;
    }

    public short getBlue() {
        return blue;
    }

    public void setBlue(short blue) {
        this.blue = blue;
    }
}
