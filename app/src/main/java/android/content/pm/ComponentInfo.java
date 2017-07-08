package android.content.pm;

import android.common.HwFrameworkFactory;
import android.common.HwPackageManager;
import android.content.ComponentName;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.Parcel;
import android.util.Printer;

public class ComponentInfo extends PackageItemInfo {
    public ApplicationInfo applicationInfo;
    public int descriptionRes;
    public boolean directBootAware;
    public boolean enabled;
    @Deprecated
    public boolean encryptionAware;
    public boolean exported;
    public String processName;

    public ComponentInfo() {
        this.enabled = true;
        this.exported = false;
        this.directBootAware = false;
        this.encryptionAware = false;
    }

    public ComponentInfo(ComponentInfo orig) {
        super((PackageItemInfo) orig);
        this.enabled = true;
        this.exported = false;
        this.directBootAware = false;
        this.encryptionAware = false;
        this.applicationInfo = orig.applicationInfo;
        this.processName = orig.processName;
        this.descriptionRes = orig.descriptionRes;
        this.enabled = orig.enabled;
        this.exported = orig.exported;
        boolean z = orig.directBootAware;
        this.directBootAware = z;
        this.encryptionAware = z;
    }

    public CharSequence loadLabel(PackageManager pm) {
        if (this.nonLocalizedLabel != null) {
            return this.nonLocalizedLabel;
        }
        ApplicationInfo ai = this.applicationInfo;
        CharSequence charSequence = null;
        if (this.labelRes != 0) {
            charSequence = pm.getText(this.packageName, this.labelRes, ai);
            if (charSequence != null) {
                return charSequence;
            }
        }
        if (ai.nonLocalizedLabel != null) {
            return ai.nonLocalizedLabel;
        }
        if (ai.labelRes != 0) {
            HwPackageManager hpm = HwFrameworkFactory.getHwPackageManager();
            if (!(ai.hwLabelRes == 0 || hpm == null)) {
                charSequence = hpm.getAppLabelText(pm, this.packageName, ai.hwLabelRes, ai);
            }
            if (charSequence == null) {
                charSequence = pm.getText(this.packageName, ai.labelRes, ai);
            }
            if (charSequence != null) {
                return charSequence;
            }
        }
        return this.name;
    }

    public boolean isEnabled() {
        return this.enabled ? this.applicationInfo.enabled : false;
    }

    public final int getIconResource() {
        HwThemeManager.updateIconCache(this, this.name, this.packageName, this.icon, this.applicationInfo.icon);
        return this.icon != 0 ? this.icon : this.applicationInfo.icon;
    }

    public final int getLogoResource() {
        return this.logo != 0 ? this.logo : this.applicationInfo.logo;
    }

    public final int getBannerResource() {
        return this.banner != 0 ? this.banner : this.applicationInfo.banner;
    }

    public ComponentName getComponentName() {
        return new ComponentName(this.packageName, this.name);
    }

    protected void dumpFront(Printer pw, String prefix) {
        super.dumpFront(pw, prefix);
        if (!(this.processName == null || this.packageName.equals(this.processName))) {
            pw.println(prefix + "processName=" + this.processName);
        }
        pw.println(prefix + "enabled=" + this.enabled + " exported=" + this.exported + " directBootAware=" + this.directBootAware);
        if (this.descriptionRes != 0) {
            pw.println(prefix + "description=" + this.descriptionRes);
        }
    }

    protected void dumpBack(Printer pw, String prefix) {
        dumpBack(pw, prefix, 3);
    }

    void dumpBack(Printer pw, String prefix, int flags) {
        if ((flags & 2) != 0) {
            if (this.applicationInfo != null) {
                pw.println(prefix + "ApplicationInfo:");
                this.applicationInfo.dump(pw, prefix + "  ", flags);
            } else {
                pw.println(prefix + "ApplicationInfo: null");
            }
        }
        super.dumpBack(pw, prefix);
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        int i;
        int i2 = 1;
        super.writeToParcel(dest, parcelableFlags);
        if ((parcelableFlags & 2) != 0) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            this.applicationInfo.writeToParcel(dest, parcelableFlags);
        }
        dest.writeString(this.processName);
        dest.writeInt(this.descriptionRes);
        if (this.enabled) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.exported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.directBootAware) {
            i2 = 0;
        }
        dest.writeInt(i2);
    }

    protected ComponentInfo(Parcel source) {
        boolean hasApplicationInfo;
        boolean z;
        boolean z2 = true;
        super(source);
        this.enabled = true;
        this.exported = false;
        this.directBootAware = false;
        this.encryptionAware = false;
        if (source.readInt() != 0) {
            hasApplicationInfo = true;
        } else {
            hasApplicationInfo = false;
        }
        if (hasApplicationInfo) {
            this.applicationInfo = (ApplicationInfo) ApplicationInfo.CREATOR.createFromParcel(source);
        }
        this.processName = source.readString();
        this.descriptionRes = source.readInt();
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.enabled = z;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.exported = z;
        if (source.readInt() == 0) {
            z2 = false;
        }
        this.directBootAware = z2;
        this.encryptionAware = z2;
    }

    public Drawable loadDefaultIcon(PackageManager pm) {
        return this.applicationInfo.loadIcon(pm);
    }

    protected Drawable loadDefaultBanner(PackageManager pm) {
        return this.applicationInfo.loadBanner(pm);
    }

    protected Drawable loadDefaultLogo(PackageManager pm) {
        return this.applicationInfo.loadLogo(pm);
    }

    protected ApplicationInfo getApplicationInfo() {
        return this.applicationInfo;
    }
}
