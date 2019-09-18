package com.android.server.policy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerPolicyConstants;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.provider.FrontFingerPrintSettings;

public class FingerprintActionsListener implements WindowManagerPolicyConstants.PointerEventListener {
    private static final boolean ENABLE_MWSWITCH = true;
    private static final String FRONT_FINGERPRINT_SWAP_KEY_POSITION = "swap_key_position";
    private static final String GSETTINGS_VDRIVE_IS_RUN = "vdrive_is_run_state";
    static final int HIT_REGION_SCALE = 4;
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final int MSG_CLOSE_SEARCH_PANEL = 1;
    private static final String TAG = "FingerprintActionsListener";
    private static final String TALKBACK_COMPONENT_NAME = "com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService";
    private static final int VDRIVE_IS_RUN = 1;
    private static final int VDRIVE_IS_UNRUN = 0;
    private static final boolean mDisableMultiWin = SystemProperties.getBoolean("ro.huawei.disable_multiwindow", false);
    private boolean isCoordinateForPad = SystemProperties.getBoolean("ro.config.coordinateforpad", false);
    /* access modifiers changed from: private */
    public Context mContext = null;
    /* access modifiers changed from: private */
    public boolean mDeviceProvisioned = true;
    private ContentObserver mDisplayDensityObserver;
    /* access modifiers changed from: private */
    public boolean mDriveState = false;
    /* access modifiers changed from: private */
    public boolean mGestureNavEnabled;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                FingerprintActionsListener.this.hideSearchPanelView();
            }
        }
    };
    private boolean mIsDoubleFlinger = false;
    private boolean mIsNeedHideMultiWindowView = false;
    private boolean mIsSingleFlinger = false;
    /* access modifiers changed from: private */
    public boolean mIsStatusBarExplaned = false;
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
    /* access modifiers changed from: private */
    public boolean mTalkBackOn;
    /* access modifiers changed from: private */
    public int mTrikeyNaviMode = -1;
    private WindowManager mWindowManager;
    private Point realSize = new Point();

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            registerContentObserver(UserHandle.myUserId());
            updateCurrentSettigns();
        }

        public void registerContentObserver(int userId) {
            FingerprintActionsListener.this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("swap_key_position"), false, this, userId);
            FingerprintActionsListener.this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("device_provisioned"), false, this, userId);
            FingerprintActionsListener.this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(GestureNavConst.KEY_SECURE_GESTURE_NAVIGATION), false, this, userId);
            FingerprintActionsListener.this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("accessibility_enabled"), false, this, userId);
            FingerprintActionsListener.this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("enabled_accessibility_services"), false, this, userId);
        }

        public void onChange(boolean selfChange) {
            updateCurrentSettigns();
        }

        private void updateCurrentSettigns() {
            boolean isContainsTalkBackService;
            boolean z = true;
            boolean unused = FingerprintActionsListener.this.mDeviceProvisioned = Settings.Secure.getIntForUser(FingerprintActionsListener.this.mContext.getContentResolver(), "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0;
            int unused2 = FingerprintActionsListener.this.mTrikeyNaviMode = Settings.System.getIntForUser(FingerprintActionsListener.this.mContext.getContentResolver(), "swap_key_position", FrontFingerPrintSettings.getDefaultNaviMode(), ActivityManager.getCurrentUser());
            boolean unused3 = FingerprintActionsListener.this.mGestureNavEnabled = GestureNavConst.isGestureNavEnabled(FingerprintActionsListener.this.mContext, -2);
            boolean accessibilityEnabled = Settings.Secure.getIntForUser(FingerprintActionsListener.this.mContext.getContentResolver(), "accessibility_enabled", 0, -2) == 1;
            String enabledSerices = Settings.Secure.getStringForUser(FingerprintActionsListener.this.mContext.getContentResolver(), "enabled_accessibility_services", -2);
            Log.i(FingerprintActionsListener.TAG, "accessibilityEnabled:" + accessibilityEnabled + ",isContainsTalkBackService:" + isContainsTalkBackService + ",mGestureNavEnabled:" + FingerprintActionsListener.this.mGestureNavEnabled);
            FingerprintActionsListener fingerprintActionsListener = FingerprintActionsListener.this;
            if (!accessibilityEnabled || !isContainsTalkBackService) {
                z = false;
            }
            boolean unused4 = fingerprintActionsListener.mTalkBackOn = z;
        }
    }

    private class StatusBarStatesChangedReceiver extends BroadcastReceiver {
        private StatusBarStatesChangedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && "com.android.systemui.statusbar.visible.change".equals(intent.getAction()) && intent.getExtras() != null) {
                boolean unused = FingerprintActionsListener.this.mIsStatusBarExplaned = Boolean.valueOf(intent.getExtras().getString("visible")).booleanValue();
                Log.i(FingerprintActionsListener.TAG, "mIsStatusBarExplaned = " + FingerprintActionsListener.this.mIsStatusBarExplaned);
            }
        }
    }

    public class TouchOutsideListener implements View.OnTouchListener {
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
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        initView();
        initStatusBarReciver();
        initDriveStateReciver();
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
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("display_density_forced"), false, this.mDisplayDensityObserver, UserHandle.myUserId());
    }

    private void initDriveStateReciver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(GSETTINGS_VDRIVE_IS_RUN), false, new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                int state = Settings.Global.getInt(FingerprintActionsListener.this.mContext.getContentResolver(), FingerprintActionsListener.GSETTINGS_VDRIVE_IS_RUN, 0);
                Log.i(FingerprintActionsListener.TAG, "mVDriveStateObserver onChange selfChange = " + selfChange + "   state = " + state);
                if (1 == state) {
                    boolean unused = FingerprintActionsListener.this.mDriveState = true;
                } else {
                    boolean unused2 = FingerprintActionsListener.this.mDriveState = false;
                }
            }
        });
    }

    private void initStatusBarReciver() {
        if (this.mContext != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.android.systemui.statusbar.visible.change");
            this.mContext.registerReceiver(new StatusBarStatesChangedReceiver(), filter, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM", null);
            Log.i(TAG, "initStatusBarReciver completed");
        }
    }

    /* access modifiers changed from: private */
    public void initView() {
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

    /* access modifiers changed from: private */
    public void hideSearchPanelView() {
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

    /* access modifiers changed from: protected */
    public WindowManager.LayoutParams getSearchLayoutParams(ViewGroup.LayoutParams layoutParams) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, HwArbitrationDEFS.MSG_VPN_STATE_OPEN, 8519936, -3);
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= 16777216;
        }
        lp.gravity = 8388691;
        lp.setTitle("Framework_SearchPanel");
        lp.windowAnimations = 16974590;
        lp.softInputMode = 49;
        return lp;
    }

    public void addWindowView(WindowManager mWindowManager2, View view, WindowManager.LayoutParams params) {
        try {
            mWindowManager2.addView(view, params);
        } catch (Exception e) {
            Log.e(TAG, "the exception happen in addWindowView, e=" + e.getMessage());
        }
    }

    public void removeWindowView(WindowManager mWindowManager2, View view, boolean immediate) {
        if (view == null) {
            return;
        }
        if (immediate) {
            try {
                mWindowManager2.removeViewImmediate(view);
            } catch (IllegalArgumentException e) {
            } catch (Exception e2) {
                Log.e(TAG, "the exception happen in removeWindowView, e=" + e2.getMessage());
            }
        } else {
            mWindowManager2.removeView(view);
        }
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
            if (this.mIsSingleFlinger && !this.mGestureNavEnabled) {
                if (motionEvent.getActionMasked() == 0) {
                    Log.d(TAG, "touchDownIsValid MotionEvent.ACTION_DOWN ");
                    touchDownIsValidLazyMode(motionEvent.getRawX(), motionEvent.getRawY());
                }
                if (this.mIsValidLazyModeGesture) {
                    this.mSlideTouchEvent.handleTouchEvent(motionEvent);
                }
                if (this.mIsValidHiboardGesture && !isSuperPowerSaveMode() && !isInLockTaskMode() && !this.mIsStatusBarExplaned && !this.mDriveState) {
                    this.mSearchPanelView.handleGesture(motionEvent);
                }
            }
            if (!this.mIsValidHiboardGesture && motionEvent.getActionMasked() == 1 && this.mSearchPanelView != null) {
                this.mSearchPanelView.hideSearchPanelView();
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

    private boolean canAssistEnable() {
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean isNaviBarEnabled = FrontFingerPrintSettings.isNaviBarEnabled(resolver);
        boolean isSingleNavBarAIEnable = FrontFingerPrintSettings.isSingleNavBarAIEnable(resolver);
        boolean isSingleVirtualNavbarEnable = FrontFingerPrintSettings.isSingleVirtualNavbarEnable(resolver);
        boolean isNavShown = Settings.Global.getInt(resolver, "navigationbar_is_min", 0) == 0;
        Log.i(TAG, "canAssistEnable():isNaviBarEnabled(resolver)=" + isNaviBarEnabled + ";---isSingleNavBarAIEnable(resolver)" + isSingleNavBarAIEnable + ";isNavShown=" + isNavShown);
        if (!isNaviBarEnabled || (isSingleVirtualNavbarEnable && !isSingleNavBarAIEnable && isNavShown)) {
            return true;
        }
        return false;
    }

    private void touchDownIsValidLazyMode(float pointX, float pointY) {
        if (this.mPolicy.mDisplay == null || (this.mPolicy.mKeyguardDelegate.isShowing() && !this.mPolicy.mKeyguardDelegate.isOccluded())) {
            this.mIsValidLazyModeGesture = false;
            this.mIsValidHiboardGesture = false;
            return;
        }
        int HIT_REGION_TO_MAX_LAZYMODE = this.mContext.getResources().getDimensionPixelSize(17105186);
        int HIT_REGION_TO_MAX_HIBOARD = (int) (((double) this.mContext.getResources().getDimensionPixelSize(17105186)) / 4.0d);
        updateRealSize();
        if (this.isCoordinateForPad) {
            HIT_REGION_TO_MAX_HIBOARD *= 2;
        }
        boolean z = true;
        if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
        }
        int i = this.mPolicy.mNavigationBarPosition;
        PhoneWindowManager phoneWindowManager = this.mPolicy;
        if (i == 4) {
            this.mIsValidLazyModeGesture = ((pointY > ((float) (this.realSize.y - HIT_REGION_TO_MAX_LAZYMODE)) ? 1 : (pointY == ((float) (this.realSize.y - HIT_REGION_TO_MAX_LAZYMODE)) ? 0 : -1)) > 0 && ((pointX > ((float) HIT_REGION_TO_MAX_LAZYMODE) ? 1 : (pointX == ((float) HIT_REGION_TO_MAX_LAZYMODE) ? 0 : -1)) < 0 || (pointX > ((float) (this.realSize.x - HIT_REGION_TO_MAX_LAZYMODE)) ? 1 : (pointX == ((float) (this.realSize.x - HIT_REGION_TO_MAX_LAZYMODE)) ? 0 : -1)) > 0)) && canAssistEnable();
            int invalidPointX = this.realSize.x / 2;
            int invalidPointY = this.realSize.y;
            if (pointY <= ((float) (this.realSize.y - HIT_REGION_TO_MAX_HIBOARD)) || pointX == ((float) invalidPointX) || pointY == ((float) invalidPointY) || !canAssistEnable() || (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 0 && (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1 || this.mTrikeyNaviMode >= 0))) {
                z = false;
            }
            this.mIsValidHiboardGesture = z;
        } else {
            int invalidPointY2 = this.realSize.y / 2;
            int invalidPointX2 = this.realSize.x;
            this.mIsValidLazyModeGesture = false;
            if (pointX <= ((float) (this.realSize.x - HIT_REGION_TO_MAX_HIBOARD)) || pointX == ((float) invalidPointX2) || pointY == ((float) invalidPointY2) || !canAssistEnable() || (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 0 && (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1 || this.mTrikeyNaviMode >= 0))) {
                z = false;
            }
            this.mIsValidHiboardGesture = z;
            int i2 = invalidPointY2;
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
        boolean ret;
        boolean z = false;
        if (isNaviBarEnable() || this.mPolicy.mDisplay == null || event.getPointerCount() != 2 || ((this.mPolicy.mKeyguardDelegate.isShowing() && !this.mPolicy.mKeyguardDelegate.isOccluded()) || isSuperPowerSaveMode() || mDisableMultiWin)) {
            return false;
        }
        float pointX0 = event.getX(0);
        float pointY0 = event.getY(0);
        float pointX1 = event.getX(1);
        float pointY1 = event.getY(1);
        int navigation_bar_height = (int) (((double) this.mContext.getResources().getDimensionPixelSize(17105186)) / 4.0d);
        if (this.isCoordinateForPad) {
            navigation_bar_height *= 2;
        }
        updateRealSize();
        int i = this.mPolicy.mNavigationBarPosition;
        PhoneWindowManager phoneWindowManager = this.mPolicy;
        if (i == 4) {
            if (pointY0 > ((float) (this.realSize.y - navigation_bar_height)) && pointY1 > ((float) (this.realSize.y - navigation_bar_height))) {
                z = true;
            }
            ret = z;
        } else {
            if (pointX0 > ((float) (this.realSize.x - navigation_bar_height)) && pointX1 > ((float) (this.realSize.x - navigation_bar_height))) {
                z = true;
            }
            ret = z;
        }
        Log.d(TAG, "touchDownIsValidMultiWin ret = " + ret);
        return ret;
    }

    private boolean isInLockTaskMode() {
        return ((ActivityManager) this.mContext.getSystemService("activity")).isInLockTaskMode();
    }
}
