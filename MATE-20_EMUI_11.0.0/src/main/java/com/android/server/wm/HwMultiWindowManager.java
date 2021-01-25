package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.WindowConfiguration;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.BadParcelableException;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import com.android.server.LocalServices;
import com.android.server.gesture.DefaultGestureNavManager;
import com.android.server.multiwin.HwMultiWinUtils;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.utils.HwDisplaySizeUtil;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.fsm.HwFoldScreenManager;
import com.huawei.android.view.HwWindowManager;
import com.huawei.server.HwPartMagicWindowServiceFactory;
import com.huawei.server.magicwin.DefaultHwMagicWindowManagerService;
import com.huawei.server.multiwindowtip.HwMultiWindowTips;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HwMultiWindowManager {
    private static final int APP_TYPE_DEFAULT = 0;
    private static final int APP_TYPE_QUICK_NOTE = 1;
    private static final int CAMERA_STATE_ACTIVE = 1;
    private static final long CANCEL_PENDING_SHOW_DELAY_TIME = 200;
    public static final int DEFATUL_FOREGROUND_FREEFORM_STACK_MAX_LIMIT = 2;
    public static final int DEFATUL_FOREGROUND_FREEFORM_STACK_MIN_LIMIT = 1;
    private static final int DISPLAY_FULL_WINDOW_FULL = 11;
    private static final int DISPLAY_FULL_WINDOW_HWFREEFORM = 10;
    private static final int DISPLAY_FULL_WINDOW_HWSPLITSCREEN = 12;
    private static final int DISPLAY_MAIN_WINDOW_FULL = 21;
    private static final int DISPLAY_MAIN_WINDOW_HWFREEFORM = 20;
    private static final int DISPLAY_MAIN_WINDOW_HWSPLITSCREEN = 23;
    private static final int DISPLAY_SUB_WINDOW_FULL = 22;
    static final String DOCKBAR_PACKAGE_NAME = "com.huawei.hwdockbar";
    private static final int FLOATING_BALL_MAX_INIT_TIME = 5;
    private static final float FLOAT_FRECISION = 0.001f;
    private static final int FREEFORM_DRAGBAR_RATIO = 3;
    private static final int GUTTER_IN_DP = 24;
    private static final int HALF_DIVISOR = 2;
    public static final String HEIGHT_COLUMNS = "heightColumns";
    public static final String HW_DOCK_START_ACTIVITY = "com.huawei.hwdockbar.floatwindowboots.FloatWindowBootsActivity";
    public static final String HW_FREEFORM_CENTER_BOUNDS = "hwFreeFormCenterBounds";
    public static final String HW_FREEFORM_CENTER_SCALE_RATIO = "hwFreeFormCenterScaleRatio";
    public static final String HW_FREEFORM_CENTER_VISUAL_BOUNDS = "hwFreeFormCenterVisualBounds";
    public static final String HW_SPLIT_SCREEN_PRIMARY_BOUNDS = "primaryBounds";
    public static final int HW_SPLIT_SCREEN_PRIMARY_EITHER = -1;
    public static final int HW_SPLIT_SCREEN_PRIMARY_LEFT = 1;
    public static final String HW_SPLIT_SCREEN_PRIMARY_POSITION = "primaryPosition";
    public static final int HW_SPLIT_SCREEN_PRIMARY_TOP = 0;
    public static final int HW_SPLIT_SCREEN_RATIO_DEFAULT = 0;
    public static final int HW_SPLIT_SCREEN_RATIO_PRAIMARY_LESS_THAN_DEFAULT = 1;
    public static final int HW_SPLIT_SCREEN_RATIO_PRAIMARY_MORE_THAN_DEFAULT = 2;
    public static final int HW_SPLIT_SCREEN_RATIO_PRIMARY_FULL = 3;
    public static final int HW_SPLIT_SCREEN_RATIO_PRIMARY_FULL_RELATIVE = 5;
    public static final int HW_SPLIT_SCREEN_RATIO_SECONDARY_FULL = 4;
    public static final int HW_SPLIT_SCREEN_RATIO_SECONDARY_FULL_RELATIVE = 6;
    public static final String HW_SPLIT_SCREEN_RATIO_VALUES = "splitRatios";
    public static final String HW_SPLIT_SCREEN_SECONDARY_BOUNDS = "secondaryBounds";
    private static final long INIT_FLOATING_BALL_POS_DELAY_TIME = 100;
    private static final float INIT_SCALE_RATIO_FOR_PAD = 0.9f;
    public static final boolean IS_HW_MULTIWINDOW_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    public static final boolean IS_NOTCH_PROP = (!"".equals(SystemProperties.get("ro.config.hw_notch_size", "")));
    public static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", ""));
    private static final String IS_TEMP_STACK = "IS_ROM_TEMP_STACK";
    private static final String LAUNCHER_PACKAGE_NAME = "com.huawei.android.launcher";
    private static final int MAGIC_WINDOW_SAVITY_LEFT = 80;
    private static final int MARGIN_COLUMN4_IN_DP = 24;
    private static final int MARGIN_COLUMN8_IN_DP = 32;
    private static final String MULTI_TASK_APP_PACKAGE = "com.huawei.hwdockbar.intent.extra.MULTIPLE_TASK_APP_PACKAGE";
    private static final Object M_LOCK = new Object();
    private static final int OVERLAP_IN_DP = 80;
    public static final int PC_MODE_FOREGROUND_FREEFORM_STACK_LIMIT = 8;
    private static final String PERMISSION_PACKAGE_NAME = "com.android.permissioncontroller";
    private static final int PRIMARY_FULL_RELATIVE_RATIO = 10;
    private static final int ROTATION_360 = 4;
    private static final int SECONDARY_FULL_RELATIVE_RATIO = 11;
    private static final long STACK_DISSMISS_DELAY_TIME = 100;
    private static final String SYS_HW_MULTIWIN_FOR_CAMERA = "sys.hw_multiwin_for_camera";
    public static final String TAG = "HwMultiWindowManager";
    private static final long TASK_STACK_HIDE_TIME_OUT = 2000;
    public static final String WIDTH_COLUMNS = "widthColumns";
    public static Set<Integer> applockSet = new HashSet();
    private static int sDisplayMode;
    private static int sDividerWindowWidth;
    private static int sForegroundFreeFormStack = 1;
    private static float sFreeformCornerRadius;
    private static String sLaunchPkg = "";
    private static int sNavigationBarHeight;
    private static int sNavigationBarWidth;
    private static long sOneStepBdTime = 0;
    public static Set<String> sRemoveFromBallActivitys = new HashSet();
    private static volatile HwMultiWindowManager sSingleInstance = null;
    private static int sStatusBarHeight;
    private static int sTranslucentStackId = -1;
    public static int sWindowOffset = 0;
    Rect defaultBound = new Rect();
    private boolean isReadyToFinishAnimation = true;
    private boolean isReadyToShow = true;
    private int mCaptionViewHeight;
    private int mDargbarWidth;
    private int mDisplayId = 0;
    private int mFloatingBallInitTimes = 0;
    private Rect mFloatingBallPos = new Rect();
    private Rect mFreeFormDrop = new Rect();
    public HashMap<Integer, Integer> mFreeformGuideMap = new HashMap<>();
    private int mGestureNavHotArea;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private DefaultHwMagicWindowManagerService mHwMagicWindowService;
    private HwMultiWindowTips mHwMultiWindowTip = null;
    private List<HwSplitScreenCombination> mHwSplitScreenCombinations = new ArrayList();
    boolean mIsAddSplitBar = false;
    private boolean mIsDockShowing = false;
    private boolean mIsFloatingBallPosInit = false;
    private boolean mIsHasSideinScreen;
    private boolean mIsStatusBarPermenantlyShowing;
    private boolean mIsUseHdr = false;
    private ActivityStack mPadCastStack;
    private int mSafeSideWidth;
    final ActivityTaskManagerService mService;
    private ActivityStack mStackToReplaceSplitScreen = null;
    HwSurfaceInNotch mSurfaceInNotch = null;
    private int mUserId = 0;
    private Map<String, String> packageNameRotations = new ConcurrentHashMap();
    private String packageNameUseCamera = "";
    private Set<Integer> pendingShowStackSet = new HashSet();
    public float ratio = 1.0f;

    static {
        sRemoveFromBallActivitys.add("com.tencent.mm/.plugin.voip.ui.VideoActivity");
        sRemoveFromBallActivitys.add("com.tencent.mobileqq/com.tencent.av.ui.VChatActivity");
        sRemoveFromBallActivitys.add("com.tencent.mobileqq/com.tencent.av.ui.VideoInviteActivity");
        sRemoveFromBallActivitys.add("com.tencent.mobileqq/com.tencent.av.ui.AVActivity");
        sRemoveFromBallActivitys.add("com.tencent.mobileqq/com.tencent.av.ui.VideoInviteFull");
        sRemoveFromBallActivitys.add("com.tencent.tim/com.tencent.av.ui.VideoInviteFull");
        sRemoveFromBallActivitys.add("com.tencent.tim/com.tencent.av.ui.VChatActivity");
        sRemoveFromBallActivitys.add("com.tencent.tim/com.tencent.av.ui.VideoInviteActivity");
    }

    private HwMultiWindowManager(ActivityTaskManagerService service) {
        this.mService = service;
        if (IS_HW_MULTIWINDOW_SUPPORTED) {
            this.mHwMultiWindowTip = HwMultiWindowTips.getInstance(service.mContext);
        }
        initHandlerThread();
    }

    public static HwMultiWindowManager getInstance(ActivityTaskManagerServiceEx atmsEx) {
        if (atmsEx == null || atmsEx.getActivityTaskManagerService() == null) {
            return null;
        }
        return getInstance(atmsEx.getActivityTaskManagerService());
    }

    public static HwMultiWindowManager getInstance(ActivityTaskManagerService service) {
        if (sSingleInstance == null) {
            synchronized (M_LOCK) {
                if (sSingleInstance == null) {
                    sSingleInstance = new HwMultiWindowManager(service);
                }
            }
        }
        return sSingleInstance;
    }

    private void initHandlerThread() {
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
    }

    private static void scale(Rect rect, float xscale, float yscale) {
        if (xscale != 1.0f) {
            rect.left = (int) ((((float) rect.left) * xscale) + 0.5f);
            rect.right = (int) ((((float) rect.right) * xscale) + 0.5f);
        }
        if (yscale != 1.0f) {
            rect.top = (int) ((((float) rect.top) * yscale) + 0.5f);
            rect.bottom = (int) ((((float) rect.bottom) * yscale) + 0.5f);
        }
    }

    public static void calcHwSplitStackBounds(ActivityDisplayEx activityDisplayEx, int splitRatio, Rect primaryOutBounds, Rect secondaryOutBounds) {
        if (activityDisplayEx != null && activityDisplayEx.getActivityDisplay() != null) {
            calcHwSplitStackBounds(activityDisplayEx.getActivityDisplay(), splitRatio, primaryOutBounds, secondaryOutBounds);
        }
    }

    public static void calcHwSplitStackBounds(ActivityDisplay display, int splitRatio, Rect primaryOutBounds, Rect secondaryOutBounds) {
        if (display != null) {
            Bundle bundle = getSplitGearsByDisplay(display);
            float[] splitRatios = bundle.getFloatArray(HW_SPLIT_SCREEN_RATIO_VALUES);
            int tempSplitRatio = splitRatio;
            if (!(splitRatio == 0 || splitRatio == 6 || splitRatio == 5 || splitRatios == null || splitRatios.length != 1)) {
                tempSplitRatio = 0;
            }
            float primaryRatio = calcPrimaryRatio(tempSplitRatio);
            if (primaryRatio == 0.0f) {
                if (primaryOutBounds != null) {
                    primaryOutBounds.setEmpty();
                }
                if (secondaryOutBounds != null) {
                    secondaryOutBounds.setEmpty();
                    return;
                }
                return;
            }
            int primaryPos = bundle.getInt(HW_SPLIT_SCREEN_PRIMARY_POSITION);
            if (primaryPos == 1) {
                calcLeftRightSplitStackBounds(display, primaryRatio, primaryOutBounds, secondaryOutBounds);
            } else if (primaryPos == 0) {
                calcTopBottomSplitStackBounds(display, primaryRatio, primaryOutBounds, secondaryOutBounds);
            }
        }
    }

    private static void calcLeftRightSplitStackBounds(ActivityDisplay display, float primaryRatio, Rect primaryOutBounds, Rect secondaryOutBounds) {
        DisplayCutout cutout;
        if (display != null && display.mDisplayContent != null) {
            int displayWidth = display.mDisplayContent.getDisplayInfo().logicalWidth;
            int displayHeight = display.mDisplayContent.getDisplayInfo().logicalHeight;
            int notchSize = 0;
            if (IS_NOTCH_PROP && ((display.mDisplayContent.getDisplayInfo().rotation == 1 || display.mDisplayContent.getDisplayInfo().rotation == 3) && (cutout = display.mDisplayContent.calculateDisplayCutoutForRotation(display.mDisplayContent.getRotation()).getDisplayCutout()) != null)) {
                notchSize = Math.max(cutout.getSafeInsetLeft(), cutout.getSafeInsetRight());
            }
            int dividerWindowWidth = sDividerWindowWidth;
            if (primaryRatio == 10.0f || primaryRatio == 11.0f) {
                notchSize = 0;
                dividerWindowWidth = 0;
                if (primaryRatio == 10.0f) {
                    primaryRatio = 1.0f;
                } else {
                    primaryRatio = 0.0f;
                }
            }
            if (primaryOutBounds != null) {
                primaryOutBounds.set(0, 0, ((int) (((float) (displayWidth - notchSize)) * primaryRatio)) - (dividerWindowWidth / 2), displayHeight);
            }
            if (secondaryOutBounds != null) {
                secondaryOutBounds.set(((int) (((float) (displayWidth - notchSize)) * primaryRatio)) + (dividerWindowWidth / 2), 0, displayWidth - notchSize, displayHeight);
            }
            if (notchSize != 0 && display.mDisplayContent.getDisplayInfo().rotation == 1) {
                if (primaryOutBounds != null) {
                    primaryOutBounds.offset(notchSize, 0);
                }
                if (secondaryOutBounds != null) {
                    secondaryOutBounds.offset(notchSize, 0);
                }
            }
        }
    }

    private static void calcTopBottomSplitStackBounds(ActivityDisplay display, float primaryRatio, Rect primaryOutBounds, Rect secondaryOutBounds) {
        int displayWidth = display.mDisplayContent.getDisplayInfo().logicalWidth;
        int displayHeight = display.mDisplayContent.getDisplayInfo().logicalHeight;
        int notchSize = sStatusBarHeight;
        if (primaryOutBounds != null) {
            primaryOutBounds.set(0, notchSize, displayWidth, (((int) (((float) (displayHeight - notchSize)) * primaryRatio)) - (sDividerWindowWidth / 2)) + notchSize);
        }
        if (secondaryOutBounds != null) {
            secondaryOutBounds.set(0, ((int) (((float) (displayHeight - notchSize)) * primaryRatio)) + (sDividerWindowWidth / 2) + notchSize, displayWidth, displayHeight);
        }
    }

    private static int getColumnsByWidth(int widthInDp) {
        if (widthInDp > 0 && widthInDp < 320) {
            return 2;
        }
        if (widthInDp >= 320 && widthInDp < 600) {
            return 4;
        }
        if (widthInDp >= 600 && widthInDp < 840) {
            return 8;
        }
        if (widthInDp >= 840) {
            return 12;
        }
        return 1;
    }

    public static Bundle getSplitGearsByDisplay(ActivityDisplay display) {
        Bundle bundle = new Bundle();
        if (display == null || display.mDisplayContent == null) {
            return bundle;
        }
        float densityWithoutRog = getDensityDpiWithoutRog();
        int widthInDp = (int) (((float) (display.mDisplayContent.getDisplayInfo().logicalWidth * 160)) / densityWithoutRog);
        int heightInDp = (int) (((float) (display.mDisplayContent.getDisplayInfo().logicalHeight * 160)) / densityWithoutRog);
        int widthColumns = getColumnsByWidth(widthInDp);
        int heightColumns = getColumnsByWidth(heightInDp);
        if (widthColumns == 4 && heightColumns > 4) {
            bundle.putInt(HW_SPLIT_SCREEN_PRIMARY_POSITION, 0);
            bundle.putFloatArray(HW_SPLIT_SCREEN_RATIO_VALUES, new float[]{0.33333334f, 0.5f, 0.6666667f});
        } else if (widthColumns > 4 && heightColumns == 4) {
            bundle.putInt(HW_SPLIT_SCREEN_PRIMARY_POSITION, 1);
            bundle.putFloatArray(HW_SPLIT_SCREEN_RATIO_VALUES, (!isRealTablet(display.mDisplayId) || widthColumns <= 8) ? new float[]{0.5f} : new float[]{0.33333334f, 0.5f, 0.6666667f});
        } else if (widthColumns == 8 && heightColumns == 8) {
            bundle.putInt(HW_SPLIT_SCREEN_PRIMARY_POSITION, 1);
            bundle.putFloatArray(HW_SPLIT_SCREEN_RATIO_VALUES, new float[]{0.5f});
        } else if (widthColumns == 8 && heightColumns == 12) {
            bundle.putInt(HW_SPLIT_SCREEN_PRIMARY_POSITION, 0);
            bundle.putFloatArray(HW_SPLIT_SCREEN_RATIO_VALUES, new float[]{0.33333334f, 0.5f, 0.6666667f});
        } else if (widthColumns == 12 && heightColumns == 8) {
            bundle.putInt(HW_SPLIT_SCREEN_PRIMARY_POSITION, 1);
            bundle.putFloatArray(HW_SPLIT_SCREEN_RATIO_VALUES, new float[]{0.33333334f, 0.5f, 0.6666667f});
        } else {
            bundle.putInt(HW_SPLIT_SCREEN_PRIMARY_POSITION, widthInDp < heightInDp ? 0 : 1);
            bundle.putFloatArray(HW_SPLIT_SCREEN_RATIO_VALUES, new float[]{0.5f});
        }
        bundle.putInt(WIDTH_COLUMNS, widthColumns);
        bundle.putInt(HEIGHT_COLUMNS, heightColumns);
        return bundle;
    }

    public static void exitHwMultiStack(ActivityStack stack) {
        if (stack != null) {
            if (!HwMwUtils.ENABLED || !HwMwUtils.performPolicy(16, new Object[]{Integer.valueOf(stack.mStackId)}).getBoolean("RESULT_HWMULTISTACK", false)) {
                stack.setWindowingMode(1);
            }
        }
    }

    public static void exitHwMultiStack(ActivityStack stack, boolean isAnimate, boolean isShowRecents, boolean isEnteringSplitScreenMode, boolean isDeferEnsuringVisibility, boolean isCreating) {
        if (stack != null) {
            if (!HwMwUtils.ENABLED || !HwMwUtils.performPolicy(16, new Object[]{Integer.valueOf(stack.mStackId)}).getBoolean("RESULT_HWMULTISTACK", false)) {
                stack.setWindowingMode(1, isAnimate, isShowRecents, isEnteringSplitScreenMode, isDeferEnsuringVisibility, isCreating);
            }
        }
    }

    private static float calcPrimaryRatio(int splitRatio) {
        switch (splitRatio) {
            case 0:
                return 0.5f;
            case 1:
                return 0.33333334f;
            case 2:
                return 0.6666667f;
            case 3:
            case 4:
                return 0.0f;
            case 5:
                return 10.0f;
            case 6:
                return 11.0f;
            default:
                return 0.0f;
        }
    }

    public void onSystemReady() {
        if (IS_HW_MULTIWINDOW_SUPPORTED || HwMwUtils.ENABLED) {
            loadDimens(0);
            HwMultiWindowSplitUI.getInstance(this.mService.mUiContext, this.mService, this.mDisplayId).onSystemReady(this.mDisplayId);
        }
        HwMultiWindowTips hwMultiWindowTips = this.mHwMultiWindowTip;
        if (hwMultiWindowTips != null) {
            hwMultiWindowTips.onSystemReady();
        }
        sForegroundFreeFormStack = calcDefaultFreeFormNum(this.mService.mWindowManager.getDefaultDisplayContentLocked());
    }

    public boolean hasSplitScreenCombinations() {
        return this.mHwSplitScreenCombinations.size() > 0;
    }

    public void setHwMagicWindowService(DefaultHwMagicWindowManagerService hwMagicWindowService) {
        this.mHwMagicWindowService = hwMagicWindowService;
    }

    public DefaultHwMagicWindowManagerService getHwMagicWindowService() {
        return this.mHwMagicWindowService;
    }

    public void addStackReferenceIfNeeded(ActivityStack stack) {
        ActivityDisplay activityDisplay;
        if (!(stack == null || !stack.inHwMultiStackWindowingMode() || (activityDisplay = stack.getDisplay()) == null)) {
            boolean isLeftRight = true;
            if (stack.inHwFreeFormWindowingMode() && !stack.inHwPCMultiStackWindowingMode()) {
                if (this.mHwMultiWindowTip.getFreeformGuideCount() == 0) {
                    this.mFreeformGuideMap.put(Integer.valueOf(stack.getStackId()), 1);
                }
                updateDragFreeFormPos(stack);
                this.mService.mH.post(new Runnable(activityDisplay) {
                    /* class com.android.server.wm.$$Lambda$HwMultiWindowManager$WC4220KtkeJT8rPCfbM17bag0 */
                    private final /* synthetic */ ActivityDisplay f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        HwMultiWindowManager.this.lambda$addStackReferenceIfNeeded$0$HwMultiWindowManager(this.f$1);
                    }
                });
            }
            if (stack.inHwSplitScreenWindowingMode()) {
                for (int i = this.mHwSplitScreenCombinations.size() - 1; i >= 0; i--) {
                    HwSplitScreenCombination screenCombination = this.mHwSplitScreenCombinations.get(i);
                    if (screenCombination.mDisplayId == activityDisplay.mDisplayId) {
                        if (screenCombination.hasHwSplitScreenStack(stack)) {
                            return;
                        }
                        if (!screenCombination.isSplitScreenCombined()) {
                            Slog.d(TAG, "combine stack: " + stack.toShortString() + ", in combination: " + screenCombination);
                            screenCombination.addStackReferenceIfNeeded(stack);
                            screenCombination.reportPkgNameEvent(this.mService);
                            this.mService.getTaskChangeNotificationController().notifyTaskStackChanged();
                            return;
                        } else if (screenCombination.isSplitScreenCombinedAndVisible()) {
                            Slog.d(TAG, "replace stack: " + stack.toShortString() + ", in combination: " + screenCombination);
                            screenCombination.replaceCombinedSplitScreenStack(stack);
                            screenCombination.reportPkgNameEvent(this.mService);
                            this.mService.getTaskChangeNotificationController().notifyTaskStackChanged();
                            return;
                        }
                    }
                }
                HwSplitScreenCombination newScreenCombination = new HwSplitScreenCombination();
                newScreenCombination.addStackReferenceIfNeeded(stack);
                this.mHwSplitScreenCombinations.add(newScreenCombination);
                Slog.d(TAG, "add combination for stack: " + stack.toShortString() + ", slipt screen combinations size: " + this.mHwSplitScreenCombinations.size());
                notifyNavMgrForMultiWindowChanged(0);
                if (this.mHwMultiWindowTip != null) {
                    if (getSplitGearsByDisplay(stack.getDisplay()).getInt(HW_SPLIT_SCREEN_PRIMARY_POSITION) != 1) {
                        isLeftRight = false;
                    }
                    this.mHwMultiWindowTip.processSplitWinDockTip(isLeftRight);
                }
            }
        }
    }

    public void doReplaceSplitStack(ActivityStack stack) {
        synchronized (this.mService.getGlobalLock()) {
            if (stack != null) {
                this.mStackToReplaceSplitScreen = stack;
                this.mHandler.postDelayed(new Runnable() {
                    /* class com.android.server.wm.$$Lambda$HwMultiWindowManager$izx39OV3Vma8beBfareBdv2k2v8 */

                    @Override // java.lang.Runnable
                    public final void run() {
                        HwMultiWindowManager.this.lambda$doReplaceSplitStack$1$HwMultiWindowManager();
                    }
                }, 300);
            } else if (!(this.mStackToReplaceSplitScreen == null || this.mStackToReplaceSplitScreen.getDisplay() == null)) {
                this.mStackToReplaceSplitScreen.getDisplay().positionChildAtBottom(this.mStackToReplaceSplitScreen);
                exitHwMultiStack(this.mStackToReplaceSplitScreen);
                this.mStackToReplaceSplitScreen = null;
            }
        }
    }

    public /* synthetic */ void lambda$doReplaceSplitStack$1$HwMultiWindowManager() {
        synchronized (this.mService.getGlobalLock()) {
            if (!(this.mStackToReplaceSplitScreen == null || this.mStackToReplaceSplitScreen.getDisplay() == null)) {
                this.mStackToReplaceSplitScreen.getDisplay().positionChildAtBottom(this.mStackToReplaceSplitScreen);
                exitHwMultiStack(this.mStackToReplaceSplitScreen);
                this.mStackToReplaceSplitScreen = null;
            }
        }
    }

    public void removeStackReferenceIfNeeded(ActivityStack stack) {
        if (stack != null) {
            if ((!stack.inHwFreeFormWindowingMode() || stack.inHwPCMultiStackWindowingMode()) && stack.mTaskStack != null) {
                stack.mTaskStack.mHwStackScale = 1.0f;
            }
            stack.mIsStartOneStepWin = false;
            stack.mIsTempStack = false;
            if (HwMwUtils.ENABLED && stack.inHwMagicWindowingMode()) {
                ActivityStackEx asEx = new ActivityStackEx();
                asEx.setActivityStack(stack);
                HwPartMagicWindowServiceFactory.getInstance().getHwPartMagicWindowServiceFactoryImpl().getHwMagicWinCombineManager().removeStackReferenceIfNeeded(asEx);
            }
            if (sTranslucentStackId == stack.mStackId) {
                sTranslucentStackId = -1;
            }
            boolean isRemovedInStacks = stack.getDisplay() == null || !stack.getDisplay().mStacks.contains(stack);
            checkForegroundFreeform(stack, isRemovedInStacks);
            if (this.mPadCastStack == stack && isRemovedInStacks) {
                this.mPadCastStack = null;
            }
            if (this.mHwSplitScreenCombinations.size() > 0) {
                Iterator<HwSplitScreenCombination> iterator = this.mHwSplitScreenCombinations.iterator();
                while (iterator.hasNext()) {
                    HwSplitScreenCombination screenCombination = iterator.next();
                    if (screenCombination.hasHwSplitScreenStack(stack)) {
                        iterator.remove();
                        Slog.d(TAG, "remove combination for stack: " + stack.toShortString() + ", slipt screen combinations size: " + this.mHwSplitScreenCombinations.size());
                        screenCombination.removeStackReferenceIfNeeded(stack);
                        ActivityStack as = getFilteredTopStack(stack.getDisplay(), Arrays.asList(5, 2, 102, 105));
                        if (as == null || as.getWindowingMode() == 1 || as.getWindowingMode() == 103) {
                            Slog.i(TAG, "getFilteredTopStack, remove divider bar");
                            removeSplitScreenDividerBar(100, false, this.mDisplayId);
                        }
                        notifyNavMgrForMultiWindowChanged(1);
                        this.mService.getTaskChangeNotificationController().notifyTaskStackChanged();
                        if (!hasVisibleHwMultiStack(stack.getDisplay())) {
                            setColorMgrInfo(false);
                            return;
                        }
                        return;
                    }
                }
            }
        }
    }

    private void checkForegroundFreeform(ActivityStack stack, boolean isRemovedInStacks) {
        ActivityStack focusStack;
        if (stack.getDisplay() != null && isRemovedInStacks && stack.getWindowingMode() == 1 && (focusStack = stack.getDisplay().getFocusedStack()) != null && focusStack.inHwFreeFormWindowingMode()) {
            if (allowedForegroundFreeForms(stack.getDisplay().mDisplayId) == 2) {
                List<TaskRecord> addTasks = new ArrayList<>();
                List<ActivityStack> activityStacks = getForegroundFreeform(stack.getDisplay(), false);
                for (int stackNdx = activityStacks.size() - 1; stackNdx >= 0; stackNdx--) {
                    ActivityStack topStack = activityStacks.get(stackNdx);
                    if (topStack != focusStack) {
                        topStack.setAlwaysOnTopOnly(true);
                        addTasks.add(topStack.topTask());
                    }
                }
                if (!addTasks.isEmpty()) {
                    this.mService.mHwATMSEx.dispatchFreeformBallLifeState(addTasks, "remove");
                }
            }
            this.mService.mH.post(new Runnable(focusStack) {
                /* class com.android.server.wm.$$Lambda$HwMultiWindowManager$idm0ucRnY_k24D5qybZBfiktu8 */
                private final /* synthetic */ ActivityStack f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwMultiWindowManager.this.lambda$checkForegroundFreeform$2$HwMultiWindowManager(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$checkForegroundFreeform$2$HwMultiWindowManager(ActivityStack focusStack) {
        lambda$addStackReferenceIfNeeded$0$HwMultiWindowManager(focusStack.getDisplay());
    }

    private void notifyNavMgrForMultiWindowChanged(int state) {
        DefaultGestureNavManager gestureNavPolicy = (DefaultGestureNavManager) LocalServices.getService(DefaultGestureNavManager.class);
        if (gestureNavPolicy != null) {
            gestureNavPolicy.onMultiWindowChanged(state);
        }
    }

    public void moveStackToFrontEx(ActivityOptions options, ActivityStack stack, ActivityRecord startActivity, ActivityRecord sourceRecord, Rect launchBounds) {
        boolean isStackAlreadyOnTop = stack.isAlwaysOnTop();
        try {
            if (!stack.inHwMultiStackWindowingMode() || (options != null && WindowConfiguration.isHwMultiStackWindowingMode(options.getLaunchWindowingMode()))) {
                if (options == null || !WindowConfiguration.isHwMultiStackWindowingMode(options.getLaunchWindowingMode())) {
                    if (!stack.isAlwaysOnTop() || !isStackAlreadyOnTop) {
                        updateDragFreeFormPos(stack);
                    }
                    if (stack.inHwFreeFormWindowingMode() && sourceRecord != null && sourceRecord.inHwFreeFormWindowingMode() && ActivityStartInterceptorBridge.isAppLockActivity(sourceRecord.shortComponentName) && !launchBounds.isEmpty()) {
                        stack.resize(launchBounds, (Rect) null, (Rect) null);
                    }
                    if (startActivity != null && WindowConfiguration.isIncompatibleWindowingMode(startActivity.getWindowingMode(), stack.getWindowingMode())) {
                        startActivity.onParentChanged();
                    }
                    setAlwaysOnTopOnly(stack.getDisplay(), stack, false, false);
                } else if (stack.mTaskStack != null && stack.mTaskStack.isVisible() && !DOCKBAR_PACKAGE_NAME.equals(sLaunchPkg)) {
                    if (!stack.isAlwaysOnTop() || !isStackAlreadyOnTop) {
                        updateDragFreeFormPos(stack);
                    }
                    if (stack.inHwFreeFormWindowingMode() && sourceRecord != null && sourceRecord.inHwFreeFormWindowingMode() && ActivityStartInterceptorBridge.isAppLockActivity(sourceRecord.shortComponentName) && !launchBounds.isEmpty()) {
                        stack.resize(launchBounds, (Rect) null, (Rect) null);
                    }
                    if (startActivity != null && WindowConfiguration.isIncompatibleWindowingMode(startActivity.getWindowingMode(), stack.getWindowingMode())) {
                        startActivity.onParentChanged();
                    }
                    setAlwaysOnTopOnly(stack.getDisplay(), stack, false, false);
                } else if (startActivity == null || !startActivity.isActivityTypeHome() || !WindowConfiguration.isHwPCFreeFormWindowingMode(options.getLaunchWindowingMode())) {
                    boolean isPendingShow = options.isPendingShow();
                    if (isPendingShow) {
                        stack.setPendingShow(true);
                    }
                    if (isPendingShow || !stack.inHwMultiStackWindowingMode() || stack.getWindowingMode() != options.getLaunchWindowingMode()) {
                        int windowMode = options.getLaunchWindowingMode();
                        stack.setWindowingMode(windowMode);
                        Rect bounds = options.getLaunchBounds();
                        if (bounds == null) {
                            bounds = new Rect();
                        }
                        float adjustScale = -1.0f;
                        if (windowMode == 102 && bounds.isEmpty() && stack.topTask() != null && stack.topTask().realActivity != null) {
                            adjustScale = this.mService.mHwATMSEx.getReusableHwFreeFormBounds(stack.topTask().realActivity.getPackageName(), stack.topTask().userId, bounds);
                        }
                        if (!stack.inHwSplitScreenWindowingMode() && !bounds.isEmpty() && isLegalBoundsForMode(bounds, windowMode)) {
                            if (adjustScale > 0.0f && stack.getTaskStack() != null) {
                                stack.getTaskStack().mHwStackScale = adjustScale;
                            }
                            stack.resize(bounds, (Rect) null, (Rect) null);
                        }
                        if (WindowConfiguration.isHwFreeFormWindowingMode(windowMode) && options.getStackScale() > 0.0f && stack.getTaskStack() != null) {
                            stack.getTaskStack().mHwStackScale = options.getStackScale();
                        }
                    } else {
                        checkHwMultiStackBoundsWhenOptionsMatch(stack);
                    }
                    if (!stack.isAlwaysOnTop() || !isStackAlreadyOnTop) {
                        updateDragFreeFormPos(stack);
                    }
                    if (stack.inHwFreeFormWindowingMode() && sourceRecord != null && sourceRecord.inHwFreeFormWindowingMode() && ActivityStartInterceptorBridge.isAppLockActivity(sourceRecord.shortComponentName) && !launchBounds.isEmpty()) {
                        stack.resize(launchBounds, (Rect) null, (Rect) null);
                    }
                    if (startActivity != null && WindowConfiguration.isIncompatibleWindowingMode(startActivity.getWindowingMode(), stack.getWindowingMode())) {
                        startActivity.onParentChanged();
                    }
                    setAlwaysOnTopOnly(stack.getDisplay(), stack, false, false);
                } else {
                    if (!stack.isAlwaysOnTop() || !isStackAlreadyOnTop) {
                        updateDragFreeFormPos(stack);
                    }
                    if (stack.inHwFreeFormWindowingMode() && sourceRecord != null && sourceRecord.inHwFreeFormWindowingMode() && ActivityStartInterceptorBridge.isAppLockActivity(sourceRecord.shortComponentName) && !launchBounds.isEmpty()) {
                        stack.resize(launchBounds, (Rect) null, (Rect) null);
                    }
                    if (WindowConfiguration.isIncompatibleWindowingMode(startActivity.getWindowingMode(), stack.getWindowingMode())) {
                        startActivity.onParentChanged();
                    }
                    setAlwaysOnTopOnly(stack.getDisplay(), stack, false, false);
                }
            } else if (stack.inHwFreeFormWindowingMode()) {
                if (!(options == null || options.getAnimationType() == 0)) {
                    options.clearAnimation();
                }
            } else if ((stack.mTaskStack == null || !stack.mTaskStack.isVisible()) && !stack.isAlwaysOnTop()) {
                exitHwMultiStack(stack, false, false, false, true, false);
                if (!stack.isAlwaysOnTop() || !isStackAlreadyOnTop) {
                    updateDragFreeFormPos(stack);
                }
                if (stack.inHwFreeFormWindowingMode() && sourceRecord != null && sourceRecord.inHwFreeFormWindowingMode() && ActivityStartInterceptorBridge.isAppLockActivity(sourceRecord.shortComponentName) && !launchBounds.isEmpty()) {
                    stack.resize(launchBounds, (Rect) null, (Rect) null);
                }
                if (startActivity != null && WindowConfiguration.isIncompatibleWindowingMode(startActivity.getWindowingMode(), stack.getWindowingMode())) {
                    startActivity.onParentChanged();
                }
                setAlwaysOnTopOnly(stack.getDisplay(), stack, false, false);
            } else {
                if (!stack.isAlwaysOnTop() || !isStackAlreadyOnTop) {
                    updateDragFreeFormPos(stack);
                }
                if (stack.inHwFreeFormWindowingMode() && sourceRecord != null && sourceRecord.inHwFreeFormWindowingMode() && ActivityStartInterceptorBridge.isAppLockActivity(sourceRecord.shortComponentName) && !launchBounds.isEmpty()) {
                    stack.resize(launchBounds, (Rect) null, (Rect) null);
                }
                if (startActivity != null && WindowConfiguration.isIncompatibleWindowingMode(startActivity.getWindowingMode(), stack.getWindowingMode())) {
                    startActivity.onParentChanged();
                }
                setAlwaysOnTopOnly(stack.getDisplay(), stack, false, false);
            }
        } finally {
            if (!stack.isAlwaysOnTop() || !isStackAlreadyOnTop) {
                updateDragFreeFormPos(stack);
            }
            if (stack.inHwFreeFormWindowingMode() && sourceRecord != null && sourceRecord.inHwFreeFormWindowingMode() && ActivityStartInterceptorBridge.isAppLockActivity(sourceRecord.shortComponentName) && !launchBounds.isEmpty()) {
                stack.resize(launchBounds, (Rect) null, (Rect) null);
            }
            if (startActivity != null && WindowConfiguration.isIncompatibleWindowingMode(startActivity.getWindowingMode(), stack.getWindowingMode())) {
                startActivity.onParentChanged();
            }
            setAlwaysOnTopOnly(stack.getDisplay(), stack, false, false);
        }
    }

    private boolean isLegalBoundsForMode(Rect bounds, int mode) {
        if (!HwActivityTaskManager.isPCMultiCastMode()) {
            return true;
        }
        ActivityDisplay at = this.mService.getRootActivityContainer().getDefaultDisplay();
        if (!WindowConfiguration.isHwPCFreeFormWindowingMode(mode) || at == null) {
            if (!WindowConfiguration.isHwFreeFormWindowingMode(mode) || at == null || bounds.left < HwActivityTaskManager.getDisplayEdge(at.mDisplay)) {
                return true;
            }
            return false;
        } else if (bounds.left >= HwActivityTaskManager.getDisplayEdge(at.mDisplay)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isAllActivityTranslucentLocked(ActivityStack stack) {
        if (!stack.mIsStartOneStepWin) {
            return false;
        }
        for (int taskNdx = stack.getChildCount() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = stack.getChildAt(taskNdx);
            for (int activityNdx = task.getChildCount() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = task.getChildAt(activityNdx);
                if (ActivityStartInterceptorBridge.isAppLockActivity(r.shortComponentName) || r.fullscreen) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<ActivityStack> findCombinedSplitScreenStacks(ActivityStack stack) {
        for (int i = this.mHwSplitScreenCombinations.size() - 1; i >= 0; i--) {
            HwSplitScreenCombination screenCombination = this.mHwSplitScreenCombinations.get(i);
            if (screenCombination.isSplitScreenCombined() && screenCombination.hasHwSplitScreenStack(stack)) {
                return screenCombination.findCombinedSplitScreenStacks(stack);
            }
        }
        return null;
    }

    public ActivityStack getFilteredTopStack(ActivityDisplay activityDisplay, List<Integer> ignoreWindowModes) {
        ActivityStack stack = null;
        synchronized (this.mService.getGlobalLock()) {
            if (activityDisplay == null) {
                Slog.i(TAG, "getFilteredTopStack activityDisplay null, no TopStack");
                return null;
            }
            for (int stackNdx = activityDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                stack = activityDisplay.getChildAt(stackNdx);
                StringBuilder sb = new StringBuilder();
                sb.append("getFilteredTopStack, stack window mode:");
                sb.append(stack.getWindowingMode());
                sb.append(" , ignore window mode: ");
                sb.append(ignoreWindowModes == null ? null : ignoreWindowModes.toString());
                Slog.i(TAG, sb.toString());
                if (ignoreWindowModes == null || !ignoreWindowModes.contains(Integer.valueOf(stack.getWindowingMode()))) {
                    return stack;
                }
            }
            return stack;
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getFilteredTopStack(ActivityDisplay activityDisplay, boolean isIgnoreAlwaysOnTop) {
        ActivityStack stack = null;
        synchronized (this.mService.getGlobalLock()) {
            if (activityDisplay == null) {
                Slog.i(TAG, "getFilteredTopStack activityDisplay null, no TopStack");
                return null;
            }
            for (int stackNdx = activityDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                stack = activityDisplay.getChildAt(stackNdx);
                if (!stack.isAlwaysOnTop() || !isIgnoreAlwaysOnTop) {
                    if (stack.topRunningActivityLocked(false) != null) {
                        return stack;
                    }
                }
            }
            return stack;
        }
    }

    public void resizeHwSplitStacks(int splitRatio, boolean isEnsureVisible) {
        synchronized (this.mService.getGlobalLock()) {
            if (this.mHwSplitScreenCombinations.size() > 0) {
                for (int i = this.mHwSplitScreenCombinations.size() - 1; i >= 0; i--) {
                    HwSplitScreenCombination screenCombination = this.mHwSplitScreenCombinations.get(i);
                    if (screenCombination.isSplitScreenCombinedAndVisible()) {
                        screenCombination.resizeHwSplitStacks(splitRatio, isEnsureVisible);
                        return;
                    }
                }
            }
        }
    }

    public void resizeMagicWindowBounds(ActivityStack stack, int splitRatio) {
        ActivityDisplay display = stack.getDisplay();
        if (display != null && stack.getTopActivity() != null) {
            Rect leftBounds = new Rect();
            Rect rightBounds = new Rect();
            calcHwSplitStackBounds(display, splitRatio, leftBounds, rightBounds);
            HwMwUtils.performPolicy(62, new Object[]{stack.getTopActivity().task, leftBounds, rightBounds, Integer.valueOf(splitRatio)});
        }
    }

    private void calcHwSplitStackForConfigChange(ActivityStack stack, Rect outBounds) {
        float[] splitRatios;
        int splitRatio = 0;
        boolean isCombined = false;
        HwSplitScreenCombination visibleCombination = null;
        int i = this.mHwSplitScreenCombinations.size() - 1;
        while (true) {
            if (i < 0) {
                break;
            }
            HwSplitScreenCombination screenCombination = this.mHwSplitScreenCombinations.get(i);
            if (screenCombination.isSplitScreenCombinedAndVisible()) {
                visibleCombination = screenCombination;
            }
            if (screenCombination.hasStackAndMatchWindowMode(stack)) {
                isCombined = true;
                if (!(screenCombination.mSplitRatio == 0 || (splitRatios = getSplitGearsByDisplay(stack.getDisplay()).getFloatArray(HW_SPLIT_SCREEN_RATIO_VALUES)) == null || splitRatios.length != 1)) {
                    screenCombination.mSplitRatio = 0;
                }
                splitRatio = screenCombination.mSplitRatio;
            } else {
                i--;
            }
        }
        if (!isCombined && visibleCombination != null) {
            splitRatio = visibleCombination.mSplitRatio;
        }
        if (stack.inHwSplitScreenPrimaryWindowingMode()) {
            calcHwSplitStackBounds(stack.getDisplay(), splitRatio, outBounds, (Rect) null);
        } else if (stack.inHwSplitScreenSecondaryWindowingMode()) {
            calcHwSplitStackBounds(stack.getDisplay(), splitRatio, (Rect) null, outBounds);
        }
    }

    public void calcHwMultiWindowStackBoundsForConfigChange(ActivityStack stack, Rect outBounds, Rect oldStackBounds, int oldDisplayWidth, int oldDisplayHeight, int newDisplayWidth, int newDisplayHeight, boolean isModeChanged) {
        Slog.d(TAG, "stack: " + stack.toShortString() + " oldStackBounds " + oldStackBounds + ", oldDisplayWidth: " + oldDisplayWidth + ", oldDisplayHeight: " + oldDisplayHeight + ", newDisplayWidth: " + newDisplayWidth + ", newDisplayHeight: " + newDisplayHeight + ", isModeChanged: " + isModeChanged);
        if (stack.inHwSplitScreenWindowingMode()) {
            calcHwSplitStackForConfigChange(stack, outBounds);
        }
        if (!stack.inHwFreeFormWindowingMode()) {
            return;
        }
        if (stack == this.mPadCastStack) {
            calcPadCastStackBounds(outBounds, stack);
        } else if (stack.getDisplay() != null && !stack.isPendingShow() && allowedForegroundFreeForms(stack.getDisplay().mDisplayId) == sForegroundFreeFormStack) {
            calcDefaultFreeFormBounds(outBounds, stack, false, true);
            if (stack.isAlwaysOnTop() && oldDisplayHeight == newDisplayWidth && oldDisplayWidth == newDisplayHeight) {
                updatePosForConfigChange(stack);
            }
        }
    }

    private void calcHwMultiWindowStackBoundsDefault(ActivityStack stack, Rect outBounds) {
        if (stack != null) {
            if (stack.inHwSplitScreenPrimaryWindowingMode()) {
                calcHwSplitStackBounds(stack.getDisplay(), 0, outBounds, (Rect) null);
            } else if (stack.inHwSplitScreenSecondaryWindowingMode()) {
                calcHwSplitStackBounds(stack.getDisplay(), 0, (Rect) null, outBounds);
            }
        }
    }

    private ActivityStack getMagicWindowStack(int displayId) {
        ActivityDisplay defaultDisplay = this.mService.mRootActivityContainer.getActivityDisplay(displayId);
        return defaultDisplay != null ? defaultDisplay.getTopStackInWindowingMode(103) : null;
    }

    public ActivityStack getSplitScreenPrimaryStack(int displayId) {
        ActivityDisplay display = this.mService.mRootActivityContainer.getActivityDisplay(displayId);
        return display != null ? display.getTopStackInWindowingMode(100) : null;
    }

    public ActivityStack getSplitScreenTopStack(int displayId) {
        return getFilteredTopStack(this.mService.mRootActivityContainer.getActivityDisplay(displayId), Arrays.asList(5, 2, 102));
    }

    public Rect getLeftBoundsForMagicWindow(ActivityStack activityStack) {
        if (activityStack == null || activityStack.getTopActivity() == null) {
            return null;
        }
        Object taskObj = activityStack.getTopActivity().task;
        if (!(taskObj instanceof TaskRecordBridge)) {
            return null;
        }
        TaskRecordBridge taskRecord = (TaskRecordBridge) taskObj;
        if (taskRecord.getDragFullMode() == 5 || taskRecord.getDragFullMode() == 6) {
            Rect primaryFull = new Rect();
            calcLeftRightSplitStackBounds(activityStack.getDisplay(), calcPrimaryRatio(taskRecord.getDragFullMode()), primaryFull, null);
            return primaryFull;
        }
        for (int i = 0; i < taskRecord.getChildCount(); i++) {
            if (taskRecord.getChildAt(i) != null && taskRecord.getChildAt(i).getBounds().left < 80 && taskRecord.getChildAt(i).getBounds().right < taskRecord.getBounds().right - sDividerWindowWidth && taskRecord.getChildAt(i).isInterestingToUserLocked()) {
                return taskRecord.getChildAt(i).getBounds();
            }
        }
        return null;
    }

    public void onConfigurationChanged(int displayId) {
        HwSurfaceInNotch hwSurfaceInNotch;
        ActivityDisplay display;
        if (IS_HW_MULTIWINDOW_SUPPORTED || HwMwUtils.ENABLED) {
            this.mService.mHwATMSEx.resetHwFreeFormBoundsRecords(displayId);
            loadDimens(displayId);
            if (!(!HwFoldScreenManager.isFoldable() || (display = this.mService.mRootActivityContainer.getActivityDisplay(displayId)) == null || display.mDisplayContent == null)) {
                display.mDisplayContent.getDisplayPolicy().updateConfigurationAndScreenSizeDependentBehaviors();
            }
            HwMultiWindowSplitUI.getInstance(((ActivityTaskManagerService) this.mService).mUiContext, this.mService, displayId).onConfigurationChanged(displayId);
            if (IS_NOTCH_PROP && (hwSurfaceInNotch = this.mSurfaceInNotch) != null) {
                hwSurfaceInNotch.remove();
            }
        }
    }

    public Bundle getStackPackageNames(boolean useAppPackageName, int displayId) {
        String str;
        HwMultiWindowManager hwMultiWindowManager = this;
        ActivityStack topStack = hwMultiWindowManager.getSplitScreenTopStack(displayId);
        Bundle bundle = new Bundle();
        ArrayList<String> combinedStackPackageNames = new ArrayList<>();
        ArrayList<Integer> combinedStackAppUserIds = new ArrayList<>();
        ArrayList<Integer> combinedAppTypes = new ArrayList<>();
        if (topStack == null || !topStack.inHwMagicWindowingMode()) {
            ActivityStack stackPrimary = hwMultiWindowManager.getSplitScreenPrimaryStack(displayId);
            if (stackPrimary == null) {
                return null;
            }
            List<ActivityStack> combinedStacks = hwMultiWindowManager.findCombinedSplitScreenStacks(stackPrimary);
            String topPackageName = null;
            if (stackPrimary.getTopActivity() != null) {
                combinedStackPackageNames.add(hwMultiWindowManager.getAppPackageName(stackPrimary, useAppPackageName));
                combinedStackAppUserIds.add(Integer.valueOf(stackPrimary.getTopActivity().mUserId));
                if (HwMultiWinUtils.isQuickNote(stackPrimary.getTopActivity().mActivityComponent)) {
                    combinedAppTypes.add(1);
                } else {
                    combinedAppTypes.add(0);
                }
            }
            if (combinedStacks != null && !combinedStacks.isEmpty()) {
                for (ActivityStack as : combinedStacks) {
                    if (as == null || as.getTopActivity() == null) {
                        str = topPackageName;
                    } else {
                        combinedStackPackageNames.add(hwMultiWindowManager.getAppPackageName(as, useAppPackageName));
                        combinedStackAppUserIds.add(Integer.valueOf(as.getTopActivity().mUserId));
                        if (HwMultiWinUtils.isQuickNote(as.getTopActivity().mActivityComponent)) {
                            combinedAppTypes.add(1);
                            str = null;
                        } else {
                            str = null;
                            combinedAppTypes.add(0);
                        }
                    }
                    topPackageName = str;
                    hwMultiWindowManager = this;
                }
            }
            bundle.putStringArrayList("pkgNames", combinedStackPackageNames);
            bundle.putIntegerArrayList("pkgUserIds", combinedStackAppUserIds);
            bundle.putIntegerArrayList("appTypes", combinedAppTypes);
            return bundle;
        }
        if (topStack.getTopActivity() != null) {
            combinedStackPackageNames.add(topStack.getTopActivity().packageName);
            combinedStackAppUserIds.add(Integer.valueOf(topStack.getTopActivity().mUserId));
            bundle.putStringArrayList("pkgNames", combinedStackPackageNames);
            bundle.putIntegerArrayList("pkgUserIds", combinedStackAppUserIds);
        }
        return bundle;
    }

    public int getTaskDragFullMode(int displayId) {
        TaskRecord mTaskRecord;
        ActivityStack stack = getMagicWindowStack(displayId);
        if (stack == null || stack.getTopActivity() == null || !stack.inHwMagicWindowingMode() || (mTaskRecord = stack.getTopActivity().getTaskRecord()) == null || !(mTaskRecord instanceof TaskRecordBridge)) {
            return 0;
        }
        return ((TaskRecordBridge) mTaskRecord).getDragFullMode();
    }

    public boolean isDragFullModeByType(int type) {
        return type == 5 || type == 6;
    }

    public boolean isDragFullModeByDisplayId(int displayId, int windowMode) {
        if (windowMode != 103) {
            return false;
        }
        return isDragFullModeByType(getTaskDragFullMode(displayId));
    }

    private String getAppPackageName(ActivityStack stack, boolean useAppPackageName) {
        ActivityRecord record = stack.getTopActivity();
        if (!useAppPackageName || !DOCKBAR_PACKAGE_NAME.equals(record.packageName)) {
            return record.getTaskRecord().getRootActivity().packageName;
        }
        return getAppPackageInMultiTaskManager(record);
    }

    public String getAppPackageInMultiTaskManager(ActivityRecord record) {
        if (record == null) {
            return "";
        }
        if (!DOCKBAR_PACKAGE_NAME.equals(record.packageName)) {
            return record.packageName;
        }
        String appPackageName = null;
        if (record.intent != null) {
            appPackageName = new Intent(record.intent).getStringExtra(MULTI_TASK_APP_PACKAGE);
        }
        return appPackageName != null ? appPackageName : record.packageName;
    }

    public int[] getCombinedSplitScreenTaskIds(ActivityStack stack) {
        if (stack == null || this.mHwSplitScreenCombinations.isEmpty()) {
            return null;
        }
        List<Integer> combinedTaskIds = new ArrayList<>();
        Iterator<HwSplitScreenCombination> it = this.mHwSplitScreenCombinations.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            HwSplitScreenCombination screens = it.next();
            if (screens != null && screens.hasHwSplitScreenStack(stack)) {
                List<ActivityStack> combinedStacks = screens.findCombinedSplitScreenStacks(stack);
                if (combinedStacks != null && !combinedStacks.isEmpty()) {
                    for (ActivityStack as : combinedStacks) {
                        if (!(as == null || as.getChildCount() <= 0 || as.getChildAt(0) == null)) {
                            combinedTaskIds.add(Integer.valueOf(as.getChildAt(0).taskId));
                        }
                    }
                }
            }
        }
        if (combinedTaskIds.isEmpty()) {
            return null;
        }
        int[] results = new int[combinedTaskIds.size()];
        for (int i = combinedTaskIds.size() - 1; i >= 0; i--) {
            results[i] = combinedTaskIds.get(i).intValue();
        }
        return results;
    }

    public void removeSplitScreenDividerBar(int windowMode, boolean immediately, int displayId) {
        HwSurfaceInNotch hwSurfaceInNotch;
        if (IS_HW_MULTIWINDOW_SUPPORTED) {
            HwMultiWindowSplitUI.getInstance(this.mService.mUiContext, this.mService, displayId).removeSplit(windowMode, immediately);
            this.mIsAddSplitBar = false;
            if (IS_NOTCH_PROP && (hwSurfaceInNotch = this.mSurfaceInNotch) != null) {
                hwSurfaceInNotch.remove();
            }
            if (WindowConfiguration.isHwSplitScreenWindowingMode(windowMode)) {
                this.mService.onMultiWindowModeChanged(false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addSplitScreenDividerBar(int displayId, int windowMode) {
        if (IS_HW_MULTIWINDOW_SUPPORTED) {
            HwMultiWindowSplitUI.getInstance(this.mService.mUiContext, this.mService, displayId).addDividerBarWindow(windowMode);
            this.mIsAddSplitBar = true;
            if (WindowConfiguration.isHwSplitScreenWindowingMode(windowMode)) {
                this.mService.onMultiWindowModeChanged(true);
            }
            if (IS_NOTCH_PROP && this.mSurfaceInNotch == null) {
                this.mSurfaceInNotch = new HwSurfaceInNotch(this.mService.mWindowManager.getDefaultDisplayContentLocked());
            }
        }
    }

    public void addSurfaceInNotchIfNeed() {
        int rotation;
        if (IS_NOTCH_PROP && this.mIsAddSplitBar && (rotation = this.mService.mWindowManager.getDefaultDisplayRotation()) != 0 && rotation != 2 && hasCutout(rotation) && this.mSurfaceInNotch != null) {
            ActivityStack stack = getSplitScreenPrimaryStack(this.mDisplayId);
            SurfaceControl surfaceControl = stack != null ? stack.mTaskStack.getSurfaceControl() : null;
            if (surfaceControl != null) {
                this.mSurfaceInNotch.show(rotation, surfaceControl);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasCutout(int rotation) {
        DisplayCutout cutout;
        DisplayContent displayContent = this.mService.mWindowManager.getDefaultDisplayContentLocked();
        if (displayContent == null || (cutout = displayContent.calculateDisplayCutoutForRotation(rotation).getDisplayCutout()) == null || cutout.isEmpty()) {
            return false;
        }
        return true;
    }

    public void focusStackChange(int currentUser, int displayId, ActivityStack currentFocusedStack, ActivityStack lastFocusedStack) {
        if (currentFocusedStack == null) {
            this.mUserId = currentUser;
            return;
        }
        if (IS_HW_MULTIWINDOW_SUPPORTED) {
            if (currentFocusedStack.inHwSplitScreenWindowingMode()) {
                addSplitScreenDividerBar(currentFocusedStack.mDisplayId, 100);
                setColorMgrInfo(true);
            } else if (currentFocusedStack.inHwFreeFormWindowingMode()) {
                List<TaskRecord> addTasks = null;
                List<ActivityStack> activityStacks = getForegroundFreeform(currentFocusedStack.getDisplay(), true);
                if (!currentFocusedStack.isAlwaysOnTop()) {
                    addTasks = new ArrayList<>();
                    for (int stackNum = 0; stackNum <= activityStacks.size() - 1; stackNum++) {
                        ActivityStack activityStack = activityStacks.get(stackNum);
                        activityStack.setAlwaysOnTopOnly(true);
                        addTasks.add(activityStack.topTask());
                    }
                } else {
                    for (int stackNum2 = 0; stackNum2 <= activityStacks.size() - 1; stackNum2++) {
                        ActivityStack stack = activityStacks.get(stackNum2);
                        if (!stack.isAlwaysOnTop()) {
                            addTasks = addTasks == null ? new ArrayList<>() : addTasks;
                            stack.setAlwaysOnTopOnly(true);
                            addTasks.add(stack.topTask());
                        }
                    }
                }
                if (addTasks != null && !addTasks.isEmpty()) {
                    this.mService.mHwATMSEx.dispatchFreeformBallLifeState(addTasks, "remove");
                }
                lambda$addStackReferenceIfNeeded$0$HwMultiWindowManager(currentFocusedStack.getDisplay());
                if (this.mUserId != currentUser) {
                    Slog.i(TAG, " mUserId = " + this.mUserId + " currentUser = " + currentUser);
                    removeSplitScreenDividerBar(100, currentFocusedStack.isHomeOrRecentsStack(), displayId);
                }
                this.mUserId = currentUser;
                setColorMgrInfo(true);
                return;
            } else {
                if (hasVisibleHwMultiStack(currentFocusedStack.getDisplay())) {
                    setColorMgrInfo(true);
                } else if (!this.mIsDockShowing) {
                    setColorMgrInfo(false);
                }
                int currentWindowMode = currentFocusedStack.getWindowingMode();
                if (currentWindowMode == 5 || currentWindowMode == 2) {
                    this.mUserId = currentUser;
                    return;
                }
                removeSplitScreenDividerBar(100, currentFocusedStack.isHomeOrRecentsStack(), displayId);
            }
        }
        this.mUserId = currentUser;
    }

    public void setColorMgrInfo(boolean isUseHdr) {
        if (isUseHdr != this.mIsUseHdr) {
            this.mIsUseHdr = isUseHdr;
            if (ActivityTaskManagerDebugConfig.DEBUG_HWFREEFORM) {
                Slog.w(TAG, "setColorMgrInfo:" + isUseHdr);
            }
            int[] iArr = new int[1];
            if (isUseHdr) {
                iArr[0] = 1;
            } else {
                iArr[0] = 0;
            }
            SurfaceControl.setColorMgrInfo(1, 0, iArr);
        }
    }

    private void initHwFreeformWindowParam(DisplayContent displayContent) {
        DisplayMetrics displayMetrics = displayContent.getDisplayMetrics();
        this.mCaptionViewHeight = WindowManagerService.dipToPixel(36, displayMetrics);
        this.mDargbarWidth = WindowManagerService.dipToPixel(70, displayMetrics);
    }

    public Rect relocateOffScreenWindow(Rect originalWindowBounds, ActivityStack stack, float scale) {
        Rect validInScreenWindowTopLeftLocation;
        ActivityDisplay activityDisplay = stack.getDisplay();
        if (activityDisplay == null) {
            Slog.w(TAG, "relocateOffScreenWindow: Invalid activityDisplay " + activityDisplay);
            return new Rect();
        }
        DisplayContent displayContent = activityDisplay.mDisplayContent;
        if (displayContent == null) {
            Slog.w(TAG, "relocateOffScreenWindow: Invalid displayContent " + displayContent);
            return new Rect();
        }
        initHwFreeformWindowParam(displayContent);
        int captionViewHeight = (int) (((float) this.mCaptionViewHeight) * stack.mTaskStack.mHwStackScale * scale);
        int dargbarWidth = (int) (((float) this.mDargbarWidth) * stack.mTaskStack.mHwStackScale * scale);
        int captionSpareWidth = (originalWindowBounds.width() - dargbarWidth) / 2;
        Rect stableRect = getStableRect(displayContent);
        if (isPhoneLandscape(displayContent)) {
            validInScreenWindowTopLeftLocation = new Rect(stableRect.left - captionSpareWidth, (this.mIsHasSideinScreen ? this.mSafeSideWidth : 0) + (this.mIsStatusBarPermenantlyShowing ? sStatusBarHeight : sStatusBarHeight / 2), (stableRect.right - captionSpareWidth) - dargbarWidth, (stableRect.bottom - captionViewHeight) - this.mGestureNavHotArea);
        } else {
            validInScreenWindowTopLeftLocation = new Rect(stableRect.left - captionSpareWidth, stableRect.top, (stableRect.right - captionSpareWidth) - dargbarWidth, (stableRect.bottom - captionViewHeight) - this.mGestureNavHotArea);
        }
        int validLeft = Math.min(Math.max(originalWindowBounds.left, validInScreenWindowTopLeftLocation.left), validInScreenWindowTopLeftLocation.right);
        int validTop = Math.min(Math.max(originalWindowBounds.top, validInScreenWindowTopLeftLocation.top), validInScreenWindowTopLeftLocation.bottom);
        if (!(originalWindowBounds.left == validLeft && originalWindowBounds.top == validTop)) {
            originalWindowBounds.offsetTo(validLeft, validTop);
        }
        return originalWindowBounds;
    }

    public Point getDragBarCenterPoint(Rect originalWindowBounds, ActivityStack stack) {
        ActivityDisplay activityDisplay = stack.getDisplay();
        if (activityDisplay == null) {
            Slog.w(TAG, "getDragBarCenterPoint: Invalid activityDisplay " + activityDisplay);
            return new Point();
        }
        DisplayContent displayContent = activityDisplay.mDisplayContent;
        if (displayContent == null) {
            Slog.w(TAG, "getDragBarCenterPoint: displayContent " + displayContent);
            return new Point();
        }
        initHwFreeformWindowParam(displayContent);
        return new Point(originalWindowBounds.left + (originalWindowBounds.width() / 2), originalWindowBounds.top + (this.mCaptionViewHeight / 2));
    }

    /* renamed from: limitForegroundFreeformStackCount */
    public void lambda$addStackReferenceIfNeeded$0$HwMultiWindowManager(ActivityDisplay activityDisplay) {
        synchronized (this.mService.getGlobalLock()) {
            if (activityDisplay == null) {
                Slog.i(TAG, "limitForegroundFreeformStackCount activityDisplay null");
                return;
            }
            int stackLimit = allowedForegroundFreeForms(activityDisplay.mDisplayId);
            int visibleStackCnt = 0;
            ActivityStack resultStack = null;
            int childCount = activityDisplay.getChildCount();
            int i = 1;
            for (int stackNdx = activityDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = activityDisplay.getChildAt(stackNdx);
                TaskRecord task = stack.topTask();
                ActivityRecord topActivity = stack.getTopActivity();
                if (stack.inHwFreeFormWindowingMode() && ((stack.isAlwaysOnTop() || stack.shouldBeVisible((ActivityRecord) null)) && task != null && topActivity != null && this.mService.mStackSupervisor.isCurrentProfileLocked(task.userId))) {
                    if (!applockSet.contains(Integer.valueOf(stack.getStackId()))) {
                        if (this.mPadCastStack != stack) {
                            if (isAllActivityTranslucentLocked(stack)) {
                                Slog.i(TAG, "all activities in stack is translucent");
                            } else if (stack.mIsTempStack) {
                                Slog.i(TAG, "limitForegroundCount:stack need to be coverd by same application stack");
                                resultStack = stack;
                            } else if (stackNdx == childCount - 1 || (task.getChildCount() != 0 && (task.getChildCount() != 1 || !task.getChildAt(0).finishing))) {
                                if (!stack.inHwPCMultiStackWindowingMode()) {
                                    visibleStackCnt++;
                                }
                            } else if (ActivityTaskManagerDebugConfig.DEBUG_HWFREEFORM) {
                                Slog.i(TAG, "task is finishing, not add to FreeformBall");
                            }
                        }
                    }
                }
            }
            List<TaskRecord> addTasks = new ArrayList<>(visibleStackCnt);
            int stackNdx2 = 0;
            while (true) {
                if (stackNdx2 > activityDisplay.getChildCount() - i) {
                    break;
                } else if (visibleStackCnt <= stackLimit) {
                    break;
                } else {
                    ActivityStack stack2 = activityDisplay.getChildAt(stackNdx2);
                    TaskRecord topTask = stack2.topTask();
                    ActivityRecord topActivity2 = stack2.getTopActivity();
                    if (stack2.inHwFreeFormWindowingMode() && ((stack2.isAlwaysOnTop() || stack2.shouldBeVisible((ActivityRecord) null)) && topTask != null && topActivity2 != null && this.mService.mStackSupervisor.isCurrentProfileLocked(topTask.userId))) {
                        if (!stack2.inHwPCMultiStackWindowingMode()) {
                            if (this.mPadCastStack != stack2) {
                                if (DOCKBAR_PACKAGE_NAME.equals(topTask.affinity)) {
                                    this.mService.mHwATMSEx.removeTask(topTask.taskId, (IBinder) null, (String) null, false, "limitForegroundFreeformStackCount");
                                } else if (stack2.isAlwaysOnTop()) {
                                    stack2.setAlwaysOnTop(false);
                                } else {
                                    activityDisplay.positionChildAtBottom(stack2);
                                }
                                addTasks.add(topTask);
                                visibleStackCnt--;
                            }
                        }
                    }
                    stackNdx2++;
                    i = 1;
                }
            }
            putSpecifiedStackToBottom(resultStack, activityDisplay, addTasks);
            if (!addTasks.isEmpty()) {
                this.mService.mHwATMSEx.dispatchFreeformBallLifeState(addTasks, "add");
            }
        }
    }

    private void putSpecifiedStackToBottom(ActivityStack resultStack, ActivityDisplay activityDisplay, List<TaskRecord> addTasks) {
        if (resultStack != null) {
            resultStack.mIsTempStack = false;
            if (resultStack.getTopActivity() != null && resultStack.topTask() != null) {
                if (resultStack.isAlwaysOnTop()) {
                    resultStack.setAlwaysOnTop(false);
                } else {
                    activityDisplay.positionChildAtBottom(resultStack);
                }
                addTasks.add(resultStack.topTask());
            }
        }
    }

    private void setFreeFromStackInvisible(List<ActivityStack> activityStacks, ActivityDisplay defaultDisplay) {
        List<TaskRecord> dispatchTasks = new ArrayList<>();
        for (int stackNum = activityStacks.size() - 1; stackNum >= 0; stackNum--) {
            ActivityStack activityStack = activityStacks.get(stackNum);
            ActivityRecord activityRecord = activityStack.getTopActivity();
            if (activityRecord != null) {
                this.mService.mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(activityRecord.appToken, true);
            }
            dispatchTasks.add(activityStack.topTask());
            activityStack.setAlwaysOnTop(false);
        }
        if (!dispatchTasks.isEmpty()) {
            this.mService.mHwATMSEx.dispatchFreeformBallLifeState(dispatchTasks, "add");
        }
        ActivityRecord topActivity = defaultDisplay.topRunningActivity();
        if (!(topActivity == null || !topActivity.isState(ActivityStack.ActivityState.RESUMED) || topActivity == this.mService.getLastResumedActivityRecord())) {
            this.mService.setResumedActivityUncheckLocked(topActivity, "onScreenDisplayModeChange");
        }
        this.mService.mStackSupervisor.mRootActivityContainer.ensureActivitiesVisible((ActivityRecord) null, 0, false);
        this.mService.mStackSupervisor.mRootActivityContainer.resumeFocusedStacksTopActivities();
    }

    private Set<Integer> setFreefromStackInvisible(ActivityDisplay activityDisplay, Set<Integer> stackIdSet) {
        Set<Integer> processedStackIdSet = new HashSet<>();
        for (int stackNdx = activityDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ActivityStack stack = activityDisplay.getChildAt(stackNdx);
            if (stack.getTaskStack().inHwFreeFormWindowingMode() && stack.getTaskStack().isAlwaysOnTop() && stackIdSet.contains(Integer.valueOf(stack.mStackId))) {
                ActivityRecord topActivity = stack.getTopActivity();
                if (topActivity != null) {
                    this.mService.mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(topActivity.appToken, true);
                }
                stack.setAlwaysOnTop(false);
                processedStackIdSet.add(Integer.valueOf(stack.mStackId));
            }
        }
        ActivityRecord topActivity2 = activityDisplay.topRunningActivity();
        if (!(topActivity2 == null || !topActivity2.isState(ActivityStack.ActivityState.RESUMED) || topActivity2 == this.mService.getLastResumedActivityRecord())) {
            this.mService.setResumedActivityUncheckLocked(topActivity2, "setFreefromStackInvisible");
        }
        return processedStackIdSet;
    }

    private Set<Integer> setFreeformStackVisible(ActivityDisplay activityDisplay, Set<Integer> stackIdSet) {
        Set<Integer> processedStackIdSet = new HashSet<>();
        for (int stackNdx = 0; stackNdx <= activityDisplay.getChildCount() - 1; stackNdx++) {
            ActivityStack stack = activityDisplay.getChildAt(stackNdx);
            if (stack.getTaskStack().inHwFreeFormWindowingMode() && !stack.getTaskStack().isAlwaysOnTop() && stackIdSet.contains(Integer.valueOf(stack.mStackId))) {
                stack.setAlwaysOnTop(true);
                processedStackIdSet.add(Integer.valueOf(stack.mStackId));
            }
        }
        return processedStackIdSet;
    }

    public int[] setFreeformStackVisibility(int displayId, int[] stackIdArray, boolean isVisible) {
        ActivityRecord topActivity;
        if (!IS_HW_MULTIWINDOW_SUPPORTED) {
            return new int[0];
        }
        synchronized (this.mService.getGlobalLock()) {
            int tempDisplayId = displayId;
            if (displayId == -1) {
                ActivityStack topStack = this.mService.mStackSupervisor.mRootActivityContainer.getTopDisplayFocusedStack();
                tempDisplayId = topStack != null ? topStack.mDisplayId : 0;
            }
            ActivityDisplay activityDisplay = ((ActivityTaskManagerService) this.mService).mStackSupervisor.mRootActivityContainer.getActivityDisplay(tempDisplayId);
            if (activityDisplay == null) {
                Slog.i(TAG, "setFreeformStackVisibility activityDisplay null");
                return new int[0];
            }
            List<ActivityStack> stackToBeMove = getFreeFromStackToChangeVisiblity(activityDisplay, stackIdArray, isVisible);
            List<Integer> processedStackIdList = new ArrayList<>();
            List<TaskRecord> dispatchTasks = new ArrayList<>();
            for (ActivityStack stack : stackToBeMove) {
                processedStackIdList.add(Integer.valueOf(stack.mStackId));
                if (!isVisible) {
                    ActivityRecord topActivity2 = stack.getTopActivity();
                    if (topActivity2 != null) {
                        this.mService.mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(topActivity2.appToken, true);
                    }
                    dispatchTasks.add(0, stack.topTask());
                } else {
                    dispatchTasks.add(stack.topTask());
                }
                if (isVisible || stack.isAlwaysOnTop()) {
                    stack.setAlwaysOnTop(isVisible);
                } else {
                    activityDisplay.positionChildAtBottom(stack);
                }
            }
            if (!dispatchTasks.isEmpty()) {
                this.mService.mHwATMSEx.dispatchFreeformBallLifeState(dispatchTasks, isVisible ? "remove" : "add");
            }
            if (!(stackIdArray == null || stackIdArray.length == 0)) {
                lambda$addStackReferenceIfNeeded$0$HwMultiWindowManager(activityDisplay);
            }
            int[] processedStackIdArray = new int[processedStackIdList.size()];
            if (processedStackIdList.size() != 0) {
                int i = 0;
                for (Integer num : processedStackIdList) {
                    processedStackIdArray[i] = num.intValue();
                    i++;
                }
            }
            if (!isVisible && (topActivity = activityDisplay.topRunningActivity()) != null && topActivity.isState(ActivityStack.ActivityState.RESUMED) && topActivity != this.mService.getLastResumedActivityRecord()) {
                this.mService.setResumedActivityUncheckLocked(topActivity, "setFreefromStackInvisible");
            }
            ((ActivityTaskManagerService) this.mService).mStackSupervisor.mRootActivityContainer.ensureActivitiesVisible((ActivityRecord) null, 0, false);
            this.mService.mStackSupervisor.mRootActivityContainer.resumeFocusedStacksTopActivities();
            return processedStackIdArray;
        }
    }

    private List<ActivityStack> getFreeFromStackToChangeVisiblity(ActivityDisplay activityDisplay, int[] stackIdArray, boolean isVisible) {
        int stackLimit;
        List<ActivityStack> stackToBeMove = new ArrayList<>();
        boolean isCalledFromDock = false;
        if (stackIdArray == null || stackIdArray.length == 0) {
            int size = activityDisplay.getChildCount();
            int visibleCnt = 0;
            for (int stackNdx = size - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = activityDisplay.getChildAt(stackNdx);
                if (!stack.inMultiWindowMode()) {
                    if (!handleGuideActivity(stack)) {
                        break;
                    }
                    isCalledFromDock = true;
                } else if (stack.inHwPCMultiStackWindowingMode()) {
                    ActivityRecord ar = stack.getTopActivity();
                    if (ar != null) {
                        this.mService.mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(ar.appToken, true);
                    }
                } else if (stack.inHwFreeFormWindowingMode() && (stack.isAlwaysOnTop() || isCalledFromDock)) {
                    if (!isVisible) {
                        stackToBeMove.add(stack);
                    }
                    visibleCnt++;
                }
            }
            if (isVisible && visibleCnt < (stackLimit = allowedForegroundFreeForms(activityDisplay.mDisplayId))) {
                for (int stackNdx2 = 0; stackNdx2 < size; stackNdx2++) {
                    ActivityStack stack2 = activityDisplay.getChildAt(stackNdx2);
                    if (!stack2.inMultiWindowMode() && visibleCnt >= stackLimit) {
                        break;
                    }
                    if (stack2.inHwFreeFormWindowingMode() && !stack2.isAlwaysOnTop()) {
                        stackToBeMove.add(stack2);
                        visibleCnt++;
                    }
                }
            }
        } else {
            stackToBeMove = new ArrayList<>();
            for (int stackId : stackIdArray) {
                ActivityStack stack3 = activityDisplay.getStack(stackId);
                if (stack3 != null && stack3.inHwFreeFormWindowingMode()) {
                    if (!stack3.isAlwaysOnTop() && isVisible) {
                        stackToBeMove.add(0, stack3);
                    } else if (stack3.isAlwaysOnTop() && !isVisible) {
                        stackToBeMove.add(0, stack3);
                    }
                }
            }
        }
        return stackToBeMove;
    }

    private boolean handleGuideActivity(ActivityStack stack) {
        ActivityRecord activityRecord = stack.getTopActivity();
        if (activityRecord == null || activityRecord.intent == null || activityRecord.intent.getComponent() == null || !"com.huawei.hwdockbar/com.huawei.hwdockbar.floatwindowboots.FloatWindowBootsActivity".equals(activityRecord.intent.getComponent().flattenToString())) {
            return false;
        }
        return true;
    }

    public Bundle getSplitGearsByDisplayId(int displayId) {
        return getSplitGearsByDisplay(this.mService.getRootActivityContainer().getActivityDisplay(displayId));
    }

    private void loadDimens(int displayId) {
        sDividerWindowWidth = this.mService.mUiContext.getResources().getDimensionPixelSize(34472582);
        sStatusBarHeight = this.mService.mUiContext.getResources().getDimensionPixelSize(17105445);
        sNavigationBarWidth = this.mService.mUiContext.getResources().getDimensionPixelSize(17105312);
        sNavigationBarHeight = this.mService.mUiContext.getResources().getDimensionPixelSize(17105307);
        sFreeformCornerRadius = (float) this.mService.mUiContext.getResources().getDimensionPixelSize(34472569);
        this.mIsHasSideinScreen = HwDisplaySizeUtil.hasSideInScreen();
        this.mSafeSideWidth = HwDisplaySizeUtil.getInstance(this.mService.mWindowManager).getSafeSideWidth();
        sWindowOffset = (sDividerWindowWidth + this.mService.mUiContext.getResources().getDimensionPixelSize(34472566)) / 2;
        this.mGestureNavHotArea = this.mService.mUiContext.getResources().getDimensionPixelSize(34472556);
    }

    private static float getDensityDpiWithoutRog() {
        int srcDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
        int dpi = SystemProperties.getInt("persist.sys.dpi", srcDpi);
        int rogDpi = SystemProperties.getInt("persist.sys.realdpi", srcDpi);
        if (dpi <= 0) {
            dpi = srcDpi;
        }
        if (rogDpi <= 0) {
            rogDpi = srcDpi;
        }
        return ((((float) srcDpi) * 1.0f) * ((float) rogDpi)) / ((float) dpi);
    }

    private int dipToPixelWithoutRog(int dip, float densityDpiWithoutRog) {
        return (int) ((((float) dip) * densityDpiWithoutRog) / 160.0f);
    }

    private int calcDefaultFreeFormNum(DisplayContent displayContent) {
        if (displayContent == null) {
            return 0;
        }
        int displayWidth = displayContent.getDisplayInfo().logicalWidth;
        int displayHeight = displayContent.getDisplayInfo().logicalHeight;
        if (getColumnsByWidth((int) (((float) (Math.min(displayWidth, displayHeight) * 160)) / getDensityDpiWithoutRog())) <= 4) {
            return 1;
        }
        return 2;
    }

    /* JADX INFO: Multiple debug info for r7v4 'scaleRatio'  float: [D('freeFormWidth' int), D('scaleRatio' float)] */
    /* access modifiers changed from: package-private */
    public Bundle getFreeformBoundsInCenter(int displayId, int centerX) {
        float scaleRatio;
        int displayHeight;
        int displayWidth;
        int freeFormWidth;
        int freeFormWidth2;
        float scaleRatio2;
        int topStart;
        ActivityDisplay activityDisplay = this.mService.getRootActivityContainer().getActivityDisplay(displayId);
        if (activityDisplay == null) {
            return null;
        }
        if (activityDisplay.mDisplayContent == null) {
            return null;
        }
        DisplayContent displayContent = activityDisplay.mDisplayContent.mDisplayContent;
        int displayWidth2 = displayContent.getDisplayInfo().logicalWidth;
        int displayHeight2 = displayContent.getDisplayInfo().logicalHeight;
        Rect outVisualBounds = new Rect();
        Rect outBounds = new Rect();
        if (allowedForegroundFreeForms(displayContent.mDisplayId) == sForegroundFreeFormStack) {
            float densityWithoutRog = getDensityDpiWithoutRog();
            int widthColumns = getColumnsByWidth((int) (((float) (displayWidth2 * 160)) / densityWithoutRog));
            int heightColumns = getColumnsByWidth((int) (((float) (displayHeight2 * 160)) / densityWithoutRog));
            int minSide = Math.min(displayWidth2, displayHeight2);
            int gutterPixel = dipToPixelWithoutRog(24, densityWithoutRog);
            dipToPixelWithoutRog(24, densityWithoutRog);
            initHwFreeformWindowParam(displayContent);
            int freeFormHeight = 0;
            int topStart2 = 0;
            if (widthColumns == 4 && heightColumns > 4) {
                int freeFormWidth3 = (int) (((float) displayWidth2) * 0.79f);
                freeFormHeight = (int) ((((float) this.mCaptionViewHeight) * 0.79f) + ((((float) freeFormWidth3) * 4.575f) / 3.0f));
                displayWidth = displayWidth2;
                displayHeight = displayHeight2;
                freeFormWidth = freeFormWidth3;
                freeFormWidth2 = 0;
                scaleRatio2 = 0.79f;
            } else if (widthColumns > 4 && heightColumns == 4) {
                int sideWidth = this.mIsHasSideinScreen ? this.mSafeSideWidth : 0;
                int i = sStatusBarHeight;
                freeFormHeight = (displayHeight2 - (i * 2)) - (sideWidth * 2);
                int freeFormWidth4 = (int) (((float) freeFormHeight) / (((((float) this.mCaptionViewHeight) * 1.0f) / ((float) displayHeight2)) + 1.3333334f));
                scaleRatio2 = (((float) freeFormWidth4) * 1.0f) / ((float) displayHeight2);
                int topPosition = (displayHeight2 - freeFormHeight) / 2;
                if (this.mIsStatusBarPermenantlyShowing) {
                    topStart = Math.max(i + sideWidth, topPosition);
                } else {
                    topStart = Math.max((i / 2) + sideWidth, topPosition);
                }
                if (!displayContent.getDisplayPolicy().mHwDisplayPolicyEx.isNaviBarMini()) {
                    topStart2 = topStart;
                    int displayRotation = displayContent.mDisplayInfo.rotation;
                    if (displayRotation == 1) {
                        displayWidth2 -= sNavigationBarWidth;
                    } else if (displayRotation == 3) {
                        displayWidth2 -= sNavigationBarWidth + sStatusBarHeight;
                    }
                } else {
                    topStart2 = topStart;
                }
                displayWidth = displayWidth2;
                freeFormWidth = freeFormWidth4;
                freeFormWidth2 = sideWidth;
                displayHeight = freeFormHeight;
            } else if (widthColumns == 8 && heightColumns == 8) {
                int freeFormWidth5 = ((minSide / 2) - dipToPixelWithoutRog(32, densityWithoutRog)) - (gutterPixel / 2);
                float scaleRatio3 = (((float) freeFormWidth5) * 1.0f) / ((float) HwFoldScreenState.getScreenPhysicalRect(2).width());
                freeFormHeight = (int) ((((((float) freeFormWidth5) * 1.0f) * 16.0f) / 9.0f) + (((float) this.mCaptionViewHeight) * scaleRatio3));
                displayWidth = displayWidth2;
                displayHeight = displayHeight2;
                scaleRatio2 = scaleRatio3;
                freeFormWidth2 = 0;
                freeFormWidth = freeFormWidth5;
            } else if ((widthColumns == 8 && heightColumns == 12) || (widthColumns == 12 && heightColumns == 8)) {
                dipToPixelWithoutRog(32, densityWithoutRog);
                int freeFormWidth6 = (int) (((((float) ((minSide - (sStatusBarHeight * 2)) * 9)) * 1.0f) / 18.0f) * INIT_SCALE_RATIO_FOR_PAD);
                displayWidth = displayWidth2;
                displayHeight = displayHeight2;
                float scaleRatio4 = (((float) freeFormWidth6) * 1.0f) / ((((float) Math.max(displayWidth2, displayHeight2)) * 1.0f) / 3.0f);
                freeFormHeight = (int) ((((((float) freeFormWidth6) * 1.0f) / 9.0f) * 18.0f) + (((float) this.mCaptionViewHeight) * scaleRatio4));
                freeFormWidth = freeFormWidth6;
                freeFormWidth2 = 0;
                scaleRatio2 = scaleRatio4;
            } else {
                displayWidth = displayWidth2;
                displayHeight = displayHeight2;
                freeFormWidth2 = 0;
                scaleRatio2 = 1.0f;
                freeFormWidth = 0;
            }
            outBounds.set(0, 0, freeFormWidth, freeFormHeight);
            scaleRatio = scaleRatio2;
            moveBoundsToCenter(outBounds, displayWidth, displayHeight, centerX, topStart2);
            outVisualBounds.set(outBounds);
            adjustTopStartToAnimation(outVisualBounds, freeFormWidth2);
            calcScaledWindowBounds(outBounds, scaleRatio);
        } else {
            scaleRatio = 1.0f;
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_HWFREEFORM) {
            Slog.i(TAG, "freeform center outBounds:" + outBounds + ", outVisualBounds:" + outVisualBounds + ", scaleRatio:" + scaleRatio);
        }
        Bundle bundle = new Bundle();
        bundle.putFloat(HW_FREEFORM_CENTER_SCALE_RATIO, scaleRatio);
        bundle.putParcelable(HW_FREEFORM_CENTER_BOUNDS, outBounds);
        bundle.putParcelable(HW_FREEFORM_CENTER_VISUAL_BOUNDS, outVisualBounds);
        return bundle;
    }

    private void adjustTopStartToAnimation(Rect outBounds, int sideWidth) {
        if (outBounds != null) {
            int freeFormHeight = outBounds.height();
            outBounds.top -= sideWidth;
            outBounds.bottom = outBounds.top + freeFormHeight;
        }
    }

    private void moveBoundsToCenter(Rect outBounds, int displayWidth, int displayHeight, int centerX, int topStart) {
        int freeFormWidth = outBounds.width();
        int freeFormHeight = outBounds.height();
        if (freeFormWidth / 2 >= centerX || centerX >= displayWidth - (freeFormWidth / 2)) {
            outBounds.left = (displayWidth - freeFormWidth) / 2;
        } else {
            outBounds.left = centerX - (freeFormWidth / 2);
        }
        outBounds.right = outBounds.left + freeFormWidth;
        outBounds.top = ((displayHeight - freeFormHeight) / 2) + topStart;
        outBounds.bottom = outBounds.top + freeFormHeight;
    }

    private void calcScaledWindowBounds(Rect outBounds, float scaleRatio) {
        if (outBounds != null) {
            int left = outBounds.left;
            int top = outBounds.top;
            outBounds.scale(1.0f / scaleRatio);
            outBounds.offsetTo(left, top);
        }
    }

    /* JADX INFO: Multiple debug info for r3v21 int: [D('left' int), D('freeFormHeight' int)] */
    /* JADX INFO: Multiple debug info for r3v37 int: [D('left' int), D('freeFormHeight' int)] */
    /* access modifiers changed from: package-private */
    public void calcDefaultFreeFormBounds(Rect outBounds, ActivityStack stack, boolean isVisual, boolean isSetStackRatio) {
        if (stack.getDisplay() != null && stack.getDisplay().mDisplayContent != null && outBounds != null) {
            DisplayContent displayContent = stack.getDisplay().mDisplayContent;
            int displayWidth = displayContent.getDisplayInfo().logicalWidth;
            int displayHeight = displayContent.getDisplayInfo().logicalHeight;
            if (allowedForegroundFreeForms(displayContent.mDisplayId) == sForegroundFreeFormStack) {
                float densityWithoutRog = getDensityDpiWithoutRog();
                int widthColumns = getColumnsByWidth((int) (((float) (displayWidth * 160)) / densityWithoutRog));
                int heightColumns = getColumnsByWidth((int) (((float) (displayHeight * 160)) / densityWithoutRog));
                int minSide = Math.min(displayWidth, displayHeight);
                int gutterPixel = dipToPixelWithoutRog(24, densityWithoutRog);
                int marginPixel = dipToPixelWithoutRog(24, densityWithoutRog);
                initHwFreeformWindowParam(displayContent);
                Rect stableRect = getStableRect(displayContent);
                if (widthColumns == 4 && heightColumns > 4) {
                    int defaultWidth = (int) (((float) displayWidth) * 0.79f);
                    outBounds.left = (displayWidth - defaultWidth) / 2;
                    outBounds.right = (displayWidth + defaultWidth) / 2;
                    int freeFormHeight = (int) ((((float) this.mCaptionViewHeight) * 0.79f) + ((((float) outBounds.width()) * 4.575f) / 3.0f));
                    outBounds.top = (displayHeight - freeFormHeight) / 2;
                    outBounds.bottom = outBounds.top + freeFormHeight;
                    if (!isVisual) {
                        this.ratio = 0.79f;
                        if (stack.inHwFreeFormWindowingMode() && isSetStackRatio) {
                            stack.mTaskStack.mHwStackScale = this.ratio;
                        }
                        int freeFormHeight2 = outBounds.left;
                        int top = outBounds.top;
                        outBounds.scale(1.0f / this.ratio);
                        outBounds.offsetTo(freeFormHeight2, top);
                    }
                } else if (widthColumns > 4 && heightColumns == 4) {
                    int sideWidth = this.mIsHasSideinScreen ? this.mSafeSideWidth : 0;
                    outBounds.right = stableRect.right - marginPixel;
                    int freeFormHeight3 = (displayHeight - (sStatusBarHeight * 2)) - (sideWidth * 2);
                    int topPosition = (displayHeight - freeFormHeight3) / 2;
                    outBounds.left = outBounds.right - ((int) (((float) freeFormHeight3) / (((((float) this.mCaptionViewHeight) * 1.0f) / ((float) displayHeight)) + 1.3333334f)));
                    if (this.mIsStatusBarPermenantlyShowing) {
                        outBounds.top = Math.max(sStatusBarHeight + sideWidth, topPosition);
                    } else {
                        outBounds.top = Math.max((sStatusBarHeight / 2) + sideWidth, topPosition);
                    }
                    outBounds.bottom = outBounds.top + freeFormHeight3;
                    if (!isVisual) {
                        this.ratio = (((float) outBounds.width()) * 1.0f) / ((float) displayHeight);
                        if (stack.inHwFreeFormWindowingMode() && isSetStackRatio) {
                            stack.mTaskStack.mHwStackScale = this.ratio;
                        }
                        int left = outBounds.left;
                        int top2 = outBounds.top;
                        outBounds.scale(1.0f / this.ratio);
                        outBounds.offsetTo(left, top2);
                    }
                } else if (widthColumns == 8 && heightColumns == 8) {
                    int marginPixel2 = dipToPixelWithoutRog(32, densityWithoutRog);
                    int freeFormWidth = ((minSide / 2) - marginPixel2) - (gutterPixel / 2);
                    outBounds.right = stableRect.right - marginPixel2;
                    outBounds.left = outBounds.right - freeFormWidth;
                    float tempScale = (((float) outBounds.width()) * 1.0f) / ((float) HwFoldScreenState.getScreenPhysicalRect(2).width());
                    int freeFormHeight4 = (int) ((((((float) freeFormWidth) * 1.0f) * 16.0f) / 9.0f) + (((float) this.mCaptionViewHeight) * tempScale));
                    outBounds.top = (displayHeight - freeFormHeight4) / 2;
                    outBounds.bottom = outBounds.top + freeFormHeight4;
                    if (!isVisual) {
                        this.ratio = tempScale;
                        if (stack.inHwFreeFormWindowingMode() && isSetStackRatio) {
                            stack.mTaskStack.mHwStackScale = this.ratio;
                        }
                        int freeFormHeight5 = outBounds.left;
                        int top3 = outBounds.top;
                        outBounds.scale(1.0f / this.ratio);
                        outBounds.offsetTo(freeFormHeight5, top3);
                    }
                } else if ((widthColumns == 8 && heightColumns == 12) || (widthColumns == 12 && heightColumns == 8)) {
                    int marginPixel3 = dipToPixelWithoutRog(32, densityWithoutRog);
                    int freeFormWidth2 = (int) (((((float) ((minSide - (sStatusBarHeight * 2)) * 9)) * 1.0f) / 18.0f) * INIT_SCALE_RATIO_FOR_PAD);
                    outBounds.right = stableRect.right - marginPixel3;
                    outBounds.left = outBounds.right - freeFormWidth2;
                    float tempScale2 = (((float) freeFormWidth2) * 1.0f) / ((((float) Math.max(displayWidth, displayHeight)) * 1.0f) / 3.0f);
                    int freeFormHeight6 = (int) ((((((float) freeFormWidth2) * 1.0f) / 9.0f) * 18.0f) + (((float) this.mCaptionViewHeight) * tempScale2));
                    outBounds.top = (displayHeight - freeFormHeight6) / 2;
                    outBounds.bottom = outBounds.top + freeFormHeight6;
                    if (!isVisual) {
                        this.ratio = tempScale2;
                        if (stack.inHwFreeFormWindowingMode() && isSetStackRatio) {
                            stack.mTaskStack.mHwStackScale = this.ratio;
                        }
                        int left2 = outBounds.left;
                        int top4 = outBounds.top;
                        outBounds.scale(1.0f / this.ratio);
                        outBounds.offsetTo(left2, top4);
                    }
                }
            }
            this.defaultBound = outBounds;
        }
    }

    private int allowedForegroundFreeForms(int displayId) {
        if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId)) {
            return sForegroundFreeFormStack;
        }
        return 8;
    }

    public int getNavBarBoundOnScreen(DisplayContent displayContent, Rect outBound) {
        int naviPos = -1;
        if (!displayContent.getDisplayPolicy().mHwDisplayPolicyEx.isNaviBarMini()) {
            int displayWidth = displayContent.getDisplayInfo().logicalWidth;
            int displayHeight = displayContent.getDisplayInfo().logicalHeight;
            naviPos = displayContent.getDisplayPolicy().navigationBarPosition(displayWidth, displayHeight, displayContent.getRotation());
            if (naviPos == 4) {
                outBound.left = 0;
                outBound.top = displayHeight - sNavigationBarHeight;
                outBound.right = displayWidth;
                outBound.bottom = displayHeight;
            } else if (naviPos == 2) {
                outBound.left = displayWidth - sNavigationBarWidth;
                outBound.top = 0;
                outBound.right = displayWidth;
                outBound.bottom = displayHeight;
            } else if (naviPos == 1) {
                outBound.left = 0;
                outBound.top = 0;
                outBound.right = sNavigationBarWidth;
                outBound.bottom = sNavigationBarHeight;
            } else {
                outBound.left = 0;
                outBound.top = 0;
                outBound.right = 0;
                outBound.bottom = 0;
            }
        }
        return naviPos;
    }

    public int getNotchBoundOnScreen(DisplayContent displayContent, Rect outBound) {
        if (displayContent == null) {
            Slog.w(TAG, "getNotchBoundOnScreen failed, cause displayContent is null!");
            return -1;
        } else if (outBound == null) {
            Slog.w(TAG, "getNotchBoundOnScreen failed, cause outBound is null!");
            return -1;
        } else {
            int displayWidth = displayContent.getDisplayInfo().logicalWidth;
            if (!IS_NOTCH_PROP) {
                Bundle bundle = getSplitGearsByDisplayId(displayContent.getDisplayId());
                if (bundle == null || bundle.getInt(HW_SPLIT_SCREEN_PRIMARY_POSITION) != 0) {
                    return -1;
                }
                outBound.set(0, 0, displayWidth, sStatusBarHeight);
                return 0;
            }
            int rotation = ((DisplayInfo) displayContent.getDisplayInfo()).rotation;
            int displayHeight = displayContent.getDisplayInfo().logicalHeight;
            DisplayCutout cutout = displayContent.calculateDisplayCutoutForRotation(rotation).getDisplayCutout();
            if (cutout == null || cutout.isEmpty()) {
                Slog.w(TAG, "getNotchBoundOnScreen failed, cause cutout is null!");
                return -1;
            } else if (rotation == 0) {
                outBound.set(0, 0, displayWidth, sStatusBarHeight);
                return 0;
            } else if (rotation == 1) {
                outBound.set(0, 0, cutout.getSafeInsetLeft(), displayHeight);
                return 1;
            } else if (rotation == 3) {
                outBound.set(displayWidth - cutout.getSafeInsetRight(), 0, displayWidth, displayHeight);
                return 2;
            } else {
                outBound.setEmpty();
                return -1;
            }
        }
    }

    private Rect getStableRect(DisplayContent displayContent) {
        int i;
        int displayWidth = displayContent.getDisplayInfo().logicalWidth;
        int displayHeight = displayContent.getDisplayInfo().logicalHeight;
        Rect stableRect = new Rect(0, 0, displayWidth, displayHeight);
        if (displayContent.mDisplayInfo.displayCutout != null) {
            i = Math.max(displayContent.mDisplayInfo.displayCutout.getSafeInsetTop(), sStatusBarHeight);
        } else {
            i = sStatusBarHeight;
        }
        stableRect.top = i;
        if (!displayContent.getDisplayPolicy().mHwDisplayPolicyEx.isNaviBarMini()) {
            int naviPos = displayContent.getDisplayPolicy().navigationBarPosition(displayWidth, displayHeight, displayContent.mDisplayInfo.rotation);
            if (naviPos == 4) {
                stableRect.bottom -= sNavigationBarHeight;
            } else if (naviPos == 2) {
                stableRect.right -= sNavigationBarWidth;
            }
        }
        if (IS_NOTCH_PROP && hasCutout(displayContent.mDisplayInfo.rotation) && displayContent.mDisplayInfo.displayCutout != null) {
            stableRect.bottom -= displayContent.mDisplayInfo.displayCutout.getSafeInsetBottom();
            stableRect.right -= displayContent.mDisplayInfo.displayCutout.getSafeInsetRight();
            stableRect.left += displayContent.mDisplayInfo.displayCutout.getSafeInsetLeft();
        }
        return stableRect;
    }

    public void updateDragFreeFormPos(ActivityStack stack) {
        if (stack == null) {
            Slog.w(TAG, "updateFreeformPos failed, cause stack is null!");
        } else if (stack.inHwFreeFormWindowingMode()) {
            if (stack.isPendingShow()) {
                Slog.w(TAG, "no need to update position when stack is pendingshow!");
            } else if (this.mService.mStartFromSelector == 1) {
                Slog.w(TAG, "no need to update position, cause start from recent!");
            } else if (stack.getDisplay() != null && allowedForegroundFreeForms(stack.getDisplay().mDisplayId) != 1) {
                List<ActivityStack> activityStacks = getForegroundFreeform(stack.getDisplay(), false);
                if (activityStacks.size() != 0) {
                    ActivityStack foreStack = activityStacks.get(0);
                    if (foreStack.mStackId == stack.mStackId) {
                        if (activityStacks.size() != 1) {
                            foreStack = activityStacks.get(1);
                        } else {
                            return;
                        }
                    }
                    if (applockSet.contains(Integer.valueOf(foreStack.mStackId))) {
                        Slog.w(TAG, "no need to update freeformpos for applock");
                    } else {
                        checkFreeformPos(foreStack.getBounds(), foreStack, stack.getBounds(), stack);
                    }
                }
            }
        }
    }

    private void updatePosForConfigChange(ActivityStack stack) {
        if (allowedForegroundFreeForms(stack.getDisplay().mDisplayId) != 1) {
            List<ActivityStack> activityStacks = getForegroundFreeform(stack.getDisplay(), false);
            if (activityStacks.size() > 1) {
                ActivityStack topStack = activityStacks.get(0);
                if (topStack.mStackId != stack.mStackId) {
                    Rect bounds = new Rect(topStack.getBounds());
                    checkFreeformPos(bounds, stack, bounds, topStack);
                }
            }
        }
    }

    private void checkFreeformPos(Rect oldBounds, ActivityStack foreStack, Rect toStartStackBounds, ActivityStack stack) {
        if (!isPadCastStack(stack)) {
            if (oldBounds == null || oldBounds.isEmpty() || toStartStackBounds == null || toStartStackBounds.isEmpty()) {
                Slog.w(TAG, "updateFreeformPos failed, cause bounds is null!");
            } else if (stack.getDisplay() != null) {
                Rect newBounds = new Rect(toStartStackBounds);
                if (Math.abs(((((float) newBounds.height()) * 1.0f) / ((float) newBounds.width())) - ((((float) oldBounds.height()) * 1.0f) / ((float) oldBounds.width()))) > 1.0E-5f) {
                    calcDefaultFreeFormBounds(newBounds, stack, false, true);
                }
                int left = oldBounds.left;
                int top = oldBounds.top;
                oldBounds.scale(foreStack.getTaskStack().mHwStackScale);
                oldBounds.offsetTo(left, top);
                int left2 = newBounds.left;
                int top2 = newBounds.top;
                int width = newBounds.width();
                int height = newBounds.height();
                newBounds.scale(stack.getTaskStack().mHwStackScale);
                newBounds.offsetTo(left2, top2);
                float densityWithoutRog = getDensityDpiWithoutRog();
                int overlap = dipToPixelWithoutRog(80, densityWithoutRog);
                if (Math.min(oldBounds.right, newBounds.right) - Math.max(oldBounds.left, newBounds.left) <= oldBounds.width() - overlap) {
                    return;
                }
                if (Math.min(oldBounds.bottom, newBounds.bottom) - Math.max(oldBounds.top, newBounds.top) > oldBounds.height() - overlap) {
                    int displayWidth = stack.getDisplay().mDisplayContent.getDisplayInfo().logicalWidth;
                    int gutterPixel = dipToPixelWithoutRog(24, densityWithoutRog);
                    int spaceWidth = newBounds.width() + gutterPixel;
                    Rect calcBounds = new Rect();
                    if (oldBounds.left >= spaceWidth) {
                        calcBounds.set(oldBounds.left - spaceWidth, newBounds.top, (oldBounds.left - spaceWidth) + newBounds.width(), newBounds.bottom);
                    } else if (displayWidth - oldBounds.right >= spaceWidth) {
                        calcBounds.set(oldBounds.right + gutterPixel, newBounds.top, oldBounds.right + gutterPixel + newBounds.width(), newBounds.bottom);
                    } else if (displayWidth - oldBounds.right < spaceWidth) {
                        if (oldBounds.left > displayWidth - oldBounds.right) {
                            calcBounds.set(gutterPixel, newBounds.top, newBounds.width() + gutterPixel, newBounds.bottom);
                        } else {
                            calcBounds.set((displayWidth - gutterPixel) - newBounds.width(), newBounds.top, displayWidth - gutterPixel, newBounds.bottom);
                        }
                    }
                    calcBounds.set(relocateOffScreenWindow(calcBounds, stack, 1.0f));
                    if (!calcBounds.isEmpty()) {
                        int left3 = calcBounds.left;
                        int top3 = calcBounds.top;
                        calcBounds.set(left3, top3, left3 + width, top3 + height);
                        stack.resize(calcBounds, (Rect) null, (Rect) null);
                    }
                }
            }
        }
    }

    public int getPrimaryStackPos(int displayId) {
        Rect leftBounds;
        synchronized (this.mService.getGlobalLock()) {
            HwMultiWindowSplitUI splitUI = HwMultiWindowSplitUI.getInstance(this.mService.getUiContext(), this.mService, displayId);
            ActivityStack topStack = getSplitScreenTopStack(displayId);
            if (topStack == null || !topStack.inHwMagicWindowingMode() || (leftBounds = getLeftBoundsForMagicWindow(topStack)) == null) {
                ActivityDisplay display = this.mService.getRootActivityContainer().getActivityDisplay(displayId);
                if (display != null) {
                    ActivityStack stackPrimary = null;
                    ArrayList<ActivityStack> stacks = display.getAllStacksInWindowingMode(100);
                    int i = stacks.size() - 1;
                    while (true) {
                        if (i < 0) {
                            break;
                        }
                        ActivityStack current = stacks.get(i);
                        if (current.getWindowingMode() == 100 && current.getVisibility((ActivityRecord) null) == 0) {
                            stackPrimary = current;
                            break;
                        }
                        i--;
                    }
                    if (stackPrimary != null) {
                        Bundle bundle = getSplitGearsByDisplay(display);
                        int splitRatio = 0;
                        float[] splitRatios = bundle.getFloatArray(HW_SPLIT_SCREEN_RATIO_VALUES);
                        for (HwSplitScreenCombination combination : this.mHwSplitScreenCombinations) {
                            if (combination.hasHwSplitScreenStack(stackPrimary) && splitRatios != null && splitRatios.length > 1) {
                                splitRatio = combination.mSplitRatio;
                            }
                        }
                        Rect primaryOutBounds = new Rect();
                        calcHwSplitStackBounds(display, splitRatio, primaryOutBounds, (Rect) null);
                        splitUI.primaryBounds = primaryOutBounds;
                        int primaryPos = bundle.getInt(HW_SPLIT_SCREEN_PRIMARY_POSITION, 0);
                        if (primaryPos == 0) {
                            return primaryOutBounds.bottom;
                        }
                        if (primaryPos == 1) {
                            return primaryOutBounds.right;
                        }
                        return 0;
                    }
                }
                return 0;
            }
            splitUI.primaryBounds = leftBounds;
            return leftBounds.right;
        }
    }

    public Bundle getSplitStacksPos(int displayId, int splitRatio) {
        synchronized (this.mService.getGlobalLock()) {
            ActivityDisplay display = this.mService.getRootActivityContainer().getActivityDisplay(displayId);
            if (display == null) {
                return null;
            }
            Rect primaryOutBounds = new Rect();
            Rect secondaryOutBounds = new Rect();
            calcHwSplitStackBounds(display, splitRatio, primaryOutBounds, secondaryOutBounds);
            Bundle bundle = new Bundle();
            bundle.putParcelable(HW_SPLIT_SCREEN_PRIMARY_BOUNDS, primaryOutBounds);
            bundle.putParcelable(HW_SPLIT_SCREEN_SECONDARY_BOUNDS, secondaryOutBounds);
            bundle.putInt(HW_SPLIT_SCREEN_PRIMARY_POSITION, getSplitGearsByDisplay(display).getInt(HW_SPLIT_SCREEN_PRIMARY_POSITION, 0));
            return bundle;
        }
    }

    public void setSplitBarVisibility(boolean isVisibility) {
        HwMultiWindowSplitUI.getInstance(this.mService.mUiContext, this.mService, this.mDisplayId).setSplitBarVisibility(isVisibility);
    }

    public Rect[] getRectForScreenShotForDrag(int splitRatio, int displayId) {
        ActivityStack activityStack = getSplitScreenTopStack(displayId);
        if (activityStack == null || activityStack.getDisplay() == null) {
            return null;
        }
        Rect[] dragBounds = {new Rect(), new Rect()};
        calcHwSplitStackBounds(activityStack.getDisplay(), splitRatio, dragBounds[0], dragBounds[1]);
        return dragBounds;
    }

    public int getHwSplitScreenRatio(ActivityStack stack) {
        for (int i = this.mHwSplitScreenCombinations.size() - 1; i >= 0; i--) {
            HwSplitScreenCombination screenCombination = this.mHwSplitScreenCombinations.get(i);
            if (screenCombination.isSplitScreenCombined() && screenCombination.hasHwSplitScreenStack(stack)) {
                return screenCombination.mSplitRatio;
            }
        }
        return 0;
    }

    public boolean isSplitStackVisible(ActivityDisplay display, int primaryPosition) {
        if (!IS_HW_MULTIWINDOW_SUPPORTED) {
            return false;
        }
        synchronized (this.mService.getGlobalLock()) {
            if (this.mHwSplitScreenCombinations.size() > 0) {
                if (display != null) {
                    for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                        ActivityStack stack = display.getChildAt(stackNdx);
                        if (!stack.inHwMultiWindowingMode() && ((stack.isHomeOrRecentsStack() || !stack.isStackTranslucent((ActivityRecord) null)) && !stack.inPinnedWindowingMode())) {
                            return false;
                        }
                        if (stack.inHwSplitScreenWindowingMode() && stack.mTaskStack != null && stack.mTaskStack.isVisible() && (primaryPosition == -1 || primaryPosition == getSplitGearsByDisplay(display).getInt(HW_SPLIT_SCREEN_PRIMARY_POSITION, 0))) {
                            return true;
                        }
                    }
                    return false;
                }
            }
            return false;
        }
    }

    public void setCallingPackage(String callingPkg) {
        sLaunchPkg = callingPkg;
    }

    public void setAlwaysOnTopOnly(ActivityDisplay display, ActivityStack stack, boolean isNewStack, boolean alwaysOnTop) {
        try {
            if (!"com.huawei.android.launcher".equals(sLaunchPkg)) {
                if (!DOCKBAR_PACKAGE_NAME.equals(sLaunchPkg)) {
                    if (display != null && !stack.inHwMultiStackWindowingMode()) {
                        if (!stack.inPinnedWindowingMode()) {
                            List<ActivityStack> activityStacks = getForegroundFreeform(display, true);
                            List<TaskRecord> addTasks = new ArrayList<>();
                            for (int stackNdx = activityStacks.size() - 1; stackNdx >= 0; stackNdx--) {
                                ActivityStack topStack = activityStacks.get(stackNdx);
                                if (isNewStack) {
                                    if (topStack.inHwFreeFormWindowingMode()) {
                                        topStack.setAlwaysOnTopOnly(alwaysOnTop);
                                        addTasks.add(topStack.topTask());
                                    }
                                } else if (topStack.inHwFreeFormWindowingMode() && stack.getTopActivity() != null && !stack.getTopActivity().visible) {
                                    topStack.setAlwaysOnTopOnly(alwaysOnTop);
                                    addTasks.add(topStack.topTask());
                                }
                            }
                            if (!addTasks.isEmpty()) {
                                this.mService.mHwATMSEx.dispatchFreeformBallLifeState(addTasks, alwaysOnTop ? "remove" : "add");
                            }
                            sLaunchPkg = "";
                            return;
                        }
                    }
                    sLaunchPkg = "";
                }
            }
        } finally {
            sLaunchPkg = "";
        }
    }

    public List<ActivityStack> getForegroundFreeform(ActivityDisplay defaultDisplay, boolean isContainsPadCast) {
        List<ActivityStack> activityStacks = new ArrayList<>();
        if (defaultDisplay != null) {
            int freeformNum = allowedForegroundFreeForms(defaultDisplay.mDisplayId);
            int index = 0;
            for (int stackNdx = defaultDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = defaultDisplay.getChildAt(stackNdx);
                if (stack.inHwFreeFormWindowingMode() && !((!stack.isAlwaysOnTop() && !stack.shouldBeVisible((ActivityRecord) null)) || stack.topRunningActivityLocked(true) == null || stack.inHwPCMultiStackWindowingMode())) {
                    if (isAllActivityTranslucentLocked(stack)) {
                        Slog.i(TAG, "all activities in stack is translucent");
                    } else if (stack.mIsTempStack) {
                        Slog.i(TAG, "getForegroundFreeform: stack need to be coverd by same application stack");
                    } else if (!isPadCastStack(stack)) {
                        activityStacks.add(stack);
                        index++;
                    } else if (isContainsPadCast) {
                        activityStacks.add(stack);
                    }
                } else if (stack.inMultiWindowMode() || stack.topRunningActivityLocked(true) != null) {
                    if (!stack.inMultiWindowMode()) {
                        if (sTranslucentStackId != stack.getStackId()) {
                            break;
                        }
                    }
                }
                if (index == freeformNum) {
                    break;
                }
            }
        }
        return activityStacks;
    }

    public boolean hasVisibleHwMultiStack(ActivityDisplay display) {
        if (display == null) {
            return false;
        }
        for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ActivityStack stack = display.getChildAt(stackNdx);
            if (stack.topRunningActivityLocked(true) != null && !stack.inHwPCMultiStackWindowingMode()) {
                if (!stack.inMultiWindowMode()) {
                    break;
                } else if (stack.inHwFreeFormWindowingMode() && (stack.isAlwaysOnTop() || stack.shouldBeVisible((ActivityRecord) null))) {
                    return true;
                } else {
                    if (stack.inHwSplitScreenWindowingMode() && stack.shouldBeVisible((ActivityRecord) null)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setDockShowing(boolean isDockShowing) {
        this.mIsDockShowing = isDockShowing;
    }

    private void checkHwMultiStackBoundsWhenOptionsMatch(ActivityStack stack) {
        ActivityDisplay activityDisplay;
        if (stack.inHwSplitScreenWindowingMode() && (activityDisplay = stack.getDisplay()) != null) {
            HwSplitScreenCombination visibleCombination = null;
            HwSplitScreenCombination stackPositionCombination = null;
            for (int i = this.mHwSplitScreenCombinations.size() - 1; i >= 0; i--) {
                HwSplitScreenCombination screenCombination = this.mHwSplitScreenCombinations.get(i);
                if (screenCombination.mDisplayId == activityDisplay.mDisplayId) {
                    if (screenCombination.isSplitScreenVisible()) {
                        visibleCombination = screenCombination;
                    } else if (screenCombination.hasHwSplitScreenStack(stack)) {
                        stackPositionCombination = screenCombination;
                    }
                }
            }
            if (!(visibleCombination == null || visibleCombination.hasHwSplitScreenStack(stack))) {
                if (stackPositionCombination != null) {
                    this.mHwSplitScreenCombinations.remove(stackPositionCombination);
                    List<ActivityStack> combinedStacks = stackPositionCombination.findCombinedSplitScreenStacks(stack);
                    if (!combinedStacks.isEmpty()) {
                        for (ActivityStack combinedStack : combinedStacks) {
                            exitHwMultiStack(combinedStack, false, false, false, true, false);
                        }
                    }
                }
                Rect bounds = new Rect(visibleCombination.getHwSplitScreenStackBounds(stack.getWindowingMode()));
                if (bounds.isEmpty()) {
                    calcHwMultiWindowStackBoundsDefault(stack, bounds);
                }
                visibleCombination.replaceCombinedSplitScreenStack(stack);
                visibleCombination.reportPkgNameEvent(this.mService);
                if (!bounds.isEmpty()) {
                    stack.resize(bounds, (Rect) null, (Rect) null);
                }
                this.mService.getTaskChangeNotificationController().notifyTaskStackChanged();
            }
        }
    }

    public void notifyDisplayModeChange(int displaymode, int currDisplaymode) {
        ActivityDisplay defaultDisplay;
        sForegroundFreeFormStack = displaymode == 1 ? 2 : 1;
        if (!(displaymode == 1 || currDisplaymode != 1 || (defaultDisplay = this.mService.mRootActivityContainer.getDefaultDisplay()) == null)) {
            List<ActivityStack> activityStacks = new ArrayList<>();
            for (int stackNdx = defaultDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = defaultDisplay.getChildAt(stackNdx);
                if (stack.inHwFreeFormWindowingMode() && stack.isAlwaysOnTop()) {
                    activityStacks.add(stack);
                } else if (!stack.inMultiWindowMode()) {
                    break;
                }
            }
            setFreeFromStackInvisible(activityStacks, defaultDisplay);
        }
        if (displaymode == 3) {
            maximizeHwFreeForm();
        }
        ActivityDisplay defaultDisplay2 = this.mService.mRootActivityContainer.getDefaultDisplay();
        if (defaultDisplay2 != null) {
            for (int stackNdx2 = defaultDisplay2.getChildCount() - 1; stackNdx2 >= 0; stackNdx2--) {
                ActivityStack stack2 = defaultDisplay2.getChildAt(stackNdx2);
                ActivityRecord activityRecord = stack2.topRunningActivityLocked();
                if (activityRecord != null && activityRecord.nowVisible) {
                    reportAppWindowMode(1103, activityRecord, stack2.getWindowingMode(), "DisplayModeChange");
                }
            }
        }
    }

    public void maximizeHwFreeForm() {
        ActivityDisplay display = this.mService.getRootActivityContainer().getDefaultDisplay();
        synchronized (this.mService.getGlobalLock()) {
            List<ActivityStack> freeFormStacks = new ArrayList<>();
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display.getChildAt(stackNdx);
                if (stack.inHwFreeFormWindowingMode() && isActivityStackVisible(stack)) {
                    freeFormStacks.add(stack);
                }
            }
            if (freeFormStacks.size() > 0) {
                for (ActivityStack activityStack : freeFormStacks) {
                    exitHwMultiStack(activityStack, false, false, false, true, false);
                }
                this.mService.mStackSupervisor.mRootActivityContainer.ensureActivitiesVisible((ActivityRecord) null, 0, true);
                this.mService.mStackSupervisor.mRootActivityContainer.resumeFocusedStacksTopActivities();
            }
        }
    }

    private boolean isActivityStackVisible(ActivityStack stack) {
        return stack != null && ((stack.mTaskStack != null && stack.mTaskStack.isVisible()) || stack.isTopActivityVisible());
    }

    public boolean isInDisplaySurfaceScaled() {
        return this.mService.mWindowManager.getLazyMode() != 0 || this.mService.mWindowManager.isInSubFoldScaleMode();
    }

    public float getStackScale(int taskId) {
        if (taskId == -1) {
            return 1.0f;
        }
        synchronized (this.mService.getGlobalLock()) {
            if (taskId != -100) {
                TaskRecord taskRecord = this.mService.mRootActivityContainer.anyTaskForId(taskId);
                if (taskRecord == null) {
                    Slog.i(TAG, " no task for id: " + taskId);
                    return 1.0f;
                } else if (taskRecord.getStack() == null || taskRecord.getStack().getTaskStack() == null || !taskRecord.getStack().getTaskStack().inHwFreeFormWindowingMode()) {
                    return 1.0f;
                } else {
                    return taskRecord.getStack().getTaskStack().mHwStackScale;
                }
            } else {
                DisplayContent displayContent = ((ActivityTaskManagerService) this.mService).mWindowManager.mRoot.getTopFocusedDisplayContent();
                if (displayContent == null) {
                    return 1.0f;
                }
                AppWindowToken focus = displayContent.mFocusedApp;
                if (focus != null) {
                    if (focus.getTask() != null) {
                        if (!focus.getTask().inHwFreeFormWindowingMode()) {
                            return 1.0f;
                        }
                        return focus.getTask().mStack == null ? 1.0f : focus.getTask().mStack.mHwStackScale;
                    }
                }
                return 1.0f;
            }
        }
    }

    public void updateSplitBarPosForIm(int position, int displayId) {
        HwMultiWindowSplitUI.getInstance(this.mService.getUiContext(), this.mService, displayId).updateSplitBarPosForIm(position);
    }

    public Bundle getHwMultiWindowState() {
        Bundle result = new Bundle();
        synchronized (this.mService.getGlobalLock()) {
            DisplayContent displayContent = this.mService.mWindowManager.mRoot.getTopFocusedDisplayContent();
            WindowState imeHolder = null;
            if (!(displayContent == null || displayContent.getDisplayPolicy() == null)) {
                imeHolder = displayContent.getDisplayPolicy().mAloneTarget;
            }
            WindowState focus = displayContent.mCurrentFocus;
            if (focus == null) {
                return result;
            }
            Rect focusRect = new Rect(focus.getBounds());
            if (!focus.inHwFreeFormWindowingMode()) {
                if (imeHolder == null || !imeHolder.inHwFreeFormWindowingMode()) {
                    if (focus.inHwSplitScreenWindowingMode() || (imeHolder != null && imeHolder.inHwSplitScreenWindowingMode())) {
                        boolean isLeftRightSplitWindow = false;
                        if (getSplitGearsByDisplay(displayContent.mAcitvityDisplay).getInt(HW_SPLIT_SCREEN_PRIMARY_POSITION, 0) == 1 && isPhoneLandscape(displayContent)) {
                            isLeftRightSplitWindow = true;
                        }
                        result.putBoolean("is_leftright_split", isLeftRightSplitWindow);
                    }
                    result.putParcelable("ime_target_rect", focusRect);
                    result.putFloat("globleScale", focus.mGlobalScale);
                    return result;
                }
            }
            result.putBoolean("float_ime_state", isPhoneLandscape(displayContent));
            if (focus.getStack() != null) {
                float scale = focus.getStack().mHwStackScale;
                int left = focusRect.left;
                int top = focusRect.top;
                focusRect.scale(scale);
                focusRect.offsetTo(left, top);
            }
            result.putParcelable("ime_target_rect", focusRect);
            result.putFloat("globleScale", focus.mGlobalScale);
            return result;
        }
    }

    public boolean isPhoneLandscape(DisplayContent displayContent) {
        if (displayContent == null) {
            return false;
        }
        int displayWidth = displayContent.getDisplayInfo().logicalWidth;
        int displayHeight = displayContent.getDisplayInfo().logicalHeight;
        float densityWithoutRog = getDensityDpiWithoutRog();
        int widthColumns = getColumnsByWidth((int) (((float) (displayWidth * 160)) / densityWithoutRog));
        int heightColumns = getColumnsByWidth((int) (((float) (displayHeight * 160)) / densityWithoutRog));
        if (widthColumns < 8 || heightColumns != 4) {
            return false;
        }
        return true;
    }

    public void setHwWinCornerRaduis(WindowState win, SurfaceControl control) {
        if (win != null && control != null) {
            Rect rect = new Rect(0, 0, win.mWindowFrames.mCompatFrame.width(), win.mWindowFrames.mCompatFrame.height());
            rect.offsetTo(win.mAttrs.surfaceInsets.left, win.mAttrs.surfaceInsets.top);
            control.setWindowCrop(rect);
            control.setCornerRadius(sFreeformCornerRadius);
        }
    }

    public Rect checkBoundInheritFromSource(ActivityRecord sourceRecord, TaskRecord task) {
        ActivityStack sourceStack;
        Rect outBounds = new Rect();
        ComponentName realActivity = null;
        if (sourceRecord != null && sourceRecord.inHwFreeFormWindowingMode() && ActivityStartInterceptorBridge.isAppLockActivity(sourceRecord.shortComponentName)) {
            ActivityStack mSourceStack = sourceRecord.getActivityStack();
            if (mSourceStack != null) {
                if (mSourceStack.topTask() != null) {
                    realActivity = mSourceStack.topTask().realActivity;
                }
                if (realActivity != null && ActivityStartInterceptorBridge.isAppLockActivity(realActivity.flattenToShortString())) {
                    TaskStack taskStack = mSourceStack.getTaskStack();
                    if (taskStack != null) {
                        outBounds = taskStack.getBounds();
                        int stackId = mSourceStack.getStackId();
                        applockSet.clear();
                        applockSet.add(Integer.valueOf(stackId));
                    }
                    if (!(task == null || task.getStack() == null)) {
                        TaskStack dstTaskStack = task.getStack().getTaskStack();
                        if (!(taskStack == null || dstTaskStack == null || !dstTaskStack.inHwFreeFormWindowingMode())) {
                            dstTaskStack.mHwStackScale = taskStack.mHwStackScale;
                        }
                    }
                }
            }
        } else if (sourceRecord != null && (sourceStack = sourceRecord.getActivityStack()) != null && sourceStack.isStackTranslucent((ActivityRecord) null) && !sourceStack.isActivityTypeHome()) {
            sTranslucentStackId = sourceStack.getStackId();
        }
        return outBounds;
    }

    public Rect handleStackFromOneStep(ActivityRecord activity, ActivityStack stack, ActivityRecord sourceRecord) {
        ActivityStack sourceStack;
        Rect outBounds = new Rect();
        if (!HwFoldScreenState.isInwardFoldDevice() || sourceRecord == null || stack == null || activity == null || stack.getDisplay() == null || sourceRecord.getWindowingMode() != 102 || (sourceStack = sourceRecord.getActivityStack()) == null || stack.mStackId == sourceStack.mStackId || sourceStack.getBounds().isEmpty() || allowedForegroundFreeForms(stack.getDisplay().mDisplayId) == 1) {
            return outBounds;
        }
        try {
            if (activity.intent == null || (activity.intent.getHwFlags() & 1048576) == 0 || !new Intent(activity.intent).getBooleanExtra(IS_TEMP_STACK, false)) {
                return outBounds;
            }
            if (stack.getWindowingMode() == 102 && stack.mTaskStack != null) {
                stack.mTaskStack.mHwStackScale = sourceStack.mTaskStack.mHwStackScale;
            }
            sourceStack.mIsTempStack = true;
            outBounds.set(sourceStack.getBounds());
            if (sourceStack.getTopActivity() != null) {
                this.mService.mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(sourceStack.getTopActivity().appToken, false);
            }
            this.mHandler.postDelayed(new Runnable(sourceStack) {
                /* class com.android.server.wm.$$Lambda$HwMultiWindowManager$DhaOygvOIzQL_C3uzMjhdekSOmQ */
                private final /* synthetic */ ActivityStack f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwMultiWindowManager.this.lambda$handleStackFromOneStep$3$HwMultiWindowManager(this.f$1);
                }
            }, 100);
            return outBounds;
        } catch (BadParcelableException e) {
            Slog.w(TAG, "handleStackFromOneStep get extra data error.");
        }
    }

    public /* synthetic */ void lambda$handleStackFromOneStep$3$HwMultiWindowManager(ActivityStack sourceStack) {
        if (sourceStack != null && sourceStack.mIsTempStack) {
            lambda$addStackReferenceIfNeeded$0$HwMultiWindowManager(sourceStack.getDisplay());
        }
    }

    public float getHwMultiWinCornerRadius(int windowingMode) {
        if (windowingMode != 102) {
            return 0.0f;
        }
        return sFreeformCornerRadius;
    }

    public void stackCreated(ActivityStack stack, ActivityRecord root) {
        if (stack != null && stack.inHwFreeFormWindowingMode() && root != null && root.intent != null && (262144 & root.intent.getHwFlags()) != 0) {
            this.mPadCastStack = stack;
            stack.mTaskStack.mHwStackScale = 1.0f;
        }
    }

    private void calcPadCastStackBounds(Rect outBounds, ActivityStack stack) {
        Rect defaultBounds = new Rect();
        calcDefaultFreeFormBounds(defaultBounds, stack, true, false);
        if (stack.matchParentBounds()) {
            outBounds.set(defaultBounds);
            return;
        }
        outBounds.offsetTo(((defaultBounds.left + defaultBounds.right) / 2) - (outBounds.width() / 2), ((defaultBounds.top + defaultBounds.bottom) / 2) - (outBounds.height() / 2));
        outBounds.set(relocateOffScreenWindow(outBounds, stack, 1.0f));
    }

    /* access modifiers changed from: package-private */
    public boolean isPadCastStack(ActivityStack stack) {
        return stack != null && stack == this.mPadCastStack;
    }

    /* access modifiers changed from: package-private */
    public boolean isPadCastTask(TaskRecord task) {
        return task != null && (isPadCastStack(task.mStack) || !(task.intent == null || (task.intent.getHwFlags() & 262144) == 0));
    }

    public boolean isStatusBarPermenantlyShowing() {
        return this.mIsStatusBarPermenantlyShowing;
    }

    public void setFreeFormDropBound(Rect freeFromDrop) {
        this.mFreeFormDrop.set(freeFromDrop);
    }

    public void adjustHwFreeformPosIfNeed(DisplayContent displayContent, boolean isStatusShowing) {
        if (isStatusShowing != this.mIsStatusBarPermenantlyShowing) {
            Rect defaultBounds = new Rect();
            if (!(displayContent == null || displayContent.getTopStack() == null || displayContent.getTopStack().mActivityStack == null)) {
                calcDefaultFreeFormBounds(defaultBounds, displayContent.getTopStack().mActivityStack, false, false);
            }
            this.mIsStatusBarPermenantlyShowing = isStatusShowing;
            if (IS_HW_MULTIWINDOW_SUPPORTED && isPhoneLandscape(displayContent)) {
                int safeSideWidth = this.mIsHasSideinScreen ? this.mSafeSideWidth : 0;
                int topHeight = sStatusBarHeight + safeSideWidth;
                Iterator it = displayContent.getStacks().iterator();
                while (it.hasNext()) {
                    TaskStack taskStack = (TaskStack) it.next();
                    if (taskStack.inHwFreeFormWindowingMode() && !taskStack.inHwPCMultiStackWindowingMode()) {
                        Rect bounds = new Rect();
                        taskStack.getBounds(bounds);
                        if (!this.mIsStatusBarPermenantlyShowing) {
                            if (bounds.equals(defaultBounds)) {
                                Rect stackBounds = new Rect();
                                calcDefaultFreeFormBounds(stackBounds, taskStack.mActivityStack, false, false);
                                taskStack.mActivityStack.resize(stackBounds, (Rect) null, (Rect) null);
                            }
                            if (bounds.equals(this.mFreeFormDrop) && bounds.top >= topHeight) {
                                bounds.offsetTo(bounds.left, Math.max((sStatusBarHeight / 2) + safeSideWidth, (displayContent.getDisplayInfo().logicalHeight - ((int) (((float) bounds.height()) * taskStack.mHwStackScale))) / 2));
                                taskStack.mActivityStack.resize(bounds, (Rect) null, (Rect) null);
                            }
                        } else if (bounds.top < topHeight) {
                            bounds.offset(0, topHeight - bounds.top);
                            taskStack.mActivityStack.resize(bounds, (Rect) null, (Rect) null);
                        }
                    }
                }
            }
        }
    }

    public boolean blockSwipeFromTop(MotionEvent event, DisplayContent display) {
        WindowState statusBar;
        if (!IS_HW_MULTIWINDOW_SUPPORTED || display == null || event == null || !isPhoneLandscape(display) || (statusBar = display.getDisplayPolicy().getStatusBar()) == null || statusBar.isVisible()) {
            return false;
        }
        boolean shouldBlock = false;
        for (int i = display.getStacks().size() - 1; i >= 0; i--) {
            TaskStack taskStack = (TaskStack) display.getStacks().get(i);
            if (!taskStack.inHwFreeFormWindowingMode()) {
                break;
            }
            if (taskStack.isVisible()) {
                Rect bounds = taskStack.getBounds();
                if (bounds.top <= statusBar.getFrameLw().bottom) {
                    int width = (int) (((float) bounds.width()) * taskStack.mHwStackScale);
                    shouldBlock |= event.getX() >= ((float) (bounds.left + (width / 3))) && ((float) (bounds.left + ((width / 3) * 2))) >= event.getX();
                }
            }
        }
        return shouldBlock;
    }

    public void setForegroundFreeFormNum(int num) {
        if (num <= 2 && num >= 1) {
            sForegroundFreeFormStack = num;
        }
    }

    public void oneStepHwMultiWindowBdReport(ActivityRecord startActivity, int windowMode, ActivityOptions options) {
        if (startActivity != null && startActivity.intent != null && options != null && (startActivity.intent.getHwFlags() & 1048576) != 0) {
            try {
                Bundle bundle = startActivity.intent.getExtras();
                if (bundle != null) {
                    Object obj = bundle.clone();
                    if (obj instanceof Bundle) {
                        Bundle tmpBundle = (Bundle) obj;
                        long oneStepTime = tmpBundle.getLong("oneStepTime", 0);
                        if (oneStepTime != sOneStepBdTime) {
                            String srcPackageName = tmpBundle.getString("srcPackageName");
                            String dstPackageName = tmpBundle.getString("dstPackageName");
                            if (dstPackageName != null && !"com.huawei.android.internal.app".equals(dstPackageName)) {
                                sOneStepBdTime = oneStepTime;
                                Flog.bdReport(991310111, String.format(Locale.ENGLISH, "{srcPackageName:%s, dstPackageName:%s, windowingMode:%d}", srcPackageName, dstPackageName, Integer.valueOf(windowMode)));
                            }
                        }
                    }
                }
            } catch (BadParcelableException e) {
                Slog.e(TAG, "oneStepHwMultiWindowBdReport exception");
            } catch (Exception e2) {
                Slog.e(TAG, "oneStepHwMultiWindowBdReport exception");
            }
        }
    }

    public void reportAppWindowVisibleOrGone(ActivityRecord record) {
        if (record == null || record.getActivityStack() == null) {
            Slog.i(TAG, "reportAppWindowVisibleOrGone fail, activityRecord or activityStack is null");
            return;
        }
        ActivityStack activityStack = record.getActivityStack();
        int windowMode = activityStack.getWindowingMode();
        if (activityStack.topTask() != null) {
            List<ActivityRecord> list = activityStack.topTask().mActivities;
            for (int i = list.size() - 1; i >= 0; i--) {
                ActivityRecord r = list.get(i);
                if (r != record && r.nowVisible && record.packageName != null && record.packageName.equals(r.packageName)) {
                    return;
                }
            }
        }
        reportAppWindowMode(record.nowVisible ? 1101 : 1102, record, windowMode, record.nowVisible ? "ActivityVisible" : "ActivityInvisible");
    }

    public void reportAppWindowMode(int appEventType, ActivityRecord activityRecord, int windowMode, String reason) {
        if (activityRecord == null || activityRecord.packageName == null || activityRecord.appInfo == null || activityRecord.getActivityStack() == null || activityRecord.getActivityStack().topTask() == null || "com.android.permissioncontroller".equals(activityRecord.packageName)) {
            Slog.i(TAG, "don't need to report app window mode");
            return;
        }
        int displayMode = HwFoldScreenManager.getDisplayMode();
        int windowState = 11;
        if (displayMode == 0 || displayMode == 1) {
            if (WindowConfiguration.isHwFreeFormWindowingMode(windowMode)) {
                windowState = 10;
            } else if (WindowConfiguration.isHwSplitScreenWindowingMode(windowMode)) {
                windowState = 12;
            } else {
                windowState = 11;
            }
        } else if (displayMode == 2) {
            if (WindowConfiguration.isHwFreeFormWindowingMode(windowMode)) {
                windowState = 20;
            } else if (WindowConfiguration.isHwSplitScreenWindowingMode(windowMode)) {
                windowState = 23;
            } else {
                windowState = 21;
            }
        } else if (displayMode == 3) {
            windowState = 22;
        }
        Date date = new Date();
        String content = String.format(Locale.ENGLISH, "{STR1:%s, LONG1:%s, LONG2:%d, LONG3:%d, LONG4:%d}", activityRecord.packageName, Integer.valueOf(windowState), Long.valueOf(activityRecord.appInfo.longVersionCode), Long.valueOf(date.getTime()), Integer.valueOf(activityRecord.getActivityStack().topTask().taskId));
        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.d(TAG, "appEventType:" + appEventType + ",content:" + content + ",reason:" + reason);
        }
        Flog.bdReport(appEventType, content, date);
    }

    public void removeFreeformBallWhenDestroy(ActivityRecord activityRecord, TaskRecord taskRecord) {
        if (activityRecord != null && taskRecord != null && activityRecord.inHwFreeFormWindowingMode() && sRemoveFromBallActivitys.contains(activityRecord.shortComponentName)) {
            if (taskRecord.intent == null || !ActivityRecord.isMainIntent(taskRecord.intent)) {
                List<TaskRecord> removeTasks = new ArrayList<>(1);
                removeTasks.add(taskRecord);
                this.mService.mHwATMSEx.dispatchFreeformBallLifeState(removeTasks, "remove");
            }
        }
    }

    public void updateFloatingBallPos(Rect pos) {
        if (pos != null) {
            this.mFloatingBallPos.set(pos);
            if (this.mFloatingBallPos.isEmpty()) {
                this.mIsFloatingBallPosInit = false;
            } else if (!this.mIsFloatingBallPosInit) {
                this.mIsFloatingBallPosInit = true;
                this.mFloatingBallInitTimes = 0;
            }
            Slog.i(TAG, "update floating ball position:" + pos);
        }
    }

    private void initFloatingBallPos(ActivityRecord record) {
        List<TaskRecord> initTasks = new ArrayList<>(1);
        if (record.task != null) {
            initTasks.add(record.task);
            this.mService.mHwATMSEx.dispatchFreeformBallLifeState(initTasks, "initBallPos");
        }
        this.mFloatingBallInitTimes++;
        Slog.i(TAG, "init floating ball position, record:" + record + " ,init times:" + this.mFloatingBallInitTimes);
    }

    /* renamed from: minimizeHwFreeForm */
    public boolean lambda$minimizeHwFreeForm$4$HwMultiWindowManager(ActivityRecord record, boolean nonRoot, boolean isAddToFloatingBall) {
        if (record == null) {
            return false;
        }
        if (this.mIsFloatingBallPosInit || !this.mFloatingBallPos.isEmpty() || this.mFloatingBallInitTimes >= 5) {
            if (isAddToFloatingBall) {
                setFloatingBallAnimationInfo(record);
            }
            return this.mService.moveActivityTaskToBack(record.appToken, nonRoot);
        }
        initFloatingBallPos(record);
        this.mHandler.postDelayed(new Runnable(record, nonRoot, isAddToFloatingBall) {
            /* class com.android.server.wm.$$Lambda$HwMultiWindowManager$E8tIBfmtROyZCFFDJhKpttgFUUE */
            private final /* synthetic */ ActivityRecord f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ boolean f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwMultiWindowManager.this.lambda$minimizeHwFreeForm$4$HwMultiWindowManager(this.f$1, this.f$2, this.f$3);
            }
        }, 100);
        return false;
    }

    public void setFloatingBallAnimationInfo(ActivityRecord record) {
        if (record != null) {
            if (this.mFloatingBallPos.isEmpty()) {
                Slog.w(TAG, "floating ball position is uninitialized, skipping animation. record:" + record);
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putFloat(HwWmConstants.PIVOTX_STR, (float) this.mFloatingBallPos.centerX());
            bundle.putFloat(HwWmConstants.PIVOTY_STR, (float) this.mFloatingBallPos.centerY());
            bundle.putInt(HwWmConstants.FLAG_STR, 3);
            bundle.putInt(HwWmConstants.TASK_ID, record.task != null ? record.task.taskId : -1);
            String packageName = record.packageName;
            ComponentName realActivity = record.task != null ? record.task.realActivity : null;
            if (realActivity != null) {
                packageName = realActivity.getPackageName();
            }
            HwWindowManager.setAppWindowExitInfo(bundle, HwMultiWinUtils.drawable2Bitmap(HwMultiWinUtils.getAppIconForFloatingBall(this.mService.mUiContext, packageName, record.mUserId, HwMultiWinUtils.isQuickNote(realActivity))));
            Slog.i(TAG, "setFloatingBallAnimationInfo, record:" + record + ", pos:" + this.mFloatingBallPos);
        }
    }

    public float[] getScaleRange(ActivityStack stack) {
        float[] scale = new float[2];
        if (stack == null || stack.getDisplay() == null || stack.getDisplay().mDisplayContent == null) {
            return scale;
        }
        DisplayContent displayContent = stack.getDisplay().mDisplayContent;
        float maxScale = 0.0f;
        float minScale = 0.0f;
        int displayWidth = displayContent.getDisplayInfo().logicalWidth;
        int displayHeight = displayContent.getDisplayInfo().logicalHeight;
        if (allowedForegroundFreeForms(displayContent.mDisplayId) == sForegroundFreeFormStack) {
            float densityWithoutRog = getDensityDpiWithoutRog();
            int widthColumns = getColumnsByWidth((int) (((float) (displayWidth * 160)) / densityWithoutRog));
            int heightColumns = getColumnsByWidth((int) (((float) (displayHeight * 160)) / densityWithoutRog));
            int minSide = Math.min(displayWidth, displayHeight);
            int marginPixel = dipToPixelWithoutRog(24, densityWithoutRog);
            if (widthColumns == 4 && heightColumns > 4) {
                minScale = 0.35f;
                maxScale = (((float) (displayWidth - (marginPixel * 2))) * 1.0f) / ((float) displayWidth);
            } else if (widthColumns > 4 && heightColumns == 4) {
                minScale = 0.35f;
                maxScale = (((float) ((int) (((float) ((displayHeight - sStatusBarHeight) - ((this.mIsHasSideinScreen ? this.mSafeSideWidth : 0) * 2))) / (((((float) this.mCaptionViewHeight) * 1.0f) / ((float) displayHeight)) + 1.3333334f)))) * 1.0f) / ((float) displayHeight);
            } else if (widthColumns == 8 && heightColumns == 8) {
                Rect physicalRect = HwFoldScreenState.getScreenPhysicalRect(2);
                int maxHeight = displayHeight - (sStatusBarHeight * 2);
                int i = this.mCaptionViewHeight;
                if (((int) (((((float) physicalRect.width()) * 1.0f) * 16.0f) / 9.0f)) + i < maxHeight) {
                    maxScale = 1.0f;
                } else {
                    maxScale = (((float) ((int) (((float) maxHeight) / (((((float) i) * 1.0f) / ((float) physicalRect.width())) + 1.7777778f)))) * 1.0f) / ((float) physicalRect.width());
                }
                minScale = 0.35f;
            } else if ((widthColumns == 8 && heightColumns == 12) || (widthColumns == 12 && heightColumns == 8)) {
                maxScale = this.ratio;
                minScale = (((float) minSide) * 0.5f) / ((float) ((int) ((((float) (minSide - (sStatusBarHeight * 2))) * INIT_SCALE_RATIO_FOR_PAD) / this.ratio)));
            }
            scale[0] = minScale;
            scale[1] = maxScale;
        }
        return scale;
    }

    public Map<String, Boolean> getAppUserAwarenessState(int displayId, List<String> packageNames) {
        Map<String, Boolean> map = new HashMap<>();
        List<ActivityDisplay> activityDisplayList = new ArrayList<>();
        if (displayId == -1) {
            for (int displayNdx = this.mService.mRootActivityContainer.getChildCount() - 1; displayNdx >= 0; displayNdx--) {
                activityDisplayList.add(this.mService.mRootActivityContainer.getChildAt(displayNdx));
            }
        } else {
            ActivityDisplay activityDisplay = this.mService.mRootActivityContainer.getActivityDisplay(displayId);
            if (activityDisplay == null) {
                Slog.i(TAG, "getAppUserAwarenessState activityDisplay null");
                return map;
            }
            activityDisplayList.add(activityDisplay);
        }
        int index = -1;
        synchronized (this.mService.getGlobalLock()) {
            for (int displayNdx2 = activityDisplayList.size() - 1; displayNdx2 >= 0; displayNdx2--) {
                ActivityDisplay activityDisplay2 = activityDisplayList.get(displayNdx2);
                for (int stackNdx = activityDisplay2.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                    ActivityStack stack = activityDisplay2.getChildAt(stackNdx);
                    if (stack.inHwFreeFormWindowingMode()) {
                        checkPackageNameInList(stack, packageNames, map);
                    } else if (stack.inHwSplitScreenWindowingMode()) {
                        if (index == -1) {
                            index = stackNdx;
                        }
                        checkPackageNameInList(stack, packageNames, map);
                    } else if (stack.getWindowingMode() == 1 && index == -1) {
                        index = stackNdx;
                        checkPackageNameInList(stack, packageNames, map);
                    }
                }
            }
        }
        return map;
    }

    public void notifyCameraStateForAtms(Bundle options) {
        if (options == null) {
            Slog.e(TAG, "notifyCameraStateForAtms, options is null.");
            return;
        }
        try {
            String clientName = options.getString("clientName", "");
            if (options.getInt("cameraState", 0) != 1 || clientName == null || clientName.isEmpty()) {
                this.packageNameUseCamera = "";
            } else {
                this.packageNameUseCamera = clientName;
                String packageNameRotation = this.packageNameRotations.get(this.packageNameUseCamera);
                if (packageNameRotation != null) {
                    SystemProperties.set(SYS_HW_MULTIWIN_FOR_CAMERA, packageNameRotation);
                    Slog.d(TAG, "set sys.hw_multiwin_for_camera=" + SystemProperties.get(SYS_HW_MULTIWIN_FOR_CAMERA));
                }
            }
            this.packageNameRotations.clear();
        } catch (BadParcelableException e) {
            Slog.e(TAG, "notifyCameraStateForAtms exception");
        } catch (Exception e2) {
            Slog.e(TAG, "notifyCameraStateForAtms exception");
        }
    }

    public void updateCameraRotatio(String packageName, int screenRotation, int windowRotation) {
        if (packageName == null || packageName.isEmpty()) {
            Slog.w(TAG, "updateCameraRotatio packageName is isEmpty");
        } else if (this.packageNameUseCamera.isEmpty()) {
            this.packageNameRotations.put(packageName, getCameraRotationString(packageName, screenRotation, windowRotation));
        } else if (this.packageNameUseCamera.equals(packageName)) {
            SystemProperties.set(SYS_HW_MULTIWIN_FOR_CAMERA, getCameraRotationString(packageName, screenRotation, windowRotation));
            Slog.d(TAG, "set sys.hw_multiwin_for_camera=" + SystemProperties.get(SYS_HW_MULTIWIN_FOR_CAMERA));
            this.packageNameRotations.clear();
        }
    }

    public Map<String, String> getPackageNameRotations() {
        return this.packageNameRotations;
    }

    private String getCameraRotationString(String packageName, int screenRotation, int windowRotation) {
        StringBuffer bf = new StringBuffer();
        if (screenRotation > windowRotation) {
            bf.append(packageName);
            bf.append("/:+");
            bf.append(screenRotation - windowRotation);
        } else if (screenRotation == windowRotation) {
            bf.append("-1");
        } else {
            bf.append(packageName);
            bf.append("/:+");
            bf.append((screenRotation - windowRotation) + 4);
        }
        return bf.toString();
    }

    private void checkPackageNameInList(ActivityStack stack, List<String> packageNames, Map<String, Boolean> map) {
        ActivityRecord record = stack.getTopActivity();
        if (record != null) {
            if (packageNames.contains(record.packageName)) {
                map.put(record.packageName, false);
            }
            ActivityRecord rootActivity = record.getTaskRecord().getRootActivity();
            if (!record.packageName.equals(rootActivity.packageName) && packageNames.contains(rootActivity.packageName)) {
                map.put(rootActivity.packageName, false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handlePadCastFullStackBallState() {
        ActivityStack activityStack = this.mPadCastStack;
        if (activityStack != null && activityStack.isTopActivityVisible() && !this.mPadCastStack.inMultiWindowMode()) {
            List<TaskRecord> handleTasks = new ArrayList<>(1);
            handleTasks.add(this.mPadCastStack.topTask());
            this.mService.mHwATMSEx.dispatchFreeformBallLifeState(handleTasks, "add");
        }
    }

    public static boolean isRealTablet(int displayId) {
        if (displayId == 0) {
            return IS_TABLET;
        }
        return HwActivityTaskManager.getVirtualDisplayId("padCast") == displayId;
    }

    private boolean isCallerHasPermission(String permission, int callingPid, int callingUid, String function) {
        ActivityTaskManagerService activityTaskManagerService = this.mService;
        boolean isAllowed = ActivityTaskManagerService.checkPermission(permission, callingPid, callingUid) == 0;
        if (!isAllowed) {
            Slog.i(TAG, "permission denied for " + function + ", callingPid:" + callingPid + ", callingUid:" + callingUid + ", requires: " + permission);
        }
        return isAllowed;
    }

    public List<String> getVisibleCanShowWhenLockedPackages(int displayId) {
        ActivityRecord topActivity;
        List<String> packages = new ArrayList<>();
        if (!isCallerHasPermission("android.permission.REAL_GET_TASKS", Binder.getCallingPid(), Binder.getCallingUid(), "getVisibleCanShowWhenLockedPackages")) {
            return packages;
        }
        synchronized (this.mService.getGlobalLock()) {
            ActivityDisplay activityDisplay = this.mService.mRootActivityContainer.getActivityDisplay(displayId);
            if (activityDisplay == null) {
                return packages;
            }
            int stackNdx = activityDisplay.getChildCount() - 1;
            while (true) {
                if (stackNdx < 0) {
                    break;
                }
                ActivityStack stack = activityDisplay.getChildAt(stackNdx);
                if (stack.shouldBeVisible((ActivityRecord) null) && (topActivity = stack.getTopActivity()) != null && topActivity.canShowWhenLocked()) {
                    packages.add(topActivity.packageName);
                }
                if (!stack.inMultiWindowMode() && !stack.isStackTranslucent((ActivityRecord) null)) {
                    break;
                } else if (stack.isActivityTypeHome()) {
                    break;
                } else {
                    stackNdx--;
                }
            }
            return packages;
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityStack findUncombinedSplitScreenStack() {
        for (int i = this.mHwSplitScreenCombinations.size() - 1; i >= 0; i--) {
            HwSplitScreenCombination screenCombination = this.mHwSplitScreenCombinations.get(i);
            if (!screenCombination.isSplitScreenCombined()) {
                if (screenCombination.hasHwSplitScreenPrimaryStack()) {
                    return screenCombination.getHwSplitScreenPrimaryStack();
                }
                if (screenCombination.hasHwSplitScreenSecondaryStack()) {
                    return screenCombination.getHwSplitScreenSecondaryStack();
                }
            }
        }
        return null;
    }

    public void notifyNotificationAnimationFinish(int displayId) {
        if (ActivityTaskManagerDebugConfig.DEBUG_HWFREEFORM) {
            Slog.i(TAG, "notifyNotificationAnimationFinish displayId:" + displayId);
        }
        this.isReadyToFinishAnimation = true;
        setAllStackToShow(displayId);
    }

    public boolean hasPendingShowStack(int displayId) {
        ActivityDisplay activityDisplay;
        if (this.pendingShowStackSet.size() == 0 || this.isReadyToShow || (activityDisplay = this.mService.getRootActivityContainer().getActivityDisplay(displayId)) == null) {
            return false;
        }
        for (Integer num : this.pendingShowStackSet) {
            if (activityDisplay.getStack(num.intValue()) != null) {
                return true;
            }
        }
        return false;
    }

    public void clearPendingShowStack(int displayId) {
        if (ActivityTaskManagerDebugConfig.DEBUG_HWFREEFORM) {
            Slog.i(TAG, "clearPendingShowStack displayId:" + displayId);
        }
        this.isReadyToShow = true;
        if (this.isReadyToFinishAnimation) {
            setAllStackToShow(displayId);
        }
    }

    private void setAllStackToShow(int displayId) {
        synchronized (this.mService.getGlobalLock()) {
            ActivityDisplay activityDisplay = this.mService.getRootActivityContainer().getActivityDisplay(displayId);
            if (activityDisplay != null) {
                Iterator iterator = this.pendingShowStackSet.iterator();
                while (iterator.hasNext()) {
                    ActivityStack stack = activityDisplay.getStack(iterator.next().intValue());
                    setTaskStackHide(stack, false);
                    if (this.isReadyToShow && this.isReadyToFinishAnimation) {
                        this.mHandler.postDelayed(new Runnable(stack) {
                            /* class com.android.server.wm.$$Lambda$HwMultiWindowManager$QTpaN8zG6zgTQguVOodqRODBFY */
                            private final /* synthetic */ ActivityStack f$1;

                            {
                                this.f$1 = r2;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                HwMultiWindowManager.this.lambda$setAllStackToShow$5$HwMultiWindowManager(this.f$1);
                            }
                        }, 200);
                        iterator.remove();
                    }
                }
            }
        }
    }

    public /* synthetic */ void lambda$setAllStackToShow$5$HwMultiWindowManager(ActivityStack stack) {
        synchronized (this.mService.getGlobalLock()) {
            if (stack != null) {
                stack.setPendingShow(false);
            }
        }
    }

    public void setTaskStackHide(ActivityStack activityStack, boolean isHide) {
        if (activityStack != null) {
            int stackId = activityStack.getStackId();
            float alpha = 1.0f;
            if (isHide && activityStack.isPendingShow()) {
                alpha = 0.0f;
                this.pendingShowStackSet.add(Integer.valueOf(stackId));
                this.isReadyToShow = false;
                this.isReadyToFinishAnimation = false;
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_HWFREEFORM) {
                Slog.i(TAG, "setTaskStackHide stackId:" + stackId + ", isHide:" + isHide);
            }
            if (activityStack.mTaskStack != null) {
                activityStack.mTaskStack.setAlpha(this.mService.mWindowManager.mTransactionFactory.make(), alpha);
                if (isHide) {
                    this.mHandler.postDelayed(new Runnable(activityStack) {
                        /* class com.android.server.wm.$$Lambda$HwMultiWindowManager$A1mWBqQrwmPu6YP3vVq31_GQFYU */
                        private final /* synthetic */ ActivityStack f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            HwMultiWindowManager.this.lambda$setTaskStackHide$6$HwMultiWindowManager(this.f$1);
                        }
                    }, TASK_STACK_HIDE_TIME_OUT);
                }
            }
        }
    }

    public /* synthetic */ void lambda$setTaskStackHide$6$HwMultiWindowManager(ActivityStack activityStack) {
        synchronized (this.mService.getGlobalLock()) {
            if (activityStack != null) {
                if (activityStack.mTaskStack != null) {
                    this.isReadyToShow = true;
                    this.isReadyToFinishAnimation = true;
                    setAllStackToShow(activityStack.mDisplayId);
                }
            }
        }
    }
}
