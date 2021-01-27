package android.graphics.drawable;

import android.graphics.Bitmap;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.Log;
import java.lang.reflect.Field;

public class LayerDrawableEx extends LayerDrawable {
    private static final String TAG = "LayerDrawableEx";
    private LayerDrawable.LayerState mLayerState;
    private LayerDrawable mLd;
    private NinePatchDrawable mNpd;
    private NinePatchDrawable.NinePatchState mPatchState;

    private LayerDrawable.LayerState getLayerState() {
        LayerDrawable layerDrawable = this.mLd;
        if (layerDrawable == null) {
            return null;
        }
        return layerDrawable.mLayerState;
    }

    private NinePatchDrawable.NinePatchState getPatchState() {
        NinePatchDrawable ninePatchDrawable = this.mNpd;
        if (ninePatchDrawable == null) {
            return null;
        }
        try {
            Field patchFiled = ninePatchDrawable.getClass().getDeclaredField("mNinePatchState");
            boolean isAccflag = patchFiled.isAccessible();
            patchFiled.setAccessible(true);
            Object obj = patchFiled.get(this.mNpd);
            if (obj instanceof NinePatchDrawable.NinePatchState) {
                this.mPatchState = (NinePatchDrawable.NinePatchState) obj;
            }
            patchFiled.setAccessible(isAccflag);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.d(TAG, "reflect exception" + e.getMessage());
        }
        return this.mPatchState;
    }

    public int getCacheSize(Object drawable) {
        if (drawable instanceof BitmapDrawable) {
            return getBitmapDrawableCacheSize((BitmapDrawable) drawable);
        }
        if (drawable instanceof LayerDrawable) {
            return getLayerDrawableCacheSize((LayerDrawable) drawable);
        }
        if (drawable instanceof NinePatchDrawable) {
            return getNinePatchCacheSize((NinePatchDrawable) drawable);
        }
        return 0;
    }

    private int getBitmapDrawableCacheSize(BitmapDrawable drawable) {
        Bitmap bitmap = drawable.getBitmap();
        if (bitmap != null) {
            return bitmap.getAllocationByteCount();
        }
        return 0;
    }

    private int getLayerDrawableCacheSize(LayerDrawable drawable) {
        this.mLd = drawable;
        this.mLayerState = getLayerState();
        LayerDrawable.LayerState layerState = this.mLayerState;
        if (layerState == null) {
            Log.d(TAG, "LayerState null");
            return 0;
        }
        int cacheSize = 0;
        int childNum = layerState.mNumChildren;
        for (int i = 0; i < childNum; i++) {
            Object obj = this.mLayerState.mChildren[i].mDrawable;
            if (obj != null) {
                if (obj instanceof BitmapDrawable) {
                    cacheSize += getBitmapDrawableCacheSize((BitmapDrawable) obj);
                }
                if (obj instanceof NinePatchDrawable) {
                    cacheSize += getNinePatchCacheSize((NinePatchDrawable) obj);
                }
            }
        }
        return cacheSize;
    }

    private int getNinePatchCacheSize(NinePatchDrawable drawable) {
        this.mNpd = drawable;
        this.mPatchState = getPatchState();
        NinePatchDrawable.NinePatchState ninePatchState = this.mPatchState;
        if (ninePatchState == null) {
            Log.d(TAG, "PatchState null");
            return 0;
        } else if (ninePatchState.mNinePatch != null && this.mPatchState.mNinePatch.getBitmap() != null) {
            return this.mPatchState.mNinePatch.getBitmap().getAllocationByteCount();
        } else {
            Log.d(TAG, "bitmap null");
            return 0;
        }
    }
}
