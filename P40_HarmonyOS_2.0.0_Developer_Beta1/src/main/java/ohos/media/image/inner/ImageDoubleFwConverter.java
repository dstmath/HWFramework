package ohos.media.image.inner;

import android.graphics.Bitmap;
import ohos.media.image.PixelMap;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.rpc.MessageParcel;
import ohos.utils.Parcel;

public class ImageDoubleFwConverter {
    private static final Logger LOGGER = LoggerFactory.getImageLogger(ImageDoubleFwConverter.class);

    private static native Bitmap nativeCreateBitmap(PixelMap pixelMap);

    private static native PixelMap nativeCreatePixelMap(Bitmap bitmap);

    private static native void nativeWriteToParcel(PixelMap pixelMap, Parcel parcel);

    static {
        LOGGER.debug("Begin loading image_converter_jni library", new Object[0]);
        System.loadLibrary("image_converter_jni.z");
    }

    public static Bitmap createShadowBitmap(PixelMap pixelMap) {
        if (pixelMap != null) {
            return nativeCreateBitmap(pixelMap);
        }
        LOGGER.error("createShadowBitmap pixelMap is null", new Object[0]);
        return null;
    }

    public static PixelMap createShellPixelMap(Bitmap bitmap) {
        if (bitmap != null) {
            return nativeCreatePixelMap(bitmap);
        }
        LOGGER.error("createShellPixelMap bitmap is null", new Object[0]);
        return null;
    }

    public static void writeToParcel(PixelMap pixelMap, Parcel parcel) {
        if (pixelMap == null) {
            LOGGER.error("writeToParcel pixelMap is null", new Object[0]);
        } else if (parcel instanceof MessageParcel) {
            nativeWriteToParcel(pixelMap, parcel);
        } else {
            LOGGER.error("writeToParcel not a MessageParcel", new Object[0]);
        }
    }
}
