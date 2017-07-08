package com.android.server.wifi.ABS;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.HwWifiStatStore;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiStateMachine;
import java.io.IOException;
import java.util.List;

public class HwABSWiFiHandler {
    private Context mContext;
    private Handler mHandler;
    private HwABSDataBaseManager mHwABSDataBaseManager;
    private int mNowCapability;
    private Runnable mRunnable;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;
    private HwWifiStatStore mWifiStatStore;
    private WifiStateMachine mWifiStateMachine;

    public HwABSWiFiHandler(Context context, Handler handler, WifiStateMachine wifiStateMachine) {
        this.mNowCapability = 2;
        this.mRunnable = new Runnable() {
            public void run() {
                while (!HwABSWiFiHandler.this.ping(HwABSWiFiHandler.this.getWayIpAddress())) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                HwABSWiFiHandler.this.mHandler.sendEmptyMessage(17);
            }
        };
        this.mContext = context;
        this.mHandler = handler;
        this.mWifiStateMachine = wifiStateMachine;
        this.mWifiNative = WifiNative.getWlanNativeInterface();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mHwABSDataBaseManager = HwABSDataBaseManager.getInstance(context);
        setAPCapability(2);
        this.mWifiStatStore = HwWifiServiceFactory.getHwWifiStatStore();
    }

    private void handoverToMIMO() {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo != null && mWifiInfo.getBSSID() != null) {
            setAPCapability(2);
            hwABSReconnectHandover();
        }
    }

    private void handoverToSISO() {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo != null && mWifiInfo.getBSSID() != null) {
            setAPCapability(1);
            hwABSReconnectHandover();
        }
    }

    public void hwABSHandover(int capability) {
        if (this.mNowCapability == capability) {
            HwABSUtils.logD("hwABSHandover, the same with current capability");
            return;
        }
        if (2 == capability) {
            handoverToMIMO();
        } else {
            handoverToSISO();
        }
    }

    private void hwABSSoftHandover(int type) {
        setAPCapability(type);
        this.mWifiNative.hwABSSoftHandover(type);
        this.mHandler.sendEmptyMessageDelayed(16, 200);
    }

    private boolean hwABSReconnectHandover() {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo == null) {
            return false;
        }
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            HwABSUtils.logD("HwABSHandover, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
            return false;
        }
        WifiConfiguration changeConfig = null;
        for (WifiConfiguration nextConfig : configNetworks) {
            if (mWifiInfo.getNetworkId() == nextConfig.networkId) {
                changeConfig = nextConfig;
                break;
            }
        }
        if (changeConfig == null) {
            HwABSUtils.logD("HwABSHandover, WifiConfiguration is null ");
            return false;
        }
        if (this.mWifiStatStore != null) {
            this.mWifiStatStore.updateAssocByABS();
        }
        this.mWifiStateMachine.autoRoamToNetwork(changeConfig.networkId, null);
        return true;
    }

    public void hwABSHandoverInScreenOff() {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo != null && mWifiInfo.getBSSID() != null) {
            HwABSApInfoData mHwABSApInfoData = this.mHwABSDataBaseManager.getApInfoByBssid(mWifiInfo.getBSSID());
            if (mHwABSApInfoData != null) {
                if (mHwABSApInfoData.mSwitch_siso_type == 0 || mHwABSApInfoData.mSwitch_siso_type == 1) {
                    hwABSSoftHandover(1);
                } else {
                    hwABSHandover(1);
                }
            }
        }
    }

    public void setAPCapability(int capability) {
        HwABSUtils.logD("setAPCapability, capability = " + capability);
        this.mNowCapability = capability;
        this.mWifiNative.hwABSSetCapability(capability);
    }

    public int getCurrentCapability() {
        return this.mNowCapability;
    }

    public void hwABScheckLinked() {
        new Thread(this.mRunnable).start();
    }

    private boolean ping(String target) {
        if (target == null) {
            return false;
        }
        String getwayIPs = target;
        try {
            Process process = Runtime.getRuntime().exec("ping -c 1 -w 10 " + target);
            int status = process.waitFor();
            process.destroy();
            if (status == 0) {
                HwABSUtils.logD("ping " + target + "  succeed");
                return true;
            }
            HwABSUtils.logD("ping " + target + "  false");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public String getWayIpAddress() {
        DhcpInfo di = this.mWifiManager.getDhcpInfo();
        if (di == null) {
            return null;
        }
        return long2ip((long) di.gateway);
    }

    String long2ip(long ip) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf((int) (ip & 255)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 8) & 255)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 16) & 255)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 24) & 255)));
        return sb.toString();
    }
}
