package android.media;

import android.content.Context;
import android.media.SubtitleController;

public class Cea708CaptionRenderer extends SubtitleController.Renderer {
    private Cea708CCWidget mCCWidget;
    private final Context mContext;

    public Cea708CaptionRenderer(Context context) {
        this.mContext = context;
    }

    public boolean supports(MediaFormat format) {
        if (format.containsKey(MediaFormat.KEY_MIME)) {
            return "text/cea-708".equals(format.getString(MediaFormat.KEY_MIME));
        }
        return false;
    }

    public SubtitleTrack createTrack(MediaFormat format) {
        if ("text/cea-708".equals(format.getString(MediaFormat.KEY_MIME))) {
            if (this.mCCWidget == null) {
                this.mCCWidget = new Cea708CCWidget(this.mContext);
            }
            return new Cea708CaptionTrack(this.mCCWidget, format);
        }
        throw new RuntimeException("No matching format: " + format.toString());
    }
}
