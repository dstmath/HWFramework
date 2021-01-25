package com.android.server.wm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.HardwareBuffer;
import android.hardware.display.DisplayManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.HwMwUtils;
import android.util.Slog;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.internal.view.RotationPolicy;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.multiwin.HwBlur;
import com.android.server.multiwin.HwMultiWinUtils;
import com.android.server.multiwin.animation.HwDragFullAnimStrategy;
import com.android.server.multiwin.animation.HwSplitBarExitAniStrategy;
import com.android.server.multiwin.animation.HwSplitBarReboundStrategy;
import com.android.server.wm.HwMultiWindowSplitUI;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.fsm.HwFoldScreenManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwMultiWindowSplitUI {
    private static final int APP_TYPE_QUICK_NOTE = 1;
    private static final int INVALID_POSITION = -200;
    private static final Object LOCK = new Object();
    private static final int RULE_FALSE = 0;
    private static final String TAG = "HwMultiWindowSplitUI";
    private static final int TIME_INTERVAL = 300;
    private static final int VELOCITY_MAX_LIMIT = 2000;
    private static final int VELOCITY_THRESHOLD = 1000;
    private static final int ZERO_SIZE = 0;
    private static volatile HwMultiWindowSplitUI mSingleInstance = null;
    private static Map<Integer, HwMultiWindowSplitUI> mSplitUIFactory = new HashMap();
    private static int sNavBarHeight;
    private static int sNavBarWidth;
    private static int sStatusBarHeight;
    private ActivityTaskManagerService mActivityTaskManagerService;
    private Bundle mAnimBundle = new Bundle();
    private View mBlackCoverInExitSplit;
    private int mBottom = 0;
    private Context mContext;
    private FrameLayout mCoverLayout = null;
    private WindowManager.LayoutParams mCoverLp = null;
    private int mDisplayHeight;
    private int mDisplayId = 0;
    private int mDisplayWidth;
    private int mDividerBarHeight;
    private int mDividerBarRadio;
    private boolean mDown = false;
    private int mDragFullLineColor;
    private RelativeLayout mDragFullZone;
    private int mDragFullZoneHeight;
    private int mDragFullZoneLastY = -1;
    private int mDragFullZonePadding;
    private int mDragFullZoneSlop;
    private int mDragFullZoneWidth;
    private ImageView mDragLine;
    private int mDragLineColor;
    private ImageView mDragPoint;
    private ImageView mDragRegion;
    private View mDragView;
    private int mExitRegion = 0;
    private ImageView mFarLine;
    private int mFarLineMargin;
    private int mFloatState = 0;
    private Handler mHandler = null;
    private int mHeightColumns;
    private int mHotRegionDefaultWidth;
    private HwMultiWindowManager mHwMultiWindowManager = null;
    private boolean mIsActionDown = false;
    private volatile boolean mIsAddedSplitBar = false;
    private boolean mIsDownAnimate = false;
    private boolean mIsDragFullAnimRunning = false;
    private boolean mIsDragFullZoneDragable = false;
    private boolean mIsLeftAndRightPos = false;
    private boolean mIsMoving = false;
    private boolean mIsNavBarMini = false;
    private boolean mIsNeedSetSplitBarVisible = false;
    private boolean mIsOneGear = false;
    private boolean mIsReadyToFull = false;
    private boolean mIsSetBackground = false;
    private boolean mIsSplitBarVisibleNow = false;
    private RelativeLayout mLeftCover;
    private Bitmap mLeftGaussBitmap;
    private ImageView mLeftImg;
    private int mMoveCount = 0;
    private int mNavBarPos = -1;
    private int mNavBarPosWhenActionDown;
    private ImageView mNearLine;
    private int mNearLineMargin;
    private int mOldWindowMode = -1;
    private float mPreDownX;
    private int mPreDownXTypeInMagic = 0;
    private float mPreDownY;
    private RelativeLayout mRightCover;
    private Bitmap mRightGaussBitmap;
    private ImageView mRightImg;
    private int mRotation;
    private int mThresholdDistance = 0;
    private int mTouchSlop;
    private int mUiMode;
    private float mVelocity;
    private VelocityTracker mVelocityTracker = null;
    private Vibrator mVibrator = null;
    private int mWidthColumns;
    private volatile int mWindowMode = -1;
    private int mWindowOffset;
    private int mWindowWH;
    private WindowManager mWm;
    public Rect primaryBounds;
    private int userRotationLocked = -1;

    private HwMultiWindowSplitUI(Context context, ActivityTaskManagerService service, int displayId) {
        this.mActivityTaskManagerService = service;
        DisplayManager displayManager = (DisplayManager) context.getSystemService("display");
        if (displayManager != null) {
            Display display = displayManager.getDisplay(displayId);
            if (display == null) {
                Slog.w(TAG, "Invalid displayId:" + displayId);
                displayId = 0;
                display = displayManager.getDisplay(0);
            }
            this.mContext = context.createDisplayContext(display);
            this.mUiMode = this.mContext.getResources().getConfiguration().uiMode & 48;
            this.mDisplayId = displayId;
            initHandler(this.mActivityTaskManagerService.mUiHandler.getLooper());
            this.mHandler.post(new Runnable(context) {
                /* class com.android.server.wm.$$Lambda$HwMultiWindowSplitUI$Kd1tw4VhOOGjGBc_s_gtpE_k6cA */
                private final /* synthetic */ Context f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwMultiWindowSplitUI.this.lambda$new$0$HwMultiWindowSplitUI(this.f$1);
                }
            });
            this.mVelocityTracker = VelocityTracker.obtain();
            return;
        }
        Slog.e(TAG, "displaymanager is null.");
    }

    public /* synthetic */ void lambda$new$0$HwMultiWindowSplitUI(Context context) {
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public static HwMultiWindowSplitUI getInstance(Context context, ActivityTaskManagerService service, int displayId) {
        if (displayId == -1 || context == null) {
            return null;
        }
        if (!mSplitUIFactory.containsKey(Integer.valueOf(displayId)) || mSplitUIFactory.get(Integer.valueOf(displayId)) == null) {
            synchronized (LOCK) {
                if (!mSplitUIFactory.containsKey(Integer.valueOf(displayId)) || mSplitUIFactory.get(Integer.valueOf(displayId)) == null) {
                    Slog.i(TAG, "add instance for split ui. displayid:" + displayId + ", config:" + context.getResources().getConfiguration());
                    mSplitUIFactory.put(Integer.valueOf(displayId), new HwMultiWindowSplitUI(context, service, displayId));
                }
            }
        }
        return mSplitUIFactory.get(Integer.valueOf(displayId));
    }

    public static HwMultiWindowSplitUI getInstance(Context context, ActivityTaskManagerServiceEx atmsEx, int displayId) {
        if (atmsEx == null || atmsEx.getActivityTaskManagerService() == null) {
            return null;
        }
        return getInstance(context, atmsEx.getActivityTaskManagerService(), displayId);
    }

    private void initHandler(Looper looper) {
        this.mHandler = new Handler(looper) {
            /* class com.android.server.wm.HwMultiWindowSplitUI.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        HwMultiWindowSplitUI.this.createSplitCover();
                        return;
                    case 1:
                        HwMultiWindowSplitUI.this.removeSplitBarWindow();
                        return;
                    case 2:
                        if (msg.obj instanceof Float) {
                            HwMultiWindowSplitUI.this.updateSplitBarPosition(msg.arg1, ((Float) msg.obj).floatValue());
                            return;
                        }
                        return;
                    case 3:
                        HwMultiWindowSplitUI.this.updatePositionOnConfigurationChanged();
                        return;
                    case 4:
                        HwMultiWindowSplitUI.this.setSplitBarVisibleIfNeeded();
                        return;
                    case 5:
                        HwMultiWindowSplitUI.this.updateSplitBarPos(msg.arg1);
                        return;
                    case 6:
                        HwMultiWindowSplitUI.this.updateSplitBarPos(-1);
                        HwMultiWindowSplitUI.this.updateSplitBarPos(1);
                        return;
                    case 7:
                    default:
                        return;
                    case 8:
                        HwMultiWindowSplitUI.this.setSplitBarDarkMode();
                        return;
                    case 9:
                        HwMultiWindowSplitUI.this.mIsDragFullZoneDragable = false;
                        return;
                }
            }
        };
    }

    public void addDividerBarWindow(int windowMode) {
        if (!(this.mWindowMode == windowMode || this.mWindowMode == -1)) {
            Slog.i(TAG, " windowMode to be changed, windowMode = " + windowMode + " mWindowMode = " + this.mWindowMode);
            this.mOldWindowMode = this.mWindowMode;
        }
        this.mWindowMode = windowMode;
        Slog.i(TAG, "addDividerBarWindow, mOldWindowMode = " + this.mOldWindowMode + " mWindowMode = " + this.mWindowMode);
        if (this.mCoverLp == null || !(this.mDragView.getVisibility() == 0 || this.mDragFullZone.getVisibility() == 0)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(0), 200);
            this.mIsAddedSplitBar = true;
        } else if (this.mIsLeftAndRightPos && isNeedSendMessageUpdate()) {
            Slog.i(TAG, "addDividerBarWindow, mCoverLp not null and mDragView visible, update splitbar");
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), 100);
        }
    }

    private boolean isNeedSendMessageUpdate() {
        if (this.mWindowMode == 103) {
            return !this.mIsMoving;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createSplitCover() {
        Slog.i(TAG, "createSplitCover, displayid:" + this.mDisplayId);
        this.mIsNeedSetSplitBarVisible = true;
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4), 350);
        if (!this.mIsAddedSplitBar) {
            Slog.i(TAG, "createSplitCover return mIsAddedSplitBar = " + this.mIsAddedSplitBar);
            return;
        }
        if (this.mContext == null) {
            this.mContext = this.mActivityTaskManagerService.mUiContext;
        }
        this.mWm = (WindowManager) this.mContext.getSystemService("window");
        this.mHwMultiWindowManager = HwMultiWindowManager.getInstance(this.mActivityTaskManagerService);
        this.mVibrator = (Vibrator) this.mContext.getSystemService(Vibrator.class);
        if (this.mCoverLp == null) {
            loadDimens();
        }
        if (this.mCoverLp != null) {
            setPosition();
            Slog.i(TAG, "mCoverLp not null, just setPosition");
        } else if (this.mCoverLayout == null) {
            Slog.i(TAG, "mCoverLayout is null");
        } else {
            this.mLeftImg = new ImageView(this.mContext);
            this.mRightImg = new ImageView(this.mContext);
            initDragView(this.mCoverLayout);
            setWindowParams();
            this.mIsSplitBarVisibleNow = true;
            if (this.mWindowMode == 103 && this.mThresholdDistance == 0) {
                this.mThresholdDistance = getThresholdDistance(this.mDisplayId);
            }
            Slog.i(TAG, "createSplitCover end");
        }
    }

    private boolean isInDragFullMode() {
        HwMultiWindowManager hwMultiWindowManager = this.mHwMultiWindowManager;
        if (hwMultiWindowManager != null) {
            return hwMultiWindowManager.isDragFullModeByDisplayId(this.mDisplayId, this.mWindowMode);
        }
        return false;
    }

    private boolean isCanDragToFullscreen() {
        if (this.mWindowMode == 103) {
            return HwMwUtils.performPolicy(109, new Object[]{Integer.valueOf(this.mDisplayId)}).getBoolean("RESULT_CAN_DRAG_TO_FS", false);
        }
        return false;
    }

    private void setWindowParams() {
        Slog.i(TAG, "set window params");
        this.mCoverLp = new WindowManager.LayoutParams();
        WindowManager.LayoutParams layoutParams = this.mCoverLp;
        layoutParams.flags = 8651529;
        layoutParams.privateFlags |= 64;
        WindowManager.LayoutParams layoutParams2 = this.mCoverLp;
        layoutParams2.type = HwArbitrationDEFS.MSG_QUERY_QOE_WM_TIMEOUT;
        layoutParams2.layoutInDisplayCutoutMode = 1;
        layoutParams2.layoutInDisplaySideMode = 1;
        layoutParams2.setTitle(HwSplitBarConstants.WINDOW_TITLE);
        setPosition();
        WindowManager.LayoutParams layoutParams3 = this.mCoverLp;
        layoutParams3.format = -3;
        layoutParams3.gravity = 51;
        if (isInDragFullMode()) {
            this.mDragView.setVisibility(4);
        }
        this.mCoverLayout.setSystemUiVisibility(4);
        try {
            this.mWm.addView(this.mCoverLayout, this.mCoverLp);
        } catch (WindowManager.InvalidDisplayException e) {
            Slog.e(TAG, "setWindowParams add view error.");
        }
    }

    private void windowMax(View backgroundLayout, MotionEvent event) {
        createGaussianBlurCover(event);
        this.mLeftCover.setVisibility(0);
        this.mRightCover.setVisibility(0);
        boolean isNaviBarMini = isNavBarMini();
        Slog.i(TAG, " isNaviBarMini = " + isNaviBarMini + " sNavBarH = " + sNavBarHeight + " sNavBarW = " + sNavBarWidth);
        setLeftCoverParams(isNaviBarMini);
        if (this.mCoverLp != null) {
            this.mIsNavBarMini = isNaviBarMini;
            this.mNavBarPosWhenActionDown = this.mNavBarPos;
            setCoverLayoutParams(isNaviBarMini);
            this.mWm.updateViewLayout(backgroundLayout, this.mCoverLp);
            this.mDragView.setVisibility(0);
            if (this.mWindowMode == 103 && isRealTablet(this.mDisplayId) && HwMwUtils.ENABLED && this.mLeftCover.getBackground() == null) {
                setCoverBackground();
            }
            addAppIcon();
        }
    }

    private void setDragLineTrans() {
        int i = this.mDragFullZoneLastY;
        if (i != -1) {
            float transY = (float) ((i - ((this.mDisplayHeight - this.mDragFullZoneHeight) / 2)) + this.mDragFullZonePadding);
            this.mDragView.setTranslationY(transY);
            this.mDragFullZone.setTranslationY(transY);
        }
    }

    private void setLeftCoverParams(boolean isNaviBarMini) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.mLeftCover.getLayoutParams();
        this.mBottom = this.mHwMultiWindowManager.getPrimaryStackPos(this.mDisplayId);
        Slog.i(TAG, "windowMax bottom = " + this.mBottom);
        if (!this.mIsLeftAndRightPos) {
            params.height = this.mBottom - this.primaryBounds.top;
            params.width = this.mDisplayWidth - this.primaryBounds.left;
        } else if (!isNaviBarMini) {
            int i = this.mNavBarPos;
            if (i == 4) {
                params.width = this.mBottom - this.primaryBounds.left;
                params.height = (this.mDisplayHeight - this.primaryBounds.top) - sNavBarWidth;
            } else if (i == 1) {
                params.width = (this.mBottom - this.primaryBounds.left) - sNavBarWidth;
                params.height = this.mDisplayHeight - this.primaryBounds.top;
            } else {
                params.width = this.mBottom - this.primaryBounds.left;
                params.height = this.mDisplayHeight - this.primaryBounds.top;
            }
        } else {
            params.width = this.mBottom - this.primaryBounds.left;
            params.height = this.mDisplayHeight - this.primaryBounds.top;
        }
        this.mLeftCover.setLayoutParams(params);
    }

    private void setCoverLayoutParams(boolean isNaviBarMini) {
        this.mCoverLp.y = this.primaryBounds.top;
        this.mCoverLp.x = this.primaryBounds.left;
        int statusBarH = getNotchSizeOnRight();
        if (!isNaviBarMini) {
            int i = this.mNavBarPos;
            if (i == 4) {
                this.mCoverLp.height = (this.mDisplayHeight - this.primaryBounds.top) - sNavBarHeight;
                this.mCoverLp.width = (this.mDisplayWidth - this.primaryBounds.left) - statusBarH;
            } else if (i == 2 || i == 1) {
                this.mCoverLp.height = this.mDisplayHeight - this.primaryBounds.top;
                this.mCoverLp.width = ((this.mDisplayWidth - this.primaryBounds.left) - sNavBarWidth) - statusBarH;
            } else {
                this.mCoverLp.height = this.mDisplayHeight - this.primaryBounds.top;
                this.mCoverLp.width = (this.mDisplayWidth - this.primaryBounds.left) - statusBarH;
            }
        } else {
            this.mCoverLp.height = this.mDisplayHeight - this.primaryBounds.top;
            this.mCoverLp.width = (this.mDisplayWidth - this.primaryBounds.left) - statusBarH;
        }
    }

    private void addAppIcon() {
        Drawable leftDrawable;
        Drawable rightDrawable;
        Drawable leftDrawable2;
        Drawable rightDrawable2;
        Bundle bundle = this.mHwMultiWindowManager.getStackPackageNames(true, this.mDisplayId);
        if (bundle != null) {
            List<String> packageName = bundle.getStringArrayList("pkgNames");
            List<Integer> userIds = bundle.getIntegerArrayList("pkgUserIds");
            if (packageName != null && userIds != null) {
                boolean isMagic = this.mWindowMode == 103 && packageName.size() == 1 && userIds.size() == 1;
                boolean isSplit = (this.mWindowMode == 100 && packageName.size() == 2) || userIds.size() == 2;
                if (isMagic) {
                    rightDrawable = getAppIcon(packageName.get(0), userIds.get(0).intValue());
                    leftDrawable = getAppIcon(packageName.get(0), userIds.get(0).intValue());
                } else if (isSplit) {
                    List<Integer> appTypes = bundle.getIntegerArrayList("appTypes");
                    if (appTypes.get(0).intValue() == 1) {
                        leftDrawable2 = HwMultiWinUtils.getQuickNoteIcon(this.mContext, userIds.get(0).intValue());
                    } else {
                        leftDrawable2 = getAppIcon(packageName.get(0), userIds.get(0).intValue());
                    }
                    if (appTypes.get(1).intValue() == 1) {
                        rightDrawable2 = HwMultiWinUtils.getQuickNoteIcon(this.mContext, userIds.get(1).intValue());
                    } else {
                        rightDrawable2 = getAppIcon(packageName.get(1), userIds.get(1).intValue());
                    }
                    leftDrawable = rightDrawable2;
                    rightDrawable = leftDrawable2;
                } else {
                    Slog.i(TAG, "mWindowMode = " + this.mWindowMode + " package count = " + packageName.size() + " userId count = " + userIds.size());
                    return;
                }
                if (rightDrawable == null || leftDrawable == null) {
                    Slog.i(TAG, "leftDrawable or rightDrawable is null. leftDrawable = " + rightDrawable + " rightDrawable = " + leftDrawable);
                }
                this.mLeftImg.setBackground(rightDrawable);
                this.mRightImg.setBackground(leftDrawable);
                if (this.mLeftImg.getParent() == null && this.mRightImg.getParent() == null) {
                    Interpolator standardCurve = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
                    addIconViewAnim(this.mLeftCover, this.mLeftImg, standardCurve);
                    addIconViewAnim(this.mRightCover, this.mRightImg, standardCurve);
                    return;
                }
                Slog.i(TAG, "addAppIcon, mLeftImg has parent already");
            }
        }
    }

    private Drawable getAppIcon(String pkgName, int userId) {
        Drawable icon = HwMultiWinUtils.getAppIcon(this.mContext, pkgName, userId);
        if (icon != null) {
            icon.setAlpha(255);
        }
        return icon;
    }

    private void addIconViewAnim(RelativeLayout layout, ImageView icon, Interpolator standardCurve) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
        params.addRule(13, -1);
        Rect iconBounds = icon.getBackground() != null ? icon.getBackground().getBounds() : null;
        if (iconBounds != null) {
            icon.setMinimumWidth(iconBounds.width());
            icon.setMinimumHeight(iconBounds.height());
        }
        layout.addView(icon, params);
        icon.setScaleX(0.85f);
        icon.setScaleY(0.85f);
        icon.setAlpha(0.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(icon, View.ALPHA, icon.getAlpha(), 1.0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(icon, View.SCALE_X, icon.getScaleX(), 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(icon, View.SCALE_Y, icon.getScaleX(), 1.0f);
        AnimatorSet sets = new AnimatorSet();
        sets.playTogether(alpha, scaleX, scaleY);
        sets.setDuration(200L);
        sets.setInterpolator(standardCurve);
        sets.start();
    }

    private void initDragView(View backgroundLayout) {
        this.mDragView = backgroundLayout.findViewById(34603268);
        this.mDragLine = (ImageView) backgroundLayout.findViewById(34603265);
        this.mDragPoint = (ImageView) backgroundLayout.findViewById(34603267);
        this.mDragRegion = (ImageView) backgroundLayout.findViewById(34603262);
        this.mDragFullZone = (RelativeLayout) backgroundLayout.findViewById(34603264);
        this.mFarLine = (ImageView) backgroundLayout.findViewById(34603263);
        this.mNearLine = (ImageView) backgroundLayout.findViewById(34603266);
        initDragFullModeViews();
        this.mDragRegion.setOnTouchListener(new View.OnTouchListener(backgroundLayout) {
            /* class com.android.server.wm.$$Lambda$HwMultiWindowSplitUI$brKiWjKFoKgZDS8LoIgYshv6pTk */
            private final /* synthetic */ View f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return HwMultiWindowSplitUI.this.lambda$initDragView$1$HwMultiWindowSplitUI(this.f$1, view, motionEvent);
            }
        });
        if (this.mWindowMode == 103) {
            this.mDragView.setVisibility(0);
        } else {
            this.mDragView.setVisibility(4);
        }
    }

    public /* synthetic */ boolean lambda$initDragView$1$HwMultiWindowSplitUI(View backgroundLayout, View v, MotionEvent event) {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.addMovement(event);
        }
        int action = event.getAction();
        if (action != 0) {
            if (action == 1) {
                Slog.i(TAG, "ACTION_UP ");
                updateCoverViewGone(event);
            } else if (action == 2) {
                executeActionMove(event, backgroundLayout);
            } else if (action == 3) {
                Slog.i(TAG, "ACTION_CANCEL");
                updateCoverViewGone(event);
            }
            return true;
        }
        Slog.i(TAG, "ACTION_DOWN");
        initActionDown(event, backgroundLayout);
        return true;
    }

    private void initDragFullModeViews() {
        this.mFarLine.setOutlineProvider(new DragLineOutlineProvider());
        this.mNearLine.setOutlineProvider(new DragLineOutlineProvider());
        this.mFarLine.setClipToOutline(true);
        this.mNearLine.setClipToOutline(true);
        this.mFarLine.setBackgroundColor(this.mDragFullLineColor);
        this.mNearLine.setBackgroundColor(this.mDragFullLineColor);
        this.mDragFullZone.setAlpha(0.7f);
    }

    private boolean isDragZoneForMagicWin(MotionEvent event) {
        if (HwMwUtils.performPolicy(109, new Object[]{Integer.valueOf(this.mDisplayId)}).getBoolean("RESULT_CAN_DRAG_TO_FS", false)) {
            return true;
        }
        if (event.getRawX() >= ((float) (this.mDisplayWidth / 6))) {
            float rawX = event.getRawX();
            int i = this.mDisplayWidth;
            if (rawX <= ((float) (i - (i / 6)))) {
                return true;
            }
        }
        return false;
    }

    private void initActionDown(MotionEvent event, View view) {
        DisplayRotation displayRotation;
        if (this.mCoverLp != null) {
            this.mDragRegion.setVisibility(4);
            ActivityDisplay activityDisplay = this.mActivityTaskManagerService.getRootActivityContainer().getActivityDisplay(this.mDisplayId);
            if (!(activityDisplay == null || activityDisplay.mDisplayContent == null || (displayRotation = activityDisplay.mDisplayContent.getDisplayRotation()) == null || displayRotation.getUserRotationMode() != 0 || !displayRotation.getDisplayModeChange())) {
                displayRotation.setDisplayModeChange(false);
            }
            if (this.mDisplayId == 0) {
                this.userRotationLocked = Settings.System.getIntForUser(this.mContext.getContentResolver(), "accelerometer_rotation", 0, -2);
                RotationPolicy.setRotationLockAtAngle(this.mContext, true, this.mRotation);
            }
            this.mBottom = this.mHwMultiWindowManager.getPrimaryStackPos(this.mDisplayId);
            this.mExitRegion = 0;
            this.mDragView.setVisibility(4);
            this.mMoveCount = 0;
            this.mDown = true;
            this.mIsActionDown = true;
            this.mIsSetBackground = false;
            this.mCoverLp.flags &= -9;
            this.mWm.updateViewLayout(view, this.mCoverLp);
            this.mPreDownX = event.getRawX();
            this.mPreDownY = event.getRawY();
            this.mFloatState = HwMultiWinUtils.getFloatTaskState(this.mContext);
            HwMultiWinUtils.putFloatTaskStateToSettings(false, this.mContext);
            setDateBundleInActionDown();
            this.mIsMoving = false;
            if (this.mWindowMode == 103) {
                this.mPreDownXTypeInMagic = changeSplitRatio(event, true);
            }
            if (isInDragFullMode()) {
                dragFullZoneActionDown();
            } else {
                this.mDragFullZoneLastY = -1;
            }
        }
    }

    private void dragFullZoneActionDown() {
        if (this.mPreDownXTypeInMagic == 6) {
        }
        this.mIsDragFullZoneDragable = true;
        this.mHandler.removeMessages(9);
        this.mHandler.sendEmptyMessageDelayed(9, 200);
        this.mDragFullZone.setAlpha(0.5f);
        this.mAnimBundle.putInt(HwSplitBarConstants.SPLIT_RATIO, this.mPreDownXTypeInMagic);
        HwDragFullAnimStrategy strategy = HwDragFullAnimStrategy.getStrategy(this.mActivityTaskManagerService, this.mDragFullZone, this.mFarLine, this.mNearLine, this.mDragLine, this.mAnimBundle);
        if (strategy != null) {
            strategy.dragFullZoneScaleDownAnim();
        }
    }

    private boolean executeActionMove(MotionEvent event, View view) {
        WindowManager.LayoutParams layoutParams;
        this.mMoveCount++;
        if (this.mMoveCount < 2) {
            return true;
        }
        if (!this.mIsDragFullZoneDragable || !isInDragFullMode()) {
            if (isCanUpdateCoverSize(event)) {
                if (this.mDown) {
                    windowMax(view, event);
                    this.mDown = false;
                    if (isInDragFullMode()) {
                        setDragLineTrans();
                        this.mIsDragFullAnimRunning = true;
                        vibrateTip();
                        runExitDragFullAnim(this.mHwMultiWindowManager.getTaskDragFullMode(this.mDisplayId));
                    }
                    if (isCanDragToFullscreen() && !HwFoldScreenManager.isFoldable()) {
                        this.mDragPoint.setVisibility(0);
                    }
                    return true;
                } else if (this.mIsDragFullAnimRunning) {
                    return true;
                } else {
                    if (!this.mIsSetBackground && this.mLeftCover.getBackground() != null && (layoutParams = this.mCoverLp) != null && layoutParams.width > this.mWindowWH && this.mCoverLp.height > this.mWindowWH) {
                        this.mCoverLayout.setBackgroundColor(-16777216);
                        this.mIsSetBackground = true;
                    }
                    if (this.mIsMoving || exceededTouchSlop(event)) {
                        upadateCoverSize(event.getRawX(), event.getRawY());
                        return true;
                    }
                    updateCoverLpIfNeed();
                }
            }
            return false;
        }
        dragFullZoneMove(event);
        return true;
    }

    private void dragFullZoneMove(MotionEvent event) {
        int rawY = (int) event.getRawY();
        int i = this.mDragFullZoneSlop;
        boolean isDragFullZoneCanMove = rawY > i && rawY < this.mDisplayHeight - i;
        if (exceededTouchSlop(event) && isDragFullZoneCanMove) {
            if (this.mDown) {
                this.mDown = false;
                this.mHandler.removeMessages(9);
                this.mHandler.removeMessages(8);
            }
            updateDragFullZonePos(rawY);
        }
    }

    private void updateDragFullZonePos(int rawY) {
        this.mDragFullZoneLastY = rawY - (this.mCoverLp.height / 2);
        this.mCoverLp.y = this.mDragFullZoneLastY;
        updateCoverLayout();
    }

    private boolean isCanUpdateCoverSize(MotionEvent event) {
        if (this.mWindowMode == 103 && isRealTablet(this.mDisplayId) && HwMwUtils.ENABLED) {
            return isDragZoneForMagicWin(event);
        }
        return true;
    }

    private void updateCoverLpIfNeed() {
        boolean isNavBarMini;
        if (this.mCoverLp != null && (isNavBarMini = isNavBarMini()) != this.mIsNavBarMini && isNavBarMini) {
            int i = this.mNavBarPosWhenActionDown;
            if (i == 4) {
                this.mCoverLp.height += sNavBarHeight;
            } else if (i == 1 || i == 2) {
                this.mCoverLp.width += sNavBarWidth;
            }
            this.mWm.updateViewLayout(this.mCoverLayout, this.mCoverLp);
            this.mIsNavBarMini = isNavBarMini;
        }
    }

    private boolean exceededTouchSlop(MotionEvent event) {
        boolean isLeftOrRight = this.mIsLeftAndRightPos && !this.mIsDragFullZoneDragable;
        if (((double) (Math.abs((isLeftOrRight ? event.getRawX() : event.getRawY()) - (isLeftOrRight ? this.mPreDownX : this.mPreDownY)) - ((float) this.mTouchSlop))) <= 1.0E-5d) {
            return false;
        }
        if (!this.mIsMoving) {
            this.mIsMoving = true;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void upadateCoverSize(float rawX, float rawY) {
        if (this.mCoverLp != null) {
            dragToFullScreen(rawX, rawY);
            RelativeLayout.LayoutParams paramsLeftCover = (RelativeLayout.LayoutParams) this.mLeftCover.getLayoutParams();
            updateCoverLpIfNeed();
            int i = 0;
            if (this.mIsLeftAndRightPos) {
                if (((int) rawX) - this.primaryBounds.left >= 0) {
                    i = ((int) rawX) - this.primaryBounds.left;
                }
                paramsLeftCover.width = i;
                paramsLeftCover.height = this.mCoverLp.height;
            } else {
                if (((int) rawY) - this.primaryBounds.top >= 0) {
                    i = ((int) rawY) - this.primaryBounds.top;
                }
                paramsLeftCover.height = i;
            }
            this.mLeftCover.setLayoutParams(paramsLeftCover);
        }
    }

    public boolean isNavBarMini() {
        DisplayContent displayContent = this.mActivityTaskManagerService.getRootActivityContainer().getActivityDisplay(this.mDisplayId).mDisplayContent;
        boolean isNavBarMini = displayContent.getDisplayPolicy().mHwDisplayPolicyEx.isNaviBarMini();
        this.mNavBarPos = displayContent.getDisplayPolicy().navigationBarPosition(displayContent.getDisplayInfo().logicalWidth, displayContent.getDisplayInfo().logicalHeight, displayContent.getRotation());
        return isNavBarMini;
    }

    public int getNotchSizeOnRight() {
        int notchSize;
        int notchSize2 = 0;
        if (!HwMultiWindowManager.IS_NOTCH_PROP || this.mRotation != 3 || !this.mIsLeftAndRightPos) {
            notchSize = 0;
        } else {
            notchSize = sStatusBarHeight;
        }
        HwMultiWindowManager manager = HwMultiWindowManager.getInstance(this.mActivityTaskManagerService);
        if (manager != null && manager.hasCutout(this.mRotation)) {
            notchSize2 = notchSize;
        }
        return notchSize2;
    }

    public int getNavBarBottomUpDown() {
        if (isNavBarMini() || this.mNavBarPos != 4 || this.mIsLeftAndRightPos) {
            return 0;
        }
        return sNavBarHeight;
    }

    public int getNavBarRight() {
        if (isNavBarMini() || this.mNavBarPos != 2 || !this.mIsLeftAndRightPos) {
            return 0;
        }
        return sNavBarWidth;
    }

    private void createGaussianBlurCover(final MotionEvent event) {
        new Thread() {
            /* class com.android.server.wm.HwMultiWindowSplitUI.AnonymousClass2 */

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Bitmap[] tmpBitmaps;
                setName("GaussianBlur");
                ActivityStack stackPrimary = HwMultiWindowSplitUI.this.mHwMultiWindowManager.getSplitScreenTopStack(HwMultiWindowSplitUI.this.mDisplayId);
                if (stackPrimary == null || !stackPrimary.inHwMagicWindowingMode()) {
                    ActivityStack stackPrimary2 = HwMultiWindowSplitUI.this.mHwMultiWindowManager.getSplitScreenPrimaryStack(HwMultiWindowSplitUI.this.mDisplayId);
                    if (stackPrimary2 == null || stackPrimary2.getChildAt(0) == null) {
                        Slog.i(HwMultiWindowSplitUI.TAG, "createGaussianBlurCover, stackPrimary null");
                        return;
                    }
                    int[] taskIds = HwMultiWindowSplitUI.this.mHwMultiWindowManager.getCombinedSplitScreenTaskIds(stackPrimary2);
                    if (taskIds == null || taskIds.length == 0) {
                        Slog.i(HwMultiWindowSplitUI.TAG, "createGaussianBlurCover, getCombinedSplitScreenTaskIDs return null");
                        return;
                    }
                    Bitmap[] tmpBitmaps2 = {HwMultiWindowSplitUI.this.getTaskSnapshotBitmap(stackPrimary2.getChildAt(0).taskId, false), HwMultiWindowSplitUI.this.getTaskSnapshotBitmap(taskIds[0], false)};
                    if (tmpBitmaps2[1] == null || tmpBitmaps2[0] == null) {
                        Slog.i(HwMultiWindowSplitUI.TAG, "createGaussianBlurCover, mLeftBitmap null");
                        return;
                    }
                    tmpBitmaps = tmpBitmaps2;
                } else {
                    tmpBitmaps = HwMultiWindowSplitUI.this.createCoverBlurBitmap(event);
                    if (tmpBitmaps == null || (tmpBitmaps[0] == null && tmpBitmaps[1] == null)) {
                        Slog.i(HwMultiWindowSplitUI.TAG, "createGaussianBlurCover setCoverBackground");
                        HwMultiWindowSplitUI.this.setCoverBackground();
                        return;
                    }
                }
                HwMultiWindowSplitUI.this.createGaussBitmap(tmpBitmaps);
                HwMultiWindowSplitUI.this.recycleBitmap(tmpBitmaps[0]);
                HwMultiWindowSplitUI.this.recycleBitmap(tmpBitmaps[1]);
                HwMultiWindowSplitUI.this.mLeftCover.post(new Runnable() {
                    /* class com.android.server.wm.$$Lambda$HwMultiWindowSplitUI$2$ht1kvRuoEwMJJE85xFscO1HwsI */

                    @Override // java.lang.Runnable
                    public final void run() {
                        HwMultiWindowSplitUI.AnonymousClass2.this.lambda$run$0$HwMultiWindowSplitUI$2();
                    }
                });
            }

            public /* synthetic */ void lambda$run$0$HwMultiWindowSplitUI$2() {
                HwMultiWindowSplitUI hwMultiWindowSplitUI = HwMultiWindowSplitUI.this;
                hwMultiWindowSplitUI.setCoverCorner(hwMultiWindowSplitUI.mLeftCover, HwMultiWindowSplitUI.this.mLeftGaussBitmap);
                HwMultiWindowSplitUI hwMultiWindowSplitUI2 = HwMultiWindowSplitUI.this;
                hwMultiWindowSplitUI2.setCoverCorner(hwMultiWindowSplitUI2.mRightCover, HwMultiWindowSplitUI.this.mRightGaussBitmap);
            }
        }.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createGaussBitmap(Bitmap[] tmpBitmaps) {
        if (tmpBitmaps[0] != null) {
            this.mLeftGaussBitmap = HwBlur.blur(tmpBitmaps[0], 200, 20, true);
        }
        if (tmpBitmaps[1] != null) {
            this.mRightGaussBitmap = HwBlur.blur(tmpBitmaps[1], 200, 20, true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap.recycle();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCoverCorner(RelativeLayout layout, Bitmap bitmap) {
        layout.setBackground(new BitmapDrawable(bitmap));
        layout.setClipToOutline(true);
        layout.setOutlineProvider(ViewOutlineProvider.HW_MULTIWINDOW_SPLITSCREEN_OUTLINE_PROVIDER);
        View rect = getBoarderRect();
        if (rect != null && rect.getParent() == null && this.mUiMode == 32) {
            layout.addView(rect);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Bitmap getTaskSnapshotBitmap(int taskId, boolean reducedResolution) {
        ActivityManager.TaskSnapshot snapshot = HwActivityTaskManager.getTaskSnapshot(taskId, reducedResolution);
        if (snapshot == null) {
            return null;
        }
        return Bitmap.wrapHardwareBuffer(HardwareBuffer.createFromGraphicBuffer(snapshot.getSnapshot()), snapshot.getColorSpace());
    }

    private Bitmap takeScreenshot() {
        Bitmap screenShot = SurfaceControl.screenshot(new Rect(0, 0, this.mDisplayWidth, this.mDisplayHeight), this.mDisplayWidth, this.mDisplayHeight, this.mRotation);
        if (screenShot != null) {
            screenShot.setHasAlpha(false);
        }
        return screenShot;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Bitmap[] createCoverBlurBitmap(MotionEvent event) {
        Rect[] dragBounds;
        ActivityStack stackPrimary = this.mHwMultiWindowManager.getSplitScreenTopStack(this.mDisplayId);
        if (stackPrimary == null || stackPrimary.getChildAt(0) == null) {
            return null;
        }
        Bitmap screenShot = this.mDisplayId == 0 ? takeScreenshot() : getTaskSnapshotBitmap(stackPrimary.getChildAt(0).taskId, false);
        if (screenShot == null || (dragBounds = this.mHwMultiWindowManager.getRectForScreenShotForDrag(changeSplitRatio(event, stackPrimary.inHwMagicWindowingMode()), this.mDisplayId)) == null || dragBounds.length < 2) {
            return null;
        }
        return new Bitmap[]{createBlurBitmap(dragBounds[0], screenShot), createBlurBitmap(dragBounds[1], screenShot)};
    }

    private Bitmap createBlurBitmap(Rect bound, Bitmap screenShot) {
        if (bound.width() == 0 || bound.height() == 0) {
            return null;
        }
        try {
            return Bitmap.createBitmap(screenShot, bound.left, bound.top, bound.width(), bound.height());
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "create bitmap fail.");
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCoverBackground() {
        this.mLeftCover.post(new Runnable() {
            /* class com.android.server.wm.HwMultiWindowSplitUI.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                HwMultiWindowSplitUI.this.mLeftCover.setBackgroundResource(33751934);
                HwMultiWindowSplitUI.this.mRightCover.setBackgroundResource(33751934);
            }
        });
    }

    private void updateCoverViewGone(MotionEvent event) {
        boolean z = true;
        if (this.mFloatState == 1) {
            HwMultiWinUtils.putFloatTaskStateToSettings(true, this.mContext);
        }
        WindowManager.LayoutParams layoutParams = this.mCoverLp;
        if (layoutParams != null) {
            this.mMoveCount = 0;
            layoutParams.flags |= 8;
            if (!this.mIsDragFullZoneDragable || !isInDragFullMode()) {
                ActivityStack activityStack = this.mHwMultiWindowManager.getSplitScreenTopStack(this.mDisplayId);
                if (activityStack != null) {
                    if ((this.mIsMoving || this.mWindowMode == 103) && isCanUpdateCoverSize(event)) {
                        upadateCoverSize(event.getRawX(), event.getRawY());
                    }
                    this.mDragPoint.setVisibility(8);
                    this.mDragFullZone.setTranslationY(0.0f);
                    int type = changeSplitRatio(getTargetPosition(event), activityStack.inHwMagicWindowingMode());
                    this.mAnimBundle.remove(HwSplitBarConstants.IS_COVER_ANIM_DELAY);
                    if (activityStack.inHwMagicWindowingMode()) {
                        Bundle bundle = this.mAnimBundle;
                        if (!HwFoldScreenManager.isFoldable() || !isInDragFullMode()) {
                            z = false;
                        }
                        bundle.putBoolean(HwSplitBarConstants.IS_COVER_ANIM_DELAY, z);
                        this.mHwMultiWindowManager.resizeMagicWindowBounds(activityStack, type);
                    } else {
                        this.mHwMultiWindowManager.resizeHwSplitStacks(type, true);
                    }
                    this.mIsActionDown = false;
                    if (!this.mHwMultiWindowManager.isDragFullModeByType(type)) {
                        this.mDragFullZoneLastY = -1;
                    }
                    updatePosViewGone(type, event);
                    return;
                }
                return;
            }
            dragFullZoneMoveGone();
        }
    }

    private void updatePosViewGone(int type, MotionEvent event) {
        float endPosition = (float) this.mHwMultiWindowManager.getPrimaryStackPos(this.mDisplayId);
        float curPosition = this.mIsLeftAndRightPos ? event.getRawX() : event.getRawY();
        setDataBundleInActionUp(curPosition, endPosition);
        this.mAnimBundle.putInt(HwSplitBarConstants.SPLIT_RATIO, type);
        if (this.mExitRegion != 0) {
            updateSplitBarPosition(type, curPosition);
        } else if (this.mWindowMode != 103 || isDragZoneForMagicWin(event)) {
            if (!this.mIsMoving && this.mWindowMode != 103) {
                this.mAnimBundle.putFloat(HwSplitBarConstants.CURRENT_POSITION, endPosition);
            }
            HwSplitBarReboundStrategy strategy = HwSplitBarReboundStrategy.getStrategy(this.mActivityTaskManagerService, this.mLeftCover, this.mDragView, this.mRightCover, this.mAnimBundle);
            if (strategy == null) {
                updateSplitBarPosition(type, curPosition);
            } else {
                strategy.startReboundAnim();
            }
        } else {
            Message msg = this.mHandler.obtainMessage(2);
            msg.arg1 = type;
            msg.obj = Float.valueOf(curPosition);
            this.mHandler.sendMessageDelayed(msg, (long) (this.mIsMoving ? 200 : 0));
        }
    }

    private void dragFullZoneMoveGone() {
        this.mDragRegion.setVisibility(0);
        this.mIsDragFullZoneDragable = false;
        this.mDragFullZone.setAlpha(0.7f);
        setSplitBarDarkDelay();
        HwDragFullAnimStrategy strategy = HwDragFullAnimStrategy.getStrategy(this.mActivityTaskManagerService, this.mDragFullZone, this.mFarLine, this.mNearLine, this.mDragLine, this.mAnimBundle);
        if (strategy != null) {
            strategy.dragFullZoneScaleGoneAnim();
        }
    }

    public void resetDragFullZoneAfterScale(int type) {
        setDragFullCoverLayout(type == 6);
    }

    private float getTargetPosition(MotionEvent event) {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.computeCurrentVelocity(300, 2000.0f);
            this.mVelocity = this.mVelocityTracker.getXVelocity();
            this.mVelocityTracker.clear();
        }
        if (isCanDragToFullscreen() && this.mIsLeftAndRightPos && this.mPreDownXTypeInMagic == 0) {
            float distance = event.getRawX() - this.mPreDownX;
            if (Math.abs(this.mVelocity) > 1000.0f && Math.abs(distance) > ((float) this.mThresholdDistance)) {
                int targetPostion = distance < 0.0f ? 0 : this.mDisplayWidth;
                this.mExitRegion = distance < 0.0f ? 1 : 2;
                return (float) targetPostion;
            }
        }
        return this.mIsLeftAndRightPos ? event.getRawX() : event.getRawY();
    }

    public void updateSplitBarPosition(int type, float curPos) {
        this.mIsSetBackground = false;
        if (this.mCoverLp != null) {
            Slog.i(TAG, "updateSplitBarPosition, type =" + type + " curPos =" + curPos);
            this.mDragRegion.setVisibility(0);
            if (!(type == 3 || type == 4 || this.mHwMultiWindowManager.isDragFullModeByType(type))) {
                removeCovers();
                if (this.mIsLeftAndRightPos) {
                    this.mCoverLp.width = this.mWindowWH;
                } else {
                    this.mCoverLp.height = this.mWindowWH;
                }
            }
            updateViewPos(type, curPos);
            if (this.mDisplayId == 0) {
                RotationPolicy.setRotationLock(this.mContext, this.userRotationLocked == 0);
                this.userRotationLocked = -1;
            }
            if (this.mWindowMode == 103 && this.mExitRegion == 0) {
                this.mIsMoving = false;
            }
        }
    }

    private void removeCovers() {
        this.mLeftCover.removeAllViews();
        this.mRightCover.removeAllViews();
        this.mLeftCover.setVisibility(8);
        this.mRightCover.setVisibility(8);
        this.mDragView.setVisibility(0);
        this.mCoverLayout.setBackgroundColor(0);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void updateViewPos(int displayType, float curPos) {
        if (this.mCoverLp != null) {
            this.mBottom = this.mHwMultiWindowManager.getPrimaryStackPos(this.mDisplayId);
            Slog.i(TAG, "updateViewPos bottom = " + this.mBottom + " displayType =" + displayType);
            switch (displayType) {
                case 0:
                case 1:
                case 2:
                    int i = this.mBottom;
                    if (i == 0) {
                        Slog.i(TAG, "updateViewPos fail, will remove split bar.");
                        if (this.mAnimBundle.getFloat(HwSplitBarConstants.END_POSITION) == 0.0f) {
                            removeSplitBarWindow();
                            return;
                        } else {
                            removeSplit(this.mWindowMode, true);
                            return;
                        }
                    } else {
                        if (this.mIsLeftAndRightPos) {
                            this.mCoverLp.x = i - this.mWindowOffset;
                        } else {
                            this.mCoverLp.y = i - this.mWindowOffset;
                        }
                        this.mWm.updateViewLayout(this.mCoverLayout, this.mCoverLp);
                        return;
                    }
                case 3:
                case 4:
                    Slog.i(TAG, "updateViewPos, to full screen exitRegion = " + this.mExitRegion);
                    if (this.mExitRegion == 0) {
                        this.mHwMultiWindowManager.removeSplitScreenDividerBar(this.mWindowMode, false, this.mDisplayId);
                        return;
                    }
                    break;
                case 5:
                case 6:
                    break;
                default:
                    return;
            }
            setDragLineTrans();
            setDataBundleInActionUp(curPos, (float) this.mBottom);
            setEnterDragFullAnimData(displayType);
            HwSplitBarExitAniStrategy dragStrategy = HwSplitBarExitAniStrategy.getStrategy(this.mActivityTaskManagerService, this.mLeftCover, this.mDragView, this.mRightCover, this.mDragFullZone, this.mAnimBundle);
            if (dragStrategy != null) {
                dragStrategy.split2FullAnimation();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateViewPos(int displayType) {
        updateViewPos(displayType, 0.0f);
    }

    public void setLayoutBackground() {
        this.mCoverLayout.setBackgroundColor(0);
    }

    private int changeSplitRatio(MotionEvent event, boolean isMagicWindow) {
        return changeSplitRatio(this.mIsLeftAndRightPos ? event.getRawX() : event.getRawY(), isMagicWindow);
    }

    private int changeSplitRatio(float curPosition, boolean isMagicWindow) {
        int displayLen = this.mIsLeftAndRightPos ? this.mDisplayWidth : this.mDisplayHeight;
        int ratio = this.mIsLeftAndRightPos ? this.mWidthColumns : this.mHeightColumns;
        int offSet = this.mIsLeftAndRightPos ? this.primaryBounds.left : this.primaryBounds.top;
        int statusBarH = getNotchSizeOnRight();
        int navBarBottomUd = getNavBarBottomUpDown();
        int navBarRight = getNavBarRight();
        float rightExitThreshold = (float) ((((((ratio - 1) * displayLen) / ratio) - statusBarH) - navBarBottomUd) - navBarRight);
        if (!this.mIsOneGear) {
            if (!isMagicWindow) {
                if (curPosition < ((float) ((displayLen / ratio) + offSet))) {
                    return 4;
                }
                if (curPosition > rightExitThreshold) {
                    return 3;
                }
            } else if (HwMwUtils.performPolicy(109, new Object[]{Integer.valueOf(this.mDisplayId)}).getBoolean("RESULT_CAN_DRAG_TO_FS", false)) {
                if (curPosition < ((float) (((displayLen / ratio) * 2) + offSet))) {
                    return 6;
                }
                if (curPosition > ((float) ((((((ratio - 2) * displayLen) / ratio) - statusBarH) - navBarBottomUd) - navBarRight))) {
                    return 5;
                }
            }
            if (curPosition < ((float) offSet) + (((float) displayLen) * 0.42f)) {
                return 1;
            }
            if (curPosition > ((float) offSet) + (((float) displayLen) * 0.58f)) {
                return 2;
            }
            return 0;
        } else if (rightExitThreshold < curPosition) {
            return calcSplitRatioForMagicWin(isMagicWindow, false);
        } else {
            if (curPosition < ((float) ((displayLen / ratio) + offSet))) {
                return calcSplitRatioForMagicWin(isMagicWindow, true);
            }
            return 0;
        }
    }

    private int calcSplitRatioForMagicWin(boolean isMagicWindow, boolean isPrimaryLess) {
        if (!isMagicWindow) {
            if (isPrimaryLess) {
                return 4;
            }
            return 3;
        } else if (HwMwUtils.performPolicy(109, new Object[]{Integer.valueOf(this.mDisplayId)}).getBoolean("RESULT_CAN_DRAG_TO_FS", false)) {
            if (isPrimaryLess) {
                return 6;
            }
            return 5;
        } else if (isPrimaryLess) {
            return 1;
        } else {
            return 2;
        }
    }

    public void removeSplit(int windowMode, boolean immediately) {
        if (immediately || (this.mIsAddedSplitBar && windowMode == this.mWindowMode && !this.mIsActionDown)) {
            Message msg = this.mHandler.obtainMessage(1);
            this.mHandler.removeMessages(0);
            this.mHandler.removeMessages(4);
            this.mHandler.removeMessages(6);
            this.mHandler.removeMessages(8);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeSplitBarWindow() {
        if (this.mCoverLp != null) {
            if (this.mDisplayId == 0) {
                if (this.userRotationLocked == -1) {
                    this.userRotationLocked = Settings.System.getIntForUser(this.mContext.getContentResolver(), "accelerometer_rotation", 0, -2);
                }
                RotationPolicy.setRotationLock(this.mContext, this.userRotationLocked == 0);
                this.userRotationLocked = -1;
            }
            this.mCoverLayout.setVisibility(8);
            this.mRightCover.setVisibility(8);
            this.mDragView.setVisibility(8);
            setDragFullModeViewsVisibility(8);
            this.mWm.removeView(this.mCoverLayout);
            this.mCoverLp = null;
            this.mWindowMode = -1;
            this.mIsAddedSplitBar = false;
            this.mIsActionDown = false;
            Slog.i(TAG, "remove split bar end, display id = " + this.mDisplayId);
            HwMultiWindowManager.getInstance(this.mActivityTaskManagerService).mIsAddSplitBar = false;
            addSplitBarIfNeeded();
        }
    }

    private void addSplitBarIfNeeded() {
        int i = this.mOldWindowMode;
        if (!(i == -1 || i == this.mWindowMode)) {
            Slog.i(TAG, "add split bar after remove if needed. mWindowModeToAdded:" + this.mOldWindowMode + " mWindowMode:" + this.mWindowMode);
            this.mIsAddedSplitBar = true;
            this.mWindowMode = 103;
            createSplitCover();
        }
        this.mOldWindowMode = -1;
    }

    public void onConfigurationChanged(int displayId) {
        if (this.mDisplayId != displayId) {
            Slog.i(TAG, "displayId has changed");
            return;
        }
        Bundle bundle = getDisplayWidthAndHeight(displayId);
        if (bundle == null) {
            Slog.i(TAG, "get display width and height failed");
            return;
        }
        int height = bundle.getInt(HwSplitBarConstants.DISPLAY_HEIGHT);
        int width = bundle.getInt(HwSplitBarConstants.DISPLAY_WIDTH);
        int rotation = bundle.getInt(HwSplitBarConstants.DISPLAY_ROTATION);
        int currentUiMode = this.mContext.getResources().getConfiguration().uiMode & 48;
        Slog.i(TAG, "mUiMode = " + this.mUiMode + "  current ui mode = " + currentUiMode);
        if (this.mUiMode == currentUiMode && this.mDisplayHeight == height && this.mDisplayWidth == width && rotation == this.mRotation) {
            HwMultiWindowManager hwMultiWindowManager = this.mHwMultiWindowManager;
            int bottom = hwMultiWindowManager != null ? hwMultiWindowManager.getPrimaryStackPos(this.mDisplayId) : this.mBottom;
            Slog.i(TAG, "orientation has not changed, mDisplayWidth: " + this.mDisplayWidth + ", current height: " + height + ", bottom: " + bottom + ", mBottom: " + this.mBottom);
            if (bottom == this.mBottom) {
                return;
            }
        }
        this.mUiMode = currentUiMode;
        this.mDisplayWidth = width;
        this.mDisplayHeight = height;
        this.mRotation = rotation;
        if (this.mCoverLp != null) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(3));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePositionOnConfigurationChanged() {
        if (this.mCoverLp != null) {
            this.mWm.removeView(this.mCoverLayout);
            loadDimens();
            initDragView(this.mCoverLayout);
            setPosition();
            this.mWm.addView(this.mCoverLayout, this.mCoverLp);
            this.mDragView.setVisibility(0);
            if (isInDragFullMode()) {
                setSplitBarDragFullMode(this.mHwMultiWindowManager.getTaskDragFullMode(this.mDisplayId));
            }
        }
    }

    private void setPosition() {
        int i;
        this.mBottom = this.mHwMultiWindowManager.getPrimaryStackPos(this.mDisplayId);
        if (this.mBottom != 0 || isInDragFullMode()) {
            boolean isNavBarMini = isNavBarMini();
            if (this.mIsLeftAndRightPos) {
                WindowManager.LayoutParams layoutParams = this.mCoverLp;
                if (isNavBarMini || this.mNavBarPos != 4) {
                    i = this.mDisplayHeight - this.primaryBounds.top;
                } else {
                    i = (this.mDisplayHeight - this.primaryBounds.top) - sNavBarHeight;
                }
                layoutParams.height = i;
                WindowManager.LayoutParams layoutParams2 = this.mCoverLp;
                layoutParams2.width = this.mWindowWH;
                layoutParams2.y = 0;
                layoutParams2.x = this.mBottom - this.mWindowOffset;
                return;
            }
            WindowManager.LayoutParams layoutParams3 = this.mCoverLp;
            layoutParams3.height = this.mWindowWH;
            layoutParams3.width = this.mDisplayWidth - this.primaryBounds.left;
            WindowManager.LayoutParams layoutParams4 = this.mCoverLp;
            layoutParams4.y = this.mBottom - this.mWindowOffset;
            layoutParams4.x = 0;
            return;
        }
        Slog.i(TAG, "updateViewPos fail, will remove split bar.");
        removeSplit(this.mWindowMode, true);
    }

    private void loadDimens() {
        loadDimensFormRes();
        this.mDragFullZonePadding = dip2px(16.0f);
        this.mDragFullZoneSlop = dip2px(60.0f);
        this.mBlackCoverInExitSplit = new View(this.mContext);
        this.mBlackCoverInExitSplit.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
        this.mBlackCoverInExitSplit.setBackgroundColor(Color.parseColor(HwSplitBarConstants.EXIT_TO_FULL_BLACK_VIEW_COLOR));
        Bundle bundle = this.mHwMultiWindowManager.getSplitGearsByDisplayId(this.mDisplayId);
        if (bundle != null) {
            this.mWidthColumns = bundle.getInt(HwMultiWindowManager.WIDTH_COLUMNS);
            this.mHeightColumns = bundle.getInt(HwMultiWindowManager.HEIGHT_COLUMNS);
            Slog.i(TAG, "width columns = " + this.mWidthColumns + " height columns = " + this.mHeightColumns);
            if (bundle.getInt(HwMultiWindowManager.HW_SPLIT_SCREEN_PRIMARY_POSITION) == 1) {
                Slog.i(TAG, "left and right split screen");
                this.mIsLeftAndRightPos = true;
                this.mCoverLayout = (FrameLayout) LayoutInflater.from(this.mContext).inflate(34013362, (ViewGroup) null);
            } else {
                Slog.i(TAG, "up and down split screen");
                this.mIsLeftAndRightPos = false;
                this.mCoverLayout = (FrameLayout) LayoutInflater.from(this.mContext).inflate(34013363, (ViewGroup) null);
            }
            this.mLeftCover = (RelativeLayout) this.mCoverLayout.findViewById(34603279);
            this.mRightCover = (RelativeLayout) this.mCoverLayout.findViewById(34603297);
            this.mLeftCover.setVisibility(8);
            this.mRightCover.setVisibility(8);
            float[] splitRatios = bundle.getFloatArray(HwMultiWindowManager.HW_SPLIT_SCREEN_RATIO_VALUES);
            if (splitRatios != null && splitRatios.length != 0) {
                if (splitRatios.length == 1) {
                    this.mIsOneGear = true;
                } else {
                    this.mIsOneGear = false;
                }
            }
        }
    }

    private void loadDimensFormRes() {
        this.mWindowWH = this.mContext.getResources().getDimensionPixelSize(34472584);
        this.mDividerBarHeight = this.mContext.getResources().getDimensionPixelSize(34472582);
        this.mFarLineMargin = this.mContext.getResources().getDimensionPixelSize(34472589);
        this.mNearLineMargin = this.mContext.getResources().getDimensionPixelSize(34472590);
        this.mDragFullZoneWidth = this.mContext.getResources().getDimensionPixelSize(34472586);
        this.mDragFullZoneHeight = this.mContext.getResources().getDimensionPixelSize(34472585);
        this.mDividerBarRadio = this.mContext.getResources().getDimensionPixelSize(34472565);
        this.mHotRegionDefaultWidth = this.mContext.getResources().getDimensionPixelSize(34472566);
        this.mWindowOffset = this.mContext.getResources().getDimensionPixelSize(34472583);
        this.mDragLineColor = this.mContext.getResources().getColor(33882962);
        this.mDragFullLineColor = this.mContext.getResources().getColor(33882961);
        sNavBarWidth = this.mContext.getResources().getDimensionPixelSize(17105312);
        sNavBarHeight = this.mContext.getResources().getDimensionPixelSize(17105307);
        sStatusBarHeight = this.mContext.getResources().getDimensionPixelSize(17105445);
    }

    private Bundle getDisplayWidthAndHeight(int displayId) {
        ActivityDisplay display = this.mActivityTaskManagerService.getRootActivityContainer().getActivityDisplay(displayId);
        Bundle bundle = new Bundle();
        if (display == null) {
            return bundle;
        }
        bundle.putInt(HwSplitBarConstants.DISPLAY_WIDTH, display.mDisplayContent.getDisplayInfo().logicalWidth);
        bundle.putInt(HwSplitBarConstants.DISPLAY_HEIGHT, display.mDisplayContent.getDisplayInfo().logicalHeight);
        bundle.putInt(HwSplitBarConstants.DISPLAY_ROTATION, display.mDisplayContent.getDisplayInfo().rotation);
        return bundle;
    }

    public void onSystemReady(int displayId) {
        Bundle bundle = getDisplayWidthAndHeight(displayId);
        if (bundle == null) {
            Slog.i(TAG, "get display width and height failed in createSplitCover");
            return;
        }
        this.mDisplayWidth = bundle.getInt(HwSplitBarConstants.DISPLAY_WIDTH);
        this.mDisplayHeight = bundle.getInt(HwSplitBarConstants.DISPLAY_HEIGHT);
        this.mRotation = bundle.getInt(HwSplitBarConstants.DISPLAY_ROTATION);
    }

    public void setSplitBarVisibility(boolean isVisibility) {
        View view;
        HwSurfaceInNotch surface;
        this.mHandler.removeMessages(4);
        if (this.mCoverLp == null || (view = this.mDragView) == null) {
            int callingUid = Binder.getCallingUid();
            Slog.i(TAG, "setSplitBarVisibility, callingUid = " + callingUid);
            if (callingUid == 1000) {
                addDividerBarWindow(100);
                return;
            }
            return;
        }
        this.mIsSplitBarVisibleNow = isVisibility;
        if ((!isVisibility || view.getVisibility() != 0) && (isVisibility || this.mDragView.getVisibility() == 0)) {
            int visible = isVisibility ? 0 : 4;
            Slog.i(TAG, "set splitBar visible = " + visible);
            if (visible == 4 && (surface = HwMultiWindowManager.getInstance(this.mActivityTaskManagerService).mSurfaceInNotch) != null) {
                surface.remove();
            }
            ((ActivityTaskManagerService) this.mActivityTaskManagerService).mUiHandler.post(new Runnable(visible, isVisibility) {
                /* class com.android.server.wm.$$Lambda$HwMultiWindowSplitUI$h_3dkpW_dR18ucYsOqoJXuH6HA */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ boolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwMultiWindowSplitUI.this.lambda$setSplitBarVisibility$2$HwMultiWindowSplitUI(this.f$1, this.f$2);
                }
            });
            return;
        }
        Slog.i(TAG, "set splitBar visible return because already visible:" + isVisibility);
    }

    public /* synthetic */ void lambda$setSplitBarVisibility$2$HwMultiWindowSplitUI(int visible, boolean isVisibility) {
        if (this.mCoverLp != null) {
            ImageView imageView = this.mDragRegion;
            if (imageView != null) {
                imageView.setVisibility(visible);
            }
            if (isVisibility && this.mCoverLp.y < 0) {
                setPosition();
                updateCoverLayout();
            }
            if (isVisibility) {
                this.mDragView.setVisibility(visible);
                this.mIsNeedSetSplitBarVisible = false;
                if (isInDragFullMode()) {
                    setDragFullModeViewsVisibility(visible);
                }
            } else if (!isVisibility) {
                this.mDragView.setVisibility(visible);
                setDragFullModeViewsVisibility(visible);
                this.mIsNeedSetSplitBarVisible = true;
            }
        }
    }

    private void setDragFullModeViewsVisibility(int visible) {
        this.mDragFullZone.setVisibility(visible);
        this.mNearLine.setVisibility(visible);
    }

    private View getBoarderRect() {
        float f;
        View rectFrameLayout = LayoutInflater.from(this.mContext).inflate(34013360, (ViewGroup) null);
        View rectFrameView = rectFrameLayout.findViewById(34603287);
        if (rectFrameView == null) {
            Slog.e(TAG, "updateView: rect frame view null.");
            return rectFrameLayout;
        }
        Drawable bg = rectFrameView.getBackground();
        if (bg == null) {
            bg = new GradientDrawable();
            rectFrameView.setBackground(bg);
        }
        if (!(bg instanceof GradientDrawable)) {
            Slog.e(TAG, "updateView: type error.");
            return rectFrameLayout;
        }
        ((GradientDrawable) bg).setStroke(dip2px(1.0f), Color.parseColor(HwSplitBarConstants.RECT_FRAME_BORDER_COLOR));
        GradientDrawable gradientDrawable = (GradientDrawable) bg;
        if (HwActivityManager.IS_PHONE) {
            f = 0.0f;
        } else {
            f = this.mContext.getResources().getDimension(34472572);
        }
        gradientDrawable.setCornerRadius(f);
        rectFrameView.setVisibility(0);
        return rectFrameLayout;
    }

    private int dip2px(float dpValue) {
        return (int) ((dpValue * this.mContext.getResources().getDisplayMetrics().density) + 0.5f);
    }

    private float getRegionAndScale(float rawX, float rawY) {
        float largePos;
        float len = (float) (this.mIsLeftAndRightPos ? this.mDisplayWidth : this.mDisplayHeight);
        float columns = (float) (this.mIsLeftAndRightPos ? this.mWidthColumns : this.mHeightColumns);
        float curPos = this.mIsLeftAndRightPos ? rawX : rawY;
        int offSet = this.mIsLeftAndRightPos ? this.primaryBounds.left : this.primaryBounds.top;
        int rightOffSet = (this.mIsLeftAndRightPos ? getNavBarRight() : getNavBarBottomUpDown()) + getNotchSizeOnRight();
        boolean isMagicCan2Full = isCanDragToFullscreen();
        int fullModeGear = this.mIsOneGear ? 1 : 2;
        float f = len / columns;
        if (isMagicCan2Full) {
            f *= (float) fullModeGear;
        }
        float lessPos = f + ((float) offSet);
        if (isMagicCan2Full) {
            largePos = (((columns - ((float) fullModeGear)) * len) / columns) - ((float) rightOffSet);
        } else {
            largePos = (((columns - 1.0f) * len) / columns) - ((float) rightOffSet);
        }
        float stopScale = (float) offSet;
        float threshold = curPos;
        float translateValue = 0.0f;
        if (curPos < lessPos) {
            if (!this.mIsReadyToFull) {
                setThresholdTip(this.mLeftCover, this.mBlackCoverInExitSplit);
            }
            this.mIsReadyToFull = true;
            threshold = lessPos;
            if (curPos <= threshold) {
                translateValue = curPos - threshold;
            }
            if (this.mIsLeftAndRightPos) {
                this.mExitRegion = 1;
                this.mLeftImg.setTranslationX(translateValue / 2.0f);
            } else {
                this.mExitRegion = 3;
                this.mLeftImg.setTranslationY(translateValue / 2.0f);
            }
        } else if (curPos > largePos) {
            if (!this.mIsReadyToFull) {
                setThresholdTip(this.mRightCover, this.mBlackCoverInExitSplit);
            }
            this.mIsReadyToFull = true;
            threshold = largePos;
            stopScale = len - ((float) rightOffSet);
            if (curPos >= threshold) {
                translateValue = curPos - threshold;
            }
            if (this.mIsLeftAndRightPos) {
                this.mExitRegion = 2;
                this.mRightImg.setTranslationX(translateValue / 2.0f);
            } else {
                this.mExitRegion = 4;
                this.mRightImg.setTranslationY(translateValue / 2.0f);
            }
        } else {
            this.mIsReadyToFull = false;
        }
        return 1.0f - (((threshold - curPos) * 0.05f) / (threshold - stopScale));
    }

    private void setThresholdTip(RelativeLayout layout, View view) {
        if (view.getParent() != null || layout == null) {
            Slog.i(TAG, "black view has parent already");
            return;
        }
        layout.addView(view);
        vibrateTip();
    }

    private void vibrateTip() {
        if (this.mVibrator != null) {
            this.mVibrator.vibrate(VibrationEffect.createOneShot(100, -1));
        }
    }

    private void setScale(float scale, int zoneRegion) {
        if (zoneRegion == 1 || zoneRegion == 3) {
            this.mLeftCover.setScaleX(scale);
            this.mLeftCover.setScaleY(scale);
        } else if (zoneRegion == 2 || zoneRegion == 4) {
            this.mRightCover.setScaleX(scale);
            this.mRightCover.setScaleY(scale);
        }
    }

    private void dragToFullScreen(float rawX, float rawY) {
        if (this.mWindowMode != 103 || isCanDragToFullscreen()) {
            float scale = getRegionAndScale(rawX, rawY);
            if (scale == 1.0f) {
                if (!this.mIsReadyToFull && this.mIsDownAnimate) {
                    this.mIsDownAnimate = false;
                    int i = this.mExitRegion;
                    if (i == 1 || i == 3) {
                        this.mLeftCover.removeView(this.mBlackCoverInExitSplit);
                    } else if (i == 2 || i == 4) {
                        this.mRightCover.removeView(this.mBlackCoverInExitSplit);
                    } else {
                        return;
                    }
                    setScale(1.0f, this.mExitRegion);
                    this.mExitRegion = 0;
                } else {
                    return;
                }
            }
            if (this.mIsReadyToFull) {
                setScale(scale, this.mExitRegion);
                this.mIsDownAnimate = true;
            }
        }
    }

    private void setDateBundleInActionDown() {
        this.mAnimBundle.putInt(HwSplitBarConstants.DISPLAY_WIDTH, this.mDisplayWidth);
        this.mAnimBundle.putInt(HwSplitBarConstants.DISPLAY_HEIGHT, this.mDisplayHeight);
        this.mAnimBundle.putInt(HwMultiWindowManager.WIDTH_COLUMNS, this.mWidthColumns);
        this.mAnimBundle.putInt(HwMultiWindowManager.HEIGHT_COLUMNS, this.mHeightColumns);
        this.mAnimBundle.putBoolean(HwSplitBarConstants.SPLIT_ORIENTATION, this.mIsLeftAndRightPos);
        this.mAnimBundle.putInt(HwSplitBarConstants.SPLIT_BAR_DISPLAY_ID, this.mDisplayId);
    }

    private void setDataBundleInActionUp(float curPosition, float endPosition) {
        this.mAnimBundle.putInt(HwSplitBarConstants.EXIT_REGION, this.mExitRegion);
        this.mAnimBundle.putFloat(HwSplitBarConstants.CURRENT_POSITION, curPosition);
        this.mAnimBundle.putFloat(HwSplitBarConstants.END_POSITION, endPosition);
    }

    private void setEnterDragFullAnimData(int type) {
        this.mAnimBundle.putInt(HwSplitBarConstants.SPLIT_RATIO, type);
        this.mAnimBundle.putInt(HwSplitBarConstants.FAR_LINE_MARGIN, this.mFarLineMargin);
        this.mAnimBundle.putInt(HwSplitBarConstants.NEAR_LINE_MARGIN, this.mNearLineMargin);
        this.mAnimBundle.putInt(HwSplitBarConstants.FAR_LINE_TRANS, this.mFarLineMargin + (this.mDividerBarHeight / 2) + (this.mFarLine.getWidth() / 2));
        this.mAnimBundle.putInt(HwSplitBarConstants.DRAG_FULL_MODE_LINE_COLOR, this.mDragFullLineColor);
        this.mAnimBundle.putInt(HwSplitBarConstants.DRAG_LINE_COLOR, this.mDragLineColor);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSplitBarVisibleIfNeeded() {
        Slog.i(TAG, "setSplitBarVisibleIfNeeded");
        if (!this.mIsNeedSetSplitBarVisible || this.mCoverLp == null) {
            Slog.i(TAG, "setSplitBarVisibleIfNeeded  return");
        } else if (isInDragFullMode()) {
            setSplitBarDragFullMode(this.mHwMultiWindowManager.getTaskDragFullMode(this.mDisplayId));
            this.mIsNeedSetSplitBarVisible = true;
        } else if (this.mDragView.getVisibility() != 0) {
            this.mDragView.setVisibility(0);
            this.mIsNeedSetSplitBarVisible = false;
        }
    }

    public void updateSplitBarPosForIm(int position) {
        if (this.mIsSplitBarVisibleNow) {
            this.mHandler.removeMessages(4);
            Message msg = this.mHandler.obtainMessage(5);
            msg.arg1 = position;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSplitBarPos(int position) {
        if (this.mCoverLp != null && this.mDragView != null && this.mDragRegion != null && this.mCoverLayout != null && this.mIsSplitBarVisibleNow) {
            int visible = position > 0 ? 0 : 4;
            if (visible == 0) {
                Slog.i(TAG, "updateSplitBarPos, position =" + position + " visible =" + visible);
            }
            removeCovers();
            this.mDragRegion.setVisibility(visible);
            this.mDragView.setVisibility(visible);
            if (position < 0) {
                this.mCoverLp.y = INVALID_POSITION;
                updateCoverLayout();
            } else if (this.mCoverLp.y < 0) {
                setPosition();
                updateCoverLayout();
                if (isInDragFullMode()) {
                    setSplitBarDragFullMode(this.mHwMultiWindowManager.getTaskDragFullMode(this.mDisplayId));
                } else {
                    resetSplitBarFromDragFullMode();
                }
            }
        }
    }

    private void updateCoverLayout() {
        FrameLayout frameLayout;
        WindowManager.LayoutParams layoutParams = this.mCoverLp;
        if (layoutParams != null && (frameLayout = this.mCoverLayout) != null) {
            try {
                this.mWm.updateViewLayout(frameLayout, layoutParams);
            } catch (WindowManager.InvalidDisplayException e) {
                Slog.e(TAG, "update view layout error.");
            }
        }
    }

    private void setSplitBarDarkDelay() {
        this.mHandler.removeMessages(8);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(8), 5000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSplitBarDarkMode() {
        HwDragFullAnimStrategy strategy = HwDragFullAnimStrategy.getStrategy(this.mActivityTaskManagerService, this.mDragFullZone, this.mFarLine, this.mNearLine, this.mDragLine, this.mAnimBundle);
        if (strategy != null) {
            strategy.splitBarDarkModeAnim();
        }
    }

    public void resetSplitBarFromDragFullMode() {
        if (this.mDragFullZone != null && this.mIsLeftAndRightPos && this.mCoverLp != null) {
            Slog.i(TAG, "reset drag full mode settings");
            this.mHandler.removeMessages(8);
            setDragFullModeViewsVisibility(8);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this.mDragRegion.getLayoutParams();
            lp.width = this.mHotRegionDefaultWidth;
            this.mDragRegion.setLayoutParams(lp);
        }
    }

    public void setSplitBarDragFullMode(int type) {
        if (this.mCoverLp != null && this.mIsLeftAndRightPos) {
            if (this.mDragView.getVisibility() == 8) {
                Slog.i(TAG, "can't enter drag full mode");
                removeSplitBarWindow();
                return;
            }
            Slog.i(TAG, "enter drag full mode");
            removeCovers();
            this.mDragView.setVisibility(4);
            boolean isLeft = false;
            setDragFullModeViewsVisibility(0);
            if (type == 6) {
                isLeft = true;
            }
            setDragFullModeLayout(isLeft);
            setEnterDragFullAnimData(type);
            runEnterDragFullAnim(isLeft);
            setSplitBarDarkDelay();
        }
    }

    private void setDragFullModeLayout(boolean isLeft) {
        int i;
        setDragFullCoverLayout(isLeft);
        RelativeLayout relativeLayout = this.mDragFullZone;
        if (isLeft) {
            i = 33751994;
        } else {
            i = 33751995;
        }
        relativeLayout.setBackgroundResource(i);
        RelativeLayout.LayoutParams zoneLp = (RelativeLayout.LayoutParams) this.mDragFullZone.getLayoutParams();
        zoneLp.addRule(1, isLeft ? this.mDragView.getId() : 0);
        zoneLp.addRule(0, isLeft ? 0 : this.mDragView.getId());
        this.mDragFullZone.setLayoutParams(zoneLp);
        this.mDragFullZone.setAlpha(0.7f);
        setLinesParamsInDragFull(isLeft);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this.mDragRegion.getLayoutParams();
        lp.width = this.mCoverLp.width;
        this.mDragRegion.setLayoutParams(lp);
    }

    private void setDragFullCoverLayout(boolean isLeft) {
        this.mCoverLp.x = (this.mBottom - (isLeft ? this.mDragFullZoneWidth + this.mDividerBarHeight : this.mDragFullZoneWidth)) - this.mDragFullZonePadding;
        boolean isLastY = this.mDragFullZoneLastY != -1;
        this.mCoverLp.y = isLastY ? this.mDragFullZoneLastY : ((this.mDisplayHeight - this.mDragFullZoneHeight) / 2) - this.mDragFullZonePadding;
        WindowManager.LayoutParams layoutParams = this.mCoverLp;
        int i = this.mDragFullZoneHeight;
        int i2 = this.mDragFullZonePadding;
        layoutParams.height = i + (i2 * 2);
        layoutParams.width = (this.mDragFullZoneWidth * 2) + this.mDividerBarHeight + (i2 * 2);
        updateCoverLayout();
        this.mCoverLayout.setBackgroundColor(0);
    }

    public void setLinesParamsInDragFull(boolean isLeft) {
        RelativeLayout.LayoutParams farLineLp = (RelativeLayout.LayoutParams) this.mFarLine.getLayoutParams();
        RelativeLayout.LayoutParams nearLineLp = (RelativeLayout.LayoutParams) this.mNearLine.getLayoutParams();
        if (isLeft) {
            farLineLp.addRule(9);
            farLineLp.removeRule(11);
            nearLineLp.addRule(9);
            nearLineLp.removeRule(11);
            farLineLp.setMargins(this.mFarLineMargin, 0, 0, 0);
            nearLineLp.setMargins(this.mNearLineMargin, 0, 0, 0);
        } else {
            farLineLp.addRule(11);
            farLineLp.removeRule(9);
            nearLineLp.addRule(11);
            nearLineLp.removeRule(9);
            farLineLp.setMargins(0, 0, this.mFarLineMargin, 0);
            nearLineLp.setMargins(0, 0, this.mNearLineMargin, 0);
        }
        this.mFarLine.setLayoutParams(farLineLp);
        this.mNearLine.setLayoutParams(nearLineLp);
        this.mFarLine.setVisibility(0);
        this.mNearLine.setVisibility(0);
        this.mFarLine.setBackgroundColor(this.mDragFullLineColor);
    }

    private void runEnterDragFullAnim(boolean isLeft) {
        int splitLineTranslate = this.mFarLineMargin - this.mNearLineMargin;
        this.mAnimBundle.putInt(HwSplitBarConstants.NEAR_LINE_TRANS, isLeft ? -splitLineTranslate : splitLineTranslate);
        HwDragFullAnimStrategy strategy = HwDragFullAnimStrategy.getStrategy(this.mActivityTaskManagerService, this.mDragFullZone, this.mFarLine, this.mNearLine, this.mDragLine, this.mAnimBundle);
        if (strategy != null) {
            strategy.split2DragFullAnim();
        }
    }

    private void runExitDragFullAnim(int type) {
        float dragBarEndPos;
        Slog.i(TAG, "exit drag full mode");
        this.mLeftCover.setPivotX(0.0f);
        this.mHandler.removeMessages(8);
        if (type == 6) {
            dragBarEndPos = (float) (this.mFarLineMargin - ((this.mDividerBarHeight - this.mDragView.getWidth()) / 2));
        } else {
            dragBarEndPos = (float) ((this.mDisplayWidth - this.mFarLineMargin) - ((this.mDividerBarHeight + this.mDragView.getWidth()) / 2));
        }
        ValueAnimator dragAnimator = ValueAnimator.ofFloat((float) this.mBottom, dragBarEndPos);
        dragAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.wm.HwMultiWindowSplitUI.AnonymousClass4 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!HwMultiWindowSplitUI.this.mIsSetBackground) {
                    HwMultiWindowSplitUI.this.mCoverLayout.setBackgroundColor(-16777216);
                    HwMultiWindowSplitUI.this.mIsSetBackground = true;
                }
                HwMultiWindowSplitUI.this.upadateCoverSize(((Float) animation.getAnimatedValue()).floatValue(), 0.0f);
            }
        });
        dragAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.android.server.wm.HwMultiWindowSplitUI.AnonymousClass5 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                HwMultiWindowSplitUI.this.mIsDragFullAnimRunning = false;
            }
        });
        HwDragFullAnimStrategy strategy = HwDragFullAnimStrategy.getStrategy(this.mActivityTaskManagerService, this.mDragFullZone, this.mFarLine, this.mNearLine, this.mDragLine, this.mAnimBundle);
        if (strategy != null) {
            strategy.dragFullMode2SplitAnim(dragAnimator);
        }
    }

    /* access modifiers changed from: private */
    public class DragLineOutlineProvider extends ViewOutlineProvider {
        private DragLineOutlineProvider() {
        }

        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) HwMultiWindowSplitUI.this.mDividerBarRadio);
        }
    }

    public static int getNavBarWidth() {
        return sNavBarWidth;
    }

    public static int getNavBarHeight() {
        return sNavBarHeight;
    }

    public static void removeSplitUIVirtualDisplay(int displayId) {
        mSplitUIFactory.remove(Integer.valueOf(displayId));
    }

    private boolean isRealTablet(int displayId) {
        return HwMultiWindowManager.isRealTablet(displayId);
    }

    private int getThresholdDistance(int displayId) {
        ActivityDisplay activityDisplay = this.mActivityTaskManagerService.getRootActivityContainer().getActivityDisplay(displayId);
        if (activityDisplay == null) {
            return 0;
        }
        Rect defalutLeftBounds = new Rect();
        Rect praimaryleftBounds = new Rect();
        HwMultiWindowManager.calcHwSplitStackBounds(activityDisplay, 0, defalutLeftBounds, (Rect) null);
        HwMultiWindowManager.calcHwSplitStackBounds(activityDisplay, 1, praimaryleftBounds, (Rect) null);
        return defalutLeftBounds.right - praimaryleftBounds.right;
    }
}
