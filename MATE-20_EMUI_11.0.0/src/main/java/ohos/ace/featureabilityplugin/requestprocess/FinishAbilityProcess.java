package ohos.ace.featureabilityplugin.requestprocess;

import com.huawei.ace.plugin.Result;
import com.huawei.ace.plugin.internal.PluginErrorCode;
import com.huawei.ace.runtime.ALog;
import java.util.List;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.ace.featureabilityplugin.requestparse.ParsedJsRequest;
import ohos.ace.featureabilityplugin.requestparse.RequestParse;
import ohos.app.AbilityContext;

public class FinishAbilityProcess {
    private static final FinishAbilityProcess INSTANCE = new FinishAbilityProcess();
    private static final String TAG = FinishAbilityProcess.class.getSimpleName();

    public static FinishAbilityProcess getInstance() {
        return INSTANCE;
    }

    public void finishAbilityWithResult(AbilityContext abilityContext, List<Object> list, Result result) {
        if (result == null) {
            ALog.e(TAG, "finish ability result handler is null!");
        } else if (abilityContext == null || list == null) {
            ALog.e(TAG, "finish ability context or arguments is null");
            result.error(2001, "finish ability context or arguments is null");
        } else {
            ParsedJsRequest parsedJsRequest = new ParsedJsRequest();
            if (!RequestParse.getInstance().checkAndParseRequest(list, parsedJsRequest, 7)) {
                result.error(2002, parsedJsRequest.getParseErrorMessage());
                return;
            }
            try {
                if (!(abilityContext instanceof Ability)) {
                    ALog.e(TAG, "context is not instance of Ability!");
                    result.error(2001, "context is not instance of Ability");
                    return;
                }
                Ability ability = (Ability) abilityContext;
                Intent intent = new Intent();
                if (parsedJsRequest.getFinishAbilityResultData() != null) {
                    IntentParams intentParams = new IntentParams();
                    intentParams.setParam("resultData", parsedJsRequest.getFinishAbilityResultData());
                    intent.setParams(intentParams);
                }
                ability.setResult(parsedJsRequest.getFinishAbilityResultCode(), intent);
                ability.terminateAbility();
            } catch (IllegalArgumentException | IllegalStateException e) {
                String str = TAG;
                ALog.e(str, "Finish ability failed:" + e.getMessage());
                result.error(PluginErrorCode.FA_FINISH_ABILITY_RET_FAILED, "Finish ability failed:" + e.getMessage());
            }
        }
    }
}
