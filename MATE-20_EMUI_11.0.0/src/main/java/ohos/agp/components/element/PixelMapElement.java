package ohos.agp.components.element;

import ohos.agp.components.element.Element;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.media.image.PixelMap;

public class PixelMapElement extends Element {
    private PixelMap mUserPixelMap;

    private native boolean nativeGetFilterPixelMap(long j);

    private native long nativeGetPixelMapElementHandle(Object obj);

    private native void nativeSetFilterPixelMap(long j, boolean z);

    @Override // ohos.agp.components.element.Element
    public void createNativePtr() {
    }

    public PixelMapElement(PixelMap pixelMap) {
        this.mNativeElementPtr = nativeGetPixelMapElementHandle(pixelMap);
        this.mUserPixelMap = pixelMap;
        if (this.mNativeElementPtr != 0) {
            MemoryCleanerRegistry.getInstance().registerWithNativeBind(this, new Element.ElementCleaner(this.mNativeElementPtr), this.mNativeElementPtr);
        }
        bind();
    }

    public PixelMap getPixelMap() {
        return this.mUserPixelMap;
    }

    public void setFilterPixelMap(boolean z) {
        nativeSetFilterPixelMap(this.mNativeElementPtr, z);
    }

    public boolean getFilterPixelMap() {
        return nativeGetFilterPixelMap(this.mNativeElementPtr);
    }
}
