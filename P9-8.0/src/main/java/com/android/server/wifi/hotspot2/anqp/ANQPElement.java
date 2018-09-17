package com.android.server.wifi.hotspot2.anqp;

import com.android.server.wifi.hotspot2.anqp.Constants.ANQPElementType;

public abstract class ANQPElement {
    private final ANQPElementType mID;

    protected ANQPElement(ANQPElementType id) {
        this.mID = id;
    }

    public ANQPElementType getID() {
        return this.mID;
    }
}
