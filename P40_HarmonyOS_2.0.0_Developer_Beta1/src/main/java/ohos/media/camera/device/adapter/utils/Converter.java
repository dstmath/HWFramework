package ohos.media.camera.device.adapter.utils;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.camera2.params.MeteringRectangle;
import android.location.Location;
import android.util.Range;
import android.view.Surface;
import java.util.ArrayList;
import java.util.List;
import ohos.agp.components.surfaceview.adapter.SurfaceUtils;
import ohos.agp.utils.Rect;
import ohos.media.camera.params.Face;
import ohos.media.camera.params.FaceLandmark;
import ohos.media.image.common.Size;
import ohos.utils.Scope;

public class Converter {
    private static final float MAX_FACE_SCORE = 100.0f;

    private Converter() {
    }

    public static Face convertFaceData(android.hardware.camera2.params.Face face) {
        ArrayList arrayList = new ArrayList(3);
        Point leftEyePosition = face.getLeftEyePosition();
        if (leftEyePosition != null) {
            arrayList.add(new FaceLandmark(0, convertPoint(leftEyePosition)));
        }
        Point rightEyePosition = face.getRightEyePosition();
        if (rightEyePosition != null) {
            arrayList.add(new FaceLandmark(1, convertPoint(rightEyePosition)));
        }
        Point mouthPosition = face.getMouthPosition();
        if (mouthPosition != null) {
            arrayList.add(new FaceLandmark(2, convertPoint(mouthPosition)));
        }
        float f = -1.0f;
        if (face.getScore() != -1) {
            f = ((float) face.getScore()) / MAX_FACE_SCORE;
        }
        return new Face(face.getId(), convertRect(face.getBounds()), f, arrayList);
    }

    private static ohos.agp.utils.Point convertPoint(Point point) {
        return new ohos.agp.utils.Point((float) point.x, (float) point.y);
    }

    private static Rect convertRect(android.graphics.Rect rect) {
        return new Rect(rect.left, rect.top, rect.right, rect.bottom);
    }

    public static List<Size> convertSizes(android.util.Size[] sizeArr) {
        ArrayList arrayList = new ArrayList(sizeArr.length);
        for (android.util.Size size : sizeArr) {
            arrayList.add(convertSize(size));
        }
        return arrayList;
    }

    private static Size convertSize(android.util.Size size) {
        return new Size(size.getWidth(), size.getHeight());
    }

    public static List<Scope<Integer>> convertRanges(Range[] rangeArr) {
        ArrayList arrayList = new ArrayList(rangeArr.length);
        for (Range range : rangeArr) {
            arrayList.add(Scope.create(range.getLower(), range.getUpper()));
        }
        return arrayList;
    }

    public static MeteringRectangle convert2MeteringRectangles(Rect rect) {
        if (rect == null) {
            return new MeteringRectangle(0, 0, 0, 0, 0);
        }
        return new MeteringRectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top, 1000);
    }

    public static Location convert2ALocation(ohos.location.Location location) {
        if (location == null) {
            return null;
        }
        Location location2 = new Location("gps");
        location2.setAccuracy(location.getAccuracy());
        location2.setAltitude(location.getAltitude());
        location2.setLatitude(location.getLatitude());
        location2.setLongitude(location.getLongitude());
        location2.setSpeed(location.getSpeed());
        location2.setTime(location.getTimeStamp());
        return location2;
    }

    public static android.graphics.Rect convert2ARect(Rect rect) {
        return new android.graphics.Rect(rect.left, rect.top, rect.right, rect.bottom);
    }

    public static Rect convert2ZRect(android.graphics.Rect rect) {
        if (rect == null) {
            return null;
        }
        return new Rect(rect.left, rect.top, rect.right, rect.bottom);
    }

    public static Rect rectf2Rect(RectF rectF) {
        Rect rect = new Rect();
        if (rectF != null) {
            rect.left = Math.round(rectF.left);
            rect.top = Math.round(rectF.top);
            rect.right = Math.round(rectF.right);
            rect.bottom = Math.round(rectF.bottom);
        }
        return rect;
    }

    public static Rect rectf2Rect(Matrix matrix, Rect rect) {
        Rect rect2 = new Rect();
        if (matrix != null) {
            RectF rectF = new RectF();
            matrix.mapRect(rectF, new RectF(convert2ARect(rect)));
            rect2.left = Math.round(rectF.left);
            rect2.top = Math.round(rectF.top);
            rect2.right = Math.round(rectF.right);
            rect2.bottom = Math.round(rectF.bottom);
        }
        return rect2;
    }

    public static Surface convert2ASurface(ohos.agp.graphics.Surface surface) {
        return SurfaceUtils.getSurfaceImpl(surface);
    }
}
