package com.st.android.nfc_extensions;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.cardemulation.AidGroup;
import android.nfc.cardemulation.ApduServiceInfo;
import android.nfc.cardemulation.CardEmulation;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class StApduServiceInfo implements Parcelable {
    public static final Parcelable.Creator<StApduServiceInfo> CREATOR = new Parcelable.Creator<StApduServiceInfo>() {
        /* class com.st.android.nfc_extensions.StApduServiceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public StApduServiceInfo createFromParcel(Parcel source) {
            Drawable drawable;
            Bitmap bitmap;
            ResolveInfo info = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(source);
            String description = source.readString();
            boolean onHost = source.readInt() != 0;
            ArrayList<StAidGroup> staticStAidGroups = new ArrayList<>();
            if (source.readInt() > 0) {
                source.readTypedList(staticStAidGroups, StAidGroup.CREATOR);
            }
            ArrayList<StAidGroup> dynamicStAidGroups = new ArrayList<>();
            if (source.readInt() > 0) {
                source.readTypedList(dynamicStAidGroups, StAidGroup.CREATOR);
            }
            boolean requiresUnlock = source.readInt() != 0;
            int bannerResource = source.readInt();
            int uid = source.readInt();
            String settingsActivityName = source.readString();
            ESeInfo seExtension = ESeInfo.CREATOR.createFromParcel(source);
            if (getClass().getClassLoader() == null || (bitmap = (Bitmap) source.readParcelable(getClass().getClassLoader())) == null) {
                drawable = null;
            } else {
                drawable = new BitmapDrawable(bitmap);
            }
            StApduServiceInfo service = new StApduServiceInfo(info, onHost, description, staticStAidGroups, dynamicStAidGroups, requiresUnlock, bannerResource, uid, settingsActivityName, seExtension, drawable, source.readInt() != 0);
            service.setServiceState("other", source.readInt());
            return service;
        }

        @Override // android.os.Parcelable.Creator
        public StApduServiceInfo[] newArray(int size) {
            return new StApduServiceInfo[size];
        }
    };
    static final boolean DBG = true;
    static final int POWER_STATE_BATTERY_OFF = 4;
    static final int POWER_STATE_SWITCH_OFF = 2;
    static final int POWER_STATE_SWITCH_ON = 1;
    static final String SECURE_ELEMENT_ALT_ESE = "eSE";
    static final String SECURE_ELEMENT_ALT_UICC = "UICC";
    static final String SECURE_ELEMENT_ALT_UICC2 = "UICC2";
    static final String SECURE_ELEMENT_ESE = "eSE1";
    public static final int SECURE_ELEMENT_ROUTE_ESE = 1;
    public static final int SECURE_ELEMENT_ROUTE_UICC = 2;
    public static final int SECURE_ELEMENT_ROUTE_UICC2 = 4;
    static final String SECURE_ELEMENT_UICC = "SIM";
    static final String SECURE_ELEMENT_UICC1 = "SIM1";
    static final String SECURE_ELEMENT_UICC2 = "SIM2";
    static final String ST_NFC_EXT_META_DATA = "com.gsma.services.nfc.extensions";
    static final String TAG = "APINfc_StApduServiceInfo";
    public final Drawable mBanner;
    final int mBannerResourceId;
    final String mDescription;
    final HashMap<String, StAidGroup> mDynamicStAidGroups;
    final boolean mModifiable;
    final boolean mOnHost;
    final boolean mRequiresDeviceUnlock;
    final ESeInfo mSeExtension;
    final ResolveInfo mService;
    int mServiceState;
    final String mSettingsActivityName;
    final HashMap<String, StAidGroup> mStaticStAidGroups;
    final int mUid;
    boolean mWasAccounted;

    public StApduServiceInfo(ResolveInfo info, boolean onHost, String description, ArrayList<StAidGroup> staticStAidGroups, ArrayList<StAidGroup> dynamicStAidGroups, boolean requiresUnlock, int bannerResource, int uid, String settingsActivityName, ESeInfo seExtension, Drawable banner, boolean modifiable) {
        if (!(staticStAidGroups == null || dynamicStAidGroups == null)) {
            Log.d(TAG, "Constructor - onHost: " + onHost + ", description: " + description + ", nb of staticStAidGroups: " + staticStAidGroups.size() + ", nb of dynamicStAidGroups: " + dynamicStAidGroups.size());
        }
        this.mService = info;
        this.mDescription = description;
        this.mStaticStAidGroups = new HashMap<>();
        this.mDynamicStAidGroups = new HashMap<>();
        this.mOnHost = onHost;
        this.mRequiresDeviceUnlock = requiresUnlock;
        this.mBanner = banner;
        this.mModifiable = modifiable;
        this.mServiceState = 2;
        this.mWasAccounted = DBG;
        if (staticStAidGroups != null) {
            Iterator<StAidGroup> it = staticStAidGroups.iterator();
            while (it.hasNext()) {
                StAidGroup stAidGroup = it.next();
                this.mStaticStAidGroups.put(stAidGroup.getCategory(), stAidGroup);
            }
        }
        if (dynamicStAidGroups != null) {
            Iterator<StAidGroup> it2 = dynamicStAidGroups.iterator();
            while (it2.hasNext()) {
                StAidGroup stAidGroup2 = it2.next();
                this.mDynamicStAidGroups.put(stAidGroup2.getCategory(), stAidGroup2);
            }
        }
        this.mBannerResourceId = bannerResource;
        this.mUid = uid;
        this.mSettingsActivityName = settingsActivityName;
        this.mSeExtension = seExtension;
    }

    /* JADX INFO: Multiple debug info for r11v8 'attrs'  android.util.AttributeSet: [D('attrs' android.util.AttributeSet), D('seName' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r11v12 'attrs'  android.util.AttributeSet: [D('attrs' android.util.AttributeSet), D('seName' java.lang.String)] */
    /* JADX WARNING: Code restructure failed: missing block: B:230:0x0567, code lost:
        if (r10 == null) goto L_0x059c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:232:0x056f, code lost:
        if (r10.equals(r12) != false) goto L_0x058c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:234:0x0575, code lost:
        if (r10.equals(r4) == false) goto L_0x0578;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:237:0x057e, code lost:
        if (r10.equals("SIM2") != false) goto L_0x058b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:239:0x0586, code lost:
        if (r10.equals(com.st.android.nfc_extensions.StApduServiceInfo.SECURE_ELEMENT_ALT_UICC2) == false) goto L_0x0589;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:241:0x0589, code lost:
        r13 = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:243:0x058c, code lost:
        r13 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:244:0x058d, code lost:
        r25.mSeExtension = new com.st.android.nfc_extensions.StApduServiceInfo.ESeInfo(r13, r2);
        android.util.Log.d(com.st.android.nfc_extensions.StApduServiceInfo.TAG, r25.mSeExtension.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:245:0x059c, code lost:
        r25.mSeExtension = new com.st.android.nfc_extensions.StApduServiceInfo.ESeInfo(-1, r2);
        android.util.Log.d(com.st.android.nfc_extensions.StApduServiceInfo.TAG, r25.mSeExtension.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:246:0x05ad, code lost:
        r15.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:305:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x0658  */
    public StApduServiceInfo(PackageManager pm, ResolveInfo info, boolean onHost) throws XmlPullParserException, IOException {
        XmlResourceParser extParser;
        AttributeSet attrs;
        String seName;
        String str;
        String str2;
        String str3;
        String str4;
        XmlResourceParser extParser2;
        XmlResourceParser extParser3;
        Object obj;
        String str5;
        String str6;
        String str7;
        String str8;
        XmlResourceParser extParser4;
        int depth;
        String str9;
        String str10;
        String str11;
        Log.d(TAG, "Constructor - onHost: " + onHost + ", reading from meta-data");
        this.mBanner = null;
        this.mModifiable = false;
        this.mServiceState = 2;
        ServiceInfo si = info.serviceInfo;
        XmlResourceParser parser = null;
        XmlResourceParser extParser5 = null;
        String seName2 = null;
        if (onHost) {
            try {
                parser = si.loadXmlMetaData(pm, "android.nfc.cardemulation.host_apdu_service");
                if (parser == null) {
                    throw new XmlPullParserException("No android.nfc.cardemulation.host_apdu_service meta-data");
                }
            } catch (PackageManager.NameNotFoundException e) {
                try {
                    throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                } catch (Throwable th) {
                    e = th;
                    if (parser != null) {
                    }
                    throw e;
                }
            }
        } else {
            try {
                parser = si.loadXmlMetaData(pm, "android.nfc.cardemulation.off_host_apdu_service");
                extParser5 = si.loadXmlMetaData(pm, ST_NFC_EXT_META_DATA);
                if (extParser5 == null) {
                    Log.d(TAG, "No com.gsma.services.nfc.extensions meta-data");
                }
                if (parser == null && extParser5 == null) {
                    throw new XmlPullParserException("No android.nfc.cardemulation.off_host_apdu_service meta-data");
                }
            } catch (PackageManager.NameNotFoundException e2) {
                throw new XmlPullParserException("Unable to create context for: " + si.packageName);
            } catch (Throwable th2) {
                e = th2;
                if (parser != null) {
                }
                throw e;
            }
        }
        try {
            int eventType = parser.getEventType();
            while (eventType != 2 && eventType != 1) {
                eventType = parser.next();
            }
            String tagName = parser.getName();
            if (onHost && !"host-apdu-service".equals(tagName)) {
                throw new XmlPullParserException("Meta-data does not start with <host-apdu-service> tag");
            } else if (onHost || "offhost-apdu-service".equals(tagName)) {
                Resources res = pm.getResourcesForApplication(si.applicationInfo);
                AttributeSet attrs2 = Xml.asAttributeSet(parser);
                String str12 = "SIM1";
                String str13 = SECURE_ELEMENT_UICC;
                String str14 = SECURE_ELEMENT_ESE;
                String str15 = "eSE";
                if (onHost) {
                    try {
                        attrs = attrs2;
                        try {
                            TypedArray sa = res.obtainAttributes(attrs, R.styleable.HostApduService);
                            this.mService = info;
                            this.mDescription = sa.getString(0);
                            extParser = extParser5;
                            try {
                                this.mRequiresDeviceUnlock = sa.getBoolean(2, false);
                                this.mBannerResourceId = sa.getResourceId(3, -1);
                                this.mSettingsActivityName = sa.getString(1);
                                sa.recycle();
                                seName = null;
                            } catch (PackageManager.NameNotFoundException e3) {
                                seName2 = null;
                                extParser5 = extParser;
                                throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                            } catch (Throwable th3) {
                                e = th3;
                                if (parser != null) {
                                    parser.close();
                                }
                                throw e;
                            }
                        } catch (PackageManager.NameNotFoundException e4) {
                            seName2 = null;
                            throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                        } catch (Throwable th4) {
                            e = th4;
                            if (parser != null) {
                            }
                            throw e;
                        }
                    } catch (PackageManager.NameNotFoundException e5) {
                        throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                    } catch (Throwable th5) {
                        e = th5;
                        if (parser != null) {
                        }
                        throw e;
                    }
                } else {
                    extParser = extParser5;
                    attrs = attrs2;
                    try {
                        TypedArray sa2 = res.obtainAttributes(attrs, R.styleable.OffHostApduService);
                        this.mService = info;
                        this.mDescription = sa2.getString(0);
                        this.mRequiresDeviceUnlock = false;
                        this.mBannerResourceId = sa2.getResourceId(2, -1);
                        this.mSettingsActivityName = sa2.getString(1);
                        seName = sa2.getString(3);
                        if (seName != null) {
                            try {
                                if (seName.equals(str15)) {
                                    seName = str14;
                                } else if (seName.equals(str13)) {
                                    seName = str12;
                                }
                            } catch (PackageManager.NameNotFoundException e6) {
                                seName2 = seName;
                                extParser5 = extParser;
                                throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                            } catch (Throwable th6) {
                                e = th6;
                                if (parser != null) {
                                }
                                throw e;
                            }
                        }
                        try {
                            sa2.recycle();
                        } catch (PackageManager.NameNotFoundException e7) {
                            seName2 = seName;
                            extParser5 = extParser;
                            throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                        } catch (Throwable th7) {
                            e = th7;
                            if (parser != null) {
                            }
                            throw e;
                        }
                    } catch (PackageManager.NameNotFoundException e8) {
                        extParser5 = extParser;
                        seName2 = null;
                        throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                    } catch (Throwable th8) {
                        e = th8;
                        if (parser != null) {
                        }
                        throw e;
                    }
                }
                this.mStaticStAidGroups = new HashMap<>();
                this.mDynamicStAidGroups = new HashMap<>();
                this.mOnHost = onHost;
                Log.d(TAG, "Constructor - mService: " + this.mService.resolvePackageName);
                Log.d(TAG, "Constructor - mDescription: " + this.mDescription);
                Log.d(TAG, "Constructor - mOnHost: " + this.mOnHost);
                int depth2 = parser.getDepth();
                StAidGroup currentGroup = null;
                while (true) {
                    int eventType2 = parser.next();
                    if (eventType2 != 3 || parser.getDepth() > depth2) {
                        if (eventType2 == 1) {
                            str3 = str12;
                            str4 = str15;
                            str = str14;
                            str2 = str13;
                            break;
                        }
                        String tagName2 = parser.getName();
                        if (eventType2 == 2 && "aid-group".equals(tagName2) && currentGroup == null) {
                            TypedArray groupAttrs = res.obtainAttributes(attrs, R.styleable.AidGroup);
                            String groupCategory = groupAttrs.getString(1);
                            String groupDescription = groupAttrs.getString(0);
                            String groupCategory2 = !"payment".equals(groupCategory) ? "other" : groupCategory;
                            Log.d(TAG, "Constructor - groupCategory: " + groupCategory2);
                            StAidGroup currentGroup2 = this.mStaticStAidGroups.get(groupCategory2);
                            if (currentGroup2 == null) {
                                currentGroup = new StAidGroup(groupCategory2, groupDescription);
                            } else if (!"other".equals(groupCategory2)) {
                                Log.e(TAG, "Not allowing multiple aid-groups in the " + groupCategory2 + " category");
                                currentGroup = null;
                            } else {
                                currentGroup = currentGroup2;
                            }
                            groupAttrs.recycle();
                            depth2 = depth2;
                            str15 = str15;
                            str12 = str12;
                            str13 = str13;
                            str14 = str14;
                        } else if (eventType2 != 3 || !"aid-group".equals(tagName2) || currentGroup == null) {
                            if (eventType2 == 2 && "aid-filter".equals(tagName2) && currentGroup != null) {
                                TypedArray a = res.obtainAttributes(attrs, R.styleable.AidFilter);
                                String aid = a.getString(0).toUpperCase();
                                if (!CardEmulation.isValidAid(aid) || currentGroup.aids.contains(aid)) {
                                    Log.e(TAG, "Ignoring invalid or duplicate aid: " + aid);
                                } else {
                                    currentGroup.aids.add(aid);
                                }
                                a.recycle();
                            } else if (eventType2 == 2 && "aid-prefix-filter".equals(tagName2) && currentGroup != null) {
                                TypedArray a2 = res.obtainAttributes(attrs, R.styleable.AidFilter);
                                String aid2 = a2.getString(0).toUpperCase().concat("*");
                                if (!CardEmulation.isValidAid(aid2) || currentGroup.aids.contains(aid2)) {
                                    Log.e(TAG, "Ignoring invalid or duplicate aid: " + aid2);
                                } else {
                                    currentGroup.aids.add(aid2);
                                }
                                a2.recycle();
                            } else if (eventType2 == 2 && tagName2.equals("aid-suffix-filter") && currentGroup != null) {
                                TypedArray a3 = res.obtainAttributes(attrs, R.styleable.AidFilter);
                                String aid3 = a3.getString(0).toUpperCase().concat("#");
                                if (!CardEmulation.isValidAid(aid3) || currentGroup.aids.contains(aid3)) {
                                    Log.e(TAG, "Ignoring invalid or duplicate aid: " + aid3);
                                } else {
                                    currentGroup.aids.add(aid3);
                                }
                                a3.recycle();
                            }
                            depth2 = depth2;
                            str15 = str15;
                            str12 = str12;
                            str13 = str13;
                            str14 = str14;
                        } else {
                            if (currentGroup.aids.size() <= 0) {
                                Log.e(TAG, "Not adding <aid-group> with empty or invalid AIDs");
                            } else if (!this.mStaticStAidGroups.containsKey(currentGroup.category)) {
                                this.mStaticStAidGroups.put(currentGroup.category, currentGroup);
                            }
                            currentGroup = null;
                            depth2 = depth2;
                            str15 = str15;
                            str12 = str12;
                            str13 = str13;
                            str14 = str14;
                        }
                    } else {
                        str3 = str12;
                        str4 = str15;
                        str = str14;
                        str2 = str13;
                        break;
                    }
                }
                parser.close();
                this.mUid = si.applicationInfo.uid;
                boolean aidBased = DBG;
                if (seName == null && extParser != null) {
                    Log.d(TAG, "Constructor - Reading  com.gsma.services.nfc.extensions meta-data");
                    try {
                        int eventType3 = extParser.getEventType();
                        int depth3 = extParser.getDepth();
                        while (eventType3 != 2 && eventType3 != 1) {
                            try {
                                eventType3 = extParser.next();
                            } catch (Throwable th9) {
                                th = th9;
                                extParser2 = extParser;
                                extParser2.close();
                                throw th;
                            }
                        }
                        String tagName3 = extParser.getName();
                        if ("extensions".equals(tagName3)) {
                            while (true) {
                                int eventType4 = extParser.next();
                                int i = 4;
                                if (eventType4 != 3 || extParser.getDepth() > depth3) {
                                    if (eventType4 == 1) {
                                        extParser3 = extParser;
                                        str5 = str4;
                                        obj = str;
                                        break;
                                    }
                                    String tagName4 = extParser.getName();
                                    if (eventType4 == 2 && "se-id".equals(tagName4)) {
                                        extParser2 = extParser;
                                        try {
                                            seName = extParser2.getAttributeValue(null, "name");
                                            if (seName == null) {
                                                break;
                                            }
                                            if (!seName.equalsIgnoreCase(str)) {
                                                str11 = str2;
                                                if (!seName.equalsIgnoreCase(str11)) {
                                                    str9 = str3;
                                                    if (seName.equalsIgnoreCase(str9)) {
                                                        depth = depth3;
                                                        str10 = str4;
                                                    } else if (!seName.equalsIgnoreCase("SIM2")) {
                                                        str10 = str4;
                                                        if (!seName.equalsIgnoreCase(str10)) {
                                                            depth = depth3;
                                                            if (!seName.equalsIgnoreCase(SECURE_ELEMENT_ALT_UICC)) {
                                                                if (!seName.equalsIgnoreCase(SECURE_ELEMENT_ALT_UICC2)) {
                                                                    break;
                                                                }
                                                            }
                                                        } else {
                                                            depth = depth3;
                                                        }
                                                    } else {
                                                        depth = depth3;
                                                        str10 = str4;
                                                    }
                                                } else {
                                                    depth = depth3;
                                                    str10 = str4;
                                                    str9 = str3;
                                                }
                                            } else {
                                                depth = depth3;
                                                str10 = str4;
                                                str9 = str3;
                                                str11 = str2;
                                            }
                                            StringBuilder sb = new StringBuilder();
                                            str2 = str11;
                                            sb.append("Constructor - seName: ");
                                            sb.append(seName);
                                            Log.d(TAG, sb.toString());
                                            str7 = str;
                                            str6 = str10;
                                            str8 = str9;
                                            extParser4 = extParser2;
                                            depth3 = depth;
                                        } catch (Throwable th10) {
                                            th = th10;
                                            extParser2.close();
                                            throw th;
                                        }
                                    } else if (eventType4 != 2 || !"AID-based".equals(tagName4)) {
                                        if (eventType4 == 2) {
                                            Log.d(TAG, "Tag not implemented yet: " + tagName4);
                                        }
                                        str6 = str4;
                                        str7 = str;
                                        str8 = str3;
                                        extParser4 = extParser;
                                        depth3 = depth3;
                                    } else if (extParser.next() == 4) {
                                        aidBased = extParser.getText().equals("true") ? DBG : false;
                                        Log.d(TAG, "Constructor - aidBased: " + aidBased);
                                        str6 = str4;
                                        str7 = str;
                                        str8 = str3;
                                        extParser4 = extParser;
                                        depth3 = depth3;
                                    } else {
                                        str6 = str4;
                                        str7 = str;
                                        str8 = str3;
                                        extParser4 = extParser;
                                        depth3 = depth3;
                                    }
                                } else {
                                    extParser3 = extParser;
                                    str5 = str4;
                                    obj = str;
                                    break;
                                }
                            }
                            throw new XmlPullParserException("Unsupported se name: " + seName);
                        }
                        throw new XmlPullParserException("Meta-data does not start with <extensions> tag " + tagName3);
                    } catch (Throwable th11) {
                        th = th11;
                        extParser2 = extParser;
                        extParser2.close();
                        throw th;
                    }
                } else if (onHost) {
                    this.mSeExtension = new ESeInfo(-1, DBG);
                } else if (seName == null) {
                    Log.e(TAG, "SE extension not present, Setting default offhost seID");
                    this.mSeExtension = new ESeInfo(2, DBG);
                } else if (seName.startsWith(str4)) {
                    this.mSeExtension = new ESeInfo(1, DBG);
                } else {
                    this.mSeExtension = new ESeInfo(2, DBG);
                }
            } else {
                throw new XmlPullParserException("Meta-data does not start with <offhost-apdu-service> tag");
            }
        } catch (PackageManager.NameNotFoundException e9) {
            throw new XmlPullParserException("Unable to create context for: " + si.packageName);
        } catch (Throwable th12) {
            e = th12;
            if (parser != null) {
            }
            throw e;
        }
    }

    public ComponentName getComponent() {
        return new ComponentName(this.mService.serviceInfo.packageName, this.mService.serviceInfo.name);
    }

    static ArrayList<AidGroup> stAidGroups2AidGroups(ArrayList<StAidGroup> stAidGroup) {
        ArrayList<AidGroup> aidGroups = new ArrayList<>();
        if (stAidGroup != null) {
            Iterator<StAidGroup> it = stAidGroup.iterator();
            while (it.hasNext()) {
                aidGroups.add(it.next().getAidGroup());
            }
        }
        return aidGroups;
    }

    public List<String> getAids() {
        ArrayList<String> aids = new ArrayList<>();
        Iterator<StAidGroup> it = getStAidGroups().iterator();
        while (it.hasNext()) {
            aids.addAll(it.next().aids);
        }
        return aids;
    }

    public List<String> getPrefixAids() {
        ArrayList<String> prefixAids = new ArrayList<>();
        Iterator<StAidGroup> it = getStAidGroups().iterator();
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
        Iterator<StAidGroup> it = getStAidGroups().iterator();
        while (it.hasNext()) {
            for (String aid : it.next().aids) {
                if (aid.endsWith("#")) {
                    subsetAids.add(aid);
                }
            }
        }
        return subsetAids;
    }

    public StAidGroup getDynamicStAidGroupForCategory(String category) {
        return this.mDynamicStAidGroups.get(category);
    }

    public boolean removeDynamicStAidGroupForCategory(String category) {
        if (this.mDynamicStAidGroups.remove(category) != null) {
            return DBG;
        }
        return false;
    }

    public ArrayList<StAidGroup> getStAidGroups() {
        ArrayList<StAidGroup> groups = new ArrayList<>();
        for (Map.Entry<String, StAidGroup> entry : this.mDynamicStAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        for (Map.Entry<String, StAidGroup> entry2 : this.mStaticStAidGroups.entrySet()) {
            if (!this.mDynamicStAidGroups.containsKey(entry2.getKey())) {
                groups.add(entry2.getValue());
            }
        }
        return groups;
    }

    public ArrayList<AidGroup> getLegacyAidGroups() {
        return stAidGroups2AidGroups(getStAidGroups());
    }

    public String getCategoryForAid(String aid) {
        Iterator<StAidGroup> it = getStAidGroups().iterator();
        while (it.hasNext()) {
            StAidGroup group = it.next();
            if (group.aids.contains(aid.toUpperCase())) {
                return group.category;
            }
        }
        return null;
    }

    public boolean hasCategory(String category) {
        if (this.mStaticStAidGroups.containsKey(category) || this.mDynamicStAidGroups.containsKey(category)) {
            return DBG;
        }
        return false;
    }

    public boolean isOnHost() {
        return this.mOnHost;
    }

    public boolean requiresUnlock() {
        return this.mRequiresDeviceUnlock;
    }

    public String getDescription() {
        String str = this.mDescription;
        if (str != null) {
            return str;
        }
        return this.mService.serviceInfo.name;
    }

    public String getGsmaDescription() {
        String result = getDescription();
        String aidDescr = null;
        StAidGroup grp = this.mDynamicStAidGroups.get("other");
        if (grp == null) {
            StAidGroup grp2 = this.mStaticStAidGroups.get("other");
            if (!(grp2 == null || grp2.getDescription() == null)) {
                aidDescr = grp2.getDescription();
            }
        } else if (grp.getDescription() != null) {
            aidDescr = grp.getDescription();
        }
        if (aidDescr == null) {
            return result;
        }
        return result + " (" + aidDescr + ")";
    }

    public String getGsmaDescription(PackageManager pm) {
        CharSequence label = loadLabel(pm);
        if (label == null) {
            label = loadAppLabel(pm);
        }
        if (label == null) {
            return getGsmaDescription();
        }
        return label.toString() + " - " + getGsmaDescription();
    }

    public int getUid() {
        return this.mUid;
    }

    public void setOrReplaceDynamicStAidGroup(StAidGroup aidGroup) {
        this.mDynamicStAidGroups.put(aidGroup.getCategory(), aidGroup);
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
        if (this.mBannerResourceId == -1) {
            return this.mBanner;
        }
        try {
            Resources res = pm.getResourcesForApplication(this.mService.serviceInfo.packageName);
            if (res != null) {
                return res.getDrawable(this.mBannerResourceId);
            }
            return null;
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not load banner.");
            return null;
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e(TAG, "Could not load banner (name)");
            return null;
        }
    }

    public String getSettingsActivityName() {
        return this.mSettingsActivityName;
    }

    public String toString() {
        StringBuilder out = new StringBuilder("StApduService: ");
        out.append(getComponent());
        out.append(", description: " + this.mDescription);
        out.append(", Static AID Groups: ");
        for (StAidGroup aidGroup : this.mStaticStAidGroups.values()) {
            out.append(aidGroup.toString());
        }
        out.append(", Dynamic AID Groups: ");
        for (StAidGroup aidGroup2 : this.mDynamicStAidGroups.values()) {
            out.append(aidGroup2.toString());
        }
        return out.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return DBG;
        }
        if (!(o instanceof StApduServiceInfo)) {
            return false;
        }
        return ((StApduServiceInfo) o).getComponent().equals(getComponent());
    }

    public int hashCode() {
        return getComponent().hashCode();
    }

    public int describeContents() {
        return 0;
    }

    public ResolveInfo getResolveInfo() {
        return this.mService;
    }

    public ApduServiceInfo createApduServiceInfo() {
        String seName = null;
        if (!isOnHost()) {
            if (this.mSeExtension.seId == 1) {
                seName = SECURE_ELEMENT_ESE;
            } else {
                seName = "SIM1";
            }
        }
        return new ApduServiceInfo(this.mService, getDescription(), stAidGroups2AidGroups(getStaticStAidGroups()), stAidGroups2AidGroups(getDynamicStAidGroups()), requiresUnlock(), getBannerId(), getUid(), getSettingsActivityName(), seName, seName);
    }

    public int getAidCacheSize(String category) {
        if (!"other".equals(category) || !hasCategory("other")) {
            return 0;
        }
        return getAidCacheSizeForCategory("other");
    }

    public int getAidCacheSizeForCategory(String category) {
        List<String> aids;
        ArrayList<StAidGroup> stAidGroups = new ArrayList<>();
        int aidCacheSize = 0;
        stAidGroups.add(this.mStaticStAidGroups.get(category));
        stAidGroups.add(this.mDynamicStAidGroups.get(category));
        Iterator<StAidGroup> it = stAidGroups.iterator();
        while (it.hasNext()) {
            StAidGroup aidCache = it.next();
            if (!(aidCache == null || (aids = aidCache.getAids()) == null)) {
                for (String aid : aids) {
                    if (aid != null) {
                        int aidLen = aid.length();
                        if (aid.endsWith("*") || aid.endsWith("#")) {
                            aidLen--;
                        }
                        aidCacheSize += aidLen >> 1;
                    }
                }
            }
        }
        return aidCacheSize;
    }

    public int getCatOthersAidSizeInLmrt() {
        return getAidCacheSizeForCategory("other") + (getTotalAidNum("other") * 4);
    }

    public int getTotalAidNum(String category) {
        if (!"other".equals(category) || !hasCategory("other")) {
            return 0;
        }
        return getTotalAidNumCategory("other");
    }

    private int getTotalAidNumCategory(String category) {
        List<String> aids;
        ArrayList<StAidGroup> aidGroups = new ArrayList<>();
        int aidTotalNum = 0;
        aidGroups.add(this.mStaticStAidGroups.get(category));
        aidGroups.add(this.mDynamicStAidGroups.get(category));
        Iterator<StAidGroup> it = aidGroups.iterator();
        while (it.hasNext()) {
            StAidGroup aidCache = it.next();
            if (!(aidCache == null || (aids = aidCache.getAids()) == null)) {
                for (String aid : aids) {
                    if (aid != null && aid.length() > 0) {
                        aidTotalNum++;
                    }
                }
            }
        }
        return aidTotalNum;
    }

    public ArrayList<StAidGroup> getStaticStAidGroups() {
        ArrayList<StAidGroup> groups = new ArrayList<>();
        for (Map.Entry<String, StAidGroup> entry : this.mStaticStAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        return groups;
    }

    public ArrayList<StAidGroup> getDynamicStAidGroups() {
        ArrayList<StAidGroup> groups = new ArrayList<>();
        for (Map.Entry<String, StAidGroup> entry : this.mDynamicStAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        return groups;
    }

    public ESeInfo getSEInfo() {
        return this.mSeExtension;
    }

    public boolean getModifiable() {
        return this.mModifiable;
    }

    public int getBannerId() {
        return this.mBannerResourceId;
    }

    public boolean isServiceEnabled(String category) {
        int i;
        if (category != "other" || (i = this.mServiceState) == 1 || i == 3) {
            return DBG;
        }
        return false;
    }

    public void enableService(String category, boolean flagEnable) {
        if (category == "other") {
            Log.d(TAG, "setServiceState:Description:" + this.mDescription + ":InternalState:" + this.mServiceState + ":flagEnable:" + flagEnable);
            if (this.mServiceState != 1 || !flagEnable) {
                if (this.mServiceState == 0 && !flagEnable) {
                    return;
                }
                if (this.mServiceState == 3 && !flagEnable) {
                    return;
                }
                if (this.mServiceState != 2 || !flagEnable) {
                    if (this.mServiceState == 1 && !flagEnable) {
                        this.mServiceState = 3;
                    } else if (this.mServiceState == 0 && flagEnable) {
                        this.mServiceState = 2;
                    } else if (this.mServiceState == 3 && flagEnable) {
                        this.mServiceState = 1;
                    } else if (this.mServiceState == 2 && !flagEnable) {
                        this.mServiceState = 0;
                    }
                }
            }
        }
    }

    public int getServiceState(String category) {
        if (category != "other") {
            return 1;
        }
        return this.mServiceState;
    }

    public int setServiceState(String category, int state) {
        if (category != "other") {
            return 1;
        }
        this.mServiceState = state;
        return this.mServiceState;
    }

    public boolean getWasAccounted() {
        return this.mWasAccounted;
    }

    public void updateServiceCommitStatus(String category, boolean commitStatus) {
        if (category == "other") {
            Log.d(TAG, "updateServiceCommitStatus(enter) - Description: " + this.mDescription + ", InternalState: " + this.mServiceState + ", commitStatus: " + commitStatus);
            boolean z = false;
            if (commitStatus) {
                int i = this.mServiceState;
                if (i == 3) {
                    this.mServiceState = 0;
                } else if (i == 2) {
                    this.mServiceState = 1;
                }
                if (this.mServiceState == 1) {
                    z = true;
                }
                this.mWasAccounted = z;
            } else {
                int i2 = this.mServiceState;
                this.mWasAccounted = i2 == 1 || i2 == 2;
                int i3 = this.mServiceState;
                if (i3 == 3) {
                    this.mServiceState = 1;
                } else if (i3 == 2) {
                    this.mServiceState = 0;
                }
            }
            Log.d(TAG, "updateServiceCommitStatus(exit) - InternalState: " + this.mServiceState);
        }
    }

    public static String serviceStateToString(int state) {
        if (state == 0) {
            return "DISABLED";
        }
        if (state == 1) {
            return "ENABLED";
        }
        if (state == 2) {
            return "ENABLING";
        }
        if (state != 3) {
            return "UNKNOWN";
        }
        return "DISABLING";
    }

    public void writeToXml(XmlSerializer out) throws IOException {
        String modifiable;
        out.attribute(null, "description", this.mDescription);
        if (this.mModifiable) {
            modifiable = "true";
        } else {
            modifiable = "false";
        }
        out.attribute(null, "modifiable", modifiable);
        out.attribute(null, "uid", Integer.toString(this.mUid));
        out.attribute(null, "seId", Integer.toString(this.mSeExtension.seId));
        out.attribute(null, "bannerId", Integer.toString(this.mBannerResourceId));
        for (StAidGroup group : this.mDynamicStAidGroups.values()) {
            group.writeAsXml(out);
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        this.mService.writeToParcel(dest, flags);
        dest.writeString(this.mDescription);
        dest.writeInt(this.mOnHost ? 1 : 0);
        dest.writeInt(this.mStaticStAidGroups.size());
        if (this.mStaticStAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList(this.mStaticStAidGroups.values()));
        }
        dest.writeInt(this.mDynamicStAidGroups.size());
        if (this.mDynamicStAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList(this.mDynamicStAidGroups.values()));
        }
        dest.writeInt(this.mRequiresDeviceUnlock ? 1 : 0);
        dest.writeInt(this.mBannerResourceId);
        dest.writeInt(this.mUid);
        dest.writeString(this.mSettingsActivityName);
        this.mSeExtension.writeToParcel(dest, flags);
        Drawable drawable = this.mBanner;
        if (drawable != null) {
            dest.writeParcelable(((BitmapDrawable) drawable).getBitmap(), flags);
        } else {
            dest.writeParcelable(null, flags);
        }
        dest.writeInt(this.mModifiable ? 1 : 0);
        dest.writeInt(this.mServiceState);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("    " + getComponent() + " (Description: " + getDescription() + ")");
        StringBuilder sb = new StringBuilder();
        sb.append("    Banner Id: ");
        sb.append(getBannerId());
        pw.println(sb.toString());
        pw.println("    Static AID groups:");
        for (StAidGroup group : this.mStaticStAidGroups.values()) {
            pw.println("        Category: " + group.category);
            String desc = group.getDescription();
            if (desc != null) {
                pw.println("           Descr: " + desc);
            }
            Iterator<String> it = group.aids.iterator();
            while (it.hasNext()) {
                pw.println("            AID: " + it.next());
            }
        }
        pw.println("    Dynamic AID groups:");
        for (StAidGroup group2 : this.mDynamicStAidGroups.values()) {
            pw.println("        Category: " + group2.category);
            String desc2 = group2.getDescription();
            if (desc2 != null) {
                pw.println("           Descr: " + desc2);
            }
            Iterator<String> it2 = group2.aids.iterator();
            while (it2.hasNext()) {
                pw.println("            AID: " + it2.next());
            }
        }
        pw.println("    Settings Activity: " + this.mSettingsActivityName);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("    Routing Destination: ");
        sb2.append(this.mOnHost ? "host" : "secure element");
        sb2.append(this.mSeExtension != null ? " (" + this.mSeExtension.toString() + ")" : "");
        pw.println(sb2.toString());
        if (hasCategory("other")) {
            pw.println("    Service State: " + serviceStateToString(this.mServiceState));
        }
    }

    public static class ESeInfo implements Parcelable {
        public static final Parcelable.Creator<ESeInfo> CREATOR = new Parcelable.Creator<ESeInfo>() {
            /* class com.st.android.nfc_extensions.StApduServiceInfo.ESeInfo.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public ESeInfo createFromParcel(Parcel source) {
                boolean aidBased = false;
                int seId = source.readInt();
                if (source.readInt() == 1) {
                    aidBased = StApduServiceInfo.DBG;
                }
                return new ESeInfo(seId, aidBased);
            }

            @Override // android.os.Parcelable.Creator
            public ESeInfo[] newArray(int size) {
                return new ESeInfo[size];
            }
        };
        final boolean aidBased;
        final int seId;

        public ESeInfo(int seId2, boolean aidBased2) {
            this.seId = seId2;
            this.aidBased = aidBased2;
        }

        public int getSeId() {
            return this.seId;
        }

        public boolean getAidBased() {
            return this.aidBased;
        }

        public String toString() {
            return new StringBuilder("seId: " + this.seId + ", aidBased: " + this.aidBased).toString();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int aidb = 0;
            if (this.aidBased) {
                aidb = 1;
            }
            dest.writeInt(this.seId);
            dest.writeInt(aidb);
        }
    }
}
