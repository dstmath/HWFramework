package com.android.server.policy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.PointerEventListener;
import huawei.android.provider.FrontFingerPrintSettings;
import huawei.com.android.server.policy.HwGlobalActionsData;

public class FingerprintActionsListener implements PointerEventListener {
    private static final boolean ENABLE_MWSWITCH = true;
    private static final String FRONT_FINGERPRINT_SWAP_KEY_POSITION = "swap_key_position";
    static final int HIT_REGION_SCALE = 4;
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static final int MSG_CLOSE_SEARCH_PANEL = 1;
    private static final String TAG = "FingerprintActionsListener";
    private boolean isCoordinateForPad = SystemProperties.getBoolean("ro.config.coordinateforpad", false);
    private Context mContext = null;
    private boolean mDeviceProvisioned = true;
    private ContentObserver mDisplayDensityObserver;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    FingerprintActionsListener.this.hideSearchPanelView();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsDoubleFlinger = false;
    private boolean mIsNeedHideMultiWindowView = false;
    private boolean mIsSingleFlinger = false;
    private boolean mIsStatusBarExplaned = false;
    private boolean mIsValidGesture = false;
    private boolean mIsValidHiboardGesture = false;
    private boolean mIsValidLazyModeGesture = false;
    private HwSplitScreenArrowView mLandMultiWinArrowView = null;
    private HwSplitScreenArrowView mMultiWinArrowView = null;
    private PhoneWindowManager mPolicy = null;
    private HwSplitScreenArrowView mPortMultiWinArrowView = null;
    private SearchPanelView mSearchPanelView = null;
    private SettingsObserver mSettingsObserver;
    private SlideTouchEvent mSlideTouchEvent;
    private int mTrikeyNaviMode = -1;
    private WindowManager mWindowManager;
    private Point realSize = new Point();

    class SettingsObserver extends ContentObserver {
        final /* synthetic */ FingerprintActionsListener this$0;

        SettingsObserver(FingerprintActionsListener this$0, Handler handler) {
            boolean z = false;
            this.this$0 = this$0;
            super(handler);
            registerContentObserver(UserHandle.myUserId());
            if (Secure.getIntForUser(this$0.mContext.getContentResolver(), "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0) {
                z = true;
            }
            this$0.mDeviceProvisioned = z;
            this$0.mTrikeyNaviMode = System.getIntForUser(this$0.mContext.getContentResolver(), "swap_key_position", FrontFingerPrintSettings.getDefaultNaviMode(), ActivityManager.getCurrentUser());
        }

        public void registerContentObserver(int userId) {
            this.this$0.mContext.getContentResolver().registerContentObserver(System.getUriFor("swap_key_position"), false, this, userId);
            this.this$0.mContext.getContentResolver().registerContentObserver(System.getUriFor("device_provisioned"), false, this, userId);
        }

        public void onChange(boolean selfChange) {
            boolean z = false;
            FingerprintActionsListener fingerprintActionsListener = this.this$0;
            if (Secure.getIntForUser(this.this$0.mContext.getContentResolver(), "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0) {
                z = true;
            }
            fingerprintActionsListener.mDeviceProvisioned = z;
            this.this$0.mTrikeyNaviMode = System.getIntForUser(this.this$0.mContext.getContentResolver(), "swap_key_position", FrontFingerPrintSettings.getDefaultNaviMode(), ActivityManager.getCurrentUser());
        }
    }

    private class StatusBarStatesChangedReceiver extends BroadcastReceiver {
        /* synthetic */ StatusBarStatesChangedReceiver(FingerprintActionsListener this$0, StatusBarStatesChangedReceiver -this1) {
            this();
        }

        private StatusBarStatesChangedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && "com.android.systemui.statusbar.visible.change".equals(intent.getAction())) {
                FingerprintActionsListener.this.mIsStatusBarExplaned = Boolean.valueOf(intent.getExtras().getString("visible")).booleanValue();
                Log.i(FingerprintActionsListener.TAG, "mIsStatusBarExplaned = " + FingerprintActionsListener.this.mIsStatusBarExplaned);
            }
        }
    }

    public class TouchOutsideListener implements OnTouchListener {
        private int mMsg;

        public TouchOutsideListener(int msg, SearchPanelView panel) {
            this.mMsg = msg;
        }

