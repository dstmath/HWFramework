package android.content.pm;

import android.annotation.SystemApi;
import android.common.HwFrameworkFactory;
import android.common.HwPackageManager;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.Bundle;
import android.os.Parcel;
import android.text.Html;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Printer;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.Collator;
import java.util.BitSet;
import java.util.Comparator;

public class PackageItemInfo {
    public static final int DUMP_FLAG_ALL = 3;
    public static final int DUMP_FLAG_APPLICATION = 2;
    public static final int DUMP_FLAG_DETAILS = 1;
    private static final int LINE_FEED_CODE_POINT = 10;
    private static final float MAX_LABEL_SIZE_PX = 500.0f;
    private static final int MAX_SAFE_LABEL_LENGTH = 50000;
    private static final int NBSP_CODE_POINT = 160;
    public static final int SAFE_LABEL_FLAG_FIRST_LINE = 4;
    public static final int SAFE_LABEL_FLAG_SINGLE_LINE = 2;
    public static final int SAFE_LABEL_FLAG_TRIM = 1;
    private static volatile boolean sForceSafeLabels = false;
    public int banner;
    public int hwLabelRes;
    public int icon;
    public int labelRes;
    public int logo;
    public Bundle metaData;
    public String name;
    public CharSequence nonLocalizedLabel;
    public String packageName;
    public int showUserIcon;

    public static class DisplayNameComparator implements Comparator<PackageItemInfo> {
        private PackageManager mPM;
        private final Collator sCollator = Collator.getInstance();

        public DisplayNameComparator(PackageManager pm) {
            this.mPM = pm;
        }

