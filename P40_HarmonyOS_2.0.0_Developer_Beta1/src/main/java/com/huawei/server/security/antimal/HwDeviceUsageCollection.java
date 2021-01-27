package com.huawei.server.security.antimal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.util.LogEx;

public class HwDeviceUsageCollection {
    private static final String ACTION_CALLS_TABLE_ADD_ENTRY = "com.android.server.telecom.intent.action.CALLS_ADD_ENTRY";
    private static final String CALL_DURATION = "duration";
    private static final long CALL_ENOUGH_TIME = 600;
    private static final long CHARGING_ENOUGH_TIME = 5;
    private static final long INVALID_TIME = -1;
    private static final boolean IS_HW_DEBUG = (LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(TAG, 4)));
    private static final long MILLIS_OF_ONE_SECOND = 1000;
    private static final long SCREEN_ON_ENOUGH_TIME = 36000;
    private static final int SET_TIME_SUCCESS = 1;
    private static final String TAG = "HwDeviceUsageCollection";
    private static final int TYPE_HAS_GET_TIME = 7;
    private static final int TYPE_OBTAIN_CALL_LOG = 1;
    private static final int TYPE_OBTAIN_CHARGING = 2;
    private static final int TYPE_OBTAIN_SCREEN_OFF = 4;
    private static final int TYPE_OBTAIN_SCREEN_ON = 3;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.security.antimal.HwDeviceUsageCollection.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || TextUtils.isEmpty(intent.getAction())) {
                Log.e(HwDeviceUsageCollection.TAG, "onReceive action is null");
                return;
            }
            String action = intent.getAction();
            if (HwDeviceUsageCollection.IS_HW_DEBUG) {
                Log.d(HwDeviceUsageCollection.TAG, "action " + action);
            }
            char c = 65535;
            switch (action.hashCode()) {
                case -2128145023:
                    if (action.equals("android.intent.action.SCREEN_OFF")) {
                        c = 1;
                        break;
                    }
                    break;
                case -1454123155:
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1019184907:
                    if (action.equals("android.intent.action.ACTION_POWER_CONNECTED")) {
                        c = 0;
                        break;
                    }
                    break;
                case 1280405454:
                    if (action.equals(HwDeviceUsageCollection.ACTION_CALLS_TABLE_ADD_ENTRY)) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                HwDeviceUsageCollection.this.obtainCharging();
            } else if (c == 1) {
                HwDeviceUsageCollection.this.obtainScreenOff();
            } else if (c == 2) {
                HwDeviceUsageCollection.this.obtainScreenOn();
                if (HwDeviceUsageCollection.IS_HW_DEBUG) {
                    Log.d(HwDeviceUsageCollection.TAG, "mScreenOnTime action " + HwDeviceUsageCollection.this.mScreenOnTime);
                }
            } else if (c != 3) {
                Log.w(HwDeviceUsageCollection.TAG, "Receive error broadcast");
            } else if (HwDeviceUsageCollection.this.mTelephonyManager != null && HwDeviceUsageCollection.this.mTelephonyManager.getSimState() != 1) {
                HwDeviceUsageCollection.this.obtainCallLog(intent.getLongExtra(HwDeviceUsageCollection.CALL_DURATION, 0));
            }
        }
    };
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private HwDeviceUsageOEMINFO mHwDeviceUsageOemInfo;
    private boolean mIsGetTime = false;
    private Runnable mJudgeIsDeviceUseRunnable = new Runnable() {
        /* class com.huawei.server.security.antimal.HwDeviceUsageCollection.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            if (!HwDeviceUsageCollection.this.mIsGetTime) {
                boolean isDeviceUsed = HwDeviceUsageCollection.this.getScreenOnTime() >= HwDeviceUsageCollection.SCREEN_ON_ENOUGH_TIME && HwDeviceUsageCollection.this.getChargeTime() >= HwDeviceUsageCollection.CHARGING_ENOUGH_TIME && HwDeviceUsageCollection.this.getTalkTime() >= HwDeviceUsageCollection.CALL_ENOUGH_TIME;
                if (HwDeviceUsageCollection.IS_HW_DEBUG) {
                    Log.d(HwDeviceUsageCollection.TAG, "isDeviceUsed = " + isDeviceUsed);
                }
                if (isDeviceUsed) {
                    HwDeviceUsageCollection.this.isFirstUseDevice();
                }
            }
        }
    };
    private long mScreenOnTime = -1;
    private TelephonyManager mTelephonyManager;

    public HwDeviceUsageCollection(Context context) {
        if (IS_HW_DEBUG) {
            Log.d(TAG, TAG);
        }
        this.mContext = context;
        this.mHwDeviceUsageOemInfo = HwDeviceUsageOEMINFO.getInstance();
    }

    public void onStart() {
        if (IS_HW_DEBUG) {
            Log.d(TAG, "onStart");
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        intentFilter.addAction(ACTION_CALLS_TABLE_ADD_ENTRY);
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mHandlerThread = new HandlerThread("HwDeviceUsageCollectionThread");
        this.mHandlerThread.start();
        this.mHandler = new DeviceUsageHandler(this.mHandlerThread.getLooper());
        this.mHandler.post(this.mJudgeIsDeviceUseRunnable);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void obtainCallLog(long duration) {
        if (duration > 0) {
            Message msg = Message.obtain();
            msg.what = 1;
            msg.obj = Long.valueOf(duration / MILLIS_OF_ONE_SECOND);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void obtainCharging() {
        Message msg = Message.obtain();
        msg.what = 2;
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void obtainScreenOn() {
        Message msg = Message.obtain();
        msg.what = 3;
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void obtainScreenOff() {
        Message msg = Message.obtain();
        msg.what = 4;
        this.mHandler.sendMessage(msg);
    }

    private boolean isTimeNull() {
        return getFirstUseTime() == 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCharging() {
        long mChargeTime = getChargeTime() + 1;
        if (IS_HW_DEBUG) {
            Log.d(TAG, "mChargeTime " + mChargeTime);
        }
        setChargeTime(mChargeTime);
        this.mHandler.post(this.mJudgeIsDeviceUseRunnable);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOn() {
        this.mScreenOnTime = SystemClock.elapsedRealtime();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOff() {
        if (this.mScreenOnTime >= 0) {
            long onTime = (SystemClock.elapsedRealtime() - this.mScreenOnTime) / MILLIS_OF_ONE_SECOND;
            if (IS_HW_DEBUG) {
                Log.d(TAG, "mOnTime " + onTime);
            }
            this.mScreenOnTime = -1;
            if (onTime > 0) {
                long screenOnTime = getScreenOnTime() + onTime;
                if (IS_HW_DEBUG) {
                    Log.d(TAG, "screenOnTime " + screenOnTime);
                }
                setScreenOnTime(screenOnTime);
                this.mHandler.post(this.mJudgeIsDeviceUseRunnable);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCallLog(long talkTime) {
        long mTalkTime = getTalkTime() + talkTime;
        if (IS_HW_DEBUG) {
            Log.d(TAG, "mTalkTime " + mTalkTime);
        }
        setTalkTime(mTalkTime);
        this.mHandler.post(this.mJudgeIsDeviceUseRunnable);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void isFirstUseDevice() {
        if (isTimeNull()) {
            getTime();
        }
    }

    private void reportTime(long time) {
        HwDeviceUsageReport mHwDeviceUsageReport = new HwDeviceUsageReport(this.mContext);
        if (!isTimeNull()) {
            mHwDeviceUsageReport.reportFirstUseTime(time);
        }
    }

    private void getTime() {
        if (!this.mIsGetTime) {
            this.mIsGetTime = true;
            HwDeviceFirstUseTime mHwDeviceFirstUseTime = new HwDeviceFirstUseTime(this.mContext, this.mHandler);
            mHwDeviceFirstUseTime.start();
            mHwDeviceFirstUseTime.triggerGetFirstUseTime();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHasGetTime(long time) {
        if (setFirstUseTime(time) == 1) {
            reportTime(time);
        }
    }

    public boolean isOpenFlagSet() {
        return this.mHwDeviceUsageOemInfo.isOpenFlagSet();
    }

    public long getScreenOnTime() {
        return this.mHwDeviceUsageOemInfo.getScreenOnTime();
    }

    public long getChargeTime() {
        return this.mHwDeviceUsageOemInfo.getChargeTime();
    }

    public long getTalkTime() {
        return this.mHwDeviceUsageOemInfo.getTalkTime();
    }

    public long getFirstUseTime() {
        return this.mHwDeviceUsageOemInfo.getFirstUseTime();
    }

    public void setOpenFlag(int flag) {
        this.mHwDeviceUsageOemInfo.setOpenFlag(flag);
    }

    public void setScreenOnTime(long time) {
        this.mHwDeviceUsageOemInfo.setScreenOnTime(time);
    }

    public void setChargeTime(long time) {
        this.mHwDeviceUsageOemInfo.setChargeTime(time);
    }

    public void setTalkTime(long time) {
        this.mHwDeviceUsageOemInfo.setTalkTime(time);
    }

    public int setFirstUseTime(long time) {
        return this.mHwDeviceUsageOemInfo.setFirstUseTime(time);
    }

    /* access modifiers changed from: private */
    public class DeviceUsageHandler extends Handler {
        DeviceUsageHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 1) {
                if (i == 2) {
                    HwDeviceUsageCollection.this.handleCharging();
                } else if (i == 3) {
                    HwDeviceUsageCollection.this.handleScreenOn();
                } else if (i == 4) {
                    HwDeviceUsageCollection.this.handleScreenOff();
                } else if (i != HwDeviceUsageCollection.TYPE_HAS_GET_TIME) {
                    Log.w(HwDeviceUsageCollection.TAG, "obtain error message");
                } else if (msg.obj instanceof Long) {
                    HwDeviceUsageCollection.this.handleHasGetTime(((Long) msg.obj).longValue());
                }
            } else if (msg.obj instanceof Long) {
                HwDeviceUsageCollection.this.handleCallLog(((Long) msg.obj).longValue());
            }
        }
    }
}
