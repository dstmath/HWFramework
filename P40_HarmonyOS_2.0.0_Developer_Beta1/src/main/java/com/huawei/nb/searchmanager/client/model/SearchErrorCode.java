package com.huawei.nb.searchmanager.client.model;

public interface SearchErrorCode {
    public static final int FAIL = 0;
    public static final int INDEXFORM_CONTENT_DIFF = -1;
    public static final int INDEXFORM_CONTENT_ERR = -2;
    public static final int INDEXFORM_NOT_EXISTED = -4;
    public static final int INDEXFORM_VERSION_ERR = -3;
    public static final int SUCCESS = 1;
}
