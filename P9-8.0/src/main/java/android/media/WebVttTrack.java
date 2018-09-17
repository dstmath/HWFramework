package android.media;

import android.media.SubtitleTrack.Cue;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/* compiled from: WebVttRenderer */
class WebVttTrack extends SubtitleTrack implements WebVttCueListener {
    private static final String TAG = "WebVttTrack";
    private Long mCurrentRunID;
    private final UnstyledTextExtractor mExtractor = new UnstyledTextExtractor();
    private final WebVttParser mParser = new WebVttParser(this);
    private final Map<String, TextTrackRegion> mRegions = new HashMap();
    private final WebVttRenderingWidget mRenderingWidget;
    private final Vector<Long> mTimestamps = new Vector();
    private final Tokenizer mTokenizer = new Tokenizer(this.mExtractor);

    WebVttTrack(WebVttRenderingWidget renderingWidget, MediaFormat format) {
        super(format);
        this.mRenderingWidget = renderingWidget;
    }

    public WebVttRenderingWidget getRenderingWidget() {
        return this.mRenderingWidget;
    }

    public void onData(byte[] data, boolean eos, long runID) {
        try {
            String str = new String(data, "UTF-8");
            synchronized (this.mParser) {
                if (this.mCurrentRunID == null || runID == this.mCurrentRunID.longValue()) {
                    this.mCurrentRunID = Long.valueOf(runID);
                    this.mParser.parse(str);
                    if (eos) {
                        finishedRun(runID);
                        this.mParser.eos();
                        this.mRegions.clear();
                        this.mCurrentRunID = null;
                    }
                } else {
                    throw new IllegalStateException("Run #" + this.mCurrentRunID + " in progress.  Cannot process run #" + runID);
                }
            }
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "subtitle data is not UTF-8 encoded: " + e);
        }
    }

    public void onCueParsed(TextTrackCue cue) {
        synchronized (this.mParser) {
            if (cue.mRegionId.length() != 0) {
                cue.mRegion = (TextTrackRegion) this.mRegions.get(cue.mRegionId);
            }
            if (this.DEBUG) {
                Log.v(TAG, "adding cue " + cue);
            }
            this.mTokenizer.reset();
            for (String s : cue.mStrings) {
                this.mTokenizer.tokenize(s);
            }
            cue.mLines = this.mExtractor.getText();
            if (this.DEBUG) {
                Log.v(TAG, cue.appendLinesToBuilder(cue.appendStringsToBuilder(new StringBuilder()).append(" simplified to: ")).toString());
            }
            for (TextTrackCueSpan[] line : cue.mLines) {
                for (TextTrackCueSpan span : r7[r5]) {
                    if (span.mTimestampMs > cue.mStartTimeMs && span.mTimestampMs < cue.mEndTimeMs && (this.mTimestamps.contains(Long.valueOf(span.mTimestampMs)) ^ 1) != 0) {
                        this.mTimestamps.add(Long.valueOf(span.mTimestampMs));
                    }
                }
            }
            if (this.mTimestamps.size() > 0) {
                cue.mInnerTimesMs = new long[this.mTimestamps.size()];
                for (int ix = 0; ix < this.mTimestamps.size(); ix++) {
                    cue.mInnerTimesMs[ix] = ((Long) this.mTimestamps.get(ix)).longValue();
                }
                this.mTimestamps.clear();
            } else {
                cue.mInnerTimesMs = null;
            }
            cue.mRunID = this.mCurrentRunID.longValue();
        }
        addCue(cue);
    }

    public void onRegionParsed(TextTrackRegion region) {
        synchronized (this.mParser) {
            this.mRegions.put(region.mId, region);
        }
    }

    public void updateView(Vector<Cue> activeCues) {
        if (this.mVisible) {
            if (this.DEBUG && this.mTimeProvider != null) {
                try {
                    Log.d(TAG, "at " + (this.mTimeProvider.getCurrentTimeUs(false, true) / 1000) + " ms the active cues are:");
                } catch (IllegalStateException e) {
                    Log.d(TAG, "at (illegal state) the active cues are:");
                }
            }
            if (this.mRenderingWidget != null) {
                this.mRenderingWidget.setActiveCues(activeCues);
            }
        }
    }
}
