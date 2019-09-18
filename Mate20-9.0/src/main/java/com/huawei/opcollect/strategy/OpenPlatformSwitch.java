package com.huawei.opcollect.strategy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.utils.OPCollectLog;

public class OpenPlatformSwitch {
    private static final long ANTI_PINGPONG_DELAYTIME = 1000;
    private static final String TAG = "OpenPlatformSwitch";
    private static OpenPlatformSwitch mInstance = null;
    private Context mContext;
    private BroadcastReceiver mReceiver = null;

    public static synchronized OpenPlatformSwitch getInstance() {
        OpenPlatformSwitch openPlatformSwitch;
        synchronized (OpenPlatformSwitch.class) {
            if (mInstance == null) {
                mInstance = new OpenPlatformSwitch();
            }
            openPlatformSwitch = mInstance;
        }
        return openPlatformSwitch;
    }

    public void initialize(Context context) {
        this.mContext = context;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    OPCollectLog.i(OpenPlatformSwitch.TAG, "action: " + action);
                    if ("android.intent.action.SCREEN_ON".equalsIgnoreCase(action)) {
                        OdmfCollectScheduler.getInstance().getCtrlHandler().removeMessages(6);
                        OdmfCollectScheduler.getInstance().getCtrlHandler().sendEmptyMessage(6);
                    } else if ("android.intent.action.SCREEN_OFF".equalsIgnoreCase(action)) {
                        OdmfCollectScheduler.getInstance().getCtrlHandler().removeMessages(7);
                        OdmfCollectScheduler.getInstance().getCtrlHandler().sendEmptyMessage(7);
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        this.mContext.registerReceiver(this.mReceiver, intentFilter, null, OdmfCollectScheduler.getInstance().getRecvHandler());
    }

    public boolean getSwitchState() {
        return true;
    }
}
