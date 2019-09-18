package com.huawei.okhttp3.internal.http;

import com.huawei.motiondetection.MotionTypeApps;
import com.huawei.okhttp3.Address;
import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.CertificatePinner;
import com.huawei.okhttp3.EventListener;
import com.huawei.okhttp3.HttpUrl;
import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.RequestBody;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.Route;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.RouteException;
import com.huawei.okhttp3.internal.connection.StreamAllocation;
import com.huawei.okhttp3.internal.http2.ConnectionShutdownException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpRetryException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;

public final class RetryAndFollowUpInterceptor implements Interceptor {
    private static final int MAX_FOLLOW_UPS = 20;
    private Object callStackTrace;
    private volatile boolean canceled;
    private final OkHttpClient client;
    private final boolean forWebSocket;
    private volatile StreamAllocation streamAllocation;

    public RetryAndFollowUpInterceptor(OkHttpClient client2, boolean forWebSocket2) {
        this.client = client2;
        this.forWebSocket = forWebSocket2;
    }

    public void cancel() {
        this.canceled = true;
        StreamAllocation streamAllocation2 = this.streamAllocation;
        if (streamAllocation2 != null) {
            streamAllocation2.cancel();
        }
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public void setCallStackTrace(Object callStackTrace2) {
        this.callStackTrace = callStackTrace2;
    }

    public StreamAllocation streamAllocation() {
        return this.streamAllocation;
    }

    /* JADX WARNING: type inference failed for: r12v1 */
    /* JADX WARNING: type inference failed for: r12v2, types: [com.huawei.okhttp3.internal.connection.RealConnection, com.huawei.okhttp3.internal.http.HttpCodec, java.io.IOException, com.huawei.okhttp3.ResponseBody] */
    /* JADX WARNING: type inference failed for: r12v3 */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x016e  */
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Response priorResponse;
        Response response;
        int followUpCount;
        Request request = chain.request();
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Call call = realChain.call();
        EventListener eventListener = realChain.eventListener();
        StreamAllocation streamAllocation2 = new StreamAllocation(this.client.connectionPool(), createAddress(request.url()), call, eventListener, this.callStackTrace, this.client.connectionAttemptDelay());
        this.streamAllocation = streamAllocation2;
        streamAllocation2.address.setHeaderHost(request.header("host"));
        int followUpCount2 = 0;
        ? r12 = 0;
        Request request2 = request;
        StreamAllocation streamAllocation3 = streamAllocation2;
        Response priorResponse2 = null;
        while (true) {
            Response priorResponse3 = priorResponse2;
            if (!this.canceled) {
                boolean requestSendStarted = true;
                try {
                    Response response2 = realChain.proceed(request2, streamAllocation3, r12, r12);
                    if (0 != 0) {
                        streamAllocation3.streamFailed(r12);
                        streamAllocation3.release();
                    }
                    if (priorResponse3 != null) {
                        response2 = response2.newBuilder().priorResponse(priorResponse3.newBuilder().body(r12).build()).build();
                    }
                    Response response3 = response2;
                    try {
                        Request followUp = followUpRequest(response3, streamAllocation3.route());
                        if (followUp == null) {
                            if (!this.forWebSocket) {
                                streamAllocation3.release();
                            }
                            return response3;
                        }
                        Util.closeQuietly((Closeable) response3.body());
                        int followUpCount3 = followUpCount2 + 1;
                        if (followUpCount3 > 20) {
                            Response response4 = response3;
                            Response response5 = priorResponse3;
                            streamAllocation3.release();
                            throw new ProtocolException("Too many follow-up requests: " + followUpCount3);
                        } else if (!(followUp.body() instanceof UnrepeatableRequestBody)) {
                            if (!sameConnection(response3, followUp.url())) {
                                streamAllocation3.release();
                                followUpCount = followUpCount3;
                                response = response3;
                                Response response6 = priorResponse3;
                                StreamAllocation streamAllocation4 = new StreamAllocation(this.client.connectionPool(), createAddress(followUp.url()), call, eventListener, this.callStackTrace, this.client.connectionAttemptDelay());
                                this.streamAllocation = streamAllocation4;
                                streamAllocation3 = streamAllocation4;
                            } else {
                                followUpCount = followUpCount3;
                                response = response3;
                                Response response7 = priorResponse3;
                                if (streamAllocation3.codec() != null) {
                                    throw new IllegalStateException("Closing the body of a  response didn't close its backing stream. Bad interceptor?");
                                }
                            }
                            request2 = followUp;
                            priorResponse2 = response;
                            followUpCount2 = followUpCount;
                            r12 = 0;
                        } else {
                            Response response8 = priorResponse3;
                            streamAllocation3.release();
                            throw new HttpRetryException("Cannot retry streamed HTTP body", response3.code());
                        }
                    } catch (IOException e) {
                        Response response9 = response3;
                        Response response10 = priorResponse3;
                        streamAllocation3.release();
                        throw e;
                    }
                } catch (RouteException e2) {
                    priorResponse = priorResponse3;
                    RouteException routeException = e2;
                    if (recover(e2.getLastConnectException(), streamAllocation3, false, request2)) {
                        if (0 != 0) {
                            streamAllocation3.streamFailed(null);
                            streamAllocation3.release();
                        }
                        priorResponse2 = priorResponse;
                        r12 = 0;
                    } else {
                        throw e2.getFirstConnectException();
                    }
                } catch (IOException e3) {
                    priorResponse = priorResponse3;
                    IOException iOException = e3;
                    if (e3 instanceof ConnectionShutdownException) {
                        requestSendStarted = false;
                    }
                    if (recover(e3, streamAllocation3, requestSendStarted, request2)) {
                        if (0 != 0) {
                            streamAllocation3.streamFailed(null);
                            streamAllocation3.release();
                        }
                        priorResponse2 = priorResponse;
                        r12 = 0;
                    } else {
                        throw e3;
                    }
                } catch (Throwable th) {
                    e = th;
                    if (1 != 0) {
                    }
                    throw e;
                }
            } else {
                streamAllocation3.release();
                throw new IOException("Canceled");
            }
        }
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
        Address address = new Address(url.host(), url.port(), this.client.dns(), this.client.socketFactory(), sslSocketFactory, hostnameVerifier, certificatePinner, this.client.proxyAuthenticator(), this.client.proxy(), this.client.protocols(), this.client.connectionSpecs(), this.client.proxySelector());
        return address;
    }

