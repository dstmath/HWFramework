package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;

public abstract class ANQPElement {
    private final ANQPElementType mID;

    protected ANQPElement(ANQPElementType id) {
        this.mID = id;
    }

    public ANQPElementType getID() {
        return this.mID;
    }
}
