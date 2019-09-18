package huawei.com.android.internal.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.android.internal.widget.ActionBarContainer;

public class HwActionBarContainer extends ActionBarContainer {
    public HwActionBarContainer(Context context) {
        this(context, null);
    }

    public HwActionBarContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Drawable getBackgroundDrawable() {
        return null;
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        HwActionBarContainer.super.onLayout(changed, l, t, r, b);
    }

    public void setDisplayNoSplitLine(boolean displayNoSplitLine) {
    }

    public void setSplitViewLocation(int start, int end) {
    }
}
