package com.android.server.rms.iaware.dev;

import android.content.Context;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScreenOnWakelockSchedFeatureRT extends DevSchedFeatureBase {
    private static final String CONFIG_IGNORE_WAKELOCK = "ignoreWakelock";
    private static final String CONFIG_SCREENON_WAKELOCK = "screenOnWakelock";
    private static final String ITEM_APP = "app";
    private static final String ITEM_TAG = "tag";
    private static final String TAG = "ScreenOnWakelockSchedFeatureRT";
    private static final String TYPE_KEYWORD = "type";
    private Context mContext;
    private List<WakelockIgnoreInfo> mIgnoreWakelockList = new ArrayList();

    private static class WakelockIgnoreInfo {
        public String mPkgName;
        public String mTag;

        public WakelockIgnoreInfo(String pkgName, String tag) {
            this.mPkgName = pkgName;
            this.mTag = tag;
        }
    }

    public ScreenOnWakelockSchedFeatureRT(Context context, String name) {
        super(context);
        this.mContext = context;
        loadScreenOnWakelockConfig();
    }

    public boolean handleUpdateCustConfig() {
        AwareLog.d(TAG, "handleUpdateCustConfig!");
        this.mIgnoreWakelockList.clear();
        loadScreenOnWakelockConfig();
        return true;
    }

    public boolean handlerNaviStatus(boolean isInNavi) {
        return true;
    }

    private void parseSubItemList(List<AwareConfig.SubItem> subItemList) {
        if (subItemList != null) {
            for (AwareConfig.SubItem subItem : subItemList) {
                if (subItem != null) {
                    Map<String, String> properties = subItem.getProperties();
                    if (properties != null) {
                        String app = properties.get(ITEM_APP);
                        if (app != null && !"".equals(app)) {
                            String tag = properties.get(ITEM_TAG);
                            if (tag != null && !"".equals(tag)) {
                                AwareLog.d(TAG, "add wakelock ignore app " + app + " tag " + tag);
                                this.mIgnoreWakelockList.add(new WakelockIgnoreInfo(app, tag));
                            }
                        }
                    }
                }
            }
        }
    }

    private void loadScreenOnWakelockConfig() {
        List<AwareConfig.Item> itemList = DevXmlConfig.getItemList(CONFIG_SCREENON_WAKELOCK);
        if (itemList == null) {
            AwareLog.i(TAG, "parse wakelock config error!");
            return;
        }
        for (AwareConfig.Item item : itemList) {
            if (item != null) {
                Map<String, String> configPropertries = item.getProperties();
                if (configPropertries != null && CONFIG_IGNORE_WAKELOCK.equals(configPropertries.get("type"))) {
                    List<AwareConfig.SubItem> subItemList = item.getSubItemList();
                    if (!(subItemList == null || subItemList.size() == 0)) {
                        parseSubItemList(subItemList);
                    }
                }
            }
        }
    }

    private boolean isIgnoreWakelock(String pkgName, String tag) {
        for (WakelockIgnoreInfo info : this.mIgnoreWakelockList) {
            if (info != null && pkgName.equals(info.mPkgName)) {
                if ("*".equals(info.mTag) || tag.equals(info.mTag)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAwarePreventWakelockScreenOn(String pkgName, String tag) {
        if (pkgName == null || tag == null) {
            AwareLog.e(TAG, "isAwarePreventWakelockScreenOn: pkgName or tag is null!");
            return false;
        } else if (isIgnoreWakelock(pkgName, tag)) {
            AwareLog.d(TAG, "isAwarePreventWakelockScreenOn: pkg " + pkgName + " tag " + tag + " match ignore list");
            return false;
        } else {
            int appAttr = AppTypeRecoManager.getInstance().getAppAttribute(pkgName);
            AwareLog.d(TAG, "isAwarePreventWakelockScreenOn: get pkg " + pkgName + " tag " + tag + " APPAttribute " + appAttr);
            if (appAttr != -1 && (appAttr & 8) == 8) {
                return true;
            }
            return false;
        }
    }
}
