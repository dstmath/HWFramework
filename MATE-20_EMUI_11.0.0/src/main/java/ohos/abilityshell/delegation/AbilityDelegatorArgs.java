package ohos.abilityshell.delegation;

import java.util.HashMap;
import java.util.Map;
import ohos.aafwk.ability.delegation.IAbilityDelegatorArgs;

public class AbilityDelegatorArgs implements IAbilityDelegatorArgs {
    private ClassLoader classLoader;
    private String testBundleName;
    private String testCaseNames;
    private Map<String, Object> testParameters = new HashMap();
    private String testRunnerClassName;

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegatorArgs
    public Map<String, Object> getTestParameters() {
        return this.testParameters;
    }

    public void setTestParameters(Map<String, Object> map) {
        if (map != null) {
            this.testParameters.putAll(map);
        }
    }

    public void removeTestParameter(String str) {
        this.testParameters.remove(str);
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegatorArgs
    public ClassLoader getTestClassLoader() {
        return this.classLoader;
    }

    public void setClassLoader(ClassLoader classLoader2) {
        this.classLoader = classLoader2;
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegatorArgs
    public String getTestCaseNames() {
        return this.testCaseNames;
    }

    public void setTestCaseNames(String str) {
        this.testCaseNames = str;
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegatorArgs
    public String getTestRunnerClassName() {
        return this.testRunnerClassName;
    }

    public void setTestRunnerClassName(String str) {
        this.testRunnerClassName = str;
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegatorArgs
    public String getTestBundleName() {
        return this.testBundleName;
    }

    public void setTestBundleName(String str) {
        this.testBundleName = str;
    }
}
