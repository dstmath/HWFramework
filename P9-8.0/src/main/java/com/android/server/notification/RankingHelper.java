package com.android.server.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.metrics.LogMaker;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.service.notification.NotificationListenerService.Ranking;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseBooleanArray;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.Preconditions;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.notification.NotificationManagerService.DumpFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class RankingHelper implements RankingConfig {
    private static final String ATT_ID = "id";
    private static final String ATT_IMPORTANCE = "importance";
    private static final String ATT_NAME = "name";
    private static final String ATT_PRIORITY = "priority";
    private static final String ATT_SHOW_BADGE = "show_badge";
    private static final String ATT_UID = "uid";
    private static final String ATT_VERSION = "version";
    private static final String ATT_VISIBILITY = "visibility";
    private static final int DEFAULT_IMPORTANCE = -1000;
    private static final int DEFAULT_PRIORITY = 0;
    private static final boolean DEFAULT_SHOW_BADGE = true;
    private static final int DEFAULT_VISIBILITY = -1000;
    private static final String TAG = "RankingHelper";
    private static final String TAG_CHANNEL = "channel";
    private static final String TAG_GROUP = "channelGroup";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_RANKING = "ranking";
    private static final int XML_VERSION = 1;
    private SparseBooleanArray mBadgingEnabled;
    private final Context mContext;
    private final GlobalSortKeyComparator mFinalComparator = new GlobalSortKeyComparator();
    private final PackageManager mPm;
    private final NotificationComparator mPreliminaryComparator;
    private final ArrayMap<String, NotificationRecord> mProxyByGroupTmp = new ArrayMap();
    private final RankingHandler mRankingHandler;
    private final ArrayMap<String, Record> mRecords = new ArrayMap();
    private final ArrayMap<String, Record> mRestoredWithoutUids = new ArrayMap();
    private final NotificationSignalExtractor[] mSignalExtractors;
    private final ArrayMap<String, NotificationSysMgrCfg> mSysMgrCfgMap = new ArrayMap();

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
        String pkg;
        int priority;
        boolean showBadge;
        int uid;
        int visibility;

        /* synthetic */ Record(Record -this0) {
            this();
        }

        private Record() {
            this.uid = UNKNOWN_UID;
            this.importance = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            this.priority = 0;
            this.visibility = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            this.showBadge = true;
            this.channels = new ArrayMap();
            this.groups = new ConcurrentHashMap();
        }
    }

    public RankingHelper(Context context, PackageManager pm, RankingHandler rankingHandler, NotificationUsageStats usageStats, String[] extractorNames) {
        this.mContext = context;
        this.mRankingHandler = rankingHandler;
        this.mPm = pm;
        this.mPreliminaryComparator = new NotificationComparator(this.mContext);
        updateBadgingEnabled();
        int N = extractorNames.length;
        this.mSignalExtractors = new NotificationSignalExtractor[N];
        for (int i = 0; i < N; i++) {
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
        if (parser.getEventType() == 2) {
            if (TAG_RANKING.equals(parser.getName())) {
                this.mRestoredWithoutUids.clear();
                while (true) {
                    int type = parser.next();
                    if (type != 1) {
                        String tag = parser.getName();
                        if (type != 3 || !TAG_RANKING.equals(tag)) {
                            if (type == 2 && "package".equals(tag)) {
                                int uid = safeInt(parser, ATT_UID, Record.UNKNOWN_UID);
                                String name = parser.getAttributeValue(null, ATT_NAME);
                                if (!TextUtils.isEmpty(name)) {
                                    if (forRestore) {
                                        try {
                                            uid = this.mPm.getPackageUidAsUser(name, 0);
                                        } catch (NameNotFoundException e) {
                                        }
                                    }
                                    Record r = getOrCreateRecord(name, uid, safeInt(parser, ATT_IMPORTANCE, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE), safeInt(parser, ATT_PRIORITY, 0), safeInt(parser, ATT_VISIBILITY, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE), safeBool(parser, ATT_SHOW_BADGE, true));
                                    r.importance = safeInt(parser, ATT_IMPORTANCE, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                                    r.priority = safeInt(parser, ATT_PRIORITY, 0);
                                    r.visibility = safeInt(parser, ATT_VISIBILITY, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                                    r.showBadge = safeBool(parser, ATT_SHOW_BADGE, true);
                                    int innerDepth = parser.getDepth();
                                    while (true) {
                                        type = parser.next();
                                        if (type == 1 || (type == 3 && parser.getDepth() <= innerDepth)) {
                                            try {
                                                deleteDefaultChannelIfNeeded(r);
                                                break;
                                            } catch (NameNotFoundException e2) {
                                                Slog.e(TAG, "deleteDefaultChannelIfNeeded - Exception: " + e2);
                                            }
                                        } else if (!(type == 3 || type == 4)) {
                                            String id;
                                            String tagName = parser.getName();
                                            if (TAG_GROUP.equals(tagName)) {
                                                id = parser.getAttributeValue(null, ATT_ID);
                                                CharSequence groupName = parser.getAttributeValue(null, ATT_NAME);
                                                if (!TextUtils.isEmpty(id)) {
                                                    r.groups.put(id, new NotificationChannelGroup(id, groupName));
                                                }
                                            }
                                            if (TAG_CHANNEL.equals(tagName)) {
                                                id = parser.getAttributeValue(null, ATT_ID);
                                                String channelName = parser.getAttributeValue(null, ATT_NAME);
                                                int channelImportance = safeInt(parser, ATT_IMPORTANCE, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                                                if (!(TextUtils.isEmpty(id) || (TextUtils.isEmpty(channelName) ^ 1) == 0)) {
                                                    NotificationChannel channel = new NotificationChannel(id, channelName, channelImportance);
                                                    channel.populateFromXml(parser);
                                                    r.channels.put(id, channel);
                                                }
                                            }
                                        }
                                    }
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
    }

    private static String recordKey(String pkg, int uid) {
        return pkg + "|" + uid;
    }

    private Record getRecord(String pkg, int uid) {
        Record record;
        String key = recordKey(pkg, uid);
        synchronized (this.mRecords) {
            record = (Record) this.mRecords.get(key);
        }
        return record;
    }

    private Record getOrCreateRecord(String pkg, int uid) {
        return getOrCreateRecord(pkg, uid, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE, 0, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE, true);
    }

    private Record getOrCreateRecord(String pkg, int uid, int importance, int priority, int visibility, boolean showBadge) {
        Record r;
        int i = 0;
        String key = recordKey(pkg, uid);
        synchronized (this.mRecords) {
            r = uid == Record.UNKNOWN_UID ? (Record) this.mRestoredWithoutUids.get(pkg) : (Record) this.mRecords.get(key);
            if (r == null) {
                r = new Record();
                r.pkg = pkg;
                r.uid = uid;
                r.importance = importance;
                r.priority = priority;
                r.visibility = visibility;
                r.showBadge = showBadge;
                NotificationSysMgrCfg cfg = (NotificationSysMgrCfg) this.mSysMgrCfgMap.get(key);
                if (cfg != null) {
                    boolean z;
                    r.importance = cfg.smc_importance;
                    r.visibility = cfg.smc_visilibity;
                    if (cfg.smc_iconBadge != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    r.showBadge = z;
                    if (cfg.smc_bypassDND != 0) {
                        i = 2;
                    }
                    r.priority = i;
                }
                try {
                    createDefaultChannelIfNeeded(r);
                } catch (NameNotFoundException e) {
                    Slog.e(TAG, "createDefaultChannelIfNeeded - Exception: " + e);
                }
                if (r.uid == Record.UNKNOWN_UID) {
                    this.mRestoredWithoutUids.put(pkg, r);
                } else {
                    this.mRecords.put(key, r);
                }
            }
        }
        return r;
    }

    private boolean shouldHaveDefaultChannel(Record r) throws NameNotFoundException {
        if (this.mPm.getApplicationInfoAsUser(r.pkg, 0, UserHandle.getUserId(r.uid)).targetSdkVersion >= 26) {
            return false;
        }
        return true;
    }

    private void deleteDefaultChannelIfNeeded(Record r) throws NameNotFoundException {
        if (r.channels.containsKey("miscellaneous") && !shouldHaveDefaultChannel(r)) {
            r.channels.remove("miscellaneous");
        }
    }

    private void createDefaultChannelIfNeeded(Record r) throws NameNotFoundException {
        boolean z = false;
        if (r.channels.containsKey("miscellaneous")) {
            ((NotificationChannel) r.channels.get("miscellaneous")).setName(this.mContext.getString(17039879));
        } else if (shouldHaveDefaultChannel(r)) {
            NotificationChannel channel = new NotificationChannel("miscellaneous", this.mContext.getString(17039879), r.importance);
            if (r.priority == 2) {
                z = true;
            }
            channel.setBypassDnd(z);
            channel.setLockscreenVisibility(r.visibility);
            if (r.importance != JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE) {
                channel.lockFields(4);
            }
            if (r.priority != 0) {
                channel.lockFields(1);
            }
            if (r.visibility != JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE) {
                channel.lockFields(2);
            }
            r.channels.put(channel.getId(), channel);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:65:0x0029 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0043  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void writeXml(XmlSerializer out, boolean forBackup) throws IOException {
        out.startTag(null, TAG_RANKING);
        out.attribute(null, ATT_VERSION, Integer.toString(1));
        synchronized (this.mRecords) {
            int N = this.mRecords.size();
            for (int i = 0; i < N; i++) {
                Record r = (Record) this.mRecords.valueAt(i);
                if (r != null && (!forBackup || UserHandle.getUserId(r.uid) == 0)) {
                    boolean hasNonDefaultSettings;
                    if (r.importance == JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE && r.priority == 0) {
                        if (r.visibility == JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE && r.showBadge && r.channels.size() <= 0) {
                            hasNonDefaultSettings = r.groups.size() > 0;
                            if (!hasNonDefaultSettings) {
                                out.startTag(null, "package");
                                out.attribute(null, ATT_NAME, r.pkg);
                                if (r.importance != JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE) {
                                    out.attribute(null, ATT_IMPORTANCE, Integer.toString(r.importance));
                                }
                                if (r.priority != 0) {
                                    out.attribute(null, ATT_PRIORITY, Integer.toString(r.priority));
                                }
                                if (r.visibility != JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE) {
                                    out.attribute(null, ATT_VISIBILITY, Integer.toString(r.visibility));
                                }
                                out.attribute(null, ATT_SHOW_BADGE, Boolean.toString(r.showBadge));
                                if (!forBackup) {
                                    out.attribute(null, ATT_UID, Integer.toString(r.uid));
                                }
                                for (NotificationChannelGroup group : r.groups.values()) {
                                    group.writeXml(out);
                                }
                                for (NotificationChannel channel : r.channels.values()) {
                                    if (channel != null && (!forBackup || (forBackup && (channel.isDeleted() ^ 1) != 0))) {
                                        channel.writeXml(out);
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
        out.endTag(null, TAG_RANKING);
    }

    private void updateConfig() {
        for (NotificationSignalExtractor config : this.mSignalExtractors) {
            config.setConfig(this);
        }
        this.mRankingHandler.requestSort(false);
    }

    public void sort(ArrayList<NotificationRecord> notificationList) {
        int i;
        int N = notificationList.size();
        for (i = N - 1; i >= 0; i--) {
            ((NotificationRecord) notificationList.get(i)).setGlobalSortKey(null);
        }
        Collections.sort(notificationList, this.mPreliminaryComparator);
        synchronized (this.mProxyByGroupTmp) {
            NotificationRecord record;
            for (i = N - 1; i >= 0; i--) {
                record = (NotificationRecord) notificationList.get(i);
                record.setAuthoritativeRank(i);
                String groupKey = record.getGroupKey();
                if (((NotificationRecord) this.mProxyByGroupTmp.get(groupKey)) == null) {
                    this.mProxyByGroupTmp.put(groupKey, record);
                }
            }
            for (i = 0; i < N; i++) {
                String groupSortKeyPortion;
                int authoritativeRank;
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
                char c = (!record.isRecentlyIntrusive() || record.getImportance() <= 1) ? '1' : '0';
                objArr[0] = Character.valueOf(c);
                if (groupProxy != null) {
                    authoritativeRank = groupProxy.getAuthoritativeRank();
                } else {
                    authoritativeRank = i;
                }
                objArr[1] = Integer.valueOf(authoritativeRank);
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

    private static boolean safeBool(XmlPullParser parser, String att, boolean defValue) {
        String value = parser.getAttributeValue(null, att);
        if (TextUtils.isEmpty(value)) {
            return defValue;
        }
        return Boolean.parseBoolean(value);
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

    public int getImportance(String packageName, int uid) {
        return getOrCreateRecord(packageName, uid).importance;
    }

    public boolean canShowBadge(String packageName, int uid) {
        return getOrCreateRecord(packageName, uid).showBadge;
    }

    public void setShowBadge(String packageName, int uid, boolean showBadge) {
        getOrCreateRecord(packageName, uid).showBadge = showBadge;
        updateConfig();
    }

    int getPackagePriority(String pkg, int uid) {
        return getOrCreateRecord(pkg, uid).priority;
    }

    int getPackageVisibility(String pkg, int uid) {
        return getOrCreateRecord(pkg, uid).visibility;
    }

    public void createNotificationChannelGroup(String pkg, int uid, NotificationChannelGroup group, boolean fromTargetApp) {
        Preconditions.checkNotNull(pkg);
        Preconditions.checkNotNull(group);
        Preconditions.checkNotNull(group.getId());
        Preconditions.checkNotNull(Boolean.valueOf(TextUtils.isEmpty(group.getName()) ^ 1));
        Record r = getOrCreateRecord(pkg, uid);
        if (r == null) {
            throw new IllegalArgumentException("Invalid package");
        }
        if (!group.equals((NotificationChannelGroup) r.groups.get(group.getId()))) {
            MetricsLogger.action(getChannelGroupLog(group.getId(), pkg));
        }
        r.groups.put(group.getId(), group);
        updateConfig();
    }

    public void createNotificationChannel(String pkg, int uid, NotificationChannel channel, boolean fromTargetApp) {
        Preconditions.checkNotNull(pkg);
        Preconditions.checkNotNull(channel);
        Preconditions.checkNotNull(channel.getId());
        Preconditions.checkArgument(TextUtils.isEmpty(channel.getName()) ^ 1);
        Record r = getOrCreateRecord(pkg, uid);
        if (r == null) {
            throw new IllegalArgumentException("Invalid package");
        } else if (channel.getGroup() != null && (r.groups.containsKey(channel.getGroup()) ^ 1) != 0) {
            throw new IllegalArgumentException("NotificationChannelGroup doesn't exist");
        } else if ("miscellaneous".equals(channel.getId())) {
            throw new IllegalArgumentException("Reserved id");
        } else {
            NotificationChannel existing = (NotificationChannel) r.channels.get(channel.getId());
            if (existing != null && fromTargetApp) {
                if (existing.isDeleted()) {
                    existing.setDeleted(false);
                    MetricsLogger.action(getChannelLog(channel, pkg).setType(1));
                }
                existing.setName(channel.getName().toString());
                existing.setDescription(channel.getDescription());
                existing.setBlockableSystem(channel.isBlockableSystem());
                updateConfig();
            } else if (channel.getImportance() < 0 || channel.getImportance() > 5) {
                throw new IllegalArgumentException("Invalid importance level");
            } else {
                if (fromTargetApp) {
                    channel.setBypassDnd(r.priority == 2);
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
                MetricsLogger.action(getChannelLog(channel, pkg).setType(1));
                updateConfig();
            }
        }
    }

    void clearLockedFields(NotificationChannel channel) {
        channel.unlockFields(channel.getUserLockedFields());
    }

    public void updateNotificationChannel(String pkg, int uid, NotificationChannel updatedChannel, boolean fromUser) {
        Preconditions.checkNotNull(updatedChannel);
        Preconditions.checkNotNull(updatedChannel.getId());
        Record r = getOrCreateRecord(pkg, uid);
        if (r == null) {
            throw new IllegalArgumentException("Invalid package");
        }
        NotificationChannel channel = (NotificationChannel) r.channels.get(updatedChannel.getId());
        if (channel == null || channel.isDeleted()) {
            throw new IllegalArgumentException("Channel does not exist");
        }
        if (updatedChannel.getLockscreenVisibility() == 1) {
            updatedChannel.setLockscreenVisibility(JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
        }
        updatedChannel.unlockFields(updatedChannel.getUserLockedFields());
        updatedChannel.lockFields(channel.getUserLockedFields());
        if (fromUser) {
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
        updateConfig();
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
        NotificationChannel nc = (NotificationChannel) r.channels.get(channelId);
        if (nc == null || (!includeDeleted && (nc.isDeleted() ^ 1) == 0)) {
            return null;
        }
        return nc;
    }

    public void deleteNotificationChannel(String pkg, int uid, String channelId) {
        Record r = getRecord(pkg, uid);
        if (r != null) {
            NotificationChannel channel = (NotificationChannel) r.channels.get(channelId);
            if (channel != null) {
                channel.setDeleted(true);
                LogMaker lm = getChannelLog(channel, pkg);
                lm.setType(2);
                MetricsLogger.action(lm);
                updateConfig();
            }
        }
    }

    public void permanentlyDeleteNotificationChannel(String pkg, int uid, String channelId) {
        Preconditions.checkNotNull(pkg);
        Preconditions.checkNotNull(channelId);
        Record r = getRecord(pkg, uid);
        if (r != null) {
            r.channels.remove(channelId);
            updateConfig();
        }
    }

    public void permanentlyDeleteNotificationChannels(String pkg, int uid) {
        Preconditions.checkNotNull(pkg);
        Record r = getRecord(pkg, uid);
        if (r != null) {
            for (int i = r.channels.size() - 1; i >= 0; i--) {
                String key = (String) r.channels.keyAt(i);
                if (!"miscellaneous".equals(key)) {
                    r.channels.remove(key);
                }
            }
            updateConfig();
        }
    }

    public NotificationChannelGroup getNotificationChannelGroup(String groupId, String pkg, int uid) {
        Preconditions.checkNotNull(pkg);
        return (NotificationChannelGroup) getRecord(pkg, uid).groups.get(groupId);
    }

    public ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroups(String pkg, int uid, boolean includeDeleted) {
        Preconditions.checkNotNull(pkg);
        Map<String, NotificationChannelGroup> groups = new ArrayMap();
        Record r = getRecord(pkg, uid);
        if (r == null) {
            return ParceledListSlice.emptyList();
        }
        NotificationChannelGroup nonGrouped = new NotificationChannelGroup(null, null);
        int N = r.channels.size();
        for (int i = 0; i < N; i++) {
            NotificationChannel nc = (NotificationChannel) r.channels.valueAt(i);
            if (includeDeleted || (nc.isDeleted() ^ 1) != 0) {
                if (nc.getGroup() == null) {
                    nonGrouped.addChannel(nc);
                } else if (r.groups.get(nc.getGroup()) != null) {
                    NotificationChannelGroup ncg = (NotificationChannelGroup) groups.get(nc.getGroup());
                    if (ncg == null) {
                        ncg = ((NotificationChannelGroup) r.groups.get(nc.getGroup())).clone();
                        groups.put(nc.getGroup(), ncg);
                    }
                    ncg.addChannel(nc);
                }
            }
        }
        if (nonGrouped.getChannels().size() > 0) {
            groups.put(null, nonGrouped);
        }
        return new ParceledListSlice(new ArrayList(groups.values()));
    }

    public List<NotificationChannel> deleteNotificationChannelGroup(String pkg, int uid, String groupId) {
        List<NotificationChannel> deletedChannels = new ArrayList();
        Record r = getRecord(pkg, uid);
        if (r == null || TextUtils.isEmpty(groupId)) {
            return deletedChannels;
        }
        r.groups.remove(groupId);
        int N = r.channels.size();
        for (int i = 0; i < N; i++) {
            NotificationChannel nc = (NotificationChannel) r.channels.valueAt(i);
            if (groupId.equals(nc.getGroup())) {
                nc.setDeleted(true);
                deletedChannels.add(nc);
            }
        }
        updateConfig();
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
        List<NotificationChannel> channels = new ArrayList();
        Record r = getRecord(pkg, uid);
        if (r == null) {
            return ParceledListSlice.emptyList();
        }
        int N = r.channels.size();
        for (int i = 0; i < N; i++) {
            NotificationChannel nc = (NotificationChannel) r.channels.valueAt(i);
            if (includeDeleted || (nc.isDeleted() ^ 1) != 0) {
                channels.add(nc);
            }
        }
        return new ParceledListSlice(channels);
    }

    public boolean onlyHasDefaultChannel(String pkg, int uid) {
        Record r = getOrCreateRecord(pkg, uid);
        if (r.channels.size() == 1 && r.channels.containsKey("miscellaneous")) {
            return true;
        }
        return false;
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
            if (((NotificationChannel) r.channels.valueAt(i)).isDeleted()) {
                deletedCount++;
            }
        }
        return deletedCount;
    }

    public void setImportance(String pkgName, int uid, int importance) {
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

    void lockFieldsForUpdate(NotificationChannel original, NotificationChannel update) {
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
        if (!(Arrays.equals(original.getVibrationPattern(), update.getVibrationPattern()) && original.shouldVibrate() == update.shouldVibrate())) {
            update.lockFields(16);
        }
        if (original.canShowBadge() != update.canShowBadge()) {
            update.lockFields(128);
        }
    }

    public void dump(PrintWriter pw, String prefix, DumpFilter filter) {
        if (filter == null) {
            pw.print(prefix);
            pw.print("mSignalExtractors.length = ");
            pw.println(N);
            for (Object println : this.mSignalExtractors) {
                pw.print(prefix);
                pw.print("  ");
                pw.println(println);
            }
        }
        if (filter == null) {
            pw.print(prefix);
            pw.println("per-package config:");
        }
        pw.println("Records:");
        synchronized (this.mRecords) {
            dumpRecords(pw, prefix, filter, this.mRecords);
        }
        pw.println("Restored without uid:");
        dumpRecords(pw, prefix, filter, this.mRestoredWithoutUids);
    }

    private static void dumpRecords(PrintWriter pw, String prefix, DumpFilter filter, ArrayMap<String, Record> records) {
        int N = records.size();
        for (int i = 0; i < N; i++) {
            Record r = (Record) records.valueAt(i);
            if (filter == null || filter.matches(r.pkg)) {
                String str;
                pw.print(prefix);
                pw.print("  AppSettings: ");
                pw.print(r.pkg);
                pw.print(" (");
                if (r.uid == Record.UNKNOWN_UID) {
                    str = "UNKNOWN_UID";
                } else {
                    str = Integer.toString(r.uid);
                }
                pw.print(str);
                pw.print(')');
                if (r.importance != JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE) {
                    pw.print(" importance=");
                    pw.print(Ranking.importanceToString(r.importance));
                }
                if (r.priority != 0) {
                    pw.print(" priority=");
                    pw.print(Notification.priorityToString(r.priority));
                }
                if (r.visibility != JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE) {
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

    public JSONObject dumpJson(DumpFilter filter) {
        JSONObject ranking = new JSONObject();
        JSONArray records = new JSONArray();
        try {
            ranking.put("noUid", this.mRestoredWithoutUids.size());
        } catch (JSONException e) {
        }
        synchronized (this.mRecords) {
            int N = this.mRecords.size();
            for (int i = 0; i < N; i++) {
                Record r = (Record) this.mRecords.valueAt(i);
                if (filter == null || filter.matches(r.pkg)) {
                    JSONObject record = new JSONObject();
                    try {
                        record.put("userId", UserHandle.getUserId(r.uid));
                        record.put("packageName", r.pkg);
                        if (r.importance != JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE) {
                            record.put(ATT_IMPORTANCE, Ranking.importanceToString(r.importance));
                        }
                        if (r.priority != 0) {
                            record.put(ATT_PRIORITY, Notification.priorityToString(r.priority));
                        }
                        if (r.visibility != JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE) {
                            record.put(ATT_VISIBILITY, Notification.visibilityToString(r.visibility));
                        }
                        if (!r.showBadge) {
                            record.put("showBadge", Boolean.valueOf(r.showBadge));
                        }
                        for (NotificationChannel channel : r.channels.values()) {
                            record.put(TAG_CHANNEL, channel.toJson());
                        }
                        for (NotificationChannelGroup group : r.groups.values()) {
                            record.put("group", group.toJson());
                        }
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
        ArrayMap<Integer, String> packageBans;
        synchronized (this.mRecords) {
            int N = this.mRecords.size();
            packageBans = new ArrayMap(N);
            for (int i = 0; i < N; i++) {
                Record r = (Record) this.mRecords.valueAt(i);
                if (r.importance == 0) {
                    packageBans.put(Integer.valueOf(r.uid), r.pkg);
                }
            }
        }
        return packageBans;
    }

    public JSONArray dumpChannelsJson(DumpFilter filter) {
        JSONArray channels = new JSONArray();
        for (Entry<String, Integer> channelCount : getPackageChannels().entrySet()) {
            String packageName = (String) channelCount.getKey();
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
        ArrayMap<String, Integer> packageChannels = new ArrayMap();
        synchronized (this.mRecords) {
            for (int i = 0; i < this.mRecords.size(); i++) {
                Record r = (Record) this.mRecords.valueAt(i);
                int channelCount = 0;
                for (int j = 0; j < r.channels.size(); j++) {
                    if (!((NotificationChannel) r.channels.valueAt(j)).isDeleted()) {
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
                if (UserHandle.getUserId(((Record) this.mRecords.valueAt(i)).uid) == userId) {
                    this.mRecords.removeAt(i);
                }
            }
        }
    }

    public void onPackagesChanged(boolean removingPackage, int changeUserId, String[] pkgList, int[] uidList) {
        if (pkgList != null && pkgList.length != 0) {
            boolean updated = false;
            String pkg;
            if (removingPackage) {
                int size = Math.min(pkgList.length, uidList.length);
                for (int i = 0; i < size; i++) {
                    pkg = pkgList[i];
                    synchronized (this.mRecords) {
                        this.mRecords.remove(recordKey(pkg, uidList[i]));
                    }
                    this.mRestoredWithoutUids.remove(pkg);
                    updated = true;
                }
            } else {
                for (String pkg2 : pkgList) {
                    Record r = (Record) this.mRestoredWithoutUids.get(pkg2);
                    if (r != null) {
                        try {
                            r.uid = this.mPm.getPackageUidAsUser(r.pkg, changeUserId);
                            this.mRestoredWithoutUids.remove(pkg2);
                            synchronized (this.mRecords) {
                                this.mRecords.put(recordKey(r.pkg, r.uid), r);
                            }
                            updated = true;
                        } catch (NameNotFoundException e) {
                        }
                    }
                    try {
                        Record fullRecord = getRecord(pkg2, this.mPm.getPackageUidAsUser(pkg2, changeUserId));
                        if (fullRecord != null) {
                            createDefaultChannelIfNeeded(fullRecord);
                            deleteDefaultChannelIfNeeded(fullRecord);
                        }
                    } catch (NameNotFoundException e2) {
                    }
                }
            }
            if (updated) {
                updateConfig();
            }
        }
    }

    private LogMaker getChannelLog(NotificationChannel channel, String pkg) {
        return new LogMaker(VoldResponseCode.VOLUME_LOWSPEED_SPEC_SD).setType(6).setPackageName(pkg).addTaggedData(857, channel.getId()).addTaggedData(858, Integer.valueOf(channel.getImportance()));
    }

    private LogMaker getChannelGroupLog(String groupId, String pkg) {
        return new LogMaker(859).setType(6).addTaggedData(860, groupId).setPackageName(pkg);
    }

    public void updateBadgingEnabled() {
        if (this.mBadgingEnabled == null) {
            this.mBadgingEnabled = new SparseBooleanArray();
        }
        int changed = 0;
        for (int index = 0; index < this.mBadgingEnabled.size(); index++) {
            int i;
            int userId = this.mBadgingEnabled.keyAt(index);
            boolean oldValue = this.mBadgingEnabled.get(userId);
            boolean newValue = Secure.getIntForUser(this.mContext.getContentResolver(), "notification_badging", 1, userId) != 0;
            this.mBadgingEnabled.put(userId, newValue);
            if (oldValue != newValue) {
                i = 1;
            } else {
                i = 0;
            }
            changed |= i;
        }
        if (changed != 0) {
            this.mRankingHandler.requestSort(false);
        }
    }

    public boolean badgingEnabled(UserHandle userHandle) {
        boolean z = false;
        int userId = userHandle.getIdentifier();
        if (userId == -1) {
            return false;
        }
        if (this.mBadgingEnabled.indexOfKey(userId) < 0) {
            SparseBooleanArray sparseBooleanArray = this.mBadgingEnabled;
            if (Secure.getIntForUser(this.mContext.getContentResolver(), "notification_badging", 1, userId) != 0) {
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
                NotificationSysMgrCfg cfg = (NotificationSysMgrCfg) cfgList.get(i);
                this.mSysMgrCfgMap.put(recordKey(cfg.smc_packageName, cfg.smc_userId), cfg);
            }
            Slog.d(TAG, "RankingHelper: setSysMgrCfgMap: get default channel cfg size:" + this.mSysMgrCfgMap.size());
        }
    }
}
