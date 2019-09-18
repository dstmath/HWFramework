package android.nfc.cardemulation;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ApduServiceInfo implements Parcelable {
    public static final Parcelable.Creator<ApduServiceInfo> CREATOR = new Parcelable.Creator<ApduServiceInfo>() {
        public ApduServiceInfo createFromParcel(Parcel source) {
            Parcel parcel = source;
            ResolveInfo info = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(parcel);
            String description = source.readString();
            boolean isOtherCategoryServiceEnabled = false;
            boolean onHost = source.readInt() != 0;
            ArrayList<AidGroup> staticAidGroups = new ArrayList<>();
            if (source.readInt() > 0) {
                parcel.readTypedList(staticAidGroups, AidGroup.CREATOR);
            }
            ArrayList<AidGroup> dynamicAidGroups = new ArrayList<>();
            if (source.readInt() > 0) {
                parcel.readTypedList(dynamicAidGroups, AidGroup.CREATOR);
            }
            boolean requiresUnlock = source.readInt() != 0;
            int bannerResource = source.readInt();
            int uid = source.readInt();
            String settingsActivityName = source.readString();
            if (source.readInt() != 0) {
                isOtherCategoryServiceEnabled = true;
            }
            ApduServiceInfo apduServiceInfo = new ApduServiceInfo(info, onHost, description, staticAidGroups, dynamicAidGroups, requiresUnlock, bannerResource, uid, settingsActivityName);
            apduServiceInfo.setOtherCategoryServiceState(isOtherCategoryServiceEnabled);
            return apduServiceInfo;
        }

        public ApduServiceInfo[] newArray(int size) {
            return new ApduServiceInfo[size];
        }
    };
    static final String TAG = "ApduServiceInfo";
    protected int mBannerResourceId;
    protected String mDescription;
    protected HashMap<String, AidGroup> mDynamicAidGroups;
    private boolean mIsOtherCategoryServiceEnabled = false;
    protected boolean mOnHost;
    protected boolean mRequiresDeviceUnlock;
    protected ResolveInfo mService;
    protected String mSettingsActivityName;
    protected HashMap<String, AidGroup> mStaticAidGroups;
    protected int mUid;

    public ApduServiceInfo(ResolveInfo info, boolean onHost, String description, ArrayList<AidGroup> staticAidGroups, ArrayList<AidGroup> dynamicAidGroups, boolean requiresUnlock, int bannerResource, int uid, String settingsActivityName) {
        this.mService = info;
        this.mDescription = description;
        this.mStaticAidGroups = new HashMap<>();
        this.mDynamicAidGroups = new HashMap<>();
        this.mOnHost = onHost;
        this.mRequiresDeviceUnlock = requiresUnlock;
        Iterator<AidGroup> it = staticAidGroups.iterator();
        while (it.hasNext()) {
            AidGroup aidGroup = it.next();
            this.mStaticAidGroups.put(aidGroup.category, aidGroup);
        }
        Iterator<AidGroup> it2 = dynamicAidGroups.iterator();
        while (it2.hasNext()) {
            AidGroup aidGroup2 = it2.next();
            this.mDynamicAidGroups.put(aidGroup2.category, aidGroup2);
        }
        this.mBannerResourceId = bannerResource;
        this.mUid = uid;
        this.mSettingsActivityName = settingsActivityName;
    }

    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
        */
    public ApduServiceInfo(android.content.pm.PackageManager r21, android.content.pm.ResolveInfo r22, boolean r23) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
            r20 = this;
            r1 = r20
            r2 = r21
            r3 = r22
            r4 = r23
            r20.<init>()
            r0 = 0
            r1.mIsOtherCategoryServiceEnabled = r0
            android.content.pm.ServiceInfo r5 = r3.serviceInfo
            r6 = 0
            r7 = r6
            if (r4 == 0) goto L_0x002c
            java.lang.String r8 = "android.nfc.cardemulation.host_apdu_service"
            android.content.res.XmlResourceParser r8 = r5.loadXmlMetaData(r2, r8)     // Catch:{ NameNotFoundException -> 0x0029 }
            r7 = r8
            if (r7 == 0) goto L_0x001e
            goto L_0x0035
        L_0x001e:
            org.xmlpull.v1.XmlPullParserException r0 = new org.xmlpull.v1.XmlPullParserException     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r6 = "No android.nfc.cardemulation.host_apdu_service meta-data"
            r0.<init>(r6)     // Catch:{ NameNotFoundException -> 0x0029 }
            throw r0     // Catch:{ NameNotFoundException -> 0x0029 }
        L_0x0026:
            r0 = move-exception
            goto L_0x02c2
        L_0x0029:
            r0 = move-exception
            goto L_0x02a8
        L_0x002c:
            java.lang.String r8 = "android.nfc.cardemulation.off_host_apdu_service"
            android.content.res.XmlResourceParser r8 = r5.loadXmlMetaData(r2, r8)     // Catch:{ NameNotFoundException -> 0x0029 }
            r7 = r8
            if (r7 == 0) goto L_0x02a0
        L_0x0035:
            int r8 = r7.getEventType()     // Catch:{ NameNotFoundException -> 0x0029 }
        L_0x0039:
            r9 = 1
            r10 = 2
            if (r8 == r10) goto L_0x0045
            if (r8 == r9) goto L_0x0045
            int r9 = r7.next()     // Catch:{ NameNotFoundException -> 0x0029 }
            r8 = r9
            goto L_0x0039
        L_0x0045:
            java.lang.String r11 = r7.getName()     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r4 == 0) goto L_0x005c
            java.lang.String r12 = "host-apdu-service"
            boolean r12 = r12.equals(r11)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r12 == 0) goto L_0x0054
            goto L_0x005c
        L_0x0054:
            org.xmlpull.v1.XmlPullParserException r0 = new org.xmlpull.v1.XmlPullParserException     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r6 = "Meta-data does not start with <host-apdu-service> tag"
            r0.<init>(r6)     // Catch:{ NameNotFoundException -> 0x0029 }
            throw r0     // Catch:{ NameNotFoundException -> 0x0029 }
        L_0x005c:
            if (r4 != 0) goto L_0x0070
            java.lang.String r12 = "offhost-apdu-service"
            boolean r12 = r12.equals(r11)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r12 == 0) goto L_0x0068
            goto L_0x0070
        L_0x0068:
            org.xmlpull.v1.XmlPullParserException r0 = new org.xmlpull.v1.XmlPullParserException     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r6 = "Meta-data does not start with <offhost-apdu-service> tag"
            r0.<init>(r6)     // Catch:{ NameNotFoundException -> 0x0029 }
            throw r0     // Catch:{ NameNotFoundException -> 0x0029 }
        L_0x0070:
            android.content.pm.ApplicationInfo r12 = r5.applicationInfo     // Catch:{ NameNotFoundException -> 0x0029 }
            android.content.res.Resources r12 = r2.getResourcesForApplication(r12)     // Catch:{ NameNotFoundException -> 0x0029 }
            android.util.AttributeSet r13 = android.util.Xml.asAttributeSet(r7)     // Catch:{ NameNotFoundException -> 0x0029 }
            r14 = -1
            r15 = 3
            if (r4 == 0) goto L_0x00a3
            int[] r6 = com.android.internal.R.styleable.HostApduService     // Catch:{ NameNotFoundException -> 0x0029 }
            android.content.res.TypedArray r6 = r12.obtainAttributes(r13, r6)     // Catch:{ NameNotFoundException -> 0x0029 }
            r1.mService = r3     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r9 = r6.getString(r0)     // Catch:{ NameNotFoundException -> 0x0029 }
            r1.mDescription = r9     // Catch:{ NameNotFoundException -> 0x0029 }
            boolean r9 = r6.getBoolean(r10, r0)     // Catch:{ NameNotFoundException -> 0x0029 }
            r1.mRequiresDeviceUnlock = r9     // Catch:{ NameNotFoundException -> 0x0029 }
            int r9 = r6.getResourceId(r15, r14)     // Catch:{ NameNotFoundException -> 0x0029 }
            r1.mBannerResourceId = r9     // Catch:{ NameNotFoundException -> 0x0029 }
            r9 = 1
            java.lang.String r14 = r6.getString(r9)     // Catch:{ NameNotFoundException -> 0x0029 }
            r1.mSettingsActivityName = r14     // Catch:{ NameNotFoundException -> 0x0029 }
            r6.recycle()     // Catch:{ NameNotFoundException -> 0x0029 }
            goto L_0x00c3
        L_0x00a3:
            int[] r6 = com.android.internal.R.styleable.OffHostApduService     // Catch:{ NameNotFoundException -> 0x0029 }
            android.content.res.TypedArray r6 = r12.obtainAttributes(r13, r6)     // Catch:{ NameNotFoundException -> 0x0029 }
            r1.mService = r3     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r9 = r6.getString(r0)     // Catch:{ NameNotFoundException -> 0x0029 }
            r1.mDescription = r9     // Catch:{ NameNotFoundException -> 0x0029 }
            r1.mRequiresDeviceUnlock = r0     // Catch:{ NameNotFoundException -> 0x0029 }
            int r9 = r6.getResourceId(r10, r14)     // Catch:{ NameNotFoundException -> 0x0029 }
            r1.mBannerResourceId = r9     // Catch:{ NameNotFoundException -> 0x0029 }
            r9 = 1
            java.lang.String r14 = r6.getString(r9)     // Catch:{ NameNotFoundException -> 0x0029 }
            r1.mSettingsActivityName = r14     // Catch:{ NameNotFoundException -> 0x0029 }
            r6.recycle()     // Catch:{ NameNotFoundException -> 0x0029 }
        L_0x00c3:
            java.util.HashMap r6 = new java.util.HashMap     // Catch:{ NameNotFoundException -> 0x0029 }
            r6.<init>()     // Catch:{ NameNotFoundException -> 0x0029 }
            r1.mStaticAidGroups = r6     // Catch:{ NameNotFoundException -> 0x0029 }
            java.util.HashMap r6 = new java.util.HashMap     // Catch:{ NameNotFoundException -> 0x0029 }
            r6.<init>()     // Catch:{ NameNotFoundException -> 0x0029 }
            r1.mDynamicAidGroups = r6     // Catch:{ NameNotFoundException -> 0x0029 }
            r1.mOnHost = r4     // Catch:{ NameNotFoundException -> 0x0029 }
            int r6 = r7.getDepth()     // Catch:{ NameNotFoundException -> 0x0029 }
            r16 = 0
        L_0x00d9:
            r9 = r16
            int r14 = r7.next()     // Catch:{ NameNotFoundException -> 0x0029 }
            r8 = r14
            if (r14 != r15) goto L_0x00e8
            int r14 = r7.getDepth()     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r14 <= r6) goto L_0x0294
        L_0x00e8:
            r14 = 1
            if (r8 == r14) goto L_0x0294
            java.lang.String r14 = r7.getName()     // Catch:{ NameNotFoundException -> 0x0029 }
            r11 = r14
            if (r8 != r10) goto L_0x0167
            java.lang.String r14 = "aid-group"
            boolean r14 = r14.equals(r11)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r14 == 0) goto L_0x0167
            if (r9 != 0) goto L_0x0167
            int[] r14 = com.android.internal.R.styleable.AidGroup     // Catch:{ NameNotFoundException -> 0x0029 }
            android.content.res.TypedArray r14 = r12.obtainAttributes(r13, r14)     // Catch:{ NameNotFoundException -> 0x0029 }
            r10 = 1
            java.lang.String r16 = r14.getString(r10)     // Catch:{ NameNotFoundException -> 0x0029 }
            r17 = r16
            java.lang.String r16 = r14.getString(r0)     // Catch:{ NameNotFoundException -> 0x0029 }
            r18 = r16
            java.lang.String r10 = "payment"
            r0 = r17
            boolean r10 = r10.equals(r0)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r10 != 0) goto L_0x0121
            java.lang.String r10 = "other"
            r17 = r10
            r0 = r17
        L_0x0121:
            java.util.HashMap<java.lang.String, android.nfc.cardemulation.AidGroup> r10 = r1.mStaticAidGroups     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.Object r10 = r10.get(r0)     // Catch:{ NameNotFoundException -> 0x0029 }
            android.nfc.cardemulation.AidGroup r10 = (android.nfc.cardemulation.AidGroup) r10     // Catch:{ NameNotFoundException -> 0x0029 }
            r9 = r10
            if (r9 == 0) goto L_0x0159
            java.lang.String r10 = "other"
            boolean r10 = r10.equals(r0)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r10 != 0) goto L_0x0154
            java.lang.String r10 = "ApduServiceInfo"
            java.lang.StringBuilder r15 = new java.lang.StringBuilder     // Catch:{ NameNotFoundException -> 0x0029 }
            r15.<init>()     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r2 = "Not allowing multiple aid-groups in the "
            r15.append(r2)     // Catch:{ NameNotFoundException -> 0x0029 }
            r15.append(r0)     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r2 = " category"
            r15.append(r2)     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r2 = r15.toString()     // Catch:{ NameNotFoundException -> 0x0029 }
            android.util.Log.e(r10, r2)     // Catch:{ NameNotFoundException -> 0x0029 }
            r2 = 0
            r16 = r2
            goto L_0x0156
        L_0x0154:
            r16 = r9
        L_0x0156:
            r10 = r18
            goto L_0x0162
        L_0x0159:
            android.nfc.cardemulation.AidGroup r2 = new android.nfc.cardemulation.AidGroup     // Catch:{ NameNotFoundException -> 0x0029 }
            r10 = r18
            r2.<init>((java.lang.String) r0, (java.lang.String) r10)     // Catch:{ NameNotFoundException -> 0x0029 }
            r16 = r2
        L_0x0162:
            r14.recycle()     // Catch:{ NameNotFoundException -> 0x0029 }
            goto L_0x028d
        L_0x0167:
            r0 = 3
            if (r8 != r0) goto L_0x019e
            java.lang.String r2 = "aid-group"
            boolean r2 = r2.equals(r11)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r2 == 0) goto L_0x019e
            if (r9 == 0) goto L_0x019e
            java.util.List<java.lang.String> r2 = r9.aids     // Catch:{ NameNotFoundException -> 0x0029 }
            int r2 = r2.size()     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r2 <= 0) goto L_0x018e
            java.util.HashMap<java.lang.String, android.nfc.cardemulation.AidGroup> r2 = r1.mStaticAidGroups     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r10 = r9.category     // Catch:{ NameNotFoundException -> 0x0029 }
            boolean r2 = r2.containsKey(r10)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r2 != 0) goto L_0x0195
            java.util.HashMap<java.lang.String, android.nfc.cardemulation.AidGroup> r2 = r1.mStaticAidGroups     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r10 = r9.category     // Catch:{ NameNotFoundException -> 0x0029 }
            r2.put(r10, r9)     // Catch:{ NameNotFoundException -> 0x0029 }
            goto L_0x0195
        L_0x018e:
            java.lang.String r2 = "ApduServiceInfo"
            java.lang.String r10 = "Not adding <aid-group> with empty or invalid AIDs"
            android.util.Log.e(r2, r10)     // Catch:{ NameNotFoundException -> 0x0029 }
        L_0x0195:
            r16 = 0
            r15 = r0
            r0 = 0
            r2 = r21
            r10 = 2
            goto L_0x00d9
        L_0x019e:
            r2 = 2
            if (r8 != r2) goto L_0x01e9
            java.lang.String r2 = "aid-filter"
            boolean r2 = r2.equals(r11)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r2 == 0) goto L_0x01e9
            if (r9 == 0) goto L_0x01e9
            int[] r2 = com.android.internal.R.styleable.AidFilter     // Catch:{ NameNotFoundException -> 0x0029 }
            android.content.res.TypedArray r2 = r12.obtainAttributes(r13, r2)     // Catch:{ NameNotFoundException -> 0x0029 }
            r10 = 0
            java.lang.String r14 = r2.getString(r10)     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r10 = r14.toUpperCase()     // Catch:{ NameNotFoundException -> 0x0029 }
            boolean r14 = android.nfc.cardemulation.CardEmulation.isValidAid(r10)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r14 == 0) goto L_0x01ce
            java.util.List<java.lang.String> r14 = r9.aids     // Catch:{ NameNotFoundException -> 0x0029 }
            boolean r14 = r14.contains(r10)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r14 != 0) goto L_0x01ce
            java.util.List<java.lang.String> r14 = r9.aids     // Catch:{ NameNotFoundException -> 0x0029 }
            r14.add(r10)     // Catch:{ NameNotFoundException -> 0x0029 }
            goto L_0x01e4
        L_0x01ce:
            java.lang.String r14 = "ApduServiceInfo"
            java.lang.StringBuilder r15 = new java.lang.StringBuilder     // Catch:{ NameNotFoundException -> 0x0029 }
            r15.<init>()     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r0 = "Ignoring invalid or duplicate aid: "
            r15.append(r0)     // Catch:{ NameNotFoundException -> 0x0029 }
            r15.append(r10)     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r0 = r15.toString()     // Catch:{ NameNotFoundException -> 0x0029 }
            android.util.Log.e(r14, r0)     // Catch:{ NameNotFoundException -> 0x0029 }
        L_0x01e4:
            r2.recycle()     // Catch:{ NameNotFoundException -> 0x0029 }
            goto L_0x028b
        L_0x01e9:
            r0 = 2
            if (r8 != r0) goto L_0x023a
            java.lang.String r0 = "aid-prefix-filter"
            boolean r0 = r0.equals(r11)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r0 == 0) goto L_0x023a
            if (r9 == 0) goto L_0x023a
            int[] r0 = com.android.internal.R.styleable.AidFilter     // Catch:{ NameNotFoundException -> 0x0029 }
            android.content.res.TypedArray r0 = r12.obtainAttributes(r13, r0)     // Catch:{ NameNotFoundException -> 0x0029 }
            r2 = 0
            java.lang.String r10 = r0.getString(r2)     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r2 = r10.toUpperCase()     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r10 = "*"
            java.lang.String r10 = r2.concat(r10)     // Catch:{ NameNotFoundException -> 0x0029 }
            r2 = r10
            boolean r10 = android.nfc.cardemulation.CardEmulation.isValidAid(r2)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r10 == 0) goto L_0x0220
            java.util.List<java.lang.String> r10 = r9.aids     // Catch:{ NameNotFoundException -> 0x0029 }
            boolean r10 = r10.contains(r2)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r10 != 0) goto L_0x0220
            java.util.List<java.lang.String> r10 = r9.aids     // Catch:{ NameNotFoundException -> 0x0029 }
            r10.add(r2)     // Catch:{ NameNotFoundException -> 0x0029 }
            goto L_0x0236
        L_0x0220:
            java.lang.String r10 = "ApduServiceInfo"
            java.lang.StringBuilder r14 = new java.lang.StringBuilder     // Catch:{ NameNotFoundException -> 0x0029 }
            r14.<init>()     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r15 = "Ignoring invalid or duplicate aid: "
            r14.append(r15)     // Catch:{ NameNotFoundException -> 0x0029 }
            r14.append(r2)     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r14 = r14.toString()     // Catch:{ NameNotFoundException -> 0x0029 }
            android.util.Log.e(r10, r14)     // Catch:{ NameNotFoundException -> 0x0029 }
        L_0x0236:
            r0.recycle()     // Catch:{ NameNotFoundException -> 0x0029 }
            goto L_0x028b
        L_0x023a:
            r0 = 2
            if (r8 != r0) goto L_0x028b
            java.lang.String r2 = "aid-suffix-filter"
            boolean r2 = r2.equals(r11)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r2 == 0) goto L_0x028b
            if (r9 == 0) goto L_0x028b
            int[] r2 = com.android.internal.R.styleable.AidFilter     // Catch:{ NameNotFoundException -> 0x0029 }
            android.content.res.TypedArray r2 = r12.obtainAttributes(r13, r2)     // Catch:{ NameNotFoundException -> 0x0029 }
            r10 = 0
            java.lang.String r14 = r2.getString(r10)     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r14 = r14.toUpperCase()     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r15 = "#"
            java.lang.String r15 = r14.concat(r15)     // Catch:{ NameNotFoundException -> 0x0029 }
            r14 = r15
            boolean r15 = android.nfc.cardemulation.CardEmulation.isValidAid(r14)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r15 == 0) goto L_0x0271
            java.util.List<java.lang.String> r15 = r9.aids     // Catch:{ NameNotFoundException -> 0x0029 }
            boolean r15 = r15.contains(r14)     // Catch:{ NameNotFoundException -> 0x0029 }
            if (r15 != 0) goto L_0x0271
            java.util.List<java.lang.String> r15 = r9.aids     // Catch:{ NameNotFoundException -> 0x0029 }
            r15.add(r14)     // Catch:{ NameNotFoundException -> 0x0029 }
            goto L_0x0287
        L_0x0271:
            java.lang.String r15 = "ApduServiceInfo"
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ NameNotFoundException -> 0x0029 }
            r0.<init>()     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r10 = "Ignoring invalid or duplicate aid: "
            r0.append(r10)     // Catch:{ NameNotFoundException -> 0x0029 }
            r0.append(r14)     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r0 = r0.toString()     // Catch:{ NameNotFoundException -> 0x0029 }
            android.util.Log.e(r15, r0)     // Catch:{ NameNotFoundException -> 0x0029 }
        L_0x0287:
            r2.recycle()     // Catch:{ NameNotFoundException -> 0x0029 }
        L_0x028b:
            r16 = r9
        L_0x028d:
            r0 = 0
            r2 = r21
            r10 = 2
            r15 = 3
            goto L_0x00d9
        L_0x0294:
            if (r7 == 0) goto L_0x0299
            r7.close()
        L_0x0299:
            android.content.pm.ApplicationInfo r0 = r5.applicationInfo
            int r0 = r0.uid
            r1.mUid = r0
            return
        L_0x02a0:
            org.xmlpull.v1.XmlPullParserException r0 = new org.xmlpull.v1.XmlPullParserException     // Catch:{ NameNotFoundException -> 0x0029 }
            java.lang.String r2 = "No android.nfc.cardemulation.off_host_apdu_service meta-data"
            r0.<init>(r2)     // Catch:{ NameNotFoundException -> 0x0029 }
            throw r0     // Catch:{ NameNotFoundException -> 0x0029 }
        L_0x02a8:
            org.xmlpull.v1.XmlPullParserException r2 = new org.xmlpull.v1.XmlPullParserException     // Catch:{ all -> 0x0026 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0026 }
            r6.<init>()     // Catch:{ all -> 0x0026 }
            java.lang.String r8 = "Unable to create context for: "
            r6.append(r8)     // Catch:{ all -> 0x0026 }
            java.lang.String r8 = r5.packageName     // Catch:{ all -> 0x0026 }
            r6.append(r8)     // Catch:{ all -> 0x0026 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0026 }
            r2.<init>(r6)     // Catch:{ all -> 0x0026 }
            throw r2     // Catch:{ all -> 0x0026 }
        L_0x02c2:
            if (r7 == 0) goto L_0x02c7
            r7.close()
        L_0x02c7:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.cardemulation.ApduServiceInfo.<init>(android.content.pm.PackageManager, android.content.pm.ResolveInfo, boolean):void");
    }

    public ComponentName getComponent() {
        return new ComponentName(this.mService.serviceInfo.packageName, this.mService.serviceInfo.name);
    }

    public List<String> getAids() {
        ArrayList<String> aids = new ArrayList<>();
        Iterator<AidGroup> it = getAidGroups().iterator();
        while (it.hasNext()) {
            aids.addAll(it.next().aids);
        }
        return aids;
    }

    public int getAidCacheSize(String category) {
        return 0;
    }

    public List<String> getPrefixAids() {
        ArrayList<String> prefixAids = new ArrayList<>();
        Iterator<AidGroup> it = getAidGroups().iterator();
        while (it.hasNext()) {
            for (String aid : it.next().aids) {
                if (aid.endsWith("*")) {
                    prefixAids.add(aid);
                }
            }
        }
        return prefixAids;
    }

    public List<String> getSubsetAids() {
        ArrayList<String> subsetAids = new ArrayList<>();
        Iterator<AidGroup> it = getAidGroups().iterator();
        while (it.hasNext()) {
            for (String aid : it.next().aids) {
                if (aid.endsWith("#")) {
                    subsetAids.add(aid);
                }
            }
        }
        return subsetAids;
    }

    public AidGroup getDynamicAidGroupForCategory(String category) {
        return this.mDynamicAidGroups.get(category);
    }

    public boolean removeDynamicAidGroupForCategory(String category) {
        return this.mDynamicAidGroups.remove(category) != null;
    }

    public ArrayList<AidGroup> getAidGroups() {
        ArrayList<AidGroup> groups = new ArrayList<>();
        for (Map.Entry<String, AidGroup> entry : this.mDynamicAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        for (Map.Entry<String, AidGroup> entry2 : this.mStaticAidGroups.entrySet()) {
            if (!this.mDynamicAidGroups.containsKey(entry2.getKey())) {
                groups.add(entry2.getValue());
            }
        }
        return groups;
    }

    public String getCategoryForAid(String aid) {
        Iterator<AidGroup> it = getAidGroups().iterator();
        while (it.hasNext()) {
            AidGroup group = it.next();
            if (group.aids.contains(aid.toUpperCase())) {
                return group.category;
            }
        }
        return null;
    }

    public boolean hasCategory(String category) {
        return this.mStaticAidGroups.containsKey(category) || this.mDynamicAidGroups.containsKey(category);
    }

    public boolean isOnHost() {
        return this.mOnHost;
    }

    public boolean requiresUnlock() {
        return this.mRequiresDeviceUnlock;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public int getUid() {
        return this.mUid;
    }

    public void setOrReplaceDynamicAidGroup(AidGroup aidGroup) {
        this.mDynamicAidGroups.put(aidGroup.getCategory(), aidGroup);
    }

    public CharSequence loadLabel(PackageManager pm) {
        return this.mService.loadLabel(pm);
    }

    public CharSequence loadAppLabel(PackageManager pm) {
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(this.mService.resolvePackageName, 128));
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public Drawable loadIcon(PackageManager pm) {
        return this.mService.loadIcon(pm);
    }

    public Drawable loadBanner(PackageManager pm) {
        try {
            return pm.getResourcesForApplication(this.mService.serviceInfo.packageName).getDrawable(this.mBannerResourceId);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not load banner.");
            return null;
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e(TAG, "Could not load banner.");
            return null;
        }
    }

    public String getSettingsActivityName() {
        return this.mSettingsActivityName;
    }

    public String toString() {
        StringBuilder out = new StringBuilder("ApduService: ");
        out.append(getComponent());
        out.append(", description: " + this.mDescription);
        out.append(", Static AID Groups: ");
        for (AidGroup aidGroup : this.mStaticAidGroups.values()) {
            out.append(aidGroup.toString());
        }
        out.append(", Dynamic AID Groups: ");
        for (AidGroup aidGroup2 : this.mDynamicAidGroups.values()) {
            out.append(aidGroup2.toString());
        }
        return out.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApduServiceInfo)) {
            return false;
        }
        return ((ApduServiceInfo) o).getComponent().equals(getComponent());
    }

    public int hashCode() {
        return getComponent().hashCode();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        this.mService.writeToParcel(dest, flags);
        dest.writeString(this.mDescription);
        dest.writeInt(this.mOnHost ? 1 : 0);
        dest.writeInt(this.mStaticAidGroups.size());
        if (this.mStaticAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList(this.mStaticAidGroups.values()));
        }
        dest.writeInt(this.mDynamicAidGroups.size());
        if (this.mDynamicAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList(this.mDynamicAidGroups.values()));
        }
        dest.writeInt(this.mRequiresDeviceUnlock ? 1 : 0);
        dest.writeInt(this.mBannerResourceId);
        dest.writeInt(this.mUid);
        dest.writeString(this.mSettingsActivityName);
        dest.writeInt(this.mIsOtherCategoryServiceEnabled ? 1 : 0);
    }

    public boolean isServiceEnabled(String category) {
        if (!"other".equals(category)) {
            return true;
        }
        return this.mIsOtherCategoryServiceEnabled;
    }

    public void setOtherCategoryServiceState(boolean isOtherCategoryServiceEnabled) {
        this.mIsOtherCategoryServiceEnabled = isOtherCategoryServiceEnabled;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("    " + getComponent() + " (Description: " + getDescription() + ")");
        pw.println("    Static AID groups:");
        for (AidGroup group : this.mStaticAidGroups.values()) {
            pw.println("        Category: " + group.category);
            Iterator<String> it = group.aids.iterator();
            while (it.hasNext()) {
                pw.println("            AID: " + it.next());
            }
        }
        pw.println("    Dynamic AID groups:");
        for (AidGroup group2 : this.mDynamicAidGroups.values()) {
            pw.println("        Category: " + group2.category);
            Iterator<String> it2 = group2.aids.iterator();
            while (it2.hasNext()) {
                pw.println("            AID: " + it2.next());
            }
        }
        pw.println("    Settings Activity: " + this.mSettingsActivityName);
    }
}
