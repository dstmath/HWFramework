package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class TabHost extends android.widget.TabHost {
    public TabHost(Context context) {
        super(context);
    }

    public TabHost(Context context, AttributeSet attrs) {
        this(context, attrs, 16842883);
    }

    public TabHost(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TabHost(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void addHwTab(View tabIndicator) {
        if (tabIndicator instanceof TabIndicator) {
            ((TabIndicator) tabIndicator).initTabIndicator();
        }
    }
}
