package com.android.internal.policy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.hwcontrol.HwWidgetFactory;
import android.os.IBinder;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.IWindowManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import androidhwext.R;
import com.android.internal.widget.ActionBarOverlayLayout;
import com.android.internal.widget.FloatingToolbar;
import com.android.internal.widget.HwDecorCaptionView;
import com.huawei.hsm.permission.StubController;
import huawei.android.utils.HwRTBlurUtils;

public class HwPhoneWindow extends PhoneWindow {
    private static final int FLOATING_MASK = Integer.MIN_VALUE;
    protected static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", "").equals(""));
    static final String TAG = "HwPhoneWindow";
    public boolean mForceDisableBlurBehind = false;
    private boolean mHwDrawerFeature;
    private boolean mHwFloating;
    private int mHwOverlayActionBar;
    private boolean mIsTranslucentImmersion;
    private boolean mNavBarShow = true;
    private boolean mSplitMode;
    private IBinder mToken;

    public static class Utils {
        /* access modifiers changed from: private */
        public static final boolean mIsChina = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
        private static boolean mIsLastDeviceProvisioned = false;

        public static void setLastDeviceProvisioned(boolean lastDeviceProvisioned) {
            mIsLastDeviceProvisioned = lastDeviceProvisioned;
        }

        public static boolean getLastDeviceProvisioned() {
            return mIsLastDeviceProvisioned;
        }
    }

    public HwPhoneWindow(Context context) {
        super(context);
    }

