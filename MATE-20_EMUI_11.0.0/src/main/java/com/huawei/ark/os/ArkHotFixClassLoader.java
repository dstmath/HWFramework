package com.huawei.ark.os;

import com.huawei.ark.classloader.ExtendedClassLoaderHelper;

public class ArkHotFixClassLoader {
    public static boolean applyPatch(ClassLoader loader, String path) {
        return ExtendedClassLoaderHelper.applyPatchByParentClassLoader(loader, path);
    }
}
