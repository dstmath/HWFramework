package huawei.android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.com.android.internal.widget.HwWidgetUtils;

public class TabIndicator extends LinearLayout {
    private static final float FIRST_MEASURE_TEXT_SIZE = 18.0f;
    private static final float HUGE_FONT_SCALE = 1.15f;
    private static final float SECOND_MEASURE_TEXT_SIZE = 15.0f;
    private static final int TEXT_MAX_LINE = 2;
    private static final float THIRD_MEASURE_TEXT_SIZE = 12.0f;
    private static final String TYPE_FACE_HWCHINESE_MEDIUM = "HwChinese-medium";
    private static final String TYPE_FACE_SANS_SERIF = "sans-serif";
    private static final String TYPE_FACE_SANS_SERIF_CONDENSED_REGULAR = "sans-serif-condensed-regular";
    private int mHeight;
    private ImageView mImageView;
    private Typeface mMedium;
    private Typeface mMediumCondensed;
    private Typeface mRegular;
    private Typeface mRegularCondensed;
    private TextView mTextView;

    public TabIndicator(Context context) {
        super(context);
    }

    public TabIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mRegular = Typeface.create(TYPE_FACE_SANS_SERIF, 0);
        this.mMedium = Typeface.create(TYPE_FACE_HWCHINESE_MEDIUM, 0);
        this.mRegularCondensed = Typeface.create(TYPE_FACE_SANS_SERIF_CONDENSED_REGULAR, 0);
        this.mMediumCondensed = Typeface.create(TYPE_FACE_SANS_SERIF_CONDENSED_REGULAR, 0);
    }

    public void initTabIndicator() {
        this.mTextView = (TextView) findViewById(16908310);
        this.mImageView = (ImageView) findViewById(16908294);
        if (this.mTextView != null) {
            HwWidgetFactory.setImmersionStyle(getContext(), this.mTextView, 33882441, 33882440, 0, false);
        }
        if (HwWidgetUtils.isActionbarBackgroundThemed(this.mContext)) {
            setBackgroundResource(33751382);
        } else {
            setBackgroundResource(HwWidgetFactory.getImmersionResource(this.mContext, 33751383, 0, 33751387, false));
        }
        initHeight();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        TextView textView = this.mTextView;
        boolean isSelected = isSelected();
        if (textView != null) {
            preMesureTextFirstTime(textView, isSelected);
            textView.measure(0, heightMeasureSpec);
            ImageView imageView = this.mImageView;
            int iconSize = imageView == null ? 0 : imageView.getMeasuredWidth();
            int tabPadding = getPaddingStart() + getPaddingEnd();
            int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
            int requiredWidth = textView.getMeasuredWidth() + iconSize + tabPadding;
            if (requiredWidth > widthSize) {
                if (isSelected) {
                    textView.setTypeface(this.mMediumCondensed);
                } else {
                    textView.setTypeface(this.mRegularCondensed);
                }
                textView.measure(0, heightMeasureSpec);
                requiredWidth = textView.getMeasuredWidth() + iconSize + tabPadding;
            }
            if (requiredWidth > widthSize) {
                textView.setTextSize(2, SECOND_MEASURE_TEXT_SIZE);
                textView.measure(0, heightMeasureSpec);
                if (textView.getMeasuredWidth() + iconSize + tabPadding > widthSize) {
                    measureTextViewThirdTime(textView);
                }
            }
        }
        int i = this.mHeight;
        super.onMeasure(widthMeasureSpec, i >= 0 ? View.MeasureSpec.makeMeasureSpec(i, 1073741824) : heightMeasureSpec);
    }

    private void preMesureTextFirstTime(TextView textView, boolean isSelected) {
        textView.setSingleLine(true);
        textView.setMaxLines(1);
        textView.setTextSize(2, FIRST_MEASURE_TEXT_SIZE);
        if (isSelected) {
            textView.setTypeface(this.mMedium);
        } else {
            textView.setTypeface(this.mRegular);
        }
    }

    private void measureTextViewThirdTime(TextView textView) {
        textView.setSingleLine(false);
        if (getContext().getResources().getConfiguration().fontScale > HUGE_FONT_SCALE) {
            textView.setTextSize(2, THIRD_MEASURE_TEXT_SIZE);
        }
        textView.setMaxLines(2);
    }

    private void initHeight() {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(new int[]{16843499});
        this.mHeight = typedArray.getDimensionPixelSize(0, 0);
        typedArray.recycle();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initHeight();
    }

    @Override // android.view.View
    public void setSelected(boolean isSelected) {
        super.setSelected(isSelected);
        TextView textView = this.mTextView;
        if (textView != null) {
            textView.setSelected(isSelected);
        }
        requestLayout();
    }
}
