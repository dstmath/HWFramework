package ohos.ai.cv.text;

import java.util.List;
import ohos.ai.cv.common.CvPoint;
import ohos.ai.cv.common.CvRect;
import ohos.utils.fastjson.annotation.JSONField;

public class Text {
    @JSONField(name = TextParamKey.TEXT_BLOCKS)
    private List<TextBlock> blocks = null;
    @JSONField(name = TextParamKey.TEXT_CORNER_POINTS)
    private CvPoint[] cornerPoints = null;
    @JSONField(name = TextParamKey.TEXT_PAGE_LANGUAGE)
    private int pageLanguage = 0;
    @JSONField(name = "textRect")
    private CvRect textRect = null;
    @JSONField(name = "value")
    private String value = null;

    public void setPageLanguage(int i) {
        this.pageLanguage = i;
    }

    public int getPageLanguage() {
        return this.pageLanguage;
    }

    public CvRect getTextRect() {
        return this.textRect;
    }

    public void setTextRect(CvRect cvRect) {
        this.textRect = cvRect;
    }

    public void setValue(String str) {
        this.value = str;
    }

    public String getValue() {
        return this.value;
    }

    public List<TextBlock> getBlocks() {
        return this.blocks;
    }

    public void setBlocks(List<TextBlock> list) {
        this.blocks = list;
    }

    public CvPoint[] getCornerPoints() {
        CvPoint[] cvPointArr = this.cornerPoints;
        return cvPointArr == null ? new CvPoint[0] : (CvPoint[]) cvPointArr.clone();
    }

    public void setCornerPoints(CvPoint[] cvPointArr) {
        this.cornerPoints = cvPointArr == null ? null : (CvPoint[]) cvPointArr.clone();
    }

    public void setText(Text text) {
        if (text != null) {
            this.value = text.getValue();
            this.blocks = text.getBlocks();
            this.textRect = text.getTextRect();
            this.cornerPoints = text.getCornerPoints();
            this.pageLanguage = text.getPageLanguage();
        }
    }
}
