package android.net.wifi.nan;

import android.net.wifi.nan.TlvBufferUtils.TlvIterable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public class SubscribeData implements Parcelable {
    public static final Creator<SubscribeData> CREATOR = null;
    public final byte[] mRxFilter;
    public final int mRxFilterLength;
    public final String mServiceName;
    public final byte[] mServiceSpecificInfo;
    public final int mServiceSpecificInfoLength;
    public final byte[] mTxFilter;
    public final int mTxFilterLength;

    public static final class Builder {
        private byte[] mRxFilter;
        private int mRxFilterLength;
        private String mServiceName;
        private byte[] mServiceSpecificInfo;
        private int mServiceSpecificInfoLength;
        private byte[] mTxFilter;
        private int mTxFilterLength;

        public Builder() {
            this.mServiceSpecificInfo = new byte[0];
            this.mTxFilter = new byte[0];
            this.mRxFilter = new byte[0];
        }

        public Builder setServiceName(String serviceName) {
            this.mServiceName = serviceName;
            return this;
        }

        public Builder setServiceSpecificInfo(byte[] serviceSpecificInfo, int serviceSpecificInfoLength) {
            this.mServiceSpecificInfoLength = serviceSpecificInfoLength;
            this.mServiceSpecificInfo = serviceSpecificInfo;
            return this;
        }

        public Builder setServiceSpecificInfo(String serviceSpecificInfoStr) {
            this.mServiceSpecificInfoLength = serviceSpecificInfoStr.length();
            this.mServiceSpecificInfo = serviceSpecificInfoStr.getBytes();
            return this;
        }

        public Builder setTxFilter(byte[] txFilter, int txFilterLength) {
            this.mTxFilter = txFilter;
            this.mTxFilterLength = txFilterLength;
            return this;
        }

        public Builder setRxFilter(byte[] rxFilter, int rxFilterLength) {
            this.mRxFilter = rxFilter;
            this.mRxFilterLength = rxFilterLength;
            return this;
        }

        public SubscribeData build() {
            return new SubscribeData(this.mServiceSpecificInfo, this.mServiceSpecificInfoLength, this.mTxFilter, this.mTxFilterLength, this.mRxFilter, this.mRxFilterLength, null);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.nan.SubscribeData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.nan.SubscribeData.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.SubscribeData.<clinit>():void");
    }

    private SubscribeData(String serviceName, byte[] serviceSpecificInfo, int serviceSpecificInfoLength, byte[] txFilter, int txFilterLength, byte[] rxFilter, int rxFilterLength) {
        this.mServiceName = serviceName;
        this.mServiceSpecificInfoLength = serviceSpecificInfoLength;
        this.mServiceSpecificInfo = serviceSpecificInfo;
        this.mTxFilterLength = txFilterLength;
        this.mTxFilter = txFilter;
        this.mRxFilterLength = rxFilterLength;
        this.mRxFilter = rxFilter;
    }

    public String toString() {
        return "SubscribeData [mServiceName='" + this.mServiceName + "', mServiceSpecificInfo='" + new String(this.mServiceSpecificInfo, 0, this.mServiceSpecificInfoLength) + "', mTxFilter=" + new TlvIterable(0, 1, this.mTxFilter, this.mTxFilterLength).toString() + ", mRxFilter=" + new TlvIterable(0, 1, this.mRxFilter, this.mRxFilterLength).toString() + "']";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mServiceName);
        dest.writeInt(this.mServiceSpecificInfoLength);
        if (this.mServiceSpecificInfoLength != 0) {
            dest.writeByteArray(this.mServiceSpecificInfo, 0, this.mServiceSpecificInfoLength);
        }
        dest.writeInt(this.mTxFilterLength);
        if (this.mTxFilterLength != 0) {
            dest.writeByteArray(this.mTxFilter, 0, this.mTxFilterLength);
        }
        dest.writeInt(this.mRxFilterLength);
        if (this.mRxFilterLength != 0) {
            dest.writeByteArray(this.mRxFilter, 0, this.mRxFilterLength);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubscribeData)) {
            return false;
        }
        SubscribeData lhs = (SubscribeData) o;
        if (!this.mServiceName.equals(lhs.mServiceName) || this.mServiceSpecificInfoLength != lhs.mServiceSpecificInfoLength || this.mTxFilterLength != lhs.mTxFilterLength || this.mRxFilterLength != lhs.mRxFilterLength) {
            return false;
        }
        int i;
        if (this.mServiceSpecificInfo != null && lhs.mServiceSpecificInfo != null) {
            for (i = 0; i < this.mServiceSpecificInfoLength; i++) {
                if (this.mServiceSpecificInfo[i] != lhs.mServiceSpecificInfo[i]) {
                    return false;
                }
            }
        } else if (this.mServiceSpecificInfoLength != 0) {
            return false;
        }
        if (this.mTxFilter != null && lhs.mTxFilter != null) {
            for (i = 0; i < this.mTxFilterLength; i++) {
                if (this.mTxFilter[i] != lhs.mTxFilter[i]) {
                    return false;
                }
            }
        } else if (this.mTxFilterLength != 0) {
            return false;
        }
        if (this.mRxFilter != null && lhs.mRxFilter != null) {
            for (i = 0; i < this.mRxFilterLength; i++) {
                if (this.mRxFilter[i] != lhs.mRxFilter[i]) {
                    return false;
                }
            }
        } else if (this.mRxFilterLength != 0) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return ((((((((((((this.mServiceName.hashCode() + 527) * 31) + this.mServiceSpecificInfoLength) * 31) + Arrays.hashCode(this.mServiceSpecificInfo)) * 31) + this.mTxFilterLength) * 31) + Arrays.hashCode(this.mTxFilter)) * 31) + this.mRxFilterLength) * 31) + Arrays.hashCode(this.mRxFilter);
    }
}
