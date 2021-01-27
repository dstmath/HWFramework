package ohos.abilityshell;

import android.app.Activity;
import android.app.Service;
import android.content.res.Configuration;
import android.os.Handler;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Optional;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.abilityshell.delegation.AbilityDelegator;
import ohos.abilityshell.utils.AbilityLoader;
import ohos.abilityshell.utils.IntentConverter;
import ohos.app.Context;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ShellInfo;
import ohos.global.configuration.DeviceCapability;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.ResourceUtils;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;

public class AbilityShellDelegate {
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    protected Ability ability;
    protected AbilityInfo abilityInfo = null;
    protected final BundleMgrBridge bundleMgrImpl = new BundleMgrBridge();
    protected IRemoteObject distSchedulerHost;
    protected final IDistributedManager distributedImpl = new DistributedImpl();
    protected final Handler handler = new Handler();
    protected Intent zidaneIntent = null;

    /* access modifiers changed from: protected */
    public boolean isFlagExists(int i, int i2) {
        return (i & i2) == i;
    }

    /* access modifiers changed from: protected */
    public Optional<Intent> mapToHarmonyIntent(android.content.Intent intent, AbilityInfo abilityInfo2) {
        Optional<Intent> createZidaneIntent = IntentConverter.createZidaneIntent(intent, abilityInfo2);
        createZidaneIntent.ifPresent($$Lambda$AbilityShellDelegate$HtAsEYwyV7iJnvhIKWNm2urRJeU.INSTANCE);
        return createZidaneIntent;
    }

    static /* synthetic */ void lambda$mapToHarmonyIntent$0(Intent intent) {
        if (intent.getParams() != null && intent.getParams().getClassLoader() == null) {
            intent.getParams().setClassLoader(HarmonyApplication.getInstance().getClassLoader());
        }
    }

    /* access modifiers changed from: protected */
    public Optional<Intent> mapToHarmonyIntent(android.content.Intent intent) {
        return mapToHarmonyIntent(intent, this.abilityInfo);
    }

    /* access modifiers changed from: protected */
    public void loadAbility(AbilityInfo abilityInfo2, Context context, Object obj) {
        this.ability = new AbilityLoader().setAbilityInfo(abilityInfo2).setContext(context).setAbilityShell(obj).loadAbility();
    }

    /* access modifiers changed from: protected */
    public void scheduleAbilityLifecycle(Intent intent, int i) {
        Ability ability2 = this.ability;
        if (ability2 == null) {
            AppLog.e(SHELL_LABEL, "ability is null, schedule lifecycle fail", new Object[0]);
            return;
        }
        try {
            ability2.scheduleAbilityLifecycle(intent, i);
        } catch (IllegalStateException e) {
            if (AbilityDelegator.getInstance().isRunning()) {
                AppLog.w(SHELL_LABEL, "Ignore IllegalStateException while running unittest!", new Object[0]);
                return;
            }
            throw e;
        }
    }

    /* access modifiers changed from: protected */
    public ShellInfo createShellInfo(Object obj) {
        if (!(obj instanceof android.content.Context)) {
            AppLog.e(SHELL_LABEL, "AbilityShellDelegate::createShellInfo shell not Context", new Object[0]);
            return null;
        }
        android.content.Context context = (android.content.Context) obj;
        ShellInfo shellInfo = new ShellInfo();
        shellInfo.setPackageName(context.getPackageName());
        shellInfo.setName(context.getClass().getName());
        if (obj instanceof Activity) {
            shellInfo.setType(ShellInfo.ShellType.ACTIVITY);
        } else if (obj instanceof Service) {
            shellInfo.setType(ShellInfo.ShellType.SERVICE);
        } else {
            shellInfo.setType(ShellInfo.ShellType.UNKNOWN);
        }
        return shellInfo;
    }

    /* access modifiers changed from: protected */
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        if (strArr.length <= 0 || !"-ability".equals(strArr[0])) {
            HarmonyApplication.getInstance().dump(str, fileDescriptor, printWriter, strArr);
            return;
        }
        Ability ability2 = this.ability;
        if (ability2 == null) {
            AppLog.e(SHELL_LABEL, "ability is null, dump fail", new Object[0]);
        } else {
            ability2.dump(str, fileDescriptor, printWriter, strArr);
        }
    }

    /* access modifiers changed from: protected */
    public void checkDmsInterfaceResult(int i, String str) {
        if (i != 0) {
            AppLog.e(SHELL_LABEL, "AbilityShellDelegate::checkDmsInterfaceResult %{private}s failed, result is %{private}d", str, Integer.valueOf(i));
        }
    }

    /* access modifiers changed from: protected */
    public final void checkHapHasLoaded(AbilityInfo abilityInfo2) {
        if (abilityInfo2 == null || abilityInfo2.getModuleName() == null || abilityInfo2.getModuleName().isEmpty()) {
            AppLog.d(SHELL_LABEL, "abilityInfo or muduleInfo is null", new Object[0]);
        } else if (HarmonyApplication.getInstance().getLoadedHapMap().containsKey(abilityInfo2.getModuleName())) {
            HarmonyApplication.getInstance().setCurrentModule(abilityInfo2.getModuleName());
            AppLog.d(SHELL_LABEL, "this module %{public}s, has already been load", abilityInfo2.getModuleName());
        } else {
            new HarmonyLoader(HarmonyApplication.getInstance().getApplicationContext()).loadFeature(abilityInfo2.getModuleName());
        }
    }

    /* access modifiers changed from: protected */
    public void updateConfiguration(Configuration configuration) {
        if (configuration == null) {
            AppLog.d("ContextDeal::updateConfiguration configuration is null", new Object[0]);
        } else if (this.ability == null) {
            AppLog.e(SHELL_LABEL, "ability is null, updateConfiguration fail", new Object[0]);
        } else {
            ohos.global.configuration.Configuration convert = ResourceUtils.convert(configuration);
            DeviceCapability convertToDeviceCapability = ResourceUtils.convertToDeviceCapability(configuration);
            ResourceManager resourceManager = this.ability.getResourceManager();
            if (resourceManager != null) {
                resourceManager.updateConfiguration(convert, convertToDeviceCapability);
            }
            this.ability.onConfigurationChanged(resourceManager.getConfiguration());
            this.ability.onConfigurationUpdated(resourceManager.getConfiguration());
        }
    }
}
