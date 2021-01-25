package com.huawei.server.fingerprint.fingerprintanimation;

import android.app.ActivityManager;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import java.lang.ref.WeakReference;

public class WaterEffectView extends GLSurfaceView {
    private static final int ALPHA_SIZE = 8;
    private static final int BLUE_SIZE = 8;
    private static final long DEFAULT_REFRESH_PERIOD = 16;
    private static final int DEPTH_SIZE = 16;
    private static final int GL_CLIENT_VERSION = 3;
    private static final int GL_ES_VERSION = 196608;
    private static final int GREEN_SIZE = 8;
    private static final boolean IS_DEBUG = true;
    private static final boolean IS_DEBUG_PER = false;
    private static final int MSG_REQUEST_RENDER = 0;
    private static final int RED_SIZE = 8;
    private static final int STENCIL_SIZE = 0;
    private static final String TAG = "WaterEffectView";
    private RenderHandler mHandler;
    private HandlerThread mHandlerThread;
    private long mRefreshPeriod;
    private WaterEffectRender mRenderer;
    private final WeakReference<WaterEffectView> mThisWeakRef;

    public WaterEffectView(Context context) {
        this(context, null);
    }

    public WaterEffectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mRefreshPeriod = DEFAULT_REFRESH_PERIOD;
        this.mThisWeakRef = new WeakReference<>(this);
        init(context);
    }

    private void init(Context context) {
        if (context == null) {
            Log.w(TAG, "init: context is null, cannot finish init");
            return;
        }
        Object object = context.getSystemService("activity");
        if (!(object instanceof ActivityManager)) {
            Log.w(TAG, "init: get WINDOW_SERVICE failed");
            return;
        }
        ActivityManager activityManager = (ActivityManager) object;
        if (activityManager != null) {
            if (activityManager.getDeviceConfigurationInfo().reqGlEsVersion >= 196608) {
                setEGLContextClientVersion(3);
                setZOrderOnTop(true);
                getHolder().setFormat(1);
                setEGLConfigChooser(8, 8, 8, 8, 16, 0);
                this.mRenderer = new WaterEffectRender(context);
                setRenderer(this.mRenderer);
                setRenderMode(0);
            }
        }
    }

    public void setYoffset(float value) {
        WaterEffectRender waterEffectRender = this.mRenderer;
        if (waterEffectRender != null) {
            waterEffectRender.setYoffset(value);
        }
    }

    public void setRefreshPeriod(long refreshPeriod) {
        this.mRefreshPeriod = refreshPeriod;
    }

    public long getRefreshPeriod() {
        return this.mRefreshPeriod;
    }

    public void playAnim() {
        WaterEffectRender waterEffectRender = this.mRenderer;
        if (waterEffectRender != null) {
            waterEffectRender.playAnim();
        }
    }

    public void playAnim(float x, float y) {
        WaterEffectRender waterEffectRender = this.mRenderer;
        if (waterEffectRender != null) {
            waterEffectRender.playAnim(x, y);
        }
    }

    public void pauseAnim() {
        WaterEffectRender waterEffectRender = this.mRenderer;
        if (waterEffectRender != null) {
            waterEffectRender.pauseAnim();
        }
    }

    public void clearAnim() {
        WaterEffectRender waterEffectRender = this.mRenderer;
        if (waterEffectRender != null) {
            waterEffectRender.clearAnim();
        }
    }

    public WaterEffectRender getRenderer() {
        return this.mRenderer;
    }

    @Override // android.opengl.GLSurfaceView
    public void setRenderer(GLSurfaceView.Renderer renderer) {
        super.setRenderer(renderer);
        if (renderer instanceof WaterEffectRender) {
            this.mRenderer = (WaterEffectRender) renderer;
            return;
        }
        throw new RuntimeException("You must use WaterEffectRenderer");
    }

    @Override // android.opengl.GLSurfaceView
    public void requestRender() {
        WaterEffectRender waterEffectRender = this.mRenderer;
        if (waterEffectRender != null && waterEffectRender.shouldRequest()) {
            super.requestRender();
        }
    }

    @Override // android.opengl.GLSurfaceView
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        WaterEffectRender waterEffectRender = this.mRenderer;
        if (waterEffectRender != null) {
            waterEffectRender.onResume();
        }
        quitHandlerThread();
        if (isRenderWhenDirty()) {
            this.mHandlerThread = new HandlerThread("WaterEffect-RenderHandler");
            this.mHandlerThread.start();
            this.mHandler = new RenderHandler(this.mHandlerThread.getLooper(), this.mThisWeakRef);
            this.mHandler.removeMessages(0);
            this.mHandler.sendEmptyMessage(0);
        }
    }

    @Override // android.opengl.GLSurfaceView
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        if (isRenderWhenDirty()) {
            this.mHandler.removeMessages(0);
        }
        WaterEffectRender waterEffectRender = this.mRenderer;
        if (waterEffectRender != null) {
            waterEffectRender.onPause();
        }
        quitHandlerThread();
    }

    private void quitHandlerThread() {
        HandlerThread handlerThread = this.mHandlerThread;
        if (handlerThread != null) {
            handlerThread.quitSafely();
            this.mHandlerThread = null;
        }
        RenderHandler renderHandler = this.mHandler;
        if (renderHandler != null) {
            renderHandler.setQuitting(true);
            this.mHandler = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isRenderWhenDirty() {
        return getRenderMode() == 0;
    }

    /* access modifiers changed from: private */
    public static class RenderHandler extends Handler {
        private boolean isQuitting;
        private WeakReference<WaterEffectView> mViewRef;

        private RenderHandler(Looper looper, WeakReference<WaterEffectView> viewRef) {
            super(looper);
            this.mViewRef = viewRef;
            this.isQuitting = false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setQuitting(boolean isQuit) {
            this.isQuitting = isQuit;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            WaterEffectView view;
            if (msg.what == 0 && (view = this.mViewRef.get()) != null && view.isRenderWhenDirty()) {
                view.requestRender();
                removeMessages(0);
                if (!this.isQuitting) {
                    sendEmptyMessageDelayed(0, view.getRefreshPeriod());
                }
            }
        }
    }
}
