package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.GraphicBuffer;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.view.DisplayCutout;
import android.view.IWindowSession;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.policy.DecorView;
import com.android.internal.view.BaseIWindow;
import com.android.server.policy.WindowManagerPolicy;
import java.util.Objects;

class TaskSnapshotSurface implements WindowManagerPolicy.StartingSurface {
    private static final int FLAG_INHERIT_EXCLUDES = 830922808;
    private static final int MSG_REPORT_DRAW = 0;
    private static final int PRIVATE_FLAG_INHERITS = 131072;
    private static final long SIZE_MISMATCH_MINIMUM_TIME_MS = 450;
    private static final String TAG = "WindowManager";
    private static final String TITLE_FORMAT = "SnapshotStartingWindow for taskId=%s";
    /* access modifiers changed from: private */
    public static Handler sHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            boolean hasDrawn;
            if (msg.what == 0) {
                TaskSnapshotSurface surface = (TaskSnapshotSurface) msg.obj;
                synchronized (surface.mService.mWindowMap) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        hasDrawn = surface.mHasDrawn;
                    } catch (Throwable th) {
                        while (true) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                if (hasDrawn) {
                    surface.reportDrawn();
                }
            }
        }
    };
    private final Paint mBackgroundPaint = new Paint();
    private SurfaceControl mChildSurfaceControl;
    private final Rect mContentInsets = new Rect();
    private final Rect mFrame = new Rect();
    private final Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mHasDrawn;
    /* access modifiers changed from: private */
    public final int mOrientationOnCreation;
    /* access modifiers changed from: private */
    public final WindowManagerService mService;
    private final IWindowSession mSession;
    private long mShownTime;
    private boolean mSizeMismatch;
    private boolean mSizeScaled;
    private ActivityManager.TaskSnapshot mSnapshot;
    private final Rect mStableInsets = new Rect();
    private final int mStatusBarColor;
    private final Surface mSurface;
    @VisibleForTesting
    final SystemBarBackgroundPainter mSystemBarBackgroundPainter;
    private final Rect mTaskBounds;
    private final CharSequence mTitle;
    private final Window mWindow;

    static class SystemBarBackgroundPainter {
        private final Rect mContentInsets = new Rect();
        private final int mNavigationBarColor;
        private final Paint mNavigationBarPaint = new Paint();
        private final Rect mStableInsets = new Rect();
        private final int mStatusBarColor;
        private final Paint mStatusBarPaint = new Paint();
        private final int mSysUiVis;
        private final int mWindowFlags;
        private final int mWindowPrivateFlags;

        SystemBarBackgroundPainter(int windowFlags, int windowPrivateFlags, int sysUiVis, int statusBarColor, int navigationBarColor) {
            this.mWindowFlags = windowFlags;
            this.mWindowPrivateFlags = windowPrivateFlags;
            this.mSysUiVis = sysUiVis;
            this.mStatusBarColor = DecorView.calculateStatusBarColor(windowFlags, ActivityThread.currentActivityThread().getSystemUiContext().getColor(17170783), statusBarColor);
            this.mNavigationBarColor = navigationBarColor;
            this.mStatusBarPaint.setColor(this.mStatusBarColor);
            this.mNavigationBarPaint.setColor(navigationBarColor);
        }

        /* access modifiers changed from: package-private */
        public void setInsets(Rect contentInsets, Rect stableInsets) {
            this.mContentInsets.set(contentInsets);
            this.mStableInsets.set(stableInsets);
        }

        /* access modifiers changed from: package-private */
        public int getStatusBarColorViewHeight() {
            if (DecorView.STATUS_BAR_COLOR_VIEW_ATTRIBUTES.isVisible(this.mSysUiVis, this.mStatusBarColor, this.mWindowFlags, (this.mWindowPrivateFlags & 131072) != 0)) {
                return DecorView.getColorViewTopInset(this.mStableInsets.top, this.mContentInsets.top);
            }
            return 0;
        }

        private boolean isNavigationBarColorViewVisible() {
            return DecorView.NAVIGATION_BAR_COLOR_VIEW_ATTRIBUTES.isVisible(this.mSysUiVis, this.mNavigationBarColor, this.mWindowFlags, false);
        }

        /* access modifiers changed from: package-private */
        public void drawDecors(Canvas c, Rect alreadyDrawnFrame) {
            drawStatusBarBackground(c, alreadyDrawnFrame, getStatusBarColorViewHeight());
            drawNavigationBarBackground(c);
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public void drawStatusBarBackground(Canvas c, Rect alreadyDrawnFrame, int statusBarHeight) {
            if (statusBarHeight > 0 && Color.alpha(this.mStatusBarColor) != 0) {
                if (alreadyDrawnFrame == null || c.getWidth() > alreadyDrawnFrame.right) {
                    c.drawRect((float) (alreadyDrawnFrame != null ? alreadyDrawnFrame.right : 0), 0.0f, (float) (c.getWidth() - DecorView.getColorViewRightInset(this.mStableInsets.right, this.mContentInsets.right)), (float) statusBarHeight, this.mStatusBarPaint);
                }
            }
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public void drawNavigationBarBackground(Canvas c) {
            Rect navigationBarRect = new Rect();
            DecorView.getNavigationBarRect(c.getWidth(), c.getHeight(), this.mStableInsets, this.mContentInsets, navigationBarRect);
            if (isNavigationBarColorViewVisible() && Color.alpha(this.mNavigationBarColor) != 0 && !navigationBarRect.isEmpty()) {
                c.drawRect(navigationBarRect, this.mNavigationBarPaint);
            }
        }
    }

    @VisibleForTesting
    static class Window extends BaseIWindow {
        private TaskSnapshotSurface mOuter;

        Window() {
        }

        public void setOuter(TaskSnapshotSurface outer) {
            this.mOuter = outer;
        }

        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeNavBar, int displayId, DisplayCutout.ParcelableWrapper displayCutout) {
            if (!(mergedConfiguration == null || this.mOuter == null || this.mOuter.mOrientationOnCreation == mergedConfiguration.getMergedConfiguration().orientation)) {
                Handler access$400 = TaskSnapshotSurface.sHandler;
                TaskSnapshotSurface taskSnapshotSurface = this.mOuter;
                Objects.requireNonNull(taskSnapshotSurface);
                access$400.post(new Runnable() {
                    public final void run() {
                        TaskSnapshotSurface.this.remove();
                    }
                });
            }
            if (reportDraw) {
                TaskSnapshotSurface.sHandler.obtainMessage(0, this.mOuter).sendToTarget();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:52:0x019f, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x01ae, code lost:
        r54 = r4;
        r51 = r7;
        r56 = r8;
        r57 = r10;
        r58 = r11;
        r59 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x01cb, code lost:
        if (r13.addToDisplay(r14, r14.mSeq, r15, 8, r62.getDisplayContent().getDisplayId(), r11, r9, r9, r9, r12, null) >= 0) goto L_0x01f4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x01cd, code lost:
        android.util.Slog.w(TAG, "Failed to add snapshot starting window res=" + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x01e3, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x01e8, code lost:
        r54 = r4;
        r51 = r7;
        r56 = r8;
        r57 = r10;
        r58 = r11;
        r59 = r13;
     */
    static TaskSnapshotSurface create(WindowManagerService service, AppWindowToken token, ActivityManager.TaskSnapshot snapshot) {
        int backgroundColor;
        int statusBarColor;
        int navigationBarColor;
        int windowFlags;
        int windowPrivateFlags;
        int currentOrientation;
        IWindowSession session;
        Rect tmpFrame;
        Rect tmpContentInsets;
        Rect tmpStableInsets;
        Rect taskBounds;
        int sysUiVis;
        int statusBarColor2;
        StringBuilder sb;
        AppWindowToken appWindowToken = token;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = new Window();
        IWindowSession session2 = WindowManagerGlobal.getWindowSession();
        window.setSession(session2);
        Surface surface = new Surface();
        Rect tmpRect = new Rect();
        DisplayCutout.ParcelableWrapper tmpCutout = new DisplayCutout.ParcelableWrapper();
        Rect tmpStableInsets2 = new Rect();
        Rect tmpContentInsets2 = new Rect();
        Rect tmpStableInsets3 = new Rect();
        MergedConfiguration tmpMergedConfiguration = new MergedConfiguration();
        synchronized (service.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                WindowState mainWindow = token.findMainWindow();
                Task task = token.getTask();
                if (task == null) {
                    backgroundColor = -1;
                    statusBarColor = 0;
                    try {
                        sb = new StringBuilder();
                    } catch (Throwable th) {
                        th = th;
                        IWindowSession iWindowSession = session2;
                        Window window2 = window;
                        WindowManager.LayoutParams layoutParams2 = layoutParams;
                        Rect rect = tmpStableInsets2;
                        Rect tmpFrame2 = tmpStableInsets3;
                        Rect tmpStableInsets4 = rect;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                    try {
                        sb.append("TaskSnapshotSurface.create: Failed to find task for token=");
                        sb.append(appWindowToken);
                        Slog.w(TAG, sb.toString());
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    } catch (Throwable th3) {
                        th = th3;
                        IWindowSession iWindowSession2 = session2;
                        Window window3 = window;
                        WindowManager.LayoutParams layoutParams3 = layoutParams;
                        int i = backgroundColor;
                        Rect rect2 = tmpStableInsets2;
                        Rect tmpFrame3 = rect2;
                        while (true) {
                            break;
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                } else {
                    backgroundColor = -1;
                    statusBarColor = 0;
                    navigationBarColor = 0;
                    try {
                        AppWindowToken topFullscreenToken = token.getTask().getTopFullscreenAppToken();
                        if (topFullscreenToken == null) {
                            Slog.w(TAG, "TaskSnapshotSurface.create: Failed to find top fullscreen for task=" + task);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return null;
                        }
                        WindowState topFullscreenWindow = topFullscreenToken.getTopFullscreenWindow();
                        if (mainWindow == null) {
                            WindowState windowState = mainWindow;
                            AppWindowToken appWindowToken2 = topFullscreenToken;
                            IWindowSession iWindowSession3 = session2;
                            Window window4 = window;
                            WindowManager.LayoutParams layoutParams4 = layoutParams;
                            Rect rect3 = tmpStableInsets2;
                            Rect tmpFrame4 = tmpStableInsets3;
                            Rect tmpStableInsets5 = rect3;
                        } else if (topFullscreenWindow == null) {
                            WindowState windowState2 = mainWindow;
                            AppWindowToken appWindowToken3 = topFullscreenToken;
                            IWindowSession iWindowSession4 = session2;
                            Window window5 = window;
                            WindowManager.LayoutParams layoutParams5 = layoutParams;
                            Rect rect4 = tmpStableInsets2;
                            Rect tmpFrame5 = tmpStableInsets3;
                            Rect tmpStableInsets6 = rect4;
                        } else {
                            int sysUiVis2 = topFullscreenWindow.getSystemUiVisibility();
                            windowFlags = topFullscreenWindow.getAttrs().flags;
                            windowPrivateFlags = topFullscreenWindow.getAttrs().privateFlags;
                            layoutParams.packageName = mainWindow.getAttrs().packageName;
                            layoutParams.windowAnimations = mainWindow.getAttrs().windowAnimations;
                            layoutParams.dimAmount = mainWindow.getAttrs().dimAmount;
                            layoutParams.type = 3;
                            layoutParams.format = snapshot.getSnapshot().getFormat();
                            layoutParams.flags = (windowFlags & -830922809) | 8 | 16;
                            layoutParams.privateFlags = windowPrivateFlags & 131072;
                            layoutParams.token = appWindowToken.token;
                            layoutParams.width = -1;
                            layoutParams.height = -1;
                            layoutParams.systemUiVisibility = sysUiVis2;
                            WindowState windowState3 = mainWindow;
                            AppWindowToken appWindowToken4 = topFullscreenToken;
                            layoutParams.setTitle(String.format(TITLE_FORMAT, new Object[]{Integer.valueOf(task.mTaskId)}));
                            layoutParams.layoutInDisplayCutoutMode = topFullscreenWindow.getAttrs().layoutInDisplayCutoutMode;
                            ActivityManager.TaskDescription taskDescription = task.getTaskDescription();
                            if (taskDescription != null) {
                                int backgroundColor2 = taskDescription.getBackgroundColor();
                                try {
                                    statusBarColor2 = taskDescription.getStatusBarColor();
                                } catch (Throwable th4) {
                                    th = th4;
                                    IWindowSession iWindowSession5 = session2;
                                    Window window6 = window;
                                    WindowManager.LayoutParams layoutParams6 = layoutParams;
                                    Rect rect22 = tmpStableInsets2;
                                    Rect tmpFrame32 = rect22;
                                    while (true) {
                                        break;
                                    }
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                                try {
                                    backgroundColor = backgroundColor2;
                                    statusBarColor = statusBarColor2;
                                    navigationBarColor = taskDescription.getNavigationBarColor();
                                } catch (Throwable th5) {
                                    th = th5;
                                    int i2 = statusBarColor2;
                                    IWindowSession iWindowSession6 = session2;
                                    Window window7 = window;
                                    WindowManager.LayoutParams layoutParams7 = layoutParams;
                                    Rect rect222 = tmpStableInsets2;
                                    Rect tmpFrame322 = rect222;
                                    while (true) {
                                        break;
                                    }
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                            Rect taskBounds2 = new Rect();
                            task.getBounds(taskBounds2);
                            currentOrientation = topFullscreenWindow.getConfiguration().orientation;
                        }
                        try {
                            Slog.w(TAG, "TaskSnapshotSurface.create: Failed to find main window for token=" + appWindowToken);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return null;
                        } catch (Throwable th6) {
                            th = th6;
                            while (true) {
                                break;
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        IWindowSession iWindowSession7 = session2;
                        Window window8 = window;
                        WindowManager.LayoutParams layoutParams8 = layoutParams;
                        Rect rect5 = tmpStableInsets2;
                        Rect tmpFrame6 = tmpStableInsets3;
                        Rect tmpStableInsets7 = rect5;
                        int i3 = backgroundColor;
                        int i4 = statusBarColor;
                        while (true) {
                            break;
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } catch (Throwable th8) {
                th = th8;
                IWindowSession iWindowSession8 = session2;
                Window window9 = window;
                WindowManager.LayoutParams layoutParams9 = layoutParams;
                Rect rect6 = tmpStableInsets2;
                Rect tmpFrame7 = tmpStableInsets3;
                Rect tmpStableInsets8 = rect6;
                while (true) {
                    break;
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        TaskSnapshotSurface taskSnapshotSurface = new TaskSnapshotSurface(service, window, surface, snapshot, layoutParams.getTitle(), backgroundColor, statusBarColor, navigationBarColor, sysUiVis, windowFlags, windowPrivateFlags, taskBounds, currentOrientation);
        TaskSnapshotSurface snapshotSurface = taskSnapshotSurface;
        window.setOuter(snapshotSurface);
        try {
            try {
                session.relayout(window, window.mSeq, layoutParams, -1, -1, 0, 0, -1, tmpFrame, tmpRect, tmpContentInsets, tmpRect, tmpStableInsets, tmpRect, tmpRect, tmpCutout, tmpMergedConfiguration, surface);
            } catch (RemoteException e) {
            }
        } catch (RemoteException e2) {
            Window window10 = window;
            WindowManager.LayoutParams layoutParams10 = layoutParams;
        }
        snapshotSurface.setFrames(tmpFrame, tmpContentInsets, tmpStableInsets);
        snapshotSurface.drawSnapshot();
        return snapshotSurface;
        snapshotSurface.setFrames(tmpFrame, tmpContentInsets, tmpStableInsets);
        snapshotSurface.drawSnapshot();
        return snapshotSurface;
    }

    @VisibleForTesting
    TaskSnapshotSurface(WindowManagerService service, Window window, Surface surface, ActivityManager.TaskSnapshot snapshot, CharSequence title, int backgroundColor, int statusBarColor, int navigationBarColor, int sysUiVis, int windowFlags, int windowPrivateFlags, Rect taskBounds, int currentOrientation) {
        this.mService = service;
        this.mHandler = new Handler(this.mService.mH.getLooper());
        this.mSession = WindowManagerGlobal.getWindowSession();
        this.mWindow = window;
        this.mSurface = surface;
        this.mSnapshot = snapshot;
        this.mTitle = title;
        this.mBackgroundPaint.setColor(backgroundColor != 0 ? backgroundColor : -1);
        this.mTaskBounds = taskBounds;
        SystemBarBackgroundPainter systemBarBackgroundPainter = new SystemBarBackgroundPainter(windowFlags, windowPrivateFlags, sysUiVis, statusBarColor, navigationBarColor);
        this.mSystemBarBackgroundPainter = systemBarBackgroundPainter;
        this.mStatusBarColor = statusBarColor;
        this.mOrientationOnCreation = currentOrientation;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x004c, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004f, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0051, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0056, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_STARTING_WINDOW == false) goto L_0x005f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0058, code lost:
        android.util.Slog.v(TAG, "Removing snapshot surface");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005f, code lost:
        r9.mSession.remove(r9.mWindow);
     */
    public void remove() {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long now = SystemClock.uptimeMillis();
                if (this.mSizeMismatch && now - this.mShownTime < SIZE_MISMATCH_MINIMUM_TIME_MS) {
                    this.mHandler.postAtTime(new Runnable() {
                        public final void run() {
                            TaskSnapshotSurface.this.remove();
                        }
                    }, this.mShownTime + SIZE_MISMATCH_MINIMUM_TIME_MS);
                    if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                        Slog.v(TAG, "Defer removing snapshot surface in " + (now - this.mShownTime) + "ms");
                    }
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setFrames(Rect frame, Rect contentInsets, Rect stableInsets) {
        this.mFrame.set(frame);
        this.mContentInsets.set(contentInsets);
        this.mStableInsets.set(stableInsets);
        boolean z = true;
        this.mSizeMismatch = (this.mFrame.width() == this.mSnapshot.getSnapshot().getWidth() && this.mFrame.height() == this.mSnapshot.getSnapshot().getHeight()) ? false : true;
        if (Math.abs((((float) this.mFrame.width()) * this.mSnapshot.getScale()) - ((float) this.mSnapshot.getSnapshot().getWidth())) >= 1.0f || Math.abs((((float) this.mFrame.height()) * this.mSnapshot.getScale()) - ((float) this.mSnapshot.getSnapshot().getHeight())) >= 1.0f) {
            z = false;
        }
        this.mSizeScaled = z;
        this.mSystemBarBackgroundPainter.setInsets(contentInsets, stableInsets);
    }

    private void drawSnapshot() {
        GraphicBuffer buffer = this.mSnapshot.getSnapshot();
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v(TAG, "Drawing snapshot surface sizeMismatch=" + this.mSizeMismatch + ", mSizeScaled=" + this.mSizeScaled);
        }
        if (!this.mSizeMismatch) {
            drawSizeMatchSnapshot(buffer);
        } else if (!TaskSnapshotPersister.USE_CONFIG_SCALE || !this.mSizeScaled) {
            drawSizeMismatchSnapshot(buffer);
        } else {
            drawSizeScaledSnapshot(buffer);
        }
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mShownTime = SystemClock.uptimeMillis();
                this.mHasDrawn = true;
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        reportDrawn();
        this.mSnapshot = null;
    }

    private void drawSizeMatchSnapshot(GraphicBuffer buffer) {
        this.mSurface.attachAndQueueBuffer(buffer);
        this.mSurface.release();
    }

    /* JADX INFO: finally extract failed */
    private void drawSizeMismatchSnapshot(GraphicBuffer buffer) {
        SurfaceControl.Builder builder = new SurfaceControl.Builder(new SurfaceSession(this.mSurface));
        this.mChildSurfaceControl = builder.setName(this.mTitle + " - task-snapshot-surface").setSize(buffer.getWidth(), buffer.getHeight()).setFormat(buffer.getFormat()).build();
        Surface surface = new Surface();
        surface.copyFrom(this.mChildSurfaceControl);
        Rect crop = calculateSnapshotCrop();
        Rect frame = calculateSnapshotFrame(crop);
        SurfaceControl.openTransaction();
        try {
            this.mChildSurfaceControl.show();
            this.mChildSurfaceControl.setWindowCrop(crop);
            this.mChildSurfaceControl.setPosition((float) frame.left, (float) frame.top);
            float scale = 1.0f / this.mSnapshot.getScale();
            this.mChildSurfaceControl.setMatrix(scale, 0.0f, 0.0f, scale);
            SurfaceControl.closeTransaction();
            surface.attachAndQueueBuffer(buffer);
            surface.release();
            Canvas c = this.mSurface.lockCanvas(null);
            drawBackgroundAndBars(c, frame);
            this.mSurface.unlockCanvasAndPost(c);
            this.mSurface.release();
        } catch (Throwable th) {
            SurfaceControl.closeTransaction();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    private void drawSizeScaledSnapshot(GraphicBuffer buffer) {
        SurfaceControl.Builder builder = new SurfaceControl.Builder(new SurfaceSession(this.mSurface));
        this.mChildSurfaceControl = builder.setName(this.mTitle + " - task-snapshot-surface").setSize(buffer.getWidth(), buffer.getHeight()).setFormat(buffer.getFormat()).build();
        Surface surface = new Surface();
        surface.copyFrom(this.mChildSurfaceControl);
        Rect crop = new Rect(0, 0, this.mSnapshot.getSnapshot().getWidth(), this.mSnapshot.getSnapshot().getHeight());
        Rect frame = new Rect(this.mFrame);
        SurfaceControl.openTransaction();
        try {
            this.mChildSurfaceControl.show();
            this.mChildSurfaceControl.setWindowCrop(crop);
            this.mChildSurfaceControl.setPosition((float) frame.left, (float) frame.top);
            float scale = 1.0f / this.mSnapshot.getScale();
            this.mChildSurfaceControl.setMatrix(scale, 0.0f, 0.0f, scale);
            SurfaceControl.closeTransaction();
            surface.attachAndQueueBuffer(buffer);
            surface.release();
            this.mSurface.release();
        } catch (Throwable th) {
            SurfaceControl.closeTransaction();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Rect calculateSnapshotCrop() {
        Rect rect = new Rect();
        int i = 0;
        rect.set(0, 0, this.mSnapshot.getSnapshot().getWidth(), this.mSnapshot.getSnapshot().getHeight());
        Rect insets = this.mSnapshot.getContentInsets();
        boolean isTop = this.mTaskBounds.top == 0 && this.mFrame.top == 0;
        int scale = (int) (((float) insets.left) * this.mSnapshot.getScale());
        if (!isTop) {
            i = (int) (((float) insets.top) * this.mSnapshot.getScale());
        }
        rect.inset(scale, i, (int) (((float) insets.right) * this.mSnapshot.getScale()), (int) (((float) insets.bottom) * this.mSnapshot.getScale()));
        return rect;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Rect calculateSnapshotFrame(Rect crop) {
        Rect frame = new Rect(crop);
        float scale = this.mSnapshot.getScale();
        frame.scale(1.0f / scale);
        frame.offsetTo((int) (((float) (-crop.left)) / scale), (int) (((float) (-crop.top)) / scale));
        frame.offset(DecorView.getColorViewLeftInset(this.mStableInsets.left, this.mContentInsets.left), 0);
        return frame;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void drawBackgroundAndBars(Canvas c, Rect frame) {
        float height;
        Rect rect = frame;
        int statusBarHeight = this.mSystemBarBackgroundPainter.getStatusBarColorViewHeight();
        boolean z = false;
        boolean fillHorizontally = c.getWidth() > rect.right;
        if (c.getHeight() > rect.bottom) {
            z = true;
        }
        boolean fillVertically = z;
        if (fillHorizontally) {
            float f = (float) rect.right;
            float f2 = Color.alpha(this.mStatusBarColor) == 255 ? (float) statusBarHeight : 0.0f;
            float width = (float) c.getWidth();
            if (fillVertically) {
                height = (float) rect.bottom;
            } else {
                height = (float) c.getHeight();
            }
            c.drawRect(f, f2, width, height, this.mBackgroundPaint);
        }
        if (fillVertically) {
            c.drawRect(0.0f, (float) rect.bottom, (float) c.getWidth(), (float) c.getHeight(), this.mBackgroundPaint);
        }
        this.mSystemBarBackgroundPainter.drawDecors(c, rect);
    }

    /* access modifiers changed from: private */
    public void reportDrawn() {
        try {
            this.mSession.finishDrawing(this.mWindow);
        } catch (RemoteException e) {
        }
    }
}
