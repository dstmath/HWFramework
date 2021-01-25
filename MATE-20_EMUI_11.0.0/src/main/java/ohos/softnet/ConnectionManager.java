package ohos.softnet;

import ohos.app.Context;
import ohos.softnet.connect.ConnectOption;
import ohos.softnet.connect.ConnectionAdapter;
import ohos.softnet.connect.ConnectionCallback;
import ohos.softnet.connect.DataCallback;
import ohos.softnet.connect.DataPayload;
import ohos.softnet.connect.DevConfig;
import ohos.softnet.connect.DiscoveryCallback;
import ohos.softnet.connect.DiscoveryOption;
import ohos.softnet.connect.LogUtils;
import ohos.softnet.connect.PublishOption;

public final class ConnectionManager {
    private static final String TAG = "ConnectionManager";
    private static volatile ConnectionManager connectionManager;
    private ConnectionAdapter connectionAdapter = null;

    public static ConnectionManager getInstance(Context context, String str) {
        ConnectionManager connectionManager2;
        if (context == null || str == null) {
            return null;
        }
        if (connectionManager != null) {
            return connectionManager;
        }
        synchronized (ConnectionManager.class) {
            if (connectionManager == null) {
                connectionManager = new ConnectionManager(context, str);
            }
            connectionManager2 = connectionManager;
        }
        return connectionManager2;
    }

    private ConnectionManager(Context context, String str) {
        this.connectionAdapter = new ConnectionAdapter(context, str);
    }

    public boolean startDiscovery(String str, DiscoveryOption discoveryOption, DiscoveryCallback discoveryCallback) {
        if (str != null && discoveryOption != null && discoveryCallback != null) {
            return this.connectionAdapter.startDiscovery(str, discoveryOption, discoveryCallback);
        }
        LogUtils.error(TAG, "startDiscovery", "input param is null", new Object[0]);
        return false;
    }

    public boolean stopDiscovery(String str, int i) {
        if (str != null) {
            return this.connectionAdapter.stopDiscovery(str, i);
        }
        LogUtils.error(TAG, "stopDiscovery", "input param is null", new Object[0]);
        return false;
    }

    public boolean publishService(String str, PublishOption publishOption, ConnectionCallback connectionCallback) {
        if (str != null && publishOption != null && connectionCallback != null) {
            return this.connectionAdapter.publishService(str, publishOption, connectionCallback);
        }
        LogUtils.error(TAG, "publishService", "input param is null", new Object[0]);
        return false;
    }

    public boolean unPublishService(String str, int i) {
        if (str != null) {
            return this.connectionAdapter.unPublishService(str, i);
        }
        LogUtils.error(TAG, "unPublishService", "input param is null", new Object[0]);
        return false;
    }

    public boolean setConfig(DevConfig devConfig) {
        if (devConfig != null) {
            return this.connectionAdapter.setConfig(devConfig);
        }
        LogUtils.error(TAG, "setConfig", "input param is null", new Object[0]);
        return false;
    }

    public boolean connectDevice(String str, String str2, String str3, ConnectOption connectOption, ConnectionCallback connectionCallback) {
        if (str != null && str2 != null && str3 != null && connectOption != null && connectionCallback != null) {
            return this.connectionAdapter.connectDevice(str, str2, str3, connectOption, connectionCallback);
        }
        LogUtils.error(TAG, "connectDevice", "input param is null", new Object[0]);
        return false;
    }

    public boolean disconnectDevice(String str, String str2, String str3) {
        if (str != null && str2 != null && str3 != null) {
            return this.connectionAdapter.disconnectDevice(str, str2, str3);
        }
        LogUtils.error(TAG, "disconnectDevice", "input param is null", new Object[0]);
        return false;
    }

    public boolean acceptConnect(String str, String str2, String str3, DataCallback dataCallback) {
        if (str != null && str2 != null && str3 != null && dataCallback != null) {
            return this.connectionAdapter.acceptConnect(str, str2, str3, dataCallback);
        }
        LogUtils.error(TAG, "acceptConnect", "input param is null", new Object[0]);
        return false;
    }

    public boolean rejectConnect(String str, String str2, String str3) {
        if (str != null && str2 != null && str3 != null) {
            return this.connectionAdapter.rejectConnect(str, str2, str3);
        }
        LogUtils.error(TAG, "rejectConnect", "input param is null", new Object[0]);
        return false;
    }

    public boolean sendByte(String str, String str2, String str3, byte[] bArr, int i, String str4) {
        if (str != null && str2 != null && str3 != null && bArr != null) {
            return this.connectionAdapter.sendByte(str, str2, str3, bArr, i, str4);
        }
        LogUtils.error(TAG, "sendByte", "input param is null", new Object[0]);
        return false;
    }

    public boolean sendBlock(String str, String str2, String str3, byte[] bArr, int i, String str4) {
        if (str != null && str2 != null && str3 != null && bArr != null) {
            return this.connectionAdapter.sendBlock(str, str2, str3, bArr, i, str4);
        }
        LogUtils.error(TAG, "sendBlock", "input param is null", new Object[0]);
        return false;
    }

    public boolean sendFile(String str, String str2, String str3, String str4, String str5, String str6) {
        if (str != null && str2 != null && str3 != null && str4 != null && str5 != null) {
            return this.connectionAdapter.sendFile(str, str2, str3, str4, str5, str6);
        }
        LogUtils.error(TAG, "sendFile", "input param is null", new Object[0]);
        return false;
    }

    public boolean sendStream(String str, String str2, String str3, DataPayload dataPayload, String str4) {
        if (str != null && str2 != null && str3 != null && dataPayload != null) {
            return this.connectionAdapter.sendStream(str, str2, str3, dataPayload, str4);
        }
        LogUtils.error(TAG, "sendStream", "input param is null", new Object[0]);
        return false;
    }

    public boolean disconnectAll() {
        return this.connectionAdapter.disconnectAll();
    }

    public boolean destroy() {
        return this.connectionAdapter.destroy();
    }
}
