package android.media;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.media.SubtitleController;

public class WebVttRenderer extends SubtitleController.Renderer {
    private final Context mContext;
    private WebVttRenderingWidget mRenderingWidget;

    @UnsupportedAppUsage
    public WebVttRenderer(Context context) {
        this.mContext = context;
    }

    @Override // android.media.SubtitleController.Renderer
    public boolean supports(MediaFormat format) {
        if (format.containsKey(MediaFormat.KEY_MIME)) {
            return format.getString(MediaFormat.KEY_MIME).equals("text/vtt");
        }
        return false;
    }

    @Override // android.media.SubtitleController.Renderer
    public SubtitleTrack createTrack(MediaFormat format) {
        if (this.mRenderingWidget == null) {
            this.mRenderingWidget = new WebVttRenderingWidget(this.mContext);
        }
        return new WebVttTrack(this.mRenderingWidget, format);
    }
}
