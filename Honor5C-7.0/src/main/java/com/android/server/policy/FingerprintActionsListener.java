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
    static final boolean DEBUG = true;
    private static final boolean ENABLE_MWSWITCH = true;
    private static final String FRONT_FINGERPRINT_SWAP_KEY_POSITION = "swap_key_position";
    static final int HIT_REGION_SCALE = 4;
    private static final boolean IS_CHINA_AREA = false;
    private static final int MSG_CLOSE_SEARCH_PANEL = 1;
    private static final String TAG = "FingerprintActionsListener";
    private Context mContext;
    private boolean mDeviceProvisioned;
    private Handler mHandler;
    private boolean mIsDoubleFlinger;
    private boolean mIsNeedHideMultiWindowView;
    private boolean mIsSingleFlinger;
    private boolean mIsStatusBarExplaned;
    private boolean mIsValidGesture;
    private boolean mIsValidHiboardGesture;
    private boolean mIsValidLazyModeGesture;
    private HwSplitScreenArrowView mLandMultiWinArrowView;
    private HwSplitScreenArrowView mMultiWinArrowView;
    private PhoneWindowManager mPolicy;
    private HwSplitScreenArrowView mPortMultiWinArrowView;
    private SearchPanelView mSearchPanelView;
    private SettingsObserver mSettingsObserver;
    private SlideTouchEvent mSlideTouchEvent;
    private int mTrikeyNaviMode;
    private WindowManager mWindowManager;
    private Point realSize;

    class SettingsObserver extends ContentObserver {
        final /* synthetic */ FingerprintActionsListener this$0;

        SettingsObserver(FingerprintActionsListener this$0, Handler handler) {
            boolean z = FingerprintActionsListener.IS_CHINA_AREA;
            this.this$0 = this$0;
            super(handler);
            registerContentObserver(UserHandle.myUserId());
            if (Secure.getIntForUser(this$0.mContext.getContentResolver(), "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0) {
                z = FingerprintActionsListener.ENABLE_MWSWITCH;
            }
            this$0.mDeviceProvisioned = z;
            this$0.mTrikeyNaviMode = System.getIntForUser(this$0.mContext.getContentResolver(), FingerprintActionsListener.FRONT_FINGERPRINT_SWAP_KEY_POSITION, FrontFingerPrintSettings.getDefaultNaviMode(), ActivityManager.getCurrentUser());
        }

        public void registerContentObserver(int userId) {
            this.this$0.mContext.getContentResolver().registerContentObserver(System.getUriFor(FingerprintActionsListener.FRONT_FINGERPRINT_SWAP_KEY_POSITION), FingerprintActionsListener.IS_CHINA_AREA, this, userId);
            this.this$0.mContext.getContentResolver().registerContentObserver(System.getUriFor("device_provisioned"), FingerprintActionsListener.IS_CHINA_AREA, this, userId);
        }

        public void onChange(boolean selfChange) {
            boolean z = FingerprintActionsListener.IS_CHINA_AREA;
            FingerprintActionsListener fingerprintActionsListener = this.this$0;
            if (Secure.getIntForUser(this.this$0.mContext.getContentResolver(), "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0) {
                z = FingerprintActionsListener.ENABLE_MWSWITCH;
            }
            fingerprintActionsListener.mDeviceProvisioned = z;
            this.this$0.mTrikeyNaviMode = System.getIntForUser(this.this$0.mContext.getContentResolver(), FingerprintActionsListener.FRONT_FINGERPRINT_SWAP_KEY_POSITION, FrontFingerPrintSettings.getDefaultNaviMode(), ActivityManager.getCurrentUser());
        }
    }

    private class StatusBarStatesChangedReceiver extends BroadcastReceiver {
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
            if (action != FingerprintActionsListener.HIT_REGION_SCALE && action != 0) {
                return FingerprintActionsListener.IS_CHINA_AREA;
            }
            FingerprintActionsListener.this.mHandler.removeMessages(this.mMsg);
            FingerprintActionsListener.this.mHandler.sendEmptyMessage(this.mMsg);
            return FingerprintActionsListener.ENABLE_MWSWITCH;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.policy.FingerprintActionsListener.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.policy.FingerprintActionsListener.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.FingerprintActionsListener.<clinit>():void");
    }

    public FingerprintActionsListener(Context context, PhoneWindowManager policy) {
        this.mContext = null;
        this.mPolicy = null;
        this.mIsValidGesture = IS_CHINA_AREA;
        this.mIsValidLazyModeGesture = IS_CHINA_AREA;
        this.mIsValidHiboardGesture = IS_CHINA_AREA;
        this.realSize = new Point();
        this.mSearchPanelView = null;
        this.mMultiWinArrowView = null;
        this.mPortMultiWinArrowView = null;
        this.mLandMultiWinArrowView = null;
        this.mIsSingleFlinger = IS_CHINA_AREA;
        this.mIsDoubleFlinger = IS_CHINA_AREA;
        this.mIsNeedHideMultiWindowView = IS_CHINA_AREA;
        this.mDeviceProvisioned = ENABLE_MWSWITCH;
        this.mTrikeyNaviMode = -1;
        this.mIsStatusBarExplaned = IS_CHINA_AREA;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case FingerprintActionsListener.MSG_CLOSE_SEARCH_PANEL /*1*/:
                        FingerprintActionsListener.this.hideSearchPanelView();
                    default:
                }
            }
        };
        this.mContext = context;
        this.mPolicy = policy;
        updateRealSize();
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mSlideTouchEvent = new SlideTouchEvent(context);
        this.mSettingsObserver = new SettingsObserver(this, this.mHandler);
        initView();
        initStatusBarReciver();
    }

    private void initStatusBarReciver() {
        if (this.mContext != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.android.systemui.statusbar.visible.change");
            this.mContext.registerReceiver(new StatusBarStatesChangedReceiver(), filter, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM", null);
            Log.i(TAG, "initStatusBarReciver completed");
        }
    }

    private void initView() {
        Point screenDims = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(screenDims);
        this.mSearchPanelView = (SearchPanelView) LayoutInflater.from(this.mContext).inflate(34013255, null);
        this.mPortMultiWinArrowView = (HwSplitScreenArrowView) LayoutInflater.from(this.mContext).inflate(34013263, null);
        if (this.mPortMultiWinArrowView != null) {
            this.mPortMultiWinArrowView.initViewParams(MSG_CLOSE_SEARCH_PANEL, screenDims);
        }
        this.mLandMultiWinArrowView = (HwSplitScreenArrowView) LayoutInflater.from(this.mContext).inflate(34013264, null);
        if (this.mLandMultiWinArrowView != null) {
            this.mLandMultiWinArrowView.initViewParams(2, new Point(screenDims.y, screenDims.x));
        }
    }

    public void createSearchPanelView() {
        if (this.mSearchPanelView != null) {
            this.mSearchPanelView.setOnTouchListener(new TouchOutsideListener(MSG_CLOSE_SEARCH_PANEL, this.mSearchPanelView));
            this.mSearchPanelView.setVisibility(8);
            addWindowView(this.mWindowManager, this.mSearchPanelView, getSearchLayoutParams(this.mSearchPanelView.getLayoutParams()));
            this.mSearchPanelView.initUI(this.mHandler.getLooper());
        }
    }

    public void destroySearchPanelView() {
        if (this.mSearchPanelView != null) {
            removeWindowView(this.mWindowManager, this.mSearchPanelView, ENABLE_MWSWITCH);
        }
    }

    public void createMultiWinArrowView() {
        if (ActivityManager.supportsMultiWindow()) {
            if (this.mMultiWinArrowView != null) {
                this.mMultiWinArrowView.removeViewToWindow();
            }
            if (MSG_CLOSE_SEARCH_PANEL == this.mContext.getResources().getConfiguration().orientation) {
                this.mMultiWinArrowView = this.mPortMultiWinArrowView;
            } else {
                this.mMultiWinArrowView = this.mLandMultiWinArrowView;
            }
            this.mMultiWinArrowView.addViewToWindow();
        }
    }

    public void destroyMultiWinArrowView() {
        if (ActivityManager.supportsMultiWindow() && this.mMultiWinArrowView != null) {
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
        return SystemProperties.getBoolean("sys.super_power_save", IS_CHINA_AREA);
    }

    public void setCurrentUser(int newUserId) {
        this.mSettingsObserver.registerContentObserver(newUserId);
        this.mSettingsObserver.onChange(ENABLE_MWSWITCH);
    }

    protected LayoutParams getSearchLayoutParams(ViewGroup.LayoutParams layoutParams) {
        LayoutParams lp = new LayoutParams(-1, -1, 2024, 8519936, -3);
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= HwGlobalActionsData.FLAG_SHUTDOWN;
        }
        lp.gravity = 8388691;
        lp.setTitle("Framework_SearchPanel");
        lp.windowAnimations = 16974579;
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
            if (motionEvent.getPointerCount() == MSG_CLOSE_SEARCH_PANEL) {
                this.mIsSingleFlinger = ENABLE_MWSWITCH;
                this.mIsDoubleFlinger = IS_CHINA_AREA;
            } else if (motionEvent.getPointerCount() == 2) {
                this.mIsDoubleFlinger = ENABLE_MWSWITCH;
                this.mIsSingleFlinger = IS_CHINA_AREA;
                this.mIsValidLazyModeGesture = IS_CHINA_AREA;
                this.mIsValidHiboardGesture = IS_CHINA_AREA;
            } else {
                this.mIsDoubleFlinger = IS_CHINA_AREA;
                this.mIsSingleFlinger = IS_CHINA_AREA;
                this.mIsNeedHideMultiWindowView = ENABLE_MWSWITCH;
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
                if (!(!this.mIsValidHiboardGesture || isSuperPowerSaveMode() || this.mIsStatusBarExplaned)) {
                    this.mSearchPanelView.handleGesture(motionEvent);
                }
            }
            if (this.mIsDoubleFlinger && !this.mIsNeedHideMultiWindowView) {
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
            if (motionEvent.getActionMasked() == MSG_CLOSE_SEARCH_PANEL) {
                reset();
            }
            if (motionEvent.getActionMasked() == 6) {
                this.mIsNeedHideMultiWindowView = ENABLE_MWSWITCH;
            }
        }
    }

    private void reset() {
        this.mIsValidGesture = IS_CHINA_AREA;
        this.mIsValidLazyModeGesture = IS_CHINA_AREA;
        this.mIsValidHiboardGesture = IS_CHINA_AREA;
        this.mIsNeedHideMultiWindowView = IS_CHINA_AREA;
    }

    private boolean isNaviBarEnable() {
        return FrontFingerPrintSettings.isNaviBarEnabled(this.mContext.getContentResolver());
    }

    private void touchDownIsValidLazyMode(float pointX, float pointY) {
        boolean z = ENABLE_MWSWITCH;
        if (this.mPolicy.mDisplay == null || (this.mPolicy.mKeyguardDelegate.isShowing() && !this.mPolicy.mKeyguardDelegate.isOccluded())) {
            this.mIsValidLazyModeGesture = IS_CHINA_AREA;
            this.mIsValidHiboardGesture = IS_CHINA_AREA;
            return;
        }
        int HIT_REGION_TO_MAX_LAZYMODE = (int) (((double) this.mContext.getResources().getDimensionPixelSize(17104920)) / 2.0d);
        int HIT_REGION_TO_MAX_HIBOARD = (int) (((double) this.mContext.getResources().getDimensionPixelSize(17104920)) / 4.0d);
        updateRealSize();
        if (this.mPolicy.mNavigationBarOnBottom) {
            boolean z2 = (pointY <= ((float) (this.realSize.y - HIT_REGION_TO_MAX_LAZYMODE)) || (pointX >= ((float) HIT_REGION_TO_MAX_LAZYMODE) && pointX <= ((float) (this.realSize.x - HIT_REGION_TO_MAX_LAZYMODE)))) ? IS_CHINA_AREA : isNaviBarEnable() ? IS_CHINA_AREA : ENABLE_MWSWITCH;
            this.mIsValidLazyModeGesture = z2;
            if (!this.mIsValidLazyModeGesture) {
                if (pointY <= ((float) (this.realSize.y - HIT_REGION_TO_MAX_HIBOARD)) || isNaviBarEnable() || pointX <= ((float) (HIT_REGION_TO_MAX_LAZYMODE * HIT_REGION_SCALE)) || pointX >= ((float) (this.realSize.x - (HIT_REGION_TO_MAX_LAZYMODE * HIT_REGION_SCALE)))) {
                    z = IS_CHINA_AREA;
                } else if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 0 && (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != MSG_CLOSE_SEARCH_PANEL || this.mTrikeyNaviMode >= 0)) {
                    z = IS_CHINA_AREA;
                }
                this.mIsValidHiboardGesture = z;
            }
        } else {
            this.mIsValidLazyModeGesture = IS_CHINA_AREA;
            if (pointX <= ((float) (this.realSize.x - HIT_REGION_TO_MAX_HIBOARD)) || isNaviBarEnable() || pointY <= ((float) (HIT_REGION_TO_MAX_LAZYMODE * HIT_REGION_SCALE)) || pointY >= ((float) (this.realSize.y - (HIT_REGION_TO_MAX_LAZYMODE * HIT_REGION_SCALE)))) {
                z = IS_CHINA_AREA;
            } else if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 0 && (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != MSG_CLOSE_SEARCH_PANEL || this.mTrikeyNaviMode >= 0)) {
                z = IS_CHINA_AREA;
            }
            this.mIsValidHiboardGesture = z;
        }
        if (this.mPolicy.isKeyguardLocked()) {
            this.mIsValidLazyModeGesture = IS_CHINA_AREA;
            this.mIsValidHiboardGesture = IS_CHINA_AREA;
        }
        Log.d(TAG, "touchDownIsValidLazyMode = " + this.mIsValidLazyModeGesture + "  touchDownIsValidHiBoard = " + this.mIsValidHiboardGesture);
    }

    private void updateRealSize() {
        if (this.mPolicy.mDisplay != null) {
            this.mPolicy.mDisplay.getRealSize(this.realSize);
        }
    }

    private boolean touchDownIsValidMultiWin(MotionEvent event) {
        if (isNaviBarEnable() || this.mPolicy.mDisplay == null || event.getPointerCount() != 2 || ((this.mPolicy.mKeyguardDelegate.isShowing() && !this.mPolicy.mKeyguardDelegate.isOccluded()) || isSuperPowerSaveMode())) {
            return IS_CHINA_AREA;
        }
        float pointX0 = event.getX(0);
        float pointY0 = event.getY(0);
        float pointX1 = event.getX(MSG_CLOSE_SEARCH_PANEL);
        float pointY1 = event.getY(MSG_CLOSE_SEARCH_PANEL);
        int navigation_bar_height = (int) (((double) this.mContext.getResources().getDimensionPixelSize(17104920)) / 4.0d);
        Point realSize = new Point();
        this.mPolicy.mDisplay.getRealSize(realSize);
        boolean ret = this.mPolicy.mNavigationBarOnBottom ? (pointY0 <= ((float) (realSize.y - navigation_bar_height)) || pointY1 <= ((float) (realSize.y - navigation_bar_height))) ? IS_CHINA_AREA : ENABLE_MWSWITCH : (pointX0 <= ((float) (realSize.x - navigation_bar_height)) || pointX1 <= ((float) (realSize.x - navigation_bar_height))) ? IS_CHINA_AREA : ENABLE_MWSWITCH;
        Log.d(TAG, "touchDownIsValidMultiWin ret = " + ret);
        return ret;
    }
}
