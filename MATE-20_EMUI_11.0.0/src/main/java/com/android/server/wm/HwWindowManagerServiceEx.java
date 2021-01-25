package com.android.server.wm;

import android.animation.TimeInterpolator;
import android.app.ActivityManager;
import android.app.WindowConfiguration;
import android.aps.IApsManager;
import android.common.HwFrameworkFactory;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.HwPCMultiWindowCompatibility;
import android.database.ContentObserver;
import android.freeform.HwFreeFormUtils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.GraphicBuffer;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.HwFoldScreenState;
import android.hardware.fingerprint.FingerprintManager;
import android.iawareperf.UniPerf;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.pc.IHwPCManager;
import android.provider.Settings;
import android.trustspace.TrustSpaceManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.CoordinationModeUtils;
import android.util.DisplayMetrics;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.TypedValue;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.WindowManagerPolicyConstants;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RemoteViews;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ToBooleanFunction;
import com.android.internal.view.IDragAndDropPermissions;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.appactcontrol.HwAppActController;
import com.android.server.displayside.HwDisplaySideRegionConfig;
import com.android.server.gesture.DefaultGestureNavManager;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.input.InputManagerServiceEx;
import com.android.server.multiwin.HwMultiWinConstants;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.policy.WindowManagerPolicyEx;
import com.android.server.wm.utils.HwDisplaySizeUtil;
import com.google.android.collect.Sets;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.view.HwTaskSnapshotWrapper;
import com.huawei.android.view.IHwMouseEventListener;
import com.huawei.android.view.IHwMultiDisplayBasicModeDragStartListener;
import com.huawei.android.view.IHwMultiDisplayBitmapDragStartListener;
import com.huawei.android.view.IHwMultiDisplayDragStartListener;
import com.huawei.android.view.IHwMultiDisplayDragStateListener;
import com.huawei.android.view.IHwMultiDisplayDropStartListener;
import com.huawei.android.view.IHwMultiDisplayDroppableListener;
import com.huawei.android.view.IHwMultiDisplayPhoneOperateListener;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.iimagekit.blur.BlurAlgorithm;
import com.huawei.server.HwBasicPlatformFactory;
import com.huawei.server.HwPCFactory;
import com.huawei.server.HwPartIawareUtil;
import com.huawei.server.HwPartMagicWindowServiceFactory;
import com.huawei.server.hwmultidisplay.DefaultHwMultiDisplayUtils;
import com.huawei.server.hwmultidisplay.windows.DefaultHwWindowsCastManager;
import huawei.android.hwutil.HwFullScreenDisplay;
import huawei.com.android.server.policy.HwGlobalActionsData;
import huawei.cust.HwCfgFilePolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class HwWindowManagerServiceEx implements IHwWindowManagerServiceEx {
    private static final String ANDROID_PACKAGE = "android";
    private static final int ANIMATION_SIZE = 2;
    private static final String ANIM_STYLE = SystemProperties.get("ro.feature.animation.style", "");
    private static final int APP_ASSOC_WINDOWADD = 8;
    private static final int APP_ASSOC_WINDOWDEL = 9;
    private static final int APP_ASSOC_WINDOWUPDATE = 27;
    private static final int APP_ASSOC_WINDOWUPOPS = 10;
    private static final String ASSOC_PKGNAME = "pkgname";
    private static final String ASSOC_RELATION_TYPE = "relationType";
    private static final String ASSOC_UID = "uid";
    private static final String ASSOC_WINDOW = "window";
    private static final String ASSOC_WINDOW_ALPHA = "alpha";
    private static final String ASSOC_WINDOW_HASHCODE = "hashcode";
    private static final String ASSOC_WINDOW_HAS_SURFACE = "hasSurface";
    private static final String ASSOC_WINDOW_HEIGHT = "height";
    private static final String ASSOC_WINDOW_MODE = "windowmode";
    private static final String ASSOC_WINDOW_PHIDE = "permanentlyhidden";
    private static final String ASSOC_WINDOW_TYPE = "windowtype";
    private static final String ASSOC_WINDOW_WIDTH = "width";
    private static final String BLUR_LEVEL_KEY = "privacy.snapshot.blur.level";
    private static final int BLUR_LEVEL_LIGHT = 1;
    private static final int BLUR_LEVEL_NOT = 0;
    private static final int BLUR_LEVEL_UNDEFINED = -1;
    private static final int BLUR_LEVEL_WEIGH = 2;
    private static final int BLUR_STYLE_INVALID = -1;
    private static final long CARD_ANIMATION_DURATION = 280;
    private static final String CATEGORY_ENTER_RECENT = "enter_recent";
    private static final String CATEGORY_EXIT_RECENT = "exit_recent";
    private static final boolean CONFIG_PRIVACY_SNAPSHOT_BLUR = SystemProperties.getBoolean("hw_mc.privacy.snapshot_blur", true);
    private static final boolean DBG = false;
    private static final int DEFAULT_BLUR_MAP_SIZE = 10;
    private static final float DEFAULT_DECIMAL_RATIO = 10000.0f;
    private static final float DEFAULT_SCREEN_RATIO = 1.33f;
    private static final String DOCK_PACKAGE = "com.huawei.hwdockbar";
    private static final int DRAG_ACTION_CLEAR_SAVED_WINDOWSTATE = 10001;
    private static final int DRAG_ACTION_SAVE_WINDOWSTATE = 10000;
    private static final long DYN_CORNER_ANIMATION_TIME = 150;
    private static final long DYN_CORNER_START_DELAY = 350;
    private static final int ENTER_INDEX = 0;
    private static final int EXIT_INDEX = 1;
    private static final Interpolator FAST_IN_SLOW_OUT = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    private static final int FILLET_RADIUS_SIZE = SystemProperties.getInt("ro.config.fillet_radius_size", 0);
    private static final int FORBIDDEN_ADDVIEW_BROADCAST = 1;
    private static final String GESTURE_CATEGORY_KEY = "category";
    private static final String GESTURE_DETECTOR_HANDLER_THREAD = "gesture_detector_handler_thread";
    private static final int HALF = 2;
    private static final int ICON_RADIUS_FACTOR = 2;
    private static final float INIT_LAND_RADIUS_RATIO = 1.9379846f;
    private static final float INIT_RADIUS_RATIO = 1.6666666f;
    private static final String INTELLIGENT_CORNER_RADIUS_KEY = "origin_card_corner";
    private static final String INTELLIGENT_RECT_KEY = "origin_card_rect";
    private static final int INVALID_INDEX = -1;
    private static final boolean IS_FOLDABLE = (!SystemProperties.get("ro.config.hw_fold_disp").isEmpty() || !SystemProperties.get("persist.sys.fold.disp.size").isEmpty());
    private static final boolean IS_LAND_ANI_OPEN = SystemProperties.getBoolean("persist.debug.land_ani", true);
    private static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", "").equals(""));
    private static final boolean IS_OPS_HANDLER_THREAD_DISABLED = SystemProperties.getBoolean("ro.config.hwopshandler.disable", false);
    private static final boolean IS_SUPER_LITE_ANIMA_STYLE = "supersimple".equals(ANIM_STYLE);
    private static final boolean IS_SUPPORT_SINGLE_MODE = SystemProperties.getBoolean("ro.feature.wms.singlemode", true);
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", ""));
    private static final TimeInterpolator[] LAND_ENTER_ANIM_ALPHA_PHASE_INTERPOLATORS = {new PathInterpolator(0.0f, 0.0f, 1.0f, 1.0f), new PathInterpolator(0.32f, 0.94f, 0.6f, 1.0f), new PathInterpolator(0.0f, 0.0f, 1.0f, 1.0f)};
    private static final float[] LAND_ENTER_ANIM_ALPHA_PHASE_PROGRESSES = {0.0f, 0.0f, 1.0f, 1.0f};
    private static final float[] LAND_ENTER_ANIM_ALPHA_PHASE_TIMES = {0.0f, 0.1f, 0.2f, 1.0f};
    private static final long LAND_ENTER_ANIM_DURATION = 400;
    private static final int LAND_ENTER_CORNER_RADIUS_BEGIN = 230;
    private static final int LAND_ENTER_CORNER_RADIUS_END = 50;
    private static final float LAND_ENTER_ICON_ALPHA_THRESHOULD = 1.0E-6f;
    private static final float LAND_ENTER_SCALE_THRESHOULD = 1.0E-6f;
    private static final PathInterpolator LAND_LAUNCH_ANIM_X_INTERPOLATOR = new PathInterpolator(0.3f, 0.0f, 0.1f, 1.0f);
    private static final PathInterpolator LAND_LAUNCH_ANIM_Y_INTERPOLATOR = new PathInterpolator(0.0f, 0.2f, 0.0f, 1.0f);
    private static final float LAZY_SCALE = 0.75f;
    private static final int LAZY_TYPE_DEFAULT = 0;
    private static final int LAZY_TYPE_LEFT = 1;
    private static final int LAZY_TYPE_RIGHT = 2;
    private static final int MATRIX33_VALUES_SIZE = 9;
    private static final int MAX_SUPPORT_HWFREEDOM = 2;
    private static final int MSG_REPORT_LOG = 2;
    private static final long MSG_ROG_FREEZE_TIME_DELEAYED = 6000;
    private static final long MSG_ROG_RESET_LAUNCH_TIME_DELEAYED = 3000;
    private static final String NOTCH_PROP = SystemProperties.get("ro.config.hw_notch_size", "");
    private static final int NOTIFY_FINGER_WIN_COVERED = 101;
    private static final int OFFSET_INDEX = 3;
    private static final float PIXEL_PRECISION = 0.5f;
    private static final String QUICK_SWITCH_SINGLE_HAND_MODE = "launcher_quick_switch_single_hand_mode";
    private static final int QUICK_SWITCH_SINGLE_HAND_TIMEOUT = 2000;
    private static final String QUICK_SWITCH_SINGLE_HAND_TYPE = "launcher_quick_switch_single_hand_type";
    private static final int RESET_LAUNCH_ORIGIN_TARGET = 102;
    private static final int RESET_ROTATION_SEAMLESS_BLOCK = 103;
    private static final int RESET_ROTATION_SEAMLESS_BLOCK_DELEAYED = 500;
    private static final String RESOURCE_APPASSOC = "RESOURCE_APPASSOC";
    private static final int ROG_FREEZE_TIMEOUT = 100;
    private static final int ROTATION_DEGREE_0 = 0;
    private static final int ROTATION_DEGREE_270 = 270;
    private static final int ROTATION_DEGREE_90 = 90;
    private static final int SIDE_LEFT_BOTTOM = 2;
    private static final int SIDE_NONE = 0;
    private static final int SIDE_RIGHT_BOTTOM = 3;
    private static final String SPLASH_SCREEN = "Splash Screen";
    private static final String SYSTEMUI_PACKAGE = "com.android.systemui";
    static final String TAG = "HwWindowManagerServiceEx";
    private static final String TENCENT_MM = "com.tencent.mm";
    private static final String TENCENT_QQ = "com.tencent.mobileqq";
    private static final int TYPE_SINGLE_HAND = 2;
    private static final int UPDATE_WINDOW_STATE = 0;
    private static final int WINDOW_ABOVE_TIMEOUT = 200;
    private static final int WINDOW_ABOVE_TIMEOUT_DURATION = 2000;
    private static final float WINDOW_ABOVE_VISIBLE_HIDE = 0.0f;
    private static final float WINDOW_ABOVE_VISIBLE_SHOW = 1.0f;
    private static final int WMS_SET_LAZY_MODE = 7009;
    private static boolean isEnableRoundCornerDisplay;
    private static int lastFocusPid = -1;
    private static boolean mIsSwitched = false;
    private static WindowState sDragWin;
    private static DefaultHwMultiDisplayUtils sHwMultiDisplayUtils = HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwMultiDisplayUtils();
    private static DefaultHwWindowsCastManager sHwWindowsCastManager = HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwWindowsCastManager();
    private static WindowState sTouchWin = null;
    private Animation[] mActivityFinishAnimations = new Animation[2];
    private Animation[] mActivityStartAnimations = new Animation[2];
    private SurfaceControl.Transaction mAlphaTransaction = new SurfaceControl.Transaction();
    private IHwMultiDisplayBasicModeDragStartListener mBasicModeDragListenerForMultiDisplay;
    private IHwMultiDisplayBitmapDragStartListener mBitmapDragListenerForMultiDisplay;
    final Context mContext;
    Configuration mCurNaviConfiguration;
    private HashMap<Integer, Integer> mCurSecureTasks = new HashMap<>();
    protected int mCurrentFoldDisplayMode;
    private IHwMultiDisplayDragStartListener mDragListenerForMultiDisplay;
    private WindowState mDragLocWindowState = null;
    private IHwMultiDisplayDropStartListener mDropListenerForMultiDisplay;
    private WindowState mDropWindowState = null;
    private Bitmap mEnterIconBitmap;
    private int mEnterIconHeight = 0;
    private int mEnterIconWidth = 0;
    private float mEnterPivotX = -1.0f;
    private float mEnterPivotY = -1.0f;
    private int mExitFlag = 0;
    private Bitmap mExitIconBitmap;
    private int mExitIconHeight = 0;
    private int mExitIconWidth = 0;
    private float mExitPivotX = -1.0f;
    private float mExitPivotY = -1.0f;
    private List<String> mFocusList = new ArrayList();
    private Handler mGestureDetectorHandler;
    private HandlerThread mGestureDetectorHandlerThread;
    private HandlerThread mHandlerThread;
    private final Map<Integer, HwFreeWindowFloatIconInfo> mHwFreeWindowFloatIconInfos = new ConcurrentHashMap(2);
    private Handler mHwHandler = new Handler() {
        /* class com.android.server.wm.HwWindowManagerServiceEx.AnonymousClass3 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 2) {
                DisplayContent displayContent = HwWindowManagerServiceEx.this.mIWmsInner.getService().getDefaultDisplayContentLocked();
                if (displayContent != null) {
                    displayContent.getDisplayRotation().getHwDisplayRotationEx().handleReportLog(msg);
                }
            } else if (i != 200) {
                boolean isCovered = false;
                switch (i) {
                    case 100:
                        Slog.d(HwWindowManagerServiceEx.TAG, "ROG_FREEZE_TIMEOUT");
                        SurfaceControl.unfreezeDisplay();
                        return;
                    case 101:
                        if (HwWindowManagerServiceEx.this.mContext.getPackageManager().hasSystemFeature("android.hardware.fingerprint")) {
                            if (msg.arg1 == 1) {
                                isCovered = true;
                            }
                            Rect frame = (Rect) msg.obj;
                            if (((FingerprintManager) HwWindowManagerServiceEx.this.mContext.getSystemService("fingerprint")) != null) {
                                Slog.i(HwWindowManagerServiceEx.TAG, "handleMessage: NOTIFY_FINGER_WIN_COVERED isCovered=" + isCovered + " frame=" + frame);
                                return;
                            }
                            return;
                        }
                        return;
                    case 102:
                        Slog.i(HwWindowManagerServiceEx.TAG, "reset launch origin and target from message!");
                        synchronized (HwWindowManagerServiceEx.this.mIWmsInner.getGlobalLock()) {
                            if (HwWindowManagerServiceEx.this.mScreenRotationAnimation != null) {
                                HwWindowManagerServiceEx.this.finishLandOpenAnimation();
                                HwWindowManagerServiceEx.this.mScreenRotationAnimation = null;
                            }
                        }
                        HwWindowManagerServiceEx.this.setLandAnimationInfo(false, null);
                        return;
                    case 103:
                        Slog.i(HwWindowManagerServiceEx.TAG, "setInputBlock false timeout");
                        HwWindowManagerServiceEx.this.mIWmsInner.getService().mInputManagerCallback.thawInputDispatchingLw();
                        return;
                    default:
                        return;
                }
            } else {
                Slog.i(HwWindowManagerServiceEx.TAG, "handle window above timeout");
                HwWindowManagerServiceEx.this.setAboveAppWindowsContainersVisible(true);
            }
        }
    };
    private InputManagerServiceEx.DefaultHwInputManagerLocalService mHwInputManagerInternal;
    IHwWindowManagerInner mIWmsInner = null;
    private ByteBuffer mIconByteBuffer = null;
    private SurfaceControl mIconSurfaceControl;
    private Rect mIntelligentCardPosition;
    private float mIntelligentCustomCornerRadius = 0.0f;
    private boolean mIsAboveWinHidden = false;
    private boolean mIsClipRectDynamicCornerNeeded = false;
    boolean mIsCoverOpen = true;
    private IHwMultiDisplayDroppableListener mIsDroppableListener;
    private boolean mIsGainFocus = true;
    private boolean mIsHintShowing;
    private boolean mIsHwSafeMode;
    private boolean mIsIgnoreFrozen = false;
    private boolean mIsLanucherLandscape = false;
    private boolean mIsLastCoveredState;
    boolean mIsLayoutNaviBar = false;
    private volatile boolean mIsNeedBlur = true;
    private boolean mIsNeedDefaultAnimation = false;
    private boolean mIsNeedLandAni;
    private boolean mIsQuickSwitchSingleHand = false;
    private boolean mIsScreenshotLayerAdjusted = false;
    private boolean mIsSingleHandSwitch;
    private boolean mIsSurfaceIconSet = false;
    private Rect mLandOpenAppDisplayRect;
    private int mLastConfigrationChange;
    private Rect mLastCoveredFrame = new Rect();
    private int mLastFloatIconlayerTaskId = -1;
    private AppWindowToken mLastIconLayerWindowToken = null;
    private int mLastTokenLayer = -1;
    private String mLaunchedPackage;
    private int mLazyModeOnEx;
    private Interpolator mMagicWindowMoveInterpolator = null;
    private IHwMouseEventListener mMouseEventListener;
    private WindowManagerPolicyConstants.PointerEventListener mMousePointerListener = new WindowManagerPolicyConstants.PointerEventListener() {
        /* class com.android.server.wm.HwWindowManagerServiceEx.AnonymousClass6 */

        public void onPointerEvent(MotionEvent motionEvent) {
            if (motionEvent.isFromSource(8194)) {
                synchronized (HwWindowManagerServiceEx.this.mIWmsInner.getGlobalLock()) {
                    if (HwWindowManagerServiceEx.this.mMouseEventListener != null) {
                        try {
                            HwWindowManagerServiceEx.this.mMouseEventListener.onReportMousePosition(motionEvent.getRawX(), motionEvent.getRawY());
                        } catch (RemoteException e) {
                            Slog.e(HwWindowManagerServiceEx.TAG, "onReportMousePosition RemoteException.");
                        }
                    }
                }
            }
        }
    };
    private IHwMultiDisplayDragStateListener mMultiDisplayDragStateListener;
    private ArrayList<SurfaceControl> mMultiWinSurface = new ArrayList<>();
    private OpsUpdateHandler mOpsHandler;
    private IHwMultiDisplayPhoneOperateListener mPhoneOperateListenerForMultiDisplay;
    private HashMap<Integer, Integer> mPreSecureTasks = new HashMap<>();
    private final Runnable mQuickSwitchSingleHandRunnable = new Runnable() {
        /* class com.android.server.wm.HwWindowManagerServiceEx.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            HwWindowManagerServiceEx.this.onEndQuickSwitchSingleHand();
        }
    };
    private final Runnable mReevaluateStatusBarSize = new Runnable() {
        /* class com.android.server.wm.HwWindowManagerServiceEx.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            synchronized (HwWindowManagerServiceEx.this.mIWmsInner.getGlobalLock()) {
                HwWindowManagerServiceEx.this.mIsIgnoreFrozen = true;
                if (HwWindowManagerServiceEx.this.mIsLayoutNaviBar) {
                    HwWindowManagerServiceEx.this.mIsLayoutNaviBar = false;
                    HwWindowManagerServiceEx.this.mCurNaviConfiguration = HwWindowManagerServiceEx.this.mIWmsInner.computeNewConfiguration(HwWindowManagerServiceEx.this.mIWmsInner.getDefaultDisplayContentLocked().getDisplayId());
                    HwWindowManagerServiceEx.this.performhwLayoutAndPlaceSurfacesLocked();
                } else {
                    HwWindowManagerServiceEx.this.performhwLayoutAndPlaceSurfacesLocked();
                }
            }
        }
    };
    private ScreenRotationAnimation mScreenRotationAnimation;
    private ArrayList<WindowState> mSecureScreenRecords = new ArrayList<>();
    private ArrayList<WindowState> mSecureScreenShot = new ArrayList<>();
    private ArrayList<WindowState> mSecureWindows = new ArrayList<>();
    private TaskStack mStackToDraw = null;
    private SurfaceFactory mSurfaceFactory = $$Lambda$6DEhn1zqxqV5_Ytb_NyzMW23Ano.INSTANCE;
    private SurfaceControl mTopTaskSurfaceControlLeash;
    private TransactionFactory mTransactionFactory = $$Lambda$hBnABSAsqXWvQ0zKwHWE4BZ3Mc0.INSTANCE;
    private final Handler mUiHandler;
    private Map<Task, SnapshotBlurInfo> mWaitBlurTaskMap = new HashMap(10);

    static {
        boolean z = false;
        isEnableRoundCornerDisplay = true;
        try {
            if (HwCfgFilePolicy.getCfgFile("display/RoundCornerDisplay/config.xml", 0) != null) {
                z = true;
            }
            isEnableRoundCornerDisplay = z;
        } catch (NoClassDefFoundError e) {
            Slog.e(TAG, "getCfgFile error");
        }
    }

    public HwWindowManagerServiceEx(IHwWindowManagerInner wms, Context context) {
        this.mIWmsInner = wms;
        this.mContext = context;
        this.mGestureDetectorHandlerThread = new HandlerThread(GESTURE_DETECTOR_HANDLER_THREAD);
        this.mGestureDetectorHandlerThread.start();
        this.mGestureDetectorHandler = new Handler(this.mGestureDetectorHandlerThread.getLooper());
        if (!IS_OPS_HANDLER_THREAD_DISABLED) {
            this.mHandlerThread = new HandlerThread("hw_ops_handler_thread");
            this.mHandlerThread.start();
            this.mOpsHandler = new OpsUpdateHandler(this.mHandlerThread.getLooper());
        }
        this.mUiHandler = UiThread.getHandler();
    }

    public void onRequestedOverrideConfigurationChanged(int displayId, Configuration originConfig, Configuration overrideConfig) {
        if (originConfig != null && overrideConfig != null) {
            this.mLastConfigrationChange = originConfig.diff(overrideConfig);
        }
    }

    public boolean isUiModeChangeWhenStatusBarDisplayed(DisplayContent displayContent) {
        if (displayContent == null) {
            return false;
        }
        boolean isKeyguardLocked = this.mIWmsInner.getService().isKeyguardLocked();
        if (!isUiModeChange() || !isStatusBarExpanded(displayContent) || isKeyguardLocked) {
            return false;
        }
        return true;
    }

    private boolean isUiModeChange() {
        int uiModeChange = this.mLastConfigrationChange & 512;
        this.mLastConfigrationChange = 0;
        if (uiModeChange != 0) {
            return true;
        }
        return false;
    }

    private boolean isStatusBarExpanded(DisplayContent displayContent) {
        WindowState windowState = displayContent.getDisplayPolicy().getStatusBar();
        DisplayMetrics displayMetrics = displayContent.getDisplayMetrics();
        if (windowState == null) {
            return false;
        }
        Rect statusBarRect = windowState.getFrameLw();
        if (statusBarRect.width() * statusBarRect.height() >= displayMetrics.widthPixels * displayMetrics.heightPixels) {
            return true;
        }
        return false;
    }

    public void onChangeConfiguration(MergedConfiguration mergedConfiguration, WindowState ws) {
        DisplayInfo displayInfo;
        if (HwPCUtils.enabled() && HwPCUtils.isPcCastModeInServer() && !HwPCUtils.isHiCarCastMode() && ws != null && HwPCUtils.isValidExtDisplayId(ws.getDisplayId()) && mergedConfiguration != null && ws.getTask() != null && ws.getTask().getWindowingMode() == 1) {
            Configuration cf = mergedConfiguration.getOverrideConfiguration();
            DisplayContent dc = ws.getDisplayContent();
            if (cf != null && dc != null && (displayInfo = ws.getDisplayInfo()) != null) {
                int displayWidth = displayInfo.logicalWidth;
                int displayHeight = displayInfo.logicalHeight;
                float scale = ((float) displayInfo.logicalDensityDpi) / 160.0f;
                cf.screenWidthDp = (int) ((((float) displayWidth) / scale) + 0.5f);
                cf.screenHeightDp = (int) ((((float) displayHeight) / scale) + 0.5f);
                mergedConfiguration.setOverrideConfiguration(cf);
                ws.onConfigurationChanged(mergedConfiguration.getMergedConfiguration());
                HwPCUtils.log(TAG, "set pc fullscreen, width:" + displayWidth + " height:" + displayHeight + " scale:" + scale + " cf.screenWidthDp:" + cf.screenWidthDp + " cf.screenHeightDp:" + cf.screenHeightDp);
            }
        }
    }

    private boolean isInputTargetWindow(WindowState windowState, WindowState inputTargetWin) {
        Task task = windowState.getTask();
        boolean z = true;
        if (inputTargetWin != null && inputTargetWin.getTask() != null && task != null) {
            return inputTargetWin.getTask().mTaskId == task.mTaskId;
        }
        synchronized (this.mIWmsInner.getGlobalLock()) {
            DisplayContent dc = this.mIWmsInner.getRoot().getDisplayContent(HwPCUtils.getPCDisplayID());
            if (dc == null) {
                return false;
            }
            WindowState win = dc.mCurrentFocusInHwPc;
            if (win == null || win.getTask() == null || task == null) {
                return false;
            }
            if (win.getTask().mTaskId != task.mTaskId) {
                z = false;
            }
            return z;
        }
    }

    public void adjustWindowPosForPadPC(Rect containingFrame, Rect contentFrame, WindowState imeWin, WindowState inputTargetWin, WindowState win) {
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.enabledInPad() && HwPCUtils.isValidExtDisplayId(win.getDisplayId()) && imeWin != null && imeWin.isVisibleNow() && isInputTargetWindow(win, inputTargetWin) && win.getTask() != null) {
            int windowState = -1;
            ActivityRecord r = ActivityRecord.forToken(win.getAttrs().token);
            if (r != null) {
                if (r instanceof ActivityRecordBridge) {
                    windowState = ((ActivityRecordBridge) r).getWindowState();
                }
                if (windowState != -1 && !HwPCMultiWindowCompatibility.isLayoutFullscreen(windowState) && !HwPCMultiWindowCompatibility.isLayoutMaximized(windowState) && !contentFrame.isEmpty()) {
                    int D1 = 0;
                    int D2 = 0;
                    if (win.getAttrs() == null || (win.getAttrs().softInputMode & 240) != 16) {
                        Rect imeBounds = new Rect();
                        imeWin.getBounds(imeBounds);
                        if (!imeBounds.isEmpty() && containingFrame.bottom > imeBounds.top) {
                            D1 = imeBounds.top - containingFrame.bottom;
                            D2 = contentFrame.top - containingFrame.top;
                        }
                    } else if (containingFrame.bottom > contentFrame.bottom) {
                        D1 = contentFrame.bottom - containingFrame.bottom;
                        D2 = contentFrame.top - containingFrame.top;
                    }
                    int offsetY = Math.max(D1, D2);
                    Rect taskBounds = new Rect();
                    if (offsetY < 0) {
                        win.getTask().getBounds(taskBounds);
                        taskBounds.offset(0, offsetY);
                        win.getTask().setBounds(taskBounds);
                        containingFrame.offset(0, offsetY);
                    }
                }
            }
        }
    }

    public void layoutWindowForPadPCMode(WindowState win, WindowState inputTargetWin, WindowState imeWin, Rect pf, Rect df, Rect cf, Rect vf, int contentBottom) {
        if (isInputTargetWindow(win, inputTargetWin)) {
            int inputMethodTop = 0;
            if (imeWin != null && imeWin.isVisibleLw()) {
                inputMethodTop = Math.min(contentBottom, Math.max(imeWin.getDisplayFrameLw().top, imeWin.getContentFrameLw().top) + imeWin.getGivenContentInsetsLw().top);
            }
            if (inputMethodTop > 0) {
                vf.bottom = inputMethodTop;
                cf.bottom = inputMethodTop;
                df.bottom = inputMethodTop;
                pf.bottom = inputMethodTop;
            }
        }
    }

    public void sendUpdateAppOpsState() {
        OpsUpdateHandler opsUpdateHandler = this.mOpsHandler;
        if (opsUpdateHandler != null) {
            opsUpdateHandler.removeMessages(0);
            this.mOpsHandler.sendEmptyMessage(0);
        }
    }

    public void setAppOpHideHook(WindowState win, boolean isVisible) {
        if (!isVisible) {
            setAppOpVisibilityChecked(win, isVisible);
        }
    }

    private boolean setAppOpVisibilityChecked(WindowState win, boolean isVisible) {
        if (isVisible) {
            setWinAndChildrenVisibility(win, true);
            return true;
        } else if (allowAnyway(win)) {
            setWinAndChildrenVisibility(win, true);
            return true;
        } else {
            setWinAndChildrenVisibility(win, false);
            sendForbiddenMessage(win);
            return false;
        }
    }

    private void setWinAndChildrenVisibility(WindowState win, boolean isVisible) {
        if (win != null) {
            win.setAppOpVisibilityLw(isVisible);
            int size = win.mChildren.size();
            Slog.i(TAG, "this win:" + win + " hase children size:" + size);
            for (int i = 0; i < size; i++) {
                setWinAndChildrenVisibility((WindowState) win.mChildren.get(i), isVisible);
            }
        }
    }

    private boolean allowAnyway(WindowState win) {
        if (win == null) {
            return true;
        }
        if (!checkFullWindowWithoutTransparent(win.mAttrs)) {
            return false;
        }
        Slog.i(TAG, "don't allow anyway," + win);
        return false;
    }

    private void sendForbiddenMessage(WindowState win) {
        OpsUpdateHandler opsUpdateHandler = this.mOpsHandler;
        if (opsUpdateHandler != null) {
            Message msg = opsUpdateHandler.obtainMessage(1);
            Bundle bundle = new Bundle();
            bundle.putInt("uid", win.getOwningUid());
            bundle.putString("package", win.getOwningPackage());
            msg.setData(bundle);
            this.mOpsHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendForbiddenBroadcast(Bundle data) {
        Intent preventIntent = new Intent("com.android.server.wm.addview.preventnotify");
        preventIntent.putExtras(data);
        this.mContext.sendBroadcastAsUser(preventIntent, UserHandle.ALL);
    }

    private boolean checkFullWindowWithoutTransparent(WindowManager.LayoutParams attrs) {
        return attrs.width == -1 && attrs.height == -1 && attrs.alpha != 0.0f;
    }

    public void setVisibleFromParent(WindowState win) {
        if (parentHiddenByAppOp(win)) {
            Slog.i(TAG, "parent is hidden by app ops, should also hide this win:" + win);
            setWinAndChildrenVisibility(win, false);
        }
    }

    private boolean parentHiddenByAppOp(WindowState win) {
        if (win == null || !win.isChildWindow()) {
            return false;
        }
        if (!win.getParentWindow().mAppOpVisibility) {
            return true;
        }
        return parentHiddenByAppOp(win.getParentWindow());
    }

    public void checkSingleHandMode(AppWindowToken oldFocus, AppWindowToken newFocus) {
        if ((oldFocus != newFocus) && newFocus != null) {
            int requestedOrientation = newFocus.mOrientation;
            if (requestedOrientation == 0 || requestedOrientation == 6 || requestedOrientation == 8 || requestedOrientation == 11) {
                Slog.i(TAG, "requestedOrientation: " + requestedOrientation);
                Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
            }
        }
    }

    /* access modifiers changed from: private */
    public class OpsUpdateHandler extends Handler {
        public OpsUpdateHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i == 0) {
                HwWindowManagerServiceEx.this.mIWmsInner.updateAppOpsState();
            } else if (i == 1) {
                HwWindowManagerServiceEx.this.sendForbiddenBroadcast(msg.getData());
            }
        }
    }

    public void updateAppView(RemoteViews remoteViews) {
        WindowManagerPolicy policy = this.mIWmsInner.getPolicy();
        if (policy instanceof HwPhoneWindowManager) {
            ((HwPhoneWindowManager) policy).updateAppView(remoteViews);
        }
    }

    public void removeAppView() {
        removeAppView(true);
    }

    public void removeAppView(boolean isNeedBtnView) {
        WindowManagerPolicy policy = this.mIWmsInner.getPolicy();
        if (policy instanceof HwPhoneWindowManager) {
            ((HwPhoneWindowManager) policy).removeAppView(isNeedBtnView);
        }
    }

    public boolean isFullScreenDevice() {
        return HwFullScreenDisplay.isFullScreenDevice();
    }

    public float getDeviceMaxRatio() {
        return HwFullScreenDisplay.getDeviceMaxRatio();
    }

    public float getDefaultNonFullMaxRatio() {
        return HwFullScreenDisplay.getDefaultNonFullMaxRatio();
    }

    public float getExclusionNavBarMaxRatio() {
        return HwFullScreenDisplay.getExclusionNavBarMaxRatio();
    }

    public void setNotchHeight(int notchHeight) {
        HwFullScreenDisplay.setNotchHeight(notchHeight);
    }

    public void getAppDisplayRect(float appMaxRatio, Rect rect, int left, int rotation) {
        HwFullScreenDisplay.getAppDisplayRect(appMaxRatio, rect, left, rotation);
    }

    public Rect getTopAppDisplayBounds(float appMaxRatio, int rotation, int screenWidth) {
        return HwFullScreenDisplay.getTopAppDisplayBounds(appMaxRatio, rotation, screenWidth);
    }

    public List<String> getNotchSystemApps() {
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwNotchScreenWhiteConfig().getNotchSystemApps();
    }

    public int getAppUseNotchMode(String packageName) {
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwNotchScreenWhiteConfig().getAppUseNotchMode(packageName);
    }

    public boolean isInNotchAppWhitelist(WindowState win) {
        WindowManagerPolicyEx.WindowStateEx windowStateEx = new WindowManagerPolicyEx.WindowStateEx();
        windowStateEx.setWindowState(win);
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwNotchScreenWhiteConfig().isNotchAppInfo(windowStateEx);
    }

    private void getAlertWindows(ArrayMap<WindowState, Integer> windows) {
        synchronized (this.mIWmsInner.getGlobalLock()) {
            for (WindowState win : this.mIWmsInner.getWindowMap().values()) {
                if (!(win == null || win.mAttrs == null || win.mSession == null)) {
                    if (!windows.containsKey(win)) {
                        if (win.mAppOp == 24) {
                            if (this.mIWmsInner.getAppOps() != null) {
                                windows.put(win, Integer.valueOf(this.mIWmsInner.getAppOps().startOpNoThrow(win.mAppOp, win.getOwningUid(), win.getOwningPackage())));
                            } else {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public List<Bundle> getVisibleWindows(int ops) {
        ArrayMap<WindowState, Integer> windows = new ArrayMap<>();
        getVisibleWindows(windows, ops);
        List<Bundle> windowsList = new ArrayList<>();
        for (Map.Entry<WindowState, Integer> win : windows.entrySet()) {
            WindowState state = win.getKey();
            Bundle bundle = new Bundle();
            bundle.putInt("window_pid", state.mSession.mPid);
            bundle.putInt("window_value", win.getValue().intValue());
            bundle.putInt("window_state", System.identityHashCode(state));
            bundle.putInt("window_width", state.mRequestedWidth);
            bundle.putInt("window_height", state.mRequestedHeight);
            bundle.putFloat("window_alpha", state.mAttrs.alpha);
            bundle.putBoolean("window_hidden", state.mPermanentlyHidden);
            bundle.putString("window_package", state.getOwningPackage());
            bundle.putInt("window_uid", state.getOwningUid());
            windowsList.add(bundle);
        }
        return windowsList;
    }

    private void getVisibleWindows(ArrayMap<WindowState, Integer> windows, int ops) {
        if (windows != null) {
            if (ops == 45) {
                getToastWindows(windows);
            } else {
                getAlertWindows(windows);
            }
        }
    }

    private void getToastWindows(ArrayMap<WindowState, Integer> windows) {
        synchronized (this.mIWmsInner.getGlobalLock()) {
            for (WindowState win : this.mIWmsInner.getWindowMap().values()) {
                if (!(win == null || win.mAttrs == null || win.mSession == null)) {
                    if (!windows.containsKey(win)) {
                        if (win.mAttrs.type == 2005) {
                            windows.put(win, 3);
                        }
                    }
                }
            }
        }
    }

    private void updateVisibleWindows(int eventType, int mode, int type, WindowState win, int requestedWidth, int requestedHeight, boolean isUpdate) {
        if (requestedWidth != win.mRequestedWidth || requestedHeight != win.mRequestedHeight || !isUpdate) {
            Bundle args = new Bundle();
            args.putInt(ASSOC_WINDOW, win.mSession.mPid);
            args.putInt(ASSOC_WINDOW_MODE, mode);
            args.putInt(ASSOC_RELATION_TYPE, eventType);
            args.putInt(ASSOC_WINDOW_HASHCODE, System.identityHashCode(win));
            args.putInt(ASSOC_WINDOW_TYPE, type);
            args.putInt(ASSOC_WINDOW_WIDTH, eventType == 8 ? win.getAttrs().width : requestedWidth);
            args.putInt(ASSOC_WINDOW_HEIGHT, eventType == 8 ? win.getAttrs().height : requestedHeight);
            args.putFloat(ASSOC_WINDOW_ALPHA, win.getAttrs().alpha);
            args.putBoolean(ASSOC_WINDOW_PHIDE, win.mPermanentlyHidden);
            args.putBoolean(ASSOC_WINDOW_HAS_SURFACE, win.mHasSurface);
            args.putString(ASSOC_PKGNAME, win.getOwningPackage());
            args.putInt("uid", win.getOwningUid());
            this.mIWmsInner.getWMMonitor().reportData(RESOURCE_APPASSOC, System.currentTimeMillis(), args);
        }
    }

    private void updateVisibleWindowsOps(int eventType, String pkgName) {
        Bundle args = new Bundle();
        args.putString(ASSOC_PKGNAME, pkgName);
        args.putInt(ASSOC_RELATION_TYPE, eventType);
        this.mIWmsInner.getWMMonitor().reportData(RESOURCE_APPASSOC, System.currentTimeMillis(), args);
    }

    private void reportWindowStatusToIAware(int eventType, WindowState win, int mode, int requestedWidth, int requestedHeight, boolean isUpdate) {
        boolean isToast = win != null && win.getAttrs().type == 2005;
        if (win == null) {
            return;
        }
        if ((win.mAppOp != 24 && !isToast) || win.mSession == null) {
            return;
        }
        if (this.mIWmsInner.getWMMonitor().isResourceNeeded(RESOURCE_APPASSOC)) {
            updateVisibleWindows(eventType, mode, isToast ? 45 : 24, win, requestedWidth, requestedHeight, isUpdate);
        }
    }

    public void addWindowReport(WindowState win, int mode) {
        reportWindowStatusToIAware(8, win, mode, 0, 0, false);
    }

    public void removeWindowReport(WindowState win) {
        reportWindowStatusToIAware(9, win, 3, 0, 0, false);
    }

    public void updateWindowReport(WindowState win, int requestedWidth, int requestedHeight) {
        reportWindowStatusToIAware(27, win, 3, requestedWidth, requestedHeight, true);
    }

    public void setStartWindowTransitionReady(WindowState win) {
        if (win != null && HwPartIawareUtil.getConcurrentSwitch()) {
            CharSequence title = win.getWindowTag();
            DisplayContent dc = win.getDisplayContent();
            if (title != null && dc != null) {
                AppTransition transition = dc.mAppTransition;
                if (win.mAttrs.type == 3 && transition.isTransitionSet() && title.toString().startsWith(SPLASH_SCREEN) && dc.mOpeningApps.contains(win.mAppToken) && transition.getRemoteAnimationController() != null) {
                    transition.setReady();
                }
            }
        }
    }

    public void updateAppOpsStateReport(int ops, String packageName) {
        if (ops == 24 && this.mIWmsInner.getWMMonitor().isResourceNeeded(RESOURCE_APPASSOC)) {
            updateVisibleWindowsOps(10, packageName);
        }
    }

    public void notifyFingerWinCovered(boolean isCovered, Rect frame) {
        if (this.mIsLastCoveredState != isCovered || !this.mLastCoveredFrame.equals(frame)) {
            this.mHwHandler.sendMessage(this.mHwHandler.obtainMessage(101, isCovered ? 1 : 0, 0, frame));
            this.mIsLastCoveredState = isCovered;
            this.mLastCoveredFrame.set(frame);
        }
    }

    public int getFocusWindowWidth(WindowState currentFocus, WindowState inputMethodTarget) {
        if (HwPCUtils.isHiCarCastMode() && currentFocus != null && !currentFocus.isDefaultDisplay()) {
            return HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwHiCarMultiWindowManager().getInputMethodWidth();
        }
        WindowState focusWindow = inputMethodTarget == null ? currentFocus : inputMethodTarget;
        if (focusWindow == null) {
            Log.e(TAG, "WMS getFocusWindowWidth error");
            return 0;
        }
        Rect rect = focusWindow.getAttrs().type == 2 ? focusWindow.getDisplayFrameLw() : focusWindow.getContentFrameLw();
        if (focusWindow.inHwMagicWindowingMode()) {
            rect = focusWindow.getDecorFrame();
        }
        return rect.width();
    }

    public void handleNewDisplayConfiguration(Configuration overrideConfig, int displayId) {
        DisplayContent dc;
        WindowManagerPolicy wmPolicy = this.mIWmsInner.getPolicy();
        if (wmPolicy instanceof HwPhoneWindowManager) {
            HwPhoneWindowManager policy = (HwPhoneWindowManager) wmPolicy;
            if (policy.getHwWindowCallback() != null && (dc = this.mIWmsInner.getRoot().getDisplayContent(displayId)) != null && (dc.getConfiguration().diff(overrideConfig) & 128) != 0) {
                Slog.v(TAG, "handleNewDisplayConfiguration notify window callback");
                try {
                    policy.getHwWindowCallback().handleConfigurationChanged();
                } catch (Exception ex) {
                    Slog.w(TAG, "mIHwWindowCallback handleNewDisplayConfiguration", ex);
                }
            }
        }
    }

    public void getCurrFocusedWinInExtDisplay(Bundle outBundle) {
        if (outBundle != null) {
            synchronized (this.mIWmsInner.getGlobalLock()) {
                DisplayContent dc = this.mIWmsInner.getRoot().getDisplayContent(HwPCUtils.getPCDisplayID());
                if (dc != null) {
                    WindowState ws = dc.findFocusedWindow();
                    if (ws != null) {
                        outBundle.putString("pkgName", ws.getAttrs().packageName);
                        boolean isApp = ws.getAppToken() != null;
                        outBundle.putBoolean("isApp", isApp);
                        outBundle.putParcelable("bounds", isApp ? ws.getBounds() : null);
                    }
                }
            }
        }
    }

    public boolean hasLighterViewInPCCastMode() {
        synchronized (this.mIWmsInner.getGlobalLock()) {
            DisplayContent dc = this.mIWmsInner.getRoot().getDisplayContent(HwPCUtils.getPCDisplayID());
            if (dc == null) {
                return false;
            }
            if (!(dc instanceof DisplayContentBridge)) {
                return false;
            }
            return ((DisplayContentBridge) dc).hasLighterViewInPCCastMode();
        }
    }

    public boolean shouldDropMotionEventForTouchPad(float pointX, float pointY) {
        DisplayContent dc = this.mIWmsInner.getRoot().getDisplayContent(0);
        if (dc != null && (dc instanceof DisplayContentBridge)) {
            return ((DisplayContentBridge) dc).shouldDropMotionEventForTouchPad(pointX, pointY);
        }
        return false;
    }

    public void updateHwStartWindowRecord(int appUid) {
        HwStartWindowRecord.getInstance().resetStartWindowApp(Integer.valueOf(appUid));
    }

    public HwTaskSnapshotWrapper getForegroundTaskSnapshotWrapper(TaskSnapshotController taskSnapshotController, WindowState focusedWindow, boolean isRefresh) {
        ActivityManager.TaskSnapshot taskSnapshot;
        synchronized (this.mIWmsInner.getGlobalLock()) {
            if (isRefresh) {
                try {
                    taskSnapshotController.clearForegroundTaskSnapshot();
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (focusedWindow == null || focusedWindow.mAppToken == null || !isRefresh) {
                taskSnapshot = taskSnapshotController.getForegroundTaskSnapshot();
            } else {
                taskSnapshot = taskSnapshotController.createForegroundTaskSnapshot(focusedWindow.mAppToken);
            }
        }
        HwTaskSnapshotWrapper hwTaskSnapshotWrapper = new HwTaskSnapshotWrapper();
        hwTaskSnapshotWrapper.setTaskSnapshot(taskSnapshot);
        return hwTaskSnapshotWrapper;
    }

    public boolean detectSafeMode() {
        WindowManagerPolicy wmPolicy = this.mIWmsInner.getPolicy();
        boolean isCheckSafeModeState = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "isSafeModeDisabled", 0) == 1) {
            Slog.i(TAG, "safemode is disabled by dpm");
            this.mIsHwSafeMode = false;
            wmPolicy.setSafeMode(this.mIsHwSafeMode);
            isCheckSafeModeState = true;
        }
        if (!"1".equals(SystemProperties.get("sys.bootfail.safemode"))) {
            return isCheckSafeModeState;
        }
        Slog.i(TAG, "safemode is enabled eRecovery");
        this.mIsHwSafeMode = true;
        wmPolicy.setSafeMode(this.mIsHwSafeMode);
        return true;
    }

    public boolean getSafeMode() {
        return this.mIsHwSafeMode;
    }

    public void setLazyModeEx(int lazyMode, boolean isHintShowing, String windowName) {
        WindowState focusedWindow;
        Slog.i(TAG, "curSingleHand: " + this.mLazyModeOnEx + " toSingleHand: " + lazyMode + ", isHintShowing:" + isHintShowing + ", windowName:" + windowName);
        if (windowName == null) {
            Slog.e(TAG, "setLazyModeEx windowName is null");
            return;
        }
        if (windowName.contains("blurpaper")) {
            if (this.mIsHintShowing != isHintShowing) {
                this.mIsHintShowing = isHintShowing;
            } else {
                return;
            }
        } else if (!windowName.contains("virtual")) {
            Slog.e(TAG, "setLazyModeEx windowName:" + windowName + "mLazyModeOnEx" + this.mLazyModeOnEx + "mIsHintShowing" + this.mIsHintShowing);
            return;
        } else if (this.mLazyModeOnEx != lazyMode) {
            this.mLazyModeOnEx = lazyMode;
            if (HwDisplaySizeUtil.hasSideInScreen() && (focusedWindow = this.mIWmsInner.getService().getFocusedWindow()) != null) {
                this.mIWmsInner.getPolicy().notchControlFilletForSideScreen(focusedWindow, false);
            }
        } else {
            return;
        }
        WindowState focusedWindow2 = this.mHwInputManagerInternal;
        if (focusedWindow2 != null) {
            focusedWindow2.setInputScaleConfig(0.75f, 0.75f, getLazyModeInputScaleSide(this.mLazyModeOnEx, this.mIsHintShowing), 2);
        }
    }

    public int getLazyModeEx() {
        return this.mLazyModeOnEx;
    }

    public void performhwLayoutAndPlaceSurfacesLocked() {
        this.mIWmsInner.getWindowSurfacePlacer().performSurfacePlacement();
    }

    public Configuration getCurNaviConfiguration() {
        return this.mCurNaviConfiguration;
    }

    public Handler getGestureDetectorHandler() {
        return this.mGestureDetectorHandler;
    }

    public Handler getHwHandler() {
        return this.mHwHandler;
    }

    public boolean getIgnoreFrozen() {
        return this.mIsIgnoreFrozen;
    }

    public void setIgnoreFrozen(boolean isIgnoreFrozen) {
        this.mIsIgnoreFrozen = isIgnoreFrozen;
    }

    public void reevaluateStatusBarSize(boolean isLayoutNaviBar) {
        synchronized (this.mIWmsInner.getGlobalLock()) {
            this.mIsLayoutNaviBar = isLayoutNaviBar;
            this.mIWmsInner.getWindowMangerServiceHandler().post(this.mReevaluateStatusBarSize);
        }
    }

    public void setCurrentUser(int newUserId, int[] currentProfileIds) {
        this.mIWmsInner.getInputManager().setCurrentUser(newUserId, currentProfileIds);
        WindowManagerPolicy policy = this.mIWmsInner.getPolicy();
        if (policy instanceof HwPhoneWindowManager) {
            ((HwPhoneWindowManager) policy).setCurrentUser(newUserId, currentProfileIds);
        }
    }

    public void hwSystemReady() {
        if (IS_SUPPORT_SINGLE_MODE) {
            this.mIsSingleHandSwitch = judgeSingleHandSwitchBySize();
            Slog.i(TAG, "WMS systemReady mIsSingleHandSwitch = " + this.mIsSingleHandSwitch);
            if (this.mIsSingleHandSwitch || HwFoldScreenState.isFoldScreenDevice()) {
                HwServiceExFactory.getHwSingleHandAdapter(this.mContext, this.mHwHandler, this.mUiHandler, this.mIWmsInner.getService()).registorLocked();
                synchronized (this.mIWmsInner.getGlobalLock()) {
                    this.mIWmsInner.getDefaultDisplayContentLocked().mSingleHandContentEx = HwServiceExFactory.getHwSingleHandContentEx(this.mIWmsInner.getService());
                }
                resetQuickSwitchSingleHand();
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(QUICK_SWITCH_SINGLE_HAND_MODE), true, new ContentObserver(this.mHwHandler) {
                    /* class com.android.server.wm.HwWindowManagerServiceEx.AnonymousClass4 */

                    @Override // android.database.ContentObserver
                    public void onChange(boolean selfChange) {
                        HwWindowManagerServiceEx.this.quickSwitchSingleHandIfNeeded();
                    }
                });
            }
        }
        this.mHwInputManagerInternal = (InputManagerServiceEx.DefaultHwInputManagerLocalService) LocalServices.getService(InputManagerServiceEx.DefaultHwInputManagerLocalService.class);
        if (CoordinationModeUtils.isFoldable()) {
            Settings.Global.putInt(this.mContext.getContentResolver(), "coordination_create_mode", 0);
        }
        DefaultGestureNavManager gestureNavPolicy = (DefaultGestureNavManager) LocalServices.getService(DefaultGestureNavManager.class);
        if (gestureNavPolicy != null) {
            gestureNavPolicy.systemReady();
        }
    }

    private boolean judgeSingleHandSwitchBySize() {
        return this.mContext.getResources().getBoolean(34537473);
    }

    public boolean isSupportSingleHand() {
        if (HwFoldScreenState.isFoldScreenDevice()) {
            return true;
        }
        return this.mIsSingleHandSwitch;
    }

    public void setCoverManagerState(boolean isCoverOpen) {
        this.mIsCoverOpen = isCoverOpen;
        HwServiceFactory.setIfCoverClosed(!isCoverOpen);
    }

    public boolean isCoverOpen() {
        return this.mIsCoverOpen;
    }

    public void freezeOrThawRotation(int rotation) {
        if (rotation < -1 || rotation > 3) {
            throw new IllegalArgumentException("Rotation argument must be -1 or a valid rotation constant.");
        }
        Slog.v(TAG, "freezeRotationTemporarily rotation:" + rotation);
        this.mIWmsInner.getDefaultDisplayContentLocked().getDisplayRotation().freezeOrThawRotation(rotation);
    }

    public void preAddWindow(WindowManager.LayoutParams attrs) {
        if (attrs.type == 2101) {
            attrs.token = null;
        }
    }

    private boolean checkPermission() {
        int uid = UserHandle.getAppId(Binder.getCallingUid());
        if (uid == 1000) {
            return true;
        }
        Slog.e(TAG, "Process Permission error! uid:" + uid);
        return false;
    }

    public void notifySwingRotation(int rotation) {
        if (checkPermission()) {
            Slog.d(TAG, "notifySwingRotation rotation:" + rotation);
            DisplayContent displayContent = this.mIWmsInner.getService().getDefaultDisplayContentLocked();
            if (displayContent != null) {
                displayContent.getDisplayRotation().getHwDisplayRotationEx().setSwingRotation(rotation);
            } else {
                Slog.e(TAG, "notifySwingRotation rotation error, displayContent is null");
            }
        }
    }

    public Rect getSafeInsets(int type) {
        DisplayContent dc = this.mIWmsInner.getDefaultDisplayContentLocked();
        if (dc == null || !(dc instanceof DisplayContentBridge)) {
            return null;
        }
        return ((DisplayContentBridge) dc).getSafeInsetsByType(type);
    }

    public List<Rect> getBounds(int type) {
        DisplayContent dc = this.mIWmsInner.getDefaultDisplayContentLocked();
        if (dc == null || !(dc instanceof DisplayContentBridge)) {
            return null;
        }
        return ((DisplayContentBridge) dc).getBoundsByType(type);
    }

    public void setForcedDisplayDensityAndSize(int displayId, int density, int width, int height) {
        Throwable th;
        PackageManagerInternal packageManagerInternal;
        if (density >= 200 && width >= 400) {
            if (height >= 400) {
                Slog.d(TAG, "setForcedDisplayDensityAndSize size: " + width + "x" + height + " density: " + density);
                if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                    throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
                } else if (displayId == 0) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        synchronized (this.mIWmsInner.getGlobalLock()) {
                            try {
                                DisplayContent displayContent = this.mIWmsInner.getRoot().getDisplayContent(displayId);
                                if (displayContent != null) {
                                    int width2 = Math.min(width, displayContent.mInitialDisplayWidth * 2);
                                    try {
                                        int height2 = Math.min(height, displayContent.mInitialDisplayHeight * 2);
                                        try {
                                            displayContent.mBaseDisplayWidth = width2;
                                            displayContent.mBaseDisplayHeight = height2;
                                            displayContent.mBaseDisplayDensity = density;
                                            this.mHwHandler.removeMessages(100);
                                            this.mHwHandler.sendEmptyMessageDelayed(100, MSG_ROG_FREEZE_TIME_DELEAYED);
                                            updateResourceConfiguration(displayId, density, width2, height2);
                                            this.mIWmsInner.getService().reconfigureDisplayLocked(displayContent);
                                            ScreenRotationAnimation screenRotationAnimation = this.mIWmsInner.getWindowAnimator().getScreenRotationAnimationLocked(displayId);
                                            if (screenRotationAnimation != null) {
                                                screenRotationAnimation.kill();
                                            }
                                            ContentResolver contentResolver = this.mContext.getContentResolver();
                                            Settings.Global.putString(contentResolver, "display_size_forced", width2 + "," + height2);
                                            List<UserInfo> userList = UserManager.get(this.mContext).getUsers();
                                            if (userList != null) {
                                                for (Iterator<UserInfo> it = userList.iterator(); it.hasNext(); it = it) {
                                                    Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "display_density_forced", Integer.toString(density), it.next().id);
                                                    userList = userList;
                                                }
                                            }
                                            SystemProperties.set("persist.sys.realdpi", density + "");
                                            SystemProperties.set("persist.sys.rog.width", width2 + "");
                                            SystemProperties.set("persist.sys.rog.height", height2 + "");
                                            ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).adjustDisplayMetricsInRog();
                                            if (IS_NOTCH_PROP) {
                                                this.mIWmsInner.getDisplayManagerInternal().updateCutoutInfoForRog(0);
                                                Slog.d(TAG, "updateCutoutInfoForRog width: " + width2 + " height " + height2);
                                            }
                                            if (displayContent.mSideSurfaceBox != null) {
                                                displayContent.mSideSurfaceBox.updateSideBox();
                                            }
                                            Display display = displayContent.getDisplay();
                                            Point maxDisplaySize = new Point();
                                            display.getRealSize(maxDisplaySize);
                                            HwFreeFormUtils.computeFreeFormSize(maxDisplaySize);
                                        } catch (Throwable th2) {
                                            packageManagerInternal = th2;
                                        }
                                    } catch (Throwable th3) {
                                        packageManagerInternal = th3;
                                    }
                                }
                                Binder.restoreCallingIdentity(ident);
                                return;
                            } catch (Throwable th4) {
                                packageManagerInternal = th4;
                            }
                        }
                        try {
                            throw packageManagerInternal;
                        } catch (Throwable th5) {
                            th = th5;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        Binder.restoreCallingIdentity(ident);
                        throw th;
                    }
                } else {
                    throw new IllegalArgumentException("Can only set the default display");
                }
            }
        }
        Slog.d(TAG, "the para of setForcedDisplayDensityAndSize is illegal : size = " + width + "x" + height + "; density = " + density);
    }

    public void updateResourceConfiguration(int displayId, int density, int width, int height) {
        if (density == 0) {
            Slog.e(TAG, "setForcedDisplayDensityAndSize density is 0");
            return;
        }
        Slog.d(TAG, "setForcedDisplay and updateResourceConfiguration, density = " + density + " width = " + width + " height = " + height);
        DisplayContent dc = this.mIWmsInner.getRoot().getDisplayContent(displayId);
        if (dc != null) {
            Configuration tempResourceConfiguration = new Configuration(dc.getConfiguration());
            tempResourceConfiguration.densityDpi = density;
            tempResourceConfiguration.screenWidthDp = (width * 160) / density;
            tempResourceConfiguration.smallestScreenWidthDp = (width * 160) / density;
            DisplayMetrics tempMetrics = this.mContext.getResources().getDisplayMetrics();
            tempMetrics.density = ((float) density) / 160.0f;
            tempMetrics.densityDpi = density;
            this.mContext.getResources().updateConfiguration(tempResourceConfiguration, tempMetrics);
            Slog.d(TAG, "setForcedDisplay and updateResourceConfiguration, tempResourceConfiguration is: " + tempResourceConfiguration + "tempMetrics is: " + tempMetrics);
        }
    }

    public void setAppWindowExitInfo(Bundle bundle, Bitmap iconBitmap, int callingUid) {
        setIntelligentWindowExitInfo(bundle, callingUid);
        if (!HwWmConstants.IS_HW_SUPPORT_LAUNCHER_EXIT_ANIM && !HwWmConstants.IS_SUPPORT_FLOAT_TO_WINDOW_ANIM) {
            return;
        }
        if (bundle == null) {
            Slog.i(TAG, "empty bundle!");
            this.mExitPivotX = -1.0f;
            this.mExitPivotY = -1.0f;
            this.mExitIconWidth = -1;
            this.mExitIconHeight = -1;
            this.mExitIconBitmap = null;
            this.mExitFlag = 0;
            this.mIsLanucherLandscape = false;
        } else if (isUidCanSetIcon(callingUid)) {
            Slog.i(TAG, "set app win exit info, bundle = " + bundle + ", iconBitmap = " + iconBitmap);
            if (!HwWmConstants.LAUNCHER_TYPE_VALUE_OPEN.equals(bundle.getString("type")) || iconBitmap == null) {
                this.mExitIconWidth = iconBitmap != null ? iconBitmap.getWidth() : bundle.getInt(HwWmConstants.ICON_WIDTH_STR);
                this.mExitIconHeight = iconBitmap != null ? iconBitmap.getHeight() : bundle.getInt(HwWmConstants.ICON_HEIGHT_STR);
                this.mExitPivotX = bundle.getFloat(HwWmConstants.PIVOTX_STR);
                this.mExitPivotY = bundle.getFloat(HwWmConstants.PIVOTY_STR);
                this.mExitIconBitmap = iconBitmap;
                this.mExitFlag = bundle.getInt(HwWmConstants.FLAG_STR);
                this.mIsLanucherLandscape = bundle.getBoolean(HwWmConstants.IS_LANDSCAPE_STR);
                pushFloatIconInfoToMap(bundle);
                Slog.d(TAG, "set app win icon info, bundle = " + bundle + ", iconBitmap = " + iconBitmap + "mExitPivotX =" + this.mExitPivotX + "mExitPivotY =" + this.mExitPivotY + "mExitIconWidth =" + this.mExitIconWidth + "mExitIconHeight =" + this.mExitIconHeight + "mExitFlag =" + this.mExitFlag + "callingUid" + callingUid);
                return;
            }
            setLandAnimationInfo(true, bundle.getString(HwWmConstants.LAUNCHER_TARGET_PACKAGE_NAME));
            this.mEnterIconWidth = iconBitmap.getWidth();
            this.mEnterIconHeight = iconBitmap.getHeight();
            this.mEnterPivotX = bundle.getFloat(HwWmConstants.PIVOTX_STR);
            this.mEnterPivotY = bundle.getFloat(HwWmConstants.PIVOTY_STR);
            int iconHyalineWidth = getIconHyalineWidth(iconBitmap);
            this.mEnterIconHeight = (this.mEnterIconHeight - iconHyalineWidth) - iconHyalineWidth;
            this.mEnterIconWidth = (this.mEnterIconWidth - iconHyalineWidth) - iconHyalineWidth;
            this.mEnterIconBitmap = Bitmap.createBitmap(iconBitmap, iconHyalineWidth, iconHyalineWidth, this.mEnterIconWidth, this.mEnterIconHeight);
        }
    }

    private void pushFloatIconInfoToMap(Bundle bundle) {
        int i = this.mExitFlag;
        if (i == 3 || i == 2) {
            int taskId = bundle.getInt(HwWmConstants.TASK_ID);
            HwFreeWindowFloatIconInfo hwFreeWindowFloatIconInfo = new HwFreeWindowFloatIconInfo();
            hwFreeWindowFloatIconInfo.setFloatPivotWidth(this.mExitIconWidth);
            hwFreeWindowFloatIconInfo.setFloatPivotHeight(this.mExitIconHeight);
            hwFreeWindowFloatIconInfo.setFloatPivotX(Float.valueOf(this.mExitPivotX));
            hwFreeWindowFloatIconInfo.setFloatPivotY(Float.valueOf(this.mExitPivotY));
            hwFreeWindowFloatIconInfo.setSceneTag(this.mExitFlag);
            hwFreeWindowFloatIconInfo.setTaskId(taskId);
            Bitmap bitmap = this.mExitIconBitmap;
            if (bitmap != null) {
                hwFreeWindowFloatIconInfo.setIconBitmap(Bitmap.createBitmap(bitmap));
            }
            this.mHwFreeWindowFloatIconInfos.put(Integer.valueOf(taskId), hwFreeWindowFloatIconInfo);
        }
    }

    private boolean isUidCanSetIcon(int callingUid) {
        if (HwWmConstants.IS_HW_SUPPORT_LAUNCHER_EXIT_ANIM && this.mIWmsInner.getService().mAtmService.mRecentTasks.isCallerRecents(callingUid)) {
            return true;
        }
        if (HwWmConstants.IS_SUPPORT_FLOAT_TO_WINDOW_ANIM) {
            boolean isSystemUid = callingUid == 1000;
            String uidName = this.mContext.getPackageManager().getNameForUid(callingUid);
            if (isSystemUid || (uidName != null && uidName.contains(HwWmConstants.PACKAGE_NAME_SYSTEMUI))) {
                return true;
            }
        }
        Slog.w(TAG, "callingUid is not qualified!");
        return false;
    }

    public void setForcedDisplaySizeAndDensity(boolean isFold, int displayId, int width, int height, int density) {
        if (!isFold) {
            Slog.d(TAG, "setForcedDisplayDensityAndSize only fold can use this method. ");
            return;
        }
        synchronized (this.mIWmsInner.getGlobalLock()) {
            DisplayContent displayContent = this.mIWmsInner.getService().mRoot.getDisplayContent(displayId);
            int foldDisplayMode = this.mIWmsInner.getService().mDisplayManagerInternal.getDisplayMode();
            boolean isDisplayModeChanged = false;
            if (foldDisplayMode != this.mCurrentFoldDisplayMode) {
                isDisplayModeChanged = true;
                this.mIWmsInner.getService().mAtmService.mHwATMSEx.notifyDisplayModeChange(foldDisplayMode, this.mCurrentFoldDisplayMode);
                this.mCurrentFoldDisplayMode = foldDisplayMode;
                if (displayContent != null) {
                    displayContent.getDisplayRotation().setDisplayModeChange(true);
                }
                this.mIWmsInner.getService().mTaskSnapshotController.clearSnapshot();
                if (this.mIWmsInner.getService().getLazyMode() != 0 && (this.mCurrentFoldDisplayMode == 1 || this.mCurrentFoldDisplayMode == 3 || this.mCurrentFoldDisplayMode == 4)) {
                    Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
                }
                if (this.mIWmsInner.getService().mIsFoldDisableEventDispatching && this.mCurrentFoldDisplayMode != 4) {
                    this.mIWmsInner.getService().mH.removeMessages((int) HwAPPQoEUtils.MSG_APP_STATE_UNKNOW);
                    this.mIWmsInner.getService().mInputManagerCallback.setEventDispatchingLw(true);
                    this.mIWmsInner.getService().mIsFoldDisableEventDispatching = false;
                }
                if (density != 0) {
                    displayContent.mBaseDisplayDensity = density;
                }
            }
            this.mIWmsInner.getService().setForcedDisplaySize(displayId, width, height);
            if (isDisplayModeChanged) {
                setWallpaperLayoutNeedLw();
            }
        }
    }

    private void setWallpaperLayoutNeedLw() {
        this.mIWmsInner.getService().mRoot.forAllWindows(new ToBooleanFunction() {
            /* class com.android.server.wm.$$Lambda$HwWindowManagerServiceEx$bckR5MSbDl3ravebneKXoFfpd2U */

            public final boolean apply(Object obj) {
                return HwWindowManagerServiceEx.this.lambda$setWallpaperLayoutNeedLw$1$HwWindowManagerServiceEx((WindowState) obj);
            }
        }, false);
    }

    public /* synthetic */ boolean lambda$setWallpaperLayoutNeedLw$1$HwWindowManagerServiceEx(WindowState w) {
        if (w == null || !w.mIsWallpaper || w.mLayoutNeeded || this.mIWmsInner.getService().mCurrentUserId != UserHandle.getUserId(w.mOwnerUid)) {
            return false;
        }
        w.mLayoutNeeded = true;
        w.mIsFoldChange = true;
        this.mIWmsInner.getService().mH.post(new Runnable(w) {
            /* class com.android.server.wm.$$Lambda$HwWindowManagerServiceEx$DqPzlAs_a46UqofKzANHFTOoAA */
            private final /* synthetic */ WindowState f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwWindowManagerServiceEx.this.lambda$setWallpaperLayoutNeedLw$0$HwWindowManagerServiceEx(this.f$1);
            }
        });
        return true;
    }

    public /* synthetic */ void lambda$setWallpaperLayoutNeedLw$0$HwWindowManagerServiceEx(WindowState w) {
        synchronized (this.mIWmsInner.getService().getGlobalLock()) {
            if (w != null) {
                w.reportResized();
            }
        }
    }

    public int getFoldDisplayMode() {
        return this.mCurrentFoldDisplayMode;
    }

    public void resetAppWindowExitInfo(int transit, AppWindowToken topOpeningApp) {
        if (HwWmConstants.IS_HW_SUPPORT_LAUNCHER_EXIT_ANIM && transit == 13 && topOpeningApp != null && HwWmConstants.containsLauncherCmpName(topOpeningApp.toString())) {
            this.mExitPivotX = -1.0f;
            this.mExitPivotY = -1.0f;
            this.mExitIconBitmap = null;
            this.mExitIconWidth = -1;
            this.mExitIconHeight = -1;
            this.mExitFlag = -1;
            this.mIsLanucherLandscape = false;
            Slog.i(TAG, "exit info has been reset.");
        }
        if (HwWmConstants.IS_SUPPORT_FLOAT_TO_WINDOW_ANIM) {
            int i = this.mExitFlag;
            if (i == 3 || i == 2 || this.mHwFreeWindowFloatIconInfos.size() > 0) {
                this.mHwFreeWindowFloatIconInfos.remove(Integer.valueOf(this.mLastFloatIconlayerTaskId));
                this.mExitPivotX = -1.0f;
                this.mExitPivotY = -1.0f;
                this.mExitIconBitmap = null;
                this.mExitIconWidth = -1;
                this.mExitIconHeight = -1;
                this.mExitFlag = -1;
                this.mIsLanucherLandscape = false;
                this.mLastTokenLayer = -1;
                this.mLastIconLayerWindowToken = null;
                Slog.i(TAG, "freewindow icon info has been reset.");
            }
        }
    }

    public void clearAppWindowIconInfo(WindowState win, int viewVisibility) {
        if (win != null && HwWmConstants.IS_HW_SUPPORT_LAUNCHER_EXIT_ANIM && win.mWinAnimator != null && win.mAnimatingExit && viewVisibility == 0) {
            win.mWinAnimator.setWindowIconInfo(0, 0, 0, (Bitmap) null);
            Slog.i(TAG, "Relayout clear set window icon info flag");
        }
    }

    public boolean isRightInMagicWindow(WindowState ws) {
        if (HwMwUtils.ENABLED && ws != null && ws.inHwMagicWindowingMode()) {
            Bundle bundle = HwMwUtils.performPolicy(104, new Object[]{ws});
            boolean isRight = bundle.getBoolean("BUNDLE_ISRIGHT_INMW", false);
            boolean isLeft = bundle.getBoolean("RESULT_ISLEFT_INMW", false);
            DisplayInfo displayInfo = ws.getDisplayInfo();
            if (displayInfo != null) {
                int width = displayInfo.logicalWidth;
                if ((this.mExitPivotX < ((float) (width / 2)) || !isLeft) && (!isRight || this.mExitPivotX >= ((float) (width / 2)))) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public AppWindowToken findExitToLauncherMaxWindowSizeToken(ArraySet<AppWindowToken> closingApps, int appsCount, int transit) {
        WindowState win;
        int maxSize = -1;
        AppWindowToken maxSizeToken = null;
        Slog.i(TAG, "closing apps count = " + appsCount);
        if (HwWmConstants.IS_HW_SUPPORT_LAUNCHER_EXIT_ANIM && transit == 13) {
            for (int i = 0; i < appsCount; i++) {
                AppWindowToken wtoken = closingApps.valueAt(i);
                if (!(wtoken == null || (win = wtoken.findMainWindow(false)) == null || win.getFrameLw() == null)) {
                    int height = win.getFrameLw().height();
                    int width = win.getFrameLw().width();
                    int size = height * width;
                    if (!this.mIWmsInner.getService().mHwWMSEx.isRightInMagicWindow(win) && !wtoken.toString().contains(HwWmConstants.PERMISSION_DIALOG_CMP) && (size > maxSize || (size == maxSize && !wtoken.isHidden()))) {
                        maxSize = height * width;
                        maxSizeToken = wtoken;
                    }
                }
            }
        }
        return maxSizeToken;
    }

    private boolean isClosingAppsContains(AppWindowToken atoken, DisplayContent dc) {
        return (dc.mClosingApps != null) && dc.mClosingApps.contains(atoken);
    }

    private boolean isOpeningAppsContains(AppWindowToken atoken, DisplayContent dc) {
        return (dc.mOpeningApps != null) && dc.mOpeningApps.contains(atoken);
    }

    private boolean isAppLauncher(AppWindowToken atoken) {
        return (atoken != null) && HwWmConstants.containsLauncherCmpName(atoken.toString());
    }

    public boolean isLastOneApp(DisplayContent dc) {
        boolean isLastOneApp = false;
        if (dc == null) {
            Slog.w(TAG, "find no display content when try to check freeform exit by back");
            return false;
        }
        AppWindowToken topOpeningApp = getTopApp(dc.mOpeningApps, false);
        Slog.d(TAG, "is app exit to launcher info: dc , mClosingApps = " + dc.mClosingApps + ", topOpeningApp = " + topOpeningApp + ", mExitIconBitmap = " + this.mExitIconBitmap + ", mExitIconHeight = " + this.mExitIconHeight + ", mExitIconWidth = " + this.mExitIconWidth);
        if (topOpeningApp == null) {
            isLastOneApp = true;
        }
        return isLastOneApp;
    }

    private boolean isAppExitToLauncher(AppWindowToken atoken, int transit, DisplayContent dc) {
        boolean isAppExitToLauncher = false;
        WindowState window = atoken.findMainWindow(IS_SUPER_LITE_ANIMA_STYLE);
        AppWindowToken topOpeningApp = getTopApp(dc.mOpeningApps, false);
        Slog.d(TAG, "is app exit to launcher info: transit = " + transit + ", app = " + atoken + ", window = " + window + ", mClosingApps = " + dc.mClosingApps + ", topOpeningApp = " + topOpeningApp + ", mExitIconBitmap = " + this.mExitIconBitmap + ", mExitIconHeight = " + this.mExitIconHeight + ", mExitIconWidth = " + this.mExitIconWidth);
        boolean isExitBitmapNotNull = true;
        boolean isWindowNotNull = window != null;
        boolean isTransitWallpaperOpen = transit == 13;
        boolean isTokenNotStkDialog = !atoken.toString().contains(HwWmConstants.STK_DIALOG_CMP);
        if (this.mExitIconBitmap == null) {
            isExitBitmapNotNull = false;
        }
        if (isWindowNotNull && isTransitWallpaperOpen && isClosingAppsContains(atoken, dc) && isAppLauncher(topOpeningApp) && isTokenNotStkDialog && isExitBitmapNotNull) {
            isAppExitToLauncher = true;
            if (window.mAttrs != null && (window.mAttrs.flags & 524288) == 524288 && (window.mAttrs.flags & HwGlobalActionsData.FLAG_KEYCOMBINATION_INIT_STATE) == 4194304) {
                Slog.d(TAG, "app to launcher window flag = " + window.mAttrs.flags);
                isAppExitToLauncher = false;
            }
            if (isFullScreenFreeformScene(atoken)) {
                isAppExitToLauncher = false;
            }
        }
        if (!isAppExitToLauncher && isWindowNotNull && window.mWinAnimator != null) {
            window.mWinAnimator.setWindowIconInfo(0, 0, 0, (Bitmap) null);
        }
        return isAppExitToLauncher;
    }

    private boolean isLauncherOpen(AppWindowToken atoken, int transit, DisplayContent dc) {
        if (!(transit == 13) || !HwWmConstants.containsLauncherCmpName(atoken.toString()) || dc.mClosingApps == null || !isOpeningAppsContains(atoken, dc)) {
            return false;
        }
        Slog.i(TAG, dc.mClosingApps + " is closing and " + dc.mOpeningApps + "is opening");
        return true;
    }

    private static boolean isValidWindow(WindowState window) {
        if (window == null) {
            Slog.w(TAG, "find no app main window!");
            return false;
        } else if (window.getDisplayFrameLw() == null) {
            Slog.w(TAG, "find no winDisplayFrame!");
            return false;
        } else if (window.getBounds() == null) {
            Slog.w(TAG, "find no app window frame!");
            return false;
        } else if (window.getStack() != null) {
            return true;
        } else {
            Slog.w(TAG, "find no app window stack!");
            return false;
        }
    }

    public WindowAnimationSpec setDynamicCornerRadiusInfo(AppWindowToken atoken, int transit, WindowAnimationSpec windowAnimationSpec) {
        if (atoken == null) {
            Slog.w(TAG, "find no app window token");
            return windowAnimationSpec;
        }
        HwFreeWindowFloatIconInfo iconInfo = getFloatIconInfo(atoken);
        if (isHwFreeWindowFloatDrawBackScene(atoken, transit, atoken.getDisplayContent())) {
            HwAppTransitionImpl.setFloatSceneCornerRadiusInfo(atoken, windowAnimationSpec, iconInfo.getFloatPivotX().floatValue(), iconInfo.getFloatPivotY().floatValue(), true);
        } else if (!isHwFreeWindowFloatOpenScene(atoken, transit, atoken.getDisplayContent())) {
            return windowAnimationSpec;
        } else {
            HwAppTransitionImpl.setFloatSceneCornerRadiusInfo(atoken, windowAnimationSpec, iconInfo.getFloatPivotX().floatValue(), iconInfo.getFloatPivotY().floatValue(), false);
        }
        return windowAnimationSpec;
    }

    public boolean isHwFreeWindowFloatDrawBackScene(AppWindowToken atoken, int transit, DisplayContent dc) {
        if (!HwWmConstants.IS_SUPPORT_FLOAT_TO_WINDOW_ANIM) {
            return false;
        }
        if (atoken == null || atoken.getTask() == null) {
            Slog.w(TAG, "find no atoken or task");
            return false;
        } else if (dc == null) {
            Slog.w(TAG, "find no display content");
            return false;
        } else {
            HwFreeWindowFloatIconInfo iconInfo = getFloatIconInfo(atoken);
            if (!(iconInfo.getIconBitmap() != null && iconInfo.getSceneTag() == 3)) {
                return false;
            }
            boolean isTransitHwFreeFormWidowBack = atoken.inHwFreeFormWindowingMode() || atoken.getWindowingMode() == 1;
            boolean isContainsClosingApps = isClosingAppsContains(atoken, dc);
            if (!isTransitHwFreeFormWidowBack || !isContainsClosingApps) {
                Slog.w(TAG, "not hw freewindow scene");
                return false;
            }
            IHwWindowManagerInner iHwWindowManagerInner = this.mIWmsInner;
            if (iHwWindowManagerInner == null || iHwWindowManagerInner.getService() == null) {
                return false;
            }
            if (this.mIWmsInner.getService().getLazyMode() != 0) {
                Slog.w(TAG, "lazy mode not support float draw back animation");
                return false;
            }
            boolean isSplitMode = false;
            if (atoken.inHwMagicWindowingMode() && atoken.getStack() != null) {
                isSplitMode = HwMwUtils.performPolicy(132, new Object[]{Integer.valueOf(atoken.getStack().mStackId), true}).getBoolean("RESULT_IN_APP_SPLIT", false);
            }
            if (!isSplitMode) {
                return true;
            }
            Slog.w(TAG, "split mode not support float draw back animation");
            return false;
        }
    }

    public boolean isHwFreeWindowFloatOpenScene(AppWindowToken atoken, int transit, DisplayContent dc) {
        if (!HwWmConstants.IS_SUPPORT_FLOAT_TO_WINDOW_ANIM) {
            return false;
        }
        if (atoken == null || atoken.getTask() == null) {
            Slog.w(TAG, "find no atoken or task");
            return false;
        } else if (dc == null) {
            Slog.w(TAG, "find no display content");
            return false;
        } else {
            HwFreeWindowFloatIconInfo iconInfo = getFloatIconInfo(atoken);
            if (!(iconInfo.getIconBitmap() != null && iconInfo.getSceneTag() == 2)) {
                return false;
            }
            boolean isTransitHwFreeFormWidowFront = atoken.inHwFreeFormWindowingMode() || atoken.getWindowingMode() == 1;
            boolean isContainsOpenigApps = isOpeningAppsContains(atoken, dc);
            if (!isTransitHwFreeFormWidowFront || !isContainsOpenigApps) {
                Slog.w(TAG, "not hw freewindow scene");
                return false;
            }
            IHwWindowManagerInner iHwWindowManagerInner = this.mIWmsInner;
            if (iHwWindowManagerInner == null || iHwWindowManagerInner.getService() == null) {
                return false;
            }
            if (this.mIWmsInner.getService().getLazyMode() != 0) {
                Slog.w(TAG, "lazy mode not support float open animation");
                return false;
            }
            boolean isSplitMode = false;
            if (atoken.inHwMagicWindowingMode() && atoken.getStack() != null) {
                isSplitMode = HwMwUtils.performPolicy(132, new Object[]{Integer.valueOf(atoken.getStack().mStackId), true}).getBoolean("RESULT_IN_APP_SPLIT", false);
            }
            if (!isSplitMode) {
                return true;
            }
            Slog.w(TAG, "split mode not support float open animation");
            return false;
        }
    }

    private boolean isFullScreenFreeformScene(AppWindowToken atoken) {
        if (atoken == null || atoken.getTask() == null) {
            return false;
        }
        int taskId = atoken.getTask().mTaskId;
        if (atoken.getWindowingMode() != 1 || !this.mHwFreeWindowFloatIconInfos.containsKey(Integer.valueOf(taskId))) {
            return false;
        }
        return true;
    }

    private boolean isFullScreenFreeformWindowBackground(AppWindowToken atoken) {
        DisplayContent displayContent;
        if (atoken == null || isFullScreenFreeformScene(atoken) || (displayContent = atoken.getDisplayContent()) == null) {
            return false;
        }
        Iterator it = displayContent.mOpeningApps.iterator();
        while (it.hasNext()) {
            if (isFullScreenFreeformScene((AppWindowToken) it.next())) {
                return true;
            }
        }
        Iterator it2 = displayContent.mClosingApps.iterator();
        while (it2.hasNext()) {
            if (isFullScreenFreeformScene((AppWindowToken) it2.next())) {
                return true;
            }
        }
        return false;
    }

    public Animation loadHwAssociateFullScreenBackgroundAnimation(Animation animation, int transit, AppWindowToken atoken) {
        if (atoken == null || !isFullScreenFreeformWindowBackground(atoken)) {
            return animation;
        }
        int windowingMode = atoken.getWindowingMode();
        if (WindowConfiguration.isHwSplitScreenWindowingMode(windowingMode) || windowingMode == 1 || WindowConfiguration.isHwMagicWindowingMode(windowingMode)) {
            return HwAppTransitionImpl.createFullScreenBackgroundAnimation(false, isOpeningAppsContains(atoken, atoken.getDisplayContent()));
        }
        if (!isAppLauncher(atoken) || transit != 12) {
            return animation;
        }
        return HwAppTransitionImpl.createFullScreenBackgroundAnimation(true, false);
    }

    public Animation loadHwFreeWindowFloatDrawBackAnimation(Animation animation, int transit, AppWindowToken atoken) {
        if (!HwWmConstants.IS_SUPPORT_FLOAT_TO_WINDOW_ANIM) {
            return animation;
        }
        if (atoken == null) {
            Slog.w(TAG, "find no atoken when try to override float draw back animation");
            return animation;
        }
        DisplayContent dc = atoken.getDisplayContent();
        if (dc == null) {
            Slog.w(TAG, "find no display content when try to override float draw back animation");
            return animation;
        } else if (!isHwFreeWindowFloatDrawBackScene(atoken, transit, dc)) {
            return animation;
        } else {
            WindowState window = atoken.findMainWindow(false);
            if (!isValidWindow(window)) {
                return animation;
            }
            if ((window.mAttrs.flags & 524288) == 524288 && (window.mAttrs.flags & HwGlobalActionsData.FLAG_KEYCOMBINATION_INIT_STATE) == 4194304) {
                return animation;
            }
            float iconRadius = HwAppTransitionImpl.getFloatBallOriginalRadius();
            HwFreeWindowFloatIconInfo iconInfo = getFloatIconInfo(atoken);
            Animation floatDrawBackSceneAnimation = HwAppTransitionImpl.createFloatDrawBackAnimation(window, iconInfo.getFloatPivotX().floatValue(), iconInfo.getFloatPivotY().floatValue(), iconRadius);
            if (floatDrawBackSceneAnimation == null) {
                return animation;
            }
            setUpHwFreeWindowFloatIconAnimation(atoken, dc, window, iconRadius, iconInfo);
            addListenToHwFreeWindowFloatAnim(floatDrawBackSceneAnimation, atoken);
            return floatDrawBackSceneAnimation;
        }
    }

    private HwFreeWindowFloatIconInfo getFloatIconInfo(AppWindowToken atoken) {
        HwFreeWindowFloatIconInfo iconInfo = this.mHwFreeWindowFloatIconInfos.get(Integer.valueOf(atoken.getTask().mTaskId));
        if (iconInfo != null) {
            return iconInfo;
        }
        HwFreeWindowFloatIconInfo iconInfo2 = new HwFreeWindowFloatIconInfo();
        iconInfo2.setFloatPivotWidth(this.mExitIconWidth);
        iconInfo2.setFloatPivotHeight(this.mExitIconHeight);
        iconInfo2.setFloatPivotX(Float.valueOf(this.mExitPivotX));
        iconInfo2.setFloatPivotY(Float.valueOf(this.mExitPivotY));
        iconInfo2.setSceneTag(this.mExitFlag);
        iconInfo2.setTaskId(atoken.getTask().mTaskId);
        iconInfo2.setIconBitmap(this.mExitIconBitmap);
        return iconInfo2;
    }

    private void setUpHwFreeWindowFloatIconAnimation(AppWindowToken atoken, DisplayContent dc, WindowState window, float iconRadius, HwFreeWindowFloatIconInfo iconInfo) {
        int i = this.mLastFloatIconlayerTaskId;
        if (i == -1 || i == iconInfo.getTaskId()) {
            int i2 = this.mLastFloatIconlayerTaskId;
            if (i2 == -1 || i2 != iconInfo.getTaskId() || this.mLastTokenLayer < atoken.getLayer()) {
                clearHwFreeWindowFloatIconLayer(this.mLastIconLayerWindowToken);
                this.mLastFloatIconlayerTaskId = iconInfo.getTaskId();
                this.mLastTokenLayer = atoken.getLayer();
                this.mLastIconLayerWindowToken = atoken;
                SurfaceControl.Transaction transaction = this.mTransactionFactory.make();
                createHwFreeWindowFloatIconSurfaceControl(atoken, dc, window, transaction, iconInfo);
                adjustHwFreeWindowFloatIconLayer(atoken, transaction);
                if (atoken.mFloatWindwoIconSurfaceControl != null) {
                    int width = iconInfo.getIconBitmap().getWidth();
                    int height = iconInfo.getIconBitmap().getHeight();
                    int byteCount = iconInfo.getIconBitmap().getRowBytes() * iconInfo.getIconBitmap().getHeight();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(byteCount);
                    iconInfo.getIconBitmap().copyPixelsToBuffer(byteBuffer);
                    byte[] byteBufferArray = byteBuffer.array();
                    ColorSpace colorSpace = iconInfo.getIconBitmap().getColorSpace();
                    if (colorSpace == null) {
                        colorSpace = ColorSpace.get(ColorSpace.Named.SRGB);
                    }
                    atoken.mFloatWindwoIconSurfaceControl.setWindowIconInfo(iconInfo.getSceneTag(), (int) (iconRadius * 2.0f), (int) (2.0f * iconRadius), byteBufferArray, byteCount, width, height, colorSpace.getId());
                    return;
                }
                return;
            }
            Slog.i(TAG, "setUpHwFreeWindowFloatIconAnimation: icon layer has created for the same task appWindowToken");
            return;
        }
        Slog.i(TAG, "setUpHwFreeWindowFloatIconAnimation: no need to create icon layer for other task appWindowToken");
    }

    private Rect getRealTaskBounds(WindowState window, boolean isMiniMizeScene) {
        Rect taskBounds = new Rect();
        if (window == null || window.getTask() == null) {
            return taskBounds;
        }
        taskBounds.set(window.getTask().getBounds());
        int left = taskBounds.left;
        int top = taskBounds.top;
        if (isMiniMizeScene) {
            taskBounds.bottom = (taskBounds.bottom + window.getContainingFrame().height()) - window.getBounds().height();
        }
        taskBounds.scale(window.getStack().mHwStackScale);
        taskBounds.offsetTo(left, top);
        return taskBounds;
    }

    private void createHwFreeWindowFloatIconSurfaceControl(AppWindowToken atoken, DisplayContent displayContent, WindowState window, SurfaceControl.Transaction transaction, HwFreeWindowFloatIconInfo iconInfo) {
        if (atoken.mFloatWindwoIconSurfaceControl == null) {
            Rect taskBounds = getRealTaskBounds(window, iconInfo != null && iconInfo.getSceneTag() == 3);
            atoken.mFloatWindwoIconSurfaceControl = displayContent.makeOverlay().setName("HwFreeWindowFloatIconSurfaceControl").setBufferSize(taskBounds.width(), taskBounds.height()).build();
            Surface surface = this.mSurfaceFactory.make();
            surface.copyFrom(atoken.mFloatWindwoIconSurfaceControl);
            try {
                Canvas canvas = surface.lockCanvas(null);
                if (canvas != null) {
                    canvas.drawColor(-16777216);
                    surface.unlockCanvasAndPost(canvas);
                    Rect windowBounds = new Rect();
                    windowBounds.set(window.getBounds());
                    int windowLeft = windowBounds.left;
                    int windowTop = windowBounds.top;
                    windowBounds.scale(window.getStack().mHwStackScale);
                    windowBounds.offsetTo(windowLeft, windowTop);
                    float positionX = (float) (windowBounds.left - taskBounds.left);
                    float positionY = (float) (windowBounds.top - taskBounds.top);
                    if (iconInfo != null && iconInfo.getSceneTag() == 3) {
                        positionX += (float) (window.getContainingFrame().left - windowLeft);
                        positionY += (float) (window.getContainingFrame().top - windowTop);
                    }
                    transaction.setPosition(atoken.mFloatWindwoIconSurfaceControl, positionX, positionY);
                    transaction.setAlpha(atoken.mFloatWindwoIconSurfaceControl, 0.0f);
                    transaction.show(atoken.mFloatWindwoIconSurfaceControl);
                    transaction.apply();
                } else {
                    Slog.e(TAG, "createHwFreeWindowFloatIconSurfaceControl canvas is null");
                }
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "createHwFreeWindowFloatIconSurfaceControl illegal argument");
            } catch (Surface.OutOfResourcesException e2) {
                Slog.e(TAG, "createHwFreeWindowFloatIconSurfaceControl out of resource");
            } catch (Throwable th) {
                surface.destroy();
                throw th;
            }
            surface.destroy();
        }
    }

    private void adjustHwFreeWindowFloatIconLayer(AppWindowToken atoken, SurfaceControl.Transaction transaction) {
        if (atoken.getSurfaceControl() != null && atoken.getSurfaceControl().isValid() && atoken.mFloatWindwoIconSurfaceControl != null && atoken.mFloatWindwoIconSurfaceControl.isValid()) {
            Slog.d(TAG, "adjustHwFreeWindowFloatIconLayer" + atoken.getSurfaceControl());
            atoken.mFloatWindwoIconSurfaceControl.reparent(atoken.getSurfaceControl());
            transaction.setLayer(atoken.mFloatWindwoIconSurfaceControl, Integer.MAX_VALUE);
            transaction.apply(true);
            atoken.mHasIconLayer = true;
        }
    }

    private void addListenToHwFreeWindowFloatAnim(Animation animation, AppWindowToken atoken) {
        animation.setAnimationListener(new Animation.AnimationListener() {
            /* class com.android.server.wm.HwWindowManagerServiceEx.AnonymousClass5 */

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationStart(Animation animation) {
                Slog.d(HwWindowManagerServiceEx.TAG, "hwFreeWindowFloatAnim start,onAnimationStart");
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation) {
                Slog.d(HwWindowManagerServiceEx.TAG, "hwFreeWindowFloatAnim end,onAnimationEnd");
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public void clearHwFreeWindowFloatIconLayer(AppWindowToken atoken) {
        if (HwWmConstants.IS_SUPPORT_FLOAT_TO_WINDOW_ANIM && atoken != null && atoken.mHasIconLayer) {
            synchronized (this.mIWmsInner.getGlobalLock()) {
                if (atoken.mFloatWindwoIconSurfaceControl != null) {
                    if (atoken.mFloatWindwoIconSurfaceControl.isValid()) {
                        atoken.mFloatWindwoIconSurfaceControl.setWindowIconInfo(0, 0, 0, new byte[1], 1, 0, 0, 0);
                        SurfaceControl.Transaction transaction = this.mTransactionFactory.make();
                        transaction.remove(atoken.mFloatWindwoIconSurfaceControl);
                        transaction.apply(true);
                        Slog.d(TAG, "clearHwFreeWindowFloatIconLayer real removed" + atoken.mFloatWindwoIconSurfaceControl);
                    }
                    atoken.mFloatWindwoIconSurfaceControl = null;
                    atoken.mHasIconLayer = false;
                    this.mLastFloatIconlayerTaskId = -1;
                }
            }
        }
    }

    public Animation loadHwFreeWindowFloatOpenSceneAnimation(Animation animation, int transit, AppWindowToken atoken) {
        if (!HwWmConstants.IS_SUPPORT_FLOAT_TO_WINDOW_ANIM) {
            Slog.d(TAG, "float to window animation disabled");
            return animation;
        } else if (atoken == null) {
            Slog.w(TAG, "find no atoken when try to override float open animation");
            return animation;
        } else {
            DisplayContent dc = atoken.getDisplayContent();
            if (dc == null) {
                Slog.w(TAG, "find no display content when try to override float open animation");
                return animation;
            } else if (!isHwFreeWindowFloatOpenScene(atoken, transit, dc)) {
                return animation;
            } else {
                WindowState window = atoken.findMainWindow();
                if (!isValidWindow(window)) {
                    return animation;
                }
                if ((window.mAttrs.flags & 524288) == 524288 && (window.mAttrs.flags & HwGlobalActionsData.FLAG_KEYCOMBINATION_INIT_STATE) == 4194304) {
                    return animation;
                }
                float iconRadius = HwAppTransitionImpl.getFloatBallOriginalRadius();
                HwFreeWindowFloatIconInfo iconInfo = getFloatIconInfo(atoken);
                Animation floatOpenSceneAnimation = HwAppTransitionImpl.createFloatOpenAnimation(window, iconInfo.getFloatPivotX().floatValue(), iconInfo.getFloatPivotY().floatValue(), iconRadius);
                if (floatOpenSceneAnimation == null) {
                    return animation;
                }
                setUpHwFreeWindowFloatIconAnimation(atoken, dc, window, iconRadius, iconInfo);
                addListenToHwFreeWindowFloatAnim(floatOpenSceneAnimation, atoken);
                return floatOpenSceneAnimation;
            }
        }
    }

    private AppWindowToken getTopApp(ArraySet<AppWindowToken> apps, boolean isIgnoreHidden) {
        int prefixOrderIndex;
        int topPrefixOrderIndex = Integer.MIN_VALUE;
        AppWindowToken topApp = null;
        for (int i = apps.size() - 1; i >= 0; i--) {
            AppWindowToken app = apps.valueAt(i);
            if ((!isIgnoreHidden || !app.isHidden()) && (prefixOrderIndex = app.getPrefixOrderIndex()) > topPrefixOrderIndex) {
                topPrefixOrderIndex = prefixOrderIndex;
                topApp = app;
            }
        }
        return topApp;
    }

    public Interpolator getMagicWindowMoveInterpolator() {
        return this.mMagicWindowMoveInterpolator;
    }

    public void setMagicWindowMoveInterpolator(Interpolator interpolator) {
        this.mMagicWindowMoveInterpolator = interpolator;
    }

    public void setMagicWindowAnimation(boolean isStart, Animation enter, Animation exit) {
        if (isStart) {
            Animation[] animationArr = this.mActivityStartAnimations;
            animationArr[0] = enter;
            animationArr[1] = exit;
            return;
        }
        Animation[] animationArr2 = this.mActivityFinishAnimations;
        animationArr2[0] = enter;
        animationArr2[1] = exit;
    }

    public Animation getMagicWindowAnimation(Animation animation, boolean isEnter, int transit, AppWindowToken appWindowToken, Rect frame) {
        if (!HwMwUtils.ENABLED || (!appWindowToken.inHwMagicWindowingMode() && transit != 12)) {
            return animation;
        }
        return HwPartMagicWindowServiceFactory.getInstance().getHwPartMagicWindowServiceFactoryImpl().getHwMagicWinManager().getHwMagicWinAnimation(animation, isEnter, transit, new AppWindowTokenExt(appWindowToken), frame, isAppLauncher(appWindowToken));
    }

    private void setIntelligentWindowExitInfo(Bundle bundle, int callingUid) {
        String uidName = this.mContext.getPackageManager().getNameForUid(callingUid);
        if (uidName != null && uidName.contains(HwWmConstants.INTELLIGENT_PKG_NAME) && bundle != null) {
            this.mIntelligentCardPosition = (Rect) bundle.getParcelable(INTELLIGENT_RECT_KEY);
            this.mIntelligentCustomCornerRadius = bundle.getFloat(INTELLIGENT_CORNER_RADIUS_KEY);
            this.mIsNeedDefaultAnimation = false;
            Log.i(TAG, "set card position info success, position = " + this.mIntelligentCardPosition);
        }
    }

    public Animation loadAppWindowExitToLauncherAnimation(Animation animation, int transit, Rect frame, AppWindowToken atoken) {
        boolean isSplitMode;
        if (!HwWmConstants.IS_HW_SUPPORT_LAUNCHER_EXIT_ANIM) {
            return animation;
        }
        if (atoken == null) {
            Slog.w(TAG, "find no atoken when try to override app exit to launcher animation");
            return animation;
        }
        DisplayContent dc = atoken.getDisplayContent();
        if (dc == null) {
            Slog.w(TAG, "find no display content when try to override app exit to launcher animation");
            return animation;
        }
        if (atoken.inHwMagicWindowingMode()) {
            isSplitMode = HwMwUtils.performPolicy(132, new Object[]{Integer.valueOf(atoken.getStack().mStackId), true}).getBoolean("RESULT_IN_APP_SPLIT", false);
        } else {
            isSplitMode = false;
        }
        if (isAppExitToLauncher(atoken, transit, dc) && frame != null && !isSplitMode) {
            Animation appExitToIconAnimation = HwAppTransitionImpl.createAppExitToIconAnimation(atoken, frame.height(), this.mExitIconWidth, this.mExitIconHeight, this.mExitPivotX, this.mExitPivotY, this.mExitIconBitmap, this.mExitFlag, this.mLazyModeOnEx);
            return appExitToIconAnimation != null ? appExitToIconAnimation : animation;
        } else if (!isLauncherOpen(atoken, transit, dc) || frame == null) {
            return animation;
        } else {
            Animation launcherEnterAnimation = HwAppTransitionImpl.createLauncherEnterAnimation(atoken, frame.height(), this.mExitIconWidth, this.mExitIconHeight, this.mExitPivotX, this.mExitPivotY, this.mExitIconBitmap);
            return launcherEnterAnimation != null ? launcherEnterAnimation : animation;
        }
    }

    public void setRtgThreadForAnimation(boolean isAdd) {
        HwPartIawareUtil.setRtgThreadForAnimation(isAdd);
    }

    public void setHwSecureScreenShot(WindowState win) {
        WindowStateAnimator winAnimator = win.mWinAnimator;
        if (winAnimator.mSurfaceController != null) {
            if ((win.mAttrs.hwFlags & 4096) != 0) {
                if (!this.mSecureScreenShot.contains(win)) {
                    winAnimator.mSurfaceController.setSecureScreenShot(true);
                    this.mSecureScreenShot.add(win);
                    Slog.i(TAG, "Set SecureScreenShot by: " + win);
                }
            } else if (this.mSecureScreenShot.contains(win)) {
                this.mSecureScreenShot.remove(win);
                winAnimator.mSurfaceController.setSecureScreenShot(false);
                Slog.i(TAG, "Remove SecureScreenShot by: " + win);
            }
            if ((win.mAttrs.hwFlags & 8192) != 0) {
                if (!this.mSecureScreenRecords.contains(win)) {
                    winAnimator.mSurfaceController.setSecureScreenRecord(true);
                    this.mSecureScreenRecords.add(win);
                    Slog.i(TAG, "Set SecureScreenRecord by: " + win);
                }
            } else if (this.mSecureScreenRecords.contains(win)) {
                this.mSecureScreenRecords.remove(win);
                winAnimator.mSurfaceController.setSecureScreenRecord(false);
                Slog.i(TAG, "Remove SecureScreenRecord by: " + win);
            }
        }
        if (isSecureWindow(win) && !this.mSecureWindows.contains(win)) {
            this.mSecureWindows.add(win);
        }
        if (!this.mIWmsInner.getService().mAtmService.mHwATMSEx.isNewPcMultiCastMode()) {
            showSecureWindowForWindowCastMode();
        }
    }

    public void travelAllWindow(WindowState w) {
        if (this.mIWmsInner.getService().mAtmService.mHwATMSEx.isNewPcMultiCastMode() && w.getDisplayId() == 0 && w.isVisible() && isSecureWindow(w)) {
            int taskId = getWindowTaskIdForPCMultiCast(w);
            int mode = getWindowingModeForPCMultiCast(w);
            if (taskId >= 0 && mode != 0) {
                this.mCurSecureTasks.put(Integer.valueOf(taskId), Integer.valueOf(mode));
            }
        }
    }

    public void handleWindowsAfterTravel(int displayId) {
        if (this.mIWmsInner.getService().mAtmService.mHwATMSEx.isNewPcMultiCastMode() && displayId == 0) {
            if (!this.mCurSecureTasks.equals(this.mPreSecureTasks)) {
                HashSet<Integer> allTaskSet = new HashSet<>();
                allTaskSet.addAll(this.mCurSecureTasks.keySet());
                allTaskSet.addAll(this.mPreSecureTasks.keySet());
                Iterator<Integer> it = allTaskSet.iterator();
                while (it.hasNext()) {
                    int taskId = it.next().intValue();
                    if (this.mCurSecureTasks.containsKey(Integer.valueOf(taskId)) && !this.mPreSecureTasks.containsKey(Integer.valueOf(taskId))) {
                        handleSecureWindow(taskId, this.mCurSecureTasks.get(Integer.valueOf(taskId)).intValue(), true);
                    } else if (!this.mCurSecureTasks.containsKey(Integer.valueOf(taskId)) && this.mPreSecureTasks.containsKey(Integer.valueOf(taskId))) {
                        handleSecureWindow(taskId, this.mPreSecureTasks.get(Integer.valueOf(taskId)).intValue(), false);
                    }
                }
                this.mPreSecureTasks.clear();
                this.mPreSecureTasks.putAll(this.mCurSecureTasks);
            }
            this.mCurSecureTasks.clear();
        }
    }

    private void handleSecureWindow(int taskId, int mode, boolean isShow) {
        if (isShow) {
            this.mIWmsInner.getPolicy().sendShowViewMsg(taskId, 2, mode);
        } else {
            this.mIWmsInner.getPolicy().sendHideViewMsg(taskId, 2, mode);
        }
    }

    public void removeWindow(WindowState win) {
        if (win != null && this.mSecureWindows.contains(win)) {
            this.mSecureWindows.remove(win);
            if (!this.mIWmsInner.getService().mAtmService.mHwATMSEx.isNewPcMultiCastMode()) {
                showSecureWindowForWindowCastMode();
            }
        }
    }

    private boolean isSecureWindow(WindowState win) {
        if (win == null) {
            return false;
        }
        if ((win.mAttrs.hwFlags & 4096) == 0 && (win.mAttrs.hwFlags & 8192) == 0 && (win.mAttrs.flags & 8192) == 0) {
            return false;
        }
        return true;
    }

    private int getWindowTaskIdForPCMultiCast(WindowState win) {
        if (win.mAttrs.type == 2011) {
            WindowState imeHolder = this.mIWmsInner.getImeHolder();
            if (imeHolder == null || imeHolder.getTask() == null) {
                return -1;
            }
            return imeHolder.getTask().mTaskId;
        } else if (win.getTask() != null) {
            return win.getTask().mTaskId;
        } else {
            return -1;
        }
    }

    private int getWindowingModeForPCMultiCast(WindowState win) {
        if (win.mAttrs.type != 2011) {
            return win.getWindowingMode();
        }
        WindowState imeHolder = this.mIWmsInner.getImeHolder();
        if (imeHolder != null) {
            return imeHolder.getWindowingMode();
        }
        return 0;
    }

    private void showSecureWindowForWindowCastMode() {
        boolean isNeedShowSecureWindow = false;
        boolean isSecurePadCast = false;
        int padCastDisplayId = this.mIWmsInner.getService().mAtmService.mHwATMSEx.getVirtualDisplayId("padCast");
        for (int i = this.mSecureWindows.size() - 1; i >= 0; i--) {
            WindowState windowState = this.mSecureWindows.get(i);
            if (windowState == null || !isSecureWindow(windowState) || !windowState.isVisible()) {
                this.mSecureWindows.remove(i);
            } else {
                isNeedShowSecureWindow = true;
                if (padCastDisplayId == windowState.getDisplayId() || (windowState.isInputMethodWindow() && this.mIWmsInner.getImeHolder() != null && padCastDisplayId == this.mIWmsInner.getImeHolder().getDisplayId())) {
                    isSecurePadCast = true;
                }
            }
        }
        this.mIWmsInner.getService().mAtmService.mHwATMSEx.notifySecureStateChange(padCastDisplayId, isSecurePadCast);
        if (!HwPCUtils.isInWindowsCastMode()) {
            return;
        }
        if (!isNeedShowSecureWindow || this.mIWmsInner.getPolicy() == null || this.mIWmsInner.getPolicy().isKeyguardLocked()) {
            sHwWindowsCastManager.sendHideViewMsg(2);
        } else {
            sHwWindowsCastManager.sendShowViewMsg(2);
        }
    }

    private boolean isValidBlurInfo(ActivityManager.TaskSnapshot snapshot, int blurLevel) {
        if (snapshot == null) {
            Slog.e(TAG, "blurSnapshot snapshot is null");
            return false;
        } else if (blurLevel == 1 || blurLevel == 2) {
            return true;
        } else {
            Slog.e(TAG, "blurSnapshot blurLevle is invalid: " + blurLevel);
            return false;
        }
    }

    private int getBlurStyle(boolean isDarkMode, int blurLevel) {
        if (isDarkMode && blurLevel == 1) {
            return 2;
        }
        if (!isDarkMode && blurLevel == 1) {
            return 0;
        }
        if (isDarkMode && blurLevel == 2) {
            return 3;
        }
        if (isDarkMode || blurLevel != 2) {
            return -1;
        }
        return 1;
    }

    private boolean isAppHasLock(String packageName, int userId) {
        if (packageName == null) {
            Slog.e(TAG, "getBlurLevel-isAppHasLock: packageName is null");
            return false;
        }
        ContentResolver resolver = this.mContext.getContentResolver();
        String appLockList = Settings.Secure.getStringForUser(resolver, "app_lock_list", userId);
        String appUnlockedList = Settings.Secure.getStringForUser(resolver, "applock_unlocked_list", userId);
        if (Settings.Secure.getInt(resolver, "app_lock_func_status", 0) != 1) {
            return false;
        }
        if (!(AwarenessInnerConstants.SEMI_COLON_KEY + appLockList + AwarenessInnerConstants.SEMI_COLON_KEY).contains(AwarenessInnerConstants.SEMI_COLON_KEY + packageName + AwarenessInnerConstants.SEMI_COLON_KEY)) {
            if (!(AwarenessInnerConstants.SEMI_COLON_KEY + appUnlockedList + AwarenessInnerConstants.SEMI_COLON_KEY).contains(AwarenessInnerConstants.SEMI_COLON_KEY + packageName + AwarenessInnerConstants.SEMI_COLON_KEY)) {
                return false;
            }
        }
        return true;
    }

    private int getActivityBlurLevel(ComponentName componentName, PackageManager pm) {
        if (componentName == null) {
            Slog.e(TAG, "getBlurLevel-activity componentName is null");
            return -1;
        }
        int activityLevel = -1;
        try {
            Bundle activityBundle = pm.getActivityInfo(componentName, 128).metaData;
            if (activityBundle != null) {
                activityLevel = activityBundle.getInt(BLUR_LEVEL_KEY, -1);
                if (isIllegalBlurLevel(activityLevel)) {
                    activityLevel = -1;
                }
                Slog.i(TAG, componentName.flattenToString() + " blur level: " + activityLevel);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "getBlurLevel-activity Exception: componentName not found");
        }
        return activityLevel;
    }

    private int getAppBlurLevel(String appPackageName, PackageManager pm) {
        if (appPackageName == null) {
            Slog.e(TAG, "getBlurLevel-application appPackageName is null");
            return -1;
        }
        int appLevel = -1;
        try {
            Bundle appBundle = pm.getApplicationInfo(appPackageName, 128).metaData;
            if (appBundle != null) {
                appLevel = appBundle.getInt(BLUR_LEVEL_KEY, -1);
                if (isIllegalBlurLevel(appLevel)) {
                    appLevel = -1;
                }
                Slog.i(TAG, appPackageName + " blur level: " + appLevel);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "getBlurLevel-application Exception: appPackageName not found");
        }
        return appLevel;
    }

    private boolean isWhiteListApp(String packageName) {
        return TENCENT_QQ.equals(packageName) || TENCENT_MM.equals(packageName);
    }

    private boolean isIllegalBlurLevel(int blurLevel) {
        return blurLevel > 2 || blurLevel < 0;
    }

    private boolean isInPaymentProtectionCenter(String packageName) {
        return TrustSpaceManager.getDefault().isIntentProtectedApp(packageName);
    }

    private int getBlurLevel(AppWindowToken appWindowToken) {
        if (!CONFIG_PRIVACY_SNAPSHOT_BLUR) {
            Slog.i(TAG, "getBlurLevel: config privacy snapshot blur is false, not blur");
            return 0;
        } else if (appWindowToken == null) {
            Slog.e(TAG, "getBlurLevel: appWindowToken is null, not blur");
            return 0;
        } else {
            String packageName = appWindowToken.appPackageName;
            if (isAppHasLock(packageName, appWindowToken.mWmService.mCurrentUserId)) {
                Slog.i(TAG, "getBlurLevel: " + packageName + " has app lock, not blur");
                return 0;
            }
            PackageManager pm = this.mContext.getPackageManager();
            if (pm == null) {
                Slog.e(TAG, "getBlurLevel: getPackageManager failed, not blur");
                return 0;
            }
            int blurLevel = getActivityBlurLevel(appWindowToken.mActivityComponent, pm);
            if (blurLevel == -1) {
                blurLevel = getAppBlurLevel(packageName, pm);
            }
            if (blurLevel != -1) {
                return blurLevel;
            }
            if (isWhiteListApp(packageName) || !isInPaymentProtectionCenter(packageName)) {
                return 0;
            }
            Slog.i(TAG, "getBlurLevel: " + packageName + " is in payment protection center, light blur");
            return 1;
        }
    }

    private boolean isDarkMode(Task task) {
        if (task == null || task.getConfiguration() == null) {
            Slog.e(TAG, "task or getConfiguration is null, can not recognise dark mode");
            return false;
        } else if ((task.getConfiguration().uiMode & 48) == 32) {
            return true;
        } else {
            return false;
        }
    }

    private Optional<ActivityManager.TaskSnapshot> blurSnapshot(ActivityManager.TaskSnapshot snapshot, boolean isDarkMode, int blurLevel) {
        if (!isValidBlurInfo(snapshot, blurLevel)) {
            return Optional.empty();
        }
        Bitmap bitmap = Bitmap.wrapHardwareBuffer(snapshot.getSnapshot(), snapshot.getColorSpace());
        if (bitmap == null) {
            Slog.e(TAG, "blurSnapshot wrap hardware bitmap failed");
            return Optional.empty();
        }
        Bitmap swBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        if (swBitmap == null) {
            Slog.e(TAG, "blurSnapshot copy swBitmap failed");
            return Optional.empty();
        }
        Bitmap swBitmapBlur = Bitmap.createBitmap(swBitmap.getWidth(), swBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        if (swBitmapBlur == null) {
            Slog.e(TAG, "blurSnapshot create swBitmapBlur failed");
            return Optional.empty();
        }
        int style = getBlurStyle(isDarkMode, blurLevel);
        if (style == -1) {
            Slog.e(TAG, "blurSnapshot style is invalid");
            return Optional.empty();
        }
        int errorCode = new BlurAlgorithm().styleBlur(this.mContext, swBitmap, swBitmapBlur, style);
        if (errorCode != 0) {
            Slog.e(TAG, "blurSnapshot styleBlur error: " + errorCode);
            return Optional.empty();
        }
        Bitmap hwBitmap = swBitmapBlur.copy(Bitmap.Config.HARDWARE, false);
        if (hwBitmap == null) {
            Slog.e(TAG, "blurSnapshot copy hwBitmap failed");
            return Optional.empty();
        }
        GraphicBuffer buffer = hwBitmap.createGraphicBufferHandle();
        if (buffer == null) {
            Slog.e(TAG, "blurSnapshot create graphic buffer failed");
            return Optional.empty();
        }
        Slog.i(TAG, "blurSnapshot, isDarkMode: " + isDarkMode + ", blurLevel: " + blurLevel);
        ActivityManager.TaskSnapshot blurSnapshot = new ActivityManager.TaskSnapshot(snapshot.getTopActivityComponent(), buffer, snapshot.getColorSpace(), snapshot.getOrientation(), snapshot.getContentInsets(), snapshot.isReducedResolution(), snapshot.getScale(), snapshot.isRealSnapshot(), snapshot.getWindowingMode(), snapshot.getSystemUiVisibility(), snapshot.isTranslucent());
        blurSnapshot.setWindowBounds(snapshot.getWindowBounds());
        return Optional.of(blurSnapshot);
    }

    private class SnapshotBlurInfo {
        private final int blurLevel;
        private final ActivityManager.TaskSnapshot snapshot;

        public SnapshotBlurInfo(ActivityManager.TaskSnapshot snapshot2, int blurLevel2) {
            this.snapshot = snapshot2;
            this.blurLevel = blurLevel2;
        }
    }

    public Point updateLazyModePoint(int type, Point point) {
        if (type == 0) {
            return point;
        }
        DisplayInfo defaultDisplayInfo = new DisplayInfo();
        this.mIWmsInner.getDefaultDisplayContentLocked().getDisplay().getDisplayInfo(defaultDisplayInfo);
        boolean isPortrait = defaultDisplayInfo.logicalHeight > defaultDisplayInfo.logicalWidth;
        int width = isPortrait ? defaultDisplayInfo.logicalWidth : defaultDisplayInfo.logicalHeight;
        int height = isPortrait ? defaultDisplayInfo.logicalHeight : defaultDisplayInfo.logicalWidth;
        float pendingX = 0.0f;
        float pendingY = 0.0f;
        if (type == 1) {
            pendingY = ((float) height) * 0.25f;
        } else if (type == 2) {
            pendingX = ((float) width) * 0.25f;
            pendingY = ((float) height) * 0.25f;
        }
        return new Point((int) ((((float) point.x) * 0.75f) + pendingX), (int) ((((float) point.y) * 0.75f) + pendingY));
    }

    public float getLazyModeScale() {
        return 0.75f;
    }

    public void takeTaskSnapshot(IBinder binder, boolean isAlwaysTake) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIWmsInner.getGlobalLock()) {
                AppWindowToken appWindowToken = this.mIWmsInner.getRoot().getAppWindowToken(binder);
                if (appWindowToken == null || (!isAlwaysTake && appWindowToken.mHadTakenSnapShot)) {
                    Slog.v(TAG, "takeTaskSnapshot appWindowToken is null");
                } else {
                    if (isAlwaysTake) {
                        appWindowToken.mHadTakenSnapShot = false;
                    }
                    WindowContainer wc = appWindowToken.getParent();
                    if (wc instanceof Task) {
                        this.mIWmsInner.getTaskSnapshotController().snapshotTasks(Sets.newArraySet(new Task[]{(Task) wc}));
                        if (isAlwaysTake) {
                            appWindowToken.mHadTakenSnapShot = false;
                        }
                    } else {
                        Slog.v(TAG, "takeTaskSnapshot has no tasks");
                    }
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void setIsNeedBlur(boolean isNeedBlur) {
        this.mIsNeedBlur = isNeedBlur;
    }

    public void handleWaitBlurTasks(WindowManagerService wms) {
        this.mHwHandler.post(new Runnable(wms) {
            /* class com.android.server.wm.$$Lambda$HwWindowManagerServiceEx$e7jn91es33mOA6X5uR9S1kTykNI */
            private final /* synthetic */ WindowManagerService f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwWindowManagerServiceEx.this.lambda$handleWaitBlurTasks$2$HwWindowManagerServiceEx(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$handleWaitBlurTasks$2$HwWindowManagerServiceEx(WindowManagerService wms) {
        if (this.mIsNeedBlur && !this.mWaitBlurTaskMap.isEmpty()) {
            Slog.i(TAG, "handleWaitBlurTasks size: " + this.mWaitBlurTaskMap.size());
            for (Map.Entry<Task, SnapshotBlurInfo> entry : this.mWaitBlurTaskMap.entrySet()) {
                Task task = entry.getKey();
                SnapshotBlurInfo snapshotBlurInfo = entry.getValue();
                Optional<ActivityManager.TaskSnapshot> optional = blurSnapshot(snapshotBlurInfo.snapshot, isDarkMode(task), snapshotBlurInfo.blurLevel);
                if (optional.isPresent()) {
                    ActivityManager.TaskSnapshot blurSnapshot = optional.get();
                    synchronized (this.mIWmsInner.getGlobalLock()) {
                        wms.mTaskSnapshotController.saveSnapshot(task, blurSnapshot);
                    }
                }
            }
            this.mWaitBlurTaskMap.clear();
        }
    }

    public void addToWaitBlurTaskMap(AppWindowToken appWindowToken, Task task, ActivityManager.TaskSnapshot snapshot) {
        this.mHwHandler.post(new Runnable(appWindowToken, snapshot, task) {
            /* class com.android.server.wm.$$Lambda$HwWindowManagerServiceEx$NnOsTpMs0uJdtPjnWb4sSR8CZlw */
            private final /* synthetic */ AppWindowToken f$1;
            private final /* synthetic */ ActivityManager.TaskSnapshot f$2;
            private final /* synthetic */ Task f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwWindowManagerServiceEx.this.lambda$addToWaitBlurTaskMap$3$HwWindowManagerServiceEx(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$addToWaitBlurTaskMap$3$HwWindowManagerServiceEx(AppWindowToken appWindowToken, ActivityManager.TaskSnapshot snapshot, Task task) {
        int blurLevel = getBlurLevel(appWindowToken);
        Slog.i(TAG, "getBlurLevel: " + blurLevel);
        if (blurLevel != 0) {
            this.mWaitBlurTaskMap.put(task, new SnapshotBlurInfo(snapshot, blurLevel));
        }
    }

    public void handleGestureActionForBlur(WindowManagerService wms, Intent intent) {
        if (wms == null || intent == null) {
            Slog.e(TAG, "handleGestureActionForBlur parameter is null");
            return;
        }
        String category = intent.getStringExtra(GESTURE_CATEGORY_KEY);
        if (category == null) {
            Slog.e(TAG, "handleGestureActionForBlur category is null");
            return;
        }
        char c = 65535;
        int hashCode = category.hashCode();
        if (hashCode != -1938314084) {
            if (hashCode == -543270494 && category.equals("enter_recent")) {
                c = 0;
            }
        } else if (category.equals("exit_recent")) {
            c = 1;
        }
        if (c == 0) {
            Slog.i(TAG, "handleGestureActionForBlur category: " + category);
            setIsNeedBlur(false);
        } else if (c == 1) {
            Slog.i(TAG, "handleGestureActionForBlur category: " + category);
            setIsNeedBlur(true);
            handleWaitBlurTasks(wms);
        }
    }

    public Rect getFocuseWindowVisibleFrame(WindowManagerService wms) {
        Rect currentRect;
        WindowState currentWindowState = wms.getFocusedWindow();
        if (currentWindowState != null && (currentRect = currentWindowState.getVisibleFrameLw()) != null) {
            return currentRect;
        }
        HwFreeFormUtils.log(TAG, "getFocuseWindowVisibleFrame is null");
        return null;
    }

    public String getTopAppPackageByWindowMode(int windowMode, RootWindowContainer root) {
        TaskStack stack = root.getStack(windowMode, 1);
        if (stack == null || stack.getTopChild() == null || stack.getTopChild().getTopFullscreenAppToken() == null) {
            return null;
        }
        return stack.getTopChild().getTopFullscreenAppToken().appPackageName;
    }

    public void relaunchIMEProcess() {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.relaunchIMEIfNecessary();
            } catch (RemoteException e) {
                Log.e(TAG, "relaunchIMEProcess()");
            }
        }
    }

    public void togglePCMode(boolean isPcMode, int displayId) {
        RootWindowContainer root = this.mIWmsInner.getRoot();
        if (root != null) {
            DisplayPolicy displayPolicy = null;
            DisplayContent dc = root.getDisplayContent(displayId);
            if (dc instanceof DisplayContentBridge) {
                displayPolicy = dc.getDisplayPolicy();
            }
            if (isPcMode) {
                HwPCUtils.log(TAG, "registerExternalPointerEventListener for screenlock");
                if (displayPolicy != null) {
                    displayPolicy.registerExternalPointerEventListener();
                    return;
                }
                return;
            }
            HwPCUtils.log(TAG, "unRegisterExternalPointerEventListener for screenlock");
            if (displayPolicy != null) {
                displayPolicy.unRegisterExternalPointerEventListener();
            }
            synchronized (this.mIWmsInner.getGlobalLock()) {
                if (dc instanceof DisplayContentBridge) {
                    ((DisplayContentBridge) dc).togglePCMode(isPcMode);
                }
            }
        }
    }

    public Bitmap getDisplayBitmap(int displayId, int width, int height) {
        DisplayManagerInternal displayManagerInternal;
        RootWindowContainer root = this.mIWmsInner.getRoot();
        if (root == null) {
            return null;
        }
        ArrayList<WindowState> windows = new ArrayList<>();
        synchronized (this.mIWmsInner.getGlobalLock()) {
            root.forAllWindows(new ToBooleanFunction(windows) {
                /* class com.android.server.wm.$$Lambda$HwWindowManagerServiceEx$ZYNqqgwBGCS0ORherUSzEahKJ4 */
                private final /* synthetic */ ArrayList f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean apply(Object obj) {
                    return HwWindowManagerServiceEx.lambda$getDisplayBitmap$4(this.f$0, (WindowState) obj);
                }
            }, false);
        }
        if (windows.size() > 0 || (displayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class)) == null) {
            return null;
        }
        IBinder token = displayManagerInternal.getDisplayToken(displayId);
        if (token != null) {
            return SurfaceControl.screenshot(token, width, height);
        }
        HwPCUtils.log(TAG, "getDisplayBitmap, getDisplayToken is null , displayId=" + displayId);
        return null;
    }

    static /* synthetic */ boolean lambda$getDisplayBitmap$4(ArrayList windows, WindowState w) {
        if (w == null || !HwPCUtils.isValidExtDisplayId(w.getDisplayId()) || w.mAttrs == null || (w.mAttrs.flags & 8192) == 0 || !w.isVisible()) {
            return false;
        }
        windows.add(w);
        return true;
    }

    public boolean isSecureForPCDisplay(WindowState win) {
        if (win.getStack() == null || !HwPCUtils.isExtDynamicStack(win.getStack().mStackId) || win.getDisplayInfo() == null || (win.getDisplayInfo().flags & 2) != 0) {
            return true;
        }
        return false;
    }

    public void setGestureNavMode(String packageName, int uid, int leftMode, int rightMode, int bottomMode) {
        DefaultGestureNavManager gestureNavPolicy = (DefaultGestureNavManager) LocalServices.getService(DefaultGestureNavManager.class);
        if (gestureNavPolicy != null) {
            gestureNavPolicy.setGestureNavMode(packageName, uid, leftMode, rightMode, bottomMode);
        }
    }

    public ArrayList<WindowState> getSecureScreenWindow() {
        ArrayList<WindowState> secureScreenWindows = new ArrayList<>(2);
        secureScreenWindows.addAll(this.mSecureScreenRecords);
        secureScreenWindows.addAll(this.mSecureScreenShot);
        return secureScreenWindows;
    }

    public void removeSecureScreenWindow(WindowState win) {
        WindowStateAnimator winAnimator;
        if (win != null && (winAnimator = win.mWinAnimator) != null && winAnimator.mSurfaceController != null) {
            if (this.mSecureScreenRecords.contains(win)) {
                this.mSecureScreenRecords.remove(win);
                winAnimator.mSurfaceController.setSecureScreenRecord(false);
                Slog.i(TAG, "Remove SecureScreenRecord : " + win);
            }
            if (this.mSecureScreenShot.contains(win)) {
                this.mSecureScreenShot.remove(win);
                winAnimator.mSurfaceController.setSecureScreenShot(false);
                Slog.i(TAG, "Remove SecureScreenShot : " + win);
            }
        }
    }

    public void updateStatusBarInMagicWindow(int mode, WindowManager.LayoutParams attrs) {
        Bundle bundle;
        if (mode == 103 && HwMwUtils.ENABLED) {
            if (((attrs.flags & 1024) != 0 && (attrs.flags & Integer.MIN_VALUE) != 0) || attrs.type > 99 || attrs.type < 1 || attrs.type == 2) {
                return;
            }
            if ((attrs.type == 3 || "com.android.packageinstaller".equals(attrs.packageName)) && (bundle = HwMwUtils.performPolicy(101, new Object[]{Integer.valueOf(attrs.flags), attrs.packageName})) != null && bundle.size() != 0) {
                attrs.flags = bundle.getInt("enableStatusBar");
            }
        }
    }

    public void performDisplayTraversalLocked() {
        this.mIWmsInner.getRoot().performDisplayTraversal();
    }

    public boolean isShowDimForPCMode(WindowContainer host, Rect outBounds) {
        if (!HwPCUtils.isPcCastModeInServer()) {
            return true;
        }
        int displayId = -1;
        if (host instanceof Task) {
            Task task = (Task) host;
            if (task.getDisplayContent() != null) {
                displayId = task.getDisplayContent().getDisplayId();
            }
        } else if (host instanceof TaskStack) {
            TaskStack taskStack = (TaskStack) host;
            if (taskStack.getDisplayContent() != null) {
                displayId = taskStack.getDisplayContent().getDisplayId();
            }
        }
        if (!HwPCUtils.isValidExtDisplayId(displayId)) {
            return true;
        }
        if (outBounds.width() >= host.getBounds().width() && outBounds.height() >= host.getBounds().height()) {
            return true;
        }
        HwPCUtils.log(TAG, "The dim's bounds is smaller. Skip to show dim.");
        return false;
    }

    public boolean isSkipComputeImeTargetForHwMultiDisplay(WindowManagerPolicy.WindowState inputMethodWin, DisplayContent dc) {
        if (inputMethodWin != null && dc != null && (inputMethodWin instanceof WindowState) && HwPCUtils.isPcCastModeInServer() && !HwPCUtils.isValidExtDisplayId(dc.getDisplayId())) {
            if (HwPCUtils.enabledInPad()) {
                return true;
            }
            if (HwPCUtils.mTouchDeviceID == -1 || !((WindowState) inputMethodWin).isVisible() || !HwPCUtils.isValidExtDisplayId(inputMethodWin.getDisplayId())) {
                return false;
            }
            return true;
        }
        return false;
    }

    public String getTouchedWinPackageName(float x, float y, int displayId) {
        synchronized (this.mIWmsInner.getGlobalLock()) {
            DisplayContent dc = this.mIWmsInner.getRoot().getDisplayContent(displayId);
            if (dc == null) {
                Slog.e(TAG, "getTouchedWinState error, dc is null, displayid: " + displayId);
                return "";
            }
            WindowState win = dc.getTouchableWinAtPointLocked(x, y);
            if (win == null) {
                Slog.e(TAG, "getTouchedWinState error, win is null, displayid: " + displayId);
                return "";
            }
            return win.getOwningPackage();
        }
    }

    public boolean notifyDragAndDropForMultiDisplay(float x, float y, int displayId, DragEvent evt) {
        synchronized (this.mIWmsInner.getGlobalLock()) {
            if (evt == null) {
                Slog.e(TAG, "notifyDragAndDropForMultiDisplay error, evt is null!");
                return false;
            }
            DisplayContent dc = this.mIWmsInner.getRoot().getDisplayContent(displayId);
            if (dc == null) {
                Slog.e(TAG, "notifyDragAndDropForMultiDisplay error, dc is null " + displayId + ". Aborting.");
                return false;
            }
            WindowState win = dc.getTouchableWinAtPointLocked(x, y);
            if (win == null && this.mDropWindowState == null) {
                Slog.e(TAG, "notifyDragAndDropForMultiDisplay error, win is null " + displayId + ".  Aborting.");
                return false;
            } else if (evt.getAction() == 10000) {
                this.mDropWindowState = win;
                return true;
            } else if (evt.getAction() == 10001) {
                this.mDropWindowState = null;
                sTouchWin = null;
                this.mDragLocWindowState = null;
                return true;
            } else {
                if (evt.getAction() == 5 && win != this.mDragLocWindowState) {
                    this.mDragLocWindowState = win;
                }
                if (this.mDropWindowState != null) {
                    if (evt.getAction() == 5) {
                        Slog.w(TAG, "check droppable while saved win not null, set saved win null");
                        this.mDropWindowState = null;
                    } else if (win != this.mDropWindowState) {
                        Slog.i(TAG, "win changed during drop, set current win as saved win.");
                        win = this.mDropWindowState;
                    } else {
                        Slog.d(TAG, "will dispatchDragEvent to win of viewroot.");
                    }
                }
                try {
                    win.mClient.dispatchDragEvent(convertDragEventForSplitWindowIfNeeded(dc, evt));
                    return true;
                } catch (RemoteException e) {
                    Slog.e(TAG, "can't send drop notification to win.");
                    return false;
                }
            }
        }
    }

    private DragEvent convertDragEventForSplitWindowIfNeeded(DisplayContent dc, DragEvent event) {
        WindowState windowState = sTouchWin;
        if (windowState != null) {
            return obtainDragEvent(windowState, event, null);
        }
        WindowState windowState2 = this.mDragLocWindowState;
        if (windowState2 == null || !windowState2.inMultiWindowMode()) {
            return event;
        }
        return obtainDragEvent(this.mDragLocWindowState, event, null);
    }

    private DragEvent obtainDragEvent(WindowState win, DragEvent event, IDragAndDropPermissions dragAndDropPermissions) {
        float pointX = event.getX();
        float pointY = event.getY();
        if (!(win.mAppToken == null || win.mAppToken.getStack() == null)) {
            float winScale = win.mAppToken.getStack().mHwStackScale;
            if (winScale != 1.0f) {
                Rect rect = win.getBounds();
                pointX = ((float) rect.left) + ((event.getX() - ((float) rect.left)) / winScale);
                pointY = ((float) rect.top) + ((event.getY() - ((float) rect.top)) / winScale);
            }
        }
        return DragEvent.obtain(event.getAction(), win.translateToWindowX(pointX), win.translateToWindowY(pointY), event.getLocalState(), event.getClipDescription(), event.getClipData(), dragAndDropPermissions, event.getResult());
    }

    public void registerDropListenerForMultiDisplay(IHwMultiDisplayDropStartListener listener) {
        Slog.i(TAG, "registerDropListenerForMultiDisplay, listener:" + listener);
        this.mDropListenerForMultiDisplay = listener;
    }

    public void unregisterDropListenerForMultiDisplay() {
        Slog.i(TAG, "unregisterDropListenerForMultiDisplay.");
        this.mDropListenerForMultiDisplay = null;
    }

    public boolean dropStartForMultiDisplay(DragEvent dragEvent) {
        Slog.i(TAG, "dropStartForMultiDisplay, mDropListenerForMultiDisplay:" + this.mDropListenerForMultiDisplay);
        if (sHwMultiDisplayUtils.isInBasicMode() || sHwMultiDisplayUtils.isInSinkWindowsCastMode()) {
            if (this.mDropListenerForMultiDisplay != null) {
                try {
                    if (sTouchWin != null) {
                        float hwStackScale = sTouchWin.mAppToken.getStack().mHwStackScale;
                        if (hwStackScale != 1.0f) {
                            Rect rect = sTouchWin.getBounds();
                            dragEvent = DragEvent.obtain(dragEvent.getAction(), (dragEvent.getX() * hwStackScale) + ((float) rect.left), (dragEvent.getY() * hwStackScale) + ((float) rect.top), dragEvent.getLocalState(), dragEvent.getClipDescription(), dragEvent.getClipData(), null, dragEvent.getResult());
                        } else if (sTouchWin.mGlobalScale != 0.0f) {
                            dragEvent = DragEvent.obtain(dragEvent.getAction(), (dragEvent.getX() / sTouchWin.mGlobalScale) + ((float) sTouchWin.mWindowFrames.mFrame.left), (dragEvent.getY() / sTouchWin.mGlobalScale) + ((float) sTouchWin.mWindowFrames.mFrame.top), dragEvent.getLocalState(), dragEvent.getClipDescription(), dragEvent.getClipData(), null, dragEvent.getResult());
                        }
                    }
                    this.mDropListenerForMultiDisplay.onDropStart(dragEvent);
                    return true;
                } catch (RemoteException e) {
                    Slog.e(TAG, "onDragStart failed");
                }
            }
            return false;
        }
        Slog.i(TAG, "dropStartForMultiDisplay is not in cast mode");
        return false;
    }

    public void setOriginalDropPoint(float x, float y) {
        if (!sHwMultiDisplayUtils.isInSinkWindowsCastMode()) {
            Slog.i(TAG, "setOriginalDropPoint is not in cast mode");
            return;
        }
        IHwMultiDisplayDropStartListener iHwMultiDisplayDropStartListener = this.mDropListenerForMultiDisplay;
        if (iHwMultiDisplayDropStartListener == null) {
            Slog.i(TAG, "mDropListenerForMultiDisplay is null");
            return;
        }
        try {
            iHwMultiDisplayDropStartListener.setOriginalDropPoint(x, y);
        } catch (RemoteException e) {
            Slog.e(TAG, "setOriginalDropPoint failed");
        }
    }

    public void updateDragState(int dragState) {
        Slog.i(TAG, "updateDragState: " + dragState);
        IHwMultiDisplayDragStateListener iHwMultiDisplayDragStateListener = this.mMultiDisplayDragStateListener;
        if (iHwMultiDisplayDragStateListener == null) {
            Slog.i(TAG, "mMultiDisplayDragStateListener is null");
            return;
        }
        try {
            iHwMultiDisplayDragStateListener.updateDragState(dragState);
        } catch (RemoteException e) {
            Slog.e(TAG, "updateDragState failed");
        }
    }

    public void registerHwMultiDisplayDragStateListener(IHwMultiDisplayDragStateListener listener) {
        Slog.i(TAG, "registerHwMultiDisplayDragStateListener.");
        this.mMultiDisplayDragStateListener = listener;
    }

    public void unregisterHwMultiDisplayDragStateListener() {
        Slog.i(TAG, "unregisterHwMultiDisplayDragStateListener.");
        this.mMultiDisplayDragStateListener = null;
    }

    public void registerDragListenerForMultiDisplay(IHwMultiDisplayDragStartListener listener) {
        Slog.i(TAG, "registerDragListenerForMultiDisplay, listener:" + listener);
        this.mDragListenerForMultiDisplay = listener;
        this.mDragLocWindowState = null;
        sTouchWin = null;
    }

    public void unregisterDragListenerForMultiDisplay() {
        Slog.i(TAG, "unregisterDragListenerForMultiDisplay.");
        this.mDragListenerForMultiDisplay = null;
        this.mIsDroppableListener = null;
        this.mDragLocWindowState = null;
        sTouchWin = null;
    }

    public void registerBitmapDragListenerForMultiDisplay(IHwMultiDisplayBitmapDragStartListener listener) {
        Slog.i(TAG, "registerBitmapDragListenerForMultiDisplayBitmap, listener:" + listener);
        this.mBitmapDragListenerForMultiDisplay = listener;
    }

    public void unregisterBitmapDragListenerForMultiDisplay() {
        Slog.i(TAG, "unregisterDragListenerForMultiDisplay.");
        this.mBitmapDragListenerForMultiDisplay = null;
    }

    public boolean dragStartForMultiDisplay(ClipData clipData) {
        Slog.i(TAG, "dragStartForMultiDisplay, mDragListenerForMultiDisplay:" + this.mDragListenerForMultiDisplay);
        if (sHwMultiDisplayUtils.isInWindowsCastMode() || sHwMultiDisplayUtils.isInSinkWindowsCastMode()) {
            IHwMultiDisplayDragStartListener iHwMultiDisplayDragStartListener = this.mDragListenerForMultiDisplay;
            if (iHwMultiDisplayDragStartListener != null) {
                try {
                    iHwMultiDisplayDragStartListener.onDragStart(clipData);
                    return true;
                } catch (RemoteException e) {
                    Slog.e(TAG, "onDragStart failed");
                }
            }
            return false;
        }
        Slog.i(TAG, "dragStartForMultiDisplay return false");
        return false;
    }

    public boolean setDragStartBitmap(Bitmap bitmap) {
        IHwMultiDisplayBitmapDragStartListener iHwMultiDisplayBitmapDragStartListener;
        Slog.i(TAG, "setDragStartBitmap, listener:" + this.mBitmapDragListenerForMultiDisplay);
        if (sHwMultiDisplayUtils.isInWindowsCastMode() && (iHwMultiDisplayBitmapDragStartListener = this.mBitmapDragListenerForMultiDisplay) != null) {
            try {
                iHwMultiDisplayBitmapDragStartListener.onDragStart(bitmap);
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                return true;
            } catch (RemoteException e) {
                Slog.e(TAG, "onDragStart failed");
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (Throwable th) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                throw th;
            }
        }
        return false;
    }

    public void registerHwMultiDisplayBasicModeDragListener(IHwMultiDisplayBasicModeDragStartListener listener) {
        Slog.i(TAG, "registerHwMultiDisplayBasicModeDragListener, listener:" + listener);
        this.mBasicModeDragListenerForMultiDisplay = listener;
    }

    public void unregisterHwMultiDisplayBasicModeDragListener() {
        Slog.i(TAG, "unregisterHwMultiDisplayBasicModeDragListener.");
        this.mBasicModeDragListenerForMultiDisplay = null;
    }

    public boolean dragStartForBasicMode(ClipData data, Bitmap bitmap) {
        IHwMultiDisplayBasicModeDragStartListener iHwMultiDisplayBasicModeDragStartListener;
        Slog.i(TAG, "dragStartForBasicMode, listener:" + this.mBasicModeDragListenerForMultiDisplay);
        if (sHwMultiDisplayUtils.isInBasicMode() && (iHwMultiDisplayBasicModeDragStartListener = this.mBasicModeDragListenerForMultiDisplay) != null) {
            try {
                iHwMultiDisplayBasicModeDragStartListener.onDragStart(data, bitmap);
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                return true;
            } catch (RemoteException e) {
                Slog.e(TAG, "onDragStart failed");
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (Throwable th) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                throw th;
            }
        }
        return false;
    }

    public void registerIsDroppableForMultiDisplay(IHwMultiDisplayDroppableListener listener) {
        Slog.i(TAG, "registerIsDroppableForMultiDisplay.");
        this.mIsDroppableListener = listener;
    }

    public void setDroppableForMultiDisplay(float posX, float posY, boolean isDroppable) {
        if (!sHwMultiDisplayUtils.isInWindowsCastMode()) {
            Slog.i(TAG, "setDroppableForMultiDisplay: not in window cast mode.");
        } else if (this.mIsDroppableListener == null) {
            Slog.i(TAG, "mIsDroppableListener is null.");
        } else {
            try {
                Slog.i(TAG, "setDroppableForMultiDisplay " + isDroppable);
                if (!(this.mDragLocWindowState == null || this.mDragLocWindowState.mGlobalScale == 0.0f || !this.mDragLocWindowState.inMultiWindowMode())) {
                    posX = (posX / this.mDragLocWindowState.mGlobalScale) + ((float) this.mDragLocWindowState.mWindowFrames.mFrame.left);
                    posY = (posY / this.mDragLocWindowState.mGlobalScale) + ((float) this.mDragLocWindowState.mWindowFrames.mFrame.top);
                }
                this.mIsDroppableListener.onDroppableResult(posX, posY, isDroppable);
            } catch (RemoteException e) {
                Slog.e(TAG, "setDroppableForMultiDisplay RemoteException.");
            }
        }
    }

    public void sendFocusProcessToRMS(WindowState curFocus, WindowState oldFocus) {
        int newPid = -1;
        int oldPid = -1;
        int newDisplayId = -1;
        if (!(curFocus == null || curFocus.mSession == null)) {
            newPid = curFocus.mSession.mPid;
            newDisplayId = curFocus.getDisplayId();
        }
        if (!(oldFocus == null || oldFocus.mSession == null)) {
            oldPid = oldFocus.mSession.mPid;
        }
        if (newPid > 0 && newPid != oldPid && newPid != lastFocusPid) {
            lastFocusPid = newPid;
            Bundle args = new Bundle();
            args.putInt("pid", newPid);
            args.putInt("type", 3);
            args.putInt("displayId", newDisplayId);
            this.mIWmsInner.getWMMonitor().reportData("RESOURCE_WINSTATE", System.currentTimeMillis(), args);
        }
    }

    public void appTransitionBoost(DisplayContent displayContent, int transit) {
        ArraySet<AppWindowToken> openingApps = displayContent != null ? displayContent.mOpeningApps : null;
        int boostPid = -1;
        if (transit == 21 && openingApps != null) {
            Iterator<AppWindowToken> it = openingApps.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                AppWindowToken wtoken = it.next();
                WindowProcessController app = null;
                if (wtoken != null && wtoken.getName().contains("com.huawei.android.launcher")) {
                    ActivityRecord activity = wtoken.mActivityRecord;
                    if (activity != null) {
                        app = activity.app;
                    }
                    if (app != null) {
                        boostPid = app.getPid();
                        Slog.d(TAG, "ATC boost pid=" + boostPid);
                    }
                }
            }
            if (boostPid > 0) {
                int displayId = displayContent.getDisplayId();
                Bundle args = new Bundle();
                args.putInt("pid", boostPid);
                args.putInt("type", 3);
                args.putInt("displayId", displayId);
                this.mIWmsInner.getWMMonitor().reportData("RESOURCE_WINSTATE", System.currentTimeMillis(), args);
            }
        }
    }

    public void setNotchFlags(WindowState win, WindowManager.LayoutParams attrs, DisplayPolicy displayPolicy, int systemUiFlags) {
        if (win != null && attrs != null && displayPolicy != null && displayPolicy.getHwDisplayPolicyEx().canUpdateDisplayFrames(win, attrs, systemUiFlags)) {
            attrs.layoutInDisplayCutoutMode = 1;
        }
    }

    public void switchDragShadow(boolean isDroppable) {
        try {
            mIsSwitched = true;
            if (sDragWin != null) {
                sDragWin.mClient.dispatchDragEvent(DragEvent.obtain(7, 0.0f, 0.0f, null, null, null, null, isDroppable));
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can't siwtchDragShadwo to windows");
        }
    }

    public void restoreShadow() {
        try {
            mIsSwitched = false;
            if (sDragWin != null) {
                sDragWin.mClient.dispatchDragEvent(DragEvent.obtain(9, 0.0f, 0.0f, null, null, null, null, true));
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can't restoreShadow to windows");
        }
    }

    public void setTouchWinState(WindowState win) {
        sTouchWin = win;
    }

    public void setDragWinState(WindowState win) {
        sDragWin = win;
        mIsSwitched = false;
    }

    public String getDragSrcPkgName() {
        WindowState windowState = sDragWin;
        if (windowState != null) {
            return windowState.getOwningPackage();
        }
        return null;
    }

    public boolean isSwitched() {
        return mIsSwitched;
    }

    public void updateFocusWindowFreezed(boolean isGainFocus) {
        WindowState win;
        if (this.mIsGainFocus != isGainFocus) {
            this.mIsGainFocus = isGainFocus;
            synchronized (this.mIWmsInner.getGlobalLock()) {
                win = this.mIWmsInner.getDefaultDisplayContentLocked().mCurrentFocus;
            }
            if (win != null) {
                Slog.i(TAG, "reportFocusChangedSerialized:" + isGainFocus);
                win.reportFocusChangedSerialized(isGainFocus, true);
            }
        }
    }

    @GuardedBy({"mPhoneOperateListenerForMultiDisplay"})
    public void registerPhoneOperateListenerForHwMultiDisplay(IHwMultiDisplayPhoneOperateListener listener) {
        this.mPhoneOperateListenerForMultiDisplay = listener;
    }

    @GuardedBy({"mPhoneOperateListenerForMultiDisplay"})
    public void unregisterPhoneOperateListenerForHwMultiDisplay() {
        this.mPhoneOperateListenerForMultiDisplay = null;
    }

    @GuardedBy({"mPhoneOperateListenerForMultiDisplay"})
    public void onOperateOnPhone() {
        Slog.i(TAG, "onOperateOnPhone");
        if (this.mPhoneOperateListenerForMultiDisplay == null) {
            Slog.e(TAG, "mPhoneOperateListenerForMultiDisplay is null.");
            return;
        }
        try {
            Slog.d(TAG, "onOperateOnPhone()");
            this.mPhoneOperateListenerForMultiDisplay.onOperateOnPhone();
        } catch (RemoteException e) {
            Slog.e(TAG, "onOperateOnPhone RemoteException.");
        }
    }

    public int getTopActivityAdaptNotchState(String packageName) {
        return this.mIWmsInner.getService().getDefaultDisplayContentLocked().getTopActivityAdaptNotchState(packageName);
    }

    public Animation reloadHwSplitScreenOpeningAnimation(Animation animation, AppWindowToken token, ArraySet<AppWindowToken> openingApps, boolean isEnter) {
        if (animation == null || token == null || openingApps == null) {
            return animation;
        }
        int hwSplitScreensCount = 0;
        boolean isOpenTwoSplitScreens = false;
        Iterator<AppWindowToken> it = openingApps.iterator();
        while (true) {
            if (it.hasNext()) {
                AppWindowToken appWindowToken = it.next();
                if (appWindowToken != null && appWindowToken.inHwSplitScreenWindowingMode()) {
                    hwSplitScreensCount++;
                    continue;
                }
                if (hwSplitScreensCount > 1) {
                    isOpenTwoSplitScreens = true;
                    break;
                }
            } else {
                break;
            }
        }
        if (!isOpenTwoSplitScreens) {
            return animation;
        }
        if (!isEnter) {
            animation.setZAdjustment(1);
        } else if (token.inHwSplitScreenWindowingMode()) {
            animation.setStartOffset(animation.computeDurationHint());
            animation.setDuration(0);
            animation.setZAdjustment(0);
        }
        return animation;
    }

    public void registerMouseEventListener(IHwMouseEventListener listener, int displayId) {
        synchronized (this.mIWmsInner.getGlobalLock()) {
            if (this.mMouseEventListener == null) {
                this.mIWmsInner.getService().registerPointerEventListener(this.mMousePointerListener, displayId);
                this.mMouseEventListener = listener;
            }
        }
    }

    public void unregisterMouseEventListener(int displayId) {
        synchronized (this.mIWmsInner.getGlobalLock()) {
            if (this.mMouseEventListener != null) {
                this.mIWmsInner.getService().unregisterPointerEventListener(this.mMousePointerListener, displayId);
                this.mMouseEventListener = null;
            }
        }
    }

    public Rect getCrossAppTransitAnimBounds(Rect rect, Rect lazyModeRect, boolean isLazyMode, AppWindowToken token) {
        if (rect == null || token == null) {
            Slog.e(TAG, "getCrossAppTransitAnimBounds inputs is invalid.");
            return rect;
        } else if (token.inMultiWindowMode()) {
            return rect;
        } else {
            return getAnimationBounds(token, rect, isLazyMode, lazyModeRect);
        }
    }

    public float getCrossAppTransitAnimRoundCornerRadius(AppWindowToken token) {
        float defaultScreenCornerRadius = (float) this.mIWmsInner.getService().mContext.getResources().getDimensionPixelSize(34472569);
        if (token == null) {
            Slog.e(TAG, "getCrossAppTransitAnimRoundCornerRadius inputs is invalid.");
            return defaultScreenCornerRadius;
        } else if (token.inHwSplitScreenWindowingMode()) {
            return (float) this.mIWmsInner.getService().mContext.getResources().getDimensionPixelSize(34472572);
        } else {
            float screenRadius = dpToPx((float) FILLET_RADIUS_SIZE, this.mIWmsInner.getService().mContext.getResources().getDisplayMetrics());
            if (Float.compare(screenRadius, 0.0f) <= 0) {
                int[] notchParams = getNotchSizeProp();
                if (!(notchParams == null || notchParams.length == 0)) {
                    screenRadius = (float) calculateSize(notchParams[3], token);
                }
                if (Float.compare(screenRadius, 0.0f) <= 0) {
                    screenRadius = defaultScreenCornerRadius * (token.mOrientation == 0 ? INIT_LAND_RADIUS_RATIO : INIT_RADIUS_RATIO);
                }
            }
            Slog.d(TAG, "getCrossAppTransitAnimRoundCornerRadius radius " + screenRadius);
            return screenRadius;
        }
    }

    private int[] getNotchSizeProp() {
        try {
            if (NOTCH_PROP.isEmpty()) {
                return new int[0];
            }
            String[] params = NOTCH_PROP.split(",");
            int length = params.length;
            if (length < 4) {
                return new int[0];
            }
            int[] notchParams = new int[length];
            for (int i = 0; i < length; i++) {
                notchParams[i] = Integer.parseInt(params[i]);
            }
            return notchParams;
        } catch (NumberFormatException e) {
            Slog.e(TAG, "getNotchSizeProp number format error " + NOTCH_PROP);
            return new int[0];
        } catch (Exception e2) {
            Slog.e(TAG, "getNotchSizeProp error " + NOTCH_PROP);
            return new int[0];
        }
    }

    private float dpToPx(float dpValue, DisplayMetrics dm) {
        return TypedValue.applyDimension(1, dpValue, dm);
    }

    private int calculateSize(int inputSize, AppWindowToken token) {
        int size = inputSize;
        Point point = new Point();
        this.mIWmsInner.getService().getInitialDisplaySize(0, point);
        int defaultWidth = point.x < point.y ? point.x : point.y;
        int rogWidth = SystemProperties.getInt("persist.sys.rog.width", 0);
        int rogHeight = SystemProperties.getInt("persist.sys.rog.height", 0);
        if (defaultWidth == 0 || rogWidth == 0 || rogHeight == 0) {
            int resolutionSize = getResolutionSize(size, token);
            Slog.w(TAG, "error rogWidth = " + rogWidth + "rogHeight = " + rogHeight + ", defaultWidth = " + defaultWidth + ",size = " + size + ",resolutionSize =" + resolutionSize);
            return resolutionSize;
        }
        int min = rogWidth < rogHeight ? rogWidth : rogHeight;
        if (defaultWidth != min) {
            size = (int) ((((float) (size * min)) / ((float) defaultWidth)) + 0.5f);
        }
        int resolutionSize2 = getResolutionSize(size, token);
        Slog.d(TAG, "rogWidth = " + rogWidth + "rogHeight = " + rogHeight + ", defaultWidth = " + defaultWidth + ",size = " + size + ",resolutionSize =" + resolutionSize2);
        return resolutionSize2;
    }

    private int getResolutionSize(int inputSize, AppWindowToken token) {
        float resolutionRatio = -1.0f;
        try {
            IApsManager apsManager = HwFrameworkFactory.getApsManager();
            if (apsManager != null) {
                resolutionRatio = apsManager.getResolution(token.appPackageName);
            }
            if (0.0f >= resolutionRatio || resolutionRatio >= 1.0f) {
                return inputSize;
            }
            int size = (int) ((((float) inputSize) * resolutionRatio) + 0.5f);
            Slog.d(TAG, "getResolutionSize resolutionRatio " + resolutionRatio);
            return size;
        } catch (SecurityException e) {
            Slog.e(TAG, "getResolutionSize catch security exception");
            return inputSize;
        } catch (Exception e2) {
            Slog.e(TAG, "getResolutionSize catch exception");
            return inputSize;
        }
    }

    private boolean isSceneValidToSetCorner(WindowAnimationSpec windowAnimationSpec, AppWindowToken token) {
        if (windowAnimationSpec == null || token == null) {
            Slog.w(TAG, "setCrossAppTransitDynamicRoundCorner input is null");
            return false;
        } else if (!isFullScreenFreeformWindowBackground(token)) {
            return true;
        } else {
            Slog.d(TAG, "no need to set dynamic round corner.");
            return false;
        }
    }

    public void setCrossAppTransitDynamicRoundCorner(WindowAnimationSpec windowAnimationSpec, boolean isEnter, float targetRadius, boolean isLazyMode, AppWindowToken token) {
        boolean isNotchDisplayDisabled;
        boolean isWindowDynCorner;
        float changedRadius;
        float scaleSetting;
        float changedRadius2;
        String str;
        String str2;
        if (isSceneValidToSetCorner(windowAnimationSpec, token)) {
            WindowManagerPolicy policy = this.mIWmsInner.getPolicy();
            if (policy instanceof HwPhoneWindowManager) {
                isNotchDisplayDisabled = ((HwPhoneWindowManager) policy).isNotchDisplayDisabled();
            } else {
                isNotchDisplayDisabled = false;
            }
            WindowState win = token.findMainWindow();
            if (win == null || token.inMultiWindowMode()) {
                isWindowDynCorner = false;
            } else {
                int rotation = this.mIWmsInner.getService().getDefaultDisplayRotation();
                boolean isWindowDynCorner2 = false;
                boolean isStatusBarHide = !((win.mAttrs.flags & 1024) == 0 && (win.getSystemUiVisibility() & 4) == 0 && win.mAttrs.layoutInDisplayCutoutMode != 2) && !win.isWindowUsingNotch();
                if ((!win.isWindowUsingNotch() && (rotation == 3 || rotation == 1)) || isStatusBarHide) {
                    isWindowDynCorner2 = true;
                }
                isWindowDynCorner = isWindowDynCorner2;
            }
            if (isLazyMode || isNotchDisplayDisabled || !isEnableRoundCornerDisplay || token.inMultiWindowMode() || isWindowDynCorner) {
                float scaleSetting2 = this.mIWmsInner.getService().getTransitionAnimationScaleLocked();
                if (!isNotchDisplayDisabled || isLazyMode || token.inMultiWindowMode()) {
                    changedRadius = 0.0f;
                } else {
                    changedRadius = (float) this.mContext.getResources().getDimensionPixelSize(34472569);
                }
                if (!isEnter) {
                    Interpolator loadInterpolator = AnimationUtils.loadInterpolator(this.mContext, 34078893);
                    changedRadius2 = changedRadius;
                    str2 = TAG;
                    scaleSetting = scaleSetting2;
                    str = " ENABLE_ROUND_CORNER_DISPLAY ";
                    windowAnimationSpec.setDynamicCornerRadiusInfo(loadInterpolator, changedRadius, targetRadius, scaleSetting2, (long) DYN_CORNER_ANIMATION_TIME, 0);
                } else {
                    changedRadius2 = changedRadius;
                    str2 = TAG;
                    scaleSetting = scaleSetting2;
                    str = " ENABLE_ROUND_CORNER_DISPLAY ";
                }
                if (isEnter) {
                    windowAnimationSpec.setDynamicCornerRadiusInfo(AnimationUtils.loadInterpolator(this.mContext, 34078893), targetRadius, changedRadius2, scaleSetting, (long) DYN_CORNER_ANIMATION_TIME, 350);
                }
                Slog.d(str2, "setCrossAppTransitDynamicRoundCorner isEnter " + isEnter + " changedRadius " + changedRadius2 + " targetRadius " + targetRadius + " scaleSetting " + scaleSetting + " isNotchDisplayDisabled " + isNotchDisplayDisabled + str + isEnableRoundCornerDisplay);
                return;
            }
            Slog.d(TAG, "setCrossAppTransitDynamicRoundCorner isNotchDisplayDisabled " + isNotchDisplayDisabled + " ENABLE_ROUND_CORNER_DISPLAY " + isEnableRoundCornerDisplay + " isLazyMode " + isLazyMode);
        }
    }

    public Animation loadWallpaperAnimation(Bundle bundle) {
        return HwWallpaperAnimation.createAnimation(bundle);
    }

    private static int getIconHyalineWidth(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (Color.alpha(bitmap.getPixel(x, y)) != 0) {
                    return x + 1;
                }
            }
        }
        return 0;
    }

    public boolean isNeedLandAni() {
        return this.mIsNeedLandAni;
    }

    public void setLandAnimationInfo(boolean isNeedLandAni, String launchedPackage) {
        Slog.i(TAG, "setLandAnimationInfo : " + isNeedLandAni);
        if (this.mIsNeedLandAni != isNeedLandAni) {
            if (!IS_LAND_ANI_OPEN || IS_FOLDABLE || IS_TABLET) {
                Slog.i(TAG, "setLandAnimationInfo Not Supported");
                this.mIsNeedLandAni = false;
            } else if (isNeedLandAni) {
                this.mIsNeedLandAni = true;
                this.mLaunchedPackage = launchedPackage;
                this.mHwHandler.removeMessages(102);
                long timeout = (long) (this.mIWmsInner.getService().getTransitionAnimationScaleLocked() * 3000.0f);
                long timeout2 = timeout == 0 ? 3000 : timeout;
                this.mHwHandler.sendEmptyMessageDelayed(102, timeout2);
                Slog.i(TAG, "set launch origin and target for: " + launchedPackage + " with delay " + timeout2);
            } else {
                this.mIsNeedLandAni = false;
                this.mLaunchedPackage = launchedPackage;
                if (this.mHwHandler.hasMessages(102)) {
                    this.mHwHandler.removeMessages(102);
                    Slog.i(TAG, "reset launch origin and target from non-message!");
                }
                this.mEnterIconBitmap = null;
                this.mIWmsInner.getInputManagerCallback().thawInputDispatchingLw();
            }
        }
    }

    public Animation createLandOpenAnimation(boolean isEnter) {
        if (isEnter) {
            return createLandEnterAnimation();
        }
        return createLandExitAnimation();
    }

    private Animation createLandExitAnimation() {
        Slog.i(TAG, "createLandExitAnimation");
        Animation animation = new AlphaAnimation(1.0f, 1.0f);
        animation.setFillEnabled(true);
        animation.setFillBefore(true);
        animation.setFillAfter(true);
        animation.setDuration(LAND_ENTER_ANIM_DURATION);
        return animation;
    }

    private Animation createLandEnterAnimation() {
        DisplayContent displayContent = this.mIWmsInner.getDefaultDisplayContentLocked();
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        int finalWidth = displayInfo.logicalWidth;
        int finalHeight = displayInfo.logicalHeight;
        ScreenRotationAnimation screenRotationAnimation = this.mIWmsInner.getWindowAnimator().getScreenRotationAnimationLocked(displayContent.getDisplayId());
        int delta = 0;
        if (screenRotationAnimation != null) {
            delta = DisplayContent.deltaRotation(screenRotationAnimation.mCurRotation, screenRotationAnimation.mOriginalRotation);
        }
        Slog.i(TAG, "createLandEnterAnimation " + finalWidth + ", " + finalHeight + ", " + delta);
        return combineAnamations(new Animation[]{createAlphaAnimation(), createScaleAnimation(finalWidth, finalHeight), createClipAnimation(finalWidth, finalHeight), createTranslateAnimation(finalWidth, finalHeight, delta)});
    }

    private Animation createAlphaAnimation() {
        PhaseInterpolator phaseInterpolator = new PhaseInterpolator(LAND_ENTER_ANIM_ALPHA_PHASE_TIMES, LAND_ENTER_ANIM_ALPHA_PHASE_PROGRESSES, LAND_ENTER_ANIM_ALPHA_PHASE_INTERPOLATORS);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setInterpolator(phaseInterpolator);
        alphaAnimation.setDuration(LAND_ENTER_ANIM_DURATION);
        return alphaAnimation;
    }

    /* JADX WARN: Type inference failed for: r2v3, types: [com.android.server.wm.UnequalRatioClipRectAnimation, android.view.animation.Animation] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private Animation createClipAnimation(int finalWidth, int finalHeight) {
        Rect fromRect;
        if (finalWidth < finalHeight) {
            int offset = (finalHeight - finalWidth) >> 1;
            fromRect = new Rect(0, offset, finalWidth, offset + finalWidth);
        } else {
            int offset2 = (finalWidth - finalHeight) >> 1;
            fromRect = new Rect(offset2, 0, offset2 + finalHeight, finalHeight);
        }
        Rect toRect = new Rect(0, 0, finalWidth, finalHeight);
        Slog.i(TAG, "createLandEnterAnimation fromRect " + fromRect + ", toRect " + toRect);
        ?? unequalRatioClipRectAnimation = new UnequalRatioClipRectAnimation(fromRect, toRect);
        unequalRatioClipRectAnimation.initialize(finalWidth, finalHeight, finalWidth, finalHeight);
        unequalRatioClipRectAnimation.setDuration(LAND_ENTER_ANIM_DURATION);
        unequalRatioClipRectAnimation.setXInterpolator(LAND_LAUNCH_ANIM_Y_INTERPOLATOR);
        unequalRatioClipRectAnimation.setYInterpolator(LAND_LAUNCH_ANIM_X_INTERPOLATOR);
        return unequalRatioClipRectAnimation;
    }

    private Animation createScaleAnimation(int finalWidth, int finalHeight) {
        float iconWidth = (float) this.mEnterIconWidth;
        float fromScale = 1.0f;
        if (!(finalWidth == 0 || finalHeight == 0)) {
            fromScale = iconWidth / ((float) (finalWidth < finalHeight ? finalWidth : finalHeight));
        }
        Slog.d(TAG, "createLandEnterAnimation fromScale " + fromScale);
        ScaleAnimation scaleAnimation = new ScaleAnimation(fromScale, 1.0f, fromScale, 1.0f);
        scaleAnimation.setDuration(LAND_ENTER_ANIM_DURATION);
        scaleAnimation.setInterpolator(LAND_LAUNCH_ANIM_X_INTERPOLATOR);
        return scaleAnimation;
    }

    private Animation createTranslateAnimation(int finalWidth, int finalHeight, int delta) {
        float pivotYValue;
        float pivotXValue;
        float fromTransX;
        float fromTransY;
        float iconWidth = (float) this.mEnterIconWidth;
        float fromScale = 1.0f;
        if (!(finalWidth == 0 || finalHeight == 0)) {
            fromScale = iconWidth / ((float) (finalWidth < finalHeight ? finalWidth : finalHeight));
        }
        float pivotOriginalY = this.mEnterPivotY;
        float pivotOriginalX = this.mEnterPivotX;
        if (delta == 1) {
            pivotXValue = ((float) finalWidth) - pivotOriginalY;
            pivotYValue = pivotOriginalX;
        } else if (delta != 3) {
            pivotXValue = 0.0f;
            pivotYValue = 0.0f;
        } else {
            pivotXValue = pivotOriginalY;
            pivotYValue = ((float) finalHeight) - pivotOriginalX;
        }
        if (finalWidth < finalHeight) {
            fromTransX = pivotXValue - (iconWidth / 2.0f);
            fromTransY = pivotYValue - ((((float) finalHeight) * fromScale) / 2.0f);
        } else {
            fromTransX = pivotXValue - ((((float) finalWidth) * fromScale) / 2.0f);
            fromTransY = pivotYValue - (iconWidth / 2.0f);
        }
        TranslateAnimation translateAnimation = new TranslateAnimation(fromTransX, 0.0f, fromTransY, 0.0f);
        translateAnimation.setDuration(LAND_ENTER_ANIM_DURATION);
        translateAnimation.setInterpolator(LAND_LAUNCH_ANIM_X_INTERPOLATOR);
        return translateAnimation;
    }

    private Animation combineAnamations(Animation[] animations) {
        AnimationSet animationSet = new AnimationSet(false);
        for (Animation animation : animations) {
            if (animation != null) {
                animation.setFillEnabled(true);
                animation.setFillBefore(true);
                animation.setFillAfter(true);
                animationSet.addAnimation(animation);
            }
        }
        return animationSet;
    }

    private void createIconSurfaceControl(DisplayContent displayContent, SurfaceControl.Transaction transaction) {
        Slog.i(TAG, "createIconSurfaceControl");
        ScreenRotationAnimation screenRotationAnimation = this.mIWmsInner.getWindowAnimator().getScreenRotationAnimationLocked(displayContent.getDisplayId());
        this.mScreenRotationAnimation = screenRotationAnimation;
        if (screenRotationAnimation != null) {
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            this.mIconSurfaceControl = displayContent.makeOverlay().setName("IconSurfaceControl").setBufferSize(displayInfo.logicalWidth, displayInfo.logicalHeight).build();
            Surface surface = this.mSurfaceFactory.make();
            surface.copyFrom(this.mIconSurfaceControl);
            try {
                Canvas canvas = surface.lockCanvas(null);
                if (canvas != null) {
                    canvas.drawColor(-16777216);
                    surface.unlockCanvasAndPost(canvas);
                    transaction.setAlpha(this.mIconSurfaceControl, 0.0f);
                    transaction.show(this.mIconSurfaceControl);
                    transaction.apply();
                } else {
                    Slog.e(TAG, "createIconSurfaceControl canvas is null");
                }
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "createIconSurfaceControl illegal argument");
            } catch (Surface.OutOfResourcesException e2) {
                Slog.e(TAG, "createIconSurfaceControl out of resource");
            } catch (Throwable th) {
                surface.destroy();
                throw th;
            }
            surface.destroy();
            return;
        }
        Slog.e(TAG, "createIconSurfaceControl screenRotationAnimation is null");
    }

    private void adjustScreenshotLayer(DisplayContent displayContent, SurfaceControl.Transaction transaction) {
        Slog.d(TAG, "adjustScreenshotLayer begin");
        if (displayContent == null) {
            Slog.e(TAG, "adjustScreenshotLayer displayContent shoud not be null");
            return;
        }
        int displayId = displayContent.getDisplayId();
        SurfaceControl screenShot = null;
        IHwWindowManagerInner iHwWindowManagerInner = this.mIWmsInner;
        if (!(iHwWindowManagerInner == null || iHwWindowManagerInner.getWindowAnimator() == null || this.mIWmsInner.getWindowAnimator().getScreenRotationAnimationLocked(displayId) == null)) {
            screenShot = this.mIWmsInner.getWindowAnimator().getScreenRotationAnimationLocked(displayId).mSurfaceControl;
        }
        if (screenShot != null) {
            transaction.reparent(screenShot, displayContent.getWindowingLayer()).apply();
            this.mIconSurfaceControl.reparent(displayContent.getWindowingLayer());
            if (!(displayContent.mTaskStackContainers == null || displayContent.mTaskStackContainers.getTopStack() == null)) {
                this.mStackToDraw = getValidStackForDraw(displayContent);
                SurfaceControl topTaskStackSurface = this.mStackToDraw.mSurfaceControl;
                if (topTaskStackSurface != null) {
                    transaction.setRelativeLayer(this.mIconSurfaceControl, topTaskStackSurface, -1);
                    Slog.d(TAG, "adjustScreenshotLayer setRelativeLayer");
                }
            }
        }
        Slog.d(TAG, "adjustScreenshotLayer end");
    }

    public void startLandOpenAnimation() {
        TaskStack taskStack;
        Slog.d(TAG, "startLandOpenAnimation begin");
        DisplayContent displayContent = this.mIWmsInner.getDefaultDisplayContentLocked();
        SurfaceControl.Transaction transaction = this.mTransactionFactory.make();
        this.mIWmsInner.getInputManagerCallback().freezeInputDispatchingLw();
        createIconSurfaceControl(displayContent, transaction);
        adjustScreenshotLayer(displayContent, transaction);
        updateAppRealRect();
        setNavigationBarVisibility(transaction, true);
        if (this.mTopTaskSurfaceControlLeash != null || (taskStack = this.mStackToDraw) == null) {
            Slog.e(TAG, "mTopTaskSurfaceControlLeash should not be null");
        } else {
            SurfaceControl topStackParentSurfaceControl = taskStack.getParentSurfaceControl();
            SurfaceControl topStackSurfaceControl = this.mStackToDraw.getSurfaceControl();
            this.mTopTaskSurfaceControlLeash = this.mStackToDraw.makeSurface().setName("Leash-for-landopen").setParent(topStackParentSurfaceControl).build();
            transaction.reparent(topStackSurfaceControl, this.mTopTaskSurfaceControlLeash);
            transaction.show(this.mTopTaskSurfaceControlLeash);
            transaction.setAlpha(this.mTopTaskSurfaceControlLeash, 0.0f);
            transaction.apply(true);
            Slog.d(TAG, "startLandOpenAnimation create animation leash for " + topStackSurfaceControl);
        }
        rotateEnterIcon();
        UniPerf.getInstance().uniPerfEvent(4105, "", new int[]{-1});
        Slog.d(TAG, "startLandOpenAnimation end");
    }

    private void updateAppRealRect() {
        this.mLandOpenAppDisplayRect = null;
        TaskStack taskStack = this.mStackToDraw;
        if (taskStack != null) {
            Task topTask = taskStack.getTopChild();
            if (topTask == null) {
                Slog.e(TAG, "updateAppRealRect topTask is null");
                return;
            }
            AppWindowToken topApp = topTask.getTopFullscreenAppToken();
            if (topApp == null) {
                Slog.e(TAG, "updateAppRealRect topApp is null");
                return;
            }
            WindowState win = topApp.findMainWindow();
            if (win == null) {
                Slog.e(TAG, "updateAppRealRect win is null");
                return;
            }
            this.mLandOpenAppDisplayRect = win.getDisplayFrameLw();
            Slog.i(TAG, "updateAppRealRect mLandOpenAppDisplayRect " + this.mLandOpenAppDisplayRect);
        }
    }

    private void rotateEnterIcon() {
        ScreenRotationAnimation screenRotationAnimation = this.mIWmsInner.getWindowAnimator().getScreenRotationAnimationLocked(this.mIWmsInner.getDefaultDisplayContentLocked().getDisplayId());
        if (screenRotationAnimation == null) {
            Slog.e(TAG, "rotateEnterIcon screenRotationAnimation should not be null");
            return;
        }
        int iconRotate = 0;
        int screenRotate = DisplayContent.deltaRotation(screenRotationAnimation.mCurRotation, screenRotationAnimation.mOriginalRotation);
        if (screenRotate == 1) {
            iconRotate = ROTATION_DEGREE_270;
        } else if (screenRotate == 3) {
            iconRotate = ROTATION_DEGREE_90;
        }
        if (this.mEnterIconBitmap != null) {
            Bitmap bitmap = this.mEnterIconBitmap;
            Matrix bitmapMatrix = new Matrix();
            bitmapMatrix.setRotate((float) (-iconRotate));
            this.mEnterIconBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getHeight(), bitmap.getWidth(), bitmapMatrix, false);
            this.mIconByteBuffer = ByteBuffer.allocate(this.mEnterIconBitmap.getRowBytes() * this.mEnterIconBitmap.getHeight());
            this.mEnterIconBitmap.copyPixelsToBuffer(this.mIconByteBuffer);
            ColorSpace colorSpace = this.mEnterIconBitmap.getColorSpace();
            if (colorSpace == null) {
                colorSpace = ColorSpace.get(ColorSpace.Named.SRGB);
            }
            int colorSpaceId = colorSpace.getId();
            Slog.d(TAG, "colorSpace = " + colorSpace);
            this.mIconSurfaceControl.setWindowIconInfo(1, this.mEnterIconBitmap.getWidth(), this.mEnterIconBitmap.getHeight(), this.mIconByteBuffer.array(), this.mIconByteBuffer.capacity(), this.mEnterIconBitmap.getWidth(), this.mEnterIconBitmap.getHeight(), colorSpaceId);
            this.mIsSurfaceIconSet = true;
            Slog.d(TAG, "startLandOpenAnimation update rotation of mEnterIconBitmap");
            return;
        }
        Slog.e(TAG, "mEnterIconBitmap should not be null");
    }

    public void finishLandOpenAnimation() {
        Slog.d(TAG, "finishLandOpenAnimation begin");
        showAboveAppWindowsContainers();
        if (this.mIsNeedLandAni) {
            this.mIWmsInner.getDefaultDisplayContentLocked();
            if (!(this.mTopTaskSurfaceControlLeash == null || this.mStackToDraw == null)) {
                SurfaceControl.Transaction transaction = this.mTransactionFactory.make();
                SurfaceControl appSurface = this.mStackToDraw.getSurfaceControl();
                if (appSurface != null && appSurface.isValid()) {
                    transaction.reparent(appSurface, this.mStackToDraw.getParentSurfaceControl());
                }
                if (this.mTopTaskSurfaceControlLeash.isValid()) {
                    transaction.remove(this.mTopTaskSurfaceControlLeash);
                }
                transaction.apply(true);
                this.mTopTaskSurfaceControlLeash = null;
            }
            if (this.mIconSurfaceControl != null) {
                Slog.d(TAG, "mIconSurfaceControl removed");
                this.mIconSurfaceControl.remove();
                this.mIconSurfaceControl = null;
            }
            if (this.mEnterIconBitmap != null) {
                this.mEnterIconBitmap = null;
            }
            if (this.mIconByteBuffer != null) {
                this.mIconByteBuffer = null;
            }
            if (this.mScreenRotationAnimation != null) {
                this.mScreenRotationAnimation = null;
            }
            setLandAnimationInfo(false, null);
            this.mStackToDraw = null;
            this.mIsScreenshotLayerAdjusted = false;
            Slog.d(TAG, "finishLandOpenAnimation end");
        }
    }

    public void applyLandOpenAnimation() {
        DisplayContent displayContent = this.mIWmsInner.getDefaultDisplayContentLocked();
        if (displayContent == null) {
            Slog.e(TAG, "applyLandOpenAnimation displayContent is null");
        } else if (this.mEnterIconBitmap == null) {
            Slog.e(TAG, "applyLandOpenAnimation mEnterIconBitmap is null");
        } else if (this.mIconSurfaceControl == null) {
            Slog.e(TAG, "applyLandOpenAnimation mIconSurfaceControl is null");
        } else if (this.mIconByteBuffer == null) {
            Slog.e(TAG, "applyLandOpenAnimation mIconByteBuffer is null");
        } else if (this.mTopTaskSurfaceControlLeash == null) {
            Slog.e(TAG, "applyLandOpenAnimation mTopTaskSurfaceControlLeash is null");
        } else {
            ScreenRotationAnimation screenRotationAnimation = this.mIWmsInner.getWindowAnimator().getScreenRotationAnimationLocked(displayContent.mDisplayId);
            if (screenRotationAnimation == null) {
                Slog.e(TAG, "applyLandOpenAnimation screenRotationAnimation is null");
            } else if (!screenRotationAnimation.mAnimRunning) {
                Slog.i(TAG, "applyLandOpenAnimation screenRotationAnimation is not running");
            } else {
                clearIconIfNeed(screenRotationAnimation.getEnterTransformation().getAlpha());
                applyAnimation(displayContent, screenRotationAnimation);
            }
        }
    }

    private void clearIconIfNeed(float alpha) {
        if (this.mIsSurfaceIconSet && 1.0f - alpha < 1.0E-6f) {
            Slog.d(TAG, "clearIconIfNeed");
            this.mIconSurfaceControl.setWindowIconInfo(0, 0, 0, new byte[1], 1, 0, 0, 0);
            this.mIsSurfaceIconSet = false;
        }
    }

    private void applyAnimation(DisplayContent displayContent, ScreenRotationAnimation screenRotationAnimation) {
        SurfaceControl.Transaction transaction = displayContent.getPendingTransaction();
        if (!(this.mIsScreenshotLayerAdjusted || screenRotationAnimation == null || this.mStackToDraw == null)) {
            SurfaceControl screenShot = screenRotationAnimation.mSurfaceControl;
            SurfaceControl topTaskStackSurface = this.mStackToDraw.mSurfaceControl;
            if (screenShot != null && screenShot.isValid() && topTaskStackSurface != null && topTaskStackSurface.isValid()) {
                transaction.setRelativeLayer(screenShot, topTaskStackSurface, -2);
                this.mIsScreenshotLayerAdjusted = true;
            }
        }
        float[] tmpMat = new float[9];
        screenRotationAnimation.getEnterTransformation().getMatrix().getValues(tmpMat);
        transaction.setMatrix(this.mTopTaskSurfaceControlLeash, tmpMat[0], tmpMat[3], tmpMat[1], tmpMat[4]);
        transaction.setPosition(this.mTopTaskSurfaceControlLeash, tmpMat[2], tmpMat[5]);
        float alpha = screenRotationAnimation.getEnterTransformation().getAlpha();
        transaction.setAlpha(this.mTopTaskSurfaceControlLeash, alpha);
        transaction.setMatrix(this.mIconSurfaceControl, tmpMat[0], tmpMat[3], tmpMat[1], tmpMat[4]);
        transaction.setPosition(this.mIconSurfaceControl, tmpMat[2], tmpMat[5]);
        transaction.setAlpha(this.mIconSurfaceControl, 0.0f);
        if (1.0f - tmpMat[0] > 1.0E-6f || 1.0f - tmpMat[4] > 1.0E-6f) {
            Rect cropRect = screenRotationAnimation.getEnterTransformation().getClipRect();
            Rect rect = this.mLandOpenAppDisplayRect;
            if (rect != null) {
                cropRect.intersect(rect);
            }
            float radius = 230.0f;
            if (alpha >= 1.0f) {
                radius = 230.0f - (tmpMat[0] * 180.0f);
            }
            transaction.setWindowCrop(this.mTopTaskSurfaceControlLeash, cropRect);
            transaction.setCornerRadius(this.mTopTaskSurfaceControlLeash, radius);
            transaction.setWindowCrop(this.mIconSurfaceControl, cropRect);
            transaction.setCornerRadius(this.mIconSurfaceControl, radius);
        } else {
            transaction.setWindowCrop(this.mTopTaskSurfaceControlLeash, new Rect(0, 0, -1, -1));
            transaction.setCornerRadius(this.mTopTaskSurfaceControlLeash, 0.0f);
            transaction.setWindowCrop(this.mIconSurfaceControl, new Rect(0, 0, -1, -1));
            transaction.setCornerRadius(this.mIconSurfaceControl, 0.0f);
        }
        Slog.d(TAG, "applyLandOpenAnimation alpha " + alpha + " scale " + tmpMat[0]);
        SurfaceControl.mergeToGlobalTransaction(transaction);
    }

    private TaskStack getValidStackForDraw(DisplayContent displayContent) {
        TaskStack targetStack = null;
        int i = displayContent.mTaskStackContainers.getChildCount() - 1;
        while (true) {
            if (i >= 0) {
                ActivityStack stack = displayContent.mTaskStackContainers.getChildAt(i).mActivityStack;
                if (stack != null && this.mLaunchedPackage != null && stack.getTopActivity() != null && this.mLaunchedPackage.equals(stack.getTopActivity().packageName) && !isNeedHide(stack.getWindowingMode())) {
                    targetStack = displayContent.mTaskStackContainers.getChildAt(i);
                    break;
                }
                i--;
            } else {
                break;
            }
        }
        if (targetStack == null) {
            return displayContent.mTaskStackContainers.getTopStack();
        }
        return targetStack;
    }

    public void hideAboveAppWindowsContainers() {
        SurfaceControl freeStackSurfaceControl;
        Slog.i(TAG, "hideAboveAppWindowsContainers");
        SurfaceControl.Transaction transaction = this.mIWmsInner.getService().mTransactionFactory.make();
        boolean isNeedHideAbove = setAboveAppWindowsVisibilityAdhoc(transaction, false);
        for (int i = this.mIWmsInner.getService().mAtmService.mRootActivityContainer.getChildCount() - 1; i >= 0; i--) {
            ActivityDisplay display = this.mIWmsInner.getService().mAtmService.mRootActivityContainer.getChildAt(i);
            for (int j = display.mStacks.size() - 1; j >= 0; j--) {
                ActivityStack freeFormStack = (ActivityStack) display.mStacks.get(j);
                if (freeFormStack != null && isNeedHide(freeFormStack.getWindowingMode()) && freeFormStack.isTopActivityVisible() && (freeStackSurfaceControl = freeFormStack.mTaskStack.getSurfaceControl()) != null && freeStackSurfaceControl.isValid()) {
                    this.mMultiWinSurface.add(freeStackSurfaceControl);
                    Slog.i(TAG, "hide freeformsurfacecontrol" + freeStackSurfaceControl);
                    isNeedHideAbove = true;
                    transaction.hide(freeStackSurfaceControl);
                }
            }
        }
        if (isNeedHideAbove) {
            transaction.apply(true);
            Slog.i(TAG, "hideAboveAppWindowsContainers finish");
        }
        this.mIsAboveWinHidden = true;
    }

    private boolean isNeedHide(int windowingMode) {
        return windowingMode == 5 || windowingMode == 2 || windowingMode == 10 || windowingMode == 102;
    }

    private void showAboveAppWindowsContainers() {
        if (this.mIsAboveWinHidden) {
            Slog.i(TAG, "showAboveAppWindowsContainers");
            SurfaceControl.Transaction transaction = this.mIWmsInner.getService().mTransactionFactory.make();
            boolean isNeedShow = setAboveAppWindowsVisibilityAdhoc(transaction, true);
            ArrayList<SurfaceControl> arrayList = this.mMultiWinSurface;
            if (arrayList != null && arrayList.size() > 0) {
                for (int i = 0; i < this.mMultiWinSurface.size(); i++) {
                    SurfaceControl surfaceToShow = this.mMultiWinSurface.get(i);
                    if (surfaceToShow != null && surfaceToShow.isValid()) {
                        isNeedShow = true;
                        transaction.show(surfaceToShow);
                    }
                }
                this.mMultiWinSurface.clear();
            }
            if (isNeedShow) {
                transaction.apply(true);
                Slog.i(TAG, "showAboveAppWindowsContainers finish");
            }
            this.mIsAboveWinHidden = false;
        }
    }

    private boolean setAboveAppWindowsVisibilityAdhoc(SurfaceControl.Transaction transaction, boolean isVisible) {
        return setAboveAppWindowsVisibility(transaction, isVisible, $$Lambda$HwWindowManagerServiceEx$p7RQYD_tYbGLTjI1T6Rbz6_RPA.INSTANCE);
    }

    static /* synthetic */ boolean lambda$setAboveAppWindowsVisibilityAdhoc$5(WindowState ws) {
        if (Arrays.asList(ANDROID_PACKAGE, DOCK_PACKAGE).contains(ws.getAttrs().packageName)) {
            return false;
        }
        List<Integer> validTypeList = Arrays.asList(Integer.valueOf((int) HwArbitrationDEFS.MSG_RECOVERY_FLAG_BY_WIFI_RX_BYTES), Integer.valueOf((int) HwArbitrationDEFS.MSG_MPLINK_ERROR));
        if (!"com.android.systemui".equals(ws.getAttrs().packageName) || validTypeList.contains(Integer.valueOf(ws.getAttrs().type))) {
            return true;
        }
        return false;
    }

    private void setNavigationBarVisibility(SurfaceControl.Transaction transaction, boolean isVisible) {
        setAboveAppWindowsVisibility(transaction, isVisible, $$Lambda$HwWindowManagerServiceEx$DBr5CkAyFAHMlwbknUz2h5e1o.INSTANCE);
    }

    static /* synthetic */ boolean lambda$setNavigationBarVisibility$6(WindowState ws) {
        return ws.getAttrs().type == 2019;
    }

    private boolean setAboveAppWindowsVisibility(SurfaceControl.Transaction transaction, boolean isVisible, Predicate<WindowState> condition) {
        DisplayContent displayContent = this.mIWmsInner.getDefaultDisplayContentLocked();
        boolean isNeedChange = false;
        for (int i = displayContent.mAboveAppWindowsContainers.getChildCount() - 1; i >= 0; i--) {
            WindowToken windowToken = displayContent.mAboveAppWindowsContainers.getChildAt(i);
            for (int j = windowToken.getChildCount() - 1; j >= 0; j--) {
                WindowState windowState = (WindowState) windowToken.getChildAt(j);
                if (!condition.test(windowState)) {
                    Slog.i(TAG, "setAboveAppWindowsVisibility keep " + windowState);
                } else {
                    Slog.i(TAG, "setAboveAppWindowsVisibility change " + windowState + " -> " + isVisible);
                    isNeedChange = true;
                    transaction.setVisibility(windowState.mSurfaceControl, isVisible);
                }
            }
        }
        return isNeedChange;
    }

    public void setInputBlock(boolean isBlock) {
        if (isBlock) {
            WindowState win = this.mIWmsInner.getDefaultDisplayContentLocked().getDisplayPolicy().getFocusedWindow();
            if (win != null) {
                if (HwWmConstants.isLauncherPkgName(win.getAttrs().packageName) && win.getAttrs().rotationAnimation == 3) {
                    Slog.i(TAG, "setInputBlock true");
                    this.mHwHandler.removeMessages(103);
                    this.mHwHandler.sendEmptyMessageDelayed(103, 500);
                    this.mIWmsInner.getService().mInputManagerCallback.freezeInputDispatchingLw();
                    return;
                }
                return;
            }
            return;
        }
        if (this.mHwHandler.hasMessages(103)) {
            this.mHwHandler.removeMessages(103);
        }
        Slog.i(TAG, "setInputBlock false");
        this.mIWmsInner.getService().mInputManagerCallback.thawInputDispatchingLw();
    }

    public Point getOriginPointForLazyMode(float scale, int lazyModeType) {
        float f;
        float f2;
        float newScale = scale;
        if (scale > 1.0f || scale < 0.0f) {
            newScale = 1.0f;
        }
        DisplayInfo displayInfo = new DisplayInfo();
        this.mIWmsInner.getDefaultDisplayContentLocked().getDisplay().getDisplayInfo(displayInfo);
        int offsetX = HwDisplaySizeUtil.hasSideInScreen() ? HwDisplaySizeUtil.getInstance(this.mIWmsInner.getService()).getSafeSideWidth() : 0;
        float pendingX = 0.0f;
        float pendingY = 0.0f;
        int mScreenHeight = displayInfo.logicalHeight;
        int mScreenWidth = displayInfo.logicalWidth;
        if (lazyModeType == 1) {
            pendingY = ((float) mScreenHeight) * (1.0f - newScale);
            pendingX = ((double) Math.abs(1.0f - newScale)) > 1.0E-4d ? (pendingY / (((float) mScreenHeight) * 0.25f)) * ((float) offsetX) : 0.0f;
        } else if (lazyModeType == 2) {
            float lazyWidth = ((float) mScreenWidth) * 0.25f;
            if (((double) Math.abs(1.0f - newScale)) > 1.0E-4d) {
                f2 = ((float) mScreenWidth) * (1.0f - newScale);
                f = 1.0f - (((float) offsetX) / lazyWidth);
            } else {
                f2 = (float) mScreenWidth;
                f = 1.0f - newScale;
            }
            pendingX = f2 * f;
            pendingY = ((float) mScreenHeight) * (1.0f - newScale);
        }
        Point point = new Point();
        point.set((int) pendingX, (int) pendingY);
        return point;
    }

    private int getLazyModeInputScaleSide(int mode, boolean isHintShowing) {
        if (isHintShowing) {
            return 0;
        }
        if (mode == 1) {
            return 2;
        }
        if (mode == 2) {
            return 3;
        }
        return 0;
    }

    public List<String> getCarFocusList() {
        return this.mFocusList;
    }

    public void travelsalPCWindowsAndFindOne(int direction) {
        ArrayList<WindowState> windows = new ArrayList<>();
        DisplayContent displayContent = this.mIWmsInner.getRoot().getDisplayContent(HwPCUtils.getPCDisplayID());
        synchronized (this.mIWmsInner.getGlobalLock()) {
            this.mIWmsInner.getRoot().forAllWindows(new Consumer(windows) {
                /* class com.android.server.wm.$$Lambda$HwWindowManagerServiceEx$u5pAQG0oByMXkKJr0dK8_2_QrxY */
                private final /* synthetic */ ArrayList f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    HwWindowManagerServiceEx.this.lambda$travelsalPCWindowsAndFindOne$7$HwWindowManagerServiceEx(this.f$1, (WindowState) obj);
                }
            }, true);
        }
        if (windows.size() <= 0) {
            HwPCUtils.log(TAG, "can not find focus window on ext display");
            return;
        }
        Iterator<WindowState> it = windows.iterator();
        WindowState currentFocusdWindow = null;
        while (it.hasNext()) {
            WindowState win = it.next();
            if (win.isFocused()) {
                currentFocusdWindow = win;
            }
        }
        if (currentFocusdWindow == null || direction == 4) {
            HwPCUtils.log(TAG, "there is no focus window on ext display");
            synchronized (this.mIWmsInner.getGlobalLock()) {
                WindowContainer parent = displayContent.getParent();
                if (!(parent == null || parent.getTopChild() == displayContent)) {
                    parent.positionChildAt(Integer.MAX_VALUE, displayContent, true);
                }
            }
            return;
        }
        WindowState nextFocusdWindow = getNearestWindowByDirection(windows, direction, currentFocusdWindow);
        if (nextFocusdWindow != null) {
            HwPCUtils.log(TAG, "nextFocusdWindow: " + nextFocusdWindow.toString() + ", currentFocus: " + currentFocusdWindow.toString());
            int taskid = -1;
            filterFocusWindow(nextFocusdWindow, windows);
            if (nextFocusdWindow.getStack() != null) {
                taskid = nextFocusdWindow.getTask().mTaskId;
            }
            if (taskid != -1) {
                this.mIWmsInner.getService().mH.sendMessageDelayed(this.mIWmsInner.getService().mH.obtainMessage(104, taskid, 0), 500);
            } else {
                synchronized (this.mIWmsInner.getGlobalLock()) {
                    displayContent.mLastFocus = null;
                    displayContent.mCurrentFocus = currentFocusdWindow;
                    this.mIWmsInner.getService().updateFocusedWindowLocked(0, true);
                    currentFocusdWindow.reportFocusChangedSerialized(false, true);
                    nextFocusdWindow.reportFocusChangedSerialized(true, true);
                }
            }
            return;
        }
        HwPCUtils.log(TAG, "nextfocuswindow null do nothing");
    }

    public /* synthetic */ void lambda$travelsalPCWindowsAndFindOne$7$HwWindowManagerServiceEx(ArrayList windows, WindowState w) {
        if (HwPCUtils.isValidExtDisplayId(w.getDisplayId()) && w.isVisible() && isWindowCanFocus(w)) {
            windows.add(w);
        }
    }

    private WindowState getNearestWindowByDirection(List<WindowState> windows, int direction, WindowState currentFocus) {
        int interval;
        HwWindowManagerServiceEx hwWindowManagerServiceEx = this;
        int i = direction;
        Rect focusRect = currentFocus.getFrameLw();
        Point focusPoint = hwWindowManagerServiceEx.getCenterPoint(focusRect);
        if (currentFocus.getWindowTag().equals("CarLauncher")) {
            hwWindowManagerServiceEx.reCalculateLauncherPoint(currentFocus, focusPoint);
        }
        HwPCUtils.log(TAG, "getNearestWindowByDirection focuswindow bounds :" + focusRect + ", point:" + focusPoint);
        int minIndex = -1;
        int minInterval = 0;
        int minSubInterval = 0;
        boolean isIntervalFound = false;
        int i2 = 0;
        while (i2 < windows.size()) {
            WindowState win = windows.get(i2);
            if (currentFocus.getWindowTag().equals(win.getWindowTag())) {
                HwPCUtils.log(TAG, "skip current focus");
            } else if (currentFocus.getVisibleFrameLw().equals(win.getVisibleFrameLw())) {
                HwPCUtils.log(TAG, "skip overlay window");
            } else {
                Rect newWindowRect = win.getFrameLw();
                Point newWidowPoint = hwWindowManagerServiceEx.getCenterPoint(newWindowRect);
                if (win.getWindowTag().equals("CarLauncher")) {
                    hwWindowManagerServiceEx.reCalculateLauncherPoint(win, newWidowPoint);
                }
                HwPCUtils.log(TAG, "window rect:" + newWindowRect + ", point:" + newWidowPoint);
                int subInterval = -1;
                if (i == 0) {
                    interval = focusPoint.y - newWidowPoint.y;
                    subInterval = focusPoint.x - newWidowPoint.x;
                    if (interval <= 0) {
                        HwPCUtils.log(TAG, "up direction skip down");
                    }
                } else if (i == 1) {
                    interval = focusPoint.x - newWidowPoint.x;
                    subInterval = focusPoint.y - newWidowPoint.y;
                    if (interval <= 0) {
                        HwPCUtils.log(TAG, "left direction skip right");
                    }
                } else if (i == 2) {
                    interval = focusPoint.x - newWidowPoint.x;
                    subInterval = focusPoint.y - newWidowPoint.y;
                    if (interval >= 0) {
                        HwPCUtils.log(TAG, "right direction skip left");
                    }
                } else if (i != 3) {
                    HwPCUtils.log(TAG, "invalid direction");
                    interval = -1;
                } else {
                    interval = focusPoint.y - newWidowPoint.y;
                    subInterval = focusPoint.x - newWidowPoint.x;
                    if (interval >= 0) {
                        HwPCUtils.log(TAG, "down direction skip up");
                    }
                }
                if (!isIntervalFound) {
                    minInterval = interval;
                    minSubInterval = subInterval;
                    isIntervalFound = true;
                    minIndex = i2;
                }
                if (Math.abs(minInterval) > Math.abs(interval)) {
                    minIndex = i2;
                    minInterval = interval;
                    minSubInterval = subInterval;
                } else if (Math.abs(minInterval) == Math.abs(interval) && Math.abs(minSubInterval) > Math.abs(subInterval)) {
                    minIndex = i2;
                    minInterval = interval;
                    minSubInterval = subInterval;
                }
            }
            i2++;
            hwWindowManagerServiceEx = this;
            i = direction;
            focusRect = focusRect;
        }
        if (minIndex < 0) {
            HwPCUtils.log(TAG, "cannot find right window");
            return null;
        }
        WindowState ret = windows.get(minIndex);
        HwPCUtils.log(TAG, "nextfocusWIndow:" + ret);
        return ret;
    }

    private Point getCenterPoint(Rect target) {
        return new Point((target.left + target.right) / 2, (target.bottom + target.top) / 2);
    }

    private void filterFocusWindow(WindowState focus, List<WindowState> wins) {
        if (!this.mFocusList.isEmpty()) {
            HwPCUtils.log(TAG, "filterFocusWindow clear list");
            this.mFocusList.clear();
        }
        for (WindowState win : wins) {
            String windowName = (String) win.getAttrs().getTitle();
            if (!windowName.equals((String) focus.getAttrs().getTitle())) {
                HwPCUtils.log(TAG, "filterFocusWindow add:" + windowName);
                this.mFocusList.add(windowName);
            } else {
                HwPCUtils.log(TAG, "filterFocusWindow skip:");
                return;
            }
        }
    }

    /* JADX INFO: Multiple debug info for r4v0 int: [D('externalContext' android.content.Context), D('offsetPixel' int)] */
    private void reCalculateLauncherPoint(WindowState win, Point focusPoint) {
        new Rect(win.getFrameLw());
        DisplayContent dc = win.getDisplayContent();
        int carDockWidth = 0;
        DisplayMetrics externalDisplayMetrics = new DisplayMetrics();
        if (dc != null) {
            dc.getDisplay().getMetrics(externalDisplayMetrics);
            carDockWidth = this.mContext.createDisplayContext(dc.mDisplay).getResources().getDimensionPixelSize(34472522);
        }
        int offsetPixel = carDockWidth / 2;
        if (Float.compare(((float) Math.round((((float) externalDisplayMetrics.widthPixels) / ((float) externalDisplayMetrics.heightPixels)) * DEFAULT_DECIMAL_RATIO)) / DEFAULT_DECIMAL_RATIO, DEFAULT_SCREEN_RATIO) >= 0) {
            focusPoint.offset(offsetPixel, 0);
        } else {
            focusPoint.offset(0, -offsetPixel);
        }
    }

    public boolean isAppNeedExpand(String pkgName) {
        return HwDisplaySideRegionConfig.getInstance().isExtendApp(pkgName);
    }

    public void setAboveAppWindowsContainersVisible(boolean isVisible) {
        this.mHwHandler.removeMessages(200);
        boolean isCallerFromHome = this.mIWmsInner.getService().mAtmService.mRecentTasks.isCallerRecents(Binder.getCallingUid());
        boolean isCallerFromSystem = isCallingFromSystem();
        if (isCallerFromHome || isCallerFromSystem) {
            synchronized (this.mIWmsInner.getService().mGlobalLock) {
                DisplayContent dc = this.mIWmsInner.getDefaultDisplayContentLocked();
                if (dc != null) {
                    if (dc.mAboveAppWindowsContainers.getSurfaceControl() != null) {
                        if (!isVisible) {
                            Message msg = Message.obtain();
                            msg.what = 200;
                            this.mHwHandler.sendMessageDelayed(msg, 2000);
                        }
                        this.mAlphaTransaction.setAlpha(dc.mAboveAppWindowsContainers.getSurfaceControl(), isVisible ? 1.0f : 0.0f).apply();
                    }
                }
            }
        }
    }

    private boolean isCallingFromSystem() {
        int uid = UserHandle.getAppId(Binder.getCallingUid());
        if (uid == 1000) {
            return true;
        }
        Slog.e(TAG, "Process Permission error! uid:" + uid);
        return false;
    }

    public void setClipRectDynamicRoundCornerIfNeeded(WindowAnimationSpec windowAnimationSpec, AppWindowToken appWindowToken, int transit, boolean isEnter) {
        if (windowAnimationSpec == null || appWindowToken == null || !this.mIsClipRectDynamicCornerNeeded) {
            this.mIsClipRectDynamicCornerNeeded = false;
            return;
        }
        Slog.i(TAG, "setClipRectDynamicRoundCornerIfNeeded, mIntelligentCustomCornerRadius = " + this.mIntelligentCustomCornerRadius);
        float scaleSetting = this.mIWmsInner.getService().getTransitionAnimationScaleLocked();
        if (transit == 12) {
            windowAnimationSpec.setDynamicCornerRadiusInfo(FAST_IN_SLOW_OUT, this.mIntelligentCustomCornerRadius, getCrossAppTransitAnimRoundCornerRadius(appWindowToken), scaleSetting, (long) CARD_ANIMATION_DURATION, 0);
        } else if (transit == 13 && !isEnter) {
            windowAnimationSpec.setDynamicCornerRadiusInfo(FAST_IN_SLOW_OUT, getCrossAppTransitAnimRoundCornerRadius(appWindowToken), this.mIntelligentCustomCornerRadius, scaleSetting, (long) CARD_ANIMATION_DURATION, 0);
        }
        this.mIntelligentCustomCornerRadius = 0.0f;
        this.mIsClipRectDynamicCornerNeeded = false;
    }

    public Animation createCardClipRevealAnimation(Animation defaultAnimation, boolean isEnter, int transit, AppWindowToken appWindowToken, Rect frame) {
        if (appWindowToken == null || frame == null || appWindowToken.getDisplayContent() == null || !HwActivityManager.IS_PHONE) {
            return defaultAnimation;
        }
        if (transit == 12) {
            return loadTransitWallPaperCloseAnimation(defaultAnimation, isEnter, appWindowToken, frame);
        }
        if (transit != 13) {
            return defaultAnimation;
        }
        return loadTransitWallPaperOpenAnimation(defaultAnimation, isEnter, appWindowToken, frame);
    }

    public void notifyWindowANR(WindowState windowState) {
        if (windowState != null) {
            CharSequence title = windowState.mAttrs.getTitle();
            if ("GestureNavLeft".equals(title) || "GestureNavRight".equals(title) || "GestureNavBottom".equals(title)) {
                ((DefaultGestureNavManager) LocalServices.getService(DefaultGestureNavManager.class)).notifyANR(title);
            }
        }
    }

    private Animation loadTransitWallPaperOpenAnimation(Animation defaultAnimation, boolean isEnter, AppWindowToken appWindowToken, Rect frame) {
        Rect rect;
        DisplayContent dc = appWindowToken.getDisplayContent();
        if (!isEnter && isClosingAppsContains(appWindowToken, dc) && (rect = this.mIntelligentCardPosition) != null && !rect.isEmpty()) {
            this.mIsNeedDefaultAnimation = true;
            Animation customAnim = HwAppTransitionImpl.createCardClipRevealAniamtion(false, getAnimationBounds(appWindowToken, frame, false, null), this.mIntelligentCardPosition, 1.0f, 0.0f);
            Slog.i(TAG, "createCardClipRevealAnimation: CardRect = " + this.mIntelligentCardPosition);
            this.mIntelligentCardPosition = null;
            if (customAnim == null) {
                return defaultAnimation;
            }
            this.mIsClipRectDynamicCornerNeeded = true;
            return customAnim;
        } else if (!isEnter || !isLauncherOpen(appWindowToken, 13, dc) || !this.mIsNeedDefaultAnimation) {
            return defaultAnimation;
        } else {
            Animation animation = createDefaultAnimationForCard(CARD_ANIMATION_DURATION);
            this.mIsNeedDefaultAnimation = false;
            return animation;
        }
    }

    private Animation loadTransitWallPaperCloseAnimation(Animation defaultAnimation, boolean isEnter, AppWindowToken appWindowToken, Rect frame) {
        DisplayContent dc = appWindowToken.getDisplayContent();
        AppWindowToken topOpeningApp = getTopApp(dc.mOpeningApps, false);
        Rect rect = this.mIntelligentCardPosition;
        if (rect == null || rect.isEmpty()) {
            return defaultAnimation;
        }
        if (isEnter && topOpeningApp != null) {
            Animation customAnim = HwAppTransitionImpl.createCardClipRevealAniamtion(true, this.mIntelligentCardPosition, getAnimationBounds(appWindowToken, frame, false, null), 0.0f, 1.0f);
            Slog.i(TAG, "createCardClipRevealAnimation: CardRect = " + this.mIntelligentCardPosition);
            this.mIntelligentCardPosition = null;
            if (customAnim == null) {
                return defaultAnimation;
            }
            this.mIsClipRectDynamicCornerNeeded = true;
            return customAnim;
        } else if (isEnter || !isClosingAppsContains(appWindowToken, dc) || !isAppLauncher(appWindowToken)) {
            return defaultAnimation;
        } else {
            Animation animation = createDefaultAnimationForCard(CARD_ANIMATION_DURATION);
            this.mIsNeedDefaultAnimation = false;
            return animation;
        }
    }

    private Rect getAnimationBounds(AppWindowToken token, Rect animationRect, boolean isLazyMode, Rect lazyModeRect) {
        if (token == null || animationRect == null || animationRect.isEmpty()) {
            return animationRect;
        }
        Rect animationBounds = animationRect;
        int rotation = this.mIWmsInner.getService().getDefaultDisplayRotation();
        DisplayCutout displayCutout = null;
        WindowState win = token.findMainWindow();
        boolean isStatusBarShow = true;
        boolean isNotchSwitchOpen = false;
        if (win != null) {
            if ((win.mAttrs.flags & 512) != 0) {
                animationBounds.set(win.getVisibleFrameLw());
            } else {
                animationBounds.set(win.getDisplayFrameLw());
            }
            isStatusBarShow = (win.mAttrs.flags & 1024) == 0 && (win.getSystemUiVisibility() & 4) == 0;
            displayCutout = win.getWmDisplayCutout().getDisplayCutout();
        }
        boolean isCutOutAvailable = displayCutout != null && !displayCutout.isEmpty();
        boolean isNotchDisplayDisabled = false;
        WindowManagerPolicy policy = this.mIWmsInner.getPolicy();
        if (policy instanceof HwPhoneWindowManager) {
            isNotchDisplayDisabled = ((HwPhoneWindowManager) policy).isNotchDisplayDisabled();
        }
        if (isNotchDisplayDisabled && token.getWindowingMode() == 1 && isCutOutAvailable) {
            isNotchSwitchOpen = true;
        }
        if (isNotchSwitchOpen && isStatusBarShow && displayCutout != null && rotation == 0) {
            animationBounds.top += displayCutout.getSafeInsetTop();
        }
        if (isLazyMode && lazyModeRect != null) {
            animationBounds = new Rect(lazyModeRect);
            if (isNotchSwitchOpen) {
                animationBounds.top = (int) (((float) animationBounds.top) + (((float) displayCutout.getSafeInsetTop()) * getLazyModeScale()));
            }
        }
        Slog.d(TAG, "getAnimationBounds outBounds " + animationBounds + " isCutOutAvailable " + isCutOutAvailable + " isNotchDisplayDisabled " + isNotchDisplayDisabled + " windowingMode " + token.getWindowingMode() + " isStatusBarShow " + isStatusBarShow);
        return animationBounds;
    }

    private Animation createDefaultAnimationForCard(long duration) {
        Animation defaultAnimation = new AlphaAnimation(1.0f, 1.0f);
        defaultAnimation.setDuration(duration);
        defaultAnimation.setInterpolator(FAST_IN_SLOW_OUT);
        defaultAnimation.setFillEnabled(true);
        defaultAnimation.setFillAfter(true);
        defaultAnimation.setFillBefore(true);
        return defaultAnimation;
    }

    private boolean isWindowCanFocus(WindowState windowState) {
        return (windowState.getAttrs().flags & 8) == 0;
    }

    public boolean isNeedForbidDialogAct(String packageName, ComponentName componentName) {
        return HwAppActController.getInstance().isNeedForbidAppAct(AppActConstant.BAD_DIALOG_FORBIDDEN, packageName, componentName != null ? componentName.getClassName() : "", null);
    }

    public void setAnimatorLazyModeEx(boolean isLazying) {
        if (this.mIsQuickSwitchSingleHand && !isLazying) {
            onEndQuickSwitchSingleHand();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void quickSwitchSingleHandIfNeeded() {
        boolean isLeft;
        String mode = Settings.Global.getString(this.mContext.getContentResolver(), QUICK_SWITCH_SINGLE_HAND_MODE);
        if (mode == null || mode.isEmpty()) {
            Slog.i(TAG, "not need to quick switch single hand because reset mode");
            return;
        }
        if (HwMultiWinConstants.LEFT_HAND_LAZY_MODE_STR.equals(mode)) {
            isLeft = true;
        } else if (HwMultiWinConstants.RIGHT_HAND_LAZY_MODE_STR.equals(mode)) {
            isLeft = false;
        } else {
            Slog.w(TAG, "invalid request single hand mode: " + mode);
            resetQuickSwitchSingleHand();
            return;
        }
        if (this.mLazyModeOnEx != 0) {
            Slog.i(TAG, "already in single hand mode");
            resetQuickSwitchSingleHand();
            return;
        }
        onStartQuickSwitchSingleHand(isLeft);
    }

    private void onStartQuickSwitchSingleHand(boolean isLeft) {
        Slog.i(TAG, "onStartQuickSwitchSingleHand isLeft: " + isLeft);
        if (this.mIsQuickSwitchSingleHand) {
            Slog.i(TAG, "already onStart");
            return;
        }
        this.mIsQuickSwitchSingleHand = true;
        this.mIWmsInner.getService().startFreezingScreen(0, 0);
        synchronized (this.mIWmsInner.getGlobalLock()) {
            ScreenRotationAnimation screenRotationAnimation = this.mIWmsInner.getWindowAnimator().getScreenRotationAnimationLocked(0);
            if (screenRotationAnimation != null) {
                screenRotationAnimation.setIsSingleHandScreenShotAnim(true);
            }
        }
        Slog.i(TAG, "start filter input event");
        synchronized (this.mIWmsInner.getGlobalLock()) {
            this.mIWmsInner.getService().mInputManagerCallback.setEventDispatchingLw(false);
        }
        Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", isLeft ? HwMultiWinConstants.LEFT_HAND_LAZY_MODE_STR : HwMultiWinConstants.RIGHT_HAND_LAZY_MODE_STR);
        this.mIWmsInner.getWindowMangerServiceHandler().postDelayed(this.mQuickSwitchSingleHandRunnable, 2000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onEndQuickSwitchSingleHand() {
        Slog.i(TAG, "onEndQuickSwitchSingleHand");
        if (!this.mIsQuickSwitchSingleHand) {
            Slog.i(TAG, "already onEnd");
            return;
        }
        this.mIsQuickSwitchSingleHand = false;
        this.mIWmsInner.getWindowMangerServiceHandler().removeCallbacks(this.mQuickSwitchSingleHandRunnable);
        this.mIWmsInner.getService().stopFreezingScreen();
        synchronized (this.mIWmsInner.getGlobalLock()) {
            this.mIWmsInner.getService().mInputManagerCallback.setEventDispatchingLw(true);
        }
        resetQuickSwitchSingleHand();
    }

    private void resetQuickSwitchSingleHand() {
        Settings.Global.putString(this.mContext.getContentResolver(), QUICK_SWITCH_SINGLE_HAND_MODE, "");
        Settings.Global.putString(this.mContext.getContentResolver(), QUICK_SWITCH_SINGLE_HAND_TYPE, "");
    }

    public Handler getAnimationHandler() {
        return this.mIWmsInner.getService().mAnimationHandler;
    }

    public boolean isAppControlPolicyExists() {
        return HwAppActController.getInstance().isAppControlPolicyExists();
    }
}
