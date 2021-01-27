package ohos.security.asset;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.IntentParams;

public abstract class AssetOperatorAbs {
    public abstract AssetResult assetDelete(Ability ability, IntentParams intentParams);

    public abstract AssetResult assetInsert(Ability ability, IntentParams intentParams);

    public abstract AssetResult assetSelect(Ability ability, IntentParams intentParams);

    public abstract AssetResult assetUpdate(Ability ability, IntentParams intentParams);
}
