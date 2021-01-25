package com.android.server.hidata.wavemapping.dataprovider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import com.android.server.hidata.wavemapping.cons.ContextManager;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.intellicom.common.SmartDualCardConsts;

public class BootBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = ("WMapping." + BootBroadcastReceiver.class.getSimpleName());
    private Handler bootHandler;
    private Context mCtx = ContextManager.getInstance().getContext();

    public BootBroadcastReceiver(Handler handler) {
        this.bootHandler = handler;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        LogUtil.i(false, "Boot this system , BootBroadcastReceiver onReceive()", new Object[0]);
        if (intent == null) {
            LogUtil.e(false, "Intent is null, so exiting", new Object[0]);
            return;
        }
        String action = intent.getAction();
        if (action != null) {
            if (action.equals("android.intent.action.BOOT_COMPLETED") || action.equals("android.intent.action.LOCKED_BOOT_COMPLETED")) {
                LogUtil.d(false, "BootBroadcastReceiver onReceive(), Boot Complete", new Object[0]);
                Message bootMsg = Message.obtain(this.bootHandler, 1);
                Handler handler = this.bootHandler;
                if (handler == null) {
                    LogUtil.w(false, "BootBroadcastReceiver onReceive(), this.bootHandler == null", new Object[0]);
                } else {
                    handler.sendMessage(bootMsg);
                }
            } else if (action.equals(SmartDualCardConsts.SYSTEM_STATE_NAME_ACTION_SHUTDOWN)) {
                LogUtil.d(false, "BootBroadcastReceiver onReceive(), Shut Down", new Object[0]);
                Message bootMsg2 = Message.obtain(this.bootHandler, 2);
                Handler handler2 = this.bootHandler;
                if (handler2 == null) {
                    LogUtil.w(false, "BootBroadcastReceiver onReceive(), this.bootHandler == null", new Object[0]);
                } else {
                    handler2.sendMessage(bootMsg2);
                }
            }
        }
    }
}
