package huawei.android.widget.loader;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.Log;
import com.huawei.uikit.effect.BuildConfig;
import java.lang.reflect.Field;

public class ResLoader {
    private static final String HW_SYSTEM_RES_PACKAGE_NAME = "androidhwext";
    private static final String HW_THEME_META_NAME = "hwc-theme";
    private static final String HW_WIDGET_THEME = "HwWidgetTheme";
    private static final String PLUGIN_RES_PACKAGE_NAME = "com.huawei.hwwidgetplugin";
    private static final String RES_TYPE_STYLEABLE = "styleable";
    private static final String TAG = "ResLoader";
    private static ResLoader mInstance;
    private boolean mIsUseHwframeworkRes = true;
    private String mPluginPath;

    private ResLoader() {
    }

    public static synchronized ResLoader getInstance() {
        ResLoader resLoader;
        synchronized (ResLoader.class) {
            if (mInstance == null) {
                mInstance = new ResLoader();
            }
            resLoader = mInstance;
        }
        return resLoader;
    }

    public Context getContext(Context base) {
        return (isInHwSystem() || getTheme(base) == null) ? base : new PluginContextWrapper(base);
    }

    public int getIdentifier(Context context, String type, String name) {
        if (context != null) {
            return getIdentifier(context, type, name, getPackageName(context));
        }
        Log.w(TAG, "getIdentifier,context is null");
        return 0;
    }

    private int getIdentifier(Context context, String type, String name, String packageName) {
        if ("styleable".equals(type)) {
            return reflectIdForInt(context, packageName, "styleable", name);
        }
        return getResources(context).getIdentifier(name, type, packageName);
    }

    public String getPackageName(Context context) {
        if (isInHwSystem()) {
            return HW_SYSTEM_RES_PACKAGE_NAME;
        }
        return context != null ? context.getPackageName() : BuildConfig.FLAVOR;
    }

    public int[] getIdentifierArray(Context context, String type, String name) {
        if (context == null) {
            Log.w(TAG, "getIdentifierArray, context is null");
            return null;
        }
        Object ids = reflectId(context, getPackageName(context), type, name);
        if (ids instanceof int[]) {
            return (int[]) ids;
        }
        Log.e(TAG, "getIdentifierArray: can not get the resource id array, please check the resource type and name");
        return null;
    }

    private int reflectIdForInt(Context context, String packageName, String type, String name) {
        Object id = reflectId(context, packageName, type, name);
        if (id instanceof Integer) {
            return ((Integer) id).intValue();
        }
        Log.e(TAG, "getIdentifier: can not get the resource id, please check the resource type and name.");
        return -1;
    }

    public Resources.Theme getTheme(Context context) {
        if (context == null) {
            Log.w(TAG, "getTheme, context is null");
            return null;
        }
        Resources.Theme theme = context.getTheme();
        if (isInHwSystem()) {
            return theme;
        }
        String themeFromMetaData = null;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            try {
                ActivityInfo info = activity.getPackageManager().getActivityInfo(activity.getComponentName(), 128);
                if (info == null || info.metaData == null) {
                    ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
                    if (!(appInfo == null || appInfo.metaData == null)) {
                        themeFromMetaData = appInfo.metaData.getString(HW_THEME_META_NAME);
                    }
                } else {
                    themeFromMetaData = info.metaData.getString(HW_THEME_META_NAME);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "getTheme: the activity is not found");
            }
        }
        if (themeFromMetaData == null) {
            return theme;
        }
        int themeId = -1;
        char c = 65535;
        if (themeFromMetaData.hashCode() == -734899914 && themeFromMetaData.equals(HW_WIDGET_THEME)) {
            c = 0;
        }
        if (c != 0) {
            Log.e(TAG, "getTheme: the theme from meta is wrong");
        } else {
            themeId = reflectIdForInt(context, PLUGIN_RES_PACKAGE_NAME, "style", "Theme_Emui");
        }
        if (themeId <= 0) {
            return theme;
        }
        Resources.Theme theme2 = getResources(context).newTheme();
        theme2.applyStyle(themeId, true);
        return theme2;
    }

    public Resources getResources(Context context) {
        if (context != null) {
            return context.getResources();
        }
        return null;
    }

    private Object reflectId(Context context, String packageName, String type, String name) {
        try {
            Class<?> clazz = context.getClassLoader().loadClass(packageName + ".R$" + type);
            String str = packageName + ".R$" + type;
            if (clazz != null) {
                Field field = clazz.getField(name);
                field.setAccessible(true);
                return field.get(clazz);
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "getIdentifier: com.huawei.hwwidgetplugin.R." + type + " not found");
        } catch (NoSuchFieldException e2) {
            Log.e(TAG, "getIdentifier: " + packageName + ".R." + type + "." + name + " not found");
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "getIdentifier: IllegalAccessException of " + packageName + ".R." + type + "." + name);
        }
        return 0;
    }

    public int getInternalId(String type, String name) {
        try {
            Field field = Class.forName("com.android.internal.R$" + type).getField(name);
            field.setAccessible(true);
            return field.getInt(null);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "getInternalId: com.android.internal.R." + type + " not found");
            return 0;
        } catch (NoSuchFieldException e2) {
            Log.e(TAG, "getInternalId: com.android.internal.R." + type + "." + name + " not found");
            return 0;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "getInternalId: IllegalAccessException of com.android.internal.R." + type + "." + name);
            return 0;
        }
    }

    private boolean isInHwSystem() {
        return this.mIsUseHwframeworkRes;
    }

    public void setInHwSystem(boolean isEnable) {
        this.mIsUseHwframeworkRes = isEnable;
    }
}
