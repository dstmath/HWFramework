package ohos.abilityshell;

import ohos.aafwk.ability.Ability;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;

public class FormAbility {
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private final Ability ability;
    private int refCount = 0;

    public FormAbility(Ability ability2) {
        this.ability = ability2;
    }

    public final Ability getAbility() {
        return this.ability;
    }

    public int getRefCount() {
        return this.refCount;
    }

    public void addRefCount() {
        this.refCount++;
        AppLog.d(SHELL_LABEL, "FormAbilityController::addRefCount %{public}d", Integer.valueOf(this.refCount));
    }

    public void subRefCount() {
        int i = this.refCount;
        if (i == 0) {
            AppLog.e(SHELL_LABEL, "FormAbilityController::subRefCount refCount invalid", new Object[0]);
            return;
        }
        this.refCount = i - 1;
        AppLog.d(SHELL_LABEL, "FormAbilityController::subRefCount %{public}d", Integer.valueOf(this.refCount));
    }
}
