package android.media;

import android.media.SubtitleTrack.Cue;

/* compiled from: TtmlRenderer */
class TtmlCue extends Cue {
    public String mText;
    public String mTtmlFragment;

    public TtmlCue(long startTimeMs, long endTimeMs, String text, String ttmlFragment) {
        this.mStartTimeMs = startTimeMs;
        this.mEndTimeMs = endTimeMs;
        this.mText = text;
        this.mTtmlFragment = ttmlFragment;
    }
}
