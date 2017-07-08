package android.telecom;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.InCallService.VideoCall;
import com.android.internal.telephony.IccCardConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Call {
    public static final String AVAILABLE_PHONE_ACCOUNTS = "selectPhoneAccountAccounts";
    public static final int STATE_ACTIVE = 4;
    public static final int STATE_CONNECTING = 9;
    public static final int STATE_DIALING = 1;
    public static final int STATE_DISCONNECTED = 7;
    public static final int STATE_DISCONNECTING = 10;
    public static final int STATE_HOLDING = 3;
    public static final int STATE_NEW = 0;
    @Deprecated
    public static final int STATE_PRE_DIAL_WAIT = 8;
    public static final int STATE_PULLING_CALL = 11;
    public static final int STATE_RINGING = 2;
    public static final int STATE_SELECT_PHONE_ACCOUNT = 8;
    private final List<CallbackRecord<Callback>> mCallbackRecords;
    private List<String> mCannedTextResponses;
    private final List<Call> mChildren;
    private boolean mChildrenCached;
    private final List<String> mChildrenIds;
    private final List<Call> mConferenceableCalls;
    private Details mDetails;
    private Bundle mExtras;
    private final InCallAdapter mInCallAdapter;
    public boolean mIsActiveSub;
    private int mNotificationCode;
    private int mNotificationType;
    private String mParentId;
    private final Phone mPhone;
    private String mRemainingPostDialSequence;
    private int mState;
    private final String mTelecomCallId;
    private final List<Call> mUnmodifiableChildren;
    private final List<Call> mUnmodifiableConferenceableCalls;
    private VideoCallImpl mVideoCallImpl;

    public static abstract class Callback {
        public void onStateChanged(Call call, int state) {
        }

        public void onParentChanged(Call call, Call parent) {
        }

        public void onChildrenChanged(Call call, List<Call> list) {
        }

        public void onDetailsChanged(Call call, Details details) {
        }

        public void onCannedTextResponsesLoaded(Call call, List<String> list) {
        }

        public void onPostDialWait(Call call, String remainingPostDialSequence) {
        }

        public void onVideoCallChanged(Call call, VideoCall videoCall) {
        }

        public void onCallDestroyed(Call call) {
        }

        public void onConferenceableCallsChanged(Call call, List<Call> list) {
        }

        public void onConnectionEvent(Call call, String event, Bundle extras) {
        }
    }

    public static class Details {
        public static final int CAPABILITY_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO = 4194304;
        public static final int CAPABILITY_CAN_PAUSE_VIDEO = 1048576;
        public static final int CAPABILITY_CAN_PULL_CALL = 8388608;
        public static final int CAPABILITY_CAN_SEND_RESPONSE_VIA_CONNECTION = 2097152;
        public static final int CAPABILITY_CAN_UPGRADE_TO_VIDEO = 524288;
        public static final int CAPABILITY_DISCONNECT_FROM_CONFERENCE = 8192;
        public static final int CAPABILITY_HOLD = 1;
        public static final int CAPABILITY_MANAGE_CONFERENCE = 128;
        public static final int CAPABILITY_MERGE_CONFERENCE = 4;
        public static final int CAPABILITY_MUTE = 64;
        public static final int CAPABILITY_RESPOND_VIA_TEXT = 32;
        public static final int CAPABILITY_SEPARATE_FROM_CONFERENCE = 4096;
        public static final int CAPABILITY_SPEED_UP_MT_AUDIO = 262144;
        public static final int CAPABILITY_SUPPORTS_VT_LOCAL_BIDIRECTIONAL = 768;
        public static final int CAPABILITY_SUPPORTS_VT_LOCAL_RX = 256;
        public static final int CAPABILITY_SUPPORTS_VT_LOCAL_TX = 512;
        public static final int CAPABILITY_SUPPORTS_VT_REMOTE_BIDIRECTIONAL = 3072;
        public static final int CAPABILITY_SUPPORTS_VT_REMOTE_RX = 1024;
        public static final int CAPABILITY_SUPPORTS_VT_REMOTE_TX = 2048;
        public static final int CAPABILITY_SUPPORT_HOLD = 2;
        public static final int CAPABILITY_SWAP_CONFERENCE = 8;
        public static final int CAPABILITY_UNUSED_1 = 16;
        public static final int CAPABILITY_VOICE_PRIVACY = 16777216;
        public static final int PROPERTY_CONFERENCE = 1;
        public static final int PROPERTY_EMERGENCY_CALLBACK_MODE = 4;
        public static final int PROPERTY_ENTERPRISE_CALL = 32;
        public static final int PROPERTY_GENERIC_CONFERENCE = 2;
        public static final int PROPERTY_HIGH_DEF_AUDIO = 16;
        public static final int PROPERTY_IS_EXTERNAL_CALL = 64;
        public static final int PROPERTY_WIFI = 8;
        private final PhoneAccountHandle mAccountHandle;
        private final int mCallCapabilities;
        private final int mCallProperties;
        private final String mCallerDisplayName;
        private final int mCallerDisplayNamePresentation;
        private final long mConnectTimeMillis;
        private final DisconnectCause mDisconnectCause;
        private final Bundle mExtras;
        private final GatewayInfo mGatewayInfo;
        private final Uri mHandle;
        private final int mHandlePresentation;
        private final Bundle mIntentExtras;
        private final StatusHints mStatusHints;
        private final String mTelecomCallId;
        private final int mVideoState;

        public static boolean can(int capabilities, int capability) {
            return (capabilities & capability) == capability;
        }

        public boolean can(int capability) {
            return can(this.mCallCapabilities, capability);
        }

        public static String capabilitiesToString(int capabilities) {
            StringBuilder builder = new StringBuilder();
            builder.append("[Capabilities:");
            if (can(capabilities, PROPERTY_CONFERENCE)) {
                builder.append(" CAPABILITY_HOLD");
            }
            if (can(capabilities, PROPERTY_GENERIC_CONFERENCE)) {
                builder.append(" CAPABILITY_SUPPORT_HOLD");
            }
            if (can(capabilities, PROPERTY_EMERGENCY_CALLBACK_MODE)) {
                builder.append(" CAPABILITY_MERGE_CONFERENCE");
            }
            if (can(capabilities, PROPERTY_WIFI)) {
                builder.append(" CAPABILITY_SWAP_CONFERENCE");
            }
            if (can(capabilities, PROPERTY_ENTERPRISE_CALL)) {
                builder.append(" CAPABILITY_RESPOND_VIA_TEXT");
            }
            if (can(capabilities, PROPERTY_IS_EXTERNAL_CALL)) {
                builder.append(" CAPABILITY_MUTE");
            }
            if (can(capabilities, CAPABILITY_MANAGE_CONFERENCE)) {
                builder.append(" CAPABILITY_MANAGE_CONFERENCE");
            }
            if (can(capabilities, CAPABILITY_SUPPORTS_VT_LOCAL_RX)) {
                builder.append(" CAPABILITY_SUPPORTS_VT_LOCAL_RX");
            }
            if (can(capabilities, CAPABILITY_SUPPORTS_VT_LOCAL_TX)) {
                builder.append(" CAPABILITY_SUPPORTS_VT_LOCAL_TX");
            }
            if (can(capabilities, CAPABILITY_SUPPORTS_VT_LOCAL_BIDIRECTIONAL)) {
                builder.append(" CAPABILITY_SUPPORTS_VT_LOCAL_BIDIRECTIONAL");
            }
            if (can(capabilities, CAPABILITY_SUPPORTS_VT_REMOTE_RX)) {
                builder.append(" CAPABILITY_SUPPORTS_VT_REMOTE_RX");
            }
            if (can(capabilities, CAPABILITY_SUPPORTS_VT_REMOTE_TX)) {
                builder.append(" CAPABILITY_SUPPORTS_VT_REMOTE_TX");
            }
            if (can(capabilities, CAPABILITY_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO)) {
                builder.append(" CAPABILITY_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO");
            }
            if (can(capabilities, CAPABILITY_SUPPORTS_VT_REMOTE_BIDIRECTIONAL)) {
                builder.append(" CAPABILITY_SUPPORTS_VT_REMOTE_BIDIRECTIONAL");
            }
            if (can(capabilities, CAPABILITY_SPEED_UP_MT_AUDIO)) {
                builder.append(" CAPABILITY_SPEED_UP_MT_AUDIO");
            }
            if (can(capabilities, CAPABILITY_CAN_UPGRADE_TO_VIDEO)) {
                builder.append(" CAPABILITY_CAN_UPGRADE_TO_VIDEO");
            }
            if (can(capabilities, CAPABILITY_CAN_PAUSE_VIDEO)) {
                builder.append(" CAPABILITY_CAN_PAUSE_VIDEO");
            }
            if (can(capabilities, CAPABILITY_CAN_PULL_CALL)) {
                builder.append(" CAPABILITY_CAN_PULL_CALL");
            }
            if (can(capabilities, CAPABILITY_VOICE_PRIVACY)) {
                builder.append(" CAPABILITY_VOICE_PRIVACY");
            }
            builder.append("]");
            return builder.toString();
        }

        public static boolean hasProperty(int properties, int property) {
            return (properties & property) == property;
        }

        public boolean hasProperty(int property) {
            return hasProperty(this.mCallProperties, property);
        }

        public static String propertiesToString(int properties) {
            StringBuilder builder = new StringBuilder();
            builder.append("[Properties:");
            if (hasProperty(properties, PROPERTY_CONFERENCE)) {
                builder.append(" PROPERTY_CONFERENCE");
            }
            if (hasProperty(properties, PROPERTY_GENERIC_CONFERENCE)) {
                builder.append(" PROPERTY_GENERIC_CONFERENCE");
            }
            if (hasProperty(properties, PROPERTY_WIFI)) {
                builder.append(" PROPERTY_WIFI");
            }
            if (hasProperty(properties, PROPERTY_HIGH_DEF_AUDIO)) {
                builder.append(" PROPERTY_HIGH_DEF_AUDIO");
            }
            if (hasProperty(properties, PROPERTY_EMERGENCY_CALLBACK_MODE)) {
                builder.append(" PROPERTY_EMERGENCY_CALLBACK_MODE");
            }
            if (hasProperty(properties, PROPERTY_IS_EXTERNAL_CALL)) {
                builder.append(" PROPERTY_IS_EXTERNAL_CALL");
            }
            builder.append("]");
            return builder.toString();
        }

        public String getTelecomCallId() {
            return this.mTelecomCallId;
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

        public PhoneAccountHandle getAccountHandle() {
            return this.mAccountHandle;
        }

        public int getCallCapabilities() {
            return this.mCallCapabilities;
        }

        public int getCallProperties() {
            return this.mCallProperties;
        }

        public DisconnectCause getDisconnectCause() {
            return this.mDisconnectCause;
        }

        public final long getConnectTimeMillis() {
            return this.mConnectTimeMillis;
        }

        public GatewayInfo getGatewayInfo() {
            return this.mGatewayInfo;
        }

        public int getVideoState() {
            return this.mVideoState;
        }

        public StatusHints getStatusHints() {
            return this.mStatusHints;
        }

        public Bundle getExtras() {
            return this.mExtras;
        }

        public Bundle getIntentExtras() {
            return this.mIntentExtras;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Details)) {
                return false;
            }
            Details d = (Details) o;
            if (Objects.equals(this.mHandle, d.mHandle) && Objects.equals(Integer.valueOf(this.mHandlePresentation), Integer.valueOf(d.mHandlePresentation)) && Objects.equals(this.mCallerDisplayName, d.mCallerDisplayName) && Objects.equals(Integer.valueOf(this.mCallerDisplayNamePresentation), Integer.valueOf(d.mCallerDisplayNamePresentation)) && Objects.equals(this.mAccountHandle, d.mAccountHandle) && Objects.equals(Integer.valueOf(this.mCallCapabilities), Integer.valueOf(d.mCallCapabilities)) && Objects.equals(Integer.valueOf(this.mCallProperties), Integer.valueOf(d.mCallProperties)) && Objects.equals(this.mDisconnectCause, d.mDisconnectCause) && Objects.equals(Long.valueOf(this.mConnectTimeMillis), Long.valueOf(d.mConnectTimeMillis)) && Objects.equals(this.mGatewayInfo, d.mGatewayInfo) && Objects.equals(Integer.valueOf(this.mVideoState), Integer.valueOf(d.mVideoState)) && Objects.equals(this.mStatusHints, d.mStatusHints) && Call.areBundlesEqual(this.mExtras, d.mExtras)) {
                z = Call.areBundlesEqual(this.mIntentExtras, d.mIntentExtras);
            }
            return z;
        }

        public int hashCode() {
            return ((((((((((((Objects.hashCode(this.mHandle) + Objects.hashCode(Integer.valueOf(this.mHandlePresentation))) + Objects.hashCode(this.mCallerDisplayName)) + Objects.hashCode(Integer.valueOf(this.mCallerDisplayNamePresentation))) + Objects.hashCode(this.mAccountHandle)) + Objects.hashCode(Integer.valueOf(this.mCallCapabilities))) + Objects.hashCode(Integer.valueOf(this.mCallProperties))) + Objects.hashCode(this.mDisconnectCause)) + Objects.hashCode(Long.valueOf(this.mConnectTimeMillis))) + Objects.hashCode(this.mGatewayInfo)) + Objects.hashCode(Integer.valueOf(this.mVideoState))) + Objects.hashCode(this.mStatusHints)) + Objects.hashCode(this.mExtras)) + Objects.hashCode(this.mIntentExtras);
        }

        public Details(String telecomCallId, Uri handle, int handlePresentation, String callerDisplayName, int callerDisplayNamePresentation, PhoneAccountHandle accountHandle, int capabilities, int properties, DisconnectCause disconnectCause, long connectTimeMillis, GatewayInfo gatewayInfo, int videoState, StatusHints statusHints, Bundle extras, Bundle intentExtras) {
            this.mTelecomCallId = telecomCallId;
            this.mHandle = handle;
            this.mHandlePresentation = handlePresentation;
            this.mCallerDisplayName = callerDisplayName;
            this.mCallerDisplayNamePresentation = callerDisplayNamePresentation;
            this.mAccountHandle = accountHandle;
            this.mCallCapabilities = capabilities;
            this.mCallProperties = properties;
            this.mDisconnectCause = disconnectCause;
            this.mConnectTimeMillis = connectTimeMillis;
            this.mGatewayInfo = gatewayInfo;
            this.mVideoState = videoState;
            this.mStatusHints = statusHints;
            this.mExtras = extras;
            this.mIntentExtras = intentExtras;
        }

        public static Details createFromParcelableCall(ParcelableCall parcelableCall) {
            return new Details(parcelableCall.getId(), parcelableCall.getHandle(), parcelableCall.getHandlePresentation(), parcelableCall.getCallerDisplayName(), parcelableCall.getCallerDisplayNamePresentation(), parcelableCall.getAccountHandle(), parcelableCall.getCapabilities(), parcelableCall.getProperties(), parcelableCall.getDisconnectCause(), parcelableCall.getConnectTimeMillis(), parcelableCall.getGatewayInfo(), parcelableCall.getVideoState(), parcelableCall.getStatusHints(), parcelableCall.getExtras(), parcelableCall.getIntentExtras());
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[pa: ");
            sb.append(this.mAccountHandle);
            sb.append(", hdl: ");
            sb.append(Connection.toLogSafePhoneNumber(Log.pii(this.mHandle)));
            sb.append(", caps: ");
            sb.append(capabilitiesToString(this.mCallCapabilities));
            sb.append(", props: ");
            sb.append(propertiesToString(this.mCallProperties));
            sb.append("]");
            return sb.toString();
        }
    }

    @Deprecated
    public static abstract class Listener extends Callback {
    }

    public String getRemainingPostDialSequence() {
        return this.mRemainingPostDialSequence;
    }

    public int getNotificationType() {
        return this.mNotificationType;
    }

    public int getNotificationCode() {
        return this.mNotificationCode;
    }

    public void answer(int videoState) {
        this.mInCallAdapter.answerCall(this.mTelecomCallId, videoState);
    }

    public boolean updateRcsPreCallInfo(Bundle extras) {
        return this.mInCallAdapter.updateRcsPreCallInfo(this.mTelecomCallId, extras);
    }

    public void reject(boolean rejectWithMessage, String textMessage) {
        this.mInCallAdapter.rejectCall(this.mTelecomCallId, rejectWithMessage, textMessage);
    }

    public void disconnect() {
        this.mInCallAdapter.disconnectCall(this.mTelecomCallId);
    }

    public void hold() {
        this.mInCallAdapter.holdCall(this.mTelecomCallId);
    }

    public void unhold() {
        this.mInCallAdapter.unholdCall(this.mTelecomCallId);
    }

    public void playDtmfTone(char digit) {
        this.mInCallAdapter.playDtmfTone(this.mTelecomCallId, digit);
    }

    public void stopDtmfTone() {
        this.mInCallAdapter.stopDtmfTone(this.mTelecomCallId);
    }

    public void postDialContinue(boolean proceed) {
        this.mInCallAdapter.postDialContinue(this.mTelecomCallId, proceed);
    }

    public void phoneAccountSelected(PhoneAccountHandle accountHandle, boolean setDefault) {
        this.mInCallAdapter.phoneAccountSelected(this.mTelecomCallId, accountHandle, setDefault);
    }

    public void conference(Call callToConferenceWith) {
        if (callToConferenceWith != null) {
            this.mInCallAdapter.conference(this.mTelecomCallId, callToConferenceWith.mTelecomCallId);
        }
    }

    public void splitFromConference() {
        this.mInCallAdapter.splitFromConference(this.mTelecomCallId);
    }

    public void mergeConference() {
        this.mInCallAdapter.mergeConference(this.mTelecomCallId);
    }

    public void swapConference() {
        this.mInCallAdapter.swapConference(this.mTelecomCallId);
    }

    public void pullExternalCall() {
        if (this.mDetails.hasProperty(64)) {
            this.mInCallAdapter.pullExternalCall(this.mTelecomCallId);
        }
    }

    public void sendCallEvent(String event, Bundle extras) {
        this.mInCallAdapter.sendCallEvent(this.mTelecomCallId, event, extras);
    }

    public final void putExtras(Bundle extras) {
        if (extras != null) {
            if (this.mExtras == null) {
                this.mExtras = new Bundle();
            }
            this.mExtras.putAll(extras);
            this.mInCallAdapter.putExtras(this.mTelecomCallId, extras);
        }
    }

    public final void putExtra(String key, boolean value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putBoolean(key, value);
        this.mInCallAdapter.putExtra(this.mTelecomCallId, key, value);
    }

    public final void putExtra(String key, int value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putInt(key, value);
        this.mInCallAdapter.putExtra(this.mTelecomCallId, key, value);
    }

    public final void putExtra(String key, String value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putString(key, value);
        this.mInCallAdapter.putExtra(this.mTelecomCallId, key, value);
    }

    public final void removeExtras(List<String> keys) {
        if (this.mExtras != null) {
            for (String key : keys) {
                this.mExtras.remove(key);
            }
            if (this.mExtras.size() == 0) {
                this.mExtras = null;
            }
        }
        this.mInCallAdapter.removeExtras(this.mTelecomCallId, keys);
    }

    public Call getParent() {
        if (this.mParentId != null) {
            return this.mPhone.internalGetCallByTelecomId(this.mParentId);
        }
        return null;
    }

    public List<Call> getChildren() {
        if (!this.mChildrenCached) {
            this.mChildrenCached = true;
            this.mChildren.clear();
            for (String id : this.mChildrenIds) {
                Call call = this.mPhone.internalGetCallByTelecomId(id);
                if (call == null) {
                    this.mChildrenCached = false;
                } else {
                    this.mChildren.add(call);
                }
            }
        }
        return this.mUnmodifiableChildren;
    }

    public List<Call> getConferenceableCalls() {
        return this.mUnmodifiableConferenceableCalls;
    }

    public int getState() {
        return this.mState;
    }

    public List<String> getCannedTextResponses() {
        return this.mCannedTextResponses;
    }

    public VideoCall getVideoCall() {
        return this.mVideoCallImpl;
    }

    public Details getDetails() {
        return this.mDetails;
    }

    public void registerCallback(Callback callback) {
        registerCallback(callback, new Handler());
    }

    public void registerCallback(Callback callback, Handler handler) {
        unregisterCallback(callback);
        if (callback != null && handler != null && this.mState != STATE_DISCONNECTED) {
            this.mCallbackRecords.add(new CallbackRecord(callback, handler));
        }
    }

    public void unregisterCallback(Callback callback) {
        if (callback != null && this.mState != STATE_DISCONNECTED) {
            for (CallbackRecord<Callback> record : this.mCallbackRecords) {
                if (record.getCallback() == callback) {
                    this.mCallbackRecords.remove(record);
                    return;
                }
            }
        }
    }

    public String toString() {
        return "Call [id: " + this.mTelecomCallId + ", state: " + stateToString(this.mState) + ", details: " + this.mDetails + "]";
    }

    private static String stateToString(int state) {
        switch (state) {
            case STATE_NEW /*0*/:
                return "NEW";
            case STATE_DIALING /*1*/:
                return "DIALING";
            case STATE_RINGING /*2*/:
                return "RINGING";
            case STATE_HOLDING /*3*/:
                return "HOLDING";
            case STATE_ACTIVE /*4*/:
                return "ACTIVE";
            case STATE_DISCONNECTED /*7*/:
                return "DISCONNECTED";
            case STATE_SELECT_PHONE_ACCOUNT /*8*/:
                return "SELECT_PHONE_ACCOUNT";
            case STATE_CONNECTING /*9*/:
                return "CONNECTING";
            case STATE_DISCONNECTING /*10*/:
                return "DISCONNECTING";
            default:
                Object[] objArr = new Object[STATE_DIALING];
                objArr[STATE_NEW] = Integer.valueOf(state);
                Log.w((Object) Call.class, "Unknown state %d", objArr);
                return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
    }

    @Deprecated
    public void addListener(Listener listener) {
        registerCallback(listener);
    }

    @Deprecated
    public void removeListener(Listener listener) {
        unregisterCallback(listener);
    }

    Call(Phone phone, String telecomCallId, InCallAdapter inCallAdapter) {
        this.mChildrenIds = new ArrayList();
        this.mChildren = new ArrayList();
        this.mUnmodifiableChildren = Collections.unmodifiableList(this.mChildren);
        this.mCallbackRecords = new CopyOnWriteArrayList();
        this.mConferenceableCalls = new ArrayList();
        this.mUnmodifiableConferenceableCalls = Collections.unmodifiableList(this.mConferenceableCalls);
        this.mParentId = null;
        this.mCannedTextResponses = null;
        this.mIsActiveSub = false;
        this.mPhone = phone;
        this.mTelecomCallId = telecomCallId;
        this.mInCallAdapter = inCallAdapter;
        this.mState = STATE_NEW;
    }

    Call(Phone phone, String telecomCallId, InCallAdapter inCallAdapter, int state, boolean isActiveSub) {
        this.mChildrenIds = new ArrayList();
        this.mChildren = new ArrayList();
        this.mUnmodifiableChildren = Collections.unmodifiableList(this.mChildren);
        this.mCallbackRecords = new CopyOnWriteArrayList();
        this.mConferenceableCalls = new ArrayList();
        this.mUnmodifiableConferenceableCalls = Collections.unmodifiableList(this.mConferenceableCalls);
        this.mParentId = null;
        this.mCannedTextResponses = null;
        this.mIsActiveSub = false;
        this.mPhone = phone;
        this.mTelecomCallId = telecomCallId;
        this.mInCallAdapter = inCallAdapter;
        this.mState = state;
        this.mIsActiveSub = isActiveSub;
    }

    final String internalGetCallId() {
        return this.mTelecomCallId;
    }

    final void internalUpdate(ParcelableCall parcelableCall, Map<String, Call> callIdMap) {
        boolean videoCallChanged;
        Details details = Details.createFromParcelableCall(parcelableCall);
        boolean detailsChanged = !Objects.equals(this.mDetails, details);
        if (detailsChanged) {
            this.mDetails = details;
        }
        this.mNotificationType = parcelableCall.getNotificationType();
        this.mNotificationCode = parcelableCall.getNotificationCode();
        boolean cannedTextResponsesChanged = false;
        if (!(this.mCannedTextResponses != null || parcelableCall.getCannedSmsResponses() == null || parcelableCall.getCannedSmsResponses().isEmpty())) {
            this.mCannedTextResponses = Collections.unmodifiableList(parcelableCall.getCannedSmsResponses());
            cannedTextResponsesChanged = true;
        }
        VideoCallImpl newVideoCallImpl = parcelableCall.getVideoCallImpl();
        if (parcelableCall.isVideoCallProviderChanged()) {
            videoCallChanged = !Objects.equals(this.mVideoCallImpl, newVideoCallImpl);
        } else {
            videoCallChanged = false;
        }
        if (videoCallChanged) {
            this.mVideoCallImpl = newVideoCallImpl;
        }
        if (this.mVideoCallImpl != null) {
            this.mVideoCallImpl.setVideoState(getDetails().getVideoState());
        }
        int state = parcelableCall.getState();
        int i = this.mState;
        boolean stateChanged = (r0 == state && this.mIsActiveSub == parcelableCall.mIsActiveSub) ? false : true;
        if (stateChanged) {
            this.mState = state;
            this.mIsActiveSub = parcelableCall.mIsActiveSub;
        }
        String parentId = parcelableCall.getParentCallId();
        boolean parentChanged = !Objects.equals(this.mParentId, parentId);
        if (parentChanged) {
            this.mParentId = parentId;
        }
        boolean childrenChanged = !Objects.equals(parcelableCall.getChildCallIds(), this.mChildrenIds);
        if (childrenChanged) {
            this.mChildrenIds.clear();
            this.mChildrenIds.addAll(parcelableCall.getChildCallIds());
            this.mChildrenCached = false;
        }
        List<String> conferenceableCallIds = parcelableCall.getConferenceableCallIds();
        List<Call> conferenceableCalls = new ArrayList(conferenceableCallIds.size());
        for (String otherId : conferenceableCallIds) {
            if (callIdMap.containsKey(otherId)) {
                conferenceableCalls.add((Call) callIdMap.get(otherId));
            }
        }
        if (!Objects.equals(this.mConferenceableCalls, conferenceableCalls)) {
            this.mConferenceableCalls.clear();
            this.mConferenceableCalls.addAll(conferenceableCalls);
            fireConferenceableCallsChanged();
        }
        if (stateChanged) {
            fireStateChanged(this.mState);
        }
        if (detailsChanged) {
            fireDetailsChanged(this.mDetails);
        }
        if (cannedTextResponsesChanged) {
            fireCannedTextResponsesLoaded(this.mCannedTextResponses);
        }
        if (videoCallChanged) {
            fireVideoCallChanged(this.mVideoCallImpl);
        }
        if (parentChanged) {
            fireParentChanged(getParent());
        }
        if (childrenChanged) {
            fireChildrenChanged(getChildren());
        }
        i = this.mState;
        if (r0 == STATE_DISCONNECTED) {
            fireCallDestroyed();
        }
    }

    final void internalSetPostDialWait(String remaining) {
        this.mRemainingPostDialSequence = remaining;
        firePostDialWait(this.mRemainingPostDialSequence);
    }

    final void internalSetDisconnected() {
        if (this.mState != STATE_DISCONNECTED) {
            this.mState = STATE_DISCONNECTED;
            fireStateChanged(this.mState);
            fireCallDestroyed();
        }
    }

    final void internalOnConnectionEvent(String event, Bundle extras) {
        fireOnConnectionEvent(event, extras);
    }

    private void fireStateChanged(int newState) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            Call call = this;
            record.getHandler().post(new 1(this, (Callback) record.getCallback(), this, newState));
        }
    }

    private void fireParentChanged(Call newParent) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            Call call = this;
            record.getHandler().post(new 2(this, (Callback) record.getCallback(), this, newParent));
        }
    }

    private void fireChildrenChanged(List<Call> children) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            Call call = this;
            record.getHandler().post(new 3(this, (Callback) record.getCallback(), this, children));
        }
    }

    private void fireDetailsChanged(Details details) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            Call call = this;
            record.getHandler().post(new 4(this, (Callback) record.getCallback(), this, details));
        }
    }

    private void fireCannedTextResponsesLoaded(List<String> cannedTextResponses) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            Call call = this;
            record.getHandler().post(new 5(this, (Callback) record.getCallback(), this, cannedTextResponses));
        }
    }

    private void fireVideoCallChanged(VideoCall videoCall) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            Call call = this;
            record.getHandler().post(new 6(this, (Callback) record.getCallback(), this, videoCall));
        }
    }

    private void firePostDialWait(String remainingPostDialSequence) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            Call call = this;
            record.getHandler().post(new 7(this, (Callback) record.getCallback(), this, remainingPostDialSequence));
        }
    }

    private void fireCallDestroyed() {
        Call call = this;
        if (this.mCallbackRecords.isEmpty()) {
            this.mPhone.internalRemoveCall(this);
        }
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            record.getHandler().post(new 8(this, (Callback) record.getCallback(), this, record));
        }
    }

    private void fireConferenceableCallsChanged() {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            Call call = this;
            record.getHandler().post(new 9(this, (Callback) record.getCallback(), this));
        }
    }

    private void fireOnConnectionEvent(String event, Bundle extras) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            Call call = this;
            record.getHandler().post(new 10(this, (Callback) record.getCallback(), this, event, extras));
        }
    }

    private static boolean areBundlesEqual(Bundle bundle, Bundle newBundle) {
        boolean z = true;
        if (bundle == null || newBundle == null) {
            if (bundle != newBundle) {
                z = false;
            }
            return z;
        } else if (bundle.size() != newBundle.size()) {
            return false;
        } else {
            for (String key : bundle.keySet()) {
                if (key != null && !Objects.equals(bundle.get(key), newBundle.get(key))) {
                    return false;
                }
            }
            return true;
        }
    }
}
