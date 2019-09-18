package android.support.v4.media.subtitle;

import android.content.Context;
import android.support.annotation.RequiresApi;
import android.support.v4.media.subtitle.SubtitleTrack;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;

@RequiresApi(28)
abstract class ClosedCaptionWidget extends ViewGroup implements SubtitleTrack.RenderingWidget {
    protected CaptioningManager.CaptionStyle mCaptionStyle;
    private final CaptioningManager.CaptioningChangeListener mCaptioningListener;
    protected ClosedCaptionLayout mClosedCaptionLayout;
    private boolean mHasChangeListener;
    protected SubtitleTrack.RenderingWidget.OnChangedListener mListener;
    private final CaptioningManager mManager;

    interface ClosedCaptionLayout {
        void setCaptionStyle(CaptioningManager.CaptionStyle captionStyle);

        void setFontScale(float f);
    }

    public abstract ClosedCaptionLayout createCaptionLayout(Context context);

    ClosedCaptionWidget(Context context) {
        this(context, null);
    }

    ClosedCaptionWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    ClosedCaptionWidget(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    ClosedCaptionWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mCaptioningListener = new CaptioningManager.CaptioningChangeListener() {
            public void onUserStyleChanged(CaptioningManager.CaptionStyle userStyle) {
                ClosedCaptionWidget.this.mCaptionStyle = userStyle;
                ClosedCaptionWidget.this.mClosedCaptionLayout.setCaptionStyle(ClosedCaptionWidget.this.mCaptionStyle);
            }

            public void onFontScaleChanged(float fontScale) {
                ClosedCaptionWidget.this.mClosedCaptionLayout.setFontScale(fontScale);
            }
        };
        setLayerType(1, null);
        this.mManager = (CaptioningManager) context.getSystemService("captioning");
        this.mCaptionStyle = this.mManager.getUserStyle();
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
