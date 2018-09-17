package com.android.okhttp;

import com.android.okhttp.internal.Platform;
import com.android.okhttp.internal.Util;
import com.squareup.okhttp.Connection;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ConnectionPool {
    private static final long DEFAULT_KEEP_ALIVE_DURATION_MS = 300000;
    private static final ConnectionPool systemDefault = null;
    private final LinkedList<Connection> connections;
    private final Runnable connectionsCleanupRunnable;
    private Executor executor;
    private final long keepAliveDurationNs;
    private final int maxIdleConnections;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.ConnectionPool.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.ConnectionPool.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.ConnectionPool.<clinit>():void");
    }

    public ConnectionPool(int maxIdleConnections, long keepAliveDurationMs) {
        this.connections = new LinkedList();
        this.executor = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), Util.threadFactory("OkHttp ConnectionPool", true));
        this.connectionsCleanupRunnable = new Runnable() {
            public void run() {
                ConnectionPool.this.runCleanupUntilPoolIsEmpty();
            }
        };
        this.maxIdleConnections = maxIdleConnections;
        this.keepAliveDurationNs = (keepAliveDurationMs * 1000) * 1000;
    }

    public static ConnectionPool getDefault() {
        return systemDefault;
    }

    public synchronized int getConnectionCount() {
        return this.connections.size();
    }

    @Deprecated
    public synchronized int getSpdyConnectionCount() {
        return getMultiplexedConnectionCount();
    }

    public synchronized int getMultiplexedConnectionCount() {
        int total;
        total = 0;
        for (Connection connection : this.connections) {
            if (connection.isFramed()) {
                total++;
            }
        }
        return total;
    }

    public synchronized int getHttpConnectionCount() {
        return this.connections.size() - getMultiplexedConnectionCount();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized Connection get(Address address) {
        Connection foundConnection;
        foundConnection = null;
        ListIterator<Connection> i = this.connections.listIterator(this.connections.size());
        while (i.hasPrevious()) {
            Connection connection = (Connection) i.previous();
            if (connection.getRoute().getAddress().equals(address) && connection.isAlive() && System.nanoTime() - connection.getIdleStartTimeNs() < this.keepAliveDurationNs) {
                i.remove();
                if (!connection.isFramed()) {
                    try {
                        Platform.get().tagSocket(connection.getSocket());
                    } catch (SocketException e) {
                        Util.closeQuietly(connection.getSocket());
                        Platform.get().logW("Unable to tagSocket(): " + e);
                    }
                }
                foundConnection = connection;
                break;
            }
        }
        if (foundConnection != null) {
            if (foundConnection.isFramed()) {
                this.connections.addFirst(foundConnection);
            }
        }
        return foundConnection;
    }

    void recycle(Connection connection) {
        if (connection.isFramed() || !connection.clearOwner()) {
            return;
        }
        if (connection.isAlive()) {
            try {
                Platform.get().untagSocket(connection.getSocket());
                synchronized (this) {
                    addConnection(connection);
                    connection.incrementRecycleCount();
                    connection.resetIdleStartTime();
                }
                return;
            } catch (SocketException e) {
                Platform.get().logW("Unable to untagSocket(): " + e);
                Util.closeQuietly(connection.getSocket());
                return;
            }
        }
        Util.closeQuietly(connection.getSocket());
    }

    private void addConnection(Connection connection) {
        boolean empty = this.connections.isEmpty();
        this.connections.addFirst(connection);
        if (empty) {
            this.executor.execute(this.connectionsCleanupRunnable);
        } else {
            notifyAll();
        }
    }

    void share(Connection connection) {
        if (!connection.isFramed()) {
            throw new IllegalArgumentException();
        } else if (connection.isAlive()) {
            synchronized (this) {
                addConnection(connection);
            }
        }
    }

    public void evictAll() {
        List<Connection> toEvict;
        synchronized (this) {
            toEvict = new ArrayList(this.connections);
            this.connections.clear();
            notifyAll();
        }
        int size = toEvict.size();
        for (int i = 0; i < size; i++) {
            Util.closeQuietly(((Connection) toEvict.get(i)).getSocket());
        }
    }

    private void runCleanupUntilPoolIsEmpty() {
        do {
        } while (performCleanup());
    }

    boolean performCleanup() {
        synchronized (this) {
            if (this.connections.isEmpty()) {
                return false;
            }
            List<Connection> evictableConnections = new ArrayList();
            int idleConnectionCount = 0;
            long now = System.nanoTime();
            long nanosUntilNextEviction = this.keepAliveDurationNs;
            ListIterator<Connection> i = this.connections.listIterator(this.connections.size());
            while (i.hasPrevious()) {
                Connection connection = (Connection) i.previous();
                long nanosUntilEviction = (connection.getIdleStartTimeNs() + this.keepAliveDurationNs) - now;
                if (nanosUntilEviction <= 0 || !connection.isAlive()) {
                    i.remove();
                    evictableConnections.add(connection);
                } else if (connection.isIdle()) {
                    idleConnectionCount++;
                    nanosUntilNextEviction = Math.min(nanosUntilNextEviction, nanosUntilEviction);
                }
            }
            i = this.connections.listIterator(this.connections.size());
            while (i.hasPrevious()) {
                int i2 = this.maxIdleConnections;
                if (idleConnectionCount <= r0) {
                    break;
                }
                connection = (Connection) i.previous();
                if (connection.isIdle()) {
                    evictableConnections.add(connection);
                    i.remove();
                    idleConnectionCount--;
                }
            }
            if (evictableConnections.isEmpty()) {
                try {
                    long millisUntilNextEviction = nanosUntilNextEviction / 1000000;
                    wait(millisUntilNextEviction, (int) (nanosUntilNextEviction - (1000000 * millisUntilNextEviction)));
                    return true;
                } catch (InterruptedException e) {
                }
            }
            int size = evictableConnections.size();
            for (int i3 = 0; i3 < size; i3++) {
                Util.closeQuietly(((Connection) evictableConnections.get(i3)).getSocket());
            }
            return true;
        }
    }

    void replaceCleanupExecutorForTests(Executor cleanupExecutor) {
        this.executor = cleanupExecutor;
    }

    synchronized List<Connection> getConnections() {
        return new ArrayList(this.connections);
    }
}
