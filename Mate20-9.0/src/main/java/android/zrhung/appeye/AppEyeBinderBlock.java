package android.zrhung.appeye;

import android.os.HandlerThread;
import android.os.Process;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.io.IoUtils;

public class AppEyeBinderBlock extends ZrHungImpl {
    public static final String CONSTANTPATH = "/sys/kernel/debug/binder/proc/";
    private static final int MAX_BINDER_CALL_DEPTH = 2;
    public static final int PROCESS_ERROR = -1;
    public static final int PROCESS_IS_NATIVE = 1;
    public static final int PROCESS_NOT_NATIVE = 0;
    static final String TAG = "AppEyeBinderBlock";
    private static AppEyeBinderBlock mInstance;
    private static final Object mLock = new Object();
    private static final SparseIntArray mPidMap = new SparseIntArray();
    private String BINDER_TRANS_PATH = "/sys/kernel/debug/binder/transactions";
    private String HUAWEI_BINDER_TRANS_PATH = "/sys/kernel/debug/binder/transaction_proc";
    private int mBlockSourcePid = -1;
    private boolean mConfiged = false;
    private boolean mEnableMinimizeDumpList = false;
    private HashMap<Integer, Set<Integer>> mExpiredBinderPidLists = new HashMap<>();
    private HandlerThread mHanderThread = new HandlerThread("writingThread");
    private ArrayList<String> mInterestedNativeStack = new ArrayList<>();
    private StringBuffer sb = new StringBuffer();

    public class ReadTransactionThread implements Runnable {
        public ReadTransactionThread() {
        }

        public void run() {
            AppEyeBinderBlock.this.readTransaction();
        }
    }

    private AppEyeBinderBlock(String wpName) {
        super(wpName);
        this.mHanderThread.start();
    }

