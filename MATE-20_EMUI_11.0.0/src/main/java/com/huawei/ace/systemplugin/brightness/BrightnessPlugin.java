package com.huawei.ace.systemplugin.brightness;

import com.huawei.ace.adapter.AceContextAdapter;
import com.huawei.ace.plugin.ErrorCode;
import com.huawei.ace.plugin.Function;
import com.huawei.ace.plugin.ModuleGroup;
import com.huawei.ace.plugin.Result;
import com.huawei.ace.systemplugin.LogUtil;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import ohos.aafwk.ability.Ability;
import ohos.ace.ability.AceAbility;
import ohos.agp.window.service.Window;
import ohos.agp.window.service.WindowManager;
import ohos.app.Context;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.utils.fastjson.JSONObject;

public class BrightnessPlugin implements ModuleGroup.ModuleGroupHandler, ErrorCode {
    private static final int ADJUST_MODE = 0;
    private static final int AUTO_MODE = 1;
    private static final float DEFAULT_BRIGHTNESS_NOT_OVERRIDE = -1.0f;
    private static final int INT_MAX_BRIGHTNESS = 255;
    private static final int INT_MIN_BRIGHTNESS = 1;
    private static final String TAG = "BrightnessPlugin#";
    private static BrightnessPlugin instance;
    private Ability ability;
    private Result moduleResult;
    private Window win;

    public static String getJsCode() {
        return "var catching = global.systemplugin.catching;var brightness = {    module: null,    onInit: function onInit() {        if (brightness.module == null) {            brightness.module = ModuleGroup.getGroup(\"AceModuleGroup/Brightness\");        }    },    getValue: async function getValue(param) {        brightness.onInit();        return await catching(brightness.module.callNative(\"getValue\"), param);    },    setValue: async function setValue(param) {        brightness.onInit();        if (typeof param.value === 'number') {            return await catching(brightness.module.callNative(\"setValue\", Math.floor(param.value)),                    param);        } else {            commonCallback(param.fail, 'fail', 'value is not an available number', 202);        }    },    getMode: async function getMode(param) {        brightness.onInit();        return await catching(brightness.module.callNative(\"getMode\"), param);    },    setMode: async function setMode(param) {        brightness.onInit();        if (param.mode === 0 || param.mode === 1) {            return await catching(brightness.module.callNative(\"setMode\", param.mode), param);        } else {            commonCallback(param.fail, 'fail', 'value is not an available number', 202);        }    },    setKeepScreenOn: async function setKeepScreenOn(param) {        brightness.onInit();        if (typeof param.keepScreenOn === 'boolean') {            return await catching(brightness.module.callNative(\"setKeepScreenOn\",                    param.keepScreenOn), param);        } else {            commonCallback(param.fail, 'fail', 'value is not an available boolean', 202);        }    }};global.systemplugin.brightness = {    getValue: brightness.getValue,    setValue: brightness.setValue,    getMode: brightness.getMode,    setMode: brightness.setMode,    setKeepScreenOn: brightness.setKeepScreenOn};";
    }

    @Override // com.huawei.ace.plugin.ModuleGroup.ModuleGroupHandler
    public void onFunctionCall(Function function, Result result) {
        LogUtil.info(TAG, "into onFunctionCall");
        this.win = this.ability.getWindow();
        if (this.win == null) {
            result.error(200, "Window is unavailable");
            return;
        }
        this.moduleResult = result;
        if (!"setKeepScreenOn".equals(function.name)) {
            Optional<WindowManager.LayoutConfig> layoutConfig = this.win.getLayoutConfig();
            if (!layoutConfig.isPresent()) {
                result.error(200, "LayoutConfig is unavailable");
                return;
            }
            WindowManager.LayoutConfig layoutConfig2 = layoutConfig.get();
            String str = function.name;
            char c = 65535;
            switch (str.hashCode()) {
                case -75324903:
                    if (str.equals("getMode")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1406685743:
                    if (str.equals("setValue")) {
                        c = 1;
                        break;
                    }
                    break;
                case 1967798203:
                    if (str.equals("getValue")) {
                        c = 0;
                        break;
                    }
                    break;
                case 1984784677:
                    if (str.equals("setMode")) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                getValue(layoutConfig2);
            } else if (c != 1) {
                if (c == 2) {
                    getMode(layoutConfig2);
                } else if (c != 3) {
                    result.notExistFunction();
                } else if (function.arguments.get(0) instanceof Integer) {
                    setMode(layoutConfig2, ((Integer) function.arguments.get(0)).intValue());
                } else {
                    result.error(202, "value is not an available number");
                }
            } else if (function.arguments.get(0) instanceof Integer) {
                setValue(layoutConfig2, ((Integer) function.arguments.get(0)).intValue());
            } else {
                result.error(202, "value is not an available number");
            }
        } else if (function.arguments.get(0) instanceof Boolean) {
            setKeepScreenOn(this.win, ((Boolean) function.arguments.get(0)).booleanValue());
        } else {
            result.error(202, "value is not an available boolean");
        }
    }

    private void getValue(WindowManager.LayoutConfig layoutConfig) {
        if (layoutConfig.windowBrightness != -1.0f) {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("value", Integer.valueOf((int) (layoutConfig.windowBrightness * 255.0f)));
            this.moduleResult.success(jSONObject);
            return;
        }
        AceContextAdapter aceContextAdapter = new AceContextAdapter(this.ability.getHostContext());
        if (aceContextAdapter.getSystemScreenBrightness() != -1) {
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("value", Integer.valueOf(aceContextAdapter.getSystemScreenBrightness()));
            this.moduleResult.success(jSONObject2);
            return;
        }
        this.moduleResult.error(200, "get system screen brightness fail");
    }

    private void setValue(WindowManager.LayoutConfig layoutConfig, int i) {
        if (i < 1) {
            i = 1;
        }
        if (i > 255) {
            i = 255;
        }
        layoutConfig.windowBrightness = ((float) i) / 255.0f;
        this.ability.getUITaskDispatcher().asyncDispatch(new Runnable(layoutConfig) {
            /* class com.huawei.ace.systemplugin.brightness.$$Lambda$BrightnessPlugin$QFNzkDb2VQpt342xwVEz2_49Nk */
            private final /* synthetic */ WindowManager.LayoutConfig f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                BrightnessPlugin.this.lambda$setValue$0$BrightnessPlugin(this.f$1);
            }
        });
        this.moduleResult.success("brightness setValue successfully");
    }

