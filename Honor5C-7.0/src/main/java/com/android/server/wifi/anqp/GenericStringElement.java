package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class GenericStringElement extends ANQPElement {
    private final String mText;

    public GenericStringElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        this.mText = Constants.getString(payload, payload.remaining(), StandardCharsets.UTF_8);
    }

    public String getM_text() {
        return this.mText;
    }

    public String toString() {
        return "Element ID " + getID() + ": '" + this.mText + "'";
    }
}
