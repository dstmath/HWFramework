package com.android.okhttp.internal.http;

import java.io.IOException;

public final class RequestException extends Exception {
    public RequestException(IOException cause) {
        super(cause);
    }

    public IOException getCause() {
        return (IOException) super.getCause();
    }
}
