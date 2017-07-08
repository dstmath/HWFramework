package com.android.server.input;

import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.service.dreams.DreamManagerInternal;
import android.util.Log;
import android.util.Slog;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.WindowManagerPolicy;
import com.android.server.DisplayThread;
import com.android.server.LocalServices;
import com.android.server.PPPOEStateMachine;
import com.android.server.am.ActivityRecord;
import com.android.server.pm.auth.HwCertification;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.wm.WindowState;
import com.huawei.android.os.BuildEx.VERSION;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.PGPlug;
import huawei.android.os.HwGeneralManager;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.List;

public class HwInputManagerService extends InputManagerService {
    private static final String App_Left = "pressure_launch_app_left";
    private static final String App_Right = "pressure_launch_app_right";
    static final boolean ENABLE_BACK_TO_HOME = true;
    static final boolean ENABLE_LOCK_DEVICE = false;
    static final String FINGERPRINT_ANSWER_CALL = "fp_answer_call";
    static final String FINGERPRINT_BACK_TO_HOME = "fp_return_desk";
    static final String FINGERPRINT_CAMERA_SWITCH = "fp_take_photo";
    static final String FINGERPRINT_GALLERY_SLIDE = "fingerprint_gallery_slide";
    static final String FINGERPRINT_GO_BACK = "fp_go_back";
    static final String FINGERPRINT_LOCK_DEVICE = "fp_lock_device";
    static final String FINGERPRINT_MARKET_DEMO_SWITCH = "fingerprint_market_demo_switch";
    static final String FINGERPRINT_RECENT_APP = "fp_recent_application";
    static final String FINGERPRINT_SHOW_NOTIFICATION = "fp_show_notification";
    static final String FINGERPRINT_SLIDE_SWITCH = "fingerprint_slide_switch";
    static final String FINGERPRINT_STOP_ALARM = "fp_stop_alarm";
    private static final boolean FRONT_FINGERPRINT_NAVIGATION;
    private static final int GOHOME_TIMEOUT_DELAY = 15000;
    private static final int HT_STATE_DISABLE = 1;
    private static final int HT_STATE_GOING_HOME = 4;
    private static final int HT_STATE_IDLE = 3;
    private static final int HT_STATE_NO_INIT = 2;
    private static final int HT_STATE_UNKOWN = 0;
    private static final boolean HW_DEBUG = false;
    private static final boolean IS_CHINA_AREA;
    private static final int MODAL_WINDOW_MASK = 40;
    private boolean IS_SUPPORT_PRESSURE;
    private String TAGPRESSURE;
    private boolean isSupportPg;
    boolean mAnswerCall;
    private ContentObserver mAppLeftStartObserver;
    private ContentObserver mAppRightStartObserver;
    boolean mBackToHome;
    private InputWindowHandle mCurFocusedWindowHandle;
    int mCurrentUserId;
    private DreamManagerInternal mDreamManagerInternal;
    FingerPressNavigation mFingerPressNavi;
    boolean mFingerprintMarketDemoSwitch;
    private FingerprintNavigation mFingerprintNavigationFilter;
    private Object mFreezeDetectLock;
    boolean mGallerySlide;
    boolean mGoBack;
    HwGuiShou mGuiShou;
    private Handler mHandler;
    boolean mHasReadDB;
    private int mHeight;
    private ComponentName mHomeComponent;
    private Runnable mHomeResumeChecker;
    private Handler mHomeTraceHandler;
    private int mHomeTraceState;
    boolean mInjectCamera;
    private boolean mLastImmersiveMode;
    private ContentObserver mMinNaviObserver;
    private boolean mModalWindowOnTop;
    private ContentObserver mNaviBarPosObserver;
    private PGPlug mPGPlug;
    private PgEventProcesser mPgEventProcesser;
    private WindowManagerPolicy mPolicy;
    private ContentObserver mPressLimitObserver;
    private boolean mPressNaviEnable;
    private ContentObserver mPressNaviObserver;
    boolean mRecentApp;
    private final ContentResolver mResolver;
    final SettingsObserver mSettingsObserver;
    boolean mShowNotification;
    boolean mStopAlarm;
    private int mWidth;
    private String needTip;

    /* renamed from: com.android.server.input.HwInputManagerService.2 */
    class AnonymousClass2 extends ContentObserver {
        AnonymousClass2(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            HwInputManagerService.this.handleNaviChangeForTip();
            if (HwInputManagerService.IS_CHINA_AREA) {
                HwInputManagerService.this.handleNaviChangeForChina();
            } else {
                HwInputManagerService.this.handleNaviChangeForOther();
            }
        }
    }

    /* renamed from: com.android.server.input.HwInputManagerService.3 */
    class AnonymousClass3 extends ContentObserver {
        AnonymousClass3(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            if (HwInputManagerService.IS_CHINA_AREA) {
                HwInputManagerService.this.handleNaviChangeForChina();
            }
        }
    }

    /* renamed from: com.android.server.input.HwInputManagerService.4 */
    class AnonymousClass4 extends ContentObserver {
        AnonymousClass4(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            float limit = System.getFloatForUser(HwInputManagerService.this.mContext.getContentResolver(), "pressure_habit_threshold", HwCircleAnimation.BG_ALPHA_FILL, ActivityManager.getCurrentUser());
            Slog.d(HwInputManagerService.this.TAGPRESSURE, "mPressLimitObserver onChange open limit = " + limit);
            if (HwInputManagerService.this.mFingerPressNavi != null) {
                HwInputManagerService.this.mFingerPressNavi.setPressureLimit(limit);
            }
        }
    }

    /* renamed from: com.android.server.input.HwInputManagerService.5 */
    class AnonymousClass5 extends ContentObserver {
        AnonymousClass5(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            int naviBarPos = System.getIntForUser(HwInputManagerService.this.mContext.getContentResolver(), "virtual_key_type", HwInputManagerService.HT_STATE_UNKOWN, ActivityManager.getCurrentUser());
            Slog.d(HwInputManagerService.this.TAGPRESSURE, "mPressNaviObserver onChange open naviBarPos = " + naviBarPos);
            if (HwInputManagerService.this.mFingerPressNavi != null) {
                HwInputManagerService.this.mFingerPressNavi.setNaviBarPosition(naviBarPos);
            }
        }
    }

