package com.example.brajcich.ledcontroller;

/**
 * Created by Robert on 5/24/2018.
 */

public class Color {
    private short red;
    private short green;
    private short blue;

    public static byte[] getHexBytes(Color c){

        byte[] b = new byte[6];

        b[0] = Utility.charToHex(c.getRed() / 16);
        b[1] = Utility.charToHex(c.getRed() % 16);
        b[2] = Utility.charToHex(c.getGreen() / 16);
        b[3] = Utility.charToHex(c.getGreen() % 16);
        b[4] = Utility.charToHex(c.getBlue() / 16);
        b[5] = Utility.charToHex(c.getBlue() % 16);

        return b;
    }

    public static Color getColorFromHSV(double hue, double saturation, double brightness){

        int red;
        int green;
        int blue;

        if(hue <= (1/6d)){ //full red, rising green
            red = 255;
            green = (int) (hue*6*255);
            blue = 0;
        }else if(hue <= (2/6d)){ //full green, falling red
            red = 255 - (int) ((hue - (1/6d))*255*6);
            green = 255;
            blue = 0;
        }else if(hue <= (3/6d)){ //full green, rising blue
            red = 0;
            green = 255;
            blue = (int) ((hue - (2/6d))*255*6);
        }else if(hue <= (4/6d)){ //full blue, falling green
            red = 0;
            green = 255 - (int) ((hue - (3/6d))*255*6);
            blue = 255;
        }else if(hue <= (5/6d)) { //full blue, rising red
            red = (int) ((hue - (4/6d))*6*255);
            green = 0;
            blue = 255;
        }else{ //full red, falling blue
            red = 255;
            green = 0;
            blue = 255 - (int) ((hue - (5/6d))*6*255);
        }

        red = red + (int) ((255 - red)*(1-saturation));
        green = green + (int) ((255 - green)*(1-saturation));
        blue = blue + (int) ((255 - blue)*(1-saturation));

        short sRed = (short) (red*brightness);
        short sGreen = (short) (green*brightness);
        short sBlue = (short) (blue*brightness);

        return new Color(sRed, sGreen, sBlue);
    }

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
