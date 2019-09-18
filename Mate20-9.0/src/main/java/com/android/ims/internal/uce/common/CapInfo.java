package com.android.ims.internal.uce.common;

import android.os.Parcel;
import android.os.Parcelable;

public class CapInfo implements Parcelable {
    public static final Parcelable.Creator<CapInfo> CREATOR = new Parcelable.Creator<CapInfo>() {
        public CapInfo createFromParcel(Parcel source) {
            return new CapInfo(source);
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
        dest.writeInt(this.mImSupported ? 1 : 0);
        dest.writeInt(this.mFtSupported ? 1 : 0);
        dest.writeInt(this.mFtThumbSupported ? 1 : 0);
        dest.writeInt(this.mFtSnFSupported ? 1 : 0);
        dest.writeInt(this.mFtHttpSupported ? 1 : 0);
        dest.writeInt(this.mIsSupported ? 1 : 0);
        dest.writeInt(this.mVsDuringCSSupported ? 1 : 0);
        dest.writeInt(this.mVsSupported ? 1 : 0);
        dest.writeInt(this.mSpSupported ? 1 : 0);
        dest.writeInt(this.mCdViaPresenceSupported ? 1 : 0);
        dest.writeInt(this.mIpVoiceSupported ? 1 : 0);
        dest.writeInt(this.mIpVideoSupported ? 1 : 0);
        dest.writeInt(this.mGeoPullFtSupported ? 1 : 0);
        dest.writeInt(this.mGeoPullSupported ? 1 : 0);
        dest.writeInt(this.mGeoPushSupported ? 1 : 0);
        dest.writeInt(this.mSmSupported ? 1 : 0);
        dest.writeInt(this.mFullSnFGroupChatSupported ? 1 : 0);
        dest.writeInt(this.mRcsIpVoiceCallSupported ? 1 : 0);
        dest.writeInt(this.mRcsIpVideoCallSupported ? 1 : 0);
        dest.writeInt(this.mRcsIpVideoOnlyCallSupported ? 1 : 0);
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
        boolean z = true;
        this.mImSupported = source.readInt() != 0;
        this.mFtSupported = source.readInt() != 0;
        this.mFtThumbSupported = source.readInt() != 0;
        this.mFtSnFSupported = source.readInt() != 0;
        this.mFtHttpSupported = source.readInt() != 0;
        this.mIsSupported = source.readInt() != 0;
        this.mVsDuringCSSupported = source.readInt() != 0;
        this.mVsSupported = source.readInt() != 0;
        this.mSpSupported = source.readInt() != 0;
        this.mCdViaPresenceSupported = source.readInt() != 0;
        this.mIpVoiceSupported = source.readInt() != 0;
        this.mIpVideoSupported = source.readInt() != 0;
        this.mGeoPullFtSupported = source.readInt() != 0;
        this.mGeoPullSupported = source.readInt() != 0;
        this.mGeoPushSupported = source.readInt() != 0;
        this.mSmSupported = source.readInt() != 0;
        this.mFullSnFGroupChatSupported = source.readInt() != 0;
        this.mRcsIpVoiceCallSupported = source.readInt() != 0;
        this.mRcsIpVideoCallSupported = source.readInt() != 0;
        if (source.readInt() == 0) {
            z = false;
        }
        this.mRcsIpVideoOnlyCallSupported = z;
        this.mExts = source.createStringArray();
        this.mCapTimestamp = source.readLong();
    }
}
