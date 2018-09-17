package com.huawei.indexsearch;

import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.BulkCursorDescriptor;
import android.database.BulkCursorToCursorAdaptor;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class IndexSearchManager {
    public static final String ACTION_BIND_INDEX_SERVICE = "com.huawei.indexsearch.index_service";
    static final int INDEX_SERVICE_BIND_FLAGS = 1;
    public static final String INDEX_SERVICE_PKG_NAME = "com.huawei.indexsearch";
    private static final int INIT_BUILD_INDEX = 1000;
    private static final int MSG_BOOTCOMPLETE = 1003;
    private static final int MSG_BOUND = 1001;
    private static final int MSG_TRIGGER_IDLE = 1004;
    private static final int MSG_UNBOUND = 1002;
    private static final String TAG = "IndexSearchManager";
    private static volatile IndexSearchManager mInstance = null;
    private boolean isUserUnlock = false;
    boolean mBound;
    final ArrayList<List<CachedItem>> mCachedItemsList = new ArrayList();
    IndexSearchConnection mDefaultIndexSearchConnection = new IndexSearchConnection();
    private Handler mHandler;
    HandlerThread mHandlerThread = new HandlerThread(TAG);
    private IIndexSearchManager mService = null;

    class IndexSearchConnection implements ServiceConnection {
        IndexSearchConnection() {
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            IndexSearchManager.this.mService = new IndexSearchManagerProxy(service);
            IndexSearchManager.this.mHandler.obtainMessage(1001, IndexSearchManager.this.mService).sendToTarget();
        }

        public void onServiceDisconnected(ComponentName className) {
            synchronized (IndexSearchManager.this) {
                if (Log.HWINFO) {
                    Log.d(IndexSearchManager.TAG, "IndexSearchManager onServiceDisconnected");
                }
            }
        }
    }

    class IndexSearchHandler extends Handler {
        public IndexSearchHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    List<CachedItem> item = msg.obj;
                    int idx = IndexSearchManager.this.mCachedItemsList.size();
                    if (IndexSearchManager.this.mBound) {
                        IndexSearchManager.this.mCachedItemsList.add(idx, item);
                        if (idx == 0) {
                            IndexSearchManager.this.mHandler.removeMessages(1002);
                            IndexSearchManager.this.mHandler.sendEmptyMessage(1001);
                            return;
                        }
                        return;
                    } else if (IndexSearchManager.this.ensureIndexSearchServiceBound()) {
                        IndexSearchManager.this.mCachedItemsList.add(idx, item);
                        return;
                    } else {
                        Log.e(IndexSearchManager.TAG, "failed to bind index search service when build index");
                        return;
                    }
                case 1001:
                    if (msg.obj != null) {
                        IndexSearchManager.this.mService = (IIndexSearchManager) msg.obj;
                    }
                    if (IndexSearchManager.this.mService == null) {
                        if (!IndexSearchManager.this.mBound) {
                            Log.e(IndexSearchManager.TAG, "MSG_BOUND, failed to bind index search service, clear all pending task");
                            IndexSearchManager.this.mCachedItemsList.clear();
                            return;
                        }
                        return;
                    } else if (IndexSearchManager.this.mCachedItemsList.size() > 0) {
                        List<CachedItem> items = (List) IndexSearchManager.this.mCachedItemsList.remove(0);
                        try {
                            IndexSearchManager.this.mService.buildIndex(items);
                        } catch (RemoteException e) {
                            Log.e(IndexSearchManager.TAG, "MSG_BOUND mService.buildIndex error", e);
                        }
                        if (IndexSearchManager.this.mCachedItemsList.size() != 0) {
                            IndexSearchManager.this.mHandler.sendEmptyMessage(1001);
                            return;
                        } else if (IndexSearchManager.this.mBound) {
                            IndexSearchManager.this.mHandler.removeMessages(1002);
                            Message ubmsg = IndexSearchManager.this.mHandler.obtainMessage(1002, items);
                            if (Log.HWINFO) {
                                Log.d(IndexSearchManager.TAG, "delayed unbound conn: " + IndexSearchManager.this.mDefaultIndexSearchConnection);
                            }
                            IndexSearchManager.this.mHandler.sendMessageDelayed(ubmsg, 10000);
                            return;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                case 1002:
                    if (IndexSearchManager.this.mCachedItemsList.size() == 0) {
                        IndexSearchManager.this.ensureIndexSearchServiceUnBound();
                        return;
                    }
                    return;
                case 1003:
                    IndexSearchManager.this.handleBootCompleted();
                    return;
                case 1004:
                    IndexSearchManager.this.handleTriggerIdle();
                    return;
                default:
                    Log.w(IndexSearchManager.TAG, "msg unhandled");
                    return;
            }
        }
    }

    static final class IndexSearchManagerProxy implements IIndexSearchManager {
        private IBinder mRemote;

        public IndexSearchManagerProxy(IBinder remote) {
            this.mRemote = remote;
        }

        public IBinder asBinder() {
            return this.mRemote;
        }

        public void forceStopBuildIndex(String path, long time) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken(IIndexSearchManager.descriptor);
            data.writeString(path);
            data.writeLong(time);
            this.mRemote.transact(5, data, reply, 0);
            reply.readException();
            reply.recycle();
            data.recycle();
        }

        public void bootCompleted() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken(IIndexSearchManager.descriptor);
            this.mRemote.transact(6, data, reply, 0);
            reply.readException();
            reply.recycle();
            data.recycle();
        }

        public void triggerIdle() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken(IIndexSearchManager.descriptor);
            this.mRemote.transact(7, data, reply, 0);
            reply.readException();
            reply.recycle();
            data.recycle();
        }

        public void buildIndex(List<CachedItem> list) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken(IIndexSearchManager.descriptor);
            int N = list != null ? list.size() : -1;
            data.writeInt(N);
            for (int i = 0; i < N; i++) {
                ((CachedItem) list.get(i)).writeToParcel(data, 0);
            }
            this.mRemote.transact(2, data, reply, 0);
            if (list != null) {
                for (CachedItem item : list) {
                    item.recycle();
                }
                list.clear();
            }
            reply.readException();
            reply.recycle();
            data.recycle();
        }

        public Cursor search(String pkgName, String queryStr) throws RemoteException {
            Cursor adaptor = new BulkCursorToCursorAdaptor();
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(IIndexSearchManager.descriptor);
                data.writeString(pkgName);
                data.writeString(queryStr);
                data.writeStrongBinder(adaptor.getObserver().asBinder());
                this.mRemote.transact(3, data, reply, 0);
                reply.readException();
                if (reply.readInt() != 0) {
                    adaptor.initialize((BulkCursorDescriptor) BulkCursorDescriptor.CREATOR.createFromParcel(reply));
                } else {
                    adaptor.close();
                    adaptor = null;
                }
                data.recycle();
                reply.recycle();
                return adaptor;
            } catch (RemoteException ex) {
                adaptor.close();
                throw ex;
            } catch (RuntimeException ex2) {
                adaptor.close();
                throw ex2;
            } catch (Throwable th) {
                data.recycle();
                reply.recycle();
            }
        }

        public Cursor search(String pkgName, String queryStr, String fieldStr) throws RemoteException {
            Cursor adaptor = new BulkCursorToCursorAdaptor();
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(IIndexSearchManager.descriptor);
                data.writeString(pkgName);
                data.writeString(queryStr);
                data.writeString(fieldStr);
                data.writeStrongBinder(adaptor.getObserver().asBinder());
                Trace.traceBegin(1, "IndexSearchManager.binderSearch");
                this.mRemote.transact(4, data, reply, 0);
                Trace.traceEnd(1);
                reply.readException();
                if (reply.readInt() != 0) {
                    adaptor.initialize((BulkCursorDescriptor) BulkCursorDescriptor.CREATOR.createFromParcel(reply));
                } else {
                    adaptor.close();
                    adaptor = null;
                }
                data.recycle();
                reply.recycle();
                return adaptor;
            } catch (RemoteException ex) {
                try {
                    adaptor.close();
                    throw ex;
                } catch (Throwable th) {
                    data.recycle();
                    reply.recycle();
                }
            } catch (RuntimeException ex2) {
                adaptor.close();
                throw ex2;
            } catch (Throwable th2) {
                Trace.traceEnd(1);
            }
        }
    }

    private void handleUnbindService() {
        this.mHandler.removeMessages(1002);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1002), 10000);
    }

    private void handleBootCompleted() {
        if (this.mService != null) {
            try {
                this.mService.bootCompleted();
                handleUnbindService();
            } catch (RemoteException e) {
                Log.e(TAG, "MSG_BOOTCOMPLETE mService.bootCompleted error", e);
            }
        } else if (this.mBound) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1003), 500);
        } else if (ensureIndexSearchServiceBound()) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1003), 1000);
        }
    }

    private void handleTriggerIdle() {
        if (this.mService != null) {
            try {
                this.mService.triggerIdle();
            } catch (RemoteException e) {
                Log.e(TAG, "mService.triggerIdle error", e);
            }
        } else if (this.mBound) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1004), 500);
        } else if (ensureIndexSearchServiceBound()) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1004), 1000);
        }
    }

    public static synchronized IndexSearchManager getInstance() {
        IndexSearchManager indexSearchManager;
        synchronized (IndexSearchManager.class) {
            if (mInstance == null) {
                mInstance = new IndexSearchManager();
            }
            indexSearchManager = mInstance;
        }
        return indexSearchManager;
    }

    private IndexSearchManager() {
        this.mHandlerThread.start();
        this.mHandler = new IndexSearchHandler(this.mHandlerThread.getLooper());
    }

    public IIndexSearchManager asInterface(IBinder b) {
        return new IndexSearchManagerProxy(b);
    }

    public void clearUserIndexSearchData(String packageName, int userId) {
        if (isMonitorPackage(packageName)) {
            ArrayList<CachedItem> itemList = new ArrayList();
            List idList = new ArrayList();
            idList.add("CLEAR_USER_DATA_OP");
            itemList.add(new CachedItem(packageName, 8, idList, userId));
            buildIndex(itemList);
        }
    }

    private boolean isMonitorPackage(String packageName) {
        for (String equals : IndexSearchConstants.MONITOR_ALL_PACKAGE_NAME) {
            if (equals.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean ensureIndexSearchServiceBound() {
        Intent intent = new Intent(ACTION_BIND_INDEX_SERVICE).setPackage("com.huawei.indexsearch");
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
        if (!context.bindServiceAsUser(intent, this.mDefaultIndexSearchConnection, 1, Process.myUserHandle())) {
            return false;
        }
        this.mBound = true;
        return true;
    }

    private void ensureIndexSearchServiceUnBound() {
        this.mService = null;
        this.mBound = false;
        ActivityThread.currentApplication().getApplicationContext().unbindService(this.mDefaultIndexSearchConnection);
    }

    public IIndexSearchManager getService() {
        return this.mService;
    }

    public Cursor search(String pkgName, String strquery) {
        try {
            if (this.mService != null) {
                return this.mService.search(pkgName, strquery);
            }
            Log.w(TAG, "mService is null");
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "binder mService error when search!");
        }
    }

    public Cursor search(String pkgName, String strquery, String searchFieldStr) {
        try {
            if (this.mService != null) {
                return this.mService.search(pkgName, strquery, searchFieldStr);
            }
            Log.w(TAG, "mService is null");
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "binder mService error when search on searchFieldStr!");
        }
    }

    public void buildIndex(List<CachedItem> items) {
        this.mHandler.obtainMessage(1000, items).sendToTarget();
    }

    public void forceStopBuildIndex(String path, long time) {
        try {
            if (this.mService != null) {
                this.mService.forceStopBuildIndex(path, time);
            } else {
                if (!this.mBound) {
                    ensureIndexSearchServiceBound();
                } else if (Log.HWINFO) {
                    Log.d(TAG, "service has already bound, but service is null");
                }
                int i = 0;
                while (i < 50) {
                    if (this.mService != null) {
                        this.mService.forceStopBuildIndex(path, time);
                        return;
                    } else {
                        SystemClock.sleep(10);
                        i++;
                    }
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "binder error when force stop build index!");
        }
    }

    public void bootCompleted() {
        this.mHandler.obtainMessage(1003).sendToTarget();
    }

    public void triggerIdle() {
        if (this.mHandler.hasMessages(1004)) {
            Log.d(TAG, "removing message MSG_TRIGGER_IDLE");
            this.mHandler.removeMessages(1004);
        }
        this.mHandler.obtainMessage(1004).sendToTarget();
    }
}
