package android.telephony;

import android.net.LinkProperties;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PreciseDataConnectionState implements Parcelable {
    public static final Creator<PreciseDataConnectionState> CREATOR = null;
    private String mAPN;
    private String mAPNType;
    private String mFailCause;
    private LinkProperties mLinkProperties;
    private int mNetworkType;
    private String mReason;
    private int mState;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.PreciseDataConnectionState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.PreciseDataConnectionState.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.PreciseDataConnectionState.<clinit>():void");
    }

    public PreciseDataConnectionState(int state, int networkType, String apnType, String apn, String reason, LinkProperties linkProperties, String failCause) {
        this.mState = -1;
        this.mNetworkType = 0;
        this.mAPNType = "";
        this.mAPN = "";
        this.mReason = "";
        this.mLinkProperties = null;
        this.mFailCause = "";
        this.mState = state;
        this.mNetworkType = networkType;
        this.mAPNType = apnType;
        this.mAPN = apn;
        this.mReason = reason;
        this.mLinkProperties = linkProperties;
        this.mFailCause = failCause;
    }

    public PreciseDataConnectionState() {
        this.mState = -1;
        this.mNetworkType = 0;
        this.mAPNType = "";
        this.mAPN = "";
        this.mReason = "";
        this.mLinkProperties = null;
        this.mFailCause = "";
    }

    private PreciseDataConnectionState(Parcel in) {
        this.mState = -1;
        this.mNetworkType = 0;
        this.mAPNType = "";
        this.mAPN = "";
        this.mReason = "";
        this.mLinkProperties = null;
        this.mFailCause = "";
        this.mState = in.readInt();
        this.mNetworkType = in.readInt();
        this.mAPNType = in.readString();
        this.mAPN = in.readString();
        this.mReason = in.readString();
        this.mLinkProperties = (LinkProperties) in.readParcelable(null);
        this.mFailCause = in.readString();
    }

    public int getDataConnectionState() {
        return this.mState;
    }

    public int getDataConnectionNetworkType() {
        return this.mNetworkType;
    }

    public String getDataConnectionAPNType() {
        return this.mAPNType;
    }

    public String getDataConnectionAPN() {
        return this.mAPN;
    }

    public String getDataConnectionChangeReason() {
        return this.mReason;
    }

    public LinkProperties getDataConnectionLinkProperties() {
        return this.mLinkProperties;
    }

    public String getDataConnectionFailCause() {
        return this.mFailCause;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mState);
        out.writeInt(this.mNetworkType);
        out.writeString(this.mAPNType);
        out.writeString(this.mAPN);
        out.writeString(this.mReason);
        out.writeParcelable(this.mLinkProperties, flags);
        out.writeString(this.mFailCause);
    }

    public int hashCode() {
        int i = 0;
        int hashCode = (((((((((((this.mState + 31) * 31) + this.mNetworkType) * 31) + (this.mAPNType == null ? 0 : this.mAPNType.hashCode())) * 31) + (this.mAPN == null ? 0 : this.mAPN.hashCode())) * 31) + (this.mReason == null ? 0 : this.mReason.hashCode())) * 31) + (this.mLinkProperties == null ? 0 : this.mLinkProperties.hashCode())) * 31;
        if (this.mFailCause != null) {
            i = this.mFailCause.hashCode();
        }
        return hashCode + i;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PreciseDataConnectionState other = (PreciseDataConnectionState) obj;
        if (this.mAPN == null) {
            if (other.mAPN != null) {
                return false;
            }
        } else if (!this.mAPN.equals(other.mAPN)) {
            return false;
        }
        if (this.mAPNType == null) {
            if (other.mAPNType != null) {
                return false;
            }
        } else if (!this.mAPNType.equals(other.mAPNType)) {
            return false;
        }
        if (this.mFailCause == null) {
            if (other.mFailCause != null) {
                return false;
            }
        } else if (!this.mFailCause.equals(other.mFailCause)) {
            return false;
        }
        if (this.mLinkProperties == null) {
            if (other.mLinkProperties != null) {
                return false;
            }
        } else if (!this.mLinkProperties.equals(other.mLinkProperties)) {
            return false;
        }
        if (this.mNetworkType != other.mNetworkType) {
            return false;
        }
        if (this.mReason == null) {
            if (other.mReason != null) {
                return false;
            }
        } else if (!this.mReason.equals(other.mReason)) {
            return false;
        }
        return this.mState == other.mState;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Data Connection state: ").append(this.mState);
        sb.append(", Network type: ").append(this.mNetworkType);
        sb.append(", APN type: ").append(this.mAPNType);
        sb.append(", APN: ").append(this.mAPN);
        sb.append(", Change reason: ").append(this.mReason);
        sb.append(", Link properties: ").append(this.mLinkProperties);
        sb.append(", Fail cause: ").append(this.mFailCause);
        return sb.toString();
    }
}
