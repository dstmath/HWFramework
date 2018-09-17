package com.android.ims.internal.uce.presence;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.LogException;

public class PresSipResponse implements Parcelable {
    public static final Creator<PresSipResponse> CREATOR = new Creator<PresSipResponse>() {
        public PresSipResponse createFromParcel(Parcel source) {
            return new PresSipResponse(source, null);
        }

        public PresSipResponse[] newArray(int size) {
            return new PresSipResponse[size];
        }
    };
    private PresCmdId mCmdId;
    private String mReasonPhrase;
    private int mRequestId;
    private int mRetryAfter;
    private int mSipResponseCode;

    /* synthetic */ PresSipResponse(Parcel source, PresSipResponse -this1) {
        this(source);
    }

    public PresCmdId getCmdId() {
        return this.mCmdId;
    }

    public void setCmdId(PresCmdId cmdId) {
        this.mCmdId = cmdId;
    }

    public int getRequestId() {
        return this.mRequestId;
    }

    public void setRequestId(int requestId) {
        this.mRequestId = requestId;
    }

    public int getSipResponseCode() {
        return this.mSipResponseCode;
    }

    public void setSipResponseCode(int sipResponseCode) {
        this.mSipResponseCode = sipResponseCode;
    }

    public String getReasonPhrase() {
        return this.mReasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.mReasonPhrase = reasonPhrase;
    }

    public int getRetryAfter() {
        return this.mRetryAfter;
    }

    public void setRetryAfter(int retryAfter) {
        this.mRetryAfter = retryAfter;
    }

    public PresSipResponse() {
        this.mCmdId = new PresCmdId();
        this.mRequestId = 0;
        this.mSipResponseCode = 0;
        this.mRetryAfter = 0;
        this.mReasonPhrase = LogException.NO_VALUE;
    }

    public int describeContents() {
        return 0;
    }

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
        this.mReasonPhrase = LogException.NO_VALUE;
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
