package ohos.app;

import ohos.global.configuration.Configuration;

public interface ElementsCallback {
    void onConfigurationUpdated(Configuration configuration);

    void onMemoryLevel(int i);
}
