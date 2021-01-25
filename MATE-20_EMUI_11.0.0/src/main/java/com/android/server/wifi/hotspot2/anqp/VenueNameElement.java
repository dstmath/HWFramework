package com.android.server.wifi.hotspot2.anqp;

import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VenueNameElement extends ANQPElement {
    @VisibleForTesting
    public static final int MAXIMUM_VENUE_NAME_LENGTH = 252;
    @VisibleForTesting
    public static final int VENUE_INFO_LENGTH = 2;
    private final List<I18Name> mNames;

    @VisibleForTesting
    public VenueNameElement(List<I18Name> names) {
        super(Constants.ANQPElementType.ANQPVenueName);
        this.mNames = names;
    }

    public static VenueNameElement parse(ByteBuffer payload) throws ProtocolException {
        for (int i = 0; i < 2; i++) {
            payload.get();
        }
        List<I18Name> names = new ArrayList<>();
        while (payload.hasRemaining()) {
            I18Name name = I18Name.parse(payload);
            int textBytes = name.getText().getBytes(StandardCharsets.UTF_8).length;
            if (textBytes <= 252) {
                names.add(name);
            } else {
                throw new ProtocolException("Venue Name exceeds the maximum allowed " + textBytes);
            }
        }
        return new VenueNameElement(names);
    }

    public List<I18Name> getNames() {
        return Collections.unmodifiableList(this.mNames);
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof VenueNameElement)) {
            return false;
        }
        return this.mNames.equals(((VenueNameElement) thatObject).mNames);
    }

    public int hashCode() {
        return this.mNames.hashCode();
    }

    public String toString() {
        return "VenueName{ mNames=" + this.mNames + "}";
    }
}
