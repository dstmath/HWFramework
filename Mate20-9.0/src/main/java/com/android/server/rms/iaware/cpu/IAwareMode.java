package com.android.server.rms.iaware.cpu;

import android.iawareperf.UniPerf;
import android.os.Bundle;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwaredConnection;
import android.util.ArrayMap;
import com.android.server.location.HwLogRecordManager;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

public class IAwareMode {
    private static final String BOOST_TAG = "boost";
    private static final int ENTER_GAME = 0;
    private static final int EXIT_GAME = 1;
    public static final int GAME_ENTER_TAG = 1;
    private static final String GAME_EXIT_TAG = "isGameExit";
    public static final int GAME_SCENE_TAG = 0;
    public static final int INVALID_INT = -1;
    private static final int LEVEL_EXIT = 0;
    private static final String LEVEL_TYPE_BCPU = "bcpu";
    private static final String LEVEL_TYPE_DDR = "ddr";
    private static final String LEVEL_TYPE_EAS = "eas";
    private static final String LEVEL_TYPE_GOV = "gov";
    private static final String LEVEL_TYPE_GPU = "gpu";
    private static final String LEVEL_TYPE_LCPU = "lcpu";
    private static final String TAG = "IAwareMode";
    private static final Object mLock = new Object();
    private static IAwareMode sInstance = null;
    /* access modifiers changed from: private */
    public boolean mEasEnable = false;
    private Map<String, LevelMng> mGameEnterLevelMngs = new ArrayMap();
    private Map<String, LevelMng> mGameSceneLevelMngs = new ArrayMap();

    private class BigCPULevelMng extends LevelMng {
        public BigCPULevelMng() {
            super();
            this.mType = IAwareMode.LEVEL_TYPE_BCPU;
        }
    }

    private class DDRLevelMng extends LevelMng {
        public DDRLevelMng() {
            super();
            this.mType = IAwareMode.LEVEL_TYPE_DDR;
        }
    }

    private class EASLevelMng extends LevelMng {
        public EASLevelMng() {
            super();
            this.mType = IAwareMode.LEVEL_TYPE_EAS;
        }

        public void doConfig(Bundle bundle) {
            if (IAwareMode.this.mEasEnable) {
                super.doConfig(bundle);
            }
            LevelInfo levelInfo = getLevelInfo(bundle.getString(this.mType));
            if (levelInfo != null) {
                if (levelInfo.mLevel != 0 || !SchedLevelBoost.getInstance().mIsLcpuLimited.get()) {
                    if (levelInfo.mLevel != -1) {
                        int cmdId = getCmdId(levelInfo.mLevel, false);
                        if (cmdId != -1) {
                            int pid = bundle.getInt("pid", -1);
                            if (pid <= 0) {
                                AwareLog.w(IAwareMode.TAG, "pid is invalid:" + pid);
                                return;
                            }
                            ArrayList<Integer> tids = parseTid(bundle.getString("tid"));
                            if (tids != null) {
                                int tidSize = tids.size();
                                ByteBuffer buffer = ByteBuffer.allocate(20 + (tidSize * 4));
                                buffer.putInt(CPUFeature.MSG_GAME_SCENE_LEVEL);
                                buffer.putInt(levelInfo.mLevel);
                                buffer.putInt(cmdId);
                                buffer.putInt(pid);
                                buffer.putInt(tidSize);
                                for (int i = 0; i < tidSize; i++) {
                                    buffer.putInt(tids.get(i).intValue());
                                }
                                sendToIawared(buffer);
                                AwareLog.d(IAwareMode.TAG, "game level " + levelInfo.mLevel + ",cmdid:" + cmdId + "type:" + this.mType + ",pid:" + pid + ",tid:" + tids);
                            } else {
                                return;
                            }
                        }
                    }
                    return;
                }
                if (bundle.getInt(IAwareMode.GAME_EXIT_TAG, -1) == 0) {
                    AwareLog.w(IAwareMode.TAG, "proc level0 but little cpu is limited");
                    SchedLevelBoost.getInstance().enterSchedLevelBoost();
                }
            }
        }

