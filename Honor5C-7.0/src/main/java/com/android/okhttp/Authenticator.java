package com.android.okhttp;

import java.io.IOException;
import java.net.Proxy;

public interface Authenticator {
    Request authenticate(Proxy proxy, Response response) throws IOException;

    Request authenticateProxy(Proxy proxy, Response response) throws IOException;
}
