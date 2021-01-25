package com.android.server.backup.transport;

import android.util.AndroidException;

public class TransportNotAvailableException extends AndroidException {
    TransportNotAvailableException() {
        super("Transport not available");
    }
}
