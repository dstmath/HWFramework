package com.huawei.i18n.taboo;

import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.taboo.TabooReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;

public class Taboo {
    private long lastmodify = 0;
    private List<String> mLanguageList = null;
    private TabooConfig mTabooConfig = null;
    private WeakHashMap<String, HashMap<String, String>> mTabooDataMap = null;
    private String path = null;
    private long version = 0;

    public class TabooConfig {
        private static final String SEP = ",";
        private List<String> cityList = new ArrayList();
        private HashMap<String, String> configDatas = null;
        private List<String> langList = new ArrayList();
        private List<String> regionList = new ArrayList();

        public TabooConfig(HashMap<String, String> data) {
            if (!data.isEmpty()) {
                this.configDatas = data;
                init(data.get(TabooReader.ParamType.CITY_NAME.getScopeName()), data.get(TabooReader.ParamType.LANGUAGE_NAME.getScopeName()), data.get(TabooReader.ParamType.REGION_NAME.getScopeName()));
            }
        }

        private void init(String citys, String langs, String regions) {
            if (citys != null && !citys.isEmpty()) {
                this.cityList = Arrays.asList(citys.split(SEP));
            }
            if (langs != null && !langs.isEmpty()) {
                this.langList = Arrays.asList(langs.split(SEP));
            }
            if (regions != null && !regions.isEmpty()) {
                this.regionList = Arrays.asList(regions.split(SEP));
            }
        }

        public List<String> getCityList() {
            return this.cityList;
        }

        public List<String> getLangList() {
            return this.langList;
        }

        public List<String> getRegionList() {
            return this.regionList;
        }

        public String getValue(String key) {
            return this.configDatas.get(key);
        }
    }

    private Taboo() {
    }

    public static Taboo getInstance(String path2) {
        Taboo taboo = new Taboo();
        taboo.version = ParseXml.getVersion(path2);
        if (taboo.version == 0) {
            return null;
        }
        taboo.lastmodify = ParseXml.getFileLastModify(path2);
        taboo.path = path2;
        taboo.mLanguageList = ParseXml.getXmlLanguageList(path2);
        Objects.requireNonNull(taboo);
        taboo.mTabooConfig = new TabooConfig(ParseXml.parseConfigXml(path2));
        if (taboo.mTabooConfig.configDatas == null) {
            return null;
        }
        return taboo;
    }

    public String getData(String localeTag, String key) {
        HashMap<String, String> tabooData;
        if (this.mTabooDataMap == null) {
            this.mTabooDataMap = new WeakHashMap<>();
        }
        if (this.mTabooDataMap.containsKey(localeTag) && (tabooData = this.mTabooDataMap.get(localeTag)) != null && tabooData.containsKey(key)) {
            return tabooData.get(key);
        }
        HashMap<String, String> map = ParseXml.parse(this.path, localeTag);
        if (map.isEmpty()) {
            return StorageManagerExt.INVALID_KEY_DESC;
        }
        this.mTabooDataMap.put(localeTag, map);
        return map.get(key);
    }

    public TabooConfig getmTabooConfig() {
        return this.mTabooConfig;
    }

    public long getVersion() {
        return this.version;
    }

    public long getLastmodify() {
        return this.lastmodify;
    }

    public List<String> getLanguageList() {
        return this.mLanguageList;
    }
}
