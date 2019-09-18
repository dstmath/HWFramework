package com.huawei.nb.searchmanager.client;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.nb.query.bulkcursor.BulkCursorDescriptor;
import com.huawei.nb.searchmanager.client.ISearchServiceCall;
import com.huawei.nb.searchmanager.client.connect.RemoteServiceConnection;
import com.huawei.nb.searchmanager.client.connect.ServiceConnectCallback;
import com.huawei.nb.utils.logger.DSLog;
import java.util.List;

public class SearchServiceProxy implements ISearchClient {
    private static final String SEARCH_SERVICE_ACTION = "com.huawei.nb.searchmanager.service.SearchService.START";
    private static final String TAG = "SearchServiceProxy";
    private static final long TIMEOUT_MILLISECONDS = 5000;
    private volatile long callbackTimeout;
    private String callingPkgName;
    private volatile ServiceConnectCallback connectCallback = null;
    private final RemoteServiceConnection dsConnection;
    /* access modifiers changed from: private */
    public volatile boolean hasBinded;
    /* access modifiers changed from: private */
    public volatile boolean hasConnected;
    private final Object locker;
    /* access modifiers changed from: private */
    public volatile ISearchServiceCall searchService = null;

    public SearchServiceProxy(Context context) {
        this.dsConnection = new RemoteServiceConnection(context, SEARCH_SERVICE_ACTION);
        this.callingPkgName = context.getPackageName();
        this.locker = new Object();
        this.hasConnected = false;
        this.hasBinded = false;
        this.callbackTimeout = TIMEOUT_MILLISECONDS;
    }

    /* access modifiers changed from: private */
    public void invokeConnectCallback(boolean connected) {
        if (connected) {
            if (this.connectCallback != null) {
                this.connectCallback.onConnect();
            }
        } else if (this.connectCallback != null) {
            this.connectCallback.onDisconnect();
        }
    }

    public boolean connect() {
        return connect(null);
    }

    public boolean connect(ServiceConnectCallback callback) {
        boolean z = true;
        synchronized (this.locker) {
            if (!this.hasBinded) {
                this.connectCallback = callback;
                if (this.searchService != null) {
                    invokeConnectCallback(true);
                }
                this.hasBinded = this.dsConnection.open(new RemoteServiceConnection.OnConnectListener() {
                    public void onConnect(IBinder binder) {
                        if (binder != null) {
                            ISearchServiceCall unused = SearchServiceProxy.this.searchService = ISearchServiceCall.Stub.asInterface(binder);
                            boolean unused2 = SearchServiceProxy.this.hasConnected = true;
                            SearchServiceProxy.this.invokeConnectCallback(true);
                            DSLog.i("SearchServiceProxy Succeed to connect", new Object[0]);
                        }
                    }

                    public void onDisconnect() {
                        ISearchServiceCall unused = SearchServiceProxy.this.searchService = null;
                        boolean unused2 = SearchServiceProxy.this.hasConnected = false;
                        boolean unused3 = SearchServiceProxy.this.hasBinded = false;
                        SearchServiceProxy.this.invokeConnectCallback(false);
                        DSLog.w("SearchServiceProxy Connection to is broken down", new Object[0]);
                    }
                });
                if (!this.hasBinded) {
                    DSLog.e("SearchServiceProxy Failed to open connection", new Object[0]);
                }
                z = this.hasBinded;
            }
        }
        return z;
    }

    public boolean disconnect() {
        synchronized (this.locker) {
            if (this.hasBinded) {
                invokeConnectCallback(false);
                this.dsConnection.close();
                this.searchService = null;
                this.hasBinded = false;
                this.hasConnected = false;
                DSLog.i("SearchServiceProxy close connection", new Object[0]);
            } else {
                DSLog.i("SearchServiceProxy Connection has been closed already", new Object[0]);
            }
        }
        return true;
    }

    public boolean hasConnected() {
        return this.hasConnected;
    }

    public boolean isBinded() {
        return this.hasBinded;
    }

    public void setExecutionTimeout(long timeout) {
        this.callbackTimeout = timeout;
    }

