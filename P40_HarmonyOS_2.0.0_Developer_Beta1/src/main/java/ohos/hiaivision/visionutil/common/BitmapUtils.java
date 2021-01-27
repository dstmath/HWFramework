package ohos.hiaivision.visionutil.common;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import java.util.Optional;
import ohos.ai.engine.utils.HiAILog;

public class BitmapUtils {
    private static final String TAG = "BitmapUtils";

    private BitmapUtils() {
    }

    public static Optional<Bitmap> resizeBitmap(Bitmap bitmap, int i) {
        if (bitmap == null) {
            HiAILog.error(TAG, "bitmap is null");
            return Optional.empty();
        }
        int byteCount = bitmap.getByteCount();
        if (byteCount == 0) {
            HiAILog.error(TAG, "byte count of bitmap is zero");
            return Optional.empty();
        }
        Matrix matrix = new Matrix();
        float sqrt = (float) Math.sqrt((double) (((float) i) / ((float) byteCount)));
        HiAILog.info(TAG, "compressSize = " + sqrt);
        matrix.postScale(sqrt, sqrt);
        return Optional.of(Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true));
    }

    public static Optional<Bitmap> resizeBitmap(Bitmap bitmap, int i, int i2) {
        if (bitmap == null) {
            HiAILog.error(TAG, "inputBitmap is null");
            return Optional.empty();
        }
        HiAILog.debug(TAG, "resizeBitmap started");
        long currentTimeMillis = System.currentTimeMillis();
        Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bitmap, i, i2, true);
        createScaledBitmap.setConfig(Bitmap.Config.ARGB_8888);
        long currentTimeMillis2 = System.currentTimeMillis();
        HiAILog.debug(TAG, "resizeBitmap stopped, cost " + (currentTimeMillis2 - currentTimeMillis) + " ms.");
        return Optional.of(createScaledBitmap);
    }
}