    /* renamed from: com.android.server.input.HwInputManagerService.6 */
    class AnonymousClass6 extends ContentObserver {
        AnonymousClass6(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            String appLeftPkg = System.getStringForUser(HwInputManagerService.this.mContext.getContentResolver(), HwInputManagerService.App_Left, ActivityManager.getCurrentUser());
            Slog.d(HwInputManagerService.this.TAGPRESSURE, "AppStartObserver onChange open appLeftPkg = " + appLeftPkg);
            if (HwInputManagerService.this.mFingerPressNavi != null && appLeftPkg != null) {
                HwInputManagerService.this.mFingerPressNavi.setAppLeftPkg(appLeftPkg);
            }
        }
    }

    /* renamed from: com.android.server.input.HwInputManagerService.7 */
    class AnonymousClass7 extends ContentObserver {
        AnonymousClass7(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            String appRightPkg = System.getStringForUser(HwInputManagerService.this.mContext.getContentResolver(), HwInputManagerService.App_Right, ActivityManager.getCurrentUser());
            Slog.d(HwInputManagerService.this.TAGPRESSURE, "AppStartObserver onChange open appRightPkg = " + appRightPkg);
            if (HwInputManagerService.this.mFingerPressNavi != null && appRightPkg != null) {
                HwInputManagerService.this.mFingerPressNavi.setAppRightPkg(appRightPkg);
            }
        }
    }

    private class BroadcastThread extends Thread {
        boolean mMinNaviBar;

        private BroadcastThread() {
        }

        public void setMiniNaviBar(boolean minNaviBar) {
            this.mMinNaviBar = minNaviBar;
        }

