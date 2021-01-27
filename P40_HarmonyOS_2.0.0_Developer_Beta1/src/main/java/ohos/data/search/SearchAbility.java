package ohos.data.search;

import android.os.Binder;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SharedMemory;
import android.system.ErrnoException;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import ohos.app.Context;
import ohos.data.orm.OrmConfig;
import ohos.data.search.connect.ServiceConnectCallback;
import ohos.data.search.listener.IIndexChangeListener;
import ohos.data.search.model.IndexData;
import ohos.data.search.model.IndexForm;
import ohos.data.search.model.SearchableEntity;
import ohos.data.searchimpl.connect.IHOSPSearchServiceCall;
import ohos.data.searchimpl.connect.ISearchSession;
import ohos.data.searchimpl.connect.SearchServiceConnection;
import ohos.data.searchimpl.model.InnerIndexData;
import ohos.data.searchimpl.model.InnerSearchableEntity;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class SearchAbility {
    private static final String DATA_MEMORY_NAME = "SearchDataSharedMemory";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109504, "SearchAbility");
    private static final String REPL_MEMORY_NAME = "SearchFailedIndexSharedMemory";
    private static final String SEARCH_SERVICE_NAME = "fusion_search";
    private final String callingBundleName;
    private IBinder clientDeathBinder;
    private volatile ServiceConnectCallback connectCallback = null;
    private final SearchServiceConnection connection;
    private SearchServiceConnection.OnConnectListener listener = new SearchServiceConnection.OnConnectListener() {
        /* class ohos.data.search.SearchAbility.AnonymousClass1 */

        @Override // ohos.data.searchimpl.connect.SearchServiceConnection.OnConnectListener
        public void onConnect(IBinder iBinder) {
            if (iBinder != null) {
                synchronized (SearchAbility.this.lock) {
                    SearchAbility.this.service = new IHOSPSearchServiceCall.Proxy(iBinder);
                }
                SearchAbility.this.service.registerClientDeathBinder(SearchAbility.this.clientDeathBinder, SearchAbility.this.callingBundleName);
                HiLog.info(SearchAbility.LABEL, "succeed connect to search service", new Object[0]);
                SearchAbility.this.invokeConnectCallback(true);
            }
        }

        @Override // ohos.data.searchimpl.connect.SearchServiceConnection.OnConnectListener
        public void onDisconnect() {
            synchronized (SearchAbility.this.lock) {
                SearchAbility.this.service = null;
            }
            HiLog.error(SearchAbility.LABEL, "connection to search service is broken down", new Object[0]);
            SearchAbility.this.invokeConnectCallback(false);
        }
    };
    private final Object lock = new Object();
    private volatile IHOSPSearchServiceCall service = null;

    public SearchAbility(Context context) {
        this.callingBundleName = context.getBundleName();
        HiLog.info(LABEL, "calling bundle name, %{public}s", new Object[]{this.callingBundleName});
        this.connection = new SearchServiceConnection((android.content.Context) context.getApplicationContext().getHostContext());
        this.clientDeathBinder = new Binder();
        getSearchService();
    }

    private void getSearchService() {
        IBinder service2 = ServiceManager.getService(SEARCH_SERVICE_NAME);
        if (service2 != null) {
            synchronized (this.lock) {
                this.service = new IHOSPSearchServiceCall.Proxy(service2);
            }
            this.service.registerClientDeathBinder(this.clientDeathBinder, this.callingBundleName);
            HiLog.info(LABEL, "succeed connect to search service", new Object[0]);
        }
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

    @Deprecated
    public boolean connect(ServiceConnectCallback serviceConnectCallback) {
        synchronized (this.lock) {
            this.connectCallback = serviceConnectCallback;
            if (this.service != null) {
                invokeConnectCallback(true);
                return true;
            }
            return this.connection.open(this.listener);
        }
    }

    @Deprecated
    public boolean disconnect() {
        synchronized (this.lock) {
            if (this.service != null) {
                this.service.unRegisterClientDeathBinder(this.clientDeathBinder, this.callingBundleName);
                this.connection.close();
                this.service = null;
                HiLog.info(LABEL, "succeed close search service connection", new Object[0]);
                invokeConnectCallback(false);
            } else {
                HiLog.info(LABEL, "search service connection has already been closed", new Object[0]);
            }
        }
        return true;
    }

    public boolean hasConnected() {
        return this.service != null;
    }

    public int setIndexForm(String str, int i, List<IndexForm> list) {
        if (this.service == null) {
            HiLog.error(LABEL, "setIndexForm failed, searchService is null", new Object[0]);
            return 0;
        }
        return this.service.setIndexForm(str, i, ConvertUtils.indexForms2InnerIndexForms(list), this.callingBundleName);
    }

    public List<IndexForm> getIndexForm(String str) {
        if (this.service != null) {
            return ConvertUtils.innerIndexForms2IndexForms(this.service.getIndexForm(str, this.callingBundleName));
        }
        HiLog.error(LABEL, "getIndexForm failed, searchService is null", new Object[0]);
        return Collections.emptyList();
    }

    public int getIndexFormVersion(String str) {
        if (this.service != null) {
            return this.service.getIndexFormVersion(str, this.callingBundleName);
        }
        HiLog.error(LABEL, "getIndexFormVersion failed, searchService is null", new Object[0]);
        return -1;
    }

    public int clearIndexForm(String str) {
        if (this.service != null) {
            return this.service.clearIndexForm(str, this.callingBundleName);
        }
        HiLog.error(LABEL, "clearIndexForm failed, searchService is null", new Object[0]);
        return 0;
    }

    private List<IndexData> getFailedList(int i, SharedMemory sharedMemory, List<IndexData> list) throws ErrnoException {
        if (i == SearchResult.SUCCESS.getRetCode()) {
            return Collections.emptyList();
        }
        return i == SearchResult.FAIL.getRetCode() ? SharedMemoryHelper.readIndexDataList(sharedMemory) : list;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:8:0x001f */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r12v0, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r12v2 */
    /* JADX WARN: Type inference failed for: r12v6 */
    /* JADX WARN: Type inference failed for: r12v7 */
    /* JADX WARN: Type inference failed for: r12v9 */
    public List<IndexData> insert(String str, String str2, List<IndexData> list) {
        Throwable th;
        SharedMemory sharedMemory;
        SharedMemory sharedMemory2;
        SharedMemory sharedMemory3;
        ErrnoException e;
        BufferOverflowException e2;
        List<IndexData> innerIndexDatas2IndexDatas;
        if (this.service == null) {
            HiLog.error(LABEL, "insert index failed, searchService is null", new Object[0]);
            return list;
        } else if (list == null || list.isEmpty()) {
            HiLog.error(LABEL, "Failed to insert index, error: indexDataList is null", new Object[0]);
            return list;
        } else {
            SharedMemory sharedMemory4 = null;
            try {
                sharedMemory = SharedMemory.create(DATA_MEMORY_NAME, OrmConfig.MAX_ENCRYPT_KEY_SIZE);
                try {
                    if (SharedMemoryHelper.writeIndexDataList(list, sharedMemory) > 153600) {
                        sharedMemory4 = SharedMemory.create(REPL_MEMORY_NAME, OrmConfig.MAX_ENCRYPT_KEY_SIZE);
                        if (this.service == null) {
                            HiLog.error(LABEL, "Failed to insert large index, error: searchService is null", new Object[0]);
                            SharedMemoryHelper.releaseMemory(sharedMemory);
                            SharedMemoryHelper.releaseMemory(sharedMemory4);
                            return list;
                        }
                        innerIndexDatas2IndexDatas = getFailedList(this.service.insertLarge(str, str2, sharedMemory, sharedMemory4, this.callingBundleName), sharedMemory4, list);
                    } else {
                        List<InnerIndexData> indexDatas2InnerIndexDatas = ConvertUtils.indexDatas2InnerIndexDatas(list);
                        if (this.service == null) {
                            HiLog.error(LABEL, "Failed to insert index originally, error: searchService is null", new Object[0]);
                            SharedMemoryHelper.releaseMemory(sharedMemory);
                            SharedMemoryHelper.releaseMemory(null);
                            return list;
                        }
                        innerIndexDatas2IndexDatas = ConvertUtils.innerIndexDatas2IndexDatas(this.service.insert(str, str2, indexDatas2InnerIndexDatas, this.callingBundleName));
                    }
                    SharedMemoryHelper.releaseMemory(sharedMemory);
                    SharedMemoryHelper.releaseMemory(sharedMemory4);
                    return innerIndexDatas2IndexDatas;
                } catch (ErrnoException e3) {
                    e = e3;
                    sharedMemory3 = null;
                    sharedMemory4 = sharedMemory;
                    HiLog.error(LABEL, "Failed to insert index, read reply memory error, errMsg: %s", new Object[]{e.getMessage()});
                    sharedMemory2 = sharedMemory3;
                    SharedMemoryHelper.releaseMemory(sharedMemory4);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (BufferOverflowException e4) {
                    e2 = e4;
                    str = 0;
                    sharedMemory4 = sharedMemory;
                    HiLog.error(LABEL, "Failed to insert index, the data is out of memory, errMsg: %s", new Object[]{e2.getMessage()});
                    sharedMemory2 = str;
                    SharedMemoryHelper.releaseMemory(sharedMemory4);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (Throwable th2) {
                    th = th2;
                    SharedMemoryHelper.releaseMemory(sharedMemory);
                    SharedMemoryHelper.releaseMemory(sharedMemory4);
                    throw th;
                }
            } catch (ErrnoException e5) {
                e = e5;
                sharedMemory3 = null;
                HiLog.error(LABEL, "Failed to insert index, read reply memory error, errMsg: %s", new Object[]{e.getMessage()});
                sharedMemory2 = sharedMemory3;
                SharedMemoryHelper.releaseMemory(sharedMemory4);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (BufferOverflowException e6) {
                e2 = e6;
                str = 0;
                HiLog.error(LABEL, "Failed to insert index, the data is out of memory, errMsg: %s", new Object[]{e2.getMessage()});
                sharedMemory2 = str;
                SharedMemoryHelper.releaseMemory(sharedMemory4);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (Throwable th3) {
                th = th3;
                sharedMemory = sharedMemory4;
                sharedMemory4 = str;
                SharedMemoryHelper.releaseMemory(sharedMemory);
                SharedMemoryHelper.releaseMemory(sharedMemory4);
                throw th;
            }
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:8:0x0020 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r12v0, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r12v2 */
    /* JADX WARN: Type inference failed for: r12v6 */
    /* JADX WARN: Type inference failed for: r12v7 */
    /* JADX WARN: Type inference failed for: r12v9 */
    public List<IndexData> update(String str, String str2, List<IndexData> list) {
        Throwable th;
        SharedMemory sharedMemory;
        SharedMemory sharedMemory2;
        SharedMemory sharedMemory3;
        ErrnoException e;
        BufferOverflowException e2;
        List<IndexData> innerIndexDatas2IndexDatas;
        if (this.service == null) {
            HiLog.error(LABEL, "update index failed, searchService is null", new Object[0]);
            return list;
        } else if (list == null || list.isEmpty()) {
            HiLog.error(LABEL, "Failed to update index, error: indexDataList is null", new Object[0]);
            return list;
        } else {
            SharedMemory sharedMemory4 = null;
            try {
                sharedMemory = SharedMemory.create(DATA_MEMORY_NAME, OrmConfig.MAX_ENCRYPT_KEY_SIZE);
                try {
                    if (SharedMemoryHelper.writeIndexDataList(list, sharedMemory) > 153600) {
                        sharedMemory4 = SharedMemory.create(REPL_MEMORY_NAME, OrmConfig.MAX_ENCRYPT_KEY_SIZE);
                        if (this.service == null) {
                            HiLog.error(LABEL, "Failed to update large index, error: searchService is null", new Object[0]);
                            SharedMemoryHelper.releaseMemory(sharedMemory);
                            SharedMemoryHelper.releaseMemory(sharedMemory4);
                            return list;
                        }
                        innerIndexDatas2IndexDatas = getFailedList(this.service.updateLarge(str, str2, sharedMemory, sharedMemory4, this.callingBundleName), sharedMemory4, list);
                    } else {
                        List<InnerIndexData> indexDatas2InnerIndexDatas = ConvertUtils.indexDatas2InnerIndexDatas(list);
                        if (this.service == null) {
                            HiLog.error(LABEL, "Failed to update index originally, error: searchService is null", new Object[0]);
                            SharedMemoryHelper.releaseMemory(sharedMemory);
                            SharedMemoryHelper.releaseMemory(null);
                            return list;
                        }
                        innerIndexDatas2IndexDatas = ConvertUtils.innerIndexDatas2IndexDatas(this.service.update(str, str2, indexDatas2InnerIndexDatas, this.callingBundleName));
                    }
                    SharedMemoryHelper.releaseMemory(sharedMemory);
                    SharedMemoryHelper.releaseMemory(sharedMemory4);
                    return innerIndexDatas2IndexDatas;
                } catch (ErrnoException e3) {
                    e = e3;
                    sharedMemory3 = null;
                    sharedMemory4 = sharedMemory;
                    HiLog.error(LABEL, "Failed to update index, read reply memory error, errMsg: %s", new Object[]{e.getMessage()});
                    sharedMemory2 = sharedMemory3;
                    SharedMemoryHelper.releaseMemory(sharedMemory4);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (BufferOverflowException e4) {
                    e2 = e4;
                    str = 0;
                    sharedMemory4 = sharedMemory;
                    HiLog.error(LABEL, "Failed to update index, the data is out of memory, errMsg: %s", new Object[]{e2.getMessage()});
                    sharedMemory2 = str;
                    SharedMemoryHelper.releaseMemory(sharedMemory4);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (Throwable th2) {
                    th = th2;
                    SharedMemoryHelper.releaseMemory(sharedMemory);
                    SharedMemoryHelper.releaseMemory(sharedMemory4);
                    throw th;
                }
            } catch (ErrnoException e5) {
                e = e5;
                sharedMemory3 = null;
                HiLog.error(LABEL, "Failed to update index, read reply memory error, errMsg: %s", new Object[]{e.getMessage()});
                sharedMemory2 = sharedMemory3;
                SharedMemoryHelper.releaseMemory(sharedMemory4);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (BufferOverflowException e6) {
                e2 = e6;
                str = 0;
                HiLog.error(LABEL, "Failed to update index, the data is out of memory, errMsg: %s", new Object[]{e2.getMessage()});
                sharedMemory2 = str;
                SharedMemoryHelper.releaseMemory(sharedMemory4);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (Throwable th3) {
                th = th3;
                sharedMemory = sharedMemory4;
                sharedMemory4 = str;
                SharedMemoryHelper.releaseMemory(sharedMemory);
                SharedMemoryHelper.releaseMemory(sharedMemory4);
                throw th;
            }
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:8:0x001f */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r12v0, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r12v2 */
    /* JADX WARN: Type inference failed for: r12v6 */
    /* JADX WARN: Type inference failed for: r12v7 */
    /* JADX WARN: Type inference failed for: r12v9 */
    public List<IndexData> delete(String str, String str2, List<IndexData> list) {
        Throwable th;
        SharedMemory sharedMemory;
        SharedMemory sharedMemory2;
        SharedMemory sharedMemory3;
        ErrnoException e;
        BufferOverflowException e2;
        List<IndexData> innerIndexDatas2IndexDatas;
        if (this.service == null) {
            HiLog.error(LABEL, "delete index failed, searchService is null", new Object[0]);
            return list;
        } else if (list == null || list.isEmpty()) {
            HiLog.error(LABEL, "Failed to delete index, error: indexDataList is null", new Object[0]);
            return list;
        } else {
            SharedMemory sharedMemory4 = null;
            try {
                sharedMemory = SharedMemory.create(DATA_MEMORY_NAME, OrmConfig.MAX_ENCRYPT_KEY_SIZE);
                try {
                    if (SharedMemoryHelper.writeIndexDataList(list, sharedMemory) > 153600) {
                        sharedMemory4 = SharedMemory.create(REPL_MEMORY_NAME, OrmConfig.MAX_ENCRYPT_KEY_SIZE);
                        if (this.service == null) {
                            HiLog.error(LABEL, "Failed to delete large index, error: searchService is null", new Object[0]);
                            SharedMemoryHelper.releaseMemory(sharedMemory);
                            SharedMemoryHelper.releaseMemory(sharedMemory4);
                            return list;
                        }
                        innerIndexDatas2IndexDatas = getFailedList(this.service.deleteLarge(str, str2, sharedMemory, sharedMemory4, this.callingBundleName), sharedMemory4, list);
                    } else {
                        List<InnerIndexData> indexDatas2InnerIndexDatas = ConvertUtils.indexDatas2InnerIndexDatas(list);
                        if (this.service == null) {
                            HiLog.error(LABEL, "Failed to delete index originally, error: searchService is null", new Object[0]);
                            SharedMemoryHelper.releaseMemory(sharedMemory);
                            SharedMemoryHelper.releaseMemory(null);
                            return list;
                        }
                        innerIndexDatas2IndexDatas = ConvertUtils.innerIndexDatas2IndexDatas(this.service.delete(str, str2, indexDatas2InnerIndexDatas, this.callingBundleName));
                    }
                    SharedMemoryHelper.releaseMemory(sharedMemory);
                    SharedMemoryHelper.releaseMemory(sharedMemory4);
                    return innerIndexDatas2IndexDatas;
                } catch (ErrnoException e3) {
                    e = e3;
                    sharedMemory3 = null;
                    sharedMemory4 = sharedMemory;
                    HiLog.error(LABEL, "Failed to delete index, read reply memory error, errMsg: %s", new Object[]{e.getMessage()});
                    sharedMemory2 = sharedMemory3;
                    SharedMemoryHelper.releaseMemory(sharedMemory4);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (BufferOverflowException e4) {
                    e2 = e4;
                    str = 0;
                    sharedMemory4 = sharedMemory;
                    HiLog.error(LABEL, "Failed to delete index, the data is out of memory, errMsg: %s", new Object[]{e2.getMessage()});
                    sharedMemory2 = str;
                    SharedMemoryHelper.releaseMemory(sharedMemory4);
                    SharedMemoryHelper.releaseMemory(sharedMemory2);
                    return list;
                } catch (Throwable th2) {
                    th = th2;
                    SharedMemoryHelper.releaseMemory(sharedMemory);
                    SharedMemoryHelper.releaseMemory(sharedMemory4);
                    throw th;
                }
            } catch (ErrnoException e5) {
                e = e5;
                sharedMemory3 = null;
                HiLog.error(LABEL, "Failed to delete index, read reply memory error, errMsg: %s", new Object[]{e.getMessage()});
                sharedMemory2 = sharedMemory3;
                SharedMemoryHelper.releaseMemory(sharedMemory4);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (BufferOverflowException e6) {
                e2 = e6;
                str = 0;
                HiLog.error(LABEL, "Failed to delete index, the data is out of memory, errMsg: %s", new Object[]{e2.getMessage()});
                sharedMemory2 = str;
                SharedMemoryHelper.releaseMemory(sharedMemory4);
                SharedMemoryHelper.releaseMemory(sharedMemory2);
                return list;
            } catch (Throwable th3) {
                th = th3;
                sharedMemory = sharedMemory4;
                sharedMemory4 = str;
                SharedMemoryHelper.releaseMemory(sharedMemory);
                SharedMemoryHelper.releaseMemory(sharedMemory4);
                throw th;
            }
        }
    }

    public List<String> deleteByTerm(String str, String str2, String str3, List<String> list) {
        if (this.service != null) {
            return this.service.deleteByTerm(str, str2, str3, list, this.callingBundleName);
        }
        HiLog.error(LABEL, "delete index by term failed, searchService is null", new Object[0]);
        return list;
    }

    public int deleteByQuery(String str, String str2, String str3) {
        if (this.service != null) {
            return this.service.deleteByQuery(str, str2, str3, this.callingBundleName);
        }
        HiLog.error(LABEL, "delete index by query failed, searchService is null", new Object[0]);
        return 0;
    }

    public int clearIndex(String str, String str2, Map<String, List<String>> map) {
        if (this.service != null) {
            return this.service.clearIndex(str, str2, map, this.callingBundleName);
        }
        HiLog.error(LABEL, "clear index failed, searchService is null", new Object[0]);
        return 0;
    }

    public SearchSession beginSearch(String str, String str2) {
        if (this.service == null) {
            HiLog.error(LABEL, "beginSearch failed, searchService is null", new Object[0]);
            return null;
        }
        ISearchSession beginSearch = this.service.beginSearch(str, str2, this.callingBundleName);
        if (beginSearch == null) {
            return null;
        }
        SearchSession searchSession = new SearchSession();
        searchSession.setProxy(beginSearch);
        return searchSession;
    }

    public void endSearch(String str, String str2, SearchSession searchSession) {
        if (this.service == null) {
            HiLog.error(LABEL, "endSearch failed, searchService is null", new Object[0]);
        } else if (searchSession == null) {
            HiLog.error(LABEL, "endSearch failed, SearchSession is null", new Object[0]);
        } else {
            this.service.endSearch(str, str2, searchSession.getProxy(), this.callingBundleName);
        }
    }

    public void registerIndexChangeListener(String str, String str2, IIndexChangeListener iIndexChangeListener) {
        if (this.service == null) {
            HiLog.error(LABEL, "register index change listener failed, searchService is null", new Object[0]);
            return;
        }
        this.service.registerIndexChangeListener(str, str2, new IndexChangeCallback(iIndexChangeListener), this.callingBundleName);
    }

    public void unRegisterIndexChangeListener(String str, String str2, IIndexChangeListener iIndexChangeListener) {
        if (this.service == null) {
            HiLog.error(LABEL, "unregister index change listener failed, searchService is null", new Object[0]);
            return;
        }
        this.service.unRegisterIndexChangeListener(str, str2, new IndexChangeCallback(iIndexChangeListener), this.callingBundleName);
    }

    public SearchResult setSearchableEntity(SearchableEntity searchableEntity) {
        if (this.service == null) {
            HiLog.error(LABEL, "setSearchableEntity failed, searchService is null", new Object[0]);
            return SearchResult.SERVICE_NOT_CONNECT;
        } else if (searchableEntity == null) {
            HiLog.error(LABEL, "setSearchableEntity failed, input argument is null", new Object[0]);
            return SearchResult.ILLEGAL_ARGUMENT;
        } else {
            return SearchResult.getSearchResult(this.service.setSearchableEntity(ConvertUtils.searchableEntity2InnerSearchableEntity(searchableEntity), this.callingBundleName));
        }
    }

    public SearchableEntity getSearchableEntity(String str) {
        if (this.service == null) {
            HiLog.error(LABEL, "getSearchableEntity failed, searchService is null", new Object[0]);
            return null;
        }
        InnerSearchableEntity searchableEntity = this.service.getSearchableEntity(str, this.callingBundleName);
        if (searchableEntity == null) {
            return null;
        }
        return ConvertUtils.innerSearchableEntity2SearchableEntity(searchableEntity);
    }

    public List<SearchableEntity> getSearchableEntityList() {
        if (this.service == null) {
            HiLog.error(LABEL, "getSearchableEntityList failed, searchService is null", new Object[0]);
            return Collections.emptyList();
        }
        List<InnerSearchableEntity> searchableEntityList = this.service.getSearchableEntityList(this.callingBundleName);
        if (searchableEntityList == null) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList(searchableEntityList.size());
        for (InnerSearchableEntity innerSearchableEntity : searchableEntityList) {
            if (innerSearchableEntity != null) {
                arrayList.add(ConvertUtils.innerSearchableEntity2SearchableEntity(innerSearchableEntity));
            }
        }
        return arrayList;
    }
}
