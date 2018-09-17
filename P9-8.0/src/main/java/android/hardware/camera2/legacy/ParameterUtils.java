package android.hardware.camera2.legacy;

import android.app.Instrumentation;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.TonemapCurve;
import android.hardware.camera2.utils.ParamsUtils;
import android.hardware.camera2.utils.SizeAreaComparator;
import android.media.MediaFormat;
import android.media.tv.TvContract.PreviewPrograms;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParameterUtils {
    private static final double ASPECT_RATIO_TOLERANCE = 0.05000000074505806d;
    public static final Area CAMERA_AREA_DEFAULT = new Area(new Rect(NORMALIZED_RECTANGLE_DEFAULT), 1);
    private static final boolean DEBUG = false;
    public static final Rect NORMALIZED_RECTANGLE_DEFAULT = new Rect(-1000, -1000, 1000, 1000);
    public static final int NORMALIZED_RECTANGLE_MAX = 1000;
    public static final int NORMALIZED_RECTANGLE_MIN = -1000;
    public static final Rect RECTANGLE_EMPTY = new Rect(0, 0, 0, 0);
    private static final String TAG = "ParameterUtils";
    private static final int ZOOM_RATIO_MULTIPLIER = 100;

    public static class MeteringData {
        public final Area meteringArea;
        public final Rect previewMetering;
        public final Rect reportedMetering;

        public MeteringData(Area meteringArea, Rect previewMetering, Rect reportedMetering) {
            this.meteringArea = meteringArea;
            this.previewMetering = previewMetering;
            this.reportedMetering = reportedMetering;
        }
    }

    public static class WeightedRectangle {
        public final Rect rect;
        public final int weight;

        public WeightedRectangle(Rect rect, int weight) {
            this.rect = (Rect) Preconditions.checkNotNull(rect, "rect must not be null");
            this.weight = weight;
        }

        public MeteringRectangle toMetering() {
            return new MeteringRectangle(clipLower(this.rect.left, 0, this.rect, "left"), clipLower(this.rect.top, 0, this.rect, "top"), clipLower(this.rect.width(), 0, this.rect, MediaFormat.KEY_WIDTH), clipLower(this.rect.height(), 0, this.rect, MediaFormat.KEY_HEIGHT), clip(this.weight, 0, 1000, this.rect, PreviewPrograms.COLUMN_WEIGHT));
        }

        public Face toFace(int id, Point leftEyePosition, Point rightEyePosition, Point mouthPosition) {
            int idSafe = clipLower(id, 0, this.rect, Instrumentation.REPORT_KEY_IDENTIFIER);
            return new Face(this.rect, clip(this.weight, 1, 100, this.rect, "score"), idSafe, leftEyePosition, rightEyePosition, mouthPosition);
        }

        public Face toFace() {
            return new Face(this.rect, clip(this.weight, 1, 100, this.rect, "score"));
        }

        private static int clipLower(int value, int lo, Rect rect, String name) {
            return clip(value, lo, Integer.MAX_VALUE, rect, name);
        }

        private static int clip(int value, int lo, int hi, Rect rect, String name) {
            if (value < lo) {
                Log.w(ParameterUtils.TAG, "toMetering - Rectangle " + rect + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + name + " too small, clip to " + lo);
                return lo;
            } else if (value <= hi) {
                return value;
            } else {
                Log.w(ParameterUtils.TAG, "toMetering - Rectangle " + rect + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + name + " too small, clip to " + hi);
                return hi;
            }
        }
    }

    public static class ZoomData {
        public final Rect previewCrop;
        public final Rect reportedCrop;
        public final int zoomIndex;

        public ZoomData(int zoomIndex, Rect previewCrop, Rect reportedCrop) {
            this.zoomIndex = zoomIndex;
            this.previewCrop = previewCrop;
            this.reportedCrop = reportedCrop;
        }
    }

    public static Size convertSize(Camera.Size size) {
        Preconditions.checkNotNull(size, "size must not be null");
        return new Size(size.width, size.height);
    }

    public static List<Size> convertSizeList(List<Camera.Size> sizeList) {
        Preconditions.checkNotNull(sizeList, "sizeList must not be null");
        List<Size> sizes = new ArrayList(sizeList.size());
        for (Camera.Size s : sizeList) {
            sizes.add(new Size(s.width, s.height));
        }
        return sizes;
    }

    public static Size[] convertSizeListToArray(List<Camera.Size> sizeList) {
        Preconditions.checkNotNull(sizeList, "sizeList must not be null");
        Size[] array = new Size[sizeList.size()];
        int ctr = 0;
        for (Camera.Size s : sizeList) {
            int ctr2 = ctr + 1;
            array[ctr] = new Size(s.width, s.height);
            ctr = ctr2;
        }
        return array;
    }

    public static boolean containsSize(List<Camera.Size> sizeList, int width, int height) {
        Preconditions.checkNotNull(sizeList, "sizeList must not be null");
        for (Camera.Size s : sizeList) {
            if (s.height == height && s.width == width) {
                return true;
            }
        }
        return false;
    }

    public static Size getLargestSupportedJpegSizeByArea(Parameters params) {
        Preconditions.checkNotNull(params, "params must not be null");
        return SizeAreaComparator.findLargestByArea(convertSizeList(params.getSupportedPictureSizes()));
    }

    public static String stringFromArea(Area area) {
        if (area == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Rect r = area.rect;
        sb.setLength(0);
        sb.append("([");
        sb.append(r.left);
        sb.append(',');
        sb.append(r.top);
        sb.append("][");
        sb.append(r.right);
        sb.append(',');
        sb.append(r.bottom);
        sb.append(']');
        sb.append(',');
        sb.append(area.weight);
        sb.append(')');
        return sb.toString();
    }

    public static String stringFromAreaList(List<Area> areaList) {
        StringBuilder sb = new StringBuilder();
        if (areaList == null) {
            return null;
        }
        int i = 0;
        for (Area area : areaList) {
            if (area == null) {
                sb.append("null");
            } else {
                sb.append(stringFromArea(area));
            }
            if (i != areaList.size() - 1) {
                sb.append(", ");
            }
            i++;
        }
        return sb.toString();
    }

    public static int getClosestAvailableZoomCrop(Parameters params, Rect activeArray, Size streamSize, Rect cropRegion, Rect reportedCropRegion, Rect previewCropRegion) {
        Preconditions.checkNotNull(params, "params must not be null");
        Preconditions.checkNotNull(activeArray, "activeArray must not be null");
        Preconditions.checkNotNull(streamSize, "streamSize must not be null");
        Preconditions.checkNotNull(reportedCropRegion, "reportedCropRegion must not be null");
        Preconditions.checkNotNull(previewCropRegion, "previewCropRegion must not be null");
        Rect actualCrop = new Rect(cropRegion);
        if (!actualCrop.intersect(activeArray)) {
            Log.w(TAG, "getClosestAvailableZoomCrop - Crop region out of range; setting to active array size");
            actualCrop.set(activeArray);
        }
        Rect cropRegionAsPreview = shrinkToSameAspectRatioCentered(getPreviewCropRectangleUnzoomed(activeArray, streamSize), actualCrop);
        Rect bestReportedCropRegion = null;
        Rect bestPreviewCropRegion = null;
        int bestZoomIndex = -1;
        List<Rect> availableReportedCropRegions = getAvailableZoomCropRectangles(params, activeArray);
        List<Rect> availablePreviewCropRegions = getAvailablePreviewZoomCropRectangles(params, activeArray, streamSize);
        if (availableReportedCropRegions.size() != availablePreviewCropRegions.size()) {
            throw new AssertionError("available reported/preview crop region size mismatch");
        }
        for (int i = 0; i < availableReportedCropRegions.size(); i++) {
            boolean isBest;
            Rect currentPreviewCropRegion = (Rect) availablePreviewCropRegions.get(i);
            Rect currentReportedCropRegion = (Rect) availableReportedCropRegions.get(i);
            if (bestZoomIndex == -1) {
                isBest = true;
            } else if (currentPreviewCropRegion.width() < cropRegionAsPreview.width() || currentPreviewCropRegion.height() < cropRegionAsPreview.height()) {
                isBest = false;
            } else {
                isBest = true;
            }
            if (!isBest) {
                break;
            }
            bestPreviewCropRegion = currentPreviewCropRegion;
            bestReportedCropRegion = currentReportedCropRegion;
            bestZoomIndex = i;
        }
        if (bestZoomIndex == -1) {
            throw new AssertionError("Should've found at least one valid zoom index");
        }
        reportedCropRegion.set(bestReportedCropRegion);
        previewCropRegion.set(bestPreviewCropRegion);
        return bestZoomIndex;
    }

    private static Rect getPreviewCropRectangleUnzoomed(Rect activeArray, Size previewSize) {
        if (previewSize.getWidth() > activeArray.width()) {
            throw new IllegalArgumentException("previewSize must not be wider than activeArray");
        } else if (previewSize.getHeight() > activeArray.height()) {
            throw new IllegalArgumentException("previewSize must not be taller than activeArray");
        } else {
            float cropH;
            float cropW;
            float aspectRatioArray = (((float) activeArray.width()) * 1.0f) / ((float) activeArray.height());
            float aspectRatioPreview = (((float) previewSize.getWidth()) * 1.0f) / ((float) previewSize.getHeight());
            if (((double) Math.abs(aspectRatioPreview - aspectRatioArray)) < ASPECT_RATIO_TOLERANCE) {
                cropH = (float) activeArray.height();
                cropW = (float) activeArray.width();
            } else if (aspectRatioPreview < aspectRatioArray) {
                cropH = (float) activeArray.height();
                cropW = cropH * aspectRatioPreview;
            } else {
                cropW = (float) activeArray.width();
                cropH = cropW / aspectRatioPreview;
            }
            Matrix translateMatrix = new Matrix();
            RectF cropRect = new RectF(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, cropW, cropH);
            translateMatrix.setTranslate(activeArray.exactCenterX(), activeArray.exactCenterY());
            translateMatrix.postTranslate(-cropRect.centerX(), -cropRect.centerY());
            translateMatrix.mapRect(cropRect);
            return ParamsUtils.createRect(cropRect);
        }
    }

    private static Rect shrinkToSameAspectRatioCentered(Rect reference, Rect shrinkTarget) {
        float cropH;
        float cropW;
        float aspectRatioShrinkTarget = (((float) shrinkTarget.width()) * 1.0f) / ((float) shrinkTarget.height());
        if (aspectRatioShrinkTarget < (((float) reference.width()) * 1.0f) / ((float) reference.height())) {
            cropH = (float) reference.height();
            cropW = cropH * aspectRatioShrinkTarget;
        } else {
            cropW = (float) reference.width();
            cropH = cropW / aspectRatioShrinkTarget;
        }
        Matrix translateMatrix = new Matrix();
        RectF shrunkRect = new RectF(shrinkTarget);
        translateMatrix.setScale(cropW / ((float) reference.width()), cropH / ((float) reference.height()), shrinkTarget.exactCenterX(), shrinkTarget.exactCenterY());
        translateMatrix.mapRect(shrunkRect);
        return ParamsUtils.createRect(shrunkRect);
    }

    public static List<Rect> getAvailableZoomCropRectangles(Parameters params, Rect activeArray) {
        Preconditions.checkNotNull(params, "params must not be null");
        Preconditions.checkNotNull(activeArray, "activeArray must not be null");
        return getAvailableCropRectangles(params, activeArray, ParamsUtils.createSize(activeArray));
    }

    public static List<Rect> getAvailablePreviewZoomCropRectangles(Parameters params, Rect activeArray, Size previewSize) {
        Preconditions.checkNotNull(params, "params must not be null");
        Preconditions.checkNotNull(activeArray, "activeArray must not be null");
        Preconditions.checkNotNull(previewSize, "previewSize must not be null");
        return getAvailableCropRectangles(params, activeArray, previewSize);
    }

    private static List<Rect> getAvailableCropRectangles(Parameters params, Rect activeArray, Size streamSize) {
        Preconditions.checkNotNull(params, "params must not be null");
        Preconditions.checkNotNull(activeArray, "activeArray must not be null");
        Preconditions.checkNotNull(streamSize, "streamSize must not be null");
        Rect unzoomedStreamCrop = getPreviewCropRectangleUnzoomed(activeArray, streamSize);
        if (params.isZoomSupported()) {
            List<Rect> zoomCropRectangles = new ArrayList(params.getMaxZoom() + 1);
            Matrix scaleMatrix = new Matrix();
            RectF scaledRect = new RectF();
            for (Integer intValue : params.getZoomRatios()) {
                float shrinkRatio = 100.0f / ((float) intValue.intValue());
                ParamsUtils.convertRectF(unzoomedStreamCrop, scaledRect);
                scaleMatrix.setScale(shrinkRatio, shrinkRatio, activeArray.exactCenterX(), activeArray.exactCenterY());
                scaleMatrix.mapRect(scaledRect);
                zoomCropRectangles.add(ParamsUtils.createRect(scaledRect));
            }
            return zoomCropRectangles;
        }
        return new ArrayList(Arrays.asList(new Rect[]{unzoomedStreamCrop}));
    }

    public static float getMaxZoomRatio(Parameters params) {
        if (!params.isZoomSupported()) {
            return 1.0f;
        }
        List<Integer> zoomRatios = params.getZoomRatios();
        return (((float) ((Integer) zoomRatios.get(zoomRatios.size() - 1)).intValue()) * 1.0f) / 100.0f;
    }

    private static SizeF getZoomRatio(Size activeArraySize, Size cropSize) {
        Preconditions.checkNotNull(activeArraySize, "activeArraySize must not be null");
        Preconditions.checkNotNull(cropSize, "cropSize must not be null");
        Preconditions.checkArgumentPositive(cropSize.getWidth(), "cropSize.width must be positive");
        Preconditions.checkArgumentPositive(cropSize.getHeight(), "cropSize.height must be positive");
        return new SizeF((((float) activeArraySize.getWidth()) * 1.0f) / ((float) cropSize.getWidth()), (((float) activeArraySize.getHeight()) * 1.0f) / ((float) cropSize.getHeight()));
    }

    public static ZoomData convertScalerCropRegion(Rect activeArraySize, Rect cropRegion, Size previewSize, Parameters params) {
        Rect activeArraySizeOnly = new Rect(0, 0, activeArraySize.width(), activeArraySize.height());
        Rect userCropRegion = cropRegion;
        if (cropRegion == null) {
            userCropRegion = activeArraySizeOnly;
        }
        Rect reportedCropRegion = new Rect();
        Rect previewCropRegion = new Rect();
        return new ZoomData(getClosestAvailableZoomCrop(params, activeArraySizeOnly, previewSize, userCropRegion, reportedCropRegion, previewCropRegion), previewCropRegion, reportedCropRegion);
    }

    public static MeteringData convertMeteringRectangleToLegacy(Rect activeArray, MeteringRectangle meteringRect, ZoomData zoomData) {
        Area meteringArea;
        Rect previewCrop = zoomData.previewCrop;
        float scaleW = 2000.0f / ((float) previewCrop.width());
        float scaleH = 2000.0f / ((float) previewCrop.height());
        Matrix transform = new Matrix();
        transform.setTranslate((float) (-previewCrop.left), (float) (-previewCrop.top));
        transform.postScale(scaleW, scaleH);
        transform.postTranslate(-1000.0f, -1000.0f);
        Rect normalizedRegionUnbounded = ParamsUtils.mapRect(transform, meteringRect.getRect());
        Rect normalizedIntersected = new Rect(normalizedRegionUnbounded);
        if (normalizedIntersected.intersect(NORMALIZED_RECTANGLE_DEFAULT)) {
            meteringArea = new Area(normalizedIntersected, meteringRect.getMeteringWeight());
        } else {
            Log.w(TAG, "convertMeteringRectangleToLegacy - metering rectangle too small, no metering will be done");
            normalizedIntersected.set(RECTANGLE_EMPTY);
            meteringArea = new Area(RECTANGLE_EMPTY, 0);
        }
        Rect previewMetering = meteringRect.getRect();
        if (!previewMetering.intersect(previewCrop)) {
            previewMetering.set(RECTANGLE_EMPTY);
        }
        return new MeteringData(meteringArea, previewMetering, convertCameraAreaToActiveArrayRectangle(activeArray, zoomData, new Area(normalizedRegionUnbounded, meteringRect.getMeteringWeight()), false).rect);
    }

    public static WeightedRectangle convertCameraAreaToActiveArrayRectangle(Rect activeArray, ZoomData zoomData, Area area) {
        return convertCameraAreaToActiveArrayRectangle(activeArray, zoomData, area, true);
    }

    public static Face convertFaceFromLegacy(Camera.Face face, Rect activeArray, ZoomData zoomData) {
        Preconditions.checkNotNull(face, "face must not be null");
        WeightedRectangle faceRect = convertCameraAreaToActiveArrayRectangle(activeArray, zoomData, new Area(face.rect, 1));
        Point leftEye = face.leftEye;
        Point rightEye = face.rightEye;
        Point mouth = face.mouth;
        if (leftEye == null || rightEye == null || mouth == null || leftEye.x == -2000 || leftEye.y == -2000 || rightEye.x == -2000 || rightEye.y == -2000 || mouth.x == -2000 || mouth.y == -2000) {
            return faceRect.toFace();
        }
        leftEye = convertCameraPointToActiveArrayPoint(activeArray, zoomData, leftEye, true);
        return faceRect.toFace(face.id, leftEye, convertCameraPointToActiveArrayPoint(activeArray, zoomData, leftEye, true), convertCameraPointToActiveArrayPoint(activeArray, zoomData, leftEye, true));
    }

    private static Point convertCameraPointToActiveArrayPoint(Rect activeArray, ZoomData zoomData, Point point, boolean usePreviewCrop) {
        WeightedRectangle adjustedRect = convertCameraAreaToActiveArrayRectangle(activeArray, zoomData, new Area(new Rect(point.x, point.y, point.x, point.y), 1), usePreviewCrop);
        return new Point(adjustedRect.rect.left, adjustedRect.rect.top);
    }

    private static WeightedRectangle convertCameraAreaToActiveArrayRectangle(Rect activeArray, ZoomData zoomData, Area area, boolean usePreviewCrop) {
        Rect previewCrop = zoomData.previewCrop;
        Rect reportedCrop = zoomData.reportedCrop;
        float scaleW = (((float) previewCrop.width()) * 1.0f) / 2000.0f;
        float scaleH = (((float) previewCrop.height()) * 1.0f) / 2000.0f;
        Matrix transform = new Matrix();
        transform.setTranslate(1000.0f, 1000.0f);
        transform.postScale(scaleW, scaleH);
        transform.postTranslate((float) previewCrop.left, (float) previewCrop.top);
        Rect cropToIntersectAgainst = usePreviewCrop ? previewCrop : reportedCrop;
        Rect reportedMetering = ParamsUtils.mapRect(transform, area.rect);
        if (!reportedMetering.intersect(cropToIntersectAgainst)) {
            reportedMetering.set(RECTANGLE_EMPTY);
        }
        if (area.weight < 0) {
            Log.w(TAG, "convertCameraAreaToMeteringRectangle - rectangle " + stringFromArea(area) + " has too small weight, clip to 0");
        }
        return new WeightedRectangle(reportedMetering, area.weight);
    }

    private ParameterUtils() {
        throw new AssertionError();
    }
}
