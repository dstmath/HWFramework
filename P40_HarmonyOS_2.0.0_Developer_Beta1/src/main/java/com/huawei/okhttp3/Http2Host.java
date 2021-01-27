package com.huawei.okhttp3;

import com.huawei.android.telephony.SignalStrengthEx;
import com.huawei.okhttp3.internal.connection.RealConnection;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public final class Http2Host {
    private final Address addressInfo;
    private final List<RealConnection> connections = new ArrayList();

    public Http2Host(Address addressInfo2) {
        this.addressInfo = addressInfo2;
    }

    public Address address() {
        return this.addressInfo;
    }

    public void addConnection(RealConnection connection) {
        if (connection != null && !this.connections.contains(connection)) {
            this.connections.add(connection);
        }
    }

    public void removeConnection(RealConnection connection) {
        if (connection != null) {
            this.connections.remove(connection);
        }
    }

    public boolean isEmpty() {
        return this.connections.isEmpty();
    }

    public RealConnection getAvailableConnection() {
        return getConnectionWithLeastAllocation();
    }

    private RealConnection getConnectionWithLeastAllocation() {
        int count;
        RealConnection connection = null;
        int minAllocationCount = SignalStrengthEx.INVALID;
        for (RealConnection aConnection : this.connections) {
            if (aConnection != null && (count = aConnection.getTransmitters().size()) < aConnection.allocationLimit() && !aConnection.getNoNewExchanges() && count < minAllocationCount) {
                connection = aConnection;
                minAllocationCount = count;
            }
        }
        return connection;
    }
}
