package com.android.server.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.net.metrics.DnsEvent;
import android.net.metrics.IDnsEventListener.Stub;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IndentingPrintWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

public class DnsEventListenerService extends Stub {
    private static final boolean DBG = true;
    private static final int MAX_LOOKUPS_PER_DNS_EVENT = 100;
    public static final String SERVICE_NAME = "dns_listener";
    private static final String TAG = null;
    private static final boolean VDBG = false;
    private final ConnectivityManager mCm;
    @GuardedBy("this")
    private SortedMap<Integer, DnsEventBatch> mEventBatches;
    private final NetworkCallback mNetworkCallback;

    private static class DnsEventBatch {
        private int mEventCount;
        private final byte[] mEventTypes;
        private final int[] mLatenciesMs;
        private final int mNetId;
        private final byte[] mReturnCodes;

        public DnsEventBatch(int netId) {
            this.mEventTypes = new byte[DnsEventListenerService.MAX_LOOKUPS_PER_DNS_EVENT];
            this.mReturnCodes = new byte[DnsEventListenerService.MAX_LOOKUPS_PER_DNS_EVENT];
            this.mLatenciesMs = new int[DnsEventListenerService.MAX_LOOKUPS_PER_DNS_EVENT];
            this.mNetId = netId;
        }

        public void addResult(byte eventType, byte returnCode, int latencyMs) {
            this.mEventTypes[this.mEventCount] = eventType;
            this.mReturnCodes[this.mEventCount] = returnCode;
            this.mLatenciesMs[this.mEventCount] = latencyMs;
            this.mEventCount++;
            if (this.mEventCount == DnsEventListenerService.MAX_LOOKUPS_PER_DNS_EVENT) {
                logAndClear();
            }
        }

        public void logAndClear() {
            if (this.mEventCount != 0) {
                DnsEvent.logEvent(this.mNetId, Arrays.copyOf(this.mEventTypes, this.mEventCount), Arrays.copyOf(this.mReturnCodes, this.mEventCount), Arrays.copyOf(this.mLatenciesMs, this.mEventCount));
                DnsEventListenerService.maybeLog(String.format("Logging %d results for netId %d", new Object[]{Integer.valueOf(this.mEventCount), Integer.valueOf(this.mNetId)}));
                this.mEventCount = 0;
            }
        }

        public String toString() {
            return String.format("%s %d %d", new Object[]{getClass().getSimpleName(), Integer.valueOf(this.mNetId), Integer.valueOf(this.mEventCount)});
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.connectivity.DnsEventListenerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.connectivity.DnsEventListenerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.DnsEventListenerService.<clinit>():void");
    }

    public DnsEventListenerService(Context context) {
        this.mEventBatches = new TreeMap();
        this.mNetworkCallback = new NetworkCallback() {
            public void onLost(Network network) {
                synchronized (DnsEventListenerService.this) {
                    DnsEventBatch batch = (DnsEventBatch) DnsEventListenerService.this.mEventBatches.remove(Integer.valueOf(network.netId));
                    if (batch != null) {
                        batch.logAndClear();
                    }
                }
            }
        };
        NetworkRequest request = new Builder().clearCapabilities().build();
        this.mCm = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
        this.mCm.registerNetworkCallback(request, this.mNetworkCallback);
    }

    public synchronized void onDnsEvent(int netId, int eventType, int returnCode, int latencyMs) {
        maybeVerboseLog(String.format("onDnsEvent(%d, %d, %d, %d)", new Object[]{Integer.valueOf(netId), Integer.valueOf(eventType), Integer.valueOf(returnCode), Integer.valueOf(latencyMs)}));
        DnsEventBatch batch = (DnsEventBatch) this.mEventBatches.get(Integer.valueOf(netId));
        if (batch == null) {
            batch = new DnsEventBatch(netId);
            this.mEventBatches.put(Integer.valueOf(netId), batch);
        }
        batch.addResult((byte) eventType, (byte) returnCode, latencyMs);
    }

    public synchronized void dump(PrintWriter writer) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println(TAG + ":");
        pw.increaseIndent();
        for (DnsEventBatch batch : this.mEventBatches.values()) {
            pw.println(batch.toString());
        }
        pw.decreaseIndent();
    }

    private static void maybeLog(String s) {
        Log.d(TAG, s);
    }

    private static void maybeVerboseLog(String s) {
    }
}
