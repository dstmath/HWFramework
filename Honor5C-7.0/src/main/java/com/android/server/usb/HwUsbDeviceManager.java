package com.android.server.usb;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hdm.HwDeviceManager;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.MediaStore.Audio.Media;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import com.android.server.PPPOEStateMachine;
import com.android.server.ServiceThread;

public class HwUsbDeviceManager extends UsbDeviceManager {
    private static boolean DEBUG = false;
    private static final String FACTORY_VERSION = "factory";
    private static final String PERSIST_CMCC_USB_LIMIT = "persist.sys.cmcc_usb_limit";
    private static final String RUN_MODE_PROPERTY = "ro.runmode";
    private static final String SYS_CMCC_USB_LIMIT = "cmcc_usb_limit";
    private static final String TAG;
    private static final String WATER_WARNING_RINGTONE = "WaterWarning.ogg";
    private AudioAttributes mAudioAttributes;
    private Context mContext;
    private final Handler mHwUsbDeviceManagerHandler;
    private final HandlerThread mHwUsbDeviceManagerThread;
    private boolean mIsShowingDialog;
    private Ringtone mRingRingtone;
    private final BroadcastReceiver mSimStatusCompletedReceiver;
    private Uri mUri;
    private AlertDialog mWarningDialog;

    static {
        TAG = HwUsbDeviceManager.class.getSimpleName();
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public HwUsbDeviceManager(Context context, UsbAlsaManager alsaManager) {
        super(context, alsaManager);
        this.mWarningDialog = null;
        this.mIsShowingDialog = false;
        this.mSimStatusCompletedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (HwUsbDeviceManager.DEBUG) {
                    Slog.d(HwUsbDeviceManager.TAG, "sim status completed");
                }
                HwUsbDeviceManager.this.sendHandlerEmptyMessage(10);
            }
        };
        setCMCCUsbLimit();
        setUsbConfig();
        registerSimStatusCompletedReceiver();
        this.mContext = context;
        this.mHwUsbDeviceManagerThread = new ServiceThread(TAG, -4, false);
        this.mHwUsbDeviceManagerThread.start();
        this.mHwUsbDeviceManagerHandler = new Handler(this.mHwUsbDeviceManagerThread.getLooper());
        this.mAudioAttributes = new Builder().setUsage(13).setContentType(4).build();
    }

    private String getCmccUsbLimit() {
        return SystemProperties.get(PERSIST_CMCC_USB_LIMIT, PPPOEStateMachine.PHASE_DEAD);
    }

    private String getDebuggleMode() {
        return SystemProperties.get("ro.debuggable", PPPOEStateMachine.PHASE_DEAD);
    }

    private void setCMCCUsbLimit() {
        String ro_debuggable = getDebuggleMode();
        String usb_limit = getCmccUsbLimit();
        if (DEBUG) {
            Slog.i(TAG, "setCMCCUsbLimit ro_debuggable " + ro_debuggable + " usb_limit " + usb_limit);
        }
        if (PPPOEStateMachine.PHASE_INITIALIZE.equals(ro_debuggable) && PPPOEStateMachine.PHASE_INITIALIZE.equals(usb_limit)) {
            SystemProperties.set(PERSIST_CMCC_USB_LIMIT, PPPOEStateMachine.PHASE_DEAD);
            if (DEBUG) {
                Slog.i(TAG, "UsbDeviceManager new init in debug mode set  to 0 !");
            }
        }
    }

    private void setUsbConfig() {
        String cur_usb_config = SystemProperties.get("persist.sys.usb.config", "adb");
        String usb_limit = getCmccUsbLimit();
        if (DEBUG) {
            Slog.i(TAG, "setUsbConfig cur_usb_config " + cur_usb_config + " usb_limit " + usb_limit);
        }
        if (PPPOEStateMachine.PHASE_INITIALIZE.equals(usb_limit) && !containsFunctionOuter(cur_usb_config, "manufacture")) {
            boolean result = setUsbConfigEx("mass_storage");
            if (DEBUG) {
                Slog.i(TAG, "UsbDeviceManager new init setusbconfig result: " + result);
            }
        }
    }

    protected void registerSimStatusCompletedReceiver() {
        if (DEBUG) {
            Slog.d(TAG, "registerSimStatusCompletedReceiver");
        }
        if (getContext() != null) {
            getContext().registerReceiver(this.mSimStatusCompletedReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        }
    }

    protected void dueSimStatusCompletedMsg() {
        String usb_limit = getCmccUsbLimit();
        if (DEBUG) {
            Slog.i(TAG, "simcardstate at receive sim_status_change usb_limit = " + usb_limit);
        }
        if (!PPPOEStateMachine.PHASE_DEAD.equals(usb_limit)) {
            int simcardstate = 0;
            try {
                if (getContext() != null) {
                    simcardstate = ((TelephonyManager) getContext().getSystemService("phone")).getSimState();
                }
                if (DEBUG) {
                    Slog.i(TAG, "simcardstate at boot completed is " + simcardstate);
                }
                if (!(simcardstate == 0 || simcardstate == 1 || simcardstate == 8 || simcardstate == 6)) {
                    Slog.i(TAG, "persist.sys.cmcc_usb_limit to 0 ");
                    SystemProperties.set(PERSIST_CMCC_USB_LIMIT, PPPOEStateMachine.PHASE_DEAD);
                    setEnabledFunctionsEx("hisuite,mtp,mass_storage", true);
                    if (getContext() != null && getUsbHandlerConnected()) {
                        if (DEBUG) {
                            Slog.i(TAG, "Secure SYS_CMCC_USB_LIMIT 0 ");
                        }
                        Secure.putInt(getContext().getContentResolver(), SYS_CMCC_USB_LIMIT, 0);
                    }
                }
            } catch (Exception e) {
                Slog.i(TAG, "dueSimStatusCompletedMsg error", e);
            }
        }
    }

    protected boolean interceptSetEnabledFunctions(String functions) {
        boolean isManufacturePort = false;
        String usb_limit = getCmccUsbLimit();
        if (DEBUG) {
            Slog.i(TAG, "interceptSetEnabledFunctions  functions:" + functions);
        }
        if (PPPOEStateMachine.PHASE_DEAD.equals(usb_limit)) {
            return false;
        }
        if (functions != null) {
            isManufacturePort = containsFunctionOuter(functions, "manufacture");
        }
        int simcardstate = 0;
        try {
            if (getContext() != null) {
                simcardstate = ((TelephonyManager) getContext().getSystemService("phone")).getSimState();
            }
            if (DEBUG) {
                Slog.i(TAG, "interceptSetEnabledFunctions simcardstate = " + simcardstate + " IsManufacturePort:" + isManufacturePort);
            }
            if (!(simcardstate == 0 || simcardstate == 1 || simcardstate == 8 || simcardstate == 6)) {
                if (DEBUG) {
                    Slog.i(TAG, "persist.sys.cmcc_usb_limit set to 0 at setenable  ");
                }
                SystemProperties.set(PERSIST_CMCC_USB_LIMIT, PPPOEStateMachine.PHASE_DEAD);
                usb_limit = PPPOEStateMachine.PHASE_DEAD;
            }
        } catch (Exception e) {
            Slog.i(TAG, "interceptSetEnabledFunctions error", e);
        }
        if (!PPPOEStateMachine.PHASE_INITIALIZE.equals(usb_limit) || isManufacturePort) {
            return false;
        }
        if (DEBUG) {
            Slog.i(TAG, "cmcc usb_limit return !");
        }
        return true;
    }

    protected boolean isCmccUsbLimit() {
        if (PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get(PERSIST_CMCC_USB_LIMIT, PPPOEStateMachine.PHASE_DEAD))) {
            return true;
        }
        return false;
    }

    protected String removeAdbFunction(String functions, String function) {
        if (containsFunctionOuter(functions, "manufacture")) {
            return functions;
        }
        return super.removeAdbFunction(functions, function);
    }

    protected boolean isAdbDisabled() {
        if (!HwDeviceManager.disallowOp(11)) {
            return false;
        }
        Global.putInt(this.mContentResolver, "adb_enabled", 0);
        return true;
    }

    protected void usbWaterInNotification(boolean enable) {
        Slog.i(TAG, "usbWaterInNotification");
        if (!this.mIsShowingDialog) {
            this.mIsShowingDialog = true;
            if (enable) {
                playRing();
            } else {
                stopRing();
            }
            Message msg = Message.obtain(this.mHwUsbDeviceManagerHandler, new Runnable() {
                public void run() {
                    HwUsbDeviceManager.this.createWarningDialog();
                }
            });
            msg.setAsynchronous(true);
            this.mHwUsbDeviceManagerHandler.sendMessage(msg);
        }
    }

    protected void playRing() {
        this.mHwUsbDeviceManagerHandler.post(new Runnable() {
            public void run() {
                HwUsbDeviceManager.this.mUri = HwUsbDeviceManager.queryRingMusicUri(HwUsbDeviceManager.this.mContext, HwUsbDeviceManager.WATER_WARNING_RINGTONE);
                HwUsbDeviceManager.this.mRingRingtone = HwUsbDeviceManager.this.playRing(HwUsbDeviceManager.this.mUri);
            }
        });
    }

    protected void stopRing() {
        this.mHwUsbDeviceManagerHandler.post(new Runnable() {
            public void run() {
                HwUsbDeviceManager.this.stopRing(HwUsbDeviceManager.this.mRingRingtone);
            }
        });
    }

    private Ringtone playRing(Uri uri) {
        Ringtone ringtone = RingtoneManager.getRingtone(this.mContext, uri);
        if (ringtone != null) {
            ringtone.setAudioAttributes(this.mAudioAttributes);
            ringtone.play();
        }
        return ringtone;
    }

    private void stopRing(Ringtone ringtone) {
        if (ringtone != null) {
            ringtone.stop();
        }
    }

    private static Uri queryRingMusicUri(Context context, String fileName) {
        return queryRingMusicUri(context.getContentResolver(), fileName);
    }

    private static Uri queryRingMusicUri(ContentResolver resolver, String fileName) {
        if (fileName == null) {
            return null;
        }
        Uri uri = Media.INTERNAL_CONTENT_URI;
        String[] cols = new String[]{"_id"};
        StringBuilder where = new StringBuilder("_data like '%");
        where.append(fileName).append("'");
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, cols, where.toString(), null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            Uri withAppendedId = ContentUris.withAppendedId(uri, (long) cursor.getInt(cursor.getColumnIndex("_id")));
            if (cursor != null) {
                cursor.close();
            }
            return withAppendedId;
        } catch (Exception e) {
            Slog.e(TAG, "queryRingMusicUri query database exception: " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void createWarningDialog() {
        this.mWarningDialog = new AlertDialog.Builder(this.mContext, 33947691).setTitle(33685891).setMessage(33685892).setIcon(17301543).setPositiveButton(33685893, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Slog.i(HwUsbDeviceManager.TAG, "dialog click, dismiss and stopRing");
                dialog.dismiss();
                HwUsbDeviceManager.this.stopRing();
                HwUsbDeviceManager.this.mIsShowingDialog = false;
            }
        }).create();
        this.mWarningDialog.getWindow().setType(2009);
        this.mWarningDialog.show();
        this.mWarningDialog.setCancelable(false);
        Slog.i(TAG, "createWarningDialog");
    }
}
