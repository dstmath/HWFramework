package android.content.pm;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import android.security.keymaster.KeymasterDefs;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.text.TextUtils;
import android.util.Printer;
import android.util.Slog;
import java.text.Collator;
import java.util.Comparator;

public class ResolveInfo implements Parcelable {
    public static final Creator<ResolveInfo> CREATOR = null;
    private static final String TAG = "ResolveInfo";
    public ActivityInfo activityInfo;
    public ResolveInfo ephemeralInstaller;
    public EphemeralResolveInfo ephemeralResolveInfo;
    public IntentFilter filter;
    public boolean handleAllWebDataURI;
    public int icon;
    public int iconResourceId;
    public boolean isDefault;
    public int labelRes;
    public int match;
    public boolean noResourceId;
    public CharSequence nonLocalizedLabel;
    public int preferredOrder;
    public int priority;
    public ProviderInfo providerInfo;
    public String resolvePackageName;
    public ServiceInfo serviceInfo;
    public int specificIndex;
    public boolean system;
    public int targetUserId;

    public static class DisplayNameComparator implements Comparator<ResolveInfo> {
        private final Collator mCollator;
        private PackageManager mPM;

        public DisplayNameComparator(PackageManager pm) {
            this.mCollator = Collator.getInstance();
            this.mPM = pm;
            this.mCollator.setStrength(0);
        }

        public final int compare(ResolveInfo a, ResolveInfo b) {
            if (a.targetUserId != -2) {
                return 1;
            }
            if (b.targetUserId != -2) {
                return -1;
            }
            CharSequence sa = a.loadLabel(this.mPM);
            if (sa == null) {
                sa = a.activityInfo.name;
            }
            CharSequence sb = b.loadLabel(this.mPM);
            if (sb == null) {
                sb = b.activityInfo.name;
            }
            return this.mCollator.compare(sa.toString(), sb.toString());
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.ResolveInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.ResolveInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.ResolveInfo.<clinit>():void");
    }

    public ComponentInfo getComponentInfo() {
        if (this.activityInfo != null) {
            return this.activityInfo;
        }
        if (this.serviceInfo != null) {
            return this.serviceInfo;
        }
        if (this.providerInfo != null) {
            return this.providerInfo;
        }
        throw new IllegalStateException("Missing ComponentInfo!");
    }

    public CharSequence loadLabel(PackageManager pm) {
        if (this.nonLocalizedLabel != null) {
            return this.nonLocalizedLabel;
        }
        CharSequence label;
        if (!(this.resolvePackageName == null || this.labelRes == 0)) {
            label = pm.getText(this.resolvePackageName, this.labelRes, null);
            if (label != null) {
                return label.toString().trim();
            }
        }
        ComponentInfo ci = getComponentInfo();
        ApplicationInfo ai = ci.applicationInfo;
        if (this.labelRes != 0) {
            label = pm.getText(ci.packageName, this.labelRes, ai);
            if (label != null) {
                return label.toString().trim();
            }
        }
        CharSequence data = ci.loadLabel(pm);
        if (data != null) {
            data = data.toString().trim();
        }
        return data;
    }

    public Drawable loadIcon(PackageManager pm) {
        Drawable dr = null;
        HwThemeManager.updateResolveInfoIconCache(this, this.icon, this.resolvePackageName);
        if (!(this.resolvePackageName == null || this.iconResourceId == 0)) {
            dr = pm.getDrawable(this.resolvePackageName, this.iconResourceId, null);
        }
        ComponentInfo ci = getComponentInfo();
        if (dr == null && this.iconResourceId != 0) {
            dr = pm.getDrawable(ci.packageName, this.iconResourceId, ci.applicationInfo);
        }
        if (dr != null) {
            return pm.getUserBadgedIcon(dr, new UserHandle(UserHandle.myUserId()));
        }
        return ci.loadIcon(pm);
    }

    final int getIconResourceInternal() {
        HwThemeManager.updateResolveInfoIconCache(this, this.icon, null);
        if (this.iconResourceId != 0) {
            return this.iconResourceId;
        }
        ComponentInfo ci = getComponentInfo();
        if (ci != null) {
            return ci.getIconResource();
        }
        return 0;
    }

    public final int getIconResource() {
        if (this.noResourceId) {
            return 0;
        }
        return getIconResourceInternal();
    }

    public void dump(Printer pw, String prefix) {
        dump(pw, prefix, 3);
    }

    public void dump(Printer pw, String prefix, int flags) {
        if (this.filter != null) {
            pw.println(prefix + "Filter:");
            this.filter.dump(pw, prefix + "  ");
        }
        pw.println(prefix + "priority=" + this.priority + " preferredOrder=" + this.preferredOrder + " match=0x" + Integer.toHexString(this.match) + " specificIndex=" + this.specificIndex + " isDefault=" + this.isDefault);
        if (this.resolvePackageName != null) {
            pw.println(prefix + "resolvePackageName=" + this.resolvePackageName);
        }
        if (this.labelRes == 0 && this.nonLocalizedLabel == null) {
            if (this.icon != 0) {
            }
            if (this.activityInfo != null) {
                pw.println(prefix + "ActivityInfo:");
                this.activityInfo.dump(pw, prefix + "  ", flags);
            } else if (this.serviceInfo != null) {
                pw.println(prefix + "ServiceInfo:");
                this.serviceInfo.dump(pw, prefix + "  ", flags);
            } else if (this.providerInfo != null) {
                pw.println(prefix + "ProviderInfo:");
                this.providerInfo.dump(pw, prefix + "  ", flags);
            }
        }
        pw.println(prefix + "labelRes=0x" + Integer.toHexString(this.labelRes) + " nonLocalizedLabel=" + this.nonLocalizedLabel + " icon=0x" + Integer.toHexString(this.icon));
        if (this.activityInfo != null) {
            pw.println(prefix + "ActivityInfo:");
            this.activityInfo.dump(pw, prefix + "  ", flags);
        } else if (this.serviceInfo != null) {
            pw.println(prefix + "ServiceInfo:");
            this.serviceInfo.dump(pw, prefix + "  ", flags);
        } else if (this.providerInfo != null) {
            pw.println(prefix + "ProviderInfo:");
            this.providerInfo.dump(pw, prefix + "  ", flags);
        }
    }

