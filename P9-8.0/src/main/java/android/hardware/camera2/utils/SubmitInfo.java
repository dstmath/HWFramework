package android.hardware.camera2.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SubmitInfo implements Parcelable {
    public static final Creator<SubmitInfo> CREATOR = new Creator<SubmitInfo>() {
        public SubmitInfo createFromParcel(Parcel in) {
            return new SubmitInfo(in, null);
        }

        public SubmitInfo[] newArray(int size) {
            return new SubmitInfo[size];
        }
    };
    private long mLastFrameNumber;
    private int mRequestId;

    public SubmitInfo() {
        this.mRequestId = -1;
        this.mLastFrameNumber = -1;
    }

    public SubmitInfo(int requestId, long lastFrameNumber) {
        this.mRequestId = requestId;
        this.mLastFrameNumber = lastFrameNumber;
    }

    private SubmitInfo(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRequestId);
        dest.writeLong(this.mLastFrameNumber);
    }

    public void readFromParcel(Parcel in) {
        this.mRequestId = in.readInt();
        this.mLastFrameNumber = in.readLong();
    }

    public int getRequestId() {
        return this.mRequestId;
    }

    public long getLastFrameNumber() {
        return this.mLastFrameNumber;
    }
}
