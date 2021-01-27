package ohos.ai.engine.utils;

import com.huawei.ohos.interwork.AndroidUtils;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.content.Intent;
import ohos.ai.engine.pluginservice.IPluginService;
import ohos.ai.engine.pluginservice.PluginServiceSkeleton;
import ohos.ai.engine.upgradestrategy.UpgradeStrategyManager;
import ohos.app.Context;
import ohos.bundle.ElementName;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class ServiceConnector {
    private static final String ENGINE_PACKAGE = "com.huawei.hiai";
    private static final String ENGINE_PLUGIN_SERVICE = "com.huawei.hiai.plugin.PluginService";
    private static final int FAILURE = -1;
    private static final String TAG = "ServiceConnector";

    private ServiceConnector() {
    }

    public static boolean connectToService(Context context, String str, IAbilityConnection iAbilityConnection) {
        HiAILog.info(TAG, "Connect to HiAI service");
        if (iAbilityConnection == null) {
            HiAILog.error(TAG, "connection is null");
            return false;
        } else if (context == null) {
            HiAILog.error(TAG, "context is null");
            return false;
        } else {
            Intent intent = new Intent();
            intent.setElement(new ElementName(str, ENGINE_PACKAGE, ENGINE_PLUGIN_SERVICE));
            if (AndroidUtils.bindService(context, intent, new ServiceConnection(iAbilityConnection)) != -1) {
                return true;
            }
            HiAILog.error(TAG, "bind to service failed");
            return false;
        }
    }

    public static void unBindService(Context context, IAbilityConnection iAbilityConnection) {
        AndroidUtils.unbindService(context, iAbilityConnection);
    }

    /* access modifiers changed from: private */
    public static class ServiceConnection implements IAbilityConnection {
        IAbilityConnection connection;

        ServiceConnection(IAbilityConnection iAbilityConnection) {
            this.connection = iAbilityConnection;
        }

        @Override // ohos.aafwk.ability.IAbilityConnection
        public void onAbilityConnectDone(ElementName elementName, IRemoteObject iRemoteObject, int i) {
            HiAILog.info(ServiceConnector.TAG, "onAbilityConnectDone success.");
            IPluginService orElse = PluginServiceSkeleton.asInterface(iRemoteObject).orElse(null);
            if (orElse == null) {
                HiAILog.error(ServiceConnector.TAG, "plugin service is null");
                return;
            }
            try {
                UpgradeStrategyManager.getInstance().setCoreService(orElse.getHostRemoteObject());
            } catch (RemoteException unused) {
                HiAILog.error(ServiceConnector.TAG, "onServiceConnected RemoteException!");
            }
            IAbilityConnection iAbilityConnection = this.connection;
            if (iAbilityConnection != null) {
                iAbilityConnection.onAbilityConnectDone(elementName, iRemoteObject, i);
            }
        }

        @Override // ohos.aafwk.ability.IAbilityConnection
        public void onAbilityDisconnectDone(ElementName elementName, int i) {
            HiAILog.info(ServiceConnector.TAG, "onAbilityDisconnectDone success.");
            IAbilityConnection iAbilityConnection = this.connection;
            if (iAbilityConnection != null) {
                iAbilityConnection.onAbilityDisconnectDone(elementName, i);
            }
        }
    }
}
