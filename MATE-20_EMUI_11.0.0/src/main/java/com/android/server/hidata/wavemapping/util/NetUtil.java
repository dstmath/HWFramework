package com.android.server.hidata.wavemapping.util;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.huawei.lcagent.client.LogCollectManager;
import java.util.List;

public class NetUtil {
    private static final int DEFAULT_LENGTH = 6;
    private static final int DEFAULT_TRAFFIC_LENGTH = 2;
    private static final int INVALID_VALUE = -1;
    private static final String KEY_CELL_FREQ = "cellFreq";
    private static final String KEY_CELL_ID = "cellId";
    private static final String KEY_CELL_RAT = "cellRAT";
    private static final String KEY_CELL_RSSI = "cellRssi";
    private static final String KEY_CELL_SERVICE = "cellService";
    private static final String KEY_CELL_STATE = "cellState";
    private static final String KEY_DATA_STATE = "dataState";
    private static final String KEY_DEFAULT_NETWORK_FREQ = "defaultNwFreq";
    private static final String KEY_DEFAULT_NETWORK_ID = "defaultNwId";
    private static final String KEY_DEFAULT_NETWORK_NAME = "defaultNwName";
    private static final String KEY_DEFAULT_TYPE = "defaultType";
    private static final String KEY_DISABLED = "DISABLED";
    private static final String KEY_DISABLING = "DISABLING";
    private static final String KEY_ENABLED = "ENABLED";
    private static final String KEY_ENABLING = "ENABLING";
    private static final String KEY_MOBILE = "mobile";
    private static final String KEY_PREFERRED_MODE = "preferredMode";
    private static final String KEY_UNKNOWN = "UNKNOWN";
    private static final String KEY_WIFI = "wifi";
    private static final String KEY_WIFI_AP = "wifiAp";
    private static final String KEY_WIFI_CH = "wifiCh";
    private static final String KEY_WIFI_LS = "wifiLS";
    private static final String KEY_WIFI_MAC = "wifiMAC";
    private static final String KEY_WIFI_RSSI = "wifiRssi";
    private static final String KEY_WIFI_STATE = "wifiState";
    private static final String TAG = ("WMapping." + NetUtil.class.getSimpleName());
    public static final String UNKNOWN_STR = "UNKNOWN";
    private static String cellFreq = "UNKNOWN";
    private static String cellId = "UNKNOWN";
    private static String cellPlmn = "";
    private static String cellRat = "UNKNOWN";
    private static String cellRssi = "UNKNOWN";
    private static String dataSwitch = "";
    private static int defaultConnectedType = 8;
    private static boolean isMobileConnected = false;
    private static boolean isWifiConnected = false;
    private static String networkFreq = "UNKNOWN";
    private static String networkId = "UNKNOWN";
    private static String networkName = "UNKNOWN";
    private static int simpleType = 0;
    private static int targetIdx = 0;

    private NetUtil() {
    }

    public static Bundle getWifiStateString(Context context) {
        Bundle output = new Bundle();
        try {
            WifiManager myWifiManager = (WifiManager) context.getSystemService(KEY_WIFI);
            if (myWifiManager == null) {
                LogUtil.d(false, "getWifiStateString:myWifiManager == null", new Object[0]);
                return output;
            }
            ConnectivityManager myConMgr = (ConnectivityManager) context.getSystemService("connectivity");
            if (myConMgr == null) {
                LogUtil.d(false, "getWifiStateString:myConMgr == null", new Object[0]);
                return output;
            }
            String wifiStateStr = getWifiStateStr(myWifiManager);
            if (!checkWifiConnection(myConMgr)) {
                LogUtil.d(false, "wifi NOT connected", new Object[0]);
                wifiStateStr = "DISCONNECTED";
            }
            writeToBundle(myWifiManager, output);
            output.putString(KEY_WIFI_STATE, wifiStateStr);
            if (!TextUtils.isEmpty(output.getString(KEY_WIFI_AP, "UNKNOWN")) && !TextUtils.isEmpty(output.getString(KEY_WIFI_STATE, "UNKNOWN"))) {
                LogUtil.i(false, " getWifiStateString, current Ssid=%{private}s, state=%{public}s", output.getString(KEY_WIFI_AP, "UNKNOWN"), output.getString(KEY_WIFI_STATE, "UNKNOWN"));
            }
            return output;
        } catch (RuntimeException e) {
            LogUtil.e(false, "getWifiStateString: RuntimeException ", new Object[0]);
        } catch (Exception e2) {
            LogUtil.e(false, "getWifiStateString failed by Exception", new Object[0]);
        }
    }

