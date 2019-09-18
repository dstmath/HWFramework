package com.android.server.policy;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.Log;
import android.util.SplitNotificationUtils;
import android.view.IDockedStackListener;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.android.server.LocalServices;
import com.android.server.gesture.GestureNavConst;
import com.android.server.statusbar.StatusBarManagerInternal;
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
    /* access modifiers changed from: private */
    public boolean mDockedStackExists;
    private float[] mDownPoint = new float[2];
    IActivityManager mIam = null;
    private boolean mIsDisabled;
    private boolean mIsMoving;
    private boolean mIsScreenLarge;
    private boolean mIsshownToast;
    private boolean mLaunchSplitScreen;
    private boolean mMoreThanThreeFinger;
    /* access modifiers changed from: private */
    public int mNavBarHeight;
    /* access modifiers changed from: private */
    public int mOrientation;
    /* access modifiers changed from: private */
    public WindowManager.LayoutParams mParams;
    private Point mScreenDims = new Point();
    /* access modifiers changed from: private */
    public WindowManager mWindowMgr;

    public HwSplitScreenArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mAm = (ActivityManager) context.getSystemService("activity");
        this.mIam = ActivityManagerNative.getDefault();
        this.mWindowMgr = (WindowManager) this.mContext.getSystemService("window");
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        updateDockedStackFlag();
    }

    public void initViewParams(int orientation, Point screenDims) {
        Resources res = this.mContext.getResources();
        this.mOrientation = orientation;
        this.mNavBarHeight = res.getDimensionPixelSize(17105186);
        this.mScreenDims = screenDims;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2014, 16777736, -3);
        this.mParams = layoutParams;
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
                public void onDividerVisibilityChanged(boolean visible) throws RemoteException {
                }

                public void onDockedStackExistsChanged(boolean exists) throws RemoteException {
                    boolean unused = HwSplitScreenArrowView.this.mDockedStackExists = exists;
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
        if (aPosY > ((float) (4 * this.mNavBarHeight))) {
            this.mParams.y = ((int) aPosY) - (3 * this.mNavBarHeight);
            runOnUiThread(new Runnable() {
                public void run() {
                    HwSplitScreenArrowView.this.mWindowMgr.updateViewLayout(HwSplitScreenArrowView.this, HwSplitScreenArrowView.this.mParams);
                }
            });
        }
    }

    private void moveToPositionX(float aPosX) {
        if (2.0f * aPosX > ((float) (this.mNavBarHeight * 3))) {
            this.mParams.x = (int) ((2.0f * aPosX) - ((float) (3 * this.mNavBarHeight)));
            runOnUiThread(new Runnable() {
                public void run() {
                    HwSplitScreenArrowView.this.mWindowMgr.updateViewLayout(HwSplitScreenArrowView.this, HwSplitScreenArrowView.this.mParams);
                }
            });
        }
    }

    public void handleSplitScreenGesture(MotionEvent event) {
        if (event != null) {
            if (event.getPointerCount() > 0) {
                Log.d("fingerTest", "event.x=:" + event.getX() + " event.y=:" + event.getY());
            }
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
                    if (isTopTaskHome()) {
                        showToastForAllUser(this.mContext, 33685923);
                    }
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
        Log.d("fingerTest", "lDistance = :" + lDistance);
        if (lDistance < ((float) this.mNavBarHeight)) {
            Log.d("fingerTest", "return since swipe distance too closely");
        } else if (!isTopTaskSupportMultiWindow()) {
            showToast();
        } else {
            if (lDistance > ((float) this.mDistanceThreshold)) {
                this.mLaunchSplitScreen = true;
            } else {
                this.mLaunchSplitScreen = false;
            }
            if (this.mIsScreenLarge || this.mOrientation != 2) {
                this.mAnimStartPos = event.getRawY();
                moveToPositionY(this.mAnimStartPos);
            } else {
                this.mAnimStartPos = event.getRawY();
                moveToPositionX(this.mAnimStartPos);
            }
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
        if (this.mLaunchSplitScreen && !shouldTriggerFreeForm()) {
            toggleSplitScreen();
        }
        reset();
    }

    private void showToast() {
        if (!this.mIsshownToast) {
            showToastForAllUser(this.mContext, 33685924);
            this.mIsshownToast = true;
        }
    }

    private void animateView(float aPos, boolean islaunchingMultiWindow) {
        if (aPos > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            float aMin = (float) (4 * this.mNavBarHeight);
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
            ValueAnimator animation = ValueAnimator.ofPropertyValuesHolder(new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat("value", new float[]{aPos2, aMax})});
            int duration = (int) (400.0f * (Math.abs(aMax - aPos2) / (aMax / 2.0f)));
            int duration2 = 200;
            if (duration > 200) {
                duration2 = duration;
            }
            animation.setDuration((long) duration2);
            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    Object object = animation.getAnimatedValue("value");
                    if (object != null) {
                        float lValue = ((Float) object).floatValue();
                        if (HwSplitScreenArrowView.this.mOrientation == 2) {
                            HwSplitScreenArrowView.this.mParams.x = ((int) lValue) - (3 * HwSplitScreenArrowView.this.mNavBarHeight);
                        } else {
                            HwSplitScreenArrowView.this.mParams.y = ((int) lValue) - (3 * HwSplitScreenArrowView.this.mNavBarHeight);
                        }
                        if (HwSplitScreenArrowView.this.mWindowMgr != null) {
                            try {
                                HwSplitScreenArrowView.this.runOnUiThread(new Runnable() {
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
                public void onAnimationStart(Animator animation) {
                }

                public void onAnimationRepeat(Animator animation) {
                }

                public void onAnimationEnd(Animator animation) {
                }

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
        if (this.mAm == null) {
            return false;
        }
        List<ActivityManager.RunningTaskInfo> tasks = this.mAm.getRunningTasks(1);
        if (tasks == null || tasks.isEmpty()) {
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
        if (this.mIam == null) {
            return false;
        }
        try {
            return this.mIam.isInLockTaskMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    private List<ActivityManager.RunningTaskInfo> getRunningTasks(int numTasks) {
        if (this.mAm == null) {
            return null;
        }
        return this.mAm.getRunningTasks(numTasks);
    }

    private ActivityManager.RunningTaskInfo getTopMostTask() {
        List<ActivityManager.RunningTaskInfo> tasks = getRunningTasks(1);
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }
        return tasks.get(0);
    }

    private boolean isInHomeStack(ActivityManager.RunningTaskInfo runningTask) {
        boolean z = false;
        if (runningTask == null) {
            return false;
        }
        if (runningTask.configuration.windowConfiguration.getActivityType() == 2) {
            z = true;
        }
        return z;
    }

    private void showToastForAllUser(Context mContext2, int message) {
        if (mContext2 != null) {
            Toast toast = Toast.makeText(mContext2, message, 0);
            toast.getWindowParams().privateFlags |= 16;
            toast.show();
        }
    }

    private boolean shouldTriggerFreeForm() {
        return SplitNotificationUtils.getInstance(this.mContext).getNotificationType("", 2).equals("floating_window");
    }

    /* access modifiers changed from: private */
    public void runOnUiThread(Runnable runnable) {
        Handler handler = getHandler();
        if (handler == null || handler.getLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }
}
