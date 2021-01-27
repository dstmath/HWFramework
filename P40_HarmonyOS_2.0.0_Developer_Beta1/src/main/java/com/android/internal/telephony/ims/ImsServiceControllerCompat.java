package com.android.internal.telephony.ims;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.telephony.ims.aidl.IImsConfig;
import android.telephony.ims.aidl.IImsMmTelFeature;
import android.telephony.ims.aidl.IImsRcsFeature;
import android.telephony.ims.aidl.IImsRegistration;
import android.util.Log;
import android.util.SparseArray;
import com.android.ims.internal.IImsFeatureStatusCallback;
import com.android.ims.internal.IImsMMTelFeature;
import com.android.ims.internal.IImsServiceController;
import com.android.internal.telephony.ims.ImsServiceController;

public class ImsServiceControllerCompat extends ImsServiceController {
    private static final String TAG = "ImsSCCompat";
    private final SparseArray<ImsConfigCompatAdapter> mConfigCompatAdapters = new SparseArray<>();
    private final SparseArray<MmTelFeatureCompatAdapter> mMmTelCompatAdapters = new SparseArray<>();
    private final SparseArray<ImsRegistrationCompatAdapter> mRegCompatAdapters = new SparseArray<>();
    private IImsServiceController mServiceController;

    public ImsServiceControllerCompat(Context context, ComponentName componentName, ImsServiceController.ImsServiceControllerCallbacks callbacks) {
        super(context, componentName, callbacks);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.ims.ImsServiceController
    public final String getServiceInterface() {
        return "android.telephony.ims.compat.ImsService";
    }

    @Override // com.android.internal.telephony.ims.ImsServiceController
    public final void enableIms(int slotId) {
        MmTelFeatureCompatAdapter adapter = this.mMmTelCompatAdapters.get(slotId);
        if (adapter == null) {
            Log.w(TAG, "enableIms: adapter null for slot :" + slotId);
            return;
        }
        try {
            adapter.enableIms();
        } catch (RemoteException e) {
            Log.w(TAG, "Couldn't enable IMS: " + e.getMessage());
        }
    }

    @Override // com.android.internal.telephony.ims.ImsServiceController
    public final void disableIms(int slotId) {
        MmTelFeatureCompatAdapter adapter = this.mMmTelCompatAdapters.get(slotId);
        if (adapter == null) {
            Log.w(TAG, "enableIms: adapter null for slot :" + slotId);
            return;
        }
        try {
            adapter.disableIms();
        } catch (RemoteException e) {
            Log.w(TAG, "Couldn't enable IMS: " + e.getMessage());
        }
    }

    @Override // com.android.internal.telephony.ims.ImsServiceController
    public final IImsRegistration getRegistration(int slotId) {
        ImsRegistrationCompatAdapter adapter = this.mRegCompatAdapters.get(slotId);
        if (adapter != null) {
            return adapter.getBinder();
        }
        Log.w(TAG, "getRegistration: Registration does not exist for slot " + slotId);
        return null;
    }

    @Override // com.android.internal.telephony.ims.ImsServiceController
    public final IImsConfig getConfig(int slotId) {
        ImsConfigCompatAdapter adapter = this.mConfigCompatAdapters.get(slotId);
        if (adapter != null) {
            return adapter.getIImsConfig();
        }
        Log.w(TAG, "getConfig: Config does not exist for slot " + slotId);
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.ims.ImsServiceController
    public final void notifyImsServiceReady() {
        Log.d(TAG, "notifyImsServiceReady");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.ims.ImsServiceController
    public final IInterface createImsFeature(int slotId, int featureType, IImsFeatureStatusCallback c) throws RemoteException {
        if (featureType == 1) {
            return createMMTelCompat(slotId, c);
        }
        if (featureType != 2) {
            return null;
        }
        return createRcsFeature(slotId, c);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.ims.ImsServiceController
    public final void removeImsFeature(int slotId, int featureType, IImsFeatureStatusCallback c) throws RemoteException {
        if (featureType == 1) {
            this.mMmTelCompatAdapters.remove(slotId);
            this.mRegCompatAdapters.remove(slotId);
            this.mConfigCompatAdapters.remove(slotId);
        }
        IImsServiceController iImsServiceController = this.mServiceController;
        if (iImsServiceController != null) {
            iImsServiceController.removeImsFeature(slotId, featureType, c);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.ims.ImsServiceController
    public void setServiceController(IBinder serviceController) {
        this.mServiceController = IImsServiceController.Stub.asInterface(serviceController);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.ims.ImsServiceController
    public boolean isServiceControllerAvailable() {
        return this.mServiceController != null;
    }

    /* access modifiers changed from: protected */
    public MmTelInterfaceAdapter getInterface(int slotId, IImsFeatureStatusCallback c) throws RemoteException {
        IImsMMTelFeature feature = this.mServiceController.createMMTelFeature(slotId, c);
        if (feature != null) {
            return new MmTelInterfaceAdapter(slotId, feature.asBinder());
        }
        Log.w(TAG, "createMMTelCompat: createMMTelFeature returned null.");
        return null;
    }

    private IImsMmTelFeature createMMTelCompat(int slotId, IImsFeatureStatusCallback c) throws RemoteException {
        MmTelFeatureCompatAdapter mmTelAdapter = new MmTelFeatureCompatAdapter(this.mContext, slotId, getInterface(slotId, c));
        this.mMmTelCompatAdapters.put(slotId, mmTelAdapter);
        ImsRegistrationCompatAdapter regAdapter = new ImsRegistrationCompatAdapter();
        mmTelAdapter.addRegistrationAdapter(regAdapter);
        this.mRegCompatAdapters.put(slotId, regAdapter);
        this.mConfigCompatAdapters.put(slotId, new ImsConfigCompatAdapter(mmTelAdapter.getOldConfigInterface()));
        return mmTelAdapter.getBinder();
    }

    private IImsRcsFeature createRcsFeature(int slotId, IImsFeatureStatusCallback c) {
        return null;
    }
}
