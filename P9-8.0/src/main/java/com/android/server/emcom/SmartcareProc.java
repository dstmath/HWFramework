package com.android.server.emcom;

import android.content.Context;
import android.emcom.EmailInfo;
import android.emcom.SmartcareInfos;
import android.emcom.SmartcareInfos.BrowserInfo;
import android.emcom.SmartcareInfos.FwkNetworkInfo;
import android.emcom.SmartcareInfos.HttpInfo;
import android.emcom.SmartcareInfos.SmartcareBaseInfo;
import android.emcom.SmartcareInfos.TcpStatusInfo;
import android.emcom.SmartcareInfos.WechatInfo;
import android.emcom.VideoInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import android.util.Pools.Pool;
import android.util.Pools.SynchronizedPool;
import com.android.server.emcom.SmartcareConfigSerializer.AppData;
import com.android.server.emcom.daemon.DaemonCommand;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.net.wifi.WifiManagerCommonEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class SmartcareProc {
    public static final String FIELED_SEPRATOR = ",";
    private static final int INVALID_PAGE = -1;
    private static final int INVALID_SIGNAL_STRENGTH = 99;
    private static final boolean IS_CDMA_GSM = SystemProperties.get("ro.config.dsds_mode", "").equals("cdma_gsm");
    private static final int LENGTH_OF_MCC = 3;
    private static final int LENGTH_OF_MNC = 2;
    private static final String LOG_TAG = "SmartcareProc";
    private static final int MAX_SMARTCARE_INFOS_POOL = 15;
    private static final int MAX_UPLOADED_TASK_SIZE = 3;
    public static final int MESSAGE_SD_SAMPLE_WIN_CLOSE = 1004;
    public static final int MSG_ADD_INFO = 1001;
    public static final int MSG_UPDATE_PAGE = 1003;
    public static final int MSG_UPLOAD = 1002;
    public static final int WIFI_SIGNAL_ZERO_LEVEL = 4;
    private static final int WLAN_BSSID_DISPLAY_LENGTH = 12;
    private static Handler mInfosHandler;
    private static SmartcareProc sInstance = null;
    private BrowserInfo mBrowserInfo = null;
    private CdmaCellLocationInfo[] mCdmaCellLocationInfo = null;
    private Context mContext = null;
    private Criteria mCriteria = new Criteria();
    private EmailInfo mEmailInfo = null;
    private FwkNetworkInfo mFwkNetworkInfo = null;
    private GsmCellLocationInfo[] mGsmCellLocationInfo = null;
    private HttpInfo mHttpInfo = null;
    private int[] mListenCallState = new int[]{0, 0, 0};
    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d(SmartcareProc.LOG_TAG, "onLocationChanged");
            SmartcareProc.this.mLocationManager.removeUpdates(this);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
    private LocationManager mLocationManager;
    long mPageId = -1;
    final HashMap<Long, FwkNetworkInfo> mPageIdToFwkInfoMap = new HashMap();
    public int mPhoneNum = 3;
    private PhoneStateListener[] mPhoneStateListeners = null;
    private long[] mPsRegisterSuccessTime = null;
    private RegInfo[] mRegInfo = null;
    private ServiceState[] mServiceState = null;
    private SignalStrengthInfo[] mSignalStrengthInfo = null;
    private SmartcareConfigSerializer mSmartcareConfigSerializer = null;
    final Pool<SmartcareInfos> mSmartcareInfosPool = new SynchronizedPool(15);
    final HashMap<String, UploadTask> mTaskMap = new HashMap();
    private TcpStatusInfo mTcpStatusInfo = null;
    private TelephonyManager mTelephonyManager = null;
    private VideoInfo mVideoInfo = null;
    private WechatInfo mWechatInfo = null;

    static abstract class UploadTask {
        public String mAppType;
        protected ArrayList<SmartcareInfos> mSmartcareInfosList = new ArrayList();

        public UploadTask(String appType) {
            this.mAppType = appType;
        }

        public String getType() {
            return this.mAppType;
        }

        void upload() {
        }

        protected boolean hasNormalData(SmartcareInfos is) {
            return false;
        }

        protected void preAdd(SmartcareInfos is) {
        }

        protected void notifySampleWinStat(boolean sampleWinOpen) {
            SmartcareProc.getInstance().notifySampleWinStat(this.mAppType, sampleWinOpen);
        }

        protected void updateLocation() {
            SmartcareProc.getInstance().requestLocationUpdate();
        }

        protected void addToTask(SmartcareInfos is) {
        }

        public boolean needToUpload() {
            return this.mSmartcareInfosList.isEmpty() ^ 1;
        }
    }

    static final class BrowserUploadTask extends UploadTask {
        public BrowserUploadTask(String type) {
            super(type);
        }

        protected boolean hasNormalData(SmartcareInfos is) {
            if (is != null && is.browserInfo != null) {
                return is.browserInfo.result;
            }
            Log.w(SmartcareProc.LOG_TAG, "check exception");
            return false;
        }

        protected void preAdd(SmartcareInfos is) {
            if (!hasNormalData(is)) {
                updateLocation();
            }
            SmartcareProc.getInstance().execCloseSampleWin();
        }

        protected void addToTask(SmartcareInfos is) {
            SmartcareProc proc = SmartcareProc.getInstance();
            HashMap<Long, FwkNetworkInfo> idToFwkInfoMap = proc.getIdToFwkInfoMap();
            FwkNetworkInfo fwkNetworkInfo = (FwkNetworkInfo) idToFwkInfoMap.get(Long.valueOf(is.browserInfo.pageId));
            if (fwkNetworkInfo == null) {
                Log.w(SmartcareProc.LOG_TAG, "Page id isn't existed!");
                return;
            }
            idToFwkInfoMap.remove(Long.valueOf(is.browserInfo.pageId));
            if (is.fwkNetworkInfo == null) {
                is.fwkNetworkInfo = fwkNetworkInfo;
            } else {
                is.fwkNetworkInfo.copyFrom(fwkNetworkInfo);
            }
            if (this.mSmartcareInfosList.isEmpty()) {
                if (SmartcareConstants.DEBUG_MODE) {
                    Log.d(SmartcareProc.LOG_TAG, "BrowserUploadTask adding first item: " + is);
                }
                this.mSmartcareInfosList.add(is);
            } else {
                proc.recycleSmartcareInfos(is);
            }
        }

        void upload() {
            int i;
            SmartcareInfos is = (SmartcareInfos) this.mSmartcareInfosList.remove(0);
            if (SmartcareConstants.DEBUG_MODE) {
                Log.d(SmartcareProc.LOG_TAG, "BrowserUploadTask uploading: " + is);
            }
            if (is.browserInfo.result) {
                i = SmartcareConstants.BROWSER_EXPERIENCE_NORMAL_LOG;
            } else {
                i = SmartcareConstants.BROWSER_EXPERIENCE_ABNORMAL_LOG;
            }
            triggerUploadBrowserExpericeInfo(i, is.browserInfo.result, is);
            SmartcareProc proc = SmartcareProc.getInstance();
            proc.recycleSmartcareInfos(is);
            proc.getIdToFwkInfoMap().clear();
        }

        public void addHttpInfo(EventStream eStream, short imonitorKeyBase, HttpInfo httpInfo) {
            if (eStream == null) {
                Log.w(SmartcareProc.LOG_TAG, "addHttpInfo,eStream is null");
                return;
            }
            short imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            EventStream param = eStream.setParam((short) imonitorKeyBase, httpInfo.host);
            imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
            param = param.setParam((short) imonitorKeyBase2, httpInfo.startDate);
            param.setParam((short) imonitorKeyBase, httpInfo.startTime).setParam((short) (imonitorKeyBase + 1), httpInfo.endTime);
        }

        public void addBrowserInfo(EventStream eStream, short imonitorKeyBase, BrowserInfo browserInfo, HttpInfo httpInfo) {
            if (eStream == null) {
                Log.w(SmartcareProc.LOG_TAG, "addBrowserInfo, eStream is null");
                return;
            }
            short imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            EventStream param = eStream.setParam((short) imonitorKeyBase, httpInfo.appName);
            imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
            param.setParam((short) imonitorKeyBase2, browserInfo.pageLatency).setParam((short) imonitorKeyBase, browserInfo.result ? 1 : 0).setParam((short) (imonitorKeyBase + 1), browserInfo.rspCode);
        }

        public void addTcpStatusInfo(EventStream eStream, short imonitorKeyBase, TcpStatusInfo tcpStatusInfo) {
            if (eStream == null) {
                Log.w(SmartcareProc.LOG_TAG, "addTcpStatusInfo, eStream is null");
                return;
            }
            short imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            EventStream param = eStream.setParam((short) imonitorKeyBase, tcpStatusInfo.dnsDelay);
            imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
            param = param.setParam((short) imonitorKeyBase2, tcpStatusInfo.synRtt);
            imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            param = param.setParam((short) imonitorKeyBase, tcpStatusInfo.synRtrans);
            imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
            param = param.setParam((short) imonitorKeyBase2, tcpStatusInfo.tcpDLWinZeroCount);
            imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            param = param.setParam((short) imonitorKeyBase, tcpStatusInfo.tcpULWinZeroCount);
            imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
            param = param.setParam((short) imonitorKeyBase2, tcpStatusInfo.tcpUlPackages);
            imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            param = param.setParam((short) imonitorKeyBase, tcpStatusInfo.tcpDlPackages);
            imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
            param = param.setParam((short) imonitorKeyBase2, tcpStatusInfo.tcpUlTimeoutRetrans);
            imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            param = param.setParam((short) imonitorKeyBase, tcpStatusInfo.tcpDlThreeDupAcks);
            param.setParam((short) imonitorKeyBase2, tcpStatusInfo.tcpUlFastRetrans).setParam((short) (imonitorKeyBase2 + 1), tcpStatusInfo.tcpDlDisorderPkts);
        }

        public void triggerUploadBrowserExpericeInfo(int eventId, boolean success, SmartcareInfos is) {
            EventStream eStream = IMonitor.openEventStream(eventId);
            addHttpInfo(eStream, (short) 0, is.httpInfo);
            SmartcareProc proc = SmartcareProc.getInstance();
            if (success) {
                addBrowserInfo(eStream, (short) 12, is.browserInfo, is.httpInfo);
                proc.addNetworkInfo(eStream, (short) 4, (short) 16, is.fwkNetworkInfo);
            } else {
                addTcpStatusInfo(eStream, (short) 13, is.tcpStatusInfo);
                addBrowserInfo(eStream, (short) 24, is.browserInfo, is.httpInfo);
                proc.addNetworkInfo(eStream, (short) 4, (short) 28, is.fwkNetworkInfo);
            }
            IMonitor.sendEvent(eStream);
            IMonitor.closeEventStream(eStream);
        }
    }

    public static class CdmaCellLocationInfo {
        private int mBaseStationId = -1;
        private int mBaseStationLatitude = -1;
        private int mBaseStationLongitude = -1;
        private int mNetworkId = -1;
        private int mSystemId = -1;

        public void update(CdmaCellLocation cellLocation) {
            if (-1 != cellLocation.getBaseStationId()) {
                this.mBaseStationId = cellLocation.getBaseStationId();
            }
            if (-1 != cellLocation.getSystemId()) {
                this.mSystemId = cellLocation.getSystemId();
            }
            if (-1 != cellLocation.getNetworkId()) {
                this.mNetworkId = cellLocation.getNetworkId();
            }
            this.mBaseStationLatitude = cellLocation.getBaseStationLatitude();
            this.mBaseStationLongitude = cellLocation.getBaseStationLongitude();
            if (SmartcareConstants.DEBUG_MODE) {
                Log.d(SmartcareProc.LOG_TAG, toString());
            }
        }

        public int getBaseStationId() {
            return this.mBaseStationId;
        }

        public int getSystemId() {
            return this.mSystemId;
        }

        public int getNetworkId() {
            return this.mNetworkId;
        }

        public int getBaseStationLatitude() {
            return this.mBaseStationLatitude;
        }

        public int getBaseStationLongitude() {
            return this.mBaseStationLongitude;
        }

        public String toString() {
            return "CdmaCellLocationInfo:mBaseStationId: " + this.mBaseStationId + "mSystemId: " + this.mSystemId + "mNetworkId: " + this.mNetworkId;
        }
    }

    static final class EmailUploadTask extends UploadTask {
        static final int EMAIL_TYPE_NUM = 3;

        public EmailUploadTask(String type) {
            super(type);
        }

        protected void preAdd(SmartcareInfos is) {
            if (is == null || is.emailInfo == null) {
                Log.w(SmartcareProc.LOG_TAG, "check exception");
                return;
            }
            if (is.emailInfo.successFlag == (byte) 0) {
                updateLocation();
            }
        }

        protected void addToTask(SmartcareInfos is) {
            SmartcareProc proc = SmartcareProc.getInstance();
            proc.ensureFwkNetworkInfo(is);
            if (this.mSmartcareInfosList.isEmpty()) {
                this.mSmartcareInfosList.add(is);
            } else {
                boolean foundMatchedType = false;
                int smartcareInfosListSize = this.mSmartcareInfosList.size();
                for (int i = 0; i < smartcareInfosListSize; i++) {
                    if (((SmartcareInfos) this.mSmartcareInfosList.get(i)).emailInfo.type == is.emailInfo.type) {
                        foundMatchedType = true;
                        proc.recycleSmartcareInfos(is);
                        break;
                    }
                }
                if (!foundMatchedType) {
                    this.mSmartcareInfosList.add(is);
                }
            }
            if (this.mSmartcareInfosList.size() == 3) {
                notifySampleWinStat(false);
            }
        }

        void upload() {
            for (int i = this.mSmartcareInfosList.size() - 1; i >= 0; i--) {
                int i2;
                SmartcareInfos is = (SmartcareInfos) this.mSmartcareInfosList.remove(i);
                if (is.emailInfo.successFlag != (byte) 0) {
                    i2 = SmartcareConstants.EMAIL_NORMAL_LOG;
                } else {
                    i2 = SmartcareConstants.EMAIL_ABNORMAL_LOG;
                }
                triggerUploadEmailInfo(i2, is.emailInfo.successFlag != (byte) 0, is);
                SmartcareProc.getInstance().recycleSmartcareInfos(is);
            }
        }

        public void addEmailInfo(EventStream eStream, short imonitorKeyBase, boolean success, EmailInfo emailInfo) {
            if (eStream == null) {
                Log.w(SmartcareProc.LOG_TAG, "addEmailInfo, eStream is null");
                return;
            }
            short emailCommonInfBase2;
            short imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            EventStream param = eStream.setParam((short) imonitorKeyBase, emailInfo.hostName);
            imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
            param = param.setParam((short) imonitorKeyBase2, emailInfo.emailStartDate);
            param.setParam((short) imonitorKeyBase, emailInfo.emailStartTime).setParam((short) (imonitorKeyBase + 1), emailInfo.emailEndTime);
            if (success) {
                emailCommonInfBase2 = (short) 12;
            } else {
                emailCommonInfBase2 = (short) 13;
            }
            short emailCommonInfBase22 = (short) (emailCommonInfBase2 + 1);
            param = eStream.setParam((short) emailCommonInfBase2, emailInfo.appName);
            emailCommonInfBase2 = (short) (emailCommonInfBase22 + 1);
            param = param.setParam((short) emailCommonInfBase22, emailInfo.type);
            emailCommonInfBase22 = (short) (emailCommonInfBase2 + 1);
            param = param.setParam((short) emailCommonInfBase2, emailInfo.latency);
            emailCommonInfBase2 = (short) (emailCommonInfBase22 + 1);
            param = param.setParam((short) emailCommonInfBase22, emailInfo.successFlag);
            emailCommonInfBase22 = (short) (emailCommonInfBase2 + 1);
            param = param.setParam((short) emailCommonInfBase2, emailInfo.abnormalDroppingFlag);
            param.setParam((short) emailCommonInfBase22, emailInfo.protocolType).setParam((short) (emailCommonInfBase22 + 1), emailInfo.emailThrouput);
        }

        public void triggerUploadEmailInfo(int eventId, boolean success, SmartcareInfos is) {
            EventStream eStream = IMonitor.openEventStream(eventId);
            addEmailInfo(eStream, (short) 0, success, is.emailInfo);
            SmartcareProc proc = SmartcareProc.getInstance();
            if (success) {
                proc.addNetworkInfo(eStream, (short) 4, (short) 19, is.fwkNetworkInfo);
            } else {
                proc.addNetworkInfo(eStream, (short) 4, (short) 20, is.fwkNetworkInfo);
            }
            IMonitor.sendEvent(eStream);
            IMonitor.closeEventStream(eStream);
        }
    }

    public static class GsmCellLocationInfo {
        private int mCid = -1;
        private int mLac = -1;

        public void update(GsmCellLocation cellLocation) {
            if (-1 != cellLocation.getLac()) {
                this.mLac = cellLocation.getLac();
            }
            if (-1 != cellLocation.getCid()) {
                this.mCid = cellLocation.getCid();
            }
        }

        public int getLac() {
            return this.mLac;
        }

        public int getCid() {
            return this.mCid;
        }

        public String toString() {
            return "GsmCellLocationInfo:mLac: " + this.mLac + "mCid: " + this.mCid;
        }
    }

    final class InfosHandler extends Handler {
        public InfosHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (SmartcareConstants.DEBUG_MODE) {
                Log.d(SmartcareProc.LOG_TAG, "InfosHandler: msg.what = " + msg.what);
            }
            switch (msg.what) {
                case 1001:
                    SmartcareProc.this.handleAddToTask(msg.obj);
                    return;
                case 1002:
                    SmartcareProc.this.handleTriggerAllUpload();
                    return;
                case 1003:
                    SmartcareProc.this.handleUpdatePageId((long) msg.arg1);
                    return;
                case 1004:
                    Log.d(SmartcareProc.LOG_TAG, "InfosHandler MESSAGE_SD_SAMPLE_WIN_CLOSE");
                    return;
                default:
                    return;
            }
        }
    }

    public static class LocalWifiInfo {
        private String mConnected_BSSID = SmartcareConstants.UNAVAIBLE_VALUE;
        private String mConnected_SSID = SmartcareConstants.UNAVAIBLE_VALUE;
        private String mScan_BSSID_1 = SmartcareConstants.UNAVAIBLE_VALUE;
        private String mScan_BSSID_2 = SmartcareConstants.UNAVAIBLE_VALUE;
        private String mScan_BSSID_3 = SmartcareConstants.UNAVAIBLE_VALUE;
        private boolean mbIsWiFiConnected = false;
        private boolean mbIsWiFiOpen = false;

        public String getConnected_BSSID() {
            return this.mConnected_BSSID;
        }

        public boolean getIsWifiOpen() {
            return this.mbIsWiFiOpen;
        }

        public boolean getIsWifiConnected() {
            return this.mbIsWiFiConnected;
        }

        public String getScan_BSSID_1() {
            return this.mScan_BSSID_1;
        }

        public String getScan_BSSID_2() {
            return this.mScan_BSSID_2;
        }

        public String getScan_BSSID_3() {
            return this.mScan_BSSID_3;
        }

        public String getConnected_SSID() {
            return this.mConnected_SSID;
        }

        public String toString() {
            return "WifiInfo:mbIsWiFiOpen: " + this.mbIsWiFiOpen + "mbIsWiFiConnected: " + this.mbIsWiFiConnected + "mConnected_BSSID: " + this.mConnected_BSSID + "mScan_BSSID_1: " + this.mScan_BSSID_1 + "mScan_BSSID_2: " + this.mScan_BSSID_2 + "mScan_BSSID_3: " + this.mScan_BSSID_3;
        }
    }

    enum NetworkType {
        UNKNOWN,
        TWO_G,
        THREE_G,
        FOUR_G,
        WIFI
    }

    public static class RegInfo {
        private int mDataNetworkType = -1;
        private String mMcc = null;
        private String mMnc = null;
        private String mOperatorNumeric = null;
        private int mRilDataRadioTechnology = -1;
        private int mRilVoiceRadioTechnology = -1;
        private int mVoiceNetworkType = -1;

        public String getRegOperatorNumeric() {
            return this.mOperatorNumeric;
        }

        public String getMcc() {
            return this.mMcc;
        }

        public String getMnc() {
            return this.mMnc;
        }

        public int getDataNetworkType() {
            return this.mDataNetworkType;
        }

        public int getVoiceNetworkType() {
            return this.mVoiceNetworkType;
        }

        public boolean isGsm() {
            if (ServiceState.isGsm(this.mRilVoiceRadioTechnology) || ServiceState.isGsm(this.mRilDataRadioTechnology)) {
                return true;
            }
            return false;
        }

        public boolean isCdma() {
            if (ServiceState.isCdma(this.mRilVoiceRadioTechnology) || ServiceState.isCdma(this.mRilDataRadioTechnology)) {
                return true;
            }
            return false;
        }

        public void update(ServiceState ss) {
            if (SmartcareConstants.DEBUG_MODE) {
                Log.d(SmartcareProc.LOG_TAG, ss.toString());
            }
            String operatorNum = ss.getOperatorNumeric();
            if (operatorNum == null || operatorNum.length() <= 3) {
                Log.w(SmartcareProc.LOG_TAG, "onServiceStateChanged, operatorNumeric: " + this.mOperatorNumeric);
                return;
            }
            this.mOperatorNumeric = operatorNum;
            this.mMcc = this.mOperatorNumeric.substring(0, 3);
            this.mMnc = this.mOperatorNumeric.substring(3);
            if (ss.getDataRegState() == 0) {
                this.mDataNetworkType = ss.getDataNetworkType();
                this.mRilDataRadioTechnology = ss.getRilDataRadioTechnology();
                if ((8 == this.mDataNetworkType || 9 == this.mDataNetworkType || 10 == this.mDataNetworkType || 15 == this.mDataNetworkType) && TextUtils.equals(this.mMcc, WifiProCommonUtils.COUNTRY_CODE_CN) && (TextUtils.equals(this.mMnc, "00") || TextUtils.equals(this.mMnc, "02") || TextUtils.equals(this.mMnc, "07") || TextUtils.equals(this.mMnc, "08"))) {
                    this.mDataNetworkType = 17;
                }
            }
            if (ss.getVoiceRegState() == 0) {
                this.mVoiceNetworkType = ss.getVoiceNetworkType();
                this.mRilVoiceRadioTechnology = ss.getRilVoiceRadioTechnology();
            }
        }

        public String toString() {
            return "RegInfo:mOperatorNumeric: " + this.mOperatorNumeric + "mMcc: " + this.mMcc + "mMnc: " + this.mMnc + "mDataNetworkType: " + this.mDataNetworkType + "mVoiceNetworkType: " + this.mVoiceNetworkType;
        }
    }

    protected static class SignalStrengthInfo {
        private int mCdmaDbm = -1;
        private int mCdmaEcio = -1;
        private int mEvdoDbm = -1;
        private int mEvdoEcio = -1;
        private int mGsmSignalStrength = -1;
        private int mLteRsrp = -1;
        private int mLteRsrq = -1;
        private int mLteRssnr = -1;
        private int mWcdmaEcio = -1;
        private int mWcdmaRscp = -1;

        protected SignalStrengthInfo() {
        }

        public int getGsmSignalStrength() {
            return this.mGsmSignalStrength;
        }

        public int getWcdmaRscp() {
            return this.mWcdmaRscp;
        }

        public int getWcdmaEcio() {
            return this.mWcdmaEcio;
        }

        public int getLteRsrp() {
            return this.mLteRsrp;
        }

        public int getLteRsrq() {
            return this.mLteRsrq;
        }

        public int getLteRssnr() {
            return this.mLteRssnr;
        }

        public int getCdmaDbm() {
            return this.mCdmaDbm;
        }

        public int getCdmaEcio() {
            return this.mCdmaEcio;
        }

        public int getEvdoDbm() {
            return this.mEvdoDbm;
        }

        public int getEvdoEcio() {
            return this.mEvdoEcio;
        }

        public void update(SignalStrength ss) {
            this.mGsmSignalStrength = ss.getGsmSignalStrength();
            if (99 == this.mGsmSignalStrength || this.mGsmSignalStrength == 0) {
                this.mGsmSignalStrength = -1;
            }
            this.mWcdmaRscp = ss.getWcdmaRscp();
            if (99 == this.mWcdmaRscp || this.mWcdmaRscp == 0) {
                this.mWcdmaRscp = -1;
            }
            this.mWcdmaEcio = ss.getWcdmaEcio();
            if (99 == this.mWcdmaEcio || this.mWcdmaEcio == 0) {
                this.mWcdmaEcio = -1;
            }
            this.mCdmaDbm = ss.getCdmaDbm();
            if (99 == this.mCdmaDbm || this.mCdmaDbm == 0) {
                this.mCdmaDbm = -1;
            }
            this.mCdmaEcio = ss.getCdmaEcio();
            if (99 == this.mCdmaEcio || this.mCdmaEcio == 0) {
                this.mCdmaEcio = -1;
            }
            this.mEvdoDbm = ss.getEvdoDbm();
            if (99 == this.mEvdoDbm || this.mEvdoDbm == 0) {
                this.mEvdoDbm = -1;
            }
            this.mEvdoEcio = ss.getEvdoEcio();
            if (99 == this.mEvdoEcio || this.mEvdoEcio == 0) {
                this.mEvdoEcio = -1;
            }
            this.mLteRsrp = ss.getLteRsrp();
            if (99 == this.mLteRsrp || this.mLteRsrp == 0) {
                this.mLteRsrp = -1;
            }
            SmartcareProc.getInstance().getOldFwkNetworkInfo().rsrp = (byte) (this.mLteRsrp & 255);
            this.mLteRsrq = ss.getLteRsrq();
            if (99 == this.mLteRsrq || this.mLteRsrq == 0) {
                this.mLteRsrq = -1;
            }
            this.mLteRssnr = ss.getLteRssnr();
            if (99 == this.mLteRssnr || this.mLteRssnr == 0) {
                this.mLteRssnr = -1;
            }
            Log.d(SmartcareProc.LOG_TAG, "SignalStrength update result, ss: " + this);
        }

        public String toString() {
            return "SignalStrengthInfo:" + hashCode() + " mGsmSignalStrength: " + this.mGsmSignalStrength + " mWcdmaRscp: " + this.mWcdmaRscp + " mWcdmaEcio: " + this.mWcdmaEcio + " mLteRsrp: " + this.mLteRsrp + " mLteRsrq: " + this.mLteRsrq + " mCdmaDbm: " + this.mCdmaDbm + " mCdmaEcio: " + this.mCdmaEcio + " mEvdoDbm: " + this.mEvdoDbm + "mEvdoEcio: " + this.mEvdoEcio + "mLteRssnr: " + this.mLteRssnr;
        }
    }

    static final class VideoUploadTask extends UploadTask {
        public VideoUploadTask(String type) {
            super(type);
        }

        protected boolean hasNormalData(SmartcareInfos is) {
            if (is == null || is.videoInfo == null) {
                Log.w(SmartcareProc.LOG_TAG, "check exception");
                return false;
            }
            boolean bSuccess = true;
            SmartcareProc proc = SmartcareProc.getInstance();
            if (is.videoInfo.fullDelay > proc.getConfigedVideoIntBufferDelayThres() || is.videoInfo.srDelay > proc.getConfigedVideoSRDelayThres() || is.videoInfo.totalLen > proc.getConfigedStallingDurationThres() || is.videoInfo.times > proc.getConfigedStallingTimesThres() || is.videoInfo.videoTerminateFlag == (byte) 0) {
                bSuccess = false;
            }
            return bSuccess;
        }

        protected void preAdd(SmartcareInfos is) {
            if (is == null || is.videoInfo == null) {
                Log.w(SmartcareProc.LOG_TAG, "check exception");
                return;
            }
            is.videoInfo.result = hasNormalData(is);
            if (!is.videoInfo.result) {
                updateLocation();
            }
            notifySampleWinStat(false);
        }

        protected void addToTask(SmartcareInfos is) {
            SmartcareProc proc = SmartcareProc.getInstance();
            proc.ensureFwkNetworkInfo(is);
            if (this.mSmartcareInfosList.isEmpty()) {
                this.mSmartcareInfosList.add(is);
            } else {
                proc.recycleSmartcareInfos(is);
            }
        }

        void upload() {
            for (int i = this.mSmartcareInfosList.size() - 1; i >= 0; i--) {
                int i2;
                SmartcareInfos is = (SmartcareInfos) this.mSmartcareInfosList.remove(i);
                if (is.videoInfo.result) {
                    i2 = SmartcareConstants.VIDEO_NORMAL_LOG;
                } else {
                    i2 = SmartcareConstants.VIDEO_ABNORMAL_LOG;
                }
                triggerUploadVideoInfo(i2, is.videoInfo.result, is);
                SmartcareProc.getInstance().recycleSmartcareInfos(is);
            }
        }

        public void addVideoInfo(EventStream eStream, short imonitorKeyBase, boolean success, VideoInfo videoInfo) {
            if (eStream == null) {
                Log.w(SmartcareProc.LOG_TAG, "addVideoInfo, eStream is null");
                return;
            }
            short videoInfoBase2;
            short imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            EventStream param = eStream.setParam((short) imonitorKeyBase, videoInfo.hostName);
            imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
            param = param.setParam((short) imonitorKeyBase2, videoInfo.videoStartDate);
            param.setParam((short) imonitorKeyBase, videoInfo.videoStartTime).setParam((short) (imonitorKeyBase + 1), videoInfo.videoEndTime);
            if (success) {
                videoInfoBase2 = (short) 12;
            } else {
                videoInfoBase2 = (short) 13;
            }
            short videoInfoBase22 = (short) (videoInfoBase2 + 1);
            param = eStream.setParam((short) videoInfoBase2, videoInfo.appName);
            videoInfoBase2 = (short) (videoInfoBase22 + 1);
            param = param.setParam((short) videoInfoBase22, videoInfo.srDelay);
            videoInfoBase22 = (short) (videoInfoBase2 + 1);
            param = param.setParam((short) videoInfoBase2, videoInfo.fullDelay);
            videoInfoBase2 = (short) (videoInfoBase22 + 1);
            param = param.setParam((short) videoInfoBase22, videoInfo.times);
            videoInfoBase22 = (short) (videoInfoBase2 + 1);
            param = param.setParam((short) videoInfoBase2, videoInfo.totalLen);
            videoInfoBase2 = (short) (videoInfoBase22 + 1);
            param = param.setParam((short) videoInfoBase22, videoInfo.streamDur);
            videoInfoBase22 = (short) (videoInfoBase2 + 1);
            param = param.setParam((short) videoInfoBase2, videoInfo.videoDataRate);
            param.setParam((short) videoInfoBase22, videoInfo.videoTerminateFlag).setParam((short) (videoInfoBase22 + 1), videoInfo.uVMos);
            if (SmartcareConstants.DEBUG_MODE) {
                Log.d(SmartcareProc.LOG_TAG, "  videoInfo = " + videoInfo.toString());
            }
        }

        public void triggerUploadVideoInfo(int eventId, boolean success, SmartcareInfos is) {
            EventStream eStream = IMonitor.openEventStream(eventId);
            addVideoInfo(eStream, (short) 0, success, is.videoInfo);
            addNetworkInfo(eStream, (short) 4, is.fwkNetworkInfo);
            IMonitor.sendEvent(eStream);
            IMonitor.closeEventStream(eStream);
        }

        private void addNetworkInfo(EventStream eStream, short imonitorKeyBase, FwkNetworkInfo fwkNetworkInfo) {
            if (eStream == null) {
                Log.w(SmartcareProc.LOG_TAG, "addNetworkInfo, eStream is null");
                return;
            }
            short imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            eStream.setParam((short) imonitorKeyBase, fwkNetworkInfo.mcc);
            imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
            eStream.setParam((short) imonitorKeyBase2, fwkNetworkInfo.mnc);
            imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            eStream.setParam((short) imonitorKeyBase, (byte) (fwkNetworkInfo.rat & 255));
            imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
            eStream.setParam((short) imonitorKeyBase2, fwkNetworkInfo.timeAndCid);
            StringBuilder sb = new StringBuilder();
            sb.append(fwkNetworkInfo.wlanSignalStrength);
            sb.append("\\");
            sb.append(fwkNetworkInfo.wlanBssid);
            sb.append(fwkNetworkInfo.wlanSsid);
            imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            eStream.setParam((short) imonitorKeyBase, sb.toString());
            imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
            EventStream param = eStream.setParam((short) imonitorKeyBase2, fwkNetworkInfo.rsrp);
            imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            param = param.setParam((short) imonitorKeyBase, fwkNetworkInfo.rsrq);
            imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
            param.setParam((short) imonitorKeyBase2, fwkNetworkInfo.sinr);
        }
    }

    static final class WechatUploadTask extends UploadTask {
        static final int WECHAT_TYPE_NUM = 3;

        public WechatUploadTask(String type) {
            super(type);
        }

        protected boolean hasNormalData(SmartcareInfos is) {
            boolean z = false;
            if (is == null || is.wechatInfo == null) {
                Log.w(SmartcareProc.LOG_TAG, "check exception");
                return false;
            }
            if (is.wechatInfo.successFlag != (byte) 0) {
                z = true;
            }
            return z;
        }

        protected void preAdd(SmartcareInfos is) {
            if (!hasNormalData(is)) {
                updateLocation();
            }
        }

        protected void addToTask(SmartcareInfos is) {
            SmartcareProc proc = SmartcareProc.getInstance();
            proc.ensureFwkNetworkInfo(is);
            if (this.mSmartcareInfosList.isEmpty()) {
                this.mSmartcareInfosList.add(is);
            } else {
                boolean foundMatchedType = false;
                int smartcareInfosListSize = this.mSmartcareInfosList.size();
                for (int i = 0; i < smartcareInfosListSize; i++) {
                    if (((SmartcareInfos) this.mSmartcareInfosList.get(i)).wechatInfo.type == is.wechatInfo.type) {
                        foundMatchedType = true;
                        proc.recycleSmartcareInfos(is);
                        break;
                    }
                }
                if (!foundMatchedType) {
                    this.mSmartcareInfosList.add(is);
                }
                if (SmartcareConstants.DEBUG_MODE) {
                    Log.d(SmartcareProc.LOG_TAG, "WechatInfo: After adding new info, mSmartcareInfosList=" + this.mSmartcareInfosList + ",new added wechatInifo=" + is.wechatInfo + ",foundMatchedType: " + foundMatchedType);
                }
            }
            if (this.mSmartcareInfosList.size() == 3) {
                notifySampleWinStat(false);
            }
        }

        void upload() {
            for (int i = this.mSmartcareInfosList.size() - 1; i >= 0; i--) {
                int i2;
                SmartcareInfos is = (SmartcareInfos) this.mSmartcareInfosList.remove(i);
                if (SmartcareConstants.DEBUG_MODE) {
                    Log.d(SmartcareProc.LOG_TAG, "WechatUploadTask uploading: " + is);
                }
                if (is.wechatInfo.successFlag != (byte) 0) {
                    i2 = SmartcareConstants.WECHAT_NORMAL_LOG;
                } else {
                    i2 = SmartcareConstants.WECHAT_ABNORMAL_LOG;
                }
                triggerUploadWechatInfo(i2, is.wechatInfo.successFlag != (byte) 0, is);
                SmartcareProc.getInstance().recycleSmartcareInfos(is);
            }
        }

        public void addHttpInfo(EventStream eStream, short imonitorKeyBase, WechatInfo wechatInfo) {
            if (eStream == null) {
                Log.w(SmartcareProc.LOG_TAG, "wechatUploadTask addHttpInfo,eStream is null");
                return;
            }
            short imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
            eStream.setParam((short) imonitorKeyBase, wechatInfo.host == null ? " " : wechatInfo.host).setParam((short) imonitorKeyBase2, wechatInfo.startDate).setParam((short) imonitorKeyBase, wechatInfo.startTime).setParam((short) (imonitorKeyBase + 1), wechatInfo.endTime);
        }

        public void addWechatInfo(EventStream eStream, short imonitorKeyBase, WechatInfo wechatInfo) {
            if (eStream == null) {
                Log.w(SmartcareProc.LOG_TAG, "addWechatInfo, eStream is null");
                return;
            }
            short imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
            EventStream param = eStream.setParam((short) imonitorKeyBase, wechatInfo.latancy);
            param.setParam((short) imonitorKeyBase2, wechatInfo.successFlag).setParam((short) (imonitorKeyBase2 + 1), wechatInfo.type);
        }

        public void triggerUploadWechatInfo(int eventId, boolean success, SmartcareInfos is) {
            short wechatInfoBase;
            EventStream eStream = IMonitor.openEventStream(eventId);
            addHttpInfo(eStream, (short) 0, is.wechatInfo);
            SmartcareProc proc = SmartcareProc.getInstance();
            if (success) {
                wechatInfoBase = (short) 12;
            } else {
                wechatInfoBase = (short) 13;
            }
            addWechatInfo(eStream, wechatInfoBase, is.wechatInfo);
            if (success) {
                proc.addNetworkInfo(eStream, (short) 4, (short) 15, is.fwkNetworkInfo);
            } else {
                proc.addNetworkInfo(eStream, (short) 4, (short) 16, is.fwkNetworkInfo);
            }
            IMonitor.sendEvent(eStream);
            IMonitor.closeEventStream(eStream);
        }
    }

    public static SmartcareProc make(Context context, int phoneNum) {
        if (sInstance != null) {
            if (SmartcareConstants.DEBUG_MODE) {
                Log.d(LOG_TAG, "make, sInstance is already made,just return");
            }
            return sInstance;
        } else if (context == null) {
            Log.e(LOG_TAG, "make, Context is null, return!");
            return null;
        } else {
            sInstance = new SmartcareProc(context, phoneNum);
            return sInstance;
        }
    }

    public static synchronized SmartcareProc getInstance() {
        SmartcareProc smartcareProc;
        synchronized (SmartcareProc.class) {
            if (sInstance == null) {
                Log.e(LOG_TAG, "getInstance, sInstance is null !");
            }
            smartcareProc = sInstance;
        }
        return smartcareProc;
    }

    private SmartcareProc(Context context, int phoneNum) {
        if (SmartcareConstants.DEBUG_MODE) {
            Log.d(LOG_TAG, "new SmartcareProc");
        }
        this.mContext = context;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mPhoneNum = phoneNum;
        this.mSmartcareConfigSerializer = SmartcareConfigSerializer.getInstance();
        initSmartcareCommParam();
        startPhoneListener();
    }

    public void processCotaUpdate() {
        int result = this.mSmartcareConfigSerializer.readConfigForCotaUpdate();
        ParaManager PMInstance = ParaManager.getInstance();
        if (PMInstance == null) {
            Log.e(LOG_TAG, "processCotaUpdate error, ParaManager sInstance is null !");
        } else {
            PMInstance.responseForParaUpgrade(256, 1, result);
        }
    }

    private UploadTask peekUploadTask(String type) {
        if (this.mTaskMap.get(type) == null) {
            if (SmartcareConstants.BROWSER_TYPE.equals(type)) {
                this.mTaskMap.put(type, new BrowserUploadTask(type));
            } else if (SmartcareConstants.WEBCHAT_TYPE.equals(type)) {
                this.mTaskMap.put(type, new WechatUploadTask(type));
            } else if (SmartcareConstants.EMAIL_TYPE.equals(type)) {
                this.mTaskMap.put(type, new EmailUploadTask(type));
            } else if (SmartcareConstants.VIDEO_TYPE.equals(type)) {
                this.mTaskMap.put(type, new VideoUploadTask(type));
            }
        }
        return (UploadTask) this.mTaskMap.get(type);
    }

    public void addToTask(String pkgName, SmartcareBaseInfo info) {
        if (SmartcareConstants.DEBUG_MODE) {
            Log.d(LOG_TAG, "addToTask, pkgName=" + pkgName + ",info=" + info);
        }
        info.pkgName = pkgName;
        mInfosHandler.obtainMessage(1001, info).sendToTarget();
    }

    public void handleAddToTask(SmartcareBaseInfo info) {
        String appType = this.mSmartcareConfigSerializer.getAppType(info.pkgName);
        if (SmartcareConstants.DEBUG_MODE) {
            Log.d(LOG_TAG, "handleAddToTask, pkgName=" + info.pkgName + ",type=" + appType + ",info=" + info);
        }
        UploadTask task = peekUploadTask(appType);
        if (task != null) {
            task.preAdd(info.smarcareInfos);
            task.addToTask(info.smarcareInfos);
            return;
        }
        Log.w(LOG_TAG, "handleAddToTask, no config found for adding " + info.pkgName);
    }

    private void handleTriggerAllUpload() {
        String[] configTypes = this.mSmartcareConfigSerializer.getConfigedTypes();
        if (configTypes == null) {
            Log.e(LOG_TAG, "handleTriggerAllUpload, configTypes is null");
            return;
        }
        int i;
        UploadTask task;
        int maxReportSize = this.mSmartcareConfigSerializer.getConfigedMaxReportSize();
        int configTypesSize = configTypes.length;
        ArrayList<UploadTask> uploadedTaskList = new ArrayList();
        for (i = 0; i < configTypesSize && uploadedTaskList.size() < maxReportSize; i++) {
            task = (UploadTask) this.mTaskMap.get(configTypes[i]);
            if (task != null && task.needToUpload()) {
                uploadedTaskList.add(task);
            }
        }
        if (SmartcareConstants.DEBUG_MODE) {
            Log.d(LOG_TAG, "handleTriggerAllUpload, configTypes: " + Arrays.toString(configTypes) + ",uploadedTaskList: " + uploadedTaskList);
        }
        int uploadedTaskListSize = uploadedTaskList.size();
        for (i = 0; i < uploadedTaskListSize; i++) {
            task = (UploadTask) uploadedTaskList.get(i);
            if (task != null) {
                if (SmartcareConstants.DEBUG_MODE) {
                    Log.d(LOG_TAG, "handleTriggerAllUpload, uploading: " + task);
                }
                task.upload();
            }
        }
    }

    private void handleUpdatePageId(long pageId) {
        this.mPageId = pageId;
        FwkNetworkInfo fwkNetworkInfo = getFwkNetworkInfo();
        updateFwkNetworkInfo(fwkNetworkInfo);
        this.mPageIdToFwkInfoMap.put(Long.valueOf(pageId), fwkNetworkInfo);
        if (SmartcareConstants.DEBUG_MODE) {
            Log.d(LOG_TAG, "handleUpdatePageId, pageId: " + pageId + ",smarcareInfos: " + fwkNetworkInfo.smarcareInfos);
        }
    }

    private void triggerAllUpload() {
        mInfosHandler.obtainMessage(1002).sendToTarget();
    }

    public void execCloseSampleWin() {
        DaemonCommand.getInstance().execCloseSampleWin(mInfosHandler.obtainMessage(1004));
    }

    private void notifySampleWinStat(String appType, boolean sampleWinOpen) {
        SystemProperties.set("sys." + appType + ".smartcare.sample.open", String.valueOf(sampleWinOpen));
    }

    void handleSamplesWinStat(boolean sampleWinOpen) {
        boolean smartcareSwitchOn = true;
        if (!this.mSmartcareConfigSerializer.smartcareSwitchOn()) {
            smartcareSwitchOn = false;
        }
        HashMap<String, AppData> configedApps = this.mSmartcareConfigSerializer.getConfigedApps();
        if (configedApps == null || configedApps.size() == 0) {
            Log.d(LOG_TAG, "No configed app found");
            return;
        }
        for (AppData appData : configedApps.values()) {
            if (!SmartcareConstants.BROWSER_TYPE.equals(appData.type)) {
                boolean z;
                String str = appData.type;
                if (smartcareSwitchOn && appData.switchOn) {
                    z = sampleWinOpen;
                } else {
                    z = false;
                }
                notifySampleWinStat(str, z);
            }
        }
        if (!sampleWinOpen) {
            triggerAllUpload();
        }
    }

    public int getConfigedVideoSRDelayThres() {
        return this.mSmartcareConfigSerializer.getConfigedVideoSRDelayThres();
    }

    public int getConfigedVideoIntBufferDelayThres() {
        return this.mSmartcareConfigSerializer.getConfigedVideoIntBufferDelayThres();
    }

    public int getConfigedStallingTimesThres() {
        return this.mSmartcareConfigSerializer.getConfigedStallingTimesThres();
    }

    public int getConfigedStallingDurationThres() {
        return this.mSmartcareConfigSerializer.getConfigedStallingDurationThres();
    }

    void updatePageId(int pageId) {
        mInfosHandler.obtainMessage(1003, pageId, 0).sendToTarget();
    }

    public long getPageId() {
        return this.mPageId;
    }

    public HashMap<Long, FwkNetworkInfo> getIdToFwkInfoMap() {
        return this.mPageIdToFwkInfoMap;
    }

    SmartcareInfos getPoolSmartcareInfos() {
        SmartcareInfos is = (SmartcareInfos) this.mSmartcareInfosPool.acquire();
        if (SmartcareConstants.DEBUG_MODE) {
            Log.d(LOG_TAG, "getPoolSmartcareInfos is: " + is + ",callers: " + Debug.getCallers(2));
        }
        if (is == null) {
            return new SmartcareInfos();
        }
        return is;
    }

    void recycleSmartcareInfos(SmartcareInfos is) {
        if (SmartcareConstants.DEBUG_MODE) {
            Log.d(LOG_TAG, "recycleSmartcareInfos");
        }
        is.recycle();
        try {
            this.mSmartcareInfosPool.release(is);
        } catch (IllegalStateException e) {
            Log.w(LOG_TAG, "IllegalStateException when recycle smartcareInfos", e);
        }
    }

    private void ensureFwkNetworkInfo(SmartcareInfos is) {
        if (is.fwkNetworkInfo == null) {
            new FwkNetworkInfo().addToInfos(is);
        }
        updateFwkNetworkInfo(is.fwkNetworkInfo);
    }

    public static String formatDegree(Double d) {
        String[] array = d.toString().split("[.]");
        String D = array[0];
        String[] array1 = Double.valueOf(Double.parseDouble("0." + array[1]) * 60.0d).toString().split("[.]");
        String M = array1[0];
        return D + "°" + M + "′" + Double.valueOf((Double.parseDouble("0." + array1[1]) * 60.0d) * 10000.0d).toString().split("[.]")[0] + "″";
    }

    public void requestLocationUpdate() {
        if (SmartcareConstants.DEBUG_MODE) {
            Log.d(LOG_TAG, "requestLocationUpdate");
        }
        this.mLocationManager.requestSingleUpdate("network", this.mLocationListener, mInfosHandler.getLooper());
    }

    private void updateFwkNetworkInfo(FwkNetworkInfo fwkNetworkInfo) {
        Binder.clearCallingIdentity();
        String imsi = ((TelephonyManager) this.mContext.getSystemService("phone")).getSubscriberId();
        if (imsi != null && imsi.length() >= 5) {
            fwkNetworkInfo.mcc = Short.parseShort(imsi.substring(0, 3));
            fwkNetworkInfo.mnc = Short.parseShort(imsi.substring(3, 5));
        } else if (imsi == null) {
            Log.w(LOG_TAG, "has no card");
        } else {
            Log.w(LOG_TAG, "IMSI length incorrect.");
        }
        fwkNetworkInfo.rat = (byte) (getNetworkType(this.mContext).ordinal() & 255);
        fwkNetworkInfo.timeAndCid = getCellId();
        LocalWifiInfo info = getWifiInfo();
        fwkNetworkInfo.wlanBssid = info.getIsWifiConnected() ? info.getConnected_BSSID() : "";
        if (fwkNetworkInfo.wlanBssid == null) {
            fwkNetworkInfo.wlanBssid = "";
        }
        fwkNetworkInfo.wlanSsid = info.getIsWifiConnected() ? info.getConnected_SSID() : "";
        if (fwkNetworkInfo.wlanSsid == null) {
            fwkNetworkInfo.wlanSsid = "";
        }
        List<ScanResult> scanResults = ((WifiManager) this.mContext.getSystemService("wifi")).getScanResults();
        if (scanResults != null) {
            for (ScanResult scanResult : scanResults) {
                if (scanResult.BSSID.equals(fwkNetworkInfo.wlanBssid)) {
                    fwkNetworkInfo.wlanSignalStrength = (byte) (scanResult.level & 255);
                    if (SmartcareConstants.DEBUG_MODE) {
                        Log.d(LOG_TAG, "wlanSignalStrength: " + fwkNetworkInfo.wlanSignalStrength + ",level=" + calculateSignalLevel(scanResult.level) + ",scanResult.level: " + scanResult.level);
                    }
                }
            }
        } else {
            fwkNetworkInfo.wlanSignalStrength = (byte) -1;
        }
        if (fwkNetworkInfo.wlanBssid.length() > 12) {
            fwkNetworkInfo.wlanBssid = fwkNetworkInfo.wlanBssid.substring(0, 12) + "*:*";
        }
        int defaultSlot = HwTelephonyManager.getDefault().getDefault4GSlotId();
        if (defaultSlot < 0 || this.mSignalStrengthInfo == null || defaultSlot >= this.mSignalStrengthInfo.length) {
            Log.w(LOG_TAG, "defaultSlot=" + defaultSlot + ",mSignalStrengthInfo=" + Arrays.toString(this.mSignalStrengthInfo));
            return;
        }
        fwkNetworkInfo.rsrp = (byte) (this.mSignalStrengthInfo[defaultSlot].getLteRsrp() & 255);
        fwkNetworkInfo.rsrq = (byte) (this.mSignalStrengthInfo[defaultSlot].getLteRsrq() & 255);
        fwkNetworkInfo.sinr = (byte) (this.mSignalStrengthInfo[defaultSlot].getLteRssnr() & 255);
    }

    public void addNetworkInfo(EventStream eStream, short imonitorKeyBase, short wlanKeyBase, FwkNetworkInfo fwkNetworkInfo) {
        if (eStream == null) {
            Log.w(LOG_TAG, "addNetworkInfo, eStream is null");
            return;
        }
        short imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
        eStream.setParam((short) imonitorKeyBase, fwkNetworkInfo.mcc);
        imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
        eStream.setParam((short) imonitorKeyBase2, fwkNetworkInfo.mnc);
        imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
        eStream.setParam((short) imonitorKeyBase, (byte) (fwkNetworkInfo.rat & 255));
        imonitorKeyBase = (short) (imonitorKeyBase2 + 1);
        eStream.setParam((short) imonitorKeyBase2, fwkNetworkInfo.timeAndCid);
        imonitorKeyBase = (short) (imonitorKeyBase + 1);
        imonitorKeyBase2 = (short) (imonitorKeyBase + 1);
        EventStream param = eStream.setParam((short) imonitorKeyBase, fwkNetworkInfo.rsrp);
        param.setParam((short) imonitorKeyBase2, fwkNetworkInfo.rsrq).setParam((short) (imonitorKeyBase2 + 1), fwkNetworkInfo.sinr);
        short wlanKeyBase2 = (short) (wlanKeyBase + 1);
        param = eStream.setParam((short) wlanKeyBase, fwkNetworkInfo.wlanSignalStrength);
        param.setParam((short) wlanKeyBase2, fwkNetworkInfo.wlanBssid).setParam((short) (wlanKeyBase2 + 1), fwkNetworkInfo.wlanSsid);
    }

    public static int calculateSignalLevel(int rssi) {
        try {
            int level = WifiManagerCommonEx.calculateSignalLevelHW(rssi) - 1;
            if (level == -1) {
                return 4;
            }
            return level;
        } catch (NoClassDefFoundError error) {
            Log.e(LOG_TAG, "NoClassDefFoundError, error msg: " + error.getMessage());
            return WifiManager.calculateSignalLevel(rssi, 4);
        } catch (NoSuchMethodError error2) {
            Log.e(LOG_TAG, "NoSuchMethodError , error msg: " + error2.getMessage());
            return WifiManager.calculateSignalLevel(rssi, 4);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception , error msg: " + e.getMessage());
            return WifiManager.calculateSignalLevel(rssi, 4);
        }
    }

    public static NetworkType getNetworkType(Context context) {
        if (context != null) {
            ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService("connectivity");
            if (connectMgr != null) {
                NetworkInfo info = connectMgr.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    switch (info.getType()) {
                        case 0:
                            return getNetworkType(info.getSubtype());
                        case 1:
                            return NetworkType.WIFI;
                        default:
                            return NetworkType.UNKNOWN;
                    }
                }
            }
        }
        return NetworkType.UNKNOWN;
    }

    public static NetworkType getNetworkType(int type) {
        switch (type) {
            case 1:
            case 2:
            case 11:
                return NetworkType.TWO_G;
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case 15:
                return NetworkType.THREE_G;
            case 13:
                return NetworkType.FOUR_G;
            default:
                return NetworkType.UNKNOWN;
        }
    }

    private String getCellId() {
        int cell_id = -1;
        int defaultSlot = HwTelephonyManager.getDefault().getDefault4GSlotId();
        if (hasCdma(defaultSlot)) {
            cell_id = getCdmaCellLocationInfo(defaultSlot).getBaseStationId();
        } else if (hasGsm(defaultSlot)) {
            cell_id = getGsmCellLocationInfo(defaultSlot).getCid();
        }
        if (SmartcareConstants.DEBUG_MODE) {
            Log.d(LOG_TAG, "getDefaultFOUR_GSlotId:" + defaultSlot + ",hasCdma: " + hasCdma(defaultSlot) + ",hasGsm: " + hasGsm(defaultSlot));
        }
        if (cell_id == -1) {
            return "";
        }
        return String.valueOf(cell_id);
    }

    public static int getMainCardSlotId() {
        int slotId = 0;
        try {
            slotId = TelephonyManagerEx.getDefault4GSlotId();
            if (SmartcareConstants.DEBUG_MODE) {
                Log.d(LOG_TAG, "getDefaultFOUR_GSlotId:" + slotId);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "getDefaultFOUR_GSlotId card failed");
        }
        return slotId;
    }

    public int getOrientation() {
        return this.mContext != null ? this.mContext.getResources().getConfiguration().orientation : 0;
    }

    private void initSmartcareCommParam() {
        this.mFwkNetworkInfo = new FwkNetworkInfo();
        this.mHttpInfo = new HttpInfo();
        this.mTcpStatusInfo = new TcpStatusInfo();
        this.mBrowserInfo = new BrowserInfo();
        this.mWechatInfo = new WechatInfo();
        this.mEmailInfo = new EmailInfo();
        this.mVideoInfo = new VideoInfo();
        this.mRegInfo = new RegInfo[this.mPhoneNum];
        this.mSignalStrengthInfo = new SignalStrengthInfo[this.mPhoneNum];
        this.mServiceState = new ServiceState[this.mPhoneNum];
        this.mGsmCellLocationInfo = new GsmCellLocationInfo[this.mPhoneNum];
        this.mCdmaCellLocationInfo = new CdmaCellLocationInfo[this.mPhoneNum];
        this.mPsRegisterSuccessTime = new long[this.mPhoneNum];
        for (int i = 0; i < this.mPhoneNum; i++) {
            this.mRegInfo[i] = new RegInfo();
            this.mSignalStrengthInfo[i] = new SignalStrengthInfo();
            this.mServiceState[i] = new ServiceState();
            this.mGsmCellLocationInfo[i] = new GsmCellLocationInfo();
            this.mCdmaCellLocationInfo[i] = new CdmaCellLocationInfo();
        }
        HandlerThread infoProcessorthread = new HandlerThread("infoProcessorthread", 10);
        infoProcessorthread.start();
        mInfosHandler = new InfosHandler(infoProcessorthread.getLooper());
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        this.mCriteria.setAccuracy(1);
        this.mCriteria.setAltitudeRequired(false);
        this.mCriteria.setBearingRequired(false);
        this.mCriteria.setCostAllowed(true);
        this.mCriteria.setPowerRequirement(1);
    }

    private void startPhoneListener() {
        this.mPhoneStateListeners = new PhoneStateListener[this.mPhoneNum];
        for (int i = 0; i < this.mPhoneNum; i++) {
            this.mPhoneStateListeners[i] = getPhoneStateListener(i);
            this.mTelephonyManager.listen(this.mPhoneStateListeners[i], MemoryConstant.MSG_PROTECTLRU_SET_PROTECTZONE);
        }
    }

    private PhoneStateListener getPhoneStateListener(int subscription) {
        return new PhoneStateListener(Integer.valueOf(subscription)) {
            public void onCallStateChanged(int listenCallState, String incomingNumber) {
                if (SmartcareConstants.DEBUG_MODE) {
                    Log.d(SmartcareProc.LOG_TAG, "onCallStateChanged: mListenCallState is: " + listenCallState + " , on sub: " + this.mSubId);
                }
                SmartcareProc.this.mListenCallState[this.mSubId.intValue()] = listenCallState;
            }

            public void onServiceStateChanged(ServiceState state) {
                if (state == null) {
                    Log.e(SmartcareProc.LOG_TAG, "onServiceStateChanged, ss is null, return");
                    return;
                }
                SmartcareProc.this.mRegInfo[this.mSubId.intValue()].update(state);
                if (SmartcareProc.this.mServiceState[this.mSubId.intValue()].getDataRegState() == 1 && state.getDataRegState() == 0) {
                    SmartcareProc.this.mPsRegisterSuccessTime[this.mSubId.intValue()] = SystemClock.elapsedRealtime();
                }
                SmartcareProc.this.mServiceState[this.mSubId.intValue()] = state;
                if (SmartcareConstants.DEBUG_MODE) {
                    Log.d(SmartcareProc.LOG_TAG, "onServiceStateChanged, state is " + state.toString());
                }
            }

            public void onCellLocationChanged(CellLocation location) {
                SmartcareProc.this.updateCellLocationInfo(this.mSubId.intValue(), location);
            }

            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                if (SmartcareConstants.DEBUG_MODE) {
                    Log.d(SmartcareProc.LOG_TAG, "onSignalStrengthsChanged: signalStrength is: " + signalStrength + " , on sub: " + this.mSubId);
                }
                SmartcareProc.this.mSignalStrengthInfo[this.mSubId.intValue()].update(signalStrength);
            }
        };
    }

    private void updateCellLocationInfo(int sub, CellLocation location) {
        if (location != null) {
            try {
                if ((isSubValid(sub) ^ 1) == 0) {
                    if (location instanceof GsmCellLocation) {
                        this.mGsmCellLocationInfo[sub].update((GsmCellLocation) location);
                    } else if (location instanceof CdmaCellLocation) {
                        this.mCdmaCellLocationInfo[sub].update((CdmaCellLocation) location);
                    } else {
                        Log.e(LOG_TAG, "updateCellLocationInfo, location type is wrong");
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception in updateCellLocationInfo", e);
            }
        }
    }

    public FwkNetworkInfo getOldFwkNetworkInfo() {
        return this.mFwkNetworkInfo;
    }

    public LocationManager getLocationManager() {
        return this.mLocationManager;
    }

    public FwkNetworkInfo getFwkNetworkInfo() {
        SmartcareInfos is = getPoolSmartcareInfos();
        if (is.fwkNetworkInfo == null) {
            new FwkNetworkInfo().addToInfos(is);
        }
        return is.fwkNetworkInfo;
    }

    public HttpInfo getHttpInfo() {
        if (this.mBrowserInfo != null) {
            SmartcareInfos is = this.mBrowserInfo.smarcareInfos;
            if (is == null) {
                Log.w(LOG_TAG, "getHttpInfo, infos is null");
                is = new SmartcareInfos();
                this.mBrowserInfo.addToInfos(is);
            }
            if (is.httpInfo == null) {
                new HttpInfo().addToInfos(is);
            }
            this.mHttpInfo = is.httpInfo;
            if (SmartcareConstants.DEBUG_MODE && this.mHttpInfo != null) {
                Log.d(LOG_TAG, "getHttpInfo, mHttpInfo: " + this.mHttpInfo + ",smarcareInfos: " + this.mHttpInfo.smarcareInfos);
            }
        } else {
            Log.e(LOG_TAG, "Exception in getHttpInfo");
        }
        return this.mHttpInfo;
    }

    public TcpStatusInfo getTcpStatusInfo() {
        if (this.mBrowserInfo != null) {
            SmartcareInfos is = this.mBrowserInfo.smarcareInfos;
            if (is == null) {
                Log.w(LOG_TAG, "getTcpStatusInfo, infos is null");
                is = new SmartcareInfos();
                this.mBrowserInfo.addToInfos(is);
            }
            if (is.tcpStatusInfo == null) {
                new TcpStatusInfo().addToInfos(is);
            }
            this.mTcpStatusInfo = is.tcpStatusInfo;
        } else {
            Log.e(LOG_TAG, "Exception in getTcpStatusInfo");
        }
        return this.mTcpStatusInfo;
    }

    public BrowserInfo getBrowserInfo() {
        SmartcareInfos is = getPoolSmartcareInfos();
        if (is.browserInfo == null) {
            new BrowserInfo().addToInfos(is);
        }
        this.mBrowserInfo = is.browserInfo;
        return this.mBrowserInfo;
    }

    public BrowserInfo getCurrentMemberBrowserInfo() {
        if (this.mBrowserInfo == null) {
            this.mBrowserInfo = new BrowserInfo();
        }
        return this.mBrowserInfo;
    }

    public WechatInfo getWechatInfo() {
        SmartcareInfos is = getPoolSmartcareInfos();
        if (is.wechatInfo == null) {
            new WechatInfo().addToInfos(is);
        }
        this.mWechatInfo = is.wechatInfo;
        return this.mWechatInfo;
    }

    public EmailInfo getEmailInfo() {
        SmartcareInfos is = getPoolSmartcareInfos();
        if (is.emailInfo == null) {
            new EmailInfo().addToInfos(is);
        }
        this.mEmailInfo = is.emailInfo;
        return this.mEmailInfo;
    }

    public VideoInfo getVideoInfo() {
        SmartcareInfos is = getPoolSmartcareInfos();
        if (is.videoInfo == null) {
            new VideoInfo().addToInfos(is);
        }
        this.mVideoInfo = is.videoInfo;
        return this.mVideoInfo;
    }

    public GsmCellLocationInfo getGsmCellLocationInfo(int sub) {
        if (isSubValid(sub)) {
            return this.mGsmCellLocationInfo[sub];
        }
        return null;
    }

    public CdmaCellLocationInfo getCdmaCellLocationInfo(int sub) {
        if (isSubValid(sub)) {
            return this.mCdmaCellLocationInfo[sub];
        }
        return null;
    }

    private boolean isWifiOpen(WifiManager oWifimanager) {
        if (oWifimanager.isWifiEnabled() || getPersistedScanAlwaysAvailableEx()) {
            return true;
        }
        return false;
    }

    private boolean getPersistedScanAlwaysAvailableEx() {
        return Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) == 1;
    }

    private boolean isWifiConnected() {
        NetworkInfo mWifi = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(1);
        if (mWifi != null) {
            return mWifi.isConnected();
        }
        Log.e(LOG_TAG, "mWifi is null");
        return false;
    }

    private void getConnectedWifiInfo(WifiManager cWifiManager, LocalWifiInfo info) {
        try {
            if (cWifiManager.getConnectionInfo() != null) {
                info.mConnected_BSSID = cWifiManager.getConnectionInfo().getBSSID();
                info.mConnected_SSID = cWifiManager.getConnectionInfo().getSSID();
                return;
            }
            Log.e(LOG_TAG, " Get Connection info Error ! ");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: getConnectionInfo");
        }
    }

    private void getScanWifiInfo(WifiManager sWifimanager, LocalWifiInfo info) {
        try {
            List<ScanResult> tmp = sWifimanager.getScanResults();
            if (SmartcareConstants.DEBUG_MODE) {
                Log.d(LOG_TAG, "done zn");
            }
            if (tmp != null) {
                Collections.sort(tmp, new Comparator<ScanResult>() {
                    public int compare(ScanResult sr1, ScanResult sr2) {
                        return sr2.level - sr1.level;
                    }
                });
                if (tmp.get(0) != null) {
                    info.mScan_BSSID_1 = ((ScanResult) tmp.get(0)).BSSID;
                }
                if (tmp.get(1) != null) {
                    info.mScan_BSSID_2 = ((ScanResult) tmp.get(1)).BSSID;
                }
                if (tmp.get(2) != null) {
                    info.mScan_BSSID_3 = ((ScanResult) tmp.get(2)).BSSID;
                    return;
                }
                return;
            }
            Log.e(LOG_TAG, " Get ScanResult info Error ! ");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: getScanWifiInfo");
        }
    }

    public LocalWifiInfo getWifiInfo() {
        LocalWifiInfo info = new LocalWifiInfo();
        try {
            WifiManager mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            info.mbIsWiFiOpen = isWifiOpen(mWifiManager);
            if (info.mbIsWiFiOpen) {
                info.mbIsWiFiConnected = isWifiConnected();
                if (info.mbIsWiFiConnected) {
                    if (SmartcareConstants.DEBUG_MODE) {
                        Log.d(LOG_TAG, "WIFI connected");
                    }
                    getConnectedWifiInfo(mWifiManager, info);
                } else {
                    if (SmartcareConstants.DEBUG_MODE) {
                        Log.d(LOG_TAG, "WIFI Scanning");
                    }
                    getScanWifiInfo(mWifiManager, info);
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: getWifiInfo");
        }
        return info;
    }

    public String getOperatorNumeric(int sub) {
        if (isSubValid(sub)) {
            return this.mRegInfo[sub].getRegOperatorNumeric();
        }
        return null;
    }

    public boolean hasGsm(int subId) {
        if (isSubValid(subId)) {
            return this.mRegInfo[subId].isGsm();
        }
        return false;
    }

    public boolean hasCdma(int subId) {
        if (isSubValid(subId)) {
            return this.mRegInfo[subId].isCdma();
        }
        return false;
    }

    private boolean isSubValid(int sub) {
        if (sub >= 0 && sub < this.mPhoneNum) {
            return true;
        }
        Log.e(LOG_TAG, "invalid sub = " + sub + ", mPhoneNum = " + this.mPhoneNum);
        return false;
    }

    public void printNetworkInfo(FileDescriptor fd, PrintWriter pw, String[] args, boolean dumpAll) {
        updateFwkNetworkInfo(this.mFwkNetworkInfo);
        if (dumpAll) {
            pw.println("mHttpInfo.startDate = " + this.mHttpInfo.startDate);
            pw.println("mHttpInfo.startTime = " + this.mHttpInfo.startTime);
            pw.println("mHttpInfo.endTime = " + this.mHttpInfo.endTime);
        }
        pw.println("mFwkNetworkInfo.rat = " + (this.mFwkNetworkInfo.rat & 255));
        pw.println("mFwkNetworkInfo.timeAndCid = " + this.mFwkNetworkInfo.timeAndCid);
        pw.println("mFwkNetworkInfo.wlanSignalStrength =" + this.mFwkNetworkInfo.wlanSignalStrength);
        pw.println("fwkNetworkInfo.rsrp = " + this.mFwkNetworkInfo.rsrp);
        pw.println("fwkNetworkInfo.rsrq = " + this.mFwkNetworkInfo.rsrq);
        pw.println("fwkNetworkInfo.sinr = " + this.mFwkNetworkInfo.sinr);
    }

    public void dumpInfo(FileDescriptor fd, PrintWriter pw, String[] args, boolean dumpAll) {
        printNetworkInfo(fd, pw, args, dumpAll);
    }
}
