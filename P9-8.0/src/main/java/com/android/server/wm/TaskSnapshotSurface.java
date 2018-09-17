package com.android.server.wm;

import android.app.ActivityManager.TaskDescription;
import android.app.ActivityManager.TaskSnapshot;
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
import android.view.IWindowSession;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerPolicy.StartingSurface;
import com.android.internal.policy.DecorView;
import com.android.internal.view.BaseIWindow;

class TaskSnapshotSurface implements StartingSurface {
    private static final int FLAG_INHERIT_EXCLUDES = 830922808;
    private static final int MSG_REPORT_DRAW = 0;
    private static final int PRIVATE_FLAG_INHERITS = 131072;
    private static final long SIZE_MISMATCH_MINIMUM_TIME_MS = 450;
    private static final String TAG = "WindowManager";
    private static final String TITLE_FORMAT = "SnapshotStartingWindow for taskId=%s";
    private static Handler sHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    boolean hasDrawn;
                    TaskSnapshotSurface surface = msg.obj;
                    synchronized (surface.mService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            hasDrawn = surface.mHasDrawn;
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    if (hasDrawn) {
                        surface.reportDrawn();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private final Paint mBackgroundPaint = new Paint();
    private SurfaceControl mChildSurfaceControl;
    private final Rect mContentInsets = new Rect();
    private final Rect mFrame = new Rect();
    private final Handler mHandler;
    private boolean mHasDrawn;
    private final int mOrientationOnCreation;
    private final WindowManagerService mService;
    private final IWindowSession mSession;
    private long mShownTime;
    private boolean mSizeMismatch;
    private TaskSnapshot mSnapshot;
    private final Rect mStableInsets = new Rect();
    private final int mStatusBarColor;
    private final Surface mSurface;
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
            this.mStatusBarColor = DecorView.calculateStatusBarColor(windowFlags, ActivityThread.currentActivityThread().getSystemUiContext().getColor(17170768), statusBarColor);
            this.mNavigationBarColor = navigationBarColor;
            this.mStatusBarPaint.setColor(this.mStatusBarColor);
            this.mNavigationBarPaint.setColor(navigationBarColor);
        }

        void setInsets(Rect contentInsets, Rect stableInsets) {
            this.mContentInsets.set(contentInsets);
            this.mStableInsets.set(stableInsets);
        }

        int getStatusBarColorViewHeight() {
            if (DecorView.STATUS_BAR_COLOR_VIEW_ATTRIBUTES.isVisible(this.mSysUiVis, this.mStatusBarColor, this.mWindowFlags, (this.mWindowPrivateFlags & 131072) != 0)) {
                return DecorView.getColorViewTopInset(this.mStableInsets.top, this.mContentInsets.top);
            }
            return 0;
        }

        private boolean isNavigationBarColorViewVisible() {
            return DecorView.NAVIGATION_BAR_COLOR_VIEW_ATTRIBUTES.isVisible(this.mSysUiVis, this.mNavigationBarColor, this.mWindowFlags, false);
        }

        void drawDecors(Canvas c, Rect alreadyDrawnFrame) {
            drawStatusBarBackground(c, alreadyDrawnFrame, getStatusBarColorViewHeight());
            drawNavigationBarBackground(c);
        }

        void drawStatusBarBackground(Canvas c, Rect alreadyDrawnFrame, int statusBarHeight) {
            if (statusBarHeight <= 0) {
                return;
            }
            if (alreadyDrawnFrame == null || c.getWidth() > alreadyDrawnFrame.right) {
                c.drawRect((float) (alreadyDrawnFrame != null ? alreadyDrawnFrame.right : 0), 0.0f, (float) (c.getWidth() - DecorView.getColorViewRightInset(this.mStableInsets.right, this.mContentInsets.right)), (float) statusBarHeight, this.mStatusBarPaint);
            }
        }

        void drawNavigationBarBackground(Canvas c) {
            Rect navigationBarRect = new Rect();
            DecorView.getNavigationBarRect(c.getWidth(), c.getHeight(), this.mStableInsets, this.mContentInsets, navigationBarRect);
            if (isNavigationBarColorViewVisible() && (navigationBarRect.isEmpty() ^ 1) != 0) {
                c.drawRect(navigationBarRect, this.mNavigationBarPaint);
            }
        }
    }

    static class Window extends BaseIWindow {
        private TaskSnapshotSurface mOuter;

        Window() {
        }

