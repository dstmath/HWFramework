package ohos.agp.render;

import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;
import ohos.media.image.PixelMap;
import ohos.media.image.common.ImageInfo;

public class Texture {
    private int mHeight = 0;
    protected final long mNativeTextureHandle;
    private PixelMap mPixelMap;
    private int mWidth = 0;

    private native long nativeGetTextureHandle(int i, int i2, int i3, int i4);

    private native long nativeGetTextureHandleWithPixelMap(Object obj);

    private native void nativeResetPixelMap(long j, Object obj);

    public enum ColorType {
        UNKNOWN(0),
        ALPHA_8(1),
        RGB_565(2),
        ARGB_4444(3),
        RGBA_8888(4),
        BGRA_8888(5),
        RGBA_F32(6);
        
        final int enumInt;

        private ColorType(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public enum AlphaType {
        UNKNOWN(0),
        OPAQUE(1),
        PREMUL(2),
        UNPREMUL(3);
        
        final int enumInt;

        private AlphaType(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public Texture(int i, int i2, ColorType colorType, AlphaType alphaType) {
        int value = colorType.value();
        int value2 = alphaType.value();
        this.mHeight = i2;
        this.mWidth = i;
        this.mNativeTextureHandle = nativeGetTextureHandle(i, i2, value, value2);
        MemoryCleanerRegistry.getInstance().register(this, new TextureCleaner(this.mNativeTextureHandle));
    }

    public Texture(PixelMap pixelMap) {
        ImageInfo imageInfo;
        this.mNativeTextureHandle = nativeGetTextureHandleWithPixelMap(pixelMap);
        this.mPixelMap = pixelMap;
        if (!(pixelMap == null || (imageInfo = pixelMap.getImageInfo()) == null)) {
            this.mWidth = imageInfo.size.width;
            this.mHeight = imageInfo.size.height;
        }
        MemoryCleanerRegistry.getInstance().register(this, new TextureCleaner(this.mNativeTextureHandle));
    }

    public PixelMap getPixelMap() {
        nativeResetPixelMap(this.mNativeTextureHandle, this.mPixelMap);
        return this.mPixelMap;
    }

    protected static class TextureCleaner extends NativeMemoryCleanerHelper {
        private native void nativeTextureRelease(long j);

        public TextureCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativeTextureRelease(j);
            }
        }
    }

    public long getNativeHandle() {
        return this.mNativeTextureHandle;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }
}
