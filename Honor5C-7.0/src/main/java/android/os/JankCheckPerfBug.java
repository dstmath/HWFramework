package android.os;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Parcelable.Creator;
import android.provider.Settings.Secure;
import android.view.accessibility.AccessibilityManager;
import java.util.ArrayList;

public class JankCheckPerfBug implements Parcelable {
    public static final Creator<JankCheckPerfBug> CREATOR = null;
    private static final String[] perfkillerapps = null;
    public boolean hasInstalledPerfKillerApps;
    public boolean isEnableAccessibility;
    public boolean isEnableDaltonizer;
    public boolean isEnableZoomGesture;
    public boolean isHighTextContrastEnabled;
    public boolean isTouchExplorationEnabled;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.JankCheckPerfBug.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.JankCheckPerfBug.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.JankCheckPerfBug.<clinit>():void");
    }

    private JankCheckPerfBug(Parcel in) {
        boolean z = true;
        if (in.readInt() >= 6) {
            boolean z2;
            if (in.readInt() != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.hasInstalledPerfKillerApps = z2;
            if (in.readInt() != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.isEnableAccessibility = z2;
            if (in.readInt() != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.isTouchExplorationEnabled = z2;
            if (in.readInt() != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.isHighTextContrastEnabled = z2;
            if (in.readInt() != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.isEnableZoomGesture = z2;
            if (in.readInt() == 0) {
                z = false;
            }
            this.isEnableDaltonizer = z;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeInt(6);
        if (this.hasInstalledPerfKillerApps) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.isEnableAccessibility) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.isTouchExplorationEnabled) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.isHighTextContrastEnabled) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.isEnableZoomGesture) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.isEnableDaltonizer) {
            i2 = 0;
        }
        dest.writeInt(i2);
    }

    public String getAppLabel(String pkgname, PackageManager pm) {
        String ret = null;
        try {
            return (String) pm.getApplicationLabel(pm.getApplicationInfo(pkgname, 0));
        } catch (NameNotFoundException e) {
            return ret;
        }
    }

    public ArrayList<String> hasInstalledPerfKillerApps(Context context) {
        ArrayList<String> result = new ArrayList();
        PackageManager pm = context.getPackageManager();
        for (int i = 0; i < perfkillerapps.length; i++) {
            if (pm.isPackageAvailable(perfkillerapps[i])) {
                result.add(getAppLabel(perfkillerapps[i], pm));
            }
        }
        return result;
    }

    public void checkPerfBug(Context context) {
        boolean z;
        boolean z2 = true;
        AccessibilityManager am = (AccessibilityManager) context.getSystemService("accessibility");
        this.isEnableAccessibility = am.isEnabled();
        this.isTouchExplorationEnabled = am.isTouchExplorationEnabled();
        this.isHighTextContrastEnabled = am.isHighTextContrastEnabled();
        if (hasInstalledPerfKillerApps(context).size() > 0) {
            this.hasInstalledPerfKillerApps = true;
        } else {
            this.hasInstalledPerfKillerApps = false;
        }
        if (Secure.getInt(context.getContentResolver(), "accessibility_display_magnification_enabled", 0) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.isEnableZoomGesture = z;
        if (Secure.getInt(context.getContentResolver(), "accessibility_display_daltonizer_enabled", 0) != 1) {
            z2 = false;
        }
        this.isEnableDaltonizer = z2;
    }
}
