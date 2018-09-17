package android.content.res;

import android.os.Parcel;
import android.os.SystemProperties;
import com.huawei.hsm.permission.StubController;

public final class ConfigurationEx implements Comparable<Object>, IHwConfiguration {
    public int hwtheme;
    public int isClearCache;
    public int simpleuiMode;
    public int userId;

    public ConfigurationEx() {
        setToDefaults();
    }

    public ConfigurationEx(ConfigurationEx o) {
        setTo(o);
    }

    public void setTo(IHwConfiguration obj) {
        ConfigurationEx o = (ConfigurationEx) obj;
        this.hwtheme = o.hwtheme;
        this.simpleuiMode = o.simpleuiMode;
        this.userId = o.userId;
        this.isClearCache = o.isClearCache;
    }

    public void setToDefaults() {
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
        int changed = 0;
        ConfigurationEx delta = (ConfigurationEx) obj;
        if (!(delta.hwtheme == 0 || this.hwtheme == delta.hwtheme)) {
            changed = StubController.PERMISSION_CALLLOG_WRITE;
            this.hwtheme = delta.hwtheme;
        }
        if (!(delta.simpleuiMode == 0 || this.simpleuiMode == delta.simpleuiMode)) {
            changed |= StubController.PERMISSION_SMSLOG_WRITE;
            this.simpleuiMode = delta.simpleuiMode;
        }
        if (this.userId != delta.userId) {
            this.userId = delta.userId;
        }
        if (this.isClearCache != delta.isClearCache) {
            this.isClearCache = delta.isClearCache;
        }
        return changed;
    }

    public int diff(IHwConfiguration obj) {
        int changed = 0;
        ConfigurationEx delta = (ConfigurationEx) obj;
        if (!(delta.hwtheme == 0 || this.hwtheme == delta.hwtheme)) {
            changed = StubController.PERMISSION_CALLLOG_WRITE;
        }
        if (delta.simpleuiMode == 0 || this.simpleuiMode == delta.simpleuiMode) {
            return changed;
        }
        return changed | StubController.PERMISSION_SMSLOG_WRITE;
    }

    public static boolean needNewResources(int configChanges) {
        return (StubController.PERMISSION_CALLLOG_WRITE & configChanges) != 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.hwtheme);
        dest.writeInt(this.simpleuiMode);
        dest.writeInt(this.userId);
        dest.writeInt(this.isClearCache);
    }

    public void readFromParcel(Parcel source) {
        this.hwtheme = source.readInt();
        this.simpleuiMode = source.readInt();
        this.userId = source.readInt();
        this.isClearCache = source.readInt();
    }

    public int compareTo(Object that) {
        if (that instanceof ConfigurationEx) {
            return compareTo((ConfigurationEx) that);
        }
        throw new ClassCastException();
    }

    public int compareTo(IHwConfiguration obj) {
        ConfigurationEx that = (ConfigurationEx) obj;
        int n = this.hwtheme - that.hwtheme;
        if (n != 0) {
            return n;
        }
        n = this.simpleuiMode - that.simpleuiMode;
        if (n != 0) {
            return n;
        }
        n = this.userId - that.userId;
        if (n != 0) {
            return n;
        }
        return this.isClearCache - that.isClearCache;
    }

    public void setConfigItem(int mode, int val) {
        switch (mode) {
            case 1:
                this.hwtheme = val;
                return;
            case 2:
                this.simpleuiMode = val;
                return;
            case 3:
                this.userId = val;
                return;
            case 4:
                this.isClearCache = val;
                return;
            default:
                return;
        }
    }

    public int getConfigItem(int mode) {
        switch (mode) {
            case 1:
                return this.hwtheme;
            case 2:
                return this.simpleuiMode;
            case 3:
                return this.userId;
            case 4:
                return this.isClearCache;
            default:
                return 0;
        }
    }

    public void setDensityDPI(int dpi) {
        int oldDisplayDpi = SystemProperties.getInt("persist.sys.dpi", SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0)));
        int oldRealDpi = SystemProperties.getInt("persist.sys.realdpi", oldDisplayDpi);
        SystemProperties.set("persist.sys.dpi", dpi + "");
        SystemProperties.set("persist.sys.realdpi", ((dpi * oldRealDpi) / oldDisplayDpi) + "");
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
        boolean z = true;
        IHwConfiguration that = (ConfigurationEx) obj;
        if (that == null) {
            return false;
        }
        if (that == this) {
            return true;
        }
        if (compareTo(that) != 0) {
            z = false;
        }
        return z;
    }

    public boolean equals(Object that) {
        if (that instanceof ConfigurationEx) {
            return equals((ConfigurationEx) that);
        }
        return false;
    }

    public int hashCode() {
        return ((((((this.simpleuiMode + 527) * 31) + this.hwtheme) * 31) + this.userId) * 31) + this.isClearCache;
    }
}