        public boolean onTouch(View v, MotionEvent ev) {
            int action = ev.getAction();
            if (action != 4 && action != 0) {
                return false;
            }
            FingerprintActionsListener.this.mHandler.removeMessages(this.mMsg);
            FingerprintActionsListener.this.mHandler.sendEmptyMessage(this.mMsg);
            return true;
        }
    }

    public FingerprintActionsListener(Context context, PhoneWindowManager policy) {
        this.mContext = context;
        this.mPolicy = policy;
        updateRealSize();
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mSlideTouchEvent = new SlideTouchEvent(context);
        initialDensityObserver(this.mHandler);
        this.mSettingsObserver = new SettingsObserver(this, this.mHandler);
        initView();
        initStatusBarReciver();
    }

    private void initialDensityObserver(Handler handler) {
        this.mDisplayDensityObserver = new ContentObserver(handler) {
            public void onChange(boolean selfChange) {
                Log.i(FingerprintActionsListener.TAG, "Density has been changed");
                FingerprintActionsListener.this.initView();
                FingerprintActionsListener.this.createSearchPanelView();
                FingerprintActionsListener.this.createMultiWinArrowView();
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("display_density_forced"), false, this.mDisplayDensityObserver, UserHandle.myUserId());
    }

    private void initStatusBarReciver() {
        if (this.mContext != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.android.systemui.statusbar.visible.change");
            this.mContext.registerReceiver(new StatusBarStatesChangedReceiver(this, null), filter, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM", null);
            Log.i(TAG, "initStatusBarReciver completed");
        }
    }

    private void initView() {
        Point screenDims = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(screenDims);
        this.mSearchPanelView = (SearchPanelView) LayoutInflater.from(this.mContext).inflate(34013255, null);
        this.mPortMultiWinArrowView = (HwSplitScreenArrowView) LayoutInflater.from(this.mContext).inflate(34013263, null);
        if (this.mPortMultiWinArrowView != null) {
            this.mPortMultiWinArrowView.initViewParams(1, screenDims);
        }
        this.mLandMultiWinArrowView = (HwSplitScreenArrowView) LayoutInflater.from(this.mContext).inflate(34013264, null);
        if (this.mLandMultiWinArrowView != null) {
            this.mLandMultiWinArrowView.initViewParams(2, new Point(screenDims.y, screenDims.x));
        }
    }

    public void createSearchPanelView() {
        if (this.mSearchPanelView != null) {
            this.mSearchPanelView.setOnTouchListener(new TouchOutsideListener(1, this.mSearchPanelView));
            this.mSearchPanelView.setVisibility(8);
            addWindowView(this.mWindowManager, this.mSearchPanelView, getSearchLayoutParams(this.mSearchPanelView.getLayoutParams()));
            this.mSearchPanelView.initUI(this.mHandler.getLooper());
        }
    }

    public void destroySearchPanelView() {
        if (this.mSearchPanelView != null) {
            removeWindowView(this.mWindowManager, this.mSearchPanelView, true);
        }
    }

    public void createMultiWinArrowView() {
        if (ActivityManager.supportsMultiWindow(this.mContext)) {
            if (this.mMultiWinArrowView != null) {
                this.mMultiWinArrowView.removeViewToWindow();
            }
            if (1 == this.mContext.getResources().getConfiguration().orientation) {
                this.mMultiWinArrowView = this.mPortMultiWinArrowView;
            } else {
                this.mMultiWinArrowView = this.mLandMultiWinArrowView;
            }
            this.mMultiWinArrowView.addViewToWindow();
        }
    }

    public void destroyMultiWinArrowView() {
        if (ActivityManager.supportsMultiWindow(this.mContext) && this.mMultiWinArrowView != null) {
            this.mMultiWinArrowView.removeViewToWindow();
        }
    }

    private void hideSearchPanelView() {
        try {
            if (this.mSearchPanelView != null) {
                this.mSearchPanelView.hideSearchPanelView();
            }
        } catch (Exception exp) {
            Log.e(TAG, "hideSearchPanelView" + exp.getMessage());
        }
    }

    private boolean isSuperPowerSaveMode() {
        return SystemProperties.getBoolean("sys.super_power_save", false);
    }

    public void setCurrentUser(int newUserId) {
        this.mSettingsObserver.registerContentObserver(newUserId);
        this.mSettingsObserver.onChange(true);
        if (this.mSlideTouchEvent != null) {
            this.mSlideTouchEvent.updateSettings();
        }
    }

    protected LayoutParams getSearchLayoutParams(ViewGroup.LayoutParams layoutParams) {
        LayoutParams lp = new LayoutParams(-1, -1, 2024, 8519936, -3);
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= HwGlobalActionsData.FLAG_SHUTDOWN;
        }
        lp.gravity = 8388691;
        lp.setTitle("Framework_SearchPanel");
        lp.windowAnimations = 16974588;
        lp.softInputMode = 49;
        return lp;
    }

    public void addWindowView(WindowManager mWindowManager, View view, LayoutParams params) {
        try {
            mWindowManager.addView(view, params);
        } catch (Exception e) {
            Log.e(TAG, "the exception happen in addWindowView, e=" + e.getMessage());
        }
    }

    public void removeWindowView(WindowManager mWindowManager, View view, boolean immediate) {
        if (view == null) {
            return;
        }
        if (immediate) {
            try {
                mWindowManager.removeViewImmediate(view);
                return;
            } catch (IllegalArgumentException e) {
                return;
            } catch (Exception e2) {
                Log.e(TAG, "the exception happen in removeWindowView, e=" + e2.getMessage());
                return;
            }
        }
        mWindowManager.removeView(view);
    }

    public void onPointerEvent(MotionEvent motionEvent) {
        if (this.mDeviceProvisioned) {
            if (motionEvent.getPointerCount() == 1) {
                this.mIsSingleFlinger = true;
                this.mIsDoubleFlinger = false;
            } else if (motionEvent.getPointerCount() == 2) {
                this.mIsDoubleFlinger = true;
                this.mIsSingleFlinger = false;
                this.mIsValidLazyModeGesture = false;
                this.mIsValidHiboardGesture = false;
            } else {
                this.mIsDoubleFlinger = false;
                this.mIsSingleFlinger = false;
                this.mIsNeedHideMultiWindowView = true;
                if (this.mMultiWinArrowView != null && this.mMultiWinArrowView.getVisibility() == 0) {
                    this.mMultiWinArrowView.setVisibility(8);
                }
            }
            if (this.mIsSingleFlinger) {
                if (motionEvent.getActionMasked() == 0) {
                    Log.d(TAG, "touchDownIsValid MotionEvent.ACTION_DOWN ");
                    touchDownIsValidLazyMode(motionEvent.getRawX(), motionEvent.getRawY());
                }
                if (this.mIsValidLazyModeGesture) {
                    this.mSlideTouchEvent.handleTouchEvent(motionEvent);
                }
                if (!(!this.mIsValidHiboardGesture || (isSuperPowerSaveMode() ^ 1) == 0 || (isInLockTaskMode() ^ 1) == 0 || this.mIsStatusBarExplaned)) {
                    this.mSearchPanelView.handleGesture(motionEvent);
                }
            }
            if (!(this.mIsValidHiboardGesture || motionEvent.getActionMasked() != 1 || this.mSearchPanelView == null)) {
                this.mSearchPanelView.hideSearchPanelView();
            }
            if (this.mIsDoubleFlinger && (this.mIsNeedHideMultiWindowView ^ 1) != 0) {
                if (motionEvent.getActionMasked() == 5) {
                    Log.d(TAG, "touchDownIsValidMultiWin MotionEvent.ACTION_DOWN ");
                    this.mIsValidGesture = touchDownIsValidMultiWin(motionEvent);
                }
                if (this.mIsValidGesture && this.mMultiWinArrowView != null) {
                    this.mMultiWinArrowView.handleSplitScreenGesture(motionEvent);
                    if (this.mSearchPanelView != null) {
                        this.mSearchPanelView.hideSearchPanelView();
                    }
                }
            }
            if (motionEvent.getActionMasked() == 1) {
                reset();
            }
            if (motionEvent.getActionMasked() == 6) {
                this.mIsNeedHideMultiWindowView = true;
            }
        }
    }

    private void reset() {
        this.mIsValidGesture = false;
        this.mIsValidLazyModeGesture = false;
        this.mIsValidHiboardGesture = false;
        this.mIsNeedHideMultiWindowView = false;
    }

    private boolean isNaviBarEnable() {
        return FrontFingerPrintSettings.isNaviBarEnabled(this.mContext.getContentResolver());
    }

    private void touchDownIsValidLazyMode(float pointX, float pointY) {
        if (this.mPolicy.mDisplay == null || (this.mPolicy.mKeyguardDelegate.isShowing() && (this.mPolicy.mKeyguardDelegate.isOccluded() ^ 1) != 0)) {
            this.mIsValidLazyModeGesture = false;
            this.mIsValidHiboardGesture = false;
            return;
        }
        int HIT_REGION_TO_MAX_LAZYMODE = this.mContext.getResources().getDimensionPixelSize(17105141);
        int HIT_REGION_TO_MAX_HIBOARD = (int) (((double) this.mContext.getResources().getDimensionPixelSize(17105141)) / 4.0d);
        updateRealSize();
        if (this.isCoordinateForPad) {
            HIT_REGION_TO_MAX_HIBOARD *= 2;
        }
        if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
        }
        boolean z;
        if (this.mPolicy.mNavigationBarPosition == 0) {
            boolean isDistanceValid = pointY > ((float) (this.realSize.y - HIT_REGION_TO_MAX_LAZYMODE)) ? pointX < ((float) HIT_REGION_TO_MAX_LAZYMODE) || pointX > ((float) (this.realSize.x - HIT_REGION_TO_MAX_LAZYMODE)) : false;
            this.mIsValidLazyModeGesture = isDistanceValid ? isNaviBarEnable() ^ 1 : false;
            z = (pointY <= ((float) (this.realSize.y - HIT_REGION_TO_MAX_HIBOARD)) || pointX == ((float) (this.realSize.x / 2)) || pointY == ((float) this.realSize.y) || (isNaviBarEnable() ^ 1) == 0) ? false : FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 0 ? FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1 && this.mTrikeyNaviMode < 0 : true;
            this.mIsValidHiboardGesture = z;
        } else {
            int invalidPointY = this.realSize.y / 2;
            int invalidPointX = this.realSize.x;
            this.mIsValidLazyModeGesture = false;
            z = (pointX <= ((float) (this.realSize.x - HIT_REGION_TO_MAX_HIBOARD)) || pointX == ((float) invalidPointX) || pointY == ((float) invalidPointY) || (isNaviBarEnable() ^ 1) == 0) ? false : FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 0 ? FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1 && this.mTrikeyNaviMode < 0 : true;
            this.mIsValidHiboardGesture = z;
        }
        if (this.mPolicy.isKeyguardLocked()) {
            this.mIsValidLazyModeGesture = false;
            this.mIsValidHiboardGesture = false;
        }
        Log.d(TAG, "touchDownIsValidLazyMode = " + this.mIsValidLazyModeGesture + "  touchDownIsValidHiBoard = " + this.mIsValidHiboardGesture);
    }

