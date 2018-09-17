package com.android.server.location;

import android.content.Context;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import com.android.internal.location.ProviderRequest;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.location.gnsschrlog.CSegEVENT_CHR_GNSS_HAL_EVENT_EXCEPTION;
import com.android.server.location.gnsschrlog.CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT;
import com.android.server.location.gnsschrlog.CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL;
import com.android.server.location.gnsschrlog.CSegEVENT_GPS_DAILY_CNT_REPORT;
import com.android.server.location.gnsschrlog.CSegEVENT_GPS_POS_FLOW_ERROR_EVENT;
import com.android.server.location.gnsschrlog.CSegEVENT_GPS_POS_TIMEOUT_EVENT;
import com.android.server.location.gnsschrlog.CSegEVENT_NETWK_POS_TIMEOUT_EVENT;
import com.android.server.location.gnsschrlog.ChrLogBaseEventModel;
import com.android.server.location.gnsschrlog.ChrLogBaseModel;
import com.android.server.location.gnsschrlog.GnssChrCommonInfo;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.location.gnsschrlog.GnssLogManager;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.wifipro.WifiProCommonUtils;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

public class HwOldGpsLogServices {
    private static final int ADDBATCHINGSTATUS = 16;
    private static final int ADDGEOFENCESTATUS = 15;
    private static final int AGPS_CONN_FAILED = 18;
    private static final int AGPS_TIMEOUT = 14;
    private static final int ANY_VALUE = 4;
    public static final String ASSISTED_GPS_MODE = "assisted_gps_mode";
    private static final int BETA_ALL_FIRST_FIX_TIMEOUT_NUM = 5;
    private static final int BETA_ALL_LOST_POS_TIMES = 5;
    private static final int C2K_VALUE = 2;
    private static final int COMM_ALL_FIRST_FIX_TIMEOUT_NUM = 30;
    private static final int COMM_ALL_LOST_POS_TIMES = 30;
    private static final long COMM_UPLOAD_MIN_SPAN = 86400000;
    private static final int CONNECTION_TYPE_ANY = 0;
    private static final int CONNECTION_TYPE_C2K = 2;
    private static final int CONNECTION_TYPE_SUPL = 1;
    private static final int CONNECTION_TYPE_WIFI = 4;
    private static final int CONNECTION_TYPE_WWAN_ANY = 3;
    private static final int DATA_DELIVERY_DELAY = 17;
    private static final boolean DEBUG = false;
    protected static final String ESCEP_ERROR = "error";
    protected static final String ESCEP_EVENT = "event";
    private static final int GOODSIGNAL = 32;
    private static final int GPSD_NOT_RECOVERY_FAILED = 21;
    private static final int GPS_ADD_BATCHING_FAILED = 10;
    private static final int GPS_ADD_GEOFENCE_FAILED = 9;
    private static final int GPS_AGPS_DATA_CONNECTED = 3;
    private static final int GPS_AGPS_DATA_CONN_DONE = 4;
    private static final int GPS_AGPS_DATA_CONN_FAILED = 5;
    private static final int GPS_CLOSE_GPS_SWITCH_FAILED = 8;
    private static final int GPS_DAILY_CNT_REPORT = 71;
    private static final int GPS_DAILY_CNT_REPORT_FAILD = 24;
    private static final int GPS_DAILY_UPLOAD = 70;
    private static final int GPS_INIT_FAILED = 24;
    private static final int GPS_IN_DOOR_FAILED = 20;
    private static final int GPS_LOST_POSITION_FAILED = 11;
    private static final int GPS_LOST_POSITION_UNSURE_FAILED = 23;
    private static final int GPS_LOW_SIGNAL_FAILED = 19;
    private static final int GPS_NTP_DLOAD_FAILED = 4;
    private static final int GPS_NTP_WRONG = 25;
    private static final int GPS_OPEN_GPS_SWITCH_FAILED = 7;
    private static final int GPS_PERMISSION_DENIED = 6;
    private static final int GPS_POSITION_MODE_MS_ASSISTED = 2;
    private static final int GPS_POSITION_MODE_MS_BASED = 1;
    private static final int GPS_POSITION_MODE_STANDALONE = 0;
    private static final int GPS_POS_FLOW_ERROR_EVENT = 65;
    private static final int GPS_POS_FLOW_ERROR_EVENT_EX = 68;
    private static final int GPS_POS_START_FAILED = 1;
    private static final int GPS_POS_STOP_FAILED = 2;
    private static final int GPS_POS_TIMEOUT_EVENT = 66;
    private static final int GPS_POS_TIMEOUT_EVENT_EX = 69;
    private static final int GPS_RELEASE_AGPS_DATA_CONN = 2;
    private static final int GPS_REQUEST_AGPS_DATA_CONN = 1;
    private static final int GPS_SET_POS_MODE_FAILED = 5;
    private static final int GPS_STATUS_ENGINE_OFF = 4;
    private static final int GPS_STATUS_ENGINE_ON = 3;
    private static final int GPS_STATUS_SESSION_BEGIN = 1;
    private static final int GPS_STATUS_SESSION_END = 2;
    private static final int GPS_WAKE_LOCK_NOT_RELEASE_FAILED = 12;
    private static final int GPS_XTRA_DLOAD_FAILED = 3;
    private static final int HOTSTART_TIMEOUT = 15;
    protected static final boolean HWFLOW = false;
    private static final int INITGPS = 1;
    private static final String KEY_AGPS_CONN_CNT = "key_agps_conn_cnt:";
    private static final String KEY_AGPS_CONN_FAIL_CNT = "key_agps_conn_fail_cnt:";
    private static final String KEY_ALL_POS_TIME = "key_all_pos_time:";
    private static final String KEY_AUTONAV_CNT = "key_autonavi_cnt:";
    private static final String KEY_AVG_POS_ACC = "key_avg_pos_acc:";
    private static final String KEY_AVG_TTFF = "key_avg_ttff:";
    private static final String KEY_BAIDU_MAP_CNT = "key_baidu_map_cnt:";
    private static final String KEY_CARELAND_CNT = "key_careland_cnt:";
    private static final String KEY_FIRST_FIX_TIMEOUT_CNT = "key_first_fix_timeout_cnt:";
    private static final String KEY_GPS_ERR_UPLOAD_CNT = "key_gps_err_upload_cnt:";
    private static final String KEY_GPS_REQ_CNT = "key_gps_req_cnt:";
    private static final String KEY_GPS_RESTART_CNT = "key_gps_restart_cnt:";
    private static final String KEY_GPS_RF_NORMAL_STATUS = "key_gps_rf_normal_status:";
    private static final String KEY_LOST_POS_IN_PERIOD_CNT = "key_lost_pos_cnt:";
    private static final String KEY_NETWORK_REQ_CNT = "key_network_req_cnt:";
    private static final String KEY_NETWORK_TIMEOUT_CNT = "key_network_timeout_cnt:";
    private static final String KEY_NTP_FLASH_SUCC_CNT = "key_ntp_flash_succ_cnt:";
    private static final String KEY_NTP_MOBILE_FAIL_CNT = "key_ntp_mobile_fail_cnt:";
    private static final String KEY_NTP_REQ_CNT = "key_ntp_req_cnt:";
    private static final String KEY_NTP_WIFI_FAIL_CNT = "key_ntp_wifi_fail_cnt:";
    private static final String KEY_TIMESTAMP = "key_timestamp:";
    private static final String KEY_XTRA_DLOAD_CNT = "key_xtra_dload_cnt:";
    private static final String KEY_XTRA_REQ_CNT = "key_xtra_req_cnt:";
    private static final int MAXSIGNAL = 35;
    private static final int MAX_NUM_TRIGGER_BETA = 5;
    private static final int MAX_NUM_TRIGGER_COMM = 1;
    private static final long MIN_PERIOD_TRIGGER_BETA = 86400000;
    private static final long MIN_PERIOD_TRIGGER_COMM = 604800000;
    private static int MIN_WRITE_STAT_SPAN = 0;
    private static final int NAVIGATION_ABORT = 16;
    private static final int NETWK_POS_TIMEOUT_EVENT = 64;
    private static final int NETWK_POS_TIMEOUT_EVENT_EX = 67;
    private static final int NETWORK_POSITION_TIMEOUT = 22;
    private static final int NORMALSIGNAL = 28;
    private static final int NORMAL_TTFF = 40;
    private static final int OPENGPSSWITCH = 14;
    private static final int PERMISSIONERR = 13;
    private static final int REPORT_SNR_THRESHOLD = 20;
    private static final String SEPARATOR_KEY = "\n";
    private static final int STANDALONE_TIMEOUT = 13;
    private static final int STARTGPS = 2;
    private static final int STOPGPS = 5;
    private static final int SUPL_VALUE = 0;
    private static final String TAG = "HwOldGpsLogServices";
    private static final int UNKNOWN_ISSUE = 0;
    private static final int UPDATEAGPSSTATE = 4;
    private static final int UPDATEAPKNAME = 18;
    private static final int UPDATEGPSRUNSTATE = 6;
    private static final int UPDATELOCATION = 8;
    private static final int UPDATELOSTPOSITION = 17;
    private static final int UPDATENETWORKLOCATION = 0;
    private static final int UPDATENETWORKSTATE = 3;
    private static final int UPDATENTPDLOADSTATUS = 11;
    private static final int UPDATENTPERRORTIME = 19;
    private static final int UPDATESETPOSMODE = 12;
    private static final int UPDATESVSTATUS = 9;
    private static final int UPDATEXTRA = 7;
    private static final int UPDATEXTRADLOADSTATUS = 10;
    private static final int VALIDSIGNAL = 15;
    private static final boolean VERBOSE = false;
    private static final int WIFI_VALUE = 1;
    private static final int WWAN_ANY_VALUE = 3;
    private static final long delayFixtime1 = 15;
    private static final long delayFixtime2 = 45;
    private static String mGpsStateCntFile = null;
    private static String mGpsStatePath = null;
    private static final int mGpsType = 14;
    private static final Object mLock = null;
    private static final HashMap<Integer, HashMap<Integer, String>> mapGpsEventReason = null;
    private static final HashMap<String, TriggerLimit> mapHalDriverEventTriggerFreq = null;
    private static final HashMap<Integer, ChrLogBaseEventModel> mapSaveLogModel = null;
    private TriggerLimit aGpsConnFailTriggerLimit;
    private int a_ucPosTime;
    private byte[] a_ucReportSvNo;
    private int a_ucSvAlmMask;
    private byte[] a_ucSvAzimuths;
    private byte[] a_ucSvElevations;
    private int a_ucSvEphMask;
    private byte[] a_ucSvNo;
    private byte[] a_ucSvSnr;
    private int a_ucSvUseMask;
    private boolean hasFixed;
    private boolean hasJudgeFirstFix;
    private boolean hasNetWorkFix;
    private boolean isGlobalVersion;
    File logFile;
    private int lostPosCnt_OneSession;
    private int mAgpsConnCnt;
    private int mAgpsConnFailedCnt;
    private int mAllPosTime;
    private String mApkName;
    private int mAutoNavi_cnt;
    private int mAvgPosAcc;
    private int mAvgTTFF;
    private int mBaidu_cnt;
    private int mCareland_cnt;
    protected GnssChrCommonInfo mChrComInfo;
    private Context mContext;
    private int mFirstFixTimeoutNum;
    private boolean mGpsChrEnable;
    private int mGpsErrorUploadCnt;
    private int mGpsReqCnt;
    private byte mGpsRfBitMask;
    private Listener mGpsStatusListener;
    private boolean mGpsStopped;
    private Timer mGpsTimer;
    private TimerTask mGpsTimerTask;
    private boolean mGpsdResart;
    private ProviderHandler mHandler;
    private boolean mInjectNtpTimePending;
    private boolean mIsDataDelayRpt;
    private boolean mIsGpsRfNormal;
    private boolean mIsReportErr;
    private boolean mIsWifiType;
    private LocationManager mLocationManager;
    private int mLostPosAllCnt;
    private int mLostPosNum;
    private long mLostPosTime;
    private boolean mMobileDataConnect;
    private boolean mNetAvaiable;
    private Timer mNetTimer;
    private TimerTask mNetTimerTask;
    private boolean mNetworkFixed;
    private int mNetworkReqCnt;
    private int mNetworkTimeOutCnt;
    private int mNtpFlashSuccCnt;
    private int mNtpMobileFailCnt;
    private int mNtpReqCnt;
    private int mNtpWifiFailCnt;
    private boolean mNtpstatus;
    private int mPositionAcc;
    private int mReportAccCnt;
    private long mResumePostime;
    private int mRstartCnt;
    private long mStartFixTime;
    private int mSvBestSvSignalInfo;
    private int mSvCount;
    private int mSvNormalSvSignalInfo;
    private int mSvSignalInfo;
    private HandlerThread mThread;
    private long mTimestamp;
    private long mWriteStatTimestamp;
    private int mXtraDloadCnt;
    private int mXtraReqCnt;
    private TriggerLimit netTimeOutTriggerLimit;
    private TriggerLimit ntpTriggerLimit;
    private int ucAGPSConnReq;
    private byte ucErrorCode;
    private byte ucGpsEngineCap;
    private byte ucGpsRunStatus;
    private byte ucNetworkStatus;
    private int ucPosMethod;
    private TriggerLimit xtraTriggerLimit;

