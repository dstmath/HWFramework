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
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class CarrierServiceBindHelper {
    private static final int EVENT_PERFORM_IMMEDIATE_UNBIND = 1;
    private static final int EVENT_REBIND = 0;
    private static final String LOG_TAG = "CarrierSvcBindHelper";
    private static final int UNBIND_DELAY_MILLIS = 30000;
    private AppBinding[] mBindings;
    private Context mContext;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            CarrierServiceBindHelper.log("mHandler: " + msg.what);
            switch (msg.what) {
                case 0:
                    AppBinding binding = msg.obj;
                    CarrierServiceBindHelper.log("Rebinding if necessary for phoneId: " + binding.getPhoneId());
                    binding.rebind();
                    return;
                case 1:
                    ((AppBinding) msg.obj).performImmediateUnbind();
                    return;
                default:
                    return;
            }
        }
    };
    private String[] mLastSimState;
    private final PackageMonitor mPackageMonitor = new CarrierServicePackageMonitor(this, null);
    private BroadcastReceiver mUserUnlockedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            CarrierServiceBindHelper.log("Received " + action);
            if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                for (AppBinding rebind : CarrierServiceBindHelper.this.mBindings) {
                    rebind.rebind();
                }
            }
        }
    };

    private class AppBinding {
        private int bindCount;
        private String carrierPackage;
        private String carrierServiceClass;
        private CarrierServiceConnection connection;
        private long lastBindStartMillis;
        private long lastUnbindMillis;
        private long mUnbindScheduledUptimeMillis = -1;
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
                unbind(false);
                return;
            }
            CarrierServiceBindHelper.log("Found carrier app: " + carrierPackageNames);
            String candidateCarrierPackage = (String) carrierPackageNames.get(0);
            if (!TextUtils.equals(this.carrierPackage, candidateCarrierPackage)) {
                unbind(true);
            }
            Intent carrierService = new Intent("android.service.carrier.CarrierService");
            carrierService.setPackage(candidateCarrierPackage);
            ResolveInfo carrierResolveInfo = CarrierServiceBindHelper.this.mContext.getPackageManager().resolveService(carrierService, 128);
            Bundle metadata = null;
            Object candidateServiceClass = null;
            if (carrierResolveInfo != null) {
                metadata = carrierResolveInfo.serviceInfo.metaData;
                candidateServiceClass = carrierResolveInfo.getComponentInfo().getComponentName().getClassName();
            }
            if (metadata == null || (metadata.getBoolean("android.service.carrier.LONG_LIVED_BINDING", false) ^ 1) != 0) {
                CarrierServiceBindHelper.log("Carrier app does not want a long lived binding");
                unbind(true);
                return;
            }
            if (!TextUtils.equals(this.carrierServiceClass, candidateServiceClass)) {
                unbind(true);
            } else if (this.connection != null) {
                cancelScheduledUnbind();
                return;
            }
            this.carrierPackage = candidateCarrierPackage;
            this.carrierServiceClass = candidateServiceClass;
            CarrierServiceBindHelper.log("Binding to " + this.carrierPackage + " for phone " + this.phoneId);
            this.bindCount++;
            this.lastBindStartMillis = System.currentTimeMillis();
            this.connection = new CarrierServiceConnection(CarrierServiceBindHelper.this, null);
            String error;
            try {
                if (!CarrierServiceBindHelper.this.mContext.bindServiceAsUser(carrierService, this.connection, 67108865, CarrierServiceBindHelper.this.mHandler, Process.myUserHandle())) {
                    error = "bindService returned false";
                    CarrierServiceBindHelper.log("Unable to bind to " + this.carrierPackage + " for phone " + this.phoneId + ". Error: " + error);
                    unbind(true);
                }
            } catch (SecurityException ex) {
                error = ex.getMessage();
            }
        }

        void unbind(boolean immediate) {
            if (this.connection != null) {
                if (immediate || (this.connection.connected ^ 1) != 0) {
                    cancelScheduledUnbind();
                    performImmediateUnbind();
                } else if (this.mUnbindScheduledUptimeMillis == -1) {
                    this.mUnbindScheduledUptimeMillis = 30000 + SystemClock.uptimeMillis();
                    CarrierServiceBindHelper.log("Scheduling unbind in 30000 millis");
                    CarrierServiceBindHelper.this.mHandler.sendMessageAtTime(CarrierServiceBindHelper.this.mHandler.obtainMessage(1, this), this.mUnbindScheduledUptimeMillis);
                }
            }
        }

        private void performImmediateUnbind() {
            this.unbindCount++;
            this.lastUnbindMillis = System.currentTimeMillis();
            this.carrierPackage = null;
            this.carrierServiceClass = null;
            CarrierServiceBindHelper.log("Unbinding from carrier app");
            CarrierServiceBindHelper.this.mContext.unbindService(this.connection);
            this.connection = null;
            this.mUnbindScheduledUptimeMillis = -1;
        }

        private void cancelScheduledUnbind() {
            CarrierServiceBindHelper.this.mHandler.removeMessages(1);
            this.mUnbindScheduledUptimeMillis = -1;
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("Carrier app binding for phone " + this.phoneId);
            pw.println("  connection: " + this.connection);
            pw.println("  bindCount: " + this.bindCount);
            pw.println("  lastBindStartMillis: " + this.lastBindStartMillis);
            pw.println("  unbindCount: " + this.unbindCount);
            pw.println("  lastUnbindMillis: " + this.lastUnbindMillis);
            pw.println("  mUnbindScheduledUptimeMillis: " + this.mUnbindScheduledUptimeMillis);
            pw.println();
        }
    }

    private class CarrierServiceConnection implements ServiceConnection {
        private boolean connected;

        /* synthetic */ CarrierServiceConnection(CarrierServiceBindHelper this$0, CarrierServiceConnection -this1) {
            this();
        }

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
        /* synthetic */ CarrierServicePackageMonitor(CarrierServiceBindHelper this$0, CarrierServicePackageMonitor -this1) {
            this();
        }

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
                for (String packageName : packages) {
                    evaluateBinding(packageName, true);
                }
            }
            return super.onHandleForceStop(intent, packages, uid, doit);
        }

        private void evaluateBinding(String carrierPackageName, boolean forceUnbind) {
            for (AppBinding appBinding : CarrierServiceBindHelper.this.mBindings) {
                String appBindingPackage = appBinding.getPackage();
                boolean isBindingForPackage = carrierPackageName.equals(appBindingPackage);
                if (isBindingForPackage) {
                    CarrierServiceBindHelper.log(carrierPackageName + " changed and corresponds to a phone. Rebinding.");
                }
                if (appBindingPackage == null || isBindingForPackage) {
                    if (forceUnbind) {
                        appBinding.unbind(true);
                    }
                    appBinding.rebind();
                }
            }
        }
    }

    public CarrierServiceBindHelper(Context context) {
        this.mContext = context;
        int numPhones = TelephonyManager.from(context).getPhoneCount();
        this.mBindings = new AppBinding[numPhones];
        this.mLastSimState = new String[numPhones];
        for (int phoneId = 0; phoneId < numPhones; phoneId++) {
            this.mBindings[phoneId] = new AppBinding(phoneId);
        }
        this.mPackageMonitor.register(context, this.mHandler.getLooper(), UserHandle.ALL, false);
        this.mContext.registerReceiverAsUser(this.mUserUnlockedReceiver, UserHandle.SYSTEM, new IntentFilter("android.intent.action.USER_UNLOCKED"), null, this.mHandler);
    }

    void updateForPhoneId(int phoneId, String simState) {
        log("update binding for phoneId: " + phoneId + " simState: " + simState);
        if (SubscriptionManager.isValidPhoneId(phoneId) && !TextUtils.isEmpty(simState) && !simState.equals(this.mLastSimState[phoneId])) {
            this.mLastSimState[phoneId] = simState;
            this.mHandler.sendMessage(this.mHandler.obtainMessage(0, this.mBindings[phoneId]));
        }
    }

    private static void log(String message) {
        Log.d(LOG_TAG, message);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("CarrierServiceBindHelper:");
        for (AppBinding binding : this.mBindings) {
            binding.dump(fd, pw, args);
        }
    }
}
