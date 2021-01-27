package com.android.server.input;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.freeform.HwFreeFormUtils;
import android.graphics.Point;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.Display;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.PointerIcon;
import android.view.WindowManager;
import com.android.server.gesture.GestureNavConst;
import com.android.server.input.InputManagerServiceEx;
import com.android.server.policy.WindowManagerPolicyEx;
import com.android.server.wm.WindowStateEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.content.IntentExEx;
import com.huawei.android.gameassist.HwGameAssistGamePad;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.pgmng.plug.PowerKit;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.android.util.SlogEx;
import com.huawei.android.view.HwWindowManager;
import com.huawei.android.view.InputFilterEx;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import com.huawei.server.HwPartIawareUtil;
import com.huawei.server.security.behaviorcollect.DefaultBehaviorCollector;
import huawei.android.os.HwGeneralManager;
import huawei.com.android.server.policy.HwFalseTouchMonitor;
import huawei.cust.HwCustUtils;
import java.util.List;

public class HwInputManagerService extends InputManagerServiceEx {
    private static final String APP_LEFT = "pressure_launch_app_left";
    private static final String APP_RIGHT = "pressure_launch_app_right";
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
    private static final String FINGER_PRESS_NVI = "persist.sys.fingerpressnavi";
    private static final String FINGER_PRESS_NVI_DISABLE = "0";
    private static final String FINGER_PRESS_NVI_ENABLE = "1";
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", BuildConfig.FLAVOR));
    static final boolean IS_ENABLE_BACK_TO_HOME = true;
    static final boolean IS_ENABLE_LOCK_DEVICE = false;
    private static final boolean IS_FRONT_FINGERPRINT_NAVIGATION = SystemPropertiesEx.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int MOUSE_EVENT = 8194;
    private static final int POWERKIT_TRY_INTERVAL = 5000;
    private static final int POWERKIT_TRY_MAX_TIMES = 10;
    private static final String TAGPRESSURE = "pressure:hwInputMS";
    private ContentObserver mAppLeftStartObserver;
    private ContentObserver mAppRightStartObserver;
    private Context mContext;
    int mCurrentUserId = 0;
    HwCustInputManagerService mCust;
    private Point mDisplaySize = new Point();
    private Context mExternalContext = null;
    FingerPressNavigation mFingerPressNavi = null;
    private FingerprintNavigation mFingerprintNavigationFilter;
    private HwFingersSnapshooter mFingersSnapshooter = null;
    private Handler mHandler;
    private int mHeight;
    boolean mIsAnswerCall = false;
    boolean mIsBackToHome = false;
    private boolean mIsEnableFingerSnapshot = false;
    boolean mIsFpMarketDemoSwitchOn = false;
    boolean mIsGallerySlide;
    boolean mIsGoBack = false;
    boolean mIsInputDeviceChanged = false;
    private boolean mIsKeepCustomPointerIcon = false;
    private boolean mIsLastImmersiveMode = false;
    private boolean mIsPressNaviEnable = false;
    boolean mIsRecentApp = false;
    boolean mIsShowNotification = false;
    boolean mIsStartInputEventControl = false;
    boolean mIsStopAlarm = false;
    private boolean mIsSupportPg = IS_ENABLE_BACK_TO_HOME;
    private boolean mIsSupportPressure = false;
    private ContentObserver mMinNaviObserver;
    private ContentObserver mNaviBarPosObserver;
    private String mNeedTip = "pressure_needTip";
    private WindowManagerPolicyEx mPolicy = null;
    private PowerKit mPowerKit;
    private ContentObserver mPressLimitObserver;
    private ContentObserver mPressNaviObserver;
    private final ContentResolver mResolver;
    private final InputScaleConfiguration mScaleConfig = new InputScaleConfiguration();
    final SettingsObserver mSettingsObserver;
    private PowerKit.Sink mStateRecognitionListener;
    private HwTripleFingersFreeForm mTripleFingersFreeForm = null;
    private int mWidth;
    private WindowManager mWindowManager;

    public HwInputManagerService(Context context, Handler handler) {
        super(context);
        this.mContext = context;
        this.mHandler = handler != null ? handler : new Handler(Looper.getMainLooper());
        HwGameAssistGamePad.mContext = context;
        this.mResolver = context.getContentResolver();
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mCurrentUserId = ActivityManagerEx.getCurrentUser();
        updateFingerprintSlideSwitchValue(this.mCurrentUserId);
        LocalServicesExt.addService(InputManagerServiceEx.DefaultHwInputManagerLocalService.class, new HwInputManagerLocalService());
    }

    private boolean isPressureModeOpenForChina() {
        if (SettingsEx.System.getIntForUser(this.mResolver, "virtual_notification_key_type", 0, this.mCurrentUserId) == 1) {
            return IS_ENABLE_BACK_TO_HOME;
        }
        return false;
    }

    private boolean isPressureModeOpenForOther() {
        if (SettingsEx.System.getIntForUser(this.mResolver, "virtual_notification_key_type", 0, this.mCurrentUserId) == 1) {
            return IS_ENABLE_BACK_TO_HOME;
        }
        return false;
    }

    private boolean isNavigationBarMin() {
        if (Settings.Global.getInt(this.mResolver, SettingsEx.System.NAVIGATIONBAR_IS_MIN, 0) == 1) {
            return IS_ENABLE_BACK_TO_HOME;
        }
        return false;
    }

