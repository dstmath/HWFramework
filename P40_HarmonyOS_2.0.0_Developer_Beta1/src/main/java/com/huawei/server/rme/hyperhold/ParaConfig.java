package com.huawei.server.rme.hyperhold;

import android.os.Environment;
import android.os.StatFs;
import android.util.Slog;
import com.android.internal.util.MemInfoReader;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.gesture.DefaultGestureNavConst;
import com.android.server.hidata.arbitration.HwArbitrationDefs;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.wifipro.WifiProCommonDefs;
import com.huawei.aod.AodThemeConst;
import com.huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public final class ParaConfig {
    private static final int MAX_USE_DAY_COUNT = 60;
    private static final String NAND_LIFE_FILE_PATH = "/data/system/HyperHoldNandLife";
    private static final String SWAP_RATIO_BGACTIVE_FILE_PATH = "/data/system/HyperHoldBgActive";
    private static final String SWAP_RATIO_USERDATA_FILE_PATH = "/data/system/HyperHoldUserData";
    private static final String TAG = "SWAP_ParaConfig";
    private static int endStorageValue = 5;
    private static volatile boolean hasReadXml = false;
    private static volatile ParaConfig paraConfig = null;
    private static boolean swapEnable = false;
    private static int swapMinRamSize = 6;
    private static int swapMinRomSize = 128;
    private static boolean swapOutEnable = false;
    private Set<String> activeAppList = Collections.synchronizedSet(new HashSet());
    private ActiveRatioParam activeRatioParam = new ActiveRatioParam(60, 10, 50);
    private AdvancedKillParam akParam = new AdvancedKillParam(false, false);
    private BgActSwapRatioParam bgActSwapRatioParam = new BgActSwapRatioParam(0, 0, 20, 100);
    private BufferSizeParam bufferSizeParam = new BufferSizeParam(1000, 950, HwArbitrationDefs.MSG_NOTIFY_CURRENT_NETWORK, 1050, 200);
    private List<RatioGroup> freezeRatioGroupList = new ArrayList();
    private FreezeRatioParam freezeRatioParam = new FreezeRatioParam(100, 100, 100);
    private FreqUseRatioParam freqUseRatioParam = new FreqUseRatioParam(0, 0, 0, 50);
    private FrontRatioParam frontRatioParam = new FrontRatioParam(0, 0, 50);
    private KillParam killParam = new KillParam(400, -1, "buffer size", 5, 400);
    private KillParamOpt killParamOpt = new KillParamOpt(400, 400, 0, 400);
    private OtherParam otherParam = new OtherParam(false, false, 22, "");
    private PsiEventParam psiEventParam = new PsiEventParam(70, 1000);
    private PsiParam psiParam = new PsiParam(true, 100);
    private RootMemcgParam rootMemcgParam = new RootMemcgParam(699998208, 300, 60, 10, 0);
    private ScoreRatioParam scoreRatioParam = new ScoreRatioParam(200, 20, 0, 100);
    private Map<String, String> specialAppGroupMap = new ConcurrentHashMap();
    private Map<String, SpecialParam> specialParamMap = new ConcurrentHashMap();
    private StorageLifeParam storageLifeParam = new StorageLifeParam();
    private Map<Integer, AdjustRatioGroup> swapIndexGroupMap = new ConcurrentHashMap();
    private SwapRatioThresParam swapRatioThresParam = new SwapRatioThresParam(100, 1000);
    private HashMap<String, WhiteListParam> whiteList = new HashMap<>();
    private ZswapdPress zswapdPress = new ZswapdPress(-1, -1);

    private ParaConfig() {
    }

    public static ParaConfig getInstance() {
        if (paraConfig == null || !hasReadXml) {
            synchronized (ParaConfig.class) {
                if (paraConfig == null) {
                    paraConfig = new ParaConfig();
                }
                if (!hasReadXml) {
                    boolean readXmlSuccess = paraConfig.readParaFromXml("xml/hyperhold_config.xml");
                    if (new File("/data/cota/para/HYPERHOLD/hyperhold_config.xml").exists()) {
                        Slog.i(TAG, "start read xml from /data/cota/para/HYPERHOLD/hyperhold_config.xml");
                        readXmlSuccess &= paraConfig.readParaFromXml("/data/cota/para/HYPERHOLD/hyperhold_config.xml");
                    }
                    swapEnable = swapEnable && readXmlSuccess;
                    hasReadXml = true;
                }
            }
        }
        return paraConfig;
    }

    public static void resetHasReadXml() {
        hasReadXml = false;
    }

    private int parseIntValue(String key, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Parse " + key + " error, inputValue = " + value);
            return 0;
        }
    }

    public boolean getSwapEnable() {
        return swapEnable;
    }

    public static class PsiParam {
        private int psiDelay;
        private boolean psiOpen;

        private PsiParam(boolean psiOpen2, int psiDelay2) {
            this.psiOpen = psiOpen2;
            this.psiDelay = psiDelay2;
        }

        public boolean getPsiOpen() {
            return this.psiOpen;
        }

        public int getPsiDelay() {
            return this.psiDelay;
        }
    }

    private void setPsiParam(Map<String, String> param) {
        boolean psiOpen = false;
        int psiDelay = 0;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            if ("psiOpen".equals(key) && AppActConstant.VALUE_TRUE.equalsIgnoreCase(value)) {
                psiOpen = true;
            }
            if ("psiDelay".equals(key)) {
                psiDelay = parseIntValue(key, value);
            }
        }
        this.psiParam = new PsiParam(psiOpen, psiDelay);
    }

    public PsiParam getPsiParam() {
        return this.psiParam;
    }

    public static class StorageLifeParam {
        private int dailySwapOutQuotaMax = 99;
        private int leftLife = -1;
        private int lifeTimeEstTypA = 2;
        private int lifeTimeEstTypB = 2;
        private int lifeTimePreEolInfo = 2;
        private int swapControlDays = 7;
        private int swapDeclineRatio = 10;
        private int swapFinalLimit = 20;
        private int swapOriginalAmount = 50;
        private int swapRedDeclineRatio = 5;
        private int wbLifeTimeEstTypA = 6;
        private int wbLifeTimeEstTypB = 6;
        private int wbLifeTimePreEolInfo = 2;

        StorageLifeParam() {
        }

        public void setSwapLifeParam(int swapOriginalAmount2, int swapDeclineRatio2, int swapFinalLimit2, int swapControlDays2, int swapRedDeclineRatio2) {
            this.swapOriginalAmount = swapOriginalAmount2;
            this.swapDeclineRatio = swapDeclineRatio2;
            this.swapFinalLimit = swapFinalLimit2;
            this.swapControlDays = swapControlDays2;
            this.swapRedDeclineRatio = swapRedDeclineRatio2;
        }

        public void setLifeTimeParam(int lifeTimeEstTypA2, int lifeTimeEstTypB2, int lifeTimePreEolInfo2) {
            this.lifeTimeEstTypA = lifeTimeEstTypA2;
            this.lifeTimeEstTypB = lifeTimeEstTypB2;
            this.lifeTimePreEolInfo = lifeTimePreEolInfo2;
        }

        public void setWbLifeTimeParam(int wbLifeTimeEstTypA2, int wbLifeTimeEstTypB2, int wbLifeTimePreEolInfo2) {
            this.wbLifeTimeEstTypA = wbLifeTimeEstTypA2;
            this.wbLifeTimeEstTypB = wbLifeTimeEstTypB2;
            this.wbLifeTimePreEolInfo = wbLifeTimePreEolInfo2;
        }

        public void setDailySwapOutQuotaMax(int value) {
            this.dailySwapOutQuotaMax = value;
        }

        public int getDailySwapOutQuotaMax() {
            return this.dailySwapOutQuotaMax;
        }

        public int getPeriodSwapOutQuotaMax() {
            int lifeLeftRatio = getLeftOfLife();
            if (lifeLeftRatio >= 11) {
                return 0;
            }
            int result = this.swapOriginalAmount - (this.swapDeclineRatio * lifeLeftRatio);
            if (result < this.swapFinalLimit) {
                result = this.swapFinalLimit;
            }
            int result2 = result * this.swapControlDays;
            return result2 < 0 ? -result2 : result2;
        }

        public int getLeftOfLife() {
            int curLifeTimeEstA = KernelInterface.getInstance().readLifeTimeEstA();
            this.leftLife = Math.max(this.leftLife, KernelInterface.getInstance().readLifeTimeEstB());
            this.leftLife = Math.max(this.leftLife, curLifeTimeEstA);
            int i = this.leftLife;
            if (i <= 0 || i >= 11) {
                this.leftLife = 11;
            }
            return this.leftLife - 1;
        }

        public int getSwapOriginalAmount() {
            return this.swapOriginalAmount;
        }

        public int getSwapDeclineRatio() {
            return this.swapDeclineRatio;
        }

        public int getSwapFinalLimit() {
            return this.swapFinalLimit;
        }

        public int getSwapControlDays() {
            return this.swapControlDays;
        }

        public int getSwapRedDeclineRatio() {
            return this.swapRedDeclineRatio;
        }

        public int getLifeTimeEstTypA() {
            return this.lifeTimeEstTypA;
        }

        public int getLifeTimeEstTypB() {
            return this.lifeTimeEstTypB;
        }

        public int getLifeTimePreEolInfo() {
            return this.lifeTimePreEolInfo;
        }

        public int getWbLifeTimeEstTypA() {
            return this.wbLifeTimeEstTypA;
        }

        public int getWbLifeTimeEstTypB() {
            return this.wbLifeTimeEstTypB;
        }

        public int getWbLifeTimePreEolInfo() {
            return this.wbLifeTimePreEolInfo;
        }
    }

    public StorageLifeParam getStorageLifeParam() {
        return this.storageLifeParam;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void setStorageSwapParam(Map<String, String> param) {
        char c;
        int dailySwapOutQuotaMax = 0;
        int swapOriginalAmount = 0;
        int swapDeclineRatio = 0;
        int swapFinalLimit = 0;
        int swapControlDays = 0;
        int swapRedDeclineRatio = 0;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            switch (key.hashCode()) {
                case -1736014340:
                    if (key.equals("swapOriginalAmount")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -572192792:
                    if (key.equals("swapDeclineRatio")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -377286701:
                    if (key.equals("swapRedDeclineRatio")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 372031425:
                    if (key.equals("swapControlDays")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 577393230:
                    if (key.equals("dailySwapOutQuotaMax")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1790404056:
                    if (key.equals("swapFinalLimit")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                dailySwapOutQuotaMax = parseIntValue(key, value);
            } else if (c == 1) {
                swapOriginalAmount = parseIntValue(key, value);
            } else if (c == 2) {
                swapDeclineRatio = parseIntValue(key, value);
            } else if (c == 3) {
                swapFinalLimit = parseIntValue(key, value);
            } else if (c == 4) {
                swapControlDays = parseIntValue(key, value);
            } else if (c == 5) {
                swapRedDeclineRatio = parseIntValue(key, value);
            }
        }
        this.storageLifeParam.setDailySwapOutQuotaMax(dailySwapOutQuotaMax);
        this.storageLifeParam.setSwapLifeParam(swapOriginalAmount, swapDeclineRatio, swapFinalLimit, swapControlDays, swapRedDeclineRatio);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void setStorageLifeTimeParam(Map<String, String> param) {
        char c;
        int lifeTimeEstTypA = 0;
        int lifeTimeEstTypB = 0;
        int lifeTimePreEolInfo = 0;
        int wbLifeTimeEstTypA = 0;
        int wbLifeTimeEstTypB = 0;
        int wbLifeTimePreEolInfo = 0;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            switch (key.hashCode()) {
                case -2142209119:
                    if (key.equals("wbLifeTimePreEolInfo")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -1424446090:
                    if (key.equals("lifeTimePreEolInfo")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -724741741:
                    if (key.equals("lifeTimeEstTypA")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -724741740:
                    if (key.equals("lifeTimeEstTypB")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1915562824:
                    if (key.equals("wbLifeTimeEstTypA")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1915562825:
                    if (key.equals("wbLifeTimeEstTypB")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                lifeTimeEstTypA = parseIntValue(key, value);
            } else if (c == 1) {
                lifeTimeEstTypB = parseIntValue(key, value);
            } else if (c == 2) {
                lifeTimePreEolInfo = parseIntValue(key, value);
            } else if (c == 3) {
                wbLifeTimeEstTypA = parseIntValue(key, value);
            } else if (c == 4) {
                wbLifeTimeEstTypB = parseIntValue(key, value);
            } else if (c == 5) {
                wbLifeTimePreEolInfo = parseIntValue(key, value);
            }
        }
        this.storageLifeParam.setLifeTimeParam(lifeTimeEstTypA, lifeTimeEstTypB, lifeTimePreEolInfo);
        this.storageLifeParam.setWbLifeTimeParam(wbLifeTimeEstTypA, wbLifeTimeEstTypB, wbLifeTimePreEolInfo);
    }

    public static class PsiEventParam {
        private int threshold;
        private int window;

        PsiEventParam(int threshold2, int window2) {
            this.threshold = threshold2;
            this.window = window2;
        }

        public int getThreshold() {
            return this.threshold;
        }

        public int getWindow() {
            return this.window;
        }
    }

    private void setPsievent(Map<String, String> param) {
        int threshold = 70;
        int window = 1000;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || value == null) {
                Slog.e(TAG, "setPsievent: read key or value null.");
            } else {
                char c = 65535;
                int hashCode = key.hashCode();
                if (hashCode != -1545477013) {
                    if (hashCode == -787751952 && key.equals("window")) {
                        c = 1;
                    }
                } else if (key.equals("threshold")) {
                    c = 0;
                }
                if (c == 0) {
                    threshold = parseIntValue(key, value);
                } else if (c != 1) {
                    Slog.e(TAG, "Unexpected key:" + key);
                } else {
                    window = parseIntValue(key, value);
                }
            }
        }
        Slog.i(TAG, "setPsievent, threshold:" + threshold + " window:" + window);
        this.psiEventParam = new PsiEventParam(threshold, window);
    }

    public PsiEventParam getPsiEventParam() {
        return this.psiEventParam;
    }

    public static class RootMemcgParam {
        private int rootAppScore;
        private int rootReclaimRatio;
        private int rootReclaimRefault;
        private int rootSoftLimit;
        private int rootSwapRatio;

        private RootMemcgParam(int rootMemcgSoftLimit, int rootMemcgAppScore, int rootMemcgReclaimRatio, int rootMemcgSwapRatio, int rootMemcgReclaimRefault) {
            this.rootSoftLimit = rootMemcgSoftLimit;
            this.rootAppScore = rootMemcgAppScore;
            this.rootReclaimRatio = rootMemcgReclaimRatio;
            this.rootSwapRatio = rootMemcgSwapRatio;
            this.rootReclaimRefault = rootMemcgReclaimRefault;
        }

        public int getRootSoftLimit() {
            return this.rootSoftLimit;
        }

        public int getRootAppScore() {
            return this.rootAppScore;
        }

        public int getRootReclaimRatio() {
            return this.rootReclaimRatio;
        }

        public int getRootSwapRatio() {
            return this.rootSwapRatio;
        }

        public int getRootReclaimRefault() {
            return this.rootReclaimRefault;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void setRootMemcgParam(Map<String, String> param) {
        char c;
        int rootSoftLimit = 0;
        int rootAppScore = 0;
        int rootReclaimRatio = 0;
        int rootSwapRatio = 0;
        int rootReclaimRefault = 0;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            switch (key.hashCode()) {
                case -2082669784:
                    if (key.equals("rootReclaimRefault")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -944705809:
                    if (key.equals("rootSoftLimit")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -138883210:
                    if (key.equals("rootSwapRatio")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1608836083:
                    if (key.equals("rootAppScore")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1937389988:
                    if (key.equals("rootReclaimRatio")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                rootSoftLimit = parseIntValue(key, value);
            } else if (c == 1) {
                rootAppScore = parseIntValue(key, value);
            } else if (c == 2) {
                rootReclaimRatio = parseIntValue(key, value);
            } else if (c == 3) {
                rootSwapRatio = parseIntValue(key, value);
            } else if (c != 4) {
                Slog.e(TAG, "Unexpected key:" + key);
            } else {
                rootReclaimRefault = parseIntValue(key, value);
            }
        }
        this.rootMemcgParam = new RootMemcgParam(rootSoftLimit, rootAppScore, rootReclaimRatio, rootSwapRatio, rootReclaimRefault);
    }

    public RootMemcgParam getRootMemcgParam() {
        return this.rootMemcgParam;
    }

    public static class KillParam {
        private int appReqKillMemMax;
        private int appReqKillThreshold;
        private int appThreshold;
        private int bigKillMem;
        private int killLimit;
        private String killParamRef;
        private int killThreshold;
        private int maxApplication;

        private KillParam(int killThreshold2, int killLimit2, String killParamRef2, int appThreshold2, int bigKillMem2) {
            this.killThreshold = killThreshold2;
            this.killLimit = killLimit2;
            this.killParamRef = killParamRef2;
            this.appThreshold = appThreshold2;
            this.bigKillMem = bigKillMem2;
            this.maxApplication = -1;
            this.appReqKillThreshold = 600;
            this.appReqKillMemMax = DefaultGestureNavConst.CHECK_AFT_TIMEOUT;
        }

        public int getKillThreshold() {
            return this.killThreshold;
        }

        public int getKillLimit() {
            return this.killLimit;
        }

        public String getKillParamRef() {
            return this.killParamRef;
        }

        public int getAppThreshold() {
            return this.appThreshold;
        }

        public int getBigKillMem() {
            return this.bigKillMem;
        }

        public int getMaxApplication() {
            return this.maxApplication;
        }

        public int getAppReqKillThreshold() {
            return this.appReqKillThreshold;
        }

        public int getAppReqKillMemMax() {
            return this.appReqKillMemMax;
        }

        public void setMaxApplication(int maxApplication2) {
            this.maxApplication = maxApplication2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setAppReqParam(int appReqKillThreshold2, int appReqKillMemMax2) {
            this.appReqKillThreshold = appReqKillThreshold2;
            this.appReqKillMemMax = appReqKillMemMax2;
        }
    }

    private void setKillParam(Map<String, String> param) {
        int killThreshold = 0;
        int killLimit = 0;
        String killParamRef = "";
        int appThreshold = 0;
        int bigKillMem = 0;
        int maxApplication = -1;
        int appReqKillThreshold = 0;
        int appReqKillMemMax = 0;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            char c = 65535;
            switch (key.hashCode()) {
                case -1581085955:
                    if (key.equals("killLimit")) {
                        c = 1;
                        break;
                    }
                    break;
                case -1578240272:
                    if (key.equals("appReqKillThreshold")) {
                        c = 6;
                        break;
                    }
                    break;
                case -802570518:
                    if (key.equals("appReqKillMemMax")) {
                        c = 7;
                        break;
                    }
                    break;
                case -747916627:
                    if (key.equals("killThreshold")) {
                        c = 0;
                        break;
                    }
                    break;
                case -429424308:
                    if (key.equals("maxApplication")) {
                        c = 5;
                        break;
                    }
                    break;
                case 164847434:
                    if (key.equals("appThreshold")) {
                        c = 3;
                        break;
                    }
                    break;
                case 781492804:
                    if (key.equals("killParamRef")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1485588599:
                    if (key.equals("bigKillMem")) {
                        c = 4;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    killThreshold = parseIntValue(key, value);
                    break;
                case 1:
                    killLimit = parseIntValue(key, value);
                    break;
                case 2:
                    killParamRef = value;
                    break;
                case 3:
                    appThreshold = parseIntValue(key, value);
                    break;
                case 4:
                    bigKillMem = parseIntValue(key, value);
                    break;
                case 5:
                    maxApplication = parseIntValue(key, value);
                    break;
                case 6:
                    appReqKillThreshold = parseIntValue(key, value);
                    break;
                case 7:
                    appReqKillMemMax = parseIntValue(key, value);
                    break;
                default:
                    Slog.e(TAG, "Unexpected key:" + key);
                    break;
            }
        }
        this.killParam = new KillParam(killThreshold, killLimit, killParamRef, appThreshold, bigKillMem);
        this.killParam.setMaxApplication(maxApplication);
        this.killParam.setAppReqParam(appReqKillThreshold, appReqKillMemMax);
    }

    public KillParam getKillParam() {
        return this.killParam;
    }

    public static class KillParamOpt {
        private int bufferScreenOffChange;
        private int futileCloseDelay;
        private int futileKillThreshold;
        private int futileKillTimeThreshold;
        private int killWeightLimitNormal;
        private int killWeightLimitQuick;
        private int levelThree;
        private int levelTwo;
        private int lowEndNormalKill;
        private int lowEndQuickKillEnable;
        private int screenOffQuickBuffer;
        private int ubSortWeightLimitNormal;
        private int ubSortWeightLimitQuick;

        private KillParamOpt(int levelTwo2, int levelThree2, int bufferScreenOffChange2, int screenOffQuickBuffer2) {
            this.levelTwo = levelTwo2;
            this.levelThree = levelThree2;
            this.bufferScreenOffChange = bufferScreenOffChange2;
            this.screenOffQuickBuffer = screenOffQuickBuffer2;
        }

        public int getLevelTwo() {
            return this.levelTwo;
        }

        public int getLevelThree() {
            return this.levelThree;
        }

        public int getBufferScreenOffChange() {
            return this.bufferScreenOffChange;
        }

        public int getScreenOffQuickBuffer() {
            return this.screenOffQuickBuffer;
        }

        public int getLowEndNormalKill() {
            return this.lowEndNormalKill;
        }

        public int getLowEndQuickKillEnable() {
            return this.lowEndQuickKillEnable;
        }

        public int getUbSortWeightLimitNormal() {
            return this.ubSortWeightLimitNormal;
        }

        public int getUbSortWeightLimitQuick() {
            return this.ubSortWeightLimitQuick;
        }

        public int getKillWeightLimitNormal() {
            return this.killWeightLimitNormal;
        }

        public int getKillWeightLimitQuick() {
            return this.killWeightLimitQuick;
        }

        public int getFutileKillThreshold() {
            return this.futileKillThreshold;
        }

        public int getFutileKillTimeThreshold() {
            return this.futileKillTimeThreshold;
        }

        public int getFutileCloseDelay() {
            return this.futileCloseDelay;
        }

        public void setLowEnd(int lowEndNormalKill2, int ubSortWeightLimitNormal2, int killWeightLimitNormal2) {
            this.lowEndNormalKill = lowEndNormalKill2;
            this.ubSortWeightLimitNormal = ubSortWeightLimitNormal2;
            this.killWeightLimitNormal = killWeightLimitNormal2;
        }

        public void setLowEndQuick(int lowEndQuickKillEnable2, int ubSortWeightLimitQuick2, int killWeightLimitQuick2) {
            this.lowEndQuickKillEnable = lowEndQuickKillEnable2;
            this.ubSortWeightLimitQuick = ubSortWeightLimitQuick2;
            this.killWeightLimitQuick = killWeightLimitQuick2;
        }

        public void setFutileKill(int futileKillThreshold2, int futileKillTimeThreshold2, int futileCloseDelay2) {
            this.futileKillThreshold = futileKillThreshold2;
            this.futileKillTimeThreshold = futileKillTimeThreshold2;
            this.futileCloseDelay = futileCloseDelay2;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void setKillParamOpt(Map<String, String> param) {
        char c;
        int levelTwo = 0;
        int levelThree = 0;
        int bufferScreenOffChange = 0;
        int screenOffQuickBuffer = 0;
        int lowEndNormalKill = 0;
        int lowEndQuickKillEnable = 0;
        int ubSortWeightLimitNormal = 0;
        int ubSortWeightLimitQuick = 0;
        int killWeightLimitNormal = Integer.MAX_VALUE;
        int killWeightLimitQuick = Integer.MAX_VALUE;
        int futileKillThreshold = 3;
        int futileKillTimeThreshold = Constant.SEND_STALL_MSG_DELAY;
        int futileCloseDelay = WifiProCommonDefs.QUERY_TIMEOUT_MS;
        for (Iterator<Map.Entry<String, String>> it = param.entrySet().iterator(); it.hasNext(); it = it) {
            Map.Entry<String, String> entry = it.next();
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            switch (key.hashCode()) {
                case -2131636184:
                    if (key.equals("levelTwo")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1419595990:
                    if (key.equals("screenOffQuickBuffer")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1203527965:
                    if (key.equals("futileKillTimeThreshold")) {
                        c = 11;
                        break;
                    }
                    c = 65535;
                    break;
                case -1072239521:
                    if (key.equals("ubSortWeightLimitNormal")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -827212697:
                    if (key.equals("lowEndQuickKillEnable")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -495442512:
                    if (key.equals("futileKillThreshold")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case 106899317:
                    if (key.equals("ubSortWeightLimitQuick")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 179733804:
                    if (key.equals("lowEndNormalKill")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 196586618:
                    if (key.equals("levelThree")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 356321864:
                    if (key.equals("futileCloseDelay")) {
                        c = '\f';
                        break;
                    }
                    c = 65535;
                    break;
                case 574165804:
                    if (key.equals("killWeightLimitNormal")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 1517277843:
                    if (key.equals("bufferScreenOffChange")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 2099671816:
                    if (key.equals("killWeightLimitQuick")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    levelTwo = parseIntValue(key, value);
                    futileKillThreshold = futileKillThreshold;
                    futileKillTimeThreshold = futileKillTimeThreshold;
                    break;
                case 1:
                    levelThree = parseIntValue(key, value);
                    futileKillThreshold = futileKillThreshold;
                    futileKillTimeThreshold = futileKillTimeThreshold;
                    break;
                case 2:
                    bufferScreenOffChange = parseIntValue(key, value);
                    futileKillThreshold = futileKillThreshold;
                    futileKillTimeThreshold = futileKillTimeThreshold;
                    break;
                case 3:
                    screenOffQuickBuffer = parseIntValue(key, value);
                    futileKillThreshold = futileKillThreshold;
                    futileKillTimeThreshold = futileKillTimeThreshold;
                    break;
                case 4:
                    lowEndNormalKill = parseIntValue(key, value);
                    futileKillThreshold = futileKillThreshold;
                    futileKillTimeThreshold = futileKillTimeThreshold;
                    break;
                case 5:
                    lowEndQuickKillEnable = parseIntValue(key, value);
                    futileKillThreshold = futileKillThreshold;
                    futileKillTimeThreshold = futileKillTimeThreshold;
                    break;
                case 6:
                    ubSortWeightLimitNormal = parseIntValue(key, value);
                    futileKillThreshold = futileKillThreshold;
                    futileKillTimeThreshold = futileKillTimeThreshold;
                    break;
                case 7:
                    ubSortWeightLimitQuick = parseIntValue(key, value);
                    futileKillThreshold = futileKillThreshold;
                    futileKillTimeThreshold = futileKillTimeThreshold;
                    break;
                case '\b':
                    killWeightLimitNormal = parseIntValue(key, value);
                    futileKillThreshold = futileKillThreshold;
                    futileKillTimeThreshold = futileKillTimeThreshold;
                    break;
                case '\t':
                    killWeightLimitQuick = parseIntValue(key, value);
                    futileKillThreshold = futileKillThreshold;
                    futileKillTimeThreshold = futileKillTimeThreshold;
                    break;
                case '\n':
                    futileKillThreshold = parseIntValue(key, value);
                    futileKillTimeThreshold = futileKillTimeThreshold;
                    break;
                case 11:
                    futileKillTimeThreshold = parseIntValue(key, value);
                    futileKillThreshold = futileKillThreshold;
                    break;
                case '\f':
                    futileCloseDelay = parseIntValue(key, value);
                    futileKillThreshold = futileKillThreshold;
                    futileKillTimeThreshold = futileKillTimeThreshold;
                    break;
                default:
                    Slog.e(TAG, "Unexpected key:" + key);
                    futileKillThreshold = futileKillThreshold;
                    futileKillTimeThreshold = futileKillTimeThreshold;
                    break;
            }
        }
        this.killParamOpt = new KillParamOpt(levelTwo, levelThree, bufferScreenOffChange, screenOffQuickBuffer);
        this.killParamOpt.setLowEnd(lowEndNormalKill, ubSortWeightLimitNormal, killWeightLimitNormal);
        this.killParamOpt.setLowEndQuick(lowEndQuickKillEnable, ubSortWeightLimitQuick, killWeightLimitQuick);
        this.killParamOpt.setFutileKill(futileKillThreshold, futileKillTimeThreshold, futileCloseDelay);
    }

    public KillParamOpt getKillParamOpt() {
        return this.killParamOpt;
    }

    public static class BufferSizeParam {
        private int appStartDelay;
        private int defaultSize;
        private int highSize;
        private int lowSize;
        private int maxBuffer;
        private int minBuffer;
        private int raiseBufferMax;
        private int raiseBufferStep;
        private int raiseBufferTimeWidth;
        private int reclaimDelay;
        private int reclaimFailCount;
        private int reclaimFailWindow;
        private int swapReserve;
        private int upperBuffer;

        private BufferSizeParam(int defaultSize2, int lowSize2, int upperBuffer2, int highSize2, int swapReserve2) {
            this.defaultSize = defaultSize2;
            this.lowSize = lowSize2;
            this.highSize = highSize2;
            this.upperBuffer = upperBuffer2;
            this.swapReserve = swapReserve2;
            this.minBuffer = 950;
            this.maxBuffer = 1900;
            this.appStartDelay = 1;
            this.reclaimDelay = 2;
            this.reclaimFailWindow = 5;
            this.reclaimFailCount = 5;
            this.raiseBufferTimeWidth = 10;
            this.raiseBufferMax = 1000;
            this.raiseBufferStep = 10;
            Slog.i(ParaConfig.TAG, "BufferParaInit:" + defaultSize2 + "," + lowSize2 + "," + upperBuffer2 + "," + highSize2 + "," + swapReserve2);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setBufferRange(int minBuffer2, int maxBuffer2) {
            this.minBuffer = minBuffer2;
            this.maxBuffer = maxBuffer2;
            Slog.i(ParaConfig.TAG, "BufferParaInit: bufferRange" + minBuffer2 + "," + maxBuffer2);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setDelay(int appStartDelay2, int reclaimDelay2) {
            this.appStartDelay = appStartDelay2;
            this.reclaimDelay = reclaimDelay2;
            Slog.i(ParaConfig.TAG, "BufferParaInit: delay" + appStartDelay2 + "," + reclaimDelay2);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setZswapd(int reclaimFailWindow2, int raiseBufferTimeWidth2, int raiseBufferMax2, int raiseBufferStep2, int reclaimFailCount2) {
            this.reclaimFailWindow = reclaimFailWindow2;
            this.raiseBufferTimeWidth = raiseBufferTimeWidth2;
            this.raiseBufferMax = raiseBufferMax2;
            this.raiseBufferStep = raiseBufferStep2;
            this.reclaimFailCount = reclaimFailCount2;
            Slog.i(ParaConfig.TAG, "BufferParaInit: zswapd" + reclaimFailWindow2 + "," + raiseBufferTimeWidth2 + "," + raiseBufferMax2 + "," + raiseBufferStep2 + "," + reclaimFailCount2);
        }

        public int getDefaultSize() {
            return this.defaultSize;
        }

        public int getLowSize() {
            return this.lowSize;
        }

        public int getUpperSize() {
            return this.upperBuffer;
        }

        public int getHighSize() {
            return this.highSize;
        }

        public int getSwapReserve() {
            return this.swapReserve;
        }

        public int getMinBuffer() {
            return this.minBuffer;
        }

        public int getMaxBuffer() {
            return this.maxBuffer;
        }

        public int getAppStartDelay() {
            return this.appStartDelay;
        }

        public int getReclaimDelay() {
            return this.reclaimDelay;
        }

        public int getReclaimFailWindow() {
            return this.reclaimFailWindow;
        }

        public int getReclaimFailCount() {
            return this.reclaimFailCount;
        }

        public int getRaiseBufferTimeWidth() {
            return this.raiseBufferTimeWidth;
        }

        public int getRaiseBufferMax() {
            return this.raiseBufferMax;
        }

        public int getRaiseBufferStep() {
            return this.raiseBufferStep;
        }
    }

    private void setBufferOtherParam(Map<String, String> param) {
        int appStartDelay = 1;
        int reclaimDelay = 2;
        int reclaimFailWindow = 5;
        int reclaimFailCount = 5;
        int raiseBufferTimeWidth = 10;
        int raiseBufferMax = 1000;
        int raiseBufferStep = 10;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            char c = 65535;
            switch (key.hashCode()) {
                case -2013987784:
                    if (key.equals("raiseBufferStep")) {
                        c = 6;
                        break;
                    }
                    break;
                case -619163016:
                    if (key.equals("raiseBufferMax")) {
                        c = 5;
                        break;
                    }
                    break;
                case -118074558:
                    if (key.equals("appStartDelay")) {
                        c = 0;
                        break;
                    }
                    break;
                case -45477606:
                    if (key.equals("reclaimDelay")) {
                        c = 1;
                        break;
                    }
                    break;
                case 865730728:
                    if (key.equals("reclaimFailCount")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1634672503:
                    if (key.equals("reclaimFailWindow")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1817653517:
                    if (key.equals("raiseBufferTimeWidth")) {
                        c = 4;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    appStartDelay = parseIntValue(key, value);
                    break;
                case 1:
                    reclaimDelay = parseIntValue(key, value);
                    break;
                case 2:
                    reclaimFailWindow = parseIntValue(key, value);
                    break;
                case 3:
                    reclaimFailCount = parseIntValue(key, value);
                    break;
                case 4:
                    raiseBufferTimeWidth = parseIntValue(key, value);
                    break;
                case 5:
                    raiseBufferMax = parseIntValue(key, value);
                    break;
                case 6:
                    raiseBufferStep = parseIntValue(key, value);
                    break;
                default:
                    Slog.e(TAG, "Unexpected key:" + key);
                    break;
            }
        }
        this.bufferSizeParam.setDelay(appStartDelay, reclaimDelay);
        this.bufferSizeParam.setZswapd(reclaimFailWindow, raiseBufferTimeWidth, raiseBufferMax, raiseBufferStep, reclaimFailCount);
    }

    private void setBufferSizeParam(Map<String, String> param) {
        int defaultSize = 1000;
        int lowSize = 950;
        int highSize = 1050;
        int upperSize = HwArbitrationDefs.MSG_NOTIFY_CURRENT_NETWORK;
        int swapReserve = 100;
        int minBuffer = 950;
        int maxBuffer = 1900;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            char c = 65535;
            switch (key.hashCode()) {
                case -1685286717:
                    if (key.equals("highSize")) {
                        c = 3;
                        break;
                    }
                    break;
                case -1242679383:
                    if (key.equals("swapReserve")) {
                        c = 4;
                        break;
                    }
                    break;
                case -1063155004:
                    if (key.equals("maxBuffer")) {
                        c = 6;
                        break;
                    }
                    break;
                case -436985374:
                    if (key.equals("defaultSize")) {
                        c = 0;
                        break;
                    }
                    break;
                case -290676430:
                    if (key.equals("minBuffer")) {
                        c = 5;
                        break;
                    }
                    break;
                case 223054787:
                    if (key.equals("upperSize")) {
                        c = 2;
                        break;
                    }
                    break;
                case 356461941:
                    if (key.equals("lowSize")) {
                        c = 1;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    defaultSize = parseIntValue(key, value);
                    break;
                case 1:
                    lowSize = parseIntValue(key, value);
                    break;
                case 2:
                    upperSize = parseIntValue(key, value);
                    break;
                case 3:
                    highSize = parseIntValue(key, value);
                    break;
                case 4:
                    swapReserve = parseIntValue(key, value);
                    break;
                case 5:
                    minBuffer = parseIntValue(key, value);
                    break;
                case 6:
                    maxBuffer = parseIntValue(key, value);
                    break;
                default:
                    Slog.e(TAG, "Unexpected key:" + key);
                    break;
            }
        }
        this.bufferSizeParam = new BufferSizeParam(defaultSize, lowSize, upperSize, highSize, swapReserve);
        this.bufferSizeParam.setBufferRange(minBuffer, maxBuffer);
        setBufferOtherParam(param);
    }

    public BufferSizeParam getBufferSizeParam() {
        return this.bufferSizeParam;
    }

    public static class FrontRatioParam {
        private int reclaimRatio;
        private int reclaimRefault;
        private int swapRatio;

        private FrontRatioParam(int reclaimRatio2, int swapRatio2, int reclaimRefault2) {
            this.reclaimRatio = reclaimRatio2;
            this.swapRatio = swapRatio2;
            this.reclaimRefault = reclaimRefault2;
        }

        public int getReclaimRatio() {
            return this.reclaimRatio;
        }

        public int getSwapRatio() {
            return this.swapRatio;
        }

        public int getReclaimRefault() {
            return this.reclaimRefault;
        }
    }

    private void setFrontRatioParam(Map<String, String> param) {
        int reclaimRatio = 0;
        int swapRatio = 0;
        int reclaimRefault = 0;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            char c = 65535;
            int hashCode = key.hashCode();
            if (hashCode != -1284091336) {
                if (hashCode != -1219698266) {
                    if (hashCode == -32659550 && key.equals("reclaimRatio")) {
                        c = 0;
                    }
                } else if (key.equals("reclaimRefault")) {
                    c = 2;
                }
            } else if (key.equals("swapRatio")) {
                c = 1;
            }
            if (c == 0) {
                reclaimRatio = parseIntValue(key, value);
            } else if (c == 1) {
                swapRatio = parseIntValue(key, value);
            } else if (c != 2) {
                Slog.e(TAG, "Unexpected key:" + key);
            } else {
                reclaimRefault = parseIntValue(key, value);
            }
        }
        this.frontRatioParam = new FrontRatioParam(reclaimRatio, swapRatio, reclaimRefault);
    }

    public FrontRatioParam getFrontRatioParam() {
        return this.frontRatioParam;
    }

    public static class ActiveRatioParam {
        private int reclaimRatio;
        private int reclaimRefault;
        private int swapRatio;

        private ActiveRatioParam(int reclaimRatio2, int swapRatio2, int reclaimRefault2) {
            this.reclaimRatio = reclaimRatio2;
            this.swapRatio = swapRatio2;
            this.reclaimRefault = reclaimRefault2;
        }

        public int getReclaimRatio() {
            return this.reclaimRatio;
        }

        public int getSwapRatio() {
            return this.swapRatio;
        }

        public int getReclaimRefault() {
            return this.reclaimRefault;
        }
    }

    private void setActiveRatioParam(Map<String, String> param) {
        int reclaimRatio = 0;
        int swapRatio = 0;
        int reclaimRefault = 0;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            char c = 65535;
            int hashCode = key.hashCode();
            if (hashCode != -1284091336) {
                if (hashCode != -1219698266) {
                    if (hashCode == -32659550 && key.equals("reclaimRatio")) {
                        c = 0;
                    }
                } else if (key.equals("reclaimRefault")) {
                    c = 2;
                }
            } else if (key.equals("swapRatio")) {
                c = 1;
            }
            if (c == 0) {
                reclaimRatio = parseIntValue(key, value);
            } else if (c == 1) {
                swapRatio = parseIntValue(key, value);
            } else if (c != 2) {
                Slog.e(TAG, "Unexpected key:" + key);
            } else {
                reclaimRefault = parseIntValue(key, value);
            }
        }
        this.activeRatioParam = new ActiveRatioParam(reclaimRatio, swapRatio, reclaimRefault);
    }

    public ActiveRatioParam getActiveRatioParam() {
        return this.activeRatioParam;
    }

    public static class FreezeRatioParam {
        private int reclaimRatio;
        private int reclaimRefault;
        private int swapRatio;

        private FreezeRatioParam(int reclaimRatio2, int swapRatio2, int reclaimRefault2) {
            this.reclaimRatio = reclaimRatio2;
            this.swapRatio = swapRatio2;
            this.reclaimRefault = reclaimRefault2;
        }

        public int getReclaimRatio() {
            return this.reclaimRatio;
        }

        public int getSwapRatio() {
            return this.swapRatio;
        }

        public int getReclaimRefault() {
            return this.reclaimRefault;
        }
    }

    private void setFreezeRatioParam(Map<String, String> param) {
        int reclaimRatio = 0;
        int swapRatio = 0;
        int reclaimRefault = 0;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            char c = 65535;
            int hashCode = key.hashCode();
            if (hashCode != -1284091336) {
                if (hashCode != -1219698266) {
                    if (hashCode == -32659550 && key.equals("reclaimRatio")) {
                        c = 0;
                    }
                } else if (key.equals("reclaimRefault")) {
                    c = 2;
                }
            } else if (key.equals("swapRatio")) {
                c = 1;
            }
            if (c == 0) {
                reclaimRatio = parseIntValue(key, value);
            } else if (c == 1) {
                swapRatio = parseIntValue(key, value);
            } else if (c != 2) {
                Slog.e(TAG, "Unexpected key:" + key);
            } else {
                reclaimRefault = parseIntValue(key, value);
            }
        }
        this.freezeRatioParam = new FreezeRatioParam(reclaimRatio, swapRatio, reclaimRefault);
    }

    public FreezeRatioParam getFreezeRatioParam() {
        return this.freezeRatioParam;
    }

    public static class AdvancedKillParam {
        private boolean akEnableDebug;
        private boolean akEnableDebugTime;

        private AdvancedKillParam(boolean akEnableDebug2, boolean akEnableDebugTime2) {
            this.akEnableDebug = akEnableDebug2;
            this.akEnableDebugTime = akEnableDebugTime2;
        }

        public boolean getAkEnableDebug() {
            return this.akEnableDebug;
        }

        public boolean getAkEnableDebugTime() {
            return this.akEnableDebugTime;
        }
    }

    private void setAdvancedKillParam(Map<String, String> param) {
        boolean akEnableDebug = false;
        boolean akEnableDebugTime = false;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            if ("akEnableDebug".equals(key) && AppActConstant.VALUE_TRUE.equalsIgnoreCase(value)) {
                akEnableDebug = true;
            } else if ("akEnableDebugTime".equals(key) && AppActConstant.VALUE_TRUE.equalsIgnoreCase(value)) {
                akEnableDebugTime = true;
            }
        }
        this.akParam = new AdvancedKillParam(akEnableDebug, akEnableDebugTime);
    }

    public AdvancedKillParam getAdvancedKillParam() {
        return this.akParam;
    }

    public static class ZswapdPress {
        private int highPressure;
        private int window;

        private ZswapdPress(int window2, int highPressure2) {
            this.window = window2;
            this.highPressure = highPressure2;
        }

        public int getWindow() {
            return this.window;
        }

        public int getHighPressure() {
            return this.highPressure;
        }
    }

    private void setZswapdPress(Map<String, String> param) {
        int window = -1;
        int highPressure = -1;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            char c = 65535;
            int hashCode = key.hashCode();
            if (hashCode != -787751952) {
                if (hashCode == 1864013319 && key.equals("highPressure")) {
                    c = 1;
                }
            } else if (key.equals("window")) {
                c = 0;
            }
            if (c == 0) {
                window = parseIntValue(key, value);
            } else if (c != 1) {
                Slog.e(TAG, "Unexpected key:" + key);
            } else {
                highPressure = parseIntValue(key, value);
            }
            this.zswapdPress = new ZswapdPress(window, highPressure);
        }
    }

    public ZswapdPress getZswapdPress() {
        return this.zswapdPress;
    }

    public static class OtherParam {
        private int dailySwapOutQuotaMax;
        private boolean logEnable;
        private boolean scorePolicyEnable;
        private String zramCriticalThres;

        private OtherParam(boolean logEnable2, boolean scorePolicyEnable2, int dailySwapOutQuotaMax2, String zramCriticalThres2) {
            this.logEnable = logEnable2;
            this.scorePolicyEnable = scorePolicyEnable2;
            this.dailySwapOutQuotaMax = dailySwapOutQuotaMax2;
            this.zramCriticalThres = zramCriticalThres2;
        }

        public int getDailySwapOutQuotaMax() {
            return this.dailySwapOutQuotaMax;
        }

        public boolean getLogEnable() {
            return this.logEnable;
        }

        public boolean getScorePolicyEnable() {
            return this.scorePolicyEnable;
        }

        public String getZramCriticalThres() {
            return this.zramCriticalThres;
        }
    }

    private void setOtherParam(Map<String, String> param) {
        boolean logEnable = false;
        boolean scorePolicyEnable = false;
        int dailySwapOutQuotaMax = 22;
        String zramCriticalThres = "";
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            if ("logEnable".equals(key) && AppActConstant.VALUE_TRUE.equalsIgnoreCase(value)) {
                logEnable = true;
            }
            if ("scorePolicyEnable".equals(key) && AppActConstant.VALUE_TRUE.equalsIgnoreCase(value)) {
                scorePolicyEnable = true;
            }
            if ("dailySwapOutQuotaMax".equals(key)) {
                dailySwapOutQuotaMax = parseIntValue(key, value);
            }
            if ("zramCriticalThres".equals(key)) {
                zramCriticalThres = value;
            }
        }
        this.otherParam = new OtherParam(logEnable, scorePolicyEnable, dailySwapOutQuotaMax, zramCriticalThres);
    }

    public OtherParam getOtherParam() {
        return this.otherParam;
    }

    public static class WhiteListParam {
        private boolean isActivity;
        private int whiteListKillThreshold;

        private WhiteListParam(int whiteListKillThreshold2, boolean isActivity2) {
            this.isActivity = false;
            this.whiteListKillThreshold = whiteListKillThreshold2;
            this.isActivity = isActivity2;
        }

        public int getWhiteListKillThreshold() {
            return this.whiteListKillThreshold;
        }

        public boolean getIsActivity() {
            return this.isActivity;
        }
    }

    private void setWhiteList(Map<String, String> param) {
        String pkgName = "";
        int killThreshold = 0;
        boolean activity = false;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            char c = 65535;
            int hashCode = key.hashCode();
            if (hashCode != -1655966961) {
                if (hashCode != -747916627) {
                    if (hashCode == 111052 && key.equals("pkg")) {
                        c = 0;
                    }
                } else if (key.equals("killThreshold")) {
                    c = 1;
                }
            } else if (key.equals("activity")) {
                c = 2;
            }
            if (c == 0) {
                pkgName = value;
            } else if (c == 1) {
                killThreshold = parseIntValue(key, value);
            } else if (c != 2) {
                Slog.e(TAG, "Unexpected key:" + key);
            } else if (AppActConstant.VALUE_TRUE.equalsIgnoreCase(value)) {
                activity = true;
            }
        }
        this.whiteList.put(pkgName, new WhiteListParam(killThreshold, activity));
    }

    public HashMap<String, WhiteListParam> getWhiteList() {
        return this.whiteList;
    }

    public static class ScoreRatioParam {
        private int activeReclaimRatio;
        private int activeReclaimRefault;
        private int activeSwapRatio;
        private int score;

        private ScoreRatioParam(int score2, int activeReclaimRatio2, int activeSwapRatio2, int activeReclaimRefault2) {
            this.score = score2;
            this.activeReclaimRatio = activeReclaimRatio2;
            this.activeSwapRatio = activeSwapRatio2;
            this.activeReclaimRefault = activeReclaimRefault2;
        }

        public int getScore() {
            return this.score;
        }

        public int getActiveReclaimRatio() {
            return this.activeReclaimRatio;
        }

        public int getActiveSwapRatio() {
            return this.activeSwapRatio;
        }

        public int getActiveReclaimRefault() {
            return this.activeReclaimRefault;
        }
    }

    private void setScoreRatioParam(Map<String, String> param) {
        int score = 0;
        int activeReclaimRatio = 0;
        int activeSwapRatio = 0;
        int activeReclaimRefault = 0;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            char c = 65535;
            switch (key.hashCode()) {
                case -848266798:
                    if (key.equals("activeSwapRatio")) {
                        c = 2;
                        break;
                    }
                    break;
                case -69983800:
                    if (key.equals("activeReclaimRatio")) {
                        c = 1;
                        break;
                    }
                    break;
                case 109264530:
                    if (key.equals("score")) {
                        c = 0;
                        break;
                    }
                    break;
                case 1566403148:
                    if (key.equals("activeReclaimRefault")) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                score = parseIntValue(key, value);
            } else if (c == 1) {
                activeReclaimRatio = parseIntValue(key, value);
            } else if (c == 2) {
                activeSwapRatio = parseIntValue(key, value);
            } else if (c != 3) {
                Slog.e(TAG, "Unexpected key:" + key);
            } else {
                activeReclaimRefault = parseIntValue(key, value);
            }
        }
        this.scoreRatioParam = new ScoreRatioParam(score, activeReclaimRatio, activeSwapRatio, activeReclaimRefault);
    }

    public ScoreRatioParam getScoreRatioParam() {
        return this.scoreRatioParam;
    }

    public static class SpecialParam {
        private int activeSwapRatio;
        private int freezeSwapRatio;
        private int frontSwapRatio;

        private SpecialParam(int frontSwapRatio2, int activeSwapRatio2, int freezeSwapRatio2) {
            this.frontSwapRatio = frontSwapRatio2;
            this.activeSwapRatio = activeSwapRatio2;
            this.freezeSwapRatio = freezeSwapRatio2;
        }

        public int getFrontSwapRatio() {
            return this.frontSwapRatio;
        }

        public int getActiveSwapRatio() {
            return this.activeSwapRatio;
        }

        public int getFreezeSwapRatio() {
            return this.freezeSwapRatio;
        }

        public String toString() {
            return "frontSwapRatio:" + this.frontSwapRatio + ", activeSwapRatio:" + this.activeSwapRatio + ", freezeSwapRatio:" + this.freezeSwapRatio;
        }
    }

    private void setSpecialParam(Map<String, String> param, String groupName) {
        if (param == null || groupName == null) {
            Slog.e(TAG, "setSpecialParam, param or groupName is null.");
            return;
        }
        int specialFrontRatio = 0;
        int specialActiveRatio = 0;
        int specialFreezeRatio = 0;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            char c = 65535;
            int hashCode = key.hashCode();
            if (hashCode != -848266798) {
                if (hashCode != 97988961) {
                    if (hashCode == 1630695951 && key.equals("frontSwapRatio")) {
                        c = 0;
                    }
                } else if (key.equals("freezeSwapRatio")) {
                    c = 2;
                }
            } else if (key.equals("activeSwapRatio")) {
                c = 1;
            }
            if (c == 0) {
                specialFrontRatio = parseIntValue(key, value);
            } else if (c == 1) {
                specialActiveRatio = parseIntValue(key, value);
            } else if (c != 2) {
                Slog.e(TAG, "Unexpected key:" + key);
            } else {
                specialFreezeRatio = parseIntValue(key, value);
            }
        }
        this.specialParamMap.put(groupName, new SpecialParam(specialFrontRatio, specialActiveRatio, specialFreezeRatio));
    }

    public Map<String, SpecialParam> getSpecialParamMap() {
        return this.specialParamMap;
    }

    private void setSpecialAppList(Map<String, String> param, String groupName) {
        if (param == null || groupName == null) {
            Slog.e(TAG, "setSpecialAppList, param or groupName is null.");
            return;
        }
        Slog.i(TAG, "set special app list groupName = " + groupName);
        for (Map.Entry<String, String> entry : param.entrySet()) {
            Slog.i(TAG, "key = " + entry.getKey() + ", value = " + entry.getValue());
            this.specialAppGroupMap.put(entry.getValue(), groupName);
        }
    }

    public Map<String, String> getSpecialAppGroupMap() {
        return this.specialAppGroupMap;
    }

    private int getCurDeviceRomSize() {
        long ufsSize = KernelInterface.getBootDeviceSize();
        long flag = 0;
        if (ufsSize % 1000000000 != 0) {
            flag = 1;
        }
        return (int) (((512 * ufsSize) / 1000000000) + flag);
    }

    public boolean getWriteBoostSwitch(boolean lifeInDanger) {
        if (!getWBSwitchOffRedLine(lifeInDanger)) {
            Slog.i(TAG, "nandlife write boost need close");
            return false;
        }
        Slog.i(TAG, "nandlife write boost keep open");
        return true;
    }

    public boolean getSwapOutSwitch() {
        if (!getSwapOutSwitchRedLine()) {
            Slog.i(TAG, "nandlife red line close");
            return false;
        }
        Slog.i(TAG, "nandlife red line open");
        return true;
    }

    public boolean getStorageEndStatus() {
        int freeRomSize = getFreeRomValue();
        if (freeRomSize >= endStorageValue) {
            return false;
        }
        Slog.i(TAG, "nandlife end storage close, cur free rom size:" + freeRomSize);
        return true;
    }

    private int getFreeRomValue() {
        try {
            Long freeRomSize = Long.valueOf(((new StatFs(Environment.getDataDirectory().getPath()).getAvailableBytes() / 1000) / 1000) / 1000);
            int ret = freeRomSize.intValue();
            Slog.i(TAG, "nandlife getFreeSize is: " + freeRomSize + " : " + ret);
            return ret;
        } catch (IllegalArgumentException iae) {
            Slog.i(TAG, "nandlife invalid storage" + iae);
            return 50;
        }
    }

    private boolean getWBSwitchOffRedLine(boolean lifeInDanger) {
        int curLifeTimeEstA = KernelInterface.getInstance().readLifeTimeEstA();
        int curLifeTimeEstB = KernelInterface.getInstance().readLifeTimeEstB();
        int curLifeTimePreEolInfo = KernelInterface.getInstance().readLifeTimePreEolInfo();
        int limitTypA = this.storageLifeParam.getWbLifeTimeEstTypA();
        int limitTypB = this.storageLifeParam.getWbLifeTimeEstTypB();
        boolean prediction = false;
        if (limitTypA > 11 && limitTypB > 11) {
            limitTypA %= 10;
            limitTypB %= 10;
            prediction = true;
        }
        if (curLifeTimeEstA >= limitTypA || curLifeTimeEstB >= limitTypB || curLifeTimePreEolInfo >= this.storageLifeParam.getWbLifeTimePreEolInfo()) {
            return false;
        }
        if (!prediction || !lifeInDanger) {
            return true;
        }
        return false;
    }

    private boolean getSwapOutSwitchRedLine() {
        int curLifeTimeEstA = KernelInterface.getInstance().readLifeTimeEstA();
        int curLifeTimeEstB = KernelInterface.getInstance().readLifeTimeEstB();
        int curLifeTimePreEolInfo = KernelInterface.getInstance().readLifeTimePreEolInfo();
        if (this.storageLifeParam.getLifeTimeEstTypA() <= curLifeTimeEstA || this.storageLifeParam.getLifeTimeEstTypB() <= curLifeTimeEstB || this.storageLifeParam.getLifeTimePreEolInfo() <= curLifeTimePreEolInfo) {
            return false;
        }
        return true;
    }

    public void updateNandLifeCycleInfo(ArrayList<Double> record) {
        Slog.i(TAG, "nandlife update nand life cycle info");
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new FileWriter(NAND_LIFE_FILE_PATH));
            Iterator<Double> it = record.iterator();
            while (it.hasNext()) {
                Double curVal = it.next();
                Slog.i(TAG, "nandlife update nand life cycle info: " + curVal);
                out2.write(String.valueOf(curVal));
                out2.newLine();
            }
            try {
                out2.close();
            } catch (IOException e) {
                Slog.e(TAG, "update nand life cycle info failed");
            }
        } catch (IOException e2) {
            Slog.e(TAG, "nandlife update lifecycle info failed");
            if (0 != 0) {
                out.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    out.close();
                } catch (IOException e3) {
                    Slog.e(TAG, "update nand life cycle info failed");
                }
            }
            throw th;
        }
    }

    public ArrayList<Double> getNandLifeCycleInfo() {
        Slog.i(TAG, "nandlife get nand life cycle info");
        ArrayList<Double> ret = new ArrayList<>();
        BufferedReader in = null;
        try {
            BufferedReader in2 = new BufferedReader(new FileReader(NAND_LIFE_FILE_PATH));
            while (true) {
                String curLine = in2.readLine();
                if (curLine == null) {
                    try {
                        break;
                    } catch (IOException e) {
                        Slog.e(TAG, "close nandlife cycle info file failed");
                    }
                } else if (!curLine.trim().isEmpty()) {
                    ret.add(Double.valueOf(Double.parseDouble(curLine.trim())));
                    Slog.i(TAG, "nandlife getNandLifeCycleInfo:" + Double.parseDouble(curLine.trim()));
                }
            }
            in2.close();
        } catch (IOException | NumberFormatException e2) {
            Slog.e(TAG, "nandlife getNandLife cycle info failed");
            if (0 != 0) {
                in.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    in.close();
                } catch (IOException e3) {
                    Slog.e(TAG, "close nandlife cycle info file failed");
                }
            }
            throw th;
        }
        return ret;
    }

    private void setActiveAppList(Map<String, String> param) {
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String value = entry.getValue();
            Slog.i(TAG, "key = " + entry.getKey() + ", value = " + value);
            this.activeAppList.add(value);
        }
    }

    public Set<String> getActiveAppList() {
        return this.activeAppList;
    }

    public static class RatioGroup implements Comparable<RatioGroup> {
        private int maxRam;
        private int minRam;
        private int reclaimRatio;
        private int reclaimRefault;
        private int swapRatio;

        private RatioGroup(int minRam2, int maxRam2, int swapRatio2, int reclaimRatio2, int reclaimRefault2) {
            this.minRam = minRam2;
            this.maxRam = maxRam2;
            this.swapRatio = swapRatio2;
            this.reclaimRatio = reclaimRatio2;
            this.reclaimRefault = reclaimRefault2;
        }

        public int getMinRam() {
            return this.minRam;
        }

        public int getMaxRam() {
            return this.maxRam;
        }

        public int getSwapRatio() {
            return this.swapRatio;
        }

        public int getReclaimRatio() {
            return this.reclaimRatio;
        }

        public int getReclaimRefault() {
            return this.reclaimRefault;
        }

        public int compareTo(RatioGroup ratioGroup) {
            return this.minRam - ratioGroup.minRam;
        }

        @Override // java.lang.Object
        public String toString() {
            return "minRam:" + this.minRam + ", maxRam:" + this.maxRam + ", swapRatio:" + this.swapRatio + ", reclaimRatio:" + this.reclaimRatio + ", reclaimRefault:" + this.reclaimRefault;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void setRationGroupList(Map<String, String> param) {
        char c;
        if (!param.isEmpty()) {
            int minRam = Integer.MAX_VALUE;
            int maxRam = Integer.MAX_VALUE;
            int swapRatio = 100;
            int reclaimRatio = 100;
            int reclaimRefault = 100;
            for (Map.Entry<String, String> entry : param.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Slog.i(TAG, "key = " + key + ", value = " + value);
                switch (key.hashCode()) {
                    case -1284091336:
                        if (key.equals("swapRatio")) {
                            c = 2;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1219698266:
                        if (key.equals("reclaimRefault")) {
                            c = 4;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1081151462:
                        if (key.equals("maxRam")) {
                            c = 1;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1074061204:
                        if (key.equals("minRam")) {
                            c = 0;
                            break;
                        }
                        c = 65535;
                        break;
                    case -32659550:
                        if (key.equals("reclaimRatio")) {
                            c = 3;
                            break;
                        }
                        c = 65535;
                        break;
                    default:
                        c = 65535;
                        break;
                }
                if (c == 0) {
                    minRam = parseIntValue(key, value);
                } else if (c == 1) {
                    maxRam = parseIntValue(key, value);
                } else if (c == 2) {
                    swapRatio = parseIntValue(key, value);
                } else if (c == 3) {
                    reclaimRatio = parseIntValue(key, value);
                } else if (c != 4) {
                    Slog.e(TAG, "Unexpected key:" + key);
                } else {
                    reclaimRefault = parseIntValue(key, value);
                }
            }
            this.freezeRatioGroupList.add(new RatioGroup(minRam, maxRam, swapRatio, reclaimRatio, reclaimRefault));
            Collections.sort(this.freezeRatioGroupList);
        }
    }

    public List<RatioGroup> getFreezeRatioGroupList() {
        return this.freezeRatioGroupList;
    }

    public static class AdjustRatioGroup {
        private int reclaimAdjustRatio;
        private int swapAdjustRatio;

        private AdjustRatioGroup(int swapAdjustRatio2, int reclaimAdjustRatio2) {
            this.swapAdjustRatio = swapAdjustRatio2;
            this.reclaimAdjustRatio = reclaimAdjustRatio2;
        }

        public int getSwapAdjustRatio() {
            return this.swapAdjustRatio;
        }

        public int getReclaimAdjustRatio() {
            return this.reclaimAdjustRatio;
        }

        public String toString() {
            return "swapAdjustRatio:" + this.swapAdjustRatio + ", reclaimAdjustRatio:" + this.reclaimAdjustRatio;
        }
    }

    private void setSwapIndexGroupMap(Map<String, String> param) {
        if (!param.isEmpty()) {
            int swapIndex = Integer.MAX_VALUE;
            int swapAdjustRatio = 100;
            int reclaimAdjustRatio = 100;
            for (Map.Entry<String, String> entry : param.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Slog.i(TAG, "key = " + key + ", value = " + value);
                char c = 65535;
                int hashCode = key.hashCode();
                if (hashCode != -1292031233) {
                    if (hashCode != -1072138711) {
                        if (hashCode == -697265133 && key.equals("reclaimAdjustRatio")) {
                            c = 2;
                        }
                    } else if (key.equals("swapAdjustRatio")) {
                        c = 1;
                    }
                } else if (key.equals("swapIndex")) {
                    c = 0;
                }
                if (c == 0) {
                    swapIndex = parseIntValue(key, value);
                } else if (c == 1) {
                    swapAdjustRatio = parseIntValue(key, value);
                } else if (c != 2) {
                    Slog.e(TAG, "Unexpected key:" + key);
                } else {
                    reclaimAdjustRatio = parseIntValue(key, value);
                }
            }
            this.swapIndexGroupMap.put(Integer.valueOf(swapIndex), new AdjustRatioGroup(swapAdjustRatio, reclaimAdjustRatio));
        }
    }

    public Map<Integer, AdjustRatioGroup> getSwapIndexGroupMap() {
        return this.swapIndexGroupMap;
    }

    private Map<String, String> getModuleParam(Node subNode) {
        Map<String, String> param = new HashMap<>();
        if (subNode.getNodeType() != 1) {
            return param;
        }
        Slog.i(TAG, "moduleName = " + ((Element) subNode).getAttribute(AodThemeConst.THEME_NAME_KEY));
        for (Node child = subNode.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == 1) {
                if (!(child instanceof Element)) {
                    return param;
                }
                Element ele = (Element) child;
                Slog.i(TAG, "param: " + ele.getAttribute("key") + " = " + ele.getTextContent());
                param.put(ele.getAttribute("key").trim(), ele.getTextContent().trim());
            }
        }
        return param;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void parseModule(String moduleName, Map<String, String> param, String name) {
        char c;
        switch (moduleName.hashCode()) {
            case -1063889755:
                if (moduleName.equals("activeRatio")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1006804125:
                if (moduleName.equals("others")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -884056777:
                if (moduleName.equals("rootMemcg")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -712255883:
                if (moduleName.equals("killOpt")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -336596126:
                if (moduleName.equals("frontRatio")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -316630156:
                if (moduleName.equals("psiEvent")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 3114:
                if (moduleName.equals("ak")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 96801:
                if (moduleName.equals("app")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 111302:
                if (moduleName.equals("psi")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 3291998:
                if (moduleName.equals("kill")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 830570860:
                if (moduleName.equals("ZswapdPress")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 1906231393:
                if (moduleName.equals("bufferSize")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 2104306612:
                if (moduleName.equals("freezeRatio")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                setPsiParam(param);
                return;
            case 1:
                setPsievent(param);
                return;
            case 2:
                setRootMemcgParam(param);
                return;
            case 3:
                setKillParam(param);
                return;
            case 4:
                setKillParamOpt(param);
                return;
            case 5:
                setBufferSizeParam(param);
                return;
            case 6:
                setFrontRatioParam(param);
                return;
            case 7:
                setActiveRatioParam(param);
                return;
            case '\b':
                setFreezeRatioParam(param);
                return;
            case '\t':
                setAdvancedKillParam(param);
                return;
            case '\n':
                setZswapdPress(param);
                return;
            case 11:
                setOtherParam(param);
                return;
            case '\f':
                setWhiteList(param);
                return;
            default:
                parseModuleSecond(moduleName, param, name);
                Slog.i(TAG, "setModuleParam second, module name = " + moduleName);
                return;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void parseModuleSecond(String moduleName, Map<String, String> param, String name) {
        char c;
        switch (moduleName.hashCode()) {
            case -1553891406:
                if (moduleName.equals("frequentUseRatio")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1278971052:
                if (moduleName.equals("specialParam")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -793234881:
                if (moduleName.equals("applist")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 99541696:
                if (moduleName.equals("swapIndexGroup")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 657645689:
                if (moduleName.equals("activeapplist")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1087655408:
                if (moduleName.equals("swapOutThreshold")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1178052052:
                if (moduleName.equals("backgroundActive")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1571318937:
                if (moduleName.equals("scoreRatio")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 2090927412:
                if (moduleName.equals("ratioGroup")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                setScoreRatioParam(param);
                return;
            case 1:
                setActiveAppList(param);
                return;
            case 2:
                setFrequentUseRatio(param);
                return;
            case 3:
                setBackgroundActiveRatio(param);
                return;
            case 4:
                setSwapOutThreshold(param);
                return;
            case 5:
                setSpecialParam(param, name);
                return;
            case 6:
                setSpecialAppList(param, name);
                return;
            case 7:
                setRationGroupList(param);
                return;
            case '\b':
                setSwapIndexGroupMap(param);
                return;
            default:
                Slog.e(TAG, "parseModuleSecond, module name = " + moduleName);
                return;
        }
    }

    private void setModuleParam(Node subNode, Map<String, String> param, String name) {
        if (subNode.getNodeType() == 1 && (subNode instanceof Element)) {
            parseModule(((Element) subNode).getAttribute(AodThemeConst.THEME_NAME_KEY).trim(), param, name);
        }
    }

    private int getCurDeviceMem() {
        MemInfoReader mInfo = new MemInfoReader();
        mInfo.readMemInfo();
        int memGb = (int) ((mInfo.getTotalSizeKb() / 1024) / 1024);
        if (memGb >= 10) {
            return 12;
        }
        if (memGb >= 7) {
            return 8;
        }
        if (memGb >= 5) {
            return 6;
        }
        return 4;
    }

    private void readSwapEnable(Node node) {
        if (node instanceof Element) {
            String content = ((Element) node).getTextContent().trim();
            Slog.i(TAG, "find swapEnable:" + content);
            if (AppActConstant.VALUE_TRUE.equalsIgnoreCase(content)) {
                swapEnable = true;
            } else {
                swapEnable = false;
            }
        }
    }

    private void readSwapOutEnable(Node node) {
        if (node instanceof Element) {
            String content = ((Element) node).getTextContent();
            if (content == null) {
                Slog.e(TAG, "nandlife swapout switch content is null");
                return;
            }
            if (AppActConstant.VALUE_TRUE.equalsIgnoreCase(content.trim())) {
                swapOutEnable = true;
            } else {
                swapOutEnable = false;
            }
            Slog.i(TAG, "nandlife find swapOurEnable:" + swapOutEnable);
        }
    }

    private void readSwapMinRamSize(Node node) {
        if (node instanceof Element) {
            String content = ((Element) node).getTextContent();
            if (content == null) {
                Slog.e(TAG, "nandlife minRamSize content is null");
                return;
            }
            try {
                swapMinRamSize = Integer.parseInt(content.trim());
            } catch (NumberFormatException e) {
                Slog.e(TAG, "nandlife swapMinRamSize format error");
            }
            if (swapMinRamSize < 0) {
                Slog.e(TAG, "nandlife invalid swapMinRamSize:" + swapMinRamSize);
                swapMinRamSize = 6;
            }
            Slog.i(TAG, "nandlife find swapOutMinRamSize" + swapMinRamSize);
        }
    }

    private void readSwapMinRomSize(Node node) {
        if (node instanceof Element) {
            String content = ((Element) node).getTextContent();
            if (content == null) {
                Slog.e(TAG, "nandlife minRomSize content is null");
                return;
            }
            try {
                swapMinRomSize = Integer.parseInt(content.trim());
            } catch (NumberFormatException e) {
                Slog.e(TAG, "nandlife invalid swapMinRomSize format error");
            }
            if (swapMinRomSize < 0) {
                Slog.e(TAG, "nandlife invalid swapMinRomSize:" + swapMinRomSize);
                swapMinRomSize = 128;
            }
            Slog.i(TAG, "nandlife find swapOutMinRomSize" + swapMinRomSize);
        }
    }

    private void readEndStorageValue(Node node) {
        if (node instanceof Element) {
            String content = ((Element) node).getTextContent();
            if (content == null) {
                Slog.e(TAG, "nandlife readEndStorage content is null");
                return;
            }
            try {
                endStorageValue = Integer.parseInt(content.trim());
            } catch (NumberFormatException e) {
                Slog.e(TAG, "nandlife invalid endStorageValue format error");
            }
            Slog.i(TAG, "nandlife find endStorageValue" + endStorageValue);
        }
    }

    private void readStorageLifeParam(Node node) {
        if (!(node instanceof Element)) {
            Slog.e(TAG, "readStorageLifeParam fail");
            return;
        }
        Node subNode = node.getFirstChild();
        if (subNode == null) {
            Slog.e(TAG, "can't find sub node of storage");
        }
        while (subNode != null) {
            Map<String, String> param = getModuleParam(subNode);
            if (param.size() > 0) {
                setStorageSwapParam(param);
                setStorageLifeTimeParam(param);
            }
            subNode = subNode.getNextSibling();
        }
    }

    private Node getRootChildNode(String xmlPath) throws ParserConfigurationException, IOException, SAXException, IllegalArgumentException {
        File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
        if (xmlFile == null) {
            Slog.i(TAG, "getRootChildNode failed from reading xml as fema, turn to use new File()");
            xmlFile = new File(xmlPath);
        }
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
        doc.getDocumentElement().getNodeName();
        Element rootElement = doc.getDocumentElement();
        Slog.i(TAG, "Root element is " + rootElement.getNodeName());
        return rootElement.getFirstChild();
    }

    private boolean parseOverAllXmlNode(Node rootChildNode) {
        String nodeName;
        if (!(rootChildNode instanceof Element) || (nodeName = ((Element) rootChildNode).getNodeName()) == null || nodeName.isEmpty()) {
            return false;
        }
        if ("swapEnable".equals(nodeName)) {
            readSwapEnable(rootChildNode);
            return true;
        } else if ("swapOutEnable".equals(nodeName)) {
            readSwapOutEnable(rootChildNode);
            return true;
        } else if ("swapMinRamSize".equals(nodeName)) {
            readSwapMinRamSize(rootChildNode);
            return true;
        } else if ("swapMinRomSize".equals(nodeName)) {
            readSwapMinRomSize(rootChildNode);
            return true;
        } else if ("endStorageValue".equals(nodeName)) {
            readEndStorageValue(rootChildNode);
            return true;
        } else if (!"storage".equals(nodeName)) {
            return false;
        } else {
            int curStorgeSize = Integer.parseInt(((Element) rootChildNode).getAttribute("size"));
            int deviceStorageSize = getCurDeviceRomSize();
            Slog.i(TAG, "find storage node size:" + curStorgeSize + ", device storage:" + deviceStorageSize);
            if (Math.abs(curStorgeSize - deviceStorageSize) <= 5) {
                readStorageLifeParam(rootChildNode);
                Slog.i(TAG, "read storage life param success");
            }
            return true;
        }
    }

    private boolean readParaFromXml(String xmlPath) {
        try {
            Node rootChildNode = getRootChildNode(xmlPath);
            Node chooseMemNode = null;
            while (true) {
                if (rootChildNode == null) {
                    break;
                } else if (rootChildNode.getNodeType() != 1) {
                    rootChildNode = rootChildNode.getNextSibling();
                } else if (!(rootChildNode instanceof Element)) {
                    return false;
                } else {
                    if (!parseOverAllXmlNode(rootChildNode)) {
                        int memory = Integer.parseInt(((Element) rootChildNode).getAttribute("size"));
                        chooseMemNode = rootChildNode;
                        Slog.i(TAG, "find memory node size:" + memory);
                        if (memory == getCurDeviceMem()) {
                            break;
                        }
                    }
                    rootChildNode = rootChildNode.getNextSibling();
                }
            }
            if (chooseMemNode == null) {
                return false;
            }
            Node node = chooseMemNode.getFirstChild();
            while (node != null) {
                if (node.getNodeType() != 1) {
                    node = node.getNextSibling();
                } else {
                    Slog.i(TAG, "group = " + node.getNodeName());
                    String groupName = null;
                    if (node instanceof Element) {
                        groupName = ((Element) node).getAttribute(AodThemeConst.THEME_NAME_KEY);
                    }
                    for (Node subNode = node.getFirstChild(); subNode != null; subNode = subNode.getNextSibling()) {
                        setModuleParam(subNode, getModuleParam(subNode), groupName);
                    }
                    node = node.getNextSibling();
                }
            }
            return true;
        } catch (IOException | IllegalArgumentException | ParserConfigurationException | SAXException ex) {
            Slog.e(TAG, "readParaFromXmlNew::read xml error, " + ex);
            return false;
        }
    }

    public ConcurrentHashMap<String, Integer> getFrequentUseAppList() {
        ConcurrentHashMap<String, Integer> frequentUseAppMap = new ConcurrentHashMap<>();
        BufferedReader in = null;
        try {
            BufferedReader in2 = new BufferedReader(new FileReader(SWAP_RATIO_USERDATA_FILE_PATH));
            while (true) {
                String curLine = in2.readLine();
                if (curLine == null) {
                    try {
                        break;
                    } catch (IOException e) {
                        Slog.e(TAG, "close frequent use app info file failed");
                    }
                } else if (!curLine.trim().isEmpty()) {
                    String[] buff = curLine.split(",");
                    if (buff.length == 2) {
                        String appName = buff[0];
                        int useDay = Integer.parseInt(buff[1]);
                        if (useDay > 60) {
                            useDay = 60;
                        }
                        frequentUseAppMap.put(appName, Integer.valueOf(useDay));
                        Slog.i(TAG, "nandlife getNandLifeCycleInfo:" + curLine.trim());
                    }
                }
            }
            in2.close();
        } catch (IOException | NumberFormatException e2) {
            Slog.e(TAG, "get frequent use app info failed");
            if (0 != 0) {
                in.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    in.close();
                } catch (IOException e3) {
                    Slog.e(TAG, "close frequent use app info file failed");
                }
            }
            throw th;
        }
        return frequentUseAppMap;
    }

    public void updateFrequentUseAppList(ConcurrentHashMap<String, Integer> frequentUseAppMap) {
        Slog.i(TAG, "frequent Use App update info");
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new FileWriter(SWAP_RATIO_USERDATA_FILE_PATH));
            for (Map.Entry<String, Integer> entry : frequentUseAppMap.entrySet()) {
                String key = entry.getKey();
                int value = entry.getValue().intValue();
                Slog.i(TAG, "key = " + key + ", value = " + value);
                StringBuilder sb = new StringBuilder();
                sb.append(key);
                sb.append(",");
                sb.append(String.valueOf(value));
                out2.write(sb.toString());
                out2.newLine();
            }
            try {
                out2.close();
            } catch (IOException e) {
                Slog.e(TAG, "update frequent Use App info failed");
            }
        } catch (IOException e2) {
            Slog.e(TAG, "frequent Use App update info failed");
            if (0 != 0) {
                out.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    out.close();
                } catch (IOException e3) {
                    Slog.e(TAG, "update frequent Use App info failed");
                }
            }
            throw th;
        }
    }

    public static class FreqUseRatioParam {
        private int activeSwapRatio;
        private int freezeSwapRatio;
        private int freqUseThres;
        private int frontSwapRatio;

        private FreqUseRatioParam(int frontSwapRatio2, int activeSwapRatio2, int freezeSwapRatio2, int freqUseThres2) {
            this.frontSwapRatio = frontSwapRatio2;
            this.activeSwapRatio = activeSwapRatio2;
            this.freezeSwapRatio = freezeSwapRatio2;
            this.freqUseThres = freqUseThres2;
        }

        public int getFrontSwapRatio() {
            return this.frontSwapRatio;
        }

        public int getActiveSwapRatio() {
            return this.activeSwapRatio;
        }

        public int getFreezeSwapRatio() {
            return this.freezeSwapRatio;
        }

        public int getFreqUseThres() {
            return this.freqUseThres;
        }
    }

    private void setFrequentUseRatio(Map<String, String> param) {
        int freqUseFrontRatio = 0;
        int freqUseActiveRatio = 0;
        int freqUseFreezeRatio = 0;
        int freqUseThres = 0;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            char c = 65535;
            switch (key.hashCode()) {
                case -848266798:
                    if (key.equals("activeSwapRatio")) {
                        c = 1;
                        break;
                    }
                    break;
                case 97988961:
                    if (key.equals("freezeSwapRatio")) {
                        c = 2;
                        break;
                    }
                    break;
                case 606654205:
                    if (key.equals("freqUseThres")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1630695951:
                    if (key.equals("frontSwapRatio")) {
                        c = 0;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                freqUseFrontRatio = parseIntValue(key, value);
            } else if (c == 1) {
                freqUseActiveRatio = parseIntValue(key, value);
            } else if (c == 2) {
                freqUseFreezeRatio = parseIntValue(key, value);
            } else if (c != 3) {
                Slog.e(TAG, "Unexpected key:" + key);
            } else {
                freqUseThres = parseIntValue(key, value);
            }
        }
        this.freqUseRatioParam = new FreqUseRatioParam(freqUseFrontRatio, freqUseActiveRatio, freqUseFreezeRatio, freqUseThres);
    }

    public FreqUseRatioParam getFrequentUseRatio() {
        return this.freqUseRatioParam;
    }

    public static class BgActSwapRatioParam {
        private int activeSwapRatio;
        private int bgActiveThres;
        private int freezeSwapRatio;
        private int frontSwapRatio;

        private BgActSwapRatioParam(int frontSwapRatio2, int activeSwapRatio2, int freezeSwapRatio2, int bgActiveThres2) {
            this.frontSwapRatio = frontSwapRatio2;
            this.activeSwapRatio = activeSwapRatio2;
            this.freezeSwapRatio = freezeSwapRatio2;
            this.bgActiveThres = bgActiveThres2;
        }

        public int getFrontSwapRatio() {
            return this.frontSwapRatio;
        }

        public int getActiveSwapRatio() {
            return this.activeSwapRatio;
        }

        public int getFreezeSwapRatio() {
            return this.freezeSwapRatio;
        }

        public int getBgActiveThres() {
            return this.bgActiveThres;
        }
    }

    private void setBackgroundActiveRatio(Map<String, String> param) {
        int bgFrontRatio = 0;
        int bgActiveRatio = 0;
        int bgFreezeRatio = 0;
        int bgActiveThres = 0;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            char c = 65535;
            switch (key.hashCode()) {
                case -1967040319:
                    if (key.equals("bgActiveThres")) {
                        c = 3;
                        break;
                    }
                    break;
                case -848266798:
                    if (key.equals("activeSwapRatio")) {
                        c = 1;
                        break;
                    }
                    break;
                case 97988961:
                    if (key.equals("freezeSwapRatio")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1630695951:
                    if (key.equals("frontSwapRatio")) {
                        c = 0;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                bgFrontRatio = parseIntValue(key, value);
            } else if (c == 1) {
                bgActiveRatio = parseIntValue(key, value);
            } else if (c == 2) {
                bgFreezeRatio = parseIntValue(key, value);
            } else if (c != 3) {
                Slog.e(TAG, "Unexpected key:" + key);
            } else {
                bgActiveThres = parseIntValue(key, value);
            }
        }
        this.bgActSwapRatioParam = new BgActSwapRatioParam(bgFrontRatio, bgActiveRatio, bgFreezeRatio, bgActiveThres);
    }

    public BgActSwapRatioParam getBackgroundActiveRatio() {
        return this.bgActSwapRatioParam;
    }

    public static class SwapRatioThresParam {
        private int dailyAllThres;
        private int dailySingleAppThres;

        private SwapRatioThresParam(int dailySingleAppThres2, int dailyAllThres2) {
            this.dailySingleAppThres = dailySingleAppThres2;
            this.dailyAllThres = dailyAllThres2;
        }

        public int getDailySingleAppThres() {
            return this.dailySingleAppThres;
        }

        public int getDailyAllThres() {
            return this.dailyAllThres;
        }
    }

    private void setSwapOutThreshold(Map<String, String> param) {
        int dailySingleAppThres = 0;
        int dailyAllThres = 0;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Slog.i(TAG, "key = " + key + ", value = " + value);
            char c = 65535;
            int hashCode = key.hashCode();
            if (hashCode != 1468631155) {
                if (hashCode == 1787762827 && key.equals("allThres")) {
                    c = 1;
                }
            } else if (key.equals("singleAppThres")) {
                c = 0;
            }
            if (c == 0) {
                dailySingleAppThres = parseIntValue(key, value);
            } else if (c != 1) {
                Slog.e(TAG, "Unexpected key:" + key);
            } else {
                dailyAllThres = parseIntValue(key, value);
            }
        }
        this.swapRatioThresParam = new SwapRatioThresParam(dailySingleAppThres, dailyAllThres);
    }

    public SwapRatioThresParam getSwapOutThreshold() {
        return this.swapRatioThresParam;
    }

    public ConcurrentHashMap<String, Integer> getBgActiveAppList() {
        ConcurrentHashMap<String, Integer> bgActiveAppList = new ConcurrentHashMap<>();
        BufferedReader in = null;
        try {
            BufferedReader in2 = new BufferedReader(new FileReader(SWAP_RATIO_BGACTIVE_FILE_PATH));
            while (true) {
                String curLine = in2.readLine();
                if (curLine == null) {
                    try {
                        break;
                    } catch (IOException e) {
                        Slog.e(TAG, "close bg Active app info file failed");
                    }
                } else if (!curLine.trim().isEmpty()) {
                    String[] buff = curLine.split(",");
                    if (buff.length == 2) {
                        String appName = buff[0];
                        int useDay = Integer.parseInt(buff[1]);
                        if (useDay > 60) {
                            useDay = 60;
                        }
                        bgActiveAppList.put(appName, Integer.valueOf(useDay));
                        Slog.i(TAG, "bg active App List info:" + curLine.trim());
                    }
                }
            }
            in2.close();
        } catch (IOException | NumberFormatException e2) {
            Slog.e(TAG, "get bg active app info failed");
            if (0 != 0) {
                in.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    in.close();
                } catch (IOException e3) {
                    Slog.e(TAG, "close bg Active app info file failed");
                }
            }
            throw th;
        }
        return bgActiveAppList;
    }

    public void updateBgActiveAppList(ConcurrentHashMap<String, Integer> bgActiveAppList) {
        Slog.i(TAG, "bgActive App update info");
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new FileWriter(SWAP_RATIO_BGACTIVE_FILE_PATH));
            for (Map.Entry<String, Integer> entry : bgActiveAppList.entrySet()) {
                String key = entry.getKey();
                int value = entry.getValue().intValue();
                Slog.i(TAG, "key = " + key + ", value = " + value);
                StringBuilder sb = new StringBuilder();
                sb.append(key);
                sb.append(",");
                sb.append(String.valueOf(value));
                out2.write(sb.toString());
                out2.newLine();
            }
            try {
                out2.close();
            } catch (IOException e) {
                Slog.e(TAG, "bg active App info failed");
            }
        } catch (IOException e2) {
            Slog.e(TAG, "bg active App update info failed");
            if (0 != 0) {
                out.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    out.close();
                } catch (IOException e3) {
                    Slog.e(TAG, "bg active App info failed");
                }
            }
            throw th;
        }
    }

    public void writeZramCriticalThres() {
        String zramCriticalThres = this.otherParam.getZramCriticalThres();
        if (zramCriticalThres == null) {
            Slog.e(TAG, "zramCriticalThres is null");
        } else if ("".equals(zramCriticalThres)) {
            Slog.e(TAG, "zramCriticalThres is non-value");
        } else if (!Files.exists(Paths.get("/dev/memcg/memory.zram_critical_threshold", new String[0]), new LinkOption[0])) {
            Slog.e(TAG, "zram_critical_threshold doesn't exists");
        } else {
            File nodeFile = new File("/dev/memcg/memory.zram_critical_threshold");
            FileOutputStream stream = null;
            byte[] arrBytes = zramCriticalThres.getBytes(Charset.defaultCharset());
            try {
                stream = new FileOutputStream(nodeFile, false);
                stream.write(arrBytes);
                Slog.i(TAG, "zram_critical_threshold write value: " + zramCriticalThres);
                try {
                    stream.close();
                } catch (IOException e) {
                    Slog.e(TAG, "Can't close zram_critical_threshold file");
                }
            } catch (FileNotFoundException e2) {
                Slog.e(TAG, "zram_critical_threshold not found");
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e3) {
                Slog.e(TAG, "Can't write zram_critical_threshold file");
                if (stream != null) {
                    stream.close();
                }
            } catch (Throwable th) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e4) {
                        Slog.e(TAG, "Can't close zram_critical_threshold file");
                    }
                }
                throw th;
            }
        }
    }
}
