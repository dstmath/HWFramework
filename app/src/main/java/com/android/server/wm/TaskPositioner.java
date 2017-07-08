package com.android.server.wm;

import android.annotation.IntDef;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.Trace;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.BatchedInputEventReceiver;
import android.view.Choreographer;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import com.android.server.input.InputApplicationHandle;
import com.android.server.input.InputWindowHandle;
import com.android.server.wm.WindowManagerService.H;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

class TaskPositioner implements DimLayerUser {
    private static final int CTRL_BOTTOM = 8;
    private static final int CTRL_LEFT = 1;
    private static final int CTRL_NONE = 0;
    private static final int CTRL_RIGHT = 2;
    private static final int CTRL_TOP = 4;
    public static final float RESIZING_HINT_ALPHA = 0.5f;
    public static final int RESIZING_HINT_DURATION_MS = 0;
    static final int SIDE_MARGIN_DIP = 100;
    private static final String TAG = null;
    private static final String TAG_LOCAL = "TaskPositioner";
    InputChannel mClientChannel;
    private int mCtrlType;
    private int mCurrentDimSide;
    private DimLayer mDimLayer;
    private Display mDisplay;
    private final DisplayMetrics mDisplayMetrics;
    InputApplicationHandle mDragApplicationHandle;
    private boolean mDragEnded;
    InputWindowHandle mDragWindowHandle;
    private WindowPositionerEventReceiver mInputEventReceiver;
    private int mMinVisibleHeight;
    private int mMinVisibleWidth;
    private boolean mResizing;
    InputChannel mServerChannel;
    private final WindowManagerService mService;
    private int mSideMargin;
    private float mStartDragX;
    private float mStartDragY;
    private Task mTask;
    private Rect mTmpRect;
    private final Rect mWindowDragBounds;
    private final Rect mWindowOriginalBounds;

    @IntDef(flag = true, value = {0, 1, 2, 4, 8})
    @Retention(RetentionPolicy.SOURCE)
    @interface CtrlType {
    }

