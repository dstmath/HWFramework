package ohos.security.asset;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.IntentParams;
import ohos.rpc.IRemoteBroker;

public interface IAssetOper extends IRemoteBroker {
    public static final String descriptor = "IAssetOper";

    AssetResult assetDelete(Ability ability, IntentParams intentParams);

    AssetResult assetInsert(Ability ability, IntentParams intentParams);

    AssetResult assetSelect(Ability ability, IntentParams intentParams);

    AssetResult assetUpdate(Ability ability, IntentParams intentParams);
}
