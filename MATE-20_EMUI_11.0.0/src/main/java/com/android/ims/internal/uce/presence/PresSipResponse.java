package com.android.ims.internal.uce.presence;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;

public class PresSipResponse implements Parcelable {
    public static final Parcelable.Creator<PresSipResponse> CREATOR = new Parcelable.Creator<PresSipResponse>() {
        /* class com.android.ims.internal.uce.presence.PresSipResponse.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PresSipResponse createFromParcel(Parcel source) {
            return new PresSipResponse(source);
        }

        @Override // android.os.Parcelable.Creator
        public PresSipResponse[] newArray(int size) {
            return new PresSipResponse[size];
        }
    };
    private PresCmdId mCmdId;
    private String mReasonPhrase;
    private int mRequestId;
    private int mRetryAfter;
    private int mSipResponseCode;

    @UnsupportedAppUsage
    public PresCmdId getCmdId() {
        return this.mCmdId;
    }

    @UnsupportedAppUsage
    public void setCmdId(PresCmdId cmdId) {
        this.mCmdId = cmdId;
    }

    @UnsupportedAppUsage
    public int getRequestId() {
        return this.mRequestId;
    }

    @UnsupportedAppUsage
    public void setRequestId(int requestId) {
        this.mRequestId = requestId;
    }

    @UnsupportedAppUsage
    public int getSipResponseCode() {
        return this.mSipResponseCode;
    }

    @UnsupportedAppUsage
    public void setSipResponseCode(int sipResponseCode) {
        this.mSipResponseCode = sipResponseCode;
    }

    @UnsupportedAppUsage
    public String getReasonPhrase() {
        return this.mReasonPhrase;
    }

    @UnsupportedAppUsage
    public void setReasonPhrase(String reasonPhrase) {
        this.mReasonPhrase = reasonPhrase;
    }

    @UnsupportedAppUsage
    public int getRetryAfter() {
        return this.mRetryAfter;
    }

    @UnsupportedAppUsage
    public void setRetryAfter(int retryAfter) {
        this.mRetryAfter = retryAfter;
    }

    @UnsupportedAppUsage
    public PresSipResponse() {
        this.mCmdId = new PresCmdId();
        this.mRequestId = 0;
        this.mSipResponseCode = 0;
        this.mRetryAfter = 0;
        this.mReasonPhrase = "";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRequestId);
        dest.writeInt(this.mSipResponseCode);
        dest.writeString(this.mReasonPhrase);
        dest.writeParcelable(this.mCmdId, flags);
        dest.writeInt(this.mRetryAfter);
    }

    private PresSipResponse(Parcel source) {
        this.mCmdId = new PresCmdId();
        this.mRequestId = 0;
        this.mSipResponseCode = 0;
        this.mRetryAfter = 0;
        this.mReasonPhrase = "";
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mRequestId = source.readInt();
        this.mSipResponseCode = source.readInt();
        this.mReasonPhrase = source.readString();
        this.mCmdId = (PresCmdId) source.readParcelable(PresCmdId.class.getClassLoader());
        this.mRetryAfter = source.readInt();
    }
}
