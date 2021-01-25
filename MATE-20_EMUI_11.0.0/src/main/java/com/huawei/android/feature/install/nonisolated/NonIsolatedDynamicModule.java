package com.huawei.android.feature.install.nonisolated;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;
import com.huawei.android.feature.install.InstallStorageManager;
import com.huawei.android.feature.module.DynamicModuleInfo;
import com.huawei.android.feature.module.DynamicModuleInternal;
import com.huawei.android.feature.utils.ReflectUtils;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class NonIsolatedDynamicModule extends DynamicModuleInternal {
    private static final String TAG = NonIsolatedDynamicModule.class.getSimpleName();

    public NonIsolatedDynamicModule(Context context, DynamicModuleInfo dynamicModuleInfo) {
        super(context, dynamicModuleInfo);
    }

    private static Object combineArray(Object obj, Object obj2) {
        Class<?> componentType = obj.getClass().getComponentType();
        int length = Array.getLength(obj);
        int length2 = Array.getLength(obj2) + length;
        Object newInstance = Array.newInstance(componentType, length2);
        for (int i = 0; i < length2; i++) {
            if (i < length) {
                Array.set(newInstance, i, Array.get(obj, i));
            } else {
                Array.set(newInstance, i, Array.get(obj2, i - length));
            }
        }
        return newInstance;
    }

    private static Object getDexElements(Object obj) {
        return getField(obj.getClass(), obj, "dexElements");
    }

    public static Object getField(Class cls, Object obj, String str) {
        Field declaredField = cls.getDeclaredField(str);
        ReflectUtils.setFieldAccess(declaredField);
        return declaredField.get(obj);
    }

    private PathClassLoader getPathClassLoader() {
        return (PathClassLoader) NonIsolatedDynamicModule.class.getClassLoader();
    }

    private static Object getPathList(Object obj) {
        return getField(Class.forName("dalvik.system.BaseDexClassLoader"), obj, "pathList");
    }

    private void installlibs(DexClassLoader dexClassLoader) {
        int i = 0;
        Object pathList = getPathList(getPathClassLoader());
        if (Build.VERSION.SDK_INT > 22) {
            ((List) getField(pathList.getClass(), pathList, "nativeLibraryDirectories")).add(new File(this.mModuleInfo.mNativeLibDir));
            Object field = getField(pathList.getClass(), pathList, "nativeLibraryPathElements");
            int length = Array.getLength(field);
            Object pathList2 = getPathList(dexClassLoader);
            Object field2 = getField(pathList2.getClass(), pathList2, "nativeLibraryPathElements");
            Class<?> componentType = field2.getClass().getComponentType();
            if (componentType != null) {
                Object newInstance = Array.newInstance(componentType, length + 1);
                System.arraycopy(field, 0, newInstance, 0, length);
                Field declaredField = Build.VERSION.SDK_INT >= 26 ? componentType.getDeclaredField("path") : componentType.getDeclaredField("dir");
                ReflectUtils.setFieldAccess(declaredField);
                int length2 = Array.getLength(field2);
                while (true) {
                    if (i >= length2) {
                        break;
                    }
                    Object obj = Array.get(field2, i);
                    if (((File) declaredField.get(obj)).getAbsolutePath().contains(InstallStorageManager.LIBS_DIR)) {
                        Array.set(newInstance, length, obj);
                        break;
                    }
                    i++;
                }
                setField(pathList.getClass(), pathList, "nativeLibraryPathElements", newInstance);
                return;
            }
            return;
        }
        File[] fileArr = (File[]) getField(pathList.getClass(), pathList, "nativeLibraryDirectories");
        File[] fileArr2 = new File[(fileArr.length + 1)];
        System.arraycopy(fileArr, 0, fileArr2, 0, fileArr.length);
        fileArr2[fileArr.length] = new File(this.mModuleInfo.mNativeLibDir);
        setField(pathList.getClass(), pathList, "nativeLibraryDirectories", fileArr2);
    }

    private Object invoke(Class cls, Object obj, String str, Object... objArr) {
        Class<?>[] clsArr = null;
        if (objArr != null) {
            Class<?>[] clsArr2 = new Class[objArr.length];
            for (int i = 0; i < objArr.length; i++) {
                clsArr2[i] = objArr[i].getClass();
            }
            clsArr = clsArr2;
        }
        Method declaredMethod = cls.getDeclaredMethod(str, clsArr);
        ReflectUtils.setMethodAccess(declaredMethod);
        return declaredMethod.invoke(obj, objArr);
    }

    private Object loadBaseDex() {
        return getDexElements(getPathList(getPathClassLoader()));
    }

    private Object loadFeatureDex(DexClassLoader dexClassLoader) {
        return getDexElements(getPathList(dexClassLoader));
    }

    @Override // com.huawei.android.feature.module.DynamicModuleInternal
    public int install() {
        Log.d(TAG, "install begin");
        this.mClassLoader = NonIsolatedDynamicModule.class.getClassLoader();
        int extractNativeLibrary = extractNativeLibrary(new File(this.mModuleInfo.mApkPath));
        Log.d(TAG, "extractNativeLibrary end. errcode:".concat(String.valueOf(extractNativeLibrary)));
        if (extractNativeLibrary != 0) {
            return extractNativeLibrary;
        }
        DexClassLoader dexClassLoader = new DexClassLoader(this.mModuleInfo.mApkPath, this.mModuleInfo.mDexDir, this.mModuleInfo.mNativeLibDir, NonIsolatedDynamicModule.class.getClassLoader());
        Log.d(TAG, "new dexloader end");
        try {
            Object pathList = getPathList(getPathClassLoader());
            setField(pathList.getClass(), pathList, "dexElements", combineArray(loadBaseDex(), loadFeatureDex(dexClassLoader)));
            Log.d(TAG, "install dex finish");
            try {
                installlibs(dexClassLoader);
                Log.d(TAG, "install libs finish");
                try {
                    invoke(AssetManager.class, this.mContext.getAssets(), "addAssetPath", this.mModuleInfo.mApkPath);
                    Log.d(TAG, "install addassertpath finish");
                    return extractNativeLibrary;
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    Log.d(TAG, "install addassertpath exception");
                    return -23;
                }
            } catch (Exception e2) {
                Log.e(TAG, e2.getMessage());
                Log.d(TAG, "install libs exception");
                return -24;
            }
        } catch (Exception e3) {
            Log.e(TAG, e3.getMessage());
            Log.d(TAG, "install dex exception");
            return -22;
        }
    }

    @Override // com.huawei.android.feature.module.DynamicModuleInternal
    public int install(boolean z) {
        return install();
    }

    public void setField(Class cls, Object obj, String str, Object obj2) {
        Field declaredField = cls.getDeclaredField(str);
        ReflectUtils.setFieldAccess(declaredField);
        declaredField.set(obj, obj2);
    }
}
