package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;

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
        setCompoundDrawablePadding(context.getResources().getDimensionPixelSize(34472754));
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean z = true;
        if (event == null) {
            Log.d(TAG, "onTouchEvent,event is null");
            return true;
        }
        if (event.getActionMasked() == 1) {
            float x = event.getX();
            int paddingStart = getTotalPaddingStart();
            int hotLength = getMinimumHeight();
            int width = getWidth();
            boolean isRtl = isRtlLocale();
            boolean isValid = isRtl && x > ((float) paddingStart) && x < ((float) (paddingStart + hotLength));
            if (isRtl || x <= ((float) ((width - paddingStart) - hotLength)) || x >= ((float) (width - paddingStart))) {
                z = false;
            }
            if (z || isValid) {
                showOrHide();
                event.setAction(3);
            }
        }
        return super.onTouchEvent(event);
    }

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
        if (isRtlLocale()) {
            setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableId, 0);
        }
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
