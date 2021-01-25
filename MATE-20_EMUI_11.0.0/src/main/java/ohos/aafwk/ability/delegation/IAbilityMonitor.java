package ohos.aafwk.ability.delegation;

import ohos.aafwk.ability.Ability;

public interface IAbilityMonitor {
    Ability waitForAbility();

    Ability waitForAbility(long j);
}
