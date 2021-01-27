package com.huawei.ace.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.TextureLayer;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import com.huawei.ace.adapter.IAceTextureLayer;
import com.huawei.ace.plugin.clipboard.ClipboardPlugin;
import com.huawei.ace.plugin.editing.TextInputPlugin;
import com.huawei.ace.plugin.texture.AceLayerTexturePlugin;
import com.huawei.ace.plugin.texture.IAceTexture;
import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.AceResourcePlugin;
import com.huawei.ace.runtime.AceResourceRegister;
import com.huawei.ace.runtime.IAceView;
import java.nio.ByteBuffer;

public final class AceNativeView extends View implements IAceView {
    private static final long INVALID_TEXTURELAYER_HANDLE = 0;
    private static final String TAG = "AceNativeView";
    private ClipboardPlugin clipboardPlugin;
    private final Context context;
    private long nativeViewPtr = INVALID_TEXTURELAYER_HANDLE;
    private final AceResourceRegister resRegister;
    private TextInputPlugin textInputPlugin;
    private final int viewId;

    private native long nativeCreateViewHandle(int i);

    private native void nativeDestroyViewHandle(long j);

    private native boolean nativeDispatchKeyEvent(long j, int i, int i2, int i3, long j2, long j3);

    private native void nativeDispatchMouseEvent(long j, ByteBuffer byteBuffer, int i);

    private native boolean nativeDispatchPointerDataPacket(long j, ByteBuffer byteBuffer, int i);

    private native boolean nativeDispatchRotationEvent(long j, float f);

    private native void nativeDrawFrame(long j, long j2);

    private native void nativeInitCacheFilePath(String str, String str2);

    private native long nativeInitResRegister(long j, AceResourceRegister aceResourceRegister);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeRegisterTexture(long j, long j2, long j3);

    private native void nativeSetCallback(long j, Object obj);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeUnregisterTexture(long j, long j2);

    private native void nativeViewChanged(long j, int i, int i2, int i3);

    private native void nativeViewDestroyed(long j);

