package ohos.aafwk.ability;

import android.content.Context;
import android.content.pm.PackageManager;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import ohos.agp.components.ComponentProvider;
import ohos.app.ContextDeal;
import ohos.global.resource.ResourceManagerInner;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.interwork.ui.RemoteViewEx;

class ComponentUtils {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "ComponentUtils");
    private static final String RESOURCE_TABLE = ".ResourceTable";

    static boolean initLayout(Context context, FormItemInfo formItemInfo) {
        if (!(context == null || formItemInfo == null)) {
            String bundleName = formItemInfo.getBundleName();
            String[] hapSourceDirs = formItemInfo.getHapSourceDirs();
            String layoutIdConfig = formItemInfo.getLayoutIdConfig();
            if (!(bundleName == null || hapSourceDirs == null || layoutIdConfig == null)) {
                try {
                    Context createPackageContext = context.createPackageContext(bundleName, 3);
                    if (createPackageContext == null) {
                        HiLog.error(LABEL, "initLayout get esystem context failed.", new Object[0]);
                        return false;
                    }
                    ClassLoader classLoader = createPackageContext.getClassLoader();
                    if (classLoader == null) {
                        return false;
                    }
                    if (classLoader instanceof PathClassLoader) {
                        PathClassLoader pathClassLoader = (PathClassLoader) classLoader;
                        for (String str : hapSourceDirs) {
                            pathClassLoader.addDexPath(str);
                        }
                    }
                    String str2 = bundleName + RESOURCE_TABLE;
                    int layoutId = getLayoutId(classLoader, str2, layoutIdConfig);
                    if (layoutId == 0) {
                        HiLog.error(LABEL, "initLayout get layout id failed.", new Object[0]);
                        return false;
                    }
                    int aResId = getAResId(createPackageContext, str2, layoutId);
                    if (aResId == 0) {
                        HiLog.error(LABEL, "initLayout get remote esystem layout id failed.", new Object[0]);
                        return false;
                    }
                    formItemInfo.previewLayoutId = layoutId;
                    formItemInfo.eSystemPreviewLayoutId = aResId;
                    return true;
                } catch (PackageManager.NameNotFoundException unused) {
                    HiLog.error(LABEL, "initLayout cannot find such bundle:%{public}s", new Object[]{bundleName});
                }
            }
        }
        return false;
    }

    private static int getLayoutId(ClassLoader classLoader, String str, String str2) {
        try {
            Class<?> loadClass = classLoader.loadClass(str);
            Field[] fields = loadClass.getFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && field.getName().equals(str2)) {
                    try {
                        return field.getInt(loadClass);
                    } catch (IllegalAccessException unused) {
                        continue;
                    }
                }
            }
        } catch (ClassNotFoundException unused2) {
            HiLog.warn(LABEL, "getLayoutId load class occur exception.", new Object[0]);
        }
        return 0;
    }

    private static int getAResId(Context context, String str, int i) {
        int i2;
        try {
            i2 = ResourceManagerInner.getAResId(i, Class.forName(str, false, context.getClassLoader()), context);
            try {
                HiLog.debug(LABEL, "remote resourceId=0x%{public}x.", new Object[]{Integer.valueOf(i2)});
            } catch (ClassNotFoundException unused) {
            }
        } catch (ClassNotFoundException unused2) {
            i2 = 0;
            HiLog.error(LABEL, "ClassNotFoundException ClassName=%{public}s", new Object[]{str});
            return i2;
        }
        return i2;
    }

    static RemoteViewEx getRemoteViewEx(Context context, String str, String[] strArr, ComponentProvider componentProvider) {
        if (context == null || str == null || strArr == null || componentProvider == null) {
            return null;
        }
        ohos.app.Context createContext = createContext(context, str, strArr);
        if (createContext != null) {
            return new RemoteViewEx(createContext, componentProvider);
        }
        HiLog.error(LABEL, "createContext failed, can not convert RemoteView", new Object[0]);
        return null;
    }

    private static ohos.app.Context createContext(Context context, String str, String[] strArr) {
        try {
            Context createPackageContext = context.createPackageContext(str, 3);
            if (createPackageContext == null) {
                HiLog.error(LABEL, "get esystem context failed.", new Object[0]);
                return null;
            }
            ClassLoader classLoader = createPackageContext.getClassLoader();
            ContextDeal contextDeal = new ContextDeal(createPackageContext, classLoader);
            if (classLoader instanceof PathClassLoader) {
                PathClassLoader pathClassLoader = (PathClassLoader) classLoader;
                for (String str2 : strArr) {
                    HiLog.info(LABEL, "addForm path:%{public}s", new Object[]{str2});
                    pathClassLoader.addDexPath(str2);
                }
            }
            return contextDeal;
        } catch (PackageManager.NameNotFoundException unused) {
            HiLog.error(LABEL, "createContext cannot find such bundle:%{public}s", new Object[]{str});
            return null;
        }
    }

    private ComponentUtils() {
    }
}
