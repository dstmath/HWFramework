package com.huawei.wifi2;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.wifi2.HwWifi2Manager;
import com.android.server.wifi.wifi2.IHwWifi2Service;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import com.huawei.wifi2.HwWifi2Service;

public class HwWifi2Service implements IHwWifi2Service {
    private static final String TAG = "HwWifi2Service";
    private static final String WIFI_DIRECT_ACTIVITY_NAME = "com.huawei.android.wfdft.ui";
    private HwWifi2ActiveModeWarden mActiveModeWarden;
    private IHwActivityNotifierEx mActivityNotifierEx = null;
    private HwWifi2ClientModeImpl mClientModeImpl;
    private Context mContext;
    private Handler mHandler = null;
    private HwWifi2Native mHwWifi2Native;
    private boolean mIsBootCompleted = false;
    private boolean mIsP2pActive = false;
    private NetworkInfo.DetailedState mWifi1State = NetworkInfo.DetailedState.DISCONNECTED;
    private HwWifi2ConnectivityManager mWifiConnectivityManager;
    private HwWifi2Controller mWifiController;
    private HwWifi2Injector mWifiInjector;

    public HwWifi2Service(Context context) {
        this.mContext = context;
        this.mWifiInjector = new HwWifi2Injector(context, this);
        this.mClientModeImpl = this.mWifiInjector.getClientModeImpl();
        this.mHwWifi2Native = this.mWifiInjector.getWifiNative();
        this.mActiveModeWarden = this.mWifiInjector.getActiveModeWarden();
        this.mWifiController = this.mWifiInjector.getWifiController();
        this.mWifiConnectivityManager = this.mWifiInjector.getWifiConnectivityManager();
        this.mHandler = new Handler(this.mWifiInjector.getWifiCoreHandlerThread().getLooper());
        this.mClientModeImpl.enableRssiPolling(true);
        registerBroadcasts();
        registerHwActivityNotifier();
    }

    public void setSlaveWifiNetworkSelectionPara(int signalLevel, int callerUid, int needInternet) {
        this.mWifiConnectivityManager.setSlaveWifiNetworkSelectionPara(signalLevel, callerUid, needInternet);
    }

    public WifiInfo getSlaveWifiConnectionInfo() {
        return this.mClientModeImpl.getSlaveWifiConnectionInfo();
    }

    public LinkProperties getLinkPropertiesForSlaveWifi() {
        return this.mClientModeImpl.getLinkPropertiesForSlaveWifi();
    }

    public NetworkInfo getNetworkInfoForSlaveWifi() {
        return this.mClientModeImpl.getNetworkInfoForSlaveWifi();
    }

    public boolean setWifi2Enable(boolean isWifiEnable, int reason) {
        HwHiLog.i(TAG, false, "setWifi2Enable %{public}b reason %{public}s", new Object[]{Boolean.valueOf(isWifiEnable), HwWifi2Manager.msgToString(reason)});
        if (!this.mIsBootCompleted) {
            HwHiLog.i(TAG, false, "setWifi2Enable mIsBootCompleted is false, return directly", new Object[0]);
            return false;
        }
        if (!isWifiEnable) {
            this.mWifiController.sendMessage(155650);
        } else if (this.mIsP2pActive || this.mWifi1State != NetworkInfo.DetailedState.CONNECTED) {
            HwHiLog.i(TAG, false, "setWifi2Enable true fail mIsP2pActive is %{public}b + mWifi1State is %{public}d", new Object[]{Boolean.valueOf(this.mIsP2pActive), this.mWifi1State});
            return false;
        } else {
            this.mWifiController.sendMessage(155649);
        }
        HwWifi2ChrManager.handleWifi2Toggled(isWifiEnable, reason);
        return true;
    }

    public void handleP2pConnectCommand(int command) {
        this.mWifiConnectivityManager.handleP2pConnectCommand(command);
    }

