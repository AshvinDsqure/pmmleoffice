package org.dspace.app.rest.utils;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import java.util.HashMap;
import java.util.Properties;

public class CustomDestinationDataProvider implements DestinationDataProvider {

    private DestinationDataEventListener eventListener;
    private final HashMap<String, Properties> destinationProperties = new HashMap<>();

    @Override
    public Properties getDestinationProperties(String destinationName) {
        return destinationProperties.get(destinationName);
    }

    @Override
    public void setDestinationDataEventListener(DestinationDataEventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public boolean supportsEvents() {
        return true;
    }

    public void addDestination(String destinationName, Properties properties) {
        destinationProperties.put(destinationName, properties);
        if (eventListener != null) {
            eventListener.updated(destinationName);
        }
    }
}