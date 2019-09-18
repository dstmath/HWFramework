package com.huawei.indexsearch;

import android.app.ActivityThread;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.nb.searchmanager.emuiclient.SearchServiceProxy;
import com.huawei.nb.searchmanager.emuiclient.connect.ServiceConnectCallback;
import com.huawei.nb.searchmanager.emuiclient.query.bulkcursor.BulkCursorDescriptorEx;
import com.huawei.nb.searchmanager.emuiclient.query.bulkcursor.BulkCursorToCursorAdaptor;
import java.util.Arrays;

public class IndexSearchManager implements IIndexSearchManager {
    private static final String TAG = "IndexSearchManager";
    private static volatile IndexSearchManager mInstance = null;
    private Context context = ActivityThread.currentApplication().getApplicationContext();
    private SearchServiceProxy searchServiceProxy = new SearchServiceProxy(this.context);

    public static IndexSearchManager getInstance() {
        if (mInstance == null) {
            mInstance = new IndexSearchManager();
        }
        return mInstance;
    }

    private IndexSearchManager() {
        Log.i(TAG, "new IndexSearchManager");
    }

    public void connect() {
        Log.i(TAG, "connect");
        this.searchServiceProxy.connect(new ServiceConnectCallback() {
            public void onConnect() {
            }

            public void onDisconnect() {
            }
        });
    }

    public Cursor search(String pkgName, String queryStr, String strField) {
        Log.i(TAG, "search");
        if (!this.searchServiceProxy.hasConnected()) {
            Log.e(TAG, "not Connected, return null");
            return null;
        } else if (TextUtils.isEmpty(strField)) {
            Log.i(TAG, "search strField is null");
            return null;
        } else {
            String[] fields = strField.split(",");
            if (fields == null || fields.length <= 0) {
                Log.i(TAG, "search fields is null");
                return null;
            }
            BulkCursorDescriptorEx bulkCursorDescriptor = this.searchServiceProxy.executeSearch(pkgName, queryStr, Arrays.asList(fields));
            if (bulkCursorDescriptor == null) {
                return null;
            }
            BulkCursorToCursorAdaptor adaptor = new BulkCursorToCursorAdaptor();
            adaptor.initialize(bulkCursorDescriptor);
            Log.i(TAG, "return adaptor");
            return adaptor;
        }
    }

    public boolean hasConnected() {
        return this.searchServiceProxy.hasConnected();
    }

    public void destroy() {
        if (this.searchServiceProxy.hasConnected()) {
            this.searchServiceProxy.disconnect();
        }
        mInstance = null;
    }
}
