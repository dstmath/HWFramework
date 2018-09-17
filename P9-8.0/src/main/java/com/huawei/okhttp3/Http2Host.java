package com.huawei.okhttp3;

import com.huawei.okhttp3.internal.connection.RealConnection;
import java.util.ArrayList;
import java.util.List;

public final class Http2Host {
    private final Address address;
    private final List<RealConnection> connections = new ArrayList();
    private int searchIndex = 0;

    public Http2Host(Address address) {
        this.address = address;
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
        RealConnection connection;
        if (this.searchIndex >= this.connections.size()) {
            this.searchIndex = 0;
        }
        int connectionSize = this.connections.size();
        int i = this.searchIndex;
        while (i < connectionSize) {
            connection = (RealConnection) this.connections.get(i);
            if (connection.allocations.size() >= connection.allocationLimit || (connection.noNewStreams ^ 1) == 0) {
                i++;
            } else {
                this.searchIndex++;
                return connection;
            }
        }
        i = 0;
        while (i < this.searchIndex) {
            connection = (RealConnection) this.connections.get(i);
            if (connection.allocations.size() >= connection.allocationLimit || (connection.noNewStreams ^ 1) == 0) {
                i++;
            } else {
                this.searchIndex++;
                return connection;
            }
        }
        return null;
    }

    private RealConnection getConnectionWithLeastAllocation() {
        RealConnection connection = null;
        int min_allocation_count = Integer.MAX_VALUE;
        for (RealConnection c : this.connections) {
            int count = c.allocations.size();
            if (count < c.allocationLimit && (c.noNewStreams ^ 1) != 0 && count < min_allocation_count) {
                connection = c;
                min_allocation_count = count;
            }
        }
        return connection;
    }
}
