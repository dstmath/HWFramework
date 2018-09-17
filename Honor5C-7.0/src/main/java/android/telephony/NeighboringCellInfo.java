package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.content.NativeLibraryHelper;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;

public class NeighboringCellInfo implements Parcelable {
    public static final Creator<NeighboringCellInfo> CREATOR = null;
    public static final int UNKNOWN_CID = -1;
    public static final int UNKNOWN_RSSI = 99;
    private int mCid;
    private int mLac;
    private int mNetworkType;
    private int mPsc;
    private int mRssi;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.NeighboringCellInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.NeighboringCellInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.NeighboringCellInfo.<clinit>():void");
    }

    @Deprecated
    public NeighboringCellInfo() {
        this.mRssi = UNKNOWN_RSSI;
        this.mLac = UNKNOWN_CID;
        this.mCid = UNKNOWN_CID;
        this.mPsc = UNKNOWN_CID;
        this.mNetworkType = 0;
    }

    @Deprecated
    public NeighboringCellInfo(int rssi, int cid) {
        this.mRssi = rssi;
        this.mCid = cid;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public NeighboringCellInfo(int rssi, String location, int radioType) {
        this.mRssi = rssi;
        this.mNetworkType = 0;
        this.mPsc = UNKNOWN_CID;
        this.mLac = UNKNOWN_CID;
        this.mCid = UNKNOWN_CID;
        int l = location.length();
        if (l <= 8) {
            if (l < 8) {
                for (int i = 0; i < 8 - l; i++) {
                    location = "0" + location;
                }
            }
            switch (radioType) {
                case HwCfgFilePolicy.EMUI /*1*/:
                case HwCfgFilePolicy.PC /*2*/:
                    try {
                        this.mNetworkType = radioType;
                        if (!location.equalsIgnoreCase("FFFFFFFF")) {
                            this.mCid = Integer.parseInt(location.substring(4), 16);
                            this.mLac = Integer.parseInt(location.substring(0, 4), 16);
                            break;
                        }
                    } catch (NumberFormatException e) {
                        this.mPsc = UNKNOWN_CID;
                        this.mLac = UNKNOWN_CID;
                        this.mCid = UNKNOWN_CID;
                        this.mNetworkType = 0;
                        break;
                    }
                    break;
                case HwCfgFilePolicy.BASE /*3*/:
                case PGSdk.TYPE_VIDEO /*8*/:
                case PGSdk.TYPE_SCRLOCK /*9*/:
                case PGSdk.TYPE_CLOCK /*10*/:
                    this.mNetworkType = radioType;
                    this.mPsc = Integer.parseInt(location, 16);
                    break;
            }
        }
    }

    public NeighboringCellInfo(Parcel in) {
        this.mRssi = in.readInt();
        this.mLac = in.readInt();
        this.mCid = in.readInt();
        this.mPsc = in.readInt();
        this.mNetworkType = in.readInt();
    }

    public int getRssi() {
        return this.mRssi;
    }

    public int getLac() {
        return this.mLac;
    }

    public int getCid() {
        return this.mCid;
    }

    public int getPsc() {
        return this.mPsc;
    }

    public int getNetworkType() {
        return this.mNetworkType;
    }

    @Deprecated
    public void setCid(int cid) {
        this.mCid = cid;
    }

    @Deprecated
    public void setRssi(int rssi) {
        this.mRssi = rssi;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (this.mPsc != UNKNOWN_CID) {
            sb.append(Integer.toHexString(this.mPsc)).append("@").append(this.mRssi == UNKNOWN_RSSI ? NativeLibraryHelper.CLEAR_ABI_OVERRIDE : Integer.valueOf(this.mRssi));
        } else if (!(this.mLac == UNKNOWN_CID || this.mCid == UNKNOWN_CID)) {
            sb.append(Integer.toHexString(this.mLac)).append(Integer.toHexString(this.mCid)).append("@").append(this.mRssi == UNKNOWN_RSSI ? NativeLibraryHelper.CLEAR_ABI_OVERRIDE : Integer.valueOf(this.mRssi));
        }
        sb.append("]");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mLac);
        dest.writeInt(this.mCid);
        dest.writeInt(this.mPsc);
        dest.writeInt(this.mNetworkType);
    }
}
