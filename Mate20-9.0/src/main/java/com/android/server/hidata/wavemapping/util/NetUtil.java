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
import android.provider.Settings;
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
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.huawei.lcagent.client.LogCollectManager;
import java.util.List;

public class NetUtil {
    private static final int INVALID_NETWORK_MODE = -1;
    private static final String TAG = ("WMapping." + NetUtil.class.getSimpleName());
    public static final String UNKNOWN_STR = "UNKNOWN";

    public static Bundle getWifiStateString(Context context) {
        String wifiStateStr;
        Context context2 = context;
        Bundle output = new Bundle();
        boolean wifiConnected = false;
        Object obj = "UNKNOWN";
        try {
            WifiManager myWifiManager = (WifiManager) context2.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
            if (myWifiManager == null) {
                LogUtil.d("getWifiStateString:myWifiManager == null");
                return output;
            }
            ConnectivityManager myConMgr = (ConnectivityManager) context2.getSystemService("connectivity");
            if (myConMgr == null) {
                LogUtil.d("getWifiStateString:myConMgr == null");
                return output;
            }
            switch (myWifiManager.getWifiState()) {
                case 0:
                    wifiStateStr = "DISABLING";
                    break;
                case 1:
                    wifiStateStr = "DISABLED";
                    break;
                case 2:
                    wifiStateStr = "ENABLING";
                    break;
                case 3:
                    wifiStateStr = "ENABLED";
                    break;
                default:
                    wifiStateStr = "UNKNOWN";
                    break;
            }
            Network[] networks = myConMgr.getAllNetworks();
            if (networks != null) {
                LogUtil.i("networks size is " + String.valueOf(networks.length));
                int i = 0;
                while (true) {
                    if (i < networks.length) {
                        NetworkInfo netInfo = myConMgr.getNetworkInfo(networks[i]);
                        if (netInfo != null) {
                            LogUtil.v("networksInfo is " + netInfo.toString());
                            if (netInfo.getType() == 1 && netInfo.isConnected()) {
                                wifiConnected = true;
                            }
                        }
                        i++;
                        Context context3 = context;
                    }
                }
            }
            if (!wifiConnected) {
                LogUtil.d("wifi NOT connected");
                wifiStateStr = "DISCONNECTED";
            }
            WifiInfo wifiInfo = myWifiManager.getConnectionInfo();
            if (!(wifiInfo == null || wifiInfo.getBSSID() == null)) {
                String wifiAp = wifiInfo.getSSID();
                String wifiMAC = wifiInfo.getBSSID();
                String wifiCh = Integer.toString(wifiInfo.getFrequency());
                String wifiLinkSpeed = Integer.toString(wifiInfo.getLinkSpeed());
                output.putString("wifiAp", wifiAp);
                output.putString("wifiMAC", wifiMAC);
                output.putString("wifiCh", wifiCh);
                output.putString("wifiLS", wifiLinkSpeed);
                output.putString("wifiRssi", Integer.toString(wifiInfo.getRssi()));
            }
            output.putString("wifiState", wifiStateStr);
            LogUtil.i(" getWifiStateString, current Ssid=" + output.getString("wifiAp", "UNKNOWN") + ", state=" + output.getString("wifiState", "UNKNOWN"));
            return output;
        } catch (RuntimeException e) {
            LogUtil.e("getWifiStateString: RuntimeException ");
        } catch (Exception e2) {
            LogUtil.e("getWifiStateString:" + e2.getMessage());
        }
    }

