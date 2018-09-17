package com.huawei.connectivitylog;

import android.common.HwFrameworkFactory;
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
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import android.util.LogException;
import com.huawei.chrfile.client.NcMetricConstant;
import com.huawei.device.connectivitychrlog.ChrLogBaseModel;
import com.huawei.device.connectivitychrlog.ChrLogModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.IllegalBlockingModeException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class ConnectivityLogManager extends Handler {
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
    private static final int DFT_GPS_FAULT_LOG_EVENT = 910800002;
    private static final int DFT_WIFI_FAULT_EVENT = 909800002;
    private static final int DFT_WIFI_FAULT_EVENT_WITH_LOG = 909800003;
    public static final int EVENT_GET_EXCEPTION_LOG = 2;
    private static final int EVENT_REPORT_EXCEPTION = 1;
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
    private static final String IMONITORLOGDIR = "/data/log";
    private static final String IMONITOR_LOGDIR_WIFI_HISI_BETA = "/data/log/wifi/Hi110x/";
    private static final String IMONITOR_LOGDIR_WIFI_HISI_COMMERCIAL = "/log/wifi/Hi110x/";
    private static final String IMONITOR_WIFI_HISI_LOGFILE_PREFIX = "wifi_log_fw_";
    private static final String IMONITOR_WIFI_HISI_LOGFILE_SUFFIX = ".bin";
    static final String LOG_TAG = "CONNECTIVITY_LOG";
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
    public static final int WIFI_ABNORMAL_DISCONNECT = 85;
    public static final int WIFI_ABNORMAL_DISCONNECT_EX = 95;
    public static final int WIFI_ACCESS_INTERNET_FAILED = 87;
    public static final int WIFI_ACCESS_INTERNET_FAILED_EX = 97;
    public static final int WIFI_ACCESS_WEB_SLOWLY = 102;
    public static final int WIFI_ACCESS_WEB_SLOWLY_EX = 104;
    public static final int WIFI_ANTS_SWITCH_FAILED = 210;
    public static final int WIFI_ANTS_SWITCH_FAILED_EX = 211;
    public static final int WIFI_ANTS_SWITCH_FAILED_PLACEHOLDER = 212;
    public static final int WIFI_AP_INFO_COLLECT = 213;
    public static final int WIFI_CLOSE_FAILED = 81;
    public static final int WIFI_CLOSE_FAILED_EX = 91;
    public static final int WIFI_CONNECT_ASSOC_FAILED = 83;
    public static final int WIFI_CONNECT_ASSOC_FAILED_EX = 93;
    public static final int WIFI_CONNECT_AUTH_FAILED = 82;
    public static final int WIFI_CONNECT_AUTH_FAILED_EX = 92;
    public static final int WIFI_CONNECT_DHCP_FAILED = 84;
    public static final int WIFI_CONNECT_DHCP_FAILED_EX = 94;
    public static final int WIFI_CONNECT_EVENT = 214;
    public static final int WIFI_DEVICE_ERROR = 208;
    public static final int WIFI_DEVICE_ERROR_EX = 209;
    private static final int WIFI_DEVICE_LOG_HI110X_LEN = 8172;
    private static final int WIFI_DEVICE_LOG_LEN = 5628;
    private static final int WIFI_DEVICE_LOG_TAG = 256;
    public static final int WIFI_HAL_DRIVER_DEVICE_EXCEPTION = 200;
    public static final int WIFI_HAL_DRIVER_EXCEPTION_EX = 204;
    private static final int WIFI_KMSG_HI110X_LEN = 0;
    private static final int WIFI_KMSG_LEN = 2044;
    private static final int WIFI_KMSG_TAG = 512;
    private static final int WIFI_LOGCAT_HI110X_LEN = 0;
    private static final int WIFI_LOGCAT_LEN = 252;
    private static final int WIFI_LOGCAT_TAG = 768;
    public static final int WIFI_OPEN_FAILED = 80;
    public static final int WIFI_OPEN_FAILED_EX = 90;
    public static final int WIFI_POOR_LEVEL = 103;
    public static final int WIFI_PORTAL_AUTH_MSG_COLLECTE = 124;
    public static final int WIFI_PORTAL_SAMPLES_COLLECTE = 120;
    public static final int WIFI_REPEATER_OPEN_OR_CLOSE_FAILED = 127;
    public static final int WIFI_SCAN_FAILED = 86;
    public static final int WIFI_SCAN_FAILED_EX = 96;
    public static final int WIFI_STABILITY_STAT = 110;
    public static final int WIFI_STATUS_CHANGEDBY_APK = 98;
    private static final int WIFI_SYSINFO_HI110X_LEN = 0;
    private static final int WIFI_SYSINFO_LEN = 232;
    private static final int WIFI_SYSINFO_TAG = 0;
    public static final int WIFI_USER_CONNECT = 101;
    public static final int WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT = 126;
    public static final int WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT = 125;
    public static final int WIFI_WIFIPRO_EXCEPTION_EVENT = 122;
    public static final int WIFI_WIFIPRO_STATISTICS_EVENT = 121;
    public static final int WIFI_WORKAROUND_STAT = 113;
    private static Context mContext;
    private static boolean mInited;
    private static long mLastExLogsCheckTime;
    private static final int mMaxExLogsCount = 0;
    private static int mTotalChrTriggerCount;
    public static final HashMap<Integer, CHRDataPlus> mapCHRDataPlus = null;
    private static ConnectivityLogManager sInstance;
    private int mAccessFailCount;
    private long mAccessInetUploadTime;
    private int mCloseFailCount;
    private long mCloseUploadTime;
    private int mDhcpFailCount;
    private long mDhcpUploadTime;
    private int mDisconnFailCount;
    private long mDisconnectUploadTime;
    private int mFlowErrorUploadCount;
    private long mFlowErrorUploadTime;
    private long mGpsDailyUploadTime;
    private int mGpsErrorUploadCount;
    private long mGpsErrorUploadTime;
    private boolean mIsConnected;
    private boolean mIsProcessing;
    private LocalSocket mLocalSocket;
    private LogException mLogException;
    private int mNetworkTimeoutCount;
    private long mNetworkTimeoutUploadTime;
    private int mOpenFailCount;
    private long mOpenUploadTime;
    private Runnable mProcessReadTimeout;
    private InputStream mReceiver;
    private int mScanFailCount;
    private long mScanUploadTime;
    private OutputStream mSender;
    private boolean mWaitToDestroy;
    private long mWifiDailyUploadTime;

    public static class CHRDataPlus {
        byte[] dataBuf;
        String errReason;
        int errorNo;
        int eventNo;

        public CHRDataPlus(int event, int error, String reason, byte[] buf) {
            this.eventNo = ConnectivityLogManager.UNKOWN_EVENT;
            this.errorNo = ConnectivityLogManager.UNKOWN_EVENT;
            this.dataBuf = null;
            this.eventNo = event;
            this.errorNo = error;
            this.errReason = reason;
            if (buf != null && buf.length > 0) {
                this.dataBuf = new byte[buf.length];
                System.arraycopy(this.dataBuf, ConnectivityLogManager.WIFI_SYSINFO_TAG, buf, ConnectivityLogManager.WIFI_SYSINFO_TAG, buf.length);
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
                return new byte[ConnectivityLogManager.WIFI_SYSINFO_TAG];
            }
            if (this.dataBuf.length <= 0) {
                return new byte[ConnectivityLogManager.WIFI_SYSINFO_TAG];
            }
            byte[] retBuf = new byte[this.dataBuf.length];
            System.arraycopy(retBuf, ConnectivityLogManager.WIFI_SYSINFO_TAG, this.dataBuf, ConnectivityLogManager.WIFI_SYSINFO_TAG, this.dataBuf.length);
            return retBuf;
        }

        public void setDataBuf(byte[] buf) {
            if (buf != null && buf.length > 0) {
                this.dataBuf = new byte[buf.length];
                System.arraycopy(this.dataBuf, ConnectivityLogManager.WIFI_SYSINFO_TAG, buf, ConnectivityLogManager.WIFI_SYSINFO_TAG, buf.length);
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
        public int mEventType;
        public int mLevel;
        public ChrLogModel mLogInfo;
        public int mMetricID;

        ConnectivityLog(ChrLogModel logInfo, int metricID, int level, int eventType, Date date) {
            this.mLogInfo = logInfo;
            this.mMetricID = metricID;
            this.mLevel = level;
            this.mEventType = eventType;
            this.mDate = date;
        }
    }

    class TimeoutRunnable implements Runnable {
        TimeoutRunnable() {
        }

        public synchronized void run() {
            ConnectivityLogManager.sInstance.close();
            ConnectivityLogManager.sInstance.removeCallbacks(this);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.connectivitylog.ConnectivityLogManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.connectivitylog.ConnectivityLogManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.connectivitylog.ConnectivityLogManager.<clinit>():void");
    }

    public static void initCHRDataPlusMap() {
        mapCHRDataPlus.put(Integer.valueOf(WIFI_OPEN_FAILED), null);
        mapCHRDataPlus.put(Integer.valueOf(WIFI_CLOSE_FAILED), null);
        mapCHRDataPlus.put(Integer.valueOf(WIFI_CONNECT_DHCP_FAILED), null);
        mapCHRDataPlus.put(Integer.valueOf(WIFI_ABNORMAL_DISCONNECT), null);
        mapCHRDataPlus.put(Integer.valueOf(WIFI_SCAN_FAILED), null);
        mapCHRDataPlus.put(Integer.valueOf(WIFI_ACCESS_INTERNET_FAILED), null);
        mapCHRDataPlus.put(Integer.valueOf(WIFI_CONNECT_AUTH_FAILED), null);
        mapCHRDataPlus.put(Integer.valueOf(WIFI_CONNECT_ASSOC_FAILED), null);
        mapCHRDataPlus.put(Integer.valueOf(WIFI_USER_CONNECT), null);
        mapCHRDataPlus.put(Integer.valueOf(WIFI_ACCESS_WEB_SLOWLY), null);
        if (HWFLOW) {
            Log.d(LOG_TAG, "initCHRDataPlusMap , mapCHRDataPlus.size() = " + mapCHRDataPlus.size());
        }
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

    private void resetWifiDailyStat() {
        this.mOpenFailCount = WIFI_SYSINFO_TAG;
        this.mCloseFailCount = WIFI_SYSINFO_TAG;
        this.mDhcpFailCount = WIFI_SYSINFO_TAG;
        this.mScanFailCount = WIFI_SYSINFO_TAG;
        this.mAccessFailCount = WIFI_SYSINFO_TAG;
        this.mDisconnFailCount = WIFI_SYSINFO_TAG;
    }

    private void resetGpsDailyStat() {
        this.mNetworkTimeoutCount = WIFI_SYSINFO_TAG;
        this.mFlowErrorUploadCount = WIFI_SYSINFO_TAG;
        this.mGpsErrorUploadCount = WIFI_SYSINFO_TAG;
    }

    private ConnectivityLogManager(Looper looper) {
        super(looper);
        this.mLocalSocket = null;
        this.mWaitToDestroy = HWFLOW;
        this.mNetworkTimeoutUploadTime = 0;
        this.mFlowErrorUploadTime = 0;
        this.mGpsErrorUploadTime = 0;
        this.mGpsDailyUploadTime = 0;
        this.mNetworkTimeoutCount = WIFI_SYSINFO_TAG;
        this.mFlowErrorUploadCount = WIFI_SYSINFO_TAG;
        this.mGpsErrorUploadCount = WIFI_SYSINFO_TAG;
        this.mOpenUploadTime = 0;
        this.mCloseUploadTime = 0;
        this.mDhcpUploadTime = 0;
        this.mDisconnectUploadTime = 0;
        this.mScanUploadTime = 0;
        this.mAccessInetUploadTime = 0;
        this.mWifiDailyUploadTime = 0;
        this.mOpenFailCount = WIFI_SYSINFO_TAG;
        this.mCloseFailCount = WIFI_SYSINFO_TAG;
        this.mDhcpFailCount = WIFI_SYSINFO_TAG;
        this.mScanFailCount = WIFI_SYSINFO_TAG;
        this.mAccessFailCount = WIFI_SYSINFO_TAG;
        this.mDisconnFailCount = WIFI_SYSINFO_TAG;
        this.mLogException = HwFrameworkFactory.getLogException();
        Log.d(LOG_TAG, "new ConnectivityLogManager");
        this.mIsProcessing = HWFLOW;
        this.mIsConnected = HWFLOW;
        this.mProcessReadTimeout = new TimeoutRunnable();
        resetWifiDailyStat();
        resetGpsDailyStat();
    }

    public static synchronized ConnectivityLogManager getInstance() {
        ConnectivityLogManager connectivityLogManager;
        synchronized (ConnectivityLogManager.class) {
            if (sInstance == null) {
                HandlerThread thread = new HandlerThread("ConnectivityLogManager");
                thread.start();
                sInstance = new ConnectivityLogManager(thread.getLooper());
            }
            connectivityLogManager = sInstance;
        }
        return connectivityLogManager;
    }

    public static void init(Context context) {
        LogManager.init(context);
        mContext = context;
    }

    private void setTimerForRead(long timeout) {
        postDelayed(this.mProcessReadTimeout, timeout);
    }

    private void cancelTimerForRead() {
        removeCallbacks(this.mProcessReadTimeout);
    }

    public static String getCurDateString() {
        return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
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
        IOException e;
        Throwable th;
        byte[] rcvBuf = new byte[RECEIVE_BUF_LENGTH];
        ByteBuffer response = ByteBuffer.wrap(new byte[MAX_EXINFO_LENGTH]);
        int totalBytes = WIFI_SYSINFO_TAG;
        FileOutputStream fout = null;
        try {
            String filePath;
            this.mIsProcessing = true;
            this.mSender = this.mLocalSocket.getOutputStream();
            this.mReceiver = this.mLocalSocket.getInputStream();
            this.mLocalSocket.setSoTimeout(SOCKET_WRITE_TIMEOUT);
            this.mSender.write(cmd, WIFI_SYSINFO_TAG, cmd.length);
            Log.d(LOG_TAG, "cmd:" + bytes2StringUTF8(cmd, cmd.length) + ", already send to server");
            this.mLocalSocket.shutdownOutput();
            setTimerForRead(20000);
            if (LogManager.getInstance().isCommercialUser()) {
                filePath = IMONITOR_LOGDIR_WIFI_HISI_COMMERCIAL;
            } else {
                filePath = IMONITOR_LOGDIR_WIFI_HISI_BETA;
            }
            File dir = new File(filePath);
            if (dir.exists() || dir.mkdirs()) {
                String fileName = filePath + IMONITOR_WIFI_HISI_LOGFILE_PREFIX;
                fileName = (fileName + getCurDateString()) + IMONITOR_WIFI_HISI_LOGFILE_SUFFIX;
                FileOutputStream fout2 = new FileOutputStream(fileName);
                try {
                    int readBytes;
                    if (HWFLOW) {
                        Log.d(LOG_TAG, "open chr log file success: " + fileName);
                    }
                    while (true) {
                        Arrays.fill(rcvBuf, (byte) 0);
                        readBytes = this.mReceiver.read(rcvBuf, WIFI_SYSINFO_TAG, rcvBuf.length);
                        Log.d(LOG_TAG, "readBytes response:" + readBytes);
                        if (readBytes == 0 || readBytes == UNKOWN_EVENT) {
                            Log.v(LOG_TAG, "Nothing received from server");
                        } else if (totalBytes + readBytes > MAX_EXINFO_LENGTH) {
                            break;
                        } else {
                            response.put(rcvBuf, WIFI_SYSINFO_TAG, readBytes);
                            fout2.write(rcvBuf, WIFI_SYSINFO_TAG, readBytes);
                            totalBytes += readBytes;
                        }
                        cancelTimerForRead();
                        close();
                        this.mIsProcessing = HWFLOW;
                        if (fout2 != null) {
                            try {
                                fout2.close();
                            } catch (IOException e2) {
                                if (HWFLOW) {
                                    Log.e(LOG_TAG, "close FileOutputStream, e:" + e2);
                                }
                            }
                        }
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
                            return Arrays.copyOfRange(response.array(), WIFI_SYSINFO_TAG, totalBytes);
                        } else {
                            return null;
                        }
                    }
                    Log.e(LOG_TAG, "read bytes:" + (totalBytes + readBytes) + " more than max limit buf size:" + MAX_EXINFO_LENGTH);
                    totalBytes = WIFI_SYSINFO_TAG;
                    response = null;
                    cancelTimerForRead();
                    close();
                    this.mIsProcessing = HWFLOW;
                    if (fout2 != null) {
                        fout2.close();
                    }
                } catch (IOException e3) {
                    e2 = e3;
                    fout = fout2;
                    try {
                        if (HWFLOW) {
                            Log.e(LOG_TAG, "read failed from hal, e:" + e2);
                        }
                        cancelTimerForRead();
                        close();
                        this.mIsProcessing = HWFLOW;
                        if (fout != null) {
                            try {
                                fout.close();
                            } catch (IOException e22) {
                                if (HWFLOW) {
                                    Log.e(LOG_TAG, "close FileOutputStream, e:" + e22);
                                }
                            }
                        }
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
                    } catch (Throwable th2) {
                        th = th2;
                        cancelTimerForRead();
                        close();
                        this.mIsProcessing = HWFLOW;
                        if (fout != null) {
                            try {
                                fout.close();
                            } catch (IOException e222) {
                                if (HWFLOW) {
                                    Log.e(LOG_TAG, "close FileOutputStream, e:" + e222);
                                }
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fout = fout2;
                    cancelTimerForRead();
                    close();
                    this.mIsProcessing = HWFLOW;
                    if (fout != null) {
                        fout.close();
                    }
                    throw th;
                }
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
            throw new IOException("Create " + filePath + " failed!");
        } catch (IOException e4) {
            e222 = e4;
            if (HWFLOW) {
                Log.e(LOG_TAG, "read failed from hal, e:" + e222);
            }
            cancelTimerForRead();
            close();
            this.mIsProcessing = HWFLOW;
            if (fout != null) {
                fout.close();
            }
            if (this.mWaitToDestroy) {
                Log.d(LOG_TAG, "Cause need to destory, so destroy() here");
                destroy();
            }
            Log.d(LOG_TAG, "totalBytes received:" + totalBytes);
            if (totalBytes > 0) {
            }
            if (totalBytes <= MAX_EXINFO_LENGTH) {
                Log.d(LOG_TAG, "response is too long, " + totalBytes + " > " + MAX_EXINFO_LENGTH);
                return null;
            }
            Log.d(LOG_TAG, "Invalid response, the length of reponse:" + totalBytes);
            return null;
        }
    }

    private void close() {
        Log.v(LOG_TAG, "ConnectivityLogManager connect close");
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
        Log.v(LOG_TAG, "ConnectivityLogManager destory, mIsProcessing:" + this.mIsProcessing + ", mWaitToDestroy:" + this.mWaitToDestroy);
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
        synchronized (ConnectivityLogManager.class) {
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
            return new String(bytes, WIFI_SYSINFO_TAG, byteslen, "UTF-8");
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
        }
    }

    private ChrLogModel rebuildChrLogModel(ConnectivityLog connectivityLog) {
        return connectivityLog.mLogInfo;
    }

    private String buildOtherCmdForWifi(String vendor) {
        String strRet = "";
        if (vendor == null || vendor.isEmpty()) {
            return strRet;
        }
        if (vendor.equals(VENDER_HISI)) {
            strRet = "0 232;256 5628;512 2044;768 252";
        } else if (vendor.equals(VENDER_HI110X)) {
            strRet = "0 0;256 8172;512 0;768 0";
        }
        return strRet;
    }

    private String buildOtherCmdForGps() {
        return "256 12288;512 3072;768 15360";
    }

    private String buildCmdString(ConnectivityLog connectivityLog) {
        String vendor = VENDER_BCM;
        String chipType = SystemProperties.get("ro.connectivity.chiptype", "");
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
        String subSystem = "";
        String others = "";
        switch (connectivityLog.mMetricID) {
            case NcMetricConstant.GPS_METRIC_ID /*14*/:
                subSystem = SUBSYS_GPS;
                others = buildOtherCmdForGps();
                break;
            case NcMetricConstant.WIFI_METRIC_ID /*15*/:
                subSystem = SUBSYS_WIFI;
                others = buildOtherCmdForWifi(vendor);
                break;
            default:
                Log.d(LOG_TAG, "unkown metric ID for get subSystem");
                break;
        }
        return vendor + ";" + subSystem + ";" + others + ";";
    }

    private void getExInfo(ConnectivityLog connectivityLog) {
        if (!COUNTRY_CODE_CN.equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""))) {
            return;
        }
        if (connectivityLog.mEventType == GPS_DAILY_CNT_REPORT || connectivityLog.mEventType == GPS_SESSION_EVENT) {
            Log.d(LOG_TAG, "GPS report EVENT : " + connectivityLog.mEventType + ", skip ex-info");
            return;
        }
        String chipType = SystemProperties.get("ro.connectivity.chiptype", "");
        if (chipType == null || !(chipType.equalsIgnoreCase("hi110x") || chipType.equalsIgnoreCase("hisi"))) {
            Log.d(LOG_TAG, "It's not correct chiptype, skip ex-info");
        } else if (connectivityLog.mMetricID == 15 || connectivityLog.mMetricID == 14) {
            initExLogsCheckParams();
            mTotalChrTriggerCount += EVENT_REPORT_EXCEPTION;
            int totalCount = mTotalChrTriggerCount;
            setPersistChrTriggerTimes(mTotalChrTriggerCount);
            if (checkAndUpdateTimesLimit()) {
                String cmdStr = buildCmdString(connectivityLog);
                if (HWFLOW) {
                    Log.d(LOG_TAG, "getExInfo execute cmd:[" + cmdStr + "]");
                }
                processCommand(cmdStr);
                return;
            }
            Log.d(LOG_TAG, "the number of ex-info CHR logs " + totalCount + " has exceed the limits " + mMaxExLogsCount + " in " + EX_LOGS_LIMIT_CHECK_PERIOD + " seconds");
        } else {
            Log.d(LOG_TAG, "don't support BT, skip ex-info");
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
        String chipType = SystemProperties.get("ro.connectivity.chiptype", "");
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
        if (COUNTRY_CODE_CN.equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""))) {
            if (str.compareTo(SUBSYS_WIFI) != 0) {
                if (str.compareTo(SUBSYS_GPS) != 0) {
                    if (HWFLOW) {
                        Log.d(LOG_TAG, "subsys != wifi & subsys != gnss");
                    }
                    return;
                }
            }
            ConnectivityLog connectivityLog = new ConnectivityLog(null, UNKOWN_EVENT, UNKOWN_EVENT, UNKOWN_EVENT, null);
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
            processCommand(cmdStr);
            return;
        }
        if (HWFLOW) {
            Log.d(LOG_TAG, "don't obtain exinfo if non CN");
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
        if (LogManager.getInstance().isCommercialUser()) {
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
            return System.getInt(mContext.getContentResolver(), SETTINGS_EX_LOGS_COUNT, WIFI_SYSINFO_TAG);
        }
        return WIFI_SYSINFO_TAG;
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
        getExInfo(connectivityLog);
        ChrLogModel model = rebuildChrLogModel(connectivityLog);
        if (model == null) {
            Log.d(LOG_TAG, "Get ChrLogModel failed, skip report CHR");
            return;
        }
        Log.d(LOG_TAG, "reportConnectivityException EX-END");
        if (!LogManager.getInstance().isCommercialUser()) {
            Log.d(LOG_TAG, "report CHR and APR log");
            model.chrLogComHeadModel = new ChrCommonInfo().getChrComHead(mContext, HWFLOW);
            LogManager.getInstance().reportAbnormalEvent(model, connectivityLog.mMetricID, EVENT_REPORT_EXCEPTION);
            int eventType = connectivityLog.mEventType;
            if (eventType != WIFI_STABILITY_STAT && eventType != WIFI_CONNECT_ASSOC_FAILED) {
                if (eventType != GPS_DAILY_CNT_REPORT && eventType != GPS_SESSION_EVENT) {
                    if (connectivityLog.mEventType != WIFI_PORTAL_SAMPLES_COLLECTE && connectivityLog.mEventType != WIFI_WIFIPRO_STATISTICS_EVENT && connectivityLog.mEventType != WIFI_PORTAL_AUTH_MSG_COLLECTE) {
                        switch (connectivityLog.mMetricID) {
                            case NcMetricConstant.GPS_METRIC_ID /*14*/:
                                reportGPSException(eventType, connectivityLog.mDate);
                                break;
                            case NcMetricConstant.WIFI_METRIC_ID /*15*/:
                                reportWifiExceptionLog(eventType, "manually", connectivityLog.mDate);
                                break;
                            default:
                                Log.d(LOG_TAG, "unkown metric ID, skip report APR");
                                break;
                        }
                    }
                    Log.d(LOG_TAG, "dont upload log for wifi event type:" + connectivityLog.mEventType);
                    return;
                }
                Log.d(LOG_TAG, "GPS dont upload ARP log: type:" + eventType);
                return;
            }
            Log.d(LOG_TAG, "dont upload ARP log: type:" + eventType);
            return;
        }
        Log.d(LOG_TAG, "report CHR log only");
        model.chrLogComHeadModel = ChrCommonInfo.getChrComHead(mContext);
        LogManager.getInstance().reportAbnormalEvent(model, connectivityLog.mMetricID, EVENT_REPORT_EXCEPTION);
    }

    public void reportAbnormalEventEx(ChrLogBaseModel logModel, int metricID, int level, int eventType, Date date, int msg_type) {
        if (!LogManager.getInstance().isOverseaCommercialUser()) {
            if (logModel == null || metricID < 0 || level < EVENT_REPORT_EXCEPTION) {
                Log.e(LOG_TAG, "illegal Parameter");
                return;
            }
            if (msg_type == EVENT_REPORT_EXCEPTION) {
                Log.d(LOG_TAG, "reportAbnormalEventEx EX-BEGIN: normal type");
                sendMessage(logModel, metricID, level, eventType, date, HWFLOW);
            } else {
                Log.e(LOG_TAG, "reportAbnormalEventEx:  error msg_type.  msg_type = " + msg_type);
            }
        }
    }

    private void sendMessage(ChrLogBaseModel logModel, int metricID, int level, int eventType, Date date, boolean isDaily) {
        ChrLogModel logInfo = new ChrLogModel();
        logInfo.chrLogFileHeadModel = ChrCommonInfo.getChrFileHead();
        logInfo.chrLogComHeadModel = ChrCommonInfo.getChrComHead(mContext);
        if (!logInfo.logEvents.isEmpty()) {
            logInfo.logEvents.clear();
        }
        logInfo.logEvents.add(logModel);
        Message targetMsg = obtainMessage(EVENT_REPORT_EXCEPTION, new ConnectivityLog(logInfo, metricID, level, eventType, date));
        if (isDaily) {
            Log.d(LOG_TAG, "sendMessage: is daily , delayed 120 sec  trigger ......");
            sendMessageDelayed(targetMsg, 120000);
            return;
        }
        String chipType = SystemProperties.get("ro.connectivity.chiptype", "");
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

    public boolean reportWIFIException(int type, String reason, Date time) {
        Log.d(LOG_TAG, "reportWIFIException: " + type);
        String LocalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(time);
        String cmd = String.format("archive -i %s -i %s -i %s -i %s -i %s -i %s -i %s -i %s -i %s -i %s -o %s_wifi -z zip", new Object[]{"/data/log/android_logs/kmsgcat-log", "/data/log/android_logs/kmsgcat-log.1", "/data/log/android_logs/applogcat-log", "/data/log/android_logs/applogcat-log.1", "/data/android_logs/kmsgcat-log", "/data/android_logs/kmsgcat-log.1", "/data/android_logs/applogcat-log", "/data/android_logs/applogcat-log.1", "/data/hwlogdir/wifi_log/wifi_log_1", "/data/hwlogdir/wifi_log/wifi_log_2", LocalTime});
        if (type == WIFI_ACCESS_INTERNET_FAILED) {
            cmd = String.format("archive -i %s -i %s -i %s -i %s -i %s -i %s -i %s -i %s -i %s -i %s -i %s -i %s -o %s_wifi -z zip", new Object[]{"/data/log/android_logs/kmsgcat-log", "/data/log/android_logs/kmsgcat-log.1", "/data/log/android_logs/applogcat-log", "/data/log/android_logs/applogcat-log.1", "/data/android_logs/kmsgcat-log", "/data/android_logs/kmsgcat-log.1", "/data/android_logs/applogcat-log", "/data/android_logs/applogcat-log.1", "/data/log/wifi/wifi_env_0.dump", "/data/log/wifi/wifi_env_1.dump", "/data/hwlogdir/wifi_log/wifi_log_1", "/data/hwlogdir/wifi_log/wifi_log_2", LocalTime});
        }
        this.mLogException.cmd(SUBSYS_WIFI, cmd);
        return true;
    }

    public boolean reportWifiExceptionLog(int type, String reason, Date time) {
        Log.d(LOG_TAG, "reportWifiExceptionLog: " + type);
        switch (type) {
            case WIFI_OPEN_FAILED /*80*/:
            case WIFI_CLOSE_FAILED /*81*/:
            case WIFI_CONNECT_AUTH_FAILED /*82*/:
            case WIFI_CONNECT_ASSOC_FAILED /*83*/:
            case WIFI_CONNECT_DHCP_FAILED /*84*/:
            case WIFI_ABNORMAL_DISCONNECT /*85*/:
            case WIFI_SCAN_FAILED /*86*/:
            case WIFI_USER_CONNECT /*101*/:
            case WIFI_WIFIPRO_EXCEPTION_EVENT /*122*/:
            case WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT /*125*/:
            case WIFI_CONNECT_EVENT /*214*/:
                EventStream eStream1 = IMonitor.openEventStream(DFT_WIFI_FAULT_EVENT);
                eStream1.setParam((short) 0, "WIFI ERROR");
                eStream1.setParam((short) 1, type);
                eStream1.setTime(time.getTime());
                IMonitor.sendEvent(eStream1);
                IMonitor.closeEventStream(eStream1);
                break;
            case WIFI_ACCESS_INTERNET_FAILED /*87*/:
            case WIFI_ACCESS_WEB_SLOWLY /*102*/:
                EventStream eStream2 = IMonitor.openEventStream(DFT_WIFI_FAULT_EVENT_WITH_LOG);
                eStream2.setParam((short) 0, "WIFI ERROR WITH LOG");
                eStream2.setParam((short) 1, type);
                eStream2.setTime(time.getTime());
                IMonitor.sendEvent(eStream2);
                IMonitor.closeEventStream(eStream2);
                break;
            default:
                Log.w(LOG_TAG, "unkown type");
                break;
        }
        return true;
    }

    public boolean reportGPSException(int type, Date time) {
        Log.d(LOG_TAG, "reportGPSException: " + type);
        switch (type) {
            case GPS_POS_ERROR_EVENT /*72*/:
                EventStream eStream = IMonitor.openEventStream(DFT_GPS_FAULT_LOG_EVENT);
                eStream.setParam((short) 0, "GPS POS ERROR");
                eStream.setParam((short) 1, type);
                eStream.setTime(time.getTime());
                IMonitor.sendEvent(eStream);
                IMonitor.closeEventStream(eStream);
                break;
            default:
                Log.w(LOG_TAG, "unkown type");
                break;
        }
        return true;
    }
}
