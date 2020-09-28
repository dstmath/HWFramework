package com.log.handler.connection;

import android.os.IHwBinder;
import android.os.RemoteException;
import com.log.handler.LogHandlerUtils;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import vendor.mediatek.hardware.lbs.V1_0.ILbs;
import vendor.mediatek.hardware.lbs.V1_0.ILbsCallback;

public class LbsHidlConnection extends AbstractLogConnection {
    private static final String TAG = "LogHandler/LbsHidlConnection";
    HidlDeathRecipient mHidlDeathRecipient = new HidlDeathRecipient();
    private ILbsCallback mLbsCallback = new ILbsCallback.Stub() {
        /* class com.log.handler.connection.LbsHidlConnection.AnonymousClass1 */

        @Override // vendor.mediatek.hardware.lbs.V1_0.ILbsCallback
        public boolean callbackToClient(ArrayList<Byte> data) throws RemoteException {
            String dataStr = LbsHidlConnection.this.covertArrayListToString(data);
            LogHandlerUtils.logw(LbsHidlConnection.TAG, "callbackToClient data = " + dataStr);
            LbsHidlConnection.this.setResponseFromServer(dataStr);
            return true;
        }
    };
    private ILbs mLbsHIDLCallback;
    private ILbs mLbsHIDLService;

    public LbsHidlConnection(String serverName) {
        super(serverName);
    }

    @Override // com.log.handler.connection.AbstractLogConnection, com.log.handler.connection.ILogConnection
    public boolean connect() {
        try {
            LogHandlerUtils.logi(TAG, "LbsHidlConnection serverName = " + this.mServerName);
            this.mLbsHIDLService = ILbs.getService(this.mServerName);
            this.mLbsHIDLService.linkToDeath(this.mHidlDeathRecipient, 0);
            this.mLbsHIDLCallback = ILbs.getService("mtk_mnld2mtklogger");
            this.mLbsHIDLCallback.setCallback(this.mLbsCallback);
            this.mLbsHIDLCallback.linkToDeath(this.mHidlDeathRecipient, 0);
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
        return (this.mLbsHIDLService == null || this.mLbsHIDLCallback == null) ? false : true;
    }

    /* access modifiers changed from: protected */
    @Override // com.log.handler.connection.AbstractLogConnection
    public boolean sendDataToServer(String data) {
        LogHandlerUtils.logd(TAG, "sendDataToServer() mServerName = " + this.mServerName + ", data = " + data);
        boolean sendSuccess = false;
        try {
            Thread.sleep(50);
            sendSuccess = this.mLbsHIDLService.sendToServer(covertStringToArrayList(data));
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
        this.mLbsHIDLService = null;
        this.mLbsHIDLCallback = null;
        super.disConnect();
    }

    class HidlDeathRecipient implements IHwBinder.DeathRecipient {
        HidlDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            LogHandlerUtils.logi(LbsHidlConnection.TAG, "serviceDied! cookie = " + cookie);
            LbsHidlConnection.this.disConnect();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String covertArrayListToString(ArrayList<Byte> dataList) {
        byte[] bytes = new byte[dataList.size()];
        for (int i = 0; i < dataList.size(); i++) {
            bytes[i] = dataList.get(i).byteValue();
        }
        return new String(bytes);
    }

    private ArrayList<Byte> covertStringToArrayList(String dataStr) {
        byte[] bytes = dataStr.getBytes();
        ArrayList<Byte> dataList = new ArrayList<>();
        for (byte bytee : bytes) {
            dataList.add(Byte.valueOf(bytee));
        }
        return dataList;
    }
}
