package com.android.server.appwidget;

import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManagerInternal;
import android.app.admin.DevicePolicyManagerInternal.OnCrossProfileWidgetProvidersChangeListener;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.FilterComparison;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Point;
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
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;
import com.android.internal.R;
import com.android.internal.app.UnlaunchableAppActivity;
import com.android.internal.appwidget.IAppWidgetHost;
import com.android.internal.appwidget.IAppWidgetService.Stub;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.widget.IRemoteViewsAdapterConnection;
import com.android.internal.widget.IRemoteViewsFactory;
import com.android.server.AbsLocationManagerService;
import com.android.server.LocalServices;
import com.android.server.WidgetBackupProvider;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.am.ProcessList;
import com.android.server.policy.IconUtilities;
import com.huawei.pgmng.log.LogPower;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Map.Entry;
import java.util.Set;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class AppWidgetServiceImpl extends Stub implements WidgetBackupProvider, OnCrossProfileWidgetProvidersChangeListener {
    private static final int CURRENT_VERSION = 1;
    private static boolean DEBUG = false;
    private static HashMap<String, String> HIDDEN_WEATHER_WIDGETS = null;
    private static final boolean HIDE_HUAWEI_WEATHER_WIDGET = false;
    private static final String HUAWEI_LAUNCHER_PACKAGE = "com.huawei.android.launcher";
    private static final int KEYGUARD_HOST_ID = 1262836039;
    private static final int LOADED_PROFILE_ID = -1;
    private static final int MIN_UPDATE_PERIOD = 0;
    private static final String NEW_KEYGUARD_HOST_PACKAGE = "com.android.keyguard";
    private static final String OLD_KEYGUARD_HOST_PACKAGE = "android";
    private static final String STATE_FILENAME = "appwidgets.xml";
    private static final String TAG = "AppWidgetServiceImpl";
    private static final int TAG_UNDEFINED = -1;
    private static final int UNKNOWN_UID = -1;
    private static final int UNKNOWN_USER_ID = -10;
    private final AlarmManager mAlarmManager;
    private final AppOpsManager mAppOpsManager;
    private final BackupRestoreController mBackupRestoreController;
    private final HashMap<Pair<Integer, FilterComparison>, ServiceConnection> mBoundRemoteViewsServices;
    private final BroadcastReceiver mBroadcastReceiver;
    private final Handler mCallbackHandler;
    private final Context mContext;
    private final DevicePolicyManagerInternal mDevicePolicyManagerInternal;
    private final ArrayList<Host> mHosts;
    private final IconUtilities mIconUtilities;
    private final KeyguardManager mKeyguardManager;
    private final SparseIntArray mLoadedUserIds;
    private Locale mLocale;
    private final Object mLock;
    private int mMaxWidgetBitmapMemory;
    private final SparseIntArray mNextAppWidgetIds;
    private final IPackageManager mPackageManager;
    private final ArraySet<Pair<Integer, String>> mPackagesWithBindWidgetPermission;
    private final ArrayList<Provider> mProviders;
    private final HashMap<Pair<Integer, FilterComparison>, HashSet<Integer>> mRemoteViewsServicesAppWidgets;
    private boolean mSafeMode;
    private final Handler mSaveStateHandler;
    private final SecurityPolicy mSecurityPolicy;
    private final UserManager mUserManager;
    private final SparseArray<ArraySet<String>> mWidgetPackages;
    protected final ArrayList<Widget> mWidgets;

    /* renamed from: com.android.server.appwidget.AppWidgetServiceImpl.2 */
    class AnonymousClass2 implements ServiceConnection {
        final /* synthetic */ Intent val$intent;

        AnonymousClass2(Intent val$intent) {
            this.val$intent = val$intent;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                IRemoteViewsFactory.Stub.asInterface(service).onDestroy(this.val$intent);
            } catch (RemoteException re) {
                Slog.e(AppWidgetServiceImpl.TAG, "Error calling remove view factory", re);
            }
            AppWidgetServiceImpl.this.mContext.unbindService(this);
        }

        public void onServiceDisconnected(ComponentName name) {
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
            public boolean notified;
            public int oldId;

            public RestoreUpdateRecord(int theOldId, int theNewId) {
                this.oldId = theOldId;
                this.newId = theNewId;
                this.notified = AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
        }

        private BackupRestoreController() {
            this.mPrunedApps = new HashSet();
            this.mUpdatesByProvider = new HashMap();
            this.mUpdatesByHost = new HashMap();
        }

        public List<String> getWidgetParticipants(int userId) {
            Slog.i(TAG, "Getting widget participants for user: " + userId);
            HashSet<String> packages = new HashSet();
            synchronized (AppWidgetServiceImpl.this.mLock) {
                int N = AppWidgetServiceImpl.this.mWidgets.size();
                for (int i = AppWidgetServiceImpl.MIN_UPDATE_PERIOD; i < N; i += AppWidgetServiceImpl.CURRENT_VERSION) {
                    Widget widget = (Widget) AppWidgetServiceImpl.this.mWidgets.get(i);
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
                if (packageNeedsWidgetBackupLocked(backedupPackage, userId)) {
                    try {
                        int i;
                        Provider provider;
                        XmlSerializer out = new FastXmlSerializer();
                        out.setOutput(stream, StandardCharsets.UTF_8.name());
                        out.startDocument(null, Boolean.valueOf(DEBUG));
                        out.startTag(null, "ws");
                        out.attribute(null, "version", String.valueOf(WIDGET_STATE_VERSION));
                        out.attribute(null, AbsLocationManagerService.DEL_PKG, backedupPackage);
                        int index = AppWidgetServiceImpl.MIN_UPDATE_PERIOD;
                        int N = AppWidgetServiceImpl.this.mProviders.size();
                        for (i = AppWidgetServiceImpl.MIN_UPDATE_PERIOD; i < N; i += AppWidgetServiceImpl.CURRENT_VERSION) {
                            provider = (Provider) AppWidgetServiceImpl.this.mProviders.get(i);
                            if (!provider.widgets.isEmpty() && (provider.isInPackageForUser(backedupPackage, userId) || provider.hostedByPackageForUser(backedupPackage, userId))) {
                                provider.tag = index;
                                AppWidgetServiceImpl.serializeProvider(out, provider);
                                index += AppWidgetServiceImpl.CURRENT_VERSION;
                            }
                        }
                        N = AppWidgetServiceImpl.this.mHosts.size();
                        index = AppWidgetServiceImpl.MIN_UPDATE_PERIOD;
                        for (i = AppWidgetServiceImpl.MIN_UPDATE_PERIOD; i < N; i += AppWidgetServiceImpl.CURRENT_VERSION) {
                            Host host = (Host) AppWidgetServiceImpl.this.mHosts.get(i);
                            if (!host.widgets.isEmpty() && (host.isInPackageForUser(backedupPackage, userId) || host.hostsPackageForUser(backedupPackage, userId))) {
                                host.tag = index;
                                AppWidgetServiceImpl.serializeHost(out, host);
                                index += AppWidgetServiceImpl.CURRENT_VERSION;
                            }
                        }
                        N = AppWidgetServiceImpl.this.mWidgets.size();
                        for (i = AppWidgetServiceImpl.MIN_UPDATE_PERIOD; i < N; i += AppWidgetServiceImpl.CURRENT_VERSION) {
                            Widget widget = (Widget) AppWidgetServiceImpl.this.mWidgets.get(i);
                            provider = widget.provider;
                            if (widget.host.isInPackageForUser(backedupPackage, userId) || (provider != null && provider.isInPackageForUser(backedupPackage, userId))) {
                                AppWidgetServiceImpl.serializeAppWidget(out, widget);
                            }
                        }
                        out.endTag(null, "ws");
                        out.endDocument();
                        return stream.toByteArray();
                    } catch (IOException e) {
                        Slog.w(TAG, "Unable to save widget state for " + backedupPackage);
                        return null;
                    }
                }
                return null;
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void restoreWidgetState(String packageName, byte[] restoredState, int userId) {
            Slog.i(TAG, "Restoring widget state for user:" + userId + " package: " + packageName);
            InputStream byteArrayInputStream = new ByteArrayInputStream(restoredState);
            try {
                ArrayList<Provider> restoredProviders = new ArrayList();
                ArrayList<Host> restoredHosts = new ArrayList();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(byteArrayInputStream, StandardCharsets.UTF_8.name());
                synchronized (AppWidgetServiceImpl.this.mLock) {
                    while (true) {
                        int type = parser.next();
                        if (type == WIDGET_STATE_VERSION) {
                            String tag = parser.getName();
                            if ("ws".equals(tag)) {
                                String version = parser.getAttributeValue(null, "version");
                                if (Integer.parseInt(version) > WIDGET_STATE_VERSION) {
                                    Slog.w(TAG, "Unable to process state version " + version);
                                    AppWidgetServiceImpl.this.saveGroupStateAsync(userId);
                                    return;
                                }
                                if (!packageName.equals(parser.getAttributeValue(null, AbsLocationManagerService.DEL_PKG))) {
                                    Slog.w(TAG, "Package mismatch in ws");
                                    AppWidgetServiceImpl.this.saveGroupStateAsync(userId);
                                    return;
                                }
                            }
                            Provider p;
                            if ("p".equals(tag)) {
                                ComponentName componentName = new ComponentName(parser.getAttributeValue(null, AbsLocationManagerService.DEL_PKG), parser.getAttributeValue(null, "cl"));
                                p = findProviderLocked(componentName, userId);
                                if (p == null) {
                                    p = new Provider();
                                    p.id = new ProviderId(componentName, null);
                                    p.info = new AppWidgetProviderInfo();
                                    p.info.provider = componentName;
                                    p.zombie = DEBUG;
                                    AppWidgetServiceImpl.this.mProviders.add(p);
                                }
                                Slog.i(TAG, "   provider " + p.id);
                                restoredProviders.add(p);
                            } else {
                                if ("h".equals(tag)) {
                                    String pkg = parser.getAttributeValue(null, AbsLocationManagerService.DEL_PKG);
                                    HostId id = new HostId(AppWidgetServiceImpl.this.getUidForPackage(pkg, userId), Integer.parseInt(parser.getAttributeValue(null, "id"), 16), pkg);
                                    Host h = AppWidgetServiceImpl.this.lookupOrAddHostLocked(id);
                                    restoredHosts.add(h);
                                    Slog.i(TAG, "   host[" + restoredHosts.size() + "]: {" + h.id + "}");
                                } else {
                                    if ("g".equals(tag)) {
                                        int restoredId = Integer.parseInt(parser.getAttributeValue(null, "id"), 16);
                                        Host host = (Host) restoredHosts.get(Integer.parseInt(parser.getAttributeValue(null, "h"), 16));
                                        p = null;
                                        String prov = parser.getAttributeValue(null, "p");
                                        if (prov != null) {
                                            p = (Provider) restoredProviders.get(Integer.parseInt(prov, 16));
                                        }
                                        if (AppWidgetServiceImpl.HUAWEI_LAUNCHER_PACKAGE.equals(host.id.packageName)) {
                                            Slog.i(TAG, "Skip restore widget state in huawei launcher host for package: " + packageName);
                                        } else {
                                            pruneWidgetStateLocked(host.id.packageName, userId);
                                            if (p != null) {
                                                pruneWidgetStateLocked(p.id.componentName.getPackageName(), userId);
                                            }
                                            Widget id2 = findRestoredWidgetLocked(restoredId, host, p);
                                            if (id2 == null) {
                                                id2 = new Widget();
                                                id2.appWidgetId = AppWidgetServiceImpl.this.incrementAndGetAppWidgetIdLocked(userId);
                                                id2.restoredId = restoredId;
                                                id2.options = parseWidgetIdOptions(parser);
                                                id2.host = host;
                                                id2.host.widgets.add(id2);
                                                id2.provider = p;
                                                if (id2.provider != null) {
                                                    id2.provider.widgets.add(id2);
                                                }
                                                Slog.i(TAG, "New restored id " + restoredId + " now " + id2);
                                                AppWidgetServiceImpl.this.addWidgetLocked(id2);
                                            }
                                            if (id2.provider.info != null) {
                                                stashProviderRestoreUpdateLocked(id2.provider, restoredId, id2.appWidgetId);
                                            } else {
                                                Slog.w(TAG, "Missing provider for restored widget " + id2);
                                            }
                                            stashHostRestoreUpdateLocked(id2.host, restoredId, id2.appWidgetId);
                                            String str = TAG;
                                            StringBuilder append = new StringBuilder().append("   instance: ");
                                            int i = id2.appWidgetId;
                                            Slog.i(str, r29.append(restoredId).append(" -> ").append(r0).append(" :: p=").append(id2.provider).toString());
                                        }
                                    }
                                }
                            }
                        }
                        if (type == AppWidgetServiceImpl.CURRENT_VERSION) {
                            break;
                        }
                    }
                }
            } catch (XmlPullParserException e) {
                try {
                    Slog.w(TAG, "Unable to restore widget state for " + packageName);
                } finally {
                    AppWidgetServiceImpl.this.saveGroupStateAsync(userId);
                }
            }
        }

        public void restoreFinished(int userId) {
            Slog.i(TAG, "restoreFinished for " + userId);
            UserHandle userHandle = new UserHandle(userId);
            synchronized (AppWidgetServiceImpl.this.mLock) {
                int i;
                for (Entry<Provider, ArrayList<RestoreUpdateRecord>> e : this.mUpdatesByProvider.entrySet()) {
                    int[] oldIds;
                    int[] newIds;
                    int N;
                    int nextPending;
                    RestoreUpdateRecord r;
                    Provider provider = (Provider) e.getKey();
                    ArrayList<RestoreUpdateRecord> updates = (ArrayList) e.getValue();
                    int pending = countPendingUpdates(updates);
                    Slog.i(TAG, "Provider " + provider + " pending: " + pending);
                    if (pending > 0) {
                        oldIds = new int[pending];
                        newIds = new int[pending];
                        N = updates.size();
                        nextPending = AppWidgetServiceImpl.MIN_UPDATE_PERIOD;
                        for (i = AppWidgetServiceImpl.MIN_UPDATE_PERIOD; i < N; i += AppWidgetServiceImpl.CURRENT_VERSION) {
                            r = (RestoreUpdateRecord) updates.get(i);
                            if (!r.notified) {
                                r.notified = DEBUG;
                                oldIds[nextPending] = r.oldId;
                                newIds[nextPending] = r.newId;
                                nextPending += AppWidgetServiceImpl.CURRENT_VERSION;
                                Slog.i(TAG, "   " + r.oldId + " => " + r.newId);
                            }
                        }
                        sendWidgetRestoreBroadcastLocked("android.appwidget.action.APPWIDGET_RESTORED", provider, null, oldIds, newIds, userHandle);
                    }
                }
                for (Entry<Host, ArrayList<RestoreUpdateRecord>> e2 : this.mUpdatesByHost.entrySet()) {
                    Host host = (Host) e2.getKey();
                    if (host.id.uid != AppWidgetServiceImpl.UNKNOWN_UID) {
                        updates = (ArrayList) e2.getValue();
                        pending = countPendingUpdates(updates);
                        Slog.i(TAG, "Host " + host + " pending: " + pending);
                        if (pending > 0) {
                            oldIds = new int[pending];
                            newIds = new int[pending];
                            N = updates.size();
                            nextPending = AppWidgetServiceImpl.MIN_UPDATE_PERIOD;
                            for (i = AppWidgetServiceImpl.MIN_UPDATE_PERIOD; i < N; i += AppWidgetServiceImpl.CURRENT_VERSION) {
                                r = (RestoreUpdateRecord) updates.get(i);
                                if (!r.notified) {
                                    r.notified = DEBUG;
                                    oldIds[nextPending] = r.oldId;
                                    newIds[nextPending] = r.newId;
                                    nextPending += AppWidgetServiceImpl.CURRENT_VERSION;
                                    Slog.i(TAG, "   " + r.oldId + " => " + r.newId);
                                }
                            }
                            sendWidgetRestoreBroadcastLocked("android.appwidget.action.APPWIDGET_HOST_RESTORED", null, host, oldIds, newIds, userHandle);
                        }
                    }
                }
            }
        }

        private Provider findProviderLocked(ComponentName componentName, int userId) {
            int providerCount = AppWidgetServiceImpl.this.mProviders.size();
            for (int i = AppWidgetServiceImpl.MIN_UPDATE_PERIOD; i < providerCount; i += AppWidgetServiceImpl.CURRENT_VERSION) {
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
            for (int i = AppWidgetServiceImpl.MIN_UPDATE_PERIOD; i < N; i += AppWidgetServiceImpl.CURRENT_VERSION) {
                Widget widget = (Widget) AppWidgetServiceImpl.this.mWidgets.get(i);
                if (widget.restoredId == restoredId && widget.host.id.equals(host.id) && widget.provider.id.equals(p.id)) {
                    Slog.i(TAG, "   Found at " + i + " : " + widget);
                    return widget;
                }
            }
            return null;
        }

        private boolean packageNeedsWidgetBackupLocked(String packageName, int userId) {
            int N = AppWidgetServiceImpl.this.mWidgets.size();
            for (int i = AppWidgetServiceImpl.MIN_UPDATE_PERIOD; i < N; i += AppWidgetServiceImpl.CURRENT_VERSION) {
                Widget widget = (Widget) AppWidgetServiceImpl.this.mWidgets.get(i);
                if (isProviderAndHostInUser(widget, userId)) {
                    if (widget.host.isInPackageForUser(packageName, userId)) {
                        return DEBUG;
                    }
                    Provider provider = widget.provider;
                    if (provider != null && provider.isInPackageForUser(packageName, userId)) {
                        return DEBUG;
                    }
                }
            }
            return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
        }

        private void stashProviderRestoreUpdateLocked(Provider provider, int oldId, int newId) {
            ArrayList<RestoreUpdateRecord> r = (ArrayList) this.mUpdatesByProvider.get(provider);
            if (r == null) {
                r = new ArrayList();
                this.mUpdatesByProvider.put(provider, r);
            } else if (alreadyStashed(r, oldId, newId)) {
                Slog.i(TAG, "ID remap " + oldId + " -> " + newId + " already stashed for " + provider);
                return;
            }
            r.add(new RestoreUpdateRecord(oldId, newId));
        }

        private boolean alreadyStashed(ArrayList<RestoreUpdateRecord> stash, int oldId, int newId) {
            int N = stash.size();
            for (int i = AppWidgetServiceImpl.MIN_UPDATE_PERIOD; i < N; i += AppWidgetServiceImpl.CURRENT_VERSION) {
                RestoreUpdateRecord r = (RestoreUpdateRecord) stash.get(i);
                if (r.oldId == oldId && r.newId == newId) {
                    return DEBUG;
                }
            }
            return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
        }

        private void stashHostRestoreUpdateLocked(Host host, int oldId, int newId) {
            ArrayList<RestoreUpdateRecord> r = (ArrayList) this.mUpdatesByHost.get(host);
            if (r == null) {
                r = new ArrayList();
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
            if (this.mPrunedApps.contains(pkg)) {
                Slog.i(TAG, "already pruned " + pkg + ", continuing normally");
                return;
            }
            Slog.i(TAG, "pruning widget state for restoring package " + pkg);
            for (int i = AppWidgetServiceImpl.this.mWidgets.size() + AppWidgetServiceImpl.UNKNOWN_UID; i >= 0; i += AppWidgetServiceImpl.UNKNOWN_UID) {
                Widget widget = (Widget) AppWidgetServiceImpl.this.mWidgets.get(i);
                Host host = widget.host;
                Provider provider = widget.provider;
                if (host.hostsPackageForUser(pkg, userId) || (provider != null && provider.isInPackageForUser(pkg, userId))) {
                    host.widgets.remove(widget);
                    provider.widgets.remove(widget);
                    AppWidgetServiceImpl.this.unbindAppWidgetRemoteViewsServicesLocked(widget);
                    AppWidgetServiceImpl.this.removeWidgetLocked(widget);
                }
            }
            this.mPrunedApps.add(pkg);
        }

        private boolean isProviderAndHostInUser(Widget widget, int userId) {
            if (widget.host.getUserId() == userId) {
                return (widget.provider == null || widget.provider.getUserId() == userId) ? DEBUG : AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            } else {
                return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
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
            int pending = AppWidgetServiceImpl.MIN_UPDATE_PERIOD;
            int N = updates.size();
            for (int i = AppWidgetServiceImpl.MIN_UPDATE_PERIOD; i < N; i += AppWidgetServiceImpl.CURRENT_VERSION) {
                if (!((RestoreUpdateRecord) updates.get(i)).notified) {
                    pending += AppWidgetServiceImpl.CURRENT_VERSION;
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
            super(looper, null, AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET);
        }

        public void handleMessage(Message message) {
            SomeArgs args;
            Host host;
            IAppWidgetHost callbacks;
            RemoteViews views;
            int appWidgetId;
            switch (message.what) {
                case MSG_NOTIFY_UPDATE_APP_WIDGET /*1*/:
                    args = message.obj;
                    host = args.arg1;
                    callbacks = args.arg2;
                    views = args.arg3;
                    long requestTime = ((Long) args.arg4).longValue();
                    appWidgetId = args.argi1;
                    args.recycle();
                    AppWidgetServiceImpl.this.handleNotifyUpdateAppWidget(host, callbacks, appWidgetId, views, requestTime);
                case MSG_NOTIFY_PROVIDER_CHANGED /*2*/:
                    args = (SomeArgs) message.obj;
                    host = (Host) args.arg1;
                    callbacks = (IAppWidgetHost) args.arg2;
                    AppWidgetProviderInfo info = args.arg3;
                    appWidgetId = args.argi1;
                    args.recycle();
                    AppWidgetServiceImpl.this.handleNotifyProviderChanged(host, callbacks, appWidgetId, info);
                case MSG_NOTIFY_PROVIDERS_CHANGED /*3*/:
                    args = (SomeArgs) message.obj;
                    host = (Host) args.arg1;
                    callbacks = (IAppWidgetHost) args.arg2;
                    args.recycle();
                    AppWidgetServiceImpl.this.handleNotifyProvidersChanged(host, callbacks);
                case MSG_NOTIFY_VIEW_DATA_CHANGED /*4*/:
                    args = (SomeArgs) message.obj;
                    host = (Host) args.arg1;
                    callbacks = (IAppWidgetHost) args.arg2;
                    appWidgetId = args.argi1;
                    int viewId = args.argi2;
                    args.recycle();
                    AppWidgetServiceImpl.this.handleNotifyAppWidgetViewDataChanged(host, callbacks, appWidgetId, viewId);
                case MSG_NOTIFY_RECYCLE_REMOTE_VIEW /*20*/:
                    args = (SomeArgs) message.obj;
                    views = (RemoteViews) args.arg1;
                    if (views != null) {
                        views.recycle();
                    }
                    args.recycle();
                default:
            }
        }
    }

    private static final class Host {
        IAppWidgetHost callbacks;
        HostId id;
        long lastWidgetUpdateTime;
        int tag;
        ArrayList<Widget> widgets;
        boolean zombie;

        private Host() {
            this.widgets = new ArrayList();
            this.tag = AppWidgetServiceImpl.UNKNOWN_UID;
        }

        public int getUserId() {
            return UserHandle.getUserId(this.id.uid);
        }

        public boolean isInPackageForUser(String packageName, int userId) {
            return getUserId() == userId ? this.id.packageName.equals(packageName) : AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
        }

        private boolean hostsPackageForUser(String pkg, int userId) {
            int N = this.widgets.size();
            for (int i = AppWidgetServiceImpl.MIN_UPDATE_PERIOD; i < N; i += AppWidgetServiceImpl.CURRENT_VERSION) {
                Provider provider = ((Widget) this.widgets.get(i)).provider;
                if (provider != null && provider.getUserId() == userId && provider.info != null && pkg.equals(provider.info.provider.getPackageName())) {
                    return true;
                }
            }
            return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
        }

        public RemoteViews getPendingViewsForId(int appWidgetId) {
            long updateTime = this.lastWidgetUpdateTime;
            int N = this.widgets.size();
            for (int i = AppWidgetServiceImpl.MIN_UPDATE_PERIOD; i < N; i += AppWidgetServiceImpl.CURRENT_VERSION) {
                Widget widget = (Widget) this.widgets.get(i);
                if (widget.appWidgetId == appWidgetId && widget.lastUpdateTime > updateTime) {
                    return AppWidgetServiceImpl.cloneIfLocalBinder(widget.getEffectiveViewsLocked());
                }
            }
            return null;
        }

        public String toString() {
            return "Host{" + this.id + (this.zombie ? " Z" : "") + '}';
        }
    }

    private static final class HostId {
        final int hostId;
        final String packageName;
        final int uid;

        public HostId(int uid, int hostId, String packageName) {
            this.uid = uid;
            this.hostId = hostId;
            this.packageName = packageName;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
            HostId other = (HostId) obj;
            if (this.uid != other.uid || this.hostId != other.hostId) {
                return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
            if (this.packageName == null) {
                if (other.packageName != null) {
                    return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
                }
            } else if (!this.packageName.equals(other.packageName)) {
                return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
            return true;
        }

        public int hashCode() {
            return (((this.uid * 31) + this.hostId) * 31) + (this.packageName != null ? this.packageName.hashCode() : AppWidgetServiceImpl.MIN_UPDATE_PERIOD);
        }

        public String toString() {
            return "HostId{user:" + UserHandle.getUserId(this.uid) + ", app:" + UserHandle.getAppId(this.uid) + ", hostId:" + this.hostId + ", pkg:" + this.packageName + '}';
        }
    }

    private class LoadedWidgetState {
        final int hostTag;
        final int providerTag;
        final Widget widget;

        public LoadedWidgetState(Widget widget, int hostTag, int providerTag) {
            this.widget = widget;
            this.hostTag = hostTag;
            this.providerTag = providerTag;
        }
    }

    protected static final class Provider {
        PendingIntent broadcast;
        ProviderId id;
        AppWidgetProviderInfo info;
        boolean maskedByLockedProfile;
        boolean maskedByQuietProfile;
        boolean maskedBySuspendedPackage;
        int tag;
        ArrayList<Widget> widgets;
        boolean zombie;

        protected Provider() {
            this.widgets = new ArrayList();
            this.tag = AppWidgetServiceImpl.UNKNOWN_UID;
        }

        public int getUserId() {
            return UserHandle.getUserId(this.id.uid);
        }

        public boolean isInPackageForUser(String packageName, int userId) {
            if (getUserId() == userId) {
                return this.id.componentName.getPackageName().equals(packageName);
            }
            return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
        }

        public boolean hostedByPackageForUser(String packageName, int userId) {
            int N = this.widgets.size();
            for (int i = AppWidgetServiceImpl.MIN_UPDATE_PERIOD; i < N; i += AppWidgetServiceImpl.CURRENT_VERSION) {
                Widget widget = (Widget) this.widgets.get(i);
                if (packageName.equals(widget.host.id.packageName) && widget.host.getUserId() == userId) {
                    return true;
                }
            }
            return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
        }

        public String toString() {
            return "Provider{" + this.id + (this.zombie ? " Z" : "") + '}';
        }

        public boolean setMaskedByQuietProfileLocked(boolean masked) {
            boolean oldState = this.maskedByQuietProfile;
            this.maskedByQuietProfile = masked;
            return masked != oldState ? true : AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
        }

        public boolean setMaskedByLockedProfileLocked(boolean masked) {
            boolean oldState = this.maskedByLockedProfile;
            this.maskedByLockedProfile = masked;
            return masked != oldState ? true : AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
        }

        public boolean setMaskedBySuspendedPackageLocked(boolean masked) {
            boolean oldState = this.maskedBySuspendedPackage;
            this.maskedBySuspendedPackage = masked;
            return masked != oldState ? true : AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
        }

        public boolean isMaskedLocked() {
            return (this.maskedByQuietProfile || this.maskedByLockedProfile) ? true : this.maskedBySuspendedPackage;
        }
    }

    private static final class ProviderId {
        final ComponentName componentName;
        final int uid;

        private ProviderId(int uid, ComponentName componentName) {
            this.uid = uid;
            this.componentName = componentName;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
            ProviderId other = (ProviderId) obj;
            if (this.uid != other.uid) {
                return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
            if (this.componentName == null) {
                if (other.componentName != null) {
                    return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
                }
            } else if (!this.componentName.equals(other.componentName)) {
                return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
            return true;
        }

        public int hashCode() {
            return (this.uid * 31) + (this.componentName != null ? this.componentName.hashCode() : AppWidgetServiceImpl.MIN_UPDATE_PERIOD);
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
                AppWidgetServiceImpl.this.ensureGroupStateLoadedLocked(this.mUserId, AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET);
                AppWidgetServiceImpl.this.saveStateLocked(this.mUserId);
            }
        }
    }

    private final class SecurityPolicy {
        private SecurityPolicy() {
        }

        public boolean isEnabledGroupProfile(int profileId) {
            return isParentOrProfile(UserHandle.getCallingUserId(), profileId) ? isProfileEnabled(profileId) : AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
        }

        public int[] getEnabledGroupProfileIds(int userId) {
            int parentId = getGroupParent(userId);
            long identity = Binder.clearCallingIdentity();
            try {
                int[] enabledProfileIds = AppWidgetServiceImpl.this.mUserManager.getEnabledProfileIds(parentId);
                return enabledProfileIds;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void enforceServiceExistsAndRequiresBindRemoteViewsPermission(ComponentName componentName, int userId) {
            long identity = Binder.clearCallingIdentity();
            try {
                ServiceInfo serviceInfo = AppWidgetServiceImpl.this.mPackageManager.getServiceInfo(componentName, DumpState.DUMP_PREFERRED, userId);
                if (serviceInfo == null) {
                    throw new SecurityException("Service " + componentName + " not installed for user " + userId);
                } else if (!"android.permission.BIND_REMOTEVIEWS".equals(serviceInfo.permission)) {
                    throw new SecurityException("Service " + componentName + " in user " + userId + "does not require " + "android.permission.BIND_REMOTEVIEWS");
                }
            } catch (RemoteException e) {
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void enforceModifyAppWidgetBindPermissions(String packageName) {
            AppWidgetServiceImpl.this.mContext.enforceCallingPermission("android.permission.MODIFY_APPWIDGET_BIND_PERMISSIONS", "hasBindAppWidgetPermission packageName=" + packageName);
        }

        public void enforceCallFromPackage(String packageName) {
            AppWidgetServiceImpl.this.mAppOpsManager.checkPackage(Binder.getCallingUid(), packageName);
        }

        public boolean hasCallerBindPermissionOrBindWhiteListedLocked(String packageName) {
            try {
                AppWidgetServiceImpl.this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_APPWIDGET", null);
            } catch (SecurityException e) {
                if (!isCallerBindAppWidgetWhiteListedLocked(packageName)) {
                    return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
                }
            }
            return true;
        }

        private boolean isCallerBindAppWidgetWhiteListedLocked(String packageName) {
            int userId = UserHandle.getCallingUserId();
            if (AppWidgetServiceImpl.this.getUidForPackage(packageName, userId) < 0) {
                throw new IllegalArgumentException("No package " + packageName + " for user " + userId);
            }
            synchronized (AppWidgetServiceImpl.this.mLock) {
                AppWidgetServiceImpl.this.ensureGroupStateLoadedLocked(userId);
                if (AppWidgetServiceImpl.this.mPackagesWithBindWidgetPermission.contains(Pair.create(Integer.valueOf(userId), packageName))) {
                    return true;
                }
                return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
        }

        public boolean canAccessAppWidget(Widget widget, int uid, String packageName) {
            if (isHostInPackageForUid(widget.host, uid, packageName) || isProviderInPackageForUid(widget.provider, uid, packageName) || isHostAccessingProvider(widget.host, widget.provider, uid, packageName)) {
                return true;
            }
            int userId = UserHandle.getUserId(uid);
            return ((widget.host.getUserId() == userId || (widget.provider != null && widget.provider.getUserId() == userId)) && AppWidgetServiceImpl.this.mContext.checkCallingPermission("android.permission.BIND_APPWIDGET") == 0) ? true : AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
        }

        private boolean isParentOrProfile(int parentId, int profileId) {
            boolean z = true;
            if (parentId == profileId) {
                return true;
            }
            if (getProfileParent(profileId) != parentId) {
                z = AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
            return z;
        }

        public boolean isProviderInCallerOrInProfileAndWhitelListed(String packageName, int profileId) {
            int callerId = UserHandle.getCallingUserId();
            if (profileId == callerId) {
                return true;
            }
            if (getProfileParent(profileId) != callerId) {
                return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
            return isProviderWhiteListed(packageName, profileId);
        }

        public boolean isProviderWhiteListed(String packageName, int profileId) {
            if (AppWidgetServiceImpl.this.mDevicePolicyManagerInternal == null) {
                return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
            return AppWidgetServiceImpl.this.mDevicePolicyManagerInternal.getCrossProfileWidgetProviders(profileId).contains(packageName);
        }

        public int getProfileParent(int profileId) {
            long identity = Binder.clearCallingIdentity();
            try {
                UserInfo parent = AppWidgetServiceImpl.this.mUserManager.getProfileParent(profileId);
                if (parent != null) {
                    int identifier = parent.getUserHandle().getIdentifier();
                    return identifier;
                }
                Binder.restoreCallingIdentity(identity);
                return AppWidgetServiceImpl.UNKNOWN_USER_ID;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public int getGroupParent(int profileId) {
            int parentId = AppWidgetServiceImpl.this.mSecurityPolicy.getProfileParent(profileId);
            return parentId != AppWidgetServiceImpl.UNKNOWN_USER_ID ? parentId : profileId;
        }

        public boolean isHostInPackageForUid(Host host, int uid, String packageName) {
            return host.id.uid == uid ? host.id.packageName.equals(packageName) : AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
        }

        public boolean isProviderInPackageForUid(Provider provider, int uid, String packageName) {
            if (provider == null || provider.id.uid != uid) {
                return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
            return provider.id.componentName.getPackageName().equals(packageName);
        }

        public boolean isHostAccessingProvider(Host host, Provider provider, int uid, String packageName) {
            if (host.id.uid != uid || provider == null) {
                return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
            return provider.id.componentName.getPackageName().equals(packageName);
        }

        private boolean isProfileEnabled(int profileId) {
            long identity = Binder.clearCallingIdentity();
            try {
                UserInfo userInfo = AppWidgetServiceImpl.this.mUserManager.getUserInfo(profileId);
                if (userInfo != null && userInfo.isEnabled()) {
                    return true;
                }
                Binder.restoreCallingIdentity(identity);
                return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private static final class ServiceConnectionProxy implements ServiceConnection {
        private final IRemoteViewsAdapterConnection mConnectionCb;

        ServiceConnectionProxy(IBinder connectionCb) {
            this.mConnectionCb = IRemoteViewsAdapterConnection.Stub.asInterface(connectionCb);
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                this.mConnectionCb.onServiceConnected(service);
            } catch (RemoteException re) {
                Slog.e(AppWidgetServiceImpl.TAG, "Error passing service interface", re);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            disconnect();
        }

        public void disconnect() {
            try {
                this.mConnectionCb.onServiceDisconnected();
            } catch (RemoteException re) {
                Slog.e(AppWidgetServiceImpl.TAG, "Error clearing service interface", re);
            }
        }
    }

    protected static final class Widget {
        int appWidgetId;
        Host host;
        long lastUpdateTime;
        RemoteViews maskedViews;
        Bundle options;
        Provider provider;
        int restoredId;
        RemoteViews views;

        protected Widget() {
        }

        public String toString() {
            return "AppWidgetId{" + this.appWidgetId + ':' + this.host + ':' + this.provider + '}';
        }

        private boolean replaceWithMaskedViewsLocked(RemoteViews views) {
            this.maskedViews = views;
            return true;
        }

        private boolean clearMaskedViewsLocked() {
            if (this.maskedViews == null) {
                return AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET;
            }
            this.maskedViews = null;
            return true;
        }

        public RemoteViews getEffectiveViewsLocked() {
            return this.maskedViews != null ? this.maskedViews : this.views;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.appwidget.AppWidgetServiceImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.appwidget.AppWidgetServiceImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.appwidget.AppWidgetServiceImpl.<clinit>():void");
    }

    private android.graphics.Bitmap createMaskedWidgetBitmap(java.lang.String r10, int r11) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:12:? in {3, 8, 9, 11, 13, 14} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r9 = this;
        r2 = android.os.Binder.clearCallingIdentity();
        r6 = r9.mContext;	 Catch:{ NameNotFoundException -> 0x0026, all -> 0x0035 }
        r7 = android.os.UserHandle.of(r11);	 Catch:{ NameNotFoundException -> 0x0026, all -> 0x0035 }
        r8 = 0;	 Catch:{ NameNotFoundException -> 0x0026, all -> 0x0035 }
        r5 = r6.createPackageContextAsUser(r10, r8, r7);	 Catch:{ NameNotFoundException -> 0x0026, all -> 0x0035 }
        r4 = r5.getPackageManager();	 Catch:{ NameNotFoundException -> 0x0026, all -> 0x0035 }
        r6 = 0;	 Catch:{ NameNotFoundException -> 0x0026, all -> 0x0035 }
        r6 = r4.getApplicationInfo(r10, r6);	 Catch:{ NameNotFoundException -> 0x0026, all -> 0x0035 }
        r1 = r6.loadUnbadgedIcon(r4);	 Catch:{ NameNotFoundException -> 0x0026, all -> 0x0035 }
        r6 = r9.mIconUtilities;	 Catch:{ NameNotFoundException -> 0x0026, all -> 0x0035 }
        r6 = r6.createIconBitmap(r1);	 Catch:{ NameNotFoundException -> 0x0026, all -> 0x0035 }
        android.os.Binder.restoreCallingIdentity(r2);
        return r6;
    L_0x0026:
        r0 = move-exception;
        r6 = "AppWidgetServiceImpl";	 Catch:{ NameNotFoundException -> 0x0026, all -> 0x0035 }
        r7 = "Fail to get application icon";	 Catch:{ NameNotFoundException -> 0x0026, all -> 0x0035 }
        android.util.Slog.e(r6, r7, r0);	 Catch:{ NameNotFoundException -> 0x0026, all -> 0x0035 }
        r6 = 0;
        android.os.Binder.restoreCallingIdentity(r2);
        return r6;
    L_0x0035:
        r6 = move-exception;
        android.os.Binder.restoreCallingIdentity(r2);
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.appwidget.AppWidgetServiceImpl.createMaskedWidgetBitmap(java.lang.String, int):android.graphics.Bitmap");
    }

    AppWidgetServiceImpl(Context context) {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                if (AppWidgetServiceImpl.DEBUG) {
                    Slog.i(AppWidgetServiceImpl.TAG, "Received broadcast: " + action + " on user " + userId);
                }
                if ("android.intent.action.CONFIGURATION_CHANGED".equals(action)) {
                    AppWidgetServiceImpl.this.onConfigurationChanged();
                } else if ("android.intent.action.MANAGED_PROFILE_AVAILABLE".equals(action) || "android.intent.action.MANAGED_PROFILE_UNAVAILABLE".equals(action)) {
                    synchronized (AppWidgetServiceImpl.this.mLock) {
                        AppWidgetServiceImpl.this.reloadWidgetsMaskedState(userId);
                    }
                } else if ("android.intent.action.PACKAGES_SUSPENDED".equals(action)) {
                    AppWidgetServiceImpl.this.updateWidgetPackageSuspensionMaskedState(intent.getStringArrayExtra("android.intent.extra.changed_package_list"), true, getSendingUserId());
                } else if ("android.intent.action.PACKAGES_UNSUSPENDED".equals(action)) {
                    AppWidgetServiceImpl.this.updateWidgetPackageSuspensionMaskedState(intent.getStringArrayExtra("android.intent.extra.changed_package_list"), AppWidgetServiceImpl.HIDE_HUAWEI_WEATHER_WIDGET, getSendingUserId());
                } else {
                    AppWidgetServiceImpl.this.onPackageBroadcastReceived(intent, userId);
                }
            }
        };
        this.mBoundRemoteViewsServices = new HashMap();
        this.mRemoteViewsServicesAppWidgets = new HashMap();
        this.mLock = new Object();
        this.mWidgets = new ArrayList();
        this.mHosts = new ArrayList();
        this.mProviders = new ArrayList();
        this.mPackagesWithBindWidgetPermission = new ArraySet();
        this.mLoadedUserIds = new SparseIntArray();
        this.mWidgetPackages = new SparseArray();
        this.mNextAppWidgetIds = new SparseIntArray();
        this.mContext = context;
        this.mPackageManager = AppGlobals.getPackageManager();
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mDevicePolicyManagerInternal = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        this.mSaveStateHandler = BackgroundThread.getHandler();
        this.mCallbackHandler = new CallbackHandler(this.mContext.getMainLooper());
        this.mBackupRestoreController = new BackupRestoreController();
        this.mSecurityPolicy = new SecurityPolicy();
        this.mIconUtilities = new IconUtilities(context);
        computeMaximumWidgetBitmapMemory();
        this.mLocale = Locale.getDefault();
        registerBroadcastReceiver();
        registerOnCrossProfileProvidersChangedListener();
    }

    private void computeMaximumWidgetBitmapMemory() {
        Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        this.mMaxWidgetBitmapMemory = (size.x * 6) * size.y;
    }

    private void registerBroadcastReceiver() {
        IntentFilter configFilter = new IntentFilter();
        configFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, configFilter, null, null);
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onConfigurationChanged() {
        Throwable th;
        if (DEBUG) {
            Slog.i(TAG, "onConfigurationChanged()");
        }
        Locale revised = Locale.getDefault();
        if (revised == null || this.mLocale == null || !revised.equals(this.mLocale)) {
            this.mLocale = revised;
            synchronized (this.mLock) {
                try {
                    ArrayList<Provider> installedProviders = new ArrayList(this.mProviders);
                    HashSet<ProviderId> removedProviders = new HashSet();
                    int i = installedProviders.size() + UNKNOWN_UID;
                    SparseIntArray changedGroups = null;
                    while (i >= 0) {
                        SparseIntArray changedGroups2;
                        try {
                            Provider provider = (Provider) installedProviders.get(i);
                            int userId = provider.getUserId();
                            if (!this.mUserManager.isUserUnlockingOrUnlocked(userId) || isProfileWithLockedParent(userId)) {
                                changedGroups2 = changedGroups;
                            } else {
                                ensureGroupStateLoadedLocked(userId);
                                if (removedProviders.contains(provider.id) || !updateProvidersForPackageLocked(provider.id.componentName.getPackageName(), provider.getUserId(), removedProviders)) {
                                    changedGroups2 = changedGroups;
                                } else {
                                    if (changedGroups == null) {
                                        changedGroups2 = new SparseIntArray();
                                    } else {
                                        changedGroups2 = changedGroups;
                                    }
                                    int groupId = this.mSecurityPolicy.getGroupParent(provider.getUserId());
                                    changedGroups2.put(groupId, groupId);
                                }
                            }
                            i += UNKNOWN_UID;
                            changedGroups = changedGroups2;
                        } catch (Throwable th2) {
                            th = th2;
                            changedGroups2 = changedGroups;
                        }
                    }
                    if (changedGroups != null) {
                        int groupCount = changedGroups.size();
                        for (i = MIN_UPDATE_PERIOD; i < groupCount; i += CURRENT_VERSION) {
                            saveGroupStateAsync(changedGroups.get(i));
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                }
            }
        }
    }

    private void onPackageBroadcastReceived(Intent intent, int userId) {
        if (this.mUserManager.isUserUnlockingOrUnlocked(userId) && !isProfileWithLockedParent(userId)) {
            String[] pkgList;
            boolean added;
            String pkgName;
            String action = intent.getAction();
            boolean changed = HIDE_HUAWEI_WEATHER_WIDGET;
            boolean componentsModified = HIDE_HUAWEI_WEATHER_WIDGET;
            if ("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE".equals(action)) {
                pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                added = true;
            } else if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                added = HIDE_HUAWEI_WEATHER_WIDGET;
            } else {
                Uri uri = intent.getData();
                if (uri != null) {
                    pkgName = uri.getSchemeSpecificPart();
                    if (pkgName != null) {
                        pkgList = new String[CURRENT_VERSION];
                        pkgList[MIN_UPDATE_PERIOD] = pkgName;
                        added = "android.intent.action.PACKAGE_ADDED".equals(action);
                        changed = "android.intent.action.PACKAGE_CHANGED".equals(action);
                    } else {
                        return;
                    }
                }
                return;
            }
            if (pkgList != null && pkgList.length != 0) {
                synchronized (this.mLock) {
                    ensureGroupStateLoadedLocked(userId);
                    Bundle extras = intent.getExtras();
                    int i;
                    if (added || r5) {
                        boolean newPackageAdded = added ? extras != null ? extras.getBoolean("android.intent.extra.REPLACING", HIDE_HUAWEI_WEATHER_WIDGET) ? HIDE_HUAWEI_WEATHER_WIDGET : true : true : HIDE_HUAWEI_WEATHER_WIDGET;
                        int length = pkgList.length;
                        for (i = MIN_UPDATE_PERIOD; i < length; i += CURRENT_VERSION) {
                            pkgName = pkgList[i];
                            componentsModified |= updateProvidersForPackageLocked(pkgName, userId, null);
                            if (newPackageAdded && userId == 0) {
                                int uid = getUidForPackage(pkgName, userId);
                                if (uid >= 0) {
                                    resolveHostUidLocked(pkgName, uid);
                                }
                            }
                        }
                    } else {
                        boolean packageRemovedPermanently = extras != null ? extras.getBoolean("android.intent.extra.REPLACING", HIDE_HUAWEI_WEATHER_WIDGET) ? HIDE_HUAWEI_WEATHER_WIDGET : true : true;
                        if (packageRemovedPermanently) {
                            for (i = MIN_UPDATE_PERIOD; i < pkgList.length; i += CURRENT_VERSION) {
                                componentsModified |= removeHostsAndProvidersForPackageLocked(pkgList[i], userId);
                            }
                        }
                    }
                    if (componentsModified) {
                        saveGroupStateAsync(userId);
                        scheduleNotifyGroupHostsForProvidersChangedLocked(userId);
                    }
                }
            }
        }
    }

    void reloadWidgetsMaskedStateForGroup(int userId) {
        if (this.mUserManager.isUserUnlockingOrUnlocked(userId)) {
            synchronized (this.mLock) {
                reloadWidgetsMaskedState(userId);
                int[] profileIds = this.mUserManager.getEnabledProfileIds(userId);
                int length = profileIds.length;
                for (int i = MIN_UPDATE_PERIOD; i < length; i += CURRENT_VERSION) {
                    reloadWidgetsMaskedState(profileIds[i]);
                }
            }
        }
    }

    private void reloadWidgetsMaskedState(int userId) {
        long identity = Binder.clearCallingIdentity();
        UserInfo user = this.mUserManager.getUserInfo(userId);
        boolean lockedProfile = this.mUserManager.isUserUnlockingOrUnlocked(userId) ? HIDE_HUAWEI_WEATHER_WIDGET : true;
        boolean quietProfile = user.isQuietModeEnabled();
        int N = this.mProviders.size();
        for (int i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
            Provider provider = (Provider) this.mProviders.get(i);
            if (provider.getUserId() == userId) {
                boolean isPackageSuspendedForUser;
                boolean changed = provider.setMaskedByLockedProfileLocked(lockedProfile) | provider.setMaskedByQuietProfileLocked(quietProfile);
                try {
                    isPackageSuspendedForUser = this.mPackageManager.isPackageSuspendedForUser(provider.info.provider.getPackageName(), provider.getUserId());
                } catch (IllegalArgumentException e) {
                    isPackageSuspendedForUser = HIDE_HUAWEI_WEATHER_WIDGET;
                }
                try {
                    changed |= provider.setMaskedBySuspendedPackageLocked(isPackageSuspendedForUser);
                } catch (RemoteException e2) {
                    Slog.e(TAG, "Failed to query application info", e2);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
                if (!changed) {
                    continue;
                } else if (provider.isMaskedLocked()) {
                    maskWidgetsViewsLocked(provider, null);
                } else {
                    unmaskWidgetsViewsLocked(provider);
                }
            }
        }
        Binder.restoreCallingIdentity(identity);
    }

    private void updateWidgetPackageSuspensionMaskedState(String[] packagesArray, boolean suspended, int profileId) {
        if (packagesArray != null) {
            Set<String> packages = new ArraySet(Arrays.asList(packagesArray));
            synchronized (this.mLock) {
                int N = this.mProviders.size();
                for (int i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
                    Provider provider = (Provider) this.mProviders.get(i);
                    if (provider.getUserId() == profileId && packages.contains(provider.info.provider.getPackageName()) && provider.setMaskedBySuspendedPackageLocked(suspended)) {
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

    private RemoteViews createMaskedWidgetRemoteViews(Bitmap icon, boolean showBadge, PendingIntent onClickIntent) {
        RemoteViews views = new RemoteViews(this.mContext.getPackageName(), 17367308);
        if (icon != null) {
            views.setImageViewBitmap(16909386, icon);
        }
        if (!showBadge) {
            views.setViewVisibility(16909387, 4);
        }
        if (onClickIntent != null) {
            views.setOnClickPendingIntent(16909385, onClickIntent);
        }
        return views;
    }

    private void maskWidgetsViewsLocked(Provider provider, Widget targetWidget) {
        int widgetCount = provider.widgets.size();
        if (widgetCount != 0) {
            String providerPackage = provider.info.provider.getPackageName();
            int providerUserId = provider.getUserId();
            Bitmap iconBitmap = createMaskedWidgetBitmap(providerPackage, providerUserId);
            if (iconBitmap != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    boolean showBadge;
                    Intent onClickIntent;
                    if (provider.maskedBySuspendedPackage) {
                        showBadge = this.mUserManager.getUserInfo(providerUserId).isManagedProfile();
                        onClickIntent = this.mDevicePolicyManagerInternal.createPackageSuspendedDialogIntent(providerPackage, providerUserId);
                    } else if (provider.maskedByQuietProfile) {
                        showBadge = true;
                        onClickIntent = UnlaunchableAppActivity.createInQuietModeDialogIntent(providerUserId);
                    } else {
                        showBadge = true;
                        onClickIntent = this.mKeyguardManager.createConfirmDeviceCredentialIntent(null, null, providerUserId);
                        if (onClickIntent != null) {
                            onClickIntent.setFlags(276824064);
                        }
                    }
                    for (int j = MIN_UPDATE_PERIOD; j < widgetCount; j += CURRENT_VERSION) {
                        Widget widget = (Widget) provider.widgets.get(j);
                        if (targetWidget == null || targetWidget == widget) {
                            PendingIntent intent = null;
                            if (onClickIntent != null) {
                                intent = PendingIntent.getActivity(this.mContext, widget.appWidgetId, onClickIntent, 134217728);
                            }
                            if (widget.replaceWithMaskedViewsLocked(createMaskedWidgetRemoteViews(iconBitmap, showBadge, intent))) {
                                scheduleNotifyUpdateAppWidgetLocked(widget, widget.getEffectiveViewsLocked());
                            }
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    private void unmaskWidgetsViewsLocked(Provider provider) {
        int widgetCount = provider.widgets.size();
        for (int j = MIN_UPDATE_PERIOD; j < widgetCount; j += CURRENT_VERSION) {
            Widget widget = (Widget) provider.widgets.get(j);
            if (widget.clearMaskedViewsLocked()) {
                scheduleNotifyUpdateAppWidgetLocked(widget, widget.getEffectiveViewsLocked());
            }
        }
    }

    private void resolveHostUidLocked(String pkg, int uid) {
        int N = this.mHosts.size();
        for (int i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
            Host host = (Host) this.mHosts.get(i);
            if (host.id.uid == UNKNOWN_UID && pkg.equals(host.id.packageName)) {
                if (DEBUG) {
                    Slog.i(TAG, "host " + host.id + " resolved to uid " + uid);
                }
                host.id = new HostId(uid, host.id.hostId, host.id.packageName);
                return;
            }
        }
    }

    private void ensureGroupStateLoadedLocked(int userId) {
        ensureGroupStateLoadedLocked(userId, true);
    }

    private void ensureGroupStateLoadedLocked(int userId, boolean enforceUserUnlockingOrUnlocked) {
        if (enforceUserUnlockingOrUnlocked && !isUserRunningAndUnlocked(userId)) {
            throw new IllegalStateException("User " + userId + " must be unlocked for widgets to be available");
        } else if (enforceUserUnlockingOrUnlocked && isProfileWithLockedParent(userId)) {
            throw new IllegalStateException("Profile " + userId + " must have unlocked parent");
        } else {
            int i;
            int[] profileIds = this.mSecurityPolicy.getEnabledGroupProfileIds(userId);
            int newMemberCount = MIN_UPDATE_PERIOD;
            int profileIdCount = profileIds.length;
            for (i = MIN_UPDATE_PERIOD; i < profileIdCount; i += CURRENT_VERSION) {
                if (this.mLoadedUserIds.indexOfKey(profileIds[i]) >= 0) {
                    profileIds[i] = UNKNOWN_UID;
                } else {
                    newMemberCount += CURRENT_VERSION;
                }
            }
            if (newMemberCount > 0) {
                int newMemberIndex = MIN_UPDATE_PERIOD;
                int[] newProfileIds = new int[newMemberCount];
                for (i = MIN_UPDATE_PERIOD; i < profileIdCount; i += CURRENT_VERSION) {
                    int profileId = profileIds[i];
                    if (profileId != UNKNOWN_UID) {
                        this.mLoadedUserIds.put(profileId, profileId);
                        newProfileIds[newMemberIndex] = profileId;
                        newMemberIndex += CURRENT_VERSION;
                    }
                }
                clearProvidersAndHostsTagsLocked();
                loadGroupWidgetProvidersLocked(newProfileIds);
                loadGroupStateLocked(newProfileIds);
            }
        }
    }

    private boolean isUserRunningAndUnlocked(int userId) {
        return this.mUserManager.isUserRunning(userId) ? StorageManager.isUserKeyUnlocked(userId) : HIDE_HUAWEI_WEATHER_WIDGET;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", "Permission Denial: can't dump from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        synchronized (this.mLock) {
            int i;
            int N = this.mProviders.size();
            pw.println("Providers:");
            for (i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
                dumpProvider((Provider) this.mProviders.get(i), i, pw);
            }
            N = this.mWidgets.size();
            pw.println(" ");
            pw.println("Widgets:");
            for (i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
                dumpWidget((Widget) this.mWidgets.get(i), i, pw);
            }
            N = this.mHosts.size();
            pw.println(" ");
            pw.println("Hosts:");
            for (i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
                dumpHost((Host) this.mHosts.get(i), i, pw);
            }
            N = this.mPackagesWithBindWidgetPermission.size();
            pw.println(" ");
            pw.println("Grants:");
            for (i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
                dumpGrant((Pair) this.mPackagesWithBindWidgetPermission.valueAt(i), i, pw);
            }
        }
    }

    public ParceledListSlice<RemoteViews> startListening(IAppWidgetHost callbacks, String callingPackage, int hostId, int[] appWidgetIds, int[] updatedIds) {
        ParceledListSlice<RemoteViews> parceledListSlice;
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "startListening() " + userId);
        }
        Log.d(TAG, "startListening:callingpackage" + callingPackage);
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Host host = lookupOrAddHostLocked(new HostId(Binder.getCallingUid(), hostId, callingPackage));
            host.callbacks = callbacks;
            int N = appWidgetIds.length;
            ArrayList<RemoteViews> outViews = new ArrayList(N);
            int added = MIN_UPDATE_PERIOD;
            for (int i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
                RemoteViews rv = host.getPendingViewsForId(appWidgetIds[i]);
                if (rv != null) {
                    updatedIds[added] = appWidgetIds[i];
                    outViews.add(rv);
                    added += CURRENT_VERSION;
                }
            }
            parceledListSlice = new ParceledListSlice(outViews);
        }
        return parceledListSlice;
    }

    public void stopListening(String callingPackage, int hostId) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "stopListening() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Host host = lookupHostLocked(new HostId(Binder.getCallingUid(), hostId, callingPackage));
            if (host != null) {
                host.callbacks = null;
                pruneHostLocked(host);
            }
        }
    }

    public int allocateAppWidgetId(String callingPackage, int hostId) {
        int appWidgetId;
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "allocateAppWidgetId() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            if (this.mNextAppWidgetIds.indexOfKey(userId) < 0) {
                this.mNextAppWidgetIds.put(userId, CURRENT_VERSION);
            }
            appWidgetId = incrementAndGetAppWidgetIdLocked(userId);
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
        return appWidgetId;
    }

    public void deleteAppWidgetId(String callingPackage, int appWidgetId) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "deleteAppWidgetId() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Widget widget = lookupWidgetLocked(appWidgetId, Binder.getCallingUid(), callingPackage);
            if (widget == null) {
                return;
            }
            deleteAppWidgetLocked(widget);
            saveGroupStateAsync(userId);
            if (DEBUG) {
                Slog.i(TAG, "Deleted widget id " + appWidgetId + " for host " + widget.host.id);
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
                return HIDE_HUAWEI_WEATHER_WIDGET;
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
            if (getUidForPackage(packageName, grantId) < 0) {
                return;
            }
            Pair<Integer, String> packageId = Pair.create(Integer.valueOf(grantId), packageName);
            if (grantPermission) {
                this.mPackagesWithBindWidgetPermission.add(packageId);
            } else {
                this.mPackagesWithBindWidgetPermission.remove(packageId);
            }
            saveGroupStateAsync(grantId);
        }
    }

    public IntentSender createAppWidgetConfigIntentSender(String callingPackage, int appWidgetId) {
        IntentSender intentSender;
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "createAppWidgetConfigIntentSender() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Widget widget = lookupWidgetLocked(appWidgetId, Binder.getCallingUid(), callingPackage);
            if (widget == null) {
                throw new IllegalArgumentException("Bad widget id " + appWidgetId);
            }
            Provider provider = widget.provider;
            if (provider == null) {
                throw new IllegalArgumentException("Widget not bound " + appWidgetId);
            }
            Intent intent = new Intent("android.appwidget.action.APPWIDGET_CONFIGURE");
            intent.putExtra("appWidgetId", appWidgetId);
            intent.setComponent(provider.info.configure);
            long identity = Binder.clearCallingIdentity();
            try {
                intentSender = PendingIntent.getActivityAsUser(this.mContext, MIN_UPDATE_PERIOD, intent, 1409286144, null, new UserHandle(provider.getUserId())).getIntentSender();
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
        return intentSender;
    }

    public boolean bindAppWidgetId(String callingPackage, int appWidgetId, int providerProfileId, ComponentName providerComponent, Bundle options) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "bindAppWidgetId() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        if (!this.mSecurityPolicy.isEnabledGroupProfile(providerProfileId)) {
            return HIDE_HUAWEI_WEATHER_WIDGET;
        }
        if (!this.mSecurityPolicy.isProviderInCallerOrInProfileAndWhitelListed(providerComponent.getPackageName(), providerProfileId)) {
            return HIDE_HUAWEI_WEATHER_WIDGET;
        }
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            if (this.mSecurityPolicy.hasCallerBindPermissionOrBindWhiteListedLocked(callingPackage)) {
                Widget widget = lookupWidgetLocked(appWidgetId, Binder.getCallingUid(), callingPackage);
                if (widget == null) {
                    Slog.e(TAG, "Bad widget id " + appWidgetId);
                    return HIDE_HUAWEI_WEATHER_WIDGET;
                } else if (widget.provider != null) {
                    Slog.e(TAG, "Widget id " + appWidgetId + " already bound to: " + widget.provider.id);
                    return HIDE_HUAWEI_WEATHER_WIDGET;
                } else {
                    int providerUid = getUidForPackage(providerComponent.getPackageName(), providerProfileId);
                    if (providerUid < 0) {
                        Slog.e(TAG, "Package " + providerComponent.getPackageName() + " not installed " + " for profile " + providerProfileId);
                        return HIDE_HUAWEI_WEATHER_WIDGET;
                    }
                    Provider provider = lookupProviderLocked(new ProviderId(providerComponent, null));
                    if (provider == null) {
                        Slog.e(TAG, "No widget provider " + providerComponent + " for profile " + providerProfileId);
                        return HIDE_HUAWEI_WEATHER_WIDGET;
                    } else if (provider.zombie) {
                        Slog.e(TAG, "Can't bind to a 3rd party provider in safe mode " + provider);
                        return HIDE_HUAWEI_WEATHER_WIDGET;
                    } else {
                        Bundle cloneIfLocalBinder;
                        widget.provider = provider;
                        if (options != null) {
                            cloneIfLocalBinder = cloneIfLocalBinder(options);
                        } else {
                            cloneIfLocalBinder = new Bundle();
                        }
                        widget.options = cloneIfLocalBinder;
                        if (!widget.options.containsKey("appWidgetCategory")) {
                            widget.options.putInt("appWidgetCategory", CURRENT_VERSION);
                        }
                        provider.widgets.add(widget);
                        onWidgetProviderAddedOrChangedLocked(widget);
                        if (provider.widgets.size() == CURRENT_VERSION) {
                            LogPower.push(168, providerComponent.getPackageName(), String.valueOf(providerUid));
                            sendEnableIntentLocked(provider);
                        }
                        int[] iArr = new int[CURRENT_VERSION];
                        iArr[MIN_UPDATE_PERIOD] = appWidgetId;
                        sendUpdateIntentLocked(provider, iArr);
                        registerForBroadcastsLocked(provider, getWidgetIds(provider.widgets));
                        saveGroupStateAsync(userId);
                        if (DEBUG) {
                            Slog.i(TAG, "Bound widget " + appWidgetId + " to provider " + provider.id);
                        }
                        return true;
                    }
                }
            }
            return HIDE_HUAWEI_WEATHER_WIDGET;
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
            Provider provider = lookupProviderLocked(new ProviderId(componentName, null));
            if (provider != null) {
                int[] widgetIds = getWidgetIds(provider.widgets);
                return widgetIds;
            }
            widgetIds = new int[MIN_UPDATE_PERIOD];
            return widgetIds;
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
            widgetIds = new int[MIN_UPDATE_PERIOD];
            return widgetIds;
        }
    }

    public void bindRemoteViewsService(String callingPackage, int appWidgetId, Intent intent, IBinder callbacks) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "bindRemoteViewsService() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Widget widget = lookupWidgetLocked(appWidgetId, Binder.getCallingUid(), callingPackage);
            if (widget == null) {
                throw new IllegalArgumentException("Bad widget id");
            } else if (widget.provider == null) {
                throw new IllegalArgumentException("No provider for widget " + appWidgetId);
            } else {
                ComponentName componentName = intent.getComponent();
                if (componentName.getPackageName().equals(widget.provider.id.componentName.getPackageName())) {
                    ServiceConnectionProxy connection;
                    this.mSecurityPolicy.enforceServiceExistsAndRequiresBindRemoteViewsPermission(componentName, widget.provider.getUserId());
                    FilterComparison fc = new FilterComparison(intent);
                    Pair<Integer, FilterComparison> key = Pair.create(Integer.valueOf(appWidgetId), fc);
                    if (this.mBoundRemoteViewsServices.containsKey(key)) {
                        connection = (ServiceConnectionProxy) this.mBoundRemoteViewsServices.get(key);
                        connection.disconnect();
                        unbindService(connection);
                        this.mBoundRemoteViewsServices.remove(key);
                    }
                    connection = new ServiceConnectionProxy(callbacks);
                    bindService(intent, connection, widget.provider.info.getProfile());
                    this.mBoundRemoteViewsServices.put(key, connection);
                    incrementAppWidgetServiceRefCount(appWidgetId, Pair.create(Integer.valueOf(widget.provider.id.uid), fc));
                } else {
                    throw new SecurityException("The taget service not in the same package as the widget provider");
                }
            }
        }
    }

    public void unbindRemoteViewsService(String callingPackage, int appWidgetId, Intent intent) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "unbindRemoteViewsService() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Pair<Integer, FilterComparison> key = Pair.create(Integer.valueOf(appWidgetId), new FilterComparison(intent));
            if (this.mBoundRemoteViewsServices.containsKey(key)) {
                if (lookupWidgetLocked(appWidgetId, Binder.getCallingUid(), callingPackage) == null) {
                    throw new IllegalArgumentException("Bad widget id " + appWidgetId);
                }
                ServiceConnectionProxy connection = (ServiceConnectionProxy) this.mBoundRemoteViewsServices.get(key);
                connection.disconnect();
                this.mContext.unbindService(connection);
                this.mBoundRemoteViewsServices.remove(key);
            }
        }
    }

    public void deleteHost(String callingPackage, int hostId) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "deleteHost() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            Host host = lookupHostLocked(new HostId(Binder.getCallingUid(), hostId, callingPackage));
            if (host == null) {
                return;
            }
            deleteHostLocked(host);
            saveGroupStateAsync(userId);
            if (DEBUG) {
                Slog.i(TAG, "Deleted host " + host.id);
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
            boolean changed = HIDE_HUAWEI_WEATHER_WIDGET;
            for (int i = this.mHosts.size() + UNKNOWN_UID; i >= 0; i += UNKNOWN_UID) {
                Host host = (Host) this.mHosts.get(i);
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
            AppWidgetProviderInfo cloneIfLocalBinder = cloneIfLocalBinder(widget.provider.info);
            return cloneIfLocalBinder;
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
            if (widget != null) {
                RemoteViews cloneIfLocalBinder = cloneIfLocalBinder(widget.getEffectiveViewsLocked());
                return cloneIfLocalBinder;
            }
            return null;
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
            if (widget == null) {
                return;
            }
            widget.options.putAll(options);
            sendOptionsChangedIntentLocked(widget);
            saveGroupStateAsync(userId);
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
            bundle = cloneIfLocalBinder(widget.options);
            return bundle;
        }
    }

    public void updateAppWidgetIds(String callingPackage, int[] appWidgetIds, RemoteViews views) {
        if (DEBUG) {
            Slog.i(TAG, "updateAppWidgetIds() " + UserHandle.getCallingUserId());
        }
        updateAppWidgetIds(callingPackage, appWidgetIds, views, HIDE_HUAWEI_WEATHER_WIDGET);
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
                int N = appWidgetIds.length;
                for (int i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
                    Widget widget = lookupWidgetLocked(appWidgetIds[i], Binder.getCallingUid(), callingPackage);
                    if (widget != null) {
                        scheduleNotifyAppWidgetViewDataChanged(widget, viewId);
                    }
                }
            }
        }
    }

    public void updateAppWidgetProvider(ComponentName componentName, RemoteViews views) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "updateAppWidgetProvider() " + userId);
        }
        this.mSecurityPolicy.enforceCallFromPackage(componentName.getPackageName());
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            ProviderId providerId = new ProviderId(componentName, null);
            Provider provider = lookupProviderLocked(providerId);
            if (provider == null) {
                Slog.w(TAG, "Provider doesn't exist " + providerId);
                return;
            }
            ArrayList<Widget> instances = provider.widgets;
            int N = instances.size();
            for (int i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
                updateAppWidgetInstanceLocked((Widget) instances.get(i), views, HIDE_HUAWEI_WEATHER_WIDGET);
            }
        }
    }

    public ParceledListSlice<AppWidgetProviderInfo> getInstalledProvidersForProfile(int categoryFilter, int profileId) {
        int userId = UserHandle.getCallingUserId();
        if (DEBUG) {
            Slog.i(TAG, "getInstalledProvidersForProfiles() " + userId);
        }
        if (!this.mSecurityPolicy.isEnabledGroupProfile(profileId)) {
            return null;
        }
        ParceledListSlice<AppWidgetProviderInfo> parceledListSlice;
        synchronized (this.mLock) {
            ensureGroupStateLoadedLocked(userId);
            ArrayList<AppWidgetProviderInfo> result = new ArrayList();
            int providerCount = this.mProviders.size();
            for (int i = MIN_UPDATE_PERIOD; i < providerCount; i += CURRENT_VERSION) {
                Provider provider = (Provider) this.mProviders.get(i);
                AppWidgetProviderInfo info = provider.info;
                if (!(provider.zombie || (info.widgetCategory & categoryFilter) == 0)) {
                    ComponentName cn = info.provider;
                    if (!HIDE_HUAWEI_WEATHER_WIDGET || cn == null || !HIDDEN_WEATHER_WIDGETS.containsKey(cn.getClassName()) || !isThirdPartyLauncherActive()) {
                        int providerProfileId = info.getProfile().getIdentifier();
                        if (providerProfileId == profileId && this.mSecurityPolicy.isProviderInCallerOrInProfileAndWhitelListed(provider.id.componentName.getPackageName(), providerProfileId)) {
                            result.add(cloneIfLocalBinder(info));
                        }
                    }
                }
            }
            parceledListSlice = new ParceledListSlice(result);
        }
        return parceledListSlice;
    }

    private boolean isThirdPartyLauncherActive() {
        boolean isThridPartylauncherActive = HIDE_HUAWEI_WEATHER_WIDGET;
        Intent launcherIntent = new Intent("android.intent.action.MAIN");
        launcherIntent.addCategory("android.intent.category.HOME");
        List<ComponentName> prefActList = new ArrayList();
        List<IntentFilter> intentList = new ArrayList();
        PackageManager mPm = this.mContext.getPackageManager();
        if (mPm == null) {
            return HIDE_HUAWEI_WEATHER_WIDGET;
        }
        int userId = UserHandle.getCallingUserId();
        long origId = Binder.clearCallingIdentity();
        List<ResolveInfo> list = null;
        try {
            list = mPm.queryIntentActivitiesAsUser(launcherIntent, MIN_UPDATE_PERIOD, userId);
        } catch (Exception e) {
            Log.e(TAG, "isThirdPartyLauncherActive Exception:" + e);
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
        if (list == null) {
            return HIDE_HUAWEI_WEATHER_WIDGET;
        }
        for (ResolveInfo info : list) {
            mPm.getPreferredActivities(intentList, prefActList, info.activityInfo.packageName);
            if (prefActList.size() > 0) {
                if (HUAWEI_LAUNCHER_PACKAGE.equals(info.activityInfo.packageName)) {
                    isThridPartylauncherActive = HIDE_HUAWEI_WEATHER_WIDGET;
                } else {
                    isThridPartylauncherActive = true;
                }
                return isThridPartylauncherActive;
            }
        }
        return isThridPartylauncherActive;
    }

    private void updateAppWidgetIds(String callingPackage, int[] appWidgetIds, RemoteViews views, boolean partially) {
        int userId = UserHandle.getCallingUserId();
        if (appWidgetIds != null && appWidgetIds.length != 0) {
            this.mSecurityPolicy.enforceCallFromPackage(callingPackage);
            int bitmapMemoryUsage = views != null ? views.estimateMemoryUsage() : MIN_UPDATE_PERIOD;
            if (bitmapMemoryUsage > this.mMaxWidgetBitmapMemory) {
                throw new IllegalArgumentException("RemoteViews for widget update exceeds maximum bitmap memory usage (used: " + bitmapMemoryUsage + ", max: " + this.mMaxWidgetBitmapMemory + ")");
            }
            synchronized (this.mLock) {
                ensureGroupStateLoadedLocked(userId);
                int N = appWidgetIds.length;
                for (int i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
                    Widget widget = lookupWidgetLocked(appWidgetIds[i], Binder.getCallingUid(), callingPackage);
                    if (widget != null) {
                        updateAppWidgetInstanceLocked(widget, views, partially);
                    }
                }
            }
        }
    }

    private int incrementAndGetAppWidgetIdLocked(int userId) {
        int appWidgetId = peekNextAppWidgetIdLocked(userId) + CURRENT_VERSION;
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
            return CURRENT_VERSION;
        }
        return this.mNextAppWidgetIds.get(userId);
    }

    private Host lookupOrAddHostLocked(HostId id) {
        Host host = lookupHostLocked(id);
        if (host != null) {
            return host;
        }
        host = new Host();
        host.id = id;
        this.mHosts.add(host);
        return host;
    }

    private void deleteHostLocked(Host host) {
        for (int i = host.widgets.size() + UNKNOWN_UID; i >= 0; i += UNKNOWN_UID) {
            deleteAppWidgetLocked((Widget) host.widgets.remove(i));
        }
        this.mHosts.remove(host);
        host.callbacks = null;
    }

    private void deleteAppWidgetLocked(Widget widget) {
        unbindAppWidgetRemoteViewsServicesLocked(widget);
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

    private void cancelBroadcasts(Provider provider) {
        if (DEBUG) {
            Slog.i(TAG, "cancelBroadcasts() for " + provider);
        }
        if (provider.broadcast != null) {
            this.mAlarmManager.cancel(provider.broadcast);
            long token = Binder.clearCallingIdentity();
            try {
                provider.broadcast.cancel();
                provider.broadcast = null;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    private void unbindAppWidgetRemoteViewsServicesLocked(Widget widget) {
        int appWidgetId = widget.appWidgetId;
        Iterator<Pair<Integer, FilterComparison>> it = this.mBoundRemoteViewsServices.keySet().iterator();
        while (it.hasNext()) {
            Pair<Integer, FilterComparison> key = (Pair) it.next();
            if (((Integer) key.first).intValue() == appWidgetId) {
                ServiceConnectionProxy conn = (ServiceConnectionProxy) this.mBoundRemoteViewsServices.get(key);
                conn.disconnect();
                this.mContext.unbindService(conn);
                it.remove();
            }
        }
        decrementAppWidgetServiceRefCount(widget);
    }

    private void destroyRemoteViewsService(Intent intent, Widget widget) {
        ServiceConnection conn = new AnonymousClass2(intent);
        long token = Binder.clearCallingIdentity();
        try {
            this.mContext.bindServiceAsUser(intent, conn, 33554433, widget.provider.info.getProfile());
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void incrementAppWidgetServiceRefCount(int appWidgetId, Pair<Integer, FilterComparison> serviceId) {
        HashSet<Integer> appWidgetIds;
        if (this.mRemoteViewsServicesAppWidgets.containsKey(serviceId)) {
            appWidgetIds = (HashSet) this.mRemoteViewsServicesAppWidgets.get(serviceId);
        } else {
            appWidgetIds = new HashSet();
            this.mRemoteViewsServicesAppWidgets.put(serviceId, appWidgetIds);
        }
        appWidgetIds.add(Integer.valueOf(appWidgetId));
    }

    private void decrementAppWidgetServiceRefCount(Widget widget) {
        Iterator<Pair<Integer, FilterComparison>> it = this.mRemoteViewsServicesAppWidgets.keySet().iterator();
        while (it.hasNext()) {
            Pair<Integer, FilterComparison> key = (Pair) it.next();
            HashSet<Integer> ids = (HashSet) this.mRemoteViewsServicesAppWidgets.get(key);
            if (ids.remove(Integer.valueOf(widget.appWidgetId)) && ids.isEmpty()) {
                destroyRemoteViewsService(((FilterComparison) key.second).getIntent(), widget);
                it.remove();
            }
        }
    }

    private void saveGroupStateAsync(int groupId) {
        this.mSaveStateHandler.post(new SaveStateRunnable(groupId));
    }

    private void updateAppWidgetInstanceLocked(Widget widget, RemoteViews views, boolean isPartialUpdate) {
        RemoteViews remoteViews = null;
        if (widget != null && widget.provider != null && !widget.provider.zombie && !widget.host.zombie) {
            if (!isPartialUpdate || widget.views == null) {
                if (!(widget.views == null || widget.views == views)) {
                    remoteViews = widget.views;
                }
                widget.views = views;
            } else {
                widget.views.mergeRemoteViews(views);
            }
            scheduleNotifyUpdateAppWidgetLocked(widget, widget.getEffectiveViewsLocked());
            if (remoteViews != null) {
                Slog.i(TAG, "recycle oldRemoteView");
                scheduleRecyleRemoteView(remoteViews);
            }
        }
    }

    private void scheduleNotifyAppWidgetViewDataChanged(Widget widget, int viewId) {
        if (widget != null && widget.host != null && !widget.host.zombie && widget.host.callbacks != null && widget.provider != null && !widget.provider.zombie) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = widget.host;
            args.arg2 = widget.host.callbacks;
            args.argi1 = widget.appWidgetId;
            args.argi2 = viewId;
            this.mCallbackHandler.obtainMessage(4, args).sendToTarget();
        }
    }

    private void handleNotifyAppWidgetViewDataChanged(Host host, IAppWidgetHost callbacks, int appWidgetId, int viewId) {
        try {
            callbacks.viewDataChanged(appWidgetId, viewId);
        } catch (RemoteException e) {
            callbacks = null;
        }
        synchronized (this.mLock) {
            if (callbacks == null) {
                host.callbacks = null;
                for (Pair<Integer, FilterComparison> key : this.mRemoteViewsServicesAppWidgets.keySet()) {
                    if (((HashSet) this.mRemoteViewsServicesAppWidgets.get(key)).contains(Integer.valueOf(appWidgetId))) {
                        bindService(((FilterComparison) key.second).getIntent(), new ServiceConnection() {
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
            }
        }
    }

    private void scheduleNotifyUpdateAppWidgetLocked(Widget widget, RemoteViews updateViews) {
        long requestTime = SystemClock.uptimeMillis();
        if (widget != null) {
            widget.lastUpdateTime = requestTime;
        }
        if (widget != null && widget.provider != null && !widget.provider.zombie && widget.host.callbacks != null && !widget.host.zombie && updateViews != null) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = widget.host;
            args.arg2 = widget.host.callbacks;
            args.arg3 = updateViews.clone();
            args.arg4 = Long.valueOf(requestTime);
            args.argi1 = widget.appWidgetId;
            this.mCallbackHandler.obtainMessage(CURRENT_VERSION, args).sendToTarget();
        }
    }

    private void handleNotifyUpdateAppWidget(Host host, IAppWidgetHost callbacks, int appWidgetId, RemoteViews views, long requestTime) {
        try {
            callbacks.updateAppWidget(appWidgetId, views);
            host.lastWidgetUpdateTime = requestTime;
        } catch (RemoteException re) {
            synchronized (this.mLock) {
            }
            Slog.e(TAG, "Widget host dead: " + host.id, re);
            host.callbacks = null;
        }
    }

    private void scheduleNotifyProviderChangedLocked(Widget widget) {
        if (widget != null && widget.provider != null && !widget.provider.zombie && widget.host.callbacks != null && !widget.host.zombie) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = widget.host;
            args.arg2 = widget.host.callbacks;
            args.arg3 = widget.provider.info;
            args.argi1 = widget.appWidgetId;
            this.mCallbackHandler.obtainMessage(2, args).sendToTarget();
        }
    }

    private void scheduleRecyleRemoteView(RemoteViews views) {
        if (views != null) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = views;
            this.mCallbackHandler.obtainMessage(20, args).sendToTarget();
        }
    }

    private void handleNotifyProviderChanged(Host host, IAppWidgetHost callbacks, int appWidgetId, AppWidgetProviderInfo info) {
        try {
            callbacks.providerChanged(appWidgetId, info);
        } catch (RemoteException re) {
            synchronized (this.mLock) {
            }
            Slog.e(TAG, "Widget host dead: " + host.id, re);
            host.callbacks = null;
        }
    }

    private void scheduleNotifyGroupHostsForProvidersChangedLocked(int userId) {
        int[] profileIds = this.mSecurityPolicy.getEnabledGroupProfileIds(userId);
        for (int i = this.mHosts.size() + UNKNOWN_UID; i >= 0; i += UNKNOWN_UID) {
            Host host = (Host) this.mHosts.get(i);
            boolean hostInGroup = HIDE_HUAWEI_WEATHER_WIDGET;
            int M = profileIds.length;
            for (int j = MIN_UPDATE_PERIOD; j < M; j += CURRENT_VERSION) {
                if (host.getUserId() == profileIds[j]) {
                    hostInGroup = true;
                    break;
                }
            }
            if (!(!hostInGroup || host == null || host.zombie || host.callbacks == null)) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = host;
                args.arg2 = host.callbacks;
                this.mCallbackHandler.obtainMessage(3, args).sendToTarget();
            }
        }
    }

    private void handleNotifyProvidersChanged(Host host, IAppWidgetHost callbacks) {
        try {
            callbacks.providersChanged();
        } catch (RemoteException re) {
            synchronized (this.mLock) {
            }
            Slog.e(TAG, "Widget host dead: " + host.id, re);
            host.callbacks = null;
        }
    }

    private static boolean isLocalBinder() {
        return Process.myPid() == Binder.getCallingPid() ? true : HIDE_HUAWEI_WEATHER_WIDGET;
    }

    private static RemoteViews cloneIfLocalBinder(RemoteViews rv) {
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
        for (int i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
            Widget widget = (Widget) this.mWidgets.get(i);
            if (widget.appWidgetId == appWidgetId && this.mSecurityPolicy.canAccessAppWidget(widget, uid, packageName)) {
                return widget;
            }
        }
        return null;
    }

    private Provider lookupProviderLocked(ProviderId id) {
        int N = this.mProviders.size();
        for (int i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
            Provider provider = (Provider) this.mProviders.get(i);
            if (provider.id.equals(id)) {
                return provider;
            }
        }
        return null;
    }

    private Host lookupHostLocked(HostId hostId) {
        int N = this.mHosts.size();
        for (int i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
            Host host = (Host) this.mHosts.get(i);
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
        int i;
        List allReceivers = null;
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
        int profileCount = profileIds.length;
        for (i = MIN_UPDATE_PERIOD; i < profileCount; i += CURRENT_VERSION) {
            List<ResolveInfo> receivers = queryIntentReceivers(intent, profileIds[i]);
            if (!(receivers == null || receivers.isEmpty())) {
                if (allReceivers == null) {
                    allReceivers = new ArrayList();
                }
                allReceivers.addAll(receivers);
            }
        }
        int N = allReceivers == null ? MIN_UPDATE_PERIOD : allReceivers.size();
        for (i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
            addProviderLocked((ResolveInfo) allReceivers.get(i));
        }
    }

    private boolean addProviderLocked(ResolveInfo ri) {
        if ((ri.activityInfo.applicationInfo.flags & DumpState.DUMP_DOMAIN_PREFERRED) != 0 || !ri.activityInfo.isEnabled()) {
            return HIDE_HUAWEI_WEATHER_WIDGET;
        }
        ComponentName componentName = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
        ProviderId providerId = new ProviderId(componentName, null);
        Provider provider = parseProviderInfoXml(providerId, ri);
        if (provider == null) {
            return HIDE_HUAWEI_WEATHER_WIDGET;
        }
        Provider existing = lookupProviderLocked(providerId);
        if (existing == null) {
            existing = lookupProviderLocked(new ProviderId(componentName, null));
        }
        if (existing == null) {
            this.mProviders.add(provider);
        } else if (existing.zombie && !this.mSafeMode) {
            existing.id = providerId;
            existing.zombie = HIDE_HUAWEI_WEATHER_WIDGET;
            existing.info = provider.info;
            if (DEBUG) {
                Slog.i(TAG, "Provider placeholder now reified: " + existing);
            }
        }
        return true;
    }

    private void deleteWidgetsLocked(Provider provider, int userId) {
        for (int i = provider.widgets.size() + UNKNOWN_UID; i >= 0; i += UNKNOWN_UID) {
            Widget widget = (Widget) provider.widgets.get(i);
            if (userId == UNKNOWN_UID || userId == widget.host.getUserId()) {
                provider.widgets.remove(i);
                updateAppWidgetInstanceLocked(widget, null, HIDE_HUAWEI_WEATHER_WIDGET);
                widget.host.widgets.remove(widget);
                removeWidgetLocked(widget);
                widget.provider = null;
                pruneHostLocked(widget.host);
                widget.host = null;
            }
        }
    }

    private void deleteProviderLocked(Provider provider) {
        deleteWidgetsLocked(provider, UNKNOWN_UID);
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
        if (provider.info.updatePeriodMillis > 0) {
            boolean alreadyRegistered = provider.broadcast != null ? true : HIDE_HUAWEI_WEATHER_WIDGET;
            Intent intent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
            intent.putExtra("appWidgetIds", appWidgetIds);
            intent.setComponent(provider.info.provider);
            long token = Binder.clearCallingIdentity();
            try {
                provider.broadcast = PendingIntent.getBroadcastAsUser(this.mContext, CURRENT_VERSION, intent, 134217728, provider.info.getProfile());
                if (!alreadyRegistered) {
                    long period = (long) provider.info.updatePeriodMillis;
                    if (period < ((long) MIN_UPDATE_PERIOD)) {
                        period = (long) MIN_UPDATE_PERIOD;
                    }
                    long oldId = Binder.clearCallingIdentity();
                    try {
                        this.mAlarmManager.setInexactRepeating(2, SystemClock.elapsedRealtime() + period, period, provider.broadcast);
                    } finally {
                        Binder.restoreCallingIdentity(oldId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    private static int[] getWidgetIds(ArrayList<Widget> widgets) {
        int instancesSize = widgets.size();
        int[] appWidgetIds = new int[instancesSize];
        for (int i = MIN_UPDATE_PERIOD; i < instancesSize; i += CURRENT_VERSION) {
            appWidgetIds[i] = ((Widget) widgets.get(i)).appWidgetId;
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

    private static void serializeProvider(XmlSerializer out, Provider p) throws IOException {
        out.startTag(null, "p");
        out.attribute(null, AbsLocationManagerService.DEL_PKG, p.info.provider.getPackageName());
        out.attribute(null, "cl", p.info.provider.getClassName());
        out.attribute(null, "tag", Integer.toHexString(p.tag));
        out.endTag(null, "p");
    }

    private static void serializeHost(XmlSerializer out, Host host) throws IOException {
        out.startTag(null, "h");
        out.attribute(null, AbsLocationManagerService.DEL_PKG, host.id.packageName);
        out.attribute(null, "id", Integer.toHexString(host.id.hostId));
        out.attribute(null, "tag", Integer.toHexString(host.tag));
        out.endTag(null, "h");
    }

    private static void serializeAppWidget(XmlSerializer out, Widget widget) throws IOException {
        out.startTag(null, "g");
        out.attribute(null, "id", Integer.toHexString(widget.appWidgetId));
        out.attribute(null, "rid", Integer.toHexString(widget.restoredId));
        out.attribute(null, "h", Integer.toHexString(widget.host.tag));
        if (widget.provider != null) {
            out.attribute(null, "p", Integer.toHexString(widget.provider.tag));
        }
        if (widget.options != null) {
            out.attribute(null, "min_width", Integer.toHexString(widget.options.getInt("appWidgetMinWidth")));
            out.attribute(null, "min_height", Integer.toHexString(widget.options.getInt("appWidgetMinHeight")));
            out.attribute(null, "max_width", Integer.toHexString(widget.options.getInt("appWidgetMaxWidth")));
            out.attribute(null, "max_height", Integer.toHexString(widget.options.getInt("appWidgetMaxHeight")));
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

    private Provider parseProviderInfoXml(ProviderId providerId, ResolveInfo ri) {
        Exception e;
        Throwable th;
        ActivityInfo activityInfo = ri.activityInfo;
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = activityInfo.loadXmlMetaData(this.mContext.getPackageManager(), "android.appwidget.provider");
            if (xmlResourceParser == null) {
                Slog.w(TAG, "No android.appwidget.provider meta-data for AppWidget provider '" + providerId + '\'');
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                return null;
            }
            AttributeSet attrs = Xml.asAttributeSet(xmlResourceParser);
            int type;
            do {
                type = xmlResourceParser.next();
                if (type == CURRENT_VERSION) {
                    break;
                }
            } while (type != 2);
            if ("appwidget-provider".equals(xmlResourceParser.getName())) {
                Provider provider = new Provider();
                long identity;
                try {
                    provider.id = providerId;
                    AppWidgetProviderInfo info = new AppWidgetProviderInfo();
                    provider.info = info;
                    info.provider = providerId.componentName;
                    info.providerInfo = activityInfo;
                    identity = Binder.clearCallingIdentity();
                    PackageManager pm = this.mContext.getPackageManager();
                    Resources resources = pm.getResourcesForApplication(pm.getApplicationInfoAsUser(activityInfo.packageName, MIN_UPDATE_PERIOD, UserHandle.getUserId(providerId.uid)));
                    Binder.restoreCallingIdentity(identity);
                    TypedArray sa = resources.obtainAttributes(attrs, R.styleable.AppWidgetProviderInfo);
                    TypedValue value = sa.peekValue(MIN_UPDATE_PERIOD);
                    info.minWidth = value != null ? value.data : MIN_UPDATE_PERIOD;
                    value = sa.peekValue(CURRENT_VERSION);
                    info.minHeight = value != null ? value.data : MIN_UPDATE_PERIOD;
                    value = sa.peekValue(8);
                    info.minResizeWidth = value != null ? value.data : info.minWidth;
                    value = sa.peekValue(9);
                    info.minResizeHeight = value != null ? value.data : info.minHeight;
                    info.updatePeriodMillis = sa.getInt(2, MIN_UPDATE_PERIOD);
                    info.initialLayout = sa.getResourceId(3, MIN_UPDATE_PERIOD);
                    info.initialKeyguardLayout = sa.getResourceId(10, MIN_UPDATE_PERIOD);
                    String className = sa.getString(4);
                    if (className != null) {
                        info.configure = new ComponentName(providerId.componentName.getPackageName(), className);
                    }
                    info.label = activityInfo.loadLabel(this.mContext.getPackageManager()).toString();
                    info.icon = ri.getIconResource();
                    info.previewImage = sa.getResourceId(5, MIN_UPDATE_PERIOD);
                    info.autoAdvanceViewId = sa.getResourceId(6, UNKNOWN_UID);
                    info.resizeMode = sa.getInt(7, MIN_UPDATE_PERIOD);
                    info.widgetCategory = sa.getInt(11, CURRENT_VERSION);
                    sa.recycle();
                    if (xmlResourceParser != null) {
                        xmlResourceParser.close();
                    }
                    return provider;
                } catch (IOException e2) {
                    e = e2;
                    Provider provider2 = provider;
                    try {
                        Slog.w(TAG, "XML parsing failed for AppWidget provider " + providerId.componentName + " for user " + providerId.uid, e);
                        if (xmlResourceParser != null) {
                            xmlResourceParser.close();
                        }
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        if (xmlResourceParser != null) {
                            xmlResourceParser.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (xmlResourceParser != null) {
                        xmlResourceParser.close();
                    }
                    throw th;
                }
            }
            Slog.w(TAG, "Meta-data does not start with appwidget-provider tag for AppWidget provider " + providerId.componentName + " for user " + providerId.uid);
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return null;
        } catch (IOException e3) {
            e = e3;
            Slog.w(TAG, "XML parsing failed for AppWidget provider " + providerId.componentName + " for user " + providerId.uid, e);
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return null;
        }
    }

    private int getUidForPackage(String packageName, int userId) {
        PackageInfo pkgInfo = null;
        long identity = Binder.clearCallingIdentity();
        try {
            pkgInfo = this.mPackageManager.getPackageInfo(packageName, MIN_UPDATE_PERIOD, userId);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
        if (pkgInfo == null || pkgInfo.applicationInfo == null) {
            return UNKNOWN_UID;
        }
        return pkgInfo.applicationInfo.uid;
    }

    private ActivityInfo getProviderInfo(ComponentName componentName, int userId) {
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
        intent.setComponent(componentName);
        List<ResolveInfo> receivers = queryIntentReceivers(intent, userId);
        if (receivers.isEmpty()) {
            return null;
        }
        return ((ResolveInfo) receivers.get(MIN_UPDATE_PERIOD)).activityInfo;
    }

    private List<ResolveInfo> queryIntentReceivers(Intent intent, int userId) {
        List<ResolveInfo> list;
        long identity = Binder.clearCallingIdentity();
        int flags = 268435584;
        try {
            if (isProfileWithUnlockedParent(userId)) {
                flags = 268435584 | 786432;
            }
            list = this.mPackageManager.queryIntentReceivers(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags | DumpState.DUMP_PROVIDERS, userId).getList();
            return list;
        } catch (RemoteException e) {
            list = Collections.emptyList();
            return list;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    void onUserUnlocked(int userId) {
        if (!isProfileWithLockedParent(userId)) {
            if (this.mUserManager.isUserUnlockingOrUnlocked(userId)) {
                synchronized (this.mLock) {
                    ensureGroupStateLoadedLocked(userId);
                    reloadWidgetsMaskedStateForGroup(this.mSecurityPolicy.getGroupParent(userId));
                    int N = this.mProviders.size();
                    for (int i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
                        Provider provider = (Provider) this.mProviders.get(i);
                        if (provider.getUserId() == userId && provider.widgets.size() > 0) {
                            sendEnableIntentLocked(provider);
                            int[] appWidgetIds = getWidgetIds(provider.widgets);
                            sendUpdateIntentLocked(provider, appWidgetIds);
                            registerForBroadcastsLocked(provider, appWidgetIds);
                        }
                    }
                }
                return;
            }
            Slog.w(TAG, "User " + userId + " is no longer unlocked - exiting");
        }
    }

    private void loadGroupStateLocked(int[] profileIds) {
        int i;
        List<LoadedWidgetState> loadedWidgets = new ArrayList();
        int version = MIN_UPDATE_PERIOD;
        int profileIdCount = profileIds.length;
        for (i = MIN_UPDATE_PERIOD; i < profileIdCount; i += CURRENT_VERSION) {
            int profileId = profileIds[i];
            try {
                FileInputStream stream = getSavedStateFile(profileId).openRead();
                version = readProfileStateFromFileLocked(stream, profileId, loadedWidgets);
                IoUtils.closeQuietly(stream);
            } catch (FileNotFoundException e) {
                Slog.w(TAG, "Failed to read state: " + e);
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
        for (i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
            ((Provider) this.mProviders.get(i)).widgets.clear();
        }
    }

    private void bindLoadedWidgetsLocked(List<LoadedWidgetState> loadedWidgets) {
        for (int i = loadedWidgets.size() + UNKNOWN_UID; i >= 0; i += UNKNOWN_UID) {
            LoadedWidgetState loadedWidget = (LoadedWidgetState) loadedWidgets.remove(i);
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
        for (int i = MIN_UPDATE_PERIOD; i < providerCount; i += CURRENT_VERSION) {
            Provider provider = (Provider) this.mProviders.get(i);
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
        for (int i = MIN_UPDATE_PERIOD; i < hostCount; i += CURRENT_VERSION) {
            Host host = (Host) this.mHosts.get(i);
            if (host.tag == tag) {
                return host;
            }
        }
        return null;
    }

    void addWidgetLocked(Widget widget) {
        this.mWidgets.add(widget);
        onWidgetProviderAddedOrChangedLocked(widget);
    }

    void onWidgetProviderAddedOrChangedLocked(Widget widget) {
        if (widget.provider != null) {
            int userId = widget.provider.getUserId();
            ArraySet<String> packages = (ArraySet) this.mWidgetPackages.get(userId);
            if (packages == null) {
                SparseArray sparseArray = this.mWidgetPackages;
                packages = new ArraySet();
                sparseArray.put(userId, packages);
            }
            packages.add(widget.provider.info.provider.getPackageName());
            if (widget.provider.isMaskedLocked()) {
                maskWidgetsViewsLocked(widget.provider, widget);
            } else {
                widget.clearMaskedViewsLocked();
            }
            addWidgetReport(userId, widget.provider.info.provider.getPackageName());
        }
    }

    void removeWidgetLocked(Widget widget) {
        this.mWidgets.remove(widget);
        onWidgetRemovedLocked(widget);
    }

    private void onWidgetRemovedLocked(Widget widget) {
        if (widget.provider != null) {
            int userId = widget.provider.getUserId();
            String packageName = widget.provider.info.provider.getPackageName();
            ArraySet<String> packages = (ArraySet) this.mWidgetPackages.get(userId);
            if (packages != null) {
                int N = this.mWidgets.size();
                int i = MIN_UPDATE_PERIOD;
                while (i < N) {
                    Widget w = (Widget) this.mWidgets.get(i);
                    if (w.provider == null || w.provider.getUserId() != userId || !packageName.equals(w.provider.info.provider.getPackageName())) {
                        i += CURRENT_VERSION;
                    } else {
                        return;
                    }
                }
                packages.remove(packageName);
                removeWidgetReport(userId, packageName);
            }
        }
    }

    protected void addWidgetReport(int userId, String pkgName) {
    }

    protected void removeWidgetReport(int userId, String pkgName) {
    }

    protected void clearWidgetReport() {
    }

    void clearWidgetsLocked() {
        this.mWidgets.clear();
        onWidgetsClearedLocked();
    }

    private void onWidgetsClearedLocked() {
        this.mWidgetPackages.clear();
        clearWidgetReport();
    }

    public boolean isBoundWidgetPackage(String packageName, int userId) {
        if (Binder.getCallingUid() != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            throw new SecurityException("Only the system process can call this");
        }
        synchronized (this.mLock) {
            ArraySet<String> packages = (ArraySet) this.mWidgetPackages.get(userId);
            if (packages != null) {
                boolean contains = packages.contains(packageName);
                return contains;
            }
            return HIDE_HUAWEI_WEATHER_WIDGET;
        }
    }

    private void saveStateLocked(int userId) {
        tagProvidersAndHosts();
        int[] profileIds = this.mSecurityPolicy.getEnabledGroupProfileIds(userId);
        int profileCount = profileIds.length;
        for (int i = MIN_UPDATE_PERIOD; i < profileCount; i += CURRENT_VERSION) {
            int profileId = profileIds[i];
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
        int i;
        int providerCount = this.mProviders.size();
        for (i = MIN_UPDATE_PERIOD; i < providerCount; i += CURRENT_VERSION) {
            ((Provider) this.mProviders.get(i)).tag = i;
        }
        int hostCount = this.mHosts.size();
        for (i = MIN_UPDATE_PERIOD; i < hostCount; i += CURRENT_VERSION) {
            ((Host) this.mHosts.get(i)).tag = i;
        }
    }

    private void clearProvidersAndHostsTagsLocked() {
        int i;
        int providerCount = this.mProviders.size();
        for (i = MIN_UPDATE_PERIOD; i < providerCount; i += CURRENT_VERSION) {
            ((Provider) this.mProviders.get(i)).tag = UNKNOWN_UID;
        }
        int hostCount = this.mHosts.size();
        for (i = MIN_UPDATE_PERIOD; i < hostCount; i += CURRENT_VERSION) {
            ((Host) this.mHosts.get(i)).tag = UNKNOWN_UID;
        }
    }

    private boolean writeProfileStateToFileLocked(FileOutputStream stream, int userId) {
        try {
            int i;
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(stream, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.startTag(null, "gs");
            out.attribute(null, "version", String.valueOf(CURRENT_VERSION));
            int N = this.mProviders.size();
            for (i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
                Provider provider = (Provider) this.mProviders.get(i);
                if (provider.getUserId() == userId && provider.widgets.size() > 0) {
                    serializeProvider(out, provider);
                }
            }
            N = this.mHosts.size();
            for (i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
                Host host = (Host) this.mHosts.get(i);
                if (host.getUserId() == userId) {
                    serializeHost(out, host);
                }
            }
            N = this.mWidgets.size();
            for (i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
                Widget widget = (Widget) this.mWidgets.get(i);
                if (widget.host.getUserId() == userId) {
                    serializeAppWidget(out, widget);
                }
            }
            Iterator<Pair<Integer, String>> it = this.mPackagesWithBindWidgetPermission.iterator();
            while (it.hasNext()) {
                Pair<Integer, String> binding = (Pair) it.next();
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
            return HIDE_HUAWEI_WEATHER_WIDGET;
        }
    }

    private int readProfileStateFromFileLocked(FileInputStream stream, int userId, List<LoadedWidgetState> outLoadedWidgets) {
        int version = UNKNOWN_UID;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, StandardCharsets.UTF_8.name());
        int legacyProviderIndex = UNKNOWN_UID;
        int legacyHostIndex = UNKNOWN_UID;
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                String tag = parser.getName();
                if ("gs".equals(tag)) {
                    try {
                        version = Integer.parseInt(parser.getAttributeValue(null, "version"));
                    } catch (NumberFormatException e) {
                        version = MIN_UPDATE_PERIOD;
                    }
                } else {
                    try {
                        String pkg;
                        int uid;
                        String tagAttribute;
                        int providerTag;
                        if ("p".equals(tag)) {
                            legacyProviderIndex += CURRENT_VERSION;
                            pkg = parser.getAttributeValue(null, AbsLocationManagerService.DEL_PKG);
                            String cl = parser.getAttributeValue(null, "cl");
                            pkg = getCanonicalPackageName(pkg, cl, userId);
                            if (pkg != null) {
                                uid = getUidForPackage(pkg, userId);
                                if (uid >= 0) {
                                    ComponentName componentName = new ComponentName(pkg, cl);
                                    ActivityInfo providerInfo = getProviderInfo(componentName, userId);
                                    if (providerInfo != null) {
                                        ProviderId providerId = new ProviderId(componentName, null);
                                        Provider provider = lookupProviderLocked(providerId);
                                        if (provider == null && this.mSafeMode) {
                                            provider = new Provider();
                                            provider.info = new AppWidgetProviderInfo();
                                            AppWidgetProviderInfo appWidgetProviderInfo = provider.info;
                                            appWidgetProviderInfo.provider = providerId.componentName;
                                            provider.info.providerInfo = providerInfo;
                                            provider.zombie = true;
                                            provider.id = providerId;
                                            this.mProviders.add(provider);
                                        }
                                        tagAttribute = parser.getAttributeValue(null, "tag");
                                        if (TextUtils.isEmpty(tagAttribute)) {
                                            providerTag = legacyProviderIndex;
                                        } else {
                                            providerTag = Integer.parseInt(tagAttribute, 16);
                                        }
                                        provider.tag = providerTag;
                                    }
                                }
                            }
                        } else {
                            int hostTag;
                            if ("h".equals(tag)) {
                                legacyHostIndex += CURRENT_VERSION;
                                Host host = new Host();
                                pkg = parser.getAttributeValue(null, AbsLocationManagerService.DEL_PKG);
                                uid = getUidForPackage(pkg, userId);
                                if (uid < 0) {
                                    host.zombie = true;
                                }
                                if (!host.zombie || this.mSafeMode) {
                                    int hostId = Integer.parseInt(parser.getAttributeValue(null, "id"), 16);
                                    tagAttribute = parser.getAttributeValue(null, "tag");
                                    if (TextUtils.isEmpty(tagAttribute)) {
                                        hostTag = legacyHostIndex;
                                    } else {
                                        hostTag = Integer.parseInt(tagAttribute, 16);
                                    }
                                    host.tag = hostTag;
                                    host.id = new HostId(uid, hostId, pkg);
                                    this.mHosts.add(host);
                                }
                            } else {
                                if ("b".equals(tag)) {
                                    String packageName = parser.getAttributeValue(null, "packageName");
                                    if (getUidForPackage(packageName, userId) >= 0) {
                                        Pair<Integer, String> packageId = Pair.create(Integer.valueOf(userId), packageName);
                                        this.mPackagesWithBindWidgetPermission.add(packageId);
                                    }
                                } else {
                                    if ("g".equals(tag)) {
                                        int i;
                                        String str;
                                        Widget widget = new Widget();
                                        widget.appWidgetId = Integer.parseInt(parser.getAttributeValue(null, "id"), 16);
                                        setMinAppWidgetIdLocked(userId, widget.appWidgetId + CURRENT_VERSION);
                                        String restoredIdString = parser.getAttributeValue(null, "rid");
                                        if (restoredIdString == null) {
                                            i = MIN_UPDATE_PERIOD;
                                        } else {
                                            i = Integer.parseInt(restoredIdString, 16);
                                        }
                                        widget.restoredId = i;
                                        Bundle options = new Bundle();
                                        String minWidthString = parser.getAttributeValue(null, "min_width");
                                        if (minWidthString != null) {
                                            str = "appWidgetMinWidth";
                                            options.putInt(r36, Integer.parseInt(minWidthString, 16));
                                        }
                                        String minHeightString = parser.getAttributeValue(null, "min_height");
                                        if (minHeightString != null) {
                                            str = "appWidgetMinHeight";
                                            options.putInt(r36, Integer.parseInt(minHeightString, 16));
                                        }
                                        String maxWidthString = parser.getAttributeValue(null, "max_width");
                                        if (maxWidthString != null) {
                                            str = "appWidgetMaxWidth";
                                            options.putInt(r36, Integer.parseInt(maxWidthString, 16));
                                        }
                                        String maxHeightString = parser.getAttributeValue(null, "max_height");
                                        if (maxHeightString != null) {
                                            str = "appWidgetMaxHeight";
                                            options.putInt(r36, Integer.parseInt(maxHeightString, 16));
                                        }
                                        String categoryString = parser.getAttributeValue(null, "host_category");
                                        if (categoryString != null) {
                                            str = "appWidgetCategory";
                                            options.putInt(r36, Integer.parseInt(categoryString, 16));
                                        }
                                        widget.options = options;
                                        hostTag = Integer.parseInt(parser.getAttributeValue(null, "h"), 16);
                                        if (parser.getAttributeValue(null, "p") != null) {
                                            providerTag = Integer.parseInt(parser.getAttributeValue(null, "p"), 16);
                                        } else {
                                            providerTag = UNKNOWN_UID;
                                        }
                                        outLoadedWidgets.add(new LoadedWidgetState(widget, hostTag, providerTag));
                                    }
                                }
                            }
                        }
                    } catch (Exception e2) {
                        Slog.w(TAG, "failed parsing " + e2);
                        return UNKNOWN_UID;
                    }
                }
            }
        } while (type != CURRENT_VERSION);
        return version;
    }

    private void performUpgradeLocked(int fromVersion) {
        if (fromVersion < CURRENT_VERSION) {
            Slog.v(TAG, "Upgrading widget database from " + fromVersion + " to " + CURRENT_VERSION);
        }
        int version = fromVersion;
        if (fromVersion == 0) {
            Host host = lookupHostLocked(new HostId(Process.myUid(), KEYGUARD_HOST_ID, OLD_KEYGUARD_HOST_PACKAGE));
            if (host != null) {
                int uid = getUidForPackage(NEW_KEYGUARD_HOST_PACKAGE, MIN_UPDATE_PERIOD);
                if (uid >= 0) {
                    host.id = new HostId(uid, KEYGUARD_HOST_ID, NEW_KEYGUARD_HOST_PACKAGE);
                }
            }
            version = CURRENT_VERSION;
        }
        if (version != CURRENT_VERSION) {
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

    void onUserStopped(int userId) {
        synchronized (this.mLock) {
            int i;
            int crossProfileWidgetsChanged = MIN_UPDATE_PERIOD;
            for (i = this.mWidgets.size() + UNKNOWN_UID; i >= 0; i += UNKNOWN_UID) {
                Widget widget = (Widget) this.mWidgets.get(i);
                boolean hostInUser = widget.host.getUserId() == userId ? true : HIDE_HUAWEI_WEATHER_WIDGET;
                boolean hasProvider = widget.provider != null ? true : HIDE_HUAWEI_WEATHER_WIDGET;
                boolean providerInUser = (hasProvider && widget.provider.getUserId() == userId) ? true : HIDE_HUAWEI_WEATHER_WIDGET;
                if (hostInUser && (!hasProvider || providerInUser)) {
                    removeWidgetLocked(widget);
                    widget.host.widgets.remove(widget);
                    widget.host = null;
                    if (hasProvider) {
                        widget.provider.widgets.remove(widget);
                        widget.provider = null;
                    }
                }
            }
            for (i = this.mHosts.size() + UNKNOWN_UID; i >= 0; i += UNKNOWN_UID) {
                Host host = (Host) this.mHosts.get(i);
                if (host.getUserId() == userId) {
                    crossProfileWidgetsChanged |= host.widgets.isEmpty() ? MIN_UPDATE_PERIOD : CURRENT_VERSION;
                    deleteHostLocked(host);
                }
            }
            for (i = this.mPackagesWithBindWidgetPermission.size() + UNKNOWN_UID; i >= 0; i += UNKNOWN_UID) {
                if (((Integer) ((Pair) this.mPackagesWithBindWidgetPermission.valueAt(i)).first).intValue() == userId) {
                    this.mPackagesWithBindWidgetPermission.removeAt(i);
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
            if (crossProfileWidgetsChanged != 0) {
                saveGroupStateAsync(userId);
            }
        }
    }

    private boolean updateProvidersForPackageLocked(String packageName, int userId, Set<ProviderId> removedProviders) {
        int i;
        Provider provider;
        boolean providersUpdated = HIDE_HUAWEI_WEATHER_WIDGET;
        HashSet<ProviderId> keep = new HashSet();
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
        intent.setPackage(packageName);
        List<ResolveInfo> broadcastReceivers = queryIntentReceivers(intent, userId);
        int N = broadcastReceivers == null ? MIN_UPDATE_PERIOD : broadcastReceivers.size();
        for (i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
            ResolveInfo ri = (ResolveInfo) broadcastReceivers.get(i);
            ActivityInfo ai = ri.activityInfo;
            if ((ai.applicationInfo.flags & DumpState.DUMP_DOMAIN_PREFERRED) == 0) {
                if (packageName.equals(ai.packageName)) {
                    ProviderId providerId = new ProviderId(new ComponentName(ai.packageName, ai.name), null);
                    provider = lookupProviderLocked(providerId);
                    if (provider != null) {
                        Provider parsed = parseProviderInfoXml(providerId, ri);
                        if (parsed != null) {
                            keep.add(providerId);
                            provider.info = parsed.info;
                            int M = provider.widgets.size();
                            if (M > 0) {
                                int[] appWidgetIds = getWidgetIds(provider.widgets);
                                cancelBroadcasts(provider);
                                registerForBroadcastsLocked(provider, appWidgetIds);
                                for (int j = MIN_UPDATE_PERIOD; j < M; j += CURRENT_VERSION) {
                                    Widget widget = (Widget) provider.widgets.get(j);
                                    widget.views = null;
                                    scheduleNotifyProviderChangedLocked(widget);
                                }
                                sendUpdateIntentLocked(provider, appWidgetIds);
                            }
                        }
                        providersUpdated = true;
                    } else if (addProviderLocked(ri)) {
                        keep.add(providerId);
                        providersUpdated = true;
                    }
                }
            }
        }
        for (i = this.mProviders.size() + UNKNOWN_UID; i >= 0; i += UNKNOWN_UID) {
            provider = (Provider) this.mProviders.get(i);
            if (packageName.equals(provider.info.provider.getPackageName()) && provider.getUserId() == userId) {
                if (!keep.contains(provider.id)) {
                    if (removedProviders != null) {
                        removedProviders.add(provider.id);
                    }
                    deleteProviderLocked(provider);
                    providersUpdated = true;
                }
            }
        }
        return providersUpdated;
    }

    private void removeWidgetsForPackageLocked(String pkgName, int userId, int parentUserId) {
        int N = this.mProviders.size();
        for (int i = MIN_UPDATE_PERIOD; i < N; i += CURRENT_VERSION) {
            Provider provider = (Provider) this.mProviders.get(i);
            if (pkgName.equals(provider.info.provider.getPackageName()) && provider.getUserId() == userId && provider.widgets.size() > 0) {
                deleteWidgetsLocked(provider, parentUserId);
            }
        }
    }

    private boolean removeProvidersForPackageLocked(String pkgName, int userId) {
        boolean removed = HIDE_HUAWEI_WEATHER_WIDGET;
        for (int i = this.mProviders.size() + UNKNOWN_UID; i >= 0; i += UNKNOWN_UID) {
            Provider provider = (Provider) this.mProviders.get(i);
            if (pkgName.equals(provider.info.provider.getPackageName()) && provider.getUserId() == userId) {
                deleteProviderLocked(provider);
                removed = true;
            }
        }
        return removed;
    }

    private boolean removeHostsAndProvidersForPackageLocked(String pkgName, int userId) {
        boolean removed = removeProvidersForPackageLocked(pkgName, userId);
        for (int i = this.mHosts.size() + UNKNOWN_UID; i >= 0; i += UNKNOWN_UID) {
            Host host = (Host) this.mHosts.get(i);
            if (pkgName.equals(host.id.packageName) && host.getUserId() == userId) {
                deleteHostLocked(host);
                removed = true;
            }
        }
        return removed;
    }

    private String getCanonicalPackageName(String packageName, String className, int userId) {
        long identity = Binder.clearCallingIdentity();
        String packageManager;
        try {
            packageManager = AppGlobals.getPackageManager();
            packageManager.getReceiverInfo(new ComponentName(packageName, className), MIN_UPDATE_PERIOD, userId);
            return packageName;
        } catch (RemoteException e) {
            PackageManager packageManager2 = this.mContext.getPackageManager();
            String[] strArr = new String[CURRENT_VERSION];
            strArr[MIN_UPDATE_PERIOD] = packageName;
            String[] packageNames = packageManager2.currentToCanonicalPackageNames(strArr);
            if (packageNames == null || packageNames.length <= 0) {
                Binder.restoreCallingIdentity(identity);
                return null;
            }
            packageManager = packageNames[MIN_UPDATE_PERIOD];
            return packageManager;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void sendBroadcastAsUser(Intent intent, UserHandle userHandle) {
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
                int i;
                int providersChanged = MIN_UPDATE_PERIOD;
                ArraySet<String> previousPackages = new ArraySet();
                int providerCount = this.mProviders.size();
                for (i = MIN_UPDATE_PERIOD; i < providerCount; i += CURRENT_VERSION) {
                    Provider provider = (Provider) this.mProviders.get(i);
                    if (provider.getUserId() == userId) {
                        previousPackages.add(provider.id.componentName.getPackageName());
                    }
                }
                int packageCount = packages.size();
                for (i = MIN_UPDATE_PERIOD; i < packageCount; i += CURRENT_VERSION) {
                    String packageName = (String) packages.get(i);
                    previousPackages.remove(packageName);
                    providersChanged |= updateProvidersForPackageLocked(packageName, userId, null);
                }
                int removedCount = previousPackages.size();
                for (i = MIN_UPDATE_PERIOD; i < removedCount; i += CURRENT_VERSION) {
                    removeWidgetsForPackageLocked((String) previousPackages.valueAt(i), userId, parentId);
                }
                if (providersChanged != 0 || removedCount > 0) {
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
                if (!(parentInfo == null || isUserRunningAndUnlocked(parentInfo.getUserHandle().getIdentifier()))) {
                    Binder.restoreCallingIdentity(token);
                    return true;
                }
            }
            Binder.restoreCallingIdentity(token);
            return HIDE_HUAWEI_WEATHER_WIDGET;
        } catch (Throwable th) {
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
        return HIDE_HUAWEI_WEATHER_WIDGET;
    }
}
