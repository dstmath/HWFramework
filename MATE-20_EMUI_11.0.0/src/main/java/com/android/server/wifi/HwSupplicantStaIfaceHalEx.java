package com.android.server.wifi;

import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.os.Bundle;
import android.os.HidlSupport;
import android.os.RemoteException;
import android.util.Log;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.SupplicantStaIfaceHal;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant;
import vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIface;
import vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback;

public class HwSupplicantStaIfaceHalEx implements IHwSupplicantStaIfaceHalEx {
    private static final int NOTIFY_TYPE_WPA3_FAIL_EVENT = 3;
    private static final int NOTIFY_TYPE_WPA_4WAY_HANDSHAKE_FAIL_EVENT = 4;
    private static final String TAG = "HwSupplicantStaIfaceHalEx";
    private HwWifiCHRService mHwWifiCHRService = null;
    private IHwSupplicantStaIfaceHalInner mSupplicantStaIfaceHalInner = null;
    private WifiMonitor mWifiMonitor = null;

    public static HwSupplicantStaIfaceHalEx createHwSupplicantStaIfaceHalEx(IHwSupplicantStaIfaceHalInner supplicantStaIfaceHalInner, WifiMonitor monitor) {
        HwHiLog.d(TAG, false, "createHwSupplicantStaIfaceHalEx is called!", new Object[0]);
        return new HwSupplicantStaIfaceHalEx(supplicantStaIfaceHalInner, monitor);
    }

    HwSupplicantStaIfaceHalEx(IHwSupplicantStaIfaceHalInner supplicantStaIfaceHalInner, WifiMonitor monitor) {
        this.mSupplicantStaIfaceHalInner = supplicantStaIfaceHalInner;
        this.mWifiMonitor = monitor;
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
    }

    public String voWifiDetect(String ifaceName, String cmd) {
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "VowifiDetect");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotResult = new HidlSupport.Mutable<>();
            try {
                iface.VowifiDetect(cmd, new ISupplicantStaIface.VowifiDetectCallback(gotResult) {
                    /* class com.android.server.wifi.$$Lambda$HwSupplicantStaIfaceHalEx$pg9p5QMtGrWXz9GbgTuuHI_rXs */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        HwSupplicantStaIfaceHalEx.this.lambda$voWifiDetect$0$HwSupplicantStaIfaceHalEx(this.f$1, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "VowifiDetect");
            }
            return (String) gotResult.value;
        }
    }

    public /* synthetic */ void lambda$voWifiDetect$0$HwSupplicantStaIfaceHalEx(HidlSupport.Mutable gotResult, SupplicantStatus status, String result) {
        if (this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(status, "VowifiDetect")) {
            gotResult.value = result;
            HwHiLog.d(TAG, false, "%{public}s", new Object[]{result});
        }
    }

    public String heartBeat(String ifaceName, String param) {
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "heartBeat");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotResult = new HidlSupport.Mutable<>();
            try {
                iface.heartBeat(param, new ISupplicantStaIface.heartBeatCallback(gotResult) {
                    /* class com.android.server.wifi.$$Lambda$HwSupplicantStaIfaceHalEx$5W37QAQfLt6aQPBSrn6XEujLnKY */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        HwSupplicantStaIfaceHalEx.this.lambda$heartBeat$1$HwSupplicantStaIfaceHalEx(this.f$1, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "heartBeat");
            }
            return (String) gotResult.value;
        }
    }

    public /* synthetic */ void lambda$heartBeat$1$HwSupplicantStaIfaceHalEx(HidlSupport.Mutable gotResult, SupplicantStatus status, String result) {
        if (this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(status, "heartBeat")) {
            gotResult.value = result;
            HwHiLog.d(TAG, false, "%{public}s", new Object[]{result});
        }
    }

