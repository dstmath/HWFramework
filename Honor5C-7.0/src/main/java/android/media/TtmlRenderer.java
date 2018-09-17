package android.media;

import android.content.Context;
import android.media.SubtitleController.Renderer;

public class TtmlRenderer extends Renderer {
    private static final String MEDIA_MIMETYPE_TEXT_TTML = "application/ttml+xml";
    private final Context mContext;
    private TtmlRenderingWidget mRenderingWidget;

    public TtmlRenderer(Context context) {
        this.mContext = context;
    }

    public boolean supports(MediaFormat format) {
        if (format.containsKey(MediaFormat.KEY_MIME)) {
            return format.getString(MediaFormat.KEY_MIME).equals(MEDIA_MIMETYPE_TEXT_TTML);
        }
        return false;
    }

    public SubtitleTrack createTrack(MediaFormat format) {
        if (this.mRenderingWidget == null) {
            this.mRenderingWidget = new TtmlRenderingWidget(this.mContext);
        }
        return new TtmlTrack(this.mRenderingWidget, format);
    }
}
