package com.android.server.security.deviceusage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;

public class HwDeviceUsageCollection {
    private static final String ACTION_CALLS_TABLE_ADD_ENTRY = "com.android.server.telecom.intent.action.CALLS_ADD_ENTRY";
    private static final String CALL_DURATION = "duration";
    private static final long CALL_ENOUGH_TIME = 600;
    private static final long CHARGING_ENOUGH_TIME = 5;
    private static final boolean HW_DEBUG;
    private static final long SCREENON_ENOUGH_TIME = 36000;
    private static final String TAG = "HwDeviceUsageCollection";
    public static final int TYPE_GET_TIME = 6;
    public static final int TYPE_HAS_GET_TIME = 7;
    private static final int TYPE_OBTAIN_CALL_LOG = 1;
    private static final int TYPE_OBTAIN_CHARGING = 2;
    private static final int TYPE_OBTAIN_SCREEN_OFF = 4;
    private static final int TYPE_OBTAIN_SCREEN_ON = 3;
    public static final int TYPE_REPORT_TIME = 5;
    private boolean isGetTime = false;
    private Runnable judgeIsDeviceUseRunnable = new Runnable() {
        public void run() {
            if (!HwDeviceUsageCollection.this.isGetTime) {
                boolean isDeviceUsed = (HwDeviceUsageCollection.this.getScreenOnTime() < HwDeviceUsageCollection.SCREENON_ENOUGH_TIME || HwDeviceUsageCollection.this.getChargeTime() < HwDeviceUsageCollection.CHARGING_ENOUGH_TIME) ? false : HwDeviceUsageCollection.this.getTalkTime() >= HwDeviceUsageCollection.CALL_ENOUGH_TIME;
                if (HwDeviceUsageCollection.HW_DEBUG) {
                    Slog.d(HwDeviceUsageCollection.TAG, "isDeviceUsed = " + isDeviceUsed);
                }
                if (isDeviceUsed) {
                    HwDeviceUsageCollection.this.isFirstUseDevice();
                }
            }
        }
    };
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (HwDeviceUsageCollection.HW_DEBUG) {
                Slog.d(HwDeviceUsageCollection.TAG, "action  " + action);
            }
            if ("android.intent.action.ACTION_POWER_CONNECTED".equals(action)) {
                HwDeviceUsageCollection.this.obtainCharging();
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                HwDeviceUsageCollection.this.obtainScreenOff();
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                HwDeviceUsageCollection.this.obtainScreenOn();
                if (HwDeviceUsageCollection.HW_DEBUG) {
                    Slog.d(HwDeviceUsageCollection.TAG, "mScreenOnTime  action " + HwDeviceUsageCollection.this.mScreenOnTime);
                }
            } else if (!HwDeviceUsageCollection.ACTION_CALLS_TABLE_ADD_ENTRY.equals(action)) {
                Slog.e(HwDeviceUsageCollection.TAG, "Receive error broadcast");
            } else if (HwDeviceUsageCollection.this.mTelephonyManager != null && HwDeviceUsageCollection.this.mTelephonyManager.getSimState() != 1) {
                HwDeviceUsageCollection.this.obtainCallLog(intent.getLongExtra(HwDeviceUsageCollection.CALL_DURATION, 0));
            }
        }
    };
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private HwDeviceUsageOEMINFO mHwDeviceUsageOEMINFO;
    private long mScreenOnTime = -1;
    private TelephonyManager mTelephonyManager;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HW_DEBUG = isLoggable;
    }

    public HwDeviceUsageCollection(Context context) {
        if (HW_DEBUG) {
            Slog.d(TAG, TAG);
        }
        this.mContext = context;
        this.mHwDeviceUsageOEMINFO = HwDeviceUsageOEMINFO.getInstance();
    }

    public void onStart() {
        if (HW_DEBUG) {
            Slog.d(TAG, "onStart");
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
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (msg.obj != null) {
                            HwDeviceUsageCollection.this.handleCallLog(((Long) msg.obj).longValue());
                            break;
                        }
                        return;
                    case 2:
                        HwDeviceUsageCollection.this.handleCharging();
                        break;
                    case 3:
                        HwDeviceUsageCollection.this.handleScreenOn();
                        break;
                    case 4:
                        HwDeviceUsageCollection.this.handleScreenOff();
                        break;
                    case 7:
                        if (msg.obj != null) {
                            HwDeviceUsageCollection.this.handleHasGetTime(((Long) msg.obj).longValue());
                            break;
                        }
                        return;
                    default:
                        Slog.e(HwDeviceUsageCollection.TAG, "obtain error message");
                        break;
                }
            }
        };
        this.mHandler.post(this.judgeIsDeviceUseRunnable);
    }

    private void obtainCallLog(long duration) {
        if (duration > 0) {
            Message msg = Message.obtain();
            msg.what = 1;
            msg.obj = Long.valueOf(duration / 1000);
            this.mHandler.sendMessage(msg);
        }
    }

    private void obtainCharging() {
        Message msg = Message.obtain();
        msg.what = 2;
        this.mHandler.sendMessage(msg);
    }

    private void obtainScreenOn() {
        Message msg = Message.obtain();
        msg.what = 3;
        this.mHandler.sendMessage(msg);
    }

    private void obtainScreenOff() {
        Message msg = Message.obtain();
        msg.what = 4;
        this.mHandler.sendMessage(msg);
    }

    private boolean isTimeNull() {
        return getFristUseTime() == 0;
    }

    private void handleCharging() {
        long mChargeTime = getChargeTime() + 1;
        if (HW_DEBUG) {
            Slog.d(TAG, "mChargeTime " + mChargeTime);
        }
        setChargeTime(mChargeTime);
        this.mHandler.post(this.judgeIsDeviceUseRunnable);
    }

    private void handleScreenOn() {
        this.mScreenOnTime = SystemClock.elapsedRealtime();
    }

    private void handleScreenOff() {
        if (this.mScreenOnTime >= 0) {
            long onTime = (SystemClock.elapsedRealtime() - this.mScreenOnTime) / 1000;
            if (HW_DEBUG) {
                Slog.d(TAG, "mOnTime " + onTime);
            }
            this.mScreenOnTime = -1;
            if (onTime > 0) {
                long screenOnTime = onTime + getScreenOnTime();
                if (HW_DEBUG) {
                    Slog.d(TAG, "screenOnTime " + screenOnTime);
                }
                setScreenOnTime(screenOnTime);
                this.mHandler.post(this.judgeIsDeviceUseRunnable);
            }
        }
    }

    private void handleCallLog(long talkTime) {
        long mTalkTime = talkTime + getTalkTime();
        if (HW_DEBUG) {
            Slog.d(TAG, "mTalkTime " + mTalkTime);
        }
        setTalkTime(mTalkTime);
        this.mHandler.post(this.judgeIsDeviceUseRunnable);
    }

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
        if (!this.isGetTime) {
            this.isGetTime = true;
            HwDeviceFirstUseTime mHwDeviceFirstUseTime = new HwDeviceFirstUseTime(this.mContext, this.mHandler);
            mHwDeviceFirstUseTime.start();
            mHwDeviceFirstUseTime.triggerGetFirstUseTime();
        }
    }

    private void handleHasGetTime(long time) {
        if (setFristUseTime(time) == 1) {
            reportTime(time);
        }
    }

    protected boolean getOpenFlag() {
        return this.mHwDeviceUsageOEMINFO.getOpenFlag();
    }

    protected long getScreenOnTime() {
        return this.mHwDeviceUsageOEMINFO.getScreenOnTime();
    }

    protected long getChargeTime() {
        return this.mHwDeviceUsageOEMINFO.getChargeTime();
    }

    protected long getTalkTime() {
        return this.mHwDeviceUsageOEMINFO.getTalkTime();
    }

    protected long getFristUseTime() {
        return this.mHwDeviceUsageOEMINFO.getFristUseTime();
    }

    protected void setOpenFlag(int flag) {
        this.mHwDeviceUsageOEMINFO.setOpenFlag(flag);
    }

    protected void setScreenOnTime(long time) {
        this.mHwDeviceUsageOEMINFO.setScreenOnTime(time);
    }

    protected void setChargeTime(long time) {
        this.mHwDeviceUsageOEMINFO.setChargeTime(time);
    }

    protected void setTalkTime(long time) {
        this.mHwDeviceUsageOEMINFO.setTalkTime(time);
    }

    protected int setFristUseTime(long time) {
        return this.mHwDeviceUsageOEMINFO.setFristUseTime(time);
    }
}