    private void updateRealSize() {
        if (this.mPolicy.mDisplay != null) {
            this.mPolicy.mDisplay.getRealSize(this.realSize);
        }
    }

    private boolean touchDownIsValidMultiWin(MotionEvent event) {
        if (isNaviBarEnable() || this.mPolicy.mDisplay == null || event.getPointerCount() != 2 || ((this.mPolicy.mKeyguardDelegate.isShowing() && (this.mPolicy.mKeyguardDelegate.isOccluded() ^ 1) != 0) || isSuperPowerSaveMode())) {
            return false;
        }
        float pointX0 = event.getX(0);
        float pointY0 = event.getY(0);
        float pointX1 = event.getX(1);
        float pointY1 = event.getY(1);
        int navigation_bar_height = (int) (((double) this.mContext.getResources().getDimensionPixelSize(17105141)) / 4.0d);
        if (this.isCoordinateForPad) {
            navigation_bar_height *= 2;
        }
        updateRealSize();
        boolean ret = this.mPolicy.mNavigationBarPosition == 0 ? pointY0 > ((float) (this.realSize.y - navigation_bar_height)) && pointY1 > ((float) (this.realSize.y - navigation_bar_height)) : pointX0 > ((float) (this.realSize.x - navigation_bar_height)) && pointX1 > ((float) (this.realSize.x - navigation_bar_height));
        Log.d(TAG, "touchDownIsValidMultiWin ret = " + ret);
        return ret;
    }

    private boolean isInLockTaskMode() {
        return ((ActivityManager) this.mContext.getSystemService("activity")).isInLockTaskMode();
    }
}
