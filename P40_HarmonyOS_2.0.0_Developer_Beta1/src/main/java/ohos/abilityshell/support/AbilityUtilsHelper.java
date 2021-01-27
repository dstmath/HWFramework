package ohos.abilityshell.support;

public class AbilityUtilsHelper {
    private static final AbilityUtilsImpl SERVICE = new AbilityUtilsImpl();

    public static IAbilityUtils getService() {
        return SERVICE;
    }

    private AbilityUtilsHelper() {
    }
}
