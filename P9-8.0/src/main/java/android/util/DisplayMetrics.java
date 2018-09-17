package android.util;

import android.os.SystemProperties;

public class DisplayMetrics {
    public static final int DENSITY_260 = 260;
    public static final int DENSITY_280 = 280;
    public static final int DENSITY_300 = 300;
    public static final int DENSITY_340 = 340;
    public static final int DENSITY_360 = 360;
    public static final int DENSITY_400 = 400;
    public static final int DENSITY_420 = 420;
    public static final int DENSITY_560 = 560;
    public static final int DENSITY_DEFAULT = 160;
    public static final float DENSITY_DEFAULT_SCALE = 0.00625f;
    @Deprecated
    public static int DENSITY_DEVICE = getDeviceDensity();
    public static final int DENSITY_DEVICE_STABLE = getDeviceDensity();
    public static final int DENSITY_HIGH = 240;
    public static final int DENSITY_LOW = 120;
    public static final int DENSITY_MEDIUM = 160;
    public static final int DENSITY_TV = 213;
    public static final int DENSITY_XHIGH = 320;
    public static final int DENSITY_XXHIGH = 480;
    public static final int DENSITY_XXXHIGH = 640;
    private static final int SCREEN_SIZE_1080P_DPI = 480;
    private static final int SCREEN_SIZE_1080P_DPI_FORPAD = 420;
    private static final int SCREEN_SIZE_1080P_WIDTH = 1080;
    private static final int SCREEN_SIZE_2K_DPI = 640;
    private static final int SCREEN_SIZE_2K_DPI_FORPAD = 560;
    private static final int SCREEN_SIZE_2K_WIDTH = 1440;
    private static final int SCREEN_SIZE_720P_DPI = 320;
    private static final int SCREEN_SIZE_720P_DPI_FORPAD = 280;
    private static final int SCREEN_SIZE_720P_WIDTH = 720;
    public float density;
    public int densityDpi;
    public int heightPixels;
    public float noncompatDensity;
    public int noncompatDensityDpi;
    public int noncompatHeightPixels;
    public float noncompatScaledDensity;
    public int noncompatWidthPixels;
    public float noncompatXdpi;
    public float noncompatYdpi;
    public float scaledDensity;
    public int widthPixels;
    public float xdpi;
    public float ydpi;

