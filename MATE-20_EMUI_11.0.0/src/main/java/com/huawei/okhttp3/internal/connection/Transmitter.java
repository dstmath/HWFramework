package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.Address;
import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.CertificatePinner;
import com.huawei.okhttp3.Connection;
import com.huawei.okhttp3.EventListener;
import com.huawei.okhttp3.HttpUrl;
import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.internal.Internal;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.platform.Platform;
import com.huawei.okio.AsyncTimeout;
import com.huawei.okio.Timeout;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public final class Transmitter {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Call call;
    @Nullable
    private Object callStackTrace;
    private boolean canceled;
    private final OkHttpClient client;
    public RealConnection connection;
    private final RealConnectionPool connectionPool;
    private final EventListener eventListener;
    @Nullable
    private Exchange exchange;
    private ExchangeFinder exchangeFinder;
    private boolean exchangeRequestDone;
    private boolean exchangeResponseDone;
    private boolean noMoreExchanges;
    private Request request;
    private final AsyncTimeout timeout = new AsyncTimeout() {
        /* class com.huawei.okhttp3.internal.connection.Transmitter.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // com.huawei.okio.AsyncTimeout
        public void timedOut() {
            Transmitter.this.cancel();
        }
    };
    private boolean timeoutEarlyExit;

    public Transmitter(OkHttpClient client2, Call call2) {
        this.client = client2;
        this.connectionPool = Internal.instance.realConnectionPool(client2.connectionPool());
        this.call = call2;
        this.eventListener = client2.eventListenerFactory().create(call2);
        this.timeout.timeout((long) client2.callTimeoutMillis(), TimeUnit.MILLISECONDS);
    }

    public Timeout timeout() {
        return this.timeout;
    }

    public void timeoutEnter() {
        this.timeout.enter();
    }

    public void timeoutEarlyExit() {
        if (!this.timeoutEarlyExit) {
            this.timeoutEarlyExit = true;
            this.timeout.exit();
            return;
        }
        throw new IllegalStateException();
    }

    @Nullable
    private IOException timeoutExit(@Nullable IOException cause) {
        if (this.timeoutEarlyExit || !this.timeout.exit()) {
            return cause;
        }
        InterruptedIOException e = new InterruptedIOException("timeout");
        if (cause != null) {
            e.initCause(cause);
        }
        return e;
    }

    public void callStart() {
        this.callStackTrace = Platform.get().getStackTraceForCloseable("response.body().close()");
        this.eventListener.callStart(this.call);
    }

    public void prepareToConnect(Request request2) {
        Request request3 = this.request;
        if (request3 != null) {
            if (Util.sameConnection(request3.url(), request2.url()) && this.exchangeFinder.hasRouteToTry()) {
                return;
            }
            if (this.exchange != null) {
                throw new IllegalStateException();
            } else if (this.exchangeFinder != null) {
                maybeReleaseConnection(null, true);
                this.exchangeFinder = null;
            }
        }
        this.request = request2;
        this.exchangeFinder = new ExchangeFinder(this, this.connectionPool, createAddress(request2.url()), this.call, this.eventListener);
        this.exchangeFinder.setAddressHeaderField(request2.header("host"));
    }

    private Address createAddress(HttpUrl url) {
        SSLSocketFactory sslSocketFactory = null;
        HostnameVerifier hostnameVerifier = null;
        CertificatePinner certificatePinner = null;
        if (url.isHttps()) {
            sslSocketFactory = this.client.sslSocketFactory();
            hostnameVerifier = this.client.hostnameVerifier();
            certificatePinner = this.client.certificatePinner();
        }
        return new Address(url.host(), url.port(), this.client.dns(), this.client.socketFactory(), sslSocketFactory, hostnameVerifier, certificatePinner, this.client.proxyAuthenticator(), this.client.proxy(), this.client.protocols(), this.client.connectionSpecs(), this.client.proxySelector());
    }

    /* access modifiers changed from: package-private */
    public Exchange newExchange(Interceptor.Chain chain, boolean doExtensiveHealthChecks) {
        synchronized (this.connectionPool) {
            if (this.noMoreExchanges) {
                throw new IllegalStateException("released");
            } else if (this.exchange != null) {
                throw new IllegalStateException("cannot make a new request because the previous response is still open: please call response.close()");
            }
        }
        Exchange result = new Exchange(this, this.call, this.eventListener, this.exchangeFinder, this.exchangeFinder.find(this.client, chain, doExtensiveHealthChecks));
        synchronized (this.connectionPool) {
            this.exchange = result;
            this.exchangeRequestDone = false;
            this.exchangeResponseDone = false;
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public void acquireConnectionNoEvents(RealConnection connection2) {
        if (this.connection == null) {
            this.connection = connection2;
            connection2.transmitters.add(new TransmitterReference(this, this.callStackTrace));
            return;
        }
        throw new IllegalStateException();
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public Socket releaseConnectionNoEvents() {
        int index = -1;
        int i = 0;
        int size = this.connection.transmitters.size();
        while (true) {
            if (i >= size) {
                break;
            } else if (((Reference) this.connection.transmitters.get(i)).get() == this) {
                index = i;
                break;
            } else {
                i++;
            }
        }
        if (index != -1) {
            RealConnection released = this.connection;
            released.transmitters.remove(index);
            this.connection = null;
            if (released.transmitters.isEmpty()) {
                released.idleAtNanos = System.nanoTime();
                if (this.connectionPool.connectionBecameIdle(released)) {
                    return released.socket();
                }
            }
            return null;
        }
        throw new IllegalStateException();
    }

    public void exchangeDoneDueToException() {
        synchronized (this.connectionPool) {
            if (!this.noMoreExchanges) {
                this.exchange = null;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public IOException exchangeMessageDone(Exchange exchange2, boolean requestDone, boolean responseDone, @Nullable IOException e) {
        boolean exchangeDone = false;
        synchronized (this.connectionPool) {
            if (exchange2 != this.exchange) {
                return e;
            }
            boolean changed = false;
            if (requestDone) {
                if (!this.exchangeRequestDone) {
                    changed = true;
                }
                this.exchangeRequestDone = true;
            }
            if (responseDone) {
                if (!this.exchangeResponseDone) {
                    changed = true;
                }
                this.exchangeResponseDone = true;
            }
            if (this.exchangeRequestDone && this.exchangeResponseDone && changed) {
                exchangeDone = true;
                this.exchange.connection().successCount++;
                this.exchange = null;
            }
        }
        if (exchangeDone) {
            return maybeReleaseConnection(e, false);
        }
        return e;
    }

    @Nullable
    public IOException noMoreExchanges(@Nullable IOException e) {
        synchronized (this.connectionPool) {
            this.noMoreExchanges = true;
        }
        return maybeReleaseConnection(e, false);
    }

    @Nullable
    private IOException maybeReleaseConnection(@Nullable IOException e, boolean force) {
        Connection releasedConnection;
        Socket socket;
        boolean callFailed;
        boolean callEnd;
        synchronized (this.connectionPool) {
            if (force) {
                if (this.exchange != null) {
                    throw new IllegalStateException("cannot release connection while it is in use");
                }
            }
            releasedConnection = this.connection;
            if (this.connection == null || this.exchange != null || (!force && !this.noMoreExchanges)) {
                socket = null;
            } else {
                socket = releaseConnectionNoEvents();
            }
            if (this.connection != null) {
                releasedConnection = null;
            }
            callFailed = true;
            callEnd = this.noMoreExchanges && this.exchange == null;
        }
        Util.closeQuietly(socket);
        if (releasedConnection != null) {
            this.eventListener.connectionReleased(this.call, releasedConnection);
        }
        if (callEnd) {
            if (e == null) {
                callFailed = false;
            }
            e = timeoutExit(e);
            if (callFailed) {
                this.eventListener.callFailed(this.call, e);
            } else {
                this.eventListener.callEnd(this.call);
            }
        }
        return e;
    }

    public boolean canRetry() {
        return this.exchangeFinder.hasStreamFailure() && this.exchangeFinder.hasRouteToTry();
    }

    public boolean hasExchange() {
        boolean z;
        synchronized (this.connectionPool) {
            z = this.exchange != null;
        }
        return z;
    }

    public void cancel() {
        Exchange exchangeToCancel;
        RealConnection connectionToCancel;
        synchronized (this.connectionPool) {
            this.canceled = true;
            exchangeToCancel = this.exchange;
            if (this.exchangeFinder == null || this.exchangeFinder.connectingConnection() == null) {
                connectionToCancel = this.connection;
            } else {
                connectionToCancel = this.exchangeFinder.connectingConnection();
            }
        }
        if (exchangeToCancel != null) {
            exchangeToCancel.cancel();
        } else if (connectionToCancel != null) {
            connectionToCancel.cancel();
        }
    }

    public boolean isCanceled() {
        boolean z;
        synchronized (this.connectionPool) {
            z = this.canceled;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public static final class TransmitterReference extends WeakReference<Transmitter> {
        final Object callStackTrace;

        TransmitterReference(Transmitter referent, Object callStackTrace2) {
            super(referent);
            this.callStackTrace = callStackTrace2;
        }
    }
}
