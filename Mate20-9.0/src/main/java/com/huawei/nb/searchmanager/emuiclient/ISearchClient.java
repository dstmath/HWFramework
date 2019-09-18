package com.huawei.nb.searchmanager.emuiclient;

import com.huawei.nb.searchmanager.emuiclient.query.bulkcursor.BulkCursorDescriptorEx;
import java.util.List;

public interface ISearchClient {
    void clearUserIndexSearchData(String str, int i);

    void executeDBCrawl(String str, List<String> list, int i);

    BulkCursorDescriptorEx executeSearch(String str, String str2, List<String> list);
}
