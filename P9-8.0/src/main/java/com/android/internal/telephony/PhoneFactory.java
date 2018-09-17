package com.android.internal.telephony;

import android.content.Context;
import android.net.LocalServerSocket;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.MultiSimVariants;
import android.util.LocalLog;
import com.android.internal.telephony.dataconnection.TelephonyNetworkFactory;
import com.android.internal.telephony.ims.ImsResolver;
import com.android.internal.telephony.imsphone.ImsPhoneFactory;
import com.android.internal.telephony.sip.SipPhone;
import com.android.internal.telephony.sip.SipPhoneFactory;
import com.android.internal.telephony.uicc.IccCardProxy;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.NotificationChannelController;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class PhoneFactory {
    static final boolean DBG = false;
    private static final boolean IS_FULL_NETWORK_SUPPORTED_IN_HISI = HwTelephonyFactory.getHwUiccManager().isFullNetworkSupported();
    static final String LOG_TAG = "PhoneFactory";
    public static final int MAX_ACTIVE_PHONES;
    static final int SOCKET_OPEN_MAX_RETRY = 3;
    static final int SOCKET_OPEN_RETRY_MILLIS = 2000;
    private static final boolean mIsAdaptMultiSimConfiguration = SystemProperties.getBoolean("ro.config.multi_sim_cfg_adapt", false);
    private static CommandsInterface sCommandsInterface = null;
    private static CommandsInterface[] sCommandsInterfaces = null;
    private static Context sContext;
    private static ImsResolver sImsResolver;
    private static final HashMap<String, LocalLog> sLocalLogs = new HashMap();
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
        if (MultiSimVariants.DSDA != TelephonyManager.getDefault().getMultiSimConfiguration() || (mIsAdaptMultiSimConfiguration ^ 1) == 0) {
            MAX_ACTIVE_PHONES = 1;
        } else {
            MAX_ACTIVE_PHONES = 2;
        }
    }

    public static void makeDefaultPhones(Context context) {
        makeDefaultPhone(context);
    }

    /* JADX WARNING: Missing block: B:12:?, code:
            sPhoneNotifier = new com.android.internal.telephony.DefaultPhoneNotifier();
            r22 = com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager.getDefault(r36);
            android.telephony.Rlog.i(LOG_TAG, "Cdma Subscription set to " + r22);
            r9 = android.telephony.TelephonyManager.getDefault().getPhoneCount();
            r24 = sContext.getResources().getString(17039796);
            android.telephony.Rlog.i(LOG_TAG, "ImsResolver: defaultImsPackage: " + r24);
            sImsResolver = new com.android.internal.telephony.ims.ImsResolver(sContext, r24, r9);
            sImsResolver.populateCacheAndStartBind();
            r31 = new int[r9];
            sPhones = new com.android.internal.telephony.Phone[r9];
            sCommandsInterfaces = new com.android.internal.telephony.RIL[r9];
            sTelephonyNetworkFactories = new com.android.internal.telephony.dataconnection.TelephonyNetworkFactory[r9];
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:13:0x0095, code:
            if (r6 >= r9) goto L_0x011d;
     */
    /* JADX WARNING: Missing block: B:14:0x0097, code:
            r31[r6] = com.android.internal.telephony.RILConstants.PREFERRED_NETWORK_MODE;
            android.telephony.Rlog.i(LOG_TAG, "Network Mode set to " + java.lang.Integer.toString(r31[r6]));
     */
    /* JADX WARNING: Missing block: B:16:?, code:
            sCommandsInterfaces[r6] = (com.android.internal.telephony.CommandsInterface) com.android.internal.telephony.HwTelephonyFactory.getHwPhoneManager().createHwRil(r36, r31[r6], r22, java.lang.Integer.valueOf(r6));
     */
    /* JADX WARNING: Missing block: B:17:0x00d3, code:
            r6 = r6 + 1;
     */
    /* JADX WARNING: Missing block: B:34:0x00f6, code:
            r25 = move-exception;
     */
    /* JADX WARNING: Missing block: B:36:?, code:
            sCommandsInterfaces[r6] = new com.android.internal.telephony.RIL(r36, r31[r6], r22, java.lang.Integer.valueOf(r6));
     */
    /* JADX WARNING: Missing block: B:37:0x010a, code:
            android.telephony.Rlog.e(LOG_TAG, "Unable to construct custom RIL class", r25);
     */
    /* JADX WARNING: Missing block: B:40:?, code:
            java.lang.Thread.sleep(10000);
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            android.telephony.Rlog.i(LOG_TAG, "Creating SubscriptionController");
            com.android.internal.telephony.SubscriptionController.init(r36, sCommandsInterfaces);
            sUiccController = com.android.internal.telephony.uicc.UiccController.make(r36, sCommandsInterfaces);
            com.android.internal.telephony.HwTelephonyFactory.getHwUiccManager().initHwModemStackController(r36, sUiccController, sCommandsInterfaces);
            com.android.internal.telephony.HwTelephonyFactory.getHwUiccManager().initHwModemBindingPolicyHandler(r36, sUiccController, sCommandsInterfaces);
            com.android.internal.telephony.HwTelephonyFactory.getHwUiccManager().initHwSubscriptionManager(r36, sCommandsInterfaces);
     */
    /* JADX WARNING: Missing block: B:46:0x015e, code:
            if (IS_FULL_NETWORK_SUPPORTED_IN_HISI == false) goto L_0x016b;
     */
    /* JADX WARNING: Missing block: B:47:0x0160, code:
            com.android.internal.telephony.HwTelephonyFactory.getHwUiccManager().initHwFullNetwork(r36, sCommandsInterfaces);
     */
    /* JADX WARNING: Missing block: B:48:0x016b, code:
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:49:0x016c, code:
            if (r6 >= r9) goto L_0x022f;
     */
    /* JADX WARNING: Missing block: B:50:0x016e, code:
            r2 = null;
            r33 = android.telephony.TelephonyManager.getPhoneType(r31[r6]);
            r30 = android.telephony.TelephonyManager.getTelephonyProperty(r6, "persist.radio.last_phone_type", "");
     */
    /* JADX WARNING: Missing block: B:51:0x0188, code:
            if ("CDMA".equals(r30) == false) goto L_0x01ed;
     */
    /* JADX WARNING: Missing block: B:52:0x018a, code:
            r33 = 2;
            android.telephony.Rlog.i(LOG_TAG, "phone type set to lastPhoneType = " + r30);
     */
    /* JADX WARNING: Missing block: B:54:0x01ab, code:
            if (r33 != 1) goto L_0x0217;
     */
    /* JADX WARNING: Missing block: B:55:0x01ad, code:
            r2 = new com.android.internal.telephony.GsmCdmaPhone(r36, sCommandsInterfaces[r6], sPhoneNotifier, r6, 1, com.android.internal.telephony.TelephonyComponentFactory.getInstance());
     */
    /* JADX WARNING: Missing block: B:56:0x01bf, code:
            android.telephony.Rlog.i(LOG_TAG, "Creating Phone with type = " + r33 + " sub = " + r6);
            sPhones[r6] = r2;
            r6 = r6 + 1;
     */
    /* JADX WARNING: Missing block: B:58:0x01f6, code:
            if ("GSM".equals(r30) == false) goto L_0x01a8;
     */
    /* JADX WARNING: Missing block: B:59:0x01f8, code:
            r33 = 1;
            android.telephony.Rlog.i(LOG_TAG, "phone type set to lastPhoneType = " + r30);
     */
    /* JADX WARNING: Missing block: B:61:0x021a, code:
            if (r33 != 2) goto L_0x01bf;
     */
    /* JADX WARNING: Missing block: B:62:0x021c, code:
            r2 = new com.android.internal.telephony.GsmCdmaPhone(r36, sCommandsInterfaces[r6], sPhoneNotifier, r6, 6, com.android.internal.telephony.TelephonyComponentFactory.getInstance());
     */
    /* JADX WARNING: Missing block: B:63:0x022f, code:
            sPhone = sPhones[0];
            sCommandsInterface = sCommandsInterfaces[0];
            r23 = com.android.internal.telephony.SmsApplication.getDefaultSmsApplication(r36, true);
            r32 = "NONE";
     */
    /* JADX WARNING: Missing block: B:64:0x0247, code:
            if (r23 == null) goto L_0x024d;
     */
    /* JADX WARNING: Missing block: B:65:0x0249, code:
            r32 = r23.getPackageName();
     */
    /* JADX WARNING: Missing block: B:66:0x024d, code:
            android.telephony.Rlog.i(LOG_TAG, "defaultSmsApplication: " + r32);
            com.android.internal.telephony.SmsApplication.initSmsPackageMonitor(r36);
            sMadeDefaults = true;
            android.telephony.Rlog.i(LOG_TAG, "Creating SubInfoRecordUpdater ");
            sSubInfoRecordUpdater = new com.android.internal.telephony.SubscriptionInfoUpdater(r36, sPhones, sCommandsInterfaces);
            com.android.internal.telephony.SubscriptionController.getInstance().updatePhonesAvailability(sPhones);
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:67:0x028f, code:
            if (r6 >= r9) goto L_0x029b;
     */
    /* JADX WARNING: Missing block: B:68:0x0291, code:
            sPhones[r6].startMonitoringImsService();
            r6 = r6 + 1;
     */
    /* JADX WARNING: Missing block: B:69:0x029b, code:
            r13 = com.android.internal.telephony.ITelephonyRegistry.Stub.asInterface(android.os.ServiceManager.getService("telephony.registry"));
            r11 = com.android.internal.telephony.SubscriptionController.getInstance();
            sSubscriptionMonitor = new com.android.internal.telephony.SubscriptionMonitor(r13, sContext, r11, r9);
            sPhoneSwitcher = new com.android.internal.telephony.PhoneSwitcher(MAX_ACTIVE_PHONES, r9, sContext, r11, android.os.Looper.myLooper(), r13, sCommandsInterfaces, sPhones);
            sProxyController = com.android.internal.telephony.ProxyController.getInstance(r36, sPhones, sUiccController, sCommandsInterfaces, sPhoneSwitcher);
            sNotificationChannelController = new com.android.internal.telephony.util.NotificationChannelController(r36);
            sTelephonyNetworkFactories = new com.android.internal.telephony.dataconnection.TelephonyNetworkFactory[r9];
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:70:0x02e4, code:
            if (r6 >= r9) goto L_0x0308;
     */
    /* JADX WARNING: Missing block: B:71:0x02e6, code:
            sTelephonyNetworkFactories[r6] = new com.android.internal.telephony.dataconnection.TelephonyNetworkFactory(sPhoneSwitcher, r11, sSubscriptionMonitor, android.os.Looper.myLooper(), sContext, r6, sPhones[r6].mDcTracker);
            r6 = r6 + 1;
     */
    /* JADX WARNING: Missing block: B:72:0x0308, code:
            com.android.internal.telephony.vsim.VSimUtilsInner.makeVSimPhoneFactory(r36, sPhoneNotifier, sPhones, sCommandsInterfaces);
            com.android.internal.telephony.HwTelephonyFactory.getHwPhoneManager().loadHuaweiPhoneService(sPhones, sContext);
            android.telephony.Rlog.i(LOG_TAG, "initHwTimeZoneUpdater");
            com.android.internal.telephony.HwTelephonyFactory.getHwPhoneManager().initHwTimeZoneUpdater(sContext);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void makeDefaultPhone(Context context) {
        synchronized (sLockProxyPhones) {
            if (!sMadeDefaults) {
                sContext = context;
                TelephonyDevController.create();
                int retryCount = 0;
                while (true) {
                    boolean hasException = false;
                    retryCount++;
                    try {
                        LocalServerSocket localServerSocket = new LocalServerSocket("com.android.internal.telephony");
                    } catch (IOException e) {
                        hasException = true;
                    }
                    if (!hasException) {
                        break;
                    } else if (retryCount > 3) {
                        throw new RuntimeException("PhoneFactory probably already running");
                    } else {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e2) {
                        }
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
        String dbgInfo = "";
        synchronized (sLockProxyPhones) {
            if (sMadeDefaults) {
                if (VSimUtilsInner.isVSimSub(phoneId)) {
                    dbgInfo = "phoneId == SUB_VSIM return sVSimPhone";
                    phone = VSimUtilsInner.getVSimPhone();
                } else {
                    phone = (phoneId == Integer.MAX_VALUE || phoneId == -1) ? sPhone : (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) ? null : sPhones[phoneId];
                }
            } else {
                throw new IllegalStateException("Default phones haven't been made yet!");
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

    public static ImsResolver getImsResolver() {
        return sImsResolver;
    }

    public static SipPhone makeSipPhone(String sipUri) {
        return SipPhoneFactory.makePhone(sipUri, sContext, sPhoneNotifier);
    }

    public static int calculatePreferredNetworkType(Context context, int phoneSubId) {
        int networkType = Global.getInt(context.getContentResolver(), "preferred_network_mode" + phoneSubId, RILConstants.PREFERRED_NETWORK_MODE);
        Rlog.d(LOG_TAG, "calculatePreferredNetworkType: phoneSubId = " + phoneSubId + " networkType = " + networkType);
        return networkType;
    }

    public static int getDefaultSubscription() {
        return SubscriptionController.getInstance().getDefaultSubId();
    }

    public static boolean isSMSPromptEnabled() {
        int value = 0;
        try {
            value = Global.getInt(sContext.getContentResolver(), "multi_sim_sms_prompt");
        } catch (SettingNotFoundException e) {
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

    public static void addLocalLog(String key, int size) {
        synchronized (sLocalLogs) {
            if (sLocalLogs.containsKey(key)) {
                throw new IllegalArgumentException("key " + key + " already present");
            }
            sLocalLogs.put(key, new LocalLog(size));
        }
    }

    public static void localLog(String key, String log) {
        synchronized (sLocalLogs) {
            if (sLocalLogs.containsKey(key)) {
                ((LocalLog) sLocalLogs.get(key)).log(log);
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
                    ((IccCardProxy) phone.getIccCard()).dump(fd, pw, args);
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
        } catch (Exception e22) {
            e22.printStackTrace();
        }
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("UiccController:");
        pw.increaseIndent();
        try {
            sUiccController.dump(fd, pw, args);
        } catch (Exception e222) {
            e222.printStackTrace();
        }
        pw.flush();
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("SubscriptionController:");
        pw.increaseIndent();
        try {
            SubscriptionController.getInstance().dump(fd, pw, args);
        } catch (Exception e2222) {
            e2222.printStackTrace();
        }
        pw.flush();
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("SubInfoRecordUpdater:");
        pw.increaseIndent();
        try {
            sSubInfoRecordUpdater.dump(fd, pw, args);
        } catch (Exception e22222) {
            e22222.printStackTrace();
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
                ((LocalLog) sLocalLogs.get(key)).dump(fd, pw, args);
                pw.decreaseIndent();
            }
            pw.flush();
        }
        pw.decreaseIndent();
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
