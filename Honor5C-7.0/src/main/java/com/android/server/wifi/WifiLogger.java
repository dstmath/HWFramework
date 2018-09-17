package com.android.server.wifi;

import android.util.Base64;
import android.util.Log;
import com.android.server.wifi.WifiNative.RingBufferStatus;
import com.android.server.wifi.WifiNative.RxFateReport;
import com.android.server.wifi.WifiNative.TxFateReport;
import com.android.server.wifi.WifiNative.WifiLoggerEventHandler;
import com.android.server.wifi.util.ByteArrayRingBuffer;
import com.android.server.wifi.util.InformationElementUtil.SupportedRates;
import com.android.server.wifi.util.StringUtil;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;

class WifiLogger extends BaseWifiLogger {
    private static final boolean DBG = false;
    public static final String DRIVER_DUMP_SECTION_HEADER = "Driver state dump";
    public static final String FIRMWARE_DUMP_SECTION_HEADER = "FW Memory dump";
    public static final int MAX_ALERT_REPORTS = 1;
    public static final int MAX_BUG_REPORTS = 4;
    private static final int[] MinBufferSizes = null;
    private static final int[] MinWakeupIntervals = null;
    public static final int REPORT_REASON_ASSOC_FAILURE = 1;
    public static final int REPORT_REASON_AUTH_FAILURE = 2;
    public static final int REPORT_REASON_AUTOROAM_FAILURE = 3;
    public static final int REPORT_REASON_DHCP_FAILURE = 4;
    public static final int REPORT_REASON_NONE = 0;
    public static final int REPORT_REASON_SCAN_FAILURE = 6;
    public static final int REPORT_REASON_UNEXPECTED_DISCONNECT = 5;
    public static final int REPORT_REASON_USER_ACTION = 7;
    public static final int RING_BUFFER_BYTE_LIMIT_LARGE = 1048576;
    public static final int RING_BUFFER_BYTE_LIMIT_SMALL = 32768;
    public static final int RING_BUFFER_FLAG_HAS_ASCII_ENTRIES = 2;
    public static final int RING_BUFFER_FLAG_HAS_BINARY_ENTRIES = 1;
    public static final int RING_BUFFER_FLAG_HAS_PER_PACKET_ENTRIES = 4;
    private static final String TAG = "WifiLogger";
    public static final int VERBOSE_DETAILED_LOG_WITH_WAKEUP = 3;
    public static final int VERBOSE_LOG_WITH_WAKEUP = 2;
    public static final int VERBOSE_NORMAL_LOG = 1;
    public static final int VERBOSE_NO_LOG = 0;
    private AtomicBoolean mBugReportDone;
    private final BuildProperties mBuildProperties;
    private final WifiLoggerEventHandler mHandler;
    private boolean mIsLoggingEventHandlerRegistered;
    private final LimitedCircularArray<BugReport> mLastAlerts;
    private final LimitedCircularArray<BugReport> mLastBugReports;
    private int mLogLevel;
    private int mMaxRingBufferSizeBytes;
    private ArrayList<FateReport> mPacketFatesForLastFailure;
    private RingBufferStatus mPerPacketRingBuffer;
    private final HashMap<String, ByteArrayRingBuffer> mRingBufferData;
    private RingBufferStatus[] mRingBuffers;
    private ThreadPoolExecutor mSingleThread;
    private final WifiNative mWifiNative;
    private WifiStateMachine mWifiStateMachine;

