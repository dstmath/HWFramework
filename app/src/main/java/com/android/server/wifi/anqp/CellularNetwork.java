package com.android.server.wifi.anqp;

import com.android.server.wifi.util.InformationElementUtil.SupportedRates;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CellularNetwork implements Iterable<String> {
    private static final int PLMNListType = 0;
    private final List<String> mMccMnc;

    private CellularNetwork(int plmnCount, ByteBuffer payload) throws ProtocolException {
        this.mMccMnc = new ArrayList(plmnCount);
        while (plmnCount > 0) {
            if (payload.remaining() < 3) {
                throw new ProtocolException("Truncated PLMN info");
            }
            String mccMnc;
            byte[] plmn = new byte[3];
            payload.get(plmn);
            int mcc = (((plmn[0] << 8) & 3840) | (plmn[0] & 240)) | (plmn[1] & 15);
            int mnc = ((plmn[2] << 4) & 240) | ((plmn[2] >> 4) & 15);
            if (((plmn[1] >> 4) & 15) != 15) {
                mccMnc = String.format("%03x%03x", new Object[]{Integer.valueOf(mcc), Integer.valueOf((mnc << 4) | ((plmn[1] >> 4) & 15))});
            } else {
                mccMnc = String.format("%03x%02x", new Object[]{Integer.valueOf(mcc), Integer.valueOf(mnc)});
            }
            this.mMccMnc.add(mccMnc);
            plmnCount--;
        }
    }

    public static CellularNetwork buildCellularNetwork(ByteBuffer payload) throws ProtocolException {
        int plmnLen = payload.get() & SupportedRates.MASK;
        if ((payload.get() & Constants.BYTE_MASK) == 0) {
            return new CellularNetwork(payload.get() & Constants.BYTE_MASK, payload);
        }
        payload.position(payload.position() + plmnLen);
        return null;
    }

    public Iterator<String> iterator() {
        return this.mMccMnc.iterator();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("PLMN:");
        for (String mccMnc : this.mMccMnc) {
            sb.append(' ').append(mccMnc);
        }
        return sb.toString();
    }
}