        public void setOuter(TaskSnapshotSurface outer) {
            this.mOuter = outer;
        }

        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeNavBar, int displayId) {
            if (!(mergedConfiguration == null || this.mOuter == null || this.mOuter.mOrientationOnCreation == mergedConfiguration.getMergedConfiguration().orientation)) {
                Handler -get3 = TaskSnapshotSurface.sHandler;
                TaskSnapshotSurface taskSnapshotSurface = this.mOuter;
                taskSnapshotSurface.getClass();
                -get3.post(new -$Lambda$av67reroNHKHPez4Kh4-Vr3uM2Q(taskSnapshotSurface));
            }
            if (reportDraw) {
                TaskSnapshotSurface.sHandler.obtainMessage(0, this.mOuter).sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:18:0x00eb, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:20:?, code:
            r43 = r3.addToDisplay(r4, r4.mSeq, r6, 0, r47.getDisplayContent().getDisplayId(), r9, r9, r9, null);
     */
    /* JADX WARNING: Missing block: B:21:0x0100, code:
            if (r43 >= 0) goto L_0x0129;
     */
    /* JADX WARNING: Missing block: B:22:0x0102, code:
            android.util.Slog.w(TAG, "Failed to add snapshot starting window res=" + r43);
     */
    /* JADX WARNING: Missing block: B:24:0x011e, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static TaskSnapshotSurface create(WindowManagerService service, AppWindowToken token, TaskSnapshot snapshot) {
        int sysUiVis;
        int windowFlags;
        int windowPrivateFlags;
        Rect taskBounds;
        int currentOrientation;
        LayoutParams layoutParams = new LayoutParams();
        Window window = new Window();
        IWindowSession session = WindowManagerGlobal.getWindowSession();
        window.setSession(session);
        Surface surface = new Surface();
        Rect tmpRect = new Rect();
        Rect tmpFrame = new Rect();
        Rect tmpContentInsets = new Rect();
        Rect tmpStableInsets = new Rect();
        MergedConfiguration tmpMergedConfiguration = new MergedConfiguration();
        int backgroundColor = -1;
        int statusBarColor = 0;
        int navigationBarColor = 0;
        synchronized (service.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                WindowState mainWindow = token.findMainWindow();
                if (mainWindow == null) {
                    Slog.w(TAG, "TaskSnapshotSurface.create: Failed to find main window for token=" + token);
                } else {
                    sysUiVis = mainWindow.getSystemUiVisibility();
                    windowFlags = mainWindow.getAttrs().flags;
                    windowPrivateFlags = mainWindow.getAttrs().privateFlags;
                    layoutParams.type = 3;
                    layoutParams.format = snapshot.getSnapshot().getFormat();
                    layoutParams.flags = ((-830922809 & windowFlags) | 8) | 16;
                    layoutParams.privateFlags = 131072 & windowPrivateFlags;
                    layoutParams.token = token.token;
                    layoutParams.width = -1;
                    layoutParams.height = -1;
                    layoutParams.systemUiVisibility = sysUiVis;
                    Task task = token.getTask();
                    if (task != null) {
                        layoutParams.setTitle(String.format(TITLE_FORMAT, new Object[]{Integer.valueOf(task.mTaskId)}));
                        TaskDescription taskDescription = task.getTaskDescription();
                        if (taskDescription != null) {
                            backgroundColor = taskDescription.getBackgroundColor();
                            statusBarColor = taskDescription.getStatusBarColor();
                            navigationBarColor = taskDescription.getNavigationBarColor();
                        }
                        taskBounds = new Rect();
                        task.getBounds(taskBounds);
                    } else {
                        taskBounds = null;
                    }
                    currentOrientation = mainWindow.getConfiguration().orientation;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        TaskSnapshotSurface snapshotSurface;
        return null;
        snapshotSurface.setFrames(tmpFrame, tmpContentInsets, tmpStableInsets);
        snapshotSurface.drawSnapshot();
        return snapshotSurface;
        snapshotSurface = new TaskSnapshotSurface(service, window, surface, snapshot, layoutParams.getTitle(), backgroundColor, statusBarColor, navigationBarColor, sysUiVis, windowFlags, windowPrivateFlags, taskBounds, currentOrientation);
        window.setOuter(snapshotSurface);
        try {
            session.relayout(window, window.mSeq, layoutParams, -1, -1, 0, 0, tmpFrame, tmpRect, tmpContentInsets, tmpRect, tmpStableInsets, tmpRect, tmpRect, tmpMergedConfiguration, surface);
        } catch (RemoteException e) {
        }
        snapshotSurface.setFrames(tmpFrame, tmpContentInsets, tmpStableInsets);
        snapshotSurface.drawSnapshot();
        return snapshotSurface;
    }

    TaskSnapshotSurface(WindowManagerService service, Window window, Surface surface, TaskSnapshot snapshot, CharSequence title, int backgroundColor, int statusBarColor, int navigationBarColor, int sysUiVis, int windowFlags, int windowPrivateFlags, Rect taskBounds, int currentOrientation) {
        this.mService = service;
        this.mHandler = new Handler(this.mService.mH.getLooper());
        this.mSession = WindowManagerGlobal.getWindowSession();
        this.mWindow = window;
        this.mSurface = surface;
        this.mSnapshot = snapshot;
        this.mTitle = title;
        Paint paint = this.mBackgroundPaint;
        if (backgroundColor == 0) {
            backgroundColor = -1;
        }
        paint.setColor(backgroundColor);
        this.mTaskBounds = taskBounds;
        this.mSystemBarBackgroundPainter = new SystemBarBackgroundPainter(windowFlags, windowPrivateFlags, sysUiVis, statusBarColor, navigationBarColor);
        this.mStatusBarColor = statusBarColor;
        this.mOrientationOnCreation = currentOrientation;
    }

    /* JADX WARNING: Missing block: B:11:0x002d, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            r10.mSession.remove(r10.mWindow);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    /* renamed from: remove */
    public void -com_android_server_wm_TaskSnapshotSurface-mthref-0() {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long now = SystemClock.uptimeMillis();
                if (!this.mSizeMismatch || now - this.mShownTime >= SIZE_MISMATCH_MINIMUM_TIME_MS) {
                } else {
                    this.mHandler.postAtTime(new com.android.server.wm.-$Lambda$av67reroNHKHPez4Kh4-Vr3uM2Q.AnonymousClass1(this), this.mShownTime + SIZE_MISMATCH_MINIMUM_TIME_MS);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void setFrames(Rect frame, Rect contentInsets, Rect stableInsets) {
        boolean z = true;
        this.mFrame.set(frame);
        this.mContentInsets.set(contentInsets);
        this.mStableInsets.set(stableInsets);
        if (this.mFrame.width() == this.mSnapshot.getSnapshot().getWidth() && this.mFrame.height() == this.mSnapshot.getSnapshot().getHeight()) {
            z = false;
        }
        this.mSizeMismatch = z;
        this.mSystemBarBackgroundPainter.setInsets(contentInsets, stableInsets);
    }

    private void drawSnapshot() {
        GraphicBuffer buffer = this.mSnapshot.getSnapshot();
        if (this.mSizeMismatch) {
            drawSizeMismatchSnapshot(buffer);
        } else {
            drawSizeMatchSnapshot(buffer);
        }
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mShownTime = SystemClock.uptimeMillis();
                this.mHasDrawn = true;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        reportDrawn();
        this.mSnapshot = null;
    }

    private void drawSizeMatchSnapshot(GraphicBuffer buffer) {
        this.mSurface.attachAndQueueBuffer(buffer);
        this.mSurface.release();
    }

    private void drawSizeMismatchSnapshot(GraphicBuffer buffer) {
        this.mChildSurfaceControl = new SurfaceControl(new SurfaceSession(this.mSurface), this.mTitle + " - task-snapshot-surface", buffer.getWidth(), buffer.getHeight(), buffer.getFormat(), 4);
        Surface surface = new Surface();
        surface.copyFrom(this.mChildSurfaceControl);
        Rect crop = calculateSnapshotCrop();
        Rect frame = calculateSnapshotFrame(crop);
        SurfaceControl.openTransaction();
        try {
            this.mChildSurfaceControl.show();
            this.mChildSurfaceControl.setWindowCrop(crop);
            this.mChildSurfaceControl.setPosition((float) frame.left, (float) frame.top);
            surface.attachAndQueueBuffer(buffer);
            surface.release();
            Canvas c = this.mSurface.lockCanvas(null);
            drawBackgroundAndBars(c, frame);
            this.mSurface.unlockCanvasAndPost(c);
            this.mSurface.release();
        } finally {
            SurfaceControl.closeTransaction();
        }
    }

    Rect calculateSnapshotCrop() {
        int i = 0;
        Rect rect = new Rect();
        rect.set(0, 0, this.mSnapshot.getSnapshot().getWidth(), this.mSnapshot.getSnapshot().getHeight());
        Rect insets = this.mSnapshot.getContentInsets();
        int i2 = insets.left;
        if (this.mTaskBounds.top != 0) {
            i = insets.top;
        }
        rect.inset(i2, i, insets.right, insets.bottom);
        return rect;
    }

    Rect calculateSnapshotFrame(Rect crop) {
        Rect frame = new Rect(crop);
        frame.offsetTo(-crop.left, -crop.top);
        frame.offset(DecorView.getColorViewLeftInset(this.mStableInsets.left, this.mContentInsets.left), 0);
        return frame;
    }

    void drawBackgroundAndBars(Canvas c, Rect frame) {
        int statusBarHeight = this.mSystemBarBackgroundPainter.getStatusBarColorViewHeight();
        boolean fillHorizontally = c.getWidth() > frame.right;
        boolean fillVertically = c.getHeight() > frame.bottom;
        if (fillHorizontally) {
            int i;
            float f = (float) frame.right;
            if (Color.alpha(this.mStatusBarColor) != 255) {
                statusBarHeight = 0;
            }
            float f2 = (float) statusBarHeight;
            float width = (float) c.getWidth();
            if (fillVertically) {
                i = frame.bottom;
            } else {
                i = c.getHeight();
            }
            c.drawRect(f, f2, width, (float) i, this.mBackgroundPaint);
        }
        if (fillVertically) {
            c.drawRect(0.0f, (float) frame.bottom, (float) c.getWidth(), (float) c.getHeight(), this.mBackgroundPaint);
        }
        this.mSystemBarBackgroundPainter.drawDecors(c, frame);
    }

    private void reportDrawn() {
        try {
            this.mSession.finishDrawing(this.mWindow);
        } catch (RemoteException e) {
        }
    }
}
