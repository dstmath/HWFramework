package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.LocalServerSocket;
import android.os.Looper;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.AnomalyReporter;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.os.BackgroundThread;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.dataconnection.TelephonyNetworkFactory;
import com.android.internal.telephony.euicc.EuiccCardController;
import com.android.internal.telephony.euicc.EuiccController;
import com.android.internal.telephony.ims.ImsResolver;
import com.android.internal.telephony.imsphone.ImsPhoneFactory;
import com.android.internal.telephony.sip.SipPhone;
import com.android.internal.telephony.sip.SipPhoneFactory;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.NotificationChannelController;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.IndentingPrintWriter;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class PhoneFactory {
    private static final String ACTION_MAKE_DEFAULT_PHONE_DONE = "com.huawei.intent.action.MAKE_DEFAULT_PHONE_DONE";
    static final boolean DBG = false;
    public static final boolean IS_DUAL_VOLTE_SUPPORTED = HwModemCapability.isCapabilitySupport(21);
    private static final boolean IS_FULL_NETWORK_SUPPORTED_IN_HISI = HwTelephonyFactory.getHwUiccManager().isFullNetworkSupported();
    public static final boolean IS_QCOM_DUAL_LTE_STACK = HwModemCapability.isCapabilitySupport(27);
    static final String LOG_TAG = "PhoneFactory";
    public static final int MAX_ACTIVE_PHONES;
    static final int SOCKET_OPEN_MAX_RETRY = 3;
    static final int SOCKET_OPEN_RETRY_MILLIS = 2000;
    private static final boolean mIsAdaptMultiSimConfiguration = SystemProperties.getBoolean("ro.config.multi_sim_cfg_adapt", false);
    private static CellularNetworkValidator sCellularNetworkValidator;
    @UnsupportedAppUsage
    private static CommandsInterface sCommandsInterface = null;
    private static CommandsInterface[] sCommandsInterfaces = null;
    @UnsupportedAppUsage
    private static Context sContext;
    private static EuiccCardController sEuiccCardController;
    private static EuiccController sEuiccController;
    private static ImsResolver sImsResolver;
    private static IntentBroadcaster sIntentBroadcaster;
    private static final HashMap<String, LocalLog> sLocalLogs = new HashMap<>();
    static final Object sLockProxyPhones = new Object();
    @UnsupportedAppUsage
    private static boolean sMadeDefaults = false;
    private static NotificationChannelController sNotificationChannelController;
    private static Phone sPhone = null;
    private static PhoneConfigurationManager sPhoneConfigurationManager;
    @UnsupportedAppUsage
    private static PhoneNotifier sPhoneNotifier;
    private static PhoneSwitcher sPhoneSwitcher;
    private static Phone[] sPhones = null;
    private static ProxyController sProxyController;
    private static SubscriptionInfoUpdater sSubInfoRecordUpdater = null;
    private static SubscriptionMonitor sSubscriptionMonitor;
    private static TelephonyNetworkFactory[] sTelephonyNetworkFactories;
    private static UiccController sUiccController;

    static {
        if ((TelephonyManager.MultiSimVariants.DSDA != TelephonyManager.getDefault().getMultiSimConfiguration() || mIsAdaptMultiSimConfiguration) && (!IS_QCOM_DUAL_LTE_STACK || !IS_DUAL_VOLTE_SUPPORTED || HuaweiTelephonyConfigs.isMTKPlatform())) {
            MAX_ACTIVE_PHONES = 1;
        } else {
            MAX_ACTIVE_PHONES = 2;
        }
    }

    public static void makeDefaultPhones(Context context) {
        makeDefaultPhone(context);
    }

    @UnsupportedAppUsage
    public static void makeDefaultPhone(Context context) {
        int phoneType;
        int phoneType2;
        synchronized (sLockProxyPhones) {
            if (!sMadeDefaults) {
                sContext = context;
                TelephonyDevController.create();
                int retryCount = 0;
                while (true) {
                    boolean hasException = false;
                    int i = 1;
                    int retryCount2 = retryCount + 1;
                    try {
                        new LocalServerSocket("com.android.internal.telephony");
                    } catch (IOException e) {
                        hasException = true;
                    }
                    if (!hasException) {
                        sPhoneNotifier = new DefaultPhoneNotifier();
                        int cdmaSubscription = CdmaSubscriptionSourceManager.getDefault(context);
                        Rlog.i(LOG_TAG, "Cdma Subscription set to " + cdmaSubscription);
                        int numPhones = TelephonyManager.getDefault().getPhoneCount();
                        int[] networkModes = new int[numPhones];
                        sPhones = new Phone[numPhones];
                        sCommandsInterfaces = new RIL[numPhones];
                        sTelephonyNetworkFactories = new TelephonyNetworkFactory[numPhones];
                        for (int i2 = 0; i2 < numPhones; i2++) {
                            networkModes[i2] = RILConstants.PREFERRED_NETWORK_MODE;
                            Rlog.i(LOG_TAG, "Network Mode set to " + Integer.toString(networkModes[i2]));
                            try {
                                sCommandsInterfaces[i2] = (CommandsInterface) HwTelephonyFactory.getHwPhoneManager().createHwRil(context, networkModes[i2], cdmaSubscription, Integer.valueOf(i2));
                            } catch (Exception e2) {
                                sCommandsInterfaces[i2] = new RIL(context, networkModes[i2], cdmaSubscription, Integer.valueOf(i2));
                                while (true) {
                                    Rlog.e(LOG_TAG, "Unable to construct custom RIL class", e2);
                                    try {
                                        Thread.sleep(10000);
                                    } catch (InterruptedException e3) {
                                    }
                                }
                            }
                        }
                        sUiccController = UiccController.make(context, sCommandsInterfaces);
                        Rlog.i(LOG_TAG, "Creating SubscriptionController");
                        SubscriptionController.init(context, sCommandsInterfaces);
                        MultiSimSettingController.init(context, SubscriptionController.getInstance());
                        HwTelephonyFactory.getHwUiccManager().initHwSubscriptionManager(context, sCommandsInterfaces);
                        if (context.getPackageManager().hasSystemFeature("android.hardware.telephony.euicc")) {
                            sEuiccController = EuiccController.init(context);
                            sEuiccCardController = EuiccCardController.init(context);
                        }
                        int i3 = 0;
                        while (i3 < numPhones) {
                            Phone phone = null;
                            int phoneType3 = TelephonyManager.getPhoneType(networkModes[i3]);
                            String lastPhoneType = TelephonyManager.getTelephonyProperty(i3, "persist.radio.last_phone_type", PhoneConfigurationManager.SSSS);
                            if ("CDMA".equals(lastPhoneType)) {
                                Rlog.i(LOG_TAG, "phone type set to lastPhoneType = " + lastPhoneType);
                                phoneType = 2;
                            } else if ("GSM".equals(lastPhoneType)) {
                                Rlog.i(LOG_TAG, "phone type set to lastPhoneType = " + lastPhoneType);
                                phoneType = 1;
                            } else {
                                phoneType = phoneType3;
                            }
                            if (phoneType == i) {
                                phoneType2 = phoneType;
                                phone = new GsmCdmaPhone(context, sCommandsInterfaces[i3], sPhoneNotifier, i3, 1, TelephonyComponentFactory.getInstance());
                            } else {
                                phoneType2 = phoneType;
                                if (phoneType2 == 2) {
                                    phone = new GsmCdmaPhone(context, sCommandsInterfaces[i3], sPhoneNotifier, i3, 6, TelephonyComponentFactory.getInstance());
                                }
                            }
                            Rlog.i(LOG_TAG, "Creating Phone with type = " + phoneType2 + " sub = " + i3);
                            sPhones[i3] = phone;
                            i3++;
                            i = 1;
                        }
                        if (numPhones > 0) {
                            sPhone = sPhones[0];
                            sCommandsInterface = sCommandsInterfaces[0];
                        }
                        ComponentName componentName = SmsApplication.getDefaultSmsApplication(context, true);
                        String packageName = "NONE";
                        if (componentName != null) {
                            packageName = componentName.getPackageName();
                        }
                        Rlog.i(LOG_TAG, "defaultSmsApplication: " + packageName);
                        SmsApplication.initSmsPackageMonitor(context);
                        sMadeDefaults = true;
                        Rlog.i(LOG_TAG, "Creating SubInfoRecordUpdater ");
                        sSubInfoRecordUpdater = new SubscriptionInfoUpdater(BackgroundThread.get().getLooper(), context, sPhones, sCommandsInterfaces);
                        SubscriptionController.getInstance().updatePhonesAvailability(sPhones);
                        if (context.getPackageManager().hasSystemFeature("android.hardware.telephony.ims")) {
                            boolean isDynamicBinding = sContext.getResources().getBoolean(17891428);
                            String defaultImsPackage = sContext.getResources().getString(17039861);
                            Rlog.i(LOG_TAG, "ImsResolver: defaultImsPackage: xxx");
                            sImsResolver = new ImsResolver(sContext, defaultImsPackage, numPhones, isDynamicBinding);
                            sImsResolver.initPopulateCacheAndStartBind();
                            for (int i4 = 0; i4 < numPhones; i4++) {
                                sPhones[i4].startMonitoringImsService();
                            }
                        } else {
                            Rlog.i(LOG_TAG, "IMS is not supported on this device, skipping ImsResolver.");
                        }
                        ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
                        SubscriptionController sc = SubscriptionController.getInstance();
                        sSubscriptionMonitor = new SubscriptionMonitor(tr, sContext, sc, numPhones);
                        sPhoneConfigurationManager = PhoneConfigurationManager.init(sContext);
                        sCellularNetworkValidator = CellularNetworkValidator.make(sContext);
                        sPhoneConfigurationManager.getNumberOfModemsWithSimultaneousDataConnections();
                        sPhoneSwitcher = PhoneSwitcher.make(MAX_ACTIVE_PHONES, numPhones, sContext, sc, Looper.myLooper(), tr, sCommandsInterfaces, sPhones);
                        sProxyController = ProxyController.getInstance(context, sPhones, sUiccController, sCommandsInterfaces, sPhoneSwitcher);
                        sIntentBroadcaster = IntentBroadcaster.getInstance(context);
                        sNotificationChannelController = new NotificationChannelController(context);
                        sTelephonyNetworkFactories = new TelephonyNetworkFactory[numPhones];
                        for (int i5 = 0; i5 < numPhones; i5++) {
                            sTelephonyNetworkFactories[i5] = new TelephonyNetworkFactory(sSubscriptionMonitor, Looper.myLooper(), sPhones[i5]);
                        }
                        if (HwFrameworkFactory.getHwInnerTelephonyManager().isNrSlicesSupported()) {
                            for (int i6 = 0; i6 < numPhones; i6++) {
                                DcTrackerEx dctrackerEx = new DcTrackerEx();
                                dctrackerEx.setDcTracker(sPhones[i6].getDcTracker(1));
                                HwTelephonyFactory.createHwSlicesNetworkFactory(dctrackerEx, Looper.myLooper(), context, sPhones[i6].getPhoneId());
                            }
                        }
                        VSimUtilsInner.makeVSimPhoneFactory(context, sPhoneNotifier, sPhones, sCommandsInterfaces);
                        HwTelephonyFactory.getHwPhoneManager().loadHuaweiPhoneService(PhoneExt.getPhoneExts(sPhones), sContext);
                        Rlog.i(LOG_TAG, "initHwTimeZoneUpdater");
                        HwTelephonyFactory.getHwPhoneManager().initHwTimeZoneUpdater(sContext);
                        context.sendBroadcast(new Intent(ACTION_MAKE_DEFAULT_PHONE_DONE));
                    } else if (retryCount2 <= 3) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e4) {
                        }
                        retryCount = retryCount2;
                    } else {
                        throw new RuntimeException("PhoneFactory probably already running");
                    }
                }
            }
        }
    }

    @UnsupportedAppUsage
    public static Phone getDefaultPhone() {
        Phone phone;
        synchronized (sLockProxyPhones) {
            if (sMadeDefaults) {
                phone = sPhone;
            } else {
                throw new IllegalStateException("Default phones haven't been made yet!");
            }
        }
        return phone;
    }

    @UnsupportedAppUsage
    public static Phone getPhone(int phoneId) {
        Phone phone;
        synchronized (sLockProxyPhones) {
            if (!sMadeDefaults) {
                throw new IllegalStateException("Default phones haven't been made yet!");
            } else if (VSimUtilsInner.isVSimSub(phoneId)) {
                phone = VSimUtilsInner.getVSimPhone();
            } else {
                if (phoneId != Integer.MAX_VALUE) {
                    if (phoneId != -1) {
                        phone = (phoneId < 0 || phoneId >= sPhones.length) ? null : sPhones[phoneId];
                    }
                }
                phone = sPhone;
            }
        }
        return phone;
    }

    @UnsupportedAppUsage
    public static Phone[] getPhones() {
        Phone[] phoneArr;
        synchronized (sLockProxyPhones) {
            if (sMadeDefaults) {
                phoneArr = sPhones;
            } else {
                throw new IllegalStateException("Default phones haven't been made yet!");
            }
        }
        return phoneArr;
    }

    public static SubscriptionInfoUpdater getSubscriptionInfoUpdater() {
        return sSubInfoRecordUpdater;
    }

    public static ImsResolver getImsResolver() {
        return sImsResolver;
    }

    public static TelephonyNetworkFactory getNetworkFactory(int phoneId) {
        TelephonyNetworkFactory factory;
        synchronized (sLockProxyPhones) {
            if (sMadeDefaults) {
                if (phoneId == Integer.MAX_VALUE) {
                    phoneId = sPhone.getSubId();
                }
                factory = (sTelephonyNetworkFactories == null || phoneId < 0 || phoneId >= sTelephonyNetworkFactories.length) ? null : sTelephonyNetworkFactories[phoneId];
            } else {
                throw new IllegalStateException("Default phones haven't been made yet!");
            }
        }
        return factory;
    }

    public static SipPhone makeSipPhone(String sipUri) {
        return SipPhoneFactory.makePhone(sipUri, sContext, sPhoneNotifier);
    }

    @UnsupportedAppUsage
    public static int calculatePreferredNetworkType(Context context, int phoneSubId) {
        int phoneId = SubscriptionController.getInstance().getPhoneId(phoneSubId);
        ContentResolver contentResolver = context.getContentResolver();
        int networkType = Settings.Global.getInt(contentResolver, "preferred_network_mode" + phoneId, -1);
        Rlog.d(LOG_TAG, "calculatePreferredNetworkType: phoneSubId = " + phoneSubId + " networkType = " + networkType);
        if (networkType != -1) {
            return networkType;
        }
        int networkType2 = RILConstants.PREFERRED_NETWORK_MODE;
        try {
            return TelephonyManager.getIntAtIndex(context.getContentResolver(), "preferred_network_mode", SubscriptionController.getInstance().getPhoneId(phoneSubId));
        } catch (Settings.SettingNotFoundException e) {
            Rlog.e(LOG_TAG, "Settings Exception Reading Value At Index for Settings.Global.PREFERRED_NETWORK_MODE");
            return networkType2;
        }
    }

    @UnsupportedAppUsage
    public static int getDefaultSubscription() {
        return SubscriptionController.getInstance().getDefaultSubId();
    }

    public static boolean isSMSPromptEnabled() {
        int value = 0;
        try {
            value = Settings.Global.getInt(sContext.getContentResolver(), "multi_sim_sms_prompt");
        } catch (Settings.SettingNotFoundException e) {
            Rlog.e(LOG_TAG, "Settings Exception Reading Dual Sim SMS Prompt Values");
        }
        boolean prompt = value != 0;
        Rlog.d(LOG_TAG, "SMS Prompt option:" + prompt);
        return prompt;
    }

    public static Phone makeImsPhone(PhoneNotifier phoneNotifier, Phone defaultPhone) {
        return ImsPhoneFactory.makePhone(sContext, phoneNotifier, defaultPhone);
    }

    public static SubscriptionInfoUpdater getSubInfoRecordUpdater() {
        return sSubInfoRecordUpdater;
    }

    public static void requestEmbeddedSubscriptionInfoListRefresh(int cardId, Runnable callback) {
        sSubInfoRecordUpdater.requestEmbeddedSubscriptionInfoListRefresh(cardId, callback);
    }

    public static SmsController getSmsController() {
        SmsController smsController;
        synchronized (sLockProxyPhones) {
            if (sMadeDefaults) {
                smsController = sProxyController.getSmsController();
            } else {
                throw new IllegalStateException("Default phones haven't been made yet!");
            }
        }
        return smsController;
    }

    public static void addLocalLog(String key, int size) {
        synchronized (sLocalLogs) {
            if (!sLocalLogs.containsKey(key)) {
                sLocalLogs.put(key, new LocalLog(size));
            } else {
                throw new IllegalArgumentException("key " + key + " already present");
            }
        }
    }

    public static void localLog(String key, String log) {
        synchronized (sLocalLogs) {
            if (sLocalLogs.containsKey(key)) {
                sLocalLogs.get(key).log(log);
            } else {
                throw new IllegalArgumentException("key " + key + " not found");
            }
        }
    }

    public static void dump(FileDescriptor fd, PrintWriter printwriter, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(printwriter, "  ");
        pw.println("PhoneFactory:");
        pw.println(" sMadeDefaults=" + sMadeDefaults);
        sPhoneSwitcher.dump(fd, pw, args);
        pw.println();
        Phone[] phones = getPhones();
        for (int i = 0; i < phones.length; i++) {
            pw.increaseIndent();
            try {
                phones[i].dump(fd, pw, args);
                pw.flush();
                pw.println("++++++++++++++++++++++++++++++++");
                sTelephonyNetworkFactories[i].dump(fd, pw, args);
                pw.flush();
                pw.decreaseIndent();
                pw.println("++++++++++++++++++++++++++++++++");
            } catch (Exception e) {
                pw.println("Telephony DebugService: Could not get Phone[" + i + "] e=" + e);
            }
        }
        pw.println("SubscriptionMonitor:");
        pw.increaseIndent();
        try {
            sSubscriptionMonitor.dump(fd, pw, args);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("UiccController:");
        pw.increaseIndent();
        try {
            sUiccController.dump(fd, pw, args);
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        pw.flush();
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        if (sEuiccController != null) {
            pw.println("EuiccController:");
            pw.increaseIndent();
            try {
                sEuiccController.dump(fd, pw, args);
                sEuiccCardController.dump(fd, pw, args);
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            pw.flush();
            pw.decreaseIndent();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        pw.println("SubscriptionController:");
        pw.increaseIndent();
        try {
            SubscriptionController.getInstance().dump(fd, pw, args);
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        pw.flush();
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("SubInfoRecordUpdater:");
        pw.increaseIndent();
        try {
            sSubInfoRecordUpdater.dump(fd, pw, args);
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        pw.flush();
        pw.decreaseIndent();
        VSimUtilsInner.dumpVSimPhoneFactory(fd, pw, args);
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("LocalLogs:");
        pw.increaseIndent();
        synchronized (sLocalLogs) {
            for (String key : sLocalLogs.keySet()) {
                pw.println(key);
                pw.increaseIndent();
                sLocalLogs.get(key).dump(fd, pw, args);
                pw.decreaseIndent();
            }
            pw.flush();
        }
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        if (Log.HWINFO) {
            pw.println("SharedPreferences:");
            pw.increaseIndent();
            try {
                if (sContext != null) {
                    Map spValues = PreferenceManager.getDefaultSharedPreferences(sContext).getAll();
                    for (Object key2 : spValues.keySet()) {
                        pw.println(key2 + " : " + spValues.get(key2));
                    }
                }
            } catch (Exception e7) {
                e7.printStackTrace();
            }
            pw.decreaseIndent();
            pw.println("++++++++++++++++++++++++++++++++");
            pw.println("DebugEvents:");
            pw.increaseIndent();
            try {
                AnomalyReporter.dump(fd, pw, args);
            } catch (Exception e8) {
                e8.printStackTrace();
            }
            pw.flush();
            pw.decreaseIndent();
        }
    }

    public static int getTopPrioritySubscriptionId() {
        PhoneSwitcher phoneSwitcher = sPhoneSwitcher;
        if (phoneSwitcher != null) {
            return phoneSwitcher.getTopPrioritySubscriptionId();
        }
        return SubscriptionManager.getDefaultDataSubscriptionId();
    }

    public static int onDataSubChange() {
        PhoneSwitcher phoneSwitcher = sPhoneSwitcher;
        if (phoneSwitcher != null) {
            return phoneSwitcher.onDataSubChange();
        }
        return 0;
    }

    public static void resendDataAllowed(int phoneId) {
        PhoneSwitcher phoneSwitcher = sPhoneSwitcher;
        if (phoneSwitcher != null) {
            phoneSwitcher.resendDataAllowedForEx(phoneId);
        }
    }

    public static TelephonyNetworkFactory getTelephonyNetworkFactory(int phoneId) {
        TelephonyNetworkFactory[] telephonyNetworkFactoryArr = sTelephonyNetworkFactories;
        if (telephonyNetworkFactoryArr != null) {
            return telephonyNetworkFactoryArr[phoneId];
        }
        return null;
    }

    public static boolean getInitState() {
        boolean z;
        synchronized (sLockProxyPhones) {
            z = sMadeDefaults;
        }
        return z;
    }
}
