package ohos.abilityshell;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import java.util.Optional;
import ohos.abilityshell.utils.AbilityLoader;
import ohos.abilityshell.utils.AbilityShellConverterUtils;
import ohos.abilityshell.utils.IntentConverter;
import ohos.abilityshell.utils.LifecycleState;
import ohos.app.Context;
import ohos.app.ContextDeal;
import ohos.app.dispatcher.threading.AndroidTaskLooper;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.AbilityInfo;
import ohos.bundle.BundleInfo;
import ohos.bundle.ShellInfo;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.tools.Bytrace;

public class AbilityShellServiceDelegate extends AbilityShellDelegate {
    private static final int DEFAULT_BACKGROUND_MODE = 0;
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private Object abilityShell;
    private ContextDeal contextDeal;
    private boolean isLoadAsForm = false;

    public AbilityShellServiceDelegate(Object obj) {
        this.abilityShell = obj;
    }

    public void onCreate() {
        AbilityInfo abilityInfo;
        ShellInfo createShellInfo = createShellInfo(this.abilityShell);
        if (createShellInfo == null) {
            AppLog.e(SHELL_LABEL, "AbilityShellServiceDelegate::onCreate shell info is null!", new Object[0]);
            return;
        }
        HarmonyLoader.waitForLoadHarmony();
        BundleInfo bundleInfo = HarmonyApplication.getInstance().getBundleInfo();
        if (bundleInfo.isDifferentName()) {
            abilityInfo = bundleInfo.getAbilityInfoByOriginalName(createShellInfo.getName());
        } else {
            this.abilityInfo = AbilityShellConverterUtils.convertToAbilityInfo(createShellInfo);
            if (this.abilityInfo == null) {
                AppLog.e(SHELL_LABEL, "AbilityShellServiceDelegate::onCreate could not find ability info from bms, stop start!", new Object[0]);
                return;
            }
            abilityInfo = bundleInfo.getAbilityInfoByName(this.abilityInfo.getClassName());
        }
        if (abilityInfo != null) {
            this.abilityInfo = abilityInfo;
        }
        this.contextDeal = createServiceContextDeal(this.abilityInfo);
        checkHapHasLoaded(this.abilityInfo);
        if (!(this.abilityShell instanceof Service)) {
            return;
        }
        if (AbilityShellConverterUtils.isFormShell(createShellInfo)) {
            AppLog.i(SHELL_LABEL, "AbilityShellServiceDelegate::onCreate is Form type", new Object[0]);
            this.isLoadAsForm = true;
            handleFormServiceAbility(this.abilityInfo, this.contextDeal, this.abilityShell);
            return;
        }
        loadAbility(this.abilityInfo, this.contextDeal, this.abilityShell);
        HarmonyApplication.getInstance().waitForUserApplicationStart();
        scheduleAbilityLifecycle(null, LifecycleState.AbilityState.INACTIVE_STATE.getValue());
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        Optional<ohos.aafwk.content.Intent> createZidaneIntent = IntentConverter.createZidaneIntent(intent, this.abilityInfo);
        if (!createZidaneIntent.isPresent()) {
            AppLog.e("AbilityShellServiceDelegate::onStartCommand createZidaneIntent failed", new Object[0]);
            return 0;
        }
        this.zidaneIntent = createZidaneIntent.get();
        this.ability.scheduleCommand(this.zidaneIntent, i != 0, i2);
        return 0;
    }

    public void onDestroy() {
        if ((this.abilityShell instanceof Service) && !this.isLoadAsForm) {
            scheduleAbilityLifecycle(this.zidaneIntent, LifecycleState.AbilityState.INITIAL_STATE.getValue());
        }
        HarmonyApplication.getInstance().getApplication().removeAbilityRecord(this.abilityShell);
    }

    public IBinder onBind(Intent intent) {
        IRemoteObject iRemoteObject;
        Optional<ohos.aafwk.content.Intent> createZidaneIntent = IntentConverter.createZidaneIntent(intent, this.abilityInfo);
        if (!createZidaneIntent.isPresent()) {
            AppLog.e(SHELL_LABEL, "AbilityShellServiceDelegate::onBind createZidaneIntent failed", new Object[0]);
            return null;
        }
        if (isFlagExists(32, createZidaneIntent.get().getFlags())) {
            iRemoteObject = this.ability.getAbilityFormProvider();
        } else {
            iRemoteObject = this.ability.scheduleConnectAbility(createZidaneIntent.get());
        }
        if (iRemoteObject == null) {
            AppLog.e(SHELL_LABEL, "AbilityShellServiceDelegate::onBind scheduleConnectAbility failed", new Object[0]);
            return null;
        }
        Bytrace.startTrace(2147483648L, "binderConverter_a");
        Optional translateToIBinder = IPCAdapter.translateToIBinder(iRemoteObject);
        Bytrace.finishTrace(2147483648L, "binderConverter_a");
        if (!translateToIBinder.isPresent() || !(translateToIBinder.get() instanceof IBinder)) {
            return null;
        }
        return (IBinder) translateToIBinder.get();
    }

