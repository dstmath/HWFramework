package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DomainNameElement extends ANQPElement {
    private final List<String> mDomains;

    public DomainNameElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        this.mDomains = new ArrayList();
        while (payload.hasRemaining()) {
            this.mDomains.add(Constants.getPrefixedString(payload, 1, StandardCharsets.ISO_8859_1));
        }
    }

    public List<String> getDomains() {
        return Collections.unmodifiableList(this.mDomains);
    }

    public String toString() {
        return "DomainName{mDomains=" + this.mDomains + '}';
    }
}
