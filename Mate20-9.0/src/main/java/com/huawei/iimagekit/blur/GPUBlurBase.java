package com.huawei.iimagekit.blur;

import android.content.Context;
import android.graphics.Bitmap;
import com.huawei.iimagekit.common.agp.graphics.porting.gl.GFX;
import com.huawei.iimagekit.common.agp.graphics.porting.gl.GFXUtil;
import com.huawei.iimagekit.common.agp.graphics.porting.gl.OffscreenGraphicsContext;

public class GPUBlurBase {
    private static boolean initOnce = true;
    private int mDstTexture = -1;
    private int mSrcTexture = -1;

    public static void initContext(Context context) {
        if (initOnce) {
            OffscreenGraphicsContext.init(context);
            initOnce = false;
        }
    }

    public void doBlur(Bitmap bitmapForBlur, Bitmap blurredBitmap, int radius) {
        this.mSrcTexture = GFX.initTexture(bitmapForBlur.getWidth(), bitmapForBlur.getHeight());
        GFXUtil.updateTexture(this.mSrcTexture, bitmapForBlur);
        this.mDstTexture = GFX.initTexture(blurredBitmap.getWidth(), blurredBitmap.getHeight());
        doBlur(this.mSrcTexture, this.mDstTexture, radius);
        GFXUtil.updateBitmap(blurredBitmap, this.mDstTexture);
        GFX.destroyTexture(this.mSrcTexture);
        GFX.destroyTexture(this.mDstTexture);
    }

    public void doBlur(int srcTexture, int dstTexture, int radius) {
    }
}
