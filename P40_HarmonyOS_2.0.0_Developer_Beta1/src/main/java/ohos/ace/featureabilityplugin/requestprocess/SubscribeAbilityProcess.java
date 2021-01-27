package ohos.ace.featureabilityplugin.requestprocess;

import com.huawei.ace.plugin.EventNotifier;
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
import ohos.ace.featureabilityplugin.AbilityStub;
import ohos.ace.featureabilityplugin.requestparse.ParsedJsRequest;
import ohos.ace.featureabilityplugin.requestparse.RequestParse;
import ohos.app.AbilityContext;
import ohos.bundle.ElementName;

public class SubscribeAbilityProcess {
    private static final int ABILITY = 0;
    private static final SubscribeAbilityProcess INSTANCE = new SubscribeAbilityProcess();
    private static final int INTERNAL_ABILITY = 1;
    private static final String TAG = SubscribeAbilityProcess.class.getSimpleName();

    public static SubscribeAbilityProcess getInstance() {
        return INSTANCE;
    }

    public void subscribeAbility(AbilityContext abilityContext, List<Object> list, EventNotifier eventNotifier, Result result) {
        if (result == null) {
            ALog.e(TAG, "subscribe ability result handler is null!");
        } else if (abilityContext == null || list == null || eventNotifier == null) {
            ALog.e(TAG, "subscribe ability context or arguments or eventsNotifier is null");
            result.error(2001, "subscribe ability context or arguments or eventsNotifier is null");
        } else if (!(abilityContext instanceof AceAbility)) {
            ALog.e(TAG, "subscribe ability context is not instanceof AceAbility");
            result.error(2001, "subscribe ability context is not instanceof AceAbility");
        } else {
            ParsedJsRequest parsedJsRequest = new ParsedJsRequest();
            if (!RequestParse.getInstance().checkAndParseRequest(list, parsedJsRequest, 2)) {
                result.error(2002, parsedJsRequest.getParseErrorMessage());
                return;
            }
            eventNotifier.setAbilityName(parsedJsRequest.getBundleAndAbilityName());
            int abilityType = parsedJsRequest.getAbilityType();
            if (abilityType == 1) {
                subscribeInternalAbility(parsedJsRequest, eventNotifier, result);
            } else if (abilityType != 0) {
                ALog.e(TAG, "subscribe ability abilityType invalid");
                result.error(2002, "subscribe ability abilityType invalid");
            } else if (parsedJsRequest.getBundleName() == null) {
                result.error(2001, "bundleName can't be null");
            } else {
                int abilityId = ((AceAbility) abilityContext).getAbilityId();
                if (!AbilityManager.getInstance().checkAbilityConnectionExist(abilityId, parsedJsRequest.getBundleAndAbilityName())) {
                    connectAndSubscribeAbility(abilityContext, parsedJsRequest, eventNotifier, result, abilityId);
                } else {
                    getAndSubscribeAbility(parsedJsRequest, eventNotifier, result, abilityId);
                }
            }
        }
    }

    public void unsubscribeAbility(List<Object> list, EventNotifier eventNotifier, Result result, int i) {
        if (result == null) {
            ALog.e(TAG, "unsubscribe ability result handler is null!");
        } else if (list == null || eventNotifier == null) {
            ALog.e(TAG, "unsubscribe ability arguments or eventsNotifier is null");
            result.error(2001, "unsubscribe ability arguments or eventsNotifier is null");
        } else {
            ParsedJsRequest parsedJsRequest = new ParsedJsRequest();
            if (!RequestParse.getInstance().checkAndParseRequest(list, parsedJsRequest, 3)) {
                result.error(2002, parsedJsRequest.getParseErrorMessage());
                return;
            }
            int abilityType = parsedJsRequest.getAbilityType();
            if (abilityType == 1) {
                unsubscribeInternalAbility(parsedJsRequest, eventNotifier, result);
            } else if (abilityType != 0) {
                ALog.e(TAG, "unsubscribe ability abilityType invalid");
                result.error(2002, "unsubscribe ability abilityType invalid");
            } else if (parsedJsRequest.getBundleName() == null) {
                result.error(2001, "bundleName can't be null");
            } else if (!AbilityManager.getInstance().checkAbilityConnectionExist(i, parsedJsRequest.getBundleAndAbilityName())) {
                ALog.e(TAG, "Ability not connected");
                result.error(2005, "Ability not connected");
            } else {
                getAndUnsubscribeAbility(parsedJsRequest, eventNotifier, result, i);
            }
        }
    }

    private void connectAndSubscribeAbility(AbilityContext abilityContext, ParsedJsRequest parsedJsRequest, EventNotifier eventNotifier, Result result, int i) {
        ALog.d(TAG, "Ability first request, setup connection and subscribe ability");
        AbilityConnection abilityConnection = new AbilityConnection(i);
        abilityConnection.setFirstConnectedSubscribeAbility(parsedJsRequest.getMessageCode(), eventNotifier, result, parsedJsRequest.getSyncOption());
        Intent intent = new Intent();
        intent.setElement(new ElementName("", parsedJsRequest.getBundleName(), parsedJsRequest.getAbilityName()));
        if (!abilityContext.connectAbility(intent, abilityConnection)) {
            result.error(2003, "Connect ability failed");
            ALog.e(TAG, "Connect ability failed");
            return;
        }
        AbilityManager.getInstance().addAbilityConnection(i, parsedJsRequest.getBundleAndAbilityName(), abilityConnection);
    }

