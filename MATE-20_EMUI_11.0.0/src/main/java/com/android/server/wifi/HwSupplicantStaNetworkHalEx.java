package com.android.server.wifi;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetworkCallback;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.net.wifi.WifiConfiguration;
import android.os.HidlSupport;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.SupplicantStaNetworkHal;
import com.android.server.wifi.hwUtil.StringUtilEx;
import vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaNetwork;
import vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaNetworkCallback;

public class HwSupplicantStaNetworkHalEx implements IHwSupplicantStaNetworkHalEx {
    private static final String TAG = "HwSupplicantStaNetworkHalEx";
    private IHwSupplicantStaNetworkHalInner mIHwSupplicantStaNetworkHalInner = null;
    private final String mIfaceName;
    private WifiMonitor mWifiMonitor = null;

    public static HwSupplicantStaNetworkHalEx createHwSupplicantStaNetworkHalEx(IHwSupplicantStaNetworkHalInner hwSupplicantStaNetworkHalInner, WifiMonitor wifiMonitor, String ifaceName) {
        return new HwSupplicantStaNetworkHalEx(hwSupplicantStaNetworkHalInner, wifiMonitor, ifaceName);
    }

    HwSupplicantStaNetworkHalEx(IHwSupplicantStaNetworkHalInner hwSupplicantStaNetworkHalInner, WifiMonitor wifiMonitor, String ifaceName) {
        this.mIHwSupplicantStaNetworkHalInner = hwSupplicantStaNetworkHalInner;
        this.mWifiMonitor = wifiMonitor;
        this.mIfaceName = ifaceName;
    }

    public ISupplicantStaNetworkCallback trySetupNetworkHalForVendorV3_0(WifiConfiguration config) {
        if (!isVendorSupplicantStaNetworkV3_0()) {
            return null;
        }
        HwHiLog.i(TAG, false, "Start to setup vendor mISupplicantStaNetworkCallback", new Object[0]);
        if (!setWapiConfiguration(config)) {
            return null;
        }
        vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaNetworkCallback vendorNetworkCallback = new VendorSupplicantStaNetworkHalCallback(config.networkId, config.SSID);
        if (!hwStaNetworkRegisterCallback(vendorNetworkCallback)) {
            HwHiLog.e(TAG, false, "Failed to register callback", new Object[0]);
            return null;
        }
        HwHiLog.d(TAG, false, "Successfully setup vendor mISupplicantStaNetworkCallback.", new Object[0]);
        return vendorNetworkCallback;
    }

    private boolean isVendorSupplicantStaNetworkV3_0() {
        return getVendorSupplicantStaNetworkV3_0() != null;
    }

    private boolean checkVendorISupplicantStaNetworkAndLogFailure(String methodStr) {
        if (!this.mIHwSupplicantStaNetworkHalInner.vendorCheckAndLogFailure(methodStr)) {
            return false;
        }
        if (getVendorSupplicantStaNetworkV3_0() != null) {
            return true;
        }
        HwHiLog.e(TAG, false, "Can't cast mISupplicantStaNetwork to vendor 3.0 version", new Object[0]);
        return false;
    }

