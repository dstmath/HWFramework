package com.android.internal.telephony.sip;

import android.content.Context;
import android.media.AudioManager;
import android.net.rtp.AudioGroup;
import android.net.sip.SipAudioCall;
import android.net.sip.SipAudioCall.Listener;
import android.net.sip.SipErrorCode;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipProfile.Builder;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.text.TextUtils;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneNotifier;
import com.huawei.internal.telephony.HwRadarUtils;
import java.text.ParseException;
import java.util.List;
import java.util.regex.Pattern;

public class SipPhone extends SipPhoneBase {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "SipPhone";
    private static final int TIMEOUT_ANSWER_CALL = 8;
    private static final int TIMEOUT_HOLD_CALL = 15;
    private static final long TIMEOUT_HOLD_PROCESSING = 1000;
    private static final int TIMEOUT_MAKE_CALL = 15;
    private static final boolean VDBG = false;
    private SipCall mBackgroundCall = new SipCall(this, null);
    private SipCall mForegroundCall = new SipCall(this, null);
    private SipProfile mProfile;
    private SipCall mRingingCall = new SipCall(this, null);
    private SipManager mSipManager;
    private long mTimeOfLastValidHoldRequest = System.currentTimeMillis();

    private abstract class SipAudioCallAdapter extends Listener {
        private static final boolean SACA_DBG = true;
        private static final String SACA_TAG = "SipAudioCallAdapter";

        /* synthetic */ SipAudioCallAdapter(SipPhone this$0, SipAudioCallAdapter -this1) {
            this();
        }

        protected abstract void onCallEnded(int i);

        protected abstract void onError(int i);

        private SipAudioCallAdapter() {
        }

        public void onCallEnded(SipAudioCall call) {
            int i;
            log("onCallEnded: call=" + call);
            if (call.isInCall()) {
                i = 2;
            } else {
                i = 1;
            }
            onCallEnded(i);
        }

        public void onCallBusy(SipAudioCall call) {
            log("onCallBusy: call=" + call);
            onCallEnded(4);
        }

        public void onError(SipAudioCall call, int errorCode, String errorMessage) {
            log("onError: call=" + call + " code=" + SipErrorCode.toString(errorCode) + ": " + errorMessage);
            switch (errorCode) {
                case -12:
                    onError(9);
                    return;
                case -11:
                    onError(11);
                    return;
                case -10:
                    onError(14);
                    return;
                case -8:
                    onError(10);
                    return;
                case -7:
                    onError(8);
                    return;
                case -6:
                    onError(7);
                    return;
                case -5:
                case -3:
                    onError(13);
                    return;
                case -2:
                    onError(12);
                    return;
                default:
                    onError(36);
                    return;
            }
        }

        private void log(String s) {
            Rlog.d(SACA_TAG, s);
        }
    }

    private class SipCall extends SipCallBase {
        private static final boolean SC_DBG = true;
        private static final String SC_TAG = "SipCall";
        private static final boolean SC_VDBG = false;

        /* synthetic */ SipCall(SipPhone this$0, SipCall -this1) {
            this();
        }

        private SipCall() {
        }

        void reset() {
            log("reset");
            this.mConnections.clear();
            setState(State.IDLE);
        }

        void switchWith(SipCall that) {
            log("switchWith");
            synchronized (SipPhone.class) {
                SipCall tmp = new SipCall();
                tmp.takeOver(this);
                takeOver(that);
                that.takeOver(tmp);
            }
        }

        private void takeOver(SipCall that) {
            log("takeOver");
            this.mConnections = that.mConnections;
            this.mState = that.mState;
            for (Connection c : this.mConnections) {
                ((SipConnection) c).changeOwner(this);
            }
        }

        public Phone getPhone() {
            return SipPhone.this;
        }

        public List<Connection> getConnections() {
            List list;
            synchronized (SipPhone.class) {
                list = this.mConnections;
            }
            return list;
        }

