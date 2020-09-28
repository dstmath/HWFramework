package android.zrhung.appeye;

import android.net.HwNetworkPolicyManager;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;
import com.huawei.uikit.effect.BuildConfig;
import huawei.android.provider.HanziToPinyin;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.io.IoUtils;

public class AppEyeBinderBlock extends ZrHungImpl {
    private static final String BINDER_TRANS_PATH = "/sys/kernel/debug/binder/transactions";
    private static final String CHARST_NAME = "UTF-8";
    private static final int DEFAULT_CAPACITY = 16;
    private static final String FILE_NOT_EXISTS_SUFFIX = "file not exists : ";
    private static final String HUAWEI_BINDER_TRANS_PATH = "/sys/kernel/debug/binder/transaction_proc";
    private static final Object LOCK = new Object();
    private static final int MAX_BINDER_CALL_DEPTH = 2;
    private static final SparseIntArray PID_MAP = new SparseIntArray();
    public static final int PROCESS_ERROR = -1;
    public static final int PROCESS_IS_NATIVE = 1;
    public static final int PROCESS_NOT_NATIVE = 0;
    static final String TAG = "AppEyeBinderBlock";
    private static volatile AppEyeBinderBlock instance;
    private int mBlockSourcePid = -1;
    private Map<Integer, Set<Integer>> mExpiredBinderPidLists = new HashMap(16);
    private HandlerThread mHanderThread = new HandlerThread("writingThread");
    private List<String> mInterestedNativeStacks = new ArrayList(16);
    private boolean mIsConfiged = false;
    private boolean mIsEnableMinimizeDumpList = false;
    private StringBuffer mTransaction = new StringBuffer();

    private AppEyeBinderBlock(String wpName) {
        super(wpName);
        this.mHanderThread.start();
    }

