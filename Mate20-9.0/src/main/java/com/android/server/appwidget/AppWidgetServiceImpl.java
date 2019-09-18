package com.android.server.appwidget;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManagerInternal;
import android.appwidget.AppWidgetManagerInternal;
import android.appwidget.AppWidgetProviderInfo;
import android.appwidget.IHwAWSIDAMonitorCallback;
import android.appwidget.IHwAppWidgetManager;
import android.appwidget.PendingHostUpdate;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.ShortcutServiceInternal;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.TypedValue;
import android.util.Xml;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;
import com.android.internal.R;
import com.android.internal.app.SuspendedAppActivity;
import com.android.internal.app.UnlaunchableAppActivity;
import com.android.internal.appwidget.IAppWidgetHost;
import com.android.internal.appwidget.IAppWidgetService;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.widget.IRemoteViewsFactory;
import com.android.server.AbsLocationManagerService;
import com.android.server.LocalServices;
import com.android.server.WidgetBackupProvider;
import com.android.server.display.DisplayTransformManager;
import com.android.server.policy.IconUtilities;
import com.android.server.utils.PriorityDump;
import com.huawei.pgmng.log.LogPower;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class AppWidgetServiceImpl extends IAppWidgetService.Stub implements WidgetBackupProvider, DevicePolicyManagerInternal.OnCrossProfileWidgetProvidersChangeListener {
    private static final String COTA_APP_UPDATE_APPWIDGET = "huawei.intent.action.UPDATE_COTA_APP_WIDGET";
    private static final String COTA_APP_UPDATE_APPWIDGET_EXTRA = "huawei.intent.extra.cota_package_list";
    private static final int CURRENT_VERSION = 1;
    /* access modifiers changed from: private */
    public static boolean DEBUG = false;
    private static HashMap<String, String> HIDDEN_WEATHER_WIDGETS = new HashMap<>();
    private static final boolean HIDE_HUAWEI_WEATHER_WIDGET = SystemProperties.getBoolean("ro.config.hide_weather_widget", false);
    private static final String HUAWEI_LAUNCHER_PACKAGE = "com.huawei.android.launcher";
    private static final int ID_PROVIDER_CHANGED = 1;
    private static final int ID_VIEWS_UPDATE = 0;
    private static final int KEYGUARD_HOST_ID = 1262836039;
    private static final int LOADED_PROFILE_ID = -1;
    private static final int MIN_UPDATE_PERIOD = (DEBUG ? 0 : 1800000);
    private static final String MUSlIM_APP_WIDGET_PACKAGE = "com.android.alarmclock.MuslimAppWidgetProvider";
    private static final String NEW_KEYGUARD_HOST_PACKAGE = "com.android.keyguard";
    private static final String OLD_KEYGUARD_HOST_PACKAGE = "android";
    private static final String STATE_FILENAME = "appwidgets.xml";
    private static final String TAG = "AppWidgetServiceImpl";
    private static final int TAG_UNDEFINED = -1;
    private static final int UNKNOWN_UID = -1;
    private static final int UNKNOWN_USER_ID = -10;
    private static final AtomicLong UPDATE_COUNTER = new AtomicLong();
    HwAWSIDAMonitorProxy mAWSIProxy = new HwAWSIDAMonitorProxy();
    private AlarmManager mAlarmManager;
    /* access modifiers changed from: private */
    public AppOpsManager mAppOpsManager;
    private BackupRestoreController mBackupRestoreController;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
            if (AppWidgetServiceImpl.DEBUG) {
                Slog.i(AppWidgetServiceImpl.TAG, "Received broadcast: " + action + " on user " + userId);
            }
            char c = 65535;
            switch (action.hashCode()) {
                case -1238404651:
                    if (action.equals("android.intent.action.MANAGED_PROFILE_UNAVAILABLE")) {
                        c = 2;
                        break;
                    }
                    break;
                case -1001645458:
                    if (action.equals("android.intent.action.PACKAGES_SUSPENDED")) {
                        c = 3;
                        break;
                    }
                    break;
                case -864107122:
                    if (action.equals("android.intent.action.MANAGED_PROFILE_AVAILABLE")) {
                        c = 1;
                        break;
                    }
                    break;
                case 158859398:
                    if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                        c = 0;
                        break;
                    }
                    break;
                case 1290767157:
                    if (action.equals("android.intent.action.PACKAGES_UNSUSPENDED")) {
                        c = 4;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    AppWidgetServiceImpl.this.onConfigurationChanged();
                    return;
                case 1:
                case 2:
                    synchronized (AppWidgetServiceImpl.this.mLock) {
                        AppWidgetServiceImpl.this.reloadWidgetsMaskedState(userId);
                    }
                    return;
                case 3:
                    AppWidgetServiceImpl.this.onPackageBroadcastReceived(intent, getSendingUserId());
                    AppWidgetServiceImpl.this.updateWidgetPackageSuspensionMaskedState(intent, true, getSendingUserId());
                    return;
                case 4:
                    AppWidgetServiceImpl.this.onPackageBroadcastReceived(intent, getSendingUserId());
                    AppWidgetServiceImpl.this.updateWidgetPackageSuspensionMaskedState(intent, false, getSendingUserId());
                    return;
                default:
                    AppWidgetServiceImpl.this.onPackageBroadcastReceived(intent, getSendingUserId());
                    return;
            }
        }
    };
    private Handler mCallbackHandler;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public DevicePolicyManagerInternal mDevicePolicyManagerInternal;
    /* access modifiers changed from: private */
    public final ArrayList<Host> mHosts = new ArrayList<>();
    HwInnerAppWidgetService mHwInnerService = new HwInnerAppWidgetService(this);
    private IconUtilities mIconUtilities;
    private KeyguardManager mKeyguardManager;
    private final SparseIntArray mLoadedUserIds = new SparseIntArray();
    private Locale mLocale;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private int mMaxWidgetBitmapMemory;
    private final SparseIntArray mNextAppWidgetIds = new SparseIntArray();
    /* access modifiers changed from: private */
    public IPackageManager mPackageManager;
    private PackageManagerInternal mPackageManagerInternal;
    /* access modifiers changed from: private */
    public final ArraySet<Pair<Integer, String>> mPackagesWithBindWidgetPermission = new ArraySet<>();
    /* access modifiers changed from: private */
    public final ArrayList<Provider> mProviders = new ArrayList<>();
    private final HashMap<Pair<Integer, Intent.FilterComparison>, HashSet<Integer>> mRemoteViewsServicesAppWidgets = new HashMap<>();
    private boolean mSafeMode;
    private Handler mSaveStateHandler;
    /* access modifiers changed from: private */
    public SecurityPolicy mSecurityPolicy;
    /* access modifiers changed from: private */
    public UserManager mUserManager;
    private final SparseArray<ArraySet<String>> mWidgetPackages = new SparseArray<>();
    protected final ArrayList<Widget> mWidgets = new ArrayList<>();

    private class AppWidgetManagerLocal extends AppWidgetManagerInternal {
        private AppWidgetManagerLocal() {
        }

        public ArraySet<String> getHostedWidgetPackages(int uid) {
            ArraySet<String> widgetPackages;
            synchronized (AppWidgetServiceImpl.this.mLock) {
                widgetPackages = null;
                int widgetCount = AppWidgetServiceImpl.this.mWidgets.size();
                for (int i = 0; i < widgetCount; i++) {
                    Widget widget = AppWidgetServiceImpl.this.mWidgets.get(i);
                    if (widget.host.id.uid == uid) {
                        if (widgetPackages == null) {
                            widgetPackages = new ArraySet<>();
                        }
                        if (!(widget.provider == null || widget.provider.id == null)) {
                            widgetPackages.add(widget.provider.id.componentName.getPackageName());
                        }
                    }
                }
            }
            return widgetPackages;
        }
    }

    private final class BackupRestoreController {
        private static final boolean DEBUG = true;
        private static final String TAG = "BackupRestoreController";
        private static final int WIDGET_STATE_VERSION = 2;
        private final HashSet<String> mPrunedApps;
        private final HashMap<Host, ArrayList<RestoreUpdateRecord>> mUpdatesByHost;
        private final HashMap<Provider, ArrayList<RestoreUpdateRecord>> mUpdatesByProvider;

        private class RestoreUpdateRecord {
            public int newId;
            public boolean notified = false;
            public int oldId;

            public RestoreUpdateRecord(int theOldId, int theNewId) {
                this.oldId = theOldId;
                this.newId = theNewId;
            }
        }

        private BackupRestoreController() {
            this.mPrunedApps = new HashSet<>();
            this.mUpdatesByProvider = new HashMap<>();
            this.mUpdatesByHost = new HashMap<>();
        }

        public List<String> getWidgetParticipants(int userId) {
            Slog.i(TAG, "Getting widget participants for user: " + userId);
            HashSet<String> packages = new HashSet<>();
            synchronized (AppWidgetServiceImpl.this.mLock) {
                int N = AppWidgetServiceImpl.this.mWidgets.size();
                for (int i = 0; i < N; i++) {
                    Widget widget = AppWidgetServiceImpl.this.mWidgets.get(i);
                    if (isProviderAndHostInUser(widget, userId)) {
                        packages.add(widget.host.id.packageName);
                        Provider provider = widget.provider;
                        if (provider != null) {
                            packages.add(provider.id.componentName.getPackageName());
                        }
                    }
                }
            }
            return new ArrayList(packages);
        }

        public byte[] getWidgetState(String backedupPackage, int userId) {
            Slog.i(TAG, "Getting widget state for user: " + userId);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            synchronized (AppWidgetServiceImpl.this.mLock) {
                if (!packageNeedsWidgetBackupLocked(backedupPackage, userId)) {
                    return null;
                }
                try {
                    XmlSerializer out = new FastXmlSerializer();
                    out.setOutput(stream, StandardCharsets.UTF_8.name());
                    out.startDocument(null, true);
                    out.startTag(null, "ws");
                    out.attribute(null, "version", String.valueOf(2));
                    out.attribute(null, AbsLocationManagerService.DEL_PKG, backedupPackage);
                    int N = AppWidgetServiceImpl.this.mProviders.size();
                    int i = 0;
                    int index = 0;
                    for (int i2 = 0; i2 < N; i2++) {
                        Provider provider = (Provider) AppWidgetServiceImpl.this.mProviders.get(i2);
                        if (provider.shouldBePersisted() && (provider.isInPackageForUser(backedupPackage, userId) || provider.hostedByPackageForUser(backedupPackage, userId))) {
                            provider.tag = index;
                            AppWidgetServiceImpl.serializeProvider(out, provider);
                            index++;
                        }
                    }
                    int N2 = AppWidgetServiceImpl.this.mHosts.size();
                    int index2 = 0;
                    for (int i3 = 0; i3 < N2; i3++) {
                        Host host = (Host) AppWidgetServiceImpl.this.mHosts.get(i3);
                        if (!host.widgets.isEmpty() && (host.isInPackageForUser(backedupPackage, userId) || host.hostsPackageForUser(backedupPackage, userId))) {
                            host.tag = index2;
                            AppWidgetServiceImpl.serializeHost(out, host);
                            index2++;
                        }
                    }
                    int N3 = AppWidgetServiceImpl.this.mWidgets.size();
                    while (true) {
                        int i4 = i;
                        if (i4 < N3) {
                            Widget widget = AppWidgetServiceImpl.this.mWidgets.get(i4);
                            Provider provider2 = widget.provider;
                            if (widget.host.isInPackageForUser(backedupPackage, userId) || (provider2 != null && provider2.isInPackageForUser(backedupPackage, userId))) {
                                AppWidgetServiceImpl.serializeAppWidget(out, widget);
                            }
                            i = i4 + 1;
                        } else {
                            out.endTag(null, "ws");
                            out.endDocument();
                            return stream.toByteArray();
                        }
                    }
                } catch (IOException e) {
                    Slog.w(TAG, "Unable to save widget state for " + backedupPackage);
                    return null;
                }
            }
        }

        public void restoreStarting(int userId) {
            Slog.i(TAG, "Restore starting for user: " + userId);
            synchronized (AppWidgetServiceImpl.this.mLock) {
                this.mPrunedApps.clear();
                this.mUpdatesByProvider.clear();
                this.mUpdatesByHost.clear();
            }
        }

        public void restoreWidgetState(String packageName, byte[] restoredState, int userId) {
            ArrayList<Host> restoredHosts;
            ArrayList<Provider> restoredProviders;
            ByteArrayInputStream stream;
            StringBuilder sb;
            Provider p;
            String str = packageName;
            int i = userId;
            Slog.i(TAG, "Restoring widget state for user:" + i + " package: " + str);
            ByteArrayInputStream stream2 = new ByteArrayInputStream(restoredState);
            try {
                ArrayList<Provider> restoredProviders2 = new ArrayList<>();
                ArrayList<Host> restoredHosts2 = new ArrayList<>();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, StandardCharsets.UTF_8.name());
                synchronized (AppWidgetServiceImpl.this.mLock) {
                    while (true) {
                        try {
                            int type = parser.next();
                            if (type == 2) {
                                String tag = parser.getName();
                                if ("ws".equals(tag)) {
                                    try {
                                        String version = parser.getAttributeValue(null, "version");
                                        if (Integer.parseInt(version) > 2) {
                                            Slog.w(TAG, "Unable to process state version " + version);
                                            AppWidgetServiceImpl.this.saveGroupStateAsync(i);
                                            return;
                                        } else if (!str.equals(parser.getAttributeValue(null, AbsLocationManagerService.DEL_PKG))) {
                                            Slog.w(TAG, "Package mismatch in ws");
                                            AppWidgetServiceImpl.this.saveGroupStateAsync(i);
                                            return;
                                        } else {
                                            stream = stream2;
                                        }
                                    } catch (Throwable th) {
                                        th = th;
                                        ByteArrayInputStream byteArrayInputStream = stream2;
                                        try {
                                            throw th;
                                        } catch (IOException | XmlPullParserException e) {
                                        }
                                    }
                                } else if ("p".equals(tag)) {
                                    try {
                                        ComponentName componentName = new ComponentName(parser.getAttributeValue(null, AbsLocationManagerService.DEL_PKG), parser.getAttributeValue(null, "cl"));
                                        Provider p2 = findProviderLocked(componentName, i);
                                        if (p2 == null) {
                                            p = new Provider();
                                            stream = stream2;
                                            try {
                                                p.id = new ProviderId(-1, componentName);
                                                p.info = new AppWidgetProviderInfo();
                                                p.info.provider = componentName;
                                                p.zombie = true;
                                                AppWidgetServiceImpl.this.mProviders.add(p);
                                            } catch (Throwable th2) {
                                                th = th2;
                                            }
                                        } else {
                                            stream = stream2;
                                            p = p2;
                                        }
                                        Slog.i(TAG, "   provider " + p.id);
                                        restoredProviders2.add(p);
                                    } catch (Throwable th3) {
                                        th = th3;
                                        ByteArrayInputStream byteArrayInputStream2 = stream2;
                                        ArrayList<Provider> arrayList = restoredProviders2;
                                        ArrayList<Host> arrayList2 = restoredHosts2;
                                        throw th;
                                    }
                                } else {
                                    stream = stream2;
                                    try {
                                        if ("h".equals(tag)) {
                                            String pkg = parser.getAttributeValue(null, AbsLocationManagerService.DEL_PKG);
                                            Host h = AppWidgetServiceImpl.this.lookupOrAddHostLocked(new HostId(AppWidgetServiceImpl.this.getUidForPackage(pkg, i), Integer.parseInt(parser.getAttributeValue(null, "id"), 16), pkg));
                                            restoredHosts2.add(h);
                                            StringBuilder sb2 = new StringBuilder();
                                            String str2 = pkg;
                                            sb2.append("   host[");
                                            sb2.append(restoredHosts2.size());
                                            sb2.append("]: {");
                                            sb2.append(h.id);
                                            sb2.append("}");
                                            Slog.i(TAG, sb2.toString());
                                        } else if ("g".equals(tag)) {
                                            int restoredId = Integer.parseInt(parser.getAttributeValue(null, "id"), 16);
                                            Host host = restoredHosts2.get(Integer.parseInt(parser.getAttributeValue(null, "h"), 16));
                                            Provider p3 = null;
                                            String prov = parser.getAttributeValue(null, "p");
                                            if (prov != null) {
                                                p3 = restoredProviders2.get(Integer.parseInt(prov, 16));
                                            }
                                            if (AppWidgetServiceImpl.HUAWEI_LAUNCHER_PACKAGE.equals(host.id.packageName)) {
                                                try {
                                                    sb = new StringBuilder();
                                                    restoredProviders = restoredProviders2;
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                    ArrayList<Provider> arrayList3 = restoredProviders2;
                                                    ArrayList<Host> arrayList4 = restoredHosts2;
                                                    throw th;
                                                }
                                                try {
                                                    sb.append("Skip restore widget state in huawei launcher host for package: ");
                                                    sb.append(str);
                                                    Slog.i(TAG, sb.toString());
                                                    restoredHosts = restoredHosts2;
                                                } catch (Throwable th5) {
                                                    th = th5;
                                                }
                                            } else {
                                                restoredProviders = restoredProviders2;
                                                try {
                                                    pruneWidgetStateLocked(host.id.packageName, i);
                                                    if (p3 != null) {
                                                        pruneWidgetStateLocked(p3.id.componentName.getPackageName(), i);
                                                    }
                                                    Widget id = findRestoredWidgetLocked(restoredId, host, p3);
                                                    if (id == null) {
                                                        id = new Widget();
                                                        id.appWidgetId = AppWidgetServiceImpl.this.incrementAndGetAppWidgetIdLocked(i);
                                                        id.restoredId = restoredId;
                                                        id.options = parseWidgetIdOptions(parser);
                                                        id.host = host;
                                                        id.host.widgets.add(id);
                                                        id.provider = p3;
                                                        if (id.provider != null) {
                                                            id.provider.widgets.add(id);
                                                        }
                                                        StringBuilder sb3 = new StringBuilder();
                                                        restoredHosts = restoredHosts2;
                                                        sb3.append("New restored id ");
                                                        sb3.append(restoredId);
                                                        sb3.append(" now ");
                                                        sb3.append(id);
                                                        Slog.i(TAG, sb3.toString());
                                                        AppWidgetServiceImpl.this.addWidgetLocked(id);
                                                    } else {
                                                        restoredHosts = restoredHosts2;
                                                    }
                                                    if (id.provider == null || id.provider.info == null) {
                                                        Slog.w(TAG, "Missing provider for restored widget " + id);
                                                    } else {
                                                        stashProviderRestoreUpdateLocked(id.provider, restoredId, id.appWidgetId);
                                                    }
                                                    stashHostRestoreUpdateLocked(id.host, restoredId, id.appWidgetId);
                                                    Slog.i(TAG, "   instance: " + restoredId + " -> " + id.appWidgetId + " :: p=" + id.provider);
                                                } catch (Throwable th6) {
                                                    th = th6;
                                                    throw th;
                                                }
                                            }
                                        } else {
                                            restoredProviders = restoredProviders2;
                                            restoredHosts = restoredHosts2;
                                        }
                                    } catch (Throwable th7) {
                                        th = th7;
                                        ArrayList<Provider> arrayList5 = restoredProviders2;
                                        ArrayList<Host> arrayList6 = restoredHosts2;
                                        throw th;
                                    }
                                }
                                restoredProviders = restoredProviders2;
                                restoredHosts = restoredHosts2;
                            } else {
                                stream = stream2;
                                restoredProviders = restoredProviders2;
                                restoredHosts = restoredHosts2;
                            }
                            if (type != 1) {
                                stream2 = stream;
                                restoredProviders2 = restoredProviders;
                                restoredHosts2 = restoredHosts;
                                byte[] bArr = restoredState;
                            }
                        } catch (Throwable th8) {
                            th = th8;
                            ByteArrayInputStream byteArrayInputStream3 = stream2;
                            ArrayList<Provider> arrayList7 = restoredProviders2;
                            ArrayList<Host> arrayList8 = restoredHosts2;
                            throw th;
                        }
                    }
                    AppWidgetServiceImpl.this.saveGroupStateAsync(i);
                }
            } catch (IOException | XmlPullParserException e2) {
                ByteArrayInputStream byteArrayInputStream4 = stream2;
                try {
                    Slog.w(TAG, "Unable to restore widget state for " + str);
                    AppWidgetServiceImpl.this.saveGroupStateAsync(i);
                } catch (Throwable th9) {
                    th = th9;
                    AppWidgetServiceImpl.this.saveGroupStateAsync(i);
                    throw th;
                }
            } catch (Throwable th10) {
                th = th10;
                ByteArrayInputStream byteArrayInputStream5 = stream2;
                AppWidgetServiceImpl.this.saveGroupStateAsync(i);
                throw th;
            }
        }

        public void restoreFinished(int userId) {
            Slog.i(TAG, "restoreFinished for " + r14);
            UserHandle userHandle = new UserHandle(userId);
            synchronized (AppWidgetServiceImpl.this.mLock) {
                Iterator<Map.Entry<Provider, ArrayList<RestoreUpdateRecord>>> it = this.mUpdatesByProvider.entrySet().iterator();
                while (true) {
                    boolean z = true;
                    if (!it.hasNext()) {
                        break;
                    }
                    Map.Entry next = it.next();
                    Provider provider = (Provider) next.getKey();
                    ArrayList arrayList = (ArrayList) next.getValue();
                    int pending = countPendingUpdates(arrayList);
                    Slog.i(TAG, "Provider " + provider + " pending: " + pending);
                    if (pending > 0) {
                        int[] oldIds = new int[pending];
                        int[] newIds = new int[pending];
                        int N = arrayList.size();
                        int nextPending = 0;
                        int i = 0;
                        while (true) {
                            int i2 = i;
                            if (i2 >= N) {
                                break;
                            }
                            RestoreUpdateRecord r = (RestoreUpdateRecord) arrayList.get(i2);
                            if (!r.notified) {
                                r.notified = z;
                                oldIds[nextPending] = r.oldId;
                                newIds[nextPending] = r.newId;
                                nextPending++;
                                Slog.i(TAG, "   " + r.oldId + " => " + r.newId);
                            }
                            i = i2 + 1;
                            z = true;
                        }
                        int i3 = N;
                        sendWidgetRestoreBroadcastLocked("android.appwidget.action.APPWIDGET_RESTORED", provider, null, oldIds, newIds, userHandle);
                    }
                }
                for (Map.Entry<Host, ArrayList<RestoreUpdateRecord>> e : this.mUpdatesByHost.entrySet()) {
                    Host host = e.getKey();
                    if (host.id.uid != -1) {
                        ArrayList<RestoreUpdateRecord> updates = e.getValue();
                        int pending2 = countPendingUpdates(updates);
                        Slog.i(TAG, "Host " + host + " pending: " + pending2);
                        if (pending2 > 0) {
                            int[] oldIds2 = new int[pending2];
                            int[] newIds2 = new int[pending2];
                            int N2 = updates.size();
                            int nextPending2 = 0;
                            for (int i4 = 0; i4 < N2; i4++) {
                                RestoreUpdateRecord r2 = updates.get(i4);
                                if (!r2.notified) {
                                    r2.notified = true;
                                    oldIds2[nextPending2] = r2.oldId;
                                    newIds2[nextPending2] = r2.newId;
                                    nextPending2++;
                                    Slog.i(TAG, "   " + r2.oldId + " => " + r2.newId);
                                }
                            }
                            int i5 = N2;
                            int i6 = pending2;
                            sendWidgetRestoreBroadcastLocked("android.appwidget.action.APPWIDGET_HOST_RESTORED", null, host, oldIds2, newIds2, userHandle);
                            int i7 = userId;
                        }
                    }
                    int i72 = userId;
                }
            }
        }

        private Provider findProviderLocked(ComponentName componentName, int userId) {
            int providerCount = AppWidgetServiceImpl.this.mProviders.size();
            for (int i = 0; i < providerCount; i++) {
                Provider provider = (Provider) AppWidgetServiceImpl.this.mProviders.get(i);
                if (provider.getUserId() == userId && provider.id.componentName.equals(componentName)) {
                    return provider;
                }
            }
            return null;
        }

        private Widget findRestoredWidgetLocked(int restoredId, Host host, Provider p) {
            Slog.i(TAG, "Find restored widget: id=" + restoredId + " host=" + host + " provider=" + p);
            if (p == null || host == null) {
                return null;
            }
            int N = AppWidgetServiceImpl.this.mWidgets.size();
            int i = 0;
            while (i < N) {
                Widget widget = AppWidgetServiceImpl.this.mWidgets.get(i);
                if (widget.restoredId != restoredId || !widget.host.id.equals(host.id) || !widget.provider.id.equals(p.id)) {
                    i++;
                } else {
                    Slog.i(TAG, "   Found at " + i + " : " + widget);
                    return widget;
                }
            }
            return null;
        }

        private boolean packageNeedsWidgetBackupLocked(String packageName, int userId) {
            int N = AppWidgetServiceImpl.this.mWidgets.size();
            for (int i = 0; i < N; i++) {
                Widget widget = AppWidgetServiceImpl.this.mWidgets.get(i);
                if (isProviderAndHostInUser(widget, userId)) {
                    if (widget.host.isInPackageForUser(packageName, userId)) {
                        return true;
                    }
                    Provider provider = widget.provider;
                    if (provider != null && provider.isInPackageForUser(packageName, userId)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void stashProviderRestoreUpdateLocked(Provider provider, int oldId, int newId) {
            ArrayList<RestoreUpdateRecord> r = this.mUpdatesByProvider.get(provider);
            if (r == null) {
                r = new ArrayList<>();
                this.mUpdatesByProvider.put(provider, r);
            } else if (alreadyStashed(r, oldId, newId)) {
                Slog.i(TAG, "ID remap " + oldId + " -> " + newId + " already stashed for " + provider);
                return;
            }
            r.add(new RestoreUpdateRecord(oldId, newId));
        }

        private boolean alreadyStashed(ArrayList<RestoreUpdateRecord> stash, int oldId, int newId) {
            int N = stash.size();
            for (int i = 0; i < N; i++) {
                RestoreUpdateRecord r = stash.get(i);
                if (r.oldId == oldId && r.newId == newId) {
                    return true;
                }
            }
            return false;
        }

        private void stashHostRestoreUpdateLocked(Host host, int oldId, int newId) {
            ArrayList<RestoreUpdateRecord> r = this.mUpdatesByHost.get(host);
            if (r == null) {
                r = new ArrayList<>();
                this.mUpdatesByHost.put(host, r);
            } else if (alreadyStashed(r, oldId, newId)) {
                Slog.i(TAG, "ID remap " + oldId + " -> " + newId + " already stashed for " + host);
                return;
            }
            r.add(new RestoreUpdateRecord(oldId, newId));
        }

        private void sendWidgetRestoreBroadcastLocked(String action, Provider provider, Host host, int[] oldIds, int[] newIds, UserHandle userHandle) {
            Intent intent = new Intent(action);
            intent.putExtra("appWidgetOldIds", oldIds);
            intent.putExtra("appWidgetIds", newIds);
            if (provider != null) {
                intent.setComponent(provider.info.provider);
                AppWidgetServiceImpl.this.sendBroadcastAsUser(intent, userHandle);
            }
            if (host != null) {
                intent.setComponent(null);
                intent.setPackage(host.id.packageName);
                intent.putExtra("hostId", host.id.hostId);
                AppWidgetServiceImpl.this.sendBroadcastAsUser(intent, userHandle);
            }
        }

        private void pruneWidgetStateLocked(String pkg, int userId) {
            if (!this.mPrunedApps.contains(pkg)) {
                Slog.i(TAG, "pruning widget state for restoring package " + pkg);
                for (int i = AppWidgetServiceImpl.this.mWidgets.size() + -1; i >= 0; i--) {
                    Widget widget = AppWidgetServiceImpl.this.mWidgets.get(i);
                    Host host = widget.host;
                    Provider provider = widget.provider;
                    if (host.hostsPackageForUser(pkg, userId) || (provider != null && provider.isInPackageForUser(pkg, userId))) {
                        host.widgets.remove(widget);
                        provider.widgets.remove(widget);
                        AppWidgetServiceImpl.this.decrementAppWidgetServiceRefCount(widget);
                        AppWidgetServiceImpl.this.removeWidgetLocked(widget);
                    }
                }
                this.mPrunedApps.add(pkg);
                return;
            }
            Slog.i(TAG, "already pruned " + pkg + ", continuing normally");
        }

        private boolean isProviderAndHostInUser(Widget widget, int userId) {
            return widget.host.getUserId() == userId && (widget.provider == null || widget.provider.getUserId() == userId);
        }

        private Bundle parseWidgetIdOptions(XmlPullParser parser) {
            Bundle options = new Bundle();
            String minWidthString = parser.getAttributeValue(null, "min_width");
            if (minWidthString != null) {
                options.putInt("appWidgetMinWidth", Integer.parseInt(minWidthString, 16));
            }
            String minHeightString = parser.getAttributeValue(null, "min_height");
            if (minHeightString != null) {
                options.putInt("appWidgetMinHeight", Integer.parseInt(minHeightString, 16));
            }
            String maxWidthString = parser.getAttributeValue(null, "max_width");
            if (maxWidthString != null) {
                options.putInt("appWidgetMaxWidth", Integer.parseInt(maxWidthString, 16));
            }
            String maxHeightString = parser.getAttributeValue(null, "max_height");
            if (maxHeightString != null) {
                options.putInt("appWidgetMaxHeight", Integer.parseInt(maxHeightString, 16));
            }
            String categoryString = parser.getAttributeValue(null, "host_category");
            if (categoryString != null) {
                options.putInt("appWidgetCategory", Integer.parseInt(categoryString, 16));
            }
            return options;
        }

        private int countPendingUpdates(ArrayList<RestoreUpdateRecord> updates) {
            int pending = 0;
            int N = updates.size();
            for (int i = 0; i < N; i++) {
                if (!updates.get(i).notified) {
                    pending++;
                }
            }
            return pending;
        }
    }

    private final class CallbackHandler extends Handler {
        public static final int MSG_NOTIFY_PROVIDERS_CHANGED = 3;
        public static final int MSG_NOTIFY_PROVIDER_CHANGED = 2;
        public static final int MSG_NOTIFY_RECYCLE_REMOTE_VIEW = 20;
        public static final int MSG_NOTIFY_UPDATE_APP_WIDGET = 1;
        public static final int MSG_NOTIFY_VIEW_DATA_CHANGED = 4;

        public CallbackHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i != 20) {
                switch (i) {
                    case 1:
                        SomeArgs args = (SomeArgs) message.obj;
                        long requestId = ((Long) args.arg4).longValue();
                        int appWidgetId = args.argi1;
                        args.recycle();
                        AppWidgetServiceImpl.this.handleNotifyUpdateAppWidget((Host) args.arg1, (IAppWidgetHost) args.arg2, appWidgetId, (RemoteViews) args.arg3, requestId);
                        return;
                    case 2:
                        SomeArgs args2 = (SomeArgs) message.obj;
                        long requestId2 = ((Long) args2.arg4).longValue();
                        int appWidgetId2 = args2.argi1;
                        args2.recycle();
                        AppWidgetServiceImpl.this.handleNotifyProviderChanged((Host) args2.arg1, (IAppWidgetHost) args2.arg2, appWidgetId2, (AppWidgetProviderInfo) args2.arg3, requestId2);
                        return;
                    case 3:
                        SomeArgs args3 = (SomeArgs) message.obj;
                        args3.recycle();
                        AppWidgetServiceImpl.this.handleNotifyProvidersChanged((Host) args3.arg1, (IAppWidgetHost) args3.arg2);
                        return;
                    case 4:
                        SomeArgs args4 = (SomeArgs) message.obj;
                        long requestId3 = ((Long) args4.arg3).longValue();
                        int appWidgetId3 = args4.argi1;
                        int viewId = args4.argi2;
                        args4.recycle();
                        AppWidgetServiceImpl.this.handleNotifyAppWidgetViewDataChanged((Host) args4.arg1, (IAppWidgetHost) args4.arg2, appWidgetId3, viewId, requestId3);
                        return;
                    default:
                        return;
                }
            } else {
                SomeArgs args5 = (SomeArgs) message.obj;
                RemoteViews views = (RemoteViews) args5.arg1;
                if (views != null) {
                    views.recycle();
                }
                args5.recycle();
            }
        }
    }

    private static final class Host {
        IAppWidgetHost callbacks;
        HostId id;
        long lastWidgetUpdateSequenceNo;
        int tag;
        ArrayList<Widget> widgets;
        boolean zombie;

        private Host() {
            this.widgets = new ArrayList<>();
            this.tag = -1;
        }

        public int getUserId() {
            return UserHandle.getUserId(this.id.uid);
        }

        public boolean isInPackageForUser(String packageName, int userId) {
            return getUserId() == userId && this.id.packageName.equals(packageName);
        }

        /* access modifiers changed from: private */
        public boolean hostsPackageForUser(String pkg, int userId) {
            int N = this.widgets.size();
            for (int i = 0; i < N; i++) {
                Provider provider = this.widgets.get(i).provider;
                if (provider != null && provider.getUserId() == userId && provider.info != null && pkg.equals(provider.info.provider.getPackageName())) {
                    return true;
                }
            }
            return false;
        }

        public boolean getPendingUpdatesForId(int appWidgetId, LongSparseArray<PendingHostUpdate> outUpdates) {
            PendingHostUpdate update;
            long updateSequenceNo = this.lastWidgetUpdateSequenceNo;
            int N = this.widgets.size();
            for (int i = 0; i < N; i++) {
                Widget widget = this.widgets.get(i);
                if (widget.appWidgetId == appWidgetId) {
                    outUpdates.clear();
                    for (int j = widget.updateSequenceNos.size() - 1; j >= 0; j--) {
                        long requestId = widget.updateSequenceNos.valueAt(j);
                        if (requestId > updateSequenceNo) {
                            int id2 = widget.updateSequenceNos.keyAt(j);
                            switch (id2) {
                                case 0:
                                    update = PendingHostUpdate.updateAppWidget(appWidgetId, AppWidgetServiceImpl.cloneIfLocalBinder(widget.getEffectiveViewsLocked()));
                                    break;
                                case 1:
                                    update = PendingHostUpdate.providerChanged(appWidgetId, widget.provider.info);
                                    break;
                                default:
                                    update = PendingHostUpdate.viewDataChanged(appWidgetId, id2);
                                    break;
                            }
                            outUpdates.put(requestId, update);
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Host{");
            sb.append(this.id);
            sb.append(this.zombie ? " Z" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            sb.append('}');
            return sb.toString();
        }
    }

    private static final class HostId {
        final int hostId;
        final String packageName;
        final int uid;

        public HostId(int uid2, int hostId2, String packageName2) {
            this.uid = uid2;
            this.hostId = hostId2;
            this.packageName = packageName2;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            HostId other = (HostId) obj;
            if (this.uid != other.uid || this.hostId != other.hostId) {
                return false;
            }
            if (this.packageName == null) {
                if (other.packageName != null) {
                    return false;
                }
            } else if (!this.packageName.equals(other.packageName)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return (31 * ((31 * this.uid) + this.hostId)) + (this.packageName != null ? this.packageName.hashCode() : 0);
        }

        public String toString() {
            return "HostId{user:" + UserHandle.getUserId(this.uid) + ", app:" + UserHandle.getAppId(this.uid) + ", hostId:" + this.hostId + ", pkg:" + this.packageName + '}';
        }
    }

    public class HwInnerAppWidgetService extends IHwAppWidgetManager.Stub {
        AppWidgetServiceImpl mAWSI;

        HwInnerAppWidgetService(AppWidgetServiceImpl service) {
            this.mAWSI = service;
        }

        private boolean checkSystemPermission() {
            int uid = UserHandle.getAppId(Binder.getCallingUid());
            if (uid == 1000) {
                return true;
            }
            Slog.e(AppWidgetServiceImpl.TAG, "process permission error! uid:" + uid);
            return false;
        }

        public boolean registerAWSIMonitorCallback(IHwAWSIDAMonitorCallback callback) {
            if (!checkSystemPermission()) {
                return false;
            }
            AppWidgetServiceImpl.this.mAWSIProxy.registerAWSIMonitorCallback(callback);
            return true;
        }
    }

    private class LoadedWidgetState {
        final int hostTag;
        final int providerTag;
        final Widget widget;

        public LoadedWidgetState(Widget widget2, int hostTag2, int providerTag2) {
            this.widget = widget2;
            this.hostTag = hostTag2;
            this.providerTag = providerTag2;
        }
    }

    protected static final class Provider {
        PendingIntent broadcast;
        ProviderId id;
        AppWidgetProviderInfo info;
        String infoTag;
        boolean maskedByLockedProfile;
        boolean maskedByQuietProfile;
        boolean maskedBySuspendedPackage;
        int tag = -1;
        ArrayList<Widget> widgets = new ArrayList<>();
        boolean zombie;

        protected Provider() {
        }

        public int getUserId() {
            return UserHandle.getUserId(this.id.uid);
        }

        public boolean isInPackageForUser(String packageName, int userId) {
            return getUserId() == userId && this.id.componentName.getPackageName().equals(packageName);
        }

        public boolean hostedByPackageForUser(String packageName, int userId) {
            int N = this.widgets.size();
            for (int i = 0; i < N; i++) {
                Widget widget = this.widgets.get(i);
                if (packageName.equals(widget.host.id.packageName) && widget.host.getUserId() == userId) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Provider{");
            sb.append(this.id);
            sb.append(this.zombie ? " Z" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            sb.append('}');
            return sb.toString();
        }

        public boolean setMaskedByQuietProfileLocked(boolean masked) {
            boolean oldState = this.maskedByQuietProfile;
            this.maskedByQuietProfile = masked;
            return masked != oldState;
        }

        public boolean setMaskedByLockedProfileLocked(boolean masked) {
            boolean oldState = this.maskedByLockedProfile;
            this.maskedByLockedProfile = masked;
            return masked != oldState;
        }

        public boolean setMaskedBySuspendedPackageLocked(boolean masked) {
            boolean oldState = this.maskedBySuspendedPackage;
            this.maskedBySuspendedPackage = masked;
            return masked != oldState;
        }

        public boolean isMaskedLocked() {
            return this.maskedByQuietProfile || this.maskedByLockedProfile || this.maskedBySuspendedPackage;
        }

        public boolean shouldBePersisted() {
            return !this.widgets.isEmpty() || !TextUtils.isEmpty(this.infoTag);
        }
    }

    private static final class ProviderId {
        final ComponentName componentName;
        final int uid;

        private ProviderId(int uid2, ComponentName componentName2) {
            this.uid = uid2;
            this.componentName = componentName2;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ProviderId other = (ProviderId) obj;
            if (this.uid != other.uid) {
                return false;
            }
            if (this.componentName == null) {
                if (other.componentName != null) {
                    return false;
                }
            } else if (!this.componentName.equals(other.componentName)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return (31 * this.uid) + (this.componentName != null ? this.componentName.hashCode() : 0);
        }

        public String toString() {
            return "ProviderId{user:" + UserHandle.getUserId(this.uid) + ", app:" + UserHandle.getAppId(this.uid) + ", cmp:" + this.componentName + '}';
        }
    }

    private final class SaveStateRunnable implements Runnable {
        final int mUserId;

        public SaveStateRunnable(int userId) {
            this.mUserId = userId;
        }

        public void run() {
            synchronized (AppWidgetServiceImpl.this.mLock) {
                AppWidgetServiceImpl.this.ensureGroupStateLoadedLocked(this.mUserId, false);
                AppWidgetServiceImpl.this.saveStateLocked(this.mUserId);
            }
        }
    }

    private final class SecurityPolicy {
        private SecurityPolicy() {
        }

        public boolean isEnabledGroupProfile(int profileId) {
            return isParentOrProfile(UserHandle.getCallingUserId(), profileId) && isProfileEnabled(profileId);
        }

        public int[] getEnabledGroupProfileIds(int userId) {
            int parentId = getGroupParent(userId);
            long identity = Binder.clearCallingIdentity();
            try {
                return AppWidgetServiceImpl.this.mUserManager.getEnabledProfileIds(parentId);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void enforceServiceExistsAndRequiresBindRemoteViewsPermission(ComponentName componentName, int userId) {
            long identity = Binder.clearCallingIdentity();
            try {
                ServiceInfo serviceInfo = AppWidgetServiceImpl.this.mPackageManager.getServiceInfo(componentName, 4096, userId);
                if (serviceInfo == null) {
                    throw new SecurityException("Service " + componentName + " not installed for user " + userId);
                } else if ("android.permission.BIND_REMOTEVIEWS".equals(serviceInfo.permission)) {
                    Binder.restoreCallingIdentity(identity);
                } else {
                    throw new SecurityException("Service " + componentName + " in user " + userId + "does not require " + "android.permission.BIND_REMOTEVIEWS");
                }
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public void enforceModifyAppWidgetBindPermissions(String packageName) {
            Context access$1300 = AppWidgetServiceImpl.this.mContext;
            access$1300.enforceCallingPermission("android.permission.MODIFY_APPWIDGET_BIND_PERMISSIONS", "hasBindAppWidgetPermission packageName=" + packageName);
        }

        public boolean isCallerInstantAppLocked() {
            int callingUid = Binder.getCallingUid();
            long identity = Binder.clearCallingIdentity();
            try {
                String[] uidPackages = AppWidgetServiceImpl.this.mPackageManager.getPackagesForUid(callingUid);
                if (!ArrayUtils.isEmpty(uidPackages)) {
                    boolean isInstantApp = AppWidgetServiceImpl.this.mPackageManager.isInstantApp(uidPackages[0], UserHandle.getCallingUserId());
                    Binder.restoreCallingIdentity(identity);
                    return isInstantApp;
                }
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
            return false;
        }

        /* JADX INFO: finally extract failed */
        public boolean isInstantAppLocked(String packageName, int userId) {
            long identity = Binder.clearCallingIdentity();
            try {
                boolean isInstantApp = AppWidgetServiceImpl.this.mPackageManager.isInstantApp(packageName, userId);
                Binder.restoreCallingIdentity(identity);
                return isInstantApp;
            } catch (RemoteException e) {
                Binder.restoreCallingIdentity(identity);
                return false;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public void enforceCallFromPackage(String packageName) {
            AppWidgetServiceImpl.this.mAppOpsManager.checkPackage(Binder.getCallingUid(), packageName);
        }

        public boolean hasCallerBindPermissionOrBindWhiteListedLocked(String packageName) {
            try {
                AppWidgetServiceImpl.this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_APPWIDGET", null);
            } catch (SecurityException e) {
                if (!isCallerBindAppWidgetWhiteListedLocked(packageName)) {
                    return false;
                }
            }
            return true;
        }

        private boolean isCallerBindAppWidgetWhiteListedLocked(String packageName) {
            int userId = UserHandle.getCallingUserId();
            if (AppWidgetServiceImpl.this.getUidForPackage(packageName, userId) >= 0) {
                synchronized (AppWidgetServiceImpl.this.mLock) {
                    AppWidgetServiceImpl.this.ensureGroupStateLoadedLocked(userId);
                    if (AppWidgetServiceImpl.this.mPackagesWithBindWidgetPermission.contains(Pair.create(Integer.valueOf(userId), packageName))) {
                        return true;
                    }
                    return false;
                }
            }
            throw new IllegalArgumentException("No package " + packageName + " for user " + userId);
        }

        public boolean canAccessAppWidget(Widget widget, int uid, String packageName) {
            if (isHostInPackageForUid(widget.host, uid, packageName) || isProviderInPackageForUid(widget.provider, uid, packageName) || isHostAccessingProvider(widget.host, widget.provider, uid, packageName)) {
                return true;
            }
            int userId = UserHandle.getUserId(uid);
            if ((widget.host.getUserId() == userId || (widget.provider != null && widget.provider.getUserId() == userId)) && AppWidgetServiceImpl.this.mContext.checkCallingPermission("android.permission.BIND_APPWIDGET") == 0) {
                return true;
            }
            return false;
        }

        private boolean isParentOrProfile(int parentId, int profileId) {
            boolean z = true;
            if (parentId == profileId) {
                return true;
            }
            if (getProfileParent(profileId) != parentId) {
                z = false;
            }
            return z;
        }

        public boolean isProviderInCallerOrInProfileAndWhitelListed(String packageName, int profileId) {
            int callerId = UserHandle.getCallingUserId();
            if (profileId == callerId) {
                return true;
            }
            if (getProfileParent(profileId) != callerId) {
                return false;
            }
            return isProviderWhiteListed(packageName, profileId);
        }

        public boolean isProviderWhiteListed(String packageName, int profileId) {
            if (AppWidgetServiceImpl.this.mDevicePolicyManagerInternal == null) {
                return false;
            }
            return AppWidgetServiceImpl.this.mDevicePolicyManagerInternal.getCrossProfileWidgetProviders(profileId).contains(packageName);
        }

        public int getProfileParent(int profileId) {
            long identity = Binder.clearCallingIdentity();
            try {
                UserInfo parent = AppWidgetServiceImpl.this.mUserManager.getProfileParent(profileId);
                if (parent != null) {
                    return parent.getUserHandle().getIdentifier();
                }
                Binder.restoreCallingIdentity(identity);
                return -10;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public int getGroupParent(int profileId) {
            int parentId = AppWidgetServiceImpl.this.mSecurityPolicy.getProfileParent(profileId);
            return parentId != -10 ? parentId : profileId;
        }

        public boolean isHostInPackageForUid(Host host, int uid, String packageName) {
            return host.id.uid == uid && host.id.packageName.equals(packageName);
        }

        public boolean isProviderInPackageForUid(Provider provider, int uid, String packageName) {
            return provider != null && provider.id.uid == uid && provider.id.componentName.getPackageName().equals(packageName);
        }

        public boolean isHostAccessingProvider(Host host, Provider provider, int uid, String packageName) {
            return host.id.uid == uid && provider != null && provider.id.componentName.getPackageName().equals(packageName);
        }

        /* JADX INFO: finally extract failed */
        private boolean isProfileEnabled(int profileId) {
            long identity = Binder.clearCallingIdentity();
            try {
                UserInfo userInfo = AppWidgetServiceImpl.this.mUserManager.getUserInfo(profileId);
                if (userInfo == null || !userInfo.isEnabled()) {
                    Binder.restoreCallingIdentity(identity);
                    return false;
                }
                Binder.restoreCallingIdentity(identity);
                return true;
            } catch (Throwable userInfo2) {
                Binder.restoreCallingIdentity(identity);
                throw userInfo2;
            }
        }
    }

    protected static final class Widget {
        int appWidgetId;
        Host host;
        RemoteViews maskedViews;
        Bundle options;
        Provider provider;
        int restoredId;
        SparseLongArray updateSequenceNos = new SparseLongArray(2);
        RemoteViews views;

        protected Widget() {
        }

        public String toString() {
            return "AppWidgetId{" + this.appWidgetId + ':' + this.host + ':' + this.provider + '}';
        }

        /* access modifiers changed from: private */
        public boolean replaceWithMaskedViewsLocked(RemoteViews views2) {
            this.maskedViews = views2;
            return true;
        }

        /* access modifiers changed from: private */
        public boolean clearMaskedViewsLocked() {
            if (this.maskedViews == null) {
                return false;
            }
            this.maskedViews = null;
            return true;
        }

        public RemoteViews getEffectiveViewsLocked() {
            return this.maskedViews != null ? this.maskedViews : this.views;
        }
    }

    static {
        HIDDEN_WEATHER_WIDGETS.put("com.huawei.android.totemweather.widget.mulan.MulanWidgetWeatherProvider", "1");
        HIDDEN_WEATHER_WIDGETS.put("com.huawei.android.totemweather.widget.doublecity.DualWidgetWeatherProvider", "1");
        HIDDEN_WEATHER_WIDGETS.put("com.huawei.android.totemweather.widget.WeatherSmallWidgetProvider", "1");
        HIDDEN_WEATHER_WIDGETS.put("com.huawei.android.totemweather.widget.WeatherSimpleWidgetProvider", "1");
        HIDDEN_WEATHER_WIDGETS.put("com.huawei.android.totemweather.widget.WeatherLimitWidgetProvider", "1");
    }

    AppWidgetServiceImpl(Context context) {
        this.mContext = context;
    }

    public void onStart() {
        this.mPackageManager = AppGlobals.getPackageManager();
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mDevicePolicyManagerInternal = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        this.mPackageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mSaveStateHandler = BackgroundThread.getHandler();
        this.mCallbackHandler = new CallbackHandler(this.mContext.getMainLooper());
        this.mBackupRestoreController = new BackupRestoreController();
        this.mSecurityPolicy = new SecurityPolicy();
        this.mIconUtilities = new IconUtilities(this.mContext);
        computeMaximumWidgetBitmapMemory();
        this.mLocale = Locale.getDefault();
        registerBroadcastReceiver();
        registerOnCrossProfileProvidersChangedListener();
        LocalServices.addService(AppWidgetManagerInternal.class, new AppWidgetManagerLocal());
    }

    private void computeMaximumWidgetBitmapMemory() {
        Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        this.mMaxWidgetBitmapMemory = 6 * size.x * size.y;
    }

    private void registerBroadcastReceiver() {
        IntentFilter configFilter = new IntentFilter();
        configFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, configFilter, null, null);
        IntentFilter cotaAppFilter = new IntentFilter();
        cotaAppFilter.addAction(COTA_APP_UPDATE_APPWIDGET);
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, cotaAppFilter, null, null);
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, packageFilter, null, null);
        IntentFilter sdFilter = new IntentFilter();
        sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
        sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, sdFilter, null, null);
        IntentFilter offModeFilter = new IntentFilter();
        offModeFilter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        offModeFilter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, offModeFilter, null, null);
        IntentFilter suspendPackageFilter = new IntentFilter();
        suspendPackageFilter.addAction("android.intent.action.PACKAGES_SUSPENDED");
        suspendPackageFilter.addAction("android.intent.action.PACKAGES_UNSUSPENDED");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, suspendPackageFilter, null, null);
    }

    private void registerOnCrossProfileProvidersChangedListener() {
        if (this.mDevicePolicyManagerInternal != null) {
            this.mDevicePolicyManagerInternal.addOnCrossProfileWidgetProvidersChangeListener(this);
        }
    }

    public void setSafeMode(boolean safeMode) {
        this.mSafeMode = safeMode;
    }

    /* access modifiers changed from: private */
    public void onConfigurationChanged() {
        if (DEBUG) {
            Slog.i(TAG, "onConfigurationChanged()");
        }
        Locale revised = Locale.getDefault();
        if (revised == null || this.mLocale == null || !revised.equals(this.mLocale)) {
            this.mLocale = revised;
            synchronized (this.mLock) {
                SparseIntArray changedGroups = null;
                ArrayList<Provider> installedProviders = new ArrayList<>(this.mProviders);
                HashSet<ProviderId> removedProviders = new HashSet<>();
                for (int i = installedProviders.size() - 1; i >= 0; i--) {
                    Provider provider = installedProviders.get(i);
                    int userId = provider.getUserId();
                    if (this.mUserManager.isUserUnlockingOrUnlocked(userId)) {
                        if (!isProfileWithLockedParent(userId)) {
                            ensureGroupStateLoadedLocked(userId);
                            if (!removedProviders.contains(provider.id) && updateProvidersForPackageLocked(provider.id.componentName.getPackageName(), provider.getUserId(), removedProviders)) {
                                if (changedGroups == null) {
                                    changedGroups = new SparseIntArray();
                                }
                                int groupId = this.mSecurityPolicy.getGroupParent(provider.getUserId());
                                changedGroups.put(groupId, groupId);
                            }
                        }
                    }
                }
                if (changedGroups != null) {
                    int groupCount = changedGroups.size();
                    for (int i2 = 0; i2 < groupCount; i2++) {
                        saveGroupStateAsync(changedGroups.get(i2));
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x00fb, code lost:
        return;
     */
    public void onPackageBroadcastReceived(Intent intent, int userId) {
        char c;
        String[] pkgList;
        String action = intent.getAction();
        boolean added = false;
        boolean changed = false;
        boolean componentsModified = false;
        boolean cotaFlag = false;
        boolean packageRemovedPermanently = true;
        int i = 0;
        switch (action.hashCode()) {
            case -1403934493:
                if (action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                    c = 3;
                    break;
                }
            case -1338021860:
                if (action.equals("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE")) {
                    c = 2;
                    break;
                }
            case -1001645458:
                if (action.equals("android.intent.action.PACKAGES_SUSPENDED")) {
                    c = 0;
                    break;
                }
            case -626041473:
                if (action.equals(COTA_APP_UPDATE_APPWIDGET)) {
                    c = 4;
                    break;
                }
            case 1290767157:
                if (action.equals("android.intent.action.PACKAGES_UNSUSPENDED")) {
                    c = 1;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
                pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                changed = true;
                break;
            case 2:
                added = true;
                break;
            case 3:
                break;
            case 4:
                pkgList = intent.getStringArrayExtra(COTA_APP_UPDATE_APPWIDGET_EXTRA);
                added = true;
                cotaFlag = true;
                break;
            default:
                Uri uri = intent.getData();
                if (uri != null) {
                    String pkgName = uri.getSchemeSpecificPart();
                    if (pkgName != null) {
                        added = "android.intent.action.PACKAGE_ADDED".equals(action);
                        changed = "android.intent.action.PACKAGE_CHANGED".equals(action);
                        pkgList = new String[]{pkgName};
                        break;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
        }
        pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
        if (pkgList != null && pkgList.length != 0) {
            synchronized (this.mLock) {
                if ((this.mUserManager.isUserUnlockingOrUnlocked(userId) && !isProfileWithLockedParent(userId)) || cotaFlag) {
                    ensureGroupStateLoadedLocked(userId, false);
                    Bundle extras = intent.getExtras();
                    if (!added) {
                        if (!changed) {
                            if (extras != null) {
                                if (extras.getBoolean("android.intent.extra.REPLACING", false)) {
                                    packageRemovedPermanently = false;
                                }
                            }
                            if (packageRemovedPermanently) {
                                int length = pkgList.length;
                                while (i < length) {
                                    componentsModified |= removeHostsAndProvidersForPackageLocked(pkgList[i], userId);
                                    i++;
                                }
                            }
                            if (componentsModified || cotaFlag) {
                                saveGroupStateAsync(userId);
                                scheduleNotifyGroupHostsForProvidersChangedLocked(userId);
                            }
                        }
                    }
                    if ((!added || (extras != null && extras.getBoolean("android.intent.extra.REPLACING", false))) && !cotaFlag) {
                        packageRemovedPermanently = false;
                    }
                    int length2 = pkgList.length;
                    while (i < length2) {
                        String pkgName2 = pkgList[i];
                        componentsModified |= updateProvidersForPackageLocked(pkgName2, userId, null);
                        if (packageRemovedPermanently && userId == 0) {
                            int uid = getUidForPackage(pkgName2, userId);
                            if (uid >= 0) {
                                resolveHostUidLocked(pkgName2, uid);
                            }
                        }
                        i++;
                    }
                    saveGroupStateAsync(userId);
                    scheduleNotifyGroupHostsForProvidersChangedLocked(userId);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reloadWidgetsMaskedStateForGroup(int userId) {
        if (this.mUserManager.isUserUnlockingOrUnlocked(userId)) {
            synchronized (this.mLock) {
                reloadWidgetsMaskedState(userId);
                for (int profileId : this.mUserManager.getEnabledProfileIds(userId)) {
                    reloadWidgetsMaskedState(profileId);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void reloadWidgetsMaskedState(int userId) {
        boolean suspended;
        long identity = Binder.clearCallingIdentity();
        try {
            UserInfo user = this.mUserManager.getUserInfo(userId);
            boolean lockedProfile = !this.mUserManager.isUserUnlockingOrUnlocked(userId);
            boolean quietProfile = user.isQuietModeEnabled();
            int N = this.mProviders.size();
            for (int i = 0; i < N; i++) {
                Provider provider = this.mProviders.get(i);
                if (provider.getUserId() == userId) {
                    boolean changed = provider.setMaskedByLockedProfileLocked(lockedProfile) | provider.setMaskedByQuietProfileLocked(quietProfile);
                    try {
                        suspended = this.mPackageManager.isPackageSuspendedForUser(provider.info.provider.getPackageName(), provider.getUserId());
                    } catch (IllegalArgumentException e) {
                        suspended = false;
                    }
                    changed |= provider.setMaskedBySuspendedPackageLocked(suspended);
                    if (changed) {
                        if (provider.isMaskedLocked()) {
                            maskWidgetsViewsLocked(provider, null);
                        } else {
                            unmaskWidgetsViewsLocked(provider);
                        }
                    }
                }
            }
            Binder.restoreCallingIdentity(identity);
        } catch (RemoteException e2) {
            Slog.e(TAG, "Failed to query application info", e2);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public void updateWidgetPackageSuspensionMaskedState(Intent intent, boolean suspended, int profileId) {
        String[] packagesArray = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
        if (packagesArray != null) {
            Set<String> packages = new ArraySet<>(Arrays.asList(packagesArray));
            synchronized (this.mLock) {
                int N = this.mProviders.size();
                for (int i = 0; i < N; i++) {
                    Provider provider = this.mProviders.get(i);
                    if (provider.getUserId() == profileId) {
                        if (packages.contains(provider.info.provider.getPackageName())) {
                            if (provider.setMaskedBySuspendedPackageLocked(suspended)) {
                                if (provider.isMaskedLocked()) {
                                    maskWidgetsViewsLocked(provider, null);
                                } else {
                                    unmaskWidgetsViewsLocked(provider);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Bitmap createMaskedWidgetBitmap(String providerPackage, int providerUserId) {
        long identity = Binder.clearCallingIdentity();
        try {
            PackageManager pm = this.mContext.createPackageContextAsUser(providerPackage, 0, UserHandle.of(providerUserId)).getPackageManager();
            Drawable icon = pm.getApplicationInfo(providerPackage, 0).loadUnbadgedIcon(pm).mutate();
            icon.setColorFilter(this.mIconUtilities.getDisabledColorFilter());
            return this.mIconUtilities.createIconBitmap(icon);
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "Fail to get application icon", e);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private RemoteViews createMaskedWidgetRemoteViews(Bitmap icon, boolean showBadge, PendingIntent onClickIntent) {
        RemoteViews views = new RemoteViews(this.mContext.getPackageName(), 17367338);
        if (icon != null) {
            views.setImageViewBitmap(16909537, icon);
        }
        if (!showBadge) {
            views.setViewVisibility(16909538, 4);
        }
        if (onClickIntent != null) {
            views.setOnClickPendingIntent(16909539, onClickIntent);
        }
        return views;
    }

    /* JADX INFO: finally extract failed */
    private void maskWidgetsViewsLocked(Provider provider, Widget targetWidget) {
        boolean showBadge;
        Intent onClickIntent;
        Intent onClickIntent2;
        Provider provider2 = provider;
        Widget widget = targetWidget;
        int widgetCount = provider2.widgets.size();
        if (widgetCount != 0) {
            String providerPackage = provider2.info.provider.getPackageName();
            int providerUserId = provider.getUserId();
            Bitmap iconBitmap = createMaskedWidgetBitmap(providerPackage, providerUserId);
            if (iconBitmap != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    if (provider2.maskedBySuspendedPackage) {
                        showBadge = this.mUserManager.getUserInfo(providerUserId).isManagedProfile();
                        String suspendingPackage = this.mPackageManagerInternal.getSuspendingPackage(providerPackage, providerUserId);
                        if ("android".equals(suspendingPackage)) {
                            onClickIntent2 = this.mDevicePolicyManagerInternal.createShowAdminSupportIntent(providerUserId, true);
                        } else {
                            onClickIntent2 = SuspendedAppActivity.createSuspendedAppInterceptIntent(providerPackage, suspendingPackage, this.mPackageManagerInternal.getSuspendedDialogMessage(providerPackage, providerUserId), providerUserId);
                        }
                        onClickIntent = onClickIntent2;
                    } else if (provider2.maskedByQuietProfile) {
                        showBadge = true;
                        onClickIntent = UnlaunchableAppActivity.createInQuietModeDialogIntent(providerUserId);
                    } else {
                        showBadge = true;
                        onClickIntent = this.mKeyguardManager.createConfirmDeviceCredentialIntent(null, null, providerUserId);
                        if (onClickIntent != null) {
                            onClickIntent.setFlags(276824064);
                        }
                    }
                    int j = 0;
                    while (j < widgetCount) {
                        Widget widget2 = provider2.widgets.get(j);
                        if (widget == null || widget == widget2) {
                            PendingIntent intent = null;
                            if (onClickIntent != null) {
                                intent = PendingIntent.getActivity(this.mContext, widget2.appWidgetId, onClickIntent, 134217728);
                            }
                            if (widget2.replaceWithMaskedViewsLocked(createMaskedWidgetRemoteViews(iconBitmap, showBadge, intent))) {
                                scheduleNotifyUpdateAppWidgetLocked(widget2, widget2.getEffectiveViewsLocked());
                            }
                        }
                        j++;
                        provider2 = provider;
                    }
                    Binder.restoreCallingIdentity(identity);
                    boolean z = showBadge;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            }
        }
    }

    private void unmaskWidgetsViewsLocked(Provider provider) {
        int widgetCount = provider.widgets.size();
        for (int j = 0; j < widgetCount; j++) {
            Widget widget = provider.widgets.get(j);
            if (widget.clearMaskedViewsLocked()) {
                scheduleNotifyUpdateAppWidgetLocked(widget, widget.getEffectiveViewsLocked());
            }
        }
    }

    private void resolveHostUidLocked(String pkg, int uid) {
        int N = this.mHosts.size();
        int i = 0;
        while (i < N) {
            Host host = this.mHosts.get(i);
            if (host.id.uid != -1 || !pkg.equals(host.id.packageName)) {
                i++;
            } else {
                if (DEBUG) {
                    Slog.i(TAG, "host " + host.id + " resolved to uid " + uid);
                }
                host.id = new HostId(uid, host.id.hostId, host.id.packageName);
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void ensureGroupStateLoadedLocked(int userId) {
        ensureGroupStateLoadedLocked(userId, true);
    }

    /* access modifiers changed from: private */
    public void ensureGroupStateLoadedLocked(int userId, boolean enforceUserUnlockingOrUnlocked) {
        if (enforceUserUnlockingOrUnlocked && !isUserRunningAndUnlocked(userId)) {
            throw new IllegalStateException("User " + userId + " must be unlocked for widgets to be available");
        } else if (!enforceUserUnlockingOrUnlocked || !isProfileWithLockedParent(userId)) {
            int[] profileIds = this.mSecurityPolicy.getEnabledGroupProfileIds(userId);
            int newMemberCount = 0;
            for (int i = 0; i < profileIdCount; i++) {
                if (this.mLoadedUserIds.indexOfKey(profileIds[i]) >= 0) {
                    profileIds[i] = -1;
                } else {
                    newMemberCount++;
                }
            }
            if (newMemberCount > 0) {
                int newMemberIndex = 0;
                int[] newProfileIds = new int[newMemberCount];
                for (int profileId : profileIds) {
                    if (profileId != -1) {
                        this.mLoadedUserIds.put(profileId, profileId);
                        newProfileIds[newMemberIndex] = profileId;
                        newMemberIndex++;
                    }
                }
                clearProvidersAndHostsTagsLocked();
                loadGroupWidgetProvidersLocked(newProfileIds);
                loadGroupStateLocked(newProfileIds);
            }
        } else {
            throw new IllegalStateException("Profile " + userId + " must have unlocked parent");
        }
    }

    private boolean isUserRunningAndUnlocked(int userId) {
        return this.mUserManager.isUserUnlockingOrUnlocked(userId);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            synchronized (this.mLock) {
                if (args.length <= 0 || !PriorityDump.PROTO_ARG.equals(args[0])) {
                    dumpInternal(pw);
                } else {
                    dumpProto(fd);
                }
            }
        }
    }

    private void dumpProto(FileDescriptor fd) {
        Slog.i(TAG, "dump proto for " + this.mWidgets.size() + " widgets");
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        int N = this.mWidgets.size();
        for (int i = 0; i < N; i++) {
            dumpProtoWidget(proto, this.mWidgets.get(i));
        }
        proto.flush();
    }

    private void dumpProtoWidget(ProtoOutputStream proto, Widget widget) {
        if (widget.host == null || widget.provider == null) {
            Slog.d(TAG, "skip dumping widget because host or provider is null: widget.host=" + widget.host + " widget.provider=" + widget.provider);
            return;
        }
        long token = proto.start(2246267895809L);
        boolean z = true;
        proto.write(1133871366145L, widget.host.getUserId() != widget.provider.getUserId());
        if (widget.host.callbacks != null) {
            z = false;
        }
        proto.write(1133871366146L, z);
        proto.write(1138166333443L, widget.host.id.packageName);
        proto.write(1138166333444L, widget.provider.id.componentName.getPackageName());
        proto.write(1138166333445L, widget.provider.id.componentName.getClassName());
        if (widget.options != null) {
            proto.write(1120986464262L, widget.options.getInt("appWidgetMinWidth", 0));
            proto.write(1120986464263L, widget.options.getInt("appWidgetMinHeight", 0));
            proto.write(1120986464264L, widget.options.getInt("appWidgetMaxWidth", 0));
            proto.write(1120986464265L, widget.options.getInt("appWidgetMaxHeight", 0));
        }
        proto.end(token);
    }

    private void dumpInternal(PrintWriter pw) {
        int N = this.mProviders.size();
        pw.println("Providers:");
        for (int i = 0; i < N; i++) {
            dumpProvider(this.mProviders.get(i), i, pw);
        }
        int N2 = this.mWidgets.size();
        pw.println(" ");
        pw.println("Widgets:");
        for (int i2 = 0; i2 < N2; i2++) {
            dumpWidget(this.mWidgets.get(i2), i2, pw);
        }
        int N3 = this.mHosts.size();
        pw.println(" ");
        pw.println("Hosts:");
        for (int i3 = 0; i3 < N3; i3++) {
            dumpHost(this.mHosts.get(i3), i3, pw);
        }
        int N4 = this.mPackagesWithBindWidgetPermission.size();
        pw.println(" ");
        pw.println("Grants:");
        for (int i4 = 0; i4 < N4; i4++) {
            dumpGrant(this.mPackagesWithBindWidgetPermission.valueAt(i4), i4, pw);
        }
    }

    public ParceledListSlice<PendingHostUpdate> startListening(IAppWidgetHost callbacks, String callingPackage, int hostId, int[] appWidgetIds) {
        HostId id;
        String str = callingPackage;
        int[] iArr = appWidgetIds;
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "startListening() " + userId);
        }
        Log.d(TAG, "startListening:callingpackage" + str);
        this.mSecurityPolicy.enforceCallFromPackage(str);
        synchronized (this.mLock) {
            try {
                if (this.mSecurityPolicy.isInstantAppLocked(str, userId)) {
                    Slog.w(TAG, "Instant package " + str + " cannot host app widgets");
                    ParceledListSlice<PendingHostUpdate> emptyList = ParceledListSlice.emptyList();
                    return emptyList;
                }
                ensureGroupStateLoadedLocked(userId);
                try {
                    HostId id2 = new HostId(Binder.getCallingUid(), hostId, str);
                    Host host = lookupOrAddHostLocked(id2);
                    host.callbacks = callbacks;
                    long updateSequenceNo = UPDATE_COUNTER.incrementAndGet();
                    int N = iArr.length;
                    ArrayList<PendingHostUpdate> outUpdates = new ArrayList<>(N);
                    LongSparseArray<PendingHostUpdate> updatesMap = new LongSparseArray<>();
                    int i = 0;
                    while (i < N) {
                        if (host.getPendingUpdatesForId(iArr[i], updatesMap)) {
                            int M = updatesMap.size();
                            int j = 0;
                            while (true) {
                                id = id2;
                                int j2 = j;
                                if (j2 >= M) {
                                    break;
                                }
                                outUpdates.add(updatesMap.valueAt(j2));
                                j = j2 + 1;
                                id2 = id;
                            }
                        } else {
                            id = id2;
                        }
                        i++;
                        id2 = id;
                    }
                    host.lastWidgetUpdateSequenceNo = updateSequenceNo;
                    ParceledListSlice<PendingHostUpdate> parceledListSlice = new ParceledListSlice<>(outUpdates);
                    return parceledListSlice;
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                IAppWidgetHost iAppWidgetHost = callbacks;
                int i2 = hostId;
                throw th;
            }
        }
    }

    public void stopListening(String callingPackage, int hostId) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "stopListening() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId, false);
            Host host = lookupHostLocked(new HostId(Binder.getCallingUid(), hostId, callingPackage));
            if (host != null) {
                host.callbacks = null;
                pruneHostLocked(host);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00a7, code lost:
        return r2;
     */
    public int allocateAppWidgetId(String callingPackage, int hostId) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "allocateAppWidgetId() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            if (this.mSecurityPolicy.isInstantAppLocked(callingPackage, userId)) {
                Slog.w(TAG, "Instant package " + callingPackage + " cannot host app widgets");
                return 0;
            }
            ensureGroupStateLoadedLocked(userId);
            if (this.mNextAppWidgetIds.indexOfKey(userId) < 0) {
                this.mNextAppWidgetIds.put(userId, 1);
            }
            int appWidgetId = incrementAndGetAppWidgetIdLocked(userId);
            Host host = lookupOrAddHostLocked(new HostId(Binder.getCallingUid(), hostId, callingPackage));
            Widget widget = new Widget();
            widget.appWidgetId = appWidgetId;
            widget.host = host;
            host.widgets.add(widget);
            addWidgetLocked(widget);
            saveGroupStateAsync(userId);
            if (DEBUG) {
                Slog.i(TAG, "Allocated widget id " + appWidgetId + " for host " + host.id);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0063, code lost:
        return;
     */
    public void deleteAppWidgetId(String callingPackage, int appWidgetId) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "deleteAppWidgetId() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Widget widget = lookupWidgetLocked(appWidgetId, Binder.getCallingUid(), callingPackage);
            if (widget != null) {
                deleteAppWidgetLocked(widget);
                saveGroupStateAsync(userId);
                if (DEBUG) {
                    Slog.i(TAG, "Deleted widget id " + appWidgetId + " for host " + widget.host.id);
                }
            }
        }
    }

    public boolean hasBindAppWidgetPermission(String packageName, int grantId) {
        if (DEBUG) {
            Slog.i(TAG, "hasBindAppWidgetPermission() " + UserHandle.getCallingUserId());
        }
        this.mSecurityPolicy.enforceModifyAppWidgetBindPermissions(packageName);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(grantId);
            if (getUidForPackage(packageName, grantId) < 0) {
                return false;
            }
            boolean contains = this.mPackagesWithBindWidgetPermission.contains(Pair.create(Integer.valueOf(grantId), packageName));
            return contains;
        }
    }

    public void setBindAppWidgetPermission(String packageName, int grantId, boolean grantPermission) {
        if (DEBUG) {
            Slog.i(TAG, "setBindAppWidgetPermission() " + UserHandle.getCallingUserId());
        }
        this.mSecurityPolicy.enforceModifyAppWidgetBindPermissions(packageName);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(grantId);
            if (getUidForPackage(packageName, grantId) >= 0) {
                Pair<Integer, String> packageId = Pair.create(Integer.valueOf(grantId), packageName);
                if (grantPermission) {
                    this.mPackagesWithBindWidgetPermission.add(packageId);
                } else {
                    this.mPackagesWithBindWidgetPermission.remove(packageId);
                }
                saveGroupStateAsync(grantId);
            }
        }
    }

    public IntentSender createAppWidgetConfigIntentSender(String callingPackage, int appWidgetId) {
        long identity;
        IntentSender intentSender;
        String str = callingPackage;
        int i = appWidgetId;
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.APPWIGDET_CREATEAPPWIDGETCONFIGINTENTSENDER);
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "createAppWidgetConfigIntentSender() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(str);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Widget widget = lookupWidgetLocked(i, Binder.getCallingUid(), str);
            if (widget != null) {
                Provider provider = widget.provider;
                if (provider != null) {
                    Intent intent = new Intent("android.appwidget.action.APPWIDGET_CONFIGURE");
                    intent.putExtra("appWidgetId", i);
                    intent.setComponent(provider.info.configure);
                    long identity2 = Binder.clearCallingIdentity();
                    try {
                        Context context = this.mContext;
                        identity = identity2;
                        try {
                            intentSender = PendingIntent.getActivityAsUser(context, 0, intent, 1409286144, null, new UserHandle(provider.getUserId())).getIntentSender();
                            Binder.restoreCallingIdentity(identity);
                        } catch (Throwable th) {
                            th = th;
                            Binder.restoreCallingIdentity(identity);
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        identity = identity2;
                        Binder.restoreCallingIdentity(identity);
                        throw th;
                    }
                } else {
                    throw new IllegalArgumentException("Widget not bound " + i);
                }
            } else {
                throw new IllegalArgumentException("Bad widget id " + i);
            }
        }
        return intentSender;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:56:0x019b, code lost:
        return true;
     */
    public boolean bindAppWidgetId(String callingPackage, int appWidgetId, int providerProfileId, ComponentName providerComponent, Bundle options) {
        String str = callingPackage;
        int i = appWidgetId;
        int i2 = providerProfileId;
        ComponentName componentName = providerComponent;
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.APPWIGDET_BINDAPPWIDGETID);
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "bindAppWidgetId() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(str);
        if (!this.mSecurityPolicy.isEnabledGroupProfile(i2) || !this.mSecurityPolicy.isProviderInCallerOrInProfileAndWhitelListed(providerComponent.getPackageName(), i2)) {
            return false;
        }
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            if (!this.mSecurityPolicy.hasCallerBindPermissionOrBindWhiteListedLocked(str)) {
                return false;
            }
            Widget widget = lookupWidgetLocked(i, Binder.getCallingUid(), str);
            if (widget == null) {
                Slog.e(TAG, "Bad widget id " + i);
                return false;
            } else if (widget.provider != null) {
                Slog.e(TAG, "Widget id " + i + " already bound to: " + widget.provider.id);
                return false;
            } else {
                int providerUid = getUidForPackage(providerComponent.getPackageName(), i2);
                if (providerUid < 0) {
                    Slog.e(TAG, "Package " + providerComponent.getPackageName() + " not installed  for profile " + i2);
                    return false;
                }
                Provider provider = lookupProviderLocked(new ProviderId(providerUid, componentName));
                if (provider == null) {
                    Slog.e(TAG, "No widget provider " + componentName + " for profile " + i2);
                    return false;
                } else if (provider.zombie) {
                    Slog.e(TAG, "Can't bind to a 3rd party provider in safe mode " + provider);
                    return false;
                } else {
                    widget.provider = provider;
                    widget.options = options != null ? cloneIfLocalBinder(options) : new Bundle();
                    if (!widget.options.containsKey("appWidgetCategory")) {
                        widget.options.putInt("appWidgetCategory", 1);
                    }
                    provider.widgets.add(widget);
                    onWidgetProviderAddedOrChangedLocked(widget);
                    if (provider.widgets.size() == 1) {
                        LogPower.push(168, providerComponent.getPackageName(), String.valueOf(providerUid));
                        sendEnableIntentLocked(provider);
                    }
                    sendUpdateIntentLocked(provider, new int[]{i});
                    registerForBroadcastsLocked(provider, getWidgetIds(provider.widgets));
                    saveGroupStateAsync(userId);
                    if (DEBUG) {
                        Slog.i(TAG, "Bound widget " + i + " to provider " + provider.id);
                    }
                }
            }
        }
    }

    public int[] getAppWidgetIds(ComponentName componentName) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "getAppWidgetIds() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(componentName.getPackageName());
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Provider provider = lookupProviderLocked(new ProviderId(Binder.getCallingUid(), componentName));
            if (provider != null) {
                int[] widgetIds = getWidgetIds(provider.widgets);
                return widgetIds;
            }
            int[] iArr = new int[0];
            return iArr;
        }
    }

    public int[] getAppWidgetIdsForHost(String callingPackage, int hostId) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "getAppWidgetIdsForHost() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Host host = lookupHostLocked(new HostId(Binder.getCallingUid(), hostId, callingPackage));
            if (host != null) {
                int[] widgetIds = getWidgetIds(host.widgets);
                return widgetIds;
            }
            int[] iArr = new int[0];
            return iArr;
        }
    }

    public boolean bindRemoteViewsService(String callingPackage, int appWidgetId, Intent intent, IApplicationThread caller, IBinder activtiyToken, IServiceConnection connection, int flags) {
        Object obj;
        long callingIdentity;
        Widget widget;
        int i = appWidgetId;
        Intent intent2 = intent;
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "bindRemoteViewsService() " + userId);
        }
        Object obj2 = this.mLock;
        synchronized (obj2) {
            try {
                ensureGroupStateLoadedLocked(userId);
                Widget widget2 = lookupWidgetLocked(i, Binder.getCallingUid(), callingPackage);
                if (widget2 == null) {
                    int i2 = userId;
                    Object obj3 = obj2;
                    throw new IllegalArgumentException("Bad widget id");
                } else if (widget2.provider != null) {
                    ComponentName componentName = intent.getComponent();
                    String providerPackage = widget2.provider.id.componentName.getPackageName();
                    String servicePackage = componentName.getPackageName();
                    if (servicePackage.equals(providerPackage)) {
                        this.mSecurityPolicy.enforceServiceExistsAndRequiresBindRemoteViewsPermission(componentName, widget2.provider.getUserId());
                        long callingIdentity2 = Binder.clearCallingIdentity();
                        try {
                            int i3 = userId;
                            obj = obj2;
                            callingIdentity = callingIdentity2;
                            String str = servicePackage;
                            String str2 = providerPackage;
                            ComponentName componentName2 = componentName;
                            widget = widget2;
                        } catch (RemoteException e) {
                            String str3 = servicePackage;
                            String str4 = providerPackage;
                            ComponentName componentName3 = componentName;
                            Widget widget3 = widget2;
                            int i4 = userId;
                            obj = obj2;
                            callingIdentity = callingIdentity2;
                            Binder.restoreCallingIdentity(callingIdentity);
                            return false;
                        } catch (Throwable th) {
                            th = th;
                            String str5 = servicePackage;
                            String str6 = providerPackage;
                            ComponentName componentName4 = componentName;
                            Widget widget4 = widget2;
                            int i5 = userId;
                            Object obj4 = obj2;
                            callingIdentity = callingIdentity2;
                            Binder.restoreCallingIdentity(callingIdentity);
                            throw th;
                        }
                        try {
                            if (ActivityManager.getService().bindService(caller, activtiyToken, intent2, intent2.resolveTypeIfNeeded(this.mContext.getContentResolver()), connection, flags, this.mContext.getOpPackageName(), widget2.provider.getUserId()) != 0) {
                                incrementAppWidgetServiceRefCount(i, Pair.create(Integer.valueOf(widget.provider.id.uid), new Intent.FilterComparison(intent2)));
                                Binder.restoreCallingIdentity(callingIdentity);
                                return true;
                            }
                            Binder.restoreCallingIdentity(callingIdentity);
                            return false;
                        } catch (RemoteException e2) {
                            Binder.restoreCallingIdentity(callingIdentity);
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } else {
                        String str7 = servicePackage;
                        String str8 = providerPackage;
                        ComponentName componentName5 = componentName;
                        Widget widget5 = widget2;
                        int i6 = userId;
                        Object obj5 = obj2;
                        throw new SecurityException("The taget service not in the same package as the widget provider");
                    }
                } else {
                    int i7 = userId;
                    Object obj6 = obj2;
                    throw new IllegalArgumentException("No provider for widget " + i);
                }
            } catch (Throwable th3) {
                th = th3;
                int i8 = userId;
                obj = obj2;
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005d, code lost:
        return;
     */
    public void deleteHost(String callingPackage, int hostId) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "deleteHost() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Host host = lookupHostLocked(new HostId(Binder.getCallingUid(), hostId, callingPackage));
            if (host != null) {
                deleteHostLocked(host);
                saveGroupStateAsync(userId);
                if (DEBUG) {
                    Slog.i(TAG, "Deleted host " + host.id);
                }
            }
        }
    }

    public void deleteAllHosts() {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "deleteAllHosts() " + userId);
        }
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            boolean changed = false;
            for (int i = this.mHosts.size() - 1; i >= 0; i--) {
                Host host = this.mHosts.get(i);
                if (host.id.uid == Binder.getCallingUid()) {
                    deleteHostLocked(host);
                    changed = true;
                    if (DEBUG) {
                        Slog.i(TAG, "Deleted host " + host.id);
                    }
                }
            }
            if (changed) {
                saveGroupStateAsync(userId);
            }
        }
    }

    public AppWidgetProviderInfo getAppWidgetInfo(String callingPackage, int appWidgetId) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "getAppWidgetInfo() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Widget widget = lookupWidgetLocked(appWidgetId, Binder.getCallingUid(), callingPackage);
            if (widget == null || widget.provider == null || widget.provider.zombie) {
                return null;
            }
            resetAppWidgetProviderInfo(widget.provider.info);
            AppWidgetProviderInfo cloneIfLocalBinder = cloneIfLocalBinder(widget.provider.info);
            return cloneIfLocalBinder;
        }
    }

    private void resetAppWidgetProviderInfo(AppWidgetProviderInfo info) {
        if (info != null && info.providerInfo != null && info.providerInfo.applicationInfo != null) {
            long origId = Binder.clearCallingIdentity();
            try {
                PackageManager pm = this.mContext.getPackageManager();
                String packageName = info.providerInfo.applicationInfo.packageName;
                Slog.d(TAG, "resetAppWidgetProviderInfo " + packageName);
                info.providerInfo.applicationInfo = pm.getApplicationInfoAsUser(packageName, 0, UserHandle.getUserId(info.providerInfo.applicationInfo.uid));
            } catch (PackageManager.NameNotFoundException e) {
                Slog.w(TAG, "resetAppWidgetProviderInfo fail ");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
            Binder.restoreCallingIdentity(origId);
        }
    }

    public RemoteViews getAppWidgetViews(String callingPackage, int appWidgetId) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "getAppWidgetViews() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Widget widget = lookupWidgetLocked(appWidgetId, Binder.getCallingUid(), callingPackage);
            if (widget == null) {
                return null;
            }
            RemoteViews cloneIfLocalBinder = cloneIfLocalBinder(widget.getEffectiveViewsLocked());
            return cloneIfLocalBinder;
        }
    }

    public void updateAppWidgetOptions(String callingPackage, int appWidgetId, Bundle options) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "updateAppWidgetOptions() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Widget widget = lookupWidgetLocked(appWidgetId, Binder.getCallingUid(), callingPackage);
            if (widget != null) {
                widget.options.putAll(options);
                sendOptionsChangedIntentLocked(widget);
                saveGroupStateAsync(userId);
                updateWidgetOptionsReport(userId, widget);
                LogPower.push(DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE, callingPackage);
            }
        }
    }

    public Bundle getAppWidgetOptions(String callingPackage, int appWidgetId) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "getAppWidgetOptions() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Widget widget = lookupWidgetLocked(appWidgetId, Binder.getCallingUid(), callingPackage);
            if (widget == null || widget.options == null) {
                Bundle bundle = Bundle.EMPTY;
                return bundle;
            }
            Bundle cloneIfLocalBinder = cloneIfLocalBinder(widget.options);
            return cloneIfLocalBinder;
        }
    }

    public void updateAppWidgetIds(String callingPackage, int[] appWidgetIds, RemoteViews views) {
        if (DEBUG) {
            Slog.i(TAG, "updateAppWidgetIds() " + UserHandle.getCallingUserId());
        }
        updateAppWidgetIds(callingPackage, appWidgetIds, views, false);
    }

    public void partiallyUpdateAppWidgetIds(String callingPackage, int[] appWidgetIds, RemoteViews views) {
        if (DEBUG) {
            Slog.i(TAG, "partiallyUpdateAppWidgetIds() " + UserHandle.getCallingUserId());
        }
        updateAppWidgetIds(callingPackage, appWidgetIds, views, true);
    }

    public void notifyAppWidgetViewDataChanged(String callingPackage, int[] appWidgetIds, int viewId) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "notifyAppWidgetViewDataChanged() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        if (appWidgetIds != null && appWidgetIds.length != 0) {
            synchronized (this.mLock) {
                ensureGroupStateLoadedLocked(userId);
                for (int appWidgetId : appWidgetIds) {
                    Widget widget = lookupWidgetLocked(appWidgetId, Binder.getCallingUid(), callingPackage);
                    if (widget != null) {
                        scheduleNotifyAppWidgetViewDataChanged(widget, viewId);
                        LogPower.push(DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE, callingPackage);
                        this.mAWSIProxy.updateWidgetFlushReport(userId, callingPackage);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0081, code lost:
        return;
     */
    public void updateAppWidgetProvider(ComponentName componentName, RemoteViews views) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "updateAppWidgetProvider() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(componentName.getPackageName());
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Provider provider = lookupProviderLocked(new ProviderId(Binder.getCallingUid(), componentName));
            if (provider == null) {
                Slog.w(TAG, "Provider doesn't exist " + providerId);
                return;
            }
            ArrayList<Widget> instances = provider.widgets;
            int N = instances.size();
            for (int i = 0; i < N; i++) {
                updateAppWidgetInstanceLocked(instances.get(i), views, false);
            }
            if (componentName != null) {
                LogPower.push(DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE, componentName.getPackageName());
                this.mAWSIProxy.updateWidgetFlushReport(userId, componentName.getPackageName());
            }
        }
    }

    public void updateAppWidgetProviderInfo(ComponentName componentName, String metadataKey) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "updateAppWidgetProvider() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(componentName.getPackageName());
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            ProviderId providerId = new ProviderId(Binder.getCallingUid(), componentName);
            Provider provider = lookupProviderLocked(providerId);
            if (provider == null) {
                throw new IllegalArgumentException(componentName + " is not a valid AppWidget provider");
            } else if (!Objects.equals(provider.infoTag, metadataKey)) {
                AppWidgetProviderInfo info = parseAppWidgetProviderInfo(providerId, provider.info.providerInfo, metadataKey == null ? "android.appwidget.provider" : metadataKey);
                if (info != null) {
                    provider.info = info;
                    provider.infoTag = metadataKey;
                    int N = provider.widgets.size();
                    for (int i = 0; i < N; i++) {
                        Widget widget = provider.widgets.get(i);
                        scheduleNotifyProviderChangedLocked(widget);
                        updateAppWidgetInstanceLocked(widget, widget.views, false);
                    }
                    saveGroupStateAsync(userId);
                    scheduleNotifyGroupHostsForProvidersChangedLocked(userId);
                    return;
                }
                throw new IllegalArgumentException("Unable to parse " + keyToUse + " meta-data to a valid AppWidget provider");
            }
        }
    }

    public boolean isRequestPinAppWidgetSupported() {
        synchronized (this.mLock) {
            if (!this.mSecurityPolicy.isCallerInstantAppLocked()) {
                return ((ShortcutServiceInternal) LocalServices.getService(ShortcutServiceInternal.class)).isRequestPinItemSupported(UserHandle.getCallingUserId(), 2);
            }
            Slog.w(TAG, "Instant uid " + Binder.getCallingUid() + " query information about app widgets");
            return false;
        }
    }

    public boolean requestPinAppWidget(String callingPackage, ComponentName componentName, Bundle extras, IntentSender resultSender) {
        int callingUid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(callingUid);
        if (DEBUG) {
            Slog.i(TAG, "requestPinAppWidget() " + userId);
        }
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Provider provider = lookupProviderLocked(new ProviderId(callingUid, componentName));
            if (provider != null) {
                if (!provider.zombie) {
                    AppWidgetProviderInfo info = provider.info;
                    if ((info.widgetCategory & 1) == 0) {
                        return false;
                    }
                    return ((ShortcutServiceInternal) LocalServices.getService(ShortcutServiceInternal.class)).requestPinAppWidget(callingPackage, info, extras, resultSender, userId);
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0099  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x009a  */
    public ParceledListSlice<AppWidgetProviderInfo> getInstalledProvidersForProfile(int categoryFilter, int profileId, String packageName) {
        boolean inPackage;
        int i = profileId;
        String str = packageName;
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "getInstalledProvidersForProfiles() " + userId);
        }
        if (!this.mSecurityPolicy.isEnabledGroupProfile(i)) {
            return null;
        }
        synchronized (this.mLock) {
            if (this.mSecurityPolicy.isCallerInstantAppLocked()) {
                Slog.w(TAG, "Instant uid " + Binder.getCallingUid() + " cannot access widget providers");
                ParceledListSlice<AppWidgetProviderInfo> emptyList = ParceledListSlice.emptyList();
                return emptyList;
            }
            ensureGroupStateLoadedLocked(userId);
            ArrayList<AppWidgetProviderInfo> result = new ArrayList<>();
            int providerCount = this.mProviders.size();
            for (int i2 = 0; i2 < providerCount; i2++) {
                Provider provider = this.mProviders.get(i2);
                AppWidgetProviderInfo info = provider.info;
                if (str != null) {
                    if (!provider.id.componentName.getPackageName().equals(str)) {
                        inPackage = false;
                        if (!provider.zombie && (info.widgetCategory & categoryFilter) != 0) {
                            if (!inPackage) {
                                ComponentName cn = info.provider;
                                if (!HIDE_HUAWEI_WEATHER_WIDGET || cn == null || !HIDDEN_WEATHER_WIDGETS.containsKey(cn.getClassName()) || !isThirdPartyLauncherActive()) {
                                    int providerProfileId = info.getProfile().getIdentifier();
                                    if (providerProfileId == i && this.mSecurityPolicy.isProviderInCallerOrInProfileAndWhitelListed(provider.id.componentName.getPackageName(), providerProfileId)) {
                                        result.add(cloneIfLocalBinder(info));
                                    }
                                }
                            }
                        }
                    }
                }
                inPackage = true;
                if (!inPackage) {
                }
            }
            ParceledListSlice<AppWidgetProviderInfo> parceledListSlice = new ParceledListSlice<>(result);
            return parceledListSlice;
        }
    }

    private boolean isThirdPartyLauncherActive() {
        boolean isThridPartylauncherActive = false;
        Intent launcherIntent = new Intent("android.intent.action.MAIN");
        launcherIntent.addCategory("android.intent.category.HOME");
        List<ComponentName> prefActList = new ArrayList<>();
        List<IntentFilter> intentList = new ArrayList<>();
        PackageManager mPm = this.mContext.getPackageManager();
        if (mPm == null) {
            return false;
        }
        int userId = UserHandle.getCallingUserId();
        long origId = Binder.clearCallingIdentity();
        List<ResolveInfo> list = null;
        try {
            list = mPm.queryIntentActivitiesAsUser(launcherIntent, 0, userId);
        } catch (Exception e) {
            Log.e(TAG, "isThirdPartyLauncherActive Exception:" + e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
        Binder.restoreCallingIdentity(origId);
        if (list == null) {
            return false;
        }
        Iterator<ResolveInfo> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ResolveInfo info = it.next();
            mPm.getPreferredActivities(intentList, prefActList, info.activityInfo.packageName);
            if (prefActList.size() > 0) {
                if (HUAWEI_LAUNCHER_PACKAGE.equals(info.activityInfo.packageName)) {
                    isThridPartylauncherActive = false;
                } else {
                    isThridPartylauncherActive = true;
                }
            }
        }
        return isThridPartylauncherActive;
    }

    private void updateAppWidgetIds(String callingPackage, int[] appWidgetIds, RemoteViews views, boolean partially) {
        int userId = UserHandle.getCallingUserId();
        if (appWidgetIds != null && appWidgetIds.length != 0) {
            this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
            synchronized (this.mLock) {
                ensureGroupStateLoadedLocked(userId);
                for (int appWidgetId : appWidgetIds) {
                    Widget widget = lookupWidgetLocked(appWidgetId, Binder.getCallingUid(), callingPackage);
                    if (widget != null) {
                        updateAppWidgetInstanceLocked(widget, views, partially);
                        LogPower.push(DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE, callingPackage);
                        this.mAWSIProxy.updateWidgetFlushReport(userId, callingPackage);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public int incrementAndGetAppWidgetIdLocked(int userId) {
        int appWidgetId = peekNextAppWidgetIdLocked(userId) + 1;
        this.mNextAppWidgetIds.put(userId, appWidgetId);
        return appWidgetId;
    }

    private void setMinAppWidgetIdLocked(int userId, int minWidgetId) {
        if (peekNextAppWidgetIdLocked(userId) < minWidgetId) {
            this.mNextAppWidgetIds.put(userId, minWidgetId);
        }
    }

    private int peekNextAppWidgetIdLocked(int userId) {
        if (this.mNextAppWidgetIds.indexOfKey(userId) < 0) {
            return 1;
        }
        return this.mNextAppWidgetIds.get(userId);
    }

    /* access modifiers changed from: private */
    public Host lookupOrAddHostLocked(HostId id) {
        Host host = lookupHostLocked(id);
        if (host != null) {
            return host;
        }
        Host host2 = new Host();
        host2.id = id;
        this.mHosts.add(host2);
        return host2;
    }

    private void deleteHostLocked(Host host) {
        for (int i = host.widgets.size() - 1; i >= 0; i--) {
            deleteAppWidgetLocked(host.widgets.remove(i));
        }
        this.mHosts.remove(host);
        host.callbacks = null;
    }

    private void deleteAppWidgetLocked(Widget widget) {
        decrementAppWidgetServiceRefCount(widget);
        Host host = widget.host;
        host.widgets.remove(widget);
        pruneHostLocked(host);
        removeWidgetLocked(widget);
        Provider provider = widget.provider;
        if (provider != null) {
            provider.widgets.remove(widget);
            if (!provider.zombie) {
                sendDeletedIntentLocked(widget);
                if (provider.widgets.isEmpty()) {
                    cancelBroadcasts(provider);
                    sendDisabledIntentLocked(provider);
                }
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private void cancelBroadcasts(Provider provider) {
        if (DEBUG) {
            Slog.i(TAG, "cancelBroadcasts() for " + provider);
        }
        if (provider.broadcast != null) {
            this.mAlarmManager.cancel(provider.broadcast);
            long token = Binder.clearCallingIdentity();
            try {
                provider.broadcast.cancel();
                Binder.restoreCallingIdentity(token);
                provider.broadcast = null;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }
    }

    private void destroyRemoteViewsService(final Intent intent, Widget widget) {
        ServiceConnection conn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                try {
                    IRemoteViewsFactory.Stub.asInterface(service).onDestroy(intent);
                } catch (RemoteException re) {
                    Slog.e(AppWidgetServiceImpl.TAG, "Error calling remove view factory", re);
                }
                AppWidgetServiceImpl.this.mContext.unbindService(this);
            }

            public void onServiceDisconnected(ComponentName name) {
            }
        };
        long token = Binder.clearCallingIdentity();
        try {
            this.mContext.bindServiceAsUser(intent, conn, 33554433, widget.provider.info.getProfile());
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void incrementAppWidgetServiceRefCount(int appWidgetId, Pair<Integer, Intent.FilterComparison> serviceId) {
        HashSet<Integer> appWidgetIds;
        if (this.mRemoteViewsServicesAppWidgets.containsKey(serviceId)) {
            appWidgetIds = this.mRemoteViewsServicesAppWidgets.get(serviceId);
        } else {
            appWidgetIds = new HashSet<>();
            this.mRemoteViewsServicesAppWidgets.put(serviceId, appWidgetIds);
        }
        appWidgetIds.add(Integer.valueOf(appWidgetId));
    }

    /* access modifiers changed from: private */
    public void decrementAppWidgetServiceRefCount(Widget widget) {
        Iterator<Pair<Integer, Intent.FilterComparison>> it = this.mRemoteViewsServicesAppWidgets.keySet().iterator();
        while (it.hasNext()) {
            Pair<Integer, Intent.FilterComparison> key = it.next();
            HashSet<Integer> ids = this.mRemoteViewsServicesAppWidgets.get(key);
            if (ids.remove(Integer.valueOf(widget.appWidgetId)) && ids.isEmpty()) {
                destroyRemoteViewsService(((Intent.FilterComparison) key.second).getIntent(), widget);
                it.remove();
            }
        }
    }

    /* access modifiers changed from: private */
    public void saveGroupStateAsync(int groupId) {
        this.mSaveStateHandler.post(new SaveStateRunnable(groupId));
    }

    private void updateAppWidgetInstanceLocked(Widget widget, RemoteViews views, boolean isPartialUpdate) {
        RemoteViews oldRemoteViews = null;
        if (widget != null && widget.provider != null && !widget.provider.zombie && !widget.host.zombie) {
            if (!isPartialUpdate || widget.views == null) {
                if (!(widget.views == null || widget.views == views)) {
                    oldRemoteViews = widget.views;
                }
                widget.views = views;
            } else {
                widget.views.mergeRemoteViews(views);
            }
            if (!(UserHandle.getAppId(Binder.getCallingUid()) == 1000 || widget.views == null)) {
                int estimateMemoryUsage = widget.views.estimateMemoryUsage();
                int memoryUsage = estimateMemoryUsage;
                if (estimateMemoryUsage > this.mMaxWidgetBitmapMemory) {
                    widget.views = null;
                    throw new IllegalArgumentException("RemoteViews for widget update exceeds maximum bitmap memory usage (used: " + memoryUsage + ", max: " + this.mMaxWidgetBitmapMemory + ")");
                }
            }
            scheduleNotifyUpdateAppWidgetLocked(widget, widget.getEffectiveViewsLocked());
            if (oldRemoteViews != null) {
                Slog.i(TAG, "recycle oldRemoteView");
                scheduleRecyleRemoteView(oldRemoteViews);
            }
        }
    }

    private void scheduleNotifyAppWidgetViewDataChanged(Widget widget, int viewId) {
        if (viewId != 0 && viewId != 1) {
            long requestId = UPDATE_COUNTER.incrementAndGet();
            if (widget != null) {
                widget.updateSequenceNos.put(viewId, requestId);
            }
            if (widget != null && widget.host != null && !widget.host.zombie && widget.host.callbacks != null && widget.provider != null && !widget.provider.zombie) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = widget.host;
                args.arg2 = widget.host.callbacks;
                args.arg3 = Long.valueOf(requestId);
                args.argi1 = widget.appWidgetId;
                args.argi2 = viewId;
                this.mCallbackHandler.obtainMessage(4, args).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleNotifyAppWidgetViewDataChanged(Host host, IAppWidgetHost callbacks, int appWidgetId, int viewId, long requestId) {
        try {
            callbacks.viewDataChanged(appWidgetId, viewId);
            host.lastWidgetUpdateSequenceNo = requestId;
        } catch (RemoteException e) {
            callbacks = null;
        }
        synchronized (this.mLock) {
            if (callbacks == null) {
                try {
                    host.callbacks = null;
                    for (Pair<Integer, Intent.FilterComparison> key : this.mRemoteViewsServicesAppWidgets.keySet()) {
                        if (this.mRemoteViewsServicesAppWidgets.get(key).contains(Integer.valueOf(appWidgetId))) {
                            bindService(((Intent.FilterComparison) key.second).getIntent(), new ServiceConnection() {
                                public void onServiceConnected(ComponentName name, IBinder service) {
                                    try {
                                        IRemoteViewsFactory.Stub.asInterface(service).onDataSetChangedAsync();
                                    } catch (RemoteException e) {
                                        Slog.e(AppWidgetServiceImpl.TAG, "Error calling onDataSetChangedAsync()", e);
                                    }
                                    AppWidgetServiceImpl.this.mContext.unbindService(this);
                                }

                                public void onServiceDisconnected(ComponentName name) {
                                }
                            }, new UserHandle(UserHandle.getUserId(((Integer) key.first).intValue())));
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    private void scheduleNotifyUpdateAppWidgetLocked(Widget widget, RemoteViews updateViews) {
        long requestId = UPDATE_COUNTER.incrementAndGet();
        if (widget != null) {
            widget.updateSequenceNos.put(0, requestId);
        }
        if (widget == null || widget.provider == null || widget.provider.zombie || widget.host.callbacks == null || widget.host.zombie) {
            Slog.i(TAG, "Widget is null when scheduleNotifyUpdateAppWidgetLocked");
            return;
        }
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = widget.host;
        args.arg2 = widget.host.callbacks;
        args.arg3 = updateViews != null ? updateViews.clone() : null;
        args.arg4 = Long.valueOf(requestId);
        args.argi1 = widget.appWidgetId;
        this.mCallbackHandler.obtainMessage(1, args).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public void handleNotifyUpdateAppWidget(Host host, IAppWidgetHost callbacks, int appWidgetId, RemoteViews views, long requestId) {
        try {
            callbacks.updateAppWidget(appWidgetId, views);
            host.lastWidgetUpdateSequenceNo = requestId;
        } catch (RemoteException re) {
            synchronized (this.mLock) {
                Slog.e(TAG, "Widget host dead: " + host.id, re);
                host.callbacks = null;
            }
        }
    }

    private void scheduleNotifyProviderChangedLocked(Widget widget) {
        long requestId = UPDATE_COUNTER.incrementAndGet();
        if (widget != null) {
            widget.updateSequenceNos.clear();
            widget.updateSequenceNos.append(1, requestId);
        }
        if (widget == null || widget.provider == null || widget.provider.zombie || widget.host.callbacks == null || widget.host.zombie) {
            Slog.i(TAG, "widget may be null when scheduleNotifyProviderChangedLocked");
            return;
        }
        if (!(widget.provider.info == null || widget.provider.info.providerInfo == null || widget.provider.info.providerInfo.applicationInfo == null || !"com.huawei.health".equals(widget.provider.info.providerInfo.packageName))) {
            Slog.i(TAG, "scheduleNotifyProviderChangedLocked, sourceDir=" + widget.provider.info.providerInfo.applicationInfo.sourceDir);
        }
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = widget.host;
        args.arg2 = widget.host.callbacks;
        args.arg3 = widget.provider.info;
        args.arg4 = Long.valueOf(requestId);
        args.argi1 = widget.appWidgetId;
        this.mCallbackHandler.obtainMessage(2, args).sendToTarget();
    }

    private void scheduleRecyleRemoteView(RemoteViews views) {
        if (views != null) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = views;
            this.mCallbackHandler.obtainMessage(20, args).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public void handleNotifyProviderChanged(Host host, IAppWidgetHost callbacks, int appWidgetId, AppWidgetProviderInfo info, long requestId) {
        try {
            callbacks.providerChanged(appWidgetId, info);
            host.lastWidgetUpdateSequenceNo = requestId;
        } catch (RemoteException re) {
            synchronized (this.mLock) {
                Slog.e(TAG, "Widget host dead: " + host.id, re);
                host.callbacks = null;
            }
        }
    }

    private void scheduleNotifyGroupHostsForProvidersChangedLocked(int userId) {
        int[] profileIds = this.mSecurityPolicy.getEnabledGroupProfileIds(userId);
        for (int i = this.mHosts.size() - 1; i >= 0; i--) {
            Host host = this.mHosts.get(i);
            boolean hostInGroup = false;
            int M = profileIds.length;
            int j = 0;
            while (true) {
                if (j >= M) {
                    break;
                }
                if (host.getUserId() == profileIds[j]) {
                    hostInGroup = true;
                    break;
                }
                j++;
            }
            if (hostInGroup && host != null && !host.zombie && host.callbacks != null) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = host;
                args.arg2 = host.callbacks;
                this.mCallbackHandler.obtainMessage(3, args).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public void handleNotifyProvidersChanged(Host host, IAppWidgetHost callbacks) {
        try {
            callbacks.providersChanged();
        } catch (RemoteException re) {
            synchronized (this.mLock) {
                Slog.e(TAG, "Widget host dead: " + host.id, re);
                host.callbacks = null;
            }
        }
    }

    private static boolean isLocalBinder() {
        return Process.myPid() == Binder.getCallingPid();
    }

    /* access modifiers changed from: private */
    public static RemoteViews cloneIfLocalBinder(RemoteViews rv) {
        if (!isLocalBinder() || rv == null) {
            return rv;
        }
        return rv.clone();
    }

    private static AppWidgetProviderInfo cloneIfLocalBinder(AppWidgetProviderInfo info) {
        if (!isLocalBinder() || info == null) {
            return info;
        }
        return info.clone();
    }

    private static Bundle cloneIfLocalBinder(Bundle bundle) {
        if (!isLocalBinder() || bundle == null) {
            return bundle;
        }
        return (Bundle) bundle.clone();
    }

    private Widget lookupWidgetLocked(int appWidgetId, int uid, String packageName) {
        int N = this.mWidgets.size();
        for (int i = 0; i < N; i++) {
            Widget widget = this.mWidgets.get(i);
            if (widget.appWidgetId == appWidgetId && this.mSecurityPolicy.canAccessAppWidget(widget, uid, packageName)) {
                return widget;
            }
        }
        return null;
    }

    private Provider lookupProviderLocked(ProviderId id) {
        int N = this.mProviders.size();
        for (int i = 0; i < N; i++) {
            Provider provider = this.mProviders.get(i);
            if (provider.id.equals(id)) {
                return provider;
            }
        }
        return null;
    }

    private Host lookupHostLocked(HostId hostId) {
        int N = this.mHosts.size();
        for (int i = 0; i < N; i++) {
            Host host = this.mHosts.get(i);
            if (host.id.equals(hostId)) {
                return host;
            }
        }
        return null;
    }

    private void pruneHostLocked(Host host) {
        if (host.widgets.size() == 0 && host.callbacks == null) {
            if (DEBUG) {
                Slog.i(TAG, "Pruning host " + host.id);
            }
            this.mHosts.remove(host);
        }
    }

    private void loadGroupWidgetProvidersLocked(int[] profileIds) {
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
        List<ResolveInfo> allReceivers = null;
        for (int profileId : profileIds) {
            List<ResolveInfo> receivers = queryIntentReceivers(intent, profileId);
            if (receivers != null && !receivers.isEmpty()) {
                if (allReceivers == null) {
                    allReceivers = new ArrayList<>();
                }
                allReceivers.addAll(receivers);
            }
        }
        int N = allReceivers == null ? 0 : allReceivers.size();
        for (int i = 0; i < N; i++) {
            addProviderLocked(allReceivers.get(i));
        }
    }

    private boolean addProviderLocked(ResolveInfo ri) {
        if ((ri.activityInfo.applicationInfo.flags & 262144) != 0) {
            return false;
        }
        if (!ri.activityInfo.isEnabled() && !MUSlIM_APP_WIDGET_PACKAGE.equals(ri.activityInfo.name)) {
            return false;
        }
        ComponentName componentName = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
        ProviderId providerId = new ProviderId(ri.activityInfo.applicationInfo.uid, componentName);
        Provider provider = parseProviderInfoXml(providerId, ri, null);
        if (provider == null) {
            return false;
        }
        Provider existing = lookupProviderLocked(providerId);
        if (existing == null) {
            existing = lookupProviderLocked(new ProviderId(-1, componentName));
        }
        if (existing == null) {
            this.mProviders.add(provider);
        } else if (existing.zombie && !this.mSafeMode) {
            existing.id = providerId;
            existing.zombie = false;
            existing.info = provider.info;
            if (DEBUG) {
                Slog.i(TAG, "Provider placeholder now reified: " + existing);
            }
        }
        return true;
    }

    private void deleteWidgetsLocked(Provider provider, int userId) {
        for (int i = provider.widgets.size() - 1; i >= 0; i--) {
            Widget widget = provider.widgets.get(i);
            if (userId == -1 || userId == widget.host.getUserId()) {
                provider.widgets.remove(i);
                updateAppWidgetInstanceLocked(widget, null, false);
                widget.host.widgets.remove(widget);
                removeWidgetLocked(widget);
                widget.provider = null;
                pruneHostLocked(widget.host);
                widget.host = null;
            }
        }
    }

    private void deleteProviderLocked(Provider provider) {
        deleteWidgetsLocked(provider, -1);
        this.mProviders.remove(provider);
        cancelBroadcasts(provider);
    }

    private void sendEnableIntentLocked(Provider p) {
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_ENABLED");
        intent.setComponent(p.info.provider);
        sendBroadcastAsUser(intent, p.info.getProfile());
    }

    private void sendUpdateIntentLocked(Provider provider, int[] appWidgetIds) {
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
        intent.putExtra("appWidgetIds", appWidgetIds);
        intent.setComponent(provider.info.provider);
        sendBroadcastAsUser(intent, provider.info.getProfile());
    }

    private void sendDeletedIntentLocked(Widget widget) {
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_DELETED");
        intent.setComponent(widget.provider.info.provider);
        intent.putExtra("appWidgetId", widget.appWidgetId);
        sendBroadcastAsUser(intent, widget.provider.info.getProfile());
    }

    private void sendDisabledIntentLocked(Provider provider) {
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_DISABLED");
        intent.setComponent(provider.info.provider);
        sendBroadcastAsUser(intent, provider.info.getProfile());
    }

    public void sendOptionsChangedIntentLocked(Widget widget) {
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_UPDATE_OPTIONS");
        intent.setComponent(widget.provider.info.provider);
        intent.putExtra("appWidgetId", widget.appWidgetId);
        intent.putExtra("appWidgetOptions", widget.options);
        sendBroadcastAsUser(intent, widget.provider.info.getProfile());
    }

    private void registerForBroadcastsLocked(Provider provider, int[] appWidgetIds) {
        long oldId;
        Provider provider2 = provider;
        if (provider2.info.updatePeriodMillis > 0) {
            boolean alreadyRegistered = provider2.broadcast != null;
            Intent intent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
            intent.putExtra("appWidgetIds", appWidgetIds);
            intent.setComponent(provider2.info.provider);
            long token = Binder.clearCallingIdentity();
            try {
                provider2.broadcast = PendingIntent.getBroadcastAsUser(this.mContext, 1, intent, 134217728, provider2.info.getProfile());
                if (!alreadyRegistered) {
                    long period = (long) provider2.info.updatePeriodMillis;
                    if (period < ((long) MIN_UPDATE_PERIOD)) {
                        period = (long) MIN_UPDATE_PERIOD;
                    }
                    long oldId2 = Binder.clearCallingIdentity();
                    try {
                        AlarmManager alarmManager = this.mAlarmManager;
                        oldId = oldId2;
                        try {
                            alarmManager.setInexactRepeating(2, SystemClock.elapsedRealtime() + period, period, provider2.broadcast);
                            Binder.restoreCallingIdentity(oldId);
                        } catch (Throwable th) {
                            th = th;
                            Binder.restoreCallingIdentity(oldId);
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        oldId = oldId2;
                        Binder.restoreCallingIdentity(oldId);
                        throw th;
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            int[] iArr = appWidgetIds;
        }
    }

    private static int[] getWidgetIds(ArrayList<Widget> widgets) {
        int instancesSize = widgets.size();
        int[] appWidgetIds = new int[instancesSize];
        for (int i = 0; i < instancesSize; i++) {
            appWidgetIds[i] = widgets.get(i).appWidgetId;
        }
        return appWidgetIds;
    }

    private static void dumpProvider(Provider provider, int index, PrintWriter pw) {
        AppWidgetProviderInfo info = provider.info;
        pw.print("  [");
        pw.print(index);
        pw.print("] provider ");
        pw.println(provider.id);
        pw.print("    min=(");
        pw.print(info.minWidth);
        pw.print("x");
        pw.print(info.minHeight);
        pw.print(")   minResize=(");
        pw.print(info.minResizeWidth);
        pw.print("x");
        pw.print(info.minResizeHeight);
        pw.print(") updatePeriodMillis=");
        pw.print(info.updatePeriodMillis);
        pw.print(" resizeMode=");
        pw.print(info.resizeMode);
        pw.print(" widgetCategory=");
        pw.print(info.widgetCategory);
        pw.print(" autoAdvanceViewId=");
        pw.print(info.autoAdvanceViewId);
        pw.print(" initialLayout=#");
        pw.print(Integer.toHexString(info.initialLayout));
        pw.print(" initialKeyguardLayout=#");
        pw.print(Integer.toHexString(info.initialKeyguardLayout));
        pw.print(" zombie=");
        pw.println(provider.zombie);
    }

    private static void dumpHost(Host host, int index, PrintWriter pw) {
        pw.print("  [");
        pw.print(index);
        pw.print("] hostId=");
        pw.println(host.id);
        pw.print("    callbacks=");
        pw.println(host.callbacks);
        pw.print("    widgets.size=");
        pw.print(host.widgets.size());
        pw.print(" zombie=");
        pw.println(host.zombie);
    }

    private static void dumpGrant(Pair<Integer, String> grant, int index, PrintWriter pw) {
        pw.print("  [");
        pw.print(index);
        pw.print(']');
        pw.print(" user=");
        pw.print(grant.first);
        pw.print(" package=");
        pw.println((String) grant.second);
    }

    private static void dumpWidget(Widget widget, int index, PrintWriter pw) {
        pw.print("  [");
        pw.print(index);
        pw.print("] id=");
        pw.println(widget.appWidgetId);
        pw.print("    host=");
        pw.println(widget.host.id);
        if (widget.provider != null) {
            pw.print("    provider=");
            pw.println(widget.provider.id);
        }
        if (widget.host != null) {
            pw.print("    host.callbacks=");
            pw.println(widget.host.callbacks);
        }
        if (widget.views != null) {
            pw.print("    views=");
            pw.println(widget.views);
        }
    }

    /* access modifiers changed from: private */
    public static void serializeProvider(XmlSerializer out, Provider p) throws IOException {
        out.startTag(null, "p");
        out.attribute(null, AbsLocationManagerService.DEL_PKG, p.info.provider.getPackageName());
        out.attribute(null, "cl", p.info.provider.getClassName());
        out.attribute(null, "tag", Integer.toHexString(p.tag));
        if (!TextUtils.isEmpty(p.infoTag)) {
            out.attribute(null, "info_tag", p.infoTag);
        }
        out.endTag(null, "p");
    }

    /* access modifiers changed from: private */
    public static void serializeHost(XmlSerializer out, Host host) throws IOException {
        out.startTag(null, "h");
        out.attribute(null, AbsLocationManagerService.DEL_PKG, host.id.packageName);
        out.attribute(null, "id", Integer.toHexString(host.id.hostId));
        out.attribute(null, "tag", Integer.toHexString(host.tag));
        out.endTag(null, "h");
    }

    /* access modifiers changed from: private */
    public static void serializeAppWidget(XmlSerializer out, Widget widget) throws IOException {
        out.startTag(null, "g");
        out.attribute(null, "id", Integer.toHexString(widget.appWidgetId));
        out.attribute(null, "rid", Integer.toHexString(widget.restoredId));
        out.attribute(null, "h", Integer.toHexString(widget.host.tag));
        if (widget.provider != null) {
            out.attribute(null, "p", Integer.toHexString(widget.provider.tag));
        }
        if (widget.options != null) {
            int minWidth = widget.options.getInt("appWidgetMinWidth");
            int minHeight = widget.options.getInt("appWidgetMinHeight");
            int maxWidth = widget.options.getInt("appWidgetMaxWidth");
            int maxHeight = widget.options.getInt("appWidgetMaxHeight");
            int i = 0;
            out.attribute(null, "min_width", Integer.toHexString(minWidth > 0 ? minWidth : 0));
            out.attribute(null, "min_height", Integer.toHexString(minHeight > 0 ? minHeight : 0));
            out.attribute(null, "max_width", Integer.toHexString(maxWidth > 0 ? maxWidth : 0));
            if (maxHeight > 0) {
                i = maxHeight;
            }
            out.attribute(null, "max_height", Integer.toHexString(i));
            out.attribute(null, "host_category", Integer.toHexString(widget.options.getInt("appWidgetCategory")));
        }
        out.endTag(null, "g");
    }

    public List<String> getWidgetParticipants(int userId) {
        return this.mBackupRestoreController.getWidgetParticipants(userId);
    }

    public byte[] getWidgetState(String packageName, int userId) {
        return this.mBackupRestoreController.getWidgetState(packageName, userId);
    }

    public void restoreStarting(int userId) {
        this.mBackupRestoreController.restoreStarting(userId);
    }

    public void restoreWidgetState(String packageName, byte[] restoredState, int userId) {
        this.mBackupRestoreController.restoreWidgetState(packageName, restoredState, userId);
    }

    public void restoreFinished(int userId) {
        this.mBackupRestoreController.restoreFinished(userId);
    }

    private Provider parseProviderInfoXml(ProviderId providerId, ResolveInfo ri, Provider oldProvider) {
        AppWidgetProviderInfo info = null;
        if (oldProvider != null && !TextUtils.isEmpty(oldProvider.infoTag)) {
            info = parseAppWidgetProviderInfo(providerId, ri.activityInfo, oldProvider.infoTag);
        }
        if (info == null) {
            info = parseAppWidgetProviderInfo(providerId, ri.activityInfo, "android.appwidget.provider");
        }
        if (info == null) {
            return null;
        }
        Provider provider = new Provider();
        provider.id = providerId;
        provider.info = info;
        return provider;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0041, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0178, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:?, code lost:
        android.os.Binder.restoreCallingIdentity(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x017c, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x017d, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x017e, code lost:
        r5 = null;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:6:0x0019, B:15:0x0048, B:32:0x00a0, B:68:0x0180] */
    private AppWidgetProviderInfo parseAppWidgetProviderInfo(ProviderId providerId, ActivityInfo activityInfo, String metadataKey) {
        Throwable th;
        AttributeSet attrs;
        ProviderId providerId2 = providerId;
        ActivityInfo activityInfo2 = activityInfo;
        String str = metadataKey;
        try {
            XmlResourceParser parser = activityInfo2.loadXmlMetaData(this.mContext.getPackageManager(), str);
            if (parser == null) {
                try {
                    Slog.w(TAG, "No " + str + " meta-data for AppWidget provider '" + providerId2 + '\'');
                    if (parser != null) {
                        $closeResource(null, parser);
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } else {
                AttributeSet attrs2 = Xml.asAttributeSet(parser);
                while (true) {
                    attrs = attrs2;
                    int next = parser.next();
                    int type = next;
                    if (next != 1 && type != 2) {
                        attrs2 = attrs;
                    }
                }
                if (!"appwidget-provider".equals(parser.getName())) {
                    Slog.w(TAG, "Meta-data does not start with appwidget-provider tag for AppWidget provider " + providerId2.componentName + " for user " + providerId2.uid);
                    if (parser != null) {
                        $closeResource(null, parser);
                    }
                    return null;
                }
                AppWidgetProviderInfo info = new AppWidgetProviderInfo();
                info.provider = providerId2.componentName;
                info.providerInfo = activityInfo2;
                long identity = Binder.clearCallingIdentity();
                PackageManager pm = this.mContext.getPackageManager();
                Resources resources = pm.getResourcesForApplication(pm.getApplicationInfoAsUser(activityInfo2.packageName, 0, UserHandle.getUserId(providerId2.uid)));
                Binder.restoreCallingIdentity(identity);
                TypedArray sa = resources.obtainAttributes(attrs, R.styleable.AppWidgetProviderInfo);
                TypedValue value = sa.peekValue(0);
                info.minWidth = value != null ? value.data : 0;
                TypedValue value2 = sa.peekValue(1);
                info.minHeight = value2 != null ? value2.data : 0;
                TypedValue value3 = sa.peekValue(8);
                info.minResizeWidth = value3 != null ? value3.data : info.minWidth;
                TypedValue value4 = sa.peekValue(9);
                info.minResizeHeight = value4 != null ? value4.data : info.minHeight;
                info.updatePeriodMillis = sa.getInt(2, 0);
                info.initialLayout = sa.getResourceId(3, 0);
                info.initialKeyguardLayout = sa.getResourceId(10, 0);
                String className = sa.getString(4);
                if (className != null) {
                    Resources resources2 = resources;
                    info.configure = new ComponentName(providerId2.componentName.getPackageName(), className);
                }
                info.label = activityInfo2.loadLabel(this.mContext.getPackageManager()).toString();
                info.icon = activityInfo.getIconResource();
                info.previewImage = sa.getResourceId(5, 0);
                info.autoAdvanceViewId = sa.getResourceId(6, -1);
                info.resizeMode = sa.getInt(7, 0);
                info.widgetCategory = sa.getInt(11, 1);
                info.widgetFeatures = sa.getInt(12, 0);
                sa.recycle();
                if (parser != null) {
                    $closeResource(null, parser);
                }
                return info;
            }
            if (parser != null) {
                $closeResource(th, parser);
            }
            throw th;
        } catch (PackageManager.NameNotFoundException | IOException | XmlPullParserException e) {
            Slog.w(TAG, "XML parsing failed for AppWidget provider " + providerId2.componentName + " for user " + providerId2.uid, e);
            return null;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* access modifiers changed from: private */
    public int getUidForPackage(String packageName, int userId) {
        PackageInfo pkgInfo = null;
        long identity = Binder.clearCallingIdentity();
        try {
            pkgInfo = this.mPackageManager.getPackageInfo(packageName, 0, userId);
        } catch (RemoteException e) {
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        if (pkgInfo == null || pkgInfo.applicationInfo == null) {
            return -1;
        }
        return pkgInfo.applicationInfo.uid;
    }

    private ActivityInfo getProviderInfo(ComponentName componentName, int userId) {
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
        intent.setComponent(componentName);
        List<ResolveInfo> receivers = queryIntentReceivers(intent, userId);
        if (!receivers.isEmpty()) {
            return receivers.get(0).activityInfo;
        }
        return null;
    }

    private List<ResolveInfo> queryIntentReceivers(Intent intent, int userId) {
        long identity = Binder.clearCallingIdentity();
        int flags = 128 | 268435456;
        try {
            if (isProfileWithUnlockedParent(userId)) {
                flags |= 786432;
            }
            return this.mPackageManager.queryIntentReceivers(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags | 1024, userId).getList();
        } catch (RemoteException e) {
            return Collections.emptyList();
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* access modifiers changed from: package-private */
    public void onUserUnlocked(int userId) {
        if (!isProfileWithLockedParent(userId)) {
            if (!this.mUserManager.isUserUnlockingOrUnlocked(userId)) {
                Slog.w(TAG, "User " + userId + " is no longer unlocked - exiting");
                return;
            }
            long time = SystemClock.elapsedRealtime();
            synchronized (this.mLock) {
                Trace.traceBegin(64, "appwidget ensure");
                ensureGroupStateLoadedLocked(userId);
                Trace.traceEnd(64);
                Trace.traceBegin(64, "appwidget reload");
                reloadWidgetsMaskedStateForGroup(this.mSecurityPolicy.getGroupParent(userId));
                Trace.traceEnd(64);
                int N = this.mProviders.size();
                for (int i = 0; i < N; i++) {
                    Provider provider = this.mProviders.get(i);
                    if (provider.getUserId() == userId) {
                        if (provider.widgets.size() > 0) {
                            Trace.traceBegin(64, "appwidget init " + provider.info.provider.getPackageName());
                            sendEnableIntentLocked(provider);
                            int[] appWidgetIds = getWidgetIds(provider.widgets);
                            sendUpdateIntentLocked(provider, appWidgetIds);
                            registerForBroadcastsLocked(provider, appWidgetIds);
                            Trace.traceEnd(64);
                        }
                    }
                }
            }
            Slog.i(TAG, "Async processing of onUserUnlocked u" + userId + " took " + (SystemClock.elapsedRealtime() - time) + " ms");
        }
    }

    private void loadGroupStateLocked(int[] profileIds) {
        FileInputStream stream;
        List<LoadedWidgetState> loadedWidgets = new ArrayList<>();
        int version = 0;
        for (int profileId : profileIds) {
            try {
                stream = getSavedStateFile(profileId).openRead();
                version = readProfileStateFromFileLocked(stream, profileId, loadedWidgets);
                if (stream != null) {
                    $closeResource(null, stream);
                }
            } catch (IOException e) {
                Slog.w(TAG, "Failed to read state: " + e);
            } catch (Throwable th) {
                if (stream != null) {
                    $closeResource(r8, stream);
                }
                throw th;
            }
        }
        if (version >= 0) {
            bindLoadedWidgetsLocked(loadedWidgets);
            performUpgradeLocked(version);
            return;
        }
        Slog.w(TAG, "Failed to read state, clearing widgets and hosts.");
        clearWidgetsLocked();
        this.mHosts.clear();
        int N = this.mProviders.size();
        for (int i = 0; i < N; i++) {
            this.mProviders.get(i).widgets.clear();
        }
    }

    private void bindLoadedWidgetsLocked(List<LoadedWidgetState> loadedWidgets) {
        for (int i = loadedWidgets.size() - 1; i >= 0; i--) {
            LoadedWidgetState loadedWidget = loadedWidgets.remove(i);
            Widget widget = loadedWidget.widget;
            widget.provider = findProviderByTag(loadedWidget.providerTag);
            if (widget.provider != null) {
                widget.host = findHostByTag(loadedWidget.hostTag);
                if (widget.host != null) {
                    widget.provider.widgets.add(widget);
                    widget.host.widgets.add(widget);
                    addWidgetLocked(widget);
                }
            }
        }
    }

    private Provider findProviderByTag(int tag) {
        if (tag < 0) {
            return null;
        }
        int providerCount = this.mProviders.size();
        for (int i = 0; i < providerCount; i++) {
            Provider provider = this.mProviders.get(i);
            if (provider.tag == tag) {
                return provider;
            }
        }
        return null;
    }

    private Host findHostByTag(int tag) {
        if (tag < 0) {
            return null;
        }
        int hostCount = this.mHosts.size();
        for (int i = 0; i < hostCount; i++) {
            Host host = this.mHosts.get(i);
            if (host.tag == tag) {
                return host;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void addWidgetLocked(Widget widget) {
        this.mWidgets.add(widget);
        onWidgetProviderAddedOrChangedLocked(widget);
    }

    /* access modifiers changed from: package-private */
    public void onWidgetProviderAddedOrChangedLocked(Widget widget) {
        if (widget.provider != null) {
            int userId = widget.provider.getUserId();
            ArraySet<String> packages = this.mWidgetPackages.get(userId);
            if (packages == null) {
                SparseArray<ArraySet<String>> sparseArray = this.mWidgetPackages;
                ArraySet<String> arraySet = new ArraySet<>();
                packages = arraySet;
                sparseArray.put(userId, arraySet);
            }
            packages.add(widget.provider.info.provider.getPackageName());
            if (widget.provider.isMaskedLocked()) {
                maskWidgetsViewsLocked(widget.provider, widget);
            } else {
                boolean unused = widget.clearMaskedViewsLocked();
            }
            addWidgetReport(userId, widget);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeWidgetLocked(Widget widget) {
        this.mWidgets.remove(widget);
        onWidgetRemovedLocked(widget);
    }

    private void onWidgetRemovedLocked(Widget widget) {
        if (widget.provider != null) {
            int userId = widget.provider.getUserId();
            String packageName = widget.provider.info.provider.getPackageName();
            removeWidgetReport(userId, widget);
            ArraySet<String> packages = this.mWidgetPackages.get(userId);
            if (packages != null) {
                int N = this.mWidgets.size();
                int i = 0;
                while (i < N) {
                    Widget w = this.mWidgets.get(i);
                    if (w.provider == null || w.provider.getUserId() != userId || !packageName.equals(w.provider.info.provider.getPackageName())) {
                        i++;
                    } else {
                        return;
                    }
                }
                packages.remove(packageName);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void addWidgetReport(int userId, Widget widget) {
    }

    /* access modifiers changed from: protected */
    public void removeWidgetReport(int userId, Widget widget) {
    }

    /* access modifiers changed from: protected */
    public void updateWidgetOptionsReport(int userId, Widget widget) {
    }

    /* access modifiers changed from: protected */
    public void clearWidgetReport() {
    }

    /* access modifiers changed from: package-private */
    public void clearWidgetsLocked() {
        this.mWidgets.clear();
        onWidgetsClearedLocked();
    }

    private void onWidgetsClearedLocked() {
        this.mWidgetPackages.clear();
        clearWidgetReport();
    }

    public boolean isBoundWidgetPackage(String packageName, int userId) {
        if (Binder.getCallingUid() == 1000) {
            synchronized (this.mLock) {
                ArraySet<String> packages = this.mWidgetPackages.get(userId);
                if (packages == null) {
                    return false;
                }
                boolean contains = packages.contains(packageName);
                return contains;
            }
        }
        throw new SecurityException("Only the system process can call this");
    }

    /* access modifiers changed from: private */
    public void saveStateLocked(int userId) {
        tagProvidersAndHosts();
        for (int profileId : this.mSecurityPolicy.getEnabledGroupProfileIds(userId)) {
            AtomicFile file = getSavedStateFile(profileId);
            try {
                FileOutputStream stream = file.startWrite();
                if (writeProfileStateToFileLocked(stream, profileId)) {
                    file.finishWrite(stream);
                } else {
                    file.failWrite(stream);
                    Slog.w(TAG, "Failed to save state, restoring backup.");
                }
            } catch (IOException e) {
                Slog.w(TAG, "Failed open state file for write: " + e);
            }
        }
    }

    private void tagProvidersAndHosts() {
        int providerCount = this.mProviders.size();
        for (int i = 0; i < providerCount; i++) {
            this.mProviders.get(i).tag = i;
        }
        int hostCount = this.mHosts.size();
        for (int i2 = 0; i2 < hostCount; i2++) {
            this.mHosts.get(i2).tag = i2;
        }
    }

    private void clearProvidersAndHostsTagsLocked() {
        int providerCount = this.mProviders.size();
        for (int i = 0; i < providerCount; i++) {
            this.mProviders.get(i).tag = -1;
        }
        int hostCount = this.mHosts.size();
        for (int i2 = 0; i2 < hostCount; i2++) {
            this.mHosts.get(i2).tag = -1;
        }
    }

    private boolean writeProfileStateToFileLocked(FileOutputStream stream, int userId) {
        try {
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(stream, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            out.startTag(null, "gs");
            out.attribute(null, "version", String.valueOf(1));
            int N = this.mProviders.size();
            for (int i = 0; i < N; i++) {
                Provider provider = this.mProviders.get(i);
                if (provider.getUserId() == userId) {
                    if (provider.shouldBePersisted()) {
                        serializeProvider(out, provider);
                    }
                }
            }
            int N2 = this.mHosts.size();
            for (int i2 = 0; i2 < N2; i2++) {
                Host host = this.mHosts.get(i2);
                if (host.getUserId() == userId) {
                    serializeHost(out, host);
                }
            }
            int N3 = this.mWidgets.size();
            for (int i3 = 0; i3 < N3; i3++) {
                Widget widget = this.mWidgets.get(i3);
                if (widget.host.getUserId() == userId) {
                    serializeAppWidget(out, widget);
                }
            }
            Iterator<Pair<Integer, String>> it = this.mPackagesWithBindWidgetPermission.iterator();
            while (it.hasNext()) {
                Pair<Integer, String> binding = it.next();
                if (((Integer) binding.first).intValue() == userId) {
                    out.startTag(null, "b");
                    out.attribute(null, "packageName", (String) binding.second);
                    out.endTag(null, "b");
                }
            }
            out.endTag(null, "gs");
            out.endDocument();
            return true;
        } catch (IOException e) {
            Slog.w(TAG, "Failed to write state: " + e);
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:109:0x0274, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x0275, code lost:
        r1 = r30;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x026d A[LOOP:0: B:7:0x001a->B:108:0x026d, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x0274 A[ExcHandler: IOException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException (e java.lang.Throwable), Splitter:B:8:0x001b] */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x026c A[SYNTHETIC] */
    private int readProfileStateFromFileLocked(FileInputStream stream, int userId, List<LoadedWidgetState> outLoadedWidgets) {
        int type;
        int i;
        int providerTag;
        Provider provider;
        int version;
        AppWidgetServiceImpl appWidgetServiceImpl = this;
        int i2 = userId;
        try {
            XmlPullParser parser = Xml.newPullParser();
            try {
                parser.setInput(stream, StandardCharsets.UTF_8.name());
                int version2 = -1;
                int legacyProviderIndex = -1;
                int legacyHostIndex = -1;
                while (true) {
                    int legacyHostIndex2 = legacyHostIndex;
                    try {
                        type = parser.next();
                        if (type == 2) {
                            String tag = parser.getName();
                            if ("gs".equals(tag)) {
                                version = Integer.parseInt(parser.getAttributeValue(null, "version"));
                                List<LoadedWidgetState> list = outLoadedWidgets;
                                version2 = version;
                                legacyHostIndex = legacyHostIndex2;
                                if (type == 1) {
                                    return version2;
                                }
                                appWidgetServiceImpl = this;
                                i2 = userId;
                            } else if ("p".equals(tag)) {
                                legacyProviderIndex++;
                                String pkg = parser.getAttributeValue(null, AbsLocationManagerService.DEL_PKG);
                                String cl = parser.getAttributeValue(null, "cl");
                                String pkg2 = appWidgetServiceImpl.getCanonicalPackageName(pkg, cl, i2);
                                if (pkg2 != null) {
                                    int uid = appWidgetServiceImpl.getUidForPackage(pkg2, i2);
                                    if (uid >= 0) {
                                        ComponentName componentName = new ComponentName(pkg2, cl);
                                        ActivityInfo providerInfo = appWidgetServiceImpl.getProviderInfo(componentName, i2);
                                        if (providerInfo != null) {
                                            ProviderId providerId = new ProviderId(uid, componentName);
                                            Provider provider2 = appWidgetServiceImpl.lookupProviderLocked(providerId);
                                            if (provider2 != null || !appWidgetServiceImpl.mSafeMode) {
                                                ComponentName componentName2 = componentName;
                                                provider = provider2;
                                            } else {
                                                provider = new Provider();
                                                String str = pkg2;
                                                provider.info = new AppWidgetProviderInfo();
                                                ComponentName componentName3 = componentName;
                                                provider.info.provider = providerId.componentName;
                                                provider.info.providerInfo = providerInfo;
                                                provider.zombie = true;
                                                provider.id = providerId;
                                                appWidgetServiceImpl.mProviders.add(provider);
                                            }
                                            String tagAttribute = parser.getAttributeValue(null, "tag");
                                            int providerTag2 = !TextUtils.isEmpty(tagAttribute) ? Integer.parseInt(tagAttribute, 16) : legacyProviderIndex;
                                            provider.tag = providerTag2;
                                            String str2 = tagAttribute;
                                            int i3 = providerTag2;
                                            provider.infoTag = parser.getAttributeValue(null, "info_tag");
                                            if (!TextUtils.isEmpty(provider.infoTag) && !appWidgetServiceImpl.mSafeMode) {
                                                AppWidgetProviderInfo info = appWidgetServiceImpl.parseAppWidgetProviderInfo(providerId, providerInfo, provider.infoTag);
                                                if (info != null) {
                                                    provider.info = info;
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if ("h".equals(tag)) {
                                legacyHostIndex2++;
                                Host host = new Host();
                                String pkg3 = parser.getAttributeValue(null, AbsLocationManagerService.DEL_PKG);
                                int uid2 = appWidgetServiceImpl.getUidForPackage(pkg3, i2);
                                if (uid2 < 0) {
                                    host.zombie = true;
                                }
                                if (!host.zombie || appWidgetServiceImpl.mSafeMode) {
                                    int hostId = Integer.parseInt(parser.getAttributeValue(null, "id"), 16);
                                    String tagAttribute2 = parser.getAttributeValue(null, "tag");
                                    host.tag = !TextUtils.isEmpty(tagAttribute2) ? Integer.parseInt(tagAttribute2, 16) : legacyHostIndex2;
                                    host.id = new HostId(uid2, hostId, pkg3);
                                    appWidgetServiceImpl.mHosts.add(host);
                                }
                            } else if ("b".equals(tag)) {
                                String packageName = parser.getAttributeValue(null, "packageName");
                                if (appWidgetServiceImpl.getUidForPackage(packageName, i2) >= 0) {
                                    appWidgetServiceImpl.mPackagesWithBindWidgetPermission.add(Pair.create(Integer.valueOf(userId), packageName));
                                }
                            } else if ("g".equals(tag)) {
                                Widget widget = new Widget();
                                widget.appWidgetId = Integer.parseInt(parser.getAttributeValue(null, "id"), 16);
                                appWidgetServiceImpl.setMinAppWidgetIdLocked(i2, widget.appWidgetId + 1);
                                String restoredIdString = parser.getAttributeValue(null, "rid");
                                if (restoredIdString == null) {
                                    i = 0;
                                } else {
                                    i = Integer.parseInt(restoredIdString, 16);
                                }
                                widget.restoredId = i;
                                Bundle options = new Bundle();
                                String minWidthString = parser.getAttributeValue(null, "min_width");
                                if (minWidthString != null) {
                                    options.putInt("appWidgetMinWidth", Integer.parseInt(minWidthString, 16));
                                }
                                String minHeightString = parser.getAttributeValue(null, "min_height");
                                if (minHeightString != null) {
                                    options.putInt("appWidgetMinHeight", Integer.parseInt(minHeightString, 16));
                                }
                                String maxWidthString = parser.getAttributeValue(null, "max_width");
                                if (maxWidthString != null) {
                                    String str3 = restoredIdString;
                                    options.putInt("appWidgetMaxWidth", Integer.parseInt(maxWidthString, 16));
                                }
                                String maxHeightString = parser.getAttributeValue(null, "max_height");
                                if (maxHeightString != null) {
                                    String str4 = maxWidthString;
                                    options.putInt("appWidgetMaxHeight", Integer.parseInt(maxHeightString, 16));
                                }
                                String categoryString = parser.getAttributeValue(null, "host_category");
                                if (categoryString != null) {
                                    String str5 = maxHeightString;
                                    options.putInt("appWidgetCategory", Integer.parseInt(categoryString, 16));
                                }
                                widget.options = options;
                                int hostTag = Integer.parseInt(parser.getAttributeValue(null, "h"), 16);
                                if (parser.getAttributeValue(null, "p") != null) {
                                    String str6 = categoryString;
                                    providerTag = Integer.parseInt(parser.getAttributeValue(null, "p"), 16);
                                } else {
                                    providerTag = -1;
                                }
                                try {
                                    outLoadedWidgets.add(new LoadedWidgetState(widget, hostTag, providerTag));
                                    legacyHostIndex = legacyHostIndex2;
                                    if (type == 1) {
                                    }
                                } catch (IOException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException e) {
                                    e = e;
                                }
                            }
                        }
                        List<LoadedWidgetState> list2 = outLoadedWidgets;
                    } catch (NumberFormatException e2) {
                        NumberFormatException numberFormatException = e2;
                        version = 0;
                    } catch (IOException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException e3) {
                    }
                    legacyHostIndex = legacyHostIndex2;
                    if (type == 1) {
                    }
                }
            } catch (IOException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException e4) {
                e = e4;
                List<LoadedWidgetState> list3 = outLoadedWidgets;
                Slog.w(TAG, "failed parsing " + e);
                return -1;
            }
        } catch (IOException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException e5) {
            e = e5;
            FileInputStream fileInputStream = stream;
            List<LoadedWidgetState> list32 = outLoadedWidgets;
            Slog.w(TAG, "failed parsing " + e);
            return -1;
        }
    }

    private void performUpgradeLocked(int fromVersion) {
        if (fromVersion < 1) {
            Slog.v(TAG, "Upgrading widget database from " + fromVersion + " to " + 1);
        }
        int version = fromVersion;
        if (version == 0) {
            Host host = lookupHostLocked(new HostId(Process.myUid(), KEYGUARD_HOST_ID, "android"));
            if (host != null) {
                int uid = getUidForPackage(NEW_KEYGUARD_HOST_PACKAGE, 0);
                if (uid >= 0) {
                    host.id = new HostId(uid, KEYGUARD_HOST_ID, NEW_KEYGUARD_HOST_PACKAGE);
                }
            }
            version = 1;
        }
        if (version != 1) {
            throw new IllegalStateException("Failed to upgrade widget database");
        }
    }

    private static File getStateFile(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), STATE_FILENAME);
    }

    private static AtomicFile getSavedStateFile(int userId) {
        File dir = Environment.getUserSystemDirectory(userId);
        File settingsFile = getStateFile(userId);
        if (!settingsFile.exists() && userId == 0) {
            if (!dir.exists()) {
                dir.mkdirs();
            }
            new File("/data/system/appwidgets.xml").renameTo(settingsFile);
        }
        return new AtomicFile(settingsFile);
    }

    /* access modifiers changed from: package-private */
    public void onUserStopped(int userId) {
        synchronized (this.mLock) {
            boolean crossProfileWidgetsChanged = false;
            int i = this.mWidgets.size() - 1;
            while (true) {
                boolean providerInUser = true;
                if (i < 0) {
                    break;
                }
                Widget widget = this.mWidgets.get(i);
                boolean hostInUser = widget.host.getUserId() == userId;
                boolean hasProvider = widget.provider != null;
                if (!hasProvider || widget.provider.getUserId() != userId) {
                    providerInUser = false;
                }
                if (hostInUser && (!hasProvider || providerInUser)) {
                    removeWidgetLocked(widget);
                    widget.host.widgets.remove(widget);
                    widget.host = null;
                    if (hasProvider) {
                        widget.provider.widgets.remove(widget);
                        widget.provider = null;
                    }
                }
                i--;
            }
            for (int i2 = this.mHosts.size() - 1; i2 >= 0; i2--) {
                Host host = this.mHosts.get(i2);
                if (host.getUserId() == userId) {
                    crossProfileWidgetsChanged |= !host.widgets.isEmpty();
                    deleteHostLocked(host);
                }
            }
            for (int i3 = this.mPackagesWithBindWidgetPermission.size() - 1; i3 >= 0; i3--) {
                if (((Integer) this.mPackagesWithBindWidgetPermission.valueAt(i3).first).intValue() == userId) {
                    this.mPackagesWithBindWidgetPermission.removeAt(i3);
                }
            }
            int userIndex = this.mLoadedUserIds.indexOfKey(userId);
            if (userIndex >= 0) {
                this.mLoadedUserIds.removeAt(userIndex);
            }
            int nextIdIndex = this.mNextAppWidgetIds.indexOfKey(userId);
            if (nextIdIndex >= 0) {
                this.mNextAppWidgetIds.removeAt(nextIdIndex);
            }
            if (crossProfileWidgetsChanged) {
                saveGroupStateAsync(userId);
            }
        }
    }

    private boolean updateProvidersForPackageLocked(String packageName, int userId, Set<ProviderId> removedProviders) {
        int N;
        List<ResolveInfo> broadcastReceivers;
        Intent intent;
        String str = packageName;
        int i = userId;
        Set<ProviderId> set = removedProviders;
        HashSet<ProviderId> keep = new HashSet<>();
        Intent intent2 = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
        intent2.setPackage(str);
        List<ResolveInfo> broadcastReceivers2 = queryIntentReceivers(intent2, i);
        int N2 = broadcastReceivers2 == null ? 0 : broadcastReceivers2.size();
        boolean providersUpdated = false;
        int i2 = 0;
        while (i2 < N2) {
            ResolveInfo ri = broadcastReceivers2.get(i2);
            ActivityInfo ai = ri.activityInfo;
            if ((ai.applicationInfo.flags & 262144) != 0) {
                intent = intent2;
            } else {
                if (str.equals(ai.packageName)) {
                    intent = intent2;
                    ProviderId providerId = new ProviderId(ai.applicationInfo.uid, new ComponentName(ai.packageName, ai.name));
                    Provider provider = lookupProviderLocked(providerId);
                    if (provider != null) {
                        Provider parsed = parseProviderInfoXml(providerId, ri, provider);
                        if (parsed != null) {
                            keep.add(providerId);
                            provider.info = parsed.info;
                            int M = provider.widgets.size();
                            if (M > 0) {
                                int[] appWidgetIds = getWidgetIds(provider.widgets);
                                cancelBroadcasts(provider);
                                registerForBroadcastsLocked(provider, appWidgetIds);
                                int j = 0;
                                while (true) {
                                    broadcastReceivers = broadcastReceivers2;
                                    int j2 = j;
                                    if (j2 >= M) {
                                        break;
                                    }
                                    ProviderId providerId2 = providerId;
                                    Widget widget = provider.widgets.get(j2);
                                    widget.views = null;
                                    scheduleNotifyProviderChangedLocked(widget);
                                    j = j2 + 1;
                                    broadcastReceivers2 = broadcastReceivers;
                                    providerId = providerId2;
                                    N2 = N2;
                                }
                                N = N2;
                                sendUpdateIntentLocked(provider, appWidgetIds);
                                providersUpdated = true;
                            }
                        }
                        broadcastReceivers = broadcastReceivers2;
                        ProviderId providerId3 = providerId;
                        N = N2;
                        providersUpdated = true;
                    } else if (addProviderLocked(ri)) {
                        keep.add(providerId);
                        providersUpdated = true;
                    }
                } else {
                    intent = intent2;
                    broadcastReceivers = broadcastReceivers2;
                    N = N2;
                }
                i2++;
                intent2 = intent;
                broadcastReceivers2 = broadcastReceivers;
                N2 = N;
            }
            broadcastReceivers = broadcastReceivers2;
            N = N2;
            i2++;
            intent2 = intent;
            broadcastReceivers2 = broadcastReceivers;
            N2 = N;
        }
        List<ResolveInfo> list = broadcastReceivers2;
        int i3 = N2;
        for (int i4 = this.mProviders.size() - 1; i4 >= 0; i4--) {
            Provider provider2 = this.mProviders.get(i4);
            if (str.equals(provider2.info.provider.getPackageName()) && provider2.getUserId() == i && !keep.contains(provider2.id)) {
                if (set != null) {
                    set.add(provider2.id);
                }
                deleteProviderLocked(provider2);
                providersUpdated = true;
            }
        }
        return providersUpdated;
    }

    private void removeWidgetsForPackageLocked(String pkgName, int userId, int parentUserId) {
        int N = this.mProviders.size();
        for (int i = 0; i < N; i++) {
            Provider provider = this.mProviders.get(i);
            if (pkgName.equals(provider.info.provider.getPackageName()) && provider.getUserId() == userId && provider.widgets.size() > 0) {
                deleteWidgetsLocked(provider, parentUserId);
            }
        }
    }

    private boolean removeProvidersForPackageLocked(String pkgName, int userId) {
        boolean removed = false;
        for (int i = this.mProviders.size() - 1; i >= 0; i--) {
            Provider provider = this.mProviders.get(i);
            if (pkgName.equals(provider.info.provider.getPackageName()) && provider.getUserId() == userId) {
                deleteProviderLocked(provider);
                removed = true;
            }
        }
        return removed;
    }

    private boolean removeHostsAndProvidersForPackageLocked(String pkgName, int userId) {
        boolean removed = removeProvidersForPackageLocked(pkgName, userId);
        for (int i = this.mHosts.size() - 1; i >= 0; i--) {
            Host host = this.mHosts.get(i);
            if (pkgName.equals(host.id.packageName) && host.getUserId() == userId) {
                deleteHostLocked(host);
                removed = true;
            }
        }
        return removed;
    }

    private String getCanonicalPackageName(String packageName, String className, int userId) {
        long identity = Binder.clearCallingIdentity();
        try {
            AppGlobals.getPackageManager().getReceiverInfo(new ComponentName(packageName, className), 0, userId);
            return packageName;
        } catch (RemoteException e) {
            String[] packageNames = this.mContext.getPackageManager().currentToCanonicalPackageNames(new String[]{packageName});
            if (packageNames != null && packageNames.length > 0) {
                return packageNames[0];
            }
            Binder.restoreCallingIdentity(identity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* access modifiers changed from: private */
    public void sendBroadcastAsUser(Intent intent, UserHandle userHandle) {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(intent, userHandle);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void bindService(Intent intent, ServiceConnection connection, UserHandle userHandle) {
        long token = Binder.clearCallingIdentity();
        try {
            this.mContext.bindServiceAsUser(intent, connection, 33554433, userHandle);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void unbindService(ServiceConnection connection) {
        long token = Binder.clearCallingIdentity();
        try {
            this.mContext.unbindService(connection);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void onCrossProfileWidgetProvidersChanged(int userId, List<String> packages) {
        int parentId = this.mSecurityPolicy.getProfileParent(userId);
        if (parentId != userId) {
            synchronized (this.mLock) {
                ArraySet<String> previousPackages = new ArraySet<>();
                int providerCount = this.mProviders.size();
                for (int i = 0; i < providerCount; i++) {
                    Provider provider = this.mProviders.get(i);
                    if (provider.getUserId() == userId) {
                        previousPackages.add(provider.id.componentName.getPackageName());
                    }
                }
                int i2 = packages.size();
                boolean providersChanged = false;
                for (int i3 = 0; i3 < i2; i3++) {
                    String packageName = packages.get(i3);
                    previousPackages.remove(packageName);
                    providersChanged |= updateProvidersForPackageLocked(packageName, userId, null);
                }
                int i4 = previousPackages.size();
                for (int i5 = 0; i5 < i4; i5++) {
                    removeWidgetsForPackageLocked(previousPackages.valueAt(i5), userId, parentId);
                }
                if (providersChanged || i4 > 0) {
                    saveGroupStateAsync(userId);
                    scheduleNotifyGroupHostsForProvidersChangedLocked(userId);
                }
            }
        }
    }

    private boolean isProfileWithLockedParent(int userId) {
        long token = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = this.mUserManager.getUserInfo(userId);
            if (userInfo != null && userInfo.isManagedProfile()) {
                UserInfo parentInfo = this.mUserManager.getProfileParent(userId);
                if (parentInfo != null && !isUserRunningAndUnlocked(parentInfo.getUserHandle().getIdentifier())) {
                    return true;
                }
            }
            Binder.restoreCallingIdentity(token);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private boolean isProfileWithUnlockedParent(int userId) {
        UserInfo userInfo = this.mUserManager.getUserInfo(userId);
        if (userInfo != null && userInfo.isManagedProfile()) {
            UserInfo parentInfo = this.mUserManager.getProfileParent(userId);
            if (parentInfo != null && this.mUserManager.isUserUnlockingOrUnlocked(parentInfo.getUserHandle())) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.appwidget.AppWidgetServiceImpl$HwInnerAppWidgetService, android.os.IBinder] */
    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }
}
