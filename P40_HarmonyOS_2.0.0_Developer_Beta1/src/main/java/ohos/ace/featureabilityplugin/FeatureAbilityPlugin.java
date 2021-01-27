package ohos.ace.featureabilityplugin;

import com.huawei.ace.plugin.EventGroup;
import com.huawei.ace.plugin.EventNotifier;
import com.huawei.ace.plugin.Function;
import com.huawei.ace.plugin.ModuleGroup;
import com.huawei.ace.plugin.Result;
import com.huawei.ace.runtime.ALog;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ohos.ace.ability.AceAbility;
import ohos.ace.featureabilityplugin.requestprocess.CallAbilityProcess;
import ohos.ace.featureabilityplugin.requestprocess.ContinueAbilityProcess;
import ohos.ace.featureabilityplugin.requestprocess.FinishAbilityProcess;
import ohos.ace.featureabilityplugin.requestprocess.StartAbilityProcess;
import ohos.ace.featureabilityplugin.requestprocess.SubscribeAbilityProcess;
import ohos.app.AbilityContext;
import ohos.app.Context;
import ohos.tools.Bytrace;

public class FeatureAbilityPlugin implements ModuleGroup.ModuleGroupHandler, EventGroup.EventGroupHandler {
    private static final String TAG = FeatureAbilityPlugin.class.getSimpleName();
    private static FeatureAbilityPlugin instance;
    private AbilityContext abilityContext;
    private EventNotifier eventsNotifier;

    public static void register(Context context) {
        if (!(context instanceof AbilityContext)) {
            ALog.e(TAG, "context is not instance of AbilityContext, register failed");
            return;
        }
        instance = new FeatureAbilityPlugin();
        instance.onRegister((AbilityContext) context);
        Integer valueOf = context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null;
        ModuleGroup.registerModuleGroup("AceInternalModuleGroup/FeatureAbility", instance, valueOf);
        EventGroup.registerEventGroup("AceInternalEventGroup/FeatureAbility", instance, valueOf);
    }

    public static void deregister(Context context) {
        Integer valueOf = context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null;
        if (valueOf != null) {
            AbilityManager.getInstance().removeConnectionsByAbilityId(valueOf.intValue());
        }
        ModuleGroup.registerModuleGroup("AceInternalModuleGroup/FeatureAbility", null, valueOf);
        EventGroup.registerEventGroup("AceInternalEventGroup/FeatureAbility", null, valueOf);
    }

    private void onRegister(AbilityContext abilityContext2) {
        this.abilityContext = abilityContext2;
    }

    public static Set<String> getPluginGroup() {
        HashSet hashSet = new HashSet();
        hashSet.add("AceInternalModuleGroup/FeatureAbility");
        hashSet.add("AceInternalEventGroup/FeatureAbility");
        return hashSet;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // com.huawei.ace.plugin.ModuleGroup.ModuleGroupHandler
    public void onFunctionCall(Function function, Result result) {
        char c;
        Bytrace.startTrace(549755813888L, "FA-onFunctionCall");
        ALog.d(TAG, "onFunctionCall into the function call");
        String str = function.name;
        switch (str.hashCode()) {
            case -2077801098:
                if (str.equals("finishWithResult")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1946950013:
                if (str.equals("continueAbility")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1613951764:
                if (str.equals("callAbility")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1334954392:
                if (str.equals("startAbility")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1553360062:
                if (str.equals("startAbilityForResult")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            CallAbilityProcess.getInstance().callAbility(this.abilityContext, function.arguments, result);
        } else if (c == 1) {
            StartAbilityProcess.getInstance().startAbility(this.abilityContext, function.arguments, result, false);
        } else if (c == 2) {
            StartAbilityProcess.getInstance().startAbility(this.abilityContext, function.arguments, result, true);
        } else if (c == 3) {
            FinishAbilityProcess.getInstance().finishAbilityWithResult(this.abilityContext, function.arguments, result);
        } else if (c != 4) {
            result.notExistFunction();
        } else {
            ContinueAbilityProcess.getInstance().continueAbility(this.abilityContext, result);
        }
        Bytrace.finishTrace(549755813888L, "FA-onFunctionCall");
    }

    @Override // com.huawei.ace.plugin.EventGroup.EventGroupHandler
    public void onSubscribe(List<Object> list, EventNotifier eventNotifier, Result result) {
        ALog.d(TAG, "onSubscribe into the function callname");
        this.eventsNotifier = eventNotifier;
        SubscribeAbilityProcess.getInstance().subscribeAbility(this.abilityContext, list, eventNotifier, result);
    }

    @Override // com.huawei.ace.plugin.EventGroup.EventGroupHandler
    public void onUnsubscribe(List<Object> list, Result result) {
        ALog.d(TAG, "onUnsubscribe into the function callname");
        AbilityContext abilityContext2 = this.abilityContext;
        if (!(abilityContext2 instanceof AceAbility)) {
            ALog.e(TAG, "unsubscribe ability context is not AceAbility");
            result.error(2001, "unsubscribe ability context is not AceAbility");
            return;
        }
        SubscribeAbilityProcess.getInstance().unsubscribeAbility(list, this.eventsNotifier, result, ((AceAbility) abilityContext2).getAbilityId());
    }
}
