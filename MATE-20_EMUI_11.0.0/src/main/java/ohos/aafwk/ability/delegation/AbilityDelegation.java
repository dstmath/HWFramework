package ohos.aafwk.ability.delegation;

import java.util.List;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.bundle.AbilityInfo;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.TouchEvent;

public abstract class AbilityDelegation {
    public static final int ACTIVE = 2;
    public static final int BACKGROUND = 3;
    public static final int INACTIVE = 1;
    public static final int INITIAL = 0;
    public static final String LIB_PATH = "TestCasePath";
    public static final String RUN_TEST = "AbilityTestCase";
    public static final int UNDEFINED = -1;
    static AbilityDelegation instance;

    public abstract void doAbilityActive();

    public abstract void doAbilityBackground();

    public abstract void doAbilityForeground(Intent intent);

    public abstract void doAbilityInactive();

    public abstract void doAbilitySliceActive();

    public abstract void doAbilitySliceBackground();

    public abstract void doAbilitySliceForeground(Intent intent);

    public abstract void doAbilitySliceInactive();

    public abstract void doAbilitySliceStart(Intent intent);

    public abstract void doAbilitySliceStop();

    public abstract void doAbilityStart(Intent intent);

    public abstract void doAbilityStop();

    public abstract Ability getAbility();

    public abstract AbilityInfo getAbilityInfo();

    public abstract List<AbilitySlice> getAbilitySlice(String str);

    public abstract int getAbilitySliceState(AbilitySlice abilitySlice);

    public abstract int getAbilityState();

    public abstract List<AbilitySlice> getAllAbilitySlice();

    public abstract AbilitySlice getCurrentAbilitySlice();

    public abstract Ability getCurrentTopAbility();

    public abstract Intent getIntent();

    /* access modifiers changed from: package-private */
    public abstract void init(Ability ability, Intent intent, ClassLoader classLoader);

    public abstract void output(String str);

    public abstract void runOnUIThreadSync(Runnable runnable);

    /* access modifiers changed from: package-private */
    public abstract void runTestCase();

    public abstract boolean triggerClickEvent(Component component);

    public abstract boolean triggerKeyEvent(KeyEvent keyEvent);

    public abstract boolean triggerTouchEvent(TouchEvent touchEvent);

    /* access modifiers changed from: package-private */
    public abstract void updateAbility(Ability ability, Intent intent);

    static AbilityDelegation getInstance() {
        return instance;
    }
}
