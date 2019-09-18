package com.huawei.okhttp3;

import java.io.IOException;
import javax.annotation.Nullable;

public interface Authenticator {
    public static final Authenticator NONE = new Authenticator() {
        public Request authenticate(Route route, Response response) {
            return null;
        }
    };

    @Nullable
    Request authenticate(Route route, Response response) throws IOException;
}
