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
    private static final int C2DM_ACQUIRE_EVENT = 1;
    private static final int C2DM_TIMEOUT_EVENT = 2;
    private static final int DEFALUT_C2DM_DELAY_TIMEOUT = 10000;
    private static final boolean HWDBG = ((Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3))) ? HWLOGW_E : false);
    private static final boolean HWLOGW_E = true;
    private static final String TAG = "HwCustPowerManager";
    private int c2dmDelayTimeOut;
    private HandlerThread mC2DMHandlerThread;
    private C2DMHelperHandler mC2DMHelperHandler;
    private PowerManager.WakeLock mC2DMWakeLock;
    private boolean mIsEnableC2DMDelay = SystemProperties.getBoolean("ro.config.enable_c2dm_delay", false);

    public void init(Context context) {
        if (context != null) {
            this.mC2DMHandlerThread = new HandlerThread("C2DMHandlerThread");
            this.mC2DMHandlerThread.start();
            this.mC2DMHelperHandler = new C2DMHelperHandler(this.mC2DMHandlerThread.getLooper());
            PowerManager pm = (PowerManager) context.getSystemService("power");
            if (pm != null) {
                this.mC2DMWakeLock = pm.newWakeLock(C2DM_ACQUIRE_EVENT, "C2DMHandlerThread");
            }
            this.c2dmDelayTimeOut = SystemProperties.getInt("ro.config.c2dmdelay", (int) DEFALUT_C2DM_DELAY_TIMEOUT);
            if (HWDBG) {
                Log.d(TAG, "c2dm delay time " + this.c2dmDelayTimeOut + "ms");
            }
        }
    }

    public boolean isDelayEnanbled() {
        return this.mIsEnableC2DMDelay;
    }

    public void checkDelay(String tagName) {
        if (tagName != null && "google_c2dm".compareToIgnoreCase(tagName) == 0) {
            if (this.mC2DMWakeLock == null) {
                Log.e(TAG, "mC2DMWakeLock = null, mC2DMWakeLock is null!");
                return;
            }
            C2DMHelperHandler c2DMHelperHandler = this.mC2DMHelperHandler;
            if (c2DMHelperHandler == null) {
                Log.e(TAG, "mC2DMHelperHandler = null, init failed!");
                return;
            }
            this.mC2DMHelperHandler.sendMessage(c2DMHelperHandler.obtainMessage(C2DM_ACQUIRE_EVENT));
        }
    }

    public class C2DMHelperHandler extends Handler {
        public C2DMHelperHandler(Looper looper) {
            super(looper, null, HwCustPowerManagerServiceImpl.HWLOGW_E);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == HwCustPowerManagerServiceImpl.C2DM_ACQUIRE_EVENT) {
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
                sendMessageDelayed(obtainMessage(HwCustPowerManagerServiceImpl.C2DM_TIMEOUT_EVENT), (long) HwCustPowerManagerServiceImpl.this.c2dmDelayTimeOut);
            } else if (i == HwCustPowerManagerServiceImpl.C2DM_TIMEOUT_EVENT) {
                HwCustPowerManagerServiceImpl.this.mC2DMWakeLock.release();
                if (HwCustPowerManagerServiceImpl.HWDBG) {
                    Log.d(HwCustPowerManagerServiceImpl.TAG, "handleMessage C2DM_TIMEOUT_EVENT, mC2DMWakeLock.release!");
                }
            }
        }
    }
}
