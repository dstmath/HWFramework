package com.huawei.okhttp3;

import java.io.IOException;
import javax.annotation.Nullable;

public interface Authenticator {
    public static final Authenticator NONE = new Authenticator() {
        /* class com.huawei.okhttp3.Authenticator.AnonymousClass1 */

        @Override // com.huawei.okhttp3.Authenticator
        public Request authenticate(@Nullable Route route, Response response) {
            return null;
        }
    };

    @Nullable
    Request authenticate(@Nullable Route route, Response response) throws IOException;
}
