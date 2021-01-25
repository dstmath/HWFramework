package com.huawei.kvdb;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HwKVConnectionPool {
    private static final int FIND_CONTINUE = -1;
    private static final int FIND_DONE = 1;
    private static final int NOT_FIND = 0;
    private static final int READ_SLEEP = 3;
    private static final String TAG = "HwKVConnectionPool";
    private static final int THRESHOLD = 4;
    private static final String THUMBNAIL_RELATIVE_PATH = (Environment.DIR_ANDROID + "/data/com.android.providers.media/thumbnail_cache");
    private static final int WRITE_SLEEP = 30;
    private List<HwKVConnection> mConnectionQueue;
    private final Object mConnectionQueueLock = new Object();
    private int mCurConnectionNum;
    private String mDbName;
    private boolean mIsClosed = false;
    private boolean mIsGeneralKV;
    private String mPath;
    private Waiter mWaiterPool;
    private final Object mWaiterPoolLock = new Object();
    private List<Waiter> mWaiterQueue;
    private final Object mWaiterQueueLock = new Object();
    private HwKVConnection mWriteConnection;
    private final Object mWriteConnectionLock = new Object();
    private int mWriteConnectionNum;
    private List<Waiter> mWriteWaiterQueue;
    private final Object mWriteWaiterQueueLock = new Object();

    /* access modifiers changed from: private */
    public static final class Waiter {
        private HwKVConnection connection;
        private boolean isAlreadyConnected;
        private Waiter next;

        private Waiter() {
            this.next = null;
            this.connection = null;
            this.isAlreadyConnected = false;
        }
    }

    HwKVConnectionPool(String dbPath) {
        int lastIndexOfSlash = dbPath.lastIndexOf("/");
        if (lastIndexOfSlash != -1) {
            init(dbPath.substring(0, lastIndexOfSlash), dbPath.substring(lastIndexOfSlash + 1), true);
            return;
        }
        throw new IllegalArgumentException("invalid file path");
    }

    HwKVConnectionPool() {
        init(getThumbnailAbsolutePath(), "thumbnail.db", false);
    }

    private void init(String path, String dbName, boolean isGeneralKV) {
        this.mWaiterPool = null;
        this.mWaiterQueue = new ArrayList();
        this.mWriteWaiterQueue = new ArrayList();
        this.mConnectionQueue = new ArrayList();
        this.mCurConnectionNum = 0;
        this.mWriteConnectionNum = -1;
        this.mPath = path;
        this.mDbName = dbName;
        this.mIsGeneralKV = isGeneralKV;
        ensureDbPath(this.mPath);
    }

    private void ensureDbPath(String path) {
        File file = new File(path);
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
    public void open() {
        this.mIsClosed = false;
    }

    /* access modifiers changed from: package-private */
    public boolean isGeneralKV() {
        return this.mIsGeneralKV;
    }

    /* access modifiers changed from: package-private */
    public void closeConnectionInner() {
        synchronized (this.mWriteConnectionLock) {
            if (this.mWriteConnectionNum == 1) {
                this.mWriteConnection.close();
                this.mWriteConnection = null;
                this.mWriteConnectionNum = -1;
            }
        }
        synchronized (this.mConnectionQueueLock) {
            for (HwKVConnection connection : this.mConnectionQueue) {
                connection.close();
            }
            this.mConnectionQueue.clear();
            this.mCurConnectionNum = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void closeConnection() {
        if (!this.mIsClosed) {
            closeConnectionInner();
            this.mIsClosed = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void closeExceptionConnection(HwKVConnection connection, boolean isWritable) {
        if (!isWritable) {
            synchronized (this.mConnectionQueueLock) {
                connection.close();
                this.mCurConnectionNum--;
            }
            return;
        }
        synchronized (this.mWriteConnectionLock) {
            connection.close();
            this.mWriteConnectionNum = -1;
            this.mWriteConnection = null;
        }
    }

    private HwKVConnection doGetWriteConnection() throws HwKVException {
        HwKVConnection connection = null;
        synchronized (this.mWriteConnectionLock) {
            if (this.mWriteConnectionNum == -1) {
                connection = new HwKVConnection(this.mPath, this.mDbName, false, this.mIsGeneralKV);
                ensureDbPath(this.mPath);
                if (connection.open()) {
                    this.mWriteConnection = connection;
                    this.mWriteConnectionNum = 0;
                } else {
                    throw new HwKVException("No Connection Found.");
                }
            } else if (this.mWriteConnectionNum == 1) {
                connection = this.mWriteConnection;
                this.mWriteConnectionNum = 0;
            } else {
                Log.w(TAG, "unexpected write connection number.");
            }
        }
        return connection;
    }

    /* access modifiers changed from: package-private */
    public void releaseWriteConnection(HwKVConnection connection) {
        int found = -1;
        while (found == -1) {
            Waiter waiter = null;
            synchronized (this.mWriteWaiterQueueLock) {
                if (this.mWriteWaiterQueue.size() > 0) {
                    waiter = this.mWriteWaiterQueue.get(0);
                    this.mWriteWaiterQueue.remove(0);
                }
            }
            if (waiter == null) {
                found = 0;
            } else {
                synchronized (waiter) {
                    if (waiter.isAlreadyConnected) {
                        releaseWaiter(waiter);
                        found = -1;
                    } else {
                        waiter.isAlreadyConnected = true;
                        waiter.connection = connection;
                        found = 1;
                        waiter.notifyAll();
                    }
                }
            }
        }
        if (found == 0) {
            synchronized (this.mWriteConnectionLock) {
                this.mWriteConnectionNum = 1;
            }
        }
        if (this.mIsClosed) {
            closeConnectionInner();
        }
    }

    /* access modifiers changed from: package-private */
    public HwKVConnection getWriteConnection() {
        try {
            HwKVConnection connection = doGetWriteConnection();
            if (connection != null) {
                return connection;
            }
            Waiter waiter = getWaiter();
            boolean isInQueue = false;
            while (connection == null) {
                synchronized (waiter) {
                    if (!isInQueue) {
                        synchronized (this.mWriteWaiterQueueLock) {
                            this.mWriteWaiterQueue.add(waiter);
                            isInQueue = true;
                        }
                    }
                    try {
                        waiter.wait(30);
                    } catch (InterruptedException e) {
                        Log.w(TAG, "InterruptedException when getWriteConnection");
                    }
                    if (waiter.isAlreadyConnected) {
                        connection = waiter.connection;
                        releaseWaiter(waiter);
                    } else {
                        try {
                            connection = doGetWriteConnection();
                            if (connection != null) {
                                waiter.isAlreadyConnected = true;
                            }
                        } catch (HwKVException e2) {
                            waiter.isAlreadyConnected = true;
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
        synchronized (this.mConnectionQueueLock) {
            if (this.mConnectionQueue.size() > 0) {
                connection = this.mConnectionQueue.get(0);
                this.mConnectionQueue.remove(0);
            } else if (this.mCurConnectionNum < 4) {
                connection = new HwKVConnection(this.mPath, this.mDbName, true, this.mIsGeneralKV);
                if (connection.open()) {
                    this.mCurConnectionNum++;
                } else if (this.mCurConnectionNum > 0) {
                    connection = null;
                } else {
                    throw new HwKVException("No Connection Found.");
                }
            } else {
                Log.w(TAG, "current connection number reaches threshold.");
            }
        }
        return connection;
    }

    /* access modifiers changed from: package-private */
    public HwKVConnection getReadOnlyConnection() {
        try {
            HwKVConnection connection = doGetReadOnlyConnection();
            if (connection != null) {
                return connection;
            }
            Waiter waiter = getWaiter();
            boolean isInQueue = false;
            while (connection == null) {
                synchronized (waiter) {
                    if (!isInQueue) {
                        synchronized (this.mWaiterQueueLock) {
                            this.mWaiterQueue.add(waiter);
                            isInQueue = true;
                        }
                    }
                    try {
                        waiter.wait(3);
                    } catch (InterruptedException e) {
                        Log.w(TAG, "InterruptedException when getReadOnlyConnection");
                    }
                    if (waiter.isAlreadyConnected) {
                        connection = waiter.connection;
                        releaseWaiter(waiter);
                    } else {
                        try {
                            connection = doGetReadOnlyConnection();
                            if (connection != null) {
                                waiter.isAlreadyConnected = true;
                            }
                        } catch (HwKVException e2) {
                            waiter.isAlreadyConnected = true;
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
    public void releaseReadOnlyConnection(HwKVConnection connection) {
        int found = -1;
        while (found == -1) {
            Waiter waiter = null;
            synchronized (this.mWaiterQueueLock) {
                if (this.mWaiterQueue.size() > 0) {
                    waiter = this.mWaiterQueue.get(0);
                    this.mWaiterQueue.remove(0);
                }
            }
            if (waiter == null) {
                found = 0;
            } else {
                synchronized (waiter) {
                    if (waiter.isAlreadyConnected) {
                        releaseWaiter(waiter);
                        found = -1;
                    } else {
                        waiter.connection = connection;
                        waiter.isAlreadyConnected = true;
                        found = 1;
                        waiter.notifyAll();
                    }
                }
            }
        }
        if (found == 0) {
            synchronized (this.mConnectionQueueLock) {
                this.mConnectionQueue.add(connection);
            }
        }
        if (this.mIsClosed) {
            closeConnectionInner();
        }
    }

    private Waiter getWaiter() {
        Waiter waiter;
        synchronized (this.mWaiterPoolLock) {
            waiter = this.mWaiterPool;
            if (waiter != null) {
                this.mWaiterPool = waiter.next;
                waiter.next = null;
            } else {
                waiter = new Waiter();
            }
        }
        return waiter;
    }

    private void releaseWaiter(Waiter waiter) {
        synchronized (this.mWaiterPoolLock) {
            waiter.connection = null;
            waiter.isAlreadyConnected = false;
            waiter.next = this.mWaiterPool;
            this.mWaiterPool = waiter;
        }
    }

    static String getThumbnailAbsolutePath() {
        try {
            String externalPath = Environment.getExternalStorageDirectory().getCanonicalPath();
            int endIndex = externalPath.lastIndexOf("/");
            if (endIndex >= 0) {
                return externalPath.substring(0, endIndex) + "/0/" + THUMBNAIL_RELATIVE_PATH;
            }
            throw new IllegalStateException("abnormal external storage canonical path.");
        } catch (IOException e) {
            throw new IllegalStateException("unable to resolve external storage canonical path.", e);
        }
    }
}
