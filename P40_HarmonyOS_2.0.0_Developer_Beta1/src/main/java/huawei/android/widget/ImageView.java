package huawei.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RemoteViews;
import androidhwext.R;
import huawei.android.graphics.drawable.HwAnimatedGradientDrawable;
import huawei.android.widget.plume.HwPlumeManager;

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
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HwWidgetClickEffect);
        if (typedArray.getBoolean(0, false)) {
            setBackground(new HwAnimatedGradientDrawable(context));
        }
        typedArray.recycle();
        setValueFromPlume();
    }

    private void setValueFromPlume() {
        if (HwPlumeManager.isPlumeUsed(this.mContext)) {
            setOnZoomEnabled(HwPlumeManager.getInstance(this.mContext).getDefault(this, "zoomEnabled", false));
        }
    }
}
