package android.media;

import android.content.Context;
import android.media.SubtitleController.Renderer;

public class Cea708CaptionRenderer extends Renderer {
    private Cea708CCWidget mCCWidget;
    private final Context mContext;

    public Cea708CaptionRenderer(Context context) {
        this.mContext = context;
    }

    public boolean supports(MediaFormat format) {
        if (!format.containsKey(MediaFormat.KEY_MIME)) {
            return false;
        }
        return MediaPlayer.MEDIA_MIMETYPE_TEXT_CEA_708.equals(format.getString(MediaFormat.KEY_MIME));
    }

    public SubtitleTrack createTrack(MediaFormat format) {
        if (MediaPlayer.MEDIA_MIMETYPE_TEXT_CEA_708.equals(format.getString(MediaFormat.KEY_MIME))) {
            if (this.mCCWidget == null) {
                this.mCCWidget = new Cea708CCWidget(this.mContext);
            }
            return new Cea708CaptionTrack(this.mCCWidget, format);
        }
        throw new RuntimeException("No matching format: " + format.toString());
    }
}