    class ProviderHandler extends Handler {
        private ArrayList list;

        ProviderHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            this.list = (ArrayList) msg.obj;
            switch (msg.what) {
                case HwOldGpsLogServices.UPDATENETWORKLOCATION /*0*/:
                    HwOldGpsLogServices.this.handlerNetWorkLocation(((Boolean) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION)).booleanValue());
                case HwOldGpsLogServices.WIFI_VALUE /*1*/:
                    Boolean isEnable = (Boolean) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION);
                    HwOldGpsLogServices.this.handlerInitGps(isEnable.booleanValue(), ((Byte) this.list.get(HwOldGpsLogServices.WIFI_VALUE)).byteValue());
                case HwOldGpsLogServices.STARTGPS /*2*/:
                    Boolean Enable = (Boolean) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION);
                    HwOldGpsLogServices.this.handlerStartGps(Enable.booleanValue(), ((Integer) this.list.get(HwOldGpsLogServices.WIFI_VALUE)).intValue());
                case HwOldGpsLogServices.WWAN_ANY_VALUE /*3*/:
                    HwOldGpsLogServices.this.handlerUpdateNetworkState((NetworkInfo) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION));
                case HwOldGpsLogServices.UPDATEAGPSSTATE /*4*/:
                    Integer type1 = (Integer) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION);
                    HwOldGpsLogServices.this.handlerUpdateAgpsState(type1.intValue(), ((Integer) this.list.get(HwOldGpsLogServices.WIFI_VALUE)).intValue());
                case HwOldGpsLogServices.STOPGPS /*5*/:
                    HwOldGpsLogServices.this.handlerStopGps(((Boolean) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION)).booleanValue());
                case HwOldGpsLogServices.UPDATEGPSRUNSTATE /*6*/:
                    HwOldGpsLogServices.this.handlerUpdateGpsRunState(((Integer) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION)).intValue());
                case HwOldGpsLogServices.UPDATELOCATION /*8*/:
                    HwOldGpsLogServices.this.handlerUpdateLocation((Location) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION), ((Long) this.list.get(HwOldGpsLogServices.WIFI_VALUE)).longValue());
                case HwOldGpsLogServices.UPDATESVSTATUS /*9*/:
                    int[] svs = (int[]) this.list.get(HwOldGpsLogServices.WIFI_VALUE);
                    float[] snrs = (float[]) this.list.get(HwOldGpsLogServices.STARTGPS);
                    float[] svElevations = (float[]) this.list.get(HwOldGpsLogServices.WWAN_ANY_VALUE);
                    float[] svAzimuths = (float[]) this.list.get(HwOldGpsLogServices.UPDATEAGPSSTATE);
                    int[] SvMasks = (int[]) this.list.get(HwOldGpsLogServices.STOPGPS);
                    HwOldGpsLogServices.this.handlerUpdateSvStatus(((Integer) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION)).intValue(), svs, snrs, svElevations, svAzimuths, SvMasks);
                case HwOldGpsLogServices.UPDATEXTRADLOADSTATUS /*10*/:
                    HwOldGpsLogServices.this.handlerUpdateXtraDloadStatus(((Boolean) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION)).booleanValue());
                case HwOldGpsLogServices.UPDATENTPDLOADSTATUS /*11*/:
                    HwOldGpsLogServices.this.mNtpstatus = ((Boolean) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION)).booleanValue();
                    if (!HwOldGpsLogServices.this.mInjectNtpTimePending) {
                        HwOldGpsLogServices.this.mInjectNtpTimePending = true;
                        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                            public void run() {
                                HwOldGpsLogServices.this.handlerUpdateNtpDloadStatus(HwOldGpsLogServices.this.mNtpstatus);
                                HwOldGpsLogServices.this.mInjectNtpTimePending = HwOldGpsLogServices.VERBOSE;
                            }
                        });
                    }
                case HwOldGpsLogServices.UPDATESETPOSMODE /*12*/:
                    HwOldGpsLogServices.this.handlerUpdateSetPosMode(((Boolean) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION)).booleanValue());
                case HwOldGpsLogServices.STANDALONE_TIMEOUT /*13*/:
                    HwOldGpsLogServices.this.handlerPermissionErr();
                case HwOldGpsLogServices.mGpsType /*14*/:
                    HwOldGpsLogServices.this.handlerOpenGpsSwitchFail(((Integer) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION)).intValue());
                case HwOldGpsLogServices.VALIDSIGNAL /*15*/:
                    HwOldGpsLogServices.this.handlerAddGeofenceFail();
                case HwOldGpsLogServices.NAVIGATION_ABORT /*16*/:
                    HwOldGpsLogServices.this.handlerAddBatchingFail();
                case HwOldGpsLogServices.UPDATELOSTPOSITION /*17*/:
                    HwOldGpsLogServices.this.handlerLostLocation();
                case HwOldGpsLogServices.UPDATEAPKNAME /*18*/:
                    HwOldGpsLogServices.this.handlerUpdateApkName((String) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION));
                case HwOldGpsLogServices.UPDATENTPERRORTIME /*19*/:
                    HwOldGpsLogServices.this.handlerUpdateNtpErrorStatus(((Long) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION)).longValue(), ((Long) this.list.get(HwOldGpsLogServices.UPDATENETWORKLOCATION)).longValue());
                default:
                    if (HwOldGpsLogServices.DEBUG) {
                        int i = msg.what;
                        String str = "====";
                        Log.d(HwOldGpsLogServices.TAG, "====handleMessage: msg.what = " + i + r33);
                    }
            }
        }
    }

    private static class TriggerLimit {
        long lastUploadTime;
        int triggerNum;

        private TriggerLimit() {
        }

        public void reset() {
            this.lastUploadTime = 0;
            this.triggerNum = HwOldGpsLogServices.UPDATENETWORKLOCATION;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.HwOldGpsLogServices.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.HwOldGpsLogServices.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.HwOldGpsLogServices.<clinit>():void");
    }

    HwOldGpsLogServices(HandlerThread thread, Context context) {
        this.a_ucSvNo = new byte[UPDATESETPOSMODE];
        this.a_ucReportSvNo = new byte[UPDATESETPOSMODE];
        this.a_ucSvSnr = new byte[GOODSIGNAL];
        this.a_ucSvElevations = new byte[GOODSIGNAL];
        this.a_ucSvAzimuths = new byte[GOODSIGNAL];
        this.mLostPosAllCnt = UPDATENETWORKLOCATION;
        this.isGlobalVersion = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region")) ? VERBOSE : true;
        this.ucErrorCode = (byte) 0;
        this.mNetTimer = null;
        this.mGpsTimer = null;
        this.mNetTimerTask = null;
        this.mGpsTimerTask = null;
        this.mChrComInfo = new GnssChrCommonInfo();
        this.ntpTriggerLimit = new TriggerLimit();
        this.xtraTriggerLimit = new TriggerLimit();
        this.netTimeOutTriggerLimit = new TriggerLimit();
        this.aGpsConnFailTriggerLimit = new TriggerLimit();
        this.mXtraDloadCnt = UPDATENETWORKLOCATION;
        this.mXtraReqCnt = UPDATENETWORKLOCATION;
        this.mNtpWifiFailCnt = UPDATENETWORKLOCATION;
        this.mNtpMobileFailCnt = UPDATENETWORKLOCATION;
        this.mNtpReqCnt = UPDATENETWORKLOCATION;
        this.mNtpFlashSuccCnt = UPDATENETWORKLOCATION;
        this.mNetworkTimeOutCnt = UPDATENETWORKLOCATION;
        this.mNetworkReqCnt = UPDATENETWORKLOCATION;
        this.mGpsErrorUploadCnt = UPDATENETWORKLOCATION;
        this.mGpsReqCnt = UPDATENETWORKLOCATION;
        this.mRstartCnt = UPDATENETWORKLOCATION;
        this.mAgpsConnFailedCnt = UPDATENETWORKLOCATION;
        this.mAgpsConnCnt = UPDATENETWORKLOCATION;
        this.mAvgTTFF = UPDATENETWORKLOCATION;
        this.mAvgPosAcc = UPDATENETWORKLOCATION;
        this.mAllPosTime = UPDATENETWORKLOCATION;
        this.mIsGpsRfNormal = VERBOSE;
        this.mFirstFixTimeoutNum = UPDATENETWORKLOCATION;
        this.mLostPosNum = UPDATENETWORKLOCATION;
        this.mBaidu_cnt = UPDATENETWORKLOCATION;
        this.mAutoNavi_cnt = UPDATENETWORKLOCATION;
        this.mCareland_cnt = UPDATENETWORKLOCATION;
        this.logFile = new File(mGpsStateCntFile);
        this.mWriteStatTimestamp = 0;
        this.mTimestamp = 0;
        this.mGpsChrEnable = SystemProperties.getBoolean("ro.config.hw_gps_wifi_chr", true);
        this.mGpsStatusListener = new Listener() {
            public void onGpsStatusChanged(int event) {
                GpsStatus gpsstatus = HwOldGpsLogServices.this.mLocationManager.getGpsStatus(null);
                switch (event) {
                    case HwOldGpsLogServices.WWAN_ANY_VALUE /*3*/:
                        HwOldGpsLogServices.this.a_ucPosTime = gpsstatus.getTimeToFirstFix() / IOTController.TYPE_MASTER;
                        Log.d(HwOldGpsLogServices.TAG, "ttff is : " + HwOldGpsLogServices.this.a_ucPosTime);
                        HwOldGpsLogServices.this.mAvgTTFF = (HwOldGpsLogServices.this.a_ucPosTime + HwOldGpsLogServices.this.mAvgTTFF) / HwOldGpsLogServices.STARTGPS;
                    default:
                }
            }
        };
        this.mContext = context;
        Log.d(TAG, "enter HwOldGpsLogServices");
        this.mThread = thread;
        this.mHandler = new ProviderHandler(this.mThread.getLooper());
        this.ntpTriggerLimit.reset();
        this.xtraTriggerLimit.reset();
        this.netTimeOutTriggerLimit.reset();
        this.aGpsConnFailTriggerLimit.reset();
        initGpsEventReasonMap();
    }

    public void netWorkLocation(String provider, ProviderRequest providerRequest) {
        if (!this.mGpsChrEnable) {
            Log.d(TAG, "GPS chr is not enabled!");
        } else if (provider.equalsIgnoreCase("network")) {
            long requestInterval = providerRequest.interval;
            if (requestInterval >= MIN_PERIOD_TRIGGER_BETA) {
                Log.d(TAG, "network location interval is : " + requestInterval + " ,larger than 24 hours,ignore !");
                return;
            }
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(Boolean.valueOf(providerRequest.reportLocation));
            msg.what = UPDATENETWORKLOCATION;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
        }
    }

    public void openGpsSwitchFail(int open) {
        if (this.mGpsChrEnable) {
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(Integer.valueOf(open));
            msg.what = mGpsType;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void initGps(boolean isEnable, byte EngineCapabilities) {
        if (this.mGpsChrEnable) {
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(Boolean.valueOf(isEnable));
            list.add(Byte.valueOf(EngineCapabilities));
            msg.what = WIFI_VALUE;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void updateXtraDloadStatus(boolean status) {
        if (this.mGpsChrEnable) {
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(Boolean.valueOf(status));
            msg.what = UPDATEXTRADLOADSTATUS;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void updateNtpDloadStatus(boolean status) {
        if (this.mGpsChrEnable) {
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(Boolean.valueOf(status));
            msg.what = UPDATENTPDLOADSTATUS;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void updateSetPosMode(boolean status) {
        if (this.mGpsChrEnable) {
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(Boolean.valueOf(status));
            msg.what = UPDATESETPOSMODE;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void updateApkName(String name) {
        if (this.mGpsChrEnable) {
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(name);
            msg.what = UPDATEAPKNAME;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void startGps(boolean isEnable, int PositionMode) {
        if (this.mGpsChrEnable) {
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(Boolean.valueOf(isEnable));
            list.add(Integer.valueOf(PositionMode));
            msg.what = STARTGPS;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void updateNetworkState(NetworkInfo info) {
        if (this.mGpsChrEnable) {
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(info);
            msg.what = WWAN_ANY_VALUE;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void updateAgpsState(int type, int state) {
        if (this.mGpsChrEnable) {
            if (DEBUG) {
                Log.d(TAG, "updateAgpsState: ");
            }
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(Integer.valueOf(type));
            list.add(Integer.valueOf(state));
            msg.what = UPDATEAGPSSTATE;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void stopGps(boolean status) {
        if (this.mGpsChrEnable) {
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(Boolean.valueOf(status));
            msg.what = STOPGPS;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void permissionErr() {
        if (this.mGpsChrEnable) {
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            msg.what = STANDALONE_TIMEOUT;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void addGeofenceStatus() {
        if (this.mGpsChrEnable) {
            Message msg = new Message();
            msg.what = VALIDSIGNAL;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void addBatchingStatus() {
        if (this.mGpsChrEnable) {
            Message msg = new Message();
            msg.what = NAVIGATION_ABORT;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void updateGpsRunState(int status) {
        if (this.mGpsChrEnable) {
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(Integer.valueOf(status));
            msg.what = UPDATEGPSRUNSTATE;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void updateLocation(Location location, long time, String provider) {
        if (this.mGpsChrEnable) {
            if (!this.hasFixed) {
                this.hasFixed = true;
            }
            if (provider.equalsIgnoreCase("network")) {
                if (!this.mNetworkFixed) {
                    this.mNetworkFixed = true;
                }
            } else if (provider.equalsIgnoreCase("gps")) {
                ArrayList list = new ArrayList();
                Message msg = new Message();
                list.clear();
                list.add(location);
                list.add(Long.valueOf(time));
                msg.what = UPDATELOCATION;
                msg.obj = list;
                this.mHandler.sendMessage(msg);
                this.mHandler.removeMessages(UPDATELOSTPOSITION);
                this.mHandler.sendEmptyMessageDelayed(UPDATELOSTPOSITION, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
            }
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void updateSvStatus(int svCount, int[] svs, float[] snrs, float[] svElevations, float[] svAzimuths, int[] SvMasks) {
        if (this.mGpsChrEnable) {
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(Integer.valueOf(svCount));
            list.add(svs);
            list.add(snrs);
            list.add(svElevations);
            list.add(svAzimuths);
            list.add(SvMasks);
            msg.what = UPDATESVSTATUS;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    public void reportErrorNtpTime(long currentNtpTime, long realTime) {
        if (this.mGpsChrEnable) {
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(Long.valueOf(currentNtpTime));
            list.add(Long.valueOf(realTime));
            msg.what = UPDATENTPERRORTIME;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "GPS chr is not enabled!");
    }

    private void handlerUpdateNtpErrorStatus(long ntpTime, long realTime) {
        this.ucErrorCode = (byte) 25;
        writeNETInfo((int) GPS_POS_FLOW_ERROR_EVENT, (boolean) VERBOSE);
    }

    private void handlerNetWorkLocation(boolean enable) {
        if (this.mNetAvaiable) {
            if (DEBUG) {
                Log.d(TAG, "handlerNetWorkLocation");
            }
            if (!enable) {
                stopNetTimer();
                this.hasNetWorkFix = VERBOSE;
            } else if (this.hasNetWorkFix) {
                Log.e(TAG, "Network pos is already runing!");
                return;
            } else {
                this.hasNetWorkFix = true;
                this.mNetworkFixed = VERBOSE;
                this.mNetworkReqCnt += WIFI_VALUE;
                if (this.mNetTimer == null) {
                    this.mNetTimer = new Timer();
                }
                if (this.mNetTimerTask != null) {
                    this.mNetTimerTask.cancel();
                    this.mNetTimerTask = null;
                }
                this.mNetTimerTask = new TimerTask() {
                    public void run() {
                        if (!HwOldGpsLogServices.this.mNetworkFixed) {
                            Log.e(HwOldGpsLogServices.TAG, "network position over 10s");
                            HwOldGpsLogServices.this.ucErrorCode = (byte) 22;
                            HwOldGpsLogServices.this.writeNETInfo((int) HwOldGpsLogServices.NETWK_POS_TIMEOUT_EVENT, (boolean) HwOldGpsLogServices.VERBOSE);
                        }
                    }
                };
                try {
                    this.mNetTimer.schedule(this.mNetTimerTask, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "TimerTask is scheduled already !");
                }
                writeGpsCHRState(VERBOSE, true);
            }
            return;
        }
        Log.e(TAG, "Network not avaiable !");
    }

    private void handlerInitGps(boolean isEnable, byte EngineCapabilities) {
        this.ucGpsEngineCap = EngineCapabilities;
        if (DEBUG) {
            Log.d(TAG, "handlerInitGps ,isEnable = " + isEnable + " ,EngineCapabilities = " + this.ucGpsEngineCap);
        }
        if (isEnable) {
            readGPSCHRStat();
        } else {
            this.ucErrorCode = (byte) 24;
            writeNETInfo((int) GPS_POS_FLOW_ERROR_EVENT, true);
        }
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        this.mLocationManager.addGpsStatusListener(this.mGpsStatusListener);
    }

    private void handlerStartGps(boolean isEnable, int PositionMode) {
        if (DEBUG) {
            Log.d(TAG, "handlerStartGps: isEnable : " + isEnable + " , PositionMode : " + PositionMode);
        }
        this.mStartFixTime = System.currentTimeMillis();
        this.mGpsReqCnt += WIFI_VALUE;
        this.hasFixed = VERBOSE;
        this.mGpsStopped = VERBOSE;
        this.hasJudgeFirstFix = VERBOSE;
        this.mIsReportErr = VERBOSE;
        this.mIsDataDelayRpt = VERBOSE;
        this.mGpsdResart = VERBOSE;
        this.lostPosCnt_OneSession = UPDATENETWORKLOCATION;
        this.mResumePostime = 0;
        this.mPositionAcc = UPDATENETWORKLOCATION;
        this.mReportAccCnt = UPDATENETWORKLOCATION;
        this.ucAGPSConnReq = UPDATENETWORKLOCATION;
        switch (PositionMode) {
            case UPDATENETWORKLOCATION /*0*/:
                this.ucPosMethod = UPDATENETWORKLOCATION;
                break;
            case WIFI_VALUE /*1*/:
                this.ucPosMethod = WIFI_VALUE;
                break;
            case STARTGPS /*2*/:
                this.ucPosMethod = STARTGPS;
                break;
            default:
                if (DEBUG) {
                    Log.d(TAG, "handlerStartGps, no PositionMode case matched!");
                    break;
                }
                break;
        }
        if (!isEnable) {
            Log.e(TAG, "start gps failed,pos mode is " + PositionMode);
            if (!this.mIsReportErr) {
                this.mIsReportErr = true;
                this.mGpsErrorUploadCnt += WIFI_VALUE;
            }
            this.ucErrorCode = (byte) 1;
            writeNETInfo((int) GPS_POS_FLOW_ERROR_EVENT, true);
        }
        writeGpsCHRState(VERBOSE, true);
    }

    private void handlerStopGps(boolean status) {
        if (!status) {
            Log.e(TAG, "Stop GPS failed !");
            if (!this.mIsReportErr) {
                this.mIsReportErr = true;
                this.mGpsErrorUploadCnt += WIFI_VALUE;
            }
            this.ucErrorCode = (byte) 2;
            writeNETInfo((int) GPS_POS_FLOW_ERROR_EVENT, true);
        }
        stopGpsTimer();
        this.mHandler.removeMessages(UPDATELOSTPOSITION);
        this.mGpsStopped = true;
        this.mLostPosAllCnt += this.lostPosCnt_OneSession;
        int diffpostime2 = (int) ((System.currentTimeMillis() - this.mStartFixTime) / 1000);
        this.mAllPosTime += diffpostime2;
        this.mAvgPosAcc = (this.mAvgPosAcc + this.mPositionAcc) / STARTGPS;
        if (DEBUG) {
            Log.d(TAG, "mAllPosTime is :" + this.mAllPosTime + " ,diffpostime to int is " + diffpostime2);
        }
        writeGpsCHRState(VERBOSE, true);
    }

    private String formateTimeToDate(long time) {
        if (time == 0) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
    }

    private void handlerPermissionErr() {
        Log.e(TAG, "GPS permission denied!");
    }

    private void handlerOpenGpsSwitchFail(int open) {
        if (open == WIFI_VALUE) {
            Log.e(TAG, "Open gps switch fail");
            this.ucErrorCode = (byte) 7;
        } else if (open == STARTGPS) {
            Log.e(TAG, "close gps switch fail");
            this.ucErrorCode = (byte) 8;
        }
        writeNETInfo((int) GPS_POS_FLOW_ERROR_EVENT, (boolean) VERBOSE);
    }

    private void handlerAddGeofenceFail() {
        Log.e(TAG, "add geofence fail");
        this.ucErrorCode = (byte) 9;
        writeNETInfo((int) GPS_POS_FLOW_ERROR_EVENT, (boolean) VERBOSE);
    }

    private void handlerAddBatchingFail() {
        Log.e(TAG, "add batching fail");
        this.ucErrorCode = (byte) 10;
        writeNETInfo((int) GPS_POS_FLOW_ERROR_EVENT, (boolean) VERBOSE);
    }

    private void handlerUpdateXtraDloadStatus(boolean status) {
        if (this.mNetAvaiable) {
            if (DEBUG) {
                Log.d(TAG, "handlerUpdateXtraDloadStatus:" + status);
            }
            if (status) {
                this.mXtraDloadCnt += WIFI_VALUE;
            } else {
                Log.e(TAG, "Download xtra data failed");
                this.ucErrorCode = (byte) 3;
                writeNETInfo((int) GPS_POS_FLOW_ERROR_EVENT, (boolean) VERBOSE);
            }
            this.mXtraReqCnt += WIFI_VALUE;
            writeGpsCHRState(VERBOSE, true);
            return;
        }
        Log.e(TAG, "handlerUpdateXtraDloadStatus , Network not avaiable !");
    }

    private void handlerUpdateNtpDloadStatus(boolean status) {
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateNtpDloadStatus:" + status);
        }
        if (this.mNetAvaiable) {
            if (status) {
                this.mNtpFlashSuccCnt += WIFI_VALUE;
            } else if (WIFI_VALUE == System.getInt(this.mContext.getContentResolver(), "CtrlSocketSaving", UPDATENETWORKLOCATION)) {
                Log.i(TAG, "power saving mode has started, ignore ntp download fail issue!");
                return;
            } else {
                if (!isRealConnNetwork()) {
                    if (this.mIsWifiType) {
                        this.mNtpWifiFailCnt += WIFI_VALUE;
                        Log.d(TAG, "NTP,wifi network can not reachedable!");
                    } else {
                        this.mNtpMobileFailCnt += WIFI_VALUE;
                        Log.d(TAG, "NTP,mobile network can not reachedable!");
                    }
                }
                Log.e(TAG, "Download ntp data failed");
                this.ucErrorCode = (byte) 4;
                writeNETInfo((int) GPS_POS_FLOW_ERROR_EVENT, (boolean) VERBOSE);
            }
            this.mNtpReqCnt += WIFI_VALUE;
            writeGpsCHRState(VERBOSE, true);
        }
    }

    private void handlerUpdateSetPosMode(boolean status) {
        if (!status) {
            Log.e(TAG, "set pos mode failed");
            this.ucErrorCode = (byte) 5;
            writeNETInfo((int) GPS_POS_FLOW_ERROR_EVENT, true);
            writeGpsCHRState(VERBOSE, true);
        }
    }

    private void handlerUpdateApkName(String name) {
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateApkName: " + name);
        }
        this.mApkName = name;
        if (this.mApkName.indexOf(NetworkCheckerThread.SERVER_BAIDU) != -1) {
            this.mBaidu_cnt += WIFI_VALUE;
            if (DEBUG) {
                Log.d(TAG, " Baidu map !");
            }
        } else if (this.mApkName.indexOf("minimap") != -1) {
            this.mAutoNavi_cnt += WIFI_VALUE;
            if (DEBUG) {
                Log.d(TAG, " minimap map !");
            }
        } else if (this.mApkName.indexOf("cld.navi") != -1) {
            this.mCareland_cnt += WIFI_VALUE;
            if (DEBUG) {
                Log.d(TAG, " kailide map !");
            }
        }
    }

    private void handlerUpdateNetworkState(NetworkInfo info) {
        this.ucNetworkStatus = (byte) 0;
        if (info != null) {
            this.mNetAvaiable = info.isAvailable();
            if (this.mNetAvaiable) {
                if (WIFI_VALUE == info.getType()) {
                    this.mIsWifiType = true;
                    this.ucNetworkStatus = (byte) (this.ucNetworkStatus | STARTGPS);
                } else {
                    this.mIsWifiType = VERBOSE;
                }
                switch (info.getSubtype()) {
                    case STARTGPS /*2*/:
                        this.ucNetworkStatus = (byte) (this.ucNetworkStatus | UPDATEAGPSSTATE);
                        break;
                    case WWAN_ANY_VALUE /*3*/:
                        this.ucNetworkStatus = (byte) (this.ucNetworkStatus | UPDATELOCATION);
                        break;
                    case UPDATEAGPSSTATE /*4*/:
                        this.ucNetworkStatus = (byte) (this.ucNetworkStatus | NAVIGATION_ABORT);
                        break;
                    case UPDATELOCATION /*8*/:
                        this.ucNetworkStatus = (byte) (this.ucNetworkStatus | GOODSIGNAL);
                        break;
                    case STANDALONE_TIMEOUT /*13*/:
                        this.ucNetworkStatus = (byte) (this.ucNetworkStatus | NETWK_POS_TIMEOUT_EVENT);
                        break;
                    default:
                        Log.d(TAG, "handlerUpdateNetworkState, no nework type case matched! Type is : " + info.getSubtype());
                        break;
                }
            }
            return;
        }
        this.ucNetworkStatus = (byte) (this.ucNetworkStatus | WIFI_VALUE);
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateNetworkState:" + this.ucNetworkStatus);
        }
    }

    private void handlerUpdateAgpsState(int type, int state) {
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateAgpsState:" + type + " ,state is: " + state);
        }
        switch (type) {
            case UPDATENETWORKLOCATION /*0*/:
                this.ucAGPSConnReq = UPDATEAGPSSTATE;
                break;
            case WIFI_VALUE /*1*/:
                this.ucAGPSConnReq = UPDATENETWORKLOCATION;
                break;
            case STARTGPS /*2*/:
                this.ucAGPSConnReq = STARTGPS;
                break;
            case WWAN_ANY_VALUE /*3*/:
                this.ucAGPSConnReq = WWAN_ANY_VALUE;
                break;
            case UPDATEAGPSSTATE /*4*/:
                this.ucAGPSConnReq = WIFI_VALUE;
                break;
            default:
                if (DEBUG) {
                    Log.d(TAG, "handlerUpdateAgpsState, no  type case matched!");
                    break;
                }
                break;
        }
        switch (state) {
            case WIFI_VALUE /*1*/:
                this.mAgpsConnCnt += WIFI_VALUE;
                break;
            case STARTGPS /*2*/:
            case WWAN_ANY_VALUE /*3*/:
            case UPDATEAGPSSTATE /*4*/:
                break;
            case STOPGPS /*5*/:
                Log.e(TAG, "agps conn failed");
                this.mAgpsConnFailedCnt += WIFI_VALUE;
                this.ucErrorCode = (byte) 18;
                writeNETInfo((int) GPS_POS_FLOW_ERROR_EVENT, true);
                break;
            default:
                if (DEBUG) {
                    Log.d(TAG, "handlerUpdateAgpsState, no  state case matched!");
                    break;
                }
                break;
        }
        writeGpsCHRState(VERBOSE, true);
    }

    private void handlerUpdateGpsRunState(int status) {
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateGpsRunState:" + status);
        }
        switch (status) {
            case WIFI_VALUE /*1*/:
                this.ucGpsRunStatus = (byte) 0;
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | WIFI_VALUE);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | UPDATEAGPSSTATE);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | NAVIGATION_ABORT);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | GOODSIGNAL);
            case STARTGPS /*2*/:
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | STARTGPS);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus & -17);
            case WWAN_ANY_VALUE /*3*/:
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | UPDATEAGPSSTATE);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | GOODSIGNAL);
            case UPDATEAGPSSTATE /*4*/:
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | STARTGPS);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | UPDATELOCATION);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus & -17);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus & -33);
            default:
                if (DEBUG) {
                    Log.d(TAG, "handlerUpdateGpsRunState, no  status case matched!");
                }
        }
    }

    private void handlerUpdateLocation(Location location, long time) {
        int diffTime = (int) TimeUnit.NANOSECONDS.toSeconds(time - location.getElapsedRealtimeNanos());
        getBsetPositionAccuracy((int) location.getAccuracy());
        if (DEBUG) {
            Log.d(TAG, "mPositionAcc is : " + this.mPositionAcc + ",difftime in secs is : " + diffTime);
        }
        if (this.lostPosCnt_OneSession > 0) {
            this.mResumePostime = System.currentTimeMillis();
        }
        if (diffTime > WIFI_VALUE) {
            Log.e(TAG, "delivering  postion data is delayed in framework layer ,difftime is : " + diffTime);
            if (!this.mIsReportErr) {
                this.mIsReportErr = true;
                this.mGpsErrorUploadCnt += WIFI_VALUE;
            }
            if (!this.mIsDataDelayRpt) {
                this.mIsDataDelayRpt = true;
                this.ucErrorCode = (byte) 17;
                writeNETInfo((int) GPS_POS_TIMEOUT_EVENT, (boolean) VERBOSE);
            }
        }
    }

    private void getBsetPositionAccuracy(int posAcc) {
        if (this.mReportAccCnt < UPDATEXTRADLOADSTATUS) {
            this.mReportAccCnt += WIFI_VALUE;
            if (this.mPositionAcc == 0) {
                this.mPositionAcc = posAcc;
            } else if (this.mPositionAcc > posAcc) {
                this.mPositionAcc = posAcc;
            }
        }
    }

    private long getGpsReqTime() {
        NetworkInfo networkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(UPDATENETWORKLOCATION);
        if (networkInfo == null) {
            if (DEBUG) {
                Log.d(TAG, "TYPE_MOBILE is not supported, maybe a wifi-only device");
            }
            return delayFixtime2;
        }
        this.mMobileDataConnect = networkInfo.isConnected();
        if (this.mMobileDataConnect) {
            return delayFixtime1;
        }
        return delayFixtime2;
    }

    private void stopGpsTimer() {
        if (DEBUG) {
            Log.d(TAG, "stopGpsTimer ");
        }
        if (this.mGpsTimer != null) {
            this.mGpsTimer.cancel();
            this.mGpsTimer.purge();
            this.mGpsTimer = null;
        }
        if (this.mGpsTimerTask != null) {
            this.mGpsTimerTask.cancel();
            this.mGpsTimerTask = null;
        }
    }

    private void stopNetTimer() {
        if (DEBUG) {
            Log.d(TAG, "stopNetTimer ");
        }
        if (this.mNetTimer != null) {
            this.mNetTimer.cancel();
            this.mNetTimer.purge();
            this.mNetTimer = null;
        }
        if (this.mNetTimerTask != null) {
            this.mNetTimerTask.cancel();
            this.mNetTimerTask = null;
        }
    }

    private void handlerCommercialFirstFixTimeOUT() {
        if (DEBUG) {
            Log.d(TAG, "Enter handlerCommercialFirstFixTimeOUT ");
        }
        this.hasJudgeFirstFix = true;
        handlerCommercialTimeoutFirstTimeOutImp(getGpsReqTime());
    }

    private void handlerCommercialTimeoutFirstTimeOutImp(long delaytime) {
        if (DEBUG) {
            Log.d(TAG, "Enter handlerCommercialTimeoutFirstTimeOutImp,delay time is " + delaytime);
        }
        if (this.mGpsTimer == null) {
            this.mGpsTimer = new Timer();
        }
        if (this.mGpsTimerTask != null) {
            this.mGpsTimerTask.cancel();
            this.mGpsTimerTask = null;
        }
        this.mGpsTimerTask = new TimerTask() {
            public void run() {
                if (!HwOldGpsLogServices.this.hasFixed && !HwOldGpsLogServices.this.mGpsStopped) {
                    HwOldGpsLogServices hwOldGpsLogServices;
                    if (!HwOldGpsLogServices.this.mIsReportErr) {
                        HwOldGpsLogServices.this.mIsReportErr = true;
                        hwOldGpsLogServices = HwOldGpsLogServices.this;
                        hwOldGpsLogServices.mGpsErrorUploadCnt = hwOldGpsLogServices.mGpsErrorUploadCnt + HwOldGpsLogServices.WIFI_VALUE;
                    }
                    if (HwOldGpsLogServices.this.mMobileDataConnect) {
                        HwOldGpsLogServices.this.ucErrorCode = (byte) 14;
                    } else {
                        HwOldGpsLogServices.this.ucErrorCode = (byte) 13;
                    }
                    hwOldGpsLogServices = HwOldGpsLogServices.this;
                    hwOldGpsLogServices.mFirstFixTimeoutNum = hwOldGpsLogServices.mFirstFixTimeoutNum + HwOldGpsLogServices.WIFI_VALUE;
                    HwOldGpsLogServices.this.writeNETInfo((int) HwOldGpsLogServices.GPS_POS_TIMEOUT_EVENT, true);
                }
            }
        };
        try {
            this.mGpsTimer.schedule(this.mGpsTimerTask, 1000 * delaytime);
        } catch (IllegalStateException e) {
            Log.e(TAG, "TimerTask is scheduled already !");
        }
    }

    private void handlerLostLocation() {
        Log.e(TAG, "no position report in 10s");
        if (this.lostPosCnt_OneSession > WWAN_ANY_VALUE || this.mGpsStopped) {
            Log.e(TAG, "lost pos again,lost num is in this session : " + this.lostPosCnt_OneSession + ",or gps navigation status is : " + this.mGpsStopped);
            return;
        }
        if (this.mSvCount > 0) {
            if (this.mSvNormalSvSignalInfo < UPDATEAGPSSTATE) {
                this.ucErrorCode = (byte) 19;
                this.mLostPosNum += WIFI_VALUE;
                Log.d(TAG, "handlerLostLocation , GPS_LOW_SIGNAl");
            } else if (this.mSvBestSvSignalInfo > WIFI_VALUE) {
                this.lostPosCnt_OneSession += WIFI_VALUE;
                if (!this.mIsReportErr) {
                    this.mIsReportErr = true;
                    this.mGpsErrorUploadCnt += WIFI_VALUE;
                }
                this.ucErrorCode = BluetoothMessage.NOTIFY_VAL_OFFS;
                Log.d(TAG, "handlerLostLocation , GPS_LOST_POSITION_FAILED");
            } else {
                this.ucErrorCode = (byte) 23;
                Log.d(TAG, "catch num of normal cn0(28db) SVs > 4,but num of best cno(32) is less than 1! ");
            }
            Log.d(TAG, "mSvCount is : " + this.mSvCount + "NormalSv num is : " + this.mSvNormalSvSignalInfo + "BestSv num is : " + this.mSvBestSvSignalInfo);
        } else if (this.mGpsdResart) {
            this.lostPosCnt_OneSession += WIFI_VALUE;
            if (!this.mIsReportErr) {
                this.mIsReportErr = true;
                this.mGpsErrorUploadCnt += WIFI_VALUE;
            }
            this.ucErrorCode = (byte) 21;
            Log.d(TAG, "handlerLostLocation , GPSD_NOT_RECOVERY_FAILED");
        } else {
            this.ucErrorCode = (byte) 20;
            Log.d(TAG, "handlerLostLocation , GPS_IN_DOOR_FAILED");
        }
        this.mLostPosTime = System.currentTimeMillis();
        writeNETInfo((int) GPS_POS_FLOW_ERROR_EVENT, true);
        writeGpsCHRState(VERBOSE, true);
    }

    private void handlerUpdateSvStatus(int svCount, int[] svs, float[] snrs, float[] svElevations, float[] svAzimuths, int[] SvMasks) {
        if (!this.mGpsStopped) {
            if (DEBUG) {
                Log.d(TAG, "handlerUpdateSvStatus ,SV counts is :" + svCount);
            }
            this.mSvCount = svCount;
            upSV(this.a_ucSvNo, svs);
            upSVWithSnr(svCount, this.a_ucReportSvNo, svs, snrs);
            this.a_ucSvSnr = new byte[GOODSIGNAL];
            this.a_ucSvElevations = new byte[GOODSIGNAL];
            this.a_ucSvAzimuths = new byte[GOODSIGNAL];
            this.mSvSignalInfo = UPDATENETWORKLOCATION;
            this.mSvNormalSvSignalInfo = UPDATENETWORKLOCATION;
            this.mSvBestSvSignalInfo = UPDATENETWORKLOCATION;
            int i = UPDATENETWORKLOCATION;
            while (i < svCount) {
                this.a_ucSvSnr[i] = (byte) ((int) snrs[i]);
                this.a_ucSvElevations[i] = (byte) ((int) svElevations[i]);
                this.a_ucSvAzimuths[i] = (byte) ((int) svAzimuths[i]);
                if (DEBUG) {
                    Log.d(TAG, "handlerUpdateSvStatus ,SV NUM : " + svs[i] + " ,CNO : " + snrs[i]);
                }
                if (snrs[i] > 15.0f) {
                    this.mSvSignalInfo += WIFI_VALUE;
                    if (snrs[i] > 28.0f) {
                        this.mSvNormalSvSignalInfo += WIFI_VALUE;
                        if (snrs[i] > 32.0f) {
                            this.mSvBestSvSignalInfo += WIFI_VALUE;
                            if (!this.mIsGpsRfNormal && snrs[i] > 35.0f) {
                                this.mIsGpsRfNormal = true;
                            }
                        }
                    }
                }
                i += WIFI_VALUE;
            }
            if (this.mSvNormalSvSignalInfo > UPDATEAGPSSTATE && this.mSvBestSvSignalInfo > STARTGPS && !this.hasFixed && !this.hasJudgeFirstFix) {
                handlerCommercialFirstFixTimeOUT();
            }
            this.a_ucSvEphMask = SvMasks[UPDATENETWORKLOCATION];
            this.a_ucSvAlmMask = SvMasks[WIFI_VALUE];
            this.a_ucSvUseMask = SvMasks[STARTGPS];
        }
    }

    private void upSVWithSnr(int svCount, byte[] b, int[] svs, float[] snrs) {
        for (int i = UPDATENETWORKLOCATION; i < svCount; i += WIFI_VALUE) {
            int svIndex = svs[i];
            if (snrs[i] >= 20.0f) {
                if (svIndex <= GOODSIGNAL) {
                    putSV(b, UPDATENETWORKLOCATION, svIndex);
                } else if (GPS_POS_FLOW_ERROR_EVENT <= svIndex && svIndex <= 88) {
                    putSV(b, UPDATEAGPSSTATE, svIndex - 65);
                } else if (WifiProCommonUtils.HTTP_REACHALBE_HOME <= svIndex && svIndex <= 232) {
                    putSV(b, UPDATEXTRA, svIndex - 200);
                }
            }
        }
    }

    private void upSV(byte[] b, int[] svs) {
        int length = svs.length;
        for (int i = UPDATENETWORKLOCATION; i < length; i += WIFI_VALUE) {
            int svIndex = svs[i];
            if (svIndex <= GOODSIGNAL) {
                putSV(b, UPDATENETWORKLOCATION, svIndex);
            } else if (GPS_POS_FLOW_ERROR_EVENT <= svIndex && svIndex <= 88) {
                putSV(b, UPDATEAGPSSTATE, svIndex - 65);
            } else if (WifiProCommonUtils.HTTP_REACHALBE_HOME <= svIndex && svIndex <= 232) {
                putSV(b, UPDATEXTRA, svIndex - 200);
            }
        }
    }

    private void putSV(byte[] b, int index, int i) {
        int residue = i % UPDATELOCATION;
        if (i != 0) {
            int index2;
            if (residue != 0) {
                index2 = i / UPDATELOCATION;
            } else {
                index2 = (i / UPDATELOCATION) - 1;
                residue = UPDATELOCATION;
            }
            index += index2;
            b[index] = (byte) (b[index] | ((byte) ((int) Math.pow(2.0d, (double) (residue - 1)))));
        }
    }

    private boolean checkUpload(TriggerLimit triggerlimit, int triggerFreq, long now) {
        if (now - triggerlimit.lastUploadTime > MIN_PERIOD_TRIGGER_BETA) {
            triggerlimit.triggerNum = UPDATENETWORKLOCATION;
        } else if (triggerlimit.triggerNum > triggerFreq) {
            return VERBOSE;
        }
        triggerlimit.triggerNum += WIFI_VALUE;
        triggerlimit.lastUploadTime = now;
        return true;
    }

    private boolean matchCommercialTrigger(boolean commercialUser, int triggerFreq, int type) {
        long now = SystemClock.elapsedRealtime();
        if (type == GPS_DAILY_CNT_REPORT) {
            this.ucErrorCode = (byte) 24;
        }
        switch (this.ucErrorCode) {
            case WIFI_VALUE /*1*/:
            case STARTGPS /*2*/:
            case STOPGPS /*5*/:
            case UPDATEXTRA /*7*/:
            case UPDATELOCATION /*8*/:
            case UPDATESVSTATUS /*9*/:
            case UPDATEXTRADLOADSTATUS /*10*/:
            case STANDALONE_TIMEOUT /*13*/:
            case mGpsType /*14*/:
            case UPDATELOSTPOSITION /*17*/:
            case REPORT_SNR_THRESHOLD /*20*/:
            case GPSD_NOT_RECOVERY_FAILED /*21*/:
            case GPS_INIT_FAILED /*24*/:
                break;
            case WWAN_ANY_VALUE /*3*/:
                if (!checkUpload(this.xtraTriggerLimit, triggerFreq, now)) {
                    return VERBOSE;
                }
                break;
            case UPDATEAGPSSTATE /*4*/:
                if (!checkUpload(this.ntpTriggerLimit, triggerFreq, now)) {
                    return VERBOSE;
                }
                break;
            case UPDATEAPKNAME /*18*/:
                if (!checkUpload(this.aGpsConnFailTriggerLimit, triggerFreq, now)) {
                    return VERBOSE;
                }
                break;
            case NETWORK_POSITION_TIMEOUT /*22*/:
                if (!checkUpload(this.netTimeOutTriggerLimit, triggerFreq, now)) {
                    return VERBOSE;
                }
                break;
            default:
                Log.d(TAG, "matchCommercialTrigger: ucErrorCode: = " + this.ucErrorCode);
                break;
        }
        return true;
    }

    private void writeNETInfo(int type, boolean ChipLogEnable) {
        int msg_type = WIFI_VALUE;
        boolean commercialUser = GnssLogManager.getInstance().isCommercialUser();
        int triggerFreq = commercialUser ? WIFI_VALUE : STOPGPS;
        ChrLogBaseModel chrLogBaseModel = null;
        Date date = new Date();
        if (matchCommercialTrigger(commercialUser, triggerFreq, type)) {
            if (ChipLogEnable) {
                Log.d(TAG, "need to catch gpslog,not used now");
            }
            switch (type) {
                case NETWK_POS_TIMEOUT_EVENT /*64*/:
                    this.mNetworkTimeOutCnt += WIFI_VALUE;
                    ChrLogBaseModel cCSegEVENT_NETWK_POS_TIMEOUT_EVENT = new CSegEVENT_NETWK_POS_TIMEOUT_EVENT();
                    cCSegEVENT_NETWK_POS_TIMEOUT_EVENT.ucErrorCode.setValue(this.ucErrorCode);
                    cCSegEVENT_NETWK_POS_TIMEOUT_EVENT.tmTimeStamp.setValue(date);
                    cCSegEVENT_NETWK_POS_TIMEOUT_EVENT.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    cCSegEVENT_NETWK_POS_TIMEOUT_EVENT.ucNetworkStatus.setValue(this.ucNetworkStatus);
                    cCSegEVENT_NETWK_POS_TIMEOUT_EVENT.strApkName.setValue(this.mApkName);
                    chrLogBaseModel = cCSegEVENT_NETWK_POS_TIMEOUT_EVENT;
                    break;
                case GPS_POS_FLOW_ERROR_EVENT /*65*/:
                    ChrLogBaseModel cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT = new CSegEVENT_GPS_POS_FLOW_ERROR_EVENT();
                    cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT.ucErrorCode.setValue(this.ucErrorCode);
                    cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT.tmTimeStamp.setValue(date);
                    cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT.ia_ucPosTime.setValue(this.a_ucPosTime);
                    cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT.ucPosMethod.setValue(this.ucPosMethod);
                    cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT.ucNetworkStatus.setValue(this.ucNetworkStatus);
                    cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT.ucGpsEngineCap.setValue(this.ucGpsEngineCap);
                    cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT.ucGpsRunStatus.setValue(this.ucGpsRunStatus);
                    cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT.ucAGPSConnReq.setValue(this.ucAGPSConnReq);
                    cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT.strStartFixTime.setValue(formateTimeToDate(this.mStartFixTime));
                    cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT.strLostPosTime.setValue(formateTimeToDate(this.mLostPosTime));
                    cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT.strResumePostime.setValue(formateTimeToDate(this.mResumePostime));
                    cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT.lPositionAcc.setValue(this.mPositionAcc);
                    cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT.strApkName.setValue(this.mApkName);
                    chrLogBaseModel = cCSegEVENT_GPS_POS_FLOW_ERROR_EVENT;
                    break;
                case GPS_POS_TIMEOUT_EVENT /*66*/:
                    ChrLogBaseModel cCSegEVENT_GPS_POS_TIMEOUT_EVENT = new CSegEVENT_GPS_POS_TIMEOUT_EVENT();
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.ucErrorCode.setValue(this.ucErrorCode);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.tmTimeStamp.setValue(date);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.ia_ucPosTime.setValue(this.a_ucPosTime);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.ucPosMethod.setValue(this.ucPosMethod);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.ucNetworkStatus.setValue(this.ucNetworkStatus);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.ucGpsEngineCap.setValue(this.ucGpsEngineCap);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.ucGpsRunStatus.setValue(this.ucGpsRunStatus);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.ucAGPSConnReq.setValue(this.ucAGPSConnReq);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.ia_ucSvEphMask.setValue(this.a_ucSvEphMask);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.ia_ucSvAlmMask.setValue(this.a_ucSvAlmMask);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.ia_ucSvUseMask.setValue(this.a_ucSvUseMask);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.auca_ucSvNo.setValue(this.a_ucSvNo);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.auca_ucSvSnr.setValue(this.a_ucSvSnr);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.auca_ucSvElevations.setValue(this.a_ucSvElevations);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.aucSvAzimuths.setValue(this.a_ucSvAzimuths);
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.strStartFixTime.setValue(formateTimeToDate(this.mStartFixTime));
                    cCSegEVENT_GPS_POS_TIMEOUT_EVENT.strApkName.setValue(this.mApkName);
                    chrLogBaseModel = cCSegEVENT_GPS_POS_TIMEOUT_EVENT;
                    break;
                case GPS_DAILY_CNT_REPORT /*71*/:
                    msg_type = STARTGPS;
                    ChrLogBaseModel cCSegEVENT_GPS_DAILY_CNT_REPORT = new CSegEVENT_GPS_DAILY_CNT_REPORT();
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.tmTimeStamp.setValue(date);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.inetworktimeoutCnt.setValue(this.mNetworkTimeOutCnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.inetworkReqCnt.setValue(this.mNetworkReqCnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.igpserroruploadCnt.setValue(this.mGpsErrorUploadCnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.igpsreqCnt.setValue(this.mGpsReqCnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.iRstartCnt.setValue(this.mRstartCnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.iXtraDloadCnt.setValue(this.mXtraDloadCnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.iXtraReqCnt.setValue(this.mXtraReqCnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.iNtpFlashSuccCnt.setValue(this.mNtpFlashSuccCnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.iNtpWifiFailCnt.setValue(this.mNtpWifiFailCnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.iNtpMobileFailCnt.setValue(this.mNtpMobileFailCnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.iNtpReqCnt.setValue(this.mNtpReqCnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.iAgpsConnFailedCnt.setValue(this.mAgpsConnFailedCnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.iAgpsConnCnt.setValue(this.mAgpsConnCnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.iAvgTTFF.setValue(this.mAvgTTFF);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.iAvgPosAcc.setValue(this.mAvgPosAcc);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.lAllPosTime.setValue(this.mAllPosTime);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.iBaidu_cnt.setValue(this.mBaidu_cnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.iAutoNavi_cnt.setValue(this.mAutoNavi_cnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.iCareland_cnt.setValue(this.mCareland_cnt);
                    cCSegEVENT_GPS_DAILY_CNT_REPORT.ucGpsRfBitMask.setValue(this.mGpsRfBitMask);
                    chrLogBaseModel = cCSegEVENT_GPS_DAILY_CNT_REPORT;
                    break;
                case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL /*201*/:
                case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_EXCEPTION /*202*/:
                case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_INJECT /*203*/:
                    chrLogBaseModel = (ChrLogBaseModel) mapSaveLogModel.get(Integer.valueOf(type));
                    mapSaveLogModel.remove(Integer.valueOf(type));
                    break;
                default:
                    Log.d(TAG, "writeNETInfo: error:type = " + type);
                    break;
            }
            if (chrLogBaseModel == null) {
                if (HWFLOW) {
                    Log.d(TAG, "writeNETInfo, null == cChrLogBaseModel , return");
                }
                return;
            }
            Log.d(TAG, "writeNETInfo: " + type + "    ucErrorCode:" + this.ucErrorCode);
            GnssConnectivityLogManager.getInstance().reportAbnormalEventEx(chrLogBaseModel, mGpsType, WIFI_VALUE, type, date, msg_type);
            return;
        }
        Log.d(TAG, " errorcode :< " + this.ucErrorCode + " > is not match trigger conditions,just return !");
    }

    private boolean createLogFile() {
        try {
            File directory = new File(mGpsStatePath);
            if (!directory.exists()) {
                Log.e(TAG, "create dir sdcard/gps ,status: " + directory.mkdirs());
            }
            if (!this.logFile.exists()) {
                Log.d(TAG, "create /data/gps/HwGpsStateCnt.txt,status : " + this.logFile.createNewFile());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "create /data/gps/HwGpsStateCnt.txt failed");
            return VERBOSE;
        }
    }

    private void writeGpsCHRState(boolean flushNow, boolean triggerChr) {
        Throwable th;
        if (DEBUG) {
            Log.d(TAG, "writeGpsCHRState , flushNow is : " + flushNow + " ,triggerChr is : " + triggerChr);
        }
        if (!createLogFile()) {
            Log.e(TAG, "create file HwGpsStateCnt.txt filed");
        }
        synchronized (mLock) {
            long now = SystemClock.elapsedRealtime();
            if (flushNow || now - this.mWriteStatTimestamp >= ((long) MIN_WRITE_STAT_SPAN)) {
                this.mWriteStatTimestamp = now;
                if (0 == this.mTimestamp) {
                    this.mTimestamp = System.currentTimeMillis();
                }
                DataOutputStream dataOutputStream = null;
                try {
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mGpsStateCntFile)));
                    try {
                        out.writeUTF(KEY_TIMESTAMP + Long.toString(this.mTimestamp) + SEPARATOR_KEY);
                        out.writeUTF(KEY_XTRA_REQ_CNT + Integer.toString(this.mXtraReqCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_XTRA_DLOAD_CNT + Integer.toString(this.mXtraDloadCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_NTP_REQ_CNT + Integer.toString(this.mNtpReqCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_NTP_FLASH_SUCC_CNT + Integer.toString(this.mNtpFlashSuccCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_NTP_WIFI_FAIL_CNT + Integer.toString(this.mNtpWifiFailCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_NTP_MOBILE_FAIL_CNT + Integer.toString(this.mNtpMobileFailCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_NETWORK_TIMEOUT_CNT + Integer.toString(this.mNetworkTimeOutCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_NETWORK_REQ_CNT + Integer.toString(this.mNetworkReqCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_GPS_ERR_UPLOAD_CNT + Integer.toString(this.mGpsErrorUploadCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_GPS_REQ_CNT + Integer.toString(this.mGpsReqCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_GPS_RESTART_CNT + Integer.toString(this.mRstartCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_AGPS_CONN_FAIL_CNT + Integer.toString(this.mAgpsConnFailedCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_AGPS_CONN_CNT + Integer.toString(this.mAgpsConnCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_AVG_TTFF + Integer.toString(this.mAvgTTFF) + SEPARATOR_KEY);
                        out.writeUTF(KEY_AVG_POS_ACC + Integer.toString(this.mAvgPosAcc) + SEPARATOR_KEY);
                        out.writeUTF(KEY_ALL_POS_TIME + Integer.toString(this.mAllPosTime) + SEPARATOR_KEY);
                        out.writeUTF(KEY_BAIDU_MAP_CNT + Integer.toString(this.mBaidu_cnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_AUTONAV_CNT + Integer.toString(this.mAutoNavi_cnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_CARELAND_CNT + Integer.toString(this.mCareland_cnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_LOST_POS_IN_PERIOD_CNT + Integer.toString(this.mLostPosNum) + SEPARATOR_KEY);
                        out.writeUTF(KEY_FIRST_FIX_TIMEOUT_CNT + Integer.toString(this.mFirstFixTimeoutNum) + SEPARATOR_KEY);
                        out.writeUTF(KEY_GPS_RF_NORMAL_STATUS + Boolean.toString(this.mIsGpsRfNormal) + SEPARATOR_KEY);
                        if (out != null) {
                            try {
                                out.close();
                            } catch (Exception e) {
                            }
                        }
                        dataOutputStream = out;
                    } catch (IOException e2) {
                        dataOutputStream = out;
                        try {
                            Log.e(TAG, "Error writing data file " + mGpsStateCntFile);
                            if (dataOutputStream != null) {
                                try {
                                    dataOutputStream.close();
                                } catch (Exception e3) {
                                }
                            }
                            if (triggerChr) {
                                triggerUploadIfNeed();
                            }
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            if (dataOutputStream != null) {
                                try {
                                    dataOutputStream.close();
                                } catch (Exception e4) {
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        dataOutputStream = out;
                        if (dataOutputStream != null) {
                            dataOutputStream.close();
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    Log.e(TAG, "Error writing data file " + mGpsStateCntFile);
                    if (dataOutputStream != null) {
                        dataOutputStream.close();
                    }
                    if (triggerChr) {
                        triggerUploadIfNeed();
                    }
                    return;
                }
                if (triggerChr) {
                    triggerUploadIfNeed();
                }
                return;
            }
        }
    }

    private void triggerUploadIfNeed() {
        if (DEBUG) {
            Log.d(TAG, "triggerUploadIfNeed ");
        }
        long now = System.currentTimeMillis();
        boolean isCommercial = GnssLogManager.getInstance().isCommercialUser();
        long minPeriod = isCommercial ? MIN_PERIOD_TRIGGER_COMM : MIN_PERIOD_TRIGGER_BETA;
        if (DEBUG) {
            Log.d(TAG, "minPeriod : " + minPeriod + " ,now : " + now + " ,mTimestamp : " + this.mTimestamp);
        }
        if (now - this.mTimestamp >= minPeriod && hasDataToTrigger()) {
            fillGpsRfBitMask(isCommercial);
            writeNETInfo((int) GPS_DAILY_CNT_REPORT, (boolean) VERBOSE);
            this.mTimestamp = now;
            clearStatInfo();
        }
    }

    private void fillGpsRfBitMask(boolean isCommercial) {
        int all_lost_pos_num = isCommercial ? COMM_ALL_LOST_POS_TIMES : STOPGPS;
        int all_first_fix_timeout_num = isCommercial ? COMM_ALL_LOST_POS_TIMES : STOPGPS;
        this.mGpsRfBitMask = (byte) 0;
        if (this.mAvgTTFF > NORMAL_TTFF) {
            this.mGpsRfBitMask = (byte) (this.mGpsRfBitMask | WIFI_VALUE);
        }
        if (this.mLostPosNum > all_lost_pos_num) {
            this.mGpsRfBitMask = (byte) (this.mGpsRfBitMask | STARTGPS);
        }
        if (this.mIsGpsRfNormal) {
            this.mGpsRfBitMask = (byte) (this.mGpsRfBitMask | UPDATEAGPSSTATE);
        }
        if (this.mFirstFixTimeoutNum > all_first_fix_timeout_num) {
            this.mGpsRfBitMask = (byte) (this.mGpsRfBitMask | UPDATELOCATION);
        }
        if (DEBUG) {
            Log.d(TAG, "mGpsRfBitMask value is : " + this.mGpsRfBitMask);
        }
    }

    private void clearStatInfo() {
        this.mXtraDloadCnt = UPDATENETWORKLOCATION;
        this.mXtraReqCnt = UPDATENETWORKLOCATION;
        this.mNtpReqCnt = UPDATENETWORKLOCATION;
        this.mNtpWifiFailCnt = UPDATENETWORKLOCATION;
        this.mNtpMobileFailCnt = UPDATENETWORKLOCATION;
        this.mNtpFlashSuccCnt = UPDATENETWORKLOCATION;
        this.mNetworkTimeOutCnt = UPDATENETWORKLOCATION;
        this.mNetworkReqCnt = UPDATENETWORKLOCATION;
        this.mGpsErrorUploadCnt = UPDATENETWORKLOCATION;
        this.mGpsReqCnt = UPDATENETWORKLOCATION;
        this.mRstartCnt = UPDATENETWORKLOCATION;
        this.mAgpsConnFailedCnt = UPDATENETWORKLOCATION;
        this.mAgpsConnCnt = UPDATENETWORKLOCATION;
        this.mAvgTTFF = UPDATENETWORKLOCATION;
        this.mAvgPosAcc = UPDATENETWORKLOCATION;
        this.mAllPosTime = UPDATENETWORKLOCATION;
        this.mBaidu_cnt = UPDATENETWORKLOCATION;
        this.mAutoNavi_cnt = UPDATENETWORKLOCATION;
        this.mCareland_cnt = UPDATENETWORKLOCATION;
        this.mLostPosAllCnt = UPDATENETWORKLOCATION;
        this.lostPosCnt_OneSession = UPDATENETWORKLOCATION;
        this.mIsGpsRfNormal = VERBOSE;
        this.mLostPosNum = UPDATENETWORKLOCATION;
        this.mFirstFixTimeoutNum = UPDATENETWORKLOCATION;
        writeGpsCHRState(true, VERBOSE);
    }

    private boolean hasDataToTrigger() {
        if (((long) ((((this.mXtraReqCnt + this.mNtpReqCnt) + this.mNetworkReqCnt) + this.mGpsReqCnt) + this.mAgpsConnCnt)) > 0) {
            return true;
        }
        return VERBOSE;
    }

    public void readGPSCHRStat() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.server.location.HwOldGpsLogServices.readGPSCHRStat():void. bs: [B:14:0x0056, B:26:0x0085]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:57)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r11 = this;
        r7 = "HwOldGpsLogServices";
        r8 = "readGPSCHRStat";
        android.util.Log.d(r7, r8);
        r3 = 0;
        r7 = r11.createLogFile();	 Catch:{ EOFException -> 0x046c, RuntimeException -> 0x046f, Exception -> 0x0472 }
        if (r7 != 0) goto L_0x0019;	 Catch:{ EOFException -> 0x046c, RuntimeException -> 0x046f, Exception -> 0x0472 }
    L_0x0010:
        r7 = "HwOldGpsLogServices";	 Catch:{ EOFException -> 0x046c, RuntimeException -> 0x046f, Exception -> 0x0472 }
        r8 = "create file HwGpsStateCnt.txt filed";	 Catch:{ EOFException -> 0x046c, RuntimeException -> 0x046f, Exception -> 0x0472 }
        android.util.Log.e(r7, r8);	 Catch:{ EOFException -> 0x046c, RuntimeException -> 0x046f, Exception -> 0x0472 }
    L_0x0019:
        r4 = new java.io.DataInputStream;	 Catch:{ EOFException -> 0x046c, RuntimeException -> 0x046f, Exception -> 0x0472 }
        r7 = new java.io.BufferedInputStream;	 Catch:{ EOFException -> 0x046c, RuntimeException -> 0x046f, Exception -> 0x0472 }
        r8 = new java.io.FileInputStream;	 Catch:{ EOFException -> 0x046c, RuntimeException -> 0x046f, Exception -> 0x0472 }
        r9 = mGpsStateCntFile;	 Catch:{ EOFException -> 0x046c, RuntimeException -> 0x046f, Exception -> 0x0472 }
        r8.<init>(r9);	 Catch:{ EOFException -> 0x046c, RuntimeException -> 0x046f, Exception -> 0x0472 }
        r7.<init>(r8);	 Catch:{ EOFException -> 0x046c, RuntimeException -> 0x046f, Exception -> 0x0472 }
        r4.<init>(r7);	 Catch:{ EOFException -> 0x046c, RuntimeException -> 0x046f, Exception -> 0x0472 }
    L_0x002a:
        r5 = r4.readUTF();	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "key_timestamp:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x005f;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x0037:
        r7 = "key_timestamp:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = java.lang.Long.parseLong(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mTimestamp = r8;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;
    L_0x0052:
        r2 = move-exception;
        r3 = r4;
    L_0x0054:
        if (r3 == 0) goto L_0x0059;
    L_0x0056:
        r3.close();	 Catch:{ Exception -> 0x0412 }
    L_0x0059:
        if (r3 == 0) goto L_0x005e;
    L_0x005b:
        r3.close();	 Catch:{ Exception -> 0x0432 }
    L_0x005e:
        return;
    L_0x005f:
        r7 = "key_xtra_req_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x00c1;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x0068:
        r7 = "key_xtra_req_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mXtraReqCnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;
    L_0x0083:
        r1 = move-exception;
        r3 = r4;
    L_0x0085:
        r7 = "HwOldGpsLogServices";	 Catch:{ all -> 0x042f }
        r8 = new java.lang.StringBuilder;	 Catch:{ all -> 0x042f }
        r8.<init>();	 Catch:{ all -> 0x042f }
        r9 = "RuntimeException: RuntimeException";	 Catch:{ all -> 0x042f }
        r8 = r8.append(r9);	 Catch:{ all -> 0x042f }
        r8 = r8.append(r1);	 Catch:{ all -> 0x042f }
        r8 = r8.toString();	 Catch:{ all -> 0x042f }
        android.util.Log.e(r7, r8);	 Catch:{ all -> 0x042f }
        if (r3 == 0) goto L_0x005e;
    L_0x00a1:
        r3.close();	 Catch:{ Exception -> 0x00a5 }
        goto L_0x005e;
    L_0x00a5:
        r0 = move-exception;
        r7 = "HwOldGpsLogServices";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "readGPSCHRStat: Error closing file";
        r8 = r8.append(r9);
        r8 = r8.append(r0);
        r8 = r8.toString();
        android.util.Log.e(r7, r8);
        goto L_0x005e;
    L_0x00c1:
        r7 = "key_xtra_dload_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x0126;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x00ca:
        r7 = "key_xtra_dload_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mXtraDloadCnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;
    L_0x00e6:
        r0 = move-exception;
        r3 = r4;
    L_0x00e8:
        r7 = "HwOldGpsLogServices";	 Catch:{ all -> 0x042f }
        r8 = new java.lang.StringBuilder;	 Catch:{ all -> 0x042f }
        r8.<init>();	 Catch:{ all -> 0x042f }
        r9 = "readGPSCHRStat: No config file, revert to default";	 Catch:{ all -> 0x042f }
        r8 = r8.append(r9);	 Catch:{ all -> 0x042f }
        r8 = r8.append(r0);	 Catch:{ all -> 0x042f }
        r8 = r8.toString();	 Catch:{ all -> 0x042f }
        android.util.Log.e(r7, r8);	 Catch:{ all -> 0x042f }
        if (r3 == 0) goto L_0x005e;
    L_0x0104:
        r3.close();	 Catch:{ Exception -> 0x0109 }
        goto L_0x005e;
    L_0x0109:
        r0 = move-exception;
        r7 = "HwOldGpsLogServices";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "readGPSCHRStat: Error closing file";
        r8 = r8.append(r9);
        r8 = r8.append(r0);
        r8 = r8.toString();
        android.util.Log.e(r7, r8);
        goto L_0x005e;
    L_0x0126:
        r7 = "key_ntp_req_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x0153;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x012f:
        r7 = "key_ntp_req_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mNtpReqCnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;
    L_0x014b:
        r7 = move-exception;
        r3 = r4;
    L_0x014d:
        if (r3 == 0) goto L_0x0152;
    L_0x014f:
        r3.close();	 Catch:{ Exception -> 0x044f }
    L_0x0152:
        throw r7;
    L_0x0153:
        r7 = "key_ntp_flash_succ_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x0178;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x015c:
        r7 = "key_ntp_flash_succ_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mNtpFlashSuccCnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x0178:
        r7 = "key_ntp_wifi_fail_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x019d;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x0181:
        r7 = "key_ntp_wifi_fail_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mNtpWifiFailCnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x019d:
        r7 = "key_ntp_mobile_fail_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x01c2;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x01a6:
        r7 = "key_ntp_mobile_fail_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mNtpMobileFailCnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x01c2:
        r7 = "key_network_timeout_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x01e7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x01cb:
        r7 = "key_network_timeout_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mNetworkTimeOutCnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x01e7:
        r7 = "key_network_req_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x020c;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x01f0:
        r7 = "key_network_req_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mNetworkReqCnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x020c:
        r7 = "key_gps_err_upload_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x0231;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x0215:
        r7 = "key_gps_err_upload_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mGpsErrorUploadCnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x0231:
        r7 = "key_gps_req_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x0256;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x023a:
        r7 = "key_gps_req_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mGpsReqCnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x0256:
        r7 = "key_gps_restart_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x027b;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x025f:
        r7 = "key_gps_restart_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mRstartCnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x027b:
        r7 = "key_agps_conn_fail_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x02a0;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x0284:
        r7 = "key_agps_conn_fail_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mAgpsConnFailedCnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x02a0:
        r7 = "key_agps_conn_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x02c5;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x02a9:
        r7 = "key_agps_conn_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mAgpsConnCnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x02c5:
        r7 = "key_avg_ttff:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x02ea;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x02ce:
        r7 = "key_avg_ttff:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mAvgTTFF = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x02ea:
        r7 = "key_avg_pos_acc:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x030f;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x02f3:
        r7 = "key_avg_pos_acc:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mAvgPosAcc = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x030f:
        r7 = "key_all_pos_time:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x0334;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x0318:
        r7 = "key_all_pos_time:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mAllPosTime = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x0334:
        r7 = "key_baidu_map_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x0359;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x033d:
        r7 = "key_baidu_map_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mBaidu_cnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x0359:
        r7 = "key_autonavi_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x037e;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x0362:
        r7 = "key_autonavi_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mAutoNavi_cnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x037e:
        r7 = "key_careland_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x03a3;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x0387:
        r7 = "key_careland_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mCareland_cnt = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x03a3:
        r7 = "key_lost_pos_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x03c8;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x03ac:
        r7 = "key_lost_pos_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mLostPosNum = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x03c8:
        r7 = "key_first_fix_timeout_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x03ed;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x03d1:
        r7 = "key_first_fix_timeout_cnt:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Integer.parseInt(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mFirstFixTimeoutNum = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x03ed:
        r7 = "key_gps_rf_normal_status:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = r5.startsWith(r7);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        if (r7 == 0) goto L_0x002a;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
    L_0x03f6:
        r7 = "key_gps_rf_normal_status:";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r5.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = "\n";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r8 = "";	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r6 = r6.replace(r7, r8);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r7 = java.lang.Boolean.parseBoolean(r6);	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        r11.mIsGpsRfNormal = r7;	 Catch:{ EOFException -> 0x0052, RuntimeException -> 0x0083, Exception -> 0x00e6, all -> 0x014b }
        goto L_0x002a;
    L_0x0412:
        r0 = move-exception;
        r7 = "HwOldGpsLogServices";	 Catch:{ all -> 0x042f }
        r8 = new java.lang.StringBuilder;	 Catch:{ all -> 0x042f }
        r8.<init>();	 Catch:{ all -> 0x042f }
        r9 = "readGPSCHRStat: Error reading file:";	 Catch:{ all -> 0x042f }
        r8 = r8.append(r9);	 Catch:{ all -> 0x042f }
        r8 = r8.append(r0);	 Catch:{ all -> 0x042f }
        r8 = r8.toString();	 Catch:{ all -> 0x042f }
        android.util.Log.e(r7, r8);	 Catch:{ all -> 0x042f }
        goto L_0x0059;
    L_0x042f:
        r7 = move-exception;
        goto L_0x014d;
    L_0x0432:
        r0 = move-exception;
        r7 = "HwOldGpsLogServices";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "readGPSCHRStat: Error closing file";
        r8 = r8.append(r9);
        r8 = r8.append(r0);
        r8 = r8.toString();
        android.util.Log.e(r7, r8);
        goto L_0x005e;
    L_0x044f:
        r0 = move-exception;
        r8 = "HwOldGpsLogServices";
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "readGPSCHRStat: Error closing file";
        r9 = r9.append(r10);
        r9 = r9.append(r0);
        r9 = r9.toString();
        android.util.Log.e(r8, r9);
        goto L_0x0152;
    L_0x046c:
        r2 = move-exception;
        goto L_0x0054;
    L_0x046f:
        r1 = move-exception;
        goto L_0x0085;
    L_0x0472:
        r0 = move-exception;
        goto L_0x00e8;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.HwOldGpsLogServices.readGPSCHRStat():void");
    }

    private boolean isRealConnNetwork() {
        boolean commercialUser = GnssLogManager.getInstance().isCommercialUser();
        if (commercialUser || this.isGlobalVersion) {
            Log.d(TAG, "commercialUser value is : " + commercialUser + " ,isGlobalVersion value is : " + this.isGlobalVersion + ",not do ping server test,just return !");
            return true;
        }
        boolean isConn = VERBOSE;
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(HwNetworkPropertyChecker.CHINA_MAINLAND_MAIN_SERVER).openConnection();
            httpURLConnection.setConnectTimeout(5000);
            if (httpURLConnection.getResponseCode() == WifiProCommonUtils.HTTP_REACHALBE_HOME) {
                isConn = true;
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (Throwable th) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        Log.d(TAG, "isConnByHttp ,network status is : " + isConn);
        return isConn;
    }

    public void processGnssHalDriverEvent(String strJsonExceptionBody) {
        JSONException e;
        if (HWFLOW) {
            Log.d(TAG, "processGnssHalDriverEvent, " + strJsonExceptionBody);
        }
        JSONObject jSONObject = null;
        int eventNo = -1;
        int errNo = -1;
        String str = null;
        try {
            JSONObject jsonStr = new JSONObject(strJsonExceptionBody);
            try {
                eventNo = jsonStr.getInt(ESCEP_EVENT);
                errNo = jsonStr.getInt(ESCEP_ERROR);
                jSONObject = jsonStr;
            } catch (JSONException e2) {
                e = e2;
                jSONObject = jsonStr;
                e.printStackTrace();
                if (jSONObject != null) {
                }
                if (HWFLOW) {
                    Log.d(TAG, "processGnssHalDriverEvent,  null == jsonStr || -1 == eventNo || -1 == errNo, return");
                }
                return;
            }
        } catch (JSONException e3) {
            e = e3;
            e.printStackTrace();
            if (jSONObject != null) {
            }
            if (HWFLOW) {
                Log.d(TAG, "processGnssHalDriverEvent,  null == jsonStr || -1 == eventNo || -1 == errNo, return");
            }
            return;
        }
        if (jSONObject != null || -1 == eventNo || -1 == errNo) {
            if (HWFLOW) {
                Log.d(TAG, "processGnssHalDriverEvent,  null == jsonStr || -1 == eventNo || -1 == errNo, return");
            }
            return;
        }
        HashMap<Integer, String> mapReason = (HashMap) mapGpsEventReason.get(Integer.valueOf(eventNo));
        if (mapReason != null) {
            if (mapReason.containsKey(Integer.valueOf(errNo))) {
                str = (String) mapReason.get(Integer.valueOf(errNo));
            }
            if (matchHalDriverEventTriggerFreq(GnssLogManager.getInstance().isCommercialUser(), str)) {
                writeNETInfo(eventNo, str);
            }
        }
    }

    protected void initGpsEventReasonMap() {
        HashMap<Integer, String> mapReason1 = new HashMap();
        mapReason1.put(Integer.valueOf(WIFI_VALUE), "CHR_GNSS_HAL_ERROR_SOCKET_CREATE_CMD");
        mapReason1.put(Integer.valueOf(STARTGPS), "CHR_GNSS_HAL_ERROR_SOCKET_CONNECT_CMD");
        mapReason1.put(Integer.valueOf(WWAN_ANY_VALUE), "CHR_GNSS_HAL_ERROR_PIPE_CREATE_CMD");
        mapReason1.put(Integer.valueOf(UPDATEAGPSSTATE), "CHR_GNSS_HAL_ERROR_EPOLL_REGISTER_CMD");
        mapReason1.put(Integer.valueOf(STOPGPS), "CHR_GNSS_HAL_ERROR_EPOLL_HUP_CMD");
        mapReason1.put(Integer.valueOf(UPDATEGPSRUNSTATE), "CHR_GNSS_HAL_ERROR_THREAD_CREATE_CMD");
        mapGpsEventReason.put(Integer.valueOf(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL), mapReason1);
        HashMap<Integer, String> mapReason2 = new HashMap();
        mapReason2.put(Integer.valueOf(WIFI_VALUE), "CHR_GNSS_HAL_ERROR_REBOOT_CMD");
        mapReason2.put(Integer.valueOf(STARTGPS), "CHR_GNSS_HAL_ERROR_TIMEOUT_CMD");
        mapReason2.put(Integer.valueOf(WWAN_ANY_VALUE), "CHR_GNSS_HAL_ERROR_DATA_LOST_CMD");
        mapReason2.put(Integer.valueOf(UPDATEAGPSSTATE), "CHR_GNSS_HAL_ERROR_DATA_WRONG_CMD");
        mapReason2.put(Integer.valueOf(STOPGPS), "CHR_GNSS_HAL_ERROR_ACK_LOST_CMD");
        mapGpsEventReason.put(Integer.valueOf(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_EXCEPTION), mapReason2);
        HashMap<Integer, String> mapReason3 = new HashMap();
        mapReason3.put(Integer.valueOf(WIFI_VALUE), "CHR_GNSS_HAL_ERROR_TIME_INJECT_CMD");
        mapReason3.put(Integer.valueOf(STARTGPS), "CHR_GNSS_HAL_ERROR_LOC_INJECT_CMD");
        mapReason3.put(Integer.valueOf(WWAN_ANY_VALUE), "CHR_GNSS_HAL_ERROR_EPH_INJECT_CMD");
        mapGpsEventReason.put(Integer.valueOf(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_INJECT), mapReason3);
        if (HWFLOW) {
            Log.d(TAG, "initGpsEventReasonMap, mapGpsEventReason.size() = " + mapGpsEventReason.size());
        }
    }

    private void writeNETInfo(int type, String errReason) {
        Date date = new Date();
        Object segLog = null;
        if (HWFLOW) {
            Log.d(TAG, "writeNETInfo, type = " + type + ", errReason = " + errReason);
        }
        switch (type) {
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL /*201*/:
                CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL segSyscall = new CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL();
                segSyscall.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                segSyscall.enGpsSysCallErrorReason.setValue(errReason);
                segSyscall.tmTimeStamp.setValue(date);
                CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL segLog2 = segSyscall;
                break;
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_EXCEPTION /*202*/:
                CSegEVENT_CHR_GNSS_HAL_EVENT_EXCEPTION segException = new CSegEVENT_CHR_GNSS_HAL_EVENT_EXCEPTION();
                segException.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                segException.enGpsExceptionReason.setValue(errReason);
                segException.tmTimeStamp.setValue(date);
                CSegEVENT_CHR_GNSS_HAL_EVENT_EXCEPTION segLog3 = segException;
                break;
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_INJECT /*203*/:
                CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT segInject = new CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT();
                segInject.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                segInject.enGpsInjectError.setValue(errReason);
                segInject.tmTimeStamp.setValue(date);
                CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT segLog4 = segInject;
                break;
        }
        mapSaveLogModel.put(Integer.valueOf(type), segLog);
        writeNETInfo(type, true);
    }

    boolean matchHalDriverEventTriggerFreq(boolean commercialUser, String suberror) {
        boolean isMatch = true;
        long nowTime = SystemClock.elapsedRealtime();
        TriggerLimit triggerLimit;
        if (mapHalDriverEventTriggerFreq.containsKey(suberror)) {
            triggerLimit = (TriggerLimit) mapHalDriverEventTriggerFreq.get(suberror);
            if (triggerLimit != null) {
                if (nowTime - triggerLimit.lastUploadTime > MIN_PERIOD_TRIGGER_BETA) {
                    triggerLimit.triggerNum = UPDATENETWORKLOCATION;
                } else {
                    if (triggerLimit.triggerNum > (commercialUser ? WIFI_VALUE : STOPGPS)) {
                        isMatch = VERBOSE;
                    }
                }
                if (isMatch) {
                    triggerLimit.triggerNum += WIFI_VALUE;
                    triggerLimit.lastUploadTime = nowTime;
                }
            }
        } else {
            triggerLimit = new TriggerLimit();
            triggerLimit.triggerNum = WIFI_VALUE;
            triggerLimit.lastUploadTime = nowTime;
            mapHalDriverEventTriggerFreq.put(suberror, triggerLimit);
        }
        if (HWFLOW) {
            Log.d(TAG, "GPS matchHalDriverEventTriggerFreq , isMatch = " + isMatch);
        }
        return isMatch;
    }
}
