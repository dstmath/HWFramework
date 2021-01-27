package com.huawei.security.dpermission.monitor;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.security.dpermission.DPermissionInitializer;
import com.huawei.security.dpermission.model.ReportInfo;
import com.huawei.security.dpermission.service.HwDPermissionService;
import com.huawei.security.dpermission.utils.DangerousPermissionDataHelper;
import com.huawei.security.dpermission.utils.PermissionUtil;
import huawei.hiview.HiEvent;
import huawei.hiview.HiView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.javax.xml.parsers.SAXParser;
import ohos.javax.xml.parsers.SAXParserFactory;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.helpers.DefaultHandler;

public class DangerousPermissionTrackerReceiver extends BroadcastReceiver {
    public static final String ACTION_REPORT_STATS = "com.huawei.security.dpermission.monitor.REPORT_REQUIRED_DANGEROUS_PERMISSION_STATS";
    public static final String ACTION_UPDATE_STATS = "com.huawei.security.dpermission.monitor.UPDATE_REQUIRED_DANGEROUS_PERMISSION_STATS";
    private static final long DEFAULT_FOREGROUND_TIME = 28800000;
    private static final int DEFAULT_LIST_LENGTH = 10;
    private static final int DEFAULT_REPORT_NUM = 10;
    private static final int DEFAULT_SET_LENGTH = 16;
    private static final HiLogLabel DPERMISSION_LABEL = new HiLogLabel(3, (int) DPermissionInitializer.DPERMISSION_LOG_ID, "DangerousPermissionTrackerReceiver");
    private static final int ERROR_TYPE = 3;
    private static final int EVT_PACKAGE_REPORT = 1;
    private static final int EVT_TRACKER_UPDATE = 0;
    public static final String INTENT_SET_HOUR = "HOUR";
    public static final String INTENT_SET_MINUTE = "MINUTE";
    public static final String INTENT_SET_REPORT_NUM = "REPORT_NUM";
    private static final String KEY_ERROR_TYPE = "violationType";
    private static final String KEY_PACKAGE_NAME = "pkgName";
    private static final String KEY_PERMISSION_NAME = "permName";
    private static final String KEY_VERSION = "version";
    private static final int MAX_PACKAGE_NAME_LENGTH = 64;
    private static final int MAX_PERMISSION_NAME_LENGTH = 32;
    private static final int MAX_VERSION_LENGTH = 32;
    private static final long ONE_HOUR = 3600000;
    private static final long ONE_MINUTE = 60000;
    private static final int REPORT_CODE = 940002009;
    private static final Set<String> REPORT_PERMISSION_NAMES = PermissionUtil.getReportPermissionNames();
    private static final String UNINSTALLED_DELAPP_DIR = "/data/system";
    private static final String UNINSTALLED_DELAPP_FILE = "uninstalled_delapp.xml";
    private final Context mContext;
    private Handler mHandler;
    private long setForegroundTime;
    private int setReportNum;