    private void initNaviObserver(Handler handler) {
        this.mPressNaviObserver = new ContentObserver(handler) {
            /* class com.android.server.input.HwInputManagerService.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                HwInputManagerService.this.handleNaviChangeForTip();
                if (HwInputManagerService.IS_CHINA_AREA) {
                    HwInputManagerService.this.handleNaviChangeForChina();
                } else {
                    HwInputManagerService.this.handleNaviChangeForOther();
                }
            }
        };
        this.mMinNaviObserver = new ContentObserver(handler) {
            /* class com.android.server.input.HwInputManagerService.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                if (HwInputManagerService.IS_CHINA_AREA) {
                    HwInputManagerService.this.handleNaviChangeForChina();
                }
            }
        };
        this.mNaviBarPosObserver = new ContentObserver(handler) {
            /* class com.android.server.input.HwInputManagerService.AnonymousClass3 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                int naviBarPos = SettingsEx.System.getIntForUser(HwInputManagerService.this.mContext.getContentResolver(), "virtual_key_type", 0, ActivityManagerEx.getCurrentUser());
                SlogEx.d(HwInputManagerService.TAGPRESSURE, "mPressNaviObserver onChange open naviBarPos = " + naviBarPos);
                if (HwInputManagerService.this.mFingerPressNavi != null) {
                    HwInputManagerService.this.mFingerPressNavi.setNaviBarPosition(naviBarPos);
                }
            }
        };
    }

    private void initAppObserver(Handler handler) {
        this.mAppLeftStartObserver = new ContentObserver(handler) {
            /* class com.android.server.input.HwInputManagerService.AnonymousClass4 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                String appLeftPkg = SettingsEx.System.getStringForUser(HwInputManagerService.this.mContext.getContentResolver(), HwInputManagerService.APP_LEFT, ActivityManagerEx.getCurrentUser());
                SlogEx.d(HwInputManagerService.TAGPRESSURE, "AppStartObserver onChange open appLeftPkg = " + appLeftPkg);
                if (HwInputManagerService.this.mFingerPressNavi != null && appLeftPkg != null) {
                    HwInputManagerService.this.mFingerPressNavi.setAppLeftPkg(appLeftPkg);
                }
            }
        };
        this.mAppRightStartObserver = new ContentObserver(handler) {
            /* class com.android.server.input.HwInputManagerService.AnonymousClass5 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                String appRightPkg = SettingsEx.System.getStringForUser(HwInputManagerService.this.mContext.getContentResolver(), HwInputManagerService.APP_RIGHT, ActivityManagerEx.getCurrentUser());
                SlogEx.d(HwInputManagerService.TAGPRESSURE, "AppStartObserver onChange open appRightPkg = " + appRightPkg);
                if (HwInputManagerService.this.mFingerPressNavi != null && appRightPkg != null) {
                    HwInputManagerService.this.mFingerPressNavi.setAppRightPkg(appRightPkg);
                }
            }
        };
    }

    private void initObserver(Handler handler) {
        this.mPressLimitObserver = new ContentObserver(handler) {
            /* class com.android.server.input.HwInputManagerService.AnonymousClass6 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                float limit = SettingsEx.System.getFloatForUser(HwInputManagerService.this.mContext.getContentResolver(), "pressure_habit_threshold", 0.2f, ActivityManagerEx.getCurrentUser());
                SlogEx.d(HwInputManagerService.TAGPRESSURE, "mPressLimitObserver onChange open limit = " + limit);
                if (HwInputManagerService.this.mFingerPressNavi != null) {
                    HwInputManagerService.this.mFingerPressNavi.setPressureLimit(limit);
                }
            }
        };
        initNaviObserver(handler);
        initAppObserver(handler);
        registerInputManagerObserver();
    }

    private void registerInputManagerObserver() {
        ContentResolverExt.registerContentObserver(this.mResolver, Settings.System.getUriFor("virtual_notification_key_type"), false, this.mPressNaviObserver, -1);
        ContentResolverExt.registerContentObserver(this.mResolver, Settings.Global.getUriFor("navigationbar_is_min"), false, this.mMinNaviObserver, -1);
        ContentResolverExt.registerContentObserver(this.mResolver, Settings.System.getUriFor("pressure_habit_threshold"), false, this.mPressLimitObserver, -1);
        ContentResolverExt.registerContentObserver(this.mResolver, Settings.System.getUriFor("virtual_key_type"), false, this.mNaviBarPosObserver, -1);
        ContentResolverExt.registerContentObserver(this.mResolver, Settings.System.getUriFor(APP_LEFT), false, this.mAppLeftStartObserver, -1);
        ContentResolverExt.registerContentObserver(this.mResolver, Settings.System.getUriFor(APP_RIGHT), false, this.mAppRightStartObserver, -1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNaviChangeForChina() {
        if (this.mIsSupportPressure) {
            if (!isPressureModeOpenForChina() || !isNavigationBarMin()) {
                SlogEx.d(TAGPRESSURE, "mPressNaviObserver onChange close");
                this.mIsPressNaviEnable = false;
                this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
                FingerPressNavigation fingerPressNavigation = this.mFingerPressNavi;
                if (fingerPressNavigation != null) {
                    fingerPressNavigation.destoryPointerCircleAnimation();
                    this.mFingerPressNavi.setMode(0);
                }
                SystemPropertiesEx.set(FINGER_PRESS_NVI, FINGER_PRESS_NVI_ENABLE);
            } else {
                SlogEx.d(TAGPRESSURE, "mPressNaviObserver onChange open");
                this.mIsPressNaviEnable = IS_ENABLE_BACK_TO_HOME;
                this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
                FingerPressNavigation fingerPressNavigation2 = this.mFingerPressNavi;
                if (fingerPressNavigation2 != null) {
                    fingerPressNavigation2.createPointerCircleAnimation();
                    this.mFingerPressNavi.setDisplayWidthAndHeight(this.mWidth, this.mHeight);
                    this.mFingerPressNavi.setMode(1);
                }
                SystemPropertiesEx.set(FINGER_PRESS_NVI, FINGER_PRESS_NVI_ENABLE);
            }
            int naviBarPos = SettingsEx.System.getIntForUser(this.mContext.getContentResolver(), "virtual_key_type", 0, this.mCurrentUserId);
            FingerPressNavigation fingerPressNavigation3 = this.mFingerPressNavi;
            if (fingerPressNavigation3 != null) {
                fingerPressNavigation3.setNaviBarPosition(naviBarPos);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNaviChangeForOther() {
        if (this.mIsSupportPressure) {
            if (isPressureModeOpenForOther()) {
                SlogEx.d(TAGPRESSURE, "mPressNaviObserver onChange open");
                this.mIsPressNaviEnable = IS_ENABLE_BACK_TO_HOME;
                this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
                FingerPressNavigation fingerPressNavigation = this.mFingerPressNavi;
                if (fingerPressNavigation != null) {
                    fingerPressNavigation.createPointerCircleAnimation();
                    this.mFingerPressNavi.setDisplayWidthAndHeight(this.mWidth, this.mHeight);
                    this.mFingerPressNavi.setMode(1);
                }
                sendBroadcast(IS_ENABLE_BACK_TO_HOME);
                SystemPropertiesEx.set(FINGER_PRESS_NVI, FINGER_PRESS_NVI_ENABLE);
            } else {
                SlogEx.d(TAGPRESSURE, "mPressNaviObserver onChange close");
                this.mIsPressNaviEnable = false;
                this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
                FingerPressNavigation fingerPressNavigation2 = this.mFingerPressNavi;
                if (fingerPressNavigation2 != null) {
                    fingerPressNavigation2.destoryPointerCircleAnimation();
                    this.mFingerPressNavi.setMode(0);
                }
                sendBroadcast(false);
                SystemPropertiesEx.set(FINGER_PRESS_NVI, FINGER_PRESS_NVI_ENABLE);
            }
            int naviBarPos = SettingsEx.System.getIntForUser(this.mContext.getContentResolver(), "virtual_key_type", 0, this.mCurrentUserId);
            FingerPressNavigation fingerPressNavigation3 = this.mFingerPressNavi;
            if (fingerPressNavigation3 != null) {
                fingerPressNavigation3.setNaviBarPosition(naviBarPos);
            }
        }
    }

    /* access modifiers changed from: private */
    public class BroadcastThread extends Thread {
        boolean mIsMinNaviBar;

        private BroadcastThread() {
        }

        public void setMiniNaviBar(boolean isMinNaviBar) {
            this.mIsMinNaviBar = isMinNaviBar;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Log.d(HwInputManagerService.TAGPRESSURE, "sendBroadcast minNaviBar = " + this.mIsMinNaviBar);
            Intent intent = new Intent(SettingsEx.System.HUAWEI_NAVIGATIONBAR_STATUSCHANGE);
            intent.putExtra(SettingsEx.System.HUAWEI_MINNAVIGATIONBAR, this.mIsMinNaviBar);
            HwInputManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandleEx.ALL);
        }
    }

    private void sendBroadcast(boolean isMinNaviBar) {
        BroadcastThread sendthread = new BroadcastThread();
        sendthread.setMiniNaviBar(isMinNaviBar);
        sendthread.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pressureinit() {
        if (this.mIsPressNaviEnable || this.mIsSupportPressure) {
            this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
            FingerPressNavigation fingerPressNavigation = this.mFingerPressNavi;
            if (fingerPressNavigation != null) {
                if (this.mIsPressNaviEnable) {
                    Log.d(TAGPRESSURE, "systemRunning mIsPressNaviEnable ");
                    this.mFingerPressNavi.createPointerCircleAnimation();
                    this.mFingerPressNavi.setMode(1);
                } else if (this.mIsSupportPressure) {
                    fingerPressNavigation.setMode(0);
                    Log.d(TAGPRESSURE, "systemRunning mIsSupportPressure ");
                }
                String appLeftPkg = SettingsEx.System.getStringForUser(this.mContext.getContentResolver(), APP_LEFT, this.mCurrentUserId);
                if (appLeftPkg != null) {
                    this.mFingerPressNavi.setAppLeftPkg(appLeftPkg);
                } else {
                    this.mFingerPressNavi.setAppLeftPkg("none_app");
                }
                String appRightPkg = SettingsEx.System.getStringForUser(this.mContext.getContentResolver(), APP_RIGHT, this.mCurrentUserId);
                if (appRightPkg != null) {
                    this.mFingerPressNavi.setAppRightPkg(appRightPkg);
                } else {
                    this.mFingerPressNavi.setAppRightPkg("none_app");
                }
                float limit = SettingsEx.System.getFloatForUser(this.mContext.getContentResolver(), "pressure_habit_threshold", 0.2f, this.mCurrentUserId);
                this.mFingerPressNavi.setNaviBarPosition(SettingsEx.System.getIntForUser(this.mContext.getContentResolver(), "virtual_key_type", 0, this.mCurrentUserId));
                this.mFingerPressNavi.setPressureLimit(limit);
                SystemPropertiesEx.set(FINGER_PRESS_NVI, FINGER_PRESS_NVI_ENABLE);
                return;
            }
            return;
        }
        SystemPropertiesEx.set(FINGER_PRESS_NVI, FINGER_PRESS_NVI_DISABLE);
    }

    private int isNeedTip() {
        return SettingsEx.System.getIntForUser(this.mContext.getContentResolver(), this.mNeedTip, 0, this.mCurrentUserId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNaviChangeForTip() {
        if (isPressureModeOpenForChina()) {
            SettingsEx.System.putIntForUser(this.mContext.getContentResolver(), this.mNeedTip, 1, this.mCurrentUserId);
            this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
            FingerPressNavigation fingerPressNavigation = this.mFingerPressNavi;
            if (fingerPressNavigation != null) {
                fingerPressNavigation.setNeedTip(IS_ENABLE_BACK_TO_HOME);
                return;
            }
            return;
        }
        SettingsEx.System.putIntForUser(this.mContext.getContentResolver(), this.mNeedTip, 2, this.mCurrentUserId);
        this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
        FingerPressNavigation fingerPressNavigation2 = this.mFingerPressNavi;
        if (fingerPressNavigation2 != null) {
            fingerPressNavigation2.setNeedTip(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pressureinitData() {
        if (this.mIsSupportPressure) {
            this.mFingerPressNavi = FingerPressNavigation.getInstance(this.mContext);
            if (this.mFingerPressNavi != null) {
                int istip = isNeedTip();
                if (istip == 0) {
                    SettingsEx.System.putIntForUser(this.mContext.getContentResolver(), this.mNeedTip, 1, this.mCurrentUserId);
                    this.mFingerPressNavi.setNeedTip(IS_ENABLE_BACK_TO_HOME);
                } else if (istip == 3) {
                    SettingsEx.System.putIntForUser(this.mContext.getContentResolver(), this.mNeedTip, 2, this.mCurrentUserId);
                    this.mFingerPressNavi.setNeedTip(false);
                } else if (istip == 1) {
                    this.mFingerPressNavi.setNeedTip(IS_ENABLE_BACK_TO_HOME);
                } else if (istip == 2) {
                    this.mFingerPressNavi.setNeedTip(false);
                }
            }
        }
    }

    public void setDisplayWidthAndHeight(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        FingerPressNavigation fingerPressNavigation = this.mFingerPressNavi;
        if (fingerPressNavigation != null) {
            fingerPressNavigation.setDisplayWidthAndHeight(width, height);
        }
    }

    public void setCurFocusWindow(WindowStateEx focus) {
        FingerPressNavigation fingerPressNavigation = this.mFingerPressNavi;
        if (fingerPressNavigation != null) {
            fingerPressNavigation.setCurFocusWindow(focus);
        }
    }

    public void setIsTopFullScreen(boolean isTopFullScreen) {
        FingerPressNavigation fingerPressNavigation = this.mFingerPressNavi;
        if (fingerPressNavigation != null) {
            fingerPressNavigation.setIsTopFullScreen(isTopFullScreen);
        }
    }

    public void setImmersiveMode(boolean isImmersiveMode) {
        if (this.mIsSupportPressure) {
            FingerPressNavigation fingerPressNavigation = this.mFingerPressNavi;
            if (fingerPressNavigation != null) {
                fingerPressNavigation.setImmersiveMode(isImmersiveMode);
            }
            if (!IS_CHINA_AREA && !isImmersiveMode && this.mIsLastImmersiveMode) {
                String str = TAG;
                SlogEx.d(str, "setImmersiveMode has Changedi mode = " + isImmersiveMode);
                handleNaviChangeForOther();
            }
            this.mIsLastImmersiveMode = isImmersiveMode;
        }
    }

    public void systemRunning() {
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        scheduleSetDispatchDisplayInfo();
        this.mIsEnableFingerSnapshot = SystemPropertiesEx.getBoolean("ro.config.hw_triple_finger", false);
        if (this.mIsEnableFingerSnapshot) {
            this.mFingersSnapshooter = new HwFingersSnapshooter(this.mContext, this);
            synchronized (getInputFilterLock()) {
                nativeSetInputFilterEnabled(getPtr(), IS_ENABLE_BACK_TO_HOME);
            }
        }
        this.mTripleFingersFreeForm = new HwTripleFingersFreeForm(this.mContext, this);
        synchronized (getInputFilterLock()) {
            nativeSetInputFilterEnabled(getPtr(), IS_ENABLE_BACK_TO_HOME);
        }
        this.mFingerprintNavigationFilter = new FingerprintNavigation(this.mContext);
        this.mFingerprintNavigationFilter.systemRunning();
        this.mIsSupportPressure = HwGeneralManager.getInstance().isSupportForce();
        if (IS_CHINA_AREA) {
            if (this.mIsSupportPressure && isPressureModeOpenForChina() && isNavigationBarMin()) {
                this.mIsPressNaviEnable = IS_ENABLE_BACK_TO_HOME;
            }
        } else if (this.mIsSupportPressure && isPressureModeOpenForOther()) {
            this.mIsPressNaviEnable = IS_ENABLE_BACK_TO_HOME;
        }
        if (this.mIsSupportPressure) {
            initObserver(this.mHandler);
        }
        pressureinit();
        pressureinitData();
        if (this.mIsSupportPg && (this.mIsSupportPressure || this.mFingerPressNavi != null)) {
            initPowerKit();
        }
        HwPartIawareUtil.setImsForAwareFakeActivityRecg(this);
        HwPartIawareUtil.setImsForSysLoadManager(this);
        HwPartIawareUtil.setImsForAwareGameModeRecg(this);
        this.mPolicy = WindowManagerPolicyEx.getInstance();
    }

    public void start() {
        this.mCust = (HwCustInputManagerService) HwCustUtils.createObj(HwCustInputManagerService.class, new Object[]{this});
        HwCustInputManagerService hwCustInputManagerService = this.mCust;
        if (hwCustInputManagerService != null) {
            hwCustInputManagerService.registerContentObserverForSetGloveMode(this.mContext);
        }
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.input.HwInputManagerService.AnonymousClass7 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                HwInputManagerService.this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", 0);
                SlogEx.e(HwInputManagerService.TAGPRESSURE, "user switch mCurrentUserId = " + HwInputManagerService.this.mCurrentUserId);
                if (HwInputManagerService.this.mIsSupportPressure) {
                    HwInputManagerService.this.pressureinit();
                    HwInputManagerService.this.pressureinitData();
                    if (HwInputManagerService.IS_CHINA_AREA) {
                        HwInputManagerService.this.handleNaviChangeForChina();
                    } else {
                        HwInputManagerService.this.handleNaviChangeForOther();
                    }
                }
            }
        }, new IntentFilter(IntentExEx.getActionUserSwitched()), null, this.mHandler);
    }

    public void onConfigurationChanged() {
        scheduleSetDispatchDisplayInfo();
    }

    private void initPowerKit() {
        new Thread(new Runnable() {
            /* class com.android.server.input.HwInputManagerService.AnonymousClass8 */

            @Override // java.lang.Runnable
            public void run() {
                for (int i = 0; i < 10; i++) {
                    HwInputManagerService.this.mPowerKit = PowerKit.getInstance();
                    if (HwInputManagerService.this.mPowerKit != null) {
                        try {
                            SlogEx.i(InputManagerServiceEx.TAG, "get PowerKit instance success!");
                            HwInputManagerService.this.mStateRecognitionListener = new StateRecognitionListener();
                            HwInputManagerService.this.mPowerKit.enableStateEvent(HwInputManagerService.this.mStateRecognitionListener, 10002);
                            HwInputManagerService.this.mPowerKit.enableStateEvent(HwInputManagerService.this.mStateRecognitionListener, 10011);
                            return;
                        } catch (RemoteException e) {
                            SlogEx.e(InputManagerServiceEx.TAG, "VBR PG Exception e: initialize powerkit error!");
                            return;
                        }
                    } else {
                        String str = InputManagerServiceEx.TAG;
                        SlogEx.i(str, "get PowerKit instance failed! tryTimes:" + i);
                        SystemClock.sleep(5000);
                    }
                }
            }
        }).start();
    }

    private class StateRecognitionListener implements PowerKit.Sink {
        private StateRecognitionListener() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            if ((stateType != 10002 && stateType != 10011) || HwInputManagerService.this.mFingerPressNavi == null) {
                return;
            }
            if (eventType == 1) {
                HwInputManagerService.this.mFingerPressNavi.setGameScene(HwInputManagerService.IS_ENABLE_BACK_TO_HOME);
            } else {
                HwInputManagerService.this.mFingerPressNavi.setGameScene(false);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void deliverInputDevicesChanged(InputDevice[] oldInputDevices) {
        if (DEBUG_HWFLOW) {
            SlogEx.d(TAG, "inputdevicechanged.....");
        }
        this.mIsInputDeviceChanged = IS_ENABLE_BACK_TO_HOME;
    }

    /* access modifiers changed from: protected */
    public boolean filterInputEvent(InputEvent event, int policyFlags) {
        HwFingersSnapshooter hwFingersSnapshooter;
        boolean z = this.mIsStartInputEventControl;
        boolean isNeedInputEvent = IS_ENABLE_BACK_TO_HOME;
        if (z) {
            if (this.mIsInputDeviceChanged && event.getSource() == MOUSE_EVENT) {
                this.mIsInputDeviceChanged = false;
                if (DEBUG_HWFLOW) {
                    SlogEx.d(TAG, "mouse event :" + this.mIsInputDeviceChanged);
                }
                setPointerIconTypeAndKeepImpl(HwFalseTouchMonitor.NoEffectClickChecker.CLICK_INTERVAL_TIMEOUT, IS_ENABLE_BACK_TO_HOME);
                setPointerIconTypeAndKeepImpl(0, IS_ENABLE_BACK_TO_HOME);
            }
            int result = HwGameAssistGamePad.notifyInputEvent(event);
            if (result == 0) {
                return false;
            }
            if (result == 1) {
                setInputEventStrategy(false);
            }
        }
        DefaultBehaviorCollector.sIsActiveTouched = IS_ENABLE_BACK_TO_HOME;
        FingerprintNavigation fingerprintNavigation = this.mFingerprintNavigationFilter;
        if (fingerprintNavigation != null && fingerprintNavigation.filterInputEvent(event, policyFlags)) {
            return false;
        }
        FingerPressNavigation fingerPressNavigation = this.mFingerPressNavi;
        if (fingerPressNavigation != null && !fingerPressNavigation.filterPressueInputEvent(event)) {
            return false;
        }
        boolean isPcMode = HwPCUtils.isPcCastModeInServer();
        if (!isPcMode && (hwFingersSnapshooter = this.mFingersSnapshooter) != null && !hwFingersSnapshooter.handleMotionEvent(event)) {
            return false;
        }
        if (!((isPcMode || this.mTripleFingersFreeForm == null || this.mPolicy == null) ? false : true) || this.mPolicy.isKeyguardLocked() || inSuperPowerSavingMode() || this.mTripleFingersFreeForm.handleMotionEvent(event)) {
            isNeedInputEvent = false;
        }
        if (isNeedInputEvent) {
            return false;
        }
        return HwInputManagerService.super.filterInputEventEx(event, policyFlags);
    }

    private boolean inSuperPowerSavingMode() {
        return SystemPropertiesEx.getBoolean(GestureNavConst.KEY_SUPER_SAVE_MODE, false);
    }

    final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            registerContentObserver(UserHandleEx.myUserId());
        }

        public void registerContentObserver(int userId) {
            ContentResolverExt.registerContentObserver(HwInputManagerService.this.mResolver, Settings.Secure.getUriFor(HwInputManagerService.FINGERPRINT_CAMERA_SWITCH), false, this, userId);
            ContentResolverExt.registerContentObserver(HwInputManagerService.this.mResolver, Settings.Secure.getUriFor(HwInputManagerService.FINGERPRINT_ANSWER_CALL), false, this, userId);
            ContentResolverExt.registerContentObserver(HwInputManagerService.this.mResolver, Settings.Secure.getUriFor(HwInputManagerService.FINGERPRINT_SHOW_NOTIFICATION), false, this, userId);
            HwInputManagerService.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(HwInputManagerService.FINGERPRINT_BACK_TO_HOME), false, this);
            ContentResolverExt.registerContentObserver(HwInputManagerService.this.mResolver, Settings.Secure.getUriFor(HwInputManagerService.FINGERPRINT_STOP_ALARM), false, this, userId);
            HwInputManagerService.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(HwInputManagerService.FINGERPRINT_LOCK_DEVICE), false, this);
            HwInputManagerService.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(HwInputManagerService.FINGERPRINT_GO_BACK), false, this);
            HwInputManagerService.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(HwInputManagerService.FINGERPRINT_RECENT_APP), false, this);
            HwInputManagerService.this.mResolver.registerContentObserver(Settings.System.getUriFor(HwInputManagerService.FINGERPRINT_MARKET_DEMO_SWITCH), false, this);
            ContentResolverExt.registerContentObserver(HwInputManagerService.this.mResolver, Settings.Secure.getUriFor(HwInputManagerService.FINGERPRINT_GALLERY_SLIDE), false, this, userId);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            SlogEx.d(InputManagerServiceEx.TAG, "SettingDB has Changed");
            HwInputManagerService.this.updateFingerprintSlideSwitchValue();
        }
    }

