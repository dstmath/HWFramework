package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.EditText;
import androidhwext.R;

public class PinTextField extends EditText {
    private static final int MAX_PIN_COUNT = 8;
    private static final int MIN_PIN_COUNT = 4;
    private float mCircleX;
    private int mDivideWidth;
    private float mInterval;
    private Drawable mLeftBg;
    private Drawable mMiddleBg;
    private int mPinColor;
    private int mPinCount;
    private Paint mPinPaint;
    private int mPinSize;
    private Drawable mRightBg;
    private int mTextLength;

    public PinTextField(Context context) {
        this(context, null);
    }

    public PinTextField(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mPinCount = 4;
        this.mTextLength = 0;
        init(context, attrs, 0);
    }

    public PinTextField(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public PinTextField(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setImeOptions(268435456);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.PinTextField, defStyleAttr, 0);
        this.mPinCount = attributes.getInteger(0, 4);
        attributes.recycle();
        judgePinCount();
        Resources res = context.getResources();
        this.mDivideWidth = res.getDimensionPixelSize(34472542);
        setBackground(null);
        setRawInputType(128);
        setPadding(0, 0, 0, 0);
        setCursorVisible(false);
        this.mLeftBg = res.getDrawable(33752106);
        this.mMiddleBg = res.getDrawable(33752107);
        this.mRightBg = res.getDrawable(33752108);
        this.mPinSize = res.getDimensionPixelSize(34472543);
        this.mPinPaint = new Paint(1);
        this.mPinPaint.setStrokeWidth((float) this.mPinSize);
        this.mPinPaint.setStyle(Paint.Style.FILL);
        this.mPinColor = res.getColor(33882334);
        this.mPinPaint.setColor(this.mPinColor);
        setMinHeight(this.mLeftBg.getIntrinsicHeight());
        setInputFilter();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        int width = (getWidth() - (this.mDivideWidth * (this.mPinCount - 1))) / this.mPinCount;
        int height = getHeight();
        int i = 0;
        int sum = 0;
        for (int i2 = 0; i2 < this.mPinCount; i2++) {
            if (i2 == 0) {
                this.mLeftBg.setBounds(0, 0, width, height);
                this.mLeftBg.draw(canvas);
            } else if (i2 == this.mPinCount - 1) {
                this.mRightBg.setBounds(sum, 0, sum + width, height);
                this.mRightBg.draw(canvas);
            } else {
                this.mMiddleBg.setBounds(sum, 0, sum + width, height);
                this.mMiddleBg.draw(canvas);
            }
            sum += this.mDivideWidth + width;
        }
        this.mInterval = (float) (getWidth() / (this.mPinCount * 2));
        this.mCircleX = this.mInterval;
        if (isRtlLocale()) {
            this.mCircleX = ((float) getWidth()) - this.mInterval;
            this.mInterval = -this.mInterval;
        }
        while (true) {
            int i3 = i;
            if (i3 < this.mTextLength && i3 < this.mPinCount) {
                canvas.drawCircle(this.mCircleX, (float) (height / 2), (float) this.mPinSize, this.mPinPaint);
                this.mCircleX += this.mInterval * 2.0f;
                i = i3 + 1;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        this.mTextLength = text.length();
    }

    public void setPinCount(int pinCount) {
        this.mPinCount = pinCount;
        judgePinCount();
        setInputFilter();
        invalidate();
    }

    private void judgePinCount() {
        if (this.mPinCount > 8) {
            this.mPinCount = 8;
        } else if (this.mPinCount < 4) {
            this.mPinCount = 4;
        }
    }

    private void setInputFilter() {
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(this.mPinCount)});
    }

    /* access modifiers changed from: protected */
    public void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        setSelection(getText().length());
    }
}
