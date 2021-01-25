package com.huawei.ace.runtime;

import com.huawei.ace.plugin.DecodeRequestCode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import ohos.ace.featureabilityplugin.FeatureAbilityPlugin;
import ohos.app.Context;

public final class HarmonySystemPluginLoader {
    private static final String TAG = HarmonySystemPluginLoader.class.getSimpleName();
    private static Map<Integer, Context> contextMap = new HashMap();
    private static Map<String, String> groupAndSystemPluginMap = new HashMap();
    private static StringBuilder jsCode = new StringBuilder();
    private static boolean loadPluginJsCodeCompleted = false;
    private static CompletableFuture<Void> loadPluginJsCodeFuture = null;
    private static Set<String> pluginPackageSets = new HashSet();
    private static Map<String, Set<Integer>> pluginRegisterMap = new HashMap();

    static {
        pluginPackageSets.add("com.huawei.ace.systemplugin.battery.BatteryPlugin");
        pluginPackageSets.add("com.huawei.ace.systemplugin.brightness.BrightnessPlugin");
        pluginPackageSets.add("com.huawei.ace.systemplugin.geolocation.GeolocationPlugin");
        pluginPackageSets.add("com.huawei.ace.systemplugin.network.NetworkPlugin");
        pluginPackageSets.add("com.huawei.ace.systemplugin.winsize.WinsizePlugin");
        pluginPackageSets.add("ohos.sensor.plugin.SensorPlugin");
        pluginPackageSets.add("ohos.vibrator.plugin.VibratorPlugin");
        pluginPackageSets.add("ohos.appexecfwk.plugin.HapInstallPlugin");
        pluginPackageSets.add("ohos.data.jsstorage.StoragePlugin");
        pluginPackageSets.add("ohos.data.jsfile.FilePlugin");
        pluginPackageSets.add("ohos.miscservices.httpaccess.HttpAccessPlugin");
        pluginPackageSets.add("ohos.eventkit.notification.plugin.NotificationPlugin");
    }

    private HarmonySystemPluginLoader() {
    }

    public static void loadPlugin(Context context, int i) {
        contextMap.put(Integer.valueOf(i), context);
        CompletableFuture.runAsync(new Runnable() {
            /* class com.huawei.ace.runtime.$$Lambda$HarmonySystemPluginLoader$aREWPJno0omCY58T7aRZ1XpFSio */

            @Override // java.lang.Runnable
            public final void run() {
                FeatureAbilityPlugin.register(Context.this);
            }
        });
        if (!loadPluginJsCodeCompleted) {
            loadPluginJsCodeFuture = CompletableFuture.runAsync($$Lambda$HarmonySystemPluginLoader$t4qS7frk7nerYT9Sf5w4mMzQPw.INSTANCE);
        }
    }

    static /* synthetic */ void lambda$loadPlugin$1() {
        for (String str : pluginPackageSets) {
            loadReflectPluginJsCode(str);
            loadReflectPluginGroup(str);
        }
    }

    private static void loadReflectPluginRegister(Context context, String str) {
        try {
            Class.forName(str).getMethod("register", Context.class).invoke(null, context);
        } catch (ReflectiveOperationException e) {
            String str2 = TAG;
            ALog.e(str2, "Load plugin exception, Class:" + str + ", cause:" + e.getCause());
        }
    }

    private static void loadReflectPluginJsCode(String str) {
        try {
            Object invoke = Class.forName(str).getMethod("getJsCode", new Class[0]).invoke(null, new Object[0]);
            if (!(invoke instanceof String)) {
                String str2 = TAG;
                ALog.e(str2, "get js code failed, package:" + str);
                return;
            }
            StringBuilder sb = jsCode;
            sb.append((String) invoke);
            sb.append(System.lineSeparator());
        } catch (ReflectiveOperationException unused) {
            String str3 = TAG;
            ALog.e(str3, "Load plugin js code exception, Class:" + str);
        }
    }

    private static void loadReflectPluginGroup(String str) {
        try {
            Object invoke = Class.forName(str).getMethod("getPluginGroup", new Class[0]).invoke(null, new Object[0]);
            if (invoke instanceof Set) {
                for (Object obj : (Set) invoke) {
                    groupAndSystemPluginMap.put((String) String.class.cast(obj), str);
                }
                return;
            }
            String str2 = TAG;
            ALog.e(str2, "get plugin group failed, package:" + str);
        } catch (ReflectiveOperationException unused) {
            String str3 = TAG;
            ALog.e(str3, "Load plugin group exception, Class:" + str);
        }
    }

    public static void loadPluginJsCode(AceContainer aceContainer) {
        if (!loadPluginJsCodeCompleted) {
            CompletableFuture<Void> completableFuture = loadPluginJsCodeFuture;
            if (completableFuture == null) {
                ALog.e(TAG, "Load plugin js code failed");
                return;
            }
            try {
                completableFuture.get();
                loadPluginJsCodeCompleted = true;
            } catch (InterruptedException | ExecutionException unused) {
                ALog.e(TAG, "get plugin js code future throw exception");
                return;
            }
        }
        aceContainer.loadPluginJsCode(jsCode.toString());
    }

    public static void deregisterPlugin(int i) {
        ALog.i(TAG, "deregister plugin");
        FeatureAbilityPlugin.deregister(contextMap.get(Integer.valueOf(i)));
        for (String str : pluginRegisterMap.keySet()) {
            try {
                Class.forName(str).getMethod("deregister", Context.class).invoke(null, contextMap.get(Integer.valueOf(i)));
            } catch (ReflectiveOperationException unused) {
                String str2 = TAG;
                ALog.e(str2, "deregister plugin exception, Class:" + str);
            }
        }
        contextMap.remove(Integer.valueOf(i));
        for (Set<Integer> set : pluginRegisterMap.values()) {
            set.remove(Integer.valueOf(i));
        }
    }

    public static void checkAndLoadSystemPlugin(String str, int i) {
        if (groupAndSystemPluginMap.containsKey(str)) {
            if (!contextMap.containsKey(Integer.valueOf(i))) {
                String str2 = TAG;
                ALog.e(str2, "context not load in container:" + i);
                return;
            }
            String str3 = groupAndSystemPluginMap.get(str);
            if (pluginRegisterMap.containsKey(str3)) {
                Set<Integer> set = pluginRegisterMap.get(str3);
                if (!set.contains(Integer.valueOf(i))) {
                    loadReflectPluginRegister(contextMap.get(Integer.valueOf(i)), str3);
                    set.add(Integer.valueOf(i));
                    pluginRegisterMap.put(str3, set);
                    return;
                }
                return;
            }
            loadReflectPluginRegister(contextMap.get(Integer.valueOf(i)), str3);
            HashSet hashSet = new HashSet();
            hashSet.add(Integer.valueOf(i));
            pluginRegisterMap.put(str3, hashSet);
        }
    }

    public static void transmitResultToPlugin(int i, int[] iArr) {
        String pluginName = DecodeRequestCode.getPluginName(i);
        try {
            if (pluginName.isEmpty()) {
                ALog.e(TAG, "can not find plugin package name by requestCode");
            } else {
                Class.forName(pluginName).getMethod("permissionResult", int[].class).invoke(null, iArr);
            }
        } catch (ReflectiveOperationException unused) {
            String str = TAG;
            ALog.e(str, "reflectPermissionResult exception, Class:" + pluginName);
        }
    }
}
