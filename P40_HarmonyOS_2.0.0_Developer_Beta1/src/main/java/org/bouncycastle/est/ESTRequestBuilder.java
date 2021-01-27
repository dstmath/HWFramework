package org.bouncycastle.est;

import java.net.URL;
import org.bouncycastle.est.HttpUtil;
import org.bouncycastle.util.Arrays;

public class ESTRequestBuilder {
    ESTClient client;
    private byte[] data;
    private HttpUtil.Headers headers;
    ESTHijacker hijacker;
    ESTSourceConnectionListener listener;
    private final String method;
    private URL url;

    public ESTRequestBuilder(String str, URL url2) {
        this.method = str;
        this.url = url2;
        this.headers = new HttpUtil.Headers();
    }

    public ESTRequestBuilder(ESTRequest eSTRequest) {
        this.method = eSTRequest.method;
        this.url = eSTRequest.url;
        this.listener = eSTRequest.listener;
        this.data = eSTRequest.data;
        this.hijacker = eSTRequest.hijacker;
        this.headers = (HttpUtil.Headers) eSTRequest.headers.clone();
        this.client = eSTRequest.getClient();
    }

    public ESTRequestBuilder addHeader(String str, String str2) {
        this.headers.add(str, str2);
        return this;
    }

    public ESTRequest build() {
        return new ESTRequest(this.method, this.url, this.data, this.hijacker, this.listener, this.headers, this.client);
    }

    public ESTRequestBuilder setHeader(String str, String str2) {
        this.headers.set(str, str2);
        return this;
    }

    public ESTRequestBuilder withClient(ESTClient eSTClient) {
        this.client = eSTClient;
        return this;
    }

    public ESTRequestBuilder withConnectionListener(ESTSourceConnectionListener eSTSourceConnectionListener) {
        this.listener = eSTSourceConnectionListener;
        return this;
    }

    public ESTRequestBuilder withData(byte[] bArr) {
        this.data = Arrays.clone(bArr);
        return this;
    }

    public ESTRequestBuilder withHijacker(ESTHijacker eSTHijacker) {
        this.hijacker = eSTHijacker;
        return this;
    }

    public ESTRequestBuilder withURL(URL url2) {
        this.url = url2;
        return this;
    }
}
