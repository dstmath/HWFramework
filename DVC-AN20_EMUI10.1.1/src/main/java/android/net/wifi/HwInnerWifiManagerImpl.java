package android.net.wifi;

import android.content.Context;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.SettingsEx;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.p2p.HwWifiP2pManagerEx;
import com.huawei.android.net.wifi.HwHiLogEx;
import com.huawei.android.net.wifi.HwWifiAdapterEx;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HwInnerWifiManagerImpl implements HwInnerWifiManager {
    private static final int CLIENT_CODE_CONTROL_HIDATA_OPTIMIZE = 3031;
    private static final int CODE_CONFIRM_WIFI_REPEATER = 1022;
    private static final int CODE_CTRL_HW_WIFI_NETWORK = 1017;
    private static final int CODE_DISABLE_RX_FILTER = 3021;
    private static final int CODE_ENABLE_HILINK_HANDSHAKE = 2001;
    private static final int CODE_ENABLE_RX_FILTER = 3022;
    private static final int CODE_EXTEND_WIFI_SCAN_PERIOD_FOR_P2P = 2006;
    private static final int CODE_GET_APLINKED_STA_LIST = 1005;
    private static final int CODE_GET_CONNECTION_RAW_PSK = 2002;
    private static final int CODE_GET_PPPOE_INFO_CONFIG = 1004;
    private static final int CODE_GET_RSDB_SUPPORTED_MODE = 2008;
    private static final int CODE_GET_SINGNAL_INFO = 1011;
    private static final int CODE_GET_SOFTAP_BANDWIDTH = 4012;
    private static final int CODE_GET_SOFTAP_CHANNEL_LIST = 1009;
    private static final int CODE_GET_VOWIFI_DETECT_MODE = 1013;
    private static final int CODE_GET_VOWIFI_DETECT_PERIOD = 1015;
    private static final int CODE_GET_WIFI_MODE = 4112;
    private static final int CODE_GET_WIFI_REPEATER_MODE = 1021;
    private static final int CODE_GET_WPA_SUPP_CONFIG = 1001;
    private static final int CODE_IS_BG_LIMIT_ALLOWED = 3008;
    private static final int CODE_IS_FEATURE_SUPPORTED = 4011;
    private static final int CODE_IS_SUPPORT_VOWIFI_DETECT = 1016;
    private static final int CODE_REPORT_SPEED_RESULT = 4103;
    private static final int CODE_REQUEST_FRESH_WHITE_LIST = 2007;
    private static final int CODE_REQUEST_WIFI_ENABLE = 2004;
    private static final int CODE_SET_FEM_TXPOWER = 4013;
    private static final int CODE_SET_PERFORMANCE_MODE = 4102;
    private static final int CODE_SET_SOFTAP_DISASSOCIATESTA = 1007;
    private static final int CODE_SET_SOFTAP_MACFILTER = 1006;
    private static final int CODE_SET_VOWIFI_DETECT_MODE = 1012;
    private static final int CODE_SET_VOWIFI_DETECT_PERIOD = 1014;
    private static final int CODE_SET_WIFI_ANTSET = 3007;
    private static final int CODE_SET_WIFI_AP_EVALUATE_ENABLED = 1010;
    private static final int CODE_SET_WIFI_MODE = 4111;
    private static final int CODE_SET_WIFI_TXPOWER = 2005;
    private static final int CODE_START_PPPOE_CONFIG = 1002;
    private static final int CODE_START_WIFI_KEEP_ALIVE = 3023;
    private static final int CODE_STOP_PPPOE_CONFIG = 1003;
    private static final int CODE_STOP_WIFI_KEEP_ALIVE = 3024;
    private static final int CODE_UPDATE_APP_EXPERIENCE_STATUS = 3006;
    private static final int CODE_UPDATE_APP_RUNNING_STATUS = 3005;
    private static final int CODE_UPDATE_LIMIT_SPEED_STATUS = 3025;
    private static final int CODE_UPDATE_WM_FREQ_LOC = 4002;
    private static final int CODE_USER_HANDOVER_WIFI = 1008;
    private static final int CODE_WIFI_DC_CONNECT = 4014;
    private static final int CODE_WIFI_DC_DISCONNECT = 4015;
    private static final int CODE_WIFI_QOE_EVALUATE = 3003;
    private static final int CODE_WIFI_QOE_START_MONITOR = 3001;
    private static final int CODE_WIFI_QOE_STOP_MONITOR = 3002;
    private static final int CODE_WIFI_QOE_UPDATE_STATUS = 3004;
    private static final boolean DBG = false;
    private static final String DESCRIPTOR = "android.net.wifi.IWifiManager";
    public static final int EAP_AKA = 5;
    public static final int EAP_AKAPrim = 6;
    private static final String EAP_KEY = "eap";
    public static final int EAP_SIM = 4;
    private static final int ERROR = -1;
    private static final String PCSC_KEY = "pcsc";
    private static final int SUCCESS = 0;
    private static final String TAG = "HwInnerWifiManagerImpl";
    private static final int TYPE_CHANNELS_5G = 0;
    private static final int TYPE_CHANNELS_BW160 = 1;
    private static final int WIFI_FEATURE_BW160 = 1;
    private static HwInnerWifiManager mInstance = new HwInnerWifiManagerImpl();
    private String wifi_level_threshold = HwWifiAdapterEx.getSystemProperties("ro.config.hw_wifi_signal_level");

    public static HwInnerWifiManager getDefault() {
        return mInstance;
    }

    public String getAppendSsidWithRandomUuid(WifiConfiguration config, Context context) {
        if (1 == SettingsEx.Systemex.getInt(context.getContentResolver(), "hw_softap_ssid_extend", 0)) {
            config.SSID += UUID.randomUUID().toString().substring(13, 18);
            HwHiLogEx.d(TAG, false, "new SSID:%{public}s", new Object[]{config.SSID});
        }
        return config.SSID;
    }

    public int calculateSignalLevelHW(int freq, int rssi) {
        int rssi_Level_4 = -65;
        int rssi_Level_3 = -75;
        int rssi_Level_2 = -82;
        int rssi_Level_1 = -88;
        if (HwWifiAdapterEx.is5GHz(freq)) {
            rssi_Level_4 = -65;
            rssi_Level_3 = -72;
            rssi_Level_2 = -79;
            rssi_Level_1 = -85;
        }
        if (!TextUtils.isEmpty(this.wifi_level_threshold)) {
            String[] wifi_level_threshold_arr = this.wifi_level_threshold.split(",");
            if (wifi_level_threshold_arr.length == 4) {
                try {
                    rssi_Level_4 = Integer.parseInt(wifi_level_threshold_arr[0]);
                    rssi_Level_3 = Integer.parseInt(wifi_level_threshold_arr[1]);
                    rssi_Level_2 = Integer.parseInt(wifi_level_threshold_arr[2]);
                    rssi_Level_1 = Integer.parseInt(wifi_level_threshold_arr[3]);
                } catch (Exception e) {
                    HwHiLogEx.i(TAG, false, "Exception occured,use the default level threshold!", new Object[0]);
                    rssi_Level_4 = -65;
                    rssi_Level_3 = -75;
                    rssi_Level_2 = -82;
                    rssi_Level_1 = -88;
                }
            } else {
                HwHiLogEx.e(TAG, false, "Customization of wifi level is illeagel! wifi_level_threshold =%{public}s", new Object[]{this.wifi_level_threshold});
            }
        }
        if (rssi_Level_4 <= rssi) {
            return 4;
        }
        if (rssi_Level_4 - 1 >= rssi && rssi_Level_3 <= rssi) {
            return 3;
        }
        if (rssi_Level_3 - 1 >= rssi && rssi_Level_2 <= rssi) {
            return 2;
        }
        if (rssi_Level_2 - 1 < rssi || rssi_Level_1 > rssi) {
            return 0;
        }
        return 1;
    }

    public int calculateSignalLevelHW(int rssi) {
        int rssi_Level_4 = -65;
        int rssi_Level_3 = -75;
        int rssi_Level_2 = -82;
        int rssi_Level_1 = -88;
        if (!TextUtils.isEmpty(this.wifi_level_threshold)) {
            String[] wifi_level_threshold_arr = this.wifi_level_threshold.split(",");
            if (wifi_level_threshold_arr.length == 4) {
                try {
                    rssi_Level_4 = Integer.parseInt(wifi_level_threshold_arr[0]);
                    rssi_Level_3 = Integer.parseInt(wifi_level_threshold_arr[1]);
                    rssi_Level_2 = Integer.parseInt(wifi_level_threshold_arr[2]);
                    rssi_Level_1 = Integer.parseInt(wifi_level_threshold_arr[3]);
                } catch (Exception e) {
                    HwHiLogEx.i(TAG, false, "Exception occured,use the default level threshold! wifi_level_threshold = %{public}s", new Object[]{this.wifi_level_threshold});
                    rssi_Level_1 = -88;
                    rssi_Level_2 = -82;
                    rssi_Level_3 = -75;
                    rssi_Level_4 = -65;
                }
            } else {
                HwHiLogEx.e(TAG, false, "Customization of wifi level is illeagel! wifi_level_threshold =%{public}s", new Object[]{this.wifi_level_threshold});
            }
        }
        if (rssi_Level_4 <= rssi) {
            return 4;
        }
        if (rssi_Level_3 <= rssi && rssi_Level_4 - 1 >= rssi) {
            return 3;
        }
        if (rssi_Level_2 <= rssi && rssi_Level_3 - 1 >= rssi) {
            return 2;
        }
        if (rssi_Level_1 > rssi || rssi_Level_2 - 1 < rssi) {
            return 0;
        }
        return 1;
    }

    public String getWpaSuppConfig() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "getWpaSuppConfig", new Object[0]);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(CODE_GET_WPA_SUPP_CONFIG, _data, _reply, 0);
                _reply.readException();
                return _reply.readString();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        } else {
            _reply.recycle();
            _data.recycle();
            return null;
        }
    }

    public void startPPPOE(PPPOEConfig config) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "startPPPOE", new Object[0]);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                if (config != null) {
                    _data.writeInt(1);
                    config.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                b.transact(CODE_START_PPPOE_CONFIG, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public void stopPPPOE() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(CODE_STOP_PPPOE_CONFIG, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public PPPOEInfo getPPPOEInfo() {
        return HwWifiAdapterEx.getPPPOEInfo(DESCRIPTOR, (int) CODE_GET_PPPOE_INFO_CONFIG);
    }

    public boolean setWifiEnterpriseConfigEapMethod(int eapMethod, HashMap<String, String> hashMap) {
        switch (eapMethod) {
            case 4:
            case 5:
            case 6:
                return true;
            default:
                return false;
        }
    }

    public boolean getHwMeteredHint(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiManager.getWifiState() != 3) {
            return false;
        }
        if (wifiInfo == null) {
            return false;
        }
        if (wifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
            return false;
        }
        return HwWifiAdapterEx.getMeteredHint(wifiInfo);
    }

    public List<String> getApLinkedStaList() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "getApLinkedStaList", new Object[0]);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(CODE_GET_APLINKED_STA_LIST, _data, _reply, 0);
                _reply.readException();
                return _reply.createStringArrayList();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        } else {
            _reply.recycle();
            _data.recycle();
            return null;
        }
    }

    public void setSoftapMacFilter(String macFilter) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "setSoftapMacFilter macFilter = %{private}s", new Object[]{macFilter});
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(macFilter);
                b.transact(CODE_SET_SOFTAP_MACFILTER, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public void setSoftapDisassociateSta(String mac) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "setSoftapDisassociateSta mac = %{private}s", new Object[]{mac});
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(mac);
                b.transact(CODE_SET_SOFTAP_DISASSOCIATESTA, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public void userHandoverWifi() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "wifipro userHandoverWifi ", new Object[0]);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(CODE_USER_HANDOVER_WIFI, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public Bundle ctrlHwWifiNetwork(String pkgName, int interfaceId, Bundle inputData) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        Log.d(TAG, "ctrlHwWifiNetwork");
        Bundle result = new Bundle();
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeString(pkgName);
                data.writeInt(interfaceId);
                data.writeBundle(inputData);
                binder.transact(CODE_CTRL_HW_WIFI_NETWORK, data, reply, 0);
                reply.readException();
                result = reply.readBundle();
            } catch (RemoteException e) {
                HwHiLogEx.d(TAG, false, "ctrlHwWifiNetwork, localRemoteException e", new Object[0]);
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    public void setWifiApEvaluateEnabled(boolean enabled) {
        int i = 1;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "setWifiApEvaluateEnabled enabled :%{public}s", new Object[]{String.valueOf(enabled)});
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                if (!enabled) {
                    i = 0;
                }
                _data.writeInt(i);
                HwHiLogEx.d(TAG, false, "setWifiApEvaluateEnabled enabled :%{public}s", new Object[]{String.valueOf(enabled)});
                b.transact(CODE_SET_WIFI_AP_EVALUATE_ENABLED, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public int[] getChannelListFor5G() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        int[] result = null;
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(0);
            b.transact(CODE_GET_SOFTAP_CHANNEL_LIST, data, reply, 0);
            reply.readException();
            result = reply.createIntArray();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } finally {
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    public byte[] fetchWifiSignalInfoForVoWiFi() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "fetchWifiSignalInfoForVoWiFi", new Object[0]);
        byte[] _result = null;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            b.transact(CODE_GET_SINGNAL_INFO, _data, _reply, 0);
            _reply.readException();
            _result = _reply.createByteArray();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

    public boolean setVoWifiDetectMode(WifiDetectConfInfo info) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            if (info != null) {
                HwHiLogEx.d(TAG, false, "setVoWifiDetectMode info :%{public}s", new Object[]{info.toString()});
                _data.writeInt(1);
                info.writeToParcel(_data, 0);
            } else {
                _data.writeInt(0);
            }
            b.transact(CODE_SET_VOWIFI_DETECT_MODE, _data, _reply, 0);
            _reply.readException();
            return true;
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
            return false;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public WifiDetectConfInfo getVoWifiDetectMode() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "getVoWifiDetectMode", new Object[0]);
        WifiDetectConfInfo _result = null;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            b.transact(CODE_GET_VOWIFI_DETECT_MODE, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = (WifiDetectConfInfo) WifiDetectConfInfo.CREATOR.createFromParcel(_data);
            } else {
                _result = null;
            }
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

    public boolean setVoWifiDetectPeriod(int period) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "setVoWifiDetectPeriod period :%{public}d", new Object[]{Integer.valueOf(period)});
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(period);
            b.transact(CODE_SET_VOWIFI_DETECT_PERIOD, _data, _reply, 0);
            _reply.readException();
            return true;
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
            return false;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public int getVoWifiDetectPeriod() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "getVoWifiDetectPeriod", new Object[0]);
        int _result = -1;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            b.transact(CODE_GET_VOWIFI_DETECT_PERIOD, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

    public boolean isSupportVoWifiDetect() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "isSupportVoWifiDetect", new Object[0]);
        boolean[] _result = new boolean[1];
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            b.transact(CODE_IS_SUPPORT_VOWIFI_DETECT, _data, _reply, 0);
            _reply.readException();
            _reply.readBooleanArray(_result);
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result[0];
    }

    public void enableHiLinkHandshake(boolean uiEnable, String bssid, WifiConfiguration config) {
        int i = 1;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            if (!uiEnable) {
                i = 0;
            }
            _data.writeInt(i);
            _data.writeString(bssid);
            if (config != null) {
                _data.writeInt(1);
                config.writeToParcel(_data, 0);
            } else {
                _data.writeInt(0);
            }
            b.transact(CODE_ENABLE_HILINK_HANDSHAKE, _data, _reply, 0);
            _reply.readException();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public String getConnectionRawPsk() {
        String _result = null;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            b.transact(CODE_GET_CONNECTION_RAW_PSK, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

    public boolean requestWifiEnable(boolean flag, String reason) {
        int i = 1;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "requestWifiEnable flag : %{public}s, reason : %{public}s", new Object[]{String.valueOf(flag), reason});
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            if (!flag) {
                i = 0;
            }
            _data.writeInt(i);
            _data.writeString(reason);
            b.transact(CODE_REQUEST_WIFI_ENABLE, _data, _reply, 0);
            _reply.readException();
            return true;
        } catch (RemoteException e) {
            HwHiLogEx.d(TAG, false, "requestWifiEnable, localRemoteException e", new Object[0]);
            return false;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public boolean setWifiTxPower(int power) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.i(TAG, false, "setWifiTxPower power :%{public}d", new Object[]{Integer.valueOf(power)});
        int _result = -1;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(power);
            b.transact(CODE_SET_WIFI_TXPOWER, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            HwHiLogEx.d(TAG, false, "setWifiTxPower, localRemoteException e", new Object[0]);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result == 0;
    }

    public boolean startHwQoEMonitor(int monitorType, int period, IHwQoECallback callback) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "startHwQoEMonitor period :%{public}d", new Object[]{Integer.valueOf(period)});
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(monitorType);
            _data.writeInt(period);
            _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
            b.transact(CODE_WIFI_QOE_START_MONITOR, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            HwHiLogEx.d(TAG, false, "startHwQoEMonitor, localRemoteException e", new Object[0]);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result == 1;
    }

    public boolean stopHwQoEMonitor(int monitorType) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "stopHwQoEMonitor", new Object[0]);
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(monitorType);
            b.transact(CODE_WIFI_QOE_STOP_MONITOR, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            HwHiLogEx.d(TAG, false, "stopHwQoEMonitor, localRemoteException e", new Object[0]);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result == 1;
    }

    public boolean evaluateNetworkQuality(IHwQoECallback callback) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "evaluateNetworkQuality", new Object[0]);
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
            b.transact(CODE_WIFI_QOE_EVALUATE, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            HwHiLogEx.d(TAG, false, "evaluateNetworkQuality, localRemoteException e", new Object[0]);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result == 1;
    }

    public boolean updateVOWIFIStatus(int state) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "updateVOWIFIState", new Object[0]);
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(state);
            b.transact(CODE_WIFI_QOE_UPDATE_STATUS, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            HwHiLogEx.d(TAG, false, "updateVOWIFIStatus, localRemoteException e", new Object[0]);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result == 1;
    }

    public boolean updateAppRunningStatus(int uid, int type, int status, int scene, int reserved) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(uid);
            _data.writeInt(type);
            _data.writeInt(status);
            _data.writeInt(scene);
            _data.writeInt(reserved);
            b.transact(CODE_UPDATE_APP_RUNNING_STATUS, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            HwHiLogEx.d(TAG, false, "updateAppRunningStatus, localRemoteException e", new Object[0]);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        if (_result == 1) {
            return true;
        }
        return false;
    }

    public boolean updateLimitSpeedStatus(int mode, int reserve1, int reserve2) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(mode);
            _data.writeInt(reserve1);
            _data.writeInt(reserve2);
            b.transact(CODE_UPDATE_LIMIT_SPEED_STATUS, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            HwHiLogEx.d(TAG, false, "updateLimitSpeedStatus, localRemoteException e", new Object[0]);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result == 1;
    }

    public boolean controlHidataOptimize(String pkgName, int action, boolean enable) {
        return HwWifiAdapterEx.controlHidataOptimize(DESCRIPTOR, (int) CLIENT_CODE_CONTROL_HIDATA_OPTIMIZE, pkgName, action, enable);
    }

    public boolean updateAppExperienceStatus(int uid, int experience, long rtt, int reserved) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(uid);
            _data.writeInt(experience);
            _data.writeLong(rtt);
            _data.writeInt(reserved);
            b.transact(CODE_UPDATE_APP_EXPERIENCE_STATUS, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            HwHiLogEx.d(TAG, false, "updateAppExperienceStatus, localRemoteException e", new Object[0]);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        if (_result == 1) {
            return true;
        }
        return false;
    }

    public void extendWifiScanPeriodForP2p(Context context, boolean bExtend, int iTimes) {
        int i = 1;
        int checkCallingOrSelfPermission = context.checkCallingOrSelfPermission(HwWifiP2pManagerEx.MAGICLINK_PERMISSION);
        context.getPackageManager();
        if (checkCallingOrSelfPermission != 0) {
            HwHiLogEx.d(TAG, false, "No INSTANTSHARE permission", new Object[0]);
            return;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "extendWifiScanPeriodForP2p flag : %{public}s", new Object[]{String.valueOf(bExtend)});
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            if (!bExtend) {
                i = 0;
            }
            _data.writeInt(i);
            _data.writeInt(iTimes);
            b.transact(CODE_EXTEND_WIFI_SCAN_PERIOD_FOR_P2P, _data, _reply, 0);
            _reply.readException();
        } catch (RemoteException e) {
            HwHiLogEx.d(TAG, false, "extendWifiScanPeriodForP2p, localRemoteException e", new Object[0]);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public void refreshPackageWhitelist(int type, List<String> pkgList) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(type);
            _data.writeStringList(pkgList);
            b.transact(CODE_REQUEST_FRESH_WHITE_LIST, _data, _reply, 0);
            _reply.readException();
        } catch (RemoteException e) {
            HwHiLogEx.d(TAG, false, "refreshPackageWhitelist, localRemoteException e", new Object[0]);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public boolean isRSDBSupported() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        boolean _result = false;
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(CODE_GET_RSDB_SUPPORTED_MODE, _data, _reply, 0);
                _reply.readException();
                boolean[] resultArray = new boolean[1];
                _reply.readBooleanArray(resultArray);
                _result = resultArray[0];
            } catch (RemoteException e) {
                HwHiLogEx.d(TAG, false, "isRSDBSupported, localRemoteException e", new Object[0]);
                _reply.recycle();
                _data.recycle();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public void hwSetWifiAnt(Context context, String iface, int mode, int operation) {
        int checkCallingOrSelfPermission = context.checkCallingOrSelfPermission("com.huawei.wifi.permission.WIFI_ANTSET");
        context.getPackageManager();
        if (checkCallingOrSelfPermission != 0) {
            HwHiLogEx.d(TAG, false, "No com.huawei.wifi.permission.WIFI_ANTSET permission", new Object[0]);
            return;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeString(iface);
            _data.writeInt(mode);
            _data.writeInt(operation);
            b.transact(CODE_SET_WIFI_ANTSET, _data, _reply, 0);
            _reply.readException();
        } catch (RemoteException e) {
            HwHiLogEx.d(TAG, false, "hwSetWifiAnt localRemoteException e", new Object[0]);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public boolean isBgLimitAllowed(int uid) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        boolean _result = false;
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(uid);
                b.transact(CODE_IS_BG_LIMIT_ALLOWED, _data, _reply, 0);
                _reply.readException();
                boolean[] resultArray = new boolean[1];
                _reply.readBooleanArray(resultArray);
                _result = resultArray[0];
            } catch (RemoteException e) {
                HwHiLogEx.d(TAG, false, "isBGAllowed, localRemoteException e", new Object[0]);
                _reply.recycle();
                _data.recycle();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public boolean disableWifiFilter(IBinder token, Context context) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        boolean _result = false;
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                if (token == null) {
                    token = null;
                }
                _data.writeStrongBinder(token);
                b.transact(CODE_DISABLE_RX_FILTER, _data, _reply, 0);
                _reply.readException();
                boolean[] resultArray = new boolean[1];
                _reply.readBooleanArray(resultArray);
                _result = resultArray[0];
            } catch (RemoteException e) {
                HwHiLogEx.d(TAG, false, "disableWifiFilter, localRemoteException e", new Object[0]);
                _reply.recycle();
                _data.recycle();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public boolean enableWifiFilter(IBinder token, Context context) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        boolean _result = false;
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                if (token == null) {
                    token = null;
                }
                _data.writeStrongBinder(token);
                b.transact(CODE_ENABLE_RX_FILTER, _data, _reply, 0);
                _reply.readException();
                boolean[] resultArray = new boolean[1];
                _reply.readBooleanArray(resultArray);
                _result = resultArray[0];
            } catch (RemoteException e) {
                HwHiLogEx.d(TAG, false, "enableWifiFilter, localRemoteException e", new Object[0]);
                _reply.recycle();
                _data.recycle();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public boolean startPacketKeepalive(Message msg) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "startPacketKeepalive", new Object[0]);
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            msg.writeToParcel(_data, 1);
            b.transact(CODE_START_WIFI_KEEP_ALIVE, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            HwHiLogEx.d(TAG, false, "evaluateNetworkQuality, localRemoteException e", new Object[0]);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result == 1;
    }

    public boolean stopPacketKeepalive(Message msg) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        HwHiLogEx.d(TAG, false, "stopPacketKeepalive", new Object[0]);
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            msg.writeToParcel(_data, 1);
            b.transact(CODE_STOP_WIFI_KEEP_ALIVE, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            HwHiLogEx.d(TAG, false, "evaluateNetworkQuality, localRemoteException e", new Object[0]);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result == 1;
    }

    public boolean updateWaveMapping(int location, int action) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        int _result = 0;
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(location);
                _data.writeInt(action);
                b.transact(CODE_UPDATE_WM_FREQ_LOC, _data, _reply, 0);
                _reply.readException();
                _result = _reply.readInt();
            } catch (RemoteException e) {
                HwHiLogEx.d(TAG, false, "updateWaveMapping, localRemoteException e", new Object[0]);
                _reply.recycle();
                _data.recycle();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return _result == 1;
    }

    public int getWifiRepeaterMode() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        int result = -1;
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                binder.transact(CODE_GET_WIFI_REPEATER_MODE, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                HwHiLogEx.e(TAG, false, "Exceptions happen when getWifiRepeaterMode", new Object[0]);
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    public void confirmWifiRepeater(int mode, IWifiRepeaterConfirmListener listener) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeInt(mode);
                data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                binder.transact(CODE_CONFIRM_WIFI_REPEATER, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                HwHiLogEx.e(TAG, false, "Exceptions happen when confirmWifiRepeater", new Object[0]);
                reply.recycle();
                data.recycle();
                return;
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public boolean isWideBandwidthSupported(int type) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        int result = 0;
        if (b != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeInt(1);
                data.writeInt(type);
                b.transact(CODE_IS_FEATURE_SUPPORTED, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                Log.d(TAG, "isWideBandwidthSupported, localRemoteException e");
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return result == 1;
    }

    public boolean reduceTxPower(boolean enable) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        int result = 0;
        if (b != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeInt(enable ? 1 : 0);
                b.transact(CODE_SET_FEM_TXPOWER, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                Log.d(TAG, "reduceTxPower, localRemoteException e");
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return result == 1;
    }

    public int getApBandwidth() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        int result = 0;
        if (b != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                b.transact(CODE_GET_SOFTAP_BANDWIDTH, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                Log.d(TAG, "getApBandwidth, localRemoteException e");
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    public int[] getWideBandwidthChannels() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        int[] result = null;
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(1);
            b.transact(CODE_GET_SOFTAP_CHANNEL_LIST, data, reply, 0);
            reply.readException();
            result = reply.createIntArray();
        } catch (RemoteException e) {
            Log.d(TAG, "getWideBandwidthChannels, localRemoteException");
        } finally {
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    public boolean reportSpeedMeasureResult(String info) {
        if (info == null) {
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        int result = 0;
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeString(info);
                binder.transact(CODE_REPORT_SPEED_RESULT, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                Log.d(TAG, "reportSpeedMeasureResult, localRemoteException e");
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        if (result > 0) {
            return true;
        }
        return false;
    }

    public boolean setPerformanceMode(int mode) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        int result = 0;
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeInt(mode);
                binder.transact(CODE_SET_PERFORMANCE_MODE, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                Log.d(TAG, "setPerformanceMode, localRemoteException e");
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        if (result > 0) {
            return true;
        }
        return false;
    }

    public void dcConnect(WifiConfiguration configuration, IWifiActionListener listener) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                if (configuration != null) {
                    data.writeInt(1);
                    configuration.writeToParcel(data, 0);
                } else {
                    data.writeInt(0);
                }
                data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                binder.transact(CODE_WIFI_DC_CONNECT, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                HwHiLogEx.e(TAG, false, "dcConnect, localRemoteException", new Object[0]);
                reply.recycle();
                data.recycle();
                return;
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public boolean dcDisconnect() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        boolean isSuccess = false;
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                binder.transact(CODE_WIFI_DC_DISCONNECT, data, reply, 0);
                reply.readException();
                boolean[] resultArray = new boolean[1];
                reply.readBooleanArray(resultArray);
                isSuccess = resultArray[0];
            } catch (RemoteException e) {
                HwHiLogEx.e(TAG, false, "dcDisconnect, localRemoteException", new Object[0]);
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return isSuccess;
    }

    public boolean setWifiMode(String packageName, int mode) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifi");
        int result = 0;
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeString(packageName);
                data.writeInt(mode);
                binder.transact(CODE_SET_WIFI_MODE, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                HwHiLogEx.e(TAG, false, "setWifiMode, localRemoteException", new Object[0]);
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        if (result > 0) {
            return true;
        }
        return false;
    }

    public int getWifiMode(String packageName) {
        int result = 0;
        if (!TextUtils.isEmpty(packageName)) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifi");
            result = 0;
            if (binder != null) {
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeString(packageName);
                    binder.transact(CODE_GET_WIFI_MODE, data, reply, 0);
                    reply.readException();
                    result = reply.readInt();
                } catch (RemoteException e) {
                    HwHiLogEx.e(TAG, false, "getWifiMode, localRemoteException", new Object[0]);
                    reply.recycle();
                    data.recycle();
                } catch (Throwable th) {
                    reply.recycle();
                    data.recycle();
                    throw th;
                }
            }
            reply.recycle();
            data.recycle();
        }
        return result;
    }
}
