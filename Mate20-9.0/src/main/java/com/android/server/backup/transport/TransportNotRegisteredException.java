package com.android.server.backup.transport;

import android.content.ComponentName;
import android.util.AndroidException;

public class TransportNotRegisteredException extends AndroidException {
    public TransportNotRegisteredException(String transportName) {
        super("Transport " + transportName + " not registered");
    }

    public TransportNotRegisteredException(ComponentName transportComponent) {
        super("Transport for host " + transportComponent + " not registered");
    }
}
