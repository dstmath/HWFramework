package android.media;

import android.app.slice.Slice;
import android.media.SubtitleTrack;
import android.net.wifi.WifiEnterpriseConfig;
import android.provider.Telephony;
import java.util.Arrays;

/* access modifiers changed from: package-private */
/* compiled from: WebVttRenderer */
public class TextTrackCue extends SubtitleTrack.Cue {
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
    String mId = "";
    Integer mLinePosition = null;
    TextTrackCueSpan[][] mLines = null;
    boolean mPauseOnExit = false;
    TextTrackRegion mRegion = null;
    String mRegionId = "";
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
            boolean res = this.mId.equals(cue.mId) && this.mPauseOnExit == cue.mPauseOnExit && this.mWritingDirection == cue.mWritingDirection && this.mRegionId.equals(cue.mRegionId) && this.mSnapToLines == cue.mSnapToLines && this.mAutoLinePosition == cue.mAutoLinePosition && (this.mAutoLinePosition || ((this.mLinePosition != null && this.mLinePosition.equals(cue.mLinePosition)) || (this.mLinePosition == null && cue.mLinePosition == null))) && this.mTextPosition == cue.mTextPosition && this.mSize == cue.mSize && this.mAlignment == cue.mAlignment && this.mLines.length == cue.mLines.length;
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
            String[] strArr = this.mStrings;
            for (String s : strArr) {
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
        TextTrackCueSpan[][] textTrackCueSpanArr;
        String str;
        String str2 = "null";
        if (this.mLines == null) {
            builder.append(str2);
        } else {
            builder.append("[");
            TextTrackCueSpan[][] textTrackCueSpanArr2 = this.mLines;
            int length = textTrackCueSpanArr2.length;
            boolean first = true;
            int i = 0;
            while (i < length) {
                TextTrackCueSpan[] spans = textTrackCueSpanArr2[i];
                if (!first) {
                    builder.append(", ");
                }
                if (spans == null) {
                    builder.append(str2);
                    str = str2;
                    textTrackCueSpanArr = textTrackCueSpanArr2;
                } else {
                    builder.append("\"");
                    int length2 = spans.length;
                    long lastTimestamp = -1;
                    boolean innerFirst = true;
                    int i2 = 0;
                    while (i2 < length2) {
                        TextTrackCueSpan span = spans[i2];
                        if (!innerFirst) {
                            builder.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                        }
                        if (span.mTimestampMs != lastTimestamp) {
                            builder.append("<");
                            builder.append(WebVttParser.timeToString(span.mTimestampMs));
                            builder.append(">");
                            lastTimestamp = span.mTimestampMs;
                        }
                        builder.append(span.mText);
                        innerFirst = false;
                        i2++;
                        str2 = str2;
                        textTrackCueSpanArr2 = textTrackCueSpanArr2;
                    }
                    str = str2;
                    textTrackCueSpanArr = textTrackCueSpanArr2;
                    builder.append("\"");
                }
                first = false;
                i++;
                str2 = str;
                textTrackCueSpanArr2 = textTrackCueSpanArr;
            }
            builder.append("]");
        }
        return builder;
    }

    public String toString() {
        String str;
        Object obj;
        StringBuilder res = new StringBuilder();
        res.append(WebVttParser.timeToString(this.mStartTimeMs));
        res.append(" --> ");
        res.append(WebVttParser.timeToString(this.mEndTimeMs));
        res.append(" {id:\"");
        res.append(this.mId);
        res.append("\", pauseOnExit:");
        res.append(this.mPauseOnExit);
        res.append(", direction:");
        int i = this.mWritingDirection;
        String str2 = "INVALID";
        if (i == 100) {
            str = Slice.HINT_HORIZONTAL;
        } else if (i == 102) {
            str = "vertical_lr";
        } else if (i == 101) {
            str = "vertical_rl";
        } else {
            str = str2;
        }
        res.append(str);
        res.append(", regionId:\"");
        res.append(this.mRegionId);
        res.append("\", snapToLines:");
        res.append(this.mSnapToLines);
        res.append(", linePosition:");
        if (this.mAutoLinePosition) {
            obj = "auto";
        } else {
            obj = this.mLinePosition;
        }
        res.append(obj);
        res.append(", textPosition:");
        res.append(this.mTextPosition);
        res.append(", size:");
        res.append(this.mSize);
        res.append(", alignment:");
        int i2 = this.mAlignment;
        if (i2 == 202) {
            str2 = "end";
        } else if (i2 == 203) {
            str2 = "left";
        } else if (i2 == 200) {
            str2 = "middle";
        } else if (i2 == 204) {
            str2 = "right";
        } else if (i2 == 201) {
            str2 = Telephony.BaseMmsColumns.START;
        }
        res.append(str2);
        res.append(", text:");
        appendStringsToBuilder(res).append("}");
        return res.toString();
    }

    public int hashCode() {
        return toString().hashCode();
    }

    @Override // android.media.SubtitleTrack.Cue
    public void onTime(long timeMs) {
        TextTrackCueSpan[][] textTrackCueSpanArr = this.mLines;
        for (TextTrackCueSpan[] line : textTrackCueSpanArr) {
            for (TextTrackCueSpan span : line) {
                span.mEnabled = timeMs >= span.mTimestampMs;
            }
        }
    }
}
