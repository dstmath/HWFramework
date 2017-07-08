package android.net.wifi.nan;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PublishSettings implements Parcelable {
    public static final Creator<PublishSettings> CREATOR = null;
    public static final int PUBLISH_TYPE_SOLICITED = 1;
    public static final int PUBLISH_TYPE_UNSOLICITED = 0;
    public final int mPublishCount;
    public final int mPublishType;
    public final int mTtlSec;

    public static final class Builder {
        int mPublishCount;
        int mPublishType;
        int mTtlSec;

        public Builder setPublishType(int publishType) {
            if (publishType < 0 || publishType > PublishSettings.PUBLISH_TYPE_SOLICITED) {
                throw new IllegalArgumentException("Invalid publishType - " + publishType);
            }
            this.mPublishType = publishType;
            return this;
        }

        public Builder setPublishCount(int publishCount) {
            if (publishCount < 0) {
                throw new IllegalArgumentException("Invalid publishCount - must be non-negative");
            }
            this.mPublishCount = publishCount;
            return this;
        }

        public Builder setTtlSec(int ttlSec) {
            if (ttlSec < 0) {
                throw new IllegalArgumentException("Invalid ttlSec - must be non-negative");
            }
            this.mTtlSec = ttlSec;
            return this;
        }

        public PublishSettings build() {
            return new PublishSettings(this.mPublishCount, this.mTtlSec, null);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.nan.PublishSettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.nan.PublishSettings.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.PublishSettings.<clinit>():void");
    }

    private PublishSettings(int publishType, int publichCount, int ttlSec) {
        this.mPublishType = publishType;
        this.mPublishCount = publichCount;
        this.mTtlSec = ttlSec;
    }

    public String toString() {
        return "PublishSettings [mPublishType=" + this.mPublishType + ", mPublishCount=" + this.mPublishCount + ", mTtlSec=" + this.mTtlSec + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPublishType);
        dest.writeInt(this.mPublishCount);
        dest.writeInt(this.mTtlSec);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof PublishSettings)) {
            return false;
        }
        PublishSettings lhs = (PublishSettings) o;
        if (this.mPublishType != lhs.mPublishType || this.mPublishCount != lhs.mPublishCount) {
            z = false;
        } else if (this.mTtlSec != lhs.mTtlSec) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((((this.mPublishType + 527) * 31) + this.mPublishCount) * 31) + this.mTtlSec;
    }
}
