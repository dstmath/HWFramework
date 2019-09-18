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
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.biometrics.IBiometricPromptReceiver;
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
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
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
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.mplink.HwMpLinkServiceImpl;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.systemui.shared.system.MetricsLoggerCompat;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.displayengine.DisplayEngineManager;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import huawei.android.aod.HwAodManager;
import huawei.android.hardware.fingerprint.FingerprintManagerEx;
import huawei.android.hwcolorpicker.HwColorPicker;
import huawei.com.android.server.fingerprint.FingerprintView;
import huawei.com.android.server.fingerprint.SingleModeContentObserver;
import huawei.com.android.server.fingerprint.SuspensionButton;
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
    private static final int BRIGHTNESS_LIFT_TIME_LONG = 200;
    private static final int BRIGHTNESS_LIFT_TIME_SHORT = 16;
    private static final int CIRCLE_LAYER = 2147483645;
    private static final int COLOR_BLACK = -16250872;
    private static final boolean DEBUG = true;
    private static int DEFAULT_BRIGHTNESS = 56;
    private static final int DEFAULT_INIT_HEIGHT = 2880;
    private static final int DEFAULT_INIT_WIDTH = 1440;
    private static int DEFAULT_LCD_DPI = 640;
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
    private static final String FINGRPRINT_IMAGE_TITLE_NAME = "hw_ud_fingerprint_image";
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
    private static final int INITIAL_BRIGHTNESS = -1;
    private static int INVALID_BRIGHTNESS = -1;
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
    private static final String[] PACKAGES_USE_CANCEL_HOTSPOT_INTERFACE = {"com.tencent.mm", "com.huawei.wallet"};
    private static final String[] PACKAGES_USE_HWAUTH_INTERFACE = {"com.huawei.hwid", "com.huawei.wallet", "com.huawei.android.hwpay"};
    private static String PANEL_INFO_NODE = "/sys/class/graphics/fb0/panel_info";
    public static final String PKGNAME_OF_KEYGUARD = "com.android.systemui";
    private static final String PKGNAME_OF_SECURITYMGR = "com.huawei.securitymgr";
    private static final String PKGNAME_OF_SYSTEMMANAGER = "com.huawei.systemmanager";
    private static final int RESULT_FINGERPRINT_AUTHENTICATION_CHECKING = 3;
    private static final int RESULT_FINGERPRINT_AUTHENTICATION_RESULT_FAIL = 1;
    private static final int RESULT_FINGERPRINT_AUTHENTICATION_RESULT_SUCCESS = 0;
    private static final int RESULT_FINGERPRINT_AUTHENTICATION_UNCHECKED = 2;
    private static final String TAG = "FingerViewController";
    public static final int TIME_UNIT = 1000;
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
    private static float mFingerPositionHeightScale = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private static int[] sDefaultSampleAlpha = {234, 229, HwMpLinkServiceImpl.MPLINK_MSG_MOBILE_DATA_SWITCH_OPEN, 220, HwMpLinkServiceImpl.MPLINK_MSG_MOBILE_DATA_DISCONNECTED, 211, HwMpLinkServiceImpl.MPLINK_MSG_WIFI_VPN_CONNETED, 205, 187, 176, HwSecDiagnoseConstant.OEMINFO_ID_DEVICE_RENEW, 163, 159, CPUFeature.MSG_SET_BG_UIDS, 140, 140, CPUFeature.MSG_SET_CPUSETCONFIG_VR, 121, 111, 101, 92, 81, 81, 69, 68, 58, 56, 46, 44, 35, 34, 30, 22, 23, 18, 0, 0};
    private static int[] sDefaultSampleBrightness = {4, 6, 8, 10, 12, 14, 16, 20, 24, 28, 30, 34, 40, 46, 50, 56, 64, 74, 84, 94, 104, 114, 124, 134, CPUFeature.MSG_RESET_VIP_THREAD, CPUFeature.MSG_RESET_ON_FIRE, 164, 174, 184, 194, 204, HwMpLinkServiceImpl.MPLINK_MSG_WIFI_DISCONNECTED, MetricsLoggerCompat.OVERVIEW_ACTIVITY, 234, 244, 248, 255};
    private static FingerViewController sInstance;
    /* access modifiers changed from: private */
    public boolean highLightViewAdded = false;
    private final Runnable mAddBackFingprintRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mAddBackFingprintRunnable");
            int unused = FingerViewController.this.mDefaultDisplayHeight = Settings.Global.getInt(FingerViewController.this.mContext.getContentResolver(), FingerViewController.APS_INIT_HEIGHT, FingerViewController.DEFAULT_INIT_HEIGHT);
            int unused2 = FingerViewController.this.mDefaultDisplayWidth = Settings.Global.getInt(FingerViewController.this.mContext.getContentResolver(), FingerViewController.APS_INIT_WIDTH, 1440);
            FingerViewController.this.createBackFingprintView();
        }
    };
    private final Runnable mAddButtonViewRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mAddButtonViewRunnable");
            FingerViewController.this.createAndAddButtonView();
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mAddFingerViewRunnable = new Runnable() {
        public void run() {
            FingerViewController.this.createFingerprintView();
            Log.i(FingerViewController.TAG, "begin mAddFingerViewRunnable");
        }
    };
    private final Runnable mAddImageOnlyRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mAddImageOnlyRunnable");
            if (FingerViewController.this.isNewMagazineViewForDownFP()) {
                FingerViewController.this.setFPAuthState(true);
            }
            int unused = FingerViewController.this.mDefaultDisplayHeight = Settings.Global.getInt(FingerViewController.this.mContext.getContentResolver(), FingerViewController.APS_INIT_HEIGHT, FingerViewController.DEFAULT_INIT_HEIGHT);
            int unused2 = FingerViewController.this.mDefaultDisplayWidth = Settings.Global.getInt(FingerViewController.this.mContext.getContentResolver(), FingerViewController.APS_INIT_WIDTH, 1440);
            FingerViewController.this.createImageOnlyView();
        }
    };
    /* access modifiers changed from: private */
    public BreathImageDrawable mAlipayDrawable;
    private HwAodManager mAodManager;
    private int mAuthenticateResult;
    private Bitmap mBLurBitmap;
    private Button mBackFingerprintCancelView;
    private TextView mBackFingerprintHintView;
    private Button mBackFingerprintUsePasswordView;
    /* access modifiers changed from: private */
    public BackFingerprintView mBackFingerprintView;
    private BitmapDrawable mBlurDrawable;
    private int mButtonCenterX = -1;
    private int mButtonCenterY = -1;
    private int mButtonColor = 0;
    /* access modifiers changed from: private */
    public SuspensionButton mButtonView;
    private boolean mButtonViewAdded = false;
    private int mButtonViewState = 1;
    private RelativeLayout mCancelView;
    private RelativeLayout mCancelViewImageOnly;
    /* access modifiers changed from: private */
    public ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentAlpha = 0;
    /* access modifiers changed from: private */
    public int mCurrentBrightness = -1;
    /* access modifiers changed from: private */
    public int mCurrentHeight;
    private int mCurrentRotation;
    /* access modifiers changed from: private */
    public int mCurrentWidth;
    /* access modifiers changed from: private */
    public int mDefaultDisplayHeight;
    /* access modifiers changed from: private */
    public int mDefaultDisplayWidth;
    /* access modifiers changed from: private */
    public IBiometricPromptReceiver mDialogReceiver;
    /* access modifiers changed from: private */
    public DisplayEngineManager mDisplayEngineManager;
    private int mDisplayNotchStatus = 0;
    /* access modifiers changed from: private */
    public FingerprintView mFingerView;
    /* access modifiers changed from: private */
    public ICallBack mFingerViewChangeCallback;
    private WindowManager.LayoutParams mFingerViewParams;
    /* access modifiers changed from: private */
    public FingerprintAnimationView mFingerprintAnimationView;
    /* access modifiers changed from: private */
    public int mFingerprintCenterX = -1;
    /* access modifiers changed from: private */
    public int mFingerprintCenterY = -1;
    /* access modifiers changed from: private */
    public FingerprintCircleOverlay mFingerprintCircleOverlay;
    private ImageView mFingerprintImageForAlipay;
    private FingerprintManagerEx mFingerprintManagerEx;
    /* access modifiers changed from: private */
    public FingerprintMaskOverlay mFingerprintMaskOverlay;
    private boolean mFingerprintOnlyViewAdded = false;
    /* access modifiers changed from: private */
    public int[] mFingerprintPosition = new int[4];
    /* access modifiers changed from: private */
    public ImageView mFingerprintView;
    private boolean mFingerprintViewAdded = false;
    private float mFontScale;
    /* access modifiers changed from: private */
    public Handler mHandler = null;
    private final HandlerThread mHandlerThread = new HandlerThread(TAG);
    /* access modifiers changed from: private */
    public boolean mHasBackFingerprint = false;
    private boolean mHasUdFingerprint = true;
    /* access modifiers changed from: private */
    public int mHbmType = 0;
    private int mHighLightRemoveType;
    /* access modifiers changed from: private */
    public int mHighLightShowType;
    /* access modifiers changed from: private */
    public HighLightMaskView mHighLightView;
    private final Runnable mHighLightViewRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mHighLightViewRunnable");
            int access$5300 = FingerViewController.this.mHighLightShowType;
            if (access$5300 != 5) {
                switch (access$5300) {
                    case 0:
                        Log.d(FingerViewController.TAG, " SETTINGS enter");
                        FingerViewController.this.createAndAddHighLightView();
                        FingerViewController.this.mHandler.postDelayed(FingerViewController.this.mSetEnrollLightLevelRunnable, 100);
                        if (FingerViewController.this.mHighLightView != null) {
                            int endAlpha = FingerViewController.this.getMaskAlpha(FingerViewController.this.mCurrentBrightness);
                            Log.d(FingerViewController.TAG, " alpha:" + endAlpha);
                            FingerViewController.this.startAlphaValueAnimation(FingerViewController.this.mHighLightView, true, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, ((float) endAlpha) / FingerViewController.MAX_BRIGHTNESS_LEVEL, 0, 200);
                            return;
                        }
                        return;
                    case 1:
                        Log.d(FingerViewController.TAG, " AUTHENTICATE enter");
                        FingerViewController.this.createAndAddHighLightView();
                        return;
                    default:
                        Log.d(FingerViewController.TAG, " default no operation");
                        return;
                }
            } else {
                Log.d(FingerViewController.TAG, " AUTHENTICATE on keyguard enter");
                FingerViewController.this.createMaskAndCircleOnKeyguard();
            }
        }
    };
    /* access modifiers changed from: private */
    public int mHighlightBrightnessLevel;
    private int mHighlightSpotColor;
    private int mHighlightSpotRadius;
    /* access modifiers changed from: private */
    public String mHint;
    private HintText mHintView;
    private boolean mIsCancelHotSpotPkgAdded = false;
    /* access modifiers changed from: private */
    public boolean mIsFingerFrozen = false;
    private boolean mIsKeygaurdCoverd;
    /* access modifiers changed from: private */
    public boolean mIsNeedReload;
    private boolean mIsSingleModeObserverRegistered = false;
    private boolean mKeepMaskAfterAuthentication = false;
    private KeyguardManager mKeyguardManager;
    private Button mLVBackFingerprintCancelView;
    private Button mLVBackFingerprintUsePasswordView;
    /* access modifiers changed from: private */
    public RelativeLayout mLayoutForAlipay;
    private LayoutInflater mLayoutInflater;
    /* access modifiers changed from: private */
    public float mMaxDigitalBrigtness;
    private RemainTimeCountDown mMyCountDown = null;
    private int mNormalizedMaxBrightness = INVALID_BRIGHTNESS;
    private int mNormalizedMinBrightness = INVALID_BRIGHTNESS;
    private boolean mNotchConerStatusChanged = false;
    private Bundle mPkgAttributes;
    /* access modifiers changed from: private */
    public String mPkgName;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public IFingerprintServiceReceiver mReceiver;
    private int mRemainTimes = 5;
    /* access modifiers changed from: private */
    public int mRemainedSecs;
    private RelativeLayout mRemoteView;
    /* access modifiers changed from: private */
    public final Runnable mRemoveBackFingprintRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mRemoveBackFingprintRunnable");
            FingerViewController.this.removeBackFingprintView();
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mRemoveButtonViewRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mRemoveButtonViewRunnable");
            FingerViewController.this.removeButtonView();
        }
    };
    private final Runnable mRemoveFingerViewRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mRemoveFingerViewRunnable");
            FingerViewController.this.removeFingerprintView();
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mRemoveHighLightView = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mRemoveHighLightView");
            FingerViewController.this.removeHighLightViewInner();
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mRemoveHighlightCircleRunnable = new Runnable() {
        public void run() {
            Log.d(FingerViewController.TAG, "RemoveHighlightCircle mHighLightShowType = " + FingerViewController.this.mHighLightShowType);
            if (FingerViewController.this.mFingerprintCircleOverlay != null && FingerViewController.this.mFingerprintCircleOverlay.isVisible()) {
                SurfaceControl.openTransaction();
                try {
                    FingerViewController.this.mFingerprintCircleOverlay.hide();
                } finally {
                    SurfaceControl.closeTransaction();
                }
            } else if (FingerViewController.this.highLightViewAdded && FingerViewController.this.mHighLightView != null && FingerViewController.this.mHighLightView.isAttachedToWindow()) {
                FingerViewController.this.mHighLightView.setCircleVisibility(4);
            }
        }
    };
    private final Runnable mRemoveImageOnlyRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mRemoveImageOnlyRunnable");
            FingerViewController.this.setFPAuthState(false);
            FingerViewController.this.removeImageOnlyView();
        }
    };
    private int[] mSampleAlpha = null;
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
    /* access modifiers changed from: private */
    public final Runnable mSetEnrollLightLevelRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mSetEnrollLightLevelRunnable");
            FingerViewController.this.setLightLevel(FingerViewController.this.mHighlightBrightnessLevel, 100);
        }
    };
    private final Runnable mSetScene = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mSetScene");
            if (FingerViewController.this.mDisplayEngineManager != null) {
                FingerViewController.this.mDisplayEngineManager.setScene(29, 0);
                Log.d(FingerViewController.TAG, "mDisplayEngineManager set scene 0");
            }
        }
    };
    private final Runnable mShowHighlightCircleRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mShowHighlightCircleRunnable, mHighLightShowType = " + FingerViewController.this.mHighLightShowType);
            if (FingerViewController.this.mHighLightShowType == 0) {
                Log.d(FingerViewController.TAG, "highLightViewAdded = " + FingerViewController.this.highLightViewAdded);
                if (FingerViewController.this.highLightViewAdded && FingerViewController.this.mHighLightView.getCircleVisibility() == 4) {
                    FingerViewController.this.mHandler.removeCallbacks(FingerViewController.this.mRemoveHighlightCircleRunnable);
                    FingerViewController.this.getCurrentFingerprintCenter();
                    FingerViewController.this.mHighLightView.setCenterPoints(FingerViewController.this.mFingerprintCenterX, FingerViewController.this.mFingerprintCenterY);
                    FingerViewController.this.mHighLightView.setCircleVisibility(0);
                    FingerViewController.this.mHandler.postDelayed(FingerViewController.this.mRemoveHighlightCircleRunnable, 1200);
                }
            }
        }
    };
    private SingleModeContentObserver mSingleContentObserver;
    /* access modifiers changed from: private */
    public String mSubTitle;
    /* access modifiers changed from: private */
    public WindowManager.LayoutParams mSuspensionButtonParams;
    /* access modifiers changed from: private */
    public final Runnable mUpdateButtonViewRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mUpdateButtonViewRunnable");
            FingerViewController.this.updateButtonView();
        }
    };
    private final Runnable mUpdateFingerprintViewRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mUpdateFingerprintViewRunnable");
            FingerViewController.this.updateFingerprintView();
        }
    };
    private final Runnable mUpdateFingprintRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mUpdateFingprintRunnable");
            FingerViewController.this.updateBackFingprintView();
        }
    };
    private final Runnable mUpdateImageOnlyRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mUpdateImageOnlyRunnable");
            FingerViewController.this.updateImageOnlyView();
        }
    };
    private final Runnable mUpdateMaskAttibuteRunnable = new Runnable() {
        public void run() {
            Log.i(FingerViewController.TAG, "begin mUpdateMaskAttibuteRunnable");
            FingerViewController.this.updateHintView(FingerViewController.this.mHint);
            if (FingerViewController.this.mFingerprintView != null) {
                FingerViewController.this.mFingerprintView.setContentDescription(FingerViewController.this.mHint);
            }
        }
    };
    private boolean mUseDefaultHint = true;
    private int mWidgetColor;
    /* access modifiers changed from: private */
    public boolean mWidgetColorSet = false;
    /* access modifiers changed from: private */
    public final WindowManager mWindowManager;
    private int mWindowType;
    private IPowerManager pm;
    private ContentObserver settingsDisplayObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange) {
            FingerViewController.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    Log.i(FingerViewController.TAG, "begin settingsDisplayObserver onchange");
                    int unused = FingerViewController.this.mDefaultDisplayHeight = Settings.Global.getInt(FingerViewController.this.mContentResolver, FingerViewController.APS_INIT_HEIGHT, FingerViewController.DEFAULT_INIT_HEIGHT);
                    int unused2 = FingerViewController.this.mDefaultDisplayWidth = Settings.Global.getInt(FingerViewController.this.mContentResolver, FingerViewController.APS_INIT_WIDTH, 1440);
                    int currentHeight = SystemPropertiesEx.getInt("persist.sys.rog.height", FingerViewController.this.mDefaultDisplayHeight);
                    int currentWidth = SystemPropertiesEx.getInt("persist.sys.rog.width", FingerViewController.this.mDefaultDisplayWidth);
                    if (FingerViewController.this.mCurrentHeight == currentHeight && FingerViewController.this.mCurrentWidth == currentWidth) {
                        Log.d(FingerViewController.TAG, "onChange: need reload display parameter");
                        boolean unused3 = FingerViewController.this.mIsNeedReload = true;
                    } else {
                        int unused4 = FingerViewController.this.mCurrentHeight = currentHeight;
                        int unused5 = FingerViewController.this.mCurrentWidth = currentWidth;
                        boolean unused6 = FingerViewController.this.mIsNeedReload = false;
                    }
                    Log.d(FingerViewController.TAG, "onChange:" + FingerViewController.this.mDefaultDisplayHeight + "," + FingerViewController.this.mDefaultDisplayWidth + "," + FingerViewController.this.mCurrentHeight + "," + FingerViewController.this.mCurrentWidth);
                }
            }, 30);
        }
    };

    private class FingerprintViewCallback implements FingerprintView.ICallBack {
        private FingerprintViewCallback() {
        }

        public void userActivity() {
            FingerViewController.this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        }

        public void onConfigurationChanged(Configuration newConfig) {
        }

        public void onDrawFinish() {
        }
    }

    public interface ICallBack {
        void onFingerViewStateChange(int i);

        void onNotifyBlueSpotDismiss();

        void onNotifyCaptureImage();
    }

    private class RemainTimeCountDown extends CountDownTimer {
        public RemainTimeCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void onTick(long millisUntilFinished) {
            int unused = FingerViewController.this.mRemainedSecs = (int) ((((double) millisUntilFinished) / 1000.0d) + 0.5d);
            if (FingerViewController.this.mRemainedSecs <= 0) {
                return;
            }
            if (FingerViewController.this.mFingerView != null && FingerViewController.this.mFingerView.isAttachedToWindow()) {
                FingerViewController.this.updateHintView(FingerViewController.this.mContext.getResources().getQuantityString(34406411, FingerViewController.this.mRemainedSecs, new Object[]{Integer.valueOf(FingerViewController.this.mRemainedSecs)}));
                FingerViewController.this.mFingerprintView.setContentDescription("");
            } else if (FingerViewController.this.mBackFingerprintView != null && FingerViewController.this.mBackFingerprintView.isAttachedToWindow()) {
                FingerViewController.this.updateBackFingerprintHintView(FingerViewController.this.mContext.getResources().getQuantityString(34406411, FingerViewController.this.mRemainedSecs, new Object[]{Integer.valueOf(FingerViewController.this.mRemainedSecs)}));
            }
        }

        public void onFinish() {
            Log.d(FingerViewController.TAG, "RemainTimeCountDown onFinish");
            boolean unused = FingerViewController.this.mIsFingerFrozen = false;
            if (FingerViewController.this.mFingerView != null && FingerViewController.this.mFingerView.isAttachedToWindow()) {
                if (FingerViewController.this.mHasBackFingerprint) {
                    FingerViewController.this.updateHintView(FingerViewController.this.mContext.getString(33686097));
                } else {
                    FingerViewController.this.updateHintView(FingerViewController.this.mContext.getString(33686103));
                }
                FingerViewController.this.mFingerprintView.setContentDescription(FingerViewController.this.mContext.getString(33686251));
            } else if (FingerViewController.this.mBackFingerprintView != null && FingerViewController.this.mBackFingerprintView.isAttachedToWindow()) {
                FingerViewController.this.updateBackFingerprintHintView(FingerViewController.this.mSubTitle);
            }
        }
    }

    private class SingleModeContentCallback implements SingleModeContentObserver.ICallBack {
        private SingleModeContentCallback() {
        }

        public void onContentChange() {
            if (((FingerViewController.this.mFingerprintView != null && FingerViewController.this.mFingerprintView.isAttachedToWindow()) || (FingerViewController.this.mLayoutForAlipay != null && FingerViewController.this.mLayoutForAlipay.isAttachedToWindow())) && !Settings.Global.getString(FingerViewController.this.mContext.getContentResolver(), "single_hand_mode").isEmpty()) {
                Settings.Global.putString(FingerViewController.this.mContext.getContentResolver(), "single_hand_mode", "");
            }
        }
    }

    private class SuspensionButtonCallback implements SuspensionButton.ICallBack {
        private SuspensionButtonCallback() {
        }

        public void onButtonViewMoved(float endX, float endY) {
            if (FingerViewController.this.mButtonView != null) {
                FingerViewController.this.mSuspensionButtonParams.x = (int) (endX - (((float) FingerViewController.this.mSuspensionButtonParams.width) * 0.5f));
                FingerViewController.this.mSuspensionButtonParams.y = (int) (endY - (((float) FingerViewController.this.mSuspensionButtonParams.height) * 0.5f));
                Log.d(FingerViewController.TAG, "onButtonViewUpdate,x = " + FingerViewController.this.mSuspensionButtonParams.x + " ,y = " + FingerViewController.this.mSuspensionButtonParams.y);
                FingerViewController.this.mWindowManager.updateViewLayout(FingerViewController.this.mButtonView, FingerViewController.this.mSuspensionButtonParams);
            }
        }

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
                    Log.d(FingerViewController.TAG, "catch exception");
                }
            }
            Context access$2500 = FingerViewController.this.mContext;
            Flog.bdReport(access$2500, 501, "{PkgName:" + FingerViewController.this.mPkgName + "}");
        }

        public String getCurrentApp() {
            return FingerViewController.this.mPkgName;
        }

        public void userActivity() {
            FingerViewController.this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        }

        public void onConfigurationChanged(Configuration newConfig) {
            FingerViewController.this.mHandler.post(FingerViewController.this.mUpdateButtonViewRunnable);
        }
    }

    private FingerViewController(Context context) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        this.mHandlerThread.start();
        this.mHandler = new HwExHandler(this.mHandlerThread.getLooper(), 500);
        this.mLayoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.pm = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mFingerprintManagerEx = new FingerprintManagerEx(this.mContext);
        this.mAodManager = HwAodManager.getInstance();
        this.mDisplayEngineManager = new DisplayEngineManager();
        this.mFingerprintMaskOverlay = new FingerprintMaskOverlay();
        this.mFingerprintCircleOverlay = new FingerprintCircleOverlay(context);
        this.mHbmType = getHbmType();
        getBrightnessRangeFromPanelInfo();
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        long identityToken = Binder.clearCallingIdentity();
        try {
            this.mContentResolver.registerContentObserver(Settings.Global.getUriFor(APS_INIT_HEIGHT), false, this.settingsDisplayObserver);
            this.mContentResolver.registerContentObserver(Settings.Global.getUriFor(APS_INIT_WIDTH), false, this.settingsDisplayObserver);
            this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("display_size_forced"), false, this.settingsDisplayObserver);
            this.settingsDisplayObserver.onChange(true);
            try {
                initBrightnessAlphaConfig();
            } catch (Exception e) {
                Log.e(TAG, "initBrightnessAlphaConfig fail ");
            }
            Configuration curConfig = new Configuration();
            try {
                curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
                this.mFontScale = curConfig.fontScale;
            } catch (RemoteException e2) {
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

    public void showMaskOrButton(String pkgName, Bundle bundle, IFingerprintServiceReceiver receiver, int type, boolean hasUdFingerprint, boolean hasBackFingerprint, IBiometricPromptReceiver dialogReceiver) {
        this.mPkgName = pkgName;
        this.mPkgAttributes = bundle;
        this.mWidgetColorSet = false;
        this.mHasUdFingerprint = hasUdFingerprint;
        this.mHasBackFingerprint = hasBackFingerprint;
        Log.d(TAG, "mPkgAttributes has been init, mPkgAttributes=" + bundle + "mHighlightBrightnessLevel = " + this.mHighlightBrightnessLevel);
        if (this.mPkgAttributes == null || this.mPkgAttributes.getString("SystemTitle") == null) {
            this.mUseDefaultHint = true;
        } else {
            this.mUseDefaultHint = false;
        }
        this.mReceiver = receiver;
        this.mDialogReceiver = dialogReceiver;
        if ("com.huawei.systemmanager".equals(pkgName)) {
            Log.d(TAG, "do not show mask for systemmanager");
        } else if (PKGNAME_OF_SECURITYMGR.equals(pkgName)) {
            Log.d(TAG, "do not show mask for securitymgr");
        } else {
            Log.d(TAG, "type of package " + pkgName + " is " + type);
            if (this.mPkgName == null || !this.mPkgName.equals(PKGNAME_OF_KEYGUARD)) {
                this.mWindowType = HwArbitrationDEFS.MSG_VPN_STATE_OPEN;
                if (type == 0) {
                    this.mHandler.post(this.mAddFingerViewRunnable);
                    Context context = this.mContext;
                    Flog.bdReport(context, 501, "{PkgName:" + this.mPkgName + "}");
                } else if (type == 1) {
                    this.mHandler.post(this.mAddButtonViewRunnable);
                } else if (type == 3) {
                    this.mIsCancelHotSpotPkgAdded = isCancelHotSpotViewVisble(this.mPkgName);
                    Log.d(TAG, "isCancelHotSpotNeed(mPkgName: " + this.mPkgName + " mIsCancelHotSpotPkgAdded: " + this.mIsCancelHotSpotPkgAdded);
                    this.mHandler.post(this.mAddImageOnlyRunnable);
                } else if (type == 4) {
                    this.mHandler.post(this.mAddBackFingprintRunnable);
                }
                return;
            }
            this.mWindowType = 2014;
        }
    }

    public void showMaskForApp(Bundle attribute) {
        this.mPkgName = getForegroundPkgName();
        this.mPkgAttributes = attribute;
        if (this.mPkgName == null || !this.mPkgName.equals(PKGNAME_OF_KEYGUARD)) {
            this.mWindowType = HwArbitrationDEFS.MSG_VPN_STATE_OPEN;
        } else {
            this.mWindowType = 2014;
        }
        if (this.mPkgAttributes == null || this.mPkgAttributes.getString("SystemTitle") == null) {
            this.mUseDefaultHint = true;
        } else {
            this.mUseDefaultHint = false;
        }
        this.mHandler.post(this.mAddFingerViewRunnable);
    }

    public void showSuspensionButtonForApp(int centerX, int centerY, String callingUidName) {
        Log.d(TAG, "mButtonCenterX = " + this.mButtonCenterX + ",mButtonCenterY =" + this.mButtonCenterY + ",callingUidName = " + callingUidName);
        this.mButtonCenterX = centerX;
        this.mButtonCenterY = centerY;
        if (UIDNAME_OF_KEYGUARD.equals(callingUidName)) {
            this.mPkgName = PKGNAME_OF_KEYGUARD;
        }
        Log.d(TAG, "mButtonCenterX = " + this.mButtonCenterX + ",mButtonCenterY =" + this.mButtonCenterY);
        this.mWindowType = HwArbitrationDEFS.MSG_VPN_STATE_OPEN;
        this.mPkgAttributes = null;
        if (this.mFingerViewChangeCallback != null) {
            this.mFingerViewChangeCallback.onFingerViewStateChange(2);
        }
        this.mHandler.post(this.mAddButtonViewRunnable);
    }

    public void removeMaskOrButton() {
        this.mHandler.post(this.mRemoveFingerViewRunnable);
        this.mHandler.post(this.mRemoveButtonViewRunnable);
        this.mHandler.post(this.mRemoveImageOnlyRunnable);
        this.mHandler.post(this.mRemoveBackFingprintRunnable);
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
            if (this.mFingerprintViewAdded && hint != null) {
                Log.d(TAG, "updateMaskViewAttributes,hint = " + hint);
                this.mHint = hint;
                this.mHandler.post(this.mUpdateMaskAttibuteRunnable);
            }
        }
    }

    public void updateFingerprintView(int result, boolean keepMaskAfterAuthentication) {
        this.mAuthenticateResult = result;
        this.mKeepMaskAfterAuthentication = keepMaskAfterAuthentication;
        Log.d(TAG, "mUseDefaultHint = " + this.mUseDefaultHint);
        if (this.mFingerView != null && this.mFingerView.isAttachedToWindow()) {
            this.mHandler.post(this.mUpdateFingerprintViewRunnable);
        } else if (this.mBackFingerprintView != null && this.mBackFingerprintView.isAttachedToWindow()) {
            this.mHandler.post(this.mUpdateFingprintRunnable);
        }
    }

    public void updateFingerprintView(int result, int failTimes) {
        if (result != 2) {
            this.mRemainTimes = 5 - failTimes;
        }
        this.mAuthenticateResult = result;
        if (this.mFingerView != null && this.mFingerView.isAttachedToWindow()) {
            this.mHandler.post(this.mUpdateFingerprintViewRunnable);
        } else if (this.mBackFingerprintView != null && this.mBackFingerprintView.isAttachedToWindow()) {
            this.mHandler.post(this.mUpdateFingprintRunnable);
        }
        if (this.mLayoutForAlipay != null && this.mLayoutForAlipay.isAttachedToWindow()) {
            this.mHandler.post(this.mUpdateImageOnlyRunnable);
        }
    }

    public void showHighlightview(int type) {
        if (!isScreenOn() || this.highLightViewAdded) {
            Log.d(TAG, "Screen not on or already added");
            return;
        }
        this.mHighLightShowType = type;
        Log.d(TAG, "show Highlightview mHighLightShowType:" + this.mHighLightShowType);
        this.mHandler.removeCallbacks(this.mHighLightViewRunnable);
        this.mHandler.removeCallbacks(this.mRemoveHighLightView);
        this.mHandler.post(this.mHighLightViewRunnable);
        if (type == 1) {
            this.mHandler.postDelayed(this.mRemoveHighLightView, 1200);
        }
    }

    public void showHighlightviewOnKeyguard() {
        this.mHighLightShowType = 5;
        Log.d(TAG, "show Highlightview mHighLightShowType:" + this.mHighLightShowType);
        this.mHandler.removeCallbacks(this.mHighLightViewRunnable);
        this.mHandler.post(this.mHighLightViewRunnable);
    }

    public void removeHighlightviewOnKeyguard() {
        this.mHandler.post(new Runnable() {
            public void run() {
                SurfaceControl.openTransaction();
                try {
                    if (FingerViewController.this.mHbmType == 0) {
                        FingerViewController.this.mFingerprintMaskOverlay.hide();
                    }
                    FingerViewController.this.mFingerprintCircleOverlay.hide();
                    if (FingerViewController.this.mFingerprintAnimationView != null && FingerViewController.this.mFingerprintAnimationView.isAttachedToWindow()) {
                        FingerViewController.this.mFingerprintAnimationView.setAddState(false);
                        FingerViewController.this.mWindowManager.removeView(FingerViewController.this.mFingerprintAnimationView);
                    }
                } finally {
                    SurfaceControl.closeTransaction();
                }
            }
        });
    }

    public void destroyHighlightviewOnKeyguard() {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (FingerViewController.this.mHbmType == 1) {
                    SurfaceControl.openTransaction();
                    try {
                        FingerViewController.this.mFingerprintMaskOverlay.hide();
                    } finally {
                        SurfaceControl.closeTransaction();
                    }
                }
                FingerViewController.this.mFingerprintMaskOverlay.destroy();
                FingerViewController.this.mFingerprintCircleOverlay.destroy();
            }
        });
    }

    public void showHighlightCircleOnKeyguard() {
        if (!isScreenOn()) {
            Log.i(TAG, "Screen not on, return");
        } else {
            this.mHandler.post(new Runnable() {
                /* JADX INFO: finally extract failed */
                public void run() {
                    SurfaceControl.openTransaction();
                    try {
                        if (!FingerViewController.this.mFingerprintCircleOverlay.isCreate()) {
                            Log.i(FingerViewController.TAG, "circle not created, create it before show");
                            float scale = FingerViewController.this.getPxScale();
                            int unused = FingerViewController.this.mFingerprintCenterX = (FingerViewController.this.mFingerprintPosition[0] + FingerViewController.this.mFingerprintPosition[2]) / 2;
                            int unused2 = FingerViewController.this.mFingerprintCenterY = (FingerViewController.this.mFingerprintPosition[1] + FingerViewController.this.mFingerprintPosition[3]) / 2;
                            FingerViewController.this.mFingerprintCircleOverlay.create(FingerViewController.this.mFingerprintCenterX, FingerViewController.this.mFingerprintCenterY, scale);
                        }
                        FingerViewController.this.mFingerprintCircleOverlay.setLayer(FingerViewController.CIRCLE_LAYER);
                        FingerViewController.this.mFingerprintCircleOverlay.show();
                        if (FingerViewController.this.mHbmType == 0) {
                            FingerViewController.this.getBrightness();
                            if (!FingerViewController.this.mFingerprintMaskOverlay.isCreate()) {
                                Log.i(FingerViewController.TAG, "mask not created, create it before show");
                                FingerViewController.this.mFingerprintMaskOverlay.create(FingerViewController.this.mCurrentWidth, FingerViewController.this.mCurrentHeight, FingerViewController.this.mHbmType);
                            }
                            int unused3 = FingerViewController.this.mCurrentAlpha = FingerViewController.this.getMaskAlpha(FingerViewController.this.mCurrentBrightness);
                            FingerViewController.this.setBacklightViaAod((float) ((int) FingerViewController.this.mMaxDigitalBrigtness), FingerViewController.this.transformBrightnessViaScreen(FingerViewController.this.mCurrentBrightness));
                            FingerViewController.this.mFingerprintMaskOverlay.setLayer(FingerViewController.MASK_LAYER);
                            FingerViewController.this.mFingerprintMaskOverlay.setAlpha(((float) FingerViewController.this.mCurrentAlpha) / FingerViewController.MAX_BRIGHTNESS_LEVEL);
                            FingerViewController.this.mFingerprintMaskOverlay.show();
                        }
                        SurfaceControl.closeTransaction();
                        if (FingerViewController.this.mFingerprintAnimationView == null) {
                            FingerprintAnimationView unused4 = FingerViewController.this.mFingerprintAnimationView = new FingerprintAnimationView(FingerViewController.this.mContext);
                        }
                        FingerViewController.this.mFingerprintAnimationView.setCenterPoints(FingerViewController.this.mFingerprintCenterX, FingerViewController.this.mFingerprintCenterY);
                        FingerViewController.this.mFingerprintAnimationView.setScale(FingerViewController.this.getPxScale());
                        if (!FingerViewController.this.mFingerprintAnimationView.isAdded()) {
                            FingerViewController.this.mFingerprintAnimationView.setAddState(true);
                            FingerViewController.this.mWindowManager.addView(FingerViewController.this.mFingerprintAnimationView, FingerViewController.this.mFingerprintAnimationView.getViewParams());
                        }
                    } catch (Throwable th) {
                        SurfaceControl.closeTransaction();
                        throw th;
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void createMaskAndCircleOnKeyguard() {
        this.mFingerprintMaskOverlay.create(this.mCurrentWidth, this.mCurrentHeight, this.mHbmType);
        this.mFingerprintCenterX = (this.mFingerprintPosition[0] + this.mFingerprintPosition[2]) / 2;
        this.mFingerprintCenterY = (this.mFingerprintPosition[1] + this.mFingerprintPosition[3]) / 2;
        this.mFingerprintCircleOverlay.create(this.mFingerprintCenterX, this.mFingerprintCenterY, getPxScale());
        if (this.mHbmType == 1) {
            boolean isKeyguardLocked = this.mKeyguardManager.isKeyguardLocked();
            Log.i(TAG, "createMaskAndCircleOnKeyguard isKeyguardLocked = " + isKeyguardLocked);
            if (isKeyguardLocked) {
                setBacklightViaAod((float) ((int) this.mMaxDigitalBrigtness), (float) (((int) this.mMaxDigitalBrigtness) + 1));
                this.mFingerprintMaskOverlay.setLayer(MASK_LAYER);
                this.mFingerprintMaskOverlay.setAlpha(((float) this.mCurrentAlpha) / MAX_BRIGHTNESS_LEVEL);
                this.mFingerprintMaskOverlay.show();
                return;
            }
            SurfaceControl.openTransaction();
            try {
                this.mFingerprintMaskOverlay.setLayer(MASK_LAYER);
                this.mFingerprintMaskOverlay.setAlpha(MIN_ALPHA);
                this.mFingerprintMaskOverlay.show();
            } finally {
                SurfaceControl.closeTransaction();
            }
        }
    }

    public void removeHighlightview(int type) {
        Log.d(TAG, "removeHighlightview mHighLightRemoveType:" + type);
        this.mHighLightRemoveType = type;
        if (this.highLightViewAdded) {
            this.mHandler.removeCallbacks(this.mRemoveHighLightView);
        }
        this.mHandler.removeCallbacks(this.mHighLightViewRunnable);
        if (type == 0) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    Log.i(FingerViewController.TAG, "begin anonymous runnable in removeHighlightview");
                    FingerViewController.this.mHandler.removeCallbacks(FingerViewController.this.mSetEnrollLightLevelRunnable);
                    FingerViewController.this.setLightLevel(-1, 150);
                    if (FingerViewController.this.mHighLightView != null) {
                        FingerViewController.this.startAlphaValueAnimation(FingerViewController.this.mHighLightView, false, FingerViewController.this.mHighLightView.getAlpha() / FingerViewController.MAX_BRIGHTNESS_LEVEL, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 70, 80);
                    }
                }
            });
        } else {
            this.mHandler.post(this.mRemoveHighLightView);
        }
    }

    private void initFingerPrintViewSubContentDes() {
        if (this.mFingerprintView != null) {
            this.mFingerprintView.setContentDescription(this.mContext.getString(33686251));
        }
        if (this.mCancelView != null) {
            this.mCancelView.setContentDescription(this.mContext.getString(33686252));
        }
    }

    private void initAddButtonViwSubContentDes() {
        if (this.mButtonView != null) {
            this.mButtonView.setContentDescription(this.mContext.getString(33686253));
        }
    }

    /* access modifiers changed from: private */
    public void createFingerprintView() {
        initBaseElement();
        updateBaseElementMargins();
        updateExtraElement();
        initFingerprintViewParams();
        Log.d(TAG, "createFingerprintView called ,reset Hint");
        this.mFingerViewParams.type = this.mWindowType;
        Log.d(TAG, "fingerviewadded,mWidgetColor = " + this.mWidgetColor);
        initFingerPrintViewSubContentDes();
        if (!this.mFingerprintViewAdded) {
            startBlurScreenshot();
            this.mWidgetColor = HwColorPicker.processBitmap(this.mScreenShot).getWidgetColor();
            this.mWidgetColorSet = true;
            this.mWindowManager.addView(this.mFingerView, this.mFingerViewParams);
            this.mFingerprintViewAdded = true;
            exitSingleHandMode();
            registerSingerHandObserver();
            getNotchState();
            transferNotchRoundCorner(0);
            Log.d(TAG, "addFingerprintView is done,is View Added = " + this.mFingerprintViewAdded);
        }
    }

    public boolean getFingerPrintRealHeightScale() {
        int fingerprintPositionHight = (this.mFingerprintPosition[1] + this.mFingerprintPosition[3]) / 2;
        if (this.mDefaultDisplayHeight != 0) {
            mFingerPositionHeightScale = (((float) fingerprintPositionHight) * 1.0f) / ((float) this.mDefaultDisplayHeight);
        }
        Log.d(TAG, "isNewMagazineViewForDownFP,mFingerPositionHeightScale:" + mFingerPositionHeightScale);
        return true;
    }

    public boolean isNewMagazineViewForDownFP() {
        if (mFingerPositionHeightScale == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            getFingerPrintRealHeightScale();
        }
        return mFingerPositionHeightScale > LV_FINGERPRINT_POSITION_VIEW_HIGHT_SCALE;
    }

    /* access modifiers changed from: private */
    public void backFingerprintUsePasswordViewOnclick() {
        Log.d(TAG, "onClick,UsePassword");
        onUsePasswordClick();
        String foregroundPkgName = getForegroundPkgName();
        Log.d(TAG, "foregroundPackageName = " + foregroundPkgName);
        if (foregroundPkgName != null && isBroadcastNeed(foregroundPkgName)) {
            Intent usePasswordIntent = new Intent(FINGERPRINT_IN_DISPLAY);
            usePasswordIntent.setPackage(foregroundPkgName);
            usePasswordIntent.putExtra(FINGERPRINT_IN_DISPLAY_HELPCODE_KEY, 1010);
            usePasswordIntent.putExtra(FINGERPRINT_IN_DISPLAY_HELPSTRING_KEY, FINGERPRINT_IN_DISPLAY_HELPSTRING_USE_PASSWORD_VALUE);
            this.mContext.sendBroadcast(usePasswordIntent);
        } else if (this.mDialogReceiver != null) {
            try {
                this.mDialogReceiver.onDialogDismissed(1);
            } catch (RemoteException e) {
                Log.d(TAG, "catch exception");
            }
        } else if (this.mReceiver != null) {
            try {
                this.mReceiver.onError(0, 10, 0);
            } catch (RemoteException e2) {
                Log.d(TAG, "catch exception");
            }
        }
    }

    /* access modifiers changed from: private */
    public void cancelHotSpotViewOnclick() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        this.mHandler.post(this.mRemoveImageOnlyRunnable);
        sendKeyEvent();
    }

    /* access modifiers changed from: private */
    public void backFingerprintCancelViewOnclick() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        onCancelClick();
        Context context = this.mContext;
        Flog.bdReport(context, 502, "{PkgName:" + this.mPkgName + "}");
        String foregroundPkgName = getForegroundPkgName();
        if (foregroundPkgName != null && isBroadcastNeed(foregroundPkgName)) {
            Intent cancelMaskIntent = new Intent(FINGERPRINT_IN_DISPLAY);
            cancelMaskIntent.putExtra(FINGERPRINT_IN_DISPLAY_HELPCODE_KEY, 1011);
            cancelMaskIntent.putExtra(FINGERPRINT_IN_DISPLAY_HELPSTRING_KEY, FINGERPRINT_IN_DISPLAY_HELPSTRING_CLOSE_VIEW_VALUE);
            cancelMaskIntent.setPackage(foregroundPkgName);
            this.mContext.sendBroadcast(cancelMaskIntent);
        } else if (this.mDialogReceiver != null) {
            try {
                this.mDialogReceiver.onDialogDismissed(2);
            } catch (RemoteException e) {
                Log.d(TAG, "catch exception");
            }
        } else if (this.mReceiver != null) {
            try {
                this.mReceiver.onAcquired(0, 6, 11);
            } catch (RemoteException e2) {
                Log.d(TAG, "catch exception");
            }
        }
    }

    private void initBaseElement() {
        int curRotation = this.mWindowManager.getDefaultDisplay().getRotation();
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
        int dpi = SystemProperties.getInt("persist.sys.dpi", lcdDpi);
        if (this.mFingerView != null && this.mCurrentHeight == this.mSavedMaskHeight && curRotation == this.mSavedRotation && dpi == this.mSavedMaskDpi) {
            Log.d(TAG, "don't need to inflate mFingerView again");
            return;
        }
        Log.d(TAG, "dpi or rotation has changed, mCurrentHeight = " + this.mCurrentHeight + ", mSavedMaskHeight = " + this.mSavedMaskHeight + ",inflate mFingerView");
        this.mSavedMaskDpi = dpi;
        this.mSavedMaskHeight = this.mCurrentHeight;
        this.mSavedRotation = curRotation;
        if (isNewMagazineViewForDownFP()) {
            this.mFingerView = (FingerprintView) this.mLayoutInflater.inflate(34013355, null);
            Log.d(TAG, " add inflate mLVFingerView!!");
        } else {
            this.mFingerView = (FingerprintView) this.mLayoutInflater.inflate(34013296, null);
        }
        this.mFingerView.setCallback(new FingerprintViewCallback());
        this.mFingerprintView = (ImageView) this.mFingerView.findViewById(34603059);
        this.mRemoteView = (RelativeLayout) this.mFingerView.findViewById(34603382);
        if (isNewMagazineViewForDownFP()) {
            float dpiScale = (((float) lcdDpi) * 1.0f) / ((float) dpi);
            this.mLVBackFingerprintUsePasswordView = (Button) this.mFingerView.findViewById(34603325);
            this.mLVBackFingerprintCancelView = (Button) this.mFingerView.findViewById(34603324);
            RelativeLayout buttonLayout = (RelativeLayout) this.mFingerView.findViewById(34603323);
            if (buttonLayout != null) {
                ViewGroup.LayoutParams buttonLayoutParams = buttonLayout.getLayoutParams();
                if (buttonLayoutParams instanceof RelativeLayout.LayoutParams) {
                    buttonLayoutParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472464)) * dpiScale) + 0.5f);
                    buttonLayoutParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472463)) * dpiScale) + 0.5f);
                    ((RelativeLayout.LayoutParams) buttonLayoutParams).bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472462)) * dpiScale) + 0.5f);
                    buttonLayout.setLayoutParams(buttonLayoutParams);
                }
            }
            if (this.mLVBackFingerprintUsePasswordView != null) {
                this.mLVBackFingerprintUsePasswordView.setTextSize(0, (float) ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472467)) * dpiScale) + 0.5f)));
                ViewGroup.LayoutParams usePasswordViewParams = this.mLVBackFingerprintUsePasswordView.getLayoutParams();
                if (usePasswordViewParams instanceof RelativeLayout.LayoutParams) {
                    usePasswordViewParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472241)) * dpiScale) + 0.5f);
                    usePasswordViewParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472122)) * dpiScale) + 0.5f);
                    getCurrentRotation();
                    if (this.mCurrentRotation == 1 || this.mCurrentRotation == 3) {
                        ((RelativeLayout.LayoutParams) usePasswordViewParams).topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472465)) * dpiScale) + 0.5f);
                    }
                    this.mLVBackFingerprintUsePasswordView.setLayoutParams(usePasswordViewParams);
                    Log.i(TAG, "zc initBaseElement usePasswordViewParams " + usePasswordViewParams.width + " " + usePasswordViewParams.height);
                }
                this.mLVBackFingerprintUsePasswordView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        FingerViewController.this.backFingerprintUsePasswordViewOnclick();
                    }
                });
            }
            if (this.mLVBackFingerprintCancelView != null) {
                this.mLVBackFingerprintCancelView.setTextSize(0, (float) ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472467)) * dpiScale) + 0.5f)));
                ViewGroup.LayoutParams cancelViewParams = this.mLVBackFingerprintCancelView.getLayoutParams();
                if (cancelViewParams instanceof RelativeLayout.LayoutParams) {
                    cancelViewParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472241)) * dpiScale) + 0.5f);
                    cancelViewParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472122)) * dpiScale) + 0.5f);
                    getCurrentRotation();
                    if (this.mCurrentRotation == 1 || this.mCurrentRotation == 3) {
                        ((RelativeLayout.LayoutParams) cancelViewParams).topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472469)) * dpiScale) + 0.5f);
                    }
                    this.mLVBackFingerprintCancelView.setLayoutParams(cancelViewParams);
                }
                this.mLVBackFingerprintCancelView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        FingerViewController.this.backFingerprintCancelViewOnclick();
                    }
                });
            }
        } else {
            this.mCancelView = (RelativeLayout) this.mFingerView.findViewById(34603034);
            this.mCancelView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    FingerViewController.this.backFingerprintCancelViewOnclick();
                }
            });
        }
    }

    private void updateBaseElementMargins() {
        getCurrentRotation();
        int[] fingerprintMargin = calculateFingerprintMargin();
        RelativeLayout relativeLayout = (RelativeLayout) this.mFingerView.findViewById(34603021);
        ViewGroup.MarginLayoutParams fingerviewLayoutParams = (ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams();
        float dpiScale = getDPIScale();
        if (isNewMagazineViewForDownFP()) {
            Log.i(TAG, "fingerviewLayoutParams.width = " + fingerviewLayoutParams.width + ",fingerviewLayoutParams.");
            ViewGroup.MarginLayoutParams fingerprintImageParams = (ViewGroup.MarginLayoutParams) this.mFingerprintView.getLayoutParams();
            fingerprintImageParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472321)) * dpiScale) + 0.5f);
            fingerprintImageParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472320)) * dpiScale) + 0.5f);
            fingerprintImageParams.leftMargin = fingerprintMargin[0];
            fingerprintImageParams.topMargin = fingerprintMargin[1];
            Log.i(TAG, "zc fingerprintViewParams.width = " + fingerprintImageParams.width + ", fingerprintViewParams.height = " + fingerprintImageParams.height);
            this.mFingerprintView.setLayoutParams(fingerprintImageParams);
            if (this.mCurrentRotation == 1 || this.mCurrentRotation == 3) {
                fingerviewLayoutParams.topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472461)) * dpiScale) + 0.5f);
                fingerviewLayoutParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472474)) * dpiScale) + 0.5f);
                relativeLayout.setLayoutParams(fingerviewLayoutParams);
                ViewGroup.MarginLayoutParams remoteViewLayoutParams = (ViewGroup.MarginLayoutParams) this.mRemoteView.getLayoutParams();
                remoteViewLayoutParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472474)) * dpiScale) + 0.5f);
                remoteViewLayoutParams.leftMargin = calculateRemoteViewLeftMargin(fingerviewLayoutParams.width);
                remoteViewLayoutParams.topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472496)) * dpiScale) + 0.5f);
                remoteViewLayoutParams.rightMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472495)) * dpiScale) + 0.5f);
                remoteViewLayoutParams.bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472492)) * dpiScale) + 0.5f);
                this.mRemoteView.setLayoutParams(remoteViewLayoutParams);
            }
        } else if (this.mCurrentRotation == 1 || this.mCurrentRotation == 3) {
            fingerviewLayoutParams.width = (fingerprintMargin[0] * 2) + ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472320)) * dpiScale) + 0.5f));
            int[] layoutMargin = calculateFingerprintLayoutLeftMargin(fingerviewLayoutParams.width);
            fingerviewLayoutParams.leftMargin = layoutMargin[0];
            fingerviewLayoutParams.rightMargin = layoutMargin[1];
            relativeLayout.setLayoutParams(fingerviewLayoutParams);
            ViewGroup.MarginLayoutParams remoteViewLayoutParams2 = (ViewGroup.MarginLayoutParams) this.mRemoteView.getLayoutParams();
            int remoteViewLeftLayoutmargin = calculateRemoteViewLeftMargin(fingerviewLayoutParams.width);
            remoteViewLayoutParams2.width = (int) ((((((float) this.mCurrentHeight) - (((float) (this.mContext.getResources().getDimensionPixelSize(34472493) * 2)) * dpiScale)) - (((float) this.mContext.getResources().getDimensionPixelSize(34472475)) * dpiScale)) - ((float) fingerviewLayoutParams.width)) + 0.5f);
            remoteViewLayoutParams2.leftMargin = remoteViewLeftLayoutmargin;
            remoteViewLayoutParams2.topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472496)) * dpiScale) + 0.5f);
            remoteViewLayoutParams2.rightMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472495)) * dpiScale) + 0.5f);
            remoteViewLayoutParams2.bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472492)) * dpiScale) + 0.5f);
            this.mRemoteView.setLayoutParams(remoteViewLayoutParams2);
            Log.d(TAG, " RemoteviewLayoutParams.leftMargin =" + remoteViewLayoutParams2.leftMargin + ",rightMargin =" + remoteViewLayoutParams2.rightMargin);
        } else {
            fingerviewLayoutParams.width = this.mCurrentWidth;
            fingerviewLayoutParams.leftMargin = 0;
        }
        ViewGroup.MarginLayoutParams fingerprintImageParams2 = (ViewGroup.MarginLayoutParams) this.mFingerprintView.getLayoutParams();
        fingerprintImageParams2.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472321)) * dpiScale) + 0.5f);
        fingerprintImageParams2.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472320)) * dpiScale) + 0.5f);
        fingerprintImageParams2.leftMargin = fingerprintMargin[0];
        fingerprintImageParams2.topMargin = fingerprintMargin[1];
        Log.d(TAG, "fingerprintViewParams.width = " + fingerprintImageParams2.width + ", fingerprintViewParams.height = " + fingerprintImageParams2.height);
        this.mFingerprintView.setLayoutParams(fingerprintImageParams2);
        if (!isNewMagazineViewForDownFP()) {
            Log.d(TAG, "updateBaseElementMargins mFingerprintCenterX = " + ((this.mFingerprintPosition[1] + this.mFingerprintPosition[3]) / 2));
            int fingerprintPosition = (this.mFingerprintPosition[1] + this.mFingerprintPosition[3]) / 2;
            int cancelButtonsize = this.mContext.getResources().getDimensionPixelSize(34472266);
            ViewGroup.MarginLayoutParams cancelViewParams = (ViewGroup.MarginLayoutParams) this.mCancelView.getLayoutParams();
            if (fingerprintPosition == FINGERPRINT_IN_DISPLAY_POSITION_VIEW_VALUE_ELLE || fingerprintPosition == FINGERPRINT_IN_DISPLAY_POSITION_VIEW_VALUE_VOGUE) {
                cancelButtonsize = this.mContext.getResources().getDimensionPixelSize(34472267);
            }
            if (this.mCurrentRotation == 1 || this.mCurrentRotation == 3) {
                cancelViewParams.topMargin = (int) (((((float) this.mCurrentWidth) - (((float) cancelButtonsize) * dpiScale)) - (((float) this.mContext.getResources().getDimensionPixelSize(34472537)) * dpiScale)) + 0.5f);
            } else {
                cancelViewParams.topMargin = (int) (((((float) this.mCurrentHeight) - (((float) cancelButtonsize) * dpiScale)) - (((float) this.mContext.getResources().getDimensionPixelSize(34472537)) * dpiScale)) + 0.5f);
            }
            ImageView cancelImage = (ImageView) this.mCancelView.findViewById(34603032);
            ViewGroup.MarginLayoutParams cancelImageParams = (ViewGroup.MarginLayoutParams) cancelImage.getLayoutParams();
            cancelImageParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472536)) * dpiScale) + 0.5f);
            cancelImageParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472536)) * dpiScale) + 0.5f);
            cancelImage.setLayoutParams(cancelImageParams);
            Log.d(TAG, "cancelViewParams.topMargin = " + cancelViewParams.topMargin);
            cancelViewParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472317)) * dpiScale) + 0.5f);
            cancelViewParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472316)) * dpiScale) + 0.5f);
            this.mCancelView.setLayoutParams(cancelViewParams);
        }
    }

    private void updateExtraElement() {
        float dpiScale = getDPIScale();
        RelativeLayout usePasswordHotSpot = (RelativeLayout) this.mFingerView.findViewById(34603435);
        TextView usePasswordView = (TextView) this.mFingerView.findViewById(34603024);
        RelativeLayout usePasswordHotspotLayout = (RelativeLayout) this.mFingerView.findViewById(34603435);
        RelativeLayout titleAndSummaryView = (RelativeLayout) this.mFingerView.findViewById(34603023);
        ViewGroup.MarginLayoutParams titleAndSummaryViewParams = (ViewGroup.MarginLayoutParams) titleAndSummaryView.getLayoutParams();
        if (isNewMagazineViewForDownFP() && (this.mCurrentRotation == 1 || this.mCurrentRotation == 3)) {
            titleAndSummaryViewParams.topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472121)) * dpiScale) + 0.5f);
        }
        titleAndSummaryViewParams.bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472121)) * dpiScale) + 0.5f);
        titleAndSummaryView.setLayoutParams(titleAndSummaryViewParams);
        TextView appNameView = (TextView) this.mFingerView.findViewById(34603022);
        appNameView.setTextSize(0, (float) ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472120)) * dpiScale) + 0.5f)));
        TextView accountMessageView = (TextView) this.mFingerView.findViewById(34603008);
        accountMessageView.setTextSize(0, (float) ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34471966)) * dpiScale) + 0.5f)));
        ViewGroup.MarginLayoutParams accountMessageViewParams = (ViewGroup.MarginLayoutParams) accountMessageView.getLayoutParams();
        accountMessageViewParams.topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472083)) * dpiScale) + 0.5f);
        accountMessageView.setLayoutParams(accountMessageViewParams);
        if (isNewMagazineViewForDownFP()) {
            if (this.mLVBackFingerprintUsePasswordView != null) {
                this.mLVBackFingerprintUsePasswordView.setText(this.mContext.getString(33686102));
            }
            if (this.mLVBackFingerprintCancelView != null) {
                this.mLVBackFingerprintCancelView.setText(this.mContext.getString(33686121));
            }
        } else {
            ViewGroup.MarginLayoutParams usePasswordHotSpotParams = (ViewGroup.MarginLayoutParams) usePasswordHotSpot.getLayoutParams();
            usePasswordHotSpotParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472545)) * dpiScale) + 0.5f);
            usePasswordHotSpotParams.bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472544)) * dpiScale) + 0.5f);
            usePasswordHotSpot.setLayoutParams(usePasswordHotSpotParams);
            usePasswordView.setText(this.mContext.getString(33686102));
            usePasswordView.setTextSize(0, (float) ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472546)) * dpiScale) + 0.5f)));
            if (usePasswordHotspotLayout != null) {
                usePasswordHotspotLayout.setContentDescription(this.mContext.getString(33686102));
            }
        }
        this.mHintView = (HintText) this.mFingerView.findViewById(34603060);
        resetFrozenCountDownIfNeed();
        if (this.mIsFingerFrozen) {
            this.mHintView.setText(this.mContext.getResources().getQuantityString(34406411, this.mRemainedSecs, new Object[]{Integer.valueOf(this.mRemainedSecs)}));
        } else if (this.mHasBackFingerprint) {
            this.mHintView.setText(this.mContext.getString(33686097));
        } else {
            this.mHintView.setText(this.mContext.getString(33686103));
        }
        this.mHintView.setTextSize(0, (float) ((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472336)) * dpiScale) + 0.5f)));
        if (!isNewMagazineViewForDownFP()) {
            ViewGroup.MarginLayoutParams hintViewParams = (ViewGroup.MarginLayoutParams) this.mHintView.getLayoutParams();
            hintViewParams.bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472335)) * dpiScale) + 0.5f);
            this.mHintView.setLayoutParams(hintViewParams);
            usePasswordHotSpot.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    FingerViewController.this.backFingerprintUsePasswordViewOnclick();
                }
            });
        } else {
            ViewGroup.LayoutParams hintViewParams2 = this.mHintView.getLayoutParams();
            if (hintViewParams2 instanceof RelativeLayout.LayoutParams) {
                getCurrentRotation();
                if (this.mCurrentRotation == 1 || this.mCurrentRotation == 3) {
                    ((RelativeLayout.LayoutParams) hintViewParams2).topMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472473)) * dpiScale) + 0.5f);
                } else {
                    ((RelativeLayout.LayoutParams) hintViewParams2).bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472472)) * dpiScale) + 0.5f);
                }
                this.mHintView.setLayoutParams(hintViewParams2);
            }
        }
        if (this.mPkgAttributes != null) {
            Log.d(TAG, "mPkgAttributes =" + this.mPkgAttributes);
            if (this.mPkgAttributes.getString("googleFlag") == null) {
                if (this.mPkgAttributes.getParcelable("CustView") != null) {
                    this.mRemoteView.setVisibility(0);
                    Log.d(TAG, "RemoteViews != null");
                    this.mRemoteView.addView(((RemoteViews) this.mPkgAttributes.getParcelable("CustView")).apply(this.mContext, this.mRemoteView));
                } else {
                    this.mRemoteView.setVisibility(4);
                }
                Log.d(TAG, "mPkgAttributes.getString= " + this.mPkgAttributes.getString("Title"));
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
                        this.mLVBackFingerprintUsePasswordView.setVisibility(0);
                        ViewGroup.LayoutParams params = this.mLVBackFingerprintCancelView.getLayoutParams();
                        if (params instanceof RelativeLayout.LayoutParams) {
                            params.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472470)) * dpiScale) + 0.5f);
                            this.mLVBackFingerprintCancelView.setLayoutParams(params);
                        }
                    }
                } else if (!isNewMagazineViewForDownFP()) {
                    usePasswordHotSpot.setVisibility(4);
                } else {
                    getCurrentRotation();
                    updateButtomLayoutInNewFinger(dpiScale);
                }
                if (this.mPkgAttributes.getString("SystemTitle") != null) {
                    Log.d(TAG, "attributestring =" + this.mPkgAttributes.getString("SystemTitle"));
                    this.mHintView.setText(this.mPkgAttributes.getString("SystemTitle"));
                    return;
                }
                return;
            }
            if (this.mPkgAttributes.getString("title") != null) {
                Log.d(TAG, "title =" + this.mPkgAttributes.getString("title"));
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
                Log.d(TAG, "description =" + this.mPkgAttributes.getString("description"));
                this.mHintView.setText(this.mPkgAttributes.getString("description"));
            }
            if (this.mPkgAttributes.getString("positive_text") != null) {
                Log.d(TAG, "positive_text =" + this.mPkgAttributes.getString("positive_text"));
                if (!isNewMagazineViewForDownFP()) {
                    usePasswordHotSpot.setVisibility(0);
                    usePasswordView.setText(this.mPkgAttributes.getString("positive_text"));
                } else {
                    this.mLVBackFingerprintUsePasswordView.setVisibility(0);
                    this.mLVBackFingerprintUsePasswordView.setText(this.mPkgAttributes.getString("positive_text"));
                }
            } else if (!isNewMagazineViewForDownFP()) {
                usePasswordHotSpot.setVisibility(4);
            } else if (this.mLVBackFingerprintUsePasswordView != null) {
                updateButtomLayoutInNewFinger(dpiScale);
            }
            if (this.mPkgAttributes.getString("negative_text") != null) {
                Log.d(TAG, "negative_text =" + this.mPkgAttributes.getString("negative_text"));
                if (this.mLVBackFingerprintCancelView != null) {
                    this.mLVBackFingerprintCancelView.setText(this.mPkgAttributes.getString("negative_text"));
                    return;
                }
                return;
            }
            return;
        }
        this.mRemoteView.setVisibility(4);
        appNameView.setVisibility(4);
        accountMessageView.setVisibility(8);
        if (!isNewMagazineViewForDownFP()) {
            usePasswordHotSpot.setVisibility(4);
            return;
        }
        getCurrentRotation();
        updateButtomLayoutInNewFinger(dpiScale);
    }

    private void updateButtomLayoutInNewFinger(float dpiScale) {
        if (this.mCurrentRotation == 1 || this.mCurrentRotation == 3) {
            this.mLVBackFingerprintUsePasswordView.setVisibility(4);
            return;
        }
        this.mLVBackFingerprintUsePasswordView.setVisibility(8);
        ViewGroup.LayoutParams params = this.mLVBackFingerprintCancelView.getLayoutParams();
        if (params instanceof RelativeLayout.LayoutParams) {
            ((RelativeLayout.LayoutParams) params).removeRule(9);
            ((RelativeLayout.LayoutParams) params).addRule(1);
            params.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472471)) * dpiScale) + 0.5f);
            this.mLVBackFingerprintCancelView.setLayoutParams(params);
        }
    }

    private void getCurrentRotation() {
        this.mCurrentRotation = this.mWindowManager.getDefaultDisplay().getRotation();
    }

    /* access modifiers changed from: private */
    public void getCurrentFingerprintCenter() {
        this.mCurrentRotation = this.mWindowManager.getDefaultDisplay().getRotation();
        switch (this.mCurrentRotation) {
            case 0:
            case 2:
                this.mFingerprintCenterX = (this.mFingerprintPosition[0] + this.mFingerprintPosition[2]) / 2;
                this.mFingerprintCenterY = (this.mFingerprintPosition[1] + this.mFingerprintPosition[3]) / 2;
                return;
            case 1:
                this.mFingerprintCenterX = (this.mFingerprintPosition[1] + this.mFingerprintPosition[3]) / 2;
                this.mFingerprintCenterY = (this.mFingerprintPosition[0] + this.mFingerprintPosition[2]) / 2;
                return;
            case 3:
                this.mFingerprintCenterX = this.mDefaultDisplayHeight - ((this.mFingerprintPosition[1] + this.mFingerprintPosition[3]) / 2);
                this.mFingerprintCenterY = (this.mFingerprintPosition[0] + this.mFingerprintPosition[2]) / 2;
                return;
            default:
                return;
        }
    }

    private void initFingerprintViewParams() {
        if (this.mFingerViewParams == null) {
            this.mFingerViewParams = new WindowManager.LayoutParams(-1, -1);
            this.mFingerViewParams.layoutInDisplayCutoutMode = 1;
            this.mFingerViewParams.flags = 201852424;
            this.mFingerViewParams.privateFlags |= 16;
            this.mFingerViewParams.format = -3;
            this.mFingerViewParams.screenOrientation = 14;
            this.mFingerViewParams.setTitle(FINGRPRINT_VIEW_TITLE_NAME);
        }
    }

    /* access modifiers changed from: private */
    public void removeFingerprintView() {
        Log.d(TAG, "removeFingerprintView start added = " + this.mFingerprintViewAdded);
        if (this.mFingerView != null && this.mFingerprintViewAdded) {
            this.mWindowManager.removeView(this.mFingerView);
            resetFingerprintView();
            if (this.mNotchConerStatusChanged) {
                transferNotchRoundCorner(1);
            }
            Log.d(TAG, "removeFingerprintView is done is View Added = " + this.mFingerprintViewAdded + "mFingerView =" + this.mFingerView);
        }
    }

    /* access modifiers changed from: private */
    public void updateFingerprintView() {
        if (this.mAuthenticateResult == 1) {
            if (this.mUseDefaultHint) {
                updateHintView();
            }
        } else if (this.mAuthenticateResult == 0) {
            if (this.mUseDefaultHint) {
                updateHintView(this.mContext.getString(33686099));
            }
            if (this.mFingerprintView != null) {
                this.mFingerprintView.setContentDescription(this.mContext.getString(33686099));
            }
            String foregroundPkg = getForegroundPkgName();
            for (String pkgName : PACKAGES_USE_HWAUTH_INTERFACE) {
                if (pkgName.equals(foregroundPkg)) {
                    Log.d(TAG, "hw wallet Identifing,pkgName = " + pkgName);
                }
            }
            if (!this.mKeepMaskAfterAuthentication) {
                removeMaskOrButton();
            }
        } else if (this.mAuthenticateResult == 2) {
            if (!this.mUseDefaultHint) {
                return;
            }
            if (this.mRemainTimes == 5) {
                if (this.mHasBackFingerprint) {
                    updateHintView(this.mContext.getString(33686097));
                } else {
                    updateHintView(this.mContext.getString(33686103));
                }
                if (this.mFingerprintView != null) {
                    this.mFingerprintView.setContentDescription(this.mContext.getString(33686251));
                    return;
                }
                return;
            }
            updateHintView();
        } else if (this.mAuthenticateResult == 3) {
            if (this.mFingerprintView != null) {
                this.mFingerprintView.setContentDescription(this.mContext.getString(33686100));
            }
            if (this.mUseDefaultHint) {
                updateHintView(this.mContext.getString(33686100));
            }
        }
    }

    private void updateHintView() {
        Log.d(TAG, "updateFingerprintView start,mFingerprintViewAdded = " + this.mFingerprintViewAdded);
        if (this.mFingerprintViewAdded && this.mHintView != null) {
            if (this.mRemainTimes > 0 && this.mRemainTimes < 5) {
                Log.d(TAG, "remaind time = " + this.mRemainTimes);
                String trymoreStr = this.mContext.getResources().getQuantityString(34406412, this.mRemainTimes, new Object[]{Integer.valueOf(this.mRemainTimes)});
                this.mHintView.setText(trymoreStr);
                if (this.mFingerprintView != null) {
                    this.mFingerprintView.setContentDescription(trymoreStr);
                }
            } else if (this.mRemainTimes == 0) {
                if (!this.mIsFingerFrozen) {
                    if (this.mMyCountDown != null) {
                        this.mMyCountDown.cancel();
                    }
                    if (this.mFingerprintView != null) {
                        this.mFingerprintView.setContentDescription(this.mContext.getResources().getQuantityString(34406411, 30, new Object[]{30}));
                    }
                    RemainTimeCountDown remainTimeCountDown = new RemainTimeCountDown(HwArbitrationDEFS.DelayTimeMillisA, 1000);
                    this.mMyCountDown = remainTimeCountDown;
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
    public void updateHintView(String hint) {
        if (this.mHintView != null) {
            this.mHintView.setText(hint);
        }
        if (this.mFingerprintViewAdded) {
            this.mWindowManager.updateViewLayout(this.mFingerView, this.mFingerViewParams);
        }
    }

    private void resetFingerprintView() {
        this.mFingerprintViewAdded = false;
        if (this.mBLurBitmap != null) {
            this.mBLurBitmap.recycle();
        }
        if (this.mScreenShot != null) {
            this.mScreenShot.recycle();
            this.mScreenShot = null;
        }
        if (this.mRemoteView != null) {
            this.mRemoteView.removeAllViews();
        }
        unregisterSingerHandObserver();
    }

    private void sendKeyEvent() {
        int[] actions = {0, 1};
        for (int keyEvent : actions) {
            long curTime = SystemClock.uptimeMillis();
            KeyEvent ev = new KeyEvent(curTime, curTime, keyEvent, 4, 0, 0, -1, 0, 8, 257);
            InputManager.getInstance().injectInputEvent(ev, 0);
        }
    }

    /* access modifiers changed from: private */
    public void createImageOnlyView() {
        int curHeight = SystemPropertiesEx.getInt("persist.sys.rog.height", this.mDefaultDisplayHeight);
        int dpi = SystemProperties.getInt("persist.sys.dpi", SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0)));
        float dpiScale = getDPIScale();
        if (!(this.mLayoutForAlipay != null && curHeight == this.mSavedImageHeight && dpi == this.mSavedImageDpi)) {
            this.mSavedImageHeight = curHeight;
            this.mSavedImageDpi = dpi;
            this.mLayoutForAlipay = (RelativeLayout) this.mLayoutInflater.inflate(34013295, null);
            this.mFingerprintImageForAlipay = (ImageView) this.mLayoutForAlipay.findViewById(34603058);
            this.mCancelViewImageOnly = (RelativeLayout) this.mLayoutForAlipay.findViewById(34603035);
            if (this.mIsCancelHotSpotPkgAdded) {
                this.mCancelViewImageOnly.setVisibility(0);
                this.mCancelViewImageOnly.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        FingerViewController.this.cancelHotSpotViewOnclick();
                    }
                });
            } else {
                this.mCancelViewImageOnly.setVisibility(8);
            }
        }
        if (!this.mHasUdFingerprint) {
            this.mFingerprintImageForAlipay.setImageResource(33751834);
        } else if (isNewMagazineViewForDownFP()) {
            this.mFingerprintImageForAlipay.setImageResource(33751856);
        } else {
            this.mAlipayDrawable = new BreathImageDrawable(this.mContext);
            this.mAlipayDrawable.setBreathImageDrawable(null, this.mContext.getDrawable(33751856));
            this.mFingerprintImageForAlipay.setImageDrawable(this.mAlipayDrawable);
            this.mFingerprintImageForAlipay.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case 0:
                            FingerViewController.this.mAlipayDrawable.startTouchDownBreathAnim();
                            break;
                        case 1:
                            FingerViewController.this.mAlipayDrawable.startTouchUpBreathAnim();
                            break;
                    }
                    return true;
                }
            });
            this.mAlipayDrawable.startBreathAnim();
        }
        int[] fingerprintMargin = calculateFingerprintImageMargin();
        ViewGroup.MarginLayoutParams fingerprintImageParams = (ViewGroup.MarginLayoutParams) this.mFingerprintImageForAlipay.getLayoutParams();
        fingerprintImageParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472319)) * dpiScale) + 0.5f);
        fingerprintImageParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472318)) * dpiScale) + 0.5f);
        this.mFingerprintImageForAlipay.setLayoutParams(fingerprintImageParams);
        WindowManager.LayoutParams fingerprintOnlyLayoutParams = new WindowManager.LayoutParams();
        fingerprintOnlyLayoutParams.flags = 16777480;
        fingerprintOnlyLayoutParams.privateFlags |= 16;
        fingerprintOnlyLayoutParams.format = -3;
        fingerprintOnlyLayoutParams.screenOrientation = 14;
        fingerprintOnlyLayoutParams.setTitle(FINGRPRINT_IMAGE_TITLE_NAME);
        fingerprintOnlyLayoutParams.gravity = 51;
        fingerprintOnlyLayoutParams.type = this.mWindowType;
        fingerprintOnlyLayoutParams.x = fingerprintMargin[0];
        fingerprintOnlyLayoutParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472319)) * dpiScale) + 0.5f);
        if (this.mIsCancelHotSpotPkgAdded) {
            fingerprintOnlyLayoutParams.y = fingerprintMargin[1] - (((int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472316)) * dpiScale) + 0.5f)) * 2);
            fingerprintOnlyLayoutParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472318)) * dpiScale) + (((float) this.mContext.getResources().getDimensionPixelSize(34472316)) * dpiScale * 2.0f) + 0.5f);
            ViewGroup.MarginLayoutParams cancelViewParams = (ViewGroup.MarginLayoutParams) this.mCancelViewImageOnly.getLayoutParams();
            cancelViewParams.bottomMargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34471941)) * dpiScale) + 0.5f);
            ImageView cancelImage = (ImageView) this.mCancelViewImageOnly.findViewById(34603033);
            ViewGroup.MarginLayoutParams cancelImageParams = (ViewGroup.MarginLayoutParams) cancelImage.getLayoutParams();
            cancelImageParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472468)) * dpiScale) + 0.5f);
            cancelImageParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472468)) * dpiScale) + 0.5f);
            cancelImage.setLayoutParams(cancelImageParams);
            cancelViewParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472317)) * dpiScale) + 0.5f);
            cancelViewParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472316)) * dpiScale) + 0.5f);
            this.mCancelViewImageOnly.setLayoutParams(cancelViewParams);
        } else {
            fingerprintOnlyLayoutParams.y = fingerprintMargin[1];
            fingerprintOnlyLayoutParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472318)) * dpiScale) + 0.5f);
        }
        Log.d(TAG, "fingerprintImage location = [" + fingerprintOnlyLayoutParams.x + "," + fingerprintOnlyLayoutParams.y + "]");
        if (!this.mFingerprintOnlyViewAdded) {
            this.mWindowManager.addView(this.mLayoutForAlipay, fingerprintOnlyLayoutParams);
            this.mFingerprintOnlyViewAdded = true;
            exitSingleHandMode();
            registerSingerHandObserver();
        }
    }

    /* access modifiers changed from: private */
    public void updateImageOnlyView() {
        if (this.mAuthenticateResult == 0) {
            this.mHandler.post(this.mRemoveImageOnlyRunnable);
            this.mFingerViewChangeCallback.onFingerViewStateChange(0);
        }
    }

    /* access modifiers changed from: private */
    public void removeImageOnlyView() {
        if (this.mFingerprintOnlyViewAdded && this.mLayoutForAlipay != null) {
            this.mWindowManager.removeView(this.mLayoutForAlipay);
            this.mFingerprintOnlyViewAdded = false;
            unregisterSingerHandObserver();
        }
    }

    private int[] calculateFingerprintImageMargin() {
        Log.d(TAG, "left = " + this.mFingerprintPosition[0] + "right = " + this.mFingerprintPosition[2]);
        Log.d(TAG, "top = " + this.mFingerprintPosition[1] + "button = " + this.mFingerprintPosition[3]);
        float dpiScale = getDPIScale();
        int[] margin = new int[2];
        int fingerPrintInScreenWidth = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472319)) * dpiScale) + 0.5f);
        int fingerPrintInScreenHeight = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472318)) * dpiScale) + 0.5f);
        Log.d(TAG, "fingerPrintInScreenWidth= " + fingerPrintInScreenWidth + "fingerPrintInScreenHeight = " + fingerPrintInScreenHeight);
        Log.d(TAG, "current height = " + this.mCurrentHeight + "mDefaultDisplayHeight = " + this.mDefaultDisplayHeight);
        float scale = ((float) this.mCurrentHeight) / ((float) this.mDefaultDisplayHeight);
        getCurrentRotation();
        if (this.mCurrentRotation == 0 || this.mCurrentRotation == 2) {
            this.mFingerprintCenterX = (this.mFingerprintPosition[0] + this.mFingerprintPosition[2]) / 2;
            this.mFingerprintCenterY = (this.mFingerprintPosition[1] + this.mFingerprintPosition[3]) / 2;
            int marginLeft = ((int) ((((float) this.mFingerprintCenterX) * scale) + 0.5f)) - (fingerPrintInScreenHeight / 2);
            int marginTop = ((int) ((((float) this.mFingerprintCenterY) * scale) + 0.5f)) - (fingerPrintInScreenWidth / 2);
            margin[0] = marginLeft;
            margin[1] = marginTop;
            Log.d(TAG, "marginLeft = " + marginLeft + "marginTop = " + marginTop + "scale = " + scale);
        } else {
            this.mFingerprintCenterX = (this.mFingerprintPosition[1] + this.mFingerprintPosition[3]) / 2;
            this.mFingerprintCenterY = (this.mFingerprintPosition[0] + this.mFingerprintPosition[2]) / 2;
            margin[0] = (int) ((((((float) (this.mDefaultDisplayHeight - this.mFingerprintCenterX)) * scale) - (((float) this.mContext.getResources().getDimensionPixelSize(34472475)) * dpiScale)) - (((float) fingerPrintInScreenWidth) / 2.0f)) + 0.5f);
            margin[1] = (int) (((((float) this.mFingerprintCenterY) * scale) - (((float) fingerPrintInScreenHeight) / 2.0f)) + 0.5f);
        }
        return margin;
    }

    /* access modifiers changed from: private */
    public void createAndAddButtonView() {
        if (this.mButtonViewAdded) {
            adjustButtonViewVisibility();
            Log.d(TAG, " mButtonViewAdded return ");
            return;
        }
        calculateButtonPosition();
        Log.d(TAG, "createAndAddButtonView,pkg = " + this.mPkgName);
        float dpiScale = getDPIScale();
        if (this.mButtonView == null || this.mCurrentHeight != this.mSavedButtonHeight) {
            this.mButtonView = (SuspensionButton) this.mLayoutInflater.inflate(34013294, null);
            this.mButtonView.setCallback(new SuspensionButtonCallback());
            this.mSavedButtonHeight = this.mCurrentHeight;
        }
        initAddButtonViwSubContentDes();
        this.mButtonView.setOnClickListener(new View.OnClickListener() {
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
                        Log.d(FingerViewController.TAG, "catch exception");
                    }
                }
                Context access$2500 = FingerViewController.this.mContext;
                Flog.bdReport(access$2500, 501, "{PkgName:" + FingerViewController.this.mPkgName + "}");
            }
        });
        ImageView buttonImage = (ImageView) this.mButtonView.findViewById(34603031);
        ViewGroup.MarginLayoutParams buttonImageParams = (ViewGroup.MarginLayoutParams) buttonImage.getLayoutParams();
        buttonImageParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472317)) * dpiScale) + 0.5f);
        buttonImageParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472316)) * dpiScale) + 0.5f);
        buttonImage.setLayoutParams(buttonImageParams);
        this.mSuspensionButtonParams = new WindowManager.LayoutParams();
        if (PKGNAME_OF_KEYGUARD.equals(this.mPkgName)) {
            this.mSuspensionButtonParams.type = 2014;
        } else {
            this.mSuspensionButtonParams.type = 2003;
        }
        this.mSuspensionButtonParams.flags = 16777480;
        this.mSuspensionButtonParams.gravity = 51;
        this.mSuspensionButtonParams.x = (int) ((((float) this.mButtonCenterX) - ((((float) this.mContext.getResources().getDimensionPixelSize(34472317)) * dpiScale) / 2.0f)) + 0.5f);
        Log.d(TAG, "mSuspensionButtonParams.x=" + this.mSuspensionButtonParams.x);
        this.mSuspensionButtonParams.y = (int) ((((float) this.mButtonCenterY) - ((((float) this.mContext.getResources().getDimensionPixelSize(34472316)) * dpiScale) / 2.0f)) + 0.5f);
        Log.d(TAG, "mSuspensionButtonParams.y=" + this.mSuspensionButtonParams.y);
        this.mSuspensionButtonParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472317)) * dpiScale) + 0.5f);
        this.mSuspensionButtonParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472316)) * dpiScale) + 0.5f);
        this.mSuspensionButtonParams.format = -3;
        this.mSuspensionButtonParams.privateFlags |= 16;
        this.mSuspensionButtonParams.setTitle("fingerprintview_button");
        this.mButtonView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                boolean unused = FingerViewController.this.mWidgetColorSet = false;
                return true;
            }
        });
        if (PKGNAME_OF_KEYGUARD.equals(this.mPkgName) && this.mButtonColor != 0) {
            this.mWidgetColor = this.mButtonColor;
            this.mWidgetColorSet = true;
        }
        if (!this.mWidgetColorSet) {
            Log.d(TAG, "mWidgetColorSet is false, get a new screenshot and calculate color");
            getScreenShot();
            this.mWidgetColor = HwColorPicker.processBitmap(this.mScreenShot).getWidgetColor();
            this.mWidgetColorSet = true;
        }
        Log.d(TAG, "mWidgetColor = " + this.mWidgetColor);
        buttonImage.setColorFilter(this.mWidgetColor);
        adjustButtonViewVisibility();
        this.mWindowManager.addView(this.mButtonView, this.mSuspensionButtonParams);
        this.mButtonViewAdded = true;
    }

    private void adjustButtonViewVisibility() {
        if (this.mButtonView == null) {
            Log.e(TAG, "mButtonView is null, cannot change visibility");
            return;
        }
        if (this.mButtonViewState == 2) {
            this.mButtonView.setVisibility(4);
        } else {
            this.mButtonView.setVisibility(0);
        }
    }

    /* access modifiers changed from: private */
    public void removeButtonView() {
        if (this.mButtonView != null && this.mButtonViewAdded) {
            Log.d(TAG, "removeButtonView begin, mButtonViewAdded added = " + this.mButtonViewAdded + "mButtonView = " + this.mButtonView);
            this.mWindowManager.removeViewImmediate(this.mButtonView);
            this.mButtonViewAdded = false;
        }
    }

    /* access modifiers changed from: private */
    public void updateButtonView() {
        calculateButtonPosition();
        if (this.mButtonView != null && this.mButtonViewAdded && this.mSuspensionButtonParams != null) {
            this.mSuspensionButtonParams.x = this.mButtonCenterX - (this.mContext.getResources().getDimensionPixelSize(34472537) / 2);
            Log.d(TAG, "mSuspensionButtonParams.x=" + this.mSuspensionButtonParams.x);
            this.mSuspensionButtonParams.y = this.mButtonCenterY - (this.mContext.getResources().getDimensionPixelSize(34472537) / 2);
            Log.d(TAG, "mSuspensionButtonParams.y=" + this.mSuspensionButtonParams.y);
            this.mWindowManager.updateViewLayout(this.mButtonView, this.mSuspensionButtonParams);
        }
    }

    /* access modifiers changed from: private */
    public void startAlphaValueAnimation(final HighLightMaskView target, final boolean isAlphaUp, float startAlpha, float endAlpha, long startDelay, int duration) {
        if (target == null) {
            Log.d(TAG, " animation abort target null");
            return;
        }
        if (startAlpha > ALPHA_LIMITED) {
            startAlpha = ALPHA_LIMITED;
        } else if (startAlpha < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            startAlpha = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        if (endAlpha > ALPHA_LIMITED) {
            endAlpha = ALPHA_LIMITED;
        } else if (endAlpha < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            endAlpha = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        if (isAlphaUp || startAlpha != endAlpha) {
            Log.d(TAG, " startAlphaAnimation current alpha:" + target.getAlpha() + " startAlpha:" + startAlpha + " endAlpha: " + endAlpha + " duration :" + duration);
            final float endAlphaValue = endAlpha;
            ValueAnimator animator = ValueAnimator.ofFloat(new float[]{startAlpha, endAlpha});
            animator.setStartDelay(startDelay);
            animator.setInterpolator(endAlpha == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO ? new AccelerateInterpolator() : new DecelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    target.setAlpha((int) (((Float) animation.getAnimatedValue()).floatValue() * FingerViewController.MAX_BRIGHTNESS_LEVEL));
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                boolean isCanceled = false;

                public void onAnimationEnd(Animator animation) {
                    Log.d(FingerViewController.TAG, " onAnimationEnd endAlpha:" + endAlphaValue + " isCanceled:" + this.isCanceled);
                    if (!isAlphaUp && !this.isCanceled) {
                        FingerViewController.this.mHandler.post(FingerViewController.this.mRemoveHighLightView);
                    }
                    this.isCanceled = false;
                }

                public void onAnimationStart(Animator animation) {
                    Log.d(FingerViewController.TAG, "onAnimationStart");
                    this.isCanceled = false;
                }

                public void onAnimationCancel(Animator animation) {
                    this.isCanceled = true;
                    super.onAnimationCancel(animation);
                }
            });
            animator.setDuration((long) duration);
            animator.start();
            return;
        }
        Log.d(TAG, " endAlpha equals startAlpha ");
        this.mHandler.post(this.mRemoveHighLightView);
    }

    public void showHighlightCircle() {
        this.mHandler.post(this.mShowHighlightCircleRunnable);
    }

    public void removeHighlightCircle() {
        this.mHandler.post(this.mRemoveHighlightCircleRunnable);
    }

    public void notifyCaptureImage() {
        if (this.mFingerViewChangeCallback != null) {
            Log.d(TAG, "onNotifyCaptureImage ");
            this.mFingerViewChangeCallback.onNotifyCaptureImage();
        }
    }

    public void notifyDismissBlueSpot() {
        if (this.mFingerViewChangeCallback != null) {
            Log.d(TAG, "onNotifyBlueSpotDismiss ");
            this.mFingerViewChangeCallback.onNotifyBlueSpotDismiss();
        }
    }

    /* access modifiers changed from: private */
    public void createAndAddHighLightView() {
        if (this.mHighLightView == null) {
            this.mHighLightView = new HighLightMaskView(this.mContext, this.mCurrentBrightness, this.mHighlightSpotRadius, this.mHighlightSpotColor);
        }
        Log.d(TAG, "SpotColor = " + this.mHighlightSpotColor);
        getCurrentFingerprintCenter();
        this.mHighLightView.setCenterPoints(this.mFingerprintCenterX, this.mFingerprintCenterY);
        Log.d(TAG, "current height = " + this.mCurrentHeight);
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
        if (this.mHighLightShowType == 1) {
            this.mHighLightView.setCircleVisibility(0);
        } else if (this.mHighLightShowType == 0) {
            this.mHighLightView.setCircleVisibility(4);
        }
        if (this.mHighLightView.getParent() != null) {
            Log.v(TAG, "REMOVE! mHighLightView before add");
            this.mWindowManager.removeView(this.mHighLightView);
        }
        this.mWindowManager.addView(this.mHighLightView, highLightViewParams);
        this.highLightViewAdded = true;
        if (this.mHighLightShowType == 1) {
            this.mMaxDigitalBrigtness = transformBrightnessViaScreen(this.mHighlightBrightnessLevel);
            setBacklightViaAod(this.mMaxDigitalBrigtness, transformBrightnessViaScreen(this.mCurrentBrightness));
            if (this.mDisplayEngineManager != null) {
                this.mDisplayEngineManager.setScene(29, (int) this.mMaxDigitalBrigtness);
                Log.d(TAG, "mDisplayEngineManager set scene");
            }
        }
    }

    private void setBacklightViaAod(float maxBright) {
        setBacklightViaAod(maxBright, maxBright);
    }

    /* access modifiers changed from: private */
    public void setBacklightViaAod(float maxBright, float currentBright) {
        if (this.mAodManager != null) {
            this.mAodManager.setBacklight((int) maxBright, (int) currentBright);
            Log.d(TAG, "mAodManager set Bright: max: " + maxBright + " current:" + currentBright);
            return;
        }
        Log.d(TAG, "mAodManager is null");
    }

    /* access modifiers changed from: private */
    public float transformBrightnessViaScreen(int brightness) {
        if (INVALID_BRIGHTNESS == this.mNormalizedMaxBrightness || INVALID_BRIGHTNESS == this.mNormalizedMinBrightness) {
            Log.i(TAG, "have not get the valid brightness, try again");
            getBrightnessRangeFromPanelInfo();
        }
        return (((((float) brightness) - 4.0f) / 251.0f) * ((float) (this.mNormalizedMaxBrightness - this.mNormalizedMinBrightness))) + ((float) this.mNormalizedMinBrightness);
    }

    /* access modifiers changed from: private */
    public void removeHighLightViewInner() {
        if (this.highLightViewAdded && this.mHighLightView != null) {
            Log.i(TAG, "highlightview is show, remove highlightview");
            this.mWindowManager.removeView(this.mHighLightView);
        }
        this.mHighLightView = null;
        this.highLightViewAdded = false;
        this.mHandler.removeCallbacks(this.mSetScene);
        this.mHandler.postDelayed(this.mSetScene, 80);
    }

    /* access modifiers changed from: private */
    public void createBackFingprintView() {
        int currentRotation = this.mWindowManager.getDefaultDisplay().getRotation();
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 640));
        int dpi = SystemProperties.getInt("persist.sys.dpi", lcdDpi);
        Configuration curConfig = new Configuration();
        try {
            curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }
        boolean fontScaleChange = curConfig.fontScale != this.mFontScale;
        if (fontScaleChange) {
            Log.e(TAG, "fontScaleChange before createBackFingprintView, curCenfig.fontScale : " + curConfig.fontScale + ", mFontScale : " + this.mFontScale);
            this.mContext.getResources().updateConfiguration(curConfig, null);
            this.mFontScale = curConfig.fontScale;
        }
        if (!(this.mBackFingerprintView != null && this.mCurrentHeight == this.mSavedBackViewHeight && currentRotation == this.mSavedBackViewRotation && dpi == this.mSavedBackViewDpi && !fontScaleChange)) {
            this.mBackFingerprintView = (BackFingerprintView) this.mLayoutInflater.inflate(34013377, null);
            this.mSavedBackViewDpi = dpi;
            this.mSavedBackViewHeight = this.mCurrentHeight;
            this.mSavedBackViewRotation = currentRotation;
        }
        float dpiScale = (((float) lcdDpi) * 1.0f) / ((float) dpi);
        this.mBackFingerprintHintView = (TextView) this.mBackFingerprintView.findViewById(34603448);
        this.mBackFingerprintUsePasswordView = (Button) this.mBackFingerprintView.findViewById(34603450);
        this.mBackFingerprintCancelView = (Button) this.mBackFingerprintView.findViewById(34603445);
        RelativeLayout buttonLayout = (RelativeLayout) this.mBackFingerprintView.findViewById(34603444);
        ViewGroup.MarginLayoutParams usePasswordViewParams = (ViewGroup.MarginLayoutParams) this.mBackFingerprintUsePasswordView.getLayoutParams();
        usePasswordViewParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472241)) * dpiScale) + 0.5f);
        usePasswordViewParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472122)) * dpiScale) + 0.5f);
        this.mBackFingerprintUsePasswordView.setLayoutParams(usePasswordViewParams);
        ViewGroup.MarginLayoutParams cancelViewParams = (ViewGroup.MarginLayoutParams) this.mBackFingerprintCancelView.getLayoutParams();
        cancelViewParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472241)) * dpiScale) + 0.5f);
        cancelViewParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472122)) * dpiScale) + 0.5f);
        this.mBackFingerprintCancelView.setLayoutParams(cancelViewParams);
        ViewGroup.MarginLayoutParams buttonLayoutParams = (ViewGroup.MarginLayoutParams) buttonLayout.getLayoutParams();
        buttonLayoutParams.width = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472240)) * dpiScale) + 0.5f);
        buttonLayoutParams.height = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472239)) * dpiScale) + 0.5f);
        buttonLayout.setLayoutParams(buttonLayoutParams);
        TextView backFingerprintTitle = (TextView) this.mBackFingerprintView.findViewById(34603449);
        TextView backFingerprintDescription = (TextView) this.mBackFingerprintView.findViewById(34603446);
        if (this.mPkgAttributes != null) {
            if (this.mPkgAttributes.getString("title") != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("title =");
                boolean z = fontScaleChange;
                sb.append(this.mPkgAttributes.getString("title"));
                Log.d(TAG, sb.toString());
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
                Log.d(TAG, "description =" + this.mPkgAttributes.getString("description"));
                backFingerprintDescription.setText(this.mPkgAttributes.getString("description"));
            }
            if (this.mPkgAttributes.getString("positive_text") != null) {
                Log.d(TAG, "positive_text =" + this.mPkgAttributes.getString("positive_text"));
                this.mBackFingerprintUsePasswordView.setVisibility(0);
                this.mBackFingerprintUsePasswordView.setText(this.mPkgAttributes.getString("positive_text"));
            } else {
                this.mBackFingerprintUsePasswordView.setVisibility(4);
            }
            if (this.mPkgAttributes.getString("negative_text") != null) {
                Log.d(TAG, "negative_text =" + this.mPkgAttributes.getString("negative_text"));
                this.mBackFingerprintCancelView.setText(this.mPkgAttributes.getString("negative_text"));
            }
        }
        resetFrozenCountDownIfNeed();
        if (this.mIsFingerFrozen) {
            int i = currentRotation;
            this.mBackFingerprintHintView.setText(this.mContext.getResources().getQuantityString(34406411, this.mRemainedSecs, new Object[]{Integer.valueOf(this.mRemainedSecs)}));
        }
        this.mBackFingerprintUsePasswordView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (FingerViewController.this.mDialogReceiver != null) {
                    try {
                        Log.i(FingerViewController.TAG, "back fingerprint view, usepassword clicked");
                        FingerViewController.this.mDialogReceiver.onDialogDismissed(1);
                    } catch (RemoteException e) {
                        Log.d(FingerViewController.TAG, "catch exception");
                    }
                }
            }
        });
        this.mBackFingerprintCancelView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FingerViewController.this.mHandler.post(FingerViewController.this.mRemoveBackFingprintRunnable);
                if (FingerViewController.this.mDialogReceiver != null) {
                    try {
                        Log.i(FingerViewController.TAG, "back fingerprint view, cancel clicked");
                        FingerViewController.this.mDialogReceiver.onDialogDismissed(2);
                    } catch (RemoteException e) {
                        Log.d(FingerViewController.TAG, "catch exception");
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
    public void removeBackFingprintView() {
        if (this.mBackFingerprintView != null && this.mBackFingerprintView.isAttachedToWindow()) {
            this.mWindowManager.removeView(this.mBackFingerprintView);
            if (this.mBLurBitmap != null) {
                this.mBLurBitmap.recycle();
            }
            if (this.mScreenShot != null) {
                this.mScreenShot.recycle();
                this.mScreenShot = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateBackFingprintView() {
        if (this.mAuthenticateResult == 1) {
            updateBackFingerprintHintView();
        } else if (this.mAuthenticateResult == 0) {
            updateBackFingerprintHintView(this.mContext.getString(33686099));
            removeBackFingprintView();
        }
    }

    private void updateBackFingerprintHintView() {
        Log.d(TAG, "updateBackFingerprintHintView start");
        if (this.mBackFingerprintView != null && this.mBackFingerprintView.isAttachedToWindow() && this.mBackFingerprintHintView != null) {
            if (this.mRemainTimes > 0 && this.mRemainTimes < 5) {
                Log.d(TAG, "remaind time = " + this.mRemainTimes);
                this.mBackFingerprintHintView.setText(this.mContext.getResources().getQuantityString(34406412, this.mRemainTimes, new Object[]{Integer.valueOf(this.mRemainTimes)}));
            } else if (this.mRemainTimes == 0) {
                if (!this.mIsFingerFrozen) {
                    if (this.mMyCountDown != null) {
                        this.mMyCountDown.cancel();
                    }
                    RemainTimeCountDown remainTimeCountDown = new RemainTimeCountDown(HwArbitrationDEFS.DelayTimeMillisA, 1000);
                    this.mMyCountDown = remainTimeCountDown;
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
    public void updateBackFingerprintHintView(String hint) {
        if (this.mBackFingerprintHintView != null) {
            this.mBackFingerprintHintView.setText(hint);
        }
        if (this.mBackFingerprintView != null && this.mBackFingerprintView.isAttachedToWindow()) {
            this.mBackFingerprintView.postInvalidate();
        }
    }

    private void startBlurBackViewScreenshot() {
        getScreenShot();
        Log.i(TAG, "mScreenShot = " + this.mScreenShot);
        if (this.mScreenShot == null || (HwColorPicker.processBitmap(this.mScreenShot).getDomainColor() == COLOR_BLACK && !PKGNAME_OF_KEYGUARD.equals(this.mPkgName))) {
            this.mBLurBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            this.mBLurBitmap.eraseColor(-7829368);
            this.mBlurDrawable = new BitmapDrawable(this.mContext.getResources(), this.mBLurBitmap);
            this.mBackFingerprintView.setBackgroundDrawable(this.mBlurDrawable);
            return;
        }
        this.mBLurBitmap = BlurUtils.blurMaskImage(this.mContext, this.mScreenShot, this.mScreenShot, 25);
        this.mBlurDrawable = new BitmapDrawable(this.mContext.getResources(), this.mBLurBitmap);
        this.mBackFingerprintView.setBackgroundDrawable(this.mBlurDrawable);
    }

    private void onCancelClick() {
        this.mHandler.post(this.mRemoveFingerViewRunnable);
        this.mHandler.post(this.mAddButtonViewRunnable);
        if (this.mFingerViewChangeCallback != null) {
            this.mFingerViewChangeCallback.onFingerViewStateChange(2);
        }
    }

    private void onUsePasswordClick() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        this.mHandler.post(this.mRemoveFingerViewRunnable);
        this.mHandler.post(this.mRemoveButtonViewRunnable);
    }

    public void parseBundle4Keyguard(Bundle bundle) {
        int suspend;
        Log.d(TAG, " suspend:" + suspend + " mButtonViewAdded:" + this.mButtonViewAdded + " mFingerprintViewAdded:" + this.mFingerprintViewAdded);
        if (suspend == -1) {
            if (this.mButtonViewAdded && this.mFingerViewChangeCallback != null) {
                this.mFingerViewChangeCallback.onFingerViewStateChange(2);
            }
            if (this.mFingerprintViewAdded) {
                return;
            }
        }
        byte[] viewType = bundle.getByteArray("viewType");
        byte[] viewState = bundle.getByteArray("viewState");
        for (int i = 0; i < viewType.length; i++) {
            if (i % 100 == 0) {
                Log.i(TAG, "do loop in parseBundle4Keyguard, time = " + i);
            }
            byte type = viewType[i];
            byte state = viewState[i];
            Log.d(TAG, " type:" + type + " state:" + state);
            switch (type) {
                case 0:
                    if (state != 1) {
                        if (state != 0) {
                            break;
                        } else {
                            this.mHandler.post(this.mRemoveFingerViewRunnable);
                            if (this.mFingerViewChangeCallback == null) {
                                break;
                            } else {
                                this.mFingerViewChangeCallback.onFingerViewStateChange(1);
                                break;
                            }
                        }
                    } else {
                        this.mHandler.post(this.mAddFingerViewRunnable);
                        break;
                    }
                case 1:
                    if (state != 1) {
                        if (state != 0) {
                            if (state != 2) {
                                break;
                            } else {
                                int[] location = bundle.getIntArray("buttonLocation");
                                this.mHandler.post(this.mRemoveFingerViewRunnable);
                                this.mButtonViewState = 2;
                                showSuspensionButtonForApp(location[0], location[1], UIDNAME_OF_KEYGUARD);
                                break;
                            }
                        } else {
                            this.mHandler.post(this.mRemoveButtonViewRunnable);
                            break;
                        }
                    } else {
                        int[] location2 = bundle.getIntArray("buttonLocation");
                        this.mButtonViewState = 1;
                        this.mButtonColor = bundle.getInt("buttonColor", 0);
                        showSuspensionButtonForApp(location2[0], location2[1], UIDNAME_OF_KEYGUARD);
                        break;
                    }
            }
        }
    }

    public void setFingerprintPosition(int[] position) {
        this.mFingerprintPosition = (int[]) position.clone();
        Log.d(TAG, "setFingerprintPosition,left = " + this.mFingerprintPosition[0] + "right = " + this.mFingerprintPosition[2]);
        Log.d(TAG, "setFingerprintPosition,top = " + this.mFingerprintPosition[1] + "button = " + this.mFingerprintPosition[3]);
        this.mFingerprintCenterX = (this.mFingerprintPosition[0] + this.mFingerprintPosition[2]) / 2;
        this.mFingerprintCenterY = (this.mFingerprintPosition[1] + this.mFingerprintPosition[3]) / 2;
    }

    public void setHighLightBrightnessLevel(int brightness) {
        if (brightness > 255) {
            Log.i(TAG, "brightness is " + brightness + ",adjust it to 255");
            brightness = 255;
        } else if (brightness < 0) {
            Log.i(TAG, "brightness is " + brightness + ",adjust it to 0");
            brightness = 0;
        }
        Log.i(TAG, "brightness to be set is " + brightness);
        this.mHighlightBrightnessLevel = brightness;
        this.mMaxDigitalBrigtness = transformBrightnessViaScreen(this.mHighlightBrightnessLevel);
        if (this.mHbmType == 1) {
            setBacklightViaAod((float) ((int) this.mMaxDigitalBrigtness), (float) (((int) this.mMaxDigitalBrigtness) + 1));
        } else {
            setBacklightViaAod((float) ((int) this.mMaxDigitalBrigtness));
        }
    }

    public void setHighLightSpotColor(int color) {
        Log.i(TAG, "color to be set is " + color);
        this.mHighlightSpotColor = color;
        if (this.mFingerprintCircleOverlay != null) {
            this.mFingerprintCircleOverlay.setColor(color);
        }
    }

    public void setHighLightSpotRadius(int radius) {
        this.mHighlightSpotRadius = radius;
        if (this.mFingerprintCircleOverlay != null) {
            this.mFingerprintCircleOverlay.setRadius(radius);
        }
    }

    private int[] calculateFingerprintMargin() {
        Log.d(TAG, "left = " + this.mFingerprintPosition[0] + "right = " + this.mFingerprintPosition[2]);
        Log.d(TAG, "top = " + this.mFingerprintPosition[1] + "button = " + this.mFingerprintPosition[3]);
        float dpiScale = getDPIScale();
        int[] margin = new int[2];
        int fingerPrintInScreenWidth = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472321)) * dpiScale) + 0.5f);
        int fingerPrintInScreenHeight = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472320)) * dpiScale) + 0.5f);
        Log.d(TAG, "fingerPrintInScreenWidth= " + fingerPrintInScreenWidth + "fingerPrintInScreenHeight = " + fingerPrintInScreenHeight);
        Log.d(TAG, "current height = " + this.mCurrentHeight + "mDefaultDisplayHeight = " + this.mDefaultDisplayHeight);
        float scale = ((float) this.mCurrentHeight) / ((float) this.mDefaultDisplayHeight);
        if (isNewMagazineViewForDownFP()) {
            if (this.mCurrentRotation == 0 || this.mCurrentRotation == 2) {
                this.mFingerprintCenterX = (this.mFingerprintPosition[0] + this.mFingerprintPosition[2]) / 2;
                this.mFingerprintCenterY = (this.mFingerprintPosition[1] + this.mFingerprintPosition[3]) / 2;
                int marginLeft = ((int) ((((float) this.mFingerprintCenterX) * scale) + 0.5f)) - (fingerPrintInScreenHeight / 2);
                int marginTop = ((int) ((((float) this.mFingerprintCenterY) * scale) + 0.5f)) - (fingerPrintInScreenWidth / 2);
                margin[0] = marginLeft;
                margin[1] = marginTop;
                Log.d(TAG, "marginLeft = " + marginLeft + "marginTop = " + marginTop + "scale = " + scale);
            } else {
                this.mFingerprintCenterY = (this.mFingerprintPosition[0] + this.mFingerprintPosition[2]) / 2;
                this.mFingerprintCenterX = (this.mFingerprintPosition[1] + this.mFingerprintPosition[3]) / 2;
                int marginLeft2 = ((int) ((((float) this.mFingerprintCenterX) * scale) + 0.5f)) - (fingerPrintInScreenWidth / 2);
                int marginTop2 = ((int) ((((float) this.mFingerprintCenterY) * scale) + 0.5f)) - (fingerPrintInScreenHeight / 2);
                if (this.mCurrentRotation == 3) {
                    marginLeft2 = this.mDefaultDisplayHeight - marginLeft2;
                }
                margin[0] = marginLeft2;
                margin[1] = marginTop2;
                Log.d(TAG, "marginLeft = " + marginLeft2 + "marginTop = " + marginTop2 + "scale = " + scale);
            }
        } else if (this.mCurrentRotation == 0 || this.mCurrentRotation == 2) {
            this.mFingerprintCenterX = (this.mFingerprintPosition[0] + this.mFingerprintPosition[2]) / 2;
            this.mFingerprintCenterY = (this.mFingerprintPosition[1] + this.mFingerprintPosition[3]) / 2;
            int marginLeft3 = ((int) ((((float) this.mFingerprintCenterX) * scale) + 0.5f)) - (fingerPrintInScreenHeight / 2);
            int marginTop3 = ((int) ((((float) this.mFingerprintCenterY) * scale) + 0.5f)) - (fingerPrintInScreenWidth / 2);
            margin[0] = marginLeft3;
            margin[1] = marginTop3;
            Log.d(TAG, "marginLeft = " + marginLeft3 + "marginTop = " + marginTop3 + "scale = " + scale);
        } else {
            this.mFingerprintCenterX = (this.mFingerprintPosition[1] + this.mFingerprintPosition[3]) / 2;
            this.mFingerprintCenterY = (this.mFingerprintPosition[0] + this.mFingerprintPosition[2]) / 2;
            int marginTop4 = (int) (((((float) this.mFingerprintCenterY) * scale) - (((float) fingerPrintInScreenHeight) / 2.0f)) + 0.5f);
            int marginLeft4 = (int) ((((((float) (this.mDefaultDisplayHeight - this.mFingerprintCenterX)) * scale) - (((float) this.mContext.getResources().getDimensionPixelSize(34472475)) * dpiScale)) - (((float) fingerPrintInScreenWidth) / 2.0f)) + 0.5f);
            margin[0] = marginLeft4;
            margin[1] = marginTop4;
            Log.d(TAG, "marginLeft = " + marginLeft4 + "marginTop = " + marginTop4 + "scale = " + scale + "mDefaultDisplayHeight = " + this.mDefaultDisplayHeight);
        }
        return margin;
    }

    private float getDPIScale() {
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 640));
        int dpi = SystemProperties.getInt("persist.sys.dpi", lcdDpi);
        int realdpi = SystemProperties.getInt("persist.sys.realdpi", dpi);
        float scale = (((float) lcdDpi) * 1.0f) / ((float) dpi);
        Log.i(TAG, "getDPIScale: lcdDpi: " + lcdDpi + " dpi: " + dpi + " realdpi: " + realdpi + " scale: " + scale);
        return scale;
    }

    /* access modifiers changed from: private */
    public float getPxScale() {
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", DEFAULT_LCD_DPI));
        int dpi = SystemProperties.getInt("persist.sys.dpi", lcdDpi);
        int realdpi = SystemProperties.getInt("persist.sys.realdpi", dpi);
        float scale = (((float) realdpi) * 1.0f) / ((float) dpi);
        Log.i(TAG, "getPxScale: lcdDpi: " + lcdDpi + " dpi: " + dpi + " realdpi: " + realdpi + " scale: " + scale);
        return scale;
    }

    private int[] calculateFingerprintLayoutLeftMargin(int width) {
        float scale = ((float) this.mCurrentHeight) / ((float) this.mDefaultDisplayHeight);
        float dpiScale = getDPIScale();
        int[] layoutMargin = new int[2];
        int leftmargin = 0;
        int rightmargin = 0;
        if (this.mCurrentRotation == 3) {
            leftmargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472475)) * dpiScale) + 0.5f);
            rightmargin = (int) (((((float) this.mFingerprintCenterX) * scale) - (((float) width) / 2.0f)) + 0.5f);
        } else if (this.mCurrentRotation == 1) {
            leftmargin = (int) (((((float) this.mFingerprintCenterX) * scale) - (((float) width) / 2.0f)) + 0.5f);
            rightmargin = (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472475)) * dpiScale) + 0.5f);
        }
        layoutMargin[0] = leftmargin;
        layoutMargin[1] = rightmargin;
        return layoutMargin;
    }

    private int calculateRemoteViewLeftMargin(int fingerLayoutWidth) {
        float dpiScale = getDPIScale();
        if (this.mCurrentRotation == 3) {
            return (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472493)) * dpiScale) + (((float) this.mContext.getResources().getDimensionPixelSize(34472475)) * dpiScale) + ((float) fingerLayoutWidth) + 0.5f);
        }
        if (this.mCurrentRotation == 1) {
            return (int) ((((float) this.mContext.getResources().getDimensionPixelSize(34472493)) * dpiScale) + 0.5f);
        }
        return 0;
    }

    private void calculateButtonPosition() {
        if (this.mPkgName != null && !this.mPkgName.equals(PKGNAME_OF_KEYGUARD)) {
            getCurrentRotation();
            Log.d(TAG, "mCurrentRotation = " + this.mCurrentRotation);
            float scale = ((float) this.mCurrentHeight) / ((float) this.mDefaultDisplayHeight);
            switch (this.mCurrentRotation) {
                case 0:
                case 2:
                    this.mButtonCenterX = (int) (((((float) (this.mFingerprintPosition[0] + this.mFingerprintPosition[2])) / 2.0f) * scale) + 0.5f);
                    this.mButtonCenterY = (this.mCurrentHeight - this.mContext.getResources().getDimensionPixelSize(34472266)) - (this.mContext.getResources().getDimensionPixelSize(34472537) / 2);
                    break;
                case 1:
                    this.mButtonCenterX = (int) (((((float) (this.mFingerprintPosition[1] + this.mFingerprintPosition[3])) / 2.0f) * scale) + 0.5f);
                    this.mButtonCenterY = (this.mCurrentWidth - this.mContext.getResources().getDimensionPixelSize(34472266)) - (this.mContext.getResources().getDimensionPixelSize(34472537) / 2);
                    break;
                case 3:
                    this.mButtonCenterX = this.mCurrentHeight - ((int) (((((float) (this.mFingerprintPosition[1] + this.mFingerprintPosition[3])) / 2.0f) * scale) + 0.5f));
                    this.mButtonCenterY = (this.mCurrentWidth - this.mContext.getResources().getDimensionPixelSize(34472266)) - (this.mContext.getResources().getDimensionPixelSize(34472537) / 2);
                    break;
            }
            Log.d(TAG, "mButtonCenterX = " + this.mButtonCenterX + ",mButtonCenterY =" + this.mButtonCenterY);
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
    public void getBrightness() {
        if (this.mPowerManager != null) {
            Bundle data = new Bundle();
            if (this.mPowerManager.hwBrightnessGetData("CurrentBrightness", data) != 0) {
                this.mCurrentBrightness = -1;
                Log.w(TAG, "get currentBrightness failed!");
            } else {
                this.mCurrentBrightness = data.getInt("Brightness");
            }
            Log.i(TAG, "currentBrightness=" + this.mCurrentBrightness);
        }
    }

    public void setLightLevel(int level, int lightLevelTime) {
        try {
            this.pm.setBrightnessNoLimit(level, lightLevelTime);
            Log.d(TAG, "setLightLevel :" + level + " time:" + lightLevelTime);
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
        for (String pkg : PACKAGES_USE_HWAUTH_INTERFACE) {
            if (pkg.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCancelHotSpotNeed(String pkgName) {
        for (String pkg : PACKAGES_USE_CANCEL_HOTSPOT_INTERFACE) {
            if (pkg.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCancelHotSpotViewVisble(String pkgName) {
        getCurrentRotation();
        return isCancelHotSpotNeed(pkgName) && isNewMagazineViewForDownFP() && (this.mCurrentRotation == 0 || this.mCurrentRotation == 2);
    }

    private void startBlurScreenshot() {
        getScreenShot();
        if (this.mScreenShot == null || (HwColorPicker.processBitmap(this.mScreenShot).getDomainColor() == COLOR_BLACK && !PKGNAME_OF_KEYGUARD.equals(this.mPkgName))) {
            this.mBLurBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            this.mBLurBitmap.eraseColor(-7829368);
            this.mBlurDrawable = new BitmapDrawable(this.mContext.getResources(), this.mBLurBitmap);
            this.mFingerView.setBackgroundDrawable(this.mBlurDrawable);
            return;
        }
        this.mBLurBitmap = BlurUtils.blurMaskImage(this.mContext, this.mScreenShot, this.mScreenShot, 25);
        this.mBlurDrawable = new BitmapDrawable(this.mContext.getResources(), this.mBLurBitmap);
        this.mFingerView.setBackgroundDrawable(this.mBlurDrawable);
    }

    private void getScreenShot() {
        try {
            this.mScreenShot = BlurUtils.screenShotBitmap(this.mContext, BLUR_SCALE);
            if (this.mScreenShot != null) {
                this.mScreenShot = this.mScreenShot.copy(Bitmap.Config.ARGB_8888, true);
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception in screenShotBitmap");
        } catch (Error err) {
            Log.d(TAG, "screenShotBitmap  Error er = " + err.getMessage());
        }
    }

    private void registerSingerHandObserver() {
        if (this.mSingleContentObserver == null) {
            this.mSingleContentObserver = new SingleModeContentObserver(new Handler(), new SingleModeContentCallback());
        }
        if (!this.mIsSingleModeObserverRegistered) {
            Log.d(TAG, "registerSingerHandObserver");
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("single_hand_mode"), true, this.mSingleContentObserver);
            this.mIsSingleModeObserverRegistered = true;
        }
    }

    private void unregisterSingerHandObserver() {
        if (this.mIsSingleModeObserverRegistered) {
            Log.d(TAG, "unregisterSingerHandObserver");
            this.mContext.getContentResolver().unregisterContentObserver(this.mSingleContentObserver);
            this.mIsSingleModeObserverRegistered = false;
        }
    }

    private void exitSingleHandMode() {
        Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
    }

    private void resetFrozenCountDownIfNeed() {
        this.mRemainTimes = this.mFingerprintManagerEx.getRemainingNum();
        Log.d(TAG, "getRemainingNum, mRemainTimes = " + this.mRemainTimes);
        if (this.mRemainTimes > 0 && this.mMyCountDown != null) {
            this.mMyCountDown.cancel();
            this.mIsFingerFrozen = false;
        }
    }

    public void notifyFingerprintViewCoverd(boolean covered, Rect winFrame) {
        Log.v(TAG, "notifyWinCovered covered=" + covered + "winFrame = " + winFrame);
        if (covered) {
            if (isFingerprintViewCoverd(getFingerprintViewRect(), winFrame)) {
                Log.v(TAG, "new window covers fingerprintview, suspend");
                if (this.mFingerViewChangeCallback != null) {
                    this.mFingerViewChangeCallback.onFingerViewStateChange(2);
                    return;
                }
                return;
            }
            Log.v(TAG, "new window doesn't cover fingerprintview");
        } else if (this.mFingerViewChangeCallback != null) {
            this.mFingerViewChangeCallback.onFingerViewStateChange(1);
        }
    }

    public void notifyTouchUp(float x, float y) {
        if (this.highLightViewAdded || this.mFingerprintCircleOverlay.isVisible()) {
            Log.d(TAG, "UD Fingerprint notifyTouchUp point (" + x + " , " + y + ")");
            if (!isFingerViewTouched(x, y)) {
                Log.i(TAG, "notifyTouchUp,point not in fingerprint view");
            } else {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Log.i(FingerViewController.TAG, "begin anonymous runnable in notifyTouchUp");
                        FingerViewController.this.removeHighlightCircle();
                    }
                });
            }
        }
    }

    private Rect getFingerprintViewRect() {
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 640));
        int dpi = SystemProperties.getInt("persist.sys.dpi", lcdDpi);
        float scale = (((float) SystemProperties.getInt("persist.sys.realdpi", dpi)) * 1.0f) / ((float) dpi);
        float lcdscale = (((float) lcdDpi) * 1.0f) / ((float) dpi);
        Rect fingerprintViewRect = new Rect();
        fingerprintViewRect.left = (int) (((double) ((((float) (this.mFingerprintPosition[0] + this.mFingerprintPosition[2])) / 2.0f) * scale)) - ((((double) (((float) this.mContext.getResources().getDimensionPixelSize(34472321)) * lcdscale)) * 0.5d) + 0.5d));
        fingerprintViewRect.top = (int) (((double) ((((float) (this.mFingerprintPosition[1] + this.mFingerprintPosition[3])) / 2.0f) * scale)) - ((((double) (((float) this.mContext.getResources().getDimensionPixelSize(34472320)) * lcdscale)) * 0.5d) + 0.5d));
        fingerprintViewRect.right = (int) (((double) ((((float) (this.mFingerprintPosition[0] + this.mFingerprintPosition[2])) / 2.0f) * scale)) + (((double) (((float) this.mContext.getResources().getDimensionPixelSize(34472321)) * lcdscale)) * 0.5d) + 0.5d);
        fingerprintViewRect.bottom = (int) (((double) ((((float) (this.mFingerprintPosition[1] + this.mFingerprintPosition[3])) / 2.0f) * scale)) + (((double) (((float) this.mContext.getResources().getDimensionPixelSize(34472320)) * lcdscale)) * 0.5d) + 0.5d);
        Log.i(TAG, "getFingerprintViewRect, fingerprintViewRect = " + fingerprintViewRect);
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
        Log.i(TAG, "setHighlightViewAlpha called , brightness = " + brightness);
        this.mCurrentBrightness = brightness;
        this.mCurrentAlpha = getMaskAlpha(brightness);
        if (this.mFingerprintMaskOverlay.isVisible()) {
            SurfaceControl.openTransaction();
            try {
                this.mFingerprintMaskOverlay.setAlpha(((float) this.mCurrentAlpha) / MAX_BRIGHTNESS_LEVEL);
            } finally {
                SurfaceControl.closeTransaction();
            }
        }
    }

    private boolean getBrightnessRangeFromPanelInfo() {
        File file = new File(PANEL_INFO_NODE);
        if (!file.exists()) {
            Log.w(TAG, "getBrightnessRangeFromPanelInfo PANEL_INFO_NODE:" + PANEL_INFO_NODE + " isn't exist");
            return false;
        }
        BufferedReader reader = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String readLine = reader.readLine();
            String tempString = readLine;
            if (readLine != null) {
                Log.i(TAG, "getBrightnessRangeFromPanelInfo String = " + tempString);
                if (tempString.length() == 0) {
                    Log.e(TAG, "getBrightnessRangeFromPanelInfo error! String is null");
                    reader.close();
                    close(reader, fis);
                    return false;
                }
                String[] stringSplited = tempString.split(",");
                if (stringSplited.length < 2) {
                    Log.e(TAG, "split failed! String = " + tempString);
                    reader.close();
                    close(reader, fis);
                    return false;
                } else if (parsePanelInfo(stringSplited)) {
                    reader.close();
                    close(reader, fis);
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "getBrightnessRangeFromPanelInfo error! FileNotFoundException");
        } catch (IOException e2) {
            Log.e(TAG, "getBrightnessRangeFromPanelInfo error! IOException " + e2);
        } catch (Exception e3) {
            Log.e(TAG, "getBrightnessRangeFromPanelInfo error! Exception " + e3);
        } catch (Throwable th) {
            close(reader, fis);
            throw th;
        }
        close(reader, fis);
        return false;
    }

    private void close(BufferedReader reader, FileInputStream fis) {
        if (reader != null || fis != null) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    private boolean parsePanelInfo(String[] stringSplited) {
        if (stringSplited == null) {
            return false;
        }
        String key = null;
        int min = -1;
        int max = -1;
        int i = 0;
        while (i < stringSplited.length) {
            try {
                if (i % 100 == 0) {
                    Log.i(TAG, "do loop in parseBundle4Keyguard, time = " + i);
                }
                key = "blmax:";
                int index = stringSplited[i].indexOf(key);
                if (index != -1) {
                    max = Integer.parseInt(stringSplited[i].substring(key.length() + index));
                } else {
                    key = "blmin:";
                    int index2 = stringSplited[i].indexOf(key);
                    if (index2 != -1) {
                        min = Integer.parseInt(stringSplited[i].substring(key.length() + index2));
                    }
                }
                i++;
            } catch (NumberFormatException e) {
                Log.e(TAG, "parsePanelInfo() error! " + key + e);
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
        String lcdversion = getVersionFromLCD();
        ArrayList<String> xmlPathList = new ArrayList<>();
        if (!(lcdversion == null || lcdname == null)) {
            xmlPathList.add(String.format("/display/effect/displayengine/%s_%s_%s%s", new Object[]{"udfp", lcdname, lcdversion, ".xml"}));
        }
        if (lcdname != null) {
            xmlPathList.add(String.format("/display/effect/displayengine/%s_%s%s", new Object[]{"udfp", lcdname, ".xml"}));
        }
        xmlPathList.add(String.format("/display/effect/displayengine/%s%s", new Object[]{"udfp", ".xml"}));
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
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e3) {
            Log.e(TAG, "initBrightnessAlphaConfig error! Exception");
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
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
        boolean configGroupLoadStarted = false;
        boolean loadFinished = false;
        String description = null;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            int eventType = parser.getEventType();
            while (true) {
                if (eventType != 1) {
                    switch (eventType) {
                        case 2:
                            String name = parser.getName();
                            if (!name.equals("BrightnessAndAlphaConfig")) {
                                if (!name.equals("Brightness")) {
                                    if (!name.equals("Alpha")) {
                                        if (name.equals("Description")) {
                                            description = parser.nextText();
                                            break;
                                        }
                                    } else {
                                        this.mSampleAlpha = covertToIntArray(parser.nextText());
                                        break;
                                    }
                                } else {
                                    this.mSampleBrightness = covertToIntArray(parser.nextText());
                                    break;
                                }
                            } else {
                                configGroupLoadStarted = true;
                                break;
                            }
                            break;
                        case 3:
                            if (parser.getName().equals("BrightnessAndAlphaConfig") && configGroupLoadStarted) {
                                loadFinished = true;
                                configGroupLoadStarted = false;
                                break;
                            }
                    }
                    if (!loadFinished) {
                        eventType = parser.next();
                    }
                }
            }
            if (loadFinished) {
                Log.d(TAG, "getBrightnessAlphaConfig success, xml Description : " + description);
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

    private int[] covertToIntArray(String str) {
        if (str == null) {
            return null;
        }
        String[] arr = str.split(",");
        int[] num = new int[arr.length];
        for (int i = 0; i < num.length; i++) {
            num[i] = Integer.parseInt(arr[i]);
        }
        return num;
    }

    private String getLcdPanelName() {
        IBinder binder = ServiceManager.getService("DisplayEngineExService");
        String panelName = null;
        if (binder == null) {
            Log.i(TAG, "getLcdPanelName() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Log.e(TAG, "getLcdPanelName() mService is null!");
            return null;
        }
        byte[] name = new byte[128];
        try {
            int ret = mService.getEffect(14, 0, name, name.length);
            if (ret != 0) {
                Log.e(TAG, "getLcdPanelName() getEffect failed! ret=" + ret);
                return null;
            }
            try {
                panelName = new String(name, "UTF-8").trim().replace(' ', '_');
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unsupported encoding type!");
            }
            Log.i(TAG, "getLcdPanelName() panelName=" + panelName);
            return panelName;
        } catch (RemoteException e2) {
            Log.e(TAG, "getLcdPanelName() RemoteException " + e2);
            return null;
        }
    }

    private String getVersionFromLCD() {
        IBinder binder = ServiceManager.getService("DisplayEngineExService");
        String panelVersion = null;
        if (binder == null) {
            Log.w(TAG, "getVersionFromLCD() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Log.w(TAG, "getVersionFromLCD() mService is null!");
            return null;
        }
        byte[] name = new byte[32];
        try {
            int ret = mService.getEffect(14, 3, name, name.length);
            if (ret != 0) {
                Log.e(TAG, "getVersionFromLCD() getEffect failed! ret=" + ret);
                return null;
            }
            try {
                String lcdVersion = new String(name, "UTF-8");
                Log.i(TAG, "getVersionFromLCD() lcdVersion=" + lcdVersion);
                String lcdVersion2 = lcdVersion.trim();
                int index = lcdVersion2.indexOf("VER:");
                if (index != -1) {
                    panelVersion = lcdVersion2.substring("VER:".length() + index);
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unsupported encoding type!");
            }
            Log.i(TAG, "getVersionFromLCD() panelVersion=" + panelVersion);
            return panelVersion;
        } catch (RemoteException e2) {
            Log.e(TAG, "getVersionFromLCD() RemoteException " + e2);
            return null;
        }
    }

    private String getMobilePhoneName() {
        return SystemProperties.get("ro.product.model");
    }

    public int[] getSampleBrightness() {
        return this.mSampleBrightness;
    }

    public int[] getSampleAlpha() {
        return this.mSampleAlpha;
    }

    /* access modifiers changed from: private */
    public void setFPAuthState(boolean authState) {
        HwPhoneWindowManager policy = (HwPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        if (policy != null) {
            Log.d(TAG, "setFPAuthState:" + authState);
            policy.getPhoneWindowManagerEx().setFPAuthState(authState);
        }
    }

    private void getNotchState() {
        this.mDisplayNotchStatus = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "display_notch_status", 0, -2);
        Log.d(TAG, "getNotchState = " + this.mDisplayNotchStatus);
    }

    private void transferNotchRoundCorner(int status) {
        if (this.mDisplayNotchStatus == 1) {
            transferSwitchStatusToSurfaceFlinger(status);
        }
    }

    private void transferSwitchStatusToSurfaceFlinger(int value) {
        String str;
        StringBuilder sb;
        int val = value;
        Parcel dataIn = Parcel.obtain();
        try {
            IBinder sfBinder = ServiceManager.getService("SurfaceFlinger");
            dataIn.writeInt(val);
            if (sfBinder != null && !sfBinder.transact(NOTCH_ROUND_CORNER_CODE, dataIn, null, 1)) {
                Log.d(TAG, "transferSwitchStatusToSurfaceFlinger error!");
            }
            if (value == 0) {
                this.mNotchConerStatusChanged = true;
            } else {
                this.mNotchConerStatusChanged = false;
            }
            str = TAG;
            sb = new StringBuilder();
        } catch (RemoteException e) {
            Log.e(TAG, "transferSwitchStatusToSurfaceFlinger catch RemoteException");
            if (value == 0) {
                this.mNotchConerStatusChanged = true;
            } else {
                this.mNotchConerStatusChanged = false;
            }
            str = TAG;
            sb = new StringBuilder();
        } catch (Exception e2) {
            Log.e(TAG, "transferSwitchStatusToSurfaceFlinger catch Exception ");
            if (value == 0) {
                this.mNotchConerStatusChanged = true;
            } else {
                this.mNotchConerStatusChanged = false;
            }
            str = TAG;
            sb = new StringBuilder();
        } catch (Throwable th) {
            if (value == 0) {
                this.mNotchConerStatusChanged = true;
            } else {
                this.mNotchConerStatusChanged = false;
            }
            Log.d(TAG, "notch coner status change to  = " + value);
            dataIn.recycle();
            throw th;
        }
        sb.append("notch coner status change to  = ");
        sb.append(value);
        Log.d(str, sb.toString());
        dataIn.recycle();
    }

    public int getMaskAlpha(int currentLight) {
        int alpha = 0;
        Log.d(TAG, " currentLight:" + currentLight);
        if (this.mSampleBrightness == null || this.mSampleAlpha == null || this.mSampleBrightness.length == 0 || this.mSampleAlpha.length == 0 || this.mSampleBrightness.length != this.mSampleAlpha.length) {
            Log.i(TAG, "get Brightness and Alpha config error, use default config.");
            this.mSampleBrightness = sDefaultSampleBrightness;
            this.mSampleAlpha = sDefaultSampleAlpha;
        }
        if (currentLight > this.mSampleBrightness[this.mSampleBrightness.length - 1]) {
            return 0;
        }
        int i = 0;
        while (true) {
            if (i >= this.mSampleBrightness.length) {
                break;
            }
            Log.d(TAG, " mSampleBrightness :[" + i + "]" + this.mSampleBrightness[i]);
            if (currentLight == this.mSampleBrightness[i]) {
                alpha = this.mSampleAlpha[i];
                break;
            } else if (currentLight >= this.mSampleBrightness[i]) {
                i++;
            } else if (i == 0) {
                alpha = this.mSampleAlpha[0];
            } else {
                alpha = queryAlphaImpl(currentLight, this.mSampleBrightness[i - 1], this.mSampleAlpha[i - 1], this.mSampleBrightness[i], this.mSampleAlpha[i]);
            }
        }
        if (alpha > this.mSampleAlpha[0] || alpha < 0) {
            alpha = 0;
        }
        Log.d(TAG, " alpha:" + alpha);
        return alpha;
    }

    private int queryAlphaImpl(int currLight, int preLevelLight, int preLevelAlpha, int lastLevelLight, int lastLevelAlpha) {
        return (((currLight - preLevelLight) * (lastLevelAlpha - preLevelAlpha)) / (lastLevelLight - preLevelLight)) + preLevelAlpha;
    }

    private int getHbmType() {
        String board = SystemProperties.get("ro.product.board", "UNKOWN").toUpperCase(Locale.US);
        return 0;
    }
}
