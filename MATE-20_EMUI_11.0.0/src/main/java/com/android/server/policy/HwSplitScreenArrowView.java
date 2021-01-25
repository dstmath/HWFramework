package com.android.server.policy;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.freeform.HwFreeFormUtils;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.Log;
import android.view.IDockedStackListener;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.android.server.LocalServices;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.huawei.android.app.HwActivityTaskManager;
import java.util.List;

public class HwSplitScreenArrowView extends LinearLayout {
    private static final int DISTANCE_THRESHOLD_DIVIDE = 8;
    private static final int DURATION = 400;
    private static final int DURATION_MIN = 200;
    private static final int SCREEN_LARGE_SHORT_SIDE = 600;
    private static final String TAG = "HwSplitScreenArrowView";
    private boolean mAdded;
    ActivityManager mAm = null;
    private float mAnimStartPos;
    private Context mContext;
    private int mDistanceThreshold;
    private boolean mDockedStackExists;
    private float[] mDownPoint = new float[2];
    IActivityManager mIam = null;
    private boolean mIsDisabled;
    private boolean mIsMoving;
    private boolean mIsScreenLarge;
    private boolean mIsshownToast;
    private boolean mLaunchSplitScreen;
    private boolean mMoreThanThreeFinger;
    private int mNavBarHeight;
    private int mOrientation;
    private WindowManager.LayoutParams mParams;
    private Point mScreenDims = new Point();
    private WindowManager mWindowMgr;

