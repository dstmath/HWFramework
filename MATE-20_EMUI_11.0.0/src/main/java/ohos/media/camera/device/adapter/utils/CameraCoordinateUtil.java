package ohos.media.camera.device.adapter.utils;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import java.util.Optional;
import ohos.agp.utils.Rect;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.Scope;

public class CameraCoordinateUtil {
    private static final int CHECK_AREA_LENGTH_RATIO = 3;
    private static final int COORDINATE_HALF_LENGTH = 1000;
    private static final int COORDINATE_LENGTH = 2000;
    private static final int HALF = 2;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraCoordinateUtil.class);
    private static final float ZOOM_MIN = 0.1f;

    public static Rect screenToDriver(Rect rect, Size size, Rect rect2, Rect rect3) {
        if (rect == null || rect3 == null || size == null || rect.left < -1000 || rect.top < -1000 || rect.right > 1000 || rect.bottom > 1000) {
            return null;
        }
        LOGGER.debug("screenToDriver in: %{public}s, previewSize: %{public}s, cropRegion: %{public}s, sensorArray: %{public}s", rect, size, rect2, rect3);
        Matrix matrix = getMatrix(false, size, rect2, rect3);
        RectF rectF = new RectF();
        matrix.mapRect(rectF, new RectF(Converter.convert2ARect(rect)));
        Rect rectf2Rect = Converter.rectf2Rect(rectF);
        LOGGER.debug("screenToDriver screen: %{public}s to driver: %{public}s", rect.toString(), rectf2Rect.toString());
        return rectf2Rect;
    }

    public static Optional<Rect> driverToScreen(Rect rect, Rect rect2, Rect rect3) {
        if (rect == null || rect2 == null) {
            LOGGER.error("driverToScreen parameter invalid.", new Object[0]);
            return Optional.empty();
        }
        LOGGER.debug("driverToScreen input region is %{public}s.", rect.toString());
        return Optional.of(Converter.rectf2Rect(getMatrix(true, rect2, rect3), rect));
    }

    public static Optional<Rect> driverToScreen(Rect rect, Size size, Rect rect2, Rect rect3) {
        if (rect == null || rect3 == null || size == null) {
            return Optional.empty();
        }
        Matrix matrix = getMatrix(true, size, rect2, rect3);
        RectF rectF = new RectF();
        matrix.mapRect(rectF, new RectF(Converter.convert2ARect(rect)));
        Rect rectf2Rect = Converter.rectf2Rect(rectF);
        LOGGER.debug("driver: %{public}s to screen: %{public}s", rect.toString(), rectf2Rect.toString());
        return Optional.ofNullable(rectf2Rect);
    }

    public static int[] screenToPreview(Rect rect, Size size) {
        if (size == null) {
            return new int[0];
        }
        if (rect == null) {
            return new int[0];
        }
        LOGGER.debug("screenToPreview, preview size is %{public}s", size);
        Point point = new Point(rect.right, rect.bottom);
        point.x = (int) ((((double) (point.x + 1000)) / 2000.0d) * ((double) size.width));
        point.y = (int) ((((double) (point.y + 1000)) / 2000.0d) * ((double) size.height));
        Point point2 = new Point(rect.left, rect.top);
        point2.x = (int) ((((double) (point2.x + 1000)) / 2000.0d) * ((double) size.width));
        point2.y = (int) ((((double) (point2.y + 1000)) / 2000.0d) * ((double) size.height));
        int max = Math.max(point.x - point2.x, point.y - point2.y);
        int min = Math.min(size.width, size.height) / 3;
        if (max < min) {
            max = min;
        }
        LOGGER.debug("SuperSlowMotion Check Area, checkAreaLength = %{public}d", Integer.valueOf(max));
        Point point3 = new Point((point2.x + point.x) / 2, (point2.y + point.y) / 2);
        Point point4 = new Point();
        int i = max / 2;
        point4.x = point3.x - i;
        point4.y = point3.y + i;
        point4.x = ((Integer) Scope.create(0, Integer.valueOf(size.width - max)).clamp(Integer.valueOf(point4.x))).intValue();
        point4.y = ((Integer) Scope.create(Integer.valueOf(max), Integer.valueOf(size.height)).clamp(Integer.valueOf(point4.y))).intValue();
        LOGGER.debug("SuperSlowMotion Check Area, the leftTop point is %{public}d", Integer.valueOf(max));
        return new int[]{point4.x, point4.y};
    }

    public static Optional<Rect> getCropRegion(Rect rect, float f, float f2) {
        if (rect == null) {
            LOGGER.error("getCropRegion failed, sensorArray is null", new Object[0]);
            return Optional.empty();
        } else if (f2 < ZOOM_MIN) {
            LOGGER.error("getCropRegion failed, zoom is too small %{public}f", Float.valueOf(f2));
            return Optional.empty();
        } else {
            int i = (rect.left + rect.right) / 2;
            int i2 = (rect.top + rect.bottom) / 2;
            int i3 = rect.right - rect.left;
            int i4 = rect.bottom - rect.top;
            float f3 = (float) i3;
            double d = (double) ((f3 / f2) / 2.0f);
            int floor = (int) Math.floor(d);
            double d2 = (double) ((((float) i4) / f2) / 2.0f);
            int floor2 = (int) Math.floor(d2);
            if ((f3 / 2.0f) / ((float) floor) > f) {
                floor = (int) Math.ceil(d);
                floor2 = (int) Math.ceil(d2);
            }
            LOGGER.debug("getCropRegion ratio = %{public}f", Float.valueOf(f3 / ((float) (floor * 2))));
            Rect rect2 = new Rect(i - floor, i2 - floor2, i + floor, i2 + floor2);
            LOGGER.debug("getCropRegion actual region = %{public}s", rect2.toString());
            return Optional.of(rect2);
        }
    }

    private static Matrix getMatrix(boolean z, Rect rect, Rect rect2) {
        int i;
        Matrix matrix = new Matrix();
        Rect rect3 = rect2 == null ? rect : rect2;
        int i2 = (rect.left + rect.right) >> 1;
        int i3 = (rect.top + rect.bottom) >> 1;
        float f = (float) (i2 - ((rect3.left + rect3.right) >> 1));
        float f2 = (float) (i3 - ((rect3.top + rect3.bottom) >> 1));
        int i4 = rect.right - rect.left;
        int i5 = rect.bottom - rect.top;
        int i6 = 0;
        if (rect2 != null) {
            i6 = rect2.right - rect2.left;
            i = rect2.bottom - rect2.top;
        } else {
            i = 0;
        }
        matrix.postTranslate(f, f2);
        if (!(i6 == 0 || i == 0)) {
            postScale(matrix, ((float) i4) / ((float) i6), ((float) i5) / ((float) i), (float) i2, (float) i3);
        }
        if (!(i4 == 0 || i5 == 0)) {
            postScale(matrix, 2000.0f / ((float) i4), 2000.0f / ((float) i5));
        }
        matrix.postTranslate(-1000.0f, -1000.0f);
        if (z) {
            return matrix;
        }
        Matrix matrix2 = new Matrix();
        matrix.invert(matrix2);
        return matrix2;
    }

    private static void postScale(Matrix matrix, float f, float f2, float f3, float f4) {
        boolean z = Float.isInfinite(f) || Float.isInfinite(f2);
        boolean z2 = Float.isInfinite(f3) || Float.isInfinite(f4);
        if (!z && !z2) {
            LOGGER.debug("postScale infinite float sx: %{public}f, sy: %{public}f, px: %{public}f, py: %{public}f", Float.valueOf(f), Float.valueOf(f2), Float.valueOf(f3), Float.valueOf(f4));
            matrix.postScale(f, f2, f3, f4);
        }
    }

    private static void postScale(Matrix matrix, float f, float f2) {
        if (!Float.isInfinite(f) && !Float.isInfinite(f2)) {
            LOGGER.debug("postScale infinite float sx: %{public}f, sy: %{public}f", Float.valueOf(f), Float.valueOf(f2));
            matrix.postScale(f, f2);
        }
    }

    private static Matrix getMatrix(boolean z, Size size, Rect rect, Rect rect2) {
        Matrix matrix = new Matrix();
        if (rect == null) {
            rect = rect2;
        }
        int i = rect.bottom - rect.top;
        int i2 = rect.right - rect.left;
        int i3 = (rect.left + rect.right) / 2;
        int i4 = (rect.top + rect.bottom) / 2;
        int i5 = rect2.bottom - rect2.top;
        int i6 = rect2.right - rect2.left;
        int i7 = (rect2.left + rect2.right) / 2;
        int i8 = (rect2.top + rect2.bottom) / 2;
        if (!(i == 0 || size.height == 0)) {
            float f = ((float) size.width) / ((float) size.height);
            float f2 = ((float) i2) / ((float) i);
            if (f > f2) {
                postScale(matrix, 1.0f, f / f2, (float) i3, (float) i4);
            } else {
                postScale(matrix, f2 / f, 1.0f, (float) i3, (float) i4);
            }
        }
        matrix.postTranslate((float) (i7 - i3), (float) (i8 - i4));
        if (!(i2 == 0 || i == 0)) {
            postScale(matrix, ((float) i6) / ((float) i2), ((float) i5) / ((float) i), (float) i7, (float) i8);
        }
        if (!(i5 == 0 || i6 == 0)) {
            postScale(matrix, 2000.0f / ((float) i6), 2000.0f / ((float) i5));
        }
        matrix.postTranslate(-1000.0f, -1000.0f);
        if (z) {
            return matrix;
        }
        Matrix matrix2 = new Matrix();
        matrix.invert(matrix2);
        return matrix2;
    }
}
