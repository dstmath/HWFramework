package ohos.ace.ability;

import com.huawei.ace.adapter.AceContextAdapter;
import com.huawei.ace.adapter.AceTextInputAdapter;
import com.huawei.ace.adapter.AceViewAdapter;
import com.huawei.ace.plugin.clipboard.ClipboardPlugin;
import com.huawei.ace.plugin.editing.TextInputPlugin;
import com.huawei.ace.plugin.texture.AceLayerTexturePlugin;
import com.huawei.ace.plugin.texture.IAceTexture;
import com.huawei.ace.runtime.AEventReport;
import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.AceResourcePlugin;
import com.huawei.ace.runtime.AceResourceRegister;
import com.huawei.ace.runtime.IAceView;
import java.io.File;
import java.nio.ByteBuffer;
import ohos.app.Context;
import ohos.global.resource.ResourceManager;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.MouseEvent;
import ohos.multimodalinput.event.RotationEvent;
import ohos.multimodalinput.event.TouchEvent;

public final class AceNativeView extends AceViewAdapter implements IAceView {
    private static final int DEVICE_TYPE_DEFAULT = 0;
    private static final int DEVICE_TYPE_TV = 1;
    private static final long KEY_LONG_PRESS_TIME = 300;
    private static final int ORIENTATION_DEFAULT = 0;
    private static final String TAG = "AceNativeView";
    private ClipboardPlugin clipboardPlugin;
    private final Context context;
    private AceContextAdapter contextAdapter;
    private long nativeViewPtr = 0;
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
    public void setWindowModal(int i) {
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void viewCreated() {
    }

    public AceNativeView(Context context2, int i) {
        super(context2.getHostContext());
        this.viewId = i;
        this.context = context2;
        this.nativeViewPtr = nativeCreateViewHandle(this.viewId);
        nativeSetCallback(this.nativeViewPtr, this);
        this.resRegister = new AceResourceRegister();
        initResRegister();
        if (context2 instanceof AceAbility) {
            this.contextAdapter = new AceContextAdapter(((AceAbility) context2).getHostContext());
            if (!this.contextAdapter.invalid()) {
                initCacheFilePath(this.contextAdapter);
            }
        }
    }

    public void initResRegister() {
        long j = this.nativeViewPtr;
        if (j != 0) {
            this.resRegister.setRegisterPtr(nativeInitResRegister(j, this.resRegister));
            this.resRegister.registerPlugin(AceLayerTexturePlugin.createRegister(new IAceTexture() {
                /* class ohos.ace.ability.AceNativeView.AnonymousClass1 */

                @Override // com.huawei.ace.plugin.texture.IAceTexture
                public void markTextureFrameAvailable(long j) {
                }

                @Override // com.huawei.ace.plugin.texture.IAceTexture
                public void registerTexture(long j, Object obj) {
                    if (AceNativeView.this.nativeViewPtr != 0) {
                        AceNativeView aceNativeView = AceNativeView.this;
                        aceNativeView.nativeRegisterTexture(aceNativeView.nativeViewPtr, j, AceNativeView.this.getTextureLayerHandle(obj));
                    }
                }

                @Override // com.huawei.ace.plugin.texture.IAceTexture
                public void unregisterTexture(long j) {
                    if (AceNativeView.this.nativeViewPtr != 0) {
                        AceNativeView aceNativeView = AceNativeView.this;
                        aceNativeView.nativeUnregisterTexture(aceNativeView.nativeViewPtr, j);
                    }
                }
            }, createIAceTextureLayer()));
        }
    }

    public void initTextInputPlugins() {
        this.clipboardPlugin = new ClipboardPlugin(this);
        this.textInputPlugin = new TextInputPlugin(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void initCacheFilePath(AceContextAdapter aceContextAdapter) {
        if (this.nativeViewPtr != 0) {
            if (aceContextAdapter.invalid()) {
                AEventReport.sendRenderEvent(0);
                ALog.e(TAG, "Get context failed!");
                return;
            }
            File filesDir = aceContextAdapter.getContext().getFilesDir();
            if (filesDir == null) {
                AEventReport.sendRenderEvent(0);
                ALog.e(TAG, "Get cache path failed!");
                return;
            }
            File file = new File(filesDir, "cache_images");
            if (!file.exists() && !file.mkdirs()) {
                AEventReport.sendRenderEvent(0);
                ALog.e(TAG, "Create cache path failed!");
            }
            File file2 = new File(filesDir, "cache_files");
            if (!file2.exists() && !file2.mkdirs()) {
                AEventReport.sendRenderEvent(0);
                ALog.e(TAG, "Create cache path failed!");
            }
            nativeInitCacheFilePath(file.getPath(), file2.getPath());
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.ace.adapter.AceViewAdapter
    public void processDraw(long j) {
        nativeDrawFrame(this.nativeViewPtr, j);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.ace.adapter.AceViewAdapter, android.view.View
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        ResourceManager resourceManager;
        super.onSizeChanged(i, i2, i3, i4);
        Context context2 = this.context;
        nativeViewChanged(this.nativeViewPtr, i, i2, (context2 == null || (resourceManager = context2.getResourceManager()) == null) ? 0 : resourceManager.getConfiguration().direction);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.ace.adapter.AceViewAdapter, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.ace.adapter.AceViewAdapter, android.view.View
    public void onDetachedFromWindow() {
        nativeViewDestroyed(this.nativeViewPtr);
        super.onDetachedFromWindow();
    }

    @Override // com.huawei.ace.adapter.AceViewAdapter
    public boolean processTouchEvent(TouchEvent touchEvent) {
        if (this.nativeViewPtr == 0 || touchEvent.getAction() == 0) {
            return false;
        }
        if (ALog.isDebuggable()) {
            ALog.d(TAG, "processTouchEventInner type = " + touchEvent.getAction() + ", screen.x = " + touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getX() + ", screen.y = " + touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getY() + ", window.x = " + touchEvent.getPointerPosition(touchEvent.getIndex()).getX() + ", window.y = " + touchEvent.getPointerPosition(touchEvent.getIndex()).getY() + ", actionId = " + touchEvent.getIndex() + ", pointerId = " + touchEvent.getPointerId(touchEvent.getIndex()) + ", force = " + touchEvent.getForcePrecision() + ", maxforce = " + touchEvent.getMaxForce() + ", radius = " + touchEvent.getRadius(touchEvent.getIndex()) + ", source = " + touchEvent.getSourceDevice() + ", deviceid = " + touchEvent.getInputDeviceId());
        }
        ByteBuffer processTouchEvent = AceEventProcessor.processTouchEvent(touchEvent, 0.0f, 0.0f);
        return nativeDispatchPointerDataPacket(this.nativeViewPtr, processTouchEvent, processTouchEvent.position());
    }

    @Override // com.huawei.ace.adapter.AceViewAdapter
    public boolean processKeyEvent(KeyEvent keyEvent) {
        int keyCode;
        if (this.nativeViewPtr != 0 && (keyCode = keyEvent.getKeyCode()) > -1 && keyCode <= KeyEvent.getMaxKeyCode()) {
            int keyToKeyCode = AceEventProcessor.keyToKeyCode(keyCode);
            long occurredTime = keyEvent.getOccurredTime();
            if (nativeDispatchKeyEvent(this.nativeViewPtr, keyToKeyCode, AceEventProcessor.keyActionToActionType(keyEvent.isKeyDown()), (int) (keyEvent.getKeyDownDuration() / KEY_LONG_PRESS_TIME), occurredTime, occurredTime - keyEvent.getKeyDownDuration())) {
                return true;
            }
        }
        return false;
    }

    public boolean processMouseEventInner(MouseEvent mouseEvent) {
        if (this.nativeViewPtr == 0) {
            return false;
        }
        ByteBuffer processMouseEvent = AceEventProcessor.processMouseEvent(mouseEvent);
        nativeDispatchMouseEvent(this.nativeViewPtr, processMouseEvent, processMouseEvent.position());
        return false;
    }

    @Override // com.huawei.ace.adapter.AceViewAdapter
    public void createInputConnection(AceTextInputAdapter aceTextInputAdapter) {
        TextInputPlugin textInputPlugin2 = this.textInputPlugin;
        if (textInputPlugin2 != null) {
            aceTextInputAdapter.setInputConnection(textInputPlugin2.createInputConnection(this, aceTextInputAdapter.getEditorInfo()));
        }
    }

    public boolean processRotationEventInner(RotationEvent rotationEvent) {
        long j = this.nativeViewPtr;
        if (j != 0 && nativeDispatchRotationEvent(j, rotationEvent.getRotationValue())) {
            return true;
        }
        return false;
    }

    @Override // com.huawei.ace.runtime.IAceView
    public long getNativePtr() {
        return this.nativeViewPtr;
    }

    @Override // com.huawei.ace.runtime.IAceView
    public void releaseNativeView() {
        long j = this.nativeViewPtr;
        if (j != 0) {
            nativeDestroyViewHandle(j);
            this.nativeViewPtr = 0;
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
}
