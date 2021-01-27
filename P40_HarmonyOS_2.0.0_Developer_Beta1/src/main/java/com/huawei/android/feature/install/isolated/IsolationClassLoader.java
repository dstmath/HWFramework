package com.huawei.android.feature.install.isolated;

import dalvik.system.DexClassLoader;

public class IsolationClassLoader extends DexClassLoader {
    public IsolationClassLoader(String str, String str2, String str3, ClassLoader classLoader) {
        super(str, str2, str3, classLoader);
    }

    @Override // java.lang.ClassLoader
    public Class<?> loadClass(String str) {
        try {
            return super.loadClass(str);
        } catch (ClassNotFoundException e) {
            return IsolationClassLoader.class.getClassLoader().loadClass(str);
        }
    }
}
