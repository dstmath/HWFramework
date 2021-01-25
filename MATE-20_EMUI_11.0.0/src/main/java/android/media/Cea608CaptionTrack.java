package android.media;

import android.media.SubtitleTrack;
import java.util.Vector;

/* compiled from: ClosedCaptionRenderer */
class Cea608CaptionTrack extends SubtitleTrack {
    private final Cea608CCParser mCCParser = new Cea608CCParser(this.mRenderingWidget);
    private final Cea608CCWidget mRenderingWidget;

    Cea608CaptionTrack(Cea608CCWidget renderingWidget, MediaFormat format) {
        super(format);
        this.mRenderingWidget = renderingWidget;
    }

    @Override // android.media.SubtitleTrack
    public void onData(byte[] data, boolean eos, long runID) {
        this.mCCParser.parse(data);
    }

    @Override // android.media.SubtitleTrack
    public SubtitleTrack.RenderingWidget getRenderingWidget() {
        return this.mRenderingWidget;
    }

    @Override // android.media.SubtitleTrack
    public void updateView(Vector<SubtitleTrack.Cue> vector) {
    }
}
