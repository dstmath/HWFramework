package com.huawei.android.feature.install.isolated;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import com.huawei.android.feature.install.InstallStorageManager;
import com.huawei.android.feature.module.DynamicModuleInfo;
import com.huawei.android.feature.module.DynamicModuleInternal;
import com.huawei.android.feature.module.DynamicModuleManager;
import com.huawei.android.feature.utils.ReflectUtils;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class IsolatedDynamicModule extends DynamicModuleInternal {
    private static final String TAG = IsolatedDynamicModule.class.getSimpleName();

    public IsolatedDynamicModule(Context context, DynamicModuleInfo dynamicModuleInfo) {
        super(context, dynamicModuleInfo);
    }

    private static boolean installResource(Context context, String str) {
        AssetManager assets = context.getAssets();
        Log.d("debugresource", "begin to add dynamic asset path");
        try {
            Method declaredMethod = assets.getClass().getDeclaredMethod("addAssetPath", String.class);
            Log.d("debugresource", "set method of addAssetPath accessible");
            declaredMethod.setAccessible(true);
            try {
                int intValue = ((Integer) declaredMethod.invoke(assets, str)).intValue();
                Log.d("debugresource", "end of adding dynamic asset path");
                Log.d(TAG, "cookie = ".concat(String.valueOf(intValue)));
                return intValue != 0;
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                return false;
            }
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, e2.toString());
            return false;
        }
    }

    public static boolean installResourceV9(Context context, String str) {
        try {
            Method declaredMethod = Class.forName("android.content.res.ApkAssets").getDeclaredMethod("loadFromPath", String.class);
            Method declaredMethod2 = AssetManager.class.getDeclaredMethod("getApkAssets", new Class[0]);
            Method declaredMethod3 = AssetManager.class.getDeclaredMethod("setApkAssets", Array.newInstance(Class.forName("android.content.res.ApkAssets"), 2).getClass(), Boolean.TYPE);
            ReflectUtils.setMethodAccess(declaredMethod);
            ReflectUtils.setMethodAccess(declaredMethod2);
            ReflectUtils.setMethodAccess(declaredMethod3);
            AssetManager assets = context.getAssets();
            try {
                Object invoke = declaredMethod.invoke(null, str);
                Object[] objArr = (Object[]) declaredMethod2.invoke(assets, new Object[0]);
                Object[] objArr2 = (Object[]) Array.newInstance(Class.forName("android.content.res.ApkAssets"), objArr.length + 1);
                System.arraycopy(objArr, 0, objArr2, 0, objArr.length);
                objArr2[objArr.length] = invoke;
                declaredMethod3.invoke(assets, objArr2, Boolean.FALSE);
                return true;
            } catch (ClassNotFoundException e) {
                Log.e(TAG, e.toString());
                return false;
            } catch (IllegalAccessException e2) {
                Log.e(TAG, e2.toString());
                return false;
            } catch (InvocationTargetException e3) {
                Log.e(TAG, e3.toString());
                return false;
            }
        } catch (ClassNotFoundException e4) {
            Log.e(TAG, e4.toString());
            return false;
        } catch (NoSuchMethodException e5) {
            Log.e(TAG, e5.toString());
            return false;
        }
    }

    public static boolean installResources(Context context, String str) {
        DynamicModuleInternal dynamicModule = DynamicModuleManager.getInstance().getDynamicModule(str);
        if (dynamicModule != null) {
            return installResource(context, InstallStorageManager.getIsolatedModuleDir(context, str).getAbsolutePath() + File.separator + str + dynamicModule.getModuleInfo().mSuffix);
        }
        Log.e(TAG, "the module to install resource not found in installed list");
        return false;
    }

    @Override // com.huawei.android.feature.module.DynamicModuleInternal
    public int install() {
        Log.d(TAG, "install");
        ClassLoader classLoader = IsolatedDynamicModule.class.getClassLoader();
        if (classLoader == null) {
            return -21;
        }
        this.mClassLoader = new IsolationClassLoader(this.mModuleInfo.mApkPath, this.mModuleInfo.mDexDir, this.mModuleInfo.mNativeLibDir, classLoader.getParent());
        Log.d(TAG, "install classloader end");
        int extractNativeLibrary = extractNativeLibrary(new File(this.mModuleInfo.mApkPath));
        Log.d(TAG, TAG + "full installFeatureFromUnverifyIfNeed end, errcode:" + extractNativeLibrary);
        return extractNativeLibrary;
    }

    @Override // com.huawei.android.feature.module.DynamicModuleInternal
    public int install(boolean z) {
        int i = 0;
        Log.d(TAG, "install isOverrideOrigin".concat(String.valueOf(z)));
        ClassLoader classLoader = IsolatedDynamicModule.class.getClassLoader();
        if (classLoader == null) {
            return -21;
        }
        this.mClassLoader = new IsolationClassLoader(this.mModuleInfo.mApkPath, this.mModuleInfo.mDexDir, this.mModuleInfo.mNativeLibDir, classLoader.getParent());
        Log.d(TAG, "install classloader end");
        if (z) {
            i = extractNativeLibrary(new File(this.mModuleInfo.mApkPath));
        }
        Log.d(TAG, TAG + "full installFeatureFromUnverifyIfNeed end");
        return i;
    }
}
