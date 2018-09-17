package com.huawei.nearbysdk.DTCP;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import com.huawei.nearbysdk.DTCP.IDTCPService.Stub;
import com.huawei.nearbysdk.HwLog;
import com.huawei.nearbysdk.NearbyDevice;
import com.huawei.nearbysdk.PublishListener;
import java.util.HashMap;

public final class DTCPAdapter {
    private static final String SERVICE_CONNECTION_ACTION = "com.huawei.nearby.DTCP.DTCPService";
    private static final String SERVICE_PACKAGE = "com.huawei.nearby";
    private static final String TAG = "DTCPAdapter";
    private static DTCPAdapter msInstance = null;
    private IDTCPService mDTCPService;
    private DTCPServiceConnection mDTCPSvcConnection;
    private DeathRecipient mDeathRecipient;
    private HashMap<PublishListener, DTCPPublishListenerTransport> mPublishListenerMap;
    private HashMap<DTCPReceiveListener, DTCPReceiveListenerTransport> mReceiveListenerMap;

    public interface AdapterCreateCallback {
        void onAdapterCreate(DTCPAdapter dTCPAdapter);
    }

    private static class DTCPServiceConnection implements ServiceConnection {
        private Context mContext = null;
        private AdapterCreateCallback mCreateCallback = null;

        DTCPServiceConnection(Context context, AdapterCreateCallback cb) {
            this.mContext = context;
            this.mCreateCallback = cb;
        }

        void unBindService() {
            if (this.mContext != null) {
                this.mContext.unbindService(this);
                this.mContext = null;
            }
            this.mCreateCallback = null;
        }

        public void onServiceConnected(ComponentName arg0, IBinder service) {
            HwLog.i(DTCPAdapter.TAG, "DTCPService connected");
            DTCPAdapter.msInstance = new DTCPAdapter(this, Stub.asInterface(service), null);
            if (this.mCreateCallback != null) {
                this.mCreateCallback.onAdapterCreate(DTCPAdapter.msInstance);
                this.mCreateCallback = null;
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            HwLog.i(DTCPAdapter.TAG, "DTCPService disconnected!");
        }
    }

    /* synthetic */ DTCPAdapter(DTCPServiceConnection svcConnection, IDTCPService dtcpSvc, DTCPAdapter -this2) {
        this(svcConnection, dtcpSvc);
    }

