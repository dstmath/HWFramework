package android_maps_conflict_avoidance.com.google.common.io.android;

import android.content.ContentResolver;
import android.os.Build;
import android.os.SystemClock;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.EntityEnclosingRequestWrapper;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

public class GoogleHttpClient implements HttpClient {
    private final AndroidHttpClient mClient;
    private final ContentResolver mResolver;
    private final String mUserAgent;

    public GoogleHttpClient(ContentResolver resolver, String appAndVersion, boolean gzipCapable) {
        String userAgent = appAndVersion + " (" + Build.DEVICE + " " + Build.ID + ")";
        if (gzipCapable) {
            userAgent = userAgent + "; gzip";
        }
        this.mClient = AndroidHttpClient.newInstance(userAgent);
        this.mResolver = resolver;
        this.mUserAgent = userAgent;
    }

    public void close() {
        this.mClient.close();
    }

    public HttpResponse executeWithoutRewriting(HttpUriRequest request, HttpContext context) throws IOException {
        String code = "Error";
        long start = SystemClock.elapsedRealtime();
        try {
            HttpResponse response = this.mClient.execute(request, context);
            code = Integer.toString(response.getStatusLine().getStatusCode());
            return response;
        } catch (IOException e) {
            code = "IOException";
            throw e;
        }
    }

    public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException {
        String original = request.getURI().toString();
        try {
            URI uri = new URI(original);
            RequestWrapper wrapper = wrapRequest(request);
            wrapper.setURI(uri);
            RequestWrapper request2 = wrapper;
            return executeWithoutRewriting(wrapper, context);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Bad URL from: " + original, e);
        }
    }

    private static RequestWrapper wrapRequest(HttpUriRequest request) throws IOException {
        try {
            RequestWrapper wrapped;
            if (request instanceof HttpEntityEnclosingRequest) {
                wrapped = new EntityEnclosingRequestWrapper((HttpEntityEnclosingRequest) request);
            } else {
                wrapped = new RequestWrapper(request);
            }
            wrapped.resetHeaders();
            return wrapped;
        } catch (ProtocolException e) {
            throw new ClientProtocolException(e);
        }
    }

    public HttpParams getParams() {
        return this.mClient.getParams();
    }

    public ClientConnectionManager getConnectionManager() {
        return this.mClient.getConnectionManager();
    }

    public HttpResponse execute(HttpUriRequest request) throws IOException {
        return execute(request, (HttpContext) null);
    }

    public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException {
        return this.mClient.execute(target, request);
    }

    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
        return this.mClient.execute(target, request, context);
    }

    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return this.mClient.execute(request, (ResponseHandler) responseHandler);
    }

    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        return this.mClient.execute(request, (ResponseHandler) responseHandler, context);
    }

    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return this.mClient.execute(target, request, (ResponseHandler) responseHandler);
    }

    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        return this.mClient.execute(target, request, responseHandler, context);
    }
}
