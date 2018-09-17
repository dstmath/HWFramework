package android.os;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.ProxyInfo;
import android.util.Jlog;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockMonitor {
    private static final int BUG_TYPE = 104;
    private static final boolean DEBUG_MAIN_THREAD_BLOCK;
    private static final long HALF_LIMIT_FOR_COUNTING_DELAYED_MSG = 1000;
    private static final long LIMIT_FOR_COUNTING_DELAYED_MSG = 2000;
    private static final long LIMIT_FOR_DUMPING_DELAYED_MSG = 4000;
    private static final long MAIN_THREAD_BLOCK_TIMEOUT = 4000;
    private static final String MAIN_THREAD_MESSAGE_CALLBACK = "android.app.LoadedApk$ReceiverDispatcher$Args";
    private static final long MAX_DELAYED_MSG_LIST_SIZE = 32;
    private static final long MIN_DUMP_INTERVAL = 10000;
    private static final int SCENE_TYPE_BINDER_BLOCK = 2903;
    private static final int SCENE_TYPE_INPUT_BLOCK = 2904;
    private static final int SCENE_TYPE_MESSAGE_BLOCK = 2901;
    private static final int SCENE_TYPE_MESSAGE_COUNT_BLOCK = 2902;
    private static final String TAG = "BlockMonitor";
    private static final int TOP_MESSAGE_TO_REPORT = 3;
    private static boolean mClearDelayedMsgListNeeded = false;
    private static List<String> mDelayedMsgList = new ArrayList();
    private static long mLastDumpTime = 0;
    private static volatile Thread sMainThread = null;

    private static class BlockRadar {
        private static final long AUTO_UPLOAD_MIN_INTERVAL_TIME = 43200000;
        private static final String CATEGORY_PREFIX = "app-";
        private static final int LEVEL_B = 66;
        private static final int LOG_MASK = 31;
        private static long sLastAutoUploadTime = 0;

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

    private static class MessageInfo {
        public int mCount;
        public String mKey;

        public MessageInfo(String key, int count) {
            this.mKey = key;
            this.mCount = count;
        }
    }

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        DEBUG_MAIN_THREAD_BLOCK = z;
    }

    public static boolean isNeedMonitor() {
        return isDebugMainThreadBlock() ? isInMainThread() : false;
    }

    private static boolean isDebugMainThreadBlock() {
        return DEBUG_MAIN_THREAD_BLOCK;
    }

    private static boolean isInMainThread() {
        boolean z = true;
        if (sMainThread != null) {
            if (sMainThread != Thread.currentThread()) {
                z = false;
            }
            return z;
        } else if (Process.myPid() != Process.myTid()) {
            return false;
        } else {
            sMainThread = Thread.currentThread();
            return true;
        }
    }

    private static void uploadLongTimeMessage(long processTime, Message message) {
        StringBuilder sb = new StringBuilder();
        sb.append("Package name: ");
        sb.append(getPackageName());
        sb.append("\n");
        sb.append("The Message");
        sb.append(dumpMessage(message));
        sb.append(" took ");
        sb.append(processTime);
        sb.append("ms.");
        Jlog.d(347, getPackageName(), (int) processTime, dumpMessage(message));
        BlockRadar.upload(104, SCENE_TYPE_MESSAGE_BLOCK, sb.toString());
    }

    public static void checkInputTime(long startTime) {
        long processTime = SystemClock.uptimeMillis() - startTime;
        if (processTime >= 4000) {
            StringBuilder sb = new StringBuilder();
            sb.append("Package name: ");
            sb.append(getPackageName());
            sb.append("\n");
            sb.append("The input took ");
            sb.append(processTime);
            sb.append("ms.");
            sb.append("\n");
            Jlog.d(350, getPackageName(), (int) processTime, ProxyInfo.LOCAL_EXCL_LIST);
            BlockRadar.upload(104, SCENE_TYPE_INPUT_BLOCK, sb.toString());
        }
    }

    public static void checkBinderTime(long startTime) {
        long processTime = SystemClock.uptimeMillis() - startTime;
        if (processTime >= 4000) {
            StringBuilder sb = new StringBuilder();
            StringBuilder briefSb = new StringBuilder();
            sb.append("Package name: ");
            sb.append(getPackageName());
            sb.append("\n");
            sb.append("The binder calling took ");
            sb.append(processTime);
            sb.append("ms.");
            sb.append("\n");
            StackTraceElement[] elements = new Throwable().getStackTrace();
            for (StackTraceElement element : elements) {
                briefSb.append(element.getClassName()).append(".").append(element.getMethodName()).append(";");
                sb.append(element);
                sb.append("\n");
            }
            Jlog.d(70, getPackageName(), (int) processTime, briefSb.toString());
            BlockRadar.upload(104, 2903, sb.toString());
        }
    }

    /* JADX WARNING: Missing block: B:17:0x002f, code:
            r4 = new java.util.ArrayList(r6.values());
            java.util.Collections.sort(r4, new android.os.BlockMonitor.AnonymousClass1());
            r7 = new java.lang.StringBuilder("TOP messages waiting to deal with are: \n");
            r0 = 0;
     */
    /* JADX WARNING: Missing block: B:19:0x004d, code:
            if (r0 >= r4.size()) goto L_0x0074;
     */
    /* JADX WARNING: Missing block: B:21:0x0050, code:
            if (r0 >= 3) goto L_0x0074;
     */
    /* JADX WARNING: Missing block: B:22:0x0052, code:
            r1 = (android.os.BlockMonitor.MessageInfo) r4.get(r0);
            r7.append(r1.mKey).append(", Count: ").append(r1.mCount).append("\n");
            r0 = r0 + 1;
     */
    /* JADX WARNING: Missing block: B:24:0x0078, code:
            return r7.toString();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String dumpTopMessage(MessageQueue messageQueue) {
        Throwable th;
        synchronized (messageQueue) {
            try {
                Map<String, MessageInfo> msgMap = new HashMap();
                try {
                    for (Message msg = messageQueue.mMessages; msg != null; msg = msg.next) {
                        String key = dumpMessage(msg);
                        MessageInfo info = (MessageInfo) msgMap.get(key);
                        if (info != null) {
                            info.mCount++;
                        } else {
                            msgMap.put(key, new MessageInfo(key, 1));
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    Map<String, MessageInfo> map = msgMap;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public static void checkMessageDelayTime(long dispatchTime, Message msg, MessageQueue queue) {
        long msgDelayed = dispatchTime - msg.expectedDispatchTime;
        long currentTime = SystemClock.uptimeMillis();
        long processTime = currentTime - dispatchTime;
        if (processTime >= 4000) {
            uploadLongTimeMessage(processTime, msg);
            clearDelayedMsgList();
            mLastDumpTime = currentTime;
            return;
        }
        if (msgDelayed >= LIMIT_FOR_COUNTING_DELAYED_MSG || processTime > HALF_LIMIT_FOR_COUNTING_DELAYED_MSG) {
            countDelayedMsg(msgDelayed, processTime, msg);
        } else if (mClearDelayedMsgListNeeded) {
            clearDelayedMsgList();
        }
        if (msgDelayed >= 4000 && SystemClock.uptimeMillis() - mLastDumpTime > 10000) {
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
        if (((long) delayedMsgListSize) >= 32) {
            List<String> list = new ArrayList();
            for (int i = 16; i < delayedMsgListSize; i++) {
                list.add((String) mDelayedMsgList.get(i));
            }
            mDelayedMsgList = list;
        }
        mDelayedMsgList.add(dumpMessage(msg) + ", delayed: " + msgDelayed + "ms, finish: " + processTime + "ms");
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
                try {
                    String callbackClass = message.callback.getClass().getName();
                    b.append(" callback=");
                    b.append(callbackClass);
                    if (MAIN_THREAD_MESSAGE_CALLBACK.equals(callbackClass)) {
                        b.append(message.callback.toString());
                    }
                } catch (Exception e) {
                    Log.i(TAG, "Could not get Class Name", e);
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
            try {
                if (message.obj != null) {
                    b.append(" obj=");
                    b.append(message.obj);
                }
            } catch (Exception e2) {
                Log.i(TAG, "Could not get message obj", e2);
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

    private static int getVersionCode() {
        if (ActivityThread.currentApplication() != null) {
            return ActivityThread.currentApplication().getApplicationContext().getApplicationInfo().versionCode;
        }
        return 0;
    }

    private static String getVersionName() {
        try {
            if (ActivityThread.currentApplication() != null) {
                Context context = ActivityThread.currentApplication().getApplicationContext();
                if (context != null) {
                    PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    if (!(info == null || info.versionName == null)) {
                        return info.versionName;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get package info", e);
        }
        return getVersionCode() + ProxyInfo.LOCAL_EXCL_LIST;
    }
}
