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

public class LocalManagerV2 extends AbstractLocalManager {
    private static final String APP_CONFIG_FILE_NAME = "grs_app_global_route_config.json";
    private static final String JSONKEY_APPLICATION = "applications";
    private static final String JSONKEY_CUSTOMSERVICES = "customservices";
    private static final String TAG = "LocalManagerV2";

    public static class LazyHolder {
        static final LocalManagerV2 INSTANCE = new LocalManagerV2();
    }

    private LocalManagerV2() {
        if (loadLocalConfig(APP_CONFIG_FILE_NAME) == 0) {
            this.readLocalConfigOk = true;
        }
    }

    public static LocalManagerV2 getLocalManagerV2() {
        return LazyHolder.INSTANCE;
    }

    @Override // com.huawei.networkit.grs.local.AbstractLocalManager
    public int parseAppBean(String configContent) {
        this.appGrs = new ApplicationBean();
        try {
            JSONObject appJsonObj = new JSONObject(configContent).getJSONArray(JSONKEY_APPLICATION).getJSONObject(0);
            this.appGrs.setName(appJsonObj.getString("name"));
            JSONArray jsArrayServices = appJsonObj.getJSONArray(Constant.JSONKEY_SERVICES);
            List<String> arrayListServices = new ArrayList<>(16);
            if (jsArrayServices != null) {
                if (jsArrayServices.length() != 0) {
                    for (int i = 0; i < jsArrayServices.length(); i++) {
                        arrayListServices.add((String) jsArrayServices.get(i));
                    }
                    if (appJsonObj.has(JSONKEY_CUSTOMSERVICES)) {
                        parseUnionServices(appJsonObj.getJSONArray(JSONKEY_CUSTOMSERVICES));
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
        try {
            parseUnionServices(new JSONObject(configContent).getJSONArray(Constant.JSONKEY_SERVICES));
            return 0;
        } catch (JSONException e) {
            Logger.w(TAG, "parse 2.0 services failed maybe because of json style.please check!", e);
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
                JSONArray jsonArrayCountryGroups = null;
                if (jsonObject.has(Constant.JSONKEY_COUNTRYORAREAGROUPS)) {
                    jsonArrayCountryGroups = jsonObject.getJSONArray(Constant.JSONKEY_COUNTRYORAREAGROUPS);
                } else if (jsonObject.has(Constant.JSONKEY_COUNTRYGROUPS)) {
                    jsonArrayCountryGroups = jsonObject.getJSONArray(Constant.JSONKEY_COUNTRYGROUPS);
                } else {
                    Logger.e(TAG, "maybe local config json is wrong because the default countryOrAreaGroups isn't config.");
                }
                if (jsonArrayCountryGroups == null) {
                    return -1;
                }
                if (jsonArrayCountryGroups.length() == 0) {
                    return 0;
                }
                int i2 = 0;
                while (i2 < jsonArrayCountryGroups.length()) {
                    JSONObject currentElement = jsonArrayCountryGroups.getJSONObject(i2);
                    CountryGroup countryGroup = new CountryGroup();
                    countryGroup.setId(currentElement.getString(Constant.JSONKEY_ID));
                    countryGroup.setName(currentElement.getString("name"));
                    countryGroup.setDescription(currentElement.getString(Constant.JSONKEY_DESCRIPTION));
                    if (currentElement.has(Constant.JSONKEY_COUNTRIESORAREAS)) {
                        jsArrayCountries = currentElement.getJSONArray(Constant.JSONKEY_COUNTRIESORAREAS);
                    } else if (currentElement.has(str)) {
                        jsArrayCountries = currentElement.getJSONArray(str);
                    } else {
                        Logger.w(TAG, "current country or area group has not config countries or areas.");
                        jsArrayCountries = null;
                    }
                    Set<String> hashSetCountries = new HashSet<>(i);
                    if (jsArrayCountries != null) {
                        if (jsArrayCountries.length() != 0) {
                            int j = 0;
                            while (j < jsArrayCountries.length()) {
                                hashSetCountries.add((String) jsArrayCountries.get(j));
                                j++;
                                str = str;
                            }
                            countryGroup.setCountries(hashSetCountries);
                            this.countryGroups.add(countryGroup);
                            i2++;
                            str = str;
                            i = 16;
                        }
                    }
                    Logger.w(TAG, "local config countryGroups.countries is not config or no any countries");
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
        if (countryGroupsArray == null || countryGroupsArray.length() == 0) {
            return new ArrayList();
        }
        try {
            int i = 16;
            List<CountryGroup> serviceCountryGroups = new ArrayList<>(16);
            int i2 = 0;
            while (i2 < countryGroupsArray.length()) {
                JSONObject currentElement = countryGroupsArray.getJSONObject(i2);
                CountryGroup countryGroup = new CountryGroup();
                countryGroup.setId(currentElement.getString(Constant.JSONKEY_ID));
                countryGroup.setName(currentElement.getString("name"));
                countryGroup.setDescription(currentElement.getString(Constant.JSONKEY_DESCRIPTION));
                JSONArray jsArrayCountries = null;
                if (currentElement.has(Constant.JSONKEY_COUNTRIESORAREAS)) {
                    jsArrayCountries = currentElement.getJSONArray(Constant.JSONKEY_COUNTRIESORAREAS);
                } else if (currentElement.has(Constant.JSONKEY_COUNTRIES)) {
                    jsArrayCountries = currentElement.getJSONArray(Constant.JSONKEY_COUNTRIES);
                } else {
                    Logger.w(TAG, "current country or area group has not config countries or areas.");
                }
                Set<String> hashSetCountries = new HashSet<>(i);
                if (jsArrayCountries != null) {
                    if (jsArrayCountries.length() != 0) {
                        for (int j = 0; j < jsArrayCountries.length(); j++) {
                            hashSetCountries.add((String) jsArrayCountries.get(j));
                        }
                        countryGroup.setCountries(hashSetCountries);
                        serviceCountryGroups.add(countryGroup);
                        i2++;
                        i = 16;
                    }
                }
                Logger.w(TAG, "local config countryGroups.countries is not config or no any countries");
                return new ArrayList();
            }
            return serviceCountryGroups;
        } catch (JSONException e) {
            Logger.w(TAG, "parse countrygroup failed maybe json style is wrong.", e);
            return new ArrayList();
        }
    }

    private void parseUnionServices(JSONArray jsonArrayService) throws JSONException {
        JSONArray jSONArray = jsonArrayService;
        if (!(jSONArray == null || jsonArrayService.length() == 0)) {
            int i = 0;
            while (i < jsonArrayService.length()) {
                JSONObject currentElement = jSONArray.getJSONObject(i);
                Service service = new Service();
                String serviceName = currentElement.getString("name");
                service.setName(serviceName);
                if (!TextUtils.isEmpty(serviceName) && TextUtils.isEmpty((CharSequence) this.serviceHashMap.get(serviceName))) {
                    this.serviceHashMap.put(serviceName, currentElement.toString());
                    service.setRouteBy(currentElement.getString(Constant.JSONKEY_ROUTEBY));
                    JSONArray jsArray = currentElement.getJSONArray(Constant.JSONKEY_SERVINGS);
                    for (int j = 0; j < jsArray.length(); j++) {
                        JSONObject jsObject2 = (JSONObject) jsArray.get(j);
                        Serving serving = new Serving();
                        String countryId = Route.NO_ROUTE_COUNTRYGROUPID;
                        if (jsObject2.has(Constant.JSONKEY_COUNTRYORAREAGROUP)) {
                            countryId = jsObject2.getString(Constant.JSONKEY_COUNTRYORAREAGROUP);
                        } else if (jsObject2.has(Constant.JSONKEY_COUNTRYGROUP)) {
                            countryId = jsObject2.getString(Constant.JSONKEY_COUNTRYGROUP);
                        } else {
                            Logger.i(TAG, "maybe this service routeBy is unconditional.");
                        }
                        serving.setCountryGroup(countryId);
                        JSONObject jsObjectAdress = jsObject2.getJSONObject(Constant.JSONKEY_ADDRESSES);
                        HashMap<String, String> mapAdress = new HashMap<>(16);
                        Iterator<String> iteratorAdress = jsObjectAdress.keys();
                        while (iteratorAdress.hasNext()) {
                            String mKey = iteratorAdress.next();
                            mapAdress.put(mKey, jsObjectAdress.getString(mKey));
                        }
                        serving.setAddresses(mapAdress);
                        service.addServing(serving.getCountryGroup(), serving);
                    }
                    List<CountryGroup> countryGroups = null;
                    if (currentElement.has(Constant.JSONKEY_COUNTRYORAREAGROUPS)) {
                        countryGroups = parseCustomCountry(currentElement.getJSONArray(Constant.JSONKEY_COUNTRYORAREAGROUPS), null);
                    } else if (currentElement.has(Constant.JSONKEY_COUNTRYGROUPS)) {
                        countryGroups = parseCustomCountry(currentElement.getJSONArray(Constant.JSONKEY_COUNTRYGROUPS), null);
                    } else {
                        Logger.i(TAG, "service use default countryOrAreaGroup");
                    }
                    service.setCountryGroups(countryGroups);
                    if (this.appGrs == null) {
                        this.appGrs = new ApplicationBean();
                    }
                    this.appGrs.addService(serviceName, service);
                }
                i++;
                jSONArray = jsonArrayService;
            }
        }
    }
}
