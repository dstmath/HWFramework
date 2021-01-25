package com.huawei.nb.searchmanager.client;

import android.database.Cursor;
import android.os.Bundle;
import com.huawei.nb.searchmanager.client.exception.SearchResult;
import com.huawei.nb.searchmanager.client.listener.IIndexChangeListener;
import com.huawei.nb.searchmanager.client.model.IndexData;
import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.SearchableEntity;
import java.util.List;
import java.util.Map;

public interface ISearchClient {
    List<String> addFileObserveDirectories(String str, List<String> list);

    SearchSession beginSearch(String str, String str2);

    int clearIndex(String str, String str2, Map<String, List<String>> map);

    int clearIndexForm(String str);

    void clearUserIndexSearchData(String str, int i);

    List<IndexData> delete(String str, String str2, List<IndexData> list);

    int deleteByQuery(String str, String str2, String str3);

    List<String> deleteByTerm(String str, String str2, String str3, List<String> list);

    List<String> deleteFileObserveDirectories(String str, List<String> list);

    void endSearch(String str, String str2, SearchSession searchSession);

    List<Word> executeAnalyzeText(String str, String str2);

    void executeDBCrawl(String str, List<String> list, int i);

    int executeDeleteIndex(String str, List<String> list, List<Attributes> list2);

    void executeFileCrawl(String str, String str2, boolean z, int i);

    int executeInsertIndex(String str, List<SearchIndexData> list, List<Attributes> list2);

    List<SearchIntentItem> executeIntentSearch(String str, String str2, List<String> list, String str3);

    Cursor executeSearch(String str, Bundle bundle);

    Cursor executeSearch(String str, String str2, List<String> list, List<Attributes> list2);

    int executeUpdateIndex(String str, List<SearchIndexData> list, List<Attributes> list2);

    SearchResult getAccessable(String str);

    int getApiVersionCode();

    List<IndexForm> getIndexForm(String str);

    int getIndexFormVersion(String str);

    SearchableEntity getSearchableEntity(String str);

    List<SearchableEntity> getSearchableEntityList();

    String grantFilePermission(String str, String str2, String str3, int i);

    List<IndexData> insert(String str, String str2, List<IndexData> list);

    boolean isIndexCompatible(String str);

    void registerIndexChangeListener(String str, String str2, IIndexChangeListener iIndexChangeListener);

    String revokeFilePermission(String str, String str2, String str3, int i);

    SearchResult setAccessable(String str, boolean z);

    int setIndexForm(String str, int i, List<IndexForm> list);

    void setSearchSwitch(String str, boolean z);

    SearchResult setSearchableEntity(SearchableEntity searchableEntity);

    void unRegisterIndexChangeListener(String str, String str2, IIndexChangeListener iIndexChangeListener);

    List<IndexData> update(String str, String str2, List<IndexData> list);
}
