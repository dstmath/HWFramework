package com.android.internal.telephony;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.telephony.CarrierConfigManager;
import android.telephony.INetworkService;
import android.telephony.INetworkServiceCallback;
import android.telephony.NetworkRegistrationState;
import android.telephony.Rlog;
import java.util.Hashtable;
import java.util.Map;

public class NetworkRegistrationManager {
    private static final String TAG = NetworkRegistrationManager.class.getSimpleName();
    /* access modifiers changed from: private */
    public final Map<NetworkRegStateCallback, Message> mCallbackTable = new Hashtable();
    private final CarrierConfigManager mCarrierConfigManager;
    /* access modifiers changed from: private */
    public RegManagerDeathRecipient mDeathRecipient;
    /* access modifiers changed from: private */
    public final Phone mPhone;
    /* access modifiers changed from: private */
    public final RegistrantList mRegStateChangeRegistrants = new RegistrantList();
    /* access modifiers changed from: private */
    public INetworkService.Stub mServiceBinder;
    /* access modifiers changed from: private */
    public final int mTransportType;

    private class NetworkRegStateCallback extends INetworkServiceCallback.Stub {
        private NetworkRegStateCallback() {
        }

        public void onGetNetworkRegistrationStateComplete(int result, NetworkRegistrationState state) {
            NetworkRegistrationManager networkRegistrationManager = NetworkRegistrationManager.this;
            int unused = networkRegistrationManager.logd("onGetNetworkRegistrationStateComplete result " + result + " state " + state);
            Message onCompleteMessage = (Message) NetworkRegistrationManager.this.mCallbackTable.remove(this);
            if (onCompleteMessage != null) {
                onCompleteMessage.arg1 = result;
                onCompleteMessage.obj = new AsyncResult(onCompleteMessage.obj, state, null);
                onCompleteMessage.sendToTarget();
                return;
            }
            int unused2 = NetworkRegistrationManager.this.loge("onCompleteMessage is null");
        }

        public void onNetworkStateChanged() {
            int unused = NetworkRegistrationManager.this.logd("onNetworkStateChanged");
            NetworkRegistrationManager.this.mRegStateChangeRegistrants.notifyRegistrants();
        }
    }

    private class NetworkServiceConnection implements ServiceConnection {
        private NetworkServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            int unused = NetworkRegistrationManager.this.logd("service connected.");
            INetworkService.Stub unused2 = NetworkRegistrationManager.this.mServiceBinder = (INetworkService.Stub) service;
            RegManagerDeathRecipient unused3 = NetworkRegistrationManager.this.mDeathRecipient = new RegManagerDeathRecipient(name);
            try {
                NetworkRegistrationManager.this.mServiceBinder.linkToDeath(NetworkRegistrationManager.this.mDeathRecipient, 0);
                NetworkRegistrationManager.this.mServiceBinder.createNetworkServiceProvider(NetworkRegistrationManager.this.mPhone.getPhoneId());
                NetworkRegistrationManager.this.mServiceBinder.registerForNetworkRegistrationStateChanged(NetworkRegistrationManager.this.mPhone.getPhoneId(), new NetworkRegStateCallback());
            } catch (RemoteException exception) {
                NetworkRegistrationManager.this.mDeathRecipient.binderDied();
                NetworkRegistrationManager networkRegistrationManager = NetworkRegistrationManager.this;
                int unused4 = networkRegistrationManager.logd("RemoteException " + exception);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            NetworkRegistrationManager networkRegistrationManager = NetworkRegistrationManager.this;
            int unused = networkRegistrationManager.logd("onServiceDisconnected " + name);
            if (NetworkRegistrationManager.this.mServiceBinder != null) {
                NetworkRegistrationManager.this.mServiceBinder.unlinkToDeath(NetworkRegistrationManager.this.mDeathRecipient, 0);
            }
        }
    }

