package android.net.wifi.aware;

import android.net.NetworkSpecifier;
import android.os.Parcel;
import android.os.Parcelable;
import android.security.keystore.KeyProperties;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import libcore.util.HexEncoding;

public class WifiAwareAgentNetworkSpecifier extends NetworkSpecifier implements Parcelable {
    public static final Parcelable.Creator<WifiAwareAgentNetworkSpecifier> CREATOR = new Parcelable.Creator<WifiAwareAgentNetworkSpecifier>() {
        public WifiAwareAgentNetworkSpecifier createFromParcel(Parcel in) {
            WifiAwareAgentNetworkSpecifier agentNs = new WifiAwareAgentNetworkSpecifier();
            for (Object obj : in.readArray(null)) {
                agentNs.mNetworkSpecifiers.add((ByteArrayWrapper) obj);
            }
            return agentNs;
        }

        public WifiAwareAgentNetworkSpecifier[] newArray(int size) {
            return new WifiAwareAgentNetworkSpecifier[size];
        }
    };
    private static final String TAG = "WifiAwareAgentNs";
    private static final boolean VDBG = false;
    private MessageDigest mDigester;
    /* access modifiers changed from: private */
    public Set<ByteArrayWrapper> mNetworkSpecifiers = new HashSet();

    private static class ByteArrayWrapper implements Parcelable {
        public static final Parcelable.Creator<ByteArrayWrapper> CREATOR = new Parcelable.Creator<ByteArrayWrapper>() {
            public ByteArrayWrapper createFromParcel(Parcel in) {
                return new ByteArrayWrapper(in.readBlob());
            }

            public ByteArrayWrapper[] newArray(int size) {
                return new ByteArrayWrapper[size];
            }
        };
        private byte[] mData;

        ByteArrayWrapper(byte[] data) {
            this.mData = data;
        }

        public int hashCode() {
            return Arrays.hashCode(this.mData);
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ByteArrayWrapper)) {
                return false;
            }
            return Arrays.equals(((ByteArrayWrapper) obj).mData, this.mData);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeBlob(this.mData);
        }

        public String toString() {
            return new String(HexEncoding.encode(this.mData));
        }
    }

    public WifiAwareAgentNetworkSpecifier() {
    }

    public WifiAwareAgentNetworkSpecifier(WifiAwareNetworkSpecifier ns) {
        initialize();
        this.mNetworkSpecifiers.add(convert(ns));
    }

    public WifiAwareAgentNetworkSpecifier(WifiAwareNetworkSpecifier[] nss) {
        initialize();
        for (WifiAwareNetworkSpecifier ns : nss) {
            this.mNetworkSpecifiers.add(convert(ns));
        }
    }

    public boolean isEmpty() {
        return this.mNetworkSpecifiers.isEmpty();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeArray(this.mNetworkSpecifiers.toArray());
    }

    public int hashCode() {
        return this.mNetworkSpecifiers.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof WifiAwareAgentNetworkSpecifier)) {
            return false;
        }
        return this.mNetworkSpecifiers.equals(((WifiAwareAgentNetworkSpecifier) obj).mNetworkSpecifiers);
    }

    public String toString() {
        StringJoiner sj = new StringJoiner(",");
        for (ByteArrayWrapper baw : this.mNetworkSpecifiers) {
            sj.add(baw.toString());
        }
        return sj.toString();
    }

    public boolean satisfiedBy(NetworkSpecifier other) {
        if (!(other instanceof WifiAwareAgentNetworkSpecifier)) {
            return false;
        }
        WifiAwareAgentNetworkSpecifier otherNs = (WifiAwareAgentNetworkSpecifier) other;
        for (ByteArrayWrapper baw : this.mNetworkSpecifiers) {
            if (!otherNs.mNetworkSpecifiers.contains(baw)) {
                return false;
            }
        }
        return true;
    }

    public boolean satisfiesAwareNetworkSpecifier(WifiAwareNetworkSpecifier ns) {
        return this.mNetworkSpecifiers.contains(convert(ns));
    }

    public void assertValidFromUid(int requestorUid) {
        throw new SecurityException("WifiAwareAgentNetworkSpecifier should not be used in network requests");
    }

    private void initialize() {
        try {
            this.mDigester = MessageDigest.getInstance(KeyProperties.DIGEST_SHA256);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Can not instantiate a SHA-256 digester!? Will match nothing.");
        }
    }

    private ByteArrayWrapper convert(WifiAwareNetworkSpecifier ns) {
        if (this.mDigester == null) {
            return null;
        }
        Parcel parcel = Parcel.obtain();
        ns.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        this.mDigester.reset();
        this.mDigester.update(bytes);
        return new ByteArrayWrapper(this.mDigester.digest());
    }
}
