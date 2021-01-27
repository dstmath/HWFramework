package com.android.systemui.shared.plugins;

import android.app.ActivityThread;
import android.app.LoadedApk;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.pm.PackageManagerServiceEx;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import com.android.systemui.shared.plugins.PluginInstanceManager;
import com.android.systemui.shared.plugins.PluginManager;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PluginManagerImpl extends BroadcastReceiver implements PluginManager {
    static final String DISABLE_PLUGIN = "com.android.systemui.action.DISABLE_PLUGIN";
    private static final boolean IS_FACTORY_MODE = SystemProperties.get("ro.runmode", "normal").equals("factory");
    private static final String QS_PLUGIN_ACTION = "com.android.systemui.action.PLUGIN_QS_SIMPLE";
    private static final String TAG = PluginManagerImpl.class.getSimpleName();
    private static PluginManager sInstance;
    private final boolean isDebuggable;
    private final Map<String, ClassLoader> mClassLoaders;
    private final Context mContext;
    private final PluginInstanceManagerFactory mFactory;
    private boolean mHasOneShot;
    private boolean mListening;
    private Looper mLooper;
    private final ArraySet<String> mOneShotPackages;
    private ClassLoaderFilter mParentClassLoader;
    private final PluginEnabler mPluginEnabler;
    private final PluginInitializer mPluginInitializer;
    private final ArrayMap<PluginListener<?>, PluginInstanceManager> mPluginMap;
    private final PluginPrefs mPluginPrefs;
    private final ArraySet<String> mWhitelistedPlugins;

    public PluginManagerImpl(Context context, PluginInitializer initializer) {
        this(context, new PluginInstanceManagerFactory(), Build.IS_DEBUGGABLE, Thread.getUncaughtExceptionPreHandler(), initializer);
    }

    @VisibleForTesting
    PluginManagerImpl(Context context, PluginInstanceManagerFactory factory, boolean debuggable, Thread.UncaughtExceptionHandler defaultHandler, final PluginInitializer initializer) {
        this.mPluginMap = new ArrayMap<>();
        this.mClassLoaders = new ArrayMap();
        this.mOneShotPackages = new ArraySet<>();
        this.mWhitelistedPlugins = new ArraySet<>();
        this.mContext = context;
        this.mFactory = factory;
        this.mLooper = initializer.getBgLooper();
        this.isDebuggable = debuggable;
        this.mWhitelistedPlugins.addAll(Arrays.asList(initializer.getWhitelistedPlugins(this.mContext)));
        this.mPluginPrefs = new PluginPrefs(this.mContext);
        this.mPluginEnabler = initializer.getPluginEnabler(this.mContext);
        this.mPluginInitializer = initializer;
        Thread.setUncaughtExceptionPreHandler(new PluginExceptionHandler(defaultHandler));
        new Handler(this.mLooper).post(new Runnable() {
            /* class com.android.systemui.shared.plugins.PluginManagerImpl.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                initializer.onPluginManagerInit();
            }
        });
        this.mClassLoaders.put(context.getApplicationInfo().packageName, context.getClassLoader());
    }

    @Override // com.android.systemui.shared.plugins.PluginManager
    public String[] getWhitelistedPlugins() {
        return (String[]) this.mWhitelistedPlugins.toArray(new String[0]);
    }

    public PluginEnabler getPluginEnabler() {
        return this.mPluginEnabler;
    }

    @Override // com.android.systemui.shared.plugins.PluginManager
    public <T extends Plugin> T getOneShotPlugin(Class<T> cls) {
        ProvidesInterface info = (ProvidesInterface) cls.getDeclaredAnnotation(ProvidesInterface.class);
        if (info == null) {
            throw new RuntimeException(cls + " doesn't provide an interface");
        } else if (!TextUtils.isEmpty(info.action())) {
            return (T) getOneShotPlugin(info.action(), cls);
        } else {
            throw new RuntimeException(cls + " doesn't provide an action");
        }
    }

    /* JADX WARN: Type inference failed for: r3v5, types: [T extends com.android.systemui.plugins.Plugin, com.android.systemui.plugins.Plugin] */
    @Override // com.android.systemui.shared.plugins.PluginManager
    public <T extends Plugin> T getOneShotPlugin(String action, Class<?> cls) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            PluginInstanceManager<T> p = this.mFactory.createPluginInstanceManager(this.mContext, action, null, false, this.mLooper, cls, this);
            this.mPluginPrefs.addAction(action);
            PluginInstanceManager.PluginInfo<T> info = p.getPlugin();
            if (info == null) {
                return null;
            }
            this.mOneShotPackages.add(info.mPackage);
            this.mHasOneShot = true;
            startListening();
            return info.mPlugin;
        }
        throw new RuntimeException("Must be called from UI thread");
    }

    @Override // com.android.systemui.shared.plugins.PluginManager
    public <T extends Plugin> void addPluginListener(PluginListener<T> listener, Class<?> cls) {
        addPluginListener((PluginListener) listener, cls, false);
    }

    @Override // com.android.systemui.shared.plugins.PluginManager
    public <T extends Plugin> void addPluginListener(PluginListener<T> listener, Class<?> cls, boolean allowMultiple) {
        addPluginListener(PluginManager.Helper.getAction(cls), listener, cls, allowMultiple);
    }

    @Override // com.android.systemui.shared.plugins.PluginManager
    public <T extends Plugin> void addPluginListener(String action, PluginListener<T> listener, Class<?> cls) {
        addPluginListener(action, listener, cls, false);
    }

    @Override // com.android.systemui.shared.plugins.PluginManager
    public <T extends Plugin> void addPluginListener(String action, PluginListener<T> listener, Class cls, boolean allowMultiple) {
        this.mPluginPrefs.addAction(action);
        PluginInstanceManager p = this.mFactory.createPluginInstanceManager(this.mContext, action, listener, allowMultiple, this.mLooper, cls, this);
        p.loadAll();
        this.mPluginMap.put(listener, p);
        startListening();
    }

    @Override // com.android.systemui.shared.plugins.PluginManager
    public void removePluginListener(PluginListener<?> listener) {
        if (this.mPluginMap.containsKey(listener)) {
            this.mPluginMap.remove(listener).destroy();
            if (this.mPluginMap.size() == 0) {
                stopListening();
            }
        }
    }

    private void startListening() {
        if (!this.mListening) {
            this.mListening = true;
            IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addAction("android.intent.action.PACKAGE_REPLACED");
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction(PluginManager.PLUGIN_CHANGED);
            filter.addAction(DISABLE_PLUGIN);
            filter.addDataScheme("package");
            this.mContext.registerReceiver(this, filter);
            this.mContext.registerReceiver(this, new IntentFilter("android.intent.action.USER_UNLOCKED"));
        }
    }

    private void stopListening() {
        if (this.mListening && !this.mHasOneShot) {
            this.mListening = false;
            this.mContext.unregisterReceiver(this);
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        int disableReason;
        if (intent == null) {
            Log.w(TAG, "intent is null, return");
        } else if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
            for (PluginInstanceManager manager : this.mPluginMap.values()) {
                if (QS_PLUGIN_ACTION.equals(manager.getPluginAction())) {
                    Log.i(TAG, "qs plugin action, not load all");
                } else {
                    manager.loadAll();
                }
            }
        } else if (DISABLE_PLUGIN.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri == null || uri.toString().length() <= 10) {
                Log.w(TAG, "uri is illegal, return");
                return;
            }
            ComponentName component = ComponentName.unflattenFromString(uri.toString().substring(10));
            if (component == null) {
                Log.w(TAG, "component is null, return");
            } else if (!isPluginWhitelisted(component)) {
                getPluginEnabler().setDisabled(component, 1);
                NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.cancel(component.getClassName(), 6);
                }
            }
        } else {
            Uri data = intent.getData();
            if (data == null) {
                Log.w(TAG, "data is null, return");
                return;
            }
            String pkg = data.getEncodedSchemeSpecificPart();
            ComponentName componentName = ComponentName.unflattenFromString(pkg);
            if (this.mOneShotPackages.contains(pkg)) {
                int icon = this.mContext.getResources().getIdentifier("tuner", "drawable", this.mContext.getPackageName());
                int color = Resources.getSystem().getIdentifier("system_notification_accent_color", "color", PackageManagerServiceEx.PLATFORM_PACKAGE_NAME);
                String label = pkg;
                try {
                    PackageManager pm = this.mContext.getPackageManager();
                    if (pm != null) {
                        label = pm.getApplicationInfo(pkg, 0).loadLabel(pm).toString();
                    }
                } catch (PackageManager.NameNotFoundException e) {
                }
                Notification.Builder color2 = new Notification.Builder(this.mContext, PluginManager.NOTIFICATION_CHANNEL_ID).setSmallIcon(icon).setWhen(0).setShowWhen(false).setPriority(2).setVisibility(1).setColor(this.mContext.getColor(color));
                Notification.Builder nb = color2.setContentTitle("Plugin \"" + label + "\" has updated").setContentText("Restart SysUI for changes to take effect.");
                Intent intent2 = new Intent("com.android.systemui.action.RESTART");
                nb.addAction(new Notification.Action.Builder((Icon) null, "Restart SysUI", PendingIntent.getBroadcast(this.mContext, 0, intent2.setData(Uri.parse("package://" + pkg)), 0)).build());
                NotificationManager notificationManager2 = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
                if (notificationManager2 != null) {
                    notificationManager2.notifyAsUser(pkg, 6, nb.build(), UserHandle.ALL);
                }
            }
            if (clearClassLoader(pkg)) {
                if (!Build.IS_ENG || IS_FACTORY_MODE) {
                    String str = TAG;
                    Log.v(str, "Reloading " + pkg);
                } else {
                    Context context2 = this.mContext;
                    Toast.makeText(context2, "Reloading " + pkg, 1).show();
                }
            }
            if ("android.intent.action.PACKAGE_REPLACED".equals(intent.getAction()) && componentName != null && ((disableReason = getPluginEnabler().getDisableReason(componentName)) == 2 || disableReason == 3 || disableReason == 1)) {
                String str2 = TAG;
                Log.i(str2, "Re-enabling previously disabled plugin that has been updated: " + componentName.flattenToShortString());
                getPluginEnabler().setEnabled(componentName);
            }
            if (!"android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                for (PluginInstanceManager manager2 : this.mPluginMap.values()) {
                    manager2.onPackageChange(pkg);
                }
                return;
            }
            for (PluginInstanceManager manager3 : this.mPluginMap.values()) {
                manager3.onPackageRemoved(pkg);
            }
        }
    }

    public ClassLoader getClassLoader(ApplicationInfo appInfo) {
        if (!this.isDebuggable && !isPluginPackageWhitelisted(appInfo.packageName)) {
            String str = TAG;
            Log.w(str, "Cannot get class loader for non-whitelisted plugin. Src:" + appInfo.sourceDir + ", pkg: " + appInfo.packageName);
            return null;
        } else if (this.mClassLoaders.containsKey(appInfo.packageName)) {
            return this.mClassLoaders.get(appInfo.packageName);
        } else {
            List<String> zipPaths = new ArrayList<>();
            List<String> libPaths = new ArrayList<>();
            LoadedApk.makePaths((ActivityThread) null, true, appInfo, zipPaths, libPaths);
            ClassLoader classLoader = new PathClassLoader(TextUtils.join(File.pathSeparator, zipPaths), TextUtils.join(File.pathSeparator, libPaths), getParentClassLoader());
            this.mClassLoaders.put(appInfo.packageName, classLoader);
            return classLoader;
        }
    }

    private boolean clearClassLoader(String pkg) {
        return this.mClassLoaders.remove(pkg) != null;
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getParentClassLoader() {
        if (this.mParentClassLoader == null) {
            this.mParentClassLoader = new ClassLoaderFilter(getClass().getClassLoader(), "com.android.systemui.plugin");
        }
        return this.mParentClassLoader;
    }

    @Override // com.android.systemui.shared.plugins.PluginManager
    public <T> boolean dependsOn(Plugin p, Class<T> cls) {
        for (int i = 0; i < this.mPluginMap.size(); i++) {
            if (this.mPluginMap.valueAt(i).dependsOn(p, cls)) {
                return true;
            }
        }
        return false;
    }

    public void handleWtfs() {
        this.mPluginInitializer.handleWtfs();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println(String.format("  plugin map (%d):", Integer.valueOf(this.mPluginMap.size())));
        for (PluginListener listener : this.mPluginMap.keySet()) {
            pw.println(String.format("    %s -> %s", listener, this.mPluginMap.get(listener)));
        }
    }

    @VisibleForTesting
    public static class PluginInstanceManagerFactory {
        public <T extends Plugin> PluginInstanceManager createPluginInstanceManager(Context context, String action, PluginListener<T> listener, boolean allowMultiple, Looper looper, Class<?> cls, PluginManagerImpl manager) {
            return new PluginInstanceManager(context, action, listener, allowMultiple, looper, new VersionInfo().addClass(cls), manager);
        }
    }

    private boolean isPluginPackageWhitelisted(String packageName) {
        Iterator<String> it = this.mWhitelistedPlugins.iterator();
        while (it.hasNext()) {
            String componentNameOrPackage = it.next();
            ComponentName componentName = ComponentName.unflattenFromString(componentNameOrPackage);
            if (componentName != null) {
                if (componentName.getPackageName().equals(packageName)) {
                    return true;
                }
            } else if (componentNameOrPackage.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPluginWhitelisted(ComponentName pluginName) {
        Iterator<String> it = this.mWhitelistedPlugins.iterator();
        while (it.hasNext()) {
            String componentNameOrPackage = it.next();
            ComponentName componentName = ComponentName.unflattenFromString(componentNameOrPackage);
            if (componentName != null) {
                if (componentName.equals(pluginName)) {
                    return true;
                }
            } else if (componentNameOrPackage.equals(pluginName.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static class ClassLoaderFilter extends ClassLoader {
        private final ClassLoader mBase;
        private final String mPackage;

        public ClassLoaderFilter(ClassLoader base, String pkg) {
            super(ClassLoader.getSystemClassLoader());
            this.mBase = base;
            this.mPackage = pkg;
        }

        /* access modifiers changed from: protected */
        @Override // java.lang.ClassLoader
        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.startsWith(this.mPackage)) {
                super.loadClass(name, resolve);
            }
            return this.mBase.loadClass(name);
        }
    }

    private class PluginExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Thread.UncaughtExceptionHandler mHandler;

        private PluginExceptionHandler(Thread.UncaughtExceptionHandler handler) {
            this.mHandler = handler;
        }

        @Override // java.lang.Thread.UncaughtExceptionHandler
        public void uncaughtException(Thread thread, Throwable throwable) {
            if (SystemProperties.getBoolean("plugin.debugging", false)) {
                this.mHandler.uncaughtException(thread, throwable);
                return;
            }
            boolean disabledAny = checkStack(throwable);
            if (!disabledAny) {
                for (PluginInstanceManager manager : PluginManagerImpl.this.mPluginMap.values()) {
                    disabledAny |= manager.disableAll();
                }
            }
            if (disabledAny) {
                throwable = new CrashWhilePluginActiveException(throwable);
            }
            this.mHandler.uncaughtException(thread, throwable);
        }

        private boolean checkStack(Throwable throwable) {
            if (throwable == null) {
                return false;
            }
            boolean disabledAny = false;
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                for (PluginInstanceManager manager : PluginManagerImpl.this.mPluginMap.values()) {
                    disabledAny |= manager.checkAndDisable(element.getClassName());
                }
            }
            return checkStack(throwable.getCause()) | disabledAny;
        }
    }

    public static class CrashWhilePluginActiveException extends RuntimeException {
        public CrashWhilePluginActiveException(Throwable throwable) {
            super(throwable);
        }
    }
}
