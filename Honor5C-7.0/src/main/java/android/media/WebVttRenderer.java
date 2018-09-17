package android.media;

import android.content.Context;
import android.media.SubtitleController.Renderer;

public class WebVttRenderer extends Renderer {
    private final Context mContext;
    private WebVttRenderingWidget mRenderingWidget;

    public WebVttRenderer(Context context) {
        this.mContext = context;
    }

    public boolean supports(MediaFormat format) {
        if (format.containsKey(MediaFormat.KEY_MIME)) {
            return format.getString(MediaFormat.KEY_MIME).equals(MediaPlayer.MEDIA_MIMETYPE_TEXT_VTT);
        }
        return false;
    }

    public SubtitleTrack createTrack(MediaFormat format) {
        if (this.mRenderingWidget == null) {
            this.mRenderingWidget = new WebVttRenderingWidget(this.mContext);
        }
        return new WebVttTrack(this.mRenderingWidget, format);
    }
}
