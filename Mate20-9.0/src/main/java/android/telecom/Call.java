package android.telecom;

import android.annotation.SystemApi;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.telecom.Call;
import android.telecom.InCallService;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Call {
    public static final String AVAILABLE_PHONE_ACCOUNTS = "selectPhoneAccountAccounts";
    public static final String EVENT_HANDOVER_COMPLETE = "android.telecom.event.HANDOVER_COMPLETE";
    public static final String EVENT_HANDOVER_FAILED = "android.telecom.event.HANDOVER_FAILED";
    public static final String EVENT_HANDOVER_SOURCE_DISCONNECTED = "android.telecom.event.HANDOVER_SOURCE_DISCONNECTED";
    public static final String EVENT_REQUEST_HANDOVER = "android.telecom.event.REQUEST_HANDOVER";
    public static final String EXTRA_HANDOVER_EXTRAS = "android.telecom.extra.HANDOVER_EXTRAS";
    public static final String EXTRA_HANDOVER_PHONE_ACCOUNT_HANDLE = "android.telecom.extra.HANDOVER_PHONE_ACCOUNT_HANDLE";
    public static final String EXTRA_HANDOVER_VIDEO_STATE = "android.telecom.extra.HANDOVER_VIDEO_STATE";
    public static final String EXTRA_LAST_EMERGENCY_CALLBACK_TIME_MILLIS = "android.telecom.extra.LAST_EMERGENCY_CALLBACK_TIME_MILLIS";
    public static final int STATE_ACTIVE = 4;
    public static final int STATE_CONNECTING = 9;
    public static final int STATE_DIALING = 1;
    public static final int STATE_DISCONNECTED = 7;
    public static final int STATE_DISCONNECTING = 10;
    public static final int STATE_HOLDING = 3;
    public static final int STATE_NEW = 0;
    @SystemApi
    @Deprecated
    public static final int STATE_PRE_DIAL_WAIT = 8;
    public static final int STATE_PULLING_CALL = 11;
    public static final int STATE_RINGING = 2;
    public static final int STATE_SELECT_PHONE_ACCOUNT = 8;
    /* access modifiers changed from: private */
    public final List<CallbackRecord<Callback>> mCallbackRecords = new CopyOnWriteArrayList();
    private String mCallingPackage;
    private List<String> mCannedTextResponses = null;
    private final List<Call> mChildren = new ArrayList();
    private boolean mChildrenCached;
    private final List<String> mChildrenIds = new ArrayList();
    private final List<Call> mConferenceableCalls = new ArrayList();
    private Details mDetails;
    private Bundle mExtras;
    private final InCallAdapter mInCallAdapter;
    public boolean mIsActiveSub = false;
    private int mNotificationCode;
    private int mNotificationType;
    private String mParentId = null;
    /* access modifiers changed from: private */
    public final Phone mPhone;
    private String mRemainingPostDialSequence;
    private RttCall mRttCall;
    private int mState;
    private int mTargetSdkVersion;
    private final String mTelecomCallId;
    private final List<Call> mUnmodifiableChildren = Collections.unmodifiableList(this.mChildren);
    /* access modifiers changed from: private */
    public final List<Call> mUnmodifiableConferenceableCalls = Collections.unmodifiableList(this.mConferenceableCalls);
    private VideoCallImpl mVideoCallImpl;

    public static abstract class Callback {
        public static final int HANDOVER_FAILURE_DEST_APP_REJECTED = 1;
        public static final int HANDOVER_FAILURE_NOT_SUPPORTED = 2;
        public static final int HANDOVER_FAILURE_ONGOING_EMERGENCY_CALL = 4;
        public static final int HANDOVER_FAILURE_UNKNOWN = 5;
        public static final int HANDOVER_FAILURE_USER_REJECTED = 3;

        @Retention(RetentionPolicy.SOURCE)
        public @interface HandoverFailureErrors {
        }

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

        public void onVideoCallChanged(Call call, InCallService.VideoCall videoCall) {
        }

        public void onCallDestroyed(Call call) {
        }

        public void onConferenceableCallsChanged(Call call, List<Call> list) {
        }

        public void onConnectionEvent(Call call, String event, Bundle extras) {
        }

        public void onRttModeChanged(Call call, int mode) {
        }

        public void onRttStatusChanged(Call call, boolean enabled, RttCall rttCall) {
        }

        public void onRttRequest(Call call, int id) {
        }

        public void onRttInitiationFailure(Call call, int reason) {
        }

        public void onHandoverComplete(Call call) {
        }

        public void onHandoverFailed(Call call, int failureReason) {
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
        public static final int CAPABILITY_SUPPORT_DEFLECT = 16777216;
        public static final int CAPABILITY_SUPPORT_HOLD = 2;
        public static final int CAPABILITY_SWAP_CONFERENCE = 8;
        public static final int CAPABILITY_UNUSED_1 = 16;
        public static final int CAPABILITY_VOICE_PRIVACY = 16777216;
        public static final int PROPERTY_ASSISTED_DIALING_USED = 512;
        public static final int PROPERTY_CONFERENCE = 1;
        public static final int PROPERTY_EMERGENCY_CALLBACK_MODE = 4;
        public static final int PROPERTY_ENTERPRISE_CALL = 32;
        public static final int PROPERTY_GENERIC_CONFERENCE = 2;
        public static final int PROPERTY_HAS_CDMA_VOICE_PRIVACY = 128;
        public static final int PROPERTY_HIGH_DEF_AUDIO = 16;
        public static final int PROPERTY_IS_EXTERNAL_CALL = 64;
        public static final int PROPERTY_RTT = 1024;
        public static final int PROPERTY_SELF_MANAGED = 256;
        public static final int PROPERTY_WIFI = 8;
        private final PhoneAccountHandle mAccountHandle;
        private final int mCallCapabilities;
        private final int mCallProperties;
        private final String mCallerDisplayName;
        private final int mCallerDisplayNamePresentation;
        private final long mConnectTimeMillis;
        private final long mCreationTimeMillis;
        private final DisconnectCause mDisconnectCause;
        private final Bundle mExtras;
        private final GatewayInfo mGatewayInfo;
        private final Uri mHandle;
        private final int mHandlePresentation;
        private final Bundle mIntentExtras;
        private final StatusHints mStatusHints;
        private final int mSupportedAudioRoutes = 15;
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
            if (can(capabilities, 1)) {
                builder.append(" CAPABILITY_HOLD");
            }
            if (can(capabilities, 2)) {
                builder.append(" CAPABILITY_SUPPORT_HOLD");
            }
            if (can(capabilities, 4)) {
                builder.append(" CAPABILITY_MERGE_CONFERENCE");
            }
            if (can(capabilities, 8)) {
                builder.append(" CAPABILITY_SWAP_CONFERENCE");
            }
            if (can(capabilities, 32)) {
                builder.append(" CAPABILITY_RESPOND_VIA_TEXT");
            }
            if (can(capabilities, 64)) {
                builder.append(" CAPABILITY_MUTE");
            }
            if (can(capabilities, 128)) {
                builder.append(" CAPABILITY_MANAGE_CONFERENCE");
            }
            if (can(capabilities, 256)) {
                builder.append(" CAPABILITY_SUPPORTS_VT_LOCAL_RX");
            }
            if (can(capabilities, 512)) {
                builder.append(" CAPABILITY_SUPPORTS_VT_LOCAL_TX");
            }
            if (can(capabilities, 768)) {
                builder.append(" CAPABILITY_SUPPORTS_VT_LOCAL_BIDIRECTIONAL");
            }
            if (can(capabilities, 1024)) {
                builder.append(" CAPABILITY_SUPPORTS_VT_REMOTE_RX");
            }
            if (can(capabilities, 2048)) {
                builder.append(" CAPABILITY_SUPPORTS_VT_REMOTE_TX");
            }
            if (can(capabilities, 4194304)) {
                builder.append(" CAPABILITY_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO");
            }
            if (can(capabilities, 3072)) {
                builder.append(" CAPABILITY_SUPPORTS_VT_REMOTE_BIDIRECTIONAL");
            }
            if (can(capabilities, 262144)) {
                builder.append(" CAPABILITY_SPEED_UP_MT_AUDIO");
            }
            if (can(capabilities, 524288)) {
                builder.append(" CAPABILITY_CAN_UPGRADE_TO_VIDEO");
            }
            if (can(capabilities, 1048576)) {
                builder.append(" CAPABILITY_CAN_PAUSE_VIDEO");
            }
            if (can(capabilities, 8388608)) {
                builder.append(" CAPABILITY_CAN_PULL_CALL");
            }
            if (can(capabilities, 16777216)) {
                builder.append(" CAPABILITY_SUPPORT_DEFLECT");
            }
            if (can(capabilities, 16777216)) {
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
            if (hasProperty(properties, 1)) {
                builder.append(" PROPERTY_CONFERENCE");
            }
            if (hasProperty(properties, 2)) {
                builder.append(" PROPERTY_GENERIC_CONFERENCE");
            }
            if (hasProperty(properties, 8)) {
                builder.append(" PROPERTY_WIFI");
            }
            if (hasProperty(properties, 16)) {
                builder.append(" PROPERTY_HIGH_DEF_AUDIO");
            }
            if (hasProperty(properties, 4)) {
                builder.append(" PROPERTY_EMERGENCY_CALLBACK_MODE");
            }
            if (hasProperty(properties, 64)) {
                builder.append(" PROPERTY_IS_EXTERNAL_CALL");
            }
            if (hasProperty(properties, 128)) {
                builder.append(" PROPERTY_HAS_CDMA_VOICE_PRIVACY");
            }
            if (hasProperty(properties, 512)) {
                builder.append(" PROPERTY_ASSISTED_DIALING_USED");
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

        public int getSupportedAudioRoutes() {
            return 15;
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

        public long getCreationTimeMillis() {
            return this.mCreationTimeMillis;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Details)) {
                return false;
            }
            Details d = (Details) o;
            if (Objects.equals(this.mHandle, d.mHandle) && Objects.equals(Integer.valueOf(this.mHandlePresentation), Integer.valueOf(d.mHandlePresentation)) && Objects.equals(this.mCallerDisplayName, d.mCallerDisplayName) && Objects.equals(Integer.valueOf(this.mCallerDisplayNamePresentation), Integer.valueOf(d.mCallerDisplayNamePresentation)) && Objects.equals(this.mAccountHandle, d.mAccountHandle) && Objects.equals(Integer.valueOf(this.mCallCapabilities), Integer.valueOf(d.mCallCapabilities)) && Objects.equals(Integer.valueOf(this.mCallProperties), Integer.valueOf(d.mCallProperties)) && Objects.equals(this.mDisconnectCause, d.mDisconnectCause) && Objects.equals(Long.valueOf(this.mConnectTimeMillis), Long.valueOf(d.mConnectTimeMillis)) && Objects.equals(this.mGatewayInfo, d.mGatewayInfo) && Objects.equals(Integer.valueOf(this.mVideoState), Integer.valueOf(d.mVideoState)) && Objects.equals(this.mStatusHints, d.mStatusHints) && Call.areBundlesEqual(this.mExtras, d.mExtras) && Call.areBundlesEqual(this.mIntentExtras, d.mIntentExtras) && Objects.equals(Long.valueOf(this.mCreationTimeMillis), Long.valueOf(d.mCreationTimeMillis))) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.mHandle, Integer.valueOf(this.mHandlePresentation), this.mCallerDisplayName, Integer.valueOf(this.mCallerDisplayNamePresentation), this.mAccountHandle, Integer.valueOf(this.mCallCapabilities), Integer.valueOf(this.mCallProperties), this.mDisconnectCause, Long.valueOf(this.mConnectTimeMillis), this.mGatewayInfo, Integer.valueOf(this.mVideoState), this.mStatusHints, this.mExtras, this.mIntentExtras, Long.valueOf(this.mCreationTimeMillis)});
        }

        public Details(String telecomCallId, Uri handle, int handlePresentation, String callerDisplayName, int callerDisplayNamePresentation, PhoneAccountHandle accountHandle, int capabilities, int properties, DisconnectCause disconnectCause, long connectTimeMillis, GatewayInfo gatewayInfo, int videoState, StatusHints statusHints, Bundle extras, Bundle intentExtras, long creationTimeMillis) {
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
            this.mCreationTimeMillis = creationTimeMillis;
        }

        public static Details createFromParcelableCall(ParcelableCall parcelableCall) {
            Details details = new Details(parcelableCall.getId(), parcelableCall.getHandle(), parcelableCall.getHandlePresentation(), parcelableCall.getCallerDisplayName(), parcelableCall.getCallerDisplayNamePresentation(), parcelableCall.getAccountHandle(), parcelableCall.getCapabilities(), parcelableCall.getProperties(), parcelableCall.getDisconnectCause(), parcelableCall.getConnectTimeMillis(), parcelableCall.getGatewayInfo(), parcelableCall.getVideoState(), parcelableCall.getStatusHints(), parcelableCall.getExtras(), parcelableCall.getIntentExtras(), parcelableCall.getCreationTimeMillis());
            return details;
        }

        public String toString() {
            return "[pa: " + this.mAccountHandle + ", hdl: " + Connection.toLogSafePhoneNumber(Log.pii(this.mHandle)) + ", caps: " + capabilitiesToString(this.mCallCapabilities) + ", props: " + propertiesToString(this.mCallProperties) + "]";
        }
    }

    @SystemApi
    @Deprecated
    public static abstract class Listener extends Callback {
    }

    public static final class RttCall {
        private static final int READ_BUFFER_SIZE = 1000;
        public static final int RTT_MODE_FULL = 1;
        public static final int RTT_MODE_HCO = 2;
        public static final int RTT_MODE_INVALID = 0;
        public static final int RTT_MODE_VCO = 3;
        private final InCallAdapter mInCallAdapter;
        private char[] mReadBuffer = new char[1000];
        private InputStreamReader mReceiveStream;
        private int mRttMode;
        private final String mTelecomCallId;
        private OutputStreamWriter mTransmitStream;

        @Retention(RetentionPolicy.SOURCE)
        public @interface RttAudioMode {
        }

        public RttCall(String telecomCallId, InputStreamReader receiveStream, OutputStreamWriter transmitStream, int mode, InCallAdapter inCallAdapter) {
            this.mTelecomCallId = telecomCallId;
            this.mReceiveStream = receiveStream;
            this.mTransmitStream = transmitStream;
            this.mRttMode = mode;
            this.mInCallAdapter = inCallAdapter;
        }

        public int getRttAudioMode() {
            return this.mRttMode;
        }

        public void setRttMode(int mode) {
            this.mInCallAdapter.setRttMode(this.mTelecomCallId, mode);
        }

        public void write(String input) throws IOException {
            this.mTransmitStream.write(input);
            this.mTransmitStream.flush();
        }

        public String read() {
            try {
                int numRead = this.mReceiveStream.read(this.mReadBuffer, 0, 1000);
                if (numRead < 0) {
                    return null;
                }
                return new String(this.mReadBuffer, 0, numRead);
            } catch (IOException e) {
                Log.w((Object) this, "Exception encountered when reading from InputStreamReader: %s", e);
                return null;
            }
        }

        public String readImmediately() throws IOException {
            if (!this.mReceiveStream.ready()) {
                return null;
            }
            int numRead = this.mReceiveStream.read(this.mReadBuffer, 0, 1000);
            if (numRead < 0) {
                return null;
            }
            return new String(this.mReadBuffer, 0, numRead);
        }

        public void close() {
            try {
                this.mReceiveStream.close();
            } catch (IOException e) {
            }
            try {
                this.mTransmitStream.close();
            } catch (IOException e2) {
            }
        }
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

    public void deflect(Uri address) {
        this.mInCallAdapter.deflectCall(this.mTelecomCallId, address);
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
        this.mInCallAdapter.sendCallEvent(this.mTelecomCallId, event, this.mTargetSdkVersion, extras);
    }

    public void sendRttRequest() {
        this.mInCallAdapter.sendRttRequest(this.mTelecomCallId);
    }

    public void respondToRttRequest(int id, boolean accept) {
        this.mInCallAdapter.respondToRttRequest(this.mTelecomCallId, id, accept);
    }

    public void handoverTo(PhoneAccountHandle toHandle, int videoState, Bundle extras) {
        this.mInCallAdapter.handoverTo(this.mTelecomCallId, toHandle, videoState, extras);
    }

    public void stopRtt() {
        this.mInCallAdapter.stopRtt(this.mTelecomCallId);
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

    public final void removeExtras(String... keys) {
        removeExtras((List<String>) Arrays.asList(keys));
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

    public InCallService.VideoCall getVideoCall() {
        return this.mVideoCallImpl;
    }

    public Details getDetails() {
        return this.mDetails;
    }

    public RttCall getRttCall() {
        return this.mRttCall;
    }

    public boolean isRttActive() {
        return this.mRttCall != null && this.mDetails.hasProperty(1024);
    }

    public void registerCallback(Callback callback) {
        registerCallback(callback, new Handler());
    }

    public void registerCallback(Callback callback, Handler handler) {
        unregisterCallback(callback);
        if (callback != null && handler != null && this.mState != 7) {
            this.mCallbackRecords.add(new CallbackRecord(callback, handler));
        }
    }

    public void unregisterCallback(Callback callback) {
        if (callback != null && this.mState != 7) {
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
            case 0:
                return "NEW";
            case 1:
                return "DIALING";
            case 2:
                return "RINGING";
            case 3:
                return "HOLDING";
            case 4:
                return "ACTIVE";
            case 7:
                return "DISCONNECTED";
            case 8:
                return "SELECT_PHONE_ACCOUNT";
            case 9:
                return "CONNECTING";
            case 10:
                return "DISCONNECTING";
            default:
                Log.w((Object) Call.class, "Unknown state %d", Integer.valueOf(state));
                return "UNKNOWN";
        }
    }

    @SystemApi
    @Deprecated
    public void addListener(Listener listener) {
        registerCallback(listener);
    }

    @SystemApi
    @Deprecated
    public void removeListener(Listener listener) {
        unregisterCallback(listener);
    }

    Call(Phone phone, String telecomCallId, InCallAdapter inCallAdapter, String callingPackage, int targetSdkVersion) {
        this.mPhone = phone;
        this.mTelecomCallId = telecomCallId;
        this.mInCallAdapter = inCallAdapter;
        this.mState = 0;
        this.mCallingPackage = callingPackage;
        this.mTargetSdkVersion = targetSdkVersion;
    }

    Call(Phone phone, String telecomCallId, InCallAdapter inCallAdapter, int state, String callingPackage, int targetSdkVersion, boolean isActiveSub) {
        this.mPhone = phone;
        this.mTelecomCallId = telecomCallId;
        this.mInCallAdapter = inCallAdapter;
        this.mState = state;
        this.mCallingPackage = callingPackage;
        this.mIsActiveSub = isActiveSub;
        this.mTargetSdkVersion = targetSdkVersion;
    }

    /* access modifiers changed from: package-private */
    public final String internalGetCallId() {
        return this.mTelecomCallId;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x01a1  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x01a8  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x01af  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x01b6  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x01bd  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x01c6  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01cf  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x01dd  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01eb  */
    /* JADX WARNING: Removed duplicated region for block: B:94:? A[RETURN, SYNTHETIC] */
    public final void internalUpdate(ParcelableCall parcelableCall, Map<String, Call> callIdMap) {
        boolean rttModeChanged;
        boolean isRttChanged;
        boolean rttModeChanged2;
        ParcelableCall parcelableCall2 = parcelableCall;
        Map<String, Call> map = callIdMap;
        Details details = Details.createFromParcelableCall(parcelableCall);
        boolean detailsChanged = !Objects.equals(this.mDetails, details);
        if (detailsChanged) {
            this.mDetails = details;
        }
        this.mNotificationType = parcelableCall.getNotificationType();
        this.mNotificationCode = parcelableCall.getNotificationCode();
        boolean cannedTextResponsesChanged = false;
        if (this.mCannedTextResponses == null && parcelableCall.getCannedSmsResponses() != null && !parcelableCall.getCannedSmsResponses().isEmpty()) {
            this.mCannedTextResponses = Collections.unmodifiableList(parcelableCall.getCannedSmsResponses());
            cannedTextResponsesChanged = true;
        }
        VideoCallImpl newVideoCallImpl = parcelableCall2.getVideoCallImpl(this.mCallingPackage, this.mTargetSdkVersion);
        boolean videoCallChanged = parcelableCall.isVideoCallProviderChanged() && !Objects.equals(this.mVideoCallImpl, newVideoCallImpl);
        if (videoCallChanged) {
            this.mVideoCallImpl = newVideoCallImpl;
        }
        if (this.mVideoCallImpl != null) {
            this.mVideoCallImpl.setVideoState(getDetails().getVideoState());
        }
        int state = parcelableCall.getState();
        boolean stateChanged = (this.mState == state && this.mIsActiveSub == parcelableCall2.mIsActiveSub) ? false : true;
        if (stateChanged) {
            this.mState = state;
            this.mIsActiveSub = parcelableCall2.mIsActiveSub;
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
        Details details2 = details;
        ArrayList arrayList = new ArrayList(conferenceableCallIds.size());
        Iterator<String> it = conferenceableCallIds.iterator();
        while (it.hasNext()) {
            Iterator<String> it2 = it;
            String otherId = it.next();
            if (map.containsKey(otherId)) {
                arrayList.add(map.get(otherId));
            }
            it = it2;
            map = callIdMap;
        }
        if (!Objects.equals(this.mConferenceableCalls, arrayList)) {
            this.mConferenceableCalls.clear();
            this.mConferenceableCalls.addAll(arrayList);
            fireConferenceableCallsChanged();
        }
        if (parcelableCall.getIsRttCallChanged()) {
            isRttChanged = false;
            ArrayList arrayList2 = arrayList;
            if (this.mDetails.hasProperty(1024)) {
                ParcelableRttCall parcelableRttCall = parcelableCall.getParcelableRttCall();
                VideoCallImpl videoCallImpl = newVideoCallImpl;
                RttCall rttCall = new RttCall(this.mTelecomCallId, new InputStreamReader(new ParcelFileDescriptor.AutoCloseInputStream(parcelableRttCall.getReceiveStream()), StandardCharsets.UTF_8), new OutputStreamWriter(new ParcelFileDescriptor.AutoCloseOutputStream(parcelableRttCall.getTransmitStream()), StandardCharsets.UTF_8), parcelableRttCall.getRttMode(), this.mInCallAdapter);
                if (this.mRttCall == null) {
                    isRttChanged = true;
                } else if (this.mRttCall.getRttAudioMode() != rttCall.getRttAudioMode()) {
                    rttModeChanged2 = true;
                    this.mRttCall = rttCall;
                    rttModeChanged = rttModeChanged2;
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
                    if (isRttChanged) {
                        fireOnIsRttChanged(this.mRttCall != null, this.mRttCall);
                    }
                    if (rttModeChanged) {
                        fireOnRttModeChanged(this.mRttCall.getRttAudioMode());
                    }
                    if (this.mState == 7) {
                        fireCallDestroyed();
                        return;
                    }
                    return;
                }
                rttModeChanged2 = false;
                this.mRttCall = rttCall;
                rttModeChanged = rttModeChanged2;
                if (stateChanged) {
                }
                if (detailsChanged) {
                }
                if (cannedTextResponsesChanged) {
                }
                if (videoCallChanged) {
                }
                if (parentChanged) {
                }
                if (childrenChanged) {
                }
                if (isRttChanged) {
                }
                if (rttModeChanged) {
                }
                if (this.mState == 7) {
                }
            } else {
                rttModeChanged = false;
                VideoCallImpl videoCallImpl2 = newVideoCallImpl;
            }
        } else {
            isRttChanged = false;
            ArrayList arrayList3 = arrayList;
            rttModeChanged = false;
            VideoCallImpl videoCallImpl3 = newVideoCallImpl;
        }
        if (this.mRttCall != null && parcelableCall.getParcelableRttCall() == null && parcelableCall.getIsRttCallChanged()) {
            this.mRttCall = null;
            isRttChanged = true;
        }
        if (stateChanged) {
        }
        if (detailsChanged) {
        }
        if (cannedTextResponsesChanged) {
        }
        if (videoCallChanged) {
        }
        if (parentChanged) {
        }
        if (childrenChanged) {
        }
        if (isRttChanged) {
        }
        if (rttModeChanged) {
        }
        if (this.mState == 7) {
        }
    }

    /* access modifiers changed from: package-private */
    public final void internalSetPostDialWait(String remaining) {
        this.mRemainingPostDialSequence = remaining;
        firePostDialWait(this.mRemainingPostDialSequence);
    }

    /* access modifiers changed from: package-private */
    public final void internalSetDisconnected() {
        if (this.mState != 7) {
            this.mState = 7;
            fireStateChanged(this.mState);
            fireCallDestroyed();
        }
    }

    /* access modifiers changed from: package-private */
    public final void internalOnConnectionEvent(String event, Bundle extras) {
        fireOnConnectionEvent(event, extras);
    }

    /* access modifiers changed from: package-private */
    public final void internalOnRttUpgradeRequest(int requestId) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            record.getHandler().post(new Runnable(this, requestId) {
                private final /* synthetic */ Call f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    Call.Callback.this.onRttRequest(this.f$1, this.f$2);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public final void internalOnRttInitiationFailure(int reason) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            record.getHandler().post(new Runnable(this, reason) {
                private final /* synthetic */ Call f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    Call.Callback.this.onRttInitiationFailure(this.f$1, this.f$2);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public final void internalOnHandoverFailed(int error) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            record.getHandler().post(new Runnable(this, error) {
                private final /* synthetic */ Call f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    Call.Callback.this.onHandoverFailed(this.f$1, this.f$2);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public final void internalOnHandoverComplete() {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            record.getHandler().post(new Runnable(this) {
                private final /* synthetic */ Call f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    Call.Callback.this.onHandoverComplete(this.f$1);
                }
            });
        }
    }

    private void fireStateChanged(final int newState) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                public void run() {
                    callback.onStateChanged(this, newState);
                }
            });
        }
    }

    private void fireParentChanged(final Call newParent) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                public void run() {
                    callback.onParentChanged(this, newParent);
                }
            });
        }
    }

    private void fireChildrenChanged(final List<Call> children) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                public void run() {
                    callback.onChildrenChanged(this, children);
                }
            });
        }
    }

    private void fireDetailsChanged(final Details details) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                public void run() {
                    callback.onDetailsChanged(this, details);
                }
            });
        }
    }

    private void fireCannedTextResponsesLoaded(final List<String> cannedTextResponses) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                public void run() {
                    callback.onCannedTextResponsesLoaded(this, cannedTextResponses);
                }
            });
        }
    }

    private void fireVideoCallChanged(final InCallService.VideoCall videoCall) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                public void run() {
                    callback.onVideoCallChanged(this, videoCall);
                }
            });
        }
    }

    private void firePostDialWait(final String remainingPostDialSequence) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                public void run() {
                    callback.onPostDialWait(this, remainingPostDialSequence);
                }
            });
        }
    }

    private void fireCallDestroyed() {
        if (this.mCallbackRecords.isEmpty()) {
            this.mPhone.internalRemoveCall(this);
        }
        for (final CallbackRecord<Callback> record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                public void run() {
                    boolean isFinalRemoval = false;
                    RuntimeException toThrow = null;
                    try {
                        callback.onCallDestroyed(this);
                    } catch (RuntimeException e) {
                        toThrow = e;
                    }
                    synchronized (Call.this) {
                        Call.this.mCallbackRecords.remove(record);
                        if (Call.this.mCallbackRecords.isEmpty()) {
                            isFinalRemoval = true;
                        }
                    }
                    if (isFinalRemoval) {
                        Call.this.mPhone.internalRemoveCall(this);
                    }
                    if (toThrow != null) {
                        throw toThrow;
                    }
                }
            });
        }
    }

    private void fireConferenceableCallsChanged() {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                public void run() {
                    callback.onConferenceableCallsChanged(this, Call.this.mUnmodifiableConferenceableCalls);
                }
            });
        }
    }

    private void fireOnConnectionEvent(String event, Bundle extras) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            Handler handler = record.getHandler();
            final Callback callback = record.getCallback();
            final String str = event;
            final Bundle bundle = extras;
            AnonymousClass10 r2 = new Runnable() {
                public void run() {
                    callback.onConnectionEvent(this, str, bundle);
                }
            };
            handler.post(r2);
        }
    }

    private void fireOnIsRttChanged(boolean enabled, RttCall rttCall) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            record.getHandler().post(new Runnable(this, enabled, rttCall) {
                private final /* synthetic */ Call f$1;
                private final /* synthetic */ boolean f$2;
                private final /* synthetic */ Call.RttCall f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    Call.Callback.this.onRttStatusChanged(this.f$1, this.f$2, this.f$3);
                }
            });
        }
    }

    private void fireOnRttModeChanged(int mode) {
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            record.getHandler().post(new Runnable(this, mode) {
                private final /* synthetic */ Call f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    Call.Callback.this.onRttModeChanged(this.f$1, this.f$2);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public static boolean areBundlesEqual(Bundle bundle, Bundle newBundle) {
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
