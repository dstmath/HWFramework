package com.huawei.ace.systemplugin.shortcut;

import com.huawei.ace.plugin.ErrorCode;
import com.huawei.ace.plugin.Function;
import com.huawei.ace.plugin.ModuleGroup;
import com.huawei.ace.plugin.Result;
import com.huawei.ace.systemplugin.LogUtil;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import ohos.ace.ability.AceAbility;
import ohos.app.Context;
import ohos.bundle.AbilityInfo;
import ohos.bundle.IBundleManager;
import ohos.bundle.ShortcutInfo;
import ohos.bundle.ShortcutIntent;
import ohos.global.resource.Element;
import ohos.global.resource.Entry;
import ohos.global.resource.NotExistException;
import ohos.global.resource.RawFileEntry;
import ohos.global.resource.Resource;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.utils.fastjson.JSONObject;

public class ShortcutPlugin implements ModuleGroup.ModuleGroupHandler, ErrorCode {
    private static final String LOG_TAG = "ShortcutPlugin#";
    private static ShortcutPlugin instance;
    private Context applicationContext;

    public static String getJsCode() {
        return "var catching = global.systemplugin.catching;var shortcut = {    module: null,    onInit: function onInit() {        if (shortcut.module == null) {            shortcut.module = ModuleGroup.getGroup(\"AceModuleGroup/Shortcut\");        }    },    create: async function create(param) {        shortcut.onInit();        return await catching(shortcut.module.callNative(\"create\",            param.page == undefined ? null : param.page,            param.params == undefined ? null : param.params,            param.label == undefined ? null : param.label,            param.description == undefined ? null : param.description,            param.icon == undefined ? null : param.icon,            param.bundleName == undefined ? null : param.bundleName,            param.abilityName == undefined ? null : param.abilityName,            param.updateIfExists == undefined ? null : param.updateIfExists), param);    },    hasCreated: async function hasCreated(param) {        shortcut.onInit();        return await catching(shortcut.module.callNative(\"hasCreated\",             param.abililtyName == undefined ? null : param.abililtyName,            param.page == undefined ? null : param.page), param);    }};global.systemplugin.shortcut = shortcut;";
    }

    public static void register(Context context) {
        instance = new ShortcutPlugin();
        instance.onRegister(context);
        ModuleGroup.registerModuleGroup("AceModuleGroup/Shortcut", instance, context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null);
    }

    public static void deregister(Context context) {
        ModuleGroup.registerModuleGroup("AceModuleGroup/Shortcut", null, context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null);
    }

    public static Set<String> getPluginGroup() {
        HashSet hashSet = new HashSet();
        hashSet.add("AceModuleGroup/Shortcut");
        return hashSet;
    }

    private void onRegister(Context context) {
        this.applicationContext = context;
    }

    /* access modifiers changed from: private */
    public static class ShortcutInstallParam {
        private String bundleName;
        private String className;
        private String description;
        private String icon;
        private String label;
        private String page;
        private String params;
        private boolean updateIfExists;

        private ShortcutInstallParam() {
            this.updateIfExists = false;
        }

        /* access modifiers changed from: package-private */
        public String getLabel() {
            return this.label;
        }

        /* access modifiers changed from: package-private */
        public void setLabel(String str) {
            this.label = str;
        }

        /* access modifiers changed from: package-private */
        public String getDescription() {
            return this.description;
        }

        /* access modifiers changed from: package-private */
        public void setDescription(String str) {
            this.description = str;
        }

        /* access modifiers changed from: package-private */
        public String getPage() {
            return this.page;
        }

        /* access modifiers changed from: package-private */
        public void setPage(String str) {
            this.page = str;
        }

        /* access modifiers changed from: package-private */
        public String getParams() {
            return this.params;
        }

        /* access modifiers changed from: package-private */
        public void setParams(String str) {
            this.params = str;
        }

        /* access modifiers changed from: package-private */
        public String getIcon() {
            return this.icon;
        }

        /* access modifiers changed from: package-private */
        public void setIcon(String str) {
            this.icon = str;
        }

