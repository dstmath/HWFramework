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

public class ScreenOnAction extends Action {
    private static final int MESSAGE_SCREEN_ON = 1;
    private static final String TAG = "ScreenOnAction";
    private static ScreenOnAction sInstance = null;
    /* access modifiers changed from: private */
    public Handler mHandler = null;
    private ScreenOnBroadcastReceiver mReceiver = null;

    private static class MyHandler extends Handler {
        private final WeakReference<ScreenOnAction> service;

        MyHandler(ScreenOnAction service2) {
            this.service = new WeakReference<>(service2);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                ScreenOnAction action = (ScreenOnAction) this.service.get();
                if (action != null) {
                    switch (msg.what) {
                        case 1:
                            action.perform();
                            return;
                        default:
                            OPCollectLog.r("ScreenOnAction", "wrong msg: " + msg.what);
                            return;
                    }
                }
            }
        }
    }

    class ScreenOnBroadcastReceiver extends BroadcastReceiver {
        ScreenOnBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                OPCollectLog.r("ScreenOnAction", "onReceive: " + action);
                if ("android.intent.action.SCREEN_ON".equalsIgnoreCase(action)) {
                    synchronized (ScreenOnAction.this) {
                        if (ScreenOnAction.this.mHandler != null) {
                            ScreenOnAction.this.mHandler.sendEmptyMessage(1);
                        }
                    }
                }
            }
        }
    }

    public static synchronized ScreenOnAction getInstance(Context context) {
        ScreenOnAction screenOnAction;
        synchronized (ScreenOnAction.class) {
            if (sInstance == null) {
                sInstance = new ScreenOnAction("ScreenOnAction", context);
            }
            screenOnAction = sInstance;
        }
        return screenOnAction;
    }

    private ScreenOnAction(String name, Context context) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_SCREEN_ON));
        OPCollectLog.r("ScreenOnAction", "ScreenOnAction");
    }

    public void enable() {
        super.enable();
        synchronized (this) {
            this.mHandler = new MyHandler(this);
        }
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new ScreenOnBroadcastReceiver();
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.SCREEN_ON"), null, OdmfCollectScheduler.getInstance().getRecvHandler());
            OPCollectLog.r("ScreenOnAction", "enabled");
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        super.execute();
        OPCollectLog.d("ScreenOnAction", "EVENT_SCREEN_ON");
        SysEventUtil.collectSysEventData(SysEventUtil.EVENT_SCREEN_ON);
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
        synchronized (ScreenOnAction.class) {
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
