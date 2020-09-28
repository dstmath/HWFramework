package com.android.internal.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.service.gatekeeper.GateKeeperResponse;
import android.util.Slog;

public final class VerifyCredentialResponse implements Parcelable {
    public static final Parcelable.Creator<VerifyCredentialResponse> CREATOR = new Parcelable.Creator<VerifyCredentialResponse>() {
        /* class com.android.internal.widget.VerifyCredentialResponse.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VerifyCredentialResponse createFromParcel(Parcel source) {
            int size;
            int responseCode = source.readInt();
            VerifyCredentialResponse response = new VerifyCredentialResponse(responseCode, 0, null);
            if (responseCode == 1) {
                response.setTimeout(source.readInt());
            } else if (responseCode == 0 && (size = source.readInt()) > 0) {
                byte[] payload = new byte[size];
                source.readByteArray(payload);
                response.setPayload(payload);
            }
            return response;
        }

        @Override // android.os.Parcelable.Creator
        public VerifyCredentialResponse[] newArray(int size) {
            return new VerifyCredentialResponse[size];
        }
    };
    public static final VerifyCredentialResponse ERROR = new VerifyCredentialResponse(-1, 0, null);
    public static final VerifyCredentialResponse OK = new VerifyCredentialResponse();
    public static final int RESPONSE_ERROR = -1;
    public static final int RESPONSE_OK = 0;
    public static final int RESPONSE_RETRY = 1;
    private static final String TAG = "VerifyCredentialResponse";
    private byte[] mPayload;
    private int mResponseCode;
    private int mTimeout;

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

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mResponseCode);
        int i = this.mResponseCode;
        if (i == 1) {
            dest.writeInt(this.mTimeout);
        } else if (i == 0) {
            byte[] bArr = this.mPayload;
            if (bArr != null) {
                dest.writeInt(bArr.length);
                dest.writeByteArray(this.mPayload);
                return;
            }
            dest.writeInt(0);
        }
    }

    @Override // android.os.Parcelable
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setTimeout(int timeout) {
        this.mTimeout = timeout;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setPayload(byte[] payload) {
        this.mPayload = payload;
    }

    public VerifyCredentialResponse stripPayload() {
        return new VerifyCredentialResponse(this.mResponseCode, this.mTimeout, new byte[0]);
    }

    public static VerifyCredentialResponse fromGateKeeperResponse(GateKeeperResponse gateKeeperResponse) {
        int responseCode = gateKeeperResponse.getResponseCode();
        if (responseCode == 1) {
            return new VerifyCredentialResponse(gateKeeperResponse.getTimeout());
        }
        if (responseCode != 0) {
            return ERROR;
        }
        byte[] token = gateKeeperResponse.getPayload();
        if (token != null) {
            return new VerifyCredentialResponse(token);
        }
        Slog.e(TAG, "verifyChallenge response had no associated payload");
        return ERROR;
    }
}
