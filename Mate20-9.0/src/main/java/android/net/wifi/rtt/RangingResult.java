package android.net.wifi.rtt;

import android.annotation.SystemApi;
import android.net.MacAddress;
import android.net.wifi.aware.PeerHandle;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Objects;

public final class RangingResult implements Parcelable {
    public static final Parcelable.Creator<RangingResult> CREATOR = new Parcelable.Creator<RangingResult>() {
        public RangingResult[] newArray(int size) {
            return new RangingResult[size];
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v7, resolved type: android.net.MacAddress} */
        /* JADX WARNING: Multi-variable type inference failed */
        public RangingResult createFromParcel(Parcel in) {
            int status = in.readInt();
            MacAddress mac = null;
            if (in.readBoolean()) {
                mac = MacAddress.CREATOR.createFromParcel(in);
            } else {
                Parcel parcel = in;
            }
            MacAddress mac2 = mac;
            boolean peerHandlePresent = in.readBoolean();
            PeerHandle peerHandle = null;
            if (peerHandlePresent) {
                peerHandle = new PeerHandle(in.readInt());
            }
            PeerHandle peerHandle2 = peerHandle;
            int distanceMm = in.readInt();
            int distanceStdDevMm = in.readInt();
            int rssi = in.readInt();
            int numAttemptedMeasurements = in.readInt();
            int numSuccessfulMeasurements = in.readInt();
            byte[] lci = in.createByteArray();
            byte[] lcr = in.createByteArray();
            long timestamp = in.readLong();
            if (peerHandlePresent) {
                RangingResult rangingResult = new RangingResult(status, peerHandle2, distanceMm, distanceStdDevMm, rssi, numAttemptedMeasurements, numSuccessfulMeasurements, lci, lcr, timestamp);
                return rangingResult;
            }
            RangingResult rangingResult2 = new RangingResult(status, mac2, distanceMm, distanceStdDevMm, rssi, numAttemptedMeasurements, numSuccessfulMeasurements, lci, lcr, timestamp);
            return rangingResult2;
        }
    };
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final int STATUS_FAIL = 1;
    public static final int STATUS_RESPONDER_DOES_NOT_SUPPORT_IEEE80211MC = 2;
    public static final int STATUS_SUCCESS = 0;
    private static final String TAG = "RangingResult";
    private final int mDistanceMm;
    private final int mDistanceStdDevMm;
    private final byte[] mLci;
    private final byte[] mLcr;
    private final MacAddress mMac;
    private final int mNumAttemptedMeasurements;
    private final int mNumSuccessfulMeasurements;
    private final PeerHandle mPeerHandle;
    private final int mRssi;
    private final int mStatus;
    private final long mTimestamp;

    @Retention(RetentionPolicy.SOURCE)
    public @interface RangeResultStatus {
    }

    public RangingResult(int status, MacAddress mac, int distanceMm, int distanceStdDevMm, int rssi, int numAttemptedMeasurements, int numSuccessfulMeasurements, byte[] lci, byte[] lcr, long timestamp) {
        this.mStatus = status;
        this.mMac = mac;
        this.mPeerHandle = null;
        this.mDistanceMm = distanceMm;
        this.mDistanceStdDevMm = distanceStdDevMm;
        this.mRssi = rssi;
        this.mNumAttemptedMeasurements = numAttemptedMeasurements;
        this.mNumSuccessfulMeasurements = numSuccessfulMeasurements;
        this.mLci = lci == null ? EMPTY_BYTE_ARRAY : lci;
        this.mLcr = lcr == null ? EMPTY_BYTE_ARRAY : lcr;
        this.mTimestamp = timestamp;
    }

