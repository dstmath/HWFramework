package ohos.hiaivision.internal;

import android.content.pm.PackageManager;
import android.text.TextUtils;
import com.huawei.ohos.interwork.AndroidUtils;
import java.util.Optional;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.content.Intent;
import ohos.abilityshell.utils.AbilityContextUtils;
import ohos.ai.cv.common.ConnectionCallback;
import ohos.ai.engine.pluginservice.IPluginService;
import ohos.ai.engine.pluginservice.PluginServiceSkeleton;
import ohos.ai.engine.utils.HiAILog;
import ohos.app.Context;
import ohos.bundle.ElementName;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class HwVisionManagerPlugin {
    private static final String CLS_NAME = "com.huawei.hiai.plugin.PluginService";
    private static final String PKG_NAME = "com.huawei.hiai";
    private static final String TAG = "HwVisionManagerPlugin";
    private Context contextH;
    private IRemoteObject hostService;
    private IPluginService service;
    private IAbilityConnection serviceConnection;
    private ConnectionCallback serviceConnectionStatusCallback;

    private HwVisionManagerPlugin() {
        this.serviceConnection = new IAbilityConnection() {
            /* class ohos.hiaivision.internal.HwVisionManagerPlugin.AnonymousClass1 */

            public void onAbilityConnectDone(ElementName elementName, IRemoteObject iRemoteObject, int i) {
                HiAILog.debug(HwVisionManagerPlugin.TAG, "onServiceConnected: " + elementName);
                HwVisionManagerPlugin.this.service = (IPluginService) PluginServiceSkeleton.asInterface(iRemoteObject).orElse(null);
                if (HwVisionManagerPlugin.this.service == null) {
                    HiAILog.error(HwVisionManagerPlugin.TAG, "service is null");
                    HwVisionManagerPlugin.this.notifyServiceDisconnected();
                    return;
                }
                try {
                    HwVisionManagerPlugin.this.hostService = HwVisionManagerPlugin.this.service.getHostRemoteObject();
                } catch (RemoteException e) {
                    HiAILog.error(HwVisionManagerPlugin.TAG, "gethostBinder error:" + e.getMessage());
                }
                HwVisionManagerPlugin.this.notifyServiceIsConnected();
            }

            public void onAbilityDisconnectDone(ElementName elementName, int i) {
                HiAILog.error(HwVisionManagerPlugin.TAG, "onServiceDisconnected: " + elementName);
                HwVisionManagerPlugin.this.service = null;
                HwVisionManagerPlugin.this.notifyServiceDisconnected();
            }
        };
    }

    private static class InstanceGetter {
        private static final HwVisionManagerPlugin INSTANCE = new HwVisionManagerPlugin();

        private InstanceGetter() {
        }
    }

    public static HwVisionManagerPlugin getInstance() {
        return InstanceGetter.INSTANCE;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyServiceIsConnected() {
        ConnectionCallback connectionCallback = this.serviceConnectionStatusCallback;
        if (connectionCallback != null) {
            connectionCallback.onServiceConnect();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyServiceDisconnected() {
        ConnectionCallback connectionCallback = this.serviceConnectionStatusCallback;
        if (connectionCallback != null) {
            connectionCallback.onServiceDisconnect();
        }
    }

    public synchronized int init(Context context, ConnectionCallback connectionCallback) {
        this.contextH = context;
        this.serviceConnectionStatusCallback = connectionCallback;
        if (this.service != null) {
            notifyServiceIsConnected();
            return 0;
        }
        return connectService();
    }

    public IRemoteObject getHostService() {
        if (this.service == null) {
            connectService();
        }
        return this.hostService;
    }

    public IPluginService getPluginService() {
        if (this.service == null) {
            connectService();
        }
        return this.service;
    }

    private int connectService() {
        Intent intent = new Intent();
        intent.setElement(new ElementName("", PKG_NAME, CLS_NAME));
        int bindService = AndroidUtils.bindService(this.contextH, intent, this.serviceConnection);
        HiAILog.debug(TAG, "Called bindService: com.huawei.hiai connectBinderService bindService: " + bindService);
        return bindService;
    }

    public Optional<ClassLoader> getRemoteClassLoader(int i) {
        String str;
        IPluginService pluginService = getPluginService();
        if (pluginService == null) {
            HiAILog.error(TAG, "getRemoteClassLoader, pluginService is null");
            return Optional.empty();
        }
        ClassLoader classLoader = null;
        try {
            str = pluginService.getPluginName(i);
        } catch (RemoteException e) {
            HiAILog.error(TAG, "getRemoteClassLoader, RemoteException: " + e.getMessage());
            str = null;
        }
        if (TextUtils.isEmpty(str)) {
            HiAILog.error(TAG, "getRemoteClassLoader, pluginName is empty");
            return Optional.empty();
        }
        android.content.Context remoteContext = getRemoteContext();
        if (remoteContext == null) {
            HiAILog.error(TAG, "getRemoteClassLoader, context is null");
            return Optional.empty();
        }
        try {
            classLoader = remoteContext.createContextForSplit(str).getClassLoader();
        } catch (PackageManager.NameNotFoundException e2) {
            HiAILog.error(TAG, "getRemoteClassLoader, NameNotFoundException: " + e2.getMessage());
        }
        return Optional.ofNullable(classLoader);
    }

    public android.content.Context getRemoteContext() {
        Object androidContext = AbilityContextUtils.getAndroidContext(this.contextH);
        if (androidContext instanceof android.content.Context) {
            try {
                return ((android.content.Context) androidContext).createPackageContext(PKG_NAME, 3);
            } catch (PackageManager.NameNotFoundException e) {
                HiAILog.error(TAG, "Failed to create getRemoteContext: " + e.getMessage());
                return null;
            }
        } else {
            HiAILog.error(TAG, "Failed to get context!");
            return null;
        }
    }

    public synchronized void destroy() {
        AndroidUtils.unbindService(this.contextH, this.serviceConnection);
        this.serviceConnectionStatusCallback = null;
        this.service = null;
    }
}
