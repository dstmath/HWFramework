package android.service.carrier;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CarrierIdentifier implements Parcelable {
    public static final Creator<CarrierIdentifier> CREATOR = null;
    private String mGid1;
    private String mGid2;
    private String mImsi;
    private String mMcc;
    private String mMnc;
    private String mSpn;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.service.carrier.CarrierIdentifier.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.service.carrier.CarrierIdentifier.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierIdentifier.<clinit>():void");
    }

    public CarrierIdentifier(String mcc, String mnc, String spn, String imsi, String gid1, String gid2) {
        this.mMcc = mcc;
        this.mMnc = mnc;
        this.mSpn = spn;
        this.mImsi = imsi;
        this.mGid1 = gid1;
        this.mGid2 = gid2;
    }

    public CarrierIdentifier(Parcel parcel) {
        readFromParcel(parcel);
    }

    public String getMcc() {
        return this.mMcc;
    }

    public String getMnc() {
        return this.mMnc;
    }

    public String getSpn() {
        return this.mSpn;
    }

    public String getImsi() {
        return this.mImsi;
    }

    public String getGid1() {
        return this.mGid1;
    }

    public String getGid2() {
        return this.mGid2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mMcc);
        out.writeString(this.mMnc);
        out.writeString(this.mSpn);
        out.writeString(this.mImsi);
        out.writeString(this.mGid1);
        out.writeString(this.mGid2);
    }

    public String toString() {
        return "CarrierIdentifier{mcc=" + this.mMcc + ",mnc=" + this.mMnc + ",spn=" + this.mSpn + ",imsi=" + this.mImsi + ",gid1=" + this.mGid1 + ",gid2=" + this.mGid2 + "}";
    }

    public void readFromParcel(Parcel in) {
        this.mMcc = in.readString();
        this.mMnc = in.readString();
        this.mSpn = in.readString();
        this.mImsi = in.readString();
        this.mGid1 = in.readString();
        this.mGid2 = in.readString();
    }
}
