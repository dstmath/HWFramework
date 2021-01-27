package com.android.server.rms.iaware.appmng;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.appmng.AwareWakeUpManager;
import com.huawei.android.internal.os.SomeArgsEx;
import com.huawei.android.os.HandlerEx;
import com.huawei.android.os.UserHandleEx;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AlarmManagerDumpRadar {
    public static final int EVENT_CONTROLED = 2;
    public static final int EVENT_OVERLOAD = 1;
    public static final int EVENT_WAKEUP = 0;
    private static final boolean IS_BETA_USER = (AwareConstant.CURRENT_USER_TYPE == 3);
    private static final Object LOCK = new Object();
    private static final int MSG_ALARM_EVENT = 1;
    private static final String TAG = "AlarmManagerDumpRadar";
    private static final int USER_OTHER = 1;
    private static AlarmManagerDumpRadar sAlarmManagerDumpRadar;
    private HashMap<Integer, HashMap<String, PackageInfo>> mAlarmWakeupInfo = new HashMap<>();
    private long mCleanupTime = System.currentTimeMillis();
    private Handler mHandler;
    private AtomicInteger mSystemWakeupCount = new AtomicInteger(0);
    private AtomicInteger mSystemWakeupOverloadCount = new AtomicInteger(0);

    public static AlarmManagerDumpRadar getInstance() {
        AlarmManagerDumpRadar alarmManagerDumpRadar;
        synchronized (LOCK) {
            if (sAlarmManagerDumpRadar == null) {
                sAlarmManagerDumpRadar = new AlarmManagerDumpRadar();
            }
            alarmManagerDumpRadar = sAlarmManagerDumpRadar;
        }
        return alarmManagerDumpRadar;
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.rms.iaware.appmng.AlarmManagerDumpRadar$AlarmRadarHandler, android.os.Handler] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void setHandler(Handler handler) {
        if (handler != null) {
            this.mHandler = new AlarmRadarHandler(handler.getLooper());
        }
    }

    /* access modifiers changed from: private */
    public final class AlarmRadarHandler extends HandlerEx {
        protected AlarmRadarHandler(Looper looper) {
            super(looper, (Handler.Callback) null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1 && (msg.obj instanceof SomeArgsEx)) {
                SomeArgsEx args = (SomeArgsEx) msg.obj;
                if ((args.arg1() instanceof String) && (args.arg2() instanceof String) && (args.arg3() instanceof AwareWakeUpManager.ControlType)) {
                    String tag = (String) args.arg2();
                    AwareWakeUpManager.ControlType policy = (AwareWakeUpManager.ControlType) args.arg3();
                    int type = args.getArgi1();
                    int uid = args.getArgi2();
                    AlarmManagerDumpRadar.this.handleAlarmEvent(type, uid, (String) args.arg1(), tag, policy);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class PackageInfo {
        private int mOverloadCount = 0;
        private String mPkg;
        private HashMap<String, TagInfo> mTagMap = new HashMap<>();
        private int mWakeupCount = 0;

        static /* synthetic */ int access$408(PackageInfo x0) {
            int i = x0.mWakeupCount;
            x0.mWakeupCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$508(PackageInfo x0) {
            int i = x0.mOverloadCount;
            x0.mOverloadCount = i + 1;
            return i;
        }

        public PackageInfo(String pkg) {
            this.mPkg = pkg;
        }
    }

    /* access modifiers changed from: private */
    public static class TagInfo {
        private int mDecideOverloadCount = 0;
        private int mExtendCount = 0;
        private int mMuteCount = 0;
        private int mOverloadCount = 0;
        private int mPerceptibleCount = 0;
        private String mTag;
        private int mTopNumCount = 0;
        private int mUnknownCount = 0;
        private int mWakeupCount = 0;
        private int mWhiteListCount = 0;

        static /* synthetic */ int access$1008(TagInfo x0) {
            int i = x0.mPerceptibleCount;
            x0.mPerceptibleCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$1108(TagInfo x0) {
            int i = x0.mUnknownCount;
            x0.mUnknownCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$1208(TagInfo x0) {
            int i = x0.mTopNumCount;
            x0.mTopNumCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$1308(TagInfo x0) {
            int i = x0.mDecideOverloadCount;
            x0.mDecideOverloadCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$1408(TagInfo x0) {
            int i = x0.mWhiteListCount;
            x0.mWhiteListCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$608(TagInfo x0) {
            int i = x0.mWakeupCount;
            x0.mWakeupCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$708(TagInfo x0) {
            int i = x0.mOverloadCount;
            x0.mOverloadCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$808(TagInfo x0) {
            int i = x0.mExtendCount;
            x0.mExtendCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$908(TagInfo x0) {
            int i = x0.mMuteCount;
            x0.mMuteCount = i + 1;
            return i;
        }

        public TagInfo(String tag) {
            this.mTag = tag;
        }
    }

    private void buidBigDataBegin(StringBuilder data) {
        String separator = System.lineSeparator();
        data.append(separator);
        data.append("[iAwareAlarmManager_Start]");
        data.append(separator);
        data.append("startTime: ");
        data.append(String.valueOf(this.mCleanupTime));
        data.append(separator);
    }

    public String saveBigData(boolean clear) {
        StringBuilder data = new StringBuilder();
        buidBigDataBegin(data);
        JSONObject bigData = new JSONObject();
        long currentTime = System.currentTimeMillis();
        synchronized (LOCK) {
            try {
                saveBigDataInternal(data, bigData);
            } catch (JSONException e) {
                AwareLog.e(TAG, "saveBigdata failed! catch JSONException e :" + e.toString());
            }
            clearBigDataIfInDebugMode(clear, currentTime);
        }
        buildBigDataEnd(data, bigData, currentTime);
        return data.toString();
    }

    private void saveBigDataInternal(StringBuilder data, JSONObject bigData) throws JSONException {
        AlarmManagerDumpRadar alarmManagerDumpRadar = this;
        JSONObject title = new JSONObject();
        JSONArray sysTitle = new JSONArray();
        alarmManagerDumpRadar.generateSysTitle(sysTitle);
        title.put("sys", sysTitle);
        JSONArray pkgTitle = new JSONArray();
        alarmManagerDumpRadar.generatePkgTitle(pkgTitle);
        title.put("pkg", pkgTitle);
        JSONArray tagTitle = new JSONArray();
        alarmManagerDumpRadar.generateTagTitle(tagTitle);
        title.put("tag", tagTitle);
        bigData.put("title", title);
        JSONArray systemData = new JSONArray();
        systemData.put(alarmManagerDumpRadar.mSystemWakeupCount);
        systemData.put(alarmManagerDumpRadar.mSystemWakeupOverloadCount);
        bigData.put("sys", systemData);
        JSONArray alarmData = new JSONArray();
        for (Map.Entry<Integer, HashMap<String, PackageInfo>> userEntry : alarmManagerDumpRadar.mAlarmWakeupInfo.entrySet()) {
            for (Map.Entry<String, PackageInfo> pkgEntry : userEntry.getValue().entrySet()) {
                JSONObject alarmDataItem = new JSONObject();
                JSONArray pkgData = new JSONArray();
                PackageInfo packageInfo = pkgEntry.getValue();
                alarmManagerDumpRadar.generatePkgData(pkgData, userEntry, packageInfo);
                alarmDataItem.put(packageInfo.mPkg, pkgData);
                for (Map.Entry<String, TagInfo> tagEntry : packageInfo.mTagMap.entrySet()) {
                    TagInfo tagInfo = tagEntry.getValue();
                    JSONArray tagData = new JSONArray();
                    alarmManagerDumpRadar.generateTagData(tagData, tagInfo);
                    alarmDataItem.put(tagInfo.mTag, tagData);
                    alarmManagerDumpRadar = this;
                    sysTitle = sysTitle;
                    title = title;
                }
                alarmData.put(alarmDataItem);
                alarmManagerDumpRadar = this;
            }
            alarmManagerDumpRadar = this;
        }
        bigData.put("alarm", alarmData);
    }

    private void buildBigDataEnd(StringBuilder data, JSONObject bigData, long currentTime) {
        String separator = System.lineSeparator();
        data.append(bigData.toString());
        data.append(separator);
        data.append("endTime: ");
        data.append(String.valueOf(currentTime));
        data.append(separator);
        data.append("[iAwareAlarmManager_End]");
    }

    private void clearBigDataIfInDebugMode(boolean clear, long currentTime) {
        if (!AwareWakeUpManager.getInstance().isDebugMode() && clear) {
            this.mAlarmWakeupInfo = new HashMap<>();
            this.mSystemWakeupOverloadCount.set(0);
            this.mSystemWakeupCount.set(0);
            this.mCleanupTime = currentTime;
        }
    }

    private void generateSysTitle(JSONArray sysTitle) {
        sysTitle.put("wakeup_count");
        sysTitle.put("overload_count");
    }

    private void generatePkgData(JSONArray pkgData, Map.Entry<Integer, HashMap<String, PackageInfo>> userEntry, PackageInfo packageInfo) {
        pkgData.put(userEntry.getKey());
        pkgData.put(packageInfo.mWakeupCount);
        pkgData.put(packageInfo.mOverloadCount);
    }

    private void generateTagData(JSONArray tagData, TagInfo tagInfo) {
        tagData.put(tagInfo.mWakeupCount);
        tagData.put(tagInfo.mOverloadCount);
        tagData.put(tagInfo.mExtendCount);
        tagData.put(tagInfo.mMuteCount);
        tagData.put(tagInfo.mPerceptibleCount);
        tagData.put(tagInfo.mUnknownCount);
        tagData.put(tagInfo.mTopNumCount);
        tagData.put(tagInfo.mDecideOverloadCount);
        tagData.put(tagInfo.mWhiteListCount);
    }

    private void generateTagTitle(JSONArray tagTitle) {
        tagTitle.put("wakeup_count");
        tagTitle.put("overload_count");
        tagTitle.put("extend_count");
        tagTitle.put("mute_count");
        tagTitle.put("perceptible");
        tagTitle.put("unknown");
        tagTitle.put("topn");
        tagTitle.put("decide_overload");
        tagTitle.put("white_list");
    }

    private void generatePkgTitle(JSONArray pkgTitle) {
        pkgTitle.put("userid");
        pkgTitle.put("wakeup_count");
        pkgTitle.put("overload_count");
    }

    public void reportSystemEvent(int type) {
        if (IS_BETA_USER && !AwareWakeUpManager.getInstance().isScreenOn() && this.mHandler != null) {
            if (type == 0) {
                this.mSystemWakeupCount.incrementAndGet();
            } else if (type == 1) {
                this.mSystemWakeupOverloadCount.incrementAndGet();
            }
        }
    }

    public void reportAlarmEvent(int type, int uid, String pkg, String tag, AwareWakeUpManager.ControlType policy) {
        Handler handler;
        if (IS_BETA_USER && !AwareWakeUpManager.getInstance().isScreenOn() && (handler = this.mHandler) != null) {
            Message msg = handler.obtainMessage();
            msg.what = 1;
            SomeArgsEx args = SomeArgsEx.obtain();
            args.setArg1(pkg);
            args.setArg2(tag);
            args.setArg3(policy);
            args.setArgi1(type);
            args.setArgi2(uid);
            msg.obj = args;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAlarmEvent(int type, int uid, String pkg, String tag, AwareWakeUpManager.ControlType policy) {
        int userId = UserHandleEx.getUserId(uid);
        if (userId != 0) {
            userId = 1;
        }
        synchronized (LOCK) {
            HashMap<String, PackageInfo> packageInfos = this.mAlarmWakeupInfo.get(Integer.valueOf(userId));
            if (packageInfos == null) {
                packageInfos = new HashMap<>();
            }
            PackageInfo packageInfo = packageInfos.get(pkg);
            if (packageInfo == null) {
                packageInfo = new PackageInfo(pkg);
            }
            TagInfo tagInfo = null;
            if (tag != null && (tagInfo = (TagInfo) packageInfo.mTagMap.get(tag)) == null) {
                tagInfo = new TagInfo(tag);
            }
            updateData(type, packageInfo, tagInfo, policy);
            if (tagInfo != null) {
                packageInfo.mTagMap.put(tag, tagInfo);
            }
            packageInfos.put(pkg, packageInfo);
            this.mAlarmWakeupInfo.put(Integer.valueOf(userId), packageInfos);
        }
    }

    private void updateData(int type, PackageInfo packageInfo, TagInfo tagInfo, AwareWakeUpManager.ControlType policy) {
        if (type == 0) {
            PackageInfo.access$408(packageInfo);
            if (tagInfo != null) {
                TagInfo.access$608(tagInfo);
            }
        } else if (type != 1) {
            if (type == 2 && tagInfo != null) {
                switch (policy) {
                    case PERCEPTIBLE:
                        TagInfo.access$1008(tagInfo);
                        return;
                    case UNKNOWN:
                        TagInfo.access$1108(tagInfo);
                        return;
                    case EXTEND:
                        TagInfo.access$808(tagInfo);
                        return;
                    case EXTEND_TOPN:
                        TagInfo.access$1208(tagInfo);
                        return;
                    case EXTEND_AND_MUTE:
                        TagInfo.access$908(tagInfo);
                        return;
                    case DECIDE_OVERLOAD:
                        TagInfo.access$1308(tagInfo);
                        return;
                    case IMPORTANT:
                        TagInfo.access$1408(tagInfo);
                        return;
                    default:
                        return;
                }
            }
        } else if (tagInfo != null) {
            TagInfo.access$708(tagInfo);
        } else {
            PackageInfo.access$508(packageInfo);
        }
    }
}
