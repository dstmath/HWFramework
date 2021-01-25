package com.android.server.wifi.hotspot2.anqp.eap;

import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.ByteBufferReader;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ExpandedEAPMethod extends AuthParam {
    public static final int EXPECTED_LENGTH_VALUE = 7;
    private final int mVendorID;
    private final long mVendorType;

    @VisibleForTesting
    public ExpandedEAPMethod(int authType, int vendorID, long vendorType) {
        super(authType);
        this.mVendorID = vendorID;
        this.mVendorType = vendorType;
    }

    public static ExpandedEAPMethod parse(ByteBuffer payload, int length, boolean inner) throws ProtocolException {
        if (length == 7) {
            int vendorID = ((int) ByteBufferReader.readInteger(payload, ByteOrder.BIG_ENDIAN, 3)) & 16777215;
            int authType = 4;
            long vendorType = ByteBufferReader.readInteger(payload, ByteOrder.BIG_ENDIAN, 4) & -1;
            if (!inner) {
                authType = 1;
            }
            return new ExpandedEAPMethod(authType, vendorID, vendorType);
        }
        throw new ProtocolException("Invalid length value: " + length);
    }

    public int getVendorID() {
        return this.mVendorID;
    }

    public long getVendorType() {
        return this.mVendorType;
    }

    public boolean equals(Object thatObject) {
        if (thatObject == this) {
            return true;
        }
        if (!(thatObject instanceof ExpandedEAPMethod)) {
            return false;
        }
        ExpandedEAPMethod that = (ExpandedEAPMethod) thatObject;
        if (this.mVendorID == that.mVendorID && this.mVendorType == that.mVendorType) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (this.mVendorID * 31) + ((int) this.mVendorType);
    }

    public String toString() {
        return "ExpandedEAPMethod{mVendorID=" + this.mVendorID + " mVendorType=" + this.mVendorType + "}";
    }
}
