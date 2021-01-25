package com.huawei.server.rme.hyperhold;

import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.IMonitor;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.server.ServiceThread;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Date;

public final class Statistics {
    private static final String AK_LOG_PATH = "/data/log/iaware/hyperhold/ak_kill_app.log";
    private static final String FREEZE_ACTIVE_LOG_PATH = "/data/log/iaware/hyperhold/abnormal_freeze_unfreeze.log";
    private static final String HYPERHOLD_LOG_PATH = "/data/log/iaware/hyperhold/";
    private static final String IAWARE_LOG_PATH = "/data/log/iaware/";
    private static final int PRIVILEGE = 504;
    private static final int READ_PSI = 1;
    private static final int REPORT_AK = 7;
    private static final long REPORT_AK_LOG_DELAY = 86400000;
    private static final int REPORT_APP = 3;
    private static final long REPORT_APP_DELAY = 7200000;
    private static final long REPORT_HEARTBEAT = 1800000;
    private static final int REPORT_NANDLIFE = 8;
    private static final long REPORT_NANDLIFE_DELAY = 86400000;
    private static final int REPORT_PAGEIN = 5;
    private static final long REPORT_PAGEIN_DELAY = 14400000;
    private static final int REPORT_PSI = 2;
    private static final long REPORT_PSI_DELAY = 86400000;
    private static final int REPORT_ZSWAPD = 4;
    private static final long REPORT_ZSWAPD_DELAY = 14400000;
    private static final String RUNSTATE_LOG_PATH = "/data/log/iaware/hyperhold/hyperhold_running_state.log";
    private static final String TAG_PG = "SWAP_Statistics";
    private static volatile Statistics statistics = null;
    private AppModel appModel;
    private volatile FreezeActiveInfo freezeActive;
    private Handler handler;
    private volatile boolean isPsiOpen;
    private volatile boolean isReportEnable;
    private KernelInterface kernelInterface;
    private long lastReportAppTime;
    private LogHandler logHandler;
    private ServiceThread logThread;
    private volatile NandLifeInfo nandLifeInfo;
    private volatile AbnormalPageinInfo pagein;
    private volatile long psiDelay;
    private long recordStartTime;
    private volatile long reportAkTime;
    private volatile long reportAppTime;
    private volatile long reportNandLifeTime;
    private volatile long reportPageinTime;
    private volatile long reportPsiTime;
    private volatile long reportZswapdTime;
    private volatile RunStateInfo runState;
    private long serviceStartTime;
    private volatile SwapDriverInfo swapDriverInfo;
    private volatile int userType;
    private volatile ZswapdInfo zswapdInfo;

