package android.net.http;

import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
import org.apache.http.HttpHost;

class ConnectionThread extends Thread {
    static final int WAIT_TICK = 1000;
    static final int WAIT_TIMEOUT = 5000;
    Connection mConnection;
    private ConnectionManager mConnectionManager;
    private Context mContext;
    long mCurrentThreadTime;
    private int mId;
    private RequestFeeder mRequestFeeder;
    private volatile boolean mRunning = true;
    long mTotalThreadTime;
    private boolean mWaiting;

    ConnectionThread(Context context, int id, ConnectionManager connectionManager, RequestFeeder requestFeeder) {
        this.mContext = context;
        setName(HttpHost.DEFAULT_SCHEME_NAME + id);
        this.mId = id;
        this.mConnectionManager = connectionManager;
        this.mRequestFeeder = requestFeeder;
    }

    void requestStop() {
        synchronized (this.mRequestFeeder) {
            this.mRunning = false;
            this.mRequestFeeder.notify();
        }
    }

    public void run() {
        Process.setThreadPriority(1);
        this.mCurrentThreadTime = 0;
        this.mTotalThreadTime = 0;
        while (this.mRunning) {
            if (this.mCurrentThreadTime == -1) {
                this.mCurrentThreadTime = SystemClock.currentThreadTimeMillis();
            }
            Request request = this.mRequestFeeder.getRequest();
            if (request == null) {
                synchronized (this.mRequestFeeder) {
                    this.mWaiting = true;
                    try {
                        this.mRequestFeeder.wait();
                    } catch (InterruptedException e) {
                    }
                    this.mWaiting = false;
                    if (this.mCurrentThreadTime != 0) {
                        this.mCurrentThreadTime = SystemClock.currentThreadTimeMillis();
                    }
                }
            } else {
                this.mConnection = this.mConnectionManager.getConnection(this.mContext, request.mHost);
                this.mConnection.processRequests(request);
                if (!this.mConnection.getCanPersist()) {
                    this.mConnection.closeConnection();
                } else if (!this.mConnectionManager.recycleConnection(this.mConnection)) {
                    this.mConnection.closeConnection();
                }
                this.mConnection = null;
                if (this.mCurrentThreadTime > 0) {
                    long start = this.mCurrentThreadTime;
                    this.mCurrentThreadTime = SystemClock.currentThreadTimeMillis();
                    this.mTotalThreadTime += this.mCurrentThreadTime - start;
                }
            }
        }
    }

    public synchronized String toString() {
        return "cid " + this.mId + " " + (this.mWaiting ? "w" : "a") + " " + (this.mConnection == null ? "" : this.mConnection.toString());
    }
}
