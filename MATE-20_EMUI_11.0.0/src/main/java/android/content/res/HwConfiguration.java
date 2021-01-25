package android.content.res;

import android.os.Parcel;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hwpartbasicplatform.BuildConfig;

public class HwConfiguration extends HwConfigurationDummy {
    public int hwtheme;
    public int isClearCache;
    public int simpleuiMode;
    public int userId;

    public HwConfiguration() {
        setToDefaults();
    }

    public HwConfiguration(IHwConfiguration newConfig) {
        setTo(newConfig);
    }

    public final void setTo(IHwConfiguration obj) {
        if (obj != null) {
            HwConfiguration hwConfig = (HwConfiguration) obj;
            this.hwtheme = hwConfig.hwtheme;
            this.simpleuiMode = hwConfig.simpleuiMode;
            this.userId = hwConfig.userId;
            this.isClearCache = hwConfig.isClearCache;
        }
    }

    public final void setToDefaults() {
        this.hwtheme = 0;
        this.simpleuiMode = 0;
        this.userId = 0;
        this.isClearCache = 0;
    }

    @Deprecated
    public void makeDefault() {
        setToDefaults();
    }

    public int updateFrom(IHwConfiguration obj) {
        if (obj == null) {
            return -1;
        }
        int changed = 0;
        HwConfiguration delta = (HwConfiguration) obj;
        int i = delta.hwtheme;
        if (!(i == 0 || this.hwtheme == i)) {
            changed = 0 | 32768;
            this.hwtheme = i;
        }
        int i2 = delta.simpleuiMode;
        if (!(i2 == 0 || this.simpleuiMode == i2)) {
            changed |= 65536;
            this.simpleuiMode = i2;
        }
        int i3 = this.userId;
        int i4 = delta.userId;
        if (i3 != i4) {
            this.userId = i4;
        }
        int i5 = this.isClearCache;
        int i6 = delta.isClearCache;
        if (i5 != i6) {
            this.isClearCache = i6;
        }
        return changed;
    }

    public int diff(IHwConfiguration obj) {
        if (obj == null) {
            return -1;
        }
        int changed = 0;
        HwConfiguration delta = (HwConfiguration) obj;
        int i = delta.hwtheme;
        if (!(i == 0 || this.hwtheme == i)) {
            changed = 0 | 32768;
        }
        int i2 = delta.simpleuiMode;
        if (i2 == 0 || this.simpleuiMode == i2) {
            return changed;
        }
        return changed | 65536;
    }

    public static boolean needNewResources(int configChanges) {
        return (32768 & configChanges) != 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (dest != null) {
            dest.writeInt(this.hwtheme);
            dest.writeInt(this.simpleuiMode);
            dest.writeInt(this.userId);
            dest.writeInt(this.isClearCache);
        }
    }

    public void readFromParcel(Parcel source) {
        if (source != null) {
            this.hwtheme = source.readInt();
            this.simpleuiMode = source.readInt();
            this.userId = source.readInt();
            this.isClearCache = source.readInt();
        }
    }

    public int compareTo(Object that) {
        if (that instanceof HwConfiguration) {
            return compareTo((IHwConfiguration) ((HwConfiguration) that));
        }
        throw new ClassCastException();
    }

    public int compareTo(IHwConfiguration obj) {
        if (obj == null) {
            return -1;
        }
        HwConfiguration that = (HwConfiguration) obj;
        int diffValue = this.hwtheme - that.hwtheme;
        if (diffValue != 0) {
            return diffValue;
        }
        int diffValue2 = this.simpleuiMode - that.simpleuiMode;
        if (diffValue2 != 0) {
            return diffValue2;
        }
        int diffValue3 = this.userId - that.userId;
        if (diffValue3 != 0) {
            return diffValue3;
        }
        return this.isClearCache - that.isClearCache;
    }

    public void setConfigItem(int mode, int val) {
        if (mode == 1) {
            this.hwtheme = val;
        } else if (mode == 2) {
            this.simpleuiMode = val;
        } else if (mode == 3) {
            this.userId = val;
        } else if (mode == 4) {
            this.isClearCache = val;
        }
    }

    public int getConfigItem(int mode) {
        if (mode == 1) {
            return this.hwtheme;
        }
        if (mode == 2) {
            return this.simpleuiMode;
        }
        if (mode == 3) {
            return this.userId;
        }
        if (mode != 4) {
            return 0;
        }
        return this.isClearCache;
    }

    public void setDensityDPI(int dpi) {
        int oldDisplayDpi = SystemPropertiesEx.getInt("persist.sys.dpi", SystemPropertiesEx.getInt("ro.sf.real_lcd_density", SystemPropertiesEx.getInt("ro.sf.lcd_density", 0)));
        int oldRealDpi = SystemPropertiesEx.getInt("persist.sys.realdpi", oldDisplayDpi);
        SystemPropertiesEx.set("persist.sys.dpi", dpi + BuildConfig.FLAVOR);
        if (isElsaProduct()) {
            setDensityDpiForElsa(dpi, oldRealDpi, oldDisplayDpi);
            return;
        }
        SystemPropertiesEx.set("persist.sys.realdpi", ((dpi * oldRealDpi) / oldDisplayDpi) + BuildConfig.FLAVOR);
    }

    private void setDensityDpiForElsa(int dpi, int oldRealDpi, int oldDisplayDpi) {
        if ((dpi * oldRealDpi) % oldDisplayDpi != 0) {
            SystemPropertiesEx.set("persist.sys.realdpi", ((int) (((((double) dpi) * 2.0d) / 3.0d) + 0.5d)) + BuildConfig.FLAVOR);
            return;
        }
        SystemPropertiesEx.set("persist.sys.realdpi", ((dpi * oldRealDpi) / oldDisplayDpi) + BuildConfig.FLAVOR);
    }

    private boolean isElsaProduct() {
        return "ELS".equals(SystemPropertiesEx.get("ro.build.product", BuildConfig.FLAVOR));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        if (this.hwtheme != 0) {
            sb.append(" hwt:");
            sb.append(this.hwtheme);
        }
        if (this.simpleuiMode != 0) {
            sb.append(" suim:");
            sb.append(this.simpleuiMode);
        }
        if (this.userId != 0) {
            sb.append(" userId:");
            sb.append(this.userId);
        }
        if (this.isClearCache != 0) {
            sb.append(" isClearCache:");
            sb.append(this.isClearCache);
        }
        return sb.toString();
    }

    public boolean equals(IHwConfiguration obj) {
        HwConfiguration that = (HwConfiguration) obj;
        if (that == null) {
            return false;
        }
        if (that == this) {
            return true;
        }
        if (compareTo((IHwConfiguration) that) == 0) {
            return true;
        }
        return false;
    }

    public boolean equals(Object that) {
        if (that instanceof HwConfiguration) {
            return equals((IHwConfiguration) ((HwConfiguration) that));
        }
        return false;
    }

    public int hashCode() {
        return (((((((17 * 31) + this.simpleuiMode) * 31) + this.hwtheme) * 31) + this.userId) * 31) + this.isClearCache;
    }
}
