package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.INetworkManagementEventObserver;
import android.net.InterfaceConfiguration;
import android.os.Binder;
import android.os.CommonTimeConfig;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.util.DumpUtils;
import com.android.server.UiModeManagerService;
import com.android.server.net.BaseNetworkObserver;
import java.io.FileDescriptor;
import java.io.PrintWriter;

class CommonTimeManagementService extends Binder {
    private static final boolean ALLOW_WIFI = (SystemProperties.getInt(ALLOW_WIFI_PROP, 0) != 0);
    private static final String ALLOW_WIFI_PROP = "ro.common_time.allow_wifi";
    private static final boolean AUTO_DISABLE = (SystemProperties.getInt(AUTO_DISABLE_PROP, 1) != 0);
    private static final String AUTO_DISABLE_PROP = "ro.common_time.auto_disable";
    private static final byte BASE_SERVER_PRIO;
    private static final InterfaceScoreRule[] IFACE_SCORE_RULES;
    private static final int NATIVE_SERVICE_RECONNECT_TIMEOUT = 5000;
    private static final int NO_INTERFACE_TIMEOUT = SystemProperties.getInt(NO_INTERFACE_TIMEOUT_PROP, 60000);
    private static final String NO_INTERFACE_TIMEOUT_PROP = "ro.common_time.no_iface_timeout";
    private static final String SERVER_PRIO_PROP = "ro.common_time.server_prio";
    private static final String TAG = CommonTimeManagementService.class.getSimpleName();
    private CommonTimeConfig mCTConfig;
    private CommonTimeConfig.OnServerDiedListener mCTServerDiedListener = new CommonTimeConfig.OnServerDiedListener() {
        public final void onServerDied() {
            CommonTimeManagementService.this.scheduleTimeConfigReconnect();
        }
    };
    private BroadcastReceiver mConnectivityMangerObserver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            CommonTimeManagementService.this.reevaluateServiceState();
        }
    };
    private final Context mContext;
    private String mCurIface;
    private boolean mDetectedAtStartup = false;
    private byte mEffectivePrio = BASE_SERVER_PRIO;
    private INetworkManagementEventObserver mIfaceObserver = new BaseNetworkObserver() {
        public void interfaceStatusChanged(String iface, boolean up) {
            CommonTimeManagementService.this.reevaluateServiceState();
        }

        public void interfaceLinkStateChanged(String iface, boolean up) {
            CommonTimeManagementService.this.reevaluateServiceState();
        }

        public void interfaceAdded(String iface) {
            CommonTimeManagementService.this.reevaluateServiceState();
        }

        public void interfaceRemoved(String iface) {
            CommonTimeManagementService.this.reevaluateServiceState();
        }
    };
    private final Object mLock = new Object();
    private INetworkManagementService mNetMgr;
    private Handler mNoInterfaceHandler = new Handler();
    private Runnable mNoInterfaceRunnable = new Runnable() {
        public final void run() {
            CommonTimeManagementService.this.handleNoInterfaceTimeout();
        }
    };
    private Handler mReconnectHandler = new Handler();
    private Runnable mReconnectRunnable = new Runnable() {
        public final void run() {
            CommonTimeManagementService.this.connectToTimeConfig();
        }
    };

    private static class InterfaceScoreRule {
        public final String mPrefix;
        public final byte mScore;

        public InterfaceScoreRule(String prefix, byte score) {
            this.mPrefix = prefix;
            this.mScore = score;
        }
    }

    static {
        int tmp = SystemProperties.getInt(SERVER_PRIO_PROP, 1);
        if (tmp < 1) {
            BASE_SERVER_PRIO = 1;
        } else if (tmp > 30) {
            BASE_SERVER_PRIO = 30;
        } else {
            BASE_SERVER_PRIO = (byte) tmp;
        }
        if (ALLOW_WIFI) {
            IFACE_SCORE_RULES = new InterfaceScoreRule[]{new InterfaceScoreRule("wlan", (byte) 1), new InterfaceScoreRule("eth", (byte) 2)};
        } else {
            IFACE_SCORE_RULES = new InterfaceScoreRule[]{new InterfaceScoreRule("eth", (byte) 2)};
        }
    }

    public CommonTimeManagementService(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: package-private */
    public void systemRunning() {
        if (ServiceManager.checkService("common_time.config") == null) {
            Log.i(TAG, "No common time service detected on this platform.  Common time services will be unavailable.");
            return;
        }
        this.mDetectedAtStartup = true;
        this.mNetMgr = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        try {
            this.mNetMgr.registerObserver(this.mIfaceObserver);
        } catch (RemoteException e) {
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(this.mConnectivityMangerObserver, filter);
        connectToTimeConfig();
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        String str;
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            if (!this.mDetectedAtStartup) {
                pw.println("Native Common Time service was not detected at startup.  Service is unavailable");
                return;
            }
            synchronized (this.mLock) {
                pw.println("Current Common Time Management Service Config:");
                Object[] objArr = new Object[1];
                if (this.mCTConfig == null) {
                    str = "reconnecting";
                } else {
                    str = "alive";
                }
                objArr[0] = str;
                pw.println(String.format("  Native service     : %s", objArr));
                Object[] objArr2 = new Object[1];
                objArr2[0] = this.mCurIface == null ? "unbound" : this.mCurIface;
                pw.println(String.format("  Bound interface    : %s", objArr2));
                Object[] objArr3 = new Object[1];
                objArr3[0] = ALLOW_WIFI ? UiModeManagerService.Shell.NIGHT_MODE_STR_YES : UiModeManagerService.Shell.NIGHT_MODE_STR_NO;
                pw.println(String.format("  Allow WiFi         : %s", objArr3));
                Object[] objArr4 = new Object[1];
                objArr4[0] = AUTO_DISABLE ? UiModeManagerService.Shell.NIGHT_MODE_STR_YES : UiModeManagerService.Shell.NIGHT_MODE_STR_NO;
                pw.println(String.format("  Allow Auto Disable : %s", objArr4));
                pw.println(String.format("  Server Priority    : %d", new Object[]{Byte.valueOf(this.mEffectivePrio)}));
                pw.println(String.format("  No iface timeout   : %d", new Object[]{Integer.valueOf(NO_INTERFACE_TIMEOUT)}));
            }
        }
    }

    private void cleanupTimeConfig() {
        this.mReconnectHandler.removeCallbacks(this.mReconnectRunnable);
        this.mNoInterfaceHandler.removeCallbacks(this.mNoInterfaceRunnable);
        if (this.mCTConfig != null) {
            this.mCTConfig.release();
            this.mCTConfig = null;
        }
    }

    /* access modifiers changed from: private */
    public void connectToTimeConfig() {
        cleanupTimeConfig();
        try {
            synchronized (this.mLock) {
                this.mCTConfig = new CommonTimeConfig();
                this.mCTConfig.setServerDiedListener(this.mCTServerDiedListener);
                this.mCurIface = this.mCTConfig.getInterfaceBinding();
                this.mCTConfig.setAutoDisable(AUTO_DISABLE);
                this.mCTConfig.setMasterElectionPriority(this.mEffectivePrio);
            }
            if (NO_INTERFACE_TIMEOUT >= 0) {
                this.mNoInterfaceHandler.postDelayed(this.mNoInterfaceRunnable, (long) NO_INTERFACE_TIMEOUT);
            }
            reevaluateServiceState();
        } catch (RemoteException e) {
            scheduleTimeConfigReconnect();
        }
    }

    /* access modifiers changed from: private */
    public void scheduleTimeConfigReconnect() {
        cleanupTimeConfig();
        Log.w(TAG, String.format("Native service died, will reconnect in %d mSec", new Object[]{Integer.valueOf(NATIVE_SERVICE_RECONNECT_TIMEOUT)}));
        this.mReconnectHandler.postDelayed(this.mReconnectRunnable, 5000);
    }

    /* access modifiers changed from: private */
    public void handleNoInterfaceTimeout() {
        if (this.mCTConfig != null) {
            Log.i(TAG, "Timeout waiting for interface to come up.  Forcing networkless master mode.");
            if (-7 == this.mCTConfig.forceNetworklessMasterMode()) {
                scheduleTimeConfigReconnect();
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:81:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    public void reevaluateServiceState() {
        boolean doRebind;
        byte newPrio;
        byte bestScore;
        String bindIface = null;
        byte bestScore2 = -1;
        try {
            String[] ifaceList = this.mNetMgr.listInterfaces();
            if (ifaceList != null) {
                int length = ifaceList.length;
                bestScore = -1;
                String bindIface2 = null;
                int i = 0;
                while (i < length) {
                    try {
                        String iface = ifaceList[i];
                        byte thisScore = -1;
                        InterfaceScoreRule[] interfaceScoreRuleArr = IFACE_SCORE_RULES;
                        int length2 = interfaceScoreRuleArr.length;
                        int i2 = 0;
                        while (true) {
                            if (i2 >= length2) {
                                break;
                            }
                            InterfaceScoreRule r = interfaceScoreRuleArr[i2];
                            if (iface.contains(r.mPrefix)) {
                                thisScore = r.mScore;
                                break;
                            }
                            i2++;
                        }
                        if (thisScore > bestScore) {
                            InterfaceConfiguration config = this.mNetMgr.getInterfaceConfig(iface);
                            if (config != null) {
                                if (config.isActive()) {
                                    bindIface2 = iface;
                                    bestScore = thisScore;
                                }
                            }
                        }
                        i++;
                    } catch (RemoteException e) {
                        bindIface = null;
                        bestScore2 = bestScore;
                        doRebind = true;
                        synchronized (this.mLock) {
                        }
                        if (!doRebind) {
                            return;
                        }
                    }
                }
                bindIface = bindIface2;
                bestScore2 = bestScore;
            }
        } catch (RemoteException e2) {
            bestScore = -1;
            RemoteException remoteException = e2;
            bindIface = null;
            bestScore2 = bestScore;
            doRebind = true;
            synchronized (this.mLock) {
            }
            if (!doRebind) {
            }
        }
        doRebind = true;
        synchronized (this.mLock) {
            if (bindIface != null) {
                try {
                    if (this.mCurIface == null) {
                        Log.e(TAG, String.format("Binding common time service to %s.", new Object[]{bindIface}));
                        this.mCurIface = bindIface;
                    }
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            }
            if (bindIface == null && this.mCurIface != null) {
                Log.e(TAG, "Unbinding common time service.");
                this.mCurIface = null;
            } else if (bindIface == null || this.mCurIface == null || bindIface.equals(this.mCurIface)) {
                doRebind = false;
            } else {
                Log.e(TAG, String.format("Switching common time service binding from %s to %s.", new Object[]{this.mCurIface, bindIface}));
                this.mCurIface = bindIface;
            }
        }
        if (!doRebind && this.mCTConfig != null) {
            if (bestScore2 > 0) {
                newPrio = (byte) (BASE_SERVER_PRIO * bestScore2);
            } else {
                newPrio = BASE_SERVER_PRIO;
            }
            if (newPrio != this.mEffectivePrio) {
                this.mEffectivePrio = newPrio;
                this.mCTConfig.setMasterElectionPriority(this.mEffectivePrio);
            }
            if (this.mCTConfig.setNetworkBinding(this.mCurIface) != 0) {
                scheduleTimeConfigReconnect();
            } else if (NO_INTERFACE_TIMEOUT >= 0) {
                this.mNoInterfaceHandler.removeCallbacks(this.mNoInterfaceRunnable);
                if (this.mCurIface == null) {
                    this.mNoInterfaceHandler.postDelayed(this.mNoInterfaceRunnable, (long) NO_INTERFACE_TIMEOUT);
                }
            }
        }
    }
}
