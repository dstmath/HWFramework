package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
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
    @UnsupportedAppUsage
    private Context mContext;
    @UnsupportedAppUsage
    private Handler mHandler = new Handler() {
        /* class com.android.internal.telephony.CarrierServiceBindHelper.AnonymousClass2 */

        public void handleMessage(Message msg) {
            CarrierServiceBindHelper.log("mHandler: " + msg.what);
            int i = msg.what;
            if (i == 0) {
                AppBinding binding = (AppBinding) msg.obj;
                CarrierServiceBindHelper.log("Rebinding if necessary for phoneId: " + binding.getPhoneId());
                binding.rebind();
            } else if (i == 1) {
                ((AppBinding) msg.obj).performImmediateUnbind();
            }
        }
    };
    private String[] mLastSimState;
    private final PackageMonitor mPackageMonitor = new CarrierServicePackageMonitor();
    private BroadcastReceiver mUserUnlockedReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.CarrierServiceBindHelper.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            CarrierServiceBindHelper.log("Received " + action);
            if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                for (int phoneId = 0; phoneId < CarrierServiceBindHelper.this.mBindings.length; phoneId++) {
                    CarrierServiceBindHelper.this.mBindings[phoneId].rebind();
                }
            }
        }
    };

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

    /* access modifiers changed from: package-private */
    public void updateForPhoneId(int phoneId, String simState) {
        log("update binding for phoneId: " + phoneId + " simState: " + simState);
        if (SubscriptionManager.isValidPhoneId(phoneId) && !TextUtils.isEmpty(simState)) {
            String[] strArr = this.mLastSimState;
            if (phoneId < strArr.length && !simState.equals(strArr[phoneId])) {
                this.mLastSimState[phoneId] = simState;
                Handler handler = this.mHandler;
                handler.sendMessage(handler.obtainMessage(0, this.mBindings[phoneId]));
            }
        }
    }

    /* access modifiers changed from: private */
    public class AppBinding {
        private int bindCount;
        private String carrierPackage;
        private String carrierServiceClass;
        private CarrierServiceConnection connection;
        private long lastBindStartMillis;
        private long lastUnbindMillis;
        private long mUnbindScheduledUptimeMillis = -1;
        private int phoneId;
        private int unbindCount;

        public AppBinding(int phoneId2) {
            this.phoneId = phoneId2;
        }

        public int getPhoneId() {
            return this.phoneId;
        }

        public String getPackage() {
            return this.carrierPackage;
        }

        /* access modifiers changed from: package-private */
        public void rebind() {
            String candidateServiceClass;
            Bundle metadata;
            String error;
            List<String> carrierPackageNames = TelephonyManager.from(CarrierServiceBindHelper.this.mContext).getCarrierPackageNamesForIntentAndPhone(new Intent("android.service.carrier.CarrierService"), this.phoneId);
            if (carrierPackageNames == null || carrierPackageNames.size() <= 0) {
                CarrierServiceBindHelper.log("No carrier app for: " + this.phoneId);
                unbind(false);
                return;
            }
            CarrierServiceBindHelper.log("Found carrier app: " + carrierPackageNames);
            String candidateCarrierPackage = carrierPackageNames.get(0);
            if (!TextUtils.equals(this.carrierPackage, candidateCarrierPackage)) {
                unbind(true);
            }
            Intent carrierService = new Intent("android.service.carrier.CarrierService");
            carrierService.setPackage(candidateCarrierPackage);
            ResolveInfo carrierResolveInfo = CarrierServiceBindHelper.this.mContext.getPackageManager().resolveService(carrierService, 128);
            if (carrierResolveInfo != null) {
                metadata = carrierResolveInfo.serviceInfo.metaData;
                candidateServiceClass = carrierResolveInfo.getComponentInfo().getComponentName().getClassName();
            } else {
                metadata = null;
                candidateServiceClass = null;
            }
            if (metadata == null || !metadata.getBoolean("android.service.carrier.LONG_LIVED_BINDING", false)) {
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
            this.bindCount = this.bindCount + 1;
            this.lastBindStartMillis = System.currentTimeMillis();
            this.connection = new CarrierServiceConnection();
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

        /* access modifiers changed from: package-private */
        public void unbind(boolean immediate) {
            CarrierServiceConnection carrierServiceConnection = this.connection;
            if (carrierServiceConnection != null) {
                if (immediate || !carrierServiceConnection.connected) {
                    cancelScheduledUnbind();
                    performImmediateUnbind();
                } else if (this.mUnbindScheduledUptimeMillis == -1) {
                    this.mUnbindScheduledUptimeMillis = 30000 + SystemClock.uptimeMillis();
                    CarrierServiceBindHelper.log("Scheduling unbind in 30000 millis");
                    CarrierServiceBindHelper.this.mHandler.sendMessageAtTime(CarrierServiceBindHelper.this.mHandler.obtainMessage(1, this), this.mUnbindScheduledUptimeMillis);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    public class CarrierServiceConnection implements ServiceConnection {
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
                for (String packageName : packages) {
                    evaluateBinding(packageName, true);
                }
            }
            return CarrierServiceBindHelper.super.onHandleForceStop(intent, packages, uid, doit);
        }

        private void evaluateBinding(String carrierPackageName, boolean forceUnbind) {
            AppBinding[] appBindingArr = CarrierServiceBindHelper.this.mBindings;
            for (AppBinding appBinding : appBindingArr) {
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

    /* access modifiers changed from: private */
    public static void log(String message) {
        Log.d(LOG_TAG, message);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("CarrierServiceBindHelper:");
        for (AppBinding binding : this.mBindings) {
            binding.dump(fd, pw, args);
        }
    }
}
