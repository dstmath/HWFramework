package com.android.server.wifi;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import com.android.internal.util.StateMachine;

public class AbsWifiController extends StateMachine {
    protected AbsWifiController(String name, Handler handler) {
        super(name, handler);
    }

    protected AbsWifiController(String name) {
        super(name);
    }

    protected AbsWifiController(String name, Looper looper) {
        super(name, looper);
    }

    /* access modifiers changed from: protected */
    public boolean processDefaultState(Message message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean processStaEnabled(Message message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean setOperationalModeByMode() {
        return false;
    }

    public void createWifiProStateMachine(Context context, Messenger messenger) {
    }

    public void putConnectWifiAppPid(Context context, int pid) {
    }

    public void setupHwSelfCureEngine(Context context, ClientModeImpl wsm) {
    }

    public void createABSService(Context context, ClientModeImpl wifiStateMachine) {
    }

    public void startWifiDataTrafficTrack() {
    }

    public void stopWifiDataTrafficTrack() {
    }

    public boolean isWifiRepeaterStarted() {
        return false;
    }

    public void createQoEEngineService(Context context, ClientModeImpl wifiStateMachine) {
    }

    public void updateWMUserAction(Context context, String action, String apkname) {
    }

    public void createWiTasService(Context context, WifiNative wifiNative) {
    }

    public void reportWiTasAntRssi(int index, int rssi) {
    }

    public void createHiCoexService(Context context, WifiNative wifiNative) {
    }

    public void createHwExtService(Context context) {
    }

    public void createFastSleepService(Context context, WifiNative wifiNative) {
    }
}
