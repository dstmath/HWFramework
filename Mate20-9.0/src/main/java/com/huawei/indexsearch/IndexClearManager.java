package com.huawei.indexsearch;

import android.app.ActivityThread;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.huawei.nb.searchmanager.emuiclient.SearchServiceProxy;
import com.huawei.nb.searchmanager.emuiclient.connect.ServiceConnectCallback;
import java.util.ArrayList;

public class IndexClearManager implements IIndexClearManager {
    private static final int INIT_CLEAR_INDEX = 1000;
    static final String[] MONITOR_ALL_PACKAGE_NAME = {"com.android.email", "com.example.android.notepad", "com.android.providers.calendar", "com.android.providers.telephony", "com.android.mms"};
    private static final int MSG_BOUND = 1001;
    private static final int MSG_UNBOUND = 1002;
    private static final String TAG = "IndexClearManager";
    private static volatile IndexClearManager mInstance = null;
    final ArrayList<CachedItem> mCachedItemList = new ArrayList<>();
    /* access modifiers changed from: private */
    public Handler mHandler;
    HandlerThread mHandlerThread = new HandlerThread("IndexSearchManager");
    /* access modifiers changed from: private */
    public SearchServiceProxy searchServiceProxy = new SearchServiceProxy(ActivityThread.currentApplication().getApplicationContext());

    class CachedItem {
        public String pkgName;
        public int userId;

        public CachedItem(String pkgName2, int userId2) {
            this.pkgName = pkgName2;
            this.userId = userId2;
        }
    }

    class IndexSearchHandler extends Handler {
        public IndexSearchHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    CachedItem item = (CachedItem) msg.obj;
                    int size = IndexClearManager.this.mCachedItemList.size();
                    if (IndexClearManager.this.searchServiceProxy.hasConnected()) {
                        IndexClearManager.this.mCachedItemList.add(size, item);
                        if (size == 0) {
                            IndexClearManager.this.mHandler.removeMessages(1002);
                            IndexClearManager.this.mHandler.sendEmptyMessage(1001);
                            return;
                        }
                        return;
                    } else if (IndexClearManager.this.searchServiceProxy.connect(new ServiceConnectCallback() {
                        public void onConnect() {
                            IndexSearchHandler.this.sendEmptyMessage(1001);
                        }

                        public void onDisconnect() {
                        }
                    })) {
                        IndexClearManager.this.mCachedItemList.add(item);
                        return;
                    } else {
                        Log.e(IndexClearManager.TAG, "failed to bind search service");
                        return;
                    }
                case 1001:
                    if (!IndexClearManager.this.searchServiceProxy.hasConnected()) {
                        if (!IndexClearManager.this.searchServiceProxy.isBinded()) {
                            Log.e(IndexClearManager.TAG, "failed to bind observer service, clear all cache task");
                            IndexClearManager.this.mCachedItemList.clear();
                            return;
                        }
                        return;
                    } else if (IndexClearManager.this.mCachedItemList.size() > 0) {
                        CachedItem item2 = IndexClearManager.this.mCachedItemList.remove(0);
                        IndexClearManager.this.searchServiceProxy.clearUserIndexSearchData(item2.pkgName, item2.userId);
                        if (IndexClearManager.this.mCachedItemList.size() != 0) {
                            sendEmptyMessage(1001);
                            return;
                        } else if (IndexClearManager.this.searchServiceProxy.hasConnected()) {
                            IndexClearManager.this.mHandler.removeMessages(1002);
                            IndexClearManager.this.mHandler.sendEmptyMessageDelayed(1002, 10000);
                            return;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                case 1002:
                    if (IndexClearManager.this.mCachedItemList.size() == 0) {
                        IndexClearManager.this.searchServiceProxy.disconnect();
                        return;
                    }
                    return;
                default:
                    Log.w(IndexClearManager.TAG, "msg unhandled");
                    return;
            }
        }
    }

    public static IndexClearManager getInstance() {
        if (mInstance == null) {
            mInstance = new IndexClearManager();
        }
        return mInstance;
    }

    private IndexClearManager() {
        this.mHandlerThread.start();
        this.mHandler = new IndexSearchHandler(this.mHandlerThread.getLooper());
    }

    private boolean isMonitorPackage(String packageName) {
        for (String equals : MONITOR_ALL_PACKAGE_NAME) {
            if (equals.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public void clearUserIndexSearchData(String pkgName, int userId) {
        if (isMonitorPackage(pkgName)) {
            this.mHandler.obtainMessage(1000, new CachedItem(pkgName, userId)).sendToTarget();
        }
    }
}