    public HwSplitScreenArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mAm = (ActivityManager) context.getSystemService("activity");
        this.mIam = ActivityManagerNative.getDefault();
        this.mWindowMgr = (WindowManager) this.mContext.getSystemService("window");
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        updateDockedStackFlag();
    }

    public void initViewParams(int orientation, Point screenDims) {
        Resources res = this.mContext.getResources();
        this.mOrientation = orientation;
        this.mNavBarHeight = res.getDimensionPixelSize(17105307);
        this.mScreenDims = screenDims;
        this.mParams = new WindowManager.LayoutParams(-1, -1, 2014, 16777736, -3);
        if (this.mOrientation == 2) {
            this.mParams.gravity = 48;
        } else {
            this.mParams.gravity = 1;
        }
        this.mParams.width = this.mScreenDims.x;
        this.mParams.height = this.mScreenDims.y;
        this.mIsScreenLarge = isScreenLarge();
        this.mDistanceThreshold = this.mScreenDims.y / 8;
    }

    private void updateDockedStackFlag() {
        try {
            WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(new IDockedStackListener.Stub() {
                /* class com.android.server.policy.HwSplitScreenArrowView.AnonymousClass1 */

                public void onDividerVisibilityChanged(boolean visible) throws RemoteException {
                }

                public void onDockedStackExistsChanged(boolean exists) throws RemoteException {
                    HwSplitScreenArrowView.this.mDockedStackExists = exists;
                }

                public void onDockedStackMinimizedChanged(boolean minimized, long animDuration, boolean isHomeStackResizable) throws RemoteException {
                }

                public void onAdjustedForImeChanged(boolean adjustedForIme, long animDuration) throws RemoteException {
                }

                public void onDockSideChanged(int newDockSide) throws RemoteException {
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Failed registering docked stack exists listener", e);
        }
    }

    private boolean isScreenLarge() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mWindowMgr.getDefaultDisplay().getMetrics(displayMetrics);
        return ((float) (this.mScreenDims.x < this.mScreenDims.y ? this.mScreenDims.x : this.mScreenDims.y)) / displayMetrics.density >= 600.0f;
    }

    public void addViewToWindow() {
        try {
            if (!this.mAdded) {
                if (this.mOrientation == 2) {
                    this.mParams.x = this.mParams.width;
                } else {
                    this.mParams.y = this.mParams.height;
                }
                this.mWindowMgr.addView(this, this.mParams);
                this.mAdded = true;
            }
        } catch (RuntimeException e) {
        }
    }

    public void removeViewToWindow() {
        try {
            if (this.mAdded) {
                this.mWindowMgr.removeView(this);
                this.mAdded = false;
            }
        } catch (RuntimeException e) {
        }
    }

    private void moveToPositionY(float aPosY) {
        int i = this.mNavBarHeight;
        if (aPosY > ((float) (i * 4))) {
            this.mParams.y = ((int) aPosY) - (i * 3);
            runOnUiThread(new Runnable() {
                /* class com.android.server.policy.HwSplitScreenArrowView.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    WindowManager windowManager = HwSplitScreenArrowView.this.mWindowMgr;
                    HwSplitScreenArrowView hwSplitScreenArrowView = HwSplitScreenArrowView.this;
                    windowManager.updateViewLayout(hwSplitScreenArrowView, hwSplitScreenArrowView.mParams);
                }
            });
        }
    }

    private void moveToPositionX(float aPosX) {
        int i = this.mNavBarHeight;
        if (aPosX * 2.0f > ((float) (i * 3))) {
            this.mParams.x = (int) ((2.0f * aPosX) - ((float) (i * 3)));
            runOnUiThread(new Runnable() {
                /* class com.android.server.policy.HwSplitScreenArrowView.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    WindowManager windowManager = HwSplitScreenArrowView.this.mWindowMgr;
                    HwSplitScreenArrowView hwSplitScreenArrowView = HwSplitScreenArrowView.this;
                    windowManager.updateViewLayout(hwSplitScreenArrowView, hwSplitScreenArrowView.mParams);
                }
            });
        }
    }

    public void handleSplitScreenGesture(MotionEvent event) {
        if (event != null) {
            boolean z = false;
            if (event.getAction() == 0) {
                Log.d("fingerTest", "MotionEvent.ACTION_DOWN");
                this.mDownPoint[0] = event.getX();
                this.mDownPoint[1] = event.getY();
            }
            if (event.getActionMasked() == 5) {
                Log.d("fingerTest", "MotionEvent.ACTION_POINTER_DOWN");
                this.mDownPoint[0] = event.getX();
                this.mDownPoint[1] = event.getY();
                if (event.getPointerCount() > 3) {
                    this.mMoreThanThreeFinger = true;
                }
            }
            if (event.getAction() == 2 && event.getPointerCount() == 3 && !this.mMoreThanThreeFinger) {
                if (!this.mIsMoving) {
                    if (this.mDockedStackExists || isTopTaskHome()) {
                        z = true;
                    }
                    this.mIsDisabled = z;
                    this.mIsMoving = true;
                }
                processFingerMoveEvent(event);
            } else if (event.getActionMasked() == 6 || event.getActionMasked() == 1) {
                Log.d("fingerTest", "MotionEvent.ACTION_UP");
                enterSplitScreenMode();
            } else if (event.getAction() == 3) {
                reset();
            }
        }
    }

    private void processFingerMoveEvent(MotionEvent event) {
        if (this.mIsDisabled) {
            Log.d("fingerTest", "no response for finger move event");
            return;
        }
        float lDistance = this.mDownPoint[1] - event.getY();
        if (lDistance < ((float) this.mNavBarHeight)) {
            Log.d("fingerTest", "return since swipe distance too closely");
        } else if (isTopTaskSupportMultiWindow() || HwFreeFormUtils.isFreeFormEnable()) {
            if (lDistance > ((float) this.mDistanceThreshold)) {
                this.mLaunchSplitScreen = true;
            } else {
                this.mLaunchSplitScreen = false;
            }
            if (this.mIsScreenLarge || this.mOrientation != 2) {
                this.mAnimStartPos = event.getRawY();
                moveToPositionY(this.mAnimStartPos);
                return;
            }
            this.mAnimStartPos = event.getRawY();
            moveToPositionX(this.mAnimStartPos);
        } else {
            showToast();
        }
    }

    private void reset() {
        this.mLaunchSplitScreen = false;
        this.mIsshownToast = false;
        this.mIsMoving = false;
        this.mMoreThanThreeFinger = false;
    }

    public void enterSplitScreenMode() {
        if (getVisibility() == 0) {
            animateView(this.mAnimStartPos, this.mLaunchSplitScreen);
        }
        if (this.mLaunchSplitScreen) {
            toggleSplitScreen();
        }
        reset();
    }

    private void showToast() {
        if (!isMultiWindowDisabled() && !this.mIsshownToast) {
            showToastForAllUser(this.mContext, 33685924);
            this.mIsshownToast = true;
        }
    }

    private void animateView(float aPos, boolean islaunchingMultiWindow) {
        if (aPos > 0.0f) {
            float aMin = (float) (this.mNavBarHeight * 4);
            float aMax = (float) (this.mOrientation == 2 ? this.mScreenDims.x : this.mScreenDims.y);
            int dividerHeight = this.mContext.getResources().getDimensionPixelSize(34472140);
            if (islaunchingMultiWindow) {
                if (this.mOrientation == 2) {
                    aMax = (((float) (this.mScreenDims.x - dividerHeight)) / 2.0f) + ((float) this.mNavBarHeight);
                } else {
                    aMax = (((float) (this.mScreenDims.y - dividerHeight)) / 2.0f) + ((float) this.mNavBarHeight);
                }
            }
            float aPos2 = aPos > aMin ? aPos : aMin;
            ValueAnimator animation = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("value", aPos2, aMax));
            int duration = (int) (400.0f * (Math.abs(aMax - aPos2) / (aMax / 2.0f)));
            int duration2 = 200;
            if (duration > 200) {
                duration2 = duration;
            }
            animation.setDuration((long) duration2);
            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.server.policy.HwSplitScreenArrowView.AnonymousClass4 */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    Object object = animation.getAnimatedValue("value");
                    if (object != null) {
                        float lValue = ((Float) object).floatValue();
                        if (HwSplitScreenArrowView.this.mOrientation == 2) {
                            HwSplitScreenArrowView.this.mParams.x = ((int) lValue) - (HwSplitScreenArrowView.this.mNavBarHeight * 3);
                        } else {
                            HwSplitScreenArrowView.this.mParams.y = ((int) lValue) - (HwSplitScreenArrowView.this.mNavBarHeight * 3);
                        }
                        if (HwSplitScreenArrowView.this.mWindowMgr != null) {
                            try {
                                HwSplitScreenArrowView.this.runOnUiThread(new Runnable() {
                                    /* class com.android.server.policy.HwSplitScreenArrowView.AnonymousClass4.AnonymousClass1 */

                                    @Override // java.lang.Runnable
                                    public void run() {
                                        HwSplitScreenArrowView.this.mWindowMgr.updateViewLayout(HwSplitScreenArrowView.this, HwSplitScreenArrowView.this.mParams);
                                    }
                                });
                            } catch (IllegalArgumentException exp) {
                                Log.e(HwSplitScreenArrowView.TAG, exp.getMessage());
                            }
                        }
                    }
                }
            });
            animation.addListener(new Animator.AnimatorListener() {
                /* class com.android.server.policy.HwSplitScreenArrowView.AnonymousClass5 */

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animation) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animation) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animation) {
                }
            });
            animation.start();
        }
    }

    private void toggleSplitScreen() {
        Flog.bdReport(this.mContext, 12);
        Log.d("fingerTest", "ready to toggle split screen");
        ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).toggleSplitScreen();
    }

    private boolean isTopTaskHome() {
        List<ActivityManager.RunningTaskInfo> tasks;
        ActivityManager activityManager = this.mAm;
        if (activityManager == null || (tasks = activityManager.getRunningTasks(1)) == null || tasks.isEmpty()) {
            return false;
        }
        return isInHomeStack(tasks.get(0));
    }

    public boolean isTopTaskSupportMultiWindow() {
        ActivityManager.RunningTaskInfo topTask = getTopMostTask();
        if (topTask == null || isInHomeStack(topTask) || isScreenPinningActive() || !topTask.supportsSplitScreenMultiWindow) {
            return false;
        }
        return true;
    }

    private boolean isScreenPinningActive() {
        IActivityManager iActivityManager = this.mIam;
        if (iActivityManager == null) {
            return false;
        }
        try {
            return iActivityManager.isInLockTaskMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    private List<ActivityManager.RunningTaskInfo> getRunningTasks(int numTasks) {
        ActivityManager activityManager = this.mAm;
        if (activityManager == null) {
            return null;
        }
        return activityManager.getRunningTasks(numTasks);
    }

    private ActivityManager.RunningTaskInfo getTopMostTask() {
        List<ActivityManager.RunningTaskInfo> tasks = getRunningTasks(1);
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }
        return tasks.get(0);
    }

    private boolean isInHomeStack(ActivityManager.RunningTaskInfo runningTask) {
        if (runningTask != null && runningTask.configuration.windowConfiguration.getActivityType() == 2) {
            return true;
        }
        return false;
    }

    private void showToastForAllUser(final Context mContext2, final int message) {
        if (mContext2 != null) {
            runOnUiThread(new Runnable() {
                /* class com.android.server.policy.HwSplitScreenArrowView.AnonymousClass6 */

                @Override // java.lang.Runnable
                public void run() {
                    Toast toast = Toast.makeText(mContext2, message, 0);
                    toast.getWindowParams().privateFlags |= 16;
                    toast.show();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void runOnUiThread(Runnable runnable) {
        Handler handler = getHandler();
        if (handler == null || handler.getLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    private boolean isMultiWindowDisabled() {
        return HwActivityTaskManager.getMultiWindowDisabled();
    }
}
