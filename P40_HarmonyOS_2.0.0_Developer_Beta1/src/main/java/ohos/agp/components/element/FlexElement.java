package ohos.agp.components.element;

import ohos.agp.utils.Rect;
import ohos.media.image.PixelMap;

public class FlexElement extends PixelMapElement {
    private Rect mRect;

    private native long nativeGetFlexElementHandle(Object obj);

    private native void nativeSetCenterBounds(long j, int[] iArr);

    public FlexElement(PixelMap pixelMap) {
        super(pixelMap);
    }

    @Override // ohos.agp.components.element.PixelMapElement, ohos.agp.components.element.Element
    public void createNativePtr(Object obj) {
        if (this.mNativeElementPtr == 0) {
            this.mNativeElementPtr = nativeGetFlexElementHandle(obj);
        }
    }

    public void setCenterBounds(Rect rect) throws IllegalArgumentException {
        if (rect == null || rect.isEmpty() || rect.left < 0 || rect.top < 0 || rect.right < 0 || rect.bottom < 0) {
            throw new IllegalArgumentException("Please make sure bounds are correct!");
        } else if (this.mUserPixelMap == null || this.mUserPixelMap.getImageInfo() == null || (rect.getWidth() <= this.mUserPixelMap.getImageInfo().size.width && rect.getHeight() <= this.mUserPixelMap.getImageInfo().size.height)) {
            this.mRect = rect;
            nativeSetCenterBounds(this.mNativeElementPtr, new int[]{this.mRect.left, this.mRect.top, this.mRect.right, this.mRect.bottom});
        } else {
            throw new IllegalArgumentException("Please make sure bounds not exceed pixel map size!");
        }
    }

    public Rect getCenterBounds() {
        return this.mRect;
    }
}
