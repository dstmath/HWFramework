package ohos.miscservices.httpaccess;

import com.huawei.ace.plugin.ErrorCode;
import com.huawei.ace.plugin.Function;
import com.huawei.ace.plugin.ModuleGroup;
import com.huawei.ace.plugin.Result;
import java.util.HashSet;
import java.util.Set;
import ohos.ace.ability.AceAbility;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.httpaccess.data.RequestData;
import ohos.miscservices.httpaccess.data.ResponseData;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.JSONObject;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;
import ohos.utils.zson.ZSONObject;

public class HttpAccessPlugin implements ModuleGroup.ModuleGroupHandler, ErrorCode {
    private static final String COMMAND_DOWNLOAD = "download";
    private static final String COMMAND_FETCH = "fetch";
    private static final String COMMAND_ONDOWNLOADCOMPLETE = "onDownloadComplete";
    private static final String COMMAND_UPLOAD = "upload";
    private static final String MODULE_GROUP_NAME = "AceModuleGroup/HttpAccess";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "HttpAccessPlugin");
    private static HttpAccessPlugin instance;
    private Context applicationContext;

    public static String getJsCode() {
        return "var fetch = {\n    fetch : async function fetch(param) {\n        var httpModuleGroup = null;\n        if (httpModuleGroup == null) {\n            httpModuleGroup = ModuleGroup.getGroup(\"AceModuleGroup/HttpAccess\");\n        }\n        return await catching(httpModuleGroup.callNative(\"fetch\", param), param);\n    }\n};\nvar request = {\n    onDownloadComplete : async function onDownloadComplete(param) {\n        var httpModuleGroup = null;\n        if (httpModuleGroup == null) {\n            httpModuleGroup = ModuleGroup.getGroup(\"AceModuleGroup/HttpAccess\");\n        }\n        return await catching(httpModuleGroup.callNative(\"onDownloadComplete\", param), param);\n    },\n    upload : async function upload(param) {\n        var httpModuleGroup = null;\n        if (httpModuleGroup == null) {\n            httpModuleGroup = ModuleGroup.getGroup(\"AceModuleGroup/HttpAccess\");\n        }\n        return await catching(httpModuleGroup.callNative(\"upload\", param), param);\n    },\n    download : async function download(param) {\n        var httpModuleGroup = null;\n        if (httpModuleGroup == null) {\n            httpModuleGroup = ModuleGroup.getGroup(\"AceModuleGroup/HttpAccess\");\n        }\n        return await catching(httpModuleGroup.callNative(\"download\", param), param);\n    }\n};\nglobal.systemplugin.fetch = fetch;\nglobal.systemplugin.request = request;";
    }

    public void onFunctionCall(Function function, final Result result) {
        RequestData requestData;
        HiLog.debug(TAG, "enter httpaccess function!", new Object[0]);
        Object obj = function.arguments.get(0);
        try {
            requestData = (RequestData) ZSONObject.stringToClass(obj instanceof String ? (String) obj : "", RequestData.class);
        } catch (JSONException unused) {
            HiLog.error(TAG, "parse json error", new Object[0]);
            requestData = null;
        }
        if (requestData == null) {
            result.error(202, "request param is illegal");
            return;
        }
        if (COMMAND_FETCH.equals(function.name)) {
            HiLog.debug(TAG, "enter fetch command", new Object[0]);
            new HttpAccess().fetch(requestData, new HttpProbe() {
                /* class ohos.miscservices.httpaccess.HttpAccessPlugin.AnonymousClass1 */

                @Override // ohos.miscservices.httpaccess.HttpProbe
                public void onResponse(ResponseData responseData) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("code", (Object) Integer.valueOf(responseData.getCode()));
                    jSONObject.put("data", (Object) responseData.getData());
                    if (responseData.getCode() == 200) {
                        jSONObject.put("headers", (Object) JSON.toJSONString(responseData.getHeaders()));
                        result.success(jSONObject);
                        return;
                    }
                    HiLog.error(HttpAccessPlugin.TAG, "fetch data error!", new Object[0]);
                    result.error(200, jSONObject);
                }
            }, this.applicationContext);
        }
        if (COMMAND_DOWNLOAD.equals(function.name)) {
            HiLog.debug(TAG, "enter download command", new Object[0]);
            new HttpAccess().download(requestData, new HttpProbe() {
                /* class ohos.miscservices.httpaccess.HttpAccessPlugin.AnonymousClass2 */

                @Override // ohos.miscservices.httpaccess.HttpProbe
                public void onResponse(ResponseData responseData) {
                    if (responseData.getCode() == 8) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("token", (Object) Long.valueOf(responseData.getToken()));
                        result.success(jSONObject);
                        return;
                    }
                    HiLog.error(HttpAccessPlugin.TAG, "download error!", new Object[0]);
                    result.error(400, "Download error!");
                }
            }, this.applicationContext);
        }
        if (COMMAND_ONDOWNLOADCOMPLETE.equals(function.name)) {
            HiLog.debug(TAG, "enter onDownloadComplete command", new Object[0]);
            new HttpAccess().onDownloadComplete(requestData, new HttpProbe() {
                /* class ohos.miscservices.httpaccess.HttpAccessPlugin.AnonymousClass3 */

                @Override // ohos.miscservices.httpaccess.HttpProbe
                public void onResponse(ResponseData responseData) {
                    if (responseData.getCode() == 8) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("uri", (Object) responseData.getUri());
                        result.success(jSONObject);
                    } else if (responseData.getCode() == 401) {
                        HiLog.error(HttpAccessPlugin.TAG, "download task does not exsist!", new Object[0]);
                        result.error((int) SystemAbilityDefinition.BUNDLE_MGR_SERVICE_SYS_ABILITY_ID, "Download task does not exsist!");
                    } else {
                        HiLog.error(HttpAccessPlugin.TAG, "download error!", new Object[0]);
                        result.error(400, "Download error!");
                    }
                }
            }, this.applicationContext);
        }
        if (COMMAND_UPLOAD.equals(function.name)) {
            HiLog.debug(TAG, "enter upload command", new Object[0]);
            new HttpAccess().upload(requestData, new HttpProbe() {
                /* class ohos.miscservices.httpaccess.HttpAccessPlugin.AnonymousClass4 */

                @Override // ohos.miscservices.httpaccess.HttpProbe
                public void onResponse(ResponseData responseData) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("code", (Object) Integer.valueOf(responseData.getCode()));
                    jSONObject.put("data", (Object) responseData.getData());
                    if (responseData.getCode() == 200) {
                        jSONObject.put("headers", (Object) JSON.toJSONString(responseData.getHeaders()));
                        result.success(jSONObject);
                        return;
                    }
                    HiLog.error(HttpAccessPlugin.TAG, "upload error!", new Object[0]);
                    result.error(200, jSONObject);
                }
            }, this.applicationContext);
        }
    }

    public static void register(Context context) {
        instance = new HttpAccessPlugin();
        instance.onRegister(context);
        ModuleGroup.registerModuleGroup(MODULE_GROUP_NAME, instance, context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null);
    }

    private void onRegister(Context context) {
        this.applicationContext = context;
    }

    public static void deregister(Context context) {
        ModuleGroup.registerModuleGroup(MODULE_GROUP_NAME, (ModuleGroup.ModuleGroupHandler) null, context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null);
    }

    public static Set<String> getPluginGroup() {
        HashSet hashSet = new HashSet();
        hashSet.add(MODULE_GROUP_NAME);
        return hashSet;
    }
}
