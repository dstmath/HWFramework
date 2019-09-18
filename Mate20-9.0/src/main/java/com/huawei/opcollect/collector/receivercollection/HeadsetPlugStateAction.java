package com.huawei.opcollect.collector.receivercollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class HeadsetPlugStateAction extends Action {
    private static final int HEADSET_PLUGGED = 1;
    private static final int HEADSET_UNPLUGGED = 0;
    private static final Object LOCK = new Object();
    private static final String TAG = "HeadsetPlugStateAction";
    private static HeadsetPlugStateAction sInstance = null;
    private HeadSetStateReceiver mReceiver = null;
    /* access modifiers changed from: private */
    public int mState = -1;

    class HeadSetStateReceiver extends BroadcastReceiver {
        HeadSetStateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    OPCollectLog.r("HeadsetPlugStateAction", "onReceive action: " + action);
                    if ("android.intent.action.HEADSET_PLUG".equals(action) && intent.hasExtra("state")) {
                        int unused = HeadsetPlugStateAction.this.mState = intent.getIntExtra("state", -1);
                        HeadsetPlugStateAction.this.perform();
                    }
                }
            }
        }
    }

    public static HeadsetPlugStateAction getInstance(Context context) {
        HeadsetPlugStateAction headsetPlugStateAction;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new HeadsetPlugStateAction(context, "HeadsetPlugStateAction");
            }
            headsetPlugStateAction = sInstance;
        }
        return headsetPlugStateAction;
    }

    private HeadsetPlugStateAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_HEADSET_PLUG) + SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_HEADSET_UNPLUG));
        OPCollectLog.r("HeadsetPlugStateAction", "HeadsetPlugStateAction");
    }

    public void enable() {
        super.enable();
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new HeadSetStateReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.HEADSET_PLUG");
            this.mContext.registerReceiver(this.mReceiver, intentFilter, null, OdmfCollectScheduler.getInstance().getCtrlHandler());
            SysEventUtil.collectKVSysEventData("sound/headset_connect_status", SysEventUtil.HEADSET_CONNECT_STATUS, SysEventUtil.OFF);
            OPCollectLog.r("HeadsetPlugStateAction", "enabled");
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        if (1 == this.mState) {
            SysEventUtil.collectSysEventData(SysEventUtil.EVENT_HEADSET_PLUG);
            SysEventUtil.collectKVSysEventData("sound/headset_connect_status", SysEventUtil.HEADSET_CONNECT_STATUS, SysEventUtil.ON);
        } else if (this.mState == 0) {
            SysEventUtil.collectSysEventData(SysEventUtil.EVENT_HEADSET_UNPLUG);
            SysEventUtil.collectKVSysEventData("sound/headset_connect_status", SysEventUtil.HEADSET_CONNECT_STATUS, SysEventUtil.OFF);
        }
        this.mState = -1;
        return true;
    }

    public boolean perform() {
        return super.perform();
    }

    public void disable() {
        super.disable();
        if (this.mReceiver != null && this.mContext != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
    }

    public boolean destroy() {
        super.destroy();
        destroyInstance();
        return true;
    }

    private static void destroyInstance() {
        synchronized (LOCK) {
            sInstance = null;
        }
    }

    public void dump(int indentNum, PrintWriter pw) {
        super.dump(indentNum, pw);
        if (pw != null) {
            String indent = String.format("%" + indentNum + "s\\-", new Object[]{" "});
            if (this.mReceiver == null) {
                pw.println(indent + "receiver is null");
            } else {
                pw.println(indent + "receiver not null");
            }
        }
    }
}
