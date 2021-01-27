package com.android.internal.telephony.uicc.euicc;

import android.telephony.Rlog;
import com.android.internal.telephony.uicc.asn1.Asn1Decoder;
import com.android.internal.telephony.uicc.asn1.Asn1Node;
import com.android.internal.telephony.uicc.asn1.InvalidAsn1DataException;
import com.android.internal.telephony.uicc.asn1.TagNotFoundException;
import java.util.Arrays;

public final class EuiccSpecVersion implements Comparable<EuiccSpecVersion> {
    private static final String LOG_TAG = "EuiccSpecVer";
    private static final int TAG_ISD_R_APP_TEMPLATE = 224;
    private static final int TAG_VERSION = 130;
    private final int[] mVersionValues;

    public static EuiccSpecVersion fromOpenChannelResponse(byte[] response) {
        byte[] versionType;
        try {
            Asn1Decoder decoder = new Asn1Decoder(response);
            if (!decoder.hasNextNode()) {
                return null;
            }
            Asn1Node node = decoder.nextNode();
            try {
                if (node.getTag() == 224) {
                    versionType = node.getChild(130, new int[0]).asBytes();
                } else {
                    versionType = node.getChild(224, new int[]{130}).asBytes();
                }
                if (versionType.length == 3) {
                    return new EuiccSpecVersion(versionType);
                }
                Rlog.e(LOG_TAG, "Cannot parse select response of ISD-R: " + node.toHex());
                return null;
            } catch (InvalidAsn1DataException | TagNotFoundException e) {
                Rlog.e(LOG_TAG, "Cannot parse select response of ISD-R: " + node.toHex());
            }
        } catch (InvalidAsn1DataException e2) {
            Rlog.e(LOG_TAG, "Cannot parse the select response of ISD-R.", e2);
            return null;
        }
    }

    public EuiccSpecVersion(int major, int minor, int revision) {
        this.mVersionValues = new int[3];
        int[] iArr = this.mVersionValues;
        iArr[0] = major;
        iArr[1] = minor;
        iArr[2] = revision;
    }

    public EuiccSpecVersion(byte[] version) {
        this.mVersionValues = new int[3];
        int[] iArr = this.mVersionValues;
        iArr[0] = version[0] & 255;
        iArr[1] = version[1] & 255;
        iArr[2] = version[2] & 255;
    }

    public int getMajor() {
        return this.mVersionValues[0];
    }

    public int getMinor() {
        return this.mVersionValues[1];
    }

    public int getRevision() {
        return this.mVersionValues[2];
    }

    public int compareTo(EuiccSpecVersion that) {
        if (getMajor() > that.getMajor()) {
            return 1;
        }
        if (getMajor() < that.getMajor()) {
            return -1;
        }
        if (getMinor() > that.getMinor()) {
            return 1;
        }
        if (getMinor() < that.getMinor()) {
            return -1;
        }
        if (getRevision() > that.getRevision()) {
            return 1;
        }
        if (getRevision() < that.getRevision()) {
            return -1;
        }
        return 0;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Arrays.equals(this.mVersionValues, ((EuiccSpecVersion) obj).mVersionValues);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Arrays.hashCode(this.mVersionValues);
    }

    @Override // java.lang.Object
    public String toString() {
        return this.mVersionValues[0] + "." + this.mVersionValues[1] + "." + this.mVersionValues[2];
    }
}
