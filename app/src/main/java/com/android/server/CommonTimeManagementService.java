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
import android.util.Log;
import com.android.server.net.BaseNetworkObserver;
import java.io.FileDescriptor;
import java.io.PrintWriter;

class CommonTimeManagementService extends Binder {
    private static final boolean ALLOW_WIFI = false;
    private static final String ALLOW_WIFI_PROP = "ro.common_time.allow_wifi";
    private static final boolean AUTO_DISABLE = false;
    private static final String AUTO_DISABLE_PROP = "ro.common_time.auto_disable";
    private static final byte BASE_SERVER_PRIO = (byte) 0;
    private static final InterfaceScoreRule[] IFACE_SCORE_RULES = null;
    private static final int NATIVE_SERVICE_RECONNECT_TIMEOUT = 5000;
    private static final int NO_INTERFACE_TIMEOUT = 0;
    private static final String NO_INTERFACE_TIMEOUT_PROP = "ro.common_time.no_iface_timeout";
    private static final String SERVER_PRIO_PROP = "ro.common_time.server_prio";
    private static final String TAG = null;
    private CommonTimeConfig mCTConfig;
    private OnServerDiedListener mCTServerDiedListener;
    private BroadcastReceiver mConnectivityMangerObserver;
    private final Context mContext;
    private String mCurIface;
    private boolean mDetectedAtStartup;
    private byte mEffectivePrio;
    private INetworkManagementEventObserver mIfaceObserver;
    private Object mLock;
    private INetworkManagementService mNetMgr;
    private Handler mNoInterfaceHandler;
    private Runnable mNoInterfaceRunnable;
    private Handler mReconnectHandler;
    private Runnable mReconnectRunnable;

    private static class InterfaceScoreRule {
        public final String mPrefix;
        public final byte mScore;

        public InterfaceScoreRule(String prefix, byte score) {
            this.mPrefix = prefix;
            this.mScore = score;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.CommonTimeManagementService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.CommonTimeManagementService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.CommonTimeManagementService.<clinit>():void");
    }

    public CommonTimeManagementService(Context context) {
        this.mReconnectHandler = new Handler();
        this.mNoInterfaceHandler = new Handler();
        this.mLock = new Object();
        this.mDetectedAtStartup = AUTO_DISABLE;
        this.mEffectivePrio = BASE_SERVER_PRIO;
        this.mIfaceObserver = new BaseNetworkObserver() {
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
        this.mConnectivityMangerObserver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                CommonTimeManagementService.this.reevaluateServiceState();
            }
        };
        this.mCTServerDiedListener = new OnServerDiedListener() {
            public void onServerDied() {
                CommonTimeManagementService.this.scheduleTimeConfigReconnect();
            }
        };
        this.mReconnectRunnable = new Runnable() {
            public void run() {
                CommonTimeManagementService.this.connectToTimeConfig();
            }
        };
        this.mNoInterfaceRunnable = new Runnable() {
            public void run() {
                CommonTimeManagementService.this.handleNoInterfaceTimeout();
            }
        };
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
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println(String.format("Permission Denial: can't dump CommonTimeManagement service from from pid=%d, uid=%d", new Object[]{Integer.valueOf(Binder.getCallingPid()), Integer.valueOf(Binder.getCallingUid())}));
        } else if (this.mDetectedAtStartup) {
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
                objArr[NO_INTERFACE_TIMEOUT] = str;
                pw.println(String.format(str2, objArr));
                str2 = "  Bound interface    : %s";
                objArr = new Object[1];
                objArr[NO_INTERFACE_TIMEOUT] = this.mCurIface == null ? "unbound" : this.mCurIface;
                pw.println(String.format(str2, objArr));
                str2 = "  Allow WiFi         : %s";
                objArr = new Object[1];
                objArr[NO_INTERFACE_TIMEOUT] = ALLOW_WIFI ? "yes" : "no";
                pw.println(String.format(str2, objArr));
                str2 = "  Allow Auto Disable : %s";
                objArr = new Object[1];
                objArr[NO_INTERFACE_TIMEOUT] = AUTO_DISABLE ? "yes" : "no";
                pw.println(String.format(str2, objArr));
                pw.println(String.format("  Server Priority    : %d", new Object[]{Byte.valueOf(this.mEffectivePrio)}));
                pw.println(String.format("  No iface timeout   : %d", new Object[]{Integer.valueOf(NO_INTERFACE_TIMEOUT)}));
            }
        } else {
            pw.println("Native Common Time service was not detected at startup.  Service is unavailable");
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
        String str = null;
        int i = -1;
        try {
            String[] ifaceList = this.mNetMgr.listInterfaces();
            if (ifaceList != null) {
                int length = ifaceList.length;
                for (int i2 = NO_INTERFACE_TIMEOUT; i2 < length; i2++) {
                    String iface = ifaceList[i2];
                    byte thisScore = (byte) -1;
                    InterfaceScoreRule[] interfaceScoreRuleArr = IFACE_SCORE_RULES;
                    int length2 = interfaceScoreRuleArr.length;
                    for (int i3 = NO_INTERFACE_TIMEOUT; i3 < length2; i3++) {
                        InterfaceScoreRule r = interfaceScoreRuleArr[i3];
                        if (iface.contains(r.mPrefix)) {
                            thisScore = r.mScore;
                            break;
                        }
                    }
                    if (thisScore > i) {
                        InterfaceConfiguration config = this.mNetMgr.getInterfaceConfig(iface);
                        if (config != null && config.isActive()) {
                            str = iface;
                            byte bestScore = thisScore;
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            str = null;
        }
        boolean doRebind = true;
        synchronized (this.mLock) {
            if (str != null) {
                if (this.mCurIface == null) {
                    Log.e(TAG, String.format("Binding common time service to %s.", new Object[]{str}));
                    this.mCurIface = str;
                }
            }
            if (str == null) {
                if (this.mCurIface != null) {
                    Log.e(TAG, "Unbinding common time service.");
                    this.mCurIface = null;
                }
            }
            if (str != null) {
                if (!(this.mCurIface == null || str.equals(this.mCurIface))) {
                    String str2 = TAG;
                    r16 = new Object[2];
                    r16[NO_INTERFACE_TIMEOUT] = this.mCurIface;
                    r16[1] = str;
                    Log.e(str2, String.format("Switching common time service binding from %s to %s.", r16));
                    this.mCurIface = str;
                }
            }
            doRebind = AUTO_DISABLE;
        }
        if (doRebind && this.mCTConfig != null) {
            byte newPrio;
            if (i > 0) {
                newPrio = (byte) (BASE_SERVER_PRIO * i);
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
