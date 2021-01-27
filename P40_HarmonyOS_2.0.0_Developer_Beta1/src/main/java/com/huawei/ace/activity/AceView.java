package com.huawei.ace.activity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Insets;
import android.os.Build;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import com.huawei.ace.plugin.clipboard.ClipboardPlugin;
import com.huawei.ace.plugin.editing.TextInputPlugin;
import com.huawei.ace.plugin.texture.AceTexturePlugin;
import com.huawei.ace.plugin.texture.IAceTexture;
import com.huawei.ace.plugin.vibrator.VibratorPlugin;
import com.huawei.ace.runtime.AEventReport;
import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.AceResourcePlugin;
import com.huawei.ace.runtime.AceResourceRegister;
import com.huawei.ace.runtime.IAceView;
import java.io.File;
import java.nio.ByteBuffer;

public class AceView extends SurfaceView implements IAceView, SurfaceHolder.Callback {
    private static final int DEVICE_TYPE_DEFAULT = 0;
    private static final int DEVICE_TYPE_TV = 1;
    private static final String LOG_TAG = "AceView";
    private static final WindowManager.LayoutParams MATCH_PARENT = new WindowManager.LayoutParams(-1, -1);
    public static final boolean USE_VSYNC = true;
    private View animateView;
    private final ClipboardPlugin clipboardPlugin;
    private int mInstanceId = 0;
    private final IAceView.ViewportMetrics mMetrics;
    private long mNativeViewPtr = 0;
    private Surface mSurface;
    private int mSurfaceHeight;
    private volatile int mSurfaceState = 0;
    private int mSurfaceWidth;
    private final AceResourceRegister resRegister;
    private final TextInputPlugin textInputPlugin;
    private final VibratorPlugin vibratorPlugin;

    /* access modifiers changed from: package-private */
    public enum ZeroSides {
        NONE,
        LEFT,
        RIGHT,
        BOTH
    }

    private native long nativeCreateSurfaceHandle(AceView aceView, int i);

    private native void nativeDestroySurfaceHandle(long j);

    private native boolean nativeDispatchKeyEvent(long j, int i, int i2, int i3, long j2, long j3);

    private native boolean nativeDispatchPointerDataPacket(long j, ByteBuffer byteBuffer, int i);

    private native int nativeGetBackgroundColor();

    private native void nativeInitCacheFilePath(String str, String str2);

    private native void nativeInitDeviceType(int i);

    private native long nativeInitResRegister(long j, AceResourceRegister aceResourceRegister);

    private native boolean nativeIsLastPage(int i);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeMarkTextureFrameAvailable(long j, long j2);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeRegisterTexture(long j, long j2, Object obj);

    private native void nativeSetViewportMetrics(long j, float f, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14);

    private native void nativeSurfaceChanged(long j, int i, int i2, int i3);

    private native void nativeSurfaceCreated(long j, Surface surface);

    private native void nativeSurfaceDestroyed(long j);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeUnregisterTexture(long j, long j2);

    @Override // com.huawei.ace.runtime.IAceView
    public void setWindowModal(int i) {
    }

    public AceView(Context context, int i, float f, Boolean bool) {
        super(context);
        this.mInstanceId = i;
        setFocusableInTouchMode(true);
        createNativePtr(i);
        getHolder().addCallback(this);
        Object systemService = context.getSystemService("window");
        if (systemService instanceof WindowManager) {
            AceVsyncWaiter.getInstance((WindowManager) systemService);
        }
        this.mMetrics = new IAceView.ViewportMetrics();
        this.mMetrics.devicePixelRatio = f;
        this.vibratorPlugin = new VibratorPlugin(this);
        initCacheFilePath();
        if (!bool.booleanValue()) {
            this.clipboardPlugin = new ClipboardPlugin(this);
            this.textInputPlugin = new TextInputPlugin(this);
            this.resRegister = new AceResourceRegister();
            initResRegister();
            return;
        }
        this.clipboardPlugin = null;
        this.textInputPlugin = null;
        this.resRegister = null;
    }

