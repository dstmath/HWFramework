package android.hardware.display;

import android.annotation.SystemApi;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.display.IColorDisplayManager;
import android.metrics.LogMaker;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import com.android.internal.R;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalTime;

@SystemApi
public final class ColorDisplayManager {
    @SystemApi
    public static final int AUTO_MODE_CUSTOM_TIME = 1;
    @SystemApi
    public static final int AUTO_MODE_DISABLED = 0;
    @SystemApi
    public static final int AUTO_MODE_TWILIGHT = 2;
    @SystemApi
    public static final int CAPABILITY_HARDWARE_ACCELERATION_GLOBAL = 2;
    @SystemApi
    public static final int CAPABILITY_HARDWARE_ACCELERATION_PER_APP = 4;
    @SystemApi
    public static final int CAPABILITY_NONE = 0;
    @SystemApi
    public static final int CAPABILITY_PROTECTED_CONTENT = 1;
    public static final int COLOR_MODE_AUTOMATIC = 3;
    public static final int COLOR_MODE_BOOSTED = 1;
    public static final int COLOR_MODE_NATURAL = 0;
    public static final int COLOR_MODE_SATURATED = 2;
    public static final int VENDOR_COLOR_MODE_RANGE_MAX = 511;
    public static final int VENDOR_COLOR_MODE_RANGE_MIN = 256;
    private final ColorDisplayManagerInternal mManager = ColorDisplayManagerInternal.getInstance();
    private MetricsLogger mMetricsLogger;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AutoMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface CapabilityType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ColorMode {
    }

    public boolean setNightDisplayActivated(boolean activated) {
        return this.mManager.setNightDisplayActivated(activated);
    }

    public boolean isNightDisplayActivated() {
        return this.mManager.isNightDisplayActivated();
    }

    public boolean setNightDisplayColorTemperature(int temperature) {
        return this.mManager.setNightDisplayColorTemperature(temperature);
    }

    public int getNightDisplayColorTemperature() {
        return this.mManager.getNightDisplayColorTemperature();
    }

    @SystemApi
    public int getNightDisplayAutoMode() {
        return this.mManager.getNightDisplayAutoMode();
    }

    public int getNightDisplayAutoModeRaw() {
        return this.mManager.getNightDisplayAutoModeRaw();
    }

    @SystemApi
    public boolean setNightDisplayAutoMode(int autoMode) {
        if (autoMode == 0 || autoMode == 1 || autoMode == 2) {
            if (this.mManager.getNightDisplayAutoMode() != autoMode) {
                getMetricsLogger().write(new LogMaker((int) MetricsProto.MetricsEvent.ACTION_NIGHT_DISPLAY_AUTO_MODE_CHANGED).setType(4).setSubtype(autoMode));
            }
            return this.mManager.setNightDisplayAutoMode(autoMode);
        }
        throw new IllegalArgumentException("Invalid autoMode: " + autoMode);
    }

    public LocalTime getNightDisplayCustomStartTime() {
        return this.mManager.getNightDisplayCustomStartTime().getLocalTime();
    }

    @SystemApi
    public boolean setNightDisplayCustomStartTime(LocalTime startTime) {
        if (startTime != null) {
            getMetricsLogger().write(new LogMaker(1310).setType(4).setSubtype(0));
            return this.mManager.setNightDisplayCustomStartTime(new Time(startTime));
        }
        throw new IllegalArgumentException("startTime cannot be null");
    }

    public LocalTime getNightDisplayCustomEndTime() {
        return this.mManager.getNightDisplayCustomEndTime().getLocalTime();
    }

    @SystemApi
    public boolean setNightDisplayCustomEndTime(LocalTime endTime) {
        if (endTime != null) {
            getMetricsLogger().write(new LogMaker(1310).setType(4).setSubtype(1));
            return this.mManager.setNightDisplayCustomEndTime(new Time(endTime));
        }
        throw new IllegalArgumentException("endTime cannot be null");
    }

    public void setColorMode(int colorMode) {
        this.mManager.setColorMode(colorMode);
    }

    public int getColorMode() {
        return this.mManager.getColorMode();
    }

    public boolean isDeviceColorManaged() {
        return this.mManager.isDeviceColorManaged();
    }

    @SystemApi
    public boolean setSaturationLevel(int saturationLevel) {
        return this.mManager.setSaturationLevel(saturationLevel);
    }

    public boolean isSaturationActivated() {
        return this.mManager.isSaturationActivated();
    }

    @SystemApi
    public boolean setAppSaturationLevel(String packageName, int saturationLevel) {
        return this.mManager.setAppSaturationLevel(packageName, saturationLevel);
    }

    public boolean setDisplayWhiteBalanceEnabled(boolean enabled) {
        return this.mManager.setDisplayWhiteBalanceEnabled(enabled);
    }

