package com.huawei.wifi2;

import android.content.Context;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetworkCallback;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.MutableBoolean;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.ScanResultRecords;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaNetworkCallback;

public class HwWifi2SupplicantStaNetworkHal {
    public static final String ID_STRING_KEY_CONFIG_KEY = "configKey";
    private static final String ID_STRING_KEY_CREATOR_UID = "creatorUid";
    private static final String ID_STRING_KEY_FQDN = "fqdn";
    private static final int INVALID = -1;
    private static final String TAG = "HwWifi2SupplicantStaNetworkHal";
    private ArrayList<Byte> mEapAnonymousIdentity;
    private final String mIfaceName;
    private final Object mLock = new Object();
    private ISupplicantStaNetwork mSupplicantStaNetwork;
    private ISupplicantStaNetworkCallback mSupplicantStaNetworkCallback;
    private HwWifi2Monitor mWifi2Monitor = null;

    public class SupplicantStaNetworkHalCallback extends ISupplicantStaNetworkCallback.Stub {
        private final int mFramewokNetworkId;
        private final String mSsid;

        SupplicantStaNetworkHalCallback(int framewokNetworkId, String ssid) {
            this.mFramewokNetworkId = framewokNetworkId;
            this.mSsid = ssid;
        }

        public void onNetworkEapSimGsmAuthRequest(ISupplicantStaNetworkCallback.NetworkRequestEapSimGsmAuthParams params) {
        }

        public void onNetworkEapSimUmtsAuthRequest(ISupplicantStaNetworkCallback.NetworkRequestEapSimUmtsAuthParams params) {
        }

        public void onNetworkEapIdentityRequest() {
            HwHiLog.e(HwWifi2SupplicantStaNetworkHal.TAG, false, "onNetworkEapIdentityRequest enter", new Object[0]);
        }

        public void onNetworkEapNotificationErrorCode(int errorcode) {
            synchronized (HwWifi2SupplicantStaNetworkHal.this.mLock) {
                HwHiLog.e(HwWifi2SupplicantStaNetworkHal.TAG, false, "onNetworkEapNotificationErrorCode errorcode = %{public}d", new Object[]{Integer.valueOf(errorcode)});
            }
        }

        public void notifyStaNetworkIfaceEvent(int notifyType, String stringData) {
            HwHiLog.d(HwWifi2SupplicantStaNetworkHal.TAG, false, "notifyStaNetworkIfaceEvent notifyType = %{public}d", new Object[]{Integer.valueOf(notifyType)});
        }
    }

    HwWifi2SupplicantStaNetworkHal(ISupplicantStaNetwork supplicantStaNetwork, String ifaceName, Context context, HwWifi2Monitor monitor) {
        this.mSupplicantStaNetwork = supplicantStaNetwork;
        this.mIfaceName = ifaceName;
        this.mWifi2Monitor = monitor;
    }

