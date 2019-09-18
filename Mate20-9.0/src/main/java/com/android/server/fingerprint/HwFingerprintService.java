package com.android.server.fingerprint;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.SynchronousUserSwitchObserver;
import android.app.trust.TrustManager;
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
import android.hardware.biometrics.IBiometricPromptReceiver;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseIntArray;
import android.view.IWindowManager;
import com.android.server.LocalServices;
import com.android.server.fingerprint.HwFingerprintSets;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.PickUpWakeScreenManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.android.server.wm.WindowManagerInternal;
import com.android.systemui.shared.recents.hwutil.HwRecentsTaskUtils;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.pm.UserInfoEx;
import com.huawei.android.os.UserManagerEx;
import com.huawei.displayengine.DisplayEngineManager;
import com.huawei.fingerprint.IAuthenticator;
import com.huawei.fingerprint.IAuthenticatorListener;
import com.huawei.hiai.BuildConfig;
import huawei.android.aod.HwAodManager;
import huawei.android.provider.FrontFingerPrintSettings;
import huawei.com.android.server.fingerprint.FingerViewController;
import huawei.com.android.server.fingerprint.FingerprintCalibrarionView;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import vendor.huawei.hardware.biometrics.fingerprint.V2_1.IExtBiometricsFingerprint;
import vendor.huawei.hardware.biometrics.fingerprint.V2_1.IFidoAuthenticationCallback;

public class HwFingerprintService extends FingerprintService {
    private static final String ACTIVITYNAME_OF_WECHAT_ENROLL = "com.tencent.mm.plugin.fingerprint.ui.FingerPrintAuthUI";
    private static final String APS_INIT_HEIGHT = "aps_init_height";
    private static final String APS_INIT_WIDTH = "aps_init_width";
    public static final int CHECK_NEED_REENROLL_FINGER = 1003;
    private static final int CODE_DISABLE_FINGERPRINT_VIEW_RULE = 1114;
    private static final int CODE_ENABLE_FINGERPRINT_VIEW_RULE = 1115;
    private static final int CODE_FINGERPRINT_FORBID_GOTOSLEEP = 1125;
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
    private static final int DEFAULT_CAPTURE_BRIGHTNESS = 248;
    private static final int DEFAULT_COLOR = -16711681;
    private static final int DEFAULT_RADIUS = 95;
    private static final int DENSITY_DEFUALT_HEIGHT = 2880;
    private static final int DENSITY_DEFUALT_WIDTH = 1440;
    private static final String DESCRIPTOR_FINGERPRINT_SERVICE = "android.hardware.fingerprint.IFingerprintService";
    private static final String FACE_DETECT_REASON = "fingerprint";
    private static final String FIDO_ASM = "com.huawei.hwasm";
    private static final int FINGERPRINT_HARDWARE_OPTICAL = 1;
    private static final int FINGERPRINT_HARDWARE_OUTSCREEN = 0;
    private static final int FINGERPRINT_HARDWARE_ULTRASONIC = 2;
    private static final String FINGERPRINT_METADATA_KEY = "fingerprint.system.view";
    private static final int FLAG_FINGERPRINT_LOCATION_BACK = 1;
    private static final int FLAG_FINGERPRINT_LOCATION_FRONT = 2;
    private static final int FLAG_FINGERPRINT_LOCATION_UNDER_DISPLAY = 4;
    private static final int FLAG_FINGERPRINT_POSITION_MASK = 65535;
    private static final int FLAG_FINGERPRINT_TYPE_CAPACITANCE = 1;
    private static final int FLAG_FINGERPRINT_TYPE_MASK = 15;
    private static final int FLAG_FINGERPRINT_TYPE_OPTICAL = 2;
    private static final int FLAG_FINGERPRINT_TYPE_ULTRASONIC = 3;
    private static final int FLAG_USE_UD_FINGERPRINT = 134217728;
    private static final int FP_CLOSE = 0;
    private static final int FP_OPEN = 1;
    public static final int GET_OLD_DATA = 100;
    private static final int HIDDEN_SPACE_ID = -100;
    private static final int INVALID_VALUE = -1;
    private static final String KEY_DB_CHILDREN_MODE_FPID = "fp_children_mode_fp_id";
    private static final String KEY_DB_CHILDREN_MODE_STATUS = "fp_children_enabled";
    private static final String KEY_KEYGUARD_ENABLE = "fp_keyguard_enable";
    public static final int MASK_TYPE_BACK = 4;
    public static final int MASK_TYPE_BUTTON = 1;
    public static final int MASK_TYPE_FULL = 0;
    public static final int MASK_TYPE_IMAGE = 3;
    public static final int MASK_TYPE_NONE = 2;
    private static final String METADATA_KEY = "fingerprint.system.view";
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
    private static final int MSG_GET_RADIUS_FROM_HAL = 902;
    private static final int MSG_GET_SENSOR_POSITION_BOTTOM_RIGHT = 60;
    private static final int MSG_GET_SENSOR_POSITION_TOP_LEFT = 59;
    private static final int MSG_GET_SENSOR_TYPE = 55;
    private static final int MSG_NOTIFY_BLUESPOT_DISMISS = 62;
    private static final int MSG_OPTICAL_HBM_FOR_CAPTURE_IMAGE = 52;
    private static final int MSG_REDUCING_FREQUENCE = 83;
    private static final int MSG_RESUME_AUTHENTICATION = 54;
    private static final int MSG_RESUME_ENROLLMENT = 65;
    private static final int MSG_SET_HOVER_DISABLE = 58;
    private static final int MSG_SET_HOVER_ENABLE = 57;
    private static final int MSG_SUSPEND_AUTHENTICATION = 53;
    private static final int MSG_SUSPEND_ENROLLMENT = 64;
    private static final int MSG_TYPE_FINGERPRINT_NAV = 43;
    private static final int MSG_TYPE_VIRTUAL_NAV = 45;
    private static final int MSG_UNLOCK_LEARNING = 63;
    private static final int MSG_UPGRADING_FREQUENCE = 82;
    private static final String PATH_CHILDMODE_STATUS = "childmode_status";
    private static final String PKGNAME_OF_KEYGUARD = "com.android.systemui";
    private static final String PKGNAME_OF_SETTINGS = "com.android.settings";
    private static final String PKGNAME_OF_WECHAT = "com.tencent.mm";
    private static final long POWER_PUSH_DOWN_TIME_THR = 430;
    private static final int PRIMARY_USER_ID = 0;
    public static final int REMOVE_USER_DATA = 101;
    public static final int RESTORE_AUTHENTICATE = 0;
    public static final int RESTORE_ENROLL = 0;
    private static final int RESULT_FINGERPRINT_AUTHENTICATION_CHECKING = 3;
    public static final int SET_LIVENESS_SWITCH = 1002;
    private static final int STATUS_PARENT_CTRL_OFF = 0;
    private static final int STATUS_PARENT_CTRL_STUDENT = 1;
    public static final int SUSPEND_AUTHENTICATE = 1;
    public static final int SUSPEND_ENROLL = 1;
    private static final int SWITCH_FREQUENCE_SUPPORT = 1;
    private static final String TAG = "HwFingerprintService";
    private static final int TOUCHE_SWITCH_OFF = 0;
    private static final int TOUCHE_SWITCH_ON = 1;
    public static final int TYPE_DISMISS_ = 0;
    public static final int TYPE_FINGERPRINT_BUTTON = 2;
    public static final int TYPE_FINGERPRINT_VIEW = 1;
    private static final int UNDEFINED_TYPE = -1;
    private static final int UPDATE_SECURITY_USER_ID = 102;
    public static final int VERIFY_USER = 1001;
    /* access modifiers changed from: private */
    public static boolean mCheckNeedEnroll = true;
    /* access modifiers changed from: private */
    public static boolean mIsChinaArea = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    private static boolean mLivenessNeedBetaQualification = false;
    /* access modifiers changed from: private */
    public static boolean mNeedRecreateDialog = false;
    private static boolean mRemoveFingerprintBGE = SystemProperties.getBoolean("ro.config.remove_finger_bge", false);
    /* access modifiers changed from: private */
    public static boolean mRemoveOldTemplatesFeature = SystemProperties.getBoolean("ro.config.remove_old_templates", false);
    private final int DEFAULT_SCREEN_SIZE_STRING_LENGHT = 2;
    private final int HUAWEI_FINGERPRINT_DOWN = 2002;
    private final int HUAWEI_FINGERPRINT_UP = 2003;
    private final int MSG_AUTHENTICATEDFINISH_TO_HAL = 199;
    private final int MSG_SCREEOFF_UNLOCK_LIGHTBRIGHT = 200;
    private final int MSG_SCREEON_UNLOCK_BACKLIGHT = 202;
    private final int MSG_SCREEON_UNLOCK_LIGHTBRIGHT = 201;
    private final int SCREENOFF_UNLOCK = 1;
    private final int SCREENON_BACKLIGHT_UNLOCK = 3;
    private final int SCREENON_UNLOCK = 2;
    ContentObserver fpObserver = new ContentObserver(null) {
        public void onChange(boolean selfChange) {
            if (HwFingerprintService.this.mContext == null) {
                Log.w(HwFingerprintService.TAG, "mContext is null");
                return;
            }
            int unused = HwFingerprintService.this.mState = Settings.Secure.getIntForUser(HwFingerprintService.this.mContext.getContentResolver(), HwFingerprintService.KEY_KEYGUARD_ENABLE, 0, ActivityManager.getCurrentUser());
            if (HwFingerprintService.this.mState == 0) {
                HwFingerprintService.this.sendCommandToHal(0);
            }
            Log.i(HwFingerprintService.TAG, "fp_keyguard_state onChange: " + HwFingerprintService.this.mState);
        }
    };
    private AODFaceUpdateMonitor mAodFaceUpdateMonitor;
    private int mAppDefinedMaskType = -1;
    private AuthenticatedParam mAuthenticatedParam = null;
    /* access modifiers changed from: private */
    public final Context mContext;
    IExtBiometricsFingerprint mDaemonEx = null;
    private String mDefinedAppName = "";
    private DisplayEngineManager mDisplayEngineManager;
    /* access modifiers changed from: private */
    public FingerViewController mFingerViewController;
    private int mFingerprintType = -1;
    /* access modifiers changed from: private */
    public boolean mForbideKeyguardCall = false;
    private int mFpDelayPowerTime = 0;
    private IAuthenticator mIAuthenticator = new IAuthenticator.Stub() {
        public int verifyUser(IFingerprintServiceReceiver receiver, IAuthenticatorListener listener, int userid, byte[] nonce, String aaid) {
            Log.d(HwFingerprintService.TAG, "verifyUser");
            if (!HwFingerprintService.this.isCurrentUserOrProfile(UserHandle.getCallingUserId())) {
                Log.w(HwFingerprintService.TAG, "Can't authenticate non-current user");
                return -1;
            } else if (receiver == null || listener == null || nonce == null || aaid == null) {
                Log.e(HwFingerprintService.TAG, "wrong paramers.");
                return -1;
            } else {
                int uid = Binder.getCallingUid();
                int pid = Binder.getCallingPid();
                Log.d(HwFingerprintService.TAG, "uid =" + uid);
                if (uid != 1000) {
                    Log.e(HwFingerprintService.TAG, "permission denied.");
                    return -1;
                }
                Class[] paramTypes = {String.class, Boolean.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE};
                Object[] params = {HwFingerprintService.FIDO_ASM, true, Integer.valueOf(uid), Integer.valueOf(pid), Integer.valueOf(userid)};
                if (!((Boolean) HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "canUseFingerprint", paramTypes, params)).booleanValue()) {
                    Log.w(HwFingerprintService.TAG, "FIDO_ASM can't use fingerprint");
                    return -1;
                }
                int effectiveGroupId = HwFingerprintService.this.getEffectiveUserId(userid);
                final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
                Class[] clsArr = paramTypes;
                final int callingUserId = UserHandle.getCallingUserId();
                AnonymousClass1 r11 = r0;
                final int i = effectiveGroupId;
                Handler handler = HwFingerprintService.this.mHandler;
                final IAuthenticatorListener iAuthenticatorListener = listener;
                final String str = aaid;
                Object[] objArr = params;
                final byte[] bArr = nonce;
                AnonymousClass1 r0 = new Runnable() {
                    public void run() {
                        HwFingerprintService.this.setLivenessSwitch("fido");
                        HwFingerprintService.this.startAuthentication(iFingerprintServiceReceiver.asBinder(), 0, callingUserId, i, iFingerprintServiceReceiver, 0, true, HwFingerprintService.FIDO_ASM, iAuthenticatorListener, str, bArr);
                    }
                };
                handler.post(r11);
                return 0;
            }
        }

