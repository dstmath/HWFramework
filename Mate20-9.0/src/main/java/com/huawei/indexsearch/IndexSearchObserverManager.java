package com.huawei.indexsearch;

import android.app.ActivityThread;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import java.util.List;

public class IndexSearchObserverManager {
    private static final String TAG = "IndexSearchObserverManager";
    private static volatile IndexSearchObserverManager mInstance = null;
    private Context context = ActivityThread.currentApplication().getApplicationContext();
    private Handler mHandler;
    private HandlerThread mHandlerThread = new HandlerThread(TAG);

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
        this.mHandler = new IndexObserverHandler(this.mHandlerThread.getLooper(), this.context);
    }

    public void buildIndex(String mPkgName, List<String> idList, int op) {
        SearchTaskItem searchTaskItem = new SearchTaskItem();
        searchTaskItem.setPkgName(mPkgName);
        searchTaskItem.setIds(idList);
        searchTaskItem.setOp(op);
        this.mHandler.obtainMessage(1000, searchTaskItem).sendToTarget();
    }
}
