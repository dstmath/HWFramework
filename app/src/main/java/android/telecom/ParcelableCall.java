package android.telecom;

import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import com.android.internal.telecom.IVideoProvider;
import java.util.Collections;
import java.util.List;

public final class ParcelableCall implements Parcelable {
    public static final Creator<ParcelableCall> CREATOR = null;
    private final PhoneAccountHandle mAccountHandle;
    private final String mCallerDisplayName;
    private final int mCallerDisplayNamePresentation;
    private final List<String> mCannedSmsResponses;
    private final int mCapabilities;
    private final List<String> mChildCallIds;
    private int mCode;
    private final List<String> mConferenceableCallIds;
    private final long mConnectTimeMillis;
    private final DisconnectCause mDisconnectCause;
    private final Bundle mExtras;
    private final GatewayInfo mGatewayInfo;
    private final Uri mHandle;
    private final int mHandlePresentation;
    private final String mId;
    private final Bundle mIntentExtras;
    boolean mIsActiveSub;
    private final boolean mIsVideoCallProviderChanged;
    private int mNotificationType;
    private final String mParentCallId;
    private final int mProperties;
    private final int mState;
    private final StatusHints mStatusHints;
    private VideoCallImpl mVideoCall;
    private final IVideoProvider mVideoCallProvider;
    private final int mVideoState;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.ParcelableCall.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telecom.ParcelableCall.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.ParcelableCall.<clinit>():void");
    }

    public ParcelableCall(String id, int state, DisconnectCause disconnectCause, List<String> cannedSmsResponses, int capabilities, int properties, long connectTimeMillis, Uri handle, int handlePresentation, String callerDisplayName, int callerDisplayNamePresentation, GatewayInfo gatewayInfo, PhoneAccountHandle accountHandle, boolean isVideoCallProviderChanged, IVideoProvider videoCallProvider, String parentCallId, List<String> childCallIds, StatusHints statusHints, int videoState, List<String> conferenceableCallIds, Bundle intentExtras, Bundle extras) {
        this(id, state, disconnectCause, cannedSmsResponses, capabilities, properties, connectTimeMillis, handle, handlePresentation, callerDisplayName, callerDisplayNamePresentation, gatewayInfo, accountHandle, isVideoCallProviderChanged, videoCallProvider, parentCallId, childCallIds, statusHints, videoState, conferenceableCallIds, intentExtras, extras, 0, 0, false);
    }

    public ParcelableCall(String id, int state, DisconnectCause disconnectCause, List<String> cannedSmsResponses, int capabilities, int properties, long connectTimeMillis, Uri handle, int handlePresentation, String callerDisplayName, int callerDisplayNamePresentation, GatewayInfo gatewayInfo, PhoneAccountHandle accountHandle, boolean isVideoCallProviderChanged, IVideoProvider videoCallProvider, String parentCallId, List<String> childCallIds, StatusHints statusHints, int videoState, List<String> conferenceableCallIds, Bundle intentExtras, Bundle extras, int notificationType, int code, boolean isActiveSub) {
        this.mId = id;
        this.mState = state;
        this.mDisconnectCause = disconnectCause;
        this.mCannedSmsResponses = cannedSmsResponses;
        this.mCapabilities = capabilities;
        this.mProperties = properties;
        this.mConnectTimeMillis = connectTimeMillis;
        this.mHandle = handle;
        this.mHandlePresentation = handlePresentation;
        this.mCallerDisplayName = callerDisplayName;
        this.mCallerDisplayNamePresentation = callerDisplayNamePresentation;
        this.mGatewayInfo = gatewayInfo;
        this.mAccountHandle = accountHandle;
        this.mIsVideoCallProviderChanged = isVideoCallProviderChanged;
        this.mVideoCallProvider = videoCallProvider;
        this.mParentCallId = parentCallId;
        this.mChildCallIds = childCallIds;
        this.mStatusHints = statusHints;
        this.mVideoState = videoState;
        this.mConferenceableCallIds = Collections.unmodifiableList(conferenceableCallIds);
        this.mIntentExtras = intentExtras;
        this.mExtras = extras;
        this.mNotificationType = notificationType;
        this.mCode = code;
        this.mIsActiveSub = isActiveSub;
    }

    public String getId() {
        return this.mId;
    }

    public int getState() {
        return this.mState;
    }

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

    public long getConnectTimeMillis() {
        return this.mConnectTimeMillis;
    }

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

    public VideoCallImpl getVideoCallImpl() {
        if (this.mVideoCall == null && this.mVideoCallProvider != null) {
            try {
                this.mVideoCall = new VideoCallImpl(this.mVideoCallProvider);
            } catch (RemoteException e) {
            }
        }
        return this.mVideoCall;
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel destination, int flags) {
        IBinder asBinder;
        int i = 1;
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
        destination.writeByte((byte) (this.mIsVideoCallProviderChanged ? 1 : 0));
        if (this.mVideoCallProvider != null) {
            asBinder = this.mVideoCallProvider.asBinder();
        } else {
            asBinder = null;
        }
        destination.writeStrongBinder(asBinder);
        destination.writeString(this.mParentCallId);
        destination.writeList(this.mChildCallIds);
        destination.writeParcelable(this.mStatusHints, 0);
        destination.writeInt(this.mVideoState);
        destination.writeList(this.mConferenceableCallIds);
        destination.writeBundle(this.mIntentExtras);
        destination.writeBundle(this.mExtras);
        destination.writeInt(this.mNotificationType);
        destination.writeInt(this.mCode);
        if (!this.mIsActiveSub) {
            i = 0;
        }
        destination.writeInt(i);
    }

    public String toString() {
        return String.format("[%s, parent:%s, children:%s]", new Object[]{this.mId, this.mParentCallId, this.mChildCallIds});
    }
}
