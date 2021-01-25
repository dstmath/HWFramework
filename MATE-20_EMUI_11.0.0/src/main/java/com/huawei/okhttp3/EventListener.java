package com.huawei.okhttp3;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import javax.annotation.Nullable;

public abstract class EventListener {
    public static final EventListener NONE = new EventListener() {
        /* class com.huawei.okhttp3.EventListener.AnonymousClass1 */
    };

    public interface Factory {
        EventListener create(Call call);
    }

    static Factory factory(EventListener listener) {
        return new Factory() {
            /* class com.huawei.okhttp3.$$Lambda$EventListener$DM9bB_Jev8UEYf_h3u_9iShrcmI */

            @Override // com.huawei.okhttp3.EventListener.Factory
            public final EventListener create(Call call) {
                return EventListener.lambda$factory$0(EventListener.this, call);
            }
        };
    }

    static /* synthetic */ EventListener lambda$factory$0(EventListener listener, Call call) {
        return listener;
    }

    public void callStart(Call call) {
    }

    public void dnsStart(Call call, String domainName) {
    }

    public void dnsEnd(Call call, String domainName, List<InetAddress> list) {
    }

    public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
    }

    public void secureConnectStart(Call call) {
    }

    public void secureConnectEnd(Call call, @Nullable Handshake handshake) {
    }

    public void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, @Nullable Protocol protocol) {
    }

    public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, @Nullable Protocol protocol, IOException ioe) {
    }

    public void connectionAcquired(Call call, Connection connection) {
    }

    public void connectionReleased(Call call, Connection connection) {
    }

    public void requestHeadersStart(Call call) {
    }

    public void requestHeadersEnd(Call call, Request request) {
    }

    public void requestBodyStart(Call call) {
    }

    public void requestBodyEnd(Call call, long byteCount) {
    }

    public void requestFailed(Call call, IOException ioe) {
    }

    public void responseHeadersStart(Call call) {
    }

    public void responseHeadersEnd(Call call, Response response) {
    }

    public void responseBodyStart(Call call) {
    }

    public void responseBodyEnd(Call call, long byteCount) {
    }

    public void responseFailed(Call call, IOException ioe) {
    }

    public void callEnd(Call call) {
    }

    public void callFailed(Call call, IOException ioe) {
    }
}
