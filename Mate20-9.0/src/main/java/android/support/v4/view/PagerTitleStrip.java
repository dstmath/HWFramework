package android.support.v4.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.TextViewCompat;
import android.text.TextUtils;
import android.text.method.SingleLineTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.util.Locale;

@ViewPager.DecorView
public class PagerTitleStrip extends ViewGroup {
    private static final int[] ATTRS = {16842804, 16842901, 16842904, 16842927};
    private static final float SIDE_ALPHA = 0.6f;
    private static final int[] TEXT_ATTRS = {16843660};
    private static final int TEXT_SPACING = 16;
    TextView mCurrText;
    private int mGravity;
    private int mLastKnownCurrentPage;
    float mLastKnownPositionOffset;
    TextView mNextText;
    private int mNonPrimaryAlpha;
    private final PageListener mPageListener;
    ViewPager mPager;
    TextView mPrevText;
    private int mScaledTextSpacing;
    int mTextColor;
    private boolean mUpdatingPositions;
    private boolean mUpdatingText;
    private WeakReference<PagerAdapter> mWatchingAdapter;

    private class PageListener extends DataSetObserver implements ViewPager.OnPageChangeListener, ViewPager.OnAdapterChangeListener {
        private int mScrollState;

        PageListener() {
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (positionOffset > 0.5f) {
                position++;
            }
            PagerTitleStrip.this.updateTextPositions(position, positionOffset, false);
        }

        public void onPageSelected(int position) {
            if (this.mScrollState == 0) {
                PagerTitleStrip.this.updateText(PagerTitleStrip.this.mPager.getCurrentItem(), PagerTitleStrip.this.mPager.getAdapter());
                float offset = 0.0f;
                if (PagerTitleStrip.this.mLastKnownPositionOffset >= 0.0f) {
                    offset = PagerTitleStrip.this.mLastKnownPositionOffset;
                }
                PagerTitleStrip.this.updateTextPositions(PagerTitleStrip.this.mPager.getCurrentItem(), offset, true);
            }
        }

        public void onPageScrollStateChanged(int state) {
            this.mScrollState = state;
        }

        public void onAdapterChanged(ViewPager viewPager, PagerAdapter oldAdapter, PagerAdapter newAdapter) {
            PagerTitleStrip.this.updateAdapter(oldAdapter, newAdapter);
        }

        public void onChanged() {
            PagerTitleStrip.this.updateText(PagerTitleStrip.this.mPager.getCurrentItem(), PagerTitleStrip.this.mPager.getAdapter());
            float offset = 0.0f;
            if (PagerTitleStrip.this.mLastKnownPositionOffset >= 0.0f) {
                offset = PagerTitleStrip.this.mLastKnownPositionOffset;
            }
            PagerTitleStrip.this.updateTextPositions(PagerTitleStrip.this.mPager.getCurrentItem(), offset, true);
        }
    }

    private static class SingleLineAllCapsTransform extends SingleLineTransformationMethod {
        private Locale mLocale;

        SingleLineAllCapsTransform(Context context) {
            this.mLocale = context.getResources().getConfiguration().locale;
        }

        public CharSequence getTransformation(CharSequence source, View view) {
            CharSequence source2 = super.getTransformation(source, view);
            if (source2 != null) {
                return source2.toString().toUpperCase(this.mLocale);
            }
            return null;
        }
    }

    private static void setSingleLineAllCaps(TextView text) {
        text.setTransformationMethod(new SingleLineAllCapsTransform(text.getContext()));
    }

    public PagerTitleStrip(@NonNull Context context) {
        this(context, null);
    }

