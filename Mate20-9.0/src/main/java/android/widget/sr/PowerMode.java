package android.widget.sr;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;

public class PowerMode {
    public static final String TAG = "PowerMode";
    private static PowerMode sInstance;
    private final Class<?> SP = getSystemPropertiesClass();
    private Context mContext;
    private int mCountStepLimit = 100;
    private boolean mIsDeviceOwner = true;
    private boolean mIsNormalPowerSaveMode = false;
    private boolean mIsNullContext = false;
    private boolean mIsSuperPowerSaveMode = false;
    private int mSuperCount = 0;

    public static synchronized PowerMode getInstance(Context context, boolean deviceOwner) {
        PowerMode powerMode;
        synchronized (PowerMode.class) {
            if (sInstance == null) {
                sInstance = new PowerMode(context, deviceOwner);
            }
            powerMode = sInstance;
        }
        return powerMode;
    }

    private PowerMode(Context context, boolean deviceOwner) {
        this.mIsDeviceOwner = deviceOwner;
        if (context == null) {
            this.mIsNullContext = true;
            this.mIsNormalPowerSaveMode = false;
        } else {
            this.mIsNullContext = false;
            this.mContext = context.getApplicationContext();
            if (this.mIsDeviceOwner) {
                try {
                    int ret = Settings.System.getInt(this.mContext.getContentResolver(), "SmartModeStatus", 1);
                    Log.i(TAG, String.format("retvalu:%d", new Object[]{Integer.valueOf(ret)}));
                    this.mIsNormalPowerSaveMode = ret == 4;
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                    this.mIsNormalPowerSaveMode = true;
                }
            }
        }
        this.mIsSuperPowerSaveMode = getBoolean("sys.super_power_save", false);
    }

    public boolean getSuperPowerSaveMode() {
        return this.mIsSuperPowerSaveMode;
    }

    public boolean getNormalPowerSaveMode() {
        return this.mIsNormalPowerSaveMode;
    }

    public void setSuperPowerSaveMode(boolean flag) {
        this.mIsSuperPowerSaveMode = flag;
    }

    public void setNormalPowerSaveMode(boolean flag) {
        this.mIsNormalPowerSaveMode = flag;
    }

    public boolean getCurrentSuperPowerState() {
        return getBoolean("sys.super_power_save", false);
    }

    private boolean getBoolean(String key, boolean def) {
        if (this.SP == null) {
            return false;
        }
        try {
            return ((Boolean) this.SP.getMethod("getBoolean", new Class[]{String.class, Boolean.TYPE}).invoke(null, new Object[]{key, Boolean.valueOf(def)})).booleanValue();
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "PowerMode.getBoolean NoSuchMethodException");
            return false;
        } catch (IllegalAccessException e2) {
            Log.w(TAG, "PowerMode.getBoolean IllegalAccessException");
            return false;
        } catch (InvocationTargetException e3) {
            Log.w(TAG, "PowerMode.getBoolean InvocationTargetException");
            return false;
        }
    }

    private Class<?> getSystemPropertiesClass() {
        try {
            return Class.forName("android.os.SystemProperties");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public boolean isInSuperPowerState() {
        if (!this.mIsDeviceOwner) {
            Log.i(TAG, "You are not device owner, the super power mode will be set to Save state");
            return true;
        }
        if (this.mCountStepLimit == this.mSuperCount) {
            this.mIsSuperPowerSaveMode = getCurrentSuperPowerState();
            this.mSuperCount = 0;
        }
        this.mSuperCount++;
        return this.mIsSuperPowerSaveMode;
    }

    public boolean isInNormalPowerState() {
        if (!this.mIsDeviceOwner) {
            Log.i(TAG, "You are not device owner, the normal power mode will be set to Save state");
            return true;
        } else if (this.mIsNullContext) {
            Log.i(TAG, "normal not supported in null context");
            return false;
        } else {
            Log.i(TAG, "normal supported");
            return this.mIsNormalPowerSaveMode;
        }
    }
}
