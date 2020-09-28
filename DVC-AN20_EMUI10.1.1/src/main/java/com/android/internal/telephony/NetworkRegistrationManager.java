package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.telephony.AccessNetworkConstants;
import android.telephony.CarrierConfigManager;
import android.telephony.INetworkService;
import android.telephony.INetworkServiceCallback;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import java.util.Hashtable;
import java.util.Map;

public class NetworkRegistrationManager extends Handler {
    private static final int EVENT_BIND_NETWORK_SERVICE = 1;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.NetworkRegistrationManager.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction()) && NetworkRegistrationManager.this.mPhone.getPhoneId() == intent.getIntExtra("android.telephony.extra.SLOT_INDEX", 0)) {
                NetworkRegistrationManager.this.logd("Carrier config changed. Try to bind network service.");
                NetworkRegistrationManager.this.sendEmptyMessage(1);
            }
        }
    };
    private final Map<NetworkRegStateCallback, Message> mCallbackTable = new Hashtable();
    private final CarrierConfigManager mCarrierConfigManager;
    private RegManagerDeathRecipient mDeathRecipient;
    private INetworkService mINetworkService;
    private final Phone mPhone;
    private final RegistrantList mRegStateChangeRegistrants = new RegistrantList();
    private NetworkServiceConnection mServiceConnection;
    private final String mTag;
    private String mTargetBindingPackageName;
    private final int mTransportType;

    public NetworkRegistrationManager(int transportType, Phone phone) {
        this.mTransportType = transportType;
        this.mPhone = phone;
        StringBuilder sb = new StringBuilder();
        sb.append("-");
        sb.append(transportType == 1 ? "C" : "I");
        String tagSuffix = sb.toString();
        if (TelephonyManager.getDefault().getPhoneCount() > 1) {
            tagSuffix = tagSuffix + "-" + this.mPhone.getPhoneId();
        }
        this.mTag = "NRM" + tagSuffix;
        this.mCarrierConfigManager = (CarrierConfigManager) phone.getContext().getSystemService("carrier_config");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        phone.getContext().registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, intentFilter, null, null);
        sendEmptyMessage(1);
    }

    public void handleMessage(Message msg) {
        if (msg.what != 1) {
            loge("Unhandled event " + msg.what);
            return;
        }
        bindService();
    }

    public boolean isServiceConnected() {
        INetworkService iNetworkService = this.mINetworkService;
        return iNetworkService != null && iNetworkService.asBinder().isBinderAlive();
    }

    public void unregisterForNetworkRegistrationInfoChanged(Handler h) {
        this.mRegStateChangeRegistrants.remove(h);
    }

    public void registerForNetworkRegistrationInfoChanged(Handler h, int what, Object obj) {
        logd("registerForNetworkRegistrationInfoChanged");
        this.mRegStateChangeRegistrants.addUnique(h, what, obj);
    }

    public void requestNetworkRegistrationInfo(int domain, Message onCompleteMessage) {
        if (onCompleteMessage != null) {
            if (!isServiceConnected()) {
                StringBuilder sb = new StringBuilder();
                sb.append("service not connected. Domain = ");
                sb.append(domain == 1 ? "CS" : "PS");
                loge(sb.toString());
                onCompleteMessage.obj = new AsyncResult(onCompleteMessage.obj, (Object) null, new IllegalStateException("Service not connected."));
                onCompleteMessage.sendToTarget();
                return;
            }
            NetworkRegStateCallback callback = new NetworkRegStateCallback();
            try {
                this.mCallbackTable.put(callback, onCompleteMessage);
                this.mINetworkService.requestNetworkRegistrationInfo(this.mPhone.getPhoneId(), domain, callback);
            } catch (RemoteException e) {
                loge("requestNetworkRegistrationInfo RemoteException " + e);
                this.mCallbackTable.remove(callback);
                onCompleteMessage.obj = new AsyncResult(onCompleteMessage.obj, (Object) null, e);
                onCompleteMessage.sendToTarget();
            }
        }
    }

    /* access modifiers changed from: private */
    public class RegManagerDeathRecipient implements IBinder.DeathRecipient {
        private final ComponentName mComponentName;

        RegManagerDeathRecipient(ComponentName name) {
            this.mComponentName = name;
        }

        public void binderDied() {
            NetworkRegistrationManager networkRegistrationManager = NetworkRegistrationManager.this;
            networkRegistrationManager.logd("NetworkService(" + this.mComponentName + " transport type " + NetworkRegistrationManager.this.mTransportType + ") died.");
        }
    }

    /* access modifiers changed from: private */
    public class NetworkServiceConnection implements ServiceConnection {
        private NetworkServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkRegistrationManager networkRegistrationManager = NetworkRegistrationManager.this;
            networkRegistrationManager.logd("service " + name + " for transport " + AccessNetworkConstants.transportTypeToString(NetworkRegistrationManager.this.mTransportType) + " is now connected.");
            NetworkRegistrationManager.this.mINetworkService = INetworkService.Stub.asInterface(service);
            NetworkRegistrationManager networkRegistrationManager2 = NetworkRegistrationManager.this;
            networkRegistrationManager2.mDeathRecipient = new RegManagerDeathRecipient(name);
            try {
                service.linkToDeath(NetworkRegistrationManager.this.mDeathRecipient, 0);
                NetworkRegistrationManager.this.mINetworkService.createNetworkServiceProvider(NetworkRegistrationManager.this.mPhone.getPhoneId());
                NetworkRegistrationManager.this.mINetworkService.registerForNetworkRegistrationInfoChanged(NetworkRegistrationManager.this.mPhone.getPhoneId(), new NetworkRegStateCallback());
                NetworkRegistrationManager.this.mRegStateChangeRegistrants.notifyRegistrants();
            } catch (RemoteException exception) {
                NetworkRegistrationManager.this.mDeathRecipient.binderDied();
                NetworkRegistrationManager networkRegistrationManager3 = NetworkRegistrationManager.this;
                networkRegistrationManager3.logd("RemoteException " + exception);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            NetworkRegistrationManager networkRegistrationManager = NetworkRegistrationManager.this;
            networkRegistrationManager.logd("service " + name + " for transport " + AccessNetworkConstants.transportTypeToString(NetworkRegistrationManager.this.mTransportType) + " is now disconnected.");
            NetworkRegistrationManager.this.mTargetBindingPackageName = null;
            if (NetworkRegistrationManager.this.mINetworkService != null) {
                NetworkRegistrationManager.this.mINetworkService.asBinder().unlinkToDeath(NetworkRegistrationManager.this.mDeathRecipient, 0);
            }
        }
    }

    /* access modifiers changed from: private */
    public class NetworkRegStateCallback extends INetworkServiceCallback.Stub {
        private NetworkRegStateCallback() {
        }

        public void onRequestNetworkRegistrationInfoComplete(int result, NetworkRegistrationInfo info) {
            NetworkRegistrationManager networkRegistrationManager = NetworkRegistrationManager.this;
            networkRegistrationManager.logd("onRequestNetworkRegistrationInfoComplete result: " + result + ", info: " + info);
            Message onCompleteMessage = (Message) NetworkRegistrationManager.this.mCallbackTable.remove(this);
            if (onCompleteMessage != null) {
                onCompleteMessage.arg1 = result;
                onCompleteMessage.obj = new AsyncResult(onCompleteMessage.obj, new NetworkRegistrationInfo(info), (Throwable) null);
                onCompleteMessage.sendToTarget();
                return;
            }
            NetworkRegistrationManager.this.loge("onCompleteMessage is null");
        }

        public void onNetworkStateChanged() {
            NetworkRegistrationManager.this.logd("onNetworkStateChanged");
            NetworkRegistrationManager.this.mRegStateChangeRegistrants.notifyRegistrants();
        }
    }

    private void bindService() {
        String packageName = getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            loge("Can't find the binding package");
        } else if (TextUtils.equals(packageName, this.mTargetBindingPackageName)) {
            logd("Service " + packageName + " already bound or being bound.");
        } else {
            INetworkService iNetworkService = this.mINetworkService;
            if (iNetworkService != null && iNetworkService.asBinder().isBinderAlive()) {
                try {
                    this.mINetworkService.removeNetworkServiceProvider(this.mPhone.getPhoneId());
                } catch (RemoteException e) {
                    loge("Cannot remove data service provider. " + e);
                }
                this.mPhone.getContext().unbindService(this.mServiceConnection);
            }
            Intent intent = new Intent("android.telephony.NetworkService");
            intent.setPackage(getPackageName());
            try {
                logd("Trying to bind " + getPackageName() + " for transport " + AccessNetworkConstants.transportTypeToString(this.mTransportType));
                this.mServiceConnection = new NetworkServiceConnection();
                if (!this.mPhone.getContext().bindService(intent, this.mServiceConnection, 1)) {
                    loge("Cannot bind to the data service.");
                } else {
                    this.mTargetBindingPackageName = packageName;
                }
            } catch (SecurityException e2) {
                loge("bindService failed " + e2);
            }
        }
    }

    private String getPackageName() {
        String carrierConfig;
        int resourceId;
        int i = this.mTransportType;
        if (i == 1) {
            resourceId = 17039910;
            carrierConfig = "carrier_network_service_wwan_package_override_string";
        } else if (i == 2) {
            resourceId = 17039908;
            carrierConfig = "carrier_network_service_wlan_package_override_string";
        } else {
            throw new IllegalStateException("Transport type not WWAN or WLAN. type=" + this.mTransportType);
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
    private void logd(String msg) {
        String str = this.mTag;
        Rlog.d(str, "[" + this.mPhone.getPhoneId() + "] " + msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String msg) {
        String str = this.mTag;
        Rlog.e(str, "[" + this.mPhone.getPhoneId() + "] " + msg);
    }
}
