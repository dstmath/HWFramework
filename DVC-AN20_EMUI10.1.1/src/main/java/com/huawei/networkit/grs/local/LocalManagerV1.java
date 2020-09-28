package com.huawei.networkit.grs.local;

import android.text.TextUtils;
import com.huawei.networkit.grs.common.Logger;
import com.huawei.networkit.grs.local.model.ApplicationBean;
import com.huawei.networkit.grs.local.model.CountryGroup;
import com.huawei.networkit.grs.local.model.Service;
import com.huawei.networkit.grs.local.model.Serving;
import com.huawei.networkit.grs.utils.Constant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocalManagerV1 extends AbstractLocalManager {
    private static final String APP_CONFIG_FILE_NAME = "grs_sdk_global_route_config.json";
    private static final String JSONKEY_APPLICATION = "application";
    private static final String TAG = "LocalManagerV1";

    public static class LazyHolder {
        static final LocalManagerV1 INSTANCE = new LocalManagerV1();
    }

    private LocalManagerV1() {
        if (loadLocalConfig(APP_CONFIG_FILE_NAME) == 0) {
            this.readLocalConfigOk = true;
        }
    }

    public static LocalManagerV1 getLocalManager() {
        return LazyHolder.INSTANCE;
    }

    @Override // com.huawei.networkit.grs.local.AbstractLocalManager
    public int parseAppBean(String configContent) {
        this.appGrs = new ApplicationBean();
        try {
            JSONObject jsonObjectApplication = new JSONObject(configContent).getJSONObject(JSONKEY_APPLICATION);
            String appName = jsonObjectApplication.getString("name");
            long appCacheControl = jsonObjectApplication.getLong(Constant.JSONKEY_CACHECONTROL);
            JSONArray jsArrayServices = jsonObjectApplication.getJSONArray(Constant.JSONKEY_SERVICES);
            this.appGrs.setName(appName);
            this.appGrs.setCacheControl(appCacheControl);
            List<String> arrayListServices = new ArrayList<>(16);
            if (jsArrayServices != null) {
                if (jsArrayServices.length() != 0) {
                    for (int i = 0; i < jsArrayServices.length(); i++) {
                        arrayListServices.add((String) jsArrayServices.get(i));
                    }
                    return 0;
                }
            }
            Logger.w(TAG, "local config application.services is not config or no any services");
            return -1;
        } catch (JSONException e) {
            Logger.w(TAG, "parse appbean failed maybe json style is wrong.", e);
            return -1;
        }
    }

    @Override // com.huawei.networkit.grs.local.AbstractLocalManager
    public int parseServices(String configContent) {
        JSONObject jsonObjectService;
        String str;
        String str2;
        JSONObject jsonObject;
        String str3;
        String countryId;
        String str4 = Constant.JSONKEY_COUNTRYGROUP;
        String str5 = Constant.JSONKEY_COUNTRYORAREAGROUP;
        try {
            JSONObject jsonObject2 = new JSONObject(configContent);
            JSONObject jsonObjectService2 = jsonObject2.getJSONObject(Constant.JSONKEY_SERVICES);
            Iterator<String> iteratorService = jsonObjectService2.keys();
            while (true) {
                int i = 0;
                if (!iteratorService.hasNext()) {
                    return 0;
                }
                String serviceName = iteratorService.next();
                Service service = new Service();
                service.setName(serviceName);
                if (TextUtils.isEmpty(serviceName) || !TextUtils.isEmpty((CharSequence) this.serviceHashMap.get(serviceName))) {
                    str = str4;
                    str2 = str5;
                    jsonObject = jsonObject2;
                    jsonObjectService = jsonObjectService2;
                } else {
                    this.serviceHashMap.put(serviceName, jsonObjectService2.toString());
                    JSONObject jsObject = jsonObjectService2.getJSONObject(serviceName);
                    service.setRouteBy(jsObject.getString(Constant.JSONKEY_ROUTEBY));
                    JSONArray jsArray = jsObject.getJSONArray(Constant.JSONKEY_SERVINGS);
                    while (true) {
                        jsonObject = jsonObject2;
                        if (i >= jsArray.length()) {
                            break;
                        }
                        JSONObject jsObject2 = (JSONObject) jsArray.get(i);
                        Serving serving = new Serving();
                        if (jsObject2.has(str5)) {
                            str3 = str4;
                            countryId = jsObject2.getString(str5);
                        } else if (jsObject2.has(str4)) {
                            str3 = str4;
                            countryId = jsObject2.getString(str4);
                        } else {
                            str3 = str4;
                            Logger.i(TAG, "maybe this service routeBy is unconditional.");
                            countryId = Route.NO_ROUTE_COUNTRYGROUPID;
                        }
                        serving.setCountryGroup(countryId);
                        JSONObject jsObjectAdress = jsObject2.getJSONObject(Constant.JSONKEY_ADDRESSES);
                        HashMap<String, String> mapAdress = new HashMap<>(16);
                        Iterator<String> iteratorAdress = jsObjectAdress.keys();
                        while (iteratorAdress.hasNext()) {
                            String mKey = iteratorAdress.next();
                            mapAdress.put(mKey, jsObjectAdress.getString(mKey));
                            iteratorAdress = iteratorAdress;
                            jsonObjectService2 = jsonObjectService2;
                        }
                        serving.setAddresses(mapAdress);
                        service.addServing(serving.getCountryGroup(), serving);
                        i++;
                        jsonObject2 = jsonObject;
                        str5 = str5;
                        str4 = str3;
                        jsonObjectService2 = jsonObjectService2;
                    }
                    str = str4;
                    str2 = str5;
                    jsonObjectService = jsonObjectService2;
                    List<CountryGroup> countryGroups = null;
                    if (jsObject.has(Constant.JSONKEY_COUNTRYORAREAGROUPS)) {
                        countryGroups = parseCustomCountry(null, jsObject.getJSONObject(Constant.JSONKEY_COUNTRYORAREAGROUPS));
                    } else if (jsObject.has(Constant.JSONKEY_COUNTRYGROUPS)) {
                        countryGroups = parseCustomCountry(null, jsObject.getJSONObject(Constant.JSONKEY_COUNTRYGROUPS));
                    } else {
                        Logger.i(TAG, "service use default countryOrAreaGroup");
                    }
                    service.setCountryGroups(countryGroups);
                    if (this.appGrs == null) {
                        this.appGrs = new ApplicationBean();
                    }
                    this.appGrs.addService(serviceName, service);
                }
                jsonObject2 = jsonObject;
                str5 = str2;
                str4 = str;
                jsonObjectService2 = jsonObjectService;
            }
        } catch (JSONException e) {
            Logger.w(TAG, "parse 1.0 services failed maybe because of json style.please check!", e);
            return -1;
        }
    }

    @Override // com.huawei.networkit.grs.local.AbstractLocalManager
    public int parseCountryGroups(String configContent) {
        JSONArray jsArrayCountries;
        String str = Constant.JSONKEY_COUNTRIES;
        int i = 16;
        this.countryGroups = new ArrayList(16);
        try {
            try {
                JSONObject jsonObject = new JSONObject(configContent);
                JSONObject jsonObjectCountryGroups = null;
                if (jsonObject.has(Constant.JSONKEY_COUNTRYORAREAGROUPS)) {
                    jsonObjectCountryGroups = jsonObject.getJSONObject(Constant.JSONKEY_COUNTRYORAREAGROUPS);
                } else if (jsonObject.has(Constant.JSONKEY_COUNTRYGROUPS)) {
                    jsonObjectCountryGroups = jsonObject.getJSONObject(Constant.JSONKEY_COUNTRYGROUPS);
                } else {
                    Logger.e(TAG, "maybe local config json is wrong because the default countryOrAreaGroups isn't config.");
                }
                if (jsonObjectCountryGroups == null) {
                    return -1;
                }
                if (jsonObjectCountryGroups.length() == 0) {
                    return 0;
                }
                Iterator<String> iterator = jsonObjectCountryGroups.keys();
                while (iterator.hasNext()) {
                    String groupId = iterator.next();
                    CountryGroup countryGroup = new CountryGroup();
                    countryGroup.setId(groupId);
                    JSONObject jsObjectCountry = jsonObjectCountryGroups.getJSONObject(groupId);
                    countryGroup.setName(jsObjectCountry.getString("name"));
                    countryGroup.setDescription(jsObjectCountry.getString(Constant.JSONKEY_DESCRIPTION));
                    if (jsObjectCountry.has(Constant.JSONKEY_COUNTRIESORAREAS)) {
                        jsArrayCountries = jsObjectCountry.getJSONArray(Constant.JSONKEY_COUNTRIESORAREAS);
                    } else if (jsObjectCountry.has(str)) {
                        jsArrayCountries = jsObjectCountry.getJSONArray(str);
                    } else {
                        Logger.w(TAG, "current country or area group has not config countries or areas.");
                        jsArrayCountries = null;
                    }
                    Set<String> hashSetCountries = new HashSet<>(i);
                    if (jsArrayCountries != null) {
                        if (jsArrayCountries.length() != 0) {
                            int i2 = 0;
                            while (i2 < jsArrayCountries.length()) {
                                hashSetCountries.add((String) jsArrayCountries.get(i2));
                                i2++;
                                str = str;
                            }
                            countryGroup.setCountries(hashSetCountries);
                            this.countryGroups.add(countryGroup);
                            str = str;
                            i = 16;
                        }
                    }
                    Logger.w(TAG, "local config countryGroups.groupId.countries is not config or no any countries");
                    return -1;
                }
                return 0;
            } catch (JSONException e) {
                e = e;
                Logger.w(TAG, "parse countrygroup failed maybe json style is wrong.", e);
                return -1;
            }
        } catch (JSONException e2) {
            e = e2;
            Logger.w(TAG, "parse countrygroup failed maybe json style is wrong.", e);
            return -1;
        }
    }

    @Override // com.huawei.networkit.grs.local.AbstractLocalManager
    public List<CountryGroup> parseCustomCountry(JSONArray countryGroupsArray, JSONObject countryGroupsObject) {
        if (countryGroupsObject == null || countryGroupsObject.length() == 0) {
            return new ArrayList();
        }
        try {
            List<CountryGroup> serviceCountryGroups = new ArrayList<>(16);
            Iterator<String> iterator = countryGroupsObject.keys();
            while (iterator.hasNext()) {
                String groupId = iterator.next();
                CountryGroup countryGroup = new CountryGroup();
                countryGroup.setId(groupId);
                JSONObject jsObjectCountry = countryGroupsObject.getJSONObject(groupId);
                countryGroup.setName(jsObjectCountry.getString("name"));
                countryGroup.setDescription(jsObjectCountry.getString(Constant.JSONKEY_DESCRIPTION));
                JSONArray jsArrayCountries = null;
                if (jsObjectCountry.has(Constant.JSONKEY_COUNTRIESORAREAS)) {
                    jsArrayCountries = jsObjectCountry.getJSONArray(Constant.JSONKEY_COUNTRIESORAREAS);
                } else if (jsObjectCountry.has(Constant.JSONKEY_COUNTRIES)) {
                    jsArrayCountries = jsObjectCountry.getJSONArray(Constant.JSONKEY_COUNTRIES);
                } else {
                    Logger.w(TAG, "current country or area group has not config countries or areas.");
                }
                Set<String> hashSetCountries = new HashSet<>(16);
                if (jsArrayCountries != null) {
                    if (jsArrayCountries.length() != 0) {
                        for (int i = 0; i < jsArrayCountries.length(); i++) {
                            hashSetCountries.add((String) jsArrayCountries.get(i));
                        }
                        countryGroup.setCountries(hashSetCountries);
                        serviceCountryGroups.add(countryGroup);
                    }
                }
                Logger.w(TAG, "local config countryGroups.groupId.countries is not config or no any countries");
                return new ArrayList();
            }
            return serviceCountryGroups;
        } catch (JSONException e) {
            Logger.w(TAG, "parse countryGroups failed maybe json style is wrong.", e);
            return new ArrayList();
        }
    }
}
