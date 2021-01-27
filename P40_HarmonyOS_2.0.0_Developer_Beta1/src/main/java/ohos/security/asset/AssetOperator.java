package ohos.security.asset;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.IntentParams;
import ohos.sysability.samgr.SysAbilityManager;

public class AssetOperator {
    public static final int ASSET_TYPE_CREDIT_CARD = 1;
    public static final int ASSET_TYPE_TOKEN = 2;
    public static final int ASSET_TYPE_USERNAME_PASSWORD = 0;
    public static final int ERROR_CODE_DATABASE_ERROR = -4;
    public static final int ERROR_CODE_INVALID_ARGUMENT = -1;
    public static final int ERROR_CODE_PERMISSION_DENIED = -2;
    public static final int ERROR_CODE_SYSTEM_ERROR = -10;
    public static final int ERROR_CODE_UNINITIALIZED = -3;
    public static final int ERROR_CODE_UNSECURED_ENVIRONMENT = -5;
    public static final String INTENT_PARAMS_KEY_ACCESSLIMITATION = "268525463";
    public static final String INTENT_PARAMS_KEY_AEADASSET = "-1878958190";
    public static final String INTENT_PARAMS_KEY_APPTAG = "-1878958192";
    public static final String INTENT_PARAMS_KEY_ASSETHANDLE = "-1878958184";
    public static final String INTENT_PARAMS_KEY_ASSETTYPE = "268525459";
    public static final String INTENT_PARAMS_KEY_AUTHENTICATELIMITATION = "268525461";
    public static final String INTENT_PARAMS_KEY_BATCHASSET = "-1878958191";
    public static final String INTENT_PARAMS_KEY_EXTINFO = "-1878958188";
    public static final String INTENT_PARAMS_KEY_SELECT_MODE = "268525465";
    public static final String INTENT_PARAMS_KEY_SYNCLIMITATION = "268525462";
    private static final int SA_ID = 3599;
    public static final int SELECT_CONTINUE = 2;
    public static final int SELECT_FROM_BEGIN = 1;
    public static final int SELECT_ONCE = 0;
    public static final int SUCCESS = 0;
    private static AssetOperatorSingleton sAssetOperator = new AssetOperatorSingleton();

    private AssetOperator() {
    }

    public static AssetOperatorAbs getInstance() {
        return sAssetOperator;
    }

    private static class AssetOperatorSingleton extends AssetOperatorAbs {
        private IAssetOper proxy = new AssetOperatorProxy(SysAbilityManager.getSysAbility(AssetOperator.SA_ID));

        @Override // ohos.security.asset.AssetOperatorAbs
        public AssetResult assetInsert(Ability ability, IntentParams intentParams) {
            return this.proxy.assetInsert(ability, intentParams);
        }

        @Override // ohos.security.asset.AssetOperatorAbs
        public AssetResult assetDelete(Ability ability, IntentParams intentParams) {
            return this.proxy.assetDelete(ability, intentParams);
        }

        @Override // ohos.security.asset.AssetOperatorAbs
        public AssetResult assetUpdate(Ability ability, IntentParams intentParams) {
            return this.proxy.assetUpdate(ability, intentParams);
        }

        @Override // ohos.security.asset.AssetOperatorAbs
        public AssetResult assetSelect(Ability ability, IntentParams intentParams) {
            return this.proxy.assetSelect(ability, intentParams);
        }
    }
}
