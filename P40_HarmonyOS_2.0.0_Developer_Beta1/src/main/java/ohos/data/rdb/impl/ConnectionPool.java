package ohos.data.rdb.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import ohos.data.DatabaseFileType;
import ohos.data.rdb.RdbException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ConnectionPool {
    private static final ConcurrentHashMap<String, ConnectionPool> ACTIVE_CONNECTION_POOL = new ConcurrentHashMap<>(10);
    private static final int INITIAL_SIZE = 10;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "ConnectionPool");
    private static final Comparator<Waiter> WAITER_COMPARATOR = $$Lambda$ConnectionPool$frQpasWz5UH_0T662zCqqV27KY.INSTANCE;
    private static final int WAIT_PARK_TIME = 1000;
    private final WeakHashMap<Connection, Connection> acquiredConnections = new WeakHashMap<>();
    private final ArrayList<Connection> availableReadConnections;
    private SqliteDatabaseConfig config;
    private final Object connectionLock = new Object();
    private boolean isOpen;
    private int maxConnectionPoolSize;
    private int nextConnectionId;
    private AtomicInteger reference;
    private PriorityBlockingQueue<Waiter> waitReadQueue = new PriorityBlockingQueue<>(10, WAITER_COMPARATOR);
    private PriorityBlockingQueue<Waiter> waitWriteQueue = new PriorityBlockingQueue<>(10, WAITER_COMPARATOR);
    private Connection writeConnection;

    static /* synthetic */ int lambda$static$0(Waiter waiter, Waiter waiter2) {
        return waiter.priority - waiter2.priority;
    }

    private ConnectionPool(SqliteDatabaseConfig sqliteDatabaseConfig) {
        this.config = new SqliteDatabaseConfig(sqliteDatabaseConfig);
        this.nextConnectionId = 0;
        if (sqliteDatabaseConfig.isMemoryDb() || !"WAL".equalsIgnoreCase(sqliteDatabaseConfig.getJournalMode())) {
            this.maxConnectionPoolSize = 1;
        } else {
            this.maxConnectionPoolSize = SqliteGlobalConfig.getMaxConnectionPoolSize();
        }
        this.availableReadConnections = new ArrayList<>(this.maxConnectionPoolSize);
        this.reference = new AtomicInteger(0);
    }

    static ConnectionPool createInstance(SqliteDatabaseConfig sqliteDatabaseConfig) {
        ConnectionPool computeIfAbsent = ACTIVE_CONNECTION_POOL.computeIfAbsent(sqliteDatabaseConfig.getPath(), new Function() {
            /* class ohos.data.rdb.impl.$$Lambda$ConnectionPool$5L71A9Oo5zZqBi9t9Vdxtm77z3M */

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return ConnectionPool.lambda$createInstance$1(SqliteDatabaseConfig.this, (String) obj);
            }
        });
        checkConnectionPoolConfig(computeIfAbsent, sqliteDatabaseConfig);
        computeIfAbsent.reference.incrementAndGet();
        return computeIfAbsent;
    }

    static /* synthetic */ ConnectionPool lambda$createInstance$1(SqliteDatabaseConfig sqliteDatabaseConfig, String str) {
        ConnectionPool connectionPool = new ConnectionPool(sqliteDatabaseConfig);
        connectionPool.open();
        return connectionPool;
    }

    private void open() {
        this.writeConnection = openConnection(true, this.config);
        this.isOpen = true;
    }

    private Connection openConnection(boolean z, SqliteDatabaseConfig sqliteDatabaseConfig) {
        int i = this.nextConnectionId;
        this.nextConnectionId = i + 1;
        return SqliteConnection.open(sqliteDatabaseConfig, i, z);
    }

    private static void checkConnectionPoolConfig(ConnectionPool connectionPool, SqliteDatabaseConfig sqliteDatabaseConfig) {
        boolean equals;
        if (!connectionPool.config.getEncryptKeyLoader().equals(sqliteDatabaseConfig.getEncryptKeyLoader()) || !(connectionPool.config.getJournalMode().equalsIgnoreCase(sqliteDatabaseConfig.getJournalMode()) & true)) {
            throw new IllegalArgumentException("Config cannot be set after opening the database");
        }
    }

    /* access modifiers changed from: package-private */
    public Connection acquireConnection(String str, boolean z) {
        Waiter waiter;
        synchronized (this.connectionLock) {
            checkIfClosed();
            Connection connection = null;
            if (z) {
                connection = tryAcquireReadConnection(str);
            }
            if (connection == null) {
                connection = tryAcquireWriteConnection();
            }
            if (connection != null) {
                return connection;
            }
            waiter = new Waiter(Thread.currentThread(), 1);
            if (z) {
                this.waitReadQueue.add(waiter);
            } else {
                this.waitWriteQueue.add(waiter);
            }
        }
        while (waiter.getConnection() == null) {
            LockSupport.park(1000);
            Thread.interrupted();
        }
        return waiter.getConnection();
    }

    private Connection tryAcquireWriteConnection() {
        Connection connection = this.writeConnection;
        if (connection == null) {
            return null;
        }
        this.writeConnection = null;
        this.acquiredConnections.put(connection, connection);
        return connection;
    }

    private Connection tryAcquireReadConnection(String str) {
        int size = this.availableReadConnections.size();
        if (size > 1 && str != null) {
            for (int i = 0; i < size; i++) {
                Connection connection = this.availableReadConnections.get(i);
                if (connection.isPrecompiledStatementInCache(str)) {
                    this.availableReadConnections.remove(i);
                    this.acquiredConnections.put(connection, connection);
                    return connection;
                }
            }
        }
        if (size > 0) {
            Connection remove = this.availableReadConnections.remove(size - 1);
            this.acquiredConnections.put(remove, remove);
            return remove;
        }
        int size2 = this.acquiredConnections.size();
        if (this.writeConnection != null) {
            size2++;
        }
        if (size2 >= this.maxConnectionPoolSize) {
            return null;
        }
        Connection openConnection = openConnection(false, this.config);
        this.acquiredConnections.put(openConnection, openConnection);
        return openConnection;
    }

    /* access modifiers changed from: package-private */
    public void releaseConnection(Connection connection) {
        synchronized (this.connectionLock) {
            if (this.acquiredConnections.remove(connection) == null) {
                throw new IllegalStateException("Release connection failed, the connection has already been released.");
            } else if (!this.isOpen) {
                closeConnection(connection);
            } else if (!wakeWaitingQueue(connection)) {
                if (connection.isWriteConnection()) {
                    this.writeConnection = connection;
                } else if (this.availableReadConnections.size() >= this.maxConnectionPoolSize - 1) {
                    closeConnection(connection);
                } else {
                    this.availableReadConnections.add(connection);
                }
            }
        }
    }

    private boolean wakeWaitingQueue(Connection connection) {
        Waiter poll;
        synchronized (this.connectionLock) {
            if (!connection.isWriteConnection() || (poll = this.waitWriteQueue.poll()) == null) {
                Waiter poll2 = this.waitReadQueue.poll();
                if (poll2 == null) {
                    return false;
                }
                poll2.setConnection(connection);
                LockSupport.unpark(poll2.thread);
                this.acquiredConnections.put(connection, connection);
                return true;
            }
            poll.setConnection(connection);
            LockSupport.unpark(poll.thread);
            this.acquiredConnections.put(connection, connection);
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (RdbException e) {
            HiLog.error(LABEL, "close releaseConnection failed. %{public}s", new Object[]{e.getMessage()});
        }
    }

    private void checkIfClosed() {
        if (!this.isOpen) {
            throw new IllegalStateException("The connection pool has been closed.");
        }
    }

    /* access modifiers changed from: package-private */
    public void close() {
        ACTIVE_CONNECTION_POOL.computeIfPresent(this.config.getPath(), new BiFunction() {
            /* class ohos.data.rdb.impl.$$Lambda$ConnectionPool$2zkLg69npM91F87QrJ84DMm_too */

            @Override // java.util.function.BiFunction
            public final Object apply(Object obj, Object obj2) {
                return ConnectionPool.this.lambda$close$2$ConnectionPool((String) obj, (ConnectionPool) obj2);
            }
        });
    }

    public /* synthetic */ ConnectionPool lambda$close$2$ConnectionPool(String str, ConnectionPool connectionPool) {
        if (this.reference.decrementAndGet() != 0) {
            return connectionPool;
        }
        connectionPool.onAllReferencesReleased();
        return null;
    }

    private void onAllReferencesReleased() {
        closeAllConnections();
        this.config.destroyEncryptKey();
    }

    private void closeAllConnections() {
        Connection connection = this.writeConnection;
        if (connection != null) {
            closeConnection(connection);
            this.writeConnection = null;
        }
        Iterator<Connection> it = this.availableReadConnections.iterator();
        while (it.hasNext()) {
            closeConnection(it.next());
        }
        this.availableReadConnections.clear();
    }

    /* access modifiers changed from: package-private */
    public boolean changeDbFileForRestore(String str, String str2, SqliteEncryptKeyLoader sqliteEncryptKeyLoader) {
        synchronized (this.connectionLock) {
            if (this.acquiredConnections.isEmpty()) {
                closeAllConnections();
                if (!SqliteDatabaseUtils.renameFile(str2, str)) {
                    HiLog.info(LABEL, "Rename failed. originalPath = %{private}s, backupPath = %{private}s ", new Object[]{str, str2});
                    SqliteDatabaseUtils.deleteFile(str2);
                    return false;
                }
                String path = this.config.getPath();
                SqliteDatabaseUtils.deleteFile(path + "-shm");
                SqliteDatabaseUtils.deleteFile(path + "-wal");
                SqliteDatabaseUtils.deleteFile(path + "-journal");
                if (!path.equals(str)) {
                    SqliteDatabaseUtils.deleteFile(path);
                    SqliteDatabaseUtils.deleteFile(str + "-shm");
                    SqliteDatabaseUtils.deleteFile(str + "-wal");
                    SqliteDatabaseUtils.deleteFile(str + "-journal");
                    ACTIVE_CONNECTION_POOL.compute(str, new BiFunction(path) {
                        /* class ohos.data.rdb.impl.$$Lambda$ConnectionPool$Dp3nw0dXdMa1RDKVpTWaVA30ts */
                        private final /* synthetic */ String f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.function.BiFunction
                        public final Object apply(Object obj, Object obj2) {
                            String str = (String) obj;
                            ConnectionPool connectionPool = (ConnectionPool) obj2;
                            return ConnectionPool.ACTIVE_CONNECTION_POOL.remove(this.f$0);
                        }
                    });
                    this.config.setPath(str);
                }
                this.config.setEncryptKeyLoader(new SqliteEncryptKeyLoader(sqliteEncryptKeyLoader));
                this.writeConnection = openConnection(true, this.config);
                return true;
            }
            SqliteDatabaseUtils.deleteFile(str2);
            throw new IllegalArgumentException("Connection pool is busy now!");
        }
    }

    /* access modifiers changed from: package-private */
    public void changeEncryptKey(SqliteEncryptKeyLoader sqliteEncryptKeyLoader) {
        synchronized (this.connectionLock) {
            if (!this.config.getEncryptKeyLoader().isEmpty()) {
                checkIfClosed();
                if (this.acquiredConnections.isEmpty()) {
                    closeAllConnections();
                    if (this.writeConnection == null) {
                        this.writeConnection = openConnection(true, this.config);
                    }
                    this.writeConnection.changeEncryptKey(sqliteEncryptKeyLoader);
                    this.config.setEncryptKeyLoader(new SqliteEncryptKeyLoader(sqliteEncryptKeyLoader));
                } else {
                    throw new IllegalStateException("Connection pool is busy now!");
                }
            } else {
                throw new IllegalStateException("Change encrypt key on a unencrypted database.");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public String getPath() {
        return this.config.getPath();
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return this.config.getName();
    }

    /* access modifiers changed from: package-private */
    public boolean isReadOnly() {
        return (this.config.getOpenFlags() & 1) == 1;
    }

    /* access modifiers changed from: package-private */
    public boolean isMemoryDb() {
        return this.config.isMemoryDb();
    }

    /* access modifiers changed from: package-private */
    public DatabaseFileType getDatabaseFileType() {
        return this.config.getDatabaseFileType();
    }

    /* access modifiers changed from: private */
    public static class Waiter {
        private Connection connection;
        private int priority;
        private Thread thread;

        Waiter(Thread thread2, int i) {
            this.thread = thread2;
            this.priority = i;
        }

        /* access modifiers changed from: package-private */
        public Connection getConnection() {
            return this.connection;
        }

        /* access modifiers changed from: package-private */
        public void setConnection(Connection connection2) {
            this.connection = connection2;
        }
    }

    /* access modifiers changed from: package-private */
    public void configLocale(Locale locale) {
        synchronized (this.connectionLock) {
            if (!this.config.getLocale().equals(locale)) {
                checkIfClosed();
                if (!this.acquiredConnections.isEmpty()) {
                    throw new IllegalStateException("Not all connections in connection pool is idle.");
                } else if (this.writeConnection != null) {
                    if (!this.availableReadConnections.isEmpty()) {
                        this.availableReadConnections.forEach(new Consumer(locale) {
                            /* class ohos.data.rdb.impl.$$Lambda$ConnectionPool$BoL1JKPivyKYbyvYBvDelHflhZk */
                            private final /* synthetic */ Locale f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                ((Connection) obj).configLocale(this.f$0);
                            }
                        });
                    }
                    this.writeConnection.configLocale(locale);
                    this.config.setLocale(locale);
                } else {
                    throw new IllegalStateException("Write connection in connection pool is not available.");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void closeAllReadConnections() {
        synchronized (this.connectionLock) {
            if (!this.availableReadConnections.isEmpty()) {
                this.availableReadConnections.forEach(new Consumer() {
                    /* class ohos.data.rdb.impl.$$Lambda$ConnectionPool$TgSqUwAmUvWvelPaOXmMYjbk0zM */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        ConnectionPool.this.closeConnection((Connection) obj);
                    }
                });
                this.availableReadConnections.clear();
            }
        }
    }
}
