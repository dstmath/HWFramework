package huawei.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RemoteViews;
import androidhwext.R;
import huawei.android.graphics.drawable.HwAnimatedGradientDrawable;

@RemoteViews.RemoteView
public class ImageView extends android.widget.ImageView {
    public ImageView(Context context) {
        this(context, null);
    }

    public ImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HwWidgetClickEffect);
        if (a.getBoolean(0, false)) {
            setBackground(new HwAnimatedGradientDrawable(context));
        }
        a.recycle();
    }
}
