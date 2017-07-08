package android.net.wifi.nan;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SubscribeSettings implements Parcelable {
    public static final Creator<SubscribeSettings> CREATOR = null;
    public static final int SUBSCRIBE_TYPE_ACTIVE = 1;
    public static final int SUBSCRIBE_TYPE_PASSIVE = 0;
    public final int mSubscribeCount;
    public final int mSubscribeType;
    public final int mTtlSec;

    public static final class Builder {
        int mSubscribeCount;
        int mSubscribeType;
        int mTtlSec;

        public Builder setSubscribeType(int subscribeType) {
            if (subscribeType < 0 || subscribeType > SubscribeSettings.SUBSCRIBE_TYPE_ACTIVE) {
                throw new IllegalArgumentException("Invalid subscribeType - " + subscribeType);
            }
            this.mSubscribeType = subscribeType;
            return this;
        }

        public Builder setSubscribeCount(int subscribeCount) {
            if (subscribeCount < 0) {
                throw new IllegalArgumentException("Invalid subscribeCount - must be non-negative");
            }
            this.mSubscribeCount = subscribeCount;
            return this;
        }

        public Builder setTtlSec(int ttlSec) {
            if (ttlSec < 0) {
                throw new IllegalArgumentException("Invalid ttlSec - must be non-negative");
            }
            this.mTtlSec = ttlSec;
            return this;
        }

        public SubscribeSettings build() {
            return new SubscribeSettings(this.mSubscribeCount, this.mTtlSec, null);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.nan.SubscribeSettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.nan.SubscribeSettings.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.SubscribeSettings.<clinit>():void");
    }

    private SubscribeSettings(int subscribeType, int publichCount, int ttlSec) {
        this.mSubscribeType = subscribeType;
        this.mSubscribeCount = publichCount;
        this.mTtlSec = ttlSec;
    }

    public String toString() {
        return "SubscribeSettings [mSubscribeType=" + this.mSubscribeType + ", mSubscribeCount=" + this.mSubscribeCount + ", mTtlSec=" + this.mTtlSec + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSubscribeType);
        dest.writeInt(this.mSubscribeCount);
        dest.writeInt(this.mTtlSec);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubscribeSettings)) {
            return false;
        }
        SubscribeSettings lhs = (SubscribeSettings) o;
        if (this.mSubscribeType != lhs.mSubscribeType || this.mSubscribeCount != lhs.mSubscribeCount) {
            z = false;
        } else if (this.mTtlSec != lhs.mTtlSec) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((((this.mSubscribeType + 527) * 31) + this.mSubscribeCount) * 31) + this.mTtlSec;
    }
}
