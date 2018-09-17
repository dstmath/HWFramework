package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.rms.iaware.cpu.IAwareMode.LevelCmdId;
import java.util.List;
import java.util.Map;

/* compiled from: CPUXmlConfiguration */
class GameLevelMapConfig extends CPUCustBaseConfig {
    private static final String GAME_ENTER_CONFIG_NAME = "game_enter_level_map";
    private static final String GAME_SCENE_CONFIG_NAME = "game_scene_level_map";
    private static final String LEVEL = "level";
    private static final String TAG = "GameLevelMapConfig";
    private static final String TYPE = "type";

    GameLevelMapConfig() {
    }

    public void setConfig(CPUFeature feature) {
        loadConfig(GAME_SCENE_CONFIG_NAME);
        loadConfig(GAME_ENTER_CONFIG_NAME);
    }

    private void loadConfig(String configName) {
        List<Item> awareConfigItemList = getItemList(configName);
        if (awareConfigItemList == null) {
            AwareLog.w(TAG, "loadConfig config prop is null!");
            return;
        }
        int size = awareConfigItemList.size();
        for (int i = 0; i < size; i++) {
            Item item = (Item) awareConfigItemList.get(i);
            if (item == null) {
                AwareLog.w(TAG, "can not find game level item");
            } else {
                Map<String, String> itemProps = item.getProperties();
                if (itemProps == null) {
                    AwareLog.w(TAG, "can not find game level property");
                } else {
                    String levelType = (String) itemProps.get("type");
                    List<SubItem> subItemList = getSubItem(item);
                    if (subItemList == null) {
                        AwareLog.w(TAG, "get subItem failed type:" + levelType);
                    } else {
                        Map<Integer, LevelCmdId> levelMap = new ArrayMap();
                        for (SubItem subItem : subItemList) {
                            Map<String, String> subItemProps = subItem.getProperties();
                            if (subItemProps != null) {
                                int level = parseInt((String) subItemProps.get("level"));
                                if (level != -1) {
                                    LevelCmdId cmdId = parseLevel(subItem.getValue());
                                    if (cmdId == null) {
                                        AwareLog.w(TAG, "get level failed:" + subItem.getValue());
                                    } else {
                                        levelMap.put(Integer.valueOf(level), cmdId);
                                    }
                                }
                            }
                        }
                        if (GAME_SCENE_CONFIG_NAME.equals(configName)) {
                            IAwareMode.getInstance().initLevelMap(0, levelType, levelMap);
                        } else if (GAME_ENTER_CONFIG_NAME.equals(configName)) {
                            IAwareMode.getInstance().initLevelMap(1, levelType, levelMap);
                        }
                    }
                }
            }
        }
    }

    private LevelCmdId parseLevel(String levelStr) {
        int i = -1;
        if (levelStr == null) {
            return null;
        }
        String[] strParts = levelStr.split(",");
        if (strParts.length != 2) {
            return null;
        }
        LevelCmdId cmdid = new LevelCmdId();
        int longtermCmdId = parseInt(strParts[0]);
        if (longtermCmdId < -1) {
            longtermCmdId = -1;
        }
        cmdid.mLongTermCmdId = longtermCmdId;
        int boostCmdId = parseInt(strParts[1]);
        if (boostCmdId >= -1) {
            i = boostCmdId;
        }
        cmdid.mBoostCmdId = i;
        return cmdid;
    }

    private int parseInt(String intStr) {
        int cmdId = -1;
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse level failed:" + intStr);
            return cmdId;
        }
    }
}
