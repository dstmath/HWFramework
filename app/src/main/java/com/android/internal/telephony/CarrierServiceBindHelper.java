package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.UserHandle;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import com.google.android.mms.pdu.PduPart;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class CarrierServiceBindHelper {
    private static final int EVENT_REBIND = 0;
    private static final String LOG_TAG = "CarrierSvcBindHelper";
    private AppBinding[] mBindings;
    private Context mContext;
    private Handler mHandler;
    private String[] mLastSimState;
    private final PackageMonitor mPackageMonitor;
    private BroadcastReceiver mUserUnlockedReceiver;

    private class AppBinding {
        private int bindCount;
        private String carrierPackage;
        private String carrierServiceClass;
        private CarrierServiceConnection connection;
        private long lastBindStartMillis;
        private long lastUnbindMillis;
        private int phoneId;
        private int unbindCount;

        public AppBinding(int phoneId) {
            this.phoneId = phoneId;
        }

        public int getPhoneId() {
            return this.phoneId;
        }

        public String getPackage() {
            return this.carrierPackage;
        }

        void rebind() {
            List<String> carrierPackageNames = TelephonyManager.from(CarrierServiceBindHelper.this.mContext).getCarrierPackageNamesForIntentAndPhone(new Intent("android.service.carrier.CarrierService"), this.phoneId);
            if (carrierPackageNames == null || carrierPackageNames.size() <= 0) {
                CarrierServiceBindHelper.log("No carrier app for: " + this.phoneId);
                unbind();
                return;
            }
            CarrierServiceBindHelper.log("Found carrier app: " + carrierPackageNames);
            String candidateCarrierPackage = (String) carrierPackageNames.get(CarrierServiceBindHelper.EVENT_REBIND);
            if (!TextUtils.equals(this.carrierPackage, candidateCarrierPackage)) {
                unbind();
            }
            Intent carrierService = new Intent("android.service.carrier.CarrierService");
            carrierService.setPackage(candidateCarrierPackage);
            ResolveInfo carrierResolveInfo = CarrierServiceBindHelper.this.mContext.getPackageManager().resolveService(carrierService, PduPart.P_Q);
            Bundle bundle = null;
            Object candidateServiceClass = null;
            if (carrierResolveInfo != null) {
                bundle = carrierResolveInfo.serviceInfo.metaData;
                candidateServiceClass = carrierResolveInfo.getComponentInfo().getComponentName().getClassName();
            }
            if (bundle == null || !bundle.getBoolean("android.service.carrier.LONG_LIVED_BINDING", false)) {
                CarrierServiceBindHelper.log("Carrier app does not want a long lived binding");
                unbind();
                return;
            }
            if (!TextUtils.equals(this.carrierServiceClass, candidateServiceClass)) {
                unbind();
            } else if (this.connection != null) {
                return;
            }
            this.carrierPackage = candidateCarrierPackage;
            this.carrierServiceClass = candidateServiceClass;
            CarrierServiceBindHelper.log("Binding to " + this.carrierPackage + " for phone " + this.phoneId);
            this.bindCount++;
            this.lastBindStartMillis = System.currentTimeMillis();
            this.connection = new CarrierServiceConnection(null);
            String error;
            try {
                if (!CarrierServiceBindHelper.this.mContext.bindService(carrierService, this.connection, 67108865)) {
                    error = "bindService returned false";
                    CarrierServiceBindHelper.log("Unable to bind to " + this.carrierPackage + " for phone " + this.phoneId + ". Error: " + error);
                    unbind();
                }
            } catch (SecurityException ex) {
                error = ex.getMessage();
            }
        }

        void unbind() {
            if (this.connection != null) {
                this.unbindCount++;
                this.lastUnbindMillis = System.currentTimeMillis();
                this.carrierPackage = null;
                this.carrierServiceClass = null;
                CarrierServiceBindHelper.log("Unbinding from carrier app");
                CarrierServiceBindHelper.this.mContext.unbindService(this.connection);
                this.connection = null;
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("Carrier app binding for phone " + this.phoneId);
            pw.println("  connection: " + this.connection);
            pw.println("  bindCount: " + this.bindCount);
            pw.println("  lastBindStartMillis: " + this.lastBindStartMillis);
            pw.println("  unbindCount: " + this.unbindCount);
            pw.println("  lastUnbindMillis: " + this.lastUnbindMillis);
            pw.println();
        }
    }

    private class CarrierServiceConnection implements ServiceConnection {
        private boolean connected;

        private CarrierServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            CarrierServiceBindHelper.log("Connected to carrier app: " + name.flattenToString());
            this.connected = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            CarrierServiceBindHelper.log("Disconnected from carrier app: " + name.flattenToString());
            this.connected = false;
        }

        public String toString() {
            return "CarrierServiceConnection[connected=" + this.connected + "]";
        }
    }

    private class CarrierServicePackageMonitor extends PackageMonitor {
        private CarrierServicePackageMonitor() {
        }

        public void onPackageAdded(String packageName, int reason) {
            evaluateBinding(packageName, true);
        }

        public void onPackageRemoved(String packageName, int reason) {
            evaluateBinding(packageName, true);
        }

        public void onPackageUpdateFinished(String packageName, int uid) {
            evaluateBinding(packageName, true);
        }

        public void onPackageModified(String packageName) {
            evaluateBinding(packageName, false);
        }

        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            if (doit) {
                int length = packages.length;
                for (int i = CarrierServiceBindHelper.EVENT_REBIND; i < length; i++) {
                    evaluateBinding(packages[i], true);
                }
            }
            return super.onHandleForceStop(intent, packages, uid, doit);
        }

        private void evaluateBinding(String carrierPackageName, boolean forceUnbind) {
            AppBinding[] -get0 = CarrierServiceBindHelper.this.mBindings;
            int length = -get0.length;
            for (int i = CarrierServiceBindHelper.EVENT_REBIND; i < length; i++) {
                AppBinding appBinding = -get0[i];
                String appBindingPackage = appBinding.getPackage();
                boolean isBindingForPackage = carrierPackageName.equals(appBindingPackage);
                if (isBindingForPackage) {
                    CarrierServiceBindHelper.log(carrierPackageName + " changed and corresponds to a phone. Rebinding.");
                }
                if (appBindingPackage == null || isBindingForPackage) {
                    if (forceUnbind) {
                        appBinding.unbind();
                    }
                    appBinding.rebind();
                }
            }
        }
    }

    public CarrierServiceBindHelper(Context context) {
        this.mPackageMonitor = new CarrierServicePackageMonitor();
        this.mUserUnlockedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                CarrierServiceBindHelper.log("Received " + action);
                if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                    for (int phoneId = CarrierServiceBindHelper.EVENT_REBIND; phoneId < CarrierServiceBindHelper.this.mBindings.length; phoneId++) {
                        CarrierServiceBindHelper.this.mBindings[phoneId].rebind();
                    }
                }
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                CarrierServiceBindHelper.log("mHandler: " + msg.what);
                switch (msg.what) {
                    case CarrierServiceBindHelper.EVENT_REBIND /*0*/:
                        AppBinding binding = msg.obj;
                        CarrierServiceBindHelper.log("Rebinding if necessary for phoneId: " + binding.getPhoneId());
                        binding.rebind();
                    default:
                }
            }
        };
        this.mContext = context;
        int numPhones = TelephonyManager.from(context).getPhoneCount();
        this.mBindings = new AppBinding[numPhones];
        this.mLastSimState = new String[numPhones];
        for (int phoneId = EVENT_REBIND; phoneId < numPhones; phoneId++) {
            this.mBindings[phoneId] = new AppBinding(phoneId);
        }
        this.mPackageMonitor.register(context, this.mHandler.getLooper(), UserHandle.ALL, false);
        this.mContext.registerReceiverAsUser(this.mUserUnlockedReceiver, UserHandle.SYSTEM, new IntentFilter("android.intent.action.USER_UNLOCKED"), null, this.mHandler);
    }

    void updateForPhoneId(int phoneId, String simState) {
        log("update binding for phoneId: " + phoneId + " simState: " + simState);
        if (SubscriptionManager.isValidPhoneId(phoneId) && !TextUtils.isEmpty(simState) && !simState.equals(this.mLastSimState[phoneId])) {
            this.mLastSimState[phoneId] = simState;
            this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_REBIND, this.mBindings[phoneId]));
        }
    }

    private static void log(String message) {
        Log.d(LOG_TAG, message);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("CarrierServiceBindHelper:");
        AppBinding[] appBindingArr = this.mBindings;
        int length = appBindingArr.length;
        for (int i = EVENT_REBIND; i < length; i++) {
            appBindingArr[i].dump(fd, pw, args);
        }
    }
}