        Connection dial(String originalNumber) throws SipException {
            log("dial: num=" + "xxx");
            String calleeSipUri = originalNumber;
            if (!originalNumber.contains("@")) {
                calleeSipUri = SipPhone.this.mProfile.getUriString().replaceFirst(Pattern.quote(SipPhone.this.mProfile.getUserName() + "@"), originalNumber + "@");
            }
            try {
                SipConnection c = new SipConnection(this, new Builder(calleeSipUri).build(), originalNumber);
                c.dial();
                this.mConnections.add(c);
                setState(State.DIALING);
                return c;
            } catch (ParseException e) {
                throw new SipException("dial", e);
            }
        }

        public void hangup() throws CallStateException {
            synchronized (SipPhone.class) {
                if (this.mState.isAlive()) {
                    log("hangup: call " + getState() + ": " + this + " on phone " + getPhone());
                    setState(State.DISCONNECTING);
                    CallStateException excp = null;
                    for (Connection c : this.mConnections) {
                        try {
                            c.hangup();
                        } catch (CallStateException e) {
                            excp = e;
                        }
                    }
                    if (excp != null) {
                        throw excp;
                    }
                } else {
                    log("hangup: dead call " + getState() + ": " + this + " on phone " + getPhone());
                }
            }
        }

        SipConnection initIncomingCall(SipAudioCall sipAudioCall, boolean makeCallWait) {
            SipConnection c = new SipConnection(SipPhone.this, this, sipAudioCall.getPeerProfile());
            this.mConnections.add(c);
            State newState = makeCallWait ? State.WAITING : State.INCOMING;
            c.initIncomingCall(sipAudioCall, newState);
            setState(newState);
            SipPhone.this.notifyNewRingingConnectionP(c);
            return c;
        }

        void rejectCall() throws CallStateException {
            log("rejectCall:");
            hangup();
        }

        void acceptCall() throws CallStateException {
            log("acceptCall: accepting");
            if (this != SipPhone.this.mRingingCall) {
                throw new CallStateException("acceptCall() in a non-ringing call");
            } else if (this.mConnections.size() != 1) {
                throw new CallStateException("acceptCall() in a conf call");
            } else {
                ((SipConnection) this.mConnections.get(0)).acceptCall();
            }
        }

        private boolean isSpeakerOn() {
            return Boolean.valueOf(((AudioManager) SipPhone.this.mContext.getSystemService("audio")).isSpeakerphoneOn()).booleanValue();
        }

        void setAudioGroupMode() {
            AudioGroup audioGroup = getAudioGroup();
            if (audioGroup == null) {
                log("setAudioGroupMode: audioGroup == null ignore");
                return;
            }
            int mode = audioGroup.getMode();
            if (this.mState == State.HOLDING) {
                audioGroup.setMode(0);
            } else if (getMute()) {
                audioGroup.setMode(1);
            } else if (isSpeakerOn()) {
                audioGroup.setMode(3);
            } else {
                audioGroup.setMode(2);
            }
            log(String.format("setAudioGroupMode change: %d --> %d", new Object[]{Integer.valueOf(mode), Integer.valueOf(audioGroup.getMode())}));
        }

        void hold() throws CallStateException {
            log("hold:");
            setState(State.HOLDING);
            for (Connection c : this.mConnections) {
                ((SipConnection) c).hold();
            }
            setAudioGroupMode();
        }

        void unhold() throws CallStateException {
            log("unhold:");
            setState(State.ACTIVE);
            AudioGroup audioGroup = new AudioGroup();
            for (Connection c : this.mConnections) {
                ((SipConnection) c).unhold(audioGroup);
            }
            setAudioGroupMode();
        }

        void setMute(boolean muted) {
            log("setMute: muted=" + muted);
            for (Connection c : this.mConnections) {
                ((SipConnection) c).setMute(muted);
            }
        }

        boolean getMute() {
            boolean ret;
            if (this.mConnections.isEmpty()) {
                ret = false;
            } else {
                ret = ((SipConnection) this.mConnections.get(0)).getMute();
            }
            log("getMute: ret=" + ret);
            return ret;
        }

        void merge(SipCall that) throws CallStateException {
            log("merge:");
            AudioGroup audioGroup = getAudioGroup();
            for (Connection c : (Connection[]) that.mConnections.toArray(new Connection[that.mConnections.size()])) {
                SipConnection conn = (SipConnection) c;
                add(conn);
                if (conn.getState() == State.HOLDING) {
                    conn.unhold(audioGroup);
                }
            }
            that.setState(State.IDLE);
        }

