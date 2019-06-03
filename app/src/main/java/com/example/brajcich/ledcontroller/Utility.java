package com.example.brajcich.ledcontroller;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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

    public static String serialize(Serializable s){
        String serializedObject = "";

        // serialize the object
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(s);
            so.flush();
            byte[] byteArray = bo.toByteArray();
            serializedObject = new String(byteArray, "ISO-8859-1");
        } catch (Exception e) {}
        return serializedObject;
    }

    public static Serializable deserialize(String s){
        Serializable obj = null;

        // deserialize the object
        try {
            byte b[] = s.getBytes("ISO-8859-1");
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            obj = (Serializable) si.readObject();
        } catch (Exception e) {}

        return obj;
    }

}
