package android.telecom;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.telecom.IVideoProvider;
import java.util.ArrayList;
import java.util.List;

public final class ParcelableConnection implements Parcelable {
    public static final Parcelable.Creator<ParcelableConnection> CREATOR = new Parcelable.Creator<ParcelableConnection>() {
        /* class android.telecom.ParcelableConnection.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableConnection createFromParcel(Parcel source) {
            ClassLoader classLoader = ParcelableConnection.class.getClassLoader();
            PhoneAccountHandle phoneAccount = (PhoneAccountHandle) source.readParcelable(classLoader);
            int state = source.readInt();
            int capabilities = source.readInt();
            Uri address = (Uri) source.readParcelable(classLoader);
            int addressPresentation = source.readInt();
            String callerDisplayName = source.readString();
            int callerDisplayNamePresentation = source.readInt();
            IVideoProvider videoCallProvider = IVideoProvider.Stub.asInterface(source.readStrongBinder());
            int videoState = source.readInt();
            boolean ringbackRequested = source.readByte() == 1;
            boolean audioModeIsVoip = source.readByte() == 1;
            long connectTimeMillis = source.readLong();
            StatusHints statusHints = (StatusHints) source.readParcelable(classLoader);
            DisconnectCause disconnectCause = (DisconnectCause) source.readParcelable(classLoader);
            List<String> conferenceableConnectionIds = new ArrayList<>();
            source.readStringList(conferenceableConnectionIds);
            return new ParcelableConnection(phoneAccount, state, capabilities, source.readInt(), source.readInt(), address, addressPresentation, callerDisplayName, callerDisplayNamePresentation, videoCallProvider, videoState, ringbackRequested, audioModeIsVoip, connectTimeMillis, source.readLong(), statusHints, disconnectCause, conferenceableConnectionIds, Bundle.setDefusable(source.readBundle(classLoader), true), source.readString(), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableConnection[] newArray(int size) {
            return new ParcelableConnection[size];
        }
    };
    private final Uri mAddress;
    private final int mAddressPresentation;
    private int mCallDirection;
    private final String mCallerDisplayName;
    private final int mCallerDisplayNamePresentation;
    private final List<String> mConferenceableConnectionIds;
    private final long mConnectElapsedTimeMillis;
    private final long mConnectTimeMillis;
    private final int mConnectionCapabilities;
    private final int mConnectionProperties;
    private final DisconnectCause mDisconnectCause;
    private final Bundle mExtras;
    private final boolean mIsVoipAudioMode;
    private String mParentCallId;
    private final PhoneAccountHandle mPhoneAccount;
    private final boolean mRingbackRequested;
    private final int mState;
    private final StatusHints mStatusHints;
    private final int mSupportedAudioRoutes;
    private final IVideoProvider mVideoProvider;
    private final int mVideoState;

    public ParcelableConnection(PhoneAccountHandle phoneAccount, int state, int capabilities, int properties, int supportedAudioRoutes, Uri address, int addressPresentation, String callerDisplayName, int callerDisplayNamePresentation, IVideoProvider videoProvider, int videoState, boolean ringbackRequested, boolean isVoipAudioMode, long connectTimeMillis, long connectElapsedTimeMillis, StatusHints statusHints, DisconnectCause disconnectCause, List<String> conferenceableConnectionIds, Bundle extras, String parentCallId, int callDirection) {
        this(phoneAccount, state, capabilities, properties, supportedAudioRoutes, address, addressPresentation, callerDisplayName, callerDisplayNamePresentation, videoProvider, videoState, ringbackRequested, isVoipAudioMode, connectTimeMillis, connectElapsedTimeMillis, statusHints, disconnectCause, conferenceableConnectionIds, extras);
        this.mParentCallId = parentCallId;
        this.mCallDirection = callDirection;
    }

    public ParcelableConnection(PhoneAccountHandle phoneAccount, int state, int capabilities, int properties, int supportedAudioRoutes, Uri address, int addressPresentation, String callerDisplayName, int callerDisplayNamePresentation, IVideoProvider videoProvider, int videoState, boolean ringbackRequested, boolean isVoipAudioMode, long connectTimeMillis, long connectElapsedTimeMillis, StatusHints statusHints, DisconnectCause disconnectCause, List<String> conferenceableConnectionIds, Bundle extras) {
        this.mPhoneAccount = phoneAccount;
        this.mState = state;
        this.mConnectionCapabilities = capabilities;
        this.mConnectionProperties = properties;
        this.mSupportedAudioRoutes = supportedAudioRoutes;
        this.mAddress = address;
        this.mAddressPresentation = addressPresentation;
        this.mCallerDisplayName = callerDisplayName;
        this.mCallerDisplayNamePresentation = callerDisplayNamePresentation;
        this.mVideoProvider = videoProvider;
        this.mVideoState = videoState;
        this.mRingbackRequested = ringbackRequested;
        this.mIsVoipAudioMode = isVoipAudioMode;
        this.mConnectTimeMillis = connectTimeMillis;
        this.mConnectElapsedTimeMillis = connectElapsedTimeMillis;
        this.mStatusHints = statusHints;
        this.mDisconnectCause = disconnectCause;
        this.mConferenceableConnectionIds = conferenceableConnectionIds;
        this.mExtras = extras;
        this.mParentCallId = null;
        this.mCallDirection = -1;
    }

    public PhoneAccountHandle getPhoneAccount() {
        return this.mPhoneAccount;
    }

    public int getState() {
        return this.mState;
    }

    public int getConnectionCapabilities() {
        return this.mConnectionCapabilities;
    }

    public int getConnectionProperties() {
        return this.mConnectionProperties;
    }

    public int getSupportedAudioRoutes() {
        return this.mSupportedAudioRoutes;
    }

    public Uri getHandle() {
        return this.mAddress;
    }

    public int getHandlePresentation() {
        return this.mAddressPresentation;
    }

    public String getCallerDisplayName() {
        return this.mCallerDisplayName;
    }

    public int getCallerDisplayNamePresentation() {
        return this.mCallerDisplayNamePresentation;
    }

    public IVideoProvider getVideoProvider() {
        return this.mVideoProvider;
    }

    public int getVideoState() {
        return this.mVideoState;
    }

    public boolean isRingbackRequested() {
        return this.mRingbackRequested;
    }

    public boolean getIsVoipAudioMode() {
        return this.mIsVoipAudioMode;
    }

    public long getConnectTimeMillis() {
        return this.mConnectTimeMillis;
    }

    public long getConnectElapsedTimeMillis() {
        return this.mConnectElapsedTimeMillis;
    }

    public final StatusHints getStatusHints() {
        return this.mStatusHints;
    }

    public final DisconnectCause getDisconnectCause() {
        return this.mDisconnectCause;
    }

    public final List<String> getConferenceableConnectionIds() {
        return this.mConferenceableConnectionIds;
    }

    public final Bundle getExtras() {
        return this.mExtras;
    }

    public final String getParentCallId() {
        return this.mParentCallId;
    }

    public int getCallDirection() {
        return this.mCallDirection;
    }

    public String toString() {
        return "ParcelableConnection [act:" + this.mPhoneAccount + "], state:" + this.mState + ", capabilities:" + Connection.capabilitiesToString(this.mConnectionCapabilities) + ", properties:" + Connection.propertiesToString(this.mConnectionProperties) + ", extras:" + this.mExtras + ", parent:" + this.mParentCallId + ", callDirection:" + this.mCallDirection;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeParcelable(this.mPhoneAccount, 0);
        destination.writeInt(this.mState);
        destination.writeInt(this.mConnectionCapabilities);
        destination.writeParcelable(this.mAddress, 0);
        destination.writeInt(this.mAddressPresentation);
        destination.writeString(this.mCallerDisplayName);
        destination.writeInt(this.mCallerDisplayNamePresentation);
        IVideoProvider iVideoProvider = this.mVideoProvider;
        destination.writeStrongBinder(iVideoProvider != null ? iVideoProvider.asBinder() : null);
        destination.writeInt(this.mVideoState);
        destination.writeByte(this.mRingbackRequested ? (byte) 1 : 0);
        destination.writeByte(this.mIsVoipAudioMode ? (byte) 1 : 0);
        destination.writeLong(this.mConnectTimeMillis);
        destination.writeParcelable(this.mStatusHints, 0);
        destination.writeParcelable(this.mDisconnectCause, 0);
        destination.writeStringList(this.mConferenceableConnectionIds);
        destination.writeBundle(this.mExtras);
        destination.writeInt(this.mConnectionProperties);
        destination.writeInt(this.mSupportedAudioRoutes);
        destination.writeString(this.mParentCallId);
        destination.writeLong(this.mConnectElapsedTimeMillis);
        destination.writeInt(this.mCallDirection);
    }
}
