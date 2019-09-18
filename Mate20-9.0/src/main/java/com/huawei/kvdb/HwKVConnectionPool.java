package com.huawei.kvdb;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;

public class HwKVConnectionPool {
    private static final int FIND_CONTINUE = -1;
    private static final int FIND_DONE = 1;
    private static final int NOT_FIND = 0;
    private static final int READ_SLEEP = 3;
    static final String RELATIVE_PATH = "Android/data/com.android.providers.media/thumbnail_cache";
    private static final String TAG = "kvdb_thumbnail";
    private static final int THRESHOLD = 4;
    private static final int WRITE_SLEEP = 30;
    private ArrayList<HwKVConnection> connectionQueue = new ArrayList<>();
    private final Object connectionQueueLock = new Object();
    private int curConnectionNum = 0;
    private Context mContext = null;
    private String mDbName = "thumbnail.db";
    private String mPackageName;
    private String mPath;
    private String mTableName = "kv";
    private Waiter waiterPool = null;
    private final Object waiterPoolLock = new Object();
    private ArrayList<Waiter> waiterQueue = new ArrayList<>();
    private final Object waiterQueueLock = new Object();
    private HwKVConnection writeConnection;
    private final Object writeConnectionLock = new Object();
    private int writeConnectionNum = -1;
    private ArrayList<Waiter> writeWaiterQueue = new ArrayList<>();
    private final Object writeWaiterQueueLock = new Object();

    private static final class Waiter {
        public boolean alreadyHasConnection = false;
        public HwKVConnection connection = null;
        public Waiter next = null;
    }

