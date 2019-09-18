package com.android.ims.internal.uce.options;

import android.os.Parcel;
import android.os.Parcelable;

public class OptionsSipResponse implements Parcelable {
    public static final Parcelable.Creator<OptionsSipResponse> CREATOR = new Parcelable.Creator<OptionsSipResponse>() {
        public OptionsSipResponse createFromParcel(Parcel source) {
            return new OptionsSipResponse(source);
        }

        public OptionsSipResponse[] newArray(int size) {
            return new OptionsSipResponse[size];
        }
    };
    private OptionsCmdId mCmdId;
    private String mReasonPhrase;
    private int mRequestId;
    private int mRetryAfter;
    private int mSipResponseCode;

    public OptionsCmdId getCmdId() {
        return this.mCmdId;
    }

    public void setCmdId(OptionsCmdId cmdId) {
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

    public OptionsSipResponse() {
        this.mRequestId = 0;
        this.mSipResponseCode = 0;
        this.mRetryAfter = 0;
        this.mReasonPhrase = "";
        this.mCmdId = new OptionsCmdId();
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

    private OptionsSipResponse(Parcel source) {
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
        this.mCmdId = (OptionsCmdId) source.readParcelable(OptionsCmdId.class.getClassLoader());
        this.mRetryAfter = source.readInt();
    }
}
