package com.huawei.pluginmanager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.huawei.android.os.BuildEx;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public final class PluginLoader {
    public static final int FAIL = -1;
    private static final String PLUGIN_ENTRY_CLASS_NAME = ".PluginEntry";
    private static final String PLUGIN_ENTRY_CLASS_PRE = "com.huawei.plugin.";
    private static final Map<String, PluginInfo> PLUGIN_INFO_MAP = new ArrayMap();
    public static final int SUCCESS = 0;
    private static final String TAG = "PluginLoader";

    private PluginLoader() {
    }

    public static IPlugin loadPlugin(Context context, String pluginName) {
        if (context == null || TextUtils.isEmpty(pluginName)) {
            Log.e(TAG, "loadPlugin context is null or pluginName is empty");
            return null;
        }
        synchronized (PLUGIN_INFO_MAP) {
            PluginInfo pi = PLUGIN_INFO_MAP.get(pluginName);
            if (pi == null) {
                pi = createPluginInfo(context, pluginName);
                if (pi == null) {
                    Log.w(TAG, "loadPlugin createPluginInfo PluginInfo fail: " + pluginName);
                    return null;
                }
                PLUGIN_INFO_MAP.put(pluginName, pi);
            }
            return pi.getPlugin();
        }
    }

    private static PluginInfo createPluginInfo(Context context, String pluginName) {
        Log.i(TAG, "createPluginInfo pluginName: " + pluginName);
        if (context == null || TextUtils.isEmpty(pluginName)) {
            Log.w(TAG, "createPluginInfo context is null or pluginName is empty");
            return null;
        }
        try {
            Context pluginContext = context.createContextForSplit(pluginName);
            Log.i(TAG, "createPluginInfo pluginContext: " + pluginContext);
            IPluginEntry pluginEntry = getPluginEntry(pluginContext, pluginName);
            if (pluginEntry == null) {
                Log.w(TAG, "createPluginInfo pluginEntry is null");
                return null;
            }
            int pluginSdk = PluginManager.getPluginSdkVersionCode(context);
            if (pluginEntry.getMinEmuiSdkVersion() > BuildEx.VERSION.EMUI_SDK_INT || pluginSdk < pluginEntry.getMinPluginSdkVersion()) {
                Log.e(TAG, "too old Runtime version: EMUI: " + BuildEx.VERSION.EMUI_SDK_INT + " pluginSdk: " + pluginSdk);
                return null;
            }
            IPlugin plugin = pluginEntry.loadPlugin(context);
            if (plugin != null) {
                return new PluginInfo(pluginContext, context, pluginName, pluginEntry, plugin);
            }
            Log.w(TAG, "createPluginInfo plugin is null");
            return null;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "createPluginInfo NameNotFoundException: " + pluginName);
            return null;
        }
    }

    private static IPluginEntry getPluginEntry(Context pluginContext, String pluginName) {
        ClassLoader pluginClassLoader = pluginContext.getClassLoader();
        Log.i(TAG, "loadPluginEntry class loader: " + pluginClassLoader);
        if (pluginClassLoader == null) {
            return null;
        }
        try {
            return (IPluginEntry) pluginClassLoader.loadClass(PLUGIN_ENTRY_CLASS_PRE + pluginName + PLUGIN_ENTRY_CLASS_NAME).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "loadPluginEntry NoSuchMethodException");
            return null;
        } catch (IllegalAccessException e2) {
            Log.w(TAG, "loadPluginEntry IllegalAccessException");
            return null;
        } catch (InstantiationException e3) {
            Log.w(TAG, "loadPluginEntry InstantiationException");
            return null;
        } catch (InvocationTargetException e4) {
            Log.w(TAG, "loadPluginEntry InvocationTargetException");
            return null;
        } catch (ClassNotFoundException e5) {
            Log.w(TAG, "loadPluginEntry ClassNotFoundException");
            return null;
        } catch (IllegalArgumentException e6) {
            Log.w(TAG, "loadPluginEntry IllegalArgumentException");
            return null;
        }
    }

    public static PluginInfo getPluginInfo(String pluginName) {
        PluginInfo pluginInfo;
        if (TextUtils.isEmpty(pluginName)) {
            Log.e(TAG, "getPluginInfo pluginName is empty");
            return null;
        }
        synchronized (PLUGIN_INFO_MAP) {
            pluginInfo = PLUGIN_INFO_MAP.get(pluginName);
        }
        return pluginInfo;
    }

    public static int releasePlugin(String pluginName) {
        Log.i(TAG, "releasePlugin pluginName: " + pluginName);
        synchronized (PLUGIN_INFO_MAP) {
            PluginInfo info = PLUGIN_INFO_MAP.get(pluginName);
            if (info == null) {
                Log.e(TAG, "the plugin is not load for " + pluginName);
                return -1;
            }
            int result = info.getPluginEntry().releasePlugin();
            Log.i(TAG, "releasePlugin inner result=" + result);
            if (info.getBaseContext() != null) {
                try {
                    info.getBaseContext().releaseContextForSplit(pluginName);
                    PLUGIN_INFO_MAP.remove(pluginName);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w(TAG, "releasePlugin NameNotFoundException: " + pluginName);
                    return -1;
                }
            }
            return 0;
        }
    }
}