    public HwPhoneWindow(Context context, Window preservedWindow, ViewRootImpl.ActivityConfigCallback activityConfigCallback) {
        super(context, preservedWindow, activityConfigCallback);
        try {
            if (context.getDisplay() != null && HwPCUtils.isValidExtDisplayId(context.getDisplay().getDisplayId())) {
                this.mUseDecorContext = false;
            }
        } catch (NoSuchMethodError e) {
            Log.e(TAG, "HwPhoneWindow isValidExtDisplayId NoSuchMethodError");
        } catch (Exception e2) {
            Log.e(TAG, "HwPhoneWindow isValidExtDisplayId error");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isEmuiStyle() {
        return HwWidgetFactory.isHwTheme(getContext());
    }

    /* access modifiers changed from: protected */
    public boolean isEmuiLightStyle() {
        return HwWidgetFactory.isHwLightTheme(getContext());
    }

    /* access modifiers changed from: protected */
    public int getHeightMeasureSpec(int fixh, int heightSize, int defaultHeightMeasureSpec) {
        if (!isEmuiStyle()) {
            return HwPhoneWindow.super.getHeightMeasureSpec(fixh, heightSize, defaultHeightMeasureSpec);
        }
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        if (metrics.widthPixels > metrics.heightPixels) {
        }
        if (fixh < metrics.heightPixels) {
            fixh -= (int) (((float) getContext().getResources().getDimensionPixelSize(17105318)) * ((((float) fixh) + 0.0f) / ((float) metrics.heightPixels)));
        }
        return View.MeasureSpec.makeMeasureSpec(fixh < heightSize ? fixh : heightSize, Integer.MIN_VALUE);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int getEmuiActionBarLayout(int layoutResource) {
        return layoutResource;
    }

    /* access modifiers changed from: protected */
    public void setEmuiActionModeBar(ViewStub viewStub) {
        if (viewStub != null && isEmuiStyle()) {
            viewStub.setLayoutResource(34013271);
        }
    }

    /* access modifiers changed from: protected */
    public boolean CheckPermanentMenuKey() {
        if (isEmuiStyle()) {
            return true;
        }
        return HwPhoneWindow.super.CheckPermanentMenuKey();
    }

    /* access modifiers changed from: protected */
    public void updateLayoutParamsColor() {
        WindowManager.LayoutParams attrs = getAttributes();
        boolean changed = false;
        int statusBarColor = getStatusBarColor();
        if (attrs.statusBarColor != statusBarColor) {
            changed = true;
            attrs.statusBarColor = statusBarColor;
        }
        int navigationBarColor = getNavigationBarColor();
        if (attrs.navigationBarColor != navigationBarColor) {
            changed = true;
            attrs.navigationBarColor = navigationBarColor;
        }
        int emuiValue = -1;
        if ((attrs.privateFlags & StubController.PERMISSION_DELETE_CALENDAR) == 0) {
            emuiValue = getEmuiValue((int) isEmuiStyle());
        }
        if (attrs.isEmuiStyle != emuiValue) {
            changed = true;
            attrs.isEmuiStyle = emuiValue;
        }
        int hwflag = attrs.hwFlags;
        boolean isForcedNavigationBarColor = isForcedNavigationBarColor();
        if (navigationBarColor == -197380 && this.mSpecialSet && !isForcedNavigationBarColor && this.mNavBarShow && !this.mIsFloating) {
            attrs.hwFlags |= 16;
        } else if (attrs.type != 2011 || !isEmuiLightStyle()) {
            attrs.hwFlags &= -17;
        } else if (!isForcedNavigationBarColor) {
            attrs.hwFlags |= 16;
        } else {
            attrs.hwFlags &= -17;
        }
        if (hwflag != attrs.hwFlags) {
            changed = true;
        }
        Log.i(TAG, "updateLayoutParamsColor " + changed + " mSpecialSet=" + this.mSpecialSet + ", mForcedNavigationBarColor=" + isForcedNavigationBarColor + ", navigationBarColor=" + Integer.toHexString(navigationBarColor) + ", mNavBarShow=" + this.mNavBarShow + ", mIsFloating=" + this.mIsFloating);
        if (changed) {
            dispatchWindowAttributesChanged(attrs);
        }
    }

    private int getEmuiValue(int emuiValue) {
        if (this.mIsFloating || this.mHwFloating || (emuiValue != 0 && windowIsTranslucent() && !isTranslucentImmersion())) {
            return emuiValue | Integer.MIN_VALUE;
        }
        return emuiValue;
    }

    public void setHwFloating(boolean isFloating) {
        if (this.mHwFloating != isFloating) {
            this.mHwFloating = isFloating;
            updateLayoutParamsColor();
        }
    }

    public boolean getHwFloating() {
        return this.mHwFloating;
    }

    /* access modifiers changed from: protected */
    public boolean getTryForcedCloseAnimation(IWindowManager wm, boolean animate, Object tag) {
        if (!animate || !"TryForcedCloseAnimation".equals(tag)) {
            return false;
        }
        return true;
    }

    public int getStatusBarColor() {
        if (!HwWidgetFactory.isHwLightTheme(getContext()) || getIsForcedStatusBarColor()) {
            return HwPhoneWindow.super.getStatusBarColor();
        }
        return HwWidgetFactory.getPrimaryColor(getContext());
    }

    /* access modifiers changed from: protected */
    public FloatingToolbar getFloatingToolbar(Window window) {
        return new FloatingToolbar(window, true);
    }

    /* access modifiers changed from: protected */
    public void initTranslucentImmersion() {
        Context context = getContext();
        if (context != null) {
            TypedValue tv = new TypedValue();
            boolean z = false;
            context.getTheme().resolveAttribute(33620019, tv, false);
            if (tv.type == 18 && tv.data == -1) {
                z = true;
            }
            this.mIsTranslucentImmersion = z;
        }
    }

    /* access modifiers changed from: protected */
    public void initSplitMode() {
        Context c = getContext();
        if (c != null && (c instanceof Activity)) {
            Intent intent = ((Activity) c).getIntent();
            if (intent != null) {
                this.mSplitMode = (intent.getHwFlags() & 4) != 0;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isSplitMode() {
        return this.mSplitMode;
    }

    /* access modifiers changed from: protected */
    public boolean isTranslucentImmersion() {
        return this.mIsTranslucentImmersion;
    }

    public void setContentView(int layoutResID) {
        HwPhoneWindow.super.setContentView(layoutResID);
        updateBlurStatus();
        initChildWindowIgnoreParentWindowClipRect();
    }

    public void setContentView(View view) {
        HwPhoneWindow.super.setContentView(view);
        updateBlurStatus();
        initChildWindowIgnoreParentWindowClipRect();
    }

    public void setContentView(View view, ViewGroup.LayoutParams params) {
        HwPhoneWindow.super.setContentView(view, params);
        updateBlurStatus();
        initChildWindowIgnoreParentWindowClipRect();
    }

    public void forceDisableBlurBehind() {
        this.mForceDisableBlurBehind = true;
    }

    private void updateBlurStatus() {
        WindowManager.LayoutParams lp = getAttributes();
        HwRTBlurUtils.BlurParams blurParams = HwRTBlurUtils.obtainBlurStyle(getContext(), null, 33619992, 0, TAG);
        if (lp != null) {
            try {
                if (getContext() != null && HwPCUtils.isValidExtDisplayId(getContext()) && getContext().getPackageName().equals("com.android.packageinstaller")) {
                    lp.flags &= -5;
                    return;
                }
            } catch (NoSuchMethodError e) {
                Log.e(TAG, "HwPhoneWindow isValidExtDisplayId NoSuchMethodError");
            }
        }
        if (this.mForceDisableBlurBehind) {
            blurParams.enable = false;
        }
        HwRTBlurUtils.updateWindowBgForBlur(blurParams, getDecorView());
        HwRTBlurUtils.updateBlurStatus(getAttributes(), blurParams);
    }

    private void initChildWindowIgnoreParentWindowClipRect() {
        TypedArray ahwext = getContext().obtainStyledAttributes(null, R.styleable.Window, 0, 0);
        boolean ignore = ahwext.getBoolean(0, false);
        ahwext.recycle();
        WindowManager.LayoutParams lp = getAttributes();
        if (ignore) {
            lp.privateFlags |= StubController.PERMISSION_ACCESS_BROWSER_RECORDS;
        } else {
            lp.privateFlags &= -1073741825;
        }
    }

    public void setHwDrawerFeature(boolean using, int overlayActionBar) {
        this.mHwDrawerFeature = using;
        if (using) {
            this.mHwOverlayActionBar = overlayActionBar;
        } else {
            this.mHwOverlayActionBar = 0;
        }
        initHwDrawerFeature();
    }

    /* access modifiers changed from: protected */
    public void initHwDrawerFeature() {
        if (this.mDecorContentParent instanceof ActionBarOverlayLayout) {
            this.mDecorContentParent.setHwDrawerFeature(this.mHwDrawerFeature, this.mHwOverlayActionBar);
        }
    }

    public void setDrawerOpend(boolean open) {
        if (this.mDecorContentParent != null && (this.mDecorContentParent instanceof ActionBarOverlayLayout)) {
            this.mDecorContentParent.setDrawerOpend(open);
        }
    }

    public void onWindowStateChanged(int windowState) {
        if (this.mDecor != null && this.mDecor.mDecorCaptionView != null && (this.mDecor.mDecorCaptionView instanceof HwDecorCaptionView)) {
            this.mDecor.mDecorCaptionView.onWindowStateChanged(windowState);
        }
    }

    public void setAppToken(IBinder token) {
        this.mToken = token;
    }

    public IBinder getAppToken() {
        return this.mToken;
    }

    public void setTitle(CharSequence title, boolean updateAccessibilityTitle) {
        HwPhoneWindow.super.setTitle(title, updateAccessibilityTitle);
        if (this.mDecor != null && this.mDecor.mDecorCaptionView != null && (this.mDecor.mDecorCaptionView instanceof HwDecorCaptionView)) {
            this.mDecor.mDecorCaptionView.setTitle(title);
        }
    }

    /* access modifiers changed from: protected */
    public void setHwFlagForNotch(boolean isTranslucent) {
        if (IS_NOTCH_PROP && isTranslucent) {
            WindowManager.LayoutParams attrs = getAttributes();
            attrs.hwFlags = (attrs.hwFlags & -32769) | StubController.PERMISSION_CALLLOG_WRITE;
        }
    }

    /* access modifiers changed from: protected */
    public void setHwFlagForInvisibleWindowDetection(boolean isTranslucent, boolean noActionBar) {
        WindowManager.LayoutParams attrs = getAttributes();
        if (isTranslucent) {
            attrs.hwFlags = (attrs.hwFlags & -268435457) | 268435456;
        }
        if (noActionBar) {
            attrs.hwFlags = (attrs.hwFlags & -536870913) | StubController.PERMISSION_DELETE_CALENDAR;
        }
    }

    private boolean isDeviceProvisioned() {
        return Settings.Global.getInt(getContext().getContentResolver(), "device_provisioned", 0) != 0;
    }

    public boolean isNavigationBarSetWhite() {
        if (Utils.mIsChina || Utils.getLastDeviceProvisioned()) {
            return false;
        }
        Utils.setLastDeviceProvisioned(isDeviceProvisioned());
        return !Utils.getLastDeviceProvisioned();
    }

    public void setNavBarShowStatus(boolean show) {
        if (this.mNavBarShow != show) {
            this.mNavBarShow = show;
            updateLayoutParamsColor();
        }
    }
}
