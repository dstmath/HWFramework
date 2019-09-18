package com.leisen.wallet.sdk.newhttp;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import com.android.server.HwNetworkPropertyChecker;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;

public class AsyncHttpClientX {
    private static final int DEFAULT_MAX_CONNECTIONS = 10;
    public static final int DEFAULT_MAX_RETRIES = 5;
    public static final int DEFAULT_RETRY_SLEEP_TIME_MILLIS = 1500;
    public static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    private static final int DEFAULT_SOCKET_TIMEOUT = 30000;
    private static final String TAG = "AsyncHttpClientX";
    private Map<String, String> clientHeaderMap = new HashMap();
    private boolean isEncodeUrl = false;
    private int maxConnections = 10;
    /* access modifiers changed from: private */
    public Map<Context, List<RequestHandleX>> requestMap = new WeakHashMap();
    private ExecutorService threadPool = getDefaultThreadPool();
    private int timeout = 30000;
    private boolean useSynchronousMode = false;

    public AsyncHttpClientX(boolean useSynchronousMode2) {
        this.useSynchronousMode = useSynchronousMode2;
    }

    /* access modifiers changed from: protected */
    public ExecutorService getDefaultThreadPool() {
        return Executors.newCachedThreadPool();
    }

    public RequestHandleX post(Context context, String url, String request, ResponseHandlerInterfaceX responseHandler) {
        HttpURLConnection conn = null;
        try {
            URL mUrl = new URL(url);
            conn = HwNetworkPropertyChecker.NetworkCheckerThread.TYPE_HTTPS.equals(mUrl.getProtocol()) ? (HttpsURLConnection) mUrl.openConnection() : (HttpURLConnection) mUrl.openConnection();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return sendRequest(conn, request, responseHandler, context);
    }

    /* access modifiers changed from: protected */
    public RequestHandleX sendRequest(HttpURLConnection conn, String request, ResponseHandlerInterfaceX responseHandler, Context context) {
        if (conn == null) {
            throw new IllegalArgumentException("HttpUriRequest must not be null");
        } else if (responseHandler != null) {
            conn.setRequestProperty("Content-Type", "application/json");
            conn.addRequestProperty("Charset", "UTF-8");
            conn.setReadTimeout(this.timeout);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            try {
                conn.setRequestMethod("POST");
                responseHandler.setUseSynchronousMode(this.useSynchronousMode);
                responseHandler.setRequestURI(conn.getURL());
                AsyncHttpRequestX asyncHttpRequest = new AsyncHttpRequestX(conn, request, responseHandler);
                if (this.useSynchronousMode) {
                    asyncHttpRequest.run();
                } else {
                    this.threadPool.submit(asyncHttpRequest);
                }
                RequestHandleX requestHandle = new RequestHandleX(asyncHttpRequest);
                if (context != null) {
                    List<RequestHandleX> requestList = this.requestMap.get(context);
                    if (requestList == null) {
                        requestList = new LinkedList<>();
                        this.requestMap.put(context, requestList);
                    }
                    requestList.add(requestHandle);
                    Iterator<RequestHandleX> iterator = requestList.iterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().shouldBeGarbageCollected()) {
                            iterator.remove();
                        }
                    }
                }
                return requestHandle;
            } catch (ProtocolException e) {
                throw new IllegalArgumentException("POST not be allowed?");
            }
        } else {
            throw new IllegalArgumentException("ResponseHandler must not be null");
        }
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
                    for (RequestHandleX requestHandle : (List) AsyncHttpClientX.this.requestMap.get(context)) {
                        requestHandle.cancel(mayInterruptIfRunning);
                    }
                    AsyncHttpClientX.this.requestMap.remove(context);
                }
            };
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(r).start();
            } else {
                r.run();
            }
        }
    }

    public void cancelAllRequests(boolean mayInterruptIfRunning) {
        for (List<RequestHandleX> requestList : this.requestMap.values()) {
            if (requestList != null) {
                for (RequestHandleX requestHandle : requestList) {
                    requestHandle.cancel(mayInterruptIfRunning);
                }
            }
        }
        this.requestMap.clear();
    }
}
