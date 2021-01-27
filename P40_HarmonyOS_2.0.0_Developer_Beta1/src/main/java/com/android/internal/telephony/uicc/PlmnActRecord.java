package com.android.internal.telephony.uicc;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.Rlog;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Objects;

public class PlmnActRecord implements Parcelable {
    public static final int ACCESS_TECH_CDMA2000_1XRTT = 16;
    public static final int ACCESS_TECH_CDMA2000_HRPD = 32;
    public static final int ACCESS_TECH_EUTRAN = 16384;
    public static final int ACCESS_TECH_GSM = 128;
    public static final int ACCESS_TECH_GSM_COMPACT = 64;
    public static final int ACCESS_TECH_RESERVED = 16143;
    public static final int ACCESS_TECH_UTRAN = 32768;
    public static final Parcelable.Creator<PlmnActRecord> CREATOR = new Parcelable.Creator<PlmnActRecord>() {
        /* class com.android.internal.telephony.uicc.PlmnActRecord.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PlmnActRecord createFromParcel(Parcel source) {
            return new PlmnActRecord(source.readString(), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public PlmnActRecord[] newArray(int size) {
            return new PlmnActRecord[size];
        }
    };
    public static final int ENCODED_LENGTH = 5;
    private static final String LOG_TAG = "PlmnActRecord";
    private static final boolean VDBG = false;
    public final int accessTechs;
    public final String plmn;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AccessTech {
    }

    public PlmnActRecord(String plmn2, int accessTechs2) {
        this.plmn = plmn2;
        this.accessTechs = accessTechs2;
    }

    public PlmnActRecord(byte[] bytes, int offset) {
        this.plmn = IccUtils.bcdPlmnToString(bytes, offset);
        this.accessTechs = (Byte.toUnsignedInt(bytes[offset + 3]) << 8) | Byte.toUnsignedInt(bytes[offset + 4]);
    }

    public byte[] getBytes() {
        byte[] ret = new byte[5];
        IccUtils.stringToBcdPlmn(this.plmn, ret, 0);
        int i = this.accessTechs;
        ret[3] = (byte) (i >> 8);
        ret[4] = (byte) i;
        return ret;
    }

    private String accessTechString() {
        if (this.accessTechs == 0) {
            return "NONE";
        }
        StringBuilder sb = new StringBuilder();
        if ((this.accessTechs & 32768) != 0) {
            sb.append("UTRAN|");
        }
        if ((this.accessTechs & ACCESS_TECH_EUTRAN) != 0) {
            sb.append("EUTRAN|");
        }
        if ((this.accessTechs & 128) != 0) {
            sb.append("GSM|");
        }
        if ((this.accessTechs & 64) != 0) {
            sb.append("GSM_COMPACT|");
        }
        if ((this.accessTechs & 32) != 0) {
            sb.append("CDMA2000_HRPD|");
        }
        if ((this.accessTechs & 16) != 0) {
            sb.append("CDMA2000_1XRTT|");
        }
        int i = this.accessTechs;
        if ((i & ACCESS_TECH_RESERVED) != 0) {
            sb.append(String.format("UNKNOWN:%x|", Integer.valueOf(i & ACCESS_TECH_RESERVED)));
        }
        return sb.substring(0, sb.length() - 1);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x000d: APUT  (r0v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r1v1 java.lang.String) */
    @Override // java.lang.Object
    public String toString() {
        Object[] objArr = new Object[2];
        objArr[0] = Log.HWINFO ? this.plmn : "***";
        objArr[1] = accessTechString();
        return String.format("{PLMN=%s,AccessTechs=%s}", objArr);
    }

    public static PlmnActRecord[] getRecords(byte[] recordBytes) {
        if (recordBytes == null || recordBytes.length == 0 || recordBytes.length % 5 != 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Malformed PlmnActRecord, bytes: ");
            sb.append(recordBytes != null ? Arrays.toString(recordBytes) : null);
            Rlog.e(LOG_TAG, sb.toString());
            return null;
        }
        int numRecords = recordBytes.length / 5;
        PlmnActRecord[] records = new PlmnActRecord[numRecords];
        for (int i = 0; i < numRecords; i++) {
            records[i] = new PlmnActRecord(recordBytes, i * 5);
        }
        return records;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.plmn);
        dest.writeInt(this.accessTechs);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(this.plmn, Integer.valueOf(this.accessTechs));
    }

    @Override // java.lang.Object
    public boolean equals(Object rhs) {
        if (!(rhs instanceof PlmnActRecord)) {
            return false;
        }
        PlmnActRecord r = (PlmnActRecord) rhs;
        if (!this.plmn.equals(r.plmn) || this.accessTechs != r.accessTechs) {
            return false;
        }
        return true;
    }
}
