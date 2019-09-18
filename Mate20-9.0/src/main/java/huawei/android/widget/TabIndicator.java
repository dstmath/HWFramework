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
import com.huawei.hwsqlite.SQLiteDatabase;
import huawei.com.android.internal.widget.HwWidgetUtils;

public class TabIndicator extends LinearLayout {
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
        this.mRegular = Typeface.create("sans-serif", 0);
        this.mMedium = Typeface.create("HwChinese-medium", 0);
        this.mRegularCondensed = Typeface.create("sans-serif-condensed-regular", 0);
        this.mMediumCondensed = Typeface.create("sans-serif-condensed-regular", 0);
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
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureSpec2 = heightMeasureSpec;
        TextView tv = this.mTextView;
        ImageView iv = this.mImageView;
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int tabPadding = getPaddingStart() + getPaddingEnd();
        boolean isSelected = isSelected();
        if (tv != null) {
            tv.setSingleLine(true);
            tv.setMaxLines(1);
            int iconSize = iv == null ? 0 : iv.getMeasuredWidth();
            tv.setTextSize(2, 18.0f);
            if (isSelected) {
                tv.setTypeface(this.mMedium);
            } else {
                tv.setTypeface(this.mRegular);
            }
            tv.measure(0, heightMeasureSpec2);
            int requiredWidth = tv.getMeasuredWidth() + iconSize + tabPadding;
            if (requiredWidth > widthSize) {
                if (isSelected) {
                    tv.setTypeface(this.mMediumCondensed);
                } else {
                    tv.setTypeface(this.mRegularCondensed);
                }
                tv.measure(0, heightMeasureSpec2);
                requiredWidth = tv.getMeasuredWidth() + iconSize + tabPadding;
            }
            if (requiredWidth > widthSize) {
                tv.setTextSize(2, 15.0f);
                tv.measure(0, heightMeasureSpec2);
                if (tv.getMeasuredWidth() + iconSize + tabPadding > widthSize) {
                    tv.setSingleLine(false);
                    if (((double) getContext().getResources().getConfiguration().fontScale) > 1.15d) {
                        tv.setTextSize(2, 12.0f);
                    }
                    tv.setMaxLines(2);
                }
            }
        }
        if (this.mHeight >= 0) {
            heightMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mHeight, SQLiteDatabase.ENABLE_DATABASE_ENCRYPTION);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec2);
    }

    private void initHeight() {
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(new int[]{16843499});
        this.mHeight = ta.getDimensionPixelSize(0, 0);
        ta.recycle();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initHeight();
    }

    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (this.mTextView != null) {
            this.mTextView.setSelected(selected);
        }
        requestLayout();
    }
}
