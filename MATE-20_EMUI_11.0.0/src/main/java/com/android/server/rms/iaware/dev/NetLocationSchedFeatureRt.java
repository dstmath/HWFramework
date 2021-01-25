package com.android.server.rms.iaware.dev;

import android.content.Context;
import android.os.SystemClock;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.rms.iaware.NetLocationStrategy;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AppBatteryStrategy;
import com.android.server.mtm.iaware.appmng.CloudPushManager;
import com.android.server.rms.iaware.dev.FeatureXmlConfigParserRt;
import com.android.server.rms.iaware.dev.PhoneStatusRecong;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NetLocationSchedFeatureRt extends DevSchedFeatureBase {
    private static final int AWARE_UNKNOWN_TYPE = -1;
    private static final String FEATURE_OPEN = "1";
    public static final long MODE_ALLOW_SCAN = 0;
    public static final long MODE_NOT_ALLOW_SCAN = -1;
    private static final int MUTIUSER_ADD_UID = 100000;
    public static final int PHONE_STATUS_ABS_STATIC = 1;
    public static final int PHONE_STATUS_DEFAULT = 0;
    public static final int PHONE_STATUS_MOVEMENT_LEVEL_ONE = 4;
    public static final int PHONE_STATUS_MOVEMENT_LEVEL_THREE = 6;
    public static final int PHONE_STATUS_MOVEMENT_LEVEL_TWO = 5;
    public static final int PHONE_STATUS_RELATIVE_STATIC_LEVEL_ONE = 2;
    public static final int PHONE_STATUS_RELATIVE_STATIC_LEVEL_TWO = 3;
    private static final ArrayMap<Integer, Integer> PHONE_STATUS_TO_SCENE_TABLE = new ArrayMap<>();
    private static final int SCENE_ID_ABS_STATIC = 2111;
    private static final int SCENE_ID_DEFAULT = 2110;
    private static final int SCENE_ID_MOVEMENT_LEVEL_ONE = 2114;
    private static final int SCENE_ID_MOVEMENT_LEVEL_THREE = 2116;
    private static final int SCENE_ID_MOVEMENT_LEVEL_TWO = 2115;
    private static final int SCENE_ID_RELATIVE_STATIC_LEVEL_ONE = 2112;
    private static final int SCENE_ID_RELATIVE_STATIC_LEVEL_TWO = 2113;
    private static final int SYSTEM_UID = 1000;
    private static final String TAG = "NetLocationSchedFeatureRT";
    private static final int THIRD_APP_UID = 10000;
    private static boolean sBatteryEscapeEnable = SystemPropertiesEx.getBoolean("hw_mc.iaware.battery_escape_enable", false);
    private Context mContext;
    private boolean mEnable = false;
    private boolean mIsInNavi = false;
    private String mNetLocationName;
    private final Map<Integer, List<FeatureXmlConfigParserRt.ConfigRule>> mSceneMap = new ArrayMap();

    static {
        PHONE_STATUS_TO_SCENE_TABLE.put(0, Integer.valueOf((int) SCENE_ID_DEFAULT));
        PHONE_STATUS_TO_SCENE_TABLE.put(1, Integer.valueOf((int) SCENE_ID_ABS_STATIC));
        PHONE_STATUS_TO_SCENE_TABLE.put(2, Integer.valueOf((int) SCENE_ID_RELATIVE_STATIC_LEVEL_ONE));
        PHONE_STATUS_TO_SCENE_TABLE.put(3, Integer.valueOf((int) SCENE_ID_RELATIVE_STATIC_LEVEL_TWO));
        PHONE_STATUS_TO_SCENE_TABLE.put(4, Integer.valueOf((int) SCENE_ID_MOVEMENT_LEVEL_ONE));
        PHONE_STATUS_TO_SCENE_TABLE.put(5, Integer.valueOf((int) SCENE_ID_MOVEMENT_LEVEL_TWO));
        PHONE_STATUS_TO_SCENE_TABLE.put(6, Integer.valueOf((int) SCENE_ID_MOVEMENT_LEVEL_THREE));
    }

    public NetLocationSchedFeatureRt(Context context, String name) {
        super(context);
        this.mContext = context;
        this.mNetLocationName = name;
        AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " create NetLocationSchedFeatureRt success");
    }

    @Override // com.android.server.rms.iaware.dev.DevSchedFeatureBase
    public void readFeatureConfig(FeatureXmlConfigParserRt.FeatureXmlConfig config) {
        initXmlMap(config);
        if (config.subSwitch) {
            this.mEnable = true;
        }
    }

    private void initXmlMap(FeatureXmlConfigParserRt.FeatureXmlConfig config) {
        this.mSceneMap.clear();
        initSceneRuleMap(config);
    }

    private void setNetLocationConfig(List<FeatureXmlConfigParserRt.ConfigItem> list) {
        for (FeatureXmlConfigParserRt.ConfigItem item : list) {
            if (!(item == null || item.itemId <= 0 || item.ruleList == null || item.ruleList.size() == 0)) {
                this.mSceneMap.put(Integer.valueOf(item.itemId), item.ruleList);
            }
        }
    }

    private void initSceneRuleMap(FeatureXmlConfigParserRt.FeatureXmlConfig config) {
        if (config.configItemList != null && config.configItemList.size() > 0 && config.subSwitch) {
            setNetLocationConfig(config.configItemList);
        }
    }

    private List<PhoneStatusRecong.CurrentStatus> getMotionStatus() {
        PhoneStatusRecong.getInstance().getDeviceStatus();
        return PhoneStatusRecong.getInstance().getCurrentStatus();
    }

    public NetLocationStrategy getNetLocationStrategy(String pkgName, int uid) {
        if (!this.mEnable || this.mIsInNavi) {
            AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " mIsInNavi is " + this.mIsInNavi + " mEnable is " + this.mEnable + " getNetLocationStrategy null");
            return null;
        } else if (pkgName == null || uid <= 0) {
            AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " getNetLocationStrategy null");
            return null;
        } else {
            long timeStamp = SystemClock.elapsedRealtime();
            int pkgType = AppTypeRecoManager.getInstance().getAppType(pkgName);
            if (isPkgOrSceneIdCloudData(pkgName, Integer.valueOf(pkgType))) {
                AwareLog.d(TAG, "pkg or pkg scene is in cloud data, allow scan, pkgName = " + pkgName);
                return new NetLocationStrategy(0, timeStamp);
            } else if (sBatteryEscapeEnable && AppBatteryStrategy.getInstance().isAppIsNeverOptimized(pkgName)) {
                AwareLog.d(TAG, "pkg is never optimized, allow scan, pkgName = " + pkgName);
                return new NetLocationStrategy(0, timeStamp);
            } else if (uid == 1000 || uid > 10000) {
                AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " pkgType is " + pkgType + " pkgName is " + pkgName + " uid is " + uid);
                if (pkgType == -1) {
                }
                List<PhoneStatusRecong.CurrentStatus> curDevStatus = getMotionStatus();
                if (curDevStatus == null) {
                    AwareLog.e(TAG, this.mNetLocationName + " get device status failed.");
                    return null;
                }
                AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + ", pkgName:" + pkgName + ", curDevStatus:" + curDevStatus);
                return getAppropriateStrategy(curDevStatus, pkgName, pkgType, uid, timeStamp);
            } else {
                AwareLog.d(TAG, "allow scan, mNetLocationName is " + this.mNetLocationName + " getNetLocationStrategy mode is 0 pkgName is " + pkgName);
                return new NetLocationStrategy(0, timeStamp);
            }
        }
    }

    private FeatureXmlConfigParserRt.ConfigRule getMatchRule(List<FeatureXmlConfigParserRt.ConfigRule> sceneRules, int pkgType, String pkgName) {
        for (FeatureXmlConfigParserRt.ConfigRule rule : sceneRules) {
            if (rule.ruleSceneType == FeatureXmlConfigParserRt.ConfigRule.SceneType.PKG_NAME_TYPE && rule.ruleScenePkgSets != null && rule.ruleScenePkgSets.contains(pkgName)) {
                return rule;
            }
            if (rule.ruleSceneType == FeatureXmlConfigParserRt.ConfigRule.SceneType.SCENE_ID_TYPE && rule.ruleSceneIdSets != null && rule.ruleSceneIdSets.contains(Integer.valueOf(pkgType))) {
                return rule;
            }
        }
        return null;
    }

    private NetLocationStrategy getAppropriateStrategy(List<PhoneStatusRecong.CurrentStatus> curDevStatus, String pkgName, int pkgType, int uid, long timeStampInit) {
        if (curDevStatus != null) {
            if (pkgName != null) {
                long mode = 0;
                long timeStamp = timeStampInit;
                Iterator<PhoneStatusRecong.CurrentStatus> it = curDevStatus.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    PhoneStatusRecong.CurrentStatus devStatus = it.next();
                    if (devStatus != null) {
                        timeStamp = devStatus.getTimeStamp();
                        int phoneStatus = devStatus.getPhoneStatus();
                        AwareLog.d(TAG, this.mNetLocationName + ", Current status :" + phoneStatus + ", current time :" + timeStamp);
                        int sceneId = convertPhoneStatusToSceneId(phoneStatus);
                        List<FeatureXmlConfigParserRt.ConfigRule> sceneInfos = this.mSceneMap.get(Integer.valueOf(sceneId));
                        if (sceneInfos == null) {
                            AwareLog.i(TAG, "no scene for " + this.mNetLocationName + ", sceneId:" + sceneId);
                            return null;
                        }
                        FeatureXmlConfigParserRt.ConfigRule rule = getMatchRule(sceneInfos, pkgType, pkgName);
                        if (rule != null) {
                            if (uid % MUTIUSER_ADD_UID == 1000) {
                                if (rule.ruleSceneType != FeatureXmlConfigParserRt.ConfigRule.SceneType.PKG_NAME_TYPE) {
                                    AwareLog.d(TAG, pkgName + " is system app and not RuleService obj, out of control");
                                    return null;
                                }
                                AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " system uid");
                                mode = (long) rule.ruleMode;
                            } else if (uid % MUTIUSER_ADD_UID < 10000) {
                                mode = 0;
                            } else {
                                mode = (long) rule.ruleMode;
                                AwareLog.d(TAG, "third party app can control.");
                            }
                            AwareLog.d(TAG, "match success, mNetLocationName is " + this.mNetLocationName + ", phoneStatus:" + phoneStatus);
                        }
                    }
                }
                AwareLog.d(TAG, "iaware strategy, mNetLocationName is " + this.mNetLocationName + " getNetLocationStrategy mode is " + mode + " pkgName is " + pkgName);
                return new NetLocationStrategy(mode, timeStamp);
            }
        }
        AwareLog.d(TAG, this.mNetLocationName + ", curDevStatus or pkgName is null");
        return null;
    }

    private int convertPhoneStatusToSceneId(int phoneStatus) {
        Integer convertType = PHONE_STATUS_TO_SCENE_TABLE.get(Integer.valueOf(phoneStatus));
        if (convertType == null) {
            return SCENE_ID_DEFAULT;
        }
        return convertType.intValue();
    }

    @Override // com.android.server.rms.iaware.dev.DevSchedFeatureBase
    public boolean handleNaviStatus(boolean isInNavi) {
        this.mIsInNavi = isInNavi;
        AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " mIsInNavi is " + this.mIsInNavi);
        return true;
    }

    private boolean isPkgOrSceneIdCloudData(String pkgName, Integer sceneId) {
        CloudPushManager.FeatureCloudData data = CloudPushManager.getInstance().getFeatureCloudInfo(this.mNetLocationName);
        if (data == null) {
            AwareLog.d(TAG, "getFeatureCloudInfo data is null ");
            return false;
        } else if (data.getFeatureSwith().equalsIgnoreCase(FEATURE_OPEN)) {
            return false;
        } else {
            if ((pkgName == null || !data.isPkgInCloudData(pkgName)) && !data.isSceneIdInCloudData(sceneId)) {
                return false;
            }
            return true;
        }
    }
}
