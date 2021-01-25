package com.android.server.policy;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.KeyguardManager;
import android.app.SynchronousUserSwitchObserver;
import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.cover.CoverManager;
import android.database.ContentObserver;
import android.freeform.HwFreeFormManager;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.HwFoldScreenState;
import android.hardware.input.InputManager;
import android.hdm.HwDeviceManager;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IHwBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.os.UserHandle;
import android.os.Vibrator;
import android.pc.IHwPCManager;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.swing.HwSwingManager;
import android.swing.IHwSwingService;
import android.telecom.TelecomManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.HwStylusUtils;
import android.util.Log;
import android.util.Slog;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerPolicyConstants;
import android.view.accessibility.AccessibilityManager;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.telephony.ITelephony;
import com.android.server.CoordinationStackDividerManager;
import com.android.server.HwServiceExFactory;
import com.android.server.LocalServices;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.PointerEventListenerEx;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.cust.utils.HwCustPkgNameConstant;
import com.android.server.displayside.HwDisplaySidePolicy;
import com.android.server.displayside.HwDisplaySideRegionConfig;
import com.android.server.foldscreenview.SubScreenViewEntry;
import com.android.server.gesture.DefaultGestureNavManager;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.lights.LightsManagerEx;
import com.android.server.notch.DefaultHwNotchScreenWhiteConfig;
import com.android.server.notch.NotchSwitchListener;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.policy.WindowManagerPolicyEx;
import com.android.server.policy.keyguard.KeyguardStateMonitor;
import com.android.server.statusbar.IHwStatusBarManagerServiceEx;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.tv.HwTvPowerManagerPolicy;
import com.android.server.utils.CommonThread;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.ActivityRecordEx;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.ActivityTaskManagerServiceEx;
import com.android.server.wm.DisplayFrames;
import com.android.server.wm.HwMultiDisplayManager;
import com.android.server.wm.HwStartWindowRecord;
import com.android.server.wm.HwWmConstants;
import com.android.server.wm.IntelliServiceManager;
import com.android.server.wm.RootActivityContainerEx;
import com.android.server.wm.TaskRecordEx;
import com.android.server.wm.WindowManagerInternal;
import com.android.server.wm.WindowState;
import com.android.server.wm.WindowStateEx;
import com.android.server.wm.utils.HwDisplaySizeUtil;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.app.IGameObserver;
import com.huawei.android.app.WindowManagerExt;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.gameassist.HwGameAssistManager;
import com.huawei.android.inputmethod.HwInputMethodManager;
import com.huawei.android.os.HwPowerManager;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.android.view.HwExtDisplaySizeUtil;
import com.huawei.forcerotation.HwForceRotationManager;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.pgmng.log.LogPower;
import com.huawei.server.HwBasicPlatformFactory;
import com.huawei.server.HwPCFactory;
import com.huawei.server.HwPartIawareUtil;
import com.huawei.server.fingerprint.HwFpServiceToHalUtils;
import com.huawei.server.hwmultidisplay.DefaultHwMultiDisplayUtils;
import com.huawei.server.hwmultidisplay.windows.DefaultHwWindowsCastManager;
import com.huawei.server.policy.HwAccessibilityWaterMark;
import com.huawei.server.policy.WindowManagerFuncsEx;
import com.huawei.server.policy.keyguard.KeyguardServiceDelegateEx;
import com.huawei.server.security.HwServiceSecurityPartsFactoryEx;
import com.huawei.server.security.behaviorcollect.DefaultBehaviorCollector;
import com.huawei.server.wm.IHwDisplayPolicyEx;
import com.huawei.systemmanager.power.HwDeviceIdleController;
import dalvik.system.DexClassLoader;
import huawei.android.aod.HwAodManager;
import huawei.android.app.HwKeyguardManagerImpl;
import huawei.android.app.IHwWindowCallback;
import huawei.android.hardware.fingerprint.FingerprintManagerEx;
import huawei.android.hwutil.HwFullScreenDisplay;
import huawei.android.provider.FrontFingerPrintSettings;
import huawei.android.security.facerecognition.FaceReportEventToIaware;
import huawei.android.view.HwWindowManager;
import huawei.com.android.server.fingerprint.FingerViewController;
import huawei.com.android.server.policy.stylus.StylusGestureListener;
import huawei.cust.HwCustUtils;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.function.Predicate;
import vendor.huawei.hardware.tp.V1_0.ITouchscreen;

