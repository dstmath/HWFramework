package huawei.com.android.server.fingerprint;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActivityManagerNative;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.biometrics.IBiometricServiceReceiver;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.hardware.input.InputManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.HwExHandler;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.server.LocalServices;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.wavemapping.cons.WMStateCons;
import com.android.server.lights.LightsManagerEx;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.systemui.shared.system.MetricsLoggerCompat;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.os.HwPowerManager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.displayengine.DisplayEngineManager;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import com.huawei.server.fingerprint.FingerprintAnimByThemeModel;
import com.huawei.server.fingerprint.FingerprintAnimByThemeView;
import com.huawei.server.fingerprint.FingerprintCircleOverlay;
import com.huawei.server.fingerprint.FingerprintController;
import com.huawei.server.fingerprint.FingerprintMaskOverlay;
import com.huawei.server.fingerprint.FingerprintViewUtils;
import com.huawei.server.fingerprint.HighLightMaskView;
import com.huawei.server.fingerprint.SuspensionButton;
import huawei.android.aod.HwAodManager;
import huawei.android.hardware.fingerprint.FingerprintManagerEx;
import huawei.android.hwcolorpicker.HwColorPicker;
import huawei.com.android.server.fingerprint.FingerprintView;
import huawei.com.android.server.fingerprint.SingleModeContentObserver;
import huawei.com.android.server.fingerprint.fingerprintAnimation.BreathImageDrawable;
import huawei.com.android.server.policy.BlurUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class FingerViewController {
    private static final int ALPHA_ANIMATION_THRESHOLD = 15;
    private static final float ALPHA_LIMITED = 0.863f;
    private static final String APS_INIT_HEIGHT = "aps_init_height";
    private static final String APS_INIT_WIDTH = "aps_init_width";
    private static final int BLUR_RADIO = 25;
    private static final float BLUR_SCALE = 0.125f;
    private static final int BRIGHTNESS_ENTER_TIME = 150;
    private static final int BRIGHTNESS_EXIT_TIME = 110;
    private static final int BRIGHTNESS_LIFT_TIME_LONG = 200;
    private static final int BRIGHTNESS_LIFT_TIME_SHORT = 16;
    private static final int CIRCLE_LAYER = 2147483645;
    private static final int COLOR_BLACK = -16250872;
    private static final float CONVERSION_PARA = 0.5f;
    private static final boolean DEBUG = true;
    private static final int DEFAULT_INIT_HEIGHT = 2880;
    private static final int DEFAULT_INIT_WIDTH = 1440;
    private static final int DEFAULT_LCD_DENSITY = 640;
    private static final int DESTORY_ANIM_DELAY_TIME = 10000;
    public static final int DISMISSED_REASON_NEGATIVE = 2;
    public static final int DISMISSED_REASON_POSITIVE = 1;
    private static final String DISPLAY_NOTCH_STATUS = "display_notch_status";
    public static final int FINGERPRINT_ACQUIRED_VENDOR = 6;
    public static final int FINGERPRINT_HELP_PAUSE_VENDORCODE = 11;
    public static final int FINGERPRINT_HELP_RESUME_VENDORCODE = 12;
    private static final String FINGERPRINT_IN_DISPLAY = "com.huawei.android.fingerprint.action.FINGERPRINT_IN_DISPLAY";
    private static final int FINGERPRINT_IN_DISPLAY_HELPCODE_CLOSE_VIEW_VALUE = 1011;
    private static final String FINGERPRINT_IN_DISPLAY_HELPCODE_KEY = "helpCode";
    private static final int FINGERPRINT_IN_DISPLAY_HELPCODE_SHOW_VIEW_VALUE = 1012;
    private static final int FINGERPRINT_IN_DISPLAY_HELPCODE_USE_PASSWORD_VALUE = 1010;
    private static final String FINGERPRINT_IN_DISPLAY_HELPSTRING_CLOSE_VIEW_VALUE = "finegrprint view closed";
    private static final String FINGERPRINT_IN_DISPLAY_HELPSTRING_KEY = "helpString";
    private static final String FINGERPRINT_IN_DISPLAY_HELPSTRING_SHOW_VIEW_VALUE = "finegrprint view show";
    private static final String FINGERPRINT_IN_DISPLAY_HELPSTRING_USE_PASSWORD_VALUE = "please use password";
    private static final int FINGERPRINT_IN_DISPLAY_POSITION_VIEW_VALUE_ELLE = 2101;
    private static final int FINGERPRINT_IN_DISPLAY_POSITION_VIEW_VALUE_VOGUE = 2133;
    public static final int FINGERPRINT_USE_PASSWORD_ERROR_CODE = 10;
    private static final int FINGER_BUTTON_SIZE_HALF = 2;
    private static final float FINGER_BUTTON_SIZE_TRANS_PARAMETER = 0.5f;
    private static final String FINGRPRINT_VIEW_TITLE_NAME = "hw_ud_fingerprint_mask_hwsinglemode_window";
    private static final int FLAG_FOR_HBM_SYNC = 500;
    public static final int HBM_TYPE_AHEAD = 1;
    public static final int HBM_TYPE_FINGERDOWN = 0;
    public static final int HIGHLIGHT_TYPE_AUTHENTICATE = 1;
    public static final int HIGHLIGHT_TYPE_AUTHENTICATE_CANCELED = 4;
    public static final int HIGHLIGHT_TYPE_AUTHENTICATE_SUCCESS = 2;
    public static final int HIGHLIGHT_TYPE_ENROLL = 0;
    public static final int HIGHLIGHT_TYPE_SCREENON = 5;
    public static final int HIGHLIGHT_TYPE_UNDEFINED = -1;
    public static final int HIGHLIGHT_VIEW_REMOVE_TIME = 3;
    private static final String HIGHLIGHT_VIEW_TITLE_NAME = "fingerprint_alpha_layer";
    private static final int INITIAL_HEIGHT = 100;
    private static final int INITIAL_VALUE = -1;
    private static final int INITIAL_WIDTN = 100;
    private static final int INVALID_COLOR = 0;
    private static final float LV_FINGERPRINT_POSITION_VIEW_HIGHT_SCALE = 0.78f;
    private static final int MASK_LAYER = 2147483644;
    private static final float MAX_BRIGHTNESS_LEVEL = 255.0f;
    private static final int MAX_CAPTURE_TIME = 1000;
    private static final int MAX_FAILED_ATTEMPTS_LOCKOUT_TIMED = 5;
    private static final float MIN_ALPHA = 0.004f;
    private static final int NOTCH_CORNER_STATUS_HIDE = 0;
    private static final int NOTCH_CORNER_STATUS_SHOW = 1;
    private static final int NOTCH_ROUND_CORNER_CODE = 8002;
    private static final int NOTCH_STATUS_DEFAULT = 0;
    private static final int NOTCH_STATUS_HIDE = 1;
    private static final String[] PACKAGES_USE_CANCEL_HOTSPOT_INTERFACES = {PKGNAME_OF_WALLET};
    private static final String[] PACKAGES_USE_HWAUTH_INTERFACES = {"com.huawei.hwid", PKGNAME_OF_WALLET, "com.huawei.android.hwpay"};
    public static final String PKGNAME_OF_KEYGUARD = "com.android.systemui";
    private static final String PKGNAME_OF_SECURITYMGR = "com.huawei.securitymgr";
    private static final String PKGNAME_OF_SETTINGS = "com.android.settings";
    private static final String PKGNAME_OF_SYSTEMMANAGER = "com.huawei.systemmanager";
    private static final String PKGNAME_OF_WALLET = "com.huawei.wallet";
    private static final int POSITION_PARAMETER_NUM = 4;
    private static final int RESULT_FINGERPRINT_AUTHENTICATION_CHECKING = 3;
    private static final int RESULT_FINGERPRINT_AUTHENTICATION_RESULT_FAIL = 1;
    private static final int RESULT_FINGERPRINT_AUTHENTICATION_RESULT_SUCCESS = 0;
    private static final int RESULT_FINGERPRINT_AUTHENTICATION_UNCHECKED = 2;
    private static final long SET_SCENE_DELAY_TIME = 80;
    private static final int SLIDE_HEIGHT_INDEXES = 1;
    private static final int SLIDE_PARAMETER_NUM = 4;
    private static final String SLIDE_PROP = SystemProperties.get("ro.config.hw_curved_side_disp", "");
    private static final String TAG = "FingerViewControllerTag";
    private static final String TAG_THREAD = "FingerViewControllerThread";
    public static final int TIME_UNIT = 1000;
    public static final int TYPE_CANCEL_FINGERPRINT = 3;
    public static final int TYPE_DISMISS_ALL = 0;
    public static final int TYPE_FINGERPRINT_BUTTON = 2;
    public static final int TYPE_FINGERPRINT_VIEW = 1;
    public static final int TYPE_FINGER_VIEW = 2105;
    private static final String UIDNAME_OF_KEYGUARD = "android.uid.systemui";
    private static final byte VIEW_STATE_DISABLED = 2;
    private static final byte VIEW_STATE_HIDE = 0;
    private static final byte VIEW_STATE_SHOW = 1;
    private static final byte VIEW_TYPE_BUTTON = 1;
    private static final byte VIEW_TYPE_FINGER_IMAGE = 3;
    private static final byte VIEW_TYPE_FINGER_MASK = 0;
    private static final byte VIEW_TYPE_HIGHLIGHT = 2;
    public static final int WAITTING_TIME = 30;
    private static int sDefaultBrightness = 56;
    private static int sDefaultLcdDpi = DEFAULT_LCD_DENSITY;
    private static int[] sDefaultSampleAlpha = {234, 229, 219, 220, 216, WMStateCons.MSG_APP_DATA_STALL, 208, HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, 187, 176, 170, 163, 159, 142, WMStateCons.MSG_CHECK_4G_COVERAGE, WMStateCons.MSG_CHECK_4G_COVERAGE, 125, 121, 111, 101, 92, 81, 81, 69, 68, 58, 56, 46, 44, 35, 34, 30, 22, 23, 18, 0, 0};
    private static int[] sDefaultSampleBrightness = {4, 6, 8, 10, 12, 14, 16, 20, 24, 28, 30, 34, 40, 46, 50, 56, 64, 74, 84, 94, 104, HwArbitrationDEFS.MSG_INSTANT_TRAVEL_APP_END, 124, 134, 144, 154, 164, 174, 184, 194, 204, 214, MetricsLoggerCompat.OVERVIEW_ACTIVITY, 234, 244, 248, 255};
    private static float sFingerPositionHeightScale = 0.0f;
    private static FingerViewController sInstance;
    private static int sInvalidBrightness = -1;
    private static String sPanelInfoNode = "/sys/class/graphics/fb0/panel_info";
    private static int sUdBuffSize = 12;
    private final Runnable mAddBackFingprintRunnable;
    private final Runnable mAddButtonViewRunnable;
    private final Runnable mAddFingerViewRunnable;
    private final Runnable mAddImageOnlyRunnable;
    private BreathImageDrawable mAlipayDrawable;
    private List<String> mAnimFileNames = new ArrayList(30);
    private HwAodManager mAodManager;
    private RelativeLayout.LayoutParams mAppParamsCancelView;
    private RelativeLayout.LayoutParams mAppParamsUsePassworView;
    private int mAuthenticateResult;
    private Button mBackFingerprintCancelView;
    private TextView mBackFingerprintHintView;
    private Button mBackFingerprintUsePasswordView;
    private BackFingerprintView mBackFingerprintView;
    private IBiometricServiceReceiverInternal mBiometricServiceReceiver;
    private Bitmap mBlurBitmap;
    private BitmapDrawable mBlurDrawable;
    private int mButtonCenterX = -1;
    private int mButtonCenterY = -1;
    private int mButtonColor = 0;
    private SuspensionButton mButtonView;
    private int mButtonViewState = 1;
    private int mCancelButtonMarginEnd;
    private int mCancelButtonMarginStart;
    private RelativeLayout mCancelView;
    private RelativeLayout mCancelViewImageOnly;
    private ContentResolver mContentResolver;
    private Context mContext;
    private int mCurrentAlpha = 0;
    private int mCurrentBrightness = -1;
    private int mCurrentHeight;
    private int mCurrentRotation;
    private int mCurrentWidth;
    private int mDefaultDisplayHeight;
    private int mDefaultDisplayWidth;
    private final Runnable mDestroyAnimViewRunnable;
    private IBiometricServiceReceiver mDialogReceiver;
    private DisplayEngineManager mDisplayEngineManager;
    private int mDisplayNotchStatus = 0;
    private int mEnrollDigitalBrigtness;
    private Handler mFingerHandler;
    private int mFingerLogoRadius;
    private FingerprintView mFingerView;
    private ICallBack mFingerViewChangeCallback;
    private WindowManager.LayoutParams mFingerViewParams;
    private FingerprintAnimByThemeView mFingerprintAnimByThemeView;
    private FingerprintAnimationView mFingerprintAnimationView;
    private int mFingerprintCenterX = -1;
    private int mFingerprintCenterY = -1;
    private FingerprintCircleOverlay mFingerprintCircleOverlay;
    private ImageView mFingerprintImageForAlipay;
    private FingerprintManagerEx mFingerprintManagerEx;
    private FingerprintMaskOverlay mFingerprintMaskOverlay;
    private final Runnable mFingerprintMaskSetAlpha;
    private int[] mFingerprintPositions = new int[4];
    private ImageView mFingerprintView;
    private float mFontScale;
    private Handler mHandler = null;
    protected final HandlerThread mHandlerThread = new HandlerThread("FingerViewController");
    private int mHighLightRemoveType;
    private int mHighLightShowType;
    private HighLightMaskView mHighLightView;
    private final Runnable mHighLightViewRunnable;
    private int mHighlightBrightnessLevel;
    private int mHighlightSpotColor;
    private int mHighlightSpotRadius;
    private String mHint;
    private HintText mHintView;
    private IPowerManager mIpowerManager;
    private boolean mIsBiometricPrompt;
    private boolean mIsBiometricRequireConfirmation;
    private boolean mIsButtonViewAdded = false;
    private boolean mIsCancelHotSpotPkgAdded;
    private boolean mIsFingerFrozen = false;
    private boolean mIsFingerprintOnlyViewAdded = false;
    private boolean mIsFingerprintViewAdded = false;
    private boolean mIsHasBackFingerprint = false;
    private boolean mIsHasUdFingerprint = true;
    private boolean mIsKeepMaskAfterAuth = false;
    private boolean mIsKeygaurdCoverd;
    private boolean mIsNeedReload;
    private boolean mIsNotchConerStatusChanged = false;
    private boolean mIsSingleModeObserverRegistered = false;
    private boolean mIsUseDefaultHint = true;
    private boolean mIsWidgetColorSet = false;
    private boolean mIshighLightViewAdded = false;
    private KeyguardManager mKeyguardManager;
    private RelativeLayout mLayoutForAlipay;
    private LayoutInflater mLayoutInflater;
    private final Runnable mLoadFingerprintAnimViewRunnable;
    private Button mLvBackFingerprintCancelView;
    private Button mLvBackFingerprintUsePasswordView;
    private float mMaxDigitalBrigtness;
    private RemainTimeCountDown mMyCountDown = null;
    private int mNormalizedMaxBrightness;
    private int mNormalizedMinBrightness;
    private int mNotchHeight;
    private Bundle mPkgAttributes;
    private String mPkgName;
    private PowerManager mPowerManager;
    private IFingerprintServiceReceiver mReceiver;
    private int mRemainTimes = 5;
    private int mRemainedSecs;
    private RelativeLayout mRemoteView;
    private final Runnable mRemoveBackFingprintRunnable;
    private final Runnable mRemoveButtonViewRunnable;
    private final Runnable mRemoveFingerHighLightView;
    private final Runnable mRemoveFingerViewRunnable;
    private final Runnable mRemoveHighLightView;
    private final Runnable mRemoveHighlightCircleRunnable;
    private final Runnable mRemoveImageOnlyRunnable;
    private int[] mSampleAlphas = null;
    private int[] mSampleBrightness = null;
    private int mSavedBackViewDpi;
    private int mSavedBackViewHeight;
    private int mSavedBackViewRotation;
    private int mSavedButtonHeight;
    private int mSavedImageDpi;
    private int mSavedImageHeight;
    private int mSavedMaskDpi;
    private int mSavedMaskHeight;
    private int mSavedRotation;
    private Bitmap mScreenShot;
    private final Runnable mSetEnrollLightLevelRunnable;
    private final Runnable mSetOutProtectEye;
    private final Runnable mSetProtectEye;
    private final Runnable mSetScene;
    private final Runnable mShowHighlightCircleRunnable;
    private SingleModeContentObserver mSingleContentObserver;
    private String mSubTitle;
    private WindowManager.LayoutParams mSuspensionButtonParams;
    private final Runnable mUpdateButtonViewRunnable;
    private final Runnable mUpdateFingerprintViewRunnable;
    private final Runnable mUpdateFingprintRunnable;
    private final Runnable mUpdateImageOnlyRunnable;
    private final Runnable mUpdateMaskAttibuteRunnable;
    private int mWidgetColor;
    private final WindowManager mWindowManager;
    private int mWindowType;
    private ContentObserver settingsDisplayObserver;

    public interface ICallBack {
        void onFingerViewStateChange(int i);

        void onNotifyBlueSpotDismiss();

        void onNotifyCaptureImage();
    }

    private FingerViewController(Context context) {
        int i = sInvalidBrightness;
        this.mNormalizedMinBrightness = i;
        this.mNormalizedMaxBrightness = i;
        this.mIsCancelHotSpotPkgAdded = false;
        this.mCancelButtonMarginStart = 0;
        this.mCancelButtonMarginEnd = 0;
        this.settingsDisplayObserver = new ContentObserver(this.mHandler) {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                FingerViewController.this.mHandler.postDelayed(new Runnable() {
                    /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass1.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        FingerViewController.this.mDefaultDisplayHeight = Settings.Global.getInt(FingerViewController.this.mContentResolver, FingerViewController.APS_INIT_HEIGHT, FingerViewController.DEFAULT_INIT_HEIGHT);
                        FingerViewController.this.mDefaultDisplayWidth = Settings.Global.getInt(FingerViewController.this.mContentResolver, FingerViewController.APS_INIT_WIDTH, FingerViewController.DEFAULT_INIT_WIDTH);
                        int currentHeight = SystemPropertiesEx.getInt("persist.sys.rog.height", FingerViewController.this.mDefaultDisplayHeight);
                        int currentWidth = SystemPropertiesEx.getInt("persist.sys.rog.width", FingerViewController.this.mDefaultDisplayWidth);
                        if (FingerViewController.this.mCurrentHeight == currentHeight && FingerViewController.this.mCurrentWidth == currentWidth) {
                            FingerViewController.this.mIsNeedReload = true;
                        } else {
                            FingerViewController.this.mCurrentHeight = currentHeight;
                            FingerViewController.this.mCurrentWidth = currentWidth;
                            FingerViewController.this.mIsNeedReload = false;
                        }
                        Log.i(FingerViewController.TAG_THREAD, "settingsDisplayObserver onChange:" + FingerViewController.this.mDefaultDisplayHeight + "," + FingerViewController.this.mDefaultDisplayWidth + "," + FingerViewController.this.mCurrentHeight + "," + FingerViewController.this.mCurrentWidth + "," + FingerViewController.this.mIsNeedReload);
                    }
                }, 30);
            }
        };
        this.mUpdateMaskAttibuteRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "mUpdateMaskAttibuteRunnable mHint:" + FingerViewController.this.mHint);
                FingerViewController fingerViewController = FingerViewController.this;
                fingerViewController.updateHintView(fingerViewController.mHint);
                if (FingerViewController.this.mFingerprintView != null) {
                    FingerViewController.this.mFingerprintView.setContentDescription(FingerViewController.this.mHint);
                }
            }
        };
        this.mFingerprintMaskSetAlpha = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                if (FingerViewController.this.mFingerprintMaskOverlay.isVisible()) {
                    Log.i(FingerViewController.TAG, "fingerprintMaskSetAlpha");
                    SurfaceControl.openTransaction();
                    try {
                        FingerViewController.this.mFingerprintMaskOverlay.setAlpha(((float) FingerViewController.this.mCurrentAlpha) / FingerViewController.MAX_BRIGHTNESS_LEVEL);
                    } finally {
                        SurfaceControl.closeTransaction();
                    }
                }
            }
        };
        this.mAddImageOnlyRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "begin mAddImageOnlyRunnable");
                if (FingerViewController.this.isNewMagazineViewForDownFP()) {
                    FingerViewController.this.setFPAuthState(true);
                }
                FingerViewController fingerViewController = FingerViewController.this;
                fingerViewController.mDefaultDisplayHeight = Settings.Global.getInt(fingerViewController.mContext.getContentResolver(), FingerViewController.APS_INIT_HEIGHT, FingerViewController.DEFAULT_INIT_HEIGHT);
                FingerViewController fingerViewController2 = FingerViewController.this;
                fingerViewController2.mDefaultDisplayWidth = Settings.Global.getInt(fingerViewController2.mContext.getContentResolver(), FingerViewController.APS_INIT_WIDTH, FingerViewController.DEFAULT_INIT_WIDTH);
                FingerViewController.this.createImageOnlyView();
            }
        };
        this.mUpdateImageOnlyRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass5 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "begin mUpdateImageOnlyRunnable");
                FingerViewController.this.updateImageOnlyView();
            }
        };
        this.mDestroyAnimViewRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass6 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG, "come in mDestroyAnimViewRunnable");
                if (FingerViewController.this.mFingerprintAnimByThemeView != null) {
                    FingerViewController.this.removeAnimViewIfAttached();
                    FingerViewController.this.mFingerprintAnimByThemeView.destroy();
                    FingerViewController.this.mFingerprintAnimByThemeView = null;
                    FingerprintAnimByThemeModel.setDestoryAmount();
                }
            }
        };
        this.mLoadFingerprintAnimViewRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass7 */

            @Override // java.lang.Runnable
            public void run() {
                Log.d(FingerViewController.TAG, "come in mLoadFingerprintAnimViewRunnable");
                if (FingerprintAnimByThemeModel.isGetThemeError(FingerViewController.this.mContext)) {
                    FingerViewController.this.mAnimFileNames.clear();
                } else if (FingerprintAnimByThemeModel.isThemChanged(FingerViewController.this.mFingerprintAnimByThemeView, FingerViewController.this.getPxScale()) || FingerViewController.this.mFingerprintAnimByThemeView == null || FingerViewController.this.mFingerprintAnimByThemeView.isLanguageChange()) {
                    FingerViewController.this.removeAnimViewIfAttached();
                    List<String> fpLoadFileNames = FingerprintAnimByThemeModel.preloadFilesFromPath(FingerViewController.this.mContext);
                    FingerViewController.this.mAnimFileNames.clear();
                    FingerViewController.this.mFingerprintAnimByThemeView = null;
                    if (fpLoadFileNames.isEmpty()) {
                        Log.i(FingerViewController.TAG, "the mAnimFileNames is empty, we use default theme");
                        return;
                    }
                    FingerViewController.this.mAnimFileNames.addAll(fpLoadFileNames);
                    FingerViewController.this.initFingerprintAnimView();
                } else {
                    Log.i(FingerViewController.TAG, "come in isLanguageChange");
                }
            }
        };
        this.mRemoveImageOnlyRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass8 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "begin mRemoveImageOnlyRunnable");
                FingerViewController.this.setFPAuthState(false);
                FingerViewController.this.removeImageOnlyView();
            }
        };
        this.mAddButtonViewRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass9 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG, "begin mAddButtonViewRunnable");
                FingerViewController.this.createAndAddButtonView();
            }
        };
        this.mRemoveButtonViewRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass10 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "begin mRemoveButtonViewRunnable");
                FingerViewController.this.removeButtonView();
            }
        };
        this.mUpdateButtonViewRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass11 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "begin mUpdateButtonViewRunnable");
                FingerViewController.this.updateButtonView();
            }
        };
        this.mRemoveFingerHighLightView = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass12 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "begin mRemoveFingerHighLightView");
                if (FingerViewController.this.mHandler.hasCallbacks(FingerViewController.this.mSetEnrollLightLevelRunnable)) {
                    FingerViewController.this.mHandler.removeCallbacks(FingerViewController.this.mSetEnrollLightLevelRunnable);
                    Log.i(FingerViewController.TAG_THREAD, "remove mSetEnrollLightLevelRunnable");
                }
                if (FingerViewController.this.mDisplayEngineManager != null) {
                    FingerViewController.this.mDisplayEngineManager.setScene(34, 0);
                    Log.i(FingerViewController.TAG_THREAD, "mRemoveFingerHighLightView exit hbm");
                }
            }
        };
        this.mRemoveHighLightView = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass13 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "begin mRemoveHighLightView");
                FingerViewController.this.removeHighLightViewInner();
            }
        };
        this.mHighLightViewRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass14 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "begin mHighLightViewRunnable type" + FingerViewController.this.mHighLightShowType);
                int i = FingerViewController.this.mHighLightShowType;
                if (i == 0) {
                    FingerViewController.this.createAndAddHighLightView();
                    FingerViewController.this.mHandler.postDelayed(FingerViewController.this.mSetEnrollLightLevelRunnable, 150);
                    if (FingerViewController.this.mHighLightView != null) {
                        FingerViewController fingerViewController = FingerViewController.this;
                        int endAlpha = fingerViewController.getMaskAlpha(fingerViewController.mCurrentBrightness);
                        FingerViewController fingerViewController2 = FingerViewController.this;
                        fingerViewController2.startAlphaValueAnimation(fingerViewController2.mHighLightView, true, 0.0f, ((float) endAlpha) / FingerViewController.MAX_BRIGHTNESS_LEVEL, 150, 0);
                    }
                } else if (i == 1) {
                    FingerViewController.this.createAndAddHighLightView();
                } else if (i == 5) {
                    FingerViewController.this.createMaskAndCircleOnKeyguard();
                }
            }
        };
        this.mSetEnrollLightLevelRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass15 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "begin mSetEnrollLightLevelRunnable");
                if (FingerViewController.this.mDisplayEngineManager != null) {
                    int digitalBrigtness = FingerViewController.this.getEnrollDigitalBrigtness();
                    FingerViewController.this.mDisplayEngineManager.setScene(34, digitalBrigtness);
                    Log.i(FingerViewController.TAG_THREAD, "mDisplayEngineManager enter hbm level digitalBrigtness:" + digitalBrigtness);
                }
            }
        };
        this.mRemoveHighlightCircleRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass16 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "RemoveHighlightCircle mHighLightShowType = " + FingerViewController.this.mHighLightShowType);
                if (FingerViewController.this.mFingerprintCircleOverlay != null && FingerViewController.this.mFingerprintCircleOverlay.isVisible()) {
                    SurfaceControl.openTransaction();
                    try {
                        FingerViewController.this.mFingerprintCircleOverlay.hide();
                    } finally {
                        SurfaceControl.closeTransaction();
                    }
                } else if (FingerViewController.this.mIshighLightViewAdded && FingerViewController.this.mHighLightView != null && FingerViewController.this.mHighLightView.isAttachedToWindow()) {
                    FingerViewController.this.mHighLightView.setCircleVisibility(4);
                }
            }
        };
        this.mShowHighlightCircleRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass17 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "begin mShowHighlightCircleRunnable, mHighLightShowType = " + FingerViewController.this.mHighLightShowType + ",mIshighLightViewAdded = " + FingerViewController.this.mIshighLightViewAdded);
                if (FingerViewController.this.mHighLightShowType == 0 && FingerViewController.this.mIshighLightViewAdded && FingerViewController.this.mHighLightView.getCircleVisibility() == 4) {
                    FingerViewController.this.mHandler.removeCallbacks(FingerViewController.this.mRemoveHighlightCircleRunnable);
                    FingerViewController.this.getCurrentFingerprintCenter();
                    FingerViewController.this.mHighLightView.setCenterPoints(FingerViewController.this.mFingerprintCenterX, FingerViewController.this.mFingerprintCenterY);
                    FingerViewController.this.mHighLightView.setCircleVisibility(0);
                    FingerViewController.this.mHandler.postDelayed(FingerViewController.this.mRemoveHighlightCircleRunnable, 1200);
                }
            }
        };
        this.mAddFingerViewRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass18 */

            @Override // java.lang.Runnable
            public void run() {
                FingerViewController.this.createFingerprintView();
                Log.i(FingerViewController.TAG, "begin mAddFingerViewRunnable");
            }
        };
        this.mRemoveFingerViewRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass19 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG, "begin mRemoveFingerViewRunnable");
                FingerViewController.this.removeFingerprintView();
            }
        };
        this.mUpdateFingerprintViewRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass20 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG, "begin mUpdateFingerprintViewRunnable");
                FingerViewController.this.updateFingerprintView();
            }
        };
        this.mSetProtectEye = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass21 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG, "begin mSetProtectEye");
                if (FingerViewController.this.mDisplayEngineManager != null) {
                    FingerViewController.this.mDisplayEngineManager.setScene(31, 16);
                    Log.w(FingerViewController.TAG, "mDisplayEngineManager set ProtectEye");
                }
            }
        };
        this.mSetOutProtectEye = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass22 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG, "begin mSetOutProtectEye");
                if (FingerViewController.this.mDisplayEngineManager != null) {
                    FingerViewController.this.mDisplayEngineManager.setScene(31, 17);
                    Log.w(FingerViewController.TAG, "mDisplayEngineManager set OutProtectEye");
                }
            }
        };
        this.mSetScene = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass23 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG, "begin mSetScene");
                if (FingerViewController.this.mDisplayEngineManager != null) {
                    FingerViewController.this.mDisplayEngineManager.setScene(29, 0);
                    Log.i(FingerViewController.TAG, "mDisplayEngineManager set scene 0");
                }
            }
        };
        this.mAddBackFingprintRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass24 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "begin mAddBackFingprintRunnable");
                FingerViewController fingerViewController = FingerViewController.this;
                fingerViewController.mDefaultDisplayHeight = Settings.Global.getInt(fingerViewController.mContext.getContentResolver(), FingerViewController.APS_INIT_HEIGHT, FingerViewController.DEFAULT_INIT_HEIGHT);
                FingerViewController fingerViewController2 = FingerViewController.this;
                fingerViewController2.mDefaultDisplayWidth = Settings.Global.getInt(fingerViewController2.mContext.getContentResolver(), FingerViewController.APS_INIT_WIDTH, FingerViewController.DEFAULT_INIT_WIDTH);
                FingerViewController.this.createBackFingprintView();
            }
        };
        this.mRemoveBackFingprintRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass25 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "begin mRemoveBackFingprintRunnable");
                FingerViewController.this.removeBackFingprintView();
            }
        };
        this.mUpdateFingprintRunnable = new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass26 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "begin mUpdateFingprintRunnable");
                FingerViewController.this.updateBackFingprintView();
            }
        };
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        this.mHandlerThread.start();
        this.mHandler = new HwExHandler(this.mHandlerThread.getLooper(), 500);
        this.mLayoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mIpowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mFingerprintManagerEx = new FingerprintManagerEx(this.mContext);
        this.mAodManager = HwAodManager.getInstance();
        this.mDisplayEngineManager = new DisplayEngineManager();
        this.mFingerprintMaskOverlay = new FingerprintMaskOverlay();
        this.mFingerprintCircleOverlay = new FingerprintCircleOverlay(context);
        getBrightnessRangeFromPanelInfo();
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        long identityToken = Binder.clearCallingIdentity();
        try {
            this.mContentResolver.registerContentObserver(Settings.Global.getUriFor(APS_INIT_HEIGHT), false, this.settingsDisplayObserver);
            this.mContentResolver.registerContentObserver(Settings.Global.getUriFor(APS_INIT_WIDTH), false, this.settingsDisplayObserver);
            this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("display_size_forced"), false, this.settingsDisplayObserver);
            this.settingsDisplayObserver.onChange(true);
            FingerprintController.getInstance().getFingerLogoColorSettingsInfor(this.mContentResolver, this.mHandler);
            try {
                initBrightnessAlphaConfig();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "initBrightnessAlphaConfig fail FileNotFoundException");
            } catch (IOException e2) {
                Log.e(TAG, "initBrightnessAlphaConfig fail IOException");
            } catch (Exception e3) {
                Log.e(TAG, "initBrightnessAlphaConfig fail");
            }
            Configuration curConfig = new Configuration();
            try {
                curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
                this.mFontScale = curConfig.fontScale;
            } catch (RemoteException e4) {
                Log.w(TAG, "Unable to retrieve font size");
            }
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    public static FingerViewController getInstance(Context context) {
        FingerViewController fingerViewController;
        synchronized (FingerViewController.class) {
            if (sInstance == null) {
                sInstance = new FingerViewController(context);
            }
            fingerViewController = sInstance;
        }
        return fingerViewController;
    }

    public void registCallback(ICallBack fingerViewChangeCallback) {
        this.mFingerViewChangeCallback = fingerViewChangeCallback;
    }

    public String getCurrentPackage() {
        Log.i(TAG, "current package is " + this.mPkgName);
        return this.mPkgName;
    }

    public Handler getFingerHandler() {
        return this.mFingerHandler;
    }

    public void setFingerHandler(Handler fingerHandler) {
        this.mFingerHandler = fingerHandler;
    }

    public void showMaskOrButtonInit(IFingerprintServiceReceiver receiver, boolean isHasUdFingerprint, boolean isHasBackFingerprint, IBiometricServiceReceiver dialogReceiver) {
        this.mReceiver = receiver;
        this.mDialogReceiver = dialogReceiver;
        this.mIsHasUdFingerprint = isHasUdFingerprint;
        this.mIsHasBackFingerprint = isHasBackFingerprint;
    }

    public void showMaskOrButton(String pkgName, Bundle bundle, int type) {
        this.mPkgName = pkgName;
        this.mPkgAttributes = bundle;
        this.mIsWidgetColorSet = false;
        Log.i(TAG, "showMaskOrButton, mPkgAttributes=" + bundle + "mHighlightBrightnessLevel = " + this.mHighlightBrightnessLevel + " package " + pkgName + " type is " + type);
        Bundle bundle2 = this.mPkgAttributes;
        if (bundle2 == null || bundle2.getString("SystemTitle") == null) {
            this.mIsUseDefaultHint = true;
        } else {
            this.mIsUseDefaultHint = false;
        }
        if ("com.huawei.systemmanager".equals(pkgName) || PKGNAME_OF_SECURITYMGR.equals(pkgName) || PKGNAME_OF_WALLET.equals(pkgName)) {
            Log.i(TAG, "do not show mask for " + pkgName);
            return;
        }
        String str = this.mPkgName;
        if (str == null || !str.equals("com.android.systemui")) {
            this.mWindowType = HwArbitrationDEFS.MSG_VPN_STATE_OPEN;
            this.mNotchHeight = FingerprintViewUtils.getFingerprintNotchHeight(this.mContext);
            if (type == 0) {
                this.mHandler.post(this.mAddFingerViewRunnable);
                Context context = this.mContext;
                Flog.bdReport(context, 501, "{PkgName:" + this.mPkgName + "}");
            } else if (type == 1) {
                this.mHandler.post(this.mAddButtonViewRunnable);
            } else if (type == 3) {
                this.mIsCancelHotSpotPkgAdded = isCancelHotSpotViewVisble(this.mPkgName);
                Log.i(TAG, "isCancelHotSpotNeed(mPkgName: " + this.mPkgName + " mIsCancelHotSpotPkgAdded: " + this.mIsCancelHotSpotPkgAdded);
                this.mHandler.post(this.mAddImageOnlyRunnable);
            } else if (type == 4) {
                this.mHandler.post(this.mAddBackFingprintRunnable);
            }
        } else {
            this.mWindowType = 2014;
        }
    }

    public void showMaskForApp(Bundle attribute) {
        this.mPkgName = getForegroundPkgName();
        this.mPkgAttributes = attribute;
        String str = this.mPkgName;
        if (str == null || !str.equals("com.android.systemui")) {
            this.mWindowType = HwArbitrationDEFS.MSG_VPN_STATE_OPEN;
        } else {
            this.mWindowType = 2014;
        }
        Bundle bundle = this.mPkgAttributes;
        if (bundle == null || bundle.getString("SystemTitle") == null) {
            this.mIsUseDefaultHint = true;
        } else {
            this.mIsUseDefaultHint = false;
        }
        this.mHandler.post(this.mAddFingerViewRunnable);
    }

    public void showSuspensionButtonForApp(int centerX, int centerY, String callingUidName) {
        if (callingUidName != null) {
            Log.i(TAG, "mButtonCenterX = " + this.mButtonCenterX + ",mButtonCenterY =" + this.mButtonCenterY + ",callingUidName = " + callingUidName + ", centerX = " + centerX + ",centerY =" + centerY);
            this.mButtonCenterX = centerX;
            this.mButtonCenterY = centerY;
            if ("android.uid.systemui".equals(callingUidName)) {
                this.mPkgName = "com.android.systemui";
            }
            this.mWindowType = HwArbitrationDEFS.MSG_VPN_STATE_OPEN;
            this.mPkgAttributes = null;
            ICallBack iCallBack = this.mFingerViewChangeCallback;
            if (iCallBack != null) {
                iCallBack.onFingerViewStateChange(2);
            }
            this.mHandler.post(this.mAddButtonViewRunnable);
        }
    }

    public void closeEyeProtecttionMode(String pkgName) {
        if (pkgName != null) {
            Log.i(TAG, "closeEyeProtecttionMode pkgName:" + pkgName);
            if (!"com.android.systemui".equals(pkgName) && !"com.android.settings".equals(pkgName)) {
                this.mHandler.removeCallbacks(this.mSetProtectEye);
                this.mHandler.post(this.mSetProtectEye);
            }
        }
    }

    public void reopenEyeProtecttionMode(String pkgName) {
        if (pkgName != null) {
            Log.i(TAG, "reopenEyeProtecttionMode pkgName:" + pkgName);
            if (!"com.android.systemui".equals(pkgName) && !"com.android.settings".equals(pkgName)) {
                this.mHandler.post(this.mSetOutProtectEye);
            }
        }
    }

    public void removeMaskOrButton() {
        removeMaskOrButton(false);
    }

    public void removeMaskOrButton(boolean async) {
        Log.i(TAG, "removeMaskOrButton pkgName:" + this.mPkgName + ", async:" + async);
        this.mHandler.post(this.mRemoveHighlightCircleRunnable);
        this.mHandler.post(this.mRemoveFingerViewRunnable);
        this.mHandler.post(this.mRemoveButtonViewRunnable);
        this.mHandler.post(this.mRemoveImageOnlyRunnable);
        this.mHandler.post(this.mRemoveBackFingprintRunnable);
        if (async) {
            this.mHandler.post(new Runnable() {
                /* class huawei.com.android.server.fingerprint.$$Lambda$FingerViewController$dlqdPs5ubPZGdNE2I7elTXiwb7k */

                @Override // java.lang.Runnable
                public final void run() {
                    FingerViewController.this.lambda$removeMaskOrButton$0$FingerViewController();
                }
            });
        } else {
            this.mFingerViewChangeCallback.onFingerViewStateChange(0);
        }
    }

    public /* synthetic */ void lambda$removeMaskOrButton$0$FingerViewController() {
        this.mFingerViewChangeCallback.onFingerViewStateChange(0);
    }

    public void removeMaskAndShowButton() {
        this.mHandler.post(this.mRemoveFingerViewRunnable);
        this.mHandler.post(this.mAddButtonViewRunnable);
        this.mFingerViewChangeCallback.onFingerViewStateChange(2);
    }

    public void updateMaskViewAttributes(Bundle attributes, String pkgname) {
        if (attributes != null && pkgname != null) {
            String hint = attributes.getString("SystemTitle");
            if (this.mIsFingerprintViewAdded && hint != null) {
                Log.i(TAG, "updateMaskViewAttributes,hint = " + hint);
                this.mHint = hint;
                this.mHandler.post(this.mUpdateMaskAttibuteRunnable);
            }
        }
    }

    public void updateFingerprintView(int result, boolean isKeepMaskAfterAuthentication) {
        this.mAuthenticateResult = result;
        this.mIsKeepMaskAfterAuth = isKeepMaskAfterAuthentication;
        Log.d(TAG, "mIsUseDefaultHint = " + this.mIsUseDefaultHint);
        FingerprintView fingerprintView = this.mFingerView;
        if (fingerprintView == null || !fingerprintView.isAttachedToWindow()) {
            BackFingerprintView backFingerprintView = this.mBackFingerprintView;
            if (backFingerprintView != null && backFingerprintView.isAttachedToWindow()) {
                this.mHandler.post(this.mUpdateFingprintRunnable);
                return;
            }
            return;
        }
        this.mHandler.post(this.mUpdateFingerprintViewRunnable);
    }

    public void updateFingerprintView(int result, int failTimes) {
        if (result != 2) {
            this.mRemainTimes = 5 - failTimes;
        }
        this.mAuthenticateResult = result;
        FingerprintView fingerprintView = this.mFingerView;
        if (fingerprintView == null || !fingerprintView.isAttachedToWindow()) {
            BackFingerprintView backFingerprintView = this.mBackFingerprintView;
            if (backFingerprintView != null && backFingerprintView.isAttachedToWindow()) {
                this.mHandler.post(this.mUpdateFingprintRunnable);
            }
        } else {
            this.mHandler.post(this.mUpdateFingerprintViewRunnable);
        }
        RelativeLayout relativeLayout = this.mLayoutForAlipay;
        if (relativeLayout != null && relativeLayout.isAttachedToWindow()) {
            this.mHandler.post(this.mUpdateImageOnlyRunnable);
        }
    }

    public void showHighlightview(int type) {
        if (!isScreenOn() || this.mIshighLightViewAdded) {
            Log.i(TAG, "Screen not on or already added");
            return;
        }
        this.mHighLightShowType = type;
        Log.i(TAG, "show Highlightview mHighLightShowType:" + this.mHighLightShowType);
        this.mHandler.removeCallbacks(this.mHighLightViewRunnable);
        this.mHandler.removeCallbacks(this.mRemoveHighLightView);
        this.mHandler.post(this.mHighLightViewRunnable);
        if (type == 1) {
            this.mHandler.postDelayed(this.mRemoveHighLightView, 1200);
        }
    }

    public void showHighlightviewOnKeyguard() {
        this.mHighLightShowType = 5;
        Log.i(TAG, "show Highlightview mHighLightShowType:" + this.mHighLightShowType);
        this.mHandler.removeCallbacks(this.mHighLightViewRunnable);
        this.mHandler.post(this.mHighLightViewRunnable);
    }

    public void loadFingerviewAnimOnKeyguard() {
        this.mHandler.removeCallbacks(this.mLoadFingerprintAnimViewRunnable);
        this.mHandler.removeCallbacks(this.mDestroyAnimViewRunnable);
        this.mHandler.post(this.mLoadFingerprintAnimViewRunnable);
    }

    public void removeHighlightviewOnKeyguard() {
        this.mHandler.post(new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass27 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "removeHighlightviewOnKeyguard");
                SurfaceControl.openTransaction();
                try {
                    FingerViewController.this.mFingerprintMaskOverlay.hide();
                    FingerViewController.this.mFingerprintCircleOverlay.hide();
                    if (!FingerViewController.this.mAnimFileNames.isEmpty()) {
                        FingerViewController.this.removeAnimViewIfAttached();
                    } else if (FingerViewController.this.mFingerprintAnimationView != null && FingerViewController.this.mFingerprintAnimationView.isAttachedToWindow()) {
                        FingerViewController.this.mFingerprintAnimationView.setAddState(false);
                        FingerViewController.this.mWindowManager.removeViewImmediate(FingerViewController.this.mFingerprintAnimationView);
                    }
                } finally {
                    SurfaceControl.closeTransaction();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeAnimViewIfAttached() {
        FingerprintAnimByThemeView fingerprintAnimByThemeView = this.mFingerprintAnimByThemeView;
        if (fingerprintAnimByThemeView != null && fingerprintAnimByThemeView.isAttachedToWindow() && this.mFingerprintAnimByThemeView.isAdded()) {
            this.mFingerprintAnimByThemeView.setAddState(false);
            this.mWindowManager.removeViewImmediate(this.mFingerprintAnimByThemeView);
        }
    }

    public void destroyHighlightviewOnKeyguard() {
        this.mHandler.post(new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass28 */

            @Override // java.lang.Runnable
            public void run() {
                FingerViewController.this.mFingerprintMaskOverlay.destroy();
                FingerViewController.this.mFingerprintCircleOverlay.destroy();
                FingerViewController.this.removeFingerprintAnimationView();
            }
        });
    }

    public void destroyFingerAnimViewOnKeyguard() {
        Log.i(TAG, "come in destroyFingerAnimViewOnKeyguard");
        this.mHandler.post(new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass29 */

            @Override // java.lang.Runnable
            public void run() {
                FingerViewController.this.removeAnimViewIfAttached();
            }
        });
        this.mHandler.removeCallbacks(this.mDestroyAnimViewRunnable);
        this.mHandler.postDelayed(this.mDestroyAnimViewRunnable, HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeFingerprintAnimationView() {
        FingerprintAnimationView fingerprintAnimationView = this.mFingerprintAnimationView;
        if (fingerprintAnimationView != null && fingerprintAnimationView.isAttachedToWindow() && this.mWindowManager != null && this.mFingerprintAnimationView.isAdded()) {
            this.mFingerprintAnimationView.setAddState(false);
            this.mWindowManager.removeViewImmediate(this.mFingerprintAnimationView);
            Log.i(TAG_THREAD, "removeFingerprintAnimationView");
        }
    }

    public void showHighlightCircleOnKeyguard() {
        if (!isScreenOn()) {
            Log.i(TAG, "Screen not on, return");
            if (!FingerprintController.getInstance().isInDreamingAndSuperWallpaper(this.mContext)) {
                return;
            }
        }
        this.mHandler.post(new Runnable() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass30 */

            /* JADX INFO: finally extract failed */
            @Override // java.lang.Runnable
            public void run() {
                Log.i(FingerViewController.TAG_THREAD, "showHighlightCircleOnKeyguard");
                SurfaceControl.openTransaction();
                try {
                    if (!FingerViewController.this.mFingerprintCircleOverlay.isCreate()) {
                        Log.i(FingerViewController.TAG, "circle not created, create it before show");
                        float scale = FingerViewController.this.getPxScale();
                        FingerViewController.this.mFingerprintCenterX = (FingerViewController.this.mFingerprintPositions[0] + FingerViewController.this.mFingerprintPositions[2]) / 2;
                        FingerViewController.this.mFingerprintCenterY = (FingerViewController.this.mFingerprintPositions[1] + FingerViewController.this.mFingerprintPositions[3]) / 2;
                        FingerViewController.this.mFingerprintCircleOverlay.create(FingerViewController.this.mFingerprintCenterX, FingerViewController.this.mFingerprintCenterY, scale);
                    }
                    FingerViewController.this.mFingerprintCircleOverlay.setLayer(FingerViewController.CIRCLE_LAYER);
                    FingerViewController.this.mFingerprintCircleOverlay.show();
                    FingerViewController.this.getBrightness();
                    if (!FingerViewController.this.mFingerprintMaskOverlay.isCreate()) {
                        Log.i(FingerViewController.TAG, "mask not created, create it before show");
                        FingerViewController.this.mFingerprintMaskOverlay.create(FingerViewController.this.mCurrentWidth, FingerViewController.this.mCurrentHeight, 0);
                    }
                    FingerViewController.this.mCurrentAlpha = FingerViewController.this.getMaskAlpha(FingerViewController.this.mCurrentBrightness);
                    FingerViewController.this.setBacklightViaDisplayEngine(1, (float) ((int) FingerViewController.this.mMaxDigitalBrigtness), FingerViewController.this.transformBrightnessViaScreen(FingerViewController.this.mCurrentBrightness));
                    FingerViewController.this.mFingerprintMaskOverlay.setLayer(FingerViewController.MASK_LAYER);
                    FingerViewController.this.mFingerprintMaskOverlay.setAlpha(((float) FingerViewController.this.mCurrentAlpha) / FingerViewController.MAX_BRIGHTNESS_LEVEL);
                    FingerViewController.this.mFingerprintMaskOverlay.show();
                    SurfaceControl.closeTransaction();
                    FingerViewController.this.showFingerAnimViewByTheme();
                } catch (Throwable th) {
                    SurfaceControl.closeTransaction();
                    throw th;
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showFingerAnimViewByTheme() {
        if (this.mFingerprintAnimByThemeView == null || this.mAnimFileNames.isEmpty()) {
            Log.i(TAG, "come in mFingerprintAnimationView");
            if (this.mFingerprintAnimationView == null) {
                this.mFingerprintAnimationView = new FingerprintAnimationView(this.mContext);
            }
            this.mFingerprintAnimationView.setCenterPoints(this.mFingerprintCenterX, this.mFingerprintCenterY);
            this.mFingerprintAnimationView.setScale(getPxScale());
            if (!this.mFingerprintAnimationView.isAdded()) {
                this.mFingerprintAnimationView.setAddState(true);
                WindowManager windowManager = this.mWindowManager;
                FingerprintAnimationView fingerprintAnimationView = this.mFingerprintAnimationView;
                windowManager.addView(fingerprintAnimationView, fingerprintAnimationView.getViewParams());
            }
        } else {
            this.mFingerprintAnimByThemeView.setCenterPoints(this.mFingerprintCenterX, this.mFingerprintCenterY);
            this.mFingerprintAnimByThemeView.setScale(getPxScale());
            this.mFingerprintAnimByThemeView.post(new Runnable() {
                /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass31 */

                @Override // java.lang.Runnable
                public void run() {
                    FingerViewController.this.mFingerprintAnimByThemeView.setAnimationPosition();
                }
            });
            if (!this.mFingerprintAnimByThemeView.isAdded()) {
                Log.i(TAG, "come in mFingerprintAnimByThemeView");
                WindowManager windowManager2 = this.mWindowManager;
                FingerprintAnimByThemeView fingerprintAnimByThemeView = this.mFingerprintAnimByThemeView;
                windowManager2.addView(fingerprintAnimByThemeView, fingerprintAnimByThemeView.getFingerViewParams());
                this.mFingerprintAnimByThemeView.setAddState(true);
            }
        }
        Log.i(TAG_THREAD, "finished showHighlightCircleOnKeyguard");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createMaskAndCircleOnKeyguard() {
        this.mFingerprintMaskOverlay.create(this.mCurrentWidth, this.mCurrentHeight, 0);
        int[] iArr = this.mFingerprintPositions;
        this.mFingerprintCenterX = (iArr[0] + iArr[2]) / 2;
        this.mFingerprintCenterY = (iArr[1] + iArr[3]) / 2;
        this.mFingerprintCircleOverlay.create(this.mFingerprintCenterX, this.mFingerprintCenterY, getPxScale());
    }

    public void removeHighlightview(int type) {
        Log.i(TAG, "removeHighlightview mHighLightRemoveType:" + type);
        this.mHighLightRemoveType = type;
        if (this.mIshighLightViewAdded) {
            this.mHandler.removeCallbacks(this.mRemoveHighLightView);
        }
        this.mHandler.removeCallbacks(this.mHighLightViewRunnable);
        if (type == 0) {
            this.mHandler.post(new Runnable() {
                /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass32 */

                @Override // java.lang.Runnable
                public void run() {
                    Log.i(FingerViewController.TAG, "begin anonymous runnable in removeHighlightview");
                    FingerViewController.this.mHandler.postDelayed(FingerViewController.this.mRemoveFingerHighLightView, 110);
                    if (FingerViewController.this.mHighLightView != null) {
                        FingerViewController fingerViewController = FingerViewController.this;
                        fingerViewController.startAlphaValueAnimation(fingerViewController.mHighLightView, false, FingerViewController.this.mHighLightView.getAlpha() / FingerViewController.MAX_BRIGHTNESS_LEVEL, 0.0f, 110, 0);
                    }
                }
            });
        } else {
            this.mHandler.post(this.mRemoveHighLightView);
        }
    }

    private void initFingerPrintViewSubContentDes() {
        ImageView imageView = this.mFingerprintView;
        if (imageView != null) {
            imageView.setContentDescription(this.mContext.getString(33686273));
        }
        RelativeLayout relativeLayout = this.mCancelView;
        if (relativeLayout != null) {
            relativeLayout.setContentDescription(this.mContext.getString(33686274));
        }
    }

    private void initAddButtonViwSubContentDes() {
        SuspensionButton suspensionButton = this.mButtonView;
        if (suspensionButton != null) {
            suspensionButton.setContentDescription(this.mContext.getString(33686275));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createFingerprintView() {
        Log.i(TAG_THREAD, "fingerviewadded,mWidgetColor = " + this.mWidgetColor + ", addView:" + this.mIsFingerprintViewAdded);
        initBaseElement();
        updateBaseElementMargins();
        updateExtraElement();
        initFingerprintViewParams();
        this.mFingerViewParams.type = this.mWindowType;
        initFingerPrintViewSubContentDes();
        if (!this.mIsFingerprintViewAdded) {
            startBlurScreenshot();
            this.mWidgetColor = HwColorPicker.processBitmap(this.mScreenShot).getWidgetColor();
            this.mIsWidgetColorSet = true;
            this.mWindowManager.addView(this.mFingerView, this.mFingerViewParams);
            this.mIsFingerprintViewAdded = true;
            FingerprintController.getInstance().setFingerViewRemoveRunnable(this.mRemoveFingerViewRunnable);
            FingerprintController.getInstance().setFingerViewRemoveFlag(false);
            exitSingleHandMode();
            registerSingerHandObserver();
            getNotchState();
            transferNotchRoundCorner(0);
        }
    }

    public boolean getFingerPrintRealHeightScale() {
        int[] iArr = this.mFingerprintPositions;
        int fingerprintPositionHight = (iArr[1] + iArr[3]) / 2;
        int i = this.mDefaultDisplayHeight;
        if (i != 0) {
            sFingerPositionHeightScale = (((float) fingerprintPositionHight) * 1.0f) / ((float) i);
        }
        Log.i(TAG, "isNewMagazineViewForDownFP,sFingerPositionHeightScale:" + sFingerPositionHeightScale);
        return true;
    }

    public boolean isNewMagazineViewForDownFP() {
        if (sFingerPositionHeightScale == 0.0f) {
            getFingerPrintRealHeightScale();
        }
        return sFingerPositionHeightScale > LV_FINGERPRINT_POSITION_VIEW_HIGHT_SCALE;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void backFingerprintUsePasswordViewOnclick() {
        onUsePasswordClick();
        String foregroundPkgName = getForegroundPkgName();
        Log.i(TAG, "onClick UsePwd, foregroundPkgName = " + foregroundPkgName);
        if (foregroundPkgName == null || !isBroadcastNeed(foregroundPkgName)) {
            IBiometricServiceReceiver iBiometricServiceReceiver = this.mDialogReceiver;
            if (iBiometricServiceReceiver != null) {
                try {
                    iBiometricServiceReceiver.onDialogDismissed(1);
                } catch (RemoteException e) {
                    Log.w(TAG, "catch exception mDialogReceiver");
                }
            } else {
                IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mReceiver;
                if (iFingerprintServiceReceiver != null) {
                    try {
                        iFingerprintServiceReceiver.onError(0, 10, 0);
                    } catch (RemoteException e2) {
                        Log.w(TAG, "catch exception mReceiver");
                    }
                } else {
                    IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal = this.mBiometricServiceReceiver;
                    if (iBiometricServiceReceiverInternal != null) {
                        try {
                            iBiometricServiceReceiverInternal.onDialogDismissed(1);
                        } catch (RemoteException e3) {
                            Log.w(TAG, "catch exception mBiometricServiceReceiver");
                        }
                    } else {
                        Log.v(TAG, "do nothing");
                    }
                }
            }
        } else {
            Intent usePasswordIntent = new Intent(FINGERPRINT_IN_DISPLAY);
            usePasswordIntent.setPackage(foregroundPkgName);
            usePasswordIntent.putExtra(FINGERPRINT_IN_DISPLAY_HELPCODE_KEY, 1010);
            usePasswordIntent.putExtra(FINGERPRINT_IN_DISPLAY_HELPSTRING_KEY, FINGERPRINT_IN_DISPLAY_HELPSTRING_USE_PASSWORD_VALUE);
            this.mContext.sendBroadcast(usePasswordIntent);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelHotSpotViewOnclick() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        this.mHandler.post(this.mRemoveImageOnlyRunnable);
        sendKeyEvent();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void backFingerprintCancelViewOnclick() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        onCancelClick();
        Context context = this.mContext;
        Flog.bdReport(context, 502, "{PkgName:" + this.mPkgName + "}");
        String foregroundPkgName = getForegroundPkgName();
        if (foregroundPkgName == null || !isBroadcastNeed(foregroundPkgName)) {
            IBiometricServiceReceiver iBiometricServiceReceiver = this.mDialogReceiver;
            if (iBiometricServiceReceiver != null) {
                try {
                    iBiometricServiceReceiver.onDialogDismissed(2);
                } catch (RemoteException e) {
                    Log.w(TAG, "catch exception");
                }
            } else {
                IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mReceiver;
                if (iFingerprintServiceReceiver != null) {
                    try {
                        iFingerprintServiceReceiver.onError(0, 5, 11);
                    } catch (RemoteException e2) {
                        Log.w(TAG, "catch exception");
                    }
                } else {
                    IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal = this.mBiometricServiceReceiver;
                    if (iBiometricServiceReceiverInternal != null) {
                        try {
                            iBiometricServiceReceiverInternal.onDialogDismissed(2);
                        } catch (RemoteException e3) {
                            Log.w(TAG, "catch exception backFingerprintCancelViewOnclick");
                        }
                    } else {
                        Log.v(TAG, "do nothing");
                    }
                }
            }
        } else {
            Intent cancelMaskIntent = new Intent(FINGERPRINT_IN_DISPLAY);
            cancelMaskIntent.putExtra(FINGERPRINT_IN_DISPLAY_HELPCODE_KEY, 1011);
            cancelMaskIntent.putExtra(FINGERPRINT_IN_DISPLAY_HELPSTRING_KEY, FINGERPRINT_IN_DISPLAY_HELPSTRING_CLOSE_VIEW_VALUE);
            cancelMaskIntent.setPackage(foregroundPkgName);
            this.mContext.sendBroadcast(cancelMaskIntent);
        }
    }

    private void initBaseElement() {
        int curRotation = this.mWindowManager.getDefaultDisplay().getRotation();
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
        int dpi = SystemProperties.getInt("persist.sys.dpi", lcdDpi);
        if (this.mFingerView != null && this.mCurrentHeight == this.mSavedMaskHeight && curRotation == this.mSavedRotation && dpi == this.mSavedMaskDpi) {
            Log.i(TAG, "don't need to inflate mFingerView again");
            return;
        }
        this.mSavedMaskDpi = dpi;
        this.mSavedMaskHeight = this.mCurrentHeight;
        this.mSavedRotation = curRotation;
        if (isNewMagazineViewForDownFP()) {
            this.mFingerView = (FingerprintView) this.mLayoutInflater.inflate(34013414, (ViewGroup) null);
            Log.i(TAG, " add inflate mLVFingerView!!");
        } else {
            this.mFingerView = (FingerprintView) this.mLayoutInflater.inflate(34013346, (ViewGroup) null);
        }
        this.mFingerView.setCallback(new FingerprintViewCallback());
        this.mFingerprintView = (ImageView) this.mFingerView.findViewById(34603227);
        FingerprintController.getInstance().setFingerViewHoverListener(this.mFingerprintView, this.mContext);
        this.mRemoteView = (RelativeLayout) this.mFingerView.findViewById(34603465);
        if (isNewMagazineViewForDownFP()) {
            float dpiScale = (((float) lcdDpi) * 1.0f) / ((float) dpi);
            this.mLvBackFingerprintUsePasswordView = (Button) this.mFingerView.findViewById(34603396);
            this.mLvBackFingerprintCancelView = (Button) this.mFingerView.findViewById(34603395);
            RelativeLayout buttonLayout = (RelativeLayout) this.mFingerView.findViewById(34603394);
            if (buttonLayout != null) {
                setButtonLayout(buttonLayout, dpiScale);
            }
            if (this.mLvBackFingerprintUsePasswordView != null) {
                setLvBackFpUsePwdView(dpiScale);
            }
            if (this.mLvBackFingerprintCancelView != null) {
                setLvBackFpCancelView(dpiScale);
                return;
            }
            return;
        }
        this.mCancelView = (RelativeLayout) this.mFingerView.findViewById(34603040);
        this.mCancelView.setOnClickListener(new View.OnClickListener() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass33 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                FingerViewController.this.backFingerprintCancelViewOnclick();
            }
        });
    }

    private void setButtonLayout(RelativeLayout buttonLayout, float dpiScale) {
        ViewGroup.LayoutParams buttonLayoutParams = buttonLayout.getLayoutParams();
        if (buttonLayoutParams instanceof RelativeLayout.LayoutParams) {
            buttonLayoutParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472727)) * dpiScale) + 0.5f);
            buttonLayoutParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472726)) * dpiScale) + 0.5f);
            ((RelativeLayout.LayoutParams) buttonLayoutParams).bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472725)) * dpiScale) + 0.5f);
            buttonLayout.setLayoutParams(buttonLayoutParams);
        }
    }

    private void setLvBackFpUsePwdView(float dpiScale) {
        this.mLvBackFingerprintUsePasswordView.setTextSize(0, (float) ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472730)) * dpiScale) + 0.5f)));
        ViewGroup.LayoutParams usePasswordViewParams = this.mLvBackFingerprintUsePasswordView.getLayoutParams();
        if (usePasswordViewParams instanceof RelativeLayout.LayoutParams) {
            usePasswordViewParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472119)) * dpiScale) + 0.5f);
            usePasswordViewParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472112)) * dpiScale) + 0.5f);
            getCurrentRotation();
            int i = this.mCurrentRotation;
            if (i == 1 || i == 3) {
                ((RelativeLayout.LayoutParams) usePasswordViewParams).topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472728)) * dpiScale) + 0.5f);
            }
            this.mAppParamsUsePassworView = new RelativeLayout.LayoutParams((RelativeLayout.LayoutParams) usePasswordViewParams);
            this.mLvBackFingerprintUsePasswordView.setLayoutParams(usePasswordViewParams);
            Log.i(TAG, "zc initBaseElement usePasswordViewParams " + usePasswordViewParams.width + " " + usePasswordViewParams.height);
        }
        this.mLvBackFingerprintUsePasswordView.setOnClickListener(new View.OnClickListener() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass34 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                FingerViewController.this.backFingerprintUsePasswordViewOnclick();
            }
        });
    }

    private void setLvBackFpCancelView(float dpiScale) {
        this.mLvBackFingerprintCancelView.setTextSize(0, (float) ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472730)) * dpiScale) + 0.5f)));
        ViewGroup.LayoutParams cancelViewParams = this.mLvBackFingerprintCancelView.getLayoutParams();
        if (cancelViewParams instanceof RelativeLayout.LayoutParams) {
            cancelViewParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472119)) * dpiScale) + 0.5f);
            cancelViewParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472112)) * dpiScale) + 0.5f);
            getCurrentRotation();
            int i = this.mCurrentRotation;
            if (i == 1 || i == 3) {
                ((RelativeLayout.LayoutParams) cancelViewParams).topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472732)) * dpiScale) + 0.5f);
            }
            this.mAppParamsCancelView = new RelativeLayout.LayoutParams((RelativeLayout.LayoutParams) cancelViewParams);
            this.mCancelButtonMarginStart = ((RelativeLayout.LayoutParams) cancelViewParams).getMarginStart();
            this.mCancelButtonMarginEnd = ((RelativeLayout.LayoutParams) cancelViewParams).getMarginEnd();
            this.mLvBackFingerprintCancelView.setLayoutParams(cancelViewParams);
        }
        this.mLvBackFingerprintCancelView.setOnClickListener(new View.OnClickListener() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass35 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                FingerViewController.this.backFingerprintCancelViewOnclick();
            }
        });
    }

    private void updateBaseElementMargins() {
        getCurrentRotation();
        int[] fingerprintMargins = calculateFingerprintMargin();
        RelativeLayout relativeLayout = (RelativeLayout) this.mFingerView.findViewById(34603024);
        ViewGroup.MarginLayoutParams fingerviewLayoutParams = (ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams();
        float dpiScale = getDpiScale();
        float scale = getPxScale();
        if (isNewMagazineViewForDownFP()) {
            rotationNewMagazineView(fingerviewLayoutParams, fingerprintMargins, relativeLayout, dpiScale, scale);
        } else {
            rotationNotNewMagazineView(fingerviewLayoutParams, fingerprintMargins, relativeLayout, dpiScale);
        }
        ViewGroup.MarginLayoutParams fingerprintImageParams = (ViewGroup.MarginLayoutParams) this.mFingerprintView.getLayoutParams();
        int i = this.mFingerLogoRadius;
        fingerprintImageParams.width = (int) (((float) i) * scale * 2.0f);
        fingerprintImageParams.height = (int) (((float) i) * scale * 2.0f);
        fingerprintImageParams.leftMargin = fingerprintMargins[0];
        fingerprintImageParams.topMargin = fingerprintMargins[1];
        Log.i(TAG, "fingerprintViewParams.width = " + fingerprintImageParams.width + ", fingerprintViewParams.height = " + fingerprintImageParams.height);
        this.mFingerprintView.setLayoutParams(fingerprintImageParams);
        if (!isNewMagazineViewForDownFP()) {
            cancelButtonsPosition(dpiScale);
        }
    }

    private void rotationNewMagazineView(ViewGroup.MarginLayoutParams fingerviewLayoutParams, int[] fingerprintMargins, RelativeLayout relativeLayout, float dpiScale, float scale) {
        Log.i(TAG, "fingerviewLayoutParams.width = " + fingerviewLayoutParams.width + ",fingerviewLayoutParams.");
        ViewGroup.MarginLayoutParams fingerprintImageParams = (ViewGroup.MarginLayoutParams) this.mFingerprintView.getLayoutParams();
        int i = this.mFingerLogoRadius;
        fingerprintImageParams.width = (int) (((float) i) * scale * 2.0f);
        fingerprintImageParams.height = (int) (((float) i) * scale * 2.0f);
        fingerprintImageParams.leftMargin = fingerprintMargins[0];
        fingerprintImageParams.topMargin = fingerprintMargins[1];
        Log.i(TAG, "zc fingerprintViewParams.width = " + fingerprintImageParams.width + ", fingerprintViewParams.height = " + fingerprintImageParams.height);
        this.mFingerprintView.setLayoutParams(fingerprintImageParams);
        int i2 = this.mCurrentRotation;
        if (i2 == 1 || i2 == 3) {
            fingerviewLayoutParams.topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472724)) * dpiScale) + 0.5f);
            fingerviewLayoutParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472737)) * dpiScale) + 0.5f);
            relativeLayout.setLayoutParams(fingerviewLayoutParams);
            ViewGroup.MarginLayoutParams remoteViewLayoutParams = (ViewGroup.MarginLayoutParams) this.mRemoteView.getLayoutParams();
            remoteViewLayoutParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472737)) * dpiScale) + 0.5f);
            remoteViewLayoutParams.leftMargin = calculateRemoteViewLeftMargin(fingerprintImageParams.width);
            remoteViewLayoutParams.topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472766)) * dpiScale) + 0.5f);
            remoteViewLayoutParams.rightMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472765)) * dpiScale) + 0.5f);
            remoteViewLayoutParams.bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472763)) * dpiScale) + 0.5f);
            this.mRemoteView.setLayoutParams(remoteViewLayoutParams);
        }
    }

    private void rotationNotNewMagazineView(ViewGroup.MarginLayoutParams fingerviewLayoutParams, int[] fingerprintMargins, RelativeLayout relativeLayout, float dpiScale) {
        int i = this.mCurrentRotation;
        if (i == 1 || i == 3) {
            fingerviewLayoutParams.width = (fingerprintMargins[0] * 2) + ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472518)) * dpiScale) + 0.5f));
            int[] layoutMargins = calculateFingerprintLayoutLeftMargin(fingerviewLayoutParams.width);
            fingerviewLayoutParams.leftMargin = layoutMargins[0];
            fingerviewLayoutParams.rightMargin = layoutMargins[1];
            relativeLayout.setLayoutParams(fingerviewLayoutParams);
            ViewGroup.MarginLayoutParams remoteViewLayoutParams = (ViewGroup.MarginLayoutParams) this.mRemoteView.getLayoutParams();
            int remoteViewLeftLayoutmargin = calculateRemoteViewLeftMargin(fingerviewLayoutParams.width);
            remoteViewLayoutParams.width = (int) ((((((float) this.mCurrentHeight) - (((float) (this.mContext.getResources().getDimensionPixelSize(34472764) * 2)) * dpiScale)) - (((float) this.mContext.getResources().getDimensionPixelSize(34472740)) * dpiScale)) - ((float) fingerviewLayoutParams.width)) + 0.5f);
            remoteViewLayoutParams.leftMargin = remoteViewLeftLayoutmargin;
            remoteViewLayoutParams.topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472766)) * dpiScale) + 0.5f);
            remoteViewLayoutParams.rightMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472765)) * dpiScale) + 0.5f);
            remoteViewLayoutParams.bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472763)) * dpiScale) + 0.5f);
            this.mRemoteView.setLayoutParams(remoteViewLayoutParams);
            Log.i(TAG, " RemoteviewLayoutParams.leftMargin =" + remoteViewLayoutParams.leftMargin + ",rightMargin =" + remoteViewLayoutParams.rightMargin);
            return;
        }
        fingerviewLayoutParams.width = this.mCurrentWidth;
        fingerviewLayoutParams.leftMargin = 0;
    }

    private void cancelButtonsPosition(float dpiScale) {
        StringBuilder sb = new StringBuilder();
        sb.append("updateBaseElementMargins mFingerprintCenterX = ");
        int[] iArr = this.mFingerprintPositions;
        sb.append((iArr[1] + iArr[3]) / 2);
        Log.i(TAG, sb.toString());
        int[] iArr2 = this.mFingerprintPositions;
        int fingerprintPosition = (iArr2[1] + iArr2[3]) / 2;
        int cancelButtonsize = this.mContext.getResources().getDimensionPixelSize(34472280);
        ViewGroup.MarginLayoutParams cancelViewParams = (ViewGroup.MarginLayoutParams) this.mCancelView.getLayoutParams();
        if (fingerprintPosition == FINGERPRINT_IN_DISPLAY_POSITION_VIEW_VALUE_ELLE || fingerprintPosition == FINGERPRINT_IN_DISPLAY_POSITION_VIEW_VALUE_VOGUE) {
            cancelButtonsize = this.mContext.getResources().getDimensionPixelSize(34472281);
        }
        int i = this.mCurrentRotation;
        if (i == 1 || i == 3) {
            cancelViewParams.topMargin = (int) (((((float) this.mCurrentWidth) - (((float) cancelButtonsize) * dpiScale)) - (((float) this.mContext.getResources().getDimensionPixelSize(34472828)) * dpiScale)) + 0.5f);
        } else {
            cancelViewParams.topMargin = (int) (((((float) this.mCurrentHeight) - (((float) cancelButtonsize) * dpiScale)) - (((float) this.mContext.getResources().getDimensionPixelSize(34472828)) * dpiScale)) + 0.5f);
        }
        ImageView cancelImage = (ImageView) this.mCancelView.findViewById(34603038);
        ViewGroup.MarginLayoutParams cancelImageParams = (ViewGroup.MarginLayoutParams) cancelImage.getLayoutParams();
        cancelImageParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472827)) * dpiScale) + 0.5f);
        cancelImageParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472827)) * dpiScale) + 0.5f);
        cancelImage.setLayoutParams(cancelImageParams);
        Log.i(TAG, "cancelViewParams.topMargin = " + cancelViewParams.topMargin);
        cancelViewParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472515)) * dpiScale) + 0.5f);
        cancelViewParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472514)) * dpiScale) + 0.5f);
        this.mCancelView.setLayoutParams(cancelViewParams);
    }

    private void updateUsePasswordAndCancelView(float dpiScale) {
        RelativeLayout.LayoutParams layoutParams = this.mAppParamsCancelView;
        if (layoutParams != null) {
            layoutParams.addRule(16, 34603226);
            this.mAppParamsCancelView.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472733)) * dpiScale) + 0.5f);
            int i = this.mCancelButtonMarginStart;
            if (i != 0) {
                this.mAppParamsCancelView.setMarginStart(i);
            }
            int i2 = this.mCancelButtonMarginEnd;
            if (i2 != 0) {
                this.mAppParamsCancelView.setMarginEnd(i2);
            }
            this.mLvBackFingerprintCancelView.setLayoutParams(this.mAppParamsCancelView);
        }
        RelativeLayout.LayoutParams layoutParams2 = this.mAppParamsUsePassworView;
        if (layoutParams2 != null) {
            layoutParams2.addRule(17, 34603226);
            this.mAppParamsUsePassworView.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472733)) * dpiScale) + 0.5f);
            this.mLvBackFingerprintUsePasswordView.setLayoutParams(this.mAppParamsUsePassworView);
        }
    }

    private void updateExtraElement() {
        float dpiScale = getDpiScale();
        RelativeLayout usePasswordHotSpot = (RelativeLayout) this.mFingerView.findViewById(34603541);
        RelativeLayout usePasswordHotspotLayout = (RelativeLayout) this.mFingerView.findViewById(34603541);
        TextView fingerprintCancel = (TextView) this.mFingerView.findViewById(34603023);
        setTitleAndSummaryView(dpiScale);
        TextView appNameView = (TextView) this.mFingerView.findViewById(34603025);
        appNameView.setTextSize(0, (float) ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472084)) * dpiScale) + 0.5f)));
        TextView accountMessageView = (TextView) this.mFingerView.findViewById(34603008);
        accountMessageView.setTextSize(0, (float) ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34471948)) * dpiScale) + 0.5f)));
        ViewGroup.MarginLayoutParams accountMessageViewParams = (ViewGroup.MarginLayoutParams) accountMessageView.getLayoutParams();
        accountMessageViewParams.topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34471949)) * dpiScale) + 0.5f);
        accountMessageView.setLayoutParams(accountMessageViewParams);
        TextView usePasswordView = (TextView) this.mFingerView.findViewById(34603027);
        if (isNewMagazineViewForDownFP()) {
            Button button = this.mLvBackFingerprintUsePasswordView;
            if (button != null) {
                button.setText(this.mContext.getString(33685688));
            }
            Button button2 = this.mLvBackFingerprintCancelView;
            if (button2 != null) {
                button2.setText(this.mContext.getString(33685796));
            }
        } else {
            setUsePasswordHotSpotParams(dpiScale, usePasswordHotSpot, usePasswordHotspotLayout, usePasswordView, fingerprintCancel);
        }
        setHintView(dpiScale, usePasswordHotSpot);
        if (this.mPkgAttributes != null) {
            Log.i(TAG, "mPkgAttributes =" + this.mPkgAttributes);
            updateUsePasswordAndCancelView(dpiScale);
            if (this.mPkgAttributes.getString("googleFlag") == null) {
                handlePkgAttrNotGoogleFlag(dpiScale, usePasswordHotSpot, appNameView, accountMessageView);
            } else {
                handlePkgAttrGoogleFlag(dpiScale, appNameView, accountMessageView, usePasswordHotSpot, fingerprintCancel);
            }
        } else {
            setViewVisibility(appNameView, accountMessageView, usePasswordHotSpot, fingerprintCancel, dpiScale);
        }
    }

    private void setTitleAndSummaryView(float dpiScale) {
        int i;
        RelativeLayout titleAndSummaryView = (RelativeLayout) this.mFingerView.findViewById(34603026);
        ViewGroup.MarginLayoutParams titleAndSummaryViewParams = (ViewGroup.MarginLayoutParams) titleAndSummaryView.getLayoutParams();
        if (isNewMagazineViewForDownFP() && ((i = this.mCurrentRotation) == 1 || i == 3)) {
            titleAndSummaryViewParams.topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472085)) * dpiScale) + 0.5f);
        }
        titleAndSummaryViewParams.bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472085)) * dpiScale) + 0.5f);
        titleAndSummaryView.setLayoutParams(titleAndSummaryViewParams);
    }

    private void setUsePasswordHotSpotParams(float dpiScale, RelativeLayout usePasswordHotSpot, RelativeLayout usePasswordHotspotLayout, TextView usePasswordView, TextView fingerprintCancel) {
        ViewGroup.MarginLayoutParams usePasswordHotSpotParams = (ViewGroup.MarginLayoutParams) usePasswordHotSpot.getLayoutParams();
        usePasswordHotSpotParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472867)) * dpiScale) + 0.5f);
        usePasswordHotSpotParams.bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472866)) * dpiScale) + 0.5f);
        usePasswordHotSpot.setLayoutParams(usePasswordHotSpotParams);
        usePasswordView.setText(this.mContext.getString(33685688));
        usePasswordView.setTextSize(0, (float) ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472868)) * dpiScale) + 0.5f)));
        if (usePasswordHotspotLayout != null) {
            usePasswordHotspotLayout.setContentDescription(this.mContext.getString(33685688));
        }
        if (this.mIsBiometricPrompt && fingerprintCancel != null) {
            ViewGroup.MarginLayoutParams fingerpirntCancelHotSpotParams = (ViewGroup.MarginLayoutParams) fingerprintCancel.getLayoutParams();
            fingerpirntCancelHotSpotParams.bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472866)) * dpiScale) + 0.5f);
            fingerprintCancel.setLayoutParams(fingerpirntCancelHotSpotParams);
            fingerprintCancel.setText(this.mContext.getString(33685796));
            fingerprintCancel.setTextSize(0, (float) ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472868)) * dpiScale) + 0.5f)));
            fingerprintCancel.setContentDescription(this.mContext.getString(33685796));
        }
    }

    private void setHintView(float dpiScale, RelativeLayout usePasswordHotSpot) {
        this.mHintView = (HintText) this.mFingerView.findViewById(34603228);
        resetFrozenCountDownIfNeed();
        if (this.mIsFingerFrozen) {
            HintText hintText = this.mHintView;
            Resources resources = this.mContext.getResources();
            int i = this.mRemainedSecs;
            hintText.setText(resources.getQuantityString(34406411, i, Integer.valueOf(i)));
        } else if (this.mIsHasBackFingerprint) {
            this.mHintView.setText(this.mContext.getString(33685679));
        } else {
            this.mHintView.setText(this.mContext.getString(33685692));
        }
        this.mHintView.setTextSize(0, (float) ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472560)) * dpiScale) + 0.5f)));
        if (!isNewMagazineViewForDownFP()) {
            ViewGroup.MarginLayoutParams hintViewParams = (ViewGroup.MarginLayoutParams) this.mHintView.getLayoutParams();
            hintViewParams.bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472559)) * dpiScale) + 0.5f);
            this.mHintView.setLayoutParams(hintViewParams);
            usePasswordHotSpot.setOnClickListener(new View.OnClickListener() {
                /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass36 */

                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    FingerViewController.this.backFingerprintUsePasswordViewOnclick();
                }
            });
            return;
        }
        ViewGroup.LayoutParams hintViewParams2 = this.mHintView.getLayoutParams();
        if (hintViewParams2 instanceof RelativeLayout.LayoutParams) {
            getCurrentRotation();
            int i2 = this.mCurrentRotation;
            if (i2 == 1 || i2 == 3) {
                ((RelativeLayout.LayoutParams) hintViewParams2).topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472736)) * dpiScale) + 0.5f);
            } else {
                ((RelativeLayout.LayoutParams) hintViewParams2).bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472735)) * dpiScale) + 0.5f);
            }
            this.mHintView.setLayoutParams(hintViewParams2);
        }
    }

    private void handlePkgAttrNotGoogleFlag(float dpiScale, RelativeLayout usePasswordHotSpot, TextView appNameView, TextView accountMessageView) {
        if (this.mPkgAttributes.getParcelable("CustView") != null) {
            this.mRemoteView.setVisibility(0);
            Log.i(TAG, "RemoteViews != null");
            RemoteViews newAppView = (RemoteViews) this.mPkgAttributes.getParcelable("CustView");
            if (newAppView != null) {
                this.mRemoteView.addView(newAppView.apply(this.mContext, this.mRemoteView));
            }
        } else {
            this.mRemoteView.setVisibility(4);
        }
        Log.i(TAG, "mPkgAttributes.getString= " + this.mPkgAttributes.getString("Title"));
        if (this.mPkgAttributes.getString("Title") != null) {
            appNameView.setVisibility(0);
            appNameView.setText(this.mPkgAttributes.getString("Title"));
        } else {
            appNameView.setVisibility(4);
        }
        if (this.mPkgAttributes.getString("Summary") != null) {
            accountMessageView.setVisibility(0);
            accountMessageView.setText(this.mPkgAttributes.getString("Summary"));
        } else {
            accountMessageView.setVisibility(8);
        }
        if (this.mPkgAttributes.getBoolean("UsePassword")) {
            if (!isNewMagazineViewForDownFP()) {
                usePasswordHotSpot.setVisibility(0);
            } else {
                this.mLvBackFingerprintUsePasswordView.setVisibility(0);
                ViewGroup.LayoutParams params = this.mLvBackFingerprintCancelView.getLayoutParams();
                if (params instanceof RelativeLayout.LayoutParams) {
                    params.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472733)) * dpiScale) + 0.5f);
                    this.mLvBackFingerprintCancelView.setLayoutParams(params);
                }
            }
        } else if (!isNewMagazineViewForDownFP()) {
            usePasswordHotSpot.setVisibility(4);
        } else {
            getCurrentRotation();
            updateButtomLayoutInNewFinger(dpiScale);
        }
        if (this.mPkgAttributes.getString("SystemTitle") != null) {
            Log.i(TAG, "attributestring =" + this.mPkgAttributes.getString("SystemTitle"));
            this.mHintView.setText(this.mPkgAttributes.getString("SystemTitle"));
        }
    }

    private void handlePkgAttrGoogleFlag(float dpiScale, TextView appNameView, TextView accountMessageView, RelativeLayout usePasswordHotSpot, TextView fingerprintCancel) {
        TextView usePasswordView = (TextView) this.mFingerView.findViewById(34603027);
        int i = 0;
        if (this.mPkgAttributes.getString("title") != null) {
            Log.i(TAG, "title =" + this.mPkgAttributes.getString("title"));
            appNameView.setVisibility(0);
            appNameView.setText(this.mPkgAttributes.getString("title"));
        }
        if (this.mPkgAttributes.getString("subtitle") != null) {
            accountMessageView.setVisibility(0);
            accountMessageView.setText(this.mPkgAttributes.getString("subtitle"));
        } else {
            accountMessageView.setVisibility(8);
        }
        if (this.mPkgAttributes.getString("description") != null) {
            Log.i(TAG, "description =" + this.mPkgAttributes.getString("description"));
            this.mHintView.setText(this.mPkgAttributes.getString("description"));
        }
        if (this.mPkgAttributes.getString("positive_text") != null) {
            Log.i(TAG, "positive_text =" + this.mPkgAttributes.getString("positive_text"));
            if (!isNewMagazineViewForDownFP()) {
                usePasswordHotSpot.setVisibility(0);
                usePasswordView.setText(this.mPkgAttributes.getString("positive_text"));
            } else {
                this.mLvBackFingerprintUsePasswordView.setVisibility(0);
                this.mLvBackFingerprintUsePasswordView.setText(this.mPkgAttributes.getString("positive_text"));
            }
        } else if (!isNewMagazineViewForDownFP()) {
            usePasswordHotSpot.setVisibility(4);
        } else if (this.mLvBackFingerprintUsePasswordView != null) {
            updateButtomLayoutInNewFinger(dpiScale);
        }
        if (this.mPkgAttributes.getString("negative_text") != null) {
            Log.i(TAG, "negative_text =" + this.mPkgAttributes.getString("negative_text"));
            Button button = this.mLvBackFingerprintCancelView;
            if (button != null) {
                button.setText(this.mPkgAttributes.getString("negative_text"));
            }
            if (fingerprintCancel != null) {
                if (!this.mIsBiometricPrompt) {
                    i = 8;
                }
                fingerprintCancel.setVisibility(i);
                fingerprintCancel.setText(this.mPkgAttributes.getString("negative_text"));
            }
        } else if (fingerprintCancel != null) {
            fingerprintCancel.setVisibility(8);
        }
    }

    private void setViewVisibility(TextView appNameView, TextView accountMessageView, RelativeLayout usePasswordHotSpot, TextView fingerprintCancel, float dpiScale) {
        this.mRemoteView.setVisibility(4);
        appNameView.setVisibility(4);
        accountMessageView.setVisibility(8);
        if (!isNewMagazineViewForDownFP()) {
            usePasswordHotSpot.setVisibility(4);
            if (fingerprintCancel != null) {
                fingerprintCancel.setVisibility(8);
                return;
            }
            return;
        }
        getCurrentRotation();
        updateButtomLayoutInNewFinger(dpiScale);
    }

    private void updateButtomLayoutInNewFinger(float dpiScale) {
        int i = this.mCurrentRotation;
        if (i == 1 || i == 3) {
            this.mLvBackFingerprintUsePasswordView.setVisibility(4);
        } else {
            this.mLvBackFingerprintUsePasswordView.setVisibility(8);
        }
        ViewGroup.LayoutParams params = this.mLvBackFingerprintCancelView.getLayoutParams();
        if (params instanceof RelativeLayout.LayoutParams) {
            ((RelativeLayout.LayoutParams) params).removeRule(16);
            ((RelativeLayout.LayoutParams) params).addRule(1);
            ((RelativeLayout.LayoutParams) params).width = -2;
            ((RelativeLayout.LayoutParams) params).setMarginStart(0);
            ((RelativeLayout.LayoutParams) params).setMarginEnd(0);
            this.mLvBackFingerprintCancelView.setLayoutParams(params);
        }
    }

    private void getCurrentRotation() {
        this.mCurrentRotation = this.mWindowManager.getDefaultDisplay().getRotation();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x005b  */
    /* JADX WARNING: Removed duplicated region for block: B:12:? A[RETURN, SYNTHETIC] */
    private void getCurrentFingerprintCenter() {
        this.mCurrentRotation = this.mWindowManager.getDefaultDisplay().getRotation();
        int i = this.mCurrentRotation;
        if (i != 0) {
            if (i == 1) {
                int[] iArr = this.mFingerprintPositions;
                this.mFingerprintCenterX = (iArr[1] + iArr[3]) / 2;
                this.mFingerprintCenterY = (iArr[0] + iArr[2]) / 2;
            } else if (i != 2) {
                if (i == 3) {
                    int i2 = this.mDefaultDisplayHeight;
                    int[] iArr2 = this.mFingerprintPositions;
                    this.mFingerprintCenterX = i2 - ((iArr2[1] + iArr2[3]) / 2);
                    this.mFingerprintCenterY = (iArr2[0] + iArr2[2]) / 2;
                }
            }
            if (this.mCurrentRotation != 1) {
                this.mFingerprintCenterX -= this.mNotchHeight;
                return;
            }
            return;
        }
        int[] iArr3 = this.mFingerprintPositions;
        this.mFingerprintCenterX = (iArr3[0] + iArr3[2]) / 2;
        this.mFingerprintCenterY = (iArr3[1] + iArr3[3]) / 2;
        if (this.mCurrentRotation != 1) {
        }
    }

    private void initFingerprintViewParams() {
        if (this.mFingerViewParams == null) {
            this.mFingerViewParams = new WindowManager.LayoutParams(-1, -1);
            WindowManager.LayoutParams layoutParams = this.mFingerViewParams;
            layoutParams.layoutInDisplayCutoutMode = 1;
            layoutParams.layoutInDisplaySideMode = 1;
            layoutParams.flags = 201852424;
            layoutParams.privateFlags |= 16;
            WindowManager.LayoutParams layoutParams2 = this.mFingerViewParams;
            layoutParams2.format = -3;
            layoutParams2.screenOrientation = 14;
            layoutParams2.setTitle(FINGRPRINT_VIEW_TITLE_NAME);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeFingerprintView() {
        Log.i(TAG, "removeFingerprintView start added = " + this.mIsFingerprintViewAdded);
        FingerprintView fingerprintView = this.mFingerView;
        if (fingerprintView != null && this.mIsFingerprintViewAdded && fingerprintView.isAttachedToWindow()) {
            this.mWindowManager.removeView(this.mFingerView);
            resetFingerprintView();
            if (this.mIsNotchConerStatusChanged) {
                transferNotchRoundCorner(1);
            }
            Log.i(TAG, "removeFingerprintView is done is View Added = " + this.mIsFingerprintViewAdded + "mFingerView =" + this.mFingerView);
        }
        if (this.mIsFingerprintViewAdded) {
            FingerprintController.getInstance().setFingerViewRemoveFlag(true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFingerprintView() {
        int i = this.mAuthenticateResult;
        if (i == 1) {
            if (this.mIsUseDefaultHint) {
                updateHintView();
            }
        } else if (i == 0) {
            if (this.mIsUseDefaultHint) {
                updateHintView(this.mContext.getString(33685684));
            }
            ImageView imageView = this.mFingerprintView;
            if (imageView != null) {
                imageView.setContentDescription(this.mContext.getString(33685684));
            }
            String foregroundPkg = getForegroundPkgName();
            String[] strArr = PACKAGES_USE_HWAUTH_INTERFACES;
            for (String pkgName : strArr) {
                if (pkgName.equals(foregroundPkg)) {
                    Log.i(TAG, "hw wallet Identifing,pkgName = " + pkgName);
                }
            }
            if (!this.mIsKeepMaskAfterAuth) {
                removeMaskOrButton();
            }
        } else if (i == 2) {
            handleFingerprintAuthUnchecked();
        } else if (i == 3) {
            ImageView imageView2 = this.mFingerprintView;
            if (imageView2 != null) {
                imageView2.setContentDescription(this.mContext.getString(33685685));
            }
            if (this.mIsUseDefaultHint) {
                updateHintView(this.mContext.getString(33685685));
            }
        }
    }

    private void handleFingerprintAuthUnchecked() {
        if (!this.mIsUseDefaultHint) {
            return;
        }
        if (this.mRemainTimes == 5) {
            if (this.mIsHasBackFingerprint) {
                updateHintView(this.mContext.getString(33685679));
            } else {
                updateHintView(this.mContext.getString(33685692));
            }
            ImageView imageView = this.mFingerprintView;
            if (imageView != null) {
                imageView.setContentDescription(this.mContext.getString(33686273));
                return;
            }
            return;
        }
        updateHintView();
    }

    private void updateHintView() {
        Log.i(TAG, "updateFingerprintView start,mIsFingerprintViewAdded = " + this.mIsFingerprintViewAdded);
        if (this.mIsFingerprintViewAdded && this.mHintView != null) {
            int i = this.mRemainTimes;
            if (i > 0 && i < 5) {
                Log.i(TAG, "remaind time = " + this.mRemainTimes);
                Resources resources = this.mContext.getResources();
                int i2 = this.mRemainTimes;
                String trymoreStr = resources.getQuantityString(34406412, i2, Integer.valueOf(i2));
                this.mHintView.setText(trymoreStr);
                ImageView imageView = this.mFingerprintView;
                if (imageView != null) {
                    imageView.setContentDescription(trymoreStr);
                }
            } else if (this.mRemainTimes == 0) {
                if (!this.mIsFingerFrozen) {
                    RemainTimeCountDown remainTimeCountDown = this.mMyCountDown;
                    if (remainTimeCountDown != null) {
                        remainTimeCountDown.cancel();
                    }
                    ImageView imageView2 = this.mFingerprintView;
                    if (imageView2 != null) {
                        imageView2.setContentDescription(this.mContext.getResources().getQuantityString(34406411, 30, 30));
                    }
                    this.mMyCountDown = new RemainTimeCountDown(HwArbitrationDEFS.DelayTimeMillisA, 1000);
                    this.mMyCountDown.start();
                    this.mIsFingerFrozen = true;
                } else {
                    return;
                }
            }
            this.mWindowManager.updateViewLayout(this.mFingerView, this.mFingerViewParams);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHintView(String hint) {
        HintText hintText = this.mHintView;
        if (hintText != null) {
            hintText.setText(hint);
        }
        if (this.mIsFingerprintViewAdded) {
            this.mWindowManager.updateViewLayout(this.mFingerView, this.mFingerViewParams);
        }
    }

    private void resetFingerprintView() {
        FingerprintController.getInstance().setFingerViewRemoveFlag(false);
        this.mIsFingerprintViewAdded = false;
        Bitmap bitmap = this.mBlurBitmap;
        if (bitmap != null) {
            bitmap.recycle();
            FingerprintView fingerprintView = this.mFingerView;
            if (fingerprintView != null) {
                fingerprintView.setBackgroundDrawable(null);
            }
        }
        Bitmap bitmap2 = this.mScreenShot;
        if (bitmap2 != null) {
            bitmap2.recycle();
            this.mScreenShot = null;
        }
        RelativeLayout relativeLayout = this.mRemoteView;
        if (relativeLayout != null) {
            relativeLayout.removeAllViews();
        }
        unregisterSingerHandObserver();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initFingerprintAnimView() {
        Log.i(TAG, "come in initFingerprintAnimView");
        this.mFingerprintAnimByThemeView = new FingerprintAnimByThemeView(this.mContext, this.mAnimFileNames, FingerprintAnimByThemeModel.getFpAnimFps(this.mContext));
        FingerprintAnimByThemeModel.setLoadAmount();
        Log.i(TAG, "initFingerprintAnimView mFingerprintAnimViewByTheme is" + this.mFingerprintAnimByThemeView);
    }

    private void sendKeyEvent() {
        int[] actions;
        for (int i : new int[]{0, 1}) {
            long curTime = SystemClock.uptimeMillis();
            InputManager.getInstance().injectInputEvent(new KeyEvent(curTime, curTime, i, 4, 0, 0, -1, 0, 8, LightsManagerEx.LIGHT_ID_SMARTBACKLIGHT), 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createImageOnlyView() {
        setCancelViewImageVisible();
        setImageDrawable();
        int[] fingerprintMargins = calculateFingerprintImageMargin();
        ViewGroup.MarginLayoutParams fingerprintImageParams = (ViewGroup.MarginLayoutParams) this.mFingerprintImageForAlipay.getLayoutParams();
        float dpiScale = getDpiScale();
        fingerprintImageParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472517)) * dpiScale) + 0.5f);
        fingerprintImageParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472516)) * dpiScale) + 0.5f);
        this.mFingerprintImageForAlipay.setLayoutParams(fingerprintImageParams);
        WindowManager.LayoutParams fingerprintOnlyLayoutParams = new WindowManager.LayoutParams();
        setImageOnlyLayoutParams(fingerprintOnlyLayoutParams, fingerprintMargins, dpiScale);
        Log.i(TAG, "fingerprintImage location = [" + fingerprintOnlyLayoutParams.x + "," + fingerprintOnlyLayoutParams.y + "]");
        if (!this.mIsFingerprintOnlyViewAdded) {
            this.mWindowManager.addView(this.mLayoutForAlipay, fingerprintOnlyLayoutParams);
            this.mIsFingerprintOnlyViewAdded = true;
            exitSingleHandMode();
            registerSingerHandObserver();
        }
    }

    private void setCancelViewImageVisible() {
        int curHeight = SystemPropertiesEx.getInt("persist.sys.rog.height", this.mDefaultDisplayHeight);
        int dpi = SystemProperties.getInt("persist.sys.dpi", SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0)));
        if (this.mLayoutForAlipay == null || curHeight != this.mSavedImageHeight || dpi != this.mSavedImageDpi) {
            this.mSavedImageHeight = curHeight;
            this.mSavedImageDpi = dpi;
            this.mLayoutForAlipay = (RelativeLayout) this.mLayoutInflater.inflate(34013345, (ViewGroup) null);
            this.mFingerprintImageForAlipay = (ImageView) this.mLayoutForAlipay.findViewById(34603225);
            this.mCancelViewImageOnly = (RelativeLayout) this.mLayoutForAlipay.findViewById(34603041);
            if (this.mIsCancelHotSpotPkgAdded) {
                this.mCancelViewImageOnly.setVisibility(0);
                this.mCancelViewImageOnly.setOnClickListener(new View.OnClickListener() {
                    /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass37 */

                    @Override // android.view.View.OnClickListener
                    public void onClick(View v) {
                        FingerViewController.this.cancelHotSpotViewOnclick();
                    }
                });
                return;
            }
            this.mCancelViewImageOnly.setVisibility(8);
        }
    }

    private void setImageDrawable() {
        if (!this.mIsHasUdFingerprint) {
            this.mFingerprintImageForAlipay.setImageResource(33751221);
        } else if (isNewMagazineViewForDownFP()) {
            this.mFingerprintImageForAlipay.setImageResource(33751385);
        } else {
            this.mAlipayDrawable = new BreathImageDrawable(this.mContext);
            this.mAlipayDrawable.setBreathImageDrawable(null, this.mContext.getDrawable(33751385));
            this.mFingerprintImageForAlipay.setImageDrawable(this.mAlipayDrawable);
            this.mFingerprintImageForAlipay.setOnTouchListener(new View.OnTouchListener() {
                /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass38 */

                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    if (action == 0) {
                        FingerViewController.this.mAlipayDrawable.startTouchDownBreathAnim();
                    } else if (action == 1) {
                        FingerViewController.this.mAlipayDrawable.startTouchUpBreathAnim();
                    }
                    return true;
                }
            });
            this.mAlipayDrawable.startBreathAnim();
        }
    }

    private void setImageOnlyLayoutParams(WindowManager.LayoutParams fingerprintOnlyLayoutParams, int[] fingerprintMargins, float dpiScale) {
        FingerprintViewUtils.setFingerprintOnlyLayoutParams(this.mContext, fingerprintOnlyLayoutParams, this.mWindowType, fingerprintMargins);
        if (this.mIsCancelHotSpotPkgAdded) {
            fingerprintOnlyLayoutParams.y = fingerprintMargins[1] - (((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472514)) * dpiScale) + 0.5f)) * 2);
            fingerprintOnlyLayoutParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472516)) * dpiScale) + (((float) this.mContext.getResources().getDimensionPixelSize(34472514)) * dpiScale * 2.0f) + 0.5f);
            ViewGroup.MarginLayoutParams cancelViewParams = (ViewGroup.MarginLayoutParams) this.mCancelViewImageOnly.getLayoutParams();
            cancelViewParams.bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34471941)) * dpiScale) + 0.5f);
            ImageView cancelImage = (ImageView) this.mCancelViewImageOnly.findViewById(34603039);
            ViewGroup.MarginLayoutParams cancelImageParams = (ViewGroup.MarginLayoutParams) cancelImage.getLayoutParams();
            cancelImageParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472731)) * dpiScale) + 0.5f);
            cancelImageParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472731)) * dpiScale) + 0.5f);
            cancelImage.setLayoutParams(cancelImageParams);
            cancelViewParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472515)) * dpiScale) + 0.5f);
            cancelViewParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472514)) * dpiScale) + 0.5f);
            this.mCancelViewImageOnly.setLayoutParams(cancelViewParams);
            return;
        }
        fingerprintOnlyLayoutParams.y = fingerprintMargins[1];
        fingerprintOnlyLayoutParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472516)) * dpiScale) + 0.5f);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateImageOnlyView() {
        if (this.mAuthenticateResult == 0) {
            this.mHandler.post(this.mRemoveImageOnlyRunnable);
            this.mFingerViewChangeCallback.onFingerViewStateChange(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeImageOnlyView() {
        RelativeLayout relativeLayout;
        if (this.mIsFingerprintOnlyViewAdded && (relativeLayout = this.mLayoutForAlipay) != null) {
            this.mWindowManager.removeView(relativeLayout);
            this.mIsFingerprintOnlyViewAdded = false;
            unregisterSingerHandObserver();
        }
    }

    private int slideOffsetDistance(float scale) {
        int offsetDistance;
        if (TextUtils.isEmpty(SLIDE_PROP)) {
            return 0;
        }
        String[] slideProps = SLIDE_PROP.split(",");
        int slideDistance = 0;
        if (slideProps.length == 4) {
            try {
                slideDistance = Integer.valueOf(slideProps[1]).intValue();
            } catch (NumberFormatException e) {
                Log.e(TAG, "slideOffsetDistance NumberFormatException format exception");
            }
        }
        if (slideDistance == 0) {
            return 0;
        }
        int offsetDistance2 = this.mDefaultDisplayWidth;
        if (offsetDistance2 > slideDistance) {
            offsetDistance = offsetDistance2 - slideDistance;
        } else {
            offsetDistance = slideDistance - offsetDistance2;
        }
        if (slideDistance == 0) {
            return 0;
        }
        int offsetDistance3 = (int) (((((float) offsetDistance) * scale) + 0.5f) / 2.0f);
        Log.i(TAG, "slideOffsetDistance offsetDistance = " + offsetDistance3);
        return offsetDistance3;
    }

    private int[] calculateFingerprintImageMargin() {
        Log.i(TAG, "left = " + this.mFingerprintPositions[0] + "right = " + this.mFingerprintPositions[2]);
        Log.i(TAG, "top = " + this.mFingerprintPositions[1] + "button = " + this.mFingerprintPositions[3]);
        float dpiScale = getDpiScale();
        int[] margins = new int[2];
        int fingerPrintInScreenWidth = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472517)) * dpiScale) + 0.5f);
        int fingerPrintInScreenHeight = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472516)) * dpiScale) + 0.5f);
        Log.i(TAG, "current height = " + this.mCurrentHeight + "mDefaultDisplayHeight = " + this.mDefaultDisplayHeight);
        float scale = ((float) this.mCurrentHeight) / ((float) this.mDefaultDisplayHeight);
        getCurrentRotation();
        int i = this.mCurrentRotation;
        if (i == 0 || i == 2) {
            int[] iArr = this.mFingerprintPositions;
            this.mFingerprintCenterX = (iArr[0] + iArr[2]) / 2;
            this.mFingerprintCenterY = (iArr[1] + iArr[3]) / 2;
            int marginLeft = ((int) ((((float) this.mFingerprintCenterX) * scale) + 0.5f)) - (fingerPrintInScreenHeight / 2);
            int marginTop = ((int) ((((float) this.mFingerprintCenterY) * scale) + 0.5f)) - (fingerPrintInScreenWidth / 2);
            margins[0] = marginLeft;
            margins[1] = marginTop;
            Log.i(TAG, "marginLeft = " + marginLeft + "marginTop = " + marginTop + "scale = " + scale);
        } else {
            int[] iArr2 = this.mFingerprintPositions;
            this.mFingerprintCenterX = (iArr2[1] + iArr2[3]) / 2;
            this.mFingerprintCenterY = (iArr2[0] + iArr2[2]) / 2;
            int marginTop2 = (int) (((((float) this.mFingerprintCenterY) * scale) - ((float) (fingerPrintInScreenHeight / 2))) + ((float) slideOffsetDistance(scale)) + 0.5f);
            int marginLeft2 = ((int) ((((float) this.mFingerprintCenterX) * scale) + 0.5f)) - (fingerPrintInScreenWidth / 2);
            if (this.mCurrentRotation == 3) {
                marginLeft2 = (this.mCurrentHeight - marginLeft2) - fingerPrintInScreenWidth;
            }
            margins[0] = marginLeft2;
            margins[1] = marginTop2;
        }
        return margins;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createAndAddButtonView() {
        int i;
        if (!"com.android.systemui".equals(this.mPkgName)) {
            Log.i(TAG, "SuspensionButton createAndAddButtonView not keyguard");
        } else if (this.mIsButtonViewAdded) {
            adjustButtonViewVisibility(this.mPkgName);
        } else {
            calculateButtonPosition();
            Log.i(TAG, "createAndAddButtonView,pkg = " + this.mPkgName);
            float dpiScale = getDpiScale();
            if (this.mButtonView == null || this.mCurrentHeight != this.mSavedButtonHeight) {
                this.mButtonView = (SuspensionButton) this.mLayoutInflater.inflate(34013344, (ViewGroup) null);
                this.mButtonView.setCallback(new SuspensionButtonCallback());
                this.mSavedButtonHeight = this.mCurrentHeight;
            }
            initAddButtonViwSubContentDes();
            this.mButtonView.setOnClickListener(new View.OnClickListener() {
                /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass39 */

                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    FingerViewController.this.mHandler.post(FingerViewController.this.mRemoveButtonViewRunnable);
                    FingerViewController.this.mHandler.post(FingerViewController.this.mAddFingerViewRunnable);
                    if (FingerViewController.this.mFingerViewChangeCallback != null) {
                        FingerViewController.this.mFingerViewChangeCallback.onFingerViewStateChange(1);
                    }
                    if (FingerViewController.this.mReceiver != null) {
                        try {
                            FingerViewController.this.mReceiver.onAcquired(0, 6, 12);
                        } catch (RemoteException e) {
                            Log.w(FingerViewController.TAG, "catch exception");
                        }
                    }
                    Context context = FingerViewController.this.mContext;
                    Flog.bdReport(context, 501, "{PkgName:" + FingerViewController.this.mPkgName + "}");
                }
            });
            ImageView buttonImage = (ImageView) this.mButtonView.findViewById(34603037);
            ViewGroup.MarginLayoutParams buttonImageParams = (ViewGroup.MarginLayoutParams) buttonImage.getLayoutParams();
            buttonImageParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472515)) * dpiScale) + 0.5f);
            buttonImageParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472514)) * dpiScale) + 0.5f);
            buttonImage.setLayoutParams(buttonImageParams);
            this.mSuspensionButtonParams = new WindowManager.LayoutParams();
            if ("com.android.systemui".equals(this.mPkgName)) {
                this.mSuspensionButtonParams.type = 2014;
            } else {
                this.mSuspensionButtonParams.type = 2003;
            }
            WindowManager.LayoutParams layoutParams = this.mSuspensionButtonParams;
            layoutParams.flags = 16777480;
            layoutParams.gravity = 8388659;
            layoutParams.x = (int) ((((float) this.mButtonCenterX) - ((((float) this.mContext.getResources().getDimensionPixelSize(34472515)) * dpiScale) / 2.0f)) + 0.5f);
            Log.i(TAG, "mSuspensionButtonParams.x=" + this.mSuspensionButtonParams.x);
            this.mSuspensionButtonParams.y = (int) ((((float) this.mButtonCenterY) - ((((float) this.mContext.getResources().getDimensionPixelSize(34472514)) * dpiScale) / 2.0f)) + 0.5f);
            Log.i(TAG, "mSuspensionButtonParams.y=" + this.mSuspensionButtonParams.y);
            this.mSuspensionButtonParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472515)) * dpiScale) + 0.5f);
            this.mSuspensionButtonParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472514)) * dpiScale) + 0.5f);
            WindowManager.LayoutParams layoutParams2 = this.mSuspensionButtonParams;
            layoutParams2.format = -3;
            layoutParams2.privateFlags |= 16;
            this.mSuspensionButtonParams.setTitle("fingerprintview_button");
            this.mButtonView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass40 */

                @Override // android.view.ViewTreeObserver.OnPreDrawListener
                public boolean onPreDraw() {
                    FingerViewController.this.mIsWidgetColorSet = false;
                    return true;
                }
            });
            if ("com.android.systemui".equals(this.mPkgName) && (i = this.mButtonColor) != 0) {
                this.mWidgetColor = i;
                this.mIsWidgetColorSet = true;
            }
            if (!this.mIsWidgetColorSet) {
                Log.i(TAG, "mIsWidgetColorSet is false, get a new screenshot and calculate color");
                getScreenShot();
                this.mWidgetColor = HwColorPicker.processBitmap(this.mScreenShot).getWidgetColor();
                this.mIsWidgetColorSet = true;
            }
            Log.i(TAG, "mWidgetColor = " + this.mWidgetColor);
            buttonImage.setColorFilter(this.mWidgetColor);
            adjustButtonViewVisibility(this.mPkgName);
            this.mWindowManager.addView(this.mButtonView, this.mSuspensionButtonParams);
            this.mIsButtonViewAdded = true;
        }
    }

    private void adjustButtonViewVisibility(String packageName) {
        if (!"com.android.systemui".equals(packageName)) {
            Log.i(TAG, "SuspensionButton adjustButtonViewVisibility not keyguard");
            return;
        }
        SuspensionButton suspensionButton = this.mButtonView;
        if (suspensionButton == null) {
            Log.e(TAG, "mButtonView is null, cannot change visibility");
        } else if (this.mButtonViewState == 2) {
            suspensionButton.setVisibility(4);
        } else {
            suspensionButton.setVisibility(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeButtonView() {
        if (!"com.android.systemui".equals(this.mPkgName)) {
            Log.i(TAG, "SuspensionButton removeButtonView not keyguard");
        } else if (this.mButtonView != null && this.mIsButtonViewAdded) {
            Log.i(TAG, "removeButtonView begin, mIsButtonViewAdded added = " + this.mIsButtonViewAdded + "mButtonView = " + this.mButtonView);
            this.mWindowManager.removeViewImmediate(this.mButtonView);
            this.mIsButtonViewAdded = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateButtonView() {
        WindowManager.LayoutParams layoutParams;
        if (!"com.android.systemui".equals(this.mPkgName)) {
            Log.i(TAG, "SuspensionButton updateButtonView not keyguard");
            return;
        }
        calculateButtonPosition();
        if (this.mButtonView != null && this.mIsButtonViewAdded && (layoutParams = this.mSuspensionButtonParams) != null) {
            layoutParams.x = this.mButtonCenterX - (this.mContext.getResources().getDimensionPixelSize(34472828) / 2);
            this.mSuspensionButtonParams.y = this.mButtonCenterY - (this.mContext.getResources().getDimensionPixelSize(34472828) / 2);
            Log.i(TAG, "updateButtonView x=" + this.mSuspensionButtonParams.x + " y=" + this.mSuspensionButtonParams.y);
            this.mWindowManager.updateViewLayout(this.mButtonView, this.mSuspensionButtonParams);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startAlphaValueAnimation(final HighLightMaskView target, final boolean isAlphaUp, float startAlpha, float endAlpha, long startDelay, int duration) {
        float tempStartAlpha = startAlpha;
        final float tempEndAlpha = endAlpha;
        if (target == null) {
            Log.d(TAG, " animation abort target null");
            return;
        }
        if (tempStartAlpha > ALPHA_LIMITED) {
            tempStartAlpha = ALPHA_LIMITED;
        } else if (tempStartAlpha < 0.0f) {
            tempStartAlpha = 0.0f;
        }
        if (tempEndAlpha > ALPHA_LIMITED) {
            tempEndAlpha = ALPHA_LIMITED;
        } else if (tempEndAlpha < 0.0f) {
            tempEndAlpha = 0.0f;
        }
        if (isAlphaUp || tempStartAlpha != tempEndAlpha) {
            Log.i(TAG, " startAlphaAnimation current alpha:" + target.getAlpha() + " startAlpha:" + tempStartAlpha + " endAlpha: " + tempEndAlpha + " duration :" + duration);
            ValueAnimator animator = ValueAnimator.ofFloat(tempStartAlpha, tempEndAlpha);
            animator.setStartDelay(startDelay);
            animator.setInterpolator(tempEndAlpha == 0.0f ? new AccelerateInterpolator() : new DecelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass41 */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    target.setAlpha((int) (((Float) animation.getAnimatedValue()).floatValue() * FingerViewController.MAX_BRIGHTNESS_LEVEL));
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass42 */
                boolean isCanceled = false;

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    Log.i(FingerViewController.TAG, " onAnimationEnd endAlpha:" + tempEndAlpha + " isCanceled:" + this.isCanceled);
                    if (!isAlphaUp && !this.isCanceled) {
                        FingerViewController.this.mHandler.post(FingerViewController.this.mRemoveHighLightView);
                    }
                    this.isCanceled = false;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    Log.i(FingerViewController.TAG, "onAnimationStart");
                    this.isCanceled = false;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animation) {
                    this.isCanceled = true;
                    super.onAnimationCancel(animation);
                }
            });
            animator.setDuration((long) duration);
            animator.start();
            return;
        }
        Log.i(TAG, " endAlpha equals startAlpha ");
        this.mHandler.post(this.mRemoveHighLightView);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getEnrollDigitalBrigtness() {
        return this.mEnrollDigitalBrigtness;
    }

    public void setEnrollDigitalBrigtness(int enrollDigitalBrigtness) {
        this.mEnrollDigitalBrigtness = enrollDigitalBrigtness;
    }

    public void showHighlightCircle() {
        this.mHandler.post(this.mShowHighlightCircleRunnable);
    }

    public void removeHighlightCircle() {
        this.mHandler.post(this.mRemoveHighlightCircleRunnable);
    }

    public void notifyCaptureImage() {
        if (this.mFingerViewChangeCallback != null) {
            Log.i(TAG, "onNotifyCaptureImage ");
            this.mFingerViewChangeCallback.onNotifyCaptureImage();
        }
    }

    public void notifyDismissBlueSpot() {
        if (this.mFingerViewChangeCallback != null) {
            Log.i(TAG, "onNotifyBlueSpotDismiss ");
            this.mFingerViewChangeCallback.onNotifyBlueSpotDismiss();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createAndAddHighLightView() {
        if (this.mHighLightView == null) {
            this.mHighLightView = new HighLightMaskView(this.mContext, this.mCurrentBrightness, this.mHighlightSpotRadius, this.mHighlightSpotColor);
        }
        getCurrentFingerprintCenter();
        this.mHighLightView.setCenterPoints(this.mFingerprintCenterX, this.mFingerprintCenterY);
        Log.i(TAG, "current height = " + this.mCurrentHeight + ",SpotColor = " + this.mHighlightSpotColor);
        if (this.mIsNeedReload) {
            this.mCurrentHeight = SystemPropertiesEx.getInt("persist.sys.rog.height", this.mDefaultDisplayHeight);
            this.mIsNeedReload = false;
        }
        this.mHighLightView.setScale(((float) this.mCurrentHeight) / ((float) this.mDefaultDisplayHeight));
        this.mHighLightView.setType(this.mHighLightShowType);
        this.mHighLightView.setPackageName(this.mPkgName);
        WindowManager.LayoutParams highLightViewParams = this.mHighLightView.getHighlightViewParams();
        highLightViewParams.setTitle("hwSingleMode_window_for_UD_highlight_mask");
        if (this.mHighLightShowType == 1) {
            highLightViewParams.setTitle(HIGHLIGHT_VIEW_TITLE_NAME);
        }
        int i = this.mHighLightShowType;
        if (i == 1) {
            this.mHighLightView.setCircleVisibility(0);
        } else if (i == 0) {
            this.mHighLightView.setCircleVisibility(4);
        }
        if (this.mHighLightView.getParent() != null) {
            Log.v(TAG, "REMOVE! mHighLightView before add");
            this.mWindowManager.removeView(this.mHighLightView);
        }
        this.mWindowManager.addView(this.mHighLightView, highLightViewParams);
        this.mIshighLightViewAdded = true;
        if (this.mHighLightShowType == 1) {
            this.mMaxDigitalBrigtness = transformBrightnessViaScreen(this.mHighlightBrightnessLevel);
            setBacklightViaDisplayEngine(1, this.mMaxDigitalBrigtness, transformBrightnessViaScreen(this.mCurrentBrightness));
            DisplayEngineManager displayEngineManager = this.mDisplayEngineManager;
            if (displayEngineManager != null) {
                displayEngineManager.setScene(29, (int) this.mMaxDigitalBrigtness);
                Log.i(TAG, "mDisplayEngineManager set scene");
            }
        }
    }

    private void setBacklightViaDisplayEngine(float maxBright) {
        setBacklightViaDisplayEngine(1, maxBright, maxBright);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setBacklightViaDisplayEngine(int scene, float maxBright, float currentBright) {
        if (this.mDisplayEngineManager == null) {
            Log.w(TAG, "mDisplayEngineManager is null");
            return;
        }
        PersistableBundle bundle = new PersistableBundle();
        bundle.putIntArray("Buffer", new int[]{scene, (int) maxBright, (int) currentBright});
        bundle.putInt("BufferLength", sUdBuffSize);
        this.mDisplayEngineManager.setData(13, bundle);
        Log.i(TAG, "mDisplayEngineManager set scene: " + scene + "Bright max: " + maxBright + " current:" + currentBright);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float transformBrightnessViaScreen(int brightness) {
        int i = sInvalidBrightness;
        if (i == this.mNormalizedMaxBrightness || i == this.mNormalizedMinBrightness) {
            Log.i(TAG, "have not get the valid brightness, try again");
            getBrightnessRangeFromPanelInfo();
        }
        int i2 = this.mNormalizedMaxBrightness;
        int i3 = this.mNormalizedMinBrightness;
        return (((((float) brightness) - 4.0f) / 251.0f) * ((float) (i2 - i3))) + ((float) i3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeHighLightViewInner() {
        if (this.mIshighLightViewAdded && this.mHighLightView != null) {
            Log.i(TAG, "highlightview is show, remove highlightview");
            this.mWindowManager.removeView(this.mHighLightView);
        }
        this.mHighLightView = null;
        this.mIshighLightViewAdded = false;
        this.mHandler.removeCallbacks(this.mSetScene);
        this.mHandler.postDelayed(this.mSetScene, SET_SCENE_DELAY_TIME);
        getInstance(this.mContext).notifyDismissBlueSpot();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createBackFingprintView() {
        int currentRotation = this.mWindowManager.getDefaultDisplay().getRotation();
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", (int) DEFAULT_LCD_DENSITY));
        int dpi = SystemProperties.getInt("persist.sys.dpi", lcdDpi);
        Configuration curConfig = new Configuration();
        try {
            curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }
        boolean isFontScaleChange = curConfig.fontScale != this.mFontScale;
        if (isFontScaleChange) {
            Log.e(TAG, "isFontScaleChange before createBackFingprintView, curCenfig.fontScale : " + curConfig.fontScale + ", mFontScale : " + this.mFontScale);
            this.mContext.getResources().updateConfiguration(curConfig, null);
            this.mFontScale = curConfig.fontScale;
        }
        if (!(this.mBackFingerprintView != null && this.mCurrentHeight == this.mSavedBackViewHeight && currentRotation == this.mSavedBackViewRotation && dpi == this.mSavedBackViewDpi && !isFontScaleChange)) {
            this.mBackFingerprintView = (BackFingerprintView) this.mLayoutInflater.inflate(34013465, (ViewGroup) null);
            this.mSavedBackViewDpi = dpi;
            this.mSavedBackViewHeight = this.mCurrentHeight;
            this.mSavedBackViewRotation = currentRotation;
        }
        float dpiScale = (((float) lcdDpi) * 1.0f) / ((float) dpi);
        this.mBackFingerprintHintView = (TextView) this.mBackFingerprintView.findViewById(34603555);
        this.mBackFingerprintUsePasswordView = (Button) this.mBackFingerprintView.findViewById(34603557);
        this.mBackFingerprintCancelView = (Button) this.mBackFingerprintView.findViewById(34603552);
        RelativeLayout buttonLayout = (RelativeLayout) this.mBackFingerprintView.findViewById(34603551);
        ViewGroup.MarginLayoutParams usePasswordViewParams = (ViewGroup.MarginLayoutParams) this.mBackFingerprintUsePasswordView.getLayoutParams();
        usePasswordViewParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472119)) * dpiScale) + 0.5f);
        usePasswordViewParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472112)) * dpiScale) + 0.5f);
        this.mBackFingerprintUsePasswordView.setLayoutParams(usePasswordViewParams);
        ViewGroup.MarginLayoutParams cancelViewParams = (ViewGroup.MarginLayoutParams) this.mBackFingerprintCancelView.getLayoutParams();
        cancelViewParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472119)) * dpiScale) + 0.5f);
        cancelViewParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472112)) * dpiScale) + 0.5f);
        this.mBackFingerprintCancelView.setLayoutParams(cancelViewParams);
        ViewGroup.MarginLayoutParams buttonLayoutParams = (ViewGroup.MarginLayoutParams) buttonLayout.getLayoutParams();
        buttonLayoutParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472118)) * dpiScale) + 0.5f);
        buttonLayoutParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472117)) * dpiScale) + 0.5f);
        buttonLayout.setLayoutParams(buttonLayoutParams);
        TextView backFingerprintTitle = (TextView) this.mBackFingerprintView.findViewById(34603556);
        TextView backFingerprintDescription = (TextView) this.mBackFingerprintView.findViewById(34603553);
        Bundle bundle = this.mPkgAttributes;
        if (bundle != null) {
            if (bundle.getString("title") != null) {
                Log.i(TAG, "title =" + this.mPkgAttributes.getString("title"));
                backFingerprintTitle.setVisibility(0);
                backFingerprintTitle.setText(this.mPkgAttributes.getString("title"));
            }
            if (this.mPkgAttributes.getString("subtitle") != null) {
                this.mBackFingerprintHintView.setVisibility(0);
                this.mBackFingerprintHintView.setText(this.mPkgAttributes.getString("subtitle"));
                this.mSubTitle = this.mPkgAttributes.getString("subtitle");
            } else {
                this.mBackFingerprintHintView.setVisibility(4);
            }
            if (this.mPkgAttributes.getString("description") != null) {
                Log.i(TAG, "description =" + this.mPkgAttributes.getString("description"));
                backFingerprintDescription.setText(this.mPkgAttributes.getString("description"));
            }
            if (this.mPkgAttributes.getString("positive_text") != null) {
                Log.i(TAG, "positive_text =" + this.mPkgAttributes.getString("positive_text"));
                this.mBackFingerprintUsePasswordView.setVisibility(0);
                this.mBackFingerprintUsePasswordView.setText(this.mPkgAttributes.getString("positive_text"));
            } else {
                this.mBackFingerprintUsePasswordView.setVisibility(4);
            }
            if (this.mPkgAttributes.getString("negative_text") != null) {
                Log.i(TAG, "negative_text =" + this.mPkgAttributes.getString("negative_text"));
                this.mBackFingerprintCancelView.setText(this.mPkgAttributes.getString("negative_text"));
            }
        }
        resetFrozenCountDownIfNeed();
        if (this.mIsFingerFrozen) {
            TextView textView = this.mBackFingerprintHintView;
            Resources resources = this.mContext.getResources();
            int i = this.mRemainedSecs;
            textView.setText(resources.getQuantityString(34406411, i, Integer.valueOf(i)));
        }
        this.mBackFingerprintUsePasswordView.setOnClickListener(new View.OnClickListener() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass43 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (FingerViewController.this.mDialogReceiver != null) {
                    try {
                        Log.i(FingerViewController.TAG, "back fingerprint view, usepassword clicked");
                        FingerViewController.this.mDialogReceiver.onDialogDismissed(1);
                    } catch (RemoteException e) {
                        Log.w(FingerViewController.TAG, "catch exception");
                    }
                }
                if (FingerViewController.this.mBiometricServiceReceiver != null) {
                    try {
                        Log.i(FingerViewController.TAG, "back fingerprint view, usepassword clicked");
                        FingerViewController.this.mBiometricServiceReceiver.onDialogDismissed(2);
                    } catch (RemoteException e2) {
                        Log.w(FingerViewController.TAG, "catch exception");
                    }
                }
            }
        });
        this.mBackFingerprintCancelView.setOnClickListener(new View.OnClickListener() {
            /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass44 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                FingerViewController.this.mHandler.post(FingerViewController.this.mRemoveBackFingprintRunnable);
                if (FingerViewController.this.mDialogReceiver != null) {
                    try {
                        Log.i(FingerViewController.TAG, "back fingerprint view, cancel clicked");
                        FingerViewController.this.mDialogReceiver.onDialogDismissed(2);
                    } catch (RemoteException e) {
                        Log.w(FingerViewController.TAG, "catch exception");
                    }
                }
                if (FingerViewController.this.mBiometricServiceReceiver != null) {
                    try {
                        Log.i(FingerViewController.TAG, "back fingerprint view, cancel clicked");
                        FingerViewController.this.mBiometricServiceReceiver.onDialogDismissed(2);
                    } catch (RemoteException e2) {
                        Log.w(FingerViewController.TAG, "catch exception");
                    }
                }
            }
        });
        WindowManager.LayoutParams backFingerprintLayoutParams = new WindowManager.LayoutParams();
        backFingerprintLayoutParams.layoutInDisplayCutoutMode = 1;
        backFingerprintLayoutParams.flags = 201852424;
        backFingerprintLayoutParams.privateFlags |= 16;
        backFingerprintLayoutParams.format = -3;
        backFingerprintLayoutParams.type = HwArbitrationDEFS.MSG_VPN_STATE_OPEN;
        backFingerprintLayoutParams.screenOrientation = 14;
        backFingerprintLayoutParams.setTitle("back_fingerprint_view");
        if (!this.mBackFingerprintView.isAttachedToWindow()) {
            startBlurBackViewScreenshot();
            this.mWindowManager.addView(this.mBackFingerprintView, backFingerprintLayoutParams);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeBackFingprintView() {
        BackFingerprintView backFingerprintView = this.mBackFingerprintView;
        if (backFingerprintView != null && backFingerprintView.isAttachedToWindow()) {
            this.mWindowManager.removeView(this.mBackFingerprintView);
            Bitmap bitmap = this.mBlurBitmap;
            if (bitmap != null) {
                bitmap.recycle();
                BackFingerprintView backFingerprintView2 = this.mBackFingerprintView;
                if (backFingerprintView2 != null) {
                    backFingerprintView2.setBackgroundDrawable(null);
                }
            }
            Bitmap bitmap2 = this.mScreenShot;
            if (bitmap2 != null) {
                bitmap2.recycle();
                this.mScreenShot = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBackFingprintView() {
        int i = this.mAuthenticateResult;
        if (i == 1) {
            updateBackFingerprintHintView();
        } else if (i == 0) {
            updateBackFingerprintHintView(this.mContext.getString(33685684));
            removeBackFingprintView();
        }
    }

    private void updateBackFingerprintHintView() {
        Log.i(TAG, "updateBackFingerprintHintView start remaind time = " + this.mRemainTimes);
        BackFingerprintView backFingerprintView = this.mBackFingerprintView;
        if (backFingerprintView != null && backFingerprintView.isAttachedToWindow() && this.mBackFingerprintHintView != null) {
            int i = this.mRemainTimes;
            if (i > 0 && i < 5) {
                Resources resources = this.mContext.getResources();
                int i2 = this.mRemainTimes;
                this.mBackFingerprintHintView.setText(resources.getQuantityString(34406412, i2, Integer.valueOf(i2)));
            } else if (this.mRemainTimes == 0) {
                if (!this.mIsFingerFrozen) {
                    RemainTimeCountDown remainTimeCountDown = this.mMyCountDown;
                    if (remainTimeCountDown != null) {
                        remainTimeCountDown.cancel();
                    }
                    this.mMyCountDown = new RemainTimeCountDown(HwArbitrationDEFS.DelayTimeMillisA, 1000);
                    this.mMyCountDown.start();
                    this.mIsFingerFrozen = true;
                } else {
                    return;
                }
            }
            this.mBackFingerprintView.postInvalidate();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBackFingerprintHintView(String hint) {
        TextView textView = this.mBackFingerprintHintView;
        if (textView != null) {
            textView.setText(hint);
        }
        BackFingerprintView backFingerprintView = this.mBackFingerprintView;
        if (backFingerprintView != null && backFingerprintView.isAttachedToWindow()) {
            this.mBackFingerprintView.postInvalidate();
        }
    }

    private void startBlurBackViewScreenshot() {
        getScreenShot();
        Log.i(TAG, "mScreenShot = " + this.mScreenShot);
        Bitmap bitmap = this.mScreenShot;
        if (bitmap == null || (HwColorPicker.processBitmap(bitmap).getDomainColor() == COLOR_BLACK && !"com.android.systemui".equals(this.mPkgName))) {
            this.mBlurBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            this.mBlurBitmap.eraseColor(-7829368);
            this.mBlurDrawable = new BitmapDrawable(this.mContext.getResources(), this.mBlurBitmap);
            this.mBackFingerprintView.setBackgroundDrawable(this.mBlurDrawable);
            return;
        }
        Context context = this.mContext;
        Bitmap bitmap2 = this.mScreenShot;
        this.mBlurBitmap = BlurUtils.blurMaskImage(context, bitmap2, bitmap2, 25);
        this.mBlurDrawable = new BitmapDrawable(this.mContext.getResources(), this.mBlurBitmap);
        this.mBackFingerprintView.setBackgroundDrawable(this.mBlurDrawable);
    }

    private void onCancelClick() {
        this.mHandler.post(this.mRemoveFingerViewRunnable);
        if (!this.mIsFingerFrozen) {
            this.mHandler.post(this.mAddButtonViewRunnable);
        }
        ICallBack iCallBack = this.mFingerViewChangeCallback;
        if (iCallBack != null) {
            iCallBack.onFingerViewStateChange(3);
        }
    }

    private void onUsePasswordClick() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        this.mHandler.post(this.mRemoveFingerViewRunnable);
        this.mHandler.post(this.mRemoveButtonViewRunnable);
    }

    public void parseBundle4Keyguard(Bundle bundle) {
        ICallBack iCallBack;
        if (bundle != null) {
            int suspend = bundle.getInt("suspend", 0);
            Log.i(TAG, " suspend:" + suspend + " mIsButtonViewAdded:" + this.mIsButtonViewAdded + " mIsFingerprintViewAdded:" + this.mIsFingerprintViewAdded);
            if (suspend == -1) {
                if (this.mIsButtonViewAdded && (iCallBack = this.mFingerViewChangeCallback) != null) {
                    iCallBack.onFingerViewStateChange(2);
                }
                if (this.mIsFingerprintViewAdded) {
                    return;
                }
            }
            byte[] viewTypes = bundle.getByteArray("viewType");
            byte[] viewStates = bundle.getByteArray("viewState");
            if (viewTypes == null || viewStates == null) {
                Log.e(TAG, "viewTypes or viewStates is null");
                return;
            }
            int i = 0;
            while (i < viewTypes.length && i < viewStates.length) {
                if (i % 100 == 0) {
                    Log.i(TAG, "do loop in parseBundle4Keyguard, time = " + i);
                }
                byte type = viewTypes[i];
                byte state = viewStates[i];
                Log.i(TAG, " type:" + ((int) type) + " state:" + ((int) state));
                handleViewType(type, state, bundle);
                i++;
            }
        }
    }

    private void handleViewType(byte type, byte state, Bundle bundle) {
        if (type != 0) {
            if (type != 1) {
                if (type == 2 || type != 3) {
                }
            } else if (state == 1) {
                int[] locations = null;
                try {
                    locations = bundle.getIntArray("buttonLocation");
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, "getIntArray error");
                }
                if (locations != null) {
                    this.mButtonViewState = 1;
                    this.mButtonColor = bundle.getInt("buttonColor", 0);
                    showSuspensionButtonForApp(locations[0], locations[1], "android.uid.systemui");
                }
            } else if (state == 0) {
                this.mHandler.post(this.mRemoveButtonViewRunnable);
            } else if (state == 2) {
                int[] locations2 = null;
                try {
                    locations2 = bundle.getIntArray("buttonLocation");
                } catch (ArrayIndexOutOfBoundsException e2) {
                    Log.e(TAG, "getIntArray error");
                }
                if (locations2 != null) {
                    this.mHandler.post(this.mRemoveFingerViewRunnable);
                    this.mButtonViewState = 2;
                    showSuspensionButtonForApp(locations2[0], locations2[1], "android.uid.systemui");
                }
            }
        } else if (state == 1) {
            this.mHandler.post(this.mAddFingerViewRunnable);
        } else if (state == 0) {
            this.mHandler.post(this.mRemoveFingerViewRunnable);
            ICallBack iCallBack = this.mFingerViewChangeCallback;
            if (iCallBack != null) {
                iCallBack.onFingerViewStateChange(1);
            }
        }
    }

    public void setFingerprintPosition(int[] position) {
        if (position != null) {
            this.mFingerprintPositions = (int[]) position.clone();
            if (this.mFingerprintPositions.length >= 4) {
                Log.i(TAG, "setFingerprintPosition,left = " + this.mFingerprintPositions[0] + "right = " + this.mFingerprintPositions[2]);
                Log.i(TAG, "setFingerprintPosition,top = " + this.mFingerprintPositions[1] + "button = " + this.mFingerprintPositions[3]);
                int[] iArr = this.mFingerprintPositions;
                this.mFingerprintCenterX = (iArr[0] + iArr[2]) / 2;
                this.mFingerprintCenterY = (iArr[1] + iArr[3]) / 2;
            }
        }
    }

    public void setHighLightBrightnessLevel(int brightness) {
        int tempBrightness = brightness;
        if (tempBrightness > 255) {
            Log.i(TAG, "brightness is " + tempBrightness + ",adjust it to 255");
            tempBrightness = 255;
        } else if (tempBrightness < 0) {
            Log.i(TAG, "brightness is " + tempBrightness + ",adjust it to 0");
            tempBrightness = 0;
        }
        Log.i(TAG, "brightness to be set is " + tempBrightness);
        this.mHighlightBrightnessLevel = tempBrightness;
        this.mMaxDigitalBrigtness = transformBrightnessViaScreen(this.mHighlightBrightnessLevel);
        setBacklightViaDisplayEngine((float) ((int) this.mMaxDigitalBrigtness));
    }

    public void setHighLightSpotColor(int color) {
        Log.i(TAG, "color to be set is " + color);
        this.mHighlightSpotColor = color;
        FingerprintCircleOverlay fingerprintCircleOverlay = this.mFingerprintCircleOverlay;
        if (fingerprintCircleOverlay != null) {
            fingerprintCircleOverlay.setColor(color);
        }
    }

    public void setHighLightSpotRadius(int radius) {
        this.mHighlightSpotRadius = radius;
        FingerprintCircleOverlay fingerprintCircleOverlay = this.mFingerprintCircleOverlay;
        if (fingerprintCircleOverlay != null) {
            fingerprintCircleOverlay.setRadius(radius);
        }
    }

    public void setFingerPrintLogoRadius(int radius) {
        this.mFingerLogoRadius = radius;
        Log.i(TAG, "setFingerPrintLogoRadius: " + this.mFingerLogoRadius);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x015a: APUT  
      (r5v6 'margins' int[] A[D('margins' int[])])
      (0 ??[int, short, byte, char])
      (r4v11 'marginLeft' int A[D('marginLeft' int)])
     */
    private int[] calculateFingerprintMargin() {
        int marginLeft;
        Log.i(TAG, "left = " + this.mFingerprintPositions[0] + "right = " + this.mFingerprintPositions[2]);
        Log.i(TAG, "top = " + this.mFingerprintPositions[1] + "button = " + this.mFingerprintPositions[3]);
        float dpiScale = getDpiScale();
        int[] margins = new int[2];
        int fingerPrintInScreenWidth = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472519)) * dpiScale) + 0.5f);
        int fingerPrintInScreenHeight = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472518)) * dpiScale) + 0.5f);
        Log.i(TAG, "current height = " + this.mCurrentHeight + "mDefaultDisplayHeight = " + this.mDefaultDisplayHeight);
        float scale = ((float) this.mCurrentHeight) / ((float) this.mDefaultDisplayHeight);
        if (isNewMagazineViewForDownFP()) {
            int i = this.mCurrentRotation;
            if (i == 0 || i == 2) {
                return handleMargin(scale, fingerPrintInScreenWidth, fingerPrintInScreenHeight);
            }
            int[] iArr = this.mFingerprintPositions;
            this.mFingerprintCenterY = (iArr[0] + iArr[2]) / 2;
            this.mFingerprintCenterX = (iArr[1] + iArr[3]) / 2;
            int marginLeft2 = ((int) ((((float) this.mFingerprintCenterX) * scale) + 0.5f)) - (fingerPrintInScreenWidth / 2);
            int marginTop = ((int) ((((float) this.mFingerprintCenterY) * scale) + 0.5f)) - (fingerPrintInScreenHeight / 2);
            if (i == 3) {
                marginLeft = (this.mCurrentHeight - marginLeft2) - fingerPrintInScreenWidth;
            } else {
                marginLeft = marginLeft2 - this.mNotchHeight;
            }
            margins[0] = marginLeft;
            margins[1] = marginTop;
            Log.i(TAG, "marginLeft = " + marginLeft + "marginTop = " + marginTop + "scale = " + scale);
            return margins;
        }
        int i2 = this.mCurrentRotation;
        if (i2 == 0 || i2 == 2) {
            return handleMargin(scale, fingerPrintInScreenWidth, fingerPrintInScreenHeight);
        }
        int[] iArr2 = this.mFingerprintPositions;
        this.mFingerprintCenterX = (iArr2[1] + iArr2[3]) / 2;
        this.mFingerprintCenterY = (iArr2[0] + iArr2[2]) / 2;
        int marginTop2 = (int) (((((float) this.mFingerprintCenterY) * scale) - (((float) fingerPrintInScreenHeight) / 2.0f)) + 0.5f);
        int marginLeft3 = (int) ((((((float) (this.mDefaultDisplayHeight - this.mFingerprintCenterX)) * scale) - (((float) this.mContext.getResources().getDimensionPixelSize(34472740)) * dpiScale)) - (((float) fingerPrintInScreenWidth) / 2.0f)) + 0.5f);
        margins[0] = marginLeft3;
        margins[1] = marginTop2;
        Log.i(TAG, "marginLeft = " + marginLeft3 + "marginTop = " + marginTop2 + "scale = " + scale + "mDefaultDisplayHeight = " + this.mDefaultDisplayHeight);
        return margins;
    }

    private int[] handleMargin(float scale, int fpInScreenWidth, int fpInScreenHeight) {
        int[] iArr = this.mFingerprintPositions;
        this.mFingerprintCenterX = (iArr[0] + iArr[2]) / 2;
        this.mFingerprintCenterY = (iArr[1] + iArr[3]) / 2;
        int marginLeft = ((int) ((((float) this.mFingerprintCenterX) * scale) + 0.5f)) - (fpInScreenHeight / 2);
        int marginTop = ((int) ((((float) this.mFingerprintCenterY) * scale) + 0.5f)) - (fpInScreenWidth / 2);
        int[] margins = {marginLeft, marginTop};
        Log.i(TAG, "marginLeft = " + marginLeft + "marginTop = " + marginTop + "scale = " + scale);
        return margins;
    }

    private float getDpiScale() {
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", (int) DEFAULT_LCD_DENSITY));
        int dpi = SystemProperties.getInt("persist.sys.dpi", lcdDpi);
        int realdpi = SystemProperties.getInt("persist.sys.realdpi", dpi);
        float scale = (((float) lcdDpi) * 1.0f) / ((float) dpi);
        Log.i(TAG, "getDpiScale: lcdDpi: " + lcdDpi + " dpi: " + dpi + " realdpi: " + realdpi + " scale: " + scale);
        return scale;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float getPxScale() {
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", sDefaultLcdDpi));
        int dpi = SystemProperties.getInt("persist.sys.dpi", lcdDpi);
        int realdpi = SystemProperties.getInt("persist.sys.realdpi", dpi);
        float scale = (((float) realdpi) * 1.0f) / ((float) dpi);
        Log.i(TAG, "getPxScale: lcdDpi: " + lcdDpi + " dpi: " + dpi + " realdpi: " + realdpi + " scale: " + scale);
        return scale;
    }

    private int[] calculateFingerprintLayoutLeftMargin(int width) {
        return FingerprintViewUtils.calculateFingerprintLayoutLeftMargin(width, ((float) this.mCurrentHeight) / ((float) this.mDefaultDisplayHeight), this.mCurrentRotation, this.mContext, this.mFingerprintCenterX);
    }

    private int calculateRemoteViewLeftMargin(int fingerLayoutWidth) {
        float dpiScale = getDpiScale();
        int i = this.mCurrentRotation;
        if (i == 3) {
            return (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472764)) * dpiScale) + (((float) this.mContext.getResources().getDimensionPixelSize(34472740)) * dpiScale) + ((float) fingerLayoutWidth) + 0.5f);
        }
        if (i == 1) {
            return (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472764)) * dpiScale) + 0.5f);
        }
        return 0;
    }

    private void calculateButtonPosition() {
        String str = this.mPkgName;
        if (str != null && !str.equals("com.android.systemui")) {
            getCurrentRotation();
            Log.i(TAG, "mCurrentRotation = " + this.mCurrentRotation);
            int i = this.mCurrentHeight;
            float scale = ((float) i) / ((float) this.mDefaultDisplayHeight);
            int i2 = this.mCurrentRotation;
            if (i2 != 0) {
                if (i2 == 1) {
                    int[] iArr = this.mFingerprintPositions;
                    this.mButtonCenterX = (int) (((((float) (iArr[1] + iArr[3])) / 2.0f) * scale) + 0.5f);
                    this.mButtonCenterY = (this.mCurrentWidth - this.mContext.getResources().getDimensionPixelSize(34472280)) - (this.mContext.getResources().getDimensionPixelSize(34472828) / 2);
                } else if (i2 != 2) {
                    if (i2 == 3) {
                        int[] iArr2 = this.mFingerprintPositions;
                        this.mButtonCenterX = i - ((int) (((((float) (iArr2[1] + iArr2[3])) / 2.0f) * scale) + 0.5f));
                        this.mButtonCenterY = (this.mCurrentWidth - this.mContext.getResources().getDimensionPixelSize(34472280)) - (this.mContext.getResources().getDimensionPixelSize(34472828) / 2);
                    }
                }
                Log.i(TAG, "mButtonCenterX = " + this.mButtonCenterX + ",mButtonCenterY =" + this.mButtonCenterY);
            }
            int[] iArr3 = this.mFingerprintPositions;
            this.mButtonCenterX = (int) (((((float) (iArr3[0] + iArr3[2])) / 2.0f) * scale) + 0.5f);
            this.mButtonCenterY = (this.mCurrentHeight - this.mContext.getResources().getDimensionPixelSize(34472280)) - (this.mContext.getResources().getDimensionPixelSize(34472828) / 2);
            Log.i(TAG, "mButtonCenterX = " + this.mButtonCenterX + ",mButtonCenterY =" + this.mButtonCenterY);
        }
    }

    private boolean isScreenOn() {
        if (!this.mPowerManager.isInteractive()) {
            Log.i(TAG, "screen is not Interactive");
            return false;
        }
        getBrightness();
        if (this.mCurrentBrightness != 0) {
            return true;
        }
        Log.i(TAG, "brightness is not set");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getBrightness() {
        Bundle data = new Bundle();
        if (HwPowerManager.getHwBrightnessData("CurrentBrightness", data) != 0) {
            this.mCurrentBrightness = -1;
            Log.w(TAG, "get currentBrightness failed!");
        } else {
            this.mCurrentBrightness = data.getInt("Brightness");
        }
        Log.i(TAG, "currentBrightness=" + this.mCurrentBrightness);
    }

    public void setLightLevel(int level, int lightLevelTime) {
        try {
            this.mIpowerManager.setBrightnessNoLimit(level, lightLevelTime);
            Log.i(TAG, "setLightLevel :" + level + " time:" + lightLevelTime);
        } catch (RemoteException e) {
            Log.e(TAG, "setFingerprintviewHighlight catch RemoteException ");
        }
    }

    /* JADX INFO: finally extract failed */
    private String getForegroundPkgName() {
        long identityToken = Binder.clearCallingIdentity();
        try {
            ActivityInfo info = ActivityManagerEx.getLastResumedActivity();
            Binder.restoreCallingIdentity(identityToken);
            String name = null;
            if (info != null) {
                name = info.packageName;
            }
            Log.w(TAG, "foreground package is " + name);
            return name;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identityToken);
            throw th;
        }
    }

    private boolean isBroadcastNeed(String pkgName) {
        for (String pkg : PACKAGES_USE_HWAUTH_INTERFACES) {
            if (pkg.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCancelHotSpotNeed(String pkgName) {
        for (String pkg : PACKAGES_USE_CANCEL_HOTSPOT_INTERFACES) {
            if (pkg.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCancelHotSpotViewVisble(String pkgName) {
        int i;
        getCurrentRotation();
        return isCancelHotSpotNeed(pkgName) && isNewMagazineViewForDownFP() && ((i = this.mCurrentRotation) == 0 || i == 2);
    }

    private void startBlurScreenshot() {
        getScreenShot();
        Bitmap bitmap = this.mScreenShot;
        if (bitmap == null || (HwColorPicker.processBitmap(bitmap).getDomainColor() == COLOR_BLACK && !"com.android.systemui".equals(this.mPkgName))) {
            this.mBlurBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            this.mBlurBitmap.eraseColor(-7829368);
            this.mBlurDrawable = new BitmapDrawable(this.mContext.getResources(), this.mBlurBitmap);
            this.mFingerView.setBackgroundDrawable(this.mBlurDrawable);
            return;
        }
        Context context = this.mContext;
        Bitmap bitmap2 = this.mScreenShot;
        this.mBlurBitmap = BlurUtils.blurMaskImage(context, bitmap2, bitmap2, 25);
        this.mBlurDrawable = new BitmapDrawable(this.mContext.getResources(), this.mBlurBitmap);
        this.mFingerView.setBackgroundDrawable(this.mBlurDrawable);
    }

    private void getScreenShot() {
        try {
            this.mScreenShot = BlurUtils.screenShotBitmap(this.mContext, BLUR_SCALE);
            if (this.mScreenShot != null) {
                this.mScreenShot = this.mScreenShot.copy(Bitmap.Config.ARGB_8888, true);
            }
        } catch (ClassCastException e) {
            Log.w(TAG, "ClassCastException in screenShotBitmap");
        } catch (Exception e2) {
            Log.w(TAG, "Exception in screenShotBitmap");
        } catch (Error err) {
            Log.w(TAG, "screenShotBitmap Error er = " + err.getMessage());
        }
    }

    private void registerSingerHandObserver() {
        if (this.mSingleContentObserver == null) {
            this.mSingleContentObserver = new SingleModeContentObserver(new Handler(), new SingleModeContentCallback());
        }
        if (!this.mIsSingleModeObserverRegistered) {
            Log.i(TAG, "registerSingerHandObserver");
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("single_hand_mode"), true, this.mSingleContentObserver);
            this.mIsSingleModeObserverRegistered = true;
        }
    }

    private void unregisterSingerHandObserver() {
        if (this.mIsSingleModeObserverRegistered) {
            Log.i(TAG, "unregisterSingerHandObserver");
            this.mContext.getContentResolver().unregisterContentObserver(this.mSingleContentObserver);
            this.mIsSingleModeObserverRegistered = false;
        }
    }

    private void exitSingleHandMode() {
        Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
    }

    private void resetFrozenCountDownIfNeed() {
        RemainTimeCountDown remainTimeCountDown;
        this.mRemainTimes = this.mFingerprintManagerEx.getRemainingNum();
        Log.i(TAG, "getRemainingNum, mRemainTimes = " + this.mRemainTimes);
        if (this.mRemainTimes > 0 && (remainTimeCountDown = this.mMyCountDown) != null) {
            remainTimeCountDown.cancel();
            this.mIsFingerFrozen = false;
        }
    }

    public void notifyFingerprintViewCoverd(boolean isCovered, Rect winFrame) {
        if (winFrame != null) {
            Log.i(TAG, "notifyWinCovered isCovered=" + isCovered + "winFrame = " + winFrame);
            if (!isCovered) {
                ICallBack iCallBack = this.mFingerViewChangeCallback;
                if (iCallBack != null) {
                    iCallBack.onFingerViewStateChange(1);
                }
            } else if (isFingerprintViewCoverd(getFingerprintViewRect(), winFrame)) {
                Log.i(TAG, "new window covers fingerprintview, suspend");
                ICallBack iCallBack2 = this.mFingerViewChangeCallback;
                if (iCallBack2 != null) {
                    iCallBack2.onFingerViewStateChange(2);
                }
            } else {
                Log.i(TAG, "new window doesn't cover fingerprintview");
            }
        }
    }

    public void notifyTouchUp(float upX, float upY) {
        if (this.mIshighLightViewAdded || this.mFingerprintCircleOverlay.isVisible()) {
            Log.i(TAG_THREAD, "UD Fingerprint notifyTouchUp point (" + upX + " , " + upY + ")");
            if (!isFingerViewTouched(upX, upY)) {
                Log.i(TAG, "notifyTouchUp,point not in fingerprint view");
            } else {
                this.mHandler.post(new Runnable() {
                    /* class huawei.com.android.server.fingerprint.FingerViewController.AnonymousClass45 */

                    @Override // java.lang.Runnable
                    public void run() {
                        Log.i(FingerViewController.TAG, "begin anonymous runnable in notifyTouchUp");
                        FingerViewController.this.removeHighlightCircle();
                    }
                });
            }
        }
    }

    private Rect getFingerprintViewRect() {
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", (int) DEFAULT_LCD_DENSITY));
        int dpi = SystemProperties.getInt("persist.sys.dpi", lcdDpi);
        float scale = (((float) SystemProperties.getInt("persist.sys.realdpi", dpi)) * 1.0f) / ((float) dpi);
        float lcdscale = (((float) lcdDpi) * 1.0f) / ((float) dpi);
        Rect fingerprintViewRect = new Rect();
        int[] iArr = this.mFingerprintPositions;
        fingerprintViewRect.left = (int) (((double) ((((float) (iArr[0] + iArr[2])) / 2.0f) * scale)) - ((((double) (((float) this.mContext.getResources().getDimensionPixelSize(34472519)) * lcdscale)) * 0.5d) + 0.5d));
        int[] iArr2 = this.mFingerprintPositions;
        fingerprintViewRect.top = (int) (((double) ((((float) (iArr2[1] + iArr2[3])) / 2.0f) * scale)) - ((((double) (((float) this.mContext.getResources().getDimensionPixelSize(34472518)) * lcdscale)) * 0.5d) + 0.5d));
        int[] iArr3 = this.mFingerprintPositions;
        fingerprintViewRect.right = (int) (((double) ((((float) (iArr3[0] + iArr3[2])) / 2.0f) * scale)) + (((double) (((float) this.mContext.getResources().getDimensionPixelSize(34472519)) * lcdscale)) * 0.5d) + 0.5d);
        int[] iArr4 = this.mFingerprintPositions;
        fingerprintViewRect.bottom = (int) (((double) ((((float) (iArr4[1] + iArr4[3])) / 2.0f) * scale)) + (((double) (((float) this.mContext.getResources().getDimensionPixelSize(34472518)) * lcdscale)) * 0.5d) + 0.5d);
        Log.i(TAG, "getFingerprintViewRect: " + fingerprintViewRect);
        return fingerprintViewRect;
    }

    private boolean isFingerprintViewCoverd(Rect fingerprintViewRect, Rect winFrame) {
        return fingerprintViewRect.right > winFrame.left && winFrame.right > fingerprintViewRect.left && fingerprintViewRect.bottom > winFrame.top && winFrame.bottom > fingerprintViewRect.top;
    }

    private boolean isFingerViewTouched(float x, float y) {
        Rect fingerprintViewRect = getFingerprintViewRect();
        return ((float) fingerprintViewRect.left) < x && ((float) fingerprintViewRect.right) > x && ((float) fingerprintViewRect.bottom) > y && ((float) fingerprintViewRect.top) < y;
    }

    public void setHighlightViewAlpha(int brightness) {
        this.mCurrentBrightness = brightness;
        this.mCurrentAlpha = getMaskAlpha(brightness);
        Handler handler = this.mHandler;
        if (handler == null) {
            Log.w(TAG, "mHandler is null");
            return;
        }
        if (handler.hasCallbacks(this.mFingerprintMaskSetAlpha)) {
            this.mHandler.removeCallbacks(this.mFingerprintMaskSetAlpha);
        }
        this.mHandler.postAtFrontOfQueue(this.mFingerprintMaskSetAlpha);
    }

    /* access modifiers changed from: private */
    public class SuspensionButtonCallback implements SuspensionButton.InterfaceCallBack {
        private SuspensionButtonCallback() {
        }

        @Override // com.huawei.server.fingerprint.SuspensionButton.InterfaceCallBack
        public void onButtonViewMoved(float endX, float endY) {
            if (FingerViewController.this.mButtonView != null) {
                FingerViewController.this.mSuspensionButtonParams.x = (int) (endX - (((float) FingerViewController.this.mSuspensionButtonParams.width) * 0.5f));
                FingerViewController.this.mSuspensionButtonParams.y = (int) (endY - (((float) FingerViewController.this.mSuspensionButtonParams.height) * 0.5f));
                Log.i(FingerViewController.TAG, "onButtonViewUpdate,x = " + FingerViewController.this.mSuspensionButtonParams.x + " ,y = " + FingerViewController.this.mSuspensionButtonParams.y);
                FingerViewController.this.mWindowManager.updateViewLayout(FingerViewController.this.mButtonView, FingerViewController.this.mSuspensionButtonParams);
            }
        }

        @Override // com.huawei.server.fingerprint.SuspensionButton.InterfaceCallBack
        public void onButtonClick() {
            FingerViewController.this.mHandler.post(FingerViewController.this.mRemoveButtonViewRunnable);
            FingerViewController.this.mHandler.post(FingerViewController.this.mAddFingerViewRunnable);
            if (FingerViewController.this.mFingerViewChangeCallback != null) {
                FingerViewController.this.mFingerViewChangeCallback.onFingerViewStateChange(1);
            }
            if (FingerViewController.this.mReceiver != null) {
                try {
                    FingerViewController.this.mReceiver.onAcquired(0, 6, 12);
                } catch (RemoteException e) {
                    Log.w(FingerViewController.TAG, "catch exception");
                }
            }
            Context context = FingerViewController.this.mContext;
            Flog.bdReport(context, 501, "{PkgName:" + FingerViewController.this.mPkgName + "}");
        }

        @Override // com.huawei.server.fingerprint.SuspensionButton.InterfaceCallBack
        public String getCurrentApp() {
            return FingerViewController.this.mPkgName;
        }

        @Override // com.huawei.server.fingerprint.SuspensionButton.InterfaceCallBack
        public void userActivity() {
            FingerViewController.this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        }

        @Override // com.huawei.server.fingerprint.SuspensionButton.InterfaceCallBack
        public void onConfigurationChanged(Configuration newConfig) {
            FingerViewController.this.mHandler.post(FingerViewController.this.mUpdateButtonViewRunnable);
        }
    }

    /* access modifiers changed from: private */
    public class FingerprintViewCallback implements FingerprintView.ICallBack {
        private FingerprintViewCallback() {
        }

        @Override // huawei.com.android.server.fingerprint.FingerprintView.ICallBack
        public void userActivity() {
            FingerViewController.this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        }

        @Override // huawei.com.android.server.fingerprint.FingerprintView.ICallBack
        public void onConfigurationChanged(Configuration newConfig) {
        }

        @Override // huawei.com.android.server.fingerprint.FingerprintView.ICallBack
        public void onDrawFinish() {
        }
    }

    /* access modifiers changed from: private */
    public class SingleModeContentCallback implements SingleModeContentObserver.ICallBack {
        private SingleModeContentCallback() {
        }

        @Override // huawei.com.android.server.fingerprint.SingleModeContentObserver.ICallBack
        public void onContentChange() {
            if (((FingerViewController.this.mFingerprintView != null && FingerViewController.this.mFingerprintView.isAttachedToWindow()) || (FingerViewController.this.mLayoutForAlipay != null && FingerViewController.this.mLayoutForAlipay.isAttachedToWindow())) && !Settings.Global.getString(FingerViewController.this.mContext.getContentResolver(), "single_hand_mode").isEmpty()) {
                Settings.Global.putString(FingerViewController.this.mContext.getContentResolver(), "single_hand_mode", "");
            }
        }
    }

    /* access modifiers changed from: private */
    public class RemainTimeCountDown extends CountDownTimer {
        public RemainTimeCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override // android.os.CountDownTimer
        public void onTick(long millisUntilFinished) {
            FingerViewController.this.mRemainedSecs = (int) ((((double) millisUntilFinished) / 1000.0d) + 0.5d);
            if (FingerViewController.this.mRemainedSecs <= 0) {
                return;
            }
            if (FingerViewController.this.mFingerView != null && FingerViewController.this.mFingerView.isAttachedToWindow()) {
                FingerViewController fingerViewController = FingerViewController.this;
                fingerViewController.updateHintView(fingerViewController.mContext.getResources().getQuantityString(34406411, FingerViewController.this.mRemainedSecs, Integer.valueOf(FingerViewController.this.mRemainedSecs)));
                FingerViewController.this.mFingerprintView.setContentDescription("");
            } else if (FingerViewController.this.mBackFingerprintView != null && FingerViewController.this.mBackFingerprintView.isAttachedToWindow()) {
                FingerViewController fingerViewController2 = FingerViewController.this;
                fingerViewController2.updateBackFingerprintHintView(fingerViewController2.mContext.getResources().getQuantityString(34406411, FingerViewController.this.mRemainedSecs, Integer.valueOf(FingerViewController.this.mRemainedSecs)));
            }
        }

        @Override // android.os.CountDownTimer
        public void onFinish() {
            Log.i(FingerViewController.TAG, "RemainTimeCountDown onFinish");
            FingerViewController.this.mIsFingerFrozen = false;
            if (FingerViewController.this.mFingerView != null && FingerViewController.this.mFingerView.isAttachedToWindow()) {
                if (FingerViewController.this.mIsHasBackFingerprint) {
                    FingerViewController fingerViewController = FingerViewController.this;
                    fingerViewController.updateHintView(fingerViewController.mContext.getString(33685679));
                } else {
                    FingerViewController fingerViewController2 = FingerViewController.this;
                    fingerViewController2.updateHintView(fingerViewController2.mContext.getString(33685692));
                }
                FingerViewController.this.mFingerprintView.setContentDescription(FingerViewController.this.mContext.getString(33686273));
            } else if (FingerViewController.this.mBackFingerprintView != null && FingerViewController.this.mBackFingerprintView.isAttachedToWindow()) {
                FingerViewController fingerViewController3 = FingerViewController.this;
                fingerViewController3.updateBackFingerprintHintView(fingerViewController3.mSubTitle);
            }
            if (FingerViewController.this.mHandler != null) {
                FingerViewController.this.mHandler.post(FingerViewController.this.mRemoveFingerViewRunnable);
            }
            if (FingerViewController.this.mFingerViewChangeCallback != null) {
                FingerViewController.this.mFingerViewChangeCallback.onFingerViewStateChange(0);
            }
        }
    }

    private boolean getBrightnessRangeFromPanelInfo() {
        File file = new File(sPanelInfoNode);
        if (!file.exists()) {
            Log.w(TAG, "getBrightnessRangeFromPanelInfo sPanelInfoNode:" + sPanelInfoNode + " isn't exist");
            return false;
        }
        BufferedReader reader = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String tempString = reader.readLine();
            if (tempString != null) {
                Log.i(TAG, "getBrightnessRangeFromPanelInfo String = " + tempString);
                if (tempString.length() == 0) {
                    Log.e(TAG, "getBrightnessRangeFromPanelInfo error! String is null");
                    reader.close();
                    close(reader, fis);
                    return false;
                }
                String[] stringSpliteds = tempString.split(",");
                if (stringSpliteds.length < 2) {
                    Log.e(TAG, "split failed! String = " + tempString);
                    reader.close();
                    close(reader, fis);
                    return false;
                } else if (parsePanelInfo(stringSpliteds)) {
                    reader.close();
                    close(reader, fis);
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "getBrightnessRangeFromPanelInfo error! FileNotFoundException");
        } catch (IOException e2) {
            Log.e(TAG, "getBrightnessRangeFromPanelInfo error! IOException");
        } catch (Exception e3) {
            Log.e(TAG, "getBrightnessRangeFromPanelInfo error! Exception");
        } catch (Throwable th) {
            close(null, null);
            throw th;
        }
        close(reader, fis);
        return false;
    }

    private void close(BufferedReader reader, FileInputStream fis) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                Log.e(TAG, "close error! IOException");
            }
        }
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e2) {
                Log.e(TAG, "close error! IOException");
            }
        }
    }

    private boolean parsePanelInfo(String[] stringSplited) {
        if (stringSplited == null) {
            return false;
        }
        int max = -1;
        int min = -1;
        String key = null;
        for (int i = 0; i < stringSplited.length; i++) {
            try {
                if (i % 100 == 0) {
                    Log.i(TAG, "do loop in parseBundle4Keyguard, time = " + i);
                }
                key = "blmax:";
                int index = stringSplited[i].indexOf(key);
                if (index != -1) {
                    try {
                        max = Integer.parseInt(stringSplited[i].substring(key.length() + index));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "parsePanelInfo max NumberFormatException");
                    }
                } else {
                    key = "blmin:";
                    int index2 = stringSplited[i].indexOf(key);
                    if (index2 != -1) {
                        try {
                            min = Integer.parseInt(stringSplited[i].substring(key.length() + index2));
                        } catch (NumberFormatException e2) {
                            Log.e(TAG, "parsePanelInfo min NumberFormatException");
                        }
                    }
                }
            } catch (NumberFormatException e3) {
                Log.e(TAG, "parsePanelInfo() error! " + key);
                return false;
            }
        }
        if (max == -1 || min == -1) {
            return false;
        }
        Log.i(TAG, "getBrightnessRangeFromPanelInfo success! min = " + min + ", max = " + max);
        this.mNormalizedMaxBrightness = max;
        this.mNormalizedMinBrightness = min;
        return true;
    }

    private File getBrightnessAlphaConfigFile() {
        String lcdname = getLcdPanelName();
        String lcdversion = getVersionFromLcd();
        ArrayList<String> xmlPathList = new ArrayList<>();
        if (!(lcdversion == null || lcdname == null)) {
            xmlPathList.add(String.format(Locale.ROOT, "/display/effect/displayengine/%s_%s_%s%s", "udfp", lcdname, lcdversion, ".xml"));
        }
        if (lcdname != null) {
            xmlPathList.add(String.format(Locale.ROOT, "/display/effect/displayengine/%s_%s%s", "udfp", lcdname, ".xml"));
        }
        xmlPathList.add(String.format(Locale.ROOT, "/display/effect/displayengine/%s%s", "udfp", ".xml"));
        File xmlFile = null;
        int listSize = xmlPathList.size();
        for (int i = 0; i < listSize; i++) {
            xmlFile = HwCfgFilePolicy.getCfgFile(xmlPathList.get(i), 2);
            if (xmlFile != null) {
                Log.i(TAG, "getBrightnessAlphaConfigFile ");
                return xmlFile;
            }
        }
        return xmlFile;
    }

    private void initBrightnessAlphaConfig() throws IOException {
        File xmlFile = getBrightnessAlphaConfigFile();
        if (xmlFile == null) {
            Log.e(TAG, "brightnessAlphaConfigFile: config file is not exist !");
            return;
        }
        FileInputStream inputStream = null;
        try {
            FileInputStream inputStream2 = new FileInputStream(xmlFile);
            if (getBrightnessAlphaConfig(inputStream2)) {
                xmlFile.getAbsolutePath();
            }
            try {
                inputStream2.close();
            } catch (IOException e) {
                Log.e(TAG, "catch IOException when inputStream close");
            }
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "initBrightnessAlphaConfig error! FileNotFoundException");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Exception e3) {
            Log.e(TAG, "initBrightnessAlphaConfig error! Exception");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    Log.e(TAG, "catch IOException when inputStream close");
                }
            }
            throw th;
        }
        Log.i(TAG, "initBrightnessAlphaConfig end .");
    }

    private boolean getBrightnessAlphaConfig(InputStream inStream) {
        boolean isConfigGroupLoadStarted = false;
        boolean isLoadFinished = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType == 2) {
                    String name = parser.getName();
                    if ("BrightnessAndAlphaConfig".equals(name)) {
                        isConfigGroupLoadStarted = true;
                    } else if ("Brightness".equals(name)) {
                        this.mSampleBrightness = FingerprintViewUtils.covertToIntArray(parser.nextText());
                    } else if ("Alpha".equals(name)) {
                        this.mSampleAlphas = FingerprintViewUtils.covertToIntArray(parser.nextText());
                    } else if ("Description".equals(name)) {
                        parser.nextText();
                    }
                } else if (eventType == 3 && "BrightnessAndAlphaConfig".equals(parser.getName()) && isConfigGroupLoadStarted) {
                    isLoadFinished = true;
                    isConfigGroupLoadStarted = false;
                }
                if (isLoadFinished) {
                    break;
                }
            }
            if (isLoadFinished) {
                Log.i(TAG, "getBrightnessAlphaConfig success, xml Description.");
                return true;
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "getBrightnessAlphaConfig error! XmlPullParserException");
        } catch (IOException e2) {
            Log.e(TAG, "getBrightnessAlphaConfig error! IOException");
        } catch (NumberFormatException e3) {
            Log.e(TAG, "getBrightnessAlphaConfig error! NumberFormatException");
        } catch (Exception e4) {
            Log.e(TAG, "getBrightnessAlphaConfig error! Exception");
        }
        return true;
    }

    private String getLcdPanelName() {
        IBinder binder = ServiceManager.getService("DisplayEngineExService");
        if (binder == null) {
            Log.i(TAG, "getLcdPanelName() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Log.e(TAG, "getLcdPanelName() mService is null!");
            return null;
        }
        byte[] names = new byte[128];
        try {
            int ret = mService.getEffect(14, 0, names, names.length);
            if (ret != 0) {
                Log.e(TAG, "getLcdPanelName() getEffect failed! ret=" + ret);
                return null;
            }
            String panelName = null;
            try {
                panelName = new String(names, "UTF-8").trim().replace(' ', '_');
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unsupported encoding type!");
            }
            Log.i(TAG, "panel get finished.");
            return panelName;
        } catch (RemoteException e2) {
            Log.e(TAG, "getLcdPanelName() RemoteException");
            return null;
        }
    }

    private String getVersionFromLcd() {
        IBinder binder = ServiceManager.getService("DisplayEngineExService");
        if (binder == null) {
            Log.w(TAG, "getVersionFromLcd() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Log.w(TAG, "getVersionFromLcd() mService is null!");
            return null;
        }
        byte[] names = new byte[32];
        try {
            int ret = mService.getEffect(14, 3, names, names.length);
            if (ret != 0) {
                Log.e(TAG, "getVersionFromLcd() getEffect failed! ret=" + ret);
                return null;
            }
            String panelVersion = null;
            try {
                String lcdVersion = new String(names, "UTF-8");
                Log.i(TAG, "getVersionFromLcd() lcdVersion=" + lcdVersion);
                String lcdVersion2 = lcdVersion.trim();
                int index = lcdVersion2.indexOf("VER:");
                if (index != -1) {
                    panelVersion = lcdVersion2.substring("VER:".length() + index);
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unsupported encoding type!");
            }
            Log.i(TAG, "getVersionFromLcd() panelVersion=" + panelVersion);
            return panelVersion;
        } catch (RemoteException e2) {
            Log.e(TAG, "getVersionFromLcd() RemoteException");
            return null;
        }
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public int[] getSampleBrightness() {
        return this.mSampleBrightness;
    }

    public int[] getSampleAlpha() {
        return this.mSampleAlphas;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setFPAuthState(boolean isAuthState) {
        HwPhoneWindowManager policy = (HwPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        if (policy != null) {
            Log.i(TAG_THREAD, "setFPAuthState:" + isAuthState);
            policy.getPhoneWindowManagerEx().setFPAuthState(isAuthState);
        }
    }

    private void getNotchState() {
        this.mDisplayNotchStatus = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "display_notch_status", 0, -2);
        Log.i(TAG, "getNotchState = " + this.mDisplayNotchStatus);
    }

    private void transferNotchRoundCorner(int status) {
        if (this.mDisplayNotchStatus == 1) {
            transferSwitchStatusToSurfaceFlinger(status);
        }
    }

    private void transferSwitchStatusToSurfaceFlinger(int value) {
        StringBuilder sb;
        Parcel dataIn = Parcel.obtain();
        try {
            IBinder sfBinder = ServiceManager.getService("SurfaceFlinger");
            dataIn.writeInt(value);
            if (sfBinder != null && !sfBinder.transact(NOTCH_ROUND_CORNER_CODE, dataIn, null, 1)) {
                Log.e(TAG, "transferSwitchStatusToSurfaceFlinger error!");
            }
            if (value == 0) {
                this.mIsNotchConerStatusChanged = true;
            } else {
                this.mIsNotchConerStatusChanged = false;
            }
            sb = new StringBuilder();
        } catch (RemoteException e) {
            Log.e(TAG, "transferSwitchStatusToSurfaceFlinger catch RemoteException");
            if (value == 0) {
                this.mIsNotchConerStatusChanged = true;
            } else {
                this.mIsNotchConerStatusChanged = false;
            }
            sb = new StringBuilder();
        } catch (Exception e2) {
            Log.e(TAG, "transferSwitchStatusToSurfaceFlinger catch Exception ");
            if (value == 0) {
                this.mIsNotchConerStatusChanged = true;
            } else {
                this.mIsNotchConerStatusChanged = false;
            }
            sb = new StringBuilder();
        } catch (Throwable th) {
            if (value == 0) {
                this.mIsNotchConerStatusChanged = true;
            } else {
                this.mIsNotchConerStatusChanged = false;
            }
            Log.i(TAG, "notch coner status change to = " + value);
            dataIn.recycle();
            throw th;
        }
        sb.append("notch coner status change to = ");
        sb.append(value);
        Log.i(TAG, sb.toString());
        dataIn.recycle();
    }

    public int getMaskAlpha(int currentLight) {
        int[] iArr;
        int alpha = 0;
        int[] iArr2 = this.mSampleBrightness;
        if (iArr2 == null || (iArr = this.mSampleAlphas) == null || iArr2.length == 0 || iArr.length == 0 || iArr2.length != iArr.length) {
            Log.i(TAG, "get Brightness and Alpha config error, use default config.");
            this.mSampleBrightness = sDefaultSampleBrightness;
            this.mSampleAlphas = sDefaultSampleAlpha;
        }
        int[] iArr3 = this.mSampleBrightness;
        if (currentLight > iArr3[iArr3.length - 1]) {
            Log.i(TAG, "currentLight:" + currentLight);
            return 0;
        }
        int i = 0;
        while (true) {
            int[] iArr4 = this.mSampleBrightness;
            if (i >= iArr4.length) {
                break;
            } else if (currentLight == iArr4[i]) {
                alpha = this.mSampleAlphas[i];
                break;
            } else if (currentLight >= iArr4[i]) {
                i++;
            } else if (i == 0) {
                alpha = this.mSampleAlphas[0];
            } else {
                int i2 = iArr4[i - 1];
                int[] iArr5 = this.mSampleAlphas;
                alpha = queryAlphaImpl(currentLight, i2, iArr5[i - 1], iArr4[i], iArr5[i]);
            }
        }
        if (alpha > this.mSampleAlphas[0] || alpha < 0) {
            alpha = 0;
        }
        Log.i(TAG, "alpha:" + alpha + ",currentLight:" + currentLight);
        return alpha;
    }

    private int queryAlphaImpl(int currLight, int preLevelLight, int preLevelAlpha, int lastLevelLight, int lastLevelAlpha) {
        return (((currLight - preLevelLight) * (lastLevelAlpha - preLevelAlpha)) / (lastLevelLight - preLevelLight)) + preLevelAlpha;
    }

    public void setBiometricServiceReceiver(IBiometricServiceReceiverInternal biometricServiceReceiver) {
        this.mBiometricServiceReceiver = biometricServiceReceiver;
    }

    public void setBiometricRequireConfirmation(boolean isBiometricConfirmation) {
        this.mIsBiometricRequireConfirmation = isBiometricConfirmation;
    }

    public void setBiometricPrompt(boolean isBiometricPrompt) {
        this.mIsBiometricPrompt = isBiometricPrompt;
    }
}
