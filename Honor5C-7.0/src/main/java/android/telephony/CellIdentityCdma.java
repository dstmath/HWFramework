package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.os.HwBootFail;
import java.util.Objects;

public final class CellIdentityCdma implements Parcelable {
    public static final Creator<CellIdentityCdma> CREATOR = null;
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellSignalStrengthCdma";
    private final int mBasestationId;
    private final int mLatitude;
    private final int mLongitude;
    private final int mNetworkId;
    private final int mSystemId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.CellIdentityCdma.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.CellIdentityCdma.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.CellIdentityCdma.<clinit>():void");
    }

    public CellIdentityCdma() {
        this.mNetworkId = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mSystemId = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mBasestationId = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mLongitude = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mLatitude = HwBootFail.STAGE_BOOT_SUCCESS;
    }

    public CellIdentityCdma(int nid, int sid, int bid, int lon, int lat) {
        this.mNetworkId = nid;
        this.mSystemId = sid;
        this.mBasestationId = bid;
        this.mLongitude = lon;
        this.mLatitude = lat;
    }

    private CellIdentityCdma(CellIdentityCdma cid) {
        this.mNetworkId = cid.mNetworkId;
        this.mSystemId = cid.mSystemId;
        this.mBasestationId = cid.mBasestationId;
        this.mLongitude = cid.mLongitude;
        this.mLatitude = cid.mLatitude;
    }

    CellIdentityCdma copy() {
        return new CellIdentityCdma(this);
    }

    public int getNetworkId() {
        return this.mNetworkId;
    }

    public int getSystemId() {
        return this.mSystemId;
    }

    public int getBasestationId() {
        return this.mBasestationId;
    }

    public int getLongitude() {
        return this.mLongitude;
    }

    public int getLatitude() {
        return this.mLatitude;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mNetworkId), Integer.valueOf(this.mSystemId), Integer.valueOf(this.mBasestationId), Integer.valueOf(this.mLatitude), Integer.valueOf(this.mLongitude)});
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof CellIdentityCdma)) {
            return DBG;
        }
        CellIdentityCdma o = (CellIdentityCdma) other;
        if (this.mNetworkId != o.mNetworkId || this.mSystemId != o.mSystemId || this.mBasestationId != o.mBasestationId || this.mLatitude != o.mLatitude) {
            z = DBG;
        } else if (this.mLongitude != o.mLongitude) {
            z = DBG;
        }
        return z;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CellIdentityCdma:{");
        sb.append(" mNetworkId=");
        sb.append(this.mNetworkId);
        sb.append(" mSystemId=");
        sb.append(this.mSystemId);
        sb.append(" mBasestationId=");
        sb.append(this.mBasestationId);
        sb.append(" mLongitude=");
        sb.append(this.mLongitude);
        sb.append(" mLatitude=");
        sb.append(this.mLatitude);
        sb.append("}");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mNetworkId);
        dest.writeInt(this.mSystemId);
        dest.writeInt(this.mBasestationId);
        dest.writeInt(this.mLongitude);
        dest.writeInt(this.mLatitude);
    }

    private CellIdentityCdma(Parcel in) {
        this.mNetworkId = in.readInt();
        this.mSystemId = in.readInt();
        this.mBasestationId = in.readInt();
        this.mLongitude = in.readInt();
        this.mLatitude = in.readInt();
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
