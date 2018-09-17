package com.huawei.indexsearch;

public class IndexSearchParserEx {
    public static synchronized void createIndexSearchParser(String pkgName, String[] tables) {
        synchronized (IndexSearchParserEx.class) {
            IndexSearchParser.createIndexSearchParser(pkgName, tables);
        }
    }
}
