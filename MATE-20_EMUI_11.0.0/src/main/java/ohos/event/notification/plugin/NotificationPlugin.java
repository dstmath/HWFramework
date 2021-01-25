package ohos.event.notification.plugin;

import com.huawei.ace.plugin.Function;
import com.huawei.ace.plugin.ModuleGroup;
import com.huawei.ace.plugin.Result;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.ace.ability.AceAbility;
import ohos.app.Context;
import ohos.bundle.ElementName;
import ohos.event.intentagent.IntentAgentConstant;
import ohos.event.intentagent.IntentAgentHelper;
import ohos.event.intentagent.IntentAgentInfo;
import ohos.event.notification.NotificationHelper;
import ohos.event.notification.NotificationRequest;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.utils.fastjson.JSONObject;

public final class NotificationPlugin implements ModuleGroup.ModuleGroupHandler {
    private static final int COMMON_ERROR_CODE = 200;
    private static final String FUNCTION_SHOW = "show";
    private static final String KEY_ABILITY_NAME = "abilityName";
    private static final String KEY_BUNDLE_NAME = "bundleName";
    private static final String KEY_CLICK_ACTION = "clickAction";
    private static final String KEY_TEXT = "contentText";
    private static final String KEY_TITLE = "contentTitle";
    private static final String KEY_URI = "uri";
    private static final String KEY_URL = "url";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "NotificationPlugin");
    private static final int REQUEST_CODE = 100;
    private static NotificationPlugin instance = null;
    private Ability ability;

    public static String getJsCode() {
        return "var nm = {\n    module: null,\n    onInit: function onInit() {\n        if (nm.module == null) {\n            nm.module = ModuleGroup.getGroup(\"AceModuleGroup/notification\");\n        }\n    },\n    show: async function show(param) {\n        nm.onInit();\n        return await catching(nm.module.callNative(\"show\", param), param);\n    },\n};\nglobal.systemplugin.notification = {\n    show: nm.show,\n};";
    }

    @Override // com.huawei.ace.plugin.ModuleGroup.ModuleGroupHandler
    public void onFunctionCall(Function function, Result result) {
        if (isValid(function, result)) {
            if (FUNCTION_SHOW.equals(function.name)) {
                showNotification(function, result);
            } else {
                result.notExistFunction();
            }
        }
    }

    public static void register(Context context) {
        instance = new NotificationPlugin();
        instance.onRegister(context);
        ModuleGroup.registerModuleGroup("AceModuleGroup/notification", instance, context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null);
    }

    public static void deregister(Context context) {
        ModuleGroup.registerModuleGroup("AceModuleGroup/notification", null, context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null);
    }

    public static Set<String> getPluginGroup() {
        HashSet hashSet = new HashSet();
        hashSet.add("AceModuleGroup/notification");
        return hashSet;
    }

    private void onRegister(Context context) {
        if (context instanceof Ability) {
            this.ability = (Ability) context;
        }
    }

    private void showNotification(Function function, Result result) {
        JSONObject jsonObject = getJsonObject(function);
        if (jsonObject == null) {
            result.error(200, "invalid argument when show notification");
            return;
        }
        String valueFromJson = getValueFromJson(jsonObject, KEY_TITLE);
        String valueFromJson2 = getValueFromJson(jsonObject, KEY_TEXT);
        if (valueFromJson == null || valueFromJson.isEmpty() || valueFromJson2 == null || valueFromJson2.isEmpty()) {
            result.error(200, "contentText or contextTitle is invalid");
            return;
        }
        NotificationRequest.NotificationNormalContent notificationNormalContent = new NotificationRequest.NotificationNormalContent();
        notificationNormalContent.setTitle(valueFromJson).setText(valueFromJson2);
        NotificationRequest.NotificationContent notificationContent = new NotificationRequest.NotificationContent(notificationNormalContent);
        NotificationRequest notificationRequest = new NotificationRequest(0);
        notificationRequest.setContent(notificationContent).setTapDismissed(true);
        JSONObject jSONObject = jsonObject.getJSONObject(KEY_CLICK_ACTION);
        if (jSONObject != null) {
            fillClickAction(jSONObject, notificationRequest);
        }
        try {
            NotificationHelper.publishNotification(notificationRequest);
        } catch (RemoteException unused) {
            result.error(200, "publish notification failed");
        }
        result.success("show success");
    }

    private void fillClickAction(JSONObject jSONObject, NotificationRequest notificationRequest) {
        String valueFromJson = getValueFromJson(jSONObject, "bundleName");
        String valueFromJson2 = getValueFromJson(jSONObject, KEY_ABILITY_NAME);
        if (valueFromJson != null && !valueFromJson.isEmpty() && valueFromJson2 != null && !valueFromJson2.isEmpty()) {
            String valueFromJson3 = getValueFromJson(jSONObject, "uri");
            ElementName createRelative = ElementName.createRelative(valueFromJson, valueFromJson2, "");
            Intent intent = new Intent();
            intent.setElement(createRelative);
            if (valueFromJson3 != null && !valueFromJson3.isEmpty()) {
                intent.setParam(KEY_URL, valueFromJson3);
            }
            ArrayList arrayList = new ArrayList();
            arrayList.add(intent);
            notificationRequest.setIntentAgent(IntentAgentHelper.getIntentAgent(this.ability, new IntentAgentInfo(100, IntentAgentConstant.OperationType.START_ABILITY, IntentAgentConstant.Flags.UPDATE_PRESENT_FLAG, arrayList, (IntentParams) null)));
        }
    }

    private JSONObject getJsonObject(Function function) {
        Object obj = function.arguments.get(0);
        if (obj == null || !(obj instanceof String)) {
            return null;
        }
        return JSONObject.parseObject((String) obj);
    }

    private String getValueFromJson(JSONObject jSONObject, String str) {
        Object obj = jSONObject.get(str);
        if (obj == null || !(obj instanceof String)) {
            return null;
        }
        return (String) obj;
    }

    private boolean isValid(Function function, Result result) {
        if (result == null) {
            HiLog.error(LABEL, "Result is null", new Object[0]);
            return false;
        } else if (function == null) {
            result.error(200, "Function is null");
            return false;
        } else if (function.name == null || function.arguments == null || function.arguments.isEmpty()) {
            result.error(200, "Function call is invalid");
            return false;
        } else if (this.ability != null) {
            return true;
        } else {
            result.error(200, "ability is unavailable");
            return false;
        }
    }
}
