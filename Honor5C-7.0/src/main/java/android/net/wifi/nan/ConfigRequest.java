package android.net.wifi.nan;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;

public class ConfigRequest implements Parcelable {
    public static final int CLUSTER_ID_MAX = 65535;
    public static final int CLUSTER_ID_MIN = 0;
    public static final Creator<ConfigRequest> CREATOR = null;
    public final int mClusterHigh;
    public final int mClusterLow;
    public final int mMasterPreference;
    public final boolean mSupport5gBand;

    public static final class Builder {
        private int mClusterHigh;
        private int mClusterLow;
        private int mMasterPreference;
        private boolean mSupport5gBand;

        public Builder() {
            this.mSupport5gBand = false;
            this.mMasterPreference = 0;
            this.mClusterLow = 0;
            this.mClusterHigh = ConfigRequest.CLUSTER_ID_MAX;
        }

        public Builder setSupport5gBand(boolean support5gBand) {
            this.mSupport5gBand = support5gBand;
            return this;
        }

        public Builder setMasterPreference(int masterPreference) {
            if (masterPreference < 0) {
                throw new IllegalArgumentException("Master Preference specification must be non-negative");
            } else if (masterPreference == 1 || masterPreference == Process.PROC_TERM_MASK || masterPreference > Process.PROC_TERM_MASK) {
                throw new IllegalArgumentException("Master Preference specification must not exceed 255 or use 1 or 255 (reserved values)");
            } else {
                this.mMasterPreference = masterPreference;
                return this;
            }
        }

        public Builder setClusterLow(int clusterLow) {
            if (clusterLow < 0) {
                throw new IllegalArgumentException("Cluster specification must be non-negative");
            } else if (clusterLow > ConfigRequest.CLUSTER_ID_MAX) {
                throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
            } else {
                this.mClusterLow = clusterLow;
                return this;
            }
        }

        public Builder setClusterHigh(int clusterHigh) {
            if (clusterHigh < 0) {
                throw new IllegalArgumentException("Cluster specification must be non-negative");
            } else if (clusterHigh > ConfigRequest.CLUSTER_ID_MAX) {
                throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
            } else {
                this.mClusterHigh = clusterHigh;
                return this;
            }
        }

        public ConfigRequest build() {
            if (this.mClusterLow <= this.mClusterHigh) {
                return new ConfigRequest(this.mMasterPreference, this.mClusterLow, this.mClusterHigh, null);
            }
            throw new IllegalArgumentException("Invalid argument combination - must have Cluster Low <= Cluster High");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.nan.ConfigRequest.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.nan.ConfigRequest.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.ConfigRequest.<clinit>():void");
    }

    private ConfigRequest(boolean support5gBand, int masterPreference, int clusterLow, int clusterHigh) {
        this.mSupport5gBand = support5gBand;
        this.mMasterPreference = masterPreference;
        this.mClusterLow = clusterLow;
        this.mClusterHigh = clusterHigh;
    }

    public String toString() {
        return "ConfigRequest [mSupport5gBand=" + this.mSupport5gBand + ", mMasterPreference=" + this.mMasterPreference + ", mClusterLow=" + this.mClusterLow + ", mClusterHigh=" + this.mClusterHigh + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSupport5gBand ? 1 : 0);
        dest.writeInt(this.mMasterPreference);
        dest.writeInt(this.mClusterLow);
        dest.writeInt(this.mClusterHigh);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigRequest)) {
            return false;
        }
        ConfigRequest lhs = (ConfigRequest) o;
        if (this.mSupport5gBand != lhs.mSupport5gBand || this.mMasterPreference != lhs.mMasterPreference || this.mClusterLow != lhs.mClusterLow) {
            z = false;
        } else if (this.mClusterHigh != lhs.mClusterHigh) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (((((((this.mSupport5gBand ? 1 : 0) + 527) * 31) + this.mMasterPreference) * 31) + this.mClusterLow) * 31) + this.mClusterHigh;
    }
}
