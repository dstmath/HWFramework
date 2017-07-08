package com.android.internal.telephony;

import android.content.ComponentName;
import android.content.Context;
import android.net.LocalServerSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.ServiceManager;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import com.android.internal.telephony.ITelephonyRegistry.Stub;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.dataconnection.TelephonyNetworkFactory;
import com.android.internal.telephony.imsphone.ImsPhoneFactory;
import com.android.internal.telephony.sip.SipPhone;
import com.android.internal.telephony.sip.SipPhoneFactory;
import com.android.internal.telephony.uicc.IccCardProxy;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class PhoneFactory {
    static final boolean DBG = false;
    private static final boolean IS_FULL_NETWORK_SUPPORTED_IN_HISI = false;
    static final String LOG_TAG = "PhoneFactory";
    public static final int MAX_ACTIVE_PHONES = 0;
    static final int SOCKET_OPEN_MAX_RETRY = 3;
    static final int SOCKET_OPEN_RETRY_MILLIS = 2000;
    private static final boolean mIsAdaptMultiSimConfiguration = false;
    private static CommandsInterface sCommandsInterface;
    private static CommandsInterface[] sCommandsInterfaces;
    private static Context sContext;
    private static final HashMap<String, LocalLog> sLocalLogs = null;
    static final Object sLockProxyPhones = null;
    private static boolean sMadeDefaults;
    private static Phone sPhone;
    private static PhoneNotifier sPhoneNotifier;
    private static PhoneSwitcher sPhoneSwitcher;
    private static Phone[] sPhones;
    private static ProxyController sProxyController;
    private static SubscriptionInfoUpdater sSubInfoRecordUpdater;
    private static SubscriptionMonitor sSubscriptionMonitor;
    private static TelephonyNetworkFactory[] sTelephonyNetworkFactories;
    private static UiccController sUiccController;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.PhoneFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.PhoneFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.PhoneFactory.<clinit>():void");
    }

    public static void makeDefaultPhones(Context context) {
        makeDefaultPhone(context);
    }

    public static void makeDefaultPhone(Context context) {
        synchronized (sLockProxyPhones) {
            if (!sMadeDefaults) {
                sContext = context;
                TelephonyDevController.create();
                int retryCount = MAX_ACTIVE_PHONES;
                while (true) {
                    boolean hasException = IS_FULL_NETWORK_SUPPORTED_IN_HISI;
                    retryCount++;
                    try {
                        LocalServerSocket localServerSocket = new LocalServerSocket("com.android.internal.telephony");
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
                        int i = MAX_ACTIVE_PHONES;
                        while (i < numPhones) {
                            networkModes[i] = RILConstants.PREFERRED_NETWORK_MODE;
                            Rlog.i(LOG_TAG, "Network Mode set to " + Integer.toString(networkModes[i]));
                            try {
                                sCommandsInterfaces[i] = (CommandsInterface) HwTelephonyFactory.getHwPhoneManager().createHwRil(context, networkModes[i], cdmaSubscription, Integer.valueOf(i));
                                i++;
                            } catch (Throwable e2) {
                                sCommandsInterfaces[i] = new RIL(context, networkModes[i], cdmaSubscription, Integer.valueOf(i));
                                while (true) {
                                    Rlog.e(LOG_TAG, "Unable to construct custom RIL class", e2);
                                    try {
                                        Thread.sleep(10000);
                                    } catch (InterruptedException e3) {
                                    }
                                }
                            }
                        }
                        Rlog.i(LOG_TAG, "Creating SubscriptionController");
                        SubscriptionController.init(context, sCommandsInterfaces);
                        sUiccController = UiccController.make(context, sCommandsInterfaces);
                        HwTelephonyFactory.getHwUiccManager().initHwModemStackController(context, sUiccController, sCommandsInterfaces);
                        HwTelephonyFactory.getHwUiccManager().initHwModemBindingPolicyHandler(context, sUiccController, sCommandsInterfaces);
                        HwTelephonyFactory.getHwUiccManager().initHwSubscriptionManager(context, sCommandsInterfaces);
                        if (IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
                            HwTelephonyFactory.getHwUiccManager().initHwFullNetwork(context, sCommandsInterfaces);
                        }
                        for (i = MAX_ACTIVE_PHONES; i < numPhones; i++) {
                            GsmCdmaPhone gsmCdmaPhone = null;
                            int phoneType = TelephonyManager.getPhoneType(networkModes[i]);
                            String lastPhoneType = TelephonyManager.getTelephonyProperty(i, "persist.radio.last_phone_type", "");
                            if ("CDMA".equals(lastPhoneType)) {
                                phoneType = 2;
                                Rlog.i(LOG_TAG, "phone type set to lastPhoneType = " + lastPhoneType);
                            } else if ("GSM".equals(lastPhoneType)) {
                                phoneType = 1;
                                Rlog.i(LOG_TAG, "phone type set to lastPhoneType = " + lastPhoneType);
                            }
                            if (phoneType == 1) {
                                gsmCdmaPhone = new GsmCdmaPhone(context, sCommandsInterfaces[i], sPhoneNotifier, i, 1, TelephonyComponentFactory.getInstance());
                            } else if (phoneType == 2) {
                                gsmCdmaPhone = new GsmCdmaPhone(context, sCommandsInterfaces[i], sPhoneNotifier, i, 6, TelephonyComponentFactory.getInstance());
                            }
                            Rlog.i(LOG_TAG, "Creating Phone with type = " + phoneType + " sub = " + i);
                            sPhones[i] = gsmCdmaPhone;
                        }
                        sPhone = sPhones[MAX_ACTIVE_PHONES];
                        sCommandsInterface = sCommandsInterfaces[MAX_ACTIVE_PHONES];
                        ComponentName componentName = SmsApplication.getDefaultSmsApplication(context, true);
                        String packageName = "NONE";
                        if (componentName != null) {
                            packageName = componentName.getPackageName();
                        }
                        Rlog.i(LOG_TAG, "defaultSmsApplication: " + packageName);
                        SmsApplication.initSmsPackageMonitor(context);
                        sMadeDefaults = true;
                        Rlog.i(LOG_TAG, "Creating SubInfoRecordUpdater ");
                        sSubInfoRecordUpdater = new SubscriptionInfoUpdater(context, sPhones, sCommandsInterfaces);
                        SubscriptionController.getInstance().updatePhonesAvailability(sPhones);
                        for (i = MAX_ACTIVE_PHONES; i < numPhones; i++) {
                            sPhones[i].startMonitoringImsService();
                        }
                        ITelephonyRegistry tr = Stub.asInterface(ServiceManager.getService("telephony.registry"));
                        SubscriptionController sc = SubscriptionController.getInstance();
                        sSubscriptionMonitor = new SubscriptionMonitor(tr, sContext, sc, numPhones);
                        sPhoneSwitcher = new PhoneSwitcher(MAX_ACTIVE_PHONES, numPhones, sContext, sc, Looper.myLooper(), tr, sCommandsInterfaces, sPhones);
                        sProxyController = ProxyController.getInstance(context, sPhones, sUiccController, sCommandsInterfaces, sPhoneSwitcher);
                        sTelephonyNetworkFactories = new TelephonyNetworkFactory[numPhones];
                        for (i = MAX_ACTIVE_PHONES; i < numPhones; i++) {
                            SubscriptionController subscriptionController = sc;
                            int i2 = i;
                            sTelephonyNetworkFactories[i] = new TelephonyNetworkFactory(sPhoneSwitcher, subscriptionController, sSubscriptionMonitor, Looper.myLooper(), sContext, i2, sPhones[i].mDcTracker);
                        }
                        VSimUtilsInner.makeVSimPhoneFactory(context, sPhoneNotifier, sPhones, sCommandsInterfaces);
                        HwTelephonyFactory.getHwPhoneManager().loadHuaweiPhoneService(sPhones, sContext);
                        HwTelephonyFactory.getHwDataConnectionManager().createIntelligentDataSwitch(sContext);
                        ImsPhoneFactory.startImsService(sContext);
                        Rlog.i(LOG_TAG, "initHwTimeZoneUpdater");
                        HwTelephonyFactory.getHwPhoneManager().initHwTimeZoneUpdater(sContext);
                    } else if (retryCount > SOCKET_OPEN_MAX_RETRY) {
                        throw new RuntimeException("PhoneFactory probably already running");
                    } else {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e4) {
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
        Phone vSimPhone;
        String dbgInfo = "";
        synchronized (sLockProxyPhones) {
            if (sMadeDefaults) {
                if (VSimUtilsInner.isVSimSub(phoneId)) {
                    dbgInfo = "phoneId == SUB_VSIM return sVSimPhone";
                    vSimPhone = VSimUtilsInner.getVSimPhone();
                } else {
                    vSimPhone = (phoneId == Integer.MAX_VALUE || phoneId == -1) ? sPhone : (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) ? null : sPhones[phoneId];
                }
            } else {
                throw new IllegalStateException("Default phones haven't been made yet!");
            }
        }
        return vSimPhone;
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
        int value = MAX_ACTIVE_PHONES;
        try {
            value = Global.getInt(sContext.getContentResolver(), "multi_sim_sms_prompt");
        } catch (SettingNotFoundException e) {
            Rlog.e(LOG_TAG, "Settings Exception Reading Dual Sim SMS Prompt Values");
        }
        boolean prompt = value == 0 ? IS_FULL_NETWORK_SUPPORTED_IN_HISI : true;
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
        for (int i = MAX_ACTIVE_PHONES; i < phones.length; i++) {
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
        return MAX_ACTIVE_PHONES;
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
}
