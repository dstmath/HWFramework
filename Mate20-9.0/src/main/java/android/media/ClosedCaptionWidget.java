package android.media;

import android.content.Context;
import android.media.SubtitleTrack;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;

/* compiled from: ClosedCaptionRenderer */
abstract class ClosedCaptionWidget extends ViewGroup implements SubtitleTrack.RenderingWidget {
    /* access modifiers changed from: private */
    public static final CaptioningManager.CaptionStyle DEFAULT_CAPTION_STYLE = CaptioningManager.CaptionStyle.DEFAULT;
    protected CaptioningManager.CaptionStyle mCaptionStyle;
    private final CaptioningManager.CaptioningChangeListener mCaptioningListener;
    protected ClosedCaptionLayout mClosedCaptionLayout;
    private boolean mHasChangeListener;
    protected SubtitleTrack.RenderingWidget.OnChangedListener mListener;
    private final CaptioningManager mManager;

    /* compiled from: ClosedCaptionRenderer */
    interface ClosedCaptionLayout {
        void setCaptionStyle(CaptioningManager.CaptionStyle captionStyle);

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
        this.mCaptioningListener = new CaptioningManager.CaptioningChangeListener() {
            public void onUserStyleChanged(CaptioningManager.CaptionStyle userStyle) {
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

    public void setOnChangedListener(SubtitleTrack.RenderingWidget.OnChangedListener listener) {
        this.mListener = listener;
    }

    public void setSize(int width, int height) {
        measure(View.MeasureSpec.makeMeasureSpec(width, 1073741824), View.MeasureSpec.makeMeasureSpec(height, 1073741824));
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

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ((ViewGroup) this.mClosedCaptionLayout).measure(widthMeasureSpec, heightMeasureSpec);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
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
