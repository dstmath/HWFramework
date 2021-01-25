package ohos.data.search;

import android.os.Binder;
import android.os.IBinder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import ohos.app.Context;
import ohos.data.search.connect.ServiceConnectCallback;
import ohos.data.search.listener.IIndexChangeListener;
import ohos.data.search.model.IndexData;
import ohos.data.search.model.IndexForm;
import ohos.data.search.model.SearchableEntity;
import ohos.data.searchimpl.connect.IHOSPSearchServiceCall;
import ohos.data.searchimpl.connect.ISearchSession;
import ohos.data.searchimpl.connect.SearchServiceConnection;
import ohos.data.searchimpl.model.InnerSearchableEntity;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class SearchAbility {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109504, "SearchAbility");
    private final String callingPkgName;
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
                SearchAbility.this.service.registerClientDeathBinder(SearchAbility.this.clientDeathBinder, SearchAbility.this.callingPkgName);
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
        this.callingPkgName = context.getBundleName();
        HiLog.info(LABEL, "calling package name, %{public}s", new Object[]{this.callingPkgName});
        this.connection = new SearchServiceConnection((android.content.Context) context.getApplicationContext().getHostContext());
        this.clientDeathBinder = new Binder();
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

    public boolean disconnect() {
        synchronized (this.lock) {
            if (this.service != null) {
                this.service.unRegisterClientDeathBinder(this.clientDeathBinder, this.callingPkgName);
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
        return this.service.setIndexForm(str, i, ConvertUtils.indexForms2InnerIndexForms(list), this.callingPkgName);
    }

    public List<IndexForm> getIndexForm(String str) {
        if (this.service != null) {
            return ConvertUtils.innerIndexForms2IndexForms(this.service.getIndexForm(str, this.callingPkgName));
        }
        HiLog.error(LABEL, "getIndexForm failed, searchService is null", new Object[0]);
        return Collections.emptyList();
    }

    public int getIndexFormVersion(String str) {
        if (this.service != null) {
            return this.service.getIndexFormVersion(str, this.callingPkgName);
        }
        HiLog.error(LABEL, "getIndexFormVersion failed, searchService is null", new Object[0]);
        return -1;
    }

    public int clearIndexForm(String str) {
        if (this.service != null) {
            return this.service.clearIndexForm(str, this.callingPkgName);
        }
        HiLog.error(LABEL, "clearIndexForm failed, searchService is null", new Object[0]);
        return 0;
    }

    public List<IndexData> insert(String str, String str2, List<IndexData> list) {
        if (this.service == null) {
            HiLog.error(LABEL, "insert index failed, searchService is null", new Object[0]);
            return list;
        }
        return ConvertUtils.innerIndexDatas2IndexDatas(this.service.insert(str, str2, ConvertUtils.indexDatas2InnerIndexDatas(list), this.callingPkgName));
    }

    public List<IndexData> update(String str, String str2, List<IndexData> list) {
        if (this.service == null) {
            HiLog.error(LABEL, "update index failed, searchService is null", new Object[0]);
            return list;
        }
        return ConvertUtils.innerIndexDatas2IndexDatas(this.service.update(str, str2, ConvertUtils.indexDatas2InnerIndexDatas(list), this.callingPkgName));
    }

    public List<IndexData> delete(String str, String str2, List<IndexData> list) {
        if (this.service == null) {
            HiLog.error(LABEL, "delete index failed, searchService is null", new Object[0]);
            return list;
        }
        return ConvertUtils.innerIndexDatas2IndexDatas(this.service.delete(str, str2, ConvertUtils.indexDatas2InnerIndexDatas(list), this.callingPkgName));
    }

    public List<String> deleteByTerm(String str, String str2, String str3, List<String> list) {
        if (this.service != null) {
            return this.service.deleteByTerm(str, str2, str3, list, this.callingPkgName);
        }
        HiLog.error(LABEL, "delete index by term failed, searchService is null", new Object[0]);
        return list;
    }

    public int deleteByQuery(String str, String str2, String str3) {
        if (this.service != null) {
            return this.service.deleteByQuery(str, str2, str3, this.callingPkgName);
        }
        HiLog.error(LABEL, "delete index by query failed, searchService is null", new Object[0]);
        return 0;
    }

    public int clearIndex(String str, String str2, Map<String, List<String>> map) {
        if (this.service != null) {
            return this.service.clearIndex(str, str2, map, this.callingPkgName);
        }
        HiLog.error(LABEL, "clear index failed, searchService is null", new Object[0]);
        return 0;
    }

    public SearchSession beginSearch(String str, String str2) {
        if (this.service == null) {
            HiLog.error(LABEL, "beginSearch failed, searchService is null", new Object[0]);
            return null;
        }
        ISearchSession beginSearch = this.service.beginSearch(str, str2, this.callingPkgName);
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
            this.service.endSearch(str, str2, searchSession.getProxy(), this.callingPkgName);
        }
    }

    public void registerIndexChangeListener(String str, String str2, IIndexChangeListener iIndexChangeListener) {
        if (this.service == null) {
            HiLog.error(LABEL, "register index change listener failed, searchService is null", new Object[0]);
            return;
        }
        this.service.registerIndexChangeListener(str, str2, new IndexChangeCallback(iIndexChangeListener), this.callingPkgName);
    }

    public void unRegisterIndexChangeListener(String str, String str2, IIndexChangeListener iIndexChangeListener) {
        if (this.service == null) {
            HiLog.error(LABEL, "unregister index change listener failed, searchService is null", new Object[0]);
            return;
        }
        this.service.unRegisterIndexChangeListener(str, str2, new IndexChangeCallback(iIndexChangeListener), this.callingPkgName);
    }

    public SearchResult setSearchableEntity(SearchableEntity searchableEntity) {
        if (this.service == null) {
            HiLog.error(LABEL, "setSearchableEntity failed, searchService is null", new Object[0]);
            return SearchResult.SERVICE_NOT_CONNECT;
        } else if (searchableEntity == null) {
            HiLog.error(LABEL, "setSearchableEntity failed, input argument is null", new Object[0]);
            return SearchResult.ILLEGAL_ARGUMENT;
        } else {
            return SearchResult.getSearchResult(this.service.setSearchableEntity(ConvertUtils.searchableEntity2InnerSearchableEntity(searchableEntity), this.callingPkgName));
        }
    }

    public SearchableEntity getSearchableEntity(String str) {
        if (this.service == null) {
            HiLog.error(LABEL, "getSearchableEntity failed, searchService is null", new Object[0]);
            return null;
        }
        InnerSearchableEntity searchableEntity = this.service.getSearchableEntity(str, this.callingPkgName);
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
        List<InnerSearchableEntity> searchableEntityList = this.service.getSearchableEntityList(this.callingPkgName);
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
