package com.android.internal.telephony.imsphone;

import android.annotation.UnsupportedAppUsage;
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
    private boolean mIsRingbackTonePlaying;
    ImsPhoneCallTracker mOwner;

    ImsPhoneCall() {
        this.mIsRingbackTonePlaying = false;
        this.mCallContext = CONTEXT_UNKNOWN;
    }

    public ImsPhoneCall(ImsPhoneCallTracker owner, String context) {
        this.mIsRingbackTonePlaying = false;
        this.mOwner = owner;
        this.mCallContext = context;
    }

    /*  JADX ERROR: StackOverflowError in pass: MarkFinallyVisitor
        java.lang.StackOverflowError
        	at jadx.core.dex.nodes.InsnNode.isSame(InsnNode.java:303)
        	at jadx.core.dex.instructions.IfNode.isSame(IfNode.java:122)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.sameInsns(MarkFinallyVisitor.java:451)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.compareBlocks(MarkFinallyVisitor.java:436)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.checkBlocksTree(MarkFinallyVisitor.java:408)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.checkBlocksTree(MarkFinallyVisitor.java:411)
        */
    public void dispose() {
        /*
            r5 = this;
            r0 = 14
            com.android.internal.telephony.imsphone.ImsPhoneCallTracker r1 = r5.mOwner     // Catch:{ CallStateException -> 0x0037, all -> 0x001e }
            r1.hangup(r5)     // Catch:{ CallStateException -> 0x0037, all -> 0x001e }
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

    @Override // com.android.internal.telephony.Call
    @UnsupportedAppUsage
    public List<Connection> getConnections() {
        return this.mConnections;
    }

    @Override // com.android.internal.telephony.Call
    public Phone getPhone() {
        return this.mOwner.getPhone();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.Call
    public void setState(Call.State newState) {
        super.setState(newState);
    }

    @Override // com.android.internal.telephony.Call
    public boolean isMultiparty() {
        synchronized (ImsPhoneCall.class) {
            int size = this.mConnections.size();
            for (int i = 0; i < size; i++) {
                ImsPhoneConnection conn = null;
                try {
                    conn = (ImsPhoneConnection) this.mConnections.get(i);
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "isMultiparty: IndexOutOfBounds");
                }
                if (conn != null && conn.isMultiparty()) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override // com.android.internal.telephony.Call
    @UnsupportedAppUsage
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

    @Override // com.android.internal.telephony.Call
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
        clearDisconnected();
        this.mConnections.add(conn);
        this.mOwner.logState();
    }

    @UnsupportedAppUsage
    public void attach(Connection conn, Call.State state) {
        if (VDBG) {
            Rlog.v(LOG_TAG, "attach : " + this.mCallContext + " state = " + state.toString());
        }
        attach(conn);
        this.mState = state;
    }

    @UnsupportedAppUsage
    public void attachFake(Connection conn, Call.State state) {
        attach(conn, state);
    }

    public boolean connectionDisconnected(ImsPhoneConnection conn) {
        if (this.mState == Call.State.DISCONNECTED) {
            return false;
        }
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
        if (!hasOnlyDisconnectedConnections) {
            return false;
        }
        this.mState = Call.State.DISCONNECTED;
        if (!VDBG) {
            return true;
        }
        Rlog.v(LOG_TAG, "connectionDisconnected : " + this.mCallContext + " state = " + this.mState);
        return true;
    }

    public void detach(ImsPhoneConnection conn) {
        if (VDBG) {
            Rlog.v(LOG_TAG, "detach : " + this.mCallContext + " conn = " + conn);
        }
        this.mConnections.remove(conn);
        clearDisconnected();
        this.mOwner.logState();
    }

    /* access modifiers changed from: package-private */
    public boolean isFull() {
        return this.mConnections.size() == 5;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
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

    @VisibleForTesting
    public ImsPhoneConnection getFirstConnection() {
        if (this.mConnections.size() == 0) {
            return null;
        }
        try {
            return (ImsPhoneConnection) this.mConnections.get(0);
        } catch (IndexOutOfBoundsException e) {
            Rlog.e(LOG_TAG, "getFirstConnection : IndexOutOfBounds");
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void setMute(boolean mute) {
        ImsCall imsCall = getFirstConnection() == null ? null : getFirstConnection().getImsCall();
        if (imsCall != null) {
            try {
                imsCall.setMute(mute);
            } catch (ImsException e) {
                Rlog.e(LOG_TAG, "setMute failed");
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
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
        if (getFirstConnection() == null) {
            return null;
        }
        return getFirstConnection().getImsCall();
    }

    static boolean isLocalTone(ImsCall imsCall) {
        if (imsCall == null || imsCall.getCallProfile() == null || imsCall.getCallProfile().mMediaProfile == null || imsCall.getCallProfile().mMediaProfile.mAudioDirection != 0) {
            return false;
        }
        return true;
    }

    public boolean update(ImsPhoneConnection conn, ImsCall imsCall, Call.State state) {
        boolean changed = false;
        Call.State oldState = this.mState;
        if (state == Call.State.ALERTING) {
            if (this.mIsRingbackTonePlaying && !isLocalTone(imsCall)) {
                getPhone().stopRingbackTone();
                this.mIsRingbackTonePlaying = false;
            } else if (!this.mIsRingbackTonePlaying && isLocalTone(imsCall)) {
                getPhone().startRingbackTone();
                this.mIsRingbackTonePlaying = true;
            }
        } else if (this.mIsRingbackTonePlaying) {
            getPhone().stopRingbackTone();
            this.mIsRingbackTonePlaying = false;
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
            ImsPhoneCall tmp = new ImsPhoneCall(this.mOwner, this.mCallContext);
            tmp.takeOver(this);
            takeOver(that);
            that.takeOver(tmp);
        }
        this.mOwner.logState();
    }

    public void maybeStopRingback() {
        if (this.mIsRingbackTonePlaying) {
            getPhone().stopRingbackTone();
            this.mIsRingbackTonePlaying = false;
        }
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
