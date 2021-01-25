package ohos.aafwk.ability;

import ohos.aafwk.content.Intent;
import ohos.rpc.IRemoteObject;

public interface IAbilityScheduler {
    void scheduleAbilityLifecycle(Intent intent, int i);

    void scheduleAbilityResult(int i, int i2, Intent intent);

    void scheduleCommand(Intent intent, boolean z, int i);

    IRemoteObject scheduleConnectAbility(Intent intent);

    void scheduleDisconnectAbility(Intent intent);

    void scheduleNewIntent(Intent intent);
}
