package android.media;

import android.os.Parcel;

public final class SubtitleData {
    private static final String TAG = "SubtitleData";
    private byte[] mData;
    private long mDurationUs;
    private long mStartTimeUs;
    private int mTrackIndex;

    public SubtitleData(Parcel parcel) {
        if (!parseParcel(parcel)) {
            throw new IllegalArgumentException("parseParcel() fails");
        }
    }

    public SubtitleData(int trackIndex, long startTimeUs, long durationUs, byte[] data) {
        if (data != null) {
            this.mTrackIndex = trackIndex;
            this.mStartTimeUs = startTimeUs;
            this.mDurationUs = durationUs;
            this.mData = data;
            return;
        }
        throw new IllegalArgumentException("null data is not allowed");
    }

    public int getTrackIndex() {
        return this.mTrackIndex;
    }

    public long getStartTimeUs() {
        return this.mStartTimeUs;
    }

    public long getDurationUs() {
        return this.mDurationUs;
    }

    public byte[] getData() {
        return this.mData;
    }

    private boolean parseParcel(Parcel parcel) {
        parcel.setDataPosition(0);
        if (parcel.dataAvail() == 0) {
            return false;
        }
        this.mTrackIndex = parcel.readInt();
        this.mStartTimeUs = parcel.readLong();
        this.mDurationUs = parcel.readLong();
        this.mData = new byte[parcel.readInt()];
        parcel.readByteArray(this.mData);
        return true;
    }
}
