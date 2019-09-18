package com.android.server.fingerprint.fingerprintAnimation;

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
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_PER = false;
    private static final long DEFAULT_REFRESH_PERIOD = 16;
    private static final int MSG_REQUEST_RENDER = 0;
    private static final String TAG = "WaterEffectView";
    private RenderHandler mHandler;
    private HandlerThread mHandlerThread;
    private long mRefreshPeriod;
    private WaterEffectRender mRenderer;
    private final WeakReference<WaterEffectView> mThisWeakRef;

    private static class RenderHandler extends Handler {
        private boolean mQuitting;
        private WeakReference<WaterEffectView> mViewRef;

        private RenderHandler(Looper looper, WeakReference<WaterEffectView> viewRef) {
            super(looper);
            this.mViewRef = viewRef;
            this.mQuitting = false;
        }

        /* access modifiers changed from: private */
        public void setQuitting(boolean quitting) {
            this.mQuitting = quitting;
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                WaterEffectView view = (WaterEffectView) this.mViewRef.get();
                if (view != null && view.isRenderWhenDirty()) {
                    view.requestRender();
                    removeMessages(0);
                    if (!this.mQuitting) {
                        sendEmptyMessageDelayed(0, view.getRefreshPeriod());
                    }
                }
            }
        }
    }

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
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
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

    public void setYOffset(float value) {
        if (this.mRenderer != null) {
            this.mRenderer.setYOffset(value);
        }
    }

    public void setRefreshPeriod(long refreshPeriod) {
        this.mRefreshPeriod = refreshPeriod;
    }

    public long getRefreshPeriod() {
        return this.mRefreshPeriod;
    }

    public void playAnim() {
        if (this.mRenderer != null) {
            this.mRenderer.playAnim();
        }
    }

    public void playAnim(float x, float y) {
        if (this.mRenderer != null) {
            this.mRenderer.playAnim(x, y);
        }
    }

    public void pauseAnim() {
        if (this.mRenderer != null) {
            this.mRenderer.pauseAnim();
        }
    }

    public void clearAnim() {
        if (this.mRenderer != null) {
            this.mRenderer.clearAnim();
        }
    }

    public WaterEffectRender getRenderer() {
        return this.mRenderer;
    }

    public void setRenderer(GLSurfaceView.Renderer renderer) {
        super.setRenderer(renderer);
        if (renderer instanceof WaterEffectRender) {
            this.mRenderer = (WaterEffectRender) renderer;
            return;
        }
        throw new RuntimeException("You must use WaterEffectRenderer");
    }

    public void requestRender() {
        if (this.mRenderer != null && this.mRenderer.shouldRequest()) {
            super.requestRender();
        }
    }

    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        if (this.mRenderer != null) {
            this.mRenderer.onResume();
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

    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        if (isRenderWhenDirty()) {
            this.mHandler.removeMessages(0);
        }
        if (this.mRenderer != null) {
            this.mRenderer.onPause();
        }
        quitHandlerThread();
    }

    private void quitHandlerThread() {
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quitSafely();
            this.mHandlerThread = null;
        }
        if (this.mHandler != null) {
            this.mHandler.setQuitting(true);
            this.mHandler = null;
        }
    }

    /* access modifiers changed from: private */
    public boolean isRenderWhenDirty() {
        return getRenderMode() == 0;
    }
}
