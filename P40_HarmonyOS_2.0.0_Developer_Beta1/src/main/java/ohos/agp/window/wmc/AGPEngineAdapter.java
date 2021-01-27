package ohos.agp.window.wmc;

import android.content.Context;
import android.view.Surface;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.ComponentContainer;
import ohos.agp.window.aspbshell.AppInfoGetter;
import ohos.global.configuration.Configuration;
import ohos.global.resource.ResourceManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.MouseEvent;
import ohos.multimodalinput.event.RotationEvent;
import ohos.multimodalinput.event.TouchEvent;

public class AGPEngineAdapter implements IAGPEngineAdapter {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "AGPEngineAdapter");
    private static final int MI_FUNC_REMOVE_ALL = 3;
    private static final int MI_FUNC_SUBSCRIB = 1;
    private static final int MI_FUNC_UNSUBSCRIB = 2;
    private static final int SURFACE_STATE_CREATED = 1;
    private static final int SURFACE_STATE_HIDDEN = 3;
    private static final int SURFACE_STATE_SHOWING = 2;
    private static final int SURFACE_STATE_UNINITIALIZED = 0;
    private Context mAndroidContext;
    private Configuration mConfiguration;
    private ohos.app.Context mContext;
    private int mFlag;
    private IAGPInputListener mInputListener;
    private long mNativeWindowPtr;
    private ComponentContainer mRootView;
    private Surface mSurface;
    private int mSurfaceHeight;
    private volatile int mSurfaceState = 0;
    private int mSurfaceWidth;

    public interface IAGPInputListener {
        void onInputStart();

        void onInputStop();
    }

    private native long nativeCreate();

    private native void nativeDestroy(long j);

    private native boolean nativeDispatchKeyboardEvent(long j, KeyEvent keyEvent);

    private native boolean nativeDispatchMouseEvent(long j, MouseEvent mouseEvent);

    private native boolean nativeDispatchRotationEvent(long j, RotationEvent rotationEvent);

    private native boolean nativeDispatchTouchEvent(long j, TouchEvent touchEvent, int[] iArr, float[] fArr);

    private native boolean nativeDraw(long j, Surface surface, int i, int i2);

    private native boolean nativeLoad(long j, Surface surface, int i, int i2);

    private native void nativeNotifyBarrierFree(long j);

    private native void nativePreSetContentLayout(long j, ComponentContainer componentContainer, int i, int i2);

    private native void nativeSaveAbility(long j, ohos.app.Context context);

    private native void nativeSaveFlag(long j, int i);

    private native void nativeSetBackgroundColor(long j, int i, int i2, int i3);

    private native void nativeSetContentLayout(long j, ComponentContainer componentContainer);

    private native void nativeSetMultiModel(long j, long j2);

    private native void nativeSetTransparent(long j, int i);

    private native void nativeSetWindowOffset(long j, int i, int i2);

    private native void nativeStartRender(long j);

    private native void nativeStopRender(long j);

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public void loadEngine() {
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public void processVSync(long j) {
    }

    public AGPEngineAdapter(ohos.app.Context context, int i) {
        this.mContext = context;
        this.mFlag = i;
        HiLog.debug(LABEL, "AGPEngineAdapter", new Object[0]);
        this.mNativeWindowPtr = nativeCreate();
        long j = this.mNativeWindowPtr;
        if (j == 0) {
            HiLog.error(LABEL, "AGPWindow mNativeWindowPtr is null", new Object[0]);
        } else if (i != 10) {
            nativeSaveFlag(j, i);
            nativeSaveAbility(this.mNativeWindowPtr, context);
            if (context == null) {
                HiLog.error(LABEL, "AGPWindow context is null", new Object[0]);
                return;
            }
            Object hostContext = context.getHostContext();
            if (hostContext instanceof Context) {
                this.mAndroidContext = (Context) hostContext;
                AppInfoGetter.setAppNameToNative(this.mAndroidContext.getApplicationContext());
                return;
            }
            HiLog.error(LABEL, "AGPWindow context.getHostContext() is not android content instance", new Object[0]);
        }
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public void processSurfaceCreated(Surface surface) {
        HiLog.debug(LABEL, "processSurfaceCreated", new Object[0]);
        long j = this.mNativeWindowPtr;
        if (j != 0 && surface != null) {
            this.mSurface = surface;
            this.mSurfaceState = 1;
            nativeStartRender(j);
        }
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public void processSurfaceChanged(Surface surface, int i, int i2, int i3) {
        HiLog.debug(LABEL, "ProcessSurfaceChanged w=%{public}d, h=%{public}d", new Object[]{Integer.valueOf(i2), Integer.valueOf(i3)});
        if (this.mSurfaceState != 0) {
            long j = this.mNativeWindowPtr;
            if (j != 0 && surface != null) {
                this.mSurface = surface;
                this.mSurfaceWidth = i2;
                this.mSurfaceHeight = i3;
                nativeLoad(j, surface, i2, i3);
                this.mSurfaceState = 2;
            }
        }
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public void processSurfaceDestroy(Surface surface) {
        HiLog.debug(LABEL, "processSurfaceDestroy", new Object[0]);
        if (this.mSurfaceState != 0 && this.mNativeWindowPtr != 0 && surface != null) {
            if (this.mSurfaceState >= 2) {
                nativeStopRender(this.mNativeWindowPtr);
            }
            this.mSurfaceState = 0;
        }
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public boolean processTouchEvent(TouchEvent touchEvent) {
        if (this.mNativeWindowPtr == 0 || touchEvent == null) {
            return false;
        }
        int pointerCount = touchEvent.getPointerCount();
        float[] fArr = new float[(pointerCount * 3)];
        for (int i = 0; i < pointerCount; i++) {
            MmiPoint pointerPosition = touchEvent.getPointerPosition(i);
            int pointerId = touchEvent.getPointerId(i);
            int i2 = i * 3;
            fArr[i2] = pointerPosition.getX();
            fArr[i2 + 1] = pointerPosition.getY();
            fArr[i2 + 2] = (float) pointerId;
        }
        return nativeDispatchTouchEvent(this.mNativeWindowPtr, touchEvent, new int[]{touchEvent.getIndex(), touchEvent.getAction(), touchEvent.getPhase()}, fArr);
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public boolean processKeyEvent(KeyEvent keyEvent) {
        if (keyEvent == null) {
            return false;
        }
        long j = this.mNativeWindowPtr;
        if (j == 0) {
            return false;
        }
        return nativeDispatchKeyboardEvent(j, keyEvent);
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public boolean processMouseEvent(MouseEvent mouseEvent) {
        if (this.mNativeWindowPtr == 0) {
            return false;
        }
        boolean z = (mouseEvent.getPressedButtons() & 1) != 0;
        if (mouseEvent.getActionButton() == 1 || z) {
            HiLog.debug(LABEL, "processMouseEvent of left button.", new Object[0]);
            return nativeDispatchMouseEvent(this.mNativeWindowPtr, mouseEvent);
        }
        HiLog.debug(LABEL, "processMouseEvent of other buttons.", new Object[0]);
        return false;
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public boolean processRotationEvent(RotationEvent rotationEvent) {
        HiLog.debug(LABEL, "processMouseEvent", new Object[0]);
        long j = this.mNativeWindowPtr;
        if (j == 0) {
            return false;
        }
        return nativeDispatchRotationEvent(j, rotationEvent);
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public void processDestroy() {
        HiLog.debug(LABEL, "processDestroy", new Object[0]);
        if (this.mNativeWindowPtr != 0) {
            if (this.mSurfaceState >= 2) {
                nativeStopRender(this.mNativeWindowPtr);
            }
            this.mSurfaceState = 0;
            long j = this.mNativeWindowPtr;
            if (j != 0) {
                nativeDestroy(j);
                this.mNativeWindowPtr = 0;
            }
            this.mInputListener = null;
            this.mContext = null;
        }
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public void processConfigurationChanged(android.content.res.Configuration configuration) {
        ohos.app.Context context;
        HiLog.debug(LABEL, "processConfigurationChanged", new Object[0]);
        if (this.mRootView == null || configuration == null || (context = this.mContext) == null) {
            HiLog.error(LABEL, "processConfigurationChanged: conditon is fail.", new Object[0]);
            return;
        }
        ResourceManager resourceManager = context.getResourceManager();
        if (resourceManager == null) {
            HiLog.error(LABEL, "processConfigurationChanged: can not get ResourceManager.", new Object[0]);
            return;
        }
        if (this.mConfiguration == null) {
            this.mConfiguration = resourceManager.getConfiguration();
            if (this.mConfiguration == null) {
                HiLog.error(LABEL, "processConfigurationChanged: can not get mConfiguration.", new Object[0]);
                return;
            }
        }
        if (((double) Math.abs(this.mConfiguration.fontRatio - configuration.fontScale)) < 1.0E-7d) {
            HiLog.debug(LABEL, "processConfigurationChanged: fontSize not change.", new Object[0]);
            return;
        }
        this.mConfiguration.fontRatio = configuration.fontScale;
        this.mRootView.informConfigurationChanged(this.mConfiguration);
    }

    public void setContentLayout(ComponentContainer componentContainer) {
        if (this.mNativeWindowPtr != 0 && componentContainer != null) {
            this.mRootView = componentContainer;
            HiLog.debug(LABEL, "setContentLayout", new Object[0]);
            nativeSetContentLayout(this.mNativeWindowPtr, this.mRootView);
        }
    }

    public void setPreContentLayout(ComponentContainer componentContainer, int i, int i2) {
        HiLog.debug(LABEL, "setPreContentLayout", new Object[0]);
        long j = this.mNativeWindowPtr;
        if (j != 0 && componentContainer != null) {
            nativePreSetContentLayout(j, componentContainer, i, i2);
        }
    }

    public void setBackgroundColor(int i, int i2, int i3) {
        long j = this.mNativeWindowPtr;
        if (j != 0) {
            nativeSetBackgroundColor(j, i, i2, i3);
        }
    }

    /* access modifiers changed from: protected */
    public void setWindowOffset(int i, int i2) {
        HiLog.debug(LABEL, "NativesetWindowOffset x=%{public}d y=%{public}d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        long j = this.mNativeWindowPtr;
        if (j != 0) {
            nativeSetWindowOffset(j, i, i2);
        }
    }

    public void setTransparent(boolean z) {
        long j = this.mNativeWindowPtr;
        if (j != 0) {
            if (z) {
                nativeSetTransparent(j, 1);
            } else {
                nativeSetTransparent(j, 0);
            }
        }
    }

    public void draw() {
        HiLog.debug(LABEL, "draw", new Object[0]);
        if (this.mSurface != null) {
            HiLog.debug(LABEL, "mSurface != null", new Object[0]);
            draw(this.mSurface, this.mSurfaceWidth, this.mSurfaceHeight);
        }
    }

    public void draw(Surface surface, int i, int i2) {
        HiLog.debug(LABEL, "draw", new Object[0]);
        if (this.mNativeWindowPtr != 0 && this.mSurface != null) {
            HiLog.debug(LABEL, "mNativeWindowPtr != 0", new Object[0]);
            nativeDraw(this.mNativeWindowPtr, surface, i, i2);
        }
    }

    public void setMultiModel(long j) {
        if (this.mNativeWindowPtr != 0 && j != 0) {
            HiLog.debug(LABEL, "MMI: setMultiModel to native.", new Object[0]);
            nativeSetMultiModel(this.mNativeWindowPtr, j);
        }
    }

    public void setInputListener(IAGPInputListener iAGPInputListener) {
        this.mInputListener = iAGPInputListener;
    }

    public void startInput() {
        IAGPInputListener iAGPInputListener = this.mInputListener;
        if (iAGPInputListener != null) {
            iAGPInputListener.onInputStart();
        }
    }

    public void stopInput() {
        IAGPInputListener iAGPInputListener = this.mInputListener;
        if (iAGPInputListener != null) {
            iAGPInputListener.onInputStop();
        }
    }

    /* access modifiers changed from: protected */
    public void notifyBarrierFree() {
        HiLog.debug(LABEL, "notifyBarrierFree", new Object[0]);
        long j = this.mNativeWindowPtr;
        if (j != 0) {
            nativeNotifyBarrierFree(j);
        }
    }
}