        private void add(SipConnection conn) {
            log("add:");
            SipCall call = conn.getCall();
            if (call != this) {
                if (call != null) {
                    call.mConnections.remove(conn);
                }
                this.mConnections.add(conn);
                conn.changeOwner(this);
            }
        }

        void sendDtmf(char c) {
            log("sendDtmf: c=" + c);
            AudioGroup audioGroup = getAudioGroup();
            if (audioGroup == null) {
                log("sendDtmf: audioGroup == null, ignore c=" + c);
            } else {
                audioGroup.sendDtmf(convertDtmf(c));
            }
        }

        private int convertDtmf(char c) {
            int code = c - 48;
            if (code >= 0 && code <= 9) {
                return code;
            }
            switch (c) {
                case '#':
                    return 11;
                case '*':
                    return 10;
                case 'A':
                    return 12;
                case 'B':
                    return 13;
                case HwRadarUtils.RADAR_LEVEL_C /*67*/:
                    return 14;
                case 'D':
                    return 15;
                default:
                    throw new IllegalArgumentException("invalid DTMF char: " + c);
            }
        }

        protected void setState(State newState) {
            if (this.mState != newState) {
                log("setState: cur state" + this.mState + " --> " + newState + ": " + this + ": on phone " + getPhone() + " " + this.mConnections.size());
                if (newState == State.ALERTING) {
                    this.mState = newState;
                    SipPhone.this.startRingbackTone();
                } else if (this.mState == State.ALERTING) {
                    SipPhone.this.stopRingbackTone();
                }
                this.mState = newState;
                SipPhone.this.updatePhoneState();
                SipPhone.this.notifyPreciseCallStateChanged();
            }
        }

        void onConnectionStateChanged(SipConnection conn) {
            log("onConnectionStateChanged: conn=" + conn);
            if (this.mState != State.ACTIVE) {
                setState(conn.getState());
            }
        }

        void onConnectionEnded(SipConnection conn) {
            log("onConnectionEnded: conn=" + conn);
            if (this.mState != State.DISCONNECTED) {
                boolean allConnectionsDisconnected = true;
                log("---check connections: " + this.mConnections.size());
                for (Connection c : this.mConnections) {
                    log("   state=" + c.getState() + ": " + c);
                    if (c.getState() != State.DISCONNECTED) {
                        allConnectionsDisconnected = false;
                        break;
                    }
                }
                if (allConnectionsDisconnected) {
                    setState(State.DISCONNECTED);
                }
            }
            SipPhone.this.-wrap2(conn);
        }

        private AudioGroup getAudioGroup() {
            if (this.mConnections.isEmpty()) {
                return null;
            }
            return ((SipConnection) this.mConnections.get(0)).getAudioGroup();
        }

        private void log(String s) {
            Rlog.d(SC_TAG, s);
        }
    }

    private class SipConnection extends SipConnectionBase {
        private static final boolean SCN_DBG = true;
        private static final String SCN_TAG = "SipConnection";
        private SipAudioCallAdapter mAdapter;
        private boolean mIncoming;
        private String mOriginalNumber;
        private SipCall mOwner;
        private SipProfile mPeer;
        private SipAudioCall mSipAudioCall;
        private State mState;

