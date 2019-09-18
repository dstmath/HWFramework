package com.huawei.android.feature.install.isolated;

import android.content.Context;
import android.util.Log;
import com.huawei.android.feature.module.DynamicModuleInfo;
import com.huawei.android.feature.module.DynamicModuleInternal;
import java.io.File;

public class IsolatedDynamicModule extends DynamicModuleInternal {
    private static final String TAG = IsolatedDynamicModule.class.getSimpleName();

    public IsolatedDynamicModule(Context context, DynamicModuleInfo dynamicModuleInfo) {
        super(context, dynamicModuleInfo);
    }

    public int install() {
        Log.d(TAG, "install");
        ClassLoader classLoader = IsolatedDynamicModule.class.getClassLoader();
        if (classLoader == null) {
            return -21;
        }
        this.mClassLoader = new IsolationClassLoader(this.mModuleInfo.mApkPath, this.mModuleInfo.mDexDir, this.mModuleInfo.mNativeLibDir, classLoader.getParent());
        Log.d(TAG, "install classloader end");
        int extractNatvieLibrary = extractNatvieLibrary(new File(this.mModuleInfo.mApkPath));
        Log.d(TAG, TAG + "full installFeatureFromUnverifyIfNeed end, errcode:" + extractNatvieLibrary);
        return extractNatvieLibrary;
    }

    public int install(boolean z) {
        int i = 0;
        Log.d(TAG, "install isOverrideOrigin" + z);
        ClassLoader classLoader = IsolatedDynamicModule.class.getClassLoader();
        if (classLoader == null) {
            return -21;
        }
        this.mClassLoader = new IsolationClassLoader(this.mModuleInfo.mApkPath, this.mModuleInfo.mDexDir, this.mModuleInfo.mNativeLibDir, classLoader.getParent());
        Log.d(TAG, "install classloader end");
        if (z) {
            i = extractNatvieLibrary(new File(this.mModuleInfo.mApkPath));
        }
        Log.d(TAG, TAG + "full installFeatureFromUnverifyIfNeed end");
        return i;
    }
}
