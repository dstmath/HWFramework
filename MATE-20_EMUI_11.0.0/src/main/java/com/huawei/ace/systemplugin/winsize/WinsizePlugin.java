package com.huawei.ace.systemplugin.winsize;

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

public class WinsizePlugin implements ModuleGroup.ModuleGroupHandler, ErrorCode {
    private static final String TAG = "WinsizePlugin#";
    private static WinsizePlugin instance;
    private Ability ability;
    private Result moduleResult;
    private Window win;

    public static String getJsCode() {
        return "var catching = global.systemplugin.catching;var winsize = {    module: null,    onInit: function onInit() {        if (winsize.module == null) {            winsize.module = ModuleGroup.getGroup(\"AceModuleGroup/Winsize\");        }    },    toFullScreen: async function toFullScreen(param) {        console.log('into winSize toFullScreen');        winsize.onInit();        return await catching(winsize.module.callNative(\"toFullScreen\"), param);    },    getWidth: async function getWidth(param) {        winsize.onInit();        return await catching(winsize.module.callNative(\"getWidth\"), param);    },    getHeight: async function getHeight(param) {        winsize.onInit();        return await catching(winsize.module.callNative(\"getHeight\"), param);    }};global.systemplugin.winsize = {    toFullScreen: winsize.toFullScreen,    getWidth: winsize.getWidth,    getHeight: winsize.getHeight};";
    }

    @Override // com.huawei.ace.plugin.ModuleGroup.ModuleGroupHandler
    public void onFunctionCall(Function function, Result result) {
        LogUtil.info(TAG, "into onFunctionCall");
        this.win = this.ability.getWindow();
        Window window = this.win;
        if (window == null) {
            result.error(200, "Window is unavailable");
            return;
        }
        this.moduleResult = result;
        Optional<WindowManager.LayoutConfig> layoutConfig = window.getLayoutConfig();
        if (!layoutConfig.isPresent()) {
            result.error(200, "LayoutConfig is unavailable");
            return;
        }
        WindowManager.LayoutConfig layoutConfig2 = layoutConfig.get();
        String str = function.name;
        char c = 65535;
        int hashCode = str.hashCode();
        if (hashCode != 474985501) {
            if (hashCode != 1382986934) {
                if (hashCode == 1968952336 && str.equals("getWidth")) {
                    c = 1;
                }
            } else if (str.equals("toFullScreen")) {
                c = 0;
            }
        } else if (str.equals("getHeight")) {
            c = 2;
        }
        if (c == 0) {
            toFullScreen(layoutConfig2);
        } else if (c == 1) {
            getWidth(layoutConfig2);
        } else if (c != 2) {
            result.notExistFunction();
        } else {
            getHeight(layoutConfig2);
        }
    }

    private void getWidth(WindowManager.LayoutConfig layoutConfig) {
        LogUtil.info(TAG, "winSizeGetWidth");
        this.moduleResult.success(Integer.valueOf(layoutConfig.width));
    }

    private void getHeight(WindowManager.LayoutConfig layoutConfig) {
        LogUtil.info(TAG, "winSizeGetHeight");
        this.moduleResult.success(Integer.valueOf(layoutConfig.height));
    }

    private void toFullScreen(WindowManager.LayoutConfig layoutConfig) {
        LogUtil.info(TAG, "winSizeToFullScreen");
        layoutConfig.x = 0;
        layoutConfig.y = 0;
        layoutConfig.width = -1;
        layoutConfig.height = -1;
        this.ability.getUITaskDispatcher().asyncDispatch(new Runnable(layoutConfig) {
            /* class com.huawei.ace.systemplugin.winsize.$$Lambda$WinsizePlugin$Mj9Kn6zrwR8ItSrHWvlX8kGpE */
            private final /* synthetic */ WindowManager.LayoutConfig f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                WinsizePlugin.this.lambda$toFullScreen$0$WinsizePlugin(this.f$1);
            }
        });
        this.moduleResult.success("winsize setFullScreen successfully");
    }

    public /* synthetic */ void lambda$toFullScreen$0$WinsizePlugin(WindowManager.LayoutConfig layoutConfig) {
        Window window = this.win;
        if (window != null) {
            window.setLayoutConfig(layoutConfig);
        }
    }

    public static void register(Context context) {
        LogUtil.info(TAG, "register");
        instance = new WinsizePlugin();
        instance.onRegister(context);
        ModuleGroup.registerModuleGroup("AceModuleGroup/Winsize", instance, context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null);
    }

    private void onRegister(Context context) {
        LogUtil.info(TAG, "onRegister");
        if (context instanceof Ability) {
            this.ability = (Ability) context;
        }
    }

    public static void deregister(Context context) {
        ModuleGroup.registerModuleGroup("AceModuleGroup/Winsize", null, context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null);
    }

    public static Set<String> getPluginGroup() {
        HashSet hashSet = new HashSet();
        hashSet.add("AceModuleGroup/Winsize");
        return hashSet;
    }
}
