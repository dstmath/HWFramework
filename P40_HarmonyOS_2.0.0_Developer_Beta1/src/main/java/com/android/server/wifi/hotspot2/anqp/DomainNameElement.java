package com.android.server.wifi.hotspot2.anqp;

import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.ByteBufferReader;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DomainNameElement extends ANQPElement {
    private final List<String> mDomains;

    @VisibleForTesting
    public DomainNameElement(List<String> domains) {
        super(Constants.ANQPElementType.ANQPDomName);
        this.mDomains = domains;
    }

    public static DomainNameElement parse(ByteBuffer payload) {
        List<String> domains = new ArrayList<>();
        while (payload.hasRemaining()) {
            domains.add(ByteBufferReader.readStringWithByteLength(payload, StandardCharsets.ISO_8859_1));
        }
        return new DomainNameElement(domains);
    }

    public List<String> getDomains() {
        return Collections.unmodifiableList(this.mDomains);
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof DomainNameElement)) {
            return false;
        }
        return this.mDomains.equals(((DomainNameElement) thatObject).mDomains);
    }

    public int hashCode() {
        return this.mDomains.hashCode();
    }

    public String toString() {
        return "DomainName{mDomains=" + this.mDomains + '}';
    }
}
