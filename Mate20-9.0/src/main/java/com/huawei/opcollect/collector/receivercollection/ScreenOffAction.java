package com.huawei.opcollect.collector.receivercollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;

public class ScreenOffAction extends Action {
    private static final int MESSAGE_SCREEN_OFF = 1;
    private static final String TAG = "ScreenOffAction";
    private static ScreenOffAction sInstance = null;
    /* access modifiers changed from: private */
    public Handler mHandler = null;
    private ScreenOffBroadcastReceiver mReceiver = null;

    private static class MyHandler extends Handler {
        private final WeakReference<ScreenOffAction> service;

        MyHandler(ScreenOffAction service2) {
            this.service = new WeakReference<>(service2);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                ScreenOffAction action = (ScreenOffAction) this.service.get();
                if (action != null) {
                    switch (msg.what) {
                        case 1:
                            action.perform();
                            return;
                        default:
                            OPCollectLog.r("ScreenOffAction", "wrong msg: " + msg.what);
                            return;
                    }
                }
            }
        }
    }

    class ScreenOffBroadcastReceiver extends BroadcastReceiver {
        ScreenOffBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                OPCollectLog.r("ScreenOffAction", "onReceive: " + intent.getAction());
                if ("android.intent.action.SCREEN_OFF".equalsIgnoreCase(intent.getAction())) {
                    synchronized (ScreenOffAction.this) {
                        if (ScreenOffAction.this.mHandler != null) {
                            ScreenOffAction.this.mHandler.sendEmptyMessage(1);
                        }
                    }
                }
            }
        }
    }

    public static synchronized ScreenOffAction getInstance(Context context) {
        ScreenOffAction screenOffAction;
        synchronized (ScreenOffAction.class) {
            if (sInstance == null) {
                sInstance = new ScreenOffAction("ScreenOffAction", context);
            }
            screenOffAction = sInstance;
        }
        return screenOffAction;
    }

    private ScreenOffAction(String name, Context context) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_SCREEN_OFF));
        OPCollectLog.r("ScreenOffAction", "ScreenOffAction");
    }

    public void enable() {
        super.enable();
        synchronized (this) {
            this.mHandler = new MyHandler(this);
        }
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new ScreenOffBroadcastReceiver();
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.SCREEN_OFF"), null, OdmfCollectScheduler.getInstance().getRecvHandler());
            OPCollectLog.r("ScreenOffAction", "enabled");
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        super.execute();
        OPCollectLog.d("ScreenOffAction", "EVENT_SCREEN_OFF");
        SysEventUtil.collectSysEventData(SysEventUtil.EVENT_SCREEN_OFF);
        return true;
    }

    public void disable() {
        super.disable();
        if (!(this.mReceiver == null || this.mContext == null)) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
        synchronized (this) {
            if (this.mHandler != null) {
                this.mHandler.removeMessages(1);
                this.mHandler = null;
            }
        }
    }

    public boolean destroy() {
        super.destroy();
        destroyInstance();
        return true;
    }

    private static synchronized void destroyInstance() {
        synchronized (ScreenOffAction.class) {
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
