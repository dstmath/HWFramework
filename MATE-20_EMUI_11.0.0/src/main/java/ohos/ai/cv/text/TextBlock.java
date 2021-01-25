package ohos.ai.cv.text;

import java.util.List;
import ohos.ai.cv.common.BoundingBox;
import ohos.ai.cv.common.CvPoint;
import ohos.utils.fastjson.annotation.JSONField;

public class TextBlock {
    @JSONField(name = "boundingBox")
    private BoundingBox boundingBox = null;
    @JSONField(name = TextParamKey.TEXT_CORNER_POINTS)
    private CvPoint[] cornerPoints = null;
    @JSONField(name = "textLines")
    private List<TextLine> textLines = null;
    @JSONField(name = "value")
    private String value = null;

    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox2) {
        this.boundingBox = boundingBox2;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String str) {
        this.value = str;
    }

    public List<TextLine> getTextLines() {
        return this.textLines;
    }

    public void setTextLines(List<TextLine> list) {
        this.textLines = list;
    }

    public CvPoint[] getCornerPoints() {
        CvPoint[] cvPointArr = this.cornerPoints;
        return cvPointArr == null ? new CvPoint[0] : (CvPoint[]) cvPointArr.clone();
    }

    public void setCornerPoints(CvPoint[] cvPointArr) {
        this.cornerPoints = cvPointArr == null ? new CvPoint[0] : (CvPoint[]) cvPointArr.clone();
    }
}