    public final void updateFingerprintSlideSwitchValue() {
        updateFingerprintSlideSwitchValue(ActivityManagerEx.getCurrentUser());
    }

    public final void updateFingerprintSlideSwitchValue(int userId) {
        String str = TAG;
        SlogEx.d(str, "ActivityManagerEx.getCurrentUser:" + userId);
        this.mIsAnswerCall = SettingsEx.Secure.getIntForUser(this.mResolver, FINGERPRINT_ANSWER_CALL, 0, userId) != 0;
        this.mIsStopAlarm = SettingsEx.Secure.getIntForUser(this.mResolver, FINGERPRINT_STOP_ALARM, 0, userId) != 0;
        this.mIsShowNotification = SettingsEx.Secure.getIntForUser(this.mResolver, FINGERPRINT_SHOW_NOTIFICATION, 0, userId) != 0;
        this.mIsBackToHome = Settings.Secure.getInt(this.mResolver, FINGERPRINT_BACK_TO_HOME, 0) != 0;
        this.mIsGoBack = Settings.Secure.getInt(this.mResolver, FINGERPRINT_GO_BACK, 0) != 0;
        this.mIsRecentApp = Settings.Secure.getInt(this.mResolver, FINGERPRINT_RECENT_APP, 0) != 0;
        this.mIsFpMarketDemoSwitchOn = Settings.System.getInt(this.mResolver, FINGERPRINT_MARKET_DEMO_SWITCH, 0) == 1;
        this.mIsGallerySlide = SettingsEx.Secure.getIntForUser(this.mResolver, FINGERPRINT_GALLERY_SLIDE, 1, userId) != 0;
        if (this.mIsBackToHome || this.mIsGoBack || this.mIsRecentApp || this.mIsFpMarketDemoSwitchOn || IS_FRONT_FINGERPRINT_NAVIGATION) {
            String str2 = TAG;
            SlogEx.d(str2, "open fingerprint nav->FINGERPRINT_SLIDE_SWITCH to 1 userId:" + userId);
            SettingsEx.System.putIntForUser(this.mResolver, FINGERPRINT_SLIDE_SWITCH, 1, userId);
        } else if (HwPCUtils.isPcCastModeInServer()) {
            HwPCUtils.log(TAG, "set fingerprint_slide_switch=1 in pc mode");
            SettingsEx.System.putIntForUser(this.mResolver, FINGERPRINT_SLIDE_SWITCH, 1, userId);
        } else if (this.mIsShowNotification || this.mIsAnswerCall || this.mIsStopAlarm) {
            String str3 = TAG;
            SlogEx.d(str3, "open fingerprint nav->FINGERPRINT_SLIDE_SWITCH to 1 userId:" + userId);
            SettingsEx.System.putIntForUser(this.mResolver, FINGERPRINT_SLIDE_SWITCH, 1, userId);
        } else {
            String str4 = TAG;
            SlogEx.d(str4, "close fingerprint nav ->FINGERPRINT_SLIDE_SWITCH to 0 userId:" + userId);
            SettingsEx.System.putIntForUser(this.mResolver, FINGERPRINT_SLIDE_SWITCH, 0, userId);
        }
    }

