package android.view.inputmethod;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.Printer;
import android.util.Slog;
import android.util.Xml;
import android.view.inputmethod.InputMethodSubtype.InputMethodSubtypeBuilder;
import com.android.internal.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public final class InputMethodInfo implements Parcelable {
    public static final Creator<InputMethodInfo> CREATOR = new Creator<InputMethodInfo>() {
        public InputMethodInfo createFromParcel(Parcel source) {
            return new InputMethodInfo(source);
        }

        public InputMethodInfo[] newArray(int size) {
            return new InputMethodInfo[size];
        }
    };
    static final String TAG = "InputMethodInfo";
    private final boolean mForceDefault;
    final String mId;
    private final boolean mIsAuxIme;
    final int mIsDefaultResId;
    final ResolveInfo mService;
    final String mSettingsActivityName;
    private final InputMethodSubtypeArray mSubtypes;
    private final boolean mSupportsSwitchingToNextInputMethod;

    public static String computeId(ResolveInfo service) {
        ServiceInfo si = service.serviceInfo;
        return new ComponentName(si.packageName, si.name).flattenToShortString();
    }

    public InputMethodInfo(Context context, ResolveInfo service) throws XmlPullParserException, IOException {
        this(context, service, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x0046 A:{PHI: r11 , Splitter: B:1:0x0030, ExcHandler: android.content.pm.PackageManager.NameNotFoundException (e android.content.pm.PackageManager$NameNotFoundException)} */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x0046 A:{PHI: r11 , Splitter: B:1:0x0030, ExcHandler: android.content.pm.PackageManager.NameNotFoundException (e android.content.pm.PackageManager$NameNotFoundException)} */
    /* JADX WARNING: Missing block: B:9:0x0066, code:
            throw new org.xmlpull.v1.XmlPullParserException("Unable to create context for: " + r16.packageName);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public InputMethodInfo(Context context, ResolveInfo service, List<InputMethodSubtype> additionalSubtypes) throws XmlPullParserException, IOException {
        this.mService = service;
        ServiceInfo si = service.serviceInfo;
        this.mId = computeId(service);
        boolean isAuxIme = true;
        this.mForceDefault = false;
        PackageManager pm = context.getPackageManager();
        XmlResourceParser parser = null;
        ArrayList<InputMethodSubtype> subtypes = new ArrayList();
        try {
            parser = si.loadXmlMetaData(pm, InputMethod.SERVICE_META_DATA);
            if (parser == null) {
                throw new XmlPullParserException("No android.view.im meta-data");
            }
            int type;
            Resources res = pm.getResourcesForApplication(si.applicationInfo);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            do {
                type = parser.next();
                if (type == 1) {
                    break;
                }
            } while (type != 2);
            if ("input-method".equals(parser.getName())) {
                InputMethodSubtype subtype;
                TypedArray sa = res.obtainAttributes(attrs, R.styleable.InputMethod);
                String settingsActivityComponent = sa.getString(1);
                int isDefaultResId = sa.getResourceId(0, 0);
                boolean supportsSwitchingToNextInputMethod = sa.getBoolean(2, false);
                sa.recycle();
                int depth = parser.getDepth();
                while (true) {
                    type = parser.next();
                    if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                        if (type == 2) {
                            if ("subtype".equals(parser.getName())) {
                                TypedArray a = res.obtainAttributes(attrs, R.styleable.InputMethod_Subtype);
                                subtype = new InputMethodSubtypeBuilder().setSubtypeNameResId(a.getResourceId(0, 0)).setSubtypeIconResId(a.getResourceId(1, 0)).setLanguageTag(a.getString(9)).setSubtypeLocale(a.getString(2)).setSubtypeMode(a.getString(3)).setSubtypeExtraValue(a.getString(4)).setIsAuxiliary(a.getBoolean(5, false)).setOverridesImplicitlyEnabledSubtype(a.getBoolean(6, false)).setSubtypeId(a.getInt(7, 0)).setIsAsciiCapable(a.getBoolean(8, false)).build();
                                if (!subtype.isAuxiliary()) {
                                    isAuxIme = false;
                                }
                                subtypes.add(subtype);
                            } else {
                                throw new XmlPullParserException("Meta-data in input-method does not start with subtype tag");
                            }
                        }
                    }
                }
                if (parser != null) {
                    parser.close();
                }
                if (subtypes.size() == 0) {
                    isAuxIme = false;
                }
                if (additionalSubtypes != null) {
                    int N = additionalSubtypes.size();
                    for (int i = 0; i < N; i++) {
                        subtype = (InputMethodSubtype) additionalSubtypes.get(i);
                        if (subtypes.contains(subtype)) {
                            Slog.w(TAG, "Duplicated subtype definition found: " + subtype.getLocale() + ", " + subtype.getMode());
                        } else {
                            subtypes.add(subtype);
                        }
                    }
                }
                this.mSubtypes = new InputMethodSubtypeArray((List) subtypes);
                this.mSettingsActivityName = settingsActivityComponent;
                this.mIsDefaultResId = isDefaultResId;
                this.mIsAuxIme = isAuxIme;
                this.mSupportsSwitchingToNextInputMethod = supportsSwitchingToNextInputMethod;
                return;
            }
            throw new XmlPullParserException("Meta-data does not start with input-method tag");
        } catch (NameNotFoundException e) {
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
        }
    }

    InputMethodInfo(Parcel source) {
        boolean z = true;
        this.mId = source.readString();
        this.mSettingsActivityName = source.readString();
        this.mIsDefaultResId = source.readInt();
        this.mIsAuxIme = source.readInt() == 1;
        if (source.readInt() != 1) {
            z = false;
        }
        this.mSupportsSwitchingToNextInputMethod = z;
        this.mService = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(source);
        this.mSubtypes = new InputMethodSubtypeArray(source);
        this.mForceDefault = false;
    }

    public InputMethodInfo(String packageName, String className, CharSequence label, String settingsActivity) {
        this(buildDummyResolveInfo(packageName, className, label), false, settingsActivity, null, 0, false, true);
    }

    public InputMethodInfo(ResolveInfo ri, boolean isAuxIme, String settingsActivity, List<InputMethodSubtype> subtypes, int isDefaultResId, boolean forceDefault) {
        this(ri, isAuxIme, settingsActivity, subtypes, isDefaultResId, forceDefault, true);
    }

    public InputMethodInfo(ResolveInfo ri, boolean isAuxIme, String settingsActivity, List<InputMethodSubtype> subtypes, int isDefaultResId, boolean forceDefault, boolean supportsSwitchingToNextInputMethod) {
        ServiceInfo si = ri.serviceInfo;
        this.mService = ri;
        this.mId = new ComponentName(si.packageName, si.name).flattenToShortString();
        this.mSettingsActivityName = settingsActivity;
        this.mIsDefaultResId = isDefaultResId;
        this.mIsAuxIme = isAuxIme;
        this.mSubtypes = new InputMethodSubtypeArray((List) subtypes);
        this.mForceDefault = forceDefault;
        this.mSupportsSwitchingToNextInputMethod = supportsSwitchingToNextInputMethod;
    }

    private static ResolveInfo buildDummyResolveInfo(String packageName, String className, CharSequence label) {
        ResolveInfo ri = new ResolveInfo();
        ServiceInfo si = new ServiceInfo();
        ApplicationInfo ai = new ApplicationInfo();
        ai.packageName = packageName;
        ai.enabled = true;
        si.applicationInfo = ai;
        si.enabled = true;
        si.packageName = packageName;
        si.name = className;
        si.exported = true;
        si.nonLocalizedLabel = label;
        ri.serviceInfo = si;
        return ri;
    }

    public String getId() {
        return this.mId;
    }

    public String getPackageName() {
        return this.mService.serviceInfo.packageName;
    }

    public String getServiceName() {
        return this.mService.serviceInfo.name;
    }

    public ServiceInfo getServiceInfo() {
        return this.mService.serviceInfo;
    }

    public ComponentName getComponent() {
        return new ComponentName(this.mService.serviceInfo.packageName, this.mService.serviceInfo.name);
    }

    public CharSequence loadLabel(PackageManager pm) {
        return this.mService.loadLabel(pm);
    }

    public Drawable loadIcon(PackageManager pm) {
        return this.mService.loadIcon(pm);
    }

    public String getSettingsActivity() {
        return this.mSettingsActivityName;
    }

    public int getSubtypeCount() {
        return this.mSubtypes.getCount();
    }

    public InputMethodSubtype getSubtypeAt(int index) {
        return this.mSubtypes.get(index);
    }

    public int getIsDefaultResourceId() {
        return this.mIsDefaultResId;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0024 A:{Splitter: B:4:0x0007, ExcHandler: android.content.pm.PackageManager.NameNotFoundException (e android.content.pm.PackageManager$NameNotFoundException)} */
    /* JADX WARNING: Missing block: B:11:0x0025, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isDefault(Context context) {
        if (this.mForceDefault) {
            return true;
        }
        try {
            if (getIsDefaultResourceId() == 0) {
                return false;
            }
            return context.createPackageContext(getPackageName(), 0).getResources().getBoolean(getIsDefaultResourceId());
        } catch (NameNotFoundException e) {
        }
    }

    public void dump(Printer pw, String prefix) {
        pw.println(prefix + "mId=" + this.mId + " mSettingsActivityName=" + this.mSettingsActivityName + " mSupportsSwitchingToNextInputMethod=" + this.mSupportsSwitchingToNextInputMethod);
        pw.println(prefix + "mIsDefaultResId=0x" + Integer.toHexString(this.mIsDefaultResId));
        pw.println(prefix + "Service:");
        this.mService.dump(pw, prefix + "  ");
    }

    public String toString() {
        return "InputMethodInfo{" + this.mId + ", settings: " + this.mSettingsActivityName + "}";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof InputMethodInfo)) {
            return false;
        }
        return this.mId.equals(((InputMethodInfo) o).mId);
    }

    public int hashCode() {
        return this.mId.hashCode();
    }

    public boolean isAuxiliaryIme() {
        return this.mIsAuxIme;
    }

    public boolean supportsSwitchingToNextInputMethod() {
        return this.mSupportsSwitchingToNextInputMethod;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeString(this.mId);
        dest.writeString(this.mSettingsActivityName);
        dest.writeInt(this.mIsDefaultResId);
        if (this.mIsAuxIme) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.mSupportsSwitchingToNextInputMethod) {
            i2 = 0;
        }
        dest.writeInt(i2);
        this.mService.writeToParcel(dest, flags);
        this.mSubtypes.writeToParcel(dest);
    }

    public int describeContents() {
        return 0;
    }
}