    public DangerousPermissionTrackerReceiver(Context context) {
        this.mContext = context;
        HandlerThread handlerThread = new HandlerThread("DangerousPermissionTrackerReceiver");
        handlerThread.start();
        if (handlerThread.getLooper() != null) {
            this.mHandler = new TrackerEventChangedHandler(handlerThread.getLooper());
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                HiLog.warn(DPERMISSION_LABEL, "onReceive : Invalid action.", new Object[0]);
                return;
            }
            HiLog.debug(DPERMISSION_LABEL, "onReceive: Action = %{public}s.", new Object[]{action});
            char c = 65535;
            int hashCode = action.hashCode();
            if (hashCode != -1016384303) {
                if (hashCode == 436123580 && action.equals(ACTION_REPORT_STATS)) {
                    c = 0;
                }
            } else if (action.equals(ACTION_UPDATE_STATS)) {
                c = 1;
            }
            if (c == 0) {
                Message.obtain(this.mHandler, 1, intent).sendToTarget();
            } else if (c == 1) {
                Message.obtain(this.mHandler, 0, intent).sendToTarget();
            }
        }
    }

    public void register() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_REPORT_STATS);
        intentFilter.addAction(ACTION_UPDATE_STATS);
        ContextEx.registerReceiverAsUser(this.mContext, this, UserHandleEx.OWNER, intentFilter, HwDPermissionService.MANAGE_DISTRIBUTED_PERMISSION, (Handler) null);
    }

    private class TrackerEventChangedHandler extends Handler {
        TrackerEventChangedHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i != 0) {
                if (i == 1 && (message.obj instanceof Intent)) {
                    DangerousPermissionTrackerReceiver.this.setReportNum((Intent) message.obj);
                    DangerousPermissionTrackerReceiver.this.handleTrackerReport();
                }
            } else if (message.obj instanceof Intent) {
                DangerousPermissionTrackerReceiver.this.setForegroundTime((Intent) message.obj);
                DangerousPermissionTrackerReceiver.this.handleTrackerUpdate();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setForegroundTime(Intent intent) {
        int intExtra = intent.getIntExtra(INTENT_SET_HOUR, 0);
        int intExtra2 = intent.getIntExtra(INTENT_SET_MINUTE, 0);
        if (intExtra2 > 0 || intExtra > 0) {
            this.setForegroundTime = (((long) intExtra) * ONE_HOUR) + (((long) intExtra2) * ONE_MINUTE);
            HiLog.debug(DPERMISSION_LABEL, "report using set foreground time %{public}d.", new Object[]{Long.valueOf(this.setForegroundTime)});
            return;
        }
        this.setForegroundTime = DEFAULT_FOREGROUND_TIME;
        HiLog.debug(DPERMISSION_LABEL, "report using default foreground time %{public}d.", new Object[]{Long.valueOf(this.setForegroundTime)});
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setReportNum(Intent intent) {
        this.setReportNum = intent.getIntExtra(INTENT_SET_REPORT_NUM, 10);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTrackerUpdate() {
        List<PackageInfo> installedPackages = this.mContext.getPackageManager().getInstalledPackages(4096);
        HiLog.debug(DPERMISSION_LABEL, "get all packages number: %{public}d", new Object[]{Integer.valueOf(installedPackages.size())});
        List<String> loadReportedPackages = DangerousPermissionDataHelper.loadReportedPackages(this.mContext);
        HiLog.debug(DPERMISSION_LABEL, "get reported packages number: %{public}d.", new Object[]{Integer.valueOf(loadReportedPackages.size())});
        Set<String> unInstalledSystemApps = getUnInstalledSystemApps();
        HiLog.debug(DPERMISSION_LABEL, "get uninstalled packages number: %{public}d.", new Object[]{Integer.valueOf(unInstalledSystemApps.size())});
        ArrayList arrayList = new ArrayList(installedPackages.size());
        for (PackageInfo packageInfo : installedPackages) {
            String str = packageInfo.packageName;
            if (!isSystemApp(packageInfo) && !unInstalledSystemApps.contains(str) && !loadReportedPackages.contains(str)) {
                List<String> nonGrantedDangerousPermission = getNonGrantedDangerousPermission(packageInfo);
                if (nonGrantedDangerousPermission.size() > 0 && isWorkingFine(packageInfo)) {
                    HiLog.debug(DPERMISSION_LABEL, "package %{public}s needs to be reported.", new Object[]{str});
                    for (String str2 : nonGrantedDangerousPermission) {
                        arrayList.add(new ReportInfo(str, packageInfo.versionName, str2));
                    }
                }
            }
        }
        HiLog.info(DPERMISSION_LABEL, "update local data success: %{public}b.", new Object[]{Boolean.valueOf(DangerousPermissionDataHelper.updateReportInfos(this.mContext, arrayList))});
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTrackerReport() {
        List<ReportInfo> loadReportInfos = DangerousPermissionDataHelper.loadReportInfos(this.mContext, this.setReportNum);
        HiLog.debug(DPERMISSION_LABEL, "get %{public}d info to report.", new Object[]{Integer.valueOf(loadReportInfos.size())});
        for (ReportInfo reportInfo : loadReportInfos) {
            if (reportInfo == null || !reportInfo.checkIsValid()) {
                HiLog.error(DPERMISSION_LABEL, "get invalid report info", new Object[0]);
            } else {
                report(reportInfo);
            }
        }
    }

    private List<String> getNonGrantedDangerousPermission(PackageInfo packageInfo) {
        String str = packageInfo.packageName;
        String[] strArr = packageInfo.requestedPermissions;
        int[] iArr = packageInfo.requestedPermissionsFlags;
        if (strArr == null || iArr == null) {
            HiLog.debug(DPERMISSION_LABEL, "%{public}s require no permissions.", new Object[]{str});
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList(10);
        for (int i = 0; i < strArr.length; i++) {
            if ((iArr[i] & 2) == 0 && isDangerous(strArr[i])) {
                arrayList.add(strArr[i]);
            }
        }
        return arrayList;
    }

    private boolean isWorkingFine(PackageInfo packageInfo) {
        return getAppTime(packageInfo.packageName, packageInfo.lastUpdateTime, System.currentTimeMillis()) > this.setForegroundTime;
    }

    private boolean isDangerous(String str) {
        return REPORT_PERMISSION_NAMES.contains(str);
    }

    private long getAppTime(String str, long j, long j2) {
        UsageStats usageStats;
        Object systemService = this.mContext.getSystemService("usagestats");
        Map<String, UsageStats> queryAndAggregateUsageStats = systemService instanceof UsageStatsManager ? ((UsageStatsManager) systemService).queryAndAggregateUsageStats(j, j2) : null;
        if (queryAndAggregateUsageStats == null || queryAndAggregateUsageStats.isEmpty() || (usageStats = queryAndAggregateUsageStats.get(str)) == null) {
            return 0;
        }
        return usageStats.getTotalTimeInForeground();
    }

    private boolean isSystemApp(PackageInfo packageInfo) {
        return (packageInfo.applicationInfo.flags & 1) != 0;
    }

    private void report(ReportInfo reportInfo) {
        String packageName = reportInfo.getPackageName();
        String version = reportInfo.getVersion();
        String[] split = reportInfo.getPermission().split("\\.");
        String str = split[split.length - 1];
        HiEvent putString = new HiEvent((int) REPORT_CODE).putInt(KEY_ERROR_TYPE, 3).putString(KEY_PACKAGE_NAME, trim(packageName, 64)).putString("version", trim(version, 32)).putString(KEY_PERMISSION_NAME, trim(str, 32));
        HiLog.debug(DPERMISSION_LABEL, "report:package:%{public}s version:%{public}s permission:%{public}s.", new Object[]{packageName, version, str});
        HiView.report(putString);
    }

    private String trim(String str, int i) {
        return str.length() > i ? str.substring(str.length() - i) : str;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0049, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x004e, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x004f, code lost:
        r0.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0052, code lost:
        throw r3;
     */
    public Set<String> getUnInstalledSystemApps() {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(String.format(Locale.ENGLISH, "%s/%s", UNINSTALLED_DELAPP_DIR, UNINSTALLED_DELAPP_FILE)));
            SAXParserFactory newInstance = SAXParserFactory.newInstance();
            newInstance.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            newInstance.setFeature("http://xml.org/sax/features/external-general-entities", false);
            newInstance.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            SAXParser newSAXParser = newInstance.newSAXParser();
            XmlHandler xmlHandler = new XmlHandler();
            newSAXParser.parse(fileInputStream, xmlHandler);
            Set<String> uninstalledPackageNames = xmlHandler.getUninstalledPackageNames();
            fileInputStream.close();
            return uninstalledPackageNames;
        } catch (FileNotFoundException unused) {
            HiLog.debug(DPERMISSION_LABEL, "Get XmlParser failed with FileNotFoundException.", new Object[0]);
            return Collections.emptySet();
        } catch (SAXException unused2) {
            HiLog.debug(DPERMISSION_LABEL, "Get XmlParser failed with SAXException.", new Object[0]);
            return Collections.emptySet();
        } catch (IOException unused3) {
            HiLog.debug(DPERMISSION_LABEL, "Get XmlParser failed with IOException.", new Object[0]);
            return Collections.emptySet();
        } catch (Exception e) {
            HiLog.debug(DPERMISSION_LABEL, "Get XmlParser failed with unexpected Exception: %{public}s.", new Object[]{e.getClass().getSimpleName()});
            return Collections.emptySet();
        }
    }

    public static class XmlHandler extends DefaultHandler {
        protected static final String PRELOAD_TAG = "/preload";
        protected static final String XML_CONFIG_NAME = "name";
        protected static final String XML_CONFIG_PATH = "codePath";
        protected static final String XML_CONFIG_TAG = "string";
        protected Set<String> packageNames = new HashSet(16);

        public Set<String> getUninstalledPackageNames() {
            return this.packageNames;
        }

        public void startElement(String str, String str2, String str3, Attributes attributes) {
            if (str3.equals("string")) {
                String value = attributes.getValue("name");
                String value2 = attributes.getValue(XML_CONFIG_PATH);
                if (value != null && value2 != null && isUninstalledAsSystemApp(value2)) {
                    this.packageNames.add(value);
                }
            }
        }

        /* access modifiers changed from: protected */
        public boolean isUninstalledAsSystemApp(String str) {
            HiLog.debug(DangerousPermissionTrackerReceiver.DPERMISSION_LABEL, "been uninstalled from %{public}s", new Object[]{str});
            return !str.startsWith(PRELOAD_TAG);
        }
    }
}
