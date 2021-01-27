package ohos.aafwk.ability.delegation;

import java.util.Map;

public interface IAbilityDelegatorArgs {
    String getTestBundleName();

    String getTestCaseNames();

    ClassLoader getTestClassLoader();

    Map<String, Object> getTestParameters();

    String getTestRunnerClassName();
}
