package com.android.server.policy;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.Log;
import android.view.IDockedStackListener.Stub;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.server.LocalServices;
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
    private ImageView mArrowIcon;
    private Context mContext;
    private int mDistanceThreshold;
    private boolean mDockedStackExists;
    private float[] mDownPoint = new float[2];
    IActivityManager mIam = null;
    private TextView mInfoText;
    private boolean mIsDisabled;
    private boolean mIsMoving;
    private boolean mIsScreenLarge;
    private boolean mIsshownToast;
    private boolean mLaunchSplitScreen;
    private int mNavBarHeight;
    private int mOrientation;
    private LayoutParams mParams;
    private Point mScreenDims = new Point();
    private WindowManager mWindowMgr;

    public HwSplitScreenArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mAm = (ActivityManager) context.getSystemService("activity");
        this.mIam = ActivityManagerNative.getDefault();
        this.mWindowMgr = (WindowManager) this.mContext.getSystemService("window");
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        updateDockedStackFlag();
    }

    public void initViewParams(int orientation, Point screenDims) {
        this.mArrowIcon = (ImageView) findViewById(34603137);
        this.mInfoText = (TextView) findViewById(34603138);
        Resources res = this.mContext.getResources();
        this.mOrientation = orientation;
        this.mNavBarHeight = res.getDimensionPixelSize(17105141);
        this.mScreenDims = screenDims;
        this.mParams = new LayoutParams(-1, -1, 2014, 16777736, -3);
        if (this.mOrientation == 2) {
            this.mParams.gravity = 48;
        } else {
            this.mParams.gravity = 1;
        }
        this.mParams.width = this.mScreenDims.x;
        this.mParams.height = this.mScreenDims.y;
        this.mIsScreenLarge = isScreenLarge();
        if (this.mOrientation != 2 || (this.mIsScreenLarge ^ 1) == 0) {
            this.mDistanceThreshold = this.mScreenDims.y / 8;
        } else {
            this.mDistanceThreshold = this.mScreenDims.x / 8;
        }
    }

    private void updateDockedStackFlag() {
        try {
            WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(new Stub() {
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
        if (aPosY > ((float) (this.mNavBarHeight * 4))) {
            this.mParams.y = ((int) aPosY) - (this.mNavBarHeight * 3);
            this.mWindowMgr.updateViewLayout(this, this.mParams);
        }
    }

    private void moveToPositionX(float aPosX) {
        if (aPosX > ((float) (this.mNavBarHeight * 4))) {
            this.mParams.x = ((int) aPosX) - (this.mNavBarHeight * 3);
            this.mWindowMgr.updateViewLayout(this, this.mParams);
        }
    }

    public void handleSplitScreenGesture(MotionEvent event) {
        Log.d("fingerTest", "event.x=:" + event.getX() + " event.y=:" + event.getY());
        if (event.getAction() == 0) {
            Log.d("fingerTest", "MotionEvent.ACTION_DOWN");
            this.mArrowIcon.setVisibility(0);
            this.mDownPoint[0] = event.getX();
            this.mDownPoint[1] = event.getY();
        }
        if (event.getActionMasked() == 5) {
            Log.d("fingerTest", "MotionEvent.ACTION_POINTER_DOWN");
            this.mArrowIcon.setVisibility(0);
            this.mDownPoint[0] = event.getX();
            this.mDownPoint[1] = event.getY();
        }
        if (event.getAction() == 2 && event.getPointerCount() == 2) {
            if (!this.mIsMoving) {
                this.mIsDisabled = !this.mDockedStackExists ? isTopTaskHome() : true;
                this.mInfoText.setText(33685889);
                if (isTopTaskHome()) {
                    showToastForAllUser(this.mContext, 33685923);
                }
                this.mIsMoving = true;
            }
            processFingerMoveEvent(event);
        } else if (event.getActionMasked() == 6) {
            Log.d("fingerTest", "MotionEvent.ACTION_UP");
            enterSplitScreenMode();
        } else if (event.getAction() == 3) {
            reset();
            setVisibility(8);
        }
    }

    private void processFingerMoveEvent(MotionEvent event) {
        if (this.mIsDisabled) {
            Log.d("fingerTest", "no response for finger move event");
            return;
        }
        float lDistance;
        if (this.mIsScreenLarge || this.mOrientation != 2) {
            lDistance = this.mDownPoint[1] - event.getY();
        } else {
            lDistance = this.mDownPoint[0] - event.getX();
        }
        Log.d("fingerTest", "lDistance = :" + lDistance);
        if (lDistance < ((float) this.mNavBarHeight)) {
            Log.d("fingerTest", "return since swipe distance too closely");
        } else if (isTopTaskSupportMultiWindow()) {
            if (lDistance > ((float) this.mDistanceThreshold)) {
                this.mInfoText.setVisibility(0);
                this.mLaunchSplitScreen = true;
            } else {
                this.mInfoText.setVisibility(4);
                this.mLaunchSplitScreen = false;
            }
            if (1 == this.mOrientation || (this.mIsScreenLarge ^ 1) != 0) {
                setVisibility(0);
            }
            if (this.mIsScreenLarge || this.mOrientation != 2) {
                this.mAnimStartPos = event.getRawY();
                moveToPositionY(this.mAnimStartPos);
            } else {
                this.mAnimStartPos = event.getRawX();
                moveToPositionX(this.mAnimStartPos);
            }
        } else {
            showToast();
        }
    }

    private void reset() {
        this.mLaunchSplitScreen = false;
        this.mIsshownToast = false;
        this.mIsMoving = false;
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
        if (!this.mIsshownToast) {
            showToastForAllUser(this.mContext, 33685924);
            this.mIsshownToast = true;
        }
    }

    private void animateView(float aPos, boolean islaunchingMultiWindow) {
        if (aPos <= 0.0f) {
            setVisibility(8);
            return;
        }
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
        if (aPos <= aMin) {
            aPos = aMin;
        }
        PropertyValuesHolder animValue = PropertyValuesHolder.ofFloat("value", new float[]{aPos, aMax});
        ValueAnimator animation = ValueAnimator.ofPropertyValuesHolder(new PropertyValuesHolder[]{animValue});
        int duration = (int) (400.0f * (Math.abs(aMax - aPos) / (aMax / 2.0f)));
        if (duration <= 200) {
            duration = 200;
        }
        animation.setDuration((long) duration);
        animation.addUpdateListener(new AnimatorUpdateListener() {
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
                            HwSplitScreenArrowView.this.mWindowMgr.updateViewLayout(HwSplitScreenArrowView.this, HwSplitScreenArrowView.this.mParams);
                        } catch (IllegalArgumentException exp) {
                            Log.e(HwSplitScreenArrowView.TAG, exp.getMessage());
                        }
                    }
                }
            }
        });
        animation.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                HwSplitScreenArrowView.this.mArrowIcon.setVisibility(4);
                HwSplitScreenArrowView.this.mInfoText.setVisibility(4);
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                HwSplitScreenArrowView.this.setVisibility(8);
            }

            public void onAnimationCancel(Animator animation) {
                HwSplitScreenArrowView.this.setVisibility(8);
            }
        });
        animation.start();
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
        List<RunningTaskInfo> tasks = this.mAm.getRunningTasks(1);
        if (tasks == null || (tasks.isEmpty() ^ 1) == 0) {
            return false;
        }
        RunningTaskInfo topTask = (RunningTaskInfo) tasks.get(0);
        return topTask != null ? isInHomeStack(topTask.stackId) : false;
    }

    private boolean isTopTaskSupportMultiWindow() {
        RunningTaskInfo topTask = getTopMostTask();
        if (topTask == null || isInHomeStack(topTask.stackId) || (isScreenPinningActive() ^ 1) == 0 || !topTask.supportsSplitScreenMultiWindow) {
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

    private List<RunningTaskInfo> getRunningTasks(int numTasks) {
        if (this.mAm == null) {
            return null;
        }
        return this.mAm.getRunningTasks(numTasks);
    }

    private RunningTaskInfo getTopMostTask() {
        List<RunningTaskInfo> tasks = getRunningTasks(1);
        if (tasks == null || (tasks.isEmpty() ^ 1) == 0) {
            return null;
        }
        return (RunningTaskInfo) tasks.get(0);
    }

    private boolean isInHomeStack(int stackId) {
        return stackId == 0;
    }

    private void showToastForAllUser(Context mContext, int message) {
        if (mContext != null) {
            Toast toast = Toast.makeText(mContext, message, 0);
            LayoutParams windowParams = toast.getWindowParams();
            windowParams.privateFlags |= 16;
            toast.show();
        }
    }
}
