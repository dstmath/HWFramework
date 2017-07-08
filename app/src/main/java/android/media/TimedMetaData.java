package android.media;

import android.os.Parcel;

public final class TimedMetaData {
    private static final String TAG = "TimedMetaData";
    private byte[] mMetaData;
    private long mTimestampUs;

    static TimedMetaData createTimedMetaDataFromParcel(Parcel parcel) {
        return new TimedMetaData(parcel);
    }

    private TimedMetaData(Parcel parcel) {
        if (!parseParcel(parcel)) {
            throw new IllegalArgumentException("parseParcel() fails");
        }
    }

    public long getTimestamp() {
        return this.mTimestampUs;
    }

    public byte[] getMetaData() {
        return this.mMetaData;
    }

    private boolean parseParcel(Parcel parcel) {
        parcel.setDataPosition(0);
        if (parcel.dataAvail() == 0) {
            return false;
        }
        this.mTimestampUs = parcel.readLong();
        this.mMetaData = new byte[parcel.readInt()];
        parcel.readByteArray(this.mMetaData);
        return true;
    }
}
