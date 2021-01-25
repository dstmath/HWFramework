package ohos.app;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import ohos.app.dispatcher.TaskDispatcherContext;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.ApplicationInfo;
import ohos.bundle.BundleInfo;

public class Application extends AbilityContext {
    private final ConcurrentMap<Object, Context> abilityRecords = new ConcurrentHashMap();
    private String appDataPath;
    private ApplicationInfo applicationInfo;
    private BundleInfo bundleInfo;
    private Date createTime;
    private String deviceProtectedPath;
    private final TaskDispatcherContext dispatcherContext = new TaskDispatcherContext();
    private Map<String, Object> harmonyAbilityPackageMap = new ConcurrentHashMap();
    private String localDeviceId = "";
    private Object ohosApplication;
    private ProcessInfo processInfo;
    private Object topAbility;

    public void setBundleInfo(BundleInfo bundleInfo2) {
        this.bundleInfo = bundleInfo2;
        if (bundleInfo2 != null) {
            this.applicationInfo = bundleInfo2.getAppInfo();
        }
    }

    public BundleInfo getBundleInfo() {
        return this.bundleInfo;
    }

    public void setLocalDeviceId(String str) {
        this.localDeviceId = str;
    }

    public String getLocalDeviceId() {
        return this.localDeviceId;
    }

    public void setProcessInfo(ProcessInfo processInfo2) {
        this.processInfo = processInfo2;
    }

    public void setApplicationInfo(ApplicationInfo applicationInfo2) {
        this.applicationInfo = applicationInfo2;
    }

    public void setAppDataPath(String str) {
        this.appDataPath = str;
    }

    public void setDeviceProtectedPath(String str) {
        this.deviceProtectedPath = str;
    }

    public void setAppCreateTime(Date date) {
        this.createTime = new Date(date.getTime());
    }

    @Override // ohos.app.AbilityContext, ohos.app.Context
    public ProcessInfo getProcessInfo() {
        return this.processInfo;
    }

    @Override // ohos.app.AbilityContext, ohos.app.Context
    public ApplicationInfo getApplicationInfo() {
        return this.applicationInfo;
    }

    public String getAppDataPath() {
        return this.appDataPath;
    }

    public String getDeviceProtectedPath() {
        return this.deviceProtectedPath;
    }

    public Date getAppCreateTime() {
        Date date = this.createTime;
        if (date != null) {
            return new Date(date.getTime());
        }
        AppLog.w("Application::getAppCreateTime app createTime not set yet", new Object[0]);
        return new Date();
    }

    public TaskDispatcherContext getTaskDispatcherContext() {
        return this.dispatcherContext;
    }

    public void addAbilityRecord(Object obj, Context context) {
        this.abilityRecords.put(obj, context);
    }

    public void removeAbilityRecord(Object obj) {
        this.abilityRecords.remove(obj);
    }

    public ConcurrentMap<Object, Context> getAbilityRecord() {
        return this.abilityRecords;
    }

    public Object getTopAbility() {
        return this.topAbility;
    }

    public Object getHarmonyosApplication() {
        return this.ohosApplication;
    }

    public void setHarmonyosApplication(Object obj) {
        this.ohosApplication = obj;
    }

    public void setHarmonyosAbilityPackage(String str, Object obj) {
        if (str != null && obj != null) {
            this.harmonyAbilityPackageMap.put(str, obj);
        }
    }

    public Object getAbilityPackage(String str) {
        if (this.harmonyAbilityPackageMap.containsKey(str)) {
            return this.harmonyAbilityPackageMap.get(str);
        }
        return null;
    }

    public void setTopAbility(Object obj) {
        this.topAbility = obj;
    }
}