    public boolean onUnbind(Intent intent) {
        Optional<ohos.aafwk.content.Intent> createZidaneIntent = IntentConverter.createZidaneIntent(intent, this.abilityInfo);
        if (!createZidaneIntent.isPresent()) {
            AppLog.e(SHELL_LABEL, "AbilityShellServiceDelegate::onUnbind createZidaneIntent failed", new Object[0]);
            return false;
        } else if (isFlagExists(32, createZidaneIntent.get().getFlags())) {
            AppLog.i(SHELL_LABEL, "AbilityShellServiceDelegate::onUnbind Form type", new Object[0]);
            this.ability.unloadForm();
            HarmonyApplication.getInstance().removeOrSubRef(this.abilityInfo.getClassName());
            return true;
        } else {
            this.ability.scheduleDisconnectAbility(createZidaneIntent.get());
            return true;
        }
    }

    public void onRebind(Intent intent) {
        Optional<ohos.aafwk.content.Intent> createZidaneIntent = IntentConverter.createZidaneIntent(intent, this.abilityInfo);
        if (!createZidaneIntent.isPresent()) {
            AppLog.e(SHELL_LABEL, "AbilityShellServiceDelegate::onRebindIntent createZidaneIntent failed", new Object[0]);
        } else {
            this.ability.onReconnect(createZidaneIntent.get());
        }
    }

    public void onTrimMemory(int i) {
        this.ability.onMemoryLevel(i);
    }

    /* access modifiers changed from: protected */
    public int getBackgroundModes(String str, String str2) {
        AppLog.d(SHELL_LABEL, "getBackgroundModes packageName: %{public}s, name:%{public}s", str, str2);
        if (this.abilityInfo != null) {
            return this.abilityInfo.getBackgroundModes();
        }
        ShellInfo shellInfo = new ShellInfo();
        shellInfo.setPackageName(str);
        shellInfo.setName(str2);
        shellInfo.setType(ShellInfo.ShellType.SERVICE);
        AbilityInfo convertToAbilityInfo = AbilityShellConverterUtils.convertToAbilityInfo(shellInfo);
        if (convertToAbilityInfo == null) {
            AppLog.w(SHELL_LABEL, "AbilityShellServiceDelegate::getBackgroundModes abilityInfo is null", new Object[0]);
            return 0;
        }
        AbilityInfo abilityInfo = this.bundleMgrImpl.getAbilityInfo(convertToAbilityInfo.getBundleName(), convertToAbilityInfo.getClassName());
        if (abilityInfo != null) {
            return abilityInfo.getBackgroundModes();
        }
        AppLog.w(SHELL_LABEL, "AbilityShellServiceDelegate::getBackgroundModes abilityInfo is null", new Object[0]);
        return 0;
    }

    private void handleFormServiceAbility(AbilityInfo abilityInfo, Context context, Object obj) {
        HarmonyApplication instance = HarmonyApplication.getInstance();
        FormAbility formAbility = instance.getFormAbility(abilityInfo.getClassName());
        if (formAbility == null) {
            AppLog.d(SHELL_LABEL, "AbilityShellServiceDelegate::handleFormServiceAbility addAbility", new Object[0]);
            this.ability = new AbilityLoader().setAbilityInfo(abilityInfo).setContext(context).setAbilityShell(obj).loadAbilityAsForm();
            formAbility = new FormAbility(this.ability);
            instance.addFormAbility(abilityInfo.getClassName(), formAbility);
        }
        formAbility.addRefCount();
    }

    private ContextDeal createServiceContextDeal(AbilityInfo abilityInfo) {
        android.content.Context context = (android.content.Context) this.abilityShell;
        ContextDeal contextDeal2 = new ContextDeal(context, context.getClassLoader());
        contextDeal2.setAbilityInfo(abilityInfo);
        contextDeal2.setHapModuleInfo(HarmonyApplication.getInstance().getHapModuleInfoByAbilityInfo(abilityInfo));
        contextDeal2.setApplication(HarmonyApplication.getInstance().getApplication());
        contextDeal2.setMainLooper(new AndroidTaskLooper(Looper.getMainLooper()));
        HarmonyApplication.getInstance().getApplication().addAbilityRecord(this.abilityShell, contextDeal2);
        return contextDeal2;
    }
}
