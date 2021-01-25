package ohos.ace.featureabilityplugin.requestprocess;

import com.huawei.ace.plugin.Result;
import com.huawei.ace.runtime.ALog;
import java.util.List;
import java.util.Map;
import ohos.aafwk.content.Intent;
import ohos.ace.ability.AceAbility;
import ohos.ace.ability.AceInternalAbility;
import ohos.ace.featureabilityplugin.AbilityConnection;
import ohos.ace.featureabilityplugin.AbilityManager;
import ohos.ace.featureabilityplugin.AbilityProxy;
import ohos.ace.featureabilityplugin.requestparse.ParsedJsRequest;
import ohos.ace.featureabilityplugin.requestparse.RequestParse;
import ohos.app.AbilityContext;
import ohos.bundle.ElementName;
import ohos.tools.Bytrace;

public class CallAbilityProcess {
    private static final int ABILITY = 0;
    private static final CallAbilityProcess INSTANCE = new CallAbilityProcess();
    private static final int INTERNAL_ABILITY = 1;
    private static final String TAG = CallAbilityProcess.class.getSimpleName();

    public static CallAbilityProcess getInstance() {
        return INSTANCE;
    }

    public void callAbility(AbilityContext abilityContext, List<Object> list, Result result) {
        if (result == null) {
            ALog.e(TAG, "call ability result handler is null!");
        } else if (abilityContext == null || list == null) {
            ALog.e(TAG, "call ability context or arguments is null");
            result.error(2001, "call ability context or arguments is null");
        } else if (!(abilityContext instanceof AceAbility)) {
            ALog.e(TAG, "call ability context is not instanceof AceAbility");
            result.error(2001, "call ability context is not instanceof AceAbility");
        } else {
            ParsedJsRequest parsedJsRequest = new ParsedJsRequest();
            if (!RequestParse.getInstance().checkAndParseRequest(list, parsedJsRequest, 1)) {
                result.error(2002, parsedJsRequest.getParseErrorMessage());
                return;
            }
            int abilityType = parsedJsRequest.getAbilityType();
            if (abilityType == 1) {
                callInternalAbility(parsedJsRequest, result);
            } else if (abilityType != 0) {
                ALog.e(TAG, "call ability abilityType invalid");
                result.error(2002, "call ability abilityType invalid");
            } else if (parsedJsRequest.getBundleName() == null) {
                result.error(2001, "bundleName can't be null");
            } else {
                int abilityId = ((AceAbility) abilityContext).getAbilityId();
                if (!AbilityManager.getInstance().checkAbilityConnectionExist(abilityId, parsedJsRequest.getBundleAndAbilityName())) {
                    connectAndCallAbility(abilityContext, parsedJsRequest, result, abilityId);
                } else {
                    getAndCallAbility(parsedJsRequest, result, abilityId);
                }
            }
        }
    }

    private void connectAndCallAbility(AbilityContext abilityContext, ParsedJsRequest parsedJsRequest, Result result, int i) {
        ALog.d(TAG, "Ability first request, setup connection and call ability");
        AbilityConnection abilityConnection = new AbilityConnection(i);
        abilityConnection.setFirstConnectedCallAbility(parsedJsRequest.getMessageCode(), parsedJsRequest.getRequestData(), result, parsedJsRequest.getSyncOption());
        Intent intent = new Intent();
        intent.setElement(new ElementName("", parsedJsRequest.getBundleName(), parsedJsRequest.getAbilityName()));
        if (!abilityContext.connectAbility(intent, abilityConnection)) {
            ALog.e(TAG, "Connect ability failed");
            result.error(2003, "Connect ability failed");
            return;
        }
        AbilityManager.getInstance().addAbilityConnection(i, parsedJsRequest.getBundleAndAbilityName(), abilityConnection);
    }

    private void getAndCallAbility(ParsedJsRequest parsedJsRequest, Result result, int i) {
        ALog.d(TAG, "Ability already connected, get connection and call ability");
        AbilityConnection abilityConnection = AbilityManager.getInstance().getAbilityConnection(i, parsedJsRequest.getBundleAndAbilityName());
        if (abilityConnection == null || !abilityConnection.isAvailable()) {
            ALog.e(TAG, "Call ability, ability is disconnected");
            result.error(2005, "Call ability, ability is disconnected");
            return;
        }
        abilityConnection.callAbility(parsedJsRequest.getMessageCode(), parsedJsRequest.getRequestData(), result, parsedJsRequest.getSyncOption());
    }

    private void callInternalAbility(ParsedJsRequest parsedJsRequest, Result result) {
        Bytrace.startTrace(549755813888L, "callInternalAbility");
        ALog.d(TAG, "call internal ability");
        AceInternalAbility.AceInternalAbilityHandler aceInternalAbilityHandler = AbilityManager.getInstance().getInternalAbilityHandlers().get(parsedJsRequest.getBundleAndAbilityName());
        if (aceInternalAbilityHandler != null) {
            Map<String, AbilityProxy> internalAbilityProxy = AbilityManager.getInstance().getInternalAbilityProxy();
            AbilityProxy abilityProxy = internalAbilityProxy.get(parsedJsRequest.getBundleAndAbilityName());
            if (abilityProxy == null) {
                abilityProxy = new AbilityProxy(aceInternalAbilityHandler);
                internalAbilityProxy.put(parsedJsRequest.getBundleAndAbilityName(), abilityProxy);
            }
            abilityProxy.callAbility(parsedJsRequest.getMessageCode(), parsedJsRequest.getRequestData(), result, parsedJsRequest.getSyncOption());
        } else {
            ALog.e(TAG, "Internal ability not register.");
            result.error(2004, "Internal ability not register.");
        }
        Bytrace.finishTrace(549755813888L, "callInternalAbility");
    }
}
