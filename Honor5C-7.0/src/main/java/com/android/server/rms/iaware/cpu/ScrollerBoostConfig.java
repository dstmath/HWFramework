package com.android.server.rms.iaware.cpu;

import java.util.HashMap;
import java.util.Map;

/* compiled from: CPUXmlConfiguration */
class ScrollerBoostConfig extends CPUCustBaseConfig {
    private static final String CONFIG_SCROLLERBOOST = "scroller_boost";
    private static final String TAG = "ScrollerBoostConfig";
    private Map<String, CPUPropInfoItem> mScrollerInfoMap;
    private Map<String, String> mScrollerItem2PropMap;

    public ScrollerBoostConfig() {
        this.mScrollerItem2PropMap = new HashMap();
        this.mScrollerInfoMap = new HashMap();
        init();
    }

    public void setConfig(CPUFeature feature) {
        applyConfig(this.mScrollerInfoMap);
    }

    private void init() {
        this.mScrollerItem2PropMap.put("boost_duration", "persist.sys.boost.durationms");
        this.mScrollerItem2PropMap.put("min_freq", "persist.sys.boost.freqmin.b");
        this.mScrollerItem2PropMap.put("ipa_power", "persist.sys.boost.ipapower");
        this.mScrollerItem2PropMap.put("boost_big_core", "persist.sys.boost.isbigcore");
        this.mScrollerItem2PropMap.put("enable_skipped_frame", "persist.sys.boost.skipframe");
        this.mScrollerItem2PropMap.put("boost_by_each_fling", "persist.sys.boost.byeachfling");
        obtainConfigInfo(CONFIG_SCROLLERBOOST, this.mScrollerItem2PropMap, this.mScrollerInfoMap);
    }
}
