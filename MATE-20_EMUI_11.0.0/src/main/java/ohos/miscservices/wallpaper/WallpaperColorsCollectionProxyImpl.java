package ohos.miscservices.wallpaper;

import android.app.WallpaperColors;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.rpc.RemoteException;

public class WallpaperColorsCollectionProxyImpl {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "WallpaperColorsProxyImpl");

    public static WallpaperColorsCollection fromPixelMap(PixelMap pixelMap) throws RemoteException {
        try {
            Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap(pixelMap);
            if (createShadowBitmap != null) {
                WallpaperColors fromBitmap = WallpaperColors.fromBitmap(createShadowBitmap);
                if (fromBitmap != null) {
                    return WallpaperUtils.convertToWallpaperColorsCollection(fromBitmap);
                }
                HiLog.error(TAG, "fromPixelMap:get WallpaperColors failed!", new Object[0]);
                throw new RemoteException();
            }
            HiLog.error(TAG, "fromPixelMap:convert pixelMap failed!", new Object[0]);
            throw new RemoteException();
        } catch (IllegalArgumentException unused) {
            HiLog.error(TAG, "fromPixelMap:construct from PixelMap failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public static WallpaperColorsCollection fromDrawable(Element element, Context context) throws RemoteException {
        if (element == null || context == null) {
            HiLog.error(TAG, "fromDrawable:drawable or context is null!", new Object[0]);
            throw new RemoteException();
        }
        PixelMapElement pixelMapElement = null;
        if (element instanceof PixelMapElement) {
            pixelMapElement = (PixelMapElement) element;
        }
        if (pixelMapElement != null) {
            PixelMap pixelMap = pixelMapElement.getPixelMap();
            if (pixelMap != null) {
                Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap(pixelMap);
                if (createShadowBitmap != null) {
                    android.content.Context convertFromHospContext = WallpaperUtils.convertFromHospContext(context);
                    if (convertFromHospContext != null) {
                        WallpaperColors fromDrawable = WallpaperColors.fromDrawable(new BitmapDrawable(convertFromHospContext.getResources(), createShadowBitmap));
                        if (fromDrawable != null) {
                            return WallpaperUtils.convertToWallpaperColorsCollection(fromDrawable);
                        }
                        HiLog.error(TAG, "fromDrawable:get WallpaperColors from drawable failed!", new Object[0]);
                        throw new RemoteException();
                    }
                    HiLog.error(TAG, "fromDrawable:convert context failed!", new Object[0]);
                    throw new RemoteException();
                }
                HiLog.error(TAG, "fromDrawable:convert pixelMap failed!", new Object[0]);
                throw new RemoteException();
            }
            HiLog.error(TAG, "fromDrawable:get pixelMap from Drawable failed!", new Object[0]);
            throw new RemoteException();
        }
        HiLog.error(TAG, "fromDrawable:drawable is not instance of PixelMapElement!", new Object[0]);
        throw new RemoteException();
    }
}
