package android.media;

import android.content.Context;
import android.media.SubtitleTrack;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;
import android.widget.LinearLayout;
import com.android.internal.widget.SubtitleView;
import java.util.ArrayList;
import java.util.Vector;

/* access modifiers changed from: package-private */
/* compiled from: WebVttRenderer */
public class WebVttRenderingWidget extends ViewGroup implements SubtitleTrack.RenderingWidget {
    private static final boolean DEBUG = false;
    private static final int DEBUG_CUE_BACKGROUND = -2130771968;
    private static final int DEBUG_REGION_BACKGROUND = -2147483393;
    private static final CaptioningManager.CaptionStyle DEFAULT_CAPTION_STYLE = CaptioningManager.CaptionStyle.DEFAULT;
    private static final float LINE_HEIGHT_RATIO = 0.0533f;
    private CaptioningManager.CaptionStyle mCaptionStyle;
    private final CaptioningManager.CaptioningChangeListener mCaptioningListener;
    private final ArrayMap<TextTrackCue, CueLayout> mCueBoxes;
    private float mFontSize;
    private boolean mHasChangeListener;
    private SubtitleTrack.RenderingWidget.OnChangedListener mListener;
    private final CaptioningManager mManager;
    private final ArrayMap<TextTrackRegion, RegionLayout> mRegionBoxes;

    public WebVttRenderingWidget(Context context) {
        this(context, null);
    }

