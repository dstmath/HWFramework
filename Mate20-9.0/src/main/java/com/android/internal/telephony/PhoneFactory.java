package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.net.LocalServerSocket;
import android.os.Handler;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.telephony.dataconnection.TelephonyNetworkFactory;
import com.android.internal.telephony.euicc.EuiccCardController;
import com.android.internal.telephony.euicc.EuiccController;
import com.android.internal.telephony.ims.ImsResolver;
import com.android.internal.telephony.imsphone.ImsPhoneFactory;
import com.android.internal.telephony.sip.SipPhone;
import com.android.internal.telephony.sip.SipPhoneFactory;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccProfile;
import com.android.internal.telephony.util.NotificationChannelController;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class PhoneFactory {
    static final boolean DBG = false;
    public static final boolean IS_DUAL_VOLTE_SUPPORTED = HwModemCapability.isCapabilitySupport(21);
    private static final boolean IS_FULL_NETWORK_SUPPORTED_IN_HISI = HwTelephonyFactory.getHwUiccManager().isFullNetworkSupported();
    public static final boolean IS_QCOM_DUAL_LTE_STACK = HwModemCapability.isCapabilitySupport(27);
    static final String LOG_TAG = "PhoneFactory";
    public static final int MAX_ACTIVE_PHONES;
    static final int SOCKET_OPEN_MAX_RETRY = 3;
    static final int SOCKET_OPEN_RETRY_MILLIS = 2000;
    private static final boolean mIsAdaptMultiSimConfiguration = SystemProperties.getBoolean("ro.config.multi_sim_cfg_adapt", false);
    private static CommandsInterface sCommandsInterface = null;
    private static CommandsInterface[] sCommandsInterfaces = null;
    private static Context sContext;
    private static EuiccCardController sEuiccCardController;
    private static EuiccController sEuiccController;
    private static ImsResolver sImsResolver;
    private static IntentBroadcaster sIntentBroadcaster;
    private static final HashMap<String, LocalLog> sLocalLogs = new HashMap<>();
    static final Object sLockProxyPhones = new Object();
    private static boolean sMadeDefaults = false;
    private static NotificationChannelController sNotificationChannelController;
    private static Phone sPhone = null;
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

    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        sPhoneNotifier = new com.android.internal.telephony.DefaultPhoneNotifier();
        r13 = com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager.getDefault(r32);
        android.telephony.Rlog.i(LOG_TAG, "Cdma Subscription set to " + r13);
        r0 = android.telephony.TelephonyManager.getDefault().getPhoneCount();
        r15 = sContext.getResources().getBoolean(17956945);
        r14 = sContext.getResources().getString(17039822);
        android.telephony.Rlog.i(LOG_TAG, "ImsResolver: defaultImsPackage: " + r14);
        sImsResolver = new com.android.internal.telephony.ims.ImsResolver(sContext, r14, r0, r15);
        sImsResolver.initPopulateCacheAndStartBind();
        r23 = new int[r0];
        sPhones = new com.android.internal.telephony.Phone[r0];
        sCommandsInterfaces = new com.android.internal.telephony.RIL[r0];
        sTelephonyNetworkFactories = new com.android.internal.telephony.dataconnection.TelephonyNetworkFactory[r0];
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x009b, code lost:
        if (r1 >= r0) goto L_0x00f5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x009d, code lost:
        r23[r1] = com.android.internal.telephony.RILConstants.PREFERRED_NETWORK_MODE;
        android.telephony.Rlog.i(LOG_TAG, "Network Mode set to " + java.lang.Integer.toString(r23[r1]));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        sCommandsInterfaces[r1] = (com.android.internal.telephony.CommandsInterface) com.android.internal.telephony.HwTelephonyFactory.getHwPhoneManager().createHwRil(r8, r23[r1], r13, java.lang.Integer.valueOf(r1));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00d1, code lost:
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00d5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00d6, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        sCommandsInterfaces[r1] = new com.android.internal.telephony.RIL(r8, r23[r1], r13, java.lang.Integer.valueOf(r1));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00e6, code lost:
        android.telephony.Rlog.e(LOG_TAG, "Unable to construct custom RIL class", r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        java.lang.Thread.sleep(10000);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        android.telephony.Rlog.i(LOG_TAG, "Creating SubscriptionController");
        com.android.internal.telephony.SubscriptionController.init(r8, sCommandsInterfaces);
        sUiccController = com.android.internal.telephony.uicc.UiccController.make(r8, sCommandsInterfaces);
        com.android.internal.telephony.HwTelephonyFactory.getHwUiccManager().initHwSubscriptionManager(r8, sCommandsInterfaces);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x011c, code lost:
        if (r32.getPackageManager().hasSystemFeature("android.hardware.telephony.euicc") == false) goto L_0x012a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x011e, code lost:
        sEuiccController = com.android.internal.telephony.euicc.EuiccController.init(r32);
        sEuiccCardController = com.android.internal.telephony.euicc.EuiccCardController.init(r32);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x012a, code lost:
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x012b, code lost:
        r7 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x012c, code lost:
        if (r7 >= r0) goto L_0x01e5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x012e, code lost:
        r16 = null;
        r1 = android.telephony.TelephonyManager.getPhoneType(r23[r7]);
        r6 = android.telephony.TelephonyManager.getTelephonyProperty(r7, "persist.radio.last_phone_type", "");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0145, code lost:
        if (com.android.internal.telephony.AbstractGsmCdmaPhone.CDMA_PHONE.equals(r6) == false) goto L_0x015f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0147, code lost:
        r1 = 2;
        android.telephony.Rlog.i(LOG_TAG, "phone type set to lastPhoneType = " + r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0165, code lost:
        if (com.android.internal.telephony.AbstractGsmCdmaPhone.GSM_PHONE.equals(r6) == false) goto L_0x017e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0167, code lost:
        r1 = 1;
        android.telephony.Rlog.i(LOG_TAG, "phone type set to lastPhoneType = " + r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x017e, code lost:
        r5 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x017f, code lost:
        if (r5 != r11) goto L_0x01a1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0181, code lost:
        r11 = r5;
        r20 = r6;
        r10 = r7;
        r1 = new com.android.internal.telephony.GsmCdmaPhone(r8, sCommandsInterfaces[r7], sPhoneNotifier, r7, 1, com.android.internal.telephony.TelephonyComponentFactory.getInstance());
        r16 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x01a1, code lost:
        r11 = r5;
        r20 = r6;
        r10 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x01a6, code lost:
        if (r11 != 2) goto L_0x01be;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x01a8, code lost:
        r1 = new com.android.internal.telephony.GsmCdmaPhone(r8, sCommandsInterfaces[r10], sPhoneNotifier, r10, 6, com.android.internal.telephony.TelephonyComponentFactory.getInstance());
        r16 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x01be, code lost:
        android.telephony.Rlog.i(LOG_TAG, "Creating Phone with type = " + r11 + " sub = " + r10);
        sPhones[r10] = r16;
        r1 = r10 + 1;
        r11 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x01e5, code lost:
        r2 = 0;
        sPhone = sPhones[0];
        sCommandsInterface = sCommandsInterfaces[0];
        r1 = com.android.internal.telephony.SmsApplication.getDefaultSmsApplication(r8, true);
        r3 = "NONE";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x01fb, code lost:
        if (r1 == null) goto L_0x0202;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x01fd, code lost:
        r3 = r1.getPackageName();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0202, code lost:
        android.telephony.Rlog.i(LOG_TAG, "defaultSmsApplication: " + r3);
        com.android.internal.telephony.SmsApplication.initSmsPackageMonitor(r32);
        sMadeDefaults = true;
        android.telephony.Rlog.i(LOG_TAG, "Creating SubInfoRecordUpdater ");
        sSubInfoRecordUpdater = new com.android.internal.telephony.SubscriptionInfoUpdater(com.android.internal.os.BackgroundThread.get().getLooper(), r8, sPhones, sCommandsInterfaces);
        com.android.internal.telephony.SubscriptionController.getInstance().updatePhonesAvailability(sPhones);
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0242, code lost:
        if (r4 >= r0) goto L_0x024e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0244, code lost:
        sPhones[r4].startMonitoringImsService();
        r4 = r4 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x024e, code lost:
        r4 = com.android.internal.telephony.ITelephonyRegistry.Stub.asInterface(android.os.ServiceManager.getService("telephony.registry"));
        r5 = com.android.internal.telephony.SubscriptionController.getInstance();
        sSubscriptionMonitor = new com.android.internal.telephony.SubscriptionMonitor(r4, sContext, r5, r0);
        r10 = r14;
        r11 = r15;
        r14 = new com.android.internal.telephony.PhoneSwitcher(MAX_ACTIVE_PHONES, r0, sContext, r5, android.os.Looper.myLooper(), r4, sCommandsInterfaces, sPhones);
        sPhoneSwitcher = r14;
        sProxyController = com.android.internal.telephony.ProxyController.getInstance(r8, sPhones, sUiccController, sCommandsInterfaces, sPhoneSwitcher);
        sIntentBroadcaster = com.android.internal.telephony.IntentBroadcaster.getInstance(r32);
        sNotificationChannelController = new com.android.internal.telephony.util.NotificationChannelController(r8);
        sTelephonyNetworkFactories = new com.android.internal.telephony.dataconnection.TelephonyNetworkFactory[r0];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x02a2, code lost:
        if (r2 >= r0) goto L_0x02c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x02a4, code lost:
        r6 = sTelephonyNetworkFactories;
        r24 = new com.android.internal.telephony.dataconnection.TelephonyNetworkFactory(sPhoneSwitcher, r5, sSubscriptionMonitor, android.os.Looper.myLooper(), sContext, r2, sPhones[r2].mDcTracker);
        r6[r2] = r24;
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x02c8, code lost:
        com.android.internal.telephony.vsim.VSimUtilsInner.makeVSimPhoneFactory(r8, sPhoneNotifier, sPhones, sCommandsInterfaces);
        com.android.internal.telephony.HwTelephonyFactory.getHwPhoneManager().loadHuaweiPhoneService(sPhones, sContext);
        android.telephony.Rlog.i(LOG_TAG, "initHwTimeZoneUpdater");
        com.android.internal.telephony.HwTelephonyFactory.getHwPhoneManager().initHwTimeZoneUpdater(sContext);
     */
    public static void makeDefaultPhone(Context context) {
        Context context2 = context;
        synchronized (sLockProxyPhones) {
            if (!sMadeDefaults) {
                sContext = context2;
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
                        break;
                    } else if (retryCount2 <= 3) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e2) {
                        }
                        retryCount = retryCount2;
                    } else {
                        throw new RuntimeException("PhoneFactory probably already running");
                    }
                }
            }
        }
    }

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

    public static Phone getPhone(int phoneId) {
        Phone phone;
        synchronized (sLockProxyPhones) {
            if (!sMadeDefaults) {
                throw new IllegalStateException("Default phones haven't been made yet!");
            } else if (VSimUtilsInner.isVSimSub(phoneId)) {
                String dbgInfo = "phoneId == SUB_VSIM return sVSimPhone";
                phone = VSimUtilsInner.getVSimPhone();
            } else {
                if (phoneId != Integer.MAX_VALUE) {
                    if (phoneId != -1) {
                        phone = (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) ? null : sPhones[phoneId];
                    }
                }
                phone = sPhone;
            }
        }
        return phone;
    }

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

    public static SipPhone makeSipPhone(String sipUri) {
        return SipPhoneFactory.makePhone(sipUri, sContext, sPhoneNotifier);
    }

    public static int calculatePreferredNetworkType(Context context, int phoneSubId) {
        ContentResolver contentResolver = context.getContentResolver();
        int networkType = Settings.Global.getInt(contentResolver, "preferred_network_mode" + phoneSubId, -1);
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

    public static int getDefaultSubscription() {
        return SubscriptionController.getInstance().getDefaultSubId();
    }

    public static boolean isSMSPromptEnabled() {
        boolean prompt;
        boolean prompt2 = false;
        int value = 0;
        try {
            value = Settings.Global.getInt(sContext.getContentResolver(), "multi_sim_sms_prompt");
        } catch (Settings.SettingNotFoundException e) {
            Rlog.e(LOG_TAG, "Settings Exception Reading Dual Sim SMS Prompt Values");
        }
        if (value != 0) {
            prompt2 = true;
        }
        Rlog.d(LOG_TAG, "SMS Prompt option:" + prompt);
        return prompt;
    }

    public static Phone makeImsPhone(PhoneNotifier phoneNotifier, Phone defaultPhone) {
        return ImsPhoneFactory.makePhone(sContext, phoneNotifier, defaultPhone);
    }

    public static SubscriptionInfoUpdater getSubInfoRecordUpdater() {
        return sSubInfoRecordUpdater;
    }

    public static void requestEmbeddedSubscriptionInfoListRefresh(Runnable callback) {
        sSubInfoRecordUpdater.requestEmbeddedSubscriptionInfoListRefresh(callback);
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
            Phone phone = phones[i];
            try {
                phone.dump(fd, pw, args);
                pw.flush();
                pw.println("++++++++++++++++++++++++++++++++");
                sTelephonyNetworkFactories[i].dump(fd, pw, args);
                pw.flush();
                pw.println("++++++++++++++++++++++++++++++++");
                try {
                    UiccProfile uiccProfile = (UiccProfile) phone.getIccCard();
                    if (uiccProfile != null) {
                        uiccProfile.dump(fd, pw, args);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pw.flush();
                pw.decreaseIndent();
                pw.println("++++++++++++++++++++++++++++++++");
            } catch (Exception e2) {
                pw.println("Telephony DebugService: Could not get Phone[" + i + "] e=" + e2);
            }
        }
        pw.println("SubscriptionMonitor:");
        pw.increaseIndent();
        try {
            sSubscriptionMonitor.dump(fd, pw, args);
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("UiccController:");
        pw.increaseIndent();
        try {
            sUiccController.dump(fd, pw, args);
        } catch (Exception e4) {
            e4.printStackTrace();
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
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            pw.flush();
            pw.decreaseIndent();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        pw.println("SubscriptionController:");
        pw.increaseIndent();
        try {
            SubscriptionController.getInstance().dump(fd, pw, args);
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        pw.flush();
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("SubInfoRecordUpdater:");
        pw.increaseIndent();
        try {
            sSubInfoRecordUpdater.dump(fd, pw, args);
        } catch (Exception e7) {
            e7.printStackTrace();
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
            } catch (Exception e8) {
                e8.printStackTrace();
            }
            pw.flush();
            pw.decreaseIndent();
        }
    }

    public static int getTopPrioritySubscriptionId() {
        if (sPhoneSwitcher != null) {
            return sPhoneSwitcher.getTopPrioritySubscriptionId();
        }
        return SubscriptionManager.getDefaultDataSubscriptionId();
    }

    public static int onDataSubChange(int what, Handler handler) {
        if (sPhoneSwitcher != null) {
            return sPhoneSwitcher.onDataSubChange(what, handler);
        }
        return 0;
    }

    public static void resendDataAllowed(int phoneId) {
        if (sPhoneSwitcher != null) {
            sPhoneSwitcher.resendDataAllowed(phoneId);
        }
    }

    public static TelephonyNetworkFactory getTelephonyNetworkFactory(int phoneId) {
        if (sTelephonyNetworkFactories != null) {
            return sTelephonyNetworkFactories[phoneId];
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
