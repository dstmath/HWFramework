package ohos.aafwk.ability;

import ohos.annotation.SystemApi;
import ohos.bundle.ElementName;

public class AbilityMissionInfo {
    private final ElementName abilityBaseBundleName;
    private final int abilityMissionId;
    private final int abilityStackId;
    private final ElementName abilityTopBundleName;

    @SystemApi
    public AbilityMissionInfo(int i, int i2, ElementName elementName, ElementName elementName2) {
        this.abilityMissionId = i;
        this.abilityStackId = i2;
        this.abilityTopBundleName = elementName;
        this.abilityBaseBundleName = elementName2;
    }

    public int getAbilityMissionId() {
        return this.abilityMissionId;
    }

    public ElementName getAbilityTopBundleName() {
        return this.abilityTopBundleName;
    }

    @SystemApi
    public int getAbilityStackId() {
        return this.abilityStackId;
    }

    public ElementName getAbilityBaseBundleName() {
        return this.abilityBaseBundleName;
    }
}
