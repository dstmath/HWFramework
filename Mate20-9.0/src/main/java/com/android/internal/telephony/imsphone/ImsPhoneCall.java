package com.android.internal.telephony.imsphone;

import android.telecom.ConferenceParticipant;
import android.telephony.Rlog;
import com.android.ims.ImsCall;
import com.android.ims.ImsException;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import java.util.Iterator;
import java.util.List;

public class ImsPhoneCall extends Call {
    public static final String CONTEXT_BACKGROUND = "BG";
    public static final String CONTEXT_FOREGROUND = "FG";
    public static final String CONTEXT_HANDOVER = "HO";
    public static final String CONTEXT_RINGING = "RG";
    public static final String CONTEXT_UNKNOWN = "UK";
    private static final boolean DBG = Rlog.isLoggable(LOG_TAG, 3);
    private static final boolean FORCE_DEBUG = false;
    private static final String LOG_TAG = "ImsPhoneCall";
    private static final boolean VDBG = Rlog.isLoggable(LOG_TAG, 2);
    private final String mCallContext;
    ImsPhoneCallTracker mOwner;
    private boolean mRingbackTonePlayed;

    ImsPhoneCall() {
        this.mRingbackTonePlayed = false;
        this.mCallContext = CONTEXT_UNKNOWN;
    }

    public ImsPhoneCall(ImsPhoneCallTracker owner, String context) {
        this.mRingbackTonePlayed = false;
        this.mOwner = owner;
        this.mCallContext = context;
    }

    /*  JADX ERROR: StackOverflow in pass: MarkFinallyVisitor
        jadx.core.utils.exceptions.JadxOverflowException: 
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    public void dispose() {
        /*
            r5 = this;
            r0 = 14
            com.android.internal.telephony.imsphone.ImsPhoneCallTracker r1 = r5.mOwner     // Catch:{ CallStateException -> 0x0037, all -> 0x001e }
            r1.hangup((com.android.internal.telephony.imsphone.ImsPhoneCall) r5)     // Catch:{ CallStateException -> 0x0037, all -> 0x001e }
            r1 = 0
            java.util.ArrayList r2 = r5.mConnections
            int r2 = r2.size()
        L_0x000e:
            if (r1 >= r2) goto L_0x004f
            java.util.ArrayList r3 = r5.mConnections
            java.lang.Object r3 = r3.get(r1)
            com.android.internal.telephony.imsphone.ImsPhoneConnection r3 = (com.android.internal.telephony.imsphone.ImsPhoneConnection) r3
            r3.onDisconnect(r0)
            int r1 = r1 + 1
            goto L_0x000e
        L_0x001e:
            r1 = move-exception
            r2 = 0
            java.util.ArrayList r3 = r5.mConnections
            int r3 = r3.size()
        L_0x0026:
            if (r2 >= r3) goto L_0x0036
            java.util.ArrayList r4 = r5.mConnections
            java.lang.Object r4 = r4.get(r2)
            com.android.internal.telephony.imsphone.ImsPhoneConnection r4 = (com.android.internal.telephony.imsphone.ImsPhoneConnection) r4
            r4.onDisconnect(r0)
            int r2 = r2 + 1
            goto L_0x0026
        L_0x0036:
            throw r1
        L_0x0037:
            r1 = move-exception
            r1 = 0
            java.util.ArrayList r2 = r5.mConnections
            int r2 = r2.size()
        L_0x003f:
            if (r1 >= r2) goto L_0x004f
            java.util.ArrayList r3 = r5.mConnections
            java.lang.Object r3 = r3.get(r1)
            com.android.internal.telephony.imsphone.ImsPhoneConnection r3 = (com.android.internal.telephony.imsphone.ImsPhoneConnection) r3
            r3.onDisconnect(r0)
            int r1 = r1 + 1
            goto L_0x003f
        L_0x004f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsPhoneCall.dispose():void");
    }

    public List<Connection> getConnections() {
        return this.mConnections;
    }

    public Phone getPhone() {
        return this.mOwner.mPhone;
    }

    /* access modifiers changed from: protected */
    public void setState(Call.State newState) {
        super.setState(newState);
    }

