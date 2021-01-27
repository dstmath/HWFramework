package com.huawei.wifi2;

import android.os.Looper;
import android.os.Message;
import android.util.wifi.HwHiLog;
import com.android.internal.util.StateMachine;

public class HwWifi2BaseStateMachine extends StateMachine {
    private static final String TAG = "Wifi2StateMachine";

    public HwWifi2BaseStateMachine(String name, Looper looper) {
        super(name, looper);
    }

    public void sendMessage(int what) {
        logMsg(what);
        HwWifi2BaseStateMachine.super.sendMessage(what);
    }

    public void sendMessage(int what, Object obj) {
        logMsg(what);
        HwWifi2BaseStateMachine.super.sendMessage(what, obj);
    }

    public void sendMessage(int what, int arg1) {
        logMsg(what);
        HwWifi2BaseStateMachine.super.sendMessage(what, arg1);
    }

    public void sendMessage(int what, int arg1, int arg2) {
        logMsg(what);
        HwWifi2BaseStateMachine.super.sendMessage(what, arg1, arg2);
    }

    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        logMsg(what);
        HwWifi2BaseStateMachine.super.sendMessage(what, arg1, arg2, obj);
    }

    public void sendMessage(Message msg) {
        logMsg(msg.what);
        HwWifi2BaseStateMachine.super.sendMessage(msg);
    }

    private void logMsg(int what) {
        HwHiLog.i(TAG, false, "%{public}s sendMessage %{public}s", new Object[]{getCurrentState().getName(), HwWifi2ClientModeImplConst.messageNumToString(what)});
    }
}
