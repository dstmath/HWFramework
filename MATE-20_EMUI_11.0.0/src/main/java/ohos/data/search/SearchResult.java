package ohos.data.search;

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
    FAIL(8, "FAIL"),
    INNER_ERROR(9, "INNER_ERROR"),
    UNKNOWN_ERROR(100, "UNKNOWN_ERROR");
    
    private static final Map<Integer, SearchResult> CODE_SEARCH_RESULT_MAPPER = new HashMap();
    private int retCode;
    private String retMsg;

    static {
        SearchResult[] values = values();
        for (SearchResult searchResult : values) {
            CODE_SEARCH_RESULT_MAPPER.put(Integer.valueOf(searchResult.retCode), searchResult);
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
        SearchResult searchResult = CODE_SEARCH_RESULT_MAPPER.get(Integer.valueOf(i));
        return searchResult == null ? UNKNOWN_ERROR : searchResult;
    }
}
