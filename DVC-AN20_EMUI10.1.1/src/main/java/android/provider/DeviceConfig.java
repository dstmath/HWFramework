package android.provider;

import android.Manifest;
import android.annotation.SystemApi;
import android.app.ActivityThread;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

@SystemApi
public final class DeviceConfig {
    public static final Uri CONTENT_URI = Uri.parse("content://settings/config");
    @SystemApi
    public static final String NAMESPACE_ACTIVITY_MANAGER = "activity_manager";
    @SystemApi
    public static final String NAMESPACE_ACTIVITY_MANAGER_NATIVE_BOOT = "activity_manager_native_boot";
    @SystemApi
    public static final String NAMESPACE_APP_COMPAT = "app_compat";
    @SystemApi
    public static final String NAMESPACE_ATTENTION_MANAGER_SERVICE = "attention_manager_service";
    @SystemApi
    public static final String NAMESPACE_AUTOFILL = "autofill";
    @SystemApi
    public static final String NAMESPACE_CONNECTIVITY = "connectivity";
    public static final String NAMESPACE_CONTACTS_PROVIDER = "contacts_provider";
    @SystemApi
    public static final String NAMESPACE_CONTENT_CAPTURE = "content_capture";
    @SystemApi
    public static final String NAMESPACE_DEX_BOOT = "dex_boot";
    @SystemApi
    public static final String NAMESPACE_GAME_DRIVER = "game_driver";
    @SystemApi
    public static final String NAMESPACE_INPUT_NATIVE_BOOT = "input_native_boot";
    @SystemApi
    public static final String NAMESPACE_INTELLIGENCE_ATTENTION = "intelligence_attention";
    public static final String NAMESPACE_INTELLIGENCE_CONTENT_SUGGESTIONS = "intelligence_content_suggestions";
    @SystemApi
    public static final String NAMESPACE_MEDIA_NATIVE = "media_native";
    @SystemApi
    public static final String NAMESPACE_NETD_NATIVE = "netd_native";
    @SystemApi
    public static final String NAMESPACE_PRIVACY = "privacy";
    @SystemApi
    public static final String NAMESPACE_ROLLBACK = "rollback";
    @SystemApi
    public static final String NAMESPACE_ROLLBACK_BOOT = "rollback_boot";
    @SystemApi
    public static final String NAMESPACE_RUNTIME = "runtime";
    @SystemApi
    public static final String NAMESPACE_RUNTIME_NATIVE = "runtime_native";
    @SystemApi
    public static final String NAMESPACE_RUNTIME_NATIVE_BOOT = "runtime_native_boot";
    @SystemApi
    public static final String NAMESPACE_SCHEDULER = "scheduler";
    public static final String NAMESPACE_SETTINGS_UI = "settings_ui";
    @SystemApi
    public static final String NAMESPACE_STORAGE = "storage";
    @SystemApi
    public static final String NAMESPACE_SYSTEMUI = "systemui";
    @SystemApi
    public static final String NAMESPACE_TELEPHONY = "telephony";
    @SystemApi
    public static final String NAMESPACE_TEXTCLASSIFIER = "textclassifier";
    public static final String NAMESPACE_WINDOW_MANAGER = "android:window_manager";
    private static final List<String> PUBLIC_NAMESPACES = Arrays.asList(NAMESPACE_TEXTCLASSIFIER, NAMESPACE_RUNTIME);
    private static final String TAG = "DeviceConfig";
    @GuardedBy({"sLock"})
    private static ArrayMap<OnPropertiesChangedListener, Pair<String, Executor>> sListeners = new ArrayMap<>();
    private static final Object sLock = new Object();
    @GuardedBy({"sLock"})
    private static Map<String, Pair<ContentObserver, Integer>> sNamespaces = new HashMap();
    @GuardedBy({"sLock"})
    private static ArrayMap<OnPropertyChangedListener, Pair<String, Executor>> sSingleListeners = new ArrayMap<>();

    @SystemApi
    public interface OnPropertiesChangedListener {
        void onPropertiesChanged(Properties properties);
    }

    @SystemApi
    public interface OnPropertyChangedListener {
        void onPropertyChanged(String str, String str2, String str3);
    }

    public interface WindowManager {
        public static final String KEY_SYSTEM_GESTURES_EXCLUDED_BY_PRE_Q_STICKY_IMMERSIVE = "system_gestures_excluded_by_pre_q_sticky_immersive";
        public static final String KEY_SYSTEM_GESTURE_EXCLUSION_LIMIT_DP = "system_gesture_exclusion_limit_dp";
    }

    private DeviceConfig() {
    }

    @SystemApi
    public static String getProperty(String namespace, String name) {
        return Settings.Config.getString(ActivityThread.currentApplication().getContentResolver(), createCompositeName(namespace, name));
    }

    @SystemApi
    public static String getString(String namespace, String name, String defaultValue) {
        String value = getProperty(namespace, name);
        return value != null ? value : defaultValue;
    }

