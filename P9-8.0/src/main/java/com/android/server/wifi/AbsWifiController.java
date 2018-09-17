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

    protected boolean processDefaultState(Message message) {
        return false;
    }

    protected boolean processStaEnabled(Message message) {
        return false;
    }

    protected boolean setOperationalModeByMode() {
        return false;
    }

    public void createWifiProStateMachine(Context context, Messenger messenger) {
    }

    public void putConnectWifiAppPid(Context context, int pid) {
    }

    public void setupHwSelfCureEngine(Context context, WifiStateMachine wsm) {
    }

    public void createABSService(Context context, WifiStateMachine wifiStateMachine) {
    }

    public void startWifiDataTrafficTrack() {
    }

    public void stopWifiDataTrafficTrack() {
    }

    public boolean isWifiRepeaterStarted() {
        return false;
    }

    public void createQoEEngineService(Context context, WifiStateMachine wifiStateMachine) {
    }
}