    public boolean isDisplayWhiteBalanceEnabled() {
        return this.mManager.isDisplayWhiteBalanceEnabled();
    }

    public static boolean isNightDisplayAvailable(Context context) {
        return context.getResources().getBoolean(R.bool.config_nightDisplayAvailable);
    }

    public static int getMinimumColorTemperature(Context context) {
        return context.getResources().getInteger(R.integer.config_nightDisplayColorTemperatureMin);
    }

    public static int getMaximumColorTemperature(Context context) {
        return context.getResources().getInteger(R.integer.config_nightDisplayColorTemperatureMax);
    }

    public static boolean isDisplayWhiteBalanceAvailable(Context context) {
        return context.getResources().getBoolean(R.bool.config_displayWhiteBalanceAvailable);
    }

    public static boolean isColorTransformAccelerated(Context context) {
        return context.getResources().getBoolean(R.bool.config_setColorTransformAccelerated);
    }

    @SystemApi
    public int getTransformCapabilities() {
        return this.mManager.getTransformCapabilities();
    }

    public static boolean areAccessibilityTransformsEnabled(Context context) {
        ContentResolver cr = context.getContentResolver();
        return Settings.Secure.getInt(cr, Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, 0) == 1 || Settings.Secure.getInt(cr, Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED, 0) == 1;
    }

    private MetricsLogger getMetricsLogger() {
        if (this.mMetricsLogger == null) {
            this.mMetricsLogger = new MetricsLogger();
        }
        return this.mMetricsLogger;
    }

    /* access modifiers changed from: private */
    public static class ColorDisplayManagerInternal {
        private static ColorDisplayManagerInternal sInstance;
        private final IColorDisplayManager mCdm;

        private ColorDisplayManagerInternal(IColorDisplayManager colorDisplayManager) {
            this.mCdm = colorDisplayManager;
        }

        public static ColorDisplayManagerInternal getInstance() {
            ColorDisplayManagerInternal colorDisplayManagerInternal;
            synchronized (ColorDisplayManagerInternal.class) {
                if (sInstance == null) {
                    try {
                        sInstance = new ColorDisplayManagerInternal(IColorDisplayManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.COLOR_DISPLAY_SERVICE)));
                    } catch (ServiceManager.ServiceNotFoundException e) {
                        throw new IllegalStateException(e);
                    }
                }
                colorDisplayManagerInternal = sInstance;
            }
            return colorDisplayManagerInternal;
        }

        /* access modifiers changed from: package-private */
        public boolean isNightDisplayActivated() {
            try {
                return this.mCdm.isNightDisplayActivated();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean setNightDisplayActivated(boolean activated) {
            try {
                return this.mCdm.setNightDisplayActivated(activated);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public int getNightDisplayColorTemperature() {
            try {
                return this.mCdm.getNightDisplayColorTemperature();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean setNightDisplayColorTemperature(int temperature) {
            try {
                return this.mCdm.setNightDisplayColorTemperature(temperature);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public int getNightDisplayAutoMode() {
            try {
                return this.mCdm.getNightDisplayAutoMode();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public int getNightDisplayAutoModeRaw() {
            try {
                return this.mCdm.getNightDisplayAutoModeRaw();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean setNightDisplayAutoMode(int autoMode) {
            try {
                return this.mCdm.setNightDisplayAutoMode(autoMode);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public Time getNightDisplayCustomStartTime() {
            try {
                return this.mCdm.getNightDisplayCustomStartTime();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean setNightDisplayCustomStartTime(Time startTime) {
            try {
                return this.mCdm.setNightDisplayCustomStartTime(startTime);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public Time getNightDisplayCustomEndTime() {
            try {
                return this.mCdm.getNightDisplayCustomEndTime();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean setNightDisplayCustomEndTime(Time endTime) {
            try {
                return this.mCdm.setNightDisplayCustomEndTime(endTime);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isDeviceColorManaged() {
            try {
                return this.mCdm.isDeviceColorManaged();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean setSaturationLevel(int saturationLevel) {
            try {
                return this.mCdm.setSaturationLevel(saturationLevel);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isSaturationActivated() {
            try {
                return this.mCdm.isSaturationActivated();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean setAppSaturationLevel(String packageName, int saturationLevel) {
            try {
                return this.mCdm.setAppSaturationLevel(packageName, saturationLevel);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isDisplayWhiteBalanceEnabled() {
            try {
                return this.mCdm.isDisplayWhiteBalanceEnabled();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean setDisplayWhiteBalanceEnabled(boolean enabled) {
            try {
                return this.mCdm.setDisplayWhiteBalanceEnabled(enabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public int getColorMode() {
            try {
                return this.mCdm.getColorMode();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public void setColorMode(int colorMode) {
            try {
                this.mCdm.setColorMode(colorMode);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public int getTransformCapabilities() {
            try {
                return this.mCdm.getTransformCapabilities();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }
}
