package com.huawei.networkit.grs.local;

import android.text.TextUtils;
import com.huawei.networkit.grs.GrsBaseInfo;
import com.huawei.networkit.grs.common.Logger;
import com.huawei.networkit.grs.local.model.ApplicationBean;
import com.huawei.networkit.grs.local.model.CountryGroup;
import com.huawei.networkit.grs.local.model.Service;
import com.huawei.networkit.grs.local.model.Serving;
import com.huawei.networkit.grs.utils.ContextUtil;
import com.huawei.networkit.grs.utils.Io;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AbstractLocalManager {
    private static final String REGEX_PATTERN = "^grs_sdk_global_route_config_[a-zA-Z]+\\.json$";
    private static final String TAG = "AbstractLocalManager";
    ApplicationBean appGrs;
    Map<String, String> country2GroupMap = new ConcurrentHashMap(16);
    List<CountryGroup> countryGroups;
    boolean readLocalConfigOk = false;
    Map<String, String> serviceHashMap = new ConcurrentHashMap(16);

    public abstract int parseAppBean(String str);

    public abstract int parseCountryGroups(String str);

    public abstract List<CountryGroup> parseCustomCountry(JSONArray jSONArray, JSONObject jSONObject);

    public abstract int parseServices(String str);

    /* access modifiers changed from: package-private */
    public int loadLocalConfig(String appConfigName) {
        int loadSuccessFlag = -1;
        if (loadAppLocalConfig(Io.getConfigContent(appConfigName)) == 0) {
            Logger.i(TAG, "load APP_CONFIG_FILE success.");
            loadSuccessFlag = 0;
        }
        try {
            String[] files = ContextUtil.getContext().getAssets().list("");
            if (files != null && files.length > 0) {
                for (String file : files) {
                    if (Pattern.matches(REGEX_PATTERN, file) && loadSdkLocalConfig(Io.getConfigContent(file)) == 0) {
                        Logger.i(TAG, "load SDK_CONFIG_FILE sucess.");
                        loadSuccessFlag = 0;
                    }
                }
            }
        } catch (IOException e) {
            Logger.w(TAG, "list assets files fail,please check if according to our standard config json files.");
        }
        return loadSuccessFlag;
    }

    private int loadAppLocalConfig(String configContent) {
        if (TextUtils.isEmpty(configContent)) {
            Logger.w(TAG, "getConfigMgr configContent is null.");
            return -1;
        }
        int result = parseCountryGroups(configContent);
        if (result != 0) {
            return result;
        }
        int result2 = parseAppBean(configContent);
        if (result2 != 0) {
            return result2;
        }
        return parseServices(configContent);
    }

    private int loadSdkLocalConfig(String configContent) {
        int result;
        if (TextUtils.isEmpty(configContent)) {
            Logger.w(TAG, "getConfigMgr configContent is null.");
            return -1;
        }
        List<CountryGroup> list = this.countryGroups;
        if ((list == null || list.isEmpty()) && (result = parseCountryGroups(configContent)) != 0) {
            return result;
        }
        return parseServices(configContent);
    }

    public boolean updateCountryGroupMap(GrsBaseInfo grsBaseInfo) {
        if (this.countryGroups == null) {
            Logger.w(TAG, "updateCountryGroupMap return null because {null == countryGroups}");
            return false;
        }
        this.country2GroupMap.put(Route.NO_ROUTEBY_COUNTRY, Route.NO_ROUTE_COUNTRYGROUPID);
        for (CountryGroup countryGroup : this.countryGroups) {
            if (countryGroup.getCountries().contains(grsBaseInfo.getIssueCountry())) {
                this.country2GroupMap.put(grsBaseInfo.getIssueCountry(), countryGroup.getId());
            }
            if (countryGroup.getCountries().contains(grsBaseInfo.getRegCountry())) {
                this.country2GroupMap.put(grsBaseInfo.getRegCountry(), countryGroup.getId());
            }
            if (countryGroup.getCountries().contains(grsBaseInfo.getSerCountry())) {
                this.country2GroupMap.put(grsBaseInfo.getSerCountry(), countryGroup.getId());
            }
        }
        return true;
    }

    private Map<String, String> createPrivateCountryGroupMap(List<CountryGroup> customCountryGroup, GrsBaseInfo grsBaseInfo) {
        Map<String, String> localCountry2GroupMap = new ConcurrentHashMap<>(16);
        localCountry2GroupMap.put(Route.NO_ROUTEBY_COUNTRY, Route.NO_ROUTE_COUNTRYGROUPID);
        for (CountryGroup countryGroup : customCountryGroup) {
            if (countryGroup.getCountries().contains(grsBaseInfo.getIssueCountry())) {
                localCountry2GroupMap.put(grsBaseInfo.getIssueCountry(), countryGroup.getId());
            }
            if (countryGroup.getCountries().contains(grsBaseInfo.getRegCountry())) {
                localCountry2GroupMap.put(grsBaseInfo.getRegCountry(), countryGroup.getId());
            }
            if (countryGroup.getCountries().contains(grsBaseInfo.getSerCountry())) {
                localCountry2GroupMap.put(grsBaseInfo.getSerCountry(), countryGroup.getId());
            }
        }
        return localCountry2GroupMap;
    }

    public String getUrlFromLocal(GrsBaseInfo grsBaseInfo, String serviceName, String key) {
        Map<String, String> addresses = getServicesUrlsFromLocal(grsBaseInfo, serviceName);
        if (addresses != null) {
            return addresses.get(key);
        }
        Logger.w(TAG, "addresses not found by routeby in local config{%s}", serviceName);
        return null;
    }

    public Map<String, String> getServicesUrlsFromLocal(GrsBaseInfo grsBaseInfo, String serviceName) {
        Serving serving;
        if (!this.readLocalConfigOk) {
            return null;
        }
        Service service = this.appGrs.getService(serviceName);
        if (service == null) {
            Logger.w(TAG, "service not found in local config{%s}", service);
            return null;
        }
        String country = Route.getRouteCountry(service.getRouteBy(), grsBaseInfo);
        if (country == null) {
            Logger.w(TAG, "country not found by routeby in local config{%s}", service.getRouteBy());
            return null;
        }
        List<CountryGroup> customCountryGroup = service.getCountryGroups();
        if (customCountryGroup == null || customCountryGroup.size() == 0) {
            serving = service.getServing(this.country2GroupMap.get(country));
        } else {
            serving = service.getServing(createPrivateCountryGroupMap(customCountryGroup, grsBaseInfo).get(country));
        }
        if (serving == null) {
            return null;
        }
        return serving.getAddresses();
    }
}