    public void setToNonCompat() {
        this.widthPixels = this.noncompatWidthPixels;
        this.heightPixels = this.noncompatHeightPixels;
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 160));
        int dpi = SystemProperties.getInt("persist.sys.dpi", lcdDpi);
        int i = (int) ((((float) (lcdDpi * SystemProperties.getInt("persist.sys.realdpi", dpi))) * 1.0f) / ((float) dpi));
        this.noncompatDensityDpi = i;
        this.densityDpi = i;
        this.density = ((float) this.densityDpi) / 160.0f;
        this.scaledDensity = this.noncompatScaledDensity;
        this.xdpi = this.noncompatXdpi;
        this.ydpi = this.noncompatYdpi;
    }

    public void setTo(DisplayMetrics o) {
        if (this != o) {
            this.widthPixels = o.widthPixels;
            this.heightPixels = o.heightPixels;
            this.density = o.density;
            this.densityDpi = o.densityDpi;
            this.scaledDensity = o.scaledDensity;
            this.xdpi = o.xdpi;
            this.ydpi = o.ydpi;
            this.noncompatWidthPixels = o.noncompatWidthPixels;
            this.noncompatHeightPixels = o.noncompatHeightPixels;
            this.noncompatDensity = o.noncompatDensity;
            this.noncompatDensityDpi = o.noncompatDensityDpi;
            this.noncompatScaledDensity = o.noncompatScaledDensity;
            this.noncompatXdpi = o.noncompatXdpi;
            this.noncompatYdpi = o.noncompatYdpi;
        }
    }

    public void setToDefaults() {
        this.widthPixels = 0;
        this.heightPixels = 0;
        this.density = ((float) DENSITY_DEVICE) / 160.0f;
        this.densityDpi = DENSITY_DEVICE;
        this.scaledDensity = this.density;
        this.xdpi = (float) DENSITY_DEVICE;
        this.ydpi = (float) DENSITY_DEVICE;
        this.noncompatWidthPixels = this.widthPixels;
        this.noncompatHeightPixels = this.heightPixels;
        this.noncompatDensity = this.density;
        this.noncompatDensityDpi = this.densityDpi;
        this.noncompatScaledDensity = this.scaledDensity;
        this.noncompatXdpi = this.xdpi;
        this.noncompatYdpi = this.ydpi;
    }

    public boolean equals(Object o) {
        return o instanceof DisplayMetrics ? equals((DisplayMetrics) o) : false;
    }

    public boolean equals(DisplayMetrics other) {
        if (equalsPhysical(other) && this.scaledDensity == other.scaledDensity && this.noncompatScaledDensity == other.noncompatScaledDensity) {
            return true;
        }
        return false;
    }

    public boolean equalsPhysical(DisplayMetrics other) {
        return other != null && this.widthPixels == other.widthPixels && this.heightPixels == other.heightPixels && this.density == other.density && this.densityDpi == other.densityDpi && this.xdpi == other.xdpi && this.ydpi == other.ydpi && this.noncompatWidthPixels == other.noncompatWidthPixels && this.noncompatHeightPixels == other.noncompatHeightPixels && this.noncompatDensity == other.noncompatDensity && this.noncompatDensityDpi == other.noncompatDensityDpi && this.noncompatXdpi == other.noncompatXdpi && this.noncompatYdpi == other.noncompatYdpi;
    }

    public int hashCode() {
        return (this.widthPixels * this.heightPixels) * this.densityDpi;
    }

    public String toString() {
        return "DisplayMetrics{density=" + this.density + ", width=" + this.widthPixels + ", height=" + this.heightPixels + ", scaledDensity=" + this.scaledDensity + ", xdpi=" + this.xdpi + ", ydpi=" + this.ydpi + ", densityDpi=" + this.densityDpi + ", noncompatWidthPixels=" + this.noncompatWidthPixels + ", noncompatHeightPixels=" + this.noncompatHeightPixels + ", noncompatDensity=" + this.noncompatDensity + ", noncompatDensityDpi=" + this.noncompatDensityDpi + ", noncompatXdpi=" + this.noncompatXdpi + ", noncompatYdpi=" + this.noncompatYdpi + "}";
    }

    public static int getROGDensity(int width) {
        int dpi = SystemProperties.getInt("persist.sys.dpi", 0);
        int realdpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
        if (realdpi == 640 || realdpi == 480) {
            switch (width) {
                case 720:
                    if (dpi <= 0) {
                        dpi = 320;
                        break;
                    }
                    dpi = (dpi * 320) / realdpi;
                    break;
                case SCREEN_SIZE_1080P_WIDTH /*1080*/:
                    if (dpi <= 0) {
                        dpi = 480;
                        break;
                    }
                    dpi = (dpi * 480) / realdpi;
                    break;
                case SCREEN_SIZE_2K_WIDTH /*1440*/:
                    if (dpi <= 0) {
                        dpi = 640;
                        break;
                    }
                    dpi = (dpi * 640) / realdpi;
                    break;
            }
        }
        if (realdpi != 560 && realdpi != 420) {
            return dpi;
        }
        switch (width) {
            case 720:
                if (dpi > 0) {
                    return (dpi * 280) / realdpi;
                }
                return 280;
            case SCREEN_SIZE_1080P_WIDTH /*1080*/:
                if (dpi > 0) {
                    return (dpi * 420) / realdpi;
                }
                return 420;
            case SCREEN_SIZE_2K_WIDTH /*1440*/:
                if (dpi > 0) {
                    return (dpi * 560) / realdpi;
                }
                return 560;
            default:
                return dpi;
        }
    }

    public static int getROGDensity() {
        return SystemProperties.getInt("persist.sys.realdpi", SystemProperties.getInt("persist.sys.dpi", SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0))));
    }

    private static int getDeviceDensity() {
        int dpi = SystemProperties.getInt("persist.sys.dpi", 0);
        if (SystemProperties.getInt("persist.sys.rog.width", 0) > 0) {
            return getROGDensity();
        }
        if (dpi > 0 && dpi != SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 160))) {
            return dpi;
        }
        int density = SystemProperties.getInt("persist.sys.lcd_density", 0);
        if (density > 0) {
            return density;
        }
        return SystemProperties.getInt("qemu.sf.lcd_density", SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 160)));
    }
}
