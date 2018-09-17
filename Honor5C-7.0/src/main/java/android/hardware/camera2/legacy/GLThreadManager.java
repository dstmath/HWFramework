package android.hardware.camera2.legacy;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.legacy.RequestThreadManager.FpsCounter;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.speech.tts.TextToSpeech;
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
    private final Callback mGLHandlerCb;
    private final RequestHandlerThread mGLHandlerThread;
    private final FpsCounter mPrevCounter;
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
        this.mPrevCounter = new FpsCounter("GL Preview Producer");
        this.mGLHandlerCb = new Callback() {
            private boolean mCleanup;
            private boolean mConfigured;
            private boolean mDroppingFrames;

            {
                this.mCleanup = GLThreadManager.DEBUG;
                this.mConfigured = GLThreadManager.DEBUG;
                this.mDroppingFrames = GLThreadManager.DEBUG;
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public boolean handleMessage(Message msg) {
                if (this.mCleanup) {
                    return true;
                }
                try {
                    switch (msg.what) {
                        case TextToSpeech.LANG_MISSING_DATA /*-1*/:
                            break;
                        case GLThreadManager.MSG_NEW_CONFIGURATION /*1*/:
                            ConfigureHolder configure = msg.obj;
                            GLThreadManager.this.mTextureRenderer.cleanupEGLContext();
                            GLThreadManager.this.mTextureRenderer.configureSurfaces(configure.surfaces);
                            GLThreadManager.this.mCaptureCollector = (CaptureCollector) Preconditions.checkNotNull(configure.collector);
                            configure.condition.open();
                            this.mConfigured = true;
                            break;
                        case GLThreadManager.MSG_NEW_FRAME /*2*/:
                            if (!this.mDroppingFrames) {
                                if (!this.mConfigured) {
                                    Log.e(GLThreadManager.this.TAG, "Dropping frame, EGL context not configured!");
                                }
                                GLThreadManager.this.mTextureRenderer.drawIntoSurfaces(GLThreadManager.this.mCaptureCollector);
                                break;
                            }
                            Log.w(GLThreadManager.this.TAG, "Ignoring frame.");
                            break;
                        case GLThreadManager.MSG_CLEANUP /*3*/:
                            GLThreadManager.this.mTextureRenderer.cleanupEGLContext();
                            this.mCleanup = true;
                            this.mConfigured = GLThreadManager.DEBUG;
                            break;
                        case GLThreadManager.MSG_DROP_FRAMES /*4*/:
                            this.mDroppingFrames = true;
                            break;
                        case GLThreadManager.MSG_ALLOW_FRAMES /*5*/:
                            this.mDroppingFrames = GLThreadManager.DEBUG;
                            break;
                        default:
                            Log.e(GLThreadManager.this.TAG, "Unhandled message " + msg.what + " on GLThread.");
                            break;
                    }
                } catch (Exception e) {
                    Log.e(GLThreadManager.this.TAG, "Received exception on GL render thread: ", e);
                    GLThreadManager.this.mDeviceState.setError(GLThreadManager.MSG_NEW_CONFIGURATION);
                }
                return true;
            }
        };
        this.mTextureRenderer = new SurfaceTextureRenderer(facing);
        Object[] objArr = new Object[MSG_NEW_CONFIGURATION];
        objArr[0] = Integer.valueOf(cameraId);
        this.TAG = String.format("CameraDeviceGLThread-%d", objArr);
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
        handler.sendMessageAtFrontOfQueue(handler.obtainMessage(MSG_CLEANUP));
        this.mGLHandlerThread.quitSafely();
        try {
            this.mGLHandlerThread.join();
        } catch (InterruptedException e) {
            String str = this.TAG;
            Object[] objArr = new Object[MSG_NEW_FRAME];
            objArr[0] = this.mGLHandlerThread.getName();
            objArr[MSG_NEW_CONFIGURATION] = Long.valueOf(this.mGLHandlerThread.getId());
            Log.e(str, String.format("Thread %s (%d) interrupted while quitting.", objArr));
        }
    }

    public void queueNewFrame() {
        Handler handler = this.mGLHandlerThread.getHandler();
        if (handler.hasMessages(MSG_NEW_FRAME)) {
            Log.e(this.TAG, "GLThread dropping frame.  Not consuming frames quickly enough!");
        } else {
            handler.sendMessage(handler.obtainMessage(MSG_NEW_FRAME));
        }
    }

    public void setConfigurationAndWait(Collection<Pair<Surface, Size>> surfaces, CaptureCollector collector) {
        Preconditions.checkNotNull(collector, "collector must not be null");
        Handler handler = this.mGLHandlerThread.getHandler();
        ConditionVariable condition = new ConditionVariable(DEBUG);
        handler.sendMessage(handler.obtainMessage(MSG_NEW_CONFIGURATION, 0, 0, new ConfigureHolder(condition, surfaces, collector)));
        condition.block();
    }

    public SurfaceTexture getCurrentSurfaceTexture() {
        return this.mTextureRenderer.getSurfaceTexture();
    }

    public void ignoreNewFrames() {
        this.mGLHandlerThread.getHandler().sendEmptyMessage(MSG_DROP_FRAMES);
    }

    public void waitUntilIdle() {
        this.mGLHandlerThread.waitUntilIdle();
    }

    public void allowNewFrames() {
        this.mGLHandlerThread.getHandler().sendEmptyMessage(MSG_ALLOW_FRAMES);
    }
}
