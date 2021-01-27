package ohos.aafwk.abilityjet.databinding;

import ohos.aafwk.ability.Lifecycle;
import ohos.agp.components.ComponentContainer;

public abstract class DataBinding {
    private Lifecycle lifecycle;

    public abstract void initComponent(ComponentContainer componentContainer);

    public void setLifecycle(Lifecycle lifecycle2) {
        if (lifecycle2 == null) {
            throw new IllegalArgumentException("lifecycle can't be null");
        } else if (this.lifecycle == null) {
            this.lifecycle = lifecycle2;
        } else {
            throw new IllegalStateException("lifecycle can't set twice");
        }
    }

    public Lifecycle getLifecycle() {
        return this.lifecycle;
    }
}
