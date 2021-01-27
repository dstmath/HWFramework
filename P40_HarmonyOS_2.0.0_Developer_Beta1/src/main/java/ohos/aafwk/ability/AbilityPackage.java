package ohos.aafwk.ability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import ohos.aafwk.utils.log.Log;
import ohos.app.AbilityContext;
import ohos.app.ElementsCallback;
import ohos.global.configuration.Configuration;
import ohos.pluginproxy.PluginProxyUtils;
import ohos.utils.PacMap;

public class AbilityPackage extends AbilityContext implements ElementsCallback {
    private final Collection<AbilityLifecycleCallbacks> myAbilityLifecycleCallbacks = Collections.synchronizedCollection(new ArrayList());
    private final Collection<ElementsCallback> myComponentsCallbacks = Collections.synchronizedCollection(new ArrayList());

    @Override // ohos.app.ElementsCallback
    public void onConfigurationUpdated(Configuration configuration) {
    }

    public void onEnd() {
    }

    public void onInitialize() {
    }

    @Override // ohos.app.ElementsCallback
    public void onMemoryLevel(int i) {
    }

    public AbilityPackage() {
        Log.debug("AbilityPackage construct", new Object[0]);
    }

    public void registerCallbacks(AbilityLifecycleCallbacks abilityLifecycleCallbacks, ElementsCallback elementsCallback) {
        if (abilityLifecycleCallbacks != null) {
            this.myAbilityLifecycleCallbacks.add(abilityLifecycleCallbacks);
        }
        if (elementsCallback != null) {
            this.myComponentsCallbacks.add(elementsCallback);
        }
    }

    public void unregisterCallbacks(AbilityLifecycleCallbacks abilityLifecycleCallbacks, ElementsCallback elementsCallback) {
        if (abilityLifecycleCallbacks != null) {
            this.myAbilityLifecycleCallbacks.remove(abilityLifecycleCallbacks);
        }
        if (elementsCallback != null) {
            this.myComponentsCallbacks.remove(elementsCallback);
        }
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

    public final Object createProxyObject(String str, Class cls, Object... objArr) {
        Log.debug("AbilityPackage createProxyObject called.", new Object[0]);
        return PluginProxyUtils.createProxyObject(this, str, cls, objArr);
    }

    private AbilityLifecycleCallbacks[] collectAbilityLifecycleCallbacks() {
        return (AbilityLifecycleCallbacks[]) this.myAbilityLifecycleCallbacks.toArray(new AbilityLifecycleCallbacks[0]);
    }

    private ElementsCallback[] collectComponentsCallbacks() {
        return (ElementsCallback[]) this.myComponentsCallbacks.toArray(new ElementsCallback[0]);
    }
}
