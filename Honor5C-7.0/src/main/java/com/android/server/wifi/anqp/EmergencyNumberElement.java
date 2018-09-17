package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EmergencyNumberElement extends ANQPElement {
    private final List<String> mNumbers;

    public EmergencyNumberElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        this.mNumbers = new ArrayList();
        while (payload.hasRemaining()) {
            this.mNumbers.add(Constants.getPrefixedString(payload, 1, StandardCharsets.UTF_8));
        }
    }

    public List<String> getNumbers() {
        return this.mNumbers;
    }

    public String toString() {
        return "EmergencyNumber{mNumbers=" + this.mNumbers + '}';
    }
}