    public static String getMobileDataScrbId(Context context, LogCollectManager mCollectManger) {
        String scriptionId = "NA";
        if (mCollectManger == null) {
            try {
                LogUtil.w(" no mCollectManger");
                return scriptionId;
            } catch (RuntimeException e) {
                LogUtil.e("getMobileDataSubId: RuntimeException ");
            } catch (Exception e2) {
                LogUtil.e("getMobileDataSubId,e" + e2.getMessage());
            }
        } else {
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (defaultTelephonyManager == null) {
                LogUtil.w(" no defaultTelephonyManager");
                return scriptionId;
            }
            int subId = SubscriptionManager.getDefaultDataSubscriptionId();
            if (-1 == subId) {
                subId = SubscriptionManager.getDefaultSubscriptionId();
                LogUtil.w(" no default DATA sub: " + subId);
            }
            if (-1 == subId) {
                LogUtil.w(" no default sub: " + subId);
                return scriptionId;
            }
            int phoneType = TelephonyManager.getPhoneType(subId);
            if (1 == phoneType) {
                String scribId = defaultTelephonyManager.getSubscriberId(subId);
                if (scribId != null) {
                    scriptionId = mCollectManger.doEncrypt(scribId);
                }
            } else if (2 == phoneType) {
                String scribId2 = defaultTelephonyManager.getMeid(subId);
                if (scribId2 != null) {
                    scriptionId = mCollectManger.doEncrypt(scribId2);
                }
            }
            LogUtil.i("getMobileDataSubId: " + scriptionId + ", subId=" + subId + ", phoneType=" + phoneType);
            return scriptionId;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:105:0x0378, code lost:
        r9 = r16;
        r8 = r24;
        r3 = r27;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x0445, code lost:
        com.android.server.hidata.wavemapping.util.LogUtil.e("getMobileDataState: RuntimeException ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        com.android.server.hidata.wavemapping.util.LogUtil.e("reading the preferred nework mode failed = " + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0247, code lost:
        r8 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x02be, code lost:
        r11 = r6;
        r3 = r8;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0444 A[ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:1:0x000b] */
    public static Bundle getMobileDataState(Context context) {
        String dataSwitch;
        String cellId;
        String cellRAT;
        int simpleType;
        String cellFreq;
        String cellRssi;
        String cellPLMN;
        int nwType;
        String cellId2;
        String cellFreq2;
        String cellRssi2;
        String cellPLMN2;
        int cellnum;
        int i;
        String cellPLMN3;
        String cellRssi3;
        String cellRssi4;
        boolean isDataOn;
        Bundle output = new Bundle();
        try {
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (defaultTelephonyManager == null) {
                LogUtil.w(" no defaultTelephonyManager");
                return output;
            }
            int subId = SubscriptionManager.getDefaultDataSubscriptionId();
            if (-1 == subId) {
                subId = SubscriptionManager.getDefaultSubscriptionId();
                LogUtil.w(" no default DATA sub");
            }
            int subId2 = subId;
            if (-1 == subId2) {
                LogUtil.w(" no default sub");
                return output;
            }
            TelephonyManager subTelephonyManager = defaultTelephonyManager.createForSubscriptionId(subId2);
            if (subTelephonyManager == null) {
                LogUtil.e(" no TelephonyManager, subId:" + subId2);
                return output;
            }
            String cellId3 = "UNKNOWN";
            String cellFreq3 = "UNKNOWN";
            String cellRssi5 = "UNKNOWN";
            Object obj = "UNKNOWN";
            String cellPLMN4 = "";
            boolean isDataOn2 = defaultTelephonyManager.isDataEnabled();
            if (isDataOn2) {
                dataSwitch = "ENABLED";
            } else {
                dataSwitch = "DISABLED";
            }
            String dataSwitch2 = dataSwitch;
            int preferredMode = -1;
            preferredMode = Settings.Global.getInt(context.getContentResolver(), "preferred_network_mode", -1);
            int mServiceState = subTelephonyManager.getServiceState().getState();
            int dataState = subTelephonyManager.getDataState();
            String nwPLMN = subTelephonyManager.getNetworkOperator();
            if (nwPLMN == null) {
                StringBuilder sb = new StringBuilder();
                cellId = cellId3;
                sb.append(" no PLMN, subId:");
                sb.append(subId2);
                LogUtil.d(sb.toString());
                nwPLMN = "UNKNOWN";
            } else {
                cellId = cellId3;
            }
            int nwType2 = subTelephonyManager.getDataNetworkType();
            int i2 = nwType2;
            switch (nwType2) {
                case 1:
                case 2:
                case 16:
                    cellRAT = "2G";
                    simpleType = 16;
                    break;
                case 3:
                case 8:
                case 9:
                case 10:
                case 15:
                case 17:
                    cellRAT = "3G";
                    simpleType = 3;
                    break;
                case 4:
                case 7:
                    cellRAT = "CDMA";
                    simpleType = 4;
                    break;
                case 5:
                case 6:
                case 12:
                case 14:
                    cellRAT = "C3G";
                    simpleType = 4;
                    break;
                case 13:
                    cellRAT = "4G";
                    simpleType = 13;
                    break;
                default:
                    cellRAT = "UNKNOWN";
                    simpleType = 0;
                    break;
            }
            int phoneCnt = subTelephonyManager.getPhoneCount();
            int targetIdx = 0;
            int i3 = 0;
            int regCnt = 0;
            int regCnt2 = 0;
            while (true) {
                TelephonyManager subTelephonyManager2 = subTelephonyManager;
                cellFreq = cellFreq3;
                int phoneCnt2 = phoneCnt;
                int k = regCnt2;
                if (k < phoneCnt2) {
                    int phoneCnt3 = phoneCnt2;
                    TelephonyManager subTel = defaultTelephonyManager.createForSubscriptionId(k);
                    if (subTel == null) {
                        cellRssi = cellRssi5;
                        cellPLMN = cellPLMN4;
                        isDataOn = isDataOn2;
                    } else {
                        cellRssi = cellRssi5;
                        int regV = subTel.getServiceState().getVoiceRegState();
                        cellPLMN = cellPLMN4;
                        int regD = subTel.getServiceState().getDataRegState();
                        TelephonyManager telephonyManager = subTel;
                        StringBuilder sb2 = new StringBuilder();
                        isDataOn = isDataOn2;
                        sb2.append("RegState(");
                        sb2.append(k);
                        sb2.append("): voice=");
                        sb2.append(regV);
                        sb2.append(", data=");
                        sb2.append(regD);
                        LogUtil.i(sb2.toString());
                        if (regV == 0 || regD == 0) {
                            regCnt++;
                            if (k == subId2) {
                                targetIdx = regCnt;
                            }
                        }
                    }
                    regCnt2 = k + 1;
                    subTelephonyManager = subTelephonyManager2;
                    cellFreq3 = cellFreq;
                    phoneCnt = phoneCnt3;
                    cellRssi5 = cellRssi;
                    cellPLMN4 = cellPLMN;
                    isDataOn2 = isDataOn;
                } else {
                    cellRssi = cellRssi5;
                    cellPLMN = cellPLMN4;
                    boolean z = isDataOn2;
                }
            }
            List<CellInfo> cellInfos = defaultTelephonyManager.getAllCellInfo();
            if (cellInfos == null || cellInfos.size() == 0) {
                nwType = nwType2;
                TelephonyManager telephonyManager2 = defaultTelephonyManager;
                List<CellInfo> list = cellInfos;
                LogUtil.i(" no cellInfo ");
                cellId2 = cellId;
                int i4 = targetIdx;
                cellFreq2 = cellFreq;
                cellRssi2 = cellRssi;
            } else {
                int cellnum2 = cellInfos.size();
                int targetIdx2 = targetIdx;
                while (true) {
                    int i5 = i3;
                    if (i5 < cellnum2) {
                        CellInfo cellInfo = cellInfos.get(i5);
                        TelephonyManager defaultTelephonyManager2 = defaultTelephonyManager;
                        StringBuilder sb3 = new StringBuilder();
                        List<CellInfo> cellInfos2 = cellInfos;
                        sb3.append("cellInfo(");
                        sb3.append(i5);
                        sb3.append("): ");
                        sb3.append(cellInfo.toString());
                        LogUtil.v(sb3.toString());
                        if (!cellInfo.isRegistered()) {
                            nwType = nwType2;
                            cellnum = cellnum2;
                            i = targetIdx2;
                        } else if (targetIdx2 == 0) {
                            nwType = nwType2;
                            int i6 = targetIdx2;
                        } else if (targetIdx2 > 1) {
                            targetIdx2--;
                            nwType = nwType2;
                            cellnum = cellnum2;
                            i3 = i5 + 1;
                            defaultTelephonyManager = defaultTelephonyManager2;
                            cellInfos = cellInfos2;
                            cellnum2 = cellnum;
                            nwType2 = nwType;
                        } else if (16 != simpleType || !(cellInfo instanceof CellInfoGsm)) {
                            nwType = nwType2;
                            cellnum = cellnum2;
                            i = targetIdx2;
                            if (13 == simpleType && (cellInfo instanceof CellInfoLte)) {
                                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                                CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                                cellPLMN3 = cellIdentityLte.getMccString() + cellIdentityLte.getMncString();
                                LogUtil.i("PLMN(" + cellPLMN3 + ")");
                                if (cellPLMN3.equals(nwPLMN)) {
                                    LogUtil.i("PLMN the same");
                                    cellRssi3 = Integer.toString(cellInfoLte.getCellSignalStrength().getDbm());
                                    cellId2 = Integer.toString(cellIdentityLte.getCi());
                                    CellInfoLte cellInfoLte2 = cellInfoLte;
                                    cellRssi4 = Integer.toString(cellIdentityLte.getEarfcn());
                                }
                                cellPLMN = cellPLMN3;
                            } else if (3 == simpleType && (cellInfo instanceof CellInfoWcdma)) {
                                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                                CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
                                cellPLMN3 = cellIdentityWcdma.getMccString() + cellIdentityWcdma.getMncString();
                                LogUtil.i("PLMN(" + cellPLMN3 + ")");
                                if (cellPLMN3.equals(nwPLMN)) {
                                    LogUtil.i("PLMN the same");
                                    cellRssi3 = Integer.toString(cellInfoWcdma.getCellSignalStrength().getDbm());
                                    cellId2 = Integer.toString(cellIdentityWcdma.getCid());
                                    CellInfoWcdma cellInfoWcdma2 = cellInfoWcdma;
                                    cellRssi4 = Integer.toString(cellIdentityWcdma.getUarfcn());
                                } else {
                                    cellPLMN = cellPLMN3;
                                }
                            } else if (4 != simpleType || !(cellInfo instanceof CellInfoCdma)) {
                                LogUtil.d("not GWLC");
                            } else {
                                CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                                String cellRssi6 = Integer.toString(cellInfoCdma.getCellSignalStrength().getDbm());
                                String cellId4 = Integer.toString(cellInfoCdma.getCellIdentity().getBasestationId());
                                cellFreq2 = cellFreq;
                            }
                        } else {
                            CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                            CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
                            cellnum = cellnum2;
                            StringBuilder sb4 = new StringBuilder();
                            i = targetIdx2;
                            sb4.append(cellIdentityGsm.getMccString());
                            sb4.append(cellIdentityGsm.getMncString());
                            String cellPLMN5 = sb4.toString();
                            StringBuilder sb5 = new StringBuilder();
                            nwType = nwType2;
                            sb5.append("PLMN(");
                            sb5.append(cellPLMN5);
                            sb5.append(")");
                            LogUtil.i(sb5.toString());
                            if (cellPLMN5.equals(nwPLMN)) {
                                LogUtil.i("PLMN the same");
                                String cellRssi7 = Integer.toString(cellInfoGsm.getCellSignalStrength().getDbm());
                                cellId2 = Integer.toString(cellIdentityGsm.getCid());
                                String cellRssi8 = cellRssi7;
                                cellRssi4 = Integer.toString(cellIdentityGsm.getArfcn());
                                cellPLMN2 = cellPLMN5;
                                cellRssi2 = cellRssi8;
                            } else {
                                cellPLMN = cellPLMN5;
                            }
                        }
                        targetIdx2 = i;
                        i3 = i5 + 1;
                        defaultTelephonyManager = defaultTelephonyManager2;
                        cellInfos = cellInfos2;
                        cellnum2 = cellnum;
                        nwType2 = nwType;
                    } else {
                        nwType = nwType2;
                        TelephonyManager telephonyManager3 = defaultTelephonyManager;
                        List<CellInfo> list2 = cellInfos;
                        int i7 = targetIdx2;
                    }
                }
                cellPLMN2 = cellPLMN;
                String str = cellPLMN2;
            }
            output.putString("cellState", dataSwitch2);
            output.putInt("dataState", dataState);
            output.putString("cellId", cellId2);
            output.putString("cellFreq", cellFreq2);
            output.putString("cellRssi", cellRssi2);
            output.putString("cellRAT", cellRAT);
            output.putInt("cellService", mServiceState);
            output.putInt("preferredMode", preferredMode);
            LogUtil.i(" getMobileDataState: subId=" + subId2 + ", nwType=" + nwType + ", cellRAT=" + cellRAT + ", cellFreq=" + cellFreq2 + ", cellState=" + dataSwitch2 + ", dataState=" + dataState + ", cellService=" + mServiceState + ", preferredMode=" + preferredMode);
            StringBuilder sb6 = new StringBuilder();
            sb6.append(" getMobileDataState: cellId=");
            sb6.append(cellId2);
            sb6.append(", PLMN=");
            sb6.append(nwPLMN);
            LogUtil.v(sb6.toString());
            return output;
        } catch (RuntimeException e) {
        } catch (Exception e2) {
            LogUtil.e("getMobileDataState,e" + e2.getMessage());
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
        boolean mobileConnected = false;
        boolean wifiConnected = false;
        int defaultConnectedType = 8;
        String networkId = "UNKNOWN";
        String networkName = "UNKNOWN";
        String networkFreq = "UNKNOWN";
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm != null) {
            NetworkInfo defaultInfo = cm.getActiveNetworkInfo();
            Network[] networks = cm.getAllNetworks();
            if (networks != null) {
                LogUtil.i("networks size is " + String.valueOf(networks.length));
                for (Network networkInfo : networks) {
                    NetworkInfo netInfo = cm.getNetworkInfo(networkInfo);
                    if (netInfo != null) {
                        LogUtil.v("networksInfo is " + netInfo.toString());
                        if (netInfo.isConnected()) {
                            if (netInfo.getType() == 0) {
                                LogUtil.i("mobile network is connected");
                                mobileConnected = true;
                            } else if (netInfo.getType() == 1) {
                                LogUtil.i("wifi network is connected");
                                wifiConnected = true;
                            }
                        }
                    }
                }
            }
            if (defaultInfo == null) {
                LogUtil.d(" no active network");
            } else if (NetworkInfo.State.CONNECTED == defaultInfo.getState() && defaultInfo.isAvailable()) {
                defaultConnectedType = defaultInfo.getType();
                if (1 == defaultConnectedType) {
                    Bundle wifiState = getWifiStateString(context);
                    if (wifiState.getString("wifiMAC", "UNKNOWN") != null) {
                        networkId = wifiState.getString("wifiMAC", "UNKNOWN");
                        networkName = wifiState.getString("wifiAp", "UNKNOWN");
                        networkFreq = wifiState.getString("wifiCh", "UNKNOWN");
                    }
                }
                if (defaultConnectedType == 0) {
                    Bundle mobileState = getMobileDataState(context);
                    if (mobileState.getString("cellId", "UNKNOWN") != null) {
                        networkId = mobileState.getString("cellId", "UNKNOWN");
                        networkName = mobileState.getString("cellRAT", "UNKNOWN");
                        networkFreq = mobileState.getString("cellFreq", "UNKNOWN");
                    }
                }
                LogUtil.i(" network is connected, default type = " + defaultConnectedType + ", Name = " + networkName);
            }
        }
        output.putBoolean("mobile", mobileConnected);
        output.putBoolean(DevSchedFeatureRT.WIFI_FEATURE, wifiConnected);
        output.putInt("defaultType", defaultConnectedType);
        output.putString("defaultNwId", networkId);
        output.putString("defaultNwName", networkName);
        output.putString("defaultNwFreq", networkFreq);
        return output;
    }

    public static boolean isMobileCallStateIdle(Context context) {
        boolean result = false;
        try {
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (defaultTelephonyManager == null) {
                LogUtil.w(" no defaultTelephonyManager");
                return false;
            }
            int subId = SubscriptionManager.getDefaultDataSubscriptionId();
            if (-1 == subId) {
                subId = SubscriptionManager.getDefaultSubscriptionId();
                LogUtil.w(" no default DATA sub");
            }
            if (-1 == subId) {
                LogUtil.w(" no default sub");
                return false;
            }
            TelephonyManager subTelephonyManager = defaultTelephonyManager.createForSubscriptionId(subId);
            if (subTelephonyManager == null) {
                LogUtil.e(" no TelephonyManager");
                return false;
            }
            int mCallState = subTelephonyManager.getCallState();
            LogUtil.i(" getMobileCallState, current call state=" + mCallState + ", subId=" + subId);
            if (mCallState == 0) {
                result = true;
            }
            return result;
        } catch (RuntimeException e) {
            LogUtil.e("getMobileCallState: RuntimeException ");
        } catch (Exception e2) {
            LogUtil.e("getMobileCallState,e:" + e2.getMessage());
        }
    }

    public static boolean isWifiEnabled(Context context) {
        try {
            WifiManager myWifiManager = (WifiManager) context.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
            if (myWifiManager == null) {
                LogUtil.d("isWifiEnabled:myWifiManager == null");
                return false;
            }
            int wifiState = myWifiManager.getWifiState();
            return myWifiManager.isWifiEnabled();
        } catch (RuntimeException e) {
            LogUtil.e("isWifiEnabled: RuntimeException ");
            return false;
        } catch (Exception e2) {
            LogUtil.e("isWifiEnabled:" + e2.getMessage());
            return false;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v12, resolved type: android.app.usage.NetworkStats} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v18, resolved type: android.app.usage.NetworkStats} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v23, resolved type: android.app.usage.NetworkStats} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v34, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v35, resolved type: android.app.usage.NetworkStats} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v37, resolved type: android.app.usage.NetworkStats} */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0136, code lost:
        if (r1 != null) goto L_0x0138;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0195, code lost:
        if (r1 == null) goto L_0x0198;
     */
    /* JADX WARNING: Failed to insert additional move for type inference */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0175 A[Catch:{ SecurityException -> 0x0176, RemoteException -> 0x0154, all -> 0x0148, all -> 0x0199 }] */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x019c  */
    public static long[] getTraffic(long startTime, long endTime, int network, Context context) {
        String ScrbId_print;
        NetworkStats mNetworkStats;
        NetworkStats mNetworkStats2;
        NetworkStats mNetworkStats3;
        long j = startTime;
        long j2 = endTime;
        int i = network;
        Context context2 = context;
        long[] traffic = new long[2];
        if (i != 0 && 1 != i) {
            LogUtil.e("network is invalid:" + i);
            return traffic;
        } else if (j2 < j) {
            LogUtil.w("Time is invalid: start=" + j + ", end=" + j2);
            return traffic;
        } else {
            NetworkStatsManager mNetworkStatsManager = (NetworkStatsManager) context2.getSystemService("netstats");
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context2.getSystemService("phone");
            if (defaultTelephonyManager == null) {
                LogUtil.w(" no defaultTelephonyManager");
                return traffic;
            } else if (mNetworkStatsManager == null) {
                LogUtil.w(" no mNetworkStatsManager");
                return traffic;
            } else {
                int subId = SubscriptionManager.getDefaultDataSubscriptionId();
                if (-1 == subId) {
                    subId = SubscriptionManager.getDefaultSubscriptionId();
                    LogUtil.w(" no default DATA sub");
                }
                int subId2 = subId;
                if (-1 == subId2) {
                    LogUtil.w(" no default sub");
                    return traffic;
                }
                String subscriberId = defaultTelephonyManager.getSubscriberId(subId2);
                if (subscriberId == null) {
                    LogUtil.w(" no subscriber");
                    return traffic;
                }
                if (subscriberId.length() > 6) {
                    ScrbId_print = subscriberId.substring(0, 6);
                } else {
                    ScrbId_print = subscriberId;
                }
                String ScrbId_print2 = ScrbId_print;
                StringBuilder sb = new StringBuilder();
                sb.append("getTraffic begin: startTime=");
                sb.append(j);
                sb.append(", endTime=");
                sb.append(j2);
                sb.append(", network=");
                sb.append(i);
                String str = ", =";
                sb.append(str);
                sb.append(ScrbId_print2);
                LogUtil.i(sb.toString());
                try {
                    mNetworkStats2 = str;
                    NetworkStats.Bucket summaryBucket = new NetworkStats.Bucket();
                    String str2 = ScrbId_print2;
                    String str3 = subscriberId;
                    TelephonyManager telephonyManager = defaultTelephonyManager;
                    int i2 = subId2;
                    try {
                        mNetworkStats3 = mNetworkStatsManager.querySummary(i, subscriberId, j, j2);
                        long rxBytes = 0;
                        long txBytes = 0;
                        if (mNetworkStats3 != null) {
                            do {
                                try {
                                    mNetworkStats3.getNextBucket(summaryBucket);
                                    rxBytes += summaryBucket.getRxBytes();
                                    txBytes += summaryBucket.getTxBytes();
                                } catch (SecurityException e) {
                                    e = e;
                                    LogUtil.e("getTraffic Exception" + e);
                                    mNetworkStats3 = mNetworkStats3;
                                } catch (RemoteException e2) {
                                    e = e2;
                                    mNetworkStats2 = mNetworkStats3;
                                    LogUtil.e("getTraffic Exception" + e);
                                    if (mNetworkStats3 != null) {
                                        mNetworkStats3 = mNetworkStats3;
                                        mNetworkStats3.close();
                                    }
                                    return traffic;
                                }
                            } while (mNetworkStats3.hasNextBucket());
                            traffic[0] = rxBytes;
                            traffic[1] = txBytes;
                            StringBuilder sb2 = new StringBuilder();
                            NetworkStats.Bucket bucket = summaryBucket;
                            sb2.append("getTraffic: rx=");
                            sb2.append(rxBytes);
                            sb2.append(", tx=");
                            sb2.append(txBytes);
                            LogUtil.i(sb2.toString());
                        } else {
                            NetworkStats.Bucket bucket2 = summaryBucket;
                            LogUtil.e("mNetworkStats == null");
                        }
                    } catch (SecurityException e3) {
                        e = e3;
                        mNetworkStats3 = null;
                        LogUtil.e("getTraffic Exception" + e);
                        mNetworkStats3 = mNetworkStats3;
                    } catch (RemoteException e4) {
                        e = e4;
                        mNetworkStats3 = null;
                        mNetworkStats2 = mNetworkStats3;
                        LogUtil.e("getTraffic Exception" + e);
                        if (mNetworkStats3 != null) {
                        }
                        return traffic;
                    } catch (Throwable th) {
                        th = th;
                        mNetworkStats = 0;
                        if (mNetworkStats != 0) {
                            mNetworkStats.close();
                        }
                        throw th;
                    }
                } catch (SecurityException e5) {
                    e = e5;
                    String str4 = ScrbId_print2;
                    String str5 = subscriberId;
                    TelephonyManager telephonyManager2 = defaultTelephonyManager;
                    int i3 = subId2;
                    mNetworkStats3 = null;
                    LogUtil.e("getTraffic Exception" + e);
                    mNetworkStats3 = mNetworkStats3;
                } catch (RemoteException e6) {
                    e = e6;
                    String str6 = ScrbId_print2;
                    String str7 = subscriberId;
                    TelephonyManager telephonyManager3 = defaultTelephonyManager;
                    int i4 = subId2;
                    mNetworkStats3 = null;
                    mNetworkStats2 = mNetworkStats3;
                    LogUtil.e("getTraffic Exception" + e);
                    if (mNetworkStats3 != null) {
                    }
                    return traffic;
                } catch (Throwable th2) {
                    th = th2;
                    mNetworkStats = mNetworkStats2;
                    if (mNetworkStats != 0) {
                    }
                    throw th;
                }
            }
        }
    }
}
