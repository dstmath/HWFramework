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
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
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
    private static String MOBILE_DATA_KEY = null;
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
    private static String SUB_SELECT_KEY = null;
    private static final int SUB_SWITCH_LATENCY = 10000;
    private static String TAG = null;
    private static final int TCP_INF_SKIP_DELAY = 30000;
    private static final int TCP_LOOP_DELAY = 10000;
    private static final int TYPE_MOBILE = 0;
    private static final int TYPE_WIFI = 1;
    public static final String WIFIPRO_CELLQOS_NOTIFY_ACTION = "wifipro.qos_notify.test_action";
    private INetworkQosCallBack mCallback;
    private Handler mCallerHandler;
    private ConnectivityManager mConnMgr;
    private ContentResolver mContentRsr;
    private Context mContext;
    private int mCurrDataSubID;
    private long mDataConnTimestamp;
    private boolean mDataSuspend;
    private int mEvents;
    private Handler mHandler;
    private int[] mHisSimStatus;
    private double[][] mIPQWeight;
    private int mIPQ_Level;
    private boolean mIsMonitoring;
    private int mIsRoveOutState;
    private long mLastAlarmTotBytes;
    private PhoneStateListener[] mListener;
    private long mLowLevelTimestamp;
    private boolean mMobileDataSwitch;
    private final int mPhoneNum;
    private int mQOS_Level;
    private double[] mRAT2G_Weight;
    private double[] mRAT3G_EVDOA_Weight;
    private double[] mRAT3G_EVDOB_Weight;
    private double[] mRAT3G_TDS_Weight;
    private double[] mRAT3G_UMTS_Weight;
    private double[] mRAT4G_Weight;
    private long mRO_StartRxBytes;
    private long mRO_StartTxBytes;
    private double[][] mRTTWeight;
    private TCPIpqRtt mRtt;
    private int[] mSIMArray;
    private int[] mSignalArray;
    private SignalStrength mSignalStrength;
    private int mSignal_Level;
    private int mSimStatus;
    private int mSpeed;
    private Runnable mSpeedUpdate;
    private int mSpeed_Level;
    private long mSubIdChangeTimsstamp;
    private TelephonyManager mTelephonyMgr;
    private long mTotalBytes;
    private TrafficMonitor mTraffic;
    private WifiProUIDisplayManager mUIManager;
    private WifiProCHRManager mWiFiCHRManager;
    private WifiProStatisticsManager mWifiProStatisticsManager;
    private int readDataPeriod;

    /* renamed from: com.android.server.wifi.wifipro.MobileQosDetector.2 */
    class AnonymousClass2 extends ContentObserver {
        AnonymousClass2(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            MobileQosDetector.this.mMobileDataSwitch = MobileQosDetector.this.getDataStatus();
            MobileQosDetector.this.requestComputeQos(0);
            MobileQosDetector.this.logi("MobileData switch:" + MobileQosDetector.this.mMobileDataSwitch);
        }
    }

    /* renamed from: com.android.server.wifi.wifipro.MobileQosDetector.3 */
    class AnonymousClass3 extends ContentObserver {
        AnonymousClass3(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            int oldId = MobileQosDetector.this.mCurrDataSubID;
            MobileQosDetector.this.mCurrDataSubID = Global.getInt(MobileQosDetector.this.mContentRsr, MobileQosDetector.SUB_SELECT_KEY, MobileQosDetector.TYPE_MOBILE);
            MobileQosDetector.this.logi("new subID:" + MobileQosDetector.this.mCurrDataSubID + " is Selected,oldId:" + oldId);
            if (oldId != MobileQosDetector.this.mCurrDataSubID) {
                int subid = MobileQosDetector.this.mCurrDataSubID;
                if (subid < 0 || subid >= MobileQosDetector.this.mPhoneNum) {
                    subid = MobileQosDetector.TYPE_MOBILE;
                }
                MobileQosDetector.this.mSignal_Level = MobileQosDetector.this.mSignalArray[subid];
                MobileQosDetector.this.mSpeed_Level = MobileQosDetector.MOBILE_INET_QOS_LEVEL_UNKNOWN;
                MobileQosDetector.this.mIPQ_Level = MobileQosDetector.MOBILE_INET_QOS_LEVEL_UNKNOWN;
                MobileQosDetector.this.mRtt.reset();
                MobileQosDetector.this.mTraffic.reset();
                MobileQosDetector.this.mSimStatus = MobileQosDetector.this.mTelephonyMgr.getSimState(MobileQosDetector.this.mCurrDataSubID);
                MobileQosDetector.this.logi("SIM status:" + MobileQosDetector.this.mSimStatus);
                MobileQosDetector.this.mSubIdChangeTimsstamp = System.currentTimeMillis();
                MobileQosDetector.this.requestComputeQos(10000);
            }
        }
    }

    /* renamed from: com.android.server.wifi.wifipro.MobileQosDetector.4 */
    class AnonymousClass4 extends DualPhoneStateListener {
        int mSubId;

        AnonymousClass4(int $anonymous0) {
            super($anonymous0);
            this.mSubId = DualPhoneStateListener.getSubscription(this);
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            Log.i(MobileQosDetector.TAG, "onSignal: mSubId=" + this.mSubId + ",currDataSubID=" + MobileQosDetector.this.mCurrDataSubID);
            MobileQosDetector.this.mSignalStrength = new SignalStrength(signalStrength);
            int signal_level = MobileQosDetector.this.convertSignalLevel(MobileQosDetector.this.mSignalStrength);
            int subid = (this.mSubId < 0 || this.mSubId >= MobileQosDetector.this.mPhoneNum) ? MobileQosDetector.TYPE_MOBILE : this.mSubId;
            MobileQosDetector.this.mSignalArray[subid] = signal_level;
            if (this.mSubId == MobileQosDetector.this.mCurrDataSubID && MobileQosDetector.this.mSignal_Level != MobileQosDetector.this.mSignalArray[subid]) {
                MobileQosDetector.this.mSignal_Level = MobileQosDetector.this.mSignalArray[subid];
                MobileQosDetector.this.requestComputeQos(0);
            }
        }

        public void onDataConnectionStateChanged(int state, int networkType) {
            Log.i(MobileQosDetector.TAG, "onDataConn: mSubId=" + this.mSubId + ",state=" + state + ",networkType=" + networkType);
            if (state == MobileQosDetector.ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY) {
                MobileQosDetector.this.mCurrDataSubID = this.mSubId;
                MobileQosDetector.this.mSimStatus = MobileQosDetector.this.mTelephonyMgr.getSimState(MobileQosDetector.this.mCurrDataSubID);
                Log.i(MobileQosDetector.TAG, "onDataConn: mSubId=" + this.mSubId + ",mSimStatus=" + MobileQosDetector.this.mSimStatus);
                MobileQosDetector.this.requestComputeQos(0);
                if (MobileQosDetector.this.mHandler.hasMessages(MobileQosDetector.MSG_QUERY_TCP_INFO)) {
                    Log.i(MobileQosDetector.TAG, "has pending msg(QUERY_TCP_INFO),skip!");
                } else {
                    MobileQosDetector.this.mHandler.sendEmptyMessageDelayed(MobileQosDetector.MSG_QUERY_TCP_INFO, 30000);
                }
                MobileQosDetector.this.mDataConnTimestamp = System.currentTimeMillis();
                MobileQosDetector.this.requestComputeQos(30000);
            }
            if (MobileQosDetector.this.mCurrDataSubID == this.mSubId) {
                boolean suspend = state == MobileQosDetector.ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY ? MobileQosDetector.DBG : MobileQosDetector.DEBUG_MODE;
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
                MobileQosDetector.this.mSIMArray[this.mSubId] = MobileQosDetector.SIM_STATE_READY;
                MobileQosDetector.this.mHisSimStatus[this.mSubId] = MobileQosDetector.SIM_STATE_READY;
                if (MobileQosDetector.this.mSimStatus != MobileQosDetector.SIM_STATE_READY && this.mSubId == MobileQosDetector.this.mCurrDataSubID) {
                    MobileQosDetector.this.mSimStatus = MobileQosDetector.SIM_STATE_READY;
                    MobileQosDetector.this.requestComputeQos(0);
                }
                String plmn = MobileQosDetector.this.mTelephonyMgr.getNetworkOperator();
                if (plmn == null || plmn.length() < MobileQosDetector.ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY) {
                    Log.e(MobileQosDetector.TAG, "MCC invalid");
                    return;
                }
                String mcc = plmn.substring(MobileQosDetector.TYPE_MOBILE, MobileQosDetector.ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY);
                try {
                    int mcc_code = Integer.parseInt(mcc);
                    if (mcc_code > 0) {
                        Message.obtain(MobileQosDetector.this.mCallerHandler, MobileQosDetector.SIM_STATE_READY, mcc_code, MobileQosDetector.TYPE_MOBILE).sendToTarget();
                        return;
                    }
                    return;
                } catch (NumberFormatException ne) {
                    Log.e(MobileQosDetector.TAG, "getMCC error:" + mcc);
                    ne.printStackTrace();
                    return;
                }
            }
            MobileQosDetector.this.mSIMArray[this.mSubId] = MobileQosDetector.TYPE_MOBILE;
            if (this.mSubId == MobileQosDetector.this.mCurrDataSubID) {
                MobileQosDetector.this.mSimStatus = MobileQosDetector.TYPE_MOBILE;
                MobileQosDetector.this.requestComputeQos(0);
            }
        }
    }

    class MobileQosDetectHandler extends Handler {
        private MobileQosDetectHandler(Looper looper) {
            super(looper);
            MobileQosDetector.this.logi("new MobileQosDetectHandler");
        }

        public void handleMessage(Message msg) {
            int conntype = MobileQosDetector.MOBILE_INET_QOS_LEVEL_UNKNOWN;
            switch (msg.what) {
                case MobileQosDetector.MSG_COMPOSE_MQOS /*101*/:
                    int old_level = MobileQosDetector.this.mQOS_Level;
                    MobileQosDetector.this.composeQOSLevel(MobileQosDetector.MOBILE_INET_QOS_LEVEL_UNKNOWN, MobileQosDetector.TYPE_WIFI);
                    if (old_level != MobileQosDetector.this.mQOS_Level && MobileQosDetector.this.mIsMonitoring) {
                        MobileQosDetector.this.reportQos(MobileQosDetector.TYPE_WIFI, MobileQosDetector.this.mQOS_Level);
                    }
                    MobileQosDetector.this.mHandler.removeMessages(MobileQosDetector.MSG_COMPOSE_MQOS);
                case MobileQosDetector.MSG_IPQOS_EXPIRED /*105*/:
                    MobileQosDetector.this.mIPQ_Level = MobileQosDetector.MOBILE_INET_QOS_LEVEL_UNKNOWN;
                    MobileQosDetector.this.mHandler.removeMessages(MobileQosDetector.MSG_IPQOS_EXPIRED);
                case MobileQosDetector.MSG_QUERY_MQOS /*106*/:
                    Log.i(MobileQosDetector.TAG, "queryNetworkQos enter");
                    MobileQosDetector.this.composeQOSLevel(MobileQosDetector.MOBILE_INET_QOS_LEVEL_UNKNOWN, MobileQosDetector.TYPE_MOBILE);
                    MobileQosDetector.this.reportQos(MobileQosDetector.TYPE_MOBILE, MobileQosDetector.this.mQOS_Level);
                    Log.i(MobileQosDetector.TAG, "queryNetworkQos exit");
                case MobileQosDetector.MSG_QUERY_TCP_INFO /*107*/:
                    Log.i(MobileQosDetector.TAG, "MSG_QUERY_TCP_INFO called,count:" + msg.arg2);
                    MobileQosDetector.this.queryRtt();
                    NetworkInfo netinfo = MobileQosDetector.this.mConnMgr.getActiveNetworkInfo();
                    if (netinfo != null) {
                        conntype = netinfo.getType();
                    }
                    if (conntype == 0) {
                        if (MobileQosDetector.this.mHandler.hasMessages(MobileQosDetector.MSG_QUERY_TCP_INFO)) {
                            Log.i(MobileQosDetector.TAG, "MSG_QUERY_TCP_INFO loop called,skip:" + msg.arg2);
                        } else {
                            MobileQosDetector.this.mHandler.sendMessageDelayed(MobileQosDetector.this.mHandler.obtainMessage(MobileQosDetector.MSG_QUERY_TCP_INFO, MobileQosDetector.TYPE_MOBILE, msg.arg2 + MobileQosDetector.TYPE_WIFI), 10000);
                        }
                        MobileQosDetector.this.periodCheckHighDataFlow();
                        return;
                    }
                    MobileQosDetector.this.mHandler.removeMessages(MobileQosDetector.MSG_QUERY_TCP_INFO);
                    Log.i(MobileQosDetector.TAG, "stop query RTT!");
                case MobileQosDetector.MSG_RO_STATE_CHANGE /*108*/:
                    int roState = msg.arg1;
                    if (MobileQosDetector.this.mTraffic == null) {
                        return;
                    }
                    if (MobileQosDetector.TYPE_WIFI == roState) {
                        MobileQosDetector.this.mIsRoveOutState = MobileQosDetector.TYPE_WIFI;
                        MobileQosDetector.this.mRO_StartRxBytes = MobileQosDetector.this.mTraffic.getMobileRxBytes();
                        MobileQosDetector.this.mRO_StartTxBytes = MobileQosDetector.this.mTraffic.getMobileTxBytes();
                        MobileQosDetector.this.mTotalBytes = 0;
                        MobileQosDetector.this.logi("rove out start, first read rxBytes=" + MobileQosDetector.this.mRO_StartRxBytes + ", txBytes=" + MobileQosDetector.this.mRO_StartTxBytes);
                    } else if (roState == 0) {
                        MobileQosDetector.this.mIsRoveOutState = MobileQosDetector.TYPE_MOBILE;
                        if (MobileQosDetector.this.mUIManager != null) {
                            MobileQosDetector.this.mUIManager.cleanUpheadNotificationHMD();
                        }
                        MobileQosDetector.this.logi("rove out end.");
                    }
                default:
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
            this.rtt = MobileQosDetector.TYPE_MOBILE;
            this.rtt_pkts = MobileQosDetector.TYPE_MOBILE;
            this.rtt_when = MobileQosDetector.TYPE_MOBILE;
            this.congestion = MobileQosDetector.TYPE_MOBILE;
            this.cong_when = MobileQosDetector.TYPE_MOBILE;
            this.timestamp = 0;
            this.type = MobileQosDetector.TYPE_MOBILE;
            this.level = MobileQosDetector.TYPE_MOBILE;
            this.tcp_tx_pkts = MobileQosDetector.TYPE_MOBILE;
            this.tcp_rx_pkts = MobileQosDetector.TYPE_MOBILE;
            this.tcp_retrans_pkts = MobileQosDetector.TYPE_MOBILE;
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
            if (this.rtt < RTT_FINE_5) {
                return RTT_VALID_PKTS;
            }
            if (this.rtt < RTT_GOOD_4) {
                return MobileQosDetector.MOBILE_INET_QOS_LEVEL_4_GOOD;
            }
            if (this.rtt < RTT_MID_3) {
                return MobileQosDetector.ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY;
            }
            if (this.rtt < RTT_POOR_2) {
                return MobileQosDetector.ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY;
            }
            if (this.rtt < RTT_VERY_POOR_1) {
                return MobileQosDetector.TYPE_WIFI;
            }
            return MobileQosDetector.TYPE_WIFI;
        }

        public String toString() {
            return "rtt=" + this.rtt + ",rtt_pkts=" + this.rtt_pkts + ",rtt_when=" + this.rtt_when + ",congestion=" + this.congestion + ",cong_when=" + this.cong_when + ",type=" + this.type + ",lvl=" + this.level + ",ts=" + this.timestamp;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.MobileQosDetector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.MobileQosDetector.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.MobileQosDetector.<clinit>():void");
    }

    public MobileQosDetector(Context ctxt, INetworkQosCallBack cb, Handler hdlr, WifiProUIDisplayManager wifiProUIDisplayManager) {
        this.mTelephonyMgr = null;
        this.mConnMgr = null;
        this.mContext = null;
        this.mCallback = null;
        this.mSignalStrength = null;
        this.mTraffic = null;
        this.mCallerHandler = null;
        this.mIsMonitoring = DEBUG_MODE;
        this.mMobileDataSwitch = DEBUG_MODE;
        this.mSimStatus = TYPE_MOBILE;
        this.mCurrDataSubID = TYPE_MOBILE;
        this.mSpeed_Level = MOBILE_INET_QOS_LEVEL_UNKNOWN;
        this.mSignal_Level = MOBILE_INET_QOS_LEVEL_UNKNOWN;
        this.mIPQ_Level = MOBILE_INET_QOS_LEVEL_UNKNOWN;
        this.mQOS_Level = MOBILE_INET_QOS_LEVEL_UNKNOWN;
        this.mSpeed = MOBILE_INET_QOS_LEVEL_UNKNOWN;
        this.mRtt = new TCPIpqRtt();
        this.mListener = null;
        this.mSignalArray = null;
        this.mSIMArray = null;
        this.mHisSimStatus = null;
        this.mSubIdChangeTimsstamp = System.currentTimeMillis();
        this.mDataConnTimestamp = System.currentTimeMillis();
        this.mLowLevelTimestamp = System.currentTimeMillis();
        this.mIsRoveOutState = TYPE_MOBILE;
        this.readDataPeriod = TYPE_MOBILE;
        this.mRO_StartRxBytes = 0;
        this.mRO_StartTxBytes = 0;
        this.mTotalBytes = 0;
        this.mLastAlarmTotBytes = 0;
        double[][] dArr = new double[RECENT_TIME_S][];
        dArr[TYPE_MOBILE] = new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d};
        dArr[TYPE_WIFI] = new double[]{0.0d, TrafficMonitor.LOW_RX_TX_RATIO, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, TrafficMonitor.LOW_RX_TX_RATIO, 0.0d};
        dArr[ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY] = new double[]{0.0d, TrafficMonitor.LOW_RX_TX_RATIO, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, TrafficMonitor.LOW_RX_TX_RATIO, 0.0d};
        dArr[ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY] = new double[]{0.0d, TrafficMonitor.LOW_RX_TX_RATIO, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, TrafficMonitor.LOW_RX_TX_RATIO, 0.0d};
        dArr[MOBILE_INET_QOS_LEVEL_4_GOOD] = new double[]{0.0d, TrafficMonitor.LOW_RX_TX_RATIO, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, TrafficMonitor.LOW_RX_TX_RATIO, 0.0d};
        dArr[SIM_STATE_READY] = new double[]{0.0d, TrafficMonitor.LOW_RX_TX_RATIO, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, TrafficMonitor.LOW_RX_TX_RATIO, 0.0d};
        this.mIPQWeight = dArr;
        dArr = new double[RECENT_TIME_S][];
        dArr[TYPE_MOBILE] = new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d};
        dArr[TYPE_WIFI] = new double[]{0.0d, 0.7d, 0.7d, TrafficMonitor.LOW_RX_TX_RATIO, TrafficMonitor.LOW_RX_TX_RATIO, TrafficMonitor.LOW_RX_TX_RATIO};
        dArr[ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY] = new double[]{0.0d, 0.7d, 0.7d, TrafficMonitor.LOW_RX_TX_RATIO, TrafficMonitor.LOW_RX_TX_RATIO, TrafficMonitor.LOW_RX_TX_RATIO};
        dArr[ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY] = new double[]{0.0d, 0.7d, 0.7d, TrafficMonitor.LOW_RX_TX_RATIO, TrafficMonitor.LOW_RX_TX_RATIO, TrafficMonitor.LOW_RX_TX_RATIO};
        dArr[MOBILE_INET_QOS_LEVEL_4_GOOD] = new double[]{0.0d, 0.7d, 0.7d, TrafficMonitor.LOW_RX_TX_RATIO, TrafficMonitor.LOW_RX_TX_RATIO, TrafficMonitor.LOW_RX_TX_RATIO};
        dArr[SIM_STATE_READY] = new double[]{0.0d, 0.7d, 0.7d, TrafficMonitor.LOW_RX_TX_RATIO, TrafficMonitor.LOW_RX_TX_RATIO, TrafficMonitor.LOW_RX_TX_RATIO};
        this.mRTTWeight = dArr;
        this.mRAT2G_Weight = new double[]{0.0d, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE};
        this.mRAT3G_EVDOA_Weight = new double[]{0.0d, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE};
        this.mRAT3G_EVDOB_Weight = new double[]{0.0d, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, 0.6d, 0.6d, 0.6d};
        this.mRAT3G_TDS_Weight = new double[]{0.0d, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, HwCHRWifiSpeedBaseChecker.RCV_SND_RATE, 0.6d, 0.6d, 0.6d};
        this.mRAT3G_UMTS_Weight = new double[]{0.0d, 1.0d, 1.0d, 1.0d, 1.0d, 1.0d};
        this.mRAT4G_Weight = new double[]{0.0d, 1.0d, 1.0d, 1.0d, 1.0d, 1.0d};
        this.mDataSuspend = DEBUG_MODE;
        this.mEvents = 321;
        this.mSpeedUpdate = new Runnable() {
            public void run() {
                int old_speed = MobileQosDetector.this.mSpeed_Level;
                MobileQosDetector.this.mSpeed = MobileQosDetector.this.mTraffic.getRxByteSpeed(MobileQosDetector.TYPE_WIFI);
                MobileQosDetector.this.mSpeed_Level = MobileQosDetector.this.mTraffic.transform(MobileQosDetector.this.mSpeed);
                if (old_speed != MobileQosDetector.this.mSpeed_Level) {
                    MobileQosDetector.this.requestComputeQos(0);
                }
                MobileQosDetector.this.logi("speed:oldlevel:" + old_speed + ",new level:" + MobileQosDetector.this.mSpeed_Level + ",newSpeed(Bps):" + MobileQosDetector.this.mSpeed);
            }
        };
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
        for (int i = TYPE_MOBILE; i < this.mPhoneNum; i += TYPE_WIFI) {
            this.mListener[i] = getPSListener(i);
            this.mTelephonyMgr.listen(this.mListener[i], this.mEvents);
            this.mSignalArray[i] = MOBILE_INET_QOS_LEVEL_UNKNOWN;
            this.mSIMArray[i] = this.mTelephonyMgr.getSimState(i);
            this.mHisSimStatus[i] = this.mSIMArray[i];
        }
        logi("PhoneNum=" + this.mPhoneNum);
        initObserver();
        this.mCurrDataSubID = SubscriptionManager.getDefaultDataSubscriptionId();
        if (this.mCurrDataSubID < 0) {
            int subid = TYPE_MOBILE;
            for (int k = TYPE_MOBILE; k < this.mPhoneNum; k += TYPE_WIFI) {
                if (this.mSIMArray[k] == SIM_STATE_READY) {
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
            this.mTraffic.enableMonitor(DBG, TYPE_MOBILE);
            this.mTraffic.setExpireTime(SPEED_EXPIRE_LATENCY);
        } else {
            this.mTraffic.enableMonitor(DEBUG_MODE, TYPE_MOBILE);
        }
        this.mIsMonitoring = enable;
    }

    public void queryNetworkQos() {
        Log.i(TAG, "queryNetworkQos start");
        queryRtt();
        this.mHandler.sendEmptyMessageDelayed(MSG_QUERY_MQOS, 5);
    }

    public String queryNetworkId() {
        int type = this.mTelephonyMgr.getPhoneType();
        String plmn = this.mTelephonyMgr.getNetworkOperator();
        int cellid = TYPE_MOBILE;
        switch (type) {
            case TYPE_WIFI /*1*/:
                GsmCellLocation gsmCellLocation = (GsmCellLocation) this.mTelephonyMgr.getCellLocation();
                if (gsmCellLocation != null) {
                    cellid = gsmCellLocation.getCid();
                    break;
                }
                break;
            case ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY /*2*/:
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
        int tcp_rtt = len > 0 ? ipqos[TYPE_MOBILE] : TYPE_MOBILE;
        int tcp_rtt_pkts = len > TYPE_WIFI ? ipqos[TYPE_WIFI] : TYPE_MOBILE;
        int tcp_rtt_when = len > ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY ? ipqos[ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY] : TYPE_MOBILE;
        int tcp_congestion = len > ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY ? ipqos[ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY] : TYPE_MOBILE;
        int tcp_cong_when = len > MOBILE_INET_QOS_LEVEL_4_GOOD ? ipqos[MOBILE_INET_QOS_LEVEL_4_GOOD] : TYPE_MOBILE;
        int tcp_level = len > SIM_STATE_READY ? ipqos[SIM_STATE_READY] : MOBILE_INET_QOS_LEVEL_UNKNOWN;
        long ts = System.currentTimeMillis() - ((long) tcp_rtt_when);
        int tcp_tx_pkts = len > RECENT_TIME_S ? ipqos[RECENT_TIME_S] : TYPE_MOBILE;
        int tcp_rx_pkts = len > 7 ? ipqos[7] : TYPE_MOBILE;
        int tcp_retran_pkts = len > 8 ? ipqos[8] : TYPE_MOBILE;
        if (type == TYPE_WIFI) {
            this.mIPQ_Level = tcp_level;
            this.mHandler.removeMessages(MSG_IPQOS_EXPIRED);
            this.mHandler.sendEmptyMessageDelayed(MSG_IPQOS_EXPIRED, 5000);
        }
        this.mRtt.setMember(tcp_rtt, tcp_rtt_pkts, tcp_rtt_when, tcp_congestion, tcp_cong_when, type, tcp_level, ts, tcp_tx_pkts, tcp_rx_pkts, tcp_retran_pkts);
        requestComputeQos(0);
        Log.i(TAG, "rtt=" + tcp_rtt + ",tcp_tx_pkts=" + tcp_tx_pkts + ",tcp_rx_pkts=" + tcp_rx_pkts);
    }

    private void queryRtt() {
        this.mCallerHandler.sendEmptyMessage(MOBILE_INET_QOS_LEVEL_4_GOOD);
    }

    private void requestComputeQos(long delay_ms) {
        this.mHandler.sendEmptyMessageDelayed(MSG_COMPOSE_MQOS, delay_ms);
    }

    private void reportQos(int report_type, int qos_level) {
        Log.i(TAG, "entry report: qos lvl=" + qos_level + ",report_type:" + report_type);
        if (this.mCallback == null) {
            Log.e(TAG, "mCallback is null!, report_type=" + report_type);
            return;
        }
        if (report_type == 0) {
            Log.i(TAG, "before query report: qos lvl=" + qos_level);
            this.mCallback.onNetworkDetectionResult(TYPE_MOBILE, qos_level);
        } else if (report_type == TYPE_WIFI) {
            this.mCallback.onNetworkQosChange(TYPE_MOBILE, qos_level);
        }
        Log.i(TAG, "after report: qos lvl=" + qos_level + ",svc working:" + this.mIsMonitoring);
    }

    private void initObserver() {
        this.mContentRsr = this.mContext.getContentResolver();
        ContentObserver dataSwitchObserver = new AnonymousClass2(this.mHandler);
        ContentObserver subSelectObserver = new AnonymousClass3(this.mHandler);
        this.mContentRsr.registerContentObserver(Global.getUriFor(MOBILE_DATA_KEY), DEBUG_MODE, dataSwitchObserver);
        this.mContentRsr.registerContentObserver(Global.getUriFor(SUB_SELECT_KEY), DEBUG_MODE, subSelectObserver);
        this.mMobileDataSwitch = getDataStatus();
    }

    private boolean getDataStatus() {
        if (Global.getInt(this.mContentRsr, MOBILE_DATA_KEY, TYPE_MOBILE) == TYPE_WIFI) {
            return DBG;
        }
        return DEBUG_MODE;
    }

    private PhoneStateListener getPSListener(int subscription) {
        return new AnonymousClass4(subscription);
    }

    private int convertSignalLevel(SignalStrength ss) {
        int signal_level = TYPE_MOBILE;
        if (ss == null) {
            return TYPE_MOBILE;
        }
        int level = ss.getLevel();
        switch (level) {
            case TYPE_MOBILE /*0*/:
                signal_level = TYPE_WIFI;
                break;
            case TYPE_WIFI /*1*/:
                signal_level = ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY;
                break;
            case ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY /*2*/:
                signal_level = ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY;
                break;
            case ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY /*3*/:
                signal_level = MOBILE_INET_QOS_LEVEL_4_GOOD;
                break;
            case MOBILE_INET_QOS_LEVEL_4_GOOD /*4*/:
                signal_level = SIM_STATE_READY;
                break;
            default:
                if (level > MOBILE_INET_QOS_LEVEL_4_GOOD) {
                    signal_level = SIM_STATE_READY;
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
            signal_level = TYPE_MOBILE;
        }
        int ipq_level = ipq_lvl > 0 ? ipq_lvl : TYPE_MOBILE;
        double w_ipqos = this.mIPQWeight[signal_level][ipq_level];
        return Math.round((float) ((((double) ipq_level) * w_ipqos) + (((double) signal_level) * (1.0d - w_ipqos))));
    }

    private int convertRTTLevel(int signal_lvl, int rtt_lvl) {
        int signal_level;
        if (signal_lvl >= 0) {
            signal_level = signal_lvl;
        } else {
            signal_level = TYPE_MOBILE;
        }
        int rtt_level = rtt_lvl > 0 ? rtt_lvl : TYPE_MOBILE;
        double w_ipqos = this.mRTTWeight[signal_level][rtt_level];
        return Math.round((float) ((((double) rtt_level) * w_ipqos) + (((double) signal_level) * (1.0d - w_ipqos))));
    }

    private int convertRATLevel(int signal_Level, int net_type) {
        int sig_level = TYPE_MOBILE;
        if (signal_Level >= 0) {
            sig_level = signal_Level;
        }
        int new_level = sig_level;
        TelephonyManager telephonyManager = this.mTelephonyMgr;
        int RAT_class = TelephonyManager.getNetworkClass(net_type);
        if (TYPE_WIFI == RAT_class) {
            new_level = (int) Math.floor((double) ((float) (((double) sig_level) * this.mRAT2G_Weight[sig_level])));
        } else if (ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY == RAT_class) {
            switch (net_type) {
                case ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY /*3*/:
                case MessageUtil.MSG_WIFI_CONFIG_CHANGED /*8*/:
                case MessageUtil.MSG_WIFI_HANDLE_DISABLE /*9*/:
                case ALARM_DATA_SIZE_LEVEL_1_DISPLAY /*10*/:
                case MessageUtil.MSG_WIFI_P2P_DISCONNECTED /*15*/:
                    new_level = Math.round((float) (((double) sig_level) * this.mRAT3G_UMTS_Weight[sig_level]));
                    break;
                case SIM_STATE_READY /*5*/:
                case RECENT_TIME_S /*6*/:
                    new_level = Math.round((float) (((double) sig_level) * this.mRAT3G_EVDOA_Weight[sig_level]));
                    break;
                case MessageUtil.MSG_WIFI_INTERNET_DISCONNECTED /*12*/:
                    new_level = Math.round((float) (((double) sig_level) * this.mRAT3G_EVDOB_Weight[sig_level]));
                    break;
                default:
                    new_level = Math.round((float) (((double) sig_level) * this.mRAT3G_TDS_Weight[sig_level]));
                    break;
            }
        } else if (ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY == RAT_class) {
            new_level = Math.round((float) (((double) sig_level) * this.mRAT4G_Weight[sig_level]));
        } else {
            Log.e(TAG, "unkown RAT!");
        }
        if (new_level < TYPE_WIFI) {
            return TYPE_WIFI;
        }
        return new_level;
    }

    private String getRATName(int net_type) {
        String rat_name = "unknownRAT";
        TelephonyManager telephonyManager = this.mTelephonyMgr;
        switch (TelephonyManager.getNetworkClass(net_type)) {
            case TYPE_WIFI /*1*/:
                return "2G";
            case ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY /*2*/:
                switch (net_type) {
                    case ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY /*3*/:
                    case MessageUtil.MSG_WIFI_CONFIG_CHANGED /*8*/:
                    case MessageUtil.MSG_WIFI_HANDLE_DISABLE /*9*/:
                    case ALARM_DATA_SIZE_LEVEL_1_DISPLAY /*10*/:
                    case MessageUtil.MSG_WIFI_P2P_DISCONNECTED /*15*/:
                        return "3G-UMTS";
                    case SIM_STATE_READY /*5*/:
                    case RECENT_TIME_S /*6*/:
                    case MessageUtil.MSG_WIFI_INTERNET_DISCONNECTED /*12*/:
                        return "3G-EVDO";
                    default:
                        return "3G-TD";
                }
            case ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY /*3*/:
                return "4G";
            default:
                return rat_name;
        }
    }

    private int getRATClass(int net_type) {
        TelephonyManager telephonyManager = this.mTelephonyMgr;
        switch (TelephonyManager.getNetworkClass(net_type)) {
            case TYPE_WIFI /*1*/:
                return TYPE_WIFI;
            case ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY /*2*/:
                switch (net_type) {
                    case ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY /*3*/:
                    case MessageUtil.MSG_WIFI_CONFIG_CHANGED /*8*/:
                    case MessageUtil.MSG_WIFI_HANDLE_DISABLE /*9*/:
                    case ALARM_DATA_SIZE_LEVEL_1_DISPLAY /*10*/:
                    case MessageUtil.MSG_WIFI_P2P_DISCONNECTED /*15*/:
                        return MOBILE_INET_QOS_LEVEL_4_GOOD;
                    case SIM_STATE_READY /*5*/:
                    case RECENT_TIME_S /*6*/:
                    case MessageUtil.MSG_WIFI_INTERNET_DISCONNECTED /*12*/:
                        return ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY;
                    default:
                        return ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY;
                }
            case ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY /*3*/:
                return SIM_STATE_READY;
            default:
                return TYPE_MOBILE;
        }
    }

    public synchronized int onGetMobileSignalLevel() {
        return this.mSignal_Level;
    }

    public synchronized int onGetMobileRATType() {
        if (this.mTelephonyMgr == null) {
            return TYPE_MOBILE;
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
        sendMsg(MSG_RO_STATE_CHANGE, roveOutState, TYPE_MOBILE);
    }

    private void periodCheckHighDataFlow() {
        if (this.mUIManager == null) {
            Log.e(TAG, "mUIManager null error.");
            return;
        }
        if (this.mIsRoveOutState != 0) {
            int i = this.readDataPeriod + TYPE_WIFI;
            this.readDataPeriod = i;
            if (i >= ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY) {
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
                    if (TYPE_WIFI == this.mIsRoveOutState && this.mTotalBytes >= ALARM_DATA_UNIT_10M) {
                        this.mUIManager.showHMDNotification(ALARM_DATA_SIZE_LEVEL_1_DISPLAY, DBG);
                        this.mIsRoveOutState = ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY;
                        this.mLastAlarmTotBytes = this.mTotalBytes;
                        logi(" high data flow >= 10M, set first notify.");
                        this.mWifiProStatisticsManager.increaseHMDNotifyCount(TYPE_WIFI);
                    } else if (ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY == this.mIsRoveOutState) {
                        if (this.mTotalBytes >= ALARM_DATA_SIZE_LEVEL_2) {
                            this.mUIManager.showHMDNotification(ALARM_DATA_SIZE_LEVEL_2_DISPLAY, DBG);
                            this.mIsRoveOutState = ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY;
                            logi(" high data flow >= 50M, show 50M notify.");
                            this.mLastAlarmTotBytes = this.mTotalBytes;
                            this.mWifiProStatisticsManager.increaseHMDNotifyCount(ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY);
                        } else if (this.mTotalBytes - this.mLastAlarmTotBytes > ALARM_DATA_UNIT_10M) {
                            dataSize = ((int) (this.mTotalBytes / ALARM_DATA_UNIT_10M)) * ALARM_DATA_SIZE_LEVEL_1_DISPLAY;
                            this.mUIManager.showHMDNotification(dataSize, DEBUG_MODE);
                            logi(" high data flow > " + dataSize + "M, update notify display.");
                            this.mLastAlarmTotBytes = this.mTotalBytes;
                        }
                    } else if (ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY == this.mIsRoveOutState && this.mTotalBytes - this.mLastAlarmTotBytes > ALARM_DATA_UNIT_10M) {
                        dataSize = ((int) (this.mTotalBytes / ALARM_DATA_UNIT_10M)) * ALARM_DATA_SIZE_LEVEL_1_DISPLAY;
                        this.mUIManager.showHMDNotification(dataSize, DEBUG_MODE);
                        logi(" high data flow > " + dataSize + "M, update notify display.");
                        this.mLastAlarmTotBytes = this.mTotalBytes;
                    }
                    this.readDataPeriod = TYPE_MOBILE;
                }
            }
        }
    }

    private void logi(String msg) {
        Log.i(TAG, msg);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void composeQOSLevel(int signal_level, int report_type) {
        boolean sim_ready;
        int conntype = MOBILE_INET_QOS_LEVEL_UNKNOWN;
        NetworkInfo netinfo = this.mConnMgr.getActiveNetworkInfo();
        String compose_info = "unknown";
        int oldQosLevel = this.mQOS_Level;
        int qos_Level = this.mQOS_Level;
        if (netinfo != null) {
            conntype = netinfo.getType();
        }
        if (signal_level < 0) {
            if (this.mSignal_Level >= 0) {
                signal_level = this.mSignal_Level;
            } else {
                signal_level = TYPE_MOBILE;
            }
        }
        int i = this.mSimStatus;
        if (r0 != SIM_STATE_READY) {
            if (this.mCurrDataSubID >= 0 && this.mCurrDataSubID < this.mPhoneNum) {
                if (this.mSIMArray[this.mCurrDataSubID] == SIM_STATE_READY) {
                    sim_ready = DBG;
                }
            }
            sim_ready = DEBUG_MODE;
        } else {
            sim_ready = DBG;
        }
        if (!this.mMobileDataSwitch) {
            qos_Level = TYPE_MOBILE;
            compose_info = "DataDisable;use level-0";
        } else if (this.mDataSuspend) {
            qos_Level = TYPE_MOBILE;
            compose_info = "DataSuspend;use level-0";
        } else {
            if (this.mTelephonyMgr.isNetworkRoaming(this.mCurrDataSubID)) {
                qos_Level = TYPE_MOBILE;
                compose_info = "dis-Roaming;use level-0";
            } else if (conntype == TYPE_WIFI) {
                if (!sim_ready) {
                    int simState = this.mTelephonyMgr.getSimState(this.mCurrDataSubID);
                    ServiceState serviceState = this.mTelephonyMgr.getServiceStateForSubscriber(this.mCurrDataSubID);
                    if (simState == SIM_STATE_READY && this.mPhoneNum >= 0) {
                        i = this.mPhoneNum;
                        if (r0 <= ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY && serviceState != null && serviceState.getState() == 0) {
                            int i2 = TYPE_MOBILE;
                            while (true) {
                                i = this.mPhoneNum;
                                if (i2 >= r0) {
                                    break;
                                }
                                this.mTelephonyMgr.listen(this.mListener[i2], this.mEvents);
                                this.mSignalArray[i2] = MOBILE_INET_QOS_LEVEL_UNKNOWN;
                                this.mSIMArray[i2] = this.mTelephonyMgr.getSimState(i2);
                                this.mHisSimStatus[i2] = this.mSIMArray[i2];
                                i2 += TYPE_WIFI;
                            }
                            this.mSimStatus = SIM_STATE_READY;
                            sim_ready = DBG;
                            logi("SIM status change, sim_ready = " + DBG);
                        }
                    }
                }
                if (sim_ready) {
                    net_type = this.mTelephonyMgr.getNetworkType();
                    qos_Level = convertRATLevel(signal_level, net_type);
                    compose_info = "wifi-link;use signalLvl:" + signal_level + ";RAT:" + getRATName(net_type);
                } else {
                    qos_Level = TYPE_MOBILE;
                    compose_info = "wifi-link;SIM error;use level-0";
                    if (this.mHisSimStatus[this.mCurrDataSubID] == SIM_STATE_READY) {
                    }
                }
            } else if (conntype == 0) {
                i = this.mSpeed_Level;
                if (r0 != SIM_STATE_READY) {
                    i = this.mSpeed_Level;
                    if (r0 != MOBILE_INET_QOS_LEVEL_4_GOOD) {
                        net_type = this.mTelephonyMgr.getNetworkType();
                        int RAT_lvl = convertRATLevel(signal_level, net_type);
                        long now = System.currentTimeMillis();
                        long rtt_escape = now - this.mRtt.timestamp;
                        boolean tcp_is_valid = now - this.mDataConnTimestamp >= 30000 ? DBG : DEBUG_MODE;
                        boolean result = DEBUG_MODE;
                        i = this.mIPQ_Level;
                        if (r0 != TYPE_WIFI) {
                            i = this.mIPQ_Level;
                        }
                        if (RAT_lvl < ROVE_OUT_TO_MOBILE_MORE_THAN_50M_NOTIFY && tcp_is_valid) {
                            qos_Level = convertIPQLevel(RAT_lvl, this.mIPQ_Level);
                            compose_info = "use IPQLvl:" + this.mIPQ_Level + ";RATLvl:" + RAT_lvl;
                            result = DBG;
                        }
                        if (!result) {
                            if (rtt_escape <= 5000 && tcp_is_valid) {
                                i = this.mRtt.rtt_pkts;
                                if (r0 > SIM_STATE_READY) {
                                    int rtt_lvl = this.mRtt.getRttLevel();
                                    qos_Level = convertRTTLevel(RAT_lvl, rtt_lvl);
                                    compose_info = "use RTTLvl:" + rtt_lvl + ";RATLvl:" + RAT_lvl;
                                }
                            }
                            TxRxStat stat = this.mTraffic.getStatic(RECENT_TIME_S);
                            if (stat.rx_tx_rto < TrafficMonitor.VERY_LOW_RX_TX_RATIO) {
                                if (stat.txPkts > 20) {
                                    qos_Level = TYPE_WIFI;
                                    compose_info = "use Rx/Tx:" + stat.rx_tx_rto + ";TxPktSum:" + stat.txPkts + ";recent 5s,work:" + this.mTraffic.isWorking();
                                }
                            }
                            qos_Level = RAT_lvl;
                            compose_info = "use RAT:" + getRATName(net_type) + ";signalLvl:" + signal_level;
                        }
                    }
                }
                qos_Level = this.mSpeed_Level;
                compose_info = "use speedLvl:" + this.mSpeed_Level;
            } else {
                qos_Level = TYPE_MOBILE;
                compose_info = "unknown-link;use Lvl0";
                if (System.currentTimeMillis() - this.mSubIdChangeTimsstamp < 10000) {
                    qos_Level = convertRATLevel(signal_level, this.mTelephonyMgr.getNetworkType());
                    compose_info = "unknown-link&Sub-Change;use RAT-level";
                } else {
                    if (this.mHisSimStatus[this.mCurrDataSubID] == SIM_STATE_READY && !sim_ready) {
                        logi("SIM status error");
                    }
                }
            }
        }
        int delta_level = qos_Level - oldQosLevel;
        if (qos_Level == 0 || (delta_level <= TYPE_WIFI && delta_level >= MOBILE_INET_QOS_LEVEL_UNKNOWN)) {
            this.mQOS_Level = qos_Level;
        } else {
            this.mQOS_Level = (int) Math.round(((double) (qos_Level + oldQosLevel)) / 2.0d);
        }
        logi("QOS_level:" + this.mQOS_Level + "(" + qos_Level + ")," + compose_info);
        if (report_type == TYPE_WIFI && qos_Level <= ROVE_OUT_TO_MOBILE_LESS_THAN_50M_NOTIFY) {
            now = System.currentTimeMillis();
            if (now - this.mLowLevelTimestamp <= 8000) {
                logi("skip-low-Level:" + this.mQOS_Level + "(" + qos_Level + ")," + compose_info);
                this.mQOS_Level = oldQosLevel;
                return;
            }
            this.mLowLevelTimestamp = now;
        }
    }

    private void initMQDHandler() {
        HandlerThread thread = new HandlerThread("MobileQosDetect");
        thread.start();
        this.mHandler = new MobileQosDetectHandler(thread.getLooper(), null);
    }
}
