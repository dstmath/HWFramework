package com.android.internal.telephony;

import android.telecom.ConferenceParticipant;
import android.telephony.Rlog;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduPersister;
import java.util.ArrayList;
import java.util.List;

public abstract class Call {
    private static final /* synthetic */ int[] -com-android-internal-telephony-DriverCall$StateSwitchesValues = null;
    protected final String LOG_TAG;
    public ArrayList<Connection> mConnections;
    public State mState;

    public enum SrvccState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.Call.SrvccState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.Call.SrvccState.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.Call.SrvccState.<clinit>():void");
        }
    }

    public enum State {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.Call.State.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.Call.State.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.Call.State.<clinit>():void");
        }

        public boolean isAlive() {
            return (this == IDLE || this == DISCONNECTED || this == DISCONNECTING) ? false : true;
        }

        public boolean isRinging() {
            return this == INCOMING || this == WAITING;
        }

        public boolean isDialing() {
            return this == DIALING || this == ALERTING;
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-DriverCall$StateSwitchesValues() {
        if (-com-android-internal-telephony-DriverCall$StateSwitchesValues != null) {
            return -com-android-internal-telephony-DriverCall$StateSwitchesValues;
        }
        int[] iArr = new int[com.android.internal.telephony.DriverCall.State.values().length];
        try {
            iArr[com.android.internal.telephony.DriverCall.State.ACTIVE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[com.android.internal.telephony.DriverCall.State.ALERTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[com.android.internal.telephony.DriverCall.State.DIALING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[com.android.internal.telephony.DriverCall.State.HOLDING.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[com.android.internal.telephony.DriverCall.State.INCOMING.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[com.android.internal.telephony.DriverCall.State.WAITING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-DriverCall$StateSwitchesValues = iArr;
        return iArr;
    }

    public abstract List<Connection> getConnections();

    public abstract Phone getPhone();

    public abstract void hangup() throws CallStateException;

    public abstract boolean isMultiparty();

    public Call() {
        this.LOG_TAG = "Call";
        this.mState = State.IDLE;
        this.mConnections = new ArrayList();
    }

    public static State stateFromDCState(com.android.internal.telephony.DriverCall.State dcState) {
        switch (-getcom-android-internal-telephony-DriverCall$StateSwitchesValues()[dcState.ordinal()]) {
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                return State.ACTIVE;
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                return State.ALERTING;
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                return State.DIALING;
            case CharacterSets.ISO_8859_1 /*4*/:
                return State.HOLDING;
            case CharacterSets.ISO_8859_2 /*5*/:
                return State.INCOMING;
            case CharacterSets.ISO_8859_3 /*6*/:
                return State.WAITING;
            default:
                throw new RuntimeException("illegal call state:" + dcState);
        }
    }

    public boolean hasConnection(Connection c) {
        return c.getCall() == this;
    }

    public boolean hasConnections() {
        boolean z = false;
        List<Connection> connections = getConnections();
        if (connections == null) {
            return false;
        }
        if (connections.size() > 0) {
            z = true;
        }
        return z;
    }

    public State getState() {
        return this.mState;
    }

    public List<ConferenceParticipant> getConferenceParticipants() {
        return null;
    }

    public boolean isIdle() {
        return !getState().isAlive();
    }

    public Connection getEarliestConnection() {
        long time = Long.MAX_VALUE;
        Connection earliest = null;
        List<Connection> l = getConnections();
        if (l.size() == 0) {
            return null;
        }
        int s = l.size();
        for (int i = 0; i < s; i++) {
            Connection c = (Connection) l.get(i);
            long t = c.getCreateTime();
            if (t < time) {
                earliest = c;
                time = t;
            }
        }
        return earliest;
    }

    public long getEarliestCreateTime() {
        long time = Long.MAX_VALUE;
        List<Connection> l = getConnections();
        if (l.size() == 0) {
            return 0;
        }
        int s = l.size();
        for (int i = 0; i < s; i++) {
            long t = ((Connection) l.get(i)).getCreateTime();
            if (t < time) {
                time = t;
            }
        }
        return time;
    }

    public long getEarliestConnectTime() {
        long time = Long.MAX_VALUE;
        List<Connection> l = getConnections();
        if (l.size() == 0) {
            return 0;
        }
        int s = l.size();
        for (int i = 0; i < s; i++) {
            long t = ((Connection) l.get(i)).getConnectTime();
            if (t < time) {
                time = t;
            }
        }
        return time;
    }

    public boolean isDialingOrAlerting() {
        return getState().isDialing();
    }

    public boolean isRinging() {
        return getState().isRinging();
    }

    public Connection getLatestConnection() {
        List<Connection> l = getConnections();
        if (l.size() == 0) {
            return null;
        }
        long time = 0;
        Connection latest = null;
        try {
            int s = l.size();
            for (int i = 0; i < s; i++) {
                Connection c = (Connection) l.get(i);
                long t = c.getCreateTime();
                if (t > time) {
                    latest = c;
                    time = t;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            latest = null;
        }
        return latest;
    }

    public void hangupIfAlive() {
        if (getState().isAlive()) {
            try {
                hangup();
            } catch (CallStateException ex) {
                Rlog.w("Call", " hangupIfActive: caught " + ex);
            }
        }
    }

    public void clearDisconnected() {
        for (int i = this.mConnections.size() - 1; i >= 0; i--) {
            if (((Connection) this.mConnections.get(i)).getState() == State.DISCONNECTED) {
                this.mConnections.remove(i);
            }
        }
        if (this.mConnections.size() == 0) {
            setState(State.IDLE);
        }
    }

    protected void setState(State newState) {
        this.mState = newState;
    }
}
