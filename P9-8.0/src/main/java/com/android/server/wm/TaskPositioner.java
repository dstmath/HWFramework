package com.android.server.wm;

import android.annotation.IntDef;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.Trace;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.BatchedInputEventReceiver;
import android.view.Choreographer;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.MotionEvent;
import com.android.server.input.InputApplicationHandle;
import com.android.server.input.InputWindowHandle;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

class TaskPositioner implements DimLayerUser {
    private static final int CTRL_BOTTOM = 8;
    private static final int CTRL_LEFT = 1;
    private static final int CTRL_NONE = 0;
    private static final int CTRL_RIGHT = 2;
    private static final int CTRL_TOP = 4;
    private static final boolean DEBUG_ORIENTATION_VIOLATIONS = false;
    private static final int DISPOSE_MSG = 0;
    static final float MIN_ASPECT = 1.2f;
    public static final float RESIZING_HINT_ALPHA = 0.5f;
    public static final int RESIZING_HINT_DURATION_MS = 0;
    static final int SIDE_MARGIN_DIP = 100;
    private static final String TAG = "WindowManager";
    private static final String TAG_LOCAL = "TaskPositioner";
    Handler PCHandler = null;
    InputChannel mClientChannel;
    private int mCtrlType = 0;
    private int mCurrentDimSide;
    private DimLayer mDimLayer;
    private Display mDisplay;
    private final DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    InputApplicationHandle mDragApplicationHandle;
    private boolean mDragEnded = false;
    InputWindowHandle mDragWindowHandle;
    private WindowPositionerEventReceiver mInputEventReceiver;
    private final Point mMaxVisibleSize = new Point();
    private int mMinVisibleHeight;
    private int mMinVisibleWidth;
    private boolean mPreserveOrientation;
    private boolean mResizing;
    InputChannel mServerChannel;
    private final WindowManagerService mService;
    private int mSideMargin;
    private float mStartDragX;
    private float mStartDragY;
    private boolean mStartOrientationWasLandscape;
    private Task mTask;
    private Rect mTmpRect = new Rect();
    private final Rect mWindowDragBounds = new Rect();
    private final Rect mWindowOriginalBounds = new Rect();

    @IntDef(flag = true, value = {0, 1, 2, 4, 8})
    @Retention(RetentionPolicy.SOURCE)
    @interface CtrlType {
    }

    private final class WindowPositionerEventReceiver extends BatchedInputEventReceiver {
        public WindowPositionerEventReceiver(InputChannel inputChannel, Looper looper, Choreographer choreographer) {
            super(inputChannel, looper, choreographer);
        }

