package android.util;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.os.SystemProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HiviewLooperCheck implements Printer {
    private static final String BEGIN_REGEX = ">>>>> Dispatching to Handler (.{1,}) \\{.{1,}\\} null: \\d{1,}";
    private static final boolean DEBUG = Log.isLoggable("hiview", 3);
    private static final String END_REGEX = "<<<<< Finished to Handler (.{1,}) \\{.{1,}\\} null";
    private static final boolean IS_ENABLE;
    private static final String TAG = "HiviewLooperCheck";
    private static final String TARGET_REGEX = "\\((.*?)\\)";
    private static final int VERSION_DOMESTIC_BETA = 3;
    private static final int VERSION_DOMESTIC_COMMERCIAL = 1;
    private static final int VERSION_OVERSEAS_BETA = 5;
    private static final String WHAT_REGEX = "[:](.*?)$";
    private static Map<String, ThreadMsgs> sThreadLoopers = null;
    private String mCurMsgToken;
    private final String mKey;
    private long mStartTime = 0;
    private ThreadMsgs mThreadMsgs = null;

    static {
        boolean z = true;
        if (!(SystemProperties.getInt("ro.logsystem.usertype", 1) == 3 || SystemProperties.getInt("ro.logsystem.usertype", 1) == 5)) {
            z = false;
        }
        IS_ENABLE = z;
    }

    public static final class LoopMsg {
        private long cnt = 0;
        private long max = 0;
        private String token;
        private long total = 0;

        public LoopMsg(String msg) {
            this.token = msg;
        }

        public void add(long time) {
            if (this.max < time) {
                this.max = time;
            }
            this.cnt++;
            this.total += time;
        }

        public JSONObject dumpJson() throws JSONException {
            JSONObject jsonMsg = new JSONObject();
            jsonMsg.put("msg", this.token);
            jsonMsg.put("cnt", this.cnt);
            jsonMsg.put("total", this.total);
            jsonMsg.put("max", this.max);
            return jsonMsg;
        }

        public String dumpString() {
            try {
                return dumpJson().toString(4);
            } catch (JSONException e) {
                Log.e(HiviewLooperCheck.TAG, "dumpString, JSONException.");
                return null;
            }
        }
    }

    public static final class ThreadMsgs {
        private Map<String, LoopMsg> looperMsgs = new HashMap();
        private String mThreadName;

        public ThreadMsgs(String threadName) {
            this.mThreadName = threadName;
        }

        public LoopMsg get(String token) {
            return this.looperMsgs.get(token);
        }

        public void put(String token, LoopMsg msg) {
            this.looperMsgs.put(token, msg);
        }

        public JSONObject dumpJson() throws JSONException {
            JSONObject jsonThread = new JSONObject();
            JSONArray jsonMsgArray = new JSONArray();
            jsonThread.put("thread", this.mThreadName);
            for (Map.Entry<String, LoopMsg> entry : this.looperMsgs.entrySet()) {
                jsonMsgArray.put(entry.getValue().dumpJson());
            }
            jsonThread.put("msg", jsonMsgArray);
            return jsonThread;
        }

        public String dumpString() {
            try {
                return dumpJson().toString(4);
            } catch (JSONException e) {
                Log.e(HiviewLooperCheck.TAG, "dumpString, JSONException.");
                return null;
            }
        }
    }

    public static void check(HandlerThread thread) {
        if (thread != null && IS_ENABLE && thread.getLooper() != null) {
            thread.getLooper().setMessageLogging(new HiviewLooperCheck(thread.getName(), thread.hashCode()));
        }
    }

    public static void check(Looper looper, String threadName) {
        if (looper != null && IS_ENABLE) {
            looper.setMessageLogging(new HiviewLooperCheck(threadName, looper.hashCode()));
        }
    }

    private HiviewLooperCheck(String threadName, int hashCode) {
        ThreadMsgs threadMsgs = new ThreadMsgs(threadName);
        synchronized (HiviewLooperCheck.class) {
            if (sThreadLoopers == null) {
                sThreadLoopers = new HashMap();
            }
            this.mKey = threadName + hashCode;
            sThreadLoopers.put(this.mKey, threadMsgs);
            this.mThreadMsgs = threadMsgs;
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        synchronized (HiviewLooperCheck.class) {
            if (sThreadLoopers != null) {
                sThreadLoopers.remove(this.mKey);
            }
        }
        super.finalize();
    }

    public static JSONArray dumpJson() throws JSONException {
        JSONArray jsonThreadArray = new JSONArray();
        synchronized (HiviewLooperCheck.class) {
            if (sThreadLoopers != null) {
                for (Map.Entry<String, ThreadMsgs> entry : sThreadLoopers.entrySet()) {
                    jsonThreadArray.put(entry.getValue().dumpJson());
                }
            }
        }
        return jsonThreadArray;
    }

    public static String dumpString() {
        try {
            return dumpJson().toString(4);
        } catch (JSONException e) {
            Log.e(TAG, "dumpString , JSONException.");
            return null;
        }
    }

    @Override // android.util.Printer
    public void println(String str) {
        if (str != null && !str.isEmpty()) {
            try {
                long now = SystemClock.uptimeMillis();
                if (str.matches(BEGIN_REGEX)) {
                    String target = getSubUtil(str, TARGET_REGEX);
                    int what = Integer.parseInt(getSubUtil(str, WHAT_REGEX));
                    this.mCurMsgToken = target + ":" + what;
                    this.mStartTime = now;
                } else if (str.matches(END_REGEX)) {
                    if (this.mCurMsgToken != null) {
                        LoopMsg curMsg = this.mThreadMsgs.get(this.mCurMsgToken);
                        if (curMsg == null) {
                            curMsg = new LoopMsg(this.mCurMsgToken);
                            this.mThreadMsgs.put(this.mCurMsgToken, curMsg);
                        }
                        if (DEBUG) {
                            Log.d(TAG, this.mCurMsgToken + ":" + (now - this.mStartTime));
                        }
                        curMsg.add(now - this.mStartTime);
                        this.mCurMsgToken = null;
                    }
                } else if (DEBUG) {
                    Log.d(TAG, str);
                }
            } catch (PatternSyntaxException e) {
                Log.e(TAG, "PatternSyntaxException.");
            } catch (RuntimeException e2) {
                Log.e(TAG, "RuntimeException.");
            }
        }
    }

    public static String getSubUtil(String target, String rgex) {
        StringBuilder stringBuilder = new StringBuilder();
        Matcher matcher = Pattern.compile(rgex).matcher(target);
        while (matcher.find()) {
            stringBuilder.append(matcher.group(1));
        }
        String result = stringBuilder.toString().trim();
        if (DEBUG) {
            Log.i(TAG, "getSubUtil() , result is " + result);
        }
        return result;
    }
}
