package com.huawei.okhttp3;

import com.huawei.okhttp3.ConnectionPool;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.Version;
import java.util.concurrent.TimeUnit;

@Deprecated
public class OkHttpClientBase {
    static final int CONNECTION_ATTEMPT_DELAY_DEFAULT = 200;
    static final int CONNECTION_ATTEMPT_DELAY_MAX = 2000;
    static final int CONNECTION_ATTEMPT_DELAY_MIN = 100;
    int connectionAttemptDelayTime;
    ConnectionPool connectionPool;
    ConnectionPoolListener connectionPoolListener = new ConnectionPoolListener(this, null);
    AbsDispatcher dispatcher;

    public OkHttpClientBase() {
    }

    OkHttpClientBase(BuilderBase builder) {
        if (builder != null) {
            this.connectionPool = builder.connectionPool;
            this.connectionAttemptDelayTime = builder.connectionAttemptDelayTime;
        }
        this.connectionPool.delegate.addHttp2Listener(this.connectionPoolListener);
    }

    public void addHttp2Host(String hostName, int port, String scheme) {
        if (hostName != null && scheme != null) {
            this.dispatcher.addHttp2Host(hostName, port, scheme);
        }
    }

    public int connectionAttemptDelay() {
        return this.connectionAttemptDelayTime;
    }

    public int http2ConnectionCount(String hostName, int port, String scheme) {
        if (hostName == null || scheme == null) {
            return 0;
        }
        return this.connectionPool.delegate.http2ConnectionCount(hostName, port, scheme);
    }

    public boolean keepHttp2ConnectionAlive(String hostName, int port, String scheme) {
        if (hostName == null || scheme == null) {
            return false;
        }
        return this.connectionPool.delegate.keepHttp2ConnectionAlive(hostName, port, scheme);
    }

    public static class BuilderBase<T extends OkHttpClient.Builder> implements iDispatcherFactory {
        int connectTimeout;
        int connectionAttemptDelayTime = 200;
        ConnectionPool connectionPool = new ConnectionPool();

        /* access modifiers changed from: protected */
        public void checkConnectionAttempt(int connectTimeout2) {
            if (this.connectionAttemptDelayTime >= connectTimeout2) {
                throw new IllegalArgumentException("Connection Attempt Delay (" + this.connectionAttemptDelayTime + " ms) is greater than or equal to Connect Timeout (" + connectTimeout2 + " ms)");
            }
        }

        public T connectionAttemptDelay(long interval, TimeUnit unit) {
            int tempTime = Util.checkDuration("connectionAttemptDelay", interval, unit);
            if (tempTime < 100 || tempTime > 2000) {
                throw new IllegalArgumentException("Connection Attempt Delay " + tempTime + "ms is out of range (100ms ~ 2000ms).");
            }
            int origDelayTime = this.connectionAttemptDelayTime;
            this.connectionAttemptDelayTime = tempTime;
            try {
                checkConnectionAttempt(this.connectTimeout);
                return (T) ((OkHttpClient.Builder) this);
            } catch (IllegalArgumentException e) {
                this.connectionAttemptDelayTime = origDelayTime;
                throw e;
            }
        }

        @Override // com.huawei.okhttp3.iDispatcherFactory
        public AbsDispatcher createDispatcher(Protocol httpProtocol) {
            int i = AnonymousClass1.$SwitchMap$okhttp3$Protocol[httpProtocol.ordinal()];
            if (i == 1) {
                return new Http2Dispatcher();
            }
            if (i == 2 || i == 3 || i == 4) {
                return new Dispatcher();
            }
            throw new IllegalArgumentException("there is no dispatcher fit for the protocol " + httpProtocol.toString());
        }
    }

    /* renamed from: com.huawei.okhttp3.OkHttpClientBase$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$okhttp3$Protocol = new int[Protocol.values().length];

        static {
            try {
                $SwitchMap$okhttp3$Protocol[Protocol.HTTP_2.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$okhttp3$Protocol[Protocol.HTTP_1_0.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$okhttp3$Protocol[Protocol.HTTP_1_1.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$okhttp3$Protocol[Protocol.SPDY_3.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private class ConnectionPoolListener implements ConnectionPool.Http2ConnectionEventListener {
        private ConnectionPoolListener() {
        }

        /* synthetic */ ConnectionPoolListener(OkHttpClientBase x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.huawei.okhttp3.ConnectionPool.Http2ConnectionEventListener
        public void onEvicted(String hostName, int port, String scheme) {
            if (hostName != null && scheme != null) {
                OkHttpClientBase.this.dispatcher.removeHttp2Host(hostName, port, scheme);
            }
        }
    }

    public static String getVersion() {
        return Version.userAgent();
    }
}