    /* access modifiers changed from: protected */
    public void createNativePtr(int i) {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeCreateSurfaceHandle(this, i);
        }
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void releaseNativeView() {
        long j = this.mNativeViewPtr;
        if (j != 0) {
            nativeDestroySurfaceHandle(j);
            this.mNativeViewPtr = 0;
        }
    }

    @Override // com.huawei.ace.runtime.IAceView
    public long getNativePtr() {
        return this.mNativeViewPtr;
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        ALog.d(LOG_TAG, "surfaceCreated");
        setFocusable(true);
        requestFocus();
        this.mSurfaceState = 1;
        this.mSurface = surfaceHolder.getSurface();
        nativeSurfaceCreated(this.mNativeViewPtr, this.mSurface);
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        this.mSurface = surfaceHolder.getSurface();
        this.mSurfaceWidth = i2;
        this.mSurfaceHeight = i3;
        IAceView.ViewportMetrics viewportMetrics = this.mMetrics;
        viewportMetrics.physicalWidth = i2;
        viewportMetrics.physicalHeight = i3;
        updateViewportMetrics();
        Context context = getContext();
        int i4 = (context == null || context.getResources() == null) ? 1 : context.getResources().getConfiguration().orientation;
        ALog.d(LOG_TAG, "surfaceChanged w=" + i2 + ",h=" + i3);
        nativeSurfaceChanged(this.mNativeViewPtr, i2, i3, i4);
    }

