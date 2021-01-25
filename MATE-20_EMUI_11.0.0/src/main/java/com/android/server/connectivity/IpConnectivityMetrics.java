package com.android.server.connectivity;

import android.content.Context;
import android.net.ConnectivityMetricsEvent;
import android.net.IIpConnectivityMetrics;
import android.net.INetdEventCallback;
import android.net.metrics.ApfProgramEvent;
import android.os.Binder;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.RingBuffer;
import com.android.internal.util.TokenBucket;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToIntFunction;

public final class IpConnectivityMetrics extends SystemService {
    private static final boolean DBG = false;
    private static final int DEFAULT_BUFFER_SIZE = 2000;
    private static final int DEFAULT_LOG_SIZE = 500;
    private static final int ERROR_RATE_LIMITED = -1;
    private static final int MAXIMUM_BUFFER_SIZE = 20000;
    private static final int MAXIMUM_CONNECT_LATENCY_RECORDS = 20000;
    private static final int NYC = 0;
    private static final int NYC_MR1 = 1;
    private static final int NYC_MR2 = 2;
    private static final ToIntFunction<Context> READ_BUFFER_SIZE = $$Lambda$IpConnectivityMetrics$B0oR30xfeM300kIzUVaV_zUNLCg.INSTANCE;
    private static final String SERVICE_NAME = "connmetrics";
    private static final String TAG = IpConnectivityMetrics.class.getSimpleName();
    public static final int VERSION = 2;
    @VisibleForTesting
    public final Impl impl;
    @GuardedBy({"mLock"})
    private final ArrayMap<Class<?>, TokenBucket> mBuckets;
    @GuardedBy({"mLock"})
    private ArrayList<ConnectivityMetricsEvent> mBuffer;
    @GuardedBy({"mLock"})
    private int mCapacity;
    private final ToIntFunction<Context> mCapacityGetter;
    @VisibleForTesting
    final DefaultNetworkMetrics mDefaultNetworkMetrics;
    @GuardedBy({"mLock"})
    private int mDropped;
    @GuardedBy({"mLock"})
    private final RingBuffer<ConnectivityMetricsEvent> mEventLog;
    private final Object mLock;
    @VisibleForTesting
    NetdEventListenerService mNetdListener;

    public interface Logger {
        DefaultNetworkMetrics defaultNetworkMetrics();
    }

    public IpConnectivityMetrics(Context ctx, ToIntFunction<Context> capacityGetter) {
        super(ctx);
        this.mLock = new Object();
        this.impl = new Impl();
        this.mEventLog = new RingBuffer<>(ConnectivityMetricsEvent.class, 500);
        this.mBuckets = makeRateLimitingBuckets();
        this.mDefaultNetworkMetrics = new DefaultNetworkMetrics();
        this.mCapacityGetter = capacityGetter;
        initBuffer();
    }

    public IpConnectivityMetrics(Context ctx) {
        this(ctx, READ_BUFFER_SIZE);
    }

    @Override // com.android.server.SystemService
    public void onStart() {
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.connectivity.IpConnectivityMetrics */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v2, types: [com.android.server.connectivity.IpConnectivityMetrics$Impl, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mNetdListener = new NetdEventListenerService(getContext());
            publishBinderService(SERVICE_NAME, this.impl);
            publishBinderService(NetdEventListenerService.SERVICE_NAME, this.mNetdListener);
            LocalServices.addService(Logger.class, new LoggerImpl());
        }
    }

    @VisibleForTesting
    public int bufferCapacity() {
        return this.mCapacityGetter.applyAsInt(getContext());
    }

