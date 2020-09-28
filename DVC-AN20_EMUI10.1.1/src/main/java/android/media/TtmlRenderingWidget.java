package android.media;

import android.content.Context;
import android.media.SubtitleTrack;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.CaptioningManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Vector;

/* compiled from: TtmlRenderer */
class TtmlRenderingWidget extends LinearLayout implements SubtitleTrack.RenderingWidget {
    private SubtitleTrack.RenderingWidget.OnChangedListener mListener;
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
        this.mTextView = new TextView(context);
        this.mTextView.setTextColor(((CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE)).getUserStyle().foregroundColor);
        addView(this.mTextView, -1, -1);
        this.mTextView.setGravity(81);
    }

    @Override // android.media.SubtitleTrack.RenderingWidget
    public void setOnChangedListener(SubtitleTrack.RenderingWidget.OnChangedListener listener) {
        this.mListener = listener;
    }

    @Override // android.media.SubtitleTrack.RenderingWidget
    public void setSize(int width, int height) {
        measure(View.MeasureSpec.makeMeasureSpec(width, 1073741824), View.MeasureSpec.makeMeasureSpec(height, 1073741824));
        layout(0, 0, width, height);
    }

    @Override // android.media.SubtitleTrack.RenderingWidget
    public void setVisible(boolean visible) {
        if (visible) {
            setVisibility(0);
        } else {
            setVisibility(8);
        }
    }

    @Override // android.view.View, android.media.SubtitleTrack.RenderingWidget, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override // android.view.View, android.media.SubtitleTrack.RenderingWidget, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setActiveCues(Vector<SubtitleTrack.Cue> activeCues) {
        String subtitleText = "";
        for (int i = 0; i < activeCues.size(); i++) {
            subtitleText = subtitleText + ((TtmlCue) activeCues.get(i)).mText + "\n";
        }
        this.mTextView.setText(subtitleText);
        SubtitleTrack.RenderingWidget.OnChangedListener onChangedListener = this.mListener;
        if (onChangedListener != null) {
            onChangedListener.onChanged(this);
        }
    }
}
