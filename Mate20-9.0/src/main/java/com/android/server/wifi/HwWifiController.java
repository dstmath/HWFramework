package com.android.server.wifi;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.util.Slog;
import com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler;
import com.android.server.wifi.ABS.HwABSDetectorService;
import com.android.server.wifi.ABS.HwABSUtils;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.HwWiTas.HwWiTasMonitor;
import com.android.server.wifi.HwWiTas.HwWiTasStateMachine;
import com.android.server.wifi.HwWiTas.HwWiTasUtils;
import com.android.server.wifi.LAA.HwLaaController;
import com.android.server.wifi.LAA.HwLaaUtils;
import com.android.server.wifi.wifipro.HwWifiproLiteStateMachine;
import com.android.server.wifi.wifipro.WifiProStateMachine;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.android.server.wifipro.WifiProCommonUtils;

public class HwWifiController extends WifiController {
    private static final int BASE = 155648;
    static final int CMD_AUTO_CONNECTION_MODE_CHANGED = 155748;
    private static final boolean DBG = true;
    private Context mContext;
    private HwWifiDataTrafficTracking mHwWifiDataTrafficTracking;
    HwWifiSettingsStoreEx mSettingsStoreEx;
    WifiStateMachine mWifiStateMachine;

    public HwWifiController(Context context, WifiStateMachine wsm, Looper wifiStateMachineLooper, WifiSettingsStore wss, Looper wifiServiceLooper, FrameworkFacade f, WifiStateMachinePrime wsmp) {
        super(context, wsm, wifiStateMachineLooper, wss, wifiServiceLooper, f, wsmp);
        this.mWifiStateMachine = wsm;
        this.mContext = context;
        this.mSettingsStoreEx = new HwWifiSettingsStoreEx(context);
        registerForConnectModeChange();
        this.mHwWifiDataTrafficTracking = new HwWifiDataTrafficTracking(this.mContext, wifiServiceLooper);
    }

    private void registerForConnectModeChange() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(MessageUtil.WIFI_CONNECT_TYPE), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                HwWifiController.this.mSettingsStoreEx.handleWifiAutoConnectChanged();
                HwWifiController.this.sendMessage(HwWifiController.CMD_AUTO_CONNECTION_MODE_CHANGED, HwWifiController.this.mSettingsStoreEx.isAutoConnectionEnabled() ? 1 : 0);
            }
        });
    }

    /* access modifiers changed from: protected */
    public boolean processDefaultState(Message message) {
        if (message.what != CMD_AUTO_CONNECTION_MODE_CHANGED) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processStaEnabled(Message message) {
        if (message.what != CMD_AUTO_CONNECTION_MODE_CHANGED) {
            return false;
        }
        int i = message.arg1;
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean setOperationalModeByMode() {
        if (this.mSettingsStoreEx == null) {
            Slog.d("HwWifiController", "setOperationalModeByMode mSettingsStoreEx = null");
            return false;
        } else if (this.mSettingsStoreEx == null || this.mSettingsStoreEx.isAutoConnectionEnabled()) {
            return false;
        } else {
            return true;
        }
    }

    public void createWifiProStateMachine(Context context, Messenger messenger) {
        if (WifiProCommonUtils.isWifiProLitePropertyEnabled(this.mContext)) {
            HwWifiproLiteStateMachine.getInstance(context, messenger).setup();
        } else if (WifiProCommonUtils.isWifiProPropertyEnabled(context)) {
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
        return 1 == Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_repeater_on", 0);
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

    public void updateWMUserAction(Context context, String action, String apkname) {
        Slog.d("WMapping.CollectUserFingersHandler.", "updateUserAction");
        if ("android.uid.system:1000".equals(apkname) || "com.android.settings".equals(apkname) || "com.android.systemui".equals(apkname)) {
            CollectUserFingersHandler collectUserFingersHandler = CollectUserFingersHandler.getInstance();
            if (collectUserFingersHandler != null) {
                collectUserFingersHandler.updateUserAction(action, apkname);
            }
        }
    }

    public void createWiTasService(Context context, WifiNative wifiNative) {
        if (HwWiTasUtils.getWiTasEnable()) {
            Slog.d(HwWiTasUtils.TAG, "createWiTasService");
            HwWiTasStateMachine.createWiTasStateMachine(context, wifiNative);
        }
    }

    public void reportWiTasAntRssi(int index, int rssi) {
        HwWiTasMonitor witasMonitor = HwWiTasMonitor.getInstance();
        if (witasMonitor != null) {
            witasMonitor.reportAntRssi(index, rssi);
        } else {
            Slog.w(HwWiTasUtils.TAG, "witasMonitor is null");
        }
    }
}
