package com.huawei.nb.searchmanager.client;

import android.database.Cursor;
import android.os.Bundle;
import com.huawei.nb.searchmanager.client.exception.SearchResult;
import com.huawei.nb.searchmanager.client.listener.IIndexChangeListener;
import com.huawei.nb.searchmanager.client.model.IndexData;
import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.SearchableEntity;
import com.huawei.nb.searchmanager.client.schema.IndexSchemaType;
import com.huawei.nb.searchmanager.distribute.DeviceChangeListener;
import com.huawei.nb.searchmanager.distribute.DeviceInfo;
import com.huawei.nb.searchmanager.distribute.RemoteSearchListener;
import com.huawei.nb.searchmanager.distribute.RemoteSearchSession;
import java.util.List;
import java.util.Map;

public interface ISearchClient {
    List<String> addFileObserveDirectories(String str, List<String> list);

    RemoteSearchSession beginRemoteSearch(DeviceInfo deviceInfo, String str);

    SearchSession beginSearch(String str, String str2);

    int clearIndex(String str, String str2, Map<String, List<String>> map);

    int clearIndexForm(String str);

    @Deprecated
    void clearUserIndexSearchData(String str, int i);

    List<IndexData> delete(String str, String str2, List<IndexData> list);

    int deleteByQuery(String str, String str2, String str3);

    List<String> deleteByTerm(String str, String str2, String str3, List<String> list);

    List<String> deleteFileObserveDirectories(String str, List<String> list);

    boolean endRemoteSearch(DeviceInfo deviceInfo, String str);

    void endSearch(String str, String str2, SearchSession searchSession);

    @Deprecated
    List<Word> executeAnalyzeText(String str, String str2);

    @Deprecated
    void executeDBCrawl(String str, List<String> list, int i);

    @Deprecated
    int executeDeleteIndex(String str, List<String> list, List<Attributes> list2);

    @Deprecated
    void executeFileCrawl(String str, String str2, boolean z, int i);

    @Deprecated
    int executeInsertIndex(String str, List<SearchIndexData> list, List<Attributes> list2);

    List<SearchIntentItem> executeIntentSearch(String str, String str2, List<String> list, String str3);

    @Deprecated
    Cursor executeSearch(String str, Bundle bundle);

    @Deprecated
    Cursor executeSearch(String str, String str2, List<String> list, List<Attributes> list2);

    @Deprecated
    int executeUpdateIndex(String str, List<SearchIndexData> list, List<Attributes> list2);

    SearchResult getAccessable(String str);

    @Deprecated
    int getApiVersionCode();

    List<IndexForm> getIndexForm(String str);

    int getIndexFormVersion(String str);

    List<DeviceInfo> getOnlineDevices();

    SearchableEntity getSearchableEntity(String str);

    List<SearchableEntity> getSearchableEntityList();

    String grantFilePermission(String str, String str2, String str3, int i);

    List<IndexData> insert(String str, String str2, List<IndexData> list);

    boolean isIndexCompatible(String str);

    boolean registerDeviceChangeListener(DeviceChangeListener deviceChangeListener);

    void registerIndexChangeListener(String str, String str2, IIndexChangeListener iIndexChangeListener);

    boolean registerRemoteSearchListener(DeviceInfo deviceInfo, RemoteSearchListener remoteSearchListener);

    String revokeFilePermission(String str, String str2, String str3, int i);

    SearchResult setAccessable(String str, boolean z);

    int setIndexForm(String str, int i, List<IndexForm> list);

    int setIndexForm(String str, int i, List<IndexForm> list, IndexSchemaType indexSchemaType);

    void setSearchSwitch(String str, boolean z);

    SearchResult setSearchableEntity(SearchableEntity searchableEntity);

    void unRegisterIndexChangeListener(String str, String str2, IIndexChangeListener iIndexChangeListener);

    boolean unregisterDeviceChangeListener(DeviceChangeListener deviceChangeListener);

    boolean unregisterRemoteSearchListener(DeviceInfo deviceInfo);

    List<IndexData> update(String str, String str2, List<IndexData> list);
}
