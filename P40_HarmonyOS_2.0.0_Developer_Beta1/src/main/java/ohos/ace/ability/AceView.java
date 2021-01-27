package ohos.ace.ability;

import com.huawei.ace.adapter.AceAGPEngineAdapter;
import com.huawei.ace.adapter.AceContextAdapter;
import com.huawei.ace.adapter.ViewportMetricsAdapter;
import com.huawei.ace.plugin.texture.AceTexturePlugin;
import com.huawei.ace.plugin.texture.IAceTexture;
import com.huawei.ace.runtime.AEventReport;
import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.AceResourcePlugin;
import com.huawei.ace.runtime.AceResourceRegister;
import com.huawei.ace.runtime.IAceView;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Consumer;
import ohos.agp.graphics.Surface;
import ohos.agp.window.service.Display;
import ohos.app.Context;
import ohos.global.resource.ResourceManager;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.MouseEvent;
import ohos.multimodalinput.event.RotationEvent;
import ohos.multimodalinput.event.TouchEvent;

public class AceView extends AceAGPEngineAdapter implements IAceView {
    private static final int DEVICE_TYPE_DEFAULT = 0;
    private static final int DEVICE_TYPE_TV = 1;
    private static final long KEY_LONG_PRESS_TIME = 300;
    private static final String LOG_TAG = "AceView";
    private static final int ORIENTATION_DEFAULT = 0;
    private static final int UI_MODE_TYPE_MASK = 15;
    private static final int UI_MODE_TYPE_TELEVISION = 4;
    private static final int WINDOW_MODAL_DIALOG = 3;
    private static final int WINDOW_MODAL_NORMAL = 0;
    private static final int WINDOW_MODAL_SEMI = 1;
    private AceContextAdapter contextAdapter;
    private Optional<Display> display;
    private Context mContext;
    private int mInstanceId = 0;
    private final IAceView.ViewportMetrics mMetrics;
    private long mNativeViewPtr = 0;
    private volatile int mSurfaceState = 0;
    private final AceResourceRegister resRegister;
    private int windowModal = 0;

    private native long nativeCreateSurfaceHandle(AceView aceView, int i);

    private native void nativeDestroySurfaceHandle(long j);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native boolean nativeDispatchKeyEvent(long j, int i, int i2, int i3, long j2, long j3);

    private native void nativeDispatchMouseEvent(long j, ByteBuffer byteBuffer, int i);

    private native boolean nativeDispatchPointerDataPacket(long j, ByteBuffer byteBuffer, int i);

    private native boolean nativeDispatchRotationEvent(long j, float f);

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

    private native void nativeSurfaceCreated(long j, Object obj);

    private native void nativeSurfaceDestroyed(long j);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeUnregisterTexture(long j, long j2);

    @Override // com.huawei.ace.runtime.IAceView
    public void initDeviceType() {
    }

    public AceView(Context context, int i, float f) {
        this.mContext = context;
        this.mInstanceId = i;
        createNativePtr(i);
        this.mMetrics = new IAceView.ViewportMetrics();
        this.mMetrics.devicePixelRatio = f;
        AceDisplayManager.initRefreshRate(context);
        if (context instanceof AceAbility) {
            this.contextAdapter = new AceContextAdapter(((AceAbility) context).getHostContext());
            if (!this.contextAdapter.invalid()) {
                initCacheFilePath(this.contextAdapter);
            }
        }
        this.resRegister = new AceResourceRegister();
        initResRegister();
    }

