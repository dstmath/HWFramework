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
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.server.gesture.GestureNavConst;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.ActivityTaskManagerExt;
import com.huawei.android.app.WindowManagerExt;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.utils.HwPartResourceUtils;
import huawei.android.provider.FrontFingerPrintSettings;

public class FingerprintActionsListener extends DefaultFingerprintActionsListener {
    private static final String ACCESSIBILITY_SCREENREADER_ENABLED = "accessibility_screenreader_enabled";
    private static final boolean DISABLE_MULTIWIN = SystemPropertiesEx.getBoolean("ro.huawei.disable_multiwindow", false);
    private static final String FRONT_FINGERPRINT_SWAP_KEY_POSITION = "swap_key_position";
    private static final String GSETTINGS_VDRIVE_IS_RUN = "vdrive_is_run_state";
    static final int HIT_REGION_SCALE = 4;
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", ""));
    private static final int MSG_CLOSE_SEARCH_PANEL = 1;
    private static final String TAG = "FingerprintActionsListener";
    private static final String TALKBACK_COMPONENT_NAME = "com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService";
    private static final int VDRIVE_IS_RUN = 1;
    private static final int VDRIVE_IS_UNRUN = 0;
    private Context mContext = null;
    private ContentObserver mDisplayDensityObserver;
    private Handler mHandler = new Handler() {
        /* class com.android.server.policy.FingerprintActionsListener.AnonymousClass3 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                FingerprintActionsListener.this.hideSearchPanelView();
            }
        }
    };
    private boolean mIsCoordinateForPad = SystemPropertiesEx.getBoolean("ro.config.coordinateforpad", false);
    private boolean mIsDeviceProvisioned = true;
    private boolean mIsDoubleFlinger = false;
    private boolean mIsDriveState = false;
    private boolean mIsGestureNavEnabled;
    private boolean mIsNeedHideMultiWindowView = false;
    private boolean mIsSingleFlinger = false;
    private boolean mIsStatusBarExplaned = false;
    private boolean mIsTalkBackOn;
    private boolean mIsValidGesture = false;
    private boolean mIsValidHiboardGesture = false;
    private boolean mIsValidLazyModeGesture = false;
    private HwSplitScreenArrowView mLandMultiWinArrowView = null;
    private HwSplitScreenArrowView mMultiWinArrowView = null;
    private PhoneWindowManagerEx mPolicy = null;
    private HwSplitScreenArrowView mPortMultiWinArrowView = null;
    private Point mRealSize = new Point();
    private SearchPanelView mSearchPanelView = null;
    private SettingsObserver mSettingsObserver;
    private SlideTouchEvent mSlideTouchEvent;
    private int mTrikeyNaviMode = -1;
    private WindowManager mWindowManager;

    public FingerprintActionsListener(Context context, PhoneWindowManagerEx policy) {
        super(context, policy);
        this.mContext = context;
        this.mPolicy = policy;
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        updateRealSize();
        this.mSlideTouchEvent = new SlideTouchEvent(context);
        initialDensityObserver(this.mHandler);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        initView();
        initStatusBarReciver();
        initDriveStateReciver();
    }

    private void initialDensityObserver(Handler handler) {
        this.mDisplayDensityObserver = new ContentObserver(handler) {
            /* class com.android.server.policy.FingerprintActionsListener.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                Log.i(FingerprintActionsListener.TAG, "Density has been changed");
                FingerprintActionsListener.this.initView();
                FingerprintActionsListener.this.createSearchPanelView();
                FingerprintActionsListener.this.createMultiWinArrowView();
            }
        };
        ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), Settings.Secure.getUriFor("display_density_forced"), false, this.mDisplayDensityObserver, UserHandleEx.myUserId());
    }

    private void initDriveStateReciver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(GSETTINGS_VDRIVE_IS_RUN), false, new ContentObserver(new Handler()) {
            /* class com.android.server.policy.FingerprintActionsListener.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                int state = Settings.Global.getInt(FingerprintActionsListener.this.mContext.getContentResolver(), FingerprintActionsListener.GSETTINGS_VDRIVE_IS_RUN, 0);
                Log.i(FingerprintActionsListener.TAG, "mVDriveStateObserver onChange isSelfChange = " + isSelfChange + " state = " + state);
                if (state == 1) {
                    FingerprintActionsListener.this.mIsDriveState = true;
                } else {
                    FingerprintActionsListener.this.mIsDriveState = false;
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
    public class StatusBarStatesChangedReceiver extends BroadcastReceiver {
        private StatusBarStatesChangedReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "com.android.systemui.statusbar.visible.change".equals(intent.getAction()) && intent.getExtras() != null) {
                String visible = intent.getExtras().getString("visible");
                FingerprintActionsListener.this.mIsStatusBarExplaned = Boolean.valueOf(visible).booleanValue();
                Log.i(FingerprintActionsListener.TAG, "mIsStatusBarExplaned = " + FingerprintActionsListener.this.mIsStatusBarExplaned);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initView() {
        Point screenDims = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(screenDims);
        this.mSearchPanelView = LayoutInflater.from(this.mContext).inflate(34013255, (ViewGroup) null);
        this.mPortMultiWinArrowView = LayoutInflater.from(this.mContext).inflate(34013263, (ViewGroup) null);
        HwSplitScreenArrowView hwSplitScreenArrowView = this.mPortMultiWinArrowView;
        if (hwSplitScreenArrowView != null) {
            hwSplitScreenArrowView.initViewParams(1, screenDims);
        }
        this.mLandMultiWinArrowView = LayoutInflater.from(this.mContext).inflate(34013264, (ViewGroup) null);
        if (this.mLandMultiWinArrowView != null) {
            this.mLandMultiWinArrowView.initViewParams(2, new Point(screenDims.y, screenDims.x));
        }
    }

    public void createSearchPanelView() {
        SearchPanelView searchPanelView = this.mSearchPanelView;
        if (searchPanelView != null) {
            searchPanelView.setOnTouchListener(new TouchOutsideListener(1, searchPanelView));
            this.mSearchPanelView.setVisibility(8);
            addWindowView(this.mWindowManager, this.mSearchPanelView, getSearchLayoutParams(this.mSearchPanelView.getLayoutParams()));
            this.mSearchPanelView.initUI(this.mHandler.getLooper());
        }
    }

    public void destroySearchPanelView() {
        SearchPanelView searchPanelView = this.mSearchPanelView;
        if (searchPanelView != null) {
            removeWindowView(this.mWindowManager, searchPanelView, true);
        }
    }

    public void createMultiWinArrowView() {
        if (ActivityTaskManagerExt.supportsMultiWindow(this.mContext)) {
            HwSplitScreenArrowView hwSplitScreenArrowView = this.mMultiWinArrowView;
            if (hwSplitScreenArrowView != null) {
                hwSplitScreenArrowView.removeViewToWindow();
            }
            if (this.mContext.getResources().getConfiguration().orientation == 1) {
                this.mMultiWinArrowView = this.mPortMultiWinArrowView;
            } else {
                this.mMultiWinArrowView = this.mLandMultiWinArrowView;
            }
            this.mMultiWinArrowView.addViewToWindow();
        }
    }

    public void destroyMultiWinArrowView() {
        HwSplitScreenArrowView hwSplitScreenArrowView;
        if (ActivityTaskManagerExt.supportsMultiWindow(this.mContext) && (hwSplitScreenArrowView = this.mMultiWinArrowView) != null) {
            hwSplitScreenArrowView.removeViewToWindow();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideSearchPanelView() {
        try {
            if (this.mSearchPanelView != null) {
                this.mSearchPanelView.hideSearchPanelView();
            }
        } catch (Exception e) {
            Log.e(TAG, "hideSearchPanelView Exception");
        }
    }

    private boolean isSuperPowerSaveMode() {
        return SystemPropertiesEx.getBoolean(GestureNavConst.KEY_SUPER_SAVE_MODE, false);
    }

    final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            registerContentObserver(UserHandleEx.myUserId());
            updateCurrentSettigns();
        }

        public void registerContentObserver(int userId) {
            ContentResolverExt.registerContentObserver(FingerprintActionsListener.this.mContext.getContentResolver(), Settings.System.getUriFor("swap_key_position"), false, this, userId);
            ContentResolverExt.registerContentObserver(FingerprintActionsListener.this.mContext.getContentResolver(), Settings.System.getUriFor("device_provisioned"), false, this, userId);
            ContentResolverExt.registerContentObserver(FingerprintActionsListener.this.mContext.getContentResolver(), Settings.Secure.getUriFor(GestureNavConst.KEY_SECURE_GESTURE_NAVIGATION), false, this, userId);
            ContentResolverExt.registerContentObserver(FingerprintActionsListener.this.mContext.getContentResolver(), Settings.Secure.getUriFor("accessibility_enabled"), false, this, userId);
            ContentResolverExt.registerContentObserver(FingerprintActionsListener.this.mContext.getContentResolver(), Settings.Secure.getUriFor("enabled_accessibility_services"), false, this, userId);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            updateCurrentSettigns();
        }

        private void updateCurrentSettigns() {
            FingerprintActionsListener fingerprintActionsListener = FingerprintActionsListener.this;
            boolean z = false;
            if (SettingsEx.Secure.getIntForUser(fingerprintActionsListener.mContext.getContentResolver(), "device_provisioned", 0, ActivityManagerEx.getCurrentUser()) != 0) {
                z = true;
            }
            fingerprintActionsListener.mIsDeviceProvisioned = z;
            FingerprintActionsListener fingerprintActionsListener2 = FingerprintActionsListener.this;
            fingerprintActionsListener2.mTrikeyNaviMode = SettingsEx.System.getIntForUser(fingerprintActionsListener2.mContext.getContentResolver(), "swap_key_position", FrontFingerPrintSettings.getDefaultNaviMode(), ActivityManagerEx.getCurrentUser());
            FingerprintActionsListener fingerprintActionsListener3 = FingerprintActionsListener.this;
            fingerprintActionsListener3.mIsGestureNavEnabled = GestureNavConst.isGestureNavEnabled(fingerprintActionsListener3.mContext, -2);
            FingerprintActionsListener fingerprintActionsListener4 = FingerprintActionsListener.this;
            fingerprintActionsListener4.mIsTalkBackOn = fingerprintActionsListener4.isTalkBackServicesOn(fingerprintActionsListener4.mContext);
            Log.i(FingerprintActionsListener.TAG, "mIsTalkBackOn:" + FingerprintActionsListener.this.mIsTalkBackOn + ",mIsGestureNavEnabled:" + FingerprintActionsListener.this.mIsGestureNavEnabled);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTalkBackServicesOn(Context context) {
        if (context != null && SettingsEx.Secure.getIntForUser(context.getContentResolver(), ACCESSIBILITY_SCREENREADER_ENABLED, 0, ActivityManagerEx.getCurrentUser()) == 1) {
            return true;
        }
        return false;
    }

    public void setCurrentUser(int newUserId) {
        this.mSettingsObserver.registerContentObserver(newUserId);
        this.mSettingsObserver.onChange(true);
        SlideTouchEvent slideTouchEvent = this.mSlideTouchEvent;
        if (slideTouchEvent != null) {
            slideTouchEvent.updateSettings();
        }
    }

    public class TouchOutsideListener implements View.OnTouchListener {
        private int mMsg;

        public TouchOutsideListener(int msg, SearchPanelView panel) {
            this.mMsg = msg;
        }

        @Override // android.view.View.OnTouchListener
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

    /* access modifiers changed from: protected */
    public WindowManager.LayoutParams getSearchLayoutParams(ViewGroup.LayoutParams layoutParams) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, WindowManagerEx.LayoutParamsEx.getTypeNavigationBarPanel(), 8519936, -3);
        if (ActivityManagerEx.isHighEndGfx()) {
            lp.flags |= 16777216;
        }
        lp.gravity = 8388691;
        lp.setTitle("Framework_SearchPanel");
        lp.windowAnimations = HwPartResourceUtils.getResourceId("Animation_RecentApplications");
        lp.softInputMode = 49;
        return lp;
    }

    public void addWindowView(WindowManager windowManager, View view, WindowManager.LayoutParams params) {
        try {
            windowManager.addView(view, params);
        } catch (Exception e) {
            Log.e(TAG, "the exception happen in addWindowView.");
        }
    }

    public void removeWindowView(WindowManager windowManager, View view, boolean isImmediate) {
        if (view == null) {
            return;
        }
        if (isImmediate) {
            try {
                windowManager.removeViewImmediate(view);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "the IllegalArgumentException happen in removeWindowView.");
            } catch (Exception e2) {
                Log.e(TAG, "the exception happen in removeWindowView.");
            }
        } else {
            windowManager.removeView(view);
        }
    }

    public void onPointerEvent(MotionEvent motionEvent) {
        SearchPanelView searchPanelView;
        if (this.mIsDeviceProvisioned) {
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
                HwSplitScreenArrowView hwSplitScreenArrowView = this.mMultiWinArrowView;
                if (hwSplitScreenArrowView != null && hwSplitScreenArrowView.getVisibility() == 0) {
                    this.mMultiWinArrowView.setVisibility(8);
                }
            }
            if (this.mIsSingleFlinger && !this.mIsGestureNavEnabled) {
                if (motionEvent.getActionMasked() == 0) {
                    Log.d(TAG, "touchDownIsValid MotionEvent.ACTION_DOWN ");
                    touchDownIsValidLazyMode(motionEvent.getRawX(), motionEvent.getRawY());
                }
                if (this.mIsValidLazyModeGesture) {
                    this.mSlideTouchEvent.handleTouchEvent(motionEvent);
                }
                if (this.mIsValidHiboardGesture && !isSuperPowerSaveMode() && !isInLockTaskMode() && !this.mIsStatusBarExplaned && !this.mIsDriveState) {
                    this.mSearchPanelView.handleGesture(motionEvent);
                }
            }
            if (!this.mIsValidHiboardGesture && motionEvent.getActionMasked() == 1 && (searchPanelView = this.mSearchPanelView) != null) {
                searchPanelView.hideSearchPanelView();
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
        return !isNaviBarEnabled || (isSingleVirtualNavbarEnable && !isSingleNavBarAIEnable && isNavShown);
    }

    private void touchDownIsValidLazyMode(float pointX, float pointY) {
        if (this.mWindowManager.getDefaultDisplay() == null || (this.mPolicy.isShowing() && !this.mPolicy.isOccluded())) {
            this.mIsValidLazyModeGesture = false;
            this.mIsValidHiboardGesture = false;
            return;
        }
        int hitRegionToMaxLazyMode = this.mContext.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("navigation_bar_height"));
        int hitRegionToMaxHiboard = (int) (((double) this.mContext.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("navigation_bar_height"))) / 4.0d);
        updateRealSize();
        if (this.mIsCoordinateForPad) {
            hitRegionToMaxHiboard *= 2;
        }
        boolean z = true;
        if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
        }
        if (getNavigationBarPosition() == this.mPolicy.getNavigationBarBottom()) {
            this.mIsValidLazyModeGesture = ((pointY > ((float) (this.mRealSize.y - hitRegionToMaxLazyMode)) ? 1 : (pointY == ((float) (this.mRealSize.y - hitRegionToMaxLazyMode)) ? 0 : -1)) > 0 && ((pointX > ((float) hitRegionToMaxLazyMode) ? 1 : (pointX == ((float) hitRegionToMaxLazyMode) ? 0 : -1)) < 0 || (pointX > ((float) (this.mRealSize.x - hitRegionToMaxLazyMode)) ? 1 : (pointX == ((float) (this.mRealSize.x - hitRegionToMaxLazyMode)) ? 0 : -1)) > 0)) && canAssistEnable();
            int invalidPointX = this.mRealSize.x / 2;
            int invalidPointY = this.mRealSize.y;
            if (pointY <= ((float) (this.mRealSize.y - hitRegionToMaxHiboard)) || pointX == ((float) invalidPointX) || pointY == ((float) invalidPointY) || !canAssistEnable() || (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 0 && (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1 || this.mTrikeyNaviMode >= 0))) {
                z = false;
            }
            this.mIsValidHiboardGesture = z;
        } else {
            int invalidPointY2 = this.mRealSize.y / 2;
            int invalidPointX2 = this.mRealSize.x;
            this.mIsValidLazyModeGesture = false;
            if (pointX <= ((float) (this.mRealSize.x - hitRegionToMaxHiboard)) || pointX == ((float) invalidPointX2) || pointY == ((float) invalidPointY2) || !canAssistEnable() || (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 0 && (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1 || this.mTrikeyNaviMode >= 0))) {
                z = false;
            }
            this.mIsValidHiboardGesture = z;
        }
        if (this.mPolicy.isKeyguardLocked()) {
            this.mIsValidLazyModeGesture = false;
            this.mIsValidHiboardGesture = false;
        }
        Log.d(TAG, "touchDownIsValidLazyMode = " + this.mIsValidLazyModeGesture + "  touchDownIsValidHiBoard = " + this.mIsValidHiboardGesture);
    }

    private void updateRealSize() {
        WindowManager windowManager = this.mWindowManager;
        if (windowManager != null && windowManager.getDefaultDisplay() != null) {
            this.mWindowManager.getDefaultDisplay().getRealSize(this.mRealSize);
        }
    }

    private int getNavigationBarPosition() {
        try {
            return WindowManagerExt.getNavBarPosition(0);
        } catch (RemoteException e) {
            Log.e(TAG, "getNavigationBarPosition error.");
            return 0;
        }
    }

    private boolean touchDownIsValidMultiWin(MotionEvent event) {
        boolean ret;
        boolean z = false;
        if (isNaviBarEnable() || this.mWindowManager.getDefaultDisplay() == null || event.getPointerCount() != 2 || ((this.mPolicy.isShowing() && !this.mPolicy.isOccluded()) || isSuperPowerSaveMode() || DISABLE_MULTIWIN)) {
            return false;
        }
        float pointX0 = event.getX(0);
        float pointY0 = event.getY(0);
        float pointX1 = event.getX(1);
        float pointY1 = event.getY(1);
        int navigationBarHeight = (int) (((double) this.mContext.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("navigation_bar_height"))) / 4.0d);
        if (this.mIsCoordinateForPad) {
            navigationBarHeight *= 2;
        }
        updateRealSize();
        if (getNavigationBarPosition() == this.mPolicy.getNavigationBarBottom()) {
            if (pointY0 > ((float) (this.mRealSize.y - navigationBarHeight)) && pointY1 > ((float) (this.mRealSize.y - navigationBarHeight))) {
                z = true;
            }
            ret = z;
        } else {
            if (pointX0 > ((float) (this.mRealSize.x - navigationBarHeight)) && pointX1 > ((float) (this.mRealSize.x - navigationBarHeight))) {
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
