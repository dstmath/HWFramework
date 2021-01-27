package ohos.ace.ability;

import com.huawei.ace.adapter.AceContextAdapter;
import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.AceResourcePlugin;
import com.huawei.ace.runtime.AceResourceRegister;
import com.huawei.ace.runtime.IAceView;
import java.nio.ByteBuffer;
import ohos.agp.components.Component;
import ohos.agp.render.Canvas;
import ohos.agp.render.Picture;
import ohos.app.Context;
import ohos.global.configuration.Configuration;
import ohos.global.resource.ResourceManager;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.TouchEvent;

public class AceNativeComponent extends Component implements IAceView {
    private static final String LOG_TAG = "AceNativeComponent";
    private int height = 0;
    private long nativeViewPtr = 0;
    private int orientation = 0;
    private final Picture picture = new Picture();
    private int width = 0;

    private native long nativeCreateViewHandle(int i);

    private native void nativeDestroyViewHandle(long j);

    private native boolean nativeDispatchKeyEvent(long j, int i, int i2, int i3, long j2, long j3);

    private native void nativeDispatchMouseEvent(long j, ByteBuffer byteBuffer, int i);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native boolean nativeDispatchPointerDataPacket(long j, ByteBuffer byteBuffer, int i);

    private native boolean nativeDispatchRotationEvent(long j, float f);

    private native void nativeDrawFrame(long j, long j2);

    private native void nativeInitCacheFilePath(String str, String str2);

    private native long nativeInitResRegister(long j, AceResourceRegister aceResourceRegister);

    private native void nativeRegisterTexture(long j, long j2, long j3);

    private native void nativeSetCallback(long j, Object obj);

    private native void nativeUnregisterTexture(long j, long j2);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeViewChanged(long j, int i, int i2, int i3);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeViewDestroyed(long j);

    @Override // com.huawei.ace.runtime.IAceView
    public void addResourcePlugin(AceResourcePlugin aceResourcePlugin) {
    }

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

