package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.SystemClock;
import com.android.internal.telephony.Call;
import java.util.List;

public class GsmCdmaCall extends Call {
    private static final long min_ringing_interval = 5000;
    private long lastChangeTime;
    private String lastRingNumber;
    GsmCdmaCallTracker mOwner;

    public GsmCdmaCall(GsmCdmaCallTracker owner) {
        this.mOwner = owner;
    }

    @Override // com.android.internal.telephony.Call
    public List<Connection> getConnections() {
        return this.mConnections;
    }

    @Override // com.android.internal.telephony.Call
    public Phone getPhone() {
        return this.mOwner.getPhone();
    }

    @Override // com.android.internal.telephony.Call
    public boolean isMultiparty() {
        return this.mConnections.size() > 1;
    }

    @Override // com.android.internal.telephony.Call
    public void hangup() throws CallStateException {
        this.mOwner.hangup(this);
    }

    public String toString() {
        return this.mState.toString();
    }

    public void attach(Connection conn, DriverCall dc) {
        this.mConnections.add(conn);
        this.mState = stateFromDCState(dc.state);
    }

    @UnsupportedAppUsage
    public void attachFake(Connection conn, Call.State state) {
        this.mConnections.add(conn);
        this.mState = state;
    }

    public boolean connectionDisconnected(GsmCdmaConnection conn) {
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
        return true;
    }

    public void detach(GsmCdmaConnection conn) {
        this.mConnections.remove(conn);
        if (this.mConnections.size() == 0) {
            this.mState = Call.State.IDLE;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean update(GsmCdmaConnection conn, DriverCall dc) {
        Call.State newState = stateFromDCState(dc.state);
        if (newState == this.mState) {
            return false;
        }
        this.mState = newState;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isFull() {
        return this.mConnections.size() == this.mOwner.getMaxConnectionsPerCall();
    }

    /* access modifiers changed from: package-private */
    public void onHangupLocal() {
        int s = this.mConnections.size();
        for (int i = 0; i < s; i++) {
            ((GsmCdmaConnection) this.mConnections.get(i)).onHangupLocal();
        }
        this.mState = Call.State.DISCONNECTING;
    }

    public void setLastRingNumberAndChangeTime(String number) {
        this.lastChangeTime = SystemClock.uptimeMillis();
        this.lastRingNumber = number;
    }

    public boolean isTooFrequency(String number) {
        return number != null && number.equals(this.lastRingNumber) && SystemClock.uptimeMillis() < this.lastChangeTime + min_ringing_interval;
    }
}
