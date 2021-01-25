package com.android.systemui.shared.plugins;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.PluginFragment;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.shared.plugins.PluginEnabler;
import com.android.systemui.shared.plugins.VersionInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PluginInstanceManager<T extends Plugin> {
    private static final boolean DEBUG = false;
    public static final String PLUGIN_PERMISSION = "com.android.systemui.permission.PLUGIN";
    private static final String TAG = "PluginInstanceManager";
    private final boolean isDebuggable;
    private final String mAction;
    private final boolean mAllowMultiple;
    private final Context mContext;
    private final PluginListener<T> mListener;
    @VisibleForTesting
    final PluginInstanceManager<T>.MainHandler mMainHandler;
    private final PluginManagerImpl mManager;
    @VisibleForTesting
    final PluginInstanceManager<T>.PluginHandler mPluginHandler;
    private final PackageManager mPm;
    private final VersionInfo mVersion;
    private final ArraySet<String> mWhitelistedPlugins;

    PluginInstanceManager(Context context, String action, PluginListener<T> listener, boolean allowMultiple, Looper looper, VersionInfo version, PluginManagerImpl manager) {
        this(context, context.getPackageManager(), action, listener, allowMultiple, looper, version, manager, Build.IS_DEBUGGABLE, manager.getWhitelistedPlugins());
    }

    @VisibleForTesting
    PluginInstanceManager(Context context, PackageManager pm, String action, PluginListener<T> listener, boolean allowMultiple, Looper looper, VersionInfo version, PluginManagerImpl manager, boolean debuggable, String[] pluginWhitelist) {
        this.mWhitelistedPlugins = new ArraySet<>();
        this.mMainHandler = new MainHandler(Looper.getMainLooper());
        this.mPluginHandler = new PluginHandler(looper);
        this.mManager = manager;
        this.mContext = context;
        this.mPm = pm;
        this.mAction = action;
        this.mListener = listener;
        this.mAllowMultiple = allowMultiple;
        this.mVersion = version;
        this.mWhitelistedPlugins.addAll(Arrays.asList(pluginWhitelist));
        this.isDebuggable = debuggable;
    }

    public PluginInfo<T> getPlugin() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            this.mPluginHandler.handleQueryPlugins(null);
            if (((PluginHandler) this.mPluginHandler).mPlugins.size() <= 0) {
                return null;
            }
            this.mMainHandler.removeMessages(1);
            PluginInfo<T> info = (PluginInfo) ((PluginHandler) this.mPluginHandler).mPlugins.get(0);
            PluginPrefs.setHasPlugins(this.mContext);
            info.mPlugin.onCreate(this.mContext, ((PluginInfo) info).mPluginContext);
            return info;
        }
        throw new RuntimeException("Must be called from UI thread");
    }

    public void loadAll() {
        this.mPluginHandler.sendEmptyMessage(1);
    }

    public String getPluginAction() {
        return this.mAction;
    }

    public void destroy() {
        Iterator<PluginInfo> it = new ArrayList<>(((PluginHandler) this.mPluginHandler).mPlugins).iterator();
        while (it.hasNext()) {
            this.mMainHandler.obtainMessage(2, it.next().mPlugin).sendToTarget();
        }
    }

    public void onPackageRemoved(String pkg) {
        this.mPluginHandler.obtainMessage(3, pkg).sendToTarget();
    }

    public void onPackageChange(String pkg) {
        this.mPluginHandler.obtainMessage(3, pkg).sendToTarget();
        this.mPluginHandler.obtainMessage(2, pkg).sendToTarget();
    }

    public boolean checkAndDisable(String className) {
        boolean disableAny = false;
        Iterator<PluginInfo> it = new ArrayList<>(((PluginHandler) this.mPluginHandler).mPlugins).iterator();
        while (it.hasNext()) {
            PluginInfo info = it.next();
            if (className.startsWith(info.mPackage)) {
                disable(info, 2);
                disableAny = true;
            }
        }
        return disableAny;
    }

    public boolean disableAll() {
        ArrayList<PluginInfo> plugins = new ArrayList<>(((PluginHandler) this.mPluginHandler).mPlugins);
        for (int i = 0; i < plugins.size(); i++) {
            disable(plugins.get(i), 3);
        }
        return plugins.size() != 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPluginWhitelisted(ComponentName pluginName) {
        Iterator<String> it = this.mWhitelistedPlugins.iterator();
        while (it.hasNext()) {
            String componentNameOrPackage = it.next();
            ComponentName componentName = ComponentName.unflattenFromString(componentNameOrPackage);
            if (componentName == null) {
                if (componentNameOrPackage.equals(pluginName.getPackageName())) {
                    return true;
                }
            } else if (componentName.equals(pluginName)) {
                return true;
            }
        }
        return false;
    }

    private void disable(PluginInfo info, @PluginEnabler.DisableReason int reason) {
        ComponentName pluginComponent = new ComponentName(info.mPackage, info.mClass);
        if (!isPluginWhitelisted(pluginComponent)) {
            Log.w(TAG, "Disabling plugin " + pluginComponent.flattenToShortString());
            this.mManager.getPluginEnabler().setDisabled(pluginComponent, reason);
        }
    }

    public <T> boolean dependsOn(Plugin p, Class<T> cls) {
        Iterator<PluginInfo> it = new ArrayList<>(((PluginHandler) this.mPluginHandler).mPlugins).iterator();
        while (it.hasNext()) {
            PluginInfo info = it.next();
            if (info.mPlugin.getClass().getName().equals(p.getClass().getName())) {
                if (info.mVersion == null || !info.mVersion.hasClass(cls)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return String.format("%s@%s (action=%s)", getClass().getSimpleName(), Integer.valueOf(hashCode()), this.mAction);
    }

    /* access modifiers changed from: private */
    public class MainHandler extends Handler {
        private static final int PLUGIN_CONNECTED = 1;
        private static final int PLUGIN_DISCONNECTED = 2;

        public MainHandler(Looper looper) {
            super(looper);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r1v6, resolved type: com.android.systemui.plugins.PluginListener */
        /* JADX DEBUG: Multi-variable search result rejected for r0v6, resolved type: com.android.systemui.plugins.PluginListener */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                PluginPrefs.setHasPlugins(PluginInstanceManager.this.mContext);
                PluginInfo<T> info = (PluginInfo) msg.obj;
                PluginInstanceManager.this.mManager.handleWtfs();
                if (!(msg.obj instanceof PluginFragment)) {
                    info.mPlugin.onCreate(PluginInstanceManager.this.mContext, ((PluginInfo) info).mPluginContext);
                }
                PluginInstanceManager.this.mListener.onPluginConnected(info.mPlugin, ((PluginInfo) info).mPluginContext);
            } else if (i != 2) {
                super.handleMessage(msg);
            } else {
                PluginInstanceManager.this.mListener.onPluginDisconnected((Plugin) msg.obj);
                if (!(msg.obj instanceof PluginFragment)) {
                    ((Plugin) msg.obj).onDestroy();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class PluginHandler extends Handler {
        private static final int QUERY_ALL = 1;
        private static final int QUERY_PKG = 2;
        private static final int REMOVE_PKG = 3;
        private final ArrayList<PluginInfo<T>> mPlugins = new ArrayList<>();

        public PluginHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                for (int i2 = this.mPlugins.size() - 1; i2 >= 0; i2--) {
                    PluginInstanceManager.this.mMainHandler.obtainMessage(2, this.mPlugins.get(i2).mPlugin).sendToTarget();
                }
                this.mPlugins.clear();
                handleQueryPlugins(null);
            } else if (i == 2) {
                String p = (String) msg.obj;
                if (PluginInstanceManager.this.mAllowMultiple || this.mPlugins.size() == 0) {
                    handleQueryPlugins(p);
                }
            } else if (i != 3) {
                super.handleMessage(msg);
            } else {
                String pkg = (String) msg.obj;
                for (int i3 = this.mPlugins.size() - 1; i3 >= 0; i3--) {
                    PluginInfo<T> pluginInfo = this.mPlugins.get(i3);
                    if (pluginInfo.mPackage.equals(pkg)) {
                        PluginInstanceManager.this.mMainHandler.obtainMessage(2, pluginInfo.mPlugin).sendToTarget();
                        this.mPlugins.remove(i3);
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleQueryPlugins(String pkgName) {
            Intent intent = new Intent(PluginInstanceManager.this.mAction);
            if (pkgName != null) {
                intent.setPackage(pkgName);
            }
            List<ResolveInfo> result = PluginInstanceManager.this.mPm.queryIntentServices(intent, 0);
            if (result.size() <= 1 || PluginInstanceManager.this.mAllowMultiple) {
                for (ResolveInfo info : result) {
                    PluginInfo<T> t = handleLoadPlugin(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
                    if (t != null) {
                        this.mPlugins.add(t);
                        PluginInstanceManager.this.mMainHandler.obtainMessage(1, t).sendToTarget();
                    }
                }
                return;
            }
            Log.w(PluginInstanceManager.TAG, "Multiple plugins found for " + PluginInstanceManager.this.mAction);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r20v0, resolved type: com.android.systemui.shared.plugins.PluginInstanceManager$PluginHandler */
        /* JADX WARN: Multi-variable type inference failed */
        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:33:0x0149  */
        /* JADX WARNING: Removed duplicated region for block: B:34:0x017a  */
        public PluginInfo<T> handleLoadPlugin(ComponentName component) {
            Plugin plugin;
            VersionInfo.InvalidVersionException e;
            if (!PluginInstanceManager.this.isDebuggable && !PluginInstanceManager.this.isPluginWhitelisted(component)) {
                Log.w(PluginInstanceManager.TAG, "Plugin cannot be loaded on production build: " + component);
                return null;
            } else if (!PluginInstanceManager.this.mManager.getPluginEnabler().isEnabled(component)) {
                return null;
            } else {
                String pkg = component.getPackageName();
                String cls = component.getClassName();
                try {
                    ApplicationInfo info = PluginInstanceManager.this.mPm.getApplicationInfo(pkg, 0);
                    if (PluginInstanceManager.this.mPm.checkPermission(PluginInstanceManager.PLUGIN_PERMISSION, pkg) != 0) {
                        Log.d(PluginInstanceManager.TAG, "Plugin doesn't have permission: " + pkg);
                        return null;
                    }
                    ClassLoader classLoader = PluginInstanceManager.this.mManager.getClassLoader(info);
                    Context pluginContext = new PluginContextWrapper(PluginInstanceManager.this.mContext.createApplicationContext(info, 0), classLoader);
                    Class<?> pluginClass = Class.forName(cls, true, classLoader);
                    Plugin plugin2 = (Plugin) pluginClass.newInstance();
                    try {
                        plugin = plugin2;
                        try {
                            return new PluginInfo<>(pkg, cls, plugin, pluginContext, checkVersion(pluginClass, plugin2, PluginInstanceManager.this.mVersion));
                        } catch (VersionInfo.InvalidVersionException e2) {
                            e = e2;
                            Notification.Builder nb = new Notification.Builder(PluginInstanceManager.this.mContext, PluginManager.NOTIFICATION_CHANNEL_ID).setStyle(new Notification.BigTextStyle()).setSmallIcon(PluginInstanceManager.this.mContext.getResources().getIdentifier("tuner", "drawable", PluginInstanceManager.this.mContext.getPackageName())).setWhen(0).setShowWhen(false).setVisibility(1).setColor(PluginInstanceManager.this.mContext.getColor(Resources.getSystem().getIdentifier("system_notification_accent_color", "color", "android")));
                            String label = cls;
                            try {
                                label = PluginInstanceManager.this.mPm.getServiceInfo(component, 0).loadLabel(PluginInstanceManager.this.mPm).toString();
                            } catch (PackageManager.NameNotFoundException e3) {
                            }
                            if (e.isTooNew()) {
                            }
                            Intent intent = new Intent("com.android.systemui.action.DISABLE_PLUGIN");
                            nb.addAction(new Notification.Action.Builder((Icon) null, "Disable plugin", PendingIntent.getBroadcast(PluginInstanceManager.this.mContext, 0, intent.setData(Uri.parse("package://" + component.flattenToString())), 0)).build());
                            ((NotificationManager) PluginInstanceManager.this.mContext.getSystemService(NotificationManager.class)).notifyAsUser(cls, 6, nb.build(), UserHandle.ALL);
                            Log.w(PluginInstanceManager.TAG, "Plugin has invalid interface version " + plugin.getVersion() + ", expected " + PluginInstanceManager.this.mVersion);
                            return null;
                        }
                    } catch (VersionInfo.InvalidVersionException e4) {
                        e = e4;
                        plugin = plugin2;
                        Notification.Builder nb2 = new Notification.Builder(PluginInstanceManager.this.mContext, PluginManager.NOTIFICATION_CHANNEL_ID).setStyle(new Notification.BigTextStyle()).setSmallIcon(PluginInstanceManager.this.mContext.getResources().getIdentifier("tuner", "drawable", PluginInstanceManager.this.mContext.getPackageName())).setWhen(0).setShowWhen(false).setVisibility(1).setColor(PluginInstanceManager.this.mContext.getColor(Resources.getSystem().getIdentifier("system_notification_accent_color", "color", "android")));
                        String label2 = cls;
                        label2 = PluginInstanceManager.this.mPm.getServiceInfo(component, 0).loadLabel(PluginInstanceManager.this.mPm).toString();
                        if (e.isTooNew()) {
                            Notification.Builder contentTitle = nb2.setContentTitle("Plugin \"" + label2 + "\" is too old");
                            StringBuilder sb = new StringBuilder();
                            sb.append("Contact plugin developer to get an updated version.\n");
                            sb.append(e.getMessage());
                            contentTitle.setContentText(sb.toString());
                        } else {
                            Notification.Builder contentTitle2 = nb2.setContentTitle("Plugin \"" + label2 + "\" is too new");
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("Check to see if an OTA is available.\n");
                            sb2.append(e.getMessage());
                            contentTitle2.setContentText(sb2.toString());
                        }
                        Intent intent2 = new Intent("com.android.systemui.action.DISABLE_PLUGIN");
                        nb2.addAction(new Notification.Action.Builder((Icon) null, "Disable plugin", PendingIntent.getBroadcast(PluginInstanceManager.this.mContext, 0, intent2.setData(Uri.parse("package://" + component.flattenToString())), 0)).build());
                        ((NotificationManager) PluginInstanceManager.this.mContext.getSystemService(NotificationManager.class)).notifyAsUser(cls, 6, nb2.build(), UserHandle.ALL);
                        Log.w(PluginInstanceManager.TAG, "Plugin has invalid interface version " + plugin.getVersion() + ", expected " + PluginInstanceManager.this.mVersion);
                        return null;
                    }
                } catch (Throwable e5) {
                    Log.w(PluginInstanceManager.TAG, "Couldn't load plugin: " + pkg, e5);
                    return null;
                }
            }
        }

        private VersionInfo checkVersion(Class<?> pluginClass, T plugin, VersionInfo version) throws VersionInfo.InvalidVersionException {
            VersionInfo pv = new VersionInfo().addClass(pluginClass);
            if (pv.hasVersionInfo()) {
                version.checkVersion(pv);
                return pv;
            } else if (plugin.getVersion() == version.getDefaultVersion()) {
                return null;
            } else {
                throw new VersionInfo.InvalidVersionException("Invalid legacy version", false);
            }
        }
    }

    public static class PluginContextWrapper extends ContextWrapper {
        private final ClassLoader mClassLoader;
        private LayoutInflater mInflater;

        public PluginContextWrapper(Context base, ClassLoader classLoader) {
            super(base);
            this.mClassLoader = classLoader;
        }

        @Override // android.content.ContextWrapper, android.content.Context
        public ClassLoader getClassLoader() {
            return this.mClassLoader;
        }

        @Override // android.content.ContextWrapper, android.content.Context
        public Object getSystemService(String name) {
            if (!"layout_inflater".equals(name)) {
                return getBaseContext().getSystemService(name);
            }
            if (this.mInflater == null) {
                this.mInflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
            }
            return this.mInflater;
        }
    }

    /* access modifiers changed from: package-private */
    public static class PluginInfo<T> {
        private String mClass;
        String mPackage;
        T mPlugin;
        private final Context mPluginContext;
        private final VersionInfo mVersion;

        public PluginInfo(String pkg, String cls, T plugin, Context pluginContext, VersionInfo info) {
            this.mPlugin = plugin;
            this.mClass = cls;
            this.mPackage = pkg;
            this.mPluginContext = pluginContext;
            this.mVersion = info;
        }
    }
}
