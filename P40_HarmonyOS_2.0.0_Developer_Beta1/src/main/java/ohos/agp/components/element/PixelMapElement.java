package ohos.agp.components.element;

import ohos.agp.components.element.Element;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.global.resource.Resource;
import ohos.media.image.PixelMap;

public class PixelMapElement extends Element {
    private boolean mStartDecodeImmediately;
    protected PixelMap mUserPixelMap;

    private native boolean nativeGetFilterPixelMap(long j);

    private native long nativeGetPixelMapElementHandle(Object obj);

    private native long nativeGetPixelMapElementHandleByResource(Object obj, boolean z);

    private native void nativeSetFilterPixelMap(long j, boolean z);

    public PixelMapElement(PixelMap pixelMap) {
        super(pixelMap);
        this.mStartDecodeImmediately = true;
        this.mUserPixelMap = pixelMap;
    }

    public PixelMapElement(Resource resource) {
        this(resource, true);
    }

    public PixelMapElement(Resource resource, boolean z) {
        this.mStartDecodeImmediately = true;
        this.mStartDecodeImmediately = z;
        if (this.mNativeElementPtr == 0) {
            this.mNativeElementPtr = nativeGetPixelMapElementHandleByResource(resource, this.mStartDecodeImmediately);
        }
        registerCleaner();
    }

    @Override // ohos.agp.components.element.Element
    public void createNativePtr(Object obj) {
        if (this.mNativeElementPtr == 0 && (obj instanceof PixelMap)) {
            this.mNativeElementPtr = nativeGetPixelMapElementHandle(obj);
        }
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

    private void registerCleaner() {
        if (this.mNativeElementPtr != 0) {
            MemoryCleanerRegistry.getInstance().registerWithNativeBind(this, new Element.ElementCleaner(this.mNativeElementPtr), this.mNativeElementPtr);
        }
    }
}
