package android.media;

import android.media.Tokenizer;
import java.util.Vector;

/* access modifiers changed from: package-private */
/* compiled from: WebVttRenderer */
public class UnstyledTextExtractor implements Tokenizer.OnTokenListener {
    Vector<TextTrackCueSpan> mCurrentLine = new Vector<>();
    long mLastTimestamp;
    StringBuilder mLine = new StringBuilder();
    Vector<TextTrackCueSpan[]> mLines = new Vector<>();

    UnstyledTextExtractor() {
        init();
    }

    private void init() {
        StringBuilder sb = this.mLine;
        sb.delete(0, sb.length());
        this.mLines.clear();
        this.mCurrentLine.clear();
        this.mLastTimestamp = -1;
    }

    @Override // android.media.Tokenizer.OnTokenListener
    public void onData(String s) {
        this.mLine.append(s);
    }

    @Override // android.media.Tokenizer.OnTokenListener
    public void onStart(String tag, String[] classes, String annotation) {
    }

    @Override // android.media.Tokenizer.OnTokenListener
    public void onEnd(String tag) {
    }

    @Override // android.media.Tokenizer.OnTokenListener
    public void onTimeStamp(long timestampMs) {
        if (this.mLine.length() > 0 && timestampMs != this.mLastTimestamp) {
            this.mCurrentLine.add(new TextTrackCueSpan(this.mLine.toString(), this.mLastTimestamp));
            StringBuilder sb = this.mLine;
            sb.delete(0, sb.length());
        }
        this.mLastTimestamp = timestampMs;
    }

    @Override // android.media.Tokenizer.OnTokenListener
    public void onLineEnd() {
        if (this.mLine.length() > 0) {
            this.mCurrentLine.add(new TextTrackCueSpan(this.mLine.toString(), this.mLastTimestamp));
            StringBuilder sb = this.mLine;
            sb.delete(0, sb.length());
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
