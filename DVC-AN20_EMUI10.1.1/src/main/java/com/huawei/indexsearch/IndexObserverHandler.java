package com.huawei.indexsearch;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.huawei.nb.searchmanager.emuiclient.SearchServiceProxy;
import com.huawei.nb.searchmanager.emuiclient.connect.ServiceConnectCallback;

public class IndexObserverHandler extends Handler {
    private static final int DELAY_TIME = 10000;
    public static final int MSG_BOUND = 1001;
    public static final int MSG_BUILD = 1000;
    public static final int MSG_UNBOUND = 1002;
    private static final String TAG = "IndexObserverHandler";
    private IndexTaskQueue indexTaskQueue = null;
    private SearchServiceProxy searchServiceProxy;

    public IndexObserverHandler(Looper looper, Context context) {
        super(looper);
        this.searchServiceProxy = new SearchServiceProxy(context);
        this.indexTaskQueue = IndexTaskQueue.getInstance();
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1000:
                SearchTaskItem task = (SearchTaskItem) msg.obj;
                int queueSize = this.indexTaskQueue.getQueueSize();
                if (this.searchServiceProxy.hasConnected()) {
                    this.indexTaskQueue.add(task);
                    if (queueSize == 0) {
                        removeMessages(MSG_UNBOUND);
                        sendEmptyMessage(MSG_BOUND);
                        return;
                    }
                    return;
                } else if (this.searchServiceProxy.connect(new ServiceConnectCallback() {
                    /* class com.huawei.indexsearch.IndexObserverHandler.AnonymousClass1 */

                    @Override // com.huawei.nb.searchmanager.emuiclient.connect.ServiceConnectCallback
                    public void onConnect() {
                        IndexObserverHandler.this.sendEmptyMessage(IndexObserverHandler.MSG_BOUND);
                    }

                    @Override // com.huawei.nb.searchmanager.emuiclient.connect.ServiceConnectCallback
                    public void onDisconnect() {
                    }
                })) {
                    this.indexTaskQueue.add(task);
                    return;
                } else {
                    Log.e(TAG, "failed to bind search service.");
                    return;
                }
            case MSG_BOUND /*{ENCODED_INT: 1001}*/:
                if (!this.searchServiceProxy.hasConnected()) {
                    if (!this.searchServiceProxy.isBinded()) {
                        Log.e(TAG, "failed to bind observer service, clear all cache task.");
                        this.indexTaskQueue.clear();
                        return;
                    }
                    return;
                } else if (this.indexTaskQueue.getQueueSize() > 0) {
                    SearchTaskItem task2 = this.indexTaskQueue.take();
                    if (task2 != null) {
                        if (task2.getIds() != null) {
                            this.searchServiceProxy.executeDBCrawl(task2.getPkgName(), task2.getIds(), task2.getOp());
                        } else {
                            Log.w(TAG, "task ids empty.");
                        }
                    }
                    if (this.indexTaskQueue.getQueueSize() != 0) {
                        sendEmptyMessage(MSG_BOUND);
                        return;
                    } else if (this.searchServiceProxy.hasConnected()) {
                        removeMessages(MSG_UNBOUND);
                        sendEmptyMessageDelayed(MSG_UNBOUND, 10000);
                        return;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            case MSG_UNBOUND /*{ENCODED_INT: 1002}*/:
                this.searchServiceProxy.disconnect();
                return;
            default:
                Log.w(TAG, "msg unhandled.");
                return;
        }
    }
}
