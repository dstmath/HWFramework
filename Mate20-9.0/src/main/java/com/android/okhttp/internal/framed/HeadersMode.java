package com.android.okhttp.internal.framed;

public enum HeadersMode {
    SPDY_SYN_STREAM,
    SPDY_REPLY,
    SPDY_HEADERS,
    HTTP_20_HEADERS;

    public boolean failIfStreamAbsent() {
        return this == SPDY_REPLY || this == SPDY_HEADERS;
    }

    public boolean failIfStreamPresent() {
        return this == SPDY_SYN_STREAM;
    }

    public boolean failIfHeadersAbsent() {
        return this == SPDY_HEADERS;
    }

    public boolean failIfHeadersPresent() {
        return this == SPDY_REPLY;
    }
}
