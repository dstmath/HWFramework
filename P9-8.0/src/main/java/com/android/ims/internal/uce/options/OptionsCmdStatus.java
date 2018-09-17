package com.android.ims.internal.uce.options;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.ims.internal.uce.common.CapInfo;
import com.android.ims.internal.uce.common.StatusCode;

public class OptionsCmdStatus implements Parcelable {
    public static final Creator<OptionsCmdStatus> CREATOR = new Creator<OptionsCmdStatus>() {
        public OptionsCmdStatus createFromParcel(Parcel source) {
            return new OptionsCmdStatus(source, null);
        }

        public OptionsCmdStatus[] newArray(int size) {
            return new OptionsCmdStatus[size];
        }
    };
    private CapInfo mCapInfo;
    private OptionsCmdId mCmdId;
    private StatusCode mStatus;
    private int mUserData;

    /* synthetic */ OptionsCmdStatus(Parcel source, OptionsCmdStatus -this1) {
        this(source);
    }

    public OptionsCmdId getCmdId() {
        return this.mCmdId;
    }

    public void setCmdId(OptionsCmdId cmdId) {
        this.mCmdId = cmdId;
    }

    public int getUserData() {
        return this.mUserData;
    }

    public void setUserData(int userData) {
        this.mUserData = userData;
    }

    public StatusCode getStatus() {
        return this.mStatus;
    }

    public void setStatus(StatusCode status) {
        this.mStatus = status;
    }

    public OptionsCmdStatus() {
        this.mStatus = new StatusCode();
        this.mCapInfo = new CapInfo();
        this.mCmdId = new OptionsCmdId();
        this.mUserData = 0;
    }

    public CapInfo getCapInfo() {
        return this.mCapInfo;
    }

    public void setCapInfo(CapInfo capInfo) {
        this.mCapInfo = capInfo;
    }

    public static OptionsCmdStatus getOptionsCmdStatusInstance() {
        return new OptionsCmdStatus();
    }

    public int describeContents() {
        return 0;
    }

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
