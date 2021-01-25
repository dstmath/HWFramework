package com.android.server.wifi.hotspot2.anqp;

import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HSFriendlyNameElement extends ANQPElement {
    @VisibleForTesting
    public static final int MAXIMUM_OPERATOR_NAME_LENGTH = 252;
    private final List<I18Name> mNames;

    @VisibleForTesting
    public HSFriendlyNameElement(List<I18Name> names) {
        super(Constants.ANQPElementType.HSFriendlyName);
        this.mNames = names;
    }

    public static HSFriendlyNameElement parse(ByteBuffer payload) throws ProtocolException {
        List<I18Name> names = new ArrayList<>();
        while (payload.hasRemaining()) {
            I18Name name = I18Name.parse(payload);
            int textBytes = name.getText().getBytes(StandardCharsets.UTF_8).length;
            if (textBytes <= 252) {
                names.add(name);
            } else {
                throw new ProtocolException("Operator Name exceeds the maximum allowed " + textBytes);
            }
        }
        return new HSFriendlyNameElement(names);
    }

    public List<I18Name> getNames() {
        return Collections.unmodifiableList(this.mNames);
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof HSFriendlyNameElement)) {
            return false;
        }
        return this.mNames.equals(((HSFriendlyNameElement) thatObject).mNames);
    }

    public int hashCode() {
        return this.mNames.hashCode();
    }

    public String toString() {
        return "HSFriendlyName{mNames=" + this.mNames + '}';
    }
}
