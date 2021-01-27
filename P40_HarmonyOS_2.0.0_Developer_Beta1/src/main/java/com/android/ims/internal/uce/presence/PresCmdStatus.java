package com.android.ims.internal.uce.presence;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.ims.internal.uce.common.StatusCode;

public class PresCmdStatus implements Parcelable {
    public static final Parcelable.Creator<PresCmdStatus> CREATOR = new Parcelable.Creator<PresCmdStatus>() {
        /* class com.android.ims.internal.uce.presence.PresCmdStatus.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PresCmdStatus createFromParcel(Parcel source) {
            return new PresCmdStatus(source);
        }

        @Override // android.os.Parcelable.Creator
        public PresCmdStatus[] newArray(int size) {
            return new PresCmdStatus[size];
        }
    };
    private PresCmdId mCmdId;
    private int mRequestId;
    private StatusCode mStatus;
    private int mUserData;

    public PresCmdId getCmdId() {
        return this.mCmdId;
    }

    @UnsupportedAppUsage
    public void setCmdId(PresCmdId cmdId) {
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

    public int getRequestId() {
        return this.mRequestId;
    }

    @UnsupportedAppUsage
    public void setRequestId(int requestId) {
        this.mRequestId = requestId;
    }

    @UnsupportedAppUsage
    public PresCmdStatus() {
        this.mCmdId = new PresCmdId();
        this.mStatus = new StatusCode();
        this.mStatus = new StatusCode();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