    private boolean recover(IOException e, StreamAllocation streamAllocation2, boolean requestSendStarted, Request userRequest) {
        streamAllocation2.streamFailed(e);
        if (!this.client.retryOnConnectionFailure()) {
            return false;
        }
        if ((!requestSendStarted || !(userRequest.body() instanceof UnrepeatableRequestBody)) && isRecoverable(e, requestSendStarted) && streamAllocation2.hasMoreRoutes()) {
            return true;
        }
        return false;
    }

    private boolean isRecoverable(IOException e, boolean requestSendStarted) {
        boolean z = false;
        if (e instanceof ProtocolException) {
            return false;
        }
        if (e instanceof InterruptedIOException) {
            if ((e instanceof SocketTimeoutException) && !requestSendStarted) {
                z = true;
            }
            return z;
        } else if ((!(e instanceof SSLHandshakeException) || !(e.getCause() instanceof CertificateException)) && !(e instanceof SSLPeerUnverifiedException)) {
            return true;
        } else {
            return false;
        }
    }

    private Request followUpRequest(Response userResponse, Route route) throws IOException {
        Proxy selectedProxy;
        if (userResponse != null) {
            int responseCode = userResponse.code();
            String method = userResponse.request().method();
            RequestBody requestBody = null;
            switch (responseCode) {
                case 300:
                case MotionTypeApps.TYPE_PROXIMITY_ANSWER:
                case MotionTypeApps.TYPE_PROXIMITY_DIAL:
                case MotionTypeApps.TYPE_PROXIMITY_SCREEN_OFF:
                    break;
                case StatusLine.HTTP_TEMP_REDIRECT:
                case StatusLine.HTTP_PERM_REDIRECT:
                    if (!method.equals("GET") && !method.equals("HEAD")) {
                        return null;
                    }
                case MotionTypeApps.TYPE_SHAKE_REFRESH:
                    return this.client.authenticator().authenticate(route, userResponse);
                case 407:
                    if (route != null) {
                        selectedProxy = route.proxy();
                    } else {
                        selectedProxy = this.client.proxy();
                    }
                    if (selectedProxy.type() == Proxy.Type.HTTP) {
                        return this.client.proxyAuthenticator().authenticate(route, userResponse);
                    }
                    throw new ProtocolException("Received HTTP_PROXY_AUTH (407) code while not using proxy");
                case 408:
                    if (!this.client.retryOnConnectionFailure() || (userResponse.request().body() instanceof UnrepeatableRequestBody)) {
                        return null;
                    }
                    if ((userResponse.priorResponse() == null || userResponse.priorResponse().code() != 408) && retryAfter(userResponse, 0) <= 0) {
                        return userResponse.request();
                    }
                    return null;
                case 503:
                    if ((userResponse.priorResponse() == null || userResponse.priorResponse().code() != 503) && retryAfter(userResponse, Integer.MAX_VALUE) == 0) {
                        return userResponse.request();
                    }
                    return null;
                default:
                    return null;
            }
            if (!this.client.followRedirects()) {
                return null;
            }
            String location = userResponse.header("Location");
            if (location == null) {
                return null;
            }
            HttpUrl url = userResponse.request().url().resolve(location);
            if (url == null) {
                return null;
            }
            if (!url.scheme().equals(userResponse.request().url().scheme()) && !this.client.followSslRedirects()) {
                return null;
            }
            Request.Builder requestBuilder = userResponse.request().newBuilder();
            if (HttpMethod.permitsRequestBody(method)) {
                boolean maintainBody = HttpMethod.redirectsWithBody(method);
                if (HttpMethod.redirectsToGet(method)) {
                    requestBuilder.method("GET", null);
                } else {
                    if (maintainBody) {
                        requestBody = userResponse.request().body();
                    }
                    requestBuilder.method(method, requestBody);
                }
                if (!maintainBody) {
                    requestBuilder.removeHeader("Transfer-Encoding");
                    requestBuilder.removeHeader("Content-Length");
                    requestBuilder.removeHeader("Content-Type");
                }
            }
            if (!sameConnection(userResponse, url)) {
                requestBuilder.removeHeader("Authorization");
            }
            return requestBuilder.url(url).build();
        }
        throw new IllegalStateException();
    }

    private int retryAfter(Response userResponse, int defaultDelay) {
        String header = userResponse.header("Retry-After");
        if (header == null) {
            return defaultDelay;
        }
        if (header.matches("\\d+")) {
            return Integer.parseInt(header);
        }
        return Integer.MAX_VALUE;
    }

    private boolean sameConnection(Response response, HttpUrl followUp) {
        HttpUrl url = response.request().url();
        return url.host().equals(followUp.host()) && url.port() == followUp.port() && url.scheme().equals(followUp.scheme());
    }
}
