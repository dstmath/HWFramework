package ohos.ai.cv.text;

import java.util.List;
import ohos.ai.cv.common.CvPoint;
import ohos.ai.cv.common.CvRect;
import ohos.utils.fastjson.annotation.JSONField;

public class TextLine {
    private static final String TAG = TextLine.class.getSimpleName();
    @JSONField(name = TextParamKey.TEXT_CORNER_POINTS)
    private CvPoint[] cornerPoints = null;
    @JSONField(name = "elements")
    private List<TextElement> elements = null;
    @JSONField(name = TextParamKey.TEXT_LANGUAGE_TYPE)
    private int languageType = 0;
    @JSONField(name = "lineRect")
    private CvRect lineRect = null;
    @JSONField(name = "value")
    private String value = null;

    public int getLanguageType() {
        return this.languageType;
    }

    public void setLanguageType(int i) {
        this.languageType = i;
    }

    public CvRect getLineRect() {
        return this.lineRect;
    }

    public void setLineRect(CvRect cvRect) {
        this.lineRect = cvRect;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String str) {
        this.value = str;
    }

    public List<TextElement> getElements() {
        return this.elements;
    }

    public void setElements(List<TextElement> list) {
        this.elements = list;
    }

    public CvPoint[] getCornerPoints() {
        CvPoint[] cvPointArr = this.cornerPoints;
        return cvPointArr == null ? new CvPoint[0] : (CvPoint[]) cvPointArr.clone();
    }

    public void setCornerPoints(CvPoint[] cvPointArr) {
        this.cornerPoints = cvPointArr == null ? new CvPoint[0] : (CvPoint[]) cvPointArr.clone();
    }
}
