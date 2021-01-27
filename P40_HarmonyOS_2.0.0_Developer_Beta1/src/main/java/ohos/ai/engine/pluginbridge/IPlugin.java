package ohos.ai.engine.pluginbridge;

import ohos.app.Context;
import ohos.rpc.IRemoteObject;

public interface IPlugin {
    IRemoteObject getPluginRemoteObject();

    int getVersion();

    int init(IRemoteObject iRemoteObject, Context context);

    void onRelease();

    void onTrim();
}
