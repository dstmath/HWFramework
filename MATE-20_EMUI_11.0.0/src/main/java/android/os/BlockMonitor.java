package android.os;

import android.rms.iaware.AwareUiRenderParallelManager;
import android.util.Jlog;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.InputEvent;
import android.zrhung.appeye.AppEyeUiProbe;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.content.pm.ApplicationInfoEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.TraceEx;
import com.huawei.dfr.zrhung.DefaultBlockMonitor;
import com.huawei.hwpartdfr.BuildConfig;
import com.huawei.util.LogEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockMonitor extends DefaultBlockMonitor {
    private static final int BUG_TYPE = 104;
    private static final long HALF_LIMIT_FOR_COUNTING_DELAYED_MSG = 1000;
    private static final boolean IS_BETA_VERSION = (SystemPropertiesEx.getInt("ro.logsystem.usertype", 0) == TOP_MESSAGE_TO_REPORT);
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
    private static List<String> delayedMsgList = new ArrayList(16);
    private static boolean isClearDelayedMsgListNeeded = false;
    private static long lastDumpTime = 0;
    private static MessageRecord lastMessageRecord = null;
    private static volatile Thread sMainThread = null;
    private AppEyeUiProbe mAppEyeUiProbe;
    private Looper mMainLooper;
    private MessageQueue mMainQueue;

    private BlockMonitor() {
    }

    public static synchronized BlockMonitor getInstance() {
        BlockMonitor blockMonitor2;
        synchronized (BlockMonitor.class) {
            blockMonitor2 = blockMonitor;
        }
        return blockMonitor2;
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
        if (isInMainThread() && lastMessageRecord != null) {
            long j = lastMessageRecord.mProcessTime;
            if (SystemClock.uptimeMillis() - eventTime >= 2000 && lastMessageRecord.mProcessTime > MIN_PROCESS_TIME) {
                Log.w(TAG, "Message " + lastMessageRecord.mMsg + " took " + lastMessageRecord.mProcessTime + " ms, before dispatch event, event seq = " + seq);
                lastMessageRecord = null;
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
            msgMap = new HashMap<>(16);
            for (Message msg = MessageQueueEx.getMessage(messageQueue); msg != null; msg = MessageEx.getNext(msg)) {
                String key = dumpMessage(msg);
                MessageInfo info = msgMap.get(key);
                if (info != null) {
                    info.setCount(info.getCount() + 1);
                } else {
                    msgMap.put(key, new MessageInfo(key, 1));
                }
            }
        }
        List<MessageInfo> msgList = new ArrayList<>(msgMap.values());
        Collections.sort(msgList, new Comparator<MessageInfo>() {
            /* class android.os.BlockMonitor.AnonymousClass1 */

            public int compare(MessageInfo info1, MessageInfo info2) {
                if (info1.getCount() < info2.getCount()) {
                    return 1;
                }
                if (info1.getCount() > info2.getCount()) {
                    return -1;
                }
                return 0;
            }
        });
        StringBuilder sb = new StringBuilder("TOP messages waiting to deal with are:  ");
        int i = 0;
        while (i < msgList.size() && i < TOP_MESSAGE_TO_REPORT) {
            MessageInfo info2 = msgList.get(i);
            sb.append(info2.getKey());
            sb.append(", Count: ");
            sb.append(info2.getCount());
            sb.append(" ");
            i++;
        }
        return sb.toString();
    }

    public void checkMessageDelayTime(long dispatchTime, Message msg, MessageQueue queue) {
        if (msg == null) {
            return;
        }
        if (queue != null) {
            long msgDelayed = dispatchTime - MessageEx.getExpectedDispatchTime(msg);
            long currentTime = SystemClock.uptimeMillis();
            long processTime = currentTime - dispatchTime;
            if (processTime > MIN_PROCESS_TIME) {
                lastMessageRecord = new MessageRecord(dumpMessage(msg), processTime);
            }
            if (processTime > 1000 && processTime <= MAX_THREAD_BLOCK_MONITOR_TIME) {
                Jlog.d(375, getPackageName(), (int) processTime, dumpMessage(msg));
            }
            if (processTime >= 4000) {
                uploadLongTimeMessage(processTime, msg);
                clearDelayedMsgList();
                lastDumpTime = currentTime;
                return;
            }
            if (msgDelayed >= 2000 || processTime > 1000) {
                countDelayedMsg(msgDelayed, processTime, msg);
            } else if (isClearDelayedMsgListNeeded) {
                clearDelayedMsgList();
            }
            if (msgDelayed >= 4000 && SystemClock.uptimeMillis() - lastDumpTime > MIN_DUMP_INTERVAL) {
                dumpDelayedMsgList(queue);
                clearDelayedMsgList();
                lastDumpTime = currentTime;
            }
        }
    }

    private static void dumpDelayedMsgList(MessageQueue queue) {
        Log.w(TAG, "Delayed messages are: ");
        for (String msgInfo : delayedMsgList) {
            Log.w(TAG, msgInfo);
        }
        Log.w(TAG, dumpTopMessage(queue));
    }

    private static void countDelayedMsg(long msgDelayed, long processTime, Message msg) {
        int delayedMsgListSize = delayedMsgList.size();
        if (((long) delayedMsgListSize) >= MAX_DELAYED_MSG_LIST_SIZE) {
            List<String> delayedList = new ArrayList<>(16);
            for (int i = 16; i < delayedMsgListSize; i++) {
                delayedList.add(delayedMsgList.get(i));
            }
            delayedMsgList = delayedList;
        }
        List<String> delayedList2 = delayedMsgList;
        delayedList2.add(dumpMessage(msg) + ", delayed: " + msgDelayed + "ms, finish: " + processTime + "ms");
        isClearDelayedMsgListNeeded = true;
    }

    private static void clearDelayedMsgList() {
        delayedMsgList.clear();
        isClearDelayedMsgListNeeded = false;
    }

    private static String dumpMessage(Message message) {
        StringBuilder msgBuffer = new StringBuilder();
        msgBuffer.append("{");
        if (MessageEx.getTarget(message) != null) {
            if (MessageEx.getCallback(message) != null) {
                String callbackClass = MessageEx.getCallback(message).getClass().getName();
                msgBuffer.append(" callback=");
                msgBuffer.append(callbackClass);
                if (MAIN_THREAD_MESSAGE_CALLBACK.equals(callbackClass)) {
                    msgBuffer.append(MessageEx.getCallback(message).toString());
                }
            } else {
                msgBuffer.append(" what=");
                msgBuffer.append(message.what);
            }
            if (message.arg1 != 0) {
                msgBuffer.append(" arg1=");
                msgBuffer.append(message.arg1);
            }
            if (message.arg2 != 0) {
                msgBuffer.append(" arg2=");
                msgBuffer.append(message.arg2);
            }
            if (message.obj != null) {
                msgBuffer.append(" obj=");
                try {
                    msgBuffer.append(message.obj);
                } catch (ClassCastException e) {
                    Log.e(TAG, "Couldn't convert obj to String");
                } catch (Exception e2) {
                    Log.e(TAG, "unexpected exception happen");
                }
            }
            msgBuffer.append(" target=");
            msgBuffer.append(MessageEx.getTarget(message).getClass().getName());
        } else {
            msgBuffer.append(" barrier=");
            msgBuffer.append(message.arg1);
        }
        msgBuffer.append(" }");
        return msgBuffer.toString();
    }

    private static String getPackageName() {
        String packageName = ActivityThreadEx.currentPackageName();
        if (packageName == null) {
            return "system_server";
        }
        return packageName;
    }

    private static long getVersionCode() {
        if (ActivityThreadEx.currentApplication() != null) {
            return (long) ApplicationInfoEx.getVersionCode(ActivityThreadEx.currentApplication().getApplicationContext().getApplicationInfo());
        }
        return 0;
    }

    public void notifyInputEvent(InputEvent event) {
        AwareUiRenderParallelManager.getInstance().notifyInputEvent(event);
    }

    /* access modifiers changed from: private */
    public static class BlockRadar {
        private BlockRadar() {
        }

        public static void upload(int bugType, int sceneDef, String msg) {
            Log.w(BlockMonitor.TAG, msg);
            if (LogEx.getLogHWInfo()) {
                long traceTagActivityManager = TraceEx.getTraceTagActivityManager();
                TraceEx.traceBegin(traceTagActivityManager, "BlockMonitor:" + msg);
                TraceEx.traceEnd(TraceEx.getTraceTagActivityManager());
            }
        }
    }

    public void initialize() {
        this.mMainLooper = Looper.getMainLooper();
        Looper looper = this.mMainLooper;
        if (looper != null) {
            this.mMainQueue = looper.getQueue();
            setObserver(this);
            if (IS_BETA_VERSION) {
                setProxyTransactListener(this);
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
            MessageQueueEx.dump(messageQueue, sbp, BuildConfig.FLAVOR, (Handler) null);
        }
        return sb.toString();
    }

    private void initAppEyeUiProbe() {
        this.mAppEyeUiProbe = AppEyeUiProbe.get();
        AppEyeUiProbe appEyeUiProbe = this.mAppEyeUiProbe;
        if (appEyeUiProbe != null) {
            appEyeUiProbe.start(null);
            this.mAppEyeUiProbe.setBlockMonitor(this);
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
        private int count;
        private String key;

        MessageInfo(String key2, int count2) {
        }

        public String getKey() {
            return this.key;
        }

        public void setKey(String key2) {
            this.key = key2;
        }

        public int getCount() {
            return this.count;
        }

        public void setCount(int count2) {
            this.count = count2;
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
