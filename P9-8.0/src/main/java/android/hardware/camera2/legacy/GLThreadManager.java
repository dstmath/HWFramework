package android.hardware.camera2.legacy;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.legacy.RequestThreadManager.FpsCounter;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.Surface;
import com.android.internal.util.Preconditions;
import java.util.Collection;

public class GLThreadManager {
    private static final boolean DEBUG = false;
    private static final int MSG_ALLOW_FRAMES = 5;
    private static final int MSG_CLEANUP = 3;
    private static final int MSG_DROP_FRAMES = 4;
    private static final int MSG_NEW_CONFIGURATION = 1;
    private static final int MSG_NEW_FRAME = 2;
    private final String TAG;
    private CaptureCollector mCaptureCollector;
    private final CameraDeviceState mDeviceState;
    private final Callback mGLHandlerCb = new Callback() {
        private boolean mCleanup = false;
        private boolean mConfigured = false;
        private boolean mDroppingFrames = false;

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean handleMessage(Message msg) {
            if (this.mCleanup) {
                return true;
            }
            try {
                switch (msg.what) {
                    case -1:
                        break;
                    case 1:
                        ConfigureHolder configure = msg.obj;
                        GLThreadManager.this.mTextureRenderer.cleanupEGLContext();
                        GLThreadManager.this.mTextureRenderer.configureSurfaces(configure.surfaces);
                        GLThreadManager.this.mCaptureCollector = (CaptureCollector) Preconditions.checkNotNull(configure.collector);
                        configure.condition.open();
                        this.mConfigured = true;
                        break;
                    case 2:
                        if (!this.mDroppingFrames) {
                            if (!this.mConfigured) {
                                Log.e(GLThreadManager.this.TAG, "Dropping frame, EGL context not configured!");
                            }
                            GLThreadManager.this.mTextureRenderer.drawIntoSurfaces(GLThreadManager.this.mCaptureCollector);
                            break;
                        }
                        Log.w(GLThreadManager.this.TAG, "Ignoring frame.");
                        break;
                    case 3:
                        GLThreadManager.this.mTextureRenderer.cleanupEGLContext();
                        this.mCleanup = true;
                        this.mConfigured = false;
                        break;
                    case 4:
                        this.mDroppingFrames = true;
                        break;
                    case 5:
                        this.mDroppingFrames = false;
                        break;
                    default:
                        Log.e(GLThreadManager.this.TAG, "Unhandled message " + msg.what + " on GLThread.");
                        break;
                }
            } catch (Exception e) {
                Log.e(GLThreadManager.this.TAG, "Received exception on GL render thread: ", e);
                GLThreadManager.this.mDeviceState.setError(1);
            }
            return true;
        }
    };
    private final RequestHandlerThread mGLHandlerThread;
    private final FpsCounter mPrevCounter = new FpsCounter("GL Preview Producer");
    private final SurfaceTextureRenderer mTextureRenderer;

    private static class ConfigureHolder {
        public final CaptureCollector collector;
        public final ConditionVariable condition;
        public final Collection<Pair<Surface, Size>> surfaces;

        public ConfigureHolder(ConditionVariable condition, Collection<Pair<Surface, Size>> surfaces, CaptureCollector collector) {
            this.condition = condition;
            this.surfaces = surfaces;
            this.collector = collector;
        }
    }

    public GLThreadManager(int cameraId, int facing, CameraDeviceState state) {
        this.mTextureRenderer = new SurfaceTextureRenderer(facing);
        this.TAG = String.format("CameraDeviceGLThread-%d", new Object[]{Integer.valueOf(cameraId)});
        this.mGLHandlerThread = new RequestHandlerThread(this.TAG, this.mGLHandlerCb);
        this.mDeviceState = state;
    }

    public void start() {
        this.mGLHandlerThread.start();
    }

    public void waitUntilStarted() {
        this.mGLHandlerThread.waitUntilStarted();
    }

    public void quit() {
        Handler handler = this.mGLHandlerThread.getHandler();
        handler.sendMessageAtFrontOfQueue(handler.obtainMessage(3));
        this.mGLHandlerThread.quitSafely();
        try {
            this.mGLHandlerThread.join();
        } catch (InterruptedException e) {
            Log.e(this.TAG, String.format("Thread %s (%d) interrupted while quitting.", new Object[]{this.mGLHandlerThread.getName(), Long.valueOf(this.mGLHandlerThread.getId())}));
        }
    }

    public void queueNewFrame() {
        Handler handler = this.mGLHandlerThread.getHandler();
        if (handler.hasMessages(2)) {
            Log.e(this.TAG, "GLThread dropping frame.  Not consuming frames quickly enough!");
        } else {
            handler.sendMessage(handler.obtainMessage(2));
        }
    }

    public void setConfigurationAndWait(Collection<Pair<Surface, Size>> surfaces, CaptureCollector collector) {
        Preconditions.checkNotNull(collector, "collector must not be null");
        Handler handler = this.mGLHandlerThread.getHandler();
        ConditionVariable condition = new ConditionVariable(false);
        handler.sendMessage(handler.obtainMessage(1, 0, 0, new ConfigureHolder(condition, surfaces, collector)));
        condition.block();
    }

    public SurfaceTexture getCurrentSurfaceTexture() {
        return this.mTextureRenderer.getSurfaceTexture();
    }

    public void ignoreNewFrames() {
        this.mGLHandlerThread.getHandler().sendEmptyMessage(4);
    }

    public void waitUntilIdle() {
        this.mGLHandlerThread.waitUntilIdle();
    }

    public void allowNewFrames() {
        this.mGLHandlerThread.getHandler().sendEmptyMessage(5);
    }
}
