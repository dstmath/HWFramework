package com.android.server.policy;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Flog;
import android.util.HwSlog;
import android.util.Log;
import android.view.DisplayCutout;
import com.android.server.FingerprintDataInterface;
import com.android.server.wm.DisplayFrames;
import com.android.server.wm.WindowManagerInternal;
import java.util.ServiceConfigurationError;

public class HwPhoneWindowManagerEx implements IHwPhoneWindowManagerEx {
    private static final long DELAY_LAUNCH_WALLET_TIME = 500;
    private static final String POWERKEY_QUICKPAY_PACKAGE = "com.huawei.wallet";
    private static final String QUICK_WALLET_ACTION_ACTIVITY = "com.huawei.oto.intent.action.QUICKPAY";
    private static final String QUICK_WALLET_ACTION_PRE_SERVICE = "com.huawei.wallet.PREPARESWIPE";
    private static final String QUICK_WALLET_ACTION_SERVICE = "com.huawei.wallet.QUICKSWIPE";
    static final String TAG = "HwPhoneWindowManagerEx";
    private boolean mAuthState;
    final Context mContext;
    IHwPhoneWindowManagerInner mIPwsInner = null;
    private boolean mIsIntersectCutout;
    private boolean mIsNavibarHide;
    private final Runnable mPowerKeyPreStartWallet = new Runnable() {
        public void run() {
            Log.i(HwPhoneWindowManagerEx.TAG, "begin preNotifyWallet");
            HwPhoneWindowManagerEx.this.preNotifyWallet();
        }
    };
    private final Runnable mPowerKeyStartWallet = new Runnable() {
        public void run() {
            Log.i(HwPhoneWindowManagerEx.TAG, "begin notifyWallet");
            HwPhoneWindowManagerEx.this.notifyWallet();
        }
    };

    public HwPhoneWindowManagerEx(IHwPhoneWindowManagerInner pws, Context context) {
        this.mIPwsInner = pws;
        this.mContext = context;
    }

    public void setNaviBarFlag(boolean flag) {
        if (flag != this.mIsNavibarHide) {
            this.mIsNavibarHide = flag;
            HwSlog.d(TAG, "setNeedHideWindow setFlag isNavibarHide is " + this.mIsNavibarHide);
        }
    }

    public boolean getNaviBarFlag() {
        return this.mIsNavibarHide;
    }

    public void updateNavigationBar(boolean minNaviBar) {
        Object obj = this.mIPwsInner.getNavigationBarPolicy();
        if (obj != null && (obj instanceof NavigationBarPolicy)) {
            NavigationBarPolicy navigationBarPolicy = (NavigationBarPolicy) obj;
            if (minNaviBar) {
                this.mIPwsInner.setNavigationBarHeightDef(this.mIPwsInner.getNavigationBarValueForRotation(0));
                this.mIPwsInner.setNavigationBarWidthDef(this.mIPwsInner.getNavigationBarValueForRotation(3));
            } else {
                HwSlog.d(TAG, "updateNavigationBar navigationbar mode: " + SystemProperties.getInt("persist.sys.navigationbar.mode", 0));
                Resources res = this.mContext.getResources();
                this.mIPwsInner.setNavigationBarValueForRotation(1, this.mIPwsInner.getRotationValueByType(2), res.getDimensionPixelSize(17105186));
                this.mIPwsInner.setNavigationBarValueForRotation(1, this.mIPwsInner.getRotationValueByType(3), res.getDimensionPixelSize(17105186));
                this.mIPwsInner.setNavigationBarValueForRotation(1, this.mIPwsInner.getRotationValueByType(0), res.getDimensionPixelSize(17105188));
                this.mIPwsInner.setNavigationBarValueForRotation(1, this.mIPwsInner.getRotationValueByType(1), res.getDimensionPixelSize(17105188));
                this.mIPwsInner.setNavigationBarValueForRotation(2, this.mIPwsInner.getRotationValueByType(2), res.getDimensionPixelSize(17105191));
                this.mIPwsInner.setNavigationBarValueForRotation(2, this.mIPwsInner.getRotationValueByType(3), res.getDimensionPixelSize(17105191));
                this.mIPwsInner.setNavigationBarValueForRotation(2, this.mIPwsInner.getRotationValueByType(0), res.getDimensionPixelSize(17105191));
                this.mIPwsInner.setNavigationBarValueForRotation(2, this.mIPwsInner.getRotationValueByType(1), res.getDimensionPixelSize(17105191));
                this.mIPwsInner.setNavigationBarHeightDef(this.mIPwsInner.getNavigationBarValueForRotation(1));
                this.mIPwsInner.setNavigationBarWidthDef(this.mIPwsInner.getNavigationBarValueForRotation(2));
            }
            navigationBarPolicy.updateNavigationBar(minNaviBar);
        }
    }

    public void removeFreeFormStackIfNeed(WindowManagerInternal windowManagerInternal) {
        if (HwFreeFormUtils.isFreeFormEnable() && windowManagerInternal.isStackVisible(5)) {
            try {
                ActivityManager.getService().removeStacksInWindowingModes(new int[]{5});
            } catch (RemoteException e) {
                HwFreeFormUtils.log(TAG, "RemoteException in removeFreeFormStackIfNeed");
            }
        }
    }

    public void setIntersectCutoutForNotch(boolean isIntersectCutout) {
        this.mIsIntersectCutout = isIntersectCutout;
    }

    public boolean isIntersectCutoutForNotch(DisplayFrames displayFrames, boolean isNotchSwitchOpen) {
        if (isNotchSwitchOpen) {
            HwSlog.d(TAG, "isIntersectCutoutForNotch isNotchSwitchOpen:" + isNotchSwitchOpen);
            return true;
        }
        DisplayCutout cutout = displayFrames.mDisplayCutout.getDisplayCutout();
        int safeInsetRight = cutout.getSafeInsetRight();
        int safeInsetBottom = cutout.getSafeInsetBottom();
        int displayWidth = displayFrames.mDisplayWidth;
        Rect rect = cutout.getBounds().getBounds();
        if (rect == null) {
            return true;
        }
        if (rect.top == 0 && safeInsetRight != 0 && !this.mIsIntersectCutout) {
            HwSlog.d(TAG, "isIntersectCutoutForNotch left-top cornor notch at rot 270");
            return false;
        } else if (rect.right != displayWidth || safeInsetBottom == 0) {
            return true;
        } else {
            HwSlog.d(TAG, "isIntersectCutoutForNotch left-top cornor notch at rot 180");
            return false;
        }
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

    public void launchWalletSwipe(Handler mHandler, long eventTime) {
        if (mHandler == null || eventTime <= 0) {
            Log.e(TAG, "launchWallet param error.");
            return;
        }
        mHandler.post(this.mPowerKeyPreStartWallet);
        long used = SystemClock.uptimeMillis() - eventTime;
        Log.i(TAG, "Starting launch wallet huaweiPay, down up used time: " + used);
        mHandler.postDelayed(this.mPowerKeyStartWallet, 500 - used);
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
    public void notifyWallet() {
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
        powerPressBDReport(985);
    }

    /* access modifiers changed from: private */
    public void preNotifyWallet() {
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
            Context context = this.mContext;
            Flog.bdReport(context, eventId, "{model:" + Build.MODEL + "}");
        }
    }
}
