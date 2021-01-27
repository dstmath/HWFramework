package com.huawei.server.security.securitycenter.cache;

import android.os.Bundle;
import android.util.Log;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleData {
    private static final int INIT_SIZE = 6;
    private static final String TAG = "ModuleData";
    private ConcurrentHashMap<String, Bundle> mMethod2ArgsMap = new ConcurrentHashMap<>((int) INIT_SIZE);
    private final String mModuleName;
    private final String mPackageName;

    public ModuleData(String moduleName, String packageName) {
        this.mModuleName = moduleName;
        this.mPackageName = packageName;
    }

    public void addMethod(String methodName, Bundle argsBundle) {
        if (!this.mMethod2ArgsMap.containsKey(methodName) && checkArgsValid(argsBundle)) {
            this.mMethod2ArgsMap.put(methodName, argsBundle);
        }
    }

    public boolean updateMethod(String methodName, Bundle argsBundle) {
        if (!this.mMethod2ArgsMap.containsKey(methodName) || !checkArgsValid(argsBundle)) {
            return false;
        }
        this.mMethod2ArgsMap.put(methodName, argsBundle);
        return true;
    }

    public ArrayList<String> getAllMethods() {
        return new ArrayList<>(this.mMethod2ArgsMap.keySet());
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getModuleName() {
        return this.mModuleName;
    }

    public Bundle getMethodBundleArgs(String methodName) {
        Bundle argsMap = this.mMethod2ArgsMap.get(methodName);
        if (checkArgsValid(argsMap)) {
            return argsMap;
        }
        Log.w(TAG, "getMethodBundleArgs:no method:" + methodName + " in " + this.mModuleName);
        return null;
    }

    private boolean checkArgsValid(Bundle argsBundle) {
        return argsBundle != null && !argsBundle.isEmpty();
    }
}
