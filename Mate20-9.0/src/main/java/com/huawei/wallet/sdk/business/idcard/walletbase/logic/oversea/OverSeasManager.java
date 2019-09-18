package com.huawei.wallet.sdk.business.idcard.walletbase.logic.oversea;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class OverSeasManager {
    private static final Object LOCK = new Object();
    private static final Object SYNCLOCK = new Object();
    private static OverSeasManager instance;
    private Map<String, String> addressMap = new HashMap();
    private Context context;

    private OverSeasManager(Context context2) {
        this.context = context2.getApplicationContext();
    }

    public static OverSeasManager getInstance(Context context2) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new OverSeasManager(context2);
                }
            }
        }
        return instance;
    }

    public String getGrsUrlSync(String modelName) {
        LogC.i("getGrsUrlSync begin:" + modelName, false);
        long currentTimeMillis = System.currentTimeMillis();
        initGrs(getCountryCodeFromTA());
        String urlResult = this.addressMap.get(modelName.toUpperCase());
        LogC.i("getGrsUrlSync end", false);
        return urlResult;
    }

    public void getUrlMapFromGrs(String serviceCountryCode) {
        Map<String, String> totalUrls = getUrlsFromTA();
        if (totalUrls.isEmpty()) {
            LogC.e("OverSeasManager getUrlMapFromGrs bad, get url map empty.", false);
        } else {
            this.addressMap = totalUrls;
        }
        LogC.d("OverSeasManager getUrlMapFromGrs END.urlMap is empty :" + totalUrls.isEmpty(), false);
    }

    private Map<String, String> getUrlsFromTA() {
        Map<String, String> totalUrls = new HashMap<>();
        try {
            String routeStr = WalletTaManager.getInstance(this.context).getRouterInfo();
            if (!TextUtils.isEmpty(routeStr)) {
                try {
                    JSONObject jsonObject = new JSONObject(routeStr);
                    Iterator iterator = jsonObject.keys();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        totalUrls.put(key, jsonObject.getString(key));
                    }
                } catch (JSONException e) {
                    LogC.e("hit JSONException when transfer string to json.", false);
                }
            }
        } catch (Exception e2) {
            LogC.e("hit Exception when getRouterInfo from TA.", false);
        }
        return totalUrls;
    }

    public void initGrs(String countryCode) {
        getUrlMapFromGrs(countryCode);
    }

    public String getCountryCodeFromTA() {
        try {
            return WalletTaManager.getInstance(this.context).getCountryCode();
        } catch (Exception e) {
            LogC.e("hit Exception when getCountryCode from TA.", false);
            return "";
        }
    }
}
