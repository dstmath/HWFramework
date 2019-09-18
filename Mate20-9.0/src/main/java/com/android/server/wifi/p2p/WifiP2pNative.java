package com.android.server.wifi.p2p;

import android.hardware.wifi.V1_0.IWifiP2pIface;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.HalDeviceManager;

public class WifiP2pNative {
    private static final int CONNECT_TO_SUPPLICANT_MAX_SAMPLES = 50;
    private static final int CONNECT_TO_SUPPLICANT_SAMPLING_INTERVAL_MS = 100;
    private static final String TAG = "WifiP2pNative";
    private final HalDeviceManager mHalDeviceManager;
    /* access modifiers changed from: private */
    public IWifiP2pIface mIWifiP2pIface;
    private InterfaceAvailableListenerInternal mInterfaceAvailableListener;
    private InterfaceDestroyedListenerInternal mInterfaceDestroyedListener;
    /* access modifiers changed from: private */
    public final SupplicantP2pIfaceHal mSupplicantP2pIfaceHal;

    private class InterfaceAvailableListenerInternal implements HalDeviceManager.InterfaceAvailableForRequestListener {
        private final HalDeviceManager.InterfaceAvailableForRequestListener mExternalListener;

        InterfaceAvailableListenerInternal(HalDeviceManager.InterfaceAvailableForRequestListener externalListener) {
            this.mExternalListener = externalListener;
        }

        public void onAvailabilityChanged(boolean isAvailable) {
            Log.d(WifiP2pNative.TAG, "P2P InterfaceAvailableListener " + isAvailable);
            if (WifiP2pNative.this.mIWifiP2pIface == null || isAvailable) {
                this.mExternalListener.onAvailabilityChanged(isAvailable);
            } else {
                Log.i(WifiP2pNative.TAG, "Masking interface non-availability callback because we created a P2P iface");
            }
        }
    }

    private class InterfaceDestroyedListenerInternal implements HalDeviceManager.InterfaceDestroyedListener {
        private final HalDeviceManager.InterfaceDestroyedListener mExternalListener;
        private boolean mValid = true;

        InterfaceDestroyedListenerInternal(HalDeviceManager.InterfaceDestroyedListener externalListener) {
            this.mExternalListener = externalListener;
        }

        public void teardownAndInvalidate(String ifaceName) {
            WifiP2pNative.this.mSupplicantP2pIfaceHal.teardownIface(ifaceName);
            IWifiP2pIface unused = WifiP2pNative.this.mIWifiP2pIface = null;
            this.mValid = false;
        }

        public void onDestroyed(String ifaceName) {
            Log.d(WifiP2pNative.TAG, "P2P InterfaceDestroyedListener " + ifaceName);
            if (!this.mValid) {
                Log.d(WifiP2pNative.TAG, "Ignoring stale interface destroyed listener");
                return;
            }
            teardownAndInvalidate(ifaceName);
            this.mExternalListener.onDestroyed(ifaceName);
        }
    }

    public WifiP2pNative(SupplicantP2pIfaceHal p2pIfaceHal, HalDeviceManager halDeviceManager) {
        this.mSupplicantP2pIfaceHal = p2pIfaceHal;
        this.mHalDeviceManager = halDeviceManager;
    }

    public void enableVerboseLogging(int verbose) {
    }

