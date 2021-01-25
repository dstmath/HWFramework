package android.telecom;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.telecom.Connection;

public final class ConnectionRequest implements Parcelable {
    public static final Parcelable.Creator<ConnectionRequest> CREATOR = new Parcelable.Creator<ConnectionRequest>() {
        /* class android.telecom.ConnectionRequest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ConnectionRequest createFromParcel(Parcel source) {
            return new ConnectionRequest(source);
        }

        @Override // android.os.Parcelable.Creator
        public ConnectionRequest[] newArray(int size) {
            return new ConnectionRequest[size];
        }
    };
    private final PhoneAccountHandle mAccountHandle;
    private final Uri mAddress;
    private final Bundle mExtras;
    private final ParcelFileDescriptor mRttPipeFromInCall;
    private final ParcelFileDescriptor mRttPipeToInCall;
    private Connection.RttTextStream mRttTextStream;
    private final boolean mShouldShowIncomingCallUi;
    private final String mTelecomCallId;
    private final int mVideoState;

    public static final class Builder {
        private PhoneAccountHandle mAccountHandle;
        private Uri mAddress;
        private Bundle mExtras;
        private ParcelFileDescriptor mRttPipeFromInCall;
        private ParcelFileDescriptor mRttPipeToInCall;
        private boolean mShouldShowIncomingCallUi = false;
        private String mTelecomCallId;
        private int mVideoState = 0;

        public Builder setAccountHandle(PhoneAccountHandle accountHandle) {
            this.mAccountHandle = accountHandle;
            return this;
        }

        public Builder setAddress(Uri address) {
            this.mAddress = address;
            return this;
        }

        public Builder setExtras(Bundle extras) {
            this.mExtras = extras;
            return this;
        }

        public Builder setVideoState(int videoState) {
            this.mVideoState = videoState;
            return this;
        }

        public Builder setTelecomCallId(String telecomCallId) {
            this.mTelecomCallId = telecomCallId;
            return this;
        }

        public Builder setShouldShowIncomingCallUi(boolean shouldShowIncomingCallUi) {
            this.mShouldShowIncomingCallUi = shouldShowIncomingCallUi;
            return this;
        }

        public Builder setRttPipeFromInCall(ParcelFileDescriptor rttPipeFromInCall) {
            this.mRttPipeFromInCall = rttPipeFromInCall;
            return this;
        }

        public Builder setRttPipeToInCall(ParcelFileDescriptor rttPipeToInCall) {
            this.mRttPipeToInCall = rttPipeToInCall;
            return this;
        }

        public ConnectionRequest build() {
            return new ConnectionRequest(this.mAccountHandle, this.mAddress, this.mExtras, this.mVideoState, this.mTelecomCallId, this.mShouldShowIncomingCallUi, this.mRttPipeFromInCall, this.mRttPipeToInCall);
        }
    }

    public ConnectionRequest(PhoneAccountHandle accountHandle, Uri handle, Bundle extras) {
        this(accountHandle, handle, extras, 0, null, false, null, null);
    }

    public ConnectionRequest(PhoneAccountHandle accountHandle, Uri handle, Bundle extras, int videoState) {
        this(accountHandle, handle, extras, videoState, null, false, null, null);
    }

    public ConnectionRequest(PhoneAccountHandle accountHandle, Uri handle, Bundle extras, int videoState, String telecomCallId, boolean shouldShowIncomingCallUi) {
        this(accountHandle, handle, extras, videoState, telecomCallId, shouldShowIncomingCallUi, null, null);
    }

    private ConnectionRequest(PhoneAccountHandle accountHandle, Uri handle, Bundle extras, int videoState, String telecomCallId, boolean shouldShowIncomingCallUi, ParcelFileDescriptor rttPipeFromInCall, ParcelFileDescriptor rttPipeToInCall) {
        this.mAccountHandle = accountHandle;
        this.mAddress = handle;
        this.mExtras = extras;
        this.mVideoState = videoState;
        this.mTelecomCallId = telecomCallId;
        this.mShouldShowIncomingCallUi = shouldShowIncomingCallUi;
        this.mRttPipeFromInCall = rttPipeFromInCall;
        this.mRttPipeToInCall = rttPipeToInCall;
    }

    private ConnectionRequest(Parcel in) {
        this.mAccountHandle = (PhoneAccountHandle) in.readParcelable(getClass().getClassLoader());
        this.mAddress = (Uri) in.readParcelable(getClass().getClassLoader());
        this.mExtras = (Bundle) in.readParcelable(getClass().getClassLoader());
        this.mVideoState = in.readInt();
        this.mTelecomCallId = in.readString();
        this.mShouldShowIncomingCallUi = in.readInt() != 1 ? false : true;
        this.mRttPipeFromInCall = (ParcelFileDescriptor) in.readParcelable(getClass().getClassLoader());
        this.mRttPipeToInCall = (ParcelFileDescriptor) in.readParcelable(getClass().getClassLoader());
    }

    public PhoneAccountHandle getAccountHandle() {
        return this.mAccountHandle;
    }

    public Uri getAddress() {
        return this.mAddress;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public int getVideoState() {
        return this.mVideoState;
    }

    public String getTelecomCallId() {
        return this.mTelecomCallId;
    }

    public boolean shouldShowIncomingCallUi() {
        return this.mShouldShowIncomingCallUi;
    }

    public ParcelFileDescriptor getRttPipeToInCall() {
        return this.mRttPipeToInCall;
    }

    public ParcelFileDescriptor getRttPipeFromInCall() {
        return this.mRttPipeFromInCall;
    }

    public Connection.RttTextStream getRttTextStream() {
        if (!isRequestingRtt()) {
            return null;
        }
        if (this.mRttTextStream == null) {
            this.mRttTextStream = new Connection.RttTextStream(this.mRttPipeToInCall, this.mRttPipeFromInCall);
        }
        return this.mRttTextStream;
    }

    public boolean isRequestingRtt() {
        return (this.mRttPipeFromInCall == null || this.mRttPipeToInCall == null) ? false : true;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x000e: APUT  (r0v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r1v1 java.lang.Object) */
    public String toString() {
        Object obj;
        Object[] objArr = new Object[1];
        if (this.mAddress == null) {
            obj = Uri.EMPTY;
        } else {
            obj = "xxxxxx";
        }
        objArr[0] = obj;
        return String.format("ConnectionRequest %s", objArr);
    }

    private static String bundleToString(Bundle extras) {
        if (extras == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Bundle[");
        for (String key : extras.keySet()) {
            sb.append(key);
            sb.append("=");
            char c = 65535;
            int hashCode = key.hashCode();
            if (hashCode != -1582002592) {
                if (hashCode == -1513984200 && key.equals(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS)) {
                    c = 0;
                }
            } else if (key.equals(TelecomManager.EXTRA_UNKNOWN_CALL_HANDLE)) {
                c = 1;
            }
            if (c == 0 || c == 1) {
                sb.append(Log.pii(extras.get(key)));
            } else {
                sb.append(extras.get(key));
            }
            sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeParcelable(this.mAccountHandle, 0);
        destination.writeParcelable(this.mAddress, 0);
        destination.writeParcelable(this.mExtras, 0);
        destination.writeInt(this.mVideoState);
        destination.writeString(this.mTelecomCallId);
        destination.writeInt(this.mShouldShowIncomingCallUi ? 1 : 0);
        destination.writeParcelable(this.mRttPipeFromInCall, 0);
        destination.writeParcelable(this.mRttPipeToInCall, 0);
    }
}
