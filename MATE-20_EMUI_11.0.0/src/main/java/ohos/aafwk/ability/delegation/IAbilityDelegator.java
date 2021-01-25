package ohos.aafwk.ability.delegation;

import java.util.List;
import java.util.Optional;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentFilter;
import ohos.agp.components.Component;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.TouchEvent;

public interface IAbilityDelegator {
    public static final int ACTIVE = 2;
    public static final int BACKGROUND = 3;
    public static final int INACTIVE = 1;
    public static final int INITIAL = 0;
    public static final int UNDEFINED = -1;

    IAbilityMonitor addAbilityMonitor(String str);

    IAbilityMonitor addAbilityMonitor(IntentFilter intentFilter);

    void clearAllMonitors();

    boolean doAbilityActive(Ability ability, Intent intent);

    boolean doAbilityBackground(Ability ability);

    boolean doAbilityForeground(Ability ability, Intent intent);

    boolean doAbilityInactive(Ability ability);

    boolean doAbilitySliceActive(Ability ability);

    boolean doAbilitySliceBackground(Ability ability);

    boolean doAbilitySliceForeground(Ability ability, Intent intent);

    boolean doAbilitySliceInactive(Ability ability);

    boolean doAbilitySliceStart(Ability ability, Intent intent);

    boolean doAbilitySliceStop(Ability ability);

    int getAbilitySliceState(AbilitySlice abilitySlice);

    int getAbilityState(Ability ability);

    List<AbilitySlice> getAllAbilitySlice(Ability ability);

    AbilitySlice getCurrentAbilitySlice(Ability ability);

    Ability getCurrentTopAbility();

    int getMonitorsNum();

    void print(String str);

    void removeAbilityMonitor(IAbilityMonitor iAbilityMonitor);

    boolean runOnUIThreadSync(Runnable runnable);

    Optional<Ability> startAbilitySync(Intent intent);

    Optional<Ability> startAbilitySync(Intent intent, long j);

    boolean stopAbility(Ability ability);

    boolean triggerClickEvent(Ability ability, Component component);

    boolean triggerKeyEvent(Ability ability, KeyEvent keyEvent);

    boolean triggerTouchEvent(Ability ability, TouchEvent touchEvent);

    Optional<Ability> waitAbilityMonitor(IAbilityMonitor iAbilityMonitor);

    Optional<Ability> waitAbilityMonitor(IAbilityMonitor iAbilityMonitor, long j);
}
