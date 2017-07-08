package com.android.server.location;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
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
import com.android.server.location.gnsschrlog.GnssLogManager;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.wifipro.WifiProCommonUtils;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class HwGpsSessionRecorder {
    private static final long AGPS_TIMEOUT_SECOND = 20;
    private static final int ALMANAC_MASK = 1;
    private static final int ANY_VALUE = 4;
    private static final int C2K_VALUE = 2;
    private static final long COMM_UPLOAD_MIN_SPAN = 86400000;
    private static final int CONNECTION_TYPE_ANY = 0;
    private static final int CONNECTION_TYPE_C2K = 2;
    private static final int CONNECTION_TYPE_SUPL = 1;
    private static final int CONNECTION_TYPE_WIFI = 4;
    private static final int CONNECTION_TYPE_WWAN_ANY = 3;
    private static final boolean DEBUG = false;
    private static final int EPHEMERIS_MASK = 0;
    private static final int GOODSIGNAL = 32;
    private static final int GPS_AGPS_DATA_CONNECTED = 3;
    private static final int GPS_AGPS_DATA_CONN_DONE = 4;
    private static final int GPS_AGPS_DATA_CONN_FAILED = 5;
    private static final int GPS_DAILY_CNT_REPORT = 71;
    private static final int GPS_DAILY_UPLOAD = 70;
    private static final int GPS_POS_ERROR_EVENT = 72;
    private static final int GPS_POS_FLOW_ERROR_EVENT = 65;
    private static final int GPS_POS_FLOW_ERROR_EVENT_EX = 68;
    private static final int GPS_POS_TIMEOUT_EVENT = 66;
    private static final int GPS_POS_TIMEOUT_EVENT_EX = 69;
    private static final int GPS_RELEASE_AGPS_DATA_CONN = 2;
    private static final int GPS_REQUEST_AGPS_DATA_CONN = 1;
    private static final int GPS_SESSION_EVENT = 73;
    private static final int GPS_SESSION_EVT_BETA_TRIGGER_NUM = 50;
    private static final int GPS_SESSION_EVT_COMM_TRIGGER_NUM = 20;
    private static final String GPS_SESSION_RPT = "gps_pos_session_report";
    private static final int GPS_STATUS_ENGINE_OFF = 4;
    private static final int GPS_STATUS_ENGINE_ON = 3;
    private static final int GPS_STATUS_SESSION_BEGIN = 1;
    private static final int GPS_STATUS_SESSION_END = 2;
    private static final String INJECT_EXTRA_DATA = "extra_data";
    private static final String INJECT_NTP_TIME = "ntp_time";
    private static final String INJECT_REF_LOCATION = "ref_location";
    private static final int LOCATION_LOST_TIMEOUT = 4000;
    private static final int MAXSIGNAL = 35;
    private static final int MAX_NUM_TRIGGER_BETA = 100;
    private static final int MAX_NUM_TRIGGER_COMM = 50;
    private static final int MAX_UPLOAD_LOST_CNT = 20;
    private static final int NETWK_POS_TIMEOUT_EVENT = 64;
    private static final int NETWK_POS_TIMEOUT_EVENT_EX = 67;
    private static final long NETWORK_POS_TIMEOUT_SECOND = 10000;
    private static final int NETWORK_SESSION_EVT_BETA_TRIGGER_NUM = 50;
    private static final int NETWORK_SESSION_EVT_COMM_TRIGGER_NUM = 20;
    private static final String NETWORK_SESSION_RPT = "network_pos_session_report";
    private static final int NORMALSIGNAL = 28;
    private static final int REPORT_SNR_THRESHOLD = 20;
    private static final long STANDALONE_TIMEOUT_SECOND = 60;
    private static final int SUPL_VALUE = 0;
    private static final String TAG = "HwGnssLog_GpsRecord";
    private static final int USED_FOR_FIX_MASK = 2;
    private static final int VALIDSIGNAL = 25;
    private static final boolean VERBOSE = false;
    private static final int WIFI_VALUE = 1;
    private static final int WWAN_ANY_VALUE = 3;
    protected static final HashMap<String, TriggerLimit> mapEventTriggerFreq = null;
    private boolean isGlobalVersion;
    private int lostPosCnt_OneSession;
    private int mAccuracy;
    private int mAvgPositionAcc;
    private Context mContext;
    private int[] mCurSvCn0;
    GpsDailyReportEvent mDailyRptEvent;
    private long mFirstFixTimeOutVal;
    private int mFirstTimoutErrCode;
    private boolean mFixPosRpt;
    private boolean mFixed;
    private long mFixedTime;
    GpsPosErrorEvent mGpsPosErrEvent;
    GpsSessionEvent mGpsSessionEvent;
    private Listener mGpsStatusListener;
    private boolean mGpsStopped;
    private Timer mGpsTimer;
    private TimerTask mGpsTimerTask;
    private boolean mGpsdResart;
    private GpsSessionRecorderHandler mHandler;
    private HwBcmGnssManager mHwBcmGnssManager;
    private HwHisiGnssManager mHwHisiGnssManager;
    private boolean mInjectNtpTimePending;
    private byte mInjectParam;
    private boolean mIsCheckedSpeed;
    private boolean mIsGpsRfGood;
    private boolean mIsGpsRfvalied;
    private boolean mIsPosLost;
    private boolean mIsResume;
    private boolean mIsSetFirstCatchSvTime;
    private boolean mIsWifiType;
    private boolean mIssueFlag;
    private boolean mJudgeFirstFix;
    private LocationManager mLocationManager;
    private boolean mMobileDataConnect;
    private boolean mNetAvailable;
    private Timer mNetTimer;
    private TimerTask mNetTimerTask;
    private boolean mNetWorkFixPending;
    private boolean mNetworkFixed;
    NetworkPosErrorEvent mNetworkPosErrEvent;
    private String mNtpIpAddr;
    private boolean mNtpstatus;
    private String mProvider;
    private int mReStartCnt_OneSession;
    private int mReportLocCnt;
    private int mSpeed;
    private int mSubNetworkType;
    private int mSvBestSvSignalInfo;
    private int mSvCount;
    private String mSvInfoString;
    private int mSvNormalSvSignalInfo;
    private HandlerThread mThread;
    private int mUsedSvCount;
    private int ucAGPSConnReq;
    private byte ucGpsEngineCap;
    private byte ucGpsRunStatus;

    class GpsSessionRecorderHandler extends Handler {
        private ArrayList list;

        GpsSessionRecorderHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            this.list = (ArrayList) msg.obj;
            String -get10;
            switch (msg.what) {
                case HwGpsSessionRecorder.SUPL_VALUE /*0*/:
                    String provider = (String) this.list.get(HwGpsSessionRecorder.WIFI_VALUE);
                    HwGpsSessionRecorder.this.handlerNetWorkLocation(provider, (ProviderRequest) this.list.get(HwGpsSessionRecorder.SUPL_VALUE));
                case HwGpsSessionRecorder.WIFI_VALUE /*1*/:
                    Boolean isEnable = (Boolean) this.list.get(HwGpsSessionRecorder.SUPL_VALUE);
                    HwGpsSessionRecorder.this.handlerInitGps(isEnable.booleanValue(), ((Byte) this.list.get(HwGpsSessionRecorder.WIFI_VALUE)).byteValue());
                case HwGpsSessionRecorder.USED_FOR_FIX_MASK /*2*/:
                    Boolean Enable = (Boolean) this.list.get(HwGpsSessionRecorder.SUPL_VALUE);
                    HwGpsSessionRecorder.this.handlerStartGps(Enable.booleanValue(), ((Integer) this.list.get(HwGpsSessionRecorder.WIFI_VALUE)).intValue());
                case HwGpsSessionRecorder.WWAN_ANY_VALUE /*3*/:
                    HwGpsSessionRecorder.this.handlerUpdateNetworkState((NetworkInfo) this.list.get(HwGpsSessionRecorder.SUPL_VALUE));
                case HwGpsSessionRecorder.GPS_STATUS_ENGINE_OFF /*4*/:
                    Integer type1 = (Integer) this.list.get(HwGpsSessionRecorder.SUPL_VALUE);
                    HwGpsSessionRecorder.this.handlerUpdateAgpsState(type1.intValue(), ((Integer) this.list.get(HwGpsSessionRecorder.WIFI_VALUE)).intValue());
                case HwGpsSessionRecorder.GPS_AGPS_DATA_CONN_FAILED /*5*/:
                    HwGpsSessionRecorder.this.handlerStopGps(((Boolean) this.list.get(HwGpsSessionRecorder.SUPL_VALUE)).booleanValue());
                case LifeCycleStateMachine.DELETE_ACCOUNT /*6*/:
                    HwGpsSessionRecorder.this.handlerUpdateGpsRunState(((Integer) this.list.get(HwGpsSessionRecorder.SUPL_VALUE)).intValue());
                case ByteUtil.LONG_SIZE /*8*/:
                    HwGpsSessionRecorder.this.handlerUpdateLocation((Location) this.list.get(HwGpsSessionRecorder.SUPL_VALUE), ((Long) this.list.get(HwGpsSessionRecorder.WIFI_VALUE)).longValue());
                case HwGnssLogHandlerMsgID.UPDATESVSTATUS /*9*/:
                    int[] svs = (int[]) this.list.get(HwGpsSessionRecorder.WIFI_VALUE);
                    float[] snrs = (float[]) this.list.get(HwGpsSessionRecorder.USED_FOR_FIX_MASK);
                    float[] svElevations = (float[]) this.list.get(HwGpsSessionRecorder.WWAN_ANY_VALUE);
                    float[] svAzimuths = (float[]) this.list.get(HwGpsSessionRecorder.GPS_STATUS_ENGINE_OFF);
                    HwGpsSessionRecorder.this.handlerUpdateSvStatus(((Integer) this.list.get(HwGpsSessionRecorder.SUPL_VALUE)).intValue(), svs, snrs, svElevations, svAzimuths);
                case AwareAppMngDFX.APPLICATION_STARTTYPE_COLD /*10*/:
                    HwGpsSessionRecorder.this.handlerUpdateXtraDloadStatus(((Boolean) this.list.get(HwGpsSessionRecorder.SUPL_VALUE)).booleanValue());
                case AwareAppMngDFX.APPLICATION_STARTTYPE_TOTAL /*11*/:
                    HwGpsSessionRecorder.this.mNtpstatus = ((Boolean) this.list.get(HwGpsSessionRecorder.SUPL_VALUE)).booleanValue();
                    if (!HwGpsSessionRecorder.this.mInjectNtpTimePending) {
                        HwGpsSessionRecorder.this.mInjectNtpTimePending = true;
                        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                            public void run() {
                                HwGpsSessionRecorder.this.handlerUpdateNtpDloadStatus(HwGpsSessionRecorder.this.mNtpstatus);
                                HwGpsSessionRecorder.this.mInjectNtpTimePending = HwGpsSessionRecorder.VERBOSE;
                            }
                        });
                    }
                case HwGnssLogHandlerMsgID.UPDATESETPOSMODE /*12*/:
                    HwGpsSessionRecorder.this.handlerUpdateSetPosMode(((Boolean) this.list.get(HwGpsSessionRecorder.SUPL_VALUE)).booleanValue());
                case HwGnssLogHandlerMsgID.PERMISSIONERR /*13*/:
                    HwGpsSessionRecorder.this.handlerPermissionErr();
                case IAwareHabitUtils.HABIT_PROTECT_MAX_TRAIN_COUNTS /*14*/:
                    HwGpsSessionRecorder.this.handlerOpenGpsSwitchFail(((Integer) this.list.get(HwGpsSessionRecorder.SUPL_VALUE)).intValue());
                case HwGnssLogHandlerMsgID.ADDGEOFENCESTATUS /*15*/:
                    HwGpsSessionRecorder.this.handlerAddGeofenceFail();
                case HwSecDiagnoseConstant.BIT_SU /*16*/:
                    HwGpsSessionRecorder.this.handlerAddBatchingFail();
                case HwGnssLogHandlerMsgID.UPDATELOSTPOSITION /*17*/:
                    HwGpsSessionRecorder.this.handlerLostLocation();
                case HwGnssLogHandlerMsgID.UPDATEAPKNAME /*18*/:
                    HwGpsSessionRecorder.this.handlerUpdateApkName((LocationRequest) this.list.get(HwGpsSessionRecorder.SUPL_VALUE), (String) this.list.get(HwGpsSessionRecorder.WIFI_VALUE));
                case HwGnssLogHandlerMsgID.UPDATENTPERRORTIME /*19*/:
                    HwGpsSessionRecorder.this.handlerUpdateNtpErrorStatus(((Long) this.list.get(HwGpsSessionRecorder.SUPL_VALUE)).longValue(), ((Long) this.list.get(HwGpsSessionRecorder.WIFI_VALUE)).longValue());
                case HwGpsSessionRecorder.REPORT_SNR_THRESHOLD /*20*/:
                    HwGpsSessionRecorder.this.handlerUpdateLocationProviderBindErrorStatus();
                case HwGnssLogHandlerMsgID.BCM_GNSS_MANAGER_INIT /*21*/:
                    new Thread(new Runnable() {
                        public void run() {
                            if (HwGpsSessionRecorder.this.mHwBcmGnssManager != null) {
                                HwGpsSessionRecorder.this.mHwBcmGnssManager.bcmGnssSocketinit();
                            }
                        }
                    }).start();
                case NetworkPosErrorEvent.NETWORK_POSITION_TIMEOUT /*22*/:
                    Integer bcmError = (Integer) this.list.get(HwGpsSessionRecorder.SUPL_VALUE);
                    HwGpsSessionRecorder.this.handlerBrcmGnssMsg(bcmError.intValue(), ((Boolean) this.list.get(HwGpsSessionRecorder.WIFI_VALUE)).booleanValue());
                case HwGnssLogHandlerMsgID.NTP_ADDRESS_MSG /*23*/:
                    HwGpsSessionRecorder.this.mNtpIpAddr = (String) this.list.get(HwGpsSessionRecorder.SUPL_VALUE);
                    -get10 = HwGpsSessionRecorder.this.mNtpIpAddr;
                    Log.i(HwGpsSessionRecorder.TAG, "NTP server ip address is : " + r37);
                case HwGnssLogHandlerMsgID.INJECT_EXTRA_PARAM /*24*/:
                    HwGpsSessionRecorder.this.handlerInjectExtraParam((String) this.list.get(HwGpsSessionRecorder.SUPL_VALUE));
                default:
                    int i = msg.what;
                    -get10 = "====";
                    Log.d(HwGpsSessionRecorder.TAG, "====handleMessage: msg.what = " + i + r37);
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
            this.triggerNum = HwGpsSessionRecorder.SUPL_VALUE;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.HwGpsSessionRecorder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.HwGpsSessionRecorder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.HwGpsSessionRecorder.<clinit>():void");
    }

    HwGpsSessionRecorder(HandlerThread thread, Context context) {
        boolean z = VERBOSE;
        this.mCurSvCn0 = new int[GOODSIGNAL];
        this.mIssueFlag = VERBOSE;
        this.mProvider = AppHibernateCst.INVALID_PKG;
        this.mNtpIpAddr = "Unknown";
        this.mNetTimer = null;
        this.mGpsTimer = null;
        this.mNetTimerTask = null;
        this.mGpsTimerTask = null;
        this.mSvInfoString = null;
        if (!"CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region"))) {
            z = true;
        }
        this.isGlobalVersion = z;
        this.mGpsStatusListener = new Listener() {
            public void onGpsStatusChanged(int event) {
                GpsStatus gpsstatus = HwGpsSessionRecorder.this.mLocationManager.getGpsStatus(null);
                switch (event) {
                    case HwGpsSessionRecorder.WWAN_ANY_VALUE /*3*/:
                        int ttff = (gpsstatus.getTimeToFirstFix() / IOTController.TYPE_MASTER) + HwGpsSessionRecorder.WIFI_VALUE;
                        HwGpsSessionRecorder.this.mGpsSessionEvent.setTTFF(ttff);
                        Log.d(HwGpsSessionRecorder.TAG, "ttff is : " + ttff);
                    default:
                }
            }
        };
        this.mThread = thread;
        this.mContext = context;
        this.mHandler = new GpsSessionRecorderHandler(this.mThread.getLooper());
        this.mGpsPosErrEvent = new GpsPosErrorEvent(this.mContext);
        this.mDailyRptEvent = new GpsDailyReportEvent(this.mContext);
        this.mGpsSessionEvent = new GpsSessionEvent(this.mContext);
        this.mHwBcmGnssManager = new HwBcmGnssManager(this.mHandler, this.mGpsSessionEvent, this.mGpsPosErrEvent);
        this.mHandler.sendEmptyMessage(21);
        this.mHwHisiGnssManager = new HwHisiGnssManager();
    }

    public void netWorkLocation(String provider, ProviderRequest providerRequest) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(providerRequest);
        list.add(provider);
        msg.what = SUPL_VALUE;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void initGps(boolean isEnable, byte EngineCapabilities) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Boolean.valueOf(isEnable));
        list.add(Byte.valueOf(EngineCapabilities));
        msg.what = WIFI_VALUE;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateXtraDloadStatus(boolean status) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Boolean.valueOf(status));
        msg.what = 10;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateNtpDloadStatus(boolean status) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Boolean.valueOf(status));
        msg.what = 11;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateSetPosMode(boolean status) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Boolean.valueOf(status));
        msg.what = 12;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateApkName(LocationRequest request, String name) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(request);
        list.add(name);
        msg.what = 18;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void startGps(boolean isEnable, int PositionMode) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Boolean.valueOf(isEnable));
        list.add(Integer.valueOf(PositionMode));
        msg.what = USED_FOR_FIX_MASK;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateNetworkState(NetworkInfo info) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(info);
        msg.what = WWAN_ANY_VALUE;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateAgpsState(int type, int state) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Integer.valueOf(type));
        list.add(Integer.valueOf(state));
        msg.what = GPS_STATUS_ENGINE_OFF;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void stopGps(boolean status) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Boolean.valueOf(status));
        msg.what = GPS_AGPS_DATA_CONN_FAILED;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void permissionErr() {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        msg.what = 13;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void openGpsSwitchFail(int open) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Integer.valueOf(open));
        msg.what = 14;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void addGeofenceStatus() {
        Message msg = new Message();
        msg.what = 15;
        this.mHandler.sendMessage(msg);
    }

    public void addBatchingStatus() {
        Message msg = new Message();
        msg.what = 16;
        this.mHandler.sendMessage(msg);
    }

    public void updateGpsRunState(int status) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Integer.valueOf(status));
        msg.what = 6;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateLocation(Location location, long time, String provider) {
        if (provider.equalsIgnoreCase("network")) {
            if (!this.mNetworkFixed) {
                this.mNetworkFixed = true;
            }
        } else if (provider.equalsIgnoreCase("gps")) {
            if (!this.mFixed) {
                this.mFixed = true;
                this.mFixedTime = System.currentTimeMillis();
            }
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(location);
            list.add(Long.valueOf(time));
            msg.what = 8;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            this.mHandler.removeMessages(17);
            this.mHandler.sendEmptyMessageDelayed(17, 4000);
        }
    }

    public void updateSvStatus(int svCount, int[] svs, float[] snrs, float[] svElevations, float[] svAzimuths) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Integer.valueOf(svCount));
        list.add(svs);
        list.add(snrs);
        list.add(svElevations);
        list.add(svAzimuths);
        msg.what = 9;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void reportErrorNtpTime(long currentNtpTime, long realTime) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Long.valueOf(currentNtpTime));
        list.add(Long.valueOf(realTime));
        msg.what = 19;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void reportBinderError() {
        Message msg = new Message();
        msg.what = REPORT_SNR_THRESHOLD;
        this.mHandler.sendMessage(msg);
    }

    public void updateNtpServerInfo(String ntpAddr) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(ntpAddr);
        msg.what = 23;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void injectExtraParam(String extraParam) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(extraParam);
        msg.what = 24;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    private void handlerBrcmGnssMsg(int bcmErr, boolean trigger) {
        if (DEBUG) {
            Log.d(TAG, "Brcm chipset msg is : " + bcmErr + " ,trigger value is " + trigger);
        }
        switch (bcmErr) {
            case MemoryConstant.DEFAULT_DIRECT_SWAPPINESS /*30*/:
                this.mReStartCnt_OneSession += WIFI_VALUE;
                if (trigger) {
                    this.mGpsdResart = true;
                    this.mGpsSessionEvent.setBrcmRestartFlag(this.mGpsdResart);
                    break;
                }
                break;
        }
        reportPosErrEvt(bcmErr);
    }

    private void handlerInjectExtraParam(String extraParam) {
        if (extraParam.equalsIgnoreCase(INJECT_NTP_TIME)) {
            this.mInjectParam = (byte) (this.mInjectParam | WIFI_VALUE);
        } else if (extraParam.equalsIgnoreCase(INJECT_REF_LOCATION)) {
            this.mInjectParam = (byte) (this.mInjectParam | USED_FOR_FIX_MASK);
        } else if (extraParam.equalsIgnoreCase(INJECT_EXTRA_DATA)) {
            this.mInjectParam = (byte) (this.mInjectParam | GPS_STATUS_ENGINE_OFF);
        } else {
            Log.d(TAG, "handlerInjectExtraParam, unkonwn extra param : " + extraParam);
        }
        this.mGpsSessionEvent.setInjectParam(this.mInjectParam);
    }

    private void handlerInitGps(boolean isEnable, byte EngineCapabilities) {
        this.ucGpsEngineCap = EngineCapabilities;
        if (DEBUG) {
            Log.d(TAG, "handlerInitGps ,isEnable = " + isEnable + " ,EngineCapabilities = " + this.ucGpsEngineCap);
        }
        if (!isEnable) {
            reportPosErrEvt(24);
        }
        this.mDailyRptEvent.reloadGpsDailyRptInfo();
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        this.mLocationManager.addGpsStatusListener(this.mGpsStatusListener);
    }

    private void handlerStartGps(boolean isEnable, int PositionMode) {
        if (DEBUG) {
            Log.d(TAG, "handlerStartGps: isEnable : " + isEnable + " , PositionMode : " + PositionMode);
        }
        long time = System.currentTimeMillis();
        this.mGpsSessionEvent.setProvider("gps");
        this.mGpsPosErrEvent.setProvider("gps");
        this.mGpsSessionEvent.setStartTime(time);
        this.mGpsPosErrEvent.setStartTime(time);
        this.mGpsSessionEvent.setPosMode(PositionMode);
        this.mGpsPosErrEvent.setPosMode(PositionMode);
        this.mFixed = VERBOSE;
        this.mFixPosRpt = VERBOSE;
        this.mIsResume = VERBOSE;
        this.mGpsdResart = VERBOSE;
        this.mGpsStopped = VERBOSE;
        this.mJudgeFirstFix = VERBOSE;
        this.mIsSetFirstCatchSvTime = VERBOSE;
        this.mIsCheckedSpeed = VERBOSE;
        this.mSpeed = SUPL_VALUE;
        this.mFixedTime = 0;
        this.mAccuracy = SUPL_VALUE;
        this.mReportLocCnt = SUPL_VALUE;
        this.ucAGPSConnReq = SUPL_VALUE;
        this.mAvgPositionAcc = SUPL_VALUE;
        this.lostPosCnt_OneSession = SUPL_VALUE;
        this.mReStartCnt_OneSession = SUPL_VALUE;
        this.mInjectParam = (byte) 0;
        if (!isEnable) {
            Log.e(TAG, "start gps failed,pos mode is " + PositionMode);
            reportPosErrEvt(WIFI_VALUE);
        }
        this.mDailyRptEvent.updateGpsPosReqCnt(true);
    }

    private void handlerStopGps(boolean status) {
        if (DEBUG) {
            Log.d(TAG, "handlerStopGps");
        }
        if (!status) {
            Log.e(TAG, "Stop GPS failed !");
            reportPosErrEvt(USED_FOR_FIX_MASK);
        }
        stopGpsTimer();
        this.mHandler.removeMessages(17);
        if (matchEventTriggerFreq(GnssLogManager.getInstance().isCommercialUser(), GPS_SESSION_RPT)) {
            this.mDailyRptEvent.updateCn0Status(this.mIsGpsRfvalied, this.mIsGpsRfGood);
            this.mGpsSessionEvent.setCommFlag(this.mIssueFlag);
            this.mGpsSessionEvent.setNetworkAvailable(this.mNetAvailable);
            this.mGpsSessionEvent.setNetworkStatus(this.mSubNetworkType);
            this.mGpsSessionEvent.setStopTime(System.currentTimeMillis());
            this.mGpsSessionEvent.setLostPosCnt(this.lostPosCnt_OneSession);
            this.mGpsSessionEvent.setReStartCnt(this.mReStartCnt_OneSession);
            this.mGpsSessionEvent.setGpsdRestartFlag(this.mGpsdResart);
            setFirstFixPosInfo();
            this.mGpsSessionEvent.writeGpsSessionInfo();
        }
        this.mGpsSessionEvent.createNewGpsSessionEvent();
        this.mGpsPosErrEvent.createNewGpsPosErrorEvent();
        this.mIsGpsRfGood = VERBOSE;
        this.mIsGpsRfvalied = VERBOSE;
        this.mIsPosLost = VERBOSE;
        this.mIsResume = VERBOSE;
        this.mIssueFlag = VERBOSE;
        this.mFixed = VERBOSE;
        this.mFixPosRpt = VERBOSE;
        this.mGpsStopped = true;
    }

    private void handlerPermissionErr() {
        Log.e(TAG, "GPS permission denied!");
    }

    private void handlerOpenGpsSwitchFail(int open) {
        int errorcode = SUPL_VALUE;
        if (open == WIFI_VALUE) {
            Log.e(TAG, "Open gps switch fail");
            errorcode = 7;
        } else if (open == USED_FOR_FIX_MASK) {
            Log.e(TAG, "close gps switch fail");
            errorcode = 8;
        }
        reportPosErrEvt(errorcode);
    }

    private void handlerAddGeofenceFail() {
        Log.e(TAG, "add geofence fail");
        reportPosErrEvt(9);
    }

    private void handlerAddBatchingFail() {
        Log.e(TAG, "add batching fail");
        reportPosErrEvt(10);
    }

    private void handlerUpdateXtraDloadStatus(boolean status) {
        if (this.mNetAvailable) {
            boolean xtraDownloadStatus = VERBOSE;
            if (DEBUG) {
                Log.d(TAG, "handlerUpdateXtraDloadStatus:" + status);
            }
            if (status) {
                xtraDownloadStatus = true;
            } else {
                Log.e(TAG, "Download xtra data failed");
                reportPosErrEvt(WWAN_ANY_VALUE);
            }
            this.mDailyRptEvent.updateXtraDownLoadCnt(true, xtraDownloadStatus);
            return;
        }
        if (DEBUG) {
            Log.e(TAG, "Network not avaiable ! Stop xtra data download");
        }
    }

    private void handlerUpdateNtpDloadStatus(boolean status) {
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateNtpDloadStatus:" + status);
        }
        if (this.mNetAvailable) {
            boolean wifiFail = VERBOSE;
            boolean dataCallFail = VERBOSE;
            boolean ntpDownloadStatus = VERBOSE;
            if (status) {
                ntpDownloadStatus = true;
            } else if (WIFI_VALUE == System.getInt(this.mContext.getContentResolver(), "CtrlSocketSaving", SUPL_VALUE)) {
                Log.i(TAG, "power saving mode has started, ignore ntp download fail issue!");
                return;
            } else {
                if (!isRealConnNetwork()) {
                    if (this.mIsWifiType) {
                        wifiFail = true;
                        Log.d(TAG, "NTP,wifi network can not reachedable!");
                    } else {
                        dataCallFail = true;
                        Log.d(TAG, "NTP,mobile network can not reachedable!");
                    }
                }
                Log.e(TAG, "Download ntp data failed");
                reportPosErrEvt(GPS_STATUS_ENGINE_OFF);
            }
            this.mDailyRptEvent.updateNtpDownLoadCnt(true, ntpDownloadStatus, wifiFail, dataCallFail);
        }
    }

    private void handlerNetWorkLocation(String provider, ProviderRequest providerRequest) {
        this.mProvider = provider;
        boolean enable = providerRequest.reportLocation;
        long requestInterval = providerRequest.interval;
        if (DEBUG) {
            Log.d(TAG, "enable : " + enable + " , mProvider : " + this.mProvider + " , requestInterval" + requestInterval);
        }
        if (!provider.equalsIgnoreCase("network")) {
            return;
        }
        if (this.mNetWorkFixPending || this.mNetworkPosErrEvent == null) {
            Log.d(TAG, "return !, mNetWorkFixPending : " + this.mNetWorkFixPending + " , mNetworkPosErrEvent : " + this.mNetworkPosErrEvent);
        } else if (requestInterval >= COMM_UPLOAD_MIN_SPAN) {
            Log.d(TAG, "network location interval is : " + requestInterval + " ,larger than 24 hours,ignore !");
        } else {
            this.mNetworkPosErrEvent.setProvider(this.mProvider);
            this.mNetworkPosErrEvent.setStartTime(System.currentTimeMillis());
            if (this.mNetAvailable) {
                if (!enable) {
                    stopNetTimer();
                    this.mNetWorkFixPending = VERBOSE;
                } else if (this.mNetWorkFixPending) {
                    Log.e(TAG, "Network pos is already runing!");
                    return;
                } else {
                    this.mNetWorkFixPending = true;
                    this.mNetworkFixed = VERBOSE;
                    this.mDailyRptEvent.updateNetworkReqCnt(VERBOSE, true);
                    if (this.mNetTimer == null) {
                        this.mNetTimer = new Timer();
                    }
                    if (this.mNetTimerTask != null) {
                        this.mNetTimerTask.cancel();
                        this.mNetTimerTask = null;
                    }
                    this.mNetTimerTask = new TimerTask() {
                        public void run() {
                            if (HwGpsSessionRecorder.this.mNetworkFixed) {
                                HwGpsSessionRecorder.this.mNetWorkFixPending = HwGpsSessionRecorder.VERBOSE;
                                return;
                            }
                            Log.e(HwGpsSessionRecorder.TAG, "network position over 10s");
                            HwGpsSessionRecorder.this.mDailyRptEvent.updateNetworkReqCnt(true, HwGpsSessionRecorder.VERBOSE);
                            if (HwGpsSessionRecorder.this.matchEventTriggerFreq(GnssLogManager.getInstance().isCommercialUser(), HwGpsSessionRecorder.NETWORK_SESSION_RPT)) {
                                HwGpsSessionRecorder.this.mNetworkPosErrEvent.setNetworkPosTimeOUTInfo(HwGpsSessionRecorder.SUPL_VALUE, HwGpsSessionRecorder.this.mNetAvailable);
                                HwGpsSessionRecorder.this.mNetworkPosErrEvent.setNetworkAvailable(HwGpsSessionRecorder.this.mNetAvailable);
                                HwGpsSessionRecorder.this.mNetworkPosErrEvent.setNetworkInfo(HwGpsSessionRecorder.this.mSubNetworkType);
                                HwGpsSessionRecorder.this.mNetworkPosErrEvent.writeNetworkPosErrInfo();
                            }
                        }
                    };
                    try {
                        this.mNetTimer.schedule(this.mNetTimerTask, NETWORK_POS_TIMEOUT_SECOND);
                        this.mNetWorkFixPending = VERBOSE;
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "TimerTask is scheduled already !");
                    }
                }
                return;
            }
            Log.e(TAG, "Network not available !");
        }
    }

    private void handlerUpdateSetPosMode(boolean status) {
        if (!status) {
            Log.e(TAG, "set pos mode failed");
            reportPosErrEvt(GPS_AGPS_DATA_CONN_FAILED);
        }
    }

    private void handlerUpdateApkName(LocationRequest request, String name) {
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateApkName: " + name + "request value is : " + request);
        }
        String providerName = request.getProvider();
        String version = getPackageApkVersion(name);
        if (version == null) {
            version = "unknown";
        }
        if (providerName.equalsIgnoreCase("gps")) {
            this.mGpsSessionEvent.setGpsApkName(name, version);
            this.mGpsPosErrEvent.setGpsApkName(name, version);
        } else if (providerName.equalsIgnoreCase("network")) {
            this.mNetworkPosErrEvent = new NetworkPosErrorEvent(this.mContext);
            this.mNetworkPosErrEvent.setGpsApkName(name, version);
        }
    }

    private String getPackageApkVersion(String apkName) {
        String pkgVersion = "unknown";
        try {
            return this.mContext.getPackageManager().getPackageInfo(apkName, SUPL_VALUE).versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getVersionName " + e.toString());
            return pkgVersion;
        }
    }

    private void handlerUpdateNtpErrorStatus(long ntpTime, long realTime) {
        this.mGpsPosErrEvent.setNtpErrTime(ntpTime, realTime, this.mNtpIpAddr);
        reportPosErrEvt(26);
    }

    private void handlerUpdateLocationProviderBindErrorStatus() {
        Log.d(TAG, "handlerUpdateLocationProviderBindErrorStatus");
        reportPosErrEvt(31);
    }

    private void handlerUpdateNetworkState(NetworkInfo info) {
        this.mNetAvailable = VERBOSE;
        this.mSubNetworkType = SUPL_VALUE;
        if (info == null) {
            Log.d(TAG, "network info is null , return!");
            return;
        }
        this.mNetAvailable = info.isAvailable();
        if (WIFI_VALUE == info.getType()) {
            this.mIsWifiType = true;
            this.mSubNetworkType = MAX_NUM_TRIGGER_BETA;
        } else {
            this.mIsWifiType = VERBOSE;
            this.mSubNetworkType = info.getSubtype();
        }
        if (DEBUG) {
            Log.d(TAG, "Networktype is :" + info.getType() + " ,SubNetworkType : " + this.mSubNetworkType + " ,networkStatus is : " + this.mNetAvailable);
        }
    }

    private void handlerUpdateAgpsState(int type, int state) {
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateAgpsState:" + type + " ,state is: " + state);
        }
        switch (type) {
            case SUPL_VALUE /*0*/:
                this.ucAGPSConnReq = GPS_STATUS_ENGINE_OFF;
                break;
            case WIFI_VALUE /*1*/:
                this.ucAGPSConnReq = SUPL_VALUE;
                break;
            case USED_FOR_FIX_MASK /*2*/:
                this.ucAGPSConnReq = USED_FOR_FIX_MASK;
                break;
            case WWAN_ANY_VALUE /*3*/:
                this.ucAGPSConnReq = WWAN_ANY_VALUE;
                break;
            case GPS_STATUS_ENGINE_OFF /*4*/:
                this.ucAGPSConnReq = WIFI_VALUE;
                break;
        }
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateAgpsState, ucAGPSConnReq is : " + this.ucAGPSConnReq);
        }
        switch (state) {
            case WIFI_VALUE /*1*/:
                this.mDailyRptEvent.updateAgpsReqCnt(VERBOSE, true);
            case USED_FOR_FIX_MASK /*2*/:
            case WWAN_ANY_VALUE /*3*/:
            case GPS_STATUS_ENGINE_OFF /*4*/:
            case GPS_AGPS_DATA_CONN_FAILED /*5*/:
                Log.e(TAG, "agps conn failed");
                this.mDailyRptEvent.updateAgpsReqCnt(true, VERBOSE);
                reportPosErrEvt(18);
            default:
                if (DEBUG) {
                    Log.d(TAG, "handlerUpdateAgpsState, no  state case matched!");
                }
        }
    }

    private void handlerUpdateGpsRunState(int status) {
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateGpsRunState:" + status);
        }
        switch (status) {
            case WIFI_VALUE /*1*/:
                this.ucGpsRunStatus = (byte) 0;
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | WIFI_VALUE);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | GPS_STATUS_ENGINE_OFF);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | 16);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | GOODSIGNAL);
            case USED_FOR_FIX_MASK /*2*/:
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | USED_FOR_FIX_MASK);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus & -17);
            case WWAN_ANY_VALUE /*3*/:
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | GPS_STATUS_ENGINE_OFF);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | GOODSIGNAL);
            case GPS_STATUS_ENGINE_OFF /*4*/:
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | USED_FOR_FIX_MASK);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | 8);
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
        getLocationParam(location);
        if (DEBUG) {
            Log.d(TAG, "mAvgPositionAcc is : " + this.mAvgPositionAcc + ",difftime in secs is : " + diffTime);
        }
        if (this.mIsPosLost) {
            this.mIsPosLost = VERBOSE;
            if (this.lostPosCnt_OneSession < REPORT_SNR_THRESHOLD) {
                this.mIsResume = true;
            }
        }
        if (diffTime > WIFI_VALUE) {
            Log.e(TAG, "delivering  postion data is delayed in framework layer ,difftime is : " + diffTime);
            this.mGpsPosErrEvent.setDataDeliveryDelay(diffTime);
            reportPosErrEvt(17);
        }
    }

    private void getLocationParam(Location location) {
        this.mAccuracy = (int) location.getAccuracy();
        this.mSpeed = (int) location.getSpeed();
        if (this.mReportLocCnt == 0) {
            this.mAvgPositionAcc = this.mAccuracy;
            this.mGpsSessionEvent.setFixLocation(location);
        }
        if (this.mReportLocCnt < 10) {
            this.mReportLocCnt += WIFI_VALUE;
            this.mAvgPositionAcc = (this.mAccuracy + this.mAvgPositionAcc) / USED_FOR_FIX_MASK;
            this.mGpsSessionEvent.setAvgAcc(this.mAvgPositionAcc);
        }
        if (!this.mIsCheckedSpeed && this.mSpeed > 40) {
            this.mIsCheckedSpeed = true;
            setDrivingModeCn0();
        }
    }

    private void setDrivingModeCn0() {
        int i;
        int Cn0Val = SUPL_VALUE;
        for (i = SUPL_VALUE; i < this.mCurSvCn0.length - 1; i += WIFI_VALUE) {
            for (int j = i + WIFI_VALUE; j < this.mCurSvCn0.length; j += WIFI_VALUE) {
                if (this.mCurSvCn0[i] < this.mCurSvCn0[j]) {
                    int temp = this.mCurSvCn0[i];
                    this.mCurSvCn0[i] = this.mCurSvCn0[j];
                    this.mCurSvCn0[j] = temp;
                }
            }
        }
        for (i = SUPL_VALUE; i < GPS_STATUS_ENGINE_OFF; i += WIFI_VALUE) {
            Cn0Val += this.mCurSvCn0[i];
        }
        if (Cn0Val != 0) {
            Cn0Val /= GPS_STATUS_ENGINE_OFF;
        }
        this.mGpsSessionEvent.setDrivingAvgCn0(Cn0Val);
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
        this.mJudgeFirstFix = true;
        this.mMobileDataConnect = VERBOSE;
        this.mGpsSessionEvent.setCatchSvTime(System.currentTimeMillis());
        NetworkInfo networkinfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(SUPL_VALUE);
        if (networkinfo != null) {
            this.mMobileDataConnect = networkinfo.isConnected();
        }
        if (this.mMobileDataConnect) {
            this.mFirstFixTimeOutVal = AGPS_TIMEOUT_SECOND;
        } else {
            this.mFirstFixTimeOutVal = STANDALONE_TIMEOUT_SECOND;
        }
        handlerCommercialTimeoutFirstTimeOutImp(this.mFirstFixTimeOutVal);
    }

    private void handlerCommercialTimeoutFirstTimeOutImp(long timeout) {
        if (DEBUG) {
            Log.d(TAG, "Enter handlerCommercialTimeoutFirstTimeOutImp,delay time is " + timeout);
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
                if (!HwGpsSessionRecorder.this.mFixed && !HwGpsSessionRecorder.this.mGpsStopped) {
                    if (HwGpsSessionRecorder.this.mMobileDataConnect) {
                        HwGpsSessionRecorder.this.mFirstTimoutErrCode = 14;
                    } else {
                        HwGpsSessionRecorder.this.mFirstTimoutErrCode = 13;
                    }
                    HwGpsSessionRecorder.this.mGpsPosErrEvent.setFirstFixTimeOutStatus(HwGpsSessionRecorder.this.mSvCount, HwGpsSessionRecorder.this.mUsedSvCount, HwGpsSessionRecorder.this.mSvInfoString, HwGpsSessionRecorder.this.mInjectParam);
                    HwGpsSessionRecorder.this.reportPosErrEvt(HwGpsSessionRecorder.this.mFirstTimoutErrCode);
                }
            }
        };
        try {
            this.mGpsTimer.schedule(this.mGpsTimerTask, 1000 * timeout);
        } catch (IllegalStateException e) {
            Log.e(TAG, "TimerTask is scheduled already !");
        }
    }

    private void handlerLostLocation() {
        Log.e(TAG, "no position report in 4s");
        this.mIsPosLost = true;
        this.lostPosCnt_OneSession += WIFI_VALUE;
        if (this.lostPosCnt_OneSession > REPORT_SNR_THRESHOLD || this.mGpsStopped) {
            Log.e(TAG, "lost pos again,lost num is in this session : " + this.lostPosCnt_OneSession + ",or gps navigation status is : " + this.mGpsStopped);
            return;
        }
        int errorcode;
        this.mGpsSessionEvent.setLostPos_SvStatus(System.currentTimeMillis() - 4000, this.mAccuracy, this.mSpeed, this.mSvCount, this.mUsedSvCount, this.mSvInfoString);
        this.mGpsPosErrEvent.setLostPos_SvStatus(System.currentTimeMillis() - 4000, this.mAccuracy, this.mSpeed, this.mSvCount, this.mUsedSvCount, this.mSvInfoString);
        if (this.mSvCount > 0) {
            if (this.mSvNormalSvSignalInfo < GPS_STATUS_ENGINE_OFF) {
                errorcode = 19;
                Log.d(TAG, "handlerLostLocation , GPS_LOW_SIGNAl");
            } else if (this.mSvBestSvSignalInfo > WIFI_VALUE) {
                errorcode = 11;
                Log.d(TAG, "handlerLostLocation , GPS_LOST_POSITION_FAILED");
            } else {
                errorcode = 23;
                Log.d(TAG, "catch num of normal cn0(28db) SVs > 4,but num of best cno(32) is less than 1! ");
            }
            Log.d(TAG, "mSvCount is : " + this.mSvCount + "NormalSv num is : " + this.mSvNormalSvSignalInfo + "BestSv num is : " + this.mSvBestSvSignalInfo);
        } else if (this.mGpsdResart) {
            errorcode = 21;
            Log.d(TAG, "handlerLostLocation , GPSD_NOT_RECOVERY_FAILED");
        } else {
            errorcode = REPORT_SNR_THRESHOLD;
            Log.d(TAG, "handlerLostLocation , GPS_IN_DOOR_FAILED");
        }
        reportPosErrEvt(errorcode);
    }

    private void handlerUpdateSvStatus(int svCount, int[] svidWithFlags, float[] snrs, float[] svElevations, float[] svAzimuths) {
        if (this.mGpsStopped) {
            Log.d(TAG, "gps engine has stopped, not report sv info, just return!");
            return;
        }
        int svid = SUPL_VALUE;
        Arrays.fill(this.mCurSvCn0, SUPL_VALUE);
        this.mUsedSvCount = SUPL_VALUE;
        this.mSvCount = svCount;
        this.mSvNormalSvSignalInfo = SUPL_VALUE;
        this.mSvBestSvSignalInfo = SUPL_VALUE;
        StringBuilder svInfoString = new StringBuilder();
        if (!this.mIsSetFirstCatchSvTime && this.mSvCount > 0) {
            this.mIsSetFirstCatchSvTime = true;
            this.mGpsSessionEvent.setFirstCatchSvTime(System.currentTimeMillis());
        }
        for (int i = SUPL_VALUE; i < svCount; i += WIFI_VALUE) {
            int snrVal = (int) snrs[i];
            if (i < GOODSIGNAL) {
                svid = svidWithFlags[i] >> 7;
                String svInfo = svid + "," + snrVal;
                this.mCurSvCn0[i] = snrVal;
                svInfoString.append(svInfo);
                svInfoString.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                if ((svidWithFlags[i] & GPS_STATUS_ENGINE_OFF) != 0) {
                    this.mUsedSvCount += WIFI_VALUE;
                }
            }
            if (DEBUG) {
                Log.d(TAG, "handlerUpdateSvStatus ,SV NUM : " + svid + " ,CNO : " + snrVal);
            }
            if (snrVal > VALIDSIGNAL) {
                this.mIsGpsRfvalied = true;
                if (snrVal > NORMALSIGNAL) {
                    this.mSvNormalSvSignalInfo += WIFI_VALUE;
                    if (snrVal > GOODSIGNAL) {
                        this.mSvBestSvSignalInfo += WIFI_VALUE;
                        if (!this.mIsGpsRfGood && snrVal > MAXSIGNAL) {
                            this.mIsGpsRfGood = true;
                        }
                    }
                }
            }
        }
        this.mSvInfoString = svInfoString.toString();
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateSvStatus ,SV counts is :" + svCount + " ,mUsedSvCount is : " + this.mUsedSvCount + " ,mSvInfoString value is " + this.mSvInfoString);
        }
        setFirstFixPosInfo();
        if (this.mIsResume) {
            this.mGpsSessionEvent.setResumePos_SvStatus(System.currentTimeMillis(), this.mAccuracy, this.mSpeed, this.mSvCount, this.mUsedSvCount, this.mSvInfoString);
            this.mIsResume = VERBOSE;
        }
        if (!(this.mSvNormalSvSignalInfo <= GPS_STATUS_ENGINE_OFF || this.mFixed || this.mJudgeFirstFix || this.mGpsStopped)) {
            handlerCommercialFirstFixTimeOUT();
        }
    }

    private void setFirstFixPosInfo() {
        if (this.mFixed && !this.mFixPosRpt) {
            this.mGpsSessionEvent.setFixPos_SvStatus(System.currentTimeMillis(), this.mSvCount, this.mUsedSvCount, this.mSvInfoString);
            this.mFixPosRpt = true;
        }
    }

    private void reportPosErrEvt(int errorcode) {
        this.mIssueFlag = true;
        String code = "UNKNOWN_ISSUE";
        switch (errorcode) {
            case WIFI_VALUE /*1*/:
            case USED_FOR_FIX_MASK /*2*/:
            case AwareAppMngDFX.APPLICATION_STARTTYPE_TOTAL /*11*/:
            case HwGnssLogHandlerMsgID.PERMISSIONERR /*13*/:
            case IAwareHabitUtils.HABIT_PROTECT_MAX_TRAIN_COUNTS /*14*/:
            case HwGnssLogHandlerMsgID.UPDATELOSTPOSITION /*17*/:
            case HwGnssLogHandlerMsgID.BCM_GNSS_MANAGER_INIT /*21*/:
            case MemoryConstant.DEFAULT_DIRECT_SWAPPINESS /*30*/:
                this.mDailyRptEvent.updateGpsPosReqCnt(VERBOSE);
                break;
        }
        switch (errorcode) {
            case SUPL_VALUE /*0*/:
                code = "UNKNOWN_ISSUE";
                break;
            case WIFI_VALUE /*1*/:
                code = "GPS_POS_START_FAILED";
                break;
            case USED_FOR_FIX_MASK /*2*/:
                code = "GPS_POS_STOP_FAILED";
                break;
            case WWAN_ANY_VALUE /*3*/:
                code = "GPS_XTRA_DLOAD_FAILED";
                break;
            case GPS_STATUS_ENGINE_OFF /*4*/:
                code = "GPS_NTP_DLOAD_FAILED";
                break;
            case GPS_AGPS_DATA_CONN_FAILED /*5*/:
                code = "GPS_SET_POS_MODE_FAILED";
                break;
            case LifeCycleStateMachine.DELETE_ACCOUNT /*6*/:
                code = "GPS_PERMISSION_DENIED";
                break;
            case LifeCycleStateMachine.TIME_OUT /*7*/:
                code = "GPS_OPEN_GPS_SWITCH_FAILED";
                break;
            case ByteUtil.LONG_SIZE /*8*/:
                code = "GPS_CLOSE_GPS_SWITCH_FAILED";
                break;
            case HwGnssLogHandlerMsgID.UPDATESVSTATUS /*9*/:
                code = "GPS_ADD_GEOFENCE_FAILED";
                break;
            case AwareAppMngDFX.APPLICATION_STARTTYPE_COLD /*10*/:
                code = "GPS_ADD_BATCHING_FAILED";
                break;
            case AwareAppMngDFX.APPLICATION_STARTTYPE_TOTAL /*11*/:
                code = "GPS_LOST_POSITION_FAILED";
                break;
            case HwGnssLogHandlerMsgID.UPDATESETPOSMODE /*12*/:
                code = "GPS_WAKE_LOCK_NOT_RELEASE_FAILED";
                break;
            case HwGnssLogHandlerMsgID.PERMISSIONERR /*13*/:
                code = "STANDALONE_TIMEOUT";
                break;
            case IAwareHabitUtils.HABIT_PROTECT_MAX_TRAIN_COUNTS /*14*/:
                code = "AGPS_TIMEOUT";
                break;
            case HwGnssLogHandlerMsgID.ADDGEOFENCESTATUS /*15*/:
                code = "HOTSTART_TIMEOUT";
                break;
            case HwSecDiagnoseConstant.BIT_SU /*16*/:
                code = "NAVIGATION_ABORT";
                break;
            case HwGnssLogHandlerMsgID.UPDATELOSTPOSITION /*17*/:
                code = "DATA_DELIVERY_DELAY";
                break;
            case HwGnssLogHandlerMsgID.UPDATEAPKNAME /*18*/:
                code = "AGPS_CONN_FAILED";
                break;
            case HwGnssLogHandlerMsgID.UPDATENTPERRORTIME /*19*/:
                code = "GPS_LOW_SIGNAL_FAILED";
                break;
            case REPORT_SNR_THRESHOLD /*20*/:
                code = "GPS_IN_DOOR_FAILED";
                break;
            case HwGnssLogHandlerMsgID.BCM_GNSS_MANAGER_INIT /*21*/:
                code = "GPSD_NOT_RECOVERY_FAILED";
                break;
            case NetworkPosErrorEvent.NETWORK_POSITION_TIMEOUT /*22*/:
                code = "NETWORK_POSITION_TIMEOUT";
                break;
            case HwGnssLogHandlerMsgID.NTP_ADDRESS_MSG /*23*/:
                code = "GPS_LOST_POSITION_UNSURE_FAILED";
                break;
            case HwGnssLogHandlerMsgID.INJECT_EXTRA_PARAM /*24*/:
                code = "GPS_INIT_FAILED";
                break;
            case VALIDSIGNAL /*25*/:
                code = "GPS_DAILY_CNT_REPORT_FAILD";
                break;
            case HwGnssLogErrorCode.GPS_NTP_WRONG /*26*/:
                code = "GPS_NTP_WRONG";
                break;
            case HwSecDiagnoseConstant.BD_PRIORITY /*27*/:
                code = "GPS_XTRA_DATA_ERR";
                break;
            case NORMALSIGNAL /*28*/:
                code = "GPS_SUPL_DATA_ERR";
                break;
            case IAwareHabitUtils.DECREASE_ROUNDS /*29*/:
                code = "GPS_LOCAL_DATA_ERR";
                break;
            case MemoryConstant.DEFAULT_DIRECT_SWAPPINESS /*30*/:
                code = "GPS_BRCM_ASSERT";
                break;
            case HwGnssLogErrorCode.LOCATIONPROVIDER_BIND_FAIL /*31*/:
                code = "LOCATIONPROVIDER_BIND_FAIL";
                break;
            default:
                code = "UNKNOWN_ISSUE";
                break;
        }
        if (matchEventTriggerFreq(GnssLogManager.getInstance().isCommercialUser(), code)) {
            this.mGpsPosErrEvent.setNetworkAvailable(this.mNetAvailable);
            this.mGpsPosErrEvent.setNetworkInfo(this.mSubNetworkType);
            this.mGpsPosErrEvent.writePosErrInfo(errorcode);
            return;
        }
        Log.d(TAG, " errorcode :< " + code + " > is not match trigger conditions,just return !");
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

    boolean matchEventTriggerFreq(boolean commercialUser, String suberror) {
        if (suberror == null) {
            return VERBOSE;
        }
        boolean isMatch = true;
        long nowTime = SystemClock.elapsedRealtime();
        TriggerLimit triggerLimit;
        if (mapEventTriggerFreq.containsKey(suberror)) {
            triggerLimit = (TriggerLimit) mapEventTriggerFreq.get(suberror);
            if (triggerLimit != null) {
                if (nowTime - triggerLimit.lastUploadTime > COMM_UPLOAD_MIN_SPAN) {
                    triggerLimit.triggerNum = SUPL_VALUE;
                } else {
                    int triggerFreq = suberror.equalsIgnoreCase(GPS_SESSION_RPT) ? commercialUser ? REPORT_SNR_THRESHOLD : NETWORK_SESSION_EVT_BETA_TRIGGER_NUM : suberror.equalsIgnoreCase(NETWORK_SESSION_RPT) ? commercialUser ? REPORT_SNR_THRESHOLD : NETWORK_SESSION_EVT_BETA_TRIGGER_NUM : commercialUser ? NETWORK_SESSION_EVT_BETA_TRIGGER_NUM : MAX_NUM_TRIGGER_BETA;
                    if (triggerLimit.triggerNum > triggerFreq) {
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
            mapEventTriggerFreq.put(suberror, triggerLimit);
        }
        if (DEBUG) {
            Log.d(TAG, "GPS matchEventTriggerFreq , isMatch = " + isMatch);
        }
        return isMatch;
    }

    public void processGnssHalDriverEvent(String strJsonExceptionBody) {
        this.mHwHisiGnssManager.handleGnssHalDriverEvent(strJsonExceptionBody);
    }
}
