package com.huawei.hwwifiproservice;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.hwwifiproservice.TrafficMonitor;

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
    private static final String MOBILE_DATA_KEY = "mobile_data";
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
    private static final String SUB_SELECT_KEY = "user_datacall_sub";
    private static final int SUB_SWITCH_LATENCY = 10000;
    private static final String TAG = "MQoS";
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
    private double[][] mIPQWeight = {new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d}, new double[]{0.0d, 0.8d, 0.5d, 0.5d, 0.8d, 0.0d}, new double[]{0.0d, 0.8d, 0.5d, 0.5d, 0.8d, 0.0d}, new double[]{0.0d, 0.8d, 0.5d, 0.5d, 0.8d, 0.0d}, new double[]{0.0d, 0.8d, 0.5d, 0.5d, 0.8d, 0.0d}, new double[]{0.0d, 0.8d, 0.5d, 0.5d, 0.8d, 0.0d}};
    private int mIpqLevel = -1;
    private boolean mIsMonitoring = false;
    private int mIsRoveOutState = 0;
    private long mLastAlarmTotBytes = 0;
    private PhoneStateListener[] mListener = null;
    private long mLowLevelTimestamp = System.currentTimeMillis();
    private boolean mMobileDataSwitch = false;
    private final int mPhoneNum;
    private int mQosLevel = -1;
    private double[][] mRTTWeight = {new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d}, new double[]{0.0d, 0.7d, 0.7d, 0.8d, 0.8d, 0.8d}, new double[]{0.0d, 0.7d, 0.7d, 0.8d, 0.8d, 0.8d}, new double[]{0.0d, 0.7d, 0.7d, 0.8d, 0.8d, 0.8d}, new double[]{0.0d, 0.7d, 0.7d, 0.8d, 0.8d, 0.8d}, new double[]{0.0d, 0.7d, 0.7d, 0.8d, 0.8d, 0.8d}};
    private TcpIpqRtt mRtt = new TcpIpqRtt();
    private int[] mSIMArray = null;
    private int[] mSignalArray = null;
    private int mSignalLevel = -1;
    private SignalStrength mSignalStrength = null;
    private int mSimStatus = 0;
    private int mSpeed = -1;
    private int mSpeedLevel = -1;
    private Runnable mSpeedUpdate = new Runnable() {
        /* class com.huawei.hwwifiproservice.MobileQosDetector.AnonymousClass4 */

        @Override // java.lang.Runnable
        public void run() {
            int oldSpeed = MobileQosDetector.this.mSpeedLevel;
            MobileQosDetector mobileQosDetector = MobileQosDetector.this;
            mobileQosDetector.mSpeed = mobileQosDetector.mTraffic.getRxByteSpeed(1);
            MobileQosDetector mobileQosDetector2 = MobileQosDetector.this;
            mobileQosDetector2.mSpeedLevel = mobileQosDetector2.mTraffic.transform(MobileQosDetector.this.mSpeed);
            if (oldSpeed != MobileQosDetector.this.mSpeedLevel) {
                MobileQosDetector.this.requestComputeQos(0);
            }
            MobileQosDetector mobileQosDetector3 = MobileQosDetector.this;
            mobileQosDetector3.logi("speed:oldlevel:" + oldSpeed + ",new level:" + MobileQosDetector.this.mSpeedLevel + ",newSpeed(Bps):" + MobileQosDetector.this.mSpeed);
        }
    };
    private long mSubIdChangeTimsstamp = System.currentTimeMillis();
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener = null;
    private SubscriptionManager mSubscriptionManager = null;
    private TelephonyManager mTelephonyMgr = null;
    private long mTotalBytes = 0;
    private TrafficMonitor mTraffic = null;
    private WifiProUIDisplayManager mUiManager;
    private WifiProStateMachine mWifiProStateMachine = null;
    private WifiProStatisticsManager mWifiProStatisticsManager;
    private double[] rat2gWeight = {0.0d, 0.5d, 0.5d, 0.5d, 0.5d, 0.5d};
    private double[] rat3gEvdoaWeight = {0.0d, 0.5d, 0.5d, 0.5d, 0.5d, 0.5d};
    private double[] rat3gEvdobWeight = {0.0d, 0.5d, 0.5d, 0.6d, 0.6d, 0.6d};
    private double[] rat3gTdsWeight = {0.0d, 0.5d, 0.5d, 0.6d, 0.6d, 0.6d};
    private double[] rat3gUmtsWeight = {0.0d, 1.0d, 1.0d, 1.0d, 1.0d, 1.0d};
    private double[] rat4gWeight = {0.0d, 1.0d, 1.0d, 1.0d, 1.0d, 1.0d};
    private int readDataPeriod = 0;
    private long roStartRxBytes = 0;
    private long roStartTxBytes = 0;

    public MobileQosDetector(Context qosContext, INetworkQosCallBack qosCallBack, Handler handler, WifiProUIDisplayManager wifiProUIDisplayManager) {
        this.mContext = qosContext;
        this.mCallback = qosCallBack;
        this.mCallerHandler = handler;
        this.mUiManager = wifiProUIDisplayManager;
        initMQDHandler();
        this.mTelephonyMgr = (TelephonyManager) qosContext.getSystemService("phone");
        this.mConnMgr = (ConnectivityManager) qosContext.getSystemService("connectivity");
        this.mPhoneNum = this.mTelephonyMgr.getPhoneCount();
        int i = this.mPhoneNum;
        this.mSignalArray = new int[i];
        this.mSIMArray = new int[i];
        this.mListener = new PhoneStateListener[i];
        this.mHisSimStatus = new int[i];
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
        if (this.mSubscriptionListener == null) {
            this.mSubscriptionListener = new SubListener();
        }
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
        logi("PhoneNum=" + this.mPhoneNum);
        initObserver();
        this.mCurrDataSubID = SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultDataSubscriptionId());
        int i2 = this.mCurrDataSubID;
        if (i2 < 0 || i2 >= this.mPhoneNum) {
            int subId = 0;
            int index = 0;
            while (true) {
                if (index >= this.mPhoneNum) {
                    break;
                } else if (this.mSIMArray[index] == 5) {
                    subId = index;
                    break;
                } else {
                    index++;
                }
            }
            this.mCurrDataSubID = subId;
            logi("subId recheck=" + this.mCurrDataSubID);
        }
        this.mSimStatus = this.mTelephonyMgr.getSimState(this.mCurrDataSubID);
        logi("init defSubID=" + this.mCurrDataSubID + ",SIMReady=" + this.mSimStatus);
        this.mTraffic = new TrafficMonitor(this.mSpeedUpdate, this.mContext);
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
        WifiProStatisticsManager wifiProStatisticsManager = this.mWifiProStatisticsManager;
        if (wifiProStatisticsManager != null) {
            wifiProStatisticsManager.registerMobileInfoCallback(this);
        }
        this.mWifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
    }

    public void monitorNetworkQos(boolean enable) {
        if (enable) {
            this.mTraffic.enableMonitor(DBG, 0);
            this.mTraffic.setExpireTime(5000);
        } else {
            this.mTraffic.enableMonitor(false, 0);
        }
        this.mIsMonitoring = enable;
    }

    public void queryNetworkQos() {
        Log.i(TAG, "queryNetworkQos start");
        queryRtt();
        this.mHandler.sendEmptyMessageDelayed(MSG_QUERY_MQOS, 5);
    }

    public void setIPQos(int type, int len, int[] ipqos) {
        int tcpRetranPkts = 0;
        int tcpRtt = len > 0 ? ipqos[0] : 0;
        int tcpRttPkts = len > 1 ? ipqos[1] : 0;
        int tcpRttWhen = len > 2 ? ipqos[2] : 0;
        int tcpCongestion = len > 3 ? ipqos[3] : 0;
        int tcpCongWhen = len > 4 ? ipqos[4] : 0;
        int tcpLevel = len > 5 ? ipqos[5] : -1;
        long ts = System.currentTimeMillis() - ((long) tcpRttWhen);
        int tcpTxPkts = len > 6 ? ipqos[6] : 0;
        int tcpRxPkts = len > 7 ? ipqos[7] : 0;
        if (len > 8) {
            tcpRetranPkts = ipqos[8];
        }
        if (type == 1) {
            this.mIpqLevel = tcpLevel;
            this.mHandler.removeMessages(MSG_IPQOS_EXPIRED);
            this.mHandler.sendEmptyMessageDelayed(MSG_IPQOS_EXPIRED, 5000);
        }
        this.mRtt.setMember(tcpRtt, tcpRttPkts, tcpRttWhen, tcpCongestion, tcpCongWhen, type, tcpLevel, ts, tcpTxPkts, tcpRxPkts, tcpRetranPkts);
        requestComputeQos(0);
        Log.i(TAG, "rtt=" + tcpRtt + ",tcpTxPkts=" + tcpTxPkts + ",tcpRxPkts=" + tcpRxPkts);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void queryRtt() {
        this.mCallerHandler.sendEmptyMessage(4);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestComputeQos(long delayMs) {
        this.mHandler.sendEmptyMessageDelayed(101, delayMs);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportQos(int reportType, int qosLevel) {
        Log.i(TAG, "entry report: qos lvl=" + qosLevel + ",reportType:" + reportType);
        INetworkQosCallBack iNetworkQosCallBack = this.mCallback;
        if (iNetworkQosCallBack == null) {
            Log.e(TAG, "mCallback is null!, reportType=" + reportType);
            return;
        }
        if (reportType == 0) {
            Log.i(TAG, "before query report: qos lvl=" + qosLevel);
            this.mCallback.onNetworkDetectionResult(0, qosLevel);
        } else if (reportType == 1) {
            iNetworkQosCallBack.onNetworkQosChange(0, qosLevel, DBG);
        } else {
            return;
        }
        Log.i(TAG, "after report: qos lvl=" + qosLevel + ",svc working:" + this.mIsMonitoring);
    }

    private void initObserver() {
        this.mContentRsr = this.mContext.getContentResolver();
        ContentObserver dataSwitchObserver = new ContentObserver(this.mHandler) {
            /* class com.huawei.hwwifiproservice.MobileQosDetector.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                MobileQosDetector mobileQosDetector = MobileQosDetector.this;
                mobileQosDetector.mMobileDataSwitch = mobileQosDetector.getDataStatus();
                MobileQosDetector.this.requestComputeQos(0);
                MobileQosDetector mobileQosDetector2 = MobileQosDetector.this;
                mobileQosDetector2.logi("MobileData switch:" + MobileQosDetector.this.mMobileDataSwitch);
            }
        };
        ContentObserver subSelectObserver = new ContentObserver(this.mHandler) {
            /* class com.huawei.hwwifiproservice.MobileQosDetector.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                int oldId = MobileQosDetector.this.mCurrDataSubID;
                MobileQosDetector mobileQosDetector = MobileQosDetector.this;
                mobileQosDetector.mCurrDataSubID = Settings.Global.getInt(mobileQosDetector.mContentRsr, MobileQosDetector.SUB_SELECT_KEY, 0);
                MobileQosDetector mobileQosDetector2 = MobileQosDetector.this;
                mobileQosDetector2.logi("new subID:" + MobileQosDetector.this.mCurrDataSubID + " is Selected,oldId:" + oldId);
                if (oldId != MobileQosDetector.this.mCurrDataSubID) {
                    int subId = MobileQosDetector.this.mCurrDataSubID;
                    if (subId < 0 || subId >= MobileQosDetector.this.mPhoneNum) {
                        subId = 0;
                    }
                    MobileQosDetector mobileQosDetector3 = MobileQosDetector.this;
                    mobileQosDetector3.mSignalLevel = mobileQosDetector3.mSignalArray[subId];
                    MobileQosDetector.this.mSpeedLevel = -1;
                    MobileQosDetector.this.mIpqLevel = -1;
                    MobileQosDetector.this.mRtt.reset();
                    MobileQosDetector.this.mTraffic.reset();
                    MobileQosDetector mobileQosDetector4 = MobileQosDetector.this;
                    mobileQosDetector4.mSimStatus = mobileQosDetector4.mTelephonyMgr.getSimState(MobileQosDetector.this.mCurrDataSubID);
                    MobileQosDetector mobileQosDetector5 = MobileQosDetector.this;
                    mobileQosDetector5.logi("SIM status:" + MobileQosDetector.this.mSimStatus);
                    MobileQosDetector.this.mSubIdChangeTimsstamp = System.currentTimeMillis();
                    MobileQosDetector.this.requestComputeQos(10000);
                }
            }
        };
        this.mContentRsr.registerContentObserver(Settings.Global.getUriFor(MOBILE_DATA_KEY), false, dataSwitchObserver);
        this.mContentRsr.registerContentObserver(Settings.Global.getUriFor(SUB_SELECT_KEY), false, subSelectObserver);
        this.mMobileDataSwitch = getDataStatus();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getDataStatus() {
        if (Settings.Global.getInt(this.mContentRsr, MOBILE_DATA_KEY, 0) == 1) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private PhoneStateListener getPSListener(int subscription) {
        return new DualPhoneStateListener(subscription) {
            /* class com.huawei.hwwifiproservice.MobileQosDetector.AnonymousClass3 */
            int mSubId = SubscriptionManager.getSlotIndex(DualPhoneStateListener.getSubscription(this));

            @Override // android.telephony.PhoneStateListener
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                Log.i(MobileQosDetector.TAG, "onSignal: mSubId=" + this.mSubId + ",currDataSubID=" + MobileQosDetector.this.mCurrDataSubID);
                if (signalStrength == null) {
                    Log.e(MobileQosDetector.TAG, "signalStrength is null and return.");
                    return;
                }
                if (MobileQosDetector.this.mWifiProStateMachine != null) {
                    if (WifiProCommonUtils.isSignalValid(signalStrength.getLteRsrp())) {
                        MobileQosDetector.this.mWifiProStateMachine.notifyTelephonySignalStrength(this.mSubId, signalStrength.getLteRsrp(), signalStrength.getLevel());
                    } else if (WifiProCommonUtils.isSignalValid(signalStrength.getNrRsrp())) {
                        MobileQosDetector.this.mWifiProStateMachine.notifyTelephonySignalStrength(this.mSubId, signalStrength.getNrRsrp(), signalStrength.getLevel());
                    } else {
                        Log.e(MobileQosDetector.TAG, "invalid signal strength.");
                    }
                }
                MobileQosDetector.this.mSignalStrength = new SignalStrength(signalStrength);
                MobileQosDetector mobileQosDetector = MobileQosDetector.this;
                int signalLevel = mobileQosDetector.convertSignalLevel(mobileQosDetector.mSignalStrength);
                int i = this.mSubId;
                int subid = (i < 0 || i >= MobileQosDetector.this.mPhoneNum) ? 0 : this.mSubId;
                MobileQosDetector.this.mSignalArray[subid] = signalLevel;
                if (this.mSubId == MobileQosDetector.this.mCurrDataSubID && MobileQosDetector.this.mSignalLevel != MobileQosDetector.this.mSignalArray[subid]) {
                    MobileQosDetector mobileQosDetector2 = MobileQosDetector.this;
                    mobileQosDetector2.mSignalLevel = mobileQosDetector2.mSignalArray[subid];
                    MobileQosDetector.this.requestComputeQos(0);
                }
            }

            @Override // android.telephony.PhoneStateListener
            public void onDataConnectionStateChanged(int state, int networkType) {
                Log.i(MobileQosDetector.TAG, "onDataConn: mSubId=" + this.mSubId + ",state=" + state + ",networkType=" + networkType);
                int i = this.mSubId;
                boolean suspend = false;
                this.mSubId = (i < 0 || i >= MobileQosDetector.this.mPhoneNum) ? 0 : this.mSubId;
                if (state == 2) {
                    MobileQosDetector.this.mCurrDataSubID = this.mSubId;
                    MobileQosDetector mobileQosDetector = MobileQosDetector.this;
                    mobileQosDetector.mSimStatus = mobileQosDetector.mTelephonyMgr.getSimState(MobileQosDetector.this.mCurrDataSubID);
                    Log.i(MobileQosDetector.TAG, "onDataConn: mSubId=" + this.mSubId + ",mSimStatus=" + MobileQosDetector.this.mSimStatus);
                    MobileQosDetector.this.requestComputeQos(0);
                    if (!MobileQosDetector.this.mHandler.hasMessages(107)) {
                        MobileQosDetector.this.mHandler.sendEmptyMessageDelayed(107, 30000);
                    } else {
                        Log.i(MobileQosDetector.TAG, "has pending msg(QUERY_TCP_INFO),skip!");
                    }
                    MobileQosDetector.this.mDataConnTimestamp = System.currentTimeMillis();
                    MobileQosDetector.this.requestComputeQos(30000);
                }
                if (MobileQosDetector.this.mCurrDataSubID == this.mSubId) {
                    if (state == 3) {
                        suspend = MobileQosDetector.DBG;
                    }
                    if (suspend != MobileQosDetector.this.mDataSuspend) {
                        MobileQosDetector.this.mDataSuspend = suspend;
                        MobileQosDetector.this.requestComputeQos(0);
                    }
                }
            }

            @Override // android.telephony.PhoneStateListener
            public void onServiceStateChanged(ServiceState serviceState) {
                int voiceState = serviceState.getState();
                int dataState = serviceState.getDataRegState();
                Log.i(MobileQosDetector.TAG, "mSubId=" + this.mSubId + ",voiceState:" + voiceState + ",dataState:" + dataState);
                int i = this.mSubId;
                this.mSubId = (i < 0 || i >= MobileQosDetector.this.mPhoneNum) ? 0 : this.mSubId;
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
                        int mccCode = Integer.parseInt(mcc);
                        if (mccCode > 0) {
                            Message.obtain(MobileQosDetector.this.mCallerHandler, 5, mccCode, 0).sendToTarget();
                        }
                    } catch (NumberFormatException e) {
                        Log.e(MobileQosDetector.TAG, "getMCC error:" + mcc);
                    }
                } else {
                    int[] iArr = MobileQosDetector.this.mSIMArray;
                    int i2 = this.mSubId;
                    iArr[i2] = 0;
                    if (i2 == MobileQosDetector.this.mCurrDataSubID) {
                        MobileQosDetector.this.mSimStatus = 0;
                        MobileQosDetector.this.requestComputeQos(0);
                    }
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int convertSignalLevel(SignalStrength ss) {
        int signalLevel = 0;
        if (ss == null) {
            return 0;
        }
        int level = ss.getLevel();
        if (level == 0) {
            signalLevel = 1;
        } else if (level == 1) {
            signalLevel = 2;
        } else if (level == 2) {
            signalLevel = 3;
        } else if (level == 3) {
            signalLevel = 4;
        } else if (level == 4) {
            signalLevel = 5;
        } else if (level > 4) {
            signalLevel = 5;
        }
        Log.i(TAG, "received cell-signal:" + signalLevel);
        return signalLevel;
    }

    private int convertIPQLevel(int signalLvl, int ipqLvl) {
        int ipqLevel = 0;
        int signalLevel = signalLvl >= 0 ? signalLvl : 0;
        if (ipqLvl > 0) {
            ipqLevel = ipqLvl;
        }
        double wIpQos = this.mIPQWeight[signalLevel][ipqLevel];
        return Math.round((float) ((((double) ipqLevel) * wIpQos) + (((double) signalLevel) * (1.0d - wIpQos))));
    }

    private int convertRTTLevel(int signalLvl, int rttLvl) {
        int rttLevel = 0;
        int signalLevel = signalLvl >= 0 ? signalLvl : 0;
        if (rttLvl > 0) {
            rttLevel = rttLvl;
        }
        double wIpQos = this.mRTTWeight[signalLevel][rttLevel];
        return Math.round((float) ((((double) rttLevel) * wIpQos) + (((double) signalLevel) * (1.0d - wIpQos))));
    }

    private int convertRATLevel(int signalLevel, int netType) {
        int sigLevel = signalLevel >= 0 ? signalLevel : 0;
        int newLevel = sigLevel;
        TelephonyManager telephonyManager = this.mTelephonyMgr;
        int ratClass = TelephonyManager.getNetworkClass(netType);
        if (1 == ratClass) {
            newLevel = (int) Math.floor((double) ((float) (((double) sigLevel) * this.rat2gWeight[sigLevel])));
        } else if (2 == ratClass) {
            if (netType != 3) {
                if (netType != 12) {
                    if (netType != 15) {
                        if (netType != 5 && netType != 6) {
                            switch (netType) {
                                case 8:
                                case 9:
                                case 10:
                                    break;
                                default:
                                    newLevel = Math.round((float) (((double) sigLevel) * this.rat3gTdsWeight[sigLevel]));
                                    break;
                            }
                        } else {
                            newLevel = Math.round((float) (((double) sigLevel) * this.rat3gEvdoaWeight[sigLevel]));
                        }
                    }
                } else {
                    newLevel = Math.round((float) (((double) sigLevel) * this.rat3gEvdobWeight[sigLevel]));
                }
            }
            newLevel = Math.round((float) (((double) sigLevel) * this.rat3gUmtsWeight[sigLevel]));
        } else if (3 == ratClass) {
            newLevel = Math.round((float) (((double) sigLevel) * this.rat4gWeight[sigLevel]));
        } else {
            Log.e(TAG, "unkown RAT!");
        }
        if (newLevel < 1) {
            return 1;
        }
        return newLevel;
    }

    private String getRATName(int netType) {
        TelephonyManager telephonyManager = this.mTelephonyMgr;
        int ratType = TelephonyManager.getNetworkClass(netType);
        if (ratType == 1) {
            return "2G";
        }
        if (ratType != 2) {
            if (ratType != 3) {
                return "unknownRAT";
            }
            return "4G";
        } else if (netType == 3) {
            return "3G-UMTS";
        } else {
            if (netType == 12) {
                return "3G-EVDO";
            }
            if (netType == 15) {
                return "3G-UMTS";
            }
            if (netType == 5 || netType == 6) {
                return "3G-EVDO";
            }
            switch (netType) {
                case 8:
                case 9:
                case 10:
                    return "3G-UMTS";
                default:
                    return "3G-TD";
            }
        }
    }

    private int getRATClass(int netType) {
        TelephonyManager telephonyManager = this.mTelephonyMgr;
        int ratType = TelephonyManager.getNetworkClass(netType);
        if (ratType == 1) {
            return 1;
        }
        if (ratType != 2) {
            if (ratType == 3) {
                return 5;
            }
            Log.w(TAG, "invalid RAT type");
            return 0;
        } else if (netType == 3) {
            return 4;
        } else {
            if (netType != 12) {
                if (netType == 15) {
                    return 4;
                }
                if (!(netType == 5 || netType == 6)) {
                    switch (netType) {
                        case 8:
                        case 9:
                        case 10:
                            return 4;
                        default:
                            return 2;
                    }
                }
            }
            return 3;
        }
    }

    @Override // com.huawei.hwwifiproservice.IGetMobileInfoCallBack
    public synchronized int onGetMobileSignalLevel() {
        return this.mSignalLevel;
    }

    @Override // com.huawei.hwwifiproservice.IGetMobileInfoCallBack
    public synchronized int onGetMobileRATType() {
        if (this.mTelephonyMgr == null) {
            return 0;
        }
        return getRATClass(this.mTelephonyMgr.getNetworkType());
    }

    @Override // com.huawei.hwwifiproservice.IGetMobileInfoCallBack
    public synchronized int getTotalRoMobileData() {
        int totData;
        totData = (int) (this.mTotalBytes / 1024);
        logi("getTotalRoMobileData total data=" + totData);
        return totData;
    }

    public void sendMsg(int what, int arg1, int arg2) {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendMessage(Message.obtain(handler, what, arg1, arg2));
        }
    }

    public synchronized void setRoveOutToMobileState(int roveOutState) {
        logi("setRoveOutToMobileState enter, state=" + roveOutState);
        sendMsg(MSG_RO_STATE_CHANGE, roveOutState, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void periodCheckHighDataFlow() {
        if (this.mUiManager == null) {
            Log.e(TAG, "mUiManager null error.");
        } else if (this.mIsRoveOutState != 0) {
            int i = this.readDataPeriod + 1;
            this.readDataPeriod = i;
            if (i >= 2) {
                long rxBytes = this.mTraffic.getMobileRxBytes();
                long txBytes = this.mTraffic.getMobileTxBytes();
                if (rxBytes == -1 || txBytes == -1) {
                    Log.e(TAG, "read rx tx error, rx=" + rxBytes + ", tx=" + txBytes);
                } else if (this.roStartRxBytes == 0 && this.roStartTxBytes == 0) {
                    this.roStartRxBytes = rxBytes;
                    this.roStartTxBytes = txBytes;
                    logi("last read data invalid.");
                } else {
                    long j = this.roStartRxBytes;
                    if (rxBytes >= j) {
                        long j2 = this.roStartTxBytes;
                        if (txBytes >= j2) {
                            this.mTotalBytes += (rxBytes - j) + (txBytes - j2);
                        }
                    }
                    if (rxBytes > 0 || txBytes > 0) {
                        this.roStartRxBytes = rxBytes;
                        this.roStartTxBytes = txBytes;
                    }
                    logi("rove out start, rxBytes=" + rxBytes + ", txBytes=" + txBytes + ", totBytes=" + this.mTotalBytes);
                    if (this.mIsRoveOutState == 1) {
                        long j3 = this.mTotalBytes;
                        if (j3 >= 10485760) {
                            this.mIsRoveOutState = 2;
                            this.mLastAlarmTotBytes = j3;
                            logi(" high data flow >= 10M, set first notify.");
                            this.mWifiProStatisticsManager.increaseHMDNotifyCount(1);
                            this.readDataPeriod = 0;
                        }
                    }
                    int i2 = this.mIsRoveOutState;
                    if (i2 == 2) {
                        long j4 = this.mTotalBytes;
                        if (j4 >= ALARM_DATA_SIZE_LEVEL_2) {
                            this.mIsRoveOutState = 3;
                            logi(" high data flow >= 50M, show 50M notify.");
                            this.mLastAlarmTotBytes = this.mTotalBytes;
                            this.mWifiProStatisticsManager.increaseHMDNotifyCount(2);
                        } else if (j4 - this.mLastAlarmTotBytes > 10485760) {
                            logi(" high data flow > " + (((int) (j4 / 10485760)) * 10) + "M, update notify display.");
                            this.mLastAlarmTotBytes = this.mTotalBytes;
                        }
                    } else if (3 == i2) {
                        long j5 = this.mTotalBytes;
                        if (j5 - this.mLastAlarmTotBytes > 10485760) {
                            logi(" high data flow > " + (((int) (j5 / 10485760)) * 10) + "M, update notify display.");
                            this.mLastAlarmTotBytes = this.mTotalBytes;
                        }
                    }
                    this.readDataPeriod = 0;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logi(String msg) {
        Log.i(TAG, msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void composeQOSLevel(int signalLevel, int reportType) {
        int ratLvl;
        int i;
        int i2;
        int currentSignalLevel = signalLevel;
        int connType = -1;
        NetworkInfo netInfo = this.mConnMgr.getActiveNetworkInfo();
        String composeInfo = "unknown";
        int currentQosLevel = this.mQosLevel;
        int[] iArr = this.mHisSimStatus;
        if (iArr == null) {
            return;
        }
        if (iArr.length != 0) {
            if (netInfo != null) {
                connType = netInfo.getType();
            }
            if (currentSignalLevel < 0) {
                int i3 = this.mSignalLevel;
                if (i3 < 0) {
                    i3 = 0;
                }
                currentSignalLevel = i3;
            }
            boolean simReady = this.mSimStatus == 5 || ((i2 = this.mCurrDataSubID) >= 0 && i2 < this.mPhoneNum && this.mSIMArray[i2] == 5);
            if (!this.mMobileDataSwitch) {
                currentQosLevel = 0;
                composeInfo = "DataDisable;use level-0";
            } else if (this.mDataSuspend) {
                currentQosLevel = 0;
                composeInfo = "DataSuspend;use level-0";
            } else if (this.mTelephonyMgr.isNetworkRoaming(this.mCurrDataSubID)) {
                currentQosLevel = 0;
                composeInfo = "dis-Roaming;use level-0";
            } else if (connType == 1) {
                if (!simReady) {
                    int simState = this.mTelephonyMgr.getSimState(this.mCurrDataSubID);
                    ServiceState serviceState = this.mTelephonyMgr.getServiceStateForSubscriber(this.mCurrDataSubID);
                    if (simState == 5 && (i = this.mPhoneNum) >= 0 && i <= 3 && serviceState != null && serviceState.getState() == 0) {
                        for (int i4 = 0; i4 < this.mPhoneNum; i4++) {
                            this.mTelephonyMgr.listen(this.mListener[i4], this.mEvents);
                            this.mSignalArray[i4] = -1;
                            this.mSIMArray[i4] = this.mTelephonyMgr.getSimState(i4);
                            this.mHisSimStatus[i4] = this.mSIMArray[i4];
                        }
                        this.mSimStatus = 5;
                        simReady = DBG;
                        logi("SIM status change, simReady = " + DBG);
                    }
                }
                if (simReady) {
                    int netType = this.mTelephonyMgr.getNetworkType();
                    currentQosLevel = convertRATLevel(currentSignalLevel, netType);
                    composeInfo = "wifi-link;use signalLvl:" + currentSignalLevel + ";RAT:" + getRATName(netType);
                } else {
                    currentQosLevel = 0;
                    composeInfo = "wifi-link;SIM error;use level-0";
                }
            } else if (connType == 0) {
                int i5 = this.mSpeedLevel;
                if (i5 != 5) {
                    if (i5 != 4) {
                        int networkType = this.mTelephonyMgr.getNetworkType();
                        int ratLvl2 = convertRATLevel(currentSignalLevel, networkType);
                        long now = System.currentTimeMillis();
                        long rttEscape = now - this.mRtt.timestamp;
                        boolean tcpIsValid = now - this.mDataConnTimestamp >= 30000;
                        boolean result = false;
                        int i6 = this.mIpqLevel;
                        if (i6 == 1 || i6 == 2) {
                            ratLvl = ratLvl2;
                            if (ratLvl < 3 && tcpIsValid) {
                                currentQosLevel = convertIPQLevel(ratLvl, this.mIpqLevel);
                                composeInfo = "use IPQLvl:" + this.mIpqLevel + ";RATLvl:" + ratLvl;
                                result = DBG;
                            }
                        } else {
                            ratLvl = ratLvl2;
                        }
                        if (!result) {
                            if (rttEscape <= 5000 && tcpIsValid) {
                                int i7 = this.mRtt.rttPkts;
                                TcpIpqRtt tcpIpqRtt = this.mRtt;
                                if (i7 > 5) {
                                    int rttLvl = tcpIpqRtt.getRttLevel();
                                    composeInfo = "use RTTLvl:" + rttLvl + ";RATLvl:" + ratLvl;
                                    currentQosLevel = convertRTTLevel(ratLvl, rttLvl);
                                }
                            }
                            TrafficMonitor.TxRxStat stat = this.mTraffic.getStatic(6);
                            if (stat.rx_tx_rto >= 0.4d || stat.txPkts <= 20) {
                                composeInfo = "use RAT:" + getRATName(networkType) + ";signalLvl:" + currentSignalLevel;
                                currentQosLevel = ratLvl;
                            } else {
                                composeInfo = "use Rx/Tx:" + stat.rx_tx_rto + ";TxPktSum:" + stat.txPkts + ";recent 5s,work:" + this.mTraffic.isWorking();
                                currentQosLevel = 1;
                            }
                        }
                    }
                }
                currentQosLevel = this.mSpeedLevel;
                composeInfo = "use speedLvl:" + this.mSpeedLevel;
            } else {
                currentQosLevel = 0;
                composeInfo = "unknown-link;use Lvl0";
                if (System.currentTimeMillis() - this.mSubIdChangeTimsstamp < 10000) {
                    currentQosLevel = convertRATLevel(currentSignalLevel, this.mTelephonyMgr.getNetworkType());
                    composeInfo = "unknown-link&Sub-Change;use RAT-level";
                } else if (this.mHisSimStatus[this.mCurrDataSubID] == 5 && !simReady) {
                    logi("SIM status error");
                }
            }
            int oldQosLevel = this.mQosLevel;
            int deltaLevel = currentQosLevel - oldQosLevel;
            if (currentQosLevel == 0 || (deltaLevel <= 1 && deltaLevel >= -1)) {
                this.mQosLevel = currentQosLevel;
            } else {
                this.mQosLevel = (int) Math.round(((double) (currentQosLevel + oldQosLevel)) / 2.0d);
            }
            logi("QOS_level:" + this.mQosLevel + "(" + currentQosLevel + ")," + composeInfo);
            if (reportType == 1 && currentQosLevel <= 2) {
                long now2 = System.currentTimeMillis();
                if (now2 - this.mLowLevelTimestamp <= 8000) {
                    logi("skip-low-Level:" + this.mQosLevel + "(" + currentQosLevel + ")," + composeInfo);
                    this.mQosLevel = oldQosLevel;
                    return;
                }
                this.mLowLevelTimestamp = now2;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class TcpIpqRtt {
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
        public int rttPkts;
        public int rttWhen;
        public int tcpRxPkts;
        public int tcpTxPkts;
        public int tcp_retrans_pkts;
        public long timestamp;
        public int type;

        public TcpIpqRtt() {
            reset();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void reset() {
            this.rtt = 0;
            this.rttPkts = 0;
            this.rttWhen = 0;
            this.congestion = 0;
            this.cong_when = 0;
            this.timestamp = 0;
            this.type = 0;
            this.level = 0;
            this.tcpTxPkts = 0;
            this.tcpRxPkts = 0;
            this.tcp_retrans_pkts = 0;
        }

        public void setMember(int rtt2, int rttPkts2, int rttWhen2, int congestion2, int cong_when2, int type2, int lvl, long ts, int tcpTxPkts2, int tcpRxPkts2, int tcpRetansPkts) {
            this.rtt = rtt2;
            this.rttPkts = rttPkts2;
            this.rttWhen = rttWhen2;
            this.congestion = congestion2;
            this.cong_when = cong_when2;
            this.timestamp = ts;
            this.type = type2;
            this.level = lvl;
            this.tcpTxPkts = tcpTxPkts2;
            this.tcpRxPkts = tcpRxPkts2;
            this.tcp_retrans_pkts = tcpRetansPkts;
        }

        public int getRttLevel() {
            int i = this.rtt;
            if (i < 300) {
                return 5;
            }
            if (i < 850) {
                return 4;
            }
            if (i < 1400) {
                return 3;
            }
            if (i < 1900) {
                return 2;
            }
            if (i < 2040) {
                return 1;
            }
            Log.w(MobileQosDetector.TAG, "rttLevel is invalid");
            return 1;
        }

        public String toString() {
            return "rtt=" + this.rtt + ",rttPkts=" + this.rttPkts + ",rttWhen=" + this.rttWhen + ",congestion=" + this.congestion + ",cong_when=" + this.cong_when + ",type=" + this.type + ",lvl=" + this.level + ",ts=" + this.timestamp;
        }
    }

    private void initMQDHandler() {
        HandlerThread thread = new HandlerThread("MobileQosDetect");
        thread.start();
        this.mHandler = new MobileQosDetectHandler(thread.getLooper());
    }

    /* access modifiers changed from: package-private */
    public class MobileQosDetectHandler extends Handler {
        private MobileQosDetectHandler(Looper looper) {
            super(looper);
            MobileQosDetector.this.logi("new MobileQosDetectHandler");
        }

        private void handleRoStateChagneMsg(Message msg) {
            int roState = msg.arg1;
            if (MobileQosDetector.this.mTraffic != null) {
                if (1 == roState) {
                    MobileQosDetector.this.mIsRoveOutState = 1;
                    MobileQosDetector mobileQosDetector = MobileQosDetector.this;
                    mobileQosDetector.roStartRxBytes = mobileQosDetector.mTraffic.getMobileRxBytes();
                    MobileQosDetector mobileQosDetector2 = MobileQosDetector.this;
                    mobileQosDetector2.roStartTxBytes = mobileQosDetector2.mTraffic.getMobileTxBytes();
                    MobileQosDetector.this.mTotalBytes = 0;
                    MobileQosDetector mobileQosDetector3 = MobileQosDetector.this;
                    mobileQosDetector3.logi("rove out start, first read rxBytes=" + MobileQosDetector.this.roStartRxBytes + ", txBytes=" + MobileQosDetector.this.roStartTxBytes);
                } else if (roState == 0) {
                    MobileQosDetector.this.mIsRoveOutState = 0;
                    if (MobileQosDetector.this.mUiManager != null) {
                        MobileQosDetector.this.mUiManager.cleanUpheadNotificationHMD();
                    }
                    MobileQosDetector.this.logi("rove out end.");
                }
            }
        }

        private void handleQueryTcpInfo(Message msg) {
            Log.i(MobileQosDetector.TAG, "MSG_QUERY_TCP_INFO called,count:" + msg.arg2);
            MobileQosDetector.this.queryRtt();
            NetworkInfo netInfo = MobileQosDetector.this.mConnMgr.getActiveNetworkInfo();
            if ((netInfo != null ? netInfo.getType() : -1) == 0) {
                if (!MobileQosDetector.this.mHandler.hasMessages(107)) {
                    MobileQosDetector.this.mHandler.sendMessageDelayed(MobileQosDetector.this.mHandler.obtainMessage(107, 0, msg.arg2 + 1), 10000);
                } else {
                    Log.i(MobileQosDetector.TAG, "MSG_QUERY_TCP_INFO loop called,skip:" + msg.arg2);
                }
                MobileQosDetector.this.periodCheckHighDataFlow();
                return;
            }
            MobileQosDetector.this.mHandler.removeMessages(107);
            Log.i(MobileQosDetector.TAG, "stop query RTT!");
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 101) {
                switch (i) {
                    case MobileQosDetector.MSG_IPQOS_EXPIRED /* 105 */:
                        MobileQosDetector.this.mIpqLevel = -1;
                        MobileQosDetector.this.mHandler.removeMessages(MobileQosDetector.MSG_IPQOS_EXPIRED);
                        return;
                    case MobileQosDetector.MSG_QUERY_MQOS /* 106 */:
                        Log.i(MobileQosDetector.TAG, "queryNetworkQos enter");
                        MobileQosDetector.this.composeQOSLevel(-1, 0);
                        MobileQosDetector mobileQosDetector = MobileQosDetector.this;
                        mobileQosDetector.reportQos(0, mobileQosDetector.mQosLevel);
                        Log.i(MobileQosDetector.TAG, "queryNetworkQos exit");
                        return;
                    case 107:
                        handleQueryTcpInfo(msg);
                        return;
                    case MobileQosDetector.MSG_RO_STATE_CHANGE /* 108 */:
                        handleRoStateChagneMsg(msg);
                        return;
                    default:
                        return;
                }
            } else {
                int oldLevel = MobileQosDetector.this.mQosLevel;
                MobileQosDetector.this.composeQOSLevel(-1, 1);
                if (oldLevel != MobileQosDetector.this.mQosLevel && MobileQosDetector.this.mIsMonitoring) {
                    MobileQosDetector mobileQosDetector2 = MobileQosDetector.this;
                    mobileQosDetector2.reportQos(1, mobileQosDetector2.mQosLevel);
                }
                MobileQosDetector.this.mHandler.removeMessages(101);
            }
        }
    }

    private class SubListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        private SubListener() {
        }

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            for (int i = 0; i < MobileQosDetector.this.mPhoneNum; i++) {
                int[] subId = SubscriptionManagerEx.getSubId(i);
                if (subId != null && subId.length > 0) {
                    if (MobileQosDetector.this.mListener[i] != null) {
                        MobileQosDetector.this.mTelephonyMgr.listen(MobileQosDetector.this.mListener[i], 0);
                    }
                    MobileQosDetector.this.mListener[i] = MobileQosDetector.this.getPSListener(subId[0]);
                    MobileQosDetector.this.mTelephonyMgr.listen(MobileQosDetector.this.mListener[i], MobileQosDetector.this.mEvents);
                    MobileQosDetector.this.mSignalArray[i] = -1;
                    MobileQosDetector.this.mSIMArray[i] = MobileQosDetector.this.mTelephonyMgr.getSimState(i);
                    MobileQosDetector.this.mHisSimStatus[i] = MobileQosDetector.this.mSIMArray[i];
                }
            }
        }
    }
}
