package com.huawei.iimagekit.common.agp.graphics;

public class GFX2D {
    public static final int FLIP_Y = 2;
    public static final int SRC_TEXTURE_EXTERNAL = 1;

    public static native void blit(int i, int i2, int i3);

    public static native void blit(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11);

    public static native void destroy();

    public static native void drawColor(int i);

    public static native void drawRect(int i, int i2, int i3, int i4, int i5);

    public static native void drawRect(int i, int i2, int i3, int i4, int i5, int i6);

    public static native void drawTexture(int i);

    public static native void drawTexture(int i, int i2, int i3);

    public static native void drawTexture(int i, int i2, int i3, int i4, int i5);

    public static native void drawTextureColorBalance(int i, float f, float f2);

    public static native void init();

    public static void blit(int srcTextureID, int srcX, int srcY, int srcWidth, int srcHeight, int dstTextureID, int dstX, int dstY, int dstWidth, int dstHeight) {
        blit(srcTextureID, srcX, srcY, srcWidth, srcHeight, dstTextureID, dstX, dstY, dstWidth, dstHeight, 0);
    }

    public static void blit(int srcTextureID, int dstTextureID) {
        blit(srcTextureID, dstTextureID, 0);
    }
}
