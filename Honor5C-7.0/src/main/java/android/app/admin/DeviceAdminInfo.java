package android.app.admin;

import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.Downloads.Impl;
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
    public static final Creator<DeviceAdminInfo> CREATOR = null;
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
    static HashMap<String, Integer> sKnownPolicies;
    static ArrayList<PolicyInfo> sPoliciesDisplayOrder;
    static SparseArray<PolicyInfo> sRevKnownPolicies;
    final ActivityInfo mActivityInfo;
    private IHwDeviceAdminInfo mHwDeviceAdminInfo;
    int mUsesPolicies;
    boolean mVisible;

    public static class PolicyInfo {
        public final int description;
        public final int descriptionForSecondaryUsers;
        public final int ident;
        public final int label;
        public final int labelForSecondaryUsers;
        public final String tag;

        public PolicyInfo(int ident, String tag, int label, int description) {
            this(ident, tag, label, description, label, description);
        }

        public PolicyInfo(int ident, String tag, int label, int description, int labelForSecondaryUsers, int descriptionForSecondaryUsers) {
            this.ident = ident;
            this.tag = tag;
            this.label = label;
            this.description = description;
            this.labelForSecondaryUsers = labelForSecondaryUsers;
            this.descriptionForSecondaryUsers = descriptionForSecondaryUsers;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.admin.DeviceAdminInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.admin.DeviceAdminInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.admin.DeviceAdminInfo.<clinit>():void");
    }

    public DeviceAdminInfo(Context context, ResolveInfo resolveInfo) throws XmlPullParserException, IOException {
        this(context, resolveInfo.activityInfo);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public DeviceAdminInfo(Context context, ActivityInfo activityInfo) throws XmlPullParserException, IOException {
        this.mActivityInfo = activityInfo;
        PackageManager pm = context.getPackageManager();
        XmlResourceParser xmlResourceParser = null;
        try {
            this.mHwDeviceAdminInfo = HwFrameworkFactory.getHwDeviceAdminInfo(context, this.mActivityInfo);
            xmlResourceParser = this.mActivityInfo.loadXmlMetaData(pm, DeviceAdminReceiver.DEVICE_ADMIN_META_DATA);
            if (xmlResourceParser == null) {
                throw new XmlPullParserException("No android.app.device_admin meta-data");
            }
            int type;
            Resources res = pm.getResourcesForApplication(this.mActivityInfo.applicationInfo);
            AttributeSet attrs = Xml.asAttributeSet(xmlResourceParser);
            do {
                type = xmlResourceParser.next();
                if (type == USES_POLICY_WATCH_LOGIN) {
                    break;
                }
            } while (type != USES_POLICY_RESET_PASSWORD);
            if ("device-admin".equals(xmlResourceParser.getName())) {
                TypedArray sa = res.obtainAttributes(attrs, R.styleable.DeviceAdmin);
                this.mVisible = sa.getBoolean(USES_POLICY_LIMIT_PASSWORD, true);
                sa.recycle();
                int outerDepth = xmlResourceParser.getDepth();
                while (true) {
                    type = xmlResourceParser.next();
                    if (type == USES_POLICY_WATCH_LOGIN || (type == USES_POLICY_FORCE_LOCK && xmlResourceParser.getDepth() <= outerDepth)) {
                        if (xmlResourceParser != null) {
                            xmlResourceParser.close();
                            return;
                        }
                        return;
                    } else if (!(type == USES_POLICY_FORCE_LOCK || type == USES_POLICY_WIPE_DATA || !xmlResourceParser.getName().equals("uses-policies"))) {
                        int innerDepth = xmlResourceParser.getDepth();
                        while (true) {
                            type = xmlResourceParser.next();
                            if (type != USES_POLICY_WATCH_LOGIN && (type != USES_POLICY_FORCE_LOCK || xmlResourceParser.getDepth() > innerDepth)) {
                                if (!(type == USES_POLICY_FORCE_LOCK || type == USES_POLICY_WIPE_DATA)) {
                                    String policyName = xmlResourceParser.getName();
                                    Integer val = (Integer) sKnownPolicies.get(policyName);
                                    if (val != null) {
                                        this.mUsesPolicies |= USES_POLICY_WATCH_LOGIN << val.intValue();
                                    } else {
                                        Log.w(TAG, "Unknown tag under uses-policies of " + getComponent() + ": " + policyName);
                                    }
                                }
                            }
                        }
                    }
                }
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                    return;
                }
                return;
            }
            throw new XmlPullParserException("Meta-data does not start with device-admin tag");
        } catch (NameNotFoundException e) {
            throw new XmlPullParserException("Unable to create context for: " + this.mActivityInfo.packageName);
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
    }

    DeviceAdminInfo(Parcel source) {
        this.mActivityInfo = (ActivityInfo) ActivityInfo.CREATOR.createFromParcel(source);
        this.mUsesPolicies = source.readInt();
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

    public CharSequence loadDescription(PackageManager pm) throws NotFoundException {
        if (this.mActivityInfo.descriptionRes != 0) {
            return pm.getText(this.mActivityInfo.packageName, this.mActivityInfo.descriptionRes, this.mActivityInfo.applicationInfo);
        }
        throw new NotFoundException();
    }

    public Drawable loadIcon(PackageManager pm) {
        return this.mActivityInfo.loadIcon(pm);
    }

    public boolean isVisible() {
        return this.mVisible;
    }

    public boolean usesPolicy(int policyIdent) {
        return (this.mUsesPolicies & (USES_POLICY_WATCH_LOGIN << policyIdent)) != 0;
    }

    public String getTagForPolicy(int policyIdent) {
        return ((PolicyInfo) sRevKnownPolicies.get(policyIdent)).tag;
    }

    public ArrayList<PolicyInfo> getUsedPolicies() {
        int i;
        ArrayList<PolicyInfo> res = new ArrayList();
        for (i = USES_POLICY_LIMIT_PASSWORD; i < sPoliciesDisplayOrder.size(); i += USES_POLICY_WATCH_LOGIN) {
            PolicyInfo pi = (PolicyInfo) sPoliciesDisplayOrder.get(i);
            if (usesPolicy(pi.ident)) {
                res.add(pi);
            }
        }
        if (this.mHwDeviceAdminInfo != null) {
            ArrayList<PolicyInfo> mUsePolicies = this.mHwDeviceAdminInfo.getHwUsedPoliciesList();
            for (i = USES_POLICY_LIMIT_PASSWORD; i < mUsePolicies.size(); i += USES_POLICY_WATCH_LOGIN) {
                res.add((PolicyInfo) mUsePolicies.get(i));
            }
        }
        return res;
    }

    public void writePoliciesToXml(XmlSerializer out) throws IllegalArgumentException, IllegalStateException, IOException {
        out.attribute(null, Impl.COLUMN_FLAGS, Integer.toString(this.mUsesPolicies));
    }

    public void readPoliciesFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        this.mUsesPolicies = Integer.parseInt(parser.getAttributeValue(null, Impl.COLUMN_FLAGS));
    }

    public void dump(Printer pw, String prefix) {
        pw.println(prefix + "Receiver:");
        this.mActivityInfo.dump(pw, prefix + "  ");
    }

    public String toString() {
        return "DeviceAdminInfo{" + this.mActivityInfo.name + "}";
    }

    public void writeToParcel(Parcel dest, int flags) {
        this.mActivityInfo.writeToParcel(dest, flags);
        dest.writeInt(this.mUsesPolicies);
    }

    public int describeContents() {
        return USES_POLICY_LIMIT_PASSWORD;
    }
}
