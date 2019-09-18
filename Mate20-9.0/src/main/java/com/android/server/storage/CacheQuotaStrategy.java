package com.android.server.storage;

import android.app.usage.CacheQuotaHint;
import android.app.usage.ICacheQuotaService;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManagerInternal;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.ArrayMap;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseLongArray;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.AtomicFile;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.server.pm.Installer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class CacheQuotaStrategy implements RemoteCallback.OnResultListener {
    private static final String ATTR_PREVIOUS_BYTES = "previousBytes";
    private static final String ATTR_QUOTA_IN_BYTES = "bytes";
    private static final String ATTR_UID = "uid";
    private static final String ATTR_UUID = "uuid";
    private static final String CACHE_INFO_TAG = "cache-info";
    private static final String TAG = "CacheQuotaStrategy";
    private static final String TAG_QUOTA = "quota";
    private final Context mContext;
    private final Installer mInstaller;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private AtomicFile mPreviousValuesFile;
    private final ArrayMap<String, SparseLongArray> mQuotaMap;
    /* access modifiers changed from: private */
    public ICacheQuotaService mRemoteService;
    private ServiceConnection mServiceConnection;
    private final UsageStatsManagerInternal mUsageStats;

    public CacheQuotaStrategy(Context context, UsageStatsManagerInternal usageStatsManager, Installer installer, ArrayMap<String, SparseLongArray> quotaMap) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mUsageStats = (UsageStatsManagerInternal) Preconditions.checkNotNull(usageStatsManager);
        this.mInstaller = (Installer) Preconditions.checkNotNull(installer);
        this.mQuotaMap = (ArrayMap) Preconditions.checkNotNull(quotaMap);
        this.mPreviousValuesFile = new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), "cachequota.xml"));
    }

    public void recalculateQuotas() {
        createServiceConnection();
        ComponentName component = getServiceComponentName();
        if (component != null) {
            Intent intent = new Intent();
            intent.setComponent(component);
            this.mContext.bindServiceAsUser(intent, this.mServiceConnection, 1, UserHandle.CURRENT);
        }
    }

    private void createServiceConnection() {
        if (this.mServiceConnection == null) {
            this.mServiceConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName name, final IBinder service) {
                    AsyncTask.execute(new Runnable() {
                        public void run() {
                            synchronized (CacheQuotaStrategy.this.mLock) {
                                ICacheQuotaService unused = CacheQuotaStrategy.this.mRemoteService = ICacheQuotaService.Stub.asInterface(service);
                                List<CacheQuotaHint> requests = CacheQuotaStrategy.this.getUnfulfilledRequests();
                                try {
                                    CacheQuotaStrategy.this.mRemoteService.computeCacheQuotaHints(new RemoteCallback(CacheQuotaStrategy.this), requests);
                                } catch (RemoteException ex) {
                                    Slog.w(CacheQuotaStrategy.TAG, "Remote exception occurred while trying to get cache quota", ex);
                                }
                            }
                        }
                    });
                }

                public void onServiceDisconnected(ComponentName name) {
                    synchronized (CacheQuotaStrategy.this.mLock) {
                        ICacheQuotaService unused = CacheQuotaStrategy.this.mRemoteService = null;
                    }
                }
            };
        }
    }

    /* access modifiers changed from: private */
    public List<CacheQuotaHint> getUnfulfilledRequests() {
        PackageManager packageManager;
        CacheQuotaStrategy cacheQuotaStrategy = this;
        long timeNow = System.currentTimeMillis();
        long oneYearAgo = timeNow - 31449600000L;
        List<CacheQuotaHint> requests = new ArrayList<>();
        List<UserInfo> users = ((UserManager) cacheQuotaStrategy.mContext.getSystemService(UserManager.class)).getUsers();
        int userCount = users.size();
        PackageManager packageManager2 = cacheQuotaStrategy.mContext.getPackageManager();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < userCount) {
                UserInfo info = users.get(i2);
                UserInfo info2 = info;
                int i3 = i2;
                int userCount2 = userCount;
                PackageManager packageManager3 = packageManager2;
                List<UserInfo> users2 = users;
                List<UsageStats> stats = cacheQuotaStrategy.mUsageStats.queryUsageStatsForUser(info.id, 4, oneYearAgo, timeNow, false);
                if (stats != null) {
                    for (UsageStats stat : stats) {
                        UserInfo info3 = info2;
                        try {
                            packageManager = packageManager3;
                            try {
                                ApplicationInfo appInfo = packageManager.getApplicationInfoAsUser(stat.getPackageName(), 0, info3.id);
                                ApplicationInfo applicationInfo = appInfo;
                                requests.add(new CacheQuotaHint.Builder().setVolumeUuid(appInfo.volumeUuid).setUid(appInfo.uid).setUsageStats(stat).setQuota(-1).build());
                            } catch (PackageManager.NameNotFoundException e) {
                            }
                        } catch (PackageManager.NameNotFoundException e2) {
                            packageManager = packageManager3;
                        }
                        info2 = info3;
                        packageManager3 = packageManager;
                    }
                }
                i = i3 + 1;
                packageManager2 = packageManager3;
                userCount = userCount2;
                users = users2;
                cacheQuotaStrategy = this;
            } else {
                PackageManager packageManager4 = packageManager2;
                List<UserInfo> list = users;
                return requests;
            }
        }
    }

    public void onResult(Bundle data) {
        List<CacheQuotaHint> processedRequests = data.getParcelableArrayList("requests");
        pushProcessedQuotas(processedRequests);
        writeXmlToFile(processedRequests);
    }

    private void pushProcessedQuotas(List<CacheQuotaHint> processedRequests) {
        int requestSize = processedRequests.size();
        for (int i = 0; i < requestSize; i++) {
            CacheQuotaHint request = processedRequests.get(i);
            long proposedQuota = request.getQuota();
            if (proposedQuota != -1) {
                try {
                    int uid = request.getUid();
                    this.mInstaller.setAppQuota(request.getVolumeUuid(), UserHandle.getUserId(uid), UserHandle.getAppId(uid), proposedQuota);
                    insertIntoQuotaMap(request.getVolumeUuid(), UserHandle.getUserId(uid), UserHandle.getAppId(uid), proposedQuota);
                } catch (Installer.InstallerException ex) {
                    Slog.w(TAG, "Failed to set cache quota for " + request.getUid(), ex);
                }
            }
        }
        disconnectService();
    }

    private void insertIntoQuotaMap(String volumeUuid, int userId, int appId, long quota) {
        SparseLongArray volumeMap = this.mQuotaMap.get(volumeUuid);
        if (volumeMap == null) {
            volumeMap = new SparseLongArray();
            this.mQuotaMap.put(volumeUuid, volumeMap);
        }
        volumeMap.put(UserHandle.getUid(userId, appId), quota);
    }

    private void disconnectService() {
        if (this.mServiceConnection != null) {
            this.mContext.unbindService(this.mServiceConnection);
            this.mServiceConnection = null;
        }
    }

    private ComponentName getServiceComponentName() {
        String packageName = this.mContext.getPackageManager().getServicesSystemSharedLibraryPackageName();
        if (packageName == null) {
            Slog.w(TAG, "could not access the cache quota service: no package!");
            return null;
        }
        Intent intent = new Intent("android.app.usage.CacheQuotaService");
        intent.setPackage(packageName);
        ResolveInfo resolveInfo = this.mContext.getPackageManager().resolveService(intent, 132);
        if (resolveInfo == null || resolveInfo.serviceInfo == null) {
            Slog.w(TAG, "No valid components found.");
            return null;
        }
        ServiceInfo serviceInfo = resolveInfo.serviceInfo;
        return new ComponentName(serviceInfo.packageName, serviceInfo.name);
    }

    private void writeXmlToFile(List<CacheQuotaHint> processedRequests) {
        FileOutputStream fileStream = null;
        try {
            XmlSerializer out = new FastXmlSerializer();
            fileStream = this.mPreviousValuesFile.startWrite();
            out.setOutput(fileStream, StandardCharsets.UTF_8.name());
            saveToXml(out, processedRequests, 0);
            this.mPreviousValuesFile.finishWrite(fileStream);
        } catch (Exception e) {
            Slog.e(TAG, "An error occurred while writing the cache quota file.", e);
            this.mPreviousValuesFile.failWrite(fileStream);
        }
    }

    public long setupQuotasFromFile() throws IOException {
        try {
            try {
                Pair<Long, List<CacheQuotaHint>> cachedValues = readFromXml(this.mPreviousValuesFile.openRead());
                if (cachedValues == null) {
                    Slog.e(TAG, "An error occurred while parsing the cache quota file.");
                    return -1;
                }
                pushProcessedQuotas((List) cachedValues.second);
                return ((Long) cachedValues.first).longValue();
            } catch (XmlPullParserException e) {
                throw new IllegalStateException(e.getMessage());
            }
        } catch (FileNotFoundException e2) {
            return -1;
        }
    }

    @VisibleForTesting
    static void saveToXml(XmlSerializer out, List<CacheQuotaHint> requests, long bytesWhenCalculated) throws IOException {
        out.startDocument(null, true);
        out.startTag(null, CACHE_INFO_TAG);
        int requestSize = requests.size();
        out.attribute(null, ATTR_PREVIOUS_BYTES, Long.toString(bytesWhenCalculated));
        for (int i = 0; i < requestSize; i++) {
            CacheQuotaHint request = requests.get(i);
            out.startTag(null, TAG_QUOTA);
            if (request.getVolumeUuid() != null) {
                out.attribute(null, ATTR_UUID, request.getVolumeUuid());
            }
            out.attribute(null, "uid", Integer.toString(request.getUid()));
            out.attribute(null, ATTR_QUOTA_IN_BYTES, Long.toString(request.getQuota()));
            out.endTag(null, TAG_QUOTA);
        }
        out.endTag(null, CACHE_INFO_TAG);
        out.endDocument();
    }

    protected static Pair<Long, List<CacheQuotaHint>> readFromXml(InputStream inputStream) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, StandardCharsets.UTF_8.name());
        int eventType = parser.getEventType();
        while (eventType != 2 && eventType != 1) {
            eventType = parser.next();
        }
        if (eventType == 1) {
            Slog.d(TAG, "No quotas found in quota file.");
            return null;
        } else if (CACHE_INFO_TAG.equals(parser.getName())) {
            List<CacheQuotaHint> quotas = new ArrayList<>();
            try {
                long previousBytes = Long.parseLong(parser.getAttributeValue(null, ATTR_PREVIOUS_BYTES));
                int eventType2 = parser.next();
                do {
                    if (eventType2 == 2 && TAG_QUOTA.equals(parser.getName())) {
                        CacheQuotaHint request = getRequestFromXml(parser);
                        if (request == null) {
                            continue;
                        } else {
                            quotas.add(request);
                        }
                    }
                    eventType2 = parser.next();
                    continue;
                } while (eventType2 != 1);
                return new Pair<>(Long.valueOf(previousBytes), quotas);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Previous bytes formatted incorrectly; aborting quota read.");
            }
        } else {
            throw new IllegalStateException("Invalid starting tag.");
        }
    }

    @VisibleForTesting
    static CacheQuotaHint getRequestFromXml(XmlPullParser parser) {
        try {
            String uuid = parser.getAttributeValue(null, ATTR_UUID);
            int uid = Integer.parseInt(parser.getAttributeValue(null, "uid"));
            return new CacheQuotaHint.Builder().setVolumeUuid(uuid).setUid(uid).setQuota(Long.parseLong(parser.getAttributeValue(null, ATTR_QUOTA_IN_BYTES))).build();
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Invalid cache quota request, skipping.");
            return null;
        }
    }
}
