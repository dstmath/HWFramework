package huawei.com.android.internal.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.android.internal.R;

public class HwCustomPanel extends LinearLayout {
    private static final boolean DB = false;
    private static final String TAG = "HwCustomPanel";
    private Drawable mBackground;
    int mSlideY = 0;

    public HwCustomPanel(Context context) {
        super(context);
    }

    public HwCustomPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(1);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionBar);
        if (HwWidgetUtils.isActionbarBackgroundThemed(context)) {
            this.mBackground = null;
        } else {
            int color = HwWidgetFactory.getPrimaryColor(context);
            if (Color.alpha(color) != 0) {
                this.mBackground = new ColorDrawable(color);
            } else {
                this.mBackground = a.getDrawable(2);
            }
        }
        setBackgroundDrawable(this.mBackground);
        a.recycle();
    }

    public void draw(Canvas canvas) {
        canvas.clipRect(0, 0, getWidth(), -this.mSlideY);
        super.draw(canvas);
    }

    public void setClipY(int slideY) {
        this.mSlideY = slideY;
        invalidate();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(new int[]{16843499});
        int actionBarHeight = ta.getDimensionPixelSize(0, 0);
        setPadding(0, 0, 0, actionBarHeight);
        setClipY(-actionBarHeight);
        ta.recycle();
    }
}