    @Override // android.view.SurfaceHolder.Callback
    public synchronized void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        ALog.d(LOG_TAG, "surfaceDestroyed");
        if (this.mSurfaceState != 0) {
            this.mSurfaceState = 0;
            if (this.mNativeViewPtr != 0) {
                nativeSurfaceDestroyed(this.mNativeViewPtr);
            }
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mNativeViewPtr == 0) {
            return super.onTouchEvent(motionEvent);
        }
        ByteBuffer processTouchEvent = AceEventProcessor.processTouchEvent(motionEvent);
        nativeDispatchPointerDataPacket(this.mNativeViewPtr, processTouchEvent, processTouchEvent.position());
        return true;
    }

    /* access modifiers changed from: package-private */
    public ZeroSides calculateShouldZeroSides() {
        if (!(getContext() instanceof Activity)) {
            return ZeroSides.NONE;
        }
        Activity activity = (Activity) getContext();
        int i = activity.getResources().getConfiguration().orientation;
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        if (i != 2) {
            return ZeroSides.NONE;
        }
        if (rotation == 1) {
            return ZeroSides.RIGHT;
        }
        if (rotation == 3) {
            return Build.VERSION.SDK_INT >= 23 ? ZeroSides.LEFT : ZeroSides.RIGHT;
        }
        if (rotation == 0 || rotation == 2) {
            return ZeroSides.BOTH;
        }
        return ZeroSides.NONE;
    }

    /* access modifiers changed from: package-private */
    public int calculateBottomKeyboardInset(WindowInsets windowInsets) {
        if (((double) windowInsets.getSystemWindowInsetBottom()) < ((double) getRootView().getHeight()) * 0.18d) {
            return 0;
        }
        return windowInsets.getSystemWindowInsetBottom();
    }

    @Override // android.view.View
    public final WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        boolean z = true;
        boolean z2 = (getWindowSystemUiVisibility() & 4) != 0;
        if ((getWindowSystemUiVisibility() & 2) == 0) {
            z = false;
        }
        ZeroSides zeroSides = ZeroSides.NONE;
        if (z) {
            zeroSides = calculateShouldZeroSides();
        }
        this.mMetrics.physicalPaddingTop = z2 ? 0 : windowInsets.getSystemWindowInsetTop();
        this.mMetrics.physicalPaddingRight = (zeroSides == ZeroSides.RIGHT || zeroSides == ZeroSides.BOTH) ? 0 : windowInsets.getSystemWindowInsetRight();
        IAceView.ViewportMetrics viewportMetrics = this.mMetrics;
        viewportMetrics.physicalPaddingBottom = 0;
        viewportMetrics.physicalPaddingLeft = (zeroSides == ZeroSides.LEFT || zeroSides == ZeroSides.BOTH) ? 0 : windowInsets.getSystemWindowInsetLeft();
        IAceView.ViewportMetrics viewportMetrics2 = this.mMetrics;
        viewportMetrics2.physicalViewInsetTop = 0;
        viewportMetrics2.physicalViewInsetRight = 0;
        viewportMetrics2.physicalViewInsetBottom = z ? calculateBottomKeyboardInset(windowInsets) : windowInsets.getSystemWindowInsetBottom();
        this.mMetrics.physicalViewInsetLeft = 0;
        if (Build.VERSION.SDK_INT >= 29) {
            Insets systemGestureInsets = windowInsets.getSystemGestureInsets();
            this.mMetrics.systemGestureInsetTop = systemGestureInsets.top;
            this.mMetrics.systemGestureInsetRight = systemGestureInsets.right;
            this.mMetrics.systemGestureInsetBottom = systemGestureInsets.bottom;
            this.mMetrics.systemGestureInsetLeft = systemGestureInsets.left;
        }
        updateViewportMetrics();
        return super.onApplyWindowInsets(windowInsets);
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (this.mNativeViewPtr == 0) {
            return super.onKeyDown(i, keyEvent);
        }
        ALog.d(LOG_TAG, "platform on key down event");
        if (nativeDispatchKeyEvent(this.mNativeViewPtr, keyEvent.getKeyCode(), keyEvent.getAction(), keyEvent.getRepeatCount(), keyEvent.getEventTime(), keyEvent.getDownTime())) {
            return true;
        }
        ALog.d(LOG_TAG, "use platform to handle key down event");
        return super.onKeyDown(i, keyEvent);
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (this.mNativeViewPtr == 0) {
            return super.onKeyUp(i, keyEvent);
        }
        ALog.d(LOG_TAG, "platform on key up event");
        if (nativeDispatchKeyEvent(this.mNativeViewPtr, keyEvent.getKeyCode(), keyEvent.getAction(), keyEvent.getRepeatCount(), keyEvent.getEventTime(), keyEvent.getDownTime())) {
            return true;
        }
        ALog.d(LOG_TAG, "use platform to handle key up event");
        return super.onKeyUp(i, keyEvent);
    }

    public int getActualWidth() {
        return this.mSurfaceWidth;
    }

    public int getActualHeight() {
        return this.mSurfaceHeight;
    }

    public int getSurfaceState() {
        return this.mSurfaceState;
    }

    public void setSurfaceState(int i) {
        this.mSurfaceState = i;
    }

    public void destroy() {
        if (this.mSurfaceState != 0) {
            this.mSurfaceState = 0;
            nativeSurfaceDestroyed(this.mNativeViewPtr);
            this.mNativeViewPtr = 0;
        }
    }

    public void initResRegister() {
        long j = this.mNativeViewPtr;
        if (j != 0) {
            this.resRegister.setRegisterPtr(nativeInitResRegister(j, this.resRegister));
            this.resRegister.registerPlugin(AceTexturePlugin.createRegister(new IAceTexture() {
                /* class com.huawei.ace.activity.AceView.AnonymousClass1 */

                @Override // com.huawei.ace.plugin.texture.IAceTexture
                public void registerTexture(long j, Object obj) {
                    if (AceView.this.mNativeViewPtr != 0) {
                        AceView aceView = AceView.this;
                        aceView.nativeRegisterTexture(aceView.mNativeViewPtr, j, obj);
                    }
                }

                @Override // com.huawei.ace.plugin.texture.IAceTexture
                public void markTextureFrameAvailable(long j) {
                    if (AceView.this.mNativeViewPtr != 0) {
                        AceView aceView = AceView.this;
                        aceView.nativeMarkTextureFrameAvailable(aceView.mNativeViewPtr, j);
                    }
                }

                @Override // com.huawei.ace.plugin.texture.IAceTexture
                public void unregisterTexture(long j) {
                    AceView aceView = AceView.this;
                    aceView.nativeUnregisterTexture(aceView.mNativeViewPtr, j);
                }
            }));
        }
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void addResourcePlugin(AceResourcePlugin aceResourcePlugin) {
        AceResourceRegister aceResourceRegister = this.resRegister;
        if (aceResourceRegister != null) {
            aceResourceRegister.registerPlugin(aceResourcePlugin);
        }
    }

    @Override // android.view.View
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        TextInputPlugin textInputPlugin2 = this.textInputPlugin;
        if (textInputPlugin2 != null) {
            return textInputPlugin2.createInputConnection(this, editorInfo);
        }
        return super.onCreateInputConnection(editorInfo);
    }

    private void updateViewportMetrics() {
        if (this.mNativeViewPtr != 0) {
            ALog.d(LOG_TAG, "updateViewportMetrics");
            nativeSetViewportMetrics(this.mNativeViewPtr, this.mMetrics.devicePixelRatio, this.mMetrics.physicalWidth, this.mMetrics.physicalHeight, this.mMetrics.physicalPaddingTop, this.mMetrics.physicalPaddingRight, this.mMetrics.physicalPaddingBottom, this.mMetrics.physicalPaddingLeft, this.mMetrics.physicalViewInsetTop, this.mMetrics.physicalViewInsetRight, this.mMetrics.physicalViewInsetBottom, this.mMetrics.physicalViewInsetLeft, this.mMetrics.systemGestureInsetTop, this.mMetrics.systemGestureInsetRight, this.mMetrics.systemGestureInsetBottom, this.mMetrics.systemGestureInsetLeft);
        }
    }

    public void initCacheFilePath() {
        if (this.mNativeViewPtr != 0) {
            Context context = getContext();
            if (context == null) {
                AEventReport.sendRenderEvent(0);
                ALog.e(LOG_TAG, "Get context failed!");
                return;
            }
            File filesDir = context.getFilesDir();
            if (filesDir == null) {
                AEventReport.sendRenderEvent(0);
                ALog.e(LOG_TAG, "Get cache path failed!");
                return;
            }
            File file = new File(filesDir, "cache_images");
            if (!file.exists() && !file.mkdirs()) {
                AEventReport.sendRenderEvent(0);
                ALog.e(LOG_TAG, "Create cache path failed!");
            }
            File file2 = new File(filesDir, "cache_files");
            if (!file2.exists() && !file2.mkdirs()) {
                AEventReport.sendRenderEvent(0);
                ALog.e(LOG_TAG, "Create cache path failed!");
            }
            nativeInitCacheFilePath(file.getPath(), file2.getPath());
        }
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void onPause() {
        AceResourceRegister aceResourceRegister = this.resRegister;
        if (aceResourceRegister != null) {
            aceResourceRegister.onActivityPause();
        }
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void onResume() {
        AceResourceRegister aceResourceRegister = this.resRegister;
        if (aceResourceRegister != null) {
            aceResourceRegister.onActivityResume();
        }
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void initDeviceType() {
        Context context = getContext();
        if (context != null) {
            Resources resources = context.getResources();
            if (resources == null) {
                ALog.e(LOG_TAG, "initDeviceInfo resources get failed!");
                return;
            }
            Configuration configuration = resources.getConfiguration();
            if (configuration == null) {
                ALog.e(LOG_TAG, "initDeviceInfo configuration get failed!");
                return;
            }
            int i = 0;
            if ((configuration.uiMode & 15) == 4) {
                i = 1;
            }
            nativeInitDeviceType(i);
        }
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void viewCreated() {
        this.animateView = createAnimateView();
        if (this.animateView != null && (getContext() instanceof Activity)) {
            ((Activity) getContext()).addContentView(this.animateView, MATCH_PARENT);
        }
    }

    public void onFirstFrame() {
        View view = this.animateView;
        if (view != null) {
            ViewParent parent = view.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(this.animateView);
            }
            this.animateView = null;
        }
    }

    private View createAnimateView() {
        if (!(getContext() instanceof Activity)) {
            return null;
        }
        View view = new View((Activity) getContext());
        view.setLayoutParams(MATCH_PARENT);
        view.setBackgroundColor(nativeGetBackgroundColor());
        return view;
    }
}
