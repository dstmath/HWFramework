package huawei.com.android.internal.widget;

import android.widget.Toolbar;
import com.android.internal.widget.ToolbarWidgetWrapper;
import huawei.android.widget.HwToolbar;
import java.lang.annotation.RCUnownedRef;

public class HwToolbarWidgetWrapper extends ToolbarWidgetWrapper {
    @RCUnownedRef
    private HwToolbar mToolbar;

    public HwToolbarWidgetWrapper(Toolbar toolbar, boolean isDefaultStyle) {
        super(toolbar, isDefaultStyle);
        init(toolbar);
    }

    public HwToolbarWidgetWrapper(Toolbar toolbar, boolean isDefaultStyle, int defaultNavigationContentDescription) {
        super(toolbar, isDefaultStyle, defaultNavigationContentDescription);
        init(toolbar);
    }

    public boolean canSplit() {
        return true;
    }

    public boolean isSplit() {
        HwToolbar hwToolbar = this.mToolbar;
        if (hwToolbar != null) {
            return hwToolbar.getSplitStatus();
        }
        return false;
    }

    public void setSplitToolbar(boolean shouldSplit) {
    }

    private void init(Toolbar toolbar) {
        if (toolbar instanceof HwToolbar) {
            this.mToolbar = (HwToolbar) toolbar;
        }
    }
}