    private void initBuffer() {
        synchronized (this.mLock) {
            this.mDropped = 0;
            this.mCapacity = bufferCapacity();
            this.mBuffer = new ArrayList<>(this.mCapacity);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int append(ConnectivityMetricsEvent event) {
        synchronized (this.mLock) {
            this.mEventLog.append(event);
            int left = this.mCapacity - this.mBuffer.size();
            if (event == null) {
                return left;
            }
            if (isRateLimited(event)) {
                return -1;
            }
            if (left == 0) {
                this.mDropped++;
                return 0;
            }
            this.mBuffer.add(event);
            return left - 1;
        }
    }

    private boolean isRateLimited(ConnectivityMetricsEvent event) {
        TokenBucket tb = this.mBuckets.get(event.data.getClass());
        return tb != null && !tb.get();
    }

    private String flushEncodedOutput() {
        ArrayList<ConnectivityMetricsEvent> events;
        int dropped;
        synchronized (this.mLock) {
            events = this.mBuffer;
            dropped = this.mDropped;
            initBuffer();
        }
        List<IpConnectivityLogClass.IpConnectivityEvent> protoEvents = IpConnectivityEventBuilder.toProto(events);
        this.mDefaultNetworkMetrics.flushEvents(protoEvents);
        NetdEventListenerService netdEventListenerService = this.mNetdListener;
        if (netdEventListenerService != null) {
            netdEventListenerService.flushStatistics(protoEvents);
        }
        try {
            return Base64.encodeToString(IpConnectivityEventBuilder.serialize(dropped, protoEvents), 0);
        } catch (IOException e) {
            Log.e(TAG, "could not serialize events");
            return "";
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cmdFlush(PrintWriter pw) {
        pw.print(flushEncodedOutput());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cmdList(PrintWriter pw) {
        pw.println("metrics events:");
        for (ConnectivityMetricsEvent ev : getEvents()) {
            pw.println(ev.toString());
        }
        pw.println("");
        NetdEventListenerService netdEventListenerService = this.mNetdListener;
        if (netdEventListenerService != null) {
            netdEventListenerService.list(pw);
        }
        pw.println("");
        this.mDefaultNetworkMetrics.listEvents(pw);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cmdListAsProto(PrintWriter pw) {
        for (IpConnectivityLogClass.IpConnectivityEvent ev : IpConnectivityEventBuilder.toProto(getEvents())) {
            pw.print(ev.toString());
        }
        NetdEventListenerService netdEventListenerService = this.mNetdListener;
        if (netdEventListenerService != null) {
            netdEventListenerService.listAsProtos(pw);
        }
        this.mDefaultNetworkMetrics.listEventsAsProto(pw);
    }

    private List<ConnectivityMetricsEvent> getEvents() {
        List<ConnectivityMetricsEvent> asList;
        synchronized (this.mLock) {
            asList = Arrays.asList((ConnectivityMetricsEvent[]) this.mEventLog.toArray());
        }
        return asList;
    }

    public final class Impl extends IIpConnectivityMetrics.Stub {
        static final String CMD_DEFAULT = "";
        static final String CMD_FLUSH = "flush";
        static final String CMD_LIST = "list";
        static final String CMD_PROTO = "proto";

        public Impl() {
        }

        public int logEvent(ConnectivityMetricsEvent event) {
            enforceConnectivityInternalPermission();
            return IpConnectivityMetrics.this.append(event);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0033, code lost:
            if (r0.equals(com.android.server.connectivity.IpConnectivityMetrics.Impl.CMD_FLUSH) != false) goto L_0x0042;
         */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x0044  */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x0052  */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            enforceDumpPermission();
            char c = 0;
            String cmd = args.length > 0 ? args[0] : "";
            int hashCode = cmd.hashCode();
            if (hashCode != 3322014) {
                if (hashCode != 97532676) {
                    if (hashCode == 106940904 && cmd.equals(CMD_PROTO)) {
                        c = 1;
                        if (c == 0) {
                            IpConnectivityMetrics.this.cmdFlush(pw);
                            return;
                        } else if (c != 1) {
                            IpConnectivityMetrics.this.cmdList(pw);
                            return;
                        } else {
                            IpConnectivityMetrics.this.cmdListAsProto(pw);
                            return;
                        }
                    }
                }
            } else if (cmd.equals(CMD_LIST)) {
                c = 2;
                if (c == 0) {
                }
            }
            c = 65535;
            if (c == 0) {
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
            int uid = Binder.getCallingUid();
            if (uid != 1000) {
                throw new SecurityException(String.format("Uid %d has no permission to listen for netd events.", Integer.valueOf(uid)));
            }
        }

        public boolean addNetdEventCallback(int callerType, INetdEventCallback callback) {
            enforceNetdEventListeningPermission();
            if (IpConnectivityMetrics.this.mNetdListener == null) {
                return false;
            }
            return IpConnectivityMetrics.this.mNetdListener.addNetdEventCallback(callerType, callback);
        }

        public boolean removeNetdEventCallback(int callerType) {
            enforceNetdEventListeningPermission();
            if (IpConnectivityMetrics.this.mNetdListener == null) {
                return true;
            }
            return IpConnectivityMetrics.this.mNetdListener.removeNetdEventCallback(callerType);
        }
    }

    static /* synthetic */ int lambda$static$0(Context ctx) {
        int size = Settings.Global.getInt(ctx.getContentResolver(), "connectivity_metrics_buffer_size", 2000);
        if (size <= 0) {
            return 2000;
        }
        return Math.min(size, 20000);
    }

    private static ArrayMap<Class<?>, TokenBucket> makeRateLimitingBuckets() {
        ArrayMap<Class<?>, TokenBucket> map = new ArrayMap<>();
        map.put(ApfProgramEvent.class, new TokenBucket(60000, 50));
        return map;
    }

    private class LoggerImpl implements Logger {
        private LoggerImpl() {
        }

        @Override // com.android.server.connectivity.IpConnectivityMetrics.Logger
        public DefaultNetworkMetrics defaultNetworkMetrics() {
            return IpConnectivityMetrics.this.mDefaultNetworkMetrics;
        }
    }
}
