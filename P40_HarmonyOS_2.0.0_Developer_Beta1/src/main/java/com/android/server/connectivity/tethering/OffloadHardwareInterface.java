package com.android.server.connectivity.tethering;

import android.hardware.tetheroffload.control.V1_0.IOffloadControl;
import android.hardware.tetheroffload.control.V1_0.ITetheringOffloadCallback;
import android.hardware.tetheroffload.control.V1_0.NatTimeoutUpdate;
import android.net.util.SharedLog;
import android.os.Handler;
import android.os.RemoteException;
import android.system.OsConstants;
import com.android.internal.util.BitUtils;
import com.android.server.connectivity.tethering.OffloadHardwareInterface;
import java.util.ArrayList;

public class OffloadHardwareInterface {
    private static final int DEFAULT_TETHER_OFFLOAD_DISABLED = 0;
    private static final String NO_INTERFACE_NAME = "";
    private static final String NO_IPV4_ADDRESS = "";
    private static final String NO_IPV4_GATEWAY = "";
    private static final String TAG = OffloadHardwareInterface.class.getSimpleName();
    private static final String YIELDS = " -> ";
    private ControlCallback mControlCallback;
    private final Handler mHandler;
    private final SharedLog mLog;
    private IOffloadControl mOffloadControl;
    private TetheringOffloadCallback mTetheringOffloadCallback;

    private static native boolean configOffload();

    public static class ControlCallback {
        public void onStarted() {
        }

        public void onStoppedError() {
        }

        public void onStoppedUnsupported() {
        }

        public void onSupportAvailable() {
        }

        public void onStoppedLimitReached() {
        }

        public void onNatTimeoutUpdate(int proto, String srcAddr, int srcPort, String dstAddr, int dstPort) {
        }
    }

    public static class ForwardedStats {
        public long rxBytes = 0;
        public long txBytes = 0;

        public void add(ForwardedStats other) {
            this.rxBytes += other.rxBytes;
            this.txBytes += other.txBytes;
        }

        public String toString() {
            return String.format("rx:%s tx:%s", Long.valueOf(this.rxBytes), Long.valueOf(this.txBytes));
        }
    }

    public OffloadHardwareInterface(Handler h, SharedLog log) {
        this.mHandler = h;
        this.mLog = log.forSubComponent(TAG);
    }

    public int getDefaultTetherOffloadDisabled() {
        return 0;
    }

