package com.android.server.hidata.mplink;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Message;
import android.telephony.ServiceState;
import com.android.server.gesture.GestureNavConst;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import huawei.android.net.hwmplink.MpLinkCommonUtils;

public class HwMpLinkTelephonyImpl {
    public static final float INTER_DISTURB_BANDWIDTH_RATE = 0.1f;
    private static final int NETWORK_REQUEST_TIMEOUT_MILLIS = 60000;
    private static final int SECOND_HARMONIC = 2;
    private static final String TAG = "HiData_HwMpLinkTelephonyImpl";
    private static final int THIRD_HARMONIC = 3;
    private int dealMobileDataRef = 0;
    private ConnectivityManager mConnectivityManager = null;
    private Context mContext;
    private float mCurrentDataBandWidth = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mCurrentDataFreq = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private boolean mCurrentDataRoamingState = false;
    private int mCurrentDataTechType = 0;
    private int mCurrentServceState = 1;
    private int mCurrentWifiBandWidth = 0;
    private int mCurrentWifiFreq = 0;
    private int mDefaultDataSubId = 0;
    private Handler mHandler;
    private boolean mIsDataTechSuitable = false;
    private boolean mIsInterDisturbExist = false;
    /* access modifiers changed from: private */
    public boolean mIsMobileDataAvailable = false;
    private final ConnectivityManager.NetworkCallback mListenNetworkCallback = new ConnectivityManager.NetworkCallback() {
        {
            MpLinkCommonUtils.logD(HwMpLinkTelephonyImpl.TAG, "onUnavailable");
        }

        public void onAvailable(Network network, NetworkCapabilities networkCapabilities, LinkProperties linkProperties) {
            String iface = linkProperties.getInterfaceName();
            MpLinkCommonUtils.logD(HwMpLinkTelephonyImpl.TAG, "onAvailable,iface:" + iface);
            if (iface != null) {
                boolean unused = HwMpLinkTelephonyImpl.this.mIsMobileDataAvailable = true;
                HwMpLinkTelephonyImpl.this.sendMessage(HwMpLinkServiceImpl.MPLINK_MSG_MOBILE_DATA_AVAILABLE);
            }
        }

        public void onUnavailable() {
        }
    };
    private boolean mMobileConnectState = false;
    private int mMobileDataSwitchState = -1;
    private String mMobileIface = "";
    private ConnectivityManager.NetworkCallback mNetworkCallback;
    NetworkRequest mNetworkRequestForCallback = new NetworkRequest.Builder().addTransportType(0).addCapability(12).build();
    private int mReportRat = 0;

    public HwMpLinkTelephonyImpl(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        getConnectiviyManger();
        registerNetworkCallback();
    }

    private void registerNetworkCallback() {
        if (this.mConnectivityManager != null) {
            this.mConnectivityManager.registerNetworkCallback(this.mNetworkRequestForCallback, this.mListenNetworkCallback, this.mHandler);
        }
    }

    private void getConnectiviyManger() {
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
    }

    /* access modifiers changed from: private */
    public void sendMessage(int what) {
        this.mHandler.sendMessage(Message.obtain(this.mHandler, what));
    }

    public int getActiveNetworkType() {
        getConnectiviyManger();
        if (this.mConnectivityManager == null) {
            return -1;
        }
        NetworkInfo netinfo = this.mConnectivityManager.getActiveNetworkInfo();
        if (netinfo != null) {
            return netinfo.getType();
        }
        return -1;
    }

    public boolean isMobileDataEnable() {
        boolean isMobileEnable = false;
        getConnectiviyManger();
        if (this.mConnectivityManager != null) {
            isMobileEnable = this.mConnectivityManager.getMobileDataEnabled();
        } else {
            MpLinkCommonUtils.logD(TAG, "isMobileDataEnable mConnectivityManager is null");
        }
        MpLinkCommonUtils.logD(TAG, "isMobileDataEnable " + isMobileEnable);
        return isMobileEnable;
    }

    public void closeMobileDataIfOpened() {
        MpLinkCommonUtils.logD(TAG, "closeMobileDataIfOpened dealMobileDataRef:" + this.dealMobileDataRef);
        if (this.dealMobileDataRef > 0) {
            mplinkSetMobileData(false);
        }
    }

    public int mplinkSetMobileData(boolean enable) {
        int ret = -1;
        MpLinkCommonUtils.logD(TAG, "mplinkSetMobileData dealMobileDataRef: " + this.dealMobileDataRef + ", enable :" + enable);
        if (isMobileDataEnable() || !enable) {
            boolean connectState = isMobileConnected();
            if (enable) {
                if (connectState) {
                    MpLinkCommonUtils.logD(TAG, "mplinkSetMobileData already connected");
                } else if (this.dealMobileDataRef == 0) {
                    this.dealMobileDataRef = 1;
                    enableMobileData(true);
                    ret = 0;
                }
            } else if (this.dealMobileDataRef > 0) {
                enableMobileData(false);
                this.dealMobileDataRef = 0;
            } else {
                MpLinkCommonUtils.logD(TAG, "mplinkSetMobileData mplink do not open MobileData");
            }
            return ret;
        }
        MpLinkCommonUtils.logD(TAG, "mplinkSetMobileData Mobile switch closed");
        return 2;
    }