    private final class WindowPositionerEventReceiver extends BatchedInputEventReceiver {
        public WindowPositionerEventReceiver(InputChannel inputChannel, Looper looper, Choreographer choreographer) {
            super(inputChannel, looper, choreographer);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onInputEvent(InputEvent event) {
            if ((event instanceof MotionEvent) && (event.getSource() & TaskPositioner.CTRL_RIGHT) != 0) {
                MotionEvent motionEvent = (MotionEvent) event;
                try {
                    if (TaskPositioner.this.mDragEnded) {
                        finishInputEvent(event, true);
                        return;
                    }
                    boolean wasResizing;
                    int createMode;
                    float newX = motionEvent.getRawX();
                    float newY = motionEvent.getRawY();
                    switch (motionEvent.getAction()) {
                        case TaskPositioner.CTRL_LEFT /*1*/:
                            TaskPositioner.this.mDragEnded = true;
                        case TaskPositioner.CTRL_RIGHT /*2*/:
                            synchronized (TaskPositioner.this.mService.mWindowMap) {
                                TaskPositioner.this.mDragEnded = TaskPositioner.this.notifyMoveLocked(newX, newY);
                                TaskPositioner.this.mTask.getDimBounds(TaskPositioner.this.mTmpRect);
                                break;
                            }
                            if (!TaskPositioner.this.mTmpRect.equals(TaskPositioner.this.mWindowDragBounds)) {
                                Trace.traceBegin(32, "wm.TaskPositioner.resizeTask");
                                try {
                                    TaskPositioner.this.mService.mActivityManager.resizeTask(TaskPositioner.this.mTask.mTaskId, TaskPositioner.this.mWindowDragBounds, TaskPositioner.CTRL_LEFT);
                                } catch (RemoteException e) {
                                }
                                Trace.traceEnd(32);
                            }
                        case H.REPORT_LOSING_FOCUS /*3*/:
                            TaskPositioner.this.mDragEnded = true;
                            if (TaskPositioner.this.mDragEnded) {
                                wasResizing = TaskPositioner.this.mResizing;
                                synchronized (TaskPositioner.this.mService.mWindowMap) {
                                    TaskPositioner.this.endDragLocked();
                                    break;
                                }
                                if (wasResizing) {
                                    try {
                                        TaskPositioner.this.mService.mActivityManager.resizeTask(TaskPositioner.this.mTask.mTaskId, TaskPositioner.this.mWindowDragBounds, 3);
                                    } catch (RemoteException e2) {
                                    }
                                }
                                if (TaskPositioner.this.mCurrentDimSide != 0) {
                                    if (TaskPositioner.this.mCurrentDimSide != TaskPositioner.CTRL_LEFT) {
                                        createMode = TaskPositioner.RESIZING_HINT_DURATION_MS;
                                    } else {
                                        createMode = TaskPositioner.CTRL_LEFT;
                                    }
                                    TaskPositioner.this.mService.mActivityManager.moveTaskToDockedStack(TaskPositioner.this.mTask.mTaskId, createMode, true, true, null, false);
                                }
                                TaskPositioner.this.mService.mH.sendEmptyMessage(40);
                            }
                            finishInputEvent(event, true);
                            break;
                    }
                    if (TaskPositioner.this.mDragEnded) {
                        wasResizing = TaskPositioner.this.mResizing;
                        synchronized (TaskPositioner.this.mService.mWindowMap) {
                            TaskPositioner.this.endDragLocked();
                        }
                        if (wasResizing) {
                            TaskPositioner.this.mService.mActivityManager.resizeTask(TaskPositioner.this.mTask.mTaskId, TaskPositioner.this.mWindowDragBounds, 3);
                        }
                        if (TaskPositioner.this.mCurrentDimSide != 0) {
                            if (TaskPositioner.this.mCurrentDimSide != TaskPositioner.CTRL_LEFT) {
                                createMode = TaskPositioner.CTRL_LEFT;
                            } else {
                                createMode = TaskPositioner.RESIZING_HINT_DURATION_MS;
                            }
                            TaskPositioner.this.mService.mActivityManager.moveTaskToDockedStack(TaskPositioner.this.mTask.mTaskId, createMode, true, true, null, false);
                        }
                        TaskPositioner.this.mService.mH.sendEmptyMessage(40);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.TaskPositioner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.TaskPositioner.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.TaskPositioner.<clinit>():void");
    }

    TaskPositioner(WindowManagerService service) {
        this.mDisplayMetrics = new DisplayMetrics();
        this.mTmpRect = new Rect();
        this.mWindowOriginalBounds = new Rect();
        this.mWindowDragBounds = new Rect();
        this.mCtrlType = RESIZING_HINT_DURATION_MS;
        this.mDragEnded = false;
        this.mService = service;
    }

    void register(Display display) {
        if (this.mClientChannel != null) {
            Slog.e(TAG, "Task positioner already registered");
            return;
        }
        this.mDisplay = display;
        this.mDisplay.getMetrics(this.mDisplayMetrics);
        InputChannel[] channels = InputChannel.openInputChannelPair(TAG);
        this.mServerChannel = channels[RESIZING_HINT_DURATION_MS];
        this.mClientChannel = channels[CTRL_LEFT];
        this.mService.mInputManager.registerInputChannel(this.mServerChannel, null);
        this.mInputEventReceiver = new WindowPositionerEventReceiver(this.mClientChannel, this.mService.mH.getLooper(), this.mService.mChoreographer);
        this.mDragApplicationHandle = new InputApplicationHandle(null);
        this.mDragApplicationHandle.name = TAG;
        this.mDragApplicationHandle.dispatchingTimeoutNanos = 5000000000L;
        this.mDragWindowHandle = new InputWindowHandle(this.mDragApplicationHandle, null, this.mDisplay.getDisplayId());
        this.mDragWindowHandle.name = TAG;
        this.mDragWindowHandle.inputChannel = this.mServerChannel;
        this.mDragWindowHandle.layer = this.mService.getDragLayerLocked();
        this.mDragWindowHandle.layoutParamsFlags = RESIZING_HINT_DURATION_MS;
        this.mDragWindowHandle.layoutParamsType = 2016;
        this.mDragWindowHandle.dispatchingTimeoutNanos = 5000000000L;
        this.mDragWindowHandle.visible = true;
        this.mDragWindowHandle.canReceiveKeys = false;
        this.mDragWindowHandle.hasFocus = true;
        this.mDragWindowHandle.hasWallpaper = false;
        this.mDragWindowHandle.paused = false;
        this.mDragWindowHandle.ownerPid = Process.myPid();
        this.mDragWindowHandle.ownerUid = Process.myUid();
        this.mDragWindowHandle.inputFeatures = RESIZING_HINT_DURATION_MS;
        this.mDragWindowHandle.scaleFactor = 1.0f;
        this.mDragWindowHandle.touchableRegion.setEmpty();
        this.mDragWindowHandle.frameLeft = RESIZING_HINT_DURATION_MS;
        this.mDragWindowHandle.frameTop = RESIZING_HINT_DURATION_MS;
        Point p = new Point();
        this.mDisplay.getRealSize(p);
        this.mDragWindowHandle.frameRight = p.x;
        this.mDragWindowHandle.frameBottom = p.y;
        this.mService.pauseRotationLocked();
        this.mDimLayer = new DimLayer(this.mService, this, this.mDisplay.getDisplayId(), TAG_LOCAL);
        this.mSideMargin = WindowManagerService.dipToPixel(SIDE_MARGIN_DIP, this.mDisplayMetrics);
        this.mMinVisibleWidth = WindowManagerService.dipToPixel(42, this.mDisplayMetrics);
        this.mMinVisibleHeight = WindowManagerService.dipToPixel(36, this.mDisplayMetrics);
        this.mDragEnded = false;
    }

    void unregister() {
        if (this.mClientChannel == null) {
            Slog.e(TAG, "Task positioner not registered");
            return;
        }
        this.mService.mInputManager.unregisterInputChannel(this.mServerChannel);
        this.mInputEventReceiver.dispose();
        this.mInputEventReceiver = null;
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
        this.mCurrentDimSide = RESIZING_HINT_DURATION_MS;
        this.mDragEnded = true;
        this.mService.resumeRotationLocked();
    }

    void startDragLocked(WindowState win, boolean resize, float startX, float startY) {
        this.mCtrlType = RESIZING_HINT_DURATION_MS;
        this.mTask = win.getTask();
        this.mStartDragX = startX;
        this.mStartDragY = startY;
        if (this.mTask.isDockedInEffect()) {
            this.mTask.getBounds(this.mTmpRect);
        } else {
            this.mTask.getDimBounds(this.mTmpRect);
        }
        if (resize) {
            if (startX < ((float) this.mTmpRect.left)) {
                this.mCtrlType |= CTRL_LEFT;
            }
            if (startX > ((float) this.mTmpRect.right)) {
                this.mCtrlType |= CTRL_RIGHT;
            }
            if (startY < ((float) this.mTmpRect.top)) {
                this.mCtrlType |= CTRL_TOP;
            }
            if (startY > ((float) this.mTmpRect.bottom)) {
                this.mCtrlType |= CTRL_BOTTOM;
            }
            this.mResizing = true;
        }
        this.mWindowOriginalBounds.set(this.mTmpRect);
    }

    private void endDragLocked() {
        this.mResizing = false;
        this.mTask.setDragResizing(false, RESIZING_HINT_DURATION_MS);
    }

    private boolean notifyMoveLocked(float x, float y) {
        if (this.mCtrlType != 0) {
            int deltaX = Math.round(x - this.mStartDragX);
            int deltaY = Math.round(y - this.mStartDragY);
            int left = this.mWindowOriginalBounds.left;
            int top = this.mWindowOriginalBounds.top;
            int right = this.mWindowOriginalBounds.right;
            int bottom = this.mWindowOriginalBounds.bottom;
            if ((this.mCtrlType & CTRL_LEFT) != 0) {
                left = Math.min(left + deltaX, right - this.mMinVisibleWidth);
            }
            if ((this.mCtrlType & CTRL_TOP) != 0) {
                top = Math.min(top + deltaY, bottom - this.mMinVisibleHeight);
            }
            if ((this.mCtrlType & CTRL_RIGHT) != 0) {
                right = Math.max(this.mMinVisibleWidth + left, right + deltaX);
            }
            if ((this.mCtrlType & CTRL_BOTTOM) != 0) {
                bottom = Math.max(this.mMinVisibleHeight + top, bottom + deltaY);
            }
            this.mWindowDragBounds.set(left, top, right, bottom);
            this.mTask.setDragResizing(true, RESIZING_HINT_DURATION_MS);
            return false;
        }
        this.mTask.mStack.getDimBounds(this.mTmpRect);
        if (!this.mTask.isDockedInEffect()) {
            this.mTmpRect.inset(this.mMinVisibleWidth, this.mMinVisibleHeight);
        }
        boolean dragEnded = false;
        int nX = (int) x;
        int nY = (int) y;
        if (!this.mTmpRect.contains(nX, nY)) {
            x = Math.min(Math.max(x, (float) this.mTmpRect.left), (float) this.mTmpRect.right);
            y = Math.min(Math.max(y, (float) this.mTmpRect.top), (float) this.mTmpRect.bottom);
            dragEnded = true;
        }
        updateWindowDragBounds(nX, nY);
        updateDimLayerVisibility(nX);
        return dragEnded;
    }

    private void updateWindowDragBounds(int x, int y) {
        this.mWindowDragBounds.set(this.mWindowOriginalBounds);
        if (!this.mTask.isDockedInEffect()) {
            this.mWindowDragBounds.offset(Math.round(((float) x) - this.mStartDragX), Math.round(((float) y) - this.mStartDragY));
        } else if (this.mService.mCurConfiguration.orientation == CTRL_RIGHT) {
            this.mWindowDragBounds.offset(Math.round(((float) x) - this.mStartDragX), RESIZING_HINT_DURATION_MS);
        } else {
            this.mWindowDragBounds.offset(RESIZING_HINT_DURATION_MS, Math.round(((float) y) - this.mStartDragY));
        }
    }

    private void updateDimLayerVisibility(int x) {
        int dimSide = getDimSide(x);
        if (dimSide != this.mCurrentDimSide) {
            this.mCurrentDimSide = dimSide;
            SurfaceControl.openTransaction();
            if (this.mCurrentDimSide == 0) {
                this.mDimLayer.hide();
            } else {
                showDimLayer();
            }
            SurfaceControl.closeTransaction();
        }
    }

    private int getDimSide(int x) {
        if (this.mTask.mStack.mStackId != CTRL_RIGHT || !this.mTask.mStack.isFullscreen() || this.mService.mCurConfiguration.orientation != CTRL_RIGHT) {
            return RESIZING_HINT_DURATION_MS;
        }
        this.mTask.mStack.getDimBounds(this.mTmpRect);
        if (x - this.mSideMargin <= this.mTmpRect.left) {
            return CTRL_LEFT;
        }
        return this.mSideMargin + x >= this.mTmpRect.right ? CTRL_RIGHT : RESIZING_HINT_DURATION_MS;
    }

    private void showDimLayer() {
        this.mTask.mStack.getDimBounds(this.mTmpRect);
        if (this.mCurrentDimSide == CTRL_LEFT) {
            this.mTmpRect.right = this.mTmpRect.centerX();
        } else if (this.mCurrentDimSide == CTRL_RIGHT) {
            this.mTmpRect.left = this.mTmpRect.centerX();
        }
        this.mDimLayer.setBounds(this.mTmpRect);
        this.mDimLayer.show(this.mService.getDragLayerLocked(), RESIZING_HINT_ALPHA, 0);
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

    public void getDimBounds(Rect out) {
    }

    public String toShortString() {
        return TAG;
    }
}
