package com.huawei.security.dpermission.model;

import ohos.global.icu.impl.PatternTokenizer;

public class ResultWrapper<T> {
    private int code;
    private T data;
    private String message;

    private ResultWrapper() {
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public T getData() {
        return this.data;
    }

    public static <T> ResultWrapper<T> wrap(int i, String str, T t) {
        if (t != null) {
            ResultWrapper<T> resultWrapper = new ResultWrapper<>();
            ((ResultWrapper) resultWrapper).code = i;
            ((ResultWrapper) resultWrapper).message = str;
            ((ResultWrapper) resultWrapper).data = t;
            return resultWrapper;
        }
        throw new IllegalArgumentException("data cannot be null");
    }

    public String toString() {
        return "ResultWrapper{errorCode=" + this.code + ", message='" + this.message + PatternTokenizer.SINGLE_QUOTE + ", data=******}";
    }
}
