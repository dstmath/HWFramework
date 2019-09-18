package com.android.server.wm;

import android.freeform.HwFreeFormUtils;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
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
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.MotionEvent;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.HwServiceExFactory;
import com.android.server.input.InputApplicationHandle;
import com.android.server.input.InputWindowHandle;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

class TaskPositioner {
    private static final int CTRL_BOTTOM = 8;
    private static final int CTRL_LEFT = 1;
    private static final int CTRL_NONE = 0;
    private static final int CTRL_RIGHT = 2;
    private static final int CTRL_TOP = 4;
    private static final boolean DEBUG_ORIENTATION_VIOLATIONS = false;
    private static final int DISPOSE_MSG = 0;
    @VisibleForTesting
    static final float MIN_ASPECT = 1.2f;
    public static final float RESIZING_HINT_ALPHA = 0.5f;
    public static final int RESIZING_HINT_DURATION_MS = 0;
    static final int SIDE_MARGIN_DIP = 100;
    private static final String TAG = "WindowManager";
    private static final String TAG_LOCAL = "TaskPositioner";
    private static Factory sFactory;
    Handler PCHandler = null;
    InputChannel mClientChannel;
    private int mCtrlType = 0;
    private int mDelta = 0;
    private Display mDisplay;
    private final DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    InputApplicationHandle mDragApplicationHandle;
    /* access modifiers changed from: private */
    public boolean mDragEnded = false;
    InputWindowHandle mDragWindowHandle;
    IHwTaskPositionerEx mHwTPEx = null;
    /* access modifiers changed from: private */
    public WindowPositionerEventReceiver mInputEventReceiver;
    private boolean mIsOutBound = false;
    /* access modifiers changed from: private */
    public boolean mIsTouching = false;
    private final Point mMaxVisibleSize = new Point();
    private int mMinVisibleHeight;
    private int mMinVisibleWidth;
    private boolean mPreserveOrientation;
    /* access modifiers changed from: private */
    public boolean mResizing;
    InputChannel mServerChannel;
    /* access modifiers changed from: private */
    public final WindowManagerService mService;
    private int mSideMargin;
    private float mStartDragX;
    private float mStartDragY;
    private boolean mStartOrientationWasLandscape;
    /* access modifiers changed from: private */
    public Task mTask;
    /* access modifiers changed from: private */
    public Rect mTmpRect = new Rect();
    /* access modifiers changed from: private */
    public final Rect mWindowDragBounds = new Rect();
    /* access modifiers changed from: private */
    public final Rect mWindowOriginalBounds = new Rect();

    @Retention(RetentionPolicy.SOURCE)
    @interface CtrlType {
    }

    interface Factory {
        TaskPositioner create(WindowManagerService service) {
            return new TaskPositioner(service);
        }
    }

