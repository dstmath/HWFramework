package huawei.android.aod;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public final class AodConfigInfo implements Parcelable {
    public static final Parcelable.Creator<AodConfigInfo> CREATOR = new Parcelable.Creator<AodConfigInfo>() {
        /* class huawei.android.aod.AodConfigInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AodConfigInfo createFromParcel(Parcel in) {
            return new AodConfigInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public AodConfigInfo[] newArray(int size) {
            return new AodConfigInfo[size];
        }
    };
    private static final String TAG = "AodConfigInfo";
    public int mAODWorkMode;
    public Rect[] mAodItemRect;
    public int mBufferHeight;
    public int mBufferWidth;
    public String mChargeTips;
    public int mClockTextArrayAreaHeight;
    public int mClockTextWidth;
    public int mCurrentColorFlag;
    public long mCurrentTime;
    public int mCurrentVolume;
    public int mDisplayMode;
    public int mDualClock;
    public int mFingerprintCount;
    public int mFingerprintMode;
    public int mForceUpdate;
    public int mHomeColorFlag;
    public int mIntelliSwitch;
    public int mIsTimeRefresh;
    public int mMaxVolume;
    public int mMinVolume;
    public int mNotificationOffset;
    public int mProtectVolume;
    public int mScreenMode;
    public int mSecondTimeZone;
    public String mStatusString;
    public int mTimeFormat;
    public int mTimeZone;
    private List<Trigger> mTriggers = new ArrayList();
    public int mVolumeType;

    public AodConfigInfo() {
    }

    public AodConfigInfo(Parcel in) {
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
        this.mDisplayMode = in.readInt();
        this.mCurrentColorFlag = in.readInt();
        this.mHomeColorFlag = in.readInt();
        this.mVolumeType = in.readInt();
        this.mMaxVolume = in.readInt();
        this.mMinVolume = in.readInt();
        this.mCurrentVolume = in.readInt();
        this.mProtectVolume = in.readInt();
        Parcelable[] parcelables = in.readParcelableArray(null);
        if (parcelables != null) {
            this.mAodItemRect = new Rect[parcelables.length];
            for (int idx = 0; idx < parcelables.length; idx++) {
                if (parcelables[idx] instanceof Rect) {
                    this.mAodItemRect[idx] = (Rect) parcelables[idx];
                }
            }
        }
        this.mIsTimeRefresh = in.readInt();
        this.mNotificationOffset = in.readInt();
        this.mTriggers = in.readArrayList(null);
        this.mChargeTips = in.readString();
        this.mScreenMode = in.readInt();
    }

    public List<Trigger> getTriggers() {
        List<Trigger> list = this.mTriggers;
        return list != null ? list : new ArrayList(0);
    }

    public void setTriggers(List<Trigger> triggers) {
        this.mTriggers = triggers;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flag) {
        if (dest != null) {
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
            dest.writeInt(this.mDisplayMode);
            dest.writeInt(this.mCurrentColorFlag);
            dest.writeInt(this.mHomeColorFlag);
            dest.writeInt(this.mVolumeType);
            dest.writeInt(this.mMaxVolume);
            dest.writeInt(this.mMinVolume);
            dest.writeInt(this.mCurrentVolume);
            dest.writeInt(this.mProtectVolume);
            dest.writeParcelableArray(this.mAodItemRect, flag);
            dest.writeInt(this.mIsTimeRefresh);
            dest.writeInt(this.mNotificationOffset);
            dest.writeList(this.mTriggers);
            dest.writeString(this.mChargeTips);
            dest.writeInt(this.mScreenMode);
        }
    }

    public boolean isDualClock() {
        return this.mDualClock != 0;
    }
}
