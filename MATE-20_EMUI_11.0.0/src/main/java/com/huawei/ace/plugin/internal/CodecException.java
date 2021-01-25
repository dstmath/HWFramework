package com.huawei.ace.plugin.internal;

public class CodecException extends RuntimeException {
    private static final long serialVersionUID = 2073555933723190053L;
    public final String code;
    public final Object details;

    CodecException(String str, String str2, Object obj) {
        super(str2);
        this.code = str;
        this.details = obj;
    }
}
