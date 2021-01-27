package com.huawei.android.feature.compat;

import android.util.Log;
import com.huawei.android.feature.install.IDynamicFeatureInstaller;
import com.huawei.android.feature.utils.ReflectUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class V23Compat implements IDynamicFeatureInstaller {
    private static final String TAG = V23Compat.class.getSimpleName();

    public static boolean isDexOptNeeded(String str, String str2) {
        return V14Compat.isDexOptNeeded(str, str2);
    }

    @Override // com.huawei.android.feature.install.IDynamicFeatureInstaller
    public int dexInstall(ClassLoader classLoader, File file, File file2) {
        return V14Compat.dexInstall(classLoader, file, file2, "makePathElements");
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
            ArrayList arrayList2 = new ArrayList();
            ReflectUtils.findField(pathList, "nativeLibraryPathElements").set(pathList, (Object[]) ReflectUtils.findMethodByClassLoader(classLoader, "makePathElements", List.class, File.class, List.class).invoke(pathList, arrayList, null, arrayList2));
            return 0;
        } catch (Exception e) {
            Log.d(TAG, "install native library exception");
            return -28;
        }
    }
}
