package ohos.data.jsstorage;

import com.huawei.ace.plugin.ErrorCode;
import com.huawei.ace.plugin.Function;
import com.huawei.ace.plugin.ModuleGroup;
import com.huawei.ace.plugin.Result;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import ohos.ace.ability.AceAbility;
import ohos.app.Context;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import ohos.data.DatabaseHelper;
import ohos.data.rdb.RawRdbPredicates;
import ohos.data.rdb.RdbOpenCallback;
import ohos.data.rdb.RdbPredicates;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.StoreConfig;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.fastjson.JSONObject;

public class StoragePlugin implements ModuleGroup.ModuleGroupHandler, ErrorCode {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS kv (key TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL)";
    private static final String DATABASE_NAME = "js_storage.db";
    private static final int DATABASE_VERSION = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109536, "StoragePlugin");
    private static final String MODULE_GROUP_NAME = "AceModuleGroup/Storage";
    private static final String TABLE_NAME = "kv";
    private static Map<Integer, StoragePlugin> instanceMap = new ConcurrentHashMap();
    private RdbOpenCallback callback = new RdbOpenCallback() {
        /* class ohos.data.jsstorage.StoragePlugin.AnonymousClass1 */

        @Override // ohos.data.rdb.RdbOpenCallback
        public void onUpgrade(RdbStore rdbStore, int i, int i2) {
        }

        @Override // ohos.data.rdb.RdbOpenCallback
        public void onCreate(RdbStore rdbStore) {
            rdbStore.executeSql(StoragePlugin.CREATE_TABLE);
        }
    };
    private Context context;
    private final Object lock = new Object();
    private RdbStore rdbStore;

    private StoragePlugin(Context context2) {
        this.context = context2;
    }

    @Override // com.huawei.ace.plugin.ModuleGroup.ModuleGroupHandler
    public void onFunctionCall(Function function, Result result) {
        if (result == null) {
            HiLog.error(LABEL, "parameter result is null when onFunctionCall", new Object[0]);
        } else if (function == null) {
            HiLog.error(LABEL, "parameter function is null when onFunctionCall", new Object[0]);
            result.error(200, "function is null");
        } else {
            try {
                if ("set".equals(function.name)) {
                    set(function, result);
                } else if ("get".equals(function.name)) {
                    get(function, result);
                } else if ("delete".equals(function.name)) {
                    delete(function, result);
                } else if (Constants.CLEAR_ATTRIBUTES.equals(function.name)) {
                    clear(result);
                } else {
                    result.notExistFunction();
                }
            } catch (IllegalArgumentException e) {
                HiLog.error(LABEL, "IllegalArgumentException occur when %{public}s data, eMsg:%{public}s", new Object[]{function.name, e.getMessage()});
                result.error(200, "Failed to " + function.name + " data");
            } catch (RuntimeException e2) {
                HiLog.error(LABEL, "RuntimeException occur when %{public}s data, eMsg:%{public}s", new Object[]{function.name, e2.getMessage()});
                result.error(200, "Failed to " + function.name + " data");
            }
        }
    }

    private JSONObject getJsonObject(Function function) {
        List<Object> list = function.arguments;
        if (list == null || list.size() < 0) {
            return null;
        }
        Object obj = list.get(0);
        if (!(obj instanceof String)) {
            return null;
        }
        return JSONObject.parseObject((String) obj);
    }

    private String getValueFromJson(JSONObject jSONObject, String str) {
        Object obj = jSONObject.get(str);
        if (!(obj instanceof String)) {
            return null;
        }
        return (String) obj;
    }

    private void set(Function function, Result result) {
        boolean z;
        JSONObject jsonObject = getJsonObject(function);
        if (jsonObject == null) {
            result.error(202, "key and value are not available when set data");
            return;
        }
        String valueFromJson = getValueFromJson(jsonObject, "key");
        if (valueFromJson == null || valueFromJson.isEmpty()) {
            result.error(202, "key is not available when set data");
            return;
        }
        String valueFromJson2 = getValueFromJson(jsonObject, "value");
        if (valueFromJson2 == null) {
            result.error(202, "value is not available when set data");
            return;
        }
        initRdbStore();
        if (valueFromJson2.isEmpty()) {
            z = deleteData(valueFromJson);
        } else {
            z = replaceData(valueFromJson, valueFromJson2);
        }
        if (z) {
            result.success("set data success");
        } else {
            result.error(200, "Failed to set data");
        }
    }

    private void get(Function function, Result result) {
        JSONObject jsonObject = getJsonObject(function);
        if (jsonObject == null) {
            result.error(202, "key and default are not available when get data");
            return;
        }
        String valueFromJson = getValueFromJson(jsonObject, "key");
        if (valueFromJson == null || valueFromJson.isEmpty()) {
            result.error(202, "key is not available when get data");
            return;
        }
        initRdbStore();
        String data = getData(valueFromJson);
        if (data == null || data.isEmpty()) {
            Object obj = jsonObject.get("default");
            if (obj == null) {
                data = "";
            } else if (!(obj instanceof String)) {
                result.error(202, "default is not available when get data");
                return;
            } else {
                data = (String) obj;
            }
        }
        result.success(data);
    }

    private void delete(Function function, Result result) {
        JSONObject jsonObject = getJsonObject(function);
        if (jsonObject == null) {
            result.error(202, "key and default are not available when delete data");
            return;
        }
        String valueFromJson = getValueFromJson(jsonObject, "key");
        if (valueFromJson == null || valueFromJson.isEmpty()) {
            result.error(202, "key is not available when delete data");
            return;
        }
        initRdbStore();
        if (deleteData(valueFromJson)) {
            result.success("delete data success");
        } else {
            result.error(200, "Failed to delete data");
        }
    }

    private void clear(Result result) {
        initRdbStore();
        if (this.rdbStore.delete(new RawRdbPredicates(TABLE_NAME, null, null)) >= 0) {
            result.success("clear data success");
        } else {
            result.error(200, "Failed to clear data");
        }
    }

    private String getData(String str) {
        RdbPredicates rdbPredicates = new RdbPredicates(TABLE_NAME);
        rdbPredicates.equalTo("key", str);
        ResultSet queryByStep = this.rdbStore.queryByStep(rdbPredicates, null);
        if (queryByStep == null) {
            return "";
        }
        if (!queryByStep.goToFirstRow()) {
            queryByStep.close();
            return "";
        }
        String string = queryByStep.getString(queryByStep.getColumnIndexForName("value"));
        queryByStep.close();
        return string;
    }

    private boolean deleteData(String str) {
        RdbPredicates rdbPredicates = new RdbPredicates(TABLE_NAME);
        rdbPredicates.equalTo("key", str);
        return this.rdbStore.delete(rdbPredicates) >= 0;
    }

    private boolean replaceData(String str, String str2) {
        ValuesBucket valuesBucket = new ValuesBucket();
        valuesBucket.putString("key", str);
        valuesBucket.putString("value", str2);
        return this.rdbStore.replace(TABLE_NAME, valuesBucket) >= 0;
    }

    private void initRdbStore() {
        synchronized (this.lock) {
            if (this.rdbStore == null) {
                this.rdbStore = new DatabaseHelper(this.context).getRdbStore(StoreConfig.newDefaultConfig(DATABASE_NAME), 1, this.callback, null);
            }
        }
    }

    private void onDeregister() {
        synchronized (this.lock) {
            if (this.rdbStore != null) {
                this.rdbStore.close();
            }
        }
    }

    public static Set<String> getPluginGroup() {
        HashSet hashSet = new HashSet();
        hashSet.add(MODULE_GROUP_NAME);
        return hashSet;
    }

    public static void register(Context context2) {
        if (context2 == null) {
            HiLog.error(LABEL, "Context may not be null when register StoragePlugin", new Object[0]);
        } else if (!(context2 instanceof AceAbility)) {
            HiLog.error(LABEL, "Failed to get abilityId when register StoragePlugin", new Object[0]);
        } else {
            int abilityId = ((AceAbility) context2).getAbilityId();
            HiLog.debug(LABEL, "register abilityId:%{public}d", new Object[]{Integer.valueOf(abilityId)});
            StoragePlugin storagePlugin = new StoragePlugin(context2);
            instanceMap.put(Integer.valueOf(abilityId), storagePlugin);
            ModuleGroup.registerModuleGroup(MODULE_GROUP_NAME, storagePlugin, Integer.valueOf(abilityId));
        }
    }

    public static void deregister(Context context2) {
        if (!(context2 instanceof AceAbility)) {
            HiLog.error(LABEL, "Failed to get abilityId when deregister StoragePlugin", new Object[0]);
            return;
        }
        int abilityId = ((AceAbility) context2).getAbilityId();
        HiLog.debug(LABEL, "deregister abilityId:%{public}d", new Object[]{Integer.valueOf(abilityId)});
        StoragePlugin storagePlugin = instanceMap.get(Integer.valueOf(abilityId));
        instanceMap.remove(Integer.valueOf(abilityId));
        if (storagePlugin != null) {
            storagePlugin.onDeregister();
        }
        ModuleGroup.registerModuleGroup(MODULE_GROUP_NAME, null, Integer.valueOf(abilityId));
    }

    public static String getJsCode() {
        HiLog.debug(LABEL, "getJsCode start", new Object[0]);
        return "var storage = {\n    moduleGroup: null,\n    onInit: function onInit() {\n        if (storage.moduleGroup == null) {\n            storage.moduleGroup = ModuleGroup.getGroup('AceModuleGroup/Storage');\n        }\n    },\n    get: async function get(param) {\n        storage.onInit();\n        return await catching(storage.moduleGroup.callNative('get', param), param);\n    },\n    set: async function set(param) {\n        storage.onInit();\n        return await catching(storage.moduleGroup.callNative('set', param), param);\n    },\n    delete: async function remove(param) {\n        storage.onInit();\n        return await catching(storage.moduleGroup.callNative('delete', param), param);\n    },\n    clear: async function clear(param) {\n        storage.onInit();\n        return await catching(storage.moduleGroup.callNative('clear'), param);\n    }\n};\nglobal.systemplugin.storage = {\n    get: storage.get,\n    set: storage.set,\n    delete: storage.delete,\n    clear: storage.clear,\n};";
    }
}
