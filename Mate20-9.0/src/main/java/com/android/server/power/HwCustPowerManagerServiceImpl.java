package com.android.server.power;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Log;

public class HwCustPowerManagerServiceImpl extends HwCustPowerManagerService {
    static final int C2DM_ACQUIRE_EVENT = 1;
    static final int C2DM_TIMEOUT_EVENT = 2;
    protected static final boolean HWDBG = ((Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3))) ? HWLOGW_E : false);
    protected static final boolean HWLOGW_E = true;
    private static final String TAG = "HwCustPowerManager";
    /* access modifiers changed from: private */
    public int C2DM_DELAY_TIMEOUT;
    private HandlerThread mC2DMHandlerThread;
    private C2DMHelperHandler mC2DMHelperHandler;
    /* access modifiers changed from: private */
    public PowerManager.WakeLock mC2DMWakeLock;
    private boolean mEnableC2DMDelay = SystemProperties.getBoolean("ro.config.enable_c2dm_delay", false);

    public class C2DMHelperHandler extends Handler {
        public C2DMHelperHandler(Looper looper) {
            super(looper, null, HwCustPowerManagerServiceImpl.HWLOGW_E);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (HwCustPowerManagerServiceImpl.HWDBG) {
                        Log.d(HwCustPowerManagerServiceImpl.TAG, "handleMessage C2DM_ACQUIRE_EVENT, mC2DMWakeLock.acquire!");
                    }
                    if (!HwCustPowerManagerServiceImpl.this.mC2DMWakeLock.isHeld()) {
                        if (HwCustPowerManagerServiceImpl.HWDBG) {
                            Log.d(HwCustPowerManagerServiceImpl.TAG, "mC2DMWakeLock has not been hold!");
                        }
                        HwCustPowerManagerServiceImpl.this.mC2DMWakeLock.acquire();
                    }
                    removeMessages(HwCustPowerManagerServiceImpl.C2DM_TIMEOUT_EVENT);
                    sendMessageDelayed(obtainMessage(HwCustPowerManagerServiceImpl.C2DM_TIMEOUT_EVENT), (long) HwCustPowerManagerServiceImpl.this.C2DM_DELAY_TIMEOUT);
                    return;
                case HwCustPowerManagerServiceImpl.C2DM_TIMEOUT_EVENT /*2*/:
                    HwCustPowerManagerServiceImpl.this.mC2DMWakeLock.release();
                    if (HwCustPowerManagerServiceImpl.HWDBG) {
                        Log.d(HwCustPowerManagerServiceImpl.TAG, "handleMessage C2DM_TIMEOUT_EVENT, mC2DMWakeLock.release!");
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public void init(Context context) {
        if (context != null) {
            this.mC2DMHandlerThread = new HandlerThread("C2DMHandlerThread");
            this.mC2DMHandlerThread.start();
            this.mC2DMHelperHandler = new C2DMHelperHandler(this.mC2DMHandlerThread.getLooper());
            PowerManager pm = (PowerManager) context.getSystemService("power");
            if (pm != null) {
                this.mC2DMWakeLock = pm.newWakeLock(1, "C2DMHandlerThread");
            }
            this.C2DM_DELAY_TIMEOUT = SystemProperties.getInt("ro.config.c2dmdelay", 10000);
            if (HWDBG) {
                Log.d(TAG, "c2dm delay time " + this.C2DM_DELAY_TIMEOUT + "ms");
            }
        }
    }

    public boolean isDelayEnanbled() {
        return this.mEnableC2DMDelay;
    }

    public void checkDelay(String tagName) {
        if ("google_c2dm".compareToIgnoreCase(tagName) == 0) {
            if (this.mC2DMWakeLock == null) {
                Log.e(TAG, "mC2DMWakeLock = null, mC2DMWakeLock is null!");
            } else if (this.mC2DMHelperHandler == null) {
                Log.e(TAG, "mC2DMHelperHandler = null, init failed!");
            } else {
                this.mC2DMHelperHandler.sendMessage(this.mC2DMHelperHandler.obtainMessage(1));
            }
        }
    }
}
