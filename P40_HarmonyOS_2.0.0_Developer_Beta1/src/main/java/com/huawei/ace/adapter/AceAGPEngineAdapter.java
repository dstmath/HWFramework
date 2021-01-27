package com.huawei.ace.adapter;

import ohos.agp.components.surfaceview.adapter.SurfaceUtils;
import ohos.agp.graphics.Surface;
import ohos.agp.window.wmc.IAGPEngineAdapter;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.MouseEvent;
import ohos.multimodalinput.event.RotationEvent;
import ohos.multimodalinput.event.TouchEvent;

public class AceAGPEngineAdapter implements IAGPEngineAdapter {
    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public void loadEngine() {
    }

    /* access modifiers changed from: protected */
    public void processDestroyInner() {
    }

    /* access modifiers changed from: protected */
    public boolean processKeyEventInner(KeyEvent keyEvent) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processMouseEventInner(MouseEvent mouseEvent) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processRotationEventInner(RotationEvent rotationEvent) {
        return true;
    }

    /* access modifiers changed from: protected */
    public void processSurfaceChangedInner(Surface surface, int i, int i2, int i3) {
    }

    /* access modifiers changed from: protected */
    public void processSurfaceCreatedInner(Surface surface) {
    }

    /* access modifiers changed from: protected */
    public void processSurfaceDestroyInner(Surface surface) {
    }

    /* access modifiers changed from: protected */
    public boolean processTouchEventInner(TouchEvent touchEvent) {
        return true;
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public void processVSync(long j) {
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public void processSurfaceCreated(android.view.Surface surface) {
        processSurfaceCreatedInner(SurfaceUtils.getSurface(surface));
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public void processSurfaceChanged(android.view.Surface surface, int i, int i2, int i3) {
        processSurfaceChangedInner(SurfaceUtils.getSurface(surface), i, i2, i3);
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public void processSurfaceDestroy(android.view.Surface surface) {
        processSurfaceDestroyInner(SurfaceUtils.getSurface(surface));
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public boolean processTouchEvent(TouchEvent touchEvent) {
        return processTouchEventInner(touchEvent);
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public boolean processKeyEvent(KeyEvent keyEvent) {
        return processKeyEventInner(keyEvent);
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public boolean processMouseEvent(MouseEvent mouseEvent) {
        return processMouseEventInner(mouseEvent);
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public boolean processRotationEvent(RotationEvent rotationEvent) {
        return processRotationEventInner(rotationEvent);
    }

    @Override // ohos.agp.window.wmc.IAGPEngineAdapter
    public void processDestroy() {
        processDestroyInner();
    }

    /* access modifiers changed from: protected */
    public android.view.Surface getASurface(Surface surface) {
        return SurfaceUtils.getSurfaceImpl(surface);
    }
}
