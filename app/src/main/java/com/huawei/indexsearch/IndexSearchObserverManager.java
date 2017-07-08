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
import com.android.ims.ImsConferenceState;
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
    private static volatile IndexSearchObserverManager mInstance;
    private boolean isUserUnlock;
    boolean mBound;
    IndexSearchObserverConnection mDefaultIndexSearchObserverConnection;
    private Handler mHandler;
    HandlerThread mHandlerThread;
    final Pool<PendingTask> mPendingTaskPool;
    final ArrayList<PendingTask> mPendingTasks;
    private IObserverManager mService;

    class IndexSearchObserverConnection implements ServiceConnection {
        IndexSearchObserverConnection() {
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            IndexSearchObserverManager.this.mService = Stub.asInterface(service);
            IndexSearchObserverManager.this.mHandler.obtainMessage(IndexSearchObserverManager.MSG_BOUND, IndexSearchObserverManager.this.mService).sendToTarget();
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
                case IndexSearchObserverManager.INIT_BUILD_INDEX /*1000*/:
                    task = msg.obj;
                    int idx = IndexSearchObserverManager.this.mPendingTasks.size();
                    if (IndexSearchObserverManager.this.mBound) {
                        IndexSearchObserverManager.this.mPendingTasks.add(idx, task);
                        if (idx == 0) {
                            IndexSearchObserverManager.this.mHandler.removeMessages(IndexSearchObserverManager.MSG_UNBOUND);
                            IndexSearchObserverManager.this.mHandler.sendEmptyMessage(IndexSearchObserverManager.MSG_BOUND);
                        }
                    } else if (IndexSearchObserverManager.this.ensureIndexSearchServiceBound()) {
                        IndexSearchObserverManager.this.mPendingTasks.add(idx, task);
                    } else {
                        Log.e(IndexSearchObserverManager.TAG, "failed to bind observer service");
                    }
                case IndexSearchObserverManager.MSG_BOUND /*1001*/:
                    if (msg.obj != null) {
                        IndexSearchObserverManager.this.mService = (IObserverManager) msg.obj;
                    }
                    if (IndexSearchObserverManager.this.mService == null) {
                        if (!IndexSearchObserverManager.this.mBound) {
                            Log.e(IndexSearchObserverManager.TAG, "failed to bind observer service, clear all pending task");
                            IndexSearchObserverManager.this.mPendingTasks.clear();
                        }
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
                            IndexSearchObserverManager.this.mHandler.sendEmptyMessage(IndexSearchObserverManager.MSG_BOUND);
                        } else if (IndexSearchObserverManager.this.mBound) {
                            IndexSearchObserverManager.this.mHandler.removeMessages(IndexSearchObserverManager.MSG_UNBOUND);
                            Message ubmsg = IndexSearchObserverManager.this.mHandler.obtainMessage(IndexSearchObserverManager.MSG_UNBOUND, task);
                            if (Log.HWINFO) {
                                Log.d(IndexSearchObserverManager.TAG, "delayed unbound conn: " + IndexSearchObserverManager.this.mDefaultIndexSearchObserverConnection);
                            }
                            IndexSearchObserverManager.this.mHandler.sendMessageDelayed(ubmsg, 10000);
                        }
                    }
                case IndexSearchObserverManager.MSG_UNBOUND /*1002*/:
                    if (msg.obj != null) {
                        task = (PendingTask) msg.obj;
                        if (IndexSearchObserverManager.this.mPendingTasks.size() == 0) {
                            IndexSearchObserverManager.this.ensureIndexSearchServiceUnBound(task);
                        }
                    }
                default:
                    Log.w(IndexSearchObserverManager.TAG, "msg unhandled");
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.indexsearch.IndexSearchObserverManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.indexsearch.IndexSearchObserverManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.indexsearch.IndexSearchObserverManager.<clinit>():void");
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
        this.isUserUnlock = false;
        this.mService = null;
        this.mPendingTasks = new ArrayList();
        this.mPendingTaskPool = new SynchronizedPool(20);
        this.mDefaultIndexSearchObserverConnection = new IndexSearchObserverConnection();
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new IndexSearchObserverHandler(this.mHandlerThread.getLooper());
    }

    private boolean ensureIndexSearchServiceBound() {
        Intent intent = new Intent(ACTION_BIND_OBSERVER_SERVICE).setPackage(OBSERVER_SERVICE_PKG_NAME);
        Context context = ActivityThread.currentApplication().getApplicationContext();
        if (!this.isUserUnlock) {
            if (((UserManager) context.getSystemService(ImsConferenceState.USER)).isUserUnlocked()) {
                this.isUserUnlock = true;
            } else {
                if (Log.HWINFO) {
                    Log.d(TAG, "user is locked");
                }
                return true;
            }
        }
        if (!context.bindService(intent, this.mDefaultIndexSearchObserverConnection, INDEX_SERVICE_BIND_FLAGS)) {
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
        this.mHandler.obtainMessage(INIT_BUILD_INDEX, p).sendToTarget();
    }

    public void buildIndex(String apkName, List<String> ids, int operator) {
        PendingTask p = (PendingTask) this.mPendingTaskPool.acquire();
        if (p == null) {
            p = new PendingTask();
        }
        p.apkName = apkName;
        p.operator = operator;
        p.ids = ids;
        this.mHandler.obtainMessage(INIT_BUILD_INDEX, p).sendToTarget();
    }
}
