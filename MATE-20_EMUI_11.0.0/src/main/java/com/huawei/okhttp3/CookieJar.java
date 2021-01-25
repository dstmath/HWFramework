package com.huawei.okhttp3;

import java.util.Collections;
import java.util.List;

public interface CookieJar {
    public static final CookieJar NO_COOKIES = new CookieJar() {
        /* class com.huawei.okhttp3.CookieJar.AnonymousClass1 */

        @Override // com.huawei.okhttp3.CookieJar
        public void saveFromResponse(HttpUrl url, List<Cookie> list) {
        }

        @Override // com.huawei.okhttp3.CookieJar
        public List<Cookie> loadForRequest(HttpUrl url) {
            return Collections.emptyList();
        }
    };

    List<Cookie> loadForRequest(HttpUrl httpUrl);

    void saveFromResponse(HttpUrl httpUrl, List<Cookie> list);
}