    public boolean initOffloadConfig() {
        return configOffload();
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0056: APUT  (r0v2 java.lang.Object[]), (0 ??[int, short, byte, char]), (r2v0 java.lang.String) */
    public boolean initOffloadControl(ControlCallback controlCb) {
        String str;
        this.mControlCallback = controlCb;
        if (this.mOffloadControl == null) {
            try {
                this.mOffloadControl = IOffloadControl.getService();
                if (this.mOffloadControl == null) {
                    this.mLog.e("tethering IOffloadControl.getService() returned null");
                    return false;
                }
            } catch (RemoteException e) {
                this.mLog.e("tethering offload control not supported: " + e);
                return false;
            }
        }
        Object[] objArr = new Object[1];
        if (controlCb == null) {
            str = "null";
        } else {
            str = "0x" + Integer.toHexString(System.identityHashCode(controlCb));
        }
        objArr[0] = str;
        String logmsg = String.format("initOffloadControl(%s)", objArr);
        this.mTetheringOffloadCallback = new TetheringOffloadCallback(this.mHandler, this.mControlCallback, this.mLog);
        CbResults results = new CbResults();
        try {
            this.mOffloadControl.initOffload(this.mTetheringOffloadCallback, new IOffloadControl.initOffloadCallback() {
                /* class com.android.server.connectivity.tethering.$$Lambda$OffloadHardwareInterface$324leYOM3BvGJiK4WadeB0d5jE */

                @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.initOffloadCallback
                public final void onValues(boolean z, String str) {
                    OffloadHardwareInterface.lambda$initOffloadControl$0(OffloadHardwareInterface.CbResults.this, z, str);
                }
            });
            record(logmsg, results);
            return results.success;
        } catch (RemoteException e2) {
            record(logmsg, e2);
            return false;
        }
    }

    static /* synthetic */ void lambda$initOffloadControl$0(CbResults results, boolean success, String errMsg) {
        results.success = success;
        results.errMsg = errMsg;
    }

    public void stopOffloadControl() {
        IOffloadControl iOffloadControl = this.mOffloadControl;
        if (iOffloadControl != null) {
            try {
                iOffloadControl.stopOffload(new IOffloadControl.stopOffloadCallback() {
                    /* class com.android.server.connectivity.tethering.$$Lambda$OffloadHardwareInterface$AOzzTRw82KskEfgGFRGSy26wGv8 */

                    @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.stopOffloadCallback
                    public final void onValues(boolean z, String str) {
                        OffloadHardwareInterface.this.lambda$stopOffloadControl$1$OffloadHardwareInterface(z, str);
                    }
                });
            } catch (RemoteException e) {
                SharedLog sharedLog = this.mLog;
                sharedLog.e("failed to stopOffload: " + e);
            }
        }
        this.mOffloadControl = null;
        this.mTetheringOffloadCallback = null;
        this.mControlCallback = null;
        this.mLog.log("stopOffloadControl()");
    }

    public /* synthetic */ void lambda$stopOffloadControl$1$OffloadHardwareInterface(boolean success, String errMsg) {
        if (!success) {
            SharedLog sharedLog = this.mLog;
            sharedLog.e("stopOffload failed: " + errMsg);
        }
    }

    public ForwardedStats getForwardedStats(String upstream) {
        String logmsg = String.format("getForwardedStats(%s)", upstream);
        ForwardedStats stats = new ForwardedStats();
        try {
            this.mOffloadControl.getForwardedStats(upstream, new IOffloadControl.getForwardedStatsCallback() {
                /* class com.android.server.connectivity.tethering.$$Lambda$OffloadHardwareInterface$nu77bP4WbZU9UPvjulauQE3Dm30 */

                @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.getForwardedStatsCallback
                public final void onValues(long j, long j2) {
                    OffloadHardwareInterface.lambda$getForwardedStats$2(OffloadHardwareInterface.ForwardedStats.this, j, j2);
                }
            });
            SharedLog sharedLog = this.mLog;
            sharedLog.log(logmsg + YIELDS + stats);
            return stats;
        } catch (RemoteException e) {
            record(logmsg, e);
            return stats;
        }
    }

    static /* synthetic */ void lambda$getForwardedStats$2(ForwardedStats stats, long rxBytes, long txBytes) {
        long j = 0;
        stats.rxBytes = rxBytes > 0 ? rxBytes : 0;
        if (txBytes > 0) {
            j = txBytes;
        }
        stats.txBytes = j;
    }

    public boolean setLocalPrefixes(ArrayList<String> localPrefixes) {
        String logmsg = String.format("setLocalPrefixes([%s])", String.join(",", localPrefixes));
        CbResults results = new CbResults();
        try {
            this.mOffloadControl.setLocalPrefixes(localPrefixes, new IOffloadControl.setLocalPrefixesCallback() {
                /* class com.android.server.connectivity.tethering.$$Lambda$OffloadHardwareInterface$IpWViosH4sGe7yz1VTujaEKIDNQ */

                @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.setLocalPrefixesCallback
                public final void onValues(boolean z, String str) {
                    OffloadHardwareInterface.lambda$setLocalPrefixes$3(OffloadHardwareInterface.CbResults.this, z, str);
                }
            });
            record(logmsg, results);
            return results.success;
        } catch (RemoteException e) {
            record(logmsg, e);
            return false;
        }
    }

    static /* synthetic */ void lambda$setLocalPrefixes$3(CbResults results, boolean success, String errMsg) {
        results.success = success;
        results.errMsg = errMsg;
    }

    public boolean setDataLimit(String iface, long limit) {
        String logmsg = String.format("setDataLimit(%s, %d)", iface, Long.valueOf(limit));
        CbResults results = new CbResults();
        try {
            this.mOffloadControl.setDataLimit(iface, limit, new IOffloadControl.setDataLimitCallback() {
                /* class com.android.server.connectivity.tethering.$$Lambda$OffloadHardwareInterface$4gz9PGxiHz6VaJglXvPXV_YCTo */

                @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.setDataLimitCallback
                public final void onValues(boolean z, String str) {
                    OffloadHardwareInterface.lambda$setDataLimit$4(OffloadHardwareInterface.CbResults.this, z, str);
                }
            });
            record(logmsg, results);
            return results.success;
        } catch (RemoteException e) {
            record(logmsg, e);
            return false;
        }
    }

    static /* synthetic */ void lambda$setDataLimit$4(CbResults results, boolean success, String errMsg) {
        results.success = success;
        results.errMsg = errMsg;
    }

    public boolean setUpstreamParameters(String iface, String v4addr, String v4gateway, ArrayList<String> v6gws) {
        String v4gateway2 = "";
        String iface2 = iface != null ? iface : v4gateway2;
        String v4addr2 = v4addr != null ? v4addr : v4gateway2;
        if (v4gateway != null) {
            v4gateway2 = v4gateway;
        }
        ArrayList<String> v6gws2 = v6gws != null ? v6gws : new ArrayList<>();
        String logmsg = String.format("setUpstreamParameters(%s, %s, %s, [%s])", iface2, v4addr2, v4gateway2, String.join(",", v6gws2));
        CbResults results = new CbResults();
        try {
            this.mOffloadControl.setUpstreamParameters(iface2, v4addr2, v4gateway2, v6gws2, new IOffloadControl.setUpstreamParametersCallback() {
                /* class com.android.server.connectivity.tethering.$$Lambda$OffloadHardwareInterface$2RWDKfyqU5SThZDqBkZ1L_XSJA */

                @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.setUpstreamParametersCallback
                public final void onValues(boolean z, String str) {
                    OffloadHardwareInterface.lambda$setUpstreamParameters$5(OffloadHardwareInterface.CbResults.this, z, str);
                }
            });
            record(logmsg, results);
            return results.success;
        } catch (RemoteException e) {
            record(logmsg, e);
            return false;
        }
    }

    static /* synthetic */ void lambda$setUpstreamParameters$5(CbResults results, boolean success, String errMsg) {
        results.success = success;
        results.errMsg = errMsg;
    }

    public boolean addDownstreamPrefix(String ifname, String prefix) {
        String logmsg = String.format("addDownstreamPrefix(%s, %s)", ifname, prefix);
        CbResults results = new CbResults();
        try {
            this.mOffloadControl.addDownstream(ifname, prefix, new IOffloadControl.addDownstreamCallback() {
                /* class com.android.server.connectivity.tethering.$$Lambda$OffloadHardwareInterface$GhKYJ09_bqn9xoRpQeCc3ZpQPU */

                @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.addDownstreamCallback
                public final void onValues(boolean z, String str) {
                    OffloadHardwareInterface.lambda$addDownstreamPrefix$6(OffloadHardwareInterface.CbResults.this, z, str);
                }
            });
            record(logmsg, results);
            return results.success;
        } catch (RemoteException e) {
            record(logmsg, e);
            return false;
        }
    }

    static /* synthetic */ void lambda$addDownstreamPrefix$6(CbResults results, boolean success, String errMsg) {
        results.success = success;
        results.errMsg = errMsg;
    }

    public boolean removeDownstreamPrefix(String ifname, String prefix) {
        String logmsg = String.format("removeDownstreamPrefix(%s, %s)", ifname, prefix);
        CbResults results = new CbResults();
        try {
            this.mOffloadControl.removeDownstream(ifname, prefix, new IOffloadControl.removeDownstreamCallback() {
                /* class com.android.server.connectivity.tethering.$$Lambda$OffloadHardwareInterface$w6w__dI5bH4oSI_P9WIdOzlG28 */

                @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.removeDownstreamCallback
                public final void onValues(boolean z, String str) {
                    OffloadHardwareInterface.lambda$removeDownstreamPrefix$7(OffloadHardwareInterface.CbResults.this, z, str);
                }
            });
            record(logmsg, results);
            return results.success;
        } catch (RemoteException e) {
            record(logmsg, e);
            return false;
        }
    }

    static /* synthetic */ void lambda$removeDownstreamPrefix$7(CbResults results, boolean success, String errMsg) {
        results.success = success;
        results.errMsg = errMsg;
    }

    private void record(String msg, Throwable t) {
        SharedLog sharedLog = this.mLog;
        sharedLog.e(msg + YIELDS + "exception: " + t);
    }

    private void record(String msg, CbResults results) {
        String logmsg = msg + YIELDS + results;
        if (!results.success) {
            this.mLog.e(logmsg);
        } else {
            this.mLog.log(logmsg);
        }
    }

    /* access modifiers changed from: private */
    public static class TetheringOffloadCallback extends ITetheringOffloadCallback.Stub {
        public final ControlCallback controlCb;
        public final Handler handler;
        public final SharedLog log;

        public TetheringOffloadCallback(Handler h, ControlCallback cb, SharedLog sharedLog) {
            this.handler = h;
            this.controlCb = cb;
            this.log = sharedLog;
        }

        @Override // android.hardware.tetheroffload.control.V1_0.ITetheringOffloadCallback
        public void onEvent(int event) {
            this.handler.post(new Runnable(event) {
                /* class com.android.server.connectivity.tethering.$$Lambda$OffloadHardwareInterface$TetheringOffloadCallback$nv6rlSkSWXyiDHHquQiDc8IaU0 */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    OffloadHardwareInterface.TetheringOffloadCallback.this.lambda$onEvent$0$OffloadHardwareInterface$TetheringOffloadCallback(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onEvent$0$OffloadHardwareInterface$TetheringOffloadCallback(int event) {
            if (event == 1) {
                this.controlCb.onStarted();
            } else if (event == 2) {
                this.controlCb.onStoppedError();
            } else if (event == 3) {
                this.controlCb.onStoppedUnsupported();
            } else if (event == 4) {
                this.controlCb.onSupportAvailable();
            } else if (event != 5) {
                SharedLog sharedLog = this.log;
                sharedLog.e("Unsupported OffloadCallbackEvent: " + event);
            } else {
                this.controlCb.onStoppedLimitReached();
            }
        }

        @Override // android.hardware.tetheroffload.control.V1_0.ITetheringOffloadCallback
        public void updateTimeout(NatTimeoutUpdate params) {
            this.handler.post(new Runnable(params) {
                /* class com.android.server.connectivity.tethering.$$Lambda$OffloadHardwareInterface$TetheringOffloadCallback$iUwkHUaFse6usZpm7pExz3WDNoQ */
                private final /* synthetic */ NatTimeoutUpdate f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    OffloadHardwareInterface.TetheringOffloadCallback.this.lambda$updateTimeout$1$OffloadHardwareInterface$TetheringOffloadCallback(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$updateTimeout$1$OffloadHardwareInterface$TetheringOffloadCallback(NatTimeoutUpdate params) {
            this.controlCb.onNatTimeoutUpdate(OffloadHardwareInterface.networkProtocolToOsConstant(params.proto), params.src.addr, BitUtils.uint16(params.src.port), params.dst.addr, BitUtils.uint16(params.dst.port));
        }
    }

    /* access modifiers changed from: private */
    public static int networkProtocolToOsConstant(int proto) {
        if (proto == 6) {
            return OsConstants.IPPROTO_TCP;
        }
        if (proto != 17) {
            return -Math.abs(proto);
        }
        return OsConstants.IPPROTO_UDP;
    }

    /* access modifiers changed from: private */
    public static class CbResults {
        String errMsg;
        boolean success;

        private CbResults() {
        }

        public String toString() {
            if (this.success) {
                return "ok";
            }
            return "fail: " + this.errMsg;
        }
    }
}