    public boolean select() {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("select")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.select(), "select");
            } catch (RemoteException e) {
                handleRemoteException("select");
                return false;
            }
        }
    }

    public boolean saveWifiConfiguration(WifiConfiguration config) {
        synchronized (this.mLock) {
            boolean z = false;
            if (config == null) {
                return false;
            }
            if (!saveWifiConfigurationInternalStep1(config)) {
                return false;
            }
            if (!saveWifiConfigurationInternalStep2(config)) {
                return false;
            }
            try {
                if (config.updateIdentifier != null && !setUpdateIdentifier(Integer.parseInt(config.updateIdentifier))) {
                    HwHiLog.e(TAG, false, "failed to set update identifier", new Object[0]);
                    return false;
                } else if (config.enterpriseConfig != null && config.enterpriseConfig.getEapMethod() != -1 && !saveWifiEnterpriseConfig(config.SSID, config.enterpriseConfig)) {
                    return false;
                } else {
                    this.mSupplicantStaNetworkCallback = trySetupNetworkHalForVendorV3Point0(config);
                    if (this.mSupplicantStaNetworkCallback != null) {
                        z = true;
                    }
                    return z;
                }
            } catch (NumberFormatException e) {
                HwHiLog.e(TAG, false, "Throws an exception when setting the update identifier", new Object[0]);
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
                HwHiLog.e(TAG, false, "Illegal argument %{public}s", new Object[]{StringUtilEx.safeDisplayBssid(bssidStr)});
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return bssid;
    }

    private boolean setBssid(byte[] bssid) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setBssid")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setBssid(bssid), "setBssid");
            } catch (RemoteException e) {
                handleRemoteException("setBssid");
                return false;
            }
        }
    }

    private boolean checkSupplicantStaNetworkAndLogFailure(String methodStr) {
        synchronized (this.mLock) {
            if (this.mSupplicantStaNetwork != null) {
                return true;
            }
            HwHiLog.e(TAG, false, "Can't call %{public}s, ISupplicantStaNetwork is null", new Object[]{methodStr});
            return false;
        }
    }

    private boolean checkStatusAndLogFailure(SupplicantStatus status, String methodStr) {
        synchronized (this.mLock) {
            if (status.code == 0) {
                return true;
            }
            HwHiLog.e(TAG, false, "%{public}s failed: %{public}s", new Object[]{methodStr, status});
            return false;
        }
    }

    private void handleRemoteException(String methodStr) {
        synchronized (this.mLock) {
            this.mSupplicantStaNetwork = null;
            HwHiLog.e(TAG, false, "ISupplicantStaNetwork. %{public}s failed with exception", new Object[]{methodStr});
        }
    }

    private boolean setSsid(ArrayList<Byte> ssid) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setSsid")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setSsid(ssid), "setSsid");
            } catch (RemoteException e) {
                handleRemoteException("setSsid");
                return false;
            }
        }
    }

    private boolean setWapiPskKeyType(int type) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setWAPIPskKeyType")) {
                return false;
            }
            try {
                vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaNetwork iface = getVendorSupplicantStaNetworkV3Point0();
                if (iface == null) {
                    return false;
                }
                return checkStatusAndLogFailure(iface.setWAPIPskKeyType(type), "setWAPIPskKeyType");
            } catch (RemoteException e) {
                handleRemoteException("setWAPIPskKeyType");
                return false;
            }
        }
    }

    private boolean setWapiAsCertPath(String path) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setWapiAsCertPath")) {
                return false;
            }
            try {
                vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaNetwork iface = getVendorSupplicantStaNetworkV3Point0();
                if (iface == null) {
                    return false;
                }
                return checkStatusAndLogFailure(iface.setWAPIASCert(path), "setWapiAsCertPath");
            } catch (RemoteException e) {
                handleRemoteException("setWapiAsCertPath");
                return false;
            }
        }
    }

    private boolean setWapiUserCertPath(String path) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setWapiUserCertPath")) {
                return false;
            }
            try {
                vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaNetwork iface = getVendorSupplicantStaNetworkV3Point0();
                if (iface == null) {
                    return false;
                }
                return checkStatusAndLogFailure(iface.setWAPIASUECert(path), "setWapiUserCertPath");
            } catch (RemoteException e) {
                handleRemoteException("setWapiUserCertPath");
                return false;
            }
        }
    }

    private boolean setSaePassword(String saePassword) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setSaePassword")) {
                return false;
            }
            try {
                android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork supplicantStaNetworkV12 = getV1Point2StaNetwork();
                if (supplicantStaNetworkV12 == null) {
                    return false;
                }
                return checkStatusAndLogFailure(supplicantStaNetworkV12.setSaePassword(saePassword), "setSaePassword");
            } catch (RemoteException e) {
                handleRemoteException("setSaePassword");
                return false;
            }
        }
    }

    private boolean setPskPassphrase(String psk) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setPskPassphrase")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setPskPassphrase(psk), "setPskPassphrase");
            } catch (RemoteException e) {
                handleRemoteException("setPskPassphrase");
                return false;
            }
        }
    }

    private boolean setPsk(byte[] psk) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setPsk")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setPsk(psk), "setPsk");
            } catch (RemoteException e) {
                handleRemoteException("setPsk");
                return false;
            }
        }
    }

    private boolean setWepKey(int keyIdx, ArrayList<Byte> wepKey) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setWepKey")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setWepKey(keyIdx, wepKey), "setWepKey");
            } catch (RemoteException e) {
                handleRemoteException("setWepKey");
                return false;
            }
        }
    }

    private boolean setWepTxKeyIdx(int keyIdx) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setWepTxKeyIdx")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setWepTxKeyIdx(keyIdx), "setWepTxKeyIdx");
            } catch (RemoteException e) {
                handleRemoteException("setWepTxKeyIdx");
                return false;
            }
        }
    }

    private boolean setScanSsid(boolean isEnable) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setScanSsid")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setScanSsid(isEnable), "setScanSsid");
            } catch (RemoteException e) {
                handleRemoteException("setScanSsid");
                return false;
            }
        }
    }

    private boolean setRequirePmf(boolean isEnable) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setRequirePmf")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setRequirePmf(isEnable), "setRequirePmf");
            } catch (RemoteException e) {
                handleRemoteException("setRequirePmf");
                return false;
            }
        }
    }

    private boolean setKeyMgmt(int keyMgmtMask) {
        SupplicantStatus status;
        synchronized (this.mLock) {
            HwHiLog.i(TAG, false, "setKeyMgmt is %{public}d", new Object[]{Integer.valueOf(keyMgmtMask)});
            if (!checkSupplicantStaNetworkAndLogFailure("setKeyMgmt")) {
                return false;
            }
            try {
                android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork supplicantStaNetworkV12 = getV1Point2StaNetwork();
                if (supplicantStaNetworkV12 != null) {
                    status = supplicantStaNetworkV12.setKeyMgmt_1_2(keyMgmtMask);
                } else {
                    status = this.mSupplicantStaNetwork.setKeyMgmt(keyMgmtMask);
                }
                return checkStatusAndLogFailure(status, "setKeyMgmt");
            } catch (RemoteException e) {
                handleRemoteException("setKeyMgmt");
                return false;
            }
        }
    }

    private boolean setProto(int protoMask) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setProto")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setProto(protoMask), "setProto");
            } catch (RemoteException e) {
                handleRemoteException("setProto");
                return false;
            }
        }
    }

    private boolean setAuthAlg(int authAlgMask) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setAuthAlg")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setAuthAlg(authAlgMask), "setAuthAlg");
            } catch (RemoteException e) {
                handleRemoteException("setAuthAlg");
                return false;
            }
        }
    }

    private boolean setPairwiseCipher(int pairwiseCipherMask) {
        SupplicantStatus status;
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setPairwiseCipher")) {
                return false;
            }
            try {
                android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork supplicantStaNetworkV12 = getV1Point2StaNetwork();
                if (supplicantStaNetworkV12 != null) {
                    status = supplicantStaNetworkV12.setPairwiseCipher_1_2(pairwiseCipherMask);
                } else {
                    status = this.mSupplicantStaNetwork.setPairwiseCipher(pairwiseCipherMask);
                }
                return checkStatusAndLogFailure(status, "setPairwiseCipher");
            } catch (RemoteException e) {
                handleRemoteException("setPairwiseCipher");
                return false;
            }
        }
    }

    private boolean setGroupMgmtCipher(int groupMgmtCipherMask) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setGroupMgmtCipher")) {
                return false;
            }
            try {
                android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork supplicantStaNetworkV12 = getV1Point2StaNetwork();
                if (supplicantStaNetworkV12 == null) {
                    return false;
                }
                return checkStatusAndLogFailure(supplicantStaNetworkV12.setGroupMgmtCipher(groupMgmtCipherMask), "setGroupMgmtCipher");
            } catch (RemoteException e) {
                handleRemoteException("setGroupMgmtCipher");
                return false;
            }
        }
    }

    private boolean enableTlsSuiteEapPhase1Param(boolean isEnable) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapPhase1Params")) {
                return false;
            }
            try {
                android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork supplicantStaNetworkV12 = getV1Point2StaNetwork();
                if (supplicantStaNetworkV12 != null) {
                    return checkStatusAndLogFailure(supplicantStaNetworkV12.enableTlsSuiteBEapPhase1Param(isEnable), "setEapPhase1Params");
                }
                HwHiLog.e(TAG, false, "Supplicant HAL version does not support %{public}s", new Object[]{"setEapPhase1Params"});
                return false;
            } catch (RemoteException e) {
                handleRemoteException("setEapPhase1Params");
                return false;
            }
        }
    }

    private boolean enableSuiteEapOpenSslCiphers() {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapOpenSslCiphers")) {
                return false;
            }
            try {
                android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork supplicantStaNetworkV12 = getV1Point2StaNetwork();
                if (supplicantStaNetworkV12 != null) {
                    return checkStatusAndLogFailure(supplicantStaNetworkV12.enableSuiteBEapOpenSslCiphers(), "setEapOpenSslCiphers");
                }
                HwHiLog.e(TAG, false, "Supplicant HAL version does not support %{public}s", new Object[]{"setEapOpenSslCiphers"});
                return false;
            } catch (RemoteException e) {
                handleRemoteException("setEapOpenSslCiphers");
                return false;
            }
        }
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
                HwHiLog.e(TAG, false, "invalid eap method value from WifiConfiguration: %{public}d", new Object[]{Integer.valueOf(value)});
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
                HwHiLog.e(TAG, false, "invalid eap phase2 method value from WifiConfiguration: %{public}d", new Object[]{Integer.valueOf(value)});
                return -1;
        }
    }

    private boolean setEapMethod(int method) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapMethod")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setEapMethod(method), "setEapMethod");
            } catch (RemoteException e) {
                handleRemoteException("setEapMethod");
                return false;
            }
        }
    }

    private boolean setEapPhase2Method(int method) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapPhase2Method")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setEapPhase2Method(method), "setEapPhase2Method");
            } catch (RemoteException e) {
                handleRemoteException("setEapPhase2Method");
                return false;
            }
        }
    }

    private boolean setEapIdentity(ArrayList<Byte> identity) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapIdentity")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setEapIdentity(identity), "setEapIdentity");
            } catch (RemoteException e) {
                handleRemoteException("setEapIdentity");
                return false;
            }
        }
    }

    private boolean setEapAnonymousIdentity(ArrayList<Byte> identity) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapAnonymousIdentity")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setEapAnonymousIdentity(identity), "setEapAnonymousIdentity");
            } catch (RemoteException e) {
                handleRemoteException("setEapAnonymousIdentity");
                return false;
            }
        }
    }

    private boolean getEapAnonymousIdentity() {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("getEapAnonymousIdentity")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                this.mSupplicantStaNetwork.getEapAnonymousIdentity(new ISupplicantStaNetwork.getEapAnonymousIdentityCallback(statusOk) {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2SupplicantStaNetworkHal$eOqcwinI3yzD_r4zOo4HJVE5z_g */
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        HwWifi2SupplicantStaNetworkHal.this.lambda$getEapAnonymousIdentity$0$HwWifi2SupplicantStaNetworkHal(this.f$1, supplicantStatus, arrayList);
                    }
                });
                return statusOk.value;
            } catch (RemoteException e) {
                handleRemoteException("getEapAnonymousIdentity");
                return false;
            }
        }
    }

    public /* synthetic */ void lambda$getEapAnonymousIdentity$0$HwWifi2SupplicantStaNetworkHal(MutableBoolean statusOk, SupplicantStatus status, ArrayList identityValue) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            this.mEapAnonymousIdentity = identityValue;
        } else {
            checkStatusAndLogFailure(status, "getEapAnonymousIdentity");
        }
    }

    private boolean setEapPassword(ArrayList<Byte> password) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapPassword")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setEapPassword(password), "setEapPassword");
            } catch (RemoteException e) {
                handleRemoteException("setEapPassword");
                return false;
            }
        }
    }

    private boolean setEapCaCert(String path) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapCACert")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setEapCACert(path), "setEapCACert");
            } catch (RemoteException e) {
                handleRemoteException("setEapCACert");
                return false;
            }
        }
    }

    private boolean setEapCaPath(String path) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapCAPath")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setEapCAPath(path), "setEapCAPath");
            } catch (RemoteException e) {
                handleRemoteException("setEapCAPath");
                return false;
            }
        }
    }

    private boolean setEapClientCert(String path) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapClientCert")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setEapClientCert(path), "setEapClientCert");
            } catch (RemoteException e) {
                handleRemoteException("setEapClientCert");
                return false;
            }
        }
    }

    private boolean setEapPrivateKeyId(String id) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapPrivateKeyId")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setEapPrivateKeyId(id), "setEapPrivateKeyId");
            } catch (RemoteException e) {
                handleRemoteException("setEapPrivateKeyId");
                return false;
            }
        }
    }

    private boolean setEapSubjectMatch(String match) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapSubjectMatch")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setEapSubjectMatch(match), "setEapSubjectMatch");
            } catch (RemoteException e) {
                handleRemoteException("setEapSubjectMatch");
                return false;
            }
        }
    }

    private boolean setEapAltSubjectMatch(String match) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapAltSubjectMatch")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setEapAltSubjectMatch(match), "setEapAltSubjectMatch");
            } catch (RemoteException e) {
                handleRemoteException("setEapAltSubjectMatch");
                return false;
            }
        }
    }

    private boolean setEapEngine(boolean isEnable) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapEngine")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setEapEngine(isEnable), "setEapEngine");
            } catch (RemoteException e) {
                handleRemoteException("setEapEngine");
                return false;
            }
        }
    }

    private boolean setEapEngineId(String id) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapEngineID")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setEapEngineID(id), "setEapEngineID");
            } catch (RemoteException e) {
                handleRemoteException("setEapEngineID");
                return false;
            }
        }
    }

    private boolean setEapDomainSuffixMatch(String match) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapDomainSuffixMatch")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setEapDomainSuffixMatch(match), "setEapDomainSuffixMatch");
            } catch (RemoteException e) {
                handleRemoteException("setEapDomainSuffixMatch");
                return false;
            }
        }
    }

    private boolean setEapProactiveKeyCaching(boolean isEnable) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setEapProactiveKeyCaching")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setProactiveKeyCaching(isEnable), "setEapProactiveKeyCaching");
            } catch (RemoteException e) {
                handleRemoteException("setEapProactiveKeyCaching");
                return false;
            }
        }
    }

    private android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork getV1Point2StaNetwork() {
        android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork supplicantStaNetworkForV1Point2Mockable;
        synchronized (this.mLock) {
            supplicantStaNetworkForV1Point2Mockable = getSupplicantStaNetworkForV1Point2Mockable();
        }
        return supplicantStaNetworkForV1Point2Mockable;
    }

    private android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork getSupplicantStaNetworkForV1Point2Mockable() {
        ISupplicantStaNetwork iSupplicantStaNetwork = this.mSupplicantStaNetwork;
        if (iSupplicantStaNetwork == null) {
            return null;
        }
        return android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.castFrom(iSupplicantStaNetwork);
    }

    private BitSet addSha256KeyMgmtFlags(BitSet keyManagementFlags) {
        synchronized (this.mLock) {
            BitSet modifiedFlags = null;
            Object obj = keyManagementFlags.clone();
            if (obj instanceof BitSet) {
                modifiedFlags = (BitSet) obj;
                if (getV1Point2StaNetwork() == null) {
                    return modifiedFlags;
                }
                if (keyManagementFlags.get(1)) {
                    modifiedFlags.set(11);
                }
                if (keyManagementFlags.get(2)) {
                    modifiedFlags.set(12);
                }
            }
            return modifiedFlags;
        }
    }

    private static int wifiConfigurationToSupplicantKeyMgmtMaskInternal(BitSet keyMgmt, int bit) {
        switch (bit) {
            case 0:
                return 0 | 4;
            case 1:
                return 0 | 2;
            case 2:
                return 0 | 1;
            case 3:
                return 0 | 8;
            case 4:
            case HwWifi2CondControl.HW_SIGNAL_POLL_PARA_LENGTH /* 13 */:
            case 14:
            case 15:
            default:
                throw new IllegalArgumentException("Invalid protoMask bit in keyMgmt: " + bit);
            case 5:
                return 0 | 32768;
            case 6:
                return 0 | 64;
            case 7:
                return 0 | 32;
            case 8:
                return 0 | 1024;
            case 9:
                return 0 | 4194304;
            case 10:
                return 0 | HwWifi2ClientModeImplConst.BASE;
            case 11:
                return 0 | 256;
            case 12:
                return 0 | 128;
            case 16:
                return 0 | 4096;
            case 17:
                return 0 | 8192;
        }
    }

    private static int wifiConfigurationToSupplicantKeyMgmtMask(BitSet keyMgmt) {
        int mask = 0;
        int bit = keyMgmt.nextSetBit(0);
        while (bit != -1) {
            mask |= wifiConfigurationToSupplicantKeyMgmtMaskInternal(keyMgmt, bit);
            bit = keyMgmt.nextSetBit(bit + 1);
        }
        return mask;
    }

    private boolean setGroupCipher(int groupCipherMask) {
        SupplicantStatus status;
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setGroupCipher")) {
                return false;
            }
            try {
                android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork supplicantStaNetworkV12 = getV1Point2StaNetwork();
                if (supplicantStaNetworkV12 != null) {
                    status = supplicantStaNetworkV12.setGroupCipher_1_2(groupCipherMask);
                } else {
                    status = this.mSupplicantStaNetwork.setGroupCipher(groupCipherMask);
                }
                return checkStatusAndLogFailure(status, "setGroupCipher");
            } catch (RemoteException e) {
                handleRemoteException("setGroupCipher");
                return false;
            }
        }
    }

    private boolean setIdStr(String idString) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setIdStr")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setIdStr(idString), "setIdStr");
            } catch (RemoteException e) {
                handleRemoteException("setIdStr");
                return false;
            }
        }
    }

    private boolean saveSuiteBconfig(WifiConfiguration config) {
        if (config.allowedGroupCiphers.cardinality() != 0 && !setGroupCipher(wifiConfigurationToSupplicantGroupCipherMask(config.allowedGroupCiphers))) {
            HwHiLog.e(TAG, false, "failed to set Group Cipher", new Object[0]);
            return false;
        } else if (config.allowedPairwiseCiphers.cardinality() != 0 && !setPairwiseCipher(wifiConfigurationToSupplicantPairwiseCipherMask(config.allowedPairwiseCiphers))) {
            HwHiLog.e(TAG, false, "failed to set PairwiseCipher", new Object[0]);
            return false;
        } else if (config.allowedGroupManagementCiphers.cardinality() == 0 || setGroupMgmtCipher(wifiConfigurationToSupplicantGroupMgmtCipherMask(config.allowedGroupManagementCiphers))) {
            if (config.allowedSuiteBCiphers.get(1)) {
                if (!enableTlsSuiteEapPhase1Param(true)) {
                    HwHiLog.e(TAG, false, "failed to set TLSSuiteB", new Object[0]);
                    return false;
                }
            } else if (!config.allowedSuiteBCiphers.get(0)) {
                HwHiLog.e(TAG, false, "unkown allowedSuiteBCiphers", new Object[0]);
            } else if (!enableSuiteEapOpenSslCiphers()) {
                HwHiLog.e(TAG, false, "failed to set OpensslCipher", new Object[0]);
                return false;
            }
            return true;
        } else {
            HwHiLog.e(TAG, false, "failed to set GroupMgmtCipher", new Object[0]);
            return false;
        }
    }

    private static int wifiConfigurationToSupplicantProtoMask(BitSet protoMask) {
        int mask = 0;
        int bit = protoMask.nextSetBit(0);
        while (bit != -1) {
            if (bit == 0) {
                mask |= 1;
            } else if (bit == 1) {
                mask |= 2;
            } else if (bit == 2) {
                mask |= 8;
            } else {
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
            if (bit == 0) {
                mask |= 1;
            } else if (bit == 1) {
                mask |= 2;
            } else if (bit == 2) {
                mask |= 4;
            } else {
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
            if (bit == 0) {
                mask |= 2;
            } else if (bit == 1) {
                mask |= 4;
            } else if (bit == 2) {
                mask |= 8;
            } else if (bit == 3) {
                mask |= 16;
            } else if (bit == 4) {
                mask |= 16384;
            } else if (bit == 5) {
                mask |= 256;
            } else {
                throw new IllegalArgumentException("Invalid GroupCipherMask bit in wificonfig: " + bit);
            }
            bit = groupCipherMask.nextSetBit(bit + 1);
        }
        return mask;
    }

    private static int wifiConfigurationToSupplicantGroupMgmtCipherMask(BitSet groupMgmtCipherMask) {
        int mask = 0;
        int bit = groupMgmtCipherMask.nextSetBit(0);
        while (bit != -1) {
            if (bit == 0) {
                mask |= 8192;
            } else if (bit == 1) {
                mask |= 2048;
            } else if (bit == 2) {
                mask |= 4096;
            } else {
                throw new IllegalArgumentException("Invalid GroupMgmtCipherMask bit in wificonfig: " + bit);
            }
            bit = groupMgmtCipherMask.nextSetBit(bit + 1);
        }
        return mask;
    }

    private static int wifiConfigurationToSupplicantPairwiseCipherMask(BitSet pairwiseCipherMask) {
        int mask = 0;
        int bit = pairwiseCipherMask.nextSetBit(0);
        while (bit != -1) {
            if (bit == 0) {
                mask |= 1;
            } else if (bit == 1) {
                mask |= 8;
            } else if (bit == 2) {
                mask |= 16;
            } else if (bit == 3) {
                mask |= 256;
            } else {
                throw new IllegalArgumentException("Invalid pairwiseCipherMask bit in wificonfig: " + bit);
            }
            bit = pairwiseCipherMask.nextSetBit(bit + 1);
        }
        return mask;
    }

    private boolean isAuthAlgNeeded(WifiConfiguration config) {
        if (!config.allowedKeyManagement.get(8)) {
            return true;
        }
        HwHiLog.d(TAG, false, "No need to set Auth Algorithm for SAE", new Object[0]);
        return false;
    }

    private static String createNetworkExtra(Map<String, String> values) {
        try {
            return URLEncoder.encode(new JSONObject(values).toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            HwHiLog.i(TAG, false, "Unable to serialize networkExtra", new Object[0]);
            return null;
        }
    }

    private boolean setUpdateIdentifier(int identifier) {
        synchronized (this.mLock) {
            if (!checkSupplicantStaNetworkAndLogFailure("setUpdateIdentifier")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicantStaNetwork.setUpdateIdentifier(identifier), "setUpdateIdentifier");
            } catch (RemoteException e) {
                handleRemoteException("setUpdateIdentifier");
                return false;
            }
        }
    }

    private boolean saveWifiConfigurationInternalStep1(WifiConfiguration config) {
        if (!saveSsidConfiguration(config) || !saveBssidConfiguration(config) || !saveSharedKeyConfiguration(config) || !saveWepKeyConfiguration(config)) {
            return false;
        }
        if (!setScanSsid(config.hiddenSSID)) {
            HwHiLog.e(TAG, false, "%{public}s: failed to set hiddenSSID: %{public}b", new Object[]{StringUtilEx.safeDisplaySsid(config.SSID), Boolean.valueOf(config.hiddenSSID)});
            return false;
        } else if (saveRequirePmfConfiguration(config) && saveKeyManagementConfiguration(config)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean saveWifiConfigurationInternalStep2(WifiConfiguration config) {
        if (config.allowedProtocols.cardinality() != 0 && !setProto(wifiConfigurationToSupplicantProtoMask(config.allowedProtocols))) {
            HwHiLog.e(TAG, false, "failed to set Security Protocol", new Object[0]);
            return false;
        } else if (config.allowedAuthAlgorithms.cardinality() != 0 && isAuthAlgNeeded(config) && !setAuthAlg(wifiConfigurationToSupplicantAuthAlgMask(config.allowedAuthAlgorithms))) {
            HwHiLog.e(TAG, false, "failed to set AuthAlgorithm", new Object[0]);
            return false;
        } else if (config.allowedGroupCiphers.cardinality() != 0 && !setGroupCipher(wifiConfigurationToSupplicantGroupCipherMask(config.allowedGroupCiphers))) {
            HwHiLog.e(TAG, false, "failed to set Group Cipher", new Object[0]);
            return false;
        } else if (config.allowedPairwiseCiphers.cardinality() == 0 || setPairwiseCipher(wifiConfigurationToSupplicantPairwiseCipherMask(config.allowedPairwiseCiphers))) {
            Map<String, String> metadata = new HashMap<>();
            if (config.isPasspoint()) {
                metadata.put(ID_STRING_KEY_FQDN, config.FQDN);
            }
            metadata.put(ID_STRING_KEY_CONFIG_KEY, config.configKey());
            metadata.put(ID_STRING_KEY_CREATOR_UID, Integer.toString(config.creatorUid));
            if (setIdStr(createNetworkExtra(metadata))) {
                return true;
            }
            HwHiLog.e(TAG, false, "failed to set id string", new Object[0]);
            return false;
        } else {
            HwHiLog.e(TAG, false, "failed to set PairwiseCipher", new Object[0]);
            return false;
        }
    }

    private boolean saveWifiEnterpriseConfigInternalStep1(String ssid, WifiEnterpriseConfig eapConfig) {
        if (!setEapMethod(wifiConfigurationToSupplicantEapMethod(eapConfig.getEapMethod()))) {
            HwHiLog.e(TAG, false, "%{public}s: failed to set eap method: %{public}d", new Object[]{StringUtilEx.safeDisplaySsid(ssid), Integer.valueOf(eapConfig.getEapMethod())});
            return false;
        } else if (!setEapPhase2Method(wifiConfigurationToSupplicantEapPhase2Method(eapConfig.getPhase2Method()))) {
            HwHiLog.e(TAG, false, "%{public}s: failed to set eap phase 2 method: %{public}d", new Object[]{StringUtilEx.safeDisplaySsid(ssid), Integer.valueOf(eapConfig.getPhase2Method())});
            return false;
        } else {
            String eapParam = eapConfig.getFieldValue("identity");
            if (TextUtils.isEmpty(eapParam) || setEapIdentity(NativeUtil.stringToByteArrayList(eapParam))) {
                String eapParam2 = eapConfig.getFieldValue("anonymous_identity");
                if (TextUtils.isEmpty(eapParam2) || setEapAnonymousIdentity(NativeUtil.stringToByteArrayList(eapParam2))) {
                    String eapParam3 = eapConfig.getFieldValue("password");
                    if (TextUtils.isEmpty(eapParam3) || setEapPassword(NativeUtil.stringToByteArrayList(eapParam3))) {
                        String eapParam4 = eapConfig.getFieldValue("client_cert");
                        if (TextUtils.isEmpty(eapParam4) || setEapClientCert(eapParam4)) {
                            return true;
                        }
                        HwHiLog.e(TAG, false, "%{public}s: failed to set eap client cert: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid), eapParam4});
                        return false;
                    }
                    HwHiLog.e(TAG, false, "%{public}s: failed to set eap password", new Object[]{StringUtilEx.safeDisplaySsid(ssid)});
                    return false;
                }
                HwHiLog.e(TAG, false, "%{public}s: failed to set eap anonymous identity: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid), eapParam2});
                return false;
            }
            HwHiLog.e(TAG, false, "%{public}s: failed to set eap identity: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid), eapParam});
            return false;
        }
    }

    private boolean saveWifiEnterpriseConfigInternalStep2(String ssid, WifiEnterpriseConfig eapConfig) {
        eapConfig.getFieldValue("identity");
        String eapParam = eapConfig.getFieldValue("ca_cert");
        if (TextUtils.isEmpty(eapParam) || setEapCaCert(eapParam)) {
            String eapParam2 = eapConfig.getFieldValue("subject_match");
            if (TextUtils.isEmpty(eapParam2) || setEapSubjectMatch(eapParam2)) {
                String eapParam3 = eapConfig.getFieldValue("engine_id");
                if (TextUtils.isEmpty(eapParam3) || setEapEngineId(eapParam3)) {
                    String eapParam4 = eapConfig.getFieldValue("engine");
                    if (TextUtils.isEmpty(eapParam4) || setEapEngine(eapParam4.equals("1"))) {
                        String eapParam5 = eapConfig.getFieldValue("key_id");
                        if (TextUtils.isEmpty(eapParam5) || setEapPrivateKeyId(eapParam5)) {
                            String eapParam6 = eapConfig.getFieldValue("altsubject_match");
                            if (TextUtils.isEmpty(eapParam6) || setEapAltSubjectMatch(eapParam6)) {
                                return true;
                            }
                            HwHiLog.e(TAG, false, "%{public}s: failed to set eap alt subject match: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid), eapParam6});
                            return false;
                        }
                        HwHiLog.e(TAG, false, "%{public}s: failed to set eap private key: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid), eapParam5});
                        return false;
                    }
                    HwHiLog.e(TAG, false, "%{public}s: failed to set eap engine: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid), eapParam4});
                    return false;
                }
                HwHiLog.e(TAG, false, "%{public}s: failed to set eap engine id: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid), eapParam3});
                return false;
            }
            HwHiLog.e(TAG, false, "%{public}s: failed to set eap subject match: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid), eapParam2});
            return false;
        }
        HwHiLog.e(TAG, false, "%{public}s: failed to set eap ca cert: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid), eapParam});
        return false;
    }

    private boolean saveWifiEnterpriseConfigInternalStep3(String ssid, WifiEnterpriseConfig eapConfig) {
        eapConfig.getFieldValue("identity");
        String eapParam = eapConfig.getFieldValue("domain_suffix_match");
        if (TextUtils.isEmpty(eapParam) || setEapDomainSuffixMatch(eapParam)) {
            String eapParam2 = eapConfig.getFieldValue("ca_path");
            if (TextUtils.isEmpty(eapParam2) || setEapCaPath(eapParam2)) {
                String eapParam3 = eapConfig.getFieldValue("proactive_key_caching");
                if (TextUtils.isEmpty(eapParam3) || setEapProactiveKeyCaching("1".equals(eapParam3))) {
                    return true;
                }
                HwHiLog.e(TAG, false, "%{public}s: failed to set proactive key caching: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid), eapParam3});
                return false;
            }
            HwHiLog.e(TAG, false, "%{public}s: failed to set eap ca path: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid), eapParam2});
            return false;
        }
        HwHiLog.e(TAG, false, "%{public}s: failed to set eap domain suffix match: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid), eapParam});
        return false;
    }

    private boolean saveWifiEnterpriseConfig(String ssid, WifiEnterpriseConfig eapConfig) {
        synchronized (this.mLock) {
            if (eapConfig == null) {
                return false;
            }
            if (!saveWifiEnterpriseConfigInternalStep1(ssid, eapConfig)) {
                return false;
            }
            if (!saveWifiEnterpriseConfigInternalStep2(ssid, eapConfig)) {
                return false;
            }
            if (!saveWifiEnterpriseConfigInternalStep3(ssid, eapConfig)) {
                return false;
            }
            return true;
        }
    }

    private android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetworkCallback trySetupNetworkHalForVendorV3Point0(WifiConfiguration config) {
        if (!isVendorSupplicantStaNetworkV3Point0()) {
            HwHiLog.i(TAG, false, "just support v3.0", new Object[0]);
            return null;
        }
        HwHiLog.i(TAG, false, "Start to setup vendor mSupplicantStaNetworkCallback", new Object[0]);
        if (!setWapiConfiguration(config)) {
            return null;
        }
        vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaNetworkCallback callback = new SupplicantStaNetworkHalCallback(config.networkId, config.SSID);
        if (!hwStaNetworkRegisterCallback(callback)) {
            HwHiLog.e(TAG, false, "Failed to register callback", new Object[0]);
            return null;
        }
        HwHiLog.d(TAG, false, "Successfully setup vendor mSupplicantStaNetworkCallback.", new Object[0]);
        return callback;
    }

    private boolean setWapiConfiguration(WifiConfiguration config) {
        if (config.wapiPskTypeBcm != -1 && !setWapiPskKeyType(config.wapiPskTypeBcm)) {
            HwHiLog.e(TAG, false, "%{public}s: failed to set wapi psk key type: %{public}d", new Object[]{StringUtilEx.safeDisplaySsid(config.SSID), Integer.valueOf(config.wapiPskTypeBcm)});
            return false;
        } else if (!TextUtils.isEmpty(config.wapiAsCertBcm) && !setWapiAsCertPath(config.wapiAsCertBcm)) {
            HwHiLog.e(TAG, false, "%{public}s: failed to set wapi as cert path: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.SSID), config.wapiAsCertBcm});
            return false;
        } else if (TextUtils.isEmpty(config.wapiUserCertBcm) || setWapiUserCertPath(config.wapiUserCertBcm)) {
            return true;
        } else {
            HwHiLog.e(TAG, false, "%{public}s: failed to set wapi user cert path: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.SSID), config.wapiUserCertBcm});
            return false;
        }
    }

    private boolean hwStaNetworkRegisterCallback(vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaNetworkCallback callback) {
        synchronized (this.mLock) {
            HwHiLog.d(TAG, false, "Start to register network callback for vendor ISupplicantStaNetwork", new Object[0]);
            if (!checkSupplicantStaNetworkAndLogFailure("hwStaNetworkRegisterCallback")) {
                return false;
            }
            try {
                vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaNetwork iface = getVendorSupplicantStaNetworkV3Point0();
                if (iface == null) {
                    return false;
                }
                return checkStatusAndLogFailure(iface.hwStaNetworkRegisterCallback(callback), "hwStaNetworkRegisterCallback");
            } catch (RemoteException e) {
                handleRemoteException("hwStaNetworkRegisterCallback");
                return false;
            }
        }
    }

    private boolean saveSsidConfiguration(WifiConfiguration config) {
        if (config.SSID != null) {
            ArrayList<Byte> ssid = ScanResultRecords.getDefault().getOriSsid(config.getNetworkSelectionStatus().getNetworkSelectionBSSID(), config.SSID);
            HwHiLog.i(TAG, false, "ssid=%{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.SSID)});
            if (ssid == null) {
                try {
                    if (!TextUtils.isEmpty(config.oriSsid)) {
                        ssid = NativeUtil.byteArrayToArrayList(NativeUtil.hexStringToByteArray(config.oriSsid));
                    } else {
                        ssid = NativeUtil.decodeSsid(config.SSID);
                    }
                } catch (IllegalArgumentException e) {
                    HwHiLog.e(TAG, false, "saveWifiConfiguration: cannot be utf-8 encoded", new Object[0]);
                    return false;
                }
            }
            if (!setSsid(ssid)) {
                HwHiLog.e(TAG, false, "failed to set SSID: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.SSID)});
                return false;
            }
        }
        return true;
    }

    private boolean saveSharedKeyConfiguration(WifiConfiguration config) {
        if (config.preSharedKey == null) {
            return true;
        }
        if (config.preSharedKey.isEmpty()) {
            HwHiLog.e(TAG, false, "psk is empty", new Object[0]);
            return false;
        }
        if (config.preSharedKey.startsWith("\"")) {
            if (config.allowedKeyManagement.get(8)) {
                if (!setSaePassword(NativeUtil.removeEnclosingQuotes(config.preSharedKey))) {
                    HwHiLog.e(TAG, false, "failed to set sae password", new Object[0]);
                    return false;
                }
            } else if (!setPskPassphrase(NativeUtil.removeEnclosingQuotes(config.preSharedKey))) {
                HwHiLog.e(TAG, false, "failed to set psk passphrase", new Object[0]);
                return false;
            }
        } else if (config.allowedKeyManagement.get(8)) {
            HwHiLog.e(TAG, false, "failed to set SAE", new Object[0]);
            return false;
        } else if (!setPsk(NativeUtil.hexStringToByteArray(config.preSharedKey))) {
            HwHiLog.e(TAG, false, "failed to set psk", new Object[0]);
            return false;
        }
        return true;
    }

    private boolean saveBssidConfiguration(WifiConfiguration config) {
        String bssidStr = config.getNetworkSelectionStatus().getNetworkSelectionBSSID();
        HwHiLog.e(TAG, false, "saveWifiConfiguration set bssidStr = %{public}s", new Object[]{StringUtilEx.safeDisplayBssid(bssidStr)});
        if (bssidStr == null || setBssid(NativeUtil.macAddressToByteArray(bssidStr))) {
            return true;
        }
        HwHiLog.e(TAG, false, "failed to set BSSID: %{public}s", new Object[]{StringUtilEx.safeDisplayBssid(bssidStr)});
        return false;
    }

    private boolean saveWepKeyConfiguration(WifiConfiguration config) {
        boolean hasSetKey = false;
        if (config.wepKeys == null) {
            return true;
        }
        for (int i = 0; i < config.wepKeys.length; i++) {
            if (config.wepKeys[i] != null && config.allowedAuthAlgorithms.get(1)) {
                if (TextUtils.isEmpty(config.wepKeys[i])) {
                    HwHiLog.e(TAG, false, "index %{public}d key is empty", new Object[]{Integer.valueOf(i)});
                } else if (!setWepKey(i, NativeUtil.hexOrQuotedStringToBytes(config.wepKeys[i]))) {
                    HwHiLog.e(TAG, false, "failed to set wep_key index %{public}d", new Object[]{Integer.valueOf(i)});
                    return false;
                } else {
                    hasSetKey = true;
                }
            }
        }
        if (!hasSetKey || setWepTxKeyIdx(config.wepTxKeyIndex)) {
            return true;
        }
        HwHiLog.e(TAG, false, "failed to set wep_tx_keyidx: %{public}d", new Object[]{Integer.valueOf(config.wepTxKeyIndex)});
        return false;
    }

    private boolean saveKeyManagementConfiguration(WifiConfiguration config) {
        if (config.allowedKeyManagement.cardinality() == 0) {
            return true;
        }
        BitSet keyMgmtMask = config.allowedKeyManagement;
        if (getV1Point2StaNetwork() != null) {
            keyMgmtMask = addSha256KeyMgmtFlags(keyMgmtMask);
        }
        if (!setKeyMgmt(wifiConfigurationToSupplicantKeyMgmtMask(keyMgmtMask))) {
            HwHiLog.e(TAG, false, "failed to set Key Management", new Object[0]);
            return false;
        } else if (!keyMgmtMask.get(10) || saveSuiteBconfig(config)) {
            return true;
        } else {
            HwHiLog.e(TAG, false, "Failed to set Suite-B-192 configuration", new Object[0]);
            return false;
        }
    }

    private boolean saveRequirePmfConfiguration(WifiConfiguration config) {
        if ((!config.requirePMF && (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3))) || setRequirePmf(config.requirePMF)) {
            return true;
        }
        HwHiLog.e(TAG, false, "%{public}s: failed to set requirePMF: %{public}b", new Object[]{StringUtilEx.safeDisplaySsid(config.SSID), Boolean.valueOf(config.requirePMF)});
        return false;
    }

    private boolean isVendorSupplicantStaNetworkV3Point0() {
        return getVendorSupplicantStaNetworkV3Point0() != null;
    }

    private vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaNetwork getVendorSupplicantStaNetworkV3Point0() {
        synchronized (this.mLock) {
            if (this.mSupplicantStaNetwork == null) {
                return null;
            }
            return vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaNetwork.castFrom(this.mSupplicantStaNetwork);
        }
    }

    public String fetchEapAnonymousIdentity() {
        synchronized (this.mLock) {
            if (!getEapAnonymousIdentity()) {
                return null;
            }
            return NativeUtil.stringFromByteArrayList(this.mEapAnonymousIdentity);
        }
    }

    private boolean checkiSupplicantStaNetworkAndLogFailure(String methodStr) {
        synchronized (this.mLock) {
            if (this.mSupplicantStaNetwork != null) {
                return true;
            }
            HwHiLog.e(TAG, false, "Can't call %{public}s, ISupplicantStaNetwork is null", new Object[]{methodStr});
            return false;
        }
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
                HwHiLog.e(TAG, false, "Illegal argument identityStr", new Object[0]);
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
            if (!checkiSupplicantStaNetworkAndLogFailure("sendNetworkEapIdentityResponse")) {
                return false;
            }
            try {
                android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork supplicantStaNetworkV11 = getSupplicantStaNetworkForV11Mockable();
                if (supplicantStaNetworkV11 == null || encryptedIdentity == null) {
                    status = this.mSupplicantStaNetwork.sendNetworkEapIdentityResponse(unencryptedIdentity);
                } else {
                    status = supplicantStaNetworkV11.sendNetworkEapIdentityResponse_1_1(unencryptedIdentity, encryptedIdentity);
                }
                return checkStatusAndLogFailure(status, "sendNetworkEapIdentityResponse");
            } catch (RemoteException e) {
                handleRemoteException("sendNetworkEapIdentityResponse");
                return false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork getSupplicantStaNetworkForV11Mockable() {
        ISupplicantStaNetwork iSupplicantStaNetwork = this.mSupplicantStaNetwork;
        if (iSupplicantStaNetwork == null) {
            return null;
        }
        return android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork.castFrom(iSupplicantStaNetwork);
    }
}
