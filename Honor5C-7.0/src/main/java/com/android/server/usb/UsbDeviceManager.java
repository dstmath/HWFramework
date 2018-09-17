package com.android.server.usb;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Flog;
import android.util.Pair;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.FgThread;
import com.android.server.HwServiceFactory;
import com.android.server.job.controllers.JobStatus;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class UsbDeviceManager extends AbsUsbDeviceManager {
    private static final int ACCESSORY_REQUEST_TIMEOUT = 10000;
    private static final String ACCESSORY_START_MATCH = "DEVPATH=/devices/virtual/misc/usb_accessory";
    private static final String ACTION_USB_USER_UPDATE = "android.hardware.usb.action.USB_UPDATE";
    private static final int AUDIO_MODE_SOURCE = 1;
    private static final String AUDIO_SOURCE_PCM_PATH = "/sys/class/android_usb/android0/f_audio_source/pcm";
    private static final String BOOT_MODE_PROPERTY = "ro.bootmode";
    private static final String CHARGE_WATER_INSTRUSED_TYPE_PATH = "sys/class/hw_power/charger/charge_data/water_intrused";
    private static boolean DEBUG = false;
    private static final String FUNCTIONS_PATH = "/sys/class/android_usb/android0/functions";
    private static final String MIDI_ALSA_PATH = "/sys/class/android_usb/android0/f_midi/alsa";
    private static final int MSG_BOOT_COMPLETED = 4;
    private static final int MSG_ENABLE_ADB = 1;
    private static final int MSG_ENABLE_ALLOWCHARGINGADB = 12;
    private static final int MSG_ENABLE_HDB = 9;
    private static final int MSG_MIRRORLINK_REQUESTED = 11;
    private static final int MSG_SET_CURRENT_FUNCTIONS = 2;
    private static final int MSG_SET_USB_DATA_UNLOCKED = 6;
    protected static final int MSG_SIM_COMPLETED = 10;
    private static final int MSG_SYSTEM_READY = 3;
    private static final int MSG_UPDATE_HOST_STATE = 8;
    private static final int MSG_UPDATE_STATE = 0;
    private static final int MSG_UPDATE_USER_RESTRICTIONS = 7;
    private static final int MSG_USER_SWITCHED = 5;
    private static final String RNDIS_ETH_ADDR_PATH = "/sys/class/android_usb/android0/f_rndis/ethaddr";
    private static final String STATE_PATH = "/sys/class/android_usb/android0/state";
    private static final String SUITE_STATE_FILE = "android_usb/f_mass_storage/suitestate";
    private static final String SUITE_STATE_PATH = "/sys/class";
    private static final String TAG = "UsbDeviceManager";
    private static final int UPDATE_DELAY = 1000;
    private static final String USBDATA_UNLOCKED = "usbdata_unlocked";
    private static final String USB_CONFIG_PROPERTY = "sys.usb.config";
    protected static final String USB_PERSISTENT_CONFIG_PROPERTY = "persist.sys.usb.config";
    private static final String USB_STATE_MATCH = "DEVPATH=/devices/virtual/android_usb/android0";
    private static final String USB_STATE_PROPERTY = "sys.usb.state";
    private String ALLOW_CHARGING_ADB;
    private long mAccessoryModeRequestTime;
    private String[] mAccessoryStrings;
    private boolean mAdbEnabled;
    private boolean mAudioSourceEnabled;
    private boolean mBootCompleted;
    private Intent mBroadcastedIntent;
    private boolean mChargingOnlySelected;
    protected final ContentResolver mContentResolver;
    private final Context mContext;
    @GuardedBy("mLock")
    private UsbSettingsManager mCurrentSettings;
    private UsbDebuggingManager mDebuggingManager;
    private HwUsbHDBManager mHDBManager;
    private UsbHandler mHandler;
    private final boolean mHasUsbAccessory;
    private boolean mHdbEnabled;
    private final BroadcastReceiver mHostReceiver;
    private boolean mLastChargingState;
    private final Object mLock;
    private int mMidiCard;
    private int mMidiDevice;
    private boolean mMidiEnabled;
    private NotificationManager mNotificationManager;
    private Map<String, List<Pair<String, String>>> mOemModeMap;
    private boolean mPowerCharging;
    private final UEventObserver mPowerSupplyObserver;
    private boolean mSystemReady;
    private final UEventObserver mUEventObserver;
    private boolean mUSBPlugType;
    private final UsbAlsaManager mUsbAlsaManager;
    private boolean mUseUsbNotification;
    private boolean settingsHdbEnabled;

    private class AdbSettingsObserver extends ContentObserver {
        public AdbSettingsObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            FileNotFoundException e;
            String str;
            String str2;
            IOException ex;
            Exception ea;
            Throwable th;
            boolean enable = Global.getInt(UsbDeviceManager.this.mContentResolver, "adb_enabled", UsbDeviceManager.MSG_UPDATE_STATE) > 0;
            if (enable && UsbDeviceManager.this.isAdbDisabled()) {
                Flog.i(1306, "UsbDeviceManager Adb is disabled by dpm");
                return;
            }
            int userType;
            Flog.i(1306, "UsbDeviceManager Adb Settings enable:" + enable);
            UsbDeviceManager.this.mHandler.sendMessage((int) UsbDeviceManager.MSG_ENABLE_ADB, enable);
            int logSwitch = UsbDeviceManager.MSG_UPDATE_STATE;
            BufferedReader bufferedReader = null;
            try {
                BufferedReader hwLogReader = new BufferedReader(new InputStreamReader(new FileInputStream("/dev/hwlog_switch"), "UTF-8"));
                try {
                    String tempString = hwLogReader.readLine();
                    if (tempString != null) {
                        logSwitch = Integer.parseInt(tempString);
                    }
                    Slog.i(UsbDeviceManager.TAG, "/dev/hwlog_switch=" + logSwitch);
                    if (hwLogReader != null) {
                        try {
                            hwLogReader.close();
                        } catch (IOException e2) {
                            Slog.i(UsbDeviceManager.TAG, "hwLogReader close failed", e2);
                        }
                    }
                    bufferedReader = hwLogReader;
                } catch (FileNotFoundException e3) {
                    e = e3;
                    bufferedReader = hwLogReader;
                    Slog.e(UsbDeviceManager.TAG, "/dev/hwlog_switch not exist", e);
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e22) {
                            Slog.i(UsbDeviceManager.TAG, "hwLogReader close failed", e22);
                        }
                    }
                    userType = SystemProperties.getInt("ro.logsystem.usertype", UsbDeviceManager.MSG_UPDATE_STATE);
                    str = "sys.logbuffer.disable";
                    if (enable) {
                        str2 = "false";
                    } else {
                        str2 = "true";
                    }
                    SystemProperties.set(str, str2);
                } catch (IOException e4) {
                    ex = e4;
                    bufferedReader = hwLogReader;
                    Slog.i(UsbDeviceManager.TAG, "logswitch read failed", ex);
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e222) {
                            Slog.i(UsbDeviceManager.TAG, "hwLogReader close failed", e222);
                        }
                    }
                    userType = SystemProperties.getInt("ro.logsystem.usertype", UsbDeviceManager.MSG_UPDATE_STATE);
                    str = "sys.logbuffer.disable";
                    if (enable) {
                        str2 = "true";
                    } else {
                        str2 = "false";
                    }
                    SystemProperties.set(str, str2);
                } catch (Exception e5) {
                    ea = e5;
                    bufferedReader = hwLogReader;
                    try {
                        Slog.i(UsbDeviceManager.TAG, "logswitch read exception", ea);
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e2222) {
                                Slog.i(UsbDeviceManager.TAG, "hwLogReader close failed", e2222);
                            }
                        }
                        userType = SystemProperties.getInt("ro.logsystem.usertype", UsbDeviceManager.MSG_UPDATE_STATE);
                        str = "sys.logbuffer.disable";
                        if (enable) {
                            str2 = "false";
                        } else {
                            str2 = "true";
                        }
                        SystemProperties.set(str, str2);
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e22222) {
                                Slog.i(UsbDeviceManager.TAG, "hwLogReader close failed", e22222);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = hwLogReader;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e6) {
                e = e6;
                Slog.e(UsbDeviceManager.TAG, "/dev/hwlog_switch not exist", e);
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                userType = SystemProperties.getInt("ro.logsystem.usertype", UsbDeviceManager.MSG_UPDATE_STATE);
                str = "sys.logbuffer.disable";
                if (enable) {
                    str2 = "true";
                } else {
                    str2 = "false";
                }
                SystemProperties.set(str, str2);
            } catch (IOException e7) {
                ex = e7;
                Slog.i(UsbDeviceManager.TAG, "logswitch read failed", ex);
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                userType = SystemProperties.getInt("ro.logsystem.usertype", UsbDeviceManager.MSG_UPDATE_STATE);
                str = "sys.logbuffer.disable";
                if (enable) {
                    str2 = "false";
                } else {
                    str2 = "true";
                }
                SystemProperties.set(str, str2);
            } catch (Exception e8) {
                ea = e8;
                Slog.i(UsbDeviceManager.TAG, "logswitch read exception", ea);
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                userType = SystemProperties.getInt("ro.logsystem.usertype", UsbDeviceManager.MSG_UPDATE_STATE);
                str = "sys.logbuffer.disable";
                if (enable) {
                    str2 = "true";
                } else {
                    str2 = "false";
                }
                SystemProperties.set(str, str2);
            }
            userType = SystemProperties.getInt("ro.logsystem.usertype", UsbDeviceManager.MSG_UPDATE_STATE);
            if ((UsbDeviceManager.MSG_ENABLE_ADB == userType || UsbDeviceManager.MSG_SET_USB_DATA_UNLOCKED == userType) && logSwitch != UsbDeviceManager.MSG_ENABLE_ADB) {
                str = "sys.logbuffer.disable";
                if (enable) {
                    str2 = "false";
                } else {
                    str2 = "true";
                }
                SystemProperties.set(str, str2);
            }
        }
    }

    private class AllowChargingAdbSettingsObserver extends ContentObserver {
        public AllowChargingAdbSettingsObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            boolean enable = Global.getInt(UsbDeviceManager.this.mContentResolver, UsbDeviceManager.this.ALLOW_CHARGING_ADB, UsbDeviceManager.MSG_UPDATE_STATE) > 0;
            Flog.i(1306, "UsbDeviceManager AllowChargingAdb Settings enable:" + enable);
            UsbDeviceManager.this.mHandler.sendMessage((int) UsbDeviceManager.MSG_ENABLE_ALLOWCHARGINGADB, enable);
        }
    }

    private class HdbSettingsObserver extends ContentObserver {
        public HdbSettingsObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            boolean enable = System.getInt(UsbDeviceManager.this.mContentResolver, "hdb_enabled", UsbDeviceManager.MSG_UPDATE_STATE) > 0;
            Flog.i(1306, "UsbDeviceManager Hdb Settings enable:" + enable);
            UsbDeviceManager.this.mHandler.sendMessage((int) UsbDeviceManager.MSG_ENABLE_HDB, enable);
        }
    }

    private class SuitestateObserver extends ContentObserver {
        public SuitestateObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            UsbDeviceManager.this.writeSuitestate();
        }
    }

    private final class UsbHandler extends Handler {
        private boolean mAdbNotificationShown;
        private boolean mConfigured;
        private boolean mConnected;
        private UsbAccessory mCurrentAccessory;
        private String mCurrentFunctions;
        private boolean mCurrentFunctionsApplied;
        private int mCurrentUser;
        private boolean mHostConnected;
        private boolean mMirrorlinkRequested;
        private boolean mSourcePower;
        private boolean mUsbDataUnlocked;
        private int mUsbNotificationId;

        public UsbHandler(Looper looper) {
            super(looper);
            this.mCurrentUser = -10000;
            try {
                this.mCurrentFunctions = SystemProperties.get(UsbDeviceManager.USB_CONFIG_PROPERTY, "none");
                if ("none".equals(this.mCurrentFunctions)) {
                    this.mCurrentFunctions = "mtp";
                }
                this.mCurrentFunctionsApplied = this.mCurrentFunctions.equals(SystemProperties.get(UsbDeviceManager.USB_STATE_PROPERTY));
                if (System.getInt(UsbDeviceManager.this.mContentResolver, "hdb_enabled", UsbDeviceManager.MSG_UPDATE_STATE) > 0) {
                    UsbDeviceManager.this.settingsHdbEnabled = true;
                }
                UsbDeviceManager.this.mAdbEnabled = UsbManager.containsFunction(getDefaultFunctions(), "adb");
                setEnabledFunctions(null, false);
                updateState(FileUtils.readTextFile(new File(UsbDeviceManager.STATE_PATH), UsbDeviceManager.MSG_UPDATE_STATE, null).trim());
                UsbDeviceManager.this.mContentResolver.registerContentObserver(Global.getUriFor("adb_enabled"), false, new AdbSettingsObserver());
                UsbDeviceManager.this.mContentResolver.registerContentObserver(Global.getUriFor(UsbDeviceManager.this.ALLOW_CHARGING_ADB), false, new AllowChargingAdbSettingsObserver());
                if (SystemProperties.get("persist.service.hdb.enable", "false").equals("true")) {
                    UsbDeviceManager.this.mContentResolver.registerContentObserver(System.getUriFor("hdb_enabled"), false, new HdbSettingsObserver());
                }
                UsbDeviceManager.this.mContentResolver.registerContentObserver(Secure.getUriFor("suitestate"), false, new SuitestateObserver());
                UsbDeviceManager.this.mUEventObserver.startObserving(UsbDeviceManager.USB_STATE_MATCH);
                UsbDeviceManager.this.mUEventObserver.startObserving(UsbDeviceManager.ACCESSORY_START_MATCH);
                if (new File(UsbDeviceManager.CHARGE_WATER_INSTRUSED_TYPE_PATH).exists()) {
                    UsbDeviceManager.this.mPowerSupplyObserver.startObserving("SUBSYSTEM=power_supply");
                } else {
                    Slog.d(UsbDeviceManager.TAG, "charge file doesnt exist, product doesnt support the 'CHARGE_WATER_INSTRUSED' function.");
                }
            } catch (Exception e) {
                Slog.e(UsbDeviceManager.TAG, "Error initializing UsbHandler", e);
            }
        }

        public void sendMessage(int what, boolean arg) {
            removeMessages(what);
            Message m = Message.obtain(this, what);
            m.arg1 = arg ? UsbDeviceManager.MSG_ENABLE_ADB : UsbDeviceManager.MSG_UPDATE_STATE;
            sendMessage(m);
        }

        public void sendMessage(int what, Object arg) {
            removeMessages(what);
            Message m = Message.obtain(this, what);
            m.obj = arg;
            sendMessage(m);
        }

        public void updateState(String state) {
            int connected;
            int configured;
            int i = UsbDeviceManager.MSG_UPDATE_STATE;
            if ("DISCONNECTED".equals(state)) {
                connected = UsbDeviceManager.MSG_UPDATE_STATE;
                configured = UsbDeviceManager.MSG_UPDATE_STATE;
            } else if ("CONNECTED".equals(state)) {
                connected = UsbDeviceManager.MSG_ENABLE_ADB;
                configured = UsbDeviceManager.MSG_UPDATE_STATE;
            } else if ("CONFIGURED".equals(state)) {
                connected = UsbDeviceManager.MSG_ENABLE_ADB;
                configured = UsbDeviceManager.MSG_ENABLE_ADB;
            } else {
                Slog.e(UsbDeviceManager.TAG, "unknown state " + state);
                return;
            }
            removeMessages(UsbDeviceManager.MSG_UPDATE_STATE);
            Message msg = Message.obtain(this, UsbDeviceManager.MSG_UPDATE_STATE);
            msg.arg1 = connected;
            msg.arg2 = configured;
            if (connected == 0) {
                i = UsbDeviceManager.UPDATE_DELAY;
            }
            sendMessageDelayed(msg, (long) i);
        }

        public void updateHostState(UsbPort port, UsbPortStatus status) {
            int i;
            int i2 = UsbDeviceManager.MSG_ENABLE_ADB;
            boolean hostConnected = false;
            boolean sourcePower = false;
            if (status != null) {
                hostConnected = status.getCurrentDataRole() == UsbDeviceManager.MSG_ENABLE_ADB;
                sourcePower = status.getCurrentPowerRole() == UsbDeviceManager.MSG_ENABLE_ADB;
            }
            if (hostConnected) {
                i = UsbDeviceManager.MSG_ENABLE_ADB;
            } else {
                i = UsbDeviceManager.MSG_UPDATE_STATE;
            }
            if (!sourcePower) {
                i2 = UsbDeviceManager.MSG_UPDATE_STATE;
            }
            obtainMessage(UsbDeviceManager.MSG_UPDATE_HOST_STATE, i, i2).sendToTarget();
        }

        private boolean waitForState(String state) {
            String value = null;
            for (int i = UsbDeviceManager.MSG_UPDATE_STATE; i < 40; i += UsbDeviceManager.MSG_ENABLE_ADB) {
                value = SystemProperties.get(UsbDeviceManager.USB_STATE_PROPERTY);
                if (state.equals(value)) {
                    return true;
                }
                SystemClock.sleep(50);
            }
            Flog.e(1306, "UsbDeviceManager waitForState(" + state + ") FAILED: got " + value);
            return false;
        }

        private boolean setUsbConfig(String config) {
            Flog.i(1306, "UsbDeviceManager setUsbConfig(" + config + ")");
            SystemProperties.set(UsbDeviceManager.USB_CONFIG_PROPERTY, config);
            return waitForState(config);
        }

        private void setUsbDataUnlocked(boolean enable) {
            if (UsbDeviceManager.DEBUG) {
                Slog.d(UsbDeviceManager.TAG, "setUsbDataUnlocked: " + enable);
            }
            this.mUsbDataUnlocked = enable;
            setEnabledFunctions(this.mCurrentFunctions, true);
        }

        private void setAdbEnabled(boolean enable) {
            if (UsbDeviceManager.DEBUG) {
                Slog.d(UsbDeviceManager.TAG, "setAdbEnabled: " + enable);
            }
            if (enable != UsbDeviceManager.this.mAdbEnabled) {
                UsbDeviceManager.this.mAdbEnabled = enable;
                if (enable && isChargingOnly_N()) {
                    UsbDeviceManager.this.mAdbEnabled = false;
                    return;
                }
                String oldFunctions = getDefaultFunctions();
                String newFunctions = applyHdbFunction(applyAdbFunction(oldFunctions));
                if (!oldFunctions.equals(newFunctions)) {
                    Flog.i(1306, "UsbDeviceManager setAdbEnabled -> USB_PERSISTENT_CONFIG_PROPERTY : " + newFunctions);
                    SystemProperties.set(UsbDeviceManager.USB_PERSISTENT_CONFIG_PROPERTY, newFunctions);
                }
                setEnabledFunctions(this.mCurrentFunctions, false);
                updateAdbNotification();
            }
            if (UsbDeviceManager.this.mDebuggingManager != null) {
                UsbDeviceManager.this.mDebuggingManager.setAdbEnabled(UsbDeviceManager.this.mAdbEnabled);
            }
        }

        private void setHdbEnabled(boolean enable) {
            if (UsbDeviceManager.DEBUG) {
                Slog.d(UsbDeviceManager.TAG, "setHdbEnabled: " + enable);
            }
            if (enable != UsbDeviceManager.this.settingsHdbEnabled) {
                UsbDeviceManager.this.settingsHdbEnabled = enable;
                String oldFunctions = getDefaultFunctions();
                String newFunctions = applyHdbFunction(oldFunctions);
                if (!oldFunctions.equals(newFunctions)) {
                    Flog.i(1306, "UsbDeviceManager setHdbEnabled -> USB_PERSISTENT_CONFIG_PROPERTY : " + newFunctions);
                    SystemProperties.set(UsbDeviceManager.USB_PERSISTENT_CONFIG_PROPERTY, newFunctions);
                }
                setEnabledFunctions(this.mCurrentFunctions, false);
            }
            if (UsbDeviceManager.this.mHDBManager != null) {
                UsbDeviceManager.this.mHDBManager.setHdbEnabled(UsbDeviceManager.this.settingsHdbEnabled);
            }
        }

        private void setEnabledFunctions(String functions, boolean forceRestart) {
            setEnabledFunctions(functions, forceRestart, false);
        }

        private void setEnabledFunctions(String functions, boolean forceRestart, boolean makeDefault) {
            if (UsbDeviceManager.DEBUG) {
                Slog.d(UsbDeviceManager.TAG, "setEnabledFunctions functions=" + functions + ", " + "forceRestart=" + forceRestart + ", makeDefault=" + makeDefault);
            }
            if (!UsbDeviceManager.this.interceptSetEnabledFunctions(functions)) {
                String oldFunctions = this.mCurrentFunctions;
                boolean oldFunctionsApplied = this.mCurrentFunctionsApplied;
                if (!trySetEnabledFunctions(functions, forceRestart, makeDefault)) {
                    if (oldFunctionsApplied && !oldFunctions.equals(functions)) {
                        Slog.e(UsbDeviceManager.TAG, "Failsafe 1: Restoring previous USB functions.");
                        if (trySetEnabledFunctions(oldFunctions, false)) {
                            return;
                        }
                    }
                    Slog.e(UsbDeviceManager.TAG, "Failsafe 2: Restoring default USB functions.");
                    if (!trySetEnabledFunctions(null, false)) {
                        Slog.e(UsbDeviceManager.TAG, "Failsafe 3: Restoring empty function list (with ADB if enabled).");
                        if (!trySetEnabledFunctions("none", false)) {
                            Slog.e(UsbDeviceManager.TAG, "Unable to set any USB functions!");
                        }
                    }
                }
            }
        }

        private boolean trySetEnabledFunctions(String functions, boolean forceRestart) {
            return trySetEnabledFunctions(functions, forceRestart, false);
        }

        private boolean trySetEnabledFunctions(String functions, boolean forceRestart, boolean makeDefault) {
            if (functions == null) {
                functions = getDefaultFunctions();
                if (isAllowedAdbHdbApply() || functions.contains("manufacture")) {
                    functions = applyHdbFunction(applyAdbFunction(functions));
                } else {
                    functions = UsbManager.removeFunction(UsbManager.removeFunction(functions, "adb"), "hdb");
                }
            } else if (!isChargingOnly_N()) {
                functions = applyHdbFunction(applyAdbFunction(functions));
            }
            functions = applyUserRestrictions(UsbDeviceManager.this.applyOemOverrideFunction(functions));
            if (makeDefault && !getDefaultFunctions().equals(functions)) {
                forceRestart = true;
            }
            if (!(this.mCurrentFunctions.equals(functions) && this.mCurrentFunctionsApplied && !r9)) {
                Slog.i(UsbDeviceManager.TAG, "Setting USB config to " + functions);
                this.mCurrentFunctionsApplied = false;
                setUsbConfig("none");
                if (makeDefault) {
                    Flog.i(1306, "UsbDeviceManager setDefaultFunctions -> USB_PERSISTENT_CONFIG_PROPERTY : " + functions);
                    SystemProperties.set(UsbDeviceManager.USB_PERSISTENT_CONFIG_PROPERTY, functions);
                    if (!waitForState(functions)) {
                        Slog.e(UsbDeviceManager.TAG, "Failed to switch persistent USB config to " + functions);
                        return false;
                    }
                } else if (!setUsbConfig(functions)) {
                    Slog.e(UsbDeviceManager.TAG, "Failed to switch USB config to " + functions);
                    return false;
                }
                if (isChargingOnly_N()) {
                    updateUsbNotification();
                    if (UsbDeviceManager.this.mNotificationManager != null && this.mAdbNotificationShown) {
                        UsbDeviceManager.this.mNotificationManager.cancelAsUser(null, 17040390, UserHandle.ALL);
                        this.mAdbNotificationShown = false;
                    }
                }
                this.mCurrentFunctions = functions;
                this.mCurrentFunctionsApplied = true;
            }
            return true;
        }

        private boolean isAllowedAdbHdbApply() {
            return !UsbDeviceManager.this.mChargingOnlySelected || (Global.getInt(UsbDeviceManager.this.mContentResolver, UsbDeviceManager.this.ALLOW_CHARGING_ADB, UsbDeviceManager.MSG_UPDATE_STATE) == UsbDeviceManager.MSG_ENABLE_ADB);
        }

        private boolean isChargingOnly_N() {
            if (isAllowedAdbHdbApply() || !UsbDeviceManager.this.mChargingOnlySelected) {
                return false;
            }
            return true;
        }

        private void updateUsbState(boolean enable) {
            if (!enable && isChargingOnly_N()) {
                Global.putInt(UsbDeviceManager.this.mContentResolver, "adb_enabled", UsbDeviceManager.MSG_UPDATE_STATE);
                System.putInt(UsbDeviceManager.this.mContentResolver, "hdb_enabled", UsbDeviceManager.MSG_UPDATE_STATE);
                setEnabledFunctions(null, false);
            }
        }

        private String applyAdbFunction(String functions) {
            if (UsbDeviceManager.this.mAdbEnabled) {
                return UsbManager.addFunction(functions, "adb");
            }
            return UsbDeviceManager.this.removeAdbFunction(functions, "adb");
        }

        private String applyHdbFunction(String functions) {
            if (UsbManager.containsFunction(functions, "hdb")) {
                functions = UsbManager.removeFunction(functions, "hdb");
            }
            if (!UsbDeviceManager.this.HdbIsEnableFunction(functions)) {
                return functions;
            }
            if (UsbDeviceManager.this.mHdbEnabled) {
                if (!UsbManager.containsFunction(functions, "hdb")) {
                    functions = UsbManager.addFunction(functions, "hdb");
                }
                Slog.i(UsbDeviceManager.TAG, "add hdb is " + functions);
                return functions;
            }
            functions = UsbManager.removeFunction(functions, "hdb");
            Slog.i(UsbDeviceManager.TAG, "remove hdb is " + functions);
            return functions;
        }

        private String applyUserRestrictions(String functions) {
            if (((UserManager) UsbDeviceManager.this.mContext.getSystemService("user")).hasUserRestriction("no_usb_file_transfer")) {
                return UsbManager.removeFunction(UsbManager.removeFunction(UsbManager.removeFunction(UsbManager.removeFunction(UsbManager.removeFunction(functions, "mtp"), "ptp"), "mass_storage"), "hisuite"), "hdb");
            }
            return functions;
        }

        private boolean isUsbTransferAllowed() {
            return !((UserManager) UsbDeviceManager.this.mContext.getSystemService("user")).hasUserRestriction("no_usb_file_transfer");
        }

        private void updateCurrentAccessory() {
            boolean enteringAccessoryMode = UsbDeviceManager.this.mAccessoryModeRequestTime > 0 ? SystemClock.elapsedRealtime() < UsbDeviceManager.this.mAccessoryModeRequestTime + JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY : false;
            if (this.mConfigured && enteringAccessoryMode) {
                if (UsbDeviceManager.this.mAccessoryStrings != null) {
                    this.mCurrentAccessory = new UsbAccessory(UsbDeviceManager.this.mAccessoryStrings);
                    Slog.d(UsbDeviceManager.TAG, "entering USB accessory mode: " + this.mCurrentAccessory);
                    if (UsbDeviceManager.this.mBootCompleted) {
                        UsbDeviceManager.this.getCurrentSettings().accessoryAttached(this.mCurrentAccessory);
                        return;
                    }
                    return;
                }
                Slog.e(UsbDeviceManager.TAG, "nativeGetAccessoryStrings failed");
            } else if (!enteringAccessoryMode) {
                Slog.d(UsbDeviceManager.TAG, "exited USB accessory mode");
                setEnabledFunctions(null, false);
                if (this.mCurrentAccessory != null) {
                    if (UsbDeviceManager.this.mBootCompleted) {
                        UsbDeviceManager.this.getCurrentSettings().accessoryDetached(this.mCurrentAccessory);
                    }
                    this.mCurrentAccessory = null;
                    UsbDeviceManager.this.mAccessoryStrings = null;
                }
            }
        }

        private boolean isUsbStateChanged(Intent intent) {
            Set<String> keySet = intent.getExtras().keySet();
            if (UsbDeviceManager.this.mBroadcastedIntent == null) {
                for (String key : keySet) {
                    if (intent.getBooleanExtra(key, false) && !"mtp".equals(key)) {
                        return true;
                    }
                }
            } else if (!keySet.equals(UsbDeviceManager.this.mBroadcastedIntent.getExtras().keySet())) {
                return true;
            } else {
                for (String key2 : keySet) {
                    if (intent.getBooleanExtra(key2, false) != UsbDeviceManager.this.mBroadcastedIntent.getBooleanExtra(key2, false)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void updateUsbStateBroadcastIfNeeded() {
            Intent intent = new Intent("android.hardware.usb.action.USB_STATE");
            intent.addFlags(805306368);
            intent.putExtra("connected", this.mConnected);
            intent.putExtra("host_connected", this.mHostConnected);
            intent.putExtra("configured", this.mConfigured);
            intent.putExtra("unlocked", isUsbTransferAllowed() ? this.mUsbDataUnlocked : false);
            intent.putExtra("only_charging", UsbDeviceManager.this.mPowerCharging);
            intent.putExtra("ncm_requested", this.mMirrorlinkRequested);
            if (this.mCurrentFunctions != null) {
                String[] functions = this.mCurrentFunctions.split(",");
                for (int i = UsbDeviceManager.MSG_UPDATE_STATE; i < functions.length; i += UsbDeviceManager.MSG_ENABLE_ADB) {
                    String function = functions[i];
                    if (!"none".equals(function)) {
                        intent.putExtra(function, true);
                    }
                }
            }
            if (isUsbStateChanged(intent)) {
                Flog.i(1306, "UsbDeviceManagerbroadcasting " + intent + " extras: " + intent.getExtras());
                UsbDeviceManager.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                UsbDeviceManager.this.mBroadcastedIntent = intent;
                return;
            }
            Flog.i(1306, "UsbDeviceManagerskip broadcasting " + intent + " extras: " + intent.getExtras());
        }

        private void updateUserBroadcast() {
            UsbDeviceManager.this.mContext.sendBroadcastAsUser(new Intent(UsbDeviceManager.ACTION_USB_USER_UPDATE), UserHandle.ALL, "android.permission.MANAGE_USB");
        }

        private void updateUsbFunctions() {
            updateAudioSourceFunction();
            updateMidiFunction();
        }

        private void updateAudioSourceFunction() {
            FileNotFoundException e;
            Throwable th;
            boolean enabled = UsbManager.containsFunction(this.mCurrentFunctions, "audio_source");
            if (enabled != UsbDeviceManager.this.mAudioSourceEnabled) {
                int card = -1;
                int device = -1;
                if (enabled) {
                    Scanner scanner = null;
                    try {
                        Scanner scanner2 = new Scanner(new File(UsbDeviceManager.AUDIO_SOURCE_PCM_PATH));
                        try {
                            card = scanner2.nextInt();
                            device = scanner2.nextInt();
                            if (scanner2 != null) {
                                scanner2.close();
                            }
                        } catch (FileNotFoundException e2) {
                            e = e2;
                            scanner = scanner2;
                            try {
                                Slog.e(UsbDeviceManager.TAG, "could not open audio source PCM file", e);
                                if (scanner != null) {
                                    scanner.close();
                                }
                                UsbDeviceManager.this.mUsbAlsaManager.setAccessoryAudioState(enabled, card, device);
                                UsbDeviceManager.this.mAudioSourceEnabled = enabled;
                            } catch (Throwable th2) {
                                th = th2;
                                if (scanner != null) {
                                    scanner.close();
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            scanner = scanner2;
                            if (scanner != null) {
                                scanner.close();
                            }
                            throw th;
                        }
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        Slog.e(UsbDeviceManager.TAG, "could not open audio source PCM file", e);
                        if (scanner != null) {
                            scanner.close();
                        }
                        UsbDeviceManager.this.mUsbAlsaManager.setAccessoryAudioState(enabled, card, device);
                        UsbDeviceManager.this.mAudioSourceEnabled = enabled;
                    }
                }
                UsbDeviceManager.this.mUsbAlsaManager.setAccessoryAudioState(enabled, card, device);
                UsbDeviceManager.this.mAudioSourceEnabled = enabled;
            }
        }

        private void updateMidiFunction() {
            FileNotFoundException e;
            UsbAlsaManager -get22;
            boolean z;
            Throwable th;
            boolean enabled = UsbManager.containsFunction(this.mCurrentFunctions, "midi");
            if (enabled != UsbDeviceManager.this.mMidiEnabled) {
                if (enabled) {
                    Scanner scanner = null;
                    try {
                        Scanner scanner2 = new Scanner(new File(UsbDeviceManager.MIDI_ALSA_PATH));
                        try {
                            UsbDeviceManager.this.mMidiCard = scanner2.nextInt();
                            UsbDeviceManager.this.mMidiDevice = scanner2.nextInt();
                            if (scanner2 != null) {
                                scanner2.close();
                            }
                        } catch (FileNotFoundException e2) {
                            e = e2;
                            scanner = scanner2;
                            try {
                                Slog.e(UsbDeviceManager.TAG, "could not open MIDI PCM file", e);
                                enabled = false;
                                if (scanner != null) {
                                    scanner.close();
                                }
                                UsbDeviceManager.this.mMidiEnabled = enabled;
                                -get22 = UsbDeviceManager.this.mUsbAlsaManager;
                                if (UsbDeviceManager.this.mMidiEnabled) {
                                    z = false;
                                } else {
                                    z = this.mConfigured;
                                }
                                -get22.setPeripheralMidiState(z, UsbDeviceManager.this.mMidiCard, UsbDeviceManager.this.mMidiDevice);
                            } catch (Throwable th2) {
                                th = th2;
                                if (scanner != null) {
                                    scanner.close();
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            scanner = scanner2;
                            if (scanner != null) {
                                scanner.close();
                            }
                            throw th;
                        }
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        Slog.e(UsbDeviceManager.TAG, "could not open MIDI PCM file", e);
                        enabled = false;
                        if (scanner != null) {
                            scanner.close();
                        }
                        UsbDeviceManager.this.mMidiEnabled = enabled;
                        -get22 = UsbDeviceManager.this.mUsbAlsaManager;
                        if (UsbDeviceManager.this.mMidiEnabled) {
                            z = this.mConfigured;
                        } else {
                            z = false;
                        }
                        -get22.setPeripheralMidiState(z, UsbDeviceManager.this.mMidiCard, UsbDeviceManager.this.mMidiDevice);
                    }
                }
                UsbDeviceManager.this.mMidiEnabled = enabled;
            }
            -get22 = UsbDeviceManager.this.mUsbAlsaManager;
            if (UsbDeviceManager.this.mMidiEnabled) {
                z = this.mConfigured;
            } else {
                z = false;
            }
            -get22.setPeripheralMidiState(z, UsbDeviceManager.this.mMidiCard, UsbDeviceManager.this.mMidiDevice);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            boolean z2;
            switch (msg.what) {
                case UsbDeviceManager.MSG_UPDATE_STATE /*0*/:
                    this.mConnected = msg.arg1 == UsbDeviceManager.MSG_ENABLE_ADB;
                    if (msg.arg2 == UsbDeviceManager.MSG_ENABLE_ADB) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    this.mConfigured = z2;
                    if (UsbDeviceManager.DEBUG) {
                        Slog.v(UsbDeviceManager.TAG, "message update state ");
                    }
                    if (!this.mConnected) {
                        this.mUsbDataUnlocked = false;
                        this.mMirrorlinkRequested = false;
                    }
                    updateUsbNotification();
                    if (!isChargingOnly_N()) {
                        updateAdbNotification();
                    }
                    if (UsbManager.containsFunction(this.mCurrentFunctions, "accessory")) {
                        updateCurrentAccessory();
                    } else if (!this.mConnected) {
                        UsbDeviceManager.this.mChargingOnlySelected = true;
                        setEnabledFunctions(null, false);
                    }
                    if (UsbDeviceManager.this.mSystemReady) {
                        updateUsbStateBroadcastIfNeeded();
                        updateUsbFunctions();
                    }
                case UsbDeviceManager.MSG_ENABLE_ADB /*1*/:
                    if (msg.arg1 != UsbDeviceManager.MSG_ENABLE_ADB) {
                        z = false;
                    }
                    setAdbEnabled(z);
                case UsbDeviceManager.MSG_SET_CURRENT_FUNCTIONS /*2*/:
                    String functions = msg.obj;
                    if (functions != null) {
                        UsbDeviceManager.this.mChargingOnlySelected = false;
                    } else {
                        UsbDeviceManager.this.mChargingOnlySelected = true;
                    }
                    if (!UsbDeviceManager.this.isDefaultFunction(functions) || functions == null) {
                        setEnabledFunctions(functions, false);
                    } else {
                        setEnabledFunctions(functions, false, true);
                    }
                case UsbDeviceManager.MSG_SYSTEM_READY /*3*/:
                    updateUsbNotification();
                    if (!isChargingOnly_N()) {
                        updateAdbNotification();
                    }
                    updateUsbStateBroadcastIfNeeded();
                    updateUsbFunctions();
                    UsbDeviceManager.this.mSystemReady = true;
                case UsbDeviceManager.MSG_BOOT_COMPLETED /*4*/:
                    UsbDeviceManager.this.mBootCompleted = true;
                    if (this.mCurrentAccessory != null) {
                        UsbDeviceManager.this.getCurrentSettings().accessoryAttached(this.mCurrentAccessory);
                    }
                    if (UsbDeviceManager.this.mDebuggingManager != null) {
                        UsbDeviceManager.this.mDebuggingManager.setAdbEnabled(UsbDeviceManager.this.mAdbEnabled);
                    }
                    if (UsbDeviceManager.this.mHDBManager != null) {
                        UsbDeviceManager.this.mHDBManager.setHdbEnabled(UsbDeviceManager.this.settingsHdbEnabled);
                    }
                case UsbDeviceManager.MSG_USER_SWITCHED /*5*/:
                    if (this.mCurrentUser != msg.arg1) {
                        boolean active;
                        if (UsbManager.containsFunction(this.mCurrentFunctions, "mtp")) {
                            active = true;
                        } else {
                            active = UsbManager.containsFunction(this.mCurrentFunctions, "ptp");
                        }
                        if (this.mUsbDataUnlocked && r0 && this.mCurrentUser != -10000) {
                            Slog.v(UsbDeviceManager.TAG, "Current user switched to " + this.mCurrentUser + "; resetting USB host stack for MTP or PTP");
                            this.mUsbDataUnlocked = false;
                            setEnabledFunctions(this.mCurrentFunctions, true);
                        }
                        this.mCurrentUser = msg.arg1;
                        if (UsbDeviceManager.this.mBootCompleted) {
                            updateUserBroadcast();
                        }
                    }
                case UsbDeviceManager.MSG_SET_USB_DATA_UNLOCKED /*6*/:
                    if (msg.arg1 != UsbDeviceManager.MSG_ENABLE_ADB) {
                        z = false;
                    }
                    setUsbDataUnlocked(z);
                case UsbDeviceManager.MSG_UPDATE_USER_RESTRICTIONS /*7*/:
                    setEnabledFunctions(this.mCurrentFunctions, false);
                case UsbDeviceManager.MSG_UPDATE_HOST_STATE /*8*/:
                    if (msg.arg1 == UsbDeviceManager.MSG_ENABLE_ADB) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    this.mHostConnected = z2;
                    if (msg.arg2 != UsbDeviceManager.MSG_ENABLE_ADB) {
                        z = false;
                    }
                    this.mSourcePower = z;
                    updateUsbNotification();
                    if (UsbDeviceManager.this.mBootCompleted) {
                        updateUsbStateBroadcastIfNeeded();
                    }
                case UsbDeviceManager.MSG_ENABLE_HDB /*9*/:
                    if (msg.arg1 != UsbDeviceManager.MSG_ENABLE_ADB) {
                        z = false;
                    }
                    setHdbEnabled(z);
                case UsbDeviceManager.MSG_SIM_COMPLETED /*10*/:
                    UsbDeviceManager.this.dueSimStatusCompletedMsg();
                case UsbDeviceManager.MSG_MIRRORLINK_REQUESTED /*11*/:
                    this.mMirrorlinkRequested = true;
                    if (UsbDeviceManager.this.mBootCompleted) {
                        updateUsbStateBroadcastIfNeeded();
                    }
                    this.mMirrorlinkRequested = false;
                case UsbDeviceManager.MSG_ENABLE_ALLOWCHARGINGADB /*12*/:
                    if (msg.arg1 != UsbDeviceManager.MSG_ENABLE_ADB) {
                        z = false;
                    }
                    updateUsbState(z);
                default:
            }
        }

        public UsbAccessory getCurrentAccessory() {
            return this.mCurrentAccessory;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void updateUsbNotification() {
            if (UsbDeviceManager.this.mNotificationManager != null && UsbDeviceManager.this.mUseUsbNotification && !"0".equals(SystemProperties.get("persist.charging.notify"))) {
                int id = UsbDeviceManager.MSG_UPDATE_STATE;
                if (UsbDeviceManager.DEBUG) {
                    Slog.v(UsbDeviceManager.TAG, "update usb notification - mConnetced = " + this.mConnected);
                }
                Resources r = UsbDeviceManager.this.mContext.getResources();
                if (this.mConnected) {
                    if (this.mUsbDataUnlocked) {
                        if (!(UsbManager.containsFunction(this.mCurrentFunctions, "mtp") || UsbManager.containsFunction(this.mCurrentFunctions, "ptp") || UsbManager.containsFunction(this.mCurrentFunctions, "midi") || !UsbManager.containsFunction(this.mCurrentFunctions, "accessory"))) {
                            id = 17040388;
                        }
                    }
                }
                if (id != this.mUsbNotificationId) {
                    if (this.mUsbNotificationId != 0) {
                        UsbDeviceManager.this.mNotificationManager.cancelAsUser(null, this.mUsbNotificationId, UserHandle.ALL);
                        this.mUsbNotificationId = UsbDeviceManager.MSG_UPDATE_STATE;
                    }
                    if (id != 0) {
                        CharSequence message = r.getText(17040389);
                        CharSequence title = r.getText(id);
                        UsbDeviceManager.this.mNotificationManager.notifyAsUser(null, id, new Builder(UsbDeviceManager.this.mContext).setSmallIcon(17303219).setWhen(0).setOngoing(true).setTicker(title).setDefaults(UsbDeviceManager.MSG_UPDATE_STATE).setPriority(-2).setColor(UsbDeviceManager.this.mContext.getColor(17170519)).setContentTitle(title).setContentText(message).setVisibility(UsbDeviceManager.MSG_ENABLE_ADB).build(), UserHandle.ALL);
                        this.mUsbNotificationId = id;
                    }
                }
            }
        }

        private void updateAdbNotification() {
            if (UsbDeviceManager.this.mNotificationManager != null) {
                if (UsbDeviceManager.this.mAdbEnabled && this.mConnected && !"none".equals(SystemProperties.get(UsbDeviceManager.USB_PERSISTENT_CONFIG_PROPERTY))) {
                    if (!("0".equals(SystemProperties.get("persist.adb.notify")) || this.mAdbNotificationShown)) {
                        if (UsbDeviceManager.DEBUG) {
                            Slog.v(UsbDeviceManager.TAG, "update adb notification");
                        }
                        Resources r = UsbDeviceManager.this.mContext.getResources();
                        CharSequence title = r.getText(17040390);
                        CharSequence message = r.getText(33685795);
                        Notification notification = new Builder(UsbDeviceManager.this.mContext).setSmallIcon(33751155).setWhen(0).setOngoing(true).setTicker(title).setDefaults(UsbDeviceManager.MSG_UPDATE_STATE).setPriority(UsbDeviceManager.MSG_UPDATE_STATE).setContentTitle(title).setContentText(message).setContentIntent(PendingIntent.getActivityAsUser(UsbDeviceManager.this.mContext, UsbDeviceManager.MSG_UPDATE_STATE, Intent.makeRestartActivityTask(new ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings")), UsbDeviceManager.MSG_UPDATE_STATE, null, UserHandle.CURRENT)).setVisibility(UsbDeviceManager.MSG_ENABLE_ADB).build();
                        this.mAdbNotificationShown = true;
                        UsbDeviceManager.this.mNotificationManager.notifyAsUser(null, 17040390, notification, UserHandle.ALL);
                    }
                } else if (this.mAdbNotificationShown) {
                    if (UsbDeviceManager.DEBUG) {
                        Slog.v(UsbDeviceManager.TAG, "cancel adb notification");
                    }
                    this.mAdbNotificationShown = false;
                    UsbDeviceManager.this.mNotificationManager.cancelAsUser(null, 17040390, UserHandle.ALL);
                }
            }
        }

        private String getDefaultFunctions() {
            String func = SystemProperties.get(UsbDeviceManager.USB_PERSISTENT_CONFIG_PROPERTY, "none");
            if ("none".equals(func)) {
                return "mtp";
            }
            return func;
        }

        public void dump(IndentingPrintWriter pw) {
            pw.println("USB Device State:");
            pw.println("  mCurrentFunctions: " + this.mCurrentFunctions);
            pw.println("  mCurrentFunctionsApplied: " + this.mCurrentFunctionsApplied);
            pw.println("  mConnected: " + this.mConnected);
            pw.println("  mConfigured: " + this.mConfigured);
            pw.println("  mUsbDataUnlocked: " + this.mUsbDataUnlocked);
            pw.println("  mCurrentAccessory: " + this.mCurrentAccessory);
            try {
                pw.println("  Kernel state: " + FileUtils.readTextFile(new File(UsbDeviceManager.STATE_PATH), UsbDeviceManager.MSG_UPDATE_STATE, null).trim());
                pw.println("  Kernel function list: " + FileUtils.readTextFile(new File(UsbDeviceManager.FUNCTIONS_PATH), UsbDeviceManager.MSG_UPDATE_STATE, null).trim());
            } catch (IOException e) {
                pw.println("IOException: " + e);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.usb.UsbDeviceManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.usb.UsbDeviceManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.usb.UsbDeviceManager.<clinit>():void");
    }

    private native String[] nativeGetAccessoryStrings();

    private native int nativeGetAudioMode();

    private native boolean nativeIsStartRequested();

    private native ParcelFileDescriptor nativeOpenAccessory();

    public UsbDeviceManager(Context context, UsbAlsaManager alsaManager) {
        this.mAccessoryModeRequestTime = 0;
        this.ALLOW_CHARGING_ADB = "allow_charging_adb";
        this.mChargingOnlySelected = true;
        this.mLock = new Object();
        this.mUEventObserver = new UEventObserver() {
            public void onUEvent(UEvent event) {
                Flog.i(1306, "UsbDeviceManagerUSB UEVENT: " + event.toString());
                String mirrorlink = event.get("MIRRORLINK");
                String state = event.get("USB_STATE");
                String accessory = event.get("ACCESSORY");
                if ("REQUESTED".equals(mirrorlink)) {
                    UsbDeviceManager.this.mHandler.sendMessage((int) UsbDeviceManager.MSG_MIRRORLINK_REQUESTED, true);
                }
                if (state != null) {
                    UsbDeviceManager.this.mHandler.updateState(state);
                } else if ("START".equals(accessory)) {
                    if (UsbDeviceManager.DEBUG) {
                        Slog.d(UsbDeviceManager.TAG, "got accessory start");
                    }
                    UsbDeviceManager.this.startAccessoryMode();
                }
            }
        };
        this.mPowerSupplyObserver = new UEventObserver() {
            public void onUEvent(UEvent event) {
                try {
                    if ("normal".equals(SystemProperties.get("ro.runmode", "normal"))) {
                        String state = FileUtils.readTextFile(new File(UsbDeviceManager.CHARGE_WATER_INSTRUSED_TYPE_PATH), UsbDeviceManager.MSG_UPDATE_STATE, null).trim();
                        Slog.i(UsbDeviceManager.TAG, "water_intrused state= " + state);
                        if (state.equals("1")) {
                            UsbDeviceManager.this.usbWaterInNotification(true);
                        }
                    }
                } catch (IOException e) {
                    Slog.e(UsbDeviceManager.TAG, "Error reading charge file", e);
                }
            }
        };
        this.mHostReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                UsbDeviceManager.this.mHandler.updateHostState((UsbPort) intent.getParcelableExtra("port"), (UsbPortStatus) intent.getParcelableExtra("portStatus"));
            }
        };
        this.mContext = context;
        this.mUsbAlsaManager = alsaManager;
        this.mContentResolver = context.getContentResolver();
        this.mHasUsbAccessory = this.mContext.getPackageManager().hasSystemFeature("android.hardware.usb.accessory");
        initRndisAddress();
        readOemUsbOverrideConfig();
        this.mHdbEnabled = SystemProperties.get("persist.service.hdb.enable", "false").equals("true");
        this.mHandler = new UsbHandler(FgThread.get().getLooper());
        if (nativeIsStartRequested()) {
            if (DEBUG) {
                Slog.d(TAG, "accessory attached at boot");
            }
            startAccessoryMode();
        }
        boolean secureAdbEnabled = SystemProperties.getBoolean("ro.adb.secure", false);
        boolean dataEncrypted = "1".equals(SystemProperties.get("vold.decrypt"));
        if (secureAdbEnabled && !dataEncrypted) {
            this.mDebuggingManager = new UsbDebuggingManager(context);
        }
        this.mContext.registerReceiver(this.mHostReceiver, new IntentFilter("android.hardware.usb.action.USB_PORT_CHANGED"));
        if (secureAdbEnabled && this.mHdbEnabled) {
            this.mHDBManager = HwServiceFactory.getHwUsbHDBManager(context);
        }
    }

    private UsbSettingsManager getCurrentSettings() {
        UsbSettingsManager usbSettingsManager;
        synchronized (this.mLock) {
            usbSettingsManager = this.mCurrentSettings;
        }
        return usbSettingsManager;
    }

    public void systemReady() {
        boolean massStorageSupported;
        boolean z;
        int i = MSG_UPDATE_STATE;
        if (DEBUG) {
            Slog.d(TAG, "systemReady");
        }
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        StorageVolume primary = StorageManager.from(this.mContext).getPrimaryVolume();
        if (primary != null) {
            massStorageSupported = primary.allowMassStorage();
        } else {
            massStorageSupported = false;
        }
        if (massStorageSupported) {
            z = false;
        } else {
            z = this.mContext.getResources().getBoolean(17956903);
        }
        this.mUseUsbNotification = z;
        if (SystemProperties.getInt("ro.debuggable", MSG_UPDATE_STATE) == MSG_ENABLE_ADB) {
            Global.putInt(this.mContentResolver, this.ALLOW_CHARGING_ADB, MSG_ENABLE_ADB);
            this.mAdbEnabled = true;
        }
        try {
            Flog.i(1306, "UsbDeviceManager make sure ADB_ENABLED setting value when systemReady, mAdbEnabled is " + this.mAdbEnabled);
            ContentResolver contentResolver = this.mContentResolver;
            String str = "adb_enabled";
            if (this.mAdbEnabled) {
                i = MSG_ENABLE_ADB;
            }
            Global.putInt(contentResolver, str, i);
        } catch (SecurityException e) {
            Flog.w(1306, "UsbDeviceManager ADB_ENABLED is restricted.");
        }
        if (SystemProperties.get("persist.service.hdb.enable", "false").equals("true") && System.getInt(this.mContentResolver, "hdb_enabled", -1) < 0) {
            if (UsbManager.containsFunction(SystemProperties.get("ro.default.userportmode", "null"), "hdb")) {
                try {
                    Flog.i(1306, "UsbDeviceManager ro.default.userportmode:" + SystemProperties.get("ro.default.userportmode", "null"));
                    System.putInt(this.mContentResolver, "hdb_enabled", MSG_ENABLE_ADB);
                } catch (Exception e2) {
                    Flog.e(1306, "UsbDeviceManager set KEY_CONTENT_HDB_ALLOWED failed: " + e2);
                }
            } else if (SystemProperties.get("ro.product.locale.region", "null").equals("CN")) {
                try {
                    Flog.i(1306, "UsbDeviceManager ro.product.locale.region:" + SystemProperties.get("ro.product.locale.region", "null"));
                    System.putInt(this.mContentResolver, "hdb_enabled", MSG_ENABLE_ADB);
                } catch (Exception e22) {
                    Flog.w(1306, "UsbDeviceManager set KEY_CONTENT_HDB_ALLOWED failed: " + e22);
                }
            } else {
                try {
                    Flog.i(1306, "UsbDeviceManager System.KEY_CONTENT_HDB_ALLOWED : 0");
                    System.putInt(this.mContentResolver, "hdb_enabled", MSG_UPDATE_STATE);
                } catch (Exception e222) {
                    Flog.w(1306, "UsbDeviceManager set KEY_CONTENT_HDB_ALLOWED failed: " + e222);
                }
            }
        }
        Slog.i(TAG, "send message for ready to delay 1 second");
        this.mHandler.sendEmptyMessageDelayed(MSG_SYSTEM_READY, 1000);
    }

    public void bootCompleted() {
        if (DEBUG) {
            Slog.d(TAG, "boot completed");
        }
        this.mHandler.sendEmptyMessage(MSG_BOOT_COMPLETED);
    }

    public void setCurrentUser(int userId, UsbSettingsManager settings) {
        synchronized (this.mLock) {
            this.mCurrentSettings = settings;
            this.mHandler.obtainMessage(MSG_USER_SWITCHED, userId, MSG_UPDATE_STATE).sendToTarget();
        }
    }

    public void updateUserRestrictions() {
        this.mHandler.sendEmptyMessage(MSG_UPDATE_USER_RESTRICTIONS);
    }

    private void startAccessoryMode() {
        boolean z = true;
        if (this.mHasUsbAccessory) {
            boolean enableAccessory;
            this.mAccessoryStrings = nativeGetAccessoryStrings();
            boolean enableAudio = nativeGetAudioMode() == MSG_ENABLE_ADB;
            if (this.mAccessoryStrings == null || this.mAccessoryStrings[MSG_UPDATE_STATE] == null) {
                enableAccessory = false;
            } else {
                if (this.mAccessoryStrings[MSG_ENABLE_ADB] == null) {
                    z = MSG_UPDATE_STATE;
                }
                enableAccessory = z;
            }
            String functions = null;
            if (enableAccessory && enableAudio) {
                functions = "accessory,audio_source";
            } else if (enableAccessory) {
                functions = "accessory";
            } else if (enableAudio) {
                functions = "audio_source";
            }
            if (functions != null) {
                this.mAccessoryModeRequestTime = SystemClock.elapsedRealtime();
                setCurrentFunctions(functions);
            }
        }
    }

    private static void initRndisAddress() {
        int[] address = new int[MSG_SET_USB_DATA_UNLOCKED];
        address[MSG_UPDATE_STATE] = MSG_SET_CURRENT_FUNCTIONS;
        String serial = SystemProperties.get("ro.serialno", "1234567890ABCDEF");
        int serialLength = serial.length();
        for (int i = MSG_UPDATE_STATE; i < serialLength; i += MSG_ENABLE_ADB) {
            int i2 = (i % MSG_USER_SWITCHED) + MSG_ENABLE_ADB;
            address[i2] = address[i2] ^ serial.charAt(i);
        }
        Object[] objArr = new Object[MSG_SET_USB_DATA_UNLOCKED];
        objArr[MSG_UPDATE_STATE] = Integer.valueOf(address[MSG_UPDATE_STATE]);
        objArr[MSG_ENABLE_ADB] = Integer.valueOf(address[MSG_ENABLE_ADB]);
        objArr[MSG_SET_CURRENT_FUNCTIONS] = Integer.valueOf(address[MSG_SET_CURRENT_FUNCTIONS]);
        objArr[MSG_SYSTEM_READY] = Integer.valueOf(address[MSG_SYSTEM_READY]);
        objArr[MSG_BOOT_COMPLETED] = Integer.valueOf(address[MSG_BOOT_COMPLETED]);
        objArr[MSG_USER_SWITCHED] = Integer.valueOf(address[MSG_USER_SWITCHED]);
        try {
            FileUtils.stringToFile(RNDIS_ETH_ADDR_PATH, String.format(Locale.US, "%02X:%02X:%02X:%02X:%02X:%02X", objArr));
        } catch (IOException e) {
            Slog.e(TAG, "failed to write to /sys/class/android_usb/android0/f_rndis/ethaddr");
        }
    }

    private boolean HdbIsEnableFunction(String functions) {
        if (isCmccUsbLimit()) {
            Slog.i(TAG, "cmcc_usb_limit do not set hdb");
            return false;
        } else if ((functions.equals("mtp") || functions.equals("mtp,adb") || functions.equals("ptp") || functions.equals("ptp,adb") || functions.equals("hisuite,mtp,mass_storage") || functions.equals("hisuite,mtp,mass_storage,adb") || functions.equals("bicr") || functions.equals("bicr,adb") || functions.equals("rndis") || functions.equals("rndis,adb")) && this.settingsHdbEnabled) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isDefaultFunction(String functions) {
        if (functions == null) {
            return false;
        }
        if (functions.equals("hisuite,mtp,mass_storage") || functions.equals("mtp")) {
            return true;
        }
        return false;
    }

    public void writeSuitestate() {
        OutputStreamWriter osw;
        IOException ex;
        Throwable th;
        OutputStreamWriter outputStreamWriter = null;
        FileOutputStream fos = null;
        try {
            File newfile = new File(SUITE_STATE_PATH, SUITE_STATE_FILE);
            if (newfile.exists()) {
                FileOutputStream fos2 = new FileOutputStream(newfile);
                try {
                    osw = new OutputStreamWriter(fos2, "UTF-8");
                } catch (IOException e) {
                    ex = e;
                    fos = fos2;
                    try {
                        Slog.e(TAG, "IOException in writeCommand hisuite", ex);
                        if (outputStreamWriter != null) {
                            try {
                                outputStreamWriter.close();
                            } catch (IOException e2) {
                                Slog.e(TAG, "IOException in close fw");
                            }
                        }
                        if (fos == null) {
                            try {
                                fos.close();
                            } catch (IOException e3) {
                                Slog.e(TAG, "IOException in close fos");
                                return;
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (outputStreamWriter != null) {
                            try {
                                outputStreamWriter.close();
                            } catch (IOException e4) {
                                Slog.e(TAG, "IOException in close fw");
                            }
                        }
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e5) {
                                Slog.e(TAG, "IOException in close fos");
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fos = fos2;
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                    throw th;
                }
                try {
                    osw.write("0");
                    osw.flush();
                    fos = fos2;
                    outputStreamWriter = osw;
                } catch (IOException e6) {
                    ex = e6;
                    fos = fos2;
                    outputStreamWriter = osw;
                    Slog.e(TAG, "IOException in writeCommand hisuite", ex);
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                    if (fos == null) {
                        fos.close();
                    }
                } catch (Throwable th4) {
                    th = th4;
                    fos = fos2;
                    outputStreamWriter = osw;
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                    throw th;
                }
            }
            if (outputStreamWriter != null) {
                try {
                    outputStreamWriter.close();
                } catch (IOException e7) {
                    Slog.e(TAG, "IOException in close fw");
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e8) {
                    Slog.e(TAG, "IOException in close fos");
                }
            }
        } catch (IOException e9) {
            ex = e9;
            Slog.e(TAG, "IOException in writeCommand hisuite", ex);
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
            if (fos == null) {
                fos.close();
            }
        }
    }

    public UsbAccessory getCurrentAccessory() {
        return this.mHandler.getCurrentAccessory();
    }

    public ParcelFileDescriptor openAccessory(UsbAccessory accessory) {
        UsbAccessory currentAccessory = this.mHandler.getCurrentAccessory();
        if (currentAccessory == null) {
            throw new IllegalArgumentException("no accessory attached");
        } else if (currentAccessory.equals(accessory)) {
            getCurrentSettings().checkPermission(accessory);
            return nativeOpenAccessory();
        } else {
            throw new IllegalArgumentException(accessory.toString() + " does not match current accessory " + currentAccessory);
        }
    }

    public boolean isFunctionEnabled(String function) {
        return UsbManager.containsFunction(SystemProperties.get(USB_CONFIG_PROPERTY), function);
    }

    public void setCurrentFunctions(String functions) {
        Flog.i(1306, "UsbDeviceManager setCurrentFunctions(" + functions + ")");
        this.mHandler.sendMessage((int) MSG_SET_CURRENT_FUNCTIONS, (Object) functions);
    }

    public void setUsbDataUnlocked(boolean unlocked) {
        Flog.i(1306, "UsbDeviceManager setUsbDataUnlocked(" + unlocked + ")");
        this.mHandler.sendMessage((int) MSG_SET_USB_DATA_UNLOCKED, unlocked);
    }

    private void readOemUsbOverrideConfig() {
        String[] configList = this.mContext.getResources().getStringArray(17236018);
        if (configList != null) {
            int length = configList.length;
            for (int i = MSG_UPDATE_STATE; i < length; i += MSG_ENABLE_ADB) {
                String[] items = configList[i].split(":");
                if (items.length == MSG_SYSTEM_READY) {
                    if (this.mOemModeMap == null) {
                        this.mOemModeMap = new HashMap();
                    }
                    List<Pair<String, String>> overrideList = (List) this.mOemModeMap.get(items[MSG_UPDATE_STATE]);
                    if (overrideList == null) {
                        overrideList = new LinkedList();
                        this.mOemModeMap.put(items[MSG_UPDATE_STATE], overrideList);
                    }
                    overrideList.add(new Pair(items[MSG_ENABLE_ADB], items[MSG_SET_CURRENT_FUNCTIONS]));
                }
            }
        }
    }

    private String applyOemOverrideFunction(String usbFunctions) {
        if (usbFunctions == null || this.mOemModeMap == null) {
            return usbFunctions;
        }
        List<Pair<String, String>> overrides = (List) this.mOemModeMap.get(SystemProperties.get(BOOT_MODE_PROPERTY, "unknown"));
        if (overrides != null) {
            for (Pair<String, String> pair : overrides) {
                if (((String) pair.first).equals(usbFunctions)) {
                    Slog.d(TAG, "OEM USB override: " + ((String) pair.first) + " ==> " + ((String) pair.second));
                    return (String) pair.second;
                }
            }
        }
        return usbFunctions;
    }

    public void allowUsbDebugging(boolean alwaysAllow, String publicKey) {
        if (this.mDebuggingManager != null) {
            Flog.i(1306, "UsbDeviceManager allowUsbDebugging...");
            this.mDebuggingManager.allowUsbDebugging(alwaysAllow, publicKey);
        }
    }

    public void denyUsbDebugging() {
        if (this.mDebuggingManager != null) {
            Flog.i(1306, "UsbDeviceManager denyUsbDebugging...");
            this.mDebuggingManager.denyUsbDebugging();
        }
    }

    public void clearUsbDebuggingKeys() {
        if (this.mDebuggingManager != null) {
            this.mDebuggingManager.clearUsbDebuggingKeys();
            return;
        }
        throw new RuntimeException("Cannot clear Usb Debugging keys, UsbDebuggingManager not enabled");
    }

    public void dump(IndentingPrintWriter pw) {
        if (this.mHandler != null) {
            this.mHandler.dump(pw);
        }
        if (this.mDebuggingManager != null) {
            this.mDebuggingManager.dump(pw);
        }
        if (this.mHDBManager != null) {
            this.mHDBManager.dump(pw);
        }
    }

    protected boolean getUsbHandlerConnected() {
        if (this.mHandler != null) {
            return this.mHandler.mConnected;
        }
        return false;
    }

    protected boolean sendHandlerEmptyMessage(int what) {
        if (this.mHandler != null) {
            return this.mHandler.sendEmptyMessage(what);
        }
        return false;
    }

    protected Context getContext() {
        return this.mContext;
    }

    protected boolean containsFunctionOuter(String functions, String function) {
        return UsbManager.containsFunction(functions, function);
    }

    protected void setEnabledFunctionsEx(String functions, boolean forceRestart) {
        if (this.mHandler != null) {
            this.mHandler.setEnabledFunctions(functions, forceRestart);
        }
    }

    protected String removeAdbFunction(String functions, String function) {
        return UsbManager.removeFunction(functions, function);
    }

    protected boolean setUsbConfigEx(String config) {
        if (this.mHandler != null) {
            return this.mHandler.setUsbConfig(config);
        }
        return false;
    }
}
