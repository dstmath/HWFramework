package com.android.server.wifi;

import android.content.Context;
import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetworkCallback;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.HidlSupport;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.MutableBoolean;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.server.wifi.WifiBackupRestore;
import com.android.server.wifi.scanner.ScanResultRecords;
import com.android.server.wifi.util.NativeUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.concurrent.ThreadSafe;
import org.json.JSONException;
import org.json.JSONObject;
import vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaNetworkCallback;

@ThreadSafe
public class SupplicantStaNetworkHal {
    private static final Pattern GSM_AUTH_RESPONSE_PARAMS_PATTERN = Pattern.compile(":([0-9a-fA-F]+):([0-9a-fA-F]+)");
    @VisibleForTesting
    public static final String ID_STRING_KEY_CONFIG_KEY = "configKey";
    @VisibleForTesting
    public static final String ID_STRING_KEY_CREATOR_UID = "creatorUid";
    @VisibleForTesting
    public static final String ID_STRING_KEY_FQDN = "fqdn";
    private static final String TAG = "SupplicantStaNetworkHal";
    private static final Pattern UMTS_AUTH_RESPONSE_PARAMS_PATTERN = Pattern.compile("^:([0-9a-fA-F]+):([0-9a-fA-F]+):([0-9a-fA-F]+)$");
    private static final Pattern UMTS_AUTS_RESPONSE_PARAMS_PATTERN = Pattern.compile("^:([0-9a-fA-F]+)$");
    private int mAuthAlgMask;
    private byte[] mBssid;
    private String mEapAltSubjectMatch;
    private ArrayList<Byte> mEapAnonymousIdentity;
    private String mEapCACert;
    private String mEapCAPath;
    private String mEapClientCert;
    private String mEapDomainSuffixMatch;
    private boolean mEapEngine;
    private String mEapEngineID;
    private ArrayList<Byte> mEapIdentity;
    private int mEapMethod;
    private ArrayList<Byte> mEapPassword;
    private int mEapPhase2Method;
    private String mEapPrivateKeyId;
    private String mEapSubjectMatch;
    private int mGroupCipherMask;
    private ISupplicantStaNetwork mISupplicantStaNetwork;
    private ISupplicantStaNetworkCallback mISupplicantStaNetworkCallback;
    private String mIdStr;
    /* access modifiers changed from: private */
    public final String mIfaceName;
    private int mKeyMgmtMask;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private int mNetworkId;
    private int mPairwiseCipherMask;
    private int mProtoMask;
    private byte[] mPsk;
    private String mPskPassphrase;
    private boolean mRequirePmf;
    private boolean mScanSsid;
    private ArrayList<Byte> mSsid;
    private boolean mSystemSupportsFastBssTransition = false;
    private boolean mVerboseLoggingEnabled = false;
    private ArrayList<Byte> mWepKey;
    private int mWepTxKeyIdx;
    /* access modifiers changed from: private */
    public final WifiMonitor mWifiMonitor;

    private class SupplicantStaNetworkHalCallback extends ISupplicantStaNetworkCallback.Stub {
        private final int mFramewokNetworkId;
        private final String mSsid;

        SupplicantStaNetworkHalCallback(int framewokNetworkId, String ssid) {
            this.mFramewokNetworkId = framewokNetworkId;
            this.mSsid = ssid;
        }

        public void onNetworkEapSimGsmAuthRequest(ISupplicantStaNetworkCallback.NetworkRequestEapSimGsmAuthParams params) {
            synchronized (SupplicantStaNetworkHal.this.mLock) {
                SupplicantStaNetworkHal.this.logCallback("onNetworkEapSimGsmAuthRequest");
                String[] data = new String[params.rands.size()];
                int i = 0;
                Iterator<byte[]> it = params.rands.iterator();
                while (it.hasNext()) {
                    data[i] = NativeUtil.hexStringFromByteArray(it.next());
                    i++;
                }
                SupplicantStaNetworkHal.this.mWifiMonitor.broadcastNetworkGsmAuthRequestEvent(SupplicantStaNetworkHal.this.mIfaceName, this.mFramewokNetworkId, this.mSsid, data);
            }
        }

        public void onNetworkEapSimUmtsAuthRequest(ISupplicantStaNetworkCallback.NetworkRequestEapSimUmtsAuthParams params) {
            synchronized (SupplicantStaNetworkHal.this.mLock) {
                SupplicantStaNetworkHal.this.logCallback("onNetworkEapSimUmtsAuthRequest");
                SupplicantStaNetworkHal.this.mWifiMonitor.broadcastNetworkUmtsAuthRequestEvent(SupplicantStaNetworkHal.this.mIfaceName, this.mFramewokNetworkId, this.mSsid, new String[]{NativeUtil.hexStringFromByteArray(params.rand), NativeUtil.hexStringFromByteArray(params.autn)});
            }
        }

        public void onNetworkEapIdentityRequest() {
            synchronized (SupplicantStaNetworkHal.this.mLock) {
                SupplicantStaNetworkHal.this.logCallback("onNetworkEapIdentityRequest");
                SupplicantStaNetworkHal.this.mWifiMonitor.broadcastNetworkIdentityRequestEvent(SupplicantStaNetworkHal.this.mIfaceName, this.mFramewokNetworkId, this.mSsid);
            }
        }
    }

    private class VendorSupplicantStaNetworkHalCallback extends ISupplicantStaNetworkCallback.Stub {
        private SupplicantStaNetworkHalCallback callback;
        private final int mFramewokNetworkId;
        private final String mSsid;

        VendorSupplicantStaNetworkHalCallback(int framewokNetworkId, String ssid) {
            this.mFramewokNetworkId = framewokNetworkId;
            this.mSsid = ssid;
            this.callback = new SupplicantStaNetworkHalCallback(framewokNetworkId, ssid);
        }

        public void onNetworkEapSimGsmAuthRequest(ISupplicantStaNetworkCallback.NetworkRequestEapSimGsmAuthParams params) {
            this.callback.onNetworkEapSimGsmAuthRequest(params);
        }

        public void onNetworkEapSimUmtsAuthRequest(ISupplicantStaNetworkCallback.NetworkRequestEapSimUmtsAuthParams params) {
            this.callback.onNetworkEapSimUmtsAuthRequest(params);
        }

        public void onNetworkEapIdentityRequest() {
            this.callback.onNetworkEapIdentityRequest();
        }

        public void onNetworkEapNotificationErrorCode(int errorcode) {
            synchronized (SupplicantStaNetworkHal.this.mLock) {
                Log.e(SupplicantStaNetworkHal.TAG, "onNetworkEapNotificationErrorCode errorcode = " + errorcode);
                SupplicantStaNetworkHal.this.mWifiMonitor.broadcastNetworkEAPErrorcodeReportEvent(SupplicantStaNetworkHal.this.mIfaceName, this.mFramewokNetworkId, this.mSsid, errorcode);
            }
        }
    }

    SupplicantStaNetworkHal(ISupplicantStaNetwork iSupplicantStaNetwork, String ifaceName, Context context, WifiMonitor monitor) {
        this.mISupplicantStaNetwork = iSupplicantStaNetwork;
        this.mIfaceName = ifaceName;
        this.mWifiMonitor = monitor;
        this.mSystemSupportsFastBssTransition = context.getResources().getBoolean(17957078);
    }

    /* access modifiers changed from: package-private */
    public void enableVerboseLogging(boolean enable) {
        synchronized (this.mLock) {
            this.mVerboseLoggingEnabled = enable;
        }
    }

