package com.android.server.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.debug.HdbManagerInternal;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Flog;
import android.util.Log;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.os.FileUtilsEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UEventObserverExt;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerExt;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.android.util.SlogEx;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import com.huawei.util.HwPartCommInterfaceWraper;
import com.huawei.util.LogEx;
import java.io.File;
import java.io.IOException;

public class HwUsbDeviceManager extends UsbDeviceManagerEx {
    private static final String ACTION_USBLIQUID = "huawei.intent.action.USB_LIQUID";
    private static final String ALLOW_CHARGING_ADB = "allow_charging_adb";
    private static final String CHARGE_WATER_INSTRUSED_TYPE_PATH = "sys/class/hw_power/power_ui/water_status";
    private static final String CLASS_NAME_USBLIQUID = "com.huawei.hwdetectrepair.smartnotify.eventlistener.USBLiquidReceiver";
    private static boolean DEBUG = (LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(TAG, 4)));
    private static final String MSG_USBLIQUID_TYPE = "MSG_USBLIQUID_TYPE";
    private static final String PACKAGE_NAME_USBLIQUID = "com.huawei.hwdetectrepair";
    private static final String PERMISSION_SEND_USB_LIQUID = "huawei.permission.SMART_NOTIFY_FAULT";
    private static final String PERSIST_CMCC_USB_LIMIT = "persist.sys.cmcc_usb_limit";
    private static final String SYS_CMCC_USB_LIMIT = "cmcc_usb_limit";
    private static final String TAG = HwUsbDeviceManager.class.getSimpleName();
    private static final String USB_STATE_PROPERTY = "sys.usb.state";
    private String mEventState;
    private boolean mIsBootCompleted = false;
    private final UEventObserverExt mPowerSupplyObserver = new UEventObserverExt() {
        /* class com.android.server.usb.HwUsbDeviceManager.AnonymousClass1 */

        public void onUEvent(UEventObserverExt.UEvent event) {
            try {
                if ("normal".equals(SystemPropertiesEx.get("ro.runmode", "normal"))) {
                    String state = FileUtilsEx.readTextFile(new File(HwUsbDeviceManager.CHARGE_WATER_INSTRUSED_TYPE_PATH), 0, (String) null).trim();
                    String str = HwUsbDeviceManager.TAG;
                    SlogEx.i(str, "water_intrused state= " + state);
                    if (!HwUsbDeviceManager.this.mIsBootCompleted) {
                        SlogEx.i(HwUsbDeviceManager.TAG, "boot not completed, do not send smart-notify broadcast");
                    } else if (state != null && !state.equals(HwUsbDeviceManager.this.mEventState)) {
                        HwUsbDeviceManager.this.sendUsbLiquidBroadcast(HwUsbDeviceManager.this.getContext(), state);
                        HwUsbDeviceManager.this.mEventState = state;
                    }
                }
            } catch (IOException e) {
                SlogEx.i(HwUsbDeviceManager.TAG, "Error reading charge file.");
            }
        }
    };
    private final BroadcastReceiver mSimStatusCompletedReceiver = new BroadcastReceiver() {
        /* class com.android.server.usb.HwUsbDeviceManager.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (HwUsbDeviceManager.DEBUG) {
                SlogEx.d(HwUsbDeviceManager.TAG, "sim status completed");
            }
            HwUsbDeviceManager.this.sendHandlerEmptyMessage(UsbDeviceManagerEx.MSG_SIM_COMPLETED);
        }
    };
    private UserManager mUserManager;

    public HwUsbDeviceManager(Context context, UsbAlsaManagerEx alsaManager, UsbSettingsManagerEx settingsManager) {
        super(context, alsaManager, settingsManager);
        setCmccUsbLimit();
        setUsbConfig();
        registerSimStatusCompletedReceiver();
        if (new File(CHARGE_WATER_INSTRUSED_TYPE_PATH).exists()) {
            this.mPowerSupplyObserver.startObserving("SUBSYSTEM=hw_power");
        } else {
            SlogEx.d(TAG, "charge file doesnt exist, product doesnt support the 'CHARGE_WATER_INSTRUSED' function.");
        }
    }

    /* access modifiers changed from: protected */
    public void onInitHandler() {
        try {
            getContentResolver().registerContentObserver(Settings.Global.getUriFor(ALLOW_CHARGING_ADB), false, new AllowChargingAdbSettingsObserver());
        } catch (SecurityException e) {
            SlogEx.e(TAG, "Error initializing UsbHandler!");
        } catch (IllegalArgumentException e2) {
            SlogEx.e(TAG, "IllegalArgumentException Error initializing UsbHandler!");
        }
    }

    private String getCmccUsbLimit() {
        return SystemPropertiesEx.get(PERSIST_CMCC_USB_LIMIT, "0");
    }

    private String getDebuggleMode() {
        return SystemPropertiesEx.get("ro.debuggable", "0");
    }

    private void setCmccUsbLimit() {
        String roDebuggable = getDebuggleMode();
        String usbLimit = getCmccUsbLimit();
        if (DEBUG) {
            String str = TAG;
            SlogEx.i(str, "roDebuggable " + roDebuggable + " usbLimit " + usbLimit);
        }
        if ("1".equals(roDebuggable) && "1".equals(usbLimit)) {
            SystemPropertiesEx.set(PERSIST_CMCC_USB_LIMIT, "0");
            if (DEBUG) {
                SlogEx.i(TAG, "UsbDeviceManager new init in debug mode set to 0 !");
            }
        }
    }

    private void setUsbConfig() {
        String curUsbConfig = SystemPropertiesEx.get("persist.sys.usb.config", "adb");
        String usbLimit = getCmccUsbLimit();
        if (DEBUG) {
            String str = TAG;
            SlogEx.i(str, "setUsbConfig curUsbConfig " + curUsbConfig + " usbLimit " + usbLimit);
        }
        if ("1".equals(usbLimit) && !containsFunctionOuter(curUsbConfig, "manufacture")) {
            boolean result = setUsbConfigEx("mass_storage");
            if (DEBUG) {
                String str2 = TAG;
                SlogEx.i(str2, "UsbDeviceManager new init setusbconfig result: " + result);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void registerSimStatusCompletedReceiver() {
        if (DEBUG) {
            SlogEx.d(TAG, "registerSimStatusCompletedReceiver");
        }
        if (getContext() != null) {
            getContext().registerReceiver(this.mSimStatusCompletedReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        }
    }

    /* access modifiers changed from: protected */
    public void handleSimStatusCompleted() {
        String usbLimit = getCmccUsbLimit();
        if (DEBUG) {
            String str = TAG;
            SlogEx.i(str, "simcardstate at receive sim_status_change usbLimit = " + usbLimit);
        }
        if (!"0".equals(usbLimit)) {
            int simCardState = 0;
            if (getContext() != null) {
                TelephonyManager tm = (TelephonyManager) getContext().getSystemService("phone");
                if (tm == null) {
                    SlogEx.i(TAG, "TelephonyManager is null, return!");
                    return;
                }
                simCardState = tm.getSimState();
            }
            if (DEBUG) {
                String str2 = TAG;
                SlogEx.i(str2, "simcardstate at boot completed is " + simCardState);
            }
            if (simCardState != 0 && simCardState != 1 && simCardState != 8 && simCardState != 6) {
                SlogEx.i(TAG, "persist.sys.cmcc_usb_limit to 0 ");
                SystemPropertiesEx.set(PERSIST_CMCC_USB_LIMIT, "0");
                setEnabledFunctionsEx("hisuite,mtp,mass_storage", true);
                if (getContext() != null && getUsbHandlerConnected()) {
                    SlogEx.i(TAG, "Secure SYS_CMCC_USB_LIMIT 0 ");
                    Settings.Secure.putInt(getContext().getContentResolver(), SYS_CMCC_USB_LIMIT, 0);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean interceptSetEnabledFunctions(String functions) {
        boolean isManufacturePort = false;
        if (DEBUG) {
            String str = TAG;
            SlogEx.i(str, "interceptSetEnabledFunctions functions:" + functions);
        }
        if (functions != null) {
            isManufacturePort = containsFunctionOuter(functions, "manufacture");
        }
        String value = SystemPropertiesEx.get(USB_STATE_PROPERTY, BuildConfig.FLAVOR);
        if (!value.equals(functions) || !isManufacturePort || isAdbDisabledByDevicePolicy()) {
            String str2 = TAG;
            SlogEx.i(str2, "function: " + functions + " sys.usb.state: " + value);
            String usbLimit = getCmccUsbLimit();
            if ("0".equals(usbLimit)) {
                return false;
            }
            int simCardState = 0;
            if (getContext() != null) {
                TelephonyManager tm = (TelephonyManager) getContext().getSystemService("phone");
                if (tm == null) {
                    SlogEx.w(TAG, "TelephonyManager is null, return!");
                    return false;
                }
                simCardState = tm.getSimState();
            }
            if (DEBUG) {
                String str3 = TAG;
                SlogEx.i(str3, "interceptSetEnabledFunctions simcardstate = " + simCardState + " IsManufacturePort:" + isManufacturePort);
            }
            if (!(simCardState == 0 || simCardState == 1 || simCardState == 8 || simCardState == 6)) {
                SystemPropertiesEx.set(PERSIST_CMCC_USB_LIMIT, "0");
                usbLimit = "0";
            }
            if (!"1".equals(usbLimit) || isManufacturePort) {
                return false;
            }
            if (DEBUG) {
                SlogEx.i(TAG, "cmcc usb_limit return !");
            }
            return true;
        }
        String str4 = TAG;
        SlogEx.i(str4, "The current function: " + functions + " has been set, return!");
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isCmccUsbLimit() {
        if ("1".equals(SystemPropertiesEx.get(PERSIST_CMCC_USB_LIMIT, "0"))) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isAdbDisabled() {
        if (!HwPartCommInterfaceWraper.disallowOp(11)) {
            return false;
        }
        Settings.Global.putInt(getContentResolver(), "adb_enabled", 0);
        return true;
    }

    public void bootCompleted() {
        this.mIsBootCompleted = true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendUsbLiquidBroadcast(Context context, String msg) {
        Intent intent = new Intent(ACTION_USBLIQUID);
        intent.setClassName(PACKAGE_NAME_USBLIQUID, CLASS_NAME_USBLIQUID);
        intent.putExtra(MSG_USBLIQUID_TYPE, msg);
        context.sendBroadcastAsUser(intent, UserHandleEx.ALL, PERMISSION_SEND_USB_LIQUID);
    }

    private UserManager getUserManager() {
        if (this.mUserManager == null) {
            this.mUserManager = UserManagerExt.get(getContext());
        }
        return this.mUserManager;
    }

    /* access modifiers changed from: protected */
    public boolean isRepairMode() {
        return UserManagerExt.getUserInfoEx(getUserManager(), ActivityManagerEx.getCurrentUser()).isRepairMode();
    }

    /* access modifiers changed from: protected */
    public String applyUserRestrictions(String functions) {
        UserManager userManager = (UserManager) getContext().getSystemService("user");
        if (userManager == null || !userManager.hasUserRestriction("no_usb_file_transfer")) {
            return functions;
        }
        String functions2 = removeFunctionOuter(removeFunctionOuter(removeFunctionOuter(removeFunctionOuter(removeFunctionOuter(functions, "mtp"), "ptp"), "mass_storage"), "hisuite"), "hdb");
        if ("none".equals(functions2)) {
            return "mtp";
        }
        return functions2;
    }

    private boolean isHdbEnabled() {
        HdbManagerInternal hdbManagerInternal = (HdbManagerInternal) LocalServicesExt.getService(HdbManagerInternal.class);
        return hdbManagerInternal != null && hdbManagerInternal.isHdbEnabled();
    }

    /* access modifiers changed from: protected */
    public String applyHdbFunction(String functions) {
        if (containsFunctionOuter(functions, "hdb")) {
            functions = removeFunctionOuter(functions, "hdb");
        }
        if (shouldApplyHdbFunction(functions)) {
            if (!containsFunctionOuter(functions, "hdb")) {
                functions = addFunctionOuter(functions, "hdb");
            }
            String str = TAG;
            SlogEx.i(str, "add hdb is " + functions);
        }
        return functions;
    }

    private boolean shouldApplyHdbFunction(String functions) {
        if (isCmccUsbLimit()) {
            SlogEx.i(TAG, "cmcc_usb_limit do not set hdb");
            return false;
        } else if (isRepairMode()) {
            return true;
        } else {
            if (!isHdbEnabled() || (!"mtp".equals(functions) && !"mtp,adb".equals(functions) && !"ptp".equals(functions) && !"ptp,adb".equals(functions) && !"hisuite,mtp,mass_storage".equals(functions) && !"hisuite,mtp,mass_storage,adb".equals(functions) && !"bicr".equals(functions) && !"bicr,adb".equals(functions) && !"rndis".equals(functions) && !"rndis,adb".equals(functions))) {
                return false;
            }
            return true;
        }
    }

    private class AllowChargingAdbSettingsObserver extends ContentObserver {
        AllowChargingAdbSettingsObserver() {
            super(null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isChange) {
            boolean enable = false;
            if (Settings.Global.getInt(HwUsbDeviceManager.this.getContentResolver(), HwUsbDeviceManager.ALLOW_CHARGING_ADB, 0) > 0) {
                enable = true;
            }
            Flog.i(1306, HwUsbDeviceManager.TAG + " AllowChargingAdb Settings enable:" + enable);
            HwUsbDeviceManager.this.sendHandlerMessage(UsbDeviceManagerEx.MSG_ENABLE_ALLOWCHARGINGADB, enable);
        }
    }
}