    private class RegManagerDeathRecipient implements IBinder.DeathRecipient {
        private final ComponentName mComponentName;

        RegManagerDeathRecipient(ComponentName name) {
            this.mComponentName = name;
        }

        public void binderDied() {
            NetworkRegistrationManager networkRegistrationManager = NetworkRegistrationManager.this;
            int unused = networkRegistrationManager.logd("NetworkService(" + this.mComponentName + " transport type " + NetworkRegistrationManager.this.mTransportType + ") died.");
        }
    }

    public NetworkRegistrationManager(int transportType, Phone phone) {
        this.mTransportType = transportType;
        this.mPhone = phone;
        this.mCarrierConfigManager = (CarrierConfigManager) phone.getContext().getSystemService("carrier_config");
        bindService();
    }

    public boolean isServiceConnected() {
        return this.mServiceBinder != null && this.mServiceBinder.isBinderAlive();
    }

    public void unregisterForNetworkRegistrationStateChanged(Handler h) {
        this.mRegStateChangeRegistrants.remove(h);
    }

    public void registerForNetworkRegistrationStateChanged(Handler h, int what, Object obj) {
        logd("registerForNetworkRegistrationStateChanged");
        new Registrant(h, what, obj);
        this.mRegStateChangeRegistrants.addUnique(h, what, obj);
    }

    public void getNetworkRegistrationState(int domain, Message onCompleteMessage) {
        if (onCompleteMessage != null) {
            logd("getNetworkRegistrationState domain " + domain);
            if (!isServiceConnected()) {
                logd("service not connected.");
                onCompleteMessage.obj = new AsyncResult(onCompleteMessage.obj, null, new IllegalStateException("Service not connected."));
                onCompleteMessage.sendToTarget();
                return;
            }
            NetworkRegStateCallback callback = new NetworkRegStateCallback();
            try {
                this.mCallbackTable.put(callback, onCompleteMessage);
                this.mServiceBinder.getNetworkRegistrationState(this.mPhone.getPhoneId(), domain, callback);
            } catch (RemoteException e) {
                String str = TAG;
                Rlog.e(str, "getNetworkRegistrationState RemoteException " + e);
                this.mCallbackTable.remove(callback);
                onCompleteMessage.obj = new AsyncResult(onCompleteMessage.obj, null, e);
                onCompleteMessage.sendToTarget();
            }
        }
    }

    private boolean bindService() {
        Intent intent = new Intent("android.telephony.NetworkService");
        intent.setPackage(getPackageName());
        try {
            return this.mPhone.getContext().bindService(intent, new NetworkServiceConnection(), 1);
        } catch (SecurityException e) {
            loge("bindService failed " + e);
            return false;
        }
    }

    private String getPackageName() {
        String carrierConfig;
        int resourceId;
        switch (this.mTransportType) {
            case 1:
                resourceId = 17039861;
                carrierConfig = "carrier_network_service_wwan_package_override_string";
                break;
            case 2:
                resourceId = 17039859;
                carrierConfig = "carrier_network_service_wlan_package_override_string";
                break;
            default:
                throw new IllegalStateException("Transport type not WWAN or WLAN. type=" + this.mTransportType);
        }
        String packageName = this.mPhone.getContext().getResources().getString(resourceId);
        PersistableBundle b = this.mCarrierConfigManager.getConfigForSubId(this.mPhone.getSubId());
        if (b != null) {
            packageName = b.getString(carrierConfig, packageName);
        }
        logd("Binding to packageName " + packageName + " for transport type" + this.mTransportType);
        return packageName;
    }

    /* access modifiers changed from: private */
    public int logd(String msg) {
        String str = TAG;
        return Rlog.d(str, "[" + this.mPhone.getPhoneId() + "] " + msg);
    }

    /* access modifiers changed from: private */
    public int loge(String msg) {
        String str = TAG;
        return Rlog.e(str, "[" + this.mPhone.getPhoneId() + "] " + msg);
    }
}
