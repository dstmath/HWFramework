package com.log.handler.connection;

import android.os.IHwBinder;
import android.os.RemoteException;
import com.log.handler.LogHandlerUtils;
import java.util.NoSuchElementException;
import vendor.mediatek.hardware.log.V1_0.ILog;
import vendor.mediatek.hardware.log.V1_0.ILogCallback;

public class LogHidlConnection extends AbstractLogConnection {
    private static final String TAG = "LogHandler/LogHidlConnection";
    HidlDeathRecipient mHidlDeathRecipient = new HidlDeathRecipient();
    private ILogCallback mLogCallback = new ILogCallback.Stub() {
        /* class com.log.handler.connection.LogHidlConnection.AnonymousClass1 */

        @Override // vendor.mediatek.hardware.log.V1_0.ILogCallback
        public boolean callbackToClient(String data) throws RemoteException {
            LogHandlerUtils.logw(LogHidlConnection.TAG, "callbackToClient data = " + data);
            LogHidlConnection.this.setResponseFromServer(data);
            return true;
        }
    };
    private ILog mLogHIDLService;

    public LogHidlConnection(String serverName) {
        super(serverName);
    }

    @Override // com.log.handler.connection.AbstractLogConnection, com.log.handler.connection.ILogConnection
    public boolean connect() {
        try {
            LogHandlerUtils.logi(TAG, "LogHIDLConnection serverName = " + this.mServerName);
            this.mLogHIDLService = ILog.getService(this.mServerName);
            this.mLogHIDLService.setCallback(this.mLogCallback);
            this.mLogHIDLService.linkToDeath(this.mHidlDeathRecipient, 0);
            LogHandlerUtils.logi(TAG, "mLogHIDLService.setCallback() done!");
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            disConnect();
            return false;
        } catch (NoSuchElementException e2) {
            e2.printStackTrace();
            disConnect();
            return false;
        }
    }

    @Override // com.log.handler.connection.AbstractLogConnection, com.log.handler.connection.ILogConnection
    public boolean isConnection() {
        return this.mLogHIDLService != null;
    }

    /* access modifiers changed from: protected */
    @Override // com.log.handler.connection.AbstractLogConnection
    public boolean sendDataToServer(String data) {
        LogHandlerUtils.logd(TAG, "sendDataToServer() mServerName = " + this.mServerName + ", data = " + data);
        boolean sendSuccess = false;
        try {
            Thread.sleep(50);
            sendSuccess = this.mLogHIDLService.sendToServer(data);
        } catch (RemoteException e) {
            LogHandlerUtils.loge(TAG, "RemoteException while sending command to native.", e);
            disConnect();
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
        LogHandlerUtils.logd(TAG, "sendToServer done! sendSuccess = " + sendSuccess);
        return sendSuccess;
    }

    @Override // com.log.handler.connection.AbstractLogConnection, com.log.handler.connection.ILogConnection
    public void disConnect() {
        this.mLogHIDLService = null;
        super.disConnect();
    }

    class HidlDeathRecipient implements IHwBinder.DeathRecipient {
        HidlDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            LogHandlerUtils.logi(LogHidlConnection.TAG, "serviceDied! cookie = " + cookie);
            LogHidlConnection.this.disConnect();
        }
    }
}
