package com.android.ims.internal.uce.presence;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.ims.internal.uce.common.StatusCode;

public class PresCmdStatus implements Parcelable {
    public static final Creator<PresCmdStatus> CREATOR = new Creator<PresCmdStatus>() {
        public PresCmdStatus createFromParcel(Parcel source) {
            return new PresCmdStatus(source, null);
        }

        public PresCmdStatus[] newArray(int size) {
            return new PresCmdStatus[size];
        }
    };
    private PresCmdId mCmdId;
    private int mRequestId;
    private StatusCode mStatus;
    private int mUserData;

    /* synthetic */ PresCmdStatus(Parcel source, PresCmdStatus -this1) {
        this(source);
    }

    public PresCmdId getCmdId() {
        return this.mCmdId;
    }

    public void setCmdId(PresCmdId cmdId) {
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

    public int getRequestId() {
        return this.mRequestId;
    }

    public void setRequestId(int requestId) {
        this.mRequestId = requestId;
    }

    public PresCmdStatus() {
        this.mCmdId = new PresCmdId();
        this.mStatus = new StatusCode();
        this.mStatus = new StatusCode();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUserData);
        dest.writeInt(this.mRequestId);
        dest.writeParcelable(this.mCmdId, flags);
        dest.writeParcelable(this.mStatus, flags);
    }

    private PresCmdStatus(Parcel source) {
        this.mCmdId = new PresCmdId();
        this.mStatus = new StatusCode();
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mUserData = source.readInt();
        this.mRequestId = source.readInt();
        this.mCmdId = (PresCmdId) source.readParcelable(PresCmdId.class.getClassLoader());
        this.mStatus = (StatusCode) source.readParcelable(StatusCode.class.getClassLoader());
    }
}
