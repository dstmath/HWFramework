package ohos.ai.cv.docrefine;

import java.util.ArrayList;
import java.util.List;
import ohos.ai.cv.common.CvPoint;
import ohos.ai.engine.utils.HiAILog;
import ohos.utils.fastjson.annotation.JSONField;

public class DocCoordinates {
    private static final String TAG = DocCoordinates.class.getSimpleName();
    @JSONField(name = "bottom_left", ordinal = 1)
    private CvPoint bottomLeft;
    @JSONField(name = "bottom_right", ordinal = 2)
    private CvPoint bottomRight;
    @JSONField(name = "top_left", ordinal = 3)
    private CvPoint topLeft;
    @JSONField(name = "top_right", ordinal = 4)
    private CvPoint topRight;

    private enum PointsOfDocRefine {
        TOP_LEFT_X(0),
        TOP_LEFT_Y(1),
        TOP_RIGHT_X(2),
        TOP_RIGHT_Y(3),
        BOTTOM_LEFT_X(4),
        BOTTOM_LEFT_Y(5),
        BOTTOM_RIGHT_X(6),
        BOTTOM_RIGHT_Y(7);
        
        private final int cornerIndex;

        private PointsOfDocRefine(int i) {
            this.cornerIndex = i;
        }

        public int getCornerIndex() {
            return this.cornerIndex;
        }
    }

    public DocCoordinates(CvPoint cvPoint, CvPoint cvPoint2, CvPoint cvPoint3, CvPoint cvPoint4) {
        this.topLeft = cvPoint;
        this.topRight = cvPoint2;
        this.bottomLeft = cvPoint3;
        this.bottomRight = cvPoint4;
    }

    public DocCoordinates() {
        this(new CvPoint(0, 0), new CvPoint(0, 0), new CvPoint(0, 0), new CvPoint(0, 0));
    }

    public CvPoint getBottomLeft() {
        return this.bottomLeft;
    }

    public CvPoint getBottomRight() {
        return this.bottomRight;
    }

    public CvPoint getTopLeft() {
        return this.topLeft;
    }

    public CvPoint getTopRight() {
        return this.topRight;
    }

    public void setBottomLeft(CvPoint cvPoint) {
        this.bottomLeft = cvPoint;
    }

    public void setBottomRight(CvPoint cvPoint) {
        this.bottomRight = cvPoint;
    }

    public void setTopLeft(CvPoint cvPoint) {
        this.topLeft = cvPoint;
    }

    public void setTopRight(CvPoint cvPoint) {
        this.topRight = cvPoint;
    }

    public void scaleDocCoordinates(float f) {
        scaleDocCoordinates(f, f);
    }

    public void scaleDocCoordinates(float f, float f2) {
        CvPoint cvPoint = this.topLeft;
        cvPoint.x = (int) (((float) cvPoint.x) * f);
        CvPoint cvPoint2 = this.topLeft;
        cvPoint2.y = (int) (((float) cvPoint2.y) * f2);
        CvPoint cvPoint3 = this.topRight;
        cvPoint3.x = (int) (((float) cvPoint3.x) * f);
        CvPoint cvPoint4 = this.topRight;
        cvPoint4.y = (int) (((float) cvPoint4.y) * f2);
        CvPoint cvPoint5 = this.bottomLeft;
        cvPoint5.x = (int) (((float) cvPoint5.x) * f);
        CvPoint cvPoint6 = this.bottomLeft;
        cvPoint6.y = (int) (((float) cvPoint6.y) * f2);
        CvPoint cvPoint7 = this.bottomRight;
        cvPoint7.x = (int) (((float) cvPoint7.x) * f);
        CvPoint cvPoint8 = this.bottomRight;
        cvPoint8.y = (int) (((float) cvPoint8.y) * f2);
    }

    public void setDocCoordinates(DocCoordinates docCoordinates) {
        if (docCoordinates == null) {
            HiAILog.error(TAG, "input docCoordinates is null");
            return;
        }
        this.topLeft = docCoordinates.getTopLeft();
        this.topRight = docCoordinates.getTopRight();
        this.bottomLeft = docCoordinates.getBottomLeft();
        this.bottomRight = docCoordinates.getBottomRight();
    }

    public static DocCoordinates toCoordinates(List<Integer> list) {
        return new DocCoordinates(new CvPoint(list.get(PointsOfDocRefine.TOP_LEFT_X.getCornerIndex()).intValue(), list.get(PointsOfDocRefine.TOP_LEFT_Y.getCornerIndex()).intValue()), new CvPoint(list.get(PointsOfDocRefine.TOP_RIGHT_X.getCornerIndex()).intValue(), list.get(PointsOfDocRefine.TOP_RIGHT_Y.getCornerIndex()).intValue()), new CvPoint(list.get(PointsOfDocRefine.BOTTOM_LEFT_X.getCornerIndex()).intValue(), list.get(PointsOfDocRefine.BOTTOM_LEFT_Y.getCornerIndex()).intValue()), new CvPoint(list.get(PointsOfDocRefine.BOTTOM_RIGHT_X.getCornerIndex()).intValue(), list.get(PointsOfDocRefine.BOTTOM_RIGHT_Y.getCornerIndex()).intValue()));
    }

    public static ArrayList<Integer> toArrayList(DocCoordinates docCoordinates) {
        if (docCoordinates == null) {
            HiAILog.error(TAG, "input docCoordinates is null");
            return new ArrayList<>(0);
        }
        ArrayList<Integer> arrayList = new ArrayList<>(8);
        arrayList.add(Integer.valueOf(docCoordinates.getTopLeft().x));
        arrayList.add(Integer.valueOf(docCoordinates.getTopLeft().y));
        arrayList.add(Integer.valueOf(docCoordinates.getTopRight().x));
        arrayList.add(Integer.valueOf(docCoordinates.getTopRight().y));
        arrayList.add(Integer.valueOf(docCoordinates.getBottomLeft().x));
        arrayList.add(Integer.valueOf(docCoordinates.getBottomLeft().y));
        arrayList.add(Integer.valueOf(docCoordinates.getBottomRight().x));
        arrayList.add(Integer.valueOf(docCoordinates.getBottomRight().y));
        return arrayList;
    }
}