        /* JADX WARNING: Removed duplicated region for block: B:16:0x0036 A:{Catch:{ all -> 0x0158, all -> 0x013d, Exception -> 0x012e, all -> 0x0143 }} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onInputEvent(InputEvent event) {
            if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0) {
                MotionEvent motionEvent = (MotionEvent) event;
                try {
                    if (TaskPositioner.this.mDragEnded) {
                        finishInputEvent(event, true);
                        return;
                    }
                    float newX = motionEvent.getRawX();
                    float newY = motionEvent.getRawY();
                    switch (motionEvent.getAction()) {
                        case 1:
                            TaskPositioner.this.mDragEnded = true;
                        case 2:
                            synchronized (TaskPositioner.this.mService.mWindowMap) {
                                WindowManagerService.boostPriorityForLockedSection();
                                TaskPositioner.this.mDragEnded = TaskPositioner.this.notifyMoveLocked(newX, newY);
                                TaskPositioner.this.mTask.getDimBounds(TaskPositioner.this.mTmpRect);
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            if (!TaskPositioner.this.mTmpRect.equals(TaskPositioner.this.mWindowDragBounds)) {
                                Trace.traceBegin(32, "wm.TaskPositioner.resizeTask");
                                try {
                                    TaskPositioner.this.mService.mActivityManager.resizeTask(TaskPositioner.this.mTask.mTaskId, TaskPositioner.this.mWindowDragBounds, 1);
                                } catch (RemoteException e) {
                                }
                                Trace.traceEnd(32);
                            }
                        case 3:
                            TaskPositioner.this.mDragEnded = true;
                            if (TaskPositioner.this.mDragEnded) {
                                boolean wasResizing = TaskPositioner.this.mResizing;
                                synchronized (TaskPositioner.this.mService.mWindowMap) {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    TaskPositioner.this.endDragLocked();
                                    TaskPositioner.this.mTask.getDimBounds(TaskPositioner.this.mTmpRect);
                                }
                                WindowManagerService.resetPriorityAfterLockedSection();
                                if (wasResizing) {
                                    try {
                                        if ((TaskPositioner.this.mTmpRect.equals(TaskPositioner.this.mWindowDragBounds) ^ 1) != 0) {
                                            TaskPositioner.this.mService.mActivityManager.resizeTask(TaskPositioner.this.mTask.mTaskId, TaskPositioner.this.mWindowDragBounds, 3);
                                        }
                                    } catch (RemoteException e2) {
                                    }
                                }
                                if (TaskPositioner.this.mCurrentDimSide != 0) {
                                    int createMode;
                                    if (TaskPositioner.this.mCurrentDimSide == 1) {
                                        createMode = 0;
                                    } else {
                                        createMode = 1;
                                    }
                                    TaskPositioner.this.mService.mActivityManager.moveTaskToDockedStack(TaskPositioner.this.mTask.mTaskId, createMode, true, true, null);
                                }
                                TaskPositioner.this.mService.mH.sendEmptyMessage(40);
                            }
                            finishInputEvent(event, true);
                            break;
                    }
                    if (TaskPositioner.this.mDragEnded) {
                    }
                    finishInputEvent(event, true);
                } catch (Exception e3) {
                    Slog.e(TaskPositioner.TAG, "Exception caught by drag handleMotion", e3);
                    finishInputEvent(event, false);
                } catch (Throwable th) {
                    finishInputEvent(event, false);
                }
            }
        }
    }

    TaskPositioner(WindowManagerService service) {
        this.mService = service;
    }

    Rect getWindowDragBounds() {
        return this.mWindowDragBounds;
    }

    void register(Display display) {
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
        this.mService.pauseRotationLocked();
        this.mDimLayer = new DimLayer(this.mService, this, this.mDisplay.getDisplayId(), TAG_LOCAL);
        this.mSideMargin = WindowManagerService.dipToPixel(100, this.mDisplayMetrics);
        this.mMinVisibleWidth = WindowManagerService.dipToPixel(42, this.mDisplayMetrics);
        this.mMinVisibleHeight = WindowManagerService.dipToPixel(36, this.mDisplayMetrics);
        this.mDisplay.getRealSize(this.mMaxVisibleSize);
        this.mDragEnded = false;
    }

    void unregister() {
        if (this.mClientChannel == null) {
            Slog.e(TAG, "Task positioner not registered");
            return;
        }
        this.mService.mInputManager.unregisterInputChannel(this.mServerChannel);
        if (HwPCUtils.enabledInPad() && this.mDisplay != null && HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(this.mDisplay.getDisplayId())) {
            this.PCHandler = new Handler(this.mService.mAnimationHandler.getLooper()) {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 0:
                            TaskPositioner.this.mInputEventReceiver.dispose();
                            TaskPositioner.this.mInputEventReceiver = null;
                            return;
                        default:
                            return;
                    }
                }
            };
            this.PCHandler.removeMessages(0);
            this.PCHandler.sendEmptyMessage(0);
        } else {
            this.mInputEventReceiver.dispose();
            this.mInputEventReceiver = null;
        }
        this.mClientChannel.dispose();
        this.mServerChannel.dispose();
        this.mClientChannel = null;
        this.mServerChannel = null;
        this.mDragWindowHandle = null;
        this.mDragApplicationHandle = null;
        this.mDisplay = null;
        if (this.mDimLayer != null) {
            this.mDimLayer.destroySurface();
            this.mDimLayer = null;
        }
        this.mCurrentDimSide = 0;
        this.mDragEnded = true;
        this.mService.resumeRotationLocked();
    }

    void startDrag(WindowState win, boolean resize, boolean preserveOrientation, float startX, float startY) {
        this.mTask = win.getTask();
        this.mTask.getDimBounds(this.mTmpRect);
        startDrag(resize, preserveOrientation, startX, startY, this.mTmpRect);
    }

    void startDrag(boolean resize, boolean preserveOrientation, float startX, float startY, Rect startBounds) {
        boolean z = true;
        this.mCtrlType = 0;
        this.mStartDragX = startX;
        this.mStartDragY = startY;
        this.mPreserveOrientation = preserveOrientation;
        if (resize) {
            if (startX < ((float) startBounds.left)) {
                this.mCtrlType |= 1;
            }
            if (startX > ((float) startBounds.right)) {
                this.mCtrlType |= 2;
            }
            if (startY < ((float) startBounds.top)) {
                this.mCtrlType |= 4;
            }
            if (startY > ((float) startBounds.bottom)) {
                this.mCtrlType |= 8;
            }
            this.mResizing = this.mCtrlType != 0;
        }
        if (startBounds.width() < startBounds.height()) {
            z = false;
        }
        this.mStartOrientationWasLandscape = z;
        this.mWindowOriginalBounds.set(startBounds);
        this.mWindowDragBounds.set(startBounds);
    }

    private void endDragLocked() {
        this.mResizing = false;
        this.mTask.setDragResizing(false, 0);
    }

    private boolean notifyMoveLocked(float x, float y) {
        if (this.mCtrlType != 0) {
            resizeDrag(x, y);
            this.mTask.setDragResizing(true, 0);
            return false;
        }
        this.mTask.mStack.getDimBounds(this.mTmpRect);
        if (HwPCUtils.isPcDynamicStack(this.mTask.mStack.mStackId)) {
            this.mTmpRect.bottom = getDisplayInfo().appHeight;
        }
        int nX = (int) x;
        int nY = (int) y;
        if (!this.mTmpRect.contains(nX, nY)) {
            nX = Math.min(Math.max(nX, this.mTmpRect.left), this.mTmpRect.right);
            nY = Math.min(Math.max(nY, this.mTmpRect.top), this.mTmpRect.bottom);
        }
        updateWindowDragBounds(nX, nY, this.mTmpRect);
        updateDimLayerVisibility(nX);
        return false;
    }

    void resizeDrag(float x, float y) {
        int deltaX = Math.round(x - this.mStartDragX);
        int deltaY = Math.round(y - this.mStartDragY);
        int left = this.mWindowOriginalBounds.left;
        int top = this.mWindowOriginalBounds.top;
        int right = this.mWindowOriginalBounds.right;
        int bottom = this.mWindowOriginalBounds.bottom;
        if (this.mPreserveOrientation) {
            if (this.mStartOrientationWasLandscape) {
            }
        }
        int width = right - left;
        int height = bottom - top;
        if ((this.mCtrlType & 1) != 0) {
            width = Math.max(this.mMinVisibleWidth, width - deltaX);
        } else if ((this.mCtrlType & 2) != 0) {
            width = Math.max(this.mMinVisibleWidth, width + deltaX);
        }
        if ((this.mCtrlType & 4) != 0) {
            height = Math.max(this.mMinVisibleHeight, height - deltaY);
        } else if ((this.mCtrlType & 8) != 0) {
            height = Math.max(this.mMinVisibleHeight, height + deltaY);
        }
        float aspect = ((float) width) / ((float) height);
        if (this.mPreserveOrientation && ((this.mStartOrientationWasLandscape && aspect < MIN_ASPECT) || (!this.mStartOrientationWasLandscape && ((double) aspect) > 0.8333333002196431d))) {
            int width1;
            int height1;
            int height2;
            int width2;
            if (this.mStartOrientationWasLandscape) {
                width1 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, width));
                height1 = Math.min(height, Math.round(((float) width1) / MIN_ASPECT));
                if (height1 < this.mMinVisibleHeight) {
                    height1 = this.mMinVisibleHeight;
                    width1 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, Math.round(((float) height1) * MIN_ASPECT)));
                }
                height2 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, height));
                width2 = Math.max(width, Math.round(((float) height2) * MIN_ASPECT));
                if (width2 < this.mMinVisibleWidth) {
                    width2 = this.mMinVisibleWidth;
                    height2 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, Math.round(((float) width2) / MIN_ASPECT)));
                }
            } else {
                width1 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, width));
                height1 = Math.max(height, Math.round(((float) width1) * MIN_ASPECT));
                if (height1 < this.mMinVisibleHeight) {
                    height1 = this.mMinVisibleHeight;
                    width1 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, Math.round(((float) height1) / MIN_ASPECT)));
                }
                height2 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, height));
                width2 = Math.min(width, Math.round(((float) height2) / MIN_ASPECT));
                if (width2 < this.mMinVisibleWidth) {
                    width2 = this.mMinVisibleWidth;
                    height2 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, Math.round(((float) width2) * MIN_ASPECT)));
                }
            }
            boolean grows = width > right - left || height > bottom - top;
            if (grows == (width1 * height1 > width2 * height2)) {
                width = width1;
                height = height1;
            } else {
                width = width2;
                height = height2;
            }
        }
        updateDraggedBounds(left, top, right, bottom, width, height);
    }

    void updateDraggedBounds(int left, int top, int right, int bottom, int newWidth, int newHeight) {
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
        int maxTop = stackBounds.bottom - this.mMinVisibleHeight;
        this.mWindowDragBounds.offsetTo(Math.min(Math.max(this.mWindowOriginalBounds.left + offsetX, (stackBounds.left + this.mMinVisibleWidth) - this.mWindowOriginalBounds.width()), stackBounds.right - this.mMinVisibleWidth), Math.min(Math.max(this.mWindowOriginalBounds.top + offsetY, stackBounds.top), maxTop));
    }

    private void updateDimLayerVisibility(int x) {
        int dimSide = getDimSide(x);
        if (dimSide != this.mCurrentDimSide) {
            this.mCurrentDimSide = dimSide;
            this.mService.openSurfaceTransaction();
            if (this.mCurrentDimSide == 0) {
                this.mDimLayer.hide();
            } else {
                showDimLayer();
            }
            this.mService.closeSurfaceTransaction();
        }
    }

    private int getDimSide(int x) {
        if (this.mTask.mStack.mStackId != 2 || (this.mTask.mStack.fillsParent() ^ 1) != 0 || this.mTask.mStack.getConfiguration().orientation != 2) {
            return 0;
        }
        this.mTask.mStack.getDimBounds(this.mTmpRect);
        if (x - this.mSideMargin <= this.mTmpRect.left) {
            return 1;
        }
        return this.mSideMargin + x >= this.mTmpRect.right ? 2 : 0;
    }

    private void showDimLayer() {
        this.mTask.mStack.getDimBounds(this.mTmpRect);
        if (this.mCurrentDimSide == 1) {
            this.mTmpRect.right = this.mTmpRect.centerX();
        } else if (this.mCurrentDimSide == 2) {
            this.mTmpRect.left = this.mTmpRect.centerX();
        }
        this.mDimLayer.setBounds(this.mTmpRect);
        this.mDimLayer.show(this.mService.getDragLayerLocked(), 0.5f, 0);
    }

    public boolean dimFullscreen() {
        return isFullscreen();
    }

    boolean isFullscreen() {
        return false;
    }

    public DisplayInfo getDisplayInfo() {
        return this.mTask.mStack.getDisplayInfo();
    }

    public boolean isAttachedToDisplay() {
        return (this.mTask == null || this.mTask.getDisplayContent() == null) ? false : true;
    }

    public void getDimBounds(Rect out) {
    }

    public String toShortString() {
        return TAG;
    }
}