    public boolean enableHiLinkHandshake(String ifaceName, boolean uiEnable, String bssid) {
        HwHiLog.d(TAG, false, "enableHiLinkHandshake:uiEnable=%{public}s bssid=%{private}s", new Object[]{String.valueOf(uiEnable), bssid});
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "enableHiLinkHandshake");
            if (iface == null) {
                return false;
            }
            try {
                return this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(iface.enableHiLinkHandshake(uiEnable, bssid), "enableHiLinkHandshake");
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "enableHiLinkHandshake");
                return false;
            }
        }
    }

    public boolean setTxPower(String ifaceName, int level) {
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "setTxPower");
            if (iface == null) {
                return false;
            }
            try {
                return this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(iface.setTxPower(level), "setTxPower");
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "setTxPower");
                return false;
            }
        }
    }

    public boolean setAbsCapability(String ifaceName, int capability) {
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "SetAbsCapability");
            if (iface == null) {
                return false;
            }
            try {
                return this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(iface.SetAbsCapability(capability), "SetAbsCapability");
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "SetAbsCapability");
                return false;
            }
        }
    }

    public boolean absPowerCtrl(String ifaceName, int type) {
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "AbsPowerCtrl");
            if (iface == null) {
                return false;
            }
            try {
                return this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(iface.AbsPowerCtrl(type), "AbsPowerCtrl");
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "AbsPowerCtrl");
                return false;
            }
        }
    }

    public boolean setAbsBlacklist(String ifaceName, String bssidList) {
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "SetAbsBlacklist");
            if (iface == null) {
                return false;
            }
            try {
                return this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(iface.SetAbsBlacklist(bssidList), "SetAbsBlacklist");
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "SetAbsBlacklist");
                return false;
            }
        }
    }

    public boolean query11vRoamingNetwork(String ifaceName, int reason) {
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "wnmBssQurey");
            if (iface == null) {
                return false;
            }
            try {
                return this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(iface.wnmBssQurey(reason), "wnmBssQurey");
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "wnmBssQurey");
                return false;
            }
        }
    }

    public String getRsdbCapability(String ifaceName) {
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "getCapabRsdb");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotRsdb = new HidlSupport.Mutable<>();
            try {
                iface.getCapabRsdb(new ISupplicantStaIface.getCapabRsdbCallback(gotRsdb) {
                    /* class com.android.server.wifi.$$Lambda$HwSupplicantStaIfaceHalEx$b1mXZ8RqiuJpGhViGawhBVz61b0 */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        HwSupplicantStaIfaceHalEx.this.lambda$getRsdbCapability$2$HwSupplicantStaIfaceHalEx(this.f$1, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "getCapabRsdb");
            }
            return (String) gotRsdb.value;
        }
    }

    public /* synthetic */ void lambda$getRsdbCapability$2$HwSupplicantStaIfaceHalEx(HidlSupport.Mutable gotRsdb, SupplicantStatus status, String rsdb) {
        if (this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(status, "getCapabRsdb")) {
            gotRsdb.value = rsdb;
        }
    }

    public String getWpasConfig(String ifaceName, int psktype) {
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "getWpasConfig");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotPsk = new HidlSupport.Mutable<>();
            try {
                iface.getWpasConfig(psktype, new ISupplicantStaIface.getWpasConfigCallback(gotPsk) {
                    /* class com.android.server.wifi.$$Lambda$HwSupplicantStaIfaceHalEx$sElB22bRdqEyWuIDOQUwkSZkvCs */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        HwSupplicantStaIfaceHalEx.this.lambda$getWpasConfig$3$HwSupplicantStaIfaceHalEx(this.f$1, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "getWpasConfig");
            }
            return (String) gotPsk.value;
        }
    }

    public /* synthetic */ void lambda$getWpasConfig$3$HwSupplicantStaIfaceHalEx(HidlSupport.Mutable gotPsk, SupplicantStatus status, String psk) {
        if (this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(status, "getWpasConfig")) {
            gotPsk.value = psk;
        }
    }

    public boolean pwrPercentBoostModeset(String ifaceName, int rssi) {
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "pwrPercentBoostModeset");
            if (iface == null) {
                return false;
            }
            try {
                return this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(iface.pwrPercentBoostModeset(rssi), "pwrPercentBoostModeset");
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "pwrPercentBoostModeset");
                return false;
            }
        }
    }

    public String getMssState(String ifaceName) {
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "getMssState");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotMss = new HidlSupport.Mutable<>();
            try {
                iface.getMssState(new ISupplicantStaIface.getMssStateCallback(gotMss) {
                    /* class com.android.server.wifi.$$Lambda$HwSupplicantStaIfaceHalEx$AJ5UNQrmpT58i5ouEw_EncHrc */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        HwSupplicantStaIfaceHalEx.this.lambda$getMssState$4$HwSupplicantStaIfaceHalEx(this.f$1, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "getMssState");
            }
            return (String) gotMss.value;
        }
    }

    public /* synthetic */ void lambda$getMssState$4$HwSupplicantStaIfaceHalEx(HidlSupport.Mutable gotMss, SupplicantStatus status, String mss) {
        if (this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(status, "getMssState")) {
            gotMss.value = mss;
        }
    }

    public String getApVendorInfo(String ifaceName) {
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "getApVendorInfo");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotApVendorInfo = new HidlSupport.Mutable<>();
            try {
                iface.getApVendorInfo(new ISupplicantStaIface.getApVendorInfoCallback(gotApVendorInfo) {
                    /* class com.android.server.wifi.$$Lambda$HwSupplicantStaIfaceHalEx$oy6x0gpDdPFlVVRkNHk5tvLk2ko */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        HwSupplicantStaIfaceHalEx.this.lambda$getApVendorInfo$5$HwSupplicantStaIfaceHalEx(this.f$1, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "getApVendorInfo");
            }
            return (String) gotApVendorInfo.value;
        }
    }

    public /* synthetic */ void lambda$getApVendorInfo$5$HwSupplicantStaIfaceHalEx(HidlSupport.Mutable gotApVendorInfo, SupplicantStatus status, String apvendorinfo) {
        if (this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(status, "getApVendorInfo")) {
            gotApVendorInfo.value = apvendorinfo;
        }
    }

    public String deliverStaIfaceData(String ifaceName, int cmdType, int dataType, String carryData) {
        HwHiLog.d(TAG, false, "deliverStaIfaceData: ifaceName=%{public}s, cmdType=%{public}d, dataType=%{public}d, carryData=%{private}s", new Object[]{ifaceName, Integer.valueOf(cmdType), Integer.valueOf(dataType), carryData});
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "deliverStaIfaceData");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> deliverStaIfaceData = new HidlSupport.Mutable<>();
            try {
                iface.deliverStaIfaceData(cmdType, dataType, carryData, new ISupplicantStaIface.deliverStaIfaceDataCallback(deliverStaIfaceData) {
                    /* class com.android.server.wifi.$$Lambda$HwSupplicantStaIfaceHalEx$nS4WV_snt3YQjfxp7oHMNFTOvfw */
                    private final /* synthetic */ HidlSupport.Mutable f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        HwSupplicantStaIfaceHalEx.lambda$deliverStaIfaceData$6(this.f$0, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "deliverStaIfaceData");
            }
            return (String) deliverStaIfaceData.value;
        }
    }

    public boolean setFilterEnable(String ifaceName, boolean enable) {
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "setFilterEnable");
            if (iface == null) {
                return false;
            }
            try {
                return this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(iface.setFilterEnable(enable), "setFilterEnable");
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "setFilterEnable");
                return false;
            }
        }
    }

    public boolean trySetupForVendorV3_0(String ifaceName, ISupplicantIface ifaceHwBinder, SupplicantStaIfaceHal.SupplicantStaIfaceHalCallback callback) {
        if (!isVendorV3_0()) {
            return false;
        }
        HwHiLog.d(TAG, false, "Start to setup vendor ISupplicantStaIface", new Object[0]);
        ISupplicantStaIface iface = getVendorStaIfaceV3_0(ifaceHwBinder);
        HwHiLog.d(TAG, false, "%{public}s", new Object[]{"ISupplicantStaIface=" + iface});
        VendorSupplicantStaIfaceHalCallbackV3_0 callbackV3_0 = new VendorSupplicantStaIfaceHalCallbackV3_0(ifaceName, this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalCallbackV1_1(ifaceName, callback));
        if (!hwStaRegisterCallback(iface, callbackV3_0)) {
            return false;
        }
        this.mSupplicantStaIfaceHalInner.getISupplicantStaIfaces().put(ifaceName, iface);
        this.mSupplicantStaIfaceHalInner.getISupplicantStaIfaceCallbacks().put(ifaceName, callbackV3_0);
        HwHiLog.d(TAG, false, "Successfully setup vendor ISupplicantStaIface", new Object[0]);
        return true;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001c: APUT  
      (r5v1 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x0018: INVOKE  (r6v3 java.lang.String) = (r6v2 boolean) type: STATIC call: java.lang.String.valueOf(boolean):java.lang.String)
     */
    private boolean isVendorV3_0() {
        boolean z;
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            z = true;
            try {
                Object[] objArr = new Object[1];
                objArr[0] = String.valueOf(getVendorSupplicantV3_0() != null);
                HwHiLog.d(TAG, false, "isVendorV3_0() %{public}s", objArr);
                if (getVendorSupplicantV3_0() == null) {
                    z = false;
                }
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "ISupplicant.getService exception: %{public}s", new Object[]{e.getMessage()});
                this.mSupplicantStaIfaceHalInner.supplicantServiceDiedHandler(0);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return z;
    }

    public ISupplicant getVendorSupplicantV3_0() throws RemoteException {
        ISupplicant castFrom;
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            try {
                HwHiLog.d(TAG, false, "call supplicant.V3_0.ISupplicant", new Object[0]);
                castFrom = ISupplicant.castFrom(android.hardware.wifi.supplicant.V1_0.ISupplicant.getService());
            } catch (NoSuchElementException e) {
                HwHiLog.e(TAG, false, "Failed to get vendor V3_0 ISupplicant %{public}s", new Object[]{e.getMessage()});
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
        return castFrom;
    }

    private ISupplicantStaIface getVendorStaIfaceV3_0(ISupplicantIface iface) {
        ISupplicantStaIface castFrom;
        if (iface == null) {
            return null;
        }
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            castFrom = ISupplicantStaIface.castFrom(iface);
        }
        return castFrom;
    }

    private boolean hwStaRegisterCallback(ISupplicantStaIface iface, ISupplicantStaIfaceCallback callback) {
        synchronized (this.mSupplicantStaIfaceHalInner.getSupplicantStaIfaceHalLock()) {
            if (iface == null) {
                HwHiLog.e(TAG, false, "Got null iface when registering callback", new Object[0]);
                return false;
            }
            try {
                return this.mSupplicantStaIfaceHalInner.checkStatusAndLogFailure(iface.hwStaRegisterCallback(callback), "hwStaRegisterCallback");
            } catch (RemoteException e) {
                this.mSupplicantStaIfaceHalInner.handleRemoteException(e, "hwStaRegisterCallback");
                return false;
            }
        }
    }

    private class VendorSupplicantStaIfaceHalCallbackV3_0 extends ISupplicantStaIfaceCallback.Stub {
        private String HANDSHAKE_ERR_CODE = "handshakeErrCode";
        private SupplicantStaIfaceHal.SupplicantStaIfaceHalCallbackV1_1 mCallbackV1_1;
        private String mIfaceName;

        VendorSupplicantStaIfaceHalCallbackV3_0(String ifaceName, SupplicantStaIfaceHal.SupplicantStaIfaceHalCallbackV1_1 callback) {
            this.mIfaceName = ifaceName;
            this.mCallbackV1_1 = callback;
        }

        public void onNetworkAdded(int id) {
            this.mCallbackV1_1.onNetworkAdded(id);
        }

        public void onNetworkRemoved(int id) {
            this.mCallbackV1_1.onNetworkRemoved(id);
        }

        public void onStateChanged(int newState, byte[] bssid, int id, ArrayList<Byte> ssid) {
            this.mCallbackV1_1.onStateChanged(newState, bssid, id, ssid);
        }

        public void onAnqpQueryDone(byte[] bssid, ISupplicantStaIfaceCallback.AnqpData data, ISupplicantStaIfaceCallback.Hs20AnqpData hs20Data) {
            this.mCallbackV1_1.onAnqpQueryDone(bssid, data, hs20Data);
        }

        public void onHs20IconQueryDone(byte[] bssid, String fileName, ArrayList<Byte> data) {
            this.mCallbackV1_1.onHs20IconQueryDone(bssid, fileName, data);
        }

        public void onHs20SubscriptionRemediation(byte[] bssid, byte osuMethod, String url) {
            this.mCallbackV1_1.onHs20SubscriptionRemediation(bssid, osuMethod, url);
        }

        public void onHs20DeauthImminentNotice(byte[] bssid, int reasonCode, int reAuthDelayInSec, String url) {
            this.mCallbackV1_1.onHs20DeauthImminentNotice(bssid, reasonCode, reAuthDelayInSec, url);
        }

        public void onDisconnected(byte[] bssid, boolean locallyGenerated, int reasonCode) {
            this.mCallbackV1_1.onDisconnected(bssid, locallyGenerated, reasonCode);
        }

        public void onAssociationRejected(byte[] bssid, int statusCode, boolean timedOut) {
            this.mCallbackV1_1.onAssociationRejected(bssid, statusCode, timedOut);
        }

        public void onAuthenticationTimeout(byte[] bssid) {
            this.mCallbackV1_1.onAuthenticationTimeout(bssid);
        }

        public void onBssidChanged(byte reason, byte[] bssid) {
            this.mCallbackV1_1.onBssidChanged(reason, bssid);
        }

        public void onEapFailure() {
            this.mCallbackV1_1.onEapFailure();
        }

        public void onEapFailure_1_1(int code) {
            this.mCallbackV1_1.onEapFailure_1_1(code);
        }

        public void onWpsEventSuccess() {
            this.mCallbackV1_1.onWpsEventSuccess();
        }

        public void onWpsEventFail(byte[] bssid, short configError, short errorInd) {
            this.mCallbackV1_1.onWpsEventFail(bssid, configError, errorInd);
        }

        public void onWpsEventPbcOverlap() {
            this.mCallbackV1_1.onWpsEventPbcOverlap();
        }

        public void onExtRadioWorkStart(int id) {
            this.mCallbackV1_1.onExtRadioWorkStart(id);
        }

        public void onExtRadioWorkTimeout(int id) {
            this.mCallbackV1_1.onExtRadioWorkTimeout(id);
        }

        public void onDppSuccessConfigReceived(ArrayList<Byte> arrayList, String password, byte[] psk, int securityAkm) {
        }

        public void onDppSuccessConfigSent() {
        }

        public void onDppProgress(int code) {
        }

        public void onDppFailure(int code) {
        }

        public void onWapiCertInitFail() {
            HwHiLog.d(HwSupplicantStaIfaceHalEx.TAG, false, "ISupplicantStaIfaceCallback. onWapiCertInitFail received", new Object[0]);
            HwSupplicantStaIfaceHalEx.this.mWifiMonitor.broadcastWapiCertInitFailEvent(this.mIfaceName);
        }

        public void onWapiAuthFail() {
            HwHiLog.d(HwSupplicantStaIfaceHalEx.TAG, false, "ISupplicantStaIfaceCallback. onWapiAuthFail received", new Object[0]);
            HwSupplicantStaIfaceHalEx.this.mWifiMonitor.broadcastWapiAuthFailEvent(this.mIfaceName);
        }

        public void onVoWifiIrqStr() {
            HwHiLog.d(HwSupplicantStaIfaceHalEx.TAG, false, "ISupplicantStaIfaceCallback. onVoWifiIrqStr received", new Object[0]);
            HwSupplicantStaIfaceHalEx.this.mWifiMonitor.broadcastVoWifiIrqStrEvent(this.mIfaceName);
        }

        public void notifyStaIfaceEvent(int notifyType, String eventInfo) {
            if (eventInfo == null) {
                HwHiLog.e(HwSupplicantStaIfaceHalEx.TAG, false, "eventInfo is null, notifyType=%{public}d", new Object[]{Integer.valueOf(notifyType)});
                return;
            }
            HwHiLog.d(HwSupplicantStaIfaceHalEx.TAG, false, "ISupplicantStaIfaceCallback. notifyStaIfaceEvent received notifyType=%{public}d, event=%{private}s", new Object[]{Integer.valueOf(notifyType), eventInfo});
            if (notifyType == 3) {
                HwSupplicantStaIfaceHalEx.this.mWifiMonitor.broadcastWpa3ConnectFailEvent(this.mIfaceName, eventInfo);
            } else if (notifyType != 4) {
                HwHiLog.d(HwSupplicantStaIfaceHalEx.TAG, false, "receive a unknown notifyType=%{public}d", new Object[]{Integer.valueOf(notifyType)});
            } else if (HwSupplicantStaIfaceHalEx.this.mHwWifiCHRService == null) {
                Log.e(HwSupplicantStaIfaceHalEx.TAG, "mHwWifiCHRService is null");
            } else {
                Bundle data = new Bundle();
                data.putString(this.HANDSHAKE_ERR_CODE, eventInfo);
                HwSupplicantStaIfaceHalEx.this.mHwWifiCHRService.uploadDFTEvent(30, data);
            }
        }

        public void onHilinkStartWps(String arg) {
            HwHiLog.d(HwSupplicantStaIfaceHalEx.TAG, false, "ISupplicantStaIfaceCallback. onHilinkStartWps received", new Object[0]);
            HwSupplicantStaIfaceHalEx.this.mWifiMonitor.broadcastHilinkStartWpsEvent(this.mIfaceName, arg);
        }

        public void onHilinkStartWps() {
            HwHiLog.d(HwSupplicantStaIfaceHalEx.TAG, false, "ISupplicantStaIfaceCallback. onHilinkStartWps received", new Object[0]);
        }

        public void onAbsAntCoreRob() {
            HwHiLog.d(HwSupplicantStaIfaceHalEx.TAG, false, "ISupplicantStaIfaceCallback. onAbsAntCoreRob received", new Object[0]);
            HwSupplicantStaIfaceHalEx.this.mWifiMonitor.broadcastAbsAntCoreRobEvent(this.mIfaceName);
        }
    }

    private ISupplicantStaIface checkVendorSupplicantStaIfaceAndLogFailure(String ifaceName, String methodStr) {
        return getVendorStaIfaceV3_0(this.mSupplicantStaIfaceHalInner.checkSupplicantStaIfaceAndLogFailure(ifaceName, methodStr));
    }
}
