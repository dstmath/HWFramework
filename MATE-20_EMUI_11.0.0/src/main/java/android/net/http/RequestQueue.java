package android.net.http;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.compatibility.WebAddress;
import android.util.Log;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import org.apache.http.HttpHost;

public class RequestQueue implements RequestFeeder {
    private static final int CONNECTION_COUNT = 4;
    private final ActivePool mActivePool;
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private final LinkedHashMap<HttpHost, LinkedList<Request>> mPending;
    private BroadcastReceiver mProxyChangeReceiver;
    private HttpHost mProxyHost;

    interface ConnectionManager {
        Connection getConnection(Context context, HttpHost httpHost);

        HttpHost getProxyHost();

        boolean recycleConnection(Connection connection);
    }

    /* access modifiers changed from: package-private */
    public class ActivePool implements ConnectionManager {
        private int mConnectionCount;
        IdleCache mIdleCache = new IdleCache();
        ConnectionThread[] mThreads;
        private int mTotalConnection;
        private int mTotalRequest;

        static /* synthetic */ int access$408(ActivePool x0) {
            int i = x0.mTotalRequest;
            x0.mTotalRequest = i + 1;
            return i;
        }

        ActivePool(int connectionCount) {
            this.mConnectionCount = connectionCount;
            this.mThreads = new ConnectionThread[this.mConnectionCount];
            for (int i = 0; i < this.mConnectionCount; i++) {
                this.mThreads[i] = new ConnectionThread(RequestQueue.this.mContext, i, this, RequestQueue.this);
            }
        }

        /* access modifiers changed from: package-private */
        public void startup() {
            for (int i = 0; i < this.mConnectionCount; i++) {
                this.mThreads[i].start();
            }
        }

        /* access modifiers changed from: package-private */
        public void shutdown() {
            for (int i = 0; i < this.mConnectionCount; i++) {
                this.mThreads[i].requestStop();
            }
        }

        /* access modifiers changed from: package-private */
        public void startConnectionThread() {
            synchronized (RequestQueue.this) {
                RequestQueue.this.notify();
            }
        }

        public void startTiming() {
            for (int i = 0; i < this.mConnectionCount; i++) {
                ConnectionThread rt = this.mThreads[i];
                rt.mCurrentThreadTime = -1;
                rt.mTotalThreadTime = 0;
            }
            this.mTotalRequest = 0;
            this.mTotalConnection = 0;
        }

        public void stopTiming() {
            int totalTime = 0;
            for (int i = 0; i < this.mConnectionCount; i++) {
                ConnectionThread rt = this.mThreads[i];
                if (rt.mCurrentThreadTime != -1) {
                    totalTime = (int) (((long) totalTime) + rt.mTotalThreadTime);
                }
                rt.mCurrentThreadTime = 0;
            }
            Log.d("Http", "Http thread used " + totalTime + " ms  for " + this.mTotalRequest + " requests and " + this.mTotalConnection + " new connections");
        }

        /* access modifiers changed from: package-private */
        public void logState() {
            StringBuilder dump = new StringBuilder();
            for (int i = 0; i < this.mConnectionCount; i++) {
                dump.append(this.mThreads[i] + "\n");
            }
            HttpLog.v(dump.toString());
        }

        @Override // android.net.http.RequestQueue.ConnectionManager
        public HttpHost getProxyHost() {
            return RequestQueue.this.mProxyHost;
        }

        /* access modifiers changed from: package-private */
        public void disablePersistence() {
            for (int i = 0; i < this.mConnectionCount; i++) {
                Connection connection = this.mThreads[i].mConnection;
                if (connection != null) {
                    connection.setCanPersist(false);
                }
            }
            this.mIdleCache.clear();
        }

        /* access modifiers changed from: package-private */
        public ConnectionThread getThread(HttpHost host) {
            synchronized (RequestQueue.this) {
                for (int i = 0; i < this.mThreads.length; i++) {
                    ConnectionThread ct = this.mThreads[i];
                    Connection connection = ct.mConnection;
                    if (connection != null && connection.mHost.equals(host)) {
                        return ct;
                    }
                }
                return null;
            }
        }

