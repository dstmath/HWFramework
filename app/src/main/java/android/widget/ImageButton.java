package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;

@RemoteView
public class ImageButton extends ImageView {
    public ImageButton(Context context) {
        this(context, null);
    }

    public ImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.imageButtonStyle);
    }

    public ImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ImageButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setFocusable(true);
    }

    protected boolean onSetAlpha(int alpha) {
        return false;
    }

    public CharSequence getAccessibilityClassName() {
        return ImageButton.class.getName();
    }
}
