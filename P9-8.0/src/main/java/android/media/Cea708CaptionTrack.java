package android.media;

import android.media.SubtitleTrack.Cue;
import android.media.SubtitleTrack.RenderingWidget;
import java.util.Vector;

/* compiled from: Cea708CaptionRenderer */
class Cea708CaptionTrack extends SubtitleTrack {
    private final Cea708CCParser mCCParser = new Cea708CCParser(this.mRenderingWidget);
    private final Cea708CCWidget mRenderingWidget;

    Cea708CaptionTrack(Cea708CCWidget renderingWidget, MediaFormat format) {
        super(format);
        this.mRenderingWidget = renderingWidget;
    }

    public void onData(byte[] data, boolean eos, long runID) {
        this.mCCParser.parse(data);
    }

    public RenderingWidget getRenderingWidget() {
        return this.mRenderingWidget;
    }

    public void updateView(Vector<Cue> vector) {
    }
}
