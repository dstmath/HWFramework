package com.huawei.okhttp3;

import com.huawei.android.telephony.SignalStrengthEx;
import com.huawei.okhttp3.internal.connection.RealConnection;
import java.util.ArrayList;
import java.util.List;

public final class Http2Host {
    private final Address address;
    private final List<RealConnection> connections = new ArrayList();
    private int searchIndex = 0;

    public Http2Host(Address address2) {
        this.address = address2;
    }

    public Address address() {
        return this.address;
    }

    public void addConnection(RealConnection connection) {
        if (!this.connections.contains(connection)) {
            this.connections.add(connection);
        }
    }

    public void removeConnection(RealConnection connection) {
        this.connections.remove(connection);
    }

    public boolean isEmpty() {
        return this.connections.isEmpty();
    }

    public RealConnection getAvailableConnection() {
        return getConnectionWithLeastAllocation();
    }

    @Deprecated
    private RealConnection getConnectionRoundRobin() {
        if (this.connections.isEmpty()) {
            return null;
        }
        if (this.searchIndex >= this.connections.size()) {
            this.searchIndex = 0;
        }
        int connectionSize = this.connections.size();
        for (int i = this.searchIndex; i < connectionSize; i++) {
            RealConnection connection = this.connections.get(i);
            if (connection.allocations.size() < connection.allocationLimit && !connection.noNewStreams) {
                this.searchIndex++;
                return connection;
            }
        }
        for (int i2 = 0; i2 < this.searchIndex; i2++) {
            RealConnection connection2 = this.connections.get(i2);
            if (connection2.allocations.size() < connection2.allocationLimit && !connection2.noNewStreams) {
                this.searchIndex++;
                return connection2;
            }
        }
        return null;
    }

    private RealConnection getConnectionWithLeastAllocation() {
        RealConnection connection = null;
        int minAllocationCount = SignalStrengthEx.INVALID;
        for (RealConnection c : this.connections) {
            int count = c.allocations.size();
            if (count < c.allocationLimit && !c.noNewStreams && count < minAllocationCount) {
                connection = c;
                minAllocationCount = count;
            }
        }
        return connection;
    }
}
