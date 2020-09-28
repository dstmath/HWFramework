package com.android.internal.telephony.dataconnection;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.telephony.AccessNetworkConstants;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.data.ApnSetting;
import android.telephony.data.IQualifiedNetworksService;
import android.telephony.data.IQualifiedNetworksServiceCallback;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.telephony.Phone;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AccessNetworksManager extends Handler {
    private static final boolean DBG = false;
    private static final int EVENT_BIND_QUALIFIED_NETWORKS_SERVICE = 1;
    private static final int[] SUPPORTED_APN_TYPES = {17, 2, 32, 64, 128, 4, 512};
    private static final String TAG = AccessNetworksManager.class.getSimpleName();
    private final SparseArray<int[]> mAvailableNetworks = new SparseArray<>();
    private final CarrierConfigManager mCarrierConfigManager;
    private final BroadcastReceiver mConfigChangedReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.dataconnection.AccessNetworksManager.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction()) && AccessNetworksManager.this.mPhone.getPhoneId() == intent.getIntExtra("android.telephony.extra.SLOT_INDEX", 0)) {
                AccessNetworksManager.this.sendEmptyMessage(1);
            }
        }
    };
    private AccessNetworksManagerDeathRecipient mDeathRecipient;
    private IQualifiedNetworksService mIQualifiedNetworksService;
    private final Phone mPhone;
    private final RegistrantList mQualifiedNetworksChangedRegistrants = new RegistrantList();
    private QualifiedNetworksServiceConnection mServiceConnection;
    private String mTargetBindingPackageName;

    public static class QualifiedNetworks {
        public final int apnType;
        public final int[] qualifiedNetworks;

        public QualifiedNetworks(int apnType2, int[] qualifiedNetworks2) {
            this.apnType = apnType2;
            this.qualifiedNetworks = qualifiedNetworks2;
        }

        public String toString() {
            List<String> accessNetworkStrings = new ArrayList<>();
            for (int network : this.qualifiedNetworks) {
                accessNetworkStrings.add(AccessNetworkConstants.AccessNetworkType.toString(network));
            }
            return "[QualifiedNetworks: apnType=" + ApnSetting.getApnTypeString(this.apnType) + ", networks=" + ((String) Arrays.stream(this.qualifiedNetworks).mapToObj($$Lambda$AccessNetworksManager$QualifiedNetworks$RFnLI6POkxFwKMiSsed1qg8X7t0.INSTANCE).collect(Collectors.joining(","))) + "]";
        }
    }

    /* access modifiers changed from: private */
    public class AccessNetworksManagerDeathRecipient implements IBinder.DeathRecipient {
        private AccessNetworksManagerDeathRecipient() {
        }

        public void binderDied() {
            AccessNetworksManager accessNetworksManager = AccessNetworksManager.this;
            accessNetworksManager.loge("QualifiedNetworksService(" + AccessNetworksManager.this.mTargetBindingPackageName + ") died.");
        }
    }

    /* access modifiers changed from: private */
    public final class QualifiedNetworksServiceConnection implements ServiceConnection {
        private QualifiedNetworksServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            AccessNetworksManager.this.mIQualifiedNetworksService = IQualifiedNetworksService.Stub.asInterface(service);
            AccessNetworksManager accessNetworksManager = AccessNetworksManager.this;
            accessNetworksManager.mDeathRecipient = new AccessNetworksManagerDeathRecipient();
            try {
                service.linkToDeath(AccessNetworksManager.this.mDeathRecipient, 0);
                AccessNetworksManager.this.mIQualifiedNetworksService.createNetworkAvailabilityProvider(AccessNetworksManager.this.mPhone.getPhoneId(), new QualifiedNetworksServiceCallback());
            } catch (RemoteException e) {
                AccessNetworksManager.this.mDeathRecipient.binderDied();
                AccessNetworksManager accessNetworksManager2 = AccessNetworksManager.this;
                accessNetworksManager2.loge("Remote exception. " + e);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            AccessNetworksManager.this.mIQualifiedNetworksService.asBinder().unlinkToDeath(AccessNetworksManager.this.mDeathRecipient, 0);
            AccessNetworksManager.this.mTargetBindingPackageName = null;
        }
    }

    /* access modifiers changed from: private */
    public final class QualifiedNetworksServiceCallback extends IQualifiedNetworksServiceCallback.Stub {
        private QualifiedNetworksServiceCallback() {
        }

        public void onQualifiedNetworkTypesChanged(int apnTypes, int[] qualifiedNetworkTypes) {
            AccessNetworksManager.this.log("onQualifiedNetworkTypesChanged. apnTypes = [" + ApnSetting.getApnTypesStringFromBitmask(apnTypes) + "], networks = [" + ((String) Arrays.stream(qualifiedNetworkTypes).mapToObj($$Lambda$AccessNetworksManager$QualifiedNetworksServiceCallback$ZAur6rkPXYVsjcy4S2I6rXzX3DM.INSTANCE).collect(Collectors.joining(","))) + "]");
            List<QualifiedNetworks> qualifiedNetworksList = new ArrayList<>();
            int[] iArr = AccessNetworksManager.SUPPORTED_APN_TYPES;
            for (int supportedApnType : iArr) {
                if ((apnTypes & supportedApnType) == supportedApnType) {
                    if (AccessNetworksManager.this.mAvailableNetworks.get(supportedApnType) == null || !Arrays.equals((int[]) AccessNetworksManager.this.mAvailableNetworks.get(supportedApnType), qualifiedNetworkTypes)) {
                        AccessNetworksManager.this.mAvailableNetworks.put(supportedApnType, qualifiedNetworkTypes);
                        qualifiedNetworksList.add(new QualifiedNetworks(supportedApnType, qualifiedNetworkTypes));
                    } else {
                        AccessNetworksManager.this.log("Available networks for " + ApnSetting.getApnTypesStringFromBitmask(supportedApnType) + " not changed.");
                    }
                }
            }
            if (!qualifiedNetworksList.isEmpty()) {
                AccessNetworksManager.this.mQualifiedNetworksChangedRegistrants.notifyResult(qualifiedNetworksList);
            }
        }
    }

    public AccessNetworksManager(Phone phone) {
        this.mPhone = phone;
        this.mCarrierConfigManager = (CarrierConfigManager) phone.getContext().getSystemService("carrier_config");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        phone.getContext().registerReceiverAsUser(this.mConfigChangedReceiver, UserHandle.ALL, intentFilter, null, null);
        sendEmptyMessage(1);
    }

    public void handleMessage(Message msg) {
        if (msg.what != 1) {
            loge("Unhandled event " + msg.what);
            return;
        }
        bindQualifiedNetworksService();
    }

    private void bindQualifiedNetworksService() {
        String packageName = getQualifiedNetworksServicePackageName();
        if (TextUtils.isEmpty(packageName)) {
            loge("Can't find the binding package");
        } else if (!TextUtils.equals(packageName, this.mTargetBindingPackageName)) {
            IQualifiedNetworksService iQualifiedNetworksService = this.mIQualifiedNetworksService;
            if (iQualifiedNetworksService != null && iQualifiedNetworksService.asBinder().isBinderAlive()) {
                try {
                    this.mIQualifiedNetworksService.removeNetworkAvailabilityProvider(this.mPhone.getPhoneId());
                } catch (RemoteException e) {
                    loge("Cannot remove network availability updater. " + e);
                }
                this.mPhone.getContext().unbindService(this.mServiceConnection);
            }
            try {
                this.mServiceConnection = new QualifiedNetworksServiceConnection();
                log("bind to " + packageName);
                if (!this.mPhone.getContext().bindService(new Intent("android.telephony.data.QualifiedNetworksService").setPackage(packageName), this.mServiceConnection, 1)) {
                    loge("Cannot bind to the qualified networks service.");
                } else {
                    this.mTargetBindingPackageName = packageName;
                }
            } catch (Exception e2) {
                loge("Cannot bind to the qualified networks service. Exception: " + e2);
            }
        }
    }

    private String getQualifiedNetworksServicePackageName() {
        String packageName = this.mPhone.getContext().getResources().getString(17039885);
        PersistableBundle b = this.mCarrierConfigManager.getConfigForSubId(this.mPhone.getSubId());
        if (b == null) {
            return packageName;
        }
        String carrierConfigPackageName = b.getString("carrier_qualified_networks_service_package_override_string");
        return !TextUtils.isEmpty(carrierConfigPackageName) ? carrierConfigPackageName : packageName;
    }

    private List<QualifiedNetworks> getQualifiedNetworksList() {
        List<QualifiedNetworks> qualifiedNetworksList = new ArrayList<>();
        for (int i = 0; i < this.mAvailableNetworks.size(); i++) {
            qualifiedNetworksList.add(new QualifiedNetworks(this.mAvailableNetworks.keyAt(i), this.mAvailableNetworks.valueAt(i)));
        }
        return qualifiedNetworksList;
    }

    public void registerForQualifiedNetworksChanged(Handler h, int what) {
        if (h != null) {
            Registrant r = new Registrant(h, what, (Object) null);
            this.mQualifiedNetworksChangedRegistrants.add(r);
            if (this.mAvailableNetworks.size() != 0) {
                r.notifyResult(getQualifiedNetworksList());
            }
        }
    }

    public void unregisterForQualifiedNetworksChanged(Handler h) {
        if (h != null) {
            this.mQualifiedNetworksChangedRegistrants.remove(h);
        }
    }

    public void dump(FileDescriptor fd, IndentingPrintWriter pw, String[] args) {
        pw.println("AccessNetworksManager:");
        pw.increaseIndent();
        pw.println("Available networks:");
        pw.increaseIndent();
        for (int i = 0; i < this.mAvailableNetworks.size(); i++) {
            pw.println("APN type " + ApnSetting.getApnTypeString(this.mAvailableNetworks.keyAt(i)) + ": [" + ((String) Arrays.stream(this.mAvailableNetworks.valueAt(i)).mapToObj($$Lambda$AccessNetworksManager$Su9aGPx8cN_dALH_BE7MctE6qX8.INSTANCE).collect(Collectors.joining(","))) + "]");
        }
        pw.decreaseIndent();
        pw.decreaseIndent();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        Rlog.i(TAG, s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String s) {
        Rlog.e(TAG, s);
    }
}
