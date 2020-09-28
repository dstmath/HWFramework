package android.media;

/* access modifiers changed from: package-private */
/* compiled from: WebVttRenderer */
public class TextTrackCueSpan {
    boolean mEnabled;
    String mText;
    long mTimestampMs;

    TextTrackCueSpan(String text, long timestamp) {
        this.mTimestampMs = timestamp;
        this.mText = text;
        this.mEnabled = this.mTimestampMs < 0;
    }

    public boolean equals(Object o) {
        if (!(o instanceof TextTrackCueSpan)) {
            return false;
        }
        TextTrackCueSpan span = (TextTrackCueSpan) o;
        if (this.mTimestampMs != span.mTimestampMs || !this.mText.equals(span.mText)) {
            return false;
        }
        return true;
    }
}