        public SipConnection(SipCall owner, SipProfile callee, String originalNumber) {
            super(originalNumber);
            this.mState = State.IDLE;
            this.mIncoming = false;
            this.mAdapter = new SipAudioCallAdapter(SipPhone.this) {
                protected void onCallEnded(int cause) {
                    if (SipConnection.this.getDisconnectCause() != 3) {
                        SipConnection.this.setDisconnectCause(cause);
                    }
                    synchronized (SipPhone.class) {
                        String sessionState;
                        SipConnection.this.setState(State.DISCONNECTED);
                        SipAudioCall sipAudioCall = SipConnection.this.mSipAudioCall;
                        SipConnection.this.mSipAudioCall = null;
                        if (sipAudioCall == null) {
                            sessionState = "";
                        } else {
                            sessionState = sipAudioCall.getState() + ", ";
                        }
                        SipConnection.this.log("[SipAudioCallAdapter] onCallEnded: " + SipPhone.hidePii(SipConnection.this.mPeer.getUriString()) + ": " + sessionState + "cause: " + SipConnection.this.getDisconnectCause() + ", on phone " + SipConnection.this.getPhone());
                        if (sipAudioCall != null) {
                            sipAudioCall.setListener(null);
                            sipAudioCall.close();
                        }
                        SipConnection.this.mOwner.onConnectionEnded(SipConnection.this);
                    }
                }

                public void onCallEstablished(SipAudioCall call) {
                    onChanged(call);
                    if (SipConnection.this.mState == State.ACTIVE) {
                        call.startAudio();
                    }
                }

                public void onCallHeld(SipAudioCall call) {
                    onChanged(call);
                    if (SipConnection.this.mState == State.HOLDING) {
                        call.startAudio();
                    }
                }

                public void onChanged(SipAudioCall call) {
                    synchronized (SipPhone.class) {
                        State newState = SipPhone.getCallStateFrom(call);
                        if (SipConnection.this.mState == newState) {
                            return;
                        }
                        if (newState == State.INCOMING) {
                            SipConnection.this.setState(SipConnection.this.mOwner.getState());
                        } else {
                            if (SipConnection.this.mOwner == SipPhone.this.mRingingCall) {
                                if (SipPhone.this.mRingingCall.getState() == State.WAITING) {
                                    try {
                                        SipPhone.this.switchHoldingAndActive();
                                    } catch (CallStateException e) {
                                        onCallEnded(3);
                                        return;
                                    }
                                }
                                SipPhone.this.mForegroundCall.switchWith(SipPhone.this.mRingingCall);
                            }
                            SipConnection.this.setState(newState);
                        }
                        SipConnection.this.mOwner.onConnectionStateChanged(SipConnection.this);
                        SipConnection.this.log("onChanged: " + SipPhone.hidePii(SipConnection.this.mPeer.getUriString()) + ": " + SipConnection.this.mState + " on phone " + SipConnection.this.getPhone());
                    }
                }

                protected void onError(int cause) {
                    SipConnection.this.log("onError: " + cause);
                    onCallEnded(cause);
                }
            };
            this.mOwner = owner;
            this.mPeer = callee;
            this.mOriginalNumber = originalNumber;
        }

        public SipConnection(SipPhone this$0, SipCall owner, SipProfile callee) {
            this(owner, callee, this$0.getUriString(callee));
        }

        public String getCnapName() {
            String displayName = this.mPeer.getDisplayName();
            return TextUtils.isEmpty(displayName) ? null : displayName;
        }

        public int getNumberPresentation() {
            return 1;
        }

        void initIncomingCall(SipAudioCall sipAudioCall, State newState) {
            setState(newState);
            this.mSipAudioCall = sipAudioCall;
            sipAudioCall.setListener(this.mAdapter);
            this.mIncoming = true;
        }

        void acceptCall() throws CallStateException {
            try {
                this.mSipAudioCall.answerCall(8);
            } catch (SipException e) {
                throw new CallStateException("acceptCall(): " + e);
            }
        }

        void changeOwner(SipCall owner) {
            this.mOwner = owner;
        }

        AudioGroup getAudioGroup() {
            if (this.mSipAudioCall == null) {
                return null;
            }
            return this.mSipAudioCall.getAudioGroup();
        }

        void dial() throws SipException {
            setState(State.DIALING);
            this.mSipAudioCall = SipPhone.this.mSipManager.makeAudioCall(SipPhone.this.mProfile, this.mPeer, null, 15);
            this.mSipAudioCall.setListener(this.mAdapter);
        }

        void hold() throws CallStateException {
            setState(State.HOLDING);
            try {
                this.mSipAudioCall.holdCall(15);
            } catch (SipException e) {
                throw new CallStateException("hold(): " + e);
            }
        }

        void unhold(AudioGroup audioGroup) throws CallStateException {
            this.mSipAudioCall.setAudioGroup(audioGroup);
            setState(State.ACTIVE);
            try {
                this.mSipAudioCall.continueCall(15);
            } catch (SipException e) {
                throw new CallStateException("unhold(): " + e);
            }
        }