    private static void writeToBundle(WifiManager wifiManager, Bundle bundle) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getBSSID() != null) {
            String wifiAp = wifiInfo.getSSID();
            String wifiMac = wifiInfo.getBSSID();
            String wifiCh = Integer.toString(wifiInfo.getFrequency());
            String wifiLinkSpeed = Integer.toString(wifiInfo.getLinkSpeed());
            String wifiRssi = Integer.toString(wifiInfo.getRssi());
            bundle.putString(KEY_WIFI_AP, wifiAp);
            bundle.putString(KEY_WIFI_MAC, wifiMac);
            bundle.putString(KEY_WIFI_CH, wifiCh);
            bundle.putString(KEY_WIFI_LS, wifiLinkSpeed);
            bundle.putString(KEY_WIFI_RSSI, wifiRssi);
        }
    }

    private static String getWifiStateStr(WifiManager wifiManager) {
        int wifiState = wifiManager.getWifiState();
        if (wifiState == 0) {
            return KEY_DISABLING;
        }
        if (wifiState == 1) {
            return KEY_DISABLED;
        }
        if (wifiState == 2) {
            return KEY_ENABLING;
        }
        if (wifiState != 3) {
            return "UNKNOWN";
        }
        return KEY_ENABLED;
    }

    private static boolean checkWifiConnection(ConnectivityManager connectivityManager) {
        Network[] networks = connectivityManager.getAllNetworks();
        if (networks == null || networks.length == 0) {
            return false;
        }
        LogUtil.i(false, "networks size is %{public}d", Integer.valueOf(networks.length));
        for (Network network : networks) {
            NetworkInfo netInfo = connectivityManager.getNetworkInfo(network);
            if (netInfo == null) {
                return false;
            }
            LogUtil.v(false, "networksInfo is %{public}s", netInfo.toString());
            if (netInfo.getType() == 1 && netInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public static String getMobileDataScrbId(Context context, LogCollectManager collectManager) {
        String scribId;
        String scriptionId = "NA";
        if (collectManager == null) {
            try {
                LogUtil.w(false, " no mCollectManger", new Object[0]);
                return scriptionId;
            } catch (RuntimeException e) {
                LogUtil.e(false, "getMobileDataSubId: RuntimeException ", new Object[0]);
            } catch (Exception e2) {
                LogUtil.e(false, "getMobileDataSubId failed by Exception", new Object[0]);
            }
        } else {
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (defaultTelephonyManager == null) {
                LogUtil.w(false, " no defaultTelephonyManager", new Object[0]);
                return scriptionId;
            }
            int subId = SubscriptionManager.getDefaultDataSubscriptionId();
            if (subId == -1) {
                subId = SubscriptionManager.getDefaultSubscriptionId();
                LogUtil.w(false, " no default DATA sub: %{public}d", Integer.valueOf(subId));
            }
            if (subId == -1) {
                LogUtil.w(false, " no default sub: %{public}d", Integer.valueOf(subId));
                return scriptionId;
            }
            int phoneType = TelephonyManager.getPhoneType(subId);
            if (phoneType == 1) {
                String scribId2 = defaultTelephonyManager.getSubscriberId(subId);
                if (scribId2 != null) {
                    scriptionId = collectManager.doEncrypt(scribId2);
                }
            } else if (phoneType == 2 && (scribId = defaultTelephonyManager.getMeid(subId)) != null) {
                scriptionId = collectManager.doEncrypt(scribId);
            }
            LogUtil.i(false, "getMobileDataSubId: %{public}s, subId=%{public}d, phoneType=%{public}d", scriptionId, Integer.valueOf(subId), Integer.valueOf(phoneType));
            return scriptionId;
        }
    }

    public static Bundle getMobileDataState(Context context) {
        Bundle output = new Bundle();
        try {
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (defaultTelephonyManager == null) {
                LogUtil.w(false, " no defaultTelephonyManager", new Object[0]);
                return output;
            }
            int subId = SubscriptionManager.getDefaultDataSubscriptionId();
            if (subId == -1) {
                subId = SubscriptionManager.getDefaultSubscriptionId();
                LogUtil.w(false, " no default DATA sub", new Object[0]);
            }
            if (subId == -1) {
                LogUtil.w(false, " no default sub", new Object[0]);
                return output;
            }
            TelephonyManager subTelephonyManager = defaultTelephonyManager.createForSubscriptionId(subId);
            if (subTelephonyManager == null) {
                LogUtil.e(false, " no TelephonyManager, subId:%{public}d", Integer.valueOf(subId));
                return output;
            }
            putParamsToBundle(defaultTelephonyManager, subTelephonyManager, output, subId);
            return output;
        } catch (RuntimeException e) {
            LogUtil.e(false, "getMobileDataState: RuntimeException ", new Object[0]);
        } catch (Exception e2) {
            LogUtil.e(false, "getMobileDataState failed by Exception", new Object[0]);
        }
    }

    private static void putParamsToBundle(TelephonyManager defaultTelephonyManager, TelephonyManager subTelephonyManager, Bundle output, int subId) {
        cellId = "UNKNOWN";
        cellFreq = "UNKNOWN";
        cellRssi = "UNKNOWN";
        cellRat = "UNKNOWN";
        cellPlmn = "";
        if (defaultTelephonyManager.isDataEnabled()) {
            dataSwitch = KEY_ENABLED;
        } else {
            dataSwitch = KEY_DISABLED;
        }
        int preferredMode = defaultTelephonyManager.getPreferredNetworkType(subId);
        int serviceState = subTelephonyManager.getServiceState().getState();
        int dataState = subTelephonyManager.getDataState();
        String nwPlmn = subTelephonyManager.getNetworkOperator();
        if (nwPlmn == null) {
            LogUtil.d(false, " no PLMN, subId:%{public}d", Integer.valueOf(subId));
            nwPlmn = "UNKNOWN";
        }
        int nwType = subTelephonyManager.getDataNetworkType();
        simpleType = nwType;
        processTypeAndRat(nwType);
        processTargetId(defaultTelephonyManager, subTelephonyManager, subId);
        processCellInfo(defaultTelephonyManager, nwPlmn);
        output.putString(KEY_CELL_STATE, dataSwitch);
        output.putInt(KEY_DATA_STATE, dataState);
        output.putString(KEY_CELL_ID, cellId);
        output.putString(KEY_CELL_FREQ, cellFreq);
        output.putString(KEY_CELL_RSSI, cellRssi);
        output.putString(KEY_CELL_RAT, cellRat);
        output.putInt(KEY_CELL_SERVICE, serviceState);
        output.putInt(KEY_PREFERRED_MODE, preferredMode);
        LogUtil.i(false, " getMobileDataState: subId=%{public}d, nwType=%{public}d, cellRat=%{private}s, cellFreq=%{public}s, cellState=%{public}s, dataState=%{public}d, cellService=%{public}d, preferredMode=%{public}d", Integer.valueOf(subId), Integer.valueOf(nwType), cellRat, cellFreq, dataSwitch, Integer.valueOf(dataState), Integer.valueOf(serviceState), Integer.valueOf(preferredMode));
        LogUtil.v(false, " getMobileDataState: cellId=%{private}s, PLMN=%{private}s", cellId, nwPlmn);
    }

    private static void processTypeAndRat(int nwType) {
        switch (nwType) {
            case 1:
            case 2:
            case 16:
                cellRat = "2G";
                simpleType = 16;
                return;
            case 3:
            case 8:
            case 9:
            case 10:
            case 15:
            case 17:
                cellRat = "3G";
                simpleType = 3;
                return;
            case 4:
            case 7:
                cellRat = "CDMA";
                simpleType = 4;
                return;
            case 5:
            case 6:
            case 12:
            case 14:
                cellRat = "C3G";
                simpleType = 4;
                return;
            case 11:
            default:
                cellRat = "UNKNOWN";
                simpleType = 0;
                return;
            case 13:
                cellRat = "4G";
                simpleType = 13;
                return;
        }
    }

    private static void processCellInfo(TelephonyManager defaultTelephonyManager, String nwPlmn) {
        List<CellInfo> cellInfos = defaultTelephonyManager.getAllCellInfo();
        if (cellInfos == null || cellInfos.size() == 0) {
            LogUtil.i(false, " no cell Info ", new Object[0]);
        } else {
            getCellInfo(cellInfos, nwPlmn);
        }
    }

    private static void getCellInfo(List<CellInfo> cellInfos, String nwPlmn) {
        int cellNum = cellInfos.size();
        for (int i = 0; i < cellNum; i++) {
            CellInfo cellInfo = cellInfos.get(i);
            LogUtil.v(false, "cell Info(%{public}d): %{private}s", Integer.valueOf(i), cellInfo.toString());
            if (cellInfo.isRegistered()) {
                int i2 = targetIdx;
                if (i2 == 0) {
                    return;
                }
                if (i2 > 1) {
                    targetIdx = i2 - 1;
                } else if (isEqualPlmn(cellInfo, nwPlmn)) {
                    return;
                }
            }
        }
    }

    private static boolean isEqualPlmn(CellInfo cellInfo, String nwPlmn) {
        if (simpleType == 16 && (cellInfo instanceof CellInfoGsm)) {
            CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
            CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
            cellPlmn = cellIdentityGsm.getMccString() + cellIdentityGsm.getMncString();
            LogUtil.i(false, "PLMN(%{private}s)", cellPlmn);
            if (cellPlmn.equals(nwPlmn)) {
                LogUtil.i(false, "PLMN the same", new Object[0]);
                cellRssi = Integer.toString(cellInfoGsm.getCellSignalStrength().getDbm());
                cellId = Integer.toString(cellIdentityGsm.getCid());
                cellFreq = Integer.toString(cellIdentityGsm.getArfcn());
                return true;
            }
        } else if (simpleType == 13 && (cellInfo instanceof CellInfoLte)) {
            CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
            CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
            cellPlmn = cellIdentityLte.getMccString() + cellIdentityLte.getMncString();
            LogUtil.i(false, "PLMN(%{private}s)", cellPlmn);
            if (cellPlmn.equals(nwPlmn)) {
                LogUtil.i(false, "PLMN the same", new Object[0]);
                cellRssi = Integer.toString(cellInfoLte.getCellSignalStrength().getDbm());
                cellId = Integer.toString(cellIdentityLte.getCi());
                cellFreq = Integer.toString(cellIdentityLte.getEarfcn());
                return true;
            }
        } else if (simpleType == 3 && (cellInfo instanceof CellInfoWcdma)) {
            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
            CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
            cellPlmn = cellIdentityWcdma.getMccString() + cellIdentityWcdma.getMncString();
            LogUtil.i(false, "PLMN(%{private}s)", cellPlmn);
            if (cellPlmn.equals(nwPlmn)) {
                LogUtil.i(false, "PLMN the same", new Object[0]);
                cellRssi = Integer.toString(cellInfoWcdma.getCellSignalStrength().getDbm());
                cellId = Integer.toString(cellIdentityWcdma.getCid());
                cellFreq = Integer.toString(cellIdentityWcdma.getUarfcn());
                return true;
            }
        } else if (simpleType != 4 || !(cellInfo instanceof CellInfoCdma)) {
            LogUtil.d(false, "not GWLC", new Object[0]);
        } else {
            CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
            cellRssi = Integer.toString(cellInfoCdma.getCellSignalStrength().getDbm());
            cellId = Integer.toString(cellInfoCdma.getCellIdentity().getBasestationId());
            return true;
        }
        return false;
    }

    private static void processTargetId(TelephonyManager defaultTelephonyManager, TelephonyManager subTelephonyManager, int subId) {
        int phoneCnt = subTelephonyManager.getPhoneCount();
        int regCnt = 0;
        targetIdx = 0;
        for (int k = 0; k < phoneCnt; k++) {
            TelephonyManager subTel = defaultTelephonyManager.createForSubscriptionId(k);
            if (subTel != null) {
                int regV = subTel.getServiceState().getVoiceRegState();
                int regD = subTel.getServiceState().getDataRegState();
                LogUtil.i(false, "RegState(%{public}d): voice=%{public}d, data=%{public}d", Integer.valueOf(k), Integer.valueOf(regV), Integer.valueOf(regD));
                if (regV == 0 || regD == 0) {
                    regCnt++;
                    if (k == subId) {
                        targetIdx = regCnt;
                        return;
                    }
                }
            }
        }
    }

    public static String getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm == null) {
            return "UNKNOWN";
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            return activeNetwork.getTypeName();
        }
        return "UNKNOWN";
    }

    public static int getNetworkTypeInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm == null) {
            return -1;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            return activeNetwork.getType();
        }
        return 8;
    }

    public static Bundle getConnectedNetworkState(Context context) {
        Bundle output = new Bundle();
        isMobileConnected = false;
        isWifiConnected = false;
        defaultConnectedType = 8;
        networkId = "UNKNOWN";
        networkName = "UNKNOWN";
        networkFreq = "UNKNOWN";
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm != null) {
            judgeNetworkState(cm);
            NetworkInfo defaultInfo = cm.getActiveNetworkInfo();
            if (defaultInfo == null) {
                LogUtil.d(false, "no active network", new Object[0]);
            } else if (!isNetworkExist(context, defaultInfo)) {
                return output;
            }
        }
        output.putBoolean(KEY_MOBILE, isMobileConnected);
        output.putBoolean(KEY_WIFI, isWifiConnected);
        output.putInt(KEY_DEFAULT_TYPE, defaultConnectedType);
        output.putString(KEY_DEFAULT_NETWORK_ID, networkId);
        output.putString(KEY_DEFAULT_NETWORK_NAME, networkName);
        output.putString(KEY_DEFAULT_NETWORK_FREQ, networkFreq);
        return output;
    }

    private static void judgeNetworkState(ConnectivityManager cm) {
        NetworkInfo netInfo;
        Network[] networks = cm.getAllNetworks();
        if (networks != null) {
            LogUtil.i(false, "networks size is %{public}d", Integer.valueOf(networks.length));
            int i = 0;
            while (i < networks.length && (netInfo = cm.getNetworkInfo(networks[i])) != null) {
                LogUtil.v(false, "networksInfo is %{public}s", netInfo.toString());
                if (netInfo.isConnected()) {
                    if (netInfo.getType() == 0) {
                        LogUtil.i(false, "mobile network is connected", new Object[0]);
                        isMobileConnected = true;
                    } else if (netInfo.getType() == 1) {
                        LogUtil.i(false, "wifi network is connected", new Object[0]);
                        isWifiConnected = true;
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    private static boolean isNetworkExist(Context context, NetworkInfo defaultInfo) {
        if (defaultInfo.getState() == NetworkInfo.State.CONNECTED && defaultInfo.isAvailable()) {
            defaultConnectedType = defaultInfo.getType();
            if (defaultConnectedType == 1) {
                Bundle wifiState = getWifiStateString(context);
                if (wifiState == null) {
                    LogUtil.e(false, "wifiState is null", new Object[0]);
                    return false;
                } else if (wifiState.getString(KEY_WIFI_MAC, "UNKNOWN") != null) {
                    networkId = wifiState.getString(KEY_WIFI_MAC, "UNKNOWN");
                    networkName = wifiState.getString(KEY_WIFI_AP, "UNKNOWN");
                    networkFreq = wifiState.getString(KEY_WIFI_CH, "UNKNOWN");
                }
            }
            if (defaultConnectedType == 0) {
                Bundle mobileState = getMobileDataState(context);
                if (mobileState == null) {
                    LogUtil.e(false, "mobileState is null", new Object[0]);
                    return false;
                } else if (mobileState.getString(KEY_CELL_ID, "UNKNOWN") != null) {
                    networkId = mobileState.getString(KEY_CELL_ID, "UNKNOWN");
                    networkName = mobileState.getString(KEY_CELL_RAT, "UNKNOWN");
                    networkFreq = mobileState.getString(KEY_CELL_FREQ, "UNKNOWN");
                }
            }
            LogUtil.i(false, " network is connected, default type = %{public}d, Name = %{public}s", Integer.valueOf(defaultConnectedType), networkName);
        }
        return true;
    }

    public static boolean isMobileCallStateIdle(Context context) {
        try {
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (defaultTelephonyManager == null) {
                LogUtil.w(false, " no defaultTelephonyManager", new Object[0]);
                return false;
            }
            int subId = SubscriptionManager.getDefaultDataSubscriptionId();
            if (subId == -1) {
                subId = SubscriptionManager.getDefaultSubscriptionId();
                LogUtil.w(false, " no default DATA sub", new Object[0]);
            }
            if (subId == -1) {
                LogUtil.w(false, " no default sub", new Object[0]);
                return false;
            }
            TelephonyManager subTelephonyManager = defaultTelephonyManager.createForSubscriptionId(subId);
            if (subTelephonyManager == null) {
                LogUtil.e(false, " no TelephonyManager", new Object[0]);
                return false;
            }
            int mCallState = subTelephonyManager.getCallState();
            LogUtil.i(false, " getMobileCallState, current call state=%{public}d, subId=%{public}d", Integer.valueOf(mCallState), Integer.valueOf(subId));
            if (mCallState == 0) {
                return true;
            }
            return false;
        } catch (RuntimeException e) {
            LogUtil.e(false, "getMobileCallState: RuntimeException ", new Object[0]);
            return false;
        } catch (Exception e2) {
            LogUtil.e(false, "getMobileCallState failed by Exception", new Object[0]);
            return false;
        }
    }

    public static boolean isWifiEnabled(Context context) {
        try {
            WifiManager myWifiManager = (WifiManager) context.getSystemService(KEY_WIFI);
            if (myWifiManager != null) {
                return myWifiManager.isWifiEnabled();
            }
            LogUtil.d(false, "isWifiEnabled:myWifiManager == null", new Object[0]);
            return false;
        } catch (RuntimeException e) {
            LogUtil.e(false, "isWifiEnabled: RuntimeException ", new Object[0]);
            return false;
        } catch (Exception e2) {
            LogUtil.e(false, "isWifiEnabled failed by Exception", new Object[0]);
            return false;
        }
    }

    public static long[] getTraffic(long startTime, long endTime, int network, Context context) {
        int subId;
        long[] traffic = new long[2];
        if (network != 0 && network != 1) {
            LogUtil.e(false, "network is invalid:%{public}d", Integer.valueOf(network));
            return traffic;
        } else if (endTime < startTime) {
            LogUtil.w(false, "Time is invalid: start=%{public}s, end=%{public}s", String.valueOf(startTime), String.valueOf(endTime));
            return traffic;
        } else {
            NetworkStatsManager mNetworkStatsManager = (NetworkStatsManager) context.getSystemService("netstats");
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (defaultTelephonyManager == null) {
                LogUtil.w(false, " no defaultTelephonyManager", new Object[0]);
                return traffic;
            } else if (mNetworkStatsManager == null) {
                LogUtil.w(false, " no mNetworkStatsManager", new Object[0]);
                return traffic;
            } else {
                int subId2 = SubscriptionManager.getDefaultDataSubscriptionId();
                if (subId2 == -1) {
                    int subId3 = SubscriptionManager.getDefaultSubscriptionId();
                    LogUtil.w(false, " no default DATA sub", new Object[0]);
                    subId = subId3;
                } else {
                    subId = subId2;
                }
                if (subId == -1) {
                    LogUtil.w(false, " no default sub", new Object[0]);
                    return traffic;
                }
                String subscriberId = defaultTelephonyManager.getSubscriberId(subId);
                if (subscriberId == null) {
                    LogUtil.w(false, " no subscriber", new Object[0]);
                    return traffic;
                }
                processScrbIdPrint(startTime, endTime, network, subscriberId);
                try {
                    processTraffic(mNetworkStatsManager.querySummary(network, subscriberId, startTime, endTime), traffic);
                } catch (SecurityException e) {
                    LogUtil.e(false, "getTraffic Exception %{public}s", e.getMessage());
                } catch (RemoteException e2) {
                    LogUtil.e(false, "getTraffic Exception %{public}s", e2.getMessage());
                }
                return traffic;
            }
        }
    }

    private static void processScrbIdPrint(long startTime, long endTime, int network, String subscriberId) {
        String scrbIdPrint;
        if (subscriberId.length() > 6) {
            scrbIdPrint = subscriberId.substring(0, 6);
        } else {
            scrbIdPrint = subscriberId;
        }
        LogUtil.i(false, "getTraffic begin: startTime=%{public}s, endTime=%{public}s, network=%{public}d, =%{private}s", String.valueOf(startTime), String.valueOf(endTime), Integer.valueOf(network), scrbIdPrint);
    }

    private static void processTraffic(NetworkStats networkStats, long[] traffic) {
        NetworkStats.Bucket summaryBucket = new NetworkStats.Bucket();
        long rxBytes = 0;
        long txBytes = 0;
        if (networkStats != null) {
            do {
                networkStats.getNextBucket(summaryBucket);
                rxBytes += summaryBucket.getRxBytes();
                txBytes += summaryBucket.getTxBytes();
            } while (networkStats.hasNextBucket());
            traffic[0] = rxBytes;
            traffic[1] = txBytes;
            LogUtil.i(false, "getTraffic: rx=%{public}s, tx=%{public}s", String.valueOf(rxBytes), String.valueOf(txBytes));
            return;
        }
        LogUtil.e(false, "mNetworkStats == null", new Object[0]);
    }
}
