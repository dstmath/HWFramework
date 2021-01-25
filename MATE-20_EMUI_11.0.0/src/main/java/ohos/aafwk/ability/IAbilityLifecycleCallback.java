package ohos.aafwk.ability;

import ohos.aafwk.content.Intent;

public interface IAbilityLifecycleCallback {
    void onAbilityActive(Intent intent);

    void onAbilityBackground();

    void onAbilityForeground(Intent intent);

    void onAbilityInactive();

    void onAbilityStart(Intent intent);

    void onAbilityStop();
}