    public PagerTitleStrip(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mLastKnownCurrentPage = -1;
        this.mLastKnownPositionOffset = -1.0f;
        this.mPageListener = new PageListener();
        TextView textView = new TextView(context);
        this.mPrevText = textView;
        addView(textView);
        TextView textView2 = new TextView(context);
        this.mCurrText = textView2;
        addView(textView2);
        TextView textView3 = new TextView(context);
        this.mNextText = textView3;
        addView(textView3);
        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);
        int textAppearance = a.getResourceId(0, 0);
        if (textAppearance != 0) {
            TextViewCompat.setTextAppearance(this.mPrevText, textAppearance);
            TextViewCompat.setTextAppearance(this.mCurrText, textAppearance);
            TextViewCompat.setTextAppearance(this.mNextText, textAppearance);
        }
        int textSize = a.getDimensionPixelSize(1, 0);
        if (textSize != 0) {
            setTextSize(0, (float) textSize);
        }
        if (a.hasValue(2)) {
            int textColor = a.getColor(2, 0);
            this.mPrevText.setTextColor(textColor);
            this.mCurrText.setTextColor(textColor);
            this.mNextText.setTextColor(textColor);
        }
        this.mGravity = a.getInteger(3, 80);
        a.recycle();
        this.mTextColor = this.mCurrText.getTextColors().getDefaultColor();
        setNonPrimaryAlpha(SIDE_ALPHA);
        this.mPrevText.setEllipsize(TextUtils.TruncateAt.END);
        this.mCurrText.setEllipsize(TextUtils.TruncateAt.END);
        this.mNextText.setEllipsize(TextUtils.TruncateAt.END);
        boolean allCaps = false;
        if (textAppearance != 0) {
            TypedArray ta = context.obtainStyledAttributes(textAppearance, TEXT_ATTRS);
            allCaps = ta.getBoolean(0, false);
            ta.recycle();
        }
        if (allCaps) {
            setSingleLineAllCaps(this.mPrevText);
            setSingleLineAllCaps(this.mCurrText);
            setSingleLineAllCaps(this.mNextText);
        } else {
            this.mPrevText.setSingleLine();
            this.mCurrText.setSingleLine();
            this.mNextText.setSingleLine();
        }
        this.mScaledTextSpacing = (int) (16.0f * context.getResources().getDisplayMetrics().density);
    }

    public void setTextSpacing(int spacingPixels) {
        this.mScaledTextSpacing = spacingPixels;
        requestLayout();
    }

    public int getTextSpacing() {
        return this.mScaledTextSpacing;
    }

    public void setNonPrimaryAlpha(@FloatRange(from = 0.0d, to = 1.0d) float alpha) {
        this.mNonPrimaryAlpha = ((int) (255.0f * alpha)) & 255;
        int transparentColor = (this.mNonPrimaryAlpha << 24) | (this.mTextColor & ViewCompat.MEASURED_SIZE_MASK);
        this.mPrevText.setTextColor(transparentColor);
        this.mNextText.setTextColor(transparentColor);
    }

    public void setTextColor(@ColorInt int color) {
        this.mTextColor = color;
        this.mCurrText.setTextColor(color);
        int transparentColor = (this.mNonPrimaryAlpha << 24) | (this.mTextColor & ViewCompat.MEASURED_SIZE_MASK);
        this.mPrevText.setTextColor(transparentColor);
        this.mNextText.setTextColor(transparentColor);
    }

    public void setTextSize(int unit, float size) {
        this.mPrevText.setTextSize(unit, size);
        this.mCurrText.setTextSize(unit, size);
        this.mNextText.setTextSize(unit, size);
    }

    public void setGravity(int gravity) {
        this.mGravity = gravity;
        requestLayout();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent parent = getParent();
        if (parent instanceof ViewPager) {
            ViewPager pager = (ViewPager) parent;
            PagerAdapter adapter = pager.getAdapter();
            pager.setInternalPageChangeListener(this.mPageListener);
            pager.addOnAdapterChangeListener(this.mPageListener);
            this.mPager = pager;
            updateAdapter(this.mWatchingAdapter != null ? (PagerAdapter) this.mWatchingAdapter.get() : null, adapter);
            return;
        }
        throw new IllegalStateException("PagerTitleStrip must be a direct child of a ViewPager.");
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mPager != null) {
            updateAdapter(this.mPager.getAdapter(), null);
            this.mPager.setInternalPageChangeListener(null);
            this.mPager.removeOnAdapterChangeListener(this.mPageListener);
            this.mPager = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateText(int currentItem, PagerAdapter adapter) {
        int itemCount = adapter != null ? adapter.getCount() : 0;
        this.mUpdatingText = true;
        CharSequence text = null;
        if (currentItem >= 1 && adapter != null) {
            text = adapter.getPageTitle(currentItem - 1);
        }
        this.mPrevText.setText(text);
        this.mCurrText.setText((adapter == null || currentItem >= itemCount) ? null : adapter.getPageTitle(currentItem));
        CharSequence text2 = null;
        if (currentItem + 1 < itemCount && adapter != null) {
            text2 = adapter.getPageTitle(currentItem + 1);
        }
        this.mNextText.setText(text2);
        int childWidthSpec = View.MeasureSpec.makeMeasureSpec(Math.max(0, (int) (((float) ((getWidth() - getPaddingLeft()) - getPaddingRight())) * 0.8f)), Integer.MIN_VALUE);
        int childHeightSpec = View.MeasureSpec.makeMeasureSpec(Math.max(0, (getHeight() - getPaddingTop()) - getPaddingBottom()), Integer.MIN_VALUE);
        this.mPrevText.measure(childWidthSpec, childHeightSpec);
        this.mCurrText.measure(childWidthSpec, childHeightSpec);
        this.mNextText.measure(childWidthSpec, childHeightSpec);
        this.mLastKnownCurrentPage = currentItem;
        if (!this.mUpdatingPositions) {
            updateTextPositions(currentItem, this.mLastKnownPositionOffset, false);
        }
        this.mUpdatingText = false;
    }

    public void requestLayout() {
        if (!this.mUpdatingText) {
            super.requestLayout();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateAdapter(PagerAdapter oldAdapter, PagerAdapter newAdapter) {
        if (oldAdapter != null) {
            oldAdapter.unregisterDataSetObserver(this.mPageListener);
            this.mWatchingAdapter = null;
        }
        if (newAdapter != null) {
            newAdapter.registerDataSetObserver(this.mPageListener);
            this.mWatchingAdapter = new WeakReference<>(newAdapter);
        }
        if (this.mPager != null) {
            this.mLastKnownCurrentPage = -1;
            this.mLastKnownPositionOffset = -1.0f;
            updateText(this.mPager.getCurrentItem(), newAdapter);
            requestLayout();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateTextPositions(int position, float positionOffset, boolean force) {
        int stripHeight;
        int prevTop;
        int vgrav;
        int i = position;
        float f = positionOffset;
        if (i != this.mLastKnownCurrentPage) {
            updateText(i, this.mPager.getAdapter());
        } else if (!force && f == this.mLastKnownPositionOffset) {
            return;
        }
        this.mUpdatingPositions = true;
        int prevWidth = this.mPrevText.getMeasuredWidth();
        int currWidth = this.mCurrText.getMeasuredWidth();
        int nextWidth = this.mNextText.getMeasuredWidth();
        int halfCurrWidth = currWidth / 2;
        int stripWidth = getWidth();
        int stripHeight2 = getHeight();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int textPaddedLeft = paddingLeft + halfCurrWidth;
        int textPaddedRight = paddingRight + halfCurrWidth;
        int contentWidth = (stripWidth - textPaddedLeft) - textPaddedRight;
        float currOffset = f + 0.5f;
        if (currOffset > 1.0f) {
            currOffset -= 1.0f;
        }
        int currLeft = ((stripWidth - textPaddedRight) - ((int) (((float) contentWidth) * currOffset))) - (currWidth / 2);
        int i2 = contentWidth;
        int currRight = currLeft + currWidth;
        int i3 = currWidth;
        int currWidth2 = this.mPrevText.getBaseline();
        int i4 = halfCurrWidth;
        int currBaseline = this.mCurrText.getBaseline();
        int i5 = textPaddedLeft;
        int nextBaseline = this.mNextText.getBaseline();
        int i6 = textPaddedRight;
        int maxBaseline = Math.max(Math.max(currWidth2, currBaseline), nextBaseline);
        int prevTopOffset = maxBaseline - currWidth2;
        int currTopOffset = maxBaseline - currBaseline;
        int nextTopOffset = maxBaseline - nextBaseline;
        int i7 = currWidth2;
        int alignedPrevHeight = prevTopOffset + this.mPrevText.getMeasuredHeight();
        int i8 = currBaseline;
        int alignedCurrHeight = currTopOffset + this.mCurrText.getMeasuredHeight();
        int i9 = nextBaseline;
        int i10 = maxBaseline;
        int maxTextHeight = Math.max(Math.max(alignedPrevHeight, alignedCurrHeight), nextTopOffset + this.mNextText.getMeasuredHeight());
        int i11 = alignedPrevHeight;
        int vgrav2 = this.mGravity & 112;
        int i12 = alignedCurrHeight;
        if (vgrav2 == 16) {
            int centeredTop = (((stripHeight2 - paddingTop) - paddingBottom) - maxTextHeight) / 2;
            int i13 = vgrav2;
            int i14 = stripHeight2;
            vgrav = centeredTop + prevTopOffset;
            prevTop = centeredTop + currTopOffset;
            stripHeight = centeredTop + nextTopOffset;
        } else if (vgrav2 != 80) {
            int i15 = vgrav2;
            vgrav = paddingTop + prevTopOffset;
            int i16 = stripHeight2;
            prevTop = paddingTop + currTopOffset;
            stripHeight = paddingTop + nextTopOffset;
        } else {
            int bottomGravTop = (stripHeight2 - paddingBottom) - maxTextHeight;
            int i17 = vgrav2;
            int i18 = stripHeight2;
            vgrav = bottomGravTop + prevTopOffset;
            stripHeight = bottomGravTop + nextTopOffset;
            prevTop = bottomGravTop + currTopOffset;
        }
        int i19 = paddingTop;
        int i20 = paddingBottom;
        this.mCurrText.layout(currLeft, prevTop, currRight, this.mCurrText.getMeasuredHeight() + prevTop);
        int prevLeft = Math.min(paddingLeft, (currLeft - this.mScaledTextSpacing) - prevWidth);
        int i21 = currLeft;
        int i22 = prevWidth;
        this.mPrevText.layout(prevLeft, vgrav, prevLeft + prevWidth, this.mPrevText.getMeasuredHeight() + vgrav);
        int nextLeft = Math.max((stripWidth - paddingRight) - nextWidth, this.mScaledTextSpacing + currRight);
        int i23 = currRight;
        this.mNextText.layout(nextLeft, stripHeight, nextLeft + nextWidth, this.mNextText.getMeasuredHeight() + stripHeight);
        this.mLastKnownPositionOffset = f;
        this.mUpdatingPositions = false;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int textHeight;
        if (View.MeasureSpec.getMode(widthMeasureSpec) == 1073741824) {
            int heightPadding = getPaddingTop() + getPaddingBottom();
            int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, heightPadding, -2);
            int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
            int childWidthSpec = getChildMeasureSpec(widthMeasureSpec, (int) (((float) widthSize) * 0.2f), -2);
            this.mPrevText.measure(childWidthSpec, childHeightSpec);
            this.mCurrText.measure(childWidthSpec, childHeightSpec);
            this.mNextText.measure(childWidthSpec, childHeightSpec);
            if (View.MeasureSpec.getMode(heightMeasureSpec) == 1073741824) {
                textHeight = View.MeasureSpec.getSize(heightMeasureSpec);
            } else {
                textHeight = Math.max(getMinHeight(), this.mCurrText.getMeasuredHeight() + heightPadding);
            }
            setMeasuredDimension(widthSize, View.resolveSizeAndState(textHeight, heightMeasureSpec, this.mCurrText.getMeasuredState() << 16));
            return;
        }
        throw new IllegalStateException("Must measure with an exact width");
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.mPager != null) {
            float offset = 0.0f;
            if (this.mLastKnownPositionOffset >= 0.0f) {
                offset = this.mLastKnownPositionOffset;
            }
            updateTextPositions(this.mLastKnownCurrentPage, offset, true);
        }
    }

    /* access modifiers changed from: package-private */
    public int getMinHeight() {
        Drawable bg = getBackground();
        if (bg != null) {
            return bg.getIntrinsicHeight();
        }
        return 0;
    }
}