    public void setCurrentUser(int newUserId, int[] currentProfileIds) {
        String str = TAG;
        SlogEx.i(str, "onUserSwitching, newUserId=" + newUserId);
        this.mSettingsObserver.registerContentObserver(newUserId);
        this.mSettingsObserver.onChange(IS_ENABLE_BACK_TO_HOME);
        FingerprintNavigation fingerprintNavigation = this.mFingerprintNavigationFilter;
        if (fingerprintNavigation != null) {
            fingerprintNavigation.setCurrentUser(newUserId, currentProfileIds);
        }
    }

    public Context getExternalContext() {
        return this.mExternalContext;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setExternalContext(Context context) {
        this.mExternalContext = context;
        nativeReloadPointerIcons(getPtr(), this.mExternalContext);
    }

    public final class HwInputManagerLocalService extends InputManagerServiceEx.DefaultHwInputManagerLocalService {
        public HwInputManagerLocalService() {
            super(HwInputManagerService.this);
        }

        public boolean injectInputEvent(InputEvent event, int mode) {
            return HwInputManagerService.this.injectInputEventOtherScreens(event, mode);
        }

        public boolean injectInputEvent(InputEvent event, int mode, int appendPolicyFlag) {
            return HwInputManagerService.this.injectInputEventInternal(event, mode, appendPolicyFlag);
        }

        public void setExternalDisplayContext(Context context) {
            HwInputManagerService.this.setExternalContext(context);
        }

        public void setPointerIconTypeAndKeep(int iconId, boolean isKeep) {
            HwInputManagerService.this.setPointerIconTypeAndKeepImpl(iconId, isKeep);
        }

        public void setCustomPointerIconAndKeep(PointerIcon icon, boolean isKeep) {
            HwInputManagerService.this.setCustomPointerIconAndKeepImpl(icon, isKeep);
        }

        public void setMirrorLinkInputStatus(boolean isMirrorLinkStatus) {
            HwInputManagerService hwInputManagerService = HwInputManagerService.this;
            hwInputManagerService.nativeSetMirrorLinkInputStatus(hwInputManagerService.getPtr(), isMirrorLinkStatus);
        }

        public void setKeyguardState(boolean isShowing) {
            HwInputManagerService hwInputManagerService = HwInputManagerService.this;
            hwInputManagerService.nativeSetKeyguardState(hwInputManagerService.getPtr(), isShowing);
        }

        public void setInputScaleConfig(float xScale, float yScale, int scaleSide, int scaleType) {
            synchronized (HwInputManagerService.this.mScaleConfig) {
                HwInputManagerService.this.mScaleConfig.updateScaleConfig(xScale, yScale, scaleSide, scaleType);
            }
            HwInputManagerService hwInputManagerService = HwInputManagerService.this;
            hwInputManagerService.nativeSetInputScaleConfig(hwInputManagerService.getPtr(), xScale, yScale, scaleSide, scaleType);
        }

        public InputScaleConfiguration getInputScaleConfig() {
            InputScaleConfiguration inputScaleConfiguration;
            synchronized (HwInputManagerService.this.mScaleConfig) {
                inputScaleConfiguration = HwInputManagerService.this.mScaleConfig;
            }
            return inputScaleConfiguration;
        }
    }

    public void responseTouchEvent(boolean isNeedResponseStatus) {
        nativeResponseTouchEvent(getPtr(), isNeedResponseStatus);
    }

    public boolean injectInputEventOtherScreens(InputEvent event, int mode) {
        int displayId = HwPCUtils.getPCDisplayID();
        if (displayId != 0 && displayId != -1) {
            return injectInputEventInternal(event, mode);
        }
        SlogEx.i(TAG, "not other screen found!");
        return false;
    }

    public static String getAppName(Context context, int pid) {
        ActivityManager activityManager;
        List<ActivityManager.RunningAppProcessInfo> appProcesses;
        if (pid <= 0 || context == null || (activityManager = (ActivityManager) context.getSystemService("activity")) == null || (appProcesses = activityManager.getRunningAppProcesses()) == null || appProcesses.size() == 0) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private boolean checkIsSystemApp() {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        if (pm.checkSignatures(Binder.getCallingUid(), Process.myUid()) != 0) {
            String str = TAG;
            SlogEx.d(str, "not SIGNATURE_MATCH ...." + Binder.getCallingUid());
            return false;
        }
        try {
            String pckName = getAppName(this.mContext, Binder.getCallingPid());
            if (pckName == null) {
                String str2 = TAG;
                SlogEx.e(str2, "pckName is null " + Binder.getCallingPid() + " " + Process.myUid());
                return false;
            }
            ApplicationInfo info = pm.getApplicationInfo(pckName, 0);
            if (info == null || (info.flags & 1) == 0) {
                String str3 = TAG;
                SlogEx.d(str3, "return false " + pckName);
                return false;
            }
            String str4 = TAG;
            SlogEx.d(str4, "return true " + pckName);
            return IS_ENABLE_BACK_TO_HOME;
        } catch (PackageManager.NameNotFoundException e) {
            String str5 = TAG;
            SlogEx.e(str5, "isSystemApp not found app" + BuildConfig.FLAVOR + "exception");
            return false;
        }
    }

    private void setInputFilterEnabled() {
        synchronized (getInputFilterLock()) {
            if (!this.mIsEnableFingerSnapshot && !this.mIsStartInputEventControl && getInputFilter() == null) {
                if (this.mTripleFingersFreeForm == null) {
                    nativeSetInputFilterEnabled(getPtr(), false);
                }
            }
            nativeSetInputFilterEnabled(getPtr(), IS_ENABLE_BACK_TO_HOME);
        }
    }

    public void setInputEventStrategy(boolean isStartInputEventControl) {
        if (checkIsSystemApp() && this.mIsStartInputEventControl != isStartInputEventControl) {
            String str = TAG;
            SlogEx.d(str, "mIsStartInputEventControl change to:" + isStartInputEventControl);
            this.mIsStartInputEventControl = isStartInputEventControl;
            if (isStartInputEventControl) {
                setPointerIconTypeAndKeepImpl(0, IS_ENABLE_BACK_TO_HOME);
                HwGameAssistGamePad.bindService();
            } else {
                setPointerIconTypeAndKeepImpl(HwFalseTouchMonitor.NoEffectClickChecker.CLICK_INTERVAL_TIMEOUT, false);
                HwGameAssistGamePad.unbindService();
            }
            setInputFilterEnabled();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x001c  */
    public void setInputFilter(InputFilterEx filter) {
        boolean isToNativeSetInput;
        synchronized (getInputFilterLock()) {
            if (filter == null) {
                if (!this.mIsEnableFingerSnapshot && !this.mIsStartInputEventControl) {
                    if (!HwFreeFormUtils.isFreeFormEnable()) {
                        isToNativeSetInput = false;
                        if (isToNativeSetInput) {
                            nativeSetInputFilterEnabled(getPtr(), IS_ENABLE_BACK_TO_HOME);
                        }
                    }
                }
                isToNativeSetInput = true;
                if (isToNativeSetInput) {
                }
            }
        }
    }

    public void setPointerIconType(int iconId) {
        if ((this.mIsStartInputEventControl || HwWindowManager.hasLighterViewInPCCastMode()) && this.mIsKeepCustomPointerIcon) {
            String str = TAG;
            SlogEx.i(str, "setPointerIconType cannot change pointer icon when lighter view above.gamepad:" + this.mIsStartInputEventControl);
            return;
        }
        HwInputManagerService.super.setPointerIconTypeEx(iconId);
    }

    public void setCustomPointerIcon(PointerIcon icon) {
        if ((this.mIsStartInputEventControl || HwWindowManager.hasLighterViewInPCCastMode()) && this.mIsKeepCustomPointerIcon) {
            String str = TAG;
            SlogEx.i(str, "setCustomPointerIcon cannot change pointer icon when lighter view above.gamepad:" + this.mIsStartInputEventControl);
            return;
        }
        HwInputManagerService.super.setCustomPointerIconEx(icon);
    }

    public void setPointerIconTypeAndKeepImpl(int iconId, boolean isKeep) {
        HwInputManagerService.super.setPointerIconTypeEx(iconId);
        this.mIsKeepCustomPointerIcon = isKeep;
    }

    public void setCustomPointerIconAndKeepImpl(PointerIcon icon, boolean isKeep) {
        HwInputManagerService.super.setCustomPointerIconEx(icon);
        this.mIsKeepCustomPointerIcon = isKeep;
    }

    public void setIawareGameMode(int gameMode) {
        nativeSetIawareGameMode(getPtr(), gameMode);
    }

    public void setIawareGameModeAccurate(int gameMode) {
        nativeSetIawareGameModeAccurate(getPtr(), gameMode);
    }

    private void scheduleSetDispatchDisplayInfo() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.input.$$Lambda$HwInputManagerService$jgNnyOQbOQ983XkwOWs3qCyDeQ */

            @Override // java.lang.Runnable
            public final void run() {
                HwInputManagerService.this.lambda$scheduleSetDispatchDisplayInfo$0$HwInputManagerService();
            }
        });
    }

    public /* synthetic */ void lambda$scheduleSetDispatchDisplayInfo$0$HwInputManagerService() {
        WindowManager windowManager = this.mWindowManager;
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            display.getRealSize(this.mDisplaySize);
            nativeSetDispatchDisplayInfo(getPtr(), 0, this.mDisplaySize.x, this.mDisplaySize.y, display.getRotation());
        }
    }
}
