package org.apache.http.impl.conn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpConnection;

@Deprecated
public class IdleConnectionHandler {
    private final Map<HttpConnection, TimeValues> connectionToTimes = new HashMap();
    private final Log log = LogFactory.getLog((Class) getClass());

    private static class TimeValues {
        /* access modifiers changed from: private */
        public final long timeAdded;
        /* access modifiers changed from: private */
        public final long timeExpires;

        TimeValues(long now, long validDuration, TimeUnit validUnit) {
            this.timeAdded = now;
            if (validDuration > 0) {
                this.timeExpires = validUnit.toMillis(validDuration) + now;
            } else {
                this.timeExpires = Long.MAX_VALUE;
            }
        }
    }

    public void add(HttpConnection connection, long validDuration, TimeUnit unit) {
        Long timeAdded = Long.valueOf(System.currentTimeMillis());
        if (this.log.isDebugEnabled()) {
            Log log2 = this.log;
            log2.debug("Adding connection at: " + timeAdded);
        }
        Map<HttpConnection, TimeValues> map = this.connectionToTimes;
        TimeValues timeValues = new TimeValues(timeAdded.longValue(), validDuration, unit);
        map.put(connection, timeValues);
    }

    public boolean remove(HttpConnection connection) {
        TimeValues times = this.connectionToTimes.remove(connection);
        boolean z = true;
        if (times == null) {
            this.log.warn("Removing a connection that never existed!");
            return true;
        }
        if (System.currentTimeMillis() > times.timeExpires) {
            z = false;
        }
        return z;
    }

    public void removeAll() {
        this.connectionToTimes.clear();
    }

    public void closeIdleConnections(long idleTime) {
        long idleTimeout = System.currentTimeMillis() - idleTime;
        if (this.log.isDebugEnabled()) {
            Log log2 = this.log;
            log2.debug("Checking for connections, idleTimeout: " + idleTimeout);
        }
        Iterator<HttpConnection> connectionIter = this.connectionToTimes.keySet().iterator();
        while (connectionIter.hasNext()) {
            HttpConnection conn = connectionIter.next();
            Long connectionTime = Long.valueOf(this.connectionToTimes.get(conn).timeAdded);
            if (connectionTime.longValue() <= idleTimeout) {
                if (this.log.isDebugEnabled()) {
                    Log log3 = this.log;
                    log3.debug("Closing connection, connection time: " + connectionTime);
                }
                connectionIter.remove();
                try {
                    conn.close();
                } catch (IOException ex) {
                    this.log.debug("I/O error closing connection", ex);
                }
            }
        }
    }

    public void closeExpiredConnections() {
        long now = System.currentTimeMillis();
        if (this.log.isDebugEnabled()) {
            Log log2 = this.log;
            log2.debug("Checking for expired connections, now: " + now);
        }
        Iterator<HttpConnection> connectionIter = this.connectionToTimes.keySet().iterator();
        while (connectionIter.hasNext()) {
            HttpConnection conn = connectionIter.next();
            TimeValues times = this.connectionToTimes.get(conn);
            if (times.timeExpires <= now) {
                if (this.log.isDebugEnabled()) {
                    Log log3 = this.log;
                    log3.debug("Closing connection, expired @: " + times.timeExpires);
                }
                connectionIter.remove();
                try {
                    conn.close();
                } catch (IOException ex) {
                    this.log.debug("I/O error closing connection", ex);
                }
            }
        }
    }
}
