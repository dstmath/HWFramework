package com.huawei.nb.searchmanager.client;

import com.huawei.nb.query.bulkcursor.BulkCursorDescriptor;
import java.util.List;

public interface ISearchClient {
    void clearUserIndexSearchData(String str, int i);

    List<Word> executeAnalyzeText(String str, String str2);

    void executeDBCrawl(String str, List<String> list, int i);

    int executeDeleteIndex(String str, List<String> list, List<Attributes> list2);

    void executeFileCrawl(String str, String str2, boolean z, int i);

    int executeInsertIndex(String str, List<SearchIndexData> list, List<Attributes> list2);

    List<SearchIntentItem> executeIntentSearch(String str, String str2, List<String> list, String str3);

    BulkCursorDescriptor executeSearch(String str, String str2, List<String> list, List<Attributes> list2);

    int executeUpdateIndex(String str, List<SearchIndexData> list, List<Attributes> list2);

    void setSearchSwitch(String str, boolean z);
}