    private void enableMobileData(boolean enable) {
        MpLinkCommonUtils.logD(TAG, "enableMobileData :" + enable);
        if (enable) {
            startNetworkForMpLink();
        } else {
            stopNetworkForMpLink();
        }
    }

    public boolean getCurrentDataTechSuitable() {
        return this.mIsDataTechSuitable;
    }

    private void startNetworkForMpLink() {
        MpLinkCommonUtils.logD(TAG, "startNetworkForMpLink");
        NetworkRequest mNetworkRequest = new NetworkRequest.Builder().addTransportType(0).addCapability(12).setNetworkSpecifier(Integer.toString(getDefaultDataSubId())).build();
        this.mNetworkCallback = new ConnectivityManager.NetworkCallback();
        if (this.mNetworkCallback != null && this.mConnectivityManager != null) {
            this.mConnectivityManager.requestNetwork(mNetworkRequest, this.mNetworkCallback, 60000);
        }
    }

    private void stopNetworkForMpLink() {
        MpLinkCommonUtils.logD(TAG, "stopNetworkForMpLink");
        if (this.mNetworkCallback != null && this.mConnectivityManager != null) {
            try {
                this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
            } catch (IllegalArgumentException e) {
                MpLinkCommonUtils.logD(TAG, "Unregister network callback exception");
            } catch (Throwable th) {
                this.mNetworkCallback = null;
                throw th;
            }
            this.mNetworkCallback = null;
        }
    }

    public int getDefaultDataSubId() {
        return this.mDefaultDataSubId;
    }

    public boolean getCurrentDataRoamingState() {
        return this.mCurrentDataRoamingState;
    }

    public int getCurrentServceState() {
        return this.mCurrentServceState;
    }

    public boolean getCurrentMobileConnectState() {
        return this.mMobileConnectState;
    }

    public void handleDataSubChange(int subId) {
        MpLinkCommonUtils.logD(TAG, "DataSub Change, new subId:" + subId);
        if (subId != -1 && this.mDefaultDataSubId != subId) {
            this.mDefaultDataSubId = subId;
            sendMessage(HwMpLinkServiceImpl.MPLINK_MSG_DATA_SUB_CHANGE);
        }
    }

    public int getCurrentDataTech() {
        return this.mCurrentDataTechType;
    }

    public boolean isFreqInterdisturbExist() {
        return this.mIsInterDisturbExist;
    }

    public void calculateInterdisturb() {
        this.mIsInterDisturbExist = harmonicInterdisturb(2) || harmonicInterdisturb(3);
    }

    public boolean harmonicInterdisturb(int num) {
        float intersectLowFreq;
        float intersectHighFreq;
        MpLinkCommonUtils.logD(TAG, "Enter harmonicInterdisturb: mCurrentWifiFreq is " + this.mCurrentWifiFreq + ",mCurrentWifiBandWidth is " + this.mCurrentWifiBandWidth + ",mCurrentDataFreq is " + this.mCurrentDataFreq + ",mCurrentDataBandWidth is " + this.mCurrentDataBandWidth);
        if (this.mReportRat == 0 || GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO == this.mCurrentDataFreq || GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO == this.mCurrentDataBandWidth) {
            return true;
        }
        if (((float) this.mCurrentWifiBandWidth) > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            float wifiLowFreq = (float) (this.mCurrentWifiFreq - (this.mCurrentWifiBandWidth / 2));
            float wifiHighFreq = (float) (this.mCurrentWifiFreq + (this.mCurrentWifiBandWidth / 2));
            float dataLowFreq = (this.mCurrentDataFreq * ((float) num)) - ((this.mCurrentDataBandWidth * ((float) num)) / 2.0f);
            float dataHighFreq = (this.mCurrentDataFreq * ((float) num)) + ((this.mCurrentDataBandWidth * ((float) num)) / 2.0f);
            if (wifiHighFreq <= dataLowFreq || dataHighFreq <= wifiLowFreq) {
                return false;
            }
            if (Float.compare(wifiLowFreq, dataLowFreq) > 0) {
                intersectLowFreq = wifiLowFreq;
            } else {
                intersectLowFreq = dataLowFreq;
            }
            if (Float.compare(wifiHighFreq, dataHighFreq) > 0) {
                intersectHighFreq = dataHighFreq;
            } else {
                intersectHighFreq = wifiHighFreq;
            }
            if ((intersectHighFreq - intersectLowFreq) / ((float) this.mCurrentWifiBandWidth) >= 0.1f) {
                return true;
            }
        }
        return false;
    }

