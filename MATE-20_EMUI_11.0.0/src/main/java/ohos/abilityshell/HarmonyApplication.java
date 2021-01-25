package ohos.abilityshell;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Looper;
import android.os.Process;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.AbilityPackage;
import ohos.aafwk.ability.HarmonyosApplication;
import ohos.abilityshell.delegation.AbilityDelegator;
import ohos.app.AbilityContext;
import ohos.app.ContextDeal;
import ohos.app.DumpHelper;
import ohos.app.ProcessInfo;
import ohos.app.dispatcher.threading.AndroidTaskLooper;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.AbilityInfo;
import ohos.bundle.BundleInfo;
import ohos.bundle.HapModuleInfo;
import ohos.eventhandler.EventRunner;
import ohos.global.resource.ResourceUtils;
import ohos.hiviewdfx.HiLogLabel;
import ohos.idn.BasicInfo;
import ohos.idn.DeviceManager;
import ohos.security.keystore.provider.HarmonyKeyStoreProvider;
import ohos.tools.Bytrace;

public class HarmonyApplication extends Application {
    private static final String IPC_JNI_SO_NAME = "ipc_core.z";
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private static String currentModuleName;
    private static HarmonyApplication harmonyApplication = null;
    private static boolean isUserApplicationStarted = false;
    private static Set<Runnable> loadDataAbilityTasks = new HashSet();
    private static Map<String, AbilityPackage> loadedHapMap = new HashMap();
    private static HarmonyosApplication userApplication = new HarmonyosApplication();
    private static CountDownLatch userApplicationLatch = new CountDownLatch(1);
    private Map<String, FormAbility> abilityMap = new HashMap();
    private ohos.app.Application application = new ohos.app.Application();
    private BundleInfo bundleInfo;
    private Date createTime = null;

    static {
        Bytrace.startTrace(2147483648L, "load ipc jni library");
        try {
            System.loadLibrary(IPC_JNI_SO_NAME);
        } catch (UnsatisfiedLinkError unused) {
            AppLog.e("load ipc core lib fail", new Object[0]);
        }
        Bytrace.finishTrace(2147483648L, "load ipc jni library");
    }

    public static HarmonyApplication getInstance() {
        return harmonyApplication;
    }

    public void waitForUserApplicationStart() {
        if (!isUserApplicationStarted) {
            try {
                userApplicationLatch.await();
            } catch (InterruptedException unused) {
                AppLog.e(SHELL_LABEL, "waitForUserApplicationStart InterruptedException occur", new Object[0]);
            }
        }
        if (!isUserApplicationStarted) {
            AppLog.w(SHELL_LABEL, "user application start timeout!", new Object[0]);
        }
    }

    public Map<String, AbilityPackage> getLoadedHapMap() {
        return loadedHapMap;
    }

    public static void registerDataAbility(Runnable runnable) {
        loadDataAbilityTasks.add(runnable);
    }

