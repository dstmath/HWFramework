package com.android.server.rms.iaware.dev;

import android.content.Context;
import android.os.SystemClock;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.rms.iaware.NetLocationStrategy;
import android.util.ArrayMap;
import com.android.server.rms.iaware.dev.PhoneStatusRecong;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NetLocationSchedFeatureRT extends DevSchedFeatureBase {
    private static final int AWARE_UNKNOWN_TYPE = -1;
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
    private static final int SCENE_ID_ABS_STATIC = 2111;
    private static final int SCENE_ID_DEFAULT = 2110;
    private static final int SCENE_ID_MOVEMENT_LEVEL_ONE = 2114;
    private static final int SCENE_ID_MOVEMENT_LEVEL_THREE = 2116;
    private static final int SCENE_ID_MOVEMENT_LEVEL_TWO = 2115;
    private static final int SCENE_ID_RELATIVE_STATIC_LEVEL_ONE = 2112;
    private static final int SCENE_ID_RELATIVE_STATIC_LEVEL_TWO = 2113;
    private static final String SCENE_NAME_ABS_STATIC = "static";
    private static final String SCENE_NAME_DEFAULT = "default";
    private static final String SCENE_NAME_MOVEMENT_LEVEL_ONE = "movement_one";
    private static final String SCENE_NAME_MOVEMENT_LEVEL_THREE = "movement_three";
    private static final String SCENE_NAME_MOVEMENT_LEVEL_TWO = "movement_two";
    private static final String SCENE_NAME_RELATIVE_STATIC_LEVEL_ONE = "relative_static_one";
    private static final String SCENE_NAME_RELATIVE_STATIC_LEVEL_TWO = "relative_static_two";
    private static final int SYSTEM_UID = 1000;
    private static final String TAG = "NetLocationSchedFeatureRT";
    private static final int THIRD_APP_UID = 10000;
    private static final ArrayMap<Integer, Integer> sPhoneStatusToSceneId = new ArrayMap<>();
    private Context mContext;
    private DevXmlConfig mDevXmlConfig = new DevXmlConfig();
    private boolean mIsInNavi = false;
    private boolean mIsParseXmlOk = false;
    private String mNetLocationName;
    private volatile int mPhoneStatus = 0;
    private final Map<Integer, SceneInfo> mSceneMap = new ArrayMap();

    static {
        sPhoneStatusToSceneId.put(0, Integer.valueOf(SCENE_ID_DEFAULT));
        sPhoneStatusToSceneId.put(1, Integer.valueOf(SCENE_ID_ABS_STATIC));
        sPhoneStatusToSceneId.put(2, Integer.valueOf(SCENE_ID_RELATIVE_STATIC_LEVEL_ONE));
        sPhoneStatusToSceneId.put(3, Integer.valueOf(SCENE_ID_RELATIVE_STATIC_LEVEL_TWO));
        sPhoneStatusToSceneId.put(4, Integer.valueOf(SCENE_ID_MOVEMENT_LEVEL_ONE));
        sPhoneStatusToSceneId.put(5, Integer.valueOf(SCENE_ID_MOVEMENT_LEVEL_TWO));
        sPhoneStatusToSceneId.put(6, Integer.valueOf(SCENE_ID_MOVEMENT_LEVEL_THREE));
    }

    public NetLocationSchedFeatureRT(Context context, String name) {
        super(context);
        this.mContext = context;
        this.mNetLocationName = name;
        initXmlMap();
        AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " create NetLocationSchedFeatureRT success");
    }

    public boolean handleUpdateCustConfig() {
        initXmlMap();
        AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " update cust config success. mSceneMap is " + this.mSceneMap);
        return true;
    }

    private void initXmlMap() {
        this.mSceneMap.clear();
        this.mIsParseXmlOk = initSceneRullMap() && isParseXmlOk();
        if (!this.mIsParseXmlOk) {
            this.mSceneMap.clear();
        }
    }

    private boolean initSceneRullMap() {
        Map<String, ArrayList<String>> sceneRullMap = new ArrayMap<>();
        ArrayList<String> rullList = new ArrayList<>();
        rullList.add(SceneInfo.ITEM_RULE_ALLOW);
        rullList.add(SceneInfo.ITEM_RULE_SERVICE);
        rullList.add(SceneInfo.ITEM_RULE_NOT_ALLOW);
        sceneRullMap.put("default", rullList);
        ArrayList<String> rullList1 = new ArrayList<>();
        rullList1.add(SceneInfo.ITEM_RULE_ALLOW);
        rullList1.add(SceneInfo.ITEM_RULE_SERVICE);
        sceneRullMap.put(SCENE_NAME_ABS_STATIC, rullList1);
        ArrayList<String> rullList2 = new ArrayList<>();
        rullList2.add(SceneInfo.ITEM_RULE_ALLOW);
        rullList2.add(SceneInfo.ITEM_RULE_SERVICE);
        sceneRullMap.put(SCENE_NAME_RELATIVE_STATIC_LEVEL_ONE, rullList2);
        ArrayList<String> rullList3 = new ArrayList<>();
        rullList3.add(SceneInfo.ITEM_RULE_ALLOW);
        rullList3.add(SceneInfo.ITEM_RULE_SERVICE);
        sceneRullMap.put(SCENE_NAME_RELATIVE_STATIC_LEVEL_TWO, rullList3);
        ArrayList<String> rullList4 = new ArrayList<>();
        rullList4.add(SceneInfo.ITEM_RULE_ALLOW);
        rullList4.add(SceneInfo.ITEM_RULE_SERVICE);
        sceneRullMap.put(SCENE_NAME_MOVEMENT_LEVEL_ONE, rullList4);
        ArrayList<String> rullList5 = new ArrayList<>();
        rullList5.add(SceneInfo.ITEM_RULE_ALLOW);
        rullList5.add(SceneInfo.ITEM_RULE_SERVICE);
        sceneRullMap.put(SCENE_NAME_MOVEMENT_LEVEL_TWO, rullList5);
        ArrayList<String> rullList6 = new ArrayList<>();
        rullList6.add(SceneInfo.ITEM_RULE_ALLOW);
        rullList6.add(SceneInfo.ITEM_RULE_SERVICE);
        sceneRullMap.put(SCENE_NAME_MOVEMENT_LEVEL_THREE, rullList6);
        return this.mDevXmlConfig.readDevStrategy(this.mSceneMap, this.mNetLocationName, sceneRullMap);
    }

    private List<PhoneStatusRecong.CurrentStatus> getMotionStatus() {
        PhoneStatusRecong.getInstance().getDeviceStatus();
        return PhoneStatusRecong.getInstance().getCurrentStatus();
    }

    private boolean isParseXmlOk() {
        for (Map.Entry<Integer, Integer> entry : sPhoneStatusToSceneId.entrySet()) {
            AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " value is " + entry.getValue());
            if (entry.getValue() == null) {
                return false;
            }
        }
        return true;
    }

    public NetLocationStrategy getNetLocationStrategy(String pkgName, int uid) {
        String str = pkgName;
        int i = uid;
        if (str == null || i <= 0 || !this.mIsParseXmlOk || this.mIsInNavi) {
            AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " mIsInNavi is " + this.mIsInNavi + " mIsParseXmlOk is " + this.mIsParseXmlOk + " getNetLocationStrategy null");
            return null;
        }
        long mode = 0;
        long timestamp = SystemClock.elapsedRealtime();
        if (i == 1000 || i > 10000) {
            int pkgType = AppTypeRecoManager.getInstance().getAppType(str);
            AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " pkgType is " + pkgType + " pkgName is " + str + " uid is " + i);
            if (pkgType == -1) {
                mode = 0;
            }
            long mode2 = mode;
            List<PhoneStatusRecong.CurrentStatus> curDevStatus = getMotionStatus();
            if (curDevStatus == null) {
                AwareLog.e(TAG, this.mNetLocationName + " get device status failed.");
                return null;
            }
            AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + ", pkgName:" + str + ", curDevStatus:" + curDevStatus);
            List<PhoneStatusRecong.CurrentStatus> list = curDevStatus;
            return getAppropriateStrategy(curDevStatus, str, pkgType, i, mode2, timestamp);
        }
        AwareLog.d(TAG, "allow scan, mNetLocationName is " + this.mNetLocationName + " getNetLocationStrategy mode is " + 0 + " pkgName is " + str);
        return new NetLocationStrategy(0, timestamp);
    }

    private NetLocationStrategy getAppropriateStrategy(List<PhoneStatusRecong.CurrentStatus> curDevStatus, String pkgName, int pkgType, int uid, long modeInit, long timestampInit) {
        int phoneStatus;
        String str = pkgName;
        NetLocationStrategy netLocationStrategy = null;
        if (curDevStatus == null || str == null) {
            AwareLog.d(TAG, this.mNetLocationName + ", curDevStatus or pkgName is null");
            return null;
        }
        long mode = modeInit;
        long timestamp = timestampInit;
        Iterator<PhoneStatusRecong.CurrentStatus> it = curDevStatus.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            PhoneStatusRecong.CurrentStatus devStatus = it.next();
            if (devStatus != null) {
                timestamp = devStatus.getTimestamp();
                AwareLog.d(TAG, this.mNetLocationName + ", Current status :" + phoneStatus + ", current time :" + timestamp);
                SceneInfo sceneInfo = this.mSceneMap.get(Integer.valueOf(convertPhoneStatusToSceneId(phoneStatus)));
                if (sceneInfo == null) {
                    AwareLog.i(TAG, "no scene for " + this.mNetLocationName + ", sceneId:" + sceneId);
                    return netLocationStrategy;
                }
                int index = sceneInfo.isMatch(str, Integer.valueOf(pkgType));
                if (index >= 0) {
                    mode = sceneInfo.getMode(index);
                    if (uid % 100000 == 1000) {
                        RuleBase ruleBase = sceneInfo.getRuleBase(index);
                        if (ruleBase == null || !(ruleBase instanceof RuleService)) {
                            AwareLog.d(TAG, str + " is system app and not RuleService obj, out of control");
                            return null;
                        }
                        AwareLog.d(TAG, "mNetLocationName is " + this.mNetLocationName + " system uid");
                        if (!((RuleService) ruleBase).getServiceList().contains(str)) {
                            mode = 0;
                        }
                    } else if (uid % 100000 < 10000) {
                        mode = 0;
                    }
                    AwareLog.d(TAG, "match success, mNetLocationName is " + this.mNetLocationName + ", phoneStatus:" + phoneStatus);
                } else {
                    netLocationStrategy = null;
                }
            }
        }
        AwareLog.d(TAG, "iaware strategy, mNetLocationName is " + this.mNetLocationName + " getNetLocationStrategy mode is " + mode + " pkgName is " + str);
        return new NetLocationStrategy(mode, timestamp);
    }

    private int convertPhoneStatusToSceneId(int phoneStatus) {
        Integer convertType = sPhoneStatusToSceneId.get(Integer.valueOf(phoneStatus));
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
