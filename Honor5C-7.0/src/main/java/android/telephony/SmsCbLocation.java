package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SmsCbLocation implements Parcelable {
    public static final Creator<SmsCbLocation> CREATOR = null;
    private final int mCid;
    private final int mLac;
    private final String mPlmn;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.SmsCbLocation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.SmsCbLocation.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsCbLocation.<clinit>():void");
    }

    public SmsCbLocation() {
        this.mPlmn = "";
        this.mLac = -1;
        this.mCid = -1;
    }

    public SmsCbLocation(String plmn) {
        this.mPlmn = plmn;
        this.mLac = -1;
        this.mCid = -1;
    }

    public SmsCbLocation(String plmn, int lac, int cid) {
        this.mPlmn = plmn;
        this.mLac = lac;
        this.mCid = cid;
    }

    public SmsCbLocation(Parcel in) {
        this.mPlmn = in.readString();
        this.mLac = in.readInt();
        this.mCid = in.readInt();
    }

    public String getPlmn() {
        return this.mPlmn;
    }

    public int getLac() {
        return this.mLac;
    }

    public int getCid() {
        return this.mCid;
    }

    public int hashCode() {
        return (((this.mPlmn.hashCode() * 31) + this.mLac) * 31) + this.mCid;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof SmsCbLocation)) {
            return false;
        }
        SmsCbLocation other = (SmsCbLocation) o;
        if (!(this.mPlmn.equals(other.mPlmn) && this.mLac == other.mLac && this.mCid == other.mCid)) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return '[' + this.mPlmn + ',' + this.mLac + ',' + this.mCid + ']';
    }

    public boolean isInLocationArea(SmsCbLocation area) {
        if (this.mCid != -1 && this.mCid != area.mCid) {
            return false;
        }
        if (this.mLac == -1 || this.mLac == area.mLac) {
            return this.mPlmn.equals(area.mPlmn);
        }
        return false;
    }

    public boolean isInLocationArea(String plmn, int lac, int cid) {
        if (!this.mPlmn.equals(plmn)) {
            return false;
        }
        if (this.mLac != -1 && this.mLac != lac) {
            return false;
        }
        if (this.mCid == -1 || this.mCid == cid) {
            return true;
        }
        return false;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPlmn);
        dest.writeInt(this.mLac);
        dest.writeInt(this.mCid);
    }

    public int describeContents() {
        return 0;
    }
}
