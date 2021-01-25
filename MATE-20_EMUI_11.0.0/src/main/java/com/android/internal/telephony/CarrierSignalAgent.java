package com.android.internal.telephony;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CarrierSignalAgent extends Handler {
    private static final String CARRIER_SIGNAL_DELIMITER = "\\s*,\\s*";
    private static final String COMPONENT_NAME_DELIMITER = "\\s*:\\s*";
    private static final boolean DBG = true;
    private static final int EVENT_REGISTER_DEFAULT_NETWORK_AVAIL = 0;
    private static final String LOG_TAG = CarrierSignalAgent.class.getSimpleName();
    private static final boolean NO_WAKE = false;
    private static final boolean VDBG = Rlog.isLoggable(LOG_TAG, 2);
    private static final boolean WAKE = true;
    private Map<String, Set<ComponentName>> mCachedNoWakeSignalConfigs = new HashMap();
    private Map<String, Set<ComponentName>> mCachedWakeSignalConfigs = new HashMap();
    private final Set<String> mCarrierSignalList = new HashSet(Arrays.asList("com.android.internal.telephony.CARRIER_SIGNAL_PCO_VALUE", "com.android.internal.telephony.CARRIER_SIGNAL_REDIRECTED", "com.android.internal.telephony.CARRIER_SIGNAL_REQUEST_NETWORK_FAILED", "com.android.internal.telephony.CARRIER_SIGNAL_RESET", "com.android.internal.telephony.CARRIER_SIGNAL_DEFAULT_NETWORK_AVAILABLE"));
    private boolean mDefaultNetworkAvail;
    private final LocalLog mErrorLocalLog = new LocalLog(10);
    private ConnectivityManager.NetworkCallback mNetworkCallback;
    private final Phone mPhone;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.CarrierSignalAgent.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            CarrierSignalAgent carrierSignalAgent = CarrierSignalAgent.this;
            carrierSignalAgent.log("CarrierSignalAgent receiver action: " + action);
            if (action != null && action.equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                CarrierSignalAgent.this.loadCarrierConfig();
            }
        }
    };

    public CarrierSignalAgent(Phone phone) {
        this.mPhone = phone;
        loadCarrierConfig();
        this.mPhone.getContext().registerReceiver(this.mReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
        this.mPhone.getCarrierActionAgent().registerForCarrierAction(3, this, 0, null, false);
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        if (msg.what == 0) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null) {
                String str = LOG_TAG;
                Rlog.e(str, "Register default network exception: " + ar.exception);
                return;
            }
            ConnectivityManager connectivityMgr = ConnectivityManager.from(this.mPhone.getContext());
            if (((Boolean) ar.result).booleanValue()) {
                this.mNetworkCallback = new ConnectivityManager.NetworkCallback() {
                    /* class com.android.internal.telephony.CarrierSignalAgent.AnonymousClass2 */

                    @Override // android.net.ConnectivityManager.NetworkCallback
                    public void onAvailable(Network network) {
                        if (!CarrierSignalAgent.this.mDefaultNetworkAvail) {
                            CarrierSignalAgent carrierSignalAgent = CarrierSignalAgent.this;
                            carrierSignalAgent.log("Default network available: " + network);
                            Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_DEFAULT_NETWORK_AVAILABLE");
                            intent.putExtra("defaultNetworkAvailable", true);
                            CarrierSignalAgent.this.notifyCarrierSignalReceivers(intent);
                            CarrierSignalAgent.this.mDefaultNetworkAvail = true;
                        }
                    }

                    @Override // android.net.ConnectivityManager.NetworkCallback
                    public void onLost(Network network) {
                        CarrierSignalAgent carrierSignalAgent = CarrierSignalAgent.this;
                        carrierSignalAgent.log("Default network lost: " + network);
                        Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_DEFAULT_NETWORK_AVAILABLE");
                        intent.putExtra("defaultNetworkAvailable", false);
                        CarrierSignalAgent.this.notifyCarrierSignalReceivers(intent);
                        CarrierSignalAgent.this.mDefaultNetworkAvail = false;
                    }
                };
                connectivityMgr.registerDefaultNetworkCallback(this.mNetworkCallback, this.mPhone);
                log("Register default network");
                return;
            }
            ConnectivityManager.NetworkCallback networkCallback = this.mNetworkCallback;
            if (networkCallback != null) {
                connectivityMgr.unregisterNetworkCallback(networkCallback);
                this.mNetworkCallback = null;
                this.mDefaultNetworkAvail = false;
                log("unregister default network");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadCarrierConfig() {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfig();
        }
        if (b != null) {
            synchronized (this.mCachedWakeSignalConfigs) {
                log("Loading carrier config: carrier_app_wake_signal_config");
                Map<String, Set<ComponentName>> config = parseAndCache(b.getStringArray("carrier_app_wake_signal_config"));
                if (!this.mCachedWakeSignalConfigs.isEmpty() && !config.equals(this.mCachedWakeSignalConfigs)) {
                    if (VDBG) {
                        log("carrier config changed, reset receivers from old config");
                    }
                    this.mPhone.getCarrierActionAgent().sendEmptyMessage(2);
                }
                this.mCachedWakeSignalConfigs = config;
            }
            synchronized (this.mCachedNoWakeSignalConfigs) {
                log("Loading carrier config: carrier_app_no_wake_signal_config");
                Map<String, Set<ComponentName>> config2 = parseAndCache(b.getStringArray("carrier_app_no_wake_signal_config"));
                if (!this.mCachedNoWakeSignalConfigs.isEmpty() && !config2.equals(this.mCachedNoWakeSignalConfigs)) {
                    if (VDBG) {
                        log("carrier config changed, reset receivers from old config");
                    }
                    this.mPhone.getCarrierActionAgent().sendEmptyMessage(2);
                }
                this.mCachedNoWakeSignalConfigs = config2;
            }
        }
    }

    private Map<String, Set<ComponentName>> parseAndCache(String[] configs) {
        Map<String, Set<ComponentName>> newCachedWakeSignalConfigs = new HashMap<>();
        if (!ArrayUtils.isEmpty(configs)) {
            for (String config : configs) {
                if (!TextUtils.isEmpty(config)) {
                    String[] splitStr = config.trim().split(COMPONENT_NAME_DELIMITER, 2);
                    if (splitStr.length == 2) {
                        ComponentName componentName = ComponentName.unflattenFromString(splitStr[0]);
                        if (componentName == null) {
                            loge("Invalid component name: " + splitStr[0]);
                        } else {
                            String[] signals = splitStr[1].split(CARRIER_SIGNAL_DELIMITER);
                            for (String s : signals) {
                                if (!this.mCarrierSignalList.contains(s)) {
                                    loge("Invalid signal name: " + s);
                                } else {
                                    Set<ComponentName> componentList = newCachedWakeSignalConfigs.get(s);
                                    if (componentList == null) {
                                        componentList = new HashSet<>();
                                        newCachedWakeSignalConfigs.put(s, componentList);
                                    }
                                    componentList.add(componentName);
                                    if (VDBG) {
                                        logv("Add config {signal: " + s + " componentName: " + componentName + "}");
                                    }
                                }
                            }
                        }
                    } else {
                        loge("invalid config format: " + config);
                    }
                }
            }
        }
        return newCachedWakeSignalConfigs;
    }

    public boolean hasRegisteredReceivers(String action) {
        return this.mCachedWakeSignalConfigs.containsKey(action) || this.mCachedNoWakeSignalConfigs.containsKey(action);
    }

    private void broadcast(Intent intent, Set<ComponentName> receivers, boolean wakeup) {
        PackageManager packageManager = this.mPhone.getContext().getPackageManager();
        for (ComponentName name : receivers) {
            Intent signal = new Intent(intent);
            signal.setComponent(name);
            if (wakeup && packageManager.queryBroadcastReceivers(signal, 65536).isEmpty()) {
                loge("Carrier signal receivers are configured but unavailable: " + signal.getComponent());
            } else if (wakeup || packageManager.queryBroadcastReceivers(signal, 65536).isEmpty()) {
                signal.putExtra("android.telephony.extra.SUBSCRIPTION_INDEX", this.mPhone.getSubId());
                signal.putExtra("subscription", this.mPhone.getSubId());
                signal.addFlags(268435456);
                if (!wakeup) {
                    signal.setFlags(16);
                }
                try {
                    this.mPhone.getContext().sendBroadcastAsUser(signal, UserHandle.ALL);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Sending signal ");
                    sb.append(signal.getAction());
                    sb.append(signal.getComponent() != null ? " to the carrier signal receiver: " + signal.getComponent() : PhoneConfigurationManager.SSSS);
                    log(sb.toString());
                } catch (ActivityNotFoundException e) {
                    loge("Send broadcast failed: " + e);
                }
            } else {
                loge("Runtime signals shouldn't be configured in Manifest: " + signal.getComponent());
            }
        }
    }

    public void notifyCarrierSignalReceivers(Intent intent) {
        synchronized (this.mCachedWakeSignalConfigs) {
            Set<ComponentName> receiverSet = this.mCachedWakeSignalConfigs.get(intent.getAction());
            if (!ArrayUtils.isEmpty(receiverSet)) {
                broadcast(intent, receiverSet, true);
            }
        }
        synchronized (this.mCachedNoWakeSignalConfigs) {
            Set<ComponentName> receiverSet2 = this.mCachedNoWakeSignalConfigs.get(intent.getAction());
            if (!ArrayUtils.isEmpty(receiverSet2)) {
                broadcast(intent, receiverSet2, false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        String str = LOG_TAG;
        Rlog.d(str, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void loge(String s) {
        this.mErrorLocalLog.log(s);
        String str = LOG_TAG;
        Rlog.e(str, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void logv(String s) {
        String str = LOG_TAG;
        Rlog.v(str, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        pw.println("mCachedWakeSignalConfigs:");
        ipw.increaseIndent();
        for (Map.Entry<String, Set<ComponentName>> entry : this.mCachedWakeSignalConfigs.entrySet()) {
            pw.println("signal: " + entry.getKey() + " componentName list: " + entry.getValue());
        }
        ipw.decreaseIndent();
        pw.println("mCachedNoWakeSignalConfigs:");
        ipw.increaseIndent();
        for (Map.Entry<String, Set<ComponentName>> entry2 : this.mCachedNoWakeSignalConfigs.entrySet()) {
            pw.println("signal: " + entry2.getKey() + " componentName list: " + entry2.getValue());
        }
        ipw.decreaseIndent();
        pw.println("mDefaultNetworkAvail: " + this.mDefaultNetworkAvail);
        pw.println("error log:");
        ipw.increaseIndent();
        this.mErrorLocalLog.dump(fd, pw, args);
        ipw.decreaseIndent();
    }
}
