package ohos.abilityshell.utils;

import ohos.aafwk.ability.Ability;
import ohos.app.Context;
import ohos.appexecfwk.utils.AppLog;
import ohos.appexecfwk.utils.HiViewUtil;
import ohos.bundle.AbilityInfo;
import ohos.hiviewdfx.HiLogLabel;

public class AbilityLoader {
    private static final int HI_EVENT_ABILITY_CLASS_NOT_FOUND = 2;
    private static final int HI_EVENT_ABILITY_TYPE_UNKONW = 1;
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private AbilityInfo abilityInfo;
    private Context context;
    private Object shell;

    public AbilityLoader setContext(Context context2) {
        this.context = context2;
        return this;
    }

    public AbilityLoader setAbilityInfo(AbilityInfo abilityInfo2) {
        this.abilityInfo = abilityInfo2;
        return this;
    }

    public AbilityLoader setAbilityShell(Object obj) {
        this.shell = obj;
        return this;
    }

    public Ability loadAbility() {
        Ability loadAbilityInner = loadAbilityInner(this.context, this.abilityInfo);
        loadAbilityInner.attachBaseContext(this.context);
        loadAbilityInner.setAbilityShell(this.shell);
        loadAbilityInner.init(this.context, this.abilityInfo);
        return loadAbilityInner;
    }

    public Ability loadAbilityAsForm() {
        AbilityInfo abilityInfo2 = this.abilityInfo;
        if (abilityInfo2 == null || abilityInfo2.getType() == AbilityInfo.AbilityType.PAGE) {
            Ability loadAbilityInner = loadAbilityInner(this.context, this.abilityInfo);
            loadAbilityInner.attachBaseContext(this.context);
            loadAbilityInner.setAbilityShell(this.shell);
            loadAbilityInner.initAsForm(this.context, this.abilityInfo);
            return loadAbilityInner;
        }
        throw new IllegalArgumentException("only page ability can provide AbilityForm");
    }

    private Ability loadAbilityInner(Context context2, AbilityInfo abilityInfo2) {
        if (context2 == null || abilityInfo2 == null) {
            AppLog.e(SHELL_LABEL, "AbilityLoader::loadAbilityInner input argument is null", new Object[0]);
            throw new IllegalArgumentException("loadAbility input argument is null");
        } else if (abilityInfo2.getType() != AbilityInfo.AbilityType.UNKNOWN) {
            String className = abilityInfo2.getClassName();
            AppLog.d(SHELL_LABEL, "AbilityLoader::loadAbilityInner start to load %{public}s.", className);
            ClassLoader classloader = context2.getClassloader();
            if (classloader != null) {
                Ability ability = null;
                try {
                    Object newInstance = classloader.loadClass(className).newInstance();
                    if (newInstance instanceof Ability) {
                        ability = (Ability) newInstance;
                    }
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    AppLog.e(SHELL_LABEL, "AbilityLoader::loadAbilityInner Construct class[%{public}s] error: %{public}s", className, e);
                }
                if (ability != null) {
                    return ability;
                }
                HiViewUtil.sendAbilityEvent(abilityInfo2.getBundleName(), abilityInfo2.getClassName(), 2);
                AppLog.e(SHELL_LABEL, "AbilityLoader::loadAbilityInner class Ability not found", new Object[0]);
                throw new IllegalStateException("failed to loadAbility " + className);
            }
            HiViewUtil.sendAbilityEvent(abilityInfo2.getBundleName(), abilityInfo2.getClassName(), 2);
            AppLog.e(SHELL_LABEL, "AbilityLoader::loadAbilityInner classloader not found", new Object[0]);
            throw new IllegalStateException("class loader not found");
        } else {
            HiViewUtil.sendAbilityEvent(abilityInfo2.getBundleName(), abilityInfo2.getClassName(), 1);
            AppLog.e(SHELL_LABEL, "AbilityLoader::loadAbilityInner input AbilityType is unknown", new Object[0]);
            throw new IllegalArgumentException("loadAbility input AbilityType is unknown");
        }
    }
}
