package huawei.android.aod;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public class AodConfigInfo implements Parcelable {
    public static final Parcelable.Creator<AodConfigInfo> CREATOR = new Parcelable.Creator<AodConfigInfo>() {
        public AodConfigInfo createFromParcel(Parcel in) {
            return new AodConfigInfo(in);
        }

        public AodConfigInfo[] newArray(int size) {
            return new AodConfigInfo[size];
        }
    };
    private static final String TAG = "AodConfigInfo";
    public int mAODWorkMode;
    public Rect[] mAodItemRect;
    public int mBufferHeight;
    public int mBufferWidth;
    public int mClockTextArrayAreaHeight;
    public int mClockTextWidth;
    public long mCurrentTime;
    public int mDualClock;
    public int mFingerprintCount;
    public int mFingerprintMode;
    public int mForceUpdate;
    public int mIntelliSwitch;
    public int mSecondTimeZone;
    public String mStatusString;
    public int mTimeFormat;
    public int mTimeZone;

    public AodConfigInfo() {
    }

    public AodConfigInfo(Parcel parcel) {
        readFromParcel(parcel);
    }

    public AodConfigInfo(AodConfigInfo another) {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flag) {
        dest.writeLong(this.mCurrentTime);
        dest.writeInt(this.mTimeZone);
        dest.writeInt(this.mSecondTimeZone);
        dest.writeInt(this.mTimeFormat);
        dest.writeInt(this.mDualClock);
        dest.writeInt(this.mBufferWidth);
        dest.writeInt(this.mBufferHeight);
        dest.writeInt(this.mIntelliSwitch);
        dest.writeInt(this.mAODWorkMode);
        dest.writeInt(this.mFingerprintCount);
        dest.writeInt(this.mFingerprintMode);
        dest.writeInt(this.mClockTextArrayAreaHeight);
        dest.writeInt(this.mClockTextWidth);
        dest.writeString(this.mStatusString);
        dest.writeInt(this.mForceUpdate);
        dest.writeParcelableArray(this.mAodItemRect, 0);
    }

    private void readFromParcel(Parcel in) {
        this.mCurrentTime = in.readLong();
        this.mTimeZone = in.readInt();
        this.mSecondTimeZone = in.readInt();
        this.mTimeFormat = in.readInt();
        this.mDualClock = in.readInt();
        this.mBufferWidth = in.readInt();
        this.mBufferHeight = in.readInt();
        this.mIntelliSwitch = in.readInt();
        this.mAODWorkMode = in.readInt();
        this.mFingerprintCount = in.readInt();
        this.mFingerprintMode = in.readInt();
        this.mClockTextArrayAreaHeight = in.readInt();
        this.mClockTextWidth = in.readInt();
        this.mStatusString = in.readString();
        this.mForceUpdate = in.readInt();
        Parcelable[] parcelables = in.readParcelableArray(null);
        if (parcelables != null) {
            this.mAodItemRect = new Rect[parcelables.length];
            for (int i = 0; i < parcelables.length; i++) {
                this.mAodItemRect[i] = (Rect) parcelables[i];
            }
        }
    }

    public boolean isDualClock() {
        return this.mDualClock != 0;
    }
}
