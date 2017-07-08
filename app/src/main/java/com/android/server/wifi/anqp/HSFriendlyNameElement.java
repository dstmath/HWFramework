package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HSFriendlyNameElement extends ANQPElement {
    private final List<I18Name> mNames;

    public HSFriendlyNameElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        this.mNames = new ArrayList();
        while (payload.hasRemaining()) {
            this.mNames.add(new I18Name(payload));
        }
    }

    public List<I18Name> getNames() {
        return Collections.unmodifiableList(this.mNames);
    }

    public String toString() {
        return "HSFriendlyName{mNames=" + this.mNames + '}';
    }
}