    /* access modifiers changed from: protected */
    public void createNativePtr(int i) {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeCreateSurfaceHandle(this, i);
        }
    }

    @Override // com.huawei.ace.runtime.IAceView
    public long getNativePtr() {
        return this.mNativeViewPtr;
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void releaseNativeView() {
        AceContextAdapter aceContextAdapter;
        long j = this.mNativeViewPtr;
        if (j != 0) {
            nativeDestroySurfaceHandle(j);
            this.mNativeViewPtr = 0;
        }
        int i = this.windowModal;
        if ((i == 1 || i == 3) && (aceContextAdapter = this.contextAdapter) != null) {
            aceContextAdapter.clearHomeKeyPressedListener();
        }
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void addResourcePlugin(AceResourcePlugin aceResourcePlugin) {
        this.resRegister.registerPlugin(aceResourcePlugin);
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void onPause() {
        this.resRegister.onActivityPause();
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void onResume() {
        this.resRegister.onActivityResume();
    }

    public void initCacheFilePath(AceContextAdapter aceContextAdapter) {
        if (this.mNativeViewPtr != 0) {
            if (aceContextAdapter.invalid()) {
                AEventReport.sendRenderEvent(0);
                ALog.e(LOG_TAG, "Get context failed!");
                return;
            }
            File filesDir = aceContextAdapter.getContext().getFilesDir();
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

    public void initResRegister() {
        long j = this.mNativeViewPtr;
        if (j != 0) {
            this.resRegister.setRegisterPtr(nativeInitResRegister(j, this.resRegister));
            this.resRegister.registerPlugin(AceTexturePlugin.createRegister(new IAceTexture() {
                /* class ohos.ace.ability.AceView.AnonymousClass1 */

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
                    if (AceView.this.mNativeViewPtr != 0) {
                        AceView aceView = AceView.this;
                        aceView.nativeUnregisterTexture(aceView.mNativeViewPtr, j);
                    }
                }
            }));
        }
    }

    @Override // com.huawei.ace.adapter.AceAGPEngineAdapter
    public void processSurfaceCreatedInner(Surface surface) {
        ALog.d(LOG_TAG, "processSurfaceCreated");
        this.mSurfaceState = 1;
        nativeSurfaceCreated(this.mNativeViewPtr, getASurface(surface));
    }

    @Override // com.huawei.ace.adapter.AceAGPEngineAdapter
    public void processSurfaceChangedInner(Surface surface, int i, int i2, int i3) {
        ResourceManager resourceManager;
        ALog.d(LOG_TAG, "processSurfaceChanged");
        IAceView.ViewportMetrics viewportMetrics = this.mMetrics;
        viewportMetrics.physicalWidth = i2;
        viewportMetrics.physicalHeight = i3;
        updateViewportMetrics();
        ALog.d(LOG_TAG, "surfaceChanged w=" + i2 + ", h=" + i3);
        Context context = this.mContext;
        int i4 = (context == null || (resourceManager = context.getResourceManager()) == null) ? 0 : resourceManager.getConfiguration().direction;
        long j = this.mNativeViewPtr;
        if (j != 0) {
            nativeSurfaceChanged(j, i2, i3, i4);
        }
    }

    @Override // com.huawei.ace.adapter.AceAGPEngineAdapter
    public void processSurfaceDestroyInner(Surface surface) {
        ALog.d(LOG_TAG, "processSurfaceDestroy");
        if (this.mSurfaceState != 0) {
            this.mSurfaceState = 0;
            long j = this.mNativeViewPtr;
            if (j != 0) {
                nativeSurfaceDestroyed(j);
            }
        }
    }

    @Override // com.huawei.ace.adapter.AceAGPEngineAdapter
    public boolean processTouchEventInner(TouchEvent touchEvent) {
        if (this.mNativeViewPtr == 0 || touchEvent.getAction() == 0) {
            return false;
        }
        if (ALog.isDebuggable()) {
            ALog.d(LOG_TAG, "processTouchEventInner type=" + touchEvent.getAction() + ", screen.x=" + touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getX() + ", screen.y=" + touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getY() + ", window.x=" + touchEvent.getPointerPosition(touchEvent.getIndex()).getX() + ", window.y=" + touchEvent.getPointerPosition(touchEvent.getIndex()).getY() + ", actionId=" + touchEvent.getIndex() + ", pointerId=" + touchEvent.getPointerId(touchEvent.getIndex()) + ", force=" + touchEvent.getForcePrecision() + ", maxforce=" + touchEvent.getMaxForce() + ", radius=" + touchEvent.getRadius(touchEvent.getIndex()) + ",source=" + touchEvent.getSourceDevice() + ", deviceid=" + touchEvent.getInputDeviceId());
        }
        ByteBuffer processTouchEvent = AceEventProcessor.processTouchEvent(touchEvent, 0.0f, 0.0f);
        return nativeDispatchPointerDataPacket(this.mNativeViewPtr, processTouchEvent, processTouchEvent.position());
    }

    @Override // com.huawei.ace.adapter.AceAGPEngineAdapter
    public boolean processKeyEventInner(KeyEvent keyEvent) {
        if (this.mNativeViewPtr == 0) {
            return false;
        }
        int keyCode = keyEvent.getKeyCode();
        if (keyCode <= -1 || keyCode > KeyEvent.getMaxKeyCode()) {
            ALog.d(LOG_TAG, "platform on key event, unkonw keyCode=" + keyCode);
            return false;
        }
        int keyActionToActionType = AceEventProcessor.keyActionToActionType(keyEvent.isKeyDown());
        int keyDownDuration = (int) (keyEvent.getKeyDownDuration() / KEY_LONG_PRESS_TIME);
        int keyToKeyCode = AceEventProcessor.keyToKeyCode(keyCode);
        long occurredTime = keyEvent.getOccurredTime();
        long keyDownDuration2 = occurredTime - keyEvent.getKeyDownDuration();
        ALog.d(LOG_TAG, "platform on key event, AgpkeyCode=" + keyCode + ", AceKeyCode=" + keyToKeyCode + ", keyAction=" + keyActionToActionType + ", keyDownDuration=" + keyEvent.getKeyDownDuration() + ", OccurredTime=" + occurredTime + ", StartTime=" + keyDownDuration2 + ", Source=" + keyEvent.getSourceDevice() + ", DeviceId" + keyEvent.getInputDeviceId());
        if (nativeDispatchKeyEvent(this.mNativeViewPtr, keyToKeyCode, keyActionToActionType, keyDownDuration, occurredTime, keyDownDuration2)) {
            return true;
        }
        ALog.d(LOG_TAG, "use platform to handle key event");
        return false;
    }

    @Override // com.huawei.ace.adapter.AceAGPEngineAdapter
    public boolean processMouseEventInner(MouseEvent mouseEvent) {
        if (this.mNativeViewPtr == 0) {
            return false;
        }
        ByteBuffer processMouseEvent = AceEventProcessor.processMouseEvent(mouseEvent);
        nativeDispatchMouseEvent(this.mNativeViewPtr, processMouseEvent, processMouseEvent.position());
        return false;
    }

    @Override // com.huawei.ace.adapter.AceAGPEngineAdapter
    public boolean processRotationEventInner(RotationEvent rotationEvent) {
        ALog.d(LOG_TAG, "processRotationEventInner value=" + rotationEvent.getRotationValue());
        long j = this.mNativeViewPtr;
        if (j == 0) {
            return false;
        }
        if (nativeDispatchRotationEvent(j, rotationEvent.getRotationValue())) {
            ALog.d(LOG_TAG, "processRotationEventInner accept");
            return true;
        }
        ALog.d(LOG_TAG, "use platform to handle rotation event");
        return false;
    }

    @Override // com.huawei.ace.adapter.AceAGPEngineAdapter
    public void processDestroyInner() {
        if (this.mSurfaceState != 0) {
            this.mSurfaceState = 0;
            long j = this.mNativeViewPtr;
            if (j != 0) {
                nativeSurfaceDestroyed(j);
            }
            this.mNativeViewPtr = 0;
        }
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void viewCreated() {
        AceContextAdapter aceContextAdapter = this.contextAdapter;
        if (aceContextAdapter != null && !aceContextAdapter.invalid()) {
            if (this.windowModal == 0) {
                this.contextAdapter.addAnimateView(nativeGetBackgroundColor());
            }
            this.contextAdapter.setOnApplyWindowInsetsListener(new ViewportMetricsAdapter(), new Consumer() {
                /* class ohos.ace.ability.$$Lambda$AceView$LR9R81p9DPRaLTBTF8y458sMOdQ */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AceView.this.lambda$viewCreated$0$AceView((ViewportMetricsAdapter) obj);
                }
            });
            int i = this.windowModal;
            if (i == 1 || i == 3) {
                this.contextAdapter.setOnHomeKeyPressedListener(new AceContextAdapter.HomeKeyPressedCallback() {
                    /* class ohos.ace.ability.AceView.AnonymousClass2 */

                    @Override // com.huawei.ace.adapter.AceContextAdapter.HomeKeyPressedCallback
                    public void onHomeKeyPressed() {
                        long currentTimeMillis = System.currentTimeMillis();
                        int keyActionToActionType = AceEventProcessor.keyActionToActionType(false);
                        int keyToKeyCode = AceEventProcessor.keyToKeyCode(1);
                        AceView aceView = AceView.this;
                        aceView.nativeDispatchKeyEvent(aceView.mNativeViewPtr, keyToKeyCode, keyActionToActionType, 0, currentTimeMillis, currentTimeMillis);
                    }
                });
            }
        }
    }

    public /* synthetic */ void lambda$viewCreated$0$AceView(ViewportMetricsAdapter viewportMetricsAdapter) {
        this.mMetrics.physicalPaddingTop = viewportMetricsAdapter.physicalPaddingTop;
        this.mMetrics.physicalViewInsetBottom = viewportMetricsAdapter.physicalViewInsetBottom;
        updateViewportMetrics();
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void setWindowModal(int i) {
        this.windowModal = i;
    }

    public void onFirstFrame() {
        AceContextAdapter aceContextAdapter = this.contextAdapter;
        if (aceContextAdapter != null && !aceContextAdapter.invalid()) {
            this.contextAdapter.blurAnimateView();
        }
    }

    private void updateViewportMetrics() {
        if (this.mNativeViewPtr != 0) {
            ALog.d(LOG_TAG, "updateViewportMetrics");
            nativeSetViewportMetrics(this.mNativeViewPtr, this.mMetrics.devicePixelRatio, this.mMetrics.physicalWidth, this.mMetrics.physicalHeight, this.mMetrics.physicalPaddingTop, this.mMetrics.physicalPaddingRight, this.mMetrics.physicalPaddingBottom, this.mMetrics.physicalPaddingLeft, this.mMetrics.physicalViewInsetTop, this.mMetrics.physicalViewInsetRight, this.mMetrics.physicalViewInsetBottom, this.mMetrics.physicalViewInsetLeft, this.mMetrics.systemGestureInsetTop, this.mMetrics.systemGestureInsetRight, this.mMetrics.systemGestureInsetBottom, this.mMetrics.systemGestureInsetLeft);
        }
    }
}
