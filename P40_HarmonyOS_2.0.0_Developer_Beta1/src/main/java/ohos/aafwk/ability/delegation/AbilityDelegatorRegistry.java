package ohos.aafwk.ability.delegation;

import java.util.concurrent.atomic.AtomicReference;

public final class AbilityDelegatorRegistry {
    private static final AtomicReference<IAbilityDelegator> ABILITY_DELEGATOR_REF = new AtomicReference<>();
    private static final AtomicReference<IAbilityDelegatorArgs> ARGUMENTS = new AtomicReference<>();

    private AbilityDelegatorRegistry() {
    }

    public static IAbilityDelegator getAbilityDelegator() {
        IAbilityDelegator iAbilityDelegator = ABILITY_DELEGATOR_REF.get();
        if (iAbilityDelegator != null) {
            return iAbilityDelegator;
        }
        throw new IllegalStateException("No abilitydelegator registered!");
    }

    public static IAbilityDelegatorArgs getArguments() {
        IAbilityDelegatorArgs iAbilityDelegatorArgs = ARGUMENTS.get();
        if (iAbilityDelegatorArgs != null) {
            return iAbilityDelegatorArgs;
        }
        throw new IllegalStateException("No abilitydelegator ARGUMENTS registered!");
    }

    public static void registerInstance(IAbilityDelegator iAbilityDelegator, IAbilityDelegatorArgs iAbilityDelegatorArgs) {
        ABILITY_DELEGATOR_REF.set(iAbilityDelegator);
        ARGUMENTS.set(iAbilityDelegatorArgs);
    }
}
