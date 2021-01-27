package ohos.aafwk.ability.delegation;

public class AbilityTestCase {
    private AbilityDelegation delegation = AbilityDelegation.getInstance();

    /* access modifiers changed from: protected */
    public AbilityDelegation getAbilityDelegation() {
        return this.delegation;
    }

    /* access modifiers changed from: protected */
    public void output(String str) {
        AbilityDelegation abilityDelegation = this.delegation;
        if (abilityDelegation != null) {
            abilityDelegation.output(str);
        }
    }
}
