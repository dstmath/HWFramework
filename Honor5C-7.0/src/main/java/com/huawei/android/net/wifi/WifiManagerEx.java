package com.huawei.android.net.wifi;

import android.common.HwFrameworkFactory;
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
}
