package android.net.wifi;

import android.content.Context;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.p2p.HwWifiP2pManagerEx;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HwInnerWifiManagerImpl implements HwInnerWifiManager {
    private static final int CODE_ENABLE_HILINK_HANDSHAKE = 2001;
    private static final int CODE_EXTEND_WIFI_SCAN_PERIOD_FOR_P2P = 2006;
    private static final int CODE_GET_APLINKED_STA_LIST = 1005;
    private static final int CODE_GET_CONNECTION_RAW_PSK = 2002;
    private static final int CODE_GET_PPPOE_INFO_CONFIG = 1004;
    private static final int CODE_GET_RSDB_SUPPORTED_MODE = 2008;
    private static final int CODE_GET_SINGNAL_INFO = 1011;
    private static final int CODE_GET_SOFTAP_CHANNEL_LIST = 1009;
    private static final int CODE_GET_VOWIFI_DETECT_MODE = 1013;
    private static final int CODE_GET_VOWIFI_DETECT_PERIOD = 1015;
    private static final int CODE_GET_WPA_SUPP_CONFIG = 1001;
    private static final int CODE_IS_BG_LIMIT_ALLOWED = 3008;
    private static final int CODE_IS_SUPPORT_VOWIFI_DETECT = 1016;
    private static final int CODE_REQUEST_FRESH_WHITE_LIST = 2007;
    private static final int CODE_REQUEST_WIFI_ENABLE = 2004;
    private static final int CODE_SET_SOFTAP_DISASSOCIATESTA = 1007;
    private static final int CODE_SET_SOFTAP_MACFILTER = 1006;
    private static final int CODE_SET_VOWIFI_DETECT_MODE = 1012;
    private static final int CODE_SET_VOWIFI_DETECT_PERIOD = 1014;
    private static final int CODE_SET_WIFI_AP_EVALUATE_ENABLED = 1010;
    private static final int CODE_SET_WIFI_TXPOWER = 2005;
    private static final int CODE_START_PPPOE_CONFIG = 1002;
    private static final int CODE_STOP_PPPOE_CONFIG = 1003;
    private static final int CODE_UPDATE_APP_EXPERIENCE_STATUS = 3006;
    private static final int CODE_UPDATE_APP_RUNNING_STATUS = 3005;
    private static final int CODE_USER_HANDOVER_WIFI = 1008;
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
    private static HwInnerWifiManager mInstance = new HwInnerWifiManagerImpl();
    private String wifi_level_threshold = SystemProperties.get("ro.config.hw_wifi_signal_level");

    public static HwInnerWifiManager getDefault() {
        return mInstance;
    }

    public String getAppendSsidWithRandomUuid(WifiConfiguration config, Context context) {
        if (1 == Systemex.getInt(context.getContentResolver(), "hw_softap_ssid_extend", 0)) {
            config.SSID += UUID.randomUUID().toString().substring(13, 18);
            Log.d(TAG, "new SSID:" + config.SSID);
        }
        return config.SSID;
    }

    public int calculateSignalLevelHW(int freq, int rssi) {
        int rssi_Level_4 = -65;
        int rssi_Level_3 = -75;
        int rssi_Level_2 = -82;
        int rssi_Level_1 = -88;
        if (ScanResult.is5GHz(freq)) {
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
                    Log.i(TAG, "Exception occured,use the default level threshold! wifi_level_threshold = " + this.wifi_level_threshold, e);
                    rssi_Level_4 = -65;
                    rssi_Level_3 = -75;
                    rssi_Level_2 = -82;
                    rssi_Level_1 = -88;
                }
            } else {
                Log.e(TAG, "Customization of wifi level is illeagel! wifi_level_threshold =" + this.wifi_level_threshold);
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
                    Log.i(TAG, "Exception occured,use the default level threshold! wifi_level_threshold = " + this.wifi_level_threshold);
                    rssi_Level_1 = -88;
                    rssi_Level_2 = -82;
                    rssi_Level_3 = -75;
                    rssi_Level_4 = -65;
                }
            } else {
                Log.e(TAG, "Customization of wifi level is illeagel! wifi_level_threshold =" + this.wifi_level_threshold);
            }
        }
        if (rssi_Level_4 <= rssi) {
            return 4;
        }
        if (rssi_Level_3 <= rssi && rssi_Level_4 - 1 >= rssi) {
            return 3;
        }
        if (rssi_Level_2 > rssi || rssi_Level_3 - 1 < rssi) {
            return (rssi_Level_1 > rssi || rssi_Level_2 - 1 < rssi) ? 0 : 1;
        } else {
            return 2;
        }
    }

    public String getWpaSuppConfig() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        String str = TAG;
        Log.d(str, "getWpaSuppConfig");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(CODE_GET_WPA_SUPP_CONFIG, _data, _reply, 0);
                _reply.readException();
                str = _reply.readString();
                return str;
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
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "startPPPOE");
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
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public void stopPPPOE() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(1003, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public PPPOEInfo getPPPOEInfo() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        PPPOEInfo _result = null;
        IBinder b = ServiceManager.getService("wifi");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(1004, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() != 0) {
                    _result = (PPPOEInfo) PPPOEInfo.CREATOR.createFromParcel(_reply);
                } else {
                    _result = null;
                }
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
        return _result;
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
        if (wifiManager.getWifiState() != 3 || wifiInfo == null) {
            return false;
        }
        Log.d(TAG, "SupplicantState = " + wifiInfo.getSupplicantState());
        if (wifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
            return false;
        }
        boolean isPhoneAP = wifiInfo.getMeteredHint();
        Log.d(TAG, "isPhoneAP = " + isPhoneAP);
        return isPhoneAP;
    }

    public List<String> getApLinkedStaList() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        List<String> list = TAG;
        Log.d(list, "getApLinkedStaList");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(1005, _data, _reply, 0);
                _reply.readException();
                list = _reply.createStringArrayList();
                return list;
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
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "setSoftapMacFilter macFilter = " + macFilter);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(macFilter);
                b.transact(1006, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public void setSoftapDisassociateSta(String mac) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "setSoftapDisassociateSta mac = " + mac);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(mac);
                b.transact(1007, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public void userHandoverWifi() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "wifipro userHandoverWifi ");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(1008, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public void setWifiApEvaluateEnabled(boolean enabled) {
        int i = 0;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "setWifiApEvaluateEnabled enabled :" + enabled);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                if (enabled) {
                    i = 1;
                }
                _data.writeInt(i);
                Log.d(TAG, "setWifiApEvaluateEnabled enabled :" + enabled);
                b.transact(1010, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public int[] getChannelListFor5G() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        int[] _result = null;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            b.transact(1009, _data, _reply, 0);
            _reply.readException();
            _result = _reply.createIntArray();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

    public byte[] fetchWifiSignalInfoForVoWiFi() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "fetchWifiSignalInfoForVoWiFi");
        byte[] _result = null;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            b.transact(1011, _data, _reply, 0);
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
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "setVoWifiDetectMode info :" + info);
        boolean _result = true;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            if (info != null) {
                _data.writeInt(1);
                info.writeToParcel(_data, 0);
            } else {
                _data.writeInt(0);
            }
            b.transact(1012, _data, _reply, 0);
            _reply.readException();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
            _result = false;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

    public WifiDetectConfInfo getVoWifiDetectMode() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "getVoWifiDetectMode");
        WifiDetectConfInfo _result = null;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            b.transact(1013, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = (WifiDetectConfInfo) WifiDetectConfInfo.CREATOR.createFromParcel(_data);
            } else {
                _result = null;
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public boolean setVoWifiDetectPeriod(int period) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "setVoWifiDetectPeriod period :" + period);
        boolean _result = true;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(period);
            b.transact(1014, _data, _reply, 0);
            _reply.readException();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
            _result = false;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

    public int getVoWifiDetectPeriod() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "getVoWifiDetectPeriod");
        int _result = -1;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            b.transact(1015, _data, _reply, 0);
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
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "isSupportVoWifiDetect");
        boolean[] _result = new boolean[1];
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            b.transact(1016, _data, _reply, 0);
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

    public void enableHiLinkHandshake(boolean uiEnable, String bssid) {
        int i = 0;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            if (uiEnable) {
                i = 1;
            }
            _data.writeInt(i);
            _data.writeString(bssid);
            b.transact(2001, _data, _reply, 0);
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
        IBinder b = ServiceManager.getService("wifi");
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
        int i = 0;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "requestWifiEnable flag : " + flag + ", reason : " + reason);
        boolean _result = true;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            if (flag) {
                i = 1;
            }
            _data.writeInt(i);
            _data.writeString(reason);
            b.transact(CODE_REQUEST_WIFI_ENABLE, _data, _reply, 0);
            _reply.readException();
        } catch (RemoteException e) {
            Log.d(TAG, "requestWifiEnable, localRemoteException e");
            _result = false;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

    public boolean setWifiTxPower(int power) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        Log.i(TAG, "setWifiTxPower power :" + power);
        int _result = -1;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(power);
            b.transact(CODE_SET_WIFI_TXPOWER, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            Log.d(TAG, "setWifiTxPower, localRemoteException e");
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        if (_result == 0) {
            return true;
        }
        return false;
    }

    public boolean startHwQoEMonitor(int monitorType, int period, IHwQoECallback callback) {
        IBinder iBinder = null;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "startHwQoEMonitor period :" + period);
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(monitorType);
            _data.writeInt(period);
            if (callback != null) {
                iBinder = callback.asBinder();
            }
            _data.writeStrongBinder(iBinder);
            b.transact(3001, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            Log.d(TAG, "startHwQoEMonitor, localRemoteException e");
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        if (_result == 1) {
            return true;
        }
        return false;
    }

    public boolean stopHwQoEMonitor(int monitorType) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "stopHwQoEMonitor");
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(monitorType);
            b.transact(3002, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            Log.d(TAG, "stopHwQoEMonitor, localRemoteException e");
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        if (_result == 1) {
            return true;
        }
        return false;
    }

    public boolean evaluateNetworkQuality(IHwQoECallback callback) {
        IBinder iBinder = null;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "evaluateNetworkQuality");
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            if (callback != null) {
                iBinder = callback.asBinder();
            }
            _data.writeStrongBinder(iBinder);
            b.transact(3003, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            Log.d(TAG, "evaluateNetworkQuality, localRemoteException e");
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        if (_result == 1) {
            return true;
        }
        return false;
    }

    public boolean updateVOWIFIStatus(int state) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "updateVOWIFIState");
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(state);
            b.transact(3004, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            Log.d(TAG, "updateVOWIFIStatus, localRemoteException e");
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        if (_result == 1) {
            return true;
        }
        return false;
    }

    public boolean updateAppRunningStatus(int uid, int type, int status, int scene, int reserved) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(uid);
            _data.writeInt(type);
            _data.writeInt(status);
            _data.writeInt(scene);
            _data.writeInt(reserved);
            b.transact(3005, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            Log.d(TAG, "updateAppRunningStatus, localRemoteException e");
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        if (_result == 1) {
            return true;
        }
        return false;
    }

    public boolean updateAppExperienceStatus(int uid, int experience, long rtt, int reserved) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(uid);
            _data.writeInt(experience);
            _data.writeLong(rtt);
            _data.writeInt(reserved);
            b.transact(3006, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            Log.d(TAG, "updateAppExperienceStatus, localRemoteException e");
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
        int i = 0;
        if (context.checkCallingOrSelfPermission(HwWifiP2pManagerEx.MAGICLINK_PERMISSION) != 0) {
            Log.d(TAG, "No INSTANTSHARE permission");
            return;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        Log.d(TAG, "extendWifiScanPeriodForP2p flag : " + bExtend);
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            if (bExtend) {
                i = 1;
            }
            _data.writeInt(i);
            _data.writeInt(iTimes);
            b.transact(CODE_EXTEND_WIFI_SCAN_PERIOD_FOR_P2P, _data, _reply, 0);
            _reply.readException();
        } catch (RemoteException e) {
            Log.d(TAG, "extendWifiScanPeriodForP2p, localRemoteException e");
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public void refreshPackageWhitelist(int type, List<String> pkgList) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(type);
            _data.writeStringList(pkgList);
            b.transact(CODE_REQUEST_FRESH_WHITE_LIST, _data, _reply, 0);
            _reply.readException();
        } catch (RemoteException e) {
            Log.d(TAG, "refreshPackageWhitelist, localRemoteException e");
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public boolean isRSDBSupported() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
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
                Log.d(TAG, "isRSDBSupported, localRemoteException e");
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public boolean isBgLimitAllowed(int uid) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifi");
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
                Log.d(TAG, "isBGAllowed, localRemoteException e");
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }
}
