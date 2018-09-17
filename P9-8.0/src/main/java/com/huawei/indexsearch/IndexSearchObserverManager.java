package com.huawei.indexsearch;

import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserManager;
import android.util.Log;
import android.util.Pools.Pool;
import android.util.Pools.SynchronizedPool;
import com.huawei.indexsearch.IObserverManager.Stub;
import java.util.ArrayList;
import java.util.List;

public class IndexSearchObserverManager {
    public static final String ACTION_BIND_OBSERVER_SERVICE = "com.huawei.indexsearch.observer_service";
    static final int INDEX_SERVICE_BIND_FLAGS = 1;
    private static final int INIT_BUILD_INDEX = 1000;
    private static final int MSG_BOUND = 1001;
    private static final int MSG_UNBOUND = 1002;
    public static final String OBSERVER_SERVICE_PKG_NAME = "com.huawei.indexsearch.observer";
    private static final String TAG = "IndexSearchObserverManager";
    private static volatile IndexSearchObserverManager mInstance = null;
    private boolean isUserUnlock = false;
    boolean mBound;
    IndexSearchObserverConnection mDefaultIndexSearchObserverConnection = new IndexSearchObserverConnection();
    private Handler mHandler;
    HandlerThread mHandlerThread = new HandlerThread(TAG);
    final Pool<PendingTask> mPendingTaskPool = new SynchronizedPool(20);
    final ArrayList<PendingTask> mPendingTasks = new ArrayList();
    private IObserverManager mService = null;

    class IndexSearchObserverConnection implements ServiceConnection {
        IndexSearchObserverConnection() {
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            IndexSearchObserverManager.this.mService = Stub.asInterface(service);
            IndexSearchObserverManager.this.mHandler.obtainMessage(1001, IndexSearchObserverManager.this.mService).sendToTarget();
        }

        public void onServiceDisconnected(ComponentName className) {
            if (Log.HWINFO) {
                Log.d(IndexSearchObserverManager.TAG, "IndexSearchObserverManager onServiceDisconnected");
            }
        }
    }