        private ArrayList<Integer> parseTid(String tidStr) {
            if (tidStr == null) {
                return null;
            }
            ArrayList<Integer> tids = new ArrayList<>();
            for (String part : tidStr.split(HwLogRecordManager.VERTICAL_ESC_SEPARATE)) {
                int tidInt = parseInt(part);
                if (tidInt > 0) {
                    tids.add(Integer.valueOf(tidInt));
                }
            }
            return tids;
        }

        private void sendToIawared(ByteBuffer buffer) {
            if (buffer != null && !IAwaredConnection.getInstance().sendPacket(buffer.array(), 0, buffer.position())) {
                AwareLog.e(IAwareMode.TAG, "send to iawared failed");
            }
        }
    }

    private class GPULevelMng extends LevelMng {
        public GPULevelMng() {
            super();
            this.mType = IAwareMode.LEVEL_TYPE_GPU;
        }
    }

    private class GovLevelMng extends LevelMng {
        public GovLevelMng() {
            super();
            this.mType = IAwareMode.LEVEL_TYPE_GOV;
        }
    }

    public static class LevelCmdId {
        public int mBoostCmdId = -1;
        public int mLongTermCmdId = -1;
    }

    private static class LevelInfo {
        public boolean isBoosted;
        public int mLevel;

        private LevelInfo() {
            this.mLevel = -1;
            this.isBoosted = false;
        }
    }

    private class LevelMng {
        protected Bundle mCurConfig;
        Map<Integer, LevelCmdId> mLevelMap;
        protected String mType;

        private LevelMng() {
            this.mType = "";
        }

        /* access modifiers changed from: protected */
        public void initConfig(Map<Integer, LevelCmdId> levelMap) {
            this.mLevelMap = levelMap;
        }

        /* access modifiers changed from: protected */
        public LevelInfo getLevelInfo(String str) {
            if (str == null) {
                return null;
            }
            LevelInfo levelInfo = new LevelInfo();
            if (str.contains(IAwareMode.BOOST_TAG)) {
                String[] strParts = str.split(HwLogRecordManager.VERTICAL_ESC_SEPARATE);
                if (strParts.length != 2) {
                    return null;
                }
                levelInfo.mLevel = parseInt(strParts[0]);
                levelInfo.isBoosted = true;
            } else {
                levelInfo.mLevel = parseInt(str);
            }
            return levelInfo;
        }

        /* access modifiers changed from: protected */
        public int parseInt(String intStr) {
            try {
                return Integer.parseInt(intStr);
            } catch (NumberFormatException e) {
                AwareLog.e(IAwareMode.TAG, "parse level failed:" + intStr);
                return -1;
            }
        }

        /* access modifiers changed from: protected */
        public void doConfig(Bundle bundle) {
            if (bundle == null) {
                AwareLog.w(IAwareMode.TAG, "bundle is null, type:" + this.mType);
                return;
            }
            LevelInfo levelInfo = getLevelInfo(bundle.getString(this.mType));
            if (levelInfo != null) {
                int level = levelInfo.mLevel;
                if (level == 0) {
                    if (this.mCurConfig != null) {
                        applyConfig(getLevelInfo(this.mCurConfig.getString(this.mType)), false);
                        this.mCurConfig = null;
                    }
                } else if (level > 0) {
                    if (this.mCurConfig != null) {
                        applyConfig(getLevelInfo(this.mCurConfig.getString(this.mType)), false);
                    }
                    applyConfig(levelInfo, true);
                    this.mCurConfig = bundle;
                } else {
                    AwareLog.e(IAwareMode.TAG, "level " + level + " is invalid for type:" + this.mType);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void applyConfig(LevelInfo levelInfo, boolean enter) {
            if (levelInfo != null) {
                int cmdId = getCmdId(levelInfo.mLevel, levelInfo.isBoosted);
                int i = -1;
                if (cmdId == -1) {
                    AwareLog.w(IAwareMode.TAG, "can not find cmdId for type " + this.mType + " and level " + levelInfo.mLevel);
                    return;
                }
                AwareLog.d(IAwareMode.TAG, "game level " + levelInfo.mLevel + ",cmdid:" + cmdId + ",type:" + this.mType + ",enter:" + enter);
                UniPerf instance = UniPerf.getInstance();
                int[] iArr = new int[1];
                if (enter) {
                    i = 0;
                }
                iArr[0] = i;
                instance.uniPerfEvent(cmdId, "", iArr);
            }
        }

        /* access modifiers changed from: protected */
        public int getCmdId(int level, boolean isBoosted) {
            int i = -1;
            if (this.mLevelMap == null) {
                AwareLog.e(IAwareMode.TAG, "levelmap is not initialized for type:" + this.mType);
                return -1;
            }
            LevelCmdId cmdId = this.mLevelMap.get(Integer.valueOf(level));
            if (cmdId != null) {
                i = isBoosted ? cmdId.mBoostCmdId : cmdId.mLongTermCmdId;
            }
            return i;
        }
    }

    private class LittleCPULevelMng extends LevelMng {
        public LittleCPULevelMng() {
            super();
            this.mType = IAwareMode.LEVEL_TYPE_LCPU;
        }
    }

    public static IAwareMode getInstance() {
        IAwareMode iAwareMode;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new IAwareMode();
            }
            iAwareMode = sInstance;
        }
        return iAwareMode;
    }

