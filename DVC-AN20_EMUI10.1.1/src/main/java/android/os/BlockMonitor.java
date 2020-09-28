package android.os;

import android.app.ActivityThread;
import android.os.Binder;
import android.os.Looper;
import android.util.Jlog;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.zrhung.appeye.AppEyeUiProbe;
import com.huawei.uikit.effect.BuildConfig;
import huawei.android.provider.HanziToPinyin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockMonitor implements IBlockMonitor, Looper.Observer, Binder.ProxyTransactListener {
    private static final int BUG_TYPE = 104;
    private static final long HALF_LIMIT_FOR_COUNTING_DELAYED_MSG = 1000;
    private static boolean IS_BETA_VERSION = false;
    private static final long LIMIT_FOR_COUNTING_DELAYED_MSG = 2000;
    private static final long LIMIT_FOR_DUMPING_DELAYED_MSG = 4000;
    private static final long MAIN_THREAD_BLOCK_TIMEOUT = 4000;
    private static final long MAIN_THREAD_INPUT_TIMEOUT = 2000;
    private static final String MAIN_THREAD_MESSAGE_CALLBACK = "android.app.LoadedApk$ReceiverDispatcher$Args";
    private static final long MAX_DELAYED_MSG_LIST_SIZE = 32;
    private static final long MAX_THREAD_BLOCK_MONITOR_TIME = 3000;
    private static final long MIN_DUMP_INTERVAL = 10000;
    private static final long MIN_PROCESS_TIME = 500;
    private static final long MIN_THREAD_BLOCK_MONITOR_TIME = 1000;
    private static final int SCENE_TYPE_BINDER_BLOCK = 2903;
    private static final int SCENE_TYPE_INPUT_BLOCK = 2904;
    private static final int SCENE_TYPE_MESSAGE_BLOCK = 2901;
    private static final int SCENE_TYPE_MESSAGE_COUNT_BLOCK = 2902;
    private static final String TAG = "BlockMonitor";
    private static final int TOP_MESSAGE_TO_REPORT = 3;
    private static BlockMonitor blockMonitor = new BlockMonitor();
    private static boolean mClearDelayedMsgListNeeded = false;
    private static List<String> mDelayedMsgList = new ArrayList();
    private static long mLastDumpTime = 0;
    private static MessageRecord mLastMessageRecord = null;
    private static volatile Thread sMainThread = null;
    private AppEyeUiProbe mAppEyeUiProbe;
    private Looper mMainLooper;
    private MessageQueue mMainQueue;

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.logsystem.usertype", 0) == 3) {
            z = true;
        }
        IS_BETA_VERSION = z;
    }

    private BlockMonitor() {
    }

    public static BlockMonitor getInstance() {
        return blockMonitor;
    }

    public boolean isInMainThread() {
        if (sMainThread != null) {
            return sMainThread == Thread.currentThread();
        }
        if (Process.myPid() != Process.myTid()) {
            return false;
        }
        sMainThread = Thread.currentThread();
        return true;
    }

    private static void uploadLongTimeMessage(long processTime, Message message) {
        Jlog.d(347, getPackageName(), (int) processTime, dumpMessage(message));
        BlockRadar.upload(BUG_TYPE, SCENE_TYPE_MESSAGE_BLOCK, "Package name: " + getPackageName() + System.lineSeparator() + "The Message" + dumpMessage(message) + " took " + processTime + "ms.");
    }

    public void checkInputTime(long startTime) {
        long processTime = SystemClock.uptimeMillis() - startTime;
        if (processTime >= 2000) {
            Jlog.d(350, getPackageName(), (int) processTime, BuildConfig.FLAVOR);
            BlockRadar.upload(BUG_TYPE, SCENE_TYPE_INPUT_BLOCK, "Package name: " + getPackageName() + System.lineSeparator() + "The input took " + processTime + "ms." + System.lineSeparator());
        }
    }

    public void checkInputReceiveTime(int seq, long eventTime) {
        if (isInMainThread() && mLastMessageRecord != null) {
            long j = mLastMessageRecord.mProcessTime;
            if (SystemClock.uptimeMillis() - eventTime >= 2000 && mLastMessageRecord.mProcessTime > MIN_PROCESS_TIME) {
                Log.w(TAG, "Message " + mLastMessageRecord.mMsg + " took " + mLastMessageRecord.mProcessTime + " ms, before dispatch event, event seq = " + seq);
                mLastMessageRecord = null;
            }
        }
    }

    public void checkBinderTime(long startTime) {
        long processTime = SystemClock.uptimeMillis() - startTime;
        if (processTime >= 4000) {
            StringBuilder sb = new StringBuilder();
            sb.append("Package name: ");
            sb.append(getPackageName());
            sb.append(System.lineSeparator());
            sb.append("The binder calling took ");
            sb.append(processTime);
            sb.append("ms.");
            sb.append(System.lineSeparator());
            StringBuilder briefSb = new StringBuilder();
            StackTraceElement[] elements = new Throwable().getStackTrace();
            for (StackTraceElement element : elements) {
                briefSb.append(element.getClassName() + "." + element.getMethodName() + ";");
                sb.append(element);
                sb.append(System.lineSeparator());
            }
            Jlog.d(70, getPackageName(), (int) processTime, briefSb.toString());
            BlockRadar.upload(BUG_TYPE, SCENE_TYPE_BINDER_BLOCK, sb.toString());
        }
    }

    private static String dumpTopMessage(MessageQueue messageQueue) {
        Map<String, MessageInfo> msgMap;
        synchronized (messageQueue) {
            msgMap = new HashMap<>();
            for (Message msg = messageQueue.mMessages; msg != null; msg = msg.next) {
                String key = dumpMessage(msg);
                MessageInfo info = msgMap.get(key);
                if (info != null) {
                    info.mCount++;
                } else {
                    msgMap.put(key, new MessageInfo(key, 1));
                }
            }
        }
        List<MessageInfo> msgList = new ArrayList<>(msgMap.values());
        Collections.sort(msgList, new Comparator<MessageInfo>() {
            /* class android.os.BlockMonitor.AnonymousClass1 */

            public int compare(MessageInfo info1, MessageInfo info2) {
                if (info1.mCount < info2.mCount) {
                    return 1;
                }
                if (info1.mCount > info2.mCount) {
                    return -1;
                }
                return 0;
            }
        });
        StringBuilder sb = new StringBuilder("TOP messages waiting to deal with are:  ");
        int i = 0;
        while (i < msgList.size() && i < 3) {
            MessageInfo info2 = msgList.get(i);
            sb.append(info2.mKey);
            sb.append(", Count: ");
            sb.append(info2.mCount);
            sb.append(HanziToPinyin.Token.SEPARATOR);
            i++;
        }
        return sb.toString();
    }

    public void checkMessageDelayTime(long dispatchTime, Message msg, MessageQueue queue) {
        long msgDelayed = dispatchTime - msg.expectedDispatchTime;
        long currentTime = SystemClock.uptimeMillis();
        long processTime = currentTime - dispatchTime;
        if (processTime > MIN_PROCESS_TIME) {
            mLastMessageRecord = new MessageRecord(dumpMessage(msg), processTime);
        }
        if (processTime > 1000 && processTime <= MAX_THREAD_BLOCK_MONITOR_TIME) {
            Jlog.d(375, getPackageName(), (int) processTime, dumpMessage(msg));
        }
        if (processTime >= 4000) {
            uploadLongTimeMessage(processTime, msg);
            clearDelayedMsgList();
            mLastDumpTime = currentTime;
            return;
        }
        if (msgDelayed >= 2000 || processTime > 1000) {
            countDelayedMsg(msgDelayed, processTime, msg);
        } else if (mClearDelayedMsgListNeeded) {
            clearDelayedMsgList();
        }
        if (msgDelayed >= 4000 && SystemClock.uptimeMillis() - mLastDumpTime > MIN_DUMP_INTERVAL) {
            dumpDelayedMsgList(queue);
            clearDelayedMsgList();
            mLastDumpTime = currentTime;
        }
    }

    private static void dumpDelayedMsgList(MessageQueue queue) {
        Log.w(TAG, "Delayed messages are: ");
        for (String msgInfo : mDelayedMsgList) {
            Log.w(TAG, msgInfo);
        }
        Log.w(TAG, dumpTopMessage(queue));
    }

    private static void countDelayedMsg(long msgDelayed, long processTime, Message msg) {
        int delayedMsgListSize = mDelayedMsgList.size();
        if (((long) delayedMsgListSize) >= MAX_DELAYED_MSG_LIST_SIZE) {
            List<String> list = new ArrayList<>();
            for (int i = 16; i < delayedMsgListSize; i++) {
                list.add(mDelayedMsgList.get(i));
            }
            mDelayedMsgList = list;
        }
        List<String> list2 = mDelayedMsgList;
        list2.add(dumpMessage(msg) + ", delayed: " + msgDelayed + "ms, finish: " + processTime + "ms");
        mClearDelayedMsgListNeeded = true;
    }

    private static void clearDelayedMsgList() {
        mDelayedMsgList.clear();
        mClearDelayedMsgListNeeded = false;
    }

    private static String dumpMessage(Message message) {
        StringBuilder b = new StringBuilder();
        b.append("{");
        if (message.target != null) {
            if (message.callback != null) {
                String callbackClass = message.callback.getClass().getName();
                b.append(" callback=");
                b.append(callbackClass);
                if (MAIN_THREAD_MESSAGE_CALLBACK.equals(callbackClass)) {
                    b.append(message.callback.toString());
                }
            } else {
                b.append(" what=");
                b.append(message.what);
            }
            if (message.arg1 != 0) {
                b.append(" arg1=");
                b.append(message.arg1);
            }
            if (message.arg2 != 0) {
                b.append(" arg2=");
                b.append(message.arg2);
            }
            if (message.obj != null) {
                b.append(" obj=");
                try {
                    b.append(message.obj);
                } catch (ClassCastException e) {
                    Log.w(TAG, "Couldn't convert obj to String");
                } catch (Exception e2) {
                    Log.w(TAG, "some exception happend when dump message obj");
                }
            }
            b.append(" target=");
            b.append(message.target.getClass().getName());
        } else {
            b.append(" barrier=");
            b.append(message.arg1);
        }
        b.append(" }");
        return b.toString();
    }

    private static String getPackageName() {
        String packageName = ActivityThread.currentPackageName();
        if (packageName == null) {
            return "system_server";
        }
        return packageName;
    }

    private static long getVersionCode() {
        if (ActivityThread.currentApplication() != null) {
            return (long) ActivityThread.currentApplication().getApplicationContext().getApplicationInfo().versionCode;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public static class BlockRadar {
        private BlockRadar() {
        }

        public static void upload(int bugType, int sceneDef, String msg) {
            Log.w(BlockMonitor.TAG, msg);
            if (Log.HWINFO) {
                Trace.traceBegin(64, "BlockMonitor:" + msg);
                Trace.traceEnd(64);
            }
        }
    }

    public void initialize() {
        this.mMainLooper = Looper.getMainLooper();
        Looper looper = this.mMainLooper;
        if (looper != null) {
            this.mMainQueue = looper.getQueue();
            Looper.setObserver(this);
            if (IS_BETA_VERSION) {
                Binder.setProxyTransactListener(this);
            }
            initAppEyeUiProbe();
            return;
        }
        Log.i(TAG, "mMainLooper equals Null");
    }

    public String dumpMainMessageQueue() {
        StringBuilder sb = new StringBuilder();
        StringBuilderPrinter sbp = new StringBuilderPrinter(sb);
        MessageQueue messageQueue = this.mMainQueue;
        if (messageQueue != null) {
            messageQueue.dump(sbp, BuildConfig.FLAVOR, null);
        }
        return sb.toString();
    }

    private void initAppEyeUiProbe() {
        this.mAppEyeUiProbe = AppEyeUiProbe.get();
        AppEyeUiProbe appEyeUiProbe = this.mAppEyeUiProbe;
        if (appEyeUiProbe != null) {
            appEyeUiProbe.setBlockMonitor(this);
        }
    }

    public Object messageDispatchStarting() {
        if (!isInMainThread()) {
            return 0;
        }
        long beginTime = SystemClock.uptimeMillis();
        AppEyeUiProbe appEyeUiProbe = this.mAppEyeUiProbe;
        if (appEyeUiProbe != null) {
            appEyeUiProbe.beginDispatching(beginTime);
        }
        return Long.valueOf(beginTime);
    }

    public void messageDispatched(Object token, Message msg) {
        if (isInMainThread()) {
            AppEyeUiProbe appEyeUiProbe = this.mAppEyeUiProbe;
            if (appEyeUiProbe != null) {
                appEyeUiProbe.endDispatching();
            }
            if (IS_BETA_VERSION && this.mMainQueue != null && (token instanceof Long)) {
                checkMessageDelayTime(((Long) token).longValue(), msg, this.mMainQueue);
            }
        }
    }

    public void dispatchingThrewException(Object token, Message msg, Exception exception) {
        if (isInMainThread()) {
            Log.i(TAG, "dispatchingThrewException In MainThread");
        }
    }

    public Object onTransactStarted(IBinder binder, int transactionCode) {
        if (!isInMainThread()) {
            return 0L;
        }
        return Long.valueOf(SystemClock.uptimeMillis());
    }

    public void onTransactEnded(Object session) {
        if (isInMainThread() && (session instanceof Long)) {
            checkBinderTime(((Long) session).longValue());
        }
    }

    /* access modifiers changed from: private */
    public static class MessageInfo {
        public int mCount;
        public String mKey;

        public MessageInfo(String key, int count) {
            this.mKey = key;
            this.mCount = count;
        }
    }

    /* access modifiers changed from: private */
    public class MessageRecord {
        String mMsg;
        long mProcessTime;

        MessageRecord(String message, long time) {
            this.mMsg = message;
            this.mProcessTime = time;
        }
    }
}