    /* renamed from: com.android.server.wifi.WifiLogger.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ BugReport val$report;

        AnonymousClass2(BugReport val$report) {
            this.val$report = val$report;
        }

        public void run() {
            Log.d(WifiLogger.TAG, "set mBugReportDone false");
            this.val$report.logcatLines = WifiLogger.this.getLogcat(SupportedRates.MASK);
            this.val$report.kernelLogLines = WifiLogger.this.getKernelLog(SupportedRates.MASK);
            Log.d(WifiLogger.TAG, "set mBugReportDone true");
            WifiLogger.this.mBugReportDone.set(true);
        }
    }

    class BugReport {
        byte[] alertData;
        int errorCode;
        byte[] fwMemoryDump;
        LimitedCircularArray<String> kernelLogLines;
        long kernelTimeNanos;
        ArrayList<String> logcatLines;
        byte[] mDriverStateDump;
        HashMap<String, byte[][]> ringBuffers;
        long systemTimeMs;

        BugReport() {
            this.ringBuffers = new HashMap();
        }

        void clearVerboseLogs() {
            this.fwMemoryDump = null;
            this.mDriverStateDump = null;
        }

        public String toString() {
            int i;
            StringBuilder builder = new StringBuilder();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(this.systemTimeMs);
            StringBuilder append = builder.append("system time = ");
            Object[] objArr = new Object[WifiLogger.REPORT_REASON_SCAN_FAILURE];
            objArr[WifiLogger.REPORT_REASON_NONE] = c;
            objArr[WifiLogger.VERBOSE_NORMAL_LOG] = c;
            objArr[WifiLogger.VERBOSE_LOG_WITH_WAKEUP] = c;
            objArr[WifiLogger.VERBOSE_DETAILED_LOG_WITH_WAKEUP] = c;
            objArr[WifiLogger.RING_BUFFER_FLAG_HAS_PER_PACKET_ENTRIES] = c;
            objArr[WifiLogger.REPORT_REASON_UNEXPECTED_DISCONNECT] = c;
            append.append(String.format("%tm-%td %tH:%tM:%tS.%tL", objArr)).append("\n");
            long kernelTimeMs = this.kernelTimeNanos / 1000000;
            long j = kernelTimeMs % 1000;
            builder.append("kernel time = ").append(kernelTimeMs / 1000).append(".").append(r16).append("\n");
            if (this.alertData == null) {
                builder.append("reason = ").append(this.errorCode).append("\n");
            } else {
                builder.append("errorCode = ").append(this.errorCode);
                builder.append("data \n");
                builder.append(WifiLogger.compressToBase64(this.alertData)).append("\n");
            }
            if (this.kernelLogLines != null) {
                builder.append("kernel log: \n");
                for (i = WifiLogger.REPORT_REASON_NONE; i < this.kernelLogLines.size(); i += WifiLogger.VERBOSE_NORMAL_LOG) {
                    builder.append((String) this.kernelLogLines.get(i)).append("\n");
                }
                builder.append("\n");
            }
            if (this.logcatLines != null) {
                builder.append("system log: \n");
                for (i = WifiLogger.REPORT_REASON_NONE; i < this.logcatLines.size(); i += WifiLogger.VERBOSE_NORMAL_LOG) {
                    builder.append((String) this.logcatLines.get(i)).append("\n");
                }
                builder.append("\n");
            }
            for (Entry<String, byte[][]> e : this.ringBuffers.entrySet()) {
                byte[][] buffers = (byte[][]) e.getValue();
                builder.append("ring-buffer = ").append((String) e.getKey()).append("\n");
                int size = WifiLogger.REPORT_REASON_NONE;
                for (i = WifiLogger.REPORT_REASON_NONE; i < buffers.length; i += WifiLogger.VERBOSE_NORMAL_LOG) {
                    size += buffers[i].length;
                }
                byte[] buffer = new byte[size];
                int index = WifiLogger.REPORT_REASON_NONE;
                for (i = WifiLogger.REPORT_REASON_NONE; i < buffers.length; i += WifiLogger.VERBOSE_NORMAL_LOG) {
                    System.arraycopy(buffers[i], WifiLogger.REPORT_REASON_NONE, buffer, index, buffers[i].length);
                    index += buffers[i].length;
                }
                builder.append(WifiLogger.compressToBase64(buffer));
                builder.append("\n");
            }
            if (this.fwMemoryDump != null) {
                builder.append(WifiLogger.FIRMWARE_DUMP_SECTION_HEADER);
                builder.append("\n");
                builder.append(WifiLogger.compressToBase64(this.fwMemoryDump));
                builder.append("\n");
            }
            if (this.mDriverStateDump != null) {
                builder.append(WifiLogger.DRIVER_DUMP_SECTION_HEADER);
                if (StringUtil.isAsciiPrintable(this.mDriverStateDump)) {
                    builder.append(" (ascii)\n");
                    builder.append(new String(this.mDriverStateDump, Charset.forName("US-ASCII")));
                    builder.append("\n");
                } else {
                    builder.append(" (base64)\n");
                    builder.append(WifiLogger.compressToBase64(this.mDriverStateDump));
                }
            }
            return builder.toString();
        }
    }

    class LimitedCircularArray<E> {
        private ArrayList<E> mArrayList;
        private int mMax;

        LimitedCircularArray(int max) {
            this.mArrayList = new ArrayList(max);
            this.mMax = max;
        }

        public final void addLast(E e) {
            if (this.mArrayList.size() >= this.mMax) {
                this.mArrayList.remove(WifiLogger.REPORT_REASON_NONE);
            }
            this.mArrayList.add(e);
        }

        public final int size() {
            return this.mArrayList.size();
        }

        public final E get(int i) {
            return this.mArrayList.get(i);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.WifiLogger.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.WifiLogger.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiLogger.<clinit>():void");
    }

    public WifiLogger(WifiStateMachine wifiStateMachine, WifiNative wifiNative, BuildProperties buildProperties) {
        this.mLogLevel = REPORT_REASON_NONE;
        this.mMaxRingBufferSizeBytes = RING_BUFFER_BYTE_LIMIT_SMALL;
        this.mLastAlerts = new LimitedCircularArray(VERBOSE_NORMAL_LOG);
        this.mLastBugReports = new LimitedCircularArray(RING_BUFFER_FLAG_HAS_PER_PACKET_ENTRIES);
        this.mRingBufferData = new HashMap();
        this.mHandler = new WifiLoggerEventHandler() {
            public void onRingBufferData(RingBufferStatus status, byte[] buffer) {
                WifiLogger.this.onRingBufferData(status, buffer);
            }

            public void onWifiAlert(int errorCode, byte[] buffer) {
                WifiLogger.this.onWifiAlert(errorCode, buffer);
            }
        };
        this.mBugReportDone = new AtomicBoolean(true);
        this.mSingleThread = (ThreadPoolExecutor) Executors.newFixedThreadPool(VERBOSE_NORMAL_LOG);
        this.mWifiStateMachine = wifiStateMachine;
        this.mWifiNative = wifiNative;
        this.mBuildProperties = buildProperties;
        this.mIsLoggingEventHandlerRegistered = DBG;
    }

    public synchronized void startLogging(boolean verboseEnabled) {
        int i = RING_BUFFER_BYTE_LIMIT_LARGE;
        synchronized (this) {
            this.mFirmwareVersion = this.mWifiNative.getFirmwareVersion();
            this.mDriverVersion = this.mWifiNative.getDriverVersion();
            this.mSupportedFeatureSet = this.mWifiNative.getSupportedLoggerFeatureSet();
            if (!this.mIsLoggingEventHandlerRegistered) {
                this.mIsLoggingEventHandlerRegistered = this.mWifiNative.setLoggingEventHandler(this.mHandler);
            }
            if (verboseEnabled) {
                this.mLogLevel = VERBOSE_LOG_WITH_WAKEUP;
                this.mMaxRingBufferSizeBytes = RING_BUFFER_BYTE_LIMIT_LARGE;
            } else {
                this.mLogLevel = VERBOSE_NORMAL_LOG;
                if (!enableVerboseLoggingForDogfood()) {
                    i = RING_BUFFER_BYTE_LIMIT_SMALL;
                }
                this.mMaxRingBufferSizeBytes = i;
                clearVerboseLogs();
            }
            if (this.mRingBuffers == null) {
                fetchRingBuffers();
            }
            if (this.mRingBuffers != null) {
                stopLoggingAllBuffers();
                resizeRingBuffers();
                startLoggingAllExceptPerPacketBuffers();
            }
            if (!this.mWifiNative.startPktFateMonitoring()) {
                Log.e(TAG, "Failed to start packet fate monitoring");
            }
        }
    }

    public synchronized void startPacketLog() {
        if (this.mPerPacketRingBuffer != null) {
            startLoggingRingBuffer(this.mPerPacketRingBuffer);
        }
    }

    public synchronized void stopPacketLog() {
        if (this.mPerPacketRingBuffer != null) {
            stopLoggingRingBuffer(this.mPerPacketRingBuffer);
        }
    }

    public synchronized void stopLogging() {
        if (this.mIsLoggingEventHandlerRegistered) {
            if (!this.mWifiNative.resetLogHandler()) {
                Log.e(TAG, "Fail to reset log handler");
            }
            this.mIsLoggingEventHandlerRegistered = DBG;
        }
        if (this.mLogLevel != 0) {
            stopLoggingAllBuffers();
            this.mRingBuffers = null;
            this.mLogLevel = REPORT_REASON_NONE;
        }
    }

    synchronized void reportConnectionFailure() {
        this.mPacketFatesForLastFailure = fetchPacketFates();
    }

    public synchronized void captureBugReportData(int reason) {
        this.mLastBugReports.addLast(captureBugreport(reason, isVerboseLoggingEnabled()));
    }

    public synchronized void captureAlertData(int errorCode, byte[] alertData) {
        BugReport report = captureBugreport(errorCode, isVerboseLoggingEnabled());
        report.alertData = alertData;
        this.mLastAlerts.addLast(report);
    }

    public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        super.dump(pw);
        for (i = REPORT_REASON_NONE; i < this.mLastAlerts.size(); i += VERBOSE_NORMAL_LOG) {
            pw.println("--------------------------------------------------------------------");
            pw.println("Alert dump " + i);
            pw.print(this.mLastAlerts.get(i));
            pw.println("--------------------------------------------------------------------");
        }
        for (i = REPORT_REASON_NONE; i < this.mLastBugReports.size(); i += VERBOSE_NORMAL_LOG) {
            pw.println("--------------------------------------------------------------------");
            pw.println("Bug dump " + i);
            pw.print(this.mLastBugReports.get(i));
            pw.println("--------------------------------------------------------------------");
        }
        dumpPacketFates(pw);
        pw.println("--------------------------------------------------------------------");
    }

    synchronized void onRingBufferData(RingBufferStatus status, byte[] buffer) {
        ByteArrayRingBuffer ring = (ByteArrayRingBuffer) this.mRingBufferData.get(status.name);
        if (ring != null) {
            ring.appendBuffer(buffer);
        }
    }

    synchronized void onWifiAlert(int errorCode, byte[] buffer) {
        if (this.mWifiStateMachine != null) {
            this.mWifiStateMachine.sendMessage(131172, errorCode, REPORT_REASON_NONE, buffer);
        }
    }

    private boolean isVerboseLoggingEnabled() {
        return this.mLogLevel > VERBOSE_NORMAL_LOG ? true : DBG;
    }

    private void clearVerboseLogs() {
        int i;
        this.mPacketFatesForLastFailure = null;
        for (i = REPORT_REASON_NONE; i < this.mLastAlerts.size(); i += VERBOSE_NORMAL_LOG) {
            ((BugReport) this.mLastAlerts.get(i)).clearVerboseLogs();
        }
        for (i = REPORT_REASON_NONE; i < this.mLastBugReports.size(); i += VERBOSE_NORMAL_LOG) {
            ((BugReport) this.mLastBugReports.get(i)).clearVerboseLogs();
        }
    }

    private boolean fetchRingBuffers() {
        boolean z = true;
        if (this.mRingBuffers != null) {
            return true;
        }
        this.mRingBuffers = this.mWifiNative.getRingBufferStatus();
        if (this.mRingBuffers != null) {
            RingBufferStatus[] ringBufferStatusArr = this.mRingBuffers;
            int length = ringBufferStatusArr.length;
            for (int i = REPORT_REASON_NONE; i < length; i += VERBOSE_NORMAL_LOG) {
                RingBufferStatus buffer = ringBufferStatusArr[i];
                if (!this.mRingBufferData.containsKey(buffer.name)) {
                    this.mRingBufferData.put(buffer.name, new ByteArrayRingBuffer(this.mMaxRingBufferSizeBytes));
                }
                if ((buffer.flag & RING_BUFFER_FLAG_HAS_PER_PACKET_ENTRIES) != 0) {
                    this.mPerPacketRingBuffer = buffer;
                }
            }
        } else {
            Log.e(TAG, "no ring buffers found");
        }
        if (this.mRingBuffers == null) {
            z = DBG;
        }
        return z;
    }

    private void resizeRingBuffers() {
        for (ByteArrayRingBuffer byteArrayRingBuffer : this.mRingBufferData.values()) {
            byteArrayRingBuffer.resize(this.mMaxRingBufferSizeBytes);
        }
    }

    private boolean startLoggingAllExceptPerPacketBuffers() {
        int i = REPORT_REASON_NONE;
        if (this.mRingBuffers == null) {
            return DBG;
        }
        RingBufferStatus[] ringBufferStatusArr = this.mRingBuffers;
        int length = ringBufferStatusArr.length;
        while (i < length) {
            RingBufferStatus buffer = ringBufferStatusArr[i];
            if ((buffer.flag & RING_BUFFER_FLAG_HAS_PER_PACKET_ENTRIES) == 0) {
                startLoggingRingBuffer(buffer);
            }
            i += VERBOSE_NORMAL_LOG;
        }
        return true;
    }

    private boolean startLoggingRingBuffer(RingBufferStatus buffer) {
        if (this.mWifiNative.startLoggingRingBuffer(this.mLogLevel, REPORT_REASON_NONE, MinWakeupIntervals[this.mLogLevel], MinBufferSizes[this.mLogLevel], buffer.name)) {
            return true;
        }
        return DBG;
    }

    private boolean stopLoggingRingBuffer(RingBufferStatus buffer) {
        if (this.mWifiNative.startLoggingRingBuffer(REPORT_REASON_NONE, REPORT_REASON_NONE, REPORT_REASON_NONE, REPORT_REASON_NONE, buffer.name)) {
        }
        return true;
    }

    private boolean stopLoggingAllBuffers() {
        if (this.mRingBuffers != null) {
            RingBufferStatus[] ringBufferStatusArr = this.mRingBuffers;
            int length = ringBufferStatusArr.length;
            for (int i = REPORT_REASON_NONE; i < length; i += VERBOSE_NORMAL_LOG) {
                stopLoggingRingBuffer(ringBufferStatusArr[i]);
            }
        }
        return true;
    }

    private boolean getAllRingBufferData() {
        if (this.mRingBuffers == null) {
            Log.e(TAG, "Not ring buffers available to collect data!");
            return DBG;
        }
        RingBufferStatus[] ringBufferStatusArr = this.mRingBuffers;
        int length = ringBufferStatusArr.length;
        int i = REPORT_REASON_NONE;
        while (i < length) {
            RingBufferStatus element = ringBufferStatusArr[i];
            if (this.mWifiNative.getRingBufferData(element.name)) {
                i += VERBOSE_NORMAL_LOG;
            } else {
                Log.e(TAG, "Fail to get ring buffer data of: " + element.name);
                return DBG;
            }
        }
        Log.d(TAG, "getAllRingBufferData Successfully!");
        return true;
    }

    private boolean enableVerboseLoggingForDogfood() {
        return DBG;
    }

    private BugReport captureBugreport(int errorCode, boolean captureFWDump) {
        BugReport report = new BugReport();
        report.errorCode = errorCode;
        report.systemTimeMs = System.currentTimeMillis();
        report.kernelTimeNanos = System.nanoTime();
        if (this.mRingBuffers != null) {
            RingBufferStatus[] ringBufferStatusArr = this.mRingBuffers;
            int length = ringBufferStatusArr.length;
            for (int i = REPORT_REASON_NONE; i < length; i += VERBOSE_NORMAL_LOG) {
                RingBufferStatus buffer = ringBufferStatusArr[i];
                this.mWifiNative.getRingBufferData(buffer.name);
                ByteArrayRingBuffer data = (ByteArrayRingBuffer) this.mRingBufferData.get(buffer.name);
                byte[][] buffers = new byte[data.getNumBuffers()][];
                for (int i2 = REPORT_REASON_NONE; i2 < data.getNumBuffers(); i2 += VERBOSE_NORMAL_LOG) {
                    buffers[i2] = (byte[]) data.getBuffer(i2).clone();
                }
                report.ringBuffers.put(buffer.name, buffers);
            }
        }
        this.mBugReportDone.set(DBG);
        FutureTask futureTask = null;
        if (this.mSingleThread.getActiveCount() == 0) {
            Log.d(TAG, "Thread Poll is free, execute task.");
            futureTask = new FutureTask(new AnonymousClass2(report), "getLogTask");
            this.mSingleThread.execute(futureTask);
            try {
                Log.d(TAG, "execute getLogTask wait 1000 ms)");
                futureTask.get(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Log.w(TAG, "execute getLogTask catch InterruptedException.");
                futureTask.cancel(true);
            } catch (ExecutionException e2) {
                Log.w(TAG, "execute getLogTask catch ExecutionException.");
                futureTask.cancel(true);
            } catch (TimeoutException e3) {
                Log.w(TAG, "execute getLogTask catch TimeoutException.");
                futureTask.cancel(true);
            } catch (Exception e4) {
                Log.w(TAG, "execute getLogTask catch Exception.");
                futureTask.cancel(true);
            }
        }
        if (!this.mBugReportDone.get()) {
            if (futureTask != null) {
                futureTask.cancel(true);
            }
            ArrayList<String> logcatLines = new ArrayList();
            logcatLines.add("get logcat timeout!");
            report.logcatLines = logcatLines;
            LimitedCircularArray<String> kernelLogLines = new LimitedCircularArray(VERBOSE_NORMAL_LOG);
            kernelLogLines.addLast("get kernel log timeout!");
            report.kernelLogLines = kernelLogLines;
            Log.w(TAG, "get logcat&kernel log timeout!");
            this.mBugReportDone.set(true);
        }
        if (this.mSingleThread.getActiveCount() > 0) {
            Log.w(TAG, "There are still some threads running in the Thread Pool.");
        }
        if (captureFWDump) {
            report.fwMemoryDump = this.mWifiNative.getFwMemoryDump();
            report.mDriverStateDump = this.mWifiNative.getDriverStateDump();
        }
        return report;
    }

    LimitedCircularArray<BugReport> getBugReports() {
        return this.mLastBugReports;
    }

    private static String compressToBase64(byte[] input) {
        Deflater compressor = new Deflater();
        compressor.setLevel(9);
        compressor.setInput(input);
        compressor.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
        byte[] buf = new byte[1024];
        while (!compressor.finished()) {
            bos.write(buf, REPORT_REASON_NONE, compressor.deflate(buf));
        }
        try {
            compressor.end();
            bos.close();
            byte[] compressed = bos.toByteArray();
            if (compressed.length >= input.length) {
                compressed = input;
            }
            return Base64.encodeToString(compressed, REPORT_REASON_NONE);
        } catch (IOException e) {
            Log.e(TAG, "ByteArrayOutputStream close error");
            return Base64.encodeToString(input, REPORT_REASON_NONE);
        }
    }

    private ArrayList<String> getLogcat(int maxLines) {
        ArrayList<String> lines = new ArrayList(maxLines);
        try {
            String line;
            Runtime runtime = Runtime.getRuntime();
            Object[] objArr = new Object[VERBOSE_NORMAL_LOG];
            objArr[REPORT_REASON_NONE] = Integer.valueOf(maxLines);
            Process process = runtime.exec(String.format("logcat -t %d", objArr));
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                lines.add(line);
            }
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                lines.add(line);
            }
            process.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "Exception while capturing logcat" + e);
        }
        return lines;
    }

    private LimitedCircularArray<String> getKernelLog(int maxLines) {
        LimitedCircularArray<String> lines = new LimitedCircularArray(maxLines);
        String[] logLines = this.mWifiNative.readKernelLog().split("\n");
        for (int i = REPORT_REASON_NONE; i < logLines.length; i += VERBOSE_NORMAL_LOG) {
            lines.addLast(logLines[i]);
        }
        return lines;
    }

    private ArrayList<FateReport> fetchPacketFates() {
        int i;
        ArrayList<FateReport> mergedFates = new ArrayList();
        TxFateReport[] txFates = new TxFateReport[32];
        if (this.mWifiNative.getTxPktFates(txFates)) {
            i = REPORT_REASON_NONE;
            while (i < txFates.length && txFates[i] != null) {
                mergedFates.add(txFates[i]);
                i += VERBOSE_NORMAL_LOG;
            }
        }
        RxFateReport[] rxFates = new RxFateReport[32];
        if (this.mWifiNative.getRxPktFates(rxFates)) {
            i = REPORT_REASON_NONE;
            while (i < rxFates.length && rxFates[i] != null) {
                mergedFates.add(rxFates[i]);
                i += VERBOSE_NORMAL_LOG;
            }
        }
        Collections.sort(mergedFates, new Comparator<FateReport>() {
            public int compare(FateReport lhs, FateReport rhs) {
                return Long.compare(lhs.mDriverTimestampUSec, rhs.mDriverTimestampUSec);
            }
        });
        return mergedFates;
    }

    private void dumpPacketFates(PrintWriter pw) {
        dumpPacketFatesInternal(pw, "Last failed connection fates", this.mPacketFatesForLastFailure, isVerboseLoggingEnabled());
        dumpPacketFatesInternal(pw, "Latest fates", fetchPacketFates(), isVerboseLoggingEnabled());
    }

    private static void dumpPacketFatesInternal(PrintWriter pw, String description, ArrayList<FateReport> fates, boolean verbose) {
        Object[] objArr;
        if (fates == null) {
            objArr = new Object[VERBOSE_NORMAL_LOG];
            objArr[REPORT_REASON_NONE] = description;
            pw.format("No fates fetched for \"%s\"\n", objArr);
        } else if (fates.size() == 0) {
            objArr = new Object[VERBOSE_NORMAL_LOG];
            objArr[REPORT_REASON_NONE] = description;
            pw.format("HAL provided zero fates for \"%s\"\n", objArr);
        } else {
            objArr = new Object[VERBOSE_NORMAL_LOG];
            objArr[REPORT_REASON_NONE] = description;
            pw.format("--------------------- %s ----------------------\n", objArr);
            StringBuilder verboseOutput = new StringBuilder();
            pw.print(FateReport.getTableHeader());
            for (FateReport fate : fates) {
                pw.print(fate.toTableRowString());
                if (verbose) {
                    verboseOutput.append(fate.toVerboseStringWithPiiAllowed());
                    verboseOutput.append("\n");
                }
            }
            if (verbose) {
                pw.format("\n>>> VERBOSE PACKET FATE DUMP <<<\n\n", new Object[REPORT_REASON_NONE]);
                pw.print(verboseOutput.toString());
            }
            pw.println("--------------------------------------------------------------------");
        }
    }
}
