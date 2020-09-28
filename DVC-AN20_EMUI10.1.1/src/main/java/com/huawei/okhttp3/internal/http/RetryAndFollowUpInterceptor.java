package com.huawei.okhttp3.internal.http;

import com.huawei.android.telephony.SignalStrengthEx;
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

    /* JADX WARNING: Code restructure failed: missing block: B:56:0x015b, code lost:
        r2 = r19;
     */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0167  */
    @Override // com.huawei.okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        RealInterceptorChain realChain;
        Response response;
        RealInterceptorChain realChain2;
        Response response2;
        int followUpCount;
        Request request = chain.request();
        RealInterceptorChain realChain3 = (RealInterceptorChain) chain;
        Call call = realChain3.call();
        EventListener eventListener = realChain3.eventListener();
        StreamAllocation streamAllocation2 = new StreamAllocation(this.client.connectionPool(), createAddress(request.url()), call, eventListener, this.callStackTrace, this.client.connectionAttemptDelay());
        this.streamAllocation = streamAllocation2;
        streamAllocation2.address.setHeaderHost(request.header("host"));
        int followUpCount2 = 0;
        Request request2 = request;
        Response priorResponse = null;
        while (!this.canceled) {
            boolean requestSendStarted = false;
            try {
                Response response3 = realChain3.proceed(request2, streamAllocation2, null, null);
                if (0 != 0) {
                    streamAllocation2.streamFailed(null);
                    streamAllocation2.release();
                }
                if (priorResponse != null) {
                    response = response3.newBuilder().priorResponse(priorResponse.newBuilder().body(null).build()).build();
                } else {
                    response = response3;
                }
                try {
                    Request followUp = followUpRequest(response, streamAllocation2.route());
                    if (followUp == null) {
                        streamAllocation2.release();
                        return response;
                    }
                    Util.closeQuietly(response.body());
                    int followUpCount3 = followUpCount2 + 1;
                    if (followUpCount3 > 20) {
                        streamAllocation2.release();
                        throw new ProtocolException("Too many follow-up requests: " + followUpCount3);
                    } else if (!(followUp.body() instanceof UnrepeatableRequestBody)) {
                        if (!sameConnection(response, followUp.url())) {
                            streamAllocation2.release();
                            realChain2 = realChain3;
                            followUpCount = followUpCount3;
                            response2 = response;
                            StreamAllocation streamAllocation3 = new StreamAllocation(this.client.connectionPool(), createAddress(followUp.url()), call, eventListener, this.callStackTrace, this.client.connectionAttemptDelay());
                            this.streamAllocation = streamAllocation3;
                            streamAllocation2 = streamAllocation3;
                        } else {
                            realChain2 = realChain3;
                            followUpCount = followUpCount3;
                            response2 = response;
                            if (streamAllocation2.codec() != null) {
                                throw new IllegalStateException("Closing the body of a  response didn't close its backing stream. Bad interceptor?");
                            }
                        }
                        request2 = followUp;
                        priorResponse = response2;
                        followUpCount2 = followUpCount;
                        realChain3 = realChain2;
                    } else {
                        streamAllocation2.release();
                        throw new HttpRetryException("Cannot retry streamed HTTP body", response.code());
                    }
                } catch (IOException e) {
                    streamAllocation2.release();
                    throw e;
                }
            } catch (RouteException e2) {
                realChain = realChain3;
                if (!recover(e2.getLastConnectException(), streamAllocation2, false, request2)) {
                    throw e2.getFirstConnectException();
                } else if (0 != 0) {
                    streamAllocation2.streamFailed(null);
                    streamAllocation2.release();
                }
            } catch (IOException e3) {
                realChain = realChain3;
                if (!(e3 instanceof ConnectionShutdownException)) {
                    requestSendStarted = true;
                }
                if (!recover(e3, streamAllocation2, requestSendStarted, request2)) {
                    throw e3;
                } else if (0 != 0) {
                    streamAllocation2.streamFailed(null);
                    streamAllocation2.release();
                }
            } catch (Throwable th) {
                e = th;
                if (1 != 0) {
                }
                throw e;
            }
        }
        streamAllocation2.release();
        throw new IOException("Canceled");
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
        if (e instanceof ProtocolException) {
            return false;
        }
        if (e instanceof InterruptedIOException) {
            if (!(e instanceof SocketTimeoutException) || requestSendStarted) {
                return false;
            }
            return true;
        } else if ((!(e instanceof SSLHandshakeException) || !(e.getCause() instanceof CertificateException)) && !(e instanceof SSLPeerUnverifiedException)) {
            return true;
        } else {
            return false;
        }
    }

    private Request followUpRequest(Response userResponse, Route route) throws IOException {
        String location;
        HttpUrl url;
        Proxy selectedProxy;
        if (userResponse != null) {
            int responseCode = userResponse.code();
            String method = userResponse.request().method();
            RequestBody requestBody = null;
            if (responseCode == 307 || responseCode == 308) {
                if (!method.equals("GET") && !method.equals("HEAD")) {
                    return null;
                }
            } else if (responseCode == 401) {
                return this.client.authenticator().authenticate(route, userResponse);
            } else {
                if (responseCode != 503) {
                    if (responseCode == 407) {
                        if (route != null) {
                            selectedProxy = route.proxy();
                        } else {
                            selectedProxy = this.client.proxy();
                        }
                        if (selectedProxy.type() == Proxy.Type.HTTP) {
                            return this.client.proxyAuthenticator().authenticate(route, userResponse);
                        }
                        throw new ProtocolException("Received HTTP_PROXY_AUTH (407) code while not using proxy");
                    } else if (responseCode != 408) {
                        switch (responseCode) {
                            case 300:
                            case MotionTypeApps.TYPE_PROXIMITY_ANSWER:
                            case MotionTypeApps.TYPE_PROXIMITY_DIAL:
                            case MotionTypeApps.TYPE_PROXIMITY_SCREEN_OFF:
                                break;
                            default:
                                return null;
                        }
                    } else if (!this.client.retryOnConnectionFailure() || (userResponse.request().body() instanceof UnrepeatableRequestBody)) {
                        return null;
                    } else {
                        if ((userResponse.priorResponse() == null || userResponse.priorResponse().code() != 408) && retryAfter(userResponse, 0) <= 0) {
                            return userResponse.request();
                        }
                        return null;
                    }
                } else if ((userResponse.priorResponse() == null || userResponse.priorResponse().code() != 503) && retryAfter(userResponse, SignalStrengthEx.INVALID) == 0) {
                    return userResponse.request();
                } else {
                    return null;
                }
            }
            if (!this.client.followRedirects() || (location = userResponse.header("Location")) == null || (url = userResponse.request().url().resolve(location)) == null) {
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
        return SignalStrengthEx.INVALID;
    }

    private boolean sameConnection(Response response, HttpUrl followUp) {
        HttpUrl url = response.request().url();
        return url.host().equals(followUp.host()) && url.port() == followUp.port() && url.scheme().equals(followUp.scheme());
    }
}
