package com.android.server.wifi.hotspot2.anqp;

import com.android.server.wifi.hotspot2.anqp.Constants;

public abstract class ANQPElement {
    private final Constants.ANQPElementType mID;

    protected ANQPElement(Constants.ANQPElementType id) {
        this.mID = id;
    }

    public Constants.ANQPElementType getID() {
        return this.mID;
    }
}
