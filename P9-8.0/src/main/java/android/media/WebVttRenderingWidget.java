package android.media;

import android.content.Context;
import android.media.SubtitleTrack.Cue;
import android.media.SubtitleTrack.RenderingWidget;
import android.media.SubtitleTrack.RenderingWidget.OnChangedListener;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;
import android.view.accessibility.CaptioningManager.CaptionStyle;
import android.view.accessibility.CaptioningManager.CaptioningChangeListener;
import android.widget.LinearLayout;
import com.android.internal.widget.SubtitleView;
import java.util.ArrayList;
import java.util.Vector;

/* compiled from: WebVttRenderer */
class WebVttRenderingWidget extends ViewGroup implements RenderingWidget {
    private static final boolean DEBUG = false;
    private static final int DEBUG_CUE_BACKGROUND = -2130771968;
    private static final int DEBUG_REGION_BACKGROUND = -2147483393;
    private static final CaptionStyle DEFAULT_CAPTION_STYLE = CaptionStyle.DEFAULT;
    private static final float LINE_HEIGHT_RATIO = 0.0533f;
    private CaptionStyle mCaptionStyle;
    private final CaptioningChangeListener mCaptioningListener;
    private final ArrayMap<TextTrackCue, CueLayout> mCueBoxes;
    private float mFontSize;
    private boolean mHasChangeListener;
    private OnChangedListener mListener;
    private final CaptioningManager mManager;
    private final ArrayMap<TextTrackRegion, RegionLayout> mRegionBoxes;

    /* compiled from: WebVttRenderer */
    private static class CueLayout extends LinearLayout {
        private boolean mActive;
        private CaptionStyle mCaptionStyle;
        public final TextTrackCue mCue;
        private float mFontSize;
        private int mOrder;

