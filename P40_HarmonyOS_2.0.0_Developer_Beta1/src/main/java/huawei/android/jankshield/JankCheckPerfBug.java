package huawei.android.jankshield;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import java.util.ArrayList;

public class JankCheckPerfBug implements Parcelable {
    public static final Parcelable.Creator<JankCheckPerfBug> CREATOR = new Parcelable.Creator<JankCheckPerfBug>() {
        /* class huawei.android.jankshield.JankCheckPerfBug.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public JankCheckPerfBug createFromParcel(Parcel in) {
            return new JankCheckPerfBug(in);
        }

        @Override // android.os.Parcelable.Creator
        public JankCheckPerfBug[] newArray(int size) {
            return new JankCheckPerfBug[size];
        }
    };
    private static final int MAX_CHECK_ITEMS = 6;
    private static final String[] PERF_KILLER_APPS = {"com.qihoo360.mobilesafe", "com.qihoo.vpnmaster", "com.qihoo.antivirus", "com.tencent.qqpimsecure", "com.tencent.powermanager", "com.tencent.token"};
    private static final String TAG = "JankShield";
    public boolean hasInstalledPerfKillerApps;
    public boolean isEnableAccessibility;
    public boolean isEnableDaltonizer;
    public boolean isEnableZoomGesture;
    public boolean isHighTextContrastEnabled;
    public boolean isTouchExplorationEnabled;

    public JankCheckPerfBug() {
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(6);
        dest.writeInt(this.hasInstalledPerfKillerApps ? 1 : 0);
        dest.writeInt(this.isEnableAccessibility ? 1 : 0);
        dest.writeInt(this.isTouchExplorationEnabled ? 1 : 0);
        dest.writeInt(this.isHighTextContrastEnabled ? 1 : 0);
        dest.writeInt(this.isEnableZoomGesture ? 1 : 0);
        dest.writeInt(this.isEnableDaltonizer ? 1 : 0);
    }

    public String getAppLabel(String pkgName, PackageManager pm) {
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(pkgName, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Could not get ApplicationInfo");
            return null;
        }
    }

    public ArrayList<String> hasInstalledPerfKillerApps(Context context) {
        ArrayList<String> result = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        int i = 0;
        while (true) {
            String[] strArr = PERF_KILLER_APPS;
            if (i >= strArr.length) {
                return result;
            }
            if (pm.isPackageAvailable(strArr[i])) {
                result.add(getAppLabel(PERF_KILLER_APPS[i], pm));
            }
            i++;
        }
    }

    public void checkPerfBug(Context context) {
        if (context.getSystemService("accessibility") instanceof AccessibilityManager) {
            AccessibilityManager am = (AccessibilityManager) context.getSystemService("accessibility");
            this.isEnableAccessibility = am.isEnabled();
            this.isTouchExplorationEnabled = am.isTouchExplorationEnabled();
            this.isHighTextContrastEnabled = am.isHighTextContrastEnabled();
        }
        boolean z = true;
        if (hasInstalledPerfKillerApps(context).size() > 0) {
            this.hasInstalledPerfKillerApps = true;
        } else {
            this.hasInstalledPerfKillerApps = false;
        }
        this.isEnableZoomGesture = Settings.Secure.getInt(context.getContentResolver(), "accessibility_display_magnification_enabled", 0) == 1;
        if (Settings.Secure.getInt(context.getContentResolver(), "accessibility_display_daltonizer_enabled", 0) != 1) {
            z = false;
        }
        this.isEnableDaltonizer = z;
    }

    private JankCheckPerfBug(Parcel in) {
        if (in.readInt() >= 6) {
            boolean z = true;
            this.hasInstalledPerfKillerApps = in.readInt() != 0;
            this.isEnableAccessibility = in.readInt() != 0;
            this.isTouchExplorationEnabled = in.readInt() != 0;
            this.isHighTextContrastEnabled = in.readInt() != 0;
            this.isEnableZoomGesture = in.readInt() != 0;
            this.isEnableDaltonizer = in.readInt() == 0 ? false : z;
        }
    }
}
