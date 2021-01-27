package com.huawei.okhttp3.internal.http2;

import com.huawei.okhttp3.internal.Util;
import com.huawei.okio.ByteString;

@Deprecated
public final class Header {
    public static final ByteString PSEUDO_PREFIX = ByteString.encodeUtf8(":");
    public static final ByteString RESPONSE_STATUS = ByteString.encodeUtf8(RESPONSE_STATUS_UTF8);
    public static final String RESPONSE_STATUS_UTF8 = ":status";
    public static final ByteString TARGET_AUTHORITY = ByteString.encodeUtf8(TARGET_AUTHORITY_UTF8);
    public static final String TARGET_AUTHORITY_UTF8 = ":authority";
    public static final ByteString TARGET_METHOD = ByteString.encodeUtf8(TARGET_METHOD_UTF8);
    public static final String TARGET_METHOD_UTF8 = ":method";
    public static final ByteString TARGET_PATH = ByteString.encodeUtf8(TARGET_PATH_UTF8);
    public static final String TARGET_PATH_UTF8 = ":path";
    public static final ByteString TARGET_SCHEME = ByteString.encodeUtf8(TARGET_SCHEME_UTF8);
    public static final String TARGET_SCHEME_UTF8 = ":scheme";
    final int hpackSize;
    public final ByteString name;
    public final ByteString value;

    public Header(String name2, String value2) {
        this(ByteString.encodeUtf8(name2), ByteString.encodeUtf8(value2));
    }

    public Header(ByteString name2, String value2) {
        this(name2, ByteString.encodeUtf8(value2));
    }

    public Header(ByteString name2, ByteString value2) {
        this.name = name2;
        this.value = value2;
        this.hpackSize = name2.size() + 32 + value2.size();
    }

    public boolean equals(Object other) {
        if (!(other instanceof Header)) {
            return false;
        }
        Header that = (Header) other;
        if (!this.name.equals(that.name) || !this.value.equals(that.value)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (((17 * 31) + this.name.hashCode()) * 31) + this.value.hashCode();
    }

    public String toString() {
        return Util.format("%s: %s", this.name.utf8(), this.value.utf8());
    }
}
