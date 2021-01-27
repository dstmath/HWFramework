package ohos.aafwk.ability.delegation;

import java.util.List;
import java.util.Optional;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Skills;
import ohos.agp.components.Component;
import ohos.app.Context;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.TouchEvent;

public interface IAbilityDelegator {
    public static final int ACTIVE = 2;
    public static final int BACKGROUND = 3;
    public static final int INACTIVE = 1;
    public static final int INITIAL = 0;
    public static final int UNDEFINED = -1;

    IAbilityMonitor addAbilityMonitor(String str);

    IAbilityMonitor addAbilityMonitor(Skills skills);

    void clearAllMonitors();

    boolean doAbilityActive(Ability ability, Intent intent);

    boolean doAbilityBackground(Ability ability);

    boolean doAbilityForeground(Ability ability, Intent intent);

    boolean doAbilityInactive(Ability ability);

    boolean doAbilitySliceActive(Ability ability);

    boolean doAbilitySliceBackground(Ability ability);

    boolean doAbilitySliceForeground(Ability ability, Intent intent);

    boolean doAbilitySliceInactive(Ability ability);

    boolean doAbilitySliceStart(Ability ability, AbilitySlice abilitySlice);

    boolean doAbilitySliceStart(Ability ability, Intent intent);

    boolean doAbilitySliceStop(Ability ability);

    int getAbilitySliceState(AbilitySlice abilitySlice);

    int getAbilityState(Ability ability);

    List<AbilitySlice> getAllAbilitySlice(Ability ability);

    Context getAppContext();

    AbilitySlice getCurrentAbilitySlice(Ability ability);

    Ability getCurrentTopAbility();

    int getMonitorsNum();

    int getSliceStackSize(Ability ability);

    String getThreadName();

    void invokeAbilityOnActive(Ability ability);

    void invokeAbilityOnBackground(Ability ability);

    void invokeAbilityOnForeground(Ability ability);

    void invokeAbilityOnInactive(Ability ability);

    void invokeAbilityOnPostActive(Ability ability);

    void invokeAbilityOnPostStart(Ability ability);

    void invokeAbilityOnStart(Ability ability);

    void invokeAbilityOnStop(Ability ability);

    boolean isAbilityHasSlice(Ability ability);

    boolean isSliceStackEmpty(Ability ability);

    void print(String str);

    void println(String str);

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
