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

public class UserPresentAction extends Action {
    private static final int MESSAGE_USER_PRESENT = 1;
    private static final String TAG = "UserPresentAction";
    private static UserPresentAction sInstance = null;
    /* access modifiers changed from: private */
    public final Object lock = new Object();
    /* access modifiers changed from: private */
    public Handler mHandler = null;
    private UserPresentBroadcastReceiver mReceiver = null;

    private static class MyHandler extends Handler {
        private final WeakReference<UserPresentAction> service;

        MyHandler(UserPresentAction service2) {
            this.service = new WeakReference<>(service2);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                UserPresentAction action = (UserPresentAction) this.service.get();
                if (action != null) {
                    switch (msg.what) {
                        case 1:
                            action.perform();
                            return;
                        default:
                            OPCollectLog.r("UserPresentAction", "wrong msg: " + msg.what);
                            return;
                    }
                }
            }
        }
    }

    private static class UserPresentBroadcastReceiver extends BroadcastReceiver {
        private final WeakReference<UserPresentAction> service;

        UserPresentBroadcastReceiver(UserPresentAction service2) {
            this.service = new WeakReference<>(service2);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                OPCollectLog.r("UserPresentAction", "onReceive: " + action);
                if ("android.intent.action.USER_PRESENT".equalsIgnoreCase(action)) {
                    UserPresentAction userPresentAction = (UserPresentAction) this.service.get();
                    if (userPresentAction != null) {
                        synchronized (userPresentAction.lock) {
                            if (userPresentAction.mHandler != null) {
                                userPresentAction.mHandler.sendEmptyMessage(1);
                            }
                        }
                    }
                }
            }
        }
    }

    public static synchronized UserPresentAction getInstance(Context context) {
        UserPresentAction userPresentAction;
        synchronized (UserPresentAction.class) {
            if (sInstance == null) {
                sInstance = new UserPresentAction("UserPresentAction", context);
            }
            userPresentAction = sInstance;
        }
        return userPresentAction;
    }

    private UserPresentAction(String name, Context context) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_USER_PRESENT));
        OPCollectLog.r("UserPresentAction", "UserPresentAction");
    }

    public void enable() {
        super.enable();
        synchronized (this.lock) {
            this.mHandler = new MyHandler(this);
        }
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new UserPresentBroadcastReceiver(this);
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.USER_PRESENT"), null, OdmfCollectScheduler.getInstance().getRecvHandler());
            OPCollectLog.r("UserPresentAction", "enabled");
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        super.execute();
        OPCollectLog.d("UserPresentAction", "execute");
        SysEventUtil.collectSysEventData(SysEventUtil.EVENT_USER_PRESENT);
        return true;
    }

    public void disable() {
        super.disable();
        if (!(this.mReceiver == null || this.mContext == null)) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
        synchronized (this.lock) {
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
        synchronized (UserPresentAction.class) {
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
