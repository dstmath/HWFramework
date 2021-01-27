package com.huawei.android.feature.compat;

import android.util.Log;
import com.huawei.android.feature.install.IDynamicFeatureInstaller;
import com.huawei.android.feature.utils.ReflectUtils;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class V26Compat implements IDynamicFeatureInstaller {
    private static final String TAG = V26Compat.class.getSimpleName();

    public static boolean isDexOptNeeded(String str) {
        Class<?> cls;
        Method method = null;
        try {
            cls = Class.forName("dalvik.system.DexFile");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Reflection exception: Class not found.");
            cls = null;
        }
        if (cls == null) {
            return false;
        }
        try {
            method = cls.getMethod("isDexOptNeeded", String.class);
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "Reflection exception: No such method.");
        }
        if (method == null) {
            return false;
        }
        try {
            return ((Boolean) method.invoke(null, str)).booleanValue();
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "Reflection exception: Illegal access.");
        } catch (InvocationTargetException e4) {
            Log.e(TAG, "Reflection exception: Involve failed.");
        }
        return false;
    }

    @Override // com.huawei.android.feature.install.IDynamicFeatureInstaller
    public int dexInstall(ClassLoader classLoader, File file, File file2) {
        try {
            Object pathList = ReflectUtils.getPathList(classLoader);
            Object[] objArr = (Object[]) ReflectUtils.findField(pathList, "dexElements").get(pathList);
            ArrayList arrayList = new ArrayList();
            for (Object obj : objArr) {
                arrayList.add((File) ReflectUtils.findField(obj, "path").get(obj));
            }
            if (arrayList.contains(file2)) {
                return 0;
            }
            if (!isDexOptNeeded(file2.getAbsolutePath())) {
                return -29;
            }
            ReflectUtils.insertNewElements(pathList, "dexElements", (Object[]) ReflectUtils.findMethodByClassLoader(classLoader, "makePathElements", List.class, File.class, List.class).invoke(pathList, new ArrayList(Collections.singleton(file2)), file, new ArrayList()));
            return 0;
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            Log.e(TAG, "get dex info failed.");
            return -29;
        } catch (Exception e2) {
            Log.e(TAG, "install dex exception.");
            return -22;
        }
    }

    @Override // com.huawei.android.feature.install.IDynamicFeatureInstaller
    public int nativeInstall(ClassLoader classLoader, Set<File> set) {
        if (set.isEmpty()) {
            return -27;
        }
        ArrayList arrayList = new ArrayList();
        for (File file : set) {
            arrayList.add(file);
        }
        try {
            Object pathList = ReflectUtils.getPathList(classLoader);
            List list = (List) ReflectUtils.findField(pathList, "nativeLibraryDirectories").get(pathList);
            arrayList.removeAll(list);
            list.addAll(arrayList);
            ReflectUtils.findField(pathList, "nativeLibraryPathElements").set(pathList, (Object[]) ReflectUtils.findMethodByClassLoader(classLoader, "makePathElements", List.class).invoke(pathList, arrayList));
            return 0;
        } catch (Exception e) {
            Log.d(TAG, "install native library exception");
            return -28;
        }
    }
}
