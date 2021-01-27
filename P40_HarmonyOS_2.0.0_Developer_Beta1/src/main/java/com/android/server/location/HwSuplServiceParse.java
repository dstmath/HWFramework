package com.android.server.location;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HwSuplServiceParse {
    private static final String CONFIG_HWSUPL_VERSION_IN_DATABASE = "hwsupl_config_version";
    private static final String JSON_TAG_ADDRESS = "address";
    private static final String JSON_TAG_COUNTRY = "country";
    private static final String JSON_TAG_PORT = "port";
    private static final String JSON_TAG_PRODUCT = "product";
    private static final String JSON_TAG_PRODUCT_NAME = "productname";
    private static final String JSON_TAG_PRODUCT_VERSION = "version";
    private static final String JSON_TAG_REQUIREMENT = "requirement";
    private static final String PARA_DEFAULT_VALUE = "***";
    private static final int PARA_END_INDEX = 3;
    private static final int PARA_START_INDEX = 0;
    private static final String PARA_SUPL_PORT = "supl_port_hw";
    private static final String PARA_SUPL_SERVER = "supl_server_hw";
    private static final int PARA_UPGRADE_FILE_NOTEXIST = 0;
    private static final int PARA_UPGRADE_RESPONSE_FILE_ERROR = 6;
    private static final String PRODUCT_LOCAL_REGION = SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, "");
    private static final String PRODUCT_PRODUCT_NAME = SystemProperties.get("ro.product.name", "");
    private static final String PRODUCT_PRODUCT_VERSION = SystemProperties.get("ro.build.display.id", "");
    private static final int PRODUCT_VERSION_FIELD_NUMBER = 4;
    private static final String TAG = "HwSuplServiceParse";
    private static volatile HwSuplServiceParse sInstance;
    private String hwSuplAddress = "";
    private String hwSuplPort = "";
    private Context mContext;

    private HwSuplServiceParse(Context context) {
        this.mContext = context;
    }

    public static HwSuplServiceParse getInstance(Context context) {
        HwSuplServiceParse hwSuplServiceParse;
        synchronized (HwSuplServiceParse.class) {
            if (sInstance == null) {
                sInstance = new HwSuplServiceParse(context);
            }
            hwSuplServiceParse = sInstance;
        }
        return hwSuplServiceParse;
    }

    public boolean isMatchRequirement(String filePath, boolean isCotaBroadcast) {
        String jsonStr = LbsParaUpdater.getInstance(this.mContext).readJsonFile(filePath);
        LBSLog.e(TAG, false, "readJsonFile %{public}s", jsonStr);
        if (jsonStr == null || jsonStr.length() == 0) {
            reportParseError(isCotaBroadcast, 0);
            LBSLog.w(TAG, false, "parseConfigFile fail, because file not exists", new Object[0]);
            return false;
        }
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONArray configArray = jsonObj.optJSONArray(JSON_TAG_REQUIREMENT);
            if (configArray == null) {
                reportParseError(isCotaBroadcast, 6);
                LBSLog.e(TAG, false, "configArray is null", new Object[0]);
                return false;
            }
            int length = configArray.length();
            for (int i = 0; i < length; i++) {
                Object obj = configArray.opt(i);
                if (!(obj instanceof JSONObject)) {
                    reportParseError(isCotaBroadcast, 6);
                    LBSLog.w(TAG, false, "parseConfigFile fail, because file config parse error", new Object[0]);
                    return false;
                } else if (isMatchCountryAndProductAndVersion((JSONObject) obj)) {
                    this.hwSuplAddress = jsonObj.optString(JSON_TAG_ADDRESS);
                    this.hwSuplPort = jsonObj.optString(JSON_TAG_PORT);
                    return true;
                }
            }
            LBSLog.e(TAG, false, "do not match requirement ", new Object[0]);
            return false;
        } catch (JSONException e) {
            LBSLog.e(TAG, false, " parse requirementJson JSONException", new Object[0]);
            return false;
        }
    }

    private boolean isMatchCountryAndProductAndVersion(JSONObject configObj) {
        String paraCountry = configObj.optString(JSON_TAG_COUNTRY);
        if ("".equals(paraCountry) || paraCountry == null) {
            LBSLog.e(TAG, false, "can not get the paraCountry", new Object[0]);
            return false;
        } else if (!PRODUCT_LOCAL_REGION.equals(paraCountry) && !PARA_DEFAULT_VALUE.equals(paraCountry)) {
            return false;
        } else {
            try {
                JSONArray productArray = configObj.getJSONArray(JSON_TAG_PRODUCT);
                if (productArray == null) {
                    LBSLog.e(TAG, false, "configArray is null", new Object[0]);
                    return false;
                }
                int length = productArray.length();
                for (int i = 0; i < length; i++) {
                    Object obj = productArray.opt(i);
                    if (!(obj instanceof JSONObject)) {
                        LBSLog.w(TAG, false, "parseparseCountry fail", new Object[0]);
                        return false;
                    } else if (isMatchProductAndVersion((JSONObject) obj)) {
                        return true;
                    }
                }
                return false;
            } catch (JSONException e) {
                LBSLog.e(TAG, false, "parseCountry JSONException", new Object[0]);
                return false;
            }
        }
    }

    private boolean isMatchProductAndVersion(JSONObject configObj) {
        String paraProdcutName = configObj.optString(JSON_TAG_PRODUCT_NAME);
        LBSLog.i(TAG, false, " paraProdcutName  %{public}s ", paraProdcutName);
        String paraProdcutVersion = configObj.optString("version");
        LBSLog.i(TAG, false, " paraProdcutVersion %{public}s ", paraProdcutVersion);
        if ("".equals(paraProdcutName) || paraProdcutName == null || "".equals(paraProdcutVersion) || paraProdcutVersion == null) {
            LBSLog.e(TAG, false, "can not get the paraProdcutName or paraProdcutVersion", new Object[0]);
            return false;
        }
        boolean isMatchHwSuplServer = false;
        char[] productNames = PRODUCT_PRODUCT_NAME.toCharArray();
        char[] configProdcutNames = paraProdcutName.toCharArray();
        if (configProdcutNames.length > productNames.length) {
            LBSLog.i(TAG, false, "productNames length is not match", new Object[0]);
            return false;
        }
        for (int i = 0; i < configProdcutNames.length; i++) {
            if (productNames[i] == configProdcutNames[i] || configProdcutNames[i] == '*') {
                isMatchHwSuplServer = true;
            } else {
                LBSLog.i(TAG, false, "configProdcutNames false", new Object[0]);
                return false;
            }
        }
        return isMatchHwSuplServer && isMatchVersion(paraProdcutVersion);
    }

    private boolean isMatchVersion(String version) {
        String subVersion;
        if ("".equals(version) || version == null) {
            LBSLog.e(TAG, false, "can not get the version", new Object[0]);
            return false;
        }
        boolean isMatchHwSuplServer = true;
        try {
            String[] productVersionStrs = version.split(AwarenessInnerConstants.DASH_KEY);
            String[] splitVersionOnes = PRODUCT_PRODUCT_VERSION.split("\\.");
            if (splitVersionOnes.length < 4) {
                LBSLog.e(TAG, false, "system subVersion is wrong", new Object[0]);
                return false;
            }
            if (splitVersionOnes[3].contains("\\(")) {
                subVersion = splitVersionOnes[3].split("\\(")[0];
            } else {
                subVersion = splitVersionOnes[3];
            }
            int productVersion = getProductVersion(subVersion);
            if (productVersion == -1) {
                LBSLog.e(TAG, false, "productVersion == -1", new Object[0]);
                return false;
            }
            if (!PARA_DEFAULT_VALUE.equals(productVersionStrs[0]) && Integer.parseInt(productVersionStrs[0]) > productVersion) {
                isMatchHwSuplServer = false;
            }
            if (PARA_DEFAULT_VALUE.equals(productVersionStrs[1]) || Integer.parseInt(productVersionStrs[1]) >= productVersion) {
                return isMatchHwSuplServer;
            }
            return false;
        } catch (IndexOutOfBoundsException e) {
            LBSLog.e(TAG, false, "parseVersion IndexOutOfBoundsException", new Object[0]);
            return false;
        } catch (ClassCastException e2) {
            LBSLog.e(TAG, false, "parseVersion ClassCastException", new Object[0]);
            return false;
        } catch (NumberFormatException e3) {
            LBSLog.e(TAG, false, "parseVersion NumberFormatException", new Object[0]);
            return false;
        }
    }

    private int getProductVersion(String subVersion) {
        if ("".equals(subVersion) || subVersion == null) {
            LBSLog.e(TAG, false, "subVersion is null", new Object[0]);
            return -1;
        }
        LBSLog.i(TAG, false, "Version  %{public}s", subVersion);
        int firstLetter = 0;
        while (firstLetter < subVersion.length() && Character.isDigit(subVersion.charAt(firstLetter))) {
            firstLetter++;
        }
        String verionWithoutLetter = subVersion.substring(0, firstLetter);
        if (verionWithoutLetter.length() == 0) {
            return 0;
        }
        int productVersion = Integer.parseInt(verionWithoutLetter);
        LBSLog.i(TAG, false, "productVersion  %{public}d", Integer.valueOf(productVersion));
        return productVersion;
    }

    private void reportParseError(boolean isCotaBroadcast, int errorCode) {
        if (isCotaBroadcast) {
            LbsParaUpdater.getInstance(this.mContext).responseForParaUpdate(errorCode);
        }
    }

    public void updateConfigToDataBase(int hwSuplconfigVersion) {
        LBSLog.i(TAG, false, "parse hwsuplconfig file and restore configration to DB", new Object[0]);
        Settings.Global.putString(this.mContext.getContentResolver(), "supl_server_hw", this.hwSuplAddress);
        Settings.Global.putString(this.mContext.getContentResolver(), "supl_port_hw", this.hwSuplPort);
        Settings.Global.putInt(this.mContext.getContentResolver(), CONFIG_HWSUPL_VERSION_IN_DATABASE, hwSuplconfigVersion);
    }
}