        void setMute(boolean muted) {
            if (this.mSipAudioCall != null && muted != this.mSipAudioCall.isMuted()) {
                log("setState: prev muted=" + (muted ^ 1) + " new muted=" + muted);
                this.mSipAudioCall.toggleMute();
            }
        }

        boolean getMute() {
            if (this.mSipAudioCall == null) {
                return false;
            }
            return this.mSipAudioCall.isMuted();
        }

        protected void setState(State state) {
            if (state != this.mState) {
                super.setState(state);
                this.mState = state;
            }
        }

        public State getState() {
            return this.mState;
        }

        public boolean isIncoming() {
            return this.mIncoming;
        }

        public String getAddress() {
            return this.mOriginalNumber;
        }

        public SipCall getCall() {
            return this.mOwner;
        }

        protected Phone getPhone() {
            return this.mOwner.getPhone();
        }

        public void hangup() throws CallStateException {
            int i = 3;
            synchronized (SipPhone.class) {
                log("hangup: conn=" + SipPhone.hidePii(this.mPeer.getUriString()) + ": " + this.mState + ": on phone " + getPhone().getPhoneName());
                if (this.mState.isAlive()) {
                    try {
                        SipAudioCall sipAudioCall = this.mSipAudioCall;
                        if (sipAudioCall != null) {
                            sipAudioCall.setListener(null);
                            sipAudioCall.endCall();
                        }
                        SipAudioCallAdapter sipAudioCallAdapter = this.mAdapter;
                        if (this.mState == State.INCOMING || this.mState == State.WAITING) {
                            i = 16;
                        }
                        sipAudioCallAdapter.onCallEnded(i);
                    } catch (SipException e) {
                        throw new CallStateException("hangup(): " + e);
                    } catch (Throwable th) {
                        SipAudioCallAdapter sipAudioCallAdapter2 = this.mAdapter;
                        if (this.mState == State.INCOMING || this.mState == State.WAITING) {
                            i = 16;
                        }
                        sipAudioCallAdapter2.onCallEnded(i);
                    }
                }
            }
        }

        public void separate() throws CallStateException {
            synchronized (SipPhone.class) {
                SipCall call;
                if (getPhone() == SipPhone.this) {
                    call = (SipCall) SipPhone.this.getBackgroundCall();
                } else {
                    call = (SipCall) SipPhone.this.getForegroundCall();
                }
                if (call.getState() != State.IDLE) {
                    throw new CallStateException("cannot put conn back to a call in non-idle state: " + call.getState());
                }
                log("separate: conn=" + this.mPeer.getUriString() + " from " + this.mOwner + " back to " + call);
                Phone originalPhone = getPhone();
                AudioGroup audioGroup = call.getAudioGroup();
                call.add(this);
                this.mSipAudioCall.setAudioGroup(audioGroup);
                originalPhone.switchHoldingAndActive();
                call = (SipCall) SipPhone.this.getForegroundCall();
                this.mSipAudioCall.startAudio();
                call.onConnectionStateChanged(this);
            }
        }

        private void log(String s) {
            Rlog.d(SCN_TAG, s);
        }
    }

