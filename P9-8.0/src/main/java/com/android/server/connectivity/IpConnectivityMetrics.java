package com.android.server.connectivity;

import android.content.Context;
import android.net.ConnectivityMetricsEvent;
import android.net.IIpConnectivityMetrics.Stub;
import android.net.INetdEventCallback;
import android.net.metrics.ApfProgramEvent;
import android.os.Binder;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.TokenBucket;
import com.android.server.SystemService;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass.IpConnectivityEvent;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public final class IpConnectivityMetrics extends SystemService {
    private static final boolean DBG = false;
    private static final int DEFAULT_BUFFER_SIZE = 2000;
    private static final int ERROR_RATE_LIMITED = -1;
    private static final int MAXIMUM_BUFFER_SIZE = 20000;
    private static final int MAXIMUM_CONNECT_LATENCY_RECORDS = 20000;
    private static final int NYC = 0;
    private static final int NYC_MR1 = 1;
    private static final int NYC_MR2 = 2;
    private static final ToIntFunction<Context> READ_BUFFER_SIZE = new -$Lambda$MsbVMSDQhSjxBVLOF10aov6ySH4();
    private static final String SERVICE_NAME = "connmetrics";
    private static final String TAG = IpConnectivityMetrics.class.getSimpleName();
    public static final int VERSION = 2;
    public final Impl impl;
    @GuardedBy("mLock")
    private final ArrayMap<Class<?>, TokenBucket> mBuckets;
    @GuardedBy("mLock")
    private ArrayList<ConnectivityMetricsEvent> mBuffer;
    @GuardedBy("mLock")
    private int mCapacity;
    private final ToIntFunction<Context> mCapacityGetter;
    @GuardedBy("mLock")
    private int mDropped;
    private final Object mLock;
    NetdEventListenerService mNetdListener;

    public final class Impl extends Stub {
        static final String CMD_DEFAULT = "stats";
        static final String CMD_DUMPSYS = "-a";
        static final String CMD_FLUSH = "flush";
        static final String CMD_LIST = "list";
        static final String CMD_STATS = "stats";

        public int logEvent(ConnectivityMetricsEvent event) {
            enforceConnectivityInternalPermission();
            return IpConnectivityMetrics.this.append(event);
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            enforceDumpPermission();
            String cmd = args.length > 0 ? args[0] : "stats";
            if (cmd.equals(CMD_FLUSH)) {
                IpConnectivityMetrics.this.cmdFlush(fd, pw, args);
            } else if (cmd.equals(CMD_DUMPSYS) || cmd.equals(CMD_LIST)) {
                IpConnectivityMetrics.this.cmdList(fd, pw, args);
            } else if (cmd.equals("stats")) {
                IpConnectivityMetrics.this.cmdStats(fd, pw, args);
            } else {
                IpConnectivityMetrics.this.cmdDefault(fd, pw, args);
            }
        }

        private void enforceConnectivityInternalPermission() {
            enforcePermission("android.permission.CONNECTIVITY_INTERNAL");
        }

        private void enforceDumpPermission() {
            enforcePermission("android.permission.DUMP");
        }

        private void enforcePermission(String what) {
            IpConnectivityMetrics.this.getContext().enforceCallingOrSelfPermission(what, "IpConnectivityMetrics");
        }

        private void enforceNetdEventListeningPermission() {
            if (Binder.getCallingUid() != 1000) {
                throw new SecurityException(String.format("Uid %d has no permission to listen for netd events.", new Object[]{Integer.valueOf(Binder.getCallingUid())}));
            }
        }

        public boolean registerNetdEventCallback(INetdEventCallback callback) {
            enforceNetdEventListeningPermission();
            if (IpConnectivityMetrics.this.mNetdListener == null) {
                return false;
            }
            return IpConnectivityMetrics.this.mNetdListener.registerNetdEventCallback(callback);
        }

        public boolean unregisterNetdEventCallback() {
            enforceNetdEventListeningPermission();
            if (IpConnectivityMetrics.this.mNetdListener == null) {
                return true;
            }
            return IpConnectivityMetrics.this.mNetdListener.unregisterNetdEventCallback();
        }
    }

    public IpConnectivityMetrics(Context ctx, ToIntFunction<Context> capacityGetter) {
        super(ctx);
        this.mLock = new Object();
        this.impl = new Impl();
        this.mBuckets = makeRateLimitingBuckets();
        this.mCapacityGetter = capacityGetter;
        initBuffer();
    }

    public IpConnectivityMetrics(Context ctx) {
        this(ctx, READ_BUFFER_SIZE);
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mNetdListener = new NetdEventListenerService(getContext());
            publishBinderService(SERVICE_NAME, this.impl);
            publishBinderService(NetdEventListenerService.SERVICE_NAME, this.mNetdListener);
        }
    }

    public int bufferCapacity() {
        return this.mCapacityGetter.applyAsInt(getContext());
    }

    private void initBuffer() {
        synchronized (this.mLock) {
            this.mDropped = 0;
            this.mCapacity = bufferCapacity();
            this.mBuffer = new ArrayList(this.mCapacity);
        }
    }

    private int append(ConnectivityMetricsEvent event) {
        synchronized (this.mLock) {
            int left = this.mCapacity - this.mBuffer.size();
            if (event == null) {
                return left;
            } else if (isRateLimited(event)) {
                return -1;
            } else if (left == 0) {
                this.mDropped++;
                return 0;
            } else {
                this.mBuffer.add(event);
                int i = left - 1;
                return i;
            }
        }
    }

    private boolean isRateLimited(ConnectivityMetricsEvent event) {
        TokenBucket tb = (TokenBucket) this.mBuckets.get(event.data.getClass());
        return tb != null ? tb.get() ^ 1 : false;
    }

    private String flushEncodedOutput() {
        List events;
        int dropped;
        synchronized (this.mLock) {
            events = this.mBuffer;
            dropped = this.mDropped;
            initBuffer();
        }
        List<IpConnectivityEvent> protoEvents = IpConnectivityEventBuilder.toProto(events);
        if (this.mNetdListener != null) {
            this.mNetdListener.flushStatistics(protoEvents);
        }
        try {
            return Base64.encodeToString(IpConnectivityEventBuilder.serialize(dropped, protoEvents), 0);
        } catch (IOException e) {
            Log.e(TAG, "could not serialize events", e);
            return "";
        }
    }

    private void cmdFlush(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print(flushEncodedOutput());
    }

    private void cmdList(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            List<ConnectivityMetricsEvent> events = new ArrayList(this.mBuffer);
        }
        if (args.length <= 1 || !args[1].equals("proto")) {
            for (ConnectivityMetricsEvent ev : events) {
                pw.println(ev.toString());
            }
            if (this.mNetdListener != null) {
                this.mNetdListener.list(pw);
            }
            return;
        }
        for (IpConnectivityEvent ev2 : IpConnectivityEventBuilder.toProto((List) events)) {
            pw.print(ev2.toString());
        }
        if (this.mNetdListener != null) {
            this.mNetdListener.listAsProtos(pw);
        }
    }

    private void cmdStats(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            pw.println("Buffered events: " + this.mBuffer.size());
            pw.println("Buffer capacity: " + this.mCapacity);
            pw.println("Dropped events: " + this.mDropped);
        }
        if (this.mNetdListener != null) {
            this.mNetdListener.dump(pw);
        }
    }

    private void cmdDefault(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length == 0) {
            pw.println("No command");
        } else {
            pw.println("Unknown command " + TextUtils.join(" ", args));
        }
    }

    static /* synthetic */ int lambda$-com_android_server_connectivity_IpConnectivityMetrics_10868(Context ctx) {
        int size = Global.getInt(ctx.getContentResolver(), "connectivity_metrics_buffer_size", 2000);
        if (size <= 0) {
            return 2000;
        }
        return Math.min(size, 20000);
    }

    private static ArrayMap<Class<?>, TokenBucket> makeRateLimitingBuckets() {
        ArrayMap<Class<?>, TokenBucket> map = new ArrayMap();
        map.put(ApfProgramEvent.class, new TokenBucket(60000, 50));
        return map;
    }
}
