package com.huawei.nearbysdk.clone;

import android.content.Context;
import android.os.Looper;
import android.os.RemoteException;
import com.huawei.nearbysdk.HwLog;
import com.huawei.nearbysdk.INearbyAdapter;
import com.huawei.nearbysdk.NearbyAdapter;
import com.huawei.nearbysdk.NearbyAdapterCallback;
import com.huawei.nearbysdk.NearbyConfig;
import com.huawei.nearbysdk.NearbyConfiguration;
import com.huawei.nearbysdk.NearbyDevice;

public final class CloneAdapter {
    private static final String TAG = "CloneAdapter";
    public static final int WIFI_BAND_2GHZ = 1;
    public static final int WIFI_BAND_5GHZ = 2;
    public static final int WIFI_BAND_AUTO = 0;
    /* access modifiers changed from: private */
    public static CloneAdapter mCloneAdapter;
    /* access modifiers changed from: private */
    public static final Object mCloneLock = new Object();
    private WifiStatusListenerTransport mApHostListenerTransport;
    private NearbyAdapter mNearbyAdapter;
    private WifiStatusListenerTransport mWifiSlaveListenerTransport;

    private CloneAdapter(NearbyAdapter adapter) {
        synchronized (mCloneLock) {
            HwLog.i(TAG, "CloneAdapter init");
            this.mNearbyAdapter = adapter;
        }
    }

    public static void createInstance(Context context, final CloneAdapterCallback callback) {
        synchronized (mCloneLock) {
            HwLog.i(TAG, "CloneAdapter createInstance");
            if (callback != null) {
                NearbyAdapter.createInstance(context, new NearbyAdapterCallback() {
                    public void onAdapterGet(NearbyAdapter adapter) {
                        synchronized (CloneAdapter.mCloneLock) {
                            HwLog.w(CloneAdapter.TAG, "CloneAdapter onAdapterGet " + adapter);
                            if (adapter == null) {
                                CloneAdapter.releaseInstance();
                                CloneAdapterCallback.this.onAdapterGet(null);
                                return;
                            }
                            if (CloneAdapter.mCloneAdapter == null) {
                                CloneAdapter unused = CloneAdapter.mCloneAdapter = new CloneAdapter(adapter);
                            }
                            CloneAdapterCallback.this.onAdapterGet(CloneAdapter.mCloneAdapter);
                        }
                    }

                    public void onBinderDied() {
                        synchronized (CloneAdapter.mCloneLock) {
                            HwLog.w(CloneAdapter.TAG, "CloneAdapter onBinderDied");
                            CloneAdapter.releaseInstance();
                            CloneAdapterCallback.this.onBinderDied();
                        }
                    }
                });
            } else {
                HwLog.e(TAG, "createInstance callback null");
                throw new IllegalArgumentException("createInstance callback null");
            }
        }
    }

    public static void releaseInstance() {
        synchronized (mCloneLock) {
            HwLog.w(TAG, "CloneAdapter releaseInstance");
            if (mCloneAdapter != null) {
                mCloneAdapter.disconnectWifi(false);
                mCloneAdapter.disableWifiAp();
                mCloneAdapter.mNearbyAdapter = null;
                mCloneAdapter.mApHostListenerTransport = null;
                mCloneAdapter.mWifiSlaveListenerTransport = null;
            }
            mCloneAdapter = null;
            NearbyAdapter.releaseInstance();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        HwLog.w(TAG, "CloneAdapter finalize");
        super.finalize();
    }

    public void enableWifiAp(String wifiSsid, String wifiPwd, int wifiBand, int serverPort, WifiStatusListener listener, int timeoutMs) {
        synchronized (mCloneLock) {
            HwLog.i(TAG, "enableWifiAp start");
            boolean result = false;
            if (wifiSsid == null || wifiPwd == null || listener == null) {
                HwLog.e(TAG, "enableWifiAp wifiSsid\\wifiPwd or listener null");
                throw new IllegalArgumentException("enableWifiAp wifiSsid\\wifiPwd or listener null");
            } else if (this.mApHostListenerTransport == null) {
                if (this.mNearbyAdapter != null) {
                    INearbyAdapter nearbyService = this.mNearbyAdapter.getNearbyService();
                    INearbyAdapter nearbyService2 = nearbyService;
                    if (nearbyService != null) {
                        Looper looper = this.mNearbyAdapter.getLooper();
                        Looper looper2 = looper;
                        if (looper != null) {
                            NearbyConfiguration nearbyConfiguration = new NearbyConfiguration(5, wifiSsid, wifiPwd, wifiBand, null, serverPort, timeoutMs);
                            NearbyConfiguration configuration = nearbyConfiguration;
                            INearbyAdapter nearbyService3 = nearbyService2;
                            WifiStatusListenerTransport wifiStatusListenerTransport = new WifiStatusListenerTransport(NearbyConfig.BusinessTypeEnum.Token, 4, configuration, listener, looper2);
                            this.mApHostListenerTransport = wifiStatusListenerTransport;
                            try {
                                result = nearbyService3.registerConnectionListener(NearbyConfig.BusinessTypeEnum.Token.toNumber(), 4, configuration, this.mApHostListenerTransport);
                            } catch (RemoteException e) {
                                HwLog.e(TAG, "enableWifiAp registerConnectionListener ERROR:" + e.getLocalizedMessage());
                                this.mApHostListenerTransport.onStatusChange(-1);
                            }
                            if (!result) {
                                HwLog.e(TAG, "enableWifiAp registerConnectionListener ERROR");
                                throw new IllegalArgumentException("enableWifiAp registerConnectionListener error");
                            }
                        }
                    }
                    INearbyAdapter iNearbyAdapter = nearbyService2;
                }
                HwLog.e(TAG, "enableWifiAp NearbyService is null. createInstance nearby ERROR");
                throw new IllegalArgumentException("enableWifiAp createInstance nearby ERROR");
            } else {
                HwLog.e(TAG, "enableWifiAp WifiStatusListener already registered");
                throw new IllegalArgumentException("enableWifiAp WifiStatusListener already registered");
            }
        }
    }

    public void disableWifiAp() {
        synchronized (mCloneLock) {
            boolean result = false;
            HwLog.d(TAG, "disableWifiAp start");
            if (!(this.mApHostListenerTransport == null || this.mNearbyAdapter == null)) {
                INearbyAdapter nearbyService = this.mNearbyAdapter.getNearbyService();
                INearbyAdapter nearbyService2 = nearbyService;
                if (nearbyService != null) {
                    this.mApHostListenerTransport.quit();
                    try {
                        result = nearbyService2.unRegisterConnectionListener(this.mApHostListenerTransport);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "disableWifiAp unRegisterConnectionListener ERROR:" + e.getLocalizedMessage());
                    }
                    if (result) {
                        this.mApHostListenerTransport.waitQuit();
                    }
                    this.mApHostListenerTransport = null;
                    HwLog.i(TAG, "disableWifiAp " + result);
                    return;
                }
            }
            HwLog.e(TAG, "disableWifiAp already done");
        }
    }

