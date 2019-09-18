package android.app;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.hardware.input.InputManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import dalvik.system.CloseGuard;
import java.util.List;

public class ActivityView extends ViewGroup {
    private static final String DISPLAY_NAME = "ActivityViewVirtualDisplay";
    private static final String TAG = "ActivityView";
    /* access modifiers changed from: private */
    public IActivityManager mActivityManager;
    /* access modifiers changed from: private */
    public StateCallback mActivityViewCallback;
    private final CloseGuard mGuard;
    private IInputForwarder mInputForwarder;
    private final int[] mLocationOnScreen;
    private boolean mOpened;
    /* access modifiers changed from: private */
    public Surface mSurface;
    private final SurfaceCallback mSurfaceCallback;
    /* access modifiers changed from: private */
    public final SurfaceView mSurfaceView;
    private TaskStackListener mTaskStackListener;
    /* access modifiers changed from: private */
    public VirtualDisplay mVirtualDisplay;

    public static abstract class StateCallback {
        public abstract void onActivityViewDestroyed(ActivityView activityView);

        public abstract void onActivityViewReady(ActivityView activityView);

        public void onTaskMovedToFront(ActivityManager.StackInfo stackInfo) {
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        private SurfaceCallback() {
        }

        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Surface unused = ActivityView.this.mSurface = ActivityView.this.mSurfaceView.getHolder().getSurface();
            if (ActivityView.this.mVirtualDisplay == null) {
                ActivityView.this.initVirtualDisplay();
                if (!(ActivityView.this.mVirtualDisplay == null || ActivityView.this.mActivityViewCallback == null)) {
                    ActivityView.this.mActivityViewCallback.onActivityViewReady(ActivityView.this);
                }
            } else {
                ActivityView.this.mVirtualDisplay.setSurface(surfaceHolder.getSurface());
            }
            ActivityView.this.updateLocation();
        }

        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            if (ActivityView.this.mVirtualDisplay != null) {
                ActivityView.this.mVirtualDisplay.resize(width, height, ActivityView.this.getBaseDisplayDensity());
            }
            ActivityView.this.updateLocation();
        }

        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            ActivityView.this.mSurface.release();
            Surface unused = ActivityView.this.mSurface = null;
            if (ActivityView.this.mVirtualDisplay != null) {
                ActivityView.this.mVirtualDisplay.setSurface(null);
            }
            ActivityView.this.cleanTapExcludeRegion();
        }
    }

    private class TaskStackListenerImpl extends TaskStackListener {
        private TaskStackListenerImpl() {
        }

        public void onTaskDescriptionChanged(int taskId, ActivityManager.TaskDescription td) throws RemoteException {
            if (ActivityView.this.mVirtualDisplay != null) {
                ActivityManager.StackInfo stackInfo = getTopMostStackInfo();
                if (stackInfo != null && taskId == stackInfo.taskIds[stackInfo.taskIds.length - 1]) {
                    ActivityView.this.mSurfaceView.setResizeBackgroundColor(td.getBackgroundColor());
                }
            }
        }

        public void onTaskMovedToFront(int taskId) throws RemoteException {
            if (ActivityView.this.mActivityViewCallback != null) {
                ActivityManager.StackInfo stackInfo = getTopMostStackInfo();
                if (stackInfo != null && taskId == stackInfo.taskIds[stackInfo.taskIds.length - 1]) {
                    ActivityView.this.mActivityViewCallback.onTaskMovedToFront(stackInfo);
                }
            }
        }

        private ActivityManager.StackInfo getTopMostStackInfo() throws RemoteException {
            int displayId = ActivityView.this.mVirtualDisplay.getDisplay().getDisplayId();
            List<ActivityManager.StackInfo> stackInfoList = ActivityView.this.mActivityManager.getAllStackInfos();
            int stackCount = stackInfoList.size();
            for (int i = 0; i < stackCount; i++) {
                ActivityManager.StackInfo stackInfo = stackInfoList.get(i);
                if (stackInfo.displayId == displayId) {
                    return stackInfo;
                }
            }
            return null;
        }
    }

    public ActivityView(Context context) {
        this(context, null);
    }

    public ActivityView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActivityView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mLocationOnScreen = new int[2];
        this.mGuard = CloseGuard.get();
        this.mActivityManager = ActivityManager.getService();
        this.mSurfaceView = new SurfaceView(context);
        this.mSurfaceCallback = new SurfaceCallback();
        this.mSurfaceView.getHolder().addCallback(this.mSurfaceCallback);
        addView(this.mSurfaceView);
        this.mOpened = true;
        this.mGuard.open("release");
    }

    public void setCallback(StateCallback callback) {
        this.mActivityViewCallback = callback;
        if (this.mVirtualDisplay != null && this.mActivityViewCallback != null) {
            this.mActivityViewCallback.onActivityViewReady(this);
        }
    }

    public void startActivity(Intent intent) {
        getContext().startActivity(intent, prepareActivityOptions().toBundle());
    }

    public void startActivity(Intent intent, UserHandle user) {
        getContext().startActivityAsUser(intent, prepareActivityOptions().toBundle(), user);
    }

    public void startActivity(PendingIntent pendingIntent) {
        try {
            pendingIntent.send(null, 0, null, null, null, null, prepareActivityOptions().toBundle());
        } catch (PendingIntent.CanceledException e) {
            throw new RuntimeException(e);
        }
    }

    private ActivityOptions prepareActivityOptions() {
        if (this.mVirtualDisplay != null) {
            ActivityOptions options = ActivityOptions.makeBasic();
            options.setLaunchDisplayId(this.mVirtualDisplay.getDisplay().getDisplayId());
            return options;
        }
        throw new IllegalStateException("Trying to start activity before ActivityView is ready.");
    }

    public void release() {
        if (this.mVirtualDisplay != null) {
            performRelease();
            return;
        }
        throw new IllegalStateException("Trying to release container that is not initialized.");
    }

    public void onLocationChanged() {
        updateLocation();
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        this.mSurfaceView.layout(0, 0, r - l, b - t);
    }

    /* access modifiers changed from: private */
    public void updateLocation() {
        try {
            getLocationOnScreen(this.mLocationOnScreen);
            WindowManagerGlobal.getWindowSession().updateTapExcludeRegion(getWindow(), hashCode(), this.mLocationOnScreen[0], this.mLocationOnScreen[1], getWidth(), getHeight());
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        return injectInputEvent(event) || super.onTouchEvent(event);
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        if (!event.isFromSource(2) || !injectInputEvent(event)) {
            return super.onGenericMotionEvent(event);
        }
        return true;
    }

    private boolean injectInputEvent(InputEvent event) {
        if (this.mInputForwarder != null) {
            try {
                return this.mInputForwarder.forwardEvent(event);
            } catch (RemoteException e) {
                e.rethrowAsRuntimeException();
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void initVirtualDisplay() {
        if (this.mVirtualDisplay == null) {
            int width = this.mSurfaceView.getWidth();
            int height = this.mSurfaceView.getHeight();
            this.mVirtualDisplay = ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).createVirtualDisplay("ActivityViewVirtualDisplay@" + System.identityHashCode(this), width, height, getBaseDisplayDensity(), this.mSurface, 9);
            if (this.mVirtualDisplay == null) {
                Log.e(TAG, "Failed to initialize ActivityView");
                return;
            }
            int displayId = this.mVirtualDisplay.getDisplay().getDisplayId();
            try {
                WindowManagerGlobal.getWindowManagerService().dontOverrideDisplayInfo(displayId);
            } catch (RemoteException e) {
                e.rethrowAsRuntimeException();
            }
            this.mInputForwarder = InputManager.getInstance().createInputForwarder(displayId);
            this.mTaskStackListener = new TaskStackListenerImpl();
            try {
                this.mActivityManager.registerTaskStackListener(this.mTaskStackListener);
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to register task stack listener", e2);
            }
            return;
        }
        throw new IllegalStateException("Trying to initialize for the second time.");
    }

    private void performRelease() {
        boolean displayReleased;
        if (this.mOpened) {
            this.mSurfaceView.getHolder().removeCallback(this.mSurfaceCallback);
            if (this.mInputForwarder != null) {
                this.mInputForwarder = null;
            }
            cleanTapExcludeRegion();
            if (this.mTaskStackListener != null) {
                try {
                    this.mActivityManager.unregisterTaskStackListener(this.mTaskStackListener);
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to unregister task stack listener", e);
                }
                this.mTaskStackListener = null;
            }
            if (this.mVirtualDisplay != null) {
                this.mVirtualDisplay.release();
                this.mVirtualDisplay = null;
                displayReleased = true;
            } else {
                displayReleased = false;
            }
            if (this.mSurface != null) {
                this.mSurface.release();
                this.mSurface = null;
            }
            if (displayReleased && this.mActivityViewCallback != null) {
                this.mActivityViewCallback.onActivityViewDestroyed(this);
            }
            this.mGuard.close();
            this.mOpened = false;
        }
    }

    /* access modifiers changed from: private */
    public void cleanTapExcludeRegion() {
        try {
            WindowManagerGlobal.getWindowSession().updateTapExcludeRegion(getWindow(), hashCode(), 0, 0, 0, 0);
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
    }

    /* access modifiers changed from: private */
    public int getBaseDisplayDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService(WindowManager.class)).getDefaultDisplay().getMetrics(metrics);
        return metrics.densityDpi;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mGuard != null) {
                this.mGuard.warnIfOpen();
                performRelease();
            }
        } finally {
            super.finalize();
        }
    }
}
