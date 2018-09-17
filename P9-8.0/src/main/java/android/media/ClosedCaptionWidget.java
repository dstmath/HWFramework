package android.media;

import android.content.Context;
import android.media.SubtitleTrack.RenderingWidget;
import android.media.SubtitleTrack.RenderingWidget.OnChangedListener;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;
import android.view.accessibility.CaptioningManager.CaptionStyle;
import android.view.accessibility.CaptioningManager.CaptioningChangeListener;

/* compiled from: ClosedCaptionRenderer */
abstract class ClosedCaptionWidget extends ViewGroup implements RenderingWidget {
    private static final CaptionStyle DEFAULT_CAPTION_STYLE = CaptionStyle.DEFAULT;
    protected CaptionStyle mCaptionStyle;
    private final CaptioningChangeListener mCaptioningListener;
    protected ClosedCaptionLayout mClosedCaptionLayout;
    private boolean mHasChangeListener;
    protected OnChangedListener mListener;
    private final CaptioningManager mManager;

    /* compiled from: ClosedCaptionRenderer */
    interface ClosedCaptionLayout {
        void setCaptionStyle(CaptionStyle captionStyle);

        void setFontScale(float f);
    }

    public abstract ClosedCaptionLayout createCaptionLayout(Context context);

    public ClosedCaptionWidget(Context context) {
        this(context, null);
    }

    public ClosedCaptionWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClosedCaptionWidget(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public ClosedCaptionWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mCaptioningListener = new CaptioningChangeListener() {
            public void onUserStyleChanged(CaptionStyle userStyle) {
                ClosedCaptionWidget.this.mCaptionStyle = ClosedCaptionWidget.DEFAULT_CAPTION_STYLE.applyStyle(userStyle);
                ClosedCaptionWidget.this.mClosedCaptionLayout.setCaptionStyle(ClosedCaptionWidget.this.mCaptionStyle);
            }

            public void onFontScaleChanged(float fontScale) {
                ClosedCaptionWidget.this.mClosedCaptionLayout.setFontScale(fontScale);
            }
        };
        setLayerType(1, null);
        this.mManager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
        this.mCaptionStyle = DEFAULT_CAPTION_STYLE.applyStyle(this.mManager.getUserStyle());
        this.mClosedCaptionLayout = createCaptionLayout(context);
        this.mClosedCaptionLayout.setCaptionStyle(this.mCaptionStyle);
        this.mClosedCaptionLayout.setFontScale(this.mManager.getFontScale());
        addView((ViewGroup) this.mClosedCaptionLayout, -1, -1);
        requestLayout();
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
        manageChangeListener();
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        manageChangeListener();
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        manageChangeListener();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ((ViewGroup) this.mClosedCaptionLayout).measure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        ((ViewGroup) this.mClosedCaptionLayout).layout(l, t, r, b);
    }

    private void manageChangeListener() {
        boolean needsListener = isAttachedToWindow() && getVisibility() == 0;
        if (this.mHasChangeListener != needsListener) {
            this.mHasChangeListener = needsListener;
            if (needsListener) {
                this.mManager.addCaptioningChangeListener(this.mCaptioningListener);
            } else {
                this.mManager.removeCaptioningChangeListener(this.mCaptioningListener);
            }
        }
    }
}