    public static AppEyeBinderBlock getInstance(String wpName) {
        if (mInstance == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new AppEyeBinderBlock(wpName);
                }
            }
        }
        return mInstance;
    }

    private void getConfigure() {
        if (!this.mConfiged) {
            ZRHung.HungConfig cfg = ZRHung.getHungConfig(267);
            if (cfg == null) {
                Slog.w(TAG, "Failed to get config from zrhung");
                this.mConfiged = true;
            } else if (cfg.status != 0) {
                if (cfg.status == -1 || cfg.status == -2) {
                    Slog.w(TAG, "config is not support or there is no config");
                    this.mConfiged = true;
                    this.mEnableMinimizeDumpList = false;
                }
            } else if (cfg.value == null) {
                Slog.w(TAG, "Failed to get config from zrhung");
                this.mConfiged = true;
            } else {
                String[] configs = cfg.value.split(",");
                if (configs.length < 1) {
                    Slog.e(TAG, "Wrong Config size");
                    this.mConfiged = true;
                    return;
                }
                this.mEnableMinimizeDumpList = configs[0].equals("1");
                this.mConfiged = true;
                initInterestedNativeProcessList(configs);
            }
        }
    }

    private void initInterestedNativeProcessList(String[] configs) {
        if (configs != null && configs.length >= 2) {
            this.mInterestedNativeStack.clear();
            for (int i = 1; i < configs.length; i++) {
                this.mInterestedNativeStack.add(configs[i]);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0037, code lost:
        if (r1.equals("addBinderPid") != false) goto L_0x0045;
     */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0049  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0055  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0059  */
    public boolean check(ZrHungData args) {
        char c = 0;
        if (args == null) {
            return false;
        }
        if (!this.mConfiged) {
            getConfigure();
        }
        String method = args.getString("method");
        int hashCode = method.hashCode();
        if (hashCode != -1394102005) {
            if (hashCode != -1062463520) {
                if (hashCode == 1366434877 && method.equals("readTransactionInSubThread")) {
                    c = 1;
                    switch (c) {
                        case 0:
                            ArrayList<Integer> notNativeList = args.getIntegerArrayList("notnativepids");
                            ArrayList<Integer> nativeList = args.getIntegerArrayList("nativepids");
                            int pid = args.getInt("pid");
                            int tid = args.getInt("tid");
                            this.mBlockSourcePid = pid;
                            clearDumpStackPidListIfNeeded(notNativeList, nativeList, pid, tid, this.mEnableMinimizeDumpList);
                            addBinderPid(notNativeList, nativeList, pid, tid);
                            break;
                        case 1:
                            readTransactionInSubThread();
                            break;
                        case 2:
                            String path = args.getString("path");
                            if (path != null) {
                                writeTransactionToTrace(path);
                                break;
                            }
                            break;
                    }
                    return true;
                }
            }
        } else if (method.equals("writeTransactionToTrace")) {
            c = 2;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
            return true;
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
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
        String[] nativeProcs = new String[this.mInterestedNativeStack.size()];
        for (int i = 0; i < nativeProcs.length; i++) {
            if (nativeProcs[i] == null) {
                nativeProcs[i] = "";
            }
        }
        this.mInterestedNativeStack.toArray(nativeProcs);
        int[] pidList = Process.getPidsForCommands(nativeProcs);
        if (pidList != null) {
            for (int pid : pidList) {
                nativeList.add(Integer.valueOf(pid));
            }
        }
    }

    public void addBinderPid(ArrayList<Integer> notNativeList, ArrayList<Integer> nativeList, int pid, int tid) {
        try {
            ArrayList<Integer> serverPidList = new ArrayList<>();
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
        } catch (Exception ex) {
            Slog.d(TAG, "exception info ex:" + ex);
        }
    }

    private Set<Integer> getIndirectBlockedBinderPidList(int pid) {
        Set<Integer> blockedBinderPidList = new HashSet<>();
        this.mExpiredBinderPidLists.clear();
        parseTransactionLogs(pid);
        blockedBinderPidList.add(Integer.valueOf(pid));
        for (int i = 0; i < 2; i++) {
            for (Integer intValue : new HashSet<>(blockedBinderPidList)) {
                Set<Integer> pids = this.mExpiredBinderPidLists.get(Integer.valueOf(intValue.intValue()));
                if (pids != null) {
                    blockedBinderPidList.addAll(pids);
                }
            }
        }
        blockedBinderPidList.remove(Integer.valueOf(pid));
        blockedBinderPidList.remove(Integer.valueOf(Process.myPid()));
        return blockedBinderPidList;
    }

    private void parseTransactionLogs(int pid) {
        String regEx;
        String str;
        StringBuilder sb2;
        InputStream in = null;
        Reader reader = null;
        if (this.mExpiredBinderPidLists.size() == 0) {
        }
        boolean isHuaweiLog = false;
        File file = new File(this.HUAWEI_BINDER_TRANS_PATH);
        if (file.exists()) {
            regEx = "([\\s\\S]*)\t(\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+) wait:(\\d+).(\\d+) s";
            isHuaweiLog = true;
        } else {
            file = new File(this.BINDER_TRANS_PATH);
            if (!file.exists()) {
                Slog.w(TAG, "file not exists : " + this.BINDER_TRANS_PATH);
                return;
            }
            regEx = "(outgoing) transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+) .+";
        }
        boolean isHuaweiLog2 = isHuaweiLog;
        String regEx2 = regEx;
        try {
            InputStream in2 = new FileInputStream(file);
            Reader reader2 = new InputStreamReader(in2, "UTF-8");
            BufferedReader buff = new BufferedReader(reader2);
            Pattern pattern = Pattern.compile(regEx2);
            for (String readLine = buff.readLine(); readLine != null; readLine = buff.readLine()) {
                Matcher matcher = pattern.matcher(readLine);
                String from_pid = null;
                String to_pid = null;
                String costTime = null;
                if (matcher.find()) {
                    from_pid = matcher.group(2);
                    to_pid = matcher.group(4);
                    if (isHuaweiLog2) {
                        costTime = matcher.group(7);
                    }
                }
                if (from_pid == null) {
                } else if (to_pid == null) {
                    String str2 = readLine;
                } else {
                    int fromPid = Integer.parseInt(from_pid);
                    int toPid = Integer.parseInt(to_pid);
                    if (isHuaweiLog2 && costTime != null) {
                        int time_cost = Integer.parseInt(costTime);
                        if (this.mBlockSourcePid != fromPid) {
                            String str3 = readLine;
                            if (time_cost < 1) {
                            }
                            updateExpiredBinderPidList(fromPid, toPid);
                        }
                    }
                    updateExpiredBinderPidList(fromPid, toPid);
                }
            }
            try {
                reader2.close();
            } catch (IOException e) {
                IOException iOException = e;
                Slog.e(TAG, "exception info e:" + e);
            }
            try {
                in2.close();
            } catch (IOException e2) {
                e = e2;
                IOException iOException2 = e;
                str = TAG;
                sb2 = new StringBuilder();
            }
        } catch (Exception e3) {
            Slog.e(TAG, "exception info e:" + e3);
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                    IOException iOException3 = e4;
                    Slog.e(TAG, "exception info e:" + e4);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e5) {
                    e = e5;
                    IOException iOException4 = e;
                    str = TAG;
                    sb2 = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e6) {
                    IOException iOException5 = e6;
                    Slog.e(TAG, "exception info e:" + e6);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e7) {
                    IOException iOException6 = e7;
                    Slog.e(TAG, "exception info e:" + e7);
                }
            }
            throw th2;
        }
        sb2.append("exception info e:");
        sb2.append(e);
        Slog.e(str, sb2.toString());
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
            return Integer.parseInt(lineString.substring(beginNumber, lineString.indexOf(" ", beginNumber)));
        } catch (IOException ex) {
            Slog.e(TAG, "exception info e:" + ex);
            return -1;
        }
    }

    private static boolean isZygoteProcess(int pid) {
        try {
            String lineString = IoUtils.readFileAsString("/proc/" + pid + "/stat");
            if (lineString != null) {
                int beginNumber = lineString.indexOf("(") + 1;
                if (lineString.substring(beginNumber, lineString.indexOf(")", beginNumber)).equals("main")) {
                    return true;
                }
            }
        } catch (IOException ex) {
            Slog.e(TAG, "exception info e:" + ex);
        }
        return false;
    }

    public static String readProcName(String pid) {
        try {
            String content = IoUtils.readFileAsString("/proc/" + pid + "/comm");
            if (content != null) {
                String[] segments = content.split("\n");
                if (segments.length > 0 && segments[0].trim().length() > 0) {
                    return segments[0];
                }
            }
            return "unknown";
        } catch (IOException e) {
            return "unknown";
        }
    }

    private void updateExpiredBinderPidList(int fromPid, int toPid) {
        Set<Integer> set = this.mExpiredBinderPidLists.get(Integer.valueOf(fromPid));
        if (set == null) {
            set = new HashSet<>();
        }
        set.add(Integer.valueOf(toPid));
        this.mExpiredBinderPidLists.put(Integer.valueOf(fromPid), set);
    }

    private static String getBlockedBinderInfo(String str, SparseIntArray PidMap) {
        String str2;
        Matcher matcher = Pattern.compile("([\\s\\S]*)\t(\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+) wait:(\\d+).(\\d+) s").matcher(str);
        if (!matcher.find()) {
            return null;
        }
        String from_pid = matcher.group(2);
        String from_tid = matcher.group(3);
        String to_pid = matcher.group(4);
        String to_tid = matcher.group(5);
        if (PidMap != null) {
            PidMap.put(Integer.parseInt(from_pid), Integer.parseInt(from_tid));
            PidMap.put(Integer.parseInt(to_pid), Integer.parseInt(to_tid));
        }
        StringBuilder sb2 = new StringBuilder();
        if (matcher.group(1).length() > 0) {
            str2 = "[" + matcher.group(1) + "]\t";
        } else {
            str2 = "\t";
        }
        sb2.append(str2);
        sb2.append(from_pid);
        sb2.append(":");
        sb2.append(from_tid);
        sb2.append("(");
        sb2.append(readProcName(from_pid));
        sb2.append(":");
        sb2.append(readProcName(from_tid));
        sb2.append(") -> ");
        sb2.append(to_pid);
        sb2.append(":");
        sb2.append(to_tid);
        sb2.append("(");
        sb2.append(readProcName(to_pid));
        sb2.append(":");
        sb2.append(readProcName(to_tid));
        sb2.append(") code ");
        sb2.append(matcher.group(6));
        sb2.append(" wait:");
        sb2.append(matcher.group(7));
        sb2.append(".");
        sb2.append(matcher.group(8));
        sb2.append(" s\n");
        return sb2.toString();
    }

    private static String getHuaweiBinderProcInfo(String str) {
        String str2;
        Matcher matcher = Pattern.compile("(\\d+)\t([\\s\\S]*)\t(\\d+)\t(\\d+)\t(\\d+)\t(\\d+)\t(\\d+)").matcher(str);
        String Suffix = "\t< -- ";
        boolean needSuffix = false;
        if (!matcher.find()) {
            return null;
        }
        int pid = Integer.parseInt(matcher.group(1));
        String context = matcher.group(2);
        int requested_threads = Integer.parseInt(matcher.group(3));
        int requested_threads_started = Integer.parseInt(matcher.group(4));
        int max_threads = Integer.parseInt(matcher.group(5));
        int ready_threads = Integer.parseInt(matcher.group(6));
        int free_async_space = Integer.parseInt(matcher.group(7));
        if (ready_threads + requested_threads == 0 && requested_threads_started >= max_threads && max_threads != 0) {
            Suffix = Suffix + "no binder thread";
            needSuffix = true;
        }
        if (free_async_space < 102400) {
            if (needSuffix) {
                Suffix = Suffix + " & ";
            }
            needSuffix = true;
            Suffix = Suffix + "binder memory < 100KB";
        }
        if (mPidMap.get(pid, -1) < 0 && free_async_space >= 102400) {
            if (!readProcName(pid + "").equals("system_server")) {
                return null;
            }
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append(pid);
        sb2.append("\t");
        if (context.length() >= 8) {
            str2 = context;
        } else {
            str2 = context + "\t";
        }
        sb2.append(str2);
        sb2.append("\t");
        sb2.append(max_threads);
        sb2.append("\t\t");
        sb2.append(ready_threads);
        sb2.append("\t\t");
        sb2.append(requested_threads);
        sb2.append("\t\t\t");
        sb2.append(requested_threads_started);
        sb2.append("\t\t\t\t\t");
        sb2.append(free_async_space);
        sb2.append(needSuffix ? Suffix : "");
        sb2.append("\n");
        return sb2.toString();
    }

    private static String getTransactionLine(String str) {
        Matcher matcher = Pattern.compile("outgoing transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+) .+").matcher(str);
        if (!matcher.find()) {
            return null;
        }
        return "\t" + matcher.group(1) + ":" + matcher.group(2) + "(" + readProcName(matcher.group(1)) + ":" + readProcName(matcher.group(2)) + ") -> " + matcher.group(3) + ":" + matcher.group(4) + "(" + readProcName(matcher.group(3)) + ":" + readProcName(matcher.group(4)) + ") code: " + matcher.group(5) + "\n";
    }

    public void readTransactionInSubThread() {
        try {
            this.mHanderThread.getThreadHandler().post(new ReadTransactionThread());
        } catch (Exception ex) {
            Slog.d(TAG, "exception info:" + ex);
        }
    }

    public void writeTransactionToTrace(String tracesPath) {
        String str;
        StringBuilder sb2;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tracesPath, true);
            out.write(this.sb.toString().getBytes("UTF-8"));
            try {
                out.close();
            } catch (IOException e) {
                e = e;
                str = TAG;
                sb2 = new StringBuilder();
                sb2.append("exception info e:");
                sb2.append(e);
                Slog.e(str, sb2.toString());
            }
        } catch (Exception ex) {
            Slog.e(TAG, "Exception is:" + ex);
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e2) {
                    e = e2;
                    str = TAG;
                    sb2 = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e3) {
                    Slog.e(TAG, "exception info e:" + e3);
                }
            }
            throw th;
        }
    }

    public void readTransaction() {
        String str;
        StringBuilder sb2;
        String str2;
        StringBuilder sb3;
        Slog.d(TAG, "read binder transaction begin");
        this.sb.setLength(0);
        int index = 0;
        boolean huaweiTransactionFileExist = true;
        mPidMap.clear();
        this.sb.append("\n----- binder transactions -----\n");
        new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        this.sb.append(sDateFormat.format(new Date()) + "\n");
        File file_huawei_transaction = new File(this.HUAWEI_BINDER_TRANS_PATH);
        if (!file_huawei_transaction.exists()) {
            Slog.w(TAG, "file not exists : " + this.HUAWEI_BINDER_TRANS_PATH);
            huaweiTransactionFileExist = false;
        }
        if (huaweiTransactionFileExist) {
            InputStream in_ht = null;
            Reader reader_ht = null;
            SparseIntArray sparseIntArray = null;
            BufferedReader buff_ht = null;
            try {
                InputStream in_ht2 = new FileInputStream(file_huawei_transaction);
                Reader reader_ht2 = new InputStreamReader(in_ht2, "UTF-8");
                BufferedReader buff_ht2 = new BufferedReader(reader_ht2);
                String readLine_ht = buff_ht2.readLine();
                boolean hasPringTableTitle = false;
                int countOfBlockedBinder = 0;
                this.sb.append("blocked binder transactions:\n");
                while (readLine_ht != null) {
                    if (!hasPringTableTitle) {
                        String ret = getBlockedBinderInfo(readLine_ht, countOfBlockedBinder < 200 ? mPidMap : sparseIntArray);
                        if (ret != null) {
                            if (countOfBlockedBinder < 200) {
                                this.sb.append(ret);
                            }
                            countOfBlockedBinder++;
                        } else {
                            if (countOfBlockedBinder > 200) {
                                this.sb.append("Too many transactions(other ");
                                this.sb.append(String.valueOf(countOfBlockedBinder - 200));
                                this.sb.append(")...\n");
                            }
                            this.sb.append("binder thread count, and memory info:\n");
                            this.sb.append("\npid\tcontext\t\tmax_threads\tready_threads\trequested_threads\trequested_threads_started\tfree async space(byte)\n");
                            hasPringTableTitle = true;
                        }
                    } else {
                        String ret2 = getHuaweiBinderProcInfo(readLine_ht);
                        if (ret2 != null) {
                            this.sb.append(ret2);
                        }
                    }
                    readLine_ht = buff_ht2.readLine();
                    index++;
                    sparseIntArray = null;
                }
                this.sb.append("----- end binder transactions -----\n");
                try {
                    in_ht2.close();
                    reader_ht2.close();
                    buff_ht2.close();
                } catch (IOException e) {
                    e = e;
                    str2 = TAG;
                    sb3 = new StringBuilder();
                    sb3.append("exception info e:");
                    sb3.append(e);
                    Slog.e(str2, sb3.toString());
                    mPidMap.clear();
                    Slog.d(TAG, "read binder transaction end");
                }
            } catch (IOException e2) {
                Slog.e(TAG, "exception info e:" + e2);
                if (in_ht != null) {
                    try {
                        in_ht.close();
                    } catch (IOException e3) {
                        e = e3;
                        str2 = TAG;
                        sb3 = new StringBuilder();
                        sb3.append("exception info e:");
                        sb3.append(e);
                        Slog.e(str2, sb3.toString());
                        mPidMap.clear();
                        Slog.d(TAG, "read binder transaction end");
                    }
                }
                if (reader_ht != null) {
                    reader_ht.close();
                }
                if (buff_ht != null) {
                    buff_ht.close();
                }
            } catch (Throwable th) {
                Throwable th2 = th;
                if (in_ht != null) {
                    try {
                        in_ht.close();
                    } catch (IOException e4) {
                        Slog.e(TAG, "exception info e:" + e4);
                        throw th2;
                    }
                }
                if (reader_ht != null) {
                    reader_ht.close();
                }
                if (buff_ht != null) {
                    buff_ht.close();
                }
                throw th2;
            }
        } else {
            BufferedReader buff = null;
            InputStream in = null;
            Reader reader = null;
            File file = new File(this.BINDER_TRANS_PATH);
            if (!file.exists()) {
                Slog.w(TAG, "file not exists : " + this.BINDER_TRANS_PATH);
            } else {
                try {
                    Thread.sleep(1600);
                } catch (InterruptedException e5) {
                    InterruptedException interruptedException = e5;
                    Slog.e(TAG, "exception info e:" + e5);
                }
            }
            try {
                InputStream in2 = new FileInputStream(file);
                Reader reader2 = new InputStreamReader(in2, "UTF-8");
                BufferedReader buff2 = new BufferedReader(reader2);
                this.sb.append("blocked binder transactions:\n");
                String readLine = buff2.readLine();
                while (readLine != null) {
                    String transaction = getTransactionLine(readLine);
                    if (transaction != null) {
                        this.sb.append(transaction);
                    }
                    readLine = buff2.readLine();
                    index++;
                }
                this.sb.append("----- end binder transactions -----\n");
                try {
                    in2.close();
                } catch (IOException e6) {
                    IOException iOException = e6;
                    Slog.e(TAG, "exception info e:" + e6);
                }
                try {
                    reader2.close();
                } catch (IOException e7) {
                    IOException iOException2 = e7;
                    Slog.e(TAG, "exception info e:" + e7);
                }
                try {
                    buff2.close();
                } catch (IOException e8) {
                    e = e8;
                    IOException iOException3 = e;
                    str = TAG;
                    sb2 = new StringBuilder();
                    sb2.append("exception info e:");
                    sb2.append(e);
                    Slog.e(str, sb2.toString());
                    mPidMap.clear();
                    Slog.d(TAG, "read binder transaction end");
                }
            } catch (FileNotFoundException e9) {
                Slog.e(TAG, "exception info e:" + e9);
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e10) {
                        IOException iOException4 = e10;
                        Slog.e(TAG, "exception info e:" + e10);
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e11) {
                        IOException iOException5 = e11;
                        Slog.e(TAG, "exception info e:" + e11);
                    }
                }
                if (buff != null) {
                    try {
                        buff.close();
                    } catch (IOException e12) {
                        e = e12;
                        IOException iOException6 = e;
                        str = TAG;
                        sb2 = new StringBuilder();
                    }
                }
            } catch (IOException e13) {
                Slog.e(TAG, "exception info e:" + e13);
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e14) {
                        IOException iOException7 = e14;
                        Slog.e(TAG, "exception info e:" + e14);
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e15) {
                        IOException iOException8 = e15;
                        Slog.e(TAG, "exception info e:" + e15);
                    }
                }
                if (buff != null) {
                    try {
                        buff.close();
                    } catch (IOException e16) {
                        e = e16;
                        IOException iOException9 = e;
                        str = TAG;
                        sb2 = new StringBuilder();
                        sb2.append("exception info e:");
                        sb2.append(e);
                        Slog.e(str, sb2.toString());
                        mPidMap.clear();
                        Slog.d(TAG, "read binder transaction end");
                    }
                }
            } catch (Throwable th3) {
                Throwable th4 = th3;
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e17) {
                        IOException iOException10 = e17;
                        Slog.e(TAG, "exception info e:" + e17);
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e18) {
                        IOException iOException11 = e18;
                        Slog.e(TAG, "exception info e:" + e18);
                    }
                }
                if (buff != null) {
                    try {
                        buff.close();
                    } catch (IOException e19) {
                        IOException iOException12 = e19;
                        Slog.e(TAG, "exception info e:" + e19);
                    }
                }
                throw th4;
            }
        }
        mPidMap.clear();
        Slog.d(TAG, "read binder transaction end");
    }

    private void removeReduntantPids(ArrayList<Integer> pids) {
        ArrayList<Integer> filteredList = new ArrayList<>();
        if (pids != null) {
            Iterator<Integer> it = pids.iterator();
            while (it.hasNext()) {
                Integer pid = it.next();
                if (!filteredList.contains(pid)) {
                    filteredList.add(pid);
                }
            }
            pids.clear();
            pids.addAll(filteredList);
        }
    }
}
