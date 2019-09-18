package com.android.server.rms.iaware.dev;

import android.os.IBinder;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DevXmlConfig {
    private static final String ATTR_IGNORE_APP = "ignore_app";
    private static final String ATTR_TYPE = "switch";
    private static final String CONFIG_SUBSWITCH = "sub_switch";
    public static final int DEFAULT_CUST_CONFIG = 0;
    private static final String DEV_FEATURE_NAME = "DevSchedFeature";
    public static final int INVALID_DEVICE_ID = -1;
    private static final String ITEM_DEVICE_ID = "dev_id";
    private static final String ITEM_EXCEPT_NAME = "name";
    public static final String ITEM_SCENE_ID = "scene_id";
    public static final String ITEM_SCENE_NAME = "scenename";
    private static final String ITEM_TYPE = "type";
    public static final int PLATFORM_CONFIG = 1;
    private static final String TAG = "DevXmlConfig";
    private static IBinder mCmsManager;

    private List<AwareConfig.Item> getItems(String configName) {
        if (DevSchedFeatureRT.MODEM_FEATURE.equals(configName) || DevSchedFeatureRT.WIFI_FEATURE.equals(configName)) {
            return getItemList(configName, 1);
        }
        return getItemList(configName);
    }

    public boolean readDevStrategy(Map<Integer, SceneInfo> sceneMap, String configName, Map<String, ArrayList<String>> sceneRullMap) {
        String str = configName;
        Map<String, ArrayList<String>> map = sceneRullMap;
        if (sceneMap == null || str == null || map == null) {
            AwareLog.e(TAG, "NetLocationSchedFeature para null");
            return false;
        }
        List<AwareConfig.Item> awareConfigItemList = getItems(str);
        if (awareConfigItemList == null) {
            AwareLog.e(TAG, "NetLocationSchedFeature read strategy config error!");
            return false;
        }
        for (AwareConfig.Item item : awareConfigItemList) {
            if (item == null) {
                AwareLog.e(TAG, "NetLocationSchedFeature item null");
                return false;
            }
            Map<String, String> configPropertries = item.getProperties();
            if (configPropertries == null || configPropertries.isEmpty()) {
                AwareLog.e(TAG, "NetLocationSchedFeature configPropertries null");
                return false;
            }
            String sceneName = configPropertries.get(ITEM_SCENE_NAME);
            if (sceneName == null || sceneName.isEmpty()) {
                AwareLog.e(TAG, "NetLocationSchedFeature sceneName null");
                return false;
            }
            ArrayList<String> ruleList = map.get(sceneName);
            if (ruleList == null) {
            } else if (ruleList.isEmpty()) {
                ArrayList<String> arrayList = ruleList;
            } else {
                try {
                    ArrayList<String> arrayList2 = ruleList;
                    if (!createMapLocal(item, sceneName, Integer.parseInt(configPropertries.get(ITEM_SCENE_ID)), sceneMap, ruleList)) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    ArrayList<String> arrayList3 = ruleList;
                    AwareLog.e(TAG, " NumberFormatException, sceneid is " + configPropertries.get(ITEM_SCENE_ID));
                    return false;
                }
            }
            AwareLog.e(TAG, "NetLocationSchedFeature ruleList null");
            return false;
        }
        return true;
    }

    private boolean createMapLocal(AwareConfig.Item item, String sceneName, int sceneId, Map<Integer, SceneInfo> sceneMap, List<String> ruleList) {
        SceneInfo sceneObj = new SceneInfo(sceneName, sceneId);
        List<AwareConfig.SubItem> subItemList = item.getSubItemList();
        if (subItemList == null || subItemList.size() == 0) {
            AwareLog.e(TAG, "NetLocationSchedFeature subItemList is null");
            return false;
        } else if (sceneObj.fillSceneInfo(subItemList, ruleList)) {
            sceneMap.put(Integer.valueOf(sceneId), sceneObj);
            return true;
        } else {
            AwareLog.e(TAG, "NetLocationSchedFeature fillSceneInfo error");
            return false;
        }
    }

    public static void loadSubFeatureSwitch(Map<String, String> subFeatureSwitch) {
        if (subFeatureSwitch != null) {
            List<AwareConfig.Item> itemList = getItemList(CONFIG_SUBSWITCH);
            if (itemList != null) {
                for (AwareConfig.Item item : itemList) {
                    if (item != null) {
                        Map<String, String> configPropertries = item.getProperties();
                        if (configPropertries != null) {
                            String itemType = configPropertries.get("type");
                            if (itemType != null && itemType.equals("switch")) {
                                parseSubSwitchFromSubItem(item, subFeatureSwitch);
                            }
                        }
                    }
                }
            }
        }
    }

    public static List<AwareConfig.Item> getItemList(String configName, int isCustConfig) {
        if (configName == null) {
            AwareLog.e(TAG, "configName is null");
            return null;
        }
        if (mCmsManager == null) {
            mCmsManager = IAwareCMSManager.getICMSManager();
        }
        AwareConfig awareConfig = null;
        try {
            if (mCmsManager != null) {
                if (isCustConfig == 0) {
                    awareConfig = IAwareCMSManager.getCustConfig(mCmsManager, DEV_FEATURE_NAME, configName);
                    AwareLog.d(TAG, "read custom config: " + configName);
                } else {
                    awareConfig = IAwareCMSManager.getConfig(mCmsManager, DEV_FEATURE_NAME, configName);
                    AwareLog.d(TAG, "read platform config: " + configName);
                }
            }
            if (awareConfig == null) {
                return null;
            }
            return awareConfig.getConfigList();
        } catch (RemoteException e) {
            AwareLog.e(TAG, "awareConfig is null");
            return null;
        }
    }

    public static List<AwareConfig.Item> getItemList(String configName) {
        return getItemList(configName, 0);
    }

    private static void parseSubSwitchFromSubItem(AwareConfig.Item item, Map<String, String> subFeatureSwitch) {
        if (item != null && subFeatureSwitch != null) {
            List<AwareConfig.SubItem> subItemList = item.getSubItemList();
            if (subItemList == null) {
                AwareLog.e(TAG, "can not get subswitch config subitem");
                return;
            }
            for (AwareConfig.SubItem subItem : subItemList) {
                String itemName = subItem.getName();
                if (itemName != null && !itemName.isEmpty()) {
                    String itemValue = subItem.getValue();
                    if (itemValue != null) {
                        subFeatureSwitch.put(itemName.trim(), itemValue.trim());
                    }
                } else {
                    return;
                }
            }
        }
    }

    public void readSceneInfos(String deviceName, List<SceneInfo> sceneList) {
        if (sceneList != null) {
            sceneList.clear();
            List<AwareConfig.Item> awareConfigItemList = getItemList(deviceName);
            if (awareConfigItemList == null) {
                AwareLog.e(TAG, "get " + deviceName + " sceneinfo error!");
                return;
            }
            for (AwareConfig.Item item : awareConfigItemList) {
                if (item != null) {
                    Map<String, String> configPropertries = item.getProperties();
                    if (!(configPropertries == null || configPropertries.get(ITEM_SCENE_NAME) == null)) {
                        sceneList.add(new SceneInfo(item));
                    }
                }
            }
        }
    }

    public void readExceptApps(String deviceName, List<String> exceptList) {
        if (exceptList != null) {
            exceptList.clear();
            List<AwareConfig.Item> awareConfigItemList = getItemList(deviceName);
            if (awareConfigItemList == null) {
                AwareLog.e(TAG, "get except apps config error!");
                return;
            }
            for (AwareConfig.Item item : awareConfigItemList) {
                if (item != null) {
                    Map<String, String> configPropertries = item.getProperties();
                    if (configPropertries != null) {
                        String name = configPropertries.get("name");
                        if (name != null && ATTR_IGNORE_APP.equals(name)) {
                            List<AwareConfig.SubItem> subItemList = item.getSubItemList();
                            if (subItemList != null) {
                                for (AwareConfig.SubItem subItem : subItemList) {
                                    String pkgName = subItem.getValue();
                                    if (pkgName != null && !pkgName.isEmpty()) {
                                        exceptList.add(pkgName.trim());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public int readDeviceId(String deviceName) {
        List<AwareConfig.Item> awareConfigItemList = getItemList(deviceName);
        if (awareConfigItemList == null) {
            AwareLog.e(TAG, "get " + deviceName + " device id error!");
            return -1;
        }
        for (AwareConfig.Item item : awareConfigItemList) {
            if (item != null) {
                Map<String, String> configPropertries = item.getProperties();
                if (configPropertries == null) {
                    continue;
                } else {
                    String devIdOrg = configPropertries.get(ITEM_DEVICE_ID);
                    if (devIdOrg == null) {
                        continue;
                    } else {
                        try {
                            return Integer.parseInt(devIdOrg.trim());
                        } catch (NumberFormatException e) {
                            AwareLog.e(TAG, "NumberFormatException, device id :" + devIdOrg);
                        }
                    }
                }
            }
        }
        return -1;
    }
}
