package android.view.inputmethod;

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
    final boolean mIsVrOnly;
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

    /* JADX WARNING: Removed duplicated region for block: B:105:0x0239  */
    public InputMethodInfo(Context context, ResolveInfo service, List<InputMethodSubtype> additionalSubtypes) throws XmlPullParserException, IOException {
        boolean isAuxIme;
        int i;
        boolean isAuxIme2;
        Resources res;
        InputMethodSubtype subtype;
        ResolveInfo resolveInfo = service;
        List<InputMethodSubtype> list = additionalSubtypes;
        this.mService = resolveInfo;
        ServiceInfo si = resolveInfo.serviceInfo;
        this.mId = computeId(service);
        boolean isAuxIme3 = true;
        this.mForceDefault = false;
        PackageManager pm = context.getPackageManager();
        XmlResourceParser parser = null;
        ArrayList<InputMethodSubtype> subtypes = new ArrayList<>();
        try {
            parser = si.loadXmlMetaData(pm, InputMethod.SERVICE_META_DATA);
            if (parser != null) {
                Resources res2 = pm.getResourcesForApplication(si.applicationInfo);
                AttributeSet attrs = Xml.asAttributeSet(parser);
                while (true) {
                    int next = parser.next();
                    int type = next;
                    if (next == 1 || type == 2) {
                    }
                }
                if ("input-method".equals(parser.getName())) {
                    TypedArray sa = res2.obtainAttributes(attrs, R.styleable.InputMethod);
                    String settingsActivityComponent = sa.getString(1);
                    try {
                        boolean isVrOnly = sa.getBoolean(3, false);
                        int isDefaultResId = sa.getResourceId(0, 0);
                        boolean supportsSwitchingToNextInputMethod = sa.getBoolean(2, false);
                        sa.recycle();
                        int depth = parser.getDepth();
                        isAuxIme3 = true;
                        while (true) {
                            TypedArray sa2 = sa;
                            try {
                                int next2 = parser.next();
                                int type2 = next2;
                                isAuxIme = isAuxIme3;
                                if (next2 == 3) {
                                    try {
                                        if (parser.getDepth() <= depth) {
                                            PackageManager packageManager = pm;
                                            i = 0;
                                            break;
                                        }
                                    } catch (PackageManager.NameNotFoundException | IndexOutOfBoundsException | NumberFormatException e) {
                                        PackageManager packageManager2 = pm;
                                        isAuxIme3 = isAuxIme;
                                        try {
                                            throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                                        } catch (Throwable th) {
                                            e = th;
                                            boolean z = isAuxIme3;
                                            if (parser != null) {
                                            }
                                            throw e;
                                        }
                                    } catch (Throwable th2) {
                                        e = th2;
                                        PackageManager packageManager3 = pm;
                                        if (parser != null) {
                                        }
                                        throw e;
                                    }
                                }
                                if (type2 == 1) {
                                    i = 0;
                                    break;
                                } else if (type2 == 2) {
                                    try {
                                        if ("subtype".equals(parser.getName())) {
                                            TypedArray a = res2.obtainAttributes(attrs, R.styleable.InputMethod_Subtype);
                                            int depth2 = depth;
                                            PackageManager pm2 = pm;
                                            try {
                                                res = res2;
                                                subtype = new InputMethodSubtype.InputMethodSubtypeBuilder().setSubtypeNameResId(a.getResourceId(0, 0)).setSubtypeIconResId(a.getResourceId(1, 0)).setLanguageTag(a.getString(9)).setSubtypeLocale(a.getString(2)).setSubtypeMode(a.getString(3)).setSubtypeExtraValue(a.getString(4)).setIsAuxiliary(a.getBoolean(5, false)).setOverridesImplicitlyEnabledSubtype(a.getBoolean(6, false)).setSubtypeId(a.getInt(7, 0)).setIsAsciiCapable(a.getBoolean(8, false)).build();
                                                if (!subtype.isAuxiliary()) {
                                                    isAuxIme3 = false;
                                                } else {
                                                    isAuxIme3 = isAuxIme;
                                                }
                                            } catch (PackageManager.NameNotFoundException | IndexOutOfBoundsException | NumberFormatException e2) {
                                                isAuxIme3 = isAuxIme;
                                                throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                                            } catch (Throwable th3) {
                                                e = th3;
                                                if (parser != null) {
                                                }
                                                throw e;
                                            }
                                            try {
                                                subtypes.add(subtype);
                                                sa = sa2;
                                                depth = depth2;
                                                pm = pm2;
                                                res2 = res;
                                            } catch (PackageManager.NameNotFoundException | IndexOutOfBoundsException | NumberFormatException e3) {
                                                throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                                            }
                                        } else {
                                            int i2 = depth;
                                            PackageManager packageManager4 = pm;
                                            Resources resources = res2;
                                            throw new XmlPullParserException("Meta-data in input-method does not start with subtype tag");
                                        }
                                    } catch (PackageManager.NameNotFoundException | IndexOutOfBoundsException | NumberFormatException e4) {
                                        PackageManager packageManager5 = pm;
                                        isAuxIme3 = isAuxIme;
                                        throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                                    } catch (Throwable th4) {
                                        e = th4;
                                        PackageManager packageManager6 = pm;
                                        if (parser != null) {
                                        }
                                        throw e;
                                    }
                                } else {
                                    int i3 = depth;
                                    PackageManager packageManager7 = pm;
                                    sa = sa2;
                                    isAuxIme3 = isAuxIme;
                                }
                            } catch (PackageManager.NameNotFoundException | IndexOutOfBoundsException | NumberFormatException e5) {
                                boolean z2 = isAuxIme3;
                                PackageManager packageManager8 = pm;
                                throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                            } catch (Throwable th5) {
                                e = th5;
                                boolean z3 = isAuxIme3;
                                PackageManager packageManager9 = pm;
                                if (parser != null) {
                                }
                                throw e;
                            }
                        }
                        if (parser != null) {
                            parser.close();
                        }
                        boolean isVrOnly2 = isVrOnly;
                        if (subtypes.size() == 0) {
                            isAuxIme2 = false;
                        } else {
                            isAuxIme2 = isAuxIme;
                        }
                        if (list != null) {
                            int N = additionalSubtypes.size();
                            while (i < N) {
                                InputMethodSubtype subtype2 = list.get(i);
                                if (!subtypes.contains(subtype2)) {
                                    subtypes.add(subtype2);
                                } else {
                                    Slog.w(TAG, "Duplicated subtype definition found: " + subtype2.getLocale() + ", " + subtype2.getMode());
                                }
                                i++;
                            }
                        }
                        this.mSubtypes = new InputMethodSubtypeArray((List<InputMethodSubtype>) subtypes);
                        this.mSettingsActivityName = settingsActivityComponent;
                        this.mIsDefaultResId = isDefaultResId;
                        this.mIsAuxIme = isAuxIme2;
                        this.mSupportsSwitchingToNextInputMethod = supportsSwitchingToNextInputMethod;
                        this.mIsVrOnly = isVrOnly2;
                    } catch (PackageManager.NameNotFoundException | IndexOutOfBoundsException | NumberFormatException e6) {
                        PackageManager packageManager10 = pm;
                        isAuxIme3 = true;
                        throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                    } catch (Throwable th6) {
                        e = th6;
                        PackageManager packageManager11 = pm;
                        if (parser != null) {
                            parser.close();
                        }
                        throw e;
                    }
                } else {
                    PackageManager packageManager12 = pm;
                    Resources resources2 = res2;
                    try {
                        throw new XmlPullParserException("Meta-data does not start with input-method tag");
                    } catch (PackageManager.NameNotFoundException | IndexOutOfBoundsException | NumberFormatException e7) {
                        isAuxIme3 = true;
                        throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                    } catch (Throwable th7) {
                        e = th7;
                        if (parser != null) {
                        }
                        throw e;
                    }
                }
            } else {
                PackageManager packageManager13 = pm;
                throw new XmlPullParserException("No android.view.im meta-data");
            }
        } catch (PackageManager.NameNotFoundException | IndexOutOfBoundsException | NumberFormatException e8) {
            PackageManager packageManager14 = pm;
            throw new XmlPullParserException("Unable to create context for: " + si.packageName);
        } catch (Throwable th8) {
            e = th8;
            PackageManager packageManager15 = pm;
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
        this.mService = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(source);
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

    public boolean isAuxiliaryIme() {
        return this.mIsAuxIme;
    }

    public boolean supportsSwitchingToNextInputMethod() {
        return this.mSupportsSwitchingToNextInputMethod;
    }

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

    public int describeContents() {
        return 0;
    }
}
