package ohos.aafwk.ability;

import ohos.aafwk.ability.Lifecycle;
import ohos.aafwk.content.Intent;

public interface LifecycleStateObserver extends ILifecycleObserver {
    void onStateChanged(Lifecycle.Event event, Intent intent);
}