    public void executeDBCrawl(String pkgName, List<String> idList, int op) {
        if (this.searchService == null) {
            DSLog.e("Failed to executeDBCrawl, error: searchService is null.", new Object[0]);
            return;
        }
        try {
            this.searchService.executeDBCrawl(pkgName, idList, op, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.e("SearchServiceProxy Failed to executeDBCrawl " + e.getMessage(), new Object[0]);
        }
    }

    public BulkCursorDescriptor executeSearch(String pkgName, String queryString, List<String> fieldList, List<Attributes> attrsList) {
        if (this.searchService == null) {
            DSLog.e("Failed to executeSearch, error: searchService is null.", new Object[0]);
            return null;
        }
        try {
            return this.searchService.executeSearch(pkgName, queryString, fieldList, attrsList, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.e("SearchServiceProxy Failed to executeSearch " + e.getMessage(), new Object[0]);
            return null;
        }
    }

    public void clearUserIndexSearchData(String pkgName, int userId) {
        if (this.searchService == null) {
            DSLog.e("Failed to clearUserIndexSearchData, error: searchService is null.", new Object[0]);
            return;
        }
        try {
            this.searchService.executeClearData(pkgName, userId, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.e("SearchServiceProxy Failed to executeClearData " + e.getMessage(), new Object[0]);
        }
    }

    public void executeFileCrawl(String pkgName, String filePath, boolean crawlContent, int op) {
        if (this.searchService == null) {
            DSLog.e("Failed to executeFileCrawl, error: searchService is null.", new Object[0]);
            return;
        }
        try {
            this.searchService.executeFileCrawl(pkgName, filePath, crawlContent, op, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.e("SearchServiceProxy Failed to executeFileCrawl " + e.getMessage(), new Object[0]);
        }
    }

    /* JADX WARNING: Unknown variable types count: 1 */
    public int executeInsertIndex(String pkgName, List<SearchIndexData> dataList, List<Attributes> attrsList) {
        ? r1 = 0;
        if (this.searchService == null) {
            DSLog.e("Failed to executeInsertIndex, error: searchService is null.", new Object[r1]);
            return r1;
        }
        try {
            return this.searchService.executeInsertIndex(pkgName, dataList, attrsList, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.e("SearchServiceProxy Failed to executeInsertIndex " + e.getMessage(), new Object[r1]);
            return r1;
        }
    }

    /* JADX WARNING: Unknown variable types count: 1 */
    public int executeUpdateIndex(String pkgName, List<SearchIndexData> dataList, List<Attributes> attrsList) {
        ? r1 = 0;
        if (this.searchService == null) {
            DSLog.e("Failed to executeUpdateIndex, error: searchService is null.", new Object[r1]);
            return r1;
        }
        try {
            return this.searchService.executeUpdateIndex(pkgName, dataList, attrsList, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.e("SearchServiceProxy Failed to executeUpdateIndex " + e.getMessage(), new Object[r1]);
            return r1;
        }
    }

    /* JADX WARNING: Unknown variable types count: 1 */
    public int executeDeleteIndex(String pkgName, List<String> idList, List<Attributes> attrsList) {
        ? r1 = 0;
        if (this.searchService == null) {
            DSLog.e("Failed to executeDeleteIndex, error: searchService is null.", new Object[r1]);
            return r1;
        }
        try {
            return this.searchService.executeDeleteIndex(pkgName, idList, attrsList, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.e("SearchServiceProxy Failed to executeDeleteIndex " + e.getMessage(), new Object[r1]);
            return r1;
        }
    }

    public List<Word> executeAnalyzeText(String pkgName, String text) {
        List<Word> list = null;
        if (this.searchService == null) {
            DSLog.e("Failed to executeAnalyzeText, error: searchService is null.", new Object[0]);
            return list;
        }
        try {
            return this.searchService.executeAnalyzeText(pkgName, text, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.e("SearchServiceProxy Failed to executeAnalyzeText " + e.getMessage(), new Object[0]);
            return list;
        }
    }

    public List<SearchIntentItem> executeIntentSearch(String pkgName, String queryString, List<String> fieldList, String type) {
        List<SearchIntentItem> list = null;
        if (this.searchService == null) {
            DSLog.e("Failed to executeIntentSearch, error: searchService is null.", new Object[0]);
            return list;
        }
        try {
            return this.searchService.executeIntentSearch(pkgName, queryString, fieldList, type, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.e("SearchServiceProxy Failed to executeIntentSearch " + e.getMessage(), new Object[0]);
            return list;
        }
    }

    public void setSearchSwitch(String pkgName, boolean isSwitchOn) {
        if (this.searchService == null) {
            DSLog.e("Failed to setSearchSwitch, error: searchService is null.", new Object[0]);
            return;
        }
        try {
            this.searchService.setSearchSwitch(pkgName, isSwitchOn, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.e("SearchServiceProxy Failed to setSearchSwitch " + e.getMessage(), new Object[0]);
        }
    }
}
