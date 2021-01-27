package com.android.server.wifi.wifibthybrid;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.wifi.HwHiLog;

public class WifiBtHybridMonitor {
    private static final int A2DP_SCENE = 1;
    private static final String BT_WIFI_COEX_ACTION = "com.huawei.android.bt.WIFI_BT_COEX_STATUS";
    private static final int CURRENT_TASK_NUMBER = 0;
    private static final int DELAY_TIME = 3000;
    private static final String EXTRA_GAMESTATUS = "gameStatus";
    private static final String GAME_ACTION = "com.huawei.android.wifi.GAME_ACTION";
    private static final String HW_SIGNATURE_OR_SYSTEM = "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM";
    private static final String INVALID_STRING = "";
    private static final int RUNNING_TASK_NUMBER = 1;
    private static final int SIX_SLOT_SCENE = 2;
    private static final int STATE_OFF = 0;
    private static final int STATE_ON = 1;
    private static final String TAG = "WifiBtHybridMonitor";
    private boolean mA2dpHighModeFlag = false;
    private ActionReceiver mActionReceiver = null;
    private ActivityManager mActivityManager = null;
    private Context mContext;
    private boolean mGameFlag = false;
    private Handler mHandler;
    private boolean mIsWifiConnected = false;

    public WifiBtHybridMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        registerActionReceiver();
    }

    private void registerActionReceiver() {
        this.mActionReceiver = new ActionReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BT_WIFI_COEX_ACTION);
        filter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        filter.addAction(GAME_ACTION);
        filter.addAction("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED");
        filter.addAction("android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        this.mContext.registerReceiver(this.mActionReceiver, filter, HW_SIGNATURE_OR_SYSTEM, null);
    }

    /* access modifiers changed from: private */
    public class ActionReceiver extends BroadcastReceiver {
        private ActionReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (WifiBtHybridMonitor.BT_WIFI_COEX_ACTION.equals(action)) {
                    handleBtBusinessChange(intent);
                } else if (WifiBtHybridMonitor.GAME_ACTION.equals(action)) {
                    handleGameSceneChanged(intent);
                } else if ("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED".equals(action)) {
                    handleBtConnectionStateChange(intent);
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    handleNetworkStateChanged(intent);
                } else if (!"android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED".equals(action)) {
                    HwHiLog.d(WifiBtHybridMonitor.TAG, false, "WifiBtCoexBcastReceiver unknown action: %{public}s", new Object[]{action});
                } else if (intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1) == 10) {
                    WifiBtHybridMonitor.this.mHandler.sendEmptyMessageDelayed(6, 3000);
                }
            }
        }

        private void handleBtBusinessChange(Intent intent) {
            int tempScene = intent.getIntExtra("scene", -1);
            int tempStatus = intent.getIntExtra("status", -1);
            if (tempScene == 1) {
                if (tempStatus == 1) {
                    if (WifiBtHybridMonitor.this.mHandler.hasMessages(4)) {
                        WifiBtHybridMonitor.this.mHandler.removeMessages(4);
                    }
                    WifiBtHybridMonitor.this.mHandler.sendEmptyMessageDelayed(3, 3000);
                } else {
                    if (WifiBtHybridMonitor.this.mHandler.hasMessages(3)) {
                        WifiBtHybridMonitor.this.mHandler.removeMessages(3);
                    }
                    WifiBtHybridMonitor.this.mHandler.sendEmptyMessageDelayed(4, 3000);
                }
            }
            if (tempScene == 2 && tempStatus == 1) {
                if (WifiBtHybridMonitor.this.mHandler.hasMessages(6)) {
                    WifiBtHybridMonitor.this.mHandler.removeMessages(6);
                }
                WifiBtHybridMonitor.this.mHandler.sendEmptyMessageDelayed(5, 3000);
            }
        }

        private void handleGameSceneChanged(Intent intent) {
            int tempStatus = intent.getIntExtra(WifiBtHybridMonitor.EXTRA_GAMESTATUS, 0);
            if (tempStatus == 1 && !WifiBtHybridMonitor.this.mGameFlag) {
                HwHiLog.d(WifiBtHybridMonitor.TAG, false, "wifibthybrid game status enter", new Object[0]);
                WifiBtHybridMonitor.this.mGameFlag = true;
            } else if (tempStatus == 0 && WifiBtHybridMonitor.this.mGameFlag) {
                HwHiLog.d(WifiBtHybridMonitor.TAG, false, "wifibthybrid game status quit", new Object[0]);
                WifiBtHybridMonitor.this.mGameFlag = false;
            }
            WifiBtHybridMonitor.this.mHandler.sendMessage(WifiBtHybridMonitor.this.mHandler.obtainMessage(2, WifiBtHybridUtils.booleanToInt(WifiBtHybridMonitor.this.mGameFlag), 0));
        }

        private void handleBtConnectionStateChange(Intent intent) {
            int newState = intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", -1);
            if (newState == 0) {
                WifiBtHybridMonitor.this.mHandler.sendEmptyMessage(7);
                HwHiLog.d(WifiBtHybridMonitor.TAG, false, "wifibthybrid bluetooth disconnected", new Object[0]);
            } else if (newState == 2) {
                WifiBtHybridMonitor.this.mHandler.sendEmptyMessage(8);
                HwHiLog.d(WifiBtHybridMonitor.TAG, false, "wifibthybrid bluetooth connected", new Object[0]);
            }
        }

        private void handleNetworkStateChanged(Intent intent) {
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            NetworkInfo.DetailedState state = info == null ? NetworkInfo.DetailedState.IDLE : info.getDetailedState();
            if (state == NetworkInfo.DetailedState.CONNECTED) {
                if (!WifiBtHybridMonitor.this.mIsWifiConnected) {
                    HwHiLog.d(WifiBtHybridMonitor.TAG, false, "handleNetworkStateChanged:MSG_WIFI_CONNECTED", new Object[0]);
                    WifiBtHybridMonitor.this.mHandler.sendEmptyMessage(9);
                }
                WifiBtHybridMonitor.this.mIsWifiConnected = true;
            } else if (state == NetworkInfo.DetailedState.DISCONNECTED) {
                if (WifiBtHybridMonitor.this.mIsWifiConnected) {
                    HwHiLog.d(WifiBtHybridMonitor.TAG, false, "handleNetworkStateChanged:MSG_WIFI_DISCONNECTED", new Object[0]);
                    WifiBtHybridMonitor.this.mHandler.sendEmptyMessage(10);
                }
                WifiBtHybridMonitor.this.mIsWifiConnected = false;
            } else {
                HwHiLog.d(WifiBtHybridMonitor.TAG, false, "handleNetworkStateChanged state: %{public}s", new Object[]{state});
            }
        }
    }
}
