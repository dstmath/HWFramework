package ohos.aafwk.ability;

import java.util.ArrayList;
import java.util.List;
import ohos.annotation.SystemApi;
import ohos.bundle.ElementName;

@SystemApi
public class AbilityStackInfo {
    private List<AbilityMissionInfo> abilityMissionInfos;
    private int[] abilityStackBounds = new int[4];
    private int abilityStackId;
    private ElementName topBundleName;

    public int getAbilityStackId() {
        return this.abilityStackId;
    }

    public void setAbilityStackId(int i) {
        this.abilityStackId = i;
    }

    public int[] getAbilityStackBounds() {
        return this.abilityStackBounds;
    }

    public void setAbilityStackBounds(int i, int i2, int i3, int i4) {
        int[] iArr = this.abilityStackBounds;
        iArr[0] = i;
        iArr[1] = i2;
        iArr[2] = i3;
        iArr[3] = i4;
    }

    public List<AbilityMissionInfo> getAbilityMissionInfos() {
        return this.abilityMissionInfos;
    }

    public void addAbilityMissionInfo(AbilityMissionInfo abilityMissionInfo) {
        if (this.abilityMissionInfos == null) {
            this.abilityMissionInfos = new ArrayList();
        }
        this.abilityMissionInfos.add(abilityMissionInfo);
    }

    public ElementName gettopBundleName() {
        return this.topBundleName;
    }

    public void settopBundleName(ElementName elementName) {
        this.topBundleName = elementName;
    }
}
