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
    private static CloneAdapter mCloneAdapter;
    private static final Object mCloneLock = new Object();
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
            if (callback == null) {
                HwLog.e(TAG, "createInstance callback null");
                throw new IllegalArgumentException("createInstance callback null");
            }
            NearbyAdapter.createInstance(context, new NearbyAdapterCallback() {
                /* class com.huawei.nearbysdk.clone.CloneAdapter.AnonymousClass1 */

                @Override // com.huawei.nearbysdk.NearbyAdapterCallback, com.huawei.nearbysdk.NearbyAdapter.NAdapterGetCallback
                public void onAdapterGet(NearbyAdapter adapter) {
                    synchronized (CloneAdapter.mCloneLock) {
                        HwLog.w(CloneAdapter.TAG, "CloneAdapter onAdapterGet " + adapter);
                        if (adapter == null) {
                            CloneAdapter.releaseInstance();
                            callback.onAdapterGet(null);
                            return;
                        }
                        if (CloneAdapter.mCloneAdapter == null) {
                            CloneAdapter unused = CloneAdapter.mCloneAdapter = new CloneAdapter(adapter);
                        }
                        callback.onAdapterGet(CloneAdapter.mCloneAdapter);
                    }
                }

                @Override // com.huawei.nearbysdk.NearbyAdapterCallback
                public void onBinderDied() {
                    synchronized (CloneAdapter.mCloneLock) {
                        HwLog.w(CloneAdapter.TAG, "CloneAdapter onBinderDied");
                        CloneAdapter.releaseInstance();
                        callback.onBinderDied();
                    }
                }
            });
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
        INearbyAdapter nearbyService;
        Looper looper;
        synchronized (mCloneLock) {
            HwLog.i(TAG, "enableWifiAp start");
            boolean result = false;
            if (wifiSsid == null || wifiPwd == null || listener == null) {
                HwLog.e(TAG, "enableWifiAp wifiSsid\\wifiPwd or listener null");
                throw new IllegalArgumentException("enableWifiAp wifiSsid\\wifiPwd or listener null");
            } else if (this.mApHostListenerTransport != null) {
                HwLog.e(TAG, "enableWifiAp WifiStatusListener already registered");
                throw new IllegalArgumentException("enableWifiAp WifiStatusListener already registered");
            } else if (this.mNearbyAdapter == null || (nearbyService = this.mNearbyAdapter.getNearbyService()) == null || (looper = this.mNearbyAdapter.getLooper()) == null) {
                HwLog.e(TAG, "enableWifiAp NearbyService is null. createInstance nearby ERROR");
                throw new IllegalArgumentException("enableWifiAp createInstance nearby ERROR");
            } else {
                NearbyConfiguration configuration = new NearbyConfiguration(5, wifiSsid, wifiPwd, wifiBand, null, serverPort, timeoutMs);
                this.mApHostListenerTransport = new WifiStatusListenerTransport(NearbyConfig.BusinessTypeEnum.Token, 4, configuration, listener, looper);
                try {
                    result = nearbyService.registerConnectionListener(NearbyConfig.BusinessTypeEnum.Token.toNumber(), 4, configuration, this.mApHostListenerTransport);
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
    }

    public void disableWifiAp() {
        INearbyAdapter nearbyService;
        synchronized (mCloneLock) {
            boolean result = false;
            HwLog.d(TAG, "disableWifiAp start");
            if (this.mApHostListenerTransport == null || this.mNearbyAdapter == null || (nearbyService = this.mNearbyAdapter.getNearbyService()) == null) {
                HwLog.e(TAG, "disableWifiAp already done");
                return;
            }
            this.mApHostListenerTransport.quit();
            try {
                result = nearbyService.unRegisterConnectionListener(this.mApHostListenerTransport);
            } catch (RemoteException e) {
                HwLog.e(TAG, "disableWifiAp unRegisterConnectionListener ERROR:" + e.getLocalizedMessage());
            }
            if (result) {
                this.mApHostListenerTransport.waitQuit();
            }
            this.mApHostListenerTransport = null;
            HwLog.i(TAG, "disableWifiAp " + result);
        }
    }

    public void connectWifi(String wifiSsid, String wifiPwd, int wifiBand, int serverPort, WifiStatusListener listener, int timeoutMs) {
        INearbyAdapter nearbyService;
        Looper looper;
        synchronized (mCloneLock) {
            HwLog.i(TAG, "connectWifi start");
            boolean result = false;
            if (wifiSsid == null || wifiPwd == null || listener == null) {
                HwLog.e(TAG, "connectWifi wifiSsid\\wifiPwd or listener null");
                throw new IllegalArgumentException("connectWifi wifiSsid\\wifiPwd or listener null");
            } else if (this.mWifiSlaveListenerTransport != null) {
                HwLog.e(TAG, "connectWifi mWifiSlaveListenerTransport already registered");
                throw new IllegalArgumentException("connectWifi mWifiSlaveListenerTransport already registered");
            } else if (this.mNearbyAdapter == null || (nearbyService = this.mNearbyAdapter.getNearbyService()) == null || (looper = this.mNearbyAdapter.getLooper()) == null) {
                HwLog.e(TAG, "connectWifi NearbyService is null, createInstance nearby ERROR");
                throw new IllegalArgumentException("connectWifi createInstance nearby ERROR");
            } else {
                NearbyDevice device = new NearbyDevice(wifiSsid, wifiPwd, wifiBand, null, serverPort);
                this.mWifiSlaveListenerTransport = new WifiStatusListenerTransport(NearbyConfig.BusinessTypeEnum.Token, 4, device, listener, looper);
                try {
                    result = nearbyService.registerConnectionListener(NearbyConfig.BusinessTypeEnum.Token.toNumber(), 4, null, this.mWifiSlaveListenerTransport);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "connectWifi registerConnectionListener ERROR:" + e.getLocalizedMessage());
                    this.mWifiSlaveListenerTransport.onStatusChange(-1);
                }
                if (!result) {
                    HwLog.e(TAG, "connectWifi registerConnectionListener ERROR");
                    throw new IllegalArgumentException("connectWifi registerConnectionListener error");
                } else if (!this.mNearbyAdapter.open(NearbyConfig.BusinessTypeEnum.Token, 5, 4, device, timeoutMs)) {
                    HwLog.e(TAG, "connectWifi open ERROR");
                    this.mWifiSlaveListenerTransport.onStatusChange(-1);
                }
            }
        }
    }

    public void disconnectWifi(boolean isNeedClose) {
        INearbyAdapter nearbyService;
        synchronized (mCloneLock) {
            HwLog.d(TAG, "disconnectWifi start");
            boolean result = false;
            if (this.mWifiSlaveListenerTransport == null || this.mNearbyAdapter == null || (nearbyService = this.mNearbyAdapter.getNearbyService()) == null) {
                HwLog.e(TAG, "disconnectWifi already done");
                return;
            }
            WifiStatusListenerTransport transport = this.mWifiSlaveListenerTransport;
            NearbyDevice nearbyDevice = transport.getNearbyDevice();
            if (nearbyDevice != null) {
                nearbyDevice.setNeedClose(isNeedClose);
            }
            transport.quit();
            this.mNearbyAdapter.close(transport.getBusinessType(), transport.getBusinessId(), nearbyDevice);
            try {
                result = nearbyService.unRegisterConnectionListener(transport);
            } catch (RemoteException e) {
                HwLog.e(TAG, "disconnectWifi unRegisterConnectionListener ERROR:" + e.getLocalizedMessage());
            }
            if (result) {
                transport.waitQuit();
            }
            this.mWifiSlaveListenerTransport = null;
            HwLog.i(TAG, "disconnectWifi " + result);
        }
    }
}