    private Statistics() {
        this.isPsiOpen = false;
        this.isReportEnable = false;
        this.userType = 0;
        this.zswapdInfo = null;
        this.swapDriverInfo = null;
        this.freezeActive = null;
        this.runState = null;
        this.pagein = null;
        this.nandLifeInfo = null;
        this.logHandler = null;
        this.logThread = null;
        this.kernelInterface = null;
        this.handler = new Handler(BackgroundThread.get().getLooper()) {
            /* class com.huawei.server.rme.hyperhold.Statistics.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if ((!Statistics.this.isPsiOpen || !Statistics.this.isDealPsi(msg)) && Statistics.this.isReportEnable) {
                    Statistics.this.dealMsg(msg);
                }
            }
        };
        this.appModel = AppModel.getInstance();
        this.serviceStartTime = System.currentTimeMillis();
        this.recordStartTime = this.serviceStartTime;
        this.lastReportAppTime = SystemClock.elapsedRealtime();
        this.psiDelay = 100;
        this.reportPsiTime = 43200000;
        this.reportAppTime = WifiProCommonUtils.RECHECK_DELAYED_MS;
        this.reportZswapdTime = REPORT_APP_DELAY;
        this.reportPageinTime = REPORT_APP_DELAY;
        this.reportAkTime = 43200000;
        this.reportNandLifeTime = 43200000;
        this.zswapdInfo = new ZswapdInfo();
        this.swapDriverInfo = new SwapDriverInfo();
        this.freezeActive = new FreezeActiveInfo();
        this.runState = new RunStateInfo();
        this.pagein = new AbnormalPageinInfo();
        this.nandLifeInfo = new NandLifeInfo();
        beginPsiPrint();
        beginReportTimer();
        if (this.logHandler == null) {
            this.logThread = new ServiceThread("SWAP_Statistics:logger", 10, true);
            this.logThread.start();
            this.logHandler = new LogHandler(this.logThread.getLooper());
        }
    }

    public static Statistics getInstance() {
        if (statistics == null) {
            synchronized (Statistics.class) {
                if (statistics == null) {
                    statistics = new Statistics();
                }
            }
        }
        return statistics;
    }

    public void init() {
        this.kernelInterface = KernelInterface.getInstance();
        setPsiOpen(ParaConfig.getInstance().getPsiParam().getPsiOpen());
        this.psiDelay = (long) ParaConfig.getInstance().getPsiParam().getPsiDelay();
    }

    public void writeLog(String message) {
        LogHandler logHandler2 = this.logHandler;
        if (logHandler2 != null) {
            logHandler2.sendMessage(logHandler2.obtainMessage(16, 0, 0, message));
        } else {
            Slog.e(TAG_PG, "Can't write log: logHandler is null");
        }
    }

    public void reportFreezeActive() {
        if (this.freezeActive == null) {
            Slog.e(TAG_PG, "Report fail, freezeActive is null");
        } else if (this.userType != 3) {
            Slog.e(TAG_PG, "Not beta user, can't report freezeActiveInfo");
        } else {
            this.freezeActive.reportFreezeActiveInfo();
        }
    }

    public void reportRunState() {
        if (this.runState == null) {
            Slog.e(TAG_PG, "Report fail, runState is null");
        } else if (this.userType != 3) {
            Slog.e(TAG_PG, "Not beta user, can't report runstate");
        } else {
            this.runState.reportRunStateInfo();
        }
    }

    public int getUserType() {
        if (this.userType == 0) {
            this.userType = SystemProperties.getInt("ro.logsystem.usertype", -1);
            Slog.i(TAG_PG, "getUserType:" + this.userType);
        }
        return this.userType;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dealMsg(Message msg) {
        Message msgSend = this.handler.obtainMessage();
        msgSend.what = msg.what;
        int i = msg.what;
        if (i == 2) {
            this.swapDriverInfo.reportSwapDriverInfo();
            this.handler.sendMessageDelayed(msgSend, REPORT_HEARTBEAT);
        } else if (i == 3) {
            reportAppInfo();
            this.handler.sendMessageDelayed(msgSend, REPORT_HEARTBEAT);
        } else if (i == 4) {
            this.zswapdInfo.reportZswapdInfo();
            this.handler.sendMessageDelayed(msgSend, REPORT_HEARTBEAT);
        } else if (i == 5) {
            this.pagein.reportPageinInfo();
            this.handler.sendMessageDelayed(msgSend, REPORT_HEARTBEAT);
        } else if (i == 7) {
            LogHandler logHandler2 = this.logHandler;
            if (logHandler2 == null) {
                Slog.e(TAG_PG, "Can't report log: logHandler is null");
                return;
            }
            if (logHandler2.isSendTime()) {
                LogHandler logHandler3 = this.logHandler;
                logHandler3.sendMessage(logHandler3.obtainMessage(17));
            }
            this.reportAkTime = 86400000;
            this.handler.sendMessageDelayed(msgSend, REPORT_HEARTBEAT);
        } else if (i == 8) {
            this.nandLifeInfo.reportNandLifeInfo();
            this.handler.sendMessageDelayed(msgSend, REPORT_HEARTBEAT);
        }
    }

    /* access modifiers changed from: private */
    public final class LogHandler extends Handler {
        private static final int AK_LOG_MSG = 16;
        private static final int AK_LOG_UPLOAD = 17;
        private int beginTime = 0;
        private int endTime = 0;
        private long lastReportTime = SystemClock.elapsedRealtime();
        private File logFile;

        LogHandler(Looper looper) {
            super(looper, null, true);
            Path path = Paths.get("HYPERHOLD_LOG_PATH", new String[0]);
            if (!Files.exists(path, new LinkOption[0])) {
                try {
                    Files.createDirectories(path, new FileAttribute[0]);
                    Slog.i(Statistics.TAG_PG, "log_priv: try hyperhold log permission grant.");
                    if (FileUtils.setPermissions(Statistics.IAWARE_LOG_PATH, Statistics.PRIVILEGE, -1, -1) != 0) {
                        Slog.e(Statistics.TAG_PG, "log_priv: iaware folder permission grant failed.");
                    }
                    if (FileUtils.setPermissions(path.toFile(), Statistics.PRIVILEGE, -1, -1) != 0) {
                        Slog.e(Statistics.TAG_PG, "log_priv: hyperhold folder permission grant failed.");
                    }
                } catch (IOException e) {
                    Slog.i(Statistics.TAG_PG, "Can't create dir for AK log");
                }
            }
            this.logFile = new File(Statistics.AK_LOG_PATH);
            this.beginTime = Statistics.this.getIntTimestamp(System.currentTimeMillis());
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 16) {
                if (i != 17) {
                    super.handleMessage(msg);
                } else {
                    uploadAkLog();
                }
            } else if (msg.obj instanceof String) {
                byte[] arrBytes = prepareMessage((String) msg.obj).getBytes(Charset.defaultCharset());
                if (!isExceedLimit(arrBytes.length)) {
                    FileOutputStream stream = null;
                    try {
                        if (this.logFile.createNewFile()) {
                            int ret = FileUtils.setPermissions(Statistics.AK_LOG_PATH, Statistics.PRIVILEGE, -1, -1);
                            Slog.i(Statistics.TAG_PG, "log_priv: ak_kill_app.log permission grant rs:" + ret);
                        }
                        FileOutputStream stream2 = new FileOutputStream(this.logFile, true);
                        stream2.write(arrBytes);
                        try {
                            stream2.close();
                        } catch (IOException e) {
                            Slog.e(Statistics.TAG_PG, "Can't close file for AK log");
                        }
                    } catch (FileNotFoundException e2) {
                        Slog.e(Statistics.TAG_PG, "log file for AK not found");
                        if (0 != 0) {
                            stream.close();
                        }
                    } catch (IOException e3) {
                        Slog.e(Statistics.TAG_PG, "Can't create file for AK log");
                        if (0 != 0) {
                            stream.close();
                        }
                    } catch (Throwable th) {
                        if (0 != 0) {
                            try {
                                stream.close();
                            } catch (IOException e4) {
                                Slog.e(Statistics.TAG_PG, "Can't close file for AK log");
                            }
                        }
                        throw th;
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isSendTime() {
            if (SystemClock.elapsedRealtime() - this.lastReportTime < Statistics.this.reportAkTime) {
                return false;
            }
            return true;
        }

        private void uploadAkLog() {
            if (!this.logFile.exists()) {
                Slog.i(Statistics.TAG_PG, "Ak log file not generated!");
                return;
            }
            IMonitor.EventStream eventStream = IMonitor.openEventStream(905001004);
            this.endTime = Statistics.this.getIntTimestamp(System.currentTimeMillis());
            Slog.i(Statistics.TAG_PG, "Send ak_log event");
            eventStream.setParam("beginTime", this.beginTime);
            eventStream.setParam("endTime", this.endTime);
            if (IMonitor.sendEvent(eventStream)) {
                Slog.i(Statistics.TAG_PG, "Ak log send successs!");
                this.beginTime = Statistics.this.getIntTimestamp(System.currentTimeMillis());
            } else {
                Slog.e(Statistics.TAG_PG, "Ak log send failed!");
            }
            this.lastReportTime = SystemClock.elapsedRealtime();
        }

        private String prepareMessage(String msg) {
            Date date = new Date();
            return date.getTime() + " " + msg + System.lineSeparator();
        }

        private boolean isExceedLimit(int length) {
            if (this.logFile.exists() && this.logFile.length() + ((long) length) >= 307200) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public class SwapDriverInfo {
        private long apkNum;
        private long continueFragNum;
        private long externNum;
        private long hyperholdFaultCount;
        private long hyperholdFaultTime;
        private long lastReportTime;
        private long maxFaultTime;
        private long swapinAllDelay;
        private long swapinCount;
        private long swapinDataSize;
        private long swapinIoFailNum;
        private long swapinMaxDelay;
        private long swapinMemAllocFailNum;
        private long swapinTimeoutNum;
        private long swapoutAllDelay;
        private long swapoutCount;
        private long swapoutDataSize;
        private long swapoutIoFailNum;
        private long swapoutMaxDelay;
        private long swapoutMemAllocFailNum;
        private long swapoutTimeoutNum;
        private long zramFaultCount;

        private SwapDriverInfo() {
            this.swapoutCount = 0;
            this.swapoutDataSize = 0;
            this.swapinCount = 0;
            this.swapinDataSize = 0;
            this.zramFaultCount = 0;
            this.hyperholdFaultCount = 0;
            this.hyperholdFaultTime = 0;
            this.maxFaultTime = 0;
            this.swapinAllDelay = 0;
            this.swapinMaxDelay = 0;
            this.swapoutAllDelay = 0;
            this.swapoutMaxDelay = 0;
            this.swapinMemAllocFailNum = 0;
            this.swapinIoFailNum = 0;
            this.swapinTimeoutNum = 0;
            this.swapoutMemAllocFailNum = 0;
            this.swapoutIoFailNum = 0;
            this.swapoutTimeoutNum = 0;
            this.apkNum = 0;
            this.externNum = 0;
            this.continueFragNum = 0;
            this.lastReportTime = SystemClock.elapsedRealtime();
        }

        public void reportSwapDriverInfo() {
            StringBuilder sb;
            long curTime = SystemClock.elapsedRealtime();
            if (curTime - this.lastReportTime >= Statistics.this.reportPsiTime) {
                BufferedReader reader = null;
                try {
                    BufferedReader reader2 = new BufferedReader(new FileReader(new File("/dev/memcg/memory.psi_health_info")));
                    for (String lineString = reader2.readLine(); lineString != null; lineString = reader2.readLine()) {
                        parsePsiLine(lineString);
                    }
                    if (!sendSwapDriverInfo()) {
                        Slog.e(Statistics.TAG_PG, "sendSwapDriverInfo failed!");
                    }
                    if (Statistics.this.userType == 3) {
                        Statistics.this.reportPsiTime = 14400000;
                    } else {
                        Statistics.this.reportPsiTime = 86400000;
                    }
                    try {
                        reader2.close();
                    } catch (IOException e) {
                        ex = e;
                        sb = new StringBuilder();
                    }
                } catch (IOException ex) {
                    Slog.e(Statistics.TAG_PG, "Read psi file failed: " + ex);
                    if (0 != 0) {
                        try {
                            reader.close();
                        } catch (IOException e2) {
                            ex = e2;
                            sb = new StringBuilder();
                        }
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
                        try {
                            reader.close();
                        } catch (IOException ex2) {
                            Slog.e(Statistics.TAG_PG, "psi file close failed: " + ex2);
                        }
                    }
                    throw th;
                }
                this.lastReportTime = curTime;
            }
            return;
            sb.append("psi file close failed: ");
            sb.append(ex);
            Slog.e(Statistics.TAG_PG, sb.toString());
            this.lastReportTime = curTime;
        }

        private void parsePsiLine(String inputLine) {
            String line = inputLine.replace(" ", "");
            int keyEndIndex = line.indexOf(AwarenessInnerConstants.COLON_KEY);
            char c = 65535;
            if (keyEndIndex != -1 && keyEndIndex < line.length() - 1) {
                String key = line.substring(0, keyEndIndex);
                long value = Statistics.this.parseLongValue(line.substring(keyEndIndex + 1).replace("MB", ""));
                switch (key.hashCode()) {
                    case -1617957362:
                        if (key.equals("hyperhold_fault")) {
                            c = 5;
                            break;
                        }
                        break;
                    case -670407828:
                        if (key.equals("hyperhold_out_comp_size")) {
                            c = 1;
                            break;
                        }
                        break;
                    case -114919440:
                        if (key.equals("hyperhold_all_fault")) {
                            c = 4;
                            break;
                        }
                        break;
                    case 813628480:
                        if (key.equals("hyperhold_in_times")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 1287906091:
                        if (key.equals("hyperhold_in_comp_size")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 1558436354:
                        if (key.equals("hyperhold_fault_out_total_lat")) {
                            c = 6;
                            break;
                        }
                        break;
                    case 1674167553:
                        if (key.equals("hyperhold_out_times")) {
                            c = 0;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        this.swapoutCount = value;
                        return;
                    case 1:
                        this.swapoutDataSize = value;
                        return;
                    case 2:
                        this.swapinCount = value;
                        return;
                    case 3:
                        this.swapinDataSize = value;
                        return;
                    case 4:
                        this.zramFaultCount = value;
                        return;
                    case 5:
                        this.hyperholdFaultCount = value;
                        return;
                    case 6:
                        this.hyperholdFaultTime = value;
                        return;
                    default:
                        parseExternKey(key, value);
                        return;
                }
            }
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        private void parseExternKey(String key, long value) {
            char c;
            switch (key.hashCode()) {
                case -1977540581:
                    if (key.equals("hyperhold_reclaim_in_timeout_cnt")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case -1714228318:
                    if (key.equals("hyperhold_fault_out_max_lat")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1630944511:
                    if (key.equals("hyperhold_batch_out_timeout_cnt")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -1512708047:
                    if (key.equals("hyperhold_store_memcg_cnt")) {
                        c = 11;
                        break;
                    }
                    c = 65535;
                    break;
                case -1435918846:
                    if (key.equals("hyperhold_reclaim_in_alloc_fail_cnt")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case -1322865382:
                    if (key.equals("hyperhold_batch_out_total_lat")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1093777060:
                    if (key.equals("hyperhold_batch_out_alloc_fail_cnt")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -1044618874:
                    if (key.equals("hyperhold_store_extent_cnt")) {
                        c = '\f';
                        break;
                    }
                    c = 65535;
                    break;
                case -942299436:
                    if (key.equals("hyperhold_reclaim_in_max_lat")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -564155206:
                    if (key.equals("hyperhold_batch_out_max_lat")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 352749748:
                    if (key.equals("hyperhold_reclaim_in_total_lat")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 816255569:
                    if (key.equals("hyperhold_reclaim_in_io_fail_cnt")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case 1162851639:
                    if (key.equals("hyperhold_batch_out_io_fail_cnt")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1857527084:
                    if (key.equals("hyperhold_store_fragment_cnt")) {
                        c = '\r';
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    this.maxFaultTime = value;
                    return;
                case 1:
                    this.swapinAllDelay = value;
                    return;
                case 2:
                    this.swapinMaxDelay = value;
                    return;
                case 3:
                    this.swapoutAllDelay = value;
                    return;
                case 4:
                    this.swapoutMaxDelay = value;
                    return;
                case 5:
                    this.swapinMemAllocFailNum = value;
                    return;
                case 6:
                    this.swapinIoFailNum = value;
                    return;
                case 7:
                    this.swapinTimeoutNum = value;
                    return;
                case '\b':
                    this.swapoutMemAllocFailNum = value;
                    return;
                case '\t':
                    this.swapoutIoFailNum = value;
                    return;
                case '\n':
                    this.swapoutTimeoutNum = value;
                    return;
                case 11:
                    this.apkNum = value;
                    return;
                case '\f':
                    this.externNum = value;
                    return;
                case '\r':
                    this.continueFragNum = value;
                    return;
                default:
                    return;
            }
        }

        private boolean sendSwapDriverInfo() {
            long timeGone = System.currentTimeMillis() - Statistics.this.serviceStartTime;
            IMonitor.EventStream eventStream = IMonitor.openEventStream(914015000);
            eventStream.setParam("swapoutAvrTime", Statistics.this.getIntTimestamp(timeGone));
            eventStream.setParam("swapinCount", this.swapinCount);
            eventStream.setParam("swapinDataSize", this.swapinDataSize);
            eventStream.setParam("swapoutCount", this.swapoutCount);
            eventStream.setParam("swapoutDataSize", this.swapoutDataSize);
            eventStream.setParam("zramFaultCount", this.zramFaultCount);
            eventStream.setParam("eswapFaultCount", this.hyperholdFaultCount);
            eventStream.setParam("eswapFaultTime", this.hyperholdFaultTime);
            eventStream.setParam("maxFaultTime", this.maxFaultTime);
            eventStream.setParam("swapinAllDelay", this.swapinAllDelay);
            eventStream.setParam("swapinMaxDelay", this.swapinMaxDelay);
            eventStream.setParam("swapoutAllDelay", this.swapoutAllDelay);
            eventStream.setParam("swapoutMaxDelay", this.swapoutMaxDelay);
            eventStream.setParam("swapinMemAllocFailNum", this.swapinMemAllocFailNum);
            eventStream.setParam("swapinIoFailNum", this.swapinIoFailNum);
            eventStream.setParam("swapinTimeoutNum", this.swapinTimeoutNum);
            eventStream.setParam("swapoutMemAllocFailNum", this.swapoutMemAllocFailNum);
            eventStream.setParam("swapoutIoFailNum", this.swapoutIoFailNum);
            eventStream.setParam("swapoutTimeoutNum", this.swapoutTimeoutNum);
            eventStream.setParam("apkNum", this.apkNum);
            eventStream.setParam("externNum", this.externNum);
            eventStream.setParam("continueFragNum", this.continueFragNum);
            eventStream.setParam("currentswapout", Boolean.valueOf(Swap.getInstance().isSwapEnabled()));
            Slog.i(Statistics.TAG_PG, "Send sendSwapDriverInfo(914015000):");
            boolean isSendSuc = IMonitor.sendEvent(eventStream);
            if (isSendSuc) {
                Slog.i(Statistics.TAG_PG, "SwapDriverInfo send successs!");
            } else {
                Slog.e(Statistics.TAG_PG, "SwapDriverInfo send failed!");
            }
            try {
                eventStream.close();
            } catch (IOException ex) {
                Slog.e(Statistics.TAG_PG, "eventStream close failed: " + ex);
            }
            return isSendSuc;
        }
    }

    /* access modifiers changed from: private */
    public class NandLifeInfo {
        private long lastReportTime;
        private long lspace;

        private NandLifeInfo() {
            this.lspace = 0;
            this.lastReportTime = SystemClock.elapsedRealtime();
        }

        public void reportNandLifeInfo() {
            long curTime = SystemClock.elapsedRealtime();
            if (curTime - this.lastReportTime >= Statistics.this.reportNandLifeTime) {
                updateLspace();
                IMonitor.EventStream eventStream = IMonitor.openEventStream(914006000);
                eventStream.setParam("LSPACE", this.lspace);
                Slog.i(Statistics.TAG_PG, "Send NandLifeInfo(914006000), lspace: " + this.lspace + " bytes");
                if (IMonitor.sendEvent(eventStream)) {
                    Slog.i(Statistics.TAG_PG, "NandLifeInfo send successs!");
                    Statistics.this.reportNandLifeTime = 86400000;
                } else {
                    Slog.e(Statistics.TAG_PG, "NandLifeInfo send failed!");
                }
                try {
                    eventStream.close();
                } catch (IOException ex) {
                    Slog.e(Statistics.TAG_PG, "eventStream close failed: " + ex);
                }
                this.lastReportTime = curTime;
            }
        }

        private void updateLspace() {
            File path = Environment.getDataDirectory();
            if (path == null) {
                Slog.e(Statistics.TAG_PG, "getDataDirectory failed!");
                return;
            }
            StatFs stat = new StatFs(path.getPath());
            long blockSize = (long) stat.getBlockSize();
            long availableBlocks = (long) stat.getAvailableBlocks();
            if (blockSize <= 0 || availableBlocks < 0 || availableBlocks > Long.MAX_VALUE / blockSize) {
                Slog.e(Statistics.TAG_PG, "get lspace failed");
            } else {
                this.lspace = ((availableBlocks * blockSize) / 1024) / 1024;
            }
        }
    }

    /* access modifiers changed from: private */
    public class ZswapdInfo {
        private long lastReportTime;
        private long zswapdEmptyRound;
        private long zswapdEmptyRoundSkipTimes;
        private long zswapdHitRefault;
        private long zswapdMemcgRatioSkip;
        private long zswapdMemcgRefaultSkip;
        private long zswapdNotSuites;
        private long zswapdRunning;
        private long zswapdSwapout;

        private ZswapdInfo() {
            this.zswapdRunning = 0;
            this.zswapdHitRefault = 0;
            this.zswapdNotSuites = 0;
            this.zswapdMemcgRatioSkip = 0;
            this.zswapdMemcgRefaultSkip = 0;
            this.zswapdSwapout = 0;
            this.zswapdEmptyRound = 0;
            this.zswapdEmptyRoundSkipTimes = 0;
            this.lastReportTime = SystemClock.elapsedRealtime();
        }

        public void reportZswapdInfo() {
            StringBuilder sb;
            long curTime = SystemClock.elapsedRealtime();
            if (curTime - this.lastReportTime >= Statistics.this.reportZswapdTime) {
                BufferedReader reader = null;
                try {
                    BufferedReader reader2 = new BufferedReader(new FileReader(new File("/proc/vmstat")));
                    for (String lineString = reader2.readLine(); lineString != null; lineString = reader2.readLine()) {
                        parseVmstatLine(lineString);
                    }
                    if (sendZswapdInfo()) {
                        Statistics.this.reportZswapdTime = 14400000;
                    } else {
                        Slog.e(Statistics.TAG_PG, "sendZswapdInfo failed!");
                    }
                    try {
                        reader2.close();
                    } catch (IOException e) {
                        ex = e;
                        sb = new StringBuilder();
                    }
                } catch (IOException ex) {
                    Slog.e(Statistics.TAG_PG, "Read vmstat file failed: " + ex);
                    if (0 != 0) {
                        try {
                            reader.close();
                        } catch (IOException e2) {
                            ex = e2;
                            sb = new StringBuilder();
                        }
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
                        try {
                            reader.close();
                        } catch (IOException ex2) {
                            Slog.e(Statistics.TAG_PG, "vmstat file close failed: " + ex2);
                        }
                    }
                    throw th;
                }
                this.lastReportTime = curTime;
            }
            return;
            sb.append("vmstat file close failed: ");
            sb.append(ex);
            Slog.e(Statistics.TAG_PG, sb.toString());
            this.lastReportTime = curTime;
        }

        private boolean sendZswapdInfo() {
            IMonitor.EventStream eventStream = IMonitor.openEventStream(905001003);
            eventStream.setParam("zswapd_running", this.zswapdRunning);
            eventStream.setParam("zswapd_hit_refault", this.zswapdHitRefault);
            eventStream.setParam("zswapd_not_suites", this.zswapdNotSuites);
            eventStream.setParam("zswapd_memcg_ratio_skip", this.zswapdMemcgRatioSkip);
            eventStream.setParam("zswapd_memcg_refault_skip", this.zswapdMemcgRefaultSkip);
            eventStream.setParam("zswapd_swapout", this.zswapdSwapout);
            eventStream.setParam("zswapd_empty_round", this.zswapdEmptyRound);
            eventStream.setParam("zswapd_empty_round_skip", this.zswapdEmptyRoundSkipTimes);
            Slog.i(Statistics.TAG_PG, "Send ZswapdInfo(905001003)");
            boolean isSendSuc = IMonitor.sendEvent(eventStream);
            if (isSendSuc) {
                Slog.i(Statistics.TAG_PG, "ZswapdInfo send successs!");
            } else {
                Slog.e(Statistics.TAG_PG, "ZswapdInfo send failed!");
            }
            try {
                eventStream.close();
            } catch (IOException ex) {
                Slog.e(Statistics.TAG_PG, "eventStream close failed: " + ex);
            }
            return isSendSuc;
        }

        private void parseVmstatLine(String line) {
            int keyEndIndex = line.indexOf(" ");
            char c = 65535;
            if (keyEndIndex == -1 || keyEndIndex == line.length() - 1) {
                Slog.e(Statistics.TAG_PG, "parseVmstatLine err: can't find separation!");
                return;
            }
            String key = line.substring(0, keyEndIndex);
            long value = Statistics.this.parseLongValue(line.substring(keyEndIndex));
            switch (key.hashCode()) {
                case -1304498797:
                    if (key.equals("zswapd_swapout")) {
                        c = 5;
                        break;
                    }
                    break;
                case -641405311:
                    if (key.equals("zswapd_memcg_ratio_skip")) {
                        c = 3;
                        break;
                    }
                    break;
                case -239679043:
                    if (key.equals("zswapd_memcg_refault_skip")) {
                        c = 4;
                        break;
                    }
                    break;
                case 46836948:
                    if (key.equals("zswapd_empty_round")) {
                        c = 6;
                        break;
                    }
                    break;
                case 311652344:
                    if (key.equals("zswapd_hit_refaults")) {
                        c = 1;
                        break;
                    }
                    break;
                case 1061388609:
                    if (key.equals("zswapd_medium_press")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1416302801:
                    if (key.equals("zswapd_empty_round_skip_times")) {
                        c = 7;
                        break;
                    }
                    break;
                case 2057646711:
                    if (key.equals("zswapd_running")) {
                        c = 0;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    this.zswapdRunning = value;
                    return;
                case 1:
                    this.zswapdHitRefault = value;
                    return;
                case 2:
                    this.zswapdNotSuites = value;
                    return;
                case 3:
                    this.zswapdMemcgRatioSkip = value;
                    return;
                case 4:
                    this.zswapdMemcgRefaultSkip = value;
                    return;
                case 5:
                    this.zswapdSwapout = value;
                    return;
                case 6:
                    this.zswapdEmptyRound = value;
                    return;
                case 7:
                    this.zswapdEmptyRoundSkipTimes = value;
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public class FreezeActiveInfo {
        private static final int MAX_LOG_SIZE = 102400;
        private int beginTime;
        private int endTime;

        private FreezeActiveInfo() {
            this.beginTime = 0;
            this.endTime = 0;
            this.beginTime = Statistics.this.getIntTimestamp(System.currentTimeMillis());
        }

        public void reportFreezeActiveInfo() {
            File logFile = new File(Statistics.FREEZE_ACTIVE_LOG_PATH);
            if (!logFile.exists()) {
                Slog.e(Statistics.TAG_PG, "freezeActiveInfo not exists.");
            } else if (logFile.length() > 102400) {
                Slog.e(Statistics.TAG_PG, "freezeActiveInfo logfile is too large: " + logFile.length());
            } else {
                IMonitor.EventStream eventStream = IMonitor.openEventStream(905001000);
                this.endTime = Statistics.this.getIntTimestamp(System.currentTimeMillis());
                eventStream.setParam("CaseName", "FreezeActive");
                eventStream.setParam("beginTime", this.beginTime);
                eventStream.setParam("endTime", this.endTime);
                if (IMonitor.sendEvent(eventStream)) {
                    Slog.i(Statistics.TAG_PG, "FreezeActive Info send successs!");
                    this.beginTime = this.endTime;
                } else {
                    Slog.e(Statistics.TAG_PG, "FreezeActive Info send failed!");
                }
                try {
                    eventStream.close();
                } catch (IOException ex) {
                    Slog.e(Statistics.TAG_PG, "eventStream close failed: " + ex);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class RunStateInfo {
        private static final int MAX_LOG_SIZE = 102400;
        private int beginTime;
        private int endTime;

        private RunStateInfo() {
            this.beginTime = 0;
            this.endTime = 0;
            this.beginTime = Statistics.this.getIntTimestamp(System.currentTimeMillis());
        }

        public void reportRunStateInfo() {
            File logFile = new File(Statistics.RUNSTATE_LOG_PATH);
            if (!logFile.exists()) {
                Slog.e(Statistics.TAG_PG, "runStateInfo logfile not exists.");
            } else if (logFile.length() > 102400) {
                Slog.e(Statistics.TAG_PG, "runStateInfo logfile is too large: " + logFile.length());
            } else {
                IMonitor.EventStream eventStream = IMonitor.openEventStream(905001001);
                this.endTime = Statistics.this.getIntTimestamp(System.currentTimeMillis());
                eventStream.setParam("beginTime", this.beginTime);
                eventStream.setParam("endTime", this.endTime);
                if (IMonitor.sendEvent(eventStream)) {
                    Slog.i(Statistics.TAG_PG, "runStateInfo Info send successs!");
                    this.beginTime = this.endTime;
                } else {
                    Slog.e(Statistics.TAG_PG, "runStateInfo Info send failed!");
                }
                try {
                    eventStream.close();
                } catch (IOException ex) {
                    Slog.e(Statistics.TAG_PG, "eventStream close failed: " + ex);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class AbnormalPageinInfo {
        private static final int MAX_LOG_SIZE = 10240;
        private static final String PAGEIN_INFO_PATH = "/sys/block/zram0/hyperhold_report";
        private long failPoint;
        private long lastReportTime;
        private String[] pageinParam;

        private AbnormalPageinInfo() {
            this.failPoint = 0;
            this.lastReportTime = SystemClock.elapsedRealtime();
            this.pageinParam = new String[]{"time", "tname", "index", "id"};
        }

        public void reportPageinInfo() {
            StringBuilder sb;
            long curTime = SystemClock.elapsedRealtime();
            if (curTime - this.lastReportTime >= Statistics.this.reportPageinTime) {
                File pageinFile = new File(PAGEIN_INFO_PATH);
                if (!pageinFile.exists()) {
                    Slog.e(Statistics.TAG_PG, "abnormal pagein info file not exists.");
                    return;
                } else if (pageinFile.length() > 10240) {
                    Slog.e(Statistics.TAG_PG, "abnormal pagein info file is too large.");
                    return;
                } else {
                    BufferedReader reader = null;
                    try {
                        BufferedReader reader2 = new BufferedReader(new FileReader(pageinFile));
                        for (String lineString = reader2.readLine(); lineString != null; lineString = reader2.readLine()) {
                            if (parseReportLine(lineString)) {
                                sendPageinEventStream();
                            }
                        }
                        Statistics.this.reportPageinTime = 14400000;
                        try {
                            reader2.close();
                        } catch (IOException e) {
                            ex = e;
                            sb = new StringBuilder();
                        }
                    } catch (IOException ex) {
                        Slog.e(Statistics.TAG_PG, "Read pageinFile failed: " + ex);
                        if (0 != 0) {
                            try {
                                reader.close();
                            } catch (IOException e2) {
                                ex = e2;
                                sb = new StringBuilder();
                            }
                        }
                    } catch (Throwable th) {
                        if (0 != 0) {
                            try {
                                reader.close();
                            } catch (IOException ex2) {
                                Slog.e(Statistics.TAG_PG, "pageinFile close failed: " + ex2);
                            }
                        }
                        throw th;
                    }
                    this.lastReportTime = curTime;
                }
            } else {
                return;
            }
            sb.append("pageinFile close failed: ");
            sb.append(ex);
            Slog.e(Statistics.TAG_PG, sb.toString());
            this.lastReportTime = curTime;
        }

        private boolean parseReportLine(String line) {
            int headFliterIndex = line.indexOf(AwarenessInnerConstants.COLON_KEY);
            if (headFliterIndex != -1) {
                parseReportHeadLine(line, headFliterIndex);
                return false;
            }
            int startIndex = line.indexOf("[");
            int endIndex = line.indexOf("]");
            if (startIndex < 0 || endIndex < 0) {
                return false;
            }
            this.failPoint = Statistics.this.parseLongValue(line.substring(startIndex + 1, endIndex));
            int curParamIndex = 0;
            int startIndex2 = line.indexOf("[", startIndex + 1);
            int endIndex2 = line.indexOf("]", endIndex + 1);
            while (startIndex2 > 0 && endIndex2 > 0 && curParamIndex < 4) {
                this.pageinParam[curParamIndex] = line.substring(startIndex2 + 1, endIndex2);
                curParamIndex++;
                startIndex2 = line.indexOf("[", startIndex2 + 1);
                endIndex2 = line.indexOf("]", endIndex2 + 1);
            }
            Slog.i(Statistics.TAG_PG, "send abnormal pagein: " + this.failPoint);
            return true;
        }

        private void parseReportHeadLine(String line, int index) {
            if (index >= line.length() - 1) {
                Slog.i(Statistics.TAG_PG, "parseHeadLine failed.");
                return;
            }
            int value = Statistics.this.parseIntValue(line.substring(index + 1));
            Slog.i(Statistics.TAG_PG, "hyperhold_report " + value + " lines.");
        }

        private void sendPageinEventStream() {
            IMonitor.EventStream eventStream = IMonitor.openEventStream(914015001);
            eventStream.setParam("failPoint", this.failPoint);
            eventStream.setParam("abnormalProcessName", this.pageinParam[1]);
            eventStream.setParam("abnormalPage", this.pageinParam[0]);
            eventStream.setParam("abnormalZramAddr", this.pageinParam[2]);
            eventStream.setParam("abnormalEswapAddr", this.pageinParam[3]);
            if (IMonitor.sendEvent(eventStream)) {
                Slog.i(Statistics.TAG_PG, "abnormal pagein Info send successs!");
            } else {
                Slog.e(Statistics.TAG_PG, "abnormal pagein Info send failed!");
            }
            try {
                eventStream.close();
            } catch (IOException ex) {
                Slog.e(Statistics.TAG_PG, "eventStream close failed: " + ex);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDealPsi(Message msg) {
        if (msg.what != 1) {
            return false;
        }
        this.kernelInterface.printPsi();
        Message msgSend = this.handler.obtainMessage();
        msgSend.what = 1;
        this.handler.sendMessageDelayed(msgSend, this.psiDelay);
        return true;
    }

    private void reportAppInfo() {
        long curTime = SystemClock.elapsedRealtime();
        if (curTime - this.lastReportAppTime >= this.reportAppTime) {
            if (sendAppLivingInfo()) {
                this.reportAppTime = REPORT_APP_DELAY;
                this.recordStartTime = System.currentTimeMillis();
            } else {
                Slog.e(TAG_PG, "sendAppInfo failed!");
            }
            this.lastReportAppTime = curTime;
        }
    }

    private boolean sendAppLivingInfo() {
        int appLivingNum = this.appModel.getLiveAppNum();
        IMonitor.EventStream eventStream = IMonitor.openEventStream(905000007);
        eventStream.setParam("start", getIntTimestamp(this.recordStartTime));
        int curIntTime = getIntTimestamp(System.currentTimeMillis());
        eventStream.setParam("end", curIntTime);
        eventStream.setParam("activity", appLivingNum);
        Slog.i(TAG_PG, "Send LivingInfo:" + getIntTimestamp(this.recordStartTime) + "," + curIntTime + "," + appLivingNum);
        boolean isSendSuc = IMonitor.sendEvent(eventStream);
        if (isSendSuc) {
            Slog.i(TAG_PG, "AppLivingInfo(905000007) send successs!");
        } else {
            Slog.e(TAG_PG, "AppLivingInfo send failed!");
        }
        try {
            eventStream.close();
        } catch (IOException ex) {
            Slog.e(TAG_PG, "eventStream close failed: " + ex);
        }
        return isSendSuc;
    }

    private void setPsiOpen(boolean isPsiOpen2) {
        if (!isPsiOpen2) {
            this.isPsiOpen = false;
        } else if (!this.isPsiOpen) {
            this.isPsiOpen = true;
            beginPsiPrint();
        }
    }

    private void judgeReportEnable() {
        this.isReportEnable = false;
        this.userType = SystemProperties.getInt("ro.logsystem.usertype", -1);
        Slog.i(TAG_PG, "get utype:" + this.userType);
        if (this.userType == 3 || this.userType == 1) {
            this.isReportEnable = true;
        }
    }

    private void beginPsiPrint() {
        if (this.isPsiOpen) {
            Message msgPsi = this.handler.obtainMessage();
            msgPsi.what = 1;
            this.handler.sendMessageDelayed(msgPsi, this.psiDelay);
        }
    }

    private void setSingleLogPermission(String pathName) {
        if (Files.exists(Paths.get(pathName, new String[0]), new LinkOption[0])) {
            FileUtils.setPermissions(pathName, PRIVILEGE, -1, -1);
            Slog.i(TAG_PG, "reset permissions of path: " + pathName);
        }
    }

    private void setLogPermissions() {
        setSingleLogPermission(IAWARE_LOG_PATH);
        setSingleLogPermission(HYPERHOLD_LOG_PATH);
        setSingleLogPermission(AK_LOG_PATH);
        setSingleLogPermission(FREEZE_ACTIVE_LOG_PATH);
        setSingleLogPermission(RUNSTATE_LOG_PATH);
    }

    private void beginReportTimer() {
        judgeReportEnable();
        Handler handler2 = this.handler;
        if (handler2 == null) {
            Slog.e(TAG_PG, "handler not initialized!");
            return;
        }
        Message msgReport = handler2.obtainMessage();
        msgReport.what = 2;
        if (this.userType == 3) {
            this.reportPsiTime = 14400000;
            setLogPermissions();
        }
        this.handler.sendMessageDelayed(msgReport, REPORT_HEARTBEAT);
        Message msgReport2 = this.handler.obtainMessage();
        msgReport2.what = 3;
        this.handler.sendMessageDelayed(msgReport2, REPORT_HEARTBEAT);
        Message msgReport3 = this.handler.obtainMessage();
        msgReport3.what = 4;
        this.handler.sendMessageDelayed(msgReport3, REPORT_HEARTBEAT);
        Message msgReport4 = this.handler.obtainMessage();
        msgReport4.what = 8;
        this.handler.sendMessageDelayed(msgReport4, REPORT_HEARTBEAT);
        Message msgReport5 = this.handler.obtainMessage();
        msgReport5.what = 5;
        this.handler.sendMessageDelayed(msgReport5, REPORT_HEARTBEAT);
        if (this.userType == 3) {
            Message msgReport6 = this.handler.obtainMessage();
            msgReport6.what = 7;
            this.handler.sendMessageDelayed(msgReport6, REPORT_HEARTBEAT);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getIntTimestamp(long timestamp) {
        return (int) (timestamp / 1000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long parseLongValue(String strValIn) {
        try {
            return Long.parseLong(strValIn.replace(" ", ""));
        } catch (NumberFormatException e) {
            Slog.e(TAG_PG, "format long value err: " + strValIn);
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int parseIntValue(String strValIn) {
        try {
            return Integer.parseInt(strValIn.replace(" ", ""));
        } catch (NumberFormatException e) {
            Slog.e(TAG_PG, "format int value err: " + strValIn);
            return 0;
        }
    }
}