    SipPhone(Context context, PhoneNotifier notifier, SipProfile profile) {
        super("SIP:" + profile.getUriString(), context, notifier);
        log("new SipPhone: " + hidePii(profile.getUriString()));
        this.mRingingCall = new SipCall(this, null);
        this.mForegroundCall = new SipCall(this, null);
        this.mBackgroundCall = new SipCall(this, null);
        this.mProfile = profile;
        this.mSipManager = SipManager.newInstance(context);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SipPhone)) {
            return false;
        }
        return this.mProfile.getUriString().equals(((SipPhone) o).mProfile.getUriString());
    }

    public String getSipUri() {
        return this.mProfile.getUriString();
    }

    public boolean equals(SipPhone phone) {
        return getSipUri().equals(phone.getSipUri());
    }

    /* JADX WARNING: Missing block: B:30:0x00a3, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Connection takeIncomingCall(Object incomingCall) {
        synchronized (SipPhone.class) {
            if (!(incomingCall instanceof SipAudioCall)) {
                log("takeIncomingCall: ret=null, not a SipAudioCall");
                return null;
            } else if (this.mRingingCall.getState().isAlive()) {
                log("takeIncomingCall: ret=null, ringingCall not alive");
                return null;
            } else if (this.mForegroundCall.getState().isAlive() && this.mBackgroundCall.getState().isAlive()) {
                log("takeIncomingCall: ret=null, foreground and background both alive");
                return null;
            } else {
                try {
                    SipAudioCall sipAudioCall = (SipAudioCall) incomingCall;
                    log("takeIncomingCall: taking call from: " + hidePii(sipAudioCall.getPeerProfile().getUriString()));
                    if (sipAudioCall.getLocalProfile().getUriString().equals(this.mProfile.getUriString())) {
                        Connection connection = this.mRingingCall.initIncomingCall(sipAudioCall, this.mForegroundCall.getState().isAlive());
                        if (sipAudioCall.getState() != 3) {
                            log("    takeIncomingCall: call cancelled !!");
                            this.mRingingCall.reset();
                            connection = null;
                        }
                    }
                } catch (Exception e) {
                    log("    takeIncomingCall: exception e=" + e);
                    this.mRingingCall.reset();
                }
                log("takeIncomingCall: NOT taking !!");
                return null;
            }
        }
    }

    public void acceptCall(int videoState) throws CallStateException {
        synchronized (SipPhone.class) {
            if (this.mRingingCall.getState() == State.INCOMING || this.mRingingCall.getState() == State.WAITING) {
                log("acceptCall: accepting");
                this.mRingingCall.setMute(false);
                this.mRingingCall.acceptCall();
            } else {
                log("acceptCall: throw CallStateException(\"phone not ringing\")");
                throw new CallStateException("phone not ringing");
            }
        }
    }

    public void rejectCall() throws CallStateException {
        synchronized (SipPhone.class) {
            if (this.mRingingCall.getState().isRinging()) {
                log("rejectCall: rejecting");
                this.mRingingCall.rejectCall();
            } else {
                log("rejectCall: throw CallStateException(\"phone not ringing\")");
                throw new CallStateException("phone not ringing");
            }
        }
    }

    public Connection dial(String dialString, int videoState) throws CallStateException {
        Connection dialInternal;
        synchronized (SipPhone.class) {
            dialInternal = dialInternal(dialString, videoState);
        }
        return dialInternal;
    }

    private Connection dialInternal(String dialString, int videoState) throws CallStateException {
        log("dialInternal: dialString=" + hidePii(dialString));
        clearDisconnected();
        if (canDial()) {
            if (this.mForegroundCall.getState() == State.ACTIVE) {
                switchHoldingAndActive();
            }
            if (this.mForegroundCall.getState() != State.IDLE) {
                throw new CallStateException("cannot dial in current state");
            }
            this.mForegroundCall.setMute(false);
            try {
                return this.mForegroundCall.dial(dialString);
            } catch (SipException e) {
                loge("dialInternal: ", e);
                throw new CallStateException("dial error: " + e);
            }
        }
        throw new CallStateException("dialInternal: cannot dial in current state");
    }

    public void switchHoldingAndActive() throws CallStateException {
        if (isHoldTimeoutExpired()) {
            log("switchHoldingAndActive: switch fg and bg");
            synchronized (SipPhone.class) {
                this.mForegroundCall.switchWith(this.mBackgroundCall);
                if (this.mBackgroundCall.getState().isAlive()) {
                    this.mBackgroundCall.hold();
                }
                if (this.mForegroundCall.getState().isAlive()) {
                    this.mForegroundCall.unhold();
                }
            }
            return;
        }
        log("switchHoldingAndActive: Disregarded! Under 1000 ms...");
    }

    public boolean canConference() {
        log("canConference: ret=true");
        return true;
    }

    public void conference() throws CallStateException {
        synchronized (SipPhone.class) {
            if (this.mForegroundCall.getState() == State.ACTIVE && this.mForegroundCall.getState() == State.ACTIVE) {
                log("conference: merge fg & bg");
                this.mForegroundCall.merge(this.mBackgroundCall);
            } else {
                throw new CallStateException("wrong state to merge calls: fg=" + this.mForegroundCall.getState() + ", bg=" + this.mBackgroundCall.getState());
            }
        }
    }

    public void conference(Call that) throws CallStateException {
        synchronized (SipPhone.class) {
            if (that instanceof SipCall) {
                this.mForegroundCall.merge((SipCall) that);
            } else {
                throw new CallStateException("expect " + SipCall.class + ", cannot merge with " + that.getClass());
            }
        }
    }

    public boolean canTransfer() {
        return false;
    }

    public void explicitCallTransfer() {
    }

    public void clearDisconnected() {
        synchronized (SipPhone.class) {
            this.mRingingCall.clearDisconnected();
            this.mForegroundCall.clearDisconnected();
            this.mBackgroundCall.clearDisconnected();
            updatePhoneState();
            notifyPreciseCallStateChanged();
        }
    }

    public void sendDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            loge("sendDtmf called with invalid character '" + c + "'");
        } else if (this.mForegroundCall.getState().isAlive()) {
            synchronized (SipPhone.class) {
                this.mForegroundCall.sendDtmf(c);
            }
        }
    }

    public void startDtmf(char c) {
        if (PhoneNumberUtils.is12Key(c)) {
            sendDtmf(c);
        } else {
            loge("startDtmf called with invalid character '" + c + "'");
        }
    }

    public void stopDtmf() {
    }

    public void sendBurstDtmf(String dtmfString) {
        loge("sendBurstDtmf() is a CDMA method");
    }

    public void getOutgoingCallerIdDisplay(Message onComplete) {
        AsyncResult.forMessage(onComplete, null, null);
        onComplete.sendToTarget();
    }

    public void setOutgoingCallerIdDisplay(int commandInterfaceCLIRMode, Message onComplete) {
        AsyncResult.forMessage(onComplete, null, null);
        onComplete.sendToTarget();
    }

    public void getCallWaiting(Message onComplete) {
        AsyncResult.forMessage(onComplete, null, null);
        onComplete.sendToTarget();
    }

    public void setCallWaiting(boolean enable, Message onComplete) {
        loge("call waiting not supported");
    }

    public void setEchoSuppressionEnabled() {
        synchronized (SipPhone.class) {
            if (((AudioManager) this.mContext.getSystemService("audio")).getParameters("ec_supported").contains("off")) {
                this.mForegroundCall.setAudioGroupMode();
            }
        }
    }

    public void setMute(boolean muted) {
        synchronized (SipPhone.class) {
            this.mForegroundCall.setMute(muted);
        }
    }

    public boolean getMute() {
        if (this.mForegroundCall.getState().isAlive()) {
            return this.mForegroundCall.getMute();
        }
        return this.mBackgroundCall.getMute();
    }

    public Call getForegroundCall() {
        return this.mForegroundCall;
    }

    public Call getBackgroundCall() {
        return this.mBackgroundCall;
    }

    public Call getRingingCall() {
        return this.mRingingCall;
    }

    public ServiceState getServiceState() {
        return super.getServiceState();
    }

    private String getUriString(SipProfile p) {
        return p.getUserName() + "@" + getSipDomain(p);
    }

    private String getSipDomain(SipProfile p) {
        String domain = p.getSipDomain();
        if (domain.endsWith(":5060")) {
            return domain.substring(0, domain.length() - 5);
        }
        return domain;
    }

    private static State getCallStateFrom(SipAudioCall sipAudioCall) {
        if (sipAudioCall.isOnHold()) {
            return State.HOLDING;
        }
        int sessionState = sipAudioCall.getState();
        switch (sessionState) {
            case 0:
                return State.IDLE;
            case 3:
            case 4:
                return State.INCOMING;
            case 5:
                return State.DIALING;
            case 6:
                return State.ALERTING;
            case 7:
                return State.DISCONNECTING;
            case 8:
                return State.ACTIVE;
            default:
                slog("illegal connection state: " + sessionState);
                return State.DISCONNECTED;
        }
    }

    private synchronized boolean isHoldTimeoutExpired() {
        long currTime = System.currentTimeMillis();
        if (currTime - this.mTimeOfLastValidHoldRequest <= TIMEOUT_HOLD_PROCESSING) {
            return false;
        }
        this.mTimeOfLastValidHoldRequest = currTime;
        return true;
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private static void slog(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }

    private void loge(String s, Exception e) {
        Rlog.e(LOG_TAG, s, e);
    }

    public static String hidePii(String s) {
        return "xxxxx";
    }
}