    private ISupplicantStaNetwork getVendorSupplicantStaNetworkV3_0() {
        synchronized (this.mIHwSupplicantStaNetworkHalInner.getHalLock()) {
            android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork mISupplicantStaNetwork = this.mIHwSupplicantStaNetworkHalInner.getISupplicantStaNetwork();
            if (mISupplicantStaNetwork == null) {
                return null;
            }
            return ISupplicantStaNetwork.castFrom(mISupplicantStaNetwork);
        }
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

    private class VendorSupplicantStaNetworkHalCallback extends ISupplicantStaNetworkCallback.Stub {
        private SupplicantStaNetworkHal.SupplicantStaNetworkHalCallback callback;
        private final int mFramewokNetworkId;
        private final String mSsid;

        VendorSupplicantStaNetworkHalCallback(int framewokNetworkId, String ssid) {
            this.mFramewokNetworkId = framewokNetworkId;
            this.mSsid = ssid;
            this.callback = HwSupplicantStaNetworkHalEx.this.mIHwSupplicantStaNetworkHalInner.createHalCallback(framewokNetworkId, ssid);
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
            synchronized (HwSupplicantStaNetworkHalEx.this.mIHwSupplicantStaNetworkHalInner.getHalLock()) {
                HwHiLog.e(HwSupplicantStaNetworkHalEx.TAG, false, "onNetworkEapNotificationErrorCode errorcode = %{public}d", new Object[]{Integer.valueOf(errorcode)});
                HwSupplicantStaNetworkHalEx.this.mWifiMonitor.broadcastNetworkEAPErrorcodeReportEvent(HwSupplicantStaNetworkHalEx.this.mIfaceName, this.mFramewokNetworkId, this.mSsid, errorcode);
            }
        }

        public void notifyStaNetworkIfaceEvent(int notifyType, String stringData) {
            HwHiLog.d(HwSupplicantStaNetworkHalEx.TAG, false, "notifyStaNetworkIfaceEvent notifyType = %{public}d", new Object[]{Integer.valueOf(notifyType)});
        }
    }

    private boolean hwStaNetworkRegisterCallback(vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaNetworkCallback callback) {
        synchronized (this.mIHwSupplicantStaNetworkHalInner.getHalLock()) {
            HwHiLog.d(TAG, false, "Start to register network callback for vendor ISupplicantStaNetwork", new Object[0]);
            if (!this.mIHwSupplicantStaNetworkHalInner.vendorCheckAndLogFailure("hwStaNetworkRegisterCallback")) {
                return false;
            }
            try {
                ISupplicantStaNetwork iface = getVendorSupplicantStaNetworkV3_0();
                if (iface == null) {
                    return false;
                }
                return this.mIHwSupplicantStaNetworkHalInner.vendorCheckStatusAndLogFailure(iface.hwStaNetworkRegisterCallback(callback), "hwStaNetworkRegisterCallback");
            } catch (RemoteException e) {
                this.mIHwSupplicantStaNetworkHalInner.vendorHandleRemoteException(e, "hwStaNetworkRegisterCallback");
                return false;
            }
        }
    }

    private boolean setWapiPskKeyType(int type) {
        synchronized (this.mIHwSupplicantStaNetworkHalInner.getHalLock()) {
            if (!this.mIHwSupplicantStaNetworkHalInner.vendorCheckAndLogFailure("setWAPIPskKeyType")) {
                return false;
            }
            try {
                ISupplicantStaNetwork iface = getVendorSupplicantStaNetworkV3_0();
                if (iface == null) {
                    return false;
                }
                return this.mIHwSupplicantStaNetworkHalInner.vendorCheckStatusAndLogFailure(iface.setWAPIPskKeyType(type), "setWAPIPskKeyType");
            } catch (RemoteException e) {
                this.mIHwSupplicantStaNetworkHalInner.vendorHandleRemoteException(e, "setWAPIPskKeyType");
                return false;
            }
        }
    }

    private boolean setWapiAsCertPath(String path) {
        synchronized (this.mIHwSupplicantStaNetworkHalInner.getHalLock()) {
            if (!this.mIHwSupplicantStaNetworkHalInner.vendorCheckAndLogFailure("setWapiAsCertPath")) {
                return false;
            }
            try {
                ISupplicantStaNetwork iface = getVendorSupplicantStaNetworkV3_0();
                if (iface == null) {
                    return false;
                }
                return this.mIHwSupplicantStaNetworkHalInner.vendorCheckStatusAndLogFailure(iface.setWAPIASCert(path), "setWapiAsCertPath");
            } catch (RemoteException e) {
                this.mIHwSupplicantStaNetworkHalInner.vendorHandleRemoteException(e, "setWapiAsCertPath");
                return false;
            }
        }
    }

    private boolean setWapiUserCertPath(String path) {
        synchronized (this.mIHwSupplicantStaNetworkHalInner.getHalLock()) {
            if (!this.mIHwSupplicantStaNetworkHalInner.vendorCheckAndLogFailure("setWapiUserCertPath")) {
                return false;
            }
            try {
                ISupplicantStaNetwork iface = getVendorSupplicantStaNetworkV3_0();
                if (iface == null) {
                    return false;
                }
                return this.mIHwSupplicantStaNetworkHalInner.vendorCheckStatusAndLogFailure(iface.setWAPIASUECert(path), "setWapiUserCertPath");
            } catch (RemoteException e) {
                this.mIHwSupplicantStaNetworkHalInner.vendorHandleRemoteException(e, "setWapiUserCertPath");
                return false;
            }
        }
    }

    public String deliverStaNetworkData(int cmdType, int dataType, String carryData) {
        HwHiLog.d(TAG, false, "deliverStaNetworkData: cmdType=%{public}d, dataType=%{public}d, carryData=%{private}s", new Object[]{Integer.valueOf(cmdType), Integer.valueOf(dataType), carryData});
        synchronized (this.mIHwSupplicantStaNetworkHalInner.getHalLock()) {
            ISupplicantStaNetwork iface = getVendorSupplicantStaNetworkV3_0();
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> deliverStaNetworkData = new HidlSupport.Mutable<>();
            try {
                iface.deliverStaNetworkData(cmdType, dataType, carryData, new ISupplicantStaNetwork.deliverStaNetworkDataCallback(deliverStaNetworkData) {
                    /* class com.android.server.wifi.$$Lambda$HwSupplicantStaNetworkHalEx$bP1tmi9lc1hnVkSd2tNX_UGIGII */
                    private final /* synthetic */ HidlSupport.Mutable f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        HwSupplicantStaNetworkHalEx.lambda$deliverStaNetworkData$0(this.f$0, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                this.mIHwSupplicantStaNetworkHalInner.vendorHandleRemoteException(e, "deliverStaNetworkData");
            }
            return (String) deliverStaNetworkData.value;
        }
    }
}
