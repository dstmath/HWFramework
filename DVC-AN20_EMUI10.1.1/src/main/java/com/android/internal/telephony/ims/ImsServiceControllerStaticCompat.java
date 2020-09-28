package com.android.internal.telephony.ims;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.ims.internal.IImsFeatureStatusCallback;
import com.android.ims.internal.IImsService;
import com.android.internal.telephony.ims.ImsServiceController;

public class ImsServiceControllerStaticCompat extends ImsServiceControllerCompat {
    private static final String IMS_SERVICE_NAME = "ims";
    private static final String TAG = "ImsSCStaticCompat";
    private ImsDeathRecipient mImsDeathRecipient = null;
    private IImsService mImsServiceCompat = null;

    private class ImsDeathRecipient implements IBinder.DeathRecipient {
        private ComponentName mComponentName;
        private ServiceConnection mServiceConnection;

        ImsDeathRecipient(ComponentName name, ServiceConnection conn) {
            this.mComponentName = name;
            this.mServiceConnection = conn;
        }

        public void binderDied() {
            Log.e(ImsServiceControllerStaticCompat.TAG, "ImsService(" + this.mComponentName + ") died. Restarting...");
            this.mServiceConnection.onBindingDied(this.mComponentName);
        }
    }

    public ImsServiceControllerStaticCompat(Context context, ComponentName componentName, ImsServiceController.ImsServiceControllerCallbacks callbacks) {
        super(context, componentName, callbacks);
    }

    @Override // com.android.internal.telephony.ims.ImsServiceController
    public boolean startBindToService(Intent intent, ImsServiceController.ImsServiceConnection connection, int flags) {
        IBinder binder = ServiceManager.checkService(IMS_SERVICE_NAME);
        if (binder == null) {
            return false;
        }
        ComponentName name = new ComponentName(this.mContext, ImsServiceControllerStaticCompat.class);
        connection.onServiceConnected(name, binder);
        try {
            this.mImsDeathRecipient = new ImsDeathRecipient(name, connection);
            binder.linkToDeath(this.mImsDeathRecipient, 0);
            return true;
        } catch (RemoteException e) {
            this.mImsDeathRecipient.binderDied();
            this.mImsDeathRecipient = null;
            return true;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.ims.ImsServiceControllerCompat, com.android.internal.telephony.ims.ImsServiceController
    public void setServiceController(IBinder serviceController) {
        if (serviceController == null) {
            IImsService iImsService = this.mImsServiceCompat;
            if (iImsService != null) {
                iImsService.asBinder().unlinkToDeath(this.mImsDeathRecipient, 0);
            }
            this.mImsDeathRecipient = null;
        }
        this.mImsServiceCompat = IImsService.Stub.asInterface(serviceController);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.ims.ImsServiceControllerCompat, com.android.internal.telephony.ims.ImsServiceController
    public boolean isServiceControllerAvailable() {
        return this.mImsServiceCompat != null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.ims.ImsServiceControllerCompat
    public MmTelInterfaceAdapter getInterface(int slotId, IImsFeatureStatusCallback c) {
        IImsService iImsService = this.mImsServiceCompat;
        if (iImsService != null) {
            return new ImsServiceInterfaceAdapter(slotId, iImsService.asBinder());
        }
        Log.w(TAG, "getInterface: IImsService returned null.");
        return null;
    }
}