    @Override // com.huawei.ace.runtime.IAceView
    public void initDeviceType() {
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void onPause() {
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void onResume() {
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void setWindowModal(int i) {
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void viewCreated() {
    }

    public AceNativeView(Context context2, int i) {
        super(context2);
        this.viewId = i;
        this.context = context2;
        Object systemService = this.context.getSystemService("window");
        if (systemService instanceof WindowManager) {
            AceVsyncWaiter.getInstance((WindowManager) systemService);
        }
        this.nativeViewPtr = nativeCreateViewHandle(this.viewId);
        nativeSetCallback(this.nativeViewPtr, this);
        this.resRegister = new AceResourceRegister();
        initResRegister();
    }

    public void initResRegister() {
        long j = this.nativeViewPtr;
        if (j != INVALID_TEXTURELAYER_HANDLE) {
            this.resRegister.setRegisterPtr(nativeInitResRegister(j, this.resRegister));
            AnonymousClass1 r0 = new IAceTextureLayer() {
                /* class com.huawei.ace.activity.AceNativeView.AnonymousClass1 */

                @Override // com.huawei.ace.adapter.IAceTextureLayer
                public TextureLayer createTextureLayer() {
                    return AceNativeView.this.getThreadedRenderer().createTextureLayer();
                }
            };
            this.resRegister.registerPlugin(AceLayerTexturePlugin.createRegister(new IAceTexture() {
                /* class com.huawei.ace.activity.AceNativeView.AnonymousClass2 */

                @Override // com.huawei.ace.plugin.texture.IAceTexture
                public void markTextureFrameAvailable(long j) {
                }

                @Override // com.huawei.ace.plugin.texture.IAceTexture
                public void registerTexture(long j, Object obj) {
                    long j2 = AceNativeView.this.nativeViewPtr;
                    long j3 = AceNativeView.INVALID_TEXTURELAYER_HANDLE;
                    if (j2 != AceNativeView.INVALID_TEXTURELAYER_HANDLE) {
                        if (obj instanceof TextureLayer) {
                            j3 = ((TextureLayer) obj).getLayerHandle();
                        }
                        AceNativeView aceNativeView = AceNativeView.this;
                        aceNativeView.nativeRegisterTexture(aceNativeView.nativeViewPtr, j, j3);
                    }
                }

                @Override // com.huawei.ace.plugin.texture.IAceTexture
                public void unregisterTexture(long j) {
                    if (AceNativeView.this.nativeViewPtr != AceNativeView.INVALID_TEXTURELAYER_HANDLE) {
                        AceNativeView aceNativeView = AceNativeView.this;
                        aceNativeView.nativeUnregisterTexture(aceNativeView.nativeViewPtr, j);
                    }
                }
            }, r0));
        }
    }

    public void initTextInputPlugins() {
        this.clipboardPlugin = new ClipboardPlugin(this);
        this.textInputPlugin = new TextInputPlugin(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override // android.view.View
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        TextInputPlugin textInputPlugin2 = this.textInputPlugin;
        if (textInputPlugin2 != null) {
            return textInputPlugin2.createInputConnection(this, editorInfo);
        }
        return super.onCreateInputConnection(editorInfo);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        nativeDrawFrame(this.nativeViewPtr, canvas.getNativeCanvasWrapper());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        nativeViewChanged(this.nativeViewPtr, i, i2, getOrientation());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        int width = getWidth();
        int height = getHeight();
        if (width != 0 && height != 0) {
            nativeViewChanged(this.nativeViewPtr, width, height, getOrientation());
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        nativeViewDestroyed(this.nativeViewPtr);
        super.onDetachedFromWindow();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.nativeViewPtr == INVALID_TEXTURELAYER_HANDLE) {
            return super.onTouchEvent(motionEvent);
        }
        if (ALog.isDebuggable()) {
            ALog.d(TAG, "processTouchEventInner type = " + motionEvent.getActionMasked() + ", window.x = " + motionEvent.getX(motionEvent.getActionIndex()) + ", window.y = " + motionEvent.getY(motionEvent.getActionIndex()) + ", actionId = " + motionEvent.getActionIndex() + ", pointerId = " + motionEvent.getPointerId(motionEvent.getActionIndex()) + ", force = " + motionEvent.getPressure(motionEvent.getActionIndex()));
        }
        ByteBuffer processTouchEvent = AceEventProcessor.processTouchEvent(motionEvent);
        nativeDispatchPointerDataPacket(this.nativeViewPtr, processTouchEvent, processTouchEvent.position());
        return true;
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        long j = this.nativeViewPtr;
        if (j == INVALID_TEXTURELAYER_HANDLE) {
            return super.onKeyDown(i, keyEvent);
        }
        if (nativeDispatchKeyEvent(j, keyEvent.getKeyCode(), keyEvent.getAction(), keyEvent.getRepeatCount(), keyEvent.getEventTime(), keyEvent.getDownTime())) {
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        long j = this.nativeViewPtr;
        if (j == INVALID_TEXTURELAYER_HANDLE) {
            return super.onKeyUp(i, keyEvent);
        }
        if (nativeDispatchKeyEvent(j, keyEvent.getKeyCode(), keyEvent.getAction(), keyEvent.getRepeatCount(), keyEvent.getEventTime(), keyEvent.getDownTime())) {
            return true;
        }
        return super.onKeyUp(i, keyEvent);
    }

    @Override // com.huawei.ace.runtime.IAceView
    public long getNativePtr() {
        return this.nativeViewPtr;
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void releaseNativeView() {
        long j = this.nativeViewPtr;
        if (j != INVALID_TEXTURELAYER_HANDLE) {
            nativeDestroyViewHandle(j);
            this.nativeViewPtr = INVALID_TEXTURELAYER_HANDLE;
        }
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void addResourcePlugin(AceResourcePlugin aceResourcePlugin) {
        this.resRegister.registerPlugin(aceResourcePlugin);
    }

    private int getOrientation() {
        if (getResources() == null || getResources().getConfiguration() == null) {
            return 1;
        }
        return getResources().getConfiguration().orientation;
    }
}
