package com.android.internal.telephony.uicc;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telephony.Rlog;
import java.util.Arrays;

public class PlmnActRecord implements Parcelable {
    public static final int ACCESS_TECH_CDMA2000_1XRTT = 16;
    public static final int ACCESS_TECH_CDMA2000_HRPD = 32;
    public static final int ACCESS_TECH_EUTRAN = 16384;
    public static final int ACCESS_TECH_GSM = 128;
    public static final int ACCESS_TECH_GSM_COMPACT = 64;
    public static final int ACCESS_TECH_RESERVED = 16143;
    public static final int ACCESS_TECH_UTRAN = 32768;
    public static final Creator<PlmnActRecord> CREATOR = new Creator<PlmnActRecord>() {
        public PlmnActRecord createFromParcel(Parcel source) {
            return new PlmnActRecord(source.readString(), source.readInt(), null);
        }

        public PlmnActRecord[] newArray(int size) {
            return new PlmnActRecord[size];
        }
    };
    public static final int ENCODED_LENGTH = 5;
    private static final String LOG_TAG = "PlmnActRecord";
    private static final boolean VDBG = false;
    public final int accessTechs;
    public final String plmn;

    /* synthetic */ PlmnActRecord(String plmn, int accessTechs, PlmnActRecord -this2) {
        this(plmn, accessTechs);
    }

    public PlmnActRecord(byte[] bytes, int offset) {
        this.plmn = IccUtils.bcdPlmnToString(bytes, offset);
        this.accessTechs = (bytes[offset + 3] << 8) | bytes[offset + 4];
    }

    private PlmnActRecord(String plmn, int accessTechs) {
        this.plmn = plmn;
        this.accessTechs = accessTechs;
    }

    private String accessTechString() {
        if (this.accessTechs == 0) {
            return "NONE";
        }
        StringBuilder sb = new StringBuilder();
        if ((this.accessTechs & 32768) != 0) {
            sb.append("UTRAN|");
        }
        if ((this.accessTechs & 16384) != 0) {
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
        if ((this.accessTechs & ACCESS_TECH_RESERVED) != 0) {
            sb.append(String.format("UNKNOWN:%x|", new Object[]{Integer.valueOf(this.accessTechs & ACCESS_TECH_RESERVED)}));
        }
        return sb.substring(0, sb.length() - 1);
    }

    public String toString() {
        return String.format("{PLMN=%s,AccessTechs=%s}", new Object[]{this.plmn, accessTechString()});
    }

    public static PlmnActRecord[] getRecords(byte[] recordBytes) {
        if (recordBytes == null || recordBytes.length == 0 || recordBytes.length % 5 != 0) {
            String arrays;
            String str = LOG_TAG;
            StringBuilder append = new StringBuilder().append("Malformed PlmnActRecord, bytes: ");
            if (recordBytes != null) {
                arrays = Arrays.toString(recordBytes);
            } else {
                arrays = null;
            }
            Rlog.e(str, append.append(arrays).toString());
            return null;
        }
        int numRecords = recordBytes.length / 5;
        PlmnActRecord[] records = new PlmnActRecord[numRecords];
        for (int i = 0; i < numRecords; i++) {
            records[i] = new PlmnActRecord(recordBytes, i * 5);
        }
        return records;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.plmn);
        dest.writeInt(this.accessTechs);
    }
}
