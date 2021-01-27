package ohos.ai.cv.common;

import ohos.ai.engine.pluginservice.ILoadPluginCallback;

public interface ICvBase {
    int getAvailability();

    void loadPlugin(ILoadPluginCallback iLoadPluginCallback);

    int prepare();

    int release();
}
