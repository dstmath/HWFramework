package android.media;

import java.util.Vector;

/* compiled from: WebVttRenderer */
class UnstyledTextExtractor implements OnTokenListener {
    Vector<TextTrackCueSpan> mCurrentLine = new Vector();
    long mLastTimestamp;
    StringBuilder mLine = new StringBuilder();
    Vector<TextTrackCueSpan[]> mLines = new Vector();

    UnstyledTextExtractor() {
        init();
    }

    private void init() {
        this.mLine.delete(0, this.mLine.length());
        this.mLines.clear();
        this.mCurrentLine.clear();
        this.mLastTimestamp = -1;
    }

    public void onData(String s) {
        this.mLine.append(s);
    }

    public void onStart(String tag, String[] classes, String annotation) {
    }

    public void onEnd(String tag) {
    }

    public void onTimeStamp(long timestampMs) {
        if (this.mLine.length() > 0 && timestampMs != this.mLastTimestamp) {
            this.mCurrentLine.add(new TextTrackCueSpan(this.mLine.toString(), this.mLastTimestamp));
            this.mLine.delete(0, this.mLine.length());
        }
        this.mLastTimestamp = timestampMs;
    }

    public void onLineEnd() {
        if (this.mLine.length() > 0) {
            this.mCurrentLine.add(new TextTrackCueSpan(this.mLine.toString(), this.mLastTimestamp));
            this.mLine.delete(0, this.mLine.length());
        }
        TextTrackCueSpan[] spans = new TextTrackCueSpan[this.mCurrentLine.size()];
        this.mCurrentLine.toArray(spans);
        this.mCurrentLine.clear();
        this.mLines.add(spans);
    }

    public TextTrackCueSpan[][] getText() {
        if (this.mLine.length() > 0 || this.mCurrentLine.size() > 0) {
            onLineEnd();
        }
        TextTrackCueSpan[][] lines = new TextTrackCueSpan[this.mLines.size()][];
        this.mLines.toArray(lines);
        init();
        return lines;
    }
}
