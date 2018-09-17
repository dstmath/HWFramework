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
import android.rms.AppAssociate;
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
import java.util.concurrent.ThreadFactory;

public class ActivityView extends ViewGroup {
    private static final int CPU_COUNT = 0;
    private static final boolean DEBUG = false;
    private static final int KEEP_ALIVE = 1;
    private static final int MAXIMUM_POOL_SIZE = 0;
    private static final int MINIMUM_POOL_SIZE = 1;
    private static final int MSG_SET_SURFACE = 1;
    private static final String TAG = "ActivityView";
    private static final Executor sExecutor = null;
    private static final BlockingQueue<Runnable> sPoolWorkQueue = null;
    private static final ThreadFactory sThreadFactory = null;
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

    /* renamed from: android.app.ActivityView.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ boolean val$callback;
        final /* synthetic */ int val$densityDpi;
        final /* synthetic */ int val$height;
        final /* synthetic */ Surface val$surface;
        final /* synthetic */ int val$width;

        /* renamed from: android.app.ActivityView.2.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ Surface val$surface;

            AnonymousClass1(Surface val$surface) {
                this.val$surface = val$surface;
            }

            public void run() {
                if (ActivityView.this.mActivityViewCallback == null) {
                    return;
                }
                if (this.val$surface != null) {
                    ActivityView.this.mActivityViewCallback.onSurfaceAvailable(ActivityView.this);
                } else {
                    ActivityView.this.mActivityViewCallback.onSurfaceDestroyed(ActivityView.this);
                }
            }
        }

        AnonymousClass2(Surface val$surface, int val$width, int val$height, int val$densityDpi, boolean val$callback) {
            this.val$surface = val$surface;
            this.val$width = val$width;
            this.val$height = val$height;
            this.val$densityDpi = val$densityDpi;
            this.val$callback = val$callback;
        }

        public void run() {
            try {
                synchronized (ActivityView.this.mActivityContainerLock) {
                    if (ActivityView.this.mActivityContainer != null) {
                        ActivityView.this.mActivityContainer.setSurface(this.val$surface, this.val$width, this.val$height, this.val$densityDpi);
                    }
                }
                if (this.val$callback) {
                    ActivityView.this.post(new AnonymousClass1(this.val$surface));
                }
            } catch (RemoteException e) {
                throw new RuntimeException("ActivityView: Unable to set surface of ActivityContainer. ", e);
            }
        }
    }

    private static class ActivityContainerCallback extends Stub {
        private final WeakReference<ActivityView> mActivityViewWeakReference;

        /* renamed from: android.app.ActivityView.ActivityContainerCallback.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ ActivityView val$activityView;
            final /* synthetic */ WeakReference val$callbackRef;

            AnonymousClass1(WeakReference val$callbackRef, ActivityView val$activityView) {
                this.val$callbackRef = val$callbackRef;
                this.val$activityView = val$activityView;
            }

            public void run() {
                ActivityViewCallback callback = (ActivityViewCallback) this.val$callbackRef.get();
                if (callback != null) {
                    callback.onAllActivitiesComplete(this.val$activityView);
                }
            }
        }

        ActivityContainerCallback(ActivityView activityView) {
            this.mActivityViewWeakReference = new WeakReference(activityView);
        }

        public void setVisible(IBinder container, boolean visible) {
        }

        public void onAllActivitiesComplete(IBinder container) {
            ActivityView activityView = (ActivityView) this.mActivityViewWeakReference.get();
            if (activityView != null) {
                ActivityViewCallback callback = activityView.mActivityViewCallback;
                if (callback != null) {
                    activityView.post(new AnonymousClass1(new WeakReference(callback), activityView));
                }
            }
        }
    }

    private static class ActivityContainerWrapper {
        private final CloseGuard mGuard;
        private final IActivityContainer mIActivityContainer;
        boolean mOpened;

        ActivityContainerWrapper(IActivityContainer container) {
            this.mGuard = CloseGuard.get();
            this.mIActivityContainer = container;
            this.mOpened = true;
            this.mGuard.open("release");
        }

        void attachToDisplay(int displayId) {
            try {
                this.mIActivityContainer.attachToDisplay(displayId);
            } catch (RemoteException e) {
            }
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
                return ActivityView.DEBUG;
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
                    this.mOpened = ActivityView.DEBUG;
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

        /* renamed from: android.app.ActivityView.SerialExecutor.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ Runnable val$r;

            AnonymousClass1(Runnable val$r) {
                this.val$r = val$r;
            }

            public void run() {
                try {
                    this.val$r.run();
                } finally {
                    SerialExecutor.this.scheduleNext();
                }
            }
        }

        private SerialExecutor() {
            this.mTasks = new ArrayDeque();
        }

        public synchronized void execute(Runnable r) {
            this.mTasks.offer(new AnonymousClass1(r));
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.ActivityView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.ActivityView.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityView.<clinit>():void");
    }

    public ActivityView(Context context) {
        this(context, null);
    }

    public ActivityView(Context context, AttributeSet attrs) {
        this(context, attrs, MAXIMUM_POOL_SIZE);
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
            this.mActivityContainer = new ActivityContainerWrapper(ActivityManagerNative.getDefault().createVirtualActivityContainer(this.mActivity.getActivityToken(), new ActivityContainerCallback(this)));
            this.mTextureView = new TextureView(context);
            this.mTextureView.setSurfaceTextureListener(new ActivityViewSurfaceTextureListener());
            addView(this.mTextureView);
            WindowManager wm = (WindowManager) this.mActivity.getSystemService(AppAssociate.ASSOC_WINDOW);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            this.mDensityDpi = metrics.densityDpi;
            this.mLastVisibility = getVisibility();
        } catch (RemoteException e) {
            throw new RuntimeException("ActivityView: Unable to create ActivityContainer. " + e);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.mTextureView.layout(MAXIMUM_POOL_SIZE, MAXIMUM_POOL_SIZE, r - l, b - t);
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (this.mSurface != null && (visibility == 8 || this.mLastVisibility == 8)) {
            setSurfaceAsync(visibility == 8 ? null : this.mSurface, this.mWidth, this.mHeight, this.mDensityDpi, DEBUG);
        }
        this.mLastVisibility = visibility;
    }

    private boolean injectInputEvent(InputEvent event) {
        return this.mActivityContainer != null ? this.mActivityContainer.injectEvent(event) : DEBUG;
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
        return this.mSurface != null ? true : DEBUG;
    }

    public void startActivity(Intent intent) {
        if (this.mActivityContainer == null) {
            throw new IllegalStateException("Attempt to call startActivity after release");
        } else if (this.mSurface == null) {
            throw new IllegalStateException("Surface not yet created.");
        } else if (this.mActivityContainer.startActivity(intent) == -6) {
            throw new OperationCanceledException();
        }
    }

    public void startActivity(IntentSender intentSender) {
        if (this.mActivityContainer == null) {
            throw new IllegalStateException("Attempt to call startActivity after release");
        } else if (this.mSurface == null) {
            throw new IllegalStateException("Surface not yet created.");
        } else {
            if (this.mActivityContainer.startActivityIntentSender(intentSender.getTarget()) == -6) {
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
            if (this.mActivityContainer.startActivityIntentSender(pendingIntent.getTarget()) == -6) {
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
        this.mExecutor.execute(new AnonymousClass2(surface, width, height, densityDpi, callback));
    }

    public void setCallback(ActivityViewCallback callback) {
        this.mActivityViewCallback = callback;
        if (this.mSurface != null) {
            this.mActivityViewCallback.onSurfaceAvailable(this);
        }
    }
}
