package com.huawei.wifi2;

import android.content.Context;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import com.huawei.wifi2.HwWifi2ClientModeManager;

public class HwWifi2Injector {
    private static HwWifi2Injector sWifiInjector = null;
    private final HwWifi2ActiveModeWarden mActiveModeWarden;
    private final HwWifi2ClientModeImpl mClientModeImpl;
    private final HwWifi2Clock mClock = new HwWifi2Clock();
    private final Context mContext;
    private HwWifi2HalDeviceManager mHalDeviceManager;
    private final HwWifi2Service mHwWifi2Service;
    private final INetworkManagementService mNwManagementService;
    private final HwWifi2SupplicantStaIfaceHal mSupplicantStaIfaceHal;
    private HwWifi2ConnectivityManager mWifiConnectivityManager;
    private final HwWifi2Controller mWifiController;
    private final HandlerThread mWifiCoreHandlerThread;
    private final HwWifi2Monitor mWifiMonitor;
    private final HwWifi2Native mWifiNative;
    private final HwWifi2NetworkSelector mWifiNetworkSelector;
    private final HandlerThread mWifiServiceHandlerThread;
    private final HwWifi2VendorHal mWifiVendorHal;
    private final HwWifi2CondControl mWificondControl;

    public HwWifi2Injector(Context context, HwWifi2Service hwWifi2Service) {
        if (context == null) {
            throw new IllegalStateException("WifiInjector should not be initialized with a null Context.");
        } else if (sWifiInjector == null) {
            sWifiInjector = this;
            this.mHwWifi2Service = hwWifi2Service;
            this.mContext = context;
            this.mWifiServiceHandlerThread = new HandlerThread("HwWifi2Service");
            this.mWifiServiceHandlerThread.start();
            this.mWifiCoreHandlerThread = new HandlerThread("HwWifi2ClientModeImpl");
            this.mWifiCoreHandlerThread.start();
            Looper clientModeImplLooper = this.mWifiCoreHandlerThread.getLooper();
            this.mWifiMonitor = new HwWifi2Monitor();
            this.mHalDeviceManager = new HwWifi2HalDeviceManager();
            this.mWifiVendorHal = new HwWifi2VendorHal(this.mHalDeviceManager, clientModeImplLooper);
            this.mSupplicantStaIfaceHal = new HwWifi2SupplicantStaIfaceHal(this.mContext, this.mWifiMonitor, clientModeImplLooper);
            this.mWificondControl = new HwWifi2CondControl(clientModeImplLooper);
            this.mWifiNetworkSelector = new HwWifi2NetworkSelector(this.mContext);
            this.mNwManagementService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
            this.mWifiNative = new HwWifi2Native(this.mWifiVendorHal, this.mSupplicantStaIfaceHal, this.mWificondControl, this.mWifiMonitor, this.mNwManagementService, new Handler(clientModeImplLooper));
            this.mActiveModeWarden = new HwWifi2ActiveModeWarden(this, this.mContext, clientModeImplLooper, this.mWifiNative);
            this.mClientModeImpl = new HwWifi2ClientModeImpl(context, clientModeImplLooper, this, this.mWifiNative);
            this.mWifiController = new HwWifi2Controller(this.mContext, this.mClientModeImpl, clientModeImplLooper, this.mWifiServiceHandlerThread.getLooper(), this.mActiveModeWarden);
            this.mClientModeImpl.start();
            HwWifi2ChrManager.initHwWifi2ChrManager();
        } else {
            throw new IllegalStateException("WifiInjector was already created, use getInstance instead.");
        }
    }

    public static HwWifi2Injector getInstance() {
        return sWifiInjector;
    }

    public HwWifi2ClientModeImpl getClientModeImpl() {
        return this.mClientModeImpl;
    }

    public HwWifi2Native getWifiNative() {
        return this.mWifiNative;
    }

    public HwWifi2ActiveModeWarden getActiveModeWarden() {
        return this.mActiveModeWarden;
    }

    public HwWifi2Controller getWifiController() {
        return this.mWifiController;
    }

    public HwWifi2ConnectivityManager getWifiConnectivityManager() {
        return this.mWifiConnectivityManager;
    }

    public HwWifi2NetworkSelector getWifiNetworkSelector() {
        return this.mWifiNetworkSelector;
    }

    public HandlerThread getWifiCoreHandlerThread() {
        return this.mWifiCoreHandlerThread;
    }

    public HandlerThread getWifiServiceHandlerThread() {
        return this.mWifiServiceHandlerThread;
    }

    public HwWifi2Monitor getWifiMonitor() {
        return this.mWifiMonitor;
    }

    public TelephonyManager makeTelephonyManager() {
        if (this.mContext.getSystemService("phone") instanceof TelephonyManager) {
            return (TelephonyManager) this.mContext.getSystemService("phone");
        }
        return null;
    }

    public HwWifi2Service getHwWifi2Service() {
        return this.mHwWifi2Service;
    }

    public HwWifi2Clock getClock() {
        return this.mClock;
    }

    public HwWifi2NetworkFactory makeHwWifi2NetworkFactory(NetworkCapabilities nc) {
        return new HwWifi2NetworkFactory(this.mWifiCoreHandlerThread.getLooper(), this.mContext, nc, this);
    }

    public HwWifi2ClientModeManager makeClientModeManager(HwWifi2ClientModeManager.Listener listener) {
        return new HwWifi2ClientModeManager(this.mContext, this.mWifiCoreHandlerThread.getLooper(), this.mWifiNative, listener, this.mClientModeImpl);
    }

    public HwWifi2ConnectivityManager makeHwWifi2ConnectivityManager(HwWifi2ClientModeImpl hwWifi2ClientModeImpl) {
        this.mWifiConnectivityManager = new HwWifi2ConnectivityManager(this.mContext, this.mWifiCoreHandlerThread.getLooper(), this.mWifiNetworkSelector, hwWifi2ClientModeImpl, this.mHwWifi2Service);
        return this.mWifiConnectivityManager;
    }
}
