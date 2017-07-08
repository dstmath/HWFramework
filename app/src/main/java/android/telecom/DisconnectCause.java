package android.telecom;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.android.internal.telephony.IccCardConstants;
import huawei.cust.HwCfgFilePolicy;
import java.util.Objects;

public final class DisconnectCause implements Parcelable {
    public static final int ANSWERED_ELSEWHERE = 11;
    public static final int BUSY = 7;
    public static final int CALL_PULLED = 12;
    public static final int CANCELED = 4;
    public static final int CONNECTION_MANAGER_NOT_SUPPORTED = 10;
    public static final Creator<DisconnectCause> CREATOR = null;
    public static final int ERROR = 1;
    public static final int LOCAL = 2;
    public static final int MISSED = 5;
    public static final int OTHER = 9;
    public static final int REJECTED = 6;
    public static final int REMOTE = 3;
    public static final int RESTRICTED = 8;
    public static final int UNKNOWN = 0;
    private int mDisconnectCode;
    private CharSequence mDisconnectDescription;
    private CharSequence mDisconnectLabel;
    private String mDisconnectReason;
    private int mToneToPlay;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.DisconnectCause.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telecom.DisconnectCause.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.DisconnectCause.<clinit>():void");
    }

    public DisconnectCause(int code) {
        this(code, null, null, null, -1);
    }

    public DisconnectCause(int code, String reason) {
        this(code, null, null, reason, -1);
    }

    public DisconnectCause(int code, CharSequence label, CharSequence description, String reason) {
        this(code, label, description, reason, -1);
    }

    public DisconnectCause(int code, CharSequence label, CharSequence description, String reason, int toneToPlay) {
        this.mDisconnectCode = code;
        this.mDisconnectLabel = label;
        this.mDisconnectDescription = description;
        this.mDisconnectReason = reason;
        this.mToneToPlay = toneToPlay;
    }

    public int getCode() {
        return this.mDisconnectCode;
    }

    public CharSequence getLabel() {
        return this.mDisconnectLabel;
    }

    public CharSequence getDescription() {
        return this.mDisconnectDescription;
    }

    public String getReason() {
        return this.mDisconnectReason;
    }

    public int getTone() {
        return this.mToneToPlay;
    }

    public void writeToParcel(Parcel destination, int flags) {
        destination.writeInt(this.mDisconnectCode);
        TextUtils.writeToParcel(this.mDisconnectLabel, destination, flags);
        TextUtils.writeToParcel(this.mDisconnectDescription, destination, flags);
        destination.writeString(this.mDisconnectReason);
        destination.writeInt(this.mToneToPlay);
    }

    public int describeContents() {
        return 0;
    }

    public int hashCode() {
        return (((Objects.hashCode(Integer.valueOf(this.mDisconnectCode)) + Objects.hashCode(this.mDisconnectLabel)) + Objects.hashCode(this.mDisconnectDescription)) + Objects.hashCode(this.mDisconnectReason)) + Objects.hashCode(Integer.valueOf(this.mToneToPlay));
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof DisconnectCause)) {
            return false;
        }
        DisconnectCause d = (DisconnectCause) o;
        if (Objects.equals(Integer.valueOf(this.mDisconnectCode), Integer.valueOf(d.getCode())) && Objects.equals(this.mDisconnectLabel, d.getLabel()) && Objects.equals(this.mDisconnectDescription, d.getDescription()) && Objects.equals(this.mDisconnectReason, d.getReason())) {
            z = Objects.equals(Integer.valueOf(this.mToneToPlay), Integer.valueOf(d.getTone()));
        }
        return z;
    }

    public String toString() {
        String code = "";
        switch (this.mDisconnectCode) {
            case HwCfgFilePolicy.GLOBAL /*0*/:
                code = IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
                break;
            case ERROR /*1*/:
                code = "ERROR";
                break;
            case LOCAL /*2*/:
                code = "LOCAL";
                break;
            case REMOTE /*3*/:
                code = "REMOTE";
                break;
            case CANCELED /*4*/:
                code = "CANCELED";
                break;
            case MISSED /*5*/:
                code = "MISSED";
                break;
            case REJECTED /*6*/:
                code = "REJECTED";
                break;
            case BUSY /*7*/:
                code = "BUSY";
                break;
            case RESTRICTED /*8*/:
                code = "RESTRICTED";
                break;
            case OTHER /*9*/:
                code = "OTHER";
                break;
            case CONNECTION_MANAGER_NOT_SUPPORTED /*10*/:
                code = "CONNECTION_MANAGER_NOT_SUPPORTED";
                break;
            default:
                code = "invalid code: " + this.mDisconnectCode;
                break;
        }
        String label = this.mDisconnectLabel == null ? "" : this.mDisconnectLabel.toString();
        return "DisconnectCause [ Code: (" + code + ")" + " Label: (" + label + ")" + " Description: (" + (this.mDisconnectDescription == null ? "" : this.mDisconnectDescription.toString()) + ")" + " Reason: (" + (this.mDisconnectReason == null ? "" : this.mDisconnectReason) + ")" + " Tone: (" + this.mToneToPlay + ") ]";
    }
}
