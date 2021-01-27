package ohos.ace.ability;

import com.huawei.ace.runtime.HarmonySystemPluginLoader;
import ohos.abilityshell.HarmonyApplication;

public class AceHarmonyApplication extends HarmonyApplication {
    @Override // ohos.abilityshell.HarmonyApplication, android.app.Application
    public void onCreate() {
        HarmonySystemPluginLoader.loadPluginJsCodeAndGroupName();
        super.onCreate();
    }
}
