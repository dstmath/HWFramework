package com.android.internal.telephony.dataconnection;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.net.LinkProperties;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.telephony.AccessNetworkConstants;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.data.DataCallResponse;
import android.telephony.data.DataProfile;
import android.telephony.data.IDataService;
import android.telephony.data.IDataServiceCallback;
import android.text.TextUtils;
import com.android.internal.telephony.Phone;
import com.huawei.internal.telephony.dataconnection.ApnSettingHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DataServiceManager extends Handler {
    static final String DATA_CALL_RESPONSE = "data_call_response";
    private static final boolean DBG = true;
    private static final int EVENT_BIND_DATA_SERVICE = 1;
    private final AppOpsManager mAppOps;
    private boolean mBound;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.dataconnection.DataServiceManager.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction()) && DataServiceManager.this.mPhone.getPhoneId() == intent.getIntExtra("android.telephony.extra.SLOT_INDEX", 0)) {
                DataServiceManager.this.log("Carrier config changed. Try to bind data service.");
                DataServiceManager.this.sendEmptyMessage(1);
            }
        }
    };
    private final CarrierConfigManager mCarrierConfigManager;
    private final RegistrantList mDataCallListChangedRegistrants = new RegistrantList();
    private DataServiceManagerDeathRecipient mDeathRecipient;
    private IDataService mIDataService;
    private final Map<IBinder, Message> mMessageMap = new ConcurrentHashMap();
    private final IPackageManager mPackageManager;
    private final Phone mPhone;
    private final RegistrantList mServiceBindingChangedRegistrants = new RegistrantList();
    private CellularDataServiceConnection mServiceConnection;
    private final String mTag;
    private String mTargetBindingPackageName;
    private final int mTransportType;

    /* access modifiers changed from: private */
    public class DataServiceManagerDeathRecipient implements IBinder.DeathRecipient {
        private DataServiceManagerDeathRecipient() {
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            DataServiceManager dataServiceManager = DataServiceManager.this;
            dataServiceManager.loge("DataService " + DataServiceManager.this.mTargetBindingPackageName + ", transport type " + DataServiceManager.this.mTransportType + " died.");
        }
    }

    private void grantPermissionsToService(String packageName) {
        String[] pkgToGrant = {packageName};
        try {
            this.mPackageManager.grantDefaultPermissionsToEnabledTelephonyDataServices(pkgToGrant, this.mPhone.getContext().getUserId());
            this.mAppOps.setMode(75, this.mPhone.getContext().getUserId(), pkgToGrant[0], 0);
        } catch (RemoteException e) {
            loge("Binder to package manager died, permission grant for DataService failed.");
            throw e.rethrowAsRuntimeException();
        }
    }

    private void revokePermissionsFromUnusedDataServices() {
        Set<String> dataServices = getAllDataServicePackageNames();
        for (int transportType : this.mPhone.getTransportManager().getAvailableTransports()) {
            dataServices.remove(getDataServicePackageName(transportType));
        }
        try {
            String[] dataServicesArray = new String[dataServices.size()];
            dataServices.toArray(dataServicesArray);
            this.mPackageManager.revokeDefaultPermissionsFromDisabledTelephonyDataServices(dataServicesArray, this.mPhone.getContext().getUserId());
            for (String pkg : dataServices) {
                this.mAppOps.setMode(75, this.mPhone.getContext().getUserId(), pkg, 2);
            }
        } catch (RemoteException e) {
            loge("Binder to package manager died; failed to revoke DataService permissions.");
            throw e.rethrowAsRuntimeException();
        }
    }

    /* access modifiers changed from: private */
    public final class CellularDataServiceConnection implements ServiceConnection {
        private CellularDataServiceConnection() {
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            DataServiceManager.this.log("onServiceConnected");
            DataServiceManager.this.mIDataService = IDataService.Stub.asInterface(service);
            DataServiceManager dataServiceManager = DataServiceManager.this;
            dataServiceManager.mDeathRecipient = new DataServiceManagerDeathRecipient();
            DataServiceManager.this.mBound = true;
            try {
                service.linkToDeath(DataServiceManager.this.mDeathRecipient, 0);
                DataServiceManager.this.mIDataService.createDataServiceProvider(DataServiceManager.this.mPhone.getPhoneId());
                DataServiceManager.this.mIDataService.registerForDataCallListChanged(DataServiceManager.this.mPhone.getPhoneId(), new CellularDataServiceCallback());
                DataServiceManager.this.mServiceBindingChangedRegistrants.notifyResult(true);
            } catch (RemoteException e) {
                DataServiceManager.this.mDeathRecipient.binderDied();
                DataServiceManager dataServiceManager2 = DataServiceManager.this;
                dataServiceManager2.loge("Remote exception. " + e);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            DataServiceManager.this.log("onServiceDisconnected");
            DataServiceManager.this.mIDataService.asBinder().unlinkToDeath(DataServiceManager.this.mDeathRecipient, 0);
            DataServiceManager.this.mIDataService = null;
            DataServiceManager.this.mBound = false;
            DataServiceManager.this.mServiceBindingChangedRegistrants.notifyResult(false);
            DataServiceManager.this.mTargetBindingPackageName = null;
        }
    }

    /* access modifiers changed from: private */
    public final class CellularDataServiceCallback extends IDataServiceCallback.Stub {
        private CellularDataServiceCallback() {
        }

        public void onSetupDataCallComplete(int resultCode, DataCallResponse response) {
            DataServiceManager dataServiceManager = DataServiceManager.this;
            dataServiceManager.log("onSetupDataCallComplete. resultCode = " + resultCode + ", response = " + response);
            Message msg = (Message) DataServiceManager.this.mMessageMap.remove(asBinder());
            if (msg != null) {
                msg.getData().putParcelable(DataServiceManager.DATA_CALL_RESPONSE, response);
                DataServiceManager.this.sendCompleteMessage(msg, resultCode);
                return;
            }
            DataServiceManager.this.loge("Unable to find the message for setup call response.");
        }

        public void onDeactivateDataCallComplete(int resultCode) {
            DataServiceManager dataServiceManager = DataServiceManager.this;
            dataServiceManager.log("onDeactivateDataCallComplete. resultCode = " + resultCode);
            DataServiceManager.this.sendCompleteMessage((Message) DataServiceManager.this.mMessageMap.remove(asBinder()), resultCode);
        }

        public void onSetInitialAttachApnComplete(int resultCode) {
            DataServiceManager dataServiceManager = DataServiceManager.this;
            dataServiceManager.log("onSetInitialAttachApnComplete. resultCode = " + resultCode);
            DataServiceManager.this.sendCompleteMessage((Message) DataServiceManager.this.mMessageMap.remove(asBinder()), resultCode);
        }

        public void onSetDataProfileComplete(int resultCode) {
            DataServiceManager dataServiceManager = DataServiceManager.this;
            dataServiceManager.log("onSetDataProfileComplete. resultCode = " + resultCode);
            DataServiceManager.this.sendCompleteMessage((Message) DataServiceManager.this.mMessageMap.remove(asBinder()), resultCode);
        }

        public void onRequestDataCallListComplete(int resultCode, List<DataCallResponse> list) {
            DataServiceManager dataServiceManager = DataServiceManager.this;
            dataServiceManager.log("onRequestDataCallListComplete. resultCode = " + resultCode);
            DataServiceManager.this.sendCompleteMessage((Message) DataServiceManager.this.mMessageMap.remove(asBinder()), resultCode);
        }

        public void onDataCallListChanged(List<DataCallResponse> dataCallList) {
            DataServiceManager.this.mDataCallListChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, dataCallList, (Throwable) null));
        }
    }

    public DataServiceManager(Phone phone, int transportType, String tagSuffix) {
        this.mPhone = phone;
        this.mTag = "DSM" + tagSuffix;
        this.mTransportType = transportType;
        this.mBound = false;
        this.mCarrierConfigManager = (CarrierConfigManager) phone.getContext().getSystemService("carrier_config");
        this.mPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        this.mAppOps = (AppOpsManager) phone.getContext().getSystemService("appops");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        phone.getContext().registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, intentFilter, null, null);
        sendEmptyMessage(1);
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        if (msg.what != 1) {
            loge("Unhandled event " + msg.what);
            return;
        }
        bindDataService();
    }

    private void bindDataService() {
        String packageName = getDataServicePackageName();
        if (TextUtils.isEmpty(packageName)) {
            loge("Can't find the binding package");
        } else if (TextUtils.equals(packageName, this.mTargetBindingPackageName)) {
            log("Service " + packageName + " already bound or being bound.");
        } else {
            revokePermissionsFromUnusedDataServices();
            IDataService iDataService = this.mIDataService;
            if (iDataService != null && iDataService.asBinder().isBinderAlive()) {
                try {
                    this.mIDataService.removeDataServiceProvider(this.mPhone.getPhoneId());
                } catch (RemoteException e) {
                    loge("Cannot remove data service provider. " + e);
                }
                this.mPhone.getContext().unbindService(this.mServiceConnection);
            }
            grantPermissionsToService(packageName);
            try {
                this.mServiceConnection = new CellularDataServiceConnection();
                if (!this.mPhone.getContext().bindService(new Intent("android.telephony.data.DataService").setPackage(packageName), this.mServiceConnection, 1)) {
                    loge("Cannot bind to the data service.");
                } else {
                    this.mTargetBindingPackageName = packageName;
                }
            } catch (Exception e2) {
                loge("Cannot bind to the data service. Exception: " + e2);
            }
        }
    }

    private Set<String> getAllDataServicePackageNames() {
        List<ResolveInfo> dataPackages = this.mPhone.getContext().getPackageManager().queryIntentServices(new Intent("android.telephony.data.DataService"), ApnSettingHelper.TYPE_BIP5);
        HashSet<String> packageNames = new HashSet<>();
        for (ResolveInfo info : dataPackages) {
            if (info.serviceInfo != null) {
                packageNames.add(info.serviceInfo.packageName);
            }
        }
        return packageNames;
    }

    private String getDataServicePackageName() {
        return getDataServicePackageName(this.mTransportType);
    }

    private String getDataServicePackageName(int transportType) {
        String carrierConfig;
        int resourceId;
        if (transportType == 1) {
            resourceId = 17039900;
            carrierConfig = "carrier_data_service_wwan_package_override_string";
        } else if (transportType == 2) {
            resourceId = 17039898;
            carrierConfig = "carrier_data_service_wlan_package_override_string";
        } else {
            throw new IllegalStateException("Transport type not WWAN or WLAN. type=" + AccessNetworkConstants.transportTypeToString(this.mTransportType));
        }
        String packageName = this.mPhone.getContext().getResources().getString(resourceId);
        PersistableBundle b = this.mCarrierConfigManager.getConfigForSubId(this.mPhone.getSubId());
        if (b == null || TextUtils.isEmpty(b.getString(carrierConfig))) {
            return packageName;
        }
        return b.getString(carrierConfig, packageName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendCompleteMessage(Message msg, int code) {
        if (msg != null) {
            msg.arg1 = code;
            msg.sendToTarget();
        }
    }

    public void setupDataCall(int accessNetworkType, DataProfile dataProfile, boolean isRoaming, boolean allowRoaming, int reason, LinkProperties linkProperties, Message onCompleteMessage) {
        log("setupDataCall");
        if (!this.mBound) {
            loge("Data service not bound.");
            sendCompleteMessage(onCompleteMessage, 4);
            return;
        }
        CellularDataServiceCallback callback = new CellularDataServiceCallback();
        if (onCompleteMessage != null) {
            this.mMessageMap.put(callback.asBinder(), onCompleteMessage);
        }
        try {
            this.mIDataService.setupDataCall(this.mPhone.getPhoneId(), accessNetworkType, dataProfile, isRoaming, allowRoaming, reason, linkProperties, callback);
        } catch (RemoteException e) {
            loge("Cannot invoke setupDataCall on data service.");
            this.mMessageMap.remove(callback.asBinder());
            sendCompleteMessage(onCompleteMessage, 4);
        }
    }

    public void deactivateDataCall(int cid, int reason, Message onCompleteMessage) {
        log("deactivateDataCall");
        if (!this.mBound) {
            loge("Data service not bound.");
            sendCompleteMessage(onCompleteMessage, 4);
            return;
        }
        CellularDataServiceCallback callback = new CellularDataServiceCallback();
        if (onCompleteMessage != null) {
            this.mMessageMap.put(callback.asBinder(), onCompleteMessage);
        }
        try {
            this.mIDataService.deactivateDataCall(this.mPhone.getPhoneId(), cid, reason, callback);
        } catch (RemoteException e) {
            loge("Cannot invoke deactivateDataCall on data service.");
            this.mMessageMap.remove(callback.asBinder());
            sendCompleteMessage(onCompleteMessage, 4);
        }
    }

    public void setInitialAttachApn(DataProfile dataProfile, boolean isRoaming, Message onCompleteMessage) {
        log("setInitialAttachApn");
        if (!this.mBound) {
            loge("Data service not bound.");
            sendCompleteMessage(onCompleteMessage, 4);
            return;
        }
        CellularDataServiceCallback callback = new CellularDataServiceCallback();
        if (onCompleteMessage != null) {
            this.mMessageMap.put(callback.asBinder(), onCompleteMessage);
        }
        try {
            this.mIDataService.setInitialAttachApn(this.mPhone.getPhoneId(), dataProfile, isRoaming, callback);
        } catch (RemoteException e) {
            loge("Cannot invoke setInitialAttachApn on data service.");
            this.mMessageMap.remove(callback.asBinder());
            sendCompleteMessage(onCompleteMessage, 4);
        }
    }

    public void setDataProfile(List<DataProfile> dps, boolean isRoaming, Message onCompleteMessage) {
        log("setDataProfile");
        if (!this.mBound) {
            loge("Data service not bound.");
            sendCompleteMessage(onCompleteMessage, 4);
            return;
        }
        CellularDataServiceCallback callback = new CellularDataServiceCallback();
        if (onCompleteMessage != null) {
            this.mMessageMap.put(callback.asBinder(), onCompleteMessage);
        }
        try {
            this.mIDataService.setDataProfile(this.mPhone.getPhoneId(), dps, isRoaming, callback);
        } catch (RemoteException e) {
            loge("Cannot invoke setDataProfile on data service.");
            this.mMessageMap.remove(callback.asBinder());
            sendCompleteMessage(onCompleteMessage, 4);
        }
    }

    public void requestDataCallList(Message onCompleteMessage) {
        log("requestDataCallList");
        if (!this.mBound) {
            loge("Data service not bound.");
            sendCompleteMessage(onCompleteMessage, 4);
            return;
        }
        CellularDataServiceCallback callback = new CellularDataServiceCallback();
        if (onCompleteMessage != null) {
            this.mMessageMap.put(callback.asBinder(), onCompleteMessage);
        }
        try {
            this.mIDataService.requestDataCallList(this.mPhone.getPhoneId(), callback);
        } catch (RemoteException e) {
            loge("Cannot invoke requestDataCallList on data service.");
            this.mMessageMap.remove(callback.asBinder());
            sendCompleteMessage(onCompleteMessage, 4);
        }
    }

    public void registerForDataCallListChanged(Handler h, int what) {
        if (h != null) {
            this.mDataCallListChangedRegistrants.addUnique(h, what, (Object) null);
        }
    }

    public void unregisterForDataCallListChanged(Handler h) {
        if (h != null) {
            this.mDataCallListChangedRegistrants.remove(h);
        }
    }

    public void registerForServiceBindingChanged(Handler h, int what, Object obj) {
        if (h != null) {
            this.mServiceBindingChangedRegistrants.addUnique(h, what, obj);
        }
    }

    public void unregisterForServiceBindingChanged(Handler h) {
        if (h != null) {
            this.mServiceBindingChangedRegistrants.remove(h);
        }
    }

    public int getTransportType() {
        return this.mTransportType;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        Rlog.i(this.mTag, s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String s) {
        Rlog.e(this.mTag, s);
    }
}
