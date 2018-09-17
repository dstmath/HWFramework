package com.android.server.wifi.hotspot2.anqp;

import android.util.Log;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CellularNetwork {
    public static final int IEI_CONTENT_LENGTH_MASK = 127;
    public static final int IEI_TYPE_PLMN_LIST = 0;
    private static final int MNC_2DIGIT_VALUE = 15;
    public static final int PLMN_DATA_BYTES = 3;
    private static final String TAG = "CellularNetwork";
    private final List<String> mPlmnList;

    public CellularNetwork(List<String> plmnList) {
        this.mPlmnList = plmnList;
    }

    public static CellularNetwork parse(ByteBuffer payload) throws ProtocolException {
        int ieiType = payload.get() & Constants.BYTE_MASK;
        int ieiSize = payload.get() & 127;
        if (ieiType != 0) {
            Log.e(TAG, "Ignore unsupported IEI Type: " + ieiType);
            payload.position(payload.position() + ieiSize);
            return null;
        }
        int plmnCount = payload.get() & Constants.BYTE_MASK;
        if (ieiSize != (plmnCount * 3) + 1) {
            throw new ProtocolException("IEI size and PLMN count mismatched: IEI Size=" + ieiSize + " PLMN Count=" + plmnCount);
        }
        List<String> plmnList = new ArrayList();
        while (plmnCount > 0) {
            plmnList.add(parsePlmn(payload));
            plmnCount--;
        }
        return new CellularNetwork(plmnList);
    }

    public List<String> getPlmns() {
        return Collections.unmodifiableList(this.mPlmnList);
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof CellularNetwork)) {
            return false;
        }
        return this.mPlmnList.equals(((CellularNetwork) thatObject).mPlmnList);
    }

    public int hashCode() {
        return this.mPlmnList.hashCode();
    }

    public String toString() {
        return "CellularNetwork{mPlmnList=" + this.mPlmnList + "}";
    }

    private static String parsePlmn(ByteBuffer payload) {
        byte[] plmn = new byte[3];
        payload.get(plmn);
        int mcc = (((plmn[0] << 8) & 3840) | (plmn[0] & 240)) | (plmn[1] & 15);
        int mnc = ((plmn[2] << 4) & 240) | ((plmn[2] >> 4) & 15);
        if (((plmn[1] >> 4) & 15) != 15) {
            return String.format("%03x%03x", new Object[]{Integer.valueOf(mcc), Integer.valueOf((mnc << 4) | ((plmn[1] >> 4) & 15))});
        }
        return String.format("%03x%02x", new Object[]{Integer.valueOf(mcc), Integer.valueOf(mnc)});
    }
}
