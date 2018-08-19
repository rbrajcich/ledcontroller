package com.example.brajcich.ledcontroller;

/**
 * Created by Robert on 8/18/2018.
 */

public class CommunicationManager {

    private static CommunicationManager instance;

    public static CommunicationManager getInstance(ConnectionManager cm){
        if(instance == null){
            instance = new CommunicationManager(cm);
        }

        return instance;
    }

    private ConnectionManager connectionManager;

    private CommunicationManager(ConnectionManager cm){
        connectionManager = cm;
        cm.registerCommunicationManager(this);
    }
}