    public AceNativeComponent(Context context, int i) {
        super(context);
        AceContextAdapter.initVsync(context.getHostContext());
        this.nativeViewPtr = nativeCreateViewHandle(i);
        nativeSetCallback(this.nativeViewPtr, this);
        addDrawTask(new Component.DrawTask() {
            /* class ohos.ace.ability.$$Lambda$AceNativeComponent$YGUQRkD_dNQDtIkTIeWWZZEhw */

            @Override // ohos.agp.components.Component.DrawTask
            public final void onDraw(Component component, Canvas canvas) {
                AceNativeComponent.this.lambda$new$0$AceNativeComponent(component, canvas);
            }
        });
        setLayoutRefreshedListener(new Component.LayoutRefreshedListener() {
            /* class ohos.ace.ability.$$Lambda$AceNativeComponent$HmwLZ_YOSyNwF338C1lc6E9Ccbk */

            @Override // ohos.agp.components.Component.LayoutRefreshedListener
            public final void onRefreshed(Component component) {
                AceNativeComponent.this.lambda$new$1$AceNativeComponent(component);
            }
        });
        setBindStateChangedListener(new Component.BindStateChangedListener() {
            /* class ohos.ace.ability.AceNativeComponent.AnonymousClass1 */

            @Override // ohos.agp.components.Component.BindStateChangedListener
            public void onComponentBoundToWindow(Component component) {
                AceNativeComponent aceNativeComponent = AceNativeComponent.this;
                aceNativeComponent.width = aceNativeComponent.getWidth();
                AceNativeComponent aceNativeComponent2 = AceNativeComponent.this;
                aceNativeComponent2.height = aceNativeComponent2.getHeight();
                AceNativeComponent aceNativeComponent3 = AceNativeComponent.this;
                aceNativeComponent3.orientation = aceNativeComponent3.getOrientation();
                if (AceNativeComponent.this.width != 0 && AceNativeComponent.this.height != 0) {
                    AceNativeComponent aceNativeComponent4 = AceNativeComponent.this;
                    aceNativeComponent4.nativeViewChanged(aceNativeComponent4.nativeViewPtr, AceNativeComponent.this.width, AceNativeComponent.this.height, AceNativeComponent.this.orientation);
                }
            }

            @Override // ohos.agp.components.Component.BindStateChangedListener
            public void onComponentUnboundFromWindow(Component component) {
                AceNativeComponent aceNativeComponent = AceNativeComponent.this;
                aceNativeComponent.nativeViewDestroyed(aceNativeComponent.nativeViewPtr);
            }
        });
        setTouchEventListener(new Component.TouchEventListener() {
            /* class ohos.ace.ability.AceNativeComponent.AnonymousClass2 */

            @Override // ohos.agp.components.Component.TouchEventListener
            public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
                if (AceNativeComponent.this.nativeViewPtr == 0 || touchEvent.getAction() == 0) {
                    return false;
                }
                if (ALog.isDebuggable()) {
                    ALog.d(AceNativeComponent.LOG_TAG, "processTouchEventInner type = " + touchEvent.getAction() + ", screen.x = " + touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getX() + ", screen.y = " + touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getY() + ", window.x = " + touchEvent.getPointerPosition(touchEvent.getIndex()).getX() + ", window.y = " + touchEvent.getPointerPosition(touchEvent.getIndex()).getY() + ", actionId = " + touchEvent.getIndex() + ", pointerId = " + touchEvent.getPointerId(touchEvent.getIndex()) + ", force = " + touchEvent.getForcePrecision() + ", maxforce = " + touchEvent.getMaxForce() + ", radius = " + touchEvent.getRadius(touchEvent.getIndex()) + ", source = " + touchEvent.getSourceDevice() + ", deviceid = " + touchEvent.getInputDeviceId());
                    for (int i = 0; i < AceNativeComponent.this.getLocationOnScreen().length; i++) {
                        ALog.d(AceNativeComponent.LOG_TAG, "location on screen: " + AceNativeComponent.this.getLocationOnScreen()[i]);
                    }
                }
                int[] locationOnScreen = AceNativeComponent.this.getLocationOnScreen();
                if (locationOnScreen.length != 2) {
                    ALog.e(AceNativeComponent.LOG_TAG, "fail to get location");
                    return false;
                }
                ByteBuffer processTouchEvent = AceEventProcessor.processTouchEvent(touchEvent, (((float) locationOnScreen[0]) - touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getX()) + touchEvent.getPointerPosition(touchEvent.getIndex()).getX(), (((float) locationOnScreen[1]) - touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getY()) + touchEvent.getPointerPosition(touchEvent.getIndex()).getY());
                AceNativeComponent aceNativeComponent = AceNativeComponent.this;
                return aceNativeComponent.nativeDispatchPointerDataPacket(aceNativeComponent.nativeViewPtr, processTouchEvent, processTouchEvent.position());
            }
        });
        setKeyEventListener(new Component.KeyEventListener() {
            /* class ohos.ace.ability.AceNativeComponent.AnonymousClass3 */

            @Override // ohos.agp.components.Component.KeyEventListener
            public boolean onKeyEvent(Component component, KeyEvent keyEvent) {
                return false;
            }
        });
    }

    public /* synthetic */ void lambda$new$0$AceNativeComponent(Component component, Canvas canvas) {
        ALog.i(LOG_TAG, "onDraw is called");
        long nativeHandle = this.picture.getNativeHandle();
        if (nativeHandle != 0) {
            nativeDrawFrame(this.nativeViewPtr, nativeHandle);
            canvas.drawPicture(this.picture);
        }
    }

    public /* synthetic */ void lambda$new$1$AceNativeComponent(Component component) {
        boolean z;
        int width2 = getWidth();
        int height2 = getHeight();
        boolean z2 = true;
        if (this.width == width2 && this.height == height2) {
            z = false;
        } else {
            this.width = width2;
            this.height = height2;
            z = true;
        }
        int orientation2 = getOrientation();
        if (this.orientation != orientation2) {
            this.orientation = orientation2;
        } else {
            z2 = false;
        }
        if (z || z2) {
            nativeViewChanged(this.nativeViewPtr, this.width, this.height, this.orientation);
        }
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getOrientation() {
        ResourceManager resourceManager = getResourceManager();
        if (resourceManager == null) {
            ALog.e(LOG_TAG, "getOrientation failed, the resource is null");
            return 0;
        }
        Configuration configuration = resourceManager.getConfiguration();
        if (configuration != null) {
            return configuration.direction;
        }
        ALog.e(LOG_TAG, "getOrientation failed, the resource is null");
        return 0;
    }
}
