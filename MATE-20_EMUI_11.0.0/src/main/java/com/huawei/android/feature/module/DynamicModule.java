package com.huawei.android.feature.module;

import java.util.List;

public class DynamicModule {
    private DynamicModuleInfo mDynamicModuleInfo;
    private String mModuleName;

    public DynamicModule(String str) {
        this.mModuleName = str;
    }

    public <T> T getClassInstance(String str, List<DynamicParams> list) {
        DynamicModuleInternal dynamicModule = DynamicModuleManager.getInstance().getDynamicModule(this.mModuleName);
        if (dynamicModule == null) {
            return null;
        }
        if (!dynamicModule.isDynamicModuleLoaded()) {
            dynamicModule.install();
        }
        return (T) dynamicModule.getClassInstance(str, list);
    }

    public DynamicModuleInfo getDynamicModuleInfo() {
        if (this.mDynamicModuleInfo == null) {
            DynamicModuleInternal dynamicModule = DynamicModuleManager.getInstance().getDynamicModule(this.mModuleName);
            if (dynamicModule == null) {
                return new DynamicModuleInfo();
            }
            this.mDynamicModuleInfo = dynamicModule.getModuleInfo() == null ? new DynamicModuleInfo() : dynamicModule.getModuleInfo();
        }
        return this.mDynamicModuleInfo;
    }

    public ClassLoader getModuleClassLoader() {
        DynamicModuleInternal dynamicModule = DynamicModuleManager.getInstance().getDynamicModule(this.mModuleName);
        if (dynamicModule == null) {
            return null;
        }
        return dynamicModule.mClassLoader;
    }
}
