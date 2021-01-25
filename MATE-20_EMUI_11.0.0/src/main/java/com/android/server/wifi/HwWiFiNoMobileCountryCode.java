package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HwWiFiNoMobileCountryCode {
    private static final String ACTION_WIFI_COUNTRY_CODE = "com.android.net.wifi.countryCode";
    private static final int COUNTRY_CODE_EID = 7;
    private static final int COUNTRY_CODE_LENGTH = 2;
    private static final String COUNTRY_CODE_NO_MOBILE = "ZZ";
    private static final int COUNTRY_CODE_OFFSET = 0;
    private static final int COUNTRY_CODE_SIZE = 2;
    private static final String EXTRA_FORCE_SET_WIFI_CCODE = "isWifiConnected";
    private static final int FEATURE_WIFI_CCODE_AP_AROUND = 2;
    private static final int FEATURE_WIFI_CCODE_AP_CONNECTED = 4;
    private static final int FEATURE_WIFI_CCODE_PASSIVE_SCAN = 1;
    private static final int FEATURE_WIFI_CCODE_USER_LOCALE = 8;
    private static final int FROM_CONNECTION_INFO = 2;
    private static final int FROM_NO_MOBILE = 3;
    private static final int FROM_REGION = 4;
    private static final int FROM_SCAN_RESULTS = 1;
    private static final String HW_SYSTEM_PERMISSION = "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM";
    private static final String HW_VSIM_SERVICE_STATE_CHANGED = "com.huawei.vsim.action.VSIM_REG_PLMN_CHANGED";
    private static final int MAX_LIST_SIZE = 3;
    private static final int OPERATOR_NUMERIC_LENGTH = 5;
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final String PROPERTY_GLOBAL_WIFI_ONLY = "ro.radio.noril";
    private static final int SCAN_RESULTS_MIN_NUMBER = 3;
    private static final int SUBSCRIPTION_ID = 0;
    private static final String TAG = "HwWiFiNoMobileCountryCode";
    private static final String WHITE_CARD_NETWORK_MCCMNC = "00101";
    private Map<String, String> bssidAndCountryCodeMap = new HashMap();
    private List<List<String>> bssidListBuffer = new ArrayList();
    private final HashSet<String> countryCodeHashSet = new HashSet<>(Arrays.asList("AA", "AD", "AE", "AF", "AG", "AI", "AL", "AM", "AN", "AO", "AQ", "AR", "AS", "AT", "AU", "AW", "AX", "AZ", "BA", "BB", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BL", "BM", "BN", "BO", "BQ", "BR", "BS", "BT", "BV", "BW", "BY", "BZ", "CA", "CC", "CD", "CF", "CG", "CH", "CI", "CK", "CL", "CM", "CN", "CO", "CR", "CS", "CU", "CV", "CW", "CX", "CY", "CZ", "DC", "DE", "DF", "DH", "DJ", "DK", "DM", "DO", "DZ", "EC", "EE", "EG", "EH", "EN", "ER", "ES", "ET", "EU", "FE", "FF", "FI", "FJ", "FK", "FM", "FO", "FR", "FX", "GA", "GB", "GD", "GE", "GF", "GG", "GH", "GI", "GL", "GM", "GN", "GP", "GQ", "GR", "GS", "GT", "GU", "GW", "GY", "HK", "HM", "HN", "HR", "HT", "HU", "ID", "IE", "IL", "IM", "IN", "IO", "IQ", "IR", "IS", "IT", "JE", "JM", "JO", "JP", "KE", "KG", "KH", "KI", "KM", "KN", "KP", "KR", "KW", "KY", "KZ", "LA", "LB", "LC", "LI", "LK", "LR", "LS", "LT", "LU", "LV", "LY", "MA", "MC", "MD", "ME", "MF", "MG", "MH", "MK", "ML", "MM", "MN", "MO", "MP", "MQ", "MR", "MS", "MT", "MU", "MV", "MW", "MX", "MY", "MZ", "NA", "NC", "NE", "NF", "NG", "NI", "NL", "NO", "NP", "NR", "NU", "NZ", "OM", "PA", "PE", "PF", "PG", "PH", "PK", "PL", "PM", "PN", "PR", "PS", "PT", "PW", "PY", "QA", "RE", "RK", "RO", "RS", "RU", "RW", "SA", "SB", "SC", "SD", "SE", "SG", "SH", "SI", "SJ", "SK", "SL", "SM", "SN", "SO", "SR", "SS", "ST", "SV", "SX", "SY", "SZ", "TC", "TD", "TF", "TG", "TH", "TJ", "TK", "TL", "TM", "TN", "TO", "TR", "TT", "TV", "TW", "TZ", "UA", "UD", "UG", "UM", "UN", "UR", "US", "UY", "UZ", "VA", "VC", "VE", "VG", "VI", "VL", "VN", "VU", "WF", "WI", "WS", "XK", "YE", "YG", "YT", "YU", "ZA", "ZM", "ZR", "ZW", COUNTRY_CODE_NO_MOBILE));
    private Context mContext;
    private String mCountryCodeFromConnectionInfo = null;
    private String mCountryCodeFromMonitor = null;
    private String mCountryCodeFromRegion = null;
    private String mCountryCodeFromScanResults = null;
    private CountryCodeReceiver mCountryCodeReceiver;
    private HwWifiCHRService mHwWifiChrService;
    private boolean mIsConnected = false;
    private int mWifiCountryCodeConf = SystemProperties.getInt("hw_mc.wifi.ccode_no_mcc", 0);
    private WifiManager mWifiMgr = null;

    public HwWiFiNoMobileCountryCode(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mCountryCodeReceiver = new CountryCodeReceiver();
            IntentFilter myFilter = new IntentFilter();
            if (this.mWifiCountryCodeConf != 0) {
                myFilter.addAction("android.intent.action.AIRPLANE_MODE");
                myFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                myFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            }
            if ((this.mWifiCountryCodeConf & 1) != 0) {
                this.mCountryCodeFromMonitor = COUNTRY_CODE_NO_MOBILE;
            }
            if ((this.mWifiCountryCodeConf & 2) != 0) {
                myFilter.addAction("android.net.wifi.SCAN_RESULTS");
            }
            if ((this.mWifiCountryCodeConf & 4) != 0) {
                myFilter.addAction("android.net.wifi.STATE_CHANGE");
            }
            if ((this.mWifiCountryCodeConf & 8) != 0) {
                myFilter.addAction("android.intent.action.LOCALE_CHANGED");
            }
            this.mContext.registerReceiver(this.mCountryCodeReceiver, myFilter);
            this.mHwWifiChrService = HwWifiServiceFactory.getHwWifiCHRService();
            return;
        }
        HwHiLog.e(TAG, false, "HwWiFiNoMobileCountryCode context is null", new Object[0]);
    }

    private class CountryCodeReceiver extends BroadcastReceiver {
        private CountryCodeReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (HwWiFiNoMobileCountryCode.this.mWifiMgr == null) {
                    HwWiFiNoMobileCountryCode.this.mWifiMgr = (WifiManager) context.getSystemService("wifi");
                }
                if ("android.intent.action.AIRPLANE_MODE".equals(intent.getAction()) || "android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction()) || HwWiFiNoMobileCountryCode.HW_VSIM_SERVICE_STATE_CHANGED.equals(intent.getAction())) {
                    if (!HwWiFiNoMobileCountryCode.this.isMobilePhoneNoService()) {
                        HwWiFiNoMobileCountryCode.this.mCountryCodeFromScanResults = null;
                        HwWiFiNoMobileCountryCode.this.mCountryCodeFromConnectionInfo = null;
                    }
                    Intent intentCountryCode = new Intent(HwWiFiNoMobileCountryCode.ACTION_WIFI_COUNTRY_CODE);
                    intentCountryCode.putExtra(HwWiFiNoMobileCountryCode.EXTRA_FORCE_SET_WIFI_CCODE, HwWiFiNoMobileCountryCode.this.mIsConnected);
                    HwWiFiNoMobileCountryCode.this.mContext.sendBroadcast(intentCountryCode, HwWiFiNoMobileCountryCode.HW_SYSTEM_PERMISSION);
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) {
                    if (intent.getIntExtra("wifi_state", 4) == 3) {
                        HwWiFiNoMobileCountryCode.this.mWifiMgr.setCountryCode(HwWiFiNoMobileCountryCode.COUNTRY_CODE_NO_MOBILE);
                    }
                } else if ("android.net.wifi.SCAN_RESULTS".equals(intent.getAction())) {
                    HwWiFiNoMobileCountryCode.this.handleScanResultAction();
                } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    HwWiFiNoMobileCountryCode.this.handleNetworkStateChangeAction(intent);
                } else if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                    HwWiFiNoMobileCountryCode.this.handleRegionChangeAction();
                } else {
                    HwHiLog.d(HwWiFiNoMobileCountryCode.TAG, false, "Do not process the broadcast", new Object[0]);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScanResultAction() {
        if ((this.mWifiCountryCodeConf & 2) == 0 || !isMobilePhoneNoService()) {
            this.mCountryCodeFromScanResults = null;
            return;
        }
        String countryCode = getCountryCodeFromScanResults(this.mWifiMgr.getScanResults());
        this.mCountryCodeFromScanResults = countryCode;
        if (!isValidCountryCode(countryCode)) {
            if (!TextUtils.isEmpty(this.mCountryCodeFromRegion)) {
                countryCode = this.mCountryCodeFromRegion;
            } else {
                countryCode = this.mCountryCodeFromMonitor;
            }
        }
        if (isValidCountryCode(countryCode) && !countryCode.equals(this.mWifiMgr.getCountryCode())) {
            this.mWifiMgr.setCountryCode(countryCode);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRegionChangeAction() {
        String countryCode = getCountryCodeByRegion();
        this.mCountryCodeFromRegion = countryCode;
        if (!isValidCountryCode(countryCode)) {
            if (!TextUtils.isEmpty(this.mCountryCodeFromScanResults)) {
                countryCode = this.mCountryCodeFromScanResults;
            } else {
                countryCode = this.mCountryCodeFromMonitor;
            }
        } else if (!TextUtils.isEmpty(this.mCountryCodeFromScanResults)) {
            return;
        }
        if (isValidCountryCode(countryCode) && !countryCode.equals(this.mWifiMgr.getCountryCode())) {
            this.mWifiMgr.setCountryCode(countryCode);
        }
    }

    public String getCountryCodeInNoMobileScene() {
        int codeType;
        String countryCode;
        WifiManager wifiManager;
        if (!isMobilePhoneNoService()) {
            return "";
        }
        if (this.mIsConnected && !TextUtils.isEmpty(this.mCountryCodeFromConnectionInfo)) {
            countryCode = this.mCountryCodeFromConnectionInfo;
            codeType = 2;
        } else if (!TextUtils.isEmpty(this.mCountryCodeFromScanResults)) {
            countryCode = this.mCountryCodeFromScanResults;
            codeType = 1;
        } else if (!TextUtils.isEmpty(this.mCountryCodeFromRegion)) {
            countryCode = this.mCountryCodeFromRegion;
            codeType = 4;
        } else {
            countryCode = this.mCountryCodeFromMonitor;
            codeType = 3;
        }
        if (!isValidCountryCode(countryCode)) {
            countryCode = "";
        }
        if (isValidCountryCode(countryCode) && (wifiManager = this.mWifiMgr) != null && this.mHwWifiChrService != null && !countryCode.equals(wifiManager.getCountryCode())) {
            Bundle chrData = new Bundle();
            chrData.putString("cCodeNoMobile", countryCode);
            chrData.putInt("cCodeType", codeType);
            this.mHwWifiChrService.uploadDFTEvent(33, chrData);
        }
        return countryCode;
    }

    private boolean isValidCountryCode(String code) {
        if (TextUtils.isEmpty(code) || code.length() != 2) {
            return false;
        }
        return this.countryCodeHashSet.contains(code);
    }

    private String getCountryCodeByRegion() {
        String regionCurrent = SystemProperties.get("persist.sys.locale", (String) null);
        String regionDefault = SystemProperties.get("ro.product.locale.region", (String) null);
        if (TextUtils.isEmpty(regionCurrent) || regionCurrent.length() <= 2) {
            return "";
        }
        String countryCode = regionCurrent.substring(regionCurrent.length() - 2, regionCurrent.length()).toUpperCase(Locale.ENGLISH);
        if (countryCode.equals(regionDefault)) {
            return "";
        }
        return countryCode;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMobilePhoneNoService() {
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
            return true;
        }
        String registerOperator = TelephonyManager.from(this.mContext).getNetworkOperator(0);
        return TextUtils.isEmpty(registerOperator) || registerOperator.length() < 5 || WHITE_CARD_NETWORK_MCCMNC.equals(registerOperator) || SystemProperties.getBoolean(PROPERTY_GLOBAL_WIFI_ONLY, false);
    }

    private String parseCountryCodeElement(ScanResult.InformationElement[] ies) {
        if (ies == null) {
            return null;
        }
        for (ScanResult.InformationElement ie : ies) {
            if (ie.id == 7 && ie.bytes.length >= 2) {
                return new String(ie.bytes, 0, 2);
            }
        }
        return null;
    }

    private String getCountryCodeFromList(List<String> countryCodeLists) {
        String countryCode = "";
        if (countryCodeLists == null || countryCodeLists.isEmpty()) {
            return countryCode;
        }
        Map<String, Integer> codeRecord = new HashMap<>();
        for (String code : countryCodeLists) {
            if (codeRecord.containsKey(code)) {
                codeRecord.put(code, Integer.valueOf(codeRecord.get(code).intValue() + 1));
            } else {
                codeRecord.put(code, 1);
            }
        }
        Integer maxCount = 0;
        boolean isSameSize = false;
        for (Map.Entry<String, Integer> entry : codeRecord.entrySet()) {
            String code2 = entry.getKey();
            Integer count = entry.getValue();
            if (count.equals(maxCount)) {
                isSameSize = true;
            }
            if (count.intValue() > maxCount.intValue()) {
                countryCode = code2;
                maxCount = count;
                isSameSize = false;
            }
        }
        if (isSameSize) {
            return "";
        }
        return countryCode;
    }

    private String getCountryCodeFromScanResults(List<ScanResult> scanLists) {
        if (scanLists == null) {
            return "";
        }
        List<String> bssidLists = new ArrayList<>();
        for (ScanResult entry : scanLists) {
            String countryCode = parseCountryCodeElement(entry.informationElements);
            String curBssid = entry.BSSID;
            if (!TextUtils.isEmpty(countryCode) && curBssid != null) {
                bssidLists.add(curBssid);
                this.bssidAndCountryCodeMap.put(curBssid, countryCode);
            }
        }
        this.bssidListBuffer.add(bssidLists);
        if (this.bssidListBuffer.size() > 3) {
            for (String str : this.bssidListBuffer.get(0)) {
                if (!this.bssidListBuffer.get(1).contains(str) && !this.bssidListBuffer.get(2).contains(str)) {
                    this.bssidAndCountryCodeMap.remove(str);
                }
            }
            this.bssidListBuffer.remove(0);
        }
        List<String> countryCodeChoosePool = new ArrayList<>();
        for (Map.Entry<String, String> entry2 : this.bssidAndCountryCodeMap.entrySet()) {
            countryCodeChoosePool.add(entry2.getValue());
        }
        return getCountryCodeFromList(countryCodeChoosePool);
    }

    private String getCountryCodeFromConnectionInfo(List<ScanResult> scanLists, WifiInfo wifiInfo) {
        if (scanLists == null || wifiInfo == null) {
            return null;
        }
        String currentBssid = wifiInfo.getBSSID();
        if (TextUtils.isEmpty(currentBssid)) {
            return null;
        }
        for (ScanResult entry : scanLists) {
            if (currentBssid.equalsIgnoreCase(entry.BSSID)) {
                return parseCountryCodeElement(entry.informationElements);
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkStateChangeAction(Intent intent) {
        if ((this.mWifiCountryCodeConf & 4) == 0 || !isMobilePhoneNoService()) {
            this.mCountryCodeFromConnectionInfo = null;
            return;
        }
        NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (netInfo == null || !netInfo.isConnected()) {
            this.mIsConnected = false;
            return;
        }
        this.mIsConnected = true;
        String countryCode = getCountryCodeFromConnectionInfo(this.mWifiMgr.getScanResults(), this.mWifiMgr.getConnectionInfo());
        if (!TextUtils.isEmpty(countryCode) && !countryCode.equalsIgnoreCase(this.mCountryCodeFromConnectionInfo)) {
            this.mCountryCodeFromConnectionInfo = countryCode;
            Intent intentCountryCode = new Intent(ACTION_WIFI_COUNTRY_CODE);
            intentCountryCode.putExtra(EXTRA_FORCE_SET_WIFI_CCODE, true);
            this.mContext.sendBroadcast(intentCountryCode, HW_SYSTEM_PERMISSION);
        }
    }
}
