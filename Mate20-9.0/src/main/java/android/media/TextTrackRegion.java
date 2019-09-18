package android.media;

/* compiled from: WebVttRenderer */
class TextTrackRegion {
    static final int SCROLL_VALUE_NONE = 300;
    static final int SCROLL_VALUE_SCROLL_UP = 301;
    float mAnchorPointX = 0.0f;
    float mAnchorPointY = 100.0f;
    String mId = "";
    int mLines = 3;
    int mScrollValue = 300;
    float mViewportAnchorPointX = 0.0f;
    float mViewportAnchorPointY = 100.0f;
    float mWidth = 100.0f;

    TextTrackRegion() {
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder(" {id:\"");
        sb.append(this.mId);
        sb.append("\", width:");
        sb.append(this.mWidth);
        sb.append(", lines:");
        sb.append(this.mLines);
        sb.append(", anchorPoint:(");
        sb.append(this.mAnchorPointX);
        sb.append(", ");
        sb.append(this.mAnchorPointY);
        sb.append("), viewportAnchorPoints:");
        sb.append(this.mViewportAnchorPointX);
        sb.append(", ");
        sb.append(this.mViewportAnchorPointY);
        sb.append("), scrollValue:");
        if (this.mScrollValue == 300) {
            str = "none";
        } else if (this.mScrollValue == 301) {
            str = "scroll_up";
        } else {
            str = "INVALID";
        }
        sb.append(str);
        return sb.append("}").toString();
    }
}
