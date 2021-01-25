package ohos.aafwk.ability;

public interface AbilityLifecycleCallbacks {
    void onAbilityActive(Ability ability);

    void onAbilityBackground(Ability ability);

    void onAbilityForeground(Ability ability);

    void onAbilityInactive(Ability ability);

    void onAbilityStart(Ability ability);

    void onAbilityStop(Ability ability);
}
