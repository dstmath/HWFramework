package com.huawei.okhttp3;

import java.io.IOException;

public interface Authenticator {
    public static final Authenticator NONE = new Authenticator() {
        public Request authenticate(Route route, Response response) {
            return null;
        }
    };

    Request authenticate(Route route, Response response) throws IOException;
}