    public boolean loadWifiConfiguration(WifiConfiguration config, Map<String, String> networkExtras) {
        synchronized (this.mLock) {
            if (config == null) {
                try {
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                config.SSID = null;
                if (!getSsid() || ArrayUtils.isEmpty(this.mSsid)) {
                    Log.e(TAG, "failed to read ssid");
                    return false;
                }
                config.SSID = NativeUtil.encodeSsid(this.mSsid);
                config.oriSsid = NativeUtil.hexStringFromByteArray(NativeUtil.byteArrayFromArrayList(this.mSsid));
                config.networkId = -1;
                if (getId()) {
                    config.networkId = this.mNetworkId;
                    config.getNetworkSelectionStatus().setNetworkSelectionBSSID(null);
                    if (getBssid() && !ArrayUtils.isEmpty(this.mBssid)) {
                        config.getNetworkSelectionStatus().setNetworkSelectionBSSID(NativeUtil.macAddressFromByteArray(this.mBssid));
                    }
                    config.hiddenSSID = false;
                    if (getScanSsid()) {
                        config.hiddenSSID = this.mScanSsid;
                    }
                    config.requirePMF = false;
                    if (getRequirePmf()) {
                        config.requirePMF = this.mRequirePmf;
                    }
                    config.wepTxKeyIndex = -1;
                    if (getWepTxKeyIdx()) {
                        config.wepTxKeyIndex = this.mWepTxKeyIdx;
                    }
                    for (int i = 0; i < 4; i++) {
                        config.wepKeys[i] = null;
                        if (getWepKey(i) && !ArrayUtils.isEmpty(this.mWepKey)) {
                            config.wepKeys[i] = NativeUtil.bytesToHexOrQuotedString(this.mWepKey);
                        }
                    }
                    config.preSharedKey = null;
                    if (getPskPassphrase() && !TextUtils.isEmpty(this.mPskPassphrase)) {
                        config.preSharedKey = NativeUtil.addEnclosingQuotes(this.mPskPassphrase);
                    } else if (getPsk() && !ArrayUtils.isEmpty(this.mPsk)) {
                        config.preSharedKey = NativeUtil.hexStringFromByteArray(this.mPsk);
                    }
                    if (getKeyMgmt()) {
                        config.allowedKeyManagement = removeFastTransitionFlags(supplicantToWifiConfigurationKeyMgmtMask(this.mKeyMgmtMask));
                    }
                    if (getProto()) {
                        config.allowedProtocols = supplicantToWifiConfigurationProtoMask(this.mProtoMask);
                    }
                    if (getAuthAlg()) {
                        config.allowedAuthAlgorithms = supplicantToWifiConfigurationAuthAlgMask(this.mAuthAlgMask);
                    }
                    if (getGroupCipher()) {
                        config.allowedGroupCiphers = supplicantToWifiConfigurationGroupCipherMask(this.mGroupCipherMask);
                    }
                    if (getPairwiseCipher()) {
                        config.allowedPairwiseCiphers = supplicantToWifiConfigurationPairwiseCipherMask(this.mPairwiseCipherMask);
                    }
                    if (!getIdStr() || TextUtils.isEmpty(this.mIdStr)) {
                        Log.w(TAG, "getIdStr failed or empty");
                    } else {
                        networkExtras.putAll(parseNetworkExtra(this.mIdStr));
                    }
                    boolean loadWifiEnterpriseConfig = loadWifiEnterpriseConfig(config.SSID, config.enterpriseConfig);
                    return loadWifiEnterpriseConfig;
                }
                Log.e(TAG, "getId failed");
                return false;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:172:0x0336, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0069, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        android.util.Log.e(TAG, "saveWifiConfiguration: cannot be utf-8 encoded", r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0072, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0008, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:4:0x0006, B:13:0x004d] */
    public boolean saveWifiConfiguration(WifiConfiguration config) {
        synchronized (this.mLock) {
            if (config == null) {
                return false;
            }
            if (config.SSID != null) {
                ArrayList<Byte> ssid = ScanResultRecords.getDefault().getOriSsid(config.getNetworkSelectionStatus().getNetworkSelectionBSSID(), config.SSID);
                Log.d(TAG, "ssid=" + config.SSID + " oriSsid=" + config.oriSsid + " oriSsidRecord=" + ssid);
                if (ssid == null) {
                    if (!TextUtils.isEmpty(config.oriSsid)) {
                        ssid = NativeUtil.byteArrayToArrayList(NativeUtil.hexStringToByteArray(config.oriSsid));
                    } else {
                        ssid = NativeUtil.decodeSsid(config.SSID);
                    }
                }
                if (!setSsid(ssid)) {
                    Log.e(TAG, "failed to set SSID: " + config.SSID);
                    return false;
                }
            }
            String bssidStr = config.getNetworkSelectionStatus().getNetworkSelectionBSSID();
            if (bssidStr == null || setBssid(NativeUtil.macAddressToByteArray(bssidStr))) {
                if (config.preSharedKey != null) {
                    if (config.preSharedKey.isEmpty()) {
                        Log.e(TAG, "psk is empty");
                        return false;
                    } else if (config.preSharedKey.startsWith("\"")) {
                        if (!setPskPassphrase(NativeUtil.removeEnclosingQuotes(config.preSharedKey))) {
                            Log.e(TAG, "failed to set psk passphrase");
                            return false;
                        }
                    } else if (!setPsk(NativeUtil.hexStringToByteArray(config.preSharedKey))) {
                        Log.e(TAG, "failed to set psk");
                        return false;
                    }
                }
                boolean i = false;
                if (config.wepKeys != null) {
                    boolean hasSetKey = false;
                    for (int i2 = 0; i2 < config.wepKeys.length; i2++) {
                        if (config.wepKeys[i2] != null && config.allowedAuthAlgorithms.get(1)) {
                            if (TextUtils.isEmpty(config.wepKeys[i2])) {
                                Log.e(TAG, "index " + i2 + " key is empty");
                            } else if (!setWepKey(i2, NativeUtil.hexOrQuotedStringToBytes(config.wepKeys[i2]))) {
                                Log.e(TAG, "failed to set wep_key " + i2);
                                return false;
                            } else {
                                hasSetKey = true;
                            }
                        }
                    }
                    i = hasSetKey;
                }
                if (i && !setWepTxKeyIdx(config.wepTxKeyIndex)) {
                    Log.e(TAG, "failed to set wep_tx_keyidx: " + config.wepTxKeyIndex);
                    return false;
                } else if (!setScanSsid(config.hiddenSSID)) {
                    Log.e(TAG, config.SSID + ": failed to set hiddenSSID: " + config.hiddenSSID);
                    return false;
                } else if (!setRequirePmf(config.requirePMF)) {
                    Log.e(TAG, config.SSID + ": failed to set requirePMF: " + config.requirePMF);
                    return false;
                } else if (config.allowedKeyManagement.cardinality() != 0 && !setKeyMgmt(wifiConfigurationToSupplicantKeyMgmtMask(addFastTransitionFlags(config.allowedKeyManagement)))) {
                    Log.e(TAG, "failed to set Key Management");
                    return false;
                } else if (config.allowedProtocols.cardinality() != 0 && !setProto(wifiConfigurationToSupplicantProtoMask(config.allowedProtocols))) {
                    Log.e(TAG, "failed to set Security Protocol");
                    return false;
                } else if (config.allowedAuthAlgorithms.cardinality() != 0 && !setAuthAlg(wifiConfigurationToSupplicantAuthAlgMask(config.allowedAuthAlgorithms))) {
                    Log.e(TAG, "failed to set AuthAlgorithm");
                    return false;
                } else if (config.allowedGroupCiphers.cardinality() != 0 && !setGroupCipher(wifiConfigurationToSupplicantGroupCipherMask(config.allowedGroupCiphers))) {
                    Log.e(TAG, "failed to set Group Cipher");
                    return false;
                } else if (config.allowedPairwiseCiphers.cardinality() == 0 || setPairwiseCipher(wifiConfigurationToSupplicantPairwiseCipherMask(config.allowedPairwiseCiphers))) {
                    Map<String, String> metadata = new HashMap<>();
                    if (config.isPasspoint()) {
                        metadata.put("fqdn", config.FQDN);
                    }
                    metadata.put("configKey", config.configKey());
                    metadata.put("creatorUid", Integer.toString(config.creatorUid));
                    if (!setIdStr(createNetworkExtra(metadata))) {
                        Log.e(TAG, "failed to set id string");
                        return false;
                    } else if (config.updateIdentifier != null && !setUpdateIdentifier(Integer.parseInt(config.updateIdentifier))) {
                        Log.e(TAG, "failed to set update identifier");
                        return false;
                    } else if (config.enterpriseConfig != null && config.enterpriseConfig.getEapMethod() != -1 && !saveWifiEnterpriseConfig(config.SSID, config.enterpriseConfig)) {
                        return false;
                    } else {
                        if (!isVendorSupplicantStaNetworkV2_0()) {
                            this.mISupplicantStaNetworkCallback = new SupplicantStaNetworkHalCallback(config.networkId, config.SSID);
                            if (registerCallback(this.mISupplicantStaNetworkCallback)) {
                                return true;
                            }
                            Log.e(TAG, "Failed to register callback");
                            return false;
                        } else if (!setWapiConfiguration(config)) {
                            return false;
                        } else {
                            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaNetworkCallback vendorNetworkCallback = new VendorSupplicantStaNetworkHalCallback(config.networkId, config.SSID);
                            if (!hwStaNetworkRegisterCallback(vendorNetworkCallback)) {
                                Log.e(TAG, "Failed to register callback");
                                return false;
                            }
                            this.mISupplicantStaNetworkCallback = vendorNetworkCallback;
                            Log.d(TAG, "Successfully seved vendor network configuration.");
                            return true;
                        }
                    }
                } else {
                    Log.e(TAG, "failed to set PairwiseCipher");
                    return false;
                }
            } else {
                Log.e(TAG, "failed to set BSSID: " + bssidStr);
                return false;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:78:0x013a, code lost:
        return true;
     */
    private boolean loadWifiEnterpriseConfig(String ssid, WifiEnterpriseConfig eapConfig) {
        String str;
        synchronized (this.mLock) {
            if (eapConfig == null) {
                try {
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else if (getEapMethod()) {
                eapConfig.setEapMethod(supplicantToWifiConfigurationEapMethod(this.mEapMethod));
                if (getEapPhase2Method()) {
                    eapConfig.setPhase2Method(supplicantToWifiConfigurationEapPhase2Method(this.mEapPhase2Method));
                    if (getEapIdentity() && !ArrayUtils.isEmpty(this.mEapIdentity)) {
                        eapConfig.setFieldValue("identity", NativeUtil.stringFromByteArrayList(this.mEapIdentity));
                    }
                    if (getEapAnonymousIdentity() && !ArrayUtils.isEmpty(this.mEapAnonymousIdentity)) {
                        eapConfig.setFieldValue("anonymous_identity", NativeUtil.stringFromByteArrayList(this.mEapAnonymousIdentity));
                    }
                    if (getEapPassword() && !ArrayUtils.isEmpty(this.mEapPassword)) {
                        eapConfig.setFieldValue("password", NativeUtil.stringFromByteArrayList(this.mEapPassword));
                    }
                    if (getEapClientCert() && !TextUtils.isEmpty(this.mEapClientCert)) {
                        eapConfig.setFieldValue(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_CLIENT_CERT, this.mEapClientCert);
                    }
                    if (getEapCACert() && !TextUtils.isEmpty(this.mEapCACert)) {
                        eapConfig.setFieldValue(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_CA_CERT, this.mEapCACert);
                    }
                    if (getEapSubjectMatch() && !TextUtils.isEmpty(this.mEapSubjectMatch)) {
                        eapConfig.setFieldValue("subject_match", this.mEapSubjectMatch);
                    }
                    if (getEapEngineID() && !TextUtils.isEmpty(this.mEapEngineID)) {
                        eapConfig.setFieldValue("engine_id", this.mEapEngineID);
                    }
                    if (getEapEngine() && !TextUtils.isEmpty(this.mEapEngineID)) {
                        if (this.mEapEngine) {
                            str = "1";
                        } else {
                            str = "0";
                        }
                        eapConfig.setFieldValue("engine", str);
                    }
                    if (getEapPrivateKeyId() && !TextUtils.isEmpty(this.mEapPrivateKeyId)) {
                        eapConfig.setFieldValue("key_id", this.mEapPrivateKeyId);
                    }
                    if (getEapAltSubjectMatch() && !TextUtils.isEmpty(this.mEapAltSubjectMatch)) {
                        eapConfig.setFieldValue("altsubject_match", this.mEapAltSubjectMatch);
                    }
                    if (getEapDomainSuffixMatch() && !TextUtils.isEmpty(this.mEapDomainSuffixMatch)) {
                        eapConfig.setFieldValue("domain_suffix_match", this.mEapDomainSuffixMatch);
                    }
                    if (getEapCAPath() && !TextUtils.isEmpty(this.mEapCAPath)) {
                        eapConfig.setFieldValue(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_CA_PATH, this.mEapCAPath);
                    }
                } else {
                    Log.e(TAG, "failed to get eap phase2 method");
                    return false;
                }
            } else {
                Log.e(TAG, "failed to get eap method. Assumimg not an enterprise network");
                return true;
            }
        }
    }

    private boolean saveWifiEnterpriseConfig(String ssid, WifiEnterpriseConfig eapConfig) {
        synchronized (this.mLock) {
            if (eapConfig == null) {
                try {
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else if (!setEapMethod(wifiConfigurationToSupplicantEapMethod(eapConfig.getEapMethod()))) {
                Log.e(TAG, ssid + ": failed to set eap method: " + eapConfig.getEapMethod());
                return false;
            } else if (!setEapPhase2Method(wifiConfigurationToSupplicantEapPhase2Method(eapConfig.getPhase2Method()))) {
                Log.e(TAG, ssid + ": failed to set eap phase 2 method: " + eapConfig.getPhase2Method());
                return false;
            } else {
                String eapParam = eapConfig.getFieldValue("identity");
                if (TextUtils.isEmpty(eapParam) || setEapIdentity(NativeUtil.stringToByteArrayList(eapParam))) {
                    String eapParam2 = eapConfig.getFieldValue("anonymous_identity");
                    if (TextUtils.isEmpty(eapParam2) || setEapAnonymousIdentity(NativeUtil.stringToByteArrayList(eapParam2))) {
                        String eapParam3 = eapConfig.getFieldValue("password");
                        if (TextUtils.isEmpty(eapParam3) || setEapPassword(NativeUtil.stringToByteArrayList(eapParam3))) {
                            String eapParam4 = eapConfig.getFieldValue(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_CLIENT_CERT);
                            if (TextUtils.isEmpty(eapParam4) || setEapClientCert(eapParam4)) {
                                String eapParam5 = eapConfig.getFieldValue(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_CA_CERT);
                                if (TextUtils.isEmpty(eapParam5) || setEapCACert(eapParam5)) {
                                    String eapParam6 = eapConfig.getFieldValue("subject_match");
                                    if (TextUtils.isEmpty(eapParam6) || setEapSubjectMatch(eapParam6)) {
                                        String eapParam7 = eapConfig.getFieldValue("engine_id");
                                        if (TextUtils.isEmpty(eapParam7) || setEapEngineID(eapParam7)) {
                                            String eapParam8 = eapConfig.getFieldValue("engine");
                                            if (TextUtils.isEmpty(eapParam8) || setEapEngine(eapParam8.equals("1"))) {
                                                String eapParam9 = eapConfig.getFieldValue("key_id");
                                                if (TextUtils.isEmpty(eapParam9) || setEapPrivateKeyId(eapParam9)) {
                                                    String eapParam10 = eapConfig.getFieldValue("altsubject_match");
                                                    if (TextUtils.isEmpty(eapParam10) || setEapAltSubjectMatch(eapParam10)) {
                                                        String eapParam11 = eapConfig.getFieldValue("domain_suffix_match");
                                                        if (TextUtils.isEmpty(eapParam11) || setEapDomainSuffixMatch(eapParam11)) {
                                                            String eapParam12 = eapConfig.getFieldValue(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_CA_PATH);
                                                            if (TextUtils.isEmpty(eapParam12) || setEapCAPath(eapParam12)) {
                                                                String eapParam13 = eapConfig.getFieldValue("proactive_key_caching");
                                                                if (TextUtils.isEmpty(eapParam13) || setEapProactiveKeyCaching(eapParam13.equals("1"))) {
                                                                    return true;
                                                                }
                                                                Log.e(TAG, ssid + ": failed to set proactive key caching: " + eapParam13);
                                                                return false;
                                                            }
                                                            Log.e(TAG, ssid + ": failed to set eap ca path: " + eapParam12);
                                                            return false;
                                                        }
                                                        Log.e(TAG, ssid + ": failed to set eap domain suffix match: " + eapParam11);
                                                        return false;
                                                    }
                                                    Log.e(TAG, ssid + ": failed to set eap alt subject match: " + eapParam10);
                                                    return false;
                                                }
                                                Log.e(TAG, ssid + ": failed to set eap private key: " + eapParam9);
                                                return false;
                                            }
                                            Log.e(TAG, ssid + ": failed to set eap engine: " + eapParam8);
                                            return false;
                                        }
                                        Log.e(TAG, ssid + ": failed to set eap engine id: " + eapParam7);
                                        return false;
                                    }
                                    Log.e(TAG, ssid + ": failed to set eap subject match: " + eapParam6);
                                    return false;
                                }
                                Log.e(TAG, ssid + ": failed to set eap ca cert: " + eapParam5);
                                return false;
                            }
                            Log.e(TAG, ssid + ": failed to set eap client cert: " + eapParam4);
                            return false;
                        }
                        Log.e(TAG, ssid + ": failed to set eap password");
                        return false;
                    }
                    Log.e(TAG, ssid + ": failed to set eap anonymous identity: " + eapParam2);
                    return false;
                }
                Log.e(TAG, ssid + ": failed to set eap identity: " + eapParam);
                return false;
            }
        }
    }

    private static int wifiConfigurationToSupplicantKeyMgmtMask(BitSet keyMgmt) {
        int mask = 0;
        int bit = keyMgmt.nextSetBit(0);
        while (bit != -1) {
            switch (bit) {
                case 0:
                    mask |= 4;
                    break;
                case 1:
                    mask |= 2;
                    break;
                case 2:
                    mask |= 1;
                    break;
                case 3:
                    mask |= 8;
                    break;
                case 5:
                    mask |= 32768;
                    break;
                case 6:
                    mask |= 64;
                    break;
                case 7:
                    mask |= 32;
                    break;
                case 8:
                    mask |= 4096;
                    break;
                case 9:
                    mask |= 8192;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid protoMask bit in keyMgmt: " + bit);
            }
            bit = keyMgmt.nextSetBit(bit + 1);
        }
        return mask;
    }

    private static int wifiConfigurationToSupplicantProtoMask(BitSet protoMask) {
        int mask = 0;
        int bit = protoMask.nextSetBit(0);
        while (bit != -1) {
            switch (bit) {
                case 0:
                    mask |= 1;
                    break;
                case 1:
                    mask |= 2;
                    break;
                case 2:
                    mask |= 8;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid protoMask bit in wificonfig: " + bit);
            }
            bit = protoMask.nextSetBit(bit + 1);
        }
        return mask;
    }

    private static int wifiConfigurationToSupplicantAuthAlgMask(BitSet authAlgMask) {
        int mask = 0;
        int bit = authAlgMask.nextSetBit(0);
        while (bit != -1) {
            switch (bit) {
                case 0:
                    mask |= 1;
                    break;
                case 1:
                    mask |= 2;
                    break;
                case 2:
                    mask |= 4;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid authAlgMask bit in wificonfig: " + bit);
            }
            bit = authAlgMask.nextSetBit(bit + 1);
        }
        return mask;
    }

    private static int wifiConfigurationToSupplicantGroupCipherMask(BitSet groupCipherMask) {
        int mask = 0;
        int bit = groupCipherMask.nextSetBit(0);
        while (bit != -1) {
            switch (bit) {
                case 0:
                    mask |= 2;
                    break;
                case 1:
                    mask |= 4;
                    break;
                case 2:
                    mask |= 8;
                    break;
                case 3:
                    mask |= 16;
                    break;
                case 4:
                    mask |= 16384;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid GroupCipherMask bit in wificonfig: " + bit);
            }
            bit = groupCipherMask.nextSetBit(bit + 1);
        }
        return mask;
    }

    private static int wifiConfigurationToSupplicantPairwiseCipherMask(BitSet pairwiseCipherMask) {
        int mask = 0;
        int bit = pairwiseCipherMask.nextSetBit(0);
        while (bit != -1) {
            switch (bit) {
                case 0:
                    mask |= 1;
                    break;
                case 1:
                    mask |= 8;
                    break;
                case 2:
                    mask |= 16;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid pairwiseCipherMask bit in wificonfig: " + bit);
            }
            bit = pairwiseCipherMask.nextSetBit(bit + 1);
        }
        return mask;
    }

    private static int supplicantToWifiConfigurationEapMethod(int value) {
        switch (value) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            default:
                Log.e(TAG, "invalid eap method value from supplicant: " + value);
                return -1;
        }
    }

    private static int supplicantToWifiConfigurationEapPhase2Method(int value) {
        switch (value) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            default:
                Log.e(TAG, "invalid eap phase2 method value from supplicant: " + value);
                return -1;
        }
    }

    private static int supplicantMaskValueToWifiConfigurationBitSet(int supplicantMask, int supplicantValue, BitSet bitset, int bitSetPosition) {
        bitset.set(bitSetPosition, (supplicantMask & supplicantValue) == supplicantValue);
        return (~supplicantValue) & supplicantMask;
    }

    private static BitSet supplicantToWifiConfigurationKeyMgmtMask(int mask) {
        BitSet bitset = new BitSet();
        int mask2 = supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(mask, 4, bitset, 0), 2, bitset, 1), 1, bitset, 2), 8, bitset, 3), 32768, bitset, 5), 64, bitset, 6), 32, bitset, 7);
        if (mask2 == 0) {
            return bitset;
        }
        throw new IllegalArgumentException("invalid key mgmt mask from supplicant: " + mask2);
    }

    private static BitSet supplicantToWifiConfigurationProtoMask(int mask) {
        BitSet bitset = new BitSet();
        int mask2 = supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(mask, 1, bitset, 0), 2, bitset, 1), 8, bitset, 2);
        if (mask2 == 0) {
            return bitset;
        }
        throw new IllegalArgumentException("invalid proto mask from supplicant: " + mask2);
    }

    private static BitSet supplicantToWifiConfigurationAuthAlgMask(int mask) {
        BitSet bitset = new BitSet();
        int mask2 = supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(mask, 1, bitset, 0), 2, bitset, 1), 4, bitset, 2);
        if (mask2 == 0) {
            return bitset;
        }
        throw new IllegalArgumentException("invalid auth alg mask from supplicant: " + mask2);
    }

    private static BitSet supplicantToWifiConfigurationGroupCipherMask(int mask) {
        BitSet bitset = new BitSet();
        int mask2 = supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(mask, 2, bitset, 0), 4, bitset, 1), 8, bitset, 2), 16, bitset, 3), 16384, bitset, 4);
        if (mask2 == 0) {
            return bitset;
        }
        throw new IllegalArgumentException("invalid group cipher mask from supplicant: " + mask2);
    }

    private static BitSet supplicantToWifiConfigurationPairwiseCipherMask(int mask) {
        BitSet bitset = new BitSet();
        int mask2 = supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(supplicantMaskValueToWifiConfigurationBitSet(mask, 1, bitset, 0), 8, bitset, 1), 16, bitset, 2);
        if (mask2 == 0) {
            return bitset;
        }
        throw new IllegalArgumentException("invalid pairwise cipher mask from supplicant: " + mask2);
    }

    private static int wifiConfigurationToSupplicantEapMethod(int value) {
        switch (value) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            default:
                Log.e(TAG, "invalid eap method value from WifiConfiguration: " + value);
                return -1;
        }
    }

    private static int wifiConfigurationToSupplicantEapPhase2Method(int value) {
        switch (value) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            default:
                Log.e(TAG, "invalid eap phase2 method value from WifiConfiguration: " + value);
                return -1;
        }
    }

    private boolean getId() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getId")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getId(new ISupplicantNetwork.getIdCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, int i) {
                        SupplicantStaNetworkHal.lambda$getId$0(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, i);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getId");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getId$0(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, int idValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mNetworkId = idValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getId");
        }
    }

    private boolean registerCallback(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetworkCallback callback) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("registerCallback")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.registerCallback(callback), "registerCallback");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "registerCallback");
                return false;
            }
        }
    }

    private boolean setSsid(ArrayList<Byte> ssid) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setSsid")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setSsid(ssid), "setSsid");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setSsid");
                return false;
            }
        }
    }

    public boolean setBssid(String bssidStr) {
        boolean bssid;
        synchronized (this.mLock) {
            try {
                bssid = setBssid(NativeUtil.macAddressToByteArray(bssidStr));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + bssidStr, e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return bssid;
    }

    private boolean setBssid(byte[] bssid) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setBssid")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setBssid(bssid), "setBssid");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setBssid");
                return false;
            }
        }
    }

    private boolean setScanSsid(boolean enable) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setScanSsid")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setScanSsid(enable), "setScanSsid");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setScanSsid");
                return false;
            }
        }
    }

    private boolean setKeyMgmt(int keyMgmtMask) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setKeyMgmt")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setKeyMgmt(keyMgmtMask), "setKeyMgmt");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setKeyMgmt");
                return false;
            }
        }
    }

    private boolean setProto(int protoMask) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setProto")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setProto(protoMask), "setProto");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setProto");
                return false;
            }
        }
    }

    private boolean setAuthAlg(int authAlgMask) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setAuthAlg")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setAuthAlg(authAlgMask), "setAuthAlg");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setAuthAlg");
                return false;
            }
        }
    }

    private boolean setGroupCipher(int groupCipherMask) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setGroupCipher")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setGroupCipher(groupCipherMask), "setGroupCipher");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setGroupCipher");
                return false;
            }
        }
    }

    private boolean setPairwiseCipher(int pairwiseCipherMask) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setPairwiseCipher")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setPairwiseCipher(pairwiseCipherMask), "setPairwiseCipher");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setPairwiseCipher");
                return false;
            }
        }
    }

    private boolean setPskPassphrase(String psk) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setPskPassphrase")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setPskPassphrase(psk), "setPskPassphrase");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setPskPassphrase");
                return false;
            }
        }
    }

    private boolean setPsk(byte[] psk) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setPsk")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setPsk(psk), "setPsk");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setPsk");
                return false;
            }
        }
    }

    private boolean setWepKey(int keyIdx, ArrayList<Byte> wepKey) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setWepKey")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setWepKey(keyIdx, wepKey), "setWepKey");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWepKey");
                return false;
            }
        }
    }

    private boolean setWepTxKeyIdx(int keyIdx) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setWepTxKeyIdx")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setWepTxKeyIdx(keyIdx), "setWepTxKeyIdx");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWepTxKeyIdx");
                return false;
            }
        }
    }

    private boolean setRequirePmf(boolean enable) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setRequirePmf")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setRequirePmf(enable), "setRequirePmf");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setRequirePmf");
                return false;
            }
        }
    }

    private boolean setUpdateIdentifier(int identifier) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setUpdateIdentifier")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setUpdateIdentifier(identifier), "setUpdateIdentifier");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setUpdateIdentifier");
                return false;
            }
        }
    }

    private boolean setEapMethod(int method) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapMethod")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setEapMethod(method), "setEapMethod");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapMethod");
                return false;
            }
        }
    }

    private boolean setEapPhase2Method(int method) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapPhase2Method")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setEapPhase2Method(method), "setEapPhase2Method");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapPhase2Method");
                return false;
            }
        }
    }

    private boolean setEapIdentity(ArrayList<Byte> identity) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapIdentity")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setEapIdentity(identity), "setEapIdentity");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapIdentity");
                return false;
            }
        }
    }

    private boolean setEapAnonymousIdentity(ArrayList<Byte> identity) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapAnonymousIdentity")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setEapAnonymousIdentity(identity), "setEapAnonymousIdentity");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapAnonymousIdentity");
                return false;
            }
        }
    }

    private boolean setEapPassword(ArrayList<Byte> password) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapPassword")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setEapPassword(password), "setEapPassword");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapPassword");
                return false;
            }
        }
    }

    private boolean setEapCACert(String path) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapCACert")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setEapCACert(path), "setEapCACert");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapCACert");
                return false;
            }
        }
    }

    private boolean setEapCAPath(String path) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapCAPath")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setEapCAPath(path), "setEapCAPath");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapCAPath");
                return false;
            }
        }
    }

    private boolean setEapClientCert(String path) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapClientCert")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setEapClientCert(path), "setEapClientCert");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapClientCert");
                return false;
            }
        }
    }

    private boolean setEapPrivateKeyId(String id) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapPrivateKeyId")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setEapPrivateKeyId(id), "setEapPrivateKeyId");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapPrivateKeyId");
                return false;
            }
        }
    }

    private boolean setEapSubjectMatch(String match) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapSubjectMatch")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setEapSubjectMatch(match), "setEapSubjectMatch");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapSubjectMatch");
                return false;
            }
        }
    }

    private boolean setEapAltSubjectMatch(String match) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapAltSubjectMatch")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setEapAltSubjectMatch(match), "setEapAltSubjectMatch");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapAltSubjectMatch");
                return false;
            }
        }
    }

    private boolean setEapEngine(boolean enable) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapEngine")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setEapEngine(enable), "setEapEngine");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapEngine");
                return false;
            }
        }
    }

    private boolean setEapEngineID(String id) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapEngineID")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setEapEngineID(id), "setEapEngineID");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapEngineID");
                return false;
            }
        }
    }

    private boolean setEapDomainSuffixMatch(String match) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapDomainSuffixMatch")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setEapDomainSuffixMatch(match), "setEapDomainSuffixMatch");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapDomainSuffixMatch");
                return false;
            }
        }
    }

    private boolean setEapProactiveKeyCaching(boolean enable) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setEapProactiveKeyCaching")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setProactiveKeyCaching(enable), "setEapProactiveKeyCaching");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setEapProactiveKeyCaching");
                return false;
            }
        }
    }

    private boolean setIdStr(String idString) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("setIdStr")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.setIdStr(idString), "setIdStr");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setIdStr");
                return false;
            }
        }
    }

    private boolean getSsid() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getSsid")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getSsid(new ISupplicantStaNetwork.getSsidCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        SupplicantStaNetworkHal.lambda$getSsid$1(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, arrayList);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getSsid");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getSsid$1(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, ArrayList ssidValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mSsid = ssidValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getSsid");
        }
    }

    private boolean getBssid() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getBssid")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getBssid(new ISupplicantStaNetwork.getBssidCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
                        SupplicantStaNetworkHal.lambda$getBssid$2(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, bArr);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getBssid");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getBssid$2(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, byte[] bssidValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mBssid = bssidValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getBssid");
        }
    }

    private boolean getScanSsid() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getScanSsid")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getScanSsid(new ISupplicantStaNetwork.getScanSsidCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, boolean z) {
                        SupplicantStaNetworkHal.lambda$getScanSsid$3(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, z);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getScanSsid");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getScanSsid$3(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, boolean enabledValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mScanSsid = enabledValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getScanSsid");
        }
    }

    private boolean getKeyMgmt() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getKeyMgmt")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getKeyMgmt(new ISupplicantStaNetwork.getKeyMgmtCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, int i) {
                        SupplicantStaNetworkHal.lambda$getKeyMgmt$4(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, i);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getKeyMgmt");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getKeyMgmt$4(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, int keyMgmtMaskValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mKeyMgmtMask = keyMgmtMaskValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getKeyMgmt");
        }
    }

    private boolean getProto() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getProto")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getProto(new ISupplicantStaNetwork.getProtoCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, int i) {
                        SupplicantStaNetworkHal.lambda$getProto$5(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, i);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getProto");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getProto$5(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, int protoMaskValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mProtoMask = protoMaskValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getProto");
        }
    }

    private boolean getAuthAlg() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getAuthAlg")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getAuthAlg(new ISupplicantStaNetwork.getAuthAlgCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, int i) {
                        SupplicantStaNetworkHal.lambda$getAuthAlg$6(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, i);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getAuthAlg");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getAuthAlg$6(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, int authAlgMaskValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mAuthAlgMask = authAlgMaskValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getAuthAlg");
        }
    }

    private boolean getGroupCipher() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getGroupCipher")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getGroupCipher(new ISupplicantStaNetwork.getGroupCipherCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, int i) {
                        SupplicantStaNetworkHal.lambda$getGroupCipher$7(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, i);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getGroupCipher");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getGroupCipher$7(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, int groupCipherMaskValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mGroupCipherMask = groupCipherMaskValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getGroupCipher");
        }
    }

    private boolean getPairwiseCipher() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getPairwiseCipher")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getPairwiseCipher(new ISupplicantStaNetwork.getPairwiseCipherCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, int i) {
                        SupplicantStaNetworkHal.lambda$getPairwiseCipher$8(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, i);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getPairwiseCipher");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getPairwiseCipher$8(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, int pairwiseCipherMaskValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mPairwiseCipherMask = pairwiseCipherMaskValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getPairwiseCipher");
        }
    }

    private boolean getPskPassphrase() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getPskPassphrase")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getPskPassphrase(new ISupplicantStaNetwork.getPskPassphraseCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaNetworkHal.lambda$getPskPassphrase$9(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, str);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getPskPassphrase");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getPskPassphrase$9(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, String pskValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mPskPassphrase = pskValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getPskPassphrase");
        }
    }

    private boolean getPsk() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getPsk")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getPsk(new ISupplicantStaNetwork.getPskCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
                        SupplicantStaNetworkHal.lambda$getPsk$10(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, bArr);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getPsk");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getPsk$10(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, byte[] pskValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mPsk = pskValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getPsk");
        }
    }

    private boolean getWepKey(int keyIdx) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("keyIdx")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getWepKey(keyIdx, new ISupplicantStaNetwork.getWepKeyCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        SupplicantStaNetworkHal.lambda$getWepKey$11(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, arrayList);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "keyIdx");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getWepKey$11(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, ArrayList wepKeyValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mWepKey = wepKeyValue;
            return;
        }
        Log.e(TAG, "keyIdx,  failed: " + status.debugMessage);
    }

    private boolean getWepTxKeyIdx() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getWepTxKeyIdx")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getWepTxKeyIdx(new ISupplicantStaNetwork.getWepTxKeyIdxCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, int i) {
                        SupplicantStaNetworkHal.lambda$getWepTxKeyIdx$12(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, i);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getWepTxKeyIdx");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getWepTxKeyIdx$12(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, int keyIdxValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mWepTxKeyIdx = keyIdxValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getWepTxKeyIdx");
        }
    }

    private boolean getRequirePmf() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getRequirePmf")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getRequirePmf(new ISupplicantStaNetwork.getRequirePmfCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, boolean z) {
                        SupplicantStaNetworkHal.lambda$getRequirePmf$13(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, z);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getRequirePmf");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getRequirePmf$13(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, boolean enabledValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mRequirePmf = enabledValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getRequirePmf");
        }
    }

    private boolean getEapMethod() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getEapMethod")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getEapMethod(new ISupplicantStaNetwork.getEapMethodCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, int i) {
                        SupplicantStaNetworkHal.lambda$getEapMethod$14(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, i);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getEapMethod");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getEapMethod$14(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, int methodValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mEapMethod = methodValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getEapMethod");
        }
    }

    private boolean getEapPhase2Method() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getEapPhase2Method")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getEapPhase2Method(new ISupplicantStaNetwork.getEapPhase2MethodCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, int i) {
                        SupplicantStaNetworkHal.lambda$getEapPhase2Method$15(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, i);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getEapPhase2Method");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getEapPhase2Method$15(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, int methodValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mEapPhase2Method = methodValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getEapPhase2Method");
        }
    }

    private boolean getEapIdentity() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getEapIdentity")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getEapIdentity(new ISupplicantStaNetwork.getEapIdentityCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        SupplicantStaNetworkHal.lambda$getEapIdentity$16(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, arrayList);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getEapIdentity");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getEapIdentity$16(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, ArrayList identityValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mEapIdentity = identityValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getEapIdentity");
        }
    }

    private boolean getEapAnonymousIdentity() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getEapAnonymousIdentity")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getEapAnonymousIdentity(new ISupplicantStaNetwork.getEapAnonymousIdentityCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        SupplicantStaNetworkHal.lambda$getEapAnonymousIdentity$17(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, arrayList);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getEapAnonymousIdentity");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getEapAnonymousIdentity$17(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, ArrayList identityValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mEapAnonymousIdentity = identityValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getEapAnonymousIdentity");
        }
    }

    public String fetchEapAnonymousIdentity() {
        synchronized (this.mLock) {
            if (!getEapAnonymousIdentity()) {
                return null;
            }
            String stringFromByteArrayList = NativeUtil.stringFromByteArrayList(this.mEapAnonymousIdentity);
            return stringFromByteArrayList;
        }
    }

    private boolean getEapPassword() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getEapPassword")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getEapPassword(new ISupplicantStaNetwork.getEapPasswordCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        SupplicantStaNetworkHal.lambda$getEapPassword$18(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, arrayList);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getEapPassword");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getEapPassword$18(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, ArrayList passwordValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mEapPassword = passwordValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getEapPassword");
        }
    }

    private boolean getEapCACert() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getEapCACert")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getEapCACert(new ISupplicantStaNetwork.getEapCACertCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaNetworkHal.lambda$getEapCACert$19(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, str);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getEapCACert");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getEapCACert$19(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, String pathValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mEapCACert = pathValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getEapCACert");
        }
    }

    private boolean getEapCAPath() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getEapCAPath")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getEapCAPath(new ISupplicantStaNetwork.getEapCAPathCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaNetworkHal.lambda$getEapCAPath$20(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, str);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getEapCAPath");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getEapCAPath$20(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, String pathValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mEapCAPath = pathValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getEapCAPath");
        }
    }

    private boolean getEapClientCert() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getEapClientCert")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getEapClientCert(new ISupplicantStaNetwork.getEapClientCertCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaNetworkHal.lambda$getEapClientCert$21(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, str);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getEapClientCert");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getEapClientCert$21(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, String pathValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mEapClientCert = pathValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getEapClientCert");
        }
    }

    private boolean getEapPrivateKeyId() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getEapPrivateKeyId")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getEapPrivateKeyId(new ISupplicantStaNetwork.getEapPrivateKeyIdCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaNetworkHal.lambda$getEapPrivateKeyId$22(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, str);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getEapPrivateKeyId");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getEapPrivateKeyId$22(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, String idValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mEapPrivateKeyId = idValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getEapPrivateKeyId");
        }
    }

    private boolean getEapSubjectMatch() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getEapSubjectMatch")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getEapSubjectMatch(new ISupplicantStaNetwork.getEapSubjectMatchCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaNetworkHal.lambda$getEapSubjectMatch$23(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, str);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getEapSubjectMatch");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getEapSubjectMatch$23(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, String matchValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mEapSubjectMatch = matchValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getEapSubjectMatch");
        }
    }

    private boolean getEapAltSubjectMatch() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getEapAltSubjectMatch")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getEapAltSubjectMatch(new ISupplicantStaNetwork.getEapAltSubjectMatchCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaNetworkHal.lambda$getEapAltSubjectMatch$24(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, str);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getEapAltSubjectMatch");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getEapAltSubjectMatch$24(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, String matchValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mEapAltSubjectMatch = matchValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getEapAltSubjectMatch");
        }
    }

    private boolean getEapEngine() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getEapEngine")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getEapEngine(new ISupplicantStaNetwork.getEapEngineCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, boolean z) {
                        SupplicantStaNetworkHal.lambda$getEapEngine$25(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, z);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getEapEngine");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getEapEngine$25(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, boolean enabledValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mEapEngine = enabledValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getEapEngine");
        }
    }

    private boolean getEapEngineID() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getEapEngineID")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getEapEngineID(new ISupplicantStaNetwork.getEapEngineIDCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaNetworkHal.lambda$getEapEngineID$26(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, str);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getEapEngineID");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getEapEngineID$26(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, String idValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mEapEngineID = idValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getEapEngineID");
        }
    }

    private boolean getEapDomainSuffixMatch() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getEapDomainSuffixMatch")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getEapDomainSuffixMatch(new ISupplicantStaNetwork.getEapDomainSuffixMatchCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaNetworkHal.lambda$getEapDomainSuffixMatch$27(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, str);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getEapDomainSuffixMatch");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getEapDomainSuffixMatch$27(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, String matchValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mEapDomainSuffixMatch = matchValue;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getEapDomainSuffixMatch");
        }
    }

    private boolean getIdStr() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getIdStr")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mISupplicantStaNetwork.getIdStr(new ISupplicantStaNetwork.getIdStrCallback(statusOk) {
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaNetworkHal.lambda$getIdStr$28(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, str);
                    }
                });
                boolean z = statusOk.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e, "getIdStr");
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getIdStr$28(SupplicantStaNetworkHal supplicantStaNetworkHal, MutableBoolean statusOk, SupplicantStatus status, String idString) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            supplicantStaNetworkHal.mIdStr = idString;
        } else {
            supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getIdStr");
        }
    }

    private boolean enable(boolean noConnect) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("enable")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.enable(noConnect), "enable");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "enable");
                return false;
            }
        }
    }

    private boolean disable() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("disable")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.disable(), "disable");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "disable");
                return false;
            }
        }
    }

    public boolean select() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("select")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.select(), "select");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "select");
                return false;
            }
        }
    }

    public boolean sendNetworkEapSimGsmAuthResponse(String paramsStr) {
        synchronized (this.mLock) {
            try {
                Matcher match = GSM_AUTH_RESPONSE_PARAMS_PATTERN.matcher(paramsStr);
                ArrayList<ISupplicantStaNetwork.NetworkResponseEapSimGsmAuthParams> params = new ArrayList<>();
                while (match.find()) {
                    if (match.groupCount() != 2) {
                        Log.e(TAG, "Malformed gsm auth response params: " + paramsStr);
                        return false;
                    }
                    ISupplicantStaNetwork.NetworkResponseEapSimGsmAuthParams param = new ISupplicantStaNetwork.NetworkResponseEapSimGsmAuthParams();
                    byte[] kc = NativeUtil.hexStringToByteArray(match.group(1));
                    if (kc != null) {
                        if (kc.length == param.kc.length) {
                            byte[] sres = NativeUtil.hexStringToByteArray(match.group(2));
                            if (sres != null) {
                                if (sres.length == param.sres.length) {
                                    System.arraycopy(kc, 0, param.kc, 0, param.kc.length);
                                    System.arraycopy(sres, 0, param.sres, 0, param.sres.length);
                                    params.add(param);
                                }
                            }
                            Log.e(TAG, "Invalid sres value: " + match.group(2));
                            return false;
                        }
                    }
                    Log.e(TAG, "Invalid kc value: " + match.group(1));
                    return false;
                }
                if (params.size() <= 3) {
                    if (params.size() >= 2) {
                        boolean sendNetworkEapSimGsmAuthResponse = sendNetworkEapSimGsmAuthResponse(params);
                        return sendNetworkEapSimGsmAuthResponse;
                    }
                }
                Log.e(TAG, "Malformed gsm auth response params: " + paramsStr);
                return false;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + paramsStr, e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private boolean sendNetworkEapSimGsmAuthResponse(ArrayList<ISupplicantStaNetwork.NetworkResponseEapSimGsmAuthParams> params) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("sendNetworkEapSimGsmAuthResponse")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.sendNetworkEapSimGsmAuthResponse(params), "sendNetworkEapSimGsmAuthResponse");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "sendNetworkEapSimGsmAuthResponse");
                return false;
            }
        }
    }

    public boolean sendNetworkEapSimGsmAuthFailure() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("sendNetworkEapSimGsmAuthFailure")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.sendNetworkEapSimGsmAuthFailure(), "sendNetworkEapSimGsmAuthFailure");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "sendNetworkEapSimGsmAuthFailure");
                return false;
            }
        }
    }

    public boolean sendNetworkEapSimUmtsAuthResponse(String paramsStr) {
        synchronized (this.mLock) {
            try {
                Matcher match = UMTS_AUTH_RESPONSE_PARAMS_PATTERN.matcher(paramsStr);
                if (match.find()) {
                    if (match.groupCount() == 3) {
                        ISupplicantStaNetwork.NetworkResponseEapSimUmtsAuthParams params = new ISupplicantStaNetwork.NetworkResponseEapSimUmtsAuthParams();
                        byte[] ik = NativeUtil.hexStringToByteArray(match.group(1));
                        if (ik != null) {
                            if (ik.length == params.ik.length) {
                                byte[] ck = NativeUtil.hexStringToByteArray(match.group(2));
                                if (ck != null) {
                                    if (ck.length == params.ck.length) {
                                        byte[] res = NativeUtil.hexStringToByteArray(match.group(3));
                                        if (res != null) {
                                            if (res.length != 0) {
                                                System.arraycopy(ik, 0, params.ik, 0, params.ik.length);
                                                System.arraycopy(ck, 0, params.ck, 0, params.ck.length);
                                                for (byte b : res) {
                                                    params.res.add(Byte.valueOf(b));
                                                }
                                                boolean sendNetworkEapSimUmtsAuthResponse = sendNetworkEapSimUmtsAuthResponse(params);
                                                return sendNetworkEapSimUmtsAuthResponse;
                                            }
                                        }
                                        Log.e(TAG, "Invalid res value: " + match.group(3));
                                        return false;
                                    }
                                }
                                Log.e(TAG, "Invalid ck value: " + match.group(2));
                                return false;
                            }
                        }
                        Log.e(TAG, "Invalid ik value: " + match.group(1));
                        return false;
                    }
                }
                Log.e(TAG, "Malformed umts auth response params: " + paramsStr);
                return false;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + paramsStr, e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private boolean sendNetworkEapSimUmtsAuthResponse(ISupplicantStaNetwork.NetworkResponseEapSimUmtsAuthParams params) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("sendNetworkEapSimUmtsAuthResponse")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.sendNetworkEapSimUmtsAuthResponse(params), "sendNetworkEapSimUmtsAuthResponse");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "sendNetworkEapSimUmtsAuthResponse");
                return false;
            }
        }
    }

    public boolean sendNetworkEapSimUmtsAutsResponse(String paramsStr) {
        synchronized (this.mLock) {
            try {
                Matcher match = UMTS_AUTS_RESPONSE_PARAMS_PATTERN.matcher(paramsStr);
                if (match.find()) {
                    if (match.groupCount() == 1) {
                        byte[] auts = NativeUtil.hexStringToByteArray(match.group(1));
                        if (auts != null) {
                            if (auts.length == 14) {
                                boolean sendNetworkEapSimUmtsAutsResponse = sendNetworkEapSimUmtsAutsResponse(auts);
                                return sendNetworkEapSimUmtsAutsResponse;
                            }
                        }
                        Log.e(TAG, "Invalid auts value: " + match.group(1));
                        return false;
                    }
                }
                Log.e(TAG, "Malformed umts auts response params: " + paramsStr);
                return false;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + paramsStr, e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private boolean sendNetworkEapSimUmtsAutsResponse(byte[] auts) {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("sendNetworkEapSimUmtsAutsResponse")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.sendNetworkEapSimUmtsAutsResponse(auts), "sendNetworkEapSimUmtsAutsResponse");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "sendNetworkEapSimUmtsAutsResponse");
                return false;
            }
        }
    }

    public boolean sendNetworkEapSimUmtsAuthFailure() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("sendNetworkEapSimUmtsAuthFailure")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicantStaNetwork.sendNetworkEapSimUmtsAuthFailure(), "sendNetworkEapSimUmtsAuthFailure");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "sendNetworkEapSimUmtsAuthFailure");
                return false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork getSupplicantStaNetworkForV1_1Mockable() {
        if (this.mISupplicantStaNetwork == null) {
            return null;
        }
        return android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork.castFrom(this.mISupplicantStaNetwork);
    }

    public boolean sendNetworkEapIdentityResponse(String identityStr, String encryptedIdentityStr) {
        boolean sendNetworkEapIdentityResponse;
        synchronized (this.mLock) {
            try {
                ArrayList<Byte> unencryptedIdentity = NativeUtil.stringToByteArrayList(identityStr);
                ArrayList<Byte> encryptedIdentity = null;
                if (!TextUtils.isEmpty(encryptedIdentityStr)) {
                    encryptedIdentity = NativeUtil.stringToByteArrayList(encryptedIdentityStr);
                }
                sendNetworkEapIdentityResponse = sendNetworkEapIdentityResponse(unencryptedIdentity, encryptedIdentity);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument identityStr");
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return sendNetworkEapIdentityResponse;
    }

    private boolean sendNetworkEapIdentityResponse(ArrayList<Byte> unencryptedIdentity, ArrayList<Byte> encryptedIdentity) {
        SupplicantStatus status;
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("sendNetworkEapIdentityResponse")) {
                return false;
            }
            try {
                android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork iSupplicantStaNetworkV11 = getSupplicantStaNetworkForV1_1Mockable();
                if (iSupplicantStaNetworkV11 == null || encryptedIdentity == null) {
                    status = this.mISupplicantStaNetwork.sendNetworkEapIdentityResponse(unencryptedIdentity);
                } else {
                    status = iSupplicantStaNetworkV11.sendNetworkEapIdentityResponse_1_1(unencryptedIdentity, encryptedIdentity);
                }
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(status, "sendNetworkEapIdentityResponse");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "sendNetworkEapIdentityResponse");
                return false;
            }
        }
    }

    public String getWpsNfcConfigurationToken() {
        synchronized (this.mLock) {
            ArrayList<Byte> token = getWpsNfcConfigurationTokenInternal();
            if (token == null) {
                return null;
            }
            String hexStringFromByteArray = NativeUtil.hexStringFromByteArray(NativeUtil.byteArrayFromArrayList(token));
            return hexStringFromByteArray;
        }
    }

    private ArrayList<Byte> getWpsNfcConfigurationTokenInternal() {
        synchronized (this.mLock) {
            if (!checkISupplicantStaNetworkAndLogFailure("getWpsNfcConfigurationToken")) {
                return null;
            }
            HidlSupport.Mutable<ArrayList<Byte>> gotToken = new HidlSupport.Mutable<>();
            try {
                this.mISupplicantStaNetwork.getWpsNfcConfigurationToken(new ISupplicantStaNetwork.getWpsNfcConfigurationTokenCallback(gotToken) {
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        SupplicantStaNetworkHal.lambda$getWpsNfcConfigurationTokenInternal$29(SupplicantStaNetworkHal.this, this.f$1, supplicantStatus, arrayList);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "getWpsNfcConfigurationToken");
            }
            ArrayList<Byte> arrayList = (ArrayList) gotToken.value;
            return arrayList;
        }
    }

    public static /* synthetic */ void lambda$getWpsNfcConfigurationTokenInternal$29(SupplicantStaNetworkHal supplicantStaNetworkHal, HidlSupport.Mutable gotToken, SupplicantStatus status, ArrayList token) {
        if (supplicantStaNetworkHal.checkStatusAndLogFailure(status, "getWpsNfcConfigurationToken")) {
            gotToken.value = token;
        }
    }

    private boolean checkStatusAndLogFailure(SupplicantStatus status, String methodStr) {
        synchronized (this.mLock) {
            if (status.code != 0) {
                Log.e(TAG, "ISupplicantStaNetwork." + methodStr + " failed: " + status);
                return false;
            }
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "ISupplicantStaNetwork." + methodStr + " succeeded");
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void logCallback(String methodStr) {
        synchronized (this.mLock) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "ISupplicantStaNetworkCallback." + methodStr + " received");
            }
        }
    }

    private boolean checkISupplicantStaNetworkAndLogFailure(String methodStr) {
        synchronized (this.mLock) {
            if (this.mISupplicantStaNetwork != null) {
                return true;
            }
            Log.e(TAG, "Can't call " + methodStr + ", ISupplicantStaNetwork is null");
            return false;
        }
    }

    private void handleRemoteException(RemoteException e, String methodStr) {
        synchronized (this.mLock) {
            this.mISupplicantStaNetwork = null;
            Log.e(TAG, "ISupplicantStaNetwork." + methodStr + " failed with exception", e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0026, code lost:
        return r1;
     */
    private BitSet addFastTransitionFlags(BitSet keyManagementFlags) {
        synchronized (this.mLock) {
            if (!this.mSystemSupportsFastBssTransition) {
                return keyManagementFlags;
            }
            BitSet modifiedFlags = (BitSet) keyManagementFlags.clone();
            if (keyManagementFlags.get(1)) {
                modifiedFlags.set(6);
            }
            if (keyManagementFlags.get(2)) {
                modifiedFlags.set(7);
            }
        }
    }

    private BitSet removeFastTransitionFlags(BitSet keyManagementFlags) {
        BitSet modifiedFlags;
        synchronized (this.mLock) {
            modifiedFlags = (BitSet) keyManagementFlags.clone();
            modifiedFlags.clear(6);
            modifiedFlags.clear(7);
        }
        return modifiedFlags;
    }

    public static String createNetworkExtra(Map<String, String> values) {
        try {
            return URLEncoder.encode(new JSONObject(values).toString(), "UTF-8");
        } catch (NullPointerException e) {
            Log.e(TAG, "Unable to serialize networkExtra: " + e.toString());
            return null;
        } catch (UnsupportedEncodingException e2) {
            Log.e(TAG, "Unable to serialize networkExtra: " + e2.toString());
            return null;
        }
    }

    public static Map<String, String> parseNetworkExtra(String encoded) {
        if (TextUtils.isEmpty(encoded)) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(URLDecoder.decode(encoded, "UTF-8"));
            Map<String, String> values = new HashMap<>();
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = json.get(key);
                if (value instanceof String) {
                    values.put(key, (String) value);
                }
            }
            return values;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unable to deserialize networkExtra: " + e.toString());
            return null;
        } catch (JSONException e2) {
            return null;
        }
    }

    private boolean isVendorSupplicantStaNetworkV2_0() {
        return getVendorSupplicantStaNetworkV2_0() != null;
    }

    private boolean checkVendorISupplicantStaNetworkAndLogFailure(String methodStr) {
        if (!checkISupplicantStaNetworkAndLogFailure(methodStr)) {
            return false;
        }
        if (getVendorSupplicantStaNetworkV2_0() != null) {
            return true;
        }
        Log.e(TAG, "Can't cast mISupplicantStaNetwork to vendor 2.0 version");
        return false;
    }

    private vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork getVendorSupplicantStaNetworkV2_0() {
        synchronized (this.mLock) {
            if (this.mISupplicantStaNetwork == null) {
                return null;
            }
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork castFrom = vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork.castFrom(this.mISupplicantStaNetwork);
            return castFrom;
        }
    }

    private boolean setWapiConfiguration(WifiConfiguration config) {
        if (config.wapiPskTypeBcm != -1 && !setWapiPskKeyType(config.wapiPskTypeBcm)) {
            Log.e(TAG, config.SSID + ": failed to set wapi psk key type: " + config.wapiPskTypeBcm);
            return false;
        } else if (!TextUtils.isEmpty(config.wapiAsCertBcm) && !setWapiAsCertPath(config.wapiAsCertBcm)) {
            Log.e(TAG, config.SSID + ": failed to set wapi as cert path: " + config.wapiAsCertBcm);
            return false;
        } else if (TextUtils.isEmpty(config.wapiUserCertBcm) || setWapiUserCertPath(config.wapiUserCertBcm)) {
            return true;
        } else {
            Log.e(TAG, config.SSID + ": failed to set wapi user cert path: " + config.wapiUserCertBcm);
            return false;
        }
    }

    private boolean hwStaNetworkRegisterCallback(vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaNetworkCallback callback) {
        synchronized (this.mLock) {
            Log.d(TAG, "Start to register network callback for vendor ISupplicantStaNetwork");
            if (!checkVendorISupplicantStaNetworkAndLogFailure("hwStaNetworkRegisterCallback")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(getVendorSupplicantStaNetworkV2_0().hwStaNetworkRegisterCallback(callback), "hwStaNetworkRegisterCallback");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "hwStaNetworkRegisterCallback");
                return false;
            }
        }
    }

    private boolean setWapiPskKeyType(int type) {
        synchronized (this.mLock) {
            if (!checkVendorISupplicantStaNetworkAndLogFailure("setWAPIPskKeyType")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(getVendorSupplicantStaNetworkV2_0().setWAPIPskKeyType(type), "setWAPIPskKeyType");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWAPIPskKeyType");
                return false;
            }
        }
    }

    private boolean setWapiAsCertPath(String path) {
        synchronized (this.mLock) {
            if (!checkVendorISupplicantStaNetworkAndLogFailure("setWapiAsCertPath")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(getVendorSupplicantStaNetworkV2_0().setWAPIASCert(path), "setWapiAsCertPath");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWapiAsCertPath");
                return false;
            }
        }
    }

    private boolean setWapiUserCertPath(String path) {
        synchronized (this.mLock) {
            if (!checkVendorISupplicantStaNetworkAndLogFailure("setWapiUserCertPath")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(getVendorSupplicantStaNetworkV2_0().setWAPIASUECert(path), "setWapiUserCertPath");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWapiUserCertPath");
                return false;
            }
        }
    }
}
