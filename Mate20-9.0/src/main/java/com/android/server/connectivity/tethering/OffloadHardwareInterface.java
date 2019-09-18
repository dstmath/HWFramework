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

    private static class CbResults {
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
            return String.format("rx:%s tx:%s", new Object[]{Long.valueOf(this.rxBytes), Long.valueOf(this.txBytes)});
        }
    }

    private static class TetheringOffloadCallback extends ITetheringOffloadCallback.Stub {
        public final ControlCallback controlCb;
        public final Handler handler;
        public final SharedLog log;

        public TetheringOffloadCallback(Handler h, ControlCallback cb, SharedLog sharedLog) {
            this.handler = h;
            this.controlCb = cb;
            this.log = sharedLog;
        }

        public void onEvent(int event) {
            this.handler.post(new Runnable(event) {
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    OffloadHardwareInterface.TetheringOffloadCallback.lambda$onEvent$0(OffloadHardwareInterface.TetheringOffloadCallback.this, this.f$1);
                }
            });
        }

        public static /* synthetic */ void lambda$onEvent$0(TetheringOffloadCallback tetheringOffloadCallback, int event) {
            switch (event) {
                case 1:
                    tetheringOffloadCallback.controlCb.onStarted();
                    return;
                case 2:
                    tetheringOffloadCallback.controlCb.onStoppedError();
                    return;
                case 3:
                    tetheringOffloadCallback.controlCb.onStoppedUnsupported();
                    return;
                case 4:
                    tetheringOffloadCallback.controlCb.onSupportAvailable();
                    return;
                case 5:
                    tetheringOffloadCallback.controlCb.onStoppedLimitReached();
                    return;
                default:
                    SharedLog sharedLog = tetheringOffloadCallback.log;
                    sharedLog.e("Unsupported OffloadCallbackEvent: " + event);
                    return;
            }
        }

        public void updateTimeout(NatTimeoutUpdate params) {
            this.handler.post(new Runnable(params) {
                private final /* synthetic */ NatTimeoutUpdate f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    OffloadHardwareInterface.TetheringOffloadCallback.this.controlCb.onNatTimeoutUpdate(OffloadHardwareInterface.networkProtocolToOsConstant(this.f$1.proto), this.f$1.src.addr, BitUtils.uint16(this.f$1.src.port), this.f$1.dst.addr, BitUtils.uint16(this.f$1.dst.port));
                }
            });
        }
    }

    private static native boolean configOffload();

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
                public final void onValues(boolean z, String str) {
                    OffloadHardwareInterface.lambda$initOffloadControl$0(OffloadHardwareInterface.CbResults.this, z, str);
                }
            });
            record(logmsg, results);
            return results.success;
        } catch (RemoteException e2) {
            record(logmsg, (Throwable) e2);
            return false;
        }
    }

    static /* synthetic */ void lambda$initOffloadControl$0(CbResults results, boolean success, String errMsg) {
        results.success = success;
        results.errMsg = errMsg;
    }

    public void stopOffloadControl() {
        if (this.mOffloadControl != null) {
            try {
                this.mOffloadControl.stopOffload(new IOffloadControl.stopOffloadCallback() {
                    public final void onValues(boolean z, String str) {
                        OffloadHardwareInterface.lambda$stopOffloadControl$1(OffloadHardwareInterface.this, z, str);
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

    public static /* synthetic */ void lambda$stopOffloadControl$1(OffloadHardwareInterface offloadHardwareInterface, boolean success, String errMsg) {
        if (!success) {
            SharedLog sharedLog = offloadHardwareInterface.mLog;
            sharedLog.e("stopOffload failed: " + errMsg);
        }
    }

    public ForwardedStats getForwardedStats(String upstream) {
        String logmsg = String.format("getForwardedStats(%s)", new Object[]{upstream});
        ForwardedStats stats = new ForwardedStats();
        try {
            this.mOffloadControl.getForwardedStats(upstream, new IOffloadControl.getForwardedStatsCallback() {
                public final void onValues(long j, long j2) {
                    OffloadHardwareInterface.lambda$getForwardedStats$2(OffloadHardwareInterface.ForwardedStats.this, j, j2);
                }
            });
            SharedLog sharedLog = this.mLog;
            sharedLog.log(logmsg + YIELDS + stats);
            return stats;
        } catch (RemoteException e) {
            record(logmsg, (Throwable) e);
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
        String logmsg = String.format("setLocalPrefixes([%s])", new Object[]{String.join(",", localPrefixes)});
        CbResults results = new CbResults();
        try {
            this.mOffloadControl.setLocalPrefixes(localPrefixes, new IOffloadControl.setLocalPrefixesCallback() {
                public final void onValues(boolean z, String str) {
                    OffloadHardwareInterface.lambda$setLocalPrefixes$3(OffloadHardwareInterface.CbResults.this, z, str);
                }
            });
            record(logmsg, results);
            return results.success;
        } catch (RemoteException e) {
            record(logmsg, (Throwable) e);
            return false;
        }
    }

    static /* synthetic */ void lambda$setLocalPrefixes$3(CbResults results, boolean success, String errMsg) {
        results.success = success;
        results.errMsg = errMsg;
    }

    public boolean setDataLimit(String iface, long limit) {
        String logmsg = String.format("setDataLimit(%s, %d)", new Object[]{iface, Long.valueOf(limit)});
        CbResults results = new CbResults();
        try {
            this.mOffloadControl.setDataLimit(iface, limit, new IOffloadControl.setDataLimitCallback() {
                public final void onValues(boolean z, String str) {
                    OffloadHardwareInterface.lambda$setDataLimit$4(OffloadHardwareInterface.CbResults.this, z, str);
                }
            });
            record(logmsg, results);
            return results.success;
        } catch (RemoteException e) {
            record(logmsg, (Throwable) e);
            return false;
        }
    }

    static /* synthetic */ void lambda$setDataLimit$4(CbResults results, boolean success, String errMsg) {
        results.success = success;
        results.errMsg = errMsg;
    }

    public boolean setUpstreamParameters(String iface, String v4addr, String v4gateway, ArrayList<String> v6gws) {
        String iface2 = iface != null ? iface : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        String v4addr2 = v4addr != null ? v4addr : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        String v4gateway2 = v4gateway != null ? v4gateway : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        ArrayList<String> v6gws2 = v6gws != null ? v6gws : new ArrayList<>();
        String logmsg = String.format("setUpstreamParameters(%s, %s, %s, [%s])", new Object[]{iface2, v4addr2, v4gateway2, String.join(",", v6gws2)});
        CbResults results = new CbResults();
        try {
            this.mOffloadControl.setUpstreamParameters(iface2, v4addr2, v4gateway2, v6gws2, new IOffloadControl.setUpstreamParametersCallback() {
                public final void onValues(boolean z, String str) {
                    OffloadHardwareInterface.lambda$setUpstreamParameters$5(OffloadHardwareInterface.CbResults.this, z, str);
                }
            });
            record(logmsg, results);
            return results.success;
        } catch (RemoteException e) {
            record(logmsg, (Throwable) e);
            return false;
        }
    }

    static /* synthetic */ void lambda$setUpstreamParameters$5(CbResults results, boolean success, String errMsg) {
        results.success = success;
        results.errMsg = errMsg;
    }

    public boolean addDownstreamPrefix(String ifname, String prefix) {
        String logmsg = String.format("addDownstreamPrefix(%s, %s)", new Object[]{ifname, prefix});
        CbResults results = new CbResults();
        try {
            this.mOffloadControl.addDownstream(ifname, prefix, new IOffloadControl.addDownstreamCallback() {
                public final void onValues(boolean z, String str) {
                    OffloadHardwareInterface.lambda$addDownstreamPrefix$6(OffloadHardwareInterface.CbResults.this, z, str);
                }
            });
            record(logmsg, results);
            return results.success;
        } catch (RemoteException e) {
            record(logmsg, (Throwable) e);
            return false;
        }
    }

    static /* synthetic */ void lambda$addDownstreamPrefix$6(CbResults results, boolean success, String errMsg) {
        results.success = success;
        results.errMsg = errMsg;
    }

    public boolean removeDownstreamPrefix(String ifname, String prefix) {
        String logmsg = String.format("removeDownstreamPrefix(%s, %s)", new Object[]{ifname, prefix});
        CbResults results = new CbResults();
        try {
            this.mOffloadControl.removeDownstream(ifname, prefix, new IOffloadControl.removeDownstreamCallback() {
                public final void onValues(boolean z, String str) {
                    OffloadHardwareInterface.lambda$removeDownstreamPrefix$7(OffloadHardwareInterface.CbResults.this, z, str);
                }
            });
            record(logmsg, results);
            return results.success;
        } catch (RemoteException e) {
            record(logmsg, (Throwable) e);
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
    public static int networkProtocolToOsConstant(int proto) {
        if (proto == 6) {
            return OsConstants.IPPROTO_TCP;
        }
        if (proto != 17) {
            return -Math.abs(proto);
        }
        return OsConstants.IPPROTO_UDP;
    }
}
