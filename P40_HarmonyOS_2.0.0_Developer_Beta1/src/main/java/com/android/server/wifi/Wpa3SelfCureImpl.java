package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Wpa3SelfCureImpl {
    private static final String ASSOC_ERROR = "ASSOC_ERROR";
    private static final String ASSOC_TIME_OUT = "ASSOC_TIME_OUT";
    private static final String AUTH_ERROR = "AUTH_ERROR";
    private static final int BLACKLIST_MAX_BSS_NUM = 20;
    private static final int BLACKLIST_MAX_ESS_NUM = 10;
    private static final int CONNECT_FAILURE_NEVER_SELF_CURE = -1;
    private static final int CONNECT_FAILURE_THRESHOLD_TRIPLE = 3;
    private static final int CONNECT_FAILURE_THRESHOLD_TWICE = 2;
    private static final int CONNECT_FAIL_EVENT_MAX_NUM = 200;
    private static final int DEGENERATE_CONNECT_TIME_OUT = 5000;
    private static final String DEGENERATE_WPA2_COUNT = "DegenerateWpa2Cnt";
    private static final String DEGENERATE_WPA2_SUCC_COUNT = "DegenerateWpa2SuccCnt";
    private static final String DEGENERATE_WPA3_COUNT = "DegenerateWpa3Cnt";
    private static final String DEGENERATE_WPA3_SUCC_COUNT = "DegenerateWpa3SuccCnt";
    private static final int INVALID_RSSI_VALUE = Integer.MIN_VALUE;
    private static final String KEY_MGMT_OWE_TRANSITION = "KEY_MGMT_OWE_TRANSITION";
    private static final String KEY_MGMT_SAE_TRANSITION = "KEY_MGMT_SAE_TRANSITION";
    private static final String KEY_MGMT_WPA_PSK = "WPA_PSK";
    private static final int MSG_DEGENERATE_CONNECT = 1;
    private static final int SELF_CURE_DEFAULT_COUNT = 1;
    private static final int SELF_CURE_RSSI_THRESHOLD = -70;
    private static final String TAG = "Wpa3SelfCureImpl";
    private static final int WLAN_STATUS_AKMP_NOT_VALID = 43;
    private static final int WLAN_STATUS_ASSOC_DENIED_UNSPEC = 12;
    private static final int WLAN_STATUS_ASSOC_RSP_TIMEOUT = 5203;
    private static final int WLAN_STATUS_AUTH_RSP2_TIMEOUT = 5201;
    private static final int WLAN_STATUS_AUTH_RSP4_TIMEOUT = 5202;
    private static final int WLAN_STATUS_AUTH_TIMEOUT = 16;
    private static final int WLAN_STATUS_CAPS_UNSUPPORTED = 10;
    private static final int WLAN_STATUS_CIPHER_REJECTED_PER_POLICY = 46;
    private static final int WLAN_STATUS_GROUP_CIPHER_NOT_VALID = 41;
    private static final int WLAN_STATUS_INVALID_IE = 40;
    private static final int WLAN_STATUS_INVALID_PARAMETERS = 38;
    private static final int WLAN_STATUS_INVALID_RSNIE = 72;
    private static final int WLAN_STATUS_INVALID_RSN_IE_CAPAB = 45;
    private static final int WLAN_STATUS_NOT_SUPPORTED_AUTH_ALG = 13;
    private static final int WLAN_STATUS_PAIRWISE_CIPHER_NOT_VALID = 42;
    private static final int WLAN_STATUS_UNKNOWN_PASSWORD_IDENTIFIER = 123;
    private static final int WLAN_STATUS_UNSUPPORTED_RSN_IE_VERSION = 44;
    private static final String WPA3_AP_SSID = "Wpa3ApSsid";
    private static final int WPA3_FAIL = -1;
    private static final int WPA3_SUCCESS = 0;
    private static boolean mIsWpa3SelfCureTrigger = false;
    private static Wpa3SelfCureImpl mWpa3SelfCureImpl = null;
    private String mAuthType;
    private String mBssid;
    private LinkedHashMap<String, Integer> mConnectFailEventStats = new LinkedHashMap<String, Integer>() {
        /* class com.android.server.wifi.Wpa3SelfCureImpl.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.util.LinkedHashMap
        public boolean removeEldestEntry(Map.Entry<String, Integer> entry) {
            return size() > 200;
        }
    };
    private Context mContext;
    private String mFailReason;
    private HashMap<String, Integer> mFailReasonThresholdTable = new HashMap<>();
    private String mFailStage;
    private Handler mHandler = new Handler() {
        /* class com.android.server.wifi.Wpa3SelfCureImpl.AnonymousClass2 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                HwHiLog.d(Wpa3SelfCureImpl.TAG, false, "no need to handle other messages", new Object[0]);
                return;
            }
            HwHiLog.d(Wpa3SelfCureImpl.TAG, false, "MSG_DEGENERATE_CONNECT", new Object[0]);
            boolean unused = Wpa3SelfCureImpl.mIsWpa3SelfCureTrigger = false;
        }
    };
    private HwWifiCHRService mHwWifiCHRService;
    private boolean mIsSelfCureOngoing = false;
    private String mSsid;
    private int mStatusCode = 0;
    private WifiManager mWifiManager = null;
    private ArrayList<Wpa3BlackListInfo> mWpa3BlackList = new ArrayList<>();

    private Wpa3SelfCureImpl(Context context) {
        this.mContext = context;
        initialFailReasonMap();
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
    }

    public static synchronized Wpa3SelfCureImpl createSelfCureImpl(Context context) {
        Wpa3SelfCureImpl wpa3SelfCureImpl;
        synchronized (Wpa3SelfCureImpl.class) {
            if (mWpa3SelfCureImpl == null) {
                mWpa3SelfCureImpl = new Wpa3SelfCureImpl(context);
            }
            wpa3SelfCureImpl = mWpa3SelfCureImpl;
        }
        return wpa3SelfCureImpl;
    }

    private void initialFailReasonMap() {
        this.mFailReasonThresholdTable.put("ASSOC_BBS_NOT_FOUND", -1);
        this.mFailReasonThresholdTable.put("ASSOC_ALL_ECC_USED", -1);
        this.mFailReasonThresholdTable.put(ASSOC_ERROR, 2);
        this.mFailReasonThresholdTable.put(ASSOC_TIME_OUT, 3);
        this.mFailReasonThresholdTable.put("ASSOC_AP_NO_PMF", -1);
        this.mFailReasonThresholdTable.put("ASSOC_RESPONSE_PARSE_FAIL", 2);
        this.mFailReasonThresholdTable.put("ASSOC_NO_DH", 2);
        this.mFailReasonThresholdTable.put("ASSOC_GROUP_MISMATCH", 2);
        this.mFailReasonThresholdTable.put("ASSOC_NO_ECDH_STATE", 2);
        this.mFailReasonThresholdTable.put("ASSOC_UNKNOWN_GROUP", 2);
        this.mFailReasonThresholdTable.put("ASSOC_INVALID_DH_KEY", 2);
        this.mFailReasonThresholdTable.put("ASSOC_GET_KEY_ECDH_FAIL", 2);
        this.mFailReasonThresholdTable.put("ASSOC_SHA_VECTOR_FAIL", 2);
        this.mFailReasonThresholdTable.put("ASSOC_HMAC_SHA_FAIL", 2);
        this.mFailReasonThresholdTable.put("ASSOC_HMAC_SHA_HKDF_FAIL", 2);
        this.mFailReasonThresholdTable.put("AUTH_TOKEN_TOO_SHORT", -1);
        this.mFailReasonThresholdTable.put("AUTH_TOKEN_INVALID", -1);
        this.mFailReasonThresholdTable.put("AUTH_SAE_SILENTLY_DISCARD", -1);
        this.mFailReasonThresholdTable.put(AUTH_ERROR, 2);
        this.mFailReasonThresholdTable.put("AUTH_SAE_PROCESS_ERROR", 2);
        this.mFailReasonThresholdTable.put("AUTH_FCC_TOO_SHORT", -1);
        this.mFailReasonThresholdTable.put("AUTH_SAE_GROUP_NOT_ALLOWED", -1);
        this.mFailReasonThresholdTable.put("AUTH_COMMIT_SCALAR_ERROR", -1);
        this.mFailReasonThresholdTable.put("AUTH_COMMIT_ELEMENT_ERROR", -1);
        this.mFailReasonThresholdTable.put("AUTH_COMMIT_PWD_ERROR", -1);
        this.mFailReasonThresholdTable.put("AUTH_SAE_SILENTLY_DISCARD", -1);
        this.mFailReasonThresholdTable.put("AUTH_UNKNOWN_PASSWORD_IDENTIFIER", 3);
        this.mFailReasonThresholdTable.put("AUTH_ALL_ECC_USED", 3);
        this.mFailReasonThresholdTable.put("AUTH_SAE_TOO_SHORT_CONFIRM", 3);
        this.mFailReasonThresholdTable.put("AUTH_SAE_NO_TEMP_DATA", 3);
        this.mFailReasonThresholdTable.put("AUTH_SAE_CONFIRM_MISMATCH", 3);
        this.mFailReasonThresholdTable.put("EAPOL_3_4_FAIL", -1);
        this.mFailReasonThresholdTable.put("EAPOL_IGTK_CONFIG_FAIL", 2);
    }

    private WifiConfiguration getConfig(WifiConfigManager wifiConfigManager) {
        if (!this.mAuthType.contains("SAE")) {
            return null;
        }
        return wifiConfigManager.getConfiguredNetwork("\"" + this.mSsid + "\"SAE");
    }

    private void updateEventInfo(String ssid, String bssid, String authType, String failStage, String failReason) {
        this.mSsid = ssid;
        this.mBssid = bssid;
        this.mAuthType = authType;
        this.mFailStage = failStage;
        this.mFailReason = failReason;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v1, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v2 */
    /* JADX WARN: Type inference failed for: r1v20 */
    private int parseSelfCureInfo(String eventInfo) {
        int i;
        String failReason;
        int macIndex = eventInfo.lastIndexOf(";M:");
        int authTypeIndex = eventInfo.lastIndexOf(";AT:");
        int failStageIndex = eventInfo.lastIndexOf(";FS:");
        int failReasonIndex = eventInfo.lastIndexOf(";FR:");
        if (macIndex == -1 || authTypeIndex == -1 || failStageIndex == -1) {
            i = 0;
        } else if (failReasonIndex == -1) {
            i = 0;
        } else {
            String ssid = eventInfo.substring("S:".length(), macIndex);
            String bssid = eventInfo.substring(";M:".length() + macIndex, authTypeIndex);
            String authType = eventInfo.substring(";AT:".length() + authTypeIndex, failStageIndex);
            String failStage = eventInfo.substring(";FS:".length() + failStageIndex, failReasonIndex);
            String failReason2 = eventInfo.substring(";FR:".length() + failReasonIndex, eventInfo.length() - 1);
            int braketsIndex = failReason2.indexOf("(");
            if (braketsIndex != -1) {
                try {
                    this.mStatusCode = Integer.parseInt(failReason2.substring(braketsIndex + 1, failReason2.length() - 1));
                    failReason2 = failReason2.substring(0, braketsIndex);
                } catch (NumberFormatException e) {
                    HwHiLog.e(TAG, false, "parseSelfCureInfo fail", new Object[0]);
                    failReason = failReason2;
                }
            } else {
                this.mStatusCode = 0;
            }
            failReason = failReason2;
            HwHiLog.d(TAG, false, "SelfCureInfo: ssid=%{public}s, bssid=%{private}s, type=%{public}s, stage=%{public}s, reason=%{public}s, statusCode=%{public}d", new Object[]{StringUtilEx.safeDisplaySsid(ssid), bssid, authType, failStage, failReason, Integer.valueOf(this.mStatusCode)});
            updateEventInfo(ssid, bssid, authType, failStage, failReason);
            if (KEY_MGMT_SAE_TRANSITION.equals(authType)) {
                uploadSelfCureStatistics(DEGENERATE_WPA3_COUNT);
                return 0;
            } else if (KEY_MGMT_OWE_TRANSITION.equals(authType)) {
                uploadSelfCureStatistics(DEGENERATE_WPA3_SUCC_COUNT);
                return 0;
            } else {
                HwHiLog.d(TAG, false, "no need to upload info to hiview", new Object[0]);
                return 0;
            }
        }
        HwHiLog.e(TAG, i, "event report with error format", new Object[i]);
        return -1;
    }

    private int getValue(HashMap<String, Integer> myMap, String key) {
        Integer value = myMap.get(key);
        if (value == null) {
            return 0;
        }
        return value.intValue();
    }

    private int getRssi(WifiConfigManager wifiConfigManager, WifiConfiguration config) {
        ScanDetailCache scanDetailCache = wifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
        if (scanDetailCache != null) {
            ScanDetail scanDetail = scanDetailCache.getScanDetail(this.mBssid);
            if (scanDetail != null) {
                return scanDetail.getScanResult().level;
            }
            HwHiLog.e(TAG, false, "fail to get scan detail", new Object[0]);
            return INVALID_RSSI_VALUE;
        }
        HwHiLog.e(TAG, false, "fail to get scan detail cache", new Object[0]);
        return INVALID_RSSI_VALUE;
    }

    private boolean isStatusCodeAllowed(int statusCode) {
        if (statusCode == 10 || statusCode == 16 || statusCode == 38 || statusCode == WLAN_STATUS_INVALID_RSNIE || statusCode == 123 || statusCode == 12 || statusCode == 13) {
            return true;
        }
        switch (statusCode) {
            case WLAN_STATUS_INVALID_IE /* 40 */:
            case 41:
            case WLAN_STATUS_PAIRWISE_CIPHER_NOT_VALID /* 42 */:
            case WLAN_STATUS_AKMP_NOT_VALID /* 43 */:
            case WLAN_STATUS_UNSUPPORTED_RSN_IE_VERSION /* 44 */:
            case WLAN_STATUS_INVALID_RSN_IE_CAPAB /* 45 */:
            case WLAN_STATUS_CIPHER_REJECTED_PER_POLICY /* 46 */:
                return true;
            default:
                switch (statusCode) {
                    case WLAN_STATUS_AUTH_RSP2_TIMEOUT /* 5201 */:
                    case WLAN_STATUS_AUTH_RSP4_TIMEOUT /* 5202 */:
                    case WLAN_STATUS_ASSOC_RSP_TIMEOUT /* 5203 */:
                        return true;
                    default:
                        return false;
                }
        }
    }

    private boolean isNeedSelfCure(WifiConfigManager wifiConfigManager, String eventInfo, WifiConfiguration config) {
        if (!KEY_MGMT_SAE_TRANSITION.equals(this.mAuthType)) {
            HwHiLog.d(TAG, false, "don't self cure, authType=%{public}s", new Object[]{this.mAuthType});
            return false;
        } else if (config.getNetworkSelectionStatus().getHasEverConnected()) {
            HwHiLog.d(TAG, false, "don't self cure, has ever successfully connect", new Object[0]);
            return false;
        } else if (!ASSOC_TIME_OUT.equals(this.mFailReason) || getRssi(wifiConfigManager, config) > SELF_CURE_RSSI_THRESHOLD) {
            int maxFailNum = getValue(this.mFailReasonThresholdTable, this.mFailReason);
            int failCounter = getValue(this.mConnectFailEventStats, eventInfo) + 1;
            this.mConnectFailEventStats.put(eventInfo, Integer.valueOf(failCounter));
            if (maxFailNum <= 0 || failCounter < maxFailNum) {
                HwHiLog.d(TAG, false, "don't self cure, reason=%{public}s, statusCode=%{public}d, counter=%{public}d", new Object[]{this.mFailReason, Integer.valueOf(this.mStatusCode), Integer.valueOf(failCounter)});
                return false;
            } else if ((ASSOC_ERROR.equals(this.mFailReason) || AUTH_ERROR.equals(this.mFailReason)) && !isStatusCodeAllowed(this.mStatusCode)) {
                HwHiLog.d(TAG, false, "don't self cure, reason=%{public}s, statusCode=%{public}d", new Object[]{this.mFailReason, Integer.valueOf(this.mStatusCode)});
                return false;
            } else {
                HwHiLog.d(TAG, false, "trigger self cure, type=%{public}s, reason=%{public}s, statusCode=%{public}d, counter=%{public}d", new Object[]{this.mAuthType, this.mFailReason, Integer.valueOf(this.mStatusCode), Integer.valueOf(failCounter)});
                return true;
            }
        } else {
            HwHiLog.d(TAG, false, "don't self cure, rssi less than -70", new Object[0]);
            return false;
        }
    }

    private String getSuggestAuthType(String authType) {
        if (authType == null) {
            HwHiLog.e(TAG, false, "authType is null", new Object[0]);
            return "";
        } else if (KEY_MGMT_SAE_TRANSITION.equals(authType)) {
            return KEY_MGMT_WPA_PSK;
        } else {
            HwHiLog.e(TAG, false, "fail to get suggest auth type.", new Object[0]);
            return "";
        }
    }

    private void addToBlackList() {
        long oldestTimeInAll = SystemClock.elapsedRealtime();
        long oldestTimeInEss = SystemClock.elapsedRealtime();
        Iterator<Wpa3BlackListInfo> it = this.mWpa3BlackList.iterator();
        int oldestIndexInAll = 0;
        int oldestIndexInEss = 0;
        long oldestTimeInAll2 = oldestTimeInAll;
        long oldestTimeInEss2 = oldestTimeInEss;
        int index = 0;
        int currentEssNum = 0;
        while (it.hasNext()) {
            Wpa3BlackListInfo info = it.next();
            if (this.mSsid.equals(info.ssid) && this.mAuthType.equals(info.origAuthType)) {
                currentEssNum++;
                if (this.mBssid.equals(info.bssid)) {
                    info.lastTimeStamp = SystemClock.elapsedRealtime();
                    return;
                } else if (info.lastTimeStamp < oldestTimeInEss2) {
                    oldestTimeInEss2 = info.lastTimeStamp;
                    oldestIndexInEss = index;
                }
            }
            if (info.lastTimeStamp < oldestTimeInAll2) {
                oldestTimeInAll2 = info.lastTimeStamp;
                oldestIndexInAll = index;
            }
            index++;
        }
        if (currentEssNum >= 10) {
            this.mWpa3BlackList.remove(oldestIndexInEss);
        } else if (this.mWpa3BlackList.size() >= 20) {
            this.mWpa3BlackList.remove(oldestIndexInAll);
        }
        ArrayList<Wpa3BlackListInfo> arrayList = this.mWpa3BlackList;
        String str = this.mSsid;
        String str2 = this.mBssid;
        String str3 = this.mAuthType;
        arrayList.add(new Wpa3BlackListInfo(str, str2, str3, getSuggestAuthType(str3)));
    }

    private void updateConfigBySuggestAuthType(WifiConfiguration config, String suggestAuthType) {
        if (KEY_MGMT_WPA_PSK.equals(suggestAuthType)) {
            config.allowedKeyManagement.clear();
            config.allowedKeyManagement.set(1);
            config.requirePMF = false;
            config.setCombinationType(false);
            return;
        }
        HwHiLog.e(TAG, false, "suggest auth type is incorrect", new Object[0]);
    }

    private boolean hasDeleteWpa3Config(WifiConfigManager wifiConfigManager, WifiConfiguration config) {
        WifiConfiguration networkConfig = wifiConfigManager.getConfiguredNetworkWithoutMasking(config.networkId);
        if (networkConfig == null) {
            HwHiLog.e(TAG, false, "fail to get network without mask", new Object[0]);
            return false;
        }
        config.preSharedKey = networkConfig.preSharedKey;
        wifiConfigManager.removeNetwork(config.networkId, 1000);
        return true;
    }

    private void connectBySuggestAuthType(WifiConfigManager wifiConfigManager, WifiConfiguration config, String authType) {
        HwHiLog.d(TAG, false, "connectBySuggestAuthType enter", new Object[0]);
        updateConfigBySuggestAuthType(config, authType);
        if (hasDeleteWpa3Config(wifiConfigManager, config)) {
            mIsWpa3SelfCureTrigger = true;
            uploadSelfCureStatistics(DEGENERATE_WPA2_COUNT);
            this.mHandler.sendEmptyMessageDelayed(1, 5000);
            if (this.mWifiManager == null) {
                this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            }
            this.mWifiManager.connect(config, null);
        }
    }

    private void uploadSelfCureStatistics(String selfCureInfo) {
        if (this.mHwWifiCHRService == null) {
            HwHiLog.e(TAG, false, "mHwWifiCHRService is null", new Object[0]);
            return;
        }
        int degenerateWpa2Count = 0;
        int degenerateWpa2SuccCount = 0;
        if (DEGENERATE_WPA2_COUNT.equals(selfCureInfo)) {
            degenerateWpa2Count = 1;
        } else if (DEGENERATE_WPA2_SUCC_COUNT.equals(selfCureInfo)) {
            degenerateWpa2SuccCount = 1;
        } else if (!DEGENERATE_WPA3_COUNT.equals(selfCureInfo)) {
            if (!DEGENERATE_WPA3_SUCC_COUNT.equals(selfCureInfo)) {
                HwHiLog.w(TAG, false, "uploadSelfCureStatistics unknown selfCureInfo", new Object[0]);
            }
        }
        Bundle data = new Bundle();
        data.putString(WPA3_AP_SSID, this.mSsid);
        data.putInt(DEGENERATE_WPA2_COUNT, degenerateWpa2Count);
        data.putInt(DEGENERATE_WPA2_SUCC_COUNT, degenerateWpa2SuccCount);
        data.putInt(DEGENERATE_WPA3_COUNT, degenerateWpa2Count);
        data.putInt(DEGENERATE_WPA3_SUCC_COUNT, degenerateWpa2SuccCount);
        this.mHwWifiCHRService.uploadDFTEvent(21, data);
    }

    public void handleSelfCureConnectSucc() {
        if (mIsWpa3SelfCureTrigger) {
            mIsWpa3SelfCureTrigger = false;
            HwHiLog.d(TAG, false, "call uploadDegenerateWpa2SuccCount", new Object[0]);
            uploadSelfCureStatistics(DEGENERATE_WPA2_SUCC_COUNT);
        }
    }

    public void handleConnectFailReport(WifiConfigManager wifiConfigManager, String eventInfo) {
        if (wifiConfigManager == null || eventInfo == null) {
            HwHiLog.e(TAG, false, "handleConnectFailReport param is null", new Object[0]);
        } else if (this.mIsSelfCureOngoing) {
            HwHiLog.d(TAG, false, "selfcure is ongoing.", new Object[0]);
        } else {
            this.mIsSelfCureOngoing = true;
            HwHiLog.d(TAG, false, "start to handle selfcure event", new Object[0]);
            if (parseSelfCureInfo(eventInfo) == -1) {
                this.mIsSelfCureOngoing = false;
                return;
            }
            WifiConfiguration config = getConfig(wifiConfigManager);
            if (config == null) {
                this.mIsSelfCureOngoing = false;
                HwHiLog.e(TAG, false, "fail to get config", new Object[0]);
                return;
            }
            if (isNeedSelfCure(wifiConfigManager, eventInfo, config)) {
                addToBlackList();
                connectBySuggestAuthType(wifiConfigManager, config, getSuggestAuthType(this.mAuthType));
            }
            this.mIsSelfCureOngoing = false;
        }
    }

    public void removeBlackList(String configKey) {
        if (configKey == null) {
            HwHiLog.e(TAG, false, "configKey is null", new Object[0]);
            return;
        }
        int lastQuotationIndex = configKey.lastIndexOf("\"");
        if (lastQuotationIndex != -1) {
            String ssid = configKey.substring(1, lastQuotationIndex);
            String authType = configKey.substring(lastQuotationIndex + 1);
            for (int index = this.mWpa3BlackList.size() - 1; index >= 0; index--) {
                Wpa3BlackListInfo info = this.mWpa3BlackList.get(index);
                if (ssid.equals(info.ssid) && authType.equals(info.suggestAuthType)) {
                    this.mWpa3BlackList.remove(info);
                }
            }
            return;
        }
        HwHiLog.e(TAG, false, "format error, configKey=%{private}s", new Object[]{configKey});
    }

    /* access modifiers changed from: private */
    public class Wpa3BlackListInfo {
        private String bssid;
        private long lastTimeStamp;
        private String origAuthType;
        private String ssid;
        private String suggestAuthType;

        private Wpa3BlackListInfo(String ssid2, String bssid2, String origAuthType2, String suggestAuthType2) {
            this.ssid = ssid2;
            this.bssid = bssid2;
            this.origAuthType = origAuthType2;
            this.suggestAuthType = suggestAuthType2;
            this.lastTimeStamp = SystemClock.elapsedRealtime();
        }
    }
}
