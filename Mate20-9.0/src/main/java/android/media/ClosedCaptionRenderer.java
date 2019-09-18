package android.media;

import android.content.Context;
import android.media.SubtitleController;

public class ClosedCaptionRenderer extends SubtitleController.Renderer {
    private Cea608CCWidget mCCWidget;
    private final Context mContext;

    public ClosedCaptionRenderer(Context context) {
        this.mContext = context;
    }

    public boolean supports(MediaFormat format) {
        if (format.containsKey(MediaFormat.KEY_MIME)) {
            return "text/cea-608".equals(format.getString(MediaFormat.KEY_MIME));
        }
        return false;
    }

    public SubtitleTrack createTrack(MediaFormat format) {
        if ("text/cea-608".equals(format.getString(MediaFormat.KEY_MIME))) {
            if (this.mCCWidget == null) {
                this.mCCWidget = new Cea608CCWidget(this.mContext);
            }
            return new Cea608CaptionTrack(this.mCCWidget, format);
        }
        throw new RuntimeException("No matching format: " + format.toString());
    }
}
