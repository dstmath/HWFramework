package com.android.server.adb;

import android.content.Context;
import android.database.ContentObserver;
import android.debug.HdbManagerInternal;
import android.debug.IHdbTransport;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Flog;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.database.ContentObserverEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.android.util.SlogEx;
import huawei.android.hardware.usb.HwUsbManagerEx;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class HwAdbService extends AdbServiceEx {
    private static final String ALLOW_CHARGING_ADB = "allow_charging_adb";
    private static final String KEY_CONTENT_SUITESTATE = "suitestate";
    private static final int REPAIR_MODE_USER_ID = 127;
    private static final String SUITE_STATE_FILE = "android_usb/f_mass_storage/suitestate";
    private static final String SUITE_STATE_PATH = "/sys/class";
    private static final String TAG = "HwAdbService";
    private final ArrayMap<IBinder, IHdbTransport> mHdbTransports = new ArrayMap<>();
    private boolean mIsChargingOnlySelected = true;
    private boolean mIsHdbEnabled;

    public HwAdbService(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    public void onInitHandle() {
        try {
            if (SystemPropertiesEx.getInt("ro.debuggable", 0) == 1) {
                Flog.i(1306, "HwAdbServicedevice is root, enable adb");
                setAdbEnabled(true);
                Settings.Global.putInt(getContentResolver(), ALLOW_CHARGING_ADB, 1);
            } else if (SystemPropertiesEx.get("ro.product.custom", "NULL").contains("docomo")) {
                Settings.Global.putInt(getContentResolver(), ALLOW_CHARGING_ADB, 1);
            }
        } catch (SecurityException e) {
            SlogEx.e(TAG, "SecurityException happened in initializing onInitHandle");
        } catch (Exception e2) {
            SlogEx.e(TAG, "Exception happened in initializing onInitHandle");
        }
    }

    public void systemReady() {
        if (SystemPropertiesEx.get("persist.service.hdb.enable", "false").equals("true")) {
            LocalServicesExt.addService(HdbManagerInternal.class, new HdbManagerInternalImpl());
            this.mIsHdbEnabled = Settings.System.getInt(getContentResolver(), "hdb_enabled", 0) > 0;
            SlogEx.i(TAG, "device support hdb feature, mIsHdbEnabled:" + this.mIsHdbEnabled);
            getContentResolver().registerContentObserver(Settings.System.getUriFor("hdb_enabled"), false, new HdbSettingsObserver(getAdbHandler()).getContentObserver());
            if (Settings.System.getInt(getContentResolver(), "hdb_enabled", -1) < 0) {
                if (containsFunction(SystemPropertiesEx.get("ro.default.userportmode", "null"), "hdb")) {
                    try {
                        Flog.i(1306, "HwAdbService ro.default.userportmode:" + SystemPropertiesEx.get("ro.default.userportmode", "null"));
                        Settings.System.putInt(getContentResolver(), "hdb_enabled", 1);
                    } catch (SecurityException e) {
                        Flog.e(1306, "HwAdbService systemReady ro.default.userportmode set KEY_CONTENT_HDB_ALLOWED SecurityException");
                    } catch (Exception e2) {
                        Flog.e(1306, "HwAdbService systemReady ro.default.userportmode set KEY_CONTENT_HDB_ALLOWED Exception");
                    }
                } else if (SystemPropertiesEx.get("ro.product.locale.region", "null").equals("CN")) {
                    try {
                        Flog.i(1306, "HwAdbService ro.product.locale.region:" + SystemPropertiesEx.get("ro.product.locale.region", "null"));
                        Settings.System.putInt(getContentResolver(), "hdb_enabled", 1);
                    } catch (SecurityException e3) {
                        Flog.w(1306, "HwAdbService systemReady ro.product.locale.region set KEY_CONTENT_HDB_ALLOWED SecurityException");
                    } catch (Exception e4) {
                        Flog.w(1306, "HwAdbService systemReady ro.product.locale.region set KEY_CONTENT_HDB_ALLOWED Exception");
                    }
                } else {
                    try {
                        Flog.i(1306, "HwAdbService System.KEY_CONTENT_HDB_ALLOWED : 0");
                        Settings.System.putInt(getContentResolver(), "hdb_enabled", 0);
                    } catch (SecurityException e5) {
                        Flog.w(1306, "HwAdbService systemReady set KEY_CONTENT_HDB_ALLOWED SecurityException");
                    } catch (Exception e6) {
                        Flog.w(1306, "HwAdbService systemReady set KEY_CONTENT_HDB_ALLOWED Exception");
                    }
                }
            }
        } else {
            SlogEx.i(TAG, "device not support hdb feature");
        }
        getContentResolver().registerContentObserver(Settings.Secure.getUriFor(KEY_CONTENT_SUITESTATE), false, new SuitestateObserver(getAdbHandler()));
    }

    public void bootCompleted() {
        boolean z = false;
        if (Settings.System.getInt(getContentResolver(), "hdb_enabled", 0) > 0) {
            z = true;
        }
        this.mIsHdbEnabled = z;
        HwUsbManagerEx.getInstance().setHdbEnabled(this.mIsHdbEnabled);
    }

    /* access modifiers changed from: protected */
    public void handleUserSwtiched(int newUserId) {
        boolean isRepairMode = newUserId == 127;
        HdbSettingsObserver hdbSettingsObserver = new HdbSettingsObserver(getAdbHandler());
        if (isRepairMode) {
            ContentResolverExt.registerContentObserver(getContentResolver(), Settings.System.getUriFor("hdb_enabled"), false, hdbSettingsObserver.getContentObserver(), 127);
            if (SystemPropertiesEx.get("ro.product.locale.region", "null").equals("CN")) {
                try {
                    SettingsEx.System.putIntForUser(getContentResolver(), "hdb_enabled", 1, 127);
                } catch (SecurityException e) {
                    Flog.w(1306, "HwAdbService handleUserSwtiched set KEY_CONTENT_HDB_ALLOWED SecurityException");
                } catch (Exception e2) {
                    Flog.w(1306, "HwAdbService handleUserSwtiched set KEY_CONTENT_HDB_ALLOWED Exception");
                }
            }
        }
    }

    private class HdbSettingsObserver extends ContentObserverEx {
        HdbSettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean isSelfChange, Uri uri, int userId) {
            boolean isEnable = false;
            if (SettingsEx.System.getIntForUser(HwAdbService.this.getContentResolver(), "hdb_enabled", 0, userId) > 0) {
                isEnable = true;
            }
            Flog.i(1306, "HwAdbService Hdb Settings isEnable:" + isEnable);
            HwAdbService.this.setAdbHandlerMessage(AdbServiceEx.MSG_ENABLE_HDB, isEnable);
        }
    }

    /* access modifiers changed from: protected */
    public void setHdbEnabled(boolean isEnable) {
        if (DEBUG) {
            SlogEx.d(TAG, "setHdbEnabled(" + isEnable + "), mIsHdbEnabled=" + this.mIsHdbEnabled);
        }
        if (isEnable != this.mIsHdbEnabled) {
            this.mIsHdbEnabled = isEnable;
            for (IHdbTransport transport : this.mHdbTransports.values()) {
                try {
                    transport.onHdbEnabled(isEnable);
                } catch (RemoteException e) {
                    SlogEx.w(TAG, "Unable to send onHdbEnabled to transport " + transport.toString());
                }
            }
            HwUsbManagerEx.getInstance().setHdbEnabled(isEnable);
        }
    }

    private class SuitestateObserver extends ContentObserver {
        public SuitestateObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            HwAdbService.writeSuitestate();
        }
    }

    public static void writeSuitestate() {
        OutputStreamWriter osw = null;
        FileOutputStream fos = null;
        try {
            File newfile = new File(SUITE_STATE_PATH, SUITE_STATE_FILE);
            if (newfile.exists()) {
                fos = new FileOutputStream(newfile);
                osw = new OutputStreamWriter(fos, "UTF-8");
                osw.write("0");
                osw.flush();
            }
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    SlogEx.e(TAG, "IOException in close fw");
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e2) {
                    SlogEx.e(TAG, "IOException in close fos");
                }
            }
        } catch (IOException e3) {
            SlogEx.e(TAG, "IOException in writeCommand hisuite");
            if (0 != 0) {
                try {
                    osw.close();
                } catch (IOException e4) {
                    SlogEx.e(TAG, "IOException in close fw");
                }
            }
            if (0 != 0) {
                fos.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    osw.close();
                } catch (IOException e5) {
                    SlogEx.e(TAG, "IOException in close fw");
                }
            }
            if (0 != 0) {
                try {
                    fos.close();
                } catch (IOException e6) {
                    SlogEx.e(TAG, "IOException in close fos");
                }
            }
            throw th;
        }
    }

    private class HdbManagerInternalImpl extends HdbManagerInternal {
        private HdbManagerInternalImpl() {
        }

        public void registerTransport(IHdbTransport transport) {
            HwAdbService.this.mHdbTransports.put(transport.asBinder(), transport);
        }

        public void unregisterTransport(IHdbTransport transport) {
            HwAdbService.this.mHdbTransports.remove(transport.asBinder());
        }

        public boolean isHdbEnabled() {
            return HwAdbService.this.mIsHdbEnabled;
        }
    }
}
