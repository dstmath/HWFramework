package com.android.internal.telephony.imsphone;

import android.telecom.ConferenceParticipant;
import android.telephony.Rlog;
import com.android.ims.ImsCall;
import com.android.ims.ImsException;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import java.util.List;

public class ImsPhoneCall extends Call {
    public static final String CONTEXT_BACKGROUND = "BG";
    public static final String CONTEXT_FOREGROUND = "FG";
    public static final String CONTEXT_HANDOVER = "HO";
    public static final String CONTEXT_RINGING = "RG";
    public static final String CONTEXT_UNKNOWN = "UK";
    private static final boolean DBG = false;
    private static final boolean FORCE_DEBUG = false;
    private static final String LOG_TAG = "ImsPhoneCall";
    private static final boolean VDBG = false;
    private final String mCallContext;
    ImsPhoneCallTracker mOwner;
    private boolean mRingbackTonePlayed;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.imsphone.ImsPhoneCall.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.imsphone.ImsPhoneCall.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsPhoneCall.<clinit>():void");
    }

    public void dispose() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Incorrect nodes count for selectOther: B:11:0x004e in [B:7:0x0040, B:11:0x004e, B:10:0x004f, B:9:0x004f]
	at jadx.core.utils.BlockUtils.selectOther(BlockUtils.java:53)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:62)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r7 = this;
        r6 = 14;
        r4 = r7.mOwner;	 Catch:{ CallStateException -> 0x001e, all -> 0x0036 }
        r4.hangup(r7);	 Catch:{ CallStateException -> 0x001e, all -> 0x0036 }
        r2 = 0;
        r4 = r7.mConnections;
        r3 = r4.size();
    L_0x000e:
        if (r2 >= r3) goto L_0x004f;
    L_0x0010:
        r4 = r7.mConnections;
        r0 = r4.get(r2);
        r0 = (com.android.internal.telephony.imsphone.ImsPhoneConnection) r0;
        r0.onDisconnect(r6);
        r2 = r2 + 1;
        goto L_0x000e;
    L_0x001e:
        r1 = move-exception;
        r2 = 0;
        r4 = r7.mConnections;
        r3 = r4.size();
    L_0x0026:
        if (r2 >= r3) goto L_0x004f;
    L_0x0028:
        r4 = r7.mConnections;
        r0 = r4.get(r2);
        r0 = (com.android.internal.telephony.imsphone.ImsPhoneConnection) r0;
        r0.onDisconnect(r6);
        r2 = r2 + 1;
        goto L_0x0026;
    L_0x0036:
        r4 = move-exception;
        r2 = 0;
        r5 = r7.mConnections;
        r3 = r5.size();
    L_0x003e:
        if (r2 >= r3) goto L_0x004e;
    L_0x0040:
        r5 = r7.mConnections;
        r0 = r5.get(r2);
        r0 = (com.android.internal.telephony.imsphone.ImsPhoneConnection) r0;
        r0.onDisconnect(r6);
        r2 = r2 + 1;
        goto L_0x003e;
    L_0x004e:
        throw r4;
    L_0x004f:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsPhoneCall.dispose():void");
    }

    ImsPhoneCall() {
        this.mRingbackTonePlayed = FORCE_DEBUG;
        this.mCallContext = CONTEXT_UNKNOWN;
    }

    public ImsPhoneCall(ImsPhoneCallTracker owner, String context) {
        this.mRingbackTonePlayed = FORCE_DEBUG;
        this.mOwner = owner;
        this.mCallContext = context;
    }

    public List<Connection> getConnections() {
        return this.mConnections;
    }

    public Phone getPhone() {
        return this.mOwner.mPhone;
    }

    public boolean isMultiparty() {
        synchronized (ImsPhoneCall.class) {
            int s = this.mConnections.size();
            for (int i = 0; i < s; i++) {
                if (((ImsPhoneConnection) this.mConnections.get(i)).isMultiparty()) {
                    return true;
                }
            }
            return FORCE_DEBUG;
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
        for (Connection conn : this.mConnections) {
            sb.append(conn);
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

    public void attach(Connection conn, State state) {
        if (VDBG) {
            Rlog.v(LOG_TAG, "attach : " + this.mCallContext + " state = " + state.toString());
        }
        attach(conn);
        this.mState = state;
    }

    public void attachFake(Connection conn, State state) {
        attach(conn, state);
    }

    public boolean connectionDisconnected(ImsPhoneConnection conn) {
        if (this.mState != State.DISCONNECTED) {
            boolean hasOnlyDisconnectedConnections = true;
            int s = this.mConnections.size();
            for (int i = 0; i < s; i++) {
                if (((Connection) this.mConnections.get(i)).getState() != State.DISCONNECTED) {
                    hasOnlyDisconnectedConnections = FORCE_DEBUG;
                    break;
                }
            }
            if (hasOnlyDisconnectedConnections) {
                this.mState = State.DISCONNECTED;
                return true;
            }
        }
        return FORCE_DEBUG;
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

    boolean isFull() {
        return this.mConnections.size() == 5 ? true : FORCE_DEBUG;
    }

    void onHangupLocal() {
        int s = this.mConnections.size();
        for (int i = 0; i < s; i++) {
            ((ImsPhoneConnection) this.mConnections.get(i)).onHangupLocal();
        }
        this.mState = State.DISCONNECTING;
    }

    public void clearDisconnected() {
        for (int i = this.mConnections.size() - 1; i >= 0; i--) {
            ImsPhoneConnection cn = (ImsPhoneConnection) this.mConnections.get(i);
            if (cn == null || cn.getState() == State.DISCONNECTED) {
                this.mConnections.remove(i);
            }
        }
        if (this.mConnections.size() == 0) {
            this.mState = State.IDLE;
        }
    }

    ImsPhoneConnection getFirstConnection() {
        if (this.mConnections.size() == 0) {
            return null;
        }
        return (ImsPhoneConnection) this.mConnections.get(0);
    }

    void setMute(boolean mute) {
        ImsCall imsCall = null;
        if (getFirstConnection() != null) {
            imsCall = getFirstConnection().getImsCall();
        }
        if (imsCall != null) {
            try {
                imsCall.setMute(mute);
            } catch (ImsException e) {
                Rlog.e(LOG_TAG, "setMute failed : " + e.getMessage());
            }
        }
    }

    void merge(ImsPhoneCall that, State state) {
        ImsPhoneConnection imsPhoneConnection = getFirstConnection();
        if (imsPhoneConnection != null) {
            long conferenceConnectTime = imsPhoneConnection.getConferenceConnectTime();
            if (conferenceConnectTime > 0) {
                imsPhoneConnection.setConnectTime(conferenceConnectTime);
            } else if (DBG) {
                Rlog.d(LOG_TAG, "merge: conference connect time is 0");
            }
        }
        if (DBG) {
            Rlog.d(LOG_TAG, "merge(" + this.mCallContext + "): " + that + "state = " + state);
        }
    }

    public ImsCall getImsCall() {
        ImsCall imsCall = null;
        synchronized (ImsPhoneCall.class) {
            if (getFirstConnection() != null) {
                imsCall = getFirstConnection().getImsCall();
            }
        }
        return imsCall;
    }

    static boolean isLocalTone(ImsCall imsCall) {
        boolean z = FORCE_DEBUG;
        if (imsCall == null || imsCall.getCallProfile() == null || imsCall.getCallProfile().mMediaProfile == null) {
            return FORCE_DEBUG;
        }
        if (imsCall.getCallProfile().mMediaProfile.mAudioDirection == 0) {
            z = true;
        }
        return z;
    }

    public boolean update(ImsPhoneConnection conn, ImsCall imsCall, State state) {
        State newState = state;
        if (state == State.ALERTING) {
            if (this.mRingbackTonePlayed && !isLocalTone(imsCall)) {
                this.mOwner.mPhone.stopRingbackTone();
                this.mRingbackTonePlayed = FORCE_DEBUG;
            } else if (!this.mRingbackTonePlayed && isLocalTone(imsCall)) {
                this.mOwner.mPhone.startRingbackTone();
                this.mRingbackTonePlayed = true;
            }
        } else if (this.mRingbackTonePlayed) {
            this.mOwner.mPhone.stopRingbackTone();
            this.mRingbackTonePlayed = FORCE_DEBUG;
        }
        if (state != this.mState && state != State.DISCONNECTED) {
            this.mState = state;
            return true;
        } else if (state == State.DISCONNECTED) {
            return true;
        } else {
            return FORCE_DEBUG;
        }
    }

    ImsPhoneConnection getHandoverConnection() {
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
        for (Connection c : this.mConnections) {
            ((ImsPhoneConnection) c).changeParent(this);
        }
    }
}