    public RangingResult(int status, PeerHandle peerHandle, int distanceMm, int distanceStdDevMm, int rssi, int numAttemptedMeasurements, int numSuccessfulMeasurements, byte[] lci, byte[] lcr, long timestamp) {
        this.mStatus = status;
        this.mMac = null;
        this.mPeerHandle = peerHandle;
        this.mDistanceMm = distanceMm;
        this.mDistanceStdDevMm = distanceStdDevMm;
        this.mRssi = rssi;
        this.mNumAttemptedMeasurements = numAttemptedMeasurements;
        this.mNumSuccessfulMeasurements = numSuccessfulMeasurements;
        this.mLci = lci == null ? EMPTY_BYTE_ARRAY : lci;
        this.mLcr = lcr == null ? EMPTY_BYTE_ARRAY : lcr;
        this.mTimestamp = timestamp;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public MacAddress getMacAddress() {
        return this.mMac;
    }

    public PeerHandle getPeerHandle() {
        return this.mPeerHandle;
    }

    public int getDistanceMm() {
        if (this.mStatus == 0) {
            return this.mDistanceMm;
        }
        throw new IllegalStateException("getDistanceMm(): invoked on an invalid result: getStatus()=" + this.mStatus);
    }

    public int getDistanceStdDevMm() {
        if (this.mStatus == 0) {
            return this.mDistanceStdDevMm;
        }
        throw new IllegalStateException("getDistanceStdDevMm(): invoked on an invalid result: getStatus()=" + this.mStatus);
    }

    public int getRssi() {
        if (this.mStatus == 0) {
            return this.mRssi;
        }
        throw new IllegalStateException("getRssi(): invoked on an invalid result: getStatus()=" + this.mStatus);
    }

    public int getNumAttemptedMeasurements() {
        if (this.mStatus == 0) {
            return this.mNumAttemptedMeasurements;
        }
        throw new IllegalStateException("getNumAttemptedMeasurements(): invoked on an invalid result: getStatus()=" + this.mStatus);
    }

    public int getNumSuccessfulMeasurements() {
        if (this.mStatus == 0) {
            return this.mNumSuccessfulMeasurements;
        }
        throw new IllegalStateException("getNumSuccessfulMeasurements(): invoked on an invalid result: getStatus()=" + this.mStatus);
    }

    @SystemApi
    public byte[] getLci() {
        if (this.mStatus == 0) {
            return this.mLci;
        }
        throw new IllegalStateException("getLci(): invoked on an invalid result: getStatus()=" + this.mStatus);
    }

    @SystemApi
    public byte[] getLcr() {
        if (this.mStatus == 0) {
            return this.mLcr;
        }
        throw new IllegalStateException("getReportedLocationCivic(): invoked on an invalid result: getStatus()=" + this.mStatus);
    }

    public long getRangingTimestampMillis() {
        if (this.mStatus == 0) {
            return this.mTimestamp;
        }
        throw new IllegalStateException("getRangingTimestampMillis(): invoked on an invalid result: getStatus()=" + this.mStatus);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mStatus);
        if (this.mMac == null) {
            dest.writeBoolean(false);
        } else {
            dest.writeBoolean(true);
            this.mMac.writeToParcel(dest, flags);
        }
        if (this.mPeerHandle == null) {
            dest.writeBoolean(false);
        } else {
            dest.writeBoolean(true);
            dest.writeInt(this.mPeerHandle.peerId);
        }
        dest.writeInt(this.mDistanceMm);
        dest.writeInt(this.mDistanceStdDevMm);
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mNumAttemptedMeasurements);
        dest.writeInt(this.mNumSuccessfulMeasurements);
        dest.writeByteArray(this.mLci);
        dest.writeByteArray(this.mLcr);
        dest.writeLong(this.mTimestamp);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RangingResult: [status=");
        sb.append(this.mStatus);
        sb.append(", mac=");
        sb.append(this.mMac);
        sb.append(", peerHandle=");
        sb.append(this.mPeerHandle == null ? "<null>" : Integer.valueOf(this.mPeerHandle.peerId));
        sb.append(", distanceMm=");
        sb.append(this.mDistanceMm);
        sb.append(", distanceStdDevMm=");
        sb.append(this.mDistanceStdDevMm);
        sb.append(", rssi=");
        sb.append(this.mRssi);
        sb.append(", numAttemptedMeasurements=");
        sb.append(this.mNumAttemptedMeasurements);
        sb.append(", numSuccessfulMeasurements=");
        sb.append(this.mNumSuccessfulMeasurements);
        sb.append(", lci=");
        sb.append(this.mLci);
        sb.append(", lcr=");
        sb.append(this.mLcr);
        sb.append(", timestamp=");
        sb.append(this.mTimestamp);
        sb.append("]");
        return sb.toString();
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof RangingResult)) {
            return false;
        }
        RangingResult lhs = (RangingResult) o;
        if (!(this.mStatus == lhs.mStatus && Objects.equals(this.mMac, lhs.mMac) && Objects.equals(this.mPeerHandle, lhs.mPeerHandle) && this.mDistanceMm == lhs.mDistanceMm && this.mDistanceStdDevMm == lhs.mDistanceStdDevMm && this.mRssi == lhs.mRssi && this.mNumAttemptedMeasurements == lhs.mNumAttemptedMeasurements && this.mNumSuccessfulMeasurements == lhs.mNumSuccessfulMeasurements && Arrays.equals(this.mLci, lhs.mLci) && Arrays.equals(this.mLcr, lhs.mLcr) && this.mTimestamp == lhs.mTimestamp)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mStatus), this.mMac, this.mPeerHandle, Integer.valueOf(this.mDistanceMm), Integer.valueOf(this.mDistanceStdDevMm), Integer.valueOf(this.mRssi), Integer.valueOf(this.mNumAttemptedMeasurements), Integer.valueOf(this.mNumSuccessfulMeasurements), this.mLci, this.mLcr, Long.valueOf(this.mTimestamp)});
    }
}
