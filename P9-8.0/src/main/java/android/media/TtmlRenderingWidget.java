package android.media;

import android.content.Context;
import android.media.SubtitleTrack.Cue;
import android.media.SubtitleTrack.RenderingWidget;
import android.media.SubtitleTrack.RenderingWidget.OnChangedListener;
import android.net.ProxyInfo;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.view.accessibility.CaptioningManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Vector;

/* compiled from: TtmlRenderer */
class TtmlRenderingWidget extends LinearLayout implements RenderingWidget {
    private OnChangedListener mListener;
    private final TextView mTextView;

    public TtmlRenderingWidget(Context context) {
        this(context, null);
    }

    public TtmlRenderingWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TtmlRenderingWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TtmlRenderingWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayerType(1, null);
        CaptioningManager captionManager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
        this.mTextView = new TextView(context);
        this.mTextView.setTextColor(captionManager.getUserStyle().foregroundColor);
        addView(this.mTextView, -1, -1);
        this.mTextView.setGravity(81);
    }

    public void setOnChangedListener(OnChangedListener listener) {
        this.mListener = listener;
    }

    public void setSize(int width, int height) {
        measure(MeasureSpec.makeMeasureSpec(width, 1073741824), MeasureSpec.makeMeasureSpec(height, 1073741824));
        layout(0, 0, width, height);
    }

    public void setVisible(boolean visible) {
        if (visible) {
            setVisibility(0);
        } else {
            setVisibility(8);
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setActiveCues(Vector<Cue> activeCues) {
        int count = activeCues.size();
        String subtitleText = ProxyInfo.LOCAL_EXCL_LIST;
        for (int i = 0; i < count; i++) {
            subtitleText = subtitleText + ((TtmlCue) activeCues.get(i)).mText + "\n";
        }
        this.mTextView.setText(subtitleText);
        if (this.mListener != null) {
            this.mListener.onChanged(this);
        }
    }
}
