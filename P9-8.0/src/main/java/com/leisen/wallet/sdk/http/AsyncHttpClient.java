package com.leisen.wallet.sdk.http;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import com.leisen.wallet.sdk.util.LogUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

public class AsyncHttpClient {
    private static final int DEFAULT_MAX_CONNECTIONS = 10;
    public static final int DEFAULT_MAX_RETRIES = 5;
    public static final int DEFAULT_RETRY_SLEEP_TIME_MILLIS = 1500;
    public static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    private static final int DEFAULT_SOCKET_TIMEOUT = 30000;
    private static final String TAG = "AsyncHttpClient";
    private final Map<String, String> clientHeaderMap;
    private final DefaultHttpClient httpClient;
    private final HttpContext httpContext;
    private boolean isEncodeUrl;
    private int maxConnections;
    private final Map<Context, List<RequestHandle>> requestMap;
    private ExecutorService threadPool;
    private int timeout;
    private boolean useSynchronousMode;

    public AsyncHttpClient(boolean useSynchronousMode) {
        this(false, 80, 443);
        this.useSynchronousMode = useSynchronousMode;
    }

    public AsyncHttpClient(int httpPort) {
        this(false, httpPort, 443);
    }

    public AsyncHttpClient(int httpPort, int httpsPort) {
        this(false, httpPort, httpsPort);
    }

    public AsyncHttpClient(boolean fixNoHttpResponseException, int httpPort, int httpsPort) {
        this(getDefaultSchemeRegistry(fixNoHttpResponseException, httpPort, httpsPort));
    }

