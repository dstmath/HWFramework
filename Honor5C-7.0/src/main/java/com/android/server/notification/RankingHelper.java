package com.android.server.notification;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService.Ranking;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.server.notification.NotificationManagerService.DumpFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class RankingHelper implements RankingConfig {
    private static final String ATT_IMPORTANCE = "importance";
    private static final String ATT_NAME = "name";
    private static final String ATT_PRIORITY = "priority";
    private static final String ATT_TOPIC_ID = "id";
    private static final String ATT_TOPIC_LABEL = "label";
    private static final String ATT_UID = "uid";
    private static final String ATT_VERSION = "version";
    private static final String ATT_VISIBILITY = "visibility";
    private static final int DEFAULT_IMPORTANCE = -1000;
    private static final int DEFAULT_PRIORITY = 0;
    private static final int DEFAULT_VISIBILITY = -1000;
    private static final String TAG = "RankingHelper";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_RANKING = "ranking";
    private static final int XML_VERSION = 1;
    private final Context mContext;
    private final GlobalSortKeyComparator mFinalComparator;
    private final NotificationComparator mPreliminaryComparator;
    private final ArrayMap<String, NotificationRecord> mProxyByGroupTmp;
    private final RankingHandler mRankingHandler;
    private final ArrayMap<String, Record> mRecords;
    private final ArrayMap<String, Record> mRestoredWithoutUids;
    private final NotificationSignalExtractor[] mSignalExtractors;

    private static class Record {
        static int UNKNOWN_UID;
        int importance;
        String pkg;
        int priority;
        int uid;
        int visibility;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.notification.RankingHelper.Record.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.notification.RankingHelper.Record.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.RankingHelper.Record.<clinit>():void");
        }

        private Record() {
            this.uid = UNKNOWN_UID;
            this.importance = RankingHelper.DEFAULT_VISIBILITY;
            this.priority = RankingHelper.DEFAULT_PRIORITY;
            this.visibility = RankingHelper.DEFAULT_VISIBILITY;
        }
    }

    public RankingHelper(Context context, RankingHandler rankingHandler, NotificationUsageStats usageStats, String[] extractorNames) {
        this.mPreliminaryComparator = new NotificationComparator();
        this.mFinalComparator = new GlobalSortKeyComparator();
        this.mRecords = new ArrayMap();
        this.mProxyByGroupTmp = new ArrayMap();
        this.mRestoredWithoutUids = new ArrayMap();
        this.mContext = context;
        this.mRankingHandler = rankingHandler;
        int N = extractorNames.length;
        this.mSignalExtractors = new NotificationSignalExtractor[N];
        for (int i = DEFAULT_PRIORITY; i < N; i += XML_VERSION) {
            try {
                NotificationSignalExtractor extractor = (NotificationSignalExtractor) this.mContext.getClassLoader().loadClass(extractorNames[i]).newInstance();
                extractor.initialize(this.mContext, usageStats);
                extractor.setConfig(this);
                this.mSignalExtractors[i] = extractor;
            } catch (ClassNotFoundException e) {
                Slog.w(TAG, "Couldn't find extractor " + extractorNames[i] + ".", e);
            } catch (InstantiationException e2) {
                Slog.w(TAG, "Couldn't instantiate extractor " + extractorNames[i] + ".", e2);
            } catch (IllegalAccessException e3) {
                Slog.w(TAG, "Problem accessing extractor " + extractorNames[i] + ".", e3);
            }
        }
    }

    public <T extends NotificationSignalExtractor> T findExtractor(Class<T> extractorClass) {
        int N = this.mSignalExtractors.length;
        for (int i = DEFAULT_PRIORITY; i < N; i += XML_VERSION) {
            NotificationSignalExtractor extractor = this.mSignalExtractors[i];
            if (extractorClass.equals(extractor.getClass())) {
                return extractor;
            }
        }
        return null;
    }

    public void extractSignals(NotificationRecord r) {
        int N = this.mSignalExtractors.length;
        for (int i = DEFAULT_PRIORITY; i < N; i += XML_VERSION) {
            try {
                RankingReconsideration recon = this.mSignalExtractors[i].process(r);
                if (recon != null) {
                    this.mRankingHandler.requestReconsideration(recon);
                }
            } catch (Throwable t) {
                Slog.w(TAG, "NotificationSignalExtractor failed.", t);
            }
        }
    }

    public void readXml(XmlPullParser parser, boolean forRestore) throws XmlPullParserException, IOException {
        PackageManager pm = this.mContext.getPackageManager();
        if (parser.getEventType() == 2) {
            if (TAG_RANKING.equals(parser.getName())) {
                this.mRecords.clear();
                this.mRestoredWithoutUids.clear();
                while (true) {
                    int type = parser.next();
                    if (type == XML_VERSION) {
                        break;
                    }
                    String tag = parser.getName();
                    if (type != 3 || !TAG_RANKING.equals(tag)) {
                        if (type == 2 && TAG_PACKAGE.equals(tag)) {
                            int uid = safeInt(parser, ATT_UID, Record.UNKNOWN_UID);
                            String name = parser.getAttributeValue(null, ATT_NAME);
                            if (!TextUtils.isEmpty(name)) {
                                Record r;
                                if (forRestore) {
                                    try {
                                        uid = pm.getPackageUidAsUser(name, DEFAULT_PRIORITY);
                                    } catch (NameNotFoundException e) {
                                    }
                                }
                                if (uid == Record.UNKNOWN_UID) {
                                    r = (Record) this.mRestoredWithoutUids.get(name);
                                    if (r == null) {
                                        r = new Record();
                                        this.mRestoredWithoutUids.put(name, r);
                                    }
                                } else {
                                    r = getOrCreateRecord(name, uid);
                                }
                                r.importance = safeInt(parser, ATT_IMPORTANCE, DEFAULT_VISIBILITY);
                                r.priority = safeInt(parser, ATT_PRIORITY, DEFAULT_PRIORITY);
                                r.visibility = safeInt(parser, ATT_VISIBILITY, DEFAULT_VISIBILITY);
                            }
                        }
                    } else {
                        return;
                    }
                }
                throw new IllegalStateException("Failed to reach END_DOCUMENT");
            }
        }
    }

    private static String recordKey(String pkg, int uid) {
        return pkg + "|" + uid;
    }

    private Record getOrCreateRecord(String pkg, int uid) {
        String key = recordKey(pkg, uid);
        Record r = (Record) this.mRecords.get(key);
        if (r != null) {
            return r;
        }
        r = new Record();
        r.pkg = pkg;
        r.uid = uid;
        this.mRecords.put(key, r);
        return r;
    }

    public void writeXml(XmlSerializer out, boolean forBackup) throws IOException {
        out.startTag(null, TAG_RANKING);
        out.attribute(null, ATT_VERSION, Integer.toString(XML_VERSION));
        int N = this.mRecords.size();
        for (int i = DEFAULT_PRIORITY; i < N; i += XML_VERSION) {
            Record r = (Record) this.mRecords.valueAt(i);
            if (r != null && (!forBackup || UserHandle.getUserId(r.uid) == 0)) {
                boolean hasNonDefaultSettings = (r.importance == DEFAULT_VISIBILITY && r.priority == 0) ? r.visibility != DEFAULT_VISIBILITY : true;
                if (hasNonDefaultSettings) {
                    out.startTag(null, TAG_PACKAGE);
                    out.attribute(null, ATT_NAME, r.pkg);
                    if (r.importance != DEFAULT_VISIBILITY) {
                        out.attribute(null, ATT_IMPORTANCE, Integer.toString(r.importance));
                    }
                    if (r.priority != 0) {
                        out.attribute(null, ATT_PRIORITY, Integer.toString(r.priority));
                    }
                    if (r.visibility != DEFAULT_VISIBILITY) {
                        out.attribute(null, ATT_VISIBILITY, Integer.toString(r.visibility));
                    }
                    if (!forBackup) {
                        out.attribute(null, ATT_UID, Integer.toString(r.uid));
                    }
                    out.endTag(null, TAG_PACKAGE);
                }
            }
        }
        out.endTag(null, TAG_RANKING);
    }

    private void updateConfig() {
        int N = this.mSignalExtractors.length;
        for (int i = DEFAULT_PRIORITY; i < N; i += XML_VERSION) {
            this.mSignalExtractors[i].setConfig(this);
        }
        this.mRankingHandler.requestSort();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sort(ArrayList<NotificationRecord> notificationList) {
        int i;
        int N = notificationList.size();
        for (i = N - 1; i >= 0; i--) {
            ((NotificationRecord) notificationList.get(i)).setGlobalSortKey(null);
        }
        Collections.sort(notificationList, this.mPreliminaryComparator);
        synchronized (this.mProxyByGroupTmp) {
            for (i = N - 1; i >= 0; i--) {
                NotificationRecord record = (NotificationRecord) notificationList.get(i);
                record.setAuthoritativeRank(i);
                String groupKey = record.getGroupKey();
                if (record.getNotification().isGroupSummary() || !this.mProxyByGroupTmp.containsKey(groupKey)) {
                    this.mProxyByGroupTmp.put(groupKey, record);
                }
            }
            for (i = DEFAULT_PRIORITY; i < N; i += XML_VERSION) {
                String groupSortKeyPortion;
                record = (NotificationRecord) notificationList.get(i);
                NotificationRecord groupProxy = (NotificationRecord) this.mProxyByGroupTmp.get(record.getGroupKey());
                String groupSortKey = record.getNotification().getSortKey();
                if (groupSortKey == null) {
                    groupSortKeyPortion = "nsk";
                } else if (groupSortKey.equals("")) {
                    groupSortKeyPortion = "esk";
                } else {
                    groupSortKeyPortion = "gsk=" + groupSortKey;
                }
                boolean isGroupSummary = record.getNotification().isGroupSummary();
                String str = "intrsv=%c:grnk=0x%04x:gsmry=%c:%s:rnk=0x%04x";
                Object[] objArr = new Object[5];
                objArr[DEFAULT_PRIORITY] = Character.valueOf(record.isRecentlyIntrusive() ? '0' : '1');
                objArr[XML_VERSION] = Integer.valueOf(groupProxy.getAuthoritativeRank());
                objArr[2] = Character.valueOf(isGroupSummary ? '0' : '1');
                objArr[3] = groupSortKeyPortion;
                objArr[4] = Integer.valueOf(record.getAuthoritativeRank());
                record.setGlobalSortKey(String.format(str, objArr));
            }
            this.mProxyByGroupTmp.clear();
        }
        Collections.sort(notificationList, this.mFinalComparator);
    }

    public int indexOf(ArrayList<NotificationRecord> notificationList, NotificationRecord target) {
        return Collections.binarySearch(notificationList, target, this.mFinalComparator);
    }

    private static int safeInt(XmlPullParser parser, String att, int defValue) {
        return tryParseInt(parser.getAttributeValue(null, att), defValue);
    }

    private static int tryParseInt(String value, int defValue) {
        if (TextUtils.isEmpty(value)) {
            return defValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    private static boolean tryParseBool(String value, boolean defValue) {
        if (TextUtils.isEmpty(value)) {
            return defValue;
        }
        return Boolean.valueOf(value).booleanValue();
    }

    public int getPriority(String packageName, int uid) {
        return getOrCreateRecord(packageName, uid).priority;
    }

    public void setPriority(String packageName, int uid, int priority) {
        getOrCreateRecord(packageName, uid).priority = priority;
        updateConfig();
    }

    public int getVisibilityOverride(String packageName, int uid) {
        return getOrCreateRecord(packageName, uid).visibility;
    }

    public void setVisibilityOverride(String pkgName, int uid, int visibility) {
        getOrCreateRecord(pkgName, uid).visibility = visibility;
        updateConfig();
    }

    public int getImportance(String packageName, int uid) {
        return getOrCreateRecord(packageName, uid).importance;
    }

    public void setImportance(String pkgName, int uid, int importance) {
        getOrCreateRecord(pkgName, uid).importance = importance;
        updateConfig();
    }

    public void setEnabled(String packageName, int uid, boolean enabled) {
        boolean wasEnabled;
        int i = DEFAULT_PRIORITY;
        if (getImportance(packageName, uid) != 0) {
            wasEnabled = true;
        } else {
            wasEnabled = false;
        }
        if (wasEnabled != enabled) {
            if (enabled) {
                i = DEFAULT_VISIBILITY;
            }
            setImportance(packageName, uid, i);
        }
    }

    public void dump(PrintWriter pw, String prefix, DumpFilter filter) {
        if (filter == null) {
            int N = this.mSignalExtractors.length;
            pw.print(prefix);
            pw.print("mSignalExtractors.length = ");
            pw.println(N);
            for (int i = DEFAULT_PRIORITY; i < N; i += XML_VERSION) {
                pw.print(prefix);
                pw.print("  ");
                pw.println(this.mSignalExtractors[i]);
            }
        }
        if (filter == null) {
            pw.print(prefix);
            pw.println("per-package config:");
        }
        pw.println("Records:");
        dumpRecords(pw, prefix, filter, this.mRecords);
        pw.println("Restored without uid:");
        dumpRecords(pw, prefix, filter, this.mRestoredWithoutUids);
    }

    private static void dumpRecords(PrintWriter pw, String prefix, DumpFilter filter, ArrayMap<String, Record> records) {
        int N = records.size();
        for (int i = DEFAULT_PRIORITY; i < N; i += XML_VERSION) {
            Record r = (Record) records.valueAt(i);
            if (filter == null || filter.matches(r.pkg)) {
                String str;
                pw.print(prefix);
                pw.print("  ");
                pw.print(r.pkg);
                pw.print(" (");
                if (r.uid == Record.UNKNOWN_UID) {
                    str = "UNKNOWN_UID";
                } else {
                    str = Integer.toString(r.uid);
                }
                pw.print(str);
                pw.print(')');
                if (r.importance != DEFAULT_VISIBILITY) {
                    pw.print(" importance=");
                    pw.print(Ranking.importanceToString(r.importance));
                }
                if (r.priority != 0) {
                    pw.print(" priority=");
                    pw.print(Notification.priorityToString(r.priority));
                }
                if (r.visibility != DEFAULT_VISIBILITY) {
                    pw.print(" visibility=");
                    pw.print(Notification.visibilityToString(r.visibility));
                }
                pw.println();
            }
        }
    }

    public JSONObject dumpJson(DumpFilter filter) {
        JSONObject ranking = new JSONObject();
        JSONArray records = new JSONArray();
        try {
            ranking.put("noUid", this.mRestoredWithoutUids.size());
        } catch (JSONException e) {
        }
        int N = this.mRecords.size();
        for (int i = DEFAULT_PRIORITY; i < N; i += XML_VERSION) {
            Record r = (Record) this.mRecords.valueAt(i);
            if (filter == null || filter.matches(r.pkg)) {
                JSONObject record = new JSONObject();
                try {
                    record.put("userId", UserHandle.getUserId(r.uid));
                    record.put("packageName", r.pkg);
                    if (r.importance != DEFAULT_VISIBILITY) {
                        record.put(ATT_IMPORTANCE, Ranking.importanceToString(r.importance));
                    }
                    if (r.priority != 0) {
                        record.put(ATT_PRIORITY, Notification.priorityToString(r.priority));
                    }
                    if (r.visibility != DEFAULT_VISIBILITY) {
                        record.put(ATT_VISIBILITY, Notification.visibilityToString(r.visibility));
                    }
                } catch (JSONException e2) {
                }
                records.put(record);
            }
        }
        try {
            ranking.put("records", records);
        } catch (JSONException e3) {
        }
        return ranking;
    }

    public JSONArray dumpBansJson(DumpFilter filter) {
        JSONArray bans = new JSONArray();
        for (Entry<Integer, String> ban : getPackageBans().entrySet()) {
            int userId = UserHandle.getUserId(((Integer) ban.getKey()).intValue());
            String packageName = (String) ban.getValue();
            if (filter == null || filter.matches(packageName)) {
                JSONObject banJson = new JSONObject();
                try {
                    banJson.put("userId", userId);
                    banJson.put("packageName", packageName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                bans.put(banJson);
            }
        }
        return bans;
    }

    public Map<Integer, String> getPackageBans() {
        int N = this.mRecords.size();
        ArrayMap<Integer, String> packageBans = new ArrayMap(N);
        for (int i = DEFAULT_PRIORITY; i < N; i += XML_VERSION) {
            Record r = (Record) this.mRecords.valueAt(i);
            if (r.importance == 0) {
                packageBans.put(Integer.valueOf(r.uid), r.pkg);
            }
        }
        return packageBans;
    }

    public void onPackagesChanged(boolean queryReplace, String[] pkgList) {
        if (!queryReplace && pkgList != null && pkgList.length != 0 && !this.mRestoredWithoutUids.isEmpty()) {
            PackageManager pm = this.mContext.getPackageManager();
            boolean updated = false;
            int length = pkgList.length;
            for (int i = DEFAULT_PRIORITY; i < length; i += XML_VERSION) {
                String pkg = pkgList[i];
                Record r = (Record) this.mRestoredWithoutUids.get(pkg);
                if (r != null) {
                    try {
                        r.uid = pm.getPackageUidAsUser(r.pkg, DEFAULT_PRIORITY);
                        this.mRestoredWithoutUids.remove(pkg);
                        this.mRecords.put(recordKey(r.pkg, r.uid), r);
                        updated = true;
                    } catch (NameNotFoundException e) {
                    }
                }
            }
            if (updated) {
                updateConfig();
            }
        }
    }
}
