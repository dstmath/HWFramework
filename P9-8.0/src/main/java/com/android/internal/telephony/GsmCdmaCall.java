package com.android.internal.telephony;

import android.os.SystemClock;
import com.android.internal.telephony.Call.State;
import java.util.List;

public class GsmCdmaCall extends Call {
    private static final long min_ringing_interval = 5000;
    private long lastChangeTime;
    private String lastRingNumber;
    GsmCdmaCallTracker mOwner;

    public GsmCdmaCall(GsmCdmaCallTracker owner) {
        this.mOwner = owner;
    }

    public List<Connection> getConnections() {
        return this.mConnections;
    }

    public Phone getPhone() {
        return this.mOwner.getPhone();
    }

    public boolean isMultiparty() {
        return this.mConnections.size() > 1;
    }

    public void hangup() throws CallStateException {
        this.mOwner.hangup(this);
    }

    public String toString() {
        return this.mState.toString();
    }

    public void attach(Connection conn, DriverCall dc) {
        this.mConnections.add(conn);
        this.mState = Call.stateFromDCState(dc.state);
    }

    public void attachFake(Connection conn, State state) {
        this.mConnections.add(conn);
        this.mState = state;
    }

    public boolean connectionDisconnected(GsmCdmaConnection conn) {
        if (this.mState != State.DISCONNECTED) {
            boolean hasOnlyDisconnectedConnections = true;
            int s = this.mConnections.size();
            for (int i = 0; i < s; i++) {
                if (((Connection) this.mConnections.get(i)).getState() != State.DISCONNECTED) {
                    hasOnlyDisconnectedConnections = false;
                    break;
                }
            }
            if (hasOnlyDisconnectedConnections) {
                this.mState = State.DISCONNECTED;
                return true;
            }
        }
        return false;
    }

    public void detach(GsmCdmaConnection conn) {
        this.mConnections.remove(conn);
        if (this.mConnections.size() == 0) {
            this.mState = State.IDLE;
        }
    }

    boolean update(GsmCdmaConnection conn, DriverCall dc) {
        State newState = Call.stateFromDCState(dc.state);
        if (newState == this.mState) {
            return false;
        }
        this.mState = newState;
        return true;
    }

    boolean isFull() {
        return this.mConnections.size() == this.mOwner.getMaxConnectionsPerCall();
    }

    void onHangupLocal() {
        int s = this.mConnections.size();
        for (int i = 0; i < s; i++) {
            ((GsmCdmaConnection) this.mConnections.get(i)).onHangupLocal();
        }
        this.mState = State.DISCONNECTING;
    }

    public void setLastRingNumberAndChangeTime(String number) {
        this.lastChangeTime = SystemClock.uptimeMillis();
        this.lastRingNumber = number;
    }

    public boolean isTooFrequency(String number) {
        if (number == null || !number.equals(this.lastRingNumber) || SystemClock.uptimeMillis() >= this.lastChangeTime + min_ringing_interval) {
            return false;
        }
        return true;
    }
}
