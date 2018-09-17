package com.android.server.wifi.wifipro;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.Global;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import com.android.server.wifi.wifipro.TrafficMonitor.TxRxStat;
import com.android.server.wifipro.WifiProCHRManager;

public class MobileQosDetector implements IGetMobileInfoCallBack {
    private static final long ALARM_DATA_SIZE_LEVEL_1 = 10485760;
    private static final int ALARM_DATA_SIZE_LEVEL_1_DISPLAY = 10;
    private static final long ALARM_DATA_SIZE_LEVEL_2 = 52428800;
    private static final int ALARM_DATA_SIZE_LEVEL_2_DISPLAY = 50;
    public static final long ALARM_DATA_UNIT_10M = 10485760;
    public static final int DATA_UNIT_1K_BYTE = 1024;
    private static final boolean DBG = true;
    private static final boolean DEBUG_MODE = false;
    private static final int FORCE_QOS_DELAY = 8000;
    private static final int IPQOS_EXPIRE_LATENCY = 5000;
    private static String MOBILE_DATA_KEY = "mobile_data";
    public static final int MOBILE_INET_QOS_LEVEL_0_NOT_AVAILABLE = 0;
    public static final int MOBILE_INET_QOS_LEVEL_1_VERY_POOR = 1;
    public static final int MOBILE_INET_QOS_LEVEL_2_POOR = 2;
    public static final int MOBILE_INET_QOS_LEVEL_3_MODERATE = 3;
    public static final int MOBILE_INET_QOS_LEVEL_4_GOOD = 4;
    public static final int MOBILE_INET_QOS_LEVEL_5_GREAT = 5;
    public static final int MOBILE_INET_QOS_LEVEL_INVALID = -1;
    public static final int MOBILE_INET_QOS_LEVEL_UNKNOWN = -1;
    private static final int MSG_COMPOSE_MQOS = 101;
    private static final int MSG_IPQOS_EXPIRED = 105;
    private static final int MSG_QUERY_MQOS = 106;
    private static final int MSG_QUERY_TCP_INFO = 107;
    private static final int MSG_RO_STATE_CHANGE = 108;
    private static final int READ_DATA_PERIOD = 2;
    private static final int RECENT_TIME_S = 6;
    private static final int REPORT_TYPE_MONITOR = 1;
    private static final int REPORT_TYPE_QUERY = 0;
    public static final int ROVE_OUT_TO_MOBILE_END = 0;
    private static final int ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY = 2;
    private static final int ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY = 3;
    public static final int ROVE_OUT_TO_MOBILE_START_NOTIFY = 1;
    private static final int RTT_QUERY_DELAY = 5;
    private static final int SIM_STATE_READY = 5;
    private static final int SPEED_EXPIRE_LATENCY = 5000;
    private static String SUB_SELECT_KEY = "user_datacall_sub";
    private static final int SUB_SWITCH_LATENCY = 10000;
    private static String TAG = "MQoS";
    private static final int TCP_INF_SKIP_DELAY = 30000;
    private static final int TCP_LOOP_DELAY = 10000;
    private static final int TYPE_MOBILE = 0;
    private static final int TYPE_WIFI = 1;
    public static final String WIFIPRO_CELLQOS_NOTIFY_ACTION = "wifipro.qos_notify.test_action";
    private INetworkQosCallBack mCallback = null;
    private Handler mCallerHandler = null;
    private ConnectivityManager mConnMgr = null;
    private ContentResolver mContentRsr;
    private Context mContext = null;
    private int mCurrDataSubID = 0;
    private long mDataConnTimestamp = System.currentTimeMillis();
    private boolean mDataSuspend = false;
    private int mEvents = 321;
    private Handler mHandler;
    private int[] mHisSimStatus = null;
    private double[][] mIPQWeight = new double[][]{new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d}, new double[]{0.0d, 0.8d, 0.5d, 0.5d, 0.8d, 0.0d}, new double[]{0.0d, 0.8d, 0.5d, 0.5d, 0.8d, 0.0d}, new double[]{0.0d, 0.8d, 0.5d, 0.5d, 0.8d, 0.0d}, new double[]{0.0d, 0.8d, 0.5d, 0.5d, 0.8d, 0.0d}, new double[]{0.0d, 0.8d, 0.5d, 0.5d, 0.8d, 0.0d}};
    private int mIPQ_Level = -1;
    private boolean mIsMonitoring = false;
    private int mIsRoveOutState = 0;
    private long mLastAlarmTotBytes = 0;
    private PhoneStateListener[] mListener = null;
    private long mLowLevelTimestamp = System.currentTimeMillis();
    private boolean mMobileDataSwitch = false;
    private final int mPhoneNum;
    private int mQOS_Level = -1;
    private double[] mRAT2G_Weight = new double[]{0.0d, 0.5d, 0.5d, 0.5d, 0.5d, 0.5d};
    private double[] mRAT3G_EVDOA_Weight = new double[]{0.0d, 0.5d, 0.5d, 0.5d, 0.5d, 0.5d};
    private double[] mRAT3G_EVDOB_Weight = new double[]{0.0d, 0.5d, 0.5d, 0.6d, 0.6d, 0.6d};
    private double[] mRAT3G_TDS_Weight = new double[]{0.0d, 0.5d, 0.5d, 0.6d, 0.6d, 0.6d};
    private double[] mRAT3G_UMTS_Weight = new double[]{0.0d, 1.0d, 1.0d, 1.0d, 1.0d, 1.0d};
    private double[] mRAT4G_Weight = new double[]{0.0d, 1.0d, 1.0d, 1.0d, 1.0d, 1.0d};
    private long mRO_StartRxBytes = 0;
    private long mRO_StartTxBytes = 0;
    private double[][] mRTTWeight = new double[][]{new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d}, new double[]{0.0d, 0.7d, 0.7d, 0.8d, 0.8d, 0.8d}, new double[]{0.0d, 0.7d, 0.7d, 0.8d, 0.8d, 0.8d}, new double[]{0.0d, 0.7d, 0.7d, 0.8d, 0.8d, 0.8d}, new double[]{0.0d, 0.7d, 0.7d, 0.8d, 0.8d, 0.8d}, new double[]{0.0d, 0.7d, 0.7d, 0.8d, 0.8d, 0.8d}};
    private TCPIpqRtt mRtt = new TCPIpqRtt();
    private int[] mSIMArray = null;
    private int[] mSignalArray = null;
    private SignalStrength mSignalStrength = null;
    private int mSignal_Level = -1;
    private int mSimStatus = 0;
    private int mSpeed = -1;
    private Runnable mSpeedUpdate = new Runnable() {
        public void run() {
            int old_speed = MobileQosDetector.this.mSpeed_Level;
            MobileQosDetector.this.mSpeed = MobileQosDetector.this.mTraffic.getRxByteSpeed(1);
            MobileQosDetector.this.mSpeed_Level = MobileQosDetector.this.mTraffic.transform(MobileQosDetector.this.mSpeed);
            if (old_speed != MobileQosDetector.this.mSpeed_Level) {
                MobileQosDetector.this.requestComputeQos(0);
            }
            MobileQosDetector.this.logi("speed:oldlevel:" + old_speed + ",new level:" + MobileQosDetector.this.mSpeed_Level + ",newSpeed(Bps):" + MobileQosDetector.this.mSpeed);
        }
    };
    private int mSpeed_Level = -1;
    private long mSubIdChangeTimsstamp = System.currentTimeMillis();
    private TelephonyManager mTelephonyMgr = null;
    private long mTotalBytes = 0;
    private TrafficMonitor mTraffic = null;
    private WifiProUIDisplayManager mUIManager;
    private WifiProCHRManager mWiFiCHRManager;
    private WifiProStatisticsManager mWifiProStatisticsManager;
    private int readDataPeriod = 0;

    class MobileQosDetectHandler extends Handler {
        /* synthetic */ MobileQosDetectHandler(MobileQosDetector this$0, Looper looper, MobileQosDetectHandler -this2) {
            this(looper);
        }

        private MobileQosDetectHandler(Looper looper) {
            super(looper);
            MobileQosDetector.this.logi("new MobileQosDetectHandler");
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    int old_level = MobileQosDetector.this.mQOS_Level;
                    MobileQosDetector.this.composeQOSLevel(-1, 1);
                    if (old_level != MobileQosDetector.this.mQOS_Level && MobileQosDetector.this.mIsMonitoring) {
                        MobileQosDetector.this.reportQos(1, MobileQosDetector.this.mQOS_Level);
                    }
                    MobileQosDetector.this.mHandler.removeMessages(101);
                    return;
                case 105:
                    MobileQosDetector.this.mIPQ_Level = -1;
                    MobileQosDetector.this.mHandler.removeMessages(105);
                    return;
                case 106:
                    Log.i(MobileQosDetector.TAG, "queryNetworkQos enter");
                    MobileQosDetector.this.composeQOSLevel(-1, 0);
                    MobileQosDetector.this.reportQos(0, MobileQosDetector.this.mQOS_Level);
                    Log.i(MobileQosDetector.TAG, "queryNetworkQos exit");
                    return;
                case 107:
                    Log.i(MobileQosDetector.TAG, "MSG_QUERY_TCP_INFO called,count:" + msg.arg2);
                    MobileQosDetector.this.queryRtt();
                    NetworkInfo netinfo = MobileQosDetector.this.mConnMgr.getActiveNetworkInfo();
                    if ((netinfo != null ? netinfo.getType() : -1) == 0) {
                        if (MobileQosDetector.this.mHandler.hasMessages(107)) {
                            Log.i(MobileQosDetector.TAG, "MSG_QUERY_TCP_INFO loop called,skip:" + msg.arg2);
                        } else {
                            MobileQosDetector.this.mHandler.sendMessageDelayed(MobileQosDetector.this.mHandler.obtainMessage(107, 0, msg.arg2 + 1), 10000);
                        }
                        MobileQosDetector.this.periodCheckHighDataFlow();
                        return;
                    }
                    MobileQosDetector.this.mHandler.removeMessages(107);
                    Log.i(MobileQosDetector.TAG, "stop query RTT!");
                    return;
                case 108:
                    int roState = msg.arg1;
                    if (MobileQosDetector.this.mTraffic == null) {
                        return;
                    }
                    if (1 == roState) {
                        MobileQosDetector.this.mIsRoveOutState = 1;
                        MobileQosDetector.this.mRO_StartRxBytes = MobileQosDetector.this.mTraffic.getMobileRxBytes();
                        MobileQosDetector.this.mRO_StartTxBytes = MobileQosDetector.this.mTraffic.getMobileTxBytes();
                        MobileQosDetector.this.mTotalBytes = 0;
                        MobileQosDetector.this.logi("rove out start, first read rxBytes=" + MobileQosDetector.this.mRO_StartRxBytes + ", txBytes=" + MobileQosDetector.this.mRO_StartTxBytes);
                        return;
                    } else if (roState == 0) {
                        MobileQosDetector.this.mIsRoveOutState = 0;
                        if (MobileQosDetector.this.mUIManager != null) {
                            MobileQosDetector.this.mUIManager.cleanUpheadNotificationHMD();
                        }
                        MobileQosDetector.this.logi("rove out end.");
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }
    }

    static class TCPIpqRtt {
        public static final int RTT_FINE_5 = 300;
        public static final int RTT_GOOD_4 = 850;
        public static final int RTT_MID_3 = 1400;
        public static final int RTT_POOR_2 = 1900;
        public static final int RTT_VALID_PKTS = 5;
        public static final int RTT_VERY_POOR_1 = 2040;
        public int cong_when;
        public int congestion;
        public int level;
        public int rtt;
        public int rtt_pkts;
        public int rtt_when;
        public int tcp_retrans_pkts;
        public int tcp_rx_pkts;
        public int tcp_tx_pkts;
        public long timestamp;
        public int type;

        public TCPIpqRtt() {
            reset();
        }

        public void reset() {
            this.rtt = 0;
            this.rtt_pkts = 0;
            this.rtt_when = 0;
            this.congestion = 0;
            this.cong_when = 0;
            this.timestamp = 0;
            this.type = 0;
            this.level = 0;
            this.tcp_tx_pkts = 0;
            this.tcp_rx_pkts = 0;
            this.tcp_retrans_pkts = 0;
        }

        public void setMember(int rtt, int rtt_pkts, int rtt_when, int congestion, int cong_when, int type, int lvl, long ts, int tcp_tx_pkts, int tcp_rx_pkts, int tcp_retans_pkts) {
            this.rtt = rtt;
            this.rtt_pkts = rtt_pkts;
            this.rtt_when = rtt_when;
            this.congestion = congestion;
            this.cong_when = cong_when;
            this.timestamp = ts;
            this.type = type;
            this.level = lvl;
            this.tcp_tx_pkts = tcp_tx_pkts;
            this.tcp_rx_pkts = tcp_rx_pkts;
            this.tcp_retrans_pkts = tcp_retans_pkts;
        }

        public int getRttLevel() {
            if (this.rtt < 300) {
                return 5;
            }
            if (this.rtt < RTT_GOOD_4) {
                return 4;
            }
            if (this.rtt < RTT_MID_3) {
                return 3;
            }
            if (this.rtt < RTT_POOR_2) {
                return 2;
            }
            if (this.rtt < RTT_VERY_POOR_1) {
                return 1;
            }
            return 1;
        }

        public String toString() {
            return "rtt=" + this.rtt + ",rtt_pkts=" + this.rtt_pkts + ",rtt_when=" + this.rtt_when + ",congestion=" + this.congestion + ",cong_when=" + this.cong_when + ",type=" + this.type + ",lvl=" + this.level + ",ts=" + this.timestamp;
        }
    }

    public MobileQosDetector(Context ctxt, INetworkQosCallBack cb, Handler hdlr, WifiProUIDisplayManager wifiProUIDisplayManager) {
        this.mContext = ctxt;
        this.mCallback = cb;
        this.mCallerHandler = hdlr;
        this.mUIManager = wifiProUIDisplayManager;
        initMQDHandler();
        this.mTelephonyMgr = (TelephonyManager) ctxt.getSystemService("phone");
        this.mConnMgr = (ConnectivityManager) ctxt.getSystemService("connectivity");
        this.mPhoneNum = this.mTelephonyMgr.getPhoneCount();
        this.mSignalArray = new int[this.mPhoneNum];
        this.mSIMArray = new int[this.mPhoneNum];
        this.mListener = new PhoneStateListener[this.mPhoneNum];
        this.mHisSimStatus = new int[this.mPhoneNum];
        for (int i = 0; i < this.mPhoneNum; i++) {
            this.mListener[i] = getPSListener(i);
            this.mTelephonyMgr.listen(this.mListener[i], this.mEvents);
            this.mSignalArray[i] = -1;
            this.mSIMArray[i] = this.mTelephonyMgr.getSimState(i);
            this.mHisSimStatus[i] = this.mSIMArray[i];
        }
        logi("PhoneNum=" + this.mPhoneNum);
        initObserver();
        this.mCurrDataSubID = SubscriptionManager.getDefaultDataSubscriptionId();
        if (this.mCurrDataSubID < 0) {
            int subid = 0;
            for (int k = 0; k < this.mPhoneNum; k++) {
                if (this.mSIMArray[k] == 5) {
                    subid = k;
                    break;
                }
            }
            this.mCurrDataSubID = subid;
            logi("subId recheck=" + this.mCurrDataSubID);
        }
        this.mSimStatus = this.mTelephonyMgr.getSimState(this.mCurrDataSubID);
        logi("init defSubID=" + this.mCurrDataSubID + ",SIMReady=" + this.mSimStatus);
        this.mTraffic = new TrafficMonitor(this.mSpeedUpdate);
        this.mWiFiCHRManager = WifiProCHRManager.getInstance();
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
        if (this.mWifiProStatisticsManager != null) {
            this.mWifiProStatisticsManager.registerMobileInfoCallback(this);
        }
    }

    public void monitorNetworkQos(boolean enable) {
        if (enable) {
            this.mTraffic.enableMonitor(true, 0);
            this.mTraffic.setExpireTime(ClientHandler.DEFAULT_WIFI_ROUTE_CHECK_TIME_FIRST);
        } else {
            this.mTraffic.enableMonitor(false, 0);
        }
        this.mIsMonitoring = enable;
    }

    public void queryNetworkQos() {
        Log.i(TAG, "queryNetworkQos start");
        queryRtt();
        this.mHandler.sendEmptyMessageDelayed(106, 5);
    }

    public String queryNetworkId() {
        int type = this.mTelephonyMgr.getPhoneType();
        String plmn = this.mTelephonyMgr.getNetworkOperator();
        int cellid = 0;
        switch (type) {
            case 1:
                GsmCellLocation gsmCellLocation = (GsmCellLocation) this.mTelephonyMgr.getCellLocation();
                if (gsmCellLocation != null) {
                    cellid = gsmCellLocation.getCid();
                    break;
                }
                break;
            case 2:
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) this.mTelephonyMgr.getCellLocation();
                if (cdmaCellLocation != null) {
                    cellid = cdmaCellLocation.getBaseStationId();
                    break;
                }
                break;
        }
        return plmn + cellid;
    }

    public void setIPQos(int type, int len, int[] ipqos) {
        int tcp_rtt = len > 0 ? ipqos[0] : 0;
        int tcp_rtt_pkts = len > 1 ? ipqos[1] : 0;
        int tcp_rtt_when = len > 2 ? ipqos[2] : 0;
        int tcp_congestion = len > 3 ? ipqos[3] : 0;
        int tcp_cong_when = len > 4 ? ipqos[4] : 0;
        int tcp_level = len > 5 ? ipqos[5] : -1;
        long ts = System.currentTimeMillis() - ((long) tcp_rtt_when);
        int tcp_tx_pkts = len > 6 ? ipqos[6] : 0;
        int tcp_rx_pkts = len > 7 ? ipqos[7] : 0;
        int tcp_retran_pkts = len > 8 ? ipqos[8] : 0;
        if (type == 1) {
            this.mIPQ_Level = tcp_level;
            this.mHandler.removeMessages(105);
            this.mHandler.sendEmptyMessageDelayed(105, 5000);
        }
        this.mRtt.setMember(tcp_rtt, tcp_rtt_pkts, tcp_rtt_when, tcp_congestion, tcp_cong_when, type, tcp_level, ts, tcp_tx_pkts, tcp_rx_pkts, tcp_retran_pkts);
        requestComputeQos(0);
        Log.i(TAG, "rtt=" + tcp_rtt + ",tcp_tx_pkts=" + tcp_tx_pkts + ",tcp_rx_pkts=" + tcp_rx_pkts);
    }

    private void queryRtt() {
        this.mCallerHandler.sendEmptyMessage(4);
    }

    private void requestComputeQos(long delay_ms) {
        this.mHandler.sendEmptyMessageDelayed(101, delay_ms);
    }

    private void reportQos(int report_type, int qos_level) {
        Log.i(TAG, "entry report: qos lvl=" + qos_level + ",report_type:" + report_type);
        if (this.mCallback == null) {
            Log.e(TAG, "mCallback is null!, report_type=" + report_type);
            return;
        }
        if (report_type == 0) {
            Log.i(TAG, "before query report: qos lvl=" + qos_level);
            this.mCallback.onNetworkDetectionResult(0, qos_level);
        } else if (report_type == 1) {
            this.mCallback.onNetworkQosChange(0, qos_level, true);
        }
        Log.i(TAG, "after report: qos lvl=" + qos_level + ",svc working:" + this.mIsMonitoring);
    }

    private void initObserver() {
        this.mContentRsr = this.mContext.getContentResolver();
        ContentObserver dataSwitchObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                MobileQosDetector.this.mMobileDataSwitch = MobileQosDetector.this.getDataStatus();
                MobileQosDetector.this.requestComputeQos(0);
                MobileQosDetector.this.logi("MobileData switch:" + MobileQosDetector.this.mMobileDataSwitch);
            }
        };
        ContentObserver subSelectObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                int oldId = MobileQosDetector.this.mCurrDataSubID;
                MobileQosDetector.this.mCurrDataSubID = Global.getInt(MobileQosDetector.this.mContentRsr, MobileQosDetector.SUB_SELECT_KEY, 0);
                MobileQosDetector.this.logi("new subID:" + MobileQosDetector.this.mCurrDataSubID + " is Selected,oldId:" + oldId);
                if (oldId != MobileQosDetector.this.mCurrDataSubID) {
                    int subid = MobileQosDetector.this.mCurrDataSubID;
                    if (subid < 0 || subid >= MobileQosDetector.this.mPhoneNum) {
                        subid = 0;
                    }
                    MobileQosDetector.this.mSignal_Level = MobileQosDetector.this.mSignalArray[subid];
                    MobileQosDetector.this.mSpeed_Level = -1;
                    MobileQosDetector.this.mIPQ_Level = -1;
                    MobileQosDetector.this.mRtt.reset();
                    MobileQosDetector.this.mTraffic.reset();
                    MobileQosDetector.this.mSimStatus = MobileQosDetector.this.mTelephonyMgr.getSimState(MobileQosDetector.this.mCurrDataSubID);
                    MobileQosDetector.this.logi("SIM status:" + MobileQosDetector.this.mSimStatus);
                    MobileQosDetector.this.mSubIdChangeTimsstamp = System.currentTimeMillis();
                    MobileQosDetector.this.requestComputeQos(10000);
                }
            }
        };
        this.mContentRsr.registerContentObserver(Global.getUriFor(MOBILE_DATA_KEY), false, dataSwitchObserver);
        this.mContentRsr.registerContentObserver(Global.getUriFor(SUB_SELECT_KEY), false, subSelectObserver);
        this.mMobileDataSwitch = getDataStatus();
    }

    private boolean getDataStatus() {
        if (Global.getInt(this.mContentRsr, MOBILE_DATA_KEY, 0) == 1) {
            return true;
        }
        return false;
    }

    private PhoneStateListener getPSListener(int subscription) {
        return new DualPhoneStateListener(subscription) {
            int mSubId = DualPhoneStateListener.getSubscription(this);

            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                Log.i(MobileQosDetector.TAG, "onSignal: mSubId=" + this.mSubId + ",currDataSubID=" + MobileQosDetector.this.mCurrDataSubID);
                MobileQosDetector.this.mSignalStrength = new SignalStrength(signalStrength);
                int signal_level = MobileQosDetector.this.convertSignalLevel(MobileQosDetector.this.mSignalStrength);
                int subid = (this.mSubId < 0 || this.mSubId >= MobileQosDetector.this.mPhoneNum) ? 0 : this.mSubId;
                MobileQosDetector.this.mSignalArray[subid] = signal_level;
                if (this.mSubId == MobileQosDetector.this.mCurrDataSubID && MobileQosDetector.this.mSignal_Level != MobileQosDetector.this.mSignalArray[subid]) {
                    MobileQosDetector.this.mSignal_Level = MobileQosDetector.this.mSignalArray[subid];
                    MobileQosDetector.this.requestComputeQos(0);
                }
            }

            public void onDataConnectionStateChanged(int state, int networkType) {
                Log.i(MobileQosDetector.TAG, "onDataConn: mSubId=" + this.mSubId + ",state=" + state + ",networkType=" + networkType);
                if (state == 2) {
                    MobileQosDetector.this.mCurrDataSubID = this.mSubId;
                    MobileQosDetector.this.mSimStatus = MobileQosDetector.this.mTelephonyMgr.getSimState(MobileQosDetector.this.mCurrDataSubID);
                    Log.i(MobileQosDetector.TAG, "onDataConn: mSubId=" + this.mSubId + ",mSimStatus=" + MobileQosDetector.this.mSimStatus);
                    MobileQosDetector.this.requestComputeQos(0);
                    if (MobileQosDetector.this.mHandler.hasMessages(107)) {
                        Log.i(MobileQosDetector.TAG, "has pending msg(QUERY_TCP_INFO),skip!");
                    } else {
                        MobileQosDetector.this.mHandler.sendEmptyMessageDelayed(107, 30000);
                    }
                    MobileQosDetector.this.mDataConnTimestamp = System.currentTimeMillis();
                    MobileQosDetector.this.requestComputeQos(30000);
                }
                if (MobileQosDetector.this.mCurrDataSubID == this.mSubId) {
                    boolean suspend = state == 3;
                    if (suspend != MobileQosDetector.this.mDataSuspend) {
                        MobileQosDetector.this.mDataSuspend = suspend;
                        MobileQosDetector.this.requestComputeQos(0);
                    }
                }
            }

            public void onServiceStateChanged(ServiceState serviceState) {
                int voiceState = serviceState.getState();
                Log.i(MobileQosDetector.TAG, "mSubId=" + this.mSubId + ",voiceState:" + voiceState + ",dataState:" + serviceState.getDataRegState());
                if (voiceState == 0) {
                    MobileQosDetector.this.mSIMArray[this.mSubId] = 5;
                    MobileQosDetector.this.mHisSimStatus[this.mSubId] = 5;
                    if (MobileQosDetector.this.mSimStatus != 5 && this.mSubId == MobileQosDetector.this.mCurrDataSubID) {
                        MobileQosDetector.this.mSimStatus = 5;
                        MobileQosDetector.this.requestComputeQos(0);
                    }
                    String plmn = MobileQosDetector.this.mTelephonyMgr.getNetworkOperator();
                    if (plmn == null || plmn.length() < 3) {
                        Log.e(MobileQosDetector.TAG, "MCC invalid");
                        return;
                    }
                    String mcc = plmn.substring(0, 3);
                    try {
                        int mcc_code = Integer.parseInt(mcc);
                        if (mcc_code > 0) {
                            Message.obtain(MobileQosDetector.this.mCallerHandler, 5, mcc_code, 0).sendToTarget();
                            return;
                        }
                        return;
                    } catch (NumberFormatException ne) {
                        Log.e(MobileQosDetector.TAG, "getMCC error:" + mcc);
                        ne.printStackTrace();
                        return;
                    }
                }
                MobileQosDetector.this.mSIMArray[this.mSubId] = 0;
                if (this.mSubId == MobileQosDetector.this.mCurrDataSubID) {
                    MobileQosDetector.this.mSimStatus = 0;
                    MobileQosDetector.this.requestComputeQos(0);
                }
            }
        };
    }

    private int convertSignalLevel(SignalStrength ss) {
        int signal_level = 0;
        if (ss == null) {
            return 0;
        }
        int level = ss.getLevel();
        switch (level) {
            case 0:
                signal_level = 1;
                break;
            case 1:
                signal_level = 2;
                break;
            case 2:
                signal_level = 3;
                break;
            case 3:
                signal_level = 4;
                break;
            case 4:
                signal_level = 5;
                break;
            default:
                if (level > 4) {
                    signal_level = 5;
                    break;
                }
                break;
        }
        Log.i(TAG, "received cell-signal:" + signal_level);
        return signal_level;
    }

    private int convertIPQLevel(int signal_lvl, int ipq_lvl) {
        int signal_level;
        if (signal_lvl >= 0) {
            signal_level = signal_lvl;
        } else {
            signal_level = 0;
        }
        int ipq_level = ipq_lvl > 0 ? ipq_lvl : 0;
        double w_ipqos = this.mIPQWeight[signal_level][ipq_level];
        return Math.round((float) ((((double) ipq_level) * w_ipqos) + (((double) signal_level) * (1.0d - w_ipqos))));
    }

    private int convertRTTLevel(int signal_lvl, int rtt_lvl) {
        int signal_level;
        if (signal_lvl >= 0) {
            signal_level = signal_lvl;
        } else {
            signal_level = 0;
        }
        int rtt_level = rtt_lvl > 0 ? rtt_lvl : 0;
        double w_ipqos = this.mRTTWeight[signal_level][rtt_level];
        return Math.round((float) ((((double) rtt_level) * w_ipqos) + (((double) signal_level) * (1.0d - w_ipqos))));
    }

    private int convertRATLevel(int signal_Level, int net_type) {
        int sig_level;
        if (signal_Level >= 0) {
            sig_level = signal_Level;
        } else {
            sig_level = 0;
        }
        int new_level = sig_level;
        TelephonyManager telephonyManager = this.mTelephonyMgr;
        int RAT_class = TelephonyManager.getNetworkClass(net_type);
        if (1 == RAT_class) {
            new_level = (int) Math.floor((double) ((float) (((double) sig_level) * this.mRAT2G_Weight[sig_level])));
        } else if (2 == RAT_class) {
            switch (net_type) {
                case 3:
                case 8:
                case 9:
                case 10:
                case 15:
                    new_level = Math.round((float) (((double) sig_level) * this.mRAT3G_UMTS_Weight[sig_level]));
                    break;
                case 5:
                case 6:
                    new_level = Math.round((float) (((double) sig_level) * this.mRAT3G_EVDOA_Weight[sig_level]));
                    break;
                case 12:
                    new_level = Math.round((float) (((double) sig_level) * this.mRAT3G_EVDOB_Weight[sig_level]));
                    break;
                default:
                    new_level = Math.round((float) (((double) sig_level) * this.mRAT3G_TDS_Weight[sig_level]));
                    break;
            }
        } else if (3 == RAT_class) {
            new_level = Math.round((float) (((double) sig_level) * this.mRAT4G_Weight[sig_level]));
        } else {
            Log.e(TAG, "unkown RAT!");
        }
        if (new_level < 1) {
            return 1;
        }
        return new_level;
    }

    private String getRATName(int net_type) {
        String rat_name = "unknownRAT";
        TelephonyManager telephonyManager = this.mTelephonyMgr;
        switch (TelephonyManager.getNetworkClass(net_type)) {
            case 1:
                return "2G";
            case 2:
                switch (net_type) {
                    case 3:
                    case 8:
                    case 9:
                    case 10:
                    case 15:
                        return "3G-UMTS";
                    case 5:
                    case 6:
                    case 12:
                        return "3G-EVDO";
                    default:
                        return "3G-TD";
                }
            case 3:
                return "4G";
            default:
                return rat_name;
        }
    }

    private int getRATClass(int net_type) {
        TelephonyManager telephonyManager = this.mTelephonyMgr;
        switch (TelephonyManager.getNetworkClass(net_type)) {
            case 1:
                return 1;
            case 2:
                switch (net_type) {
                    case 3:
                    case 8:
                    case 9:
                    case 10:
                    case 15:
                        return 4;
                    case 5:
                    case 6:
                    case 12:
                        return 3;
                    default:
                        return 2;
                }
            case 3:
                return 5;
            default:
                return 0;
        }
    }

    public synchronized int onGetMobileSignalLevel() {
        return this.mSignal_Level;
    }

    public synchronized int onGetMobileRATType() {
        if (this.mTelephonyMgr == null) {
            return 0;
        }
        return getRATClass(this.mTelephonyMgr.getNetworkType());
    }

    public synchronized int getTotalRoMobileData() {
        int totData;
        totData = (int) (this.mTotalBytes / 1024);
        logi("getTotalRoMobileData total data=" + totData);
        return totData;
    }

    public void sendMsg(int what, int arg1, int arg2) {
        if (this.mHandler != null) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, what, arg1, arg2));
        }
    }

    public synchronized void setRoveOutToMobileState(int roveOutState) {
        logi("setRoveOutToMobileState enter, state=" + roveOutState);
        sendMsg(108, roveOutState, 0);
    }

    private void periodCheckHighDataFlow() {
        if (this.mUIManager == null) {
            Log.e(TAG, "mUIManager null error.");
            return;
        }
        if (this.mIsRoveOutState != 0) {
            int i = this.readDataPeriod + 1;
            this.readDataPeriod = i;
            if (i >= 2) {
                long rxBytes = this.mTraffic.getMobileRxBytes();
                long txBytes = this.mTraffic.getMobileTxBytes();
                if (rxBytes == -1 || txBytes == -1) {
                    Log.e(TAG, "read rx tx error, rx=" + rxBytes + ", tx=" + txBytes);
                } else if (this.mRO_StartRxBytes == 0 && this.mRO_StartTxBytes == 0) {
                    this.mRO_StartRxBytes = rxBytes;
                    this.mRO_StartTxBytes = txBytes;
                    logi("last read data invalid.");
                } else {
                    if (rxBytes >= this.mRO_StartRxBytes && txBytes >= this.mRO_StartTxBytes) {
                        this.mTotalBytes += (rxBytes - this.mRO_StartRxBytes) + (txBytes - this.mRO_StartTxBytes);
                    }
                    if (rxBytes > 0 || txBytes > 0) {
                        this.mRO_StartRxBytes = rxBytes;
                        this.mRO_StartTxBytes = txBytes;
                    }
                    logi("rove out start, rxBytes=" + rxBytes + ", txBytes=" + txBytes + ", totBytes=" + this.mTotalBytes);
                    int dataSize;
                    if (1 == this.mIsRoveOutState && this.mTotalBytes >= 10485760) {
                        this.mUIManager.showHMDNotification(10, true);
                        this.mIsRoveOutState = 2;
                        this.mLastAlarmTotBytes = this.mTotalBytes;
                        logi(" high data flow >= 10M, set first notify.");
                        this.mWifiProStatisticsManager.increaseHMDNotifyCount(1);
                    } else if (2 == this.mIsRoveOutState) {
                        if (this.mTotalBytes >= ALARM_DATA_SIZE_LEVEL_2) {
                            this.mUIManager.showHMDNotification(50, true);
                            this.mIsRoveOutState = 3;
                            logi(" high data flow >= 50M, show 50M notify.");
                            this.mLastAlarmTotBytes = this.mTotalBytes;
                            this.mWifiProStatisticsManager.increaseHMDNotifyCount(2);
                        } else if (this.mTotalBytes - this.mLastAlarmTotBytes > 10485760) {
                            dataSize = ((int) (this.mTotalBytes / 10485760)) * 10;
                            this.mUIManager.showHMDNotification(dataSize, false);
                            logi(" high data flow > " + dataSize + "M, update notify display.");
                            this.mLastAlarmTotBytes = this.mTotalBytes;
                        }
                    } else if (3 == this.mIsRoveOutState && this.mTotalBytes - this.mLastAlarmTotBytes > 10485760) {
                        dataSize = ((int) (this.mTotalBytes / 10485760)) * 10;
                        this.mUIManager.showHMDNotification(dataSize, false);
                        logi(" high data flow > " + dataSize + "M, update notify display.");
                        this.mLastAlarmTotBytes = this.mTotalBytes;
                    }
                    this.readDataPeriod = 0;
                }
            }
        }
    }

    private void logi(String msg) {
        Log.i(TAG, msg);
    }

    private void composeQOSLevel(int signal_level, int report_type) {
        int conntype = -1;
        NetworkInfo netinfo = this.mConnMgr.getActiveNetworkInfo();
        String compose_info = "unknown";
        int oldQosLevel = this.mQOS_Level;
        int qos_Level = this.mQOS_Level;
        if (this.mHisSimStatus != null && this.mHisSimStatus.length != 0) {
            long now;
            if (netinfo != null) {
                conntype = netinfo.getType();
            }
            if (signal_level < 0) {
                if (this.mSignal_Level >= 0) {
                    signal_level = this.mSignal_Level;
                } else {
                    signal_level = 0;
                }
            }
            boolean sim_ready = this.mSimStatus != 5 ? this.mCurrDataSubID >= 0 && this.mCurrDataSubID < this.mPhoneNum && this.mSIMArray[this.mCurrDataSubID] == 5 : true;
            int net_type;
            if (!this.mMobileDataSwitch) {
                qos_Level = 0;
                compose_info = "DataDisable;use level-0";
            } else if (this.mDataSuspend) {
                qos_Level = 0;
                compose_info = "DataSuspend;use level-0";
            } else if (this.mTelephonyMgr.isNetworkRoaming(this.mCurrDataSubID)) {
                qos_Level = 0;
                compose_info = "dis-Roaming;use level-0";
            } else if (conntype == 1) {
                if (!sim_ready) {
                    int simState = this.mTelephonyMgr.getSimState(this.mCurrDataSubID);
                    ServiceState serviceState = this.mTelephonyMgr.getServiceStateForSubscriber(this.mCurrDataSubID);
                    if (simState == 5 && this.mPhoneNum >= 0 && this.mPhoneNum <= 3 && serviceState != null && serviceState.getState() == 0) {
                        for (int i = 0; i < this.mPhoneNum; i++) {
                            this.mTelephonyMgr.listen(this.mListener[i], this.mEvents);
                            this.mSignalArray[i] = -1;
                            this.mSIMArray[i] = this.mTelephonyMgr.getSimState(i);
                            this.mHisSimStatus[i] = this.mSIMArray[i];
                        }
                        this.mSimStatus = 5;
                        sim_ready = true;
                        logi("SIM status change, sim_ready = " + true);
                    }
                }
                if (sim_ready) {
                    net_type = this.mTelephonyMgr.getNetworkType();
                    qos_Level = convertRATLevel(signal_level, net_type);
                    compose_info = "wifi-link;use signalLvl:" + signal_level + ";RAT:" + getRATName(net_type);
                } else {
                    qos_Level = 0;
                    compose_info = "wifi-link;SIM error;use level-0";
                    int i2 = this.mHisSimStatus[this.mCurrDataSubID];
                }
            } else if (conntype != 0) {
                qos_Level = 0;
                compose_info = "unknown-link;use Lvl0";
                if (System.currentTimeMillis() - this.mSubIdChangeTimsstamp < 10000) {
                    qos_Level = convertRATLevel(signal_level, this.mTelephonyMgr.getNetworkType());
                    compose_info = "unknown-link&Sub-Change;use RAT-level";
                } else if (this.mHisSimStatus[this.mCurrDataSubID] == 5 && !sim_ready) {
                    logi("SIM status error");
                }
            } else if (this.mSpeed_Level == 5 || this.mSpeed_Level == 4) {
                qos_Level = this.mSpeed_Level;
                compose_info = "use speedLvl:" + this.mSpeed_Level;
            } else {
                net_type = this.mTelephonyMgr.getNetworkType();
                int RAT_lvl = convertRATLevel(signal_level, net_type);
                now = System.currentTimeMillis();
                long rtt_escape = now - this.mRtt.timestamp;
                boolean tcp_is_valid = now - this.mDataConnTimestamp >= 30000;
                boolean result = false;
                if ((this.mIPQ_Level == 1 || this.mIPQ_Level == 2) && RAT_lvl < 3 && tcp_is_valid) {
                    qos_Level = convertIPQLevel(RAT_lvl, this.mIPQ_Level);
                    compose_info = "use IPQLvl:" + this.mIPQ_Level + ";RATLvl:" + RAT_lvl;
                    result = true;
                }
                if (!result) {
                    if (rtt_escape > 5000 || !tcp_is_valid || this.mRtt.rtt_pkts <= 5) {
                        TxRxStat stat = this.mTraffic.getStatic(6);
                        if (stat.rx_tx_rto >= 0.4d || stat.txPkts <= 20) {
                            qos_Level = RAT_lvl;
                            compose_info = "use RAT:" + getRATName(net_type) + ";signalLvl:" + signal_level;
                        } else {
                            qos_Level = 1;
                            compose_info = "use Rx/Tx:" + stat.rx_tx_rto + ";TxPktSum:" + stat.txPkts + ";recent 5s,work:" + this.mTraffic.isWorking();
                        }
                    } else {
                        int rtt_lvl = this.mRtt.getRttLevel();
                        qos_Level = convertRTTLevel(RAT_lvl, rtt_lvl);
                        compose_info = "use RTTLvl:" + rtt_lvl + ";RATLvl:" + RAT_lvl;
                    }
                }
            }
            int delta_level = qos_Level - oldQosLevel;
            if (qos_Level == 0 || (delta_level <= 1 && delta_level >= -1)) {
                this.mQOS_Level = qos_Level;
            } else {
                this.mQOS_Level = (int) Math.round(((double) (qos_Level + oldQosLevel)) / 2.0d);
            }
            logi("QOS_level:" + this.mQOS_Level + "(" + qos_Level + ")," + compose_info);
            if (report_type == 1 && qos_Level <= 2) {
                now = System.currentTimeMillis();
                if (now - this.mLowLevelTimestamp <= 8000) {
                    logi("skip-low-Level:" + this.mQOS_Level + "(" + qos_Level + ")," + compose_info);
                    this.mQOS_Level = oldQosLevel;
                } else {
                    this.mLowLevelTimestamp = now;
                }
            }
        }
    }

    private void initMQDHandler() {
        HandlerThread thread = new HandlerThread("MobileQosDetect");
        thread.start();
        this.mHandler = new MobileQosDetectHandler(this, thread.getLooper(), null);
    }
}
