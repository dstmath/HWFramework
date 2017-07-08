package com.android.server.location.gnsschrlog;

import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import com.android.server.jankshield.TableJankBd;
import com.android.server.jankshield.TableJankEvent;
import com.android.server.location.HwGnssDftManager;
import com.android.server.location.HwGnssLogHandlerMsgID;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.IllegalBlockingModeException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class GnssConnectivityLogManager extends Handler {
    private static final int CHIP_LOG_TRIGGER_DELAYED = 2000;
    protected static final String CHRLOG_ERROR = "error";
    protected static final String CHRLOG_EVENT = "event";
    protected static final String CHRLOG_SUBSYS = "subsys";
    public static final int CHR_GNSS_HAL_EVENT_EXCEPTION = 202;
    public static final int CHR_GNSS_HAL_EVENT_EXCEPTION_EX = 206;
    public static final int CHR_GNSS_HAL_EVENT_INJECT = 203;
    public static final int CHR_GNSS_HAL_EVENT_INJECT_EX = 207;
    public static final int CHR_GNSS_HAL_EVENT_SYSCALL = 201;
    public static final int CHR_GNSS_HAL_EVENT_SYSCALL_EX = 205;
    private static final long COMM_UPLOAD_MIN_SPAN = 86400000;
    private static final String COUNTRY_CODE_CN = "CN";
    public static final int EVENT_GET_EXCEPTION_LOG = 2;
    private static final int EVENT_REPORT_EXCEPTION = 1;
    private static final int EVENT_TRIGGER_EXCEPTION_LOG = 3;
    private static final long EX_LOGS_LIMIT_CHECK_PERIOD = 2592000000L;
    public static final int GPS_DAILY_CNT_REPORT = 71;
    private static final int GPS_DEVICE_LOG_LEN = 12288;
    private static final int GPS_DEVICE_LOG_TAG = 256;
    private static final int GPS_KMSG_LEN = 3072;
    private static final int GPS_KMSG_TAG = 512;
    private static final int GPS_LOGCAT_LEN = 15360;
    private static final int GPS_LOGCAT_TAG = 768;
    private static final int GPS_POS_ERROR_EVENT = 72;
    public static final int GPS_POS_FLOW_ERROR_EVENT = 65;
    public static final int GPS_POS_FLOW_ERROR_EVENT_EX = 68;
    public static final int GPS_POS_TIMEOUT_EVENT = 66;
    public static final int GPS_POS_TIMEOUT_EVENT_EX = 69;
    private static final int GPS_SESSION_EVENT = 73;
    protected static final boolean HWFLOW = false;
    static final String LOG_TAG = "HwGnssLog_CONNECTIVITY_LOG";
    private static final int MAX_EXINFO_LENGTH = 8192;
    public static final int NETWK_POS_TIMEOUT_EVENT = 64;
    public static final int NETWK_POS_TIMEOUT_EVENT_EX = 67;
    private static final int RECEIVE_BUF_LENGTH = 9216;
    private static final int RETRY_CONNECT_INTERVAL = 1000;
    public static final String SETTINGS_EX_LOGS_COUNT = "con_chr_ex_logs_count";
    public static final String SETTINGS_EX_LOGS_LAST_CHECK_TIME = "con_chr_ex_logs_last_check_time";
    public static final String SOCKET_ADDRESS = "/data/hwlogdir/chr_logd";
    private static final int SOCKET_READ_TIMEOUT = 20000;
    private static final int SOCKET_WRITE_TIMEOUT = 20000;
    public static final String SUBSYS_BT = "bt";
    public static final String SUBSYS_GPS = "gnss";
    public static final String SUBSYS_WIFI = "wifi";
    private static final int TRIGGER_DELAYED_DAILY = 120000;
    private static final int TRIGGER_DELAYED_NORMAL = 30000;
    private static final int TRY_CONNECT_MAXTIMES = 3;
    private static final int UNKOWN_EVENT = -1;
    public static final String VENDER_BCM = "bcm";
    public static final String VENDER_HI110X = "HI110X";
    public static final String VENDER_HISI = "HI1101";
    private static Context mContext;
    private static boolean mInited;
    private static long mLastExLogsCheckTime;
    private static final int mMaxExLogsCount = 0;
    private static int mTotalChrTriggerCount;
    public static final HashMap<Integer, CHRDataPlus> mapCHRDataPlus = null;
    private static GnssConnectivityLogManager sInstance;
    private int mFlowErrorUploadCount;
    private long mFlowErrorUploadTime;
    private long mGpsDailyUploadTime;
    private int mGpsErrorUploadCount;
    private long mGpsErrorUploadTime;
    private boolean mIsConnected;
    private boolean mIsProcessing;
    private LocalSocket mLocalSocket;
    private int mNetworkTimeoutCount;
    private long mNetworkTimeoutUploadTime;
    private Runnable mProcessReadTimeout;
    private InputStream mReceiver;
    private OutputStream mSender;
    private boolean mWaitToDestroy;

    public static class CHRDataPlus {
        byte[] dataBuf;
        String errReason;
        int errorNo;
        int eventNo;

        public CHRDataPlus(int event, int error, String reason, byte[] buf) {
            this.eventNo = GnssConnectivityLogManager.UNKOWN_EVENT;
            this.errorNo = GnssConnectivityLogManager.UNKOWN_EVENT;
            this.dataBuf = null;
            this.eventNo = event;
            this.errorNo = error;
            this.errReason = reason;
            if (buf != null && buf.length > 0) {
                this.dataBuf = new byte[buf.length];
                System.arraycopy(this.dataBuf, 0, buf, 0, buf.length);
            }
        }

        public int getEventNo() {
            return this.eventNo;
        }

        public void setEventNo(int event) {
            this.eventNo = event;
        }

        public int getErrorNo() {
            return this.errorNo;
        }

        public void setErrorNo(int error) {
            this.errorNo = error;
        }

        public byte[] getDataBuf() {
            if (this.dataBuf == null) {
                return new byte[0];
            }
            if (this.dataBuf.length <= 0) {
                return new byte[0];
            }
            byte[] retBuf = new byte[this.dataBuf.length];
            System.arraycopy(retBuf, 0, this.dataBuf, 0, this.dataBuf.length);
            return retBuf;
        }

        public void setDataBuf(byte[] buf) {
            if (buf != null && buf.length > 0) {
                this.dataBuf = new byte[buf.length];
                System.arraycopy(this.dataBuf, 0, buf, 0, buf.length);
            }
        }

        public String getErrorReason() {
            return this.errReason;
        }

        public void setErrorReason(String reason) {
            this.errReason = reason;
        }
    }

    private class ConnectivityLog {
        public Date mDate;
        public int mErrorcode;
        public int mEventType;
        public int mLevel;
        public ChrLogModel mLogInfo;
        public int mMetricID;

        ConnectivityLog(ChrLogModel logInfo, int metricID, int level, int eventType, Date date, int errorCode) {
            this.mLogInfo = logInfo;
            this.mMetricID = metricID;
            this.mLevel = level;
            this.mEventType = eventType;
            this.mDate = date;
            this.mErrorcode = errorCode;
        }
    }

    class TimeoutRunnable implements Runnable {
        TimeoutRunnable() {
        }

        public void run() {
            synchronized (GnssConnectivityLogManager.this) {
                GnssConnectivityLogManager.sInstance.close();
                GnssConnectivityLogManager.sInstance.removeCallbacks(this);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.gnsschrlog.GnssConnectivityLogManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.gnsschrlog.GnssConnectivityLogManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.gnsschrlog.GnssConnectivityLogManager.<clinit>():void");
    }

    public static void mapPutCHRData(int eventNo, CHRDataPlus dataPlus) {
        synchronized (mapCHRDataPlus) {
            mapCHRDataPlus.put(Integer.valueOf(eventNo), dataPlus);
        }
    }

    public static CHRDataPlus mapGetCHRData(int eventNo) {
        CHRDataPlus cHRDataPlus;
        synchronized (mapCHRDataPlus) {
            cHRDataPlus = (CHRDataPlus) mapCHRDataPlus.get(Integer.valueOf(eventNo));
        }
        return cHRDataPlus;
    }

    private void resetGpsDailyStat() {
        this.mNetworkTimeoutCount = 0;
        this.mFlowErrorUploadCount = 0;
        this.mGpsErrorUploadCount = 0;
    }

    private GnssConnectivityLogManager(Looper looper) {
        super(looper);
        this.mLocalSocket = null;
        this.mWaitToDestroy = HWFLOW;
        this.mNetworkTimeoutUploadTime = 0;
        this.mFlowErrorUploadTime = 0;
        this.mGpsErrorUploadTime = 0;
        this.mGpsDailyUploadTime = 0;
        this.mNetworkTimeoutCount = 0;
        this.mFlowErrorUploadCount = 0;
        this.mGpsErrorUploadCount = 0;
        Log.d(LOG_TAG, "new GnssConnectivityLogManager");
        this.mIsProcessing = HWFLOW;
        this.mIsConnected = HWFLOW;
        this.mProcessReadTimeout = new TimeoutRunnable();
        resetGpsDailyStat();
    }

    public static synchronized GnssConnectivityLogManager getInstance() {
        GnssConnectivityLogManager gnssConnectivityLogManager;
        synchronized (GnssConnectivityLogManager.class) {
            if (sInstance == null) {
                HandlerThread thread = new HandlerThread("GnssConnectivityLogManager");
                thread.start();
                sInstance = new GnssConnectivityLogManager(thread.getLooper());
            }
            gnssConnectivityLogManager = sInstance;
        }
        return gnssConnectivityLogManager;
    }

    public static void init(Context context) {
        GnssLogManager.init(context);
        mContext = context;
    }

    private void setTimerForRead(long timeout) {
        postDelayed(this.mProcessReadTimeout, timeout);
    }

    private void cancelTimerForRead() {
        removeCallbacks(this.mProcessReadTimeout);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean tryToConnect() {
        if (this.mIsConnected) {
            Log.d(LOG_TAG, "the Socket is connected");
            return true;
        }
        LocalSocketAddress socketAddr = new LocalSocketAddress(SOCKET_ADDRESS, Namespace.FILESYSTEM);
        Log.d(LOG_TAG, "tryToConnect, socketAddr:" + socketAddr.getName());
        if (this.mLocalSocket != null) {
            close();
        }
        this.mLocalSocket = new LocalSocket();
        int count = TRY_CONNECT_MAXTIMES;
        while (true) {
            int count2 = count + UNKOWN_EVENT;
            if (count <= 0) {
                break;
            }
            try {
                this.mLocalSocket.connect(socketAddr);
                this.mIsConnected = true;
                break;
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "try to connect socket:/data/hwlogdir/chr_logd IllegalArgumentException. " + count2 + " times left to retry. err:" + e);
                if (count2 > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                }
            } catch (SocketTimeoutException e3) {
                Log.e(LOG_TAG, "try to connect socket:/data/hwlogdir/chr_logd SocketTimeoutException. " + count2 + " times left to retry. err:" + e3);
                if (count2 > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e22) {
                        e22.printStackTrace();
                    }
                }
            } catch (IllegalBlockingModeException e4) {
                Log.e(LOG_TAG, "try to connect socket:/data/hwlogdir/chr_logd IllegalBlockingModeException. " + count2 + " times left to retry. err:" + e4);
                if (count2 > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e222) {
                        e222.printStackTrace();
                    }
                }
            } catch (IOException e5) {
                Log.e(LOG_TAG, "try to connect socket:/data/hwlogdir/chr_logd IOException. " + count2 + " times left to retry. err:" + e5);
                if (count2 > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2222) {
                        e2222.printStackTrace();
                    }
                }
            } catch (Throwable th) {
                if (count2 > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e22222) {
                        e22222.printStackTrace();
                    }
                }
            }
            count = count2;
        }
        return this.mIsConnected;
    }

    private byte[] processCommand(String cmd) {
        if (this.mIsProcessing) {
            Log.e(LOG_TAG, "Unlucky, the command channel is busy, skip this command. Normally here is unreachable.");
            return null;
        } else if (tryToConnect()) {
            return sendCommand(cmd.getBytes());
        } else {
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public byte[] sendCommand(byte[] cmd) {
        byte[] rcvBuf = new byte[RECEIVE_BUF_LENGTH];
        ByteBuffer response = ByteBuffer.wrap(new byte[MAX_EXINFO_LENGTH]);
        int totalBytes = 0;
        try {
            this.mIsProcessing = true;
            this.mSender = this.mLocalSocket.getOutputStream();
            this.mReceiver = this.mLocalSocket.getInputStream();
            this.mLocalSocket.setSoTimeout(SOCKET_WRITE_TIMEOUT);
            this.mSender.write(cmd, 0, cmd.length);
            Log.d(LOG_TAG, "cmd:" + bytes2StringUTF8(cmd, cmd.length) + ", already send to server");
            this.mLocalSocket.shutdownOutput();
            setTimerForRead(TableJankBd.recordMAXCOUNT);
            while (true) {
                Arrays.fill(rcvBuf, (byte) 0);
                int readBytes = this.mReceiver.read(rcvBuf, 0, rcvBuf.length);
                Log.d(LOG_TAG, "readBytes response:" + readBytes);
                if (readBytes == 0 || readBytes == UNKOWN_EVENT) {
                    Log.v(LOG_TAG, "Nothing received from server");
                } else if (totalBytes + readBytes > MAX_EXINFO_LENGTH) {
                    break;
                } else {
                    response.put(rcvBuf, 0, readBytes);
                    totalBytes += readBytes;
                }
                cancelTimerForRead();
                close();
                this.mIsProcessing = HWFLOW;
                if (this.mWaitToDestroy) {
                    Log.d(LOG_TAG, "Cause need to destory, so destroy() here");
                    destroy();
                }
                Log.d(LOG_TAG, "totalBytes received:" + totalBytes);
                if (totalBytes > 0 || totalBytes > MAX_EXINFO_LENGTH) {
                    if (totalBytes <= MAX_EXINFO_LENGTH) {
                        Log.d(LOG_TAG, "response is too long, " + totalBytes + " > " + MAX_EXINFO_LENGTH);
                        return null;
                    }
                    Log.d(LOG_TAG, "Invalid response, the length of reponse:" + totalBytes);
                    return null;
                } else if (response != null) {
                    return Arrays.copyOfRange(response.array(), 0, totalBytes);
                } else {
                    return null;
                }
            }
            Log.v(LOG_TAG, "Nothing received from server");
            cancelTimerForRead();
            close();
        } catch (Exception e) {
            if (HWFLOW) {
                Log.e(LOG_TAG, "read failed from hal, e:" + e);
            }
            cancelTimerForRead();
            close();
        } catch (Throwable th) {
            cancelTimerForRead();
            close();
            this.mIsProcessing = HWFLOW;
        }
        this.mIsProcessing = HWFLOW;
        if (this.mWaitToDestroy) {
            Log.d(LOG_TAG, "Cause need to destory, so destroy() here");
            destroy();
        }
        Log.d(LOG_TAG, "totalBytes received:" + totalBytes);
        if (totalBytes > 0) {
        }
        if (totalBytes <= MAX_EXINFO_LENGTH) {
            Log.d(LOG_TAG, "Invalid response, the length of reponse:" + totalBytes);
            return null;
        }
        Log.d(LOG_TAG, "response is too long, " + totalBytes + " > " + MAX_EXINFO_LENGTH);
        return null;
    }

    private void close() {
        Log.v(LOG_TAG, "GnssConnectivityLogManager connect close");
        try {
            this.mIsConnected = HWFLOW;
            this.mIsProcessing = true;
            if (this.mLocalSocket != null) {
                this.mLocalSocket.shutdownInput();
                this.mLocalSocket.shutdownOutput();
                this.mLocalSocket.close();
                this.mLocalSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            this.mIsProcessing = HWFLOW;
        }
        this.mIsProcessing = HWFLOW;
    }

    private void onDestroy() {
        Log.v(LOG_TAG, "GnssConnectivityLogManager destory, mIsProcessing:" + this.mIsProcessing + ", mWaitToDestroy:" + this.mWaitToDestroy);
        if (this.mIsProcessing) {
            this.mWaitToDestroy = true;
            return;
        }
        close();
        if (getLooper() != null) {
            getLooper().quit();
        }
    }

    public static synchronized void destroy() {
        synchronized (GnssConnectivityLogManager.class) {
            if (sInstance != null) {
                sInstance.onDestroy();
                sInstance = null;
            }
        }
    }

    public static String bytes2StringUTF8(byte[] bytes, int byteslen) {
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, 0, byteslen, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "bytes2StringUTF8 err:" + e);
            return null;
        }
    }

    public void handleMessage(Message msg) {
        if (msg == null) {
            Log.d(LOG_TAG, "msg is null, return");
            return;
        }
        Log.d(LOG_TAG, "The event is: " + msg.what);
        switch (msg.what) {
            case EVENT_REPORT_EXCEPTION /*1*/:
                reportConnectivityException((ConnectivityLog) msg.obj);
                break;
            case EVENT_GET_EXCEPTION_LOG /*2*/:
                saveExInfo((String) msg.obj);
                break;
            case TRY_CONNECT_MAXTIMES /*3*/:
                ConnectivityLog gpsConnectivityLog = msg.obj;
                reportGPSException(gpsConnectivityLog.mEventType, gpsConnectivityLog.mDate, gpsConnectivityLog.mErrorcode);
                break;
            default:
                Log.d(LOG_TAG, "no case is match to handle");
                break;
        }
    }

    private int getExEventType(int type) {
        switch (type) {
            case NETWK_POS_TIMEOUT_EVENT /*64*/:
                return NETWK_POS_TIMEOUT_EVENT_EX;
            case GPS_POS_FLOW_ERROR_EVENT /*65*/:
                return GPS_POS_FLOW_ERROR_EVENT_EX;
            case GPS_POS_TIMEOUT_EVENT /*66*/:
                return GPS_POS_TIMEOUT_EVENT_EX;
            case CHR_GNSS_HAL_EVENT_SYSCALL /*201*/:
                return CHR_GNSS_HAL_EVENT_SYSCALL_EX;
            case CHR_GNSS_HAL_EVENT_EXCEPTION /*202*/:
                return CHR_GNSS_HAL_EVENT_EXCEPTION_EX;
            case CHR_GNSS_HAL_EVENT_INJECT /*203*/:
                return CHR_GNSS_HAL_EVENT_INJECT_EX;
            default:
                Log.e(LOG_TAG, "ExEvent type,not handler type:" + type);
                return UNKOWN_EVENT;
        }
    }

    private ChrLogModel rebuildChrLogModel(ConnectivityLog connectivityLog) {
        return connectivityLog.mLogInfo;
    }

    private String getCenumValue(Cenum obj) {
        String value = null;
        try {
            Field nameField = obj.getClass().getSuperclass().getDeclaredField(MemoryConstant.MEM_POLICY_ACTIONNAME);
            nameField.setAccessible(true);
            return (String) nameField.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return value;
        }
    }

    private ChrLogBaseModel exChangeEvent2Ex(ChrLogBaseModel baseModel, int type, byte[] ext) {
        Log.d(LOG_TAG, "exChangeEvent2Ex enter. type:" + type);
        switch (type) {
            case NETWK_POS_TIMEOUT_EVENT_EX /*67*/:
                CSegEVENT_NETWK_POS_TIMEOUT_EVENT event = (CSegEVENT_NETWK_POS_TIMEOUT_EVENT) baseModel;
                ChrLogBaseModel eventEx = new CSegEVENT_NETWK_POS_TIMEOUT_EVENT_EX();
                eventEx.tmTimeStamp.setValue(event.tmTimeStamp.getValue());
                eventEx.ucLocSetStatus.setValue(event.ucLocSetStatus.getValue());
                eventEx.ucNetworkStatus.setValue(event.ucNetworkStatus.getValue());
                eventEx.aucExt_info.setValue(ext);
                return eventEx;
            case GPS_POS_FLOW_ERROR_EVENT_EX /*68*/:
                CSegEVENT_GPS_POS_FLOW_ERROR_EVENT event2 = (CSegEVENT_GPS_POS_FLOW_ERROR_EVENT) baseModel;
                ChrLogBaseModel eventEx2 = new CSegEVENT_GPS_POS_FLOW_ERROR_EVENT_EX();
                eventEx2.ucErrorCode.setValue(event2.ucErrorCode.getValue());
                eventEx2.tmTimeStamp.setValue(event2.tmTimeStamp.getValue());
                eventEx2.ia_ucPosTime.setValue(event2.ia_ucPosTime.getValue());
                eventEx2.ucPosMethod.setValue(event2.ucPosMethod.getValue());
                eventEx2.ucLocSetStatus.setValue(event2.ucLocSetStatus.getValue());
                eventEx2.ucNetworkStatus.setValue(event2.ucNetworkStatus.getValue());
                eventEx2.ucGpsEngineCap.setValue(event2.ucGpsEngineCap.getValue());
                eventEx2.ucGpsRunStatus.setValue(event2.ucGpsRunStatus.getValue());
                eventEx2.ucAGPSConnReq.setValue(event2.ucAGPSConnReq.getValue());
                eventEx2.ucPosMode.setValue(event2.ucPosMode.getValue());
                eventEx2.ucAidingDataStatus.setValue(event2.ucAidingDataStatus.getValue());
                eventEx2.ucAidingDataReqFlg.setValue(event2.ucAidingDataReqFlg.getValue());
                eventEx2.aucCurNetStatus.setValue(event2.aucCurNetStatus.getValue());
                eventEx2.ucAGPSResult.setValue(event2.ucAGPSResult.getValue());
                eventEx2.ucSUPLStatus.setValue(event2.ucSUPLStatus.getValue());
                eventEx2.ucTimeFlg.setValue(event2.ucTimeFlg.getValue());
                eventEx2.ucucAddrFlg.setValue(event2.ucucAddrFlg.getValue());
                eventEx2.auca_ucServerAdder.setValue(event2.auca_ucServerAdder.getValue());
                eventEx2.usucServerIpPort.setValue(event2.usucServerIpPort.getValue());
                eventEx2.la_ucAgpsStartTime.setValue(event2.la_ucAgpsStartTime.getValue());
                eventEx2.la_ucAtlOpenTime.setValue(event2.la_ucAtlOpenTime.getValue());
                eventEx2.la_ucConnSvrTime.setValue(event2.la_ucConnSvrTime.getValue());
                eventEx2.la_ucAgpsEndTime.setValue(event2.la_ucAgpsEndTime.getValue());
                eventEx2.aucExt_info.setValue(ext);
                return eventEx2;
            case GPS_POS_TIMEOUT_EVENT_EX /*69*/:
                CSegEVENT_GPS_POS_TIMEOUT_EVENT event3 = (CSegEVENT_GPS_POS_TIMEOUT_EVENT) baseModel;
                ChrLogBaseModel eventEx3 = new CSegEVENT_GPS_POS_TIMEOUT_EVENT_EX();
                eventEx3.ucErrorCode.setValue(event3.ucErrorCode.getValue());
                eventEx3.tmTimeStamp.setValue(event3.tmTimeStamp.getValue());
                eventEx3.ia_ucPosTime.setValue(event3.ia_ucPosTime.getValue());
                eventEx3.ucPosMethod.setValue(event3.ucPosMethod.getValue());
                eventEx3.ucLocSetStatus.setValue(event3.ucLocSetStatus.getValue());
                eventEx3.ucNetworkStatus.setValue(event3.ucNetworkStatus.getValue());
                eventEx3.ucGpsEngineCap.setValue(event3.ucGpsEngineCap.getValue());
                eventEx3.ucGpsRunStatus.setValue(event3.ucGpsRunStatus.getValue());
                eventEx3.ucAGPSConnReq.setValue(event3.ucAGPSConnReq.getValue());
                eventEx3.ucSvFlg.setValue(event3.ucSvFlg.getValue());
                eventEx3.ia_ucSvEphMask.setValue(event3.ia_ucSvEphMask.getValue());
                eventEx3.ia_ucSvAlmMask.setValue(event3.ia_ucSvAlmMask.getValue());
                eventEx3.ia_ucSvUseMask.setValue(event3.ia_ucSvUseMask.getValue());
                eventEx3.auca_ucSvNo.setValue(event3.auca_ucSvNo.getValue());
                eventEx3.auca_ucSvSnr.setValue(event3.auca_ucSvSnr.getValue());
                eventEx3.auca_ucSvElevations.setValue(event3.auca_ucSvElevations.getValue());
                eventEx3.aucSvAzimuths.setValue(event3.aucSvAzimuths.getValue());
                eventEx3.ucPosMode.setValue(event3.ucPosMode.getValue());
                eventEx3.ucAidingDataStatus.setValue(event3.ucAidingDataStatus.getValue());
                eventEx3.ucAidingDataReqFlg.setValue(event3.ucAidingDataReqFlg.getValue());
                eventEx3.aucCurNetStatus.setValue(event3.aucCurNetStatus.getValue());
                eventEx3.ucAGPSResult.setValue(event3.ucAGPSResult.getValue());
                eventEx3.ucSUPLStatus.setValue(event3.ucSUPLStatus.getValue());
                eventEx3.ucTimeFlg.setValue(event3.ucTimeFlg.getValue());
                eventEx3.ucucAddrFlg.setValue(event3.ucucAddrFlg.getValue());
                eventEx3.auca_ucServerAdder.setValue(event3.auca_ucServerAdder.getValue());
                eventEx3.usucServerIpPort.setValue(event3.usucServerIpPort.getValue());
                eventEx3.la_ucAgpsStartTime.setValue(event3.la_ucAgpsStartTime.getValue());
                eventEx3.la_ucAtlOpenTime.setValue(event3.la_ucAtlOpenTime.getValue());
                eventEx3.la_ucConnSvrTime.setValue(event3.la_ucConnSvrTime.getValue());
                eventEx3.la_ucAgpsEndTime.setValue(event3.la_ucAgpsEndTime.getValue());
                eventEx3.aucExt_info.setValue(ext);
                return eventEx3;
            case CHR_GNSS_HAL_EVENT_SYSCALL_EX /*205*/:
                if (!(baseModel instanceof CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL)) {
                    return null;
                }
                CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL event4 = (CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL) baseModel;
                ChrLogBaseModel segGnssSyscallEx = new CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL_EX();
                segGnssSyscallEx.tmTimeStamp.setValue(event4.tmTimeStamp.getValue());
                segGnssSyscallEx.ucCardIndex.setValue(event4.ucCardIndex.getValue());
                segGnssSyscallEx.enGpsSysCallErrorReason.setValue(getCenumValue(event4.enGpsSysCallErrorReason));
                segGnssSyscallEx.aucExt_info.setValue(ext);
                return segGnssSyscallEx;
            case CHR_GNSS_HAL_EVENT_EXCEPTION_EX /*206*/:
                if (!(baseModel instanceof CSegEVENT_CHR_GNSS_HAL_EVENT_EXCEPTION)) {
                    return null;
                }
                CSegEVENT_CHR_GNSS_HAL_EVENT_EXCEPTION event5 = (CSegEVENT_CHR_GNSS_HAL_EVENT_EXCEPTION) baseModel;
                ChrLogBaseModel segGnssExceptionEx = new CSegEVENT_CHR_GNSS_HAL_EVENT_EXCEPTION_EX();
                segGnssExceptionEx.tmTimeStamp.setValue(event5.tmTimeStamp.getValue());
                segGnssExceptionEx.ucCardIndex.setValue(event5.ucCardIndex.getValue());
                segGnssExceptionEx.enGpsExceptionReason.setValue(getCenumValue(event5.enGpsExceptionReason));
                segGnssExceptionEx.aucExt_info.setValue(ext);
                return segGnssExceptionEx;
            case CHR_GNSS_HAL_EVENT_INJECT_EX /*207*/:
                if (!(baseModel instanceof CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT)) {
                    return null;
                }
                CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT event6 = (CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT) baseModel;
                ChrLogBaseModel segGnssInjectEx = new CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT_EX();
                segGnssInjectEx.tmTimeStamp.setValue(event6.tmTimeStamp.getValue());
                segGnssInjectEx.ucCardIndex.setValue(event6.ucCardIndex.getValue());
                segGnssInjectEx.enGpsInjectError.setValue(getCenumValue(event6.enGpsInjectError));
                segGnssInjectEx.aucExt_info.setValue(ext);
                return segGnssInjectEx;
            default:
                return null;
        }
    }

    private ChrLogModel rebuildChrLogModelEx(ConnectivityLog connectivityLog, byte[] exInfo) {
        int type = getExEventType(connectivityLog.mEventType);
        if (type == UNKOWN_EVENT) {
            return null;
        }
        Log.d(LOG_TAG, "will exchange event:" + connectivityLog.mEventType + " to its EX event:" + type);
        ChrLogModel logInfo = connectivityLog.mLogInfo;
        ChrLogBaseModel baseModel = (ChrLogBaseModel) logInfo.logEvents.get(0);
        if (baseModel == null) {
            Log.e(LOG_TAG, "get ChrLogBaseModel from ChrLogModel fail");
            return null;
        }
        ChrLogBaseModel baseModelEx = exChangeEvent2Ex(baseModel, type, exInfo);
        if (baseModelEx == null) {
            Log.e(LOG_TAG, "exChangeEvent2Ex fail");
            return null;
        }
        if (!logInfo.logEvents.isEmpty()) {
            logInfo.logEvents.clear();
        }
        logInfo.logEvents.add(baseModelEx);
        return logInfo;
    }

    private String buildOtherCmdForGps() {
        return "256 12288;512 3072;768 15360";
    }

    private String buildCmdString(ConnectivityLog connectivityLog) {
        String vendor = VENDER_BCM;
        String chipType = SystemProperties.get("ro.connectivity.chiptype", AppHibernateCst.INVALID_PKG);
        if (chipType == null || !(chipType.equalsIgnoreCase("hi110x") || chipType.equalsIgnoreCase("hisi"))) {
            vendor = VENDER_BCM;
        } else {
            vendor = VENDER_HISI;
            if (chipType.equalsIgnoreCase("hi110x")) {
                vendor = VENDER_HISI;
            } else if (chipType.equalsIgnoreCase("hisi")) {
                vendor = VENDER_HI110X;
            }
        }
        String subSystem = AppHibernateCst.INVALID_PKG;
        String others = AppHibernateCst.INVALID_PKG;
        switch (connectivityLog.mMetricID) {
            case IAwareHabitUtils.HABIT_PROTECT_MAX_TRAIN_COUNTS /*14*/:
                subSystem = SUBSYS_GPS;
                others = buildOtherCmdForGps();
                break;
            case HwGnssLogHandlerMsgID.ADDGEOFENCESTATUS /*15*/:
                break;
            default:
                Log.d(LOG_TAG, "unkown metric ID for get subSystem");
                break;
        }
        return vendor + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + subSystem + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + others + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER;
    }

    private byte[] getExInfo(ConnectivityLog connectivityLog) {
        if (!COUNTRY_CODE_CN.equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", AppHibernateCst.INVALID_PKG)) || getExEventType(connectivityLog.mEventType) == UNKOWN_EVENT) {
            return null;
        }
        if (connectivityLog.mEventType == GPS_DAILY_CNT_REPORT || connectivityLog.mEventType == GPS_SESSION_EVENT) {
            Log.d(LOG_TAG, "GPS report EVENT : " + connectivityLog.mEventType + ", skip ex-info");
            return null;
        }
        String chipType = SystemProperties.get("ro.connectivity.chiptype", AppHibernateCst.INVALID_PKG);
        if (chipType == null || !(chipType.equalsIgnoreCase("hi110x") || chipType.equalsIgnoreCase("hisi"))) {
            Log.d(LOG_TAG, "It's not correct chiptype, skip ex-info");
            return null;
        } else if (connectivityLog.mMetricID == 15 || connectivityLog.mMetricID == 14) {
            initExLogsCheckParams();
            mTotalChrTriggerCount += EVENT_REPORT_EXCEPTION;
            int totalCount = mTotalChrTriggerCount;
            setPersistChrTriggerTimes(mTotalChrTriggerCount);
            if (checkAndUpdateTimesLimit()) {
                byte[] bArr = null;
                if (mapCHRDataPlus.containsKey(Integer.valueOf(connectivityLog.mEventType))) {
                    CHRDataPlus dataPlus = mapGetCHRData(connectivityLog.mEventType);
                    if (dataPlus != null) {
                        if (HWFLOW) {
                            Log.d(LOG_TAG, "get saved dataPlus eventNo = " + dataPlus.eventNo + ", errorNo = " + dataPlus.errorNo + ", errReason = " + dataPlus.errReason);
                        }
                        bArr = dataPlus.getDataBuf();
                    }
                    mapPutCHRData(connectivityLog.mEventType, null);
                }
                if (bArr == null || bArr.length <= 0) {
                    String cmdStr = buildCmdString(connectivityLog);
                    if (HWFLOW) {
                        Log.d(LOG_TAG, "null == resp, execute cmd:[" + cmdStr + "]");
                    }
                    bArr = processCommand(cmdStr);
                }
                return bArr;
            }
            Log.d(LOG_TAG, "the number of ex-info CHR logs " + totalCount + " has exceed the limits " + mMaxExLogsCount + " in " + EX_LOGS_LIMIT_CHECK_PERIOD + " seconds");
            return null;
        } else {
            Log.d(LOG_TAG, "don't support BT, skip ex-info");
            return null;
        }
    }

    private void saveExInfo(String strJson) {
        JSONObject jsonStr = null;
        int eventNo = UNKOWN_EVENT;
        int errorNo = UNKOWN_EVENT;
        String str = null;
        if (HWFLOW) {
            Log.d(LOG_TAG, "saveExInfo enter, JSON = " + strJson);
        }
        String chipType = SystemProperties.get("ro.connectivity.chiptype", AppHibernateCst.INVALID_PKG);
        if (chipType == null || !(chipType.equalsIgnoreCase("hi110x") || chipType.equalsIgnoreCase("hisi"))) {
            if (HWFLOW) {
                Log.d(LOG_TAG, "It's not correct chiptype, skip ex-info");
            }
            return;
        }
        try {
            jsonStr = new JSONObject(strJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonStr == null) {
            if (HWFLOW) {
                Log.d(LOG_TAG, "null == jsonStr");
            }
            return;
        }
        try {
            str = jsonStr.getString(CHRLOG_SUBSYS);
            eventNo = jsonStr.getInt(CHRLOG_EVENT);
            errorNo = jsonStr.getInt(CHRLOG_ERROR);
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
        if (str == null || UNKOWN_EVENT == eventNo || UNKOWN_EVENT == errorNo) {
            if (HWFLOW) {
                Log.d(LOG_TAG, "null == strSubsys || -1 == eventNo || -1 == errorNo, return");
            }
            return;
        }
        if (!COUNTRY_CODE_CN.equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", AppHibernateCst.INVALID_PKG))) {
            if (HWFLOW) {
                Log.d(LOG_TAG, "don't obtain exinfo if non CN");
            }
        } else if (getExEventType(eventNo) == UNKOWN_EVENT) {
            if (HWFLOW) {
                Log.d(LOG_TAG, "don't obtain exinfo if type is unknown");
            }
        } else {
            if (str.compareTo(SUBSYS_WIFI) != 0) {
                if (str.compareTo(SUBSYS_GPS) != 0) {
                    if (HWFLOW) {
                        Log.d(LOG_TAG, "subsys != wifi & subsys != gnss");
                    }
                    return;
                }
            }
            ConnectivityLog connectivityLog = new ConnectivityLog(null, UNKOWN_EVENT, UNKOWN_EVENT, UNKOWN_EVENT, null, UNKOWN_EVENT);
            if (str.compareTo(SUBSYS_WIFI) == 0) {
                connectivityLog.mMetricID = 15;
            }
            if (str.compareTo(SUBSYS_GPS) == 0) {
                connectivityLog.mMetricID = 14;
            }
            String cmdStr = buildCmdString(connectivityLog);
            if (HWFLOW) {
                Log.d(LOG_TAG, "saveExInfo execute cmd:[" + cmdStr + "]");
            }
            byte[] resp = processCommand(cmdStr);
            if (resp != null) {
                if (HWFLOW) {
                    Log.d(LOG_TAG, "saveExInfo, mapCHRDataPlus.size() = " + mapCHRDataPlus.size() + ", eventNo = " + eventNo);
                }
                if (mapCHRDataPlus.containsKey(Integer.valueOf(eventNo))) {
                    CHRDataPlus dataPlus = mapGetCHRData(eventNo);
                    if (dataPlus == null) {
                        mapPutCHRData(eventNo, new CHRDataPlus(eventNo, errorNo, null, resp));
                        if (HWFLOW) {
                            Log.d(LOG_TAG, "saveExInfo, null == dataPlus & new CHRDataPlus");
                        }
                    } else {
                        dataPlus.setDataBuf(resp);
                        if (HWFLOW) {
                            Log.d(LOG_TAG, "saveExInfo, null != dataPlus & set resp to CHRDataPlus");
                        }
                    }
                }
                if (HWFLOW) {
                    Log.d(LOG_TAG, "saveExInfo leave, dataPlus eventNo = " + eventNo + " , errorNo = " + errorNo + " , resp.length = " + resp.length);
                }
            }
        }
    }

    private static void initExLogsCheckParams() {
        if (!mInited) {
            mTotalChrTriggerCount = getPersistChrTriggerTimes();
            mLastExLogsCheckTime = getPersistLastExLogsCheckTime();
            if (mLastExLogsCheckTime == 0) {
                mLastExLogsCheckTime = System.currentTimeMillis();
            }
            Log.d(LOG_TAG, "init for mTotalChrTriggerCount:" + mTotalChrTriggerCount + ", mLastExLogsCheckTime:" + mLastExLogsCheckTime);
            mInited = true;
        }
    }

    private boolean checkAndUpdateTimesLimit() {
        if (GnssLogManager.getInstance().isCommercialUser()) {
            boolean ret;
            long now = System.currentTimeMillis();
            Log.d(LOG_TAG, "mLastExLogsCheckTime:" + mLastExLogsCheckTime + ", now:" + now + ", EX_LOGS_LIMIT_CHECK_PERIOD:" + EX_LOGS_LIMIT_CHECK_PERIOD + ", mTotalChrTriggerCount:" + mTotalChrTriggerCount + ", mMaxExLogsCount:" + mMaxExLogsCount);
            if (now > mLastExLogsCheckTime + EX_LOGS_LIMIT_CHECK_PERIOD) {
                Log.d(LOG_TAG, "go into next check peroid set to 0");
                mTotalChrTriggerCount = EVENT_REPORT_EXCEPTION;
                setPersistChrTriggerTimes(mTotalChrTriggerCount);
                mLastExLogsCheckTime = now;
                setPersistLastExLogsCheckTime(now);
                ret = true;
            } else if (mTotalChrTriggerCount > mMaxExLogsCount) {
                ret = HWFLOW;
            } else {
                ret = true;
            }
            return ret;
        }
        Log.d(LOG_TAG, "non-commercial users, skip ex-info CHR log's number limit check");
        return true;
    }

    private static int getPersistChrTriggerTimes() {
        if (mContext != null) {
            return System.getInt(mContext.getContentResolver(), SETTINGS_EX_LOGS_COUNT, 0);
        }
        return 0;
    }

    private static void setPersistChrTriggerTimes(int times) {
        if (mContext != null) {
            System.putInt(mContext.getContentResolver(), SETTINGS_EX_LOGS_COUNT, times);
        }
    }

    private static long getPersistLastExLogsCheckTime() {
        if (mContext != null) {
            return System.getLong(mContext.getContentResolver(), SETTINGS_EX_LOGS_LAST_CHECK_TIME, 0);
        }
        return 0;
    }

    private static void setPersistLastExLogsCheckTime(long checkTime) {
        if (mContext != null) {
            System.putLong(mContext.getContentResolver(), SETTINGS_EX_LOGS_LAST_CHECK_TIME, checkTime);
        }
    }

    private void reportConnectivityException(ConnectivityLog connectivityLog) {
        ChrLogModel model;
        byte[] exInfo = getExInfo(connectivityLog);
        if (exInfo == null) {
            model = rebuildChrLogModel(connectivityLog);
        } else {
            model = rebuildChrLogModelEx(connectivityLog, exInfo);
        }
        if (model == null) {
            Log.d(LOG_TAG, "Get ChrLogModel failed, skip report CHR");
            return;
        }
        Log.d(LOG_TAG, "reportConnectivityException EX-END");
        if (!GnssLogManager.getInstance().isCommercialUser()) {
            Log.d(LOG_TAG, "report CHR and APR log");
            model.chrLogComHeadModel = new GnssChrCommonInfo().getChrComHead(mContext, HWFLOW);
            GnssLogManager.getInstance().reportAbnormalEvent(model, connectivityLog.mMetricID, EVENT_REPORT_EXCEPTION);
            int eventType = connectivityLog.mEventType;
            if (eventType != GPS_DAILY_CNT_REPORT && eventType != GPS_SESSION_EVENT) {
                switch (connectivityLog.mMetricID) {
                    case IAwareHabitUtils.HABIT_PROTECT_MAX_TRAIN_COUNTS /*14*/:
                        sendMessageDelayed(obtainMessage(TRY_CONNECT_MAXTIMES, connectivityLog), TableJankEvent.recMAXCOUNT);
                        break;
                    case HwGnssLogHandlerMsgID.ADDGEOFENCESTATUS /*15*/:
                        break;
                    default:
                        Log.d(LOG_TAG, "unkown metric ID, skip report APR");
                        break;
                }
            }
            Log.d(LOG_TAG, "GPS dont upload ARP log: type:" + eventType);
            return;
        }
        Log.d(LOG_TAG, "report CHR log only");
        model.chrLogComHeadModel = GnssChrCommonInfo.getChrComHead(mContext);
        GnssLogManager.getInstance().reportAbnormalEvent(model, connectivityLog.mMetricID, EVENT_REPORT_EXCEPTION);
    }

    public void reportAbnormalEventEx(ChrLogBaseModel logModel, int metricID, int level, int eventType, Date date, int msg_type, int errorCode) {
        reportAbnormalEventWithErrCode(logModel, metricID, level, eventType, date, msg_type, errorCode);
    }

    public void reportAbnormalEventEx(ChrLogBaseModel logModel, int metricID, int level, int eventType, Date date, int msg_type) {
        reportAbnormalEventWithErrCode(logModel, metricID, level, eventType, date, msg_type, 0);
    }

    private void reportAbnormalEventWithErrCode(ChrLogBaseModel logModel, int metricID, int level, int eventType, Date date, int msg_type, int errorCode) {
        if (!GnssLogManager.getInstance().isOverseaCommercialUser()) {
            if (logModel == null || metricID < 0 || level < EVENT_REPORT_EXCEPTION) {
                Log.e(LOG_TAG, "illegal Parameter");
                return;
            }
            if (msg_type == EVENT_REPORT_EXCEPTION) {
                Log.d(LOG_TAG, "reportAbnormalEventEx EX-BEGIN: normal type");
                sendMessage(logModel, metricID, level, eventType, date, HWFLOW, errorCode);
            } else if (msg_type == EVENT_GET_EXCEPTION_LOG) {
                Log.d(LOG_TAG, "reportAbnormalEventEx EX-BEGIN,daily type:" + eventType);
            } else {
                Log.e(LOG_TAG, "reportAbnormalEventEx:  error msg_type.  msg_type = " + msg_type);
            }
        }
    }

    private void sendMessage(ChrLogBaseModel logModel, int metricID, int level, int eventType, Date date, boolean isDaily, int errorCode) {
        ChrLogModel logInfo = new ChrLogModel();
        logInfo.chrLogFileHeadModel = GnssChrCommonInfo.getChrFileHead();
        logInfo.chrLogComHeadModel = GnssChrCommonInfo.getChrComHead(mContext);
        if (!logInfo.logEvents.isEmpty()) {
            logInfo.logEvents.clear();
        }
        logInfo.logEvents.add(logModel);
        Message targetMsg = obtainMessage(EVENT_REPORT_EXCEPTION, new ConnectivityLog(logInfo, metricID, level, eventType, date, errorCode));
        if (isDaily) {
            Log.d(LOG_TAG, "sendMessage: is daily , delayed 120 sec  trigger ......");
            sendMessageDelayed(targetMsg, 120000);
            return;
        }
        String chipType = SystemProperties.get("ro.connectivity.chiptype", AppHibernateCst.INVALID_PKG);
        if (chipType.equalsIgnoreCase("hi110x") || chipType.equalsIgnoreCase("hisi")) {
            Log.d(LOG_TAG, "sendMessage: not daily, normal trigger ......");
            sendMessage(targetMsg);
        } else if (metricID == 14) {
            Log.d(LOG_TAG, "GPS sendMessage: not daily, normal trigger ......");
            sendMessage(targetMsg);
        } else {
            Log.d(LOG_TAG, "sendMessage: not daily, delayed 30 sec trigger ......");
            sendMessageDelayed(targetMsg, 30000);
        }
    }

    private boolean reportGPSException(int type, Date time, int errorCode) {
        Log.d(LOG_TAG, "reportGPSException: " + type + "errorCode = " + errorCode);
        new HwGnssDftManager(mContext).sendExceptionDataToImonitor(type, time, errorCode);
        return true;
    }
}
