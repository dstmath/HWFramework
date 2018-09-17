package org.apache.http.impl.conn.tsccm;

import java.lang.ref.ReferenceQueue;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.AbstractPoolEntry;

@Deprecated
public class BasicPoolEntry extends AbstractPoolEntry {
    private final BasicPoolEntryRef reference;

    public BasicPoolEntry(ClientConnectionOperator op, HttpRoute route, ReferenceQueue<Object> queue) {
        super(op, route);
        if (route == null) {
            throw new IllegalArgumentException("HTTP route may not be null");
        }
        this.reference = new BasicPoolEntryRef(this, queue);
    }

    protected final OperatedClientConnection getConnection() {
        return this.connection;
    }

    protected final HttpRoute getPlannedRoute() {
        return this.route;
    }

    protected final BasicPoolEntryRef getWeakRef() {
        return this.reference;
    }
}
