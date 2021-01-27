package ohos.aafwk.ability;

import ohos.utils.PacMap;

public interface AbilityLifecycleCallbacks {
    void onAbilityActive(Ability ability);

    void onAbilityBackground(Ability ability);

    void onAbilityForeground(Ability ability);

    void onAbilityInactive(Ability ability);

    void onAbilitySaveState(PacMap pacMap);

    void onAbilityStart(Ability ability);

    void onAbilityStop(Ability ability);
}
