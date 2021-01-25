package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import androidhwext.R;
import huawei.android.graphics.drawable.HwAnimatedGradientDrawable;

@RemoteViews.RemoteView
public class HwTextView extends TextView {
    private static final int CHANGE_SIZE_FIRST = 0;
    private static final int WRAP_FIRST = 1;
    private boolean mIsWrapFirst;
    private float mMinSize;
    private float mSizeStep;
    private StaticLayout mStaticLayout;
    private TextPaint mTextPaint;
    private float mTextSize;

    public HwTextView(Context context) {
        this(context, null);
    }

    public HwTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842884);
    }

    public HwTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HwTextView);
        boolean z = false;
        boolean isClickEffect = typedArray.getBoolean(0, false);
        this.mIsWrapFirst = typedArray.getInt(1, 0) == 1 ? true : z;
        if (isClickEffect) {
            setBackground(new HwAnimatedGradientDrawable(context));
        }
        typedArray.recycle();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        autoText(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec), View.MeasureSpec.getMode(heightMeasureSpec));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override // android.widget.TextView
    public void setText(CharSequence text, TextView.BufferType type) {
        super.setText(text, type);
        if (this.mMinSize > 0.0f && this.mSizeStep > 0.0f) {
            requestLayout();
        }
    }

    private void initialise() {
        this.mTextPaint = new TextPaint();
        this.mTextPaint.set(getPaint());
        this.mTextSize = getTextSize();
        this.mMinSize = (float) getAutoSizeMinTextSize();
        this.mSizeStep = (float) getAutoSizeStepGranularity();
        setAutoSizeTextTypeWithDefaults(0);
    }

    public void setAutoTextInfo(int autoSizeMinTextSize, int autoSizeStepGranularity, int unit) {
        Resources res;
        Context ctx = getContext();
        if (ctx == null) {
            res = Resources.getSystem();
        } else {
            res = ctx.getResources();
        }
        this.mMinSize = TypedValue.applyDimension(unit, (float) autoSizeMinTextSize, res.getDisplayMetrics());
        this.mSizeStep = TypedValue.applyDimension(unit, (float) autoSizeStepGranularity, res.getDisplayMetrics());
    }

    private void autoText(int parentWidth, int parentHeight, int heightMode) {
        int width = parentWidth;
        int height = parentHeight;
        int maxWidth = getMaxWidth();
        int maxHeight = getMaxHeight();
        if (maxWidth != -1 && maxWidth < parentWidth) {
            width = maxWidth;
        }
        if (maxHeight != -1 && maxHeight < parentHeight) {
            height = maxHeight;
        }
        int viewWidth = (width - getTotalPaddingLeft()) - getTotalPaddingRight();
        if (viewWidth >= 0) {
            if (this.mTextPaint == null) {
                this.mTextPaint = new TextPaint();
                this.mTextPaint.set(getPaint());
            }
            adjustTextSize(heightMode, width, height, viewWidth);
        }
    }

    private void adjustTextSize(int heightMode, int width, int height, int viewWidth) {
        if ((!this.mIsWrapFirst || getMaxLines() != Integer.MAX_VALUE) && this.mMinSize > 0.0f && this.mSizeStep > 0.0f) {
            float currentSize = this.mTextSize;
            CharSequence text = getText();
            TransformationMethod transformationMethod = getTransformationMethod();
            if (transformationMethod != null) {
                text = transformationMethod.getTransformation(text, this);
            }
            this.mTextPaint.setTextSize(currentSize);
            float textWidth = this.mTextPaint.measureText(text.toString());
            while (needAutoSize(textWidth, viewWidth, currentSize)) {
                currentSize -= this.mSizeStep;
                this.mTextPaint.setTextSize(currentSize);
                textWidth = this.mTextPaint.measureText(text.toString());
            }
            if (currentSize < this.mMinSize) {
                currentSize = this.mMinSize;
            }
            setTextSize(0, currentSize);
            if (heightMode != 0) {
                measureHeight(height, width);
            }
        }
    }

    private boolean needAutoSize(float textWidth, int viewWidth, float currentSize) {
        return (!this.mIsWrapFirst || getMaxLines() == 1) ? textWidth > ((float) viewWidth) && currentSize > this.mMinSize : getCurLineCount(viewWidth) > getMaxLines() && currentSize > this.mMinSize;
    }

    private int getCurLineCount(int viewWidth) {
        CharSequence text = getText();
        if (text == null) {
            return 0;
        }
        return StaticLayout.Builder.obtain(text, 0, text.length(), this.mTextPaint, viewWidth).build().getLineCount();
    }

    private void measureHeight(int parentHeight, int parentWidth) {
        int maxLines = getMaxLines();
        if (maxLines > 1) {
            int viewWidth = (parentWidth - getTotalPaddingLeft()) - getTotalPaddingRight();
            int availedHeight = (parentHeight - getExtendedPaddingBottom()) - getExtendedPaddingTop();
            if (availedHeight > 0) {
                this.mStaticLayout = new StaticLayout(getText(), getPaint(), viewWidth, Layout.Alignment.ALIGN_NORMAL, getLineSpacingMultiplier(), getLineSpacingExtra(), false);
                int lineCount = this.mStaticLayout.getLineCount();
                if (this.mStaticLayout.getHeight() > availedHeight && lineCount > 1 && lineCount <= maxLines + 1) {
                    setMaxLines(lineCount - 1);
                }
            }
        }
    }

    public void setAutoTextSize(float size) {
        Resources res;
        Context ctx = getContext();
        if (ctx == null) {
            res = Resources.getSystem();
        } else {
            res = ctx.getResources();
        }
        this.mTextSize = TypedValue.applyDimension(2, size, res.getDisplayMetrics());
        super.setTextSize(size);
    }

    public void setAutoTextSize(int unit, float size) {
        Resources res;
        Context ctx = getContext();
        if (ctx == null) {
            res = Resources.getSystem();
        } else {
            res = ctx.getResources();
        }
        this.mTextSize = TypedValue.applyDimension(unit, size, res.getDisplayMetrics());
        super.setTextSize(unit, size);
    }
}