    public /* synthetic */ void lambda$setValue$0$BrightnessPlugin(WindowManager.LayoutConfig layoutConfig) {
        Window window = this.win;
        if (window != null) {
            window.setLayoutConfig(layoutConfig);
        }
    }

    private void getMode(WindowManager.LayoutConfig layoutConfig) {
        float f = layoutConfig.windowBrightness;
        JSONObject jSONObject = new JSONObject();
        if (f == -1.0f) {
            jSONObject.put(Constants.ATTRNAME_MODE, 1);
            this.moduleResult.success(jSONObject);
            return;
        }
        jSONObject.put(Constants.ATTRNAME_MODE, 0);
        this.moduleResult.success(jSONObject);
    }

    private void setMode(WindowManager.LayoutConfig layoutConfig, int i) {
        if (i == 1) {
            layoutConfig.windowBrightness = -1.0f;
        } else {
            int systemScreenBrightness = new AceContextAdapter(this.ability.getHostContext()).getSystemScreenBrightness();
            if (systemScreenBrightness != -1) {
                layoutConfig.windowBrightness = ((float) systemScreenBrightness) / 255.0f;
            } else {
                this.moduleResult.error(200, "get system screen brightness fail");
                return;
            }
        }
        this.ability.getUITaskDispatcher().asyncDispatch(new Runnable(layoutConfig) {
            /* class com.huawei.ace.systemplugin.brightness.$$Lambda$BrightnessPlugin$IZgIZ7Iieg3418o8xdoWoMZ9IGs */
            private final /* synthetic */ WindowManager.LayoutConfig f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                BrightnessPlugin.this.lambda$setMode$1$BrightnessPlugin(this.f$1);
            }
        });
        this.moduleResult.success("brightness setMode successfully");
    }

    public /* synthetic */ void lambda$setMode$1$BrightnessPlugin(WindowManager.LayoutConfig layoutConfig) {
        Window window = this.win;
        if (window != null) {
            window.setLayoutConfig(layoutConfig);
        }
    }

    private void setKeepScreenOn(Window window, boolean z) {
        if (z) {
            this.ability.getUITaskDispatcher().asyncDispatch(new Runnable() {
                /* class com.huawei.ace.systemplugin.brightness.$$Lambda$BrightnessPlugin$FDwZZjEs6QRJBOzHhXDBXt7YZuY */

                @Override // java.lang.Runnable
                public final void run() {
                    BrightnessPlugin.lambda$setKeepScreenOn$2(Window.this);
                }
            });
        } else {
            this.ability.getUITaskDispatcher().asyncDispatch(new Runnable() {
                /* class com.huawei.ace.systemplugin.brightness.$$Lambda$BrightnessPlugin$peRslaO_5Fpf99tAQntWgzGLF3I */

                @Override // java.lang.Runnable
                public final void run() {
                    BrightnessPlugin.lambda$setKeepScreenOn$3(Window.this);
                }
            });
        }
        this.moduleResult.success("brightness setKeepScreenOn successfully");
    }

    static /* synthetic */ void lambda$setKeepScreenOn$2(Window window) {
        if (window != null) {
            window.addFlags(128);
        }
    }

    static /* synthetic */ void lambda$setKeepScreenOn$3(Window window) {
        if (window != null) {
            window.clearFlags(128);
        }
    }

    public static void register(Context context) {
        LogUtil.info(TAG, "register");
        instance = new BrightnessPlugin();
        instance.onRegister(context);
        ModuleGroup.registerModuleGroup("AceModuleGroup/Brightness", instance, context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null);
    }

    private void onRegister(Context context) {
        LogUtil.info(TAG, "onRegister");
        if (context instanceof Ability) {
            this.ability = (Ability) context;
        }
    }

    public static void deregister(Context context) {
        ModuleGroup.registerModuleGroup("AceModuleGroup/Brightness", null, context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null);
    }

    public static Set<String> getPluginGroup() {
        HashSet hashSet = new HashSet();
        hashSet.add("AceModuleGroup/Brightness");
        return hashSet;
    }
}
