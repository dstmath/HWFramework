package com.android.internal.telephony;

import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.telecom.ConferenceParticipant;
import android.telecom.Connection.VideoProvider;
import android.telephony.Rlog;
import com.android.internal.telephony.Call.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Connection {
    public static final int AUDIO_QUALITY_HIGH_DEFINITION = 2;
    public static final int AUDIO_QUALITY_STANDARD = 1;
    private static String LOG_TAG = "Connection";
    protected String mAddress;
    private boolean mAllowAddCallDuringVideoCall;
    private boolean mAnsweringDisconnectsActiveCall;
    private boolean mAudioModeIsVoip;
    private int mAudioQuality;
    private int mCallSubstate;
    protected int mCause = 0;
    protected String mCnapName;
    protected int mCnapNamePresentation = 1;
    protected long mConnectTime;
    protected long mConnectTimeReal;
    private int mConnectionCapabilities;
    protected String mConvertedNumber;
    protected long mCreateTime;
    protected String mDialAddress;
    protected String mDialString;
    protected long mDuration;
    private Bundle mExtras;
    protected long mHoldingStartTime;
    protected boolean mIsIncoming;
    private boolean mIsPulledCall = false;
    private boolean mIsWifi;
    public Set<Listener> mListeners = new CopyOnWriteArraySet();
    protected int mNextPostDialChar;
    protected boolean mNumberConverted = false;
    protected int mNumberPresentation = 1;
    protected Connection mOrigConnection;
    private int mPhoneType;
    private List<PostDialListener> mPostDialListeners = new ArrayList();
    protected PostDialState mPostDialState = PostDialState.NOT_STARTED;
    protected String mPostDialString;
    public State mPreHandoverState = State.IDLE;
    private int mPulledDialogId;
    private String mTelecomCallId;
    Object mUserData;
    private VideoProvider mVideoProvider;
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

        void onCallSubstateChanged(int i);

        void onConferenceMergedFailed();

        void onConferenceParticipantsChanged(List<ConferenceParticipant> list);

        void onConnectionCapabilitiesChanged(int i);

        void onConnectionEvent(String str, Bundle bundle);

        void onExitedEcmMode();

        void onExtrasChanged(Bundle bundle);

        void onHandoverToWifiFailed();

        void onMultipartyStateChanged(boolean z);

        void onVideoProviderChanged(VideoProvider videoProvider);

        void onVideoStateChanged(int i);

        void onWifiChanged(boolean z);
    }

    public static abstract class ListenerBase implements Listener {
        public void onVideoStateChanged(int videoState) {
        }

        public void onConnectionCapabilitiesChanged(int capability) {
        }

        public void onWifiChanged(boolean isWifi) {
        }

        public void onVideoProviderChanged(VideoProvider videoProvider) {
        }

        public void onAudioQualityChanged(int audioQuality) {
        }

        public void onConferenceParticipantsChanged(List<ConferenceParticipant> list) {
        }

        public void onCallSubstateChanged(int callSubstate) {
        }

        public void onMultipartyStateChanged(boolean isMultiParty) {
        }

        public void onConferenceMergedFailed() {
        }

        public void onExtrasChanged(Bundle extras) {
        }

        public void onExitedEcmMode() {
        }

        public void onCallPullFailed(Connection externalConnection) {
        }

        public void onHandoverToWifiFailed() {
        }

        public void onConnectionEvent(String event, Bundle extras) {
        }
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

    public abstract Call getCall();

    public abstract long getDisconnectTime();

    public abstract long getHoldDurationMillis();

    public abstract int getNumberPresentation();

    public abstract int getPreciseDisconnectCause();

    public abstract UUSInfo getUUSInfo();

    public abstract String getVendorDisconnectCause();

    public abstract void hangup() throws CallStateException;

    public abstract boolean isMultiparty();

    public abstract void proceedAfterWaitChar();

    public abstract void proceedAfterWildChar(String str);

    public abstract void separate() throws CallStateException;

    protected Connection(int phoneType) {
        this.mPhoneType = phoneType;
    }

    public String getTelecomCallId() {
        return this.mTelecomCallId;
    }

    public void setTelecomCallId(String telecomCallId) {
        this.mTelecomCallId = telecomCallId;
    }

    public String getAddress() {
        return this.mAddress;
    }

    public void setAddress(String dialAddress) {
        this.mAddress = dialAddress;
    }

    public String getDialAddress() {
        return this.mDialAddress;
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

    public long getCreateTime() {
        return this.mCreateTime;
    }

    public long getConnectTime() {
        return this.mConnectTime;
    }

    public void setConnectTime(long connectTime) {
        this.mConnectTime = connectTime;
    }

    public long getConnectTimeReal() {
        return this.mConnectTimeReal;
    }

    public long getDurationMillis() {
        if (this.mConnectTimeReal == 0) {
            return 0;
        }
        if (this.mDuration == 0) {
            return SystemClock.elapsedRealtime() - this.mConnectTimeReal;
        }
        return this.mDuration;
    }

    public long getHoldingStartTime() {
        return this.mHoldingStartTime;
    }

    public int getDisconnectCause() {
        return this.mCause;
    }

    public boolean isIncoming() {
        return this.mIsIncoming;
    }

    public State getState() {
        Call c = getCall();
        if (c == null) {
            return State.IDLE;
        }
        return c.getState();
    }

    public State getStateBeforeHandover() {
        return this.mPreHandoverState;
    }

    public List<ConferenceParticipant> getConferenceParticipants() {
        Call c = getCall();
        if (c == null) {
            return null;
        }
        return c.getConferenceParticipants();
    }

    public boolean isAlive() {
        return getState().isAlive();
    }

    public boolean isRinging() {
        return getState().isRinging();
    }

    public Object getUserData() {
        return this.mUserData;
    }

    public void setUserData(Object userdata) {
        this.mUserData = userdata;
    }

    public void clearUserData() {
        this.mUserData = null;
    }

    public final void addPostDialListener(PostDialListener listener) {
        if (!this.mPostDialListeners.contains(listener)) {
            this.mPostDialListeners.add(listener);
        }
    }

    public final void removePostDialListener(PostDialListener listener) {
        this.mPostDialListeners.remove(listener);
    }

    protected final void clearPostDialListeners() {
        if (this.mPostDialListeners != null) {
            this.mPostDialListeners.clear();
        }
    }

    protected final void notifyPostDialListeners() {
        if (getPostDialState() == PostDialState.WAIT) {
            for (PostDialListener listener : new ArrayList(this.mPostDialListeners)) {
                listener.onPostDialWait();
            }
        }
    }

    protected final void notifyPostDialListenersNextChar(char c) {
        for (PostDialListener listener : new ArrayList(this.mPostDialListeners)) {
            listener.onPostDialChar(c);
        }
    }

    public PostDialState getPostDialState() {
        return this.mPostDialState;
    }

    public String getRemainingPostDialString() {
        if (this.mPostDialState == PostDialState.CANCELLED || this.mPostDialState == PostDialState.COMPLETE || this.mPostDialString == null || this.mPostDialString.length() <= this.mNextPostDialChar) {
            return "";
        }
        return this.mPostDialString.substring(this.mNextPostDialChar);
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
        }
    }

    public final void addListener(Listener listener) {
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
        return this.mIsWifi;
    }

    public boolean getAudioModeIsVoip() {
        return this.mAudioModeIsVoip;
    }

    public VideoProvider getVideoProvider() {
        return this.mVideoProvider;
    }

    public int getAudioQuality() {
        return this.mAudioQuality;
    }

    public int getCallSubstate() {
        return this.mCallSubstate;
    }

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

    public void setWifi(boolean isWifi) {
        this.mIsWifi = isWifi;
        for (Listener l : this.mListeners) {
            l.onWifiChanged(this.mIsWifi);
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
        } else {
            this.mExtras = null;
        }
        for (Listener l : this.mListeners) {
            l.onExtrasChanged(this.mExtras);
        }
    }

    public Bundle getConnectionExtras() {
        return this.mExtras == null ? null : new Bundle(this.mExtras);
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

    public void setVideoProvider(VideoProvider videoProvider) {
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

    public int getPhoneType() {
        return this.mPhoneType;
    }

    public String toString() {
        StringBuilder str = new StringBuilder(128);
        str.append(" callId: ").append(getTelecomCallId());
        str.append(" isExternal: ").append((this.mConnectionCapabilities & 16) == 16 ? "Y" : "N");
        if (Rlog.isLoggable(LOG_TAG, 3)) {
            str.append("addr: XXXX").append(" pres.: ").append(getNumberPresentation()).append(" dial: XXXX").append(" postdial: ").append(getRemainingPostDialString()).append(" cnap name: ").append(getCnapName()).append("(").append(getCnapNamePresentation()).append(")");
        }
        str.append(" incoming: ").append(isIncoming()).append(" state: ").append(getState()).append(" post dial state: ").append(getPostDialState());
        return str.toString();
    }
}