        public CueLayout(Context context, TextTrackCue cue, CaptionStyle captionStyle, float fontSize) {
            int i = 1;
            super(context);
            this.mCue = cue;
            this.mCaptionStyle = captionStyle;
            this.mFontSize = fontSize;
            boolean horizontal = cue.mWritingDirection == 100;
            setOrientation(horizontal ? 1 : 0);
            switch (cue.mAlignment) {
                case 200:
                    if (!horizontal) {
                        i = 16;
                    }
                    setGravity(i);
                    break;
                case 201:
                    setGravity(8388611);
                    break;
                case 202:
                    setGravity(8388613);
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

        public void setCaptionStyle(CaptionStyle style, float fontSize) {
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
            Alignment alignment;
            this.mActive = true;
            removeAllViews();
            switch (WebVttRenderingWidget.resolveCueAlignment(getLayoutDirection(), this.mCue.mAlignment)) {
                case 203:
                    alignment = Alignment.ALIGN_LEFT;
                    break;
                case 204:
                    alignment = Alignment.ALIGN_RIGHT;
                    break;
                default:
                    alignment = Alignment.ALIGN_CENTER;
                    break;
            }
            CaptionStyle captionStyle = this.mCaptionStyle;
            float fontSize = this.mFontSize;
            for (TextTrackCueSpan[] spanLayout : this.mCue.mLines) {
                SpanLayout lineBox = new SpanLayout(getContext(), spanLayout);
                lineBox.setAlignment(alignment);
                lineBox.setCaptionStyle(captionStyle, fontSize);
                addView(lineBox, -2, -2);
            }
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        public void measureForParent(int widthMeasureSpec, int heightMeasureSpec) {
            int maximumSize;
            TextTrackCue cue = this.mCue;
            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = MeasureSpec.getSize(heightMeasureSpec);
            switch (WebVttRenderingWidget.resolveCueAlignment(getLayoutDirection(), cue.mAlignment)) {
                case 200:
                    if (cue.mTextPosition > 50) {
                        maximumSize = (100 - cue.mTextPosition) * 2;
                        break;
                    } else {
                        maximumSize = cue.mTextPosition * 2;
                        break;
                    }
                case 203:
                    maximumSize = 100 - cue.mTextPosition;
                    break;
                case 204:
                    maximumSize = cue.mTextPosition;
                    break;
                default:
                    maximumSize = 0;
                    break;
            }
            measure(MeasureSpec.makeMeasureSpec((Math.min(cue.mSize, maximumSize) * specWidth) / 100, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(specHeight, Integer.MIN_VALUE));
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

    /* compiled from: WebVttRenderer */
    private static class RegionLayout extends LinearLayout {
        private CaptionStyle mCaptionStyle;
        private float mFontSize;
        private final TextTrackRegion mRegion;
        private final ArrayList<CueLayout> mRegionCueBoxes = new ArrayList();

        public RegionLayout(Context context, TextTrackRegion region, CaptionStyle captionStyle, float fontSize) {
            super(context);
            this.mRegion = region;
            this.mCaptionStyle = captionStyle;
            this.mFontSize = fontSize;
            setOrientation(1);
            setBackgroundColor(captionStyle.windowColor);
        }

        public void setCaptionStyle(CaptionStyle captionStyle, float fontSize) {
            this.mCaptionStyle = captionStyle;
            this.mFontSize = fontSize;
            int cueCount = this.mRegionCueBoxes.size();
            for (int i = 0; i < cueCount; i++) {
                ((CueLayout) this.mRegionCueBoxes.get(i)).setCaptionStyle(captionStyle, fontSize);
            }
            setBackgroundColor(captionStyle.windowColor);
        }

        public void measureForParent(int widthMeasureSpec, int heightMeasureSpec) {
            TextTrackRegion region = this.mRegion;
            measure(MeasureSpec.makeMeasureSpec((((int) region.mWidth) * MeasureSpec.getSize(widthMeasureSpec)) / 100, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), Integer.MIN_VALUE));
        }

        public void prepForPrune() {
            int cueCount = this.mRegionCueBoxes.size();
            for (int i = 0; i < cueCount; i++) {
                ((CueLayout) this.mRegionCueBoxes.get(i)).prepForPrune();
            }
        }

        public void put(TextTrackCue cue) {
            CueLayout cueBox;
            int cueCount = this.mRegionCueBoxes.size();
            for (int i = 0; i < cueCount; i++) {
                cueBox = (CueLayout) this.mRegionCueBoxes.get(i);
                if (cueBox.getCue() == cue) {
                    cueBox.update();
                    return;
                }
            }
            cueBox = new CueLayout(getContext(), cue, this.mCaptionStyle, this.mFontSize);
            this.mRegionCueBoxes.add(cueBox);
            addView(cueBox, -2, -2);
            if (getChildCount() > this.mRegion.mLines) {
                removeViewAt(0);
            }
        }

        public boolean prune() {
            int cueCount = this.mRegionCueBoxes.size();
            int i = 0;
            while (i < cueCount) {
                CueLayout cueBox = (CueLayout) this.mRegionCueBoxes.get(i);
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

    /* compiled from: WebVttRenderer */
    private static class SpanLayout extends SubtitleView {
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
                    builder.append(spans[i].mText);
                }
            }
            setText(builder);
        }

        public void setCaptionStyle(CaptionStyle captionStyle, float fontSize) {
            setBackgroundColor(captionStyle.backgroundColor);
            setForegroundColor(captionStyle.foregroundColor);
            setEdgeColor(captionStyle.edgeColor);
            setEdgeType(captionStyle.edgeType);
            setTypeface(captionStyle.getTypeface());
            setTextSize(fontSize);
        }
    }

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
        this.mRegionBoxes = new ArrayMap();
        this.mCueBoxes = new ArrayMap();
        this.mCaptioningListener = new CaptioningChangeListener() {
            public void onFontScaleChanged(float fontScale) {
                WebVttRenderingWidget.this.setCaptionStyle(WebVttRenderingWidget.this.mCaptionStyle, (((float) WebVttRenderingWidget.this.getHeight()) * fontScale) * WebVttRenderingWidget.LINE_HEIGHT_RATIO);
            }

            public void onUserStyleChanged(CaptionStyle userStyle) {
                WebVttRenderingWidget.this.setCaptionStyle(userStyle, WebVttRenderingWidget.this.mFontSize);
            }
        };
        setLayerType(1, null);
        this.mManager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
        this.mCaptionStyle = this.mManager.getUserStyle();
        this.mFontSize = (this.mManager.getFontScale() * ((float) getHeight())) * LINE_HEIGHT_RATIO;
    }

    public void setSize(int width, int height) {
        measure(MeasureSpec.makeMeasureSpec(width, 1073741824), MeasureSpec.makeMeasureSpec(height, 1073741824));
        layout(0, 0, width, height);
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        manageChangeListener();
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        manageChangeListener();
    }

    public void setOnChangedListener(OnChangedListener listener) {
        this.mListener = listener;
    }

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
                setCaptionStyle(this.mManager.getUserStyle(), (this.mManager.getFontScale() * ((float) getHeight())) * LINE_HEIGHT_RATIO);
                return;
            }
            this.mManager.removeCaptioningChangeListener(this.mCaptioningListener);
        }
    }

