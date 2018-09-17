package android.media;

import android.media.SubtitleTrack.Cue;
import android.media.SubtitleTrack.RenderingWidget;
import java.util.Vector;

/* compiled from: ClosedCaptionRenderer */
class Cea608CaptionTrack extends SubtitleTrack {
    private final Cea608CCParser mCCParser = new Cea608CCParser(this.mRenderingWidget);
    private final Cea608CCWidget mRenderingWidget;

    Cea608CaptionTrack(Cea608CCWidget renderingWidget, MediaFormat format) {
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