    /* access modifiers changed from: protected */
    @Override // android.content.ContextWrapper
    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        setHarmonyApplication(this);
        new Thread(new HarmonyLoader(context), "tryLoadHarmony").start();
    }

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        EventRunner.setMainEventRunner();
        Thread thread = new Thread(new Runnable() {
            /* class ohos.abilityshell.$$Lambda$HarmonyApplication$gIv5hhTqWFQ8P4U54EjJ7lD_Y */

            @Override // java.lang.Runnable
            public final void run() {
                HarmonyApplication.this.lambda$onCreate$0$HarmonyApplication();
            }
        }, "createApplication");
        thread.setUncaughtExceptionHandler($$Lambda$HarmonyApplication$5NTwYAzdS0pI1X2mLZwWtDp1M.INSTANCE);
        thread.start();
    }

    public /* synthetic */ void lambda$onCreate$0$HarmonyApplication() {
        Bytrace.startTrace(2147483648L, "Applicaion on Create");
        this.createTime = new Date();
        Bytrace.startTrace(2147483648L, "KeyStore install");
        HarmonyKeyStoreProvider.install();
        Bytrace.finishTrace(2147483648L, "KeyStore install");
        setApplicationEnv();
        for (Runnable runnable : loadDataAbilityTasks) {
            runnable.run();
        }
        AbilityDelegator.getInstance().setClassLoader(getClassLoader());
        Bytrace.finishTrace(2147483648L, "Application on Create");
    }

    @Override // android.app.Application
    public void onTerminate() {
        super.onTerminate();
        waitForUserApplicationStart();
        userApplication.onTerminate();
        if (loadedHapMap.containsKey(currentModuleName)) {
            loadedHapMap.get(currentModuleName).onEnd();
        }
    }

    @Override // android.app.Application, android.content.ComponentCallbacks2
    public void onTrimMemory(int i) {
        super.onTrimMemory(i);
        waitForUserApplicationStart();
        userApplication.memoryLevelChange(i);
        if (loadedHapMap.containsKey(currentModuleName)) {
            loadedHapMap.get(currentModuleName).memoryLevelChange(i);
        }
    }

    @Override // android.app.Application, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        waitForUserApplicationStart();
        ohos.global.configuration.Configuration convert = ResourceUtils.convert(configuration);
        userApplication.configurationChanged(convert);
        if (loadedHapMap.containsKey(currentModuleName)) {
            loadedHapMap.get(currentModuleName).configurationChanged(convert);
        }
    }

    public final ohos.app.Application getApplication() {
        return this.application;
    }

    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        new DumpHelper(this.application).dump(str, fileDescriptor, printWriter, strArr);
    }

    public void setBundleInfo(BundleInfo bundleInfo2) {
        this.bundleInfo = bundleInfo2;
    }

    public BundleInfo getBundleInfo() {
        return this.bundleInfo;
    }

    public void setCurrentModule(String str) {
        currentModuleName = str;
    }

    public FormAbility getFormAbility(String str) {
        return this.abilityMap.get(str);
    }

    public void addFormAbility(String str, FormAbility formAbility) {
        this.abilityMap.put(str, formAbility);
    }

    public void removeOrSubRef(String str) {
        FormAbility formAbility = getFormAbility(str);
        if (formAbility == null) {
            AppLog.w(SHELL_LABEL, "find %{private}s failed", str);
        } else if (formAbility.getRefCount() == 1) {
            this.abilityMap.remove(str);
        } else {
            formAbility.subRefCount();
        }
    }

    private void setHarmonyApplication(HarmonyApplication harmonyApplication2) {
        harmonyApplication = harmonyApplication2;
    }

    private void setApplicationEnv() {
        ContextDeal contextDeal = new ContextDeal(getApplicationContext(), getClassLoader());
        this.application.attachBaseContext(contextDeal);
        this.application.setAppCreateTime(this.createTime);
        contextDeal.setApplication(this.application);
        contextDeal.setMainLooper(new AndroidTaskLooper(Looper.getMainLooper()));
        new DeviceManager().getLocalNodeBasicInfo().ifPresent(new Consumer() {
            /* class ohos.abilityshell.$$Lambda$HarmonyApplication$MMEepGuX_2y4efWHtR57aPRynqc */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                HarmonyApplication.this.lambda$setApplicationEnv$2$HarmonyApplication((BasicInfo) obj);
            }
        });
        this.application.setProcessInfo(new ProcessInfo(getApplicationInfo().processName, Process.myPid()));
        HarmonyLoader.waitForLoadHarmony();
        setAppDataPath();
        BundleInfo bundleInfo2 = this.bundleInfo;
        if (bundleInfo2 != null) {
            this.application.setBundleInfo(bundleInfo2);
            contextDeal.initApplicationResourceManager(this.bundleInfo.name);
            createUserApplication(this.bundleInfo.getEntryModuleName());
            isUserApplicationStarted = true;
            userApplicationLatch.countDown();
        }
    }

    public /* synthetic */ void lambda$setApplicationEnv$2$HarmonyApplication(BasicInfo basicInfo) {
        this.application.setLocalDeviceId(basicInfo.getNodeId());
    }

    public void createUserApplication(String str) {
        setUserApplication(str);
        Bytrace.startTrace(2147483648L, "userApplication onStart");
        userApplication.onStart();
        if (loadedHapMap.containsKey(currentModuleName)) {
            loadedHapMap.get(currentModuleName).onInitialize();
        }
        Bytrace.finishTrace(2147483648L, "userApplication onStart");
    }

    public void loadClass(String str) {
        HapModuleInfo hapModuleInfo = this.bundleInfo.getHapModuleInfo(str);
        if (hapModuleInfo == null || hapModuleInfo.getName() == null) {
            AppLog.w(SHELL_LABEL, "hap moduleInfo is null", new Object[0]);
            return;
        }
        String name = hapModuleInfo.getName();
        if (loadedHapMap.containsKey(str)) {
            AppLog.w(SHELL_LABEL, "this module %{public}s has been load", name);
            return;
        }
        try {
            Object newInstance = getClassLoader().loadClass(name).newInstance();
            if (newInstance instanceof AbilityPackage) {
                loadedHapMap.put(str, (AbilityPackage) newInstance);
                attachHapModuleContext((AbilityPackage) newInstance, hapModuleInfo);
                currentModuleName = str;
                this.application.setHarmonyosAbilityPackage(str, newInstance);
                ((AbilityPackage) newInstance).onInitialize();
            }
        } catch (ClassNotFoundException unused) {
            loadedHapMap.put(str, new AbilityPackage());
            AppLog.e(SHELL_LABEL, "class not found exception %{public}s", name);
        } catch (IllegalAccessException | InstantiationException unused2) {
            AppLog.e(SHELL_LABEL, "newInstance failed", new Object[0]);
        }
    }

    private void setUserApplication(String str) {
        HapModuleInfo hapModuleInfo = this.bundleInfo.getHapModuleInfo(str);
        if (hapModuleInfo == null || hapModuleInfo.getName() == null) {
            AppLog.w(SHELL_LABEL, "entry hap moduleInfo is null", new Object[0]);
            return;
        }
        String name = hapModuleInfo.getName();
        try {
            Object newInstance = getClassLoader().loadClass(name).newInstance();
            if (newInstance instanceof HarmonyosApplication) {
                userApplication = (HarmonyosApplication) newInstance;
                attachHapModuleContext(userApplication, null);
                this.application.setHarmonyosApplication(newInstance);
                loadedHapMap.put(str, new AbilityPackage());
                this.application.setHarmonyosAbilityPackage(str, new AbilityPackage());
            } else if (newInstance instanceof AbilityPackage) {
                loadedHapMap.put(str, (AbilityPackage) newInstance);
                attachHapModuleContext((AbilityPackage) newInstance, hapModuleInfo);
                currentModuleName = str;
                this.application.setHarmonyosAbilityPackage(str, newInstance);
                this.application.setHarmonyosApplication(new HarmonyosApplication());
            }
        } catch (ClassNotFoundException unused) {
            this.application.setHarmonyosApplication(new HarmonyosApplication());
            loadedHapMap.put(str, new AbilityPackage());
            this.application.setHarmonyosAbilityPackage(str, new AbilityPackage());
            AppLog.e(SHELL_LABEL, "class not found exception %{public}s", name);
        } catch (IllegalAccessException | InstantiationException unused2) {
            AppLog.e(SHELL_LABEL, "newInstance failed", new Object[0]);
        }
    }

    private void attachHapModuleContext(AbilityContext abilityContext, HapModuleInfo hapModuleInfo) {
        if (abilityContext != null) {
            ContextDeal contextDeal = new ContextDeal(getApplicationContext(), getClassLoader());
            contextDeal.setMainLooper(new AndroidTaskLooper(Looper.getMainLooper()));
            contextDeal.setApplication(this.application);
            if (abilityContext instanceof AbilityPackage) {
                contextDeal.setHapModuleInfo(hapModuleInfo);
            }
            abilityContext.attachBaseContext(contextDeal);
        }
    }

    private void setAppDataPath() {
        String str;
        String str2 = null;
        try {
            str = getInstance().getApplicationContext().getDataDir().getCanonicalPath();
            try {
                str2 = getInstance().getApplicationContext().createDeviceProtectedStorageContext().getDataDir().getCanonicalPath();
            } catch (IOException unused) {
            }
        } catch (IOException unused2) {
            str = null;
            AppLog.e(SHELL_LABEL, "fail to get dataDir", new Object[0]);
            this.application.setAppDataPath(str);
            this.application.setDeviceProtectedPath(str2);
        }
        this.application.setAppDataPath(str);
        this.application.setDeviceProtectedPath(str2);
    }

    public Ability getTopAbility() {
        ohos.app.Application application2 = this.application;
        if (application2 == null) {
            return null;
        }
        Object topAbility = application2.getTopAbility();
        if (topAbility instanceof Ability) {
            return (Ability) topAbility;
        }
        return null;
    }

    public void setTopAbility(Ability ability) {
        ohos.app.Application application2 = this.application;
        if (application2 != null) {
            application2.setTopAbility(ability);
        }
    }

    public HapModuleInfo getHapModuleInfoByAbilityInfo(AbilityInfo abilityInfo) {
        if (abilityInfo != null && abilityInfo.getModuleName() != null) {
            return this.bundleInfo.getHapModuleInfo(abilityInfo.getModuleName());
        }
        AppLog.w(SHELL_LABEL, "AbilityInfo or moduleInfo is null", new Object[0]);
        return null;
    }
}
