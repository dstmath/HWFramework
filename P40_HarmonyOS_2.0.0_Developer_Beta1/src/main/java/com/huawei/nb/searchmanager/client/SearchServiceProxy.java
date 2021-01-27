package com.huawei.nb.searchmanager.client;

import android.content.Context;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SharedMemory;
import android.system.ErrnoException;
import com.huawei.nb.searchmanager.callback.IndexChangeCallback;
import com.huawei.nb.searchmanager.client.ISearchServiceCall;
import com.huawei.nb.searchmanager.client.connect.RemoteServiceConnection;
import com.huawei.nb.searchmanager.client.connect.ServiceConnectCallback;
import com.huawei.nb.searchmanager.client.exception.SearchResult;
import com.huawei.nb.searchmanager.client.listener.IIndexChangeListener;
import com.huawei.nb.searchmanager.client.model.IndexData;
import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.SearchableEntity;
import com.huawei.nb.searchmanager.client.schema.IndexSchemaType;
import com.huawei.nb.searchmanager.distribute.DeviceChangeCallback;
import com.huawei.nb.searchmanager.distribute.DeviceChangeListener;
import com.huawei.nb.searchmanager.distribute.DeviceInfo;
import com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback;
import com.huawei.nb.searchmanager.distribute.RemoteSearchCallback;
import com.huawei.nb.searchmanager.distribute.RemoteSearchListener;
import com.huawei.nb.searchmanager.distribute.RemoteSearchSession;
import com.huawei.nb.searchmanager.query.bulkcursor.BulkCursorDescriptor;
import com.huawei.nb.searchmanager.query.bulkcursor.BulkCursorToCursorAdaptor;
import com.huawei.nb.searchmanager.utils.SharedMemoryHelper;
import com.huawei.nb.searchmanager.utils.Waiter;
import com.huawei.nb.searchmanager.utils.logger.DSLog;
import java.nio.BufferOverflowException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SearchServiceProxy implements ISearchClient {
    private static final int BINDER_ERROR = -1;
    private static final String SEARCH_SERVICE_ACTION = "com.huawei.nb.searchmanager.service.SearchService.START";
    private static final String TAG = "SearchServiceProxy";
    private static final long TIMEOUT_MILLISECONDS = 5000;
    private volatile long callbackTimeout;
    private final String callingPkgName;
    private IBinder clientDeathBinder;
    private volatile ServiceConnectCallback connectCallback = null;
    private final Object lock;
    private final RemoteServiceConnection searchConnection;
    private volatile ISearchServiceCall searchService = null;

    public static int getApiVersion() {
        return 8;
    }

    public static String getApiVersionName() {
        return "11.1.0";
    }

    private class SyncConnectionListener implements RemoteServiceConnection.OnConnectListener {
        private final Waiter waiter;

        SyncConnectionListener(Waiter waiter2) {
            this.waiter = waiter2;
        }

        @Override // com.huawei.nb.searchmanager.client.connect.RemoteServiceConnection.OnConnectListener
        public void onConnect(IBinder iBinder) {
            if (iBinder != null) {
                SearchServiceProxy.this.searchService = ISearchServiceCall.Stub.asInterface(iBinder);
                try {
                    SearchServiceProxy.this.searchService.registerClientDeathBinder(SearchServiceProxy.this.clientDeathBinder, SearchServiceProxy.this.callingPkgName);
                } catch (RemoteException e) {
                    DSLog.et(SearchServiceProxy.TAG, "registerClientDeathBinder RemoteException: " + e.getMessage(), new Object[0]);
                }
                Waiter waiter2 = this.waiter;
                if (waiter2 != null) {
                    waiter2.signal();
                }
                DSLog.it(SearchServiceProxy.TAG, "Succeed sync connect search service", new Object[0]);
            }
        }

        @Override // com.huawei.nb.searchmanager.client.connect.RemoteServiceConnection.OnConnectListener
        public void onDisconnect() {
            SearchServiceProxy.this.searchService = null;
            DSLog.it(SearchServiceProxy.TAG, "sync connection to search service is broken down.", new Object[0]);
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    @Deprecated
    public int getApiVersionCode() {
        return getApiVersion();
    }

    public SearchServiceProxy(Context context) {
        this.searchConnection = new RemoteServiceConnection(context, SEARCH_SERVICE_ACTION);
        this.callingPkgName = context.getPackageName();
        this.lock = new Object();
        this.callbackTimeout = TIMEOUT_MILLISECONDS;
        this.clientDeathBinder = new Binder();
        DSLog.init("HwSearchService: Client", 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void invokeConnectCallback(boolean z) {
        if (z) {
            if (this.connectCallback != null) {
                this.connectCallback.onConnect();
            }
        } else if (this.connectCallback != null) {
            this.connectCallback.onDisconnect();
        }
    }

    public boolean connect() {
        synchronized (this.lock) {
            if (this.searchService != null) {
                return true;
            }
            Waiter waiter = new Waiter();
            SyncConnectionListener syncConnectionListener = new SyncConnectionListener(waiter);
            int i = 3;
            while (true) {
                int i2 = i - 1;
                if (i <= 0) {
                    DSLog.et(TAG, "failed to connect to search service in 3 times", new Object[0]);
                    return false;
                } else if (this.searchService != null) {
                    return true;
                } else {
                    if (this.searchConnection.open(syncConnectionListener) && waiter.await(this.callbackTimeout)) {
                        return true;
                    }
                    i = i2;
                }
            }
        }
    }

    public boolean connect(ServiceConnectCallback serviceConnectCallback) {
        synchronized (this.lock) {
            this.connectCallback = serviceConnectCallback;
            if (this.searchService != null) {
                invokeConnectCallback(true);
                return true;
            }
            boolean open = this.searchConnection.open(new RemoteServiceConnection.OnConnectListener() {
                /* class com.huawei.nb.searchmanager.client.SearchServiceProxy.AnonymousClass1 */

                @Override // com.huawei.nb.searchmanager.client.connect.RemoteServiceConnection.OnConnectListener
                public void onConnect(IBinder iBinder) {
                    if (iBinder != null) {
                        SearchServiceProxy.this.searchService = ISearchServiceCall.Stub.asInterface(iBinder);
                        try {
                            SearchServiceProxy.this.searchService.registerClientDeathBinder(SearchServiceProxy.this.clientDeathBinder, SearchServiceProxy.this.callingPkgName);
                        } catch (RemoteException e) {
                            DSLog.et(SearchServiceProxy.TAG, "registerClientDeathBinder RemoteException: " + e.getMessage(), new Object[0]);
                        }
                        DSLog.it(SearchServiceProxy.TAG, "Succeed async connect search service", new Object[0]);
                        SearchServiceProxy.this.invokeConnectCallback(true);
                    }
                }

                @Override // com.huawei.nb.searchmanager.client.connect.RemoteServiceConnection.OnConnectListener
                public void onDisconnect() {
                    SearchServiceProxy.this.searchService = null;
                    DSLog.it(SearchServiceProxy.TAG, "async connection to search service is broken down.", new Object[0]);
                    SearchServiceProxy.this.invokeConnectCallback(false);
                }
            });
            if (!open) {
                DSLog.et(TAG, "Failed to open search service connection.", new Object[0]);
            }
            return open;
        }
    }

    public boolean disconnect() {
        synchronized (this.lock) {
            if (this.searchService != null) {
                try {
                    this.searchService.unRegisterClientDeathBinder(this.clientDeathBinder, this.callingPkgName);
                } catch (RemoteException e) {
                    DSLog.et(TAG, "unRegisterClientDeathBinder RemoteException: " + e.getMessage(), new Object[0]);
                }
                this.searchConnection.close();
                this.searchService = null;
                DSLog.it(TAG, "succeed close search service connection.", new Object[0]);
                invokeConnectCallback(false);
            } else {
                DSLog.it(TAG, "search service connection has been closed already.", new Object[0]);
            }
        }
        return true;
    }

    public boolean hasConnected() {
        return this.searchService != null;
    }

    @Deprecated
    public boolean isBinded() {
        return this.searchService != null;
    }

    public void setExecutionTimeout(long j) {
        this.callbackTimeout = j;
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public void executeDBCrawl(String str, List<String> list, int i) {
        if (this.searchService == null) {
            DSLog.e("Failed to executeDBCrawl, error: searchService is null.", new Object[0]);
            return;
        }
        try {
            this.searchService.executeDBCrawl(str, list, i, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to executeDBCrawl, errMsg: %s", e.getMessage());
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    @Deprecated
    public Cursor executeSearch(String str, String str2, List<String> list, List<Attributes> list2) {
        if (this.searchService == null) {
            DSLog.e("Failed to executeSearch, error: searchService is null.", new Object[0]);
            return null;
        }
        try {
            BulkCursorDescriptor executeSearch = this.searchService.executeSearch(str, str2, list, list2, this.callingPkgName);
            if (executeSearch == null) {
                return null;
            }
            BulkCursorToCursorAdaptor bulkCursorToCursorAdaptor = new BulkCursorToCursorAdaptor();
            bulkCursorToCursorAdaptor.initialize(executeSearch);
            return bulkCursorToCursorAdaptor;
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to executeSearch, errMsg: %s", e.getMessage());
            return null;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    @Deprecated
    public Cursor executeSearch(String str, Bundle bundle) {
        if (this.searchService == null) {
            DSLog.e("Failed to executeSearch, error: searchService is null.", new Object[0]);
            return null;
        }
        try {
            BulkCursorDescriptor executeMultiSearch = this.searchService.executeMultiSearch(str, bundle, this.callingPkgName);
            if (executeMultiSearch == null) {
                return null;
            }
            BulkCursorToCursorAdaptor bulkCursorToCursorAdaptor = new BulkCursorToCursorAdaptor();
            bulkCursorToCursorAdaptor.initialize(executeMultiSearch);
            return bulkCursorToCursorAdaptor;
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to executeSearch, errMsg: %s", e.getMessage());
            return null;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    @Deprecated
    public void clearUserIndexSearchData(String str, int i) {
        if (this.searchService == null) {
            DSLog.e("Failed to clearUserIndexSearchData, error: searchService is null.", new Object[0]);
            return;
        }
        try {
            this.searchService.executeClearData(str, i, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to executeClearData, errMsg: %s", e.getMessage());
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    @Deprecated
    public void executeFileCrawl(String str, String str2, boolean z, int i) {
        if (this.searchService == null) {
            DSLog.e("Failed to executeFileCrawl, error: searchService is null.", new Object[0]);
            return;
        }
        try {
            this.searchService.executeFileCrawl(str, str2, z, i, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to executeFileCrawl, errMsg: %s", e.getMessage());
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    @Deprecated
    public int executeInsertIndex(String str, List<SearchIndexData> list, List<Attributes> list2) {
        if (this.searchService == null) {
            DSLog.e("Failed to executeInsertIndex, error: searchService is null.", new Object[0]);
            return 0;
        }
        try {
            return this.searchService.executeInsertIndex(str, list, list2, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to executeInsertIndex, errMsg: %s", e.getMessage());
            return 0;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    @Deprecated
    public int executeUpdateIndex(String str, List<SearchIndexData> list, List<Attributes> list2) {
        if (this.searchService == null) {
            DSLog.e("Failed to executeUpdateIndex, error: searchService is null.", new Object[0]);
            return 0;
        }
        try {
            return this.searchService.executeUpdateIndex(str, list, list2, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to executeUpdateIndex, errMsg: %s", e.getMessage());
            return 0;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    @Deprecated
    public int executeDeleteIndex(String str, List<String> list, List<Attributes> list2) {
        if (this.searchService == null) {
            DSLog.e("Failed to executeDeleteIndex, error: searchService is null.", new Object[0]);
            return 0;
        }
        try {
            return this.searchService.executeDeleteIndex(str, list, list2, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to executeDeleteIndex, errMsg: %s", e.getMessage());
            return 0;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    @Deprecated
    public List<Word> executeAnalyzeText(String str, String str2) {
        if (this.searchService == null) {
            DSLog.e("Failed to executeAnalyzeText, error: searchService is null.", new Object[0]);
            return null;
        }
        try {
            return this.searchService.executeAnalyzeText(str, str2, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to executeAnalyzeText, errMsg: %s", e.getMessage());
            return null;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public List<SearchIntentItem> executeIntentSearch(String str, String str2, List<String> list, String str3) {
        if (this.searchService == null) {
            DSLog.e("Failed to executeIntentSearch, error: searchService is null.", new Object[0]);
            return null;
        }
        try {
            return this.searchService.executeIntentSearch(str, str2, list, str3, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to executeIntentSearch, errMsg: %s", e.getMessage());
            return null;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public void setSearchSwitch(String str, boolean z) {
        if (this.searchService == null) {
            DSLog.e("Failed to setSearchSwitch, error: searchService is null.", new Object[0]);
            return;
        }
        try {
            this.searchService.setSearchSwitch(str, z, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to setSearchSwitch, errMsg: %s", e.getMessage());
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public String grantFilePermission(String str, String str2, String str3, int i) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to grantFilePermission, error: searchService is null.", new Object[0]);
            return "";
        }
        try {
            return this.searchService.grantFilePermission(str, str2, str3, i, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to grantFilePermission, errMsg: %s", e.getMessage());
            return "";
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public String revokeFilePermission(String str, String str2, String str3, int i) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to revokeFilePermission, error: searchService is null.", new Object[0]);
            return "";
        }
        try {
            return this.searchService.revokeFilePermission(str, str2, str3, i, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to revokeFilePermission, errMsg: %s", e.getMessage());
            return "";
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public int setIndexForm(String str, int i, List<IndexForm> list) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to setIndexForm, error: searchService is null.", new Object[0]);
            return 0;
        }
        try {
            return this.searchService.setIndexForm(str, i, list, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to setIndexForm, errMsg: %s", e.getMessage());
            return 0;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public int clearIndexForm(String str) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to clearIndexForm, error: searchService is null.", new Object[0]);
            return 0;
        }
        try {
            return this.searchService.clearIndexForm(str, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to clearIndexForm, errMsg: %s", e.getMessage());
            return 0;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public int getIndexFormVersion(String str) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to getIndexFormVersion, error: searchService is null.", new Object[0]);
            return -1;
        }
        try {
            return this.searchService.getIndexFormVersion(str, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to getIndexFormVersion, errMsg: %s", e.getMessage());
            return -1;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public List<IndexForm> getIndexForm(String str) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to getIndexForm, error: searchService is null.", new Object[0]);
            return null;
        }
        try {
            return this.searchService.getIndexForm(str, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to getIndexForm, errMsg: %s", e.getMessage());
            return null;
        }
    }

    private List<IndexData> getFailedList(int i, SharedMemory sharedMemory, List<IndexData> list) throws ErrnoException {
        if (i == SearchResult.SUCCESS.getRetCode()) {
            return Collections.emptyList();
        }
        return i == SearchResult.FAIL.getRetCode() ? SharedMemoryHelper.readIndexDataList(sharedMemory) : list;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:8:0x0027 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r5v0, types: [long] */
    /* JADX WARN: Type inference failed for: r5v1 */
    /* JADX WARN: Type inference failed for: r5v7 */
    /* JADX WARN: Type inference failed for: r5v8 */
    /* JADX WARN: Type inference failed for: r5v11 */
    /* JADX WARN: Type inference failed for: r5v17 */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00ef, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00f0, code lost:
        r16 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00f3, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00f4, code lost:
        r16 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00fa, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00fb, code lost:
        r16 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0101, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0102, code lost:
        r16 = r15;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00ef A[ExcHandler: all (th java.lang.Throwable), PHI: r7 
      PHI: (r7v14 android.os.SharedMemory) = (r7v0 android.os.SharedMemory), (r7v0 android.os.SharedMemory), (r7v16 android.os.SharedMemory), (r7v16 android.os.SharedMemory) binds: [B:10:0x002b, B:11:?, B:17:0x0042, B:18:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:10:0x002b] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public List<IndexData> insert(String str, String str2, List<IndexData> list) {
        SharedMemory sharedMemory;
        Throwable th;
        SharedMemory sharedMemory2;
        SharedMemory sharedMemory3;
        ErrnoException e;
        SharedMemory sharedMemory4;
        BufferOverflowException e2;
        RemoteException e3;
        List<IndexData> insert;
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to insert index, error: searchService is null", new Object[0]);
            return list;
        } else if (list == null || list.isEmpty()) {
            DSLog.et(TAG, "Failed to insert index, error: indexDataList is null", new Object[0]);
            return list;
        } else {
            ?? currentTimeMillis = System.currentTimeMillis();
            SharedMemory sharedMemory5 = null;
            try {
                SharedMemory create = SharedMemory.create("SearchDataSharedMemory", SharedMemoryHelper.LARGE_THRESHOLD);
                try {
                    if (SharedMemoryHelper.writeIndexDataList(list, create) > 153600) {
                        sharedMemory5 = SharedMemory.create("SearchFailedIndexSharedMemory", SharedMemoryHelper.LARGE_THRESHOLD);
                        if (this.searchService == null) {
                            DSLog.et(TAG, "Failed to insert large index, error: searchService is null", new Object[0]);
                            SharedMemoryHelper.releaseMemory(create);
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            return list;
                        }
                        sharedMemory = create;
                        try {
                            insert = getFailedList(this.searchService.insertLarge(str, str2, create, sharedMemory5, this.callingPkgName), sharedMemory5, list);
                            DSLog.it(TAG, "large insert " + list.size() + " index cost " + (System.currentTimeMillis() - currentTimeMillis), new Object[0]);
                        } catch (ErrnoException e4) {
                            e = e4;
                            sharedMemory3 = sharedMemory5;
                            sharedMemory5 = sharedMemory;
                            DSLog.et(TAG, "Failed to insert index, read reply memory error, errMsg: %s", e.getMessage());
                            sharedMemory2 = sharedMemory3;
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            SharedMemoryHelper.releaseMemory(sharedMemory2);
                            return list;
                        } catch (BufferOverflowException e5) {
                            e2 = e5;
                            sharedMemory4 = sharedMemory5;
                            sharedMemory5 = sharedMemory;
                            DSLog.et(TAG, "Failed to insert index, the data is out of memory, errMsg: %s", e2.getMessage());
                            sharedMemory2 = sharedMemory4;
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            SharedMemoryHelper.releaseMemory(sharedMemory2);
                            return list;
                        } catch (RemoteException e6) {
                            e3 = e6;
                            currentTimeMillis = sharedMemory5;
                            sharedMemory5 = sharedMemory;
                            DSLog.et(TAG, "Failed to insert index, remote error, errMsg: %s", e3.getMessage());
                            sharedMemory2 = currentTimeMillis;
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            SharedMemoryHelper.releaseMemory(sharedMemory2);
                            return list;
                        } catch (Throwable th2) {
                            th = th2;
                            SharedMemoryHelper.releaseMemory(sharedMemory);
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            throw th;
                        }
                    } else {
                        sharedMemory = create;
                        if (this.searchService == null) {
                            DSLog.et(TAG, "Failed to insert index originally, error: searchService is null", new Object[0]);
                            SharedMemoryHelper.releaseMemory(sharedMemory);
                            SharedMemoryHelper.releaseMemory(null);
                            return list;
                        }
                        insert = this.searchService.insert(str, str2, list, this.callingPkgName);
                        DSLog.it(TAG, "insert " + list.size() + " index cost " + (System.currentTimeMillis() - currentTimeMillis), new Object[0]);
                    }
                    SharedMemoryHelper.releaseMemory(sharedMemory);
                    SharedMemoryHelper.releaseMemory(sharedMemory5);
                    return insert;
                } catch (ErrnoException e7) {
                    e = e7;
                    sharedMemory3 = sharedMemory5;
                    sharedMemory5 = create;
                    DSLog.et(TAG, "Failed to insert index, read reply memory error, errMsg: %s", e.getMessage());
                    sharedMemory2 = sharedMemory3;
                    SharedMemoryHelper.releaseMemory(sharedMemory5);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (BufferOverflowException e8) {
                    e2 = e8;
                    sharedMemory4 = sharedMemory5;
                    sharedMemory5 = create;
                    DSLog.et(TAG, "Failed to insert index, the data is out of memory, errMsg: %s", e2.getMessage());
                    sharedMemory2 = sharedMemory4;
                    SharedMemoryHelper.releaseMemory(sharedMemory5);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (RemoteException e9) {
                    e3 = e9;
                    currentTimeMillis = sharedMemory5;
                    sharedMemory5 = create;
                    DSLog.et(TAG, "Failed to insert index, remote error, errMsg: %s", e3.getMessage());
                    sharedMemory2 = currentTimeMillis;
                    SharedMemoryHelper.releaseMemory(sharedMemory5);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (Throwable th3) {
                }
            } catch (ErrnoException e10) {
                e = e10;
                sharedMemory3 = null;
                DSLog.et(TAG, "Failed to insert index, read reply memory error, errMsg: %s", e.getMessage());
                sharedMemory2 = sharedMemory3;
                SharedMemoryHelper.releaseMemory(sharedMemory5);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (BufferOverflowException e11) {
                e2 = e11;
                sharedMemory4 = null;
                DSLog.et(TAG, "Failed to insert index, the data is out of memory, errMsg: %s", e2.getMessage());
                sharedMemory2 = sharedMemory4;
                SharedMemoryHelper.releaseMemory(sharedMemory5);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (RemoteException e12) {
                e3 = e12;
                currentTimeMillis = 0;
                DSLog.et(TAG, "Failed to insert index, remote error, errMsg: %s", e3.getMessage());
                sharedMemory2 = currentTimeMillis;
                SharedMemoryHelper.releaseMemory(sharedMemory5);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (Throwable th4) {
                th = th4;
                sharedMemory = sharedMemory5;
                sharedMemory5 = currentTimeMillis;
                SharedMemoryHelper.releaseMemory(sharedMemory);
                SharedMemoryHelper.releaseMemory(sharedMemory5);
                throw th;
            }
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:8:0x0027 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r5v0, types: [long] */
    /* JADX WARN: Type inference failed for: r5v1 */
    /* JADX WARN: Type inference failed for: r5v7 */
    /* JADX WARN: Type inference failed for: r5v8 */
    /* JADX WARN: Type inference failed for: r5v11 */
    /* JADX WARN: Type inference failed for: r5v17 */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00ef, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00f0, code lost:
        r16 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00f3, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00f4, code lost:
        r16 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00fa, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00fb, code lost:
        r16 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0101, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0102, code lost:
        r16 = r15;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00ef A[ExcHandler: all (th java.lang.Throwable), PHI: r7 
      PHI: (r7v14 android.os.SharedMemory) = (r7v0 android.os.SharedMemory), (r7v0 android.os.SharedMemory), (r7v16 android.os.SharedMemory), (r7v16 android.os.SharedMemory) binds: [B:10:0x002b, B:11:?, B:17:0x0042, B:18:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:10:0x002b] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public List<IndexData> update(String str, String str2, List<IndexData> list) {
        SharedMemory sharedMemory;
        Throwable th;
        SharedMemory sharedMemory2;
        SharedMemory sharedMemory3;
        ErrnoException e;
        SharedMemory sharedMemory4;
        BufferOverflowException e2;
        RemoteException e3;
        List<IndexData> update;
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to update index, error: searchService is null", new Object[0]);
            return list;
        } else if (list == null || list.isEmpty()) {
            DSLog.et(TAG, "Failed to update index, error: indexDataList is null", new Object[0]);
            return list;
        } else {
            ?? currentTimeMillis = System.currentTimeMillis();
            SharedMemory sharedMemory5 = null;
            try {
                SharedMemory create = SharedMemory.create("SearchDataSharedMemory", SharedMemoryHelper.LARGE_THRESHOLD);
                try {
                    if (SharedMemoryHelper.writeIndexDataList(list, create) > 153600) {
                        sharedMemory5 = SharedMemory.create("SearchFailedIndexSharedMemory", SharedMemoryHelper.LARGE_THRESHOLD);
                        if (this.searchService == null) {
                            DSLog.et(TAG, "Failed to update large index, error: searchService is null", new Object[0]);
                            SharedMemoryHelper.releaseMemory(create);
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            return list;
                        }
                        sharedMemory = create;
                        try {
                            update = getFailedList(this.searchService.updateLarge(str, str2, create, sharedMemory5, this.callingPkgName), sharedMemory5, list);
                            DSLog.it(TAG, "large update " + list.size() + " index cost " + (System.currentTimeMillis() - currentTimeMillis), new Object[0]);
                        } catch (ErrnoException e4) {
                            e = e4;
                            sharedMemory3 = sharedMemory5;
                            sharedMemory5 = sharedMemory;
                            DSLog.et(TAG, "Failed to update index, read reply memory error, errMsg: %s", e.getMessage());
                            sharedMemory2 = sharedMemory3;
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            SharedMemoryHelper.releaseMemory(sharedMemory2);
                            return list;
                        } catch (BufferOverflowException e5) {
                            e2 = e5;
                            sharedMemory4 = sharedMemory5;
                            sharedMemory5 = sharedMemory;
                            DSLog.et(TAG, "Failed to update index, the data is out of memory, errMsg: %s", e2.getMessage());
                            sharedMemory2 = sharedMemory4;
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            SharedMemoryHelper.releaseMemory(sharedMemory2);
                            return list;
                        } catch (RemoteException e6) {
                            e3 = e6;
                            currentTimeMillis = sharedMemory5;
                            sharedMemory5 = sharedMemory;
                            DSLog.et(TAG, "Failed to update index, remote error, errMsg: %s", e3.getMessage());
                            sharedMemory2 = currentTimeMillis;
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            SharedMemoryHelper.releaseMemory(sharedMemory2);
                            return list;
                        } catch (Throwable th2) {
                            th = th2;
                            SharedMemoryHelper.releaseMemory(sharedMemory);
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            throw th;
                        }
                    } else {
                        sharedMemory = create;
                        if (this.searchService == null) {
                            DSLog.et(TAG, "Failed to update index originally, error: searchService is null", new Object[0]);
                            SharedMemoryHelper.releaseMemory(sharedMemory);
                            SharedMemoryHelper.releaseMemory(null);
                            return list;
                        }
                        update = this.searchService.update(str, str2, list, this.callingPkgName);
                        DSLog.it(TAG, "update " + list.size() + " index cost " + (System.currentTimeMillis() - currentTimeMillis), new Object[0]);
                    }
                    SharedMemoryHelper.releaseMemory(sharedMemory);
                    SharedMemoryHelper.releaseMemory(sharedMemory5);
                    return update;
                } catch (ErrnoException e7) {
                    e = e7;
                    sharedMemory3 = sharedMemory5;
                    sharedMemory5 = create;
                    DSLog.et(TAG, "Failed to update index, read reply memory error, errMsg: %s", e.getMessage());
                    sharedMemory2 = sharedMemory3;
                    SharedMemoryHelper.releaseMemory(sharedMemory5);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (BufferOverflowException e8) {
                    e2 = e8;
                    sharedMemory4 = sharedMemory5;
                    sharedMemory5 = create;
                    DSLog.et(TAG, "Failed to update index, the data is out of memory, errMsg: %s", e2.getMessage());
                    sharedMemory2 = sharedMemory4;
                    SharedMemoryHelper.releaseMemory(sharedMemory5);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (RemoteException e9) {
                    e3 = e9;
                    currentTimeMillis = sharedMemory5;
                    sharedMemory5 = create;
                    DSLog.et(TAG, "Failed to update index, remote error, errMsg: %s", e3.getMessage());
                    sharedMemory2 = currentTimeMillis;
                    SharedMemoryHelper.releaseMemory(sharedMemory5);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (Throwable th3) {
                }
            } catch (ErrnoException e10) {
                e = e10;
                sharedMemory3 = null;
                DSLog.et(TAG, "Failed to update index, read reply memory error, errMsg: %s", e.getMessage());
                sharedMemory2 = sharedMemory3;
                SharedMemoryHelper.releaseMemory(sharedMemory5);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (BufferOverflowException e11) {
                e2 = e11;
                sharedMemory4 = null;
                DSLog.et(TAG, "Failed to update index, the data is out of memory, errMsg: %s", e2.getMessage());
                sharedMemory2 = sharedMemory4;
                SharedMemoryHelper.releaseMemory(sharedMemory5);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (RemoteException e12) {
                e3 = e12;
                currentTimeMillis = 0;
                DSLog.et(TAG, "Failed to update index, remote error, errMsg: %s", e3.getMessage());
                sharedMemory2 = currentTimeMillis;
                SharedMemoryHelper.releaseMemory(sharedMemory5);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (Throwable th4) {
                th = th4;
                sharedMemory = sharedMemory5;
                sharedMemory5 = currentTimeMillis;
                SharedMemoryHelper.releaseMemory(sharedMemory);
                SharedMemoryHelper.releaseMemory(sharedMemory5);
                throw th;
            }
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:8:0x0027 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r5v0, types: [long] */
    /* JADX WARN: Type inference failed for: r5v1 */
    /* JADX WARN: Type inference failed for: r5v7 */
    /* JADX WARN: Type inference failed for: r5v8 */
    /* JADX WARN: Type inference failed for: r5v11 */
    /* JADX WARN: Type inference failed for: r5v17 */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00ef, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00f0, code lost:
        r16 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00f3, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00f4, code lost:
        r16 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00fa, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00fb, code lost:
        r16 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0101, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0102, code lost:
        r16 = r15;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00ef A[ExcHandler: all (th java.lang.Throwable), PHI: r7 
      PHI: (r7v14 android.os.SharedMemory) = (r7v0 android.os.SharedMemory), (r7v0 android.os.SharedMemory), (r7v16 android.os.SharedMemory), (r7v16 android.os.SharedMemory) binds: [B:10:0x002b, B:11:?, B:17:0x0042, B:18:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:10:0x002b] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public List<IndexData> delete(String str, String str2, List<IndexData> list) {
        SharedMemory sharedMemory;
        Throwable th;
        SharedMemory sharedMemory2;
        SharedMemory sharedMemory3;
        ErrnoException e;
        SharedMemory sharedMemory4;
        BufferOverflowException e2;
        RemoteException e3;
        List<IndexData> delete;
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to delete index, error: searchService is null", new Object[0]);
            return list;
        } else if (list == null || list.isEmpty()) {
            DSLog.et(TAG, "Failed to delete index, error: indexDataList is null", new Object[0]);
            return list;
        } else {
            ?? currentTimeMillis = System.currentTimeMillis();
            SharedMemory sharedMemory5 = null;
            try {
                SharedMemory create = SharedMemory.create("SearchDataSharedMemory", SharedMemoryHelper.LARGE_THRESHOLD);
                try {
                    if (SharedMemoryHelper.writeIndexDataList(list, create) > 153600) {
                        sharedMemory5 = SharedMemory.create("SearchFailedIndexSharedMemory", SharedMemoryHelper.LARGE_THRESHOLD);
                        if (this.searchService == null) {
                            DSLog.et(TAG, "Failed to delete large index, error: searchService is null", new Object[0]);
                            SharedMemoryHelper.releaseMemory(create);
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            return list;
                        }
                        sharedMemory = create;
                        try {
                            delete = getFailedList(this.searchService.deleteLarge(str, str2, create, sharedMemory5, this.callingPkgName), sharedMemory5, list);
                            DSLog.it(TAG, "large delete " + list.size() + " index cost " + (System.currentTimeMillis() - currentTimeMillis), new Object[0]);
                        } catch (ErrnoException e4) {
                            e = e4;
                            sharedMemory3 = sharedMemory5;
                            sharedMemory5 = sharedMemory;
                            DSLog.et(TAG, "Failed to delete index, read reply memory error, errMsg: %s", e.getMessage());
                            sharedMemory2 = sharedMemory3;
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            SharedMemoryHelper.releaseMemory(sharedMemory2);
                            return list;
                        } catch (BufferOverflowException e5) {
                            e2 = e5;
                            sharedMemory4 = sharedMemory5;
                            sharedMemory5 = sharedMemory;
                            DSLog.et(TAG, "Failed to delete index, the data is out of memory, errMsg: %s", e2.getMessage());
                            sharedMemory2 = sharedMemory4;
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            SharedMemoryHelper.releaseMemory(sharedMemory2);
                            return list;
                        } catch (RemoteException e6) {
                            e3 = e6;
                            currentTimeMillis = sharedMemory5;
                            sharedMemory5 = sharedMemory;
                            DSLog.et(TAG, "Failed to delete index, remote error, errMsg: %s", e3.getMessage());
                            sharedMemory2 = currentTimeMillis;
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            SharedMemoryHelper.releaseMemory(sharedMemory2);
                            return list;
                        } catch (Throwable th2) {
                            th = th2;
                            SharedMemoryHelper.releaseMemory(sharedMemory);
                            SharedMemoryHelper.releaseMemory(sharedMemory5);
                            throw th;
                        }
                    } else {
                        sharedMemory = create;
                        if (this.searchService == null) {
                            DSLog.et(TAG, "Failed to delete index originally, error: searchService is null", new Object[0]);
                            SharedMemoryHelper.releaseMemory(sharedMemory);
                            SharedMemoryHelper.releaseMemory(null);
                            return list;
                        }
                        delete = this.searchService.delete(str, str2, list, this.callingPkgName);
                        DSLog.it(TAG, "delete " + list.size() + " index cost " + (System.currentTimeMillis() - currentTimeMillis), new Object[0]);
                    }
                    SharedMemoryHelper.releaseMemory(sharedMemory);
                    SharedMemoryHelper.releaseMemory(sharedMemory5);
                    return delete;
                } catch (ErrnoException e7) {
                    e = e7;
                    sharedMemory3 = sharedMemory5;
                    sharedMemory5 = create;
                    DSLog.et(TAG, "Failed to delete index, read reply memory error, errMsg: %s", e.getMessage());
                    sharedMemory2 = sharedMemory3;
                    SharedMemoryHelper.releaseMemory(sharedMemory5);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (BufferOverflowException e8) {
                    e2 = e8;
                    sharedMemory4 = sharedMemory5;
                    sharedMemory5 = create;
                    DSLog.et(TAG, "Failed to delete index, the data is out of memory, errMsg: %s", e2.getMessage());
                    sharedMemory2 = sharedMemory4;
                    SharedMemoryHelper.releaseMemory(sharedMemory5);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (RemoteException e9) {
                    e3 = e9;
                    currentTimeMillis = sharedMemory5;
                    sharedMemory5 = create;
                    DSLog.et(TAG, "Failed to delete index, remote error, errMsg: %s", e3.getMessage());
                    sharedMemory2 = currentTimeMillis;
                    SharedMemoryHelper.releaseMemory(sharedMemory5);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (Throwable th3) {
                }
            } catch (ErrnoException e10) {
                e = e10;
                sharedMemory3 = null;
                DSLog.et(TAG, "Failed to delete index, read reply memory error, errMsg: %s", e.getMessage());
                sharedMemory2 = sharedMemory3;
                SharedMemoryHelper.releaseMemory(sharedMemory5);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (BufferOverflowException e11) {
                e2 = e11;
                sharedMemory4 = null;
                DSLog.et(TAG, "Failed to delete index, the data is out of memory, errMsg: %s", e2.getMessage());
                sharedMemory2 = sharedMemory4;
                SharedMemoryHelper.releaseMemory(sharedMemory5);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (RemoteException e12) {
                e3 = e12;
                currentTimeMillis = 0;
                DSLog.et(TAG, "Failed to delete index, remote error, errMsg: %s", e3.getMessage());
                sharedMemory2 = currentTimeMillis;
                SharedMemoryHelper.releaseMemory(sharedMemory5);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (Throwable th4) {
                th = th4;
                sharedMemory = sharedMemory5;
                sharedMemory5 = currentTimeMillis;
                SharedMemoryHelper.releaseMemory(sharedMemory);
                SharedMemoryHelper.releaseMemory(sharedMemory5);
                throw th;
            }
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public List<String> deleteByTerm(String str, String str2, String str3, List<String> list) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to delete index, error: searchService is null", new Object[0]);
            return list;
        }
        try {
            return this.searchService.deleteByTerm(str, str2, str3, list, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to delete index, errMsg: %s", e.getMessage());
            return list;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public int deleteByQuery(String str, String str2, String str3) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to delete index, error: searchService is null", new Object[0]);
            return 0;
        }
        try {
            return this.searchService.deleteByQuery(str, str2, str3, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to delete index, errMsg: %s", e.getMessage());
            return 0;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public int clearIndex(String str, String str2, Map<String, List<String>> map) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to clear index, error: searchService is null", new Object[0]);
            return 0;
        }
        try {
            return this.searchService.clearIndex(str, str2, map, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to clear index, errMsg: %s", e.getMessage());
            return 0;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public SearchSession beginSearch(String str, String str2) {
        ISearchSession iSearchSession;
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to begin search, error: searchService is null", new Object[0]);
            return null;
        }
        try {
            iSearchSession = this.searchService.beginSearch(str, str2, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to begin search, errMsg: %s", e.getMessage());
            iSearchSession = null;
        }
        if (iSearchSession != null) {
            return new SearchSession(iSearchSession);
        }
        DSLog.et(TAG, "search session proxy is null", new Object[0]);
        return null;
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public void endSearch(String str, String str2, SearchSession searchSession) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to end search, error: searchService is null", new Object[0]);
        } else if (searchSession == null) {
            DSLog.et(TAG, "null SearchSession instance cannot endSearch", new Object[0]);
        } else {
            try {
                this.searchService.endSearch(str, str2, searchSession.getSearchSessionProxy(), this.callingPkgName);
            } catch (RemoteException e) {
                DSLog.et(TAG, "Failed to end search, errMsg: %s", e.getMessage());
            }
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public void registerIndexChangeListener(String str, String str2, IIndexChangeListener iIndexChangeListener) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to registerIndexChangeListener, error: searchService is null", new Object[0]);
            return;
        }
        try {
            this.searchService.registerIndexChangeListener(str, str2, new IndexChangeCallback(iIndexChangeListener), this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to registerIndexChangeListener, errMsg: %s", e.getMessage());
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public void unRegisterIndexChangeListener(String str, String str2, IIndexChangeListener iIndexChangeListener) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to unRegisterIndexChangeListener, error: searchService is null", new Object[0]);
            return;
        }
        try {
            this.searchService.unRegisterIndexChangeListener(str, str2, new IndexChangeCallback(iIndexChangeListener), this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to unRegisterIndexChangeListener, errMsg: %s", e.getMessage());
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public List<String> addFileObserveDirectories(String str, List<String> list) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to addFileObserveDirectories, error: searchService is null", new Object[0]);
            return list;
        }
        try {
            return this.searchService.addFileObserveDirectories(str, list, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to addFileObserveDirectories, errMsg: %s", e.getMessage());
            return list;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public List<String> deleteFileObserveDirectories(String str, List<String> list) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to deleteFileObserveDirectories, error: searchService is null", new Object[0]);
            return list;
        }
        try {
            return this.searchService.deleteFileObserveDirectories(str, list, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to deleteFileObserveDirectories, errMsg: %s", e.getMessage());
            return list;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public SearchResult setSearchableEntity(SearchableEntity searchableEntity) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to setSearchableEntity, error: searchService is null", new Object[0]);
            return SearchResult.SERVICE_NOT_CONNECT;
        }
        try {
            return SearchResult.getSearchResult(this.searchService.setSearchableEntity(searchableEntity, this.callingPkgName));
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to setSearchableEntity, errMsg: %s", e.getMessage());
            return SearchResult.IPC_EXCEPTION;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public SearchableEntity getSearchableEntity(String str) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to getSearchableEntity, error: searchService is null", new Object[0]);
            return null;
        }
        try {
            return this.searchService.getSearchableEntity(str, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to getSearchableEntity, errMsg: %s", e.getMessage());
            return null;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public List<SearchableEntity> getSearchableEntityList() {
        List<SearchableEntity> emptyList = Collections.emptyList();
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to getSearchableEntityList, error: searchService is null", new Object[0]);
            return emptyList;
        }
        try {
            return this.searchService.getSearchableEntityList(this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to getSearchableEntityList, errMsg: %s", e.getMessage());
            return emptyList;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public SearchResult setAccessable(String str, boolean z) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to setAccessable, error: searchService is null", new Object[0]);
            return SearchResult.SERVICE_NOT_CONNECT;
        }
        try {
            return SearchResult.getSearchResult(this.searchService.setAccessable(str, z, this.callingPkgName));
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to setAccessable, errMsg: %s", e.getMessage());
            return SearchResult.IPC_EXCEPTION;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public SearchResult getAccessable(String str) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to getAccessable, error: searchService is null", new Object[0]);
            return SearchResult.SERVICE_NOT_CONNECT;
        }
        try {
            return SearchResult.getSearchResult(this.searchService.getAccessable(str, this.callingPkgName));
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to getAccessable, errMsg: %s", e.getMessage());
            return SearchResult.IPC_EXCEPTION;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public boolean isIndexCompatible(String str) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to get index compatibility, error: searchService is null", new Object[0]);
            return true;
        }
        try {
            return this.searchService.isIndexCompatible(str, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to get index compatibility, errMsg: %s", e.getMessage());
            return true;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public int setIndexForm(String str, int i, List<IndexForm> list, IndexSchemaType indexSchemaType) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to setIndexFormSchema, error: searchService is null.", new Object[0]);
            return 0;
        }
        try {
            return this.searchService.setIndexFormSchema(str, i, list, indexSchemaType.getSchemaCode(), this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to setIndexFormSchema, errMsg: %s", e.getMessage());
            return 0;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public boolean registerRemoteSearchListener(DeviceInfo deviceInfo, RemoteSearchListener remoteSearchListener) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to register remote search listener, error: searchService is null", new Object[0]);
            return false;
        }
        try {
            return this.searchService.registerRemoteSearchCallback(deviceInfo, new RemoteSearchCallback(remoteSearchListener), this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to register remote search listener, errMsg: %s", e.getMessage());
            return false;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public boolean unregisterRemoteSearchListener(DeviceInfo deviceInfo) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to unregister remote search listener, error: searchService is null", new Object[0]);
            return false;
        }
        try {
            return this.searchService.unregisterRemoteSearchCallback(deviceInfo, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to unregister remote search listener, errMsg: %s", e.getMessage());
            return false;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public RemoteSearchSession beginRemoteSearch(DeviceInfo deviceInfo, String str) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to begin remote search session, error: searchService is null", new Object[0]);
            return null;
        }
        try {
            IRemoteSearchCallback beginRemoteSearch = this.searchService.beginRemoteSearch(deviceInfo, str, this.callingPkgName);
            if (beginRemoteSearch != null) {
                return new RemoteSearchSession(beginRemoteSearch);
            }
            DSLog.et(TAG, "begin remote search session failed", new Object[0]);
            return null;
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to begin remote search session, errMsg: %s", e.getMessage());
            return null;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public boolean endRemoteSearch(DeviceInfo deviceInfo, String str) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to end remote search session, error: searchService is null", new Object[0]);
            return false;
        }
        try {
            return this.searchService.endRemoteSearch(deviceInfo, str, this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to end remote search session, errMsg: %s", e.getMessage());
            return false;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public List<DeviceInfo> getOnlineDevices() {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to get online device list, error: searchService is null", new Object[0]);
            return Collections.emptyList();
        }
        try {
            return this.searchService.getOnlineDevices(this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to get online device list, errMsg: %s", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public boolean registerDeviceChangeListener(DeviceChangeListener deviceChangeListener) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to register device change listener, error: searchService is null", new Object[0]);
            return false;
        }
        try {
            return this.searchService.registerDeviceChangeCallback(this.callingPkgName, new DeviceChangeCallback(deviceChangeListener));
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to register device change listener, errMsg: %s", e.getMessage());
            return false;
        }
    }

    @Override // com.huawei.nb.searchmanager.client.ISearchClient
    public boolean unregisterDeviceChangeListener(DeviceChangeListener deviceChangeListener) {
        if (this.searchService == null) {
            DSLog.et(TAG, "Failed to unregister device change listener, error: searchService is null", new Object[0]);
            return false;
        }
        try {
            return this.searchService.unregisterDeviceChangeCallback(this.callingPkgName);
        } catch (RemoteException e) {
            DSLog.et(TAG, "Failed to unregister device change listener, errMsg: %s", e.getMessage());
            return false;
        }
    }
}
