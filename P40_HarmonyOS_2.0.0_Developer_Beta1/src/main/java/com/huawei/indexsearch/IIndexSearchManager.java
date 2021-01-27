package com.huawei.indexsearch;

import android.database.Cursor;

public interface IIndexSearchManager {
    void connect();

    void destroy();

    boolean hasConnected();

    boolean isSupportHwGlobalSearch();

    Cursor search(String str, String str2, String str3);
}