    private IAwareMode() {
        this.mGameSceneLevelMngs.put(LEVEL_TYPE_BCPU, new BigCPULevelMng());
        this.mGameSceneLevelMngs.put(LEVEL_TYPE_LCPU, new LittleCPULevelMng());
        this.mGameSceneLevelMngs.put(LEVEL_TYPE_GPU, new GPULevelMng());
        this.mGameSceneLevelMngs.put(LEVEL_TYPE_DDR, new DDRLevelMng());
        this.mGameSceneLevelMngs.put(LEVEL_TYPE_GOV, new GovLevelMng());
        this.mGameSceneLevelMngs.put(LEVEL_TYPE_EAS, new EASLevelMng());
        this.mGameEnterLevelMngs.put(LEVEL_TYPE_GOV, new GovLevelMng());
    }

    /* access modifiers changed from: package-private */
    public void enable(int mode) {
        AwareLog.d(TAG, "enable curmode=" + mode);
    }

    /* access modifiers changed from: package-private */
    public void modeChange(int mode) {
        AwareLog.d(TAG, "modeChange curmode=" + mode);
    }

    public void initLevelMap(int mapTag, String type, Map<Integer, LevelCmdId> levelMap) {
        if (type == null || levelMap == null) {
            AwareLog.e(TAG, "type or levelmap is null");
            return;
        }
        if (mapTag == 0) {
            LevelMng levelMng = this.mGameSceneLevelMngs.get(type);
            if (levelMng != null) {
                levelMng.initConfig(levelMap);
            }
        } else if (mapTag == 1) {
            LevelMng levelMng2 = this.mGameEnterLevelMngs.get(type);
            if (levelMng2 != null) {
                levelMng2.initConfig(levelMap);
            }
        }
    }

    private void doLevelConfig(Map<String, LevelMng> levelMngs, Bundle bundle) {
        for (Map.Entry<String, LevelMng> entry : levelMngs.entrySet()) {
            LevelMng levelMng = entry.getValue();
            if (levelMng != null) {
                levelMng.doConfig(bundle);
            }
        }
    }

    private void doSceneLevelConfig(Bundle bundle) {
        doLevelConfig(this.mGameSceneLevelMngs, bundle);
    }

    private void doGameEnterLevelConfig(Bundle bundle) {
        doLevelConfig(this.mGameEnterLevelMngs, bundle);
    }

    public void gameLevel(Bundle bundle) {
        if (bundle == null) {
            AwareLog.e(TAG, "game level bundle is null");
            return;
        }
        AwareLog.d(TAG, "adjust game level");
        doSceneLevelConfig(bundle);
    }

    public void gameEnter(Bundle bundle, boolean isEasEnable) {
        this.mEasEnable = isEasEnable;
        if (bundle == null) {
            AwareLog.e(TAG, "game enter bundle is null");
            return;
        }
        AwareLog.d(TAG, "adjust game enter");
        doGameEnterLevelConfig(bundle);
    }
}
