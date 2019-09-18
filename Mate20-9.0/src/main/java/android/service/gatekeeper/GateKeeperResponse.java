package android.service.gatekeeper;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.annotations.VisibleForTesting;

public final class GateKeeperResponse implements Parcelable {
    public static final Parcelable.Creator<GateKeeperResponse> CREATOR = new Parcelable.Creator<GateKeeperResponse>() {
        public GateKeeperResponse createFromParcel(Parcel source) {
            int responseCode = source.readInt();
            boolean shouldReEnroll = true;
            if (responseCode == 1) {
                return GateKeeperResponse.createRetryResponse(source.readInt());
            }
            if (responseCode != 0) {
                return GateKeeperResponse.createGenericResponse(responseCode);
            }
            if (source.readInt() != 1) {
                shouldReEnroll = false;
            }
            byte[] payload = null;
            int size = source.readInt();
            if (size > 0) {
                payload = new byte[size];
                source.readByteArray(payload);
            }
            return GateKeeperResponse.createOkResponse(payload, shouldReEnroll);
        }

        public GateKeeperResponse[] newArray(int size) {
            return new GateKeeperResponse[size];
        }
    };
    public static final int RESPONSE_ERROR = -1;
    public static final int RESPONSE_OK = 0;
    public static final int RESPONSE_RETRY = 1;
    private byte[] mPayload;
    private final int mResponseCode;
    private boolean mShouldReEnroll;
    private int mTimeout;

    private GateKeeperResponse(int responseCode) {
        this.mResponseCode = responseCode;
    }

    @VisibleForTesting
    public static GateKeeperResponse createGenericResponse(int responseCode) {
        return new GateKeeperResponse(responseCode);
    }

    /* access modifiers changed from: private */
    public static GateKeeperResponse createRetryResponse(int timeout) {
        GateKeeperResponse response = new GateKeeperResponse(1);
        response.mTimeout = timeout;
        return response;
    }

    @VisibleForTesting
    public static GateKeeperResponse createOkResponse(byte[] payload, boolean shouldReEnroll) {
        GateKeeperResponse response = new GateKeeperResponse(0);
        response.mPayload = payload;
        response.mShouldReEnroll = shouldReEnroll;
        return response;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mResponseCode);
        if (this.mResponseCode == 1) {
            dest.writeInt(this.mTimeout);
        } else if (this.mResponseCode == 0) {
            dest.writeInt(this.mShouldReEnroll ? 1 : 0);
            if (this.mPayload != null) {
                dest.writeInt(this.mPayload.length);
                dest.writeByteArray(this.mPayload);
                return;
            }
            dest.writeInt(0);
        }
    }

    public byte[] getPayload() {
        return this.mPayload;
    }

    public int getTimeout() {
        return this.mTimeout;
    }

    public boolean getShouldReEnroll() {
        return this.mShouldReEnroll;
    }

    public int getResponseCode() {
        return this.mResponseCode;
    }
}
