package com.android.server.fingerprint;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Log;
import com.android.server.FingerprintDataInterface;
import com.android.server.fingerprint.HwFingerprintSets;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.biometric.AuthenticationClientEx;
import com.huawei.android.biometric.BiometricAuthenticatorEx;
import com.huawei.android.biometric.BiometricServiceReceiverListenerEx;
import com.huawei.android.biometric.BiometricsFingerprintEx;
import com.huawei.android.biometric.ClientMonitorEx;
import com.huawei.android.biometric.ClientMonitorParameterEx;
import com.huawei.android.biometric.FingerprintEx;
import com.huawei.android.biometric.FingerprintParameterEx;
import com.huawei.android.biometric.FingerprintServiceEx;
import com.huawei.android.biometric.FingerprintStateNotifierEx;
import com.huawei.android.biometric.FingerprintSupportEx;
import com.huawei.android.biometric.FingerprintUtilsEx;
import com.huawei.android.biometric.ParcelEx;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.pm.UserInfoExt;
import com.huawei.android.os.HwBinderEx;
import com.huawei.android.os.PowerManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerExt;
import com.huawei.android.provider.SettingsEx;
import com.huawei.displayengine.DisplayEngineManager;
import com.huawei.hwpartfingerprintopt.BuildConfig;
import com.huawei.server.ServiceThreadExt;
import com.huawei.server.face.FaceDetectManager;
import com.huawei.server.fingerprint.FingerViewController;
import com.huawei.server.fingerprint.FingerprintCalibrarionView;
import com.huawei.server.fingerprint.FingerprintController;
import com.huawei.server.fingerprint.FingerprintTestForMmi;
import com.huawei.server.fingerprint.FingerprintViewUtils;
import com.huawei.server.fingerprint.HwFpServiceToHalUtils;
import com.huawei.utils.HwPartResourceUtils;
import huawei.android.provider.FrontFingerPrintSettings;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwFingerprintService extends FingerprintServiceEx {
    private static final String ACTIVITYNAME_OF_WECHAT_ENROLL = "com.tencent.mm.plugin.fingerprint.ui.FingerPrintAuthUI";
    private static final String APS_INIT_HEIGHT = "aps_init_height";
    private static final String APS_INIT_WIDTH = "aps_init_width";
    private static final int BASE_BRIGHTNESS = 3000;
    public static final int CHECK_NEED_REENROLL_FINGER = 1003;
    protected static final int CLIENT_OFF = 0;
    protected static final int CLIENT_ON = 1;
    private static final int CODE_DISABLE_FINGERPRINT_VIEW_RULE = 1114;
    private static final int CODE_ENABLE_FINGERPRINT_VIEW_RULE = 1115;
    private static final int CODE_FINGERPRINT_FORBID_GOTOSLEEP = 1125;
    private static final int CODE_FINGERPRINT_LOGO_POSITION = 1130;
    private static final int CODE_FINGERPRINT_LOGO_RADIUS = 1129;
    private static final int CODE_FINGERPRINT_MMI_TEST = 1131;
    private static final int CODE_FINGERPRINT_WEATHER_DATA = 1128;
    private static final int CODE_GET_FINGERPRINT_LIST_ENROLLED = 1118;
    private static final int CODE_GET_HARDWARE_POSITION = 1110;
    private static final int CODE_GET_HARDWARE_TYPE = 1109;
    private static final int CODE_GET_HIGHLIGHT_SPOT_RADIUS_RULE = 1122;
    private static final int CODE_GET_HOVER_SUPPORT = 1113;
    private static final int CODE_GET_TOKEN_LEN_RULE = 1103;
    private static final int CODE_IS_FINGERPRINT_HARDWARE_DETECTED = 1119;
    private static final int CODE_IS_FP_NEED_CALIBRATE_RULE = 1101;
    private static final int CODE_IS_SUPPORT_DUAL_FINGERPRINT = 1120;
    private static final int CODE_IS_WAIT_AUTHEN = 1127;
    private static final int CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION_RULE = 1116;
    private static final int CODE_NOTIFY_OPTICAL_CAPTURE = 1111;
    private static final int CODE_POWER_KEYCODE = 1126;
    private static final int CODE_REMOVE_FINGERPRINT_RULE = 1107;
    private static final int CODE_REMOVE_MASK_AND_SHOW_BUTTON_RULE = 1117;
    private static final int CODE_SEND_UNLOCK_LIGHTBRIGHT = 1121;
    private static final int CODE_SET_CALIBRATE_MODE_RULE = 1102;
    private static final int CODE_SET_FINGERPRINT_MASK_VIEW_RULE = 1104;
    private static final int CODE_SET_HOVER_SWITCH = 1112;
    private static final int CODE_SHOW_FINGERPRINT_BUTTON_RULE = 1106;
    private static final int CODE_SHOW_FINGERPRINT_VIEW_RULE = 1105;
    private static final int CODE_SUSPEND_AUTHENTICATE = 1108;
    private static final int CODE_SUSPEND_ENROLL = 1123;
    private static final int CODE_UDFINGERPRINT_SPOTCOLOR = 1124;
    private static final int CONFIG_FP_DELAY_POWER_ID = 2;
    private static final int CONFIG_POWER_DELAY_FP_ID = 3;
    private static final int CONFIG_SIZE = 4;
    private static final int CONFIG_SUPPORT_BLACK_AUTH_ID = 1;
    private static final int CONFIG_SUPPORT_POWER_FP_ID = 0;
    private static final int DEFAULT_CAPTURE_BRIGHTNESS = 248;
    private static final int DEFAULT_COLOR = -16711681;
    private static final int DEFAULT_FINGER_LOGO_RADIUS = 87;
    private static final int DEFAULT_RADIUS = 95;
    private static final int DEFAULT_SCREEN_SIZE_STRING_LENGHT = 2;
    private static final int DENSITY_DEFUALT_HEIGHT = 2880;
    private static final int DENSITY_DEFUALT_WIDTH = 1440;
    private static final String DESCRIPTOR_FINGERPRINT_SERVICE = "android.hardware.fingerprint.IFingerprintService";
    private static final int ERROR_CODE_COMMEN_ERROR = 8;
    private static final String FIDO_ASM = "com.huawei.hwasm";
    private static final int FINGERPRINT_ACQUIRED_FINGER_DOWN = 2002;
    private static final int FINGERPRINT_HARDWARE_OPTICAL = 1;
    private static final int FINGERPRINT_HARDWARE_OUTSCREEN = 0;
    private static final int FINGERPRINT_HARDWARE_ULTRASONIC = 2;
    private static final String FINGERPRINT_METADATA_KEY = "fingerprint.system.view";
    private static final int FINGER_DOWN_TYPE_AUTHENTICATING = 1;
    private static final int FINGER_DOWN_TYPE_AUTHENTICATING_SETTINGS = 2;
    private static final int FINGER_DOWN_TYPE_AUTHENTICATING_SYSTEMUI = 3;
    private static final int FINGER_DOWN_TYPE_ENROLLING = 0;
    private static final int FLAG_FINGERPRINT_LOCATION_BACK = 1;
    private static final int FLAG_FINGERPRINT_LOCATION_FRONT = 2;
    private static final int FLAG_FINGERPRINT_LOCATION_UNDER_DISPLAY = 4;
    private static final int FLAG_FINGERPRINT_TYPE_MASK = 15;
    private static final int FLAG_FINGERPRINT_TYPE_OPTICAL = 2;
    private static final int FLAG_FINGERPRINT_TYPE_ULTRASONIC = 3;
    private static final int FLAG_USE_UD_FINGERPRINT = 134217728;
    private static final int FP_CLOSE = 0;
    private static final int FP_OPEN = 1;
    public static final int GET_OLD_DATA = 100;
    private static final int HAL_CMD_NOT_FOUND_RET = 0;
    private static final int HUAWEI_FINGERPRINT_CAPTURE_COMPLETE = 0;
    private static final int HUAWEI_FINGERPRINT_DOWN = 2002;
    private static final int HUAWEI_FINGERPRINT_DOWN_UD = 2102;
    private static final int HUAWEI_FINGERPRINT_TRIGGER_FACE_RECOGNIZATION = 2104;
    private static final int HUAWEI_FINGERPRINT_UP = 2003;
    private static final int HW_FP_NO_COUNT_FAILED_ATTEMPS = 16777216;
    private static final int INITIAL_PARAMETER_NUM = 4;
    private static final int INVALID_VALUE = -1;
    private static final int IN_SET_FINGERPRINT_IDENTIFY = 1;
    private static final boolean IS_SUPPORT_INFORM_FACE = SystemPropertiesEx.getBoolean("ro.config.fp_notice_face", false);
    private static final String KEY_DB_CHILDREN_MODE_FPID = "fp_children_mode_fp_id";
    private static final String KEY_DB_CHILDREN_MODE_STATUS = "fp_children_enabled";
    private static final String KEY_KEYGUARD_ENABLE = "fp_keyguard_enable";
    public static final int MASK_TYPE_BACK = 4;
    public static final int MASK_TYPE_BUTTON = 1;
    public static final int MASK_TYPE_FULL = 0;
    public static final int MASK_TYPE_IMAGE = 3;
    public static final int MASK_TYPE_NONE = 2;
    private static final int MAX_BRIGHTNESS = 255;
    private static final String METADATA_KEY = "fingerprint.system.view";
    private static final int MMI_TYPE_GET_HIGHLIGHT_LEVEL = 906;
    private static final int MSG_AUTHENTICATEDFINISH_TO_HAL = 199;
    private static final int MSG_BINDER_SUCCESS_FLAG = 170;
    private static final int MSG_CHECK_AND_DEL_TEMPLATES = 84;
    private static final int MSG_CHECK_HOVER_SUPPORT = 56;
    private static final int MSG_CHECK_OLD_TEMPLATES = 85;
    private static final int MSG_CHECK_SWITCH_FREQUENCE_SUPPORT = 81;
    private static final int MSG_DEL_OLD_TEMPLATES = 86;
    private static final int MSG_DISABLE_FACE_RECOGNIZATION = 210;
    private static final int MSG_ENABLE_FACE_RECOGNIZATION = 211;
    private static final int MSG_FACE_RECOGNIZATION_SUCC = 212;
    private static final int MSG_GET_BRIGHTNEWSS_FROM_HAL = 909;
    private static final int MSG_GET_HIGHLIGHT_SPOT_COLOR_FROM_HAL = 903;
    private static final int MSG_GET_LOGO_POSITION_FROM_HAL = 912;
    private static final int MSG_GET_RADIUS_FROM_HAL = 902;
    private static final int MSG_GET_SENSOR_POSITION_BOTTOM_RIGHT = 60;
    private static final int MSG_GET_SENSOR_POSITION_TOP_LEFT = 59;
    private static final int MSG_GET_SENSOR_TYPE = 55;
    private static final int MSG_JUMP_IN_SETTING_VIEW = 66;
    private static final int MSG_JUMP_OUT_SETTING_VIEW = 67;
    private static final int MSG_MMI_UD_UI_LOGO_SIZE = 910;
    private static final int MSG_NOTIFY_AUTHENCATION = 87;
    private static final int MSG_NOTIFY_BLUESPOT_DISMISS = 62;
    private static final int MSG_OPTICAL_HBM_FOR_CAPTURE_IMAGE = 52;
    private static final int MSG_REDUCING_FREQUENCE = 83;
    private static final int MSG_RESUME_AUTHENTICATION = 54;
    private static final int MSG_RESUME_ENROLLMENT = 65;
    private static final int MSG_SCREEOFF_UNLOCK_LIGHTBRIGHT = 200;
    private static final int MSG_SCREEON_UNLOCK_BACKLIGHT = 202;
    private static final int MSG_SCREEON_UNLOCK_LIGHTBRIGHT = 201;
    private static final int MSG_SET_HOVER_DISABLE = 58;
    private static final int MSG_SET_HOVER_ENABLE = 57;
    private static final int MSG_SUSPEND_AUTHENTICATION = 53;
    private static final int MSG_SUSPEND_ENROLLMENT = 64;
    private static final int MSG_TYPE_FINGERPRINT_NAV = 43;
    private static final int MSG_TYPE_VIRTUAL_NAV = 45;
    private static final int MSG_UDENV_PARA_DATA = 400;
    private static final int MSG_UNLOCK_LEARNING = 63;
    private static final int MSG_UPDATE_AUTHENTICATION_SCENARIO = 300;
    private static final int MSG_UPGRADING_FREQUENCE = 82;
    private static final int OUT_SET_FINGERPRINT_IDENTIFY = 0;
    private static final String PATH_CHILDMODE_STATUS = "childmode_status";
    private static final String PKGNAME_OF_KEYGUARD = "com.android.systemui";
    private static final String PKGNAME_OF_SETTINGS = "com.android.settings";
    private static final String PKGNAME_OF_SYSTEM_MANAGER = "com.huawei.systemmanager";
    private static final String PKGNAME_OF_WECHAT = "com.tencent.mm";
    private static final long POWER_PUSH_DOWN_TIME_THR = 430;
    private static final int PROTECT_TIME_FROM_END = 600;
    private static final int PROTECT_TIME_FROM_FINGER_DOWN = 1000;
    private static final int PROTECT_TIME_FROM_START = 700;
    private static final String PROXIMITY_TP = "proximity-tp";
    public static final int REMOVE_USER_DATA = 101;
    public static final int RESTORE_AUTHENTICATE = 0;
    public static final int RESTORE_ENROLL = 0;
    private static final int RESULT_FINGERPRINT_AUTHENTICATION_CHECKING = 3;
    private static final int SCREENOFF_UNLOCK = 1;
    private static final int SCREENON_BACKLIGHT_UNLOCK = 3;
    private static final int SCREENON_UNLOCK = 2;
    protected static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final String SET_FINGERPRINT_IDENTIFY = "set_fingerprint_identify";
    public static final int SET_LIVENESS_SWITCH = 1002;
    private static final int STATUS_PARENT_CTRL_OFF = 0;
    private static final int STATUS_PARENT_CTRL_STUDENT = 1;
    public static final int SUSPEND_AUTHENTICATE = 1;
    public static final int SUSPEND_ENROLL = 1;
    private static final int SWITCH_FREQUENCE_SUPPORT = 1;
    private static final String TAG = "HwFingerprintService";
    private static final int TOUCHE_SWITCH_OFF = 0;
    private static final int TOUCHE_SWITCH_ON = 1;
    protected static final String TP_HAL_CONFIG_OFF = "1,0";
    protected static final String TP_HAL_CONFIG_ON = "1,1";
    private static final int TP_HAL_DEATH_COOKIE = 1001;
    private static final int TP_HAL_FEATURE_FLAG = 2;
    public static final int TYPE_DISMISS = 0;
    private static final int TYPE_FINGERPRINT_AUTHENTICATION_RESULT_FAIL = 1;
    private static final int TYPE_FINGERPRINT_AUTHENTICATION_RESULT_SUCCESS = 0;
    private static final int TYPE_FINGERPRINT_AUTHENTICATION_UNCHECKED = 2;
    public static final int TYPE_FINGERPRINT_BUTTON = 2;
    public static final int TYPE_FINGERPRINT_VIEW = 1;
    private static final int UNDEFINED_TYPE = -1;
    public static final int VERIFY_USER = 1001;
    private static boolean sIsCheckNeedEnroll = true;
    private static boolean sIsChinaArea = "156".equals(SystemPropertiesEx.get("ro.config.hw_optb", "0"));
    private static boolean sIsLivenessNeedBetaQualification = false;
    private static boolean sIsNeedRecreateDialog = false;
    private static boolean sIsRemoveFingerprintBGE = SystemPropertiesEx.getBoolean("ro.config.remove_finger_bge", false);
    private static boolean sIsRemoveOldTemplatesFeature = SystemPropertiesEx.getBoolean("ro.config.remove_old_templates", false);
    ContentObserver fpObserver = new ContentObserver(null) {
        /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass7 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            if (HwFingerprintService.this.mContext == null) {
                Log.w(HwFingerprintService.TAG, "mContext is null");
                return;
            }
            HwFingerprintService hwFingerprintService = HwFingerprintService.this;
            hwFingerprintService.mState = SettingsEx.Secure.getIntForUser(hwFingerprintService.mContext.getContentResolver(), HwFingerprintService.KEY_KEYGUARD_ENABLE, 0, ActivityManagerEx.getCurrentUser());
            if (HwFingerprintService.this.mState == 0) {
                HwFpServiceToHalUtils.sendCommandToHal(0);
            }
            Log.i(HwFingerprintService.TAG, "fp_keyguard_state onChange: " + HwFingerprintService.this.mState);
        }
    };
    private AODFaceUpdateMonitor mAodFaceUpdateMonitor;
    private int mAppDefinedMaskType = -1;
    private FingerprintController.AuthenticatedParam mAuthenticatedParam = null;
    private Runnable mBlackAuthenticateEventResetRunable = new Runnable() {
        /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass3 */

        @Override // java.lang.Runnable
        public void run() {
            Log.i(HwFingerprintService.TAG, "powerfp mBlackAuthenticateEventResetRunable");
            HwFingerprintService.this.mIsBlackAuthenticateEvent = false;
        }
    };
    private List<Integer> mCodeList = Arrays.asList(Integer.valueOf((int) CODE_IS_FP_NEED_CALIBRATE_RULE), Integer.valueOf((int) CODE_SET_CALIBRATE_MODE_RULE), Integer.valueOf((int) CODE_GET_TOKEN_LEN_RULE), Integer.valueOf((int) CODE_SET_FINGERPRINT_MASK_VIEW_RULE), Integer.valueOf((int) CODE_SHOW_FINGERPRINT_VIEW_RULE), Integer.valueOf((int) CODE_SHOW_FINGERPRINT_BUTTON_RULE), Integer.valueOf((int) CODE_REMOVE_FINGERPRINT_RULE), Integer.valueOf((int) CODE_GET_HARDWARE_POSITION), Integer.valueOf((int) CODE_FINGERPRINT_LOGO_POSITION), Integer.valueOf((int) CODE_GET_HARDWARE_TYPE), Integer.valueOf((int) CODE_NOTIFY_OPTICAL_CAPTURE), Integer.valueOf((int) CODE_SUSPEND_AUTHENTICATE), Integer.valueOf((int) CODE_SET_HOVER_SWITCH), Integer.valueOf((int) CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION_RULE), Integer.valueOf((int) CODE_DISABLE_FINGERPRINT_VIEW_RULE), Integer.valueOf((int) CODE_ENABLE_FINGERPRINT_VIEW_RULE), Integer.valueOf((int) CODE_GET_HOVER_SUPPORT), Integer.valueOf((int) CODE_REMOVE_MASK_AND_SHOW_BUTTON_RULE), Integer.valueOf((int) CODE_IS_SUPPORT_DUAL_FINGERPRINT), Integer.valueOf((int) CODE_GET_FINGERPRINT_LIST_ENROLLED), Integer.valueOf((int) CODE_IS_FINGERPRINT_HARDWARE_DETECTED), Integer.valueOf((int) CODE_SEND_UNLOCK_LIGHTBRIGHT), Integer.valueOf((int) CODE_GET_HIGHLIGHT_SPOT_RADIUS_RULE), Integer.valueOf((int) CODE_SUSPEND_ENROLL), Integer.valueOf((int) CODE_UDFINGERPRINT_SPOTCOLOR), Integer.valueOf((int) CODE_FINGERPRINT_FORBID_GOTOSLEEP), Integer.valueOf((int) CODE_POWER_KEYCODE), Integer.valueOf((int) CODE_IS_WAIT_AUTHEN), Integer.valueOf((int) CODE_FINGERPRINT_WEATHER_DATA), Integer.valueOf((int) CODE_FINGERPRINT_MMI_TEST));
    private final Context mContext;
    private int mCurrentAuthFpDev;
    protected BiometricsFingerprintEx mDaemonEx = null;
    private String mDefinedAppName = BuildConfig.FLAVOR;
    private DisplayEngineManager mDisplayEngineManager;
    private long mDownTime;
    private Handler mFingerHandler;
    private FingerViewController mFingerViewController;
    private final Runnable mFingerprintStateNotifierExInit = new Runnable() {
        /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass13 */

        @Override // java.lang.Runnable
        public void run() {
            FingerprintStateNotifierEx.getInstance(HwFingerprintService.this.mContext).init();
        }
    };
    private int mFingerprintType = -1;
    protected FingerprintDataInterface mFpDataCollector;
    private int mFpDelayPowerTime = 0;
    private int mHwFailedAttempts = 0;
    private int mInitDisplayHeight = -1;
    private int mInitDisplayWidth = -1;
    private boolean mIsBlackAuthenticateEvent = false;
    private boolean mIsFingerInScreenSupported;
    private boolean mIsFlagFirstIn = true;
    private boolean mIsForbideKeyguardCall = false;
    private boolean mIsHighLightNeed;
    private boolean mIsInteractive = false;
    private boolean mIsKeepMaskAfterAuthentication = false;
    private AtomicBoolean mIsKeyguardAuthenStatus = new AtomicBoolean(false);
    private boolean mIsNeedResumeTouchSwitch = false;
    private boolean mIsPowerKeyDown = false;
    private boolean mIsReadyWakeUp = false;
    private boolean mIsSupportBlackAuthentication = false;
    private boolean mIsSupportDualFingerprint = false;
    private boolean mIsSupportKids = SystemPropertiesEx.getBoolean("ro.config.kidsfinger_enable", false);
    private boolean mIsSupportPowerFp = false;
    private boolean mIsUdAuthenticating = false;
    private boolean mIsUdEnrolling = false;
    private boolean mIsUdFingerprintChecking = false;
    private boolean mIsVirNavModeEnabled = false;
    private long mLastAuthenticatedEndTime = 0;
    private long mLastAuthenticatedStartTime = 0;
    private long mLastFingerDownTime = 0;
    private long mLastPowerKeyDownTime = 0;
    private long mLastPowerKeyUpTime = 0;
    private final Object mLock = new Object();
    private Bundle mMaskViewBundle;
    private ContentObserver mNavModeObserver = new ContentObserver(null) {
        /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass8 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            if (HwFingerprintService.this.mContext == null || HwFingerprintService.this.mContext.getContentResolver() == null) {
                Log.i(HwFingerprintService.TAG, "mContext or the resolver is null");
                return;
            }
            boolean isVirNavModeEnabled = FrontFingerPrintSettings.isNaviBarEnabled(HwFingerprintService.this.mContext.getContentResolver());
            if (HwFingerprintService.this.mIsVirNavModeEnabled != isVirNavModeEnabled) {
                HwFingerprintService.this.mIsVirNavModeEnabled = isVirNavModeEnabled;
                Log.i(HwFingerprintService.TAG, "Navigation mode changed, mIsVirNavModeEnabled = " + HwFingerprintService.this.mIsVirNavModeEnabled);
                HwFpServiceToHalUtils.sendCommandToHal(HwFingerprintService.this.mIsVirNavModeEnabled ? HwFingerprintService.MSG_TYPE_VIRTUAL_NAV : HwFingerprintService.MSG_TYPE_FINGERPRINT_NAV);
            }
        }
    };
    private String mOpPackageName;
    private String mPackageDisableMask;
    private int mPowerDelayFpTime = 0;
    private Runnable mPowerFingerWakeUpRunable = new Runnable() {
        /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass12 */

        @Override // java.lang.Runnable
        public void run() {
            Log.i(HwFingerprintService.TAG, "powerfp wakeup run");
            HwFingerprintService.this.mIsReadyWakeUp = false;
            HwFingerprintService.this.mIsKeyguardAuthenStatus.set(false);
            PowerManagerEx.wakeUp(HwFingerprintService.this.mPowerManager, SystemClock.uptimeMillis(), HwFingerprintService.this.getPowerWakeupReason(), "android.policy:POWER_FINGERPRINT");
        }
    };
    private final PowerManager mPowerManager;
    private AlertDialog mReEnrollDialog;
    private BroadcastReceiver mReceiver;
    private String mScreen;
    private int mState = 0;
    private BroadcastReceiver mSwitchFrequenceMonitor = new BroadcastReceiver() {
        /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass10 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action;
            if (context != null && intent != null && (action = intent.getAction()) != null) {
                if (action.equals("android.intent.action.SCREEN_ON") || action.equals("android.intent.action.SCREEN_OFF")) {
                    HwFingerprintService.this.mScreen = action;
                    HwFingerprintService.this.handleScreenOnOrOff();
                }
            }
        }
    };
    private TelecomManager mTelecomManager = null;
    private long mTimeStart = 0;
    private String mTpSensorName = null;
    private int mTypeDetails = -1;
    private BroadcastReceiver mUserDeletedMonitor = new BroadcastReceiver() {
        /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass4 */
        private static final String FP_DATA_DIR = "fpdata";

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action;
            if (context != null && intent != null && (action = intent.getAction()) != null) {
                if ("android.intent.action.USER_REMOVED".equals(action)) {
                    receiveUserRemove(intent);
                } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                    recerveUserPersent(context, intent);
                }
            }
        }

        private void receiveUserRemove(Intent intent) {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            Log.i(HwFingerprintService.TAG, "user deleted:" + userId);
            if (userId == -1) {
                Log.i(HwFingerprintService.TAG, "get User id failed");
                return;
            }
            int newUserId = userId;
            int newPathId = userId;
            if (FingerprintSupportEx.isHwHiddenSpace(UserManagerExt.getUserInfoEx(UserManagerExt.get(HwFingerprintService.this.mContext), userId))) {
                newUserId = HwFingerprintService.this.getHiddenSpaceId();
                newPathId = HwFingerprintService.this.getPrimaryUserId();
            }
            File fpDir = new File(HwFingerprintService.getFingerprintFileDirectory(newPathId), FP_DATA_DIR);
            if (!fpDir.exists()) {
                Log.v(HwFingerprintService.TAG, "no fpdata!");
                return;
            }
            try {
                HwFingerprintService.this.removeUserData(newUserId, fpDir.getCanonicalPath());
            } catch (IOException e) {
                Log.e(HwFingerprintService.TAG, "removeUserData error");
            }
        }

        private void recerveUserPersent(Context context, Intent intent) {
            if (HwFingerprintService.sIsCheckNeedEnroll) {
                if (HwFingerprintService.sIsRemoveOldTemplatesFeature) {
                    if (HwFingerprintService.sIsChinaArea) {
                        HwFpServiceToHalUtils.sendCommandToHal(HwFingerprintService.MSG_CHECK_AND_DEL_TEMPLATES);
                    } else {
                        HwFpServiceToHalUtils.sendCommandToHal(HwFingerprintService.MSG_CHECK_OLD_TEMPLATES);
                    }
                }
                int checkValReEnroll = HwFingerprintService.this.checkNeedReEnrollFingerPrints();
                int checkValCalibrate = HwFingerprintService.this.checkNeedCalibrateFingerPrint();
                Log.i(HwFingerprintService.TAG, "USER_PRESENT mUserDeletedMonitor need enrol : " + checkValReEnroll + "need calibrate:" + checkValCalibrate);
                if (HwFingerprintService.sIsRemoveOldTemplatesFeature) {
                    if (HwFingerprintService.sIsChinaArea && checkValReEnroll == 1) {
                        HwFingerprintService hwFingerprintService = HwFingerprintService.this;
                        hwFingerprintService.updateActiveGroupEx(hwFingerprintService.getHiddenSpaceId());
                        HwFingerprintService hwFingerprintService2 = HwFingerprintService.this;
                        hwFingerprintService2.updateActiveGroupEx(hwFingerprintService2.getPrimaryUserId());
                        HwFingerprintService.this.showDialog(false);
                    } else if (!HwFingerprintService.sIsChinaArea && checkValReEnroll == 3) {
                        HwFingerprintService.this.showDialog(true);
                    }
                } else if (checkValReEnroll == 1 && checkValCalibrate != 1) {
                    HwFingerprintService.this.intentOthers(context);
                }
                boolean unused = HwFingerprintService.sIsCheckNeedEnroll = false;
            }
            if (HwFingerprintService.this.mFingerViewController != null && "com.android.systemui".equals(HwFingerprintService.this.mFingerViewController.getCurrentPackage())) {
                Log.i(HwFingerprintService.TAG, "USER_PRESENT removeMaskOrButton");
                HwFingerprintService.this.mFingerViewController.removeMaskOrButton();
            }
            HwFingerprintService.this.mIsForbideKeyguardCall = false;
            if ("fingerprint".equals(intent.getStringExtra("unlockReason"))) {
                HwFpServiceToHalUtils.sendCommandToHal(HwFingerprintService.MSG_FACE_RECOGNIZATION_SUCC);
            } else {
                HwFpServiceToHalUtils.sendCommandToHal(HwFingerprintService.MSG_UNLOCK_LEARNING);
            }
        }
    };
    private BroadcastReceiver mUserSwitchReceiver = new BroadcastReceiver() {
        /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass9 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.w(HwFingerprintService.TAG, "intent is null");
            } else if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                HwFingerprintService hwFingerprintService = HwFingerprintService.this;
                hwFingerprintService.mIsVirNavModeEnabled = FrontFingerPrintSettings.isNaviBarEnabled(hwFingerprintService.mContext.getContentResolver());
                Log.i(HwFingerprintService.TAG, "Read the navigation mode after user switch, mIsVirNavModeEnabled = " + HwFingerprintService.this.mIsVirNavModeEnabled);
                HwFpServiceToHalUtils.sendCommandToHal(HwFingerprintService.this.mIsVirNavModeEnabled ? HwFingerprintService.MSG_TYPE_VIRTUAL_NAV : HwFingerprintService.MSG_TYPE_FINGERPRINT_NAV);
            }
        }
    };
    private HashSet<String> mWhitelist = new HashSet<>();
    private final Runnable screenOnOrOffRunnable = new Runnable() {
        /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass5 */

        @Override // java.lang.Runnable
        public void run() {
            boolean isHasFingerprints = false;
            int fpState = SettingsEx.Secure.getIntForUser(HwFingerprintService.this.mContext.getContentResolver(), HwFingerprintService.KEY_KEYGUARD_ENABLE, 0, ActivityManagerEx.getCurrentUser());
            if (FingerprintUtilsEx.getInstance().getBiometricsForUser(HwFingerprintService.this.mContext, ActivityManagerEx.getCurrentUser()).size() > 0) {
                isHasFingerprints = true;
            }
            if (fpState != 0 && isHasFingerprints) {
                return;
            }
            if ("android.intent.action.SCREEN_ON".equals(HwFingerprintService.this.mScreen)) {
                HwFpServiceToHalUtils.sendCommandToHal(HwFingerprintService.MSG_UPGRADING_FREQUENCE);
            } else if ("android.intent.action.SCREEN_OFF".equals(HwFingerprintService.this.mScreen)) {
                HwFpServiceToHalUtils.sendCommandToHal(HwFingerprintService.MSG_REDUCING_FREQUENCE);
            }
        }
    };
    ContentObserver setFpIdentifyViewObserver = new ContentObserver(null) {
        /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass6 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            if (HwFingerprintService.this.mContext == null) {
                Log.w(HwFingerprintService.TAG, "setFpIdentifyViewObserver mContext is null");
                return;
            }
            int setState = SettingsEx.Secure.getIntForUser(HwFingerprintService.this.mContext.getContentResolver(), HwFingerprintService.SET_FINGERPRINT_IDENTIFY, 0, ActivityManagerEx.getCurrentUser());
            HwFpServiceToHalUtils.sendCommandToHal(setState == 1 ? HwFingerprintService.MSG_JUMP_IN_SETTING_VIEW : HwFingerprintService.MSG_JUMP_OUT_SETTING_VIEW);
            Log.i(HwFingerprintService.TAG, "setFpIdentifyViewObserver setState: " + setState);
        }
    };
    private ContentObserver setFpToughenedFilmStateObserver = new ContentObserver(null) {
        /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass11 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            if (HwFingerprintService.this.mContext == null) {
                Log.w(HwFingerprintService.TAG, "setFpToughenedFilmStateObserver mContext is null");
                return;
            }
            int filmState = SettingsEx.Secure.getIntForUser(HwFingerprintService.this.mContext.getContentResolver(), HwFpServiceToHalUtils.SET_TOUGHENED_FILM_STATE, 1, ActivityManagerEx.getCurrentUser());
            int ret = HwFpServiceToHalUtils.sendDataToHal(HwFpServiceToHalUtils.MSG_UPDATE_SCREEN_FILM_STATE, BuildConfig.FLAVOR + filmState);
            Log.i(HwFingerprintService.TAG, "setFpToughenedFilmStateObserver filmState: " + filmState + " ,ret: " + ret);
        }
    };

    public int verifyUserEx(final BiometricServiceReceiverListenerEx receiver, int userid, final byte[] nonce, final String aaid) {
        Log.i(TAG, "verifyUser");
        if (!HwFingerprintService.super.isCurrentUserOrProfile(UserHandleEx.getCallingUserId())) {
            Log.w(TAG, "Can't authenticate non-current user");
            return -1;
        }
        if (receiver != null && nonce != null) {
            if (aaid != null) {
                int uid = Binder.getCallingUid();
                int pid = Binder.getCallingPid();
                Log.i(TAG, "uid =" + uid);
                if (uid != 1000) {
                    Log.e(TAG, "permission denied.");
                    return -1;
                } else if (!HwFingerprintService.super.canUseBiometric(FIDO_ASM, true, uid, pid, userid)) {
                    Log.w(TAG, "FIDO_ASM can't use fingerprint");
                    return -1;
                } else {
                    final int effectiveGroupId = getEffectiveUserId(userid);
                    final int callingUserId = UserHandleEx.getCallingUserId();
                    getHandler().post(new Runnable() {
                        /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            HwFingerprintService.this.setLivenessSwitch("fido");
                            ClientMonitorParameterEx clientMonitorParameterEx = new ClientMonitorParameterEx();
                            clientMonitorParameterEx.setContext(HwFingerprintService.this.getContextEx());
                            clientMonitorParameterEx.setConstants(HwFingerprintService.this.getConstantsEx());
                            clientMonitorParameterEx.setDaemon(HwFingerprintService.this.getDaemonWrapperEx());
                            clientMonitorParameterEx.setToken(receiver.asBinder());
                            clientMonitorParameterEx.setOpId(0);
                            clientMonitorParameterEx.setCallingUserId(callingUserId);
                            clientMonitorParameterEx.setGroupId(effectiveGroupId);
                            clientMonitorParameterEx.setListener(receiver);
                            clientMonitorParameterEx.setFlags(0);
                            clientMonitorParameterEx.setRestricted(true);
                            clientMonitorParameterEx.setOwner(HwFingerprintService.FIDO_ASM);
                            clientMonitorParameterEx.setAaid(aaid);
                            clientMonitorParameterEx.setNonce(nonce);
                            HwFingerprintService.this.startAuthentication(clientMonitorParameterEx);
                        }
                    });
                    return 0;
                }
            }
        }
        Log.e(TAG, "wrong paramers.");
        return -1;
    }

    public int cancelVerifyUserEx(final BiometricServiceReceiverListenerEx receiver, int userId) {
        if (receiver == null) {
            Log.e(TAG, "wrong paramers.");
            return -1;
        }
        Log.i(TAG, "cancelVerify");
        if (!isCurrentUserOrProfile(UserHandleEx.getCallingUserId())) {
            Log.w(TAG, "Can't cancel authenticate non-current user");
            return -1;
        }
        int uid = Binder.getCallingUid();
        Log.i(TAG, "uid =" + uid);
        if (uid != 1000) {
            Log.e(TAG, "permission denied.");
            return -1;
        } else if (!canUseBiometric(FIDO_ASM, true, uid, Binder.getCallingPid(), userId)) {
            Log.w(TAG, "FIDO_ASM can't cancel fingerprint auth");
            return -1;
        } else {
            getHandler().post(new Runnable() {
                /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    HwFingerprintService.this.stopFidoClient(receiver);
                }
            });
            return 0;
        }
    }

    private void initFpToughenedFilmStateObserver() {
        if (this.setFpToughenedFilmStateObserver == null) {
            Log.w(TAG, "setFpToughenedFilmStateObserver is null");
            return;
        }
        Log.i(TAG, "initFpToughenedFilmStateObserver");
        registerContentObserver(getUriForSecure(HwFpServiceToHalUtils.SET_TOUGHENED_FILM_STATE), false, this.setFpToughenedFilmStateObserver, -1);
        this.setFpToughenedFilmStateObserver.onChange(true);
    }

    public HwFingerprintService(Context context) {
        super(context);
        this.mContext = context;
        this.mDisplayEngineManager = new DisplayEngineManager();
        ServiceThreadExt fingerprintThread = new ServiceThreadExt("fingerprintServcie", -8, false);
        fingerprintThread.start();
        this.mFingerHandler = new Handler(fingerprintThread.getLooper()) {
            /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass14 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what != 87) {
                    Log.w(HwFingerprintService.TAG, "Unknown message:" + msg.what);
                    return;
                }
                Log.i(HwFingerprintService.TAG, "MSG_NOTIFY_AUTHENCATION");
                HwFingerprintService.this.handlerNotifyAuthencation(msg);
            }
        };
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        initPropHwFpType();
        this.mFpDataCollector = FingerprintDataInterface.getInstance();
    }

    public void serviceDied(long cookie) {
        HwFingerprintService.super.serviceDied(cookie);
        this.mDaemonEx = null;
        HwFpServiceToHalUtils.destroyDaemonEx();
        Log.w(TAG, "serviceDied ");
    }

    public BiometricsFingerprintEx getFingerprintDaemonEx() {
        BiometricsFingerprintEx biometricsFingerprintEx = this.mDaemonEx;
        if (biometricsFingerprintEx != null) {
            return biometricsFingerprintEx;
        }
        this.mDaemonEx = HwFingerprintService.super.getFingerprintDaemonEx();
        Log.w(TAG, "getFingerprintDaemonEx inst = " + this.mDaemonEx);
        return this.mDaemonEx;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean inLockoutMode() {
        int failedAttempts = getFailedAttempts().get(ActivityManagerEx.getCurrentUser(), 0);
        Log.w(TAG, "inLockoutMode failedAttempts= " + failedAttempts);
        if (failedAttempts >= getMaxFailedAttemptsLockoutTimed()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean canUseBiometric(String opPackageName) {
        return canUseBiometric(opPackageName, true, Binder.getCallingUid(), Binder.getCallingPid(), UserHandleEx.getCallingUserId());
    }

    /* access modifiers changed from: protected */
    public boolean canUseBiometric(String opPackageName, boolean isRequireForeground, int uid, int pid, int userId, boolean isDetected) {
        if (opPackageName == null || BuildConfig.FLAVOR.equals(opPackageName)) {
            Log.i(TAG, "opPackageName is null or opPackageName is invalid");
            return false;
        }
        this.mOpPackageName = opPackageName;
        if (FIDO_ASM.equals(opPackageName) || "com.huawei.securitymgr".equals(opPackageName) || "com.huawei.aod".equals(opPackageName)) {
            return true;
        }
        if (!isDetected || !PKGNAME_OF_WECHAT.equals(opPackageName)) {
            return HwFingerprintService.super.canUseBiometric(opPackageName, isRequireForeground, uid, pid, userId, isDetected);
        }
        return true;
    }

    private int getKidsFingerId(String whichMode, int userId, Context context) {
        if (context != null) {
            return SettingsEx.Secure.getIntForUser(context.getContentResolver(), whichMode, 0, userId);
        }
        Log.w(TAG, "getkidsFingerId - context = null");
        return 0;
    }

    private boolean isKidSwitchOn(int userId, Context context) {
        return SettingsEx.Secure.getIntForUser(context.getContentResolver(), KEY_DB_CHILDREN_MODE_STATUS, 0, userId) == 1;
    }

    private boolean isParentControl(int userId, Context context) {
        if (context == null || context.getContentResolver() == null) {
            return false;
        }
        int status = SettingsEx.Secure.getIntForUser(context.getContentResolver(), PATH_CHILDMODE_STATUS, 0, userId);
        Log.i(TAG, "ParentControl status is " + status);
        if (status == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void setKidsFingerprint(int userId, boolean isKeyguard) {
        Log.d(TAG, "setKidsFingerprint:start");
        int kidFpId = getKidsFingerId(KEY_DB_CHILDREN_MODE_FPID, userId, this.mContext);
        if (kidFpId != 0) {
            boolean isParent = isParentControl(userId, this.mContext);
            boolean isPcCastMode = HwPCUtils.isPcCastModeInServer();
            if (isKeyguard && isKidSwitchOn(userId, this.mContext) && !isParent && !isPcCastMode) {
                kidFpId = 0;
            }
            Log.i(TAG, "setKidsFingerprint-kidFpId = " + kidFpId);
            BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
            if (daemon == null) {
                Log.e(TAG, "Fingerprintd is not available!");
                return;
            }
            daemon.setKidsFingerprintEx(kidFpId);
            Log.i(TAG, "framework setKidsFingerprint is ok ---end");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startAuthentication(ClientMonitorParameterEx clientMonitorParameterEx) {
        updateActiveGroup(clientMonitorParameterEx.getGroupId(), clientMonitorParameterEx.getOwner());
        Log.i(TAG, "HwFingerprintService-startAuthentication(" + clientMonitorParameterEx.getOwner() + ")");
        AuthenticationClientEx client = new HwFIDOAuthenticationClient(clientMonitorParameterEx);
        if (!inLockoutMode() || isKeyguard(clientMonitorParameterEx.getOwner())) {
            startAuthenticationClientEx(this, getClass().getSuperclass().getSuperclass().getSuperclass(), client);
            return;
        }
        Log.i(TAG, "In lockout mode; disallowing authentication");
        if (!client.onError(0, 7, 0)) {
            Log.w(TAG, "Cannot send timeout message to client");
        }
    }

    /* access modifiers changed from: protected */
    public Object invokeParentPrivateFunction(Object instance, Class targetClass, String method, Class[] paramTypes, Object[] params) {
        Object superInst = targetClass.cast(instance);
        try {
            final Method med = targetClass.getDeclaredMethod(method, paramTypes);
            AccessController.doPrivileged(new PrivilegedAction() {
                /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass15 */

                @Override // java.security.PrivilegedAction
                public Object run() {
                    med.setAccessible(true);
                    return null;
                }
            });
            return med.invoke(superInst, params);
        } catch (Exception e) {
            Log.v(TAG, "invokeParentPrivateFunction error", e);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void notifyEnrollCanceled() {
        notifyEnrollmentCanceled();
    }

    /* access modifiers changed from: protected */
    public void handleAcquired(long deviceId, int acquiredInfo, int vendorCode) {
        HwFingerprintService.super.stopPickupTrunOff();
        HwFingerprintService.super.handleAcquired(deviceId, acquiredInfo, vendorCode);
        if ((acquiredInfo == getFingerprintAcquiredVendor() ? getFingerprintAcquiredVendorBase() + vendorCode : acquiredInfo) == 2002) {
            this.mLastFingerDownTime = System.currentTimeMillis();
            this.mIsBlackAuthenticateEvent = !this.mPowerManager.isInteractive();
            getHandler().removeCallbacks(this.mBlackAuthenticateEventResetRunable);
            Log.i(TAG, "powerfp mLastFingerDownTime=" + this.mLastFingerDownTime + " mIsBlackAuthenticateEvent=" + this.mIsBlackAuthenticateEvent);
        }
    }

    /* access modifiers changed from: protected */
    public boolean handleEnrollResultEx(BiometricAuthenticatorEx identifier, int remaining) {
        notifyEnrollingFingerUp();
        Log.w(TAG, "handleEnrollResultEx");
        if (remaining == 0) {
            BiometricsFingerprintEx daemon = getBiometricsFingerprintDaemon();
            if (daemon == null) {
                return false;
            }
            daemon.removeEx(identifier, ActivityManagerEx.getCurrentUser());
        }
        return HwFingerprintService.super.handleEnrollResultEx(identifier, remaining);
    }

    /* access modifiers changed from: protected */
    public void notifyAuthCanceled(String topPackage) {
        notifyAuthenticationCanceled(topPackage);
    }

    /* access modifiers changed from: protected */
    public FingerprintServiceEx.FingerprintAuthClientEx creatAuthenticationClientEx(ClientMonitorParameterEx clientMonitorParameterEx) {
        Log.i(TAG, "FingerprintAuthClientEx creatAuthenticationClientEx");
        return new HwFingerprintAuthClient(clientMonitorParameterEx);
    }

    public void handleHwFailedAttempt(int flags, String packagesName) {
        if ((HW_FP_NO_COUNT_FAILED_ATTEMPS & flags) == 0 || !"com.android.settings".equals(packagesName)) {
            this.mHwFailedAttempts++;
        } else {
            Log.i(TAG, "no need count hw failed attempts");
        }
    }

    /* access modifiers changed from: protected */
    public void resetFailedAttemptsForUser(boolean isClearAttemptCounter, int userId) {
        HwFingerprintService.super.resetFailedAttemptsForUser(isClearAttemptCounter, userId);
        this.mHwFailedAttempts = 0;
    }

    /* access modifiers changed from: protected */
    public void addHighlightOnAcquired(int acquiredInfo, int vendorCode) {
        FingerprintDataInterface fingerprintDataInterface;
        FingerprintDataInterface fingerprintDataInterface2;
        int clientAcquireInfo = acquiredInfo == getFingerprintAcquiredVendor() ? getFingerprintAcquiredVendorBase() + vendorCode : acquiredInfo;
        if (clientAcquireInfo == 2002 && (fingerprintDataInterface2 = this.mFpDataCollector) != null) {
            fingerprintDataInterface2.reportFingerDown();
        } else if (clientAcquireInfo == 0 && (fingerprintDataInterface = this.mFpDataCollector) != null) {
            fingerprintDataInterface.reportCaptureCompleted();
        }
        if (clientAcquireInfo == 2002) {
            this.mDownTime = System.currentTimeMillis();
        }
        if (clientAcquireInfo == 2002 && isKeyguardCurrentClient()) {
            FingerprintSupportEx.logPower();
        }
        ClientMonitorEx clientMonitor = getCurrentClientEx();
        if (clientMonitor == null) {
            Log.e(TAG, "mCurrentClient is null notifyFinger failed");
            setKeyguardAuthenStatusForce(false);
            return;
        }
        if (clientAcquireInfo == 2002) {
            Log.i(TAG, "onAcquired set mCurrentAuthFpDev DEVICE_BACK");
            this.mCurrentAuthFpDev = FingerprintUtilsEx.DEVICE_BACK;
        }
        if (isSupportPowerFp() && currentClient(HwFingerprintService.super.getKeyguardPackage())) {
            if (clientAcquireInfo == 2002) {
                setKeyguardAuthenStatus(true);
            } else if (clientAcquireInfo == HUAWEI_FINGERPRINT_UP) {
                setNoWaitPowerEvent();
                errorTouchOnAcquired();
            }
        }
        handleClientAcquireInfo(clientAcquireInfo, clientMonitor);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void errorTouchOnAcquired() {
        if (this.mIsReadyWakeUp) {
            Log.i(TAG, "errorTouchOnAcquired start wakeup");
            getHandler().removeCallbacks(this.mPowerFingerWakeUpRunable);
            getHandler().post(this.mPowerFingerWakeUpRunable);
        }
    }

    private void handleClientAcquireInfo(int clientAcquireInfo, ClientMonitorEx clientMonitor) {
        String currentOpName = clientMonitor.getOwnerString();
        if (clientAcquireInfo == HUAWEI_FINGERPRINT_DOWN_UD) {
            if (clientMonitor.isAuthenticationClient()) {
                Log.i(TAG, "notify that AuthenticationClient finger down:" + currentOpName);
                this.mCurrentAuthFpDev = FingerprintUtilsEx.DEVICE_UD;
                if ("com.android.systemui".equals(currentOpName)) {
                    notifyFingerDown(3);
                } else {
                    notifyFingerDown(1);
                }
            } else if (clientMonitor.isEnrollClient()) {
                Log.i(TAG, "notify that EnrollClientEx finger down");
                notifyFingerDown(0);
            }
        } else if (clientAcquireInfo == 5 || clientAcquireInfo == 1 || clientAcquireInfo == HUAWEI_FINGERPRINT_UP) {
            if (clientAcquireInfo == 5) {
                Log.i(TAG, "FINGERPRINT_ACQUIRED_TOO_FAST notifyCaptureFinished");
                notifyCaptureFinished(1);
            }
            if (clientMonitor.isAuthenticationClient()) {
                Log.i(TAG, "clientAcquireInfo = " + clientAcquireInfo);
                notifyAuthenticationFinished(currentOpName, 2, this.mHwFailedAttempts);
            }
            setKeyguardAuthenStatus(false);
        } else if (clientAcquireInfo == 0) {
            if (!clientMonitor.isAuthenticationClient()) {
                return;
            }
            if ("com.android.settings".equals(currentOpName)) {
                notifyCaptureFinished(2);
            } else {
                notifyCaptureFinished(1);
            }
        } else if (clientAcquireInfo == HUAWEI_FINGERPRINT_TRIGGER_FACE_RECOGNIZATION) {
            Log.i(TAG, "clientAcquireInfo = " + clientAcquireInfo);
            HwFingerprintService.super.triggerFaceRecognization();
        }
    }

    /* access modifiers changed from: protected */
    public void handleErrorEx(long deviceId, int error, int vendorCode) {
        Log.i(TAG, "handleErrorEx = " + vendorCode + ",error=" + error);
        int tempVendorCode = vendorCode;
        ClientMonitorEx client = getCurrentClientEx();
        if (client != null && client.isEnrollClient()) {
            notifyEnrollmentCanceled();
        }
        if (error == ERROR_CODE_COMMEN_ERROR && tempVendorCode > BASE_BRIGHTNESS) {
            int tempVendorCode2 = tempVendorCode - 3000;
            int i = MAX_BRIGHTNESS;
            if (tempVendorCode2 < MAX_BRIGHTNESS) {
                i = tempVendorCode2;
            }
            tempVendorCode = i;
            Log.w(TAG, "change brightness to " + tempVendorCode);
            notifyFingerCalibrarion(tempVendorCode);
        }
        if (client != null && "com.android.systemui".equals(client.getOwnerString())) {
            clearRepeatAuthentication();
        }
        HwFingerprintService.super.handleErrorEx(deviceId, error, tempVendorCode);
    }

    /* access modifiers changed from: protected */
    public boolean removeInternalEx(FingerprintServiceEx.RemovalClientEx client) {
        if (client == null || client.getListener() == null) {
            Log.w(TAG, "startRemove: receiver is null");
            return false;
        }
        if (FingerprintUtilsEx.getInstance().isDualFp() && client.getBiometricId() != 0) {
            List<FingerprintEx> finerprints = FingerprintUtilsEx.getInstance().getFingerprintsForUser(getContextEx(), client.getTargetUserId(), FingerprintUtilsEx.DEVICE_UD);
            int fingerprintSize = finerprints.size();
            int i = 0;
            while (true) {
                if (i >= fingerprintSize) {
                    break;
                } else if (finerprints.get(i).getBiometricId() == client.getBiometricId()) {
                    Log.i(TAG, "dualFingerprint send MSG_REMOVE_UD");
                    HwFpServiceToHalUtils.sendCommandToHal(FingerprintUtilsEx.MSG_REMOVE_UD);
                    break;
                } else {
                    i++;
                }
            }
        }
        if (!FingerprintUtilsEx.getInstance().isDualFp() || client.getBiometricId() != 0) {
            return true;
        }
        Log.i(TAG, "dualFingerprint send MSG_REMOVE_ALL");
        HwFpServiceToHalUtils.sendCommandToHal(FingerprintUtilsEx.MSG_REMOVE_ALL);
        return true;
    }

    /* access modifiers changed from: protected */
    public void udFingerprintAllRemovedEx(FingerprintServiceEx.RemovalClientEx client, int groupId) {
        if (FingerprintUtilsEx.getInstance().isDualFp() && client.isRemovalClient() && (client instanceof FingerprintServiceEx.RemovalClientEx)) {
            FingerprintUtilsEx fingerUtil = FingerprintUtilsEx.getInstance();
            boolean isHasUdFingerprints = true;
            boolean isHasFingerprints = fingerUtil.getFingerprintsForUser(this.mContext, groupId, FingerprintUtilsEx.DEVICE_ALL).size() > 0;
            if (fingerUtil.getFingerprintsForUser(this.mContext, groupId, FingerprintUtilsEx.DEVICE_UD).size() <= 0) {
                isHasUdFingerprints = false;
            }
            if (!isHasUdFingerprints) {
                HwFpServiceToHalUtils.sendCommandToHal(0);
                Log.i(TAG, "UDFingerprint all removed so TP CLOSE");
            }
            if (client.getBiometricId() == 0 && isHasFingerprints) {
                Log.i(TAG, "dualFingerprint-> handleRemoved, but do not destory client.");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dualFingerprintStartAuth(int flags, String opPackageName) {
        if (FingerprintUtilsEx.getInstance().isDualFp()) {
            Log.i(TAG, "dualFingerprint startAuthentication and flag is: " + flags);
            if (flags == 0) {
                if (canUseUdFingerprint(opPackageName)) {
                    Log.i(TAG, "dualFingerprint send MSG_AUTH_ALL");
                    HwFpServiceToHalUtils.sendCommandToHal(FingerprintUtilsEx.MSG_AUTH_ALL);
                }
            } else if ((FLAG_USE_UD_FINGERPRINT & flags) == 0) {
            } else {
                if ((67108864 & flags) != 0) {
                    Log.i(TAG, "dualFingerprint send MSG_AUTH_ALL");
                    HwFpServiceToHalUtils.sendCommandToHal(FingerprintUtilsEx.MSG_AUTH_ALL);
                    return;
                }
                Log.i(TAG, "dualFingerprint send MSG_AUTH_UD");
                HwFpServiceToHalUtils.sendCommandToHal(FingerprintUtilsEx.MSG_AUTH_UD);
            }
        }
    }

    public void enrollInternalEx(FingerprintServiceEx.EnrollClientImplEx client, int userId, int flags, String opPackageName) {
        boolean isDualFp = FingerprintUtilsEx.getInstance().isDualFp();
        if (isDualFp) {
            HwFingerprintService.super.setEnrolled(getEnrolledFingerprintsEx(opPackageName, flags == 4096 ? FingerprintUtilsEx.DEVICE_UD : FingerprintUtilsEx.DEVICE_BACK, userId).size());
        }
        if (!HwFingerprintService.super.hasReachedEnrollmentLimit(userId)) {
            boolean isPrivacyUser = checkPrivacySpaceEnroll(userId, ActivityManagerEx.getCurrentUser());
            if (isCurrentUserOrProfile(userId) || isPrivacyUser) {
                updateActiveGroup(userId, opPackageName);
                client.setGroupId(userId);
                if (isDualFp && "com.android.settings".equals(opPackageName)) {
                    int targetDevice = flags == 4096 ? FingerprintUtilsEx.DEVICE_UD : FingerprintUtilsEx.DEVICE_BACK;
                    Log.i(TAG, "dualFingerprint enroll targetDevice is: " + targetDevice);
                    if (targetDevice == FingerprintUtilsEx.DEVICE_UD) {
                        Log.i(TAG, "dualFingerprint send MSG_ENROLL_UD");
                        HwFpServiceToHalUtils.sendCommandToHal(FingerprintUtilsEx.MSG_ENROLL_UD);
                        client.setTargetDevice(FingerprintUtilsEx.DEVICE_UD);
                    }
                }
                notifyEnrollmentStarted(flags);
                return;
            }
            Flog.w(1303, "user invalid enroll error");
        }
    }

    public FingerprintServiceEx.FingerprintServiceWrapperEx creatFingerprintServiceWrapper() {
        return new HwFingerprintServiceWrapper();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isHwTransactInterest(int code) {
        if (this.mCodeList.contains(Integer.valueOf(code))) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isClonedProfile(int userId) {
        long token = Binder.clearCallingIdentity();
        try {
            UserInfoExt userInfoExt = UserManagerExt.getUserInfoEx(UserManagerExt.get(this.mContext), userId);
            return userInfoExt != null && userInfoExt.isClonedProfile();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isFingerprintTransactInterest(int code) {
        if (code == CODE_FINGERPRINT_LOGO_RADIUS) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean onHwFingerprintTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (onTransactToHal(code, data, reply, flags)) {
            return true;
        }
        return onHwTransact(code, data, reply, flags);
    }

    private final class HwFingerprintServiceWrapper extends FingerprintServiceEx.FingerprintServiceWrapperEx {
        private HwFingerprintServiceWrapper() {
            super(HwFingerprintService.this);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            try {
                if (HwFingerprintService.this.isHwTransactInterest(code)) {
                    HwFingerprintService.this.checkPermissions();
                    return HwFingerprintService.this.onHwTransact(code, data, reply, flags);
                } else if (!HwFingerprintService.this.isFingerprintTransactInterest(code)) {
                    return HwFingerprintService.super.onTransact(code, data, reply, flags);
                } else {
                    HwFingerprintService.this.checkPermissions();
                    return HwFingerprintService.this.onHwFingerprintTransact(code, data, reply, flags);
                }
            } catch (RemoteException e) {
                Log.w(HwFingerprintService.TAG, "onTransact RemoteException");
                return false;
            }
        }

        public long preEnroll(IBinder token) {
            Flog.i(1303, "FingerprintService preEnroll");
            return HwFingerprintService.super.preEnroll(token);
        }

        public int postEnroll(IBinder token) {
            Flog.i(1303, "FingerprintService postEnroll");
            return HwFingerprintService.super.postEnroll(token);
        }

        public void enrollEx(FingerprintParameterEx enrollExParameter) {
            Flog.i(1303, "FingerprintService enroll");
        }

        public void cancelEnrollment(IBinder token) {
            Flog.i(1303, "FingerprintService cancelEnrollment");
            HwFingerprintService.super.cancelEnrollment(token);
            HwFingerprintService.this.notifyEnrollmentCanceled();
        }

        public void authenticateEx(FingerprintParameterEx fingerprintParameterEx) {
            int hwGroupId;
            if (HwFingerprintService.this.isKeyguard(fingerprintParameterEx.getOpPackageName()) && FingerprintViewUtils.isApModeAodEnable(HwFingerprintService.this.mContext)) {
                Log.i(HwFingerprintService.TAG, "Init FingerprintStateNotifierEx before authenticate");
                HwFingerprintService.this.getHandler().post(HwFingerprintService.this.mFingerprintStateNotifierExInit);
            }
            if (HwFingerprintService.this.isKeyguard(fingerprintParameterEx.getOpPackageName())) {
                FingerprintController.getInstance().setCurrentPackageName("com.android.systemui");
            }
            Flog.i(1303, "FingerprintService authenticate:" + fingerprintParameterEx.getOpPackageName());
            if (!HwFingerprintService.PKGNAME_OF_SYSTEM_MANAGER.equals(fingerprintParameterEx.getOpPackageName()) || !HwFingerprintService.this.isKeyguardLocked()) {
                if (fingerprintParameterEx.getGroupId() == 0 || !HwFingerprintService.this.isClonedProfile(fingerprintParameterEx.getGroupId())) {
                    hwGroupId = fingerprintParameterEx.getGroupId();
                } else {
                    Log.i(HwFingerprintService.TAG, "Clone profile authenticate,change userid to 0");
                    hwGroupId = 0;
                    fingerprintParameterEx.setGroupId(0);
                }
                if (!HwFingerprintService.this.canUseBiometric(fingerprintParameterEx.getOpPackageName())) {
                    Log.i(HwFingerprintService.TAG, "authenticate reject opPackageName:" + fingerprintParameterEx.getOpPackageName());
                    return;
                }
                HwFingerprintService.this.setLivenessSwitch(fingerprintParameterEx.getOpPackageName());
                if (HwFingerprintService.this.mIsSupportKids) {
                    Log.i(HwFingerprintService.TAG, "mIsSupportKids=" + HwFingerprintService.this.mIsSupportKids);
                    HwFingerprintService.this.setKidsFingerprint(fingerprintParameterEx.getGroupId(), HwFingerprintService.this.isKeyguard(fingerprintParameterEx.getOpPackageName()));
                }
                HwFingerprintService.this.notifyAuthenticationStarted(fingerprintParameterEx.getOpPackageName(), fingerprintParameterEx.getReceiver(), fingerprintParameterEx.getFlags(), hwGroupId, null, false);
                return;
            }
            Log.w(HwFingerprintService.TAG, "interrupt authenticate in keyguard locked");
        }

        public void cancelAuthentication(IBinder token, String opPackageName) {
            HwFingerprintService.super.cancelAuthentication(token, opPackageName);
            HwFingerprintService.this.notifyAuthenticationCanceled(opPackageName);
        }

        public void removeEx(IBinder token, int fingerId, int groupId, int userId, BiometricServiceReceiverListenerEx receiver) {
            Flog.i(1303, "FingerprintService remove");
        }

        public void rename(int fingerId, int groupId, String name) {
            Flog.i(1303, "FingerprintService rename");
            HwFingerprintService.super.rename(fingerId, groupId, name);
        }

        public List<FingerprintEx> getEnrolledFingerprintsWrapper(int userId, String opPackageName) {
            Flog.i(1303, "FingerprintService getEnrolledFingerprints");
            if (!HwFingerprintService.this.canUseBiometric(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandleEx.getCallingUserId(), false)) {
                return Collections.emptyList();
            }
            if (!FingerprintUtilsEx.getInstance().isDualFp()) {
                return HwFingerprintService.super.getEnrolledFingerprintsWrapper(userId, opPackageName);
            }
            Log.d(HwFingerprintService.TAG, "dualFingerprint getEnrolledFingerprints and userId is: " + userId);
            return HwFingerprintService.this.getEnrolledFingerprintsEx(opPackageName, FingerprintUtilsEx.DEVICE_ALL, userId);
        }

        public boolean hasEnrolledFingerprints(int userId, String opPackageName) {
            int tempUserId = userId;
            Flog.i(1303, "FingerprintService hasEnrolledFingerprints");
            boolean isHasEnrollFingerprints = false;
            if (!HwFingerprintService.this.canUseBiometric(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandleEx.getCallingUserId(), false)) {
                return false;
            }
            if (FingerprintUtilsEx.getInstance().isDualFp()) {
                Log.i(HwFingerprintService.TAG, "dualFingerprint hasEnrolledFingerprints and userId is: " + userId);
                if (HwFingerprintService.this.getEnrolledFingerprintsEx(opPackageName, FingerprintUtilsEx.DEVICE_ALL, tempUserId).size() > 0) {
                    isHasEnrollFingerprints = true;
                }
                Log.i(HwFingerprintService.TAG, "dualFingerprint hasEnrolledFingerprints: " + isHasEnrollFingerprints);
                return isHasEnrollFingerprints;
            }
            if (tempUserId != 0 && HwFingerprintService.this.isClonedProfile(tempUserId)) {
                Log.i(HwFingerprintService.TAG, "Clone profile get Enrolled Fingerprints,change userid to 0");
                tempUserId = 0;
            }
            return HwFingerprintService.super.hasEnrolledFingerprints(tempUserId, opPackageName);
        }

        public long getAuthenticatorId(String opPackageName) {
            Flog.i(1303, "FingerprintService getAuthenticatorId");
            return HwFingerprintService.super.getAuthenticatorId(opPackageName);
        }

        public int getRemainingNum() {
            HwFingerprintService.this.checkPermissions();
            long token = Binder.clearCallingIdentity();
            int failedAttempts = 0;
            try {
                failedAttempts = HwFingerprintService.this.getFailedAttempts().get(ActivityManagerEx.getCurrentUser(), 0);
            } catch (SecurityException e) {
                Log.e(HwFingerprintService.TAG, "failed getCurrentUser");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
            Log.i(HwFingerprintService.TAG, " Remaining Num Attempts = " + (5 - failedAttempts));
            return 5 - failedAttempts;
        }

        public long getRemainingTime() {
            HwFingerprintService.this.checkPermissions();
            long now = SystemClock.elapsedRealtime();
            long nowToLockout = now - HwFingerprintService.this.getLockoutTime().get(ActivityManagerEx.getCurrentUser(), 0);
            Log.i(HwFingerprintService.TAG, "Remaining Time mLockoutTime = " + HwFingerprintService.this.getLockoutTime() + "  now = " + now);
            if (nowToLockout <= 0 || nowToLockout >= HwFingerprintService.this.getFailLockoutTimeoutMs()) {
                return 0;
            }
            return HwFingerprintService.this.getFailLockoutTimeoutMs() - nowToLockout;
        }

        public void addLockoutResetCallbackEx(BiometricServiceReceiverListenerEx callback) throws RemoteException {
            if (callback == null) {
                Log.e(HwFingerprintService.TAG, " FingerprintServiceLockoutResetCallback is null, cannot addLockoutResetMonitor, return");
            }
        }
    }

    private final class HwFingerprintAuthClient extends FingerprintServiceEx.FingerprintAuthClientEx {
        public HwFingerprintAuthClient(ClientMonitorParameterEx clientMonitorParameterEx) {
            super(HwFingerprintService.this, clientMonitorParameterEx);
        }

        public void handleHwFailedAttempt(int flags, String packagesName) {
            HwFingerprintService.this.handleHwFailedAttempt(flags, packagesName);
        }

        public boolean onAuthenticatedEx(BiometricAuthenticatorEx identifier, boolean isAuthenticated, ArrayList<Byte> token) {
            HwFingerprintService hwFingerprintService = HwFingerprintService.this;
            if (hwFingerprintService.isKeyguard(hwFingerprintService.mOpPackageName) && FingerprintViewUtils.isFingerprintEnabledInAppMode(HwFingerprintService.this.mContext)) {
                FingerprintStateNotifierEx.getInstance(HwFingerprintService.this.mContext).notifyStateChange(isAuthenticated ? 2 : 3);
            }
            BiometricServiceReceiverListenerEx listener = geServicetListener();
            Log.e(HwFingerprintService.TAG, "onAuthenticatedEx,listener = " + listener);
            if (listener != null) {
                if (!isAuthenticated) {
                    Log.e(HwFingerprintService.TAG, "onAuthenticated,fail ,mHwFailedAttempts = " + HwFingerprintService.this.mHwFailedAttempts);
                    HwFingerprintService.this.notifyAuthenticationFinished(getPackageName(), 1, HwFingerprintService.this.mHwFailedAttempts + 1);
                    if (HwFingerprintService.this.getAuTime() - HwFingerprintService.this.mDownTime > 0) {
                        Context context = HwFingerprintService.this.mContext;
                        Flog.bdReport(context, 991310007, "{CostTime:" + (HwFingerprintService.this.getAuTime() - HwFingerprintService.this.mDownTime) + ",pkg:" + HwFingerprintService.this.mOpPackageName + ",DeviceType:" + HwFingerprintService.this.mCurrentAuthFpDev + "}");
                        Log.i(HwFingerprintService.TAG, "onAuthenticated fail:{CostTime:" + (HwFingerprintService.this.getAuTime() - HwFingerprintService.this.mDownTime) + ",pkg:" + HwFingerprintService.this.mOpPackageName + ",DeviceType:" + HwFingerprintService.this.mCurrentAuthFpDev + "}");
                    } else {
                        Log.i(HwFingerprintService.TAG, "Fingerprint authenticate time less than equal to or equal to Fingerprint down time");
                    }
                    HwFingerprintService.this.errorTouchOnAcquired();
                    if (isScreenOn(HwFingerprintService.this.getContextEx())) {
                        handleHwFailedAttempt(getFlags(), getPackageName());
                    }
                    HwFingerprintService.this.mStatusBarManagerServiceEx.onBiometricHelp(HwFingerprintService.this.getContextEx().getResources().getString(HwPartResourceUtils.getResourceId("fingerprint_recognized_fail")));
                } else {
                    Log.e(HwFingerprintService.TAG, "onAuthenticated, pass");
                    HwFingerprintService.this.notifyAuthenticationFinished(getPackageName(), 0, 0);
                    Context context2 = HwFingerprintService.this.mContext;
                    Flog.bdReport(context2, 991310008, "{pkg:" + HwFingerprintService.this.mOpPackageName + ",ErrorCount:" + HwFingerprintService.this.mHwFailedAttempts + ",DeviceType:" + HwFingerprintService.this.mCurrentAuthFpDev + "}");
                    Log.i(HwFingerprintService.TAG, "onAuthenticated success:{pkg:" + HwFingerprintService.this.mOpPackageName + ",ErrorCount:" + HwFingerprintService.this.mHwFailedAttempts + ",DeviceType:" + HwFingerprintService.this.mCurrentAuthFpDev + "}");
                    HwFingerprintService.this.mStatusBarManagerServiceEx.onBiometricAuthenticated(isAuthenticated, (String) null);
                    FaceDetectManager.getInstance().cancelFaceAuth();
                }
            }
            return HwFingerprintService.super.onAuthenticatedEx(identifier, isAuthenticated, token);
        }

        public int handleFailedAttempt() {
            int result = HwFingerprintService.super.handleFailedAttempt();
            boolean isIgnoreFailedAttemps = false;
            if ((getFlags() & HwFingerprintService.HW_FP_NO_COUNT_FAILED_ATTEMPS) != 0 && "com.android.settings".equals(getOwnerStringEx())) {
                isIgnoreFailedAttemps = true;
                Log.i(HwFingerprintService.TAG, "no need count failed attempts");
            }
            int currentUser = ActivityManagerEx.getCurrentUser();
            if (isIgnoreFailedAttemps) {
                HwFingerprintService.this.getFailedAttempts().put(currentUser, HwFingerprintService.this.getFailedAttempts().get(currentUser, 0) - 1);
            }
            if (result == 0 || !HwFingerprintService.this.isKeyguard(getPackageName())) {
                return result;
            }
            return 0;
        }

        public boolean inLockoutMode() {
            return HwFingerprintService.this.inLockoutMode();
        }

        public void resetFailedAttempts() {
            if (inLockoutMode()) {
                Log.i(HwFingerprintService.TAG, "resetFailedAttempts should be called from APP");
            } else {
                HwFingerprintService.super.resetFailedAttempts();
            }
        }

        public void onStart() {
            HwFingerprintService.super.onStart();
        }

        public void onStop() {
            HwFingerprintService.this.notifyAuthenticationCanceled(getPackageName());
            HwFingerprintService.super.onStop();
        }
    }

    /* access modifiers changed from: protected */
    public void stopFidoClient(BiometricServiceReceiverListenerEx receiver) {
        ClientMonitorEx currentClient = getCurrentClientEx();
        if (currentClient != null) {
            if (!currentClient.isAuthenticationClient()) {
                Log.i(TAG, "can't cancel non-authenticating client" + currentClient.getOwnerString());
            } else if (currentClient.getToken() == receiver.asBinder()) {
                Log.i(TAG, "stop client " + currentClient.getOwnerString());
                currentClient.stop(true);
            } else {
                Log.i(TAG, "can't stop client " + currentClient.getOwnerString() + " since tokens don't match");
            }
        }
    }

    /* access modifiers changed from: private */
    public class FingerViewChangeCallback implements FingerViewController.ICallBack {
        private FingerViewChangeCallback() {
        }

        @Override // com.huawei.server.fingerprint.FingerViewController.ICallBack
        public void onFingerViewStateChange(int type) {
            Log.i(HwFingerprintService.TAG, "View State Change to " + type);
            int i = 0;
            if (type == 3) {
                ClientMonitorEx clientMonitorEx = HwFingerprintService.this.getCurrentClientEx();
                if (clientMonitorEx != null) {
                    String packageOwnerName = BuildConfig.FLAVOR;
                    if (clientMonitorEx.getOwnerString() != null) {
                        packageOwnerName = new String(clientMonitorEx.getOwnerString());
                    }
                    Log.w(HwFingerprintService.TAG, "onError cancel");
                    clientMonitorEx.stop(false);
                    HwFingerprintService.this.notifyAuthCanceled(packageOwnerName);
                    return;
                }
                return;
            }
            HwFingerprintService hwFingerprintService = HwFingerprintService.this;
            if (type == 2) {
                i = 1;
            }
            hwFingerprintService.suspendAuthentication(i);
        }

        @Override // com.huawei.server.fingerprint.FingerViewController.ICallBack
        public void onNotifyCaptureImage() {
            HwFingerprintService.this.notifyCaptureOpticalImage();
        }

        @Override // com.huawei.server.fingerprint.FingerViewController.ICallBack
        public void onNotifyBlueSpotDismiss() {
            HwFingerprintService.this.notifyBluespotDismiss();
        }
    }

    /* access modifiers changed from: private */
    public class HwFIDOAuthenticationClient extends AuthenticationClientEx {
        private String mAaid;
        private int mGroupId;
        private BiometricServiceReceiverListenerEx mListener;
        private byte[] mNonces;
        private String mPkgName;
        private int mUserId;

        public void onUserVerificationResult(final int result, long opId, final ArrayList<Byte> userId, final ArrayList<Byte> encapsulatedResult) {
            Log.i(HwFingerprintService.TAG, "onUserVerificationResult");
            HwFingerprintService.this.getHandler().post(new Runnable() {
                /* class com.android.server.fingerprint.HwFingerprintService.HwFIDOAuthenticationClient.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    Log.i(HwFingerprintService.TAG, "onUserVerificationResult-run");
                    HwFingerprintService.this.resumeTouchSwitch();
                    if (HwFIDOAuthenticationClient.this.mListener != null) {
                        byte[] byteUserIds = new byte[userId.size()];
                        int userIdLen = userId.size();
                        for (int i = 0; i < userIdLen; i++) {
                            byteUserIds[i] = ((Byte) userId.get(i)).byteValue();
                        }
                        byte[] byteEncapsulatedResults = new byte[encapsulatedResult.size()];
                        int encapsulatedResultLen = encapsulatedResult.size();
                        for (int i2 = 0; i2 < encapsulatedResultLen; i2++) {
                            byteEncapsulatedResults[i2] = ((Byte) encapsulatedResult.get(i2)).byteValue();
                        }
                        HwFIDOAuthenticationClient.this.mListener.onUserVerificationResult(result, byteUserIds, byteEncapsulatedResults);
                    }
                }
            });
        }

        public HwFIDOAuthenticationClient(ClientMonitorParameterEx clientMonitorParameterEx) {
            super(clientMonitorParameterEx);
            this.mPkgName = clientMonitorParameterEx.getOwner();
            this.mListener = clientMonitorParameterEx.getListener();
            this.mGroupId = clientMonitorParameterEx.getGroupId();
            this.mAaid = clientMonitorParameterEx.getAaid();
            this.mNonces = clientMonitorParameterEx.getNonce();
            this.mUserId = clientMonitorParameterEx.getCallingUserId();
        }

        public boolean wasUserDetected() {
            return false;
        }

        public boolean onAuthenticatedEx(BiometricAuthenticatorEx identifier, boolean authenticated, ArrayList<Byte> arrayList) {
            if (!authenticated) {
                if (isScreenOn(HwFingerprintService.this.getContextEx())) {
                    handleHwFailedAttempt(getFlags(), this.mPkgName);
                }
                HwFingerprintService.this.mStatusBarManagerServiceEx.onBiometricHelp(HwFingerprintService.this.getContextEx().getResources().getString(HwPartResourceUtils.getResourceId("fingerprint_recognized_fail")));
                return true;
            }
            HwFingerprintService.this.mStatusBarManagerServiceEx.onBiometricAuthenticated(authenticated, (String) null);
            return true;
        }

        public String getErrorString(int error, int vendorCode) {
            return HwFingerprintService.super.getErrorString(error, vendorCode);
        }

        public String getAcquiredString(int acquireInfo, int vendorCode) {
            return HwFingerprintService.super.getAcquiredString(acquireInfo, vendorCode);
        }

        public int getBiometricType() {
            return HwFingerprintService.super.getBiometricType();
        }

        public boolean shouldFrameworkHandleLockout() {
            return true;
        }

        /* access modifiers changed from: protected */
        public int statsModality() {
            return 0;
        }

        public int stop(boolean initiatedByClient) {
            return HwFingerprintService.super.stop(initiatedByClient);
        }

        public int handleFailedAttempt() {
            int currentUser = ActivityManagerEx.getCurrentUser();
            HwFingerprintService.this.getFailedAttempts().put(currentUser, HwFingerprintService.this.getFailedAttempts().get(currentUser, 0) + 1);
            HwFingerprintService.this.getTimedLockoutCleared().put(ActivityManagerEx.getCurrentUser(), false);
            int lockoutMode = HwFingerprintService.this.getLockoutMode();
            if (!inLockoutMode()) {
                return 0;
            }
            HwFingerprintService.this.getLockoutTime().put(currentUser, SystemClock.elapsedRealtime());
            Class targetClass = HwFingerprintService.this.getClass().getSuperclass().getSuperclass();
            HwFingerprintService hwFingerprintService = HwFingerprintService.this;
            hwFingerprintService.invokeParentPrivateFunction(hwFingerprintService, targetClass, "scheduleLockoutResetForUser", new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(this.mUserId)});
            onError(getHalDeviceIdEx(), 7, 0);
            stop(true);
            return lockoutMode;
        }

        public void resetFailedAttempts() {
            HwFingerprintService.this.resetFailedAttemptsForUser(true, this.mUserId);
        }

        public void notifyUserActivity() {
            Class targetClass = HwFingerprintService.this.getClass().getSuperclass().getSuperclass().getSuperclass();
            HwFingerprintService hwFingerprintService = HwFingerprintService.this;
            hwFingerprintService.invokeParentPrivateFunction(hwFingerprintService, targetClass, "userActivity", null, null);
        }

        public void handleHwFailedAttempt(int flags, String packagesName) {
            HwFingerprintService.this.handleHwFailedAttempt(0, null);
        }

        public boolean inLockoutMode() {
            return HwFingerprintService.this.inLockoutMode();
        }

        public int start() {
            Log.i(HwFingerprintService.TAG, "start pkgName:" + this.mPkgName);
            try {
                doVerifyUser(this.mGroupId, this.mAaid, this.mNonces);
                return 0;
            } catch (RemoteException e) {
                Log.w(HwFingerprintService.TAG, "call fingerprintD verify user failed");
                return 0;
            }
        }

        public void onStart() {
        }

        public void onStop() {
        }

        private void doVerifyUser(int groupId, String aaid, byte[] nonce) throws RemoteException {
            if (HwFingerprintService.this.isFingerprintDReady()) {
                BiometricsFingerprintEx daemon = HwFingerprintService.this.getFingerprintDaemonEx();
                if (daemon == null) {
                    Log.e(HwFingerprintService.TAG, "Fingerprintd is not available!");
                    return;
                }
                ArrayList<Byte> arrayNonces = new ArrayList<>();
                for (byte b : nonce) {
                    arrayNonces.add(Byte.valueOf(b));
                }
                daemon.verifyUserEx(this.fidoServiceReceiverListenerEx, groupId, aaid, arrayNonces);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showDialog(final boolean isWithConfirm) {
        int i;
        int i2;
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, HwPartResourceUtils.getResourceId("Theme_Emui_Dialog_Alert"));
        if (isWithConfirm) {
            i = HwPartResourceUtils.getResourceId("reenroll_fingerprint_positive_button_message");
        } else {
            i = HwPartResourceUtils.getResourceId("reenroll_fingerprint_button_message");
        }
        AlertDialog.Builder title = builder.setPositiveButton(i, new DialogInterface.OnClickListener() {
            /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass17 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                boolean unused = HwFingerprintService.sIsNeedRecreateDialog = false;
                if (isWithConfirm) {
                    HwFpServiceToHalUtils.sendCommandToHal(HwFingerprintService.MSG_DEL_OLD_TEMPLATES);
                    if (HwFingerprintService.this.checkNeedReEnrollFingerPrints() == 1) {
                        HwFingerprintService hwFingerprintService = HwFingerprintService.this;
                        hwFingerprintService.updateActiveGroupEx(hwFingerprintService.getHiddenSpaceId());
                        HwFingerprintService hwFingerprintService2 = HwFingerprintService.this;
                        hwFingerprintService2.updateActiveGroupEx(hwFingerprintService2.getPrimaryUserId());
                    }
                }
                HwFingerprintService hwFingerprintService3 = HwFingerprintService.this;
                hwFingerprintService3.intentOthers(hwFingerprintService3.mContext);
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass16 */

            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialog) {
                if (!HwFingerprintService.sIsNeedRecreateDialog) {
                    HwFingerprintService.this.unRegisterPhoneStateReceiver();
                }
            }
        }).setTitle(this.mContext.getString(HwPartResourceUtils.getResourceId("tip")));
        Context context = this.mContext;
        if (isWithConfirm) {
            i2 = HwPartResourceUtils.getResourceId("reenroll_fingerprint_message_oversea");
        } else {
            i2 = HwPartResourceUtils.getResourceId("reenroll_fingerprint_message");
        }
        AlertDialog.Builder builder2 = title.setMessage(context.getString(i2));
        if (isWithConfirm) {
            builder2.setNegativeButton(HwPartResourceUtils.getResourceId("cancel"), new DialogInterface.OnClickListener() {
                /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass18 */

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    boolean unused = HwFingerprintService.sIsNeedRecreateDialog = false;
                    HwFingerprintService.this.mReEnrollDialog.dismiss();
                }
            });
        }
        this.mReEnrollDialog = builder2.create();
        AlertDialog alertDialog = this.mReEnrollDialog;
        if (alertDialog != null) {
            alertDialog.getWindow().setType(HUAWEI_FINGERPRINT_UP);
            this.mReEnrollDialog.setCanceledOnTouchOutside(false);
            this.mReEnrollDialog.setCancelable(false);
            this.mReEnrollDialog.show();
        }
        registerPhoneStateReceiver();
    }

    private void registerPhoneStateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        this.mReceiver = new BroadcastReceiver() {
            /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass19 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                TelephonyManager telephonyManager;
                if (intent.getAction() != null && (telephonyManager = (TelephonyManager) context.getSystemService("phone")) != null && HwFingerprintService.this.mReEnrollDialog != null) {
                    if (telephonyManager.getCallState() == 1 && HwFingerprintService.this.mReEnrollDialog.isShowing()) {
                        boolean unused = HwFingerprintService.sIsNeedRecreateDialog = true;
                        HwFingerprintService.this.mReEnrollDialog.dismiss();
                    } else if (telephonyManager.getCallState() == 0 && !HwFingerprintService.this.mReEnrollDialog.isShowing()) {
                        HwFingerprintService.this.mReEnrollDialog.show();
                    }
                }
            }
        };
        Context context = this.mContext;
        if (context != null) {
            context.registerReceiver(this.mReceiver, filter);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unRegisterPhoneStateReceiver() {
        Context context;
        BroadcastReceiver broadcastReceiver = this.mReceiver;
        if (broadcastReceiver != null && (context = this.mContext) != null) {
            context.unregisterReceiver(broadcastReceiver);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateActiveGroupEx(int userId) {
        File systemDir;
        BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
        if (daemon != null) {
            int userIdForHal = userId;
            try {
                UserInfoExt infoEx = UserManagerExt.getUserInfoEx(UserManagerExt.get(this.mContext), userId);
                if (infoEx != null && FingerprintSupportEx.isHwHiddenSpace(infoEx)) {
                    userIdForHal = getHiddenSpaceId();
                    Log.i(TAG, "userIdForHal is " + userIdForHal);
                }
                if (userIdForHal == getHiddenSpaceId()) {
                    Log.i(TAG, "userIdForHal == HIDDEN_SPACE_ID");
                    systemDir = getUserSystemDirectory(getPrimaryUserId());
                } else {
                    systemDir = getUserSystemDirectory(userId);
                }
                File fpDir = new File(systemDir, "fpdata");
                if (!fpDir.exists()) {
                    if (!fpDir.mkdir()) {
                        Log.w(TAG, "Cannot make directory: " + fpDir.getCanonicalPath());
                        return;
                    } else if (!FingerprintSupportEx.restoreconSeLinux(fpDir)) {
                        Log.w(TAG, "Restorecons failed. Directory will have wrong label.");
                        return;
                    }
                }
                daemon.setActiveGroupEx(userIdForHal, fpDir.getCanonicalPath());
                updateFingerprints(userId);
            } catch (IOException e) {
                Log.e(TAG, "updateActiveGroupEx error");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOnOrOff() {
        getHandler().removeCallbacks(this.screenOnOrOffRunnable);
        getHandler().post(this.screenOnOrOffRunnable);
    }

    private boolean isBetaUser() {
        int userType = SystemPropertiesEx.getInt("ro.logsystem.usertype", 0);
        if (userType == 3 || userType == 5) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void intentOthers(Context context) {
        Intent intent = new Intent();
        if (SystemPropertiesEx.getBoolean("ro.config.hw_front_fp_navi", false)) {
            intent.setAction("com.android.settings.fingerprint.FingerprintSettings");
        } else {
            intent.setAction("com.android.settings.fingerprint.FingerprintMainSettings");
        }
        intent.setPackage("com.android.settings");
        intent.addFlags(268435456);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Activity not found");
        }
    }

    private void initFpIdentifyViewObserver() {
        if (this.setFpIdentifyViewObserver == null) {
            Log.w(TAG, "setFpIdentifyViewObserver is null");
            return;
        }
        Log.i(TAG, "initFpIdentifyViewObserver");
        registerContentObserver(getSettingsSecureUriFor(SET_FINGERPRINT_IDENTIFY), false, this.setFpIdentifyViewObserver, -1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerNotifyAuthencation(Message msg) {
        BiometricServiceReceiverListenerEx receiver;
        Bundle data = msg.getData();
        if (data == null) {
            Log.w(TAG, "handlerNotifyAuthencation, getData is null");
            return;
        }
        int userId = data.getInt("userId");
        Bundle bundle = data.getBundle("bundle");
        String packageName = bundle != null ? bundle.getString("packagename") : BuildConfig.FLAVOR;
        FingerViewController fingerViewController = this.mFingerViewController;
        if (fingerViewController != null) {
            receiver = fingerViewController.getBiometricServiceReceiver();
        } else {
            receiver = null;
        }
        notifyAuthenticationStarted(packageName, receiver, 0, userId, bundle, true);
    }

    private void initObserver() {
        if (this.fpObserver == null) {
            Log.w(TAG, "fpObserver is null");
            return;
        }
        registerContentObserver(getSettingsSecureUriFor(KEY_KEYGUARD_ENABLE), false, this.fpObserver, -1);
        this.mState = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), KEY_KEYGUARD_ENABLE, 0, ActivityManagerEx.getCurrentUser());
        this.fpObserver.onChange(true);
    }

    private void updateTPState(String opPackageName, boolean isUdFingerprintExist) {
        if (!"com.android.systemui".equals(opPackageName)) {
            HwFpServiceToHalUtils.sendCommandToHal(1);
        } else if (!isUdFingerprintExist || this.mState == 0) {
            HwFpServiceToHalUtils.sendCommandToHal(0);
        } else {
            HwFpServiceToHalUtils.sendCommandToHal(1);
        }
        Log.i(TAG, "updateTPState isUdFingerprintExist " + isUdFingerprintExist + ",opPackageName:" + opPackageName);
    }

    private void initNavModeObserver() {
        if (this.mNavModeObserver == null) {
            Log.w(TAG, "mNavModeObserver is null");
            return;
        }
        Context context = this.mContext;
        if (context == null || context.getContentResolver() == null) {
            Log.w(TAG, "mContext or the resolver is null");
            return;
        }
        registerContentObserver(SettingsEx.Systemex.getUriFor("enable_navbar"), true, this.mNavModeObserver, -1);
        this.mIsVirNavModeEnabled = FrontFingerPrintSettings.isNaviBarEnabled(this.mContext.getContentResolver());
        Log.i(TAG, "Read the navigation mode after boot, mIsVirNavModeEnabled = " + this.mIsVirNavModeEnabled);
        HwFpServiceToHalUtils.sendCommandToHal(this.mIsVirNavModeEnabled ? MSG_TYPE_VIRTUAL_NAV : MSG_TYPE_FINGERPRINT_NAV);
    }

    private void initUserSwitchReceiver() {
        BroadcastReceiver broadcastReceiver = this.mUserSwitchReceiver;
        if (broadcastReceiver == null) {
            Log.i(TAG, "mUserSwitchReceiver is null");
            return;
        }
        Context context = this.mContext;
        if (context == null) {
            Log.d(TAG, "mContext is null");
        } else {
            ContextEx.registerReceiverAsUser(context, broadcastReceiver, UserHandleEx.ALL, new IntentFilter("android.intent.action.USER_SWITCHED"), (String) null, (Handler) null);
        }
    }

    public void onStart() {
        HwFingerprintService.super.onStart();
        publishBinderServiceEx(getAuthenticatorName(), getAuthenticatorBinder());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.USER_PRESENT");
        this.mContext.registerReceiver(this.mUserDeletedMonitor, filter);
        this.mIsForbideKeyguardCall = false;
        Log.v(TAG, "HwFingerprintService onstart");
    }

    public void onUserSwitching(int newUserId) {
        if (this.mFingerViewController != null) {
            Log.v(TAG, "onUserSwitching removeMaskOrButton");
            this.mFingerViewController.removeMaskOrButton(true);
            this.mIsForbideKeyguardCall = true;
        }
    }

    public void onUserSwitchComplete(int newUserId) {
        Log.v(TAG, "onUserSwitchComplete ");
        this.mIsForbideKeyguardCall = false;
        ContentObserver contentObserver = this.fpObserver;
        if (contentObserver != null) {
            contentObserver.onChange(true);
        }
        if (this.mFingerViewController != null) {
            FingerprintController.getInstance().getDisplaySettingsInformationWhenSwitchUser();
        }
    }

    private int getSwitchFrequenceSupport() {
        return HwFpServiceToHalUtils.sendCommandToHal(MSG_CHECK_SWITCH_FREQUENCE_SUPPORT, -1);
    }

    public void onBootPhase(int phase) {
        HwFingerprintService.super.onBootPhase(phase);
        Log.i(TAG, "HwFingerprintService onBootPhase:" + phase);
        if (phase == PHASE_BOOT_COMPLETED) {
            initSwitchFrequence();
            initPositionAndType();
            if (sIsRemoveFingerprintBGE) {
                initUserSwitchReceiver();
                initNavModeObserver();
            }
            getAodFace().sendFaceStatusToHal(true);
            connectToProxy();
            startCurrentClient();
            FingerprintController.getInstance().sendTpStateToHal(this.mIsFingerInScreenSupported, this.mContext, this.mDaemonEx);
            FingerprintController.getInstance().initScreenRefreshRate();
        }
    }

    private void initSwitchFrequence() {
        if (getSwitchFrequenceSupport() == 1) {
            IntentFilter filterScreen = new IntentFilter();
            filterScreen.addAction("android.intent.action.SCREEN_ON");
            filterScreen.addAction("android.intent.action.SCREEN_OFF");
            this.mContext.registerReceiver(this.mSwitchFrequenceMonitor, filterScreen);
        }
    }

    public void updateFingerprints(int userId) {
        if (FingerprintUtilsEx.getInstance().isDualFp()) {
            Log.d(TAG, "dualFingerprint-> updateFingerprints");
            HwFpServiceToHalUtils.sendCommandToHal(FingerprintUtilsEx.MSG_GETOLDDATA_UD);
            refreshData(remoteGetOldData(), userId, FingerprintUtilsEx.DEVICE_UD);
            refreshData(remoteGetOldData(), userId, FingerprintUtilsEx.DEVICE_BACK);
            return;
        }
        refreshData(remoteGetOldData(), userId, FingerprintUtilsEx.DEVICE_BACK);
    }

    private void refreshData(HwFingerprintSets hwFpSets, int userId, int deviceIndex) {
        FingerprintUtilsEx utils;
        List<FingerprintEx> fpOldList;
        if (!(hwFpSets == null || (utils = FingerprintUtilsEx.getInstance()) == null)) {
            ArrayList<FingerprintEx> mNewFingerprints = null;
            int fingerprintGpSize = hwFpSets.mFingerprintGroups.size();
            for (int i = 0; i < fingerprintGpSize; i++) {
                HwFingerprintSets.HwFingerprintGroup fpGroup = hwFpSets.mFingerprintGroups.get(i);
                int realGroupId = fpGroup.mGroupId;
                if (fpGroup.mGroupId == getHiddenSpaceId()) {
                    realGroupId = getRealUserIdForApp(fpGroup.mGroupId);
                }
                if (realGroupId == userId) {
                    mNewFingerprints = fpGroup.mFingerprints;
                }
            }
            if (mNewFingerprints == null) {
                mNewFingerprints = new ArrayList<>();
            }
            if (utils.isDualFp()) {
                fpOldList = utils.getFingerprintsForUser(this.mContext, userId, deviceIndex);
            } else {
                fpOldList = utils.getBiometricsForUser(this.mContext, userId);
            }
            removeFingerprintNotExist(fpOldList, utils, userId, mNewFingerprints);
            int size = mNewFingerprints.size();
            for (int i2 = 0; i2 < size; i2++) {
                FingerprintEx fp = mNewFingerprints.get(i2);
                if (utils.isDualFp()) {
                    utils.addFingerprintForUser(this.mContext, fp.getBiometricId(), userId, deviceIndex);
                } else {
                    utils.addBiometricForUser(this.mContext, userId, fp);
                }
                CharSequence fpName = fp.getName();
                if (fpName != null && !fpName.toString().isEmpty()) {
                    utils.renameFingerprintForUser(this.mContext, fp.getBiometricId(), userId, fpName);
                }
            }
        }
    }

    private void removeFingerprintNotExist(List<FingerprintEx> fpOldList, FingerprintUtilsEx utils, int userId, ArrayList<FingerprintEx> newFingerprints) {
        for (FingerprintEx oldFp : fpOldList) {
            if (!checkItemExist(oldFp.getBiometricId(), newFingerprints)) {
                utils.removeFingerprintIdForUser(this.mContext, oldFp.getBiometricId(), userId);
            }
        }
    }

    public boolean checkPrivacySpaceEnroll(int userId, int currentUserId) {
        if (!FingerprintSupportEx.isHwHiddenSpace(UserManagerExt.getUserInfoEx(UserManagerExt.get(this.mContext), userId)) || currentUserId != getPrimaryUserId()) {
            return false;
        }
        Log.v(TAG, "enroll privacy fingerprint in primary user ");
        return true;
    }

    public boolean checkNeedPowerpush() {
        if (this.mIsFlagFirstIn) {
            this.mTimeStart = System.currentTimeMillis();
            this.mIsFlagFirstIn = false;
            return true;
        }
        long timePassed = System.currentTimeMillis() - this.mTimeStart;
        Log.v(TAG, "timepassed is  " + timePassed);
        this.mTimeStart = System.currentTimeMillis();
        return timePassed > POWER_PUSH_DOWN_TIME_THR;
    }

    public int removeUserData(int groupId, String storePath) {
        if (!isFingerprintDReady()) {
            return -1;
        }
        BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Log.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        daemon.removeUserData(groupId, storePath);
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int checkNeedReEnrollFingerPrints() {
        Log.w(TAG, "checkNeedReEnrollFingerPrints");
        if (!isFingerprintDReady()) {
            return -1;
        }
        BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Log.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        int result = daemon.checkNeedReEnrollFinger();
        Log.w(TAG, "framework checkNeedReEnrollFingerPrints is finish return = " + result);
        return result;
    }

    public boolean onHwTransact(int code, ParcelEx data, ParcelEx reply, int flags) {
        if (code == CODE_IS_FP_NEED_CALIBRATE_RULE) {
            Log.d(TAG, "code == CODE_IS_FP_NEED_CALIBRATE_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int result = checkNeedCalibrateFingerPrint();
            reply.writeNoException();
            reply.writeInt(result);
            return true;
        } else if (code == CODE_SET_CALIBRATE_MODE_RULE) {
            Log.d(TAG, "code == CODE_SET_CALIBRATE_MODE_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            setCalibrateMode(data.readInt());
            reply.writeNoException();
            return true;
        } else if (code == CODE_GET_TOKEN_LEN_RULE) {
            Log.d(TAG, "code == CODE_GET_TOKEN_LEN_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int result2 = getTokenLen();
            reply.writeNoException();
            reply.writeInt(result2);
            return true;
        } else if (code == CODE_SET_FINGERPRINT_MASK_VIEW_RULE) {
            Log.d(TAG, "code == CODE_SET_FINGERPRINT_MASK_VIEW_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            setFingerprintMaskView(data.readBundle());
            reply.writeNoException();
            return true;
        } else if (code == CODE_SHOW_FINGERPRINT_VIEW_RULE) {
            Log.d(TAG, "code == CODE_SHOW_FINGERPRINT_VIEW_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            showFingerprintView();
            reply.writeNoException();
            return true;
        } else if (code == CODE_SHOW_FINGERPRINT_BUTTON_RULE) {
            Log.d(TAG, "code == CODE_SHOW_FINGERPRINT_BUTTON_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            showSuspensionButton(data.readInt(), data.readInt());
            reply.writeNoException();
            return true;
        } else if (code == CODE_REMOVE_FINGERPRINT_RULE) {
            Log.d(TAG, "code == CODE_REMOVE_FINGERPRINT_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            removeFingerprintView();
            reply.writeNoException();
            return true;
        } else if (code == CODE_GET_HARDWARE_POSITION) {
            Log.d(TAG, "CODE_GET_HARDWARE_POSITION");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int[] result3 = getFingerprintHardwarePosition();
            reply.writeNoException();
            reply.writeInt(result3[0]);
            reply.writeInt(result3[1]);
            reply.writeInt(result3[2]);
            reply.writeInt(result3[3]);
            return true;
        } else if (code == CODE_FINGERPRINT_LOGO_POSITION) {
            Log.d(TAG, "CODE_FINGERPRINT_LOGO_POSITION");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int[] result4 = getFingerprintLogoPosition();
            reply.writeNoException();
            reply.writeInt(result4[0]);
            reply.writeInt(result4[1]);
            reply.writeInt(result4[2]);
            reply.writeInt(result4[3]);
            return true;
        } else if (code == CODE_GET_HARDWARE_TYPE) {
            Log.d(TAG, "CODE_GET_HARDWARE_TYPE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int result5 = getFingerprintHardwareType();
            reply.writeNoException();
            reply.writeInt(result5);
            return true;
        } else if (code == CODE_NOTIFY_OPTICAL_CAPTURE) {
            Log.d(TAG, "CODE_NOTIFY_OPTICAL_CAPTURE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            notifyCaptureOpticalImage();
            reply.writeNoException();
            return true;
        } else if (code == CODE_SUSPEND_AUTHENTICATE) {
            Log.d(TAG, "CODE_SUSPEND_AUTHENTICATE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            suspendAuthentication(data.readInt());
            reply.writeNoException();
            return true;
        } else if (code == CODE_SET_HOVER_SWITCH) {
            Log.d(TAG, "CODE_SET_HOVER_SWITCH");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            setHoverEventSwitch(data.readInt());
            reply.writeNoException();
            return true;
        } else if (code == CODE_GET_HOVER_SUPPORT) {
            Log.d(TAG, "CODE_GET_HOVER_SUPPORT");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int result6 = getHoverEventSupport();
            reply.writeNoException();
            reply.writeInt(result6);
            return true;
        } else if (code == CODE_DISABLE_FINGERPRINT_VIEW_RULE) {
            Log.d(TAG, "CODE_DISABLE_FINGERPRINT_VIEW_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            disableFingerprintView(data.isReadBoolean());
            reply.writeNoException();
            return true;
        } else if (code == CODE_ENABLE_FINGERPRINT_VIEW_RULE) {
            Log.d(TAG, "CODE_ENABLE_FINGERPRINT_VIEW_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            enableFingerprintView(data.isReadBoolean(), data.readInt());
            reply.writeNoException();
            return true;
        } else if (code == CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION_RULE) {
            Log.d(TAG, "CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            keepMaskShowAfterAuthentication();
            reply.writeNoException();
            return true;
        } else if (code == CODE_REMOVE_MASK_AND_SHOW_BUTTON_RULE) {
            Log.d(TAG, "CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            removeMaskAndShowButton();
            reply.writeNoException();
            return true;
        } else if (code == CODE_IS_FINGERPRINT_HARDWARE_DETECTED) {
            Log.d(TAG, "CODE_IS_FINGERPRINT_HARDWARE_DETECTED");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            boolean isHardwareDetected = isHardwareDetectedNoWhitelist(data.readString(), data.readInt());
            reply.writeNoException();
            reply.isWriteBoolean(isHardwareDetected);
            return true;
        } else if (code == CODE_GET_FINGERPRINT_LIST_ENROLLED) {
            Log.d(TAG, "CODE_GET_FINGERPRINT_LIST_ENROLLED");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            List<FingerprintEx> fingerprints = getEnrolledFingerprintsNoWhitelist(data.readString(), data.readInt(), data.readInt());
            reply.writeNoException();
            reply.writeTypedList(fingerprints);
            return true;
        } else if (code == CODE_IS_SUPPORT_DUAL_FINGERPRINT) {
            Log.d(TAG, "CODE_IS_SUPPORT_DUAL_FINGERPRINT");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            reply.writeNoException();
            reply.isWriteBoolean(isSupportDualFingerprint());
            return true;
        } else if (code == CODE_SEND_UNLOCK_LIGHTBRIGHT) {
            Log.d(TAG, "CODE_SEND_UNLOCK_LIGHTBRIGHT");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int result7 = sendUnlockAndLightbright(data.readInt());
            reply.writeNoException();
            reply.writeInt(result7);
            return true;
        } else if (code == CODE_GET_HIGHLIGHT_SPOT_RADIUS_RULE) {
            Log.d(TAG, "CODE_GET_HIGHLIGHT_SPOT_RADIUS_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int radius = getHighLightspotRadius();
            reply.writeNoException();
            reply.writeInt(radius);
            return true;
        } else if (code == CODE_SUSPEND_ENROLL) {
            Log.d(TAG, "CODE_SUSPEND_ENROLL");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int result8 = suspendEnroll(data.readInt());
            reply.writeNoException();
            reply.writeInt(result8);
            return true;
        } else if (code == CODE_UDFINGERPRINT_SPOTCOLOR) {
            Log.d(TAG, "CODE_UDFINGERPRINT_SPOTCOLOR");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int result9 = getSpotColor();
            reply.writeNoException();
            reply.writeInt(result9);
            return true;
        } else if (code == CODE_FINGERPRINT_FORBID_GOTOSLEEP) {
            Log.i(TAG, "CODE_FINGERPRINT_FORBID_GOTOSLEEP");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            boolean isPowerFpForbidGotoSleep = isPowerFpForbidGotoSleep();
            reply.writeNoException();
            reply.isWriteBoolean(isPowerFpForbidGotoSleep);
            return true;
        } else if (code == CODE_POWER_KEYCODE) {
            Log.d(TAG, "CODE_POWER_KEYCODE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int resultKeyCode = operPowerFpPowerKeyCode(data.readInt(), data.isReadBoolean(), data.isReadBoolean());
            reply.writeNoException();
            reply.writeInt(resultKeyCode);
            return true;
        } else if (code == CODE_IS_WAIT_AUTHEN) {
            Log.d(TAG, "CODE_IS_WAIT_AUTHEN");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            boolean isAuthenStatus = isKeyguardAuthenStatus();
            reply.writeNoException();
            reply.isWriteBoolean(isAuthenStatus);
            return true;
        } else if (code == CODE_FINGERPRINT_WEATHER_DATA) {
            Log.d(TAG, "CODE_FP_ENV_DATA");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            setWeatherDataToHal(data.readInt(), data.readString());
            reply.writeNoException();
            reply.writeInt((int) MSG_BINDER_SUCCESS_FLAG);
            return true;
        } else if (code != CODE_FINGERPRINT_MMI_TEST) {
            return HwFingerprintService.super.onHwTransact(code, data, reply, flags);
        } else {
            Log.i(TAG, "CODE_FINGERPRINT_MMI_TEST");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            if (this.mContext.checkPermission(HwFingerprintService.super.getManageBiometricPermission(), Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                reply.writeInt(-1);
                return true;
            }
            int resultKeyCode2 = startFingerprintTest(data.readInt(), data.readString());
            reply.writeNoException();
            reply.writeInt(resultKeyCode2);
            return true;
        }
    }

    private boolean onTransactToHal(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code != CODE_FINGERPRINT_LOGO_RADIUS) {
            return false;
        }
        Log.d(TAG, "CODE_FINGERPRINT_LOGO_RADIUS");
        data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
        int result = getFingerPrintLogoRadius();
        reply.writeNoException();
        reply.writeInt(result);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isSupportDualFingerprint() {
        if (!(this.mIsSupportDualFingerprint || getFingerprintHardwareType() == -1 || (getFingerprintHardwareType() & 1) == 0 || (getFingerprintHardwareType() & 4) == 0)) {
            FingerprintUtilsEx.getInstance().setDualFp(true);
            this.mIsSupportDualFingerprint = true;
            this.mWhitelist.add("com.android.settings");
            this.mWhitelist.add("com.android.systemui");
            this.mWhitelist.add("com.huawei.aod");
            this.mWhitelist.add(PKGNAME_OF_SYSTEM_MANAGER);
            this.mWhitelist.add("com.huawei.hidisk");
            this.mWhitelist.add("com.huawei.wallet");
            this.mWhitelist.add("com.huawei.hwid");
            this.mWhitelist.add("com.huawei.systemserver");
            this.mWhitelist.add("com.android.cts.verifier");
            this.mWhitelist.add("com.eg.android.AlipayGphone");
            this.mWhitelist.add("com.tmall.wireless");
            this.mWhitelist.add("com.taobao.taobao");
            this.mWhitelist.add("com.alibaba.wireless");
            this.mWhitelist.add("com.taobao.trip");
            this.mWhitelist.add("com.taobao.idlefish");
            this.mWhitelist.add("com.taobao.mobile.dipei");
            this.mWhitelist.add("com.taobao.movie.android");
            this.mWhitelist.add("com.alibaba.wireless.microsupply");
            this.mWhitelist.add("com.alibaba.wireless.lstretailer");
            this.mWhitelist.add("com.wudaokou.hippo");
            this.mWhitelist.add("com.taobao.ju.android");
            this.mWhitelist.add("com.taobao.htao.android");
            this.mWhitelist.add("com.taobao.kepler");
            this.mWhitelist.add("com.taobao.shoppingstreets");
            this.mWhitelist.add("com.antfortune.wealth");
            this.mWhitelist.add("com.taobao.qianniu");
            this.mWhitelist.add("com.taobao.litetao");
            this.mWhitelist.add("com.taobao.auction");
            this.mWhitelist.add("com.alibaba.cun.assistant");
            this.mWhitelist.add("com.taobao.caipiao");
        }
        return this.mIsSupportDualFingerprint;
    }

    /* access modifiers changed from: protected */
    public boolean canUseUdFingerprint(String opPackageName) {
        String type;
        if (opPackageName == null || BuildConfig.FLAVOR.equals(opPackageName)) {
            Log.d(TAG, "calling opPackageName is invalid");
            return false;
        } else if (opPackageName.equals(this.mDefinedAppName)) {
            return true;
        } else {
            Log.d(TAG, "canUseUdFingerprint opPackageName is " + opPackageName);
            Iterator<String> it = this.mWhitelist.iterator();
            while (it.hasNext()) {
                if (it.next().equals(opPackageName)) {
                    return true;
                }
            }
            long token = Binder.clearCallingIdentity();
            try {
                Bundle metaData = this.mContext.getPackageManager().getApplicationInfo(opPackageName, 128).metaData;
                if (metaData == null) {
                    Log.d(TAG, "metaData is null");
                }
                if (!(metaData == null || (type = metaData.getString("fingerprint.system.view")) == null || BuildConfig.FLAVOR.equals(type))) {
                    Log.d(TAG, "calling opPackageName metaData value is: " + type);
                    Binder.restoreCallingIdentity(token);
                    return true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "cannot find metaData of package");
            } catch (Exception e2) {
                Log.e(TAG, "exception occured");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public List<FingerprintEx> getEnrolledFingerprintsEx(String opPackageName, int targetDevice, int userId) {
        int tempUserId = userId;
        if (userId != 0 && isClonedProfile(userId)) {
            Log.d(TAG, "Clone profile get Enrolled Fingerprints,change userid to 0");
            tempUserId = 0;
        }
        FingerprintUtilsEx fingerprintUtils = FingerprintUtilsEx.getInstance();
        if (canUseUdFingerprint(opPackageName)) {
            return fingerprintUtils.getFingerprintsForUser(this.mContext, tempUserId, targetDevice);
        }
        if (targetDevice == FingerprintUtilsEx.DEVICE_UD) {
            return Collections.emptyList();
        }
        return fingerprintUtils.getFingerprintsForUser(this.mContext, tempUserId, FingerprintUtilsEx.DEVICE_BACK);
    }

    private List<FingerprintEx> getEnrolledFingerprintsNoWhitelist(String opPackageName, int targetDevice, int userId) {
        Log.d(TAG, "dualFingerprint getEnrolledFingerprints opPackageName is " + opPackageName + " userId is " + userId);
        return FingerprintUtilsEx.getInstance().getFingerprintsForUser(this.mContext, userId, targetDevice);
    }

    /* access modifiers changed from: protected */
    public boolean isHardwareDetectedEx(String opPackageName, int targetDevice) {
        boolean z = false;
        if (getBiometricsFingerprintDaemon() == null) {
            Log.d(TAG, "Daemon is not available!");
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            if (canUseUdFingerprint(opPackageName)) {
                if (targetDevice == FingerprintUtilsEx.DEVICE_UD) {
                    if (getUdHalDeviceId() != 0) {
                        z = true;
                    }
                    return z;
                } else if (targetDevice == FingerprintUtilsEx.DEVICE_BACK) {
                    if (getHalDeviceIdEx() != 0) {
                        z = true;
                    }
                    Binder.restoreCallingIdentity(token);
                    return z;
                } else {
                    if (!(getHalDeviceIdEx() == 0 || getUdHalDeviceId() == 0)) {
                        z = true;
                    }
                    Binder.restoreCallingIdentity(token);
                    return z;
                }
            } else if (targetDevice == FingerprintUtilsEx.DEVICE_BACK) {
                if (getHalDeviceIdEx() != 0) {
                    z = true;
                }
                Binder.restoreCallingIdentity(token);
                return z;
            } else {
                Binder.restoreCallingIdentity(token);
                return false;
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private boolean isHardwareDetectedNoWhitelist(String opPackageName, int targetDevice) {
        boolean z = false;
        if (getBiometricsFingerprintDaemon() == null) {
            Log.d(TAG, "Daemon is not available!");
            return false;
        }
        Log.d(TAG, "dualFingerprint isHardwareDetected opPackageName is " + opPackageName + " targetDevice is " + targetDevice);
        long token = Binder.clearCallingIdentity();
        try {
            if (targetDevice == FingerprintUtilsEx.DEVICE_UD) {
                if (getUdHalDeviceId() != 0) {
                    z = true;
                }
                return z;
            } else if (targetDevice == FingerprintUtilsEx.DEVICE_BACK) {
                if (getHalDeviceIdEx() != 0) {
                    z = true;
                }
                Binder.restoreCallingIdentity(token);
                return z;
            } else {
                if (!(getHalDeviceIdEx() == 0 || getUdHalDeviceId() == 0)) {
                    z = true;
                }
                Binder.restoreCallingIdentity(token);
                return z;
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public int checkNeedCalibrateFingerPrint() {
        if (!isFingerprintDReady()) {
            return -1;
        }
        BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Log.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        Log.d(TAG, "pacel packaged :checkNeedCalibrateFingerPrint");
        int result = daemon.checkNeedCalibrateFingerPrint();
        Log.d(TAG, "fingerprintd calibrate return = " + result);
        return result;
    }

    public void setCalibrateMode(int mode) {
        if (!isFingerprintDReady()) {
            Log.w(TAG, "FingerprintD is not Ready");
            return;
        }
        BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Log.e(TAG, "Fingerprintd is not available!");
            return;
        }
        Log.d(TAG, "pacel packaged setCalibrateMode: " + mode);
        daemon.setCalibrateMode(mode);
    }

    public int getTokenLen() {
        if (!isFingerprintDReady()) {
            return -1;
        }
        BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Log.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        Log.d(TAG, "pacel packaged :getTokenLen");
        int result = daemon.getTokenLen();
        Log.d(TAG, "fingerprintd getTokenLen token len = " + result);
        return result;
    }

    public void showFingerprintView() {
        initPositionAndType();
        if (this.mIsFingerInScreenSupported) {
            checkPermissions();
            this.mFingerViewController.showMaskForApp(this.mMaskViewBundle);
        }
    }

    public void showSuspensionButton(int centerX, int centerY) {
        initPositionAndType();
        if (this.mIsFingerInScreenSupported) {
            checkPermissions();
            this.mFingerViewController.showSuspensionButtonForApp(centerX, centerY, this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid()));
        }
    }

    public void setFingerprintMaskView(Bundle bundle) {
        initPositionAndType();
        if (this.mIsFingerInScreenSupported) {
            checkPermissions();
            if (bundle != null) {
                String callingApp = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
                Log.d(TAG, "callingApp = " + callingApp);
                String pkgNamefromBundle = bundle.getString("PackageName");
                this.mMaskViewBundle = bundle;
                if (this.mFingerViewController == null) {
                    return;
                }
                if (!"com.android.systemui".equals(pkgNamefromBundle)) {
                    this.mFingerViewController.updateMaskViewAttributes(bundle, callingApp);
                } else if (!this.mIsForbideKeyguardCall) {
                    this.mFingerViewController.parseBundle4Keyguard(bundle);
                }
            }
        }
    }

    private boolean isStopAuthenticationStarted(String pkgName, int flag) {
        if (!this.mIsFingerInScreenSupported) {
            return true;
        }
        if (pkgName == null) {
            Log.d(TAG, "pkgname is null");
            return true;
        }
        checkPermissions();
        if (flag == 0 || (FLAG_USE_UD_FINGERPRINT & flag) != 0) {
            return false;
        }
        Log.d(TAG, "flag = " + flag);
        return true;
    }

    private void setForbidGotoSleepFlag(boolean isForbidFlag, Handler handler, String packageName) {
        if (isSupportPowerFp()) {
            FingerprintController.getInstance().setForbidGotoSleepFlag(isForbidFlag, handler, packageName);
        }
    }

    public void notifyAuthenticationStarted(String pkgName, BiometricServiceReceiverListenerEx receiver, int flag, int userId, Bundle bundle, boolean isBiometricPrompt) {
        boolean isHasUdFingerprint;
        initPositionAndType();
        if (this.mIsFingerInScreenSupported || isSupportPowerFp() || IS_SUPPORT_INFORM_FACE) {
            getAodFace().checkIsPrimaryUser(userId, pkgName);
        }
        if (!isStopAuthenticationStarted(pkgName, flag)) {
            notifyAuthenticCancelToKeyguard(pkgName);
            if (bundle != null) {
                this.mMaskViewBundle = bundle;
                this.mMaskViewBundle.putString("googleFlag", "googleFlag");
            }
            Log.i(TAG, "show, pkgName =" + pkgName + " userId = " + userId);
            FingerprintUtilsEx fingerprintUtils = FingerprintUtilsEx.getInstance();
            int initType = getInitType(pkgName, bundle, isBiometricPrompt, fingerprintUtils);
            boolean isHasBackFingerprint = false;
            boolean z = false;
            if (fingerprintUtils.isDualFp()) {
                isHasUdFingerprint = fingerprintUtils.getFingerprintsForUser(this.mContext, userId, FingerprintUtilsEx.DEVICE_UD).size() > 0;
                if (fingerprintUtils.getFingerprintsForUser(this.mContext, userId, FingerprintUtilsEx.DEVICE_BACK).size() > 0) {
                    z = true;
                }
                isHasBackFingerprint = z;
                if (!isHasUdFingerprint) {
                    Log.d(TAG, "userId:" + userId + "has no UD_fingerprint");
                    if (!(initType == 3 || initType == 4)) {
                        return;
                    }
                }
            } else {
                if (fingerprintUtils.getBiometricsForUser(this.mContext, userId).size() > 0) {
                    z = true;
                }
                isHasUdFingerprint = z;
            }
            this.mFingerViewController.closeEyeProtecttionMode(pkgName);
            FingerprintViewUtils.setScreenRefreshRate(this.mContext, 2, pkgName, this.mIsFingerInScreenSupported, this.mFingerViewController.getHandler());
            if (!checkIfCallerDisableMask()) {
                Log.d(TAG, "dialogReceiver = " + receiver);
                this.mFingerViewController.setBiometricPrompt(isBiometricPrompt);
                this.mFingerViewController.showMaskOrButtonInit(receiver, isHasUdFingerprint, isHasBackFingerprint);
                this.mFingerViewController.showMaskOrButton(pkgName, this.mMaskViewBundle, initType);
            }
            Log.i(TAG, " begin add windowservice view");
            loadHighlightAndAnimOnKeyguard(pkgName);
            this.mIsUdAuthenticating = true;
            updateTPState(pkgName, isHasUdFingerprint);
        }
    }

    private void notifyAuthenticCancelToKeyguard(String pkgName) {
        if ("com.android.systemui".equals(pkgName)) {
            notifyAuthenticationCanceled(pkgName);
        }
        setForbidGotoSleepFlag(true, getHandler(), pkgName);
    }

    private int getInitType(String pkgName, Bundle bundle, boolean isBiometricPrompt, FingerprintUtilsEx fingerprintUtils) {
        int initType = -1;
        if (pkgName.equals(this.mDefinedAppName)) {
            initType = this.mAppDefinedMaskType;
            Log.i(TAG, "initType = " + initType + ",defined by enable interface");
        }
        if (initType == -1) {
            initType = getAppType(pkgName);
        }
        if (initType == -1) {
            Iterator<String> it = this.mWhitelist.iterator();
            while (it.hasNext()) {
                if (it.next().equals(pkgName)) {
                    Log.i(TAG, "pkgName in whitelist, show default mask for it");
                    initType = 0;
                }
            }
        }
        if ("com.android.cts.verifier".equals(pkgName)) {
            initType = 0;
            Log.i(TAG, "com.android.cts.verifier initType = 0");
        }
        if (isBiometricPrompt && pkgName.equals("com.android.settings")) {
            initType = 0;
        }
        if (!fingerprintUtils.isDualFp() && initType == -1) {
            initType = 0;
        } else if (fingerprintUtils.isDualFp() && bundle != null) {
            initType = 4;
        }
        if (!fingerprintUtils.isDualFp()) {
            initType = adjustMaskTypeForWechat(initType, pkgName);
        }
        Log.i(TAG, "final initType = " + initType);
        return initType;
    }

    private void loadHighlightAndAnimOnKeyguard(String pkgName) {
        if (this.mFingerViewController != null && "com.android.systemui".equals(pkgName)) {
            Log.i(TAG, "keyguard show highlight mask and fingerviewAim");
            this.mFingerViewController.showHighlightviewOnKeyguard();
            this.mFingerViewController.loadFingerviewAnimOnKeyguard();
        }
    }

    private int adjustMaskTypeForWechat(int initType, String pkgName) {
        if (PKGNAME_OF_WECHAT.equals(pkgName)) {
            return 3;
        }
        return initType;
    }

    public void notifyAuthenticationCanceled(String pkgName) {
        if (this.mIsFingerInScreenSupported && this.mFingerViewController != null) {
            Log.i(TAG, "notifyAuthenticationCanceled pkgName = " + pkgName);
            checkPermissions();
            this.mMaskViewBundle = null;
            if ("com.android.systemui".equals(pkgName)) {
                this.mFingerViewController.destroyHighlightviewOnKeyguard();
                this.mFingerViewController.destroyFingerAnimViewOnKeyguard();
            }
            if (!this.mIsKeepMaskAfterAuthentication) {
                Log.i(TAG, "mIsKeepMaskAfterAuthentication is false, start remove, pkgName = " + pkgName);
                this.mFingerViewController.removeMaskOrButton();
                this.mFingerViewController.reopenEyeProtecttionMode(pkgName);
                FingerprintViewUtils.setScreenRefreshRate(this.mContext, 0, pkgName, this.mIsFingerInScreenSupported, this.mFingerViewController.getHandler());
            }
            this.mIsUdAuthenticating = false;
            this.mIsKeepMaskAfterAuthentication = false;
            this.mAppDefinedMaskType = -1;
            this.mDefinedAppName = BuildConfig.FLAVOR;
        }
    }

    private void removeFingerprintView() {
        if (this.mIsFingerInScreenSupported) {
            String callingApp = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
            Log.i(TAG, "removeFingerprintView,callingApp = " + callingApp);
            checkPermissions();
            this.mMaskViewBundle = null;
            this.mFingerViewController.removeMaskOrButton();
        }
    }

    private void setTouchSwitch() {
        long token = Binder.clearCallingIdentity();
        try {
            if (getSettingsSystemIntForUser(this.mContext.getContentResolver(), "show_touches", -2) == 1) {
                Log.i(TAG, "turn off the show_touch switch when authenticating");
                this.mIsNeedResumeTouchSwitch = true;
                SettingsEx.Systemex.putIntForUser(this.mContext.getContentResolver(), "show_touches", 0, -2);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "catch SecurityException");
        } catch (Exception e2) {
            Log.e(TAG, "settings show_touches not found");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
        Binder.restoreCallingIdentity(token);
    }

    public void notifyFingerDown(int type) {
        Log.i(TAG, "notifyFingerDown mIsFingerInScreenSupported:" + this.mIsFingerInScreenSupported);
        if (this.mIsFingerInScreenSupported) {
            if (isCoverOpen()) {
                Log.i(TAG, "mWindowManagerInternal.isCover added");
                return;
            }
            checkPermissions();
            this.mIsUdFingerprintChecking = true;
            if (this.mIsHighLightNeed) {
                if (type == 1) {
                    Log.i(TAG, "FINGER_DOWN_TYPE_AUTHENTICATING notifyFingerDown ");
                    setTouchSwitch();
                    this.mFingerViewController.showHighlightview(1);
                } else if (type == 0) {
                    Log.i(TAG, "FINGER_DOWN_TYPE_ENROLLING notifyFingerDown ");
                    setTouchSwitch();
                    this.mFingerViewController.showHighlightCircle();
                } else if (type == 3) {
                    Log.i(TAG, "FINGER_DOWN_TYPE_AUTHENTICATING_SYSTEMUI notifyFingerDown ");
                    this.mFingerViewController.showHighlightCircleOnKeyguard();
                }
            }
            if (type == 1) {
                this.mFingerViewController.updateFingerprintView(3, this.mIsKeepMaskAfterAuthentication);
            }
        }
    }

    public void notifyEnrollingFingerUp() {
        if (this.mIsFingerInScreenSupported && this.mIsUdEnrolling) {
            resumeTouchSwitch();
            if (this.mIsHighLightNeed) {
                Log.i(TAG, "notifyEnrollingFingerUp removeHighlightCircle");
                this.mFingerViewController.removeHighlightCircle();
            }
        }
    }

    public void notifyCaptureFinished(int type) {
        FingerViewController fingerViewController;
        Log.i(TAG, "notifyCaptureFinished");
        if (this.mIsHighLightNeed && (fingerViewController = this.mFingerViewController) != null) {
            fingerViewController.removeHighlightCircle();
        }
    }

    public void notifyFingerCalibrarion(int value) {
        initPositionAndType();
        if (this.mIsFingerInScreenSupported) {
            checkPermissions();
            int[] positions = getFingerprintHardwarePosition();
            FingerprintCalibrarionView caliview = FingerprintCalibrarionView.getInstance(this.mContext);
            caliview.setCenterPoints((positions[0] + positions[2]) / 2, (positions[1] + positions[3]) / 2);
            caliview.showHighlightviewCali(value);
        }
    }

    public void notifyEnrollmentStarted(int flags) {
        initPositionAndType();
        if (this.mIsHighLightNeed) {
            FingerprintUtilsEx fingerprintUtils = FingerprintUtilsEx.getInstance();
            if (!fingerprintUtils.isDualFp() || flags == 4096) {
                this.mIsUdEnrolling = true;
                checkPermissions();
                if (!fingerprintUtils.isDualFp() || flags == 4096) {
                    DisplayEngineManager displayEngineManager = this.mDisplayEngineManager;
                    if (displayEngineManager != null) {
                        displayEngineManager.setScene(FingerprintSupportEx.DE_SCENE_UD_ENROLL_FINGER_PRINT, 16);
                    } else {
                        Log.w(TAG, "mDisplayEngineManager is null!");
                    }
                }
                FingerViewController fingerViewController = this.mFingerViewController;
                if (fingerViewController != null) {
                    fingerViewController.showHighlightview(0);
                }
                Log.i(TAG, "start enroll begin add Highlight view");
                HwFpServiceToHalUtils.sendCommandToHal(1);
                setForbidGotoSleepFlag(true, getHandler(), this.mOpPackageName);
                return;
            }
            Log.i(TAG, "not enrolling UD fingerprint");
        }
    }

    public void notifyEnrollmentCanceled() {
        FingerViewController fingerViewController;
        boolean isHasUdFingerprint;
        if (this.mIsFingerInScreenSupported) {
            FingerprintUtilsEx fingerprintUtils = FingerprintUtilsEx.getInstance();
            int currentUser = ActivityManagerEx.getCurrentUser();
            boolean z = true;
            if (fingerprintUtils.isDualFp()) {
                if (fingerprintUtils.getFingerprintsForUser(this.mContext, currentUser, FingerprintUtilsEx.DEVICE_UD).size() <= 0) {
                    z = false;
                }
                isHasUdFingerprint = z;
            } else {
                if (fingerprintUtils.getBiometricsForUser(this.mContext, currentUser).size() <= 0) {
                    z = false;
                }
                isHasUdFingerprint = z;
            }
            if (!isHasUdFingerprint) {
                HwFpServiceToHalUtils.sendCommandToHal(0);
            }
        }
        Log.i(TAG, "notifyEnrollmentEnd mIsUdEnrolling: " + this.mIsUdEnrolling);
        if (this.mIsHighLightNeed) {
            checkPermissions();
            resumeTouchSwitch();
            if (this.mIsUdEnrolling && (fingerViewController = this.mFingerViewController) != null) {
                fingerViewController.removeHighlightview(0);
            }
            if (this.mIsUdEnrolling) {
                DisplayEngineManager displayEngineManager = this.mDisplayEngineManager;
                if (displayEngineManager != null) {
                    displayEngineManager.setScene(FingerprintSupportEx.DE_SCENE_UD_ENROLL_FINGER_PRINT, 17);
                } else {
                    Log.w(TAG, "mDisplayEngineManager is null!");
                }
            }
            this.mIsUdEnrolling = false;
        }
    }

    public void notifyAuthenticationFinished(String opName, int result, int failTimes) {
        if (this.mIsFingerInScreenSupported && this.mFingerViewController != null) {
            checkPermissions();
            Log.i(TAG, "notifyAuthenticationFinished,mIsUdFingerprintChecking = " + this.mIsUdFingerprintChecking + ",result =" + result + "failTimes = " + failTimes + ",mIsHighLightNeed=" + this.mIsHighLightNeed);
            if (this.mIsHighLightNeed && this.mIsUdFingerprintChecking) {
                resumeTouchSwitch();
                if (!"com.android.systemui".equals(opName)) {
                    this.mFingerViewController.removeHighlightview(result == 0 ? 2 : -1);
                } else {
                    this.mFingerViewController.removeHighlightviewOnKeyguard();
                    Log.i(TAG, "finger check finish removeHighlightCircle");
                }
            }
            if ("com.android.systemui".equals(opName) && result == 0) {
                Log.i(TAG, " KEYGUARD notifyAuthenticationFinished remove FingerviewAnim");
                this.mFingerViewController.destroyFingerAnimViewOnKeyguard();
            }
            if (result == 0) {
                this.mFingerViewController.reopenEyeProtecttionMode(opName);
                FingerprintViewUtils.setScreenRefreshRate(this.mContext, 0, opName, this.mIsFingerInScreenSupported, this.mFingerViewController.getHandler());
            }
            this.mFingerViewController.updateFingerprintView(result, failTimes);
            if (result == 0 && "com.android.systemui".equals(opName)) {
                this.mFingerViewController.removeMaskOrButton();
            }
            this.mIsUdFingerprintChecking = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resumeTouchSwitch() {
        if (this.mIsHighLightNeed && this.mIsUdFingerprintChecking && this.mIsNeedResumeTouchSwitch) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mIsNeedResumeTouchSwitch = false;
                Log.i(TAG, "turn on the show_touch switch after authenticating");
                SettingsEx.Systemex.putIntForUser(this.mContext.getContentResolver(), "show_touches", 1, -2);
            } catch (SecurityException e) {
                Log.e(TAG, "catch SecurityException");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    private void disableFingerprintView(boolean isHasAnimation) {
        if (this.mIsFingerInScreenSupported) {
            checkPermissions();
            String callingApp = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
            Log.i(TAG, "callingApp = " + callingApp);
            if (this.mIsUdAuthenticating && callingApp.equals(FingerprintViewUtils.getForegroundActivity())) {
                this.mFingerViewController.removeMaskOrButton();
            }
            this.mPackageDisableMask = callingApp;
        }
    }

    private void enableFingerprintView(boolean isHasAnimation, int initStatus) {
        if (this.mIsFingerInScreenSupported) {
            checkPermissions();
            String callingApp = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
            Log.i(TAG, "enableFingerprintView,callingApp = " + callingApp);
            String str = this.mPackageDisableMask;
            if (str != null && str.equals(callingApp)) {
                this.mPackageDisableMask = null;
            }
            this.mAppDefinedMaskType = initStatus;
            this.mDefinedAppName = callingApp;
        }
    }

    private int getAppType(String pkgName) {
        int type = -1;
        try {
            Bundle metaData = this.mContext.getPackageManager().getApplicationInfo(pkgName, 128).metaData;
            if (metaData == null) {
                Log.d(TAG, "metaData is null");
            }
            if (metaData != null) {
                Log.i(TAG, "pkgName is " + pkgName + "metaData is " + metaData.getString("fingerprint.system.view"));
                if (metaData.getString("fingerprint.system.view") != null) {
                    if ("full".equals(metaData.getString("fingerprint.system.view"))) {
                        type = 0;
                    } else if ("button".equals(metaData.getString("fingerprint.system.view"))) {
                        type = 1;
                    } else if ("image".equals(metaData.getString("fingerprint.system.view"))) {
                        type = 3;
                    } else {
                        type = 2;
                    }
                }
                Log.i(TAG, "metaData type is " + type);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "cannot find metaData of package");
        } catch (SecurityException e2) {
            Log.e(TAG, "app don't have permissions");
        }
        return type;
    }

    private void keepMaskShowAfterAuthentication() {
        if (this.mIsFingerInScreenSupported) {
            checkPermissions();
            this.mIsKeepMaskAfterAuthentication = true;
        }
    }

    private void removeMaskAndShowButton() {
        if (this.mIsFingerInScreenSupported) {
            checkPermissions();
            this.mFingerViewController.removeMaskAndShowButton();
        }
    }

    /* JADX INFO: finally extract failed */
    private String getForegroundActivityName() {
        long token = Binder.clearCallingIdentity();
        try {
            ActivityInfo info = ActivityManagerEx.getLastResumedActivity();
            Binder.restoreCallingIdentity(token);
            String name = BuildConfig.FLAVOR;
            if (info != null) {
                name = info.name;
            }
            Log.i(TAG, "foreground wechat activity name is" + name);
            return name;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private boolean checkIfCallerDisableMask() {
        String str = this.mPackageDisableMask;
        if (str == null) {
            return false;
        }
        if (FingerprintViewUtils.isForegroundActivity(str)) {
            return true;
        }
        this.mPackageDisableMask = null;
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkPermissions() {
        Context context = this.mContext;
        if (context != null && context.checkCallingOrSelfPermission("android.permission.USE_FINGERPRINT") != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.USE_BIOMETRIC", "Must have USE_BIOMETRIC permission");
        }
    }

    private void resetFingerprintPosition() {
        int[] positions = getFingerprintHardwarePosition();
        this.mFingerViewController.setFingerprintPosition(positions);
        Log.i(TAG, "defaultScreenSize be init second, positions = " + Arrays.toString(positions));
    }

    private int getEnrollDigitalBrigtness() {
        return HwFpServiceToHalUtils.sendCommandToHal(MMI_TYPE_GET_HIGHLIGHT_LEVEL, 0);
    }

    private int setWeatherDataToHal(int curTemperture, String curHumidity) {
        byte[] sendDatas = new byte[4];
        float[] huimidityDatas = new float[4];
        if (curHumidity == null) {
            return 0;
        }
        try {
            huimidityDatas[0] = Float.parseFloat(curHumidity.split("%")[0]);
        } catch (NumberFormatException e) {
            Log.e(TAG, "setDataToHal NumberFormatException");
        }
        sendDatas[0] = (byte) curTemperture;
        sendDatas[1] = (byte) ((int) huimidityDatas[0]);
        ArrayList<Byte> list = new ArrayList<>();
        list.add(Byte.valueOf(sendDatas[0]));
        list.add(Byte.valueOf(sendDatas[1]));
        BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
        if (daemon != null) {
            return daemon.sendDataToHal((int) MSG_UDENV_PARA_DATA, list);
        }
        return 0;
    }

    private void initPositionAndType() {
        int i = this.mFingerprintType;
        if (i == 0) {
            Log.i(TAG, "FingerprintType do not support inscreen fingerprint");
        } else if (i <= 0 || this.mFingerViewController == null) {
            this.mFingerprintType = getFingerprintHardwareTypeInternal();
            Log.i(TAG, "FingerprintType type = " + this.mFingerprintType);
            boolean z = false;
            this.mIsFingerInScreenSupported = this.mFingerprintType > 0;
            if (this.mFingerprintType == 1) {
                z = true;
            }
            this.mIsHighLightNeed = z;
            if (this.mIsFingerInScreenSupported) {
                this.mFingerViewController = FingerViewController.getInstance(this.mContext);
                this.mFingerViewController.registCallback(new FingerViewChangeCallback());
                this.mFingerViewController.setFingerHandler(this.mFingerHandler);
                this.mFingerViewController.setFingerprintPosition(getFingerprintHardwarePosition());
                this.mFingerViewController.setEnrollDigitalBrigtness(getEnrollDigitalBrigtness());
                this.mFingerViewController.setHighLightBrightnessLevel(getHighLightBrightnessLevel());
                this.mFingerViewController.setFingerPrintLogoRadius(getFingerPrintLogoRadius());
                int spotColor = getSpotColor();
                if (spotColor == 0) {
                    spotColor = DEFAULT_COLOR;
                }
                this.mFingerViewController.setHighLightSpotColor(spotColor);
                this.mFingerViewController.setHighLightSpotRadius(getHighLightspotRadius());
                initObserver();
                initFpIdentifyViewObserver();
                initFpToughenedFilmStateObserver();
                getAodFace();
            }
        } else {
            if (this.mInitDisplayHeight == -1) {
                resetFingerprintPosition();
            }
            Log.i(TAG, "FingerprintType has been inited, FingerprintType = " + this.mFingerprintType);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void suspendAuthentication(int status) {
        if (!isFingerprintDReady()) {
            Log.w(TAG, "FingerprintD is not Ready");
        } else if (!this.mIsFingerInScreenSupported) {
            Log.w(TAG, "do not have UD device suspend invalid");
        } else {
            BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
            if (daemon == null) {
                Log.e(TAG, "Fingerprintd is not available!");
                return;
            }
            Log.i(TAG, "pacel packaged suspendAuthentication: " + status);
            if (status == 1) {
                daemon.sendCmdToHal((int) MSG_SUSPEND_AUTHENTICATION);
            } else {
                daemon.sendCmdToHal((int) MSG_RESUME_AUTHENTICATION);
            }
        }
    }

    private int suspendEnroll(int status) {
        if (!isFingerprintDReady()) {
            Log.w(TAG, "FingerprintD is not Ready");
            return -1;
        } else if (!this.mIsFingerInScreenSupported) {
            Log.w(TAG, "do not have UD device suspend invalid");
            return -1;
        } else {
            BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
            if (daemon == null) {
                Log.e(TAG, "Fingerprintd is not available!");
                return -1;
            }
            Log.i(TAG, "pacel packaged suspendEnroll: " + status);
            if (status == 1) {
                return daemon.sendCmdToHal((int) MSG_SUSPEND_ENROLLMENT);
            }
            return daemon.sendCmdToHal((int) MSG_RESUME_ENROLLMENT);
        }
    }

    private int getFingerprintHardwareType() {
        if (this.mTypeDetails == -1) {
            if (!isFingerprintDReady()) {
                return -1;
            }
            BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
            if (daemon == null) {
                Log.e(TAG, "Fingerprintd is not available!");
                return -1;
            }
            Log.i(TAG, "pacel packaged :HardwareType");
            this.mTypeDetails = daemon.sendCmdToHal((int) MSG_GET_SENSOR_TYPE);
            Log.i(TAG, "fingerprintd HardwareType = " + this.mTypeDetails);
        }
        return this.mTypeDetails;
    }

    private int getFingerprintHardwareTypeInternal() {
        if (getFingerprintHardwareType() == -1) {
            return -1;
        }
        int offset = -1;
        if ((getFingerprintHardwareType() & 1) != 0) {
            offset = -1 + 1;
        }
        if ((getFingerprintHardwareType() & 2) != 0) {
            offset++;
        }
        if ((getFingerprintHardwareType() & 4) == 0) {
            return 0;
        }
        int physicalType = (getFingerprintHardwareType() >> (((offset + 1) * 4) + ERROR_CODE_COMMEN_ERROR)) & FLAG_FINGERPRINT_TYPE_MASK;
        Log.i(TAG, "LOCATION_UNDER_DISPLAY physicalType :" + physicalType);
        if (physicalType == 2) {
            return 1;
        }
        if (physicalType == 3) {
            return 2;
        }
        return -1;
    }

    private int[] getFingerprintLogoPosition() {
        int[] results = {-1, -1};
        int[] pxPositions = {-1, -1, -1, -1};
        if (!isFingerprintDReady()) {
            return pxPositions;
        }
        BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Log.e(TAG, "Fingerprintd is not available!");
            return pxPositions;
        }
        results[0] = daemon.sendCmdToHal((int) MSG_GET_LOGO_POSITION_FROM_HAL);
        results[1] = results[0];
        Log.d(TAG, "fingerprintd getFingerprintLogoPosition = " + results[0]);
        if (results[0] == -1 || results[0] == 0) {
            return getFingerprintHardwarePosition();
        }
        return FingerprintViewUtils.getFingerprintHardwarePosition(results);
    }

    private int[] getFingerprintHardwarePosition() {
        int[] results = {-1, -1};
        int[] pxPositions = {-1, -1, -1, -1};
        if (!isFingerprintDReady()) {
            return pxPositions;
        }
        BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Log.e(TAG, "Fingerprintd is not available!");
            return pxPositions;
        }
        results[0] = daemon.sendCmdToHal((int) MSG_GET_SENSOR_POSITION_TOP_LEFT);
        results[1] = daemon.sendCmdToHal((int) MSG_GET_SENSOR_POSITION_BOTTOM_RIGHT);
        Log.d(TAG, "fingerprintd HardwarePosition = " + results[0] + " " + results[1]);
        if (results[0] == -1) {
            return FingerprintViewUtils.getFingerprintHardwarePosition(results);
        }
        return physicalConvert2Px(FingerprintViewUtils.getFingerprintHardwarePosition(results));
    }

    private int[] physicalConvert2Px(int[] input) {
        int[] covertPositions = {-1, -1, -1, -1};
        if (this.mInitDisplayHeight == -1 && this.mContext != null) {
            String defaultScreenSize = SystemPropertiesEx.get("ro.config.default_screensize");
            if (defaultScreenSize == null || BuildConfig.FLAVOR.equals(defaultScreenSize)) {
                this.mInitDisplayHeight = getSettingsGlobalInt(this.mContext.getContentResolver(), APS_INIT_HEIGHT, -1);
                this.mInitDisplayWidth = getSettingsGlobalInt(this.mContext.getContentResolver(), APS_INIT_WIDTH, -1);
                Log.i(TAG, "defaultScreenSizePoint mInitDisplayWidth =" + this.mInitDisplayWidth + ",mInitDisplayHeight=" + this.mInitDisplayHeight);
            } else {
                String[] array = defaultScreenSize.split(",");
                if (array.length == 2) {
                    try {
                        this.mInitDisplayWidth = Integer.parseInt(array[0]);
                        this.mInitDisplayHeight = Integer.parseInt(array[1]);
                        Log.i(TAG, "defaultScreenSizePoint get from prop : mInitDisplayWidth=" + this.mInitDisplayWidth + ",mInitDisplayHeight=" + this.mInitDisplayHeight);
                    } catch (NumberFormatException e) {
                        Log.i(TAG, "defaultScreenSizePoint: NumberFormatException");
                    }
                } else {
                    Log.i(TAG, "defaultScreenSizePoint the defaultScreenSize prop is error,defaultScreenSize=" + defaultScreenSize);
                }
            }
        }
        int i = input[0];
        int i2 = this.mInitDisplayWidth;
        covertPositions[0] = (i * i2) / 1000;
        int i3 = input[1];
        int i4 = this.mInitDisplayHeight;
        covertPositions[1] = (i3 * i4) / 1000;
        covertPositions[2] = (input[2] * i2) / 1000;
        covertPositions[3] = (input[3] * i4) / 1000;
        Log.d(TAG, "Width: " + this.mInitDisplayWidth + " height: " + this.mInitDisplayHeight);
        for (int i5 = 0; i5 < 4; i5++) {
            Log.d(TAG, "use hal after covert: " + covertPositions[i5]);
        }
        return covertPositions;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyCaptureOpticalImage() {
        if (isFingerprintDReady()) {
            HwFpServiceToHalUtils.sendCommandToHal(MSG_OPTICAL_HBM_FOR_CAPTURE_IMAGE);
            Log.i(TAG, "pacel packaged :notifyCaptureOpticalImage");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyBluespotDismiss() {
        if (isFingerprintDReady()) {
            HwFpServiceToHalUtils.sendCommandToHal(MSG_NOTIFY_BLUESPOT_DISMISS);
            Log.i(TAG, "pacel packaged :notifyBluespotDismiss");
        }
    }

    private void setHoverEventSwitch(int enabled) {
        if (!isFingerprintDReady()) {
            Log.w(TAG, "FingerprintD is not Ready");
            return;
        }
        BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Log.e(TAG, "Fingerprintd is not available!");
            return;
        }
        Log.i(TAG, "pacel packaged setHoverEventSwitch: " + enabled);
        if (enabled == 1) {
            daemon.sendCmdToHal((int) MSG_SET_HOVER_ENABLE);
        } else {
            daemon.sendCmdToHal((int) MSG_SET_HOVER_DISABLE);
        }
    }

    private int getHoverEventSupport() {
        if (!isFingerprintDReady()) {
            return -1;
        }
        int result = HwFpServiceToHalUtils.sendCommandToHal(MSG_CHECK_HOVER_SUPPORT, -1);
        Log.i(TAG, "fingerprintd getHoverEventSupport result = " + result);
        return result;
    }

    private int getHighLightBrightnessLevel() {
        if (!isFingerprintDReady()) {
            Log.e(TAG, "Fingerprintd is not ready!");
            return DEFAULT_CAPTURE_BRIGHTNESS;
        }
        int result = HwFpServiceToHalUtils.sendCommandToHal(MSG_GET_BRIGHTNEWSS_FROM_HAL, DEFAULT_CAPTURE_BRIGHTNESS);
        Log.i(TAG, "fingerprintd getHighLightBrightnessLevel = " + result);
        return result;
    }

    private int getSpotColor() {
        if (!isFingerprintDReady()) {
            Log.e(TAG, "Fingerprintd is not ready!");
            return 0;
        }
        int color = HwFpServiceToHalUtils.sendCommandToHal(MSG_GET_HIGHLIGHT_SPOT_COLOR_FROM_HAL, 0);
        Log.i(TAG, "fingerprintd getHighLightColor = " + color);
        return color;
    }

    private int checkForegroundNeedLiveness() {
        Log.w(TAG, "checkForegroundNeedLiveness:start");
        try {
            List<ActivityManager.RunningAppProcessInfo> procs = ActivityManagerEx.getRunningAppProcesses();
            if (procs == null) {
                return 0;
            }
            int size = procs.size();
            for (int i = 0; i < size; i++) {
                ActivityManager.RunningAppProcessInfo proc = procs.get(i);
                if (proc.importance == 100) {
                    if ("com.alipay.security.mobile.authentication.huawei".equals(proc.processName)) {
                        Log.w(TAG, "ForegroundActivity is " + proc.processName + "need liveness auth");
                        return 1;
                    } else if ("com.huawei.wallet".equals(proc.processName)) {
                        Log.w(TAG, "ForegroundActivity is " + proc.processName + "need liveness auth");
                        return 1;
                    } else if ("com.huawei.android.hwpay".equals(proc.processName)) {
                        Log.w(TAG, "ForegroundActivity is " + proc.processName + "need liveness auth");
                        return 1;
                    }
                }
            }
            return 0;
        } catch (RemoteException e) {
            Log.w(TAG, "am.getRunningAppProcesses() failed in checkForegroundNeedLiveness");
        }
    }

    private int checkNeedLivenessList(String opPackageName) {
        Log.w(TAG, "checkNeedLivenessList:start");
        if (opPackageName == null || "com.android.keyguard".equals(opPackageName)) {
            return 0;
        }
        if ("com.huawei.securitymgr".equals(opPackageName)) {
            return checkForegroundNeedLiveness();
        }
        if (!"com.eg.android.AlipayGphone".equals(opPackageName) && !"fido".equals(opPackageName) && !"com.alipay.security.mobile.authentication.huawei".equals(opPackageName) && !"com.huawei.wallet".equals(opPackageName) && !"com.huawei.android.hwpay".equals(opPackageName) && !PKGNAME_OF_WECHAT.equals(opPackageName)) {
            return 0;
        }
        return 1;
    }

    private List<Byte> getAuthenticationScenarioPackageName(String appPackageName) {
        if (appPackageName == null) {
            Log.e(TAG, "updateAuthenticationScenario opPackageName is null");
            return new ArrayList<>(0);
        }
        byte[] packageNameBytes = appPackageName.getBytes(StandardCharsets.UTF_8);
        if (!(packageNameBytes == null || packageNameBytes.length == 0)) {
            List<Byte> packageNameArrayList = new ArrayList<>(packageNameBytes.length);
            for (byte i : packageNameBytes) {
                packageNameArrayList.add(Byte.valueOf(i));
            }
            if (!packageNameArrayList.isEmpty()) {
                return packageNameArrayList;
            }
        }
        return new ArrayList<>(0);
    }

    /* access modifiers changed from: protected */
    public void setLivenessSwitch(String opPackageName) {
        Log.w(TAG, "setLivenessSwitch:start");
        if ((!sIsLivenessNeedBetaQualification || isBetaUser()) && isFingerprintDReady()) {
            int needLivenessAuthentication = checkNeedLivenessList(opPackageName);
            Log.w(TAG, "needLivenessAuthentication = " + needLivenessAuthentication);
            BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
            if (daemon == null) {
                Log.e(TAG, "Fingerprintd is not available!");
                return;
            }
            daemon.setLivenessSwitch(needLivenessAuthentication);
            List<Byte> packageNameArrays = getAuthenticationScenarioPackageName(opPackageName);
            if (packageNameArrays != null && !packageNameArrays.isEmpty() && (packageNameArrays instanceof ArrayList)) {
                daemon.sendDataToHal((int) MSG_UPDATE_AUTHENTICATION_SCENARIO, (ArrayList) packageNameArrays);
            }
            Log.w(TAG, "framework setLivenessSwitch is ok ---end");
        }
    }

    private boolean checkPackageName(String opPackageName) {
        if (opPackageName == null || !"com.android.systemui".equals(opPackageName)) {
            return false;
        }
        return true;
    }

    public boolean shouldAuthBothSpaceFingerprints(String opPackageName, int flags) {
        if (!checkPackageName(opPackageName) || (33554432 & flags) == 0) {
            return false;
        }
        return true;
    }

    private HwFingerprintSets remoteGetOldData() {
        HwFingerprintSets result;
        Log.i(TAG, "remoteGetOldData:start");
        if (!isFingerprintDReady()) {
            return null;
        }
        BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Log.e(TAG, "Fingerprintd is not available!");
            return null;
        }
        List<Integer> fingerprintInfos = daemon.getFpOldData();
        Parcel reply = Parcel.obtain();
        int fingerprintInfoLen = fingerprintInfos.size();
        for (int i = 0; i < fingerprintInfoLen; i++) {
            int intValue = fingerprintInfos.get(i).intValue();
            if (intValue != -1) {
                reply.writeInt(intValue);
            }
        }
        reply.setDataPosition(0);
        if (reply.readInt() != 0) {
            result = HwFingerprintSets.CREATOR.createFromParcel(reply);
        } else {
            result = null;
        }
        reply.recycle();
        return result;
    }

    private static boolean checkItemExist(int oldFpId, ArrayList<FingerprintEx> fingerprints) {
        int size = fingerprints.size();
        for (int i = 0; i < size; i++) {
            if (fingerprints.get(i).getBiometricId() == oldFpId) {
                fingerprints.remove(i);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isFingerprintDReady() {
        if (getBiometricsFingerprintDaemon() != null) {
            return true;
        }
        Log.w(TAG, "isFingerprintDReady: no fingeprintd!");
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleAuthenticatedEx(BiometricAuthenticatorEx identifier, ArrayList<Byte> token) {
        if (identifier == null || token == null) {
            Log.e(TAG, "handleAuthenticated identifier or token is null");
            return;
        }
        this.mLastAuthenticatedStartTime = System.currentTimeMillis();
        boolean isAuthenticated = identifier.getBiometricId() != 0;
        if (isAuthenticated) {
            clearKeyguardAuthenScreenOn();
            HwFingerprintService.super.setFingerprintWakeup(true);
        }
        if (isPowerFpAbandonAuthenticated(isAuthenticated)) {
            Log.i(TAG, "discard onAuthenticated:" + isAuthenticated);
            return;
        }
        FingerprintDataInterface fingerprintDataInterface = this.mFpDataCollector;
        if (fingerprintDataInterface != null) {
            fingerprintDataInterface.reportFingerprintAuthenticated(isAuthenticated);
        }
        setAuTime(System.currentTimeMillis());
        HwFingerprintService.super.handleAuthenticatedEx(identifier, token);
        sendOnAuthenticatedFinishToHal();
        if (!isAuthenticated) {
            setKeyguardAuthenStatus(false);
        }
        this.mLastAuthenticatedEndTime = System.currentTimeMillis();
        Log.i(TAG, "powerfp duration=" + (this.mLastAuthenticatedEndTime - this.mLastAuthenticatedStartTime));
        getHandler().postDelayed(this.mBlackAuthenticateEventResetRunable, 600);
    }

    private int sendUnlockAndLightbright(int unlockType) {
        Log.i(TAG, "sendUnlockAndLightbright unlockType:" + unlockType);
        int result = -1;
        if (unlockType == 2) {
            result = HwFpServiceToHalUtils.sendCommandToHal(MSG_SCREEON_UNLOCK_LIGHTBRIGHT, -1);
        } else if (unlockType == 1) {
            result = HwFpServiceToHalUtils.sendCommandToHal(MSG_SCREEOFF_UNLOCK_LIGHTBRIGHT, -1);
        } else if (unlockType == 3) {
            result = HwFpServiceToHalUtils.sendCommandToHal(MSG_SCREEON_UNLOCK_BACKLIGHT, -1);
        }
        Log.i(TAG, "sendCommandToHal result:" + result);
        return result;
    }

    private int getHighLightspotRadius() {
        Log.d(TAG, "getHighLightspotRadius start");
        if (!isFingerprintDReady()) {
            Log.e(TAG, "Fingerprintd is not ready!");
            return DEFAULT_RADIUS;
        }
        int radius = HwFpServiceToHalUtils.sendCommandToHal(MSG_GET_RADIUS_FROM_HAL, DEFAULT_RADIUS);
        Log.i(TAG, "fingerprintd getHighLightspotRadius = " + radius);
        return radius;
    }

    private int getFingerPrintLogoRadius() {
        int result = HwFpServiceToHalUtils.sendCommandToHal(MSG_MMI_UD_UI_LOGO_SIZE, -1);
        Log.i(TAG, "getFingerPrintLogoRadius:" + result);
        if (result == -1 || result <= 0) {
            return FingerprintViewUtils.getFingerprintDefaultLogoRadius(this.mContext);
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public int shouldAuthBothSpaceBiometricEx(FingerprintServiceEx.FingerprintAuthClientEx client, String opPackageName, int flags) {
        Log.i(TAG, "shouldAuthBothSpaceBiometricEx");
        if (!shouldAuthBothSpaceFingerprints(opPackageName, flags)) {
            return client.getGroupIdEx();
        }
        Log.i(TAG, "should authenticate both space fingerprints");
        return -101;
    }

    private AODFaceUpdateMonitor getAodFace() {
        if (this.mAodFaceUpdateMonitor == null) {
            this.mAodFaceUpdateMonitor = new AODFaceUpdateMonitor();
        }
        return this.mAodFaceUpdateMonitor;
    }

    /* access modifiers changed from: protected */
    public class AODFaceUpdateMonitor {
        private static final String ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE = "huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE";
        private static final String ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE = "huawei.intent.action.HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE";
        private static final int CLOSE_SAVE_POWER = 0;
        private static final String CONTENT_URI = "content://com.android.huawei.keyguardstate";
        private static final String FACE_KEYGUARD_WITH_LOCK = "face_bind_with_lock";
        private static final String FACE_RECOGNIZE_SLIDE_UNLOCK = "face_recognize_slide_unlock";
        private static final String FACE_RECOGNIZE_UNLOCK = "face_recognize_unlock";
        private static final String KCALL_GET_STRONG_AUTH_STATE = "getStrongAuthState";
        private static final int OPEN_SAVE_POWER = 3;
        private static final String PERMISSION = "com.huawei.android.launcher.permission.CHANGE_POWERMODE";
        private static final String POWER_MODE = "power_mode";
        private static final String SHUTDOWN_LIMIT_POWERMODE = "shutdomn_limit_powermode";
        private boolean mIsFaceDetectEnabled;
        private boolean mIsPrimaryUser;
        private boolean mIsSuperPowerEnabled;
        private boolean mIsSupportAODFace;
        private boolean mIsTrustEnabled;
        private boolean mIsTrustManageEnabled;
        private boolean mIsTrustUnlock;
        private boolean mIsfaceStatus;
        protected final ContentObserver mSettingsObserver = new ContentObserver(null) {
            /* class com.android.server.fingerprint.HwFingerprintService.AODFaceUpdateMonitor.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                AODFaceUpdateMonitor.this.notifyFaceSettingModify(false);
            }
        };
        private final BroadcastReceiver powerReceiver = new BroadcastReceiver() {
            /* class com.android.server.fingerprint.HwFingerprintService.AODFaceUpdateMonitor.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && context != null) {
                    String action = intent.getAction();
                    Log.i(HwFingerprintService.TAG, "super power action:" + action);
                    if (AODFaceUpdateMonitor.ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE.equals(action)) {
                        if (intent.getIntExtra(AODFaceUpdateMonitor.POWER_MODE, 0) == 3) {
                            AODFaceUpdateMonitor.this.mIsSuperPowerEnabled = true;
                        }
                    } else if (AODFaceUpdateMonitor.ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE.equals(action) && intent.getIntExtra(AODFaceUpdateMonitor.SHUTDOWN_LIMIT_POWERMODE, 0) == 0) {
                        AODFaceUpdateMonitor.this.mIsSuperPowerEnabled = false;
                    }
                    AODFaceUpdateMonitor.this.sendFaceStatusToHal(false);
                }
            }
        };

        public void setTrustManagerEnable(boolean isTrustManageEnabled) {
            this.mIsTrustManageEnabled = isTrustManageEnabled;
        }

        public void setTrustEnable(boolean isTrustEnabled) {
            this.mIsTrustEnabled = isTrustEnabled;
        }

        public AODFaceUpdateMonitor() {
            if (HwFingerprintService.this.mIsFingerInScreenSupported || HwFingerprintService.this.isSupportPowerFp() || HwFingerprintService.IS_SUPPORT_INFORM_FACE) {
                String board = SystemPropertiesEx.get("ro.product.board", "UNKOWN").toUpperCase(Locale.US);
                if (board.contains("TNY") || board.contains("TONY") || board.contains("NEO")) {
                    this.mIsSupportAODFace = false;
                    return;
                }
                this.mIsSupportAODFace = true;
                HwFingerprintService.this.registerContentObserver(HwFingerprintService.this.getSettingsSecureUriFor(FACE_KEYGUARD_WITH_LOCK), false, this.mSettingsObserver, 0);
                HwFingerprintService.this.registerContentObserver(HwFingerprintService.this.getSettingsSecureUriFor(FACE_RECOGNIZE_SLIDE_UNLOCK), false, this.mSettingsObserver, 0);
                HwFingerprintService.this.registerContentObserver(HwFingerprintService.this.getSettingsSecureUriFor(FACE_RECOGNIZE_UNLOCK), false, this.mSettingsObserver, 0);
                IntentFilter powerFilter = new IntentFilter();
                powerFilter.addAction(ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE);
                powerFilter.addAction(ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE);
                HwFingerprintService.this.mContext.registerReceiver(this.powerReceiver, powerFilter, PERMISSION, null);
                try {
                    HwFingerprintService.this.registerTrustListenerForAod();
                } catch (SecurityException e) {
                    Log.e(HwFingerprintService.TAG, "create AODFaceUpdateMonitor SecurityException");
                }
                getFaceSetting();
                return;
            }
            this.mIsSupportAODFace = false;
        }

        private boolean isSupportAODFace() {
            return this.mIsSupportAODFace;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyFaceSettingModify(boolean isBooting) {
            getFaceSetting();
            sendFaceStatusToHal(isBooting);
        }

        private void getFaceSetting() {
            ContentResolver resolver = HwFingerprintService.this.mContext.getContentResolver();
            boolean z = false;
            int faceKeyguardWithLock = SettingsEx.Secure.getIntForUser(resolver, FACE_KEYGUARD_WITH_LOCK, -1, 0);
            if (faceKeyguardWithLock == -1) {
                if (SettingsEx.Secure.getIntForUser(resolver, FACE_RECOGNIZE_UNLOCK, 0, 0) == 1 || SettingsEx.Secure.getIntForUser(resolver, FACE_RECOGNIZE_SLIDE_UNLOCK, 0, 0) == 1) {
                    z = true;
                }
                this.mIsFaceDetectEnabled = z;
                return;
            }
            if (faceKeyguardWithLock == 1) {
                z = true;
            }
            this.mIsFaceDetectEnabled = z;
        }

        public void checkIsPrimaryUser(int userId, String opPackageName) {
            if (isSupportAODFace()) {
                this.mIsPrimaryUser = userId == 0;
                if ("com.android.systemui".equals(opPackageName)) {
                    sendFaceStatusToHal(false);
                }
            }
        }

        public void sendFaceStatusToHal(boolean isBooting) {
            if (isSupportAODFace()) {
                boolean isFaceStatus = this.mIsFaceDetectEnabled && !this.mIsSuperPowerEnabled && this.mIsPrimaryUser && !this.mIsTrustUnlock;
                Log.i(HwFingerprintService.TAG, "sendFaceStatusToHal:mIsFaceDetectEnabled " + this.mIsFaceDetectEnabled + ",mIsSuperPowerEnabled " + this.mIsSuperPowerEnabled + ",mIsPrimaryUser " + this.mIsPrimaryUser + ",mIsTrustUnlock " + this.mIsTrustUnlock + ",isFaceStatus " + isFaceStatus + ",mIsfaceStatus " + this.mIsfaceStatus + ",isBooting " + isBooting);
                if (isBooting || isFaceStatus != this.mIsfaceStatus) {
                    int cmd = isFaceStatus ? HwFingerprintService.MSG_ENABLE_FACE_RECOGNIZATION : HwFingerprintService.MSG_DISABLE_FACE_RECOGNIZATION;
                    this.mIsfaceStatus = isFaceStatus;
                    HwFpServiceToHalUtils.sendCommandToHal(cmd);
                }
            }
        }

        public void updateTrustUnlockStatus() {
            if (isSupportAODFace()) {
                if (!this.mIsTrustEnabled || !this.mIsTrustManageEnabled) {
                    this.mIsTrustUnlock = false;
                } else {
                    Log.i(HwFingerprintService.TAG, "update TrustUnlock Status");
                    try {
                        Bundle outData = HwFingerprintService.this.mContext.getContentResolver().call(Uri.parse(CONTENT_URI), KCALL_GET_STRONG_AUTH_STATE, (String) null, (Bundle) null);
                        if (outData == null || outData.getInt("result") != 0 || outData.getBoolean("StrongAuth")) {
                            this.mIsTrustUnlock = false;
                        } else {
                            this.mIsTrustUnlock = true;
                        }
                    } catch (IllegalArgumentException e) {
                        Log.e(HwFingerprintService.TAG, "getStrongAuthState IllegalArgumentException");
                    } catch (Exception e2) {
                        Log.e(HwFingerprintService.TAG, "getStrongAuthState Exception");
                    }
                }
                sendFaceStatusToHal(false);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onTrustChangedEx(boolean isEnabled, int userId, int flags) {
        getAodFace().setTrustEnable(isEnabled);
        getAodFace().updateTrustUnlockStatus();
    }

    /* access modifiers changed from: protected */
    public void onTrustManagedChangedEx(boolean isEnabled, int userId) {
        getAodFace().setTrustManagerEnable(isEnabled);
        getAodFace().updateTrustUnlockStatus();
    }

    /* access modifiers changed from: protected */
    public void onTrustError(CharSequence message) {
        Log.d(TAG, "onTrustError:message " + message.toString());
    }

    private void startCurrentClient() {
        ClientMonitorEx clientMonitorEx = HwFingerprintService.super.getCurrentClientEx();
        Log.i(TAG, "startCurrentClient = " + clientMonitorEx);
        if (clientMonitorEx == null) {
            return;
        }
        if (clientMonitorEx.isAuthenticationClient() || clientMonitorEx.isEnrollClient()) {
            sendCmdToTpHal(1);
        }
    }

    private void setFaceDetectManagerCookie(int cookie) {
        ClientMonitorEx clientMonitorEx = getCurrentClientEx();
        if (clientMonitorEx != null && clientMonitorEx.isAuthenticationClient()) {
            FaceDetectManager.getInstance().setCookie(cookie);
        }
    }

    /* access modifiers changed from: protected */
    public void startCurrentClient(int cookie) {
        HwFingerprintService.super.startCurrentClient(cookie);
        ClientMonitorEx clientMonitorEx = getCurrentClientEx();
        Log.i(TAG, "startCurrentClient with cookie = " + clientMonitorEx);
        if (clientMonitorEx != null && (clientMonitorEx.isAuthenticationClient() || clientMonitorEx.isEnrollClient())) {
            sendCmdToTpHal(1);
            setForbidGotoSleepFlag(true, getHandler(), clientMonitorEx.getOwnerString());
            Log.w(TAG, "startCurrentClient");
        }
        setFaceDetectManagerCookie(cookie);
    }

    private void clearRepeatAuthentication() {
        ClientMonitorEx clientMonitorEx = getPendingClientEx();
        if (clientMonitorEx != null && !isKeyguardLocked()) {
            Log.i(TAG, "clearRepeatAuthentication.");
            if ("com.android.systemui".equals(clientMonitorEx.getOwnerString())) {
                clientMonitorEx.destroy();
                this.mPendingClientEx = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeClientEx(ClientMonitorEx client) {
        Log.i(TAG, "removeClientEx = " + client);
        if (client.isAuthenticationClient() || client.isEnrollClient()) {
            sendCmdToTpHal(0);
        }
        if (isSupportPowerFp()) {
            FingerprintController.getInstance().setPowerForbidGotoSleepDelay(getHandler());
        }
        this.mCurrentClientEx = null;
        this.mPendingClientEx = null;
        FingerprintController.getInstance().setCurrentPackageName(BuildConfig.FLAVOR);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isKeyguardLocked() {
        KeyguardManager keyguard;
        Context context = this.mContext;
        if (context == null || (keyguard = (KeyguardManager) context.getSystemService("keyguard")) == null) {
            return false;
        }
        return keyguard.isKeyguardLocked();
    }

    private void sendCmdToTpHal(int config) {
        synchronized (this.mLock) {
            if (this.mProxy == null) {
                this.mProxy = FingerprintSupportEx.getInstance().getTouchscreenService();
                if (this.mProxy != null) {
                    Log.i(TAG, "sendCmdToTpHal: mProxy get success.");
                    this.mProxy.linkToDeath(new DeathRecipient(), 1001);
                } else {
                    Log.i(TAG, "sendCmdToTpHal: mProxy get failed.");
                }
            }
            if (this.mProxy == null) {
                Log.w(TAG, "mProxy is null, return");
                return;
            }
            Log.i(TAG, "sendCmdToTpHal start config = " + config);
            if (this.mProxy.hwSetFeatureConfig(2, config == 1 ? TP_HAL_CONFIG_ON : TP_HAL_CONFIG_OFF) == 0) {
                Log.i(TAG, "sendCmdToTpHal success");
            } else {
                Log.i(TAG, "sendCmdToTpHal error");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void connectToProxy() {
        synchronized (this.mLock) {
            if (this.mProxy != null) {
                Log.i(TAG, "mProxy has registered, do not register again");
                return;
            }
            this.mProxy = FingerprintSupportEx.getInstance().getTouchscreenService();
            if (this.mProxy != null) {
                Log.i(TAG, "connectToProxy: mProxy get success.");
                this.mProxy.linkToDeath(new DeathRecipient(), 1001);
            } else {
                Log.d(TAG, "connectToProxy: mProxy get failed.");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class DeathRecipient extends HwBinderEx.DeathRecipientEx {
        DeathRecipient() {
        }

        public void serviceDied(long cookie) {
            if (cookie == 1001) {
                Log.d(HwFingerprintService.TAG, "tp hal service died cookie: " + cookie);
                synchronized (HwFingerprintService.this.mLock) {
                    HwFingerprintService.this.mProxy = null;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private FingerprintController.AuthenticatedParam getPowerFpParam() {
        if (this.mAuthenticatedParam == null) {
            FingerprintController instance = FingerprintController.getInstance();
            instance.getClass();
            this.mAuthenticatedParam = new FingerprintController.AuthenticatedParam();
        }
        return this.mAuthenticatedParam;
    }

    private boolean currentClient(String opPackageName) {
        ClientMonitorEx clientMonitorEx = getCurrentClientEx();
        return clientMonitorEx != null && clientMonitorEx.getOwnerString().equals(opPackageName);
    }

    private boolean isSupportBlackAuthentication() {
        String settingValue = SettingsEx.Secure.getStringForUser(this.mContext.getContentResolver(), "screen_lock_fingerprint_unlock_protection_db", 0);
        if (settingValue != null) {
            return "0".equals(settingValue);
        }
        return this.mIsSupportBlackAuthentication;
    }

    public boolean isSupportPowerFp() {
        return this.mIsSupportPowerFp;
    }

    public boolean isPowerFpForbidGotoSleep() {
        if (!isSupportPowerFp()) {
            return false;
        }
        if (isThreesidesAuthenticating() || FingerprintController.getInstance().isForbidGotoSleep()) {
            Log.i(TAG, "powerfp forbid gotosleep isThreesidesAuthenticating");
            return true;
        }
        if (isSupportBlackAuthentication() && this.mIsBlackAuthenticateEvent) {
            long duration = System.currentTimeMillis() - this.mLastFingerDownTime;
            if (duration > 1000) {
                Log.i(TAG, "powerfp forbid gotosleep do nothing, lastFingerDown duration=" + duration);
                return false;
            } else if (this.mLastAuthenticatedEndTime > this.mLastAuthenticatedStartTime) {
                long duration2 = System.currentTimeMillis() - this.mLastAuthenticatedEndTime;
                Log.i(TAG, "powerfp forbid gotosleep 1 duration=" + duration2);
                if (duration2 < 600) {
                    return true;
                }
            } else {
                long duration3 = System.currentTimeMillis() - this.mLastAuthenticatedStartTime;
                Log.i(TAG, "powerfp forbid gotosleep 2 duration=" + duration3);
                if (duration3 < 700) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isThreesidesAuthenticating() {
        ClientMonitorEx clientMonitorEx = getCurrentClientEx();
        return clientMonitorEx != null && ((clientMonitorEx.isAuthenticationClient() && !"com.android.systemui".equals(clientMonitorEx.getOwnerString())) || clientMonitorEx.isEnrollClient());
    }

    private boolean isKeyguardAuthenticating() {
        ClientMonitorEx clientMonitorEx = getCurrentClientEx();
        return clientMonitorEx != null && clientMonitorEx.isAuthenticationClient() && "com.android.systemui".equals(clientMonitorEx.getOwnerString());
    }

    private int operPowerFpPowerKeyCode(int keycode, boolean isPowerDown, boolean isInteractive) {
        if (!isSupportPowerFp() || isSupportBlackAuthentication() || keycode != 26) {
            return -1;
        }
        if (isPowerDown) {
            this.mLastPowerKeyDownTime = System.currentTimeMillis();
        } else {
            this.mLastPowerKeyUpTime = System.currentTimeMillis();
        }
        this.mIsInteractive = isInteractive;
        if (isPowerDown && !isInteractive) {
            immediatelyOnAuthenticatedRunnable();
        }
        Log.i(TAG, "operPowerFpPowerKeyCode: mLastPowerKeyDownTime=" + this.mLastPowerKeyDownTime + " mLastPowerKeyUpTime=" + this.mLastPowerKeyUpTime + " isPowerDown=" + isPowerDown + " mIsInteractive=" + this.mIsInteractive);
        return 0;
    }

    private void immediatelyOnAuthenticatedRunnable() {
        if (isKeyguardAuthenticating() && getPowerFpParam().isWaitPowerEvent()) {
            getHandler().post(new Runnable() {
                /* class com.android.server.fingerprint.HwFingerprintService.AnonymousClass20 */

                @Override // java.lang.Runnable
                public void run() {
                    Log.w(HwFingerprintService.TAG, "process FP delayed authentication");
                    FingerprintController.AuthenticatedParam param = HwFingerprintService.this.getPowerFpParam();
                    FingerprintEx fp = new FingerprintEx(BuildConfig.FLAVOR, param.getGroupId(), param.getFingerId(), param.getDeviceId());
                    BiometricAuthenticatorEx biometricAuthenticatorEx = new BiometricAuthenticatorEx();
                    biometricAuthenticatorEx.setFingerprintEx(fp);
                    HwFingerprintService hwFingerprintService = HwFingerprintService.this;
                    hwFingerprintService.handleAuthenticatedEx(biometricAuthenticatorEx, hwFingerprintService.getPowerFpParam().getToken());
                }
            });
        }
    }

    public boolean isPowerFpAbandonAuthenticated(boolean isAuthenticated) {
        if (!isSupportPowerFp() || isSupportBlackAuthentication() || !isKeyguardAuthenticating()) {
            return false;
        }
        if (this.mLastPowerKeyUpTime >= this.mLastPowerKeyDownTime) {
            boolean isInteractive = this.mPowerManager.isInteractive() || !isAuthenticated;
            if (!isInteractive) {
                Log.i(TAG, "dev is sleep and no support black authenticated");
            }
            if (!isInteractive) {
                return true;
            }
            return false;
        }
        if (this.mIsInteractive) {
            Log.i(TAG, "power down finish, wait up.");
        }
        return this.mIsInteractive;
    }

    public long getPowerDelayFpTime(boolean isAuthenticated) {
        if (!isSupportPowerFp() || isSupportBlackAuthentication() || !isKeyguardAuthenticating()) {
            return 0;
        }
        long nowTime = System.currentTimeMillis();
        long j = this.mLastPowerKeyUpTime;
        long j2 = this.mLastPowerKeyDownTime;
        if (j < j2 || nowTime <= j2 || (!isAuthenticated && this.mIsReadyWakeUp)) {
            Log.i(TAG, "powerfp mLastPowerKeyUpTime:" + this.mLastPowerKeyUpTime + ",mLastPowerKeyDownTime:" + this.mLastPowerKeyDownTime + ",nowTime:" + nowTime);
            return 0;
        } else if (this.mPowerManager.isInteractive()) {
            return (long) this.mPowerDelayFpTime;
        } else {
            return -1;
        }
    }

    private int sendOnAuthenticatedFinishToHal() {
        if (!isSupportPowerFp() || !isFingerprintDReady()) {
            Log.e(TAG, "Fingerprintd is not ready!");
            return -1;
        }
        int result = HwFpServiceToHalUtils.sendCommandToHal(MSG_AUTHENTICATEDFINISH_TO_HAL, -1);
        Log.d(TAG, "fingerprintd sendOnAuthenticatedFinishToHal = " + result);
        return result;
    }

    public void saveWaitRunonAuthenticated(long deviceId, int fingerId, int groupId, ArrayList<Byte> token) {
        getPowerFpParam().setAuthenticatedParam(deviceId, fingerId, groupId, token);
    }

    public void setNoWaitPowerEvent() {
        getPowerFpParam().setNoWaitPowerEvent();
    }

    private boolean isKeyguardAuthenStatus() {
        if (!isSupportPowerFp() || !isKeyguardAuthenticating() || isInCallAndTpSenser()) {
            return false;
        }
        boolean isDelayed = this.mIsKeyguardAuthenStatus.getAndSet(false);
        Log.i(TAG, "powerfp isKeyguardAuthenStatus wakeup delayed=" + isDelayed);
        if (isDelayed) {
            this.mIsReadyWakeUp = true;
            getHandler().removeCallbacks(this.mPowerFingerWakeUpRunable);
            getHandler().postDelayed(this.mPowerFingerWakeUpRunable, (long) this.mFpDelayPowerTime);
        }
        return isDelayed;
    }

    private boolean isInCallAndTpSenser() {
        boolean isInCallAndTpSenser = isTpSensor() && isInCall();
        Log.i(TAG, "current is calling and tp sensor : " + isInCallAndTpSenser);
        return isInCallAndTpSenser;
    }

    private boolean isTpSensor() {
        if (this.mTpSensorName == null) {
            Sensor proximityTpSensor = ((SensorManager) this.mContext.getSystemService(SensorManager.class)).getDefaultSensor(ERROR_CODE_COMMEN_ERROR);
            if (proximityTpSensor == null) {
                Log.d(TAG, "proximity tp sensor is null .");
                return false;
            }
            this.mTpSensorName = proximityTpSensor.getName();
        }
        return PROXIMITY_TP.equals(this.mTpSensorName);
    }

    private boolean isInCall() {
        Object object;
        if (this.mTelecomManager == null && (object = this.mContext.getSystemService("telecom")) != null && (object instanceof TelecomManager)) {
            this.mTelecomManager = (TelecomManager) object;
        }
        TelecomManager telecomManager = this.mTelecomManager;
        if (telecomManager != null) {
            return telecomManager.isInCall();
        }
        return false;
    }

    private void setKeyguardAuthenStatus(boolean isDownStatus) {
        if (isKeyguardAuthenticating()) {
            setKeyguardAuthenStatusForce(isDownStatus);
        }
    }

    private void setKeyguardAuthenStatusForce(boolean isDownStatus) {
        if (isSupportPowerFp()) {
            this.mIsKeyguardAuthenStatus.set(isDownStatus);
            Log.i(TAG, "setKeyguardAuthenStatusForce set status = " + isDownStatus);
        }
    }

    private void clearKeyguardAuthenScreenOn() {
        if (isSupportPowerFp() && isKeyguardAuthenticating()) {
            getHandler().removeCallbacks(this.mPowerFingerWakeUpRunable);
        }
    }

    private void initPropHwFpType() {
        String config = SystemPropertiesEx.get("ro.config.hw_fp_type", BuildConfig.FLAVOR);
        Log.i(TAG, "powerfp initPropHwFpType config=" + config);
        String[] bufs = config.split(",");
        if (bufs.length == 4) {
            this.mIsSupportPowerFp = "3".equals(bufs[0]);
            this.mIsSupportBlackAuthentication = "1".equals(bufs[1]);
            try {
                this.mFpDelayPowerTime = Integer.parseInt(bufs[2]);
                this.mPowerDelayFpTime = Integer.parseInt(bufs[3]);
            } catch (NumberFormatException e) {
                Log.w(TAG, "initPropHwFpType error:" + config);
            }
        }
    }

    public void notifyFingerRemovedAtAuth(ClientMonitorEx client) {
        if (client == null) {
            Log.e(TAG, "ClientMonitorEx is null");
        } else if (isSupportPowerFp()) {
            Log.i(TAG, "power finger client.stop");
            client.stop(true);
        }
    }

    private int startFingerprintTest(int type, String param) {
        int testRun;
        if (this.mDisplayEngineManager == null) {
            Log.e(TAG, "FPT manger error.");
            return -1;
        }
        synchronized (this.mLock) {
            if (this.mProxy == null) {
                connectToProxy();
            }
            testRun = new FingerprintTestForMmi(this.mProxy, this.mDisplayEngineManager).testRun(type, param);
        }
        return testRun;
    }
}
