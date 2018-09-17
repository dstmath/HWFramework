package com.android.ims.internal.uce.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CapInfo implements Parcelable {
    public static final Creator<CapInfo> CREATOR = new Creator<CapInfo>() {
        public CapInfo createFromParcel(Parcel source) {
            return new CapInfo(source, null);
        }

        public CapInfo[] newArray(int size) {
            return new CapInfo[size];
        }
    };
    private long mCapTimestamp;
    private boolean mCdViaPresenceSupported;
    private String[] mExts;
    private boolean mFtHttpSupported;
    private boolean mFtSnFSupported;
    private boolean mFtSupported;
    private boolean mFtThumbSupported;
    private boolean mFullSnFGroupChatSupported;
    private boolean mGeoPullFtSupported;
    private boolean mGeoPullSupported;
    private boolean mGeoPushSupported;
    private boolean mImSupported;
    private boolean mIpVideoSupported;
    private boolean mIpVoiceSupported;
    private boolean mIsSupported;
    private boolean mRcsIpVideoCallSupported;
    private boolean mRcsIpVideoOnlyCallSupported;
    private boolean mRcsIpVoiceCallSupported;
    private boolean mSmSupported;
    private boolean mSpSupported;
    private boolean mVsDuringCSSupported;
    private boolean mVsSupported;

    /* synthetic */ CapInfo(Parcel source, CapInfo -this1) {
        this(source);
    }

    public CapInfo() {
        this.mImSupported = false;
        this.mFtSupported = false;
        this.mFtThumbSupported = false;
        this.mFtSnFSupported = false;
        this.mFtHttpSupported = false;
        this.mIsSupported = false;
        this.mVsDuringCSSupported = false;
        this.mVsSupported = false;
        this.mSpSupported = false;
        this.mCdViaPresenceSupported = false;
        this.mIpVoiceSupported = false;
        this.mIpVideoSupported = false;
        this.mGeoPullFtSupported = false;
        this.mGeoPullSupported = false;
        this.mGeoPushSupported = false;
        this.mSmSupported = false;
        this.mFullSnFGroupChatSupported = false;
        this.mRcsIpVoiceCallSupported = false;
        this.mRcsIpVideoCallSupported = false;
        this.mRcsIpVideoOnlyCallSupported = false;
        this.mExts = new String[10];
        this.mCapTimestamp = 0;
    }

    public boolean isImSupported() {
        return this.mImSupported;
    }

    public void setImSupported(boolean imSupported) {
        this.mImSupported = imSupported;
    }

    public boolean isFtThumbSupported() {
        return this.mFtThumbSupported;
    }

    public void setFtThumbSupported(boolean ftThumbSupported) {
        this.mFtThumbSupported = ftThumbSupported;
    }

    public boolean isFtSnFSupported() {
        return this.mFtSnFSupported;
    }

    public void setFtSnFSupported(boolean ftSnFSupported) {
        this.mFtSnFSupported = ftSnFSupported;
    }

    public boolean isFtHttpSupported() {
        return this.mFtHttpSupported;
    }

    public void setFtHttpSupported(boolean ftHttpSupported) {
        this.mFtHttpSupported = ftHttpSupported;
    }

    public boolean isFtSupported() {
        return this.mFtSupported;
    }

    public void setFtSupported(boolean ftSupported) {
        this.mFtSupported = ftSupported;
    }

    public boolean isIsSupported() {
        return this.mIsSupported;
    }

    public void setIsSupported(boolean isSupported) {
        this.mIsSupported = isSupported;
    }

    public boolean isVsDuringCSSupported() {
        return this.mVsDuringCSSupported;
    }

    public void setVsDuringCSSupported(boolean vsDuringCSSupported) {
        this.mVsDuringCSSupported = vsDuringCSSupported;
    }

    public boolean isVsSupported() {
        return this.mVsSupported;
    }

    public void setVsSupported(boolean vsSupported) {
        this.mVsSupported = vsSupported;
    }

    public boolean isSpSupported() {
        return this.mSpSupported;
    }

    public void setSpSupported(boolean spSupported) {
        this.mSpSupported = spSupported;
    }

    public boolean isCdViaPresenceSupported() {
        return this.mCdViaPresenceSupported;
    }

    public void setCdViaPresenceSupported(boolean cdViaPresenceSupported) {
        this.mCdViaPresenceSupported = cdViaPresenceSupported;
    }

    public boolean isIpVoiceSupported() {
        return this.mIpVoiceSupported;
    }

    public void setIpVoiceSupported(boolean ipVoiceSupported) {
        this.mIpVoiceSupported = ipVoiceSupported;
    }

    public boolean isIpVideoSupported() {
        return this.mIpVideoSupported;
    }

    public void setIpVideoSupported(boolean ipVideoSupported) {
        this.mIpVideoSupported = ipVideoSupported;
    }

    public boolean isGeoPullFtSupported() {
        return this.mGeoPullFtSupported;
    }

    public void setGeoPullFtSupported(boolean geoPullFtSupported) {
        this.mGeoPullFtSupported = geoPullFtSupported;
    }

    public boolean isGeoPullSupported() {
        return this.mGeoPullSupported;
    }

    public void setGeoPullSupported(boolean geoPullSupported) {
        this.mGeoPullSupported = geoPullSupported;
    }

    public boolean isGeoPushSupported() {
        return this.mGeoPushSupported;
    }

    public void setGeoPushSupported(boolean geoPushSupported) {
        this.mGeoPushSupported = geoPushSupported;
    }

    public boolean isSmSupported() {
        return this.mSmSupported;
    }

    public void setSmSupported(boolean smSupported) {
        this.mSmSupported = smSupported;
    }

    public boolean isFullSnFGroupChatSupported() {
        return this.mFullSnFGroupChatSupported;
    }

    public boolean isRcsIpVoiceCallSupported() {
        return this.mRcsIpVoiceCallSupported;
    }

    public boolean isRcsIpVideoCallSupported() {
        return this.mRcsIpVideoCallSupported;
    }

    public boolean isRcsIpVideoOnlyCallSupported() {
        return this.mRcsIpVideoOnlyCallSupported;
    }

    public void setFullSnFGroupChatSupported(boolean fullSnFGroupChatSupported) {
        this.mFullSnFGroupChatSupported = fullSnFGroupChatSupported;
    }

    public void setRcsIpVoiceCallSupported(boolean rcsIpVoiceCallSupported) {
        this.mRcsIpVoiceCallSupported = rcsIpVoiceCallSupported;
    }

    public void setRcsIpVideoCallSupported(boolean rcsIpVideoCallSupported) {
        this.mRcsIpVideoCallSupported = rcsIpVideoCallSupported;
    }

    public void setRcsIpVideoOnlyCallSupported(boolean rcsIpVideoOnlyCallSupported) {
        this.mRcsIpVideoOnlyCallSupported = rcsIpVideoOnlyCallSupported;
    }

    public String[] getExts() {
        return this.mExts;
    }

    public void setExts(String[] exts) {
        this.mExts = exts;
    }

    public long getCapTimestamp() {
        return this.mCapTimestamp;
    }

    public void setCapTimestamp(long capTimestamp) {
        this.mCapTimestamp = capTimestamp;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeInt(this.mImSupported ? 1 : 0);
        if (this.mFtSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mFtThumbSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mFtSnFSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mFtHttpSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mIsSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mVsDuringCSSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mVsSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mSpSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mCdViaPresenceSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mIpVoiceSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mIpVideoSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mGeoPullFtSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mGeoPullSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mGeoPushSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mSmSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mFullSnFGroupChatSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mRcsIpVoiceCallSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mRcsIpVideoCallSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.mRcsIpVideoOnlyCallSupported) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeStringArray(this.mExts);
        dest.writeLong(this.mCapTimestamp);
    }

    private CapInfo(Parcel source) {
        this.mImSupported = false;
        this.mFtSupported = false;
        this.mFtThumbSupported = false;
        this.mFtSnFSupported = false;
        this.mFtHttpSupported = false;
        this.mIsSupported = false;
        this.mVsDuringCSSupported = false;
        this.mVsSupported = false;
        this.mSpSupported = false;
        this.mCdViaPresenceSupported = false;
        this.mIpVoiceSupported = false;
        this.mIpVideoSupported = false;
        this.mGeoPullFtSupported = false;
        this.mGeoPullSupported = false;
        this.mGeoPushSupported = false;
        this.mSmSupported = false;
        this.mFullSnFGroupChatSupported = false;
        this.mRcsIpVoiceCallSupported = false;
        this.mRcsIpVideoCallSupported = false;
        this.mRcsIpVideoOnlyCallSupported = false;
        this.mExts = new String[10];
        this.mCapTimestamp = 0;
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        boolean z;
        boolean z2 = false;
        this.mImSupported = source.readInt() != 0;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mFtSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mFtThumbSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mFtSnFSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mFtHttpSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mIsSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mVsDuringCSSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mVsSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mSpSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mCdViaPresenceSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mIpVoiceSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mIpVideoSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mGeoPullFtSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mGeoPullSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mGeoPushSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mSmSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mFullSnFGroupChatSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mRcsIpVoiceCallSupported = z;
        if (source.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mRcsIpVideoCallSupported = z;
        if (source.readInt() != 0) {
            z2 = true;
        }
        this.mRcsIpVideoOnlyCallSupported = z2;
        this.mExts = source.createStringArray();
        this.mCapTimestamp = source.readLong();
    }
}