    public void setActiveCues(Vector<Cue> activeCues) {
        Context context = getContext();
        CaptionStyle captionStyle = this.mCaptionStyle;
        float fontSize = this.mFontSize;
        prepForPrune();
        int count = activeCues.size();
        for (int i = 0; i < count; i++) {
            TextTrackCue cue = (TextTrackCue) activeCues.get(i);
            TextTrackRegion region = cue.mRegion;
            if (region != null) {
                RegionLayout regionBox = (RegionLayout) this.mRegionBoxes.get(region);
                if (regionBox == null) {
                    regionBox = new RegionLayout(context, region, captionStyle, fontSize);
                    this.mRegionBoxes.put(region, regionBox);
                    addView(regionBox, -2, -2);
                }
                regionBox.put(cue);
            } else {
                CueLayout cueBox = (CueLayout) this.mCueBoxes.get(cue);
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
        if (this.mListener != null) {
            this.mListener.onChanged(this);
        }
    }

    private void setCaptionStyle(CaptionStyle captionStyle, float fontSize) {
        int i;
        captionStyle = DEFAULT_CAPTION_STYLE.applyStyle(captionStyle);
        this.mCaptionStyle = captionStyle;
        this.mFontSize = fontSize;
        int cueCount = this.mCueBoxes.size();
        for (i = 0; i < cueCount; i++) {
            ((CueLayout) this.mCueBoxes.valueAt(i)).setCaptionStyle(captionStyle, fontSize);
        }
        int regionCount = this.mRegionBoxes.size();
        for (i = 0; i < regionCount; i++) {
            ((RegionLayout) this.mRegionBoxes.valueAt(i)).setCaptionStyle(captionStyle, fontSize);
        }
    }

    private void prune() {
        int regionCount = this.mRegionBoxes.size();
        int i = 0;
        while (i < regionCount) {
            RegionLayout regionBox = (RegionLayout) this.mRegionBoxes.valueAt(i);
            if (regionBox.prune()) {
                removeView(regionBox);
                this.mRegionBoxes.removeAt(i);
                regionCount--;
                i--;
            }
            i++;
        }
        int cueCount = this.mCueBoxes.size();
        i = 0;
        while (i < cueCount) {
            CueLayout cueBox = (CueLayout) this.mCueBoxes.valueAt(i);
            if (!cueBox.isActive()) {
                removeView(cueBox);
                this.mCueBoxes.removeAt(i);
                cueCount--;
                i--;
            }
            i++;
        }
    }

    private void prepForPrune() {
        int i;
        int regionCount = this.mRegionBoxes.size();
        for (i = 0; i < regionCount; i++) {
            ((RegionLayout) this.mRegionBoxes.valueAt(i)).prepForPrune();
        }
        int cueCount = this.mCueBoxes.size();
        for (i = 0; i < cueCount; i++) {
            ((CueLayout) this.mCueBoxes.valueAt(i)).prepForPrune();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int regionCount = this.mRegionBoxes.size();
        for (i = 0; i < regionCount; i++) {
            ((RegionLayout) this.mRegionBoxes.valueAt(i)).measureForParent(widthMeasureSpec, heightMeasureSpec);
        }
        int cueCount = this.mCueBoxes.size();
        for (i = 0; i < cueCount; i++) {
            ((CueLayout) this.mCueBoxes.valueAt(i)).measureForParent(widthMeasureSpec, heightMeasureSpec);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int i;
        int viewportWidth = r - l;
        int viewportHeight = b - t;
        setCaptionStyle(this.mCaptionStyle, (this.mManager.getFontScale() * LINE_HEIGHT_RATIO) * ((float) viewportHeight));
        int regionCount = this.mRegionBoxes.size();
        for (i = 0; i < regionCount; i++) {
            layoutRegion(viewportWidth, viewportHeight, (RegionLayout) this.mRegionBoxes.valueAt(i));
        }
        int cueCount = this.mCueBoxes.size();
        for (i = 0; i < cueCount; i++) {
            layoutCue(viewportWidth, viewportHeight, (CueLayout) this.mCueBoxes.valueAt(i));
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
        switch (absAlignment) {
            case 203:
                xPosition = cue.mTextPosition;
                break;
            case 204:
                xPosition = cue.mTextPosition - size;
                break;
            default:
                xPosition = cue.mTextPosition - (size / 2);
                break;
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
        if (!snapToLines && (autoPosition ^ 1) != 0 && (linePosition.intValue() < 0 || linePosition.intValue() > 100)) {
            return 100;
        }
        if (!autoPosition) {
            return linePosition.intValue();
        }
        if (snapToLines) {
            return -(cueBox.mOrder + 1);
        }
        return 100;
    }

    private static int resolveCueAlignment(int layoutDirection, int alignment) {
        int i = 204;
        int i2 = 203;
        switch (alignment) {
            case 201:
                if (layoutDirection != 0) {
                    i2 = 204;
                }
                return i2;
            case 202:
                if (layoutDirection != 0) {
                    i = 203;
                }
                return i;
            default:
                return alignment;
        }
    }
}
