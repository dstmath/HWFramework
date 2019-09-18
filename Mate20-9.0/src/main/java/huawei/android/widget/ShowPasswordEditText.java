package huawei.android.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;

public class ShowPasswordEditText extends EditText {
    private static final String TAG = "ShowPasswordEditText";
    private Drawable[] mCompoundDrawables;
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
        this.mCompoundDrawables = getCompoundDrawables();
        setCompoundDrawablePadding(context.getResources().getDimensionPixelSize(34472517));
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            Log.d(TAG, "onTouchEvent,event is null");
            return true;
        }
        if (event.getAction() == 1 && this.mCompoundDrawables != null) {
            if (this.mCompoundDrawables[0] != null && event.getX() <= ((float) (this.mCompoundDrawables[0].getBounds().width() + getPaddingLeft()))) {
                showOrHide();
                event.setAction(3);
            }
            if (this.mCompoundDrawables[2] != null && event.getX() > ((float) ((getWidth() - this.mCompoundDrawables[2].getBounds().width()) - getPaddingRight()))) {
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

    private void setEyeIcon(boolean visible) {
        int drawableID = visible ? 33751131 : 33751130;
        if (isRtlLocale()) {
            setCompoundDrawablesWithIntrinsicBounds(drawableID, 0, 0, 0);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableID, 0);
        }
    }

    private void showOrHide() {
        setInputType(1 | (this.mIsPasswordVisible ? 128 : 144));
        setSelection(getText().toString().length());
    }
}