    private final class WindowPositionerEventReceiver extends BatchedInputEventReceiver {
        public WindowPositionerEventReceiver(InputChannel inputChannel, Looper looper, Choreographer choreographer) {
            super(inputChannel, looper, choreographer);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* JADX WARNING: Removed duplicated region for block: B:41:0x00c1 A[Catch:{ all -> 0x01dc, all -> 0x009c, Exception -> 0x01e6 }] */
        public void onInputEvent(InputEvent event, int displayId) {
            if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0) {
                MotionEvent motionEvent = (MotionEvent) event;
                boolean handled = false;
                if (TaskPositioner.this.mDragEnded) {
                    finishInputEvent(event, true);
                    return;
                }
                float newX = motionEvent.getRawX();
                float newY = motionEvent.getRawY();
                switch (motionEvent.getAction()) {
                    case 0:
                        break;
                    case 1:
                        if (HwFreeFormUtils.isFreeFormEnable()) {
                            boolean unused = TaskPositioner.this.mIsTouching = false;
                            TaskPositioner.this.updateFreeFormOutLine(2);
                        }
                        boolean unused2 = TaskPositioner.this.mDragEnded = true;
                    case 2:
                        synchronized (TaskPositioner.this.mService.mWindowMap) {
                            try {
                                WindowManagerService.boostPriorityForLockedSection();
                                boolean unused3 = TaskPositioner.this.mDragEnded = TaskPositioner.this.notifyMoveLocked(newX, newY);
                                TaskPositioner.this.mTask.getDimBounds(TaskPositioner.this.mTmpRect);
                            } catch (Exception e) {
                                try {
                                    Slog.e(TaskPositioner.TAG, "Exception caught by drag handleMotion", e);
                                    break;
                                } catch (Throwable th) {
                                    finishInputEvent(event, false);
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                while (true) {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th2;
                                    break;
                                }
                            }
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        if (!TaskPositioner.this.mTmpRect.equals(TaskPositioner.this.mWindowDragBounds)) {
                            Trace.traceBegin(32, "wm.TaskPositioner.resizeTask");
                            try {
                                TaskPositioner.this.mService.mActivityManager.resizeTask(TaskPositioner.this.mTask.mTaskId, TaskPositioner.this.mWindowDragBounds, 1);
                            } catch (RemoteException e2) {
                            }
                            Trace.traceEnd(32);
                        }
                    case 3:
                        boolean unused4 = TaskPositioner.this.mDragEnded = true;
                        if (TaskPositioner.this.mDragEnded) {
                            boolean wasResizing = TaskPositioner.this.mResizing;
                            synchronized (TaskPositioner.this.mService.mWindowMap) {
                                WindowManagerService.boostPriorityForLockedSection();
                                TaskPositioner.this.endDragLocked();
                                TaskPositioner.this.mTask.getDimBounds(TaskPositioner.this.mTmpRect);
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            try {
                                if (HwFreeFormUtils.isFreeFormEnable() && !(TaskPositioner.this.mWindowOriginalBounds.width() == TaskPositioner.this.mWindowDragBounds.width() && TaskPositioner.this.mWindowOriginalBounds.height() == TaskPositioner.this.mWindowDragBounds.height())) {
                                    Rect availableRect = TaskPositioner.this.mTask.getParent().getBounds();
                                    Flog.bdReport(TaskPositioner.this.mService.mContext, 10067, "{ height:" + availableRect.height() + ",width:" + availableRect.width() + ",left:" + TaskPositioner.this.mWindowDragBounds.left + ",top:" + TaskPositioner.this.mWindowDragBounds.top + ",right:" + TaskPositioner.this.mWindowDragBounds.right + ",bottom:" + TaskPositioner.this.mWindowDragBounds.bottom + "}");
                                }
                                if (wasResizing && !TaskPositioner.this.mTmpRect.equals(TaskPositioner.this.mWindowDragBounds)) {
                                    TaskPositioner.this.mService.mActivityManager.resizeTask(TaskPositioner.this.mTask.mTaskId, TaskPositioner.this.mWindowDragBounds, 3);
                                }
                            } catch (RemoteException e3) {
                            }
                            TaskPositioner.this.mService.mTaskPositioningController.finishTaskPositioning();
                        }
                        handled = true;
                        break;
                }
                if (TaskPositioner.this.mDragEnded) {
                }
                handled = true;
                finishInputEvent(event, handled);
            }
        }
    }

    TaskPositioner(WindowManagerService service) {
        this.mService = service;
        this.mHwTPEx = HwServiceExFactory.getHwTaskPositionerEx(service);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Rect getWindowDragBounds() {
        return this.mWindowDragBounds;
    }

    /* access modifiers changed from: package-private */
    public void register(DisplayContent displayContent) {
        Display display = displayContent.getDisplay();
        if (this.mClientChannel != null) {
            Slog.e(TAG, "Task positioner already registered");
            return;
        }
        this.mDisplay = display;
        this.mDisplay.getMetrics(this.mDisplayMetrics);
        InputChannel[] channels = InputChannel.openInputChannelPair(TAG);
        this.mServerChannel = channels[0];
        this.mClientChannel = channels[1];
        this.mService.mInputManager.registerInputChannel(this.mServerChannel, null);
        this.mInputEventReceiver = new WindowPositionerEventReceiver(this.mClientChannel, this.mService.mAnimationHandler.getLooper(), this.mService.mAnimator.getChoreographer());
        this.mDragApplicationHandle = new InputApplicationHandle(null);
        this.mDragApplicationHandle.name = TAG;
        this.mDragApplicationHandle.dispatchingTimeoutNanos = 5000000000L;
        this.mDragWindowHandle = new InputWindowHandle(this.mDragApplicationHandle, null, null, this.mDisplay.getDisplayId());
        this.mDragWindowHandle.name = TAG;
        this.mDragWindowHandle.inputChannel = this.mServerChannel;
        this.mDragWindowHandle.layer = this.mService.getDragLayerLocked();
        this.mDragWindowHandle.layoutParamsFlags = 0;
        this.mDragWindowHandle.layoutParamsType = 2016;
        this.mDragWindowHandle.dispatchingTimeoutNanos = 5000000000L;
        this.mDragWindowHandle.visible = true;
        this.mDragWindowHandle.canReceiveKeys = false;
        this.mDragWindowHandle.hasFocus = true;
        this.mDragWindowHandle.hasWallpaper = false;
        this.mDragWindowHandle.paused = false;
        this.mDragWindowHandle.ownerPid = Process.myPid();
        this.mDragWindowHandle.ownerUid = Process.myUid();
        this.mDragWindowHandle.inputFeatures = 0;
        this.mDragWindowHandle.scaleFactor = 1.0f;
        this.mDragWindowHandle.touchableRegion.setEmpty();
        this.mDragWindowHandle.frameLeft = 0;
        this.mDragWindowHandle.frameTop = 0;
        Point p = new Point();
        this.mDisplay.getRealSize(p);
        this.mDragWindowHandle.frameRight = p.x;
        this.mDragWindowHandle.frameBottom = p.y;
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.d(TAG, "Pausing rotation during re-position");
        }
        this.mService.pauseRotationLocked();
        this.mSideMargin = WindowManagerService.dipToPixel(100, this.mDisplayMetrics);
        this.mMinVisibleWidth = WindowManagerService.dipToPixel(42, this.mDisplayMetrics);
        this.mMinVisibleHeight = WindowManagerService.dipToPixel(36, this.mDisplayMetrics);
        this.mDisplay.getRealSize(this.mMaxVisibleSize);
        HwFreeFormUtils.computeFreeFormSize(this.mMaxVisibleSize);
        this.mDelta = WindowManagerService.dipToPixel(10, this.mDisplayMetrics);
        this.mDragEnded = false;
    }

    /* access modifiers changed from: package-private */
    public void unregister() {
        if (this.mClientChannel == null) {
            Slog.e(TAG, "Task positioner not registered");
            return;
        }
        this.mService.mInputManager.unregisterInputChannel(this.mServerChannel);
        if (!HwPCUtils.enabledInPad() || this.mDisplay == null || !HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(this.mDisplay.getDisplayId())) {
            this.mInputEventReceiver.dispose();
            this.mInputEventReceiver = null;
        } else {
            this.PCHandler = new Handler(this.mService.mAnimationHandler.getLooper()) {
                public void handleMessage(Message msg) {
                    if (msg.what == 0) {
                        TaskPositioner.this.mInputEventReceiver.dispose();
                        WindowPositionerEventReceiver unused = TaskPositioner.this.mInputEventReceiver = null;
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
        this.mDisplay = null;
        this.mDragEnded = true;
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.d(TAG, "Resuming rotation after re-position");
        }
        this.mService.resumeRotationLocked();
    }

    /* access modifiers changed from: package-private */
    public void startDrag(WindowState win, boolean resize, boolean preserveOrientation, float startX, float startY) {
        this.mTask = win.getTask();
        this.mTask.getDimBounds(this.mTmpRect);
        startDrag(resize, preserveOrientation, startX, startY, this.mTmpRect);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void startDrag(boolean resize, boolean preserveOrientation, float startX, float startY, Rect startBounds) {
        boolean z = false;
        this.mCtrlType = 0;
        this.mStartDragX = startX;
        this.mStartDragY = startY;
        this.mPreserveOrientation = preserveOrientation;
        if (resize) {
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
                this.mCtrlType |= 8;
            }
            this.mResizing = this.mCtrlType != 0;
        }
        if (startBounds.width() >= startBounds.height()) {
            z = true;
        }
        this.mStartOrientationWasLandscape = z;
        this.mWindowOriginalBounds.set(startBounds);
        if (this.mResizing) {
            synchronized (this.mService.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    notifyMoveLocked(startX, startY);
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            this.mService.mH.post(new Runnable(startBounds) {
                private final /* synthetic */ Rect f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    TaskPositioner.lambda$startDrag$0(TaskPositioner.this, this.f$1);
                }
            });
        }
        this.mWindowDragBounds.set(startBounds);
    }

    public static /* synthetic */ void lambda$startDrag$0(TaskPositioner taskPositioner, Rect startBounds) {
        try {
            taskPositioner.mService.mActivityManager.resizeTask(taskPositioner.mTask.mTaskId, startBounds, 3);
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: private */
    public void endDragLocked() {
        this.mResizing = false;
        this.mTask.setDragResizing(false, 0);
    }

    /* access modifiers changed from: private */
    public boolean notifyMoveLocked(float x, float y) {
        if (this.mCtrlType != 0) {
            if (!this.mIsTouching) {
                updateFreeFormOutLine(4);
                this.mIsTouching = true;
            }
            resizeDrag(x, y);
            this.mTask.setDragResizing(true, 0);
            return false;
        }
        this.mTask.mStack.getDimBounds(this.mTmpRect);
        if (HwPCUtils.isExtDynamicStack(this.mTask.mStack.mStackId)) {
            if (this.mService == null || this.mService.mHwWMSEx == null || this.mService.mHwWMSEx.getPCScreenDisplayMode() == 0 || this.mDisplay == null) {
                this.mTmpRect.bottom = this.mTask.mStack.getDisplayInfo().appHeight;
            } else {
                float screenScale = this.mService.mHwWMSEx.getPCScreenScale();
                Point displaySize = new Point();
                this.mDisplay.getRealSize(displaySize);
                this.mTmpRect.bottom = (int) ((((float) this.mTask.mStack.getDisplayInfo().appHeight) * screenScale) + ((((float) displaySize.y) * (1.0f - screenScale)) / 2.0f));
            }
        }
        int nX = (int) x;
        int nY = (int) y;
        if (!this.mTmpRect.contains(nX, nY)) {
            nX = Math.min(Math.max(nX, this.mTmpRect.left), this.mTmpRect.right);
            nY = Math.min(Math.max(nY, this.mTmpRect.top), this.mTmpRect.bottom);
        }
        updateWindowDragBounds(nX, nY, this.mTmpRect);
        return false;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void resizeDrag(float x, float y) {
        char c;
        int width;
        int width2;
        int height1;
        int width1;
        int width22;
        int height2;
        int height12;
        int height13;
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
        char c2 = c;
        int width3 = right - left;
        int height = bottom - top;
        if ((this.mCtrlType & 1) != 0) {
            width3 = Math.max(this.mMinVisibleWidth, width3 - deltaX);
        } else if ((this.mCtrlType & 2) != 0) {
            width3 = Math.max(this.mMinVisibleWidth, width3 + deltaX);
        }
        if ((this.mCtrlType & 4) != 0) {
            height = Math.max(this.mMinVisibleHeight, height - deltaY);
        } else if ((this.mCtrlType & 8) != 0) {
            height = Math.max(this.mMinVisibleHeight, height + deltaY);
        }
        float aspect = ((float) width3) / ((float) height);
        if (!this.mPreserveOrientation || ((!this.mStartOrientationWasLandscape || aspect >= MIN_ASPECT) && (this.mStartOrientationWasLandscape || ((double) aspect) <= 0.8333333002196431d))) {
            width = width3;
        } else {
            if (this.mStartOrientationWasLandscape) {
                int width12 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, width3));
                int height14 = Math.min(height, Math.round(((float) width12) / MIN_ASPECT));
                if (height14 < this.mMinVisibleHeight) {
                    height14 = this.mMinVisibleHeight;
                    int i = width12;
                    width12 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, Math.round(((float) height14) * MIN_ASPECT)));
                } else {
                    int i2 = width12;
                }
                int height22 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, height));
                width22 = Math.max(width3, Math.round(((float) height22) * MIN_ASPECT));
                int height23 = height22;
                if (width22 < this.mMinVisibleWidth) {
                    width22 = this.mMinVisibleWidth;
                    height2 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, Math.round(((float) width22) / MIN_ASPECT)));
                    width1 = width12;
                    height1 = height14;
                } else {
                    height2 = height23;
                    width1 = width12;
                    height1 = height14;
                }
            } else {
                int width13 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, width3));
                int height15 = Math.max(height, Math.round(((float) width13) * MIN_ASPECT));
                if (height15 < this.mMinVisibleHeight) {
                    int height16 = this.mMinVisibleHeight;
                    int i3 = width13;
                    height12 = height16;
                    height13 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, Math.round(((float) height16) / MIN_ASPECT)));
                } else {
                    height12 = height15;
                    height13 = width13;
                }
                int height24 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, height));
                width22 = Math.min(width3, Math.round(((float) height24) / MIN_ASPECT));
                int height25 = height24;
                if (width22 < this.mMinVisibleWidth) {
                    width22 = this.mMinVisibleWidth;
                    width1 = height13;
                    height1 = height12;
                    height2 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, Math.round(((float) width22) * MIN_ASPECT)));
                } else {
                    width1 = height13;
                    height1 = height12;
                    height2 = height25;
                }
            }
            int i4 = width3;
            if ((width3 > right - left || height > bottom - top) == (width1 * height1 > width22 * height2)) {
                width = width1;
                height = height1;
            } else {
                width = width22;
                height = height2;
            }
        }
        if (HwFreeFormUtils.isFreeFormEnable()) {
            int tmpFreeformMaxLength = HwFreeFormUtils.getFreeformMaxLength();
            int tmpFreeformMinLength = HwFreeFormUtils.getFreeformMinLength();
            int tmpHeight = tmpFreeformMaxLength > height ? height : tmpFreeformMaxLength;
            height = tmpHeight > tmpFreeformMinLength ? tmpHeight : tmpFreeformMinLength;
            int tmpWidth = tmpFreeformMaxLength > width ? width : tmpFreeformMaxLength;
            int width4 = tmpWidth > tmpFreeformMinLength ? tmpWidth : tmpFreeformMinLength;
            if ((tmpFreeformMaxLength == height && tmpFreeformMaxLength == width4) || (height == tmpFreeformMinLength && width4 == tmpFreeformMinLength)) {
                width2 = width4;
                if (this.mIsOutBound == 0) {
                    updateFreeFormOutLine(3);
                    this.mIsOutBound = true;
                }
            } else {
                width2 = width4;
                if (this.mIsOutBound != 0) {
                    updateFreeFormOutLine(4);
                    this.mIsOutBound = false;
                }
            }
        } else {
            width2 = width;
        }
        float f = aspect;
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

    private void updateWindowDragBounds(int x, int y, Rect stackBounds) {
        int offsetX = Math.round(((float) x) - this.mStartDragX);
        int offsetY = Math.round(((float) y) - this.mStartDragY);
        this.mWindowDragBounds.set(this.mWindowOriginalBounds);
        int maxLeft = stackBounds.right - this.mMinVisibleWidth;
        int minLeft = (stackBounds.left + this.mMinVisibleWidth) - this.mWindowOriginalBounds.width();
        int minTop = stackBounds.top;
        this.mWindowDragBounds.offsetTo(Math.min(Math.max(this.mWindowOriginalBounds.left + offsetX, minLeft), maxLeft), Math.min(Math.max(this.mWindowOriginalBounds.top + offsetY, minTop), stackBounds.bottom - this.mMinVisibleHeight));
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
            };
        }
        return sFactory.create(service);
    }

    /* access modifiers changed from: private */
    public void updateFreeFormOutLine(int color) {
        this.mHwTPEx.updateFreeFormOutLine(color);
    }
}