        public final int compare(PackageItemInfo aa, PackageItemInfo ab) {
            CharSequence sa = aa.loadLabel(this.mPM);
            if (sa == null) {
                sa = aa.name;
            }
            CharSequence sb = ab.loadLabel(this.mPM);
            if (sb == null) {
                sb = ab.name;
            }
            return this.sCollator.compare(sa.toString(), sb.toString());
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SafeLabelFlags {
    }

    private static class StringWithRemovedChars {
        private final String mOriginal;
        private BitSet mRemovedChars;

        StringWithRemovedChars(String original) {
            this.mOriginal = original;
        }

        /* access modifiers changed from: package-private */
        public void removeRange(int firstRemoved, int firstNonRemoved) {
            if (this.mRemovedChars == null) {
                this.mRemovedChars = new BitSet(this.mOriginal.length());
            }
            this.mRemovedChars.set(firstRemoved, firstNonRemoved);
        }

        /* access modifiers changed from: package-private */
        public void removeAllCharBefore(int firstNonRemoved) {
            if (this.mRemovedChars == null) {
                this.mRemovedChars = new BitSet(this.mOriginal.length());
            }
            this.mRemovedChars.set(0, firstNonRemoved);
        }

        /* access modifiers changed from: package-private */
        public void removeAllCharAfter(int firstRemoved) {
            if (this.mRemovedChars == null) {
                this.mRemovedChars = new BitSet(this.mOriginal.length());
            }
            this.mRemovedChars.set(firstRemoved, this.mOriginal.length());
        }

        public String toString() {
            if (this.mRemovedChars == null) {
                return this.mOriginal;
            }
            StringBuilder sb = new StringBuilder(this.mOriginal.length());
            for (int i = 0; i < this.mOriginal.length(); i++) {
                if (!this.mRemovedChars.get(i)) {
                    sb.append(this.mOriginal.charAt(i));
                }
            }
            return sb.toString();
        }

        /* access modifiers changed from: package-private */
        public int length() {
            return this.mOriginal.length();
        }

        /* access modifiers changed from: package-private */
        public boolean isRemoved(int offset) {
            return this.mRemovedChars != null && this.mRemovedChars.get(offset);
        }

        /* access modifiers changed from: package-private */
        public int codePointAt(int offset) {
            return this.mOriginal.codePointAt(offset);
        }
    }

    public static void setForceSafeLabels(boolean forceSafeLabels) {
        sForceSafeLabels = forceSafeLabels;
    }

    public PackageItemInfo() {
        this.showUserIcon = UserInfo.NO_PROFILE_GROUP_ID;
    }

    public PackageItemInfo(PackageItemInfo orig) {
        this.name = orig.name;
        if (this.name != null) {
            this.name = this.name.trim();
        }
        this.packageName = orig.packageName;
        this.labelRes = orig.labelRes;
        this.hwLabelRes = orig.hwLabelRes;
        this.nonLocalizedLabel = orig.nonLocalizedLabel;
        if (this.nonLocalizedLabel != null) {
            this.nonLocalizedLabel = this.nonLocalizedLabel.toString().trim();
        }
        this.icon = orig.icon;
        this.banner = orig.banner;
        this.logo = orig.logo;
        this.metaData = orig.metaData;
        this.showUserIcon = orig.showUserIcon;
    }

    public CharSequence loadLabel(PackageManager pm) {
        if (sForceSafeLabels) {
            return loadSafeLabel(pm);
        }
        return loadUnsafeLabel(pm);
    }

    public CharSequence loadUnsafeLabel(PackageManager pm) {
        if (this.nonLocalizedLabel != null) {
            return this.nonLocalizedLabel;
        }
        if (this.labelRes != 0) {
            ApplicationInfo ai = getApplicationInfo();
            CharSequence label = null;
            HwPackageManager hpm = HwFrameworkFactory.getHwPackageManager();
            if (!(ai == null || ai.hwLabelRes == 0 || hpm == null)) {
                label = hpm.getAppLabelText(pm, this.packageName, ai.hwLabelRes, ai);
            }
            if (label == null) {
                label = pm.getText(this.packageName, this.labelRes, getApplicationInfo());
            }
            if (label != null) {
                return label.toString().trim();
            }
        }
        if (this.name != null) {
            return this.name;
        }
        return this.packageName;
    }

    @SystemApi
    public CharSequence loadSafeLabel(PackageManager pm) {
        String labelStr = Html.fromHtml(loadUnsafeLabel(pm).toString()).toString();
        int labelLength = Math.min(labelStr.length(), 50000);
        StringBuffer sb = new StringBuffer(labelLength);
        int offset = 0;
        while (true) {
            if (offset >= labelLength) {
                break;
            }
            int codePoint = labelStr.codePointAt(offset);
            int type = Character.getType(codePoint);
            if (type == 13 || type == 15 || type == 14) {
                String labelStr2 = labelStr.substring(0, offset);
            } else {
                int charCount = Character.charCount(codePoint);
                if (type == 12) {
                    sb.append(' ');
                } else {
                    sb.append(labelStr.charAt(offset));
                    if (charCount == 2) {
                        sb.append(labelStr.charAt(offset + 1));
                    }
                }
                offset += charCount;
            }
        }
        String labelStr22 = labelStr.substring(0, offset);
        String labelStr3 = sb.toString().trim();
        if (labelStr3.isEmpty()) {
            return this.packageName;
        }
        TextPaint paint = new TextPaint();
        paint.setTextSize(42.0f);
        return TextUtils.ellipsize(labelStr3, paint, MAX_LABEL_SIZE_PX, TextUtils.TruncateAt.END);
    }

    private static boolean isNewline(int codePoint) {
        int type = Character.getType(codePoint);
        return type == 14 || type == 13 || codePoint == 10;
    }

    private static boolean isWhiteSpace(int codePoint) {
        return Character.isWhitespace(codePoint) || codePoint == 160;
    }

    public CharSequence loadSafeLabel(PackageManager pm, float ellipsizeDip, int flags) {
        float f = ellipsizeDip;
        int i = flags;
        boolean z = true;
        boolean onlyKeepFirstLine = (i & 4) != 0;
        boolean forceSingleLine = (i & 2) != 0;
        boolean trim = (i & 1) != 0;
        Preconditions.checkNotNull(pm);
        Preconditions.checkArgument(f >= 0.0f);
        Preconditions.checkFlagsArgument(i, 7);
        if (onlyKeepFirstLine && forceSingleLine) {
            z = false;
        }
        Preconditions.checkArgument(z, "Cannot set SAFE_LABEL_FLAG_SINGLE_LINE and SAFE_LABEL_FLAG_FIRST_LINE at the same time");
        StringWithRemovedChars labelStr = new StringWithRemovedChars(Html.fromHtml(loadUnsafeLabel(pm).toString()).toString());
        int labelLength = labelStr.length();
        int firstTrailingWhiteSpace = -1;
        int firstNonWhiteSpace = -1;
        int offset = 0;
        while (true) {
            if (offset >= labelLength) {
                break;
            }
            int codePoint = labelStr.codePointAt(offset);
            int type = Character.getType(codePoint);
            int codePointLen = Character.charCount(codePoint);
            boolean isNewline = isNewline(codePoint);
            if (offset > 50000 || (onlyKeepFirstLine && isNewline)) {
                labelStr.removeAllCharAfter(offset);
            } else {
                if (forceSingleLine && isNewline) {
                    labelStr.removeRange(offset, offset + codePointLen);
                } else if (type == 15 && !isNewline) {
                    labelStr.removeRange(offset, offset + codePointLen);
                } else if (trim && !isWhiteSpace(codePoint)) {
                    if (firstNonWhiteSpace == -1) {
                        firstNonWhiteSpace = offset;
                    }
                    firstTrailingWhiteSpace = offset + codePointLen;
                }
                offset += codePointLen;
            }
        }
        labelStr.removeAllCharAfter(offset);
        if (trim) {
            if (firstNonWhiteSpace == -1) {
                labelStr.removeAllCharAfter(0);
            } else {
                if (firstNonWhiteSpace > 0) {
                    labelStr.removeAllCharBefore(firstNonWhiteSpace);
                }
                if (firstTrailingWhiteSpace < labelLength) {
                    labelStr.removeAllCharAfter(firstTrailingWhiteSpace);
                }
            }
        }
        if (f == 0.0f) {
            return labelStr.toString();
        }
        TextPaint paint = new TextPaint();
        paint.setTextSize(42.0f);
        return TextUtils.ellipsize(labelStr.toString(), paint, f, TextUtils.TruncateAt.END);
    }

    public Drawable loadIcon(PackageManager pm) {
        int appInfoIcon = 0;
        if (this.icon == 0 && getApplicationInfo() != null) {
            appInfoIcon = getApplicationInfo().icon;
        }
        HwThemeManager.updateIconCache(this, this.name, this.packageName, this.icon, appInfoIcon);
        return pm.loadItemIcon(this, getApplicationInfo());
    }

    public Drawable loadUnbadgedIcon(PackageManager pm) {
        return pm.loadUnbadgedItemIcon(this, getApplicationInfo());
    }

    public Drawable loadBanner(PackageManager pm) {
        if (this.banner != 0) {
            Drawable dr = pm.getDrawable(this.packageName, this.banner, getApplicationInfo());
            if (dr != null) {
                return dr;
            }
        }
        return loadDefaultBanner(pm);
    }

    public Drawable loadDefaultIcon(PackageManager pm) {
        return pm.getDefaultActivityIcon();
    }

    /* access modifiers changed from: protected */
    public Drawable loadDefaultBanner(PackageManager pm) {
        return null;
    }

    public Drawable loadLogo(PackageManager pm) {
        if (this.logo != 0) {
            Drawable d = pm.getDrawable(this.packageName, this.logo, getApplicationInfo());
            if (d != null) {
                return d;
            }
        }
        return loadDefaultLogo(pm);
    }

    /* access modifiers changed from: protected */
    public Drawable loadDefaultLogo(PackageManager pm) {
        return null;
    }

    public XmlResourceParser loadXmlMetaData(PackageManager pm, String name2) {
        if (this.metaData != null) {
            int resid = this.metaData.getInt(name2);
            if (resid != 0) {
                return pm.getXml(this.packageName, resid, getApplicationInfo());
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void dumpFront(Printer pw, String prefix) {
        if (this.name != null) {
            pw.println(prefix + "name=" + this.name);
        }
        pw.println(prefix + "packageName=" + this.packageName);
        if (this.labelRes != 0 || this.nonLocalizedLabel != null || this.icon != 0 || this.banner != 0) {
            pw.println(prefix + "labelRes=0x" + Integer.toHexString(this.labelRes) + " nonLocalizedLabel=" + this.nonLocalizedLabel + " icon=0x" + Integer.toHexString(this.icon) + " banner=0x" + Integer.toHexString(this.banner));
        }
    }

    /* access modifiers changed from: protected */
    public void dumpBack(Printer pw, String prefix) {
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.name);
        dest.writeString(this.packageName);
        dest.writeInt(this.labelRes);
        dest.writeInt(this.hwLabelRes);
        TextUtils.writeToParcel(this.nonLocalizedLabel, dest, parcelableFlags);
        dest.writeInt(this.icon);
        dest.writeInt(this.logo);
        dest.writeBundle(this.metaData);
        dest.writeInt(this.banner);
        dest.writeInt(this.showUserIcon);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        if (this.name != null) {
            proto.write(1138166333441L, this.name);
        }
        proto.write(1138166333442L, this.packageName);
        if (!(this.labelRes == 0 && this.nonLocalizedLabel == null && this.icon == 0 && this.banner == 0)) {
            proto.write(1120986464259L, this.labelRes);
            proto.write(1138166333444L, this.nonLocalizedLabel.toString());
            proto.write(1120986464261L, this.icon);
            proto.write(1120986464262L, this.banner);
        }
        proto.end(token);
    }

    protected PackageItemInfo(Parcel source) {
        this.name = source.readString();
        this.packageName = source.readString();
        this.labelRes = source.readInt();
        this.hwLabelRes = source.readInt();
        this.nonLocalizedLabel = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        this.icon = source.readInt();
        this.logo = source.readInt();
        this.metaData = source.readBundle();
        this.banner = source.readInt();
        this.showUserIcon = source.readInt();
    }

    /* access modifiers changed from: protected */
    public ApplicationInfo getApplicationInfo() {
        return null;
    }
}
