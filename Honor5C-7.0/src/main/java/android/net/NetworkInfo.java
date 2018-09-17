package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.EnumMap;

public class NetworkInfo implements Parcelable {
    public static final Creator<NetworkInfo> CREATOR = null;
    private static final EnumMap<DetailedState, State> stateMap = null;
    private DetailedState mDetailedState;
    private String mExtraInfo;
    private boolean mIsAvailable;
    private boolean mIsFailover;
    private boolean mIsMetered;
    private boolean mIsRoaming;
    private int mNetworkType;
    private String mReason;
    private State mState;
    private int mSubtype;
    private String mSubtypeName;
    private String mTypeName;

    public enum DetailedState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkInfo.DetailedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.NetworkInfo.DetailedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkInfo.DetailedState.<clinit>():void");
        }
    }

    public enum State {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkInfo.State.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.NetworkInfo.State.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkInfo.State.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.NetworkInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkInfo.<clinit>():void");
    }

    public NetworkInfo(int type, int subtype, String typeName, String subtypeName) {
        if (ConnectivityManager.isNetworkTypeValid(type)) {
            this.mNetworkType = type;
            this.mSubtype = subtype;
            this.mTypeName = typeName;
            this.mSubtypeName = subtypeName;
            setDetailedState(DetailedState.IDLE, null, null);
            this.mState = State.UNKNOWN;
            return;
        }
        throw new IllegalArgumentException("Invalid network type: " + type);
    }

    public NetworkInfo(NetworkInfo source) {
        if (source != null) {
            synchronized (source) {
                this.mNetworkType = source.mNetworkType;
                this.mSubtype = source.mSubtype;
                this.mTypeName = source.mTypeName;
                this.mSubtypeName = source.mSubtypeName;
                this.mState = source.mState;
                this.mDetailedState = source.mDetailedState;
                this.mReason = source.mReason;
                this.mExtraInfo = source.mExtraInfo;
                this.mIsFailover = source.mIsFailover;
                this.mIsAvailable = source.mIsAvailable;
                this.mIsRoaming = source.mIsRoaming;
                this.mIsMetered = source.mIsMetered;
            }
        }
    }

    public int getType() {
        int i;
        synchronized (this) {
            i = this.mNetworkType;
        }
        return i;
    }

    public void setType(int type) {
        synchronized (this) {
            this.mNetworkType = type;
        }
    }

    public int getSubtype() {
        int i;
        synchronized (this) {
            i = this.mSubtype;
        }
        return i;
    }

    public void setSubtype(int subtype, String subtypeName) {
        synchronized (this) {
            this.mSubtype = subtype;
            this.mSubtypeName = subtypeName;
        }
    }

    public String getTypeName() {
        String str;
        synchronized (this) {
            str = this.mTypeName;
        }
        return str;
    }

    public String getSubtypeName() {
        String str;
        synchronized (this) {
            str = this.mSubtypeName;
        }
        return str;
    }

    public boolean isConnectedOrConnecting() {
        boolean z = true;
        synchronized (this) {
            if (!(this.mState == State.CONNECTED || this.mState == State.CONNECTING)) {
                z = false;
            }
        }
        return z;
    }

    public boolean isConnected() {
        boolean z;
        synchronized (this) {
            z = this.mState == State.CONNECTED;
        }
        return z;
    }

    public boolean isAvailable() {
        boolean z;
        synchronized (this) {
            z = this.mIsAvailable;
        }
        return z;
    }

    public void setIsAvailable(boolean isAvailable) {
        synchronized (this) {
            this.mIsAvailable = isAvailable;
        }
    }

    public boolean isFailover() {
        boolean z;
        synchronized (this) {
            z = this.mIsFailover;
        }
        return z;
    }

    public void setFailover(boolean isFailover) {
        synchronized (this) {
            this.mIsFailover = isFailover;
        }
    }

    public boolean isRoaming() {
        boolean z;
        synchronized (this) {
            z = this.mIsRoaming;
        }
        return z;
    }

    public void setRoaming(boolean isRoaming) {
        synchronized (this) {
            this.mIsRoaming = isRoaming;
        }
    }

    public boolean isMetered() {
        boolean z;
        synchronized (this) {
            z = this.mIsMetered;
        }
        return z;
    }

    public void setMetered(boolean isMetered) {
        synchronized (this) {
            this.mIsMetered = isMetered;
        }
    }

    public State getState() {
        State state;
        synchronized (this) {
            state = this.mState;
        }
        return state;
    }

    public DetailedState getDetailedState() {
        DetailedState detailedState;
        synchronized (this) {
            detailedState = this.mDetailedState;
        }
        return detailedState;
    }

    public void setDetailedState(DetailedState detailedState, String reason, String extraInfo) {
        synchronized (this) {
            this.mDetailedState = detailedState;
            this.mState = (State) stateMap.get(detailedState);
            this.mReason = reason;
            this.mExtraInfo = extraInfo;
        }
    }

    public void setExtraInfo(String extraInfo) {
        synchronized (this) {
            this.mExtraInfo = extraInfo;
        }
    }

    public String getReason() {
        String str;
        synchronized (this) {
            str = this.mReason;
        }
        return str;
    }

    public String getExtraInfo() {
        String str;
        synchronized (this) {
            str = this.mExtraInfo;
        }
        return str;
    }

    public String toString() {
        String stringBuilder;
        synchronized (this) {
            StringBuilder builder = new StringBuilder("[");
            builder.append("type: ").append(getTypeName()).append("[").append(getSubtypeName()).append("], state: ").append(this.mState).append("/").append(this.mDetailedState).append(", reason: ").append(this.mReason == null ? "(unspecified)" : this.mReason).append(", extra: ").append(this.mExtraInfo == null ? "(none)" : this.mExtraInfo).append(", failover: ").append(this.mIsFailover).append(", available: ").append(this.mIsAvailable).append(", roaming: ").append(this.mIsRoaming).append(", metered: ").append(this.mIsMetered).append("]");
            stringBuilder = builder.toString();
        }
        return stringBuilder;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        synchronized (this) {
            int i2;
            dest.writeInt(this.mNetworkType);
            dest.writeInt(this.mSubtype);
            dest.writeString(this.mTypeName);
            dest.writeString(this.mSubtypeName);
            dest.writeString(this.mState.name());
            dest.writeString(this.mDetailedState.name());
            if (this.mIsFailover) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            dest.writeInt(i2);
            if (this.mIsAvailable) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            dest.writeInt(i2);
            if (this.mIsRoaming) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            dest.writeInt(i2);
            if (!this.mIsMetered) {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeString(this.mReason);
            dest.writeString(this.mExtraInfo);
        }
    }
}
