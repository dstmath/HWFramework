package android.media;

import android.media.SubtitleTrack.Cue;
import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import java.util.Arrays;

/* compiled from: WebVttRenderer */
class TextTrackCue extends Cue {
    static final int ALIGNMENT_END = 202;
    static final int ALIGNMENT_LEFT = 203;
    static final int ALIGNMENT_MIDDLE = 200;
    static final int ALIGNMENT_RIGHT = 204;
    static final int ALIGNMENT_START = 201;
    private static final String TAG = "TTCue";
    static final int WRITING_DIRECTION_HORIZONTAL = 100;
    static final int WRITING_DIRECTION_VERTICAL_LR = 102;
    static final int WRITING_DIRECTION_VERTICAL_RL = 101;
    int mAlignment = 200;
    boolean mAutoLinePosition;
    String mId = ProxyInfo.LOCAL_EXCL_LIST;
    Integer mLinePosition = null;
    TextTrackCueSpan[][] mLines = null;
    boolean mPauseOnExit = false;
    TextTrackRegion mRegion = null;
    String mRegionId = ProxyInfo.LOCAL_EXCL_LIST;
    int mSize = 100;
    boolean mSnapToLines = true;
    String[] mStrings;
    int mTextPosition = 50;
    int mWritingDirection = 100;

    TextTrackCue() {
    }

    public boolean equals(Object o) {
        if (!(o instanceof TextTrackCue)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        try {
            TextTrackCue cue = (TextTrackCue) o;
            boolean res = (this.mId.equals(cue.mId) && this.mPauseOnExit == cue.mPauseOnExit && this.mWritingDirection == cue.mWritingDirection && this.mRegionId.equals(cue.mRegionId) && this.mSnapToLines == cue.mSnapToLines && this.mAutoLinePosition == cue.mAutoLinePosition && ((this.mAutoLinePosition || ((this.mLinePosition != null && this.mLinePosition.equals(cue.mLinePosition)) || (this.mLinePosition == null && cue.mLinePosition == null))) && this.mTextPosition == cue.mTextPosition && this.mSize == cue.mSize && this.mAlignment == cue.mAlignment)) ? this.mLines.length == cue.mLines.length : false;
            if (res) {
                for (int line = 0; line < this.mLines.length; line++) {
                    if (!Arrays.equals(this.mLines[line], cue.mLines[line])) {
                        return false;
                    }
                }
            }
            return res;
        } catch (IncompatibleClassChangeError e) {
            return false;
        }
    }

    public StringBuilder appendStringsToBuilder(StringBuilder builder) {
        if (this.mStrings == null) {
            builder.append("null");
        } else {
            builder.append("[");
            boolean first = true;
            for (String s : this.mStrings) {
                if (!first) {
                    builder.append(", ");
                }
                if (s == null) {
                    builder.append("null");
                } else {
                    builder.append("\"");
                    builder.append(s);
                    builder.append("\"");
                }
                first = false;
            }
            builder.append("]");
        }
        return builder;
    }

    public StringBuilder appendLinesToBuilder(StringBuilder builder) {
        if (this.mLines == null) {
            builder.append("null");
        } else {
            builder.append("[");
            boolean first = true;
            for (TextTrackCueSpan[] spans : this.mLines) {
                if (!first) {
                    builder.append(", ");
                }
                if (spans == null) {
                    builder.append("null");
                } else {
                    builder.append("\"");
                    boolean innerFirst = true;
                    long lastTimestamp = -1;
                    for (TextTrackCueSpan span : spans) {
                        if (!innerFirst) {
                            builder.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                        }
                        if (span.mTimestampMs != lastTimestamp) {
                            builder.append("<").append(WebVttParser.timeToString(span.mTimestampMs)).append(">");
                            lastTimestamp = span.mTimestampMs;
                        }
                        builder.append(span.mText);
                        innerFirst = false;
                    }
                    builder.append("\"");
                }
                first = false;
            }
            builder.append("]");
        }
        return builder;
    }

    public String toString() {
        String str;
        Object obj;
        StringBuilder res = new StringBuilder();
        StringBuilder append = res.append(WebVttParser.timeToString(this.mStartTimeMs)).append(" --> ").append(WebVttParser.timeToString(this.mEndTimeMs)).append(" {id:\"").append(this.mId).append("\", pauseOnExit:").append(this.mPauseOnExit).append(", direction:");
        if (this.mWritingDirection == 100) {
            str = "horizontal";
        } else if (this.mWritingDirection == 102) {
            str = "vertical_lr";
        } else if (this.mWritingDirection == 101) {
            str = "vertical_rl";
        } else {
            str = "INVALID";
        }
        append = append.append(str).append(", regionId:\"").append(this.mRegionId).append("\", snapToLines:").append(this.mSnapToLines).append(", linePosition:");
        if (this.mAutoLinePosition) {
            obj = "auto";
        } else {
            obj = this.mLinePosition;
        }
        append = append.append(obj).append(", textPosition:").append(this.mTextPosition).append(", size:").append(this.mSize).append(", alignment:");
        str = this.mAlignment == 202 ? TtmlUtils.ATTR_END : this.mAlignment == 203 ? "left" : this.mAlignment == 200 ? "middle" : this.mAlignment == 204 ? "right" : this.mAlignment == 201 ? "start" : "INVALID";
        append.append(str).append(", text:");
        appendStringsToBuilder(res).append("}");
        return res.toString();
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public void onTime(long timeMs) {
        for (TextTrackCueSpan[] line : this.mLines) {
            for (TextTrackCueSpan span : r6[r5]) {
                boolean z;
                if (timeMs >= span.mTimestampMs) {
                    z = true;
                } else {
                    z = false;
                }
                span.mEnabled = z;
            }
        }
    }
}
