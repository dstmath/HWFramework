package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HSOsuProvidersElement extends ANQPElement {
    private final List<OSUProvider> mProviders;
    private final String mSSID;

    public HSOsuProvidersElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        this.mSSID = Constants.getPrefixedString(payload, 1, StandardCharsets.UTF_8);
        int providerCount = payload.get() & Constants.BYTE_MASK;
        this.mProviders = new ArrayList(providerCount);
        while (providerCount > 0) {
            this.mProviders.add(new OSUProvider(payload));
            providerCount--;
        }
    }

    public String getSSID() {
        return this.mSSID;
    }

    public List<OSUProvider> getProviders() {
        return Collections.unmodifiableList(this.mProviders);
    }

    public String toString() {
        return "HSOsuProviders{SSID='" + this.mSSID + '\'' + ", providers=" + this.mProviders + '}';
    }
}
