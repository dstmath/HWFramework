package huawei.android.widget;

import android.content.Context;
import android.widget.Scroller;

public interface HwViewPager {
    boolean canScrollEdge();

    Scroller createScroller(Context context);

    float scrollEdgeBound(boolean z, float f, float f2, float f3);

    void tabScrollerFollowed(int i, float f);
}
