package com.huawei.i18n.taboo;

import com.huawei.i18n.taboo.FindTargetRules;
import com.huawei.i18n.taboo.Taboo;
import com.huawei.uikit.effect.BuildConfig;
import huawei.android.provider.HwSettings;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TabooReader {
    private static final String CLOUD_PATH = "/data/cota/para/taboo";
    private static final String LOG_TAG = "TabooReader";
    private static final long MAX_WAIT = 60000;
    private static final String PREAS_PATH = "/preas/xml/taboo";
    private static final String SYSTEM_PATH = "/system/emui/base/taboo";
    private static Taboo cloudTaboo = null;
    private static Taboo insideTaboo = null;
    private static long lastReadTime = 0;
    private static Taboo preasTaboo = null;
    private static FindTargetRules.SystemParam sParam = null;

    public enum ParamType {
        LANGUAGE_NAME("lang", "langs"),
        REGION_NAME(HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_REGION_SUFFIX, "regions"),
        BLACK_LANG("black_lang", BuildConfig.FLAVOR),
        TABOO_BLACK_LANG("taboo_black_lang", BuildConfig.FLAVOR),
        CITY_NAME("city", "citys"),
        BLACK_CITY("black_city", BuildConfig.FLAVOR);
        
        private String prefix;
        private String scopeName;

        private ParamType(String prefix2, String scopeName2) {
            this.prefix = prefix2;
            this.scopeName = scopeName2;
        }

        public String getPrefix() {
            return this.prefix;
        }

        public String getScopeName() {
            return this.scopeName;
        }
    }

    public static String getValue(ParamType type, Locale locale, String key) {
        String result;
        initAndcheckUpdate();
        Taboo current = getCurrentTaboo();
        if (current == null || type == null) {
            return null;
        }
        int i = AnonymousClass1.$SwitchMap$com$huawei$i18n$taboo$TabooReader$ParamType[type.ordinal()];
        if (i == 1) {
            return getConfig(current, type.getPrefix(), true);
        }
        if (i == 2) {
            return getConfig(current, type.getPrefix(), true);
        }
        if (i == 3) {
            return getConfig(current, type.getPrefix(), true);
        }
        String result2 = null;
        if (i == 4) {
            String key2 = FindTargetRules.cityNameNormalize(key);
            if (key2 != null && isContain(type, key2)) {
                result2 = getData(current, locale, type.getPrefix() + "_" + key2);
            }
            return result2;
        } else if (i != 5) {
            if (key != null && isContain(type, key)) {
                result2 = getData(current, locale, type.getPrefix() + "_" + key);
            }
            return result2;
        } else if (locale == null || key == null || key.isEmpty()) {
            return null;
        } else {
            if (isContain(type, key)) {
                result = getData(current, locale, type.getPrefix() + "_" + key);
            } else {
                result = null;
            }
            if (result != null) {
                return result;
            }
            String formatKey = FindTargetRules.langTagFormat(key);
            if (isContain(type, formatKey)) {
                result2 = getData(current, locale, type.getPrefix() + "_" + formatKey);
            }
            return result2;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.i18n.taboo.TabooReader$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$i18n$taboo$TabooReader$ParamType = new int[ParamType.values().length];

        static {
            try {
                $SwitchMap$com$huawei$i18n$taboo$TabooReader$ParamType[ParamType.BLACK_LANG.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$i18n$taboo$TabooReader$ParamType[ParamType.TABOO_BLACK_LANG.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$i18n$taboo$TabooReader$ParamType[ParamType.BLACK_CITY.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$i18n$taboo$TabooReader$ParamType[ParamType.CITY_NAME.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$huawei$i18n$taboo$TabooReader$ParamType[ParamType.LANGUAGE_NAME.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$huawei$i18n$taboo$TabooReader$ParamType[ParamType.REGION_NAME.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    private static boolean isContain(ParamType type, String key) {
        Taboo.TabooConfig tabooConfig;
        initAndcheckUpdate();
        Taboo current = getCurrentTaboo();
        if (current == null || (tabooConfig = current.getmTabooConfig()) == null) {
            return false;
        }
        int i = AnonymousClass1.$SwitchMap$com$huawei$i18n$taboo$TabooReader$ParamType[type.ordinal()];
        if (i == 4) {
            return tabooConfig.getCityList().contains(key);
        }
        if (i == 5) {
            return tabooConfig.getLangList().contains(key);
        }
        if (i != 6) {
            return false;
        }
        return tabooConfig.getRegionList().contains(key);
    }

    private static String getData(Taboo taboo, Locale locale, String key) {
        List<String> resList;
        if (locale == null || (resList = taboo.getLanguageList()) == null || resList.isEmpty()) {
            return null;
        }
        List<Map.Entry<String, Integer>> list = FindTargetRules.getFilterList(resList, locale);
        if (list.get(0).getValue().intValue() == -1) {
            return null;
        }
        String localeId = list.get(0).getKey();
        for (String strKey : FindTargetRules.getSequentialFallBack(sParam, key)) {
            String value = taboo.getData(localeId, strKey);
            if (!(value == null || value.isEmpty())) {
                return value;
            }
        }
        return null;
    }

    private static String getConfig(Taboo taboo, String key, boolean isFallBack) {
        if (!isFallBack) {
            return taboo.getmTabooConfig().getValue(key);
        }
        for (String str : FindTargetRules.getSequentialFallBack(sParam, key)) {
            String value = taboo.getmTabooConfig().getValue(str);
            if (!(value == null || value.isEmpty())) {
                return value;
            }
        }
        return null;
    }

    private static synchronized void initAndcheckUpdate() {
        synchronized (TabooReader.class) {
            long currentTime = System.currentTimeMillis();
            if (lastReadTime == 0 || ((cloudTaboo == null && insideTaboo == null && preasTaboo == null) || currentTime - lastReadTime >= MAX_WAIT)) {
                if (insideTaboo == null) {
                    insideTaboo = Taboo.getInstance(SYSTEM_PATH);
                }
                if (preasTaboo == null) {
                    preasTaboo = Taboo.getInstance(PREAS_PATH);
                }
                if (cloudTaboo == null) {
                    Taboo taboo = Taboo.getInstance(CLOUD_PATH);
                    if (taboo != null) {
                        cloudTaboo = taboo;
                    }
                } else if (ParseXml.getFileLastModify(CLOUD_PATH) > cloudTaboo.getLastmodify()) {
                    cloudTaboo = Taboo.getInstance(CLOUD_PATH);
                }
                if (sParam == null) {
                    sParam = FindTargetRules.SystemParam.getSystemParam();
                }
                lastReadTime = System.currentTimeMillis();
            }
        }
    }

    private static Taboo getCurrentTaboo() {
        Taboo currentTaboo = null;
        HashMap<Long, Taboo> tabooMap = new HashMap<>();
        Taboo taboo = insideTaboo;
        if (taboo != null) {
            tabooMap.put(Long.valueOf(taboo.getVersion()), insideTaboo);
        }
        Taboo taboo2 = cloudTaboo;
        if (taboo2 != null) {
            tabooMap.put(Long.valueOf(taboo2.getVersion()), cloudTaboo);
        }
        Taboo taboo3 = preasTaboo;
        if (taboo3 != null) {
            tabooMap.put(Long.valueOf(taboo3.getVersion()), preasTaboo);
        }
        long currentVersion = 0;
        for (Map.Entry<Long, Taboo> next : tabooMap.entrySet()) {
            long key = next.getKey().longValue();
            if (currentVersion < key) {
                currentVersion = key;
                currentTaboo = next.getValue();
            }
        }
        return currentTaboo;
    }
}
