package huawei.android.widget;

import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView.BufferType;

@RemoteView
public class Button extends android.widget.Button implements OnGlobalLayoutListener {
    private float mOriginTextSize;

    public Button(Context context) {
        this(context, null);
    }

    public Button(Context context, AttributeSet attrs) {
        this(context, attrs, 16842824);
    }

    public Button(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Button(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mOriginTextSize = getTextSize();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    public void onGlobalLayout() {
        HwWidgetFactory.autoTextSize(this, getContext(), this.mOriginTextSize);
    }

    public void setTextSize(int unit, float size) {
        this.mOriginTextSize = TypedValue.applyDimension(unit, size, getResources().getDisplayMetrics());
        super.setTextSize(unit, size);
    }

    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        requestLayout();
        invalidate();
    }
}
