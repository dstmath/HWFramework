package com.android.server.rms.iaware.dev;

import android.content.Context;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import android.rms.iaware.LogIAware;
import android.util.ArrayMap;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GpsSchedFeatureRT extends DevSchedFeatureBase {
    private static final String ACTIVITY_SCENE_NAME = "Activity_Start";
    private static final String ATTR_RULE_ACTIVITY_IN = "Activity_In";
    private static final String CONFIG_GPS_STRATEGY = "gps_strategy";
    private static final String FEATURE_TITLE = "DevSchedFeature";
    public static final String GPS_SPLIT_SYMBOL = ",";
    private static final String ITEM_GPS_MODE = "mode";
    private static final String ITEM_PACKAGE_NAME = "package_name";
    private static final String ITEM_RULE = "rule";
    private static final String ITEM_SCENE_NAME = "scenename";
    private static final String TAG = "GpsSchedFeatureRT";
    private final List<GpsActivityInfo> mActivityInfoList = new ArrayList();
    private String mCurrentActivityName;
    private String mCurrentPackageName;

    public GpsSchedFeatureRT(Context context, String name) {
        super(context);
        loadGpsActivityInfo(this.mActivityInfoList);
        AwareLog.d(TAG, "create " + name + "GpsSchedFeatureRT success");
    }

    public boolean handleResAppData(long timestamp, int event, AttrSegments attrSegments) {
        switch (event) {
            case 15019:
            case 85019:
                handleActivityEvent(event, attrSegments);
                return true;
            default:
                return false;
        }
    }

    private void handleActivityEvent(int event, AttrSegments attrSegments) {
        if (attrSegments.isValid()) {
            ArrayMap<String, String> appInfo = attrSegments.getSegment("calledApp");
            if (appInfo == null) {
                AwareLog.i(TAG, "appInfo is NULL");
                return;
            }
            String packageName = (String) appInfo.get(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY);
            String activityName = (String) appInfo.get("activityName");
            if (15019 == event) {
                this.mCurrentPackageName = packageName;
                this.mCurrentActivityName = activityName;
            }
            try {
                int uid = Integer.parseInt((String) appInfo.get("uid"));
                int pid = Integer.parseInt((String) appInfo.get("pid"));
                if (inInvalidActivityInfo(packageName, activityName, uid, pid)) {
                    AwareLog.i(TAG, "isInvalidActivityInfo, packageName: " + packageName + ", activityName: " + activityName + ", uid: " + uid + ", pid:" + pid);
                    return;
                }
                GpsActivityInfo devActivityInfo = queryActivityInfo(packageName, activityName);
                if (devActivityInfo != null) {
                    int mode = devActivityInfo.getLocationMode();
                    AwareLog.d(TAG, "activity match success, packageName : " + packageName + ", activityName : " + activityName + ", mode : " + mode + ", event : " + event + ", uid is " + uid + ", pid is " + pid);
                    reportActivityStateToNRT(event, packageName, activityName, uid, pid, mode);
                }
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "get uid fail, happend NumberFormatException");
            }
        }
    }

    private boolean inInvalidActivityInfo(String packageName, String activityName, int uid, int pid) {
        if (packageName == null || activityName == null || uid <= 1000 || pid < 0) {
            return true;
        }
        return false;
    }

    private GpsActivityInfo queryActivityInfo(String packageName, String activityName) {
        if (packageName == null || activityName == null) {
            return null;
        }
        int size = this.mActivityInfoList.size();
        for (int index = 0; index < size; index++) {
            GpsActivityInfo devActivityInfo = (GpsActivityInfo) this.mActivityInfoList.get(index);
            if (devActivityInfo == null) {
                return null;
            }
            if (devActivityInfo.isMatch(packageName, activityName)) {
                return devActivityInfo;
            }
        }
        return null;
    }

    private void reportActivityStateToNRT(int event, String packageName, String activityName, int uid, int pid, int mode) {
        if (packageName == null || activityName == null) {
            AwareLog.e(TAG, "input param error, packageName is" + packageName + ", activityName is " + activityName);
            return;
        }
        if (15019 == event) {
            reportActivityState(packageName, activityName, uid, pid, mode, true);
        } else if (85019 == event) {
            if (!packageName.equals(this.mCurrentPackageName) || queryActivityInfo(this.mCurrentPackageName, this.mCurrentActivityName) == null) {
                reportActivityState(packageName, activityName, uid, pid, mode, false);
            } else {
                AwareLog.d(TAG, "switch control activity " + activityName + " to control activity " + this.mCurrentActivityName + ", do not remove activity control. package name : " + packageName);
            }
        }
    }

    private void reportActivityState(String packageName, String activityName, int uid, int pid, int mode, boolean in) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(FEATURE_TITLE).append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
        stringBuffer.append(packageName).append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
        stringBuffer.append(activityName).append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
        stringBuffer.append(uid).append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
        stringBuffer.append(pid).append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
        stringBuffer.append(mode);
        LogIAware.report(in ? 2104 : 2105, stringBuffer.toString());
    }

    private void loadGpsActivityInfo(List<GpsActivityInfo> activityInfoList) {
        if (activityInfoList != null) {
            activityInfoList.clear();
            List<Item> itemList = DevXmlConfig.getItemList(CONFIG_GPS_STRATEGY);
            if (itemList == null) {
                AwareLog.e(TAG, "parse gps strategy config error!");
                return;
            }
            for (Item item : itemList) {
                if (item != null) {
                    Map<String, String> configPropertries = item.getProperties();
                    if (configPropertries != null) {
                        if (ACTIVITY_SCENE_NAME.equals((String) configPropertries.get(ITEM_SCENE_NAME))) {
                            List<SubItem> subItemList = item.getSubItemList();
                            if (subItemList == null || subItemList.size() == 0) {
                                AwareLog.e(TAG, " subItemList is null");
                            } else {
                                parseSubItemList(subItemList, activityInfoList);
                            }
                        }
                    }
                }
            }
        }
    }

    private void parseSubItemList(List<SubItem> subItemList, List<GpsActivityInfo> activityInfoList) {
        if (subItemList != null && activityInfoList != null) {
            for (SubItem subItem : subItemList) {
                if (subItem != null) {
                    Map<String, String> properties = subItem.getProperties();
                    if (properties != null) {
                        if (ATTR_RULE_ACTIVITY_IN.equals((String) properties.get(ITEM_RULE))) {
                            String packageName = (String) properties.get("package_name");
                            if (packageName != null) {
                                try {
                                    GpsActivityInfo devActivityInfo = new GpsActivityInfo(packageName, Integer.parseInt((String) properties.get("mode")));
                                    if (devActivityInfo.loadActivitys(subItem.getValue())) {
                                        activityInfoList.add(devActivityInfo);
                                    }
                                } catch (NumberFormatException e) {
                                    AwareLog.e(TAG, "NumberFormatException, mode is not number! String mode is " + ((String) properties.get("mode")));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean handlerNaviStatus(boolean isInNavi) {
        return true;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("GpsSchedFeatureRT : ");
        s.append(", GpsActivityInfo num : ").append(this.mActivityInfoList.size());
        s.append(", GpsActivityInfo : ").append(this.mActivityInfoList.toString());
        return s.toString();
    }
}
