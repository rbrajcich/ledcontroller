package com.example.brajcich.ledcontroller;

/**
 * Created by Robert on 8/19/2018.
 */

public class Utility {

    public static byte charToHex(int b){
        if(b < 10){
            return (byte) (b + '0');
        }else{
            return (byte) (b + 'A' - 10);
        }
    }

}
