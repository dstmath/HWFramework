package ohos.aafwk.ability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import ohos.aafwk.utils.log.Log;
import ohos.annotation.SystemApi;
import ohos.app.AbilityContext;
import ohos.app.ElementsCallback;
import ohos.global.configuration.Configuration;
import ohos.utils.PacMap;

@SystemApi
public class HarmonyosApplication extends AbilityContext implements ElementsCallback {
    private final Collection<AbilityLifecycleCallbacks> myAbilityLifecycleCallbacks = Collections.synchronizedCollection(new ArrayList());
    private final Collection<ElementsCallback> myComponentsCallbacks = Collections.synchronizedCollection(new ArrayList());

    public void onStart() {
        Log.debug("UserApplication::onCreate Called when the application is starting before all ability", new Object[0]);
    }

    public void onTerminate() {
        Log.debug("UserApplication::onTerminate Called when the application is starting before all ability", new Object[0]);
    }

    @Override // ohos.app.ElementsCallback
    public void onMemoryLevel(int i) {
        Log.debug("UserApplication::onMemoryLevel Called when memory level change", new Object[0]);
    }

    @Override // ohos.app.ElementsCallback
    public void onConfigurationUpdated(Configuration configuration) {
        Log.warn("wait for configuration support by global system", new Object[0]);
    }

    public void registerElementsCallbacks(ElementsCallback elementsCallback) {
        if (elementsCallback != null) {
            this.myComponentsCallbacks.add(elementsCallback);
            return;
        }
        throw new IllegalArgumentException("ElementsCallback argument is null");
    }

    public void unregisterElementsCallbacks(ElementsCallback elementsCallback) {
        if (elementsCallback != null) {
            this.myComponentsCallbacks.remove(elementsCallback);
            return;
        }
        throw new IllegalArgumentException("ElementsCallback argument is null");
    }

    public void registerAbilityLifecycleCallbacks(AbilityLifecycleCallbacks abilityLifecycleCallbacks) {
        if (abilityLifecycleCallbacks != null) {
            this.myAbilityLifecycleCallbacks.add(abilityLifecycleCallbacks);
            return;
        }
        throw new IllegalArgumentException("registerAbilityLifecycleCallbacks argument is null");
    }

    public void unregisterAbilityLifecycleCallbacks(AbilityLifecycleCallbacks abilityLifecycleCallbacks) {
        if (abilityLifecycleCallbacks != null) {
            this.myAbilityLifecycleCallbacks.remove(abilityLifecycleCallbacks);
            return;
        }
        throw new IllegalArgumentException("unregisterAbilityLifecycleCallbacks argument is null");
    }

    public void memoryLevelChange(int i) {
        for (ElementsCallback elementsCallback : collectComponentsCallbacks()) {
            elementsCallback.onMemoryLevel(i);
        }
    }

    public void configurationChanged(Configuration configuration) {
        for (ElementsCallback elementsCallback : collectComponentsCallbacks()) {
            elementsCallback.onConfigurationUpdated(configuration);
        }
    }

    public void dispatchAbilityStarted(Ability ability) {
        for (AbilityLifecycleCallbacks abilityLifecycleCallbacks : collectAbilityLifecycleCallbacks()) {
            abilityLifecycleCallbacks.onAbilityStart(ability);
        }
    }

    public void dispatchAbilityActived(Ability ability) {
        for (AbilityLifecycleCallbacks abilityLifecycleCallbacks : collectAbilityLifecycleCallbacks()) {
            abilityLifecycleCallbacks.onAbilityActive(ability);
        }
    }

    public void dispatchAbilityInactived(Ability ability) {
        for (AbilityLifecycleCallbacks abilityLifecycleCallbacks : collectAbilityLifecycleCallbacks()) {
            abilityLifecycleCallbacks.onAbilityInactive(ability);
        }
    }

    public void dispatchAbilityForegrounded(Ability ability) {
        for (AbilityLifecycleCallbacks abilityLifecycleCallbacks : collectAbilityLifecycleCallbacks()) {
            abilityLifecycleCallbacks.onAbilityForeground(ability);
        }
    }

    public void dispatchAbilityBackgrounded(Ability ability) {
        for (AbilityLifecycleCallbacks abilityLifecycleCallbacks : collectAbilityLifecycleCallbacks()) {
            abilityLifecycleCallbacks.onAbilityBackground(ability);
        }
    }

    public void dispatchAbilityStoped(Ability ability) {
        for (AbilityLifecycleCallbacks abilityLifecycleCallbacks : collectAbilityLifecycleCallbacks()) {
            abilityLifecycleCallbacks.onAbilityStop(ability);
        }
    }

    public void dispatchAbilitySavedState(PacMap pacMap) {
        for (AbilityLifecycleCallbacks abilityLifecycleCallbacks : collectAbilityLifecycleCallbacks()) {
            abilityLifecycleCallbacks.onAbilitySaveState(pacMap);
        }
    }

    private AbilityLifecycleCallbacks[] collectAbilityLifecycleCallbacks() {
        return (AbilityLifecycleCallbacks[]) this.myAbilityLifecycleCallbacks.toArray(new AbilityLifecycleCallbacks[0]);
    }

    private ElementsCallback[] collectComponentsCallbacks() {
        return (ElementsCallback[]) this.myComponentsCallbacks.toArray(new ElementsCallback[0]);
    }
}
