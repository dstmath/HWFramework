package com.android.server.wm;

import android.app.IActivityTaskManager;
import android.freeform.HwFreeFormUtils;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.Trace;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.BatchedInputEventReceiver;
import android.view.Choreographer;
import android.view.Display;
import android.view.IWindow;
import android.view.InputApplicationHandle;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputWindowHandle;
import android.view.MotionEvent;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.HwServiceExFactory;
import com.android.server.wm.utils.HwDisplaySizeUtil;
import com.huawei.android.os.HwVibrator;
import com.huawei.anim.dynamicanimation.util.DynamicCurveRate;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* access modifiers changed from: package-private */
public class TaskPositioner implements IBinder.DeathRecipient {
    private static final int BETWEEN_MINSCALE_AND_MAXSCALE = 0;
    private static final int BIGGER_THAN_MAXSCALE = 1;
    static final int CTRL_BOTTOM = 8;
    static final int CTRL_LEFT = 1;
    static final int CTRL_NONE = 0;
    static final int CTRL_RIGHT = 2;
    static final int CTRL_TOP = 4;
    private static final boolean DEBUG_ORIENTATION_VIOLATIONS = false;
    private static final int DISABLE_ANIMATION_DURATION = -1;
    private static final int DISPOSE_MSG = 0;
    private static final int ERROR_RANGE_FLOAT_TO_INT = 2;
    private static final float FLOAT_CMP_EQ_TH = 0.01f;
    @VisibleForTesting
    static final float MIN_ASPECT = 1.2f;
    public static final float RESIZING_HINT_ALPHA = 0.5f;
    public static final int RESIZING_HINT_DURATION_MS = 0;
    static final int SIDE_MARGIN_DIP = 100;
    private static final int SMALLER_THAN_MINSCALE = -1;
    private static final String TAG = "WindowManager";
    private static final String TAG_LOCAL = "TaskPositioner";
    private static final int TYPE_HEIGHT = 2;
    private static final int TYPE_WIDTH = 1;
    private static Factory sFactory;
    Handler PCHandler;
    private float dampingCoefficient;
    private float defaultScale;
    private float dynamicDelta;
    private final IActivityTaskManager mActivityManager;
    IBinder mClientCallback;
    InputChannel mClientChannel;
    private final Rect mContainingBounds;
    private int mCtrlType;
    private float mCurScale;
    private int mDelta;
    private DisplayContent mDisplayContent;
    private final DisplayMetrics mDisplayMetrics;
    InputApplicationHandle mDragApplicationHandle;
    @VisibleForTesting
    boolean mDragEnded;
    InputWindowHandle mDragWindowHandle;
    private int mDragbarHeight;
    private int mDragbarWidth;
    private boolean mHasSideinScreen;
    IHwActivityTaskManagerServiceEx mHwATMSEx;
    private int mHwFreeformCaptionBarHeight;
    IHwTaskPositionerEx mHwTPEx;
    private float mInitialScale;
    private WindowPositionerEventReceiver mInputEventReceiver;
    private boolean mIsCalledByResize;
    private boolean mIsOutBound;
    private boolean mIsTouching;
    private final Point mMaxVisibleSize;
    private int mMinVisibleHeight;
    private int mMinVisibleWidth;
    private boolean mPreserveOrientation;
    private boolean mResizing;
    private int mRightDelta;
    private int mSafeSideWidth;
    private float mScale;
    InputChannel mServerChannel;
    private final WindowManagerService mService;
    private int mSideMargin;
    private Rect mStartAnimationBounds;
    private float mStartDragX;
    private float mStartDragY;
    private boolean mStartOrientationWasLandscape;
    private int mStatusBarHeight;
    private Rect mStopAnimationBounds;
    @VisibleForTesting
    Task mTask;
    private Rect mTmpRect;
    private int mVibratorFlag;
    private final Rect mWindowDragBounds;
    private final Rect mWindowOriginalBounds;
    private float maxScale;
    private float minScale;
    private boolean preIsDampState;
    private float previousFrameX;
    private float previousFrameY;
    private boolean reboundIsOutOfScreen;

    @Retention(RetentionPolicy.SOURCE)
    @interface CtrlType {
    }

    /* access modifiers changed from: private */
    public final class WindowPositionerEventReceiver extends BatchedInputEventReceiver {
        public WindowPositionerEventReceiver(InputChannel inputChannel, Looper looper, Choreographer choreographer) {
            super(inputChannel, looper, choreographer);
        }