    public boolean isMultiparty() {
        synchronized (ImsPhoneCall.class) {
            int s = this.mConnections.size();
            for (int i = 0; i < s; i++) {
                if (((ImsPhoneConnection) this.mConnections.get(i)).isMultiparty()) {
                    return true;
                }
            }
            return false;
        }
    }

    public void hangup() throws CallStateException {
        this.mOwner.hangup(this);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ImsPhoneCall ");
        sb.append(this.mCallContext);
        sb.append(" state: ");
        sb.append(this.mState.toString());
        sb.append(" ");
        if (this.mConnections.size() > 1) {
            sb.append(" ERROR_MULTIPLE ");
        }
        Iterator it = this.mConnections.iterator();
        while (it.hasNext()) {
            sb.append((Connection) it.next());
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

    public List<ConferenceParticipant> getConferenceParticipants() {
        ImsCall call = getImsCall();
        if (call == null) {
            return null;
        }
        return call.getConferenceParticipants();
    }

    public void attach(Connection conn) {
        if (VDBG) {
            Rlog.v(LOG_TAG, "attach : " + this.mCallContext + " conn = " + conn);
        }
        synchronized (ImsPhoneCall.class) {
            clearDisconnected();
            this.mConnections.add(conn);
        }
        this.mOwner.logState();
    }

    public void attach(Connection conn, Call.State state) {
        if (VDBG) {
            Rlog.v(LOG_TAG, "attach : " + this.mCallContext + " state = " + state.toString());
        }
        attach(conn);
        this.mState = state;
    }

    public void attachFake(Connection conn, Call.State state) {
        attach(conn, state);
    }

    public boolean connectionDisconnected(ImsPhoneConnection conn) {
        if (this.mState != Call.State.DISCONNECTED) {
            boolean hasOnlyDisconnectedConnections = true;
            int i = 0;
            int s = this.mConnections.size();
            while (true) {
                if (i >= s) {
                    break;
                } else if (((Connection) this.mConnections.get(i)).getState() != Call.State.DISCONNECTED) {
                    hasOnlyDisconnectedConnections = false;
                    break;
                } else {
                    i++;
                }
            }
            if (hasOnlyDisconnectedConnections) {
                this.mState = Call.State.DISCONNECTED;
                Rlog.v(LOG_TAG, "connectionDisconnected : " + this.mCallContext + " state = " + this.mState);
                return true;
            }
        }
        return false;
    }

    public void detach(ImsPhoneConnection conn) {
        if (VDBG) {
            Rlog.v(LOG_TAG, "detach : " + this.mCallContext + " conn = " + conn);
        }
        synchronized (ImsPhoneCall.class) {
            this.mConnections.remove(conn);
            clearDisconnected();
        }
        this.mOwner.logState();
    }

    /* access modifiers changed from: package-private */
    public boolean isFull() {
        return this.mConnections.size() == 5;
    }

    /* access modifiers changed from: package-private */
    public void onHangupLocal() {
        int s = this.mConnections.size();
        for (int i = 0; i < s; i++) {
            ((ImsPhoneConnection) this.mConnections.get(i)).onHangupLocal();
        }
        this.mState = Call.State.DISCONNECTING;
        if (VDBG) {
            Rlog.v(LOG_TAG, "onHangupLocal : " + this.mCallContext + " state = " + this.mState);
        }
    }

    public void clearDisconnected() {
        for (int i = this.mConnections.size() - 1; i >= 0; i--) {
            ImsPhoneConnection cn = (ImsPhoneConnection) this.mConnections.get(i);
            if (cn == null || cn.getState() == Call.State.DISCONNECTED) {
                this.mConnections.remove(i);
            }
        }
        if (this.mConnections.size() == 0) {
            this.mState = Call.State.IDLE;
        }
    }

    @VisibleForTesting
    public ImsPhoneConnection getFirstConnection() {
        if (this.mConnections.size() == 0) {
            return null;
        }
        return (ImsPhoneConnection) this.mConnections.get(0);
    }

    /* access modifiers changed from: package-private */
    public void setMute(boolean mute) {
        ImsCall imsCall = getFirstConnection() == null ? null : getFirstConnection().getImsCall();
        if (imsCall != null) {
            try {
                imsCall.setMute(mute);
            } catch (ImsException e) {
                Rlog.e(LOG_TAG, "setMute failed : " + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void merge(ImsPhoneCall that, Call.State state) {
        ImsPhoneConnection imsPhoneConnection = getFirstConnection();
        if (imsPhoneConnection != null) {
            long conferenceConnectTime = imsPhoneConnection.getConferenceConnectTime();
            if (conferenceConnectTime > 0) {
                imsPhoneConnection.setConnectTime(conferenceConnectTime);
                imsPhoneConnection.setConnectTimeReal(imsPhoneConnection.getConnectTimeReal());
            } else if (DBG) {
                Rlog.d(LOG_TAG, "merge: conference connect time is 0");
            }
        }
        if (DBG) {
            Rlog.d(LOG_TAG, "merge(" + this.mCallContext + "): " + that + "state = " + state);
        }
    }

    @VisibleForTesting
    public ImsCall getImsCall() {
        ImsCall imsCall;
        synchronized (ImsPhoneCall.class) {
            imsCall = getFirstConnection() == null ? null : getFirstConnection().getImsCall();
        }
        return imsCall;
    }

    static boolean isLocalTone(ImsCall imsCall) {
        boolean z = false;
        if (imsCall == null || imsCall.getCallProfile() == null || imsCall.getCallProfile().mMediaProfile == null) {
            return false;
        }
        if (imsCall.getCallProfile().mMediaProfile.mAudioDirection == 0) {
            z = true;
        }
        return z;
    }

    public boolean update(ImsPhoneConnection conn, ImsCall imsCall, Call.State state) {
        boolean changed = false;
        Call.State oldState = this.mState;
        if (state == Call.State.ALERTING) {
            if (this.mRingbackTonePlayed && !isLocalTone(imsCall)) {
                this.mOwner.mPhone.stopRingbackTone();
                this.mRingbackTonePlayed = false;
            } else if (!this.mRingbackTonePlayed && isLocalTone(imsCall)) {
                this.mOwner.mPhone.startRingbackTone();
                this.mRingbackTonePlayed = true;
            }
        } else if (this.mRingbackTonePlayed) {
            this.mOwner.mPhone.stopRingbackTone();
            this.mRingbackTonePlayed = false;
        }
        if (state != this.mState && state != Call.State.DISCONNECTED) {
            this.mState = state;
            changed = true;
        } else if (state == Call.State.DISCONNECTED) {
            changed = true;
        }
        if (VDBG) {
            Rlog.v(LOG_TAG, "update : " + this.mCallContext + " state: " + oldState + " --> " + this.mState);
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    public ImsPhoneConnection getHandoverConnection() {
        return (ImsPhoneConnection) getEarliestConnection();
    }

    public void switchWith(ImsPhoneCall that) {
        if (VDBG) {
            Rlog.v(LOG_TAG, "switchWith : switchCall = " + this + " withCall = " + that);
        }
        synchronized (ImsPhoneCall.class) {
            ImsPhoneCall tmp = new ImsPhoneCall();
            tmp.takeOver(this);
            takeOver(that);
            that.takeOver(tmp);
        }
        this.mOwner.logState();
    }

    private void takeOver(ImsPhoneCall that) {
        this.mConnections = that.mConnections;
        this.mState = that.mState;
        Iterator it = this.mConnections.iterator();
        while (it.hasNext()) {
            ((ImsPhoneConnection) ((Connection) it.next())).changeParent(this);
        }
    }
}
