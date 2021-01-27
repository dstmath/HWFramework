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
    private static final int PIN_COUNT = 4;
    private static final int SIZE_FACTOR = 2;
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
        this.mDivideWidth = res.getDimensionPixelSize(34472917);
        setBackground(null);
        setRawInputType(128);
        setPadding(0, 0, 0, 0);
        setCursorVisible(false);
        this.mLeftBg = res.getDrawable(33752523);
        this.mMiddleBg = res.getDrawable(33752524);
        this.mRightBg = res.getDrawable(33752525);
        this.mPinSize = res.getDimensionPixelSize(34472918);
        this.mPinPaint = new Paint(1);
        this.mPinPaint.setStrokeWidth((float) this.mPinSize);
        this.mPinPaint.setStyle(Paint.Style.FILL);
        this.mPinColor = res.getColor(33882334);
        this.mPinPaint.setColor(this.mPinColor);
        setMinHeight(this.mLeftBg.getIntrinsicHeight());
        setInputFilter();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onDraw(Canvas canvas) {
        int width = getWidth();
        int i = this.mDivideWidth;
        int i2 = this.mPinCount;
        int width2 = (width - (i * (i2 - 1))) / i2;
        int sum = 0;
        int height = getHeight();
        int i3 = 0;
        while (true) {
            int i4 = this.mPinCount;
            if (i3 >= i4) {
                break;
            }
            if (i3 == 0) {
                this.mLeftBg.setBounds(0, 0, width2, height);
                this.mLeftBg.draw(canvas);
            } else if (i3 == i4 - 1) {
                this.mRightBg.setBounds(sum, 0, sum + width2, height);
                this.mRightBg.draw(canvas);
            } else {
                this.mMiddleBg.setBounds(sum, 0, sum + width2, height);
                this.mMiddleBg.draw(canvas);
            }
            sum = sum + width2 + this.mDivideWidth;
            i3++;
        }
        this.mInterval = (float) (getWidth() / (this.mPinCount * 2));
        this.mCircleX = this.mInterval;
        if (isRtlLocale()) {
            float f = this.mInterval;
            this.mCircleX = ((float) getWidth()) - f;
            this.mInterval = -f;
        }
        int i5 = 0;
        while (i5 < this.mTextLength && i5 < this.mPinCount) {
            canvas.drawCircle(this.mCircleX, (float) (height / 2), (float) this.mPinSize, this.mPinPaint);
            this.mCircleX += this.mInterval * 2.0f;
            i5++;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView
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
        int i = this.mPinCount;
        if (i > 8) {
            this.mPinCount = 8;
        } else if (i < 4) {
            this.mPinCount = 4;
        }
    }

    private void setInputFilter() {
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(this.mPinCount)});
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView
    public void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        setSelection(getText().length());
    }
}
