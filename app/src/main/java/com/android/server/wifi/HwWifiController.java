package com.android.server.wifi;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.Slog;
import com.android.server.wifi.ABS.HwABSDetectorService;
import com.android.server.wifi.ABS.HwABSUtils;
import com.android.server.wifi.WifiServiceImpl.LockList;
import com.android.server.wifi.wifipro.WifiProStateMachine;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwWifiController extends WifiController {
    private static final int BASE = 155648;
    static final int CMD_AUTO_CONNECTION_MODE_CHANGED = 155748;
    private static final boolean DBG = true;
    private Context mContext;
    private HwWifiDataTrafficTracking mHwWifiDataTrafficTracking;
    HwWifiSettingsStoreEx mSettingsStoreEx;

    /* renamed from: com.android.server.wifi.HwWifiController.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            HwWifiController.this.mSettingsStoreEx.handleWifiAutoConnectChanged();
            HwWifiController.this.sendMessage(HwWifiController.CMD_AUTO_CONNECTION_MODE_CHANGED, HwWifiController.this.mSettingsStoreEx.isAutoConnectionEnabled() ? 1 : 0);
        }
    }

    public /* bridge */ /* synthetic */ void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
    }

    public HwWifiController(Context context, WifiStateMachine wsm, WifiSettingsStore wss, LockList locks, Looper looper, FrameworkFacade f) {
        super(context, wsm, wss, locks, looper, f);
        this.mContext = context;
        this.mSettingsStoreEx = new HwWifiSettingsStoreEx(context);
        registerForConnectModeChange();
        this.mHwWifiDataTrafficTracking = new HwWifiDataTrafficTracking(this.mContext, this, looper);
    }

    private void registerForConnectModeChange() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(MessageUtil.WIFI_CONNECT_TYPE), false, new AnonymousClass1(null));
    }

    protected boolean processDefaultState(Message message) {
        switch (message.what) {
            case CMD_AUTO_CONNECTION_MODE_CHANGED /*155748*/:
                return DBG;
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
                return DBG;
            default:
                return false;
        }
    }

    protected boolean setOperationalModeByMode() {
        if (this.mSettingsStoreEx == null) {
            Slog.d("HwWifiController", "setOperationalModeByMode mSettingsStoreEx = null");
            return false;
        } else if (this.mSettingsStoreEx == null || this.mSettingsStoreEx.isAutoConnectionEnabled()) {
            return false;
        } else {
            this.mWifiStateMachine.setOperationalMode(100);
            return DBG;
        }
    }

    public void createWifiProStateMachine(Context context, Messenger messenger) {
        WifiProStateMachine.createWifiProStateMachine(context, messenger);
    }

    public void putConnectWifiAppPid(Context context, int pid) {
        WifiProStateMachine.putConnectWifiAppPid(context, pid);
    }

    public void setupHwSelfCureEngine(Context context, WifiStateMachine wsm) {
        HwSelfCureEngine.getInstance(context, wsm).setup();
        HwWifiRoamingEngine.getInstance(context, wsm).setup();
    }

    public void startWifiDataTrafficTrack() {
        this.mHwWifiDataTrafficTracking.startTrack();
    }

    public void stopWifiDataTrafficTrack() {
        this.mHwWifiDataTrafficTracking.stopTrack();
    }

    public boolean isWifiRepeaterStarted() {
        return 1 == Global.getInt(this.mContext.getContentResolver(), "wifi_repeater_on", 0) ? DBG : false;
    }

    public void createABSService(Context context, WifiStateMachine wifiStateMachine) {
        Slog.d("HwWifiController", "createABSService");
        if (HwABSUtils.getABSEnable()) {
            HwABSDetectorService.createHwABSDetectorService(this.mContext, wifiStateMachine);
        }
    }
}