public class HwPhoneWindowManager extends PhoneWindowManager implements AccessibilityManager.TouchExplorationStateChangeListener {
    private static final String ACTION_ACTURAL_SHUTDOWN = "com.android.internal.app.SHUTDOWNBROADCAST";
    private static final String ACTION_HUAWEI_VASSISTANT_SERVICE = "com.huawei.ziri.model.MODELSERVICE";
    private static final String ACTION_SYSTEM_BUTTON_PRESS = "com.huawei.action.SYSTEM_BUTTON_PRESS";
    private static final String ACTIVITY_NAME_EMERGENCY_SIMPLIFIEDINFO = "com.android.emergency/.view.ViewSimplifiedInfoActivity";
    private static final String AOD_GOING_TO_SLEEP_ACTION = "com.huawei.aod.action.AOD_GOING_TO_SLEEP";
    private static final int AOD_TOUCH_SWITCH_CTRL = 6;
    private static final int AOD_TOUCH_TIME_INDEX = 1;
    private static final String AOD_WAKE_UP_ACTION = "com.huawei.aod.action.AOD_WAKE_UP";
    private static final String ASSOCIATE_ASSISTANT_ACTIVITY_NAME = "com.huawei.associateassistant.pcactivity.PcScreenActivity";
    private static final String ASSOCIATE_ASSISTANT_PACKAGE = "com.huawei.associateassistant";
    private static final int BACK_HOME_RECENT_DOUBLE_CLICK_TIMEOUT = 300;
    private static final String BOOT_ANIM_EXIT_PROP = "service.bootanim.exit";
    private static final String BOOT_ANIM_NOT_EXIT_PROP_VALUE = "0";
    private static final String CHARACTERISTICS = SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT);
    private static final String CHATMM_PACKAGE_NAME = "com.tencent.mm.plugin.recordvideo.activity.MMRecordUI";
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_IMMERSION = false;
    private static final int DEFAULT_MAX_POWERKEY_COUNTDOWN = 5;
    private static final int DEFAULT_RESULT_VALUE = -2;
    private static final int DISABLED_POWER_TIMEOUT = 1000;
    private static final long DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MAX = 15000;
    private static final long DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MIN = 1500;
    private static final int DISPLAY_MODE_TYPE_INDEX = 0;
    private static final String DISTANCE_WARN_INFO = "com.android.systemui/com.huawei.homevision.systemui.warningdialogs.KidsDistanceWarnActivity";
    private static final String DROP_SMARTKEY_ACTIVITY = "drop_smartkey_activity";
    private static final int EMUI_11_0 = 25;
    private static final int EMUI_VERSION = SystemProperties.getInt("ro.build.hw_emui_api_level", 0);
    private static final boolean ESD_PROTECT_ENABLE = SystemProperties.getBoolean("hw_mc.lcd.esd_protect_enable", false);
    private static final int EVENT_DURING_MIN_TIME = 500;
    private static final long FILM_REPORT_TIME_INTERVAL = 86400000;
    private static final String FILM_STATE = "film_state";
    private static final int FILM_STATE_INIT_VALUE = -1;
    private static final String FINGERPRINT_ANSWER_CALL = "fp_answer_call";
    private static final String FINGERPRINT_CAMERA_SWITCH = "fp_take_photo";
    private static final int FINGERPRINT_HARDWARE_OPTICAL = 1;
    private static final int FINGERPRINT_HARDWARE_OUTSCREEN = 0;
    private static final String FINGERPRINT_STOP_ALARM = "fp_stop_alarm";
    private static final String FINGERSENSE_JAR_PATH = "/hw_product/jar/FingerSense/hwFingerSense.jar";
    private static final int FINGER_PRINT_TOUCH_TIME_INDEX = 2;
    private static final int FLOATING_MASK = Integer.MIN_VALUE;
    private static final int FORCE_STATUS_BAR = 1;
    private static final String FRONT_FINGERPRINT_BUTTON_LIGHT_MODE = "button_light_mode";
    private static final int FRONT_FINGERPRINT_KEYCODE_HOME_UP = 515;
    private static final String FRONT_FINGERPRINT_SWAP_KEY_POSITION = "swap_key_position";
    private static final String HAPTIC_FEEDBACK_TRIKEY_SETTINGS = "physic_navi_haptic_feedback_enabled";
    private static final String HIVOICE_PRESS_TYPE_POWER = "power";
    private static final String HOMOAI_EVENT_TAG = "event_type";
    private static final String HOMOAI_PRESS_TAG = "press_type";
    private static final int HOMOKEY_LONGPRESS_EVENT = 1;
    private static final String HUAWEI_ANDROID_INCALL_UI = "com.android.incallui";
    private static final String HUAWEI_HIACTION_ACTION = "com.huawei.hiaction.HOMOAI";
    private static final String HUAWEI_HIACTION_PACKAGE = "com.huawei.hiaction";
    private static final String HUAWEI_RAPIDCAPTURE_START_MODE = "com.huawei.RapidCapture";
    private static final String HUAWEI_SCREENRECORDER_ACTION = "com.huawei.screenrecorder.Start";
    private static final String HUAWEI_SCREENRECORDER_PACKAGE = "com.huawei.screenrecorder";
    private static final String HUAWEI_SCREENRECORDER_START_MODE = "com.huawei.screenrecorder.ScreenRecordService";
    private static final String HUAWEI_SHUTDOWN_PERMISSION = "huawei.android.permission.HWSHUTDOWN";
    private static final String HUAWEI_VASSISTANT_EXTRA_START_MODE = "com.huawei.vassistant.extra.SERVICE_START_MODE";
    private static final String HUAWEI_VASSISTANT_PACKAGE = "com.huawei.vassistant";
    private static final String HUAWEI_VASSISTANT_PACKAGE_OVERSEA = "com.huawei.hiassistantoversea";
    private static final String HUAWEI_VOICE_DEBUG_BETACLUB = "com.huawei.betaclub";
    private static final String HUAWEI_VOICE_SOUNDTRIGGER_BROADCAST = "com.mmc.SOUNDTRIGGER";
    private static final String HUAWEI_VOICE_SOUNDTRIGGER_PACKAGE = "com.mmc.soundtrigger";
    private static final long HWVASSISTANT_STAY_IN_WHITELIST_TIMEOUT = 3000;
    private static final String HW_NOTEEDITOR_ACTIVITY_NAME = "com.huawei.notepad/com.huawei.android.notepad.views.SketchActivity";
    private static final int INVALID_HARDWARE_TYPE = -1;
    private static final int IN_SCREEN_OPTIC_TYPE = 1;
    private static final int IN_SCREEN_ULTRA_TYPE = 2;
    private static final boolean IS_GAME_ASSIST = (SystemProperties.getInt("ro.config.gameassist", 0) == 1);
    private static final boolean IS_HWRIDEMODE_FEATURE_SUPPORTED = SystemProperties.getBoolean("ro.config.ride_mode", false);
    private static final boolean IS_HW_EASY_WAKE_UP = SystemProperties.getBoolean("ro.config.hw_easywakeup", false);
    private static final boolean IS_LONG_HOME_VASSITANT = SystemProperties.getBoolean("ro.hw.long.home.vassistant", true);
    private static final boolean IS_OPEN_PROXIMITY_DISPALY = SystemProperties.getBoolean("ro.config.open_proximity_display", false);
    private static final boolean IS_POWER_HIACTION_KEY = SystemProperties.getBoolean("ro.config.hw_power_voice_key", true);
    private static final boolean IS_QUICK_RECORD_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_quickrecord", false);
    private static final boolean IS_SUPPORT_DOUBLETAP_PAY = SystemProperties.getBoolean("ro.config.support_doubletap_pay", false);
    private static final boolean IS_SUPPORT_FOLD_SCREEN = (!SystemProperties.get("ro.config.hw_fold_disp").isEmpty());
    private static final boolean IS_SUPPORT_HW_BEHAVIOR_AUTH = SystemProperties.getBoolean("hw_mc.authentication.behavior_auth_bot", true);
    private static final boolean IS_SUPPORT_RAPID_CAPTURE = SystemProperties.getBoolean("ro.hwcamera.fastcapture", true);
    private static final boolean IS_TABLET = "tablet".equals(CHARACTERISTICS);
    private static final boolean IS_TV = ("tv".equals(CHARACTERISTICS) || "mobiletv".equals(CHARACTERISTICS));
    private static final boolean IS_VIBRATE_IMPLEMENTED = false;
    private static final List KEYCODE_NOT_FOR_AA = Arrays.asList(4, 3, 187, 25, 24, 220, 221, 164, 120);
    private static final List KEYCODE_NOT_FOR_CLOUD = Arrays.asList(4, 3, 25, 24, 220, 221);
    private static final String KEY_DOUBLE_TAP_PAY = "double_tap_enable_pay";
    private static final String KEY_HIVOICE_PRESS_TYPE = "invoke_hivoice_keypress_type";
    private static final String KEY_HWOUC_KEYGUARD_VIEW_ON_TOP = "hwouc_keyguard_view_on_top";
    private static final String KEY_POWER_LONGPRESS_TIMEOUT = "power_longpress_timeout";
    private static final String KEY_TOAST_POWER_OFF = "toast_power_off";
    private static final String KEY_TOUCH_DISABLE_MODE = "touch_disable_mode";
    private static final String KIDSMODE = "kids_mode_is_open";
    private static final long LAUNCH_VASSIT_TIMEOUT = 1000;
    private static final String[] LMT_CONFIGS = LMT_DISPLAY_CONFIG.split(",");
    private static final int LMT_CONFIG_LENGTH = 3;
    private static final String LMT_DISPLAY_CONFIG = SystemProperties.get("hw_mc.aod.support_display_style", "");
    private static final int LMT_MODE_MOVE = 1;
    private static final int LMT_MODE_MOVE_TOUCH = 3;
    private static final int LMT_MODE_TOUCH = 2;
    private static final int LMT_SWING_CONFIG_LENGTH = 4;
    private static final int LMT_SWITCH_DEFAULT = 0;
    private static final int LMT_SWITCH_MOVE_TOUCH = 3;
    private static final int LMT_SWITCH_TOUCH = 2;
    private static final int LMT_TOUCH_OFF = 0;
    private static final int LMT_TOUCH_ON = 1;
    private static final int MAX_POWERKEY_COUNTDOWN = SystemProperties.getInt("hw_mc.power_manage.active_sos_times", 5);
    private static final int MAX_POWEROFF_TOAST_SHOW = 2;
    private static final int MSG_BUTTON_LIGHT_TIMEOUT = 4099;
    private static final int MSG_DISABLED_POWER_KEY = 4104;
    private static final int MSG_DISABLE_SWING = 106;
    private static final int MSG_ENABLE_SWING = 105;
    private static final int MSG_FREEZE_POWER_KEY = 4103;
    private static final int MSG_LAUNCH_WATCH_VASSISTANT = 112;
    private static final int MSG_NAVIBAR_DISABLE = 104;
    private static final int MSG_NAVIBAR_ENABLE = 103;
    private static final int MSG_NOTIFY_FINGER_OPTICAL = 4100;
    private static final int MSG_POWER_KEY_PRESS = 109;
    private static final int MSG_SCREEN_ON_EX_FINISHED = 108;
    private static final int MSG_SEND_KEYEVENT = 111;
    private static final int MSG_SET_FOLD_MODE_FINISHED = 107;
    private static final int MSG_TRIKEY_BACK_LONG_PRESS = 4097;
    private static final int MSG_TRIKEY_RECENT_LONG_PRESS = 4098;
    private static final int MSG_TV_CUSTOM_BUTTON_PRESS = 113;
    private static final int MSG_VOICE_ASSIST_KEY_PRESS = 110;
    private static final String NEW_HWNOTEPAD_PKG_NAME = "com.huawei.notepad";
    private static final int NON_TOUGHENED_FILM = 1;
    private static final int NOTCH_ROUND_CORNER_CODE = 8002;
    private static final int NOTCH_ROUND_CORNER_HIDE = 0;
    private static final int NOTCH_ROUND_CORNER_SHOW = 1;
    private static final int NOTCH_ROUND_CORNER_SIDE_COMPRESS = 2;
    private static final int NOTCH_ROUND_CORNER_SIDE_EXPAND = 1;
    private static final int NOTCH_STATUS_BAR_BLACK = 1;
    private static final int NOTCH_STATUS_BAR_DEFAULT = 0;
    private static final String NOTEEDITOR_ACTIVITY_NAME = "com.example.android.notepad/com.huawei.android.notepad.views.SketchActivity";
    private static final int NOTEPAD_START_DELAY = 500;
    private static final int NOTEPAD_START_DURATION = 2000;
    private static final int NOTIFY_HIACTION_LONGPRESS_TYPE_POWER = 2;
    private static final String PACKAGE_NAME_HWPCASSISTANT = "com.huawei.pcassistant";
    private static final String PARENTS_INFO = "com.huawei.homevision.kidsmode/.activity.KidsParentalConfirmActivity";
    private static final String PERMISSION_MDM_DEVICE_MANAGER = "com.huawei.permission.sec.MDM_DEVICE_MANAGER";
    private static final String PERMISSION_RECEIVE_SYSTEM_BUTTON = "com.huawei.permission.SYSTEM_BUTTON_PRESS";
    private static final String PKG_CALCULATOR = "com.huawei.calculator";
    private static final String PKG_CALCULATOR_OLD = "com.android.calculator2";
    private static final String PKG_CAMERA = "com.huawei.camera";
    private static final String PKG_GALLERY = "com.huawei.photos";
    private static final String PKG_GALLERY_OLD = "com.android.gallery3d";
    private static final String PKG_HWNOTEPAD = "com.example.android.notepad";
    private static final String PKG_NAME_EMERGENCY = "com.android.emergency";
    private static final String PKG_SCANNER = "com.huawei.scanner";
    private static final String PKG_SOUNDRECORDER = "com.huawei.soundrecorder";
    private static final String PKG_SOUNDRECORDER_OLD = "com.android.soundrecorder";
    private static final String POSITION_INFO = "com.android.systemui/com.huawei.homevision.systemui.warningdialogs.KidsPositionActivity";
    private static final int POWERKEY_LONG_PRESS_TIMEOUT = 700;
    private static final long POWER_SOS_MISTOUCH_THRESHOLD = 300;
    private static final long POWER_SOS_TIMEOUT = 500;
    private static final String PROXIMITY_UI_WINDOW_TITLE = "Emui:ProximityWnd";
    private static final String PROXIMITY_VIEW_TAG = "HwScreenOnProximityLock";
    private static final String REMOTE_HOUSEKEEPING_INFO = "com.huawei.homevision.kidsmode/.activity.HouseKeepingActivity";
    private static final String RING_VOLUMEBAR_DISP = SystemProperties.get("ro.config.hw_curved_side_disp", "");
    private static final long SCREENRECORDER_DEBOUNCE_DELAY_MILLIS = 150;
    private static final int SCREEN_CHANGE_REASON_FEATURE = 4;
    private static final String SCREEN_POWER_CHANGED_ACTION = "com.huawei.action.ACTION_SCREEN_POWER_CHANGED";
    private static final String SCREEN_POWER_CHANGED_FLAG_EXTRA = "flag";
    private static final String SHUTDOWN_MENU_DISABLED = "com.huawei.homevision.SHUTDOWN_MENU_DISABLED";
    private static final int SIDE_POWER_FP_COMB = 3;
    private static final int SINGLE_HAND_STATE = 1989;
    private static final int START_MODE_QUICK_START_CALL = 2;
    static final int START_MODE_VOICE_WAKEUP_ONE_SHOT = 4;
    private static final int SUCCESS_RETURN_VALUE = 0;
    static final String TAG = "HwPhoneWindowManager";
    private static final String TIME_FINISH_INFO = "com.android.systemui/com.huawei.homevision.systemui.warningdialogs.KidsTimeFinishActivity";
    private static final int TOAST_POWER_OFF_TIMEOUT = 1000;
    private static final int TOAST_TYPE_COVER_SCREEN = 2101;
    private static final long TOUCH_DISABLE_DEBOUNCE_DELAY_MILLIS = 150;
    private static final int TOUCH_EXPLR_NAVIGATION_BAR_COLOR = -16777216;
    private static final int TOUCH_EXPLR_STATUS_BAR_COLOR = -16777216;
    private static final long TOUCH_SPINNING_DELAY_MILLIS = 2000;
    private static final int TOUGHENED_FILM = 2;
    private static final int TP_HAL_DEATH_COOKIE = 1001;
    private static final String TV_COMMAND_SERVICE_PERMISSION = "com.huawei.homevision.permission.TV_COMMAND_SERVICE";
    private static final String TV_CUSTOM_BUTTON_DOWN_ACTION = "com.huawei.homevision.action.CUSTOM_BUTTON_PRESS";
    private static final String TV_CUSTOM_KEY_CODE = "keyCode";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final int UNDEFINED_TYPE = -1;
    private static final String UPDATE_GESTURE_NAV_ACTION = "huawei.intent.action.FWK_UPDATE_GESTURE_NAV_ACTION";
    private static final String UPDATE_NOTCH_SCREEN_ACTION = "huawei.intent.action.FWK_UPDATE_NOTCH_SCREEN_ACTION";
    private static final String UPDATE_NOTCH_SCREEN_PER = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    private static final String VIBRATE_ON_TOUCH = "vibrate_on_touch";
    private static final int VIBRATOR_LONG_PRESS_FOR_FRONT_FP = SystemProperties.getInt("ro.config.trikey_vibrate_press", 16);
    private static final int VIBRATOR_SHORT_PRESS_FOR_FRONT_FP = SystemProperties.getInt("ro.config.trikey_vibrate_touch", 8);
    private static String VOICE_ASSISTANT_ACTION = "com.huawei.action.VOICE_ASSISTANT";
    private static final String VOICE_ASSIST_KEY_DOWN_ACTION = "com.huawei.key.action.VOICE_ASSIST_KEY_DOWN";
    private static final String VOICE_ASSIST_KEY_UP_ACTION = "com.huawei.key.action.VOICE_ASSIST_KEY_UP";
    private static final long VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT = 400;
    private static final long VOLUMEDOWN_LONG_PRESS_TIMEOUT = 500;
    private static final int VOLUME_CACHE_TIMEOUT_MS = 2000;
    private static final float VOLUME_INVALID_DATA = -1.0f;
    private static final float VOLUME_MAX_PROGRESS = 1000.0f;
    private static final long WAITING_SCREEN_ON_EX_TIMEOUT = 1500;
    private static final int WAKEUP_NOTEEDITOR_TIMEOUT = 500;
    private static final String WATCH_DESKCLOCK_PACKAGE_NAME = "com.huawei.harmonyos.deskclock";
    private static final String WATCH_DESKCLOCK_WINDOW_TITLE = "com.huawei.harmonyos.deskclock.alarmclock.AlarmClockAlertAbilityShellActivity";
    private static final String WATCH_HEALTHSPORT_PACKAGE_NAME = "com.huawei.watch.healthsport";
    private static final String WATCH_HEALTHSPORT_WINDOW_TITLE = "PassPowerKey";
    private static final String WATCH_INCALLUI_PACKAGE_NAME = "com.huawei.harmonyos.call";
    private static final String WATCH_INCALLUI_WINDOW_TITLE = "com.huawei.harmonyos.call.InCallAbility";
    private static final String WATCH_RECENT_ACTION = "com.huawei.watch.home.action.START_RECENTS";
    private static final String WATCH_RECENT_PACKAGE_NAME = "com.huawei.watch.home";
    private static final String WATCH_SOS_ACTION = "com.huawei.watch.care.action.START_SOS_MAIN_PAGE";
    private static final int WATCH_SOS_EXTRA_TRANSFER = 2;
    private static final String WATCH_SOS_PACKAGE_NAME = "com.huawei.watch.care";
    private static final String WATCH_VASSISTANT_ACTION = "com.huawei.watch.vassistant.action.START_FROM_BUTTON";
    private static final String WATCH_VASSISTANT_PACKAGE_NAME = "com.huawei.watch.vassistant";
    private static final String WATCH_VOICE_SWITCH = "hw_wake_up_by_voice";
    private static final int WATCH_VOICE_SWITCH_ON = 1;
    private static boolean mIsNeedMuteByPowerKeyDown = false;
    private static boolean mIsSidePowerFpComb = false;
    private static PhoneStateListener mPhoneListener = new PhoneStateListener() {
        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass1 */

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            Log.i(HwPhoneWindowManager.TAG, "PhoneState = " + state);
            boolean unused = HwPhoneWindowManager.mIsNeedMuteByPowerKeyDown = state == 1;
            if (state == 1) {
                HwPhoneWindowManager.sHwMultiDisplayUtils.lightScreenOnForHwMultiDisplay();
            }
        }
    };
    private static int[] mUnableWakeKeys;
    private static DexClassLoader sDexClassLoader;
    private static DefaultHwMultiDisplayUtils sHwMultiDisplayUtils = HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwMultiDisplayUtils();
    private static DefaultHwWindowsCastManager sHwWindowsCastManager = HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwWindowsCastManager();
    private static boolean sIsCustBeInit = false;
    private static boolean sIsCustUsed = false;
    private static boolean sIsSwitch = false;
    private static Set<Integer> sSpecialWindowType = new HashSet();
    private static Set<Integer> sSpecialWindowingModes = new HashSet();
    private final int KEYGUARD_PROXIMITY_OFF = 21;
    private final int KEYGUARD_PROXIMITY_ON = 20;
    private int TRIKEY_NAVI_DEFAULT_MODE = -1;
    private DefaultBehaviorCollector defaultBehaviorCollector = null;
    DefaultFingerprintActionsListener fingerprintActionsListener;
    private DefaultHwNotchScreenWhiteConfig hwNotchScreenWhiteConfig;
    boolean isHomeAndDBothDown = false;
    boolean isHomeAndEBothDown = false;
    boolean isHomeAndLBothDown = false;
    boolean isHomeAndOtherKeyBothDown = false;
    boolean isHomePressDown = false;
    private boolean isHwOUCKeyguardViewOnTop = false;
    private int lastDensityDpi = -1;
    private final Runnable mAIPowerLongPressed = new Runnable() {
        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass27 */

        @Override // java.lang.Runnable
        public void run() {
            Log.i(HwPhoneWindowManager.TAG, "handle power long press");
            HwPhoneWindowManager hwPhoneWindowManager = HwPhoneWindowManager.this;
            hwPhoneWindowManager.mPowerKeyHandledByHiaction = true;
            hwPhoneWindowManager.handleHomoAiKeyLongPress(2);
        }
    };
    private HwAccessibilityWaterMark mAccessbilityWaterMark = null;
    private final IZrHung mAppEyeBackKey = HwFrameworkFactory.getZrHung("appeye_backkey");
    private final IZrHung mAppEyeHomeKey = HwFrameworkFactory.getZrHung("appeye_homekey");
    private AudioManager mAudioManager = null;
    private long mBackKeyPressTime = 0;
    private Light mBackLight = null;
    volatile boolean mBackTrikeyHandled;
    private int mBarVisibility = 1;
    private PowerManager.WakeLock mBlePowerKeyWakeLock;
    boolean mBooted = false;
    private PowerManager.WakeLock mBroadcastWakeLock;
    private Light mButtonLight = null;
    private int mButtonLightMode = 1;
    private final Runnable mCancleInterceptFingerprintEvent = new Runnable() {
        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass26 */

        @Override // java.lang.Runnable
        public void run() {
            HwPhoneWindowManager.this.mIsNeedDropFingerprintEvent = false;
        }
    };
    private CoverManager mCoverManager = null;
    private int mCurUser;
    private float mCurVolume = VOLUME_INVALID_DATA;
    private int mCurrenSideMode = 1;
    private int mCurrentNotchMode = 0;
    HwCustPhoneWindowManager mCust = ((HwCustPhoneWindowManager) HwCustUtils.createObj(HwCustPhoneWindowManager.class, new Object[0]));
    int mDesiredRotation = -1;
    private DiagnosisDetector mDetector;
    private int mDeviceNodeFD = -2147483647;
    private HwDisplaySidePolicy mDisplaySidePolicy;
    private DefaultHwFalseTouchMonitor mFalseTouchMonitor = null;
    private int mFilmState;
    private int mFingerPrintId = -2;
    private int mFingerprintHardwareType = -1;
    private ContentObserver mFingerprintObserver;
    private int mFingerprintType = -1;
    private int mFoldDisplayMode = 0;
    private HwFoldScreenManagerInternal mFoldScreenManagerService;
    private FoldScreenOnUnblocker mFoldScreenOnUnblocker;
    private final Runnable mFoldWindowDrawCallback = new Runnable() {
        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass32 */

        @Override // java.lang.Runnable
        public void run() {
            if (PhoneWindowManager.DEBUG_WAKEUP) {
                Slog.i(HwPhoneWindowManager.TAG, "All windows draw complete for fold screen");
            }
            synchronized (HwPhoneWindowManager.this.mScreenOnExLock) {
                HwPhoneWindowManager.this.mHandlerEx.sendEmptyMessage(108);
            }
        }
    };
    private Class mFsManagerCls = null;
    private Object mFsManagerObj = null;
    private HwGameDockGesture mGameDockGesture = null;
    private DefaultGestureNavManager mGestureNavPolicy;
    private final Runnable mHandleVolumeDownKey = new Runnable() {
        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass17 */

        @Override // java.lang.Runnable
        public void run() {
            if (HwPhoneWindowManager.this.isMusicActive()) {
                HwPhoneWindowManager.this.handleVolumeKey(3, 25);
            }
        }
    };
    private Handler mHandlerEx;
    private String mHiVoiceKeyType;
    private HwGameSpaceToggleManager mHwGameSpaceToggleManager = null;
    private HwMultiDisplayManager mHwMultiDisplayManager = null;
    private ContentObserver mHwOUCObserver;
    private DefaultHwScreenOnProximityLock mHwScreenOnProximityLock;
    public IHwWindowCallback mIHwWindowCallback;
    private boolean mInPCMultiWindowModeLastHomeKeyDown = false;
    private boolean mIsActuralShutDown = false;
    private boolean mIsAppWindow = false;
    private boolean mIsBackKeyPress = false;
    private boolean mIsCanBeSearched = false;
    private boolean mIsCoverOpen = true;
    private boolean mIsDeviceProvisioned = false;
    private boolean mIsDoubleTapPayEnabled = false;
    private boolean mIsEnableKeyInCurrentFgGameApp;
    private boolean mIsFingerAnswerPhoneOn = false;
    private boolean mIsFingerShotCameraOn = false;
    private boolean mIsFingerStopAlarmOn = false;
    private boolean mIsFirstSetCornerDefault = true;
    private boolean mIsFirstSetCornerInLandNoNotch = true;
    private boolean mIsFirstSetCornerInLandNotch = true;
    private boolean mIsFirstSetCornerInPort = true;
    private boolean mIsFirstSetCornerInReversePortait = true;
    private boolean mIsForceSetStatusBar = false;
    private boolean mIsFreezePowerkey = false;
    private boolean mIsFromNonInteractive;
    private boolean mIsHapticEnabled = true;
    private boolean mIsHeadless;
    private boolean mIsHintShown;
    private boolean mIsHomeAndLBothPressed = false;
    private boolean mIsKidsMode = false;
    private boolean mIsLDown = false;
    private boolean mIsLastKeyDownDropped;
    private boolean mIsLastMuteRinger;
    private boolean mIsLayoutBelowWhenHideNotch = false;
    private boolean mIsLongPressMenuKeyHandled = true;
    private boolean mIsMenuClickedOnlyOnce = false;
    private boolean mIsMenuKeyPress = false;
    private boolean mIsNavibarAlignLeftWhenLand;
    private boolean mIsNeedDropFingerprintEvent = false;
    private boolean mIsNeedHide = false;
    public boolean mIsNoneNotchAppInHideMode = false;
    protected boolean mIsNotchSwitchOpen = false;
    private boolean mIsNotchTemp;
    private boolean mIsPowerKeyDisTouch;
    private boolean mIsProximity = false;
    private boolean mIsProximitySensorEnabled = false;
    private boolean mIsProximityTop = SystemProperties.getBoolean("ro.config.proximity_top", false);
    private boolean mIsRestoreStatusBar = false;
    private boolean mIsScreenOnForFalseTouch;
    private boolean mIsScreenRecorderChordEnabled = true;
    private boolean mIsScreenRecorderPowerKeyTriggered;
    private boolean mIsScreenRecorderVolumeDownKeyTriggered;
    private boolean mIsScreenRecorderVolumeUpKeyConsumed;
    private boolean mIsScreenRecorderVolumeUpKeyTriggered;
    private boolean mIsScreenTurnedOn;
    private boolean mIsSensorRegisted = false;
    public boolean mIsSkipUpdateSideAndCorner = false;
    private boolean mIsStrm1DownIntercepted;
    private boolean mIsSwingMotionOrEyeGazeEnable = false;
    private boolean mIsTouchExplrEnabled;
    private boolean mIsVoiceRecognitionActive;
    private boolean mIsVolumeDownKeyDisTouch;
    private boolean mIsVolumeUpKeyConsumedByDisTouch;
    private boolean mIsVolumeUpKeyDisTouch;
    private boolean mIsWatchAssistTriggered;
    private String[] mKeyguardShortcutApps = {PKG_CAMERA, PKG_GALLERY, PKG_GALLERY_OLD, PKG_SCANNER};
    private WindowManagerPolicy.WindowState mLastColorWin;
    private String mLastFgPackageName;
    private long mLastFilmReportTimeMs;
    private int mLastIsEmuiStyle;
    private int mLastKeyDownKeyCode;
    private long mLastKeyDownTime;
    private long mLastKeyPointerTime = 0;
    private int mLastNavigationBarColor;
    private long mLastPowerKeyDownTime = 0;
    private long mLastStartVassistantServiceTime;
    private int mLastStatusBarColor;
    private long mLastVolumeCacheTime;
    private long mLastVolumeDownKeyDownTime;
    private long mLastVolumeKeyDownTime = 0;
    private long mLastWakeupTime = 0;
    public WindowManagerPolicy.WindowState mLighterDrawView;
    private ProximitySensorListener mListener = null;
    private WindowManagerPolicyConstants.PointerEventListener mLockScreenBuildInDisplayListener;
    private WindowManagerPolicyConstants.PointerEventListener mLockScreenListener;
    private final Runnable mLongPressMenuActionForTv = new Runnable() {
        /* class com.android.server.policy.$$Lambda$HwPhoneWindowManager$vh16gQmdzL1ZEgezpDuy7dDSxKo */

        @Override // java.lang.Runnable
        public final void run() {
            HwPhoneWindowManager.this.lambda$new$0$HwPhoneWindowManager();
        }
    };
    private String[] mLsKeyguardShortcutApps = {PKG_SOUNDRECORDER, PKG_CALCULATOR, PKG_CALCULATOR_OLD, PKG_HWNOTEPAD, PKG_SOUNDRECORDER_OLD, NEW_HWNOTEPAD_PKG_NAME};
    private long mMenuKeyPressTime = 0;
    boolean mNaviBarStateInited = false;
    boolean mNavibarEnabled = false;
    private NotchSwitchListener mNotchSwitchListener;
    OverscanTimeout mOverscanTimeout = new OverscanTimeout();
    private int mPowerKeyCount = 0;
    private long mPowerKeyDisTouchTime;
    private int mPowerLongPressTimeout;
    private final Runnable mPowerOffRunner = new Runnable() {
        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass28 */

        @Override // java.lang.Runnable
        public void run() {
            HwPhoneWindowManager.this.powerOffToast();
        }
    };
    private final SensorEventListener mProximitySensorListener = new SensorEventListener() {
        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass31 */

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    final Runnable mProximitySensorTimeoutRunnable = new Runnable() {
        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass3 */

        @Override // java.lang.Runnable
        public void run() {
            Log.i(HwPhoneWindowManager.TAG, "mProximitySensorTimeout, unRegisterListener");
            HwPhoneWindowManager.this.turnOffSensorListener();
        }
    };
    volatile boolean mRecentTrikeyHandled;
    private final Runnable mRemovePowerSaveWhitelistRunnable = new Runnable() {
        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass25 */

        @Override // java.lang.Runnable
        public void run() {
            try {
                if (HwPhoneWindowManager.this.checkPackageInstalled(HwPhoneWindowManager.HUAWEI_VASSISTANT_PACKAGE)) {
                    HwDeviceIdleController.removePowerSaveWhitelistApp(HwPhoneWindowManager.HUAWEI_VASSISTANT_PACKAGE);
                } else if (HwPhoneWindowManager.this.checkPackageInstalled(HwPhoneWindowManager.HUAWEI_VASSISTANT_PACKAGE_OVERSEA)) {
                    HwDeviceIdleController.removePowerSaveWhitelistApp(HwPhoneWindowManager.HUAWEI_VASSISTANT_PACKAGE_OVERSEA);
                } else {
                    Log.w(HwPhoneWindowManager.TAG, "vassistant not exists");
                }
            } catch (RemoteException e) {
                Slog.e(HwPhoneWindowManager.TAG, "remove hwvassistant exception!");
            }
        }
    };
    private ContentResolver mResolver;
    private long mScreenOffTime = 0;
    private WindowManagerPolicy.ScreenOnExListener mScreenOnExListener;
    private final Object mScreenOnExLock = new Object();
    private long mScreenRecorderPowerKeyTime;
    private final Runnable mScreenRecorderRunnable = new Runnable() {
        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass12 */

        @Override // java.lang.Runnable
        public void run() {
            Intent intent = new Intent();
            intent.setAction(HwPhoneWindowManager.HUAWEI_SCREENRECORDER_ACTION);
            intent.setClassName(HwPhoneWindowManager.HUAWEI_SCREENRECORDER_PACKAGE, HwPhoneWindowManager.HUAWEI_SCREENRECORDER_START_MODE);
            HwPhoneWindowManager.this.powerPressBDReport(991310987);
            try {
                HwPhoneWindowManager.this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
            } catch (Exception e) {
                Slog.e(HwPhoneWindowManager.TAG, "unable to start screenrecorder service: " + intent);
            }
            Log.d(HwPhoneWindowManager.TAG, "start screen recorder service");
        }
    };
    private long mScreenRecorderVolumeUpKeyTime;
    private final Object mScreenTurnedOnLock = new Object();
    private int mSecondToLastKeyDownKeyCode;
    private long mSecondToLastKeyDownTime;
    private SensorManager mSensorManager = null;
    final Object mServiceAquireLock = new Object();
    private final ServiceNotification mServiceNotification = new ServiceNotification();
    private SettingsObserver mSettingsObserver;
    private BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                Slog.i(HwPhoneWindowManager.TAG, "onReceive action: " + action);
                if (HwPhoneWindowManager.ACTION_ACTURAL_SHUTDOWN.equals(action)) {
                    HwPhoneWindowManager.this.mIsActuralShutDown = true;
                }
            }
        }
    };
    private final List<String> mSpecialPackage = Arrays.asList(HwCustPkgNameConstant.HW_PERMISSION_CONTROLLER_PACKAGE);
    private final List<Integer> mSpecialWindowType = Arrays.asList(2003, Integer.valueOf((int) HwArbitrationDEFS.MSG_RECOVERY_FLAG_BY_WIFI_RX_BYTES), Integer.valueOf((int) HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL), Integer.valueOf((int) HwArbitrationDEFS.MSG_ARBITRATION_REQUEST_MPLINK));
    boolean mStatuBarObsecured;
    private IHwStatusBarManagerServiceEx mStatusBarManagerServiceEx = null;
    IStatusBarService mStatusBarService;
    private long mStrm1DownTime;
    private StylusGestureListener mStylusGestureListener = null;
    private StylusGestureListener mStylusGestureListener4PCMode = null;
    private SubScreenViewEntry mSubScreenViewEntry;
    private WindowManagerPolicyConstants.PointerEventListener mSwingMotionPointerEventListener = new WindowManagerPolicyConstants.PointerEventListener() {
        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass22 */

        public void onPointerEvent(MotionEvent motionEvent) {
            IHwSwingService hwSwing = HwSwingManager.getService();
            if (hwSwing != null) {
                try {
                    int action = motionEvent.getAction();
                    if (action == 0) {
                        hwSwing.notifyFingersTouching(true);
                    } else if (action == 1 || action == 3) {
                        hwSwing.notifyFingersTouching(false);
                    }
                } catch (RemoteException e) {
                    Log.e(HwPhoneWindowManager.TAG, "notifyFingersTouching error : " + e.getMessage());
                }
            }
        }
    };
    private TouchCountPolicy mTouchCountPolicy = new TouchCountPolicy();
    private int mTpDeviceId = -1;
    private ITouchscreen mTpTouchSwitch = null;
    private int mTrikeyNaviMode = -1;
    private HwTvPowerManagerPolicy mTvPolicy;
    private SystemVibrator mVibrator;
    private final Runnable mVolumeDownLongPressed = new Runnable() {
        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass18 */

        @Override // java.lang.Runnable
        public void run() {
            HwPhoneWindowManager.this.cancelVolumeDownKeyPressed();
            if (!HwPhoneWindowManager.this.mIsProximity || !HwPhoneWindowManager.this.mIsSensorRegisted) {
                HwPhoneWindowManager.this.notifyVassistantService("start", 2, null);
            }
            HwPhoneWindowManager.this.turnOffSensorListener();
            HwPhoneWindowManager.this.mIsVoiceRecognitionActive = true;
            HwPhoneWindowManager.this.mLastStartVassistantServiceTime = SystemClock.uptimeMillis();
        }
    };
    private PowerManager.WakeLock mVolumeDownWakeLock;
    private long mVolumeUpKeyDisTouchTime;
    private long mWatchPowerUpTime;
    private BroadcastReceiver mWhitelistReceived = new BroadcastReceiver() {
        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass9 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || context == null || intent.getAction() == null) {
                Slog.i(HwPhoneWindowManager.TAG, "intent is " + intent + "context is " + context);
                return;
            }
            if (PhoneWindowManager.IS_NOTCH_PROP && HwPhoneWindowManager.UPDATE_NOTCH_SCREEN_ACTION.equals(intent.getAction())) {
                String fileName = intent.getStringExtra("uri");
                Slog.i(HwPhoneWindowManager.TAG, "fileName:" + fileName);
                if (fileName != null) {
                    HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwNotchScreenWhiteConfig().updateWhitelistByHot(context, fileName);
                }
            }
            if (HwPhoneWindowManager.UPDATE_GESTURE_NAV_ACTION.equals(intent.getAction())) {
                String fileName2 = intent.getStringExtra("uri");
                Slog.i(HwPhoneWindowManager.TAG, "fileName:" + fileName2);
                if (fileName2 != null) {
                    HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwGestureNavWhiteConfig().updateWhitelistByHot(context, fileName2);
                }
            }
        }
    };
    private HashSet<String> needDropSmartKeyActivities = new HashSet<>();
    private int showPowerOffToastTimes = 0;

    static {
        sSpecialWindowingModes.add(5);
        sSpecialWindowingModes.add(102);
        sSpecialWindowingModes.add(2);
        sSpecialWindowingModes.add(105);
        sSpecialWindowType.add(Integer.valueOf((int) HwArbitrationDEFS.MSG_RECOVERY_FLAG_BY_WIFI_RX_BYTES));
        sSpecialWindowType.add(Integer.valueOf((int) HwArbitrationDEFS.MSG_HISTREAM_TRIGGER_MPPLINK_INTERNAL));
    }

    /* JADX WARN: Type inference failed for: r4v20, types: [com.android.server.policy.DefaultEasyWakeUpManager, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void systemReady() {
        HwPhoneWindowManager.super.systemReady();
        this.mHandler.post(new Runnable() {
            /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                HwPhoneWindowManager.this.initQuickcall();
                HwPhoneWindowManager.this.filmStateReporter();
            }
        });
        this.mHwMultiDisplayManager = HwMultiDisplayManager.getInstance(this.mWindowManagerInternal.getActivityTaskManagerService());
        if (IS_NOTCH_PROP) {
            this.hwNotchScreenWhiteConfig = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwNotchScreenWhiteConfig();
            HwFullScreenDisplay.setNotchHeight(this.mContext.getResources().getDimensionPixelSize(17105445));
        }
        if (IS_SUPPORT_FOLD_SCREEN) {
            this.mFoldScreenManagerService = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
            this.mFoldScreenOnUnblocker = new FoldScreenOnUnblocker();
        }
        if (HwDisplaySizeUtil.hasSideInScreen()) {
            HwDisplaySideRegionConfig.getInstance();
        }
        Handler proximityHandler = CommonThread.getHandler();
        proximityHandler.post(new Runnable(proximityHandler) {
            /* class com.android.server.policy.$$Lambda$HwPhoneWindowManager$ggIKDkhc3Nk9AuTA6dZSIyh5INw */
            private final /* synthetic */ Handler f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwPhoneWindowManager.this.lambda$systemReady$1$HwPhoneWindowManager(this.f$1);
            }
        });
        if (IS_HW_EASY_WAKE_UP && this.mSystemReady) {
            KeyguardServiceDelegateEx keyguardDelegateEx = new KeyguardServiceDelegateEx();
            keyguardDelegateEx.setKeyguardServiceDelegate(this.mKeyguardDelegate);
            ?? easyWakeUpManager = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getEasyWakeUpManager(this.mContext, this.mHandler, keyguardDelegateEx);
            ServiceManager.addService("easywakeup", (IBinder) easyWakeUpManager);
            easyWakeUpManager.saveTouchPointNodePath();
        }
        if (this.mListener == null) {
            this.mListener = new ProximitySensorListener();
        }
        this.mResolver = this.mContext.getContentResolver();
        this.TRIKEY_NAVI_DEFAULT_MODE = FrontFingerPrintSettings.getDefaultNaviMode();
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mVibrator = (SystemVibrator) ((Vibrator) this.mContext.getSystemService("vibrator"));
        if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
            LightsManager lights = (LightsManager) LocalServices.getService(LightsManager.class);
            this.mButtonLight = lights.getLight(2);
            this.mBackLight = lights.getLight(0);
            IntentFilter filter = new IntentFilter();
            filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
            this.mContext.registerReceiver(new ScreenBroadcastReceiver(), filter);
        }
        this.mFalseTouchMonitor = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwFalseTouchMonitor();
        this.mGameDockGesture = (HwGameDockGesture) LocalServices.getService(HwGameDockGesture.class);
        this.mGameDockGesture.systemReadyAndInit(this.mWindowManagerFuncs, this.mGestureNavPolicy);
        if (IS_GAME_ASSIST) {
            this.mFingerPrintId = SystemProperties.getInt("sys.fingerprint.deviceId", -2);
            ActivityManagerEx.registerGameObserver(new IGameObserver.Stub() {
                /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass5 */

                public void onGameStatusChanged(String packageName, int event) {
                    Log.i(HwPhoneWindowManager.TAG, "currentFgApp=" + packageName + ", mLastFgPackageName=" + HwPhoneWindowManager.this.mLastFgPackageName);
                    if (packageName != null && !packageName.equals(HwPhoneWindowManager.this.mLastFgPackageName)) {
                        HwPhoneWindowManager.this.mIsEnableKeyInCurrentFgGameApp = false;
                        if (ActivityManagerEx.isGameDndOn()) {
                            HwPhoneWindowManager.this.registerBuoyListener();
                            if (HwPhoneWindowManager.this.defaultBehaviorCollector != null) {
                                HwPhoneWindowManager.this.defaultBehaviorCollector.notifyEvent(1);
                            }
                        } else {
                            HwPhoneWindowManager.this.unRegisterBuoyListener();
                            if (HwPhoneWindowManager.this.defaultBehaviorCollector != null) {
                                HwPhoneWindowManager.this.defaultBehaviorCollector.notifyEvent(2);
                            }
                        }
                    }
                    HwPhoneWindowManager.this.mLastFgPackageName = packageName;
                }

                public void onGameListChanged() {
                }
            });
        }
        IGameObserver.Stub gameObserver = this.mSubScreenViewEntry;
        if (gameObserver != null) {
            gameObserver.init();
        }
        WindowManagerPolicyEx.WindowManagerFuncsEx funcsEx = new WindowManagerPolicyEx.WindowManagerFuncsEx();
        funcsEx.setWindowManagerFuncs(this.mWindowManagerFuncs);
        KeyguardServiceDelegateEx delegateEx = new KeyguardServiceDelegateEx();
        delegateEx.setKeyguardServiceDelegate(this.mKeyguardDelegate);
        HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.PICK_UP_WAKE_SCREEN_PART_FACTORY_IMPL).getPickUpWakeScreenManager().initIfNeed(this.mContext, this.mHandler, funcsEx, delegateEx);
        this.mTpDeviceId = getInputDeviceId(MSG_TRIKEY_RECENT_LONG_PRESS);
        Message msg = this.mHandlerEx.obtainMessage(MSG_NOTIFY_FINGER_OPTICAL);
        msg.setAsynchronous(true);
        this.mHandlerEx.sendMessage(msg);
        mIsSidePowerFpComb = getPowerFpType();
        if (ActivityManagerEx.isGameDndOn()) {
            registerBuoyListener();
        }
        HwAccessibilityWaterMark hwAccessibilityWaterMark = this.mAccessbilityWaterMark;
        if (hwAccessibilityWaterMark != null) {
            hwAccessibilityWaterMark.systemReady();
        }
        HwGameSpaceToggleManager hwGameSpaceToggleManager = this.mHwGameSpaceToggleManager;
        if (hwGameSpaceToggleManager != null) {
            hwGameSpaceToggleManager.init();
        }
        this.mTvPolicy = (HwTvPowerManagerPolicy) LocalServices.getService(HwTvPowerManagerPolicy.class);
        this.mStatusBarManagerServiceEx = HwServiceExFactory.getHwStatusBarManagerServiceEx();
    }

    public /* synthetic */ void lambda$systemReady$1$HwPhoneWindowManager(Handler proximityHandler) {
        WindowManagerPolicyEx.WindowManagerFuncsEx funcsEx = new WindowManagerPolicyEx.WindowManagerFuncsEx();
        funcsEx.setWindowManagerFuncs(this.mWindowManagerFuncs);
        this.mHwScreenOnProximityLock = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwScreenOnProximityLock(this.mContext, this, funcsEx, proximityHandler);
    }

    private boolean getPowerFpType() {
        String[] fpType = SystemProperties.get("ro.config.hw_fp_type").split(",");
        if (fpType.length != 4) {
            return false;
        }
        int type = -1;
        try {
            type = Integer.parseInt(fpType[0]);
        } catch (NumberFormatException e) {
            Log.e(TAG, "getPowerFpType NumberFormatException");
        }
        if (type == 3) {
            return true;
        }
        return false;
    }

    public DefaultHwScreenOnProximityLock getScreenOnProximity() {
        return this.mHwScreenOnProximityLock;
    }

    public void addPointerEvent(MotionEvent motionEvent) {
        getDefaultDisplayPolicy().getHwDisplayPolicyEx().addPointerEvent(motionEvent);
    }

    private void initNoPasswordDetectionScheme() {
        ComponentName detectorComponentName;
        String lockActivity = "";
        try {
            lockActivity = this.mContext.getResources().getString(17039867);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "DiagnosisDetector res NotFoundException:" + e);
        }
        Log.d(TAG, "DiagnosisDetector lockActivity " + lockActivity);
        if (!TextUtils.isEmpty(lockActivity) && (detectorComponentName = ComponentName.unflattenFromString(lockActivity)) != null) {
            this.mDetector = new DiagnosisDetector(this.mContext, this.mHandler, detectorComponentName);
        }
    }

    public void init(Context context, IWindowManager windowManager, WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs) {
        this.mHandlerEx = new PolicyHandlerEx();
        this.navibar_enable = "enable_navbar";
        this.mCurUser = ActivityManager.getCurrentUser();
        this.mResolver = context.getContentResolver();
        this.mFingerprintObserver = new ContentObserver(this.mHandler) {
            /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass6 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                HwPhoneWindowManager.this.updateFingerprintNav();
            }
        };
        registerFingerprintObserver(this.mCurUser);
        updateFingerprintNav();
        this.mHwOUCObserver = new ContentObserver(this.mHandler) {
            /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass7 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                HwPhoneWindowManager.this.updateHwOUCKeyguardViewState();
            }
        };
        registerHwOUCObserver(this.mCurUser);
        updateHwOUCKeyguardViewState();
        initDropSmartKey();
        HwPhoneWindowManager.super.init(context, windowManager, windowManagerFuncs);
        updateConfigBehaviors();
        context.registerReceiver(this.mShutdownReceiver, new IntentFilter(ACTION_ACTURAL_SHUTDOWN), HUAWEI_SHUTDOWN_PERMISSION, null);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mGestureNavPolicy = (DefaultGestureNavManager) LocalServices.getService(DefaultGestureNavManager.class);
        this.mAccessbilityWaterMark = new HwAccessibilityWaterMark(this.mContext);
        if (HwFoldScreenState.isFoldScreenDevice()) {
            this.mSubScreenViewEntry = new SubScreenViewEntry(context);
        }
        this.mKeyguardDelegate.setHwPCKeyguardShowingCallback(new KeyguardStateMonitor.HwPCKeyguardShowingCallback() {
            /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass8 */

            public void onShowingChanged(boolean showing) {
                if (HwPCUtils.isPcCastModeInServer() || HwPCUtils.isInWindowsCastMode()) {
                    HwPhoneWindowManager.this.lockScreen(showing);
                }
                if (HwPhoneWindowManager.this.mHwScreenOnProximityLock != null) {
                    if (!showing) {
                        HwPhoneWindowManager.this.mHwScreenOnProximityLock.releaseLock(3);
                        Log.i(HwPhoneWindowManager.PROXIMITY_VIEW_TAG, "quit mistouch view for lock go away");
                    } else {
                        Log.i(HwPhoneWindowManager.PROXIMITY_VIEW_TAG, "keyguardShowing, screen on:" + HwPhoneWindowManager.this.mIsScreenTurnedOn);
                        synchronized (HwPhoneWindowManager.this.mScreenTurnedOnLock) {
                            if (HwPhoneWindowManager.this.mIsScreenTurnedOn && HwPhoneWindowManager.this.isAcquireProximityLock()) {
                                WindowManagerPolicyEx policyEx = new WindowManagerPolicyEx();
                                policyEx.setWindowManagerPolicy(HwPhoneWindowManager.this);
                                HwPhoneWindowManager.this.mHwScreenOnProximityLock.acquireLock(policyEx, HwPhoneWindowManager.this.mFoldDisplayMode);
                            }
                        }
                    }
                }
                if (HwPhoneWindowManager.this.mAccessbilityWaterMark != null) {
                    HwPhoneWindowManager.this.mAccessbilityWaterMark.isKeyGuardShowing(showing);
                }
                Slog.i(HwPhoneWindowManager.TAG, "onShowingChangedL: " + showing);
                if (HwPhoneWindowManager.this.mSubScreenViewEntry != null) {
                    HwPhoneWindowManager.this.mSubScreenViewEntry.handleLockScreenShowChanged(showing);
                }
            }
        });
        registerNotchListener();
        registerReceivers(context);
        IZrHung iZrHung = this.mAppEyeBackKey;
        if (iZrHung != null) {
            iZrHung.init((ZrHungData) null);
        }
        IZrHung iZrHung2 = this.mAppEyeHomeKey;
        if (iZrHung2 != null) {
            iZrHung2.init((ZrHungData) null);
        }
        initTpKeepParamters();
        HwExtDisplaySizeUtil displaySizeUtil = HwExtDisplaySizeUtil.getInstance();
        if (displaySizeUtil != null && displaySizeUtil.hasSideInScreen()) {
            this.mDisplaySidePolicy = new HwDisplaySidePolicy(context);
        }
        loadFingerSenseManager();
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        if (tm != null) {
            tm.listen(mPhoneListener, 32);
        }
        this.mHwGameSpaceToggleManager = HwGameSpaceToggleManager.getInstance(this.mContext);
        initNoPasswordDetectionScheme();
    }

    private void updateConfigBehaviors() {
        this.mIsScreenRecorderChordEnabled = this.mContext.getResources().getBoolean(17891448);
    }

    private void registerReceivers(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_NOTCH_SCREEN_ACTION);
        filter.addAction(UPDATE_GESTURE_NAV_ACTION);
        context.registerReceiverAsUser(this.mWhitelistReceived, UserHandle.ALL, filter, UPDATE_NOTCH_SCREEN_PER, null);
    }

    private void registerNotchListener() {
        if (IS_NOTCH_PROP) {
            updateNotchSwitchStatus(true);
            this.mNotchSwitchListener = new NotchSwitchListener() {
                /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass10 */

                public void onChange() {
                    HwPhoneWindowManager.this.updateNotchSwitchStatus(false);
                }
            };
            HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwNotchScreenWhiteConfig().registerNotchSwitchListener(this.mContext, this.mNotchSwitchListener);
            try {
                ActivityManager.getService().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                    /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass11 */

                    public void onUserSwitching(int newUserId) throws RemoteException {
                        HwPhoneWindowManager.this.updateNotchSwitchStatus(true);
                    }
                }, TAG);
            } catch (RemoteException e) {
                Log.i(TAG, "registerUserSwitchObserver fail");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNotchSwitchStatus(boolean forceUpdate) {
        boolean oldStatus = this.mIsNotchSwitchOpen;
        boolean z = true;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "display_notch_status", 0, this.mCurUser) != 1) {
            z = false;
        }
        this.mIsNotchSwitchOpen = z;
        if (this.mIsNotchSwitchOpen != oldStatus || forceUpdate) {
            HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwNotchScreenWhiteConfig().setNotchSwitchStatus(this.mIsNotchSwitchOpen);
            if (IS_NOTCH_PROP && this.mGameDockGesture != null && HwGameDockGesture.isGameDockGestureFeatureOn()) {
                this.mGameDockGesture.updateOnNotchSwitchChange();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFingerprintNav() {
        boolean z = false;
        this.mIsFingerShotCameraOn = Settings.Secure.getIntForUser(this.mResolver, FINGERPRINT_CAMERA_SWITCH, 1, this.mCurUser) == 1;
        this.mIsFingerStopAlarmOn = Settings.Secure.getIntForUser(this.mResolver, FINGERPRINT_STOP_ALARM, 0, this.mCurUser) == 1;
        if (Settings.Secure.getIntForUser(this.mResolver, FINGERPRINT_ANSWER_CALL, 0, this.mCurUser) == 1) {
            z = true;
        }
        this.mIsFingerAnswerPhoneOn = z;
    }

    private void registerFingerprintObserver(int userId) {
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor(FINGERPRINT_CAMERA_SWITCH), true, this.mFingerprintObserver, userId);
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor(FINGERPRINT_STOP_ALARM), true, this.mFingerprintObserver, userId);
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor(FINGERPRINT_ANSWER_CALL), true, this.mFingerprintObserver, userId);
    }

    public void setCurrentUser(int userId, int[] currentProfileIds) {
        this.mCurUser = userId;
        registerFingerprintObserver(userId);
        this.mFingerprintObserver.onChange(true);
        this.mSettingsObserver.registerContentObserver(userId);
        this.mSettingsObserver.onChange(true);
        DefaultFingerprintActionsListener defaultFingerprintActionsListener = this.fingerprintActionsListener;
        if (defaultFingerprintActionsListener != null) {
            defaultFingerprintActionsListener.setCurrentUser(userId);
        }
        DefaultGestureNavManager defaultGestureNavManager = this.mGestureNavPolicy;
        if (defaultGestureNavManager != null) {
            defaultGestureNavManager.onUserChanged(userId);
        }
        HwAccessibilityWaterMark hwAccessibilityWaterMark = this.mAccessbilityWaterMark;
        if (hwAccessibilityWaterMark != null) {
            hwAccessibilityWaterMark.setCurrentUser(userId);
        }
    }

    private void registerHwOUCObserver(int userId) {
        Log.d(TAG, "register HwOUC Observer");
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor(KEY_HWOUC_KEYGUARD_VIEW_ON_TOP), true, this.mHwOUCObserver, userId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHwOUCKeyguardViewState() {
        boolean z = true;
        if (Settings.Secure.getIntForUser(this.mResolver, KEY_HWOUC_KEYGUARD_VIEW_ON_TOP, 0, this.mCurUser) != 1) {
            z = false;
        }
        this.isHwOUCKeyguardViewOnTop = z;
    }

    private boolean supportActivityForbidSpecialKey(int keyCode) {
        return this.isHwOUCKeyguardViewOnTop && (keyCode == 3 || keyCode == 4 || keyCode == 187);
    }

    public int checkAddPermission(WindowManager.LayoutParams attrs, int[] outAppOp) {
        int type = attrs.type;
        if (type == TOAST_TYPE_COVER_SCREEN) {
            outAppOp[0] = -1;
            return 0;
        } else if (type == 2104) {
            return 0;
        } else {
            return HwPhoneWindowManager.super.checkAddPermission(attrs, outAppOp);
        }
    }

    public void onKeyguardOccludedChangedLw(boolean isOccluded) {
        HwPhoneWindowManager.super.onKeyguardOccludedChangedLw(isOccluded);
        SubScreenViewEntry subScreenViewEntry = this.mSubScreenViewEntry;
        if (subScreenViewEntry != null) {
            subScreenViewEntry.handleOccludedChanged(isOccluded);
        }
        if (HwPCUtils.isInWindowsCastMode()) {
            sHwWindowsCastManager.onKeyguardOccludedChangedLw(isOccluded);
        }
    }

    public int getLastSystemUiFlags() {
        return this.mDefaultDisplayPolicy.getLastSystemUiFlags();
    }

    public int getWindowLayerFromTypeLw(int type, boolean canAddInternalSystemWindow) {
        if (type == 2100) {
            return 33;
        }
        if (type == TOAST_TYPE_COVER_SCREEN) {
            return 34;
        }
        if (type == 2104) {
            return 35;
        }
        if (type == 2105) {
            return 34;
        }
        int ret = HwPhoneWindowManager.super.getWindowLayerFromTypeLw(type, canAddInternalSystemWindow);
        return ret >= 33 ? ret + 2 : ret;
    }

    public void freezeOrThawRotation(int rotation) {
        this.mDesiredRotation = rotation;
    }

    private static boolean isMultiSimEnabled() {
        try {
            return MSimTelephonyManager.getDefault().isMultiSimEnabled();
        } catch (NoExtAPIException e) {
            Log.w(TAG, "CoverManagerService->isMultiSimEnabled->NoExtAPIException!");
            return false;
        }
    }

    private boolean isPhoneInCall() {
        if (isMultiSimEnabled()) {
            int phoneCount = MSimTelephonyManager.getDefault().getPhoneCount();
            for (int i = 0; i < phoneCount; i++) {
                if (MSimTelephonyManager.getDefault().getCallState(i) != 0) {
                    return true;
                }
            }
            return false;
        } else if (TelephonyManager.getDefault().getCallState(SubscriptionManager.getDefaultSubscriptionId()) != 0) {
            return true;
        } else {
            return false;
        }
    }

    static ITelephony getTelephonyService() {
        return ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
    }

    public boolean needTurnOff(int why) {
        if (!isPhoneInCall()) {
            return true;
        }
        if (!isKeyguardSecure(this.mCurrentUserId)) {
            return false;
        }
        if (why == 3 || why == 6) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isWakeKeyWhenScreenOff(int keyCode) {
        if (!sIsCustUsed) {
            return HwPhoneWindowManager.super.isWakeKeyWhenScreenOff(keyCode);
        }
        int i = 0;
        while (true) {
            int[] iArr = mUnableWakeKeys;
            if (i >= iArr.length) {
                return true;
            }
            if (keyCode == iArr[i]) {
                return false;
            }
            i++;
        }
    }

    public boolean isWakeKeyFun(int keyCode) {
        if (!sIsCustBeInit) {
            getKeycodeFromCust();
        }
        if (!sIsCustUsed) {
            return false;
        }
        int i = 0;
        while (true) {
            int[] iArr = mUnableWakeKeys;
            if (i >= iArr.length) {
                return true;
            }
            if (keyCode == iArr[i]) {
                return false;
            }
            i++;
        }
    }

    private void getKeycodeFromCust() {
        String[] unableWakeKeyArray;
        String unableCustomizedWakeKey = null;
        try {
            unableCustomizedWakeKey = SettingsEx.Systemex.getString(this.mContext.getContentResolver(), "unable_wake_up_key");
        } catch (Exception e) {
            Log.e(TAG, "Exception when got name value", e);
        }
        if (!(unableCustomizedWakeKey == null || (unableWakeKeyArray = unableCustomizedWakeKey.split(AwarenessInnerConstants.SEMI_COLON_KEY)) == null || unableWakeKeyArray.length == 0)) {
            mUnableWakeKeys = new int[unableWakeKeyArray.length];
            for (int i = 0; i < mUnableWakeKeys.length; i++) {
                try {
                    mUnableWakeKeys[i] = Integer.parseInt(unableWakeKeyArray[i]);
                } catch (Exception e2) {
                    Log.e(TAG, "Exception when copy the translated value from sting array to int array", e2);
                }
            }
            sIsCustUsed = true;
        }
        sIsCustBeInit = true;
    }

    public int interceptMotionBeforeQueueingNonInteractive(int displayId, long whenNanos, int policyFlags) {
        if (isTvMode()) {
            return 0;
        }
        if ((FLOATING_MASK & policyFlags) == 0) {
            return HwPhoneWindowManager.super.interceptMotionBeforeQueueingNonInteractive(displayId, whenNanos, policyFlags);
        }
        Slog.i(TAG, "interceptMotionBeforeQueueingNonInteractive policyFlags: " + policyFlags);
        Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getSingleHandState() {
        IBinder windowManagerBinder = WindowManagerGlobal.getWindowManagerService().asBinder();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (windowManagerBinder != null) {
            try {
                data.writeInterfaceToken("android.view.IWindowManager");
                windowManagerBinder.transact(1990, data, reply, 0);
                reply.readException();
                return reply.readInt();
            } catch (RemoteException e) {
                return 0;
            } finally {
                data.recycle();
                reply.recycle();
            }
        } else {
            data.recycle();
            reply.recycle();
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void unlockScreenPinningTest() {
        IBinder statusBarServiceBinder;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (!(getHWStatusBarService() == null || (statusBarServiceBinder = getHWStatusBarService().asBinder()) == null)) {
                Log.d(TAG, "Transact unlockScreenPinningTest to status bar service!");
                data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                statusBarServiceBinder.transact(111, data, reply, 0);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService->threw remote exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    public void finishedGoingToSleep(int why) {
        this.mHandler.removeCallbacks(this.mOverscanTimeout);
        this.mHandler.postDelayed(this.mOverscanTimeout, 200);
        HwPhoneWindowManager.super.finishedGoingToSleep(why);
    }

    public void startedWakingUp(int why) {
        HwTvPowerManagerPolicy hwTvPowerManagerPolicy = this.mTvPolicy;
        if (hwTvPowerManagerPolicy != null) {
            hwTvPowerManagerPolicy.onStartedWakingUp(why);
        }
        HwPhoneWindowManager.super.startedWakingUp(why);
    }

    public void startedGoingToSleepSync(int why) {
        if (HwPCUtils.isInWindowsCastMode()) {
            sHwWindowsCastManager.sendShowViewMsg(1);
        }
    }

    class OverscanTimeout implements Runnable {
        OverscanTimeout() {
        }

        @Override // java.lang.Runnable
        public void run() {
            Slog.i(HwPhoneWindowManager.TAG, "OverscanTimeout run");
            Settings.Global.putString(HwPhoneWindowManager.this.mContext.getContentResolver(), "single_hand_mode", "");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void powerPressBDReport(int eventId) {
        if (Log.HWINFO) {
            Flog.bdReport(eventId);
        }
    }

    private void interceptScreenRecorder() {
        if (isTvMode()) {
            Slog.d(TAG, "ScreenRecorder not support on TV," + this.mIsScreenRecorderChordEnabled);
        } else if (this.mIsScreenRecorderChordEnabled && this.mIsScreenRecorderVolumeUpKeyTriggered && this.mIsScreenRecorderPowerKeyTriggered && !this.mIsScreenRecorderVolumeDownKeyTriggered && !SystemProperties.getBoolean("sys.super_power_save", false) && !keyguardIsShowingTq() && checkPackageInstalled(HUAWEI_SCREENRECORDER_PACKAGE)) {
            HwCustPhoneWindowManager hwCustPhoneWindowManager = this.mCust;
            if (hwCustPhoneWindowManager != null && !hwCustPhoneWindowManager.isSosAllowed()) {
                return;
            }
            if (!IS_HWRIDEMODE_FEATURE_SUPPORTED || !SystemProperties.getBoolean("sys.ride_mode", false)) {
                long now = SystemClock.uptimeMillis();
                if (now <= this.mScreenRecorderVolumeUpKeyTime + 150 && now <= this.mScreenRecorderPowerKeyTime + 150) {
                    this.mIsScreenRecorderVolumeUpKeyConsumed = true;
                    cancelPendingPowerKeyActionForDistouch();
                    this.mHandler.postDelayed(this.mScreenRecorderRunnable, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                }
            }
        }
    }

    private void cancelPendingScreenRecorderAction() {
        this.mHandler.removeCallbacks(this.mScreenRecorderRunnable);
    }

    /* access modifiers changed from: package-private */
    public boolean isVoiceCall() {
        IAudioService audioService = getAudioService();
        if (audioService != null) {
            try {
                int mode = audioService.getMode();
                if (mode == 3 || mode == 2) {
                    return true;
                }
                return false;
            } catch (RemoteException e) {
                Log.w(TAG, "getMode exception");
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendKeyEvent(int keycode) {
        int[] actions;
        for (int i : new int[]{0, 1}) {
            long curTime = SystemClock.uptimeMillis();
            InputManager.getInstance().injectInputEvent(new KeyEvent(curTime, curTime, i, keycode, 0, 0, -1, 0, 8, LightsManagerEx.LIGHT_ID_SMARTBACKLIGHT), 0);
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            registerContentObserver(UserHandle.myUserId());
            boolean z = true;
            HwPhoneWindowManager.this.mIsDeviceProvisioned = Settings.Secure.getIntForUser(HwPhoneWindowManager.this.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0;
            HwPhoneWindowManager.this.mTrikeyNaviMode = Settings.System.getIntForUser(HwPhoneWindowManager.this.mResolver, HwPhoneWindowManager.FRONT_FINGERPRINT_SWAP_KEY_POSITION, HwPhoneWindowManager.this.TRIKEY_NAVI_DEFAULT_MODE, ActivityManager.getCurrentUser());
            HwPhoneWindowManager.this.mButtonLightMode = Settings.System.getIntForUser(HwPhoneWindowManager.this.mResolver, HwPhoneWindowManager.FRONT_FINGERPRINT_BUTTON_LIGHT_MODE, 1, ActivityManager.getCurrentUser());
            HwPhoneWindowManager.this.mIsHapticEnabled = Settings.System.getIntForUser(HwPhoneWindowManager.this.mResolver, HwPhoneWindowManager.HAPTIC_FEEDBACK_TRIKEY_SETTINGS, 1, ActivityManager.getCurrentUser()) != 0;
            HwPhoneWindowManager.this.mHiVoiceKeyType = Settings.Secure.getStringForUser(HwPhoneWindowManager.this.mResolver, HwPhoneWindowManager.KEY_HIVOICE_PRESS_TYPE, ActivityManager.getCurrentUser());
            HwPhoneWindowManager.this.mIsDoubleTapPayEnabled = Settings.Secure.getIntForUser(HwPhoneWindowManager.this.mResolver, HwPhoneWindowManager.KEY_DOUBLE_TAP_PAY, 0, ActivityManager.getCurrentUser()) != 1 ? false : z;
            HwPhoneWindowManager.this.mPowerLongPressTimeout = Settings.Secure.getIntForUser(HwPhoneWindowManager.this.mResolver, HwPhoneWindowManager.KEY_POWER_LONGPRESS_TIMEOUT, 700, ActivityManager.getCurrentUser());
            HwPhoneWindowManager.this.showPowerOffToastTimes = Settings.Secure.getIntForUser(HwPhoneWindowManager.this.mResolver, HwPhoneWindowManager.KEY_TOAST_POWER_OFF, 0, ActivityManager.getCurrentUser());
            HwPhoneWindowManager.this.mIsKidsMode = AppActConstant.VALUE_TRUE.equalsIgnoreCase(Settings.System.getStringForUser(HwPhoneWindowManager.this.mResolver, HwPhoneWindowManager.KIDSMODE, ActivityManager.getCurrentUser()));
        }

        public void registerContentObserver(int userId) {
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.System.getUriFor(HwPhoneWindowManager.FRONT_FINGERPRINT_SWAP_KEY_POSITION), false, this, userId);
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.System.getUriFor("device_provisioned"), false, this, userId);
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.System.getUriFor(HwPhoneWindowManager.FRONT_FINGERPRINT_BUTTON_LIGHT_MODE), false, this, userId);
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.System.getUriFor(HwPhoneWindowManager.HAPTIC_FEEDBACK_TRIKEY_SETTINGS), false, this, userId);
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(HwPhoneWindowManager.KEY_HIVOICE_PRESS_TYPE), false, this, userId);
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(HwPhoneWindowManager.KEY_DOUBLE_TAP_PAY), false, this, userId);
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(HwPhoneWindowManager.KEY_POWER_LONGPRESS_TIMEOUT), false, this, userId);
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.System.getUriFor(HwPhoneWindowManager.KIDSMODE), false, this, userId);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            HwPhoneWindowManager hwPhoneWindowManager = HwPhoneWindowManager.this;
            boolean z = true;
            hwPhoneWindowManager.mIsDeviceProvisioned = Settings.Secure.getIntForUser(hwPhoneWindowManager.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0;
            HwPhoneWindowManager hwPhoneWindowManager2 = HwPhoneWindowManager.this;
            hwPhoneWindowManager2.mTrikeyNaviMode = Settings.System.getIntForUser(hwPhoneWindowManager2.mResolver, HwPhoneWindowManager.FRONT_FINGERPRINT_SWAP_KEY_POSITION, HwPhoneWindowManager.this.TRIKEY_NAVI_DEFAULT_MODE, ActivityManager.getCurrentUser());
            HwPhoneWindowManager hwPhoneWindowManager3 = HwPhoneWindowManager.this;
            hwPhoneWindowManager3.mButtonLightMode = Settings.System.getIntForUser(hwPhoneWindowManager3.mResolver, HwPhoneWindowManager.FRONT_FINGERPRINT_BUTTON_LIGHT_MODE, 1, ActivityManager.getCurrentUser());
            HwPhoneWindowManager.this.resetButtonLightStatus();
            Slog.i(HwPhoneWindowManager.TAG, "mTrikeyNaviMode is:" + HwPhoneWindowManager.this.mTrikeyNaviMode + " mButtonLightMode is:" + HwPhoneWindowManager.this.mButtonLightMode);
            HwPhoneWindowManager hwPhoneWindowManager4 = HwPhoneWindowManager.this;
            hwPhoneWindowManager4.mIsHapticEnabled = Settings.System.getIntForUser(hwPhoneWindowManager4.mResolver, HwPhoneWindowManager.HAPTIC_FEEDBACK_TRIKEY_SETTINGS, 1, ActivityManager.getCurrentUser()) != 0;
            HwPhoneWindowManager hwPhoneWindowManager5 = HwPhoneWindowManager.this;
            hwPhoneWindowManager5.mHiVoiceKeyType = Settings.Secure.getStringForUser(hwPhoneWindowManager5.mResolver, HwPhoneWindowManager.KEY_HIVOICE_PRESS_TYPE, ActivityManager.getCurrentUser());
            HwPhoneWindowManager hwPhoneWindowManager6 = HwPhoneWindowManager.this;
            hwPhoneWindowManager6.mPowerLongPressTimeout = Settings.Secure.getIntForUser(hwPhoneWindowManager6.mResolver, HwPhoneWindowManager.KEY_POWER_LONGPRESS_TIMEOUT, 700, ActivityManager.getCurrentUser());
            HwPhoneWindowManager hwPhoneWindowManager7 = HwPhoneWindowManager.this;
            if (Settings.Secure.getIntForUser(hwPhoneWindowManager7.mResolver, HwPhoneWindowManager.KEY_DOUBLE_TAP_PAY, 0, ActivityManager.getCurrentUser()) != 1) {
                z = false;
            }
            hwPhoneWindowManager7.mIsDoubleTapPayEnabled = z;
            HwPhoneWindowManager hwPhoneWindowManager8 = HwPhoneWindowManager.this;
            hwPhoneWindowManager8.mIsKidsMode = AppActConstant.VALUE_TRUE.equalsIgnoreCase(Settings.System.getStringForUser(hwPhoneWindowManager8.mResolver, HwPhoneWindowManager.KIDSMODE, ActivityManager.getCurrentUser()));
            Slog.i(HwPhoneWindowManager.TAG, "onChange mHiVoiceKeyType is:" + HwPhoneWindowManager.this.mHiVoiceKeyType + " mDoubleTapPay " + HwPhoneWindowManager.this.mIsDoubleTapPayEnabled + " powerLongPressTimeout " + HwPhoneWindowManager.this.mPowerLongPressTimeout + " mIsKidsMode: " + HwPhoneWindowManager.this.mIsKidsMode);
        }
    }

    private boolean isExcluedScene() {
        ServiceManager.getService("activity");
        String pkgName = getTopActivity();
        boolean isSuperPowerMode = SystemProperties.getBoolean("sys.super_power_save", false);
        if (pkgName == null) {
            return false;
        }
        if (HwCustPkgNameConstant.HW_DESKLOCK_FULL_ACTIVITYNAME_BEFORE.equals(pkgName) || "com.huawei.deskclock/.alarmclock.LockAlarmFullActivity".equals(pkgName) || isSuperPowerMode || !this.mIsDeviceProvisioned || keyguardOn()) {
            return true;
        }
        return false;
    }

    private boolean isExcluedBackScene() {
        if (this.mTrikeyNaviMode == 1) {
            return isExcluedScene();
        }
        return !this.mIsDeviceProvisioned;
    }

    private boolean isExcluedRecentScene() {
        if (this.mTrikeyNaviMode == 1) {
            return !this.mIsDeviceProvisioned;
        }
        return isExcluedScene();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetButtonLightStatus() {
        if (this.mButtonLight == null) {
            return;
        }
        if (!this.mIsDeviceProvisioned) {
            setButtonLightTimeout(false);
            this.mButtonLight.setBrightness(0);
            return;
        }
        Slog.i(TAG, "resetButtonLightStatus");
        this.mHandlerEx.removeMessages(MSG_BUTTON_LIGHT_TIMEOUT);
        if (this.mTrikeyNaviMode < 0) {
            setButtonLightTimeout(false);
            this.mButtonLight.setBrightness(0);
        } else if (this.mButtonLightMode != 0) {
            setButtonLightTimeout(false);
            this.mButtonLight.setBrightness(this.mBackLight.getCurrentBrightness());
        } else if (this.mButtonLight.getCurrentBrightness() > 0) {
            setButtonLightTimeout(false);
            Message msg = this.mHandlerEx.obtainMessage(MSG_BUTTON_LIGHT_TIMEOUT);
            msg.setAsynchronous(true);
            this.mHandlerEx.sendMessageDelayed(msg, 5000);
        } else {
            setButtonLightTimeout(true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setButtonLightTimeout(boolean timeout) {
        SystemProperties.set("sys.button.light.timeout", String.valueOf(timeout));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendLightTimeoutMsg() {
        if (this.mButtonLight != null && this.mIsDeviceProvisioned) {
            this.mHandlerEx.removeMessages(MSG_BUTTON_LIGHT_TIMEOUT);
            if (this.mTrikeyNaviMode >= 0) {
                int curButtonBrightness = this.mButtonLight.getCurrentBrightness();
                int curBackBrightness = this.mBackLight.getCurrentBrightness();
                if (this.mButtonLightMode == 0) {
                    if (SystemProperties.getBoolean("sys.button.light.timeout", false) && curButtonBrightness == 0) {
                        this.mButtonLight.setBrightness(curBackBrightness);
                    }
                    setButtonLightTimeout(false);
                    Message msg = this.mHandlerEx.obtainMessage(MSG_BUTTON_LIGHT_TIMEOUT);
                    msg.setAsynchronous(true);
                    this.mHandlerEx.sendMessageDelayed(msg, 5000);
                } else if (curButtonBrightness == 0) {
                    this.mButtonLight.setBrightness(curBackBrightness);
                }
            } else {
                setButtonLightTimeout(false);
                this.mButtonLight.setBrightness(0);
            }
        }
    }

    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private ScreenBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(intent.getAction())) {
                HwPhoneWindowManager.this.sendLightTimeoutMsg();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startHwVibrate(int vibrateMode) {
        if (!isKeyguardLocked() && this.mIsHapticEnabled && !AppActConstant.VALUE_TRUE.equals(SystemProperties.get("runtime.mmitest.isrunning", AppActConstant.VALUE_FALSE)) && this.mVibrator != null) {
            Log.d(TAG, "startVibrateWithConfigProp:" + vibrateMode);
            this.mVibrator.vibrate((long) vibrateMode);
        }
    }

    private boolean isMMITesting() {
        return AppActConstant.VALUE_TRUE.equals(SystemProperties.get("runtime.mmitest.isrunning", AppActConstant.VALUE_FALSE));
    }

    private boolean isNeedNotifyWallet() {
        if (!IS_SUPPORT_DOUBLETAP_PAY) {
            return false;
        }
        if (!this.mIsDoubleTapPayEnabled) {
            if (HWFLOW) {
                Log.d(TAG, "DoubleTapPay switch is not open");
            }
            return false;
        } else if (mIsNeedMuteByPowerKeyDown && isNeedMuteFirstPowerKeyDown()) {
            return false;
        } else {
            if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
                if (HWFLOW) {
                    Log.i(TAG, "PC mode");
                }
                return false;
            } else if (!mIsSidePowerFpComb || !this.mHwPWMEx.isPowerFpForbidGotoSleep() || isKeyguardShowingOrOccluded()) {
                return true;
            } else {
                if (HWFLOW) {
                    Log.d(TAG, "wallet Is Side powerFp comb");
                }
                return false;
            }
        }
    }

    private boolean isNeedMuteFirstPowerKeyDown() {
        WindowManagerPolicy.WindowState focusedWindow = this.mDefaultDisplayPolicy.getFocusedWindow();
        if (isIncallui(focusedWindow)) {
            return true;
        }
        if (focusedWindow == null || focusedWindow.getAttrs() == null || !String.valueOf(focusedWindow.getAttrs().getTitle()).contains(PROXIMITY_UI_WINDOW_TITLE)) {
            return false;
        }
        return isIncallui(this.mDefaultDisplayPolicy.getLastFocusedWindow());
    }

    private boolean isIncallui(WindowManagerPolicy.WindowState focusedWindow) {
        if (focusedWindow == null || focusedWindow.getAttrs() == null) {
            return false;
        }
        return String.valueOf(focusedWindow.getAttrs().getTitle()).contains(HUAWEI_ANDROID_INCALL_UI);
    }

    private void wakeupSOSPage(int keycode) {
        if (isTvMode()) {
            Slog.d(TAG, "SOS not support on TV");
        } else if (keycode == 25 || keycode == 24) {
            this.mLastVolumeKeyDownTime = SystemClock.uptimeMillis();
        } else if (keycode == 26) {
            long now = SystemClock.uptimeMillis();
            if (this.mPowerKeyCount <= 0 || (now - this.mLastPowerKeyDownTime <= 500 && now - this.mLastVolumeKeyDownTime >= POWER_SOS_MISTOUCH_THRESHOLD)) {
                if (now - this.mLastNotifyWalletTime < 500) {
                    this.mHwPWMEx.cancelWalletSwipe(this.mHandler);
                }
                this.mPowerKeyCount++;
                this.mLastPowerKeyDownTime = now;
                if (this.mPowerKeyCount == MAX_POWERKEY_COUNTDOWN) {
                    resetSOS();
                    this.mIsFreezePowerkey = false;
                    PowerManager powerManager = (PowerManager) this.mContext.getSystemService(HIVOICE_PRESS_TYPE_POWER);
                    powerPressBDReport(991310986);
                    try {
                        String pkgName = getTopActivity();
                        Log.d(TAG, "get Emergency power TopActivity is:" + pkgName);
                        if (!"com.android.emergency/.view.ViewCountDownActivity".equals(pkgName) && !"com.android.emergency/.view.EmergencyNumberActivity".equals(pkgName)) {
                            if (!ACTIVITY_NAME_EMERGENCY_SIMPLIFIEDINFO.equals(pkgName)) {
                                Intent intent = createSosIntent();
                                if (isSoSEmergencyInstalled(intent)) {
                                    this.mIsFreezePowerkey = true;
                                    this.mHandlerEx.removeMessages(MSG_FREEZE_POWER_KEY);
                                    Log.d(TAG, "Emergency power start activity");
                                    this.mContext.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
                                }
                                if (this.mIsFreezePowerkey) {
                                    powerManager.wakeUp(SystemClock.uptimeMillis());
                                    this.mHandlerEx.removeMessages(MSG_FREEZE_POWER_KEY);
                                    Message msg = this.mHandlerEx.obtainMessage(MSG_FREEZE_POWER_KEY);
                                    msg.setAsynchronous(true);
                                    this.mHandlerEx.sendMessageDelayed(msg, LAUNCH_VASSIT_TIMEOUT);
                                    return;
                                }
                                return;
                            }
                        }
                        Log.d(TAG, "current topActivity is emergency, return ");
                        if (powerManager != null && !powerManager.isScreenOn()) {
                            powerManager.wakeUp(SystemClock.uptimeMillis());
                        }
                    } catch (ActivityNotFoundException ex) {
                        Log.e(TAG, "ActivityNotFoundException failed message : " + ex);
                        this.mIsFreezePowerkey = false;
                    } catch (Exception e) {
                        Log.e(TAG, "StartActivity Exception");
                        this.mIsFreezePowerkey = false;
                    }
                }
            } else {
                this.mPowerKeyCount = 1;
                this.mLastPowerKeyDownTime = now;
            }
        }
    }

    private void resetSOS() {
        this.mLastPowerKeyDownTime = 0;
        this.mPowerKeyCount = 0;
    }

    private boolean isSoSEmergencyInstalled(Intent intent) {
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager == null || intent.resolveActivity(packageManager) == null) {
            return false;
        }
        return true;
    }

    private void lockOnPadAssistMode(int keyCode) {
        if (keyCode == 26 && HwPCUtils.isPadAssistantMode()) {
            boolean isScreenPowerOn = false;
            try {
                IHwPCManager pcMgr = HwPCUtils.getHwPCManager();
                if (pcMgr != null && !pcMgr.isScreenPowerOn()) {
                    isScreenPowerOn = true;
                }
            } catch (RemoteException e) {
                isScreenPowerOn = false;
            }
            if (isScreenPowerOn) {
                if (HwPCUtils.isInWindowsCastMode() && !HwKeyguardManagerImpl.getDefault().isLockScreenDisabled(this.mContext)) {
                    sHwWindowsCastManager.sendShowViewMsg(1);
                }
                lockNow(null);
            }
        }
    }

    private void interceptDiagnosisDetector(int keyCode, boolean isDown) {
        if (this.mDetector != null && isKeyguardLocked()) {
            this.mDetector.updateState(keyCode, isDown);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:132:0x020d, code lost:
        if (r13 == 24) goto L_0x0212;
     */
    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        int result;
        char c;
        KeyEvent keyEvent;
        int policyFlags2;
        String str;
        boolean down;
        int deviceID;
        boolean isInjected;
        int keyCode;
        boolean isScreenOn;
        String str2;
        HwCustPhoneWindowManager hwCustPhoneWindowManager;
        HwCustPhoneWindowManager hwCustPhoneWindowManager2;
        boolean isVolumeDownDoubleClick;
        boolean isIntercept;
        boolean isVolumeDownDoubleClick2;
        HwCustPhoneWindowManager hwCustPhoneWindowManager3;
        boolean isVolumeDownDoubleClick3;
        boolean isCameraOpened;
        HwTvPowerManagerPolicy hwTvPowerManagerPolicy;
        AudioManager audioManager;
        boolean down2 = event.getAction() == 0;
        int keyCode2 = event.getKeyCode();
        int deviceID2 = event.getDeviceId();
        boolean isScreenOn2 = (policyFlags & 536870912) != 0;
        interceptDiagnosisDetector(keyCode2, down2);
        if (isTvMode() && isInterceptKeyBeforeQueueingTv(event, isScreenOn2)) {
            return 0;
        }
        int flags = event.getFlags();
        if (IS_TV && ((keyCode2 == 24 || keyCode2 == 25 || keyCode2 == 4 || keyCode2 == 164 || keyCode2 == 26 || keyCode2 == 3 || ((keyCode2 == 82 && (268435456 & flags) == 0) || keyCode2 == 182 || keyCode2 == 737 || keyCode2 == 738)) && down2 && (audioManager = this.mAudioManager) != null)) {
            audioManager.playSoundEffect(0);
        }
        if (supportActivityForbidSpecialKey(keyCode2)) {
            Log.d(TAG, "isScreenOn = " + isScreenOn2 + ", has intercept Key for block " + keyCode2 + ", some ssssuper activity is on top now.");
            return 0;
        }
        if (down2) {
            if (keyCode2 == 4 && this.mAppEyeBackKey != null) {
                ZrHungData arg = new ZrHungData();
                arg.putLong("downTime", event.getDownTime());
                this.mAppEyeBackKey.check(arg);
            }
            if (keyCode2 == 3 && this.mAppEyeHomeKey != null) {
                ZrHungData arg2 = new ZrHungData();
                arg2.putLong("downTime", event.getDownTime());
                this.mAppEyeHomeKey.check(arg2);
            }
        }
        if (handleInputEventInPCCastMode(event)) {
            return 0;
        }
        HwCustPhoneWindowManager hwCustPhoneWindowManager4 = this.mCust;
        if (hwCustPhoneWindowManager4 != null) {
            hwCustPhoneWindowManager4.processCustInterceptKey(keyCode2, down2, this.mContext);
        }
        Flog.i((int) WifiProCommonUtils.RESP_CODE_INVALID_URL, "HwPhoneWindowManager has intercept Key : " + keyCode2 + ", isdown : " + down2 + ", flags : " + flags);
        lockOnPadAssistMode(keyCode2);
        boolean initialDown = down2 && event.getRepeatCount() == 0;
        if (initialDown && (hwTvPowerManagerPolicy = this.mTvPolicy) != null) {
            hwTvPowerManagerPolicy.onKeyOperation();
        }
        reportMonitorData(keyCode2, down2);
        HwFreeFormManager.getInstance(this.mContext).removeFloatListView();
        boolean isInjected2 = (policyFlags & 16777216) != 0;
        wakeupNoteEditor(event, isInjected2);
        if (keyCode2 == 26 && this.mIsFreezePowerkey) {
            return 1;
        }
        if ((keyCode2 == 26 || keyCode2 == 6 || keyCode2 == 187 || keyCode2 == 715) && this.mDefaultDisplayPolicy.getFocusedWindow() != null && (this.mDefaultDisplayPolicy.getFocusedWindow().getAttrs().hwFlags & FLOATING_MASK) == FLOATING_MASK) {
            Log.i(TAG, "power and endcall key received and passsing to user.");
            return 1;
        }
        if (IS_HW_EASY_WAKE_UP && this.mSystemReady) {
            KeyguardServiceDelegateEx keyguardDelegateEx = new KeyguardServiceDelegateEx();
            keyguardDelegateEx.setKeyguardServiceDelegate(this.mKeyguardDelegate);
            if (HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getEasyWakeUpManager(this.mContext, this.mHandler, keyguardDelegateEx).handleWakeUpKey(event, isScreenOn2 ? -1 : this.mScreenOffReason)) {
                Log.i(TAG, "EasyWakeUpManager has handled the keycode : " + event.getKeyCode());
                return 0;
            }
        }
        if (initialDown) {
            if (((keyCode2 == 82 && (268435456 & flags) == 0) || keyCode2 == 3 || keyCode2 == 4) && SystemProperties.get(VIBRATE_ON_TOUCH, AppActConstant.VALUE_FALSE).equals(AppActConstant.VALUE_TRUE) && (policyFlags & 2) != 0) {
                performHapticFeedbackLw(null, 1, false, "Huawei  --- TBD");
            }
        }
        boolean isWakeKey = isWakeKeyFun(keyCode2) | ((policyFlags & 1) != 0);
        if ((!isScreenOn2 || this.mIsHeadless) && (!isInjected2 || isWakeKey)) {
            if (down2 && isWakeKey) {
                isWakeKeyWhenScreenOff(keyCode2);
            }
            result = 0;
        } else {
            result = 1;
        }
        notifyStatusBarManager(event);
        int i = 25;
        if (keyCode2 != 25) {
            c = 24;
        } else {
            c = 24;
        }
        if (this.mDefaultDisplayPolicy.getFocusedWindow() != null && (this.mDefaultDisplayPolicy.getFocusedWindow().getAttrs().hwFlags & 8) == 8 && AppActConstant.VALUE_TRUE.equals(SystemProperties.get("runtime.mmitest.isrunning", AppActConstant.VALUE_FALSE))) {
            Log.i(TAG, "Prevent hard key volume event to mmi test before queueing.");
            return result & -2;
        }
        if (lightScreenOnPcMode(keyCode2)) {
            return 0;
        }
        if (keyCode2 == 3 || keyCode2 == 4) {
            str = TAG;
            isInjected = isInjected2;
            isScreenOn = isScreenOn2;
            deviceID = deviceID2;
            keyCode = keyCode2;
            down = down2;
            keyEvent = event;
        } else {
            if (keyCode2 != 82) {
                if (keyCode2 != 164) {
                    if (keyCode2 != 182) {
                        if (keyCode2 == 187) {
                            str = TAG;
                            isInjected = isInjected2;
                            isScreenOn = isScreenOn2;
                            deviceID = deviceID2;
                            keyCode = keyCode2;
                            down = down2;
                            keyEvent = event;
                        } else if (keyCode2 != 231) {
                            if (keyCode2 != 265) {
                                if (keyCode2 == 702) {
                                    handleGameEvent(event);
                                    keyEvent = event;
                                } else if (keyCode2 == 715) {
                                    handleGameSpace(event);
                                    keyEvent = event;
                                } else if (keyCode2 == 708 || keyCode2 == 709) {
                                    setTpKeep(keyCode2, down2);
                                    return result & -2;
                                } else if (keyCode2 == 717) {
                                    WindowManagerPolicy.WindowState focusedWindow = this.mDefaultDisplayPolicy.getFocusedWindow();
                                    if (focusedWindow == null || focusedWindow.getAttrs() == null) {
                                        isCameraOpened = false;
                                    } else {
                                        isCameraOpened = focusedWindow.getAttrs().packageName.contains(PKG_CAMERA);
                                    }
                                    Log.d(TAG, "Camera keycode: " + keyCode2 + " isDwon:" + down2 + " isCamera: " + isCameraOpened);
                                    if (!initialDown || isCameraOpened) {
                                        keyEvent = event;
                                    } else {
                                        Log.d(TAG, "Open Camara keycode: " + keyCode2 + " is comsuming.");
                                        Intent intent = new Intent(HUAWEI_RAPIDCAPTURE_START_MODE);
                                        intent.setPackage(PKG_CAMERA);
                                        intent.putExtra("command", "launchHwCamera");
                                        try {
                                            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
                                        } catch (Exception e) {
                                            Slog.e(TAG, "unable to start service:" + intent, e);
                                        }
                                        keyEvent = event;
                                    }
                                } else if (keyCode2 != 718) {
                                    switch (keyCode2) {
                                        case 24:
                                        case 25:
                                            break;
                                        case 26:
                                            if (!HwFrameworkFactory.getVRSystemServiceManager().isVRDeviceConnected()) {
                                                updatePowerKeyPolicy(down2, isScreenOn2, event);
                                                notifyPowerKeyEventToHiAction(this.mContext, event);
                                                if (!down2) {
                                                    powerPressBDReport(991310981);
                                                    this.mIsPowerKeyDisTouch = false;
                                                    this.mIsScreenRecorderPowerKeyTriggered = false;
                                                    cancelPendingScreenRecorderAction();
                                                    cancelPowerOffToast();
                                                    keyEvent = event;
                                                    break;
                                                } else {
                                                    setIsNeedNotifyWallet(isNeedNotifyWallet());
                                                    powerPressBDReport(991310980);
                                                    if (this.mPowerManager != null && isScreenOn2) {
                                                        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
                                                    }
                                                    this.mPowerOffToastShown = false;
                                                    showPowerOffToast(isScreenOn2);
                                                    if (mIsNeedMuteByPowerKeyDown && isScreenOn2 && isNeedMuteFirstPowerKeyDown()) {
                                                        mIsNeedMuteByPowerKeyDown = false;
                                                        mNeedHushByPowerKeyDown = true;
                                                    }
                                                    this.mIsLastMuteRinger = mNeedHushByPowerKeyDown;
                                                    DefaultHwScreenOnProximityLock defaultHwScreenOnProximityLock = this.mHwScreenOnProximityLock;
                                                    if (defaultHwScreenOnProximityLock != null && defaultHwScreenOnProximityLock.isShowing() && isScreenOn2 && !this.mIsPowerKeyDisTouch && (event.getFlags() & 1024) == 0) {
                                                        this.mIsPowerKeyDisTouch = true;
                                                        this.mPowerKeyDisTouchTime = event.getDownTime();
                                                    }
                                                    if (isScreenOn2 && !this.mIsScreenRecorderPowerKeyTriggered && (event.getFlags() & 1024) == 0) {
                                                        this.mIsScreenRecorderPowerKeyTriggered = true;
                                                        this.mScreenRecorderPowerKeyTime = event.getDownTime();
                                                        interceptScreenRecorder();
                                                    }
                                                    if (!isTvMode()) {
                                                        keyEvent = event;
                                                        break;
                                                    } else {
                                                        interceptPowerKeyDownTv(event, isScreenOn2);
                                                        keyEvent = event;
                                                        break;
                                                    }
                                                }
                                            } else {
                                                return 1;
                                            }
                                        default:
                                            switch (keyCode2) {
                                                case 401:
                                                case 402:
                                                case 403:
                                                case 404:
                                                case HwFpServiceToHalUtils.MSG_UPDATE_SCREEN_FILM_STATE /* 405 */:
                                                    processing_KEYCODE_SOUNDTRIGGER_EVENT(keyCode2, this.mContext, isMusicActive() || isFMActive(), down2, keyguardIsShowingTq());
                                                    keyEvent = event;
                                                    break;
                                                default:
                                                    switch (keyCode2) {
                                                        case 723:
                                                            if (down2) {
                                                                interceptBlePowerKeyDown(event, isScreenOn2);
                                                            } else {
                                                                interceptBlePowerKeyUp(event, isScreenOn2, event.isCanceled());
                                                            }
                                                            return result & -2;
                                                        case 724:
                                                            if (isFocusApp(PKG_CAMERA)) {
                                                                keyEvent = event;
                                                                break;
                                                            } else {
                                                                launchUnderWaterCamera();
                                                                Log.i(TAG, "camera not front, not pass ble double click keyevent");
                                                                return result & -2;
                                                            }
                                                        case 725:
                                                            if (isFocusApp(PKG_CAMERA)) {
                                                                keyEvent = event;
                                                                break;
                                                            } else {
                                                                Log.i(TAG, "camera not front, not pass ble triple click keyevent");
                                                                return result & -2;
                                                            }
                                                        default:
                                                            switch (keyCode2) {
                                                                case 737:
                                                                case 738:
                                                                case 739:
                                                                case 740:
                                                                    break;
                                                                default:
                                                                    keyEvent = event;
                                                                    break;
                                                            }
                                                    }
                                            }
                                    }
                                } else {
                                    StylusGestureListener stylusGestureListener = this.mStylusGestureListener;
                                    if (stylusGestureListener != null) {
                                        stylusGestureListener.onKeyEvent(keyCode2, down2);
                                    }
                                    policyFlags2 = policyFlags & -2;
                                    keyEvent = event;
                                    return HwPhoneWindowManager.super.interceptKeyBeforeQueueing(keyEvent, policyFlags2);
                                }
                            } else if (handleStem1Event(down2, isScreenOn2, event)) {
                                return result & -2;
                            } else {
                                keyEvent = event;
                            }
                        } else if (!isTvMode()) {
                            keyEvent = event;
                        } else if (isDisabledVoiceAssistButton(down2)) {
                            return 0;
                        } else {
                            if (initialDown || !down2) {
                                Message message = this.mHandlerEx.obtainMessage(110, Boolean.valueOf(down2));
                                message.setAsynchronous(true);
                                message.sendToTarget();
                            }
                            return result & -2;
                        }
                    }
                    if (!isTvMode() || !isScreenOn2) {
                        keyEvent = event;
                    } else {
                        if (initialDown && down2) {
                            Message message2 = this.mHandlerEx.obtainMessage(113, Integer.valueOf(keyCode2));
                            message2.setAsynchronous(true);
                            message2.sendToTarget();
                        }
                        return result & -2;
                    }
                }
                if (HwDeviceManager.disallowOp(38)) {
                    if (down2) {
                        this.mHandler.post(new Runnable() {
                            /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass13 */

                            @Override // java.lang.Runnable
                            public void run() {
                                Toast toast = Toast.makeText(HwPhoneWindowManager.this.mContext, 33685973, 0);
                                toast.getWindowParams().type = HwArbitrationDEFS.MSG_MPLINK_UNBIND_FAIL;
                                toast.getWindowParams().privateFlags |= 16;
                                toast.show();
                            }
                        });
                    }
                    return result & -2;
                }
                HwDisplaySidePolicy hwDisplaySidePolicy = this.mDisplaySidePolicy;
                if (hwDisplaySidePolicy != null) {
                    str2 = TAG;
                    i = 25;
                    if (hwDisplaySidePolicy.interceptVolumeKey(event, isInjected2, isScreenOn2, keyCode2, down2)) {
                        return result & -2;
                    }
                } else {
                    str2 = TAG;
                }
                if (keyCode2 == i) {
                    if (down2) {
                        DefaultHwScreenOnProximityLock defaultHwScreenOnProximityLock2 = this.mHwScreenOnProximityLock;
                        if (defaultHwScreenOnProximityLock2 != null && defaultHwScreenOnProximityLock2.isShowing() && isScreenOn2 && !this.mIsVolumeDownKeyDisTouch && (event.getFlags() & 1024) == 0) {
                            Log.d(str2, "keycode: KEYCODE_VOLUME_DOWN is comsumed by disable touch mode.");
                            this.mIsVolumeDownKeyDisTouch = true;
                            handleHintShown(this.mIsHintShown);
                            keyEvent = event;
                        } else if (isScreenOn2 && !this.mIsScreenRecorderVolumeDownKeyTriggered && (event.getFlags() & 1024) == 0) {
                            cancelPendingPowerKeyActionForDistouch();
                            this.mIsScreenRecorderVolumeDownKeyTriggered = true;
                            cancelPendingScreenRecorderAction();
                        }
                    } else {
                        this.mIsVolumeDownKeyDisTouch = false;
                        this.mIsScreenRecorderVolumeDownKeyTriggered = false;
                        cancelPendingScreenRecorderAction();
                    }
                    Log.i(str2, "interceptVolumeDownKey down=" + down2 + " policyFlags=" + Integer.toHexString(policyFlags));
                    if ((isScreenOn2 && !keyguardIsShowingTq()) || isInjected2) {
                        keyEvent = event;
                    } else if ((event.getFlags() & 1024) != 0) {
                        keyEvent = event;
                    } else if (!isDeviceProvisioned()) {
                        Log.i(str2, "Device is not Provisioned");
                        keyEvent = event;
                    } else if (down2) {
                        boolean isPhoneActive = isMusicActive() || isPhoneInCall() || isVoiceCall() || isFMActive();
                        if (this.mIsVoiceRecognitionActive) {
                            isIntercept = false;
                            isVolumeDownDoubleClick = false;
                            long interval = event.getEventTime() - this.mLastStartVassistantServiceTime;
                            if (interval > 15000) {
                                this.mIsVoiceRecognitionActive = false;
                            } else if (interval > 1500) {
                                this.mIsVoiceRecognitionActive = AudioSystem.isSourceActive(6);
                            }
                        } else {
                            isIntercept = false;
                            isVolumeDownDoubleClick = false;
                        }
                        if (isSideTouchVolumeKey(event, isInjected2) || !isDoubleClickEnabled()) {
                            isVolumeDownDoubleClick2 = isVolumeDownDoubleClick;
                        } else {
                            long timediff = event.getEventTime() - this.mLastVolumeDownKeyDownTime;
                            this.mLastVolumeDownKeyDownTime = event.getEventTime();
                            if (timediff < VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT) {
                                if (this.mListener == null) {
                                    this.mListener = new ProximitySensorListener();
                                }
                                turnOnSensorListener();
                                if (!this.mIsProximity || !this.mIsSensorRegisted) {
                                    StringBuilder sb = new StringBuilder();
                                    isVolumeDownDoubleClick3 = true;
                                    sb.append("mIsProximity ");
                                    sb.append(this.mIsProximity);
                                    sb.append(", mIsSensorRegisted ");
                                    sb.append(this.mIsSensorRegisted);
                                    Log.i(str2, sb.toString());
                                    notifyRapidCaptureService("start");
                                } else {
                                    isVolumeDownDoubleClick3 = true;
                                }
                                turnOffSensorListener();
                                result &= -2;
                                isVolumeDownDoubleClick2 = isVolumeDownDoubleClick3;
                            } else {
                                notifyRapidCaptureService("wakeup");
                                if (this.mListener == null) {
                                    this.mListener = new ProximitySensorListener();
                                }
                                turnOnSensorListener();
                                isVolumeDownDoubleClick2 = isVolumeDownDoubleClick;
                            }
                            if (!isScreenOn2 || isVolumeDownDoubleClick2) {
                                isIntercept = true;
                            }
                        }
                        if (!isTvMode() && !isPhoneActive && !isScreenOn2 && !isVolumeDownDoubleClick2 && isVassistantInstall()) {
                            notifyVassistantService("wakeup", 2, event);
                            if (this.mListener == null) {
                                this.mListener = new ProximitySensorListener();
                            }
                            turnOnSensorListener();
                            interceptQuickCallChord();
                            isIntercept = true;
                        }
                        Log.i(str2, "intercept volume down key, isIntercept=" + isIntercept + " now=" + SystemClock.uptimeMillis() + " EventTime=" + event.getEventTime());
                        if (isInterceptAndCheckRinging(isIntercept)) {
                            return result;
                        }
                        if ((result & 1) == 0 && getTelecommService().isInCall() && (hwCustPhoneWindowManager3 = this.mCust) != null && hwCustPhoneWindowManager3.isVolumnkeyWakeup(this.mContext)) {
                            this.mCust.volumnkeyWakeup(this.mContext, isScreenOn2, this.mPowerManager);
                        }
                        policyFlags2 = policyFlags;
                        keyEvent = event;
                        return HwPhoneWindowManager.super.interceptKeyBeforeQueueing(keyEvent, policyFlags2);
                    } else if (event.getEventTime() - event.getDownTime() >= 500) {
                        resetVolumeDownKeyLongPressed();
                        keyEvent = event;
                    } else {
                        cancelPendingQuickCallChordAction();
                        keyEvent = event;
                    }
                } else if (keyCode2 == 24) {
                    if (down2) {
                        DefaultHwScreenOnProximityLock defaultHwScreenOnProximityLock3 = this.mHwScreenOnProximityLock;
                        if (defaultHwScreenOnProximityLock3 == null || !defaultHwScreenOnProximityLock3.isShowing() || !isScreenOn2 || this.mIsVolumeUpKeyDisTouch || (event.getFlags() & 1024) != 0) {
                            if (isScreenOn2 && !this.mIsScreenRecorderVolumeUpKeyTriggered && (event.getFlags() & 1024) == 0) {
                                cancelPendingPowerKeyActionForDistouch();
                                this.mIsScreenRecorderVolumeUpKeyTriggered = true;
                                this.mScreenRecorderVolumeUpKeyTime = event.getDownTime();
                                this.mIsScreenRecorderVolumeUpKeyConsumed = false;
                                interceptScreenRecorder();
                            }
                            if ((result & 1) == 0 && getTelecommService().isInCall() && (hwCustPhoneWindowManager2 = this.mCust) != null && hwCustPhoneWindowManager2.isVolumnkeyWakeup(this.mContext)) {
                                this.mCust.volumnkeyWakeup(this.mContext, isScreenOn2, this.mPowerManager);
                            }
                        } else {
                            Log.d(str2, "keycode: KEYCODE_VOLUME_UP is comsumed by disable touch mode.");
                            this.mIsVolumeUpKeyDisTouch = true;
                            this.mVolumeUpKeyDisTouchTime = event.getDownTime();
                            this.mIsVolumeUpKeyConsumedByDisTouch = false;
                            handleHintShown(this.mIsHintShown);
                            cancelPendingPowerKeyActionForDistouch();
                            keyEvent = event;
                        }
                    } else {
                        this.mIsVolumeUpKeyDisTouch = false;
                        this.mIsScreenRecorderVolumeUpKeyTriggered = false;
                        cancelPendingScreenRecorderAction();
                    }
                    if (!IS_QUICK_RECORD_SUPPORTED || (hwCustPhoneWindowManager = this.mCust) == null) {
                        keyEvent = event;
                    } else {
                        keyEvent = event;
                        if (hwCustPhoneWindowManager.interceptVolumeUpKey(event, this.mContext, isScreenOn2, keyguardIsShowingTq(), isMusicActive() || isVoiceCall() || isFMActive(), isInjected2, down2)) {
                            return result;
                        }
                    }
                } else {
                    keyEvent = event;
                }
            } else {
                keyEvent = event;
                if (!this.mDefaultDisplayPolicy.hasNavigationBar() && down2) {
                    if (isScreenInLockTaskMode()) {
                        this.mIsMenuKeyPress = true;
                        this.mMenuKeyPressTime = event.getDownTime();
                        interceptBackandMenuKey();
                    } else {
                        this.mIsMenuKeyPress = false;
                        this.mMenuKeyPressTime = 0;
                    }
                }
                if (isScreenOn2 && isTvMode() && interceptMenuKeyForTv(event)) {
                    return result & -2;
                }
            }
            policyFlags2 = policyFlags;
            return HwPhoneWindowManager.super.interceptKeyBeforeQueueing(keyEvent, policyFlags2);
        }
        if (down) {
            DefaultHwFalseTouchMonitor defaultHwFalseTouchMonitor = this.mFalseTouchMonitor;
            if (defaultHwFalseTouchMonitor != null) {
                defaultHwFalseTouchMonitor.handleKeyEvent(keyEvent);
            }
            DefaultHwScreenOnProximityLock defaultHwScreenOnProximityLock4 = this.mHwScreenOnProximityLock;
            if (defaultHwScreenOnProximityLock4 != null && defaultHwScreenOnProximityLock4.isShowing() && isScreenOn && !this.mIsHintShown && (event.getFlags() & 1024) == 0) {
                Log.d(str, "keycode: " + keyCode + " is comsumed by disable touch mode.");
                this.mHwScreenOnProximityLock.forceShowHint();
                this.mIsHintShown = true;
                policyFlags2 = policyFlags;
                return HwPhoneWindowManager.super.interceptKeyBeforeQueueing(keyEvent, policyFlags2);
            }
        }
        boolean isFrontFpNavi = FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION;
        boolean isSupportTrikey = FrontFingerPrintSettings.isSupportTrikey();
        boolean isMMITest = isMMITesting();
        Flog.i((int) WifiProCommonUtils.RESP_CODE_INVALID_URL, "HwPhoneWindowManagerinterceptKeyBeforeQueueing deviceID:" + deviceID + " isFrontFpNavi:" + isFrontFpNavi + " isSupportTrikey:" + isSupportTrikey + " isMMITest:" + isMMITest);
        if (deviceID > 0 && isFrontFpNavi && isSupportTrikey && !isMMITest && keyCode == 4) {
            if (isTrikeyNaviKeycodeFromLON(isInjected, isExcluedBackScene())) {
                return 0;
            }
            sendLightTimeoutMsg();
            if (down) {
                this.mBackTrikeyHandled = false;
                Message msg = this.mHandlerEx.obtainMessage(MSG_TRIKEY_BACK_LONG_PRESS);
                msg.setAsynchronous(true);
                this.mHandlerEx.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                if (this.mTrikeyNaviMode == 1) {
                    return 0;
                }
            } else {
                boolean handled = this.mBackTrikeyHandled;
                if (!this.mBackTrikeyHandled) {
                    this.mBackTrikeyHandled = true;
                    this.mHandlerEx.removeMessages(MSG_TRIKEY_BACK_LONG_PRESS);
                }
                if (handled) {
                    return 0;
                }
                startHwVibrate(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP);
                if (this.mTrikeyNaviMode == 1) {
                    Flog.bdReport(991310016);
                    sendKeyEvent(187);
                    return 0;
                }
            }
        }
        if (!this.mDefaultDisplayPolicy.hasNavigationBar() && keyCode == 4 && down) {
            if (isScreenInLockTaskMode()) {
                this.mIsBackKeyPress = true;
                this.mBackKeyPressTime = event.getDownTime();
                interceptBackandMenuKey();
            } else {
                this.mIsBackKeyPress = false;
                this.mBackKeyPressTime = 0;
            }
        }
        policyFlags2 = policyFlags;
        return HwPhoneWindowManager.super.interceptKeyBeforeQueueing(keyEvent, policyFlags2);
    }

    private void handleHintShown(boolean isHintShown) {
        if (!isHintShown) {
            this.mHwScreenOnProximityLock.forceShowHint();
            this.mIsHintShown = true;
        }
    }

    private boolean isDisabledVoiceAssistButton(boolean down) {
        if (!HwDeviceManager.disallowOp(72)) {
            return false;
        }
        if (down) {
            return true;
        }
        this.mHandler.post(new Runnable() {
            /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass14 */

            @Override // java.lang.Runnable
            public void run() {
                Toast toast = Toast.makeText(HwPhoneWindowManager.this.mContext, 33685904, 0);
                toast.getWindowParams().type = HwArbitrationDEFS.MSG_MPLINK_UNBIND_FAIL;
                toast.getWindowParams().privateFlags |= 16;
                toast.show();
            }
        });
        return true;
    }

    private boolean isDoubleClickEnabled() {
        if (isTvMode()) {
            return false;
        }
        boolean isMusicOrFMOrVoiceCallActive = isMusicActive() || isVoiceCall() || isFMActive();
        HwDisplaySidePolicy hwDisplaySidePolicy = this.mDisplaySidePolicy;
        boolean isSideScreenAndTalkbackEnable = hwDisplaySidePolicy != null && hwDisplaySidePolicy.isTalkbackEnable();
        Log.i(TAG, "isMusicOrFMOrVoiceCallActive=" + isMusicOrFMOrVoiceCallActive + " mIsVoiceRecognitionActive=" + this.mIsVoiceRecognitionActive + " isSideScreenAndTalkbackEnable=" + isSideScreenAndTalkbackEnable);
        if (isMusicOrFMOrVoiceCallActive || this.mIsVoiceRecognitionActive || SystemProperties.getBoolean("sys.super_power_save", false) || isSideScreenAndTalkbackEnable) {
            return false;
        }
        return true;
    }

    private void reportMonitorData(int keyCode, boolean down) {
        TurnOnWakeScreenManager turnOnWakeScreenManager;
        HwPartIawareUtil.getContinuePowerDevMng().keyDownEvent(keyCode, down);
        if (!down && keyCode == 26 && (turnOnWakeScreenManager = TurnOnWakeScreenManager.getInstance(this.mContext)) != null && turnOnWakeScreenManager.isTurnOnSensorSupport()) {
            TurnOnWakeScreenManager.getInstance(this.mContext).reportMonitorData("action=pressPowerKey");
        }
    }

    private boolean getIsSwich() {
        return sIsSwitch;
    }

    private void setIsSwich(boolean isSwich) {
        sIsSwitch = isSwich;
    }

    private boolean lightScreenOnPcMode(int keyCode) {
        boolean isDreaming = this.mPowerManagerInternal.isUserActivityScreenDimOrDream();
        if (HwPCUtils.isPcCastModeInServer() || HwPCUtils.getPhoneDisplayID() != -1) {
            if (keyCode == 601) {
                setIsSwich(isDreaming);
            }
            if (getIsSwich() && (keyCode == 3 || keyCode == 4)) {
                sIsSwitch = false;
                return true;
            }
        }
        if ((HwPCUtils.isPcCastModeInServer() || HwPCUtils.getPhoneDisplayID() != -1 || HwPCUtils.isInWindowsCastMode() || HwPCUtils.isDisallowLockScreenForHwMultiDisplay()) && !HwPCUtils.enabledInPad() && (keyCode == 26 || keyCode == 502 || keyCode == 511 || keyCode == 512 || keyCode == 513 || keyCode == 514 || keyCode == 501 || keyCode == 601 || keyCode == FRONT_FINGERPRINT_KEYCODE_HOME_UP)) {
            boolean keyHandled = false;
            try {
                IHwPCManager pcMgr = HwPCUtils.getHwPCManager();
                if (pcMgr != null && !pcMgr.isScreenPowerOn()) {
                    HwPCUtils.log(TAG, "some key set screen from OFF to ON");
                    pcMgr.setScreenPower(true);
                    resumeCurrentBrightness();
                    notifyScreenPowerChanged(true);
                    boolean isPhoneModeLocked = HwPCUtils.getPhoneDisplayID() != -1 && isScreenLocked() && keyCode == 26;
                    if (HwPCUtils.isDisallowLockScreenForHwMultiDisplay() || !isPhoneModeLocked) {
                        keyHandled = true;
                        if (keyCode == 26) {
                            cancelPendingPowerKeyActionForDistouch();
                        }
                    }
                }
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "lightScreenOnPcMode " + e);
                HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwPCDataReporter().reportFailLightScreen(1, keyCode, "");
            }
            if (isDreaming || keyHandled) {
                this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isRinging() {
        TelecomManager telecomManager = getTelecommService();
        return telecomManager != null && telecomManager.isRinging() && "1".equals(SystemProperties.get("persist.sys.show_incallscreen", "0"));
    }

    public KeyEvent dispatchUnhandledKey(WindowManagerPolicy.WindowState win, KeyEvent event, int policyFlags) {
        IHwSwingService hwSwing = HwSwingManager.getService();
        String pkgName = win == null ? "NA" : win.getOwningPackage();
        if (hwSwing != null) {
            try {
                if (hwSwing.dispatchUnhandledKey(event, pkgName)) {
                    return null;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "dispatchUnhandledKey error : " + e.getMessage());
            }
        }
        return HwPhoneWindowManager.super.dispatchUnhandledKey(win, event, policyFlags);
    }

    private void resumeCurrentBrightness() {
        Slog.i(TAG, "some key pressed, resumeCurrentBrightness");
        Bundle bundle = new Bundle();
        bundle.putBoolean("UpdateBrightnessEnable", true);
        int ret = HwPowerManager.setHwBrightnessData("ResetCurrentBrightness", bundle);
        if (ret != 0) {
            Slog.w(TAG, "resumeCurrentBrightness failed, ret = " + ret);
        }
    }

    private boolean isHardwareKeyboardConnected() {
        Log.i(TAG, "isHardwareKeyboardConnected--begin");
        int[] devices = InputDevice.getDeviceIds();
        boolean isConnected = false;
        int i = 0;
        while (true) {
            if (i >= devices.length) {
                break;
            }
            InputDevice device = InputDevice.getDevice(devices[i]);
            if (device != null) {
                if (device.getProductId() != 4817 || device.getVendorId() != 1455) {
                    if (device.isExternal() && (device.getSources() & LightsManagerEx.LIGHT_ID_SMARTBACKLIGHT) != 0) {
                        isConnected = true;
                        break;
                    }
                } else {
                    isConnected = true;
                    break;
                }
            }
            i++;
        }
        Log.i(TAG, "isHardwareKeyboardConnected--end");
        return isConnected;
    }

    private boolean isRightKey(int keyCode) {
        return (keyCode >= 7 && keyCode <= 16) || (keyCode >= 29 && keyCode <= 54);
    }

    private void setToolType() {
        StylusGestureListener stylusGestureListener;
        if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || (stylusGestureListener = this.mStylusGestureListener4PCMode) == null) {
            StylusGestureListener stylusGestureListener2 = this.mStylusGestureListener;
            if (stylusGestureListener2 != null) {
                stylusGestureListener2.setToolType();
                return;
            }
            return;
        }
        stylusGestureListener.setToolType();
    }

    private void handleGameEvent(KeyEvent event) {
        if (!(event.getAction() == 0) && event.getEventTime() - event.getDownTime() <= 500) {
            Slog.d(TAG, "gamekey arrive, notify GameAssist");
            HwGameAssistManager.notifyKeyEvent();
        }
    }

    private void handleGameSpace(KeyEvent event) {
        HwGameSpaceToggleManager hwGameSpaceToggleManager = this.mHwGameSpaceToggleManager;
        if (hwGameSpaceToggleManager != null) {
            hwGameSpaceToggleManager.handleGameSpace(event);
        }
    }

    private void interceptBlePowerKeyDown(KeyEvent event, boolean isScreenOn) {
        PowerManager.WakeLock wakeLock = this.mBlePowerKeyWakeLock;
        if (wakeLock != null && !wakeLock.isHeld()) {
            this.mBlePowerKeyWakeLock.acquire();
        }
        if (!isScreenOn) {
            wakeUpFromPowerKey(event.getDownTime());
        }
    }

    private void interceptBlePowerKeyUp(KeyEvent event, boolean isScreenOn, boolean isCanceled) {
        PowerManager.WakeLock wakeLock = this.mBlePowerKeyWakeLock;
        if (wakeLock != null && wakeLock.isHeld()) {
            this.mBlePowerKeyWakeLock.release();
        }
        if (isScreenOn && !isCanceled) {
            this.mRequestedOrGoingToSleep = true;
            this.mPowerManager.goToSleep(event.getDownTime(), 4, 0);
        }
    }

    private boolean isFocusApp(String pkgName) {
        if (pkgName == null) {
            Log.e(TAG, "isFocusApp, pkgName is null");
            return false;
        }
        WindowManagerPolicy.WindowState focusedWindow = this.mDefaultDisplayPolicy.getFocusedWindow();
        if (focusedWindow == null || focusedWindow.getAttrs() == null) {
            Log.e(TAG, "isFocusApp, focusedWindow or getAttrs is null");
            return false;
        }
        String focusPackageName = focusedWindow.getAttrs().packageName;
        Log.i(TAG, "isFocusApp, focusPackageName:" + focusPackageName + ",pkgName:" + pkgName);
        return pkgName.equals(focusPackageName);
    }

    private void launchUnderWaterCamera() {
        Log.i(TAG, "launchUnderWaterCamera");
        Intent intent = new Intent(HUAWEI_RAPIDCAPTURE_START_MODE);
        intent.setPackage(PKG_CAMERA);
        intent.putExtra("command", "launchUnderWaterCamera");
        try {
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to launchUnderWaterCamera throw IllegalStateException");
        } catch (ServiceConfigurationError e2) {
            Log.e(TAG, "Failed to launchUnderWaterCamera throw ServiceConfigurationError");
        } catch (SecurityException e3) {
            Log.e(TAG, "Failed to launchUnderWaterCamera throw SecurityException");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startNotepadActivity() {
        Intent notePadEditorIntent = new Intent("android.huawei.intent.action.note.handwriting");
        LogPower.push(148, "visible", NEW_HWNOTEPAD_PKG_NAME);
        LogPower.push(148, "visible", PKG_HWNOTEPAD);
        Log.i(TAG, "LogPower push notepad FREEZER_EXCEPTION success");
        try {
            this.mContext.startActivityAsUser(notePadEditorIntent, UserHandle.CURRENT_OR_SELF);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "startActivity notepad activity failed message : " + ex.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "startActivityAsUser(): notepad activity Exception");
        }
    }

    private void wakeupNoteEditor(KeyEvent event, boolean isInjected) {
        Log.i(TAG, "enter wakeupNoteEditor");
        if (!this.mSystemReady) {
            Log.d(TAG, "system not ready, return");
        } else if (!isInjected) {
            int keyCode = event.getKeyCode();
            boolean isWakeupTimeout = false;
            boolean isActionDown = event.getAction() == 0;
            if (keyCode != 704 || !isActionDown) {
                if (keyCode == 705 && isActionDown) {
                    Log.d(TAG, "recieved KEYCODE_STYLUS_POWERON");
                    PowerManager powerManager = (PowerManager) this.mContext.getSystemService(HIVOICE_PRESS_TYPE_POWER);
                    if (powerManager == null) {
                        return;
                    }
                    if (!powerManager.isScreenOn()) {
                        powerManager.wakeUp(SystemClock.uptimeMillis());
                        return;
                    }
                    if (Settings.Global.getInt(this.mContext.getContentResolver(), "stylus_state_activate", 0) == 1) {
                        isWakeupTimeout = true;
                    }
                    if (!isWakeupTimeout) {
                        Settings.Global.putInt(this.mContext.getContentResolver(), "stylus_state_activate", 1);
                        Log.d(TAG, "recieve stylus signal and activate stylus.");
                    }
                } else if (!this.mIsDeviceProvisioned) {
                    Log.d(TAG, "Device not Provisioned, return");
                } else if (isActionDown) {
                    wakeupSOSPage(keyCode);
                    if (this.mIsFreezePowerkey && keyCode == 26) {
                        powerPressBDReport(991310980);
                    }
                } else if (this.mIsFreezePowerkey && keyCode == 26) {
                    powerPressBDReport(991310981);
                }
            } else if (!this.mIsDeviceProvisioned) {
                Log.d(TAG, "Device not Provisioned, return");
            } else {
                long now = SystemClock.uptimeMillis();
                if (now - this.mLastWakeupTime >= 500) {
                    isWakeupTimeout = true;
                }
                if (!((PowerManager) this.mContext.getSystemService(HIVOICE_PRESS_TYPE_POWER)).isScreenOn() && isWakeupTimeout) {
                    String pkgName = getTopActivity();
                    Log.d(TAG, "wakeup NoteEditor now " + now + " screenOffTime " + this.mScreenOffTime);
                    if (!NOTEEDITOR_ACTIVITY_NAME.equals(pkgName) && !HW_NOTEEDITOR_ACTIVITY_NAME.equals(pkgName)) {
                        startNotepadActivity();
                        this.mLastWakeupTime = SystemClock.uptimeMillis();
                    } else if (now - this.mScreenOffTime < TOUCH_SPINNING_DELAY_MILLIS) {
                        this.mHandler.postDelayed(new Runnable() {
                            /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass15 */

                            @Override // java.lang.Runnable
                            public void run() {
                                if (PhoneWindowManager.HWFLOW) {
                                    Log.d(HwPhoneWindowManager.TAG, "start activity delayed");
                                }
                                HwPhoneWindowManager.this.startNotepadActivity();
                            }
                        }, 500);
                        this.mLastWakeupTime = SystemClock.uptimeMillis();
                    }
                }
            }
        }
    }

    private boolean setGSensorEnabled(int keyCode, boolean down, int deviceID) {
        boolean z = false;
        if (keyCode != 703) {
            return false;
        }
        if (!ESD_PROTECT_ENABLE && deviceID != this.mTpDeviceId) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("keyCode=");
        sb.append(keyCode);
        sb.append(" down=");
        sb.append(down);
        sb.append(" isFromTP=");
        if (deviceID == this.mTpDeviceId) {
            z = true;
        }
        sb.append(z);
        Log.i(TAG, sb.toString());
        if (down) {
            TurnOnWakeScreenManager.getInstance(this.mContext).setAccSensorEnabled(true);
        }
        return true;
    }

    private boolean isKidsSpecialPage() {
        String pkgName = getTopActivity();
        if (pkgName == null) {
            return false;
        }
        if (TIME_FINISH_INFO.equals(pkgName) || PARENTS_INFO.equals(pkgName) || DISTANCE_WARN_INFO.equals(pkgName) || POSITION_INFO.equals(pkgName) || REMOTE_HOUSEKEEPING_INFO.equals(pkgName)) {
            return true;
        }
        return false;
    }

    private int interceptHomeKeyEventForTvKidMode(int keycode, boolean isDown) {
        if (keycode != 3 || !IS_TV || !this.mIsKidsMode) {
            return 0;
        }
        if (isKidsSpecialPage() || isDown) {
            Log.i(TAG, " special page");
            return -1;
        }
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("himovietv://com.huawei.himovie.tv/startmainpage"));
        intent.setFlags(268435456);
        startActivityAsCurrentUser(intent);
        Log.i(TAG, " start hwtv activity");
        return -1;
    }

    private boolean interceptKeyInPCMultiCastMode(WindowManagerPolicy.WindowState win, KeyEvent event) {
        boolean isIntercepted = false;
        if (event.getKeyCode() != 4 || !HwActivityTaskManager.isPCMultiCastMode()) {
            return false;
        }
        int eventPositionX = event.getScanCode();
        Rect winRect = win.getDisplayFrameLw();
        List<Bundle> taskList = HwActivityTaskManager.getTaskList();
        Log.i(TAG, "win:" + win.toString() + " key:" + event + " winRect:" + winRect.toString() + " windowingMode:" + win.getWindowingMode() + " eventPositionX:" + eventPositionX + " mInPCMultiWindowModeLastHomeKeyDown:" + this.mInPCMultiWindowModeLastHomeKeyDown);
        int windowingMode = win.getWindowingMode();
        Optional.empty();
        if (eventPositionX == 0) {
            if (windowingMode == 105) {
                if (event.getAction() == 0) {
                    this.mInPCMultiWindowModeLastHomeKeyDown = true;
                    isIntercepted = true;
                }
                if (event.getAction() == 1) {
                    return dealKeyEventWhenFocusChange(taskList.stream().filter($$Lambda$HwPhoneWindowManager$f4sgQhn1l5ZCvq5gac07xwEdfRk.INSTANCE).reduce($$Lambda$HwPhoneWindowManager$2Ob6ag5n9SDgOWjuqpPEVhW64.INSTANCE), event);
                }
                return isIntercepted;
            } else if (event.getAction() != 1 || !this.mInPCMultiWindowModeLastHomeKeyDown) {
                return false;
            } else {
                this.mInPCMultiWindowModeLastHomeKeyDown = false;
                sendKeyEvent(4);
                return true;
            }
        } else if (winRect.left <= eventPositionX && winRect.right >= eventPositionX) {
            return false;
        } else {
            if (event.getAction() == 0) {
                isIntercepted = true;
            }
            if (event.getAction() == 1) {
                return dealKeyEventWhenFocusChange(taskList.stream().filter(new Predicate(eventPositionX) {
                    /* class com.android.server.policy.$$Lambda$HwPhoneWindowManager$THC5GvoSXJZZeGAIjQVLQ4v40Z4 */
                    private final /* synthetic */ int f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return HwPhoneWindowManager.lambda$interceptKeyInPCMultiCastMode$4(this.f$0, (Bundle) obj);
                    }
                }).reduce($$Lambda$HwPhoneWindowManager$mVt_bBzSDLgqaH5botr9xFMTzCw.INSTANCE), event);
            }
            return isIntercepted;
        }
    }

    static /* synthetic */ boolean lambda$interceptKeyInPCMultiCastMode$2(Bundle task) {
        return task.getInt(HwMultiDisplayManager.KEY_PC_MULTI_WINDOW_MODE) != 105 && task.getBoolean(HwMultiDisplayManager.KEY_PC_VISIBLE);
    }

    static /* synthetic */ Bundle lambda$interceptKeyInPCMultiCastMode$3(Bundle first, Bundle second) {
        return second;
    }

    static /* synthetic */ boolean lambda$interceptKeyInPCMultiCastMode$4(int eventPositionX, Bundle task) {
        Rect rect = (Rect) task.getParcelable(HwMultiDisplayManager.KEY_PC_RECT);
        return rect != null && rect.left <= eventPositionX && rect.right >= eventPositionX && task.getBoolean(HwMultiDisplayManager.KEY_PC_VISIBLE);
    }

    static /* synthetic */ Bundle lambda$interceptKeyInPCMultiCastMode$5(Bundle first, Bundle second) {
        return second;
    }

    private boolean dealKeyEventWhenFocusChange(Optional<Bundle> optional, KeyEvent event) {
        if (!optional.isPresent()) {
            return false;
        }
        Bundle findTask = optional.get();
        Log.d(TAG, "inHwPCMultiStackWindowingMode, set focus task = " + findTask.getInt(HwMultiDisplayManager.KEY_PC_TASKID) + ", scanCode = " + event.getScanCode());
        HwActivityTaskManager.setFocusedTaskForMultiDisplay(findTask.getInt(HwMultiDisplayManager.KEY_PC_TASKID));
        sendKeyEvent(4);
        this.mInPCMultiWindowModeLastHomeKeyDown = false;
        return true;
    }

    public long interceptKeyBeforeDispatching(WindowManagerPolicy.WindowState win, KeyEvent event, int policyFlags) {
        int deviceID;
        String lastIme;
        int keyCode = event.getKeyCode();
        int flags = event.getFlags();
        boolean isActionDown = event.getAction() == 0;
        boolean isInjected = (16777216 & policyFlags) != 0;
        int repeatCount = event.getRepeatCount();
        Flog.i((int) WifiProCommonUtils.RESP_CODE_INVALID_URL, "HwPhoneWindowManagerinterceptKeyTi keyCode=" + keyCode + " isActionDown=" + isActionDown + " repeatCount=" + repeatCount + " isInjected=" + isInjected);
        int deviceID2 = event.getDeviceId();
        if (setGSensorEnabled(keyCode, isActionDown, deviceID2)) {
            return -1;
        }
        if (HwPCUtils.isPcCastModeInServer()) {
            deviceID = deviceID2;
            if (event.getEventTime() - this.mLastKeyPointerTime > 500) {
                this.mLastKeyPointerTime = event.getEventTime();
                userActivityOnDesktop();
            }
        } else {
            deviceID = deviceID2;
        }
        if (HwPCUtils.isInWindowsCastMode()) {
            WindowManagerExt.updateFocusWindowFreezed(true);
        }
        if (handleInputEventInPCCastMode(event)) {
            return -1;
        }
        if (win != null && win.getWindowingMode() == 103 && keyCode == 66) {
            HwMwUtils.performPolicy(128, new Object[]{win});
        }
        if (win != null && interceptKeyInPCMultiCastMode(win, event)) {
            return -1;
        }
        try {
            if (this.mIHwWindowCallback != null) {
                this.mIHwWindowCallback.interceptKeyBeforeDispatching(event, policyFlags);
            }
        } catch (Exception ex) {
            Log.w(TAG, "mIHwWindowCallback interceptKeyBeforeDispatching threw RemoteException", ex);
        }
        if (repeatCount > 0) {
            notifyStatusBarManager(event);
        }
        int singleAppResult = getSingAppKeyEventResult(keyCode);
        if (singleAppResult != -2) {
            return (long) singleAppResult;
        }
        int result = getDisabledKeyEventResult(keyCode);
        if (result != -2) {
            if (isActionDown) {
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass16 */

                    @Override // java.lang.Runnable
                    public void run() {
                        Toast toast = Toast.makeText(HwPhoneWindowManager.this.mContext, 33685655, 0);
                        toast.getWindowParams().type = HwArbitrationDEFS.MSG_MPLINK_UNBIND_FAIL;
                        toast.getWindowParams().privateFlags |= 16;
                        toast.show();
                    }
                });
            }
            return (long) result;
        }
        int result2 = interceptHomeKeyEventForTvKidMode(keyCode, isActionDown);
        if (result2 < 0) {
            Log.i(TAG, "skip home key");
            return (long) result2;
        }
        if (isRightKey(keyCode) && isHardwareKeyboardConnected() && (lastIme = Settings.Secure.getString(this.mContext.getContentResolver(), "default_input_method")) != null && lastIme.contains("com.visionobjects.stylusmobile.v3_2_huawei")) {
            HwInputMethodManager.setDefaultIme("");
            setToolType();
        }
        int result1 = getGameControlKeyReslut(event);
        if (result1 != -2) {
            Log.i(TAG, "getGameControlKeyReslut return !");
            return (long) result1;
        } else if ((keyCode == 3 || keyCode == 187) && win != null && (win.getAttrs().hwFlags & FLOATING_MASK) == FLOATING_MASK) {
            return 0;
        } else {
            if (keyCode == 82 && !this.mDefaultDisplayPolicy.hasNavigationBar() && (268435456 & flags) == 0) {
                handleMenuKeyEvent(isActionDown, repeatCount);
                return -1;
            } else if (!this.mIsVolumeUpKeyDisTouch || this.mIsPowerKeyDisTouch || (flags & 1024) != 0) {
                if (keyCode == 24) {
                    if (this.mIsVolumeUpKeyConsumedByDisTouch) {
                        if (!isActionDown) {
                            this.mIsVolumeUpKeyConsumedByDisTouch = false;
                            this.mIsHintShown = false;
                        }
                        return -1;
                    } else if (this.mIsHintShown) {
                        if (!isActionDown) {
                            this.mIsHintShown = false;
                        }
                        return -1;
                    }
                }
                if (!isNeedPassEventToCloud(keyCode)) {
                    if (!isNeedPassEventToAssociateAssistant(keyCode)) {
                        if (handleDesktopKeyEvent(event, win)) {
                            return -1;
                        }
                        if (IS_TABLET && handlePadKeyEvent(event)) {
                            return -1;
                        }
                        if (keyCode == 3 || keyCode == 4 || keyCode == 25 || keyCode == 187) {
                            if (this.mIsHintShown) {
                                if (!isActionDown) {
                                    this.mIsHintShown = false;
                                }
                                return -1;
                            }
                            boolean isFrontFpNavi = FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION;
                            boolean isSupportTrikey = FrontFingerPrintSettings.isSupportTrikey();
                            boolean isMMITest = isMMITesting();
                            Flog.i((int) WifiProCommonUtils.RESP_CODE_INVALID_URL, "HwPhoneWindowManagerdeviceID:" + deviceID + " isFrontFpNavi:" + isFrontFpNavi + " isSupportTrikey:" + isSupportTrikey + " isMMITest:" + isMMITest);
                            if (deviceID > 0 && isFrontFpNavi && isSupportTrikey && !isMMITest && keyCode == 187) {
                                if (isTrikeyNaviKeycodeFromLON(isInjected, isExcluedRecentScene())) {
                                    return -1;
                                }
                                sendLightTimeoutMsg();
                                handleAppSwitchKeyEvent(isActionDown, repeatCount);
                                return -1;
                            }
                        }
                        if (this.mIsScreenRecorderChordEnabled && (flags & 1024) == 0) {
                            if (this.mIsScreenRecorderVolumeUpKeyTriggered && !this.mIsScreenRecorderPowerKeyTriggered) {
                                long now = SystemClock.uptimeMillis();
                                long timeoutTime = this.mScreenRecorderVolumeUpKeyTime + 150;
                                if (now < timeoutTime) {
                                    return timeoutTime - now;
                                }
                            }
                            if (keyCode == 24 && this.mIsScreenRecorderVolumeUpKeyConsumed) {
                                if (isActionDown) {
                                    return -1;
                                }
                                this.mIsScreenRecorderVolumeUpKeyConsumed = false;
                                return -1;
                            }
                        }
                        return HwPhoneWindowManager.super.interceptKeyBeforeDispatching(win, event, policyFlags);
                    }
                }
                return 0;
            } else {
                long now2 = SystemClock.uptimeMillis();
                long timeoutTime2 = this.mVolumeUpKeyDisTouchTime + 150;
                if (now2 < timeoutTime2) {
                    return timeoutTime2 - now2;
                }
                return -1;
            }
        }
    }

    private void notifyStatusBarManager(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == 24 || keyCode == 25) {
            this.mStatusBarManagerServiceEx.notifyKeyEvent(event);
        }
    }

    private void handleMenuKeyEvent(boolean isActionDown, int repeatCount) {
        if (!isActionDown) {
            if (this.mIsMenuClickedOnlyOnce) {
                this.mIsMenuClickedOnlyOnce = false;
                sendHwMenuKeyEvent();
            }
            cancelPreloadRecentApps();
        } else if (repeatCount == 0) {
            this.mIsMenuClickedOnlyOnce = true;
            preloadRecentApps();
        } else if (repeatCount == 1) {
            this.mIsMenuClickedOnlyOnce = false;
            toggleRecentApps();
        }
    }

    private void handleAppSwitchKeyEvent(boolean isActionDown, int repeatCount) {
        if (!isActionDown) {
            boolean handled = this.mRecentTrikeyHandled;
            if (!this.mRecentTrikeyHandled) {
                this.mRecentTrikeyHandled = true;
                this.mHandlerEx.removeMessages(MSG_TRIKEY_RECENT_LONG_PRESS);
            }
            if (!handled) {
                int i = this.mTrikeyNaviMode;
                if (i == 1) {
                    startHwVibrate(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP);
                    sendKeyEvent(4);
                } else if (i == 0) {
                    Flog.bdReport(991310017);
                    startHwVibrate(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP);
                    toggleRecentApps();
                }
            }
        } else if (repeatCount == 0) {
            this.mRecentTrikeyHandled = false;
            Message msg = this.mHandlerEx.obtainMessage(MSG_TRIKEY_RECENT_LONG_PRESS);
            msg.setAsynchronous(true);
            this.mHandlerEx.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
            if (this.mTrikeyNaviMode == 0) {
                preloadRecentApps();
            }
        }
    }

    private int getInputDeviceId(int inputSource) {
        int[] devIds = InputDevice.getDeviceIds();
        for (int devId : devIds) {
            InputDevice inputDev = InputDevice.getDevice(devId);
            if (inputDev != null && inputDev.supportsSource(inputSource)) {
                return devId;
            }
        }
        return 0;
    }

    private void initTpKeepParamters() {
        boolean ret = false;
        try {
            IServiceManager serviceManager = IServiceManager.getService();
            if (serviceManager != null) {
                ret = serviceManager.registerForNotifications(ITouchscreen.kInterfaceName, "", this.mServiceNotification);
            }
            if (!ret) {
                Slog.e(TAG, "Failed to register service start notification");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to register service start notification", e);
        }
        connectToProxy();
    }

    /* access modifiers changed from: private */
    public final class ServiceNotification extends IServiceNotification.Stub {
        private ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean preexisting) {
            Slog.i(HwPhoneWindowManager.TAG, "tp hal service started " + fqName + " " + name);
            HwPhoneWindowManager.this.connectToProxy();
        }
    }

    /* access modifiers changed from: private */
    public final class TPKeepDeathRecipient implements IHwBinder.DeathRecipient {
        private TPKeepDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            if (cookie == 1001) {
                Slog.e(HwPhoneWindowManager.TAG, "tp hal service died cookie: " + cookie);
                synchronized (HwPhoneWindowManager.this.mLock) {
                    HwPhoneWindowManager.this.mTpTouchSwitch = null;
                    if (HwPhoneWindowManager.this.mTpKeepListener != null) {
                        HwPhoneWindowManager.this.mTpKeepListener.setTpKeep(false);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connectToProxy() {
        synchronized (this.mLock) {
            if (this.mTpTouchSwitch == null) {
                try {
                    this.mTpTouchSwitch = ITouchscreen.getService();
                    if (this.mTpTouchSwitch != null) {
                        this.mTpTouchSwitch.linkToDeath(new TPKeepDeathRecipient(), 1001);
                    }
                } catch (NoSuchElementException e) {
                    Slog.e(TAG, "connectToProxy: tp hal service not found. Did the service fail to start?", e);
                } catch (RemoteException e2) {
                    Slog.e(TAG, "connectToProxy: tp hal service not responding", e2);
                }
            }
        }
    }

    private void setTpKeep(int keyCode, boolean isActionDown) {
        if (this.mIsProximityTop && isActionDown && this.mTpKeepListener != null) {
            if (keyCode == 708) {
                this.mTpKeepListener.setTpKeep(true);
            } else if (keyCode == 709) {
                this.mTpKeepListener.setTpKeep(false);
            }
        }
    }

    public void setTPDozeMode(int scene, int mode) {
        ITouchscreen iTouchscreen = this.mTpTouchSwitch;
        if (iTouchscreen == null) {
            Slog.d(TAG, "get touch service failed");
            return;
        }
        try {
            boolean isSuccess = iTouchscreen.hwTsSetDozeMode(scene, mode, 0);
            if (Log.isLoggable(TAG, 3)) {
                Slog.d(TAG, "set parameter scene:" + scene + ",mode:" + mode + "  sucess:" + isSuccess);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "set doze mode RemoteException:" + e.getMessage());
        } catch (Exception e2) {
            Slog.e(TAG, "get service  error");
        }
    }

    public void setScreenChangedReason(int reason) {
        ITouchscreen iTouchscreen = this.mTpTouchSwitch;
        if (iTouchscreen == null) {
            Slog.e(TAG, "touch service not available");
            return;
        }
        try {
            int result = iTouchscreen.hwSetFeatureConfig(4, Integer.toString(reason));
            Slog.d(TAG, "finish hwSetFeatureConfig with result: " + result + " reasonString: " + reason);
        } catch (RemoteException e) {
            Log.e(TAG, "updateAudioStatus RemoteException");
        }
    }

    private void showStartMenu() {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.showStartMenu();
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException showStartMenu");
            }
        }
    }

    private void screenshotPc() {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.screenshotPc();
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException screenshotPc");
            }
        }
    }

    private void closeTopWindow() {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.closeTopWindow();
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException closeTopWindow");
            }
        }
    }

    private void triggerSwitchTaskView(boolean show) {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.triggerSwitchTaskView(show);
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException triggerSwitchTaskView");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void lockScreen(boolean isLocked) {
        if (HwPCUtils.isInWindowsCastMode()) {
            if (this.mWindowManagerInternal.getActivityTaskManagerService().mHwATMSEx.isNewPcMultiCastMode()) {
                if (!isLocked || !isKeyguardLocked()) {
                    sendHideViewMsg(-1, 1, 0);
                } else {
                    sendShowViewMsg(-1, 1, 0);
                }
            } else if (isLocked) {
                sHwWindowsCastManager.sendShowViewMsg(1);
            } else {
                sHwWindowsCastManager.sendHideViewMsg(1);
            }
        }
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.lockScreen(isLocked);
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException lockScreen");
            }
        }
    }

    public void sendShowViewMsg(int taskId, int type, int mode) {
        Slog.i(TAG, "sendShowViewMsg: type " + type + " taskId " + taskId + " mode " + mode);
        Message msg = Message.obtain();
        if (type == 1) {
            msg.what = 40;
        } else {
            msg.what = 42;
            msg.arg1 = taskId;
            msg.arg2 = mode;
        }
        this.mHandler.sendMessage(msg);
    }

    public void sendHideViewMsg(int taskId, int type, int mode) {
        Slog.i(TAG, "sendHideViewMsg: type " + type + " taskId " + taskId);
        Message msg = Message.obtain();
        if (type == 1) {
            msg.what = 41;
        } else {
            msg.what = 43;
            msg.arg1 = taskId;
            msg.arg2 = mode;
        }
        this.mHandler.sendMessage(msg);
    }

    public void showLockSceenViewIfNeeded() {
        this.mHwMultiDisplayManager.showAllReminderIfNeeded();
    }

    public void hideLockSceenViewIfNeeded() {
        this.mHwMultiDisplayManager.hideAllReminderIfNeeded();
    }

    public void showSecureViewIfNeeded(int taskId, int mode) {
        this.mHwMultiDisplayManager.showSecureReminderIfNeeded(taskId, mode);
    }

    public void hideSecureViewIfNeeded(int taskId, int mode) {
        this.mHwMultiDisplayManager.hideSecureReminderIfNeeded(taskId, mode);
    }

    public void sendMessageHint(int taskId) {
        this.mHwMultiDisplayManager.sendMessageHint(taskId);
    }

    private void toggleHome() {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.toggleHome();
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException toggleHome");
            }
        }
    }

    private void dispatchKeyEventForExclusiveKeyboard(KeyEvent ke) {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.dispatchKeyEventForExclusiveKeyboard(ke);
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException dispatchKeyEvent");
            }
        }
    }

    private void userActivityOnDesktop() {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.userActivityOnDesktop();
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException userActivityOnDesktop");
            }
        }
    }

    private final void sendHwMenuKeyEvent() {
        int[] actions;
        for (int i : new int[]{0, 1}) {
            long curTime = SystemClock.uptimeMillis();
            InputManager.getInstance().injectInputEvent(new KeyEvent(curTime, curTime, i, 82, 0, 0, -1, 0, 268435464, LightsManagerEx.LIGHT_ID_SMARTBACKLIGHT), 0);
        }
    }

    /* access modifiers changed from: protected */
    public void launchAssistAction(String hint, int deviceId) {
        if (!checkPackageInstalled("com.google.android.googlequicksearchbox")) {
            sendCloseSystemWindows();
            boolean isVoiceAssistantEnabled = true;
            if (Settings.Secure.getInt(this.mContext.getContentResolver(), "hw_long_home_voice_assistant", 0) != 1) {
                isVoiceAssistantEnabled = false;
            }
            if (IS_LONG_HOME_VASSITANT && isVoiceAssistantEnabled) {
                performHapticFeedbackLw(null, 0, false, "launchAssistAction");
                String intent = "android.intent.action.ASSIST";
                try {
                    if (checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE) || checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE_OVERSEA)) {
                        intent = VOICE_ASSISTANT_ACTION;
                    }
                    this.mContext.startActivity(new Intent(intent).setFlags(268435456));
                } catch (ActivityNotFoundException anfe) {
                    Slog.w(TAG, "No activity to handle voice assistant action.", anfe);
                }
            }
        } else {
            HwPhoneWindowManager.super.launchAssistAction(hint, deviceId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkPackageInstalled(String packageName) {
        try {
            this.mContext.getPackageManager().getPackageInfo(packageName, 128);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isFMActive() {
        return AudioSystem.getDeviceConnectionState(1048576, "") == 1;
    }

    private boolean isVassistantInstall() {
        return checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE) || checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE_OVERSEA);
    }

    /* access modifiers changed from: package-private */
    public boolean isMusicActive() {
        if (((AudioManager) this.mContext.getSystemService("audio")) != null) {
            return AudioSystem.isStreamActive(3, 0);
        }
        Log.w(TAG, "isMusicActive: couldn't get AudioManager reference");
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isDeviceProvisioned() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
    }

    /* access modifiers changed from: package-private */
    public void handleVolumeKey(int stream, int keycode) {
        PowerManager.WakeLock wakeLock;
        IAudioService audioService = getAudioService();
        if (audioService != null) {
            try {
                if (this.mBroadcastWakeLock != null) {
                    this.mBroadcastWakeLock.acquire();
                }
                audioService.adjustStreamVolume(stream, keycode == 24 ? 1 : -1, 0, this.mContext.getOpPackageName());
                wakeLock = this.mBroadcastWakeLock;
                if (wakeLock == null) {
                    return;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "IAudioService.adjust*StreamVolume() threw RemoteException");
                wakeLock = this.mBroadcastWakeLock;
                if (wakeLock == null) {
                    return;
                }
            } catch (Throwable th) {
                PowerManager.WakeLock wakeLock2 = this.mBroadcastWakeLock;
                if (wakeLock2 != null) {
                    wakeLock2.release();
                }
                throw th;
            }
            wakeLock.release();
        }
    }

    private void sendVolumeDownKeyPressed() {
        this.mHandler.postDelayed(this.mHandleVolumeDownKey, 500);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelVolumeDownKeyPressed() {
        this.mHandler.removeCallbacks(this.mHandleVolumeDownKey);
    }

    private void resetVolumeDownKeyPressed() {
        if (this.mHandler.hasCallbacks(this.mHandleVolumeDownKey)) {
            this.mHandler.removeCallbacks(this.mHandleVolumeDownKey);
            this.mHandler.post(this.mHandleVolumeDownKey);
        }
    }

    private void interceptQuickCallChord() {
        this.mHandler.postDelayed(this.mVolumeDownLongPressed, 500);
    }

    private void cancelPendingQuickCallChordAction() {
        this.mHandler.removeCallbacks(this.mVolumeDownLongPressed);
        resetVolumeDownKeyPressed();
    }

    private void resetVolumeDownKeyLongPressed() {
        if (this.mHandler.hasCallbacks(this.mVolumeDownLongPressed)) {
            this.mHandler.removeCallbacks(this.mVolumeDownLongPressed);
            this.mHandler.post(this.mVolumeDownLongPressed);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initQuickcall() {
        if (this.mPowerManager != null) {
            this.mBroadcastWakeLock = this.mPowerManager.newWakeLock(1, "HwPhoneWindowManager.mBroadcastWakeLock");
            this.mVolumeDownWakeLock = this.mPowerManager.newWakeLock(1, "HwPhoneWindowManager.mVolumeDownWakeLock");
            this.mBlePowerKeyWakeLock = this.mPowerManager.newWakeLock(1, "HwPhoneWindowManager.mBlePowerKeyWakeLock");
        }
        this.mIsHeadless = "1".equals(SystemProperties.get("ro.config.headless", "0"));
    }

    private void notifyRapidCaptureService(String command) {
        if (this.mSystemReady) {
            if (!IS_SUPPORT_RAPID_CAPTURE) {
                Slog.i(TAG, "not support rapid capture");
            } else if (isTvMode()) {
                Slog.d(TAG, "Rapid capture not support on TV");
            } else {
                Intent intent = new Intent(HUAWEI_RAPIDCAPTURE_START_MODE);
                intent.setPackage(PKG_CAMERA);
                intent.putExtra("command", command);
                try {
                    this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
                } catch (Exception e) {
                    Slog.e(TAG, "unable to start service:" + intent, e);
                }
                PowerManager.WakeLock wakeLock = this.mVolumeDownWakeLock;
                if (wakeLock != null) {
                    wakeLock.acquire(500);
                }
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Log.i(TAG, "start Rapid Capture Service, command:" + extras.get("command"));
                }
            }
        }
    }

    public void showHwTransientBars() {
        this.mDefaultDisplayPolicy.getStatusBar();
    }

    public void transactToStatusBarService(int code, String transactName, String paramName, int paramValue) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            IBinder statusBarServiceBinder = getHWStatusBarService().asBinder();
            if (statusBarServiceBinder != null) {
                data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                if (paramName != null) {
                    data.writeInt(paramValue);
                }
                statusBarServiceBinder.transact(code, data, reply, 0);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService four params->threw remote exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    /* access modifiers changed from: protected */
    public void transactToStatusBarService(int code, String transactName, int isEmuiStyle, int statusbarColor, int navigationBarColor, int isEmuiLightStyle) {
        IBinder statusBarServiceBinder;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (!(getHWStatusBarService() == null || (statusBarServiceBinder = getHWStatusBarService().asBinder()) == null)) {
                Log.d(TAG, "Transact:" + transactName + " to status bar service");
                data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                data.writeInt(isEmuiStyle);
                data.writeInt(statusbarColor);
                data.writeInt(navigationBarColor);
                data.writeInt(isEmuiLightStyle);
                statusBarServiceBinder.transact(code, data, reply, 0);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService->threw remote exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    public void updateSystemUiColorLw(WindowManagerPolicy.WindowState win) {
        boolean isColorChanged;
        if (win != null) {
            if (isCoverWindow(win)) {
                Slog.i(TAG, "updateSystemUiColorLw isCoverWindow return " + win);
                return;
            }
            WindowManager.LayoutParams attrs = win.getAttrs();
            if (this.mLastColorWin != win || this.mLastStatusBarColor != attrs.statusBarColor || this.mLastNavigationBarColor != attrs.navigationBarColor) {
                boolean isFloating = getFloatingValue(attrs.isEmuiStyle);
                boolean isPopup = attrs.type == 1000 || attrs.type == 1002 || attrs.type == 2009 || attrs.type == 2010 || attrs.type == 2003;
                if (attrs.type == 3) {
                }
                boolean isTouchExplrEnabled = this.mAccessibilityManager.isTouchExplorationEnabled();
                int isEmuiStyle = getEmuiStyleValue(attrs.isEmuiStyle);
                int statusBarColor = attrs.statusBarColor;
                int navigationBarColor = attrs.navigationBarColor;
                if (!isTouchExplrEnabled) {
                    isColorChanged = (this.mLastStatusBarColor == statusBarColor && this.mLastNavigationBarColor == navigationBarColor) ? false : true;
                } else {
                    isColorChanged = isTouchExplrEnabled != this.mIsTouchExplrEnabled;
                    isEmuiStyle = -2;
                }
                boolean isStyleChanged = isEmuiStyleChanged(isEmuiStyle);
                boolean isIgnoreWindow = (win == this.mDefaultDisplayPolicy.getStatusBar() || attrs.type == 2024 || isKeyguardHostWindow(attrs)) || isFloating || isPopup || (attrs.type == 2034) || (win.getWindowingMode() != 3 && win.inMultiWindowMode());
                boolean isChanged = (isStyleChanged && !isIgnoreWindow) || (!isStyleChanged && !isIgnoreWindow && isColorChanged);
                if (!isIgnoreWindow) {
                    win.setCanCarryColors(true);
                }
                this.mLastNavigationBarColor = isTouchExplrEnabled ? -16777216 : navigationBarColor;
                if (isChanged) {
                    this.mLastStatusBarColor = isTouchExplrEnabled ? -16777216 : statusBarColor;
                    this.mLastIsEmuiStyle = isEmuiStyle;
                    this.mIsTouchExplrEnabled = isTouchExplrEnabled;
                    this.mLastColorWin = win;
                    Slog.v(TAG, "updateSystemUiColorLw window=" + win + ",EmuiStyle=" + isEmuiStyle + ",StatusBarColor=0x" + Integer.toHexString(statusBarColor) + ",NavigationBarColor=0x" + Integer.toHexString(navigationBarColor) + ", mForceNotchStatusBar=" + getForceNotchStatusBar());
                    this.mHandler.post(new Runnable() {
                        /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass19 */

                        @Override // java.lang.Runnable
                        public void run() {
                            if (HwPhoneWindowManager.this.mIsNotchSwitchOpen || (!HwPhoneWindowManager.this.getForceNotchStatusBar() && !HwPhoneWindowManager.this.mIsForceSetStatusBar)) {
                                if (HwPhoneWindowManager.this.mLastIsEmuiStyle == -1) {
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException e) {
                                        Slog.v(HwPhoneWindowManager.TAG, "InterruptedException is happed in method updateSystemUiColorLw");
                                    }
                                }
                                HwPhoneWindowManager hwPhoneWindowManager = HwPhoneWindowManager.this;
                                hwPhoneWindowManager.transactToStatusBarService(106, "setSystemUIColor", hwPhoneWindowManager.mLastIsEmuiStyle, HwPhoneWindowManager.this.mLastStatusBarColor, HwPhoneWindowManager.this.mLastNavigationBarColor, -1);
                            }
                        }
                    });
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getEmuiStyleValue(int styleValue) {
        if (styleValue == -1) {
            return -1;
        }
        return Integer.MAX_VALUE & styleValue;
    }

    /* access modifiers changed from: protected */
    public boolean isEmuiStyleChanged(int isEmuiStyle) {
        return this.mLastIsEmuiStyle != isEmuiStyle;
    }

    /* access modifiers changed from: protected */
    public boolean getFloatingValue(int styleValue) {
        return styleValue != -1 && (styleValue & FLOATING_MASK) == FLOATING_MASK;
    }

    @Override // android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener
    public void onTouchExplorationStateChanged(boolean enabled) {
    }

    /* access modifiers changed from: protected */
    public void hwInit() {
        this.mAccessibilityManager.addTouchExplorationStateChangeListener(this);
    }

    /* access modifiers changed from: package-private */
    public IStatusBarService getHWStatusBarService() {
        IStatusBarService iStatusBarService;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarService == null) {
                this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
            }
            iStatusBarService = this.mStatusBarService;
        }
        return iStatusBarService;
    }

    private class PolicyHandlerEx extends Handler {
        private PolicyHandlerEx() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == HwPhoneWindowManager.MSG_FREEZE_POWER_KEY) {
                Log.d(HwPhoneWindowManager.TAG, "Emergency power FreezePowerkey timeout.");
                HwPhoneWindowManager.this.mIsFreezePowerkey = false;
            } else if (i != HwPhoneWindowManager.MSG_DISABLED_POWER_KEY) {
                switch (i) {
                    case 103:
                        HwPhoneWindowManager.this.enableFingerPrintActions();
                        return;
                    case 104:
                        HwPhoneWindowManager.this.disableFingerPrintActions();
                        return;
                    case 105:
                        HwPhoneWindowManager.this.enableSwingMotion();
                        return;
                    case 106:
                        HwPhoneWindowManager.this.disableSwingMotion();
                        return;
                    case 107:
                        HwPhoneWindowManager.this.finishSetFoldMode(msg.arg1, msg.arg2);
                        return;
                    case 108:
                        HwPhoneWindowManager.this.finishScreenTurningOnEx();
                        return;
                    case 109:
                        if (!HwPhoneWindowManager.this.isTvMode()) {
                            return;
                        }
                        if (msg.obj instanceof Boolean) {
                            HwPhoneWindowManager.this.noticePowerKeyPressed(((Boolean) msg.obj).booleanValue());
                            return;
                        } else {
                            Log.e(HwPhoneWindowManager.TAG, "MSG_POWER_KEY_PRESS msg.obj invalid.");
                            return;
                        }
                    case 110:
                        if (!HwPhoneWindowManager.this.isTvMode()) {
                            return;
                        }
                        if (msg.obj instanceof Boolean) {
                            HwPhoneWindowManager.this.noticeVoiceAssistKeyPressed(((Boolean) msg.obj).booleanValue());
                            return;
                        } else {
                            Log.e(HwPhoneWindowManager.TAG, "MSG_VOICE_ASSIST_KEY_PRESS msg.obj invalid.");
                            return;
                        }
                    case 111:
                        HwPhoneWindowManager.this.sendKeyEvent(msg.arg1);
                        return;
                    case 112:
                        HwPhoneWindowManager.this.launchWatchVassistant();
                        return;
                    case 113:
                        if (!HwPhoneWindowManager.this.isTvMode()) {
                            return;
                        }
                        if (msg.obj instanceof Integer) {
                            HwPhoneWindowManager.this.noticeTvCustomKeyPressed(((Integer) msg.obj).intValue());
                            return;
                        } else {
                            Log.e(HwPhoneWindowManager.TAG, "MSG_VOICE_ASSIST_KEY_PRESS msg.obj invalid.");
                            return;
                        }
                    default:
                        switch (i) {
                            case HwPhoneWindowManager.MSG_TRIKEY_BACK_LONG_PRESS /* 4097 */:
                                HwPhoneWindowManager hwPhoneWindowManager = HwPhoneWindowManager.this;
                                hwPhoneWindowManager.mBackTrikeyHandled = true;
                                if (hwPhoneWindowManager.mTrikeyNaviMode == 1) {
                                    HwPhoneWindowManager.this.startHwVibrate(HwPhoneWindowManager.VIBRATOR_LONG_PRESS_FOR_FRONT_FP);
                                    Log.i(HwPhoneWindowManager.TAG, "LEFT->RECENT; RIGHT->BACK, handle longpress with recentTrikey and toggleSplitScreen");
                                    ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).toggleSplitScreen();
                                    return;
                                } else if (HwPhoneWindowManager.this.mTrikeyNaviMode == 0) {
                                    Log.i(HwPhoneWindowManager.TAG, "LEFT->BACK; RIGHT->RECENT, handle longpress with backTrikey and unlockScreenPinningTest");
                                    HwPhoneWindowManager.this.unlockScreenPinningTest();
                                    return;
                                } else {
                                    return;
                                }
                            case HwPhoneWindowManager.MSG_TRIKEY_RECENT_LONG_PRESS /* 4098 */:
                                HwPhoneWindowManager hwPhoneWindowManager2 = HwPhoneWindowManager.this;
                                hwPhoneWindowManager2.mRecentTrikeyHandled = true;
                                if (hwPhoneWindowManager2.mTrikeyNaviMode == 0) {
                                    HwPhoneWindowManager.this.startHwVibrate(HwPhoneWindowManager.VIBRATOR_LONG_PRESS_FOR_FRONT_FP);
                                    Log.i(HwPhoneWindowManager.TAG, "LEFT->BACK; RIGHT->RECENT, handle longpress with recentTrikey and toggleSplitScreen");
                                    ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).toggleSplitScreen();
                                    return;
                                } else if (HwPhoneWindowManager.this.mTrikeyNaviMode == 1) {
                                    Log.i(HwPhoneWindowManager.TAG, "LEFT->RECENT; RIGHT->BACK, handle longpress with backTrikey and unlockScreenPinningTest");
                                    HwPhoneWindowManager.this.unlockScreenPinningTest();
                                    return;
                                } else {
                                    return;
                                }
                            case HwPhoneWindowManager.MSG_BUTTON_LIGHT_TIMEOUT /* 4099 */:
                                if (HwPhoneWindowManager.this.mButtonLight == null) {
                                    return;
                                }
                                if (HwPhoneWindowManager.this.mPowerManager == null || !HwPhoneWindowManager.this.mPowerManager.isScreenOn()) {
                                    HwPhoneWindowManager.this.setButtonLightTimeout(false);
                                    return;
                                }
                                HwPhoneWindowManager.this.mButtonLight.setBrightness(0);
                                HwPhoneWindowManager.this.setButtonLightTimeout(true);
                                return;
                            case HwPhoneWindowManager.MSG_NOTIFY_FINGER_OPTICAL /* 4100 */:
                                HwPhoneWindowManager.this.notifyFingerOptical();
                                return;
                            default:
                                return;
                        }
                }
            } else if (msg.obj instanceof Boolean) {
                Slog.i(HwPhoneWindowManager.TAG, "MDM disabled the shutdown menu");
                Intent intent = new Intent(HwPhoneWindowManager.SHUTDOWN_MENU_DISABLED);
                intent.putExtra("isTvButtonPress", ((Boolean) msg.obj).booleanValue());
                HwPhoneWindowManager.this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, HwPhoneWindowManager.PERMISSION_MDM_DEVICE_MANAGER);
            } else {
                Slog.e(HwPhoneWindowManager.TAG, "MSG_DISABLED_POWER_KEY msg.obj invalid.");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyFingerOptical() {
        Log.i(TAG, "system ready, register pointer event listenr for UD Optical fingerprint");
        this.mFingerprintHardwareType = FingerprintManagerEx.getHardwareType();
        if (this.mFingerprintHardwareType == 1) {
            this.mWindowManagerFuncs.registerPointerEventListener(new WindowManagerPolicyConstants.PointerEventListener() {
                /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass20 */

                public void onPointerEvent(MotionEvent motionEvent) {
                    if (motionEvent.getActionMasked() == 1) {
                        FingerViewController.getInstance(HwPhoneWindowManager.this.mContext).notifyTouchUp(motionEvent.getRawX(), motionEvent.getRawY());
                    } else if (motionEvent.getActionMasked() == 6) {
                        int actionIndex = motionEvent.getActionIndex();
                        FingerViewController.getInstance(HwPhoneWindowManager.this.mContext).notifyTouchUp(motionEvent.getX(actionIndex), motionEvent.getY(actionIndex));
                    }
                }
            }, 0);
        }
    }

    public boolean checkPhoneOFFHOOK() {
        int callState = ((TelephonyManager) this.mContext.getSystemService("phone")).getCallState();
        Log.i(TAG, "callState : " + callState);
        return callState == 2;
    }

    public boolean checkHeadSetIsConnected() {
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        boolean isHeadSetConnected = false;
        if (audioManager == null) {
            return false;
        }
        if (audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn() || audioManager.isBluetoothScoOn()) {
            isHeadSetConnected = true;
        }
        Log.i(TAG, "checkHeadSetIsConnected : " + isHeadSetConnected);
        return isHeadSetConnected;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerBuoyListener() {
        if (this.mGameDockGesture == null || !HwGameDockGesture.isGameDockGestureFeatureOn()) {
            Log.i("HwGameDockGesture", "not Support: " + HwGameDockGesture.isGameDockGestureFeatureOn());
            return;
        }
        this.mGameDockGesture.enableGameDockGesture(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unRegisterBuoyListener() {
        if (this.mGameDockGesture != null && HwGameDockGesture.isGameDockGestureFeatureOn()) {
            this.mGameDockGesture.enableGameDockGesture(false);
        }
    }

    public void screenTurningOn(WindowManagerPolicy.ScreenOnListener screenOnListener) {
        HwPhoneWindowManager.super.screenTurningOn(screenOnListener);
        if (this.mContext == null) {
            Log.i(TAG, "Context object is null.");
            return;
        }
        setProximitySensorEnabled(true);
        DefaultHwFalseTouchMonitor defaultHwFalseTouchMonitor = this.mFalseTouchMonitor;
        if (defaultHwFalseTouchMonitor != null && defaultHwFalseTouchMonitor.isFalseTouchFeatureOn() && !this.mIsScreenOnForFalseTouch) {
            this.mIsScreenOnForFalseTouch = true;
            PointerEventListenerEx listenerEx = this.mFalseTouchMonitor.getEventListener();
            if (listenerEx != null) {
                this.mWindowManagerFuncs.registerPointerEventListener(listenerEx.getPointerEventListenerBridge(), 0);
            } else {
                return;
            }
        }
        if (IS_HW_EASY_WAKE_UP && this.mSystemReady) {
            KeyguardServiceDelegateEx keyguardDelegateEx = new KeyguardServiceDelegateEx();
            keyguardDelegateEx.setKeyguardServiceDelegate(this.mKeyguardDelegate);
            DefaultEasyWakeUpManager mWakeUpManager = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getEasyWakeUpManager(this.mContext, this.mHandler, keyguardDelegateEx);
            if (mWakeUpManager != null) {
                mWakeUpManager.turnOffSensorListener();
            }
        }
    }

    public void screenTurnedOn() {
        HwPhoneWindowManager.super.screenTurnedOn();
        FaceReportEventToIaware.reportEventToIaware(this.mContext, 20023);
        if (this.mSystemReady) {
            Slog.i(TAG, "UL_PowerscreenTurnedOn");
            if (this.mBooted) {
                HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.PICK_UP_WAKE_SCREEN_PART_FACTORY_IMPL).getPickUpWakeScreenManager().enablePickupMotionOrNot(false);
            }
            if (isAcquireProximityLock()) {
                WindowManagerPolicyEx policyEx = new WindowManagerPolicyEx();
                policyEx.setWindowManagerPolicy(this);
                this.mHwScreenOnProximityLock.acquireLock(policyEx, 0);
            }
            HwDisplaySidePolicy hwDisplaySidePolicy = this.mDisplaySidePolicy;
            if (hwDisplaySidePolicy != null) {
                hwDisplaySidePolicy.screenTurnedOn();
            }
            HwFoldScreenManagerInternal hwFoldScreenManagerInternal = this.mFoldScreenManagerService;
            if (hwFoldScreenManagerInternal != null) {
                hwFoldScreenManagerInternal.notifyScreenOn();
            }
            initBehaviorCollector();
            StylusGestureListener stylusGestureListener = this.mStylusGestureListener;
            if (stylusGestureListener != null) {
                stylusGestureListener.onScreenTurnedOn();
            }
            synchronized (this.mScreenTurnedOnLock) {
                this.mIsScreenTurnedOn = true;
            }
        }
    }

    public void screenTurnedOff() {
        HwPhoneWindowManager.super.screenTurnedOff();
        FaceReportEventToIaware.reportEventToIaware(this.mContext, 90023);
        setProximitySensorEnabled(false);
        DefaultHwFalseTouchMonitor defaultHwFalseTouchMonitor = this.mFalseTouchMonitor;
        if (defaultHwFalseTouchMonitor != null && defaultHwFalseTouchMonitor.isFalseTouchFeatureOn() && this.mIsScreenOnForFalseTouch) {
            this.mIsScreenOnForFalseTouch = false;
            PointerEventListenerEx listenerEx = this.mFalseTouchMonitor.getEventListener();
            if (listenerEx != null) {
                this.mWindowManagerFuncs.unregisterPointerEventListener(listenerEx.getPointerEventListenerBridge(), 0);
            } else {
                return;
            }
        }
        DefaultHwScreenOnProximityLock defaultHwScreenOnProximityLock = this.mHwScreenOnProximityLock;
        if (defaultHwScreenOnProximityLock != null) {
            defaultHwScreenOnProximityLock.releaseLock(1);
            Log.i(TAG, "HwScreenOnProximityLock quit mistouch view for screen off");
        }
        if (IS_HW_EASY_WAKE_UP && this.mSystemReady) {
            KeyguardServiceDelegateEx keyguardDelegateEx = new KeyguardServiceDelegateEx();
            keyguardDelegateEx.setKeyguardServiceDelegate(this.mKeyguardDelegate);
            DefaultEasyWakeUpManager mWakeUpManager = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getEasyWakeUpManager(this.mContext, this.mHandler, keyguardDelegateEx);
            if (mWakeUpManager != null) {
                mWakeUpManager.turnOnSensorListener();
            }
        }
        try {
            if (this.mIHwWindowCallback != null) {
                this.mIHwWindowCallback.screenTurnedOff();
            }
        } catch (Exception e) {
            Log.w(TAG, "mIHwWindowCallback threw RemoteException");
        }
        if (this.mSystemReady) {
            this.mScreenOffTime = SystemClock.uptimeMillis();
            Slog.i(TAG, "UL_PowerscreenTurnedOff");
            HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.PICK_UP_WAKE_SCREEN_PART_FACTORY_IMPL).getPickUpWakeScreenManager().enablePickupMotionOrNot(true);
            HwDisplaySidePolicy hwDisplaySidePolicy = this.mDisplaySidePolicy;
            if (hwDisplaySidePolicy != null) {
                hwDisplaySidePolicy.screenTurnedOff(false);
            }
            HwFoldScreenManagerInternal hwFoldScreenManagerInternal = this.mFoldScreenManagerService;
            if (hwFoldScreenManagerInternal != null) {
                hwFoldScreenManagerInternal.notifySleep();
            }
            StylusGestureListener stylusGestureListener = this.mStylusGestureListener;
            if (stylusGestureListener != null) {
                stylusGestureListener.onScreenTurnedOff();
            }
            synchronized (this.mScreenTurnedOnLock) {
                this.mIsScreenTurnedOn = false;
            }
        }
    }

    public void onConfigurationChanged() {
        SubScreenViewEntry subScreenViewEntry = this.mSubScreenViewEntry;
        if (subScreenViewEntry != null) {
            subScreenViewEntry.onConfigurationChanged();
        }
    }

    public void setRotationLw(int rotation) {
        SubScreenViewEntry subScreenViewEntry = this.mSubScreenViewEntry;
        if (subScreenViewEntry != null) {
            subScreenViewEntry.onRotationChanged(rotation);
        }
        HwDisplaySidePolicy hwDisplaySidePolicy = this.mDisplaySidePolicy;
        if (hwDisplaySidePolicy != null) {
            hwDisplaySidePolicy.onRotationChanged(rotation);
        }
    }

    public void notifyDispalyModeChangeBefore(int displayMode) {
        SubScreenViewEntry subScreenViewEntry = this.mSubScreenViewEntry;
        if (subScreenViewEntry != null) {
            subScreenViewEntry.handleDisplayModeChangeBefore(displayMode);
        }
    }

    public void updateAppView(RemoteViews remoteViews) {
        SubScreenViewEntry subScreenViewEntry = this.mSubScreenViewEntry;
        if (subScreenViewEntry != null) {
            subScreenViewEntry.updateAppView(remoteViews, "");
        }
    }

    public void removeAppView(boolean isNeedAddBtnView) {
        SubScreenViewEntry subScreenViewEntry = this.mSubScreenViewEntry;
        if (subScreenViewEntry != null) {
            subScreenViewEntry.removeAppView(isNeedAddBtnView);
        }
    }

    public int getRemoteViewsPid() {
        SubScreenViewEntry subScreenViewEntry = this.mSubScreenViewEntry;
        if (subScreenViewEntry != null) {
            return subScreenViewEntry.getRemoteViewsPid();
        }
        return -1;
    }

    public void handleAppDiedForRemoteViews() {
        SubScreenViewEntry subScreenViewEntry = this.mSubScreenViewEntry;
        if (subScreenViewEntry != null) {
            subScreenViewEntry.handleAppDiedForRemoteViews();
        }
    }

    public void handleCloseMobileViewChanged(boolean isMobileViewClosed) {
        SubScreenViewEntry subScreenViewEntry = this.mSubScreenViewEntry;
        if (subScreenViewEntry != null) {
            subScreenViewEntry.handleCloseMobileViewChanged(isMobileViewClosed);
        }
    }

    public void notifyUpdateAftPolicy(int ownerPid, int mode) {
        SubScreenViewEntry subScreenViewEntry = this.mSubScreenViewEntry;
        if (subScreenViewEntry != null) {
            subScreenViewEntry.notifyUpdateAftPolicy(ownerPid, mode);
        }
    }

    public boolean isGestureIsolated() {
        WindowManagerPolicy.WindowState win;
        if (this.mDefaultDisplayPolicy.getFocusedWindow() != null) {
            win = this.mDefaultDisplayPolicy.getFocusedWindow();
        } else {
            win = this.mDefaultDisplayPolicy.getTopFullscreenOpaqueWindowState();
        }
        return win != null && (win.getAttrs().hwFlags & 512) == 512;
    }

    public void requestTransientStatusBars() {
        getDefaultDisplayPolicy().requestTransientStatusBars();
    }

    public boolean isTopIsFullscreen() {
        WindowManagerPolicy.WindowState focusWindow = this.mDefaultDisplayPolicy.getFocusedWindow();
        if (focusWindow == null || focusWindow.getAttrs() == null) {
            return this.mDefaultDisplayPolicy.isTopIsFullscreen();
        }
        return ((focusWindow.getAttrs().flags & 1024) == 0 && (this.mDefaultDisplayPolicy.getLastSystemUiFlags() & 4) == 0) ? false : true;
    }

    public boolean okToShowTransientBar() {
        return this.mDefaultDisplayPolicy.checkShowTransientBarLw();
    }

    private void turnOnSensorListener() {
        if (this.mSensorManager == null) {
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        }
        if (this.mCoverManager == null) {
            this.mCoverManager = new CoverManager();
        }
        CoverManager coverManager = this.mCoverManager;
        if (coverManager != null) {
            this.mIsCoverOpen = coverManager.isCoverOpen();
        }
        boolean isTouchDisableModeOpen = Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_TOUCH_DISABLE_MODE, 1, -2) == 1;
        if (this.mIsCoverOpen && !this.mIsSensorRegisted && this.mListener != null && isTouchDisableModeOpen) {
            Log.i(TAG, "turnOnSensorListener, registerListener");
            SensorManager sensorManager = this.mSensorManager;
            sensorManager.registerListener(this.mListener, sensorManager.getDefaultSensor(8), 0);
            this.mIsSensorRegisted = true;
            this.mHandler.removeCallbacks(this.mProximitySensorTimeoutRunnable);
            this.mHandler.postDelayed(this.mProximitySensorTimeoutRunnable, LAUNCH_VASSIT_TIMEOUT);
        }
    }

    public void turnOffSensorListener() {
        if (this.mIsSensorRegisted && this.mListener != null) {
            Log.i(TAG, "turnOffSensorListener, unregisterListener ");
            this.mSensorManager.unregisterListener(this.mListener);
            this.mHandler.removeCallbacks(this.mProximitySensorTimeoutRunnable);
            this.mIsProximity = false;
        }
        this.mIsSensorRegisted = false;
    }

    public void setHwWindowCallback(IHwWindowCallback hwWindowCallback) {
        Log.i(TAG, "setHwWindowCallback=" + hwWindowCallback);
        this.mIHwWindowCallback = hwWindowCallback;
    }

    public IHwWindowCallback getHwWindowCallback() {
        return this.mIHwWindowCallback;
    }

    /* access modifiers changed from: private */
    public class ProximitySensorListener implements SensorEventListener {
        public ProximitySensorListener() {
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent arg0) {
            float[] its = arg0.values;
            if (its != null && arg0.sensor.getType() == 8 && its.length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("sensor value: its[0] = ");
                boolean z = false;
                sb.append(its[0]);
                Log.i(HwPhoneWindowManager.TAG, sb.toString());
                HwPhoneWindowManager hwPhoneWindowManager = HwPhoneWindowManager.this;
                if (its[0] >= 0.0f && its[0] < HwPhoneWindowManager.TYPICAL_PROXIMITY_THRESHOLD) {
                    z = true;
                }
                hwPhoneWindowManager.mIsProximity = z;
            }
        }
    }

    public void updateSettings() {
        Flog.i(1503, "updateSettings");
        HwPhoneWindowManager.super.updateSettings();
        setNaviBarState();
        updateSwingMotionState();
    }

    public void enableScreenAfterBoot() {
        HwPhoneWindowManager.super.enableScreenAfterBoot();
        this.mBooted = true;
        enableSystemWideAfterBoot(this.mContext);
        enableFingerPrintActionsAfterBoot(this.mContext);
        enableStylusAfterBoot(this.mContext);
        HwDisplaySidePolicy hwDisplaySidePolicy = this.mDisplaySidePolicy;
        if (hwDisplaySidePolicy != null) {
            hwDisplaySidePolicy.systemReady();
        }
    }

    public WindowManagerPolicy.WindowState getFocusedWindow() {
        if (this.mDefaultDisplayPolicy != null) {
            return this.mDefaultDisplayPolicy.getFocusedWindow();
        }
        return null;
    }

    public WindowManagerPolicy.WindowState getInputMethodWindow() {
        return this.mDefaultDisplayPolicy.getInputMethodWindow();
    }

    public WindowManagerPolicy.WindowState getNavigationBar() {
        return this.mDefaultDisplayPolicy.getNavigationBar();
    }

    public int getRestrictedScreenHeight() {
        return getDefaultDisplayPolicy().getRestrictedScreenHeight();
    }

    private void enableStylusAfterBoot(Context context) {
        if (HwStylusUtils.hasStylusFeature(context)) {
            Log.i(TAG, "enable stylus gesture feature.");
            this.mHandler.post(new Runnable() {
                /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass21 */

                @Override // java.lang.Runnable
                public void run() {
                    HwPhoneWindowManager.this.enableStylusAction();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableStylusAction() {
        if (this.mStylusGestureListener == null) {
            this.mStylusGestureListener = new StylusGestureListener(this.mContext, this);
            this.mWindowManagerFuncs.registerPointerEventListener(this.mStylusGestureListener, 0);
        }
    }

    public boolean isNavigationBarVisible() {
        return this.mDefaultDisplayPolicy.hasNavigationBar() && this.mDefaultDisplayPolicy.getNavigationBar() != null && this.mDefaultDisplayPolicy.getNavigationBar().isVisibleLw();
    }

    /* access modifiers changed from: protected */
    public void enableFingerPrintActions() {
        Log.d(TAG, "enableFingerPrintActions()");
        DefaultFingerprintActionsListener defaultFingerprintActionsListener = this.fingerprintActionsListener;
        if (defaultFingerprintActionsListener != null) {
            if (defaultFingerprintActionsListener instanceof PointerEventListenerEx) {
                this.mWindowManagerFuncs.unregisterPointerEventListener(this.fingerprintActionsListener.getPointerEventListenerBridge(), 0);
            }
            this.fingerprintActionsListener.destroySearchPanelView();
            this.fingerprintActionsListener.destroyMultiWinArrowView();
            this.fingerprintActionsListener = null;
        }
        PhoneWindowManagerEx policyEx = new PhoneWindowManagerEx();
        policyEx.setPhoneWindowManager(this);
        this.fingerprintActionsListener = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getFingerprintActionsListener(this.mContext, policyEx);
        if (this.fingerprintActionsListener instanceof PointerEventListenerEx) {
            this.mWindowManagerFuncs.registerPointerEventListener(this.fingerprintActionsListener.getPointerEventListenerBridge(), 0);
        }
        this.fingerprintActionsListener.createSearchPanelView();
        this.fingerprintActionsListener.createMultiWinArrowView();
    }

    /* access modifiers changed from: protected */
    public void disableFingerPrintActions() {
        DefaultFingerprintActionsListener defaultFingerprintActionsListener = this.fingerprintActionsListener;
        if (defaultFingerprintActionsListener != null) {
            if (defaultFingerprintActionsListener instanceof PointerEventListenerEx) {
                this.mWindowManagerFuncs.unregisterPointerEventListener(this.fingerprintActionsListener.getPointerEventListenerBridge(), 0);
            }
            this.fingerprintActionsListener.destroySearchPanelView();
            this.fingerprintActionsListener.destroyMultiWinArrowView();
            this.fingerprintActionsListener = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableSwingMotion() {
        Log.i(TAG, "enableSwingMotion");
        this.mWindowManagerFuncs.registerPointerEventListener(this.mSwingMotionPointerEventListener, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableSwingMotion() {
        Log.i(TAG, "disableSwingMotion");
        this.mWindowManagerFuncs.unregisterPointerEventListener(this.mSwingMotionPointerEventListener, 0);
    }

    private void updateSwingMotionState() {
        boolean isSwingMotionOrEyeGazeEnable = false;
        int slideScreenValue = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "item_space_sliding_switch", 0, -2);
        int eyeGazeValue = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "item_wakeup_gaze_switch", 0, -2);
        if (slideScreenValue == 1 || eyeGazeValue == 1) {
            isSwingMotionOrEyeGazeEnable = true;
        }
        if (isSwingMotionOrEyeGazeEnable != this.mIsSwingMotionOrEyeGazeEnable) {
            this.mHandlerEx.sendEmptyMessage(isSwingMotionOrEyeGazeEnable ? 105 : 106);
            this.mIsSwingMotionOrEyeGazeEnable = isSwingMotionOrEyeGazeEnable;
        }
    }

    /* access modifiers changed from: protected */
    public void enableFingerPrintActionsAfterBoot(Context context) {
        final ContentResolver resolver = context.getContentResolver();
        this.mHandler.post(new Runnable() {
            /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass23 */

            @Override // java.lang.Runnable
            public void run() {
                if (!FrontFingerPrintSettings.isNaviBarEnabled(resolver) || (FrontFingerPrintSettings.isSingleVirtualNavbarEnable(resolver) && !FrontFingerPrintSettings.isSingleNavBarAIEnable(resolver))) {
                    HwPhoneWindowManager.this.enableFingerPrintActions();
                } else {
                    HwPhoneWindowManager.this.disableFingerPrintActions();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void filmStateReporter() {
        this.mFilmState = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "film_state", -1, UserHandle.myUserId());
        this.mWindowManagerFuncs.registerPointerEventListener(new WindowManagerPolicyConstants.PointerEventListener() {
            /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass24 */

            public void onPointerEvent(MotionEvent motionEvent) {
                if (motionEvent.getAction() == 0) {
                    int currentFilmState = (int) motionEvent.getAxisValue(47);
                    if (currentFilmState != 1 && currentFilmState != 2) {
                        return;
                    }
                    if (HwPhoneWindowManager.this.mFilmState != currentFilmState) {
                        HwPhoneWindowManager.this.reportFilmState(currentFilmState);
                        HwPhoneWindowManager.this.mFilmState = currentFilmState;
                        Settings.Secure.putIntForUser(HwPhoneWindowManager.this.mContext.getContentResolver(), "film_state", currentFilmState, UserHandle.myUserId());
                    } else if (SystemClock.uptimeMillis() - HwPhoneWindowManager.this.mLastFilmReportTimeMs >= 86400000) {
                        HwPhoneWindowManager.this.reportFilmState(currentFilmState);
                    }
                }
            }
        }, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportFilmState(int currentFilmState) {
        Slog.i(TAG, "filmState:" + currentFilmState);
        Flog.bdReport(991310210, "filmState", currentFilmState);
        this.mLastFilmReportTimeMs = SystemClock.uptimeMillis();
    }

    /* access modifiers changed from: protected */
    public void setNaviBarState() {
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean isNavibarEnable = FrontFingerPrintSettings.isNaviBarEnabled(resolver);
        boolean isSingleNavBarEnable = FrontFingerPrintSettings.isSingleVirtualNavbarEnable(resolver);
        boolean isSingleNavBarAiEnable = FrontFingerPrintSettings.isSingleNavBarAIEnable(resolver);
        boolean isNavibarEnable2 = false;
        boolean isSingEnarAiEnable = isSingleNavBarEnable && !isSingleNavBarAiEnable;
        if (isSingEnarAiEnable || !isNavibarEnable) {
            isNavibarEnable2 = true;
        }
        Log.d(TAG, "setNaviBarState()--isNavibarEnable:" + isNavibarEnable2 + ";mNavibarEnabled:" + this.mNavibarEnabled + ";isSingleNavBarEnable:" + isSingleNavBarEnable + ";isSingleNavBarAiEnable:" + isSingleNavBarAiEnable + ";isSingEnarAiEnable:" + isSingEnarAiEnable);
        int i = 103;
        if (!this.mNaviBarStateInited) {
            if (this.mBooted) {
                this.mNavibarEnabled = isNavibarEnable2;
                Handler handler = this.mHandlerEx;
                if (!isNavibarEnable2) {
                    i = 104;
                }
                handler.sendEmptyMessage(i);
                this.mNaviBarStateInited = true;
            }
        } else if (this.mNavibarEnabled != isNavibarEnable2) {
            Log.d(TAG, "setNaviBarState()--" + this.mNavibarEnabled);
            this.mNavibarEnabled = isNavibarEnable2;
            Handler handler2 = this.mHandlerEx;
            if (!isNavibarEnable2) {
                i = 104;
            }
            handler2.sendEmptyMessage(i);
        }
    }

    /* access modifiers changed from: protected */
    public void updateSplitScreenView() {
        DefaultFingerprintActionsListener defaultFingerprintActionsListener;
        if ((!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer()) && (defaultFingerprintActionsListener = this.fingerprintActionsListener) != null) {
            defaultFingerprintActionsListener.createMultiWinArrowView();
        }
    }

    public DexClassLoader getDexClassLoader() {
        if (sDexClassLoader == null) {
            sDexClassLoader = new DexClassLoader(FINGERSENSE_JAR_PATH, null, null, this.mContext.getClassLoader());
        }
        return sDexClassLoader;
    }

    private void loadFingerSenseManager() {
        try {
            this.mFsManagerCls = getDexClassLoader().loadClass("com.huawei.fingersense.HwFingerSenseManager");
            this.mFsManagerObj = this.mFsManagerCls.getConstructor(Context.class, WindowManagerPolicy.WindowManagerFuncs.class).newInstance(this.mContext, this.mWindowManagerFuncs);
            Log.i(TAG, "loadFingerSenseManager success");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "loadFingerSenseManager error : 1");
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "loadFingerSenseManager error : 2");
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "loadFingerSenseManager error : 3");
        } catch (InstantiationException e4) {
            Log.e(TAG, "loadFingerSenseManager error : 4");
        } catch (InvocationTargetException e5) {
            Log.e(TAG, "loadFingerSenseManager error : 5");
        }
    }

    /* access modifiers changed from: protected */
    public void enableSystemWideAfterBoot() {
        Class cls = this.mFsManagerCls;
        if (cls == null || this.mFsManagerObj == null) {
            Log.e(TAG, "enableSystemWideAfterBoot fingersense object load fail");
            return;
        }
        try {
            cls.getMethod("enableSystemWideAfterBoot", new Class[0]).invoke(this.mFsManagerObj, new Object[0]);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "enableSystemWideAfterBoot error : 1");
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "enableSystemWideAfterBoot error : 2");
        } catch (InvocationTargetException e3) {
            Log.e(TAG, "enableSystemWideAfterBoot error : 3");
        }
    }

    public void processing_KEYCODE_SOUNDTRIGGER_EVENT(int keyCode, Context context, boolean isMusicOrFMActive, boolean isDwon, boolean isKeyguardShow) {
        Log.d(TAG, "intercept DSP WAKEUP EVENT" + keyCode + " isDwon=" + isDwon + " isKeyguardShow=" + isKeyguardShow);
        ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
        switch (keyCode) {
            case 401:
                if (isDwon) {
                    Log.i(TAG, "soundtrigger wakeup.");
                    if (isTOPActivity(HUAWEI_VOICE_SOUNDTRIGGER_PACKAGE)) {
                        Log.i(TAG, "start SoundTiggerTest");
                        notifySoundTriggerTest();
                        return;
                    } else if (isTOPActivity(HUAWEI_VOICE_DEBUG_BETACLUB)) {
                        Log.i(TAG, "soundtrigger debug during betaclub.");
                        notifySoundTriggerTest();
                        return;
                    } else {
                        Log.i(TAG, "start VA");
                        notifyVassistantService("start", 4, null);
                        return;
                    }
                } else {
                    return;
                }
            case 402:
                if (isDwon) {
                    Log.i(TAG, "command that find my phone.");
                    if (isTOPActivity(HUAWEI_VOICE_SOUNDTRIGGER_PACKAGE)) {
                        Log.i(TAG, "looking for my phone during SoundTiggerTest");
                        return;
                    } else if (isTOPActivity(HUAWEI_VOICE_DEBUG_BETACLUB)) {
                        Log.i(TAG, "looking for my phone during betaclub.");
                        return;
                    } else {
                        Log.i(TAG, "findphone.");
                        notifyVassistantService("findphone", 4, null);
                        return;
                    }
                } else {
                    return;
                }
            case 403:
            case 404:
            default:
                return;
        }
    }

    private boolean isTOPActivity(String appnames) {
        try {
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks != null) {
                if (!tasks.isEmpty()) {
                    for (ActivityManager.RunningTaskInfo info : tasks) {
                        Log.i(TAG, "info.topActivity.getPackageName() is " + info.topActivity.getPackageName());
                        if (info.topActivity.getPackageName().equals(appnames) && info.baseActivity.getPackageName().equals(appnames)) {
                            return true;
                        }
                    }
                    return false;
                }
            }
            return false;
        } catch (RuntimeException e) {
            Log.e(TAG, "isTOPActivity->RuntimeException happened");
        } catch (Exception e2) {
            Log.e(TAG, "isTOPActivity->other exception happened");
        }
    }

    private boolean isNeedPassEventToAssociateAssistant(int keyCode) {
        return !KEYCODE_NOT_FOR_AA.contains(Integer.valueOf(keyCode)) && IS_TABLET && isAssociateAssistantOnTop();
    }

    private boolean isAssociateAssistantActivityOnTop(ActivityManager.RunningTaskInfo info) {
        return ASSOCIATE_ASSISTANT_PACKAGE.equals(info.topActivity.getPackageName()) && ASSOCIATE_ASSISTANT_PACKAGE.equals(info.baseActivity.getPackageName()) && ASSOCIATE_ASSISTANT_ACTIVITY_NAME.equals(info.topActivity.getClassName());
    }

    private boolean isAssociateAssistantOnTop() {
        try {
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks != null) {
                if (!tasks.isEmpty()) {
                    for (ActivityManager.RunningTaskInfo info : tasks) {
                        if (info.topActivity != null) {
                            if (info.baseActivity != null) {
                                if (isAssociateAssistantActivityOnTop(info)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                    return false;
                }
            }
            return false;
        } catch (RuntimeException e) {
            Log.i(TAG, "isAssociateAssistantOnTop->RuntimeException happened");
        } catch (Exception e2) {
            Log.i(TAG, "isAssociateAssistantOnTop->other exception happened");
        }
    }

    private boolean isNeedPassEventToCloud(int keyCode) {
        return !KEYCODE_NOT_FOR_CLOUD.contains(Integer.valueOf(keyCode)) && (IS_TABLET || HwPCUtils.isPcCastModeInServer()) && isCloudOnTOP();
    }

    private boolean isCloudActivityOnTop(ActivityManager.RunningTaskInfo info) {
        return "com.huawei.cloud".equals(info.topActivity.getPackageName()) && "com.huawei.cloud".equals(info.baseActivity.getPackageName()) && "com.huawei.ahdp.session.VmActivity".equals(info.topActivity.getClassName());
    }

    private boolean isCloudOnTOP() {
        try {
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks != null) {
                if (!tasks.isEmpty()) {
                    for (ActivityManager.RunningTaskInfo info : tasks) {
                        if (info.topActivity != null) {
                            if (info.baseActivity != null) {
                                if (isCloudActivityOnTop(info) && (IS_TABLET || HwPCUtils.isPcDynamicStack(info.stackId))) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                    return false;
                }
            }
            return false;
        } catch (RuntimeException e) {
            HwPCUtils.log(TAG, "isCloudOnTOP->RuntimeException happened");
        } catch (Exception e2) {
            HwPCUtils.log(TAG, "isCloudOnTOP->other exception happened");
        }
    }

    private void removeHwVAFromPowerSaveWhitelist() {
        this.mHandler.postDelayed(this.mRemovePowerSaveWhitelistRunnable, 3000);
    }

    private void addHwVAToPowerSaveWhitelist() {
        if (this.mHandler.hasCallbacks(this.mRemovePowerSaveWhitelistRunnable)) {
            this.mHandler.removeCallbacks(this.mRemovePowerSaveWhitelistRunnable);
        }
        try {
            if (checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE)) {
                HwDeviceIdleController.addPowerSaveWhitelistApp(HUAWEI_VASSISTANT_PACKAGE);
            } else if (checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE_OVERSEA)) {
                HwDeviceIdleController.addPowerSaveWhitelistApp(HUAWEI_VASSISTANT_PACKAGE_OVERSEA);
            } else {
                Log.w(TAG, "vassistant not exists");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "add hwvassistant exception!");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyVassistantService(String command, int mode, KeyEvent event) {
        Intent intent = new Intent(ACTION_HUAWEI_VASSISTANT_SERVICE);
        intent.putExtra(HUAWEI_VASSISTANT_EXTRA_START_MODE, mode);
        intent.putExtra("command", command);
        if (event != null) {
            intent.putExtra("KeyEvent", event);
        }
        if (checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE)) {
            intent.setPackage(HUAWEI_VASSISTANT_PACKAGE);
        } else if (checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE_OVERSEA)) {
            intent.setPackage(HUAWEI_VASSISTANT_PACKAGE_OVERSEA);
        } else {
            Log.w(TAG, "vassistant not exists");
            return;
        }
        addHwVAToPowerSaveWhitelist();
        try {
            this.mContext.startService(intent);
        } catch (Exception e) {
            Slog.e(TAG, "unable to start service:" + intent, e);
        }
        removeHwVAFromPowerSaveWhitelist();
        PowerManager.WakeLock wakeLock = this.mVolumeDownWakeLock;
        if (wakeLock != null) {
            wakeLock.acquire(500);
        }
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Log.d(TAG, "start VASSISTANT Service, state:" + extras.get(HUAWEI_VASSISTANT_EXTRA_START_MODE) + " command:" + extras.get("command"));
        }
    }

    private void notifySoundTriggerTest() {
        try {
            this.mContext.sendBroadcast(new Intent(HUAWEI_VOICE_SOUNDTRIGGER_BROADCAST));
            Log.i(TAG, "start up HUAWEI_VOICE_SOUNDTRIGGER_BROADCAST");
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "No receiver to handle HUAWEI_VOICE_SOUNDTRIGGER_BROADCAST intent", e);
        }
    }

    private void notifyScreenPowerChanged(boolean state) {
        Intent intent = new Intent(SCREEN_POWER_CHANGED_ACTION);
        intent.putExtra("flag", state);
        intent.setPackage(PACKAGE_NAME_HWPCASSISTANT);
        intent.addFlags(268435456);
        intent.addFlags(16777216);
        this.mContext.sendBroadcast(intent);
    }

    private void cancelAIPowerLongPressed() {
        this.mHandler.removeCallbacks(this.mAIPowerLongPressed);
        Log.i(TAG, "cancel power long press");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void powerOffToast() {
        Toast toast = Toast.makeText(new ContextThemeWrapper(this.mContext, 33947656), this.mContext.getString(33686258, 3), 1);
        toast.getWindowParams().type = TOAST_TYPE_COVER_SCREEN;
        toast.getWindowParams().privateFlags |= 16;
        toast.show();
        this.showPowerOffToastTimes++;
        this.mPowerOffToastShown = true;
        Settings.Secure.putIntForUser(this.mResolver, KEY_TOAST_POWER_OFF, this.showPowerOffToastTimes, ActivityManager.getCurrentUser());
    }

    private void cancelPowerOffToast() {
        this.mHandler.removeCallbacks(this.mPowerOffRunner);
    }

    private void showPowerOffToast(boolean isScreenOn) {
        if (isScreenOn && this.mBooted && !isTvMode() && !this.mHasFeatureWatch) {
            if ((!IS_POWER_HIACTION_KEY || !HIVOICE_PRESS_TYPE_POWER.equals(this.mHiVoiceKeyType)) && this.showPowerOffToastTimes < 2) {
                this.mHandler.postDelayed(this.mPowerOffRunner, LAUNCH_VASSIT_TIMEOUT);
            }
        }
    }

    private void processPowerKey(Context context, int eventAction, int eventCode, long eventDnTime, long eventTime) {
        if (!mIsSidePowerFpComb || !this.mHwPWMEx.isPowerFpForbidGotoSleep()) {
            boolean isDwon = eventAction == 0;
            if (eventCode != 26) {
                Log.i(TAG, "Not POWER Key." + eventCode);
            } else if (isDwon) {
                this.mPowerKeyHandledByHiaction = false;
                this.mHandler.postDelayed(this.mAIPowerLongPressed, (long) this.mPowerLongPressTimeout);
            } else {
                Log.i(TAG, "Power up eventTime " + eventTime + " DnTime " + eventDnTime);
                cancelAIPowerLongPressed();
            }
        } else {
            Log.i(TAG, "Is Side PowerFpComb ");
        }
    }

    private void notifyHomoAiKeyEvent(int homoType, int pressType) {
        Intent intent = new Intent(HUAWEI_HIACTION_ACTION);
        intent.setPackage(HUAWEI_HIACTION_PACKAGE);
        intent.putExtra(HOMOAI_EVENT_TAG, homoType);
        intent.putExtra(HOMOAI_PRESS_TAG, pressType);
        if (homoType == 1) {
            powerPressBDReport(991310984);
        }
        try {
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
        } catch (Exception e) {
            Log.e(TAG, "start HiAction server err");
        }
        Log.i(TAG, "send HomoAi key " + homoType + " pressType " + pressType);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHomoAiKeyLongPress(int pressType) {
        notifyHomoAiKeyEvent(1, pressType);
    }

    private void notifyPowerKeyEventToHiAction(Context context, KeyEvent event) {
        if (HWFLOW) {
            Log.i(TAG, "mHiVoiceKeyType: " + this.mHiVoiceKeyType);
        }
        if (IS_POWER_HIACTION_KEY && HIVOICE_PRESS_TYPE_POWER.equals(this.mHiVoiceKeyType)) {
            processPowerKey(this.mContext, event.getAction(), event.getKeyCode(), event.getDownTime(), event.getEventTime());
        }
    }

    public boolean getNeedDropFingerprintEvent() {
        return this.mIsNeedDropFingerprintEvent;
    }

    private String getTopActivity() {
        ActivityInfo activityInfo = HwActivityTaskManager.getLastResumedActivity();
        if (activityInfo == null || activityInfo.getComponentName() == null) {
            return null;
        }
        return activityInfo.getComponentName().flattenToShortString();
    }

    private void initDropSmartKey() {
        String dropSmartKeyActivity = SettingsEx.Systemex.getString(this.mResolver, DROP_SMARTKEY_ACTIVITY);
        if (TextUtils.isEmpty(dropSmartKeyActivity)) {
            Log.w(TAG, "dropSmartKeyActivity not been configured in hw_defaults.xml!");
            return;
        }
        for (String str : dropSmartKeyActivity.split(AwarenessInnerConstants.SEMI_COLON_KEY)) {
            this.needDropSmartKeyActivities.add(str);
        }
    }

    private boolean needDropSmartKey() {
        boolean result = false;
        String topActivityName = getTopActivity();
        HashSet<String> hashSet = this.needDropSmartKeyActivities;
        if (hashSet != null && hashSet.contains(topActivityName)) {
            result = true;
            Log.d(TAG, "drop smartkey event because of conflict with fingerprint authentication!");
        }
        if ((!isCamera() || !this.mIsFingerShotCameraOn) && ((!isInCallUIAndRinging() || !this.mIsFingerAnswerPhoneOn) && (!isAlarm(this.mCurUser) || !this.mIsFingerStopAlarmOn))) {
            return result;
        }
        Log.d(TAG, "drop smartkey event because of conflict with fingerprint longpress event!");
        return true;
    }

    private boolean isCamera() {
        String pkgName = getTopActivity();
        return pkgName != null && pkgName.startsWith(PKG_CAMERA);
    }

    public boolean isKeyguardShortcutApps() {
        WindowManagerPolicy.WindowState focusedWindow = this.mDefaultDisplayPolicy.getFocusedWindow();
        if (focusedWindow == null || focusedWindow.getAttrs() == null) {
            Log.e(TAG, "isKeyguardShortcutApps, focusedWindow or getAttrs is null");
            return false;
        }
        String focusPackageName = focusedWindow.getAttrs().packageName;
        if (focusPackageName == null) {
            return false;
        }
        int len = this.mKeyguardShortcutApps.length;
        for (int i = 0; i < len; i++) {
            if (focusPackageName.startsWith(this.mKeyguardShortcutApps[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean isLsKeyguardShortcutApps() {
        WindowManagerPolicy.WindowState focusedWindow = this.mDefaultDisplayPolicy.getFocusedWindow();
        if (focusedWindow == null || focusedWindow.getAttrs() == null) {
            Log.e(TAG, "isLsKeyguardShortcutApps, focusedWindow or getAttrs is null");
            return false;
        }
        String focusPackageName = focusedWindow.getAttrs().packageName;
        if (focusPackageName == null) {
            return false;
        }
        int len = this.mLsKeyguardShortcutApps.length;
        for (int i = 0; i < len; i++) {
            if (focusPackageName.startsWith(this.mLsKeyguardShortcutApps[i])) {
                return true;
            }
        }
        return false;
    }

    public void onProximityPositive() {
        Log.i(TAG, "onProximityPositive");
        DefaultHwScreenOnProximityLock defaultHwScreenOnProximityLock = this.mHwScreenOnProximityLock;
        if (defaultHwScreenOnProximityLock != null) {
            defaultHwScreenOnProximityLock.releaseLock(1);
            Log.i(TAG, "HwScreenOnProximityLock quit mistouch view for screen off");
        }
        HwDisplaySidePolicy hwDisplaySidePolicy = this.mDisplaySidePolicy;
        if (hwDisplaySidePolicy != null) {
            hwDisplaySidePolicy.screenTurnedOff(true);
        }
    }

    private boolean isInCallUIAndRinging() {
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        return telecomManager != null && telecomManager.isRinging();
    }

    private boolean isAlarm(int user) {
        ComponentName oldCmpName = ComponentName.unflattenFromString("com.android.deskclock/.alarmclock.AlarmKlaxon");
        ComponentName newCmpName = ComponentName.unflattenFromString("com.huawei.deskclock/.alarmclock.AlarmKlaxon");
        HwActivityManagerService hwAms = (HwActivityManagerService) ServiceManager.getService("activity");
        return hwAms.serviceIsRunning(oldCmpName, user) || hwAms.serviceIsRunning(newCmpName, user);
    }

    private void interceptBackandMenuKey() {
        long now = SystemClock.uptimeMillis();
        if (isScreenInLockTaskMode() && this.mIsBackKeyPress && this.mIsMenuKeyPress && now <= this.mBackKeyPressTime + TOUCH_SPINNING_DELAY_MILLIS && now <= this.mMenuKeyPressTime + TOUCH_SPINNING_DELAY_MILLIS) {
            this.mIsBackKeyPress = false;
            this.mIsMenuKeyPress = false;
            this.mBackKeyPressTime = 0;
            this.mMenuKeyPressTime = 0;
        }
    }

    private boolean isScreenInLockTaskMode() {
        try {
            return ActivityManagerNative.getDefault().isInLockTaskMode();
        } catch (RemoteException e) {
            Log.e(TAG, "isScreenInLockTaskMode  ", e);
            return false;
        }
    }

    public boolean isStatusBarObsecured() {
        return this.mStatuBarObsecured;
    }

    /* access modifiers changed from: package-private */
    public boolean isStatusBarObsecuredByWin(WindowManagerPolicy.WindowState win) {
        if (win == null || this.mDefaultDisplayPolicy.getStatusBar() == null || (win.getAttrs().flags & 16) != 0 || win.toString().contains("hwSingleMode_window")) {
            return false;
        }
        Rect winFrame = win.getFrameLw();
        Rect statusbarFrame = this.mDefaultDisplayPolicy.getStatusBar().getFrameLw();
        if (winFrame.top > statusbarFrame.top || winFrame.bottom < statusbarFrame.bottom || winFrame.left > statusbarFrame.left || winFrame.right < statusbarFrame.right) {
            return false;
        }
        return true;
    }

    public void adjustConfigurationLw(Configuration config, int keyboardPresence, int navigationPresence) {
        HwPhoneWindowManager.super.adjustConfigurationLw(config, keyboardPresence, navigationPresence);
        int tempDpi = config.densityDpi;
        if (tempDpi != this.lastDensityDpi) {
            updateSystemWideConfiguration();
            StylusGestureListener stylusGestureListener = this.mStylusGestureListener;
            if (stylusGestureListener != null) {
                stylusGestureListener.updateConfiguration();
            }
            this.lastDensityDpi = tempDpi;
        }
    }

    private void updateSystemWideConfiguration() {
        Class cls = this.mFsManagerCls;
        if (cls == null || this.mFsManagerObj == null) {
            Log.e(TAG, "updateConfiguration fingersense object load fail");
            return;
        }
        try {
            cls.getMethod("updateConfiguration", new Class[0]).invoke(this.mFsManagerObj, new Object[0]);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "updateSystemWideConfiguration NoSuchMethodException");
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "updateSystemWideConfiguration IllegalAccessException");
        } catch (InvocationTargetException e3) {
            Log.e(TAG, "updateSystemWideConfiguration InvocationTargetException");
        }
    }

    public boolean performHapticFeedbackLw(WindowManagerPolicy.WindowState win, int effectId, boolean isAlways, String reason) {
        String owningPackage;
        int owningUid;
        if (win != null) {
            owningUid = win.getOwningUid();
            owningPackage = win.getOwningPackage();
        } else {
            owningUid = Process.myUid();
            owningPackage = this.mContext.getOpPackageName();
        }
        return HwPhoneWindowManager.super.performHapticFeedback(owningUid, owningPackage, effectId, isAlways, reason);
    }

    public void setNavibarAlignLeftWhenLand(boolean isLeft) {
        this.mIsNavibarAlignLeftWhenLand = isLeft;
    }

    public boolean getNavibarAlignLeftWhenLand() {
        return this.mIsNavibarAlignLeftWhenLand;
    }

    public boolean isPhoneIdle() {
        if (getTelephonyService() != null) {
            try {
                TelephonyManager.getDefault().isMultiSimEnabled();
            } catch (Exception ex) {
                Log.w(TAG, "ITelephony threw RemoteException", ex);
            }
        }
        return false;
    }

    public int getDisabledKeyEventResult(int keyCode) {
        if (keyCode == 3) {
            HwCustPhoneWindowManager hwCustPhoneWindowManager = this.mCust;
            if ((hwCustPhoneWindowManager == null || !hwCustPhoneWindowManager.disableHomeKey(this.mContext)) && !HwDeviceManager.disallowOp(14)) {
                return -2;
            }
            Log.i(TAG, "the device's home key has been disabled for the user.");
            return 0;
        } else if (keyCode != 4) {
            if (keyCode != 187 || !HwDeviceManager.disallowOp(15)) {
                return -2;
            }
            Log.i(TAG, "the device's task key has been disabled for the user.");
            return 0;
        } else if (!HwDeviceManager.disallowOp(16)) {
            return -2;
        } else {
            Log.i(TAG, "the device's back key has been disabled for the user.");
            return -1;
        }
    }

    private int getGameControlKeyReslut(KeyEvent event) {
        if (!IS_GAME_ASSIST || !FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION) {
            return -2;
        }
        int keyCode = event.getKeyCode();
        boolean isGameKeyControlOn = (!this.mIsEnableKeyInCurrentFgGameApp && ActivityManagerEx.isGameKeyControlOn()) || this.mIsLastKeyDownDropped;
        Log.d(TAG, "deviceId:" + event.getDeviceId() + " mFingerPrintId:" + this.mFingerPrintId + " isGameKeyControlOn:" + isGameKeyControlOn + ",EnableKey=" + this.mIsEnableKeyInCurrentFgGameApp + ",mIsLastKeyDownDropped=" + this.mIsLastKeyDownDropped);
        if (!isGameKeyControlOn) {
            return -2;
        }
        if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
            if (this.mTrikeyNaviMode < 0) {
                Log.d(TAG, "trikey single mode.");
                return performGameMode(event, 0);
            }
            Log.d(TAG, "trikey three mode.");
            return performGameMode(event, 1);
        } else if (!FrontFingerPrintSettings.isNaviBarEnabled(this.mResolver)) {
            Log.d(TAG, "single key.");
            return performGameMode(event, 2);
        } else if (keyCode != 3 || event.getDeviceId() != this.mFingerPrintId) {
            return -2;
        } else {
            Log.d(TAG, "NaviBarEnabled KEYCODE_HOME !");
            return performGameMode(event, 3);
        }
    }

    private int performGameMode(KeyEvent event, int naviMode) {
        boolean z = true;
        boolean isSingleKeyMode = naviMode == 0 || naviMode == 2;
        int keyCode = event.getKeyCode();
        long keyTime = event.getEventTime();
        boolean isKeyDown = event.getAction() == 0;
        boolean isInitialDown = isKeyDown && event.getRepeatCount() == 0;
        if (this.mIsLastKeyDownDropped && isInitialDown) {
            this.mIsLastKeyDownDropped = false;
        }
        if (keyCode != 3) {
            if (keyCode != 4) {
                if (keyCode != 187) {
                    return -2;
                }
                if (isSingleKeyMode) {
                    return -1;
                }
                if (isKeyDown) {
                    return -2;
                }
                int result = getClickResult(keyTime, keyCode);
                this.mHandlerEx.removeMessages(MSG_TRIKEY_RECENT_LONG_PRESS);
                return result;
            } else if (isInitialDown) {
                int result2 = getClickResult(keyTime, keyCode);
                if (result2 == -2) {
                    z = false;
                }
                this.mIsLastKeyDownDropped = z;
                return result2;
            } else if (!this.mIsLastKeyDownDropped) {
                return -2;
            } else {
                Log.d(TAG, "drop key up for last event beacause down dropped.");
                if (!isKeyDown) {
                    this.mIsLastKeyDownDropped = false;
                }
                return -1;
            }
        } else if (!isSingleKeyMode && !isKeyDown) {
            return getClickResult(keyTime, keyCode);
        } else {
            return -2;
        }
    }

    private int getClickResult(long eventTime, int keyCode) {
        int result;
        int i;
        if (!this.mIsEnableKeyInCurrentFgGameApp) {
            long j = this.mLastKeyDownTime;
            if (eventTime - j < POWER_SOS_MISTOUCH_THRESHOLD && (i = this.mLastKeyDownKeyCode) == keyCode && j - this.mSecondToLastKeyDownTime < POWER_SOS_MISTOUCH_THRESHOLD && this.mSecondToLastKeyDownKeyCode == i) {
                Log.i(TAG, "Navigation keys unlocked.");
                result = -1;
                this.mIsEnableKeyInCurrentFgGameApp = true;
                showKeyEnableToast();
                Flog.bdReport(991310503);
                this.mSecondToLastKeyDownTime = this.mLastKeyDownTime;
                this.mSecondToLastKeyDownKeyCode = this.mLastKeyDownKeyCode;
                this.mLastKeyDownTime = eventTime;
                this.mLastKeyDownKeyCode = keyCode;
                Log.i(TAG, "getClickResult result:" + result + ",keyCode:" + keyCode + ",EnableKey:" + this.mIsEnableKeyInCurrentFgGameApp);
                return result;
            }
        }
        result = -1;
        this.mSecondToLastKeyDownTime = this.mLastKeyDownTime;
        this.mSecondToLastKeyDownKeyCode = this.mLastKeyDownKeyCode;
        this.mLastKeyDownTime = eventTime;
        this.mLastKeyDownKeyCode = keyCode;
        Log.i(TAG, "getClickResult result:" + result + ",keyCode:" + keyCode + ",EnableKey:" + this.mIsEnableKeyInCurrentFgGameApp);
        return result;
    }

    private void showKeyEnableToast() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass29 */

            @Override // java.lang.Runnable
            public void run() {
                Toast toast = Toast.makeText(HwPhoneWindowManager.this.mContext, 33686257, 0);
                toast.getWindowParams().type = HwArbitrationDEFS.MSG_MPLINK_UNBIND_FAIL;
                toast.getWindowParams().privateFlags |= 16;
                toast.show();
            }
        });
    }

    public void onPointDown() {
        this.mTouchCountPolicy.updateTouchCountInfo();
    }

    public int[] getTouchCountInfo() {
        return this.mTouchCountPolicy.getTouchCountInfo();
    }

    public int[] getDefaultTouchCountInfo() {
        return this.mTouchCountPolicy.getDefaultTouchCountInfo();
    }

    private boolean isTrikeyNaviKeycodeFromLON(boolean isInjected, boolean isExcluded) {
        int frontFpNaviTriKey = FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY;
        Log.d(TAG, "frontFpNaviTriKey:" + frontFpNaviTriKey + " isInjected:" + isInjected + " mTrikeyNaviMode:" + this.mTrikeyNaviMode + " isExcluded:" + isExcluded);
        return frontFpNaviTriKey == 0 || (!isInjected && this.mTrikeyNaviMode < 0) || isExcluded;
    }

    public boolean isSupportCover() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "cover_enabled", 1) != 0;
    }

    public boolean isSmartCoverMode() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "cover_type", 0) == 1;
    }

    public boolean isInCallActivity() {
        String pkgName = getTopActivity();
        return pkgName != null && pkgName.startsWith(HUAWEI_ANDROID_INCALL_UI);
    }

    public boolean isInterceptAndCheckRinging(boolean isIntercept) {
        TelecomManager telecomManager = getTelecommService();
        return isIntercept && (telecomManager == null || !telecomManager.isRinging());
    }

    public int getSingAppKeyEventResult(int keyCode) {
        String packageName = HwDeviceManager.getString(34);
        if (packageName == null || packageName.isEmpty()) {
            return -2;
        }
        boolean[] results = isNeedStartSingleApp(packageName);
        if (keyCode == 3) {
            if (!results[0]) {
                Log.i(TAG, "Single app model running, start the single app's main activity.");
                startSingleApp(packageName);
            }
            return 0;
        } else if (keyCode != 4) {
            return -2;
        } else {
            if (results[0]) {
                return -1;
            }
            if (!results[1]) {
                return -2;
            }
            Log.i(TAG, "Single app model running, start the single app's main activity.");
            startSingleApp(packageName);
            return -1;
        }
    }

    private boolean[] isNeedStartSingleApp(String packageName) {
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        boolean[] results = {false, false};
        if (activityManager != null) {
            try {
                List<ActivityManager.RunningTaskInfo> runningTask = activityManager.getRunningTasks(2);
                if (runningTask != null && runningTask.size() > 0) {
                    ComponentName cn = runningTask.get(0).topActivity;
                    if (cn != null) {
                        String currentAppName = cn.getPackageName();
                        String currentActivityName = cn.getClassName();
                        PackageManager pm = this.mContext.getPackageManager();
                        String mainClassName = pm.getLaunchIntentForPackage(packageName).resolveActivity(pm).getClassName();
                        if (mainClassName != null && mainClassName.equals(currentActivityName) && packageName.equals(currentAppName)) {
                            results[0] = true;
                        }
                    }
                    String nextAppName = null;
                    if (runningTask.size() > 1) {
                        nextAppName = runningTask.get(1).topActivity.getPackageName();
                    }
                    if ((runningTask.get(0).numActivities <= 1 && (runningTask.size() <= 1 || !packageName.equals(nextAppName))) || "com.android.systemui".equals(nextAppName)) {
                        results[1] = true;
                    }
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "isTopApp->RuntimeException happened");
            } catch (Exception e2) {
                Log.e(TAG, "isTopApp->other exception happened");
            }
        }
        return results;
    }

    private void startSingleApp(String packageName) {
        Intent launchIntent = this.mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            this.mContext.startActivity(launchIntent);
        }
    }

    public boolean isKeyguardOccluded() {
        return this.mKeyguardOccluded;
    }

    /* access modifiers changed from: protected */
    public void notifyPowerkeyInteractive(boolean isInteractive) {
        HwPartIawareUtil.notifyPowerkeyInteractive(true);
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        HwPhoneWindowManager.super.dump(prefix, pw, args);
        DefaultGestureNavManager defaultGestureNavManager = this.mGestureNavPolicy;
        if (defaultGestureNavManager != null) {
            defaultGestureNavManager.dump(prefix, pw, args);
        }
    }

    private boolean layoutStatusBarExternal(Rect pf, Rect df, Rect of, Rect vf, Rect dcf, DisplayFrames displayFrames) {
        if (this.mDefaultDisplayPolicy.getStatusBar() == null) {
            return false;
        }
        int i = displayFrames.mUnrestricted.left;
        of.left = i;
        df.left = i;
        pf.left = i;
        int i2 = displayFrames.mUnrestricted.right;
        of.right = i2;
        df.right = i2;
        pf.right = i2;
        int i3 = displayFrames.mUnrestricted.top;
        of.top = i3;
        df.top = i3;
        pf.top = i3;
        int i4 = displayFrames.mUnrestricted.bottom;
        of.bottom = i4;
        df.bottom = i4;
        pf.bottom = i4;
        vf.left = displayFrames.mStable.left;
        vf.top = displayFrames.mStable.top;
        vf.right = displayFrames.mStable.right;
        vf.bottom = displayFrames.mStable.bottom;
        this.mDefaultDisplayPolicy.getStatusBar().computeFrameLw();
        return false;
    }

    private boolean isScreenLocked() {
        KeyguardManager km = (KeyguardManager) this.mContext.getSystemService("keyguard");
        return km != null && km.isKeyguardLocked();
    }

    private boolean handleDesktopKeyEvent(KeyEvent event, WindowManagerPolicy.WindowState win) {
        if (!HwPCUtils.isPcCastModeInServer()) {
            return false;
        }
        int keyCode = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        int displayId = this.mWindowManagerInternal.getFocusedDisplayId();
        if (!(!HwPCUtils.enabledInPad() || keyCode == 40 || keyCode == 117 || keyCode == 118)) {
            this.mIsHomeAndLBothPressed = false;
            this.mIsLDown = false;
        }
        if (win != null) {
            boolean isKeyNaviKeys = keyCode == 19 || keyCode == 20 || keyCode == 21 || keyCode == 22;
            if (!HwPCUtils.isValidExtDisplayId(win.getDisplayId()) && isKeyNaviKeys) {
                this.mHwPWMEx.handleHicarExtraKeys(new KeyEvent(0, 0), win, this.mWindowManagerInternal);
            }
        }
        if (!HwPCUtils.isValidExtDisplayId(displayId) && !HwPCUtils.enabledInPad()) {
            this.isHomePressDown = false;
            this.isHomeAndEBothDown = false;
            this.isHomeAndLBothDown = false;
            this.isHomeAndDBothDown = false;
            this.isHomeAndOtherKeyBothDown = false;
            return false;
        } else if (HwPCUtils.enabledInPad() && handleExclusiveKeykoard(event)) {
            return true;
        } else {
            boolean isActionDown = event.getAction() == 0;
            if (keyCode == 120 && isActionDown && repeatCount == 0) {
                screenshotPc();
                return true;
            } else if (keyCode == 134 && isActionDown && event.isAltPressed() && repeatCount == 0) {
                closeTopWindow();
                return true;
            } else if (keyCode != 33 || !isActionDown || !this.isHomePressDown || repeatCount != 0) {
                if (keyCode == 40) {
                    if (isActionDown && this.isHomePressDown && repeatCount == 0) {
                        this.isHomeAndLBothDown = true;
                        if (HwPCUtils.enabledInPad()) {
                            this.mIsHomeAndLBothPressed = true;
                            this.mIsLDown = true;
                        } else {
                            lockScreen(true);
                        }
                        return true;
                    } else if (HwPCUtils.enabledInPad() && !isActionDown && this.mIsHomeAndLBothPressed) {
                        this.mIsLDown = false;
                        if (!this.isHomePressDown) {
                            HwPCUtils.log(TAG, "will turnOffScreen in DesktopMode");
                            turnOffScreenInDeskMode();
                            this.mIsHomeAndLBothPressed = false;
                        }
                        return true;
                    }
                }
                if (keyCode != 32 || !isActionDown || !this.isHomePressDown || repeatCount != 0) {
                    if (isActionDown && repeatCount == 0 && keyCode == 61) {
                        if (this.mRecentAppsHeldModifiers == 0 && !keyguardOn() && isUserSetupComplete()) {
                            int shiftlessModifiers = event.getModifiers() & -194;
                            if (KeyEvent.metaStateHasModifiers(shiftlessModifiers, 2)) {
                                this.mRecentAppsHeldModifiers = shiftlessModifiers;
                                triggerSwitchTaskView(true);
                                return true;
                            }
                        }
                    } else if (!isActionDown && this.mRecentAppsHeldModifiers != 0 && (event.getMetaState() & this.mRecentAppsHeldModifiers) == 0) {
                        this.mRecentAppsHeldModifiers = 0;
                        triggerSwitchTaskView(false);
                    }
                    if (keyCode != 3 && isActionDown && this.isHomePressDown && repeatCount == 0) {
                        this.isHomeAndOtherKeyBothDown = true;
                    }
                    if ((HwPCUtils.enabledInPad() || keyCode != 3) && keyCode != 117 && keyCode != 118) {
                        return false;
                    }
                    if (!isActionDown) {
                        if (this.isHomeAndEBothDown) {
                            this.isHomeAndEBothDown = false;
                        } else if (this.isHomeAndLBothDown) {
                            this.isHomeAndLBothDown = false;
                        } else if (this.isHomeAndDBothDown) {
                            this.isHomeAndDBothDown = false;
                        } else if (this.isHomeAndOtherKeyBothDown) {
                            this.isHomeAndOtherKeyBothDown = false;
                        } else {
                            showStartMenu();
                        }
                        this.isHomePressDown = false;
                        if (HwPCUtils.enabledInPad() && this.mIsHomeAndLBothPressed && !this.mIsLDown) {
                            HwPCUtils.log(TAG, "the home key up, will turnOffScreen in DesktopMode");
                            turnOffScreenInDeskMode();
                            this.mIsHomeAndLBothPressed = false;
                        }
                    } else {
                        this.isHomePressDown = true;
                    }
                    return true;
                }
                this.isHomeAndDBothDown = true;
                toggleHome();
                return true;
            } else {
                this.isHomeAndEBothDown = true;
                ComponentName componentName = new ComponentName("com.huawei.hidisk", "com.huawei.hidisk.filemanager.FileManager");
                Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
                intent.setFlags(268435456);
                intent.setComponent(componentName);
                this.mContext.createDisplayContext(DisplayManagerGlobal.getInstance().getRealDisplay(displayId)).startActivity(intent);
                return true;
            }
        }
    }

    private boolean handlePadKeyEvent(KeyEvent event) {
        boolean isHandled;
        if (HwPCUtils.isPcCastModeInServer() || isKeyguardLocked()) {
            return false;
        }
        int keyCode = event.getKeyCode();
        boolean isActionDown = event.getAction() == 0;
        int repeatCount = event.getRepeatCount();
        boolean isMetaPressed = event.isMetaPressed();
        if (!KeyEvent.isMetaKey(keyCode)) {
            this.mIsCanBeSearched = false;
            if (!isActionDown || repeatCount != 0) {
                return false;
            }
            if (isMetaPressed && (isHandled = handlePadMetaCombinationKey(keyCode, event))) {
                return isHandled;
            }
            if (keyCode == 61 && event.isAltPressed()) {
                try {
                    this.mWindowManager.setInTouchMode(false);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to set touch mode!");
                }
            }
            return false;
        } else if (isActionDown) {
            this.mIsCanBeSearched = true;
            return false;
        } else if (!this.mIsCanBeSearched) {
            return true;
        } else {
            this.mIsCanBeSearched = false;
            return false;
        }
    }

    private boolean isStatusBarShown() {
        WindowManagerPolicy.WindowState windowState = this.mDefaultDisplayPolicy.getFocusedWindow();
        if (windowState != null && windowState.getAttrs().type == 2000 && windowState.toString().contains("StatusBar")) {
            return true;
        }
        return false;
    }

    private boolean handlePadMetaCombinationKey(int keyCode, KeyEvent event) {
        if (keyCode == 40 && this.mPowerManager != null) {
            this.mPowerManager.goToSleep(SystemClock.uptimeMillis());
            return true;
        } else if (keyCode == 32) {
            if (!isHome() || isStatusBarShown()) {
                launchHomeFromHotKey(event.getDisplayId());
            } else {
                List<ActivityManager.RecentTaskInfo> recentTaskInfos = ((ActivityManager) this.mContext.getSystemService("activity")).getRecentTasks(1, 2);
                if (recentTaskInfos != null && recentTaskInfos.size() > 0) {
                    ActivityManager.RecentTaskInfo info = recentTaskInfos.get(0);
                    Intent intent = new Intent(info.baseIntent);
                    intent.setFlags(270532608);
                    if (info.origActivity != null) {
                        intent.setComponent(info.origActivity);
                    }
                    try {
                        this.mContext.startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        Slog.e(TAG, "cloud not find activity!" + ex);
                    }
                }
            }
            Flog.bdReport(this.mContext, 990203005, "{keycode:meta_d}");
            return true;
        } else if (keyCode != 33) {
            return false;
        } else {
            openFileManagerByShortcutKey();
            Flog.bdReport(this.mContext, 990203005, "{keycode:meta_e}");
            return true;
        }
    }

    private void openFileManagerByShortcutKey() {
        ComponentName componentName;
        int displayId = this.mWindowManagerInternal.getFocusedDisplayId();
        if (EMUI_VERSION < 25) {
            componentName = new ComponentName("com.huawei.hidisk", "com.huawei.hidisk.filemanager.FileManager");
        } else {
            componentName = new ComponentName("com.huawei.filemanager", "com.huawei.hidisk.filemanager.FileManager");
        }
        Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
        intent.setFlags(268435456);
        intent.setComponent(componentName);
        Context context = this.mContext.createDisplayContext(DisplayManagerGlobal.getInstance().getRealDisplay(displayId));
        if (context != null) {
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                Slog.e(TAG, "cloud not find activity!" + ex);
            }
        }
    }

    private boolean isHome() {
        TaskRecordEx taskRecord;
        ActivityRecordEx activityRecord;
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
        if (runningTaskInfos == null || runningTaskInfos.isEmpty()) {
            return false;
        }
        int taskId = runningTaskInfos.get(0).taskId;
        ActivityTaskManagerService atms = this.mWindowManagerInternal.getActivityTaskManagerService();
        if (atms == null) {
            return false;
        }
        ActivityTaskManagerServiceEx atmsEx = new ActivityTaskManagerServiceEx();
        atmsEx.setActivityTaskManagerService(atms);
        RootActivityContainerEx rootActivityContainerEx = atmsEx.getRootActivityContainer();
        if (rootActivityContainerEx == null || (taskRecord = rootActivityContainerEx.anyTaskForId(taskId)) == null || (activityRecord = taskRecord.getTopActivity()) == null) {
            return false;
        }
        return activityRecord.isActivityTypeHome();
    }

    private void turnOffScreenInDeskMode() {
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService(HIVOICE_PRESS_TYPE_POWER);
        if (powerManager != null && powerManager.isScreenOn() && !isScreenLocked()) {
            powerManager.goToSleep(SystemClock.uptimeMillis());
        }
    }

    private boolean handleExclusiveKeykoard(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean isActionDown = event.getAction() == 0;
        int repeatCount = event.getRepeatCount();
        if (keyCode != 3) {
            if (keyCode != 4) {
                if (keyCode == 118) {
                    if (isActionDown) {
                        dispatchKeyEventForExclusiveKeyboard(event);
                    }
                    return true;
                } else if (keyCode != 187) {
                    if (keyCode != 220 && keyCode != 221) {
                        return false;
                    }
                    if (isScreenLocked()) {
                        HwPCUtils.log(TAG, "ScreenLocked! Not handle" + event);
                        return true;
                    }
                    dispatchKeyEventForExclusiveKeyboard(event);
                } else if (isScreenLocked()) {
                    HwPCUtils.log(TAG, "ScreenLocked! Not handle" + event);
                    return true;
                } else {
                    dispatchKeyEventForExclusiveKeyboard(event);
                    return true;
                }
            } else if (isActionDown && repeatCount == 0) {
                dispatchKeyEventForExclusiveKeyboard(event);
                return false;
            }
            return false;
        }
        dispatchKeyEventForExclusiveKeyboard(event);
        return true;
    }

    public void overrideRectForForceRotation(WindowManagerPolicy.WindowState win, Rect pf, Rect df, Rect of, Rect cf, Rect vf, Rect dcf) {
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (forceRotationManager.isForceRotationSupported() && forceRotationManager.isForceRotationSwitchOpen(this.mContext) && win != null && win.getAppToken() != null && win.getAttrs() != null) {
            String winTitle = String.valueOf(win.getAttrs().getTitle());
            if (TextUtils.isEmpty(winTitle) || winTitle.startsWith("SurfaceView") || winTitle.startsWith("PopupWindow")) {
                return;
            }
            if (win.inMultiWindowMode()) {
                Slog.v(TAG, "window is in multiwindow mode");
                return;
            }
            Display defDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            defDisplay.getMetrics(dm);
            if (dm.widthPixels >= dm.heightPixels) {
                Rect tmpRect = new Rect(vf);
                if (forceRotationManager.isAppForceLandRotatable(win.getAttrs().packageName, win.getAppToken().asBinder())) {
                    forceRotationManager.applyForceRotationLayout(win.getAppToken().asBinder(), tmpRect);
                    if (!tmpRect.equals(vf)) {
                        int i = tmpRect.left;
                        vf.left = i;
                        cf.left = i;
                        df.left = i;
                        pf.left = i;
                        dcf.left = i;
                        of.left = i;
                        int i2 = tmpRect.right;
                        vf.right = i2;
                        cf.right = i2;
                        df.right = i2;
                        pf.right = i2;
                        dcf.right = i2;
                        of.right = i2;
                    }
                    win.getAttrs().privateFlags |= 64;
                }
            }
        }
    }

    public void notifyRotationChange(int rotation) {
        DefaultHwScreenOnProximityLock defaultHwScreenOnProximityLock = this.mHwScreenOnProximityLock;
        if (defaultHwScreenOnProximityLock != null) {
            defaultHwScreenOnProximityLock.refreshForRotationChange(rotation);
            if (CoordinationStackDividerManager.getInstance(this.mContext).isVisible() && getDefaultDisplayPolicy() != null) {
                CoordinationStackDividerManager.getInstance(this.mContext).updateDividerView(isLandscape(getDefaultDisplayPolicy().getDisplayRotation()));
            }
        }
        IHwSwingService hwSwing = HwSwingManager.getService();
        if (hwSwing != null) {
            try {
                hwSwing.notifyRotationChange(rotation);
            } catch (RemoteException e) {
                Log.e(TAG, "notifyRotationChange error : " + e.getMessage());
            }
        }
    }

    private boolean isLandscape(int rotation) {
        return rotation == 1 || rotation == 3;
    }

    public void notifyFingerSense(int rotation) {
        Class cls = this.mFsManagerCls;
        if (cls != null && this.mFsManagerObj != null) {
            try {
                cls.getMethod("notifyFingerSense", Integer.TYPE).invoke(this.mFsManagerObj, Integer.valueOf(rotation));
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "notifyFingerSense NoSuchMethodException");
            } catch (IllegalAccessException e2) {
                Log.e(TAG, "notifyFingerSense IllegalAccessException");
            } catch (InvocationTargetException e3) {
                Log.e(TAG, "notifyFingerSense InvocationTargetException");
            }
        }
    }

    public void layoutWindowLwForNotch(WindowManagerPolicy.WindowState win, WindowManager.LayoutParams attrs) {
        if (IS_NOTCH_PROP) {
            int displayRotation = getDefaultDisplayPolicy().getDisplayRotation();
            boolean isNeverMode = false;
            this.mIsAppWindow = attrs.type >= 1 && attrs.type <= 99;
            WindowManagerPolicyEx.WindowStateEx windowStateEx = new WindowManagerPolicyEx.WindowStateEx();
            windowStateEx.setWindowState(win);
            this.mIsNoneNotchAppInHideMode = this.hwNotchScreenWhiteConfig.isNoneNotchAppHideInfo(windowStateEx);
            boolean isSnapshotStartingWindow = win.toString().contains("SnapshotStartingWindow");
            if (!HwDisplaySizeUtil.hasSideInScreen()) {
                notchControlFillet(win);
            }
            if (displayRotation == 0 && !this.mIsNotchSwitchOpen) {
                if ((this.mDefaultDisplayPolicy.getFocusedWindow() != null ? this.hwNotchScreenWhiteConfig.getAppUseNotchMode(this.mDefaultDisplayPolicy.getFocusedWindow().getOwningPackage()) : 0) == 2) {
                    isNeverMode = true;
                }
                setStatusBarColorForNotchMode(isNeverMode, isSnapshotStartingWindow);
            }
        }
    }

    public boolean isWindowNeedLayoutBelowWhenHideNotch() {
        return this.mIsLayoutBelowWhenHideNotch;
    }

    public boolean isWindowNeedLayoutBelowNotch(WindowManagerPolicy.WindowState win) {
        boolean z = false;
        this.mIsLayoutBelowWhenHideNotch = false;
        if (!IS_NOTCH_PROP) {
            return false;
        }
        if (this.mIsNotchSwitchOpen) {
            int displayRotation = getDefaultDisplayPolicy().getDisplayRotation();
            if (displayRotation == 1 || displayRotation == 3) {
                this.mLayoutBelowNotch = !(win.toString().contains("com.qeexo.smartshot.CropActivity") || win.toString().contains("com.huawei.smartshot.CropActivity") || win.toString().contains("com.huawei.ucd.walllpaper1.GLWallpaperService") || win.toString().contains("com.huawei.android.launcher"));
            } else {
                boolean z2 = this.mIsNoneNotchAppInHideMode;
                this.mIsLayoutBelowWhenHideNotch = z2;
                this.mLayoutBelowNotch = z2 || !canLayoutInDisplayCutout(win);
                if (win.toString().contains(CHATMM_PACKAGE_NAME)) {
                    this.mLayoutBelowNotch = true;
                }
            }
        } else {
            this.mLayoutBelowNotch = !canLayoutInDisplayCutout(win);
        }
        if (this.mLayoutBelowNotch && !win.toString().contains("PointerLocation")) {
            z = true;
        }
        this.mLayoutBelowNotch = z;
        return this.mLayoutBelowNotch;
    }

    public boolean canLayoutInDisplayCutout(WindowManagerPolicy.WindowState win) {
        if (!IS_NOTCH_PROP) {
            return true;
        }
        int mode = win != null ? this.hwNotchScreenWhiteConfig.getAppUseNotchMode(win.getOwningPackage()) : 0;
        boolean isNeverMode = mode == 2;
        boolean isAlwaysMode = mode == 1;
        if (isNeverMode) {
            return false;
        }
        if (isAlwaysMode) {
            return true;
        }
        WindowManagerPolicyEx.WindowStateEx windowStateEx = new WindowManagerPolicyEx.WindowStateEx();
        windowStateEx.setWindowState(win);
        if ((this.mIsNotchSwitchOpen || !this.hwNotchScreenWhiteConfig.isNotchAppInfo(windowStateEx)) && ((!this.mIsNotchSwitchOpen || !this.hwNotchScreenWhiteConfig.isNotchAppHideInfo(windowStateEx)) && (win.getAttrs().hwFlags & 65536) == 0 && !win.getHwNotchSupport() && win.getAttrs().layoutInDisplayCutoutMode != 1 && win.getAttrs().layoutInDisplayCutoutMode != 3)) {
            return false;
        }
        return true;
    }

    private void setStatusBarColorForNotchMode(boolean isNeverMode, boolean isSnapshotStartingWindow) {
        if (!isSnapshotStartingWindow && this.mIsAppWindow && isNeverMode && !this.mIsForceSetStatusBar) {
            notchStatusBarColorUpdate(1);
            this.mIsForceSetStatusBar = true;
            this.mIsRestoreStatusBar = false;
        } else if (!isSnapshotStartingWindow && this.mIsAppWindow && !this.mIsNotchSwitchOpen && !this.mIsRestoreStatusBar && !isNeverMode) {
            this.mIsForceSetStatusBar = false;
            this.mIsRestoreStatusBar = true;
            notchStatusBarColorUpdate(0);
        }
    }

    private void hideNotchRoundCorner() {
        this.mIsFirstSetCornerInLandNoNotch = false;
        this.mIsFirstSetCornerInLandNotch = true;
        this.mHwPWMEx.setIntersectCutoutForNotch(false);
        transferSwitchStatusToSurfaceFlinger(0);
    }

    private void showNotchRoundCorner() {
        this.mIsFirstSetCornerInLandNotch = false;
        this.mIsFirstSetCornerInLandNoNotch = true;
        this.mHwPWMEx.setIntersectCutoutForNotch(true);
        transferSwitchStatusToSurfaceFlinger(1);
    }

    private boolean shouldSkipSpecialWindow(WindowManagerPolicy.WindowState win) {
        for (String packageName : this.mSpecialPackage) {
            if (packageName.equals(win.getOwningPackage())) {
                return true;
            }
        }
        for (Integer num : this.mSpecialWindowType) {
            if (num.intValue() == win.getAttrs().type) {
                return true;
            }
        }
        CharSequence title = win.getAttrs().getTitle();
        if (title == null || title.toString() == null || !title.toString().startsWith("PopupWindow:")) {
            return false;
        }
        return true;
    }

    private boolean shouldPreventNotchFilletForSideScreen(WindowManagerPolicy.WindowState win) {
        if (!shouldSkipSpecialWindow(win)) {
            return this.mWindowManagerInternal.isNeedLandAni();
        }
        Slog.i(TAG, "shouldPreventNotchFilletForSideScreen win = " + win + " is special window, do prevent");
        return true;
    }

    public void setNotchRoundCornerVisibility(boolean isVisibility) {
        this.mIsNeedHide = isVisibility;
        if (HwDisplaySizeUtil.hasSideInScreen()) {
            if (this.mIsNeedHide) {
                transferSwitchStatusToSurfaceFlingerForSideScreen(0, 1, getFocusedWindow());
            } else {
                notchControlFilletForSideScreen(getFocusedWindow(), false);
            }
        } else if (this.mIsNotchSwitchOpen) {
            if (this.mIsNeedHide) {
                transferSwitchStatusToSurfaceFlinger(0);
            } else {
                transferSwitchStatusToSurfaceFlinger(1);
            }
        }
        Slog.i(TAG, "setNotchRoundCornerVisibility isVisibility " + isVisibility + " mIsNotchSwitchOpen " + this.mIsNotchSwitchOpen);
    }

    public void notchControlFilletForSideScreenEx(WindowStateEx winEx, boolean isForced) {
        notchControlFilletForSideScreen(winEx == null ? null : winEx.getWindowState(), isForced);
    }

    public void notchControlFilletForSideScreen(WindowManagerPolicy.WindowState win, boolean isForced) {
        int i;
        if (win != null && win.getDisplayId() <= 0 && !this.mIsNeedHide) {
            if (this.mIsSkipUpdateSideAndCorner) {
                this.mIsSkipUpdateSideAndCorner = false;
                Slog.i(TAG, "skip update side and corner");
                return;
            }
            int lazyMode = this.mWindowManagerInternal.getLazyMode();
            int displayRotation = this.mDefaultDisplayPolicy.getRealTimeRotation();
            int sideMode = 1;
            if (lazyMode != 0) {
                sideMode = 2;
            } else if ("com.huawei.android.extdisplay".equals(win.getOwningPackage()) && win.getAttrs().type == 2003) {
                sideMode = 2;
            } else if (!isForced && shouldPreventNotchFilletForSideScreen(win)) {
                return;
            } else {
                if (win instanceof WindowState) {
                    if (sSpecialWindowingModes.contains(Integer.valueOf(win.getWindowingMode()))) {
                        win = this.mWindowManagerInternal.findVisibleUnfloatingModeWindow((WindowState) win);
                        if (win == null) {
                            return;
                        }
                    } else if (!sSpecialWindowType.contains(Integer.valueOf(win.getAttrs().type))) {
                        if (2000 == win.getAttrs().type && !isKeyguardShowingAndNotOccluded()) {
                            return;
                        }
                    } else {
                        return;
                    }
                    IHwDisplayPolicyEx iHwDisplayPolicyEx = this.mDefaultDisplayPolicy.getHwDisplayPolicyEx();
                    if (iHwDisplayPolicyEx != null && (win instanceof WindowState)) {
                        WindowState windowState = (WindowState) win;
                        windowState.mIsNeedExceptDisplaySide = iHwDisplayPolicyEx.isNeedExceptDisplaySide(win.getAttrs(), windowState, displayRotation);
                        if (windowState.mIsNeedExceptDisplaySide) {
                            i = 2;
                        } else {
                            i = 1;
                        }
                        sideMode = i;
                    }
                }
            }
            transferSwitchStatusToSurfaceFlingerForSideScreen(detectNotchMode(win, displayRotation, lazyMode), sideMode, win);
        }
    }

    private int detectNotchMode(WindowManagerPolicy.WindowState win, int displayRotation, int lazyMode) {
        if (!this.mIsNotchSwitchOpen) {
            if (displayRotation != 1 && displayRotation != 3) {
                return 0;
            }
            if (canLayoutInDisplayCutout(win)) {
                this.mHwPWMEx.setIntersectCutoutForNotch(false);
                return 0;
            }
            this.mHwPWMEx.setIntersectCutoutForNotch(true);
            return 1;
        } else if (displayRotation == 2 || lazyMode != 0) {
            return 0;
        } else {
            if (win.getAttrs().type != 2000 || !win.toString().contains("StatusBar") || isKeyguardLocked()) {
                return 1;
            }
            return 0;
        }
    }

    private void transferSwitchStatusToSurfaceFlingerForSideScreen(int notchMode, int sideMode, WindowManagerPolicy.WindowState win) {
        int i = 0;
        if (sideMode == 1) {
            this.mWindowManagerInternal.setScreenSideBoxVisibility(win != null ? win.getDisplayId() : 0, false);
        } else if (sideMode == 2) {
            WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
            if (win != null) {
                i = win.getDisplayId();
            }
            windowManagerInternal.setScreenSideBoxVisibility(i, true);
        }
        this.mCurrenSideMode = sideMode;
        this.mCurrentNotchMode = notchMode;
        Parcel dataIn = Parcel.obtain();
        int val = (notchMode & 3) | ((sideMode << 2) & 12);
        try {
            Slog.i(TAG, "StatusToSurfaceFlinger " + win + " sideMode = " + sideMode + " notchMode = " + notchMode + " dataIn val = " + val);
            dataIn.writeInt(val);
            IBinder sfBinder = ServiceManager.getService("SurfaceFlinger");
            if (sfBinder != null && !sfBinder.transact(NOTCH_ROUND_CORNER_CODE, dataIn, null, 1)) {
                Slog.d(TAG, "StatusToSurfaceFlinger error!");
            }
        } catch (RemoteException e) {
            Slog.d(TAG, "StatusToSurfaceFlinger RemoteException");
        } catch (Throwable th) {
            dataIn.recycle();
            throw th;
        }
        dataIn.recycle();
    }

    public void setScreenSideBoxAndCornerVisibility(int displayId, boolean isVisible) {
        if (displayId == 0 && HwDisplaySizeUtil.hasSideInScreen()) {
            int sideMode = isVisible ? 2 : 1;
            if (sideMode != this.mCurrenSideMode) {
                transferSwitchStatusToSurfaceFlingerForSideScreen(this.mCurrentNotchMode, sideMode, null);
            }
        }
    }

    private void handleNotchRoundCorner(WindowManagerPolicy.WindowState win) {
        boolean isFlagGroup = true;
        this.mIsFirstSetCornerInPort = true;
        boolean isSplashScreen = win.toString().contains("Splash Screen");
        boolean isContainsWorkSpace = win.toString().contains("com.huawei.intelligent.Workspace");
        boolean isNotchSupport = canLayoutInDisplayCutout(win);
        boolean isFlagGroup2 = this.mIsAppWindow && (this.mDefaultDisplayPolicy.getFocusedWindow() != null && this.mDefaultDisplayPolicy.getFocusedWindow().toString().equals(win.toString())) && !isSplashScreen;
        if (win.inMultiWindowMode()) {
            if (isSplashScreen || !this.mIsAppWindow) {
                isFlagGroup = false;
            }
            if (win.getWindowingMode() != 3) {
                return;
            }
            if (isFlagGroup && isNotchSupport && this.mIsFirstSetCornerInLandNoNotch) {
                hideNotchRoundCorner();
            } else if (isFlagGroup && !isNotchSupport && this.mIsFirstSetCornerInLandNotch) {
                showNotchRoundCorner();
            }
        } else if (isFlagGroup2 && isNotchSupport && this.mIsFirstSetCornerInLandNoNotch) {
            hideNotchRoundCorner();
        } else if (isFlagGroup2 && !isNotchSupport && this.mIsFirstSetCornerInLandNotch) {
            if (this.mIsFirstSetCornerInLandNoNotch || !isContainsWorkSpace) {
                showNotchRoundCorner();
            }
        }
    }

    private void notchControlFillet(WindowManagerPolicy.WindowState win) {
        int displayRotation = getDefaultDisplayPolicy().getDisplayRotation();
        if (!this.mIsNotchSwitchOpen) {
            if (displayRotation == 1 || displayRotation == 3) {
                handleNotchRoundCorner(win);
                return;
            }
            this.mIsFirstSetCornerInLandNoNotch = true;
            this.mIsFirstSetCornerInLandNotch = true;
            if (this.mIsFirstSetCornerInPort) {
                this.mIsFirstSetCornerInPort = false;
                transferSwitchStatusToSurfaceFlinger(0);
            }
        } else if (displayRotation == 2) {
            boolean isSplashScreen = win.toString().contains("Splash Screen");
            boolean isTopWindow = this.mDefaultDisplayPolicy.getFocusedWindow() != null && this.mDefaultDisplayPolicy.getFocusedWindow().toString().equals(win.toString());
            if (!isSplashScreen && this.mIsAppWindow && this.mIsFirstSetCornerInReversePortait && isTopWindow) {
                this.mIsFirstSetCornerInReversePortait = false;
                this.mIsFirstSetCornerDefault = true;
                transferSwitchStatusToSurfaceFlinger(0);
            }
        } else if (this.mIsFirstSetCornerDefault) {
            this.mIsFirstSetCornerInReversePortait = true;
            this.mIsFirstSetCornerDefault = false;
            transferSwitchStatusToSurfaceFlinger(1);
        }
    }

    private void transferSwitchStatusToSurfaceFlinger(int notchMode) {
        Slog.d(TAG, "Window issued fillet display notchMode = " + notchMode + ", mIsNotchSwitchOpen = " + this.mIsNotchSwitchOpen + ", mDisplayRotation = " + getDefaultDisplayPolicy().getDisplayRotation());
        Parcel dataIn = Parcel.obtain();
        try {
            dataIn.writeInt(notchMode);
            IBinder sfBinder = ServiceManager.getService("SurfaceFlinger");
            if (sfBinder != null && !sfBinder.transact(NOTCH_ROUND_CORNER_CODE, dataIn, null, 1)) {
                Slog.d(TAG, "transferSwitchStatusToSurfaceFlinger error!");
            }
        } catch (RemoteException e) {
            Slog.d(TAG, "transferSwitchStatusToSurfaceFlinger RemoteException on notify screen rotation animation end");
        } catch (Throwable th) {
            dataIn.recycle();
            throw th;
        }
        dataIn.recycle();
    }

    public void layoutWindowForPadPCMode(WindowManagerPolicy.WindowState win, Rect pf, Rect df, Rect cf, Rect vf, int mContentBottom) {
    }

    public void setSwitchingUser(boolean isSwitchingUser) {
        if (isSwitchingUser) {
            Slog.d(TAG, "face_rotation: switchUser unbindIntelliService");
            IntelliServiceManager.getInstance(this.mContext).unbindIntelliService();
            SubScreenViewEntry subScreenViewEntry = this.mSubScreenViewEntry;
            if (subScreenViewEntry != null) {
                subScreenViewEntry.handleSwitchingUserForRemoteViews();
            }
        }
        HwPhoneWindowManager.super.setSwitchingUser(isSwitchingUser);
    }

    private boolean isRightNotchState() {
        WindowManagerPolicy.WindowState focusedWindow = getDefaultDisplayPolicy().getFocusedWindow();
        boolean isTopWindowLauncher = getDefaultDisplayPolicy().getTopFullscreenOpaqueWindowState().toString().contains("com.huawei.android.launcher");
        return !isTopWindowLauncher || (focusedWindow == null && isTopWindowLauncher);
    }

    private boolean isWhiteFocusedWindow() {
        WindowManagerPolicy.WindowState focusedWindow = getDefaultDisplayPolicy().getFocusedWindow();
        String focusedWindowString = !(focusedWindow == null) ? focusedWindow.toString() : "";
        return focusedWindowString.contains("HwGlobalActions") || focusedWindowString.contains("Sys2023:dream");
    }

    public boolean getForceNotchStatusBar() {
        return getDefaultDisplayPolicy().getForceNotchStatusBar();
    }

    public void setForceNotchStatusBar(boolean isForceNotchStatusBar) {
        getDefaultDisplayPolicy().setForceNotchStatusBar(isForceNotchStatusBar);
    }

    private boolean isPowerOffScreen() {
        WindowManagerPolicy.WindowState topFullOpaqueWin = getDefaultDisplayPolicy().getTopFullscreenOpaqueWindowState();
        WindowManagerPolicy.WindowState focusedWindow = getDefaultDisplayPolicy().getFocusedWindow();
        if (topFullOpaqueWin == null || focusedWindow == null) {
            return false;
        }
        boolean isLauncherWin = topFullOpaqueWin.toString().contains("com.huawei.android.launcher");
        boolean isPowerOffWin = focusedWindow.toString().contains("HwGlobalActions");
        if (!isLauncherWin || !isPowerOffWin || !this.mIsNotchSwitchOpen) {
            return false;
        }
        return true;
    }

    public boolean hideNotchStatusBar(int fl) {
        boolean isHideNotchStatusBar = true;
        this.mBarVisibility = 1;
        WindowManagerPolicy.WindowState focusedWindow = getDefaultDisplayPolicy().getFocusedWindow();
        int lastSystemUiFlags = getDefaultDisplayPolicy().getLastSystemUiFlags();
        boolean isNotchSupport = focusedWindow != null && canLayoutInDisplayCutout(focusedWindow);
        int displayRotation = getDefaultDisplayPolicy().getDisplayRotation();
        if (!IS_NOTCH_PROP || displayRotation != 0) {
            return true;
        }
        if (focusedWindow != null && focusedWindow.toString().contains(HwWmConstants.INTELLIGENT_PKG_NAME)) {
            return true;
        }
        if (isPowerOffScreen()) {
            setForceNotchStatusBar(true);
            this.mBarVisibility = 0;
            return false;
        } else if (!this.mIsNotchSwitchOpen || !isRightNotchState()) {
            WindowManagerPolicy.WindowState topFullscreenOpaqueWindowState = getDefaultDisplayPolicy().getTopFullscreenOpaqueWindowState();
            WindowManagerPolicyEx.WindowStateEx windowStateEx = new WindowManagerPolicyEx.WindowStateEx();
            windowStateEx.setWindowState(focusedWindow);
            if (isNotchSupport) {
                return true;
            }
            if (focusedWindow != null && (this.hwNotchScreenWhiteConfig.isNotchAppInfo(windowStateEx) || focusedWindow.toString().contains("com.huawei.android.launcher"))) {
                return true;
            }
            if (focusedWindow != null && (this.hwNotchScreenWhiteConfig.isNoneNotchAppWithStatusbarInfo(windowStateEx) || (((focusedWindow.getAttrs().hwFlags & 32768) != 0 && ((fl & 1024) != 0 || (lastSystemUiFlags & 4) != 0)) || (getForceNotchStatusBar() && (focusedWindow.toString().contains("SearchPanel") || (!topFullscreenOpaqueWindowState.toString().contains("Splash Screen") && !topFullscreenOpaqueWindowState.toString().equals(focusedWindow.toString()))))))) {
                setForceNotchStatusBar(true);
                isHideNotchStatusBar = false;
                this.mBarVisibility = 0;
            } else if (getForceNotchStatusBar() && focusedWindow != null && focusedWindow.getAttrs().type == 2 && ((getDefaultDisplayPolicy().getWindowFlags((WindowState) null, focusedWindow.getAttrs()) & 1024) != 0 || ((focusedWindow.getAttrs().hwFlags & 32768) != 0 && (lastSystemUiFlags & 1024) != 0))) {
                setForceNotchStatusBar(true);
                isHideNotchStatusBar = false;
                this.mBarVisibility = 0;
            } else if ((focusedWindow == null && getForceNotchStatusBar() && (!((fl & 1024) == 0 && (lastSystemUiFlags & 4) == 0) && !topFullscreenOpaqueWindowState.toString().contains("Splash Screen"))) || !(topFullscreenOpaqueWindowState.getAttrs().type == 3 || (fl & 1024) == 0 || !topFullscreenOpaqueWindowState.toString().contains("Splash Screen"))) {
                setForceNotchStatusBar(true);
                isHideNotchStatusBar = false;
                this.mBarVisibility = 0;
            }
            if (!(isKeyguardShowingOrOccluded() || (this.mKeyguardDelegate != null && this.mKeyguardDelegate.isOccluded()))) {
                return isHideNotchStatusBar;
            }
            setForceNotchStatusBar(false);
            this.mBarVisibility = 1;
            return true;
        } else {
            boolean isStatusBarFocused = (focusedWindow == null || (getDefaultDisplayPolicy().getWindowFlags(null, focusedWindow.getAttrs()) & 2048) == 0) ? false : true;
            if ((((fl & 1024) == 0 && (lastSystemUiFlags & 4) == 0) ? false : true) || isWhiteFocusedWindow()) {
                isHideNotchStatusBar = false;
                setForceNotchStatusBar(getForceNotchStatusBar() || !isStatusBarFocused);
                this.mBarVisibility = 0;
            }
            if (!getForceNotchStatusBar() || !isStatusBarFocused) {
                return isHideNotchStatusBar;
            }
            this.mBarVisibility = 1;
            setForceNotchStatusBar(false);
            getDefaultDisplayPolicy().setNotchStatusBarColorLw(0);
            notchStatusBarColorUpdate(1);
            return false;
        }
    }

    public void notchStatusBarColorUpdate(int statusbarStateFlag) {
        if (!this.mIsForceSetStatusBar) {
            if (this.mDefaultDisplayPolicy.getFocusedWindow() != null) {
                WindowManager.LayoutParams attrs = this.mDefaultDisplayPolicy.getFocusedWindow().getAttrs();
                this.mLastNavigationBarColor = attrs.navigationBarColor;
                this.mLastStatusBarColor = attrs.statusBarColor;
                this.mLastIsEmuiStyle = getEmuiStyleValue(attrs.isEmuiStyle);
            }
            notchTransactToStatusBarService(121, "notchTransactToStatusBarService", this.mLastIsEmuiStyle, this.mLastStatusBarColor, this.mLastNavigationBarColor, -1, statusbarStateFlag, this.mBarVisibility);
        }
    }

    /* access modifiers changed from: protected */
    public void wakeUpFromPowerKey(long eventTime) {
        doFaceRecognize(true, "FCDT-POWERKEY");
        HwPhoneWindowManager.super.wakeUpFromPowerKey(eventTime);
    }

    public void doFaceRecognize(boolean isDetect, String reason) {
        if (this.mKeyguardDelegate != null) {
            FaceReportEventToIaware.reportEventToIaware(this.mContext, 20025);
            this.mKeyguardDelegate.doFaceRecognize(isDetect, reason);
        }
    }

    public void notchTransactToStatusBarService(int code, String transactName, int isEmuiStyle, int statusbarColor, int navigationBarColor, int isEmuiLightStyle, int statusbarStateFlag, int barVisibility) {
        IBinder statusBarServiceBinder;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (!(getHWStatusBarService() == null || (statusBarServiceBinder = getHWStatusBarService().asBinder()) == null)) {
                Log.d(TAG, "set statusbarColor:" + statusbarColor + ", barVisibility: " + barVisibility + " to status bar service");
                data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                data.writeInt(isEmuiStyle);
                data.writeInt(statusbarColor);
                data.writeInt(navigationBarColor);
                data.writeInt(isEmuiLightStyle);
                data.writeInt(statusbarStateFlag);
                data.writeInt(barVisibility);
                statusBarServiceBinder.transact(code, data, reply, 0);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "notchTransactToStatusBarService->threw remote exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    private boolean handleInputEventInPCCastMode(KeyEvent event) {
        WindowManagerPolicy.WindowState windowState;
        IHwPCManager pcManager;
        if (HwPCUtils.isPcCastModeInServer() && (windowState = this.mLighterDrawView) != null && windowState.isVisibleLw() && (pcManager = HwPCUtils.getHwPCManager()) != null) {
            try {
                return pcManager.shouldInterceptInputEvent(event, false);
            } catch (RemoteException e) {
                Log.e(TAG, "interceptInputEventInPCCastMode()");
            }
        }
        return false;
    }

    public void setPowerState(int powerState) {
        HwAodManager.getInstance().setPowerState(powerState);
    }

    private boolean isInScreenFingerprint(int type) {
        return type == 1 || type == 2;
    }

    private void pauseAOD() {
        if (this.mDeviceNodeFD > 0) {
            HwAodManager.getInstance().pause();
        }
    }

    private int getDeviceNodeFD() {
        return HwAodManager.getInstance().getDeviceNodeFD();
    }

    public void setAodState(final int aodState) {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.policy.HwPhoneWindowManager.AnonymousClass30 */

            @Override // java.lang.Runnable
            public void run() {
                HwPhoneWindowManager.this.startAodService(aodState);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startAodService(int aodState) {
        Slog.i("AOD_HwPWM", "startAodService mAodSwitch=" + this.mAodSwitch + " mAODState=" + this.mAODState + " aodState=" + aodState + " mIsFingerprintEnabledBySettings=" + this.mIsFingerprintEnabledBySettings + " mFingerprintType=" + this.mFingerprintType);
        if (!this.mIsActuralShutDown || !(aodState == 101 || aodState == 103)) {
            if (this.mFingerprintType == -1) {
                this.mFingerprintType = getHardwareType();
            }
            if (aodState == 101 || aodState == 103) {
                if (this.mDeviceNodeFD == -2147483647) {
                    this.mDeviceNodeFD = getDeviceNodeFD();
                }
                setLmtSwitch();
            }
            if (this.mAodSwitch == 0) {
                boolean isFpEnable = false;
                if (isInScreenFingerprint(this.mFingerprintType) && this.mIsFingerprintEnabledBySettings) {
                    Slog.i("AOD_HwPWM", "startAodService fp enabled");
                    isFpEnable = true;
                }
                boolean isAudioStreamEnable = false;
                if (!TextUtils.isEmpty(RING_VOLUMEBAR_DISP)) {
                    Slog.i("AOD_HwPWM", "startAodService audio enabled");
                    isAudioStreamEnable = true;
                }
                if (!isFpEnable && !isAudioStreamEnable && !IS_SUPPORT_FOLD_SCREEN) {
                    return;
                }
            }
            if (this.mAODState == aodState) {
                Slog.i("AOD_HwPWM", "handleAodState mAODState equal, state =" + aodState);
                return;
            }
            this.mAODState = aodState;
            handleAodState(aodState);
            Slog.w("AOD_HwPWM", " startAodService end success.");
        }
    }

    private void handleAodState(int aodState) {
        Intent intent = null;
        Slog.w("AOD_HwPWM", " handleAodState aodState:" + aodState);
        switch (aodState) {
            case 100:
            case 102:
                setPowerState(aodState);
                pauseAOD();
                intent = new Intent(AOD_WAKE_UP_ACTION);
                break;
            case 101:
                setPowerState(10);
            case 103:
                setPowerState(101);
                intent = new Intent(AOD_GOING_TO_SLEEP_ACTION);
                break;
        }
        if (intent != null) {
            intent.setComponent(new ComponentName("com.huawei.aod", "com.huawei.aod.AODService"));
            this.mContext.startService(intent);
        }
    }

    private void setLmtSwitch() {
        if (!isSupportLmtDisplay()) {
            Slog.i(TAG, "AOD does not support lmt display ");
            return;
        }
        boolean isFpEnable = isInScreenFingerprint(this.mFingerprintType) && this.mIsFingerprintEnabledBySettings;
        if (this.mAodSwitch != 0 || isFpEnable) {
            connectToProxy();
            if (isConfigMatchMode(Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "aod_display_type", 0, ActivityManager.getCurrentUser()), 2) || isSupportAodAnimationPlay()) {
                setAodTouchState(1);
            } else {
                setAodTouchState(0);
            }
        } else {
            Slog.i(TAG, "AOD not display, finger not enable, return.");
            setAodTouchState(0);
        }
    }

    private boolean isSupportAodAnimationPlay() {
        boolean isAnimationable = false;
        try {
            Slog.i(TAG, "mDecodeAnalogImageResourceRunnable with uid : " + ActivityManager.getCurrentUser());
            SharedPreferences spf = this.mContext.createPackageContext("com.huawei.aod", 0).getSharedPreferences("com.huawei.aod.theme_preferences", 4);
            if (spf == null) {
                Slog.w(TAG, "isSupportAnimationPlay return false for sharedPreferences invalid");
                return false;
            }
            isAnimationable = spf.getBoolean("is_animationable", false);
            Slog.i(TAG, "isSupportAnimationPlay return:" + isAnimationable);
            return isAnimationable;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.w(TAG, "package not exist " + e.toString());
        }
    }

    private boolean isSupportLmtDisplay() {
        int aodTouchTime;
        int displayModeType;
        int displayModeType2;
        String[] strArr = LMT_CONFIGS;
        if (strArr.length != 3 && strArr.length != 4) {
            return false;
        }
        try {
            displayModeType2 = Integer.parseInt(LMT_CONFIGS[0]);
            displayModeType = Integer.parseInt(LMT_CONFIGS[1]);
            aodTouchTime = Integer.parseInt(LMT_CONFIGS[2]);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "hw_mc.aod.support_display_style contains invalid item.");
            aodTouchTime = 0;
            displayModeType2 = 0;
            displayModeType = 0;
        }
        if ((isConfigMatchMode(displayModeType2, 1) || isConfigMatchMode(displayModeType2, 2)) && displayModeType > 0 && aodTouchTime > 0) {
            return true;
        }
        return false;
    }

    private boolean isConfigMatchMode(int displayModeType, int mode) {
        return (displayModeType & mode) == mode;
    }

    private void setAodTouchState(int state) {
        ITouchscreen iTouchscreen = this.mTpTouchSwitch;
        if (iTouchscreen == null) {
            Slog.e(TAG, "touch service not available");
            return;
        }
        try {
            int result = iTouchscreen.hwSetFeatureConfig(6, Integer.toString(state));
            Slog.d(TAG, "finish hwSetFeatureConfig with state: " + state + " stateString: " + state + " result: " + result);
        } catch (RemoteException e) {
            Log.e(TAG, "setAodTouchState RemoteException");
        }
    }

    public int getHardwareType() {
        return FingerprintManagerEx.getHardwareType();
    }

    public boolean isWindowSupportKnuckle() {
        if (getFocusedWindow() == null || (getFocusedWindow().getAttrs().flags & HwWindowManager.LayoutParams.FLAG_DISABLE_KNUCKLE_TO_LAUNCH_APP) == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void uploadKeyEvent(int keyEvent) {
        if (this.mContext != null) {
            if (keyEvent == 3) {
                Flog.bdReport(991310173);
            } else if (keyEvent == 4) {
                Flog.bdReport(991310172);
            }
        }
    }

    public boolean isHwStartWindowEnabled(int type) {
        if (type == 0) {
            return HwPartIawareUtil.isStartWindowEnable();
        }
        return false;
    }

    public Context addHwStartWindow(ApplicationInfo appInfo, Context overrideContext, Context context, TypedArray typedArray, int windowFlags) {
        if (overrideContext == null || context == null || typedArray == null || appInfo == null) {
            return null;
        }
        boolean isHwStartWindowFlag = false;
        boolean isWindowIsTranslucent = typedArray.getBoolean(5, false);
        boolean isWindowDisableStarting = typedArray.getBoolean(12, false);
        boolean isWindowShowWallpaper = typedArray.getBoolean(14, false);
        if ((isWindowDisableStarting || isWindowIsTranslucent || (isWindowShowWallpaper && (windowFlags & 1048576) != 1048576)) && HwStartWindowRecord.getInstance().checkStartWindowApp(Integer.valueOf(appInfo.uid))) {
            isHwStartWindowFlag = true;
        }
        if (!isHwStartWindowFlag) {
            return null;
        }
        overrideContext.setTheme(overrideContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.NoActionBar", null, null));
        Slog.d(TAG, "addHwStartWindow set default on app : " + appInfo.packageName);
        return overrideContext;
    }

    public boolean isNotchDisplayDisabled() {
        return this.mIsNotchSwitchOpen;
    }

    public boolean getWindowLayoutBelowNotch() {
        return this.mLayoutBelowNotch;
    }

    /* access modifiers changed from: protected */
    public void cancelPendingPowerKeyAction() {
        HwPhoneWindowManager.super.cancelPendingPowerKeyAction();
        cancelAIPowerLongPressed();
    }

    private void setProximitySensorEnabled(boolean isEnable) {
        if (IS_OPEN_PROXIMITY_DISPALY) {
            if (this.mSensorManager == null) {
                this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            }
            if (isEnable) {
                if (!this.mIsProximitySensorEnabled) {
                    this.mIsProximitySensorEnabled = true;
                    SensorManager sensorManager = this.mSensorManager;
                    sensorManager.registerListener(this.mProximitySensorListener, sensorManager.getDefaultSensor(8), 3);
                }
            } else if (this.mIsProximitySensorEnabled) {
                this.mIsProximitySensorEnabled = false;
                this.mSensorManager.unregisterListener(this.mProximitySensorListener);
            }
        }
    }

    private boolean isScreenTurnedOn() {
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService(HIVOICE_PRESS_TYPE_POWER);
        if (powerManager == null) {
            return true;
        }
        Log.i(TAG, "isScreenTurnedOn " + powerManager.isScreenOn());
        return powerManager.isScreenOn();
    }

    public boolean isSideTouchVolumeKey(KeyEvent event, boolean isInjected) {
        HwDisplaySidePolicy hwDisplaySidePolicy = this.mDisplaySidePolicy;
        return hwDisplaySidePolicy != null && hwDisplaySidePolicy.isSideTouchEvent(event, isInjected);
    }

    public void notifyVolumePanelStatus(boolean isVolumePanelVisible) {
        HwDisplaySidePolicy hwDisplaySidePolicy = this.mDisplaySidePolicy;
        if (hwDisplaySidePolicy != null) {
            hwDisplaySidePolicy.notifyVolumePanelStatus(isVolumePanelVisible);
        }
    }

    public int checkActionResult(KeyEvent event, boolean isInjected, boolean isScreenOn, boolean isKeyguardActive, int result) {
        HwDisplaySidePolicy hwDisplaySidePolicy = this.mDisplaySidePolicy;
        if (hwDisplaySidePolicy == null || !hwDisplaySidePolicy.shouldSendToSystemMediaSession(event, isInjected, isScreenOn, isKeyguardActive)) {
            return result;
        }
        return result & -2;
    }

    public boolean isMusicOnly(boolean isScreenOn) {
        HwDisplaySidePolicy hwDisplaySidePolicy = this.mDisplaySidePolicy;
        if (hwDisplaySidePolicy != null) {
            return hwDisplaySidePolicy.isMusicOnly(isScreenOn);
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAcquireProximityLock() {
        if (this.mHwScreenOnProximityLock == null) {
            return false;
        }
        boolean isModeEnabled = true;
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_TOUCH_DISABLE_MODE, 1, ActivityManager.getCurrentUser()) <= 0 || "factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            isModeEnabled = false;
        }
        Log.i(TAG, "touch disable mode : " + isModeEnabled);
        if (!isModeEnabled || !this.mIsDeviceProvisioned) {
            return false;
        }
        boolean isPhoneCallState = checkPhoneOFFHOOK();
        if (!isPhoneCallState || (isPhoneCallState && checkHeadSetIsConnected())) {
            return true;
        }
        return false;
    }

    public void setDisplayMode(int mode) {
        this.mFoldDisplayMode = mode;
        DefaultHwScreenOnProximityLock defaultHwScreenOnProximityLock = this.mHwScreenOnProximityLock;
        if (defaultHwScreenOnProximityLock != null) {
            if (mode == 3) {
                defaultHwScreenOnProximityLock.releaseLock(0);
                Log.i(TAG, "HwScreenOnProximityLock quit mistouch view for fold display sub mode");
            } else if (isAcquireProximityLock() && isKeyguardLocked()) {
                if (isScreenTurnedOn()) {
                    WindowManagerPolicyEx policyEx = new WindowManagerPolicyEx();
                    policyEx.setWindowManagerPolicy(this);
                    this.mHwScreenOnProximityLock.acquireLock(policyEx, mode);
                }
                this.mHwScreenOnProximityLock.forceRefreshHintView();
                Log.i(TAG, "HwScreenOnProximityLock refresh mistouch view for fold display mode change");
            }
        }
    }

    public boolean shouldWaitScreenOnExBlocker() {
        return IS_SUPPORT_FOLD_SCREEN;
    }

    public boolean isScreenOnExBlocking() {
        boolean z;
        synchronized (this.mScreenOnExLock) {
            z = this.mScreenOnExListener != null;
        }
        return z;
    }

    public void screenTurningOnEx(WindowManagerPolicy.ScreenOnExListener screenOnExListener) {
        if (shouldWaitScreenOnExBlocker()) {
            if (DEBUG_WAKEUP) {
                Slog.i(TAG, "Screen turning on ex...");
            }
            synchronized (this.mScreenOnExLock) {
                this.mScreenOnExListener = screenOnExListener;
                if (this.mFoldScreenManagerService == null || this.mFoldScreenOnUnblocker == null) {
                    this.mHandlerEx.sendEmptyMessage(108);
                } else {
                    this.mHandlerEx.removeMessages(108);
                    this.mHandlerEx.sendEmptyMessageDelayed(108, 1500);
                    this.mFoldScreenManagerService.foldScreenTurningOn(this.mFoldScreenOnUnblocker);
                }
            }
        } else if (screenOnExListener != null) {
            screenOnExListener.onScreenOnEx();
        }
    }

    private final class FoldScreenOnUnblocker implements HwFoldScreenManagerInternal.FoldScreenOnListener {
        private FoldScreenOnUnblocker() {
        }

        public void onFoldScreenOn(int newMode, int oldMode) {
            Slog.i(HwPhoneWindowManager.TAG, "onFoldScreenOn newMode:" + newMode + ", oldMode:" + oldMode);
            HwPhoneWindowManager.this.mHandlerEx.sendMessage(HwPhoneWindowManager.this.mHandlerEx.obtainMessage(107, newMode, oldMode));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finishSetFoldMode(int newMode, int oldMode) {
        long timeout;
        synchronized (this.mScreenOnExLock) {
            if (this.mScreenOnExListener != null) {
                this.mHandlerEx.removeMessages(107);
                if (!(newMode != oldMode)) {
                    this.mHandlerEx.sendEmptyMessage(108);
                    return;
                }
            } else {
                return;
            }
        }
        WindowManagerPolicy.ScreenOnListener listener = this.mDefaultDisplayPolicy.getScreenOnListener();
        boolean isWindowDrawComplete = this.mDefaultDisplayPolicy.isWindowManagerDrawComplete();
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "finishSetFoldMode listener:" + listener + ", isWindowDrawComplete:" + isWindowDrawComplete);
        }
        if (listener != null || !isWindowDrawComplete) {
            timeout = 0;
        } else {
            this.mWindowManagerInternal.waitForAllWindowsDrawn(this.mFoldWindowDrawCallback, (long) LAUNCH_VASSIT_TIMEOUT);
            timeout = LAUNCH_VASSIT_TIMEOUT;
        }
        synchronized (this.mScreenOnExLock) {
            this.mHandlerEx.sendEmptyMessageDelayed(108, timeout);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finishScreenTurningOnEx() {
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "finish screen turning on ex");
        }
        synchronized (this.mScreenOnExLock) {
            this.mHandlerEx.removeMessages(108);
            WindowManagerPolicy.ScreenOnExListener screenOnExListener = this.mScreenOnExListener;
            if (screenOnExListener != null) {
                screenOnExListener.onScreenOnEx();
            }
            this.mScreenOnExListener = null;
        }
    }

    private void initBehaviorCollector() {
        if (IS_SUPPORT_HW_BEHAVIOR_AUTH && this.defaultBehaviorCollector == null) {
            WindowManagerFuncsEx windowManagerFuncsEx = new WindowManagerFuncsEx();
            windowManagerFuncsEx.setWindowManagerFuncs(this.mWindowManagerFuncs);
            this.defaultBehaviorCollector = HwServiceSecurityPartsFactoryEx.getInstance().getBehaviorCollector();
            this.defaultBehaviorCollector.init(this.mContext, windowManagerFuncsEx);
            Log.i(TAG, "behaviorCollector init success");
        }
    }

    /* access modifiers changed from: protected */
    public void checkFaceDetect(boolean isInteractive, String reason, InputEvent event) {
        if (!IS_TABLET || isInteractive) {
            return;
        }
        if (event == null || event.isFromSource(LightsManagerEx.LIGHT_ID_SMARTBACKLIGHT)) {
            doFaceRecognize(true, reason);
        }
    }

    private boolean isTvButtonPress(KeyEvent event) {
        return event.getSource() == 257;
    }

    private boolean isInputBlock(int keyCode, boolean isDownAction, boolean isInteractive) {
        if (!isInteractive && isBlockKeyForNonInteractive(keyCode, false)) {
            return true;
        }
        HwTvPowerManagerPolicy hwTvPowerManagerPolicy = this.mTvPolicy;
        if (hwTvPowerManagerPolicy == null || !hwTvPowerManagerPolicy.isHiRmsProcessing() || !isBlockKeyForNonInteractive(keyCode, true)) {
            return false;
        }
        Slog.i(TAG, "Wait for hiRmService to finish.");
        return true;
    }

    private boolean isBlockKeyForNonInteractive(int keyCode, boolean isRmsProcessing) {
        if (keyCode == 164 || keyCode == 224 || keyCode == 231) {
            return false;
        }
        switch (keyCode) {
            case 24:
            case 25:
                return false;
            case 26:
                return isRmsProcessing;
            default:
                return true;
        }
    }

    private void setCurVolume(float curVolume, int minVolume, int maxVomule) {
        if (curVolume < ((float) minVolume)) {
            this.mCurVolume = (float) minVolume;
        } else if (curVolume > ((float) maxVomule)) {
            this.mCurVolume = (float) maxVomule;
        } else {
            this.mCurVolume = curVolume;
        }
    }

    private boolean isInterceptKeyBeforeQueueingTv(KeyEvent event, boolean isInteractive) {
        float curVolume;
        int keyCode = event.getKeyCode();
        if (keyCode != 26 || !"0".equals(SystemProperties.get(BOOT_ANIM_EXIT_PROP))) {
            boolean isDownAction = event.getAction() == 0;
            if (keyCode == 730) {
                if (isCurrentTvAivisionTutorialWindow()) {
                    Slog.i(TAG, "current is tv tutorial window, pass event");
                    return false;
                }
                if (isDownAction) {
                    AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
                    long now = SystemClock.uptimeMillis();
                    if (this.mCurVolume < 0.0f || now - this.mLastVolumeCacheTime > TOUCH_SPINNING_DELAY_MILLIS) {
                        curVolume = (float) audioManager.getStreamVolume(3);
                    } else {
                        curVolume = this.mCurVolume;
                    }
                    int maxVolume = audioManager.getStreamMaxVolume(3);
                    int offset = event.getScanCode();
                    float level = (((float) (offset * maxVolume)) / VOLUME_MAX_PROGRESS) + curVolume;
                    setCurVolume(level, 0, maxVolume);
                    this.mLastVolumeCacheTime = now;
                    Slog.i(TAG, "handle keyevent : " + keyCode + ",curVolume:" + curVolume + ",offset:" + offset + ",level:" + level);
                    audioManager.setStreamVolume(3, (int) level, 1);
                }
                return true;
            } else if (!isInputBlock(keyCode, isDownAction, isInteractive)) {
                return false;
            } else {
                Slog.i(TAG, "isInterceptKeyBeforeQueueingTv isInputBlock=true");
                return true;
            }
        } else {
            Slog.i(TAG, "system init not compeleted, ignore power_key.");
            return true;
        }
    }

    private void interceptPowerKeyDownTv(KeyEvent event, boolean isInteractive) {
        Log.i(TAG, "interceptPowerKeyDownTv: event " + event + " isInteractive " + isInteractive);
        this.mPowerKeyHandledByTv = false;
        if (isInteractive) {
            Message msg = this.mHandlerEx.obtainMessage(109, Boolean.valueOf(isTvButtonPress(event)));
            msg.setAsynchronous(true);
            this.mHandlerEx.sendMessage(msg);
            this.mPowerKeyHandledByTv = true;
            Slog.i(TAG, "interceptPowerKeyDownTv: sendMessage MSG_POWER_KEY_PRESS");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void noticePowerKeyPressed(boolean isTvButtonPress) {
        if (HwDeviceManager.disallowOp(37)) {
            this.mHandlerEx.sendMessageDelayed(this.mHandlerEx.obtainMessage(MSG_DISABLED_POWER_KEY, Boolean.valueOf(isTvButtonPress)), LAUNCH_VASSIT_TIMEOUT);
            if (isTvButtonPress) {
                turnOffScreenInDeskMode();
            }
        } else if (isTvButtonPress) {
            Slog.i(TAG, "noticePowerKeyPressed: goToSleep shutdown");
            goToSleep(SystemClock.uptimeMillis(), 4, 65536);
        } else {
            Slog.i(TAG, "noticePowerKeyPressed: call the shutdown menu");
            Intent intent = new Intent(ACTION_SYSTEM_BUTTON_PRESS);
            intent.putExtra("button_keycode", 26);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, PERMISSION_RECEIVE_SYSTEM_BUTTON);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void noticeVoiceAssistKeyPressed(boolean isDown) {
        if (isDown) {
            this.mContext.sendOrderedBroadcastAsUser(new Intent(VOICE_ASSIST_KEY_DOWN_ACTION), UserHandle.CURRENT, null, null, null, -1, null, null);
            Log.i(TAG, "noticeVoiceAssistKeyPressed: broadcast VOICE_ASSIST_KEY_DOWN_ACTION.");
            return;
        }
        this.mContext.sendBroadcastAsUser(new Intent(VOICE_ASSIST_KEY_UP_ACTION), UserHandle.ALL);
        Log.i(TAG, "noticeVoiceAssistKeyPressed: broadcast VOICE_ASSIST_KEY_UP_ACTION.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void noticeTvCustomKeyPressed(int keyCode) {
        Intent intent = new Intent(TV_CUSTOM_BUTTON_DOWN_ACTION);
        intent.putExtra(TV_CUSTOM_KEY_CODE, keyCode);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, TV_COMMAND_SERVICE_PERMISSION);
        Log.i(TAG, "send tv broadcast successfully. keyCode is " + keyCode);
    }

    private boolean interceptMenuKeyForTv(KeyEvent event) {
        if ((event.getFlags() & 268435456) != 0) {
            return false;
        }
        if (event.getAction() == 0) {
            this.mIsLongPressMenuKeyHandled = false;
            this.mHandler.postDelayed(this.mLongPressMenuActionForTv, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
        } else if (!this.mIsLongPressMenuKeyHandled) {
            this.mIsLongPressMenuKeyHandled = true;
            this.mHandler.removeCallbacks(this.mLongPressMenuActionForTv);
            sendHwMenuKeyEvent();
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: handleLongPressOnMenuKeyForTv */
    public void lambda$new$0$HwPhoneWindowManager() {
        int type;
        this.mIsLongPressMenuKeyHandled = true;
        WindowManagerPolicy.WindowState focusedWindow = this.mDefaultDisplayPolicy.getFocusedWindow();
        WindowManager.LayoutParams attrs = focusedWindow != null ? focusedWindow.getAttrs() : null;
        if (attrs != null && ((type = attrs.type) == 2009 || (attrs.privateFlags & 1024) != 0 || type == 2003 || type == 2010)) {
            Slog.i(TAG, "System window focused, ignore long press menu");
        } else if (isUserSetupComplete() && !keyguardOn()) {
            this.mContext.sendBroadcastAsUser(new Intent("com.huawei.homevision.action.LONG_PRESS_MENU"), UserHandle.CURRENT, "com.huawei.homevision.permission.LONG_PRESS_MENU");
        }
    }

    private Intent createSosIntent() {
        Intent intent = new Intent();
        if (this.mHasFeatureWatch) {
            intent.setPackage(WATCH_SOS_PACKAGE_NAME);
            intent.setAction(WATCH_SOS_ACTION);
            intent.putExtra("sos_transfer", 2);
            intent.setFlags(343932928);
        } else {
            intent.setPackage(PKG_NAME_EMERGENCY);
            intent.setAction("android.emergency.COUNT_DOWN");
            intent.addCategory("android.intent.category.DEFAULT");
        }
        return intent;
    }

    private void startActivityAsCurrentUser(Intent intent) {
        try {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "ActivityNotFoundException:" + e.getMessage());
        } catch (IllegalStateException e2) {
            Log.e(TAG, "IllegalStateException:" + e2.getMessage());
        } catch (Exception e3) {
            Log.e(TAG, "StartActivity Exception:" + e3.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void showRecentAppsForWatch() {
        if (isKeyguardShowingOrOccluded()) {
            Log.i(TAG, "keyguard is showing and ignore show recent");
            return;
        }
        Log.i(TAG, "start launch recent activity");
        Intent intent = new Intent();
        intent.setPackage(WATCH_RECENT_PACKAGE_NAME);
        intent.setAction(WATCH_RECENT_ACTION);
        startActivityAsCurrentUser(intent);
    }

    private boolean isTargetWindowFocused(WindowManagerPolicy.WindowState focusedWindow, String packageName, String windowTitle) {
        if (focusedWindow == null || focusedWindow.getAttrs() == null) {
            return false;
        }
        String focusPackageName = focusedWindow.getAttrs().packageName;
        String focusWindowTitle = focusedWindow.getAttrs().getTitle().toString();
        if (focusPackageName == null || focusWindowTitle == null || !focusPackageName.equals(packageName) || !focusWindowTitle.contains(windowTitle)) {
            return false;
        }
        return true;
    }

    private void updatePowerKeyPolicy(boolean isDown, boolean isScreenOn, KeyEvent event) {
        if (this.mHasFeatureWatch) {
            if (!isDown) {
                this.mWatchPowerUpTime = event.getEventTime();
                return;
            }
            this.mPowerKeyPolicy = 0;
            boolean isLastFromNonInteractive = this.mIsFromNonInteractive;
            this.mIsFromNonInteractive = false;
            if (isScreenOn && this.mDreamManagerInternal != null && this.mDreamManagerInternal.isDreaming()) {
                Log.i(TAG, "awaken from dream by power key");
                awakenDreams();
                this.mIsFromNonInteractive = true;
            }
            if (!isScreenOn) {
                this.mIsFromNonInteractive = true;
            }
            long downTime = event.getDownTime();
            boolean z = this.mIsFromNonInteractive;
            if (z || (isLastFromNonInteractive && !z && downTime - this.mWatchPowerUpTime < ((long) ViewConfiguration.getMultiPressTimeout()))) {
                this.mPowerKeyPolicy |= 5;
                Log.i(TAG, "update power key policy, isFromNonInteractive:" + this.mIsFromNonInteractive);
                return;
            }
            WindowManagerPolicy.WindowState focusedWindow = this.mDefaultDisplayPolicy != null ? this.mDefaultDisplayPolicy.getFocusedWindow() : null;
            if (isTargetWindowFocused(focusedWindow, WATCH_HEALTHSPORT_PACKAGE_NAME, WATCH_HEALTHSPORT_WINDOW_TITLE)) {
                this.mPowerKeyPolicy |= 65539;
            } else if (isTargetWindowFocused(focusedWindow, WATCH_DESKCLOCK_PACKAGE_NAME, WATCH_DESKCLOCK_WINDOW_TITLE)) {
                this.mPowerKeyPolicy |= 65537;
            } else if (isTargetWindowFocused(focusedWindow, WATCH_INCALLUI_PACKAGE_NAME, WATCH_INCALLUI_WINDOW_TITLE) && this.mIsLastMuteRinger && !mNeedHushByPowerKeyDown) {
                Log.i(TAG, "ringer has mute, then send back");
                this.mPowerKeyPolicy = 1 | this.mPowerKeyPolicy;
                this.mHandlerEx.sendMessage(this.mHandlerEx.obtainMessage(111, 4, 0));
            }
            Log.i(TAG, "update power key policy:0x" + Integer.toHexString(this.mPowerKeyPolicy));
        }
    }

    private boolean handleStem1Event(boolean isDown, boolean isScreenOn, KeyEvent event) {
        if (!this.mHasFeatureWatch) {
            return false;
        }
        boolean isIntercept = false;
        if (!isScreenOn) {
            if (this.mPowerManager != null) {
                this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), 6, "android.policy:KEY_Stem1");
            }
            isIntercept = true;
        }
        if (isScreenOn && this.mDreamManagerInternal != null && this.mDreamManagerInternal.isDreaming()) {
            Log.i(TAG, "awaken from dream by stem1 key");
            awakenDreams();
            isIntercept = true;
        }
        if (isDown) {
            long lastStem1DownTime = this.mStrm1DownTime;
            this.mStrm1DownTime = event.getDownTime();
            if (this.mIsStrm1DownIntercepted && this.mStrm1DownTime - lastStem1DownTime < ((long) ViewConfiguration.getMultiPressTimeout())) {
                Log.i(TAG, "Keep intercpet for quickly multi press after key intercepted");
                isIntercept = true;
            }
            this.mIsStrm1DownIntercepted = isIntercept;
            if (!this.mIsWatchAssistTriggered && isWatchAssistGestureAvailable()) {
                this.mIsWatchAssistTriggered = true;
                this.mHandlerEx.sendEmptyMessageDelayed(112, LAUNCH_VASSIT_TIMEOUT);
            }
        } else {
            if (this.mIsStrm1DownIntercepted) {
                isIntercept = true;
            }
            this.mIsWatchAssistTriggered = false;
            this.mHandlerEx.removeMessages(112);
        }
        return isIntercept;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void launchWatchVassistant() {
        Log.i(TAG, "start launch vassistant activity");
        Intent intent = new Intent();
        intent.setPackage(WATCH_VASSISTANT_PACKAGE_NAME);
        intent.setAction(WATCH_VASSISTANT_ACTION);
        startActivityAsCurrentUser(intent);
    }

    private boolean isWatchAssistGestureAvailable() {
        return (Settings.Global.getInt(this.mContext.getContentResolver(), WATCH_VOICE_SWITCH, 1) == 1) && !canFocusWindowInterceptEvent();
    }

    private boolean canFocusWindowInterceptEvent() {
        WindowManagerPolicy.WindowState focusWindow = getFocusedWindow();
        return focusWindow != null && (focusWindow.getAttrs().hwFlags & FLOATING_MASK) == FLOATING_MASK && focusWindow.getOwningUid() == 1000;
    }

    public boolean hasFingerprintInScreen() {
        int i = this.mFingerprintHardwareType;
        return (i == 0 || i == -1) ? false : true;
    }
}
