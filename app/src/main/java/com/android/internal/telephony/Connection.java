package com.android.internal.telephony;

import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.telecom.ConferenceParticipant;
import android.telecom.Connection.VideoProvider;
import android.telephony.Rlog;
import com.android.internal.telephony.Call.State;
import com.google.android.mms.pdu.PduPart;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Connection {
    public static final int AUDIO_QUALITY_HIGH_DEFINITION = 2;
    public static final int AUDIO_QUALITY_STANDARD = 1;
    private static String LOG_TAG;
    protected String mAddress;
    private int mAudioQuality;
    private int mCallSubstate;
    protected int mCause;
    protected String mCnapName;
    protected int mCnapNamePresentation;
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
    private boolean mIsWifi;
    public Set<Listener> mListeners;
    protected int mNextPostDialChar;
    protected boolean mNumberConverted;
    protected int mNumberPresentation;
    protected Connection mOrigConnection;
    private int mPhoneType;
    private List<PostDialListener> mPostDialListeners;
    protected PostDialState mPostDialState;
    protected String mPostDialString;
    public State mPreHandoverState;
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

        void onCallSubstateChanged(int i);

        void onConferenceMergedFailed();

        void onConferenceParticipantsChanged(List<ConferenceParticipant> list);

        void onConnectionCapabilitiesChanged(int i);

        void onExitedEcmMode();

        void onExtrasChanged(Bundle bundle);

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
    }

    public interface PostDialListener {
        void onPostDialChar(char c);

        void onPostDialWait();
    }

    public enum PostDialState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.Connection.PostDialState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.Connection.PostDialState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.Connection.PostDialState.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.Connection.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.Connection.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.Connection.<clinit>():void");
    }

    public static int removeCapability(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.Connection.removeCapability(int, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.Connection.removeCapability(int, int):int");
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
        this.mCnapNamePresentation = AUDIO_QUALITY_STANDARD;
        this.mNumberPresentation = AUDIO_QUALITY_STANDARD;
        this.mPostDialListeners = new ArrayList();
        this.mListeners = new CopyOnWriteArraySet();
        this.mNumberConverted = false;
        this.mCause = 0;
        this.mPostDialState = PostDialState.NOT_STARTED;
        this.mPreHandoverState = State.IDLE;
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
        this.mPostDialListeners.clear();
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

    public static int addCapability(int capabilities, int capability) {
        return capabilities | capability;
    }

    public boolean isWifi() {
        return this.mIsWifi;
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
        return this.mExtras;
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

    public void onDisconnectConferenceParticipant(Uri endpoint) {
    }

    public void pullExternalCall() {
    }

    public int getPhoneType() {
        return this.mPhoneType;
    }

    public String toString() {
        StringBuilder str = new StringBuilder(PduPart.P_Q);
        str.append(" callId: ").append(getTelecomCallId());
        if (Rlog.isLoggable(LOG_TAG, 3)) {
            str.append("addr: XXXX").append(" pres.: ").append(getNumberPresentation()).append(" dial: XXXX").append(" postdial: ").append(getRemainingPostDialString()).append(" cnap name: ").append(getCnapName()).append("(").append(getCnapNamePresentation()).append(")");
        }
        str.append(" incoming: ").append(isIncoming()).append(" state: ").append(getState()).append(" post dial state: ").append(getPostDialState());
        return str.toString();
    }
}