    public WebVttRenderingWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebVttRenderingWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public WebVttRenderingWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mRegionBoxes = new ArrayMap<>();
        this.mCueBoxes = new ArrayMap<>();
        this.mCaptioningListener = new CaptioningManager.CaptioningChangeListener() {
            /* class android.media.WebVttRenderingWidget.AnonymousClass1 */

            @Override // android.view.accessibility.CaptioningManager.CaptioningChangeListener
            public void onFontScaleChanged(float fontScale) {
                float fontSize = ((float) WebVttRenderingWidget.this.getHeight()) * fontScale * WebVttRenderingWidget.LINE_HEIGHT_RATIO;
                WebVttRenderingWidget webVttRenderingWidget = WebVttRenderingWidget.this;
                webVttRenderingWidget.setCaptionStyle(webVttRenderingWidget.mCaptionStyle, fontSize);
            }

            @Override // android.view.accessibility.CaptioningManager.CaptioningChangeListener
            public void onUserStyleChanged(CaptioningManager.CaptionStyle userStyle) {
                WebVttRenderingWidget webVttRenderingWidget = WebVttRenderingWidget.this;
                webVttRenderingWidget.setCaptionStyle(userStyle, webVttRenderingWidget.mFontSize);
            }
        };
        setLayerType(1, null);
        this.mManager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
        this.mCaptionStyle = this.mManager.getUserStyle();
        this.mFontSize = this.mManager.getFontScale() * ((float) getHeight()) * LINE_HEIGHT_RATIO;
    }

    @Override // android.media.SubtitleTrack.RenderingWidget
    public void setSize(int width, int height) {
        measure(View.MeasureSpec.makeMeasureSpec(width, 1073741824), View.MeasureSpec.makeMeasureSpec(height, 1073741824));
        layout(0, 0, width, height);
    }

    @Override // android.view.View, android.media.SubtitleTrack.RenderingWidget, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        manageChangeListener();
    }

    @Override // android.view.View, android.media.SubtitleTrack.RenderingWidget, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        manageChangeListener();
    }

    @Override // android.media.SubtitleTrack.RenderingWidget
    public void setOnChangedListener(SubtitleTrack.RenderingWidget.OnChangedListener listener) {
        this.mListener = listener;
    }

    @Override // android.media.SubtitleTrack.RenderingWidget
    public void setVisible(boolean visible) {
        if (visible) {
            setVisibility(0);
        } else {
            setVisibility(8);
        }
        manageChangeListener();
    }

    private void manageChangeListener() {
        boolean needsListener = isAttachedToWindow() && getVisibility() == 0;
        if (this.mHasChangeListener != needsListener) {
            this.mHasChangeListener = needsListener;
            if (needsListener) {
                this.mManager.addCaptioningChangeListener(this.mCaptioningListener);
                setCaptionStyle(this.mManager.getUserStyle(), this.mManager.getFontScale() * ((float) getHeight()) * LINE_HEIGHT_RATIO);
                return;
            }
            this.mManager.removeCaptioningChangeListener(this.mCaptioningListener);
        }
    }

    public void setActiveCues(Vector<SubtitleTrack.Cue> activeCues) {
        Context context = getContext();
        CaptioningManager.CaptionStyle captionStyle = this.mCaptionStyle;
        float fontSize = this.mFontSize;
        prepForPrune();
        int count = activeCues.size();
        for (int i = 0; i < count; i++) {
            TextTrackCue cue = (TextTrackCue) activeCues.get(i);
            TextTrackRegion region = cue.mRegion;
            if (region != null) {
                RegionLayout regionBox = this.mRegionBoxes.get(region);
                if (regionBox == null) {
                    regionBox = new RegionLayout(context, region, captionStyle, fontSize);
                    this.mRegionBoxes.put(region, regionBox);
                    addView(regionBox, -2, -2);
                }
                regionBox.put(cue);
            } else {
                CueLayout cueBox = this.mCueBoxes.get(cue);
                if (cueBox == null) {
                    cueBox = new CueLayout(context, cue, captionStyle, fontSize);
                    this.mCueBoxes.put(cue, cueBox);
                    addView(cueBox, -2, -2);
                }
                cueBox.update();
                cueBox.setOrder(i);
            }
        }
        prune();
        setSize(getWidth(), getHeight());
        SubtitleTrack.RenderingWidget.OnChangedListener onChangedListener = this.mListener;
        if (onChangedListener != null) {
            onChangedListener.onChanged(this);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCaptionStyle(CaptioningManager.CaptionStyle captionStyle, float fontSize) {
        CaptioningManager.CaptionStyle captionStyle2 = DEFAULT_CAPTION_STYLE.applyStyle(captionStyle);
        this.mCaptionStyle = captionStyle2;
        this.mFontSize = fontSize;
        int cueCount = this.mCueBoxes.size();
        for (int i = 0; i < cueCount; i++) {
            this.mCueBoxes.valueAt(i).setCaptionStyle(captionStyle2, fontSize);
        }
        int regionCount = this.mRegionBoxes.size();
        for (int i2 = 0; i2 < regionCount; i2++) {
            this.mRegionBoxes.valueAt(i2).setCaptionStyle(captionStyle2, fontSize);
        }
    }

    private void prune() {
        int regionCount = this.mRegionBoxes.size();
        int i = 0;
        while (i < regionCount) {
            RegionLayout regionBox = this.mRegionBoxes.valueAt(i);
            if (regionBox.prune()) {
                removeView(regionBox);
                this.mRegionBoxes.removeAt(i);
                regionCount--;
                i--;
            }
            i++;
        }
        int cueCount = this.mCueBoxes.size();
        int i2 = 0;
        while (i2 < cueCount) {
            CueLayout cueBox = this.mCueBoxes.valueAt(i2);
            if (!cueBox.isActive()) {
                removeView(cueBox);
                this.mCueBoxes.removeAt(i2);
                cueCount--;
                i2--;
            }
            i2++;
        }
    }

    private void prepForPrune() {
        int regionCount = this.mRegionBoxes.size();
        for (int i = 0; i < regionCount; i++) {
            this.mRegionBoxes.valueAt(i).prepForPrune();
        }
        int cueCount = this.mCueBoxes.size();
        for (int i2 = 0; i2 < cueCount; i2++) {
            this.mCueBoxes.valueAt(i2).prepForPrune();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int regionCount = this.mRegionBoxes.size();
        for (int i = 0; i < regionCount; i++) {
            this.mRegionBoxes.valueAt(i).measureForParent(widthMeasureSpec, heightMeasureSpec);
        }
        int cueCount = this.mCueBoxes.size();
        for (int i2 = 0; i2 < cueCount; i2++) {
            this.mCueBoxes.valueAt(i2).measureForParent(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int viewportWidth = r - l;
        int viewportHeight = b - t;
        setCaptionStyle(this.mCaptionStyle, this.mManager.getFontScale() * LINE_HEIGHT_RATIO * ((float) viewportHeight));
        int regionCount = this.mRegionBoxes.size();
        for (int i = 0; i < regionCount; i++) {
            layoutRegion(viewportWidth, viewportHeight, this.mRegionBoxes.valueAt(i));
        }
        int cueCount = this.mCueBoxes.size();
        for (int i2 = 0; i2 < cueCount; i2++) {
            layoutCue(viewportWidth, viewportHeight, this.mCueBoxes.valueAt(i2));
        }
    }

    private void layoutRegion(int viewportWidth, int viewportHeight, RegionLayout regionBox) {
        TextTrackRegion region = regionBox.getRegion();
        int regionHeight = regionBox.getMeasuredHeight();
        int regionWidth = regionBox.getMeasuredWidth();
        int left = (int) ((((float) (viewportWidth - regionWidth)) * region.mViewportAnchorPointX) / 100.0f);
        int top = (int) ((((float) (viewportHeight - regionHeight)) * region.mViewportAnchorPointY) / 100.0f);
        regionBox.layout(left, top, left + regionWidth, top + regionHeight);
    }

    private void layoutCue(int viewportWidth, int viewportHeight, CueLayout cueBox) {
        int xPosition;
        int top;
        TextTrackCue cue = cueBox.getCue();
        int direction = getLayoutDirection();
        int absAlignment = resolveCueAlignment(direction, cue.mAlignment);
        boolean cueSnapToLines = cue.mSnapToLines;
        int size = (cueBox.getMeasuredWidth() * 100) / viewportWidth;
        if (absAlignment == 203) {
            xPosition = cue.mTextPosition;
        } else if (absAlignment != 204) {
            xPosition = cue.mTextPosition - (size / 2);
        } else {
            xPosition = cue.mTextPosition - size;
        }
        if (direction == 1) {
            xPosition = 100 - xPosition;
        }
        if (cueSnapToLines) {
            int paddingLeft = (getPaddingLeft() * 100) / viewportWidth;
            int paddingRight = (getPaddingRight() * 100) / viewportWidth;
            if (xPosition < paddingLeft && xPosition + size > paddingLeft) {
                xPosition += paddingLeft;
                size -= paddingLeft;
            }
            float rightEdge = (float) (100 - paddingRight);
            if (((float) xPosition) < rightEdge && ((float) (xPosition + size)) > rightEdge) {
                size -= paddingRight;
            }
        }
        int left = (xPosition * viewportWidth) / 100;
        int width = (size * viewportWidth) / 100;
        int yPosition = calculateLinePosition(cueBox);
        int height = cueBox.getMeasuredHeight();
        if (yPosition < 0) {
            top = viewportHeight + (yPosition * height);
        } else {
            top = ((viewportHeight - height) * yPosition) / 100;
        }
        cueBox.layout(left, top, left + width, top + height);
    }

    private int calculateLinePosition(CueLayout cueBox) {
        TextTrackCue cue = cueBox.getCue();
        Integer linePosition = cue.mLinePosition;
        boolean snapToLines = cue.mSnapToLines;
        boolean autoPosition = linePosition == null;
        if (!snapToLines && !autoPosition && (linePosition.intValue() < 0 || linePosition.intValue() > 100)) {
            return 100;
        }
        if (!autoPosition) {
            return linePosition.intValue();
        }
        if (!snapToLines) {
            return 100;
        }
        return -(cueBox.mOrder + 1);
    }

    /* access modifiers changed from: private */
    public static int resolveCueAlignment(int layoutDirection, int alignment) {
        if (alignment == 201) {
            return layoutDirection == 0 ? 203 : 204;
        }
        if (alignment != 202) {
            return alignment;
        }
        return layoutDirection == 0 ? 204 : 203;
    }

    /* access modifiers changed from: private */
    /* compiled from: WebVttRenderer */
    public static class RegionLayout extends LinearLayout {
        private CaptioningManager.CaptionStyle mCaptionStyle;
        private float mFontSize;
        private final TextTrackRegion mRegion;
        private final ArrayList<CueLayout> mRegionCueBoxes = new ArrayList<>();

        public RegionLayout(Context context, TextTrackRegion region, CaptioningManager.CaptionStyle captionStyle, float fontSize) {
            super(context);
            this.mRegion = region;
            this.mCaptionStyle = captionStyle;
            this.mFontSize = fontSize;
            setOrientation(1);
            setBackgroundColor(captionStyle.windowColor);
        }

        public void setCaptionStyle(CaptioningManager.CaptionStyle captionStyle, float fontSize) {
            this.mCaptionStyle = captionStyle;
            this.mFontSize = fontSize;
            int cueCount = this.mRegionCueBoxes.size();
            for (int i = 0; i < cueCount; i++) {
                this.mRegionCueBoxes.get(i).setCaptionStyle(captionStyle, fontSize);
            }
            setBackgroundColor(captionStyle.windowColor);
        }

        public void measureForParent(int widthMeasureSpec, int heightMeasureSpec) {
            TextTrackRegion region = this.mRegion;
            measure(View.MeasureSpec.makeMeasureSpec((((int) region.mWidth) * View.MeasureSpec.getSize(widthMeasureSpec)) / 100, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(heightMeasureSpec), Integer.MIN_VALUE));
        }

        public void prepForPrune() {
            int cueCount = this.mRegionCueBoxes.size();
            for (int i = 0; i < cueCount; i++) {
                this.mRegionCueBoxes.get(i).prepForPrune();
            }
        }

        public void put(TextTrackCue cue) {
            int cueCount = this.mRegionCueBoxes.size();
            for (int i = 0; i < cueCount; i++) {
                CueLayout cueBox = this.mRegionCueBoxes.get(i);
                if (cueBox.getCue() == cue) {
                    cueBox.update();
                    return;
                }
            }
            CueLayout cueBox2 = new CueLayout(getContext(), cue, this.mCaptionStyle, this.mFontSize);
            this.mRegionCueBoxes.add(cueBox2);
            addView(cueBox2, -2, -2);
            if (getChildCount() > this.mRegion.mLines) {
                removeViewAt(0);
            }
        }

        public boolean prune() {
            int cueCount = this.mRegionCueBoxes.size();
            int i = 0;
            while (i < cueCount) {
                CueLayout cueBox = this.mRegionCueBoxes.get(i);
                if (!cueBox.isActive()) {
                    this.mRegionCueBoxes.remove(i);
                    removeView(cueBox);
                    cueCount--;
                    i--;
                }
                i++;
            }
            return this.mRegionCueBoxes.isEmpty();
        }

        public TextTrackRegion getRegion() {
            return this.mRegion;
        }
    }

    /* access modifiers changed from: private */
    /* compiled from: WebVttRenderer */
    public static class CueLayout extends LinearLayout {
        private boolean mActive;
        private CaptioningManager.CaptionStyle mCaptionStyle;
        public final TextTrackCue mCue;
        private float mFontSize;
        private int mOrder;

        public CueLayout(Context context, TextTrackCue cue, CaptioningManager.CaptionStyle captionStyle, float fontSize) {
            super(context);
            this.mCue = cue;
            this.mCaptionStyle = captionStyle;
            this.mFontSize = fontSize;
            int i = 0;
            int i2 = 1;
            boolean horizontal = cue.mWritingDirection == 100;
            setOrientation(horizontal ? 1 : i);
            switch (cue.mAlignment) {
                case 200:
                    setGravity(!horizontal ? 16 : i2);
                    break;
                case 201:
                    setGravity(Gravity.START);
                    break;
                case 202:
                    setGravity(Gravity.END);
                    break;
                case 203:
                    setGravity(3);
                    break;
                case 204:
                    setGravity(5);
                    break;
            }
            update();
        }

        public void setCaptionStyle(CaptioningManager.CaptionStyle style, float fontSize) {
            this.mCaptionStyle = style;
            this.mFontSize = fontSize;
            int n = getChildCount();
            for (int i = 0; i < n; i++) {
                View child = getChildAt(i);
                if (child instanceof SpanLayout) {
                    ((SpanLayout) child).setCaptionStyle(style, fontSize);
                }
            }
        }

        public void prepForPrune() {
            this.mActive = false;
        }

        public void update() {
            Layout.Alignment alignment;
            this.mActive = true;
            removeAllViews();
            int cueAlignment = WebVttRenderingWidget.resolveCueAlignment(getLayoutDirection(), this.mCue.mAlignment);
            if (cueAlignment == 203) {
                alignment = Layout.Alignment.ALIGN_LEFT;
            } else if (cueAlignment != 204) {
                alignment = Layout.Alignment.ALIGN_CENTER;
            } else {
                alignment = Layout.Alignment.ALIGN_RIGHT;
            }
            CaptioningManager.CaptionStyle captionStyle = this.mCaptionStyle;
            float fontSize = this.mFontSize;
            for (TextTrackCueSpan[] textTrackCueSpanArr : this.mCue.mLines) {
                SpanLayout lineBox = new SpanLayout(getContext(), textTrackCueSpanArr);
                lineBox.setAlignment(alignment);
                lineBox.setCaptionStyle(captionStyle, fontSize);
                addView(lineBox, -2, -2);
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.LinearLayout, android.view.View
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        public void measureForParent(int widthMeasureSpec, int heightMeasureSpec) {
            int maximumSize;
            TextTrackCue cue = this.mCue;
            int specWidth = View.MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = View.MeasureSpec.getSize(heightMeasureSpec);
            int absAlignment = WebVttRenderingWidget.resolveCueAlignment(getLayoutDirection(), cue.mAlignment);
            if (absAlignment != 200) {
                if (absAlignment == 203) {
                    maximumSize = 100 - cue.mTextPosition;
                } else if (absAlignment != 204) {
                    maximumSize = 0;
                } else {
                    maximumSize = cue.mTextPosition;
                }
            } else if (cue.mTextPosition <= 50) {
                maximumSize = cue.mTextPosition * 2;
            } else {
                maximumSize = (100 - cue.mTextPosition) * 2;
            }
            measure(View.MeasureSpec.makeMeasureSpec((Math.min(cue.mSize, maximumSize) * specWidth) / 100, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(specHeight, Integer.MIN_VALUE));
        }

        public void setOrder(int order) {
            this.mOrder = order;
        }

        public boolean isActive() {
            return this.mActive;
        }

        public TextTrackCue getCue() {
            return this.mCue;
        }
    }

    /* access modifiers changed from: private */
    /* compiled from: WebVttRenderer */
    public static class SpanLayout extends SubtitleView {
        private final SpannableStringBuilder mBuilder = new SpannableStringBuilder();
        private final TextTrackCueSpan[] mSpans;

        public SpanLayout(Context context, TextTrackCueSpan[] spans) {
            super(context);
            this.mSpans = spans;
            update();
        }

        public void update() {
            SpannableStringBuilder builder = this.mBuilder;
            TextTrackCueSpan[] spans = this.mSpans;
            builder.clear();
            builder.clearSpans();
            int spanCount = spans.length;
            for (int i = 0; i < spanCount; i++) {
                if (spans[i].mEnabled) {
                    builder.append((CharSequence) spans[i].mText);
                }
            }
            setText(builder);
        }

        public void setCaptionStyle(CaptioningManager.CaptionStyle captionStyle, float fontSize) {
            setBackgroundColor(captionStyle.backgroundColor);
            setForegroundColor(captionStyle.foregroundColor);
            setEdgeColor(captionStyle.edgeColor);
            setEdgeType(captionStyle.edgeType);
            setTypeface(captionStyle.getTypeface());
            setTextSize(fontSize);
        }
    }
}
