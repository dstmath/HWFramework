package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.INetworkManagementEventObserver;
import android.net.InterfaceConfiguration;
import android.os.Binder;
import android.os.CommonTimeConfig;
import android.os.CommonTimeConfig.OnServerDiedListener;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.util.DumpUtils;
import com.android.server.net.BaseNetworkObserver;
import java.io.FileDescriptor;
import java.io.PrintWriter;

class CommonTimeManagementService extends Binder {
    private static final boolean ALLOW_WIFI;
    private static final String ALLOW_WIFI_PROP = "ro.common_time.allow_wifi";
    private static final boolean AUTO_DISABLE;
    private static final String AUTO_DISABLE_PROP = "ro.common_time.auto_disable";
    private static final byte BASE_SERVER_PRIO;
    private static final InterfaceScoreRule[] IFACE_SCORE_RULES;
    private static final int NATIVE_SERVICE_RECONNECT_TIMEOUT = 5000;
    private static final int NO_INTERFACE_TIMEOUT = SystemProperties.getInt(NO_INTERFACE_TIMEOUT_PROP, 60000);
    private static final String NO_INTERFACE_TIMEOUT_PROP = "ro.common_time.no_iface_timeout";
    private static final String SERVER_PRIO_PROP = "ro.common_time.server_prio";
    private static final String TAG = CommonTimeManagementService.class.getSimpleName();
    private CommonTimeConfig mCTConfig;
    private OnServerDiedListener mCTServerDiedListener = new OnServerDiedListener() {
        public void onServerDied() {
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
    private Object mLock = new Object();
    private INetworkManagementService mNetMgr;
    private Handler mNoInterfaceHandler = new Handler();
    private Runnable mNoInterfaceRunnable = new Runnable() {
        public void run() {
            CommonTimeManagementService.this.handleNoInterfaceTimeout();
        }
    };
    private Handler mReconnectHandler = new Handler();
    private Runnable mReconnectRunnable = new Runnable() {
        public void run() {
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
        boolean z;
        if (SystemProperties.getInt(AUTO_DISABLE_PROP, 1) != 0) {
            z = true;
        } else {
            z = false;
        }
        AUTO_DISABLE = z;
        if (SystemProperties.getInt(ALLOW_WIFI_PROP, 0) != 0) {
            z = true;
        } else {
            z = false;
        }
        ALLOW_WIFI = z;
        int tmp = SystemProperties.getInt(SERVER_PRIO_PROP, 1);
        if (tmp < 1) {
            BASE_SERVER_PRIO = (byte) 1;
        } else if (tmp > 30) {
            BASE_SERVER_PRIO = (byte) 30;
        } else {
            BASE_SERVER_PRIO = (byte) tmp;
        }
        if (ALLOW_WIFI) {
            IFACE_SCORE_RULES = new InterfaceScoreRule[]{new InterfaceScoreRule("wlan", (byte) 1), new InterfaceScoreRule("eth", (byte) 2)};
            return;
        }
        IFACE_SCORE_RULES = new InterfaceScoreRule[]{new InterfaceScoreRule("eth", (byte) 2)};
    }

    public CommonTimeManagementService(Context context) {
        this.mContext = context;
    }

    void systemRunning() {
        if (ServiceManager.checkService("common_time.config") == null) {
            Log.i(TAG, "No common time service detected on this platform.  Common time services will be unavailable.");
            return;
        }
        this.mDetectedAtStartup = true;
        this.mNetMgr = Stub.asInterface(ServiceManager.getService("network_management"));
        try {
            this.mNetMgr.registerObserver(this.mIfaceObserver);
        } catch (RemoteException e) {
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(this.mConnectivityMangerObserver, filter);
        connectToTimeConfig();
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (!DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            return;
        }
        if (this.mDetectedAtStartup) {
            synchronized (this.mLock) {
                String str;
                pw.println("Current Common Time Management Service Config:");
                String str2 = "  Native service     : %s";
                Object[] objArr = new Object[1];
                if (this.mCTConfig == null) {
                    str = "reconnecting";
                } else {
                    str = "alive";
                }
                objArr[0] = str;
                pw.println(String.format(str2, objArr));
                str2 = "  Bound interface    : %s";
                objArr = new Object[1];
                objArr[0] = this.mCurIface == null ? "unbound" : this.mCurIface;
                pw.println(String.format(str2, objArr));
                str2 = "  Allow WiFi         : %s";
                objArr = new Object[1];
                objArr[0] = ALLOW_WIFI ? Shell.NIGHT_MODE_STR_YES : Shell.NIGHT_MODE_STR_NO;
                pw.println(String.format(str2, objArr));
                str2 = "  Allow Auto Disable : %s";
                objArr = new Object[1];
                objArr[0] = AUTO_DISABLE ? Shell.NIGHT_MODE_STR_YES : Shell.NIGHT_MODE_STR_NO;
                pw.println(String.format(str2, objArr));
                pw.println(String.format("  Server Priority    : %d", new Object[]{Byte.valueOf(this.mEffectivePrio)}));
                pw.println(String.format("  No iface timeout   : %d", new Object[]{Integer.valueOf(NO_INTERFACE_TIMEOUT)}));
            }
            return;
        }
        pw.println("Native Common Time service was not detected at startup.  Service is unavailable");
    }

    private void cleanupTimeConfig() {
        this.mReconnectHandler.removeCallbacks(this.mReconnectRunnable);
        this.mNoInterfaceHandler.removeCallbacks(this.mNoInterfaceRunnable);
        if (this.mCTConfig != null) {
            this.mCTConfig.release();
            this.mCTConfig = null;
        }
    }

    private void connectToTimeConfig() {
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

    private void scheduleTimeConfigReconnect() {
        cleanupTimeConfig();
        Log.w(TAG, String.format("Native service died, will reconnect in %d mSec", new Object[]{Integer.valueOf(NATIVE_SERVICE_RECONNECT_TIMEOUT)}));
        this.mReconnectHandler.postDelayed(this.mReconnectRunnable, 5000);
    }

    private void handleNoInterfaceTimeout() {
        if (this.mCTConfig != null) {
            Log.i(TAG, "Timeout waiting for interface to come up.  Forcing networkless master mode.");
            if (-7 == this.mCTConfig.forceNetworklessMasterMode()) {
                scheduleTimeConfigReconnect();
            }
        }
    }

    private void reevaluateServiceState() {
        String bindIface = null;
        int bestScore = -1;
        try {
            String[] ifaceList = this.mNetMgr.listInterfaces();
            if (ifaceList != null) {
                for (String iface : ifaceList) {
                    byte thisScore = (byte) -1;
                    for (InterfaceScoreRule r : IFACE_SCORE_RULES) {
                        if (iface.contains(r.mPrefix)) {
                            thisScore = r.mScore;
                            break;
                        }
                    }
                    if (thisScore > bestScore) {
                        InterfaceConfiguration config = this.mNetMgr.getInterfaceConfig(iface);
                        if (config != null && config.isActive()) {
                            bindIface = iface;
                            byte bestScore2 = thisScore;
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            bindIface = null;
        }
        boolean doRebind = true;
        synchronized (this.mLock) {
            if (bindIface != null) {
                if (this.mCurIface == null) {
                    Log.e(TAG, String.format("Binding common time service to %s.", new Object[]{bindIface}));
                    this.mCurIface = bindIface;
                }
            }
            if (bindIface == null) {
                if (this.mCurIface != null) {
                    Log.e(TAG, "Unbinding common time service.");
                    this.mCurIface = null;
                }
            }
            if (bindIface != null) {
                if (!(this.mCurIface == null || (bindIface.equals(this.mCurIface) ^ 1) == 0)) {
                    Log.e(TAG, String.format("Switching common time service binding from %s to %s.", new Object[]{this.mCurIface, bindIface}));
                    this.mCurIface = bindIface;
                }
            }
            doRebind = false;
        }
        if (doRebind && this.mCTConfig != null) {
            byte newPrio;
            if (bestScore > 0) {
                newPrio = (byte) (BASE_SERVER_PRIO * bestScore);
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
