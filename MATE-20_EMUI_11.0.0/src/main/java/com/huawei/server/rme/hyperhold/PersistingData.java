package com.huawei.server.rme.hyperhold;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;
import com.android.server.ServiceThread;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.huawei.server.rme.collector.ResourceCollector;

public class PersistingData {
    private static final int BOOT_MSG = 1;
    private static final int FREQUENT_UPDATE_DELAY = 600000;
    private static final int FREQUENT_UPDATE_MSG = 2;
    private static final long INITIAL_TIME = 0;
    private static final long INVALID_TIME = -1;
    private static final int POWER_OFF_MSG = 4;
    private static final int RARE_UPDATE_DELAY = 86400000;
    private static final int RARE_UPDATE_MSG = 3;
    private static final String TAG = "SWAPPersistingData";
    private static final int UPDATE_ON_BOOT = 1;
    private static final int UPDATE_ON_DAY_CHANGE = 3;
    private static final int UPDATE_ON_POWER_OFF = 2;
    private static final int UPDATE_ON_TIMER = 0;
    private static volatile PersistingData persistingDataInstance = null;
    private Context context;
    private BroadcastReceiver powerOffReceiver = new PowerOffReceiver();
    private long timeOnCache = 0;
    private UpdateEventHandler updateEventHandler = null;
    private ServiceThread updateEventThread = null;

    /* access modifiers changed from: private */
    public final class UpdateEventHandler extends Handler {
        UpdateEventHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                PersistingData.this.handleBootMsg();
            } else if (i == 2) {
                PersistingData.this.handleFrequentUpdateMsg();
            } else if (i == 3) {
                PersistingData.this.handleRareUpdateMsg();
            } else if (i != 4) {
                Slog.e(PersistingData.TAG, "invalid message!");
            } else {
                PersistingData.this.handlePowerOffMsg();
            }
        }
    }

    private PersistingData() {
        if (this.updateEventHandler == null) {
            this.updateEventThread = new ServiceThread("SWAPPersistingData:update", 0, true);
            this.updateEventThread.start();
            this.updateEventHandler = new UpdateEventHandler(this.updateEventThread.getLooper());
        }
        Slog.i(TAG, "PersistingData module created");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBootMsg() {
        Slog.i(TAG, "handle boot");
        updateTimeOn(1);
        Message freqMsg = this.updateEventHandler.obtainMessage();
        freqMsg.what = 2;
        this.updateEventHandler.sendMessageDelayed(freqMsg, Constant.MAX_TRAIN_MODEL_TIME);
        Message rareMsg = this.updateEventHandler.obtainMessage();
        rareMsg.what = 3;
        this.updateEventHandler.sendMessageDelayed(rareMsg, 86400000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFrequentUpdateMsg() {
        Slog.i(TAG, "handle frequent update");
        updateTimeOn(0);
        Message freqMsg = this.updateEventHandler.obtainMessage();
        freqMsg.what = 2;
        this.updateEventHandler.sendMessageDelayed(freqMsg, Constant.MAX_TRAIN_MODEL_TIME);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRareUpdateMsg() {
        Slog.i(TAG, "handle rare update");
        updateTimeOn(3);
        Message rareMsg = this.updateEventHandler.obtainMessage();
        rareMsg.what = 3;
        this.updateEventHandler.sendMessageDelayed(rareMsg, 86400000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePowerOffMsg() {
        Slog.i(TAG, "handle power off");
        updateTimeOn(2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPowerOff() {
        Message powerOffMsg = this.updateEventHandler.obtainMessage();
        powerOffMsg.what = 4;
        this.updateEventHandler.sendMessage(powerOffMsg);
    }

    private class PowerOffReceiver extends BroadcastReceiver {
        private PowerOffReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || context == null) {
                Slog.e(PersistingData.TAG, "Invalid onReceive params");
                return;
            }
            String action = intent.getAction();
            if (action != null) {
                if (SmartDualCardConsts.SYSTEM_STATE_NAME_ACTION_SHUTDOWN.equals(action) || "android.intent.action.REBOOT".equals(action)) {
                    Slog.i(PersistingData.TAG, "broadcast: " + action);
                    PersistingData.this.onPowerOff();
                    return;
                }
                Slog.e(PersistingData.TAG, "unexpected broadcast: " + action);
            }
        }
    }

    private void registerPowerOffReceiver() {
        IntentFilter powerStatus = new IntentFilter();
        powerStatus.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_ACTION_SHUTDOWN);
        powerStatus.addAction("android.intent.action.REBOOT");
        this.context.registerReceiver(this.powerOffReceiver, powerStatus);
    }

    private void unregisterPowerOffReceiver() {
        BroadcastReceiver broadcastReceiver = this.powerOffReceiver;
        if (broadcastReceiver != null) {
            this.context.unregisterReceiver(broadcastReceiver);
        }
    }

    private void updateTimeOn(int updateReason) {
        this.timeOnCache = ResourceCollector.updateTimeOn(updateReason);
        Slog.i(TAG, "updated timeOn:  " + this.timeOnCache);
    }

    public static PersistingData getInstance() {
        if (persistingDataInstance == null) {
            synchronized (PersistingData.class) {
                if (persistingDataInstance == null) {
                    persistingDataInstance = new PersistingData();
                }
            }
        }
        Slog.i(TAG, "PersistingData getinstance called");
        return persistingDataInstance;
    }

    public void init(Context context2) {
        Slog.i(TAG, "PersistingData init");
        Message msg = this.updateEventHandler.obtainMessage();
        msg.what = 1;
        this.updateEventHandler.sendMessage(msg);
        this.context = context2;
        registerPowerOffReceiver();
    }

    public long getTimeOn() {
        Slog.i(TAG, "getTimeOn:  " + this.timeOnCache);
        return this.timeOnCache;
    }
}
