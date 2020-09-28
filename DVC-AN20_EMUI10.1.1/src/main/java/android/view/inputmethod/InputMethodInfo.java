package android.view.inputmethod;

import android.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Printer;
import android.util.Slog;
import android.util.Xml;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public final class InputMethodInfo implements Parcelable {
    public static final Parcelable.Creator<InputMethodInfo> CREATOR = new Parcelable.Creator<InputMethodInfo>() {
        /* class android.view.inputmethod.InputMethodInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public InputMethodInfo createFromParcel(Parcel source) {
            return new InputMethodInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public InputMethodInfo[] newArray(int size) {
            return new InputMethodInfo[size];
        }
    };
    static final String TAG = "InputMethodInfo";
    private final boolean mForceDefault;
    final String mId;
    private final boolean mIsAuxIme;
    final int mIsDefaultResId;
    final boolean mIsVrOnly;
    final ResolveInfo mService;
    final String mSettingsActivityName;
    @UnsupportedAppUsage
    private final InputMethodSubtypeArray mSubtypes;
    private final boolean mSupportsSwitchingToNextInputMethod;

    public static String computeId(ResolveInfo service) {
        ServiceInfo si = service.serviceInfo;
        return new ComponentName(si.packageName, si.name).flattenToShortString();
    }

    public InputMethodInfo(Context context, ResolveInfo service) throws XmlPullParserException, IOException {
        this(context, service, null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:71:0x01ce, code lost:
        r5 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01f0, code lost:
        r5 = r5;
     */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x01cd A[ExcHandler: NameNotFoundException | IndexOutOfBoundsException | NumberFormatException (e java.lang.Throwable), Splitter:B:34:0x00a6] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x01ef A[ExcHandler: NameNotFoundException | IndexOutOfBoundsException | NumberFormatException (e java.lang.Throwable), Splitter:B:49:0x0146] */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01f9 A[ExcHandler: NameNotFoundException | IndexOutOfBoundsException | NumberFormatException (e java.lang.Throwable), Splitter:B:18:0x007f] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x021c  */
    public InputMethodInfo(Context context, ResolveInfo service, List<InputMethodSubtype> additionalSubtypes) throws XmlPullParserException, IOException {
        boolean isAuxIme;
        InputMethodSubtype subtype;
        this.mService = service;
        ServiceInfo si = service.serviceInfo;
        this.mId = computeId(service);
        this.mForceDefault = false;
        PackageManager pm = context.getPackageManager();
        XmlResourceParser parser = null;
        ArrayList<InputMethodSubtype> subtypes = new ArrayList<>();
        try {
            parser = si.loadXmlMetaData(pm, InputMethod.SERVICE_META_DATA);
            if (parser != null) {
                Resources res = pm.getResourcesForApplication(si.applicationInfo);
                AttributeSet attrs = Xml.asAttributeSet(parser);
                while (true) {
                    int type = parser.next();
                    if (type == 1 || type == 2) {
                    }
                }
                if ("input-method".equals(parser.getName())) {
                    TypedArray a = res.obtainAttributes(attrs, R.styleable.InputMethod);
                    String settingsActivityComponent = a.getString(1);
                    try {
                        boolean isVrOnly = a.getBoolean(3, false);
                        int isDefaultResId = a.getResourceId(0, 0);
                        boolean supportsSwitchingToNextInputMethod = a.getBoolean(2, false);
                        a.recycle();
                        int depth = parser.getDepth();
                        boolean isAuxIme2 = true;
                        while (true) {
                            try {
                                int type2 = parser.next();
                                if (type2 == 3) {
                                    try {
                                        if (parser.getDepth() <= depth) {
                                            break;
                                        }
                                    } catch (PackageManager.NameNotFoundException | IndexOutOfBoundsException | NumberFormatException e) {
                                        isAuxIme2 = isAuxIme2;
                                        try {
                                            throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                                        } catch (Throwable th) {
                                            e = th;
                                            if (parser != null) {
                                            }
                                            throw e;
                                        }
                                    } catch (Throwable th2) {
                                        e = th2;
                                        if (parser != null) {
                                            parser.close();
                                        }
                                        throw e;
                                    }
                                }
                                if (type2 == 1) {
                                    break;
                                } else if (type2 == 2) {
                                    try {
                                        if ("subtype".equals(parser.getName())) {
                                            TypedArray a2 = res.obtainAttributes(attrs, R.styleable.InputMethod_Subtype);
                                            try {
                                                subtype = new InputMethodSubtype.InputMethodSubtypeBuilder().setSubtypeNameResId(a2.getResourceId(0, 0)).setSubtypeIconResId(a2.getResourceId(1, 0)).setLanguageTag(a2.getString(9)).setSubtypeLocale(a2.getString(2)).setSubtypeMode(a2.getString(3)).setSubtypeExtraValue(a2.getString(4)).setIsAuxiliary(a2.getBoolean(5, false)).setOverridesImplicitlyEnabledSubtype(a2.getBoolean(6, false)).setSubtypeId(a2.getInt(7, 0)).setIsAsciiCapable(a2.getBoolean(8, false)).build();
                                                if (!subtype.isAuxiliary()) {
                                                    isAuxIme2 = false;
                                                } else {
                                                    isAuxIme2 = isAuxIme2;
                                                }
                                            } catch (Throwable th3) {
                                                e = th3;
                                                if (parser != null) {
                                                }
                                                throw e;
                                            }
                                            try {
                                                subtypes.add(subtype);
                                                res = res;
                                                a = a;
                                                depth = depth;
                                                pm = pm;
                                            } catch (PackageManager.NameNotFoundException | IndexOutOfBoundsException | NumberFormatException e2) {
                                                throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                                            }
                                        } else {
                                            try {
                                                throw new XmlPullParserException("Meta-data in input-method does not start with subtype tag");
                                            } catch (PackageManager.NameNotFoundException | IndexOutOfBoundsException | NumberFormatException e3) {
                                            }
                                        }
                                    } catch (PackageManager.NameNotFoundException | IndexOutOfBoundsException | NumberFormatException e4) {
                                    }
                                } else {
                                    res = res;
                                    isAuxIme2 = isAuxIme2;
                                    a = a;
                                    pm = pm;
                                }
                            } catch (PackageManager.NameNotFoundException | IndexOutOfBoundsException | NumberFormatException e5) {
                            }
                        }
                        parser.close();
                        if (subtypes.size() == 0) {
                            isAuxIme = false;
                        } else {
                            isAuxIme = isAuxIme2;
                        }
                        if (additionalSubtypes != null) {
                            int N = additionalSubtypes.size();
                            for (int i = 0; i < N; i++) {
                                InputMethodSubtype subtype2 = additionalSubtypes.get(i);
                                if (!subtypes.contains(subtype2)) {
                                    subtypes.add(subtype2);
                                } else {
                                    Slog.w(TAG, "Duplicated subtype definition found: " + subtype2.getLocale() + ", " + subtype2.getMode());
                                }
                            }
                        }
                        this.mSubtypes = new InputMethodSubtypeArray(subtypes);
                        this.mSettingsActivityName = settingsActivityComponent;
                        this.mIsDefaultResId = isDefaultResId;
                        this.mIsAuxIme = isAuxIme;
                        this.mSupportsSwitchingToNextInputMethod = supportsSwitchingToNextInputMethod;
                        this.mIsVrOnly = isVrOnly;
                    } catch (Throwable th4) {
                        e = th4;
                        if (parser != null) {
                        }
                        throw e;
                    }
                } else {
                    throw new XmlPullParserException("Meta-data does not start with input-method tag");
                }
            } else {
                throw new XmlPullParserException("No android.view.im meta-data");
            }
        } catch (Throwable th5) {
            e = th5;
            if (parser != null) {
            }
            throw e;
        }
    }

    InputMethodInfo(Parcel source) {
        this.mId = source.readString();
        this.mSettingsActivityName = source.readString();
        this.mIsDefaultResId = source.readInt();
        boolean z = true;
        this.mIsAuxIme = source.readInt() == 1;
        this.mSupportsSwitchingToNextInputMethod = source.readInt() != 1 ? false : z;
        this.mIsVrOnly = source.readBoolean();
        this.mService = ResolveInfo.CREATOR.createFromParcel(source);
        this.mSubtypes = new InputMethodSubtypeArray(source);
        this.mForceDefault = false;
    }

    public InputMethodInfo(String packageName, String className, CharSequence label, String settingsActivity) {
        this(buildDummyResolveInfo(packageName, className, label), false, settingsActivity, null, 0, false, true, false);
    }

    public InputMethodInfo(ResolveInfo ri, boolean isAuxIme, String settingsActivity, List<InputMethodSubtype> subtypes, int isDefaultResId, boolean forceDefault) {
        this(ri, isAuxIme, settingsActivity, subtypes, isDefaultResId, forceDefault, true, false);
    }

    public InputMethodInfo(ResolveInfo ri, boolean isAuxIme, String settingsActivity, List<InputMethodSubtype> subtypes, int isDefaultResId, boolean forceDefault, boolean supportsSwitchingToNextInputMethod, boolean isVrOnly) {
        ServiceInfo si = ri.serviceInfo;
        this.mService = ri;
        this.mId = new ComponentName(si.packageName, si.name).flattenToShortString();
        this.mSettingsActivityName = settingsActivity;
        this.mIsDefaultResId = isDefaultResId;
        this.mIsAuxIme = isAuxIme;
        this.mSubtypes = new InputMethodSubtypeArray(subtypes);
        this.mForceDefault = forceDefault;
        this.mSupportsSwitchingToNextInputMethod = supportsSwitchingToNextInputMethod;
        this.mIsVrOnly = isVrOnly;
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

    public boolean isVrOnly() {
        return this.mIsVrOnly;
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

    @UnsupportedAppUsage
    public boolean isDefault(Context context) {
        if (this.mForceDefault) {
            return true;
        }
        try {
            if (getIsDefaultResourceId() == 0) {
                return false;
            }
            return context.createPackageContext(getPackageName(), 0).getResources().getBoolean(getIsDefaultResourceId());
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            return false;
        }
    }

    public void dump(Printer pw, String prefix) {
        pw.println(prefix + "mId=" + this.mId + " mSettingsActivityName=" + this.mSettingsActivityName + " mIsVrOnly=" + this.mIsVrOnly + " mSupportsSwitchingToNextInputMethod=" + this.mSupportsSwitchingToNextInputMethod);
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("mIsDefaultResId=0x");
        sb.append(Integer.toHexString(this.mIsDefaultResId));
        pw.println(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append(prefix);
        sb2.append("Service:");
        pw.println(sb2.toString());
        ResolveInfo resolveInfo = this.mService;
        resolveInfo.dump(pw, prefix + "  ");
    }

    public String toString() {
        return "InputMethodInfo{" + this.mId + ", settings: " + this.mSettingsActivityName + "}";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o != null && (o instanceof InputMethodInfo)) {
            return this.mId.equals(((InputMethodInfo) o).mId);
        }
        return false;
    }

    public int hashCode() {
        return this.mId.hashCode();
    }

    public boolean isSystem() {
        return (this.mService.serviceInfo.applicationInfo.flags & 1) != 0;
    }

    public boolean isAuxiliaryIme() {
        return this.mIsAuxIme;
    }

    public boolean supportsSwitchingToNextInputMethod() {
        return this.mSupportsSwitchingToNextInputMethod;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
        dest.writeString(this.mSettingsActivityName);
        dest.writeInt(this.mIsDefaultResId);
        dest.writeInt(this.mIsAuxIme ? 1 : 0);
        dest.writeInt(this.mSupportsSwitchingToNextInputMethod ? 1 : 0);
        dest.writeBoolean(this.mIsVrOnly);
        this.mService.writeToParcel(dest, flags);
        this.mSubtypes.writeToParcel(dest);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
