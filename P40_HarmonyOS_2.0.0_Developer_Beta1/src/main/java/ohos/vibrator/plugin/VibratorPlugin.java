package ohos.vibrator.plugin;

import com.huawei.ace.plugin.EventGroup;
import com.huawei.ace.plugin.EventNotifier;
import com.huawei.ace.plugin.Function;
import com.huawei.ace.plugin.ModuleGroup;
import com.huawei.ace.plugin.Result;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ohos.ace.ability.AceAbility;
import ohos.app.Context;
import ohos.utils.zson.ZSONObject;
import ohos.vibrator.agent.VibratorAgent;

public class VibratorPlugin implements ModuleGroup.ModuleGroupHandler, EventGroup.EventGroupHandler {
    private static final String TAG = "VibratorPlugin#";
    private static final int VIBRATOR_DURATION_LONG = 1000;
    private static final int VIBRATOR_DURATION_SHORT = 35;
    private static final String VIBRATOR_MODE_LONG = "long";
    private static final String VIBRATOR_MODE_SHORT = "short";
    private static VibratorPlugin instance;
    private VibratorAgent vibratorAgent = new VibratorAgent();

    public static String getJsCode() {
        return "var vibrator = {\n    vibratorModuleGroup: null,\n    vibrate: async function vibrate(param) {\n    console.info(\"into vibrator vibrate\");\n    if (param.mode != \"short\" && param.mode != \"long\") {\n        console.error(\"input parameter error\");\n        return false;\n    }\n    try {\n        if (vibrator.vibratorModuleGroup == null) {\n            vibrator.vibratorModuleGroup = ModuleGroup.getGroup(\"AceModuleGroup/Vibrator\");\n        }\n        if (vibrator.vibratorModuleGroup != null) {\n            var ret = await vibrator.vibratorModuleGroup.callNative(\"vibrate\", param);\n            var retJson = JSON.parse(ret);\n            if (retJson.code == 0) {\n                console.error(\"vibrate success:\" + JSON.stringify(retJson));\n                console.error(\"vibrate result is: \" + JSON.stringify(retJson.data));\n                vibrator.vibrateResult = JSON.stringify(retJson.data);\n                param.success(retJson);\n            } else {\n                param.fail(retJson);\n                console.error(\"User error code:\" + retJson.code);\n                console.error(\"User error message:\" + JSON.stringify(retJson.data));\n            }\n        }\n    } catch (pluginError) {\n        var pluginErrorJson = JSON.parse(pluginError);\n        console.error(\"Plugin error code:\" + pluginErrorJson.code);\n        console.error(\"Plugin error message:\" + JSON.stringify(pluginErrorJson.data));\n    }\n    param.complete(retJson);\n}\n};\nglobal.systemplugin.vibrator = {\n    vibrate: vibrator.vibrate\n};";
    }

    public void onSubscribe(List<Object> list, EventNotifier eventNotifier, Result result) {
    }

    public void onUnsubscribe(List<Object> list, Result result) {
    }

    public static void register(Context context) {
        instance = new VibratorPlugin();
        ModuleGroup.registerModuleGroup("AceModuleGroup/Vibrator", instance, context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null);
    }

    public static void deregister(Context context) {
        ModuleGroup.registerModuleGroup("AceModuleGroup/Vibrator", (ModuleGroup.ModuleGroupHandler) null, context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null);
    }

    public static Set<String> getPluginGroup() {
        HashSet hashSet = new HashSet();
        hashSet.add("AceModuleGroup/Vibrator");
        return hashSet;
    }

    public void onFunctionCall(Function function, Result result) {
        int i;
        LogUtil.info(TAG, "onFunctionCall");
        Object obj = function.arguments.get(0);
        String str = obj instanceof String ? (String) obj : "";
        LogUtil.info(TAG, "jsonStr" + str);
        String mode = ((VibratorMode) ZSONObject.stringToClass(str, VibratorMode.class)).getMode();
        LogUtil.info(TAG, "vibrator mode is: " + mode);
        if (VIBRATOR_MODE_SHORT.equals(mode)) {
            i = 35;
        } else if (VIBRATOR_MODE_LONG.equals(mode)) {
            i = 1000;
        } else {
            LogUtil.error(TAG, "input mode is invalid!");
            return;
        }
        if (function.name.equals("vibrate")) {
            List<Integer> vibratorIdList = this.vibratorAgent.getVibratorIdList();
            if (!vibratorIdList.isEmpty()) {
                this.vibratorAgent.startOnce(vibratorIdList.get(0).intValue(), i);
                return;
            }
            return;
        }
        result.notExistFunction();
    }
}
