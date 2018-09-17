package com.android.server.rms.iaware.dev;

import android.content.Context;
import android.os.SystemClock;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.rms.iaware.NetLocationStrategy;
import android.util.ArrayMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

public class NetLocationSchedFeatureRT extends DevSchedFeatureBase {
    private static final int AWARE_UNKNOWN_TYPE = -1;
    public static final long MODE_ALLOW_SCAN = 0;
    public static final long MODE_NOT_ALLOW_SCAN = -1;
    private static final int MUTIUSER_ADD_UID = 100000;
    public static final int PHONE_STATUS_ABS_STATIC = 1;
    public static final int PHONE_STATUS_DEFAULT = 0;
    public static final int PHONE_STATUS_RELATIVE_STATIC = 2;
    private static final int SCENE_ID_ABS_STATIC = 2111;
    private static final int SCENE_ID_DEFAULT = 2110;
    private static final int SCENE_ID_RELATIVE_STATIC = 2112;
    private static final String SCENE_NAME_ABS_STATIC = "static";
    private static final String SCENE_NAME_DEFAULT = "default";
    private static final String SCENE_NAME_RELATIVE_STATIC_LEVEL_ONE = "relative_static_one";
    private static final int SYSTEM_UID = 1000;
    private static final String TAG = "NetLocationSchedFeatureRT";
    private static final int THIRD_APP_UID = 10000;
    private static final ArrayMap<Integer, Integer> sPhoneStatusToSceneId = new ArrayMap();
    private Context mContext;
    private DevXmlConfig mDevXmlConfig = new DevXmlConfig();
    private boolean mIsInNavi = false;
    private boolean mIsParseXmlOk = false;
    private String mNetLocationName;
    private volatile int mPhoneStatus = 0;
    private final Map<Integer, SceneInfo> mSceneMap = new ArrayMap();
    private long mTimeStamp = SystemClock.elapsedRealtime();

    static {
        sPhoneStatusToSceneId.put(Integer.valueOf(0), Integer.valueOf(SCENE_ID_DEFAULT));
        sPhoneStatusToSceneId.put(Integer.valueOf(1), Integer.valueOf(SCENE_ID_ABS_STATIC));
        sPhoneStatusToSceneId.put(Integer.valueOf(2), Integer.valueOf(SCENE_ID_RELATIVE_STATIC));
    }

    public NetLocationSchedFeatureRT(Context context, String name) {
        super(context);
        this.mContext = context;
        this.mNetLocationName = name;
        initXmlMap();
        AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " create NetLocationSchedFeatureRT success");
    }

    private void initXmlMap() {
        this.mSceneMap.clear();
        this.mIsParseXmlOk = initSceneRullMap() ? isParseXmlOk() : false;
        if (!this.mIsParseXmlOk) {
            this.mSceneMap.clear();
        }
    }

    private boolean initSceneRullMap() {
        Map<String, ArrayList<String>> sceneRullMap = new ArrayMap();
        ArrayList<String> rullList = new ArrayList();
        rullList.add(SceneInfo.ITEM_RULE_ALLOW);
        rullList.add(SceneInfo.ITEM_RULE_SERVICE);
        rullList.add(SceneInfo.ITEM_RULE_NOT_ALLOW);
        sceneRullMap.put("default", rullList);
        ArrayList<String> rullList1 = new ArrayList();
        rullList1.add(SceneInfo.ITEM_RULE_ALLOW);
        rullList1.add(SceneInfo.ITEM_RULE_SERVICE);
        sceneRullMap.put(SCENE_NAME_ABS_STATIC, rullList1);
        ArrayList<String> rullList2 = new ArrayList();
        rullList2.add(SceneInfo.ITEM_RULE_ALLOW);
        rullList2.add(SceneInfo.ITEM_RULE_SERVICE);
        sceneRullMap.put(SCENE_NAME_RELATIVE_STATIC_LEVEL_ONE, rullList2);
        return this.mDevXmlConfig.readDevStrategy(this.mSceneMap, this.mNetLocationName, sceneRullMap);
    }

    private int getMotionStatus() {
        PhoneStatusRecong.getInstance().getDeviceStatus();
        this.mPhoneStatus = PhoneStatusRecong.getInstance().getCurrentStatus();
        AwareLog.d(TAG, "current status is " + this.mPhoneStatus);
        return this.mPhoneStatus;
    }

    private long getTimeStamp() {
        this.mTimeStamp = PhoneStatusRecong.getInstance().getCurrentStatusEnterTime();
        return this.mTimeStamp;
    }

    private boolean isParseXmlOk() {
        for (Entry<Integer, Integer> entry : sPhoneStatusToSceneId.entrySet()) {
            AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " value is " + entry.getValue());
            if (entry.getValue() == null) {
                return false;
            }
            SceneInfo sceneInfo = (SceneInfo) this.mSceneMap.get(entry.getValue());
            if (sceneInfo != null) {
                RuleBase ruleBase = sceneInfo.getRuleBase(SceneInfo.ITEM_RULE_ALLOW);
                if (ruleBase == null || ((ruleBase instanceof RuleAllow) ^ 1) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public NetLocationStrategy getNetLocationStrategy(String pkgName, int uid) {
        if (pkgName == null || uid <= 0 || (this.mIsParseXmlOk ^ 1) != 0 || this.mIsInNavi) {
            AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " mIsInNavi is " + this.mIsInNavi + " mIsParseXmlOk is " + this.mIsParseXmlOk + " getNetLocationStrategy null");
            return null;
        }
        long mode = -1;
        if (uid == 1000 || uid > 10000) {
            int pkgType = AppTypeRecoManager.getInstance().getAppType(pkgName);
            AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " pkgType is " + pkgType + " pkgName is " + pkgName + " uid is " + uid);
            if (pkgType == -1) {
                mode = 0;
            }
            SceneInfo sceneInfo = (SceneInfo) this.mSceneMap.get(Integer.valueOf(convertPhoneStatusToSceneId(getMotionStatus())));
            if (sceneInfo != null) {
                int index = sceneInfo.isMatch(pkgName, Integer.valueOf(pkgType));
                if (index >= 0) {
                    mode = sceneInfo.getMode(index);
                }
                if (uid % 100000 == 1000) {
                    RuleBase ruleBase = sceneInfo.getRuleBase(SceneInfo.ITEM_RULE_SERVICE);
                    if (ruleBase == null || ((ruleBase instanceof RuleService) ^ 1) != 0) {
                        return null;
                    }
                    AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " system uid");
                    if (!((RuleService) ruleBase).getServiceList().contains(pkgName)) {
                        mode = 0;
                    }
                } else if (uid % 100000 < 10000) {
                    mode = 0;
                }
            } else {
                AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " sceneInfo is null, getNetLocationStrategy null");
                return null;
            }
        }
        mode = 0;
        AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " getNetLocationStrategy mode is " + mode + " pkgName is " + pkgName);
        return new NetLocationStrategy(mode, getTimeStamp());
    }

    private int convertPhoneStatusToSceneId(int phoneStatus) {
        Integer convertType = (Integer) sPhoneStatusToSceneId.get(Integer.valueOf(phoneStatus));
        if (convertType == null) {
            return SCENE_ID_DEFAULT;
        }
        return convertType.intValue();
    }

    public boolean handlerNaviStatus(boolean isInNavi) {
        this.mIsInNavi = isInNavi;
        AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " mIsInNavi is " + this.mIsInNavi);
        return true;
    }
}
