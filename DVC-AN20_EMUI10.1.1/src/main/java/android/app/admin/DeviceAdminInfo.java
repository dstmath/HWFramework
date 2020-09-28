package android.app.admin;

import android.annotation.UnsupportedAppUsage;
import android.app.admin.IHwDeviceAdminInfo;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Printer;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class DeviceAdminInfo implements Parcelable {
    public static final Parcelable.Creator<DeviceAdminInfo> CREATOR = new Parcelable.Creator<DeviceAdminInfo>() {
        /* class android.app.admin.DeviceAdminInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DeviceAdminInfo createFromParcel(Parcel source) {
            return new DeviceAdminInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public DeviceAdminInfo[] newArray(int size) {
            return new DeviceAdminInfo[size];
        }
    };
    static final String TAG = "DeviceAdminInfo";
    public static final int USES_ENCRYPTED_STORAGE = 7;
    public static final int USES_POLICY_DEVICE_OWNER = -2;
    public static final int USES_POLICY_DISABLE_CAMERA = 8;
    public static final int USES_POLICY_DISABLE_KEYGUARD_FEATURES = 9;
    public static final int USES_POLICY_EXPIRE_PASSWORD = 6;
    public static final int USES_POLICY_FORCE_LOCK = 3;
    public static final int USES_POLICY_LIMIT_PASSWORD = 0;
    public static final int USES_POLICY_PROFILE_OWNER = -1;
    public static final int USES_POLICY_RESET_PASSWORD = 2;
    public static final int USES_POLICY_SETS_GLOBAL_PROXY = 5;
    public static final int USES_POLICY_WATCH_LOGIN = 1;
    public static final int USES_POLICY_WIPE_DATA = 4;
    static HashMap<String, Integer> sKnownPolicies = new HashMap<>();
    static ArrayList<PolicyInfo> sPoliciesDisplayOrder = new ArrayList<>();
    static SparseArray<PolicyInfo> sRevKnownPolicies = new SparseArray<>();
    final ActivityInfo mActivityInfo;
    private IHwDeviceAdminInfo mHwDeviceAdminInfo;
    boolean mSupportsTransferOwnership;
    int mUsesPolicies;
    boolean mVisible;

    public static class PolicyInfo {
        public final int description;
        public final int descriptionForSecondaryUsers;
        public final int ident;
        public final int label;
        public final int labelForSecondaryUsers;
        public final String tag;

        public PolicyInfo(int ident2, String tag2, int label2, int description2) {
            this(ident2, tag2, label2, description2, label2, description2);
        }

        public PolicyInfo(int ident2, String tag2, int label2, int description2, int labelForSecondaryUsers2, int descriptionForSecondaryUsers2) {
            this.ident = ident2;
            this.tag = tag2;
            this.label = label2;
            this.description = description2;
            this.labelForSecondaryUsers = labelForSecondaryUsers2;
            this.descriptionForSecondaryUsers = descriptionForSecondaryUsers2;
        }
    }

    static {
        sPoliciesDisplayOrder.add(new PolicyInfo(4, "wipe-data", R.string.policylab_wipeData, R.string.policydesc_wipeData, R.string.policylab_wipeData_secondaryUser, R.string.policydesc_wipeData_secondaryUser));
        sPoliciesDisplayOrder.add(new PolicyInfo(2, "reset-password", R.string.policylab_resetPassword, R.string.policydesc_resetPassword));
        sPoliciesDisplayOrder.add(new PolicyInfo(0, "limit-password", R.string.policylab_limitPassword, R.string.policydesc_limitPassword));
        sPoliciesDisplayOrder.add(new PolicyInfo(1, "watch-login", R.string.policylab_watchLogin, R.string.policydesc_watchLogin, R.string.policylab_watchLogin, R.string.policydesc_watchLogin_secondaryUser));
        sPoliciesDisplayOrder.add(new PolicyInfo(3, "force-lock", R.string.policylab_forceLock, R.string.policydesc_forceLock));
        sPoliciesDisplayOrder.add(new PolicyInfo(5, "set-global-proxy", R.string.policylab_setGlobalProxy, R.string.policydesc_setGlobalProxy));
        sPoliciesDisplayOrder.add(new PolicyInfo(6, "expire-password", R.string.policylab_expirePassword, R.string.policydesc_expirePassword));
        sPoliciesDisplayOrder.add(new PolicyInfo(7, "encrypted-storage", R.string.policylab_encryptedStorage, R.string.policydesc_encryptedStorage));
        sPoliciesDisplayOrder.add(new PolicyInfo(8, "disable-camera", R.string.policylab_disableCamera, R.string.policydesc_disableCamera));
        sPoliciesDisplayOrder.add(new PolicyInfo(9, "disable-keyguard-features", R.string.policylab_disableKeyguardFeatures, R.string.policydesc_disableKeyguardFeatures));
        for (int i = 0; i < sPoliciesDisplayOrder.size(); i++) {
            PolicyInfo pi = sPoliciesDisplayOrder.get(i);
            sRevKnownPolicies.put(pi.ident, pi);
            sKnownPolicies.put(pi.tag, Integer.valueOf(pi.ident));
        }
    }

    public DeviceAdminInfo(Context context, ResolveInfo resolveInfo) throws XmlPullParserException, IOException {
        this(context, resolveInfo.activityInfo);
    }

    /* JADX WARNING: Removed duplicated region for block: B:77:0x0168  */
    public DeviceAdminInfo(Context context, ActivityInfo activityInfo) throws XmlPullParserException, IOException {
        int i;
        Resources res;
        int i2;
        Resources res2;
        int i3;
        Resources res3;
        this.mActivityInfo = activityInfo;
        PackageManager pm = context.getPackageManager();
        XmlResourceParser parser = null;
        try {
            try {
                this.mHwDeviceAdminInfo = HwFrameworkFactory.getHwDeviceAdminInfo(context, this.mActivityInfo);
                XmlResourceParser parser2 = this.mActivityInfo.loadXmlMetaData(pm, DeviceAdminReceiver.DEVICE_ADMIN_META_DATA);
                if (parser2 != null) {
                    Resources res4 = pm.getResourcesForApplication(this.mActivityInfo.applicationInfo);
                    AttributeSet attrs = Xml.asAttributeSet(parser2);
                    while (true) {
                        int type = parser2.next();
                        i = 1;
                        if (type == 1 || type == 2) {
                        }
                    }
                    if ("device-admin".equals(parser2.getName())) {
                        TypedArray sa = res4.obtainAttributes(attrs, R.styleable.DeviceAdmin);
                        this.mVisible = sa.getBoolean(0, true);
                        sa.recycle();
                        int outerDepth = parser2.getDepth();
                        while (true) {
                            int type2 = parser2.next();
                            if (type2 == i) {
                                break;
                            }
                            int i4 = 3;
                            if (type2 == 3 && parser2.getDepth() <= outerDepth) {
                                break;
                            }
                            if (type2 == 3) {
                                res = res4;
                                i2 = i;
                            } else if (type2 == 4) {
                                res = res4;
                                i2 = i;
                            } else {
                                String tagName = parser2.getName();
                                if (tagName.equals("uses-policies")) {
                                    int innerDepth = parser2.getDepth();
                                    while (true) {
                                        int type3 = parser2.next();
                                        if (type3 == i) {
                                            res2 = res4;
                                            break;
                                        }
                                        if (type3 == i4) {
                                            if (parser2.getDepth() <= innerDepth) {
                                                res2 = res4;
                                                break;
                                            }
                                        }
                                        if (type3 == i4) {
                                            res3 = res4;
                                        } else if (type3 == 4) {
                                            res3 = res4;
                                        } else {
                                            String policyName = parser2.getName();
                                            Integer val = sKnownPolicies.get(policyName);
                                            if (val != null) {
                                                this.mUsesPolicies |= 1 << val.intValue();
                                                res3 = res4;
                                            } else {
                                                res3 = res4;
                                                Log.w(TAG, "Unknown tag under uses-policies of " + getComponent() + ": " + policyName);
                                            }
                                        }
                                        res4 = res3;
                                        i = 1;
                                        i4 = 3;
                                    }
                                    i3 = 1;
                                } else {
                                    res2 = res4;
                                    if (!tagName.equals("support-transfer-ownership")) {
                                        i3 = 1;
                                    } else if (parser2.next() == 3) {
                                        i3 = 1;
                                        this.mSupportsTransferOwnership = true;
                                    } else {
                                        throw new XmlPullParserException("support-transfer-ownership tag must be empty.");
                                    }
                                }
                                i = i3;
                                res4 = res2;
                            }
                            i = i2;
                            res4 = res;
                        }
                        parser2.close();
                        return;
                    }
                    throw new XmlPullParserException("Meta-data does not start with device-admin tag");
                }
                throw new XmlPullParserException("No android.app.device_admin meta-data");
            } catch (PackageManager.NameNotFoundException e) {
                try {
                    throw new XmlPullParserException("Unable to create context for: " + this.mActivityInfo.packageName);
                } catch (Throwable th) {
                    e = th;
                    if (0 != 0) {
                        parser.close();
                    }
                    throw e;
                }
            }
        } catch (PackageManager.NameNotFoundException e2) {
            throw new XmlPullParserException("Unable to create context for: " + this.mActivityInfo.packageName);
        } catch (Throwable th2) {
            e = th2;
            if (0 != 0) {
            }
            throw e;
        }
    }

    DeviceAdminInfo(Parcel source) {
        this.mActivityInfo = ActivityInfo.CREATOR.createFromParcel(source);
        this.mUsesPolicies = source.readInt();
        this.mSupportsTransferOwnership = source.readBoolean();
    }

    public String getPackageName() {
        return this.mActivityInfo.packageName;
    }

    public String getReceiverName() {
        return this.mActivityInfo.name;
    }

    public ActivityInfo getActivityInfo() {
        return this.mActivityInfo;
    }

    public ComponentName getComponent() {
        return new ComponentName(this.mActivityInfo.packageName, this.mActivityInfo.name);
    }

    public CharSequence loadLabel(PackageManager pm) {
        return this.mActivityInfo.loadLabel(pm);
    }

    public CharSequence loadDescription(PackageManager pm) throws Resources.NotFoundException {
        if (this.mActivityInfo.descriptionRes != 0) {
            return pm.getText(this.mActivityInfo.packageName, this.mActivityInfo.descriptionRes, this.mActivityInfo.applicationInfo);
        }
        throw new Resources.NotFoundException();
    }

    public Drawable loadIcon(PackageManager pm) {
        return this.mActivityInfo.loadIcon(pm);
    }

    public boolean isVisible() {
        return this.mVisible;
    }

    public boolean usesPolicy(int policyIdent) {
        return (this.mUsesPolicies & (1 << policyIdent)) != 0;
    }

    public String getTagForPolicy(int policyIdent) {
        return sRevKnownPolicies.get(policyIdent).tag;
    }

    public boolean supportsTransferOwnership() {
        return this.mSupportsTransferOwnership;
    }

    @UnsupportedAppUsage
    public ArrayList<PolicyInfo> getUsedPolicies() {
        ArrayList<PolicyInfo> res = new ArrayList<>();
        for (int i = 0; i < sPoliciesDisplayOrder.size(); i++) {
            PolicyInfo pi = sPoliciesDisplayOrder.get(i);
            if (usesPolicy(pi.ident)) {
                res.add(pi);
            }
        }
        IHwDeviceAdminInfo iHwDeviceAdminInfo = this.mHwDeviceAdminInfo;
        if (iHwDeviceAdminInfo != null) {
            ArrayList<IHwDeviceAdminInfo.PolicyInfo> mUsePolicies = iHwDeviceAdminInfo.getHwUsedPoliciesList();
            int numOfPolicies = mUsePolicies.size();
            for (int i2 = 0; i2 < numOfPolicies; i2++) {
                IHwDeviceAdminInfo.PolicyInfo hwPi = mUsePolicies.get(i2);
                res.add(new PolicyInfo(hwPi.ident, hwPi.tag, hwPi.label, hwPi.description, hwPi.labelForSecondaryUsers, hwPi.descriptionForSecondaryUsers));
            }
        }
        return res;
    }

    public void writePoliciesToXml(XmlSerializer out) throws IllegalArgumentException, IllegalStateException, IOException {
        out.attribute(null, "flags", Integer.toString(this.mUsesPolicies));
    }

    public void readPoliciesFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        try {
            this.mUsesPolicies = Integer.parseInt(parser.getAttributeValue(null, "flags"));
        } catch (NumberFormatException e) {
            Log.e("readPoliciesFromXml", "NumberFormatException");
        }
    }

    public void dump(Printer pw, String prefix) {
        pw.println(prefix + "Receiver:");
        ActivityInfo activityInfo = this.mActivityInfo;
        activityInfo.dump(pw, prefix + "  ");
    }

    public String toString() {
        return "DeviceAdminInfo{" + this.mActivityInfo.name + "}";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        this.mActivityInfo.writeToParcel(dest, flags);
        dest.writeInt(this.mUsesPolicies);
        dest.writeBoolean(this.mSupportsTransferOwnership);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
