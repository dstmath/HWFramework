package ohos.aafwk.abilityjet.activedata;

import ohos.aafwk.ability.Lifecycle;
import ohos.aafwk.ability.LifecycleStateObserver;

public abstract class DataObserver<T> {
    private static final String LIFECYCLE_NOTNULL_LOG = "lifecycle can't be null";
    private Lifecycle lifeOwner;

    public abstract void onChanged(T t);

    public final void setLifecycle(Lifecycle lifecycle) {
        if (lifecycle == null) {
            throw new IllegalArgumentException(LIFECYCLE_NOTNULL_LOG);
        } else if (this.lifeOwner == null) {
            this.lifeOwner = lifecycle;
        } else {
            throw new IllegalStateException("lifecycle can't set twice");
        }
    }

    /* access modifiers changed from: package-private */
    public final void setObserver(LifecycleStateObserver lifecycleStateObserver) {
        Lifecycle lifecycle = this.lifeOwner;
        if (lifecycle != null) {
            lifecycle.addObserver(lifecycleStateObserver);
            return;
        }
        throw new IllegalStateException(LIFECYCLE_NOTNULL_LOG);
    }

    /* access modifiers changed from: package-private */
    public final void clearObserver(LifecycleStateObserver lifecycleStateObserver) {
        Lifecycle lifecycle = this.lifeOwner;
        if (lifecycle != null) {
            lifecycle.removeObserver(lifecycleStateObserver);
            return;
        }
        throw new IllegalStateException(LIFECYCLE_NOTNULL_LOG);
    }

    /* access modifiers changed from: package-private */
    public final Lifecycle.Event getLifecycleState() {
        Lifecycle lifecycle = this.lifeOwner;
        if (lifecycle != null) {
            return lifecycle.getLifecycleState();
        }
        throw new IllegalStateException(LIFECYCLE_NOTNULL_LOG);
    }
}
