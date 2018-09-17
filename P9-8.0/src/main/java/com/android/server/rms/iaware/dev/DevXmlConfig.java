package com.android.server.rms.iaware.dev;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import android.rms.iaware.ICMSManager;
import android.rms.iaware.ICMSManager.Stub;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DevXmlConfig {
    private static final String ATTR_TYPE = "switch";
    private static final String CMS_SERVICE = "IAwareCMSService";
    private static final String CONFIG_SUBSWITCH = "sub_switch";
    private static final String DEV_FEATURE_NAME = "DevSchedFeature";
    private static final String ITEM_BIT = "bit";
    private static final String ITEM_SCENE_ID = "scene_id";
    private static final String ITEM_SCENE_NAME = "scenename";
    private static final String ITEM_TYPE = "type";
    private static final String TAG = "DevXmlConfig";
    private static ICMSManager mCmsManager;

    public boolean readDevStrategy(Map<Integer, SceneInfo> sceneMap, String configName, Map<String, ArrayList<String>> sceneRullMap) {
        if (sceneMap == null || configName == null || sceneRullMap == null) {
            AwareLog.e(TAG, "NetLocationSchedFeature para null");
            return false;
        }
        List<Item> awareConfigItemList = getItemList(configName);
        if (awareConfigItemList == null) {
            AwareLog.e(TAG, "NetLocationSchedFeature read strategy config error!");
            return false;
        }
        for (Item item : awareConfigItemList) {
            if (item == null) {
                AwareLog.e(TAG, "NetLocationSchedFeature item null");
                return false;
            }
            Map<String, String> configPropertries = item.getProperties();
            if (configPropertries == null || configPropertries.isEmpty()) {
                AwareLog.e(TAG, "NetLocationSchedFeature configPropertries null");
                return false;
            }
            String sceneName = (String) configPropertries.get(ITEM_SCENE_NAME);
            if (sceneName == null || sceneName.isEmpty()) {
                AwareLog.e(TAG, "NetLocationSchedFeature sceneName null");
                return false;
            }
            ArrayList<String> ruleList = (ArrayList) sceneRullMap.get(sceneName);
            if (ruleList == null || ruleList.isEmpty()) {
                AwareLog.e(TAG, "NetLocationSchedFeature ruleList null");
                return false;
            }
            try {
                if (!createMapLocal(item, sceneName, Integer.parseInt((String) configPropertries.get(ITEM_SCENE_ID)), sceneMap, ruleList)) {
                    return false;
                }
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, " NumberFormatException, sceneid is " + ((String) configPropertries.get(ITEM_SCENE_ID)));
                return false;
            }
        }
        return true;
    }

    private boolean createMapLocal(Item item, String sceneName, int sceneId, Map<Integer, SceneInfo> sceneMap, List<String> ruleList) {
        SceneInfo sceneObj = new SceneInfo(sceneName, sceneId);
        List<SubItem> subItemList = item.getSubItemList();
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
            List<Item> itemList = getItemList(CONFIG_SUBSWITCH);
            if (itemList != null) {
                for (Item item : itemList) {
                    if (item != null) {
                        Map<String, String> configPropertries = item.getProperties();
                        if (configPropertries != null) {
                            String itemType = (String) configPropertries.get("type");
                            if (itemType != null && itemType.equals(ATTR_TYPE)) {
                                parseSubSwitchFromSubItem(item, subFeatureSwitch);
                            }
                        }
                    }
                }
            }
        }
    }

    public static List<Item> getItemList(String configName) {
        if (mCmsManager == null) {
            mCmsManager = Stub.asInterface(ServiceManager.getService(CMS_SERVICE));
        }
        AwareConfig awareConfig = null;
        try {
            if (mCmsManager != null) {
                awareConfig = mCmsManager.getCustConfig(DEV_FEATURE_NAME, configName);
                AwareLog.d(TAG, "DevXmlBaseConfiguration");
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

    private static void parseSubSwitchFromSubItem(Item item, Map<String, String> subFeatureSwitch) {
        if (item != null && subFeatureSwitch != null) {
            List<SubItem> subItemList = item.getSubItemList();
            if (subItemList == null) {
                AwareLog.e(TAG, "can not get subswitch config subitem");
                return;
            }
            for (SubItem subItem : subItemList) {
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
}
