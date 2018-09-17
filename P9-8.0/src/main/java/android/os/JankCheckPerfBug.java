package android.os;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Parcelable.Creator;
import android.provider.Settings.Secure;
import android.view.accessibility.AccessibilityManager;
import java.util.ArrayList;

public class JankCheckPerfBug implements Parcelable {
    public static final Creator<JankCheckPerfBug> CREATOR = new Creator<JankCheckPerfBug>() {
        public JankCheckPerfBug createFromParcel(Parcel in) {
            return new JankCheckPerfBug(in, null);
        }

        public JankCheckPerfBug[] newArray(int size) {
            return new JankCheckPerfBug[size];
        }
    };
    private static final String[] perfkillerapps = new String[]{"com.qihoo360.mobilesafe", "com.qihoo.vpnmaster", "com.qihoo.antivirus", "com.tencent.qqpimsecure", "com.tencent.powermanager", "com.tencent.token"};
    public boolean hasInstalledPerfKillerApps;
    public boolean isEnableAccessibility;
    public boolean isEnableDaltonizer;
    public boolean isEnableZoomGesture;
    public boolean isHighTextContrastEnabled;
    public boolean isTouchExplorationEnabled;

    /* synthetic */ JankCheckPerfBug(Parcel in, JankCheckPerfBug -this1) {
        this(in);
    }

    private JankCheckPerfBug(Parcel in) {
        boolean z = true;
        if (in.readInt() >= 6) {
            boolean z2;
            this.hasInstalledPerfKillerApps = in.readInt() != 0;
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
        dest.writeInt(this.hasInstalledPerfKillerApps ? 1 : 0);
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
