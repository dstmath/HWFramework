package android.zrhung.appeye;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.util.Slog;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppEyeFwkBlock extends ZrHungImpl implements Runnable {
    private static final int COMPLETED = 0;
    private static final String[] INTEREST_PROCESSES = {"surfaceflinger", "android.hardware.graphics.composer@2.1-service", "android.hardware.graphics.composer@2.2-service"};
    private static final int OVERDUE = 1;
    private static final String TAG = "AppEyeFwkBlock";
    private static AppEyeFwkBlock sAppEyeFwkBlock;
    private String BINDER_TRANS_PATH = "/sys/kernel/debug/binder/transactions";
    private String HUAWEI_BINDER_TRANS_PATH = "/sys/kernel/debug/binder/transaction_proc";
    private long checkInterval;
    private boolean configReady;
    private boolean enabled;
    private boolean freezeHappened;
    private boolean isFirstOverdue = false;
    private Thread mAppEyeFwkBlockThread;
    private ArrayList<HandlerChecker> mHandlerCheckers = new ArrayList<>();
    int mLastBlockedTid = -1;
    private HandlerChecker mMonitorChecker;
    private HandlerThread mMonitorThread;

    public final class HandlerChecker implements Runnable {
        private boolean mCompleted;
        public Object mCurrentMonitor;
        private final Handler mHandler;
        public final ArrayList<Object> mMonitors = new ArrayList<>();
        private final String mName;

        HandlerChecker(Handler handler, String name) {
            this.mHandler = handler;
            this.mName = name;
            this.mCompleted = true;
        }

        public void addMonitor(Object monitor) {
            this.mMonitors.add(monitor);
        }

        public void scheduleCheckLocked() {
            if (this.mMonitors.size() == 0 && this.mHandler.getLooper().getQueue().isPolling()) {
                this.mCompleted = true;
            } else if (this.mCompleted) {
                this.mCompleted = false;
                this.mCurrentMonitor = null;
                this.mHandler.postAtFrontOfQueue(this);
            }
        }

        public int getCompletionStateLocked() {
            if (this.mCompleted) {
                return 0;
            }
            return 1;
        }

        public String describeBlockedStateLocked() {
            if (this.mCurrentMonitor == null) {
                return "Blocked in handler on " + this.mName + " (" + this.mHandler.getLooper().getThread().getName() + ")";
            }
            return "Blocked in monitor " + this.mCurrentMonitor.getClass().getName();
        }

        public void doMonitor(Object monitor) {
            synchronized (monitor) {
            }
        }

        public void run() {
            int size = this.mMonitors.size();
            for (int i = 0; i < size; i++) {
                synchronized (AppEyeFwkBlock.this) {
                    this.mCurrentMonitor = this.mMonitors.get(i);
                }
                doMonitor(this.mCurrentMonitor);
            }
            synchronized (AppEyeFwkBlock.this) {
                this.mCompleted = true;
                this.mCurrentMonitor = null;
            }
        }
    }

    public static synchronized AppEyeFwkBlock getInstance() {
        AppEyeFwkBlock appEyeFwkBlock;
        synchronized (AppEyeFwkBlock.class) {
            if (sAppEyeFwkBlock == null) {
                sAppEyeFwkBlock = new AppEyeFwkBlock();
            }
            appEyeFwkBlock = sAppEyeFwkBlock;
        }
        return appEyeFwkBlock;
    }

    private AppEyeFwkBlock() {
        super(TAG);
        Slog.i(TAG, "Create AppEyeFwkBlock");
        this.mMonitorThread = new HandlerThread("monitor thread");
        this.mMonitorThread.start();
        this.mMonitorChecker = new HandlerChecker(this.mMonitorThread.getThreadHandler(), "monitor thread");
        this.mHandlerCheckers.add(this.mMonitorChecker);
        this.mAppEyeFwkBlockThread = null;
        this.configReady = false;
        this.enabled = true;
        this.checkInterval = 6000;
        this.freezeHappened = false;
    }

    private void addMonitor(Object monitor) {
        synchronized (this) {
            if (monitor != null) {
                try {
                    this.mMonitorChecker.addMonitor(monitor);
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    private int evaluateCheckerCompletionLocked() {
        int state = 0;
        int checkerSize = this.mHandlerCheckers.size();
        for (int i = 0; i < checkerSize; i++) {
            HandlerChecker hc = this.mHandlerCheckers.get(i);
            state = state > hc.getCompletionStateLocked() ? state : hc.getCompletionStateLocked();
        }
        return state;
    }

    public boolean start(ZrHungData args) {
        if (sAppEyeFwkBlock == null) {
            return false;
        }
        this.mAppEyeFwkBlockThread = new Thread(sAppEyeFwkBlock);
        this.mAppEyeFwkBlockThread.start();
        return true;
    }

    public boolean check(ZrHungData args) {
        Object monitor = args.get("monitor");
        if (monitor != null) {
            addMonitor(monitor);
        }
        return true;
    }

    private void getConfigure() {
        if (!this.configReady) {
            ZRHung.HungConfig cfg = ZRHung.getHungConfig(this.mWpId);
            if (cfg == null) {
                Slog.w(TAG, "Failed to get config from zrhung");
                this.configReady = true;
            } else if (cfg.status != 0) {
                if (cfg.status == -1 || cfg.status == -2) {
                    Slog.w(TAG, "config is not support or there is no config");
                    this.configReady = true;
                    this.enabled = false;
                }
            } else if (cfg.value == null) {
                Slog.w(TAG, "Failed to get config from zrhung");
                this.configReady = true;
            } else {
                String[] configs = cfg.value.split(",");
                if (configs == null) {
                    Slog.e(TAG, "Failed to parse HungConfig");
                    this.configReady = true;
                    return;
                }
                this.enabled = configs[0].equals("1");
                if (configs.length > 1) {
                    long time = 0;
                    try {
                        time = Long.valueOf(configs[1]).longValue();
                    } catch (NumberFormatException e) {
                        Slog.w(TAG, "the config string cannot be parsed as long");
                    }
                    if (time != 0) {
                        this.checkInterval = time;
                    }
                }
                this.configReady = true;
            }
        }
    }

    private void reportFreeze(short wpId, boolean withCmd) {
        try {
            int checkerSize = this.mHandlerCheckers.size();
            StringBuilder sb = new StringBuilder();
            ZrHungData data = new ZrHungData();
            StringBuilder cmd = new StringBuilder();
            if (Log.HWINFO && withCmd) {
                cmd.append("B,");
                cmd.append("p=");
                cmd.append(Process.myPid());
            }
            for (int i = 0; i < checkerSize; i++) {
                HandlerChecker hc = this.mHandlerCheckers.get(i);
                if (hc.getCompletionStateLocked() == 1) {
                    sb.append(hc.describeBlockedStateLocked());
                    sb.append("\n");
                }
                if (hc.mCurrentMonitor != null) {
                    int curBlockedTid = Thread.getLockOwnerThreadId(hc.mCurrentMonitor);
                    Log.i(TAG, "current locked tid is:" + curBlockedTid);
                    if (this.mLastBlockedTid > 0 && this.mLastBlockedTid == curBlockedTid) {
                        int curLockholderPid = catchBadproc(this.mLastBlockedTid);
                        Log.d(TAG, "locked pid is:" + curLockholderPid);
                        data.putInt("pid", curLockholderPid);
                        if (Log.HWINFO && withCmd && curLockholderPid > 0) {
                            cmd.append(",");
                            cmd.append("p=");
                            cmd.append(curLockholderPid);
                        }
                    }
                    this.mLastBlockedTid = curBlockedTid;
                }
            }
            if (System.DEBUG != 0 && withCmd) {
                for (String append : INTEREST_PROCESSES) {
                    cmd.append(",n=");
                    cmd.append(append);
                }
                cmd.append(",P=");
                cmd.append(Process.myPid());
            }
            Slog.w(TAG, sb.toString());
            sendAppEyeEvent(wpId, data, cmd.toString(), sb.toString());
        } catch (Exception ex) {
            Log.d(TAG, "sendAppEyeEvent exception: " + ex);
        }
    }

    public void run() {
        while (true) {
            synchronized (this) {
                try {
                    getConfigure();
                } catch (Exception e) {
                    Slog.w(TAG, "getconfig exception");
                }
                if (!this.enabled) {
                    Slog.w(TAG, "the function is not enabled, quit");
                    return;
                }
                int checkerSize = this.mHandlerCheckers.size();
                for (int i = 0; i < checkerSize; i++) {
                    this.mHandlerCheckers.get(i).scheduleCheckLocked();
                }
                try {
                    wait(this.checkInterval);
                    int state = evaluateCheckerCompletionLocked();
                    if (state == 0) {
                        if (this.isFirstOverdue && !this.freezeHappened) {
                            Slog.w(TAG, "systemserver recover happend");
                            sendAppEyeEvent(288, null, null, null);
                        }
                        this.freezeHappened = false;
                        this.isFirstOverdue = false;
                        this.mLastBlockedTid = -1;
                    } else if (state == 1) {
                        Slog.w(TAG, "systemserver overdue happend");
                        if (!this.isFirstOverdue) {
                            Slog.w(TAG, "systemserver first overdue");
                            this.isFirstOverdue = true;
                            reportFreeze(270, true);
                        } else if (!this.freezeHappened) {
                            Slog.w(TAG, "systemserver freeze happened");
                            this.freezeHappened = true;
                            reportFreeze(271, false);
                        } else {
                            Slog.w(TAG, "freeze happened agin, don't repeat report");
                        }
                    }
                } catch (InterruptedException e2) {
                    Slog.w(TAG, "error msg :" + e2.getMessage());
                }
            }
        }
    }

    public int getLockOwnerPid(Object lock) {
        if (lock == null) {
            return -1;
        }
        int lock_holder_tid = Thread.getLockOwnerThreadId(lock);
        int pid = catchBadproc(lock_holder_tid);
        Log.i(TAG, "blocking in thread:" + lock_holder_tid + " remote:" + pid);
        return pid;
    }

    private int parseTransactionLine(String str, int tid, Pattern pattern) {
        Matcher matcher = pattern.matcher(str);
        if (!matcher.find()) {
            return -1;
        }
        Slog.i(TAG, "parseTransactionLine1 : " + tid + " " + matcher.group(1) + ":" + matcher.group(2) + " " + matcher.group(3) + ":" + matcher.group(4));
        if (tid == Integer.parseInt(matcher.group(2))) {
            return Integer.parseInt(matcher.group(3));
        }
        return -1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00fa, code lost:
        r13 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0101, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0102, code lost:
        r1 = r0;
        android.util.Slog.e(TAG, "IOException " + r0);
     */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x021d A[SYNTHETIC, Splitter:B:100:0x021d] */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x0243 A[SYNTHETIC, Splitter:B:109:0x0243] */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x0264 A[SYNTHETIC, Splitter:B:115:0x0264] */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x0285 A[SYNTHETIC, Splitter:B:121:0x0285] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x016e A[SYNTHETIC, Splitter:B:66:0x016e] */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x018f A[SYNTHETIC, Splitter:B:72:0x018f] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x01b0 A[SYNTHETIC, Splitter:B:78:0x01b0] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x01db A[SYNTHETIC, Splitter:B:88:0x01db] */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x01fc A[SYNTHETIC, Splitter:B:94:0x01fc] */
    public int catchBadproc(int tid) {
        StringBuilder sb;
        String str;
        String regEx;
        AppEyeFwkBlock appEyeFwkBlock = this;
        BufferedReader buff = null;
        File file = new File(appEyeFwkBlock.HUAWEI_BINDER_TRANS_PATH);
        InputStream in = null;
        Reader reader = null;
        int index = 0;
        int ret = -1;
        boolean isHwTransLog = true;
        if (!file.exists()) {
            Slog.w(TAG, "file not exists : " + appEyeFwkBlock.HUAWEI_BINDER_TRANS_PATH);
            file = new File(appEyeFwkBlock.BINDER_TRANS_PATH);
            isHwTransLog = false;
            if (!file.exists()) {
                Slog.w(TAG, "file not exists : " + appEyeFwkBlock.BINDER_TRANS_PATH);
                return -1;
            }
        }
        boolean isHwTransLog2 = isHwTransLog;
        try {
            in = new FileInputStream(file);
            reader = new InputStreamReader(in, "UTF-8");
            buff = new BufferedReader(reader);
            if (!isHwTransLog2) {
                regEx = "outgoing transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+)";
            } else {
                regEx = "[\\s\\S]*\t(\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+) wait:(\\d+).(\\d+) s";
            }
            Pattern pattern = Pattern.compile(regEx);
            String readLine = buff.readLine();
            while (true) {
                String readLine2 = readLine;
                if (readLine2 == null) {
                    break;
                }
                try {
                    ret = appEyeFwkBlock.parseTransactionLine(readLine2, tid, pattern);
                    if (ret > 0) {
                        try {
                            buff.close();
                        } catch (IOException e) {
                            IOException iOException = e;
                            Slog.e(TAG, "IOException " + e);
                        }
                        try {
                            reader.close();
                        } catch (IOException e2) {
                            IOException iOException2 = e2;
                            Slog.e(TAG, "IOException " + e2);
                        }
                        try {
                            in.close();
                        } catch (IOException e3) {
                            IOException iOException3 = e3;
                            Slog.e(TAG, "IOException " + e3);
                        }
                        return ret;
                    }
                    readLine = buff.readLine();
                    index++;
                    appEyeFwkBlock = this;
                } catch (FileNotFoundException e4) {
                    e = e4;
                    Slog.e(TAG, "FileNotFoundException " + e);
                    if (buff != null) {
                        try {
                            buff.close();
                        } catch (IOException e5) {
                            IOException iOException4 = e5;
                            Slog.e(TAG, "IOException " + e5);
                        }
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e6) {
                            IOException iOException5 = e6;
                            Slog.e(TAG, "IOException " + e6);
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e7) {
                            e = e7;
                            IOException iOException6 = e;
                            str = TAG;
                            sb = new StringBuilder();
                            sb.append("IOException ");
                            sb.append(e);
                            Slog.e(str, sb.toString());
                            return ret;
                        }
                    }
                    return ret;
                } catch (IOException e8) {
                    e = e8;
                    try {
                        Slog.e(TAG, "IOException " + e);
                        if (buff != null) {
                            try {
                                buff.close();
                            } catch (IOException e9) {
                                IOException iOException7 = e9;
                                Slog.e(TAG, "IOException " + e9);
                            }
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e10) {
                                IOException iOException8 = e10;
                                Slog.e(TAG, "IOException " + e10);
                            }
                        }
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e11) {
                                e = e11;
                                IOException iOException9 = e;
                                str = TAG;
                                sb = new StringBuilder();
                            }
                        }
                        return ret;
                    } catch (Throwable th) {
                        e = th;
                        IOException iOException10 = e;
                        if (buff != null) {
                        }
                        if (reader != null) {
                        }
                        if (in != null) {
                        }
                        throw iOException10;
                    }
                }
            }
        } catch (FileNotFoundException e12) {
            e = e12;
            int i = tid;
            Slog.e(TAG, "FileNotFoundException " + e);
            if (buff != null) {
            }
            if (reader != null) {
            }
            if (in != null) {
            }
            return ret;
        } catch (IOException e13) {
            e = e13;
            int i2 = tid;
            Slog.e(TAG, "IOException " + e);
            if (buff != null) {
            }
            if (reader != null) {
            }
            if (in != null) {
            }
            return ret;
        } catch (Throwable th2) {
            e = th2;
            int i3 = tid;
            IOException iOException102 = e;
            if (buff != null) {
                try {
                    buff.close();
                } catch (IOException e14) {
                    IOException iOException11 = e14;
                    Slog.e(TAG, "IOException " + e14);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e15) {
                    IOException iOException12 = e15;
                    Slog.e(TAG, "IOException " + e15);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e16) {
                    IOException iOException13 = e16;
                    Slog.e(TAG, "IOException " + e16);
                }
            }
            throw iOException102;
        }
        return ret;
        try {
            reader.close();
        } catch (IOException e17) {
            IOException iOException14 = e17;
            Slog.e(TAG, "IOException " + e17);
        }
        try {
            in.close();
            break;
        } catch (IOException e18) {
            e = e18;
            IOException iOException15 = e;
            str = TAG;
            sb = new StringBuilder();
            sb.append("IOException ");
            sb.append(e);
            Slog.e(str, sb.toString());
            return ret;
        }
        return ret;
        in.close();
        break;
        return ret;
    }
}
