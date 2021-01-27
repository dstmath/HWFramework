package ohos.abilityshell.delegation;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.delegation.IAbilityMonitor;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Skills;
import ohos.appexecfwk.utils.AppLog;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.hiviewdfx.HiLogLabel;

public class AbilityMonitor implements IAbilityMonitor {
    private static final long MAX_WAIT_TIME = 5000;
    private static final HiLogLabel MONITOR_LABEL = new HiLogLabel(3, 218108160, "AbilityMonitor");
    private final BlockingQueue<Ability> abilities;
    private final String abilityClass;
    private final Skills skills;

    public AbilityMonitor(String str) {
        this.abilities = new ArrayBlockingQueue(1);
        this.skills = null;
        this.abilityClass = str;
    }

    public AbilityMonitor(Skills skills2) {
        this.abilities = new ArrayBlockingQueue(1);
        this.skills = skills2;
        this.abilityClass = null;
    }

    public String getAbilityClass() {
        return this.abilityClass;
    }

    public final Ability getAbility() {
        return this.abilities.peek();
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityMonitor
    public final Ability waitForAbility() {
        return waitForAbility(MAX_WAIT_TIME);
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityMonitor
    public final Ability waitForAbility(long j) {
        try {
            return this.abilities.poll(j, TimeUnit.MILLISECONDS);
        } catch (InterruptedException unused) {
            AppLog.d(MONITOR_LABEL, "Meet InterruptedException.", new Object[0]);
            return null;
        }
    }

    public final boolean match(Ability ability, Intent intent) {
        boolean z;
        String str;
        String str2;
        Skills skills2 = this.skills;
        if (skills2 == null || !skills2.match(intent)) {
            z = false;
        } else {
            AppLog.d(MONITOR_LABEL, "Skills matched", new Object[0]);
            z = true;
        }
        if (this.abilityClass != null && !z) {
            if (intent != null) {
                str2 = intent.getElement().getAbilityName();
                str = intent.getElement().getBundleName();
            } else if (ability != null) {
                str2 = ability.getClass().getName();
                str = ability.getBundleName();
            } else {
                AppLog.e("match failed since null input.", new Object[0]);
                return false;
            }
            AppLog.d(MONITOR_LABEL, "match %{public}s/%{public}s with %{public}s", str, str2, this.abilityClass);
            if (!(str2 == null || str == null)) {
                if (this.abilityClass.contains(PsuedoNames.PSEUDONAME_ROOT)) {
                    z = (str + PsuedoNames.PSEUDONAME_ROOT + str2).equals(this.abilityClass);
                } else {
                    z = str2.equals(this.abilityClass);
                }
            }
        }
        AppLog.d(MONITOR_LABEL, "match result is %{public}s", Boolean.valueOf(z));
        if (!z || ability == null) {
            return false;
        }
        if (!this.abilities.offer(ability)) {
            AppLog.w("offer ability failed", new Object[0]);
        }
        return true;
    }
}
