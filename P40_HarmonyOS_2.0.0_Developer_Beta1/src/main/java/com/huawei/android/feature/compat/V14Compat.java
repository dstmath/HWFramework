package com.huawei.android.feature.compat;

import android.util.Log;
import com.huawei.android.feature.install.IDynamicFeatureInstaller;
import com.huawei.android.feature.utils.ReflectUtils;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class V14Compat implements IDynamicFeatureInstaller {
    private static final String TAG = V14Compat.class.getSimpleName();

    public static int dexInstall(ClassLoader classLoader, File file, File file2, String str) {
        try {
            Object pathList = ReflectUtils.getPathList(classLoader);
            Object[] objArr = (Object[]) ReflectUtils.findField(pathList, "dexElements").get(pathList);
            ArrayList arrayList = new ArrayList();
            for (Object obj : objArr) {
                arrayList.add((File) ReflectUtils.findField(obj, "zip").get(obj));
            }
            if (arrayList.contains(file2)) {
                return 0;
            }
            if (!isDexOptNeeded(file2.getAbsolutePath(), file.getAbsolutePath())) {
                Log.w(TAG, "SplitCompat Should be optimized.");
                return -29;
            }
            ReflectUtils.insertNewElements(pathList, "dexElements", (Object[]) ReflectUtils.findMethodByClassLoader(classLoader, str, List.class, File.class, List.class).invoke(pathList, new ArrayList(Collections.singleton(file2)), file, new ArrayList()));
            return 0;
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            Log.e(TAG, "get dex info failed.");
            return -29;
        } catch (Exception e2) {
            Log.e(TAG, "install dex exception.");
            return -22;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x001d  */
    public static boolean isDexOptNeeded(String str, String str2) {
        Method method;
        String str3 = null;
        try {
            method = ReflectUtils.findMethod("dalvik.system.DexPathList", "optimizedPathFor", File.class, File.class);
            try {
                ReflectUtils.setMethodAccess(method);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "Reflection exception: No such method.");
                if (method == null) {
                }
            } catch (ClassNotFoundException e2) {
                Log.e(TAG, "Reflection exception: Class not found.");
                if (method == null) {
                }
            }
        } catch (NoSuchMethodException e3) {
            method = null;
            Log.e(TAG, "Reflection exception: No such method.");
            if (method == null) {
            }
        } catch (ClassNotFoundException e4) {
            method = null;
            Log.e(TAG, "Reflection exception: Class not found.");
            if (method == null) {
            }
        }
        if (method == null) {
            return false;
        }
        try {
            str3 = (String) method.invoke(null, new File(str), new File(str2));
        } catch (IllegalAccessException e5) {
            Log.e(TAG, "Reflection exception: Illegal access.");
        } catch (InvocationTargetException e6) {
            Log.e(TAG, "Reflection exception: Involve failed.");
        }
        if (str3 == null) {
            return false;
        }
        return !new File(str3).exists();
    }

    @Override // com.huawei.android.feature.install.IDynamicFeatureInstaller
    public int dexInstall(ClassLoader classLoader, File file, File file2) {
        return dexInstall(classLoader, file, file2, "makeDexElements");
    }

    @Override // com.huawei.android.feature.install.IDynamicFeatureInstaller
    public int nativeInstall(ClassLoader classLoader, Set<File> set) {
        if (set.isEmpty()) {
            return -27;
        }
        HashSet hashSet = new HashSet();
        for (File file : set) {
            hashSet.add(file);
        }
        try {
            Object pathList = ReflectUtils.getPathList(classLoader);
            Field findField = ReflectUtils.findField(pathList, "nativeLibraryDirectories");
            hashSet.removeAll(Arrays.asList((File[]) findField.get(pathList)));
            Log.d(TAG, "Adding directories " + hashSet.size());
            findField.set(pathList, hashSet.toArray());
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "install native library exception");
            return -28;
        }
    }
}
