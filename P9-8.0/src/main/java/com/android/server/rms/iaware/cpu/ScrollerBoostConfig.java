package com.android.server.rms.iaware.cpu;

import android.util.ArrayMap;
import java.util.Map;

/* compiled from: CPUXmlConfiguration */
class ScrollerBoostConfig extends CPUCustBaseConfig {
    private static final String CONFIG_SCROLLERBOOST = "scroller_boost";
    private static final String TAG = "ScrollerBoostConfig";
    private Map<String, CPUPropInfoItem> mScrollerInfoMap = new ArrayMap();
    private Map<String, String> mScrollerItem2PropMap = new ArrayMap();

    public ScrollerBoostConfig() {
        init();
    }

    public void setConfig(CPUFeature feature) {
        applyConfig(this.mScrollerInfoMap);
    }

    private void init() {
        this.mScrollerItem2PropMap.put("boost_duration", "persist.sys.boost.durationms");
        this.mScrollerItem2PropMap.put("enable_skipped_frame", "persist.sys.boost.skipframe");
        this.mScrollerItem2PropMap.put("boost_by_each_fling", "persist.sys.boost.byeachfling");
        obtainConfigInfo(CONFIG_SCROLLERBOOST, this.mScrollerItem2PropMap, this.mScrollerInfoMap);
    }
}