    /* JADX WARNING: Missing block: B:11:0x0015, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized boolean createInstance(Context context, AdapterCreateCallback cb) {
        synchronized (DTCPAdapter.class) {
            if (context == null) {
                return false;
            } else if (msInstance == null) {
                HwLog.i(TAG, "Start connect DTCPService");
                Intent intent = new Intent();
                intent.setAction(SERVICE_CONNECTION_ACTION);
                intent.setPackage(SERVICE_PACKAGE);
                if (context.bindServiceAsUser(intent, new DTCPServiceConnection(context, cb), 1, UserHandle.CURRENT)) {
                    return true;
                }
                HwLog.e(TAG, "Bind DTCPService fail!");
                return false;
            } else if (cb != null) {
                cb.onAdapterCreate(msInstance);
            }
        }
    }

    public static synchronized DTCPAdapter getInstance() {
        DTCPAdapter dTCPAdapter;
        synchronized (DTCPAdapter.class) {
            dTCPAdapter = msInstance;
        }
        return dTCPAdapter;
    }

    public static synchronized void releaseInstance() {
        synchronized (DTCPAdapter.class) {
            if (msInstance != null) {
                HwLog.i(TAG, "Release dtcp instance");
                msInstance.unBindDTCPService();
                msInstance = null;
            }
        }
    }

    private DTCPAdapter(DTCPServiceConnection svcConnection, IDTCPService dtcpSvc) {
        this.mDTCPService = null;
        this.mDTCPSvcConnection = null;
        this.mDeathRecipient = null;
        this.mPublishListenerMap = new HashMap();
        this.mReceiveListenerMap = new HashMap();
        this.mDTCPSvcConnection = svcConnection;
        this.mDTCPService = dtcpSvc;
        this.mDeathRecipient = new DeathRecipient() {
            public void binderDied() {
                HwLog.e(DTCPAdapter.TAG, "DTCP Service died!");
                DTCPAdapter.this.mDeathRecipient = null;
                DTCPAdapter.releaseInstance();
            }
        };
        linkToDeath(this.mDeathRecipient);
    }

    private void unBindDTCPService() {
        if (this.mDeathRecipient != null) {
            unlinkToDeath(this.mDeathRecipient);
            this.mDeathRecipient = null;
        }
        if (this.mDTCPSvcConnection != null) {
            this.mDTCPSvcConnection.unBindService();
            this.mDTCPSvcConnection = null;
            this.mDTCPService = null;
        }
        synchronized (this.mPublishListenerMap) {
            this.mPublishListenerMap.clear();
        }
        synchronized (this.mReceiveListenerMap) {
            this.mReceiveListenerMap.clear();
        }
    }

    public int publish(PublishListener listener) {
        return publish(listener, Looper.myLooper());
    }

    public int publish(PublishListener listener, Looper looper) {
        if (listener == null || looper == null) {
            return -2;
        }
        IDTCPService dtcpService = this.mDTCPService;
        if (dtcpService == null) {
            return -10;
        }
        DTCPPublishListenerTransport publishListener = new DTCPPublishListenerTransport(listener, looper);
        try {
            int rtn = dtcpService.publish(publishListener);
            if (rtn != 0) {
                return rtn;
            }
            synchronized (this.mPublishListenerMap) {
                this.mPublishListenerMap.put(listener, publishListener);
            }
            return 0;
        } catch (RemoteException e) {
            HwLog.e(TAG, "Call publish RemoteException, may be DTCP service died");
            return -10;
        }
    }

    public int unPublish(PublishListener listener) {
        if (listener == null) {
            return -2;
        }
        IDTCPService dtcpService = this.mDTCPService;
        if (dtcpService == null) {
            return -10;
        }
        DTCPPublishListenerTransport publishListener;
        synchronized (this.mPublishListenerMap) {
            publishListener = (DTCPPublishListenerTransport) this.mPublishListenerMap.remove(listener);
        }
        if (publishListener == null) {
            HwLog.e(TAG, "Invaild PublishListener, can not unregist");
            return -2;
        }
        try {
            return dtcpService.unPublish(publishListener);
        } catch (RemoteException e) {
            HwLog.e(TAG, "Call unpublish RemoteException, may be DTCP service died");
            return -10;
        }
    }

    public int registerReceivelistener(DTCPReceiveListener listener) {
        return registerReceivelistener(listener, Looper.myLooper());
    }

    public int registerReceivelistener(DTCPReceiveListener listener, Looper looper) {
        if (listener == null || looper == null) {
            return -2;
        }
        IDTCPService dtcpService = this.mDTCPService;
        if (dtcpService == null) {
            return -10;
        }
        DTCPReceiveListenerTransport recvListener = new DTCPReceiveListenerTransport(listener, looper);
        try {
            int rtn = dtcpService.registerReceivelistener(recvListener);
            if (rtn != 0) {
                return rtn;
            }
            synchronized (this.mReceiveListenerMap) {
                this.mReceiveListenerMap.put(listener, recvListener);
            }
            return 0;
        } catch (RemoteException e) {
            HwLog.e(TAG, "Call unpublish RemoteException, may be DTCP service died");
            return -10;
        }
    }

    public int unRegisterReceivelistener(DTCPReceiveListener listener) {
        if (listener == null) {
            return -2;
        }
        IDTCPService dtcpService = this.mDTCPService;
        if (dtcpService == null) {
            return -10;
        }
        DTCPReceiveListenerTransport recvListener;
        synchronized (this.mReceiveListenerMap) {
            recvListener = (DTCPReceiveListenerTransport) this.mReceiveListenerMap.remove(listener);
        }
        if (recvListener == null) {
            HwLog.e(TAG, "Invaild DTCPReceiveListener, can not unregist");
            return -2;
        }
        try {
            return dtcpService.unRegisterReceivelistener(recvListener);
        } catch (RemoteException e) {
            HwLog.e(TAG, "Call unRegisterReceivelistener RemoteException, may be DTCP service died");
            return -10;
        }
    }

    public DTCPSender sendFile(NearbyDevice recvDevice, int timeout, Uri[] fileUriList, SendTransmitCallback transCallback) {
        return sendFile(recvDevice, timeout, fileUriList, transCallback, Looper.myLooper());
    }

    public DTCPSender sendFile(NearbyDevice recvDevice, int timeout, Uri[] fileUriList, SendTransmitCallback transCallback, Looper looper) {
        if (recvDevice == null || timeout < 0 || fileUriList == null || transCallback == null || looper == null) {
            return null;
        }
        IDTCPService dtcpService = this.mDTCPService;
        if (dtcpService == null) {
            return null;
        }
        SendTransmitCallbackTranspot transpotCB = new SendTransmitCallbackTranspot(transCallback, looper);
        try {
            IDTCPSender sender = dtcpService.sendFile(recvDevice, timeout, fileUriList, transpotCB);
            if (sender == null) {
                return null;
            }
            return new DTCPSender(sender, transpotCB);
        } catch (RemoteException e) {
            HwLog.e(TAG, "Call sendFile RemoteException, may be DTCP service died");
            return null;
        }
    }

    public DTCPSender sendText(NearbyDevice recvDevice, int timeout, String text, SendTransmitCallback transCallback) {
        return sendText(recvDevice, timeout, text, transCallback, Looper.myLooper());
    }

    public DTCPSender sendText(NearbyDevice recvDevice, int timeout, String text, SendTransmitCallback transCallback, Looper looper) {
        if (recvDevice == null || timeout < 0 || text == null || transCallback == null || looper == null) {
            return null;
        }
        IDTCPService dtcpService = this.mDTCPService;
        if (dtcpService == null) {
            return null;
        }
        SendTransmitCallbackTranspot transpotCB = new SendTransmitCallbackTranspot(transCallback, looper);
        try {
            IDTCPSender sender = dtcpService.sendText(recvDevice, timeout, text, transpotCB);
            if (sender == null) {
                return null;
            }
            return new DTCPSender(sender, transpotCB);
        } catch (RemoteException e) {
            HwLog.e(TAG, "Call sendFile RemoteException, may be DTCP service died");
            return null;
        }
    }

    public boolean setHwIDInfo(String nickName, byte[] headImage) {
        IDTCPService dtcpService = this.mDTCPService;
        if (dtcpService == null) {
            return false;
        }
        try {
            return dtcpService.setHwIDInfo(nickName, headImage);
        } catch (RemoteException e) {
            HwLog.e(TAG, "Call setHwIDInfo RemoteException, may be DTCP service died");
            return false;
        }
    }

    public boolean linkToDeath(DeathRecipient deathRecipient) {
        IDTCPService dtcpService = this.mDTCPService;
        if (dtcpService == null) {
            return false;
        }
        try {
            dtcpService.asBinder().linkToDeath(deathRecipient, 0);
            return true;
        } catch (RemoteException e) {
            HwLog.e(TAG, "Call service linkToDeath RemoteException");
            return false;
        }
    }

    public boolean unlinkToDeath(DeathRecipient deathRecipient) {
        IDTCPService dtcpService = this.mDTCPService;
        if (dtcpService == null) {
            return false;
        }
        return dtcpService.asBinder().unlinkToDeath(deathRecipient, 0);
    }
}
