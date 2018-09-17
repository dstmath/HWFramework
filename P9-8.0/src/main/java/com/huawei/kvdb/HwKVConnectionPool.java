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
    private static final String RELATIVE_PATH = "Android/data/com.android.providers.media/thumbnail_cache";
    private static final String TAG = "kvdb_thumbnail";
    private static final int THRESHOLD = 4;
    private static final int WRITE_SLEEP = 30;
    private ArrayList<HwKVConnection> connectionQueue = new ArrayList();
    private final Object connectionQueueLock = new Object();
    private int curConnectionNum = 0;
    private Context mContext = null;
    private String mDbName = "thumbnail.db";
    private String mPackageName;
    private String mPath;
    private String mTableName = "kv";
    private Waiter waiterPool = null;
    private final Object waiterPoolLock = new Object();
    private ArrayList<Waiter> waiterQueue = new ArrayList();
    private final Object waiterQueueLock = new Object();
    private HwKVConnection writeConnection;
    private final Object writeConnectionLock = new Object();
    private int writeConnectionNum = -1;
    private ArrayList<Waiter> writeWaiterQueue = new ArrayList();
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

    void closeConnection() {
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
                HwKVConnection connection = (HwKVConnection) this.connectionQueue.get(0);
                this.connectionQueue.remove(0);
                connection.close();
            }
            this.curConnectionNum = 0;
        }
    }

    void closeExceptionConnection(HwKVConnection connection, boolean writable) {
        Object obj;
        if (writable) {
            obj = this.writeConnectionLock;
            synchronized (obj) {
                connection.close();
                this.writeConnectionNum = -1;
                this.writeConnection = null;
            }
        } else {
            obj = this.connectionQueueLock;
            synchronized (obj) {
                connection.close();
                this.curConnectionNum--;
            }
        }
    }

    /* JADX WARNING: Missing block: B:18:0x0046, code:
            if (r7 == false) goto L_0x006c;
     */
    /* JADX WARNING: Missing block: B:20:0x0050, code:
            throw new com.huawei.kvdb.HwKVException("No Connection Found.");
     */
    /* JADX WARNING: Missing block: B:35:0x006c, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private HwKVConnection doGetWriteConnection() throws HwKVException {
        Throwable th;
        boolean NoConnection = false;
        synchronized (this.writeConnectionLock) {
            HwKVConnection connection;
            try {
                if (this.writeConnectionNum == -1) {
                    connection = new HwKVConnection(this.mPath, this.mDbName, this.mTableName, this.mPackageName, this.mContext, Boolean.valueOf(false));
                    try {
                        File file = new File(this.mPath);
                        if (!file.exists()) {
                            if (!file.mkdirs()) {
                                Log.w(TAG, "Making cache directory failed for some reasons.");
                            }
                        }
                    } catch (SecurityException e) {
                        Log.w(TAG, "permission denied to create the named directory, please check.");
                    } catch (Throwable th2) {
                        th = th2;
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
                } else {
                    connection = null;
                }
            } catch (Throwable th3) {
                th = th3;
                connection = null;
                throw th;
            }
        }
    }

    void releaseWriteConnection(HwKVConnection connection) {
        int found = -1;
        Waiter waiter = null;
        while (found == -1) {
            synchronized (this.writeWaiterQueueLock) {
                if (this.writeWaiterQueue.size() > 0) {
                    waiter = (Waiter) this.writeWaiterQueue.get(0);
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
        if (found == 0) {
            synchronized (this.writeConnectionLock) {
                this.writeConnectionNum = 1;
            }
        }
    }

    HwKVConnection getWriteConnection() {
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

    /* JADX WARNING: Missing block: B:9:0x001d, code:
            if (r7 == false) goto L_0x0059;
     */
    /* JADX WARNING: Missing block: B:11:0x0027, code:
            throw new com.huawei.kvdb.HwKVException("No Connection Found.");
     */
    /* JADX WARNING: Missing block: B:28:0x0059, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private HwKVConnection doGetReadOnlyConnection() throws HwKVException {
        Throwable th;
        boolean NoConnection = false;
        synchronized (this.connectionQueueLock) {
            HwKVConnection connection;
            try {
                if (this.connectionQueue.size() > 0) {
                    connection = (HwKVConnection) this.connectionQueue.get(0);
                    try {
                        this.connectionQueue.remove(0);
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } else if (this.curConnectionNum < 4) {
                    connection = new HwKVConnection(this.mPath, this.mDbName, this.mTableName, this.mPackageName, this.mContext, Boolean.valueOf(true));
                    if (connection.open()) {
                        this.curConnectionNum++;
                    } else if (this.curConnectionNum <= 0) {
                        NoConnection = true;
                    } else {
                        connection = null;
                    }
                } else {
                    connection = null;
                }
            } catch (Throwable th3) {
                th = th3;
                connection = null;
                throw th;
            }
        }
    }

    HwKVConnection getReadOnlyConnection() {
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

    void releaseReadOnlyConnection(HwKVConnection connection) {
        int found = -1;
        Waiter waiter = null;
        while (found == -1) {
            synchronized (this.waiterQueueLock) {
                if (this.waiterQueue.size() > 0) {
                    waiter = (Waiter) this.waiterQueue.get(0);
                    this.waiterQueue.remove(0);
                }
            }
            if (waiter == null) {
                found = 0;
            } else {
                Waiter waiter2;
                synchronized (waiter) {
                    if (waiter.alreadyHasConnection) {
                        releaseWaiter(waiter);
                        found = -1;
                        waiter2 = null;
                    } else {
                        waiter.connection = connection;
                        waiter.alreadyHasConnection = true;
                        found = 1;
                        waiter.notifyAll();
                        waiter2 = waiter;
                    }
                }
                waiter = waiter2;
            }
        }
        if (found == 0) {
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