        /* access modifiers changed from: package-private */
        public String getBundleName() {
            return this.bundleName;
        }

        /* access modifiers changed from: package-private */
        public void setBundleName(String str) {
            this.bundleName = str;
        }

        /* access modifiers changed from: package-private */
        public String getClassName() {
            return this.className;
        }

        /* access modifiers changed from: package-private */
        public void setClassName(String str) {
            this.className = str;
        }

        /* access modifiers changed from: package-private */
        public boolean isUpdateIfExists() {
            return this.updateIfExists;
        }

        /* access modifiers changed from: package-private */
        public void setUpdateIfExists(boolean z) {
            this.updateIfExists = z;
        }
    }

    @Override // com.huawei.ace.plugin.ModuleGroup.ModuleGroupHandler
    public void onFunctionCall(Function function, Result result) {
        if (function != null && function.name != null) {
            boolean z = true;
            if ("create".equals(function.name)) {
                if (!isPinShortcutSupported()) {
                    result.error(200, "CREATE_SHORTCUT_NOT_SUPPORTED");
                    return;
                }
                ShortcutInstallParam shortcutInstallParam = new ShortcutInstallParam();
                shortcutInstallParam.setPage(getStringArg(function, 0));
                shortcutInstallParam.setParams(getStringArg(function, 1));
                shortcutInstallParam.setLabel(getStringArg(function, 2));
                shortcutInstallParam.setDescription(getStringArg(function, 3));
                shortcutInstallParam.setIcon(composeIconPath(getStringArg(function, 4)));
                shortcutInstallParam.setBundleName(getStringArg(function, 5));
                shortcutInstallParam.setClassName(getStringArg(function, 6));
                shortcutInstallParam.setUpdateIfExists(getBooleanArg(function, 7));
                if (createShortcut(shortcutInstallParam)) {
                    result.success(null);
                } else {
                    result.error(200, "FAILED_CREATE_SHORTCUT");
                }
            } else if ("hasCreated".equals(function.name)) {
                int hasInstalled = hasInstalled(getStringArg(function, 0), getStringArg(function, 1));
                if (hasInstalled == 2) {
                    result.error(200, "FAILED_QUERY_SHORTCUT");
                    return;
                }
                JSONObject jSONObject = new JSONObject();
                if (hasInstalled != 0) {
                    z = false;
                }
                jSONObject.put("exists", Boolean.valueOf(z));
                result.success(jSONObject);
            } else {
                result.notExistFunction();
            }
        }
    }

    private String getStringArg(Function function, int i) {
        if (function.arguments == null || function.arguments.size() <= i || i < 0) {
            return null;
        }
        Object obj = function.arguments.get(i);
        if (obj instanceof String) {
            return (String) obj;
        }
        return null;
    }

