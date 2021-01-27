package com.android.server.rms.iaware.srms;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.rule.ListItem;
import com.android.server.mtm.iaware.srms.AwareBroadcastDumpRadar;
import com.android.server.mtm.iaware.srms.AwareBroadcastSend;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import com.android.server.rms.iaware.feature.RFeature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class BroadcastExFeature extends RFeature {
    public static final String BR_FILTER_APP_BLACKLIST = "filter_blackbrapp_list";
    public static final String BR_FILTER_APP_WHITELIST = "filter_whitebrapp_list";
    private static final ArrayMap<String, ArraySet<String>> BR_FILTER_BLACK_APP = new ArrayMap<>();
    private static final HashMap<String, Integer> BR_FILTER_DATA = new HashMap<>();
    public static final String BR_FILTER_SWITCH = "filterSwitch";
    public static final String BR_FILTER_WHITELIST = "filter_white_list";
    private static final ArrayMap<String, ArraySet<String>> BR_FILTER_WHITE_APP = new ArrayMap<>();
    private static final ArraySet<String> BR_FILTER_WHITE_LIST = new ArraySet<>();
    public static final String BR_GOOGLE_APP_LIST_NAME = "br_google_app";
    private static final ArraySet<String> BR_GOOGLE_APP_SET = new ArraySet<>();
    public static final String BR_SEND_SWITCH = "SendSwitch";
    public static final int FILTER_SWITCH = 1;
    private static final int PARSE_LIST_LENGTH = 2;
    public static final int SEND_SWITCH = 2;
    private static final String TAG = "BroadcastExFeature";
    private static final int VERSION = 3;
    private static boolean sBroadcastFilterEnable = false;
    private static boolean sBroadcastSendEnable = false;
    private static boolean sFeature = false;
    private static AtomicBoolean sIsInitialized = new AtomicBoolean(false);
    private AwareBroadcastDumpRadar mDumpRadar = null;

    public BroadcastExFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        initConfig();
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        AwareLog.i(TAG, "BroadcastExFeature disable");
        setEnable(false);
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        if (realVersion >= 3) {
            AwareLog.i(TAG, "BroadcastExFeature 3.0 enableFeatureEx");
            AwareBroadcastSend.getInstance().updateConfigData();
            setEnable(true);
            return true;
        }
        AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", BroadcastExFeature Version: 3");
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        return false;
    }

    private static void setEnable(boolean enable) {
        sFeature = enable;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public String getDftDataByVersion(int iawareVer, boolean forBeta, boolean clearData, boolean betaEncode) {
        if (iawareVer < 3) {
            AwareLog.i(TAG, "Feature based on IAware3.0, getBigDataByVersion return null. iawareVer: " + iawareVer);
        } else if (!sFeature) {
            AwareLog.e(TAG, "Broadcast feature is disabled, it is invalid operation to save big data.");
            return null;
        } else if (getDumpRadar() != null) {
            return getDumpRadar().getDftData(forBeta, clearData, betaEncode);
        }
        return null;
    }

    private AwareBroadcastDumpRadar getDumpRadar() {
        if (MultiTaskManagerService.self() != null) {
            this.mDumpRadar = MultiTaskManagerService.self().getAwareBrRadar();
        }
        return this.mDumpRadar;
    }

    public static boolean isFeatureEnabled(int type) {
        return type == 1 ? sFeature && sBroadcastFilterEnable : type == 2 && sFeature && sBroadcastSendEnable;
    }

    private void initConfig() {
        if (!sIsInitialized.get()) {
            sIsInitialized.set(true);
            updateConfig();
        }
    }

    public static void updateConfig() {
        ArrayList<String> filter = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), BR_FILTER_SWITCH);
        if (filter != null && filter.size() == 1) {
            sBroadcastFilterEnable = switchOn(filter.get(0));
        }
        ArrayList<String> filter2 = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), BR_SEND_SWITCH);
        if (filter2 != null && filter2.size() > 0) {
            sBroadcastSendEnable = switchOn(filter2.get(0));
        }
        getBrList();
        getBrDataPolicy();
    }

    private static boolean switchOn(String value) {
        try {
            if (Integer.parseInt(value) == 1) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "iaware_brEx value format error");
            return false;
        }
    }

    private static void getBrList() {
        ArrayList<String> whiteFilterList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), BR_FILTER_WHITELIST);
        if (whiteFilterList != null) {
            synchronized (BR_FILTER_WHITE_LIST) {
                BR_FILTER_WHITE_LIST.clear();
                int size = whiteFilterList.size();
                for (int index = 0; index < size; index++) {
                    BR_FILTER_WHITE_LIST.add(whiteFilterList.get(index));
                }
            }
        }
        ArrayList<String> backBrAppFilterList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), BR_FILTER_APP_BLACKLIST);
        if (backBrAppFilterList != null) {
            ArrayMap<String, ArraySet<String>> filterBackBrApp = parseResult(backBrAppFilterList);
            synchronized (BR_FILTER_BLACK_APP) {
                BR_FILTER_BLACK_APP.clear();
                BR_FILTER_BLACK_APP.putAll((ArrayMap<? extends String, ? extends ArraySet<String>>) filterBackBrApp);
            }
        }
        ArrayList<String> whiteBrAppFilterList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), BR_FILTER_APP_WHITELIST);
        if (whiteBrAppFilterList != null) {
            ArrayMap<String, ArraySet<String>> filterWhiteBrApp = parseResult(whiteBrAppFilterList);
            synchronized (BR_FILTER_WHITE_APP) {
                BR_FILTER_WHITE_APP.clear();
                BR_FILTER_WHITE_APP.putAll((ArrayMap<? extends String, ? extends ArraySet<String>>) filterWhiteBrApp);
            }
        }
        ArrayList<String> googleAppList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), BR_GOOGLE_APP_LIST_NAME);
        if (googleAppList != null) {
            synchronized (BR_GOOGLE_APP_SET) {
                BR_GOOGLE_APP_SET.clear();
                int size2 = googleAppList.size();
                for (int index2 = 0; index2 < size2; index2++) {
                    BR_GOOGLE_APP_SET.add(googleAppList.get(index2));
                }
            }
        }
    }

    private static ArrayMap<String, ArraySet<String>> parseResult(ArrayList<String> results) {
        ArrayMap<String, ArraySet<String>> parseResult = new ArrayMap<>();
        int size = results.size();
        for (int index = 0; index < size; index++) {
            String[] contentArray = results.get(index).split(":");
            if (contentArray.length == 2) {
                ArraySet<String> apps = new ArraySet<>();
                String action = contentArray[0].trim();
                for (String str : contentArray[1].trim().split(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                    apps.add(str.trim());
                }
                parseResult.put(action, apps);
            } else {
                AwareLog.e(TAG, "iaware_brEx value format error");
            }
        }
        return parseResult;
    }

    private static void getBrDataPolicy() {
        ArrayMap<String, ListItem> filterBrDatas = DecisionMaker.getInstance().getBrListItem(AppMngConstant.AppMngFeature.BROADCAST, AppMngConstant.BroadcastSource.BROADCAST_FILTER);
        if (filterBrDatas != null) {
            synchronized (BR_FILTER_DATA) {
                BR_FILTER_DATA.clear();
                for (Map.Entry<String, ListItem> ent : filterBrDatas.entrySet()) {
                    String action = ent.getKey();
                    ListItem item = ent.getValue();
                    if (!(action == null || item == null)) {
                        BR_FILTER_DATA.put(action, Integer.valueOf(item.getPolicy()));
                    }
                }
            }
        }
    }

    public static boolean isBrFilterWhiteList(String pkgName) {
        boolean contains;
        synchronized (BR_FILTER_WHITE_LIST) {
            contains = BR_FILTER_WHITE_LIST.contains(pkgName);
        }
        return contains;
    }

    public static ArraySet<String> getBrFilterWhiteList() {
        ArraySet<String> whiteList = new ArraySet<>();
        synchronized (BR_FILTER_WHITE_LIST) {
            whiteList.addAll((ArraySet<? extends String>) BR_FILTER_WHITE_LIST);
        }
        return whiteList;
    }

    public static int getBrFilterPolicy(String action) {
        synchronized (BR_FILTER_DATA) {
            Integer policy = BR_FILTER_DATA.get(action);
            if (policy == null) {
                return -1;
            }
            return policy.intValue();
        }
    }

    public static boolean isBrFilterBlackApp(String action, String pkgName) {
        synchronized (BR_FILTER_BLACK_APP) {
            ArraySet<String> apps = BR_FILTER_BLACK_APP.get(action);
            if (apps == null) {
                return false;
            }
            return apps.contains(pkgName);
        }
    }

    public static ArrayMap<String, ArraySet<String>> getBrFilterBlackApp() {
        ArrayMap<String, ArraySet<String>> arrayMap;
        synchronized (BR_FILTER_BLACK_APP) {
            arrayMap = new ArrayMap<>(BR_FILTER_BLACK_APP);
        }
        return arrayMap;
    }

    public static boolean isBrFilterWhiteApp(String action, String pkgName) {
        synchronized (BR_FILTER_WHITE_APP) {
            ArraySet<String> apps = BR_FILTER_WHITE_APP.get(action);
            if (apps == null) {
                return false;
            }
            return apps.contains(pkgName);
        }
    }

    public static ArrayMap<String, ArraySet<String>> getBrFilterWhiteApp() {
        ArrayMap<String, ArraySet<String>> arrayMap;
        synchronized (BR_FILTER_WHITE_APP) {
            arrayMap = new ArrayMap<>(BR_FILTER_WHITE_APP);
        }
        return arrayMap;
    }

    public static ArraySet<String> getBrGoogleAppList() {
        ArraySet<String> googleAppList = new ArraySet<>();
        synchronized (BR_GOOGLE_APP_SET) {
            googleAppList.addAll((ArraySet<? extends String>) BR_GOOGLE_APP_SET);
        }
        return googleAppList;
    }

    public static boolean isBrGoogleApp(String pkgName) {
        boolean contains;
        synchronized (BR_GOOGLE_APP_SET) {
            contains = BR_GOOGLE_APP_SET.contains(pkgName);
        }
        return contains;
    }
}
