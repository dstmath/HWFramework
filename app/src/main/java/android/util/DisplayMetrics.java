package android.util;

import android.os.SystemProperties;

public class DisplayMetrics {
    public static final int DENSITY_280 = 280;
    public static final int DENSITY_360 = 360;
    public static final int DENSITY_400 = 400;
    public static final int DENSITY_420 = 420;
    public static final int DENSITY_560 = 560;
    public static final int DENSITY_DEFAULT = 160;
    public static final float DENSITY_DEFAULT_SCALE = 0.00625f;
    @Deprecated
    public static int DENSITY_DEVICE = 0;
    public static final int DENSITY_DEVICE_STABLE = 0;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.DisplayMetrics.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.DisplayMetrics.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.DisplayMetrics.<clinit>():void");
    }

    public void setTo(DisplayMetrics o) {
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

    public void setToDefaults() {
        this.widthPixels = DENSITY_DEVICE_STABLE;
        this.heightPixels = DENSITY_DEVICE_STABLE;
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
        return "DisplayMetrics{density=" + this.density + ", width=" + this.widthPixels + ", height=" + this.heightPixels + ", scaledDensity=" + this.scaledDensity + ", xdpi=" + this.xdpi + ", ydpi=" + this.ydpi + "}";
    }

    public static int getROGDensity(int width) {
        int dpi = SystemProperties.getInt("persist.sys.dpi", DENSITY_DEVICE_STABLE);
        int realdpi = SystemProperties.getInt("ro.sf.lcd_density", DENSITY_DEVICE_STABLE);
        if (realdpi == SCREEN_SIZE_2K_DPI || realdpi == SCREEN_SIZE_1080P_DPI) {
            switch (width) {
                case SCREEN_SIZE_720P_WIDTH /*720*/:
                    if (dpi <= 0) {
                        dpi = SCREEN_SIZE_720P_DPI;
                        break;
                    }
                    dpi = (dpi * SCREEN_SIZE_720P_DPI) / realdpi;
                    break;
                case SCREEN_SIZE_1080P_WIDTH /*1080*/:
                    if (dpi <= 0) {
                        dpi = SCREEN_SIZE_1080P_DPI;
                        break;
                    }
                    dpi = (dpi * SCREEN_SIZE_1080P_DPI) / realdpi;
                    break;
                case SCREEN_SIZE_2K_WIDTH /*1440*/:
                    if (dpi <= 0) {
                        dpi = SCREEN_SIZE_2K_DPI;
                        break;
                    }
                    dpi = (dpi * SCREEN_SIZE_2K_DPI) / realdpi;
                    break;
            }
        }
        if (realdpi != SCREEN_SIZE_2K_DPI_FORPAD && realdpi != SCREEN_SIZE_1080P_DPI_FORPAD) {
            return dpi;
        }
        switch (width) {
            case SCREEN_SIZE_720P_WIDTH /*720*/:
                if (dpi > 0) {
                    return (dpi * SCREEN_SIZE_720P_DPI_FORPAD) / realdpi;
                }
                return SCREEN_SIZE_720P_DPI_FORPAD;
            case SCREEN_SIZE_1080P_WIDTH /*1080*/:
                if (dpi > 0) {
                    return (dpi * SCREEN_SIZE_1080P_DPI_FORPAD) / realdpi;
                }
                return SCREEN_SIZE_1080P_DPI_FORPAD;
            case SCREEN_SIZE_2K_WIDTH /*1440*/:
                if (dpi > 0) {
                    return (dpi * SCREEN_SIZE_2K_DPI_FORPAD) / realdpi;
                }
                return SCREEN_SIZE_2K_DPI_FORPAD;
            default:
                return dpi;
        }
    }

    public static int getROGDensity() {
        return SystemProperties.getInt("persist.sys.realdpi", SystemProperties.getInt("persist.sys.dpi", SystemProperties.getInt("ro.sf.lcd_density", DENSITY_DEVICE_STABLE)));
    }

    private static int getDeviceDensity() {
        int dpi = SystemProperties.getInt("persist.sys.dpi", DENSITY_DEVICE_STABLE);
        if (dpi > 0 && dpi != SystemProperties.getInt("ro.sf.lcd_density", DENSITY_MEDIUM)) {
            return dpi;
        }
        int density = SystemProperties.getInt("persist.sys.lcd_density", DENSITY_DEVICE_STABLE);
        if (density > 0) {
            return density;
        }
        return SystemProperties.getInt("qemu.sf.lcd_density", SystemProperties.getInt("ro.sf.lcd_density", DENSITY_MEDIUM));
    }
}