    private static SchemeRegistry getDefaultSchemeRegistry(boolean fixNoHttpResponseException, int httpPort, int httpsPort) {
        if (httpPort < 1) {
            httpPort = 80;
        }
        if (httpsPort < 1) {
            httpsPort = 443;
        }
        SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), httpPort));
        schemeRegistry.register(new Scheme("https", sslSocketFactory, httpsPort));
        return schemeRegistry;
    }

    public AsyncHttpClient(SchemeRegistry schemeRegistry) {
        this.timeout = 30000;
        this.maxConnections = 10;
        this.isEncodeUrl = false;
        this.useSynchronousMode = false;
        BasicHttpParams httpParams = new BasicHttpParams();
        ConnManagerParams.setTimeout(httpParams, (long) this.timeout);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(this.maxConnections));
        ConnManagerParams.setMaxTotalConnections(httpParams, 10);
        HttpConnectionParams.setSoTimeout(httpParams, this.timeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, this.timeout);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        ClientConnectionManager cm = createConnectionManager(schemeRegistry, httpParams);
        this.threadPool = getDefaultThreadPool();
        this.requestMap = new WeakHashMap();
        this.clientHeaderMap = new HashMap();
        this.httpContext = new SyncBasicHttpContext(new BasicHttpContext());
        this.httpClient = new DefaultHttpClient(cm, httpParams);
        this.httpClient.setHttpRequestRetryHandler(new RetryHandler(5, DEFAULT_RETRY_SLEEP_TIME_MILLIS));
    }

    protected ExecutorService getDefaultThreadPool() {
        return Executors.newCachedThreadPool();
    }

    protected ClientConnectionManager createConnectionManager(SchemeRegistry schemeRegistry, BasicHttpParams httpParams) {
        return new ThreadSafeClientConnManager(httpParams, schemeRegistry);
    }

    public RequestHandle get(Context context, String url, RequestParams params, ResponseHandlerInterface responseHandler) {
        return sendRequest(this.httpClient, this.httpContext, new HttpGet(getUrlWithQueryString(this.isEncodeUrl, url, params)), null, responseHandler, context);
    }

    public RequestHandle post(Context context, String url, RequestParams params, ResponseHandlerInterface responseHandler) {
        return post(context, url, paramsToEntity(params, responseHandler), null, responseHandler);
    }

    public RequestHandle post(Context context, String url, HttpEntity entity, String contentType, ResponseHandlerInterface responseHandler) {
        return sendRequest(this.httpClient, this.httpContext, addEntityToRequestBase(new HttpPost(URI.create(url).normalize()), entity), contentType, responseHandler, context);
    }

    private HttpEntityEnclosingRequestBase addEntityToRequestBase(HttpEntityEnclosingRequestBase requestBase, HttpEntity entity) {
        if (entity != null) {
            requestBase.setEntity(entity);
        }
        return requestBase;
    }

    private HttpEntity paramsToEntity(RequestParams params, ResponseHandlerInterface responseHandler) {
        HttpEntity entity = null;
        if (params == null) {
            return entity;
        }
        try {
            return params.getEntity(responseHandler);
        } catch (Throwable t) {
            if (responseHandler == null) {
                LogUtil.e(TAG, "==>" + t.getMessage());
                return entity;
            }
            responseHandler.sendFailureMessage(0, null, null, t);
            return entity;
        }
    }

    protected RequestHandle sendRequest(DefaultHttpClient httpClient, HttpContext httpContext, HttpUriRequest httpUriRequest, String contentType, ResponseHandlerInterface responseHandler, Context context) {
        if (httpUriRequest == null) {
            throw new IllegalArgumentException("HttpUriRequest must not be null");
        } else if (responseHandler != null) {
            if (contentType != null) {
                httpUriRequest.setHeader("Content-Type", contentType);
            }
            responseHandler.setUseSynchronousMode(this.useSynchronousMode);
            responseHandler.setRequestHeaders(httpUriRequest.getAllHeaders());
            responseHandler.setRequestURI(httpUriRequest.getURI());
            AsyncHttpRequest asyncHttpRequest = newAysncHttpRequest(httpClient, httpContext, httpUriRequest, responseHandler);
            if (this.useSynchronousMode) {
                asyncHttpRequest.run();
            } else {
                this.threadPool.submit(asyncHttpRequest);
            }
            RequestHandle requestHandle = new RequestHandle(asyncHttpRequest);
            if (context != null) {
                List<RequestHandle> requestList = (List) this.requestMap.get(context);
                if (requestList == null) {
                    requestList = new LinkedList();
                    this.requestMap.put(context, requestList);
                }
                requestList.add(requestHandle);
                Iterator<RequestHandle> iterator = requestList.iterator();
                while (iterator.hasNext()) {
                    if (((RequestHandle) iterator.next()).shouldBeGarbageCollected()) {
                        iterator.remove();
                    }
                }
            }
            return requestHandle;
        } else {
            throw new IllegalArgumentException("ResponseHandler must not be null");
        }
    }

    private AsyncHttpRequest newAysncHttpRequest(DefaultHttpClient httpClient, HttpContext httpContext, HttpUriRequest httpUriRequest, ResponseHandlerInterface responseHandler) {
        return new AsyncHttpRequest(httpClient, httpContext, httpUriRequest, responseHandler);
    }

    private String getUrlWithQueryString(boolean shouldEncodeUrl, String url, RequestParams params) {
        if (shouldEncodeUrl) {
            url = url.replace(" ", "%20");
        }
        if (params == null) {
            return url;
        }
        String paramString = params.getParamString();
        if (paramString == null || "?".equals(paramString)) {
            return url;
        }
        return new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(url)).append(!url.contains("?") ? "?" : "&").toString())).append(paramString).toString();
    }

    public static void silentCloseInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                Log.w(TAG, "Cannot close input stream", e);
            }
        }
    }

    public static void silentCloseOutputStream(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                Log.w(TAG, "Cannot close output stream", e);
            }
        }
    }

    public void cancelRequests(final Context context, final boolean mayInterruptIfRunning) {
        if (context != null) {
            Runnable r = new Runnable() {
                public void run() {
                    for (RequestHandle requestHandle : (List) AsyncHttpClient.this.requestMap.get(context)) {
                        requestHandle.cancel(mayInterruptIfRunning);
                    }
                    AsyncHttpClient.this.requestMap.remove(context);
                }
            };
            if (Looper.myLooper() != Looper.getMainLooper()) {
                r.run();
            } else {
                new Thread(r).start();
            }
        }
    }

    public void cancelAllRequests(boolean mayInterruptIfRunning) {
        for (List<RequestHandle> requestList : this.requestMap.values()) {
            if (requestList != null) {
                for (RequestHandle requestHandle : requestList) {
                    requestHandle.cancel(mayInterruptIfRunning);
                }
            }
        }
        this.requestMap.clear();
    }

    public CookieStore getCookieStore() {
        return this.httpClient.getCookieStore();
    }

    public void setCookieStore(CookieStore cookieStore) {
        this.httpContext.setAttribute("http.cookie-store", cookieStore);
    }
}