        public void run() {
            Log.d(HwInputManagerService.this.TAGPRESSURE, "sendBroadcast minNaviBar = " + this.mMinNaviBar);
            Intent intent = new Intent("com.huawei.navigationbar.statuschange");
            intent.putExtra("minNavigationBar", this.mMinNaviBar);
            HwInputManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private class PgEventProcesser implements IPGPlugCallbacks {
        private PgEventProcesser() {
        }

        public void onDaemonConnected() {
        }

        public void onConnectedTimeout() {
        }

        public boolean onEvent(int actionID, String msg) {
            if (PGAction.checkActionType(actionID) == HwInputManagerService.HT_STATE_DISABLE && PGAction.checkActionFlag(actionID) == HwInputManagerService.HT_STATE_IDLE) {
                if (actionID == 10002 || actionID == 10011) {
                    if (HwInputManagerService.this.mFingerPressNavi != null) {
                        HwInputManagerService.this.mFingerPressNavi.setGameScene(HwInputManagerService.ENABLE_BACK_TO_HOME);
                    }
                } else if (HwInputManagerService.this.mFingerPressNavi != null) {
                    HwInputManagerService.this.mFingerPressNavi.setGameScene(HwInputManagerService.IS_CHINA_AREA);
                }
            }
            return HwInputManagerService.ENABLE_BACK_TO_HOME;
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            registerContentObserver(UserHandle.myUserId());
        }

        public void registerContentObserver(int userId) {
            if (!HwGuiShou.isGuiShouEnabled()) {
                HwInputManagerService.this.mResolver.registerContentObserver(Secure.getUriFor(HwInputManagerService.FINGERPRINT_SLIDE_SWITCH), HwInputManagerService.IS_CHINA_AREA, this, userId);
            }
            HwInputManagerService.this.mResolver.registerContentObserver(Secure.getUriFor(HwInputManagerService.FINGERPRINT_CAMERA_SWITCH), HwInputManagerService.IS_CHINA_AREA, this, userId);
            HwInputManagerService.this.mResolver.registerContentObserver(Secure.getUriFor(HwInputManagerService.FINGERPRINT_ANSWER_CALL), HwInputManagerService.IS_CHINA_AREA, this, userId);
            HwInputManagerService.this.mResolver.registerContentObserver(Secure.getUriFor(HwInputManagerService.FINGERPRINT_SHOW_NOTIFICATION), HwInputManagerService.IS_CHINA_AREA, this, userId);
            HwInputManagerService.this.mResolver.registerContentObserver(Secure.getUriFor(HwInputManagerService.FINGERPRINT_BACK_TO_HOME), HwInputManagerService.IS_CHINA_AREA, this);
            HwInputManagerService.this.mResolver.registerContentObserver(Secure.getUriFor(HwInputManagerService.FINGERPRINT_STOP_ALARM), HwInputManagerService.IS_CHINA_AREA, this, userId);
            HwInputManagerService.this.mResolver.registerContentObserver(Secure.getUriFor(HwInputManagerService.FINGERPRINT_LOCK_DEVICE), HwInputManagerService.IS_CHINA_AREA, this);
            HwInputManagerService.this.mResolver.registerContentObserver(Secure.getUriFor(HwInputManagerService.FINGERPRINT_GO_BACK), HwInputManagerService.IS_CHINA_AREA, this);
            HwInputManagerService.this.mResolver.registerContentObserver(Secure.getUriFor(HwInputManagerService.FINGERPRINT_RECENT_APP), HwInputManagerService.IS_CHINA_AREA, this);
            HwInputManagerService.this.mResolver.registerContentObserver(System.getUriFor(HwInputManagerService.FINGERPRINT_MARKET_DEMO_SWITCH), HwInputManagerService.IS_CHINA_AREA, this);
            HwInputManagerService.this.mResolver.registerContentObserver(Secure.getUriFor(HwInputManagerService.FINGERPRINT_GALLERY_SLIDE), HwInputManagerService.IS_CHINA_AREA, this, userId);
        }

        public void onChange(boolean selfChange) {
            HwInputManagerService hwInputManagerService;
            boolean z;
            Slog.d("InputManager", "SettingDB has Changed");
            if (!HwGuiShou.isGuiShouEnabled()) {
                hwInputManagerService = HwInputManagerService.this;
                if (Secure.getIntForUser(HwInputManagerService.this.mResolver, HwInputManagerService.FINGERPRINT_CAMERA_SWITCH, HwInputManagerService.HT_STATE_DISABLE, ActivityManager.getCurrentUser()) != 0) {
                    z = HwInputManagerService.ENABLE_BACK_TO_HOME;
                } else {
                    z = HwInputManagerService.IS_CHINA_AREA;
                }
                hwInputManagerService.mInjectCamera = z;
                hwInputManagerService = HwInputManagerService.this;
                if (Secure.getIntForUser(HwInputManagerService.this.mResolver, HwInputManagerService.FINGERPRINT_ANSWER_CALL, HwInputManagerService.HT_STATE_UNKOWN, ActivityManager.getCurrentUser()) != 0) {
                    z = HwInputManagerService.ENABLE_BACK_TO_HOME;
                } else {
                    z = HwInputManagerService.IS_CHINA_AREA;
                }
                hwInputManagerService.mAnswerCall = z;
                hwInputManagerService = HwInputManagerService.this;
                if (Secure.getIntForUser(HwInputManagerService.this.mResolver, HwInputManagerService.FINGERPRINT_STOP_ALARM, HwInputManagerService.HT_STATE_UNKOWN, ActivityManager.getCurrentUser()) != 0) {
                    z = HwInputManagerService.ENABLE_BACK_TO_HOME;
                } else {
                    z = HwInputManagerService.IS_CHINA_AREA;
                }
                hwInputManagerService.mStopAlarm = z;
            }
            hwInputManagerService = HwInputManagerService.this;
            if (Secure.getIntForUser(HwInputManagerService.this.mResolver, HwInputManagerService.FINGERPRINT_SHOW_NOTIFICATION, HwInputManagerService.HT_STATE_UNKOWN, ActivityManager.getCurrentUser()) != 0) {
                z = HwInputManagerService.ENABLE_BACK_TO_HOME;
            } else {
                z = HwInputManagerService.IS_CHINA_AREA;
            }
            hwInputManagerService.mShowNotification = z;
            hwInputManagerService = HwInputManagerService.this;
            if (Secure.getInt(HwInputManagerService.this.mResolver, HwInputManagerService.FINGERPRINT_BACK_TO_HOME, HwInputManagerService.HT_STATE_UNKOWN) != 0) {
                z = HwInputManagerService.ENABLE_BACK_TO_HOME;
            } else {
                z = HwInputManagerService.IS_CHINA_AREA;
            }
            hwInputManagerService.mBackToHome = z;
            hwInputManagerService = HwInputManagerService.this;
            if (Secure.getInt(HwInputManagerService.this.mResolver, HwInputManagerService.FINGERPRINT_GO_BACK, HwInputManagerService.HT_STATE_UNKOWN) != 0) {
                z = HwInputManagerService.ENABLE_BACK_TO_HOME;
            } else {
                z = HwInputManagerService.IS_CHINA_AREA;
            }
            hwInputManagerService.mGoBack = z;
            hwInputManagerService = HwInputManagerService.this;
            if (Secure.getInt(HwInputManagerService.this.mResolver, HwInputManagerService.FINGERPRINT_RECENT_APP, HwInputManagerService.HT_STATE_UNKOWN) != 0) {
                z = HwInputManagerService.ENABLE_BACK_TO_HOME;
            } else {
                z = HwInputManagerService.IS_CHINA_AREA;
            }
            hwInputManagerService.mRecentApp = z;
            hwInputManagerService = HwInputManagerService.this;
            if (System.getInt(HwInputManagerService.this.mResolver, HwInputManagerService.FINGERPRINT_MARKET_DEMO_SWITCH, HwInputManagerService.HT_STATE_UNKOWN) == HwInputManagerService.HT_STATE_DISABLE) {
                z = HwInputManagerService.ENABLE_BACK_TO_HOME;
            } else {
                z = HwInputManagerService.IS_CHINA_AREA;
            }
            hwInputManagerService.mFingerprintMarketDemoSwitch = z;
            hwInputManagerService = HwInputManagerService.this;
            if (Secure.getIntForUser(HwInputManagerService.this.mResolver, HwInputManagerService.FINGERPRINT_GALLERY_SLIDE, HwInputManagerService.HT_STATE_DISABLE, ActivityManager.getCurrentUser()) != 0) {
                z = HwInputManagerService.ENABLE_BACK_TO_HOME;
            } else {
                z = HwInputManagerService.IS_CHINA_AREA;
            }
            hwInputManagerService.mGallerySlide = z;
            if (HwInputManagerService.this.mShowNotification || HwInputManagerService.this.mBackToHome || HwInputManagerService.this.mGoBack || HwInputManagerService.this.mRecentApp || HwInputManagerService.this.mFingerprintMarketDemoSwitch || HwInputManagerService.this.mGallerySlide || !HwGuiShou.isFPNavigationInThreeAppsEnabled(HwInputManagerService.this.mInjectCamera, HwInputManagerService.this.mAnswerCall, HwInputManagerService.this.mStopAlarm) || HwInputManagerService.FRONT_FINGERPRINT_NAVIGATION) {
                Slog.d("InputManager", "open fingerprint nav");
                System.putInt(HwInputManagerService.this.mResolver, HwInputManagerService.FINGERPRINT_SLIDE_SWITCH, HwInputManagerService.HT_STATE_DISABLE);
                HwInputManagerService.this.mGuiShou.setFingerprintSlideSwitchValue(HwInputManagerService.ENABLE_BACK_TO_HOME);
                return;
            }
            Slog.d("InputManager", "close fingerprint nav");
            System.putInt(HwInputManagerService.this.mResolver, HwInputManagerService.FINGERPRINT_SLIDE_SWITCH, HwInputManagerService.HT_STATE_UNKOWN);
            HwInputManagerService.this.mGuiShou.setFingerprintSlideSwitchValue(HwInputManagerService.IS_CHINA_AREA);
        }
    }

    static {
        FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", IS_CHINA_AREA);
        IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", AppHibernateCst.INVALID_PKG));
    }

    public HwInputManagerService(Context context, Handler handler) {
        super(context);
        this.mHasReadDB = IS_CHINA_AREA;
        this.mShowNotification = IS_CHINA_AREA;
        this.mBackToHome = IS_CHINA_AREA;
        this.mGoBack = IS_CHINA_AREA;
        this.mRecentApp = IS_CHINA_AREA;
        this.mFingerprintMarketDemoSwitch = IS_CHINA_AREA;
        this.mPgEventProcesser = new PgEventProcesser();
        this.mCurrentUserId = HT_STATE_UNKOWN;
        this.mInjectCamera = IS_CHINA_AREA;
        this.mAnswerCall = IS_CHINA_AREA;
        this.mStopAlarm = IS_CHINA_AREA;
        this.mHomeComponent = null;
        this.mFreezeDetectLock = new Object();
        this.mHomeTraceState = HT_STATE_UNKOWN;
        this.mDreamManagerInternal = null;
        this.mPolicy = null;
        this.mModalWindowOnTop = IS_CHINA_AREA;
        this.mCurFocusedWindowHandle = null;
        this.mHomeTraceHandler = null;
        this.mLastImmersiveMode = IS_CHINA_AREA;
        this.mPressNaviEnable = IS_CHINA_AREA;
        this.TAGPRESSURE = "pressure:hwInputMS";
        this.needTip = "pressure_needTip";
        this.IS_SUPPORT_PRESSURE = IS_CHINA_AREA;
        this.isSupportPg = ENABLE_BACK_TO_HOME;
        this.mFingerPressNavi = null;
        this.mHomeResumeChecker = new Runnable() {
            public void run() {
                HwInputManagerService.this.handleGoHomeTimeout();
            }
        };
        this.mFingerprintNavigationFilter = new FingerprintNavigation(this.mContext);
        this.mResolver = context.getContentResolver();
        this.mSettingsObserver = new SettingsObserver(handler);
        this.mHandler = handler;
        this.mGuiShou = new HwGuiShou(this, context, handler, this.mFingerprintNavigationFilter);
        if (!this.mHasReadDB) {
            boolean z;
            this.mHasReadDB = ENABLE_BACK_TO_HOME;
            HwGuiShou hwGuiShou = this.mGuiShou;
            if (!HwGuiShou.isGuiShouEnabled()) {
                this.mInjectCamera = Secure.getIntForUser(this.mResolver, FINGERPRINT_CAMERA_SWITCH, HT_STATE_DISABLE, ActivityManager.getCurrentUser()) != 0 ? ENABLE_BACK_TO_HOME : IS_CHINA_AREA;
                if (Secure.getIntForUser(this.mResolver, FINGERPRINT_ANSWER_CALL, HT_STATE_UNKOWN, ActivityManager.getCurrentUser()) != 0) {
                    z = ENABLE_BACK_TO_HOME;
                } else {
                    z = IS_CHINA_AREA;
                }
                this.mAnswerCall = z;
                if (Secure.getIntForUser(this.mResolver, FINGERPRINT_STOP_ALARM, HT_STATE_UNKOWN, ActivityManager.getCurrentUser()) != 0) {
                    z = ENABLE_BACK_TO_HOME;
                } else {
                    z = IS_CHINA_AREA;
                }
                this.mStopAlarm = z;
            }
            if (Secure.getIntForUser(this.mResolver, FINGERPRINT_SHOW_NOTIFICATION, HT_STATE_UNKOWN, ActivityManager.getCurrentUser()) != 0) {
                z = ENABLE_BACK_TO_HOME;
            } else {
                z = IS_CHINA_AREA;
            }
            this.mShowNotification = z;
            if (Secure.getIntForUser(this.mResolver, FINGERPRINT_BACK_TO_HOME, HT_STATE_UNKOWN, ActivityManager.getCurrentUser()) != 0) {
                z = ENABLE_BACK_TO_HOME;
            } else {
                z = IS_CHINA_AREA;
            }
            this.mBackToHome = z;
            if (Secure.getIntForUser(this.mResolver, FINGERPRINT_GO_BACK, HT_STATE_UNKOWN, ActivityManager.getCurrentUser()) != 0) {
                z = ENABLE_BACK_TO_HOME;
            } else {
                z = IS_CHINA_AREA;
            }
            this.mGoBack = z;
            if (Secure.getIntForUser(this.mResolver, FINGERPRINT_RECENT_APP, HT_STATE_UNKOWN, ActivityManager.getCurrentUser()) != 0) {
                z = ENABLE_BACK_TO_HOME;
            } else {
                z = IS_CHINA_AREA;
            }
            this.mRecentApp = z;
            if (System.getInt(this.mResolver, FINGERPRINT_MARKET_DEMO_SWITCH, HT_STATE_UNKOWN) == HT_STATE_DISABLE) {
                z = ENABLE_BACK_TO_HOME;
            } else {
                z = IS_CHINA_AREA;
            }
            this.mFingerprintMarketDemoSwitch = z;
            if (Secure.getIntForUser(this.mResolver, FINGERPRINT_GALLERY_SLIDE, HT_STATE_DISABLE, ActivityManager.getCurrentUser()) != 0) {
                z = ENABLE_BACK_TO_HOME;
            } else {
                z = IS_CHINA_AREA;
            }
            this.mGallerySlide = z;
            if (!(this.mShowNotification || this.mBackToHome || this.mGoBack || this.mRecentApp || this.mFingerprintMarketDemoSwitch || this.mGallerySlide)) {
                hwGuiShou = this.mGuiShou;
                if (HwGuiShou.isFPNavigationInThreeAppsEnabled(this.mInjectCamera, this.mAnswerCall, this.mStopAlarm) && !FRONT_FINGERPRINT_NAVIGATION) {
                    Slog.d("InputManager", "close fingerprint nav");
                    System.putInt(this.mResolver, FINGERPRINT_SLIDE_SWITCH, HT_STATE_UNKOWN);
                    this.mGuiShou.setFingerprintSlideSwitchValue(IS_CHINA_AREA);
                    this.mGuiShou.registerPhoneStateListener();
                }
            }
            Slog.d("InputManager", "open fingerprint nav");
            System.putInt(this.mResolver, FINGERPRINT_SLIDE_SWITCH, HT_STATE_DISABLE);
            this.mGuiShou.setFingerprintSlideSwitchValue(ENABLE_BACK_TO_HOME);
            this.mGuiShou.registerPhoneStateListener();
        }
        this.mGuiShou.registerFpSlideSwitchSettingsObserver();
        this.mCurrentUserId = ActivityManager.getCurrentUser();
    }

    private boolean isPressureModeOpenForChina() {
        return HT_STATE_DISABLE == System.getIntForUser(this.mResolver, "virtual_notification_key_type", HT_STATE_UNKOWN, this.mCurrentUserId) ? ENABLE_BACK_TO_HOME : IS_CHINA_AREA;
    }

    private boolean isPressureModeOpenForOther() {
        return HT_STATE_DISABLE == System.getIntForUser(this.mResolver, "virtual_notification_key_type", HT_STATE_UNKOWN, this.mCurrentUserId) ? ENABLE_BACK_TO_HOME : IS_CHINA_AREA;
    }

    private boolean isNavigationBarMin() {
        return HT_STATE_DISABLE == Global.getInt(this.mResolver, "navigationbar_is_min", HT_STATE_UNKOWN) ? ENABLE_BACK_TO_HOME : IS_CHINA_AREA;
    }

    private void initObserver(Handler handler) {
        this.mPressNaviObserver = new AnonymousClass2(handler);
        this.mMinNaviObserver = new AnonymousClass3(handler);
        this.mPressLimitObserver = new AnonymousClass4(handler);
        this.mNaviBarPosObserver = new AnonymousClass5(handler);
        this.mAppLeftStartObserver = new AnonymousClass6(handler);
        this.mAppRightStartObserver = new AnonymousClass7(handler);
        this.mResolver.registerContentObserver(System.getUriFor("virtual_notification_key_type"), IS_CHINA_AREA, this.mPressNaviObserver, -1);
        this.mResolver.registerContentObserver(Global.getUriFor("navigationbar_is_min"), IS_CHINA_AREA, this.mMinNaviObserver, -1);
        this.mResolver.registerContentObserver(System.getUriFor("pressure_habit_threshold"), IS_CHINA_AREA, this.mPressLimitObserver, -1);
        this.mResolver.registerContentObserver(System.getUriFor("virtual_key_type"), IS_CHINA_AREA, this.mNaviBarPosObserver, -1);
        this.mResolver.registerContentObserver(System.getUriFor(App_Left), IS_CHINA_AREA, this.mAppLeftStartObserver, -1);
        this.mResolver.registerContentObserver(System.getUriFor(App_Right), IS_CHINA_AREA, this.mAppRightStartObserver, -1);
    }

    private void handleNaviChangeForChina() {
        if (this.IS_SUPPORT_PRESSURE) {
            if (isPressureModeOpenForChina() && isNavigationBarMin()) {
                Slog.d(this.TAGPRESSURE, "mPressNaviObserver onChange open");
                this.mPressNaviEnable = ENABLE_BACK_TO_HOME;
                this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
                if (this.mFingerPressNavi != null) {
                    this.mFingerPressNavi.createPointerCircleAnimation();
                    this.mFingerPressNavi.setDisplayWidthAndHeight(this.mWidth, this.mHeight);
                    this.mFingerPressNavi.setMode(HT_STATE_DISABLE);
                }
                SystemProperties.set("persist.sys.fingerpressnavi", PPPOEStateMachine.PHASE_INITIALIZE);
            } else {
                Slog.d(this.TAGPRESSURE, "mPressNaviObserver onChange close");
                this.mPressNaviEnable = IS_CHINA_AREA;
                this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
                if (this.mFingerPressNavi != null) {
                    this.mFingerPressNavi.destoryPointerCircleAnimation();
                    this.mFingerPressNavi.setMode(HT_STATE_UNKOWN);
                }
                SystemProperties.set("persist.sys.fingerpressnavi", PPPOEStateMachine.PHASE_INITIALIZE);
            }
            int naviBarPos = System.getIntForUser(this.mContext.getContentResolver(), "virtual_key_type", HT_STATE_UNKOWN, this.mCurrentUserId);
            if (this.mFingerPressNavi != null) {
                this.mFingerPressNavi.setNaviBarPosition(naviBarPos);
            }
        }
    }

    private void handleNaviChangeForOther() {
        if (this.IS_SUPPORT_PRESSURE) {
            if (isPressureModeOpenForOther()) {
                Slog.d(this.TAGPRESSURE, "mPressNaviObserver onChange open");
                this.mPressNaviEnable = ENABLE_BACK_TO_HOME;
                this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
                if (this.mFingerPressNavi != null) {
                    this.mFingerPressNavi.createPointerCircleAnimation();
                    this.mFingerPressNavi.setDisplayWidthAndHeight(this.mWidth, this.mHeight);
                    this.mFingerPressNavi.setMode(HT_STATE_DISABLE);
                }
                sendBroadcast(ENABLE_BACK_TO_HOME);
                SystemProperties.set("persist.sys.fingerpressnavi", PPPOEStateMachine.PHASE_INITIALIZE);
            } else {
                Slog.d(this.TAGPRESSURE, "mPressNaviObserver onChange close");
                this.mPressNaviEnable = IS_CHINA_AREA;
                this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
                if (this.mFingerPressNavi != null) {
                    this.mFingerPressNavi.destoryPointerCircleAnimation();
                    this.mFingerPressNavi.setMode(HT_STATE_UNKOWN);
                }
                sendBroadcast(IS_CHINA_AREA);
                SystemProperties.set("persist.sys.fingerpressnavi", PPPOEStateMachine.PHASE_INITIALIZE);
            }
            int naviBarPos = System.getIntForUser(this.mContext.getContentResolver(), "virtual_key_type", HT_STATE_UNKOWN, this.mCurrentUserId);
            if (this.mFingerPressNavi != null) {
                this.mFingerPressNavi.setNaviBarPosition(naviBarPos);
            }
        }
    }

    private void sendBroadcast(boolean minNaviBar) {
        BroadcastThread sendthread = new BroadcastThread();
        sendthread.setMiniNaviBar(minNaviBar);
        sendthread.start();
    }

    private void pressureinit() {
        float limit;
        int naviBarPos;
        String appLeftPkg;
        String appRightPkg;
        if (this.mPressNaviEnable) {
            Log.v(this.TAGPRESSURE, "systemRunning  mPressNaviEnable = " + this.mPressNaviEnable);
            this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
            if (this.mFingerPressNavi != null) {
                this.mFingerPressNavi.createPointerCircleAnimation();
                limit = System.getFloatForUser(this.mContext.getContentResolver(), "pressure_habit_threshold", HwCircleAnimation.BG_ALPHA_FILL, this.mCurrentUserId);
                naviBarPos = System.getIntForUser(this.mContext.getContentResolver(), "virtual_key_type", HT_STATE_UNKOWN, this.mCurrentUserId);
                appLeftPkg = System.getStringForUser(this.mContext.getContentResolver(), App_Left, this.mCurrentUserId);
                if (appLeftPkg != null) {
                    this.mFingerPressNavi.setAppLeftPkg(appLeftPkg);
                } else {
                    this.mFingerPressNavi.setAppLeftPkg("none_app");
                }
                appRightPkg = System.getStringForUser(this.mContext.getContentResolver(), App_Right, this.mCurrentUserId);
                if (appRightPkg != null) {
                    this.mFingerPressNavi.setAppRightPkg(appRightPkg);
                } else {
                    this.mFingerPressNavi.setAppRightPkg("none_app");
                }
                this.mFingerPressNavi.setNaviBarPosition(naviBarPos);
                this.mFingerPressNavi.setPressureLimit(limit);
                this.mFingerPressNavi.setMode(HT_STATE_DISABLE);
                SystemProperties.set("persist.sys.fingerpressnavi", PPPOEStateMachine.PHASE_INITIALIZE);
            }
        } else if (this.IS_SUPPORT_PRESSURE) {
            this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
            if (this.mFingerPressNavi != null) {
                limit = System.getFloatForUser(this.mContext.getContentResolver(), "pressure_habit_threshold", HwCircleAnimation.BG_ALPHA_FILL, this.mCurrentUserId);
                naviBarPos = System.getIntForUser(this.mContext.getContentResolver(), "virtual_key_type", HT_STATE_UNKOWN, this.mCurrentUserId);
                appLeftPkg = System.getStringForUser(this.mContext.getContentResolver(), App_Left, this.mCurrentUserId);
                if (appLeftPkg != null) {
                    this.mFingerPressNavi.setAppLeftPkg(appLeftPkg);
                } else {
                    this.mFingerPressNavi.setAppLeftPkg("none_app");
                }
                appRightPkg = System.getStringForUser(this.mContext.getContentResolver(), App_Right, this.mCurrentUserId);
                if (appRightPkg != null) {
                    this.mFingerPressNavi.setAppRightPkg(appRightPkg);
                } else {
                    this.mFingerPressNavi.setAppRightPkg("none_app");
                }
                this.mFingerPressNavi.setNaviBarPosition(naviBarPos);
                this.mFingerPressNavi.setPressureLimit(limit);
                this.mFingerPressNavi.setMode(HT_STATE_UNKOWN);
                Log.v(this.TAGPRESSURE, "systemRunning  mPressNaviEnable = " + this.mPressNaviEnable);
                SystemProperties.set("persist.sys.fingerpressnavi", PPPOEStateMachine.PHASE_INITIALIZE);
            }
        } else {
            SystemProperties.set("persist.sys.fingerpressnavi", PPPOEStateMachine.PHASE_DEAD);
        }
    }

    private int isNeedTip() {
        return System.getIntForUser(this.mContext.getContentResolver(), this.needTip, HT_STATE_UNKOWN, this.mCurrentUserId);
    }

    private void handleNaviChangeForTip() {
        if (isPressureModeOpenForChina()) {
            System.putIntForUser(this.mContext.getContentResolver(), this.needTip, HT_STATE_DISABLE, this.mCurrentUserId);
            this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
            if (this.mFingerPressNavi != null) {
                this.mFingerPressNavi.setNeedTip(ENABLE_BACK_TO_HOME);
                return;
            }
            return;
        }
        System.putIntForUser(this.mContext.getContentResolver(), this.needTip, HT_STATE_NO_INIT, this.mCurrentUserId);
        this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
        if (this.mFingerPressNavi != null) {
            this.mFingerPressNavi.setNeedTip(IS_CHINA_AREA);
        }
    }

    private void pressureinitData() {
        if (this.IS_SUPPORT_PRESSURE) {
            this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
            if (this.mFingerPressNavi != null) {
                int istip = isNeedTip();
                if (istip == 0) {
                    System.putIntForUser(this.mContext.getContentResolver(), this.needTip, HT_STATE_DISABLE, this.mCurrentUserId);
                    this.mFingerPressNavi.setNeedTip(ENABLE_BACK_TO_HOME);
                } else if (istip == HT_STATE_IDLE) {
                    System.putIntForUser(this.mContext.getContentResolver(), this.needTip, HT_STATE_NO_INIT, this.mCurrentUserId);
                    this.mFingerPressNavi.setNeedTip(IS_CHINA_AREA);
                } else if (istip == HT_STATE_DISABLE) {
                    this.mFingerPressNavi.setNeedTip(ENABLE_BACK_TO_HOME);
                } else if (istip == HT_STATE_NO_INIT) {
                    this.mFingerPressNavi.setNeedTip(IS_CHINA_AREA);
                }
            }
        }
    }

    public void setDisplayWidthAndHeight(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        if (this.mFingerPressNavi != null) {
            this.mFingerPressNavi.setDisplayWidthAndHeight(width, height);
        }
    }

    public void setCurFocusWindow(WindowState focus) {
        if (this.mFingerPressNavi != null) {
            this.mFingerPressNavi.setCurFocusWindow(focus);
        }
    }

    public void setIsTopFullScreen(boolean isTopFullScreen) {
        if (this.mFingerPressNavi != null) {
            this.mFingerPressNavi.setIsTopFullScreen(isTopFullScreen);
        }
    }

    public void setImmersiveMode(boolean mode) {
        if (this.IS_SUPPORT_PRESSURE) {
            if (this.mFingerPressNavi != null) {
                this.mFingerPressNavi.setImmersiveMode(mode);
            }
            if (!(IS_CHINA_AREA || mode || !this.mLastImmersiveMode)) {
                Slog.d("InputManager", "setImmersiveMode  has Changedi mode = " + mode);
                handleNaviChangeForOther();
            }
            this.mLastImmersiveMode = mode;
        }
    }

    public void systemRunning() {
        super.systemRunning();
        this.IS_SUPPORT_PRESSURE = HwGeneralManager.getInstance().isSupportForce();
        if (IS_CHINA_AREA) {
            if (this.IS_SUPPORT_PRESSURE && isPressureModeOpenForChina() && isNavigationBarMin()) {
                this.mPressNaviEnable = ENABLE_BACK_TO_HOME;
            }
        } else if (this.IS_SUPPORT_PRESSURE && isPressureModeOpenForOther()) {
            this.mPressNaviEnable = ENABLE_BACK_TO_HOME;
        }
        if (this.IS_SUPPORT_PRESSURE) {
            initObserver(this.mHandler);
        }
        this.mFingerprintNavigationFilter.systemRunning();
        pressureinit();
        pressureinitData();
        if (this.isSupportPg) {
            initPgPlugThread();
        }
        if (isHomeTraceSupported()) {
            this.mHomeTraceState = HT_STATE_NO_INIT;
        } else {
            this.mHomeTraceState = HT_STATE_DISABLE;
        }
    }

    public void start() {
        super.start();
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                HwInputManagerService.this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", HwInputManagerService.HT_STATE_UNKOWN);
                Slog.e(HwInputManagerService.this.TAGPRESSURE, "user switch mCurrentUserId = " + HwInputManagerService.this.mCurrentUserId);
                if (HwInputManagerService.this.IS_SUPPORT_PRESSURE) {
                    HwInputManagerService.this.pressureinit();
                    HwInputManagerService.this.pressureinitData();
                    if (HwInputManagerService.IS_CHINA_AREA) {
                        HwInputManagerService.this.handleNaviChangeForChina();
                    } else {
                        HwInputManagerService.this.handleNaviChangeForOther();
                    }
                }
            }
        }, new IntentFilter("android.intent.action.USER_SWITCHED"), null, this.mHandler);
    }

    private void initPgPlugThread() {
        this.mPGPlug = new PGPlug(this.mPgEventProcesser, "InputManager");
        new Thread(this.mPGPlug, "InputManager").start();
    }

    boolean filterInputEvent(InputEvent event, int policyFlags) {
        if (this.mFingerprintNavigationFilter.filterInputEvent(event, policyFlags)) {
            return IS_CHINA_AREA;
        }
        if (this.mFingerPressNavi == null || this.mFingerPressNavi.filterPressueInputEvent(event)) {
            return super.filterInputEvent(event, policyFlags);
        }
        return IS_CHINA_AREA;
    }

    boolean isFingprintEventUnHandled(KeyEvent event) {
        return (event.getHwFlags() & HwGlobalActionsData.FLAG_SILENTMODE_TRANSITING) != 0 ? ENABLE_BACK_TO_HOME : IS_CHINA_AREA;
    }

    KeyEvent dispatchUnhandledKey(InputWindowHandle focus, KeyEvent event, int policyFlags) {
        if (!isFingprintEventUnHandled(event) || !this.mFingerprintNavigationFilter.dispatchUnhandledKey(event, policyFlags)) {
            return super.dispatchUnhandledKey(focus, event, policyFlags);
        }
        Slog.d("InputManager", "dispatchUnhandledKey flags=" + Integer.toHexString(event.getHwFlags()));
        return null;
    }

    public boolean isDefaultSlideSwitchOn() {
        if (this.mShowNotification || this.mBackToHome || this.mGoBack || this.mRecentApp || this.mFingerprintMarketDemoSwitch) {
            return ENABLE_BACK_TO_HOME;
        }
        return this.mGallerySlide;
    }

    public void setCurrentUser(int newUserId, int[] currentProfileIds) {
        Slog.i("InputManager", "onUserSwitching, newUserId=" + newUserId);
        this.mGuiShou.registerFpSlideSwitchSettingsObserver(newUserId);
        this.mSettingsObserver.registerContentObserver(newUserId);
        this.mSettingsObserver.onChange(ENABLE_BACK_TO_HOME);
        this.mFingerprintNavigationFilter.setCurrentUser(newUserId, currentProfileIds);
    }

    public void setInputWindows(InputWindowHandle[] windowHandles) {
        super.setInputWindows(windowHandles);
        checkHomeWindowResumed(windowHandles);
    }

    private boolean inSuperPowerSavingMode() {
        return SystemProperties.getBoolean("sys.super_power_save", IS_CHINA_AREA);
    }

    private boolean isHomeTraceSupported() {
        boolean z = ENABLE_BACK_TO_HOME;
        if (!SystemProperties.get("ro.runmode", "normal").equals("normal") || SystemProperties.getInt("ro.logsystem.usertype", HT_STATE_DISABLE) != HT_STATE_IDLE) {
            return IS_CHINA_AREA;
        }
        if (VERSION.EMUI_SDK_INT <= 11) {
            z = IS_CHINA_AREA;
        }
        return z;
    }

    private void notifyHomeResumed() {
        synchronized (this.mFreezeDetectLock) {
            this.mHomeTraceState = HT_STATE_IDLE;
            this.mFreezeDetectLock.notifyAll();
        }
    }

    private String getInputWindowInfo(InputWindowHandle windowHandle) {
        if (windowHandle == null) {
            return null;
        }
        return "windowHandle=" + windowHandle.name + ",windowHandle.InputApplicationHandle=" + (windowHandle.inputApplicationHandle != null ? windowHandle.inputApplicationHandle.name : HwCertification.SIGNATURE_DEFAULT) + ",windowHandle.hasFocus=" + windowHandle.hasFocus + ",windowHandle.visible=" + windowHandle.visible + ",windowHandle.inputChannel=" + windowHandle.inputChannel;
    }

    private void handleGoHomeTimeout() {
        Slog.e("InputManager", "go home timeout, current focused window" + getInputWindowInfo(this.mCurFocusedWindowHandle));
        synchronized (this.mFreezeDetectLock) {
            this.mHomeTraceState = HT_STATE_IDLE;
        }
    }

    private boolean isHomeTraceEnabled() {
        if (this.mHomeTraceState == HT_STATE_NO_INIT) {
            this.mHomeTraceState = initHomeTrace();
        }
        return this.mHomeTraceState >= HT_STATE_IDLE ? ENABLE_BACK_TO_HOME : IS_CHINA_AREA;
    }

    private ComponentName getComponentName(InputWindowHandle windowHandle) {
        if (windowHandle == null) {
            Slog.i("InputManager", "non-home window: windowHandle is null");
            return null;
        }
        WindowState windowState = windowHandle.windowState;
        if (windowState == null) {
            Slog.i("InputManager", "non-home window: windowHandle's state is null");
            return null;
        }
        ActivityRecord r = ActivityRecord.forToken((IBinder) windowState.getAppToken());
        if (r != null) {
            return r.realActivity;
        }
        Slog.i("InputManager", "non-home window:Can not get ActivityRecord from token of windowHandle's windowState");
        return null;
    }

    private boolean isHomeWindow(ComponentName componentName) {
        return componentName != null ? componentName.equals(this.mHomeComponent) : IS_CHINA_AREA;
    }

    private boolean isHomeWindow(InputWindowHandle windowHandle) {
        return isHomeWindow(getComponentName(windowHandle));
    }

    private InputWindowHandle updateFocusedWindow(InputWindowHandle[] windowHandles) {
        this.mCurFocusedWindowHandle = null;
        for (int i = HT_STATE_UNKOWN; i < windowHandles.length; i += HT_STATE_DISABLE) {
            InputWindowHandle windowHandle = windowHandles[i];
            if (!(windowHandle == null || !windowHandle.hasFocus || windowHandle.inputChannel == null)) {
                this.mCurFocusedWindowHandle = windowHandle;
            }
        }
        return this.mCurFocusedWindowHandle;
    }

    private boolean modalWindowOnTop() {
        boolean systemDialog = IS_CHINA_AREA;
        if (this.mCurFocusedWindowHandle == null) {
            return IS_CHINA_AREA;
        }
        this.mModalWindowOnTop = IS_CHINA_AREA;
        int curWindowType = this.mCurFocusedWindowHandle.layoutParamsType;
        int curLayoutParamsFlags = this.mCurFocusedWindowHandle.layoutParamsFlags;
        if (curWindowType == 2010) {
            systemDialog = ENABLE_BACK_TO_HOME;
        } else if (curWindowType == 2003) {
            systemDialog = ENABLE_BACK_TO_HOME;
        }
        if (systemDialog && (curLayoutParamsFlags & MODAL_WINDOW_MASK) == 0) {
            this.mModalWindowOnTop = ENABLE_BACK_TO_HOME;
        }
        return this.mModalWindowOnTop;
    }

    private boolean isHomeWindowFocused() {
        ComponentName curWinComponentName = getComponentName(this.mCurFocusedWindowHandle);
        if (curWinComponentName == null) {
            return IS_CHINA_AREA;
        }
        if (!isHomeWindow(curWinComponentName)) {
            Slog.i("InputManager", "new focused window is not home activity, refresh home activity!");
            getHomeActivity();
            if (!isHomeWindow(curWinComponentName)) {
                Slog.i("InputManager", "home window not resumed, current window is" + this.mCurFocusedWindowHandle.name);
                return IS_CHINA_AREA;
            }
        }
        return ENABLE_BACK_TO_HOME;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void checkHomeWindowResumed(InputWindowHandle[] windowHandles) {
        if (isHomeTraceEnabled() && windowHandles != null && updateFocusedWindow(windowHandles) != null) {
            if (modalWindowOnTop()) {
                finishTraceHomeKey();
            } else if (this.mHomeTraceHandler.hasCallbacks(this.mHomeResumeChecker) && isHomeWindowFocused()) {
                Slog.i("InputManager", "Home window resumed!");
                finishTraceHomeKey();
            }
        }
    }

    private ComponentName getHomeActivity() {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            Slog.e("InputManager", "getHomeActivity, can't get packagemanager");
            return null;
        }
        List<ResolveInfo> homeActivities = new ArrayList();
        ComponentName preferHomeActivity = pm.getHomeActivities(homeActivities);
        if (preferHomeActivity != null) {
            this.mHomeComponent = preferHomeActivity;
        } else if (homeActivities.size() == 0) {
            Slog.e("InputManager", "No home activity found!");
            return null;
        } else {
            ActivityInfo homeActivityInfo = ((ResolveInfo) homeActivities.get(HT_STATE_UNKOWN)).activityInfo;
            this.mHomeComponent = new ComponentName(homeActivityInfo.packageName, homeActivityInfo.name);
        }
        Slog.i("InputManager", "Preferred homeActivity = " + this.mHomeComponent + "," + homeActivities.size() + " homeActivities");
        return this.mHomeComponent;
    }

    private int initHomeTrace() {
        int uiModeType = HT_STATE_UNKOWN;
        UiModeManager uiModeManager = (UiModeManager) this.mContext.getSystemService("uimode");
        if (uiModeManager != null) {
            uiModeType = uiModeManager.getCurrentModeType();
        }
        if (uiModeType != HT_STATE_DISABLE) {
            Slog.e("InputManager", "current Ui mode is " + uiModeType + " not support to trace HOME!");
            return HT_STATE_DISABLE;
        } else if (getHomeActivity() == null) {
            Slog.e("InputManager", "do not trace HOME because no prefered homeActivity found!");
            return HT_STATE_DISABLE;
        } else {
            this.mDreamManagerInternal = (DreamManagerInternal) LocalServices.getService(DreamManagerInternal.class);
            this.mPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
            this.mHomeTraceHandler = DisplayThread.getHandler();
            return HT_STATE_IDLE;
        }
    }

    private boolean isUserSetupComplete() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", HT_STATE_UNKOWN, -2) != 0 ? ENABLE_BACK_TO_HOME : IS_CHINA_AREA;
    }

    private boolean ignoreHomeTrace() {
        if ((this.mDreamManagerInternal != null && this.mDreamManagerInternal.isDreaming()) || (this.mPolicy != null && this.mPolicy.isKeyguardLocked())) {
            return ENABLE_BACK_TO_HOME;
        }
        if (!isUserSetupComplete()) {
            Slog.i("InputManager", "user setup not complete, skip the trace of HOME");
            return ENABLE_BACK_TO_HOME;
        } else if (inSuperPowerSavingMode()) {
            Slog.i("InputManager", "in super power saving mode, skip the trace of HOME");
            return ENABLE_BACK_TO_HOME;
        } else if (!this.mModalWindowOnTop) {
            return IS_CHINA_AREA;
        } else {
            Slog.i("InputManager", "Modal window on the top, skip the trace of HOME");
            return ENABLE_BACK_TO_HOME;
        }
    }

    protected void finishTraceHomeKey() {
        this.mHomeTraceHandler.removeCallbacks(this.mHomeResumeChecker);
        notifyHomeResumed();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean startTraceHomeKey(InputWindowHandle focus, KeyEvent event, int policyFlags) {
        if (event.getKeyCode() != HT_STATE_IDLE || event.getAction() != HT_STATE_DISABLE || event.isCanceled() || focus == null || !isHomeTraceEnabled() || ignoreHomeTrace()) {
            return IS_CHINA_AREA;
        }
        if (!(isHomeWindow(focus) || this.mHomeTraceHandler.hasCallbacks(this.mHomeResumeChecker))) {
            Slog.i("InputManager", "trace HOME in window:" + focus.name);
            this.mHomeTraceHandler.postDelayed(this.mHomeResumeChecker, 15000);
            synchronized (this.mFreezeDetectLock) {
                this.mHomeTraceState = HT_STATE_GOING_HOME;
            }
        }
        return ENABLE_BACK_TO_HOME;
    }

    protected long interceptKeyBeforeDispatching(InputWindowHandle focus, KeyEvent event, int policyFlags) {
        long ret = super.interceptKeyBeforeDispatching(focus, event, policyFlags);
        if (ret == -1) {
            startTraceHomeKey(focus, event, policyFlags);
        }
        return ret;
    }
}