        /* JADX INFO: finally extract failed */
        public void onInputEvent(InputEvent event) {
            boolean handled = false;
            try {
                if (event instanceof MotionEvent) {
                    if ((event.getSource() & 2) != 0) {
                        MotionEvent motionEvent = (MotionEvent) event;
                        if (TaskPositioner.this.mDragEnded) {
                            finishInputEvent(event, true);
                            return;
                        }
                        if (HwFoldScreenState.isFoldScreenDevice() && TaskPositioner.this.mService.isInSubFoldScaleMode() && TaskPositioner.this.mTask.inHwFreeFormWindowingMode()) {
                            TaskPositioner.this.mScale = TaskPositioner.this.mService.mSubFoldModeScale;
                        }
                        float newX = motionEvent.getRawX() / TaskPositioner.this.mScale;
                        float newY = motionEvent.getRawY() / TaskPositioner.this.mScale;
                        int action = motionEvent.getAction();
                        if (action == 0) {
                            if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
                                Slog.w(TaskPositioner.TAG, "ACTION_DOWN @ {" + newX + ", " + newY + "}");
                            }
                            TaskPositioner.this.mInitialScale = TaskPositioner.this.mTask.mStack.mHwStackScale;
                        } else if (action == 1) {
                            if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
                                Slog.w(TaskPositioner.TAG, "ACTION_UP @ {" + newX + ", " + newY + "}");
                            }
                            if (HwFreeFormUtils.isFreeFormEnable()) {
                                TaskPositioner.this.mIsTouching = false;
                                TaskPositioner.this.updateFreeFormOutLine(2);
                            }
                            if (!TaskPositioner.this.mResizing && HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(TaskPositioner.this.mTask.getDisplayContent().getDisplayId())) {
                                TaskPositioner.this.processPCWindowFinishDragHitHotArea(TaskPositioner.this.mTask.mTaskRecord, newX, newY);
                            }
                            if (TaskPositioner.this.mCurScale * TaskPositioner.this.mInitialScale <= TaskPositioner.this.maxScale && TaskPositioner.this.mCurScale * TaskPositioner.this.mInitialScale >= TaskPositioner.this.minScale) {
                                TaskPositioner.this.mStartAnimationBounds.set(TaskPositioner.this.mWindowDragBounds.left, TaskPositioner.this.mWindowDragBounds.top, TaskPositioner.this.mWindowDragBounds.left + ((int) (((float) TaskPositioner.this.mWindowDragBounds.width()) * TaskPositioner.this.mInitialScale)), TaskPositioner.this.mWindowDragBounds.top + ((int) (((float) TaskPositioner.this.mWindowDragBounds.height()) * TaskPositioner.this.mInitialScale)));
                            }
                            TaskPositioner.this.mDragEnded = true;
                        } else if (action == 2) {
                            if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
                                Slog.w(TaskPositioner.TAG, "ACTION_MOVE @ {" + newX + ", " + newY + "}");
                            }
                            synchronized (TaskPositioner.this.mService.mGlobalLock) {
                                try {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    TaskPositioner.this.mDragEnded = TaskPositioner.this.notifyMoveLocked(newX, newY);
                                    TaskPositioner.this.mTask.getDimBounds(TaskPositioner.this.mTmpRect);
                                } catch (Throwable th) {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            if (!TaskPositioner.this.mTmpRect.equals(TaskPositioner.this.mWindowDragBounds)) {
                                Trace.traceBegin(32, "wm.TaskPositioner.resizeTask");
                                try {
                                    if (TaskPositioner.this.mTask.inHwFreeFormWindowingMode()) {
                                        if (!TaskPositioner.this.mResizing) {
                                            TaskPositioner.this.mTask.updateHwFreeFormScaleLeash(TaskPositioner.this.mWindowDragBounds.left, TaskPositioner.this.mWindowDragBounds.top, TaskPositioner.this.mCurScale);
                                        }
                                    } else if (TaskPositioner.this.mTask.getWindowingMode() == 1) {
                                        Slog.i(TaskPositioner.TAG, "No resize in fullscreen mode");
                                    } else {
                                        TaskPositioner.this.mActivityManager.resizeTask(TaskPositioner.this.mTask.mTaskId, TaskPositioner.this.mWindowDragBounds, 1);
                                    }
                                } catch (RemoteException e) {
                                }
                                Trace.traceEnd(32);
                            }
                            if (!TaskPositioner.this.mResizing && HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(TaskPositioner.this.mTask.getDisplayContent().getDisplayId())) {
                                TaskPositioner.this.processPCWindowDragHitHotArea(TaskPositioner.this.mTask.mTaskRecord, newX, newY);
                            }
                        } else if (action == 3) {
                            if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
                                Slog.w(TaskPositioner.TAG, "ACTION_CANCEL @ {" + newX + ", " + newY + "}");
                            }
                            if (!TaskPositioner.this.mResizing && HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(TaskPositioner.this.mTask.getDisplayContent().getDisplayId())) {
                                TaskPositioner.this.processPCWindowFinishDragHitHotArea(TaskPositioner.this.mTask.mTaskRecord, newX, newY);
                            }
                            TaskPositioner.this.mDragEnded = true;
                        }
                        if (TaskPositioner.this.mDragEnded) {
                            TaskPositioner.this.endHwFreeFormDrag();
                            boolean wasResizing = TaskPositioner.this.mResizing;
                            if (!wasResizing && TaskPositioner.this.mService.mAtmService.mHwATMSEx.isPadCastStack(TaskPositioner.this.mTask.mTaskRecord.getStack())) {
                                Bundle bundle = new Bundle();
                                bundle.putString("android.intent.extra.REASON", "freeFormDragEnd");
                                bundle.putInt("actionType", motionEvent.getAction());
                                bundle.putInt("android.intent.extra.user_handle", TaskPositioner.this.mService.mCurrentUserId);
                                TaskPositioner.this.mService.mAtmService.mHwATMSEx.call(bundle);
                                Slog.i(TaskPositioner.TAG, "notify freeFormDragEnd actionType=" + motionEvent.getAction());
                            }
                            synchronized (TaskPositioner.this.mService.mGlobalLock) {
                                try {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    TaskPositioner.this.endDragLocked();
                                    TaskPositioner.this.mTask.getDimBounds(TaskPositioner.this.mTmpRect);
                                } finally {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                }
                            }
                            try {
                                if (HwFreeFormUtils.isFreeFormEnable() && !((TaskPositioner.this.mWindowOriginalBounds.width() == TaskPositioner.this.mWindowDragBounds.width() && TaskPositioner.this.mWindowOriginalBounds.height() == TaskPositioner.this.mWindowDragBounds.height()) || TaskPositioner.this.mTask.getParent() == null)) {
                                    Rect availableRect = TaskPositioner.this.mTask.getParent().getBounds();
                                    Flog.bdReport(991311067, "{height:" + availableRect.height() + ",width:" + availableRect.width() + ",left:" + TaskPositioner.this.mWindowDragBounds.left + ",top:" + TaskPositioner.this.mWindowDragBounds.top + ",right:" + TaskPositioner.this.mWindowDragBounds.right + ",bottom:" + TaskPositioner.this.mWindowDragBounds.bottom + "}");
                                }
                                if (wasResizing && !TaskPositioner.this.mTmpRect.equals(TaskPositioner.this.mWindowDragBounds)) {
                                    if (TaskPositioner.this.mTask.inHwFreeFormWindowingMode()) {
                                        TaskPositioner.this.mActivityManager.resizeStack(TaskPositioner.this.mTask.mTaskRecord.getStack().getStackId(), TaskPositioner.this.mWindowDragBounds, false, false, false, -1);
                                    } else if (TaskPositioner.this.mTask.getWindowingMode() == 1) {
                                        Slog.i(TaskPositioner.TAG, "No resize in fullscreen mode");
                                    } else {
                                        TaskPositioner.this.mActivityManager.resizeTask(TaskPositioner.this.mTask.mTaskId, TaskPositioner.this.mWindowDragBounds, 3);
                                    }
                                }
                            } catch (RemoteException e2) {
                            }
                            TaskPositioner.this.mIsCalledByResize = false;
                            TaskPositioner.this.mService.mTaskPositioningController.finishTaskPositioning();
                        }
                        handled = true;
                        finishInputEvent(event, handled);
                        return;
                    }
                }
                finishInputEvent(event, false);
            } catch (Exception e3) {
                Slog.e(TaskPositioner.TAG, "Exception caught by drag handleMotion", e3);
            } catch (Throwable th2) {
                finishInputEvent(event, false);
                throw th2;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void endHwFreeFormDrag() {
        if (!this.mResizing && this.mTask.inHwFreeFormWindowingMode()) {
            try {
                this.mActivityManager.resizeStack(this.mTask.mTaskRecord.getStack().getStackId(), this.mWindowDragBounds, false, false, false, -1);
            } catch (RemoteException e) {
                Slog.e(TAG, "Stack not resized");
            }
        }
        relocateOffScreenHwFreeformWindow();
    }

    @VisibleForTesting
    TaskPositioner(WindowManagerService service, IActivityTaskManager activityManager) {
        this.mDelta = 0;
        this.mRightDelta = 0;
        this.mIsTouching = false;
        this.mIsOutBound = false;
        this.mDisplayMetrics = new DisplayMetrics();
        this.mTmpRect = new Rect();
        this.mStartAnimationBounds = new Rect();
        this.mStopAnimationBounds = new Rect();
        this.mWindowOriginalBounds = new Rect();
        this.mContainingBounds = new Rect();
        this.mWindowDragBounds = new Rect();
        this.mMaxVisibleSize = new Point();
        this.mInitialScale = 1.0f;
        this.mCurScale = 1.0f;
        this.mScale = 1.0f;
        this.mCtrlType = 0;
        this.PCHandler = null;
        this.mHasSideinScreen = false;
        this.mSafeSideWidth = 0;
        this.mIsCalledByResize = false;
        this.maxScale = 1.0f;
        this.minScale = 0.5f;
        this.defaultScale = 0.79f;
        this.mVibratorFlag = 0;
        this.dynamicDelta = 0.0f;
        this.dampingCoefficient = 10.0f;
        this.preIsDampState = false;
        this.reboundIsOutOfScreen = false;
        this.mHwTPEx = null;
        this.mHwATMSEx = null;
        this.mService = service;
        this.mActivityManager = activityManager;
    }

    TaskPositioner(WindowManagerService service) {
        this(service, service.mActivityTaskManager);
        this.mHwTPEx = HwServiceExFactory.getHwTaskPositionerEx(service);
        this.mHwATMSEx = service.mAtmService.mHwATMSEx;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Rect getWindowDragBounds() {
        return this.mWindowDragBounds;
    }

    /* access modifiers changed from: package-private */
    public void register(DisplayContent displayContent) {
        Display display = displayContent.getDisplay();
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d(TAG, "Registering task positioner");
        }
        if (this.mClientChannel != null) {
            Slog.e(TAG, "Task positioner already registered");
            return;
        }
        this.mDisplayContent = displayContent;
        display.getMetrics(this.mDisplayMetrics);
        InputChannel[] channels = InputChannel.openInputChannelPair(TAG);
        this.mServerChannel = channels[0];
        this.mClientChannel = channels[1];
        this.mService.mInputManager.registerInputChannel(this.mServerChannel, (IBinder) null);
        this.mInputEventReceiver = new WindowPositionerEventReceiver(this.mClientChannel, this.mService.mAnimationHandler.getLooper(), this.mService.mAnimator.getChoreographer());
        this.mDragApplicationHandle = new InputApplicationHandle(new Binder());
        InputApplicationHandle inputApplicationHandle = this.mDragApplicationHandle;
        inputApplicationHandle.name = TAG;
        inputApplicationHandle.dispatchingTimeoutNanos = 5000000000L;
        this.mDragWindowHandle = new InputWindowHandle(inputApplicationHandle, (IWindow) null, display.getDisplayId());
        InputWindowHandle inputWindowHandle = this.mDragWindowHandle;
        inputWindowHandle.name = TAG;
        inputWindowHandle.token = this.mServerChannel.getToken();
        this.mDragWindowHandle.layer = this.mService.getDragLayerLocked();
        InputWindowHandle inputWindowHandle2 = this.mDragWindowHandle;
        inputWindowHandle2.layoutParamsFlags = 0;
        inputWindowHandle2.layoutParamsType = 2016;
        inputWindowHandle2.dispatchingTimeoutNanos = 5000000000L;
        inputWindowHandle2.visible = true;
        inputWindowHandle2.canReceiveKeys = false;
        inputWindowHandle2.hasFocus = true;
        inputWindowHandle2.hasWallpaper = false;
        inputWindowHandle2.paused = false;
        inputWindowHandle2.ownerPid = Process.myPid();
        this.mDragWindowHandle.ownerUid = Process.myUid();
        InputWindowHandle inputWindowHandle3 = this.mDragWindowHandle;
        inputWindowHandle3.inputFeatures = 0;
        inputWindowHandle3.scaleFactor = 1.0f;
        inputWindowHandle3.touchableRegion.setEmpty();
        InputWindowHandle inputWindowHandle4 = this.mDragWindowHandle;
        inputWindowHandle4.frameLeft = 0;
        inputWindowHandle4.frameTop = 0;
        Point p = new Point();
        display.getRealSize(p);
        this.mDragWindowHandle.frameRight = p.x;
        this.mDragWindowHandle.frameBottom = p.y;
        Slog.i(TAG, "Pausing rotation during re-position");
        this.mDisplayContent.pauseRotationLocked();
        this.mDisplayContent.getInputMonitor().updateInputWindowsLw(true);
        this.mSideMargin = WindowManagerService.dipToPixel(SIDE_MARGIN_DIP, this.mDisplayMetrics);
        this.mMinVisibleWidth = WindowManagerService.dipToPixel(48, this.mDisplayMetrics);
        this.mMinVisibleHeight = WindowManagerService.dipToPixel(32, this.mDisplayMetrics);
        this.mDragbarWidth = WindowManagerService.dipToPixel(70, this.mDisplayMetrics);
        this.mDragbarHeight = WindowManagerService.dipToPixel(4, this.mDisplayMetrics);
        this.mHwFreeformCaptionBarHeight = WindowManagerService.dipToPixel(36, this.mDisplayMetrics);
        display.getRealSize(this.mMaxVisibleSize);
        HwFreeFormUtils.computeFreeFormSize(this.mMaxVisibleSize);
        this.mDelta = WindowManagerService.dipToPixel(10, this.mDisplayMetrics);
        this.mDragEnded = false;
    }

    private void updateDottingReport(float curScale, float initialScale) {
        if (this.mCtrlType != 0) {
            Flog.bdReport(991310117, "{operateArea:" + this.mCtrlType + ",originalScale:" + initialScale + ",currentScale:" + (curScale * initialScale) + "}");
        }
    }

    /* access modifiers changed from: package-private */
    public void unregister() {
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d(TAG, "Unregistering task positioner");
        }
        if (this.mCtrlType == 0 && this.mTask.mStack != null) {
            float[] scale = this.mHwATMSEx.getScaleRange(this.mTask.mStack.mActivityStack);
            if (scale[0] > 1.0E-6f) {
                this.minScale = scale[0];
                this.maxScale = scale[1];
                this.defaultScale = scale[2];
            }
            this.mInitialScale = this.mTask.mStack.mHwStackScale;
        }
        this.dynamicDelta = 0.0f;
        Task task = this.mTask;
        if (task != null && task.inHwFreeFormWindowingMode() && (this.mTask.mTaskRecord == null || this.mTask.mTaskRecord.getStack() == null || !this.mHwATMSEx.isPadCastStack(this.mTask.mTaskRecord.getStack()))) {
            startReboundAniamtion();
        }
        this.reboundIsOutOfScreen = false;
        if (this.mClientChannel == null) {
            Slog.e(TAG, "Task positioner not registered");
            return;
        }
        this.mService.mInputManager.unregisterInputChannel(this.mServerChannel);
        if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(this.mDisplayContent.getDisplayId())) {
            this.mService.mAnimationHandler.post(new Runnable() {
                /* class com.android.server.wm.$$Lambda$TaskPositioner$2nYLiRaGrINcRTe4opwrzYSOcIU */

                @Override // java.lang.Runnable
                public final void run() {
                    TaskPositioner.this.lambda$unregister$0$TaskPositioner();
                }
            });
        } else {
            this.PCHandler = new Handler(this.mService.mAnimationHandler.getLooper()) {
                /* class com.android.server.wm.TaskPositioner.AnonymousClass1 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    if (msg.what == 0) {
                        TaskPositioner.this.mInputEventReceiver.dispose();
                        TaskPositioner.this.mInputEventReceiver = null;
                    }
                }
            };
            this.PCHandler.removeMessages(0);
            this.PCHandler.sendEmptyMessage(0);
        }
        this.mClientChannel.dispose();
        this.mServerChannel.dispose();
        this.mClientChannel = null;
        this.mServerChannel = null;
        this.mDragWindowHandle = null;
        this.mDragApplicationHandle = null;
        this.mDragEnded = true;
        this.mDisplayContent.getInputMonitor().updateInputWindowsLw(true);
        Slog.i(TAG, "Resuming rotation after re-position");
        this.mDisplayContent.resumeRotationLocked();
        this.mDisplayContent = null;
        this.mClientCallback.unlinkToDeath(this, 0);
    }

    public /* synthetic */ void lambda$unregister$0$TaskPositioner() {
        this.mInputEventReceiver.dispose();
        this.mInputEventReceiver = null;
    }

    private void startReboundAniamtion() {
        updateDottingReport(this.mCurScale, this.mInitialScale);
        float f = this.mCurScale;
        float f2 = this.mInitialScale;
        if (f * f2 > this.maxScale || f * f2 < this.minScale) {
            Rect tempRect = new Rect();
            tempRect.set(this.mStopAnimationBounds.left, this.mStopAnimationBounds.top, this.mStopAnimationBounds.left + this.mWindowOriginalBounds.width(), this.mStopAnimationBounds.top + this.mWindowOriginalBounds.height());
            this.mWindowDragBounds.set(tempRect);
            relocateOffScreenHwFreeformWindow();
            this.mService.mH.post(new Runnable() {
                /* class com.android.server.wm.$$Lambda$TaskPositioner$eMIS9fl_lcu3rBv9W_iUM9V7IeE */

                @Override // java.lang.Runnable
                public final void run() {
                    TaskPositioner.this.lambda$startReboundAniamtion$1$TaskPositioner();
                }
            });
            startScaleAnimation();
        } else if (this.reboundIsOutOfScreen) {
            Rect tmpRect = new Rect();
            tmpRect.set(this.mStopAnimationBounds.left, this.mStopAnimationBounds.top, this.mStopAnimationBounds.left + this.mWindowOriginalBounds.width(), this.mStopAnimationBounds.top + this.mWindowOriginalBounds.height());
            this.mService.mH.post(new Runnable(tmpRect) {
                /* class com.android.server.wm.$$Lambda$TaskPositioner$WzbgIbQlXNTYoFda_EgX9Fj6Jqg */
                private final /* synthetic */ Rect f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    TaskPositioner.this.lambda$startReboundAniamtion$2$TaskPositioner(this.f$1);
                }
            });
            startScaleAnimation();
        } else {
            destroyLeash();
        }
        this.mVibratorFlag = 0;
    }

    public /* synthetic */ void lambda$startReboundAniamtion$1$TaskPositioner() {
        Rect tmpRect = new Rect();
        this.mTask.getBounds(tmpRect);
        lambda$startReboundAniamtion$2$TaskPositioner(tmpRect);
    }

    /* access modifiers changed from: private */
    /* renamed from: resizeStack */
    public void lambda$startReboundAniamtion$2$TaskPositioner(Rect tmpRect) {
        try {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    tmpRect.set(this.mStopAnimationBounds.left, this.mStopAnimationBounds.top, this.mStopAnimationBounds.left + tmpRect.width(), this.mStopAnimationBounds.top + tmpRect.height());
                    this.mActivityManager.resizeStack(this.mTask.mTaskRecord.getStackId(), tmpRect, false, false, false, -1);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Task positioner not resized");
        }
    }

    private void startScaleAnimation() {
        if (!this.mTask.getAnimRunningFlag()) {
            relocateOffScreenHwFreeformWindow();
            this.mTask.startReboundScaleAnimation(this.mStartAnimationBounds, this.mStopAnimationBounds, this.mCtrlType, new float[]{this.mCurScale, this.mInitialScale}, this.reboundIsOutOfScreen);
            return;
        }
        destroyLeash();
    }

    private void destroyLeash() {
        float f = this.mCurScale;
        float f2 = this.mInitialScale;
        float f3 = this.maxScale;
        if (f * f2 > f3) {
            this.mTask.destroyHwFreeFormScaleLeash(f3);
        } else {
            float f4 = this.minScale;
            if (f * f2 < f4) {
                this.mTask.destroyHwFreeFormScaleLeash(f4);
            } else {
                this.mTask.destroyHwFreeFormScaleLeash(f * f2);
            }
        }
        updateFreeFormOutLine(-2);
    }

    /* access modifiers changed from: package-private */
    public void startDrag(WindowState win, boolean resize, boolean preserveOrientation, float startX, float startY) {
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d(TAG, "startDrag: win=" + win + ", resize=" + resize + ", preserveOrientation=" + preserveOrientation + ", {" + startX + ", " + startY + "}");
        }
        try {
            this.mClientCallback = win.mClient.asBinder();
            this.mClientCallback.linkToDeath(this, 0);
            this.previousFrameX = startX;
            this.previousFrameY = startY;
            this.dynamicDelta = 0.0f;
            this.mTask = win.getTask();
            this.mTask.getBounds(this.mTmpRect);
            this.mContainingBounds.set(win.getContainingFrame());
            this.mHasSideinScreen = HwDisplaySizeUtil.hasSideInScreen();
            this.mSafeSideWidth = HwDisplaySizeUtil.getInstance(this.mService).getSafeSideWidth();
            if (this.mTask.inHwFreeFormWindowingMode() && this.mTask.mStack != null && !resize) {
                startX = ((float) this.mTmpRect.left) + ((startX - ((float) this.mTmpRect.left)) * this.mTask.mStack.mHwStackScale * win.mGlobalScale);
                startY = ((float) this.mTmpRect.top) + ((startY - ((float) this.mTmpRect.top)) * this.mTask.mStack.mHwStackScale * win.mGlobalScale);
            }
            startDrag(resize, preserveOrientation, startX, startY, this.mTmpRect);
        } catch (RemoteException e) {
            this.mService.mTaskPositioningController.finishTaskPositioning();
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public void startDrag(boolean resize, boolean preserveOrientation, float startX, float startY, Rect startBounds) {
        this.mCtrlType = 0;
        this.mStartDragX = startX;
        this.mStartDragY = startY;
        this.mPreserveOrientation = preserveOrientation;
        if (resize) {
            if (!this.mTask.inHwFreeFormWindowingMode() || this.mTask.mStack == null) {
                if (startX < ((float) (startBounds.left + this.mDelta))) {
                    this.mCtrlType |= 1;
                }
                if (startX > ((float) (startBounds.right - this.mDelta))) {
                    this.mCtrlType |= 2;
                }
                if (startY < ((float) startBounds.top)) {
                    this.mCtrlType |= 4;
                }
                if (startY > ((float) (startBounds.bottom - this.mDelta))) {
                    this.mCtrlType |= CTRL_BOTTOM;
                }
            } else {
                this.mCtrlType = getCtrlType(startX, startY, startBounds, 0);
            }
            this.mResizing = this.mCtrlType != 0;
        }
        this.mStartOrientationWasLandscape = startBounds.width() >= startBounds.height();
        this.mWindowOriginalBounds.set(startBounds);
        this.mStatusBarHeight = this.mService.mAtmService.mUiContext.getResources().getDimensionPixelSize(17105445);
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mTask.inHwFreeFormWindowingMode()) {
                    this.mTask.initHwFreeFormScaleLeash(startBounds);
                }
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        if (this.mResizing) {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (this.mTask.inHwFreeFormWindowingMode()) {
                        float[] scale = this.mHwATMSEx.getScaleRange(this.mTask.mStack.mActivityStack);
                        if (scale[0] > 1.0E-6f) {
                            this.minScale = scale[0];
                            this.maxScale = scale[1];
                            this.defaultScale = scale[2];
                        }
                        updateFreeFormOutLine(-1);
                    }
                    notifyMoveLocked(startX, startY);
                } catch (Throwable th2) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th2;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            this.mService.mH.post(new Runnable(startBounds) {
                /* class com.android.server.wm.$$Lambda$TaskPositioner$9fw7PLlJHEPQ7HOZ309Vn35NEk */
                private final /* synthetic */ Rect f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    TaskPositioner.this.lambda$startDrag$3$TaskPositioner(this.f$1);
                }
            });
        }
        this.mWindowDragBounds.set(startBounds);
    }

    public /* synthetic */ void lambda$startDrag$3$TaskPositioner(Rect startBounds) {
        try {
            if (this.mTask.inHwFreeFormWindowingMode()) {
                this.mActivityManager.resizeStack(this.mTask.mTaskRecord.getStackId(), startBounds, false, false, false, -1);
            } else if (this.mTask.getWindowingMode() == 1) {
                Slog.i(TAG, "No resize in fullscreen mode!");
            } else {
                this.mActivityManager.resizeTask(this.mTask.mTaskId, startBounds, 3);
            }
        } catch (RemoteException e) {
        }
    }

    private int getCtrlType(float startX, float startY, Rect startBounds, int ctrlType) {
        Rect tmpStartBounds = new Rect(startBounds);
        this.mInitialScale = this.mTask.mStack.mHwStackScale;
        int left = startBounds.left;
        int top = startBounds.top;
        tmpStartBounds.scale(this.mInitialScale);
        tmpStartBounds.offsetTo(left, top);
        this.mDelta = WindowManagerService.dipToPixel(5, this.mDisplayMetrics);
        this.mRightDelta = (((WindowManagerService.dipToPixel(20, this.mDisplayMetrics) * 2) + tmpStartBounds.width()) / 3) - WindowManagerService.dipToPixel(20, this.mDisplayMetrics);
        if (startX <= ((float) (tmpStartBounds.left + this.mDelta)) || (startX <= ((float) (tmpStartBounds.left + this.mRightDelta)) && startY >= ((float) (tmpStartBounds.bottom - this.mDelta)))) {
            ctrlType |= 1;
        }
        if (startX >= ((float) (tmpStartBounds.right - this.mDelta)) || (startX >= ((float) (tmpStartBounds.right - this.mRightDelta)) && startY >= ((float) (tmpStartBounds.bottom - this.mDelta)))) {
            ctrlType |= 2;
        }
        if (startY <= ((float) tmpStartBounds.top)) {
            ctrlType |= 4;
        }
        if (startY >= ((float) (tmpStartBounds.bottom - this.mDelta))) {
            ctrlType |= CTRL_BOTTOM;
        }
        if (ctrlType == 0) {
            this.mIsCalledByResize = true;
            Slog.i(TAG, "getCtrlType startX:" + startX + " startY:" + startY + " tmpStartBounds:" + tmpStartBounds + " mDelta:" + this.mDelta + " mRightDelta:" + this.mRightDelta);
        }
        return ctrlType;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void endDragLocked() {
        this.mResizing = false;
        this.mTask.setDragResizing(false, 0);
    }

    /* JADX INFO: Multiple debug info for r0v9 int: [D('nX' int), D('stableBounds' android.graphics.Rect)] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean notifyMoveLocked(float x, float y) {
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d(TAG, "notifyMoveLocked: {" + x + "," + y + "}");
        }
        if (this.mIsCalledByResize && this.mCtrlType == 0) {
            return false;
        }
        if (this.mCtrlType != 0) {
            if (!this.mIsTouching) {
                updateFreeFormOutLine(4);
                this.mIsTouching = true;
            }
            if (!this.mTask.inHwFreeFormWindowingMode()) {
                resizeDrag(x, y);
            } else if (this.mService.mAtmService.mRootActivityContainer.isTopDisplayFocusedStack(this.mTask.mTaskRecord.getStack())) {
                resizeHwFreeFormDrag(x, y);
            }
            this.mTask.setDragResizing(true, 0);
            return false;
        }
        if (this.mTask.inHwFreeFormWindowingMode()) {
            allowFreeformDragPos(this.mTmpRect);
        } else {
            this.mTask.mStack.getDimBounds(this.mTmpRect);
        }
        if (this.mTask.mStack != null && HwPCUtils.isExtDynamicStack(this.mTask.mStack.mStackId)) {
            this.mTmpRect.bottom = this.mTask.mStack.getDisplayInfo().appHeight;
        }
        if (!this.mTask.inHwFreeFormWindowingMode()) {
            Rect stableBounds = new Rect();
            this.mDisplayContent.getStableRect(stableBounds);
            this.mTmpRect.intersect(stableBounds);
        }
        int nX = (int) x;
        int nY = (int) y;
        if (!this.mTmpRect.contains(nX, nY)) {
            nX = Math.min(Math.max(nX, this.mTmpRect.left), this.mTmpRect.right);
            nY = Math.min(Math.max(nY, this.mTmpRect.top), this.mTmpRect.bottom);
        }
        if (this.mTask.inHwFreeFormWindowingMode()) {
            updateHwFreeformWindowDragBounds(nX, nY, this.mTmpRect);
        } else {
            updateWindowDragBounds(nX, nY, this.mTmpRect);
        }
        return false;
    }

    private void resizeHwFreeFormDrag(float x, float y) {
        int right;
        float tempScale;
        int deltaY;
        int deltaX;
        int deltaY2;
        float tempScale2;
        float tempScale3;
        int deltaY3;
        int deltaX2;
        int deltaY4;
        float tempScale4;
        int deltaX3;
        int deltaX4;
        int deltaX5 = Math.round(x - this.mStartDragX);
        int deltaY5 = Math.round(y - this.mStartDragY);
        int left = this.mWindowOriginalBounds.left;
        int top = this.mWindowOriginalBounds.top;
        int right2 = this.mWindowOriginalBounds.right;
        int bottom = this.mWindowOriginalBounds.bottom;
        int width = right2 - left;
        int height = bottom - top;
        int i = this.mCtrlType;
        if (i == 1) {
            float tempScale5 = 1.0f - (((float) deltaX5) / (((float) width) * this.mInitialScale));
            isReachedThreshold(tempScale5);
            float f = this.mInitialScale;
            if (f * tempScale5 <= this.maxScale && f * tempScale5 >= this.minScale) {
                this.preIsDampState = false;
            }
            if (deltaX5 >= 0 || this.mInitialScale * tempScale5 <= this.maxScale) {
                deltaX3 = deltaX5;
            } else {
                changeSurfaceReboundState();
                int deltaXTempMin = (int) (((float) width) * (this.mInitialScale - this.maxScale));
                this.dynamicDelta += dampchange(width, x - this.previousFrameX, deltaX5 - deltaXTempMin);
                int deltaX6 = (int) (((float) deltaXTempMin) + this.dynamicDelta);
                float f2 = this.mInitialScale;
                tempScale5 = 1.0f - (((float) deltaX6) / (((float) width) * f2));
                deltaX3 = deltaX6;
                this.mStartAnimationBounds.set(left + deltaX6, top, left + deltaX6 + ((int) (((float) width) * tempScale5 * f2)), ((int) (((float) height) * tempScale5 * f2)) + top);
                this.mStopAnimationBounds.set(this.mStartAnimationBounds.right - ((int) (((float) width) * this.maxScale)), top, this.mStartAnimationBounds.right, ((int) (((float) height) * this.maxScale)) + top);
            }
            if (deltaX3 <= 0 || this.mInitialScale * tempScale5 >= this.minScale) {
                deltaX4 = deltaX3;
            } else {
                changeSurfaceReboundState();
                int deltaXTempMin2 = (int) (((float) width) * (this.mInitialScale - this.minScale));
                this.dynamicDelta += dampchange(width, x - this.previousFrameX, deltaX3 - deltaXTempMin2);
                int deltaX7 = (int) (((float) deltaXTempMin2) + this.dynamicDelta);
                float f3 = this.mInitialScale;
                tempScale5 = 1.0f - (((float) deltaX7) / (((float) width) * f3));
                this.mStartAnimationBounds.set(left + deltaX7, top, left + deltaX7 + ((int) (((float) width) * tempScale5 * f3)), ((int) (((float) height) * tempScale5 * f3)) + top);
                this.mStopAnimationBounds.set(this.mStartAnimationBounds.right - ((int) (((float) width) * this.minScale)), top, this.mStartAnimationBounds.right, ((int) (((float) height) * this.minScale)) + top);
                deltaX4 = deltaX7;
            }
            left += deltaX4;
            right = left + width;
            this.mCurScale = tempScale5;
            this.mTask.updateHwFreeFormScaleLeash(left, top, this.mCurScale);
        } else if (i == 2) {
            float tempScale6 = (((float) deltaX5) / (((float) width) * this.mInitialScale)) + 1.0f;
            isReachedThreshold(tempScale6);
            float f4 = this.mInitialScale;
            if (f4 * tempScale6 <= this.maxScale && f4 * tempScale6 >= this.minScale) {
                this.preIsDampState = false;
            }
            if (deltaX5 > 0 && this.mInitialScale * tempScale6 > this.maxScale) {
                changeSurfaceReboundState();
                int deltaXTempMin3 = -((int) (((float) width) * (this.mInitialScale - this.maxScale)));
                this.dynamicDelta += dampchange(width, x - this.previousFrameX, deltaX5 - deltaXTempMin3);
                deltaX5 = (int) (((float) deltaXTempMin3) + this.dynamicDelta);
                float f5 = this.mInitialScale;
                tempScale6 = (((float) deltaX5) / (((float) width) * f5)) + 1.0f;
                this.mStartAnimationBounds.set(left, top, ((int) (((float) width) * tempScale6 * f5)) + left, ((int) (((float) height) * tempScale6 * f5)) + top);
                Rect rect = this.mStopAnimationBounds;
                float f6 = this.maxScale;
                rect.set(left, top, ((int) (((float) width) * f6)) + left, ((int) (((float) height) * f6)) + top);
            }
            if (deltaX5 < 0 && this.mInitialScale * tempScale6 < this.minScale) {
                changeSurfaceReboundState();
                int deltaXTempMin4 = -((int) (((float) width) * (this.mInitialScale - this.minScale)));
                this.dynamicDelta += dampchange(width, x - this.previousFrameX, deltaX5 - deltaXTempMin4);
                float f7 = this.mInitialScale;
                tempScale6 = (((float) ((int) (((float) deltaXTempMin4) + this.dynamicDelta))) / (((float) width) * f7)) + 1.0f;
                this.mStartAnimationBounds.set(left, top, ((int) (((float) width) * tempScale6 * f7)) + left, ((int) (((float) height) * tempScale6 * f7)) + top);
                Rect rect2 = this.mStopAnimationBounds;
                float f8 = this.minScale;
                rect2.set(left, top, ((int) (((float) width) * f8)) + left, ((int) (((float) height) * f8)) + top);
            }
            this.mCurScale = tempScale6;
            this.mTask.updateHwFreeFormScaleLeash(left, top, this.mCurScale);
            right = right2;
        } else if (i == CTRL_BOTTOM) {
            float tempScale7 = (((float) deltaY5) / (((float) height) * this.mInitialScale)) + 1.0f;
            isReachedThreshold(tempScale7);
            float f9 = this.mInitialScale;
            if (f9 * tempScale7 <= this.maxScale && f9 * tempScale7 >= this.minScale) {
                this.preIsDampState = false;
            }
            if (deltaY5 <= 0 || this.mInitialScale * tempScale7 <= this.maxScale) {
                deltaY3 = deltaY5;
                tempScale3 = tempScale7;
            } else {
                changeSurfaceReboundState();
                int deltaYTempMin = -((int) (((float) height) * (this.mInitialScale - this.maxScale)));
                this.dynamicDelta += dampchange(height, y - this.previousFrameY, deltaY5 - deltaYTempMin);
                int deltaY6 = (int) (((float) deltaYTempMin) + this.dynamicDelta);
                float f10 = this.mInitialScale;
                float tempScale8 = (((float) deltaY6) / (((float) height) * f10)) + 1.0f;
                int animationStartWidth = (int) (((float) width) * tempScale8 * f10);
                int animationStartHeight = (int) (((float) height) * tempScale8 * f10);
                int deltaX8 = -(animationStartWidth - (((animationStartHeight - deltaY6) * animationStartWidth) / animationStartHeight));
                deltaY3 = deltaY6;
                this.mStartAnimationBounds.set(left + (deltaX8 / 2), top, left + (deltaX8 / 2) + animationStartWidth, top + animationStartHeight);
                tempScale3 = tempScale8;
                float tempScale9 = this.maxScale;
                this.mStopAnimationBounds.set(((this.mStartAnimationBounds.left + this.mStartAnimationBounds.right) / 2) - ((int) ((((float) width) * this.maxScale) / 2.0f)), top, ((this.mStartAnimationBounds.left + this.mStartAnimationBounds.right) / 2) + ((int) ((((float) width) * tempScale9) / 2.0f)), ((int) (((float) height) * tempScale9)) + top);
                deltaX5 = deltaX8;
            }
            if (deltaY3 >= 0 || this.mInitialScale * tempScale3 >= this.minScale) {
                deltaX2 = deltaX5;
                deltaY4 = deltaY3;
                tempScale4 = tempScale3;
            } else {
                changeSurfaceReboundState();
                int deltaYTempMin2 = -((int) (((float) height) * (this.mInitialScale - this.minScale)));
                this.dynamicDelta += dampchange(height, y - this.previousFrameY, deltaY3 - deltaYTempMin2);
                int deltaY7 = (int) (((float) deltaYTempMin2) + this.dynamicDelta);
                float f11 = this.mInitialScale;
                float tempScale10 = (((float) deltaY7) / (((float) height) * f11)) + 1.0f;
                int animationStartWidth2 = (int) (((float) width) * tempScale10 * f11);
                int animationStartHeight2 = (int) (((float) height) * tempScale10 * f11);
                int deltaX9 = animationStartWidth2 - (((animationStartHeight2 + deltaY7) * animationStartWidth2) / animationStartHeight2);
                deltaX2 = deltaX9;
                this.mStartAnimationBounds.set((deltaX9 / 2) + left, top, (deltaX9 / 2) + left + animationStartWidth2, top + animationStartHeight2);
                float f12 = this.minScale;
                this.mStopAnimationBounds.set(((this.mStartAnimationBounds.left + this.mStartAnimationBounds.right) / 2) - ((int) ((((float) width) * this.minScale) / 2.0f)), top, ((this.mStartAnimationBounds.left + this.mStartAnimationBounds.right) / 2) + ((int) ((((float) width) * f12) / 2.0f)), ((int) (((float) height) * f12)) + top);
                deltaY4 = deltaY7;
                tempScale4 = tempScale10;
            }
            left -= (deltaY4 * width) / (height * 2);
            right = left + width;
            this.mCurScale = tempScale4;
            this.mTask.updateHwFreeFormScaleLeash(left, top, this.mCurScale);
        } else if ((i & CTRL_BOTTOM) == 0 || (2 & i) == 0) {
            int i2 = this.mCtrlType;
            if ((i2 & CTRL_BOTTOM) == 0 || (i2 & 1) == 0) {
                right = right2;
            } else {
                float tempScale11 = (((float) deltaY5) / (((float) height) * this.mInitialScale)) + 1.0f;
                isReachedThreshold(tempScale11);
                float f13 = this.mInitialScale;
                if (f13 * tempScale11 <= this.maxScale && f13 * tempScale11 >= this.minScale) {
                    this.preIsDampState = false;
                }
                if (deltaY5 <= 0 || this.mInitialScale * tempScale11 <= this.maxScale) {
                    deltaY = deltaY5;
                    tempScale = tempScale11;
                } else {
                    changeSurfaceReboundState();
                    int deltaYTempMin3 = -((int) (((float) height) * (this.mInitialScale - this.maxScale)));
                    this.dynamicDelta += dampchange(height, y - this.previousFrameY, deltaY5 - deltaYTempMin3);
                    int deltaY8 = (int) (((float) deltaYTempMin3) + this.dynamicDelta);
                    float f14 = this.mInitialScale;
                    float tempScale12 = (((float) deltaY8) / (((float) height) * f14)) + 1.0f;
                    int animationStartWidth3 = (int) (((float) width) * tempScale12 * f14);
                    int animationStartHeight3 = (int) (((float) height) * tempScale12 * f14);
                    int deltaX10 = -(animationStartWidth3 - (((animationStartHeight3 - deltaY8) * animationStartWidth3) / animationStartHeight3));
                    deltaY = deltaY8;
                    this.mStartAnimationBounds.set(left + deltaX10, top, left + deltaX10 + animationStartWidth3, top + animationStartHeight3);
                    tempScale = tempScale12;
                    this.mStopAnimationBounds.set(this.mStartAnimationBounds.right - ((int) (((float) width) * this.maxScale)), top, this.mStartAnimationBounds.right, ((int) (((float) height) * this.maxScale)) + top);
                    deltaX5 = deltaX10;
                }
                if (deltaY >= 0 || this.mInitialScale * tempScale >= this.minScale) {
                    deltaX = deltaX5;
                    deltaY2 = deltaY;
                    tempScale2 = tempScale;
                } else {
                    changeSurfaceReboundState();
                    int deltaYTempMin4 = -((int) (((float) height) * (this.mInitialScale - this.minScale)));
                    this.dynamicDelta += dampchange(height, y - this.previousFrameY, deltaY - deltaYTempMin4);
                    int deltaY9 = (int) (((float) deltaYTempMin4) + this.dynamicDelta);
                    float f15 = this.mInitialScale;
                    float tempScale13 = (((float) deltaY9) / (((float) height) * f15)) + 1.0f;
                    int animationStartWidth4 = (int) (((float) width) * tempScale13 * f15);
                    int animationStartHeight4 = (int) (((float) height) * tempScale13 * f15);
                    int deltaX11 = animationStartWidth4 - (((animationStartHeight4 + deltaY9) * animationStartWidth4) / animationStartHeight4);
                    deltaX = deltaX11;
                    this.mStartAnimationBounds.set(left + deltaX11, top, left + deltaX11 + animationStartWidth4, top + animationStartHeight4);
                    this.mStopAnimationBounds.set(this.mStartAnimationBounds.right - ((int) (((float) width) * this.minScale)), top, this.mStartAnimationBounds.right, ((int) (((float) height) * this.minScale)) + top);
                    deltaY2 = deltaY9;
                    tempScale2 = tempScale13;
                }
                left -= (deltaY2 * width) / height;
                right = left + width;
                this.mCurScale = tempScale2;
                this.mTask.updateHwFreeFormScaleLeash(left, top, this.mCurScale);
            }
        } else {
            float tempScale14 = (((float) deltaY5) / (((float) height) * this.mInitialScale)) + 1.0f;
            isReachedThreshold(tempScale14);
            float f16 = this.mInitialScale;
            if (f16 * tempScale14 <= this.maxScale && f16 * tempScale14 >= this.minScale) {
                this.preIsDampState = false;
            }
            if (deltaY5 > 0 && this.mInitialScale * tempScale14 > this.maxScale) {
                changeSurfaceReboundState();
                int deltaYTempMin5 = -((int) (((float) height) * (this.mInitialScale - this.maxScale)));
                this.dynamicDelta += dampchange(height, y - this.previousFrameY, deltaY5 - deltaYTempMin5);
                deltaY5 = (int) (((float) deltaYTempMin5) + this.dynamicDelta);
                float f17 = this.mInitialScale;
                tempScale14 = (((float) deltaY5) / (((float) height) * f17)) + 1.0f;
                this.mStartAnimationBounds.set(left, top, ((int) (((float) width) * tempScale14 * f17)) + left, ((int) (((float) height) * tempScale14 * f17)) + top);
                Rect rect3 = this.mStopAnimationBounds;
                float f18 = this.maxScale;
                rect3.set(left, top, ((int) (((float) width) * f18)) + left, ((int) (((float) height) * f18)) + top);
            }
            if (deltaY5 < 0 && this.mInitialScale * tempScale14 < this.minScale) {
                changeSurfaceReboundState();
                int deltaYTempMin6 = -((int) (((float) height) * (this.mInitialScale - this.minScale)));
                this.dynamicDelta += dampchange(height, y - this.previousFrameY, deltaY5 - deltaYTempMin6);
                float f19 = this.mInitialScale;
                tempScale14 = (((float) ((int) (((float) deltaYTempMin6) + this.dynamicDelta))) / (((float) height) * f19)) + 1.0f;
                this.mStartAnimationBounds.set(left, top, ((int) (((float) width) * tempScale14 * f19)) + left, ((int) (((float) height) * tempScale14 * f19)) + top);
                Rect rect4 = this.mStopAnimationBounds;
                float f20 = this.minScale;
                rect4.set(left, top, ((int) (((float) width) * f20)) + left, ((int) (((float) height) * f20)) + top);
            }
            this.mCurScale = tempScale14;
            this.mTask.updateHwFreeFormScaleLeash(left, top, this.mCurScale);
            right = right2;
        }
        this.previousFrameX = x;
        this.previousFrameY = y;
        this.mWindowDragBounds.set(left, top, right, bottom);
    }

    private float dampchange(int maxDelta, float curDelta, int delta) {
        return new DynamicCurveRate((float) maxDelta, this.dampingCoefficient).getRate((float) Math.abs(delta)) * curDelta;
    }

    private void changeSurfaceReboundState() {
        if (!this.preIsDampState) {
            this.dynamicDelta = 0.0f;
        }
        this.preIsDampState = true;
    }

    private void isReachedThreshold(float tempScale) {
        float f = this.maxScale;
        float f2 = this.minScale;
        if (f - f2 < FLOAT_CMP_EQ_TH) {
            if (this.mVibratorFlag == 0) {
                HwVibrator.setHwVibrator(Process.myUid(), this.mService.mContext.getPackageName(), "haptic.common.threshold");
                this.mVibratorFlag = -1;
            }
        } else if (this.mVibratorFlag != -1 && (this.mInitialScale * tempScale) - f2 < FLOAT_CMP_EQ_TH) {
            HwVibrator.setHwVibrator(Process.myUid(), this.mService.mContext.getPackageName(), "haptic.common.threshold");
            this.mVibratorFlag = -1;
        } else if (this.mVibratorFlag == 1 || this.maxScale - (this.mInitialScale * tempScale) >= FLOAT_CMP_EQ_TH) {
            float f3 = this.mInitialScale;
            if ((f3 * tempScale) - this.minScale > FLOAT_CMP_EQ_TH && this.maxScale - (f3 * tempScale) > FLOAT_CMP_EQ_TH) {
                this.mVibratorFlag = 0;
            }
        } else {
            HwVibrator.setHwVibrator(Process.myUid(), this.mService.mContext.getPackageName(), "haptic.common.threshold");
            this.mVibratorFlag = 1;
        }
    }

    private void allowFreeformDragPos(Rect outBounds) {
        DisplayContent displayContent;
        this.mDisplayContent.getStableRect(outBounds);
        if (this.mService.mAtmService.mHwATMSEx.isPhoneLandscape(this.mDisplayContent) && (displayContent = this.mDisplayContent) != null && displayContent.getDisplayPolicy() != null) {
            int i = 0;
            outBounds.top = 0;
            int i2 = outBounds.top;
            if (this.mHasSideinScreen) {
                i = this.mSafeSideWidth;
            }
            outBounds.top = i2 + i;
            if (this.mService.mAtmService.mHwATMSEx.isStatusBarPermenantlyShowing()) {
                outBounds.top += this.mStatusBarHeight;
            } else {
                outBounds.top += this.mStatusBarHeight / 2;
            }
        }
    }

    /* JADX INFO: Multiple debug info for r1v13 'tmpLandscapeMaxLength'  int: [D('tmpLandscapeMaxLength' int), D('width' int)] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x0277, code lost:
        if (r5 != r1) goto L_0x027e;
     */
    @VisibleForTesting
    public void resizeDrag(float x, float y) {
        char c;
        int width;
        int width2;
        int height;
        int width3;
        int width1;
        int height1;
        int width22;
        int height2;
        int deltaX = Math.round(x - this.mStartDragX);
        int deltaY = Math.round(y - this.mStartDragY);
        int left = this.mWindowOriginalBounds.left;
        int top = this.mWindowOriginalBounds.top;
        int right = this.mWindowOriginalBounds.right;
        int bottom = this.mWindowOriginalBounds.bottom;
        if (!this.mPreserveOrientation) {
            c = 0;
        } else {
            c = this.mStartOrientationWasLandscape ? (char) 39322 : 21845;
        }
        int width4 = right - left;
        int height3 = bottom - top;
        int i = this.mCtrlType;
        if ((i & 1) != 0) {
            width4 = Math.max(this.mMinVisibleWidth, width4 - deltaX);
        } else if ((i & 2) != 0) {
            width4 = Math.max(this.mMinVisibleWidth, width4 + deltaX);
        }
        int i2 = this.mCtrlType;
        if ((i2 & 4) != 0) {
            height3 = Math.max(this.mMinVisibleHeight, height3 - deltaY);
        } else if ((i2 & CTRL_BOTTOM) != 0) {
            height3 = Math.max(this.mMinVisibleHeight, height3 + deltaY);
        }
        float aspect = ((float) width4) / ((float) height3);
        if (!this.mPreserveOrientation || ((!this.mStartOrientationWasLandscape || aspect >= MIN_ASPECT) && (this.mStartOrientationWasLandscape || ((double) aspect) <= 0.8333333002196431d))) {
            width = width4;
        } else {
            if (this.mStartOrientationWasLandscape) {
                int width12 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, width4));
                height1 = Math.min(height3, Math.round(((float) width12) / MIN_ASPECT));
                if (height1 < this.mMinVisibleHeight) {
                    height1 = this.mMinVisibleHeight;
                    width1 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, Math.round(((float) height1) * MIN_ASPECT)));
                } else {
                    width1 = width12;
                }
                height2 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, height3));
                width22 = Math.max(width4, Math.round(((float) height2) * MIN_ASPECT));
                if (width22 < this.mMinVisibleWidth) {
                    width22 = this.mMinVisibleWidth;
                    height2 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, Math.round(((float) width22) / MIN_ASPECT)));
                }
            } else {
                int width13 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, width4));
                int height12 = Math.max(height3, Math.round(((float) width13) * MIN_ASPECT));
                if (height12 < this.mMinVisibleHeight) {
                    int height13 = this.mMinVisibleHeight;
                    width1 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, Math.round(((float) height13) / MIN_ASPECT)));
                    height1 = height13;
                } else {
                    width1 = width13;
                    height1 = height12;
                }
                height2 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, height3));
                width22 = Math.min(width4, Math.round(((float) height2) / MIN_ASPECT));
                if (width22 < this.mMinVisibleWidth) {
                    width22 = this.mMinVisibleWidth;
                    height2 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, Math.round(((float) width22) * MIN_ASPECT)));
                }
            }
            if ((width4 > right - left || height3 > bottom - top) == (width1 * height1 > width22 * height2)) {
                width = width1;
                height3 = height1;
            } else {
                width = width22;
                height3 = height2;
            }
        }
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(this.mTask.getDisplayContent().getDisplayId())) {
            width = limitPCWindowSize(width, 1);
            height3 = limitPCWindowSize(height3, 2);
        }
        if (!HwFreeFormUtils.isFreeFormEnable() || this.mTask.inHwFreeFormWindowingMode()) {
            width2 = width;
            height = height3;
        } else {
            boolean isLandscape = false;
            DisplayContent displayContent = this.mTask.getDisplayContent();
            if (displayContent != null) {
                if (HwFreeFormUtils.getFreeformMaxLength() == 0 && displayContent.getDisplay() != null) {
                    Point maxDisplaySize = new Point();
                    displayContent.getDisplay().getRealSize(maxDisplaySize);
                    HwFreeFormUtils.computeFreeFormSize(maxDisplaySize);
                }
                Rect displayBounds = displayContent.mAcitvityDisplay.getBounds();
                WindowManagerService windowManagerService = this.mService;
                isLandscape = !WindowManagerService.IS_TABLET && displayBounds.width() > displayBounds.height();
            }
            int statusBarHeight = this.mService.mAtmService.mUiContext.getResources().getDimensionPixelSize(17105445);
            int tmpFreeformMaxLength = HwFreeFormUtils.getFreeformMaxLength();
            int tmpFreeformMinLength = HwFreeFormUtils.getFreeformMinLength();
            int tmpLandscapeMaxLength = isLandscape ? HwFreeFormUtils.getLandscapeFreeformMaxLength() - statusBarHeight : tmpFreeformMaxLength;
            int tmpHeight = tmpLandscapeMaxLength > height3 ? height3 : tmpLandscapeMaxLength;
            int height4 = tmpHeight > tmpFreeformMinLength ? tmpHeight : tmpFreeformMinLength;
            int tmpWidth = tmpFreeformMaxLength > width ? width : tmpFreeformMaxLength;
            int width5 = tmpWidth > tmpFreeformMinLength ? tmpWidth : tmpFreeformMinLength;
            if (tmpLandscapeMaxLength == height4) {
                width3 = width5;
            } else {
                width3 = width5;
            }
            if (!(height4 == tmpFreeformMinLength && width3 == tmpFreeformMinLength)) {
                height = height4;
                if (this.mIsOutBound) {
                    updateFreeFormOutLine(4);
                    this.mIsOutBound = false;
                }
                width2 = width3;
            }
            height = height4;
            if (!this.mIsOutBound) {
                updateFreeFormOutLine(3);
                this.mIsOutBound = true;
            }
            width2 = width3;
        }
        updateDraggedBounds(left, top, right, bottom, width2, height);
    }

    /* access modifiers changed from: package-private */
    public void updateDraggedBounds(int left, int top, int right, int bottom, int newWidth, int newHeight) {
        if ((this.mCtrlType & 1) != 0) {
            left = right - newWidth;
        } else {
            right = left + newWidth;
        }
        if ((this.mCtrlType & 4) != 0) {
            top = bottom - newHeight;
        } else {
            bottom = top + newHeight;
        }
        this.mWindowDragBounds.set(left, top, right, bottom);
        checkBoundsForOrientationViolations(this.mWindowDragBounds);
    }

    private void checkBoundsForOrientationViolations(Rect bounds) {
    }

    private void relocateOffScreenHwFreeformWindow() {
        Rect tmpRect;
        if (this.mTask.inHwFreeFormWindowingMode() && this.mTask.mStack != null) {
            Rect originalWindowBounds = new Rect();
            if (!isImeVisible() || this.mService.mAtmService.mHwATMSEx.isPhoneLandscape(this.mDisplayContent)) {
                originalWindowBounds = new Rect(this.mWindowDragBounds);
                this.mTmpRect = new Rect(originalWindowBounds);
                tmpRect = this.mTmpRect;
            } else {
                this.mTask.getBounds(originalWindowBounds);
                tmpRect = new Rect(originalWindowBounds);
            }
            ActivityStack stack = this.mTask.mStack.mActivityStack;
            if (stack != null) {
                int left = tmpRect.left;
                int top = tmpRect.top;
                tmpRect.scale(this.mTask.mStack.mHwStackScale * reboundScaleSelect(this.mCurScale));
                tmpRect.offsetTo(left, top);
                int originalLeft = originalWindowBounds.left;
                int originalTop = originalWindowBounds.top;
                originalWindowBounds.scale(this.mTask.mStack.mHwStackScale * reboundScaleSelect(this.mCurScale));
                originalWindowBounds.offsetTo(originalLeft, originalTop);
                Rect validHwFreeformWindowBounds = relocateOffScreenWindow(tmpRect, stack, reboundScaleSelect(this.mCurScale));
                if (!validHwFreeformWindowBounds.equals(originalWindowBounds)) {
                    if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
                        Slog.w(TAG, "relocateOffScreenHwFreeformWindow to new bounds {" + validHwFreeformWindowBounds + "}");
                    }
                    this.reboundIsOutOfScreen = true;
                    int validLeft = tmpRect.left;
                    int validTop = tmpRect.top;
                    validHwFreeformWindowBounds.scale(1.0f / (this.mTask.mStack.mHwStackScale * reboundScaleSelect(this.mCurScale)));
                    validHwFreeformWindowBounds.offsetTo(validLeft, validTop);
                    this.mWindowDragBounds.set(validHwFreeformWindowBounds);
                    setOutofScreenStopBounds(validHwFreeformWindowBounds);
                    stack.mIsInDefaultPos = false;
                }
            }
        }
    }

    private void setOutofScreenStopBounds(Rect validHwFreeformWindowBounds) {
        float f = this.mCurScale;
        float f2 = this.mInitialScale;
        if (f * f2 > this.maxScale || f * f2 < this.minScale) {
            float f3 = this.mCurScale;
            float f4 = this.mInitialScale;
            if (f3 * f4 > this.maxScale) {
                this.mStopAnimationBounds.set(validHwFreeformWindowBounds.left, validHwFreeformWindowBounds.top, validHwFreeformWindowBounds.left + ((int) (((float) validHwFreeformWindowBounds.width()) * this.maxScale)), validHwFreeformWindowBounds.top + ((int) (((float) validHwFreeformWindowBounds.height()) * this.maxScale)));
            } else if (f3 * f4 < this.minScale) {
                this.mStopAnimationBounds.set(validHwFreeformWindowBounds.left, validHwFreeformWindowBounds.top, validHwFreeformWindowBounds.left + ((int) (((float) validHwFreeformWindowBounds.width()) * this.minScale)), validHwFreeformWindowBounds.top + ((int) (((float) validHwFreeformWindowBounds.height()) * this.minScale)));
            }
        } else {
            this.mStopAnimationBounds.set(validHwFreeformWindowBounds.left, validHwFreeformWindowBounds.top, validHwFreeformWindowBounds.left + ((int) (((float) validHwFreeformWindowBounds.width()) * this.mInitialScale)), validHwFreeformWindowBounds.top + ((int) (((float) validHwFreeformWindowBounds.height()) * this.mInitialScale)));
        }
    }

    private float reboundScaleSelect(float curScale) {
        float f = this.mInitialScale;
        float f2 = this.maxScale;
        if (curScale * f > f2) {
            return f2 / f;
        }
        float f3 = this.minScale;
        if (curScale * f < f3) {
            return f3 / f;
        }
        return curScale;
    }

    private void updateHwFreeformWindowDragBounds(int x, int y, Rect stackBounds) {
        if (this.mTask.mStack != null) {
            int offsetX = Math.round(((float) x) - this.mStartDragX);
            int offsetY = Math.round(((float) y) - this.mStartDragY);
            float freeFormInitialScale = this.mTask.mStack.mHwStackScale;
            int freeFormDragbarWidth = Math.round(((float) this.mDragbarWidth) * freeFormInitialScale);
            int freeFormDragbarHeight = Math.round(((float) this.mDragbarHeight) * freeFormInitialScale);
            int freeFormCaptionBarHeight = Math.round(((float) this.mHwFreeformCaptionBarHeight) * freeFormInitialScale);
            int captionSpareWidth = (Math.round(((float) this.mWindowOriginalBounds.width()) * freeFormInitialScale) - freeFormDragbarWidth) / 2;
            int captionSpareHeight = (freeFormCaptionBarHeight - freeFormDragbarHeight) / 2;
            int maxLeft = (stackBounds.right - captionSpareWidth) - (freeFormDragbarWidth / 2);
            int minLeft = (stackBounds.left - captionSpareWidth) - (freeFormDragbarWidth / 2);
            int minTop = stackBounds.top;
            int maxTop = (stackBounds.bottom - captionSpareHeight) - freeFormDragbarHeight;
            if (!isImeVisible() || this.mService.mAtmService.mHwATMSEx.isPhoneLandscape(this.mDisplayContent)) {
                this.mWindowDragBounds.offsetTo(Math.min(Math.max(this.mWindowOriginalBounds.left + offsetX, minLeft), maxLeft), Math.min(Math.max(this.mWindowOriginalBounds.top + offsetY, minTop), maxTop));
            } else {
                this.mWindowDragBounds.offsetTo(Math.min(Math.max(this.mWindowOriginalBounds.left + offsetX, minLeft), maxLeft), this.mWindowOriginalBounds.top);
                updateVerticallyBoundsOnImeExist((y - freeFormDragbarHeight) - captionSpareHeight, minTop, freeFormInitialScale);
            }
            if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
                Slog.d(TAG, "updateHwFreeformWindowDragBounds: " + this.mWindowDragBounds);
            }
        }
    }

    private void updateVerticallyBoundsOnImeExist(int yPosition, int minTop, float freeFormInitialScale) {
        int imeTop = getInputMethodTop();
        int windowTop = this.mContainingBounds.top;
        int windowBottom = Math.round(((float) this.mContainingBounds.height()) * freeFormInitialScale) + windowTop;
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d(TAG, "updateVerticallyBoundsOnImeExist mWindowDragBounds: " + this.mWindowDragBounds + " mContainingBounds:" + this.mContainingBounds + " mWindowOriginalBounds:" + this.mWindowOriginalBounds + " freeFormInitialScale:" + freeFormInitialScale + " imeTop:" + imeTop);
        }
        if (windowTop < minTop || windowBottom - imeTop >= 2) {
            Slog.w(TAG, "updateVerticallyBoundsOnImeExist windowTop:" + windowTop + " minTop:" + minTop + " windowBottom" + windowBottom + " imeTop:" + imeTop);
        } else if (windowTop != minTop || imeTop - windowBottom > 2) {
            Rect rect = this.mWindowDragBounds;
            rect.offsetTo(rect.left, Math.min(Math.max(yPosition, minTop), (imeTop - windowBottom) + windowTop));
        }
    }

    private int getInputMethodTop() {
        WindowState imeWin = this.mService.mRoot.getCurrentInputMethodWindow();
        if (imeWin == null) {
            return 0;
        }
        return (imeWin.getDisplayFrameLw().top > imeWin.getContentFrameLw().top ? imeWin.getDisplayFrameLw() : imeWin.getContentFrameLw()).top + imeWin.getGivenContentInsetsLw().top;
    }

    private boolean isImeVisible() {
        WindowState imeWin = this.mService.mRoot.getCurrentInputMethodWindow();
        return imeWin != null && imeWin.isVisibleNow();
    }

    private void updateWindowDragBounds(int x, int y, Rect stackBounds) {
        int offsetX = Math.round(((float) x) - this.mStartDragX);
        int offsetY = Math.round(((float) y) - this.mStartDragY);
        this.mWindowDragBounds.set(this.mWindowOriginalBounds);
        int maxLeft = stackBounds.right - this.mMinVisibleWidth;
        int minLeft = (stackBounds.left + this.mMinVisibleWidth) - this.mWindowOriginalBounds.width();
        int minTop = stackBounds.top;
        this.mWindowDragBounds.offsetTo(Math.min(Math.max(this.mWindowOriginalBounds.left + offsetX, minLeft), maxLeft), Math.min(Math.max(this.mWindowOriginalBounds.top + offsetY, minTop), stackBounds.bottom - this.mMinVisibleHeight));
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d(TAG, "updateWindowDragBounds: " + this.mWindowDragBounds);
        }
    }

    public String toShortString() {
        return TAG;
    }

    static void setFactory(Factory factory) {
        sFactory = factory;
    }

    static TaskPositioner create(WindowManagerService service) {
        if (sFactory == null) {
            sFactory = new Factory() {
                /* class com.android.server.wm.TaskPositioner.AnonymousClass2 */
            };
        }
        return sFactory.create(service);
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        this.mService.mTaskPositioningController.finishTaskPositioning();
    }

    /* access modifiers changed from: package-private */
    public interface Factory {
        default TaskPositioner create(WindowManagerService service) {
            return new TaskPositioner(service);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFreeFormOutLine(int color) {
        this.mHwTPEx.updateFreeFormOutLine(color);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processPCWindowDragHitHotArea(TaskRecord taskRecord, float newX, float newY) {
        this.mHwTPEx.processPCWindowDragHitHotArea(taskRecord, newX, newY);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processPCWindowFinishDragHitHotArea(TaskRecord taskRecord, float newX, float newY) {
        this.mHwTPEx.processPCWindowFinishDragHitHotArea(taskRecord, newX, newY);
    }

    private int limitPCWindowSize(int legnth, int limitType) {
        return this.mHwTPEx.limitPCWindowSize(legnth, limitType);
    }

    private Rect relocateOffScreenWindow(Rect originalWindowBounds, ActivityStack stack, float scale) {
        return this.mHwATMSEx.relocateOffScreenWindow(originalWindowBounds, stack, scale);
    }
}