    private boolean getBooleanArg(Function function, int i) {
        if (function.arguments == null || function.arguments.size() <= i || i < 0) {
            return false;
        }
        Object obj = function.arguments.get(i);
        if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        }
        return false;
    }

    private boolean isPinShortcutSupported() {
        IBundleManager bundleManager = this.applicationContext.getBundleManager();
        if (bundleManager != null) {
            return bundleManager.isHomeShortcutSupported();
        }
        return false;
    }

    private boolean createShortcut(ShortcutInstallParam shortcutInstallParam) {
        Context context = this.applicationContext;
        if (context == null) {
            return false;
        }
        AbilityInfo abilityInfo = context.getAbilityInfo();
        ResourceManager resourceManager = this.applicationContext.getResourceManager();
        IBundleManager bundleManager = this.applicationContext.getBundleManager();
        if (resourceManager == null || abilityInfo == null || bundleManager == null) {
            return false;
        }
        ShortcutInfo shortcutInfo = new ShortcutInfo();
        String bundleName = shortcutInstallParam.getBundleName() == null ? abilityInfo.getBundleName() : shortcutInstallParam.getBundleName();
        String fullClassName = getFullClassName(bundleName, shortcutInstallParam.getClassName() == null ? abilityInfo.getClassName() : shortcutInstallParam.getClassName());
        shortcutInfo.setId(composeShortcutId(fullClassName, shortcutInstallParam.getPage()));
        shortcutInfo.setLabel(getLabel(shortcutInstallParam.getLabel(), abilityInfo.getLabelId(), resourceManager));
        ShortcutIntent shortcutIntent = new ShortcutIntent(bundleName, fullClassName);
        if (shortcutInstallParam.getPage() != null) {
            shortcutIntent.addParam("url", shortcutInstallParam.getPage());
        }
        if (shortcutInstallParam.getParams() != null) {
            shortcutIntent.addParam("__startParams", shortcutInstallParam.getParams());
        }
        shortcutInfo.setIntents(Arrays.asList(shortcutIntent));
        shortcutInfo.setHostAbilityName(abilityInfo.getClassName());
        shortcutInfo.setBundleName(abilityInfo.getBundleName());
        Resource resource = null;
        if (shortcutInstallParam.getIcon() != null) {
            resource = createResourceFromRawFile(resourceManager, shortcutInstallParam.getIcon());
            shortcutInfo.setIconStream(resource);
        } else {
            shortcutInfo.setShortcutIconId(abilityInfo.getIconId());
        }
        try {
            LogUtil.info(LOG_TAG, "Create shortcut with id " + shortcutInfo.getId());
            if (shortcutInstallParam.isUpdateIfExists() && hasInstalled(fullClassName, shortcutInstallParam.getPage()) == 0) {
                return bundleManager.updateShortcuts(Arrays.asList(shortcutInfo));
            }
            boolean addHomeShortcut = bundleManager.addHomeShortcut(shortcutInfo);
            closeQuietly(resource);
            return addHomeShortcut;
        } finally {
            closeQuietly(resource);
        }
    }

    private String getLabel(String str, int i, ResourceManager resourceManager) {
        if (str != null) {
            return str;
        }
        try {
            Element element = resourceManager.getElement(i);
            if (element != null) {
                return element.getString();
            }
            return "NO_LABEL";
        } catch (IOException | NotExistException | WrongTypeException unused) {
            LogUtil.warn(LOG_TAG, "label id resource not found.");
            return "NO_LABEL";
        }
    }

    private Resource createResourceFromRawFile(ResourceManager resourceManager, String str) {
        RawFileEntry rawFileEntry = resourceManager.getRawFileEntry(str);
        if (rawFileEntry == null || rawFileEntry.getType() != Entry.Type.FILE) {
            LogUtil.info(LOG_TAG, "Cannot find icon file.");
            return null;
        }
        try {
            return rawFileEntry.openRawFile();
        } catch (IOException unused) {
            LogUtil.info(LOG_TAG, "Read icon IOException.");
            return null;
        }
    }

    private String composeIconPath(String str) {
        if (str == null || !(this.applicationContext instanceof AceAbility)) {
            return null;
        }
        return "js" + File.separator + ((AceAbility) this.applicationContext).getInstanceName() + File.separator + str;
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
                LogUtil.warn(LOG_TAG, "Close failed due to io exception.");
            }
        }
    }

    private int hasInstalled(String str, String str2) {
        Context context = this.applicationContext;
        if (context == null) {
            return 2;
        }
        IBundleManager bundleManager = context.getBundleManager();
        AbilityInfo abilityInfo = this.applicationContext.getAbilityInfo();
        if (abilityInfo == null || bundleManager == null) {
            return 2;
        }
        String bundleName = abilityInfo.getBundleName();
        if (str == null) {
            str = abilityInfo.getClassName();
        }
        return bundleManager.isShortcutExist(composeShortcutId(getFullClassName(bundleName, str), str2), 0);
    }

    private String composeShortcutId(String str, String str2) {
        if (str == null) {
            return "DEFAULT_SHORTCUT_ID";
        }
        if (str2 == null) {
            return str;
        }
        return str + "#" + str2;
    }

    private String getFullClassName(String str, String str2) {
        if (str == null || str2 == null || !str2.startsWith(".")) {
            return str2;
        }
        return str + str2;
    }
}
