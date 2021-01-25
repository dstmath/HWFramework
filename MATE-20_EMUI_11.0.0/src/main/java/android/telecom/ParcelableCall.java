package android.telecom;

import android.annotation.UnsupportedAppUsage;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import com.android.internal.telecom.IVideoProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ParcelableCall implements Parcelable {
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static final Parcelable.Creator<ParcelableCall> CREATOR = new Parcelable.Creator<ParcelableCall>() {
        /* class android.telecom.ParcelableCall.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableCall createFromParcel(Parcel source) {
            ClassLoader classLoader = ParcelableCall.class.getClassLoader();
            String id = source.readString();
            int state = source.readInt();
            DisconnectCause disconnectCause = (DisconnectCause) source.readParcelable(classLoader);
            List<String> cannedSmsResponses = new ArrayList<>();
            source.readList(cannedSmsResponses, classLoader);
            int capabilities = source.readInt();
            int properties = source.readInt();
            long connectTimeMillis = source.readLong();
            Uri handle = (Uri) source.readParcelable(classLoader);
            int handlePresentation = source.readInt();
            String callerDisplayName = source.readString();
            int callerDisplayNamePresentation = source.readInt();
            GatewayInfo gatewayInfo = (GatewayInfo) source.readParcelable(classLoader);
            PhoneAccountHandle accountHandle = (PhoneAccountHandle) source.readParcelable(classLoader);
            boolean isVideoCallProviderChanged = source.readByte() == 1;
            IVideoProvider videoCallProvider = IVideoProvider.Stub.asInterface(source.readStrongBinder());
            String parentCallId = source.readString();
            List<String> childCallIds = new ArrayList<>();
            source.readList(childCallIds, classLoader);
            StatusHints statusHints = (StatusHints) source.readParcelable(classLoader);
            int videoState = source.readInt();
            List<String> conferenceableCallIds = new ArrayList<>();
            source.readList(conferenceableCallIds, classLoader);
            Bundle intentExtras = source.readBundle(classLoader);
            Bundle extras = source.readBundle(classLoader);
            return new ParcelableCall(id, state, disconnectCause, cannedSmsResponses, capabilities, properties, source.readInt(), connectTimeMillis, handle, handlePresentation, callerDisplayName, callerDisplayNamePresentation, gatewayInfo, accountHandle, isVideoCallProviderChanged, videoCallProvider, source.readByte() == 1, (ParcelableRttCall) source.readParcelable(classLoader), parentCallId, childCallIds, statusHints, videoState, conferenceableCallIds, intentExtras, extras, source.readLong(), source.readInt(), source.readInt(), source.readInt(), source.readInt() == 1);
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableCall[] newArray(int size) {
            return new ParcelableCall[size];
        }
    };
    private final PhoneAccountHandle mAccountHandle;
    private final int mCallDirection;
    private final String mCallerDisplayName;
    private final int mCallerDisplayNamePresentation;
    private final List<String> mCannedSmsResponses;
    private final int mCapabilities;
    private final List<String> mChildCallIds;
    private int mCode;
    private final List<String> mConferenceableCallIds;
    private final long mConnectTimeMillis;
    private final long mCreationTimeMillis;
    private final DisconnectCause mDisconnectCause;
    private final Bundle mExtras;
    private final GatewayInfo mGatewayInfo;
    private final Uri mHandle;
    private final int mHandlePresentation;
    private final String mId;
    private final Bundle mIntentExtras;
    boolean mIsActiveSub;
    private final boolean mIsRttCallChanged;
    private final boolean mIsVideoCallProviderChanged;
    private int mNotificationType;
    private final String mParentCallId;
    private final int mProperties;
    private final ParcelableRttCall mRttCall;
    private final int mState;
    private final StatusHints mStatusHints;
    private final int mSupportedAudioRoutes;
    private VideoCallImpl mVideoCall;
    private final IVideoProvider mVideoCallProvider;
    private final int mVideoState;

    public ParcelableCall(String id, int state, DisconnectCause disconnectCause, List<String> cannedSmsResponses, int capabilities, int properties, int supportedAudioRoutes, long connectTimeMillis, Uri handle, int handlePresentation, String callerDisplayName, int callerDisplayNamePresentation, GatewayInfo gatewayInfo, PhoneAccountHandle accountHandle, boolean isVideoCallProviderChanged, IVideoProvider videoCallProvider, boolean isRttCallChanged, ParcelableRttCall rttCall, String parentCallId, List<String> childCallIds, StatusHints statusHints, int videoState, List<String> conferenceableCallIds, Bundle intentExtras, Bundle extras, long creationTimeMillis, int callDirection) {
        this(id, state, disconnectCause, cannedSmsResponses, capabilities, properties, supportedAudioRoutes, connectTimeMillis, handle, handlePresentation, callerDisplayName, callerDisplayNamePresentation, gatewayInfo, accountHandle, isVideoCallProviderChanged, videoCallProvider, isRttCallChanged, rttCall, parentCallId, childCallIds, statusHints, videoState, conferenceableCallIds, intentExtras, extras, creationTimeMillis, callDirection, 0, 0, false);
    }

    public ParcelableCall(String id, int state, DisconnectCause disconnectCause, List<String> cannedSmsResponses, int capabilities, int properties, int supportedAudioRoutes, long connectTimeMillis, Uri handle, int handlePresentation, String callerDisplayName, int callerDisplayNamePresentation, GatewayInfo gatewayInfo, PhoneAccountHandle accountHandle, boolean isVideoCallProviderChanged, IVideoProvider videoCallProvider, boolean isRttCallChanged, ParcelableRttCall rttCall, String parentCallId, List<String> childCallIds, StatusHints statusHints, int videoState, List<String> conferenceableCallIds, Bundle intentExtras, Bundle extras, long creationTimeMillis, int callDirection, int notificationType, int code, boolean isActiveSub) {
        this.mId = id;
        this.mState = state;
        this.mDisconnectCause = disconnectCause;
        this.mCannedSmsResponses = cannedSmsResponses;
        this.mCapabilities = capabilities;
        this.mProperties = properties;
        this.mSupportedAudioRoutes = supportedAudioRoutes;
        this.mConnectTimeMillis = connectTimeMillis;
        this.mHandle = handle;
        this.mHandlePresentation = handlePresentation;
        this.mCallerDisplayName = callerDisplayName;
        this.mCallerDisplayNamePresentation = callerDisplayNamePresentation;
        this.mGatewayInfo = gatewayInfo;
        this.mAccountHandle = accountHandle;
        this.mIsVideoCallProviderChanged = isVideoCallProviderChanged;
        this.mVideoCallProvider = videoCallProvider;
        this.mIsRttCallChanged = isRttCallChanged;
        this.mRttCall = rttCall;
        this.mParentCallId = parentCallId;
        this.mChildCallIds = childCallIds;
        this.mStatusHints = statusHints;
        this.mVideoState = videoState;
        this.mConferenceableCallIds = Collections.unmodifiableList(conferenceableCallIds);
        this.mIntentExtras = intentExtras;
        this.mExtras = extras;
        this.mCreationTimeMillis = creationTimeMillis;
        this.mCallDirection = callDirection;
        this.mNotificationType = notificationType;
        this.mCode = code;
        this.mIsActiveSub = isActiveSub;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public String getId() {
        return this.mId;
    }

    public int getState() {
        return this.mState;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public DisconnectCause getDisconnectCause() {
        return this.mDisconnectCause;
    }

    public List<String> getCannedSmsResponses() {
        return this.mCannedSmsResponses;
    }

    public int getCapabilities() {
        return this.mCapabilities;
    }

    public int getProperties() {
        return this.mProperties;
    }

    public int getSupportedAudioRoutes() {
        return this.mSupportedAudioRoutes;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public long getConnectTimeMillis() {
        return this.mConnectTimeMillis;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public Uri getHandle() {
        return this.mHandle;
    }

    public int getHandlePresentation() {
        return this.mHandlePresentation;
    }

    public String getCallerDisplayName() {
        return this.mCallerDisplayName;
    }

    public int getCallerDisplayNamePresentation() {
        return this.mCallerDisplayNamePresentation;
    }

    public GatewayInfo getGatewayInfo() {
        return this.mGatewayInfo;
    }

    public PhoneAccountHandle getAccountHandle() {
        return this.mAccountHandle;
    }

    public VideoCallImpl getVideoCallImpl(String callingPackageName, int targetSdkVersion) {
        IVideoProvider iVideoProvider;
        if (this.mVideoCall == null && (iVideoProvider = this.mVideoCallProvider) != null) {
            try {
                this.mVideoCall = new VideoCallImpl(iVideoProvider, callingPackageName, targetSdkVersion);
            } catch (RemoteException e) {
            }
        }
        return this.mVideoCall;
    }

    public boolean getIsRttCallChanged() {
        return this.mIsRttCallChanged;
    }

    public ParcelableRttCall getParcelableRttCall() {
        return this.mRttCall;
    }

    public String getParentCallId() {
        return this.mParentCallId;
    }

    public List<String> getChildCallIds() {
        return this.mChildCallIds;
    }

    public List<String> getConferenceableCallIds() {
        return this.mConferenceableCallIds;
    }

    public StatusHints getStatusHints() {
        return this.mStatusHints;
    }

    public int getVideoState() {
        return this.mVideoState;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public Bundle getIntentExtras() {
        return this.mIntentExtras;
    }

    public boolean isVideoCallProviderChanged() {
        return this.mIsVideoCallProviderChanged;
    }

    public int getNotificationType() {
        return this.mNotificationType;
    }

    public int getNotificationCode() {
        return this.mCode;
    }

    public long getCreationTimeMillis() {
        return this.mCreationTimeMillis;
    }

    public int getCallDirection() {
        return this.mCallDirection;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeString(this.mId);
        destination.writeInt(this.mState);
        destination.writeParcelable(this.mDisconnectCause, 0);
        destination.writeList(this.mCannedSmsResponses);
        destination.writeInt(this.mCapabilities);
        destination.writeInt(this.mProperties);
        destination.writeLong(this.mConnectTimeMillis);
        destination.writeParcelable(this.mHandle, 0);
        destination.writeInt(this.mHandlePresentation);
        destination.writeString(this.mCallerDisplayName);
        destination.writeInt(this.mCallerDisplayNamePresentation);
        destination.writeParcelable(this.mGatewayInfo, 0);
        destination.writeParcelable(this.mAccountHandle, 0);
        destination.writeByte(this.mIsVideoCallProviderChanged ? (byte) 1 : 0);
        IVideoProvider iVideoProvider = this.mVideoCallProvider;
        destination.writeStrongBinder(iVideoProvider != null ? iVideoProvider.asBinder() : null);
        destination.writeString(this.mParentCallId);
        destination.writeList(this.mChildCallIds);
        destination.writeParcelable(this.mStatusHints, 0);
        destination.writeInt(this.mVideoState);
        destination.writeList(this.mConferenceableCallIds);
        destination.writeBundle(this.mIntentExtras);
        destination.writeBundle(this.mExtras);
        destination.writeInt(this.mSupportedAudioRoutes);
        destination.writeInt(this.mNotificationType);
        destination.writeInt(this.mCode);
        destination.writeInt(this.mIsActiveSub ? 1 : 0);
        destination.writeByte(this.mIsRttCallChanged ? (byte) 1 : 0);
        destination.writeParcelable(this.mRttCall, 0);
        destination.writeLong(this.mCreationTimeMillis);
        destination.writeInt(this.mCallDirection);
    }

    public String toString() {
        return String.format("[%s, parent:%s, children:%s]", this.mId, this.mParentCallId, this.mChildCallIds);
    }
}