    @SystemApi
    public static boolean getBoolean(String namespace, String name, boolean defaultValue) {
        String value = getProperty(namespace, name);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    @SystemApi
    public static int getInt(String namespace, String name, int defaultValue) {
        String value = getProperty(namespace, name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Parsing integer failed for " + namespace + SettingsStringUtil.DELIMITER + name);
            return defaultValue;
        }
    }

    @SystemApi
    public static long getLong(String namespace, String name, long defaultValue) {
        String value = getProperty(namespace, name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Parsing long failed for " + namespace + SettingsStringUtil.DELIMITER + name);
            return defaultValue;
        }
    }

    @SystemApi
    public static float getFloat(String namespace, String name, float defaultValue) {
        String value = getProperty(namespace, name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Parsing float failed for " + namespace + SettingsStringUtil.DELIMITER + name);
            return defaultValue;
        }
    }

    @SystemApi
    public static boolean setProperty(String namespace, String name, String value, boolean makeDefault) {
        return Settings.Config.putString(ActivityThread.currentApplication().getContentResolver(), createCompositeName(namespace, name), value, makeDefault);
    }

    @SystemApi
    public static void resetToDefaults(int resetMode, String namespace) {
        Settings.Config.resetToDefaults(ActivityThread.currentApplication().getContentResolver(), resetMode, namespace);
    }

    @SystemApi
    public static void addOnPropertyChangedListener(String namespace, Executor executor, OnPropertyChangedListener onPropertyChangedListener) {
        enforceReadPermission(ActivityThread.currentApplication().getApplicationContext(), namespace);
        synchronized (sLock) {
            Pair<String, Executor> oldNamespace = sSingleListeners.get(onPropertyChangedListener);
            if (oldNamespace == null) {
                sSingleListeners.put(onPropertyChangedListener, new Pair<>(namespace, executor));
                incrementNamespace(namespace);
            } else if (namespace.equals(oldNamespace.first)) {
                sSingleListeners.put(onPropertyChangedListener, new Pair<>(namespace, executor));
            } else {
                decrementNamespace(sSingleListeners.get(onPropertyChangedListener).first);
                sSingleListeners.put(onPropertyChangedListener, new Pair<>(namespace, executor));
                incrementNamespace(namespace);
            }
        }
    }

    @SystemApi
    public static void addOnPropertiesChangedListener(String namespace, Executor executor, OnPropertiesChangedListener onPropertiesChangedListener) {
        enforceReadPermission(ActivityThread.currentApplication().getApplicationContext(), namespace);
        synchronized (sLock) {
            Pair<String, Executor> oldNamespace = sListeners.get(onPropertiesChangedListener);
            if (oldNamespace == null) {
                sListeners.put(onPropertiesChangedListener, new Pair<>(namespace, executor));
                incrementNamespace(namespace);
            } else if (namespace.equals(oldNamespace.first)) {
                sListeners.put(onPropertiesChangedListener, new Pair<>(namespace, executor));
            } else {
                decrementNamespace(sListeners.get(onPropertiesChangedListener).first);
                sListeners.put(onPropertiesChangedListener, new Pair<>(namespace, executor));
                incrementNamespace(namespace);
            }
        }
    }

    @SystemApi
    public static void removeOnPropertyChangedListener(OnPropertyChangedListener onPropertyChangedListener) {
        Preconditions.checkNotNull(onPropertyChangedListener);
        synchronized (sLock) {
            if (sSingleListeners.containsKey(onPropertyChangedListener)) {
                decrementNamespace(sSingleListeners.get(onPropertyChangedListener).first);
                sSingleListeners.remove(onPropertyChangedListener);
            }
        }
    }

    @SystemApi
    public static void removeOnPropertiesChangedListener(OnPropertiesChangedListener onPropertiesChangedListener) {
        Preconditions.checkNotNull(onPropertiesChangedListener);
        synchronized (sLock) {
            if (sListeners.containsKey(onPropertiesChangedListener)) {
                decrementNamespace(sListeners.get(onPropertiesChangedListener).first);
                sListeners.remove(onPropertiesChangedListener);
            }
        }
    }

    private static String createCompositeName(String namespace, String name) {
        Preconditions.checkNotNull(namespace);
        Preconditions.checkNotNull(name);
        return namespace + "/" + name;
    }

    private static Uri createNamespaceUri(String namespace) {
        Preconditions.checkNotNull(namespace);
        return CONTENT_URI.buildUpon().appendPath(namespace).build();
    }

    @GuardedBy({"sLock"})
    private static void incrementNamespace(String namespace) {
        Preconditions.checkNotNull(namespace);
        Pair<ContentObserver, Integer> namespaceCount = sNamespaces.get(namespace);
        if (namespaceCount != null) {
            sNamespaces.put(namespace, new Pair<>(namespaceCount.first, Integer.valueOf(namespaceCount.second.intValue() + 1)));
            return;
        }
        ContentObserver contentObserver = new ContentObserver(null) {
            /* class android.provider.DeviceConfig.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange, Uri uri) {
                if (uri != null) {
                    DeviceConfig.handleChange(uri);
                }
            }
        };
        ActivityThread.currentApplication().getContentResolver().registerContentObserver(createNamespaceUri(namespace), true, contentObserver);
        sNamespaces.put(namespace, new Pair<>(contentObserver, 1));
    }

    @GuardedBy({"sLock"})
    private static void decrementNamespace(String namespace) {
        Preconditions.checkNotNull(namespace);
        Pair<ContentObserver, Integer> namespaceCount = sNamespaces.get(namespace);
        if (namespaceCount != null) {
            if (namespaceCount.second.intValue() > 1) {
                sNamespaces.put(namespace, new Pair<>(namespaceCount.first, Integer.valueOf(namespaceCount.second.intValue() - 1)));
                return;
            }
            ActivityThread.currentApplication().getContentResolver().unregisterContentObserver(namespaceCount.first);
            sNamespaces.remove(namespace);
        }
    }

    /* access modifiers changed from: private */
    public static void handleChange(Uri uri) {
        Preconditions.checkNotNull(uri);
        List<String> pathSegments = uri.getPathSegments();
        final String namespace = pathSegments.get(1);
        final String name = pathSegments.get(2);
        try {
            final String value = getProperty(namespace, name);
            synchronized (sLock) {
                for (final int i = 0; i < sListeners.size(); i++) {
                    if (namespace.equals(sListeners.valueAt(i).first)) {
                        sListeners.valueAt(i).second.execute(new Runnable() {
                            /* class android.provider.DeviceConfig.AnonymousClass2 */

                            public void run() {
                                Map<String, String> propertyMap = new HashMap<>(1);
                                propertyMap.put(name, value);
                                ((OnPropertiesChangedListener) DeviceConfig.sListeners.keyAt(i)).onPropertiesChanged(new Properties(namespace, propertyMap));
                            }
                        });
                    }
                }
                for (final int i2 = 0; i2 < sSingleListeners.size(); i2++) {
                    if (namespace.equals(sSingleListeners.valueAt(i2).first)) {
                        sSingleListeners.valueAt(i2).second.execute(new Runnable() {
                            /* class android.provider.DeviceConfig.AnonymousClass3 */

                            public void run() {
                                ((OnPropertyChangedListener) DeviceConfig.sSingleListeners.keyAt(i2)).onPropertyChanged(namespace, name, value);
                            }
                        });
                    }
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "OnPropertyChangedListener update failed: permission violation.");
        }
    }

    public static void enforceReadPermission(Context context, String namespace) {
        if (context.checkCallingOrSelfPermission(Manifest.permission.READ_DEVICE_CONFIG) != 0 && !PUBLIC_NAMESPACES.contains(namespace)) {
            throw new SecurityException("Permission denial: reading from settings requires:android.permission.READ_DEVICE_CONFIG");
        }
    }

    @SystemApi
    public static class Properties {
        private final HashMap<String, String> mMap = new HashMap<>();
        private final String mNamespace;

        Properties(String namespace, Map<String, String> keyValueMap) {
            Preconditions.checkNotNull(namespace);
            this.mNamespace = namespace;
            if (keyValueMap != null) {
                this.mMap.putAll(keyValueMap);
            }
        }

        public String getNamespace() {
            return this.mNamespace;
        }

        public Set<String> getKeyset() {
            return this.mMap.keySet();
        }

        public String getString(String name, String defaultValue) {
            Preconditions.checkNotNull(name);
            String value = this.mMap.get(name);
            return value != null ? value : defaultValue;
        }

        public boolean getBoolean(String name, boolean defaultValue) {
            Preconditions.checkNotNull(name);
            String value = this.mMap.get(name);
            return value != null ? Boolean.parseBoolean(value) : defaultValue;
        }

        public int getInt(String name, int defaultValue) {
            Preconditions.checkNotNull(name);
            String value = this.mMap.get(name);
            if (value == null) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.e(DeviceConfig.TAG, "Parsing int failed for " + name);
                return defaultValue;
            }
        }

        public long getLong(String name, long defaultValue) {
            Preconditions.checkNotNull(name);
            String value = this.mMap.get(name);
            if (value == null) {
                return defaultValue;
            }
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                Log.e(DeviceConfig.TAG, "Parsing long failed for " + name);
                return defaultValue;
            }
        }

        public float getFloat(String name, float defaultValue) {
            Preconditions.checkNotNull(name);
            String value = this.mMap.get(name);
            if (value == null) {
                return defaultValue;
            }
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                Log.e(DeviceConfig.TAG, "Parsing float failed for " + name);
                return defaultValue;
            }
        }
    }
}