    private boolean waitForSupplicantConnection() {
        if (!this.mSupplicantP2pIfaceHal.isInitializationStarted() && !this.mSupplicantP2pIfaceHal.initialize()) {
            return false;
        }
        int connectTries = 0;
        while (true) {
            int connectTries2 = connectTries + 1;
            if (connectTries >= 50) {
                return false;
            }
            if (this.mSupplicantP2pIfaceHal.isInitializationComplete()) {
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            connectTries = connectTries2;
        }
    }

    public void closeSupplicantConnection() {
    }

    public void registerInterfaceAvailableListener(HalDeviceManager.InterfaceAvailableForRequestListener listener, Handler handler) {
        this.mInterfaceAvailableListener = new InterfaceAvailableListenerInternal(listener);
        this.mHalDeviceManager.registerStatusListener(new HalDeviceManager.ManagerStatusListener(handler) {
            private final /* synthetic */ Handler f$1;

            {
                this.f$1 = r2;
            }

            public final void onStatusChanged() {
                WifiP2pNative.lambda$registerInterfaceAvailableListener$0(WifiP2pNative.this, this.f$1);
            }
        }, handler);
        if (this.mHalDeviceManager.isStarted()) {
            this.mHalDeviceManager.registerInterfaceAvailableForRequestListener(2, this.mInterfaceAvailableListener, handler);
        }
    }

    public static /* synthetic */ void lambda$registerInterfaceAvailableListener$0(WifiP2pNative wifiP2pNative, Handler handler) {
        if (wifiP2pNative.mHalDeviceManager.isStarted()) {
            Log.i(TAG, "Registering for interface available listener");
            wifiP2pNative.mHalDeviceManager.registerInterfaceAvailableForRequestListener(2, wifiP2pNative.mInterfaceAvailableListener, handler);
        }
    }

    public String setupInterface(HalDeviceManager.InterfaceDestroyedListener destroyedListener, Handler handler) {
        Log.d(TAG, "Setup P2P interface");
        if (this.mIWifiP2pIface == null) {
            this.mInterfaceDestroyedListener = new InterfaceDestroyedListenerInternal(destroyedListener);
            this.mIWifiP2pIface = this.mHalDeviceManager.createP2pIface(this.mInterfaceDestroyedListener, handler);
            if (this.mIWifiP2pIface == null) {
                Log.e(TAG, "Failed to create P2p iface in HalDeviceManager");
                return null;
            } else if (!waitForSupplicantConnection()) {
                Log.e(TAG, "Failed to connect to supplicant");
                teardownInterface();
                return null;
            } else {
                String ifaceName = HalDeviceManager.getName(this.mIWifiP2pIface);
                if (TextUtils.isEmpty(ifaceName)) {
                    Log.e(TAG, "Failed to get p2p iface name");
                    teardownInterface();
                    return null;
                } else if (!this.mSupplicantP2pIfaceHal.setupIface(ifaceName)) {
                    Log.e(TAG, "Failed to setup P2p iface in supplicant");
                    teardownInterface();
                    return null;
                } else {
                    Log.i(TAG, "P2P interface setup completed");
                }
            }
        }
        return HalDeviceManager.getName(this.mIWifiP2pIface);
    }

    public void teardownInterface() {
        Log.d(TAG, "Teardown P2P interface");
        if (this.mIWifiP2pIface != null) {
            String ifaceName = HalDeviceManager.getName(this.mIWifiP2pIface);
            this.mHalDeviceManager.removeIface(this.mIWifiP2pIface);
            this.mInterfaceDestroyedListener.teardownAndInvalidate(ifaceName);
            Log.i(TAG, "P2P interface teardown completed");
        }
    }

    public boolean setDeviceName(String name) {
        return this.mSupplicantP2pIfaceHal.setWpsDeviceName(name);
    }

    public boolean p2pListNetworks(WifiP2pGroupList groups) {
        return this.mSupplicantP2pIfaceHal.loadGroups(groups);
    }

    public boolean startWpsPbc(String iface, String bssid) {
        return this.mSupplicantP2pIfaceHal.startWpsPbc(iface, bssid);
    }

    public boolean startWpsPinKeypad(String iface, String pin) {
        return this.mSupplicantP2pIfaceHal.startWpsPinKeypad(iface, pin);
    }

    public String startWpsPinDisplay(String iface, String bssid) {
        return this.mSupplicantP2pIfaceHal.startWpsPinDisplay(iface, bssid);
    }

    public boolean removeP2pNetwork(int netId) {
        return this.mSupplicantP2pIfaceHal.removeNetwork(netId);
    }

    public boolean setP2pDeviceName(String name) {
        return this.mSupplicantP2pIfaceHal.setWpsDeviceName(name);
    }

    public boolean setP2pDeviceType(String type) {
        return this.mSupplicantP2pIfaceHal.setWpsDeviceType(type);
    }

    public boolean setConfigMethods(String cfg) {
        return this.mSupplicantP2pIfaceHal.setWpsConfigMethods(cfg);
    }

    public boolean setP2pSsidPostfix(String postfix) {
        return this.mSupplicantP2pIfaceHal.setSsidPostfix(postfix);
    }

    public boolean setP2pGroupIdle(String iface, int time) {
        return this.mSupplicantP2pIfaceHal.setGroupIdle(iface, time);
    }

    public boolean setP2pPowerSave(String iface, boolean enabled) {
        return this.mSupplicantP2pIfaceHal.setPowerSave(iface, enabled);
    }

    public boolean setWfdEnable(boolean enable) {
        return this.mSupplicantP2pIfaceHal.enableWfd(enable);
    }

    public boolean setWfdDeviceInfo(String hex) {
        return this.mSupplicantP2pIfaceHal.setWfdDeviceInfo(hex);
    }

    public boolean p2pFind() {
        return p2pFind(0);
    }

    public boolean p2pFind(int timeout) {
        return this.mSupplicantP2pIfaceHal.find(timeout);
    }

    public boolean p2pStopFind() {
        return this.mSupplicantP2pIfaceHal.stopFind();
    }

    public boolean p2pExtListen(boolean enable, int period, int interval) {
        return this.mSupplicantP2pIfaceHal.configureExtListen(enable, period, interval);
    }

    public boolean p2pSetChannel(int lc, int oc) {
        return this.mSupplicantP2pIfaceHal.setListenChannel(lc, oc);
    }

    public boolean p2pFlush() {
        return this.mSupplicantP2pIfaceHal.flush();
    }

    public String p2pConnect(WifiP2pConfig config, boolean joinExistingGroup) {
        return this.mSupplicantP2pIfaceHal.connect(config, joinExistingGroup);
    }

    public boolean p2pCancelConnect() {
        return this.mSupplicantP2pIfaceHal.cancelConnect();
    }

    public boolean p2pProvisionDiscovery(WifiP2pConfig config) {
        return this.mSupplicantP2pIfaceHal.provisionDiscovery(config);
    }

    public boolean p2pGroupAdd(boolean persistent) {
        return this.mSupplicantP2pIfaceHal.groupAdd(persistent);
    }

    public boolean p2pGroupAdd(int netId) {
        return this.mSupplicantP2pIfaceHal.groupAdd(netId, true);
    }

    public boolean magiclinkGroupAdd(boolean persistent, String freq) {
        return this.mSupplicantP2pIfaceHal.groupAddWithFreq(-1, persistent, freq);
    }

    public boolean magiclinkGroupAdd(int netId, String freq) {
        return this.mSupplicantP2pIfaceHal.groupAddWithFreq(netId, true, freq);
    }

    public boolean magiclinkConnect(String cmd) {
        return this.mSupplicantP2pIfaceHal.magiclinkConnect(cmd);
    }

    public boolean p2pGroupRemove(String iface) {
        return this.mSupplicantP2pIfaceHal.groupRemove(iface);
    }

    public boolean p2pReject(String deviceAddress) {
        return this.mSupplicantP2pIfaceHal.reject(deviceAddress);
    }

    public boolean p2pInvite(WifiP2pGroup group, String deviceAddress) {
        return this.mSupplicantP2pIfaceHal.invite(group, deviceAddress);
    }

    public boolean p2pReinvoke(int netId, String deviceAddress) {
        return this.mSupplicantP2pIfaceHal.reinvoke(netId, deviceAddress);
    }

    public String p2pGetSsid(String deviceAddress) {
        return this.mSupplicantP2pIfaceHal.getSsid(deviceAddress);
    }

    public String p2pGetDeviceAddress() {
        return this.mSupplicantP2pIfaceHal.getDeviceAddress();
    }

    public int getGroupCapability(String deviceAddress) {
        return this.mSupplicantP2pIfaceHal.getGroupCapability(deviceAddress);
    }

    public boolean p2pServiceAdd(WifiP2pServiceInfo servInfo) {
        return this.mSupplicantP2pIfaceHal.serviceAdd(servInfo);
    }

    public boolean p2pServiceDel(WifiP2pServiceInfo servInfo) {
        return this.mSupplicantP2pIfaceHal.serviceRemove(servInfo);
    }

    public boolean p2pServiceFlush() {
        return this.mSupplicantP2pIfaceHal.serviceFlush();
    }

    public String p2pServDiscReq(String addr, String query) {
        return this.mSupplicantP2pIfaceHal.requestServiceDiscovery(addr, query);
    }

    public boolean p2pServDiscCancelReq(String id) {
        return this.mSupplicantP2pIfaceHal.cancelServiceDiscovery(id);
    }

    public void setMiracastMode(int mode) {
        this.mSupplicantP2pIfaceHal.setMiracastMode(mode);
    }

    public String getNfcHandoverRequest() {
        return this.mSupplicantP2pIfaceHal.getNfcHandoverRequest();
    }

    public String getNfcHandoverSelect() {
        return this.mSupplicantP2pIfaceHal.getNfcHandoverSelect();
    }

    public boolean initiatorReportNfcHandover(String selectMessage) {
        return this.mSupplicantP2pIfaceHal.initiatorReportNfcHandover(selectMessage);
    }

    public boolean responderReportNfcHandover(String requestMessage) {
        return this.mSupplicantP2pIfaceHal.responderReportNfcHandover(requestMessage);
    }

    public String getP2pClientList(int netId) {
        return this.mSupplicantP2pIfaceHal.getClientList(netId);
    }

    public boolean setP2pClientList(int netId, String list) {
        return this.mSupplicantP2pIfaceHal.setClientList(netId, list);
    }

    public boolean saveConfig() {
        return this.mSupplicantP2pIfaceHal.saveConfig();
    }

    public boolean enableP2p(int p2pFlag) {
        return true;
    }

    public boolean addP2pRptGroup(String config) {
        return this.mSupplicantP2pIfaceHal.addP2pRptGroup(config);
    }

    public int getLinkSpeed(String ifname) {
        return this.mSupplicantP2pIfaceHal.getP2pLinkspeed(ifname);
    }
}
