package ohos.ai.cv.text;

import ohos.ai.cv.common.CvPoint;
import ohos.ai.cv.common.CvRect;
import ohos.utils.fastjson.annotation.JSONField;

public class TextElement {
    @JSONField(name = TextParamKey.TEXT_CORNER_POINTS)
    private CvPoint[] cornerPoints = null;
    @JSONField(name = "elementRect")
    private CvRect elementRect = null;
    @JSONField(name = "value")
    private String value = null;

    public String getValue() {
        return this.value;
    }

    public void setValue(String str) {
        this.value = str;
    }

    public CvRect getElementRect() {
        return this.elementRect;
    }

    public void setElementRect(CvRect cvRect) {
        this.elementRect = cvRect;
    }

    public CvPoint[] getCornerPoints() {
        CvPoint[] cvPointArr = this.cornerPoints;
        return cvPointArr == null ? new CvPoint[0] : (CvPoint[]) cvPointArr.clone();
    }

    public void setCornerPoints(CvPoint[] cvPointArr) {
        this.cornerPoints = cvPointArr == null ? new CvPoint[0] : (CvPoint[]) cvPointArr.clone();
    }
}
