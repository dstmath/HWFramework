package com.huawei.indexsearch;

import java.util.List;

public interface IIndexSearchParser {
    boolean isValidTable(String str);

    void notifyIndexSearchService(long j, int i);

    void notifyIndexSearchService(List<Long> list, int i);
}
