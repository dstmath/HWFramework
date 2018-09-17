package com.android.okhttp.internal.framed;

import com.android.okhttp.okio.ByteString;

public final class Header {
    public static final ByteString RESPONSE_STATUS = ByteString.encodeUtf8(":status");
    public static final ByteString TARGET_AUTHORITY = ByteString.encodeUtf8(":authority");
    public static final ByteString TARGET_HOST = ByteString.encodeUtf8(":host");
    public static final ByteString TARGET_METHOD = ByteString.encodeUtf8(":method");
    public static final ByteString TARGET_PATH = ByteString.encodeUtf8(":path");
    public static final ByteString TARGET_SCHEME = ByteString.encodeUtf8(":scheme");
    public static final ByteString VERSION = ByteString.encodeUtf8(":version");
    final int hpackSize;
    public final ByteString name;
    public final ByteString value;

    public Header(String name, String value) {
        this(ByteString.encodeUtf8(name), ByteString.encodeUtf8(value));
    }

    public Header(ByteString name, String value) {
        this(name, ByteString.encodeUtf8(value));
    }

    public Header(ByteString name, ByteString value) {
        this.name = name;
        this.value = value;
        this.hpackSize = (name.size() + 32) + value.size();
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof Header)) {
            return false;
        }
        Header that = (Header) other;
        if (this.name.equals(that.name)) {
            z = this.value.equals(that.value);
        }
        return z;
    }

    public int hashCode() {
        return ((this.name.hashCode() + 527) * 31) + this.value.hashCode();
    }

    public String toString() {
        return String.format("%s: %s", new Object[]{this.name.utf8(), this.value.utf8()});
    }
}