    public void upDataCellUlFreqInfo(HwMpLinkInterDisturbInfo disturbInfo) {
        this.mReportRat = disturbInfo.mRat;
        this.mCurrentDataBandWidth = ((float) disturbInfo.mUlbw) / 1000.0f;
        this.mCurrentDataFreq = ((float) disturbInfo.mUlfreq) / 10.0f;
        MpLinkCommonUtils.logD(TAG, "upDataCellUlFreqInfo, mReportRat = " + this.mReportRat + ", mCurrentDataBandWidth = " + this.mCurrentDataBandWidth + ", mCurrentDataFreq = " + this.mCurrentDataFreq);
        calculateInterdisturb();
    }

    public boolean isMobileConnected() {
        return this.mMobileConnectState;
    }

    public String getMobileIface() {
        return this.mMobileIface;
    }

    public boolean getMobileDataAvaiable() {
        return this.mIsMobileDataAvailable;
    }

    public void handleTelephonyDataConnectionChanged(String state, String iface, int subId) {
        MpLinkCommonUtils.logI(TAG, "ACTION_ANY_DATA_CONNECTION_STATE_CHANGED subId:" + subId + ",state:" + state);
        if (subId != this.mDefaultDataSubId) {
            return;
        }
        if (AwareJobSchedulerConstants.SERVICES_STATUS_CONNECTED.equals(state)) {
            this.mMobileConnectState = true;
            this.mMobileIface = iface;
            sendMessage(HwMpLinkServiceImpl.MPLINK_MSG_MOBILE_DATA_CONNECTED);
        } else if ("DISCONNECTED".equals(state)) {
            this.mMobileConnectState = false;
            this.mMobileIface = "";
            this.mIsMobileDataAvailable = false;
            sendMessage(HwMpLinkServiceImpl.MPLINK_MSG_MOBILE_DATA_DISCONNECTED);
        }
    }

    public void handleTelephonyServiceStateChanged(ServiceState serviceState, int subId) {
        MpLinkCommonUtils.logI(TAG, "ACTION_SERVICE_STATE_CHANGED subId:" + subId);
        if (subId != -1 && subId == this.mDefaultDataSubId && serviceState != null) {
            int newServiceState = serviceState.getDataRegState();
            if (this.mCurrentServceState != newServiceState) {
                this.mCurrentServceState = newServiceState;
                handleRadioServiceStateChange(newServiceState);
            }
            int newDataTechType = serviceState.getDataNetworkType();
            if (this.mCurrentDataTechType != newDataTechType) {
                this.mCurrentDataTechType = newDataTechType;
                handleDataTechTypeChange(newDataTechType);
            }
            boolean newRoamingState = serviceState.getDataRoaming();
            if (this.mCurrentDataRoamingState != newRoamingState) {
                this.mCurrentDataRoamingState = newRoamingState;
                handleDataRoamingStateChange(newRoamingState);
            }
        }
    }

    public void handleDataTechTypeChange(int dataTech) {
        boolean newDataTechSuitable;
        MpLinkCommonUtils.logI(TAG, "handlerDataTechTypeChange dataTech :" + dataTech);
        if (dataTech == 3 || dataTech == 8 || dataTech == 9 || dataTech == 10 || dataTech == 15 || dataTech == 13 || dataTech == 19) {
            newDataTechSuitable = true;
        } else {
            newDataTechSuitable = false;
        }
        if (this.mIsDataTechSuitable != newDataTechSuitable) {
            this.mIsDataTechSuitable = newDataTechSuitable;
            if (newDataTechSuitable) {
                sendMessage(201);
            } else {
                sendMessage(202);
            }
        }
    }

    public void handleRadioServiceStateChange(int State) {
        MpLinkCommonUtils.logI(TAG, "handlerDataTechTypeChange State :" + State);
        if (State != 0) {
            sendMessage(206);
        } else {
            sendMessage(205);
        }
    }

    public void handleDataRoamingStateChange(boolean roaming) {
        MpLinkCommonUtils.logI(TAG, "handlerDataTechTypeChange roaming :" + roaming);
        if (roaming) {
            sendMessage(204);
        } else {
            sendMessage(203);
        }
    }

    public void handleMobileDataSwitchChange(boolean enable) {
        MpLinkCommonUtils.logD(TAG, "handleMobileDataSwitchChange:" + enable);
        int iFlag = 0;
        if (enable) {
            iFlag = 1;
        }
        if (this.mMobileDataSwitchState != iFlag) {
            MpLinkCommonUtils.logI(TAG, "handleMobileDataSwitchChange sendmsg:" + enable);
            this.mMobileDataSwitchState = iFlag;
            if (enable) {
                sendMessage(HwMpLinkServiceImpl.MPLINK_MSG_MOBILE_DATA_SWITCH_OPEN);
            } else {
                sendMessage(220);
            }
        }
    }

    public void updateWifiLcfInfo(int frep, int bandWidth) {
        this.mCurrentWifiFreq = frep;
        this.mCurrentWifiBandWidth = bandWidth;
        MpLinkCommonUtils.logD(TAG, "updateWifiLcfInfo frep:" + this.mCurrentWifiFreq + ",bandWidth:" + this.mCurrentWifiBandWidth);
    }
}
