package android.zrhung.appeye;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.MessageQueueEx;
import android.os.Process;
import android.util.Log;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;
import com.huawei.android.os.HandlerThreadEx;
import com.huawei.android.util.SlogEx;
import com.huawei.libcore.lang.ThreadEx;
import com.huawei.util.LogEx;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppEyeFwkBlock extends ZrHungImpl implements Runnable {
    private static final String BINDER_TRANS_PATH = "/sys/kernel/debug/binder/transactions";
    private static final int COMPLETED = 0;
    private static final String HUAWEI_BINDER_TRANS_PATH = "/sys/kernel/debug/binder/transaction_proc";
    private static final String[] INTEREST_PROCESSES = {"surfaceflinger", "android.hardware.graphics.composer@2.1-service", "android.hardware.graphics.composer@2.2-service"};
    private static final int OVERDUE = 1;
    private static final String TAG = "AppEyeFwkBlock";
    private static AppEyeFwkBlock sAppEyeFwkBlock;
    private Thread mAppEyeFwkBlockThread;
    private long mCheckInterval;
    private List<HandlerChecker> mHandlerCheckers = new ArrayList(16);
    private boolean mIsConfigReady;
    private boolean mIsEnabled;
    private boolean mIsFreezeHappened;
    private int mLastBlockedTid = -1;
    private HandlerChecker mMonitorChecker;
    private HandlerThread mMonitorThread;

    private AppEyeFwkBlock() {
        super(TAG);
        SlogEx.i(TAG, "Create AppEyeFwkBlock");
        this.mMonitorThread = new HandlerThread("monitor thread");
        HandlerThreadEx.start(this.mMonitorThread);
        this.mMonitorChecker = new HandlerChecker(HandlerThreadEx.getThreadHandler(this.mMonitorThread), "monitor thread");
        this.mHandlerCheckers.add(this.mMonitorChecker);
        this.mAppEyeFwkBlockThread = null;
        this.mIsConfigReady = false;
        this.mIsEnabled = true;
        this.mCheckInterval = 6000;
        this.mIsFreezeHappened = false;
    }

    public final class HandlerChecker implements Runnable {
        private boolean isCompleted;
        private Object mCurrentMonitor;
        private final Handler mHandler;
        private final List<Object> mMonitors = new ArrayList(16);
        private final String mName;

        HandlerChecker(Handler handler, String name) {
            this.mHandler = handler;
            this.mName = name;
            this.isCompleted = true;
        }

        public List<Object> getMonitors() {
            return this.mMonitors;
        }

        public Object getCurrentMonitor() {
            return this.mCurrentMonitor;
        }

        public void addMonitor(Object monitor) {
            this.mMonitors.add(monitor);
        }

        public void scheduleCheckLocked() {
            if (this.mMonitors.size() == 0 && MessageQueueEx.isPolling(this.mHandler.getLooper().getQueue())) {
                this.isCompleted = true;
            } else if (this.isCompleted) {
                this.isCompleted = false;
                this.mCurrentMonitor = null;
                this.mHandler.postAtFrontOfQueue(this);
            }
        }

        public int getCompletionStateLocked() {
            if (this.isCompleted) {
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
                SlogEx.i(AppEyeFwkBlock.TAG, "do monitor");
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            int size = this.mMonitors.size();
            for (int i = 0; i < size; i++) {
                synchronized (AppEyeFwkBlock.this) {
                    this.mCurrentMonitor = this.mMonitors.get(i);
                }
                doMonitor(this.mCurrentMonitor);
            }
            synchronized (AppEyeFwkBlock.this) {
                this.isCompleted = true;
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

    private void addMonitor(Object monitor) {
        synchronized (this) {
            if (monitor != null) {
                this.mMonitorChecker.addMonitor(monitor);
            }
        }
    }

    private int evaluateCheckerCompletionLocked() {
        int state = 0;
        for (HandlerChecker hc : this.mHandlerCheckers) {
            state = state > hc.getCompletionStateLocked() ? state : hc.getCompletionStateLocked();
        }
        return state;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean start(ZrHungData args) {
        AppEyeFwkBlock appEyeFwkBlock = sAppEyeFwkBlock;
        if (appEyeFwkBlock == null) {
            return false;
        }
        this.mAppEyeFwkBlockThread = new Thread(appEyeFwkBlock);
        this.mAppEyeFwkBlockThread.start();
        return true;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean check(ZrHungData args) {
        if (args == null) {
            return false;
        }
        Object monitor = args.get("monitor");
        if (monitor == null) {
            return true;
        }
        addMonitor(monitor);
        return true;
    }

    private void getConfigure() {
        if (!this.mIsConfigReady) {
            ZRHung.HungConfig cfg = ZRHung.getHungConfig(this.mWpId);
            if (cfg == null) {
                SlogEx.w(TAG, "Failed to get config from zrhung");
                this.mIsConfigReady = true;
            } else if (cfg.status == 0) {
                if (cfg.value == null) {
                    SlogEx.w(TAG, "Failed to get config from zrhung");
                    this.mIsConfigReady = true;
                    return;
                }
                String[] configs = cfg.value.split(",");
                if (configs == null) {
                    SlogEx.e(TAG, "Failed to parse HungConfig");
                    this.mIsConfigReady = true;
                    return;
                }
                this.mIsEnabled = configs[0].equals("1");
                if (configs.length > 1) {
                    long time = 0;
                    try {
                        time = Long.valueOf(configs[1]).longValue();
                    } catch (NumberFormatException e) {
                        SlogEx.w(TAG, "the config string cannot be parsed as long");
                    }
                    if (time != 0) {
                        this.mCheckInterval = time;
                    }
                }
                this.mIsConfigReady = true;
            } else if (cfg.status == -1 || cfg.status == -2) {
                SlogEx.w(TAG, "config is not support or there is no config");
                this.mIsConfigReady = true;
                this.mIsEnabled = false;
            }
        }
    }

    private void reportFreeze() {
        int checkerSize = this.mHandlerCheckers.size();
        StringBuilder eventBuffer = new StringBuilder();
        ZrHungData data = new ZrHungData();
        StringBuilder cmd = new StringBuilder();
        if (LogEx.getLogHWInfo()) {
            cmd.append("B,");
            cmd.append("p=");
            cmd.append(Process.myPid());
        }
        for (int i = 0; i < checkerSize; i++) {
            HandlerChecker hc = this.mHandlerCheckers.get(i);
            if (hc.getCompletionStateLocked() == 1) {
                eventBuffer.append(hc.describeBlockedStateLocked());
                eventBuffer.append(System.lineSeparator());
            }
            if (hc.mCurrentMonitor != null) {
                int curBlockedTid = ThreadEx.getLockOwnerThreadId(hc.mCurrentMonitor);
                Log.i(TAG, "current locked tid is:" + curBlockedTid);
                int i2 = this.mLastBlockedTid;
                if (i2 > 0 && i2 == curBlockedTid) {
                    int curLockholderPid = catchBadproc(i2);
                    Log.d(TAG, "locked pid is:" + curLockholderPid);
                    data.putInt("pid", curLockholderPid);
                    if (LogEx.getLogHWInfo() && curLockholderPid > 0) {
                        cmd.append(",");
                        cmd.append("p=");
                        cmd.append(curLockholderPid);
                    }
                    this.mIsFreezeHappened = true;
                }
                this.mLastBlockedTid = curBlockedTid;
            }
        }
        SlogEx.w(TAG, eventBuffer.toString());
        if (this.mIsFreezeHappened) {
            sendAppEyeEvent(271, data, cmd.toString(), eventBuffer.toString());
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        while (true) {
            synchronized (this) {
                getConfigure();
                if (!this.mIsEnabled) {
                    SlogEx.w(TAG, "the function is not enabled, quit");
                    return;
                }
                int checkerSize = this.mHandlerCheckers.size();
                for (int i = 0; i < checkerSize; i++) {
                    this.mHandlerCheckers.get(i).scheduleCheckLocked();
                }
                try {
                    wait(this.mCheckInterval);
                    int state = evaluateCheckerCompletionLocked();
                    if (state == 0) {
                        this.mIsFreezeHappened = false;
                        this.mLastBlockedTid = -1;
                    } else if (state == 1) {
                        SlogEx.w(TAG, "systemserver freeze happend");
                        if (!this.mIsFreezeHappened) {
                            reportFreeze();
                        } else {
                            SlogEx.w(TAG, "freeze happened agin, don't repeat report");
                        }
                    }
                } catch (InterruptedException e) {
                    SlogEx.w(TAG, "error msg :" + e.getMessage());
                }
            }
        }
    }

    public int getLockOwnerPid(Object lock) {
        if (lock == null) {
            return -1;
        }
        int lockHolderTid = ThreadEx.getLockOwnerThreadId(lock);
        int pid = catchBadproc(lockHolderTid);
        Log.i(TAG, "blocking in thread:" + lockHolderTid + " remote:" + pid);
        return pid;
    }

    private int parseTransactionLine(String str, int tid, Pattern pattern) {
        Matcher matcher = pattern.matcher(str);
        if (!matcher.find()) {
            return -1;
        }
        SlogEx.i(TAG, "parseTransactionLine1 : " + tid + " " + matcher.group(1) + ":" + matcher.group(2) + " " + matcher.group(3) + ":" + matcher.group(4));
        if (tid == parseInt(matcher.group(2))) {
            return parseInt(matcher.group(3));
        }
        return -1;
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseInt NumberFormatException");
            return -1;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0076, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r6.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007b, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x007c, code lost:
        r7.addSuppressed(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x007f, code lost:
        throw r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0082, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0087, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0088, code lost:
        r6.addSuppressed(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x008b, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x008e, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0093, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0094, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0097, code lost:
        throw r6;
     */
    public int catchBadproc(int tid) {
        File file = new File(HUAWEI_BINDER_TRANS_PATH);
        int ret = -1;
        boolean isHwTransLog = true;
        if (!file.exists()) {
            SlogEx.w(TAG, "file not exists : /sys/kernel/debug/binder/transaction_proc");
            file = new File(BINDER_TRANS_PATH);
            isHwTransLog = false;
            if (!file.exists()) {
                SlogEx.w(TAG, "file not exists : /sys/kernel/debug/binder/transactions");
                return -1;
            }
        }
        try {
            InputStream in = new FileInputStream(file);
            Reader reader = new InputStreamReader(in, "UTF-8");
            BufferedReader buff = new BufferedReader(reader);
            String regEx = "[\\s\\S]*\t(\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+) wait:(\\d+).(\\d+) s";
            if (!isHwTransLog) {
                regEx = "outgoing transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+)";
            }
            Pattern pattern = Pattern.compile(regEx);
            String readLine = buff.readLine();
            int index = 0;
            while (readLine != null) {
                ret = parseTransactionLine(readLine, tid, pattern);
                if (ret > 0) {
                    buff.close();
                    reader.close();
                    in.close();
                    return ret;
                }
                readLine = buff.readLine();
                index++;
            }
            buff.close();
            reader.close();
            in.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found exception");
        } catch (IOException e2) {
            Log.e(TAG, "IO exception");
        }
        return ret;
    }
}
