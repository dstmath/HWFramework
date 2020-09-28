package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.telecom.ConferenceParticipant;
import android.telecom.Connection;
import android.telephony.Rlog;
import android.telephony.emergency.EmergencyNumber;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.emergency.EmergencyNumberTracker;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Connection {
    public static final int AUDIO_QUALITY_HIGH_DEFINITION = 2;
    public static final int AUDIO_QUALITY_STANDARD = 1;
    @UnsupportedAppUsage
    private static String LOG_TAG = TAG;
    private static final String TAG = "Connection";
    @UnsupportedAppUsage
    protected String mAddress;
    private boolean mAllowAddCallDuringVideoCall;
    private boolean mAnsweringDisconnectsActiveCall;
    private boolean mAudioModeIsVoip;
    private int mAudioQuality;
    private int mCallRadioTech = 0;
    private int mCallSubstate;
    protected int mCause = 0;
    @UnsupportedAppUsage
    protected String mCnapName;
    @UnsupportedAppUsage
    protected int mCnapNamePresentation = 1;
    protected long mConnectTime;
    protected long mConnectTimeReal;
    private int mConnectionCapabilities;
    protected String mConvertedNumber;
    protected long mCreateTime;
    protected String mDialAddress;
    @UnsupportedAppUsage
    protected String mDialString;
    @UnsupportedAppUsage
    protected long mDuration;
    private EmergencyNumber mEmergencyNumberInfo;
    private Bundle mExtras;
    private boolean mHasKnownUserIntentEmergency;
    protected long mHoldingStartTime;
    private boolean mIsEmergencyCall;
    @UnsupportedAppUsage
    protected boolean mIsIncoming;
    private boolean mIsNetworkIdentifiedEmergencyCall;
    private boolean mIsPulledCall = false;
    public Set<Listener> mListeners = new CopyOnWriteArraySet();
    protected int mNextPostDialChar;
    protected boolean mNumberConverted = false;
    @UnsupportedAppUsage
    protected int mNumberPresentation = 1;
    protected Connection mOrigConnection;
    private int mPhoneType;
    private List<PostDialListener> mPostDialListeners = new ArrayList();
    protected PostDialState mPostDialState = PostDialState.NOT_STARTED;
    protected String mPostDialString;
    public Call.State mPreHandoverState = Call.State.IDLE;
    private int mPulledDialogId;
    protected String mRedirectAddress;
    protected int mRedirectNumberPresentation = 1;
    private String mTelecomCallId;
    Object mUserData;
    private Connection.VideoProvider mVideoProvider;
    private int mVideoState;

    public static class Capability {
        public static final int IS_EXTERNAL_CONNECTION = 16;
        public static final int IS_PULLABLE = 32;
        public static final int SUPPORTS_DOWNGRADE_TO_VOICE_LOCAL = 1;
        public static final int SUPPORTS_DOWNGRADE_TO_VOICE_REMOTE = 2;
        public static final int SUPPORTS_VT_LOCAL_BIDIRECTIONAL = 4;
        public static final int SUPPORTS_VT_REMOTE_BIDIRECTIONAL = 8;
    }

    public interface Listener {
        void onAudioQualityChanged(int i);

        void onCallPullFailed(Connection connection);

        void onCallRadioTechChanged(int i);

        void onCallSubstateChanged(int i);

        void onConferenceMergedFailed();

        void onConferenceParticipantsChanged(List<ConferenceParticipant> list);

        void onConnectionCapabilitiesChanged(int i);

        void onConnectionEvent(String str, Bundle bundle);

        void onDisconnect(int i);

        void onExitedEcmMode();

        void onExtrasChanged(Bundle bundle);

        void onHandoverToWifiFailed();

        void onIsNetworkEmergencyCallChanged(boolean z);

        void onMultipartyStateChanged(boolean z);

        void onOriginalConnectionReplaced(Connection connection);

        void onRttInitiated();

        void onRttModifyRequestReceived();

        void onRttModifyResponseReceived(int i);

        void onRttTerminated();

        void onVideoProviderChanged(Connection.VideoProvider videoProvider);

        void onVideoStateChanged(int i);
    }

    public interface PostDialListener {
        void onPostDialChar(char c);

        void onPostDialWait();
    }

    public enum PostDialState {
        NOT_STARTED,
        STARTED,
        WAIT,
        WILD,
        COMPLETE,
        CANCELLED,
        PAUSE
    }

    public abstract void cancelPostDial();

    public abstract void deflect(String str) throws CallStateException;

    @UnsupportedAppUsage
    public abstract Call getCall();

    @UnsupportedAppUsage
    public abstract long getDisconnectTime();

    public abstract long getHoldDurationMillis();

    public abstract int getNumberPresentation();

    public abstract int getPreciseDisconnectCause();

    public abstract UUSInfo getUUSInfo();

    public abstract String getVendorDisconnectCause();

    @UnsupportedAppUsage
    public abstract void hangup() throws CallStateException;

    public abstract boolean isMultiparty();

    public abstract void proceedAfterWaitChar();

    public abstract void proceedAfterWildChar(String str);

    public abstract void separate() throws CallStateException;

    public static abstract class ListenerBase implements Listener {
        @Override // com.android.internal.telephony.Connection.Listener
        public void onVideoStateChanged(int videoState) {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onConnectionCapabilitiesChanged(int capability) {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onCallRadioTechChanged(int vrat) {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onVideoProviderChanged(Connection.VideoProvider videoProvider) {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onAudioQualityChanged(int audioQuality) {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onConferenceParticipantsChanged(List<ConferenceParticipant> list) {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onCallSubstateChanged(int callSubstate) {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onMultipartyStateChanged(boolean isMultiParty) {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onConferenceMergedFailed() {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onExtrasChanged(Bundle extras) {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onExitedEcmMode() {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onCallPullFailed(Connection externalConnection) {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onHandoverToWifiFailed() {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onConnectionEvent(String event, Bundle extras) {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onRttModifyRequestReceived() {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onRttModifyResponseReceived(int status) {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onDisconnect(int cause) {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onRttInitiated() {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onRttTerminated() {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onOriginalConnectionReplaced(Connection newConnection) {
        }

        @Override // com.android.internal.telephony.Connection.Listener
        public void onIsNetworkEmergencyCallChanged(boolean isEmergencyCall) {
        }
    }

    @UnsupportedAppUsage
    protected Connection(int phoneType) {
        this.mPhoneType = phoneType;
    }

    public String getTelecomCallId() {
        return this.mTelecomCallId;
    }

    public void setTelecomCallId(String telecomCallId) {
        this.mTelecomCallId = telecomCallId;
    }

    @UnsupportedAppUsage
    public String getAddress() {
        return this.mAddress;
    }

    public void setAddress(String dialAddress) {
        this.mAddress = dialAddress;
    }

    public String getDialAddress() {
        return this.mDialAddress;
    }

    public String getRedirectAddress() {
        return this.mRedirectAddress;
    }

    public int getRedirectNumberPresentation() {
        return this.mRedirectNumberPresentation;
    }

    public String getCnapName() {
        return this.mCnapName;
    }

    public String getOrigDialString() {
        return null;
    }

    public int getCnapNamePresentation() {
        return this.mCnapNamePresentation;
    }

    @UnsupportedAppUsage
    public long getCreateTime() {
        return this.mCreateTime;
    }

    @UnsupportedAppUsage
    public long getConnectTime() {
        return this.mConnectTime;
    }

    public void setConnectTime(long connectTime) {
        this.mConnectTime = connectTime;
    }

    public void setConnectTimeReal(long connectTimeReal) {
        this.mConnectTimeReal = connectTimeReal;
    }

    public long getConnectTimeReal() {
        return this.mConnectTimeReal;
    }

    @UnsupportedAppUsage
    public long getDurationMillis() {
        if (this.mConnectTimeReal == 0) {
            return 0;
        }
        long j = this.mDuration;
        if (j == 0) {
            return SystemClock.elapsedRealtime() - this.mConnectTimeReal;
        }
        return j;
    }

    public long getHoldingStartTime() {
        return this.mHoldingStartTime;
    }

    @UnsupportedAppUsage
    public int getDisconnectCause() {
        return this.mCause;
    }

    @UnsupportedAppUsage
    public boolean isIncoming() {
        return this.mIsIncoming;
    }

    public void setIsIncoming(boolean isIncoming) {
        this.mIsIncoming = isIncoming;
    }

    public boolean isEmergencyCall() {
        return this.mIsEmergencyCall;
    }

    public EmergencyNumber getEmergencyNumberInfo() {
        return this.mEmergencyNumberInfo;
    }

    public boolean hasKnownUserIntentEmergency() {
        return this.mHasKnownUserIntentEmergency;
    }

    public void setEmergencyCallInfo(CallTracker ct) {
        if (ct != null) {
            Phone phone = ct.getPhone();
            if (phone != null) {
                EmergencyNumberTracker tracker = phone.getEmergencyNumberTracker();
                if (tracker != null) {
                    EmergencyNumber num = tracker.getEmergencyNumber(this.mAddress);
                    if (num != null) {
                        this.mIsEmergencyCall = true;
                        this.mEmergencyNumberInfo = num;
                        return;
                    }
                    Rlog.e(TAG, "setEmergencyCallInfo: emergency number is null");
                    return;
                }
                Rlog.e(TAG, "setEmergencyCallInfo: emergency number tracker is null");
                return;
            }
            Rlog.e(TAG, "setEmergencyCallInfo: phone is null");
            return;
        }
        Rlog.e(TAG, "setEmergencyCallInfo: call tracker is null");
    }

    public void setHasKnownUserIntentEmergency(boolean hasKnownUserIntentEmergency) {
        this.mHasKnownUserIntentEmergency = hasKnownUserIntentEmergency;
    }

    @UnsupportedAppUsage
    public Call.State getState() {
        Call c = getCall();
        if (c == null) {
            return Call.State.IDLE;
        }
        return c.getState();
    }

    public Call.State getStateBeforeHandover() {
        return this.mPreHandoverState;
    }

    public List<ConferenceParticipant> getConferenceParticipants() {
        Call c = getCall();
        if (c == null) {
            return null;
        }
        return c.getConferenceParticipants();
    }

    @UnsupportedAppUsage
    public boolean isAlive() {
        return getState().isAlive();
    }

    public boolean isRinging() {
        return getState().isRinging();
    }

    @UnsupportedAppUsage
    public Object getUserData() {
        return this.mUserData;
    }

    public void setUserData(Object userdata) {
        this.mUserData = userdata;
    }

    public void clearUserData() {
        this.mUserData = null;
    }

    public void addPostDialListener(PostDialListener listener) {
        if (!this.mPostDialListeners.contains(listener)) {
            this.mPostDialListeners.add(listener);
        }
    }

    public final void removePostDialListener(PostDialListener listener) {
        this.mPostDialListeners.remove(listener);
    }

    /* access modifiers changed from: protected */
    public final void clearPostDialListeners() {
        List<PostDialListener> list = this.mPostDialListeners;
        if (list != null) {
            list.clear();
        }
    }

    /* access modifiers changed from: protected */
    public final void notifyPostDialListeners() {
        if (getPostDialState() == PostDialState.WAIT) {
            Iterator it = new ArrayList(this.mPostDialListeners).iterator();
            while (it.hasNext()) {
                ((PostDialListener) it.next()).onPostDialWait();
            }
        }
    }

    /* access modifiers changed from: protected */
    public final void notifyPostDialListenersNextChar(char c) {
        Iterator it = new ArrayList(this.mPostDialListeners).iterator();
        while (it.hasNext()) {
            ((PostDialListener) it.next()).onPostDialChar(c);
        }
    }

    public PostDialState getPostDialState() {
        return this.mPostDialState;
    }

    public String getRemainingPostDialString() {
        String str;
        int i;
        if (this.mPostDialState == PostDialState.CANCELLED || this.mPostDialState == PostDialState.COMPLETE || (str = this.mPostDialString) == null || str.length() <= (i = this.mNextPostDialChar)) {
            return PhoneConfigurationManager.SSSS;
        }
        return this.mPostDialString.substring(i);
    }

    public boolean onDisconnect(int cause) {
        return false;
    }

    public Connection getOrigConnection() {
        return this.mOrigConnection;
    }

    public boolean isConferenceHost() {
        return false;
    }

    public boolean isMemberOfPeerConference() {
        return false;
    }

    public void migrateFrom(Connection c) {
        if (c != null) {
            this.mListeners = c.mListeners;
            this.mDialAddress = c.getDialAddress();
            this.mAddress = c.getAddress();
            this.mDialString = c.getOrigDialString();
            this.mCreateTime = c.getCreateTime();
            this.mConnectTime = c.getConnectTime();
            this.mConnectTimeReal = c.getConnectTimeReal();
            this.mHoldingStartTime = c.getHoldingStartTime();
            this.mOrigConnection = c;
            this.mPostDialString = c.mPostDialString;
            this.mNextPostDialChar = c.mNextPostDialChar;
            this.mPostDialState = c.mPostDialState;
            this.mIsEmergencyCall = c.isEmergencyCall();
            this.mEmergencyNumberInfo = c.getEmergencyNumberInfo();
            this.mHasKnownUserIntentEmergency = c.hasKnownUserIntentEmergency();
        }
    }

    public void addListener(Listener listener) {
        this.mListeners.add(listener);
    }

    public final void removeListener(Listener listener) {
        this.mListeners.remove(listener);
    }

    public int getVideoState() {
        return this.mVideoState;
    }

    public int getConnectionCapabilities() {
        return this.mConnectionCapabilities;
    }

    public boolean hasCapabilities(int connectionCapabilities) {
        return (this.mConnectionCapabilities & connectionCapabilities) == connectionCapabilities;
    }

    public static int addCapability(int capabilities, int capability) {
        return capabilities | capability;
    }

    public static int removeCapability(int capabilities, int capability) {
        return (~capability) & capabilities;
    }

    public boolean isWifi() {
        return getCallRadioTech() == 18;
    }

    public int getCallRadioTech() {
        return this.mCallRadioTech;
    }

    public boolean getAudioModeIsVoip() {
        return this.mAudioModeIsVoip;
    }

    public Connection.VideoProvider getVideoProvider() {
        return this.mVideoProvider;
    }

    public int getAudioQuality() {
        return this.mAudioQuality;
    }

    public int getCallSubstate() {
        return this.mCallSubstate;
    }

    @UnsupportedAppUsage
    public void setVideoState(int videoState) {
        this.mVideoState = videoState;
        for (Listener l : this.mListeners) {
            l.onVideoStateChanged(this.mVideoState);
        }
    }

    public void setConnectionCapabilities(int capabilities) {
        if (this.mConnectionCapabilities != capabilities) {
            this.mConnectionCapabilities = capabilities;
            for (Listener l : this.mListeners) {
                l.onConnectionCapabilitiesChanged(this.mConnectionCapabilities);
            }
        }
    }

    public void setCallRadioTech(int vrat) {
        if (this.mCallRadioTech != vrat) {
            this.mCallRadioTech = vrat;
            for (Listener l : this.mListeners) {
                l.onCallRadioTechChanged(vrat);
            }
        }
    }

    public void setAudioModeIsVoip(boolean isVoip) {
        this.mAudioModeIsVoip = isVoip;
    }

    public void setAudioQuality(int audioQuality) {
        this.mAudioQuality = audioQuality;
        for (Listener l : this.mListeners) {
            l.onAudioQualityChanged(this.mAudioQuality);
        }
    }

    public void setConnectionExtras(Bundle extras) {
        if (extras != null) {
            this.mExtras = new Bundle(extras);
            int previousCount = this.mExtras.size();
            this.mExtras = this.mExtras.filterValues();
            int filteredCount = this.mExtras.size();
            if (filteredCount != previousCount) {
                Rlog.i(TAG, "setConnectionExtras: filtering " + (previousCount - filteredCount) + " invalid extras.");
            }
        } else {
            this.mExtras = null;
        }
        for (Listener l : this.mListeners) {
            l.onExtrasChanged(this.mExtras);
        }
    }

    public Bundle getConnectionExtras() {
        Bundle bundle = this.mExtras;
        if (bundle == null) {
            return null;
        }
        return new Bundle(bundle);
    }

    public boolean isActiveCallDisconnectedOnAnswer() {
        return this.mAnsweringDisconnectsActiveCall;
    }

    public void setActiveCallDisconnectedOnAnswer(boolean answeringDisconnectsActiveCall) {
        this.mAnsweringDisconnectsActiveCall = answeringDisconnectsActiveCall;
    }

    public boolean shouldAllowAddCallDuringVideoCall() {
        return this.mAllowAddCallDuringVideoCall;
    }

    public void setAllowAddCallDuringVideoCall(boolean allowAddCallDuringVideoCall) {
        this.mAllowAddCallDuringVideoCall = allowAddCallDuringVideoCall;
    }

    public void setIsPulledCall(boolean isPulledCall) {
        this.mIsPulledCall = isPulledCall;
    }

    public boolean isPulledCall() {
        return this.mIsPulledCall;
    }

    public void setPulledDialogId(int pulledDialogId) {
        this.mPulledDialogId = pulledDialogId;
    }

    public int getPulledDialogId() {
        return this.mPulledDialogId;
    }

    public void setCallSubstate(int callSubstate) {
        this.mCallSubstate = callSubstate;
        for (Listener l : this.mListeners) {
            l.onCallSubstateChanged(this.mCallSubstate);
        }
    }

    public void setVideoProvider(Connection.VideoProvider videoProvider) {
        this.mVideoProvider = videoProvider;
        for (Listener l : this.mListeners) {
            l.onVideoProviderChanged(this.mVideoProvider);
        }
    }

    public void setConverted(String oriNumber) {
        this.mNumberConverted = true;
        this.mConvertedNumber = this.mAddress;
        this.mAddress = oriNumber;
        this.mDialAddress = this.mAddress;
        this.mDialString = oriNumber;
    }

    public void setAddress(String newAddress, int numberPresentation) {
        Rlog.i(TAG, "setAddress = " + newAddress);
        this.mAddress = newAddress;
        this.mNumberPresentation = numberPresentation;
    }

    public void setDialString(String newDialString) {
        this.mDialString = newDialString;
    }

    public void updateConferenceParticipants(List<ConferenceParticipant> conferenceParticipants) {
        for (Listener l : this.mListeners) {
            l.onConferenceParticipantsChanged(conferenceParticipants);
        }
    }

    public void updateMultipartyState(boolean isMultiparty) {
        for (Listener l : this.mListeners) {
            l.onMultipartyStateChanged(isMultiparty);
        }
    }

    public void onConferenceMergeFailed() {
        for (Listener l : this.mListeners) {
            l.onConferenceMergedFailed();
        }
    }

    public void onExitedEcmMode() {
        for (Listener l : this.mListeners) {
            l.onExitedEcmMode();
        }
    }

    public void onCallPullFailed(Connection externalConnection) {
        for (Listener l : this.mListeners) {
            l.onCallPullFailed(externalConnection);
        }
    }

    public void onOriginalConnectionReplaced(Connection newConnection) {
        for (Listener l : this.mListeners) {
            l.onOriginalConnectionReplaced(newConnection);
        }
    }

    public void onHandoverToWifiFailed() {
        for (Listener l : this.mListeners) {
            l.onHandoverToWifiFailed();
        }
    }

    public void onConnectionEvent(String event, Bundle extras) {
        for (Listener l : this.mListeners) {
            l.onConnectionEvent(event, extras);
        }
    }

    public void onDisconnectConferenceParticipant(Uri endpoint) {
    }

    public void pullExternalCall() {
    }

    public void onRttModifyRequestReceived() {
        for (Listener l : this.mListeners) {
            l.onRttModifyRequestReceived();
        }
    }

    public void onRttModifyResponseReceived(int status) {
        for (Listener l : this.mListeners) {
            l.onRttModifyResponseReceived(status);
        }
    }

    public void onRttInitiated() {
        for (Listener l : this.mListeners) {
            l.onRttInitiated();
        }
    }

    public void onRttTerminated() {
        for (Listener l : this.mListeners) {
            l.onRttTerminated();
        }
    }

    /* access modifiers changed from: protected */
    public void notifyDisconnect(int reason) {
        Rlog.i(TAG, "notifyDisconnect: callId=" + getTelecomCallId() + ", reason=" + reason);
        for (Listener l : this.mListeners) {
            l.onDisconnect(reason);
        }
    }

    public int getPhoneType() {
        return this.mPhoneType;
    }

    public void resetConnectionTime() {
        int i = this.mPhoneType;
        if (i == 6 || i == 2) {
            this.mConnectTime = System.currentTimeMillis();
            this.mConnectTimeReal = SystemClock.elapsedRealtime();
            this.mDuration = 0;
        }
    }

    public void setIsNetworkIdentifiedEmergencyCall(boolean isNetworkIdentifiedEmergencyCall) {
        this.mIsNetworkIdentifiedEmergencyCall = isNetworkIdentifiedEmergencyCall;
        for (Listener l : this.mListeners) {
            l.onIsNetworkEmergencyCallChanged(isNetworkIdentifiedEmergencyCall);
        }
    }

    public boolean isNetworkIdentifiedEmergencyCall() {
        return this.mIsNetworkIdentifiedEmergencyCall;
    }

    public String toString() {
        StringBuilder str = new StringBuilder(128);
        str.append(" callId: " + getTelecomCallId());
        StringBuilder sb = new StringBuilder();
        sb.append(" isExternal: ");
        sb.append((this.mConnectionCapabilities & 16) == 16 ? "Y" : "N");
        str.append(sb.toString());
        if (Rlog.isLoggable(LOG_TAG, 3)) {
            str.append("addr: XXXX");
            str.append(" pres.: " + getNumberPresentation());
            str.append(" dial: XXXX");
            str.append(" postdial: " + getRemainingPostDialString());
            str.append(" cnap name: " + getCnapName());
            str.append("(" + getCnapNamePresentation() + ")");
        }
        str.append(" incoming: " + isIncoming());
        str.append(" state: " + getState());
        str.append(" post dial state: " + getPostDialState());
        return str.toString();
    }
}