    class IndexSearchObserverHandler extends Handler {
        public IndexSearchObserverHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            PendingTask task;
            switch (msg.what) {
                case 1000:
                    task = msg.obj;
                    int idx = IndexSearchObserverManager.this.mPendingTasks.size();
                    if (IndexSearchObserverManager.this.mBound) {
                        IndexSearchObserverManager.this.mPendingTasks.add(idx, task);
                        if (idx == 0) {
                            IndexSearchObserverManager.this.mHandler.removeMessages(1002);
                            IndexSearchObserverManager.this.mHandler.sendEmptyMessage(1001);
                            return;
                        }
                        return;
                    } else if (IndexSearchObserverManager.this.ensureIndexSearchServiceBound()) {
                        IndexSearchObserverManager.this.mPendingTasks.add(idx, task);
                        return;
                    } else {
                        Log.e(IndexSearchObserverManager.TAG, "failed to bind observer service");
                        return;
                    }
                case 1001:
                    if (msg.obj != null) {
                        IndexSearchObserverManager.this.mService = (IObserverManager) msg.obj;
                    }
                    if (IndexSearchObserverManager.this.mService == null) {
                        if (!IndexSearchObserverManager.this.mBound) {
                            Log.e(IndexSearchObserverManager.TAG, "failed to bind observer service, clear all pending task");
                            IndexSearchObserverManager.this.mPendingTasks.clear();
                            return;
                        }
                        return;
                    } else if (IndexSearchObserverManager.this.mPendingTasks.size() > 0) {
                        task = (PendingTask) IndexSearchObserverManager.this.mPendingTasks.remove(0);
                        try {
                            if (task.ids != null) {
                                IndexSearchObserverManager.this.mService.buildIndexes(task.apkName, (String[]) task.ids.toArray(new String[task.ids.size()]), task.operator);
                            } else {
                                Log.w(IndexSearchObserverManager.TAG, "task ids empty");
                            }
                        } catch (RemoteException e) {
                            Log.e(IndexSearchObserverManager.TAG, "MSG_BOUND mService.buildIndexes error", e);
                        }
                        try {
                            IndexSearchObserverManager.this.mPendingTaskPool.release(task);
                        } catch (IllegalStateException e2) {
                            Log.e(IndexSearchObserverManager.TAG, "MSG_BOUND this pool is already release ");
                        }
                        if (IndexSearchObserverManager.this.mPendingTasks.size() != 0) {
                            IndexSearchObserverManager.this.mHandler.sendEmptyMessage(1001);
                            return;
                        } else if (IndexSearchObserverManager.this.mBound) {
                            IndexSearchObserverManager.this.mHandler.removeMessages(1002);
                            Message ubmsg = IndexSearchObserverManager.this.mHandler.obtainMessage(1002, task);
                            if (Log.HWINFO) {
                                Log.d(IndexSearchObserverManager.TAG, "delayed unbound conn: " + IndexSearchObserverManager.this.mDefaultIndexSearchObserverConnection);
                            }
                            IndexSearchObserverManager.this.mHandler.sendMessageDelayed(ubmsg, 10000);
                            return;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                case 1002:
                    if (msg.obj != null) {
                        task = (PendingTask) msg.obj;
                        if (IndexSearchObserverManager.this.mPendingTasks.size() == 0) {
                            IndexSearchObserverManager.this.ensureIndexSearchServiceUnBound(task);
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    Log.w(IndexSearchObserverManager.TAG, "msg unhandled");
                    return;
            }
        }
    }

    static final class PendingTask {
        String apkName;
        List<String> ids;
        int operator;

        PendingTask() {
        }
    }

    public static synchronized IndexSearchObserverManager getInstance() {
        IndexSearchObserverManager indexSearchObserverManager;
        synchronized (IndexSearchObserverManager.class) {
            if (mInstance == null) {
                mInstance = new IndexSearchObserverManager();
            }
            indexSearchObserverManager = mInstance;
        }
        return indexSearchObserverManager;
    }

    private IndexSearchObserverManager() {
        this.mHandlerThread.start();
        this.mHandler = new IndexSearchObserverHandler(this.mHandlerThread.getLooper());
    }

    private boolean ensureIndexSearchServiceBound() {
        Intent intent = new Intent(ACTION_BIND_OBSERVER_SERVICE).setPackage(OBSERVER_SERVICE_PKG_NAME);
        Context context = ActivityThread.currentApplication().getApplicationContext();
        if (!this.isUserUnlock) {
            if (((UserManager) context.getSystemService("user")).isUserUnlocked()) {
                this.isUserUnlock = true;
            } else {
                if (Log.HWINFO) {
                    Log.d(TAG, "user is locked");
                }
                return true;
            }
        }
        if (!context.bindService(intent, this.mDefaultIndexSearchObserverConnection, 1)) {
            return false;
        }
        this.mBound = true;
        return true;
    }

    private void ensureIndexSearchServiceUnBound(PendingTask pt) {
        ActivityThread.currentApplication().getApplicationContext().unbindService(this.mDefaultIndexSearchObserverConnection);
        this.mService = null;
        this.mBound = false;
    }

    public IObserverManager getService() {
        return this.mService;
    }

    public void buildIndex(String pkgName, String id, int op) {
        PendingTask p = (PendingTask) this.mPendingTaskPool.acquire();
        if (p == null) {
            p = new PendingTask();
        }
        p.apkName = pkgName;
        p.operator = op;
        p.ids = new ArrayList();
        p.ids.add(id);
        this.mHandler.obtainMessage(1000, p).sendToTarget();
    }

    public void buildIndex(String apkName, List<String> ids, int operator) {
        PendingTask p = (PendingTask) this.mPendingTaskPool.acquire();
        if (p == null) {
            p = new PendingTask();
        }
        p.apkName = apkName;
        p.operator = operator;
        p.ids = ids;
        this.mHandler.obtainMessage(1000, p).sendToTarget();
    }
}