    private void getAndSubscribeAbility(ParsedJsRequest parsedJsRequest, EventNotifier eventNotifier, Result result, int i) {
        ALog.d(TAG, "Ability already connected, get connection and subscribe ability");
        AbilityConnection abilityConnection = AbilityManager.getInstance().getAbilityConnection(i, parsedJsRequest.getBundleAndAbilityName());
        if (abilityConnection == null || !abilityConnection.isAvailable()) {
            ALog.e(TAG, "Subscribe ability, ability is disconnected");
            result.error(2005, "Subscribe ability, ability is disconnected");
            return;
        }
        abilityConnection.subscribeAbility(parsedJsRequest.getMessageCode(), eventNotifier, result, parsedJsRequest.getSyncOption());
    }

    private void subscribeInternalAbility(ParsedJsRequest parsedJsRequest, EventNotifier eventNotifier, Result result) {
        ALog.d(TAG, "subscribe internal ability");
        AceInternalAbility.AceInternalAbilityHandler aceInternalAbilityHandler = AbilityManager.getInstance().getInternalAbilityHandlers().get(parsedJsRequest.getBundleAndAbilityName());
        if (aceInternalAbilityHandler != null) {
            Map<String, AbilityProxy> internalAbilityProxy = AbilityManager.getInstance().getInternalAbilityProxy();
            AbilityProxy abilityProxy = internalAbilityProxy.get(parsedJsRequest.getBundleAndAbilityName());
            if (abilityProxy == null) {
                abilityProxy = new AbilityProxy(aceInternalAbilityHandler);
                internalAbilityProxy.put(parsedJsRequest.getBundleAndAbilityName(), abilityProxy);
            }
            AbilityStub abilityStub = new AbilityStub(eventNotifier);
            if (abilityProxy.subscribeAbility(parsedJsRequest.getMessageCode(), abilityStub, result, parsedJsRequest.getSyncOption())) {
                AbilityManager.getInstance().getInternalAbilityStub().put(parsedJsRequest.getBundleAndAbilityName(), abilityStub);
                return;
            }
            return;
        }
        ALog.e(TAG, "Subscribe internal ability, internal ability not register.");
        result.error(2004, "Internal ability not register.");
    }

    private void getAndUnsubscribeAbility(ParsedJsRequest parsedJsRequest, EventNotifier eventNotifier, Result result, int i) {
        ALog.d(TAG, "Ability already connected, get connection and unsubscribe ability");
        AbilityConnection abilityConnection = AbilityManager.getInstance().getAbilityConnection(i, parsedJsRequest.getBundleAndAbilityName());
        if (abilityConnection == null || !abilityConnection.isAvailable()) {
            ALog.e(TAG, "Unsubscribe ability, ability is disconnected");
            result.error(2005, "Unsubscribe ability, ability is disconnected");
            return;
        }
        abilityConnection.unsubscribeAbility(parsedJsRequest.getMessageCode(), result, parsedJsRequest.getSyncOption());
    }

    private void unsubscribeInternalAbility(ParsedJsRequest parsedJsRequest, EventNotifier eventNotifier, Result result) {
        ALog.d(TAG, "unsubscribe internal ability");
        AceInternalAbility.AceInternalAbilityHandler aceInternalAbilityHandler = AbilityManager.getInstance().getInternalAbilityHandlers().get(parsedJsRequest.getBundleAndAbilityName());
        if (aceInternalAbilityHandler != null) {
            Map<String, AbilityProxy> internalAbilityProxy = AbilityManager.getInstance().getInternalAbilityProxy();
            AbilityProxy abilityProxy = internalAbilityProxy.get(parsedJsRequest.getBundleAndAbilityName());
            if (abilityProxy == null) {
                abilityProxy = new AbilityProxy(aceInternalAbilityHandler);
                internalAbilityProxy.put(parsedJsRequest.getBundleAndAbilityName(), abilityProxy);
            }
            Map<String, AbilityStub> internalAbilityStub = AbilityManager.getInstance().getInternalAbilityStub();
            AbilityStub abilityStub = internalAbilityStub.get(parsedJsRequest.getBundleAndAbilityName());
            if (abilityProxy.unsubscribeAbility(parsedJsRequest.getMessageCode(), abilityStub, result, parsedJsRequest.getSyncOption())) {
                abilityStub.setEventNotifier(null);
                internalAbilityStub.remove(parsedJsRequest.getBundleAndAbilityName());
                return;
            }
            return;
        }
        ALog.e(TAG, "Unsubscribe internal ability, internal ability not register.");
        result.error(2004, "Unsubscribe internal ability, internal ability not register.");
    }
}
