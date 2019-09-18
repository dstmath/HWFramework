package com.android.server.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.metrics.LogMaker;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.notification.NotificationManagerService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class RankingHelper implements RankingConfig {
    private static final String ATT_APP_USER_LOCKED_FIELDS = "app_user_locked_fields";
    private static final String ATT_ID = "id";
    private static final String ATT_IMPORTANCE = "importance";
    private static final String ATT_NAME = "name";
    private static final String ATT_PRIORITY = "priority";
    private static final String ATT_SHOW_BADGE = "show_badge";
    private static final String ATT_UID = "uid";
    private static final String ATT_VERSION = "version";
    private static final String ATT_VISIBILITY = "visibility";
    private static final int DEFAULT_IMPORTANCE = -1000;
    private static final int DEFAULT_LOCKED_APP_FIELDS = 0;
    private static final int DEFAULT_PRIORITY = 0;
    private static final boolean DEFAULT_SHOW_BADGE = true;
    private static final int DEFAULT_VISIBILITY = -1000;
    private static final String TAG = "RankingHelper";
    private static final String TAG_CHANNEL = "channel";
    private static final String TAG_GROUP = "channelGroup";
    private static final String TAG_PACKAGE = "package";
    static final String TAG_RANKING = "ranking";
    private static final int XML_VERSION = 1;
    private boolean mAreChannelsBypassingDnd;
    private SparseBooleanArray mBadgingEnabled;
    private final Context mContext;
    private final GlobalSortKeyComparator mFinalComparator = new GlobalSortKeyComparator();
    private final PackageManager mPm;
    private final NotificationComparator mPreliminaryComparator;
    private final ArrayMap<String, NotificationRecord> mProxyByGroupTmp = new ArrayMap<>();
    private final RankingHandler mRankingHandler;
    private final ArrayMap<String, Record> mRecords = new ArrayMap<>();
    private final ArrayMap<String, Record> mRestoredWithoutUids = new ArrayMap<>();
    private final NotificationSignalExtractor[] mSignalExtractors;
    private final ArrayMap<String, NotificationSysMgrCfg> mSysMgrCfgMap = new ArrayMap<>();
    private ZenModeHelper mZenModeHelper;

    public @interface LockableAppFields {
        public static final int USER_LOCKED_IMPORTANCE = 1;
    }

    public static class NotificationSysMgrCfg {
        int smc_bypassDND;
        int smc_iconBadge;
        int smc_importance;
        String smc_packageName;
        int smc_userId;
        int smc_visilibity;
    }

    private static class Record {
        static int UNKNOWN_UID = -10000;
        ArrayMap<String, NotificationChannel> channels;
        Map<String, NotificationChannelGroup> groups;
        int importance;
        int lockedAppFields;
        String pkg;
        int priority;
        boolean showBadge;
        int uid;
        int visibility;

        private Record() {
            this.uid = UNKNOWN_UID;
            this.importance = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            this.priority = 0;
            this.visibility = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            this.showBadge = true;
            this.lockedAppFields = 0;
            this.channels = new ArrayMap<>();
            this.groups = new ConcurrentHashMap();
        }
    }

    public RankingHelper(Context context, PackageManager pm, RankingHandler rankingHandler, ZenModeHelper zenHelper, NotificationUsageStats usageStats, String[] extractorNames) {
        this.mContext = context;
        this.mRankingHandler = rankingHandler;
        this.mPm = pm;
        this.mZenModeHelper = zenHelper;
        this.mPreliminaryComparator = new NotificationComparator(this.mContext);
        updateBadgingEnabled();
        int N = extractorNames.length;
        this.mSignalExtractors = new NotificationSignalExtractor[N];
        boolean z = false;
        for (int i = 0; i < N; i++) {
            try {
                NotificationSignalExtractor extractor = (NotificationSignalExtractor) this.mContext.getClassLoader().loadClass(extractorNames[i]).newInstance();
                extractor.initialize(this.mContext, usageStats);
                extractor.setConfig(this);
                extractor.setZenHelper(zenHelper);
                this.mSignalExtractors[i] = extractor;
            } catch (ClassNotFoundException e) {
                Slog.w(TAG, "Couldn't find extractor " + extractorNames[i] + ".", e);
            } catch (InstantiationException e2) {
                Slog.w(TAG, "Couldn't instantiate extractor " + extractorNames[i] + ".", e2);
            } catch (IllegalAccessException e3) {
                Slog.w(TAG, "Problem accessing extractor " + extractorNames[i] + ".", e3);
            }
        }
        this.mAreChannelsBypassingDnd = (this.mZenModeHelper.getNotificationPolicy().state & 1) == 1 ? true : z;
        updateChannelsBypassingDnd();
    }

    public <T extends NotificationSignalExtractor> T findExtractor(Class<T> extractorClass) {
        for (NotificationSignalExtractor extractor : this.mSignalExtractors) {
            if (extractorClass.equals(extractor.getClass())) {
                return extractor;
            }
        }
        return null;
    }

    public void extractSignals(NotificationRecord r) {
        for (NotificationSignalExtractor extractor : this.mSignalExtractors) {
            try {
                RankingReconsideration recon = extractor.process(r);
                if (recon != null) {
                    this.mRankingHandler.requestReconsideration(recon);
                }
            } catch (Throwable t) {
                Slog.w(TAG, "NotificationSignalExtractor failed.", t);
            }
        }
    }

    public void readXml(XmlPullParser parser, boolean forRestore) throws XmlPullParserException, IOException {
        int uid;
        Record r;
        int innerDepth;
        int innerDepth2;
        int innerDepth3;
        String str;
        XmlPullParser xmlPullParser = parser;
        int i = 2;
        if (parser.getEventType() == 2 && TAG_RANKING.equals(parser.getName())) {
            this.mRestoredWithoutUids.clear();
            while (true) {
                int next = parser.next();
                int type = next;
                if (next != 1) {
                    String tag = parser.getName();
                    if (type != 3 || !TAG_RANKING.equals(tag)) {
                        if (type == i && "package".equals(tag)) {
                            int uid2 = XmlUtils.readIntAttribute(xmlPullParser, "uid", Record.UNKNOWN_UID);
                            String name = xmlPullParser.getAttributeValue(null, "name");
                            if (!TextUtils.isEmpty(name)) {
                                if (forRestore) {
                                    try {
                                        uid = this.mPm.getPackageUidAsUser(name, 0);
                                    } catch (PackageManager.NameNotFoundException e) {
                                    }
                                    int readIntAttribute = XmlUtils.readIntAttribute(xmlPullParser, ATT_IMPORTANCE, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                                    int readIntAttribute2 = XmlUtils.readIntAttribute(xmlPullParser, ATT_PRIORITY, 0);
                                    int i2 = readIntAttribute2;
                                    String str2 = name;
                                    r = getOrCreateRecord(name, uid, readIntAttribute, i2, XmlUtils.readIntAttribute(xmlPullParser, ATT_VISIBILITY, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE), XmlUtils.readBooleanAttribute(xmlPullParser, ATT_SHOW_BADGE, true));
                                    r.importance = XmlUtils.readIntAttribute(xmlPullParser, ATT_IMPORTANCE, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                                    r.priority = XmlUtils.readIntAttribute(xmlPullParser, ATT_PRIORITY, 0);
                                    r.visibility = XmlUtils.readIntAttribute(xmlPullParser, ATT_VISIBILITY, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                                    r.showBadge = XmlUtils.readBooleanAttribute(xmlPullParser, ATT_SHOW_BADGE, true);
                                    r.lockedAppFields = XmlUtils.readIntAttribute(xmlPullParser, ATT_APP_USER_LOCKED_FIELDS, 0);
                                    innerDepth = parser.getDepth();
                                    while (true) {
                                        innerDepth2 = innerDepth;
                                        innerDepth3 = parser.next();
                                        type = innerDepth3;
                                        if (innerDepth3 != 1 || (type == 3 && parser.getDepth() <= innerDepth2)) {
                                            try {
                                                deleteDefaultChannelIfNeeded(r);
                                                break;
                                            } catch (PackageManager.NameNotFoundException e2) {
                                                PackageManager.NameNotFoundException nameNotFoundException = e2;
                                                Slog.e(TAG, "deleteDefaultChannelIfNeeded - Exception: " + e2);
                                            }
                                        } else {
                                            if (type != 3 && type != 4) {
                                                String tagName = parser.getName();
                                                if (TAG_GROUP.equals(tagName)) {
                                                    str = null;
                                                    String id = xmlPullParser.getAttributeValue(null, ATT_ID);
                                                    CharSequence groupName = xmlPullParser.getAttributeValue(null, "name");
                                                    if (!TextUtils.isEmpty(id)) {
                                                        NotificationChannelGroup group = new NotificationChannelGroup(id, groupName);
                                                        group.populateFromXml(xmlPullParser);
                                                        r.groups.put(id, group);
                                                    }
                                                } else {
                                                    str = null;
                                                }
                                                if (TAG_CHANNEL.equals(tagName)) {
                                                    String id2 = xmlPullParser.getAttributeValue(str, ATT_ID);
                                                    String channelName = xmlPullParser.getAttributeValue(str, "name");
                                                    int channelImportance = XmlUtils.readIntAttribute(xmlPullParser, ATT_IMPORTANCE, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                                                    if (!TextUtils.isEmpty(id2) && !TextUtils.isEmpty(channelName)) {
                                                        NotificationChannel channel = new NotificationChannel(id2, channelName, channelImportance);
                                                        if (forRestore) {
                                                            channel.populateFromXmlForRestore(xmlPullParser, this.mContext);
                                                        } else {
                                                            channel.populateFromXml(xmlPullParser);
                                                        }
                                                        r.channels.put(id2, channel);
                                                    }
                                                }
                                            }
                                            innerDepth = innerDepth2;
                                        }
                                    }
                                }
                                uid = uid2;
                                int readIntAttribute3 = XmlUtils.readIntAttribute(xmlPullParser, ATT_IMPORTANCE, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                                int readIntAttribute22 = XmlUtils.readIntAttribute(xmlPullParser, ATT_PRIORITY, 0);
                                int i22 = readIntAttribute22;
                                String str22 = name;
                                r = getOrCreateRecord(name, uid, readIntAttribute3, i22, XmlUtils.readIntAttribute(xmlPullParser, ATT_VISIBILITY, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE), XmlUtils.readBooleanAttribute(xmlPullParser, ATT_SHOW_BADGE, true));
                                r.importance = XmlUtils.readIntAttribute(xmlPullParser, ATT_IMPORTANCE, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                                r.priority = XmlUtils.readIntAttribute(xmlPullParser, ATT_PRIORITY, 0);
                                r.visibility = XmlUtils.readIntAttribute(xmlPullParser, ATT_VISIBILITY, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                                r.showBadge = XmlUtils.readBooleanAttribute(xmlPullParser, ATT_SHOW_BADGE, true);
                                r.lockedAppFields = XmlUtils.readIntAttribute(xmlPullParser, ATT_APP_USER_LOCKED_FIELDS, 0);
                                innerDepth = parser.getDepth();
                                while (true) {
                                    innerDepth2 = innerDepth;
                                    innerDepth3 = parser.next();
                                    type = innerDepth3;
                                    if (innerDepth3 != 1) {
                                    }
                                    deleteDefaultChannelIfNeeded(r);
                                    break;
                                    innerDepth = innerDepth2;
                                }
                            }
                        }
                        i = 2;
                    } else {
                        return;
                    }
                } else {
                    throw new IllegalStateException("Failed to reach END_DOCUMENT");
                }
            }
        }
    }

    private static String recordKey(String pkg, int uid) {
        return pkg + "|" + uid;
    }

    private Record getRecord(String pkg, int uid) {
        Record record;
        String key = recordKey(pkg, uid);
        synchronized (this.mRecords) {
            record = this.mRecords.get(key);
        }
        return record;
    }

    private Record getOrCreateRecord(String pkg, int uid) {
        return getOrCreateRecord(pkg, uid, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE, 0, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE, true);
    }

    private Record getOrCreateRecord(String pkg, int uid, int importance, int priority, int visibility, boolean showBadge) {
        Record r;
        String key = recordKey(pkg, uid);
        synchronized (this.mRecords) {
            r = (Record) (uid == Record.UNKNOWN_UID ? this.mRestoredWithoutUids.get(pkg) : this.mRecords.get(key));
            if (r == null) {
                r = new Record();
                r.pkg = pkg;
                r.uid = uid;
                r.importance = importance;
                r.priority = priority;
                r.visibility = visibility;
                r.showBadge = showBadge;
                NotificationSysMgrCfg cfg = this.mSysMgrCfgMap.get(key);
                if ("com.android.mms".equals(pkg) && cfg != null) {
                    Log.i(TAG, "cfg.importance = " + cfg.smc_importance);
                }
                if (cfg != null) {
                    r.importance = cfg.smc_importance;
                    r.visibility = cfg.smc_visilibity;
                    int i = 0;
                    r.showBadge = cfg.smc_iconBadge != 0;
                    if (cfg.smc_bypassDND != 0) {
                        i = 2;
                    }
                    r.priority = i;
                }
                try {
                    createDefaultChannelIfNeeded(r);
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.e(TAG, "createDefaultChannelIfNeeded - Exception: " + e);
                }
                if (r.uid == Record.UNKNOWN_UID) {
                    this.mRestoredWithoutUids.put(pkg, r);
                } else if (r.uid >= 0) {
                    this.mRecords.put(key, r);
                } else {
                    Log.e(TAG, "record uid is below zero", new Throwable());
                }
            }
            if ("com.android.mms".equals(pkg)) {
                Slog.i(TAG, "getOrCreateRecord - importance = " + r.importance + "priority = " + r.priority);
            }
        }
        return r;
    }

    private boolean shouldHaveDefaultChannel(Record r) throws PackageManager.NameNotFoundException {
        if (this.mPm.getApplicationInfoAsUser(r.pkg, 0, UserHandle.getUserId(r.uid)).targetSdkVersion >= 26) {
            return false;
        }
        return true;
    }

    private void deleteDefaultChannelIfNeeded(Record r) throws PackageManager.NameNotFoundException {
        if (r.channels.containsKey("miscellaneous") && !shouldHaveDefaultChannel(r)) {
            r.channels.remove("miscellaneous");
        }
    }

    private void createDefaultChannelIfNeeded(Record r) throws PackageManager.NameNotFoundException {
        if (r.channels.containsKey("miscellaneous")) {
            r.channels.get("miscellaneous").setName(this.mContext.getString(17039922));
        } else if (shouldHaveDefaultChannel(r)) {
            NotificationChannel channel = new NotificationChannel("miscellaneous", this.mContext.getString(17039922), r.importance);
            channel.setBypassDnd(r.priority == 2);
            channel.setLockscreenVisibility(r.visibility);
            if (r.importance != -1000) {
                channel.lockFields(4);
            }
            if (r.priority != 0) {
                channel.lockFields(1);
            }
            if (r.visibility != -1000) {
                channel.lockFields(2);
            }
            r.channels.put(channel.getId(), channel);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0063  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0110 A[SYNTHETIC] */
    public void writeXml(XmlSerializer out, boolean forBackup) throws IOException {
        boolean hasNonDefaultSettings;
        out.startTag(null, TAG_RANKING);
        out.attribute(null, ATT_VERSION, Integer.toString(1));
        synchronized (this.mRecords) {
            int N = this.mRecords.size();
            for (int i = 0; i < N; i++) {
                Record r = this.mRecords.valueAt(i);
                if (r != null) {
                    if (!forBackup || UserHandle.getUserId(r.uid) == 0) {
                        if (r.importance == -1000 && r.priority == 0 && r.visibility == -1000 && r.showBadge && r.lockedAppFields == 0 && r.channels.size() <= 0) {
                            if (r.groups.size() <= 0) {
                                hasNonDefaultSettings = false;
                                if (!hasNonDefaultSettings) {
                                    out.startTag(null, "package");
                                    out.attribute(null, "name", r.pkg);
                                    if (r.importance != -1000) {
                                        out.attribute(null, ATT_IMPORTANCE, Integer.toString(r.importance));
                                    }
                                    if (r.priority != 0) {
                                        out.attribute(null, ATT_PRIORITY, Integer.toString(r.priority));
                                    }
                                    if (r.visibility != -1000) {
                                        out.attribute(null, ATT_VISIBILITY, Integer.toString(r.visibility));
                                    }
                                    out.attribute(null, ATT_SHOW_BADGE, Boolean.toString(r.showBadge));
                                    out.attribute(null, ATT_APP_USER_LOCKED_FIELDS, Integer.toString(r.lockedAppFields));
                                    if (!forBackup) {
                                        out.attribute(null, "uid", Integer.toString(r.uid));
                                    }
                                    for (NotificationChannelGroup group : r.groups.values()) {
                                        group.writeXml(out);
                                    }
                                    for (NotificationChannel channel : r.channels.values()) {
                                        if (channel != null) {
                                            if (!forBackup) {
                                                channel.writeXml(out);
                                            } else if (!channel.isDeleted()) {
                                                channel.writeXmlForBackup(out, this.mContext);
                                            }
                                        }
                                    }
                                    out.endTag(null, "package");
                                }
                            }
                        }
                        hasNonDefaultSettings = true;
                        if (!hasNonDefaultSettings) {
                        }
                    }
                }
            }
        }
        out.endTag(null, TAG_RANKING);
    }

    private void updateConfig() {
        for (NotificationSignalExtractor config : this.mSignalExtractors) {
            config.setConfig(this);
        }
        this.mRankingHandler.requestSort();
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00b2 A[Catch:{ all -> 0x0044 }] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00b7 A[Catch:{ all -> 0x0044 }] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00c1 A[Catch:{ all -> 0x0044 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00c2 A[Catch:{ all -> 0x0044 }] */
    public void sort(ArrayList<NotificationRecord> notificationList) {
        String groupSortKeyPortion;
        boolean isGroupSummary;
        ArrayList<NotificationRecord> arrayList = notificationList;
        int N = notificationList.size();
        for (int i = N - 1; i >= 0; i--) {
            arrayList.get(i).setGlobalSortKey(null);
        }
        Collections.sort(arrayList, this.mPreliminaryComparator);
        synchronized (this.mProxyByGroupTmp) {
            int i2 = N - 1;
            while (i2 >= 0) {
                try {
                    NotificationRecord record = arrayList.get(i2);
                    record.setAuthoritativeRank(i2);
                    String groupKey = record.getGroupKey();
                    if (this.mProxyByGroupTmp.get(groupKey) == null) {
                        this.mProxyByGroupTmp.put(groupKey, record);
                    }
                    i2--;
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            }
            for (int i3 = 0; i3 < N; i3++) {
                NotificationRecord record2 = arrayList.get(i3);
                NotificationRecord groupProxy = this.mProxyByGroupTmp.get(record2.getGroupKey());
                String groupSortKey = record2.getNotification().getSortKey();
                if (groupSortKey == null) {
                    groupSortKeyPortion = "nsk";
                } else if (groupSortKey.equals(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS)) {
                    groupSortKeyPortion = "esk";
                } else {
                    groupSortKeyPortion = "gsk=" + groupSortKey;
                    isGroupSummary = record2.getNotification().isGroupSummary();
                    Object[] objArr = new Object[5];
                    char c = '0';
                    objArr[0] = Character.valueOf((record2.isRecentlyIntrusive() || record2.getImportance() <= 1) ? '1' : '0');
                    objArr[1] = Integer.valueOf(groupProxy == null ? groupProxy.getAuthoritativeRank() : i3);
                    if (isGroupSummary) {
                        c = '1';
                    }
                    objArr[2] = Character.valueOf(c);
                    objArr[3] = groupSortKeyPortion;
                    objArr[4] = Integer.valueOf(record2.getAuthoritativeRank());
                    record2.setGlobalSortKey(String.format("intrsv=%c:grnk=0x%04x:gsmry=%c:%s:rnk=0x%04x", objArr));
                }
                isGroupSummary = record2.getNotification().isGroupSummary();
                Object[] objArr2 = new Object[5];
                char c2 = '0';
                objArr2[0] = Character.valueOf((record2.isRecentlyIntrusive() || record2.getImportance() <= 1) ? '1' : '0');
                objArr2[1] = Integer.valueOf(groupProxy == null ? groupProxy.getAuthoritativeRank() : i3);
                if (isGroupSummary) {
                }
                objArr2[2] = Character.valueOf(c2);
                objArr2[3] = groupSortKeyPortion;
                objArr2[4] = Integer.valueOf(record2.getAuthoritativeRank());
                record2.setGlobalSortKey(String.format("intrsv=%c:grnk=0x%04x:gsmry=%c:%s:rnk=0x%04x", objArr2));
            }
            this.mProxyByGroupTmp.clear();
        }
        Collections.sort(arrayList, this.mFinalComparator);
    }

    public int indexOf(ArrayList<NotificationRecord> notificationList, NotificationRecord target) {
        return Collections.binarySearch(notificationList, target, this.mFinalComparator);
    }

    public int getImportance(String packageName, int uid) {
        return getOrCreateRecord(packageName, uid).importance;
    }

    public boolean getIsAppImportanceLocked(String packageName, int uid) {
        return (getOrCreateRecord(packageName, uid).lockedAppFields & 1) != 0;
    }

    public boolean canShowBadge(String packageName, int uid) {
        return getOrCreateRecord(packageName, uid).showBadge;
    }

    public void setShowBadge(String packageName, int uid, boolean showBadge) {
        getOrCreateRecord(packageName, uid).showBadge = showBadge;
        updateConfig();
    }

    public boolean isGroupBlocked(String packageName, int uid, String groupId) {
        if (groupId == null) {
            return false;
        }
        NotificationChannelGroup group = getOrCreateRecord(packageName, uid).groups.get(groupId);
        if (group == null) {
            return false;
        }
        return group.isBlocked();
    }

    /* access modifiers changed from: package-private */
    public int getPackagePriority(String pkg, int uid) {
        return getOrCreateRecord(pkg, uid).priority;
    }

    /* access modifiers changed from: package-private */
    public int getPackageVisibility(String pkg, int uid) {
        return getOrCreateRecord(pkg, uid).visibility;
    }

    public void createNotificationChannelGroup(String pkg, int uid, NotificationChannelGroup group, boolean fromTargetApp) {
        Preconditions.checkNotNull(pkg);
        Preconditions.checkNotNull(group);
        Preconditions.checkNotNull(group.getId());
        Preconditions.checkNotNull(Boolean.valueOf(!TextUtils.isEmpty(group.getName())));
        Record r = getOrCreateRecord(pkg, uid);
        if (r != null) {
            NotificationChannelGroup oldGroup = r.groups.get(group.getId());
            if (!group.equals(oldGroup)) {
                MetricsLogger.action(getChannelGroupLog(group.getId(), pkg));
            }
            if (oldGroup != null) {
                group.setChannels(oldGroup.getChannels());
                if (fromTargetApp) {
                    group.setBlocked(oldGroup.isBlocked());
                }
            }
            r.groups.put(group.getId(), group);
            return;
        }
        throw new IllegalArgumentException("Invalid package");
    }

    public void createNotificationChannel(String pkg, int uid, NotificationChannel channel, boolean fromTargetApp, boolean hasDndAccess) {
        Preconditions.checkNotNull(pkg);
        Preconditions.checkNotNull(channel);
        Preconditions.checkNotNull(channel.getId());
        Preconditions.checkArgument(!TextUtils.isEmpty(channel.getName()));
        Record r = getOrCreateRecord(pkg, uid);
        if (r == null) {
            throw new IllegalArgumentException("Invalid package");
        } else if (channel.getGroup() != null && !r.groups.containsKey(channel.getGroup())) {
            throw new IllegalArgumentException("NotificationChannelGroup doesn't exist");
        } else if (!"miscellaneous".equals(channel.getId())) {
            NotificationChannel existing = r.channels.get(channel.getId());
            if (existing != null && fromTargetApp) {
                if (existing.isDeleted()) {
                    existing.setDeleted(false);
                    MetricsLogger.action(getChannelLog(channel, pkg).setType(1));
                }
                existing.setName(channel.getName().toString());
                existing.setDescription(channel.getDescription());
                existing.setBlockableSystem(channel.isBlockableSystem());
                if (existing.getGroup() == null) {
                    existing.setGroup(channel.getGroup());
                }
                if (existing.getUserLockedFields() == 0 && channel.getImportance() < existing.getImportance()) {
                    existing.setImportance(channel.getImportance());
                }
                if (existing.getUserLockedFields() == 0 && hasDndAccess) {
                    boolean bypassDnd = channel.canBypassDnd();
                    existing.setBypassDnd(bypassDnd);
                    if (bypassDnd != this.mAreChannelsBypassingDnd) {
                        updateChannelsBypassingDnd();
                    }
                }
                updateConfig();
            } else if (channel.getImportance() < 0 || channel.getImportance() > 5) {
                throw new IllegalArgumentException("Invalid importance level");
            } else {
                if (fromTargetApp && !hasDndAccess) {
                    channel.setBypassDnd(r.priority == 2);
                }
                if (fromTargetApp) {
                    channel.setLockscreenVisibility(r.visibility);
                }
                clearLockedFields(channel);
                if (channel.getLockscreenVisibility() == 1) {
                    channel.setLockscreenVisibility(JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                }
                if (!r.showBadge) {
                    channel.setShowBadge(false);
                }
                r.channels.put(channel.getId(), channel);
                if (channel.canBypassDnd() != this.mAreChannelsBypassingDnd) {
                    updateChannelsBypassingDnd();
                }
                MetricsLogger.action(getChannelLog(channel, pkg).setType(1));
            }
        } else {
            throw new IllegalArgumentException("Reserved id");
        }
    }

    /* access modifiers changed from: package-private */
    public void clearLockedFields(NotificationChannel channel) {
        channel.unlockFields(channel.getUserLockedFields());
    }

    public void updateNotificationChannel(String pkg, int uid, NotificationChannel updatedChannel, boolean fromUser) {
        Preconditions.checkNotNull(updatedChannel);
        Preconditions.checkNotNull(updatedChannel.getId());
        Record r = getOrCreateRecord(pkg, uid);
        if (r != null) {
            NotificationChannel channel = r.channels.get(updatedChannel.getId());
            if (channel == null || channel.isDeleted()) {
                throw new IllegalArgumentException("Channel does not exist");
            }
            if (updatedChannel.getLockscreenVisibility() == 1) {
                updatedChannel.setLockscreenVisibility(JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
            }
            if (!fromUser) {
                updatedChannel.unlockFields(updatedChannel.getUserLockedFields());
            }
            if (fromUser) {
                updatedChannel.lockFields(channel.getUserLockedFields());
                lockFieldsForUpdate(channel, updatedChannel);
            }
            r.channels.put(updatedChannel.getId(), updatedChannel);
            if ("miscellaneous".equals(updatedChannel.getId()) && r.channels.size() == 1) {
                Slog.d(TAG, "only modify pkg when there is single channel");
                r.importance = updatedChannel.getImportance();
                r.priority = updatedChannel.canBypassDnd() ? 2 : 0;
                r.visibility = updatedChannel.getLockscreenVisibility();
                r.showBadge = updatedChannel.canShowBadge();
            }
            if (!channel.equals(updatedChannel)) {
                MetricsLogger.action(getChannelLog(updatedChannel, pkg));
            }
            if (updatedChannel.canBypassDnd() != this.mAreChannelsBypassingDnd) {
                updateChannelsBypassingDnd();
            }
            Slog.i(TAG, "RankingHelper: updateNotificationChannel importance = " + r.importance);
            updateConfig();
            return;
        }
        throw new IllegalArgumentException("Invalid package");
    }

    public NotificationChannel getNotificationChannel(String pkg, int uid, String channelId, boolean includeDeleted) {
        Preconditions.checkNotNull(pkg);
        Record r = getOrCreateRecord(pkg, uid);
        if (r == null) {
            return null;
        }
        if (channelId == null) {
            channelId = "miscellaneous";
        }
        NotificationChannel nc = r.channels.get(channelId);
        if (nc == null || (!includeDeleted && nc.isDeleted())) {
            return null;
        }
        return nc;
    }

    public void deleteNotificationChannel(String pkg, int uid, String channelId) {
        Record r = getRecord(pkg, uid);
        if (r != null) {
            NotificationChannel channel = r.channels.get(channelId);
            if (channel != null) {
                channel.setDeleted(true);
                LogMaker lm = getChannelLog(channel, pkg);
                lm.setType(2);
                MetricsLogger.action(lm);
                if (this.mAreChannelsBypassingDnd && channel.canBypassDnd()) {
                    updateChannelsBypassingDnd();
                }
            }
        }
    }

    @VisibleForTesting
    public void permanentlyDeleteNotificationChannel(String pkg, int uid, String channelId) {
        Preconditions.checkNotNull(pkg);
        Preconditions.checkNotNull(channelId);
        Record r = getRecord(pkg, uid);
        if (r != null) {
            r.channels.remove(channelId);
        }
    }

    public void permanentlyDeleteNotificationChannels(String pkg, int uid) {
        Preconditions.checkNotNull(pkg);
        Record r = getRecord(pkg, uid);
        if (r != null) {
            for (int i = r.channels.size() - 1; i >= 0; i--) {
                String key = r.channels.keyAt(i);
                if (!"miscellaneous".equals(key)) {
                    r.channels.remove(key);
                }
            }
        }
    }

    public NotificationChannelGroup getNotificationChannelGroupWithChannels(String pkg, int uid, String groupId, boolean includeDeleted) {
        Preconditions.checkNotNull(pkg);
        Record r = getRecord(pkg, uid);
        if (r == null || groupId == null || !r.groups.containsKey(groupId)) {
            return null;
        }
        NotificationChannelGroup group = r.groups.get(groupId).clone();
        group.setChannels(new ArrayList());
        int N = r.channels.size();
        for (int i = 0; i < N; i++) {
            NotificationChannel nc = r.channels.valueAt(i);
            if ((includeDeleted || !nc.isDeleted()) && groupId.equals(nc.getGroup())) {
                group.addChannel(nc);
            }
        }
        return group;
    }

    public NotificationChannelGroup getNotificationChannelGroup(String groupId, String pkg, int uid) {
        Preconditions.checkNotNull(pkg);
        Record r = getRecord(pkg, uid);
        if (r == null) {
            return null;
        }
        return r.groups.get(groupId);
    }

    public ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroups(String pkg, int uid, boolean includeDeleted, boolean includeNonGrouped, boolean includeEmpty) {
        Preconditions.checkNotNull(pkg);
        Map<String, NotificationChannelGroup> groups = new ArrayMap<>();
        Record r = getRecord(pkg, uid);
        if (r == null) {
            return ParceledListSlice.emptyList();
        }
        NotificationChannelGroup nonGrouped = new NotificationChannelGroup(null, null);
        int N = r.channels.size();
        for (int i = 0; i < N; i++) {
            NotificationChannel nc = r.channels.valueAt(i);
            if (includeDeleted || !nc.isDeleted()) {
                if (nc.getGroup() == null) {
                    nonGrouped.addChannel(nc);
                } else if (r.groups.get(nc.getGroup()) != null) {
                    NotificationChannelGroup ncg = groups.get(nc.getGroup());
                    if (ncg == null) {
                        ncg = r.groups.get(nc.getGroup()).clone();
                        ncg.setChannels(new ArrayList());
                        groups.put(nc.getGroup(), ncg);
                    }
                    ncg.addChannel(nc);
                }
            }
        }
        if (includeNonGrouped && nonGrouped.getChannels().size() > 0) {
            groups.put(null, nonGrouped);
        }
        if (includeEmpty) {
            for (NotificationChannelGroup group : r.groups.values()) {
                if (!groups.containsKey(group.getId())) {
                    groups.put(group.getId(), group);
                }
            }
        }
        return new ParceledListSlice<>(new ArrayList(groups.values()));
    }

    public List<NotificationChannel> deleteNotificationChannelGroup(String pkg, int uid, String groupId) {
        List<NotificationChannel> deletedChannels = new ArrayList<>();
        Record r = getRecord(pkg, uid);
        if (r == null || TextUtils.isEmpty(groupId)) {
            return deletedChannels;
        }
        r.groups.remove(groupId);
        int N = r.channels.size();
        for (int i = 0; i < N; i++) {
            NotificationChannel nc = r.channels.valueAt(i);
            if (groupId.equals(nc.getGroup())) {
                nc.setDeleted(true);
                deletedChannels.add(nc);
            }
        }
        return deletedChannels;
    }

    public Collection<NotificationChannelGroup> getNotificationChannelGroups(String pkg, int uid) {
        Record r = getRecord(pkg, uid);
        if (r == null) {
            return new ArrayList();
        }
        return r.groups.values();
    }

    public ParceledListSlice<NotificationChannel> getNotificationChannels(String pkg, int uid, boolean includeDeleted) {
        Preconditions.checkNotNull(pkg);
        List<NotificationChannel> channels = new ArrayList<>();
        Record r = getRecord(pkg, uid);
        if (r == null) {
            return ParceledListSlice.emptyList();
        }
        int N = r.channels.size();
        for (int i = 0; i < N; i++) {
            NotificationChannel nc = r.channels.valueAt(i);
            if (includeDeleted || !nc.isDeleted()) {
                channels.add(nc);
            }
        }
        return new ParceledListSlice<>(channels);
    }

    public boolean onlyHasDefaultChannel(String pkg, int uid) {
        Record r = getOrCreateRecord(pkg, uid);
        if (r.channels.size() != 1 || !r.channels.containsKey("miscellaneous")) {
            return false;
        }
        return true;
    }

    public int getDeletedChannelCount(String pkg, int uid) {
        Preconditions.checkNotNull(pkg);
        int deletedCount = 0;
        Record r = getRecord(pkg, uid);
        if (r == null) {
            return 0;
        }
        int N = r.channels.size();
        for (int i = 0; i < N; i++) {
            if (r.channels.valueAt(i).isDeleted()) {
                deletedCount++;
            }
        }
        return deletedCount;
    }

    public int getBlockedChannelCount(String pkg, int uid) {
        Preconditions.checkNotNull(pkg);
        int blockedCount = 0;
        Record r = getRecord(pkg, uid);
        if (r == null) {
            return 0;
        }
        int N = r.channels.size();
        for (int i = 0; i < N; i++) {
            NotificationChannel nc = r.channels.valueAt(i);
            if (!nc.isDeleted() && nc.getImportance() == 0) {
                blockedCount++;
            }
        }
        return blockedCount;
    }

    public int getBlockedAppCount(int userId) {
        int count = 0;
        synchronized (this.mRecords) {
            int N = this.mRecords.size();
            for (int i = 0; i < N; i++) {
                Record r = this.mRecords.valueAt(i);
                if (userId == UserHandle.getUserId(r.uid) && r.importance == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003f, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0049, code lost:
        if (r9.mAreChannelsBypassingDnd == false) goto L_0x0050;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004b, code lost:
        r9.mAreChannelsBypassingDnd = false;
        updateZenPolicy(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0050, code lost:
        return;
     */
    public void updateChannelsBypassingDnd() {
        synchronized (this.mRecords) {
            int numRecords = this.mRecords.size();
            for (int recordIndex = 0; recordIndex < numRecords; recordIndex++) {
                Record r = this.mRecords.valueAt(recordIndex);
                int numChannels = r.channels.size();
                int channelIndex = 0;
                while (channelIndex < numChannels) {
                    NotificationChannel channel = r.channels.valueAt(channelIndex);
                    if (channel == null || channel.isDeleted() || !channel.canBypassDnd()) {
                        channelIndex++;
                    } else if (!this.mAreChannelsBypassingDnd) {
                        this.mAreChannelsBypassingDnd = true;
                        updateZenPolicy(true);
                    }
                }
            }
        }
    }

    public void updateZenPolicy(boolean areChannelsBypassingDnd) {
        int i;
        NotificationManager.Policy policy = this.mZenModeHelper.getNotificationPolicy();
        ZenModeHelper zenModeHelper = this.mZenModeHelper;
        int i2 = policy.priorityCategories;
        int i3 = policy.priorityCallSenders;
        int i4 = policy.priorityMessageSenders;
        int i5 = policy.suppressedVisualEffects;
        if (areChannelsBypassingDnd) {
            i = 1;
        } else {
            i = 0;
        }
        NotificationManager.Policy policy2 = new NotificationManager.Policy(i2, i3, i4, i5, i);
        zenModeHelper.setNotificationPolicy(policy2);
    }

    public boolean areChannelsBypassingDnd() {
        return this.mAreChannelsBypassingDnd;
    }

    public void setImportance(String pkgName, int uid, int importance) {
        Slog.i(TAG, "RankingHelper: setImportance: pkgName = " + pkgName + "importance = " + importance);
        getOrCreateRecord(pkgName, uid).importance = importance;
        updateConfig();
    }

    public void setEnabled(String packageName, int uid, boolean enabled) {
        int i = 0;
        if ((getImportance(packageName, uid) != 0) != enabled) {
            if (enabled) {
                i = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            }
            setImportance(packageName, uid, i);
        }
    }

    public void setAppImportanceLocked(String packageName, int uid) {
        Record record = getOrCreateRecord(packageName, uid);
        if ((record.lockedAppFields & 1) == 0) {
            record.lockedAppFields |= 1;
            updateConfig();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void lockFieldsForUpdate(NotificationChannel original, NotificationChannel update) {
        if (original.canBypassDnd() != update.canBypassDnd()) {
            update.lockFields(1);
        }
        if (original.getLockscreenVisibility() != update.getLockscreenVisibility()) {
            update.lockFields(2);
        }
        if (original.getImportance() != update.getImportance()) {
            update.lockFields(4);
        }
        if (!(original.shouldShowLights() == update.shouldShowLights() && original.getLightColor() == update.getLightColor())) {
            update.lockFields(8);
        }
        if (!Objects.equals(original.getSound(), update.getSound())) {
            update.lockFields(32);
        }
        if (!Arrays.equals(original.getVibrationPattern(), update.getVibrationPattern()) || original.shouldVibrate() != update.shouldVibrate()) {
            update.lockFields(16);
        }
        if (original.canShowBadge() != update.canShowBadge()) {
            update.lockFields(128);
        }
    }

    public void dump(PrintWriter pw, String prefix, NotificationManagerService.DumpFilter filter) {
        pw.print(prefix);
        pw.print("mSignalExtractors.length = ");
        pw.println(N);
        for (NotificationSignalExtractor notificationSignalExtractor : this.mSignalExtractors) {
            pw.print(prefix);
            pw.print("  ");
            pw.println(notificationSignalExtractor.getClass().getSimpleName());
        }
        pw.print(prefix);
        pw.println("per-package config:");
        pw.println("Records:");
        synchronized (this.mRecords) {
            dumpRecords(pw, prefix, filter, this.mRecords);
        }
        pw.println("Restored without uid:");
        dumpRecords(pw, prefix, filter, this.mRestoredWithoutUids);
    }

    public void dump(ProtoOutputStream proto, NotificationManagerService.DumpFilter filter) {
        for (NotificationSignalExtractor notificationSignalExtractor : this.mSignalExtractors) {
            proto.write(2237677961217L, notificationSignalExtractor.getClass().getSimpleName());
        }
        synchronized (this.mRecords) {
            dumpRecords(proto, 2246267895810L, filter, this.mRecords);
        }
        dumpRecords(proto, 2246267895811L, filter, this.mRestoredWithoutUids);
    }

    private static void dumpRecords(ProtoOutputStream proto, long fieldId, NotificationManagerService.DumpFilter filter, ArrayMap<String, Record> records) {
        int N = records.size();
        for (int i = 0; i < N; i++) {
            Record r = records.valueAt(i);
            if (filter.matches(r.pkg)) {
                long fToken = proto.start(fieldId);
                proto.write(1138166333441L, r.pkg);
                proto.write(1120986464258L, r.uid);
                proto.write(1172526071811L, r.importance);
                proto.write(1120986464260L, r.priority);
                proto.write(1172526071813L, r.visibility);
                proto.write(1133871366150L, r.showBadge);
                for (NotificationChannel channel : r.channels.values()) {
                    channel.writeToProto(proto, 2246267895815L);
                }
                for (NotificationChannelGroup group : r.groups.values()) {
                    group.writeToProto(proto, 2246267895816L);
                }
                proto.end(fToken);
            }
        }
    }

    private static void dumpRecords(PrintWriter pw, String prefix, NotificationManagerService.DumpFilter filter, ArrayMap<String, Record> records) {
        int N = records.size();
        for (int i = 0; i < N; i++) {
            Record r = records.valueAt(i);
            if (filter.matches(r.pkg)) {
                pw.print(prefix);
                pw.print("  AppSettings: ");
                pw.print(r.pkg);
                pw.print(" (");
                pw.print(r.uid == Record.UNKNOWN_UID ? "UNKNOWN_UID" : Integer.toString(r.uid));
                pw.print(')');
                if (r.importance != -1000) {
                    pw.print(" importance=");
                    pw.print(NotificationListenerService.Ranking.importanceToString(r.importance));
                }
                if (r.priority != 0) {
                    pw.print(" priority=");
                    pw.print(Notification.priorityToString(r.priority));
                }
                if (r.visibility != -1000) {
                    pw.print(" visibility=");
                    pw.print(Notification.visibilityToString(r.visibility));
                }
                pw.print(" showBadge=");
                pw.print(Boolean.toString(r.showBadge));
                pw.println();
                for (NotificationChannel channel : r.channels.values()) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.print("  ");
                    pw.println(channel);
                }
                for (NotificationChannelGroup group : r.groups.values()) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.print("  ");
                    pw.println(group);
                }
            }
        }
    }

    public JSONObject dumpJson(NotificationManagerService.DumpFilter filter) {
        JSONObject ranking = new JSONObject();
        JSONArray records = new JSONArray();
        try {
            ranking.put("noUid", this.mRestoredWithoutUids.size());
        } catch (JSONException e) {
        }
        synchronized (this.mRecords) {
            int N = this.mRecords.size();
            for (int i = 0; i < N; i++) {
                Record r = this.mRecords.valueAt(i);
                if (filter == null || filter.matches(r.pkg)) {
                    JSONObject record = new JSONObject();
                    try {
                        record.put("userId", UserHandle.getUserId(r.uid));
                        record.put("packageName", r.pkg);
                        if (r.importance != -1000) {
                            record.put(ATT_IMPORTANCE, NotificationListenerService.Ranking.importanceToString(r.importance));
                        }
                        if (r.priority != 0) {
                            record.put(ATT_PRIORITY, Notification.priorityToString(r.priority));
                        }
                        if (r.visibility != -1000) {
                            record.put(ATT_VISIBILITY, Notification.visibilityToString(r.visibility));
                        }
                        if (!r.showBadge) {
                            record.put("showBadge", Boolean.valueOf(r.showBadge));
                        }
                        JSONArray channels = new JSONArray();
                        for (NotificationChannel channel : r.channels.values()) {
                            channels.put(channel.toJson());
                        }
                        record.put("channels", channels);
                        JSONArray groups = new JSONArray();
                        for (NotificationChannelGroup group : r.groups.values()) {
                            groups.put(group.toJson());
                        }
                        record.put("groups", groups);
                    } catch (JSONException e2) {
                    }
                    records.put(record);
                }
            }
        }
        try {
            ranking.put("records", records);
        } catch (JSONException e3) {
        }
        return ranking;
    }

    public JSONArray dumpBansJson(NotificationManagerService.DumpFilter filter) {
        JSONArray bans = new JSONArray();
        for (Map.Entry<Integer, String> ban : getPackageBans().entrySet()) {
            int userId = UserHandle.getUserId(ban.getKey().intValue());
            String packageName = ban.getValue();
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
        ArrayMap<Integer, String> packageBans;
        synchronized (this.mRecords) {
            int N = this.mRecords.size();
            packageBans = new ArrayMap<>(N);
            for (int i = 0; i < N; i++) {
                Record r = this.mRecords.valueAt(i);
                if (r.importance == 0) {
                    packageBans.put(Integer.valueOf(r.uid), r.pkg);
                }
            }
        }
        return packageBans;
    }

    public JSONArray dumpChannelsJson(NotificationManagerService.DumpFilter filter) {
        JSONArray channels = new JSONArray();
        for (Map.Entry<String, Integer> channelCount : getPackageChannels().entrySet()) {
            String packageName = channelCount.getKey();
            if (filter == null || filter.matches(packageName)) {
                JSONObject channelCountJson = new JSONObject();
                try {
                    channelCountJson.put("packageName", packageName);
                    channelCountJson.put("channelCount", channelCount.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                channels.put(channelCountJson);
            }
        }
        return channels;
    }

    private Map<String, Integer> getPackageChannels() {
        ArrayMap<String, Integer> packageChannels = new ArrayMap<>();
        synchronized (this.mRecords) {
            for (int i = 0; i < this.mRecords.size(); i++) {
                Record r = this.mRecords.valueAt(i);
                int channelCount = 0;
                for (int j = 0; j < r.channels.size(); j++) {
                    if (!r.channels.valueAt(j).isDeleted()) {
                        channelCount++;
                    }
                }
                packageChannels.put(r.pkg, Integer.valueOf(channelCount));
            }
        }
        return packageChannels;
    }

    public void onUserRemoved(int userId) {
        synchronized (this.mRecords) {
            for (int i = this.mRecords.size() - 1; i >= 0; i--) {
                if (UserHandle.getUserId(this.mRecords.valueAt(i).uid) == userId) {
                    this.mRecords.removeAt(i);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLocaleChanged(Context context, int userId) {
        synchronized (this.mRecords) {
            int N = this.mRecords.size();
            for (int i = 0; i < N; i++) {
                Record record = this.mRecords.valueAt(i);
                if (UserHandle.getUserId(record.uid) == userId && record.channels.containsKey("miscellaneous")) {
                    record.channels.get("miscellaneous").setName(context.getResources().getString(17039922));
                }
            }
        }
    }

    public void onPackagesChanged(boolean removingPackage, int changeUserId, String[] pkgList, int[] uidList) {
        if (pkgList != null && pkgList.length != 0) {
            boolean updated = false;
            int i = 0;
            if (removingPackage) {
                int size = Math.min(pkgList.length, uidList.length);
                while (i < size) {
                    String pkg = pkgList[i];
                    int uid = uidList[i];
                    synchronized (this.mRecords) {
                        this.mRecords.remove(recordKey(pkg, uid));
                    }
                    this.mRestoredWithoutUids.remove(pkg);
                    updated = true;
                    i++;
                }
            } else {
                int length = pkgList.length;
                while (i < length) {
                    String pkg2 = pkgList[i];
                    Record r = this.mRestoredWithoutUids.get(pkg2);
                    if (r != null) {
                        try {
                            r.uid = this.mPm.getPackageUidAsUser(r.pkg, changeUserId);
                            this.mRestoredWithoutUids.remove(pkg2);
                            synchronized (this.mRecords) {
                                this.mRecords.put(recordKey(r.pkg, r.uid), r);
                            }
                            updated = true;
                        } catch (PackageManager.NameNotFoundException e) {
                        }
                    }
                    try {
                        Record fullRecord = getRecord(pkg2, this.mPm.getPackageUidAsUser(pkg2, changeUserId));
                        if (fullRecord != null) {
                            createDefaultChannelIfNeeded(fullRecord);
                            deleteDefaultChannelIfNeeded(fullRecord);
                        }
                    } catch (PackageManager.NameNotFoundException e2) {
                    }
                    i++;
                }
            }
            if (updated) {
                updateConfig();
            }
        }
    }

    private LogMaker getChannelLog(NotificationChannel channel, String pkg) {
        return new LogMaker(856).setType(6).setPackageName(pkg).addTaggedData(857, channel.getId()).addTaggedData(858, Integer.valueOf(channel.getImportance()));
    }

    private LogMaker getChannelGroupLog(String groupId, String pkg) {
        return new LogMaker(859).setType(6).addTaggedData(860, groupId).setPackageName(pkg);
    }

    public void updateBadgingEnabled() {
        if (this.mBadgingEnabled == null) {
            this.mBadgingEnabled = new SparseBooleanArray();
        }
        boolean changed = false;
        for (int index = 0; index < this.mBadgingEnabled.size(); index++) {
            int userId = this.mBadgingEnabled.keyAt(index);
            boolean oldValue = this.mBadgingEnabled.get(userId);
            boolean z = true;
            boolean newValue = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "notification_badging", 1, userId) != 0;
            this.mBadgingEnabled.put(userId, newValue);
            if (oldValue == newValue) {
                z = false;
            }
            changed |= z;
        }
        if (changed) {
            updateConfig();
        }
    }

    public boolean badgingEnabled(UserHandle userHandle) {
        int userId = userHandle.getIdentifier();
        boolean z = false;
        if (userId == -1) {
            return false;
        }
        if (this.mBadgingEnabled.indexOfKey(userId) < 0) {
            SparseBooleanArray sparseBooleanArray = this.mBadgingEnabled;
            if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "notification_badging", 1, userId) != 0) {
                z = true;
            }
            sparseBooleanArray.put(userId, z);
        }
        return this.mBadgingEnabled.get(userId, true);
    }

    public void setSysMgrCfgMap(ArrayList<NotificationSysMgrCfg> cfgList) {
        synchronized (this.mSysMgrCfgMap) {
            this.mSysMgrCfgMap.clear();
            if (cfgList == null) {
                Slog.w(TAG, "RankingHelper: setSysMgrCfgMap: get default channel cfg is null:");
                return;
            }
            int size = cfgList.size();
            for (int i = 0; i < size; i++) {
                NotificationSysMgrCfg cfg = cfgList.get(i);
                this.mSysMgrCfgMap.put(recordKey(cfg.smc_packageName, cfg.smc_userId), cfg);
            }
            Slog.d(TAG, "RankingHelper: setSysMgrCfgMap: get default channel cfg size:" + this.mSysMgrCfgMap.size());
        }
    }
}
