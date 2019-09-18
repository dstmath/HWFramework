package huawei.com.android.internal.widget;

import android.widget.Toolbar;
import com.android.internal.widget.ToolbarWidgetWrapper;
import huawei.android.widget.HwToolbar;
import java.lang.annotation.RCUnownedRef;

public class HwToolbarWidgetWrapper extends ToolbarWidgetWrapper {
    @RCUnownedRef
    private HwToolbar mToolbar;

    public HwToolbarWidgetWrapper(Toolbar toolbar, boolean style) {
        super(toolbar, style);
        init(toolbar);
    }

    public HwToolbarWidgetWrapper(Toolbar toolbar, boolean style, int defaultNavigationContentDescription) {
        super(toolbar, style, defaultNavigationContentDescription);
        init(toolbar);
    }

    public boolean canSplit() {
        return true;
    }

    public boolean isSplit() {
        if (this.mToolbar != null) {
            return this.mToolbar.getSplitStatus();
        }
        return false;
    }

    public void setSplitToolbar(boolean split) {
    }

    private void init(Toolbar toolbar) {
        if (toolbar instanceof HwToolbar) {
            this.mToolbar = (HwToolbar) toolbar;
        }
    }
}