        @Override // android.net.http.RequestQueue.ConnectionManager
        public Connection getConnection(Context context, HttpHost host) {
            HttpHost host2 = RequestQueue.this.determineHost(host);
            Connection con = this.mIdleCache.getConnection(host2);
            if (con != null) {
                return con;
            }
            this.mTotalConnection++;
            return Connection.getConnection(RequestQueue.this.mContext, host2, RequestQueue.this.mProxyHost, RequestQueue.this);
        }

        @Override // android.net.http.RequestQueue.ConnectionManager
        public boolean recycleConnection(Connection connection) {
            return this.mIdleCache.cacheConnection(connection.getHost(), connection);
        }
    }

    public RequestQueue(Context context) {
        this(context, 4);
    }

    public RequestQueue(Context context, int connectionCount) {
        this.mProxyHost = null;
        this.mContext = context;
        this.mPending = new LinkedHashMap<>(32);
        this.mActivePool = new ActivePool(connectionCount);
        this.mActivePool.startup();
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
    }

    public synchronized void enablePlatformNotifications() {
        if (this.mProxyChangeReceiver == null) {
            this.mProxyChangeReceiver = new BroadcastReceiver() {
                /* class android.net.http.RequestQueue.AnonymousClass1 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context ctx, Intent intent) {
                    RequestQueue.this.setProxyConfig();
                }
            };
            this.mContext.registerReceiver(this.mProxyChangeReceiver, new IntentFilter("android.intent.action.PROXY_CHANGE"));
        }
        setProxyConfig();
    }

    public synchronized void disablePlatformNotifications() {
        if (this.mProxyChangeReceiver != null) {
            this.mContext.unregisterReceiver(this.mProxyChangeReceiver);
            this.mProxyChangeReceiver = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void setProxyConfig() {
        NetworkInfo info = this.mConnectivityManager.getActiveNetworkInfo();
        if (info == null || info.getType() != 1) {
            String host = Proxy.getHost(this.mContext);
            if (host == null) {
                this.mProxyHost = null;
            } else {
                this.mActivePool.disablePersistence();
                this.mProxyHost = new HttpHost(host, Proxy.getPort(this.mContext), HttpHost.DEFAULT_SCHEME_NAME);
            }
        } else {
            this.mProxyHost = null;
        }
    }

    public HttpHost getProxyHost() {
        return this.mProxyHost;
    }

    public RequestHandle queueRequest(String url, String method, Map<String, String> headers, EventHandler eventHandler, InputStream bodyProvider, int bodyLength) {
        return queueRequest(url, new WebAddress(url), method, headers, eventHandler, bodyProvider, bodyLength);
    }

    public RequestHandle queueRequest(String url, WebAddress uri, String method, Map<String, String> headers, EventHandler eventHandler, InputStream bodyProvider, int bodyLength) {
        EventHandler eventHandler2;
        if (eventHandler == null) {
            eventHandler2 = new LoggingEventHandler();
        } else {
            eventHandler2 = eventHandler;
        }
        Request req = new Request(method, new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()), this.mProxyHost, uri.getPath(), bodyProvider, bodyLength, eventHandler2, headers);
        queueRequest(req, false);
        ActivePool.access$408(this.mActivePool);
        this.mActivePool.startConnectionThread();
        return new RequestHandle(this, url, uri, method, headers, bodyProvider, bodyLength, req);
    }

    private static class SyncFeeder implements RequestFeeder {
        private Request mRequest;

        SyncFeeder() {
        }

        @Override // android.net.http.RequestFeeder
        public Request getRequest() {
            Request r = this.mRequest;
            this.mRequest = null;
            return r;
        }

        @Override // android.net.http.RequestFeeder
        public Request getRequest(HttpHost host) {
            return getRequest();
        }

        @Override // android.net.http.RequestFeeder
        public boolean haveRequest(HttpHost host) {
            return this.mRequest != null;
        }

        @Override // android.net.http.RequestFeeder
        public void requeueRequest(Request r) {
            this.mRequest = r;
        }
    }

    public RequestHandle queueSynchronousRequest(String url, WebAddress uri, String method, Map<String, String> headers, EventHandler eventHandler, InputStream bodyProvider, int bodyLength) {
        HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        return new RequestHandle(this, url, uri, method, headers, bodyProvider, bodyLength, new Request(method, host, this.mProxyHost, uri.getPath(), bodyProvider, bodyLength, eventHandler, headers), Connection.getConnection(this.mContext, determineHost(host), this.mProxyHost, new SyncFeeder()));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private HttpHost determineHost(HttpHost host) {
        if (this.mProxyHost == null || "https".equals(host.getSchemeName())) {
            return host;
        }
        return this.mProxyHost;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean requestsPending() {
        return !this.mPending.isEmpty();
    }

    /* access modifiers changed from: package-private */
    public synchronized void dump() {
        HttpLog.v("dump()");
        StringBuilder dump = new StringBuilder();
        int count = 0;
        if (!this.mPending.isEmpty()) {
            Iterator<Map.Entry<HttpHost, LinkedList<Request>>> iter = this.mPending.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<HttpHost, LinkedList<Request>> entry = iter.next();
                String hostName = entry.getKey().getHostName();
                StringBuilder sb = new StringBuilder();
                sb.append("p");
                int count2 = count + 1;
                sb.append(count);
                sb.append(" ");
                sb.append(hostName);
                sb.append(" ");
                StringBuilder line = new StringBuilder(sb.toString());
                entry.getValue().listIterator(0);
                while (iter.hasNext()) {
                    line.append(((Request) iter.next()) + " ");
                }
                dump.append((CharSequence) line);
                dump.append("\n");
                count = count2;
            }
        }
        HttpLog.v(dump.toString());
    }

    @Override // android.net.http.RequestFeeder
    public synchronized Request getRequest() {
        Request ret;
        ret = null;
        if (!this.mPending.isEmpty()) {
            ret = removeFirst(this.mPending);
        }
        return ret;
    }

    @Override // android.net.http.RequestFeeder
    public synchronized Request getRequest(HttpHost host) {
        Request ret;
        ret = null;
        if (this.mPending.containsKey(host)) {
            LinkedList<Request> reqList = this.mPending.get(host);
            ret = reqList.removeFirst();
            if (reqList.isEmpty()) {
                this.mPending.remove(host);
            }
        }
        return ret;
    }

    @Override // android.net.http.RequestFeeder
    public synchronized boolean haveRequest(HttpHost host) {
        return this.mPending.containsKey(host);
    }

    @Override // android.net.http.RequestFeeder
    public void requeueRequest(Request request) {
        queueRequest(request, true);
    }

    public void shutdown() {
        this.mActivePool.shutdown();
    }

    /* access modifiers changed from: protected */
    public synchronized void queueRequest(Request request, boolean head) {
        LinkedList<Request> reqList;
        HttpHost host = request.mProxyHost == null ? request.mHost : request.mProxyHost;
        if (this.mPending.containsKey(host)) {
            reqList = this.mPending.get(host);
        } else {
            reqList = new LinkedList<>();
            this.mPending.put(host, reqList);
        }
        if (head) {
            reqList.addFirst(request);
        } else {
            reqList.add(request);
        }
    }

    public void startTiming() {
        this.mActivePool.startTiming();
    }

    public void stopTiming() {
        this.mActivePool.stopTiming();
    }

    private Request removeFirst(LinkedHashMap<HttpHost, LinkedList<Request>> requestQueue) {
        Request ret = null;
        Iterator<Map.Entry<HttpHost, LinkedList<Request>>> iter = requestQueue.entrySet().iterator();
        if (iter.hasNext()) {
            Map.Entry<HttpHost, LinkedList<Request>> entry = iter.next();
            LinkedList<Request> reqList = entry.getValue();
            ret = reqList.removeFirst();
            if (reqList.isEmpty()) {
                requestQueue.remove(entry.getKey());
            }
        }
        return ret;
    }
}
