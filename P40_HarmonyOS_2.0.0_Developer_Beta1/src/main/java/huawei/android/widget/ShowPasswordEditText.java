package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class ShowPasswordEditText extends EditText {
    private static final String TAG = "ShowPasswordEditText";
    private boolean mIsPasswordVisible;

    public ShowPasswordEditText(Context context) {
        this(context, null);
    }

    public ShowPasswordEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mIsPasswordVisible = false;
        init(context);
    }

    public ShowPasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    private void init(Context context) {
        setImeOptions(268435456);
        if (getInputType() != 145) {
            setInputType(129);
        }
        setInputTypeState(getInputType());
        setCompoundDrawablePadding(context.getResources().getDimensionPixelSize(34472884));
    }

    @Override // huawei.android.widget.EditText, android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        boolean z = true;
        if (event == null) {
            Log.d(TAG, "onTouchEvent,event is null");
            return true;
        }
        if (event.getActionMasked() == 1) {
            float eventX = event.getX();
            int paddingStart = getTotalPaddingStart();
            int hotLength = getMinimumHeight();
            int width = getWidth();
            boolean isRtl = isRtlLocale();
            boolean isValid = isRtl && eventX > ((float) paddingStart) && eventX < ((float) (paddingStart + hotLength));
            if (isRtl || eventX <= ((float) ((width - paddingStart) - hotLength)) || eventX >= ((float) (width - paddingStart))) {
                z = false;
            }
            if (z || isValid) {
                showOrHide();
                event.setAction(3);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override // android.widget.TextView
    public void setInputType(int type) {
        super.setInputType(type);
        setInputTypeState(type);
    }

    private void setInputTypeState(int type) {
        this.mIsPasswordVisible = type == 145;
        setEyeIcon(this.mIsPasswordVisible);
    }

    private void setEyeIcon(boolean isVisible) {
        int drawableId;
        if (isVisible) {
            drawableId = 33751131;
        } else {
            drawableId = 33751130;
        }
        int padding = getResources().getDimensionPixelSize(34472219);
        if (isRtlLocale()) {
            if (getPaddingEnd() == 0) {
                setPadding(padding, 0, 0, 0);
            }
            setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0);
            return;
        }
        if (getPaddingEnd() == 0) {
            setPadding(0, 0, padding, 0);
        }
        setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableId, 0);
    }

    private void showOrHide() {
        int inputType;
        if (this.mIsPasswordVisible) {
            inputType = 128;
        } else {
            inputType = 144;
        }
        setInputType(inputType | 1);
        setSelection(getText().toString().length());
    }
}
