package android.app;

import android.app.IActivityContainerCallback.Stub;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.SurfaceTexture;
import android.os.IBinder;
import android.os.OperationCanceledException;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.internal.annotations.GuardedBy;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ActivityView extends ViewGroup {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final boolean DEBUG = false;
    private static final int KEEP_ALIVE = 1;
    private static final int MAXIMUM_POOL_SIZE = ((CPU_COUNT * 2) + 1);
    private static final int MINIMUM_POOL_SIZE = 1;
    private static final int MSG_SET_SURFACE = 1;
    private static final String TAG = "ActivityView";
    private static final Executor sExecutor = new ThreadPoolExecutor(1, MAXIMUM_POOL_SIZE, 1, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue(128);
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ActivityView #" + this.mCount.getAndIncrement());
        }
    };
    private Activity mActivity;
    @GuardedBy("mActivityContainerLock")
    private ActivityContainerWrapper mActivityContainer;
    private Object mActivityContainerLock;
    private ActivityViewCallback mActivityViewCallback;
    private final int mDensityDpi;
    private final SerialExecutor mExecutor;
    private int mHeight;
    private int mLastVisibility;
    private Surface mSurface;
    private final TextureView mTextureView;
    private int mWidth;

    private static class ActivityContainerCallback extends Stub {
        private final WeakReference<ActivityView> mActivityViewWeakReference;

        ActivityContainerCallback(ActivityView activityView) {
            this.mActivityViewWeakReference = new WeakReference(activityView);
        }

        public void setVisible(IBinder container, boolean visible) {
        }

        public void onAllActivitiesComplete(IBinder container) {
            final ActivityView activityView = (ActivityView) this.mActivityViewWeakReference.get();
            if (activityView != null) {
                ActivityViewCallback callback = activityView.mActivityViewCallback;
                if (callback != null) {
                    final WeakReference<ActivityViewCallback> callbackRef = new WeakReference(callback);
                    activityView.post(new Runnable() {
                        public void run() {
                            ActivityViewCallback callback = (ActivityViewCallback) callbackRef.get();
                            if (callback != null) {
                                callback.onAllActivitiesComplete(activityView);
                            }
                        }
                    });
                }
            }
        }
    }

    private static class ActivityContainerWrapper {
        private final CloseGuard mGuard = CloseGuard.get();
        private final IActivityContainer mIActivityContainer;
        boolean mOpened;

        ActivityContainerWrapper(IActivityContainer container) {
            this.mIActivityContainer = container;
            this.mOpened = true;
            this.mGuard.open("release");
        }

        void setSurface(Surface surface, int width, int height, int density) throws RemoteException {
            this.mIActivityContainer.setSurface(surface, width, height, density);
        }

        int startActivity(Intent intent) {
            try {
                return this.mIActivityContainer.startActivity(intent);
            } catch (RemoteException e) {
                throw new RuntimeException("ActivityView: Unable to startActivity. " + e);
            }
        }

        int startActivityIntentSender(IIntentSender intentSender) {
            try {
                return this.mIActivityContainer.startActivityIntentSender(intentSender);
            } catch (RemoteException e) {
                throw new RuntimeException("ActivityView: Unable to startActivity from IntentSender. " + e);
            }
        }

        int getDisplayId() {
            try {
                return this.mIActivityContainer.getDisplayId();
            } catch (RemoteException e) {
                return -1;
            }
        }

        boolean injectEvent(InputEvent event) {
            try {
                return this.mIActivityContainer.injectEvent(event);
            } catch (RemoteException e) {
                return false;
            }
        }

        void release() {
            synchronized (this.mGuard) {
                if (this.mOpened) {
                    try {
                        this.mIActivityContainer.release();
                        this.mGuard.close();
                    } catch (RemoteException e) {
                    }
                    this.mOpened = false;
                }
            }
        }

        protected void finalize() throws Throwable {
            try {
                if (this.mGuard != null) {
                    this.mGuard.warnIfOpen();
                    release();
                }
                super.finalize();
            } catch (Throwable th) {
                super.finalize();
            }
        }
    }

    public static abstract class ActivityViewCallback {
        public abstract void onAllActivitiesComplete(ActivityView activityView);

        public abstract void onSurfaceAvailable(ActivityView activityView);

        public abstract void onSurfaceDestroyed(ActivityView activityView);
    }

    private class ActivityViewSurfaceTextureListener implements SurfaceTextureListener {
        /* synthetic */ ActivityViewSurfaceTextureListener(ActivityView this$0, ActivityViewSurfaceTextureListener -this1) {
            this();
        }

        private ActivityViewSurfaceTextureListener() {
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            if (ActivityView.this.mActivityContainer != null) {
                ActivityView.this.mWidth = width;
                ActivityView.this.mHeight = height;
                ActivityView.this.mSurface = new Surface(surfaceTexture);
                ActivityView.this.setSurfaceAsync(ActivityView.this.mSurface, ActivityView.this.mWidth, ActivityView.this.mHeight, ActivityView.this.mDensityDpi, true);
            }
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            if (ActivityView.this.mActivityContainer != null) {
            }
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            if (ActivityView.this.mActivityContainer == null) {
                return true;
            }
            ActivityView.this.mSurface.release();
            ActivityView.this.mSurface = null;
            ActivityView.this.setSurfaceAsync(null, ActivityView.this.mWidth, ActivityView.this.mHeight, ActivityView.this.mDensityDpi, true);
            return true;
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    }

    private static class SerialExecutor implements Executor {
        private Runnable mActive;
        private final ArrayDeque<Runnable> mTasks;

        /* synthetic */ SerialExecutor(SerialExecutor -this0) {
            this();
        }

        private SerialExecutor() {
            this.mTasks = new ArrayDeque();
        }

        public synchronized void execute(final Runnable r) {
            this.mTasks.offer(new Runnable() {
                public void run() {
                    try {
                        r.run();
                    } finally {
                        SerialExecutor.this.scheduleNext();
                    }
                }
            });
            if (this.mActive == null) {
                scheduleNext();
            }
        }

        protected synchronized void scheduleNext() {
            Runnable runnable = (Runnable) this.mTasks.poll();
            this.mActive = runnable;
            if (runnable != null) {
                ActivityView.sExecutor.execute(this.mActive);
            }
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
        this.mExecutor = new SerialExecutor();
        this.mActivityContainerLock = new Object();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                this.mActivity = (Activity) context;
                break;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        if (this.mActivity == null) {
            throw new IllegalStateException("The ActivityView's Context is not an Activity.");
        }
        try {
            this.mActivityContainer = new ActivityContainerWrapper(ActivityManager.getService().createVirtualActivityContainer(this.mActivity.getActivityToken(), new ActivityContainerCallback(this)));
            this.mTextureView = new TextureView(context);
            this.mTextureView.setSurfaceTextureListener(new ActivityViewSurfaceTextureListener(this, null));
            addView(this.mTextureView);
            WindowManager wm = (WindowManager) this.mActivity.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            this.mDensityDpi = metrics.densityDpi;
            this.mLastVisibility = getVisibility();
        } catch (RemoteException e) {
            throw new RuntimeException("ActivityView: Unable to create ActivityContainer. " + e);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.mTextureView.layout(0, 0, r - l, b - t);
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (this.mSurface != null && (visibility == 8 || this.mLastVisibility == 8)) {
            setSurfaceAsync(visibility == 8 ? null : this.mSurface, this.mWidth, this.mHeight, this.mDensityDpi, false);
        }
        this.mLastVisibility = visibility;
    }

    private boolean injectInputEvent(InputEvent event) {
        return this.mActivityContainer != null ? this.mActivityContainer.injectEvent(event) : false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        return !injectInputEvent(event) ? super.onTouchEvent(event) : true;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        if (event.isFromSource(2) && injectInputEvent(event)) {
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    public void onAttachedToWindow() {
    }

    public void onDetachedFromWindow() {
    }

    public boolean isAttachedToDisplay() {
        return this.mSurface != null;
    }

    public void startActivity(Intent intent) {
        if (this.mActivityContainer == null) {
            throw new IllegalStateException("Attempt to call startActivity after release");
        } else if (this.mSurface == null) {
            throw new IllegalStateException("Surface not yet created.");
        } else if (this.mActivityContainer.startActivity(intent) == -96) {
            throw new OperationCanceledException();
        }
    }

    public void startActivity(IntentSender intentSender) {
        if (this.mActivityContainer == null) {
            throw new IllegalStateException("Attempt to call startActivity after release");
        } else if (this.mSurface == null) {
            throw new IllegalStateException("Surface not yet created.");
        } else {
            if (this.mActivityContainer.startActivityIntentSender(intentSender.getTarget()) == -96) {
                throw new OperationCanceledException();
            }
        }
    }

    public void startActivity(PendingIntent pendingIntent) {
        if (this.mActivityContainer == null) {
            throw new IllegalStateException("Attempt to call startActivity after release");
        } else if (this.mSurface == null) {
            throw new IllegalStateException("Surface not yet created.");
        } else {
            if (this.mActivityContainer.startActivityIntentSender(pendingIntent.getTarget()) == -96) {
                throw new OperationCanceledException();
            }
        }
    }

    public void release() {
        if (this.mActivityContainer == null) {
            Log.e(TAG, "Duplicate call to release");
            return;
        }
        synchronized (this.mActivityContainerLock) {
            this.mActivityContainer.release();
            this.mActivityContainer = null;
        }
        if (this.mSurface != null) {
            this.mSurface.release();
            this.mSurface = null;
        }
        this.mTextureView.setSurfaceTextureListener(null);
    }

    private void setSurfaceAsync(Surface surface, int width, int height, int densityDpi, boolean callback) {
        final Surface surface2 = surface;
        final int i = width;
        final int i2 = height;
        final int i3 = densityDpi;
        final boolean z = callback;
        this.mExecutor.execute(new Runnable() {
            public void run() {
                try {
                    synchronized (ActivityView.this.mActivityContainerLock) {
                        if (ActivityView.this.mActivityContainer != null) {
                            ActivityView.this.mActivityContainer.setSurface(surface2, i, i2, i3);
                        }
                    }
                    if (z) {
                        ActivityView activityView = ActivityView.this;
                        final Surface surface = surface2;
                        activityView.post(new Runnable() {
                            public void run() {
                                if (ActivityView.this.mActivityViewCallback == null) {
                                    return;
                                }
                                if (surface != null) {
                                    ActivityView.this.mActivityViewCallback.onSurfaceAvailable(ActivityView.this);
                                } else {
                                    ActivityView.this.mActivityViewCallback.onSurfaceDestroyed(ActivityView.this);
                                }
                            }
                        });
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException("ActivityView: Unable to set surface of ActivityContainer. ", e);
                }
            }
        });
    }

    public void setCallback(ActivityViewCallback callback) {
        this.mActivityViewCallback = callback;
        if (this.mSurface != null) {
            this.mActivityViewCallback.onSurfaceAvailable(this);
        }
    }
}
