package ohos.security.asset;

import android.content.Context;
import android.os.Bundle;
import com.huawei.security.hwassetmanager.HwAssetManager;
import java.util.Map;
import java.util.Optional;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.IntentParams;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;

public class AssetOperatorProxy implements IAssetOper {
    private static final HiLogLabel LABEL = new HiLogLabel(3, SUB_DOMAIN_SECURITY_ASSET, "AssetOperatorProxy");
    private static final int SUB_DOMAIN_SECURITY_ASSET = 218115845;
    private final IRemoteObject mRemote;

    public AssetOperatorProxy(IRemoteObject iRemoteObject) {
        this.mRemote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.mRemote;
    }

    private static Optional<Bundle> convertIntentParamsToBundle(Map<String, Object> map) {
        if (map == null) {
            return Optional.empty();
        }
        Bundle bundle = new Bundle();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                if (value instanceof Integer) {
                    bundle.putInt(key, ((Integer) value).intValue());
                } else if (value instanceof String) {
                    bundle.putString(key, (String) value);
                } else if (value instanceof byte[]) {
                    bundle.putByteArray(key, (byte[]) value);
                } else {
                    HiLog.error(LABEL, "value type is not supported, key: %{public}s", key);
                }
            }
        }
        return Optional.of(bundle);
    }

    private Optional<Context> covertAbilityToContext(Ability ability) {
        if (ability == null) {
            return Optional.empty();
        }
        Object hostContext = ability.getHostContext();
        if (hostContext instanceof Context) {
            return Optional.of((Context) hostContext);
        }
        return Optional.empty();
    }

    @Override // ohos.security.asset.IAssetOper
    public AssetResult assetInsert(Ability ability, IntentParams intentParams) {
        if (ability == null || intentParams == null) {
            return new AssetResult(-1);
        }
        Optional<Context> covertAbilityToContext = covertAbilityToContext(ability);
        Optional<Bundle> convertIntentParamsToBundle = convertIntentParamsToBundle(intentParams.getParams());
        if (!covertAbilityToContext.isPresent() || !convertIntentParamsToBundle.isPresent()) {
            return new AssetResult(-1);
        }
        HwAssetManager.AssetResult assetInsert = HwAssetManager.getInstance().assetInsert(covertAbilityToContext.get(), convertIntentParamsToBundle.get());
        return new AssetResult(assetInsert.resultCode, assetInsert.resultInfo, assetInsert.resultNumber);
    }

    @Override // ohos.security.asset.IAssetOper
    public AssetResult assetDelete(Ability ability, IntentParams intentParams) {
        if (ability == null || intentParams == null) {
            return new AssetResult(-1);
        }
        Optional<Context> covertAbilityToContext = covertAbilityToContext(ability);
        Optional<Bundle> convertIntentParamsToBundle = convertIntentParamsToBundle(intentParams.getParams());
        if (!covertAbilityToContext.isPresent() || !convertIntentParamsToBundle.isPresent()) {
            return new AssetResult(-1);
        }
        HwAssetManager.AssetResult assetDelete = HwAssetManager.getInstance().assetDelete(covertAbilityToContext.get(), convertIntentParamsToBundle.get());
        return new AssetResult(assetDelete.resultCode, assetDelete.resultInfo, assetDelete.resultNumber);
    }

    @Override // ohos.security.asset.IAssetOper
    public AssetResult assetUpdate(Ability ability, IntentParams intentParams) {
        if (ability == null || intentParams == null) {
            return new AssetResult(-1);
        }
        Optional<Context> covertAbilityToContext = covertAbilityToContext(ability);
        Optional<Bundle> convertIntentParamsToBundle = convertIntentParamsToBundle(intentParams.getParams());
        if (!covertAbilityToContext.isPresent() || !convertIntentParamsToBundle.isPresent()) {
            return new AssetResult(-1);
        }
        HwAssetManager.AssetResult assetUpdate = HwAssetManager.getInstance().assetUpdate(covertAbilityToContext.get(), convertIntentParamsToBundle.get());
        return new AssetResult(assetUpdate.resultCode, assetUpdate.resultInfo, assetUpdate.resultNumber);
    }

    @Override // ohos.security.asset.IAssetOper
    public AssetResult assetSelect(Ability ability, IntentParams intentParams) {
        if (ability == null || intentParams == null) {
            return new AssetResult(-1);
        }
        Optional<Context> covertAbilityToContext = covertAbilityToContext(ability);
        Optional<Bundle> convertIntentParamsToBundle = convertIntentParamsToBundle(intentParams.getParams());
        if (!covertAbilityToContext.isPresent() || !convertIntentParamsToBundle.isPresent()) {
            return new AssetResult(-1);
        }
        HwAssetManager.AssetResult assetSelect = HwAssetManager.getInstance().assetSelect(covertAbilityToContext.get(), convertIntentParamsToBundle.get());
        return new AssetResult(assetSelect.resultCode, assetSelect.resultInfo, assetSelect.resultNumber);
    }
}
