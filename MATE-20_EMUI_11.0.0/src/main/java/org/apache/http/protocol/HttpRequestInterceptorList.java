package org.apache.http.protocol;

import java.util.List;
import org.apache.http.HttpRequestInterceptor;

@Deprecated
public interface HttpRequestInterceptorList {
    void addRequestInterceptor(HttpRequestInterceptor httpRequestInterceptor);

    void addRequestInterceptor(HttpRequestInterceptor httpRequestInterceptor, int i);

    void clearRequestInterceptors();

    HttpRequestInterceptor getRequestInterceptor(int i);

    int getRequestInterceptorCount();

    void removeRequestInterceptorByClass(Class cls);

    @Override // org.apache.http.protocol.HttpResponseInterceptorList
    void setInterceptors(List list);
}
