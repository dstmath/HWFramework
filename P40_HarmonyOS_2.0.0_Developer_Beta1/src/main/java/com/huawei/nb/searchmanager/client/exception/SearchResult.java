package com.huawei.nb.searchmanager.client.exception;

import java.util.HashMap;
import java.util.Map;

public enum SearchResult {
    SUCCESS(0, "SUCCESS"),
    SERVICE_NOT_CONNECT(1, "SERVICE_NOT_CONNECT"),
    ILLEGAL_ARGUMENT(2, "ILLEGAL_ARGUMENT"),
    PERMISSION_DENIED(3, "PERMISSION_DENIED"),
    PERMISSION_GRANTED(4, "PERMISSION_GRANTED"),
    IPC_EXCEPTION(5, "IPC_EXCEPTION"),
    DB_EXCEPTION(6, "DB_EXCEPTION"),
    FILE_RW_EXCEPTION(7, "FILE_RW_EXCEPTION"),
    UNKNOWN_ERROR(100, "UNKNOWN_ERROR"),
    FAIL(8, "FAIL"),
    INNER_ERROR(9, "INNER_ERROR"),
    OUT_OF_MEMORY(10, "OUT_OF_MEMORY");
    
    private static final Map<Integer, SearchResult> CODE_TO_SEARCH_RESULT = new HashMap();
    private int retCode;
    private String retMsg;

    static {
        SearchResult[] values = values();
        for (SearchResult searchResult : values) {
            CODE_TO_SEARCH_RESULT.put(Integer.valueOf(searchResult.retCode), searchResult);
        }
    }

    private SearchResult(int i, String str) {
        this.retCode = i;
        this.retMsg = str;
    }

    public int getRetCode() {
        return this.retCode;
    }

    public String getRetMsg() {
        return this.retMsg;
    }

    public static SearchResult getSearchResult(int i) {
        SearchResult searchResult = CODE_TO_SEARCH_RESULT.get(Integer.valueOf(i));
        return searchResult == null ? UNKNOWN_ERROR : searchResult;
    }
}