    public static AppEyeBinderBlock getInstance(String wpName) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new AppEyeBinderBlock(wpName);
                }
            }
        }
        return instance;
    }

    private static int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "parseInt NumberFormatException e = " + e.getMessage());
            return -1;
        }
    }

    private void getConfigure() {
        if (!this.mIsConfiged) {
            ZRHung.HungConfig cfg = ZRHung.getHungConfig(ZRHung.APPEYE_ANR);
            if (cfg == null) {
                Slog.w(TAG, "Failed to get config from zrhung");
                this.mIsConfiged = true;
            } else if (cfg.status == 0) {
                if (cfg.value == null) {
                    Slog.w(TAG, "Failed to get config from zrhung");
                    this.mIsConfiged = true;
                    return;
                }
                String[] configs = cfg.value.split(",");
                if (configs.length < 1) {
                    Slog.e(TAG, "Wrong Config size");
                    this.mIsConfiged = true;
                    return;
                }
                this.mIsEnableMinimizeDumpList = configs[0].equals("1");
                this.mIsConfiged = true;
                initInterestedNativeProcessList(configs);
            } else if (cfg.status == -1 || cfg.status == -2) {
                Slog.w(TAG, "config is not support or there is no config");
                this.mIsConfiged = true;
                this.mIsEnableMinimizeDumpList = false;
            }
        }
    }

    private void initInterestedNativeProcessList(String[] configs) {
        if (configs != null && configs.length >= 2) {
            this.mInterestedNativeStacks.clear();
            for (String config : configs) {
                this.mInterestedNativeStacks.add(config);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0038, code lost:
        if (r1.equals("addBinderPid") != false) goto L_0x0046;
     */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0048  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x005d  */
    @Override // android.zrhung.ZrHungImpl
    public boolean check(ZrHungData args) {
        String path;
        boolean z = false;
        if (args == null) {
            return false;
        }
        if (!this.mIsConfiged) {
            getConfigure();
        }
        String method = args.getString("method");
        int hashCode = method.hashCode();
        if (hashCode != -1394102005) {
            if (hashCode != -1062463520) {
                if (hashCode == 1366434877 && method.equals("readTransactionInSubThread")) {
                    z = true;
                    if (!z) {
                        ArrayList<Integer> notNativeList = args.getIntegerArrayList("notnativepids");
                        ArrayList<Integer> nativeList = args.getIntegerArrayList("nativepids");
                        int pid = args.getInt("pid");
                        int tid = args.getInt("tid");
                        this.mBlockSourcePid = pid;
                        clearDumpStackPidListIfNeeded(notNativeList, nativeList, pid, tid, this.mIsEnableMinimizeDumpList);
                        addBinderPid(notNativeList, nativeList, pid, tid);
                    } else if (z) {
                        readTransactionInSubThread();
                    } else if (z && (path = args.getString("path")) != null) {
                        writeTransactionToTrace(path);
                    }
                    return true;
                }
            }
        } else if (method.equals("writeTransactionToTrace")) {
            z = true;
            if (!z) {
            }
            return true;
        }
        z = true;
        if (!z) {
        }
        return true;
    }

    private void clearDumpStackPidListIfNeeded(ArrayList<Integer> notNativeList, ArrayList<Integer> nativeList, int pid, int tid, boolean isNeedClearList) {
        if (pid != Process.myPid()) {
            if (isNeedClearList) {
                if (nativeList != null) {
                    nativeList.clear();
                    updateNativeDumpStackPidList(nativeList);
                }
                if (notNativeList != null) {
                    notNativeList.clear();
                    notNativeList.add(Integer.valueOf(pid));
                    notNativeList.add(Integer.valueOf(Process.myPid()));
                }
            }
            Slog.i(TAG, " isNeedClearDumpBackStackTracePidList:" + isNeedClearList);
        }
    }

    private void updateNativeDumpStackPidList(ArrayList<Integer> nativeList) {
        String[] nativeProcs = new String[this.mInterestedNativeStacks.size()];
        this.mInterestedNativeStacks.toArray(nativeProcs);
        for (int i = 0; i < nativeProcs.length; i++) {
            if (nativeProcs[i] == null) {
                nativeProcs[i] = BuildConfig.FLAVOR;
            }
        }
        int[] pidLists = Process.getPidsForCommands(nativeProcs);
        if (pidLists != null) {
            for (int pid : pidLists) {
                nativeList.add(Integer.valueOf(pid));
            }
        }
    }

    public void addBinderPid(ArrayList<Integer> notNativeList, ArrayList<Integer> nativeList, int pid, int tid) {
        List<Integer> serverPidList = new ArrayList<>(16);
        serverPidList.addAll(getIndirectBlockedBinderPidList(pid));
        int length = serverPidList.size();
        for (int i = 0; i < length; i++) {
            if (isNativeProcess(serverPidList.get(i).intValue()) == 1) {
                nativeList.add(serverPidList.get(i));
            } else if (isNativeProcess(serverPidList.get(i).intValue()) == 0) {
                notNativeList.add(serverPidList.get(i));
            }
        }
        removeReduntantPids(notNativeList);
        removeReduntantPids(nativeList);
    }

    private Set<Integer> getIndirectBlockedBinderPidList(int pid) {
        Set<Integer> blockedBinderPidLists = new HashSet<>(16);
        this.mExpiredBinderPidLists.clear();
        parseTransactionLogs(pid);
        blockedBinderPidLists.add(Integer.valueOf(pid));
        for (int i = 0; i < 2; i++) {
            for (Integer num : new HashSet<>(blockedBinderPidLists)) {
                Set<Integer> pids = this.mExpiredBinderPidLists.get(Integer.valueOf(num.intValue()));
                if (pids != null) {
                    blockedBinderPidLists.addAll(pids);
                }
            }
        }
        blockedBinderPidLists.remove(Integer.valueOf(pid));
        blockedBinderPidLists.remove(Integer.valueOf(Process.myPid()));
        return blockedBinderPidLists;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004f, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0050, code lost:
        $closeResource(r7, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0053, code lost:
        throw r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0056, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0057, code lost:
        $closeResource(r6, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005a, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005d, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x005e, code lost:
        $closeResource(r5, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0061, code lost:
        throw r6;
     */
    private void parseTransactionLogs(int pid) {
        String regEx;
        boolean isHuaweiLog = false;
        File file = new File(HUAWEI_BINDER_TRANS_PATH);
        if (file.exists()) {
            regEx = "([\\s\\S]*)\t(\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+) wait:(\\d+).(\\d+) s";
            isHuaweiLog = true;
        } else {
            file = new File(BINDER_TRANS_PATH);
            if (!file.exists()) {
                Slog.w(TAG, "file not exists : /sys/kernel/debug/binder/transactions");
                return;
            }
            regEx = "(outgoing) transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+) .+";
        }
        try {
            InputStream in = new FileInputStream(file);
            Reader reader = new InputStreamReader(in, CHARST_NAME);
            BufferedReader buff = new BufferedReader(reader);
            parseLogs(buff, regEx, isHuaweiLog);
            $closeResource(null, buff);
            $closeResource(null, reader);
            $closeResource(null, in);
        } catch (IOException e) {
            Slog.e(TAG, "exception info e");
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    private void parseLogs(BufferedReader buff, String regEx, boolean isHuaweiLog) throws IOException {
        String readLine = buff.readLine();
        Pattern pattern = Pattern.compile(regEx);
        while (readLine != null) {
            Matcher matcher = pattern.matcher(readLine);
            String pidFrom = null;
            String pidTo = null;
            String costTime = null;
            if (matcher.find()) {
                pidFrom = matcher.group(2);
                pidTo = matcher.group(4);
                if (isHuaweiLog) {
                    costTime = matcher.group(7);
                }
            }
            if (!(pidFrom == null || pidTo == null)) {
                int fromPid = parseInt(pidFrom);
                int toPid = parseInt(pidTo);
                if (isHuaweiLog && costTime != null) {
                    int timeCost = parseInt(costTime);
                    if (this.mBlockSourcePid != fromPid && timeCost < 1) {
                    }
                }
                updateExpiredBinderPidList(fromPid, toPid);
            }
            readLine = buff.readLine();
        }
    }

    public static int isNativeProcess(int pid) {
        if (pid <= 0) {
            Slog.w(TAG, "pid less than 0, pid is " + pid);
            return -1;
        }
        int fatherPid = getFatherPid(pid);
        if (fatherPid <= 0 || !isZygoteProcess(fatherPid)) {
            return 1;
        }
        return 0;
    }

    private static int getFatherPid(int pid) {
        try {
            String lineString = IoUtils.readFileAsString("/proc/" + pid + "/stat");
            if (lineString == null) {
                return -1;
            }
            int beginNumber = lineString.indexOf(")") + 4;
            return parseInt(lineString.substring(beginNumber, lineString.indexOf(HanziToPinyin.Token.SEPARATOR, beginNumber)));
        } catch (IOException ex) {
            Slog.e(TAG, "exception info e:" + ex);
            return -1;
        }
    }

    private static boolean isZygoteProcess(int pid) {
        try {
            String lineString = IoUtils.readFileAsString("/proc/" + pid + "/stat");
            if (lineString == null) {
                return false;
            }
            int beginNumber = lineString.indexOf("(") + 1;
            if ("main".equals(lineString.substring(beginNumber, lineString.indexOf(")", beginNumber)))) {
                return true;
            }
            return false;
        } catch (IOException ex) {
            Slog.e(TAG, "exception info e:" + ex);
            return false;
        }
    }

    public static String readProcName(String pid) {
        if (pid == null) {
            return "unknow";
        }
        try {
            String content = IoUtils.readFileAsString("/proc/" + pid + "/comm");
            if (content == null) {
                return "unknow";
            }
            String[] segments = content.split(System.lineSeparator());
            if (segments.length <= 0 || segments[0].trim().length() <= 0) {
                return "unknow";
            }
            return segments[0];
        } catch (IOException e) {
            Slog.e(TAG, "readProcName read IOException");
            return "unknow";
        }
    }

    private void updateExpiredBinderPidList(int fromPid, int toPid) {
        Set<Integer> sets = this.mExpiredBinderPidLists.get(Integer.valueOf(fromPid));
        if (sets == null) {
            sets = new HashSet();
        }
        sets.add(Integer.valueOf(toPid));
        this.mExpiredBinderPidLists.put(Integer.valueOf(fromPid), sets);
    }

    private static String getBlockedBinderInfo(String str, SparseIntArray pidMap) {
        String str2;
        Matcher matcher = Pattern.compile("([\\s\\S]*)\t(\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+) wait:(\\d+).(\\d+) s").matcher(str);
        if (!matcher.find()) {
            return null;
        }
        String fromPid = matcher.group(2);
        String fromTid = matcher.group(3);
        String toPid = matcher.group(4);
        String toTid = matcher.group(5);
        if (pidMap != null) {
            pidMap.put(parseInt(fromPid), parseInt(fromTid));
            pidMap.put(parseInt(toPid), parseInt(toTid));
        }
        StringBuilder sb = new StringBuilder();
        if (matcher.group(1).length() > 0) {
            str2 = "[" + matcher.group(1) + "]\t";
        } else {
            str2 = "\t";
        }
        sb.append(str2);
        sb.append(fromPid);
        sb.append(":");
        sb.append(fromTid);
        sb.append("(");
        sb.append(readProcName(fromPid));
        sb.append(":");
        sb.append(readProcName(fromTid));
        sb.append(") -> ");
        sb.append(toPid);
        sb.append(":");
        sb.append(toTid);
        sb.append("(");
        sb.append(readProcName(toPid));
        sb.append(":");
        sb.append(readProcName(toTid));
        sb.append(") code ");
        sb.append(matcher.group(6));
        sb.append(" wait:");
        sb.append(matcher.group(7));
        sb.append(".");
        sb.append(matcher.group(8));
        sb.append(" s");
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    private static String getHuaweiBinderProcInfo(String str) {
        String str2;
        Matcher matcher = Pattern.compile("(\\d+)\t([\\s\\S]*)\t(\\d+)\t(\\d+)\t(\\d+)\t(\\d+)\t(\\d+)").matcher(str);
        if (!matcher.find()) {
            return null;
        }
        int pid = parseInt(matcher.group(1));
        String context = matcher.group(2);
        int requestedThreads = parseInt(matcher.group(3));
        int requestedThreadsStarted = parseInt(matcher.group(4));
        int maxThreads = parseInt(matcher.group(5));
        int readyThreads = parseInt(matcher.group(6));
        int freeAsyncSpace = parseInt(matcher.group(7));
        String suffix = "\t< -- ";
        boolean isNeedSuffix = false;
        if (readyThreads + requestedThreads == 0 && requestedThreadsStarted >= maxThreads && maxThreads != 0) {
            suffix = suffix + "no binder thread";
            isNeedSuffix = true;
        }
        if (freeAsyncSpace < 102400) {
            if (isNeedSuffix) {
                suffix = suffix + " & ";
            }
            isNeedSuffix = true;
            suffix = suffix + "binder memory < 100KB";
        }
        int i = PID_MAP.get(pid, -1);
        String str3 = BuildConfig.FLAVOR;
        if (i < 0 && freeAsyncSpace >= 102400) {
            if (!readProcName(pid + str3).equals("system_server")) {
                return null;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(pid);
        sb.append("\t");
        if (context.length() >= 8) {
            str2 = context;
        } else {
            str2 = context + "\t";
        }
        sb.append(str2);
        sb.append("\t");
        sb.append(maxThreads);
        sb.append("\t\t");
        sb.append(readyThreads);
        sb.append("\t\t");
        sb.append(requestedThreads);
        sb.append("\t\t\t");
        sb.append(requestedThreadsStarted);
        sb.append("\t\t\t\t\t");
        sb.append(freeAsyncSpace);
        if (isNeedSuffix) {
            str3 = suffix;
        }
        sb.append(str3);
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    private static String getTransactionLine(String str) {
        Matcher matcher = Pattern.compile("outgoing transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+) .+").matcher(str);
        if (!matcher.find()) {
            return null;
        }
        return "\t" + matcher.group(1) + ":" + matcher.group(2) + "(" + readProcName(matcher.group(1)) + ":" + readProcName(matcher.group(2)) + ") -> " + matcher.group(3) + ":" + matcher.group(4) + "(" + readProcName(matcher.group(3)) + ":" + readProcName(matcher.group(4)) + ") code: " + matcher.group(5) + System.lineSeparator();
    }

    public class ReadTransactionThread implements Runnable {
        public ReadTransactionThread() {
        }

        public void run() {
            AppEyeBinderBlock.this.readTransaction();
        }
    }

    public void readTransactionInSubThread() {
        this.mHanderThread.getThreadHandler().post(new ReadTransactionThread());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0022, code lost:
        throw r3;
     */
    public void writeTransactionToTrace(String tracesPath) {
        try {
            FileOutputStream out = new FileOutputStream(tracesPath, true);
            out.write(this.mTransaction.toString().getBytes(CHARST_NAME));
            $closeResource(null, out);
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "file not found exception");
        } catch (IOException e2) {
            Slog.e(TAG, "find IOException");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0095, code lost:
        r10 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0096, code lost:
        $closeResource(r9, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0099, code lost:
        throw r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x009c, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x009d, code lost:
        $closeResource(r8, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00a0, code lost:
        throw r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00a3, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00a4, code lost:
        $closeResource(r7, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a7, code lost:
        throw r8;
     */
    public void readTransaction() {
        Slog.d(TAG, "read binder transaction begin");
        this.mTransaction.setLength(0);
        boolean isHuaweiTransactionFileExist = true;
        PID_MAP.clear();
        StringBuffer stringBuffer = this.mTransaction;
        stringBuffer.append(System.lineSeparator() + "----- binder transactions -----" + System.lineSeparator());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        this.mTransaction.append(dateFormat.format(new Date()) + System.lineSeparator());
        File fileHuaweiTransaction = new File(HUAWEI_BINDER_TRANS_PATH);
        if (!fileHuaweiTransaction.exists()) {
            Slog.w(TAG, "file not exists : /sys/kernel/debug/binder/transaction_proc");
            isHuaweiTransactionFileExist = false;
        }
        if (isHuaweiTransactionFileExist) {
            try {
                InputStream inHt = new FileInputStream(fileHuaweiTransaction);
                Reader readerHt = new InputStreamReader(inHt, CHARST_NAME);
                BufferedReader buffHt = new BufferedReader(readerHt);
                readTransaction(buffHt, 0);
                $closeResource(null, buffHt);
                $closeResource(null, readerHt);
                $closeResource(null, inHt);
            } catch (IOException e) {
                Slog.e(TAG, "IO exception found in readTransaction");
            }
        } else {
            readTransaction(0);
        }
        PID_MAP.clear();
        Slog.d(TAG, "read binder transaction end");
    }

    private void readTransaction(BufferedReader buffHt, int index) throws IOException {
        String readLineHt = buffHt.readLine();
        boolean isPringTableTitle = false;
        int countOfBlockedBinder = 0;
        StringBuffer stringBuffer = this.mTransaction;
        stringBuffer.append("blocked binder transactions:" + System.lineSeparator());
        int readIndex = index;
        while (readLineHt != null) {
            if (!isPringTableTitle) {
                String ret = getBlockedBinderInfo(readLineHt, countOfBlockedBinder < 200 ? PID_MAP : null);
                if (ret != null) {
                    checkCountOfBlockBinder(countOfBlockedBinder, HwNetworkPolicyManager.TRANSCODE_SET_HWUIDPOLICY, ret, this.mTransaction);
                    countOfBlockedBinder++;
                } else {
                    checkCountOfBlockBinder(countOfBlockedBinder, HwNetworkPolicyManager.TRANSCODE_SET_HWUIDPOLICY, ret, this.mTransaction);
                    StringBuffer stringBuffer2 = this.mTransaction;
                    stringBuffer2.append("binder thread count, and memory info:" + System.lineSeparator());
                    StringBuffer stringBuffer3 = this.mTransaction;
                    stringBuffer3.append(System.lineSeparator() + "pid\tcontext\t\tmax_threads\tready_threads\trequested_threads\trequested_threads_started\tfree async space(byte)" + System.lineSeparator());
                    isPringTableTitle = true;
                }
            } else {
                String ret2 = getHuaweiBinderProcInfo(readLineHt);
                this.mTransaction.append(ret2 == null ? BuildConfig.FLAVOR : ret2);
            }
            readLineHt = buffHt.readLine();
            readIndex++;
        }
        StringBuffer stringBuffer4 = this.mTransaction;
        stringBuffer4.append("----- end binder transactions -----" + System.lineSeparator());
    }

    private void checkCountOfBlockBinder(int countOfBlockedBinder, int maxBlockedBinderTransactionToDispaly, String ret, StringBuffer transaction) {
        if (countOfBlockedBinder < maxBlockedBinderTransactionToDispaly && ret != null) {
            transaction.append(ret);
        } else if (countOfBlockedBinder > maxBlockedBinderTransactionToDispaly && ret == null) {
            transaction.append("Too many transactions(other ");
            transaction.append(String.valueOf(countOfBlockedBinder - maxBlockedBinderTransactionToDispaly));
            transaction.append(")..." + System.lineSeparator());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0090, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0091, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0094, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0097, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0098, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x009b, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x009e, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x009f, code lost:
        $closeResource(r3, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a2, code lost:
        throw r4;
     */
    private void readTransaction(int index) {
        File file = new File(BINDER_TRANS_PATH);
        if (!file.exists()) {
            Slog.w(TAG, "file not exists : /sys/kernel/debug/binder/transactions");
        } else {
            try {
                Thread.sleep(1600);
            } catch (InterruptedException e) {
                Slog.e(TAG, "thread sleep InterruptedException in readTransaction");
            }
        }
        try {
            InputStream in = new FileInputStream(file);
            Reader reader = new InputStreamReader(in, CHARST_NAME);
            BufferedReader buff = new BufferedReader(reader);
            StringBuffer stringBuffer = this.mTransaction;
            stringBuffer.append("blocked binder transactions:" + System.lineSeparator());
            String readLine = buff.readLine();
            int readIndex = index;
            while (readLine != null) {
                String transactionLine = getTransactionLine(readLine);
                if (transactionLine != null) {
                    this.mTransaction.append(transactionLine);
                }
                readLine = buff.readLine();
                readIndex++;
            }
            StringBuffer stringBuffer2 = this.mTransaction;
            stringBuffer2.append("----- end binder transactions -----" + System.lineSeparator());
            $closeResource(null, buff);
            $closeResource(null, reader);
            $closeResource(null, in);
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "file not found exception");
        } catch (IOException e3) {
            Slog.e(TAG, "IO exception found in readTransaction");
        }
    }

    private void removeReduntantPids(ArrayList<Integer> pids) {
        List<Integer> filteredList = new ArrayList<>(16);
        if (pids != null) {
            Iterator it = pids.iterator();
            while (it.hasNext()) {
                Integer pid = (Integer) it.next();
                if (!filteredList.contains(pid)) {
                    filteredList.add(pid);
                }
            }
            pids.clear();
            pids.addAll(filteredList);
        }
    }
}
