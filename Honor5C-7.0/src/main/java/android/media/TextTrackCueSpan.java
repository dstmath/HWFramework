package android.media;

/* compiled from: WebVttRenderer */
class TextTrackCueSpan {
    boolean mEnabled;
    String mText;
    long mTimestampMs;

    TextTrackCueSpan(String text, long timestamp) {
        this.mTimestampMs = timestamp;
        this.mText = text;
        this.mEnabled = this.mTimestampMs < 0;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof TextTrackCueSpan)) {
            return false;
        }
        TextTrackCueSpan span = (TextTrackCueSpan) o;
        if (this.mTimestampMs == span.mTimestampMs) {
            z = this.mText.equals(span.mText);
        }
        return z;
    }
}
