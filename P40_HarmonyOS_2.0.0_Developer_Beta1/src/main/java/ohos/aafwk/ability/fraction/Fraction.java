package ohos.aafwk.ability.fraction;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import ohos.aafwk.ability.ILifecycle;
import ohos.aafwk.ability.Lifecycle;
import ohos.aafwk.content.Intent;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;
import ohos.app.AbilityContext;
import ohos.app.Context;
import ohos.utils.PacMap;
import ohos.utils.Sequenceable;

public class Fraction extends AbilityContext implements ILifecycle {
    static final int ACTIVE = 3;
    static final int ATTACH_COMPONENT = 1;
    static final int INITIAL = 0;
    private static final LogLabel LABEL = LogLabel.create();
    static final int STARTED = 2;
    private static final HashMap<String, Class<?>> S_CLASS_MAP = new HashMap<>();
    static final int UNINITIAL = -1;
    private Lifecycle lifecycle = new Lifecycle();
    FractionAbility mAbility;
    boolean mAdded;
    PacMap mArguments;
    int mBackStackNesting;
    Component mComponent;
    ComponentContainer mContainer;
    int mContainerId;
    int mFractionId;
    boolean mFromLayout;
    boolean mHidden;
    boolean mHiddenChanged;
    int mIndex = -1;
    Intent mIntent = new Intent();
    FractionManager mManager;
    Component mRealComponent;
    boolean mRemoving;
    Sequenceable[] mSavedComponentState;
    PacMap mSavedFractionData;
    int mState = 0;
    String mTag;
    boolean mUpgrade = true;

    /* access modifiers changed from: protected */
    public void onActive() {
    }

    /* access modifiers changed from: protected */
    public void onBackground() {
    }

    /* access modifiers changed from: protected */
    public Component onComponentAttached(LayoutScatter layoutScatter, ComponentContainer componentContainer, Intent intent) {
        return null;
    }

    /* access modifiers changed from: protected */
    public void onComponentDetach() {
    }

    /* access modifiers changed from: protected */
    public void onForeground(Intent intent) {
    }

    /* access modifiers changed from: protected */
    public void onInactive() {
    }

    /* access modifiers changed from: protected */
    public void onSaveFractionState(PacMap pacMap) {
    }

    /* access modifiers changed from: protected */
    public void onStart(Intent intent) {
    }

    /* access modifiers changed from: protected */
    public void onStop() {
    }

    /* access modifiers changed from: package-private */
    public void initState() {
        this.mIndex = -1;
        this.mAdded = false;
        this.mRemoving = false;
        this.mFromLayout = false;
        this.mBackStackNesting = 0;
        this.mContainerId = 0;
        this.mTag = null;
        this.mHidden = false;
    }

    static Fraction init(Context context, String str, PacMap pacMap) {
        Fraction fraction = new Fraction();
        try {
            Class<?> cls = S_CLASS_MAP.get(str);
            if (cls == null) {
                cls = context.getClassloader().loadClass(str);
                S_CLASS_MAP.put(str, cls);
            }
            if (cls.getConstructor(new Class[0]).newInstance(new Object[0]) instanceof Fraction) {
                fraction = (Fraction) cls.getConstructor(new Class[0]).newInstance(new Object[0]);
            }
            if (pacMap != null) {
                pacMap.setClassLoader(fraction.getClass().getClassLoader());
                fraction.setArguments(pacMap);
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            Log.error(LABEL, "Unable to init fraction %{public}s: make sure class name exists, %{public}s", str, e);
        }
        return fraction;
    }

    /* access modifiers changed from: package-private */
    public void setIndex(int i) {
        this.mIndex = i;
    }

    @Override // ohos.aafwk.ability.ILifecycle
    public final Lifecycle getLifecycle() {
        return this.lifecycle;
    }

    private void dispatchLifecycle(Lifecycle.Event event, Intent intent) throws IllegalStateException {
        Lifecycle lifecycle2 = this.lifecycle;
        if (lifecycle2 != null) {
            lifecycle2.dispatchLifecycle(event, intent);
            return;
        }
        throw new IllegalStateException("lifecycle is null, dispatch lifecycle failed for slice");
    }

    private void setArguments(PacMap pacMap) {
        if (this.mIndex <= 0 || !isStateSaved()) {
            this.mArguments = pacMap;
            return;
        }
        throw new IllegalArgumentException("Fraction already active and state has been saved.");
    }

    private boolean isStateSaved() {
        FractionManager fractionManager = this.mManager;
        if (fractionManager == null) {
            return false;
        }
        return fractionManager.isStateSaved();
    }

    /* access modifiers changed from: package-private */
    public void executeAttachComponent(LayoutScatter layoutScatter, ComponentContainer componentContainer, Intent intent) {
        this.mState = 1;
        this.mComponent = onComponentAttached(layoutScatter, componentContainer, intent);
    }

    /* access modifiers changed from: package-private */
    public void executeStart(Intent intent) {
        this.mState = 2;
        onStart(intent);
        dispatchLifecycle(Lifecycle.Event.ON_START, intent);
    }

    /* access modifiers changed from: package-private */
    public void executeForeground(Intent intent) {
        this.mState = 2;
        onForeground(intent);
        dispatchLifecycle(Lifecycle.Event.ON_FOREGROUND, intent);
    }

    /* access modifiers changed from: package-private */
    public void executeActive(Intent intent) {
        this.mState = 3;
        onActive();
        dispatchLifecycle(Lifecycle.Event.ON_ACTIVE, intent);
    }

    /* access modifiers changed from: package-private */
    public void executeInActive(Intent intent) {
        this.mState = 2;
        onInactive();
        dispatchLifecycle(Lifecycle.Event.ON_INACTIVE, intent);
    }

    /* access modifiers changed from: package-private */
    public void executeBackground(Intent intent) {
        this.mState = 1;
        onBackground();
        dispatchLifecycle(Lifecycle.Event.ON_BACKGROUND, intent);
    }

    /* access modifiers changed from: package-private */
    public void executeStop(Intent intent) {
        this.mState = 0;
        onStop();
        dispatchLifecycle(Lifecycle.Event.ON_STOP, intent);
    }

    /* access modifiers changed from: package-private */
    public void executeComponentDetach() {
        onComponentDetach();
    }

    public FractionAbility getFractionAbility() {
        return this.mAbility;
    }

    public Component getComponent() {
        return this.mComponent;
    }

    /* access modifiers changed from: package-private */
    public boolean isInBackStack() {
        return this.mBackStackNesting > 0;
    }
}