    HwKVConnectionPool(Context context) {
        this.mContext = context;
        this.mPackageName = context.getPackageName();
        this.mPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + RELATIVE_PATH;
        File file = new File(this.mPath);
        if (!file.exists()) {
            try {
                if (!file.mkdirs()) {
                    Log.w(TAG, "Making cache directory failed for some reasons.");
                }
            } catch (SecurityException e) {
                Log.w(TAG, "permission denied to create the named directory, please check.");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void closeConnection() {
        synchronized (this.writeConnectionLock) {
            if (this.writeConnectionNum == 1) {
                this.writeConnection.close();
                this.writeConnection = null;
                this.writeConnectionNum = -1;
            }
        }
        synchronized (this.connectionQueueLock) {
            int size = this.connectionQueue.size();
            for (int i = 0; i < size; i++) {
                this.connectionQueue.remove(0);
                this.connectionQueue.get(0).close();
            }
            this.curConnectionNum = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void closeExceptionConnection(HwKVConnection connection, boolean writable) {
        if (!writable) {
            synchronized (this.connectionQueueLock) {
                connection.close();
                this.curConnectionNum--;
            }
            return;
        }
        synchronized (this.writeConnectionLock) {
            connection.close();
            this.writeConnectionNum = -1;
            this.writeConnection = null;
        }
    }

    private HwKVConnection doGetWriteConnection() throws HwKVException {
        HwKVConnection connection = null;
        boolean NoConnection = false;
        synchronized (this.writeConnectionLock) {
            if (this.writeConnectionNum == -1) {
                HwKVConnection hwKVConnection = new HwKVConnection(this.mPath, this.mDbName, this.mTableName, this.mPackageName, this.mContext, false);
                connection = hwKVConnection;
                File file = new File(this.mPath);
                if (!file.exists()) {
                    try {
                        if (!file.mkdirs()) {
                            Log.w(TAG, "Making cache directory failed for some reasons.");
                        }
                    } catch (SecurityException e) {
                        Log.w(TAG, "permission denied to create the named directory, please check.");
                    }
                }
                if (connection.open()) {
                    this.writeConnection = connection;
                    this.writeConnectionNum = 0;
                } else {
                    NoConnection = true;
                }
            } else if (this.writeConnectionNum == 1) {
                connection = this.writeConnection;
                this.writeConnectionNum = 0;
            }
        }
        if (!NoConnection) {
            return connection;
        }
        throw new HwKVException("No Connection Found.");
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x003f, code lost:
        if (r0 != 0) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0041, code lost:
        r2 = r7.writeConnectionLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0043, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        r7.writeConnectionNum = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0046, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        return;
     */
    public void releaseWriteConnection(HwKVConnection connection) {
        int found = -1;
        Waiter waiter = null;
        while (true) {
            if (found != -1) {
                break;
            }
            synchronized (this.writeWaiterQueueLock) {
                if (this.writeWaiterQueue.size() > 0) {
                    waiter = this.writeWaiterQueue.get(0);
                    this.writeWaiterQueue.remove(0);
                }
            }
            if (waiter == null) {
                found = 0;
                break;
            }
            synchronized (waiter) {
                if (waiter.alreadyHasConnection) {
                    releaseWaiter(waiter);
                    found = -1;
                } else {
                    waiter.alreadyHasConnection = true;
                    waiter.connection = connection;
                    found = 1;
                    waiter.notifyAll();
                }
            }
        }
        while (true) {
        }
    }

    /* access modifiers changed from: package-private */
    public HwKVConnection getWriteConnection() {
        try {
            HwKVConnection connection = doGetWriteConnection();
            if (connection == null) {
                Waiter w = getWaiter();
                boolean isInQueue = false;
                while (connection == null) {
                    synchronized (w) {
                        if (!isInQueue) {
                            synchronized (this.writeWaiterQueueLock) {
                                this.writeWaiterQueue.add(w);
                                isInQueue = true;
                            }
                        }
                        try {
                            w.wait(30);
                        } catch (InterruptedException e) {
                        }
                        if (w.alreadyHasConnection) {
                            connection = w.connection;
                            releaseWaiter(w);
                        } else {
                            try {
                                connection = doGetWriteConnection();
                                if (connection != null) {
                                    w.alreadyHasConnection = true;
                                }
                            } catch (HwKVException e2) {
                                w.alreadyHasConnection = true;
                            }
                        }
                    }
                }
            }
            return connection;
        } catch (HwKVException e3) {
            return null;
        }
    }

    private HwKVConnection doGetReadOnlyConnection() throws HwKVException {
        HwKVConnection connection = null;
        boolean NoConnection = false;
        synchronized (this.connectionQueueLock) {
            if (this.connectionQueue.size() > 0) {
                connection = this.connectionQueue.get(0);
                this.connectionQueue.remove(0);
            } else if (this.curConnectionNum < 4) {
                HwKVConnection hwKVConnection = new HwKVConnection(this.mPath, this.mDbName, this.mTableName, this.mPackageName, this.mContext, true);
                connection = hwKVConnection;
                if (connection.open()) {
                    this.curConnectionNum++;
                } else if (this.curConnectionNum <= 0) {
                    NoConnection = true;
                } else {
                    connection = null;
                }
            }
        }
        if (!NoConnection) {
            return connection;
        }
        throw new HwKVException("No Connection Found.");
    }

    /* access modifiers changed from: package-private */
    public HwKVConnection getReadOnlyConnection() {
        try {
            HwKVConnection connection = doGetReadOnlyConnection();
            if (connection == null) {
                Waiter w = getWaiter();
                boolean isInQueue = false;
                while (connection == null) {
                    synchronized (w) {
                        if (!isInQueue) {
                            synchronized (this.waiterQueueLock) {
                                this.waiterQueue.add(w);
                                isInQueue = true;
                            }
                        }
                        try {
                            w.wait(3);
                        } catch (InterruptedException e) {
                        }
                        if (w.alreadyHasConnection) {
                            connection = w.connection;
                            releaseWaiter(w);
                        } else {
                            try {
                                connection = doGetReadOnlyConnection();
                                if (connection != null) {
                                    w.alreadyHasConnection = true;
                                }
                            } catch (HwKVException e2) {
                                w.alreadyHasConnection = true;
                            }
                        }
                    }
                }
            }
            return connection;
        } catch (HwKVException e3) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003a, code lost:
        r1 = r2;
     */
    public void releaseReadOnlyConnection(HwKVConnection connection) {
        Waiter waiter;
        char c = 65535;
        Waiter waiter2 = null;
        while (c == 65535) {
            synchronized (this.waiterQueueLock) {
                if (this.waiterQueue.size() > 0) {
                    waiter2 = this.waiterQueue.get(0);
                    this.waiterQueue.remove(0);
                }
            }
            if (waiter2 == null) {
                c = 0;
            } else {
                synchronized (waiter2) {
                    try {
                        if (waiter2.alreadyHasConnection) {
                            releaseWaiter(waiter2);
                            c = 65535;
                            waiter = null;
                        } else {
                            waiter2.connection = connection;
                            waiter2.alreadyHasConnection = true;
                            c = 1;
                            waiter2.notifyAll();
                            waiter = waiter2;
                        }
                        try {
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            char c2 = c;
                            Waiter waiter3 = waiter;
                            th = th2;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        char c3 = c;
                        Waiter waiter4 = waiter2;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            }
        }
        if (c == 0) {
            synchronized (this.connectionQueueLock) {
                this.connectionQueue.add(connection);
            }
        }
    }

    private Waiter getWaiter() {
        Waiter waiter;
        synchronized (this.waiterPoolLock) {
            waiter = this.waiterPool;
            if (waiter != null) {
                this.waiterPool = waiter.next;
                waiter.next = null;
            } else {
                waiter = new Waiter();
            }
        }
        return waiter;
    }

    private void releaseWaiter(Waiter waiter) {
        synchronized (this.waiterPoolLock) {
            waiter.connection = null;
            waiter.alreadyHasConnection = false;
            waiter.next = this.waiterPool;
            this.waiterPool = waiter;
        }
    }
}