    public ResolveInfo() {
        this.specificIndex = -1;
        this.targetUserId = -2;
    }

    public ResolveInfo(ResolveInfo orig) {
        this.specificIndex = -1;
        this.activityInfo = orig.activityInfo;
        this.serviceInfo = orig.serviceInfo;
        this.providerInfo = orig.providerInfo;
        this.filter = orig.filter;
        this.priority = orig.priority;
        this.preferredOrder = orig.preferredOrder;
        this.match = orig.match;
        this.specificIndex = orig.specificIndex;
        this.labelRes = orig.labelRes;
        this.nonLocalizedLabel = orig.nonLocalizedLabel;
        this.icon = orig.icon;
        this.resolvePackageName = orig.resolvePackageName;
        this.noResourceId = orig.noResourceId;
        this.iconResourceId = orig.iconResourceId;
        this.system = orig.system;
        this.targetUserId = orig.targetUserId;
        this.handleAllWebDataURI = orig.handleAllWebDataURI;
    }

    public String toString() {
        int i = 0;
        ComponentInfo ci = getComponentInfo();
        StringBuilder sb = new StringBuilder(KeymasterDefs.KM_ALGORITHM_HMAC);
        sb.append("ResolveInfo{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        ComponentName.appendShortString(sb, ci.packageName, ci.name);
        if (this.priority != 0) {
            sb.append(" p=");
            sb.append(this.priority);
        }
        if (this.preferredOrder != 0) {
            sb.append(" o=");
            sb.append(this.preferredOrder);
        }
        sb.append(" m=0x");
        sb.append(Integer.toHexString(this.match));
        if (this.targetUserId != -2) {
            sb.append(" targetUserId=");
            sb.append(this.targetUserId);
        }
        sb.append(" euid=");
        if (ci.applicationInfo != null) {
            i = ci.applicationInfo.euid;
        }
        sb.append(i);
        sb.append('}');
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        int i;
        int i2 = 1;
        if (this.activityInfo != null) {
            dest.writeInt(1);
            this.activityInfo.writeToParcel(dest, parcelableFlags);
        } else if (this.serviceInfo != null) {
            dest.writeInt(2);
            this.serviceInfo.writeToParcel(dest, parcelableFlags);
        } else if (this.providerInfo != null) {
            dest.writeInt(3);
            this.providerInfo.writeToParcel(dest, parcelableFlags);
        } else {
            dest.writeInt(0);
        }
        if (this.filter != null) {
            dest.writeInt(1);
            this.filter.writeToParcel(dest, parcelableFlags);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.priority);
        dest.writeInt(this.preferredOrder);
        dest.writeInt(this.match);
        dest.writeInt(this.specificIndex);
        dest.writeInt(this.labelRes);
        TextUtils.writeToParcel(this.nonLocalizedLabel, dest, parcelableFlags);
        dest.writeInt(this.icon);
        dest.writeString(this.resolvePackageName);
        dest.writeInt(this.targetUserId);
        dest.writeInt(this.system ? 1 : 0);
        if (this.noResourceId) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.iconResourceId);
        if (!this.handleAllWebDataURI) {
            i2 = 0;
        }
        dest.writeInt(i2);
    }

    private ResolveInfo(Parcel source) {
        boolean z;
        boolean z2 = true;
        this.specificIndex = -1;
        this.activityInfo = null;
        this.serviceInfo = null;
        this.providerInfo = null;
        switch (source.readInt()) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                this.activityInfo = (ActivityInfo) ActivityInfo.CREATOR.createFromParcel(source);
                break;
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                this.serviceInfo = (ServiceInfo) ServiceInfo.CREATOR.createFromParcel(source);
                break;
            case Engine.DEFAULT_STREAM /*3*/:
                this.providerInfo = (ProviderInfo) ProviderInfo.CREATOR.createFromParcel(source);
                break;
            default:
                Slog.w(TAG, "Missing ComponentInfo!");
                break;
        }
        if (source.readInt() != 0) {
            this.filter = (IntentFilter) IntentFilter.CREATOR.createFromParcel(source);
        }
        this.priority = source.readInt();
        this.preferredOrder = source.readInt();
        this.match = source.readInt();
        this.specificIndex = source.readInt();
        this.labelRes = source.readInt();
        this.nonLocalizedLabel = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        this.icon = source.readInt();
        this.resolvePackageName = source.readString();
        this.targetUserId = source.readInt();
        this.system = source.readInt() != 0;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.noResourceId = z;
        this.iconResourceId = source.readInt();
        if (source.readInt() == 0) {
            z2 = false;
        }
        this.handleAllWebDataURI = z2;
    }
}
