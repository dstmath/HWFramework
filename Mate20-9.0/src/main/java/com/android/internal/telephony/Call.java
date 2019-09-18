package com.android.internal.telephony;

import android.telecom.ConferenceParticipant;
import android.telephony.Rlog;
import com.android.internal.telephony.DriverCall;
import java.util.ArrayList;
import java.util.List;

public abstract class Call {
    protected final String LOG_TAG = "Call";
    public ArrayList<Connection> mConnections = new ArrayList<>();
    public State mState = State.IDLE;

    public enum SrvccState {
        NONE,
        STARTED,
        COMPLETED,
        FAILED,
        CANCELED
    }

    public enum State {
        IDLE,
        ACTIVE,
        HOLDING,
        DIALING,
        ALERTING,
        INCOMING,
        WAITING,
        DISCONNECTED,
        DISCONNECTING;

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

    public abstract List<Connection> getConnections();

    public abstract Phone getPhone();

    public abstract void hangup() throws CallStateException;

    public abstract boolean isMultiparty();

    public static State stateFromDCState(DriverCall.State dcState) {
        switch (dcState) {
            case ACTIVE:
                return State.ACTIVE;
            case HOLDING:
                return State.HOLDING;
            case DIALING:
                return State.DIALING;
            case ALERTING:
                return State.ALERTING;
            case INCOMING:
                return State.INCOMING;
            case WAITING:
                return State.WAITING;
            default:
                throw new RuntimeException("illegal call state:" + dcState);
        }
    }

    public boolean hasConnection(Connection c) {
        return c.getCall() == this;
    }

    public boolean hasConnections() {
        List<Connection> connections = getConnections();
        boolean z = false;
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
        try {
            int s = l.size();
            for (int i = 0; i < s; i++) {
                Connection c = l.get(i);
                if (c != null) {
                    long t = c.getCreateTime();
                    if (t < time) {
                        earliest = c;
                        time = t;
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            Rlog.w("Call", "IndexOutOfBoundsException expected");
            earliest = null;
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
            Connection c = l.get(i);
            if (c != null) {
                long t = c.getCreateTime();
                time = t < time ? t : time;
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
            Connection c = l.get(i);
            if (c != null) {
                long t = c.getConnectTime();
                time = t < time ? t : time;
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
                Connection c = l.get(i);
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
            if (this.mConnections.get(i).getState() == State.DISCONNECTED) {
                this.mConnections.remove(i);
            }
        }
        if (this.mConnections.size() == 0) {
            setState(State.IDLE);
        }
    }

    /* access modifiers changed from: protected */
    public void setState(State newState) {
        this.mState = newState;
    }
}
