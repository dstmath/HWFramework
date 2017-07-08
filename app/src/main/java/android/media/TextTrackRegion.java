package android.media;

import android.hardware.SensorManager;
import android.net.NetworkPolicyManager;
import android.net.ProxyInfo;

/* compiled from: WebVttRenderer */
class TextTrackRegion {
    static final int SCROLL_VALUE_NONE = 300;
    static final int SCROLL_VALUE_SCROLL_UP = 301;
    float mAnchorPointX;
    float mAnchorPointY;
    String mId;
    int mLines;
    int mScrollValue;
    float mViewportAnchorPointX;
    float mViewportAnchorPointY;
    float mWidth;

    TextTrackRegion() {
        this.mId = ProxyInfo.LOCAL_EXCL_LIST;
        this.mWidth = SensorManager.LIGHT_CLOUDY;
        this.mLines = 3;
        this.mViewportAnchorPointX = 0.0f;
        this.mAnchorPointX = 0.0f;
        this.mViewportAnchorPointY = SensorManager.LIGHT_CLOUDY;
        this.mAnchorPointY = SensorManager.LIGHT_CLOUDY;
        this.mScrollValue = SCROLL_VALUE_NONE;
    }

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder(" {id:\"").append(this.mId).append("\", width:").append(this.mWidth).append(", lines:").append(this.mLines).append(", anchorPoint:(").append(this.mAnchorPointX).append(", ").append(this.mAnchorPointY).append("), viewportAnchorPoints:").append(this.mViewportAnchorPointX).append(", ").append(this.mViewportAnchorPointY).append("), scrollValue:");
        if (this.mScrollValue == SCROLL_VALUE_NONE) {
            str = NetworkPolicyManager.FIREWALL_CHAIN_NAME_NONE;
        } else if (this.mScrollValue == SCROLL_VALUE_SCROLL_UP) {
            str = "scroll_up";
        } else {
            str = "INVALID";
        }
        return append.append(str).append("}").toString();
    }
}
