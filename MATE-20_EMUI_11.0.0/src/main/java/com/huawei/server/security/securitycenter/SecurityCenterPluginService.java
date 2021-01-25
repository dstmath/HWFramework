package com.huawei.server.security.securitycenter;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.internal.os.BackgroundThreadEx;
import com.huawei.securitycenter.SecCenterServiceHolder;
import com.huawei.server.security.core.IHwSecurityPlugin;
import com.huawei.server.security.securitycenter.cache.ModuleData;
import com.huawei.server.security.securitycenter.parsexml.ConfigFileManager;
import huawei.android.security.ISecurityCenterManager;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class SecurityCenterPluginService extends ISecurityCenterManager.Stub implements IHwSecurityPlugin {
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.securitycenter.SecurityCenterPluginService.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            Log.d(SecurityCenterPluginService.TAG, "create SecurityCenterService");
            return new SecurityCenterPluginService(context);
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return SecurityCenterPluginService.SECURITY_CENTER_PERMISSION;
        }
    };
    private static final String DEFAULT_FEATURE = "";
    private static final int INIT_SIZE = 6;
    private static final String KEY_FEATURE = "feature";
    private static final String KEY_METHODS = "methods";
    private static final String KEY_MODULES = "modules";
    private static final String KEY_PACKAGE = "package";
    private static final String SECURITY_CENTER_PERMISSION = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    private static final String TAG = "SecurityCenterPluginService";
    private String mConfigFeature;
    private Context mContext;
    private AtomicBoolean mHasRegisterBinderDie;
    private ConcurrentHashMap<String, ModuleData> mModuleAbility;
    private AtomicBoolean mSecServiceAlive;
    private SecCenterServiceHolder.ServiceDieListener mSecServiceDieListener;

    /* access modifiers changed from: private */
    public class WriteConfigTask implements Runnable {
        private HashMap<String, ModuleData> mCacheData;
        private String mFeature;

        WriteConfigTask(HashMap<String, ModuleData> moduleDataHashMap, String feature) {
            this.mCacheData = moduleDataHashMap;
            this.mFeature = feature;
        }

        @Override // java.lang.Runnable
        public void run() {
            ConfigFileManager.writeConfigData(this.mCacheData, this.mFeature);
        }
    }

    private SecurityCenterPluginService(Context context) {
        this.mModuleAbility = new ConcurrentHashMap<>((int) INIT_SIZE);
        this.mSecServiceAlive = new AtomicBoolean(false);
        this.mHasRegisterBinderDie = new AtomicBoolean(false);
        this.mConfigFeature = "";
        this.mSecServiceDieListener = new SecCenterServiceHolder.ServiceDieListener() {
            /* class com.huawei.server.security.securitycenter.SecurityCenterPluginService.AnonymousClass2 */

            public void notifyServiceDie() {
                if (SecurityCenterPluginService.this.mSecServiceAlive.compareAndSet(true, false)) {
                    Log.i(SecurityCenterPluginService.TAG, "securitycenter service binderDied");
                } else {
                    Log.i(SecurityCenterPluginService.TAG, "securitycenter service already died before");
                }
            }
        };
        this.mContext = context;
        this.mSecServiceAlive.set(true);
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
        Log.i(TAG, "onStart");
        initConfigData();
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
        Log.i(TAG, "onStop");
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.server.security.securitycenter.SecurityCenterPluginService */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        return this;
    }

    public Bundle getSecureAbility(String moduleName, String methodName) {
        registerBinderDieListener();
        if (isStringTrimmedEmpty(moduleName) || isStringTrimmedEmpty(methodName)) {
            Log.w(TAG, "getSecureAbility:arguments is invalid");
            return null;
        } else if (!this.mHasRegisterBinderDie.get() || this.mSecServiceAlive.get()) {
            ModuleData moduleData = this.mModuleAbility.get(moduleName);
            if (moduleData != null) {
                return moduleData.getMethodBundleArgs(methodName);
            }
            Log.i(TAG, "getSecureAbility:no such module:" + moduleName);
            return null;
        } else {
            Log.i(TAG, "getSecureAbility:ability is not available yet");
            return null;
        }
    }

    public synchronized void syncSecureAbility(Bundle abilities) {
        this.mContext.enforceCallingPermission(SECURITY_CENTER_PERMISSION, null);
        registerBinderDieListener();
        if (!checkIfNeedUpdate(abilities, this.mConfigFeature)) {
            Log.i(TAG, "no need to update config");
            return;
        }
        String newFeature = updateAbilities(abilities, this.mModuleAbility);
        if (!"".equals(newFeature)) {
            this.mConfigFeature = newFeature;
            writeConfigData(this.mModuleAbility, newFeature);
        }
    }

    private String updateAbilities(@NonNull Bundle ability, @NonNull ConcurrentHashMap<String, ModuleData> result) {
        List<String> modules = ability.getStringArrayList(KEY_MODULES);
        if (modules == null || modules.isEmpty()) {
            Log.i(TAG, "updateAbilities:empty modules");
            return "";
        }
        HashMap<String, ModuleData> moduleDataHashMap = new HashMap<>(modules.size());
        for (String module : modules) {
            Bundle moduleBundle = ability.getBundle(module);
            if (moduleBundle != null) {
                List<String> methods = moduleBundle.getStringArrayList(KEY_METHODS);
                String packageName = moduleBundle.getString(KEY_PACKAGE);
                if (isStringTrimmedEmpty(packageName) || methods == null) {
                    Log.i(TAG, "updateAbilities:invalid package or method");
                } else {
                    ModuleData moduleData = new ModuleData(module, packageName);
                    for (String method : methods) {
                        Bundle methodArgs = moduleBundle.getBundle(method);
                        if (!(methodArgs == null || methodArgs.size() == 0)) {
                            moduleData.addMethod(method, methodArgs);
                        }
                    }
                    moduleDataHashMap.put(module, moduleData);
                }
            }
        }
        result.clear();
        result.putAll(moduleDataHashMap);
        String newFeature = ability.getString(KEY_FEATURE);
        Log.i(TAG, "updateAbilities success");
        return newFeature;
    }

    private void registerBinderDieListener() {
        boolean z = true;
        if (SecCenterServiceHolder.getHwSecService() != null && this.mHasRegisterBinderDie.compareAndSet(false, true)) {
            SecCenterServiceHolder.addServiceDieListener(this.mSecServiceDieListener, "hwsecservice");
        }
        if (this.mHasRegisterBinderDie.get()) {
            AtomicBoolean atomicBoolean = this.mSecServiceAlive;
            if (SecCenterServiceHolder.getHwSecService() == null) {
                z = false;
            }
            atomicBoolean.set(z);
        }
    }

    private boolean isStringTrimmedEmpty(String string) {
        return string == null || TextUtils.isEmpty(string.trim());
    }

    private boolean checkIfNeedUpdate(Bundle ability, String currentFeature) {
        return ability != null && !TextUtils.isEmpty(ability.getString(KEY_FEATURE)) && !ability.getString(KEY_FEATURE).equals(currentFeature);
    }

    private void initConfigData() {
        synchronized (this) {
            this.mConfigFeature = ConfigFileManager.parseConfigData(this.mModuleAbility);
        }
    }

    private void writeConfigData(@NonNull ConcurrentHashMap<String, ModuleData> data, String targetFeature) {
        BackgroundThreadEx.getHandler().post(new WriteConfigTask(new HashMap<>(data), targetFeature));
    }
}
