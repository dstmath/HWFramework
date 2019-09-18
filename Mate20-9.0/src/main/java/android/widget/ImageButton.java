package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.widget.RemoteViews;

@RemoteViews.RemoteView
public class ImageButton extends ImageView {
    public ImageButton(Context context) {
        this(context, null);
    }

    public ImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, 16842866);
    }

    public ImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ImageButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setFocusable(true);
    }

    /* access modifiers changed from: protected */
    public boolean onSetAlpha(int alpha) {
        return false;
    }

    public CharSequence getAccessibilityClassName() {
        return ImageButton.class.getName();
    }

    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        if (getPointerIcon() != null || !isClickable() || !isEnabled()) {
            return super.onResolvePointerIcon(event, pointerIndex);
        }
        return PointerIcon.getSystemIcon(getContext(), 1002);
    }
}
