package com.android.ims.internal.uce.options;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.ims.internal.uce.common.CapInfo;
import com.android.ims.internal.uce.common.StatusCode;

public class OptionsCmdStatus implements Parcelable {
    public static final Parcelable.Creator<OptionsCmdStatus> CREATOR = new Parcelable.Creator<OptionsCmdStatus>() {
        /* class com.android.ims.internal.uce.options.OptionsCmdStatus.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OptionsCmdStatus createFromParcel(Parcel source) {
            return new OptionsCmdStatus(source);
        }

        @Override // android.os.Parcelable.Creator
        public OptionsCmdStatus[] newArray(int size) {
            return new OptionsCmdStatus[size];
        }
    };
    private CapInfo mCapInfo;
    private OptionsCmdId mCmdId;
    private StatusCode mStatus;
    private int mUserData;

    public OptionsCmdId getCmdId() {
        return this.mCmdId;
    }

    @UnsupportedAppUsage
    public void setCmdId(OptionsCmdId cmdId) {
        this.mCmdId = cmdId;
    }

    public int getUserData() {
        return this.mUserData;
    }

    @UnsupportedAppUsage
    public void setUserData(int userData) {
        this.mUserData = userData;
    }

    public StatusCode getStatus() {
        return this.mStatus;
    }

    @UnsupportedAppUsage
    public void setStatus(StatusCode status) {
        this.mStatus = status;
    }

    @UnsupportedAppUsage
    public OptionsCmdStatus() {
        this.mStatus = new StatusCode();
        this.mCapInfo = new CapInfo();
        this.mCmdId = new OptionsCmdId();
        this.mUserData = 0;
    }

    public CapInfo getCapInfo() {
        return this.mCapInfo;
    }

    @UnsupportedAppUsage
    public void setCapInfo(CapInfo capInfo) {
        this.mCapInfo = capInfo;
    }

    public static OptionsCmdStatus getOptionsCmdStatusInstance() {
        return new OptionsCmdStatus();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUserData);
        dest.writeParcelable(this.mCmdId, flags);
        dest.writeParcelable(this.mStatus, flags);
        dest.writeParcelable(this.mCapInfo, flags);
    }

    private OptionsCmdStatus(Parcel source) {
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mUserData = source.readInt();
        this.mCmdId = (OptionsCmdId) source.readParcelable(OptionsCmdId.class.getClassLoader());
        this.mStatus = (StatusCode) source.readParcelable(StatusCode.class.getClassLoader());
        this.mCapInfo = (CapInfo) source.readParcelable(CapInfo.class.getClassLoader());
    }
}
