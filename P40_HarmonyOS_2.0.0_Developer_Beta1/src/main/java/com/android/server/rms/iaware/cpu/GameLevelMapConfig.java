package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.rms.iaware.cpu.AwareMode;
import java.util.List;
import java.util.Map;

/* access modifiers changed from: package-private */
/* compiled from: CpuXmlConfiguration */
public class GameLevelMapConfig extends CpuCustBaseConfig {
    private static final String GAME_ENTER_CONFIG_NAME = "game_enter_level_map";
    private static final String GAME_SCENE_CONFIG_NAME = "game_scene_level_map";
    private static final String LEVEL = "level";
    private static final String TAG = "GameLevelMapConfig";
    private static final String TYPE = "type";

    GameLevelMapConfig() {
    }

    @Override // com.android.server.rms.iaware.cpu.CpuCustBaseConfig
    public void setConfig(CpuFeature feature) {
        loadConfig(GAME_SCENE_CONFIG_NAME);
        loadConfig(GAME_ENTER_CONFIG_NAME);
    }

    private void loadConfig(String configName) {
        int level;
        GameLevelMapConfig gameLevelMapConfig = this;
        List<AwareConfig.Item> awareConfigItemList = getItemList(configName);
        if (awareConfigItemList == null) {
            AwareLog.w(TAG, "loadConfig config prop is null!");
            return;
        }
        for (AwareConfig.Item item : awareConfigItemList) {
            if (item == null) {
                AwareLog.w(TAG, "can not find game level item");
            } else {
                Map<String, String> itemProps = item.getProperties();
                if (itemProps == null) {
                    AwareLog.w(TAG, "can not find game level property");
                } else {
                    String levelType = itemProps.get(TYPE);
                    List<AwareConfig.SubItem> subItemList = gameLevelMapConfig.getSubItem(item);
                    if (subItemList == null) {
                        AwareLog.w(TAG, "get subItem failed type:" + levelType);
                    } else {
                        Map<Integer, AwareMode.LevelCmdId> levelMap = new ArrayMap<>();
                        for (AwareConfig.SubItem subItem : subItemList) {
                            Map<String, String> subItemProps = subItem.getProperties();
                            if (!(subItemProps == null || (level = gameLevelMapConfig.parseInt(subItemProps.get("level"))) == -1)) {
                                AwareMode.LevelCmdId cmdId = gameLevelMapConfig.parseLevel(subItem.getValue());
                                if (cmdId == null) {
                                    AwareLog.w(TAG, "get level failed:" + subItem.getValue());
                                    gameLevelMapConfig = this;
                                } else {
                                    levelMap.put(Integer.valueOf(level), cmdId);
                                    gameLevelMapConfig = this;
                                }
                            }
                        }
                        if (GAME_SCENE_CONFIG_NAME.equals(configName)) {
                            AwareMode.getInstance().initLevelMap(0, levelType, levelMap);
                        } else if (GAME_ENTER_CONFIG_NAME.equals(configName)) {
                            AwareMode.getInstance().initLevelMap(1, levelType, levelMap);
                        } else {
                            AwareLog.d(TAG, "Invalid config name:" + configName);
                        }
                        gameLevelMapConfig = this;
                    }
                }
            }
        }
    }

    private AwareMode.LevelCmdId parseLevel(String levelStr) {
        if (levelStr == null) {
            return null;
        }
        String[] strParts = levelStr.split(",");
        if (strParts.length != 2) {
            return null;
        }
        AwareMode.LevelCmdId cmdId = new AwareMode.LevelCmdId();
        int longtermCmdId = parseInt(strParts[0]);
        int i = -1;
        cmdId.mLongTermCmdId = longtermCmdId < -1 ? -1 : longtermCmdId;
        int boostCmdId = parseInt(strParts[1]);
        if (boostCmdId >= -1) {
            i = boostCmdId;
        }
        cmdId.mBoostCmdId = i;
        return cmdId;
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
