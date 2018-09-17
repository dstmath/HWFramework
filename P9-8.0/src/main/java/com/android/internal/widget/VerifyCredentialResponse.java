package com.android.internal.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class VerifyCredentialResponse implements Parcelable {
    public static final Creator<VerifyCredentialResponse> CREATOR = new Creator<VerifyCredentialResponse>() {
        public VerifyCredentialResponse createFromParcel(Parcel source) {
            int responseCode = source.readInt();
            VerifyCredentialResponse response = new VerifyCredentialResponse(responseCode, 0, null, null);
            if (responseCode == 1) {
                response.setTimeout(source.readInt());
            } else if (responseCode == 0) {
                int size = source.readInt();
                if (size > 0) {
                    byte[] payload = new byte[size];
                    source.readByteArray(payload);
                    response.setPayload(payload);
                }
            }
            return response;
        }

        public VerifyCredentialResponse[] newArray(int size) {
            return new VerifyCredentialResponse[size];
        }
    };
    public static final VerifyCredentialResponse ERROR = new VerifyCredentialResponse(-1, 0, null);
    public static final VerifyCredentialResponse OK = new VerifyCredentialResponse();
    public static final int RESPONSE_ERROR = -1;
    public static final int RESPONSE_OK = 0;
    public static final int RESPONSE_RETRY = 1;
    private byte[] mPayload;
    private int mResponseCode;
    private int mTimeout;

    /* synthetic */ VerifyCredentialResponse(int responseCode, int timeout, byte[] payload, VerifyCredentialResponse -this3) {
        this(responseCode, timeout, payload);
    }

    public VerifyCredentialResponse() {
        this.mResponseCode = 0;
        this.mPayload = null;
    }

    public VerifyCredentialResponse(byte[] payload) {
        this.mPayload = payload;
        this.mResponseCode = 0;
    }

    public VerifyCredentialResponse(int timeout) {
        this.mTimeout = timeout;
        this.mResponseCode = 1;
        this.mPayload = null;
    }

    private VerifyCredentialResponse(int responseCode, int timeout, byte[] payload) {
        this.mResponseCode = responseCode;
        this.mTimeout = timeout;
        this.mPayload = payload;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mResponseCode);
        if (this.mResponseCode == 1) {
            dest.writeInt(this.mTimeout);
        } else if (this.mResponseCode != 0) {
        } else {
            if (this.mPayload != null) {
                dest.writeInt(this.mPayload.length);
                dest.writeByteArray(this.mPayload);
                return;
            }
            dest.writeInt(0);
        }
    }

    public int describeContents() {
        return 0;
    }

    public byte[] getPayload() {
        return this.mPayload;
    }

    public int getTimeout() {
        return this.mTimeout;
    }

    public int getResponseCode() {
        return this.mResponseCode;
    }

    private void setTimeout(int timeout) {
        this.mTimeout = timeout;
    }

    private void setPayload(byte[] payload) {
        this.mPayload = payload;
    }
}
