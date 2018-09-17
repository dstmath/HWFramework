package com.android.server.wifi;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.Slog;
import com.android.server.wifi.ABS.HwABSDetectorService;
import com.android.server.wifi.ABS.HwABSUtils;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.LAA.HwLaaController;
import com.android.server.wifi.LAA.HwLaaUtils;
import com.android.server.wifi.wifipro.WifiProStateMachine;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.android.server.wifipro.WifiProCommonUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwWifiController extends WifiController {
    private static final int BASE = 155648;
    static final int CMD_AUTO_CONNECTION_MODE_CHANGED = 155748;
    private static final boolean DBG = true;
    private Context mContext;
    private HwWifiDataTrafficTracking mHwWifiDataTrafficTracking;
    HwWifiSettingsStoreEx mSettingsStoreEx;
    WifiStateMachine mWifiStateMachine;

    public /* bridge */ /* synthetic */ void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(fileDescriptor, printWriter, strArr);
    }

    public HwWifiController(Context context, WifiStateMachine wsm, WifiSettingsStore wss, WifiLockManager wifiLockManager, Looper looper, FrameworkFacade f) {
        super(context, wsm, wss, wifiLockManager, looper, f);
        this.mWifiStateMachine = wsm;
        this.mContext = context;
        this.mSettingsStoreEx = new HwWifiSettingsStoreEx(context);
        registerForConnectModeChange();
        this.mHwWifiDataTrafficTracking = new HwWifiDataTrafficTracking(this.mContext, wifiLockManager, looper);
    }

    private void registerForConnectModeChange() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(MessageUtil.WIFI_CONNECT_TYPE), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                HwWifiController.this.mSettingsStoreEx.handleWifiAutoConnectChanged();
                HwWifiController.this.sendMessage(HwWifiController.CMD_AUTO_CONNECTION_MODE_CHANGED, HwWifiController.this.mSettingsStoreEx.isAutoConnectionEnabled() ? 1 : 0);
            }
        });
    }

    protected boolean processDefaultState(Message message) {
        switch (message.what) {
            case CMD_AUTO_CONNECTION_MODE_CHANGED /*155748*/:
                return true;
            default:
                return false;
        }
    }

    protected boolean processStaEnabled(Message message) {
        switch (message.what) {
            case CMD_AUTO_CONNECTION_MODE_CHANGED /*155748*/:
                if (message.arg1 == 1) {
                    this.mWifiStateMachine.setOperationalMode(1);
                } else {
                    this.mWifiStateMachine.setOperationalMode(100);
                }
                return true;
            default:
                return false;
        }
    }

    protected boolean setOperationalModeByMode() {
        if (this.mSettingsStoreEx == null) {
            Slog.d("HwWifiController", "setOperationalModeByMode mSettingsStoreEx = null");
            return false;
        } else if (this.mSettingsStoreEx == null || (this.mSettingsStoreEx.isAutoConnectionEnabled() ^ 1) == 0) {
            return false;
        } else {
            this.mWifiStateMachine.setOperationalMode(100);
            return true;
        }
    }

    public void createWifiProStateMachine(Context context, Messenger messenger) {
        if (WifiProCommonUtils.isWifiProPropertyEnabled()) {
            WifiProStateMachine.createWifiProStateMachine(context, messenger);
        }
    }

    public void putConnectWifiAppPid(Context context, int pid) {
        WifiProStateMachine.putConnectWifiAppPid(context, pid);
    }

    public void setupHwSelfCureEngine(Context context, WifiStateMachine wsm) {
        HwSelfCureEngine.getInstance(context, wsm).setup();
        HwWifiConnectivityMonitor.getInstance(context, wsm).setup();
    }

    public void startWifiDataTrafficTrack() {
        this.mHwWifiDataTrafficTracking.startTrack();
    }

    public void stopWifiDataTrafficTrack() {
        this.mHwWifiDataTrafficTracking.stopTrack();
    }

    public boolean isWifiRepeaterStarted() {
        return 1 == Global.getInt(this.mContext.getContentResolver(), "wifi_repeater_on", 0);
    }

    public void createABSService(Context context, WifiStateMachine wifiStateMachine) {
        Slog.d("HwWifiController", "createABSService");
        if (HwABSUtils.getABSEnable()) {
            HwABSDetectorService.createHwABSDetectorService(this.mContext, wifiStateMachine);
        }
    }

    public void createQoEEngineService(Context context, WifiStateMachine wifiStateMachine) {
        Slog.d("HwQoEService", "createQoEService");
        HwQoEService.createHwQoEService(context, wifiStateMachine);
        if (HwLaaUtils.isLaaPlusEnable()) {
            HwLaaController.createHwLaaController(context);
        }
    }
}
