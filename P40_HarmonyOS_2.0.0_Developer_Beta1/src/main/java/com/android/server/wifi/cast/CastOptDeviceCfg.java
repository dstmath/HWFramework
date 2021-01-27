package com.android.server.wifi.cast;

import android.content.Context;
import android.net.MacAddress;
import android.net.wifi.IWifiCfgCallback;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.HwSoftApManager;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class CastOptDeviceCfg {
    private static final int BEGIN_OFFSET = 0;
    private static final int BSSID_LENGTH = 6;
    private static final int BYTE_MASK = 255;
    private static final int INDEX_LENGTH = 1;
    private static final int INDEX_START = 0;
    private static final int INDEX_VALUE = 2;
    private static final int INVALID_VALUE_OR_TYPE = -1;
    private static final int LENGTH_OFFSET = 1;
    private static final int MAX_BYTES = 255;
    private static final int MAX_PASSWORD_LENGTH = 64;
    private static final int MAX_SSID_LENGTH = 32;
    private static final int NORMAL_TYPE_LENGTH = 1;
    private static final String TAG = "CastOptDeviceCfg";
    private static final int THE_COUNT_OF_TYPE_FOR_CALLBACK = 4;
    private static final int TYPE_AND_LENGTH = 2;
    private static final int TYPE_AP_BSSID_FOR_CALLBACK = 4;
    private static final int TYPE_AP_SSID_FOR_CALLBACK = 3;
    private static final int TYPE_CREATE_DFS_CHANNEL = 6;
    private static final int TYPE_CREATE_INDOOR_CHANNEL = 7;
    private static final int TYPE_CSA = 4;
    private static final int TYPE_CURRENT_AP_BSSID = 11;
    private static final int TYPE_CURRENT_AP_IS_PORTAL = 9;
    private static final int TYPE_CURRENT_AP_PASSWORD = 14;
    private static final int TYPE_CURRENT_AP_ROUTE_TYPE = 16;
    private static final int TYPE_CURRENT_AP_SECURITY_TYPE = 15;
    private static final int TYPE_CURRENT_AP_SSID = 10;
    private static final int TYPE_CURRENT_SESSION_ID_TYPE = 18;
    private static final int TYPE_CURRENT_STA_CHANNEL = 8;
    private static final int TYPE_CURRENT_STA_HAS_INTERNET = 12;
    private static final int TYPE_DBDC = 3;
    private static final int TYPE_DEVICE_TYPE = 17;
    private static final int TYPE_OF_GET_SELF_CONFIG = 1;
    private static final int TYPE_OF_GET_SELF_CONFIG_WITH_PASSWORD = 2;
    private static final int TYPE_OF_SET_PEER_CONFIG = 1;
    private static final int TYPE_OF_SET_PEER_STATE_CHANGE = 2;
    private static final int TYPE_P2P_CHANNEL_OPT = 2;
    private static final int TYPE_P2P_SUPPORT_CHANNEL = 13;
    private static final int TYPE_RADAR_DETECT = 5;
    private static final int TYPE_STA_CHANNEL_FOR_CALLBACK = 2;
    private static final int TYPE_STA_STATE_FOR_CALLBACK = 1;
    private static final int TYPE_VERSION = 1;
    private static final int TYPICAL_TLV_LENGTH = 3;
    private static final int VALUE_OFFSET = 2;
    private static CastOptDeviceCfg sCastOptDeviceCfg = null;
    private IWifiCfgCallback mCallBack;
    private Context mContext;
    private DeviceWifiInfo mPeerDeviceInfo;
    private DeviceWifiInfo mSelfDeviceInfo;

    private CastOptDeviceCfg(Context context) {
        this.mSelfDeviceInfo = null;
        this.mPeerDeviceInfo = null;
        this.mCallBack = null;
        this.mContext = null;
        this.mSelfDeviceInfo = new DeviceWifiInfo();
        this.mSelfDeviceInfo.initSelfDeviceCfg();
        this.mPeerDeviceInfo = new DeviceWifiInfo();
        this.mContext = context;
    }

    protected static CastOptDeviceCfg createCastOptDeviceCfg(Context context) {
        if (sCastOptDeviceCfg == null) {
            sCastOptDeviceCfg = new CastOptDeviceCfg(context);
        }
        return sCastOptDeviceCfg;
    }

    protected static CastOptDeviceCfg getInstance() {
        return sCastOptDeviceCfg;
    }

    /* access modifiers changed from: protected */
    public DeviceWifiInfo getSelfDeviceCfgInfo() {
        return this.mSelfDeviceInfo;
    }

    /* access modifiers changed from: protected */
    public DeviceWifiInfo getPeerDeviceCfgInfo() {
        return this.mPeerDeviceInfo;
    }

    public class DeviceWifiInfo {
        private String mCurrentApBssid = "";
        private String mCurrentApSsid = "";
        private int mDeviceType = -1;
        private boolean mIsChooseP2pChannelOpt = false;
        private boolean mIsCurrentApHasInternet = false;
        private boolean mIsP2pSupportDfsChannel = false;
        private boolean mIsP2pSupportIndoorChannel = false;
        private boolean mIsP2pSupportRadarDetect = false;
        private boolean mIsPortalAp = false;
        private boolean mIsSupportCsa = false;
        private boolean mIsSupportDbdc = false;
        private List<Integer> mP2pSupportChannelList = new ArrayList();
        private String mPassword = "";
        private int mRouterType = -1;
        private int mSecurityType = -1;
        private String mSessionId = "";
        private int mStaChannel = 0;
        private int mVersion = 1;

        public DeviceWifiInfo() {
        }

        /* access modifiers changed from: protected */
        public void initSelfDeviceCfg() {
            this.mIsChooseP2pChannelOpt = CastOptUtils.isCastOptSupported();
            this.mIsSupportDbdc = CastOptUtils.isDbdcSupported();
            this.mIsSupportCsa = CastOptUtils.isCsaSupported();
            this.mIsP2pSupportRadarDetect = CastOptUtils.isP2pRadarDetectSupported();
            this.mDeviceType = CastOptUtils.getDeviceType();
        }

        /* access modifiers changed from: protected */
        public void resetDeviceWifiCfg() {
            this.mStaChannel = 0;
            this.mCurrentApSsid = "";
            this.mCurrentApBssid = "";
            this.mIsCurrentApHasInternet = false;
            this.mPassword = "";
            this.mIsPortalAp = false;
            this.mSecurityType = -1;
            this.mRouterType = -1;
        }

        /* access modifiers changed from: protected */
        public int getVersion() {
            HwHiLog.i(CastOptDeviceCfg.TAG, false, "getVersion :  %{public}d ", new Object[]{Integer.valueOf(this.mVersion)});
            return this.mVersion;
        }

        /* access modifiers changed from: protected */
        public void setVerison(int version) {
            if (version > 0) {
                this.mVersion = version;
            }
        }

        /* access modifiers changed from: protected */
        public boolean getCapsOfChooseP2pChannelOpt() {
            return this.mIsChooseP2pChannelOpt;
        }

        /* access modifiers changed from: protected */
        public void setCapsOfChooseP2pChannelOpt(boolean isEnabled) {
            this.mIsChooseP2pChannelOpt = isEnabled;
        }

        /* access modifiers changed from: protected */
        public boolean getCapsOfDbdc() {
            return this.mIsSupportDbdc;
        }

        /* access modifiers changed from: protected */
        public void setCapsOfDbdc(boolean isSupportDbdc) {
            this.mIsSupportDbdc = isSupportDbdc;
        }

        /* access modifiers changed from: protected */
        public boolean getCapsOfCsa() {
            return this.mIsSupportCsa;
        }

        /* access modifiers changed from: protected */
        public void setCapsOfCsa(boolean isSupportCsa) {
            this.mIsSupportCsa = isSupportCsa;
        }

        /* access modifiers changed from: protected */
        public boolean getCapsOfP2pRadarDetect() {
            return this.mIsP2pSupportRadarDetect;
        }

        /* access modifiers changed from: protected */
        public void setCapsOfP2pRadarDetect(boolean isP2pSupportRadarDetect) {
            this.mIsP2pSupportRadarDetect = isP2pSupportRadarDetect;
        }

        /* access modifiers changed from: protected */
        public boolean getCapsOfP2pDfsChannel() {
            return this.mIsP2pSupportDfsChannel;
        }

        /* access modifiers changed from: protected */
        public void setCapsOfP2pDfsChannel(boolean isP2pSupportDfsChannel) {
            this.mIsP2pSupportDfsChannel = isP2pSupportDfsChannel;
        }

        /* access modifiers changed from: protected */
        public boolean getCapsOfP2pIndoorChannel() {
            return this.mIsP2pSupportIndoorChannel;
        }

        /* access modifiers changed from: protected */
        public void setCapsOfP2pIndoorChannel(boolean isP2pSupportIndoorChannel) {
            this.mIsP2pSupportIndoorChannel = isP2pSupportIndoorChannel;
        }

        /* access modifiers changed from: protected */
        public void setStaChannel(int channel) {
            HwHiLog.i(CastOptDeviceCfg.TAG, false, "setStaChannel :  %{public}d ", new Object[]{Integer.valueOf(channel)});
            if (channel >= 0) {
                this.mStaChannel = channel;
            }
        }

        /* access modifiers changed from: protected */
        public int getStaChannel() {
            return this.mStaChannel;
        }

        /* access modifiers changed from: protected */
        public boolean getApType() {
            return this.mIsPortalAp;
        }

        /* access modifiers changed from: protected */
        public void setApType(boolean isPortalAp) {
            this.mIsPortalAp = isPortalAp;
        }

        /* access modifiers changed from: protected */
        public String getCurrentApSsid() {
            return this.mCurrentApSsid;
        }

        /* access modifiers changed from: protected */
        public void setCurrentApSsid(String ssid) {
            this.mCurrentApSsid = ssid;
        }

        /* access modifiers changed from: protected */
        public String getCurrentApBssid() {
            return this.mCurrentApBssid;
        }

        /* access modifiers changed from: protected */
        public void setCurrentApBssid(String bssid) {
            this.mCurrentApBssid = bssid;
        }

        /* access modifiers changed from: protected */
        public void setCurrentApHasInternet(boolean hasInternet) {
            this.mIsCurrentApHasInternet = hasInternet;
        }

        /* access modifiers changed from: protected */
        public boolean getCurrentApHasInternet() {
            return this.mIsCurrentApHasInternet;
        }

        /* access modifiers changed from: protected */
        public List<Integer> getP2pSupportChannel() {
            return this.mP2pSupportChannelList;
        }

        /* access modifiers changed from: protected */
        public void setP2pSupportChannel(int[] channels) {
            if (!(channels == null || channels.length == 0)) {
                if (this.mP2pSupportChannelList.size() > 0) {
                    this.mP2pSupportChannelList.clear();
                }
                for (int channel : channels) {
                    this.mP2pSupportChannelList.add(Integer.valueOf(channel));
                    HwHiLog.i(CastOptDeviceCfg.TAG, false, "setP2pSupportChannel : %{public}d ", new Object[]{Integer.valueOf(channel)});
                }
            }
        }

        /* access modifiers changed from: protected */
        public String getApPassword() {
            return this.mPassword;
        }

        /* access modifiers changed from: protected */
        public void setApPassword(String password) {
            this.mPassword = password;
        }

        /* access modifiers changed from: protected */
        public int getApSecurityType() {
            return this.mSecurityType;
        }

        /* access modifiers changed from: protected */
        public void setApSecurityType(int securityType) {
            if (securityType == 255) {
                securityType = -1;
            }
            this.mSecurityType = securityType;
        }

        /* access modifiers changed from: protected */
        public int getRouterType() {
            return this.mRouterType;
        }

        /* access modifiers changed from: protected */
        public String generateSessionId() {
            this.mSessionId = CastOptChr.generateSessionId();
            return this.mSessionId;
        }

        /* access modifiers changed from: protected */
        public String getSessionId() {
            return this.mSessionId;
        }

        /* access modifiers changed from: protected */
        public void setSessionId(String sessionId) {
            HwHiLog.i(CastOptDeviceCfg.TAG, false, "setSessionId :" + CastOptChr.safePrintSessionId(sessionId), new Object[0]);
            this.mSessionId = sessionId;
        }

        /* access modifiers changed from: protected */
        public void clearSessionId() {
            this.mSessionId = "";
        }

        /* access modifiers changed from: protected */
        public void setRouteType(int routerType) {
            if (routerType == 255) {
                routerType = -1;
            }
            this.mRouterType = routerType;
        }

        /* access modifiers changed from: protected */
        public int getDeviceType() {
            return this.mDeviceType;
        }

        /* access modifiers changed from: protected */
        public void setDeviceType(int deviceType) {
            if (deviceType == 255) {
                deviceType = -1;
            }
            this.mDeviceType = deviceType;
        }

        /* access modifiers changed from: protected */
        public int getValueFromType(int type) {
            switch (type) {
                case 1:
                    return getVersion();
                case 2:
                    return getCapsOfChooseP2pChannelOpt() ? 1 : 0;
                case 3:
                    return getCapsOfDbdc() ? 1 : 0;
                case 4:
                    return getCapsOfCsa() ? 1 : 0;
                case 5:
                    return getCapsOfP2pRadarDetect() ? 1 : 0;
                case 6:
                    return getCapsOfP2pDfsChannel() ? 1 : 0;
                case 7:
                    return getCapsOfP2pIndoorChannel() ? 1 : 0;
                case 8:
                    return getStaChannel();
                case 9:
                    return getApType() ? 1 : 0;
                default:
                    HwHiLog.i(CastOptDeviceCfg.TAG, false, "getValueFromType invalid type = %{public}d", new Object[]{Integer.valueOf(type)});
                    return -1;
            }
        }

        /* access modifiers changed from: protected */
        public void setDeviceCfg(int type, int inputValue) {
            boolean z = false;
            switch (type) {
                case 1:
                    setVerison(inputValue);
                    return;
                case 2:
                    if (inputValue == 1) {
                        z = true;
                    }
                    setCapsOfChooseP2pChannelOpt(z);
                    return;
                case 3:
                    if (inputValue == 1) {
                        z = true;
                    }
                    setCapsOfDbdc(z);
                    return;
                case 4:
                    if (inputValue == 1) {
                        z = true;
                    }
                    setCapsOfCsa(z);
                    return;
                case 5:
                    if (inputValue == 1) {
                        z = true;
                    }
                    setCapsOfP2pRadarDetect(z);
                    return;
                case 6:
                    if (inputValue == 1) {
                        z = true;
                    }
                    setCapsOfP2pDfsChannel(z);
                    return;
                case 7:
                    if (inputValue == 1) {
                        z = true;
                    }
                    setCapsOfP2pIndoorChannel(z);
                    return;
                case 8:
                    setStaChannel(inputValue);
                    return;
                case 9:
                    if (inputValue == 1) {
                        z = true;
                    }
                    setApType(z);
                    return;
                default:
                    HwHiLog.i(CastOptDeviceCfg.TAG, false, "setDeviceCfg invalid type = %{public}d", new Object[]{Integer.valueOf(type)});
                    return;
            }
        }
    }

    private String parseStringFromByteArray(byte[] data, int index, int maxLength) {
        if (data == null || data.length < index + 2) {
            return "";
        }
        int length = data[index + 1] & 255;
        byte[] ssidArray = new byte[maxLength];
        if (length > maxLength) {
            HwHiLog.e(TAG, false, "invalid length = %{public}d", new Object[]{Integer.valueOf(length)});
            return "";
        }
        System.arraycopy(data, index + 2, ssidArray, 0, length);
        return new String(ssidArray, 0, findIndexFromByteArray(ssidArray, 0), Charset.forName("UTF-8"));
    }

    private int[] parseP2pSupportChannelFromByteArray(byte[] data, int index) {
        if (data == null || data.length < index + 2) {
            return new int[0];
        }
        int length = data[index + 1] & 255;
        int[] p2pSupportChannels = new int[length];
        HwHiLog.i(TAG, false, "p2pSupportChannels.size = %{public}d", new Object[]{Integer.valueOf(length)});
        if (data.length < index + length) {
            return new int[0];
        }
        for (int i = 0; i < data[index + 1]; i++) {
            p2pSupportChannels[i] = data[index + 2 + i] & 255;
        }
        return p2pSupportChannels;
    }

    private void handlePeerApPassword() {
        CastOptChr castOptChr = CastOptChr.getInstance();
        if (castOptChr != null) {
            castOptChr.setIsSameAcct(true);
        }
        DeviceWifiInfo deviceWifiInfo = this.mPeerDeviceInfo;
        if (deviceWifiInfo != null && this.mSelfDeviceInfo != null) {
            Boolean isPeerApHasInternet = Boolean.valueOf(deviceWifiInfo.getCurrentApHasInternet());
            Boolean isPeerApPortal = Boolean.valueOf(this.mPeerDeviceInfo.getApType());
            if (!isPeerApHasInternet.booleanValue() || isPeerApPortal.booleanValue()) {
                HwHiLog.i(TAG, false, "peer ap has no internet or is portal network, return", new Object[0]);
                return;
            }
            String peerApBssid = this.mPeerDeviceInfo.getCurrentApBssid();
            String selfApBssid = this.mSelfDeviceInfo.getCurrentApBssid();
            CastOptGcActioner castOptGcActioner = CastOptGcActioner.getInstance();
            if (castOptGcActioner != null && peerApBssid != null && !peerApBssid.equals(selfApBssid)) {
                Bundle data = new Bundle();
                data.putString("ssid", this.mPeerDeviceInfo.getCurrentApSsid());
                data.putString("bssid", this.mPeerDeviceInfo.getCurrentApBssid());
                data.putString("password", this.mPeerDeviceInfo.getApPassword());
                data.putBoolean("hasInternet", this.mPeerDeviceInfo.getCurrentApHasInternet());
                data.putInt("securityType", this.mPeerDeviceInfo.getApSecurityType());
                castOptGcActioner.updateWifiConfig(data);
            }
        }
    }

    private void handleTlvData(int type, byte[] cfgData, int index) {
        boolean z = true;
        switch (type) {
            case 10:
                this.mPeerDeviceInfo.setCurrentApSsid(parseStringFromByteArray(cfgData, index, 32));
                return;
            case 11:
                byte[] macByteAddr = new byte[6];
                System.arraycopy(cfgData, index + 2, macByteAddr, 0, 6);
                this.mPeerDeviceInfo.setCurrentApBssid(MacAddress.stringAddrFromByteAddr(macByteAddr));
                return;
            case 12:
                DeviceWifiInfo deviceWifiInfo = this.mPeerDeviceInfo;
                if ((cfgData[index + 2] & 255) != 1) {
                    z = false;
                }
                deviceWifiInfo.setCurrentApHasInternet(z);
                return;
            case 13:
                this.mPeerDeviceInfo.setP2pSupportChannel(parseP2pSupportChannelFromByteArray(cfgData, index));
                return;
            case 14:
                this.mPeerDeviceInfo.setApPassword(parseStringFromByteArray(cfgData, index, 64));
                return;
            case 15:
                this.mPeerDeviceInfo.setApSecurityType(cfgData[index + 2] & 255);
                HwHiLog.i(TAG, false, "parsePeerDeviceCfgInfo SecurityType = %{public}d", new Object[]{Integer.valueOf(this.mPeerDeviceInfo.getApSecurityType())});
                return;
            case 16:
                this.mPeerDeviceInfo.setRouteType(cfgData[index + 2] & 255);
                HwHiLog.i(TAG, false, "parsePeerDeviceCfgInfo RouterType = %{public}d", new Object[]{Integer.valueOf(this.mPeerDeviceInfo.getRouterType())});
                return;
            case 17:
                this.mPeerDeviceInfo.setDeviceType(cfgData[index + 2] & 255);
                HwHiLog.i(TAG, false, "parsePeerDeviceCfgInfo DeviceType = %{public}d", new Object[]{Integer.valueOf(this.mPeerDeviceInfo.getDeviceType())});
                return;
            case 18:
                this.mPeerDeviceInfo.setSessionId(parseStringFromByteArray(cfgData, index, 20));
                return;
            default:
                HwHiLog.i(TAG, false, "parsePeerDeviceCfgInfo invalid type = %{public}d", new Object[]{Integer.valueOf(type)});
                return;
        }
    }

    /* access modifiers changed from: protected */
    public int parsePeerDeviceCfgInfo(int cfgType, byte[] cfgData) {
        DeviceWifiInfo deviceWifiInfo = this.mPeerDeviceInfo;
        if (deviceWifiInfo == null || cfgData == null || cfgData.length == 0 || cfgType != 1) {
            return -1;
        }
        deviceWifiInfo.resetDeviceWifiCfg();
        int length = cfgData[0] & 255;
        if (cfgData.length != length) {
            return -1;
        }
        HwHiLog.i(TAG, false, "length = %{public}d", new Object[]{Integer.valueOf(length)});
        int i = 1;
        while (true) {
            if (i >= length || i + 1 >= length) {
                break;
            }
            try {
                int valueLength = cfgData[i + 1] & 255;
                if (i + valueLength + 2 > length) {
                    break;
                }
                byte b = cfgData[i];
                int valueIndex = i + 2;
                if (b <= 0 || b > 9 || valueIndex >= length) {
                    handleTlvData(b, cfgData, i);
                    i += valueLength + 2;
                } else {
                    this.mPeerDeviceInfo.setDeviceCfg(b, cfgData[valueIndex] & 255);
                    i += valueLength + 2;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                HwHiLog.e(TAG, false, "parse peer device info exception", new Object[0]);
            }
        }
        if (this.mPeerDeviceInfo.getApSecurityType() != -1) {
            handlePeerApPassword();
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public int parsePeerDeviceStaChanngeInfo(int cfgType, byte[] cfgData) {
        if (this.mPeerDeviceInfo == null || cfgData == null || cfgData.length == 0 || cfgType != 2) {
            return -1;
        }
        if (cfgData.length != cfgData[0]) {
            HwHiLog.i(TAG, false, "cfgData is invalid", new Object[0]);
            return -1;
        }
        int i = 1;
        while (i < cfgData.length) {
            try {
                byte b = cfgData[i];
                if (b == 1) {
                    handlePeerStaStateChange(cfgData, i);
                } else if (b == 2) {
                    this.mPeerDeviceInfo.setDeviceCfg(8, cfgData[i + 2] & 255);
                    handleDeviceChannelChange();
                } else if (b == 3) {
                    this.mPeerDeviceInfo.setCurrentApSsid(parseStringFromByteArray(cfgData, i, 32));
                } else if (b != 4) {
                    HwHiLog.i(TAG, false, "parse sta channge info invalid type = %{public}d, i = %{public}d", new Object[]{Integer.valueOf(b), Integer.valueOf(i)});
                    return 0;
                } else {
                    byte[] macByteAddr = new byte[6];
                    System.arraycopy(cfgData, i + 2, macByteAddr, 0, 6);
                    this.mPeerDeviceInfo.setCurrentApBssid(MacAddress.stringAddrFromByteAddr(macByteAddr));
                }
                i += (cfgData[i + 1] & 255) + 2;
            } catch (ArrayIndexOutOfBoundsException e) {
                HwHiLog.e(TAG, false, "parse peer sta change info exception", new Object[0]);
            }
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public byte[] getSelfDeviceCfg(int cfgType) {
        DeviceWifiInfo deviceWifiInfo;
        if (!(cfgType == 1 || cfgType == 2) || (deviceWifiInfo = this.mSelfDeviceInfo) == null) {
            return new byte[0];
        }
        deviceWifiInfo.setP2pSupportChannel(HwSoftApManager.getChannelListFor5GWithoutIndoor());
        byte[] totalCfgInfo = new byte[255];
        for (int i = 0; i < totalCfgInfo.length; i++) {
            totalCfgInfo[i] = 0;
        }
        int position = 1;
        for (int i2 = 1; i2 <= 9; i2++) {
            position += setTlvByteNormal(i2, this.mSelfDeviceInfo.getValueFromType(i2), totalCfgInfo, position);
        }
        int position2 = position + setTlvByte(this.mSelfDeviceInfo.getCurrentApSsid(), 10, totalCfgInfo, position);
        int position3 = position2 + setBssidByte(11, totalCfgInfo, position2);
        int position4 = position3 + setTlvByteNormal(12, this.mSelfDeviceInfo.getCurrentApHasInternet() ? 1 : 0, totalCfgInfo, position3);
        int position5 = position4 + setP2pSupportChannelByte(totalCfgInfo, position4);
        if (cfgType == 2 && CastOptUtils.isSyncWifiConfigSwitchOn(this.mContext)) {
            int position6 = position5 + setTlvByte(this.mSelfDeviceInfo.getApPassword(), 14, totalCfgInfo, position5);
            position5 = position6 + setTlvByteNormal(15, this.mSelfDeviceInfo.getApSecurityType(), totalCfgInfo, position6);
            CastOptChr castOptChr = CastOptChr.getInstance();
            if (castOptChr != null) {
                castOptChr.setIsSameAcct(true);
            }
        }
        int position7 = position5 + setTlvByteNormal(16, this.mSelfDeviceInfo.getRouterType(), totalCfgInfo, position5);
        int position8 = position7 + setTlvByteNormal(17, this.mSelfDeviceInfo.getDeviceType(), totalCfgInfo, position7);
        totalCfgInfo[0] = (byte) (position8 + setTlvByte(this.mSelfDeviceInfo.generateSessionId(), 18, totalCfgInfo, position8));
        HwHiLog.i(TAG, false, "totalCfgInfo[0]=%{public}d ", new Object[]{Integer.valueOf(totalCfgInfo[0] & 255)});
        byte[] cfgInfo = new byte[(totalCfgInfo[0] & 255)];
        System.arraycopy(totalCfgInfo, 0, cfgInfo, 0, 255 & totalCfgInfo[0]);
        return cfgInfo;
    }

    /* access modifiers changed from: protected */
    public int findIndexFromByteArray(byte[] data, int value) {
        if (data == null) {
            return 0;
        }
        int i = 0;
        while (i < data.length && data[i] != value) {
            i++;
        }
        return i;
    }

    /* access modifiers changed from: protected */
    public int setPeerWifiCfgInfo(int cfgType, byte[] cfgData) {
        HwHiLog.i(TAG, false, "setPeerWifiCfgInfo  cfgType = %{public}d", new Object[]{Integer.valueOf(cfgType)});
        if (cfgType == 1) {
            return parsePeerDeviceCfgInfo(cfgType, cfgData);
        }
        if (cfgType == 2) {
            return parsePeerDeviceStaChanngeInfo(cfgType, cfgData);
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public int registerWifiCfgCallback(IWifiCfgCallback callback) {
        if (callback != null) {
            this.mCallBack = callback;
        }
        HwHiLog.i(TAG, false, "registerWifiCfgCallback", new Object[0]);
        return 0;
    }

    /* access modifiers changed from: protected */
    public int unregisterWifiCfgCallback() {
        this.mCallBack = null;
        HwHiLog.i(TAG, false, "unregisterWifiCfgCallback", new Object[0]);
        return 0;
    }

    private int setBssidByte(int typeBssidValue, byte[] dataArray, int offset) {
        byte[] macByteAddr;
        DeviceWifiInfo deviceWifiInfo = this.mSelfDeviceInfo;
        if (deviceWifiInfo == null || dataArray == null || dataArray.length < offset + 8) {
            return 0;
        }
        dataArray[offset] = (byte) typeBssidValue;
        String bssid = deviceWifiInfo.getCurrentApBssid();
        dataArray[offset + 1] = 6;
        if (bssid == null || TextUtils.isEmpty(bssid)) {
            macByteAddr = new byte[6];
        } else {
            macByteAddr = MacAddress.byteAddrFromStringAddr(bssid);
        }
        System.arraycopy(macByteAddr, 0, dataArray, offset + 2, 6);
        return 8;
    }

    private int setP2pSupportChannelByte(byte[] dataArray, int offset) {
        List<Integer> p2pSupportChannelList;
        DeviceWifiInfo deviceWifiInfo = this.mSelfDeviceInfo;
        if (deviceWifiInfo == null || dataArray == null || dataArray.length < offset + 1 || (p2pSupportChannelList = deviceWifiInfo.getP2pSupportChannel()) == null) {
            return 0;
        }
        dataArray[offset] = 13;
        dataArray[offset + 1] = (byte) p2pSupportChannelList.size();
        byte b = dataArray[offset + 1];
        if (dataArray.length < offset + b + 2) {
            return 0;
        }
        for (int i = 0; i < dataArray[offset + 1]; i++) {
            dataArray[offset + 2 + i] = (byte) p2pSupportChannelList.get(i).intValue();
        }
        return b + 2;
    }

    private int setTlvByteNormal(int type, int typeValue, byte[] dataArray, int offset) {
        if (this.mSelfDeviceInfo == null || dataArray == null || dataArray.length <= offset + 2) {
            return 0;
        }
        dataArray[offset] = (byte) type;
        dataArray[offset + 1] = 1;
        dataArray[offset + 2] = (byte) typeValue;
        return 3;
    }

    private int setTlvByte(String type, int typeValue, byte[] dataArray, int offset) {
        if (this.mSelfDeviceInfo == null || dataArray == null) {
            return 0;
        }
        if (TextUtils.isEmpty(type) || ((typeValue == 14 && type.length() > 64) || (typeValue == 10 && type.length() > 32))) {
            dataArray[offset] = (byte) typeValue;
            dataArray[offset + 1] = 1;
            dataArray[offset + 2] = 0;
            return 3;
        }
        byte[] getByte = type.getBytes(Charset.forName("UTF-8"));
        if (dataArray.length < getByte.length + offset) {
            return 0;
        }
        dataArray[offset] = (byte) typeValue;
        dataArray[offset + 1] = (byte) getByte.length;
        if (getByte.length == 1) {
            try {
                dataArray[offset + 2] = (byte) Integer.parseInt(type);
            } catch (NumberFormatException e) {
                HwHiLog.e(TAG, false, "parse type Error", new Object[0]);
            }
        } else {
            System.arraycopy(getByte, 0, dataArray, offset + 2, getByte.length);
        }
        return dataArray[offset + 1] + 2;
    }

    private void handlePeerStaStateChange(byte[] data, int index) {
        if (data != null && data.length > index + 2 && data[index + 2] == 0) {
            this.mPeerDeviceInfo.resetDeviceWifiCfg();
            handleDeviceChannelChange();
        }
    }

    private void handleDeviceChannelChange() {
        CastOptGoActioner castOptGoActioner = CastOptGoActioner.getInstance();
        if (castOptGoActioner != null) {
            castOptGoActioner.handleDeviceChannelChange();
        }
    }

    /* access modifiers changed from: protected */
    public void doCallback(boolean isStaConnected) {
        try {
            if (this.mCallBack != null) {
                if (!isStaConnected) {
                    byte[] callbackInfo = new byte[4];
                    callbackInfo[0] = (byte) callbackInfo.length;
                    setTlvByte(Integer.toString(0), 1, callbackInfo, 1);
                    this.mCallBack.onNotifyWifiCfg(1, callbackInfo);
                } else {
                    byte[] callbackInfo2 = new byte[255];
                    int position = 1 + setTlvByte(Integer.toString(1), 1, callbackInfo2, 1);
                    callbackInfo2[position] = 2;
                    callbackInfo2[position + 1] = 1;
                    callbackInfo2[position + 2] = (byte) this.mSelfDeviceInfo.getStaChannel();
                    int position2 = position + callbackInfo2[position + 1] + 2;
                    int position3 = position2 + setTlvByte(this.mSelfDeviceInfo.getCurrentApSsid(), 3, callbackInfo2, position2);
                    int position4 = position3 + setBssidByte(4, callbackInfo2, position3);
                    callbackInfo2[0] = (byte) position4;
                    byte[] cfgInfo = new byte[position4];
                    System.arraycopy(callbackInfo2, 0, cfgInfo, 0, position4);
                    this.mCallBack.onNotifyWifiCfg(1, cfgInfo);
                }
                HwHiLog.i(TAG, false, "sta doCallback", new Object[0]);
                return;
            }
            HwHiLog.i(TAG, false, "sta doCallback : mCallBack is null", new Object[0]);
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "Exceptions happen at doCallback", new Object[0]);
        }
    }
}