    private void registerHwActivityNotifier() {
        this.mActivityNotifierEx = new IHwActivityNotifierEx() {
            /* class com.huawei.wifi2.HwWifi2Service.AnonymousClass1 */

            public void call(Bundle extras) {
                if (extras == null) {
                    HwHiLog.e(HwWifi2Service.TAG, false, "extras == null", new Object[0]);
                    return;
                }
                Object tempComp = extras.getParcelable("comp");
                ComponentName componentName = null;
                if (tempComp instanceof ComponentName) {
                    componentName = (ComponentName) tempComp;
                }
                int uid = extras.getInt("uid");
                if ("onResume".equals(extras.getString("state")) && componentName != null && uid != -1 && componentName.getClassName().contains(HwWifi2Service.WIFI_DIRECT_ACTIVITY_NAME)) {
                    HwHiLog.w(HwWifi2Service.TAG, false, "WIFI_DIRECT_ACTIVITY enter close wifi2", new Object[0]);
                    HwWifi2Service.this.setWifi2Enable(false, 1004);
                }
            }
        };
        ActivityManagerEx.registerHwActivityNotifier(this.mActivityNotifierEx, "activityLifeState");
    }

    private void registerReceiverForWifi1NetworkState() {
        HwHiLog.i(TAG, false, "registerReceiverForWifi1NetworkState", new Object[0]);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.STATE_CHANGE");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.huawei.wifi2.HwWifi2Service.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction()) && (intent.getParcelableExtra("networkInfo") instanceof NetworkInfo)) {
                    HwWifi2Service.this.mHandler.post(new Runnable((NetworkInfo) intent.getParcelableExtra("networkInfo")) {
                        /* class com.huawei.wifi2.$$Lambda$HwWifi2Service$2$8Wdp0xfZiqa3mEuYoIguB7yQtQ */
                        private final /* synthetic */ NetworkInfo f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            HwWifi2Service.AnonymousClass2.this.lambda$onReceive$0$HwWifi2Service$2(this.f$1);
                        }
                    });
                }
            }

            public /* synthetic */ void lambda$onReceive$0$HwWifi2Service$2(NetworkInfo info) {
                HwWifi2Service.this.handleWifi1NetworkStateChanged(info);
            }
        }, filter);
    }

    private void registerReceiverForBootCompleted() {
        HwHiLog.i(TAG, false, "registerReceiverForBootCompleted[D]", new Object[0]);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.huawei.wifi2.HwWifi2Service.AnonymousClass3 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                    HwWifi2Service.this.mHandler.post(new Runnable() {
                        /* class com.huawei.wifi2.$$Lambda$HwWifi2Service$3$DQMdMUhAPqd6YE5_21D19IjNupM */

                        @Override // java.lang.Runnable
                        public final void run() {
                            HwWifi2Service.AnonymousClass3.this.lambda$onReceive$0$HwWifi2Service$3();
                        }
                    });
                }
            }

            public /* synthetic */ void lambda$onReceive$0$HwWifi2Service$3() {
                HwWifi2Service.this.handleBootCompleted();
            }
        }, new IntentFilter("android.intent.action.BOOT_COMPLETED"));
    }

    private void registerReceiverForWifi1CategoryChanged() {
        HwHiLog.i(TAG, false, "registerReceiverForWifi1CategoryChanged", new Object[0]);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.huawei.wifi2.HwWifi2Service.AnonymousClass4 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                HwWifi2Service.this.mHandler.post(new Runnable() {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2Service$4$jumQKm5JBFh0C6AJq0Th4XwM1Vo */

                    @Override // java.lang.Runnable
                    public final void run() {
                        HwWifi2Service.AnonymousClass4.this.lambda$onReceive$0$HwWifi2Service$4();
                    }
                });
            }

            public /* synthetic */ void lambda$onReceive$0$HwWifi2Service$4() {
                HwWifi2Service.this.mClientModeImpl.handleWifi1CateChange();
            }
        }, new IntentFilter("com.huawei.wifi.action.WIFICATEGORY_CHANGED_ACTION"));
    }

    private void registerReceiverForP2pConnectionStateChanged() {
        HwHiLog.i(TAG, false, "registerReceiverForP2pConnectionStateChanged", new Object[0]);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        filter.addAction("android.net.wifi.p2p.CONNECT_STATE_CHANGE");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.huawei.wifi2.HwWifi2Service.AnonymousClass5 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    NetworkInfo info = null;
                    if (intent.getParcelableExtra("networkInfo") instanceof NetworkInfo) {
                        info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    }
                    if (info == null) {
                        HwHiLog.w(HwWifi2Service.TAG, false, "receive NETWORK_STATE_CHANGED_ACTION but network info is null", new Object[0]);
                        return;
                    }
                    boolean isP2pActivate = info.isConnectedOrConnecting();
                    HwHiLog.i(HwWifi2Service.TAG, false, "rec android p2p broadcast p2pActive: %{public}b", new Object[]{Boolean.valueOf(isP2pActivate)});
                    HwWifi2Service.this.mWifiConnectivityManager.setP2pActivate(isP2pActivate);
                    HwWifi2Service.this.mIsP2pActive = isP2pActivate;
                } else if ("android.net.wifi.p2p.CONNECT_STATE_CHANGE".equals(action)) {
                    int p2pState = intent.getIntExtra("extraState", -1);
                    HwHiLog.i(HwWifi2Service.TAG, false, "rec huawei p2p broadcast p2pState : %{public}b ", new Object[]{Integer.valueOf(p2pState)});
                    if (p2pState == 2 || p2pState == 1) {
                        HwWifi2Service.this.mWifiConnectivityManager.setP2pActivate(true);
                        HwWifi2Service.this.mIsP2pActive = true;
                        return;
                    }
                    HwWifi2Service.this.mWifiConnectivityManager.setP2pActivate(false);
                    HwWifi2Service.this.mIsP2pActive = false;
                } else {
                    HwHiLog.e(HwWifi2Service.TAG, false, "rec unknown p2p action: %{public}s", new Object[]{action});
                }
            }
        }, filter);
    }

    private void registerBroadcasts() {
        registerReceiverForWifi1NetworkState();
        registerReceiverForBootCompleted();
        registerReceiverForP2pConnectionStateChanged();
        registerReceiverForWifi1CategoryChanged();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifi1NetworkStateChanged(NetworkInfo info) {
        if (info == null) {
            HwHiLog.w(TAG, false, "receive NETWORK_STATE_CHANGED_ACTION but network info is null", new Object[0]);
            return;
        }
        NetworkInfo.DetailedState state = info.getDetailedState();
        HwHiLog.i(TAG, false, "wifi1 network state changed from %{public}s to %{public}s", new Object[]{this.mWifi1State, state});
        if (!this.mWifi1State.equals(state) || NetworkInfo.DetailedState.CONNECTED.equals(state)) {
            if (NetworkInfo.DetailedState.CONNECTED.equals(state)) {
                this.mWifiConnectivityManager.setWifi1connected(true);
            } else if (NetworkInfo.DetailedState.DISCONNECTED.equals(state)) {
                if (this.mIsBootCompleted) {
                    this.mWifiController.sendMessage(155650);
                }
                this.mWifiConnectivityManager.setWifi1connected(false);
            } else {
                this.mWifiConnectivityManager.setWifi1connected(false);
            }
            this.mWifi1State = state;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBootCompleted() {
        HwHiLog.i(TAG, false, "handleBootCompleted", new Object[0]);
        this.mClientModeImpl.handleBootCompleted();
        this.mWifiConnectivityManager.handleBootCompleted();
        this.mWifiInjector.getWifiNetworkSelector().handleBootCompleted();
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager == null || wifiManager.getWifiState() != 3) {
            HwHiLog.i(TAG, false, "handleBootCompleted, but wifi is closed", new Object[0]);
        } else {
            this.mClientModeImpl.syncInitialize();
        }
        this.mWifiController.start();
        this.mIsBootCompleted = true;
    }
}
