package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.rms.iaware.cpu.IAwareMode;
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
        List<AwareConfig.Item> awareConfigItemList;
        GameLevelMapConfig gameLevelMapConfig = this;
        String str = configName;
        List<AwareConfig.Item> awareConfigItemList2 = getItemList(configName);
        if (awareConfigItemList2 == null) {
            AwareLog.w(TAG, "loadConfig config prop is null!");
            return;
        }
        int size = awareConfigItemList2.size();
        int i = 0;
        while (i < size) {
            AwareConfig.Item item = awareConfigItemList2.get(i);
            if (item == null) {
                AwareLog.w(TAG, "can not find game level item");
            } else {
                Map<String, String> itemProps = item.getProperties();
                if (itemProps == null) {
                    AwareLog.w(TAG, "can not find game level property");
                } else {
                    String levelType = itemProps.get("type");
                    List<AwareConfig.SubItem> subItemList = gameLevelMapConfig.getSubItem(item);
                    if (subItemList == null) {
                        AwareLog.w(TAG, "get subItem failed type:" + levelType);
                    } else {
                        Map<Integer, IAwareMode.LevelCmdId> levelMap = new ArrayMap<>();
                        for (AwareConfig.SubItem subItem : subItemList) {
                            Map<String, String> subItemProps = subItem.getProperties();
                            if (subItemProps != null) {
                                int level = gameLevelMapConfig.parseInt(subItemProps.get("level"));
                                if (level != -1) {
                                    IAwareMode.LevelCmdId cmdId = gameLevelMapConfig.parseLevel(subItem.getValue());
                                    if (cmdId == null) {
                                        AwareLog.w(TAG, "get level failed:" + subItem.getValue());
                                        awareConfigItemList2 = awareConfigItemList2;
                                    } else {
                                        List<AwareConfig.Item> list = awareConfigItemList2;
                                        levelMap.put(Integer.valueOf(level), cmdId);
                                    }
                                    gameLevelMapConfig = this;
                                }
                            }
                        }
                        awareConfigItemList = awareConfigItemList2;
                        if (GAME_SCENE_CONFIG_NAME.equals(str)) {
                            IAwareMode.getInstance().initLevelMap(0, levelType, levelMap);
                        } else if (GAME_ENTER_CONFIG_NAME.equals(str)) {
                            IAwareMode.getInstance().initLevelMap(1, levelType, levelMap);
                        }
                        i++;
                        awareConfigItemList2 = awareConfigItemList;
                        gameLevelMapConfig = this;
                    }
                }
            }
            awareConfigItemList = awareConfigItemList2;
            i++;
            awareConfigItemList2 = awareConfigItemList;
            gameLevelMapConfig = this;
        }
    }

    private IAwareMode.LevelCmdId parseLevel(String levelStr) {
        if (levelStr == null) {
            return null;
        }
        String[] strParts = levelStr.split(",");
        if (strParts.length != 2) {
            return null;
        }
        IAwareMode.LevelCmdId cmdid = new IAwareMode.LevelCmdId();
        int longtermCmdId = parseInt(strParts[0]);
        int i = -1;
        cmdid.mLongTermCmdId = longtermCmdId < -1 ? -1 : longtermCmdId;
        int boostCmdId = parseInt(strParts[1]);
        if (boostCmdId >= -1) {
            i = boostCmdId;
        }
        cmdid.mBoostCmdId = i;
        return cmdid;
    }

    private int parseInt(String intStr) {
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse level failed:" + intStr);
            return -1;
        }
    }
}
