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
    public boolean directBootAware = false;
    public boolean enabled = true;
    @Deprecated
    public boolean encryptionAware = false;
    public boolean exported = false;
    public String processName;
    public String splitName;

    public ComponentInfo() {
    }

    public ComponentInfo(ComponentInfo orig) {
        super((PackageItemInfo) orig);
        this.applicationInfo = orig.applicationInfo;
        this.processName = orig.processName;
        this.splitName = orig.splitName;
        this.descriptionRes = orig.descriptionRes;
        this.enabled = orig.enabled;
        this.exported = orig.exported;
        boolean z = orig.directBootAware;
        this.directBootAware = z;
        this.encryptionAware = z;
    }

    public CharSequence loadUnsafeLabel(PackageManager pm) {
        if (this.nonLocalizedLabel != null) {
            return this.nonLocalizedLabel;
        }
        ApplicationInfo ai = this.applicationInfo;
        CharSequence label = null;
        if (this.labelRes != 0) {
            label = pm.getText(this.packageName, this.labelRes, ai);
            if (label != null) {
                return label;
            }
        }
        if (ai.nonLocalizedLabel != null) {
            return ai.nonLocalizedLabel;
        }
        if (ai.labelRes != 0) {
            HwPackageManager hpm = HwFrameworkFactory.getHwPackageManager();
            if (!(ai.hwLabelRes == 0 || hpm == null)) {
                label = hpm.getAppLabelText(pm, this.packageName, ai.hwLabelRes, ai);
            }
            if (label == null) {
                label = pm.getText(this.packageName, ai.labelRes, ai);
            }
            if (label != null) {
                return label;
            }
        }
        return this.name;
    }

    public boolean isEnabled() {
        return this.enabled && this.applicationInfo.enabled;
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

    /* access modifiers changed from: protected */
    public void dumpFront(Printer pw, String prefix) {
        super.dumpFront(pw, prefix);
        if (this.processName != null && !this.packageName.equals(this.processName)) {
            pw.println(prefix + "processName=" + this.processName);
        }
        if (this.splitName != null) {
            pw.println(prefix + "splitName=" + this.splitName);
        }
        pw.println(prefix + "enabled=" + this.enabled + " exported=" + this.exported + " directBootAware=" + this.directBootAware);
        if (this.descriptionRes != 0) {
            pw.println(prefix + "description=" + this.descriptionRes);
        }
    }

    /* access modifiers changed from: protected */
    public void dumpBack(Printer pw, String prefix) {
        dumpBack(pw, prefix, 3);
    }

    /* access modifiers changed from: package-private */
    public void dumpBack(Printer pw, String prefix, int dumpFlags) {
        if ((dumpFlags & 2) != 0) {
            if (this.applicationInfo != null) {
                pw.println(prefix + "ApplicationInfo:");
                ApplicationInfo applicationInfo2 = this.applicationInfo;
                applicationInfo2.dump(pw, prefix + "  ", dumpFlags);
            } else {
                pw.println(prefix + "ApplicationInfo: null");
            }
        }
        super.dumpBack(pw, prefix);
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        super.writeToParcel(dest, parcelableFlags);
        if ((parcelableFlags & 2) != 0) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            this.applicationInfo.writeToParcel(dest, parcelableFlags);
        }
        dest.writeString(this.processName);
        dest.writeString(this.splitName);
        dest.writeInt(this.descriptionRes);
        dest.writeInt(this.enabled ? 1 : 0);
        dest.writeInt(this.exported ? 1 : 0);
        dest.writeInt(this.directBootAware ? 1 : 0);
    }

    protected ComponentInfo(Parcel source) {
        super(source);
        boolean z = true;
        if (source.readInt() != 0) {
            this.applicationInfo = ApplicationInfo.CREATOR.createFromParcel(source);
        }
        this.processName = source.readString();
        this.splitName = source.readString();
        this.descriptionRes = source.readInt();
        this.enabled = source.readInt() != 0;
        this.exported = source.readInt() != 0;
        z = source.readInt() == 0 ? false : z;
        this.directBootAware = z;
        this.encryptionAware = z;
    }

    public Drawable loadDefaultIcon(PackageManager pm) {
        return this.applicationInfo.loadIcon(pm);
    }

    /* access modifiers changed from: protected */
    public Drawable loadDefaultBanner(PackageManager pm) {
        return this.applicationInfo.loadBanner(pm);
    }

    /* access modifiers changed from: protected */
    public Drawable loadDefaultLogo(PackageManager pm) {
        return this.applicationInfo.loadLogo(pm);
    }

    /* access modifiers changed from: protected */
    public ApplicationInfo getApplicationInfo() {
        return this.applicationInfo;
    }
}
