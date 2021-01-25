package com.android.server.policy;

import android.app.ActivityTaskManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import com.android.server.FingerprintDataInterface;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.policy.keyguard.KeyguardServiceDelegate;
import com.android.server.wm.DisplayFrames;
import com.android.server.wm.DisplayPolicy;
import com.android.server.wm.WindowManagerInternal;
import com.android.server.wm.WindowState;
import com.huawei.server.HwPartIawareUtil;
import java.util.ServiceConfigurationError;

public class HwPhoneWindowManagerEx implements IHwPhoneWindowManagerEx {
    private static final long DELAY_LAUNCH_WALLET_TIME = 500;
    private static final int INVALID_DIRECTION = -1;
    private static final String POWERKEY_QUICKPAY_PACKAGE = "com.huawei.wallet";
    private static final String QUICK_WALLET_ACTION_ACTIVITY = "com.huawei.oto.intent.action.QUICKPAY";
    private static final String QUICK_WALLET_ACTION_PRE_SERVICE = "com.huawei.wallet.PREPARESWIPE";
    private static final String QUICK_WALLET_ACTION_SERVICE = "com.huawei.wallet.QUICKSWIPE";
    static final String TAG = "HwPhoneWindowManagerEx";
    private boolean mAuthState;
    final Context mContext;
    IHwPhoneWindowManagerInner mIPwsInner = null;
    private boolean mIsIntersectCutout;
    private final Runnable mPowerKeyPreStartWallet = new Runnable() {
        /* class com.android.server.policy.HwPhoneWindowManagerEx.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            Log.i(HwPhoneWindowManagerEx.TAG, "begin preNotifyWallet");
            HwPhoneWindowManagerEx.this.preNotifyWallet();
        }
    };
    private final Runnable mPowerKeyStartWallet = new Runnable() {
        /* class com.android.server.policy.HwPhoneWindowManagerEx.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            Log.i(HwPhoneWindowManagerEx.TAG, "begin notifyWallet");
            HwPhoneWindowManagerEx.this.notifyWallet();
        }
    };
    private Boolean shouldIntersectCutoutForNotch = null;

    public HwPhoneWindowManagerEx(IHwPhoneWindowManagerInner pws, Context context) {
        this.mIPwsInner = pws;
        this.mContext = context;
    }

    public void removeFreeFormStackIfNeed(WindowManagerInternal windowManagerInternal) {
        if (HwFreeFormUtils.isFreeFormEnable()) {
            synchronized (windowManagerInternal.getActivityTaskManagerService().getGlobalLock()) {
                if (windowManagerInternal.isStackVisibleLw(5)) {
                    try {
                        ActivityTaskManager.getService().removeStacksInWindowingModes(new int[]{5});
                    } catch (RemoteException e) {
                        HwFreeFormUtils.log(TAG, "RemoteException in removeFreeFormStackIfNeed");
                    }
                }
            }
        }
    }

    public WindowState getTopFullscreenWindow() {
        DisplayPolicy displayPolicy = this.mIPwsInner.getDefaultDisplayPolicy();
        if (displayPolicy != null) {
            return displayPolicy.getTopFullscreenOpaqueWindowState();
        }
        return null;
    }

    public void setIntersectCutoutForNotch(boolean isIntersectCutout) {
        this.mIsIntersectCutout = isIntersectCutout;
    }

    public boolean isIntersectCutoutForNotch(DisplayFrames displayFrames, boolean isNotchSwitchOpen) {
        if (isNotchSwitchOpen) {
            return true;
        }
        DisplayCutout cutout = displayFrames.mDisplayCutout.getDisplayCutout();
        int safeInsetRight = cutout.getSafeInsetRight();
        if (this.shouldIntersectCutoutForNotch == null) {
            this.shouldIntersectCutoutForNotch = Boolean.valueOf(this.mContext.getResources().getBoolean(17891617));
        }
        int displayHeight = displayFrames.mDisplayHeight;
        Rect rect270 = cutout.getBoundingRectRight();
        if (rect270 == null) {
            return true;
        }
        if ((rect270.top == 0 || rect270.bottom == displayHeight) && safeInsetRight != 0 && !this.mIsIntersectCutout) {
            return this.shouldIntersectCutoutForNotch.booleanValue();
        }
        int safeInsetBottom = cutout.getSafeInsetBottom();
        int displayWidth = displayFrames.mDisplayWidth;
        Rect rect180 = cutout.getBoundingRectBottom();
        if (rect180 == null) {
            return true;
        }
        if ((rect180.right == displayWidth || rect180.left == 0) && safeInsetBottom != 0) {
            return this.shouldIntersectCutoutForNotch.booleanValue();
        }
        return true;
    }

    public boolean getFPAuthState() {
        return this.mAuthState;
    }

    public void setFPAuthState(boolean authState) {
        this.mAuthState = authState;
    }

    public boolean isNeedWaitForAuthenticate() {
        return FingerprintDataInterface.getInstance().isNeedWaitForAuthenticate();
    }

    public boolean isPowerFpForbidGotoSleep() {
        return FingerprintDataInterface.getInstance().isPowerFpForbidGotoSleep();
    }

    public void sendPowerKeyToFingerprint(int keyCode, boolean isDown, boolean interactive) {
        FingerprintDataInterface.getInstance().sendPowerKeyCode(keyCode, isDown, interactive);
    }

    public View tryAddViewFromCache(String packageName, IBinder appToken, Configuration config) {
        return HwPartIawareUtil.tryAddViewFromCache(packageName, appToken, config);
    }

    public void putViewToCache(String packageName, View startView, WindowManager.LayoutParams params) {
        HwPartIawareUtil.putViewToCache(packageName, startView, params);
    }

    public void launchWalletSwipe(Handler mHandler, long eventTime) {
        if (mHandler == null || eventTime <= 0) {
            Log.e(TAG, "launchWallet param error.");
            return;
        }
        mHandler.post(this.mPowerKeyPreStartWallet);
        long used = SystemClock.uptimeMillis() - eventTime;
        Log.i(TAG, "Starting launch wallet huaweiPay, down up used time: " + used);
        mHandler.postDelayed(this.mPowerKeyStartWallet, DELAY_LAUNCH_WALLET_TIME - used);
    }

    public void cancelWalletSwipe(Handler mHandler) {
        if (mHandler == null) {
            Log.e(TAG, "launchWallet param error.");
            return;
        }
        mHandler.removeCallbacks(this.mPowerKeyStartWallet);
        Log.i(TAG, "cancelWallet");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyWallet() {
        Intent intent = new Intent(QUICK_WALLET_ACTION_SERVICE);
        intent.setPackage(POWERKEY_QUICKPAY_PACKAGE);
        intent.putExtra("channel", "doubleClickPowerBtn");
        if (isIntentAvailable(intent)) {
            try {
                this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
            } catch (ServiceConfigurationError e) {
                Log.e(TAG, "can not start wallet service: ServiceConfigurationError");
            } catch (Exception e2) {
                Log.e(TAG, "can not start wallet service: Exception");
            }
            Log.i(TAG, "notifyWallet by service end");
        } else {
            Intent intent2 = new Intent(QUICK_WALLET_ACTION_ACTIVITY);
            intent2.setPackage(POWERKEY_QUICKPAY_PACKAGE);
            intent2.putExtra("channel", "doubleClickPowerBtn");
            try {
                this.mContext.startActivityAsUser(intent2, UserHandle.CURRENT);
            } catch (ActivityNotFoundException e3) {
                Log.e(TAG, "can not start wallet activity: ActivityNotFoundException");
            } catch (Exception e4) {
                Log.e(TAG, "can not start wallet activity: Exception");
            }
            Log.i(TAG, "notifyWallet by Activity end");
        }
        powerPressBDReport(991310985);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void preNotifyWallet() {
        Intent intent = new Intent(QUICK_WALLET_ACTION_PRE_SERVICE);
        if (isIntentAvailable(intent)) {
            intent.setPackage(POWERKEY_QUICKPAY_PACKAGE);
            intent.putExtra("channel", "doubleClickPowerBtn");
            try {
                this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
            } catch (ServiceConfigurationError e) {
                Log.e(TAG, "can not start service: ServiceConfigurationError");
            } catch (Exception e2) {
                Log.e(TAG, "can not start service: Exception");
            }
            Log.i(TAG, "preNotifyWallet end");
        }
    }

    private boolean isIntentAvailable(Intent intent) {
        if (this.mContext.getPackageManager().queryIntentServices(intent, 32).size() > 0) {
            return true;
        }
        Log.i(TAG, "wallet swipe service is not avaiable");
        return false;
    }

    private void powerPressBDReport(int eventId) {
        if (Log.HWINFO) {
            Flog.bdReport(eventId, "model", Build.MODEL);
        }
    }

    public void showKeyguard(WindowManagerPolicy policy) {
        KeyguardServiceDelegate keyguardServiceDelegate;
        if (HwPCUtils.isDisallowLockScreenForHwMultiDisplay() && (policy instanceof PhoneWindowManager) && (keyguardServiceDelegate = ((PhoneWindowManager) policy).mKeyguardDelegate) != null) {
            keyguardServiceDelegate.doKeyguardTimeout((Bundle) null);
        }
    }

    public void handleHicarExtraKeys(KeyEvent event, WindowManagerPolicy.WindowState win, WindowManagerInternal wmInternal) {
        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isHiCarCastMode()) {
            HwPCUtils.log(TAG, "handleHicarExtraKeys not in hicar mode");
            return;
        }
        int direction = checkIfSwitchKeys(event.getKeyCode());
        if (direction == -1) {
            HwPCUtils.log(TAG, "handleHicarExtraKeys not valid keys");
            return;
        }
        boolean down = event.getAction() == 0;
        int repeatCount = event.getRepeatCount();
        if (HwPCUtils.isValidExtDisplayId(win.getDisplayId())) {
            if (down && repeatCount == 0) {
                wmInternal.travelsalPCWindowsAndFindOne(direction);
            }
        } else if (down && repeatCount == 0) {
            wmInternal.travelsalPCWindowsAndFindOne(4);
        }
    }

    private int checkIfSwitchKeys(int keyCode) {
        if (keyCode == 0) {
            return 4;
        }
        switch (keyCode) {
            case 19:
                return 0;
            case 20:
                return 3;
            case 21:
                return 1;
            case 22:
                return 2;
            default:
                switch (keyCode) {
                    case 741:
                        return 0;
                    case 742:
                        return 3;
                    case 743:
                        return 1;
                    case 744:
                        return 2;
                    default:
                        return -1;
                }
        }
    }
}
