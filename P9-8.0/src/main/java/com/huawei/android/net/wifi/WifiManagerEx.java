package com.huawei.android.net.wifi;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiDetectConfInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.VoWifiSignalDetectInterruptCallback;
import java.util.List;

public class WifiManagerEx {
    public static List<String> getApLinkedStaList(WifiManager obj) {
        return HwFrameworkFactory.getHwInnerWifiManager().getApLinkedStaList();
    }

    public static void setSoftapMacFilter(WifiManager obj, String macFilter) {
        HwFrameworkFactory.getHwInnerWifiManager().setSoftapMacFilter(macFilter);
    }

    public static void setSoftapDisassociateSta(WifiManager obj, String mac) {
        HwFrameworkFactory.getHwInnerWifiManager().setSoftapDisassociateSta(mac);
    }

    public static boolean setWifiApMaxSCB(WifiManager obj, WifiConfiguration wifiConfig, int maxNum) {
        return false;
    }

    public static boolean isSupportConnectManager(WifiManager obj) {
        return true;
    }

    public static boolean isSupportChannel(WifiManager obj) {
        return true;
    }

    public static void userHandoverWifi(WifiManager obj) {
        HwFrameworkFactory.getHwInnerWifiManager().userHandoverWifi();
    }

    public static void setWifiApEvaluateEnabled(WifiManager obj, boolean enabled) {
        HwFrameworkFactory.getHwInnerWifiManager().setWifiApEvaluateEnabled(enabled);
    }

    public static boolean registerVoWifiSignalDetectInterrupt(WifiManager obj, VoWifiSignalDetectInterruptCallback callback) {
        return obj.registerVoWifiSignalDetectInterrupt(callback);
    }

    public static boolean unregisterVoWifiSignalDetectInterrupt(WifiManager obj) {
        return obj.unregisterVoWifiSignalDetectInterrupt();
    }

    public static boolean setVoWifiDetectMode(WifiManager obj, WifiDetectConfInfo info) {
        return obj.setVoWifiDetectMode(info);
    }

    public static byte[] fetchWifiSignalInfoForVoWiFi() {
        return HwFrameworkFactory.getHwInnerWifiManager().fetchWifiSignalInfoForVoWiFi();
    }

    public static WifiDetectConfInfo getVoWifiDetectMode() {
        return HwFrameworkFactory.getHwInnerWifiManager().getVoWifiDetectMode();
    }

    public static boolean setVoWifiDetectPeriod(int period) {
        return HwFrameworkFactory.getHwInnerWifiManager().setVoWifiDetectPeriod(period);
    }

    public static int getVoWifiDetectPeriod() {
        return HwFrameworkFactory.getHwInnerWifiManager().getVoWifiDetectPeriod();
    }

    public static boolean isSupportVoWifiDetect() {
        return HwFrameworkFactory.getHwInnerWifiManager().isSupportVoWifiDetect();
    }

    public static void enableHiLinkHandshake(boolean uiEnable, String bssid) {
        HwFrameworkFactory.getHwInnerWifiManager().enableHiLinkHandshake(uiEnable, bssid);
    }

    public static String getConnectionRawPsk() {
        return HwFrameworkFactory.getHwInnerWifiManager().getConnectionRawPsk();
    }

    public static boolean requestWifiEnable(boolean flag, String reason) {
        return HwFrameworkFactory.getHwInnerWifiManager().requestWifiEnable(flag, reason);
    }

    public static boolean setWifiTxPower(int power) {
        return HwFrameworkFactory.getHwInnerWifiManager().setWifiTxPower(power);
    }

    public static boolean startHwQoEMonitor(int monitorType, int period, IHwQoECallback callback) {
        return HwFrameworkFactory.getHwInnerWifiManager().startHwQoEMonitor(monitorType, period, callback);
    }

    public static boolean stopHwQoEMonitor(int monitorType) {
        return HwFrameworkFactory.getHwInnerWifiManager().stopHwQoEMonitor(monitorType);
    }

    public static boolean evaluateNetworkQuality(IHwQoECallback callback) {
        return HwFrameworkFactory.getHwInnerWifiManager().evaluateNetworkQuality(callback);
    }

    public static boolean updateVOWIFIStatus(int state) {
        return HwFrameworkFactory.getHwInnerWifiManager().updateVOWIFIStatus(state);
    }

    public static boolean updateAppRunningStatus(int uid, int type, int status, int scene, int reserved) {
        return HwFrameworkFactory.getHwInnerWifiManager().updateAppRunningStatus(uid, type, status, scene, reserved);
    }

    public static boolean updateAppExperienceStatus(int uid, int experience, long rtt, int reserved) {
        return HwFrameworkFactory.getHwInnerWifiManager().updateAppExperienceStatus(uid, experience, rtt, reserved);
    }

    public static void extendWifiScanPeriodForP2p(Context context, boolean bExtend, int iTimes) {
        HwFrameworkFactory.getHwInnerWifiManager().extendWifiScanPeriodForP2p(context, bExtend, iTimes);
    }
}