        public int cancelVerifyUser(final IFingerprintServiceReceiver receiver, int userId) {
            if (receiver == null) {
                Log.e(HwFingerprintService.TAG, "wrong paramers.");
                return -1;
            }
            Log.d(HwFingerprintService.TAG, "cancelVerify");
            if (!HwFingerprintService.this.isCurrentUserOrProfile(UserHandle.getCallingUserId())) {
                Log.w(HwFingerprintService.TAG, "Can't cancel authenticate non-current user");
                return -1;
            }
            int uId = Binder.getCallingUid();
            Log.d(HwFingerprintService.TAG, "uId =" + uId);
            if (uId != 1000) {
                Log.e(HwFingerprintService.TAG, "permission denied.");
                return -1;
            }
            int pId = Binder.getCallingPid();
            if (!((Boolean) HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "canUseFingerprint", new Class[]{String.class, Boolean.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE}, new Object[]{HwFingerprintService.FIDO_ASM, true, Integer.valueOf(uId), Integer.valueOf(pId), Integer.valueOf(userId)})).booleanValue()) {
                Log.w(HwFingerprintService.TAG, "FIDO_ASM can't cancel fingerprint auth");
                return -1;
            }
            HwFingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    HwFingerprintService.this.stopFidoClient(receiver.asBinder());
                }
            });
            return 0;
        }
    };
    private IWindowManager mIWm;
    private int mInitDisplayHeight = -1;
    private int mInitDisplayWidth = -1;
    private boolean mIsBlackAuthenticateEvent = false;
    /* access modifiers changed from: private */
    public boolean mIsFingerInScreenSupported;
    private boolean mIsHighLightNeed;
    private boolean mIsInteractive = false;
    /* access modifiers changed from: private */
    public AtomicBoolean mIsKeyguardAuthenStatus = new AtomicBoolean(false);
    private boolean mIsPowerKeyDown = false;
    private boolean mIsSupportDualFingerprint = false;
    private boolean mIsUdAuthenticating = false;
    private boolean mIsUdEnrolling = false;
    private boolean mIsUdFingerprintChecking = false;
    private boolean mIsUdFingerprintNeed = true;
    private boolean mKeepMaskAfterAuthentication = false;
    private long mLastAuthenticatedEndTime = 0;
    private long mLastAuthenticatedStartTime = 0;
    private long mLastFingerDownTime = 0;
    private long mLastPowerKeyDownTime = 0;
    private long mLastPowerKeyUpTime = 0;
    private Bundle mMaskViewBundle;
    private ContentObserver mNavModeObserver = new ContentObserver(null) {
        public void onChange(boolean selfChange) {
            if (HwFingerprintService.this.mContext == null || HwFingerprintService.this.mContext.getContentResolver() == null) {
                Log.d(HwFingerprintService.TAG, "mContext or the resolver is null");
                return;
            }
            boolean virNavModeEnabled = FrontFingerPrintSettings.isNaviBarEnabled(HwFingerprintService.this.mContext.getContentResolver());
            if (HwFingerprintService.this.mVirNavModeEnabled != virNavModeEnabled) {
                boolean unused = HwFingerprintService.this.mVirNavModeEnabled = virNavModeEnabled;
                Log.d(HwFingerprintService.TAG, "Navigation mode changed, mVirNavModeEnabled = " + HwFingerprintService.this.mVirNavModeEnabled);
                HwFingerprintService.this.sendCommandToHal(HwFingerprintService.this.mVirNavModeEnabled ? 45 : 43);
            }
        }
    };
    private boolean mNeedResumeTouchSwitch = false;
    private String mPackageDisableMask;
    private int mPowerDelayFpTime = 0;
    private Runnable mPowerFingerWakeUpRunable = new Runnable() {
        public void run() {
            Slog.i(HwFingerprintService.TAG, "powerfp wakeup run");
            HwFingerprintService.this.mIsKeyguardAuthenStatus.set(false);
            HwFingerprintService.this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.policy:POWER_FINGERPRINT");
        }
    };
    /* access modifiers changed from: private */
    public AlertDialog mReEnrollDialog;
    private BroadcastReceiver mReceiver;
    /* access modifiers changed from: private */
    public String mScreen;
    /* access modifiers changed from: private */
    public int mState = 0;
    private boolean mSupportBlackAuthentication = false;
    private boolean mSupportPowerFp = false;
    private BroadcastReceiver mSwitchFrequenceMonitor = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.SCREEN_ON") || action.equals("android.intent.action.SCREEN_OFF")) {
                    String unused = HwFingerprintService.this.mScreen = action;
                    HwFingerprintService.this.handleScreenOnOrOff();
                }
            }
        }
    };
    private BroadcastReceiver mUserDeletedMonitor = new BroadcastReceiver() {
        private static final String FP_DATA_DIR = "fpdata";

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.USER_REMOVED")) {
                    int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    Slog.i(HwFingerprintService.TAG, "user deleted:" + userId);
                    if (userId == -1) {
                        Slog.i(HwFingerprintService.TAG, "get User id failed");
                        return;
                    }
                    int newUserId = userId;
                    int newPathId = userId;
                    if (UserManagerEx.isHwHiddenSpace(UserManagerEx.getUserInfoEx(UserManager.get(HwFingerprintService.this.mContext), userId))) {
                        newUserId = -100;
                        newPathId = 0;
                    }
                    File fpDir = new File(Environment.getFingerprintFileDirectory(newPathId), FP_DATA_DIR);
                    if (!fpDir.exists()) {
                        Slog.v(HwFingerprintService.TAG, "no fpdata!");
                        return;
                    }
                    HwFingerprintService.this.removeUserData(newUserId, fpDir.getAbsolutePath());
                } else if (action.equals("android.intent.action.USER_PRESENT")) {
                    if (HwFingerprintService.mCheckNeedEnroll) {
                        if (HwFingerprintService.mRemoveOldTemplatesFeature) {
                            if (HwFingerprintService.mIsChinaArea) {
                                HwFingerprintService.this.sendCommandToHal(HwFingerprintService.MSG_CHECK_AND_DEL_TEMPLATES);
                            } else {
                                HwFingerprintService.this.sendCommandToHal(HwFingerprintService.MSG_CHECK_OLD_TEMPLATES);
                            }
                        }
                        int checkValReEnroll = HwFingerprintService.this.checkNeedReEnrollFingerPrints();
                        int checkValCalibrate = HwFingerprintService.this.checkNeedCalibrateFingerPrint();
                        Log.e(HwFingerprintService.TAG, "USER_PRESENT mUserDeletedMonitor need enrol : " + checkValReEnroll + "need calibrate:" + checkValCalibrate);
                        if (HwFingerprintService.mRemoveOldTemplatesFeature) {
                            if (HwFingerprintService.mIsChinaArea && checkValReEnroll == 1) {
                                HwFingerprintService.this.updateActiveGroupEx(-100);
                                HwFingerprintService.this.updateActiveGroupEx(0);
                                HwFingerprintService.this.showDialog(false);
                            } else if (!HwFingerprintService.mIsChinaArea && checkValReEnroll == 3) {
                                HwFingerprintService.this.showDialog(true);
                            }
                        } else if (checkValReEnroll == 1 && checkValCalibrate != 1) {
                            HwFingerprintService.this.intentOthers(context);
                        }
                        boolean unused = HwFingerprintService.mCheckNeedEnroll = false;
                    }
                    if (HwFingerprintService.this.mFingerViewController != null && "com.android.systemui".equals(HwFingerprintService.this.mFingerViewController.getCurrentPackage())) {
                        Log.d(HwFingerprintService.TAG, "USER_PRESENT removeMaskOrButton");
                        HwFingerprintService.this.mFingerViewController.removeMaskOrButton();
                    }
                    boolean unused2 = HwFingerprintService.this.mForbideKeyguardCall = false;
                    if (HwFingerprintService.FACE_DETECT_REASON.equals(intent.getStringExtra("unlockReason"))) {
                        HwFingerprintService.this.sendCommandToHal(212);
                    } else {
                        HwFingerprintService.this.sendCommandToHal(63);
                    }
                }
            }
        }
    };
    private BroadcastReceiver mUserSwitchReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.d(HwFingerprintService.TAG, "intent is null");
                return;
            }
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                boolean unused = HwFingerprintService.this.mVirNavModeEnabled = FrontFingerPrintSettings.isNaviBarEnabled(HwFingerprintService.this.mContext.getContentResolver());
                Log.d(HwFingerprintService.TAG, "Read the navigation mode after user switch, mVirNavModeEnabled = " + HwFingerprintService.this.mVirNavModeEnabled);
                HwFingerprintService.this.sendCommandToHal(HwFingerprintService.this.mVirNavModeEnabled ? 45 : 43);
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mVirNavModeEnabled = false;
    private HashSet<String> mWhitelist = new HashSet<>();
    private WindowManagerInternal mWindowManagerInternal;
    private boolean mflagFirstIn = true;
    private long mtimeStart = 0;
    private final Runnable screenOnOrOffRunnable = new Runnable() {
        public void run() {
            boolean z = false;
            int fpState = Settings.Secure.getIntForUser(HwFingerprintService.this.mContext.getContentResolver(), HwFingerprintService.KEY_KEYGUARD_ENABLE, 0, ActivityManager.getCurrentUser());
            if (FingerprintUtils.getInstance().getFingerprintsForUser(HwFingerprintService.this.mContext, ActivityManager.getCurrentUser()).size() > 0) {
                z = true;
            }
            boolean hasFingerprints = z;
            if (fpState != 0 && hasFingerprints) {
                return;
            }
            if ("android.intent.action.SCREEN_ON".equals(HwFingerprintService.this.mScreen)) {
                HwFingerprintService.this.sendCommandToHal(HwFingerprintService.MSG_UPGRADING_FREQUENCE);
            } else if ("android.intent.action.SCREEN_OFF".equals(HwFingerprintService.this.mScreen)) {
                HwFingerprintService.this.sendCommandToHal(HwFingerprintService.MSG_REDUCING_FREQUENCE);
            }
        }
    };
    private int typeDetails = -1;

    public class AODFaceUpdateMonitor {
        private static final String ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE = "huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE";
        private static final String ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE = "huawei.intent.action.HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE";
        private static final int CLOSE_SAVE_POWER = 0;
        private static final String FACE_KEYGUARD_WITH_LOCK = "face_bind_with_lock";
        private static final String FACE_RECOGNIZE_SLIDE_UNLOCK = "face_recognize_slide_unlock";
        private static final String FACE_RECOGNIZE_UNLOCK = "face_recognize_unlock";
        private static final int OPEN_SAVE_POWER = 3;
        private static final String PERMISSION = "com.huawei.android.launcher.permission.CHANGE_POWERMODE";
        private static final String POWER_MODE = "power_mode";
        private static final String SHUTDOWN_LIMIT_POWERMODE = "shutdomn_limit_powermode";
        private AODFaceTrustListener mAodFaceTrustListener;
        private boolean mIsFaceDetectEnabled;
        private boolean mIsPrimaryUser;
        /* access modifiers changed from: private */
        public boolean mIsSuperPowerEnabled;
        private boolean mIsSupportAODFace;
        /* access modifiers changed from: private */
        public boolean mIsTrustEnabled;
        /* access modifiers changed from: private */
        public boolean mIsTrustManageEnabled;
        /* access modifiers changed from: private */
        public boolean mIsTrustUnlock;
        protected final ContentObserver mSettingsObserver = new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                AODFaceUpdateMonitor.this.notifyFaceSettingModify(false);
            }
        };
        private TrustManager mTrustManager;
        private boolean mfaceStatus;
        private final BroadcastReceiver powerReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    Slog.i(HwFingerprintService.TAG, "super power action:" + action);
                    if (AODFaceUpdateMonitor.ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE.equals(action)) {
                        if (intent.getIntExtra(AODFaceUpdateMonitor.POWER_MODE, 0) == 3) {
                            boolean unused = AODFaceUpdateMonitor.this.mIsSuperPowerEnabled = true;
                        }
                    } else if (AODFaceUpdateMonitor.ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE.equals(action) && intent.getIntExtra(AODFaceUpdateMonitor.SHUTDOWN_LIMIT_POWERMODE, 0) == 0) {
                        boolean unused2 = AODFaceUpdateMonitor.this.mIsSuperPowerEnabled = false;
                    }
                    AODFaceUpdateMonitor.this.sendFaceStatusToHal(false);
                }
            }
        };

        public class AODFaceTrustListener implements TrustManager.TrustListener {
            private static final String CONTENT_URI = "content://com.android.huawei.keyguardstate";
            private static final String KCALL_GET_STRONG_AUTH_STATE = "getStrongAuthState";

            public AODFaceTrustListener() {
            }

            private void updateTrustUnlockStatus() {
                if (AODFaceUpdateMonitor.this.isSupportAODFace()) {
                    if (!AODFaceUpdateMonitor.this.mIsTrustEnabled || !AODFaceUpdateMonitor.this.mIsTrustManageEnabled) {
                        boolean unused = AODFaceUpdateMonitor.this.mIsTrustUnlock = false;
                    } else {
                        Slog.i(HwFingerprintService.TAG, "update TrustUnlock Status");
                        try {
                            Bundle outData = HwFingerprintService.this.mContext.getContentResolver().call(Uri.parse(CONTENT_URI), KCALL_GET_STRONG_AUTH_STATE, null, null);
                            if (outData == null || outData.getInt("result") != 0 || outData.getBoolean("StrongAuth")) {
                                boolean unused2 = AODFaceUpdateMonitor.this.mIsTrustUnlock = false;
                            } else {
                                boolean unused3 = AODFaceUpdateMonitor.this.mIsTrustUnlock = true;
                            }
                        } catch (Exception e) {
                            Slog.e(HwFingerprintService.TAG, "getStrongAuthState Exception", e);
                        }
                    }
                    AODFaceUpdateMonitor.this.sendFaceStatusToHal(false);
                }
            }

            public void onTrustChanged(boolean enabled, int userId, int flags) {
                boolean unused = AODFaceUpdateMonitor.this.mIsTrustEnabled = enabled;
                updateTrustUnlockStatus();
            }

            public void onTrustManagedChanged(boolean enabled, int userId) {
                boolean unused = AODFaceUpdateMonitor.this.mIsTrustManageEnabled = enabled;
                updateTrustUnlockStatus();
            }

            public void onTrustError(CharSequence message) {
                Slog.d(HwFingerprintService.TAG, "onTrustError:message " + message.toString());
            }
        }

        public AODFaceUpdateMonitor() {
            if (HwFingerprintService.this.mIsFingerInScreenSupported || HwFingerprintService.this.isSupportPowerFp()) {
                String board = SystemProperties.get("ro.product.board", "UNKOWN").toUpperCase(Locale.US);
                if (board.contains("TNY") || board.contains("TONY") || board.contains("NEO")) {
                    this.mIsSupportAODFace = false;
                    return;
                }
                this.mIsSupportAODFace = true;
                ContentResolver resolver = HwFingerprintService.this.mContext.getContentResolver();
                resolver.registerContentObserver(Settings.Secure.getUriFor(FACE_KEYGUARD_WITH_LOCK), false, this.mSettingsObserver, 0);
                resolver.registerContentObserver(Settings.Secure.getUriFor(FACE_RECOGNIZE_SLIDE_UNLOCK), false, this.mSettingsObserver, 0);
                resolver.registerContentObserver(Settings.Secure.getUriFor(FACE_RECOGNIZE_UNLOCK), false, this.mSettingsObserver, 0);
                IntentFilter powerFilter = new IntentFilter();
                powerFilter.addAction(ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE);
                powerFilter.addAction(ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE);
                HwFingerprintService.this.mContext.registerReceiver(this.powerReceiver, powerFilter, PERMISSION, null);
                try {
                    this.mAodFaceTrustListener = new AODFaceTrustListener();
                    this.mTrustManager = (TrustManager) HwFingerprintService.this.mContext.getSystemService("trust");
                    this.mTrustManager.registerTrustListener(this.mAodFaceTrustListener);
                } catch (Exception e) {
                    Slog.e(HwFingerprintService.TAG, "create AODFaceUpdateMonitor Exception", e);
                }
                getFaceSetting();
                return;
            }
            this.mIsSupportAODFace = false;
        }

        /* access modifiers changed from: private */
        public boolean isSupportAODFace() {
            return this.mIsSupportAODFace;
        }

        /* access modifiers changed from: private */
        public void notifyFaceSettingModify(boolean isBooting) {
            getFaceSetting();
            sendFaceStatusToHal(isBooting);
        }

        private void getFaceSetting() {
            ContentResolver resolver = HwFingerprintService.this.mContext.getContentResolver();
            boolean z = false;
            int faceKeyguardWithLock = Settings.Secure.getIntForUser(resolver, FACE_KEYGUARD_WITH_LOCK, -1, 0);
            if (faceKeyguardWithLock == -1) {
                if (Settings.Secure.getIntForUser(resolver, FACE_RECOGNIZE_UNLOCK, 0, 0) == 1 || Settings.Secure.getIntForUser(resolver, FACE_RECOGNIZE_SLIDE_UNLOCK, 0, 0) == 1) {
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
                if (userId == 0) {
                    this.mIsPrimaryUser = true;
                } else {
                    this.mIsPrimaryUser = false;
                }
                if ("com.android.systemui".equals(opPackageName)) {
                    sendFaceStatusToHal(false);
                }
            }
        }

        public void sendFaceStatusToHal(boolean isBooting) {
            if (isSupportAODFace()) {
                boolean faceStatus = this.mIsFaceDetectEnabled && !this.mIsSuperPowerEnabled && this.mIsPrimaryUser && !this.mIsTrustUnlock;
                Slog.i(HwFingerprintService.TAG, "sendFaceStatusToHal:mIsFaceDetectEnabled " + this.mIsFaceDetectEnabled + ",mIsSuperPowerEnabled " + this.mIsSuperPowerEnabled + ",mIsPrimaryUser " + this.mIsPrimaryUser + ",mIsTrustUnlock " + this.mIsTrustUnlock + ",faceStatus " + faceStatus + ",mfaceStatus " + this.mfaceStatus + ",isBooting " + isBooting);
                if (isBooting || faceStatus != this.mfaceStatus) {
                    int cmd = faceStatus ? 211 : 210;
                    this.mfaceStatus = faceStatus;
                    HwFingerprintService.this.sendCommandToHal(cmd);
                }
            }
        }
    }

    private class AuthenticatedParam {
        private long mDeviceId;
        private int mFingerId;
        private int mGroupId;
        private AtomicBoolean mIsWaitPowerEvent;
        private ArrayList<Byte> mToken;

        private AuthenticatedParam() {
            this.mIsWaitPowerEvent = new AtomicBoolean(false);
        }

        public void setAuthenticatedParam(long deviceId, int fingerId, int groupId, ArrayList<Byte> token) {
            this.mDeviceId = deviceId;
            this.mFingerId = fingerId;
            this.mGroupId = groupId;
            this.mToken = token;
            this.mIsWaitPowerEvent.set(true);
        }

        public void setNoWaitPowerEvent() {
            this.mIsWaitPowerEvent.set(false);
        }

        public boolean isWaitPowerEvent() {
            return this.mIsWaitPowerEvent.getAndSet(false);
        }

        public long getDeviceId() {
            return this.mDeviceId;
        }

        public int getFingerId() {
            return this.mFingerId;
        }

        public int getGroupId() {
            return this.mGroupId;
        }

        public ArrayList<Byte> getToken() {
            return this.mToken;
        }
    }

    private class FingerViewChangeCallback implements FingerViewController.ICallBack {
        private FingerViewChangeCallback() {
        }

        public void onFingerViewStateChange(int type) {
            Log.d(HwFingerprintService.TAG, "View State Change to " + type);
            HwFingerprintService.this.suspendAuthentication(type == 2 ? 1 : 0);
        }

        public void onNotifyCaptureImage() {
            Log.d(HwFingerprintService.TAG, "onNotifyCaptureImage");
            HwFingerprintService.this.notifyCaptureOpticalImage();
        }

        public void onNotifyBlueSpotDismiss() {
            HwFingerprintService.this.notifyBluespotDismiss();
        }
    }

    private class HwFIDOAuthenticationClient extends AuthenticationClient {
        private String aaid;
        private int groupId;
        /* access modifiers changed from: private */
        public IAuthenticatorListener listener;
        private IFidoAuthenticationCallback mFidoAuthenticationCallback = new IFidoAuthenticationCallback.Stub() {
            public void onUserVerificationResult(final int result, long opId, final ArrayList<Byte> userId, final ArrayList<Byte> encapsulatedResult) {
                Log.d(HwFingerprintService.TAG, "onUserVerificationResult");
                HwFIDOAuthenticationClient.this.this$0.mHandler.post(new Runnable() {
                    public void run() {
                        Log.d(HwFingerprintService.TAG, "onUserVerificationResult-run");
                        HwFIDOAuthenticationClient.this.this$0.resumeTouchSwitch();
                        if (HwFIDOAuthenticationClient.this.listener != null) {
                            try {
                                byte[] byteUserId = new byte[userId.size()];
                                int userIdLen = userId.size();
                                for (int i = 0; i < userIdLen; i++) {
                                    byteUserId[i] = ((Byte) userId.get(i)).byteValue();
                                }
                                byte[] byteEncapsulatedResult = new byte[encapsulatedResult.size()];
                                int encapsulatedResultLen = encapsulatedResult.size();
                                for (int i2 = 0; i2 < encapsulatedResultLen; i2++) {
                                    byteEncapsulatedResult[i2] = ((Byte) encapsulatedResult.get(i2)).byteValue();
                                }
                                HwFIDOAuthenticationClient.this.listener.onUserVerificationResult(result, byteUserId, byteEncapsulatedResult);
                            } catch (RemoteException e) {
                                Log.w(HwFingerprintService.TAG, "onUserVerificationResult RemoteException");
                            }
                        }
                    }
                });
            }
        };
        private byte[] nonce;
        private String pkgName;
        final /* synthetic */ HwFingerprintService this$0;
        private int user_id;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public HwFIDOAuthenticationClient(HwFingerprintService hwFingerprintService, Context context, long halDeviceId, IBinder token, IFingerprintServiceReceiver receiver, int callingUserId, int groupId2, long opId, boolean restricted, String owner, IAuthenticatorListener listener2, String aaid2, byte[] nonce2) {
            super(context, halDeviceId, token, receiver, callingUserId, groupId2, opId, restricted, owner, null, null, null);
            this.this$0 = hwFingerprintService;
            this.pkgName = owner;
            this.listener = listener2;
            this.groupId = groupId2;
            this.aaid = aaid2;
            this.nonce = nonce2;
            this.user_id = callingUserId;
        }

        public boolean onAuthenticated(int fingerId, int groupId2) {
            if (fingerId != 0) {
            }
            return HwFingerprintService.super.onAuthenticated(fingerId, groupId2);
        }

        public int handleFailedAttempt() {
            int currentUser = ActivityManager.getCurrentUser();
            SparseIntArray failedAttempts = (SparseIntArray) HwFingerprintService.getParentPrivateField(this.this$0, "mFailedAttempts");
            failedAttempts.put(currentUser, failedAttempts.get(currentUser, 0) + 1);
            int lockoutMode = this.this$0.getLockoutMode();
            if (!inLockoutMode()) {
                return 0;
            }
            HwFingerprintService.setParentPrivateField(this.this$0, "mLockoutTime", Long.valueOf(SystemClock.elapsedRealtime()));
            Object unused = HwFingerprintService.invokeParentPrivateFunction(this.this$0, "scheduleLockoutResetForUser", new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(this.user_id)});
            onError(7, 0);
            stop(true);
            return lockoutMode;
        }

        public void resetFailedAttempts() {
            Object unused = HwFingerprintService.invokeParentPrivateFunction(this.this$0, "resetFailedAttemptsForUser", new Class[]{Boolean.TYPE, Integer.TYPE}, new Object[]{true, Integer.valueOf(this.user_id)});
        }

        public void notifyUserActivity() {
            Object unused = HwFingerprintService.invokeParentPrivateFunction(this.this$0, "userActivity", null, null);
        }

        public IBiometricsFingerprint getFingerprintDaemon() {
            return (IBiometricsFingerprint) HwFingerprintService.invokeParentPrivateFunction(this.this$0, "getFingerprintDaemon", null, null);
        }

        public void handleHwFailedAttempt(int flags, String packagesName) {
            Object unused = HwFingerprintService.invokeParentPrivateFunction(this.this$0, "handleHwFailedAttempt", new Class[]{Integer.TYPE, String.class}, new Object[]{0, null});
        }

        public boolean inLockoutMode() {
            return ((Boolean) HwFingerprintService.invokeParentPrivateFunction(this.this$0, "inLockoutMode", null, null)).booleanValue();
        }

        public int start() {
            Slog.d("FingerprintService", "start pkgName:" + this.pkgName);
            try {
                doVerifyUser(this.groupId, this.aaid, this.nonce);
            } catch (RemoteException e) {
                Log.w(HwFingerprintService.TAG, "call fingerprintD verify user failed");
            }
            return 0;
        }

        public void onStart() {
        }

        public void onStop() {
        }

        private void doVerifyUser(int groupId2, String aaid2, byte[] nonce2) throws RemoteException {
            if (this.this$0.isFingerprintDReady()) {
                IExtBiometricsFingerprint daemon = this.this$0.getFingerprintDaemonEx();
                if (daemon == null) {
                    Slog.e("FingerprintService", "Fingerprintd is not available!");
                    return;
                }
                ArrayList<Byte> arrayNonce = new ArrayList<>();
                for (byte valueOf : nonce2) {
                    arrayNonce.add(Byte.valueOf(valueOf));
                }
                try {
                    daemon.verifyUser(this.mFidoAuthenticationCallback, groupId2, aaid2, arrayNonce);
                } catch (RemoteException e) {
                    Slog.e("FingerprintService", "doVerifyUser RemoteException:" + e);
                }
            }
        }
    }

    public void serviceDied(long cookie) {
        HwFingerprintService.super.serviceDied(cookie);
        this.mDaemonEx = null;
    }

    /* access modifiers changed from: private */
    public IExtBiometricsFingerprint getFingerprintDaemonEx() {
        if (this.mDaemonEx != null) {
            return this.mDaemonEx;
        }
        try {
            this.mDaemonEx = IExtBiometricsFingerprint.getService();
        } catch (NoSuchElementException e) {
        } catch (RemoteException e2) {
            Slog.e(TAG, "Failed to get biometric interface", e2);
        }
        Slog.w(TAG, "getFingerprintDaemonEx inst = " + this.mDaemonEx);
        return this.mDaemonEx;
    }

    private int getkidsFingerId(String whichMode, int userID, Context context) {
        if (context != null) {
            return Settings.Secure.getIntForUser(context.getContentResolver(), whichMode, 0, userID);
        }
        Slog.w(TAG, "getkidsFingerId - context = null");
        return 0;
    }

    private boolean isKidSwitchOn(int userID, Context context) {
        if (1 == Settings.Secure.getIntForUser(context.getContentResolver(), KEY_DB_CHILDREN_MODE_STATUS, 0, userID)) {
            return true;
        }
        return false;
    }

    private boolean isParentControl(int userID, Context context) {
        boolean isInStudent = false;
        if (context == null || context.getContentResolver() == null) {
            return false;
        }
        int status = Settings.Secure.getIntForUser(context.getContentResolver(), PATH_CHILDMODE_STATUS, 0, userID);
        Slog.d(TAG, "ParentControl status is " + status);
        if (status == 1) {
            isInStudent = true;
        }
        return isInStudent;
    }

    /* access modifiers changed from: protected */
    public void setKidsFingerprint(int userID, boolean isKeyguard) {
        Slog.d(TAG, "setKidsFingerprint:start");
        int kidFpId = getkidsFingerId(KEY_DB_CHILDREN_MODE_FPID, userID, this.mContext);
        if (kidFpId != 0) {
            boolean isParent = isParentControl(userID, this.mContext);
            boolean isPcCastMode = HwPCUtils.isPcCastModeInServer();
            Slog.d(TAG, "setKidsFingerprint-isParent = " + isParent + ", isPcCastMode =" + isPcCastMode);
            if (isKeyguard && isKidSwitchOn(userID, this.mContext) && !isParent && !isPcCastMode) {
                kidFpId = 0;
            }
            Slog.d(TAG, "setKidsFingerprint-kidFpId = " + kidFpId);
            IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
            if (daemon == null) {
                Slog.e(TAG, "Fingerprintd is not available!");
                return;
            }
            try {
                daemon.setKidsFingerprint(kidFpId);
            } catch (RemoteException e) {
                Slog.e(TAG, "setLivenessSwitch RemoteException:" + e);
            }
            Slog.d(TAG, "framework setLivenessSwitch is ok ---end");
        }
    }

    /* JADX WARNING: type inference failed for: r2v4, types: [boolean] */
    /* JADX WARNING: type inference failed for: r2v5 */
    /* JADX WARNING: type inference failed for: r2v6 */
    /* access modifiers changed from: private */
    public void startAuthentication(IBinder token, long opId, int callingUserId, int groupId, IFingerprintServiceReceiver receiver, int flags, boolean restricted, String opPackageName, IAuthenticatorListener listener, String aaid, byte[] nonce) {
        char c;
        ? r2;
        String str = opPackageName;
        Class[] paramTypes = {Integer.TYPE, String.class};
        Object[] params = {Integer.valueOf(groupId), str};
        invokeParentPrivateFunction(this, "updateActiveGroup", paramTypes, params);
        Log.v(TAG, "HwFingerprintService-startAuthentication(" + str + ")");
        Class[] clsArr = paramTypes;
        Object[] objArr = params;
        Object obj = str;
        AuthenticationClient client = new HwFIDOAuthenticationClient(this, getContext(), 0, token, receiver, callingUserId, groupId, opId, restricted, str, listener, aaid, nonce);
        if (((Boolean) invokeParentPrivateFunction(this, "inLockoutMode", null, null)).booleanValue()) {
            r2 = 1;
            c = 0;
            if (!((Boolean) invokeParentPrivateFunction(this, "isKeyguard", new Class[]{String.class}, new Object[]{obj})).booleanValue()) {
                Log.v(TAG, "In lockout mode; disallowing authentication");
                if (!client.onError(7, 0)) {
                    Log.w(TAG, "Cannot send timeout message to client");
                }
                return;
            }
        } else {
            r2 = 1;
            c = 0;
        }
        Class[] clsArr2 = new Class[2];
        clsArr2[c] = ClientMonitor.class;
        clsArr2[r2] = Boolean.TYPE;
        Object[] objArr2 = new Object[2];
        objArr2[c] = client;
        objArr2[r2] = Boolean.valueOf(r2);
        invokeParentPrivateFunction(this, "startClient", clsArr2, objArr2);
    }

    /* access modifiers changed from: private */
    public void stopFidoClient(IBinder token) {
        ClientMonitor currentClient = this.mCurrentClient;
        if (currentClient != null) {
            if (currentClient instanceof AuthenticationClient) {
                if (currentClient.getToken() == token) {
                    Log.v(TAG, "stop client " + currentClient.getOwnerString());
                    currentClient.stop(true);
                } else {
                    Log.v(TAG, "can't stop client " + currentClient.getOwnerString() + " since tokens don't match");
                }
            } else if (currentClient != null) {
                Log.v(TAG, "can't cancel non-authenticating client" + currentClient.getOwnerString());
            }
        }
    }

    private static Field getAccessibleField(Class targetClass, String variableName) {
        try {
            final Field field = targetClass.getDeclaredField(variableName);
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    field.setAccessible(true);
                    return null;
                }
            });
            return field;
        } catch (Exception e) {
            Log.v(TAG, "getAccessibleField error", e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static Object getParentPrivateField(Object instance, String variableName) {
        Class targetClass = instance.getClass().getSuperclass();
        Object superInst = targetClass.cast(instance);
        Field field = getAccessibleField(targetClass, variableName);
        if (field != null) {
            try {
                return field.get(superInst);
            } catch (IllegalAccessException e) {
                Log.v(TAG, "getParentPrivateField error", e);
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public static void setParentPrivateField(Object instance, String variableName, Object value) {
        Class targetClass = instance.getClass().getSuperclass();
        Object superInst = targetClass.cast(instance);
        Field field = getAccessibleField(targetClass, variableName);
        if (field != null) {
            try {
                field.set(superInst, value);
            } catch (IllegalAccessException e) {
                Log.v(TAG, "setParentPrivateField error", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public static Object invokeParentPrivateFunction(Object instance, String method, Class[] paramTypes, Object[] params) {
        Class targetClass = instance.getClass().getSuperclass();
        Object superInst = targetClass.cast(instance);
        try {
            final Method med = targetClass.getDeclaredMethod(method, paramTypes);
            AccessController.doPrivileged(new PrivilegedAction() {
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

    /* access modifiers changed from: private */
    public void showDialog(final boolean withConfirm) {
        int i;
        int i2;
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, 33947691);
        if (withConfirm) {
            i = 33686213;
        } else {
            i = 33686210;
        }
        AlertDialog.Builder title = builder.setPositiveButton(i, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean unused = HwFingerprintService.mNeedRecreateDialog = false;
                if (withConfirm) {
                    HwFingerprintService.this.sendCommandToHal(HwFingerprintService.MSG_DEL_OLD_TEMPLATES);
                    if (HwFingerprintService.this.checkNeedReEnrollFingerPrints() == 1) {
                        HwFingerprintService.this.updateActiveGroupEx(-100);
                        HwFingerprintService.this.updateActiveGroupEx(0);
                    }
                }
                HwFingerprintService.this.intentOthers(HwFingerprintService.this.mContext);
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (!HwFingerprintService.mNeedRecreateDialog) {
                    HwFingerprintService.this.unRegisterPhoneStateReceiver();
                }
            }
        }).setTitle(this.mContext.getString(33685797));
        Context context = this.mContext;
        if (withConfirm) {
            i2 = 33686212;
        } else {
            i2 = 33686211;
        }
        AlertDialog.Builder builder2 = title.setMessage(context.getString(i2));
        if (withConfirm) {
            builder2.setNegativeButton(17039360, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    boolean unused = HwFingerprintService.mNeedRecreateDialog = false;
                    HwFingerprintService.this.mReEnrollDialog.dismiss();
                }
            });
        }
        this.mReEnrollDialog = builder2.create();
        if (this.mReEnrollDialog != null) {
            this.mReEnrollDialog.getWindow().setType(2003);
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
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null) {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                    if (telephonyManager != null && HwFingerprintService.this.mReEnrollDialog != null) {
                        if (telephonyManager.getCallState() == 1 && HwFingerprintService.this.mReEnrollDialog.isShowing()) {
                            boolean unused = HwFingerprintService.mNeedRecreateDialog = true;
                            HwFingerprintService.this.mReEnrollDialog.dismiss();
                        } else if (telephonyManager.getCallState() == 0 && !HwFingerprintService.this.mReEnrollDialog.isShowing()) {
                            HwFingerprintService.this.mReEnrollDialog.show();
                        }
                    }
                }
            }
        };
        if (this.mContext != null) {
            this.mContext.registerReceiver(this.mReceiver, filter);
        }
    }

    /* access modifiers changed from: private */
    public void unRegisterPhoneStateReceiver() {
        if (this.mReceiver != null && this.mContext != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
        }
    }

    /* access modifiers changed from: private */
    public void updateActiveGroupEx(int userId) {
        File systemDir;
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon != null) {
            int userIdForHal = userId;
            try {
                UserInfoEx infoEx = UserManagerEx.getUserInfoEx(UserManager.get(this.mContext), userId);
                if (infoEx != null && UserManagerEx.isHwHiddenSpace(infoEx)) {
                    userIdForHal = -100;
                    Slog.i(TAG, "userIdForHal is " + -100);
                }
                if (userIdForHal == -100) {
                    Slog.i(TAG, "userIdForHal == HIDDEN_SPACE_ID");
                    systemDir = Environment.getUserSystemDirectory(0);
                } else {
                    systemDir = Environment.getUserSystemDirectory(userId);
                }
                File fpDir = new File(systemDir, "fpdata");
                if (!fpDir.exists()) {
                    if (!fpDir.mkdir()) {
                        Slog.v(TAG, "Cannot make directory: " + fpDir.getAbsolutePath());
                        return;
                    } else if (!SELinux.restorecon(fpDir)) {
                        Slog.w(TAG, "Restorecons failed. Directory will have wrong label.");
                        return;
                    }
                }
                daemon.setActiveGroup(userIdForHal, fpDir.getAbsolutePath());
                updateFingerprints(userId);
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to setActiveGroup():", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleScreenOnOrOff() {
        this.mHandler.removeCallbacks(this.screenOnOrOffRunnable);
        this.mHandler.post(this.screenOnOrOffRunnable);
    }

    private boolean isBetaUser() {
        int userType = SystemProperties.getInt("ro.logsystem.usertype", 0);
        if (userType == 3 || userType == 5) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void intentOthers(Context context) {
        Intent intent = new Intent();
        if (SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false)) {
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

    public HwFingerprintService(Context context) {
        super(context);
        this.mContext = context;
        this.mDisplayEngineManager = new DisplayEngineManager();
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        initPropHwFpType();
    }

    private void initPropHwFpType() {
        String config = SystemProperties.get("ro.config.hw_fp_type", "");
        Slog.i(TAG, "powerfp initPropHwFpType config=" + config);
        String[] bufs = config.split(",");
        if (bufs.length == 4) {
            this.mSupportPowerFp = "3".equals(bufs[0]);
            this.mSupportBlackAuthentication = "1".equals(bufs[1]);
            this.mFpDelayPowerTime = Integer.parseInt(bufs[2]);
            this.mPowerDelayFpTime = Integer.parseInt(bufs[3]);
        }
    }

    private void initObserver() {
        if (this.fpObserver == null) {
            Log.w(TAG, "fpObserver is null");
            return;
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(KEY_KEYGUARD_ENABLE), false, this.fpObserver, -1);
        this.mState = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), KEY_KEYGUARD_ENABLE, 0, ActivityManager.getCurrentUser());
        this.fpObserver.onChange(true);
    }

    private void updateTPState(String opPackageName, boolean udFingerprintExist) {
        if (!"com.android.systemui".equals(opPackageName)) {
            sendCommandToHal(1);
        } else if (!udFingerprintExist || this.mState == 0) {
            sendCommandToHal(0);
        } else {
            sendCommandToHal(1);
        }
        Log.i(TAG, "updateTPState udFingerprintExist " + udFingerprintExist + ",opPackageName:" + opPackageName);
    }

    private void initNavModeObserver() {
        if (this.mNavModeObserver == null) {
            Log.d(TAG, "mNavModeObserver is null");
        } else if (this.mContext == null || this.mContext.getContentResolver() == null) {
            Log.d(TAG, "mContext or the resolver is null");
        } else {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("enable_navbar"), true, this.mNavModeObserver, -1);
            this.mVirNavModeEnabled = FrontFingerPrintSettings.isNaviBarEnabled(this.mContext.getContentResolver());
            Log.d(TAG, "Read the navigation mode after boot, mVirNavModeEnabled = " + this.mVirNavModeEnabled);
            sendCommandToHal(this.mVirNavModeEnabled ? 45 : 43);
        }
    }

    private void initUserSwitchReceiver() {
        if (this.mUserSwitchReceiver == null) {
            Log.d(TAG, "mUserSwitchReceiver is null");
        } else if (this.mContext == null) {
            Log.d(TAG, "mContext is null");
        } else {
            this.mContext.registerReceiverAsUser(this.mUserSwitchReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.USER_SWITCHED"), null, null);
        }
    }

    public void onStart() {
        HwFingerprintService.super.onStart();
        publishBinderService("fido_authenticator", this.mIAuthenticator.asBinder());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.USER_PRESENT");
        this.mContext.registerReceiver(this.mUserDeletedMonitor, filter);
        Slog.v(TAG, "HwFingerprintService onstart");
        try {
            ActivityManager.getService().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                public void onUserSwitching(int newUserId) {
                    if (HwFingerprintService.this.mFingerViewController != null) {
                        Slog.v(HwFingerprintService.TAG, "onUserSwitching removeMaskOrButton");
                        HwFingerprintService.this.mFingerViewController.removeMaskOrButton();
                        boolean unused = HwFingerprintService.this.mForbideKeyguardCall = true;
                    }
                }

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                    Slog.v(HwFingerprintService.TAG, "onUserSwitchComplete ");
                    boolean unused = HwFingerprintService.this.mForbideKeyguardCall = false;
                    if (HwFingerprintService.this.fpObserver != null) {
                        HwFingerprintService.this.fpObserver.onChange(true);
                    }
                }
            }, TAG);
        } catch (RemoteException e) {
            Slog.e(TAG, "registerUserSwitchObserver fail", e);
        } catch (SecurityException e2) {
            Slog.w(TAG, "registerReceiverAsUser fail ", e2);
        } catch (Throwable th) {
            this.mForbideKeyguardCall = false;
            throw th;
        }
        this.mForbideKeyguardCall = false;
    }

    private int getSwitchFrequenceSupport() {
        int result = -1;
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        try {
            result = daemon.sendCmdToHal(81);
        } catch (RemoteException e) {
            Slog.e(TAG, "checkSwitchFrequenceSupport RemoteException:" + e);
        }
        return result;
    }

    public void onBootPhase(int phase) {
        HwFingerprintService.super.onBootPhase(phase);
        Slog.v(TAG, "HwFingerprintService onBootPhase:" + phase);
        if (phase == 1000) {
            initSwitchFrequence();
            initPositionAndType();
            if (mRemoveFingerprintBGE) {
                initUserSwitchReceiver();
                initNavModeObserver();
            }
            getAodFace().sendFaceStatusToHal(true);
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
        if (FingerprintUtils.getInstance().isDualFp()) {
            Slog.d(TAG, "dualFingerprint-> updateFingerprints");
            sendCommandToHal(106);
            refreshData(remoteGetOldData(), userId, 1);
            refreshData(remoteGetOldData(), userId, 0);
            return;
        }
        refreshData(remoteGetOldData(), userId, 0);
    }

    private void refreshData(HwFingerprintSets hwFpSets, int userId, int deviceIndex) {
        List<Fingerprint> fpOldList;
        if (hwFpSets != null) {
            FingerprintUtils utils = FingerprintUtils.getInstance();
            if (utils != null) {
                ArrayList<Fingerprint> mNewFingerprints = null;
                int fingerprintGpSize = hwFpSets.mFingerprintGroups.size();
                for (int i = 0; i < fingerprintGpSize; i++) {
                    HwFingerprintSets.HwFingerprintGroup fpGroup = hwFpSets.mFingerprintGroups.get(i);
                    int realGroupId = fpGroup.mGroupId;
                    if (fpGroup.mGroupId == -100) {
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
                    fpOldList = utils.getFingerprintsForUser(this.mContext, userId);
                }
                for (Fingerprint oldFp : fpOldList) {
                    if (!checkItemExist(oldFp.getFingerId(), mNewFingerprints)) {
                        utils.removeFingerprintIdForUser(this.mContext, oldFp.getFingerId(), userId);
                    }
                }
                int size = mNewFingerprints.size();
                for (int i2 = 0; i2 < size; i2++) {
                    Fingerprint fp = mNewFingerprints.get(i2);
                    if (utils.isDualFp()) {
                        utils.addFingerprintForUser(this.mContext, fp.getFingerId(), userId, deviceIndex);
                    } else {
                        utils.addFingerprintForUser(this.mContext, fp.getFingerId(), userId);
                    }
                    CharSequence fpName = fp.getName();
                    if (fpName != null && !fpName.toString().isEmpty()) {
                        utils.renameFingerprintForUser(this.mContext, fp.getFingerId(), userId, fpName);
                    }
                }
            }
        }
    }

    public boolean checkPrivacySpaceEnroll(int userId, int currentUserId) {
        if (!UserManagerEx.isHwHiddenSpace(UserManagerEx.getUserInfoEx(UserManager.get(this.mContext), userId)) || currentUserId != 0) {
            return false;
        }
        Slog.v(TAG, "enroll privacy fingerprint in primary user ");
        return true;
    }

    public boolean checkNeedPowerpush() {
        if (this.mflagFirstIn) {
            this.mtimeStart = System.currentTimeMillis();
            this.mflagFirstIn = false;
            return true;
        }
        long timePassed = System.currentTimeMillis() - this.mtimeStart;
        Slog.v(TAG, "timepassed is  " + timePassed);
        this.mtimeStart = System.currentTimeMillis();
        return POWER_PUSH_DOWN_TIME_THR < timePassed;
    }

    public int removeUserData(int groupId, String storePath) {
        if (!isFingerprintDReady()) {
            return -1;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        try {
            daemon.removeUserData(groupId, storePath);
        } catch (RemoteException e) {
            Slog.e(TAG, "checkNeedReEnrollFingerPrints RemoteException:" + e);
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public int checkNeedReEnrollFingerPrints() {
        int result = -1;
        Log.w(TAG, "checkNeedReEnrollFingerPrints");
        if (!isFingerprintDReady()) {
            return -1;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        try {
            result = daemon.checkNeedReEnrollFinger();
        } catch (RemoteException e) {
            Slog.e(TAG, "checkNeedReEnrollFingerPrints RemoteException:" + e);
        }
        Log.w(TAG, "framework checkNeedReEnrollFingerPrints is finish return = " + result);
        return result;
    }

    public boolean onHwTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == CODE_IS_FP_NEED_CALIBRATE_RULE) {
            Slog.d(TAG, "code == CODE_IS_FP_NEED_CALIBRATE_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int result = checkNeedCalibrateFingerPrint();
            reply.writeNoException();
            reply.writeInt(result);
            return true;
        } else if (code == CODE_SET_CALIBRATE_MODE_RULE) {
            Slog.d(TAG, "code == CODE_SET_CALIBRATE_MODE_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            setCalibrateMode(data.readInt());
            reply.writeNoException();
            return true;
        } else if (code == CODE_GET_TOKEN_LEN_RULE) {
            Slog.d(TAG, "code == CODE_GET_TOKEN_LEN_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int result2 = getTokenLen();
            reply.writeNoException();
            reply.writeInt(result2);
            return true;
        } else if (code == CODE_SET_FINGERPRINT_MASK_VIEW_RULE) {
            Slog.d(TAG, "code == CODE_SET_FINGERPRINT_MASK_VIEW_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            setFingerprintMaskView(data.readBundle());
            reply.writeNoException();
            return true;
        } else if (code == CODE_SHOW_FINGERPRINT_VIEW_RULE) {
            Slog.d(TAG, "code == CODE_SHOW_FINGERPRINT_VIEW_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            showFingerprintView();
            reply.writeNoException();
            return true;
        } else if (code == 1106) {
            Slog.d(TAG, "code == CODE_SHOW_FINGERPRINT_BUTTON_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            showSuspensionButton(data.readInt(), data.readInt());
            reply.writeNoException();
            return true;
        } else if (code == 1107) {
            Slog.d(TAG, "code == CODE_REMOVE_FINGERPRINT_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            removeFingerprintView();
            reply.writeNoException();
            return true;
        } else if (code == CODE_GET_HARDWARE_POSITION) {
            Slog.d(TAG, "CODE_GET_HARDWARE_POSITION");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int[] result3 = getFingerprintHardwarePosition();
            reply.writeNoException();
            reply.writeInt(result3[0]);
            reply.writeInt(result3[1]);
            reply.writeInt(result3[2]);
            reply.writeInt(result3[3]);
            return true;
        } else if (code == CODE_GET_HARDWARE_TYPE) {
            Slog.d(TAG, "CODE_GET_HARDWARE_TYPE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int result4 = getFingerprintHardwareType();
            reply.writeNoException();
            reply.writeInt(result4);
            return true;
        } else if (code == CODE_NOTIFY_OPTICAL_CAPTURE) {
            Slog.d(TAG, "CODE_NOTIFY_OPTICAL_CAPTURE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            notifyCaptureOpticalImage();
            reply.writeNoException();
            return true;
        } else if (code == 1108) {
            Slog.d(TAG, "CODE_SUSPEND_AUTHENTICATE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            suspendAuthentication(data.readInt());
            reply.writeNoException();
            return true;
        } else if (code == CODE_SET_HOVER_SWITCH) {
            Slog.d(TAG, "CODE_SET_HOVER_SWITCH");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            setHoverEventSwitch(data.readInt());
            reply.writeNoException();
            return true;
        } else if (code == CODE_GET_HOVER_SUPPORT) {
            Slog.d(TAG, "CODE_GET_HOVER_SUPPORT");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int result5 = getHoverEventSupport();
            reply.writeNoException();
            reply.writeInt(result5);
            return true;
        } else if (code == CODE_DISABLE_FINGERPRINT_VIEW_RULE) {
            Slog.d(TAG, "CODE_DISABLE_FINGERPRINT_VIEW_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            disableFingerprintView(data.readBoolean());
            reply.writeNoException();
            return true;
        } else if (code == CODE_ENABLE_FINGERPRINT_VIEW_RULE) {
            Slog.d(TAG, "CODE_ENABLE_FINGERPRINT_VIEW_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            enableFingerprintView(data.readBoolean(), data.readInt());
            reply.writeNoException();
            return true;
        } else if (code == CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION_RULE) {
            Slog.d(TAG, "CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            keepMaskShowAfterAuthentication();
            reply.writeNoException();
            return true;
        } else if (code == 1117) {
            Slog.d(TAG, "CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            removeMaskAndShowButton();
            reply.writeNoException();
            return true;
        } else if (code == CODE_IS_FINGERPRINT_HARDWARE_DETECTED) {
            Slog.d(TAG, "CODE_IS_FINGERPRINT_HARDWARE_DETECTED");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            boolean isHardwareDetected = isHardwareDetectedNoWhitelist(data.readString(), data.readInt());
            reply.writeNoException();
            reply.writeBoolean(isHardwareDetected);
            return true;
        } else if (code == 1118) {
            Slog.d(TAG, "CODE_GET_FINGERPRINT_LIST_ENROLLED");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            List<Fingerprint> fingerprints = getEnrolledFingerprintsNoWhitelist(data.readString(), data.readInt(), data.readInt());
            reply.writeNoException();
            reply.writeTypedList(fingerprints);
            return true;
        } else if (code == CODE_IS_SUPPORT_DUAL_FINGERPRINT) {
            Slog.d(TAG, "CODE_IS_SUPPORT_DUAL_FINGERPRINT");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            reply.writeNoException();
            reply.writeBoolean(isSupportDualFingerprint());
            return true;
        } else if (code == CODE_SEND_UNLOCK_LIGHTBRIGHT) {
            Slog.d(TAG, "CODE_SEND_UNLOCK_LIGHTBRIGHT");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int result6 = sendUnlockAndLightbright(data.readInt());
            reply.writeNoException();
            reply.writeInt(result6);
            return true;
        } else if (code == CODE_GET_HIGHLIGHT_SPOT_RADIUS_RULE) {
            Slog.d(TAG, "CODE_GET_HIGHLIGHT_SPOT_RADIUS_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int radius = getHighLightspotRadius();
            reply.writeNoException();
            reply.writeInt(radius);
            return true;
        } else if (code == CODE_SUSPEND_ENROLL) {
            Slog.d(TAG, "CODE_SUSPEND_ENROLL");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int result7 = suspendEnroll(data.readInt());
            reply.writeNoException();
            reply.writeInt(result7);
            return true;
        } else if (code == CODE_UDFINGERPRINT_SPOTCOLOR) {
            Slog.d(TAG, "CODE_UDFINGERPRINT_SPOTCOLOR");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int result8 = getSpotColor();
            reply.writeNoException();
            reply.writeInt(result8);
            return true;
        } else if (code == CODE_FINGERPRINT_FORBID_GOTOSLEEP) {
            Slog.i(TAG, "CODE_FINGERPRINT_FORBID_GOTOSLEEP");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            boolean isPowerFpForbidGotoSleep = isPowerFpForbidGotoSleep();
            reply.writeNoException();
            reply.writeBoolean(isPowerFpForbidGotoSleep);
            return true;
        } else if (code == CODE_POWER_KEYCODE) {
            Slog.d(TAG, "CODE_POWER_KEYCODE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            int resultKeyCode = operPowerFpPowerKeyCode(data.readInt(), data.readBoolean(), data.readBoolean());
            reply.writeNoException();
            reply.writeInt(resultKeyCode);
            return true;
        } else if (code != 1127) {
            return HwFingerprintService.super.onHwTransact(code, data, reply, flags);
        } else {
            Slog.d(TAG, "CODE_IS_WAIT_AUTHEN");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            boolean isAuthenStatus = isKeyguardAuthenStatus();
            reply.writeNoException();
            reply.writeBoolean(isAuthenStatus);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isSupportDualFingerprint() {
        if (!(this.mIsSupportDualFingerprint || getFingerprintHardwareType() == -1 || (getFingerprintHardwareType() & 1) == 0 || (getFingerprintHardwareType() & 4) == 0)) {
            FingerprintUtils.getInstance().setDualFp(true);
            this.mIsSupportDualFingerprint = true;
            this.mWhitelist.add("com.android.settings");
            this.mWhitelist.add("com.android.systemui");
            this.mWhitelist.add("com.huawei.aod");
            this.mWhitelist.add(HwRecentsTaskUtils.PKG_SYS_MANAGER);
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
    public int sendCommandToHal(int command) {
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            return -1;
        }
        try {
            daemon.sendCmdToHal(command);
            return 0;
        } catch (RemoteException e) {
            Slog.e(TAG, "dualfingerprint sendCmdToHal RemoteException:" + e);
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public boolean canUseUdFingerprint(String opPackageName) {
        if (opPackageName == null || "".equals(opPackageName)) {
            Slog.d(TAG, "calling opPackageName is invalid");
            return false;
        } else if (opPackageName.equals(this.mDefinedAppName)) {
            return true;
        } else {
            Slog.d(TAG, "canUseUdFingerprint opPackageName is " + opPackageName);
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
                    Slog.d(TAG, "metaData is null");
                }
                if (metaData != null) {
                    String type = metaData.getString("fingerprint.system.view");
                    if (type != null && !"".equals(type)) {
                        Slog.d(TAG, "calling opPackageName  metaData value is: " + type);
                        Binder.restoreCallingIdentity(token);
                        return true;
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Slog.e(TAG, "cannot find metaData of package: " + e);
            } catch (Exception e2) {
                Slog.e(TAG, "exception occured: " + e2);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public List<Fingerprint> getEnrolledFingerprintsEx(String opPackageName, int targetDevice, int userId) {
        if (userId != 0 && isClonedProfile(userId)) {
            Log.i(TAG, "Clone profile get Enrolled Fingerprints,change userid to 0");
            userId = 0;
        }
        FingerprintUtils fingerprintUtils = FingerprintUtils.getInstance();
        if (canUseUdFingerprint(opPackageName)) {
            return fingerprintUtils.getFingerprintsForUser(this.mContext, userId, targetDevice);
        }
        if (targetDevice == 1) {
            return Collections.emptyList();
        }
        return fingerprintUtils.getFingerprintsForUser(this.mContext, userId, 0);
    }

    private List<Fingerprint> getEnrolledFingerprintsNoWhitelist(String opPackageName, int targetDevice, int userId) {
        Slog.d(TAG, "dualFingerprint getEnrolledFingerprints opPackageName is " + opPackageName + " userId is " + userId);
        return FingerprintUtils.getInstance().getFingerprintsForUser(this.mContext, userId, targetDevice);
    }

    /* access modifiers changed from: protected */
    public boolean isHardwareDetectedEx(String opPackageName, int targetDevice) {
        boolean z = false;
        if (getFingerprintDaemon() == null) {
            Slog.d(TAG, "Daemon is not available!");
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            if (canUseUdFingerprint(opPackageName)) {
                if (targetDevice == 1) {
                    if (getUdHalDeviceId() != 0) {
                        z = true;
                    }
                    return z;
                } else if (targetDevice == 0) {
                    if (getHalDeviceId() != 0) {
                        z = true;
                    }
                    Binder.restoreCallingIdentity(token);
                    return z;
                } else {
                    if (!(getHalDeviceId() == 0 || getUdHalDeviceId() == 0)) {
                        z = true;
                    }
                    Binder.restoreCallingIdentity(token);
                    return z;
                }
            } else if (targetDevice == 0) {
                if (getHalDeviceId() != 0) {
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
        if (getFingerprintDaemon() == null) {
            Slog.d(TAG, "Daemon is not available!");
            return false;
        }
        Slog.d(TAG, "dualFingerprint isHardwareDetected opPackageName is " + opPackageName + " targetDevice is " + targetDevice);
        long token = Binder.clearCallingIdentity();
        if (targetDevice == 1) {
            try {
                if (getUdHalDeviceId() != 0) {
                    z = true;
                }
                return z;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else if (targetDevice == 0) {
            if (getHalDeviceId() != 0) {
                z = true;
            }
            Binder.restoreCallingIdentity(token);
            return z;
        } else {
            if (!(getHalDeviceId() == 0 || getUdHalDeviceId() == 0)) {
                z = true;
            }
            Binder.restoreCallingIdentity(token);
            return z;
        }
    }

    public int checkNeedCalibrateFingerPrint() {
        int result = -1;
        if (!isFingerprintDReady()) {
            return -1;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        Slog.d(TAG, "pacel  packaged :checkNeedCalibrateFingerPrint");
        try {
            result = daemon.checkNeedCalibrateFingerPrint();
        } catch (RemoteException e) {
            Slog.e(TAG, "checkNeedCalibrateFingerPrint RemoteException:" + e);
        }
        Slog.d(TAG, "fingerprintd calibrate return = " + result);
        return result;
    }

    public void setCalibrateMode(int mode) {
        if (!isFingerprintDReady()) {
            Log.w(TAG, "FingerprintD is not Ready");
            return;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return;
        }
        Slog.d(TAG, "pacel  packaged setCalibrateMode: " + mode);
        try {
            daemon.setCalibrateMode(mode);
        } catch (RemoteException e) {
            Slog.e(TAG, "setCalibrateMode RemoteException:" + e);
        }
    }

    public int getTokenLen() {
        if (!isFingerprintDReady()) {
            return -1;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        int result = -1;
        Slog.d(TAG, "pacel  packaged :getTokenLen");
        try {
            result = daemon.getTokenLen();
        } catch (RemoteException e) {
            Slog.e(TAG, "getTokenLen RemoteException:" + e);
        }
        Slog.d(TAG, "fingerprintd getTokenLen token len = " + result);
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
                if (this.mFingerViewController != null) {
                    if (!"com.android.systemui".equals(pkgNamefromBundle)) {
                        this.mFingerViewController.updateMaskViewAttributes(bundle, callingApp);
                    } else if (!this.mForbideKeyguardCall) {
                        this.mFingerViewController.parseBundle4Keyguard(bundle);
                    }
                }
            }
        }
    }

    public void notifyAuthenticationStarted(String pkgName, IFingerprintServiceReceiver receiver, int flag, int userID, Bundle bundle, IBiometricPromptReceiver dialogReceiver) {
        boolean hasUdFingerprint;
        boolean hasUdFingerprint2;
        String str = pkgName;
        int i = flag;
        int i2 = userID;
        Bundle bundle2 = bundle;
        initPositionAndType();
        if (this.mIsFingerInScreenSupported || isSupportPowerFp()) {
            getAodFace().checkIsPrimaryUser(i2, str);
        }
        if (this.mIsFingerInScreenSupported) {
            if (str == null) {
                Log.d(TAG, "pkgname is null");
                return;
            }
            checkPermissions();
            if (i == 0 || (FLAG_USE_UD_FINGERPRINT & i) != 0) {
                if ("com.android.systemui".equals(str)) {
                    notifyAuthenticationCanceled(pkgName);
                }
                if (bundle2 != null) {
                    this.mMaskViewBundle = bundle2;
                    this.mMaskViewBundle.putString("googleFlag", "googleFlag");
                }
                Log.d(TAG, "show,pkgName =" + str + " userID = " + i2);
                int initType = -1;
                if (str.equals(this.mDefinedAppName)) {
                    initType = this.mAppDefinedMaskType;
                    Log.d(TAG, "initType = " + initType + ",defined by enable interface");
                }
                if (initType == -1) {
                    initType = getAppType(pkgName);
                }
                if (initType == -1) {
                    Iterator<String> it = this.mWhitelist.iterator();
                    while (it.hasNext()) {
                        if (it.next().equals(str)) {
                            Log.d(TAG, "pkgName in whitelist, show default mask for it");
                            initType = 0;
                        }
                    }
                }
                if (str.equals("com.android.cts.verifier")) {
                    initType = 0;
                    Log.d(TAG, "com.android.cts.verifier initType = " + 0);
                }
                FingerprintUtils fingerprintUtils = FingerprintUtils.getInstance();
                if (!fingerprintUtils.isDualFp() && initType == -1) {
                    Log.i(TAG, "single inscreen");
                    initType = 0;
                } else if (fingerprintUtils.isDualFp() && bundle2 != null) {
                    initType = 4;
                }
                if (!fingerprintUtils.isDualFp()) {
                    initType = adjustMaskTypeForWechat(initType, str);
                }
                int initType2 = initType;
                Log.i(TAG, "final initType = " + initType2);
                boolean hasBackFingerprint = false;
                boolean z = false;
                if (fingerprintUtils.isDualFp()) {
                    hasUdFingerprint = fingerprintUtils.getFingerprintsForUser(this.mContext, i2, 1).size() > 0;
                    if (fingerprintUtils.getFingerprintsForUser(this.mContext, i2, 0).size() > 0) {
                        z = true;
                    }
                    hasBackFingerprint = z;
                    if (!hasUdFingerprint) {
                        Log.d(TAG, "userID:" + i2 + "has no UD_fingerprint");
                        if (!(initType2 == 3 || initType2 == 4)) {
                            return;
                        }
                    }
                } else {
                    if (fingerprintUtils.getFingerprintsForUser(this.mContext, i2).size() > 0) {
                        z = true;
                    }
                    hasUdFingerprint = z;
                }
                boolean hasUdFingerprint3 = hasUdFingerprint;
                boolean hasBackFingerprint2 = hasBackFingerprint;
                if (!this.mIsUdFingerprintNeed || checkIfCallerDisableMask()) {
                    hasUdFingerprint2 = hasUdFingerprint3;
                } else {
                    Log.d(TAG, "dialogReceiver = " + dialogReceiver);
                    hasUdFingerprint2 = hasUdFingerprint3;
                    this.mFingerViewController.showMaskOrButton(str, this.mMaskViewBundle, receiver, initType2, hasUdFingerprint3, hasBackFingerprint2, dialogReceiver);
                }
                Log.d(TAG, "begin add windowservice view");
                if ("com.android.systemui".equals(str)) {
                    Log.i(TAG, "keyguard show highlight mask");
                    this.mFingerViewController.showHighlightviewOnKeyguard();
                }
                this.mIsUdAuthenticating = true;
                updateTPState(str, hasUdFingerprint2);
                return;
            }
            Log.d(TAG, "flag = " + i);
        }
    }

    private int adjustMaskTypeForWechat(int initType, String pkgName) {
        if (!PKGNAME_OF_WECHAT.equals(pkgName)) {
            return initType;
        }
        if (ACTIVITYNAME_OF_WECHAT_ENROLL.equals(getForegroundActivityName())) {
            return 0;
        }
        return 3;
    }

    public void notifyAuthenticationCanceled(String pkgName) {
        if (this.mIsFingerInScreenSupported && this.mFingerViewController != null) {
            Log.d(TAG, "notifyAuthenticationCanceled");
            checkPermissions();
            this.mMaskViewBundle = null;
            if ("com.android.systemui".equals(pkgName)) {
                Log.d(TAG, " KEYGUARD notifyAuthenticationCanceled removeHighlightview");
                this.mFingerViewController.destroyHighlightviewOnKeyguard();
            }
            if (!this.mKeepMaskAfterAuthentication) {
                Log.d(TAG, "mKeepMaskAfterAuthentication is false,start remove,pkgName = " + pkgName);
                this.mFingerViewController.removeMaskOrButton();
            }
            this.mIsUdAuthenticating = false;
            this.mKeepMaskAfterAuthentication = false;
            this.mAppDefinedMaskType = -1;
            this.mDefinedAppName = "";
        }
    }

    private void removeFingerprintView() {
        if (this.mIsFingerInScreenSupported) {
            String callingApp = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
            Log.d(TAG, "removeFingerprintView,callingApp = " + callingApp);
            checkPermissions();
            this.mKeepMaskAfterAuthentication = false;
            this.mMaskViewBundle = null;
            this.mFingerViewController.removeMaskOrButton();
        }
    }

    public void notifyFingerDown(int type) {
        Log.d(TAG, "notifyFingerDown mIsFingerInScreenSupported:" + this.mIsFingerInScreenSupported);
        if (this.mIsFingerInScreenSupported) {
            if (this.mWindowManagerInternal == null) {
                this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            }
            if (this.mWindowManagerInternal == null || this.mWindowManagerInternal.isCoverOpen()) {
                checkPermissions();
                this.mIsUdFingerprintChecking = true;
                if (this.mIsHighLightNeed) {
                    if (type == 1) {
                        Log.d(TAG, "FINGER_DOWN_TYPE_AUTHENTICATING notifyFingerDown ");
                        long token = Binder.clearCallingIdentity();
                        try {
                            if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "show_touches", -2) == 1) {
                                Log.d(TAG, "turn off the show_touch switch when authenticating");
                                this.mNeedResumeTouchSwitch = true;
                                Settings.System.putIntForUser(this.mContext.getContentResolver(), "show_touches", 0, -2);
                            }
                        } catch (SecurityException e) {
                            Log.e(TAG, "catch SecurityException");
                        } catch (Settings.SettingNotFoundException e2) {
                            Log.d(TAG, "settings show_touches not found");
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(token);
                            throw th;
                        }
                        Binder.restoreCallingIdentity(token);
                        this.mFingerViewController.showHighlightview(1);
                    } else if (type == 0) {
                        Log.d(TAG, "FINGER_DOWN_TYPE_ENROLLING notifyFingerDown ");
                        this.mFingerViewController.showHighlightCircle();
                    } else if (type == 3) {
                        Log.d(TAG, "FINGER_DOWN_TYPE_AUTHENTICATING_SYSTEMUI notifyFingerDown ");
                        this.mFingerViewController.showHighlightCircleOnKeyguard();
                    }
                }
                if (this.mIsUdFingerprintNeed && type == 1) {
                    this.mFingerViewController.updateFingerprintView(3, this.mKeepMaskAfterAuthentication);
                }
                return;
            }
            Log.d(TAG, "mWindowManagerInternal.isCover added");
        }
    }

    public void notifyEnrollingFingerUp() {
        if (this.mIsFingerInScreenSupported && this.mIsUdEnrolling && this.mIsHighLightNeed) {
            Log.d(TAG, "notifyEnrollingFingerUp removeHighlightCircle");
            this.mFingerViewController.removeHighlightCircle();
        }
    }

    public void notifyCaptureFinished(int type) {
        Log.d(TAG, "notifyCaptureFinished");
        if (this.mIsHighLightNeed && this.mFingerViewController != null) {
            this.mFingerViewController.removeHighlightCircle();
        }
    }

    public void notifyFingerCalibrarion(int value) {
        initPositionAndType();
        if (this.mIsFingerInScreenSupported) {
            checkPermissions();
            int[] position = getFingerprintHardwarePosition();
            FingerprintCalibrarionView caliview = FingerprintCalibrarionView.getInstance(this.mContext);
            caliview.setCenterPoints((position[0] + position[2]) / 2, (position[1] + position[3]) / 2);
            caliview.showHighlightviewCali(value);
        }
    }

    public void notifyEnrollmentStarted(int flags) {
        initPositionAndType();
        if (this.mIsHighLightNeed) {
            FingerprintUtils fingerprintUtils = FingerprintUtils.getInstance();
            if (!fingerprintUtils.isDualFp() || flags == 4096) {
                this.mIsUdEnrolling = true;
                checkPermissions();
                if (!fingerprintUtils.isDualFp() || flags == 4096) {
                    if (this.mDisplayEngineManager != null) {
                        this.mDisplayEngineManager.setScene(31, 16);
                    } else {
                        Log.w(TAG, "mDisplayEngineManager is null!");
                    }
                }
                if (this.mFingerViewController != null) {
                    this.mFingerViewController.showHighlightview(0);
                }
                Log.d(TAG, "start enroll begin add Highlight view");
                sendCommandToHal(1);
                return;
            }
            Log.d(TAG, "not enrolling UD fingerprint");
        }
    }

    public void notifyEnrollmentCanceled() {
        boolean hasUdFingerprint;
        if (this.mIsFingerInScreenSupported) {
            FingerprintUtils fingerprintUtils = FingerprintUtils.getInstance();
            int currentUser = ActivityManager.getCurrentUser();
            boolean z = true;
            if (fingerprintUtils.isDualFp()) {
                if (fingerprintUtils.getFingerprintsForUser(this.mContext, currentUser, 1).size() <= 0) {
                    z = false;
                }
                hasUdFingerprint = z;
            } else {
                hasUdFingerprint = fingerprintUtils.getFingerprintsForUser(this.mContext, currentUser).size() > 0;
            }
            if (!hasUdFingerprint) {
                sendCommandToHal(0);
            }
        }
        Log.d(TAG, "notifyEnrollmentEnd mIsUdEnrolling: " + this.mIsUdEnrolling);
        if (this.mIsHighLightNeed) {
            checkPermissions();
            if (this.mIsUdEnrolling && this.mFingerViewController != null) {
                this.mFingerViewController.removeHighlightview(0);
            }
            if (this.mIsUdEnrolling) {
                if (this.mDisplayEngineManager != null) {
                    this.mDisplayEngineManager.setScene(31, 17);
                } else {
                    Log.w(TAG, "mDisplayEngineManager is null!");
                }
            }
            this.mIsUdEnrolling = false;
        }
    }

    public void notifyAuthenticationFinished(String opName, int result, int failTimes) {
        int i;
        if (this.mIsFingerInScreenSupported) {
            checkPermissions();
            Log.d(TAG, "notifyAuthenticationFinished,mIsUdFingerprintChecking = " + this.mIsUdFingerprintChecking + ",result =" + result + "failTimes = " + failTimes);
            if (this.mIsHighLightNeed && this.mIsUdFingerprintChecking) {
                Log.d(TAG, "UdFingerprint Checking,AuthenticationFinished begin remove Highlight view");
                resumeTouchSwitch();
                if (!"com.android.systemui".equals(opName)) {
                    FingerViewController fingerViewController = this.mFingerViewController;
                    if (result == 0) {
                        i = 2;
                    } else {
                        i = -1;
                    }
                    fingerViewController.removeHighlightview(i);
                } else {
                    this.mFingerViewController.removeHighlightviewOnKeyguard();
                    Log.i(TAG, "finger check finish removeHighlightCircle");
                }
            }
            if (this.mIsUdFingerprintNeed) {
                this.mFingerViewController.updateFingerprintView(result, failTimes);
                if (result == 0 && "com.android.systemui".equals(opName)) {
                    this.mFingerViewController.removeMaskOrButton();
                }
            }
            this.mIsUdFingerprintChecking = false;
        }
    }

    /* access modifiers changed from: private */
    public void resumeTouchSwitch() {
        if (this.mIsHighLightNeed && this.mIsUdFingerprintChecking && this.mNeedResumeTouchSwitch) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mNeedResumeTouchSwitch = false;
                Log.d(TAG, "turn on the show_touch switch after authenticating");
                Settings.System.putIntForUser(this.mContext.getContentResolver(), "show_touches", 1, -2);
            } catch (SecurityException e) {
                Log.e(TAG, "catch SecurityException");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    private void disableFingerprintView(boolean hasAnimation) {
        if (this.mIsFingerInScreenSupported) {
            checkPermissions();
            String callingApp = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
            Log.d(TAG, "callingApp = " + callingApp);
            if (this.mIsUdAuthenticating && callingApp.equals(getForegroundActivity())) {
                this.mFingerViewController.removeMaskOrButton();
            }
            this.mPackageDisableMask = callingApp;
        }
    }

    private void enableFingerprintView(boolean hasAnimation, int initStatus) {
        if (this.mIsFingerInScreenSupported) {
            checkPermissions();
            String callingApp = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
            Log.d(TAG, "enableFingerprintView,callingApp = " + callingApp);
            if (this.mPackageDisableMask != null && this.mPackageDisableMask.equals(callingApp)) {
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
                Log.d(TAG, "pkgName is " + pkgName + "metaData is " + metaData.getString("fingerprint.system.view"));
                if (metaData.getString("fingerprint.system.view") != null) {
                    if (metaData.getString("fingerprint.system.view").equals(BuildConfig.FLAVOR_product)) {
                        type = 0;
                    } else if (metaData.getString("fingerprint.system.view").equals("button")) {
                        type = 1;
                    } else if (metaData.getString("fingerprint.system.view").equals("image")) {
                        type = 3;
                    } else {
                        type = 2;
                    }
                }
                Log.d(TAG, "metaData type is " + type);
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
            this.mKeepMaskAfterAuthentication = true;
        }
    }

    private void removeMaskAndShowButton() {
        if (this.mIsFingerInScreenSupported) {
            checkPermissions();
            this.mFingerViewController.removeMaskAndShowButton();
        }
    }

    private boolean isForegroundActivity(String packageName) {
        try {
            List<ActivityManager.RunningAppProcessInfo> procs = ActivityManager.getService().getRunningAppProcesses();
            if (procs == null) {
                Log.i(TAG, "isForegroundActivity RunningAppProcessInfo is null");
                return false;
            }
            int N = procs.size();
            for (int i = 0; i < N; i++) {
                ActivityManager.RunningAppProcessInfo proc = procs.get(i);
                if (proc.processName.equals(packageName) && proc.importance == 100) {
                    return true;
                }
            }
            return false;
        } catch (RemoteException e) {
            Slog.w(TAG, "am.getRunningAppProcesses() failed");
        }
    }

    private String getForegroundActivity() {
        try {
            List<ActivityManager.RunningAppProcessInfo> procs = ActivityManager.getService().getRunningAppProcesses();
            if (procs == null) {
                Log.i(TAG, "isForegroundActivity RunningAppProcessInfo is null");
                return "";
            }
            int N = procs.size();
            for (int i = 0; i < N; i++) {
                ActivityManager.RunningAppProcessInfo proc = procs.get(i);
                if (proc.importance == 100) {
                    Log.d(TAG, "foreground processName = " + proc.processName);
                    return proc.processName;
                }
            }
            return "";
        } catch (RemoteException e) {
            Slog.w(TAG, "am.getRunningAppProcesses() failed");
        }
    }

    /* JADX INFO: finally extract failed */
    private String getForegroundActivityName() {
        long token = Binder.clearCallingIdentity();
        try {
            ActivityInfo info = ActivityManagerEx.getLastResumedActivity();
            Binder.restoreCallingIdentity(token);
            String name = "";
            if (info != null) {
                name = info.name;
            }
            Log.d(TAG, "foreground wechat activity name is" + name);
            return name;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private boolean checkIfCallerDisableMask() {
        if (this.mPackageDisableMask == null) {
            return false;
        }
        if (isForegroundActivity(this.mPackageDisableMask)) {
            return true;
        }
        this.mPackageDisableMask = null;
        return false;
    }

    private void checkPermissions() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.USE_FINGERPRINT", null);
    }

    private void resetFingerprintPosition() {
        int[] position = getFingerprintHardwarePosition();
        this.mFingerViewController.setFingerprintPosition(position);
        Log.d(TAG, "defaultScreenSize be init second, position = " + Arrays.toString(position));
    }

    private void initPositionAndType() {
        if (this.mFingerprintType == 0) {
            Log.d(TAG, "FingerprintType do not support inscreen fingerprint");
        } else if (this.mFingerprintType <= 0 || this.mFingerViewController == null) {
            this.mFingerprintType = getFingerprintHardwareTypeInternal();
            Log.d(TAG, "FingerprintType type = " + this.mFingerprintType);
            boolean z = false;
            this.mIsFingerInScreenSupported = this.mFingerprintType > 0;
            if (this.mFingerprintType == 1) {
                z = true;
            }
            this.mIsHighLightNeed = z;
            if (this.mIsFingerInScreenSupported) {
                this.mFingerViewController = FingerViewController.getInstance(this.mContext);
                this.mFingerViewController.registCallback(new FingerViewChangeCallback());
                int[] position = getFingerprintHardwarePosition();
                Log.d(TAG, " hardware type = " + this.mFingerprintType);
                this.mFingerViewController.setFingerprintPosition(position);
                this.mFingerViewController.setHighLightBrightnessLevel(getHighLightBrightnessLevel());
                int spotColor = getSpotColor();
                if (spotColor == 0) {
                    spotColor = DEFAULT_COLOR;
                }
                this.mFingerViewController.setHighLightSpotColor(spotColor);
                this.mFingerViewController.setHighLightSpotRadius(getHighLightspotRadius());
                initObserver();
                getAodFace();
            }
        } else {
            if (this.mInitDisplayHeight == -1) {
                resetFingerprintPosition();
            }
            Log.d(TAG, "FingerprintType has been inited, FingerprintType = " + this.mFingerprintType);
        }
    }

    /* access modifiers changed from: protected */
    public void triggerFaceRecognization() {
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        if (powerManager == null || !powerManager.isInteractive()) {
            HwPhoneWindowManager policy = (HwPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
            if (policy != null) {
                policy.doFaceRecognize(true, FACE_DETECT_REASON);
            }
            return;
        }
        Log.i(TAG, "screen on, do not trigger face detection");
    }

    /* access modifiers changed from: private */
    public void suspendAuthentication(int status) {
        if (!isFingerprintDReady()) {
            Log.w(TAG, "FingerprintD is not Ready");
        } else if (!this.mIsFingerInScreenSupported) {
            Log.w(TAG, "do not have UD device suspend invalid");
        } else {
            IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
            if (daemon == null) {
                Slog.e(TAG, "Fingerprintd is not available!");
                return;
            }
            Slog.d(TAG, "pacel  packaged suspendAuthentication: " + status);
            if (status == 1) {
                try {
                    daemon.sendCmdToHal(53);
                } catch (RemoteException e) {
                    Slog.e(TAG, "suspendAuthentication RemoteException:" + e);
                }
            } else {
                daemon.sendCmdToHal(MSG_RESUME_AUTHENTICATION);
            }
        }
    }

    private int suspendEnroll(int status) {
        int result = -1;
        if (!isFingerprintDReady()) {
            Log.w(TAG, "FingerprintD is not Ready");
            return -1;
        } else if (!this.mIsFingerInScreenSupported) {
            Log.w(TAG, "do not have UD device suspend invalid");
            return -1;
        } else {
            IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
            if (daemon == null) {
                Slog.e(TAG, "Fingerprintd is not available!");
                return -1;
            }
            Slog.d(TAG, "pacel  packaged suspendEnroll: " + status);
            if (status == 1) {
                try {
                    result = daemon.sendCmdToHal(64);
                } catch (RemoteException e) {
                    Slog.e(TAG, "suspendEnroll RemoteException:" + e);
                }
            } else {
                result = daemon.sendCmdToHal(MSG_RESUME_ENROLLMENT);
            }
            return result;
        }
    }

    private int getFingerprintHardwareType() {
        if (this.typeDetails == -1) {
            if (!isFingerprintDReady()) {
                return -1;
            }
            IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
            if (daemon == null) {
                Slog.e(TAG, "Fingerprintd is not available!");
                return -1;
            }
            Slog.d(TAG, "pacel  packaged :HardwareType");
            try {
                this.typeDetails = daemon.sendCmdToHal(55);
            } catch (RemoteException e) {
                Slog.e(TAG, "HardwareType RemoteException:" + e);
            }
            Slog.d(TAG, "fingerprintd HardwareType = " + this.typeDetails);
        }
        return this.typeDetails;
    }

    private int getFingerprintHardwareTypeInternal() {
        int type = -1;
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
        if ((getFingerprintHardwareType() & 4) != 0) {
            int physicalType = (getFingerprintHardwareType() >> (8 + ((offset + 1) * 4))) & 15;
            Log.d(TAG, "LOCATION_UNDER_DISPLAY physicalType :" + physicalType);
            if (physicalType == 2) {
                type = 1;
            } else if (physicalType == 3) {
                type = 2;
            }
        } else {
            type = 0;
        }
        return type;
    }

    private int[] getFingerprintHardwarePosition() {
        int[] result = {-1, -1};
        int[] pxPosition = {-1, -1, -1, -1};
        if (!isFingerprintDReady()) {
            return pxPosition;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return pxPosition;
        }
        try {
            result[0] = daemon.sendCmdToHal(MSG_GET_SENSOR_POSITION_TOP_LEFT);
            result[1] = daemon.sendCmdToHal(60);
        } catch (RemoteException e) {
            Slog.e(TAG, "HardwarePosition RemoteException:" + e);
        }
        Slog.d(TAG, "fingerprintd HardwarePosition = " + result[0] + " " + result[1]);
        if (result[0] == -1) {
            String[] positionG = SystemProperties.get("persist.sys.fingerprint.hardwarePosition", "-1,-1,-1,-1").split(",");
            pxPosition[0] = Integer.parseInt(positionG[0]);
            pxPosition[1] = Integer.parseInt(positionG[1]);
            pxPosition[2] = Integer.parseInt(positionG[2]);
            pxPosition[3] = Integer.parseInt(positionG[3]);
            Log.d(TAG, "getHardwarePosition from SystemProperties: " + pxPosition[0]);
        } else {
            int[] parsedPosition = {-1, -1, -1, -1};
            parsedPosition[0] = result[0] >> 16;
            parsedPosition[1] = result[0] & FLAG_FINGERPRINT_POSITION_MASK;
            parsedPosition[2] = result[1] >> 16;
            parsedPosition[3] = result[1] & FLAG_FINGERPRINT_POSITION_MASK;
            pxPosition = physicalConvert2Px(parsedPosition);
        }
        return pxPosition;
    }

    private int[] physicalConvert2Px(int[] input) {
        int[] covertPosition = {-1, -1, -1, -1};
        int i = 0;
        if (this.mInitDisplayHeight == -1 && this.mContext != null) {
            String defaultScreenSize = SystemProperties.get("ro.config.default_screensize");
            if (defaultScreenSize == null || defaultScreenSize.equals("")) {
                this.mInitDisplayHeight = Settings.Global.getInt(this.mContext.getContentResolver(), APS_INIT_HEIGHT, -1);
                this.mInitDisplayWidth = Settings.Global.getInt(this.mContext.getContentResolver(), APS_INIT_WIDTH, -1);
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
        covertPosition[0] = (input[0] * this.mInitDisplayWidth) / 1000;
        covertPosition[1] = (input[1] * this.mInitDisplayHeight) / 1000;
        covertPosition[2] = (input[2] * this.mInitDisplayWidth) / 1000;
        covertPosition[3] = (input[3] * this.mInitDisplayHeight) / 1000;
        Log.d(TAG, "Width: " + this.mInitDisplayWidth + " height: " + this.mInitDisplayHeight);
        while (true) {
            int i2 = i;
            if (i2 >= 4) {
                return covertPosition;
            }
            Log.d(TAG, "use hal after covert: " + covertPosition[i2]);
            i = i2 + 1;
        }
    }

    /* access modifiers changed from: private */
    public void notifyCaptureOpticalImage() {
        if (isFingerprintDReady()) {
            IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
            if (daemon == null) {
                Slog.e(TAG, "Fingerprintd is not available!");
                return;
            }
            Slog.d(TAG, "pacel  packaged :notifyCaptureOpticalImage");
            try {
                daemon.sendCmdToHal(52);
            } catch (RemoteException e) {
                Slog.e(TAG, "notifyCaptureOpticalImage RemoteException:" + e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyBluespotDismiss() {
        if (isFingerprintDReady()) {
            IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
            if (daemon == null) {
                Slog.e(TAG, "Fingerprintd is not available!");
                return;
            }
            Slog.d(TAG, "pacel  packaged :notifyBluespotDismiss");
            try {
                daemon.sendCmdToHal(62);
            } catch (RemoteException e) {
                Slog.e(TAG, "notifyBluespotDismiss RemoteException:" + e);
            }
        }
    }

    private void setHoverEventSwitch(int enabled) {
        if (!isFingerprintDReady()) {
            Log.w(TAG, "FingerprintD is not Ready");
            return;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return;
        }
        Slog.d(TAG, "pacel  packaged setHoverEventSwitch: " + enabled);
        if (enabled == 1) {
            try {
                daemon.sendCmdToHal(MSG_SET_HOVER_ENABLE);
            } catch (RemoteException e) {
                Slog.e(TAG, "setHoverEventSwitch RemoteException:" + e);
            }
        } else {
            daemon.sendCmdToHal(MSG_SET_HOVER_DISABLE);
        }
    }

    private int getHoverEventSupport() {
        if (!isFingerprintDReady()) {
            return -1;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        int result = -1;
        Slog.d(TAG, "pacel  packaged :getHoverEventSupport");
        try {
            result = daemon.sendCmdToHal(MSG_CHECK_HOVER_SUPPORT);
        } catch (RemoteException e) {
            Slog.e(TAG, "getHoverEventSupport RemoteException:" + e);
        }
        Slog.d(TAG, "fingerprintd getHoverEventSupport = " + result);
        return result;
    }

    private int getHighLightBrightnessLevel() {
        int result = DEFAULT_CAPTURE_BRIGHTNESS;
        if (!isFingerprintDReady()) {
            Slog.e(TAG, "Fingerprintd is not ready!");
            return DEFAULT_CAPTURE_BRIGHTNESS;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return DEFAULT_CAPTURE_BRIGHTNESS;
        }
        Slog.d(TAG, "pacel  packaged :getHighLightBrightnessLevel");
        try {
            result = daemon.sendCmdToHal(MSG_GET_BRIGHTNEWSS_FROM_HAL);
        } catch (RemoteException e) {
            Slog.e(TAG, "getHighLightBrightnessLevel RemoteException");
        }
        Slog.d(TAG, "fingerprintd getHighLightBrightnessLevel = " + result);
        return result;
    }

    private int getSpotColor() {
        int color = 0;
        if (!isFingerprintDReady()) {
            Slog.e(TAG, "Fingerprintd is not ready!");
            return 0;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return 0;
        }
        Slog.d(TAG, "pacel  packaged :getHighLightColor");
        try {
            color = daemon.sendCmdToHal(MSG_GET_HIGHLIGHT_SPOT_COLOR_FROM_HAL);
        } catch (RemoteException e) {
            Slog.e(TAG, "getHighLightColor RemoteException");
        }
        Slog.d(TAG, "fingerprintd getHighLightColor = " + color);
        return color;
    }

    private int checkForegroundNeedLiveness() {
        Slog.w(TAG, "checkForegroundNeedLiveness:start");
        try {
            List<ActivityManager.RunningAppProcessInfo> procs = ActivityManagerNative.getDefault().getRunningAppProcesses();
            if (procs == null) {
                return 0;
            }
            int N = procs.size();
            for (int i = 0; i < N; i++) {
                ActivityManager.RunningAppProcessInfo proc = procs.get(i);
                if (proc.importance == 100) {
                    if ("com.alipay.security.mobile.authentication.huawei".equals(proc.processName)) {
                        Slog.w(TAG, "ForegroundActivity is " + proc.processName + "need liveness auth");
                        return 1;
                    } else if ("com.huawei.wallet".equals(proc.processName)) {
                        Slog.w(TAG, "ForegroundActivity is " + proc.processName + "need liveness auth");
                        return 1;
                    } else if ("com.huawei.android.hwpay".equals(proc.processName)) {
                        Slog.w(TAG, "ForegroundActivity is " + proc.processName + "need liveness auth");
                        return 1;
                    }
                }
            }
            return 0;
        } catch (RemoteException e) {
            Slog.w(TAG, "am.getRunningAppProcesses() failed in checkForegroundNeedLiveness");
        }
    }

    private int checkNeedLivenessList(String opPackageName) {
        Slog.w(TAG, "checkNeedLivenessList:start");
        if (opPackageName == null || opPackageName.equals("com.android.keyguard")) {
            return 0;
        }
        if (opPackageName.equals("com.huawei.securitymgr")) {
            return checkForegroundNeedLiveness();
        }
        if (!opPackageName.equals("com.eg.android.AlipayGphone") && !opPackageName.equals("fido") && !opPackageName.equals("com.alipay.security.mobile.authentication.huawei") && !opPackageName.equals("com.huawei.wallet") && !opPackageName.equals("com.huawei.android.hwpay") && !opPackageName.equals(PKGNAME_OF_WECHAT)) {
            return 0;
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public void setLivenessSwitch(String opPackageName) {
        Slog.w(TAG, "setLivenessSwitch:start");
        if ((!mLivenessNeedBetaQualification || isBetaUser()) && isFingerprintDReady()) {
            int NEED_LIVENESS_AUTHENTICATION = checkNeedLivenessList(opPackageName);
            Slog.w(TAG, "NEED_LIVENESS_AUTHENTICATION = " + NEED_LIVENESS_AUTHENTICATION);
            IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
            if (daemon == null) {
                Slog.e(TAG, "Fingerprintd is not available!");
                return;
            }
            try {
                daemon.setLivenessSwitch(NEED_LIVENESS_AUTHENTICATION);
            } catch (RemoteException e) {
                Slog.e(TAG, "setLivenessSwitch RemoteException:" + e);
            }
            Slog.w(TAG, "framework setLivenessSwitch is ok ---end");
        }
    }

    private boolean checkPackageName(String opPackageName) {
        if (opPackageName == null || !opPackageName.equals("com.android.systemui")) {
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
        HwFingerprintSets _result;
        Slog.i(TAG, "remoteGetOldData:start");
        if (!isFingerprintDReady()) {
            return null;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return null;
        }
        ArrayList<Integer> fingerprintInfo = new ArrayList<>();
        try {
            fingerprintInfo = daemon.getFpOldData();
        } catch (RemoteException e) {
            Slog.e(TAG, "remoteGetOldData RemoteException:" + e);
        }
        Parcel _reply = Parcel.obtain();
        int fingerprintInfoLen = fingerprintInfo.size();
        for (int i = 0; i < fingerprintInfoLen; i++) {
            int intValue = fingerprintInfo.get(i).intValue();
            if (intValue != -1) {
                _reply.writeInt(intValue);
            }
        }
        _reply.setDataPosition(0);
        if (_reply.readInt() != 0) {
            _result = HwFingerprintSets.CREATOR.createFromParcel(_reply);
        } else {
            _result = null;
        }
        _reply.recycle();
        return _result;
    }

    private static boolean checkItemExist(int oldFpId, ArrayList<Fingerprint> fingerprints) {
        int size = fingerprints.size();
        for (int i = 0; i < size; i++) {
            if (fingerprints.get(i).getFingerId() == oldFpId) {
                fingerprints.remove(i);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isFingerprintDReady() {
        if (getFingerprintDaemon() != null) {
            return true;
        }
        Slog.w(TAG, "isFingerprintDReady: no fingeprintd!");
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleAcquired(long deviceId, int acquiredInfo, int vendorCode) {
        HwFingerprintService.super.handleAcquired(deviceId, acquiredInfo, vendorCode);
        if ((acquiredInfo == 6 ? vendorCode + 1000 : acquiredInfo) == 2002) {
            this.mLastFingerDownTime = System.currentTimeMillis();
            this.mIsBlackAuthenticateEvent = !this.mPowerManager.isInteractive();
            Slog.i(TAG, "powerfp mLastFingerDownTime=" + this.mLastFingerDownTime + " mIsBlackAuthenticateEvent=" + this.mIsBlackAuthenticateEvent);
        }
    }

    /* access modifiers changed from: protected */
    public void handleAuthenticated(long deviceId, int fingerId, int groupId, ArrayList<Byte> token) {
        this.mLastAuthenticatedStartTime = System.currentTimeMillis();
        if (fingerId != 0) {
            clearKeyguardAuthenScreenOn();
            AwareFakeActivityRecg.self().setFingerprintWakeup(true);
        }
        HwFingerprintService.super.handleAuthenticated(deviceId, fingerId, groupId, token);
        sendOnAuthenticatedFinishToHal();
        this.mLastAuthenticatedEndTime = System.currentTimeMillis();
        Slog.i(TAG, "powerfp handleAuthenticated finish, duration=" + (this.mLastAuthenticatedEndTime - this.mLastAuthenticatedStartTime));
    }

    /* access modifiers changed from: protected */
    public void stopPickupTrunOff() {
        if (PickUpWakeScreenManager.isPickupSensorSupport(this.mContext) && PickUpWakeScreenManager.getInstance() != null) {
            PickUpWakeScreenManager.getInstance().stopTrunOffScrren();
        }
    }

    public void setPowerState(int powerState) {
        HwAodManager.getInstance().setPowerState(powerState);
    }

    private int sendUnlockAndLightbright(int unlockType) {
        Slog.d(TAG, "sendUnlockAndLightbright unlockType:" + unlockType);
        int result = -1;
        if (2 == unlockType) {
            result = sendCommandToHal(201);
        } else if (1 == unlockType) {
            result = sendCommandToHal(200);
        } else if (3 == unlockType) {
            result = sendCommandToHal(202);
        }
        Slog.d(TAG, "sendCommandToHal result:" + result);
        return result;
    }

    private int getHighLightspotRadius() {
        Slog.d(TAG, "getHighLightspotRadius start");
        int radius = 95;
        if (!isFingerprintDReady()) {
            Slog.e(TAG, "Fingerprintd is not ready!");
            return 95;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return 95;
        }
        try {
            radius = daemon.sendCmdToHal(MSG_GET_RADIUS_FROM_HAL);
        } catch (RemoteException e) {
            Slog.e(TAG, "getHighLightspotRadius RemoteException");
        }
        Slog.d(TAG, "fingerprintd getHighLightspotRadius = " + radius);
        return radius;
    }

    private boolean isSupportBlackAuthentication() {
        String settingValue = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "screen_lock_fingerprint_unlock_protection_db", 0);
        if (settingValue != null) {
            return "0".equals(settingValue);
        }
        return this.mSupportBlackAuthentication;
    }

    public boolean isSupportPowerFp() {
        return this.mSupportPowerFp;
    }

    public boolean isPowerFpForbidGotoSleep() {
        if (!isSupportPowerFp()) {
            return false;
        }
        if (isThreesidesAuthenticating()) {
            Slog.i(TAG, "powerfp forbid gotosleep isThreesidesAuthenticating");
            return true;
        }
        if (isSupportBlackAuthentication() && this.mIsBlackAuthenticateEvent) {
            this.mIsBlackAuthenticateEvent = false;
            long duration = System.currentTimeMillis() - this.mLastFingerDownTime;
            if (duration > 700) {
                Slog.i(TAG, "powerfp forbid gotosleep do nothing, lastFingerDown duration=" + duration);
                return false;
            } else if (this.mLastAuthenticatedEndTime > this.mLastAuthenticatedStartTime) {
                long duration2 = System.currentTimeMillis() - this.mLastAuthenticatedEndTime;
                Slog.i(TAG, "powerfp forbid gotosleep 1 duration=" + duration2);
                if (duration2 < 300) {
                    return true;
                }
            } else {
                long duration3 = System.currentTimeMillis() - this.mLastAuthenticatedStartTime;
                Slog.i(TAG, "powerfp forbid gotosleep 2 duration=" + duration3);
                if (duration3 < 500) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isThreesidesAuthenticating() {
        return this.mCurrentClient != null && (((this.mCurrentClient instanceof AuthenticationClient) && !"com.android.systemui".equals(this.mCurrentClient.getOwnerString())) || (this.mCurrentClient instanceof EnrollClient));
    }

    private boolean isKeyguardAuthenticating() {
        return this.mCurrentClient != null && (this.mCurrentClient instanceof AuthenticationClient) && "com.android.systemui".equals(this.mCurrentClient.getOwnerString());
    }

    private int operPowerFpPowerKeyCode(int keycode, boolean isPowerDown, boolean interactive) {
        if (!isSupportPowerFp() || isSupportBlackAuthentication() || keycode != 26) {
            return -1;
        }
        if (isPowerDown) {
            this.mLastPowerKeyDownTime = System.currentTimeMillis();
        } else {
            this.mLastPowerKeyUpTime = System.currentTimeMillis();
        }
        this.mIsInteractive = interactive;
        if (isPowerDown && !interactive) {
            immediatelyOnAuthenticatedRunnable();
        }
        Slog.i(TAG, "operPowerFpPowerKeyCode: mLastPowerKeyDownTime=" + this.mLastPowerKeyDownTime + " mLastPowerKeyUpTime=" + this.mLastPowerKeyUpTime + " isPowerDown=" + isPowerDown + " mIsInteractive=" + this.mIsInteractive);
        return 0;
    }

    private void immediatelyOnAuthenticatedRunnable() {
        if (isKeyguardAuthenticating() && getPowerFpParam().isWaitPowerEvent()) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    Slog.w(HwFingerprintService.TAG, "onAuthenticated 2");
                    HwFingerprintService.this.handleAuthenticated(HwFingerprintService.this.getPowerFpParam().getDeviceId(), HwFingerprintService.this.getPowerFpParam().getFingerId(), HwFingerprintService.this.getPowerFpParam().getGroupId(), HwFingerprintService.this.getPowerFpParam().getToken());
                }
            });
        }
    }

    public boolean isPowerFpAbandonAuthenticated() {
        if (!isSupportPowerFp() || isSupportBlackAuthentication() || !isKeyguardAuthenticating()) {
            return false;
        }
        if (this.mLastPowerKeyUpTime >= this.mLastPowerKeyDownTime) {
            boolean isInteractive = this.mPowerManager.isInteractive();
            if (!isInteractive && isSupportBlackAuthentication()) {
                return false;
            }
            if (!isInteractive) {
                Slog.i(TAG, "dev is sleep and no support black authenticated");
            }
            return !isInteractive;
        }
        if (this.mIsInteractive) {
            Slog.i(TAG, "power down finish, wait up.");
        }
        return this.mIsInteractive;
    }

    public long getPowerDelayFpTime() {
        if (!isSupportPowerFp() || isSupportBlackAuthentication() || !isKeyguardAuthenticating()) {
            return 0;
        }
        long nowTime = System.currentTimeMillis();
        if (this.mLastPowerKeyUpTime < this.mLastPowerKeyDownTime || nowTime <= this.mLastPowerKeyDownTime) {
            Slog.i(TAG, "powerfp mLastPowerKeyUpTime:" + this.mLastPowerKeyUpTime + ",mLastPowerKeyDownTime:" + this.mLastPowerKeyDownTime + ",nowTime:" + nowTime);
            return 0;
        }
        return this.mPowerManager.isInteractive() ? (long) this.mPowerDelayFpTime : -1;
    }

    private int sendOnAuthenticatedFinishToHal() {
        int result = -1;
        if (!isSupportPowerFp() || !isFingerprintDReady()) {
            Slog.e(TAG, "Fingerprintd is not ready!");
            return -1;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        Slog.d(TAG, "pacel  packaged :sendOnAuthenticatedFinishToHal");
        try {
            result = daemon.sendCmdToHal(199);
        } catch (RemoteException e) {
            Slog.e(TAG, "sendOnAuthenticatedFinishToHal RemoteException");
        }
        Slog.d(TAG, "fingerprintd sendOnAuthenticatedFinishToHal = " + result);
        return result;
    }

    public void saveWaitRunonAuthenticated(long deviceId, int fingerId, int groupId, ArrayList<Byte> token) {
        getPowerFpParam().setAuthenticatedParam(deviceId, fingerId, groupId, token);
    }

    public void setNoWaitPowerEvent() {
        getPowerFpParam().setNoWaitPowerEvent();
    }

    private boolean isKeyguardAuthenStatus() {
        if (!isSupportPowerFp() || !isKeyguardAuthenticating()) {
            return false;
        }
        boolean result = this.mIsKeyguardAuthenStatus.getAndSet(false);
        Slog.i(TAG, "powerfp isKeyguardAuthenStatus wakeup delayed=" + result);
        if (result) {
            this.mHandler.removeCallbacks(this.mPowerFingerWakeUpRunable);
            this.mHandler.postDelayed(this.mPowerFingerWakeUpRunable, (long) this.mFpDelayPowerTime);
        }
        return result;
    }

    public void setKeyguardAuthenScreenOn() {
        if (isSupportPowerFp() && isKeyguardAuthenticating()) {
            this.mIsKeyguardAuthenStatus.set(true);
        }
    }

    private void clearKeyguardAuthenScreenOn() {
        if (isSupportPowerFp() && isKeyguardAuthenticating()) {
            this.mHandler.removeCallbacks(this.mPowerFingerWakeUpRunable);
        }
    }

    /* access modifiers changed from: private */
    public AuthenticatedParam getPowerFpParam() {
        if (this.mAuthenticatedParam == null) {
            this.mAuthenticatedParam = new AuthenticatedParam();
        }
        return this.mAuthenticatedParam;
    }

    private AODFaceUpdateMonitor getAodFace() {
        if (this.mAodFaceUpdateMonitor == null) {
            this.mAodFaceUpdateMonitor = new AODFaceUpdateMonitor();
        }
        return this.mAodFaceUpdateMonitor;
    }
}