    public void connectWifi(String wifiSsid, String wifiPwd, int wifiBand, int serverPort, WifiStatusListener listener, int timeoutMs) {
        synchronized (mCloneLock) {
            HwLog.i(TAG, "connectWifi start");
            boolean result = false;
            if (wifiSsid == null || wifiPwd == null || listener == null) {
                HwLog.e(TAG, "connectWifi wifiSsid\\wifiPwd or listener null");
                throw new IllegalArgumentException("connectWifi wifiSsid\\wifiPwd or listener null");
            } else if (this.mWifiSlaveListenerTransport == null) {
                if (this.mNearbyAdapter != null) {
                    INearbyAdapter nearbyService = this.mNearbyAdapter.getNearbyService();
                    INearbyAdapter nearbyService2 = nearbyService;
                    if (nearbyService != null) {
                        Looper looper = this.mNearbyAdapter.getLooper();
                        Looper looper2 = looper;
                        if (looper != null) {
                            NearbyDevice nearbyDevice = new NearbyDevice(wifiSsid, wifiPwd, wifiBand, null, serverPort);
                            INearbyAdapter nearbyService3 = nearbyService2;
                            WifiStatusListenerTransport wifiStatusListenerTransport = new WifiStatusListenerTransport(NearbyConfig.BusinessTypeEnum.Token, 4, nearbyDevice, listener, looper2);
                            this.mWifiSlaveListenerTransport = wifiStatusListenerTransport;
                            try {
                                result = nearbyService3.registerConnectionListener(NearbyConfig.BusinessTypeEnum.Token.toNumber(), 4, null, this.mWifiSlaveListenerTransport);
                            } catch (RemoteException e) {
                                HwLog.e(TAG, "connectWifi registerConnectionListener ERROR:" + e.getLocalizedMessage());
                                this.mWifiSlaveListenerTransport.onStatusChange(-1);
                            }
                            if (result) {
                                if (!this.mNearbyAdapter.open(NearbyConfig.BusinessTypeEnum.Token, 5, 4, nearbyDevice, timeoutMs)) {
                                    HwLog.e(TAG, "connectWifi open ERROR");
                                    this.mWifiSlaveListenerTransport.onStatusChange(-1);
                                }
                            } else {
                                HwLog.e(TAG, "connectWifi registerConnectionListener ERROR");
                                throw new IllegalArgumentException("connectWifi registerConnectionListener error");
                            }
                        }
                    }
                    INearbyAdapter iNearbyAdapter = nearbyService2;
                }
                HwLog.e(TAG, "connectWifi NearbyService is null, createInstance nearby ERROR");
                throw new IllegalArgumentException("connectWifi createInstance nearby ERROR");
            } else {
                HwLog.e(TAG, "connectWifi mWifiSlaveListenerTransport already registered");
                throw new IllegalArgumentException("connectWifi mWifiSlaveListenerTransport already registered");
            }
        }
    }

    public void disconnectWifi(boolean isNeedClose) {
        synchronized (mCloneLock) {
            HwLog.d(TAG, "disconnectWifi start");
            boolean result = false;
            if (!(this.mWifiSlaveListenerTransport == null || this.mNearbyAdapter == null)) {
                INearbyAdapter nearbyService = this.mNearbyAdapter.getNearbyService();
                INearbyAdapter nearbyService2 = nearbyService;
                if (nearbyService != null) {
                    WifiStatusListenerTransport transport = this.mWifiSlaveListenerTransport;
                    NearbyDevice nearbyDevice = transport.getNearbyDevice();
                    if (nearbyDevice != null) {
                        nearbyDevice.setNeedClose(isNeedClose);
                    }
                    transport.quit();
                    this.mNearbyAdapter.close(transport.getBusinessType(), transport.getBusinessId(), nearbyDevice);
                    try {
                        result = nearbyService2.unRegisterConnectionListener(transport);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "disconnectWifi unRegisterConnectionListener ERROR:" + e.getLocalizedMessage());
                    }
                    if (result) {
                        transport.waitQuit();
                    }
                    this.mWifiSlaveListenerTransport = null;
                    HwLog.i(TAG, "disconnectWifi " + result);
                    return;
                }
            }
            HwLog.e(TAG, "disconnectWifi already done");
        }
    }
}
