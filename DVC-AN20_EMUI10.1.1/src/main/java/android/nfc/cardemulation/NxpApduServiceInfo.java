package android.nfc.cardemulation;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.cardemulation.NxpAidGroup;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class NxpApduServiceInfo extends ApduServiceInfo implements Parcelable {
    public static final Parcelable.Creator<NxpApduServiceInfo> CREATOR = new Parcelable.Creator<NxpApduServiceInfo>() {
        /* class android.nfc.cardemulation.NxpApduServiceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NxpApduServiceInfo createFromParcel(Parcel source) {
            ResolveInfo info = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(source);
            String description = source.readString();
            boolean onHost = source.readInt() != 0;
            ArrayList<NxpAidGroup> staticNxpAidGroups = new ArrayList<>();
            if (source.readInt() > 0) {
                source.readTypedList(staticNxpAidGroups, NxpAidGroup.CREATOR);
            }
            ArrayList<NxpAidGroup> dynamicNxpAidGroups = new ArrayList<>();
            if (source.readInt() > 0) {
                source.readTypedList(dynamicNxpAidGroups, NxpAidGroup.CREATOR);
            }
            boolean requiresUnlock = source.readInt() != 0;
            int bannerResource = source.readInt();
            int uid = source.readInt();
            String settingsActivityName = source.readString();
            ESeInfo seExtension = ESeInfo.CREATOR.createFromParcel(source);
            ArrayList<Nfcid2Group> nfcid2Groups = new ArrayList<>();
            if (source.readInt() > 0) {
                source.readTypedList(nfcid2Groups, Nfcid2Group.CREATOR);
            }
            new byte[1][0] = 0;
            NxpApduServiceInfo service = new NxpApduServiceInfo(info, onHost, description, staticNxpAidGroups, dynamicNxpAidGroups, requiresUnlock, bannerResource, uid, settingsActivityName, seExtension, nfcid2Groups, source.createByteArray(), source.readInt() != 0);
            service.setServiceState("other", source.readInt());
            return service;
        }

        @Override // android.os.Parcelable.Creator
        public NxpApduServiceInfo[] newArray(int size) {
            return new NxpApduServiceInfo[size];
        }
    };
    static final String DEVICE_ST = "/dev/st21nfc";
    private static final List<String> ESE_APP_PACKAGE_LIST = Arrays.asList("de.bmw.connected.cn", "de.bmw.connected.cn.dev", "de.bmw.connected.cn.int", "de.bmw.connected.cn.stg", "de.myaudi.mobile.assistant.dev", "de.myaudi.mobile.assistant.r", "de.myaudi.mobile.assistant.p", "de.myaudi.mobile.assistant", "com.huawei.wallet");
    static final String GSMA_EXT_META_DATA = "com.gsma.services.nfc.extensions";
    public static final boolean IS_ST = NFC_DEVICE.equalsIgnoreCase(DEVICE_ST);
    static final String NFC_DEVICE = SystemProperties.get("nfc.node", "/dev/pn544");
    static final String NXP_NFC_EXT_META_DATA = "com.nxp.nfc.extensions";
    static final int POWER_STATE_BATTERY_OFF = 4;
    static final int POWER_STATE_SWITCH_OFF = 2;
    static final int POWER_STATE_SWITCH_ON = 1;
    static final String SECURE_ELEMENT_ESE = "eSE";
    public static final int SECURE_ELEMENT_ROUTE_ESE = 1;
    public static final int SECURE_ELEMENT_ROUTE_UICC = 2;
    public static final int SECURE_ELEMENT_ROUTE_UICC2 = 4;
    static final String SECURE_ELEMENT_SIM = "SIM";
    static final String SECURE_ELEMENT_SIM1 = "SIM1";
    static final String SECURE_ELEMENT_UICC = "UICC";
    static final String SECURE_ELEMENT_UICC2 = "UICC2";
    static final String TAG = "NxpApduServiceInfo";
    boolean mAidSupport;
    byte[] mByteArrayBanner;
    final HashMap<String, NxpAidGroup> mDynamicNxpAidGroups;
    final FelicaInfo mFelicaExtension;
    final boolean mModifiable;
    final HashMap<String, Nfcid2Group> mNfcid2CategoryToGroup;
    final ArrayList<Nfcid2Group> mNfcid2Groups;
    final ArrayList<String> mNfcid2s;
    final ESeInfo mSeExtension;
    int mServiceState;
    final HashMap<String, NxpAidGroup> mStaticNxpAidGroups;
    boolean mWasAccounted;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public NxpApduServiceInfo(ResolveInfo info, boolean onHost, String description, ArrayList<NxpAidGroup> staticNxpAidGroups, ArrayList<NxpAidGroup> dynamicNxpAidGroups, boolean requiresUnlock, int bannerResource, int uid, String settingsActivityName, ESeInfo seExtension, ArrayList<Nfcid2Group> nfcid2Groups, byte[] banner, boolean modifiable) {
        super(info, description, nxpAidGroups2AidGroups(staticNxpAidGroups), nxpAidGroups2AidGroups(dynamicNxpAidGroups), requiresUnlock, bannerResource, uid, settingsActivityName, onHost ? null : "false", seExtension.getSEName());
        this.mByteArrayBanner = null;
        this.mAidSupport = true;
        if (banner != null) {
            this.mByteArrayBanner = banner;
        } else {
            this.mByteArrayBanner = null;
        }
        this.mModifiable = modifiable;
        this.mServiceState = 2;
        this.mNfcid2Groups = new ArrayList<>();
        this.mNfcid2s = new ArrayList<>();
        this.mNfcid2CategoryToGroup = new HashMap<>();
        this.mStaticNxpAidGroups = new HashMap<>();
        this.mDynamicNxpAidGroups = new HashMap<>();
        this.mWasAccounted = true;
        if (staticNxpAidGroups != null) {
            Iterator<NxpAidGroup> it = staticNxpAidGroups.iterator();
            while (it.hasNext()) {
                NxpAidGroup nxpAidGroup = it.next();
                this.mStaticNxpAidGroups.put(nxpAidGroup.getCategory(), nxpAidGroup);
            }
        }
        if (dynamicNxpAidGroups != null) {
            Iterator<NxpAidGroup> it2 = dynamicNxpAidGroups.iterator();
            while (it2.hasNext()) {
                NxpAidGroup nxpAidGroup2 = it2.next();
                this.mDynamicNxpAidGroups.put(nxpAidGroup2.getCategory(), nxpAidGroup2);
            }
        }
        if (nfcid2Groups != null) {
            Iterator<Nfcid2Group> it3 = nfcid2Groups.iterator();
            while (it3.hasNext()) {
                Nfcid2Group nfcid2Group = it3.next();
                this.mNfcid2Groups.add(nfcid2Group);
                this.mNfcid2CategoryToGroup.put(nfcid2Group.category, nfcid2Group);
                this.mNfcid2s.addAll(nfcid2Group.nfcid2s);
            }
        }
        this.mSeExtension = seExtension;
        this.mFelicaExtension = null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:208:0x0463, code lost:
        throw new org.xmlpull.v1.XmlPullParserException("Unsupported felicaId: " + r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:211:0x0477, code lost:
        if (r14 == null) goto L_0x049b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:213:0x047f, code lost:
        if (r14.equals("eSE") == false) goto L_0x0483;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:214:0x0481, code lost:
        r10 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:216:0x0487, code lost:
        if (r14.equals(android.nfc.cardemulation.NxpApduServiceInfo.SECURE_ELEMENT_UICC) == false) goto L_0x048b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:217:0x0489, code lost:
        r10 = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:218:0x048b, code lost:
        r10 = 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:219:0x048c, code lost:
        r26.mSeExtension = new android.nfc.cardemulation.NxpApduServiceInfo.ESeInfo(r10, r15);
        android.util.Log.d(android.nfc.cardemulation.NxpApduServiceInfo.TAG, r26.mSeExtension.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:220:0x049b, code lost:
        r26.mSeExtension = new android.nfc.cardemulation.NxpApduServiceInfo.ESeInfo(-1, 0);
        android.util.Log.d(android.nfc.cardemulation.NxpApduServiceInfo.TAG, r26.mSeExtension.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:221:0x04ad, code lost:
        if (r3 == null) goto L_0x04c0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:222:0x04af, code lost:
        r26.mFelicaExtension = new android.nfc.cardemulation.NxpApduServiceInfo.FelicaInfo(r3, r8);
        android.util.Log.d(android.nfc.cardemulation.NxpApduServiceInfo.TAG, r26.mFelicaExtension.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:223:0x04c0, code lost:
        r26.mFelicaExtension = new android.nfc.cardemulation.NxpApduServiceInfo.FelicaInfo(null, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:224:0x04c8, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:292:0x0611, code lost:
        r12.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:355:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:319:0x0670  */
    public NxpApduServiceInfo(PackageManager pm, ResolveInfo info, boolean onHost) throws XmlPullParserException, IOException {
        super(pm, info, onHost);
        ServiceInfo si;
        XmlResourceParser nfcSeExtParser;
        String str;
        XmlResourceParser nfcSeExtParser2;
        XmlResourceParser nfcSeExtParser3;
        String seName;
        int eventType;
        ServiceInfo si2;
        int depth;
        NxpAidGroup.ApduPatternGroup currApduPatternGroup;
        this.mByteArrayBanner = null;
        this.mAidSupport = true;
        this.mModifiable = false;
        this.mServiceState = 2;
        ServiceInfo si3 = info.serviceInfo;
        XmlResourceParser parser = null;
        XmlResourceParser extParser = null;
        XmlResourceParser nfcSeExtParser4 = null;
        if (onHost) {
            try {
                parser = si3.loadXmlMetaData(pm, "android.nfc.cardemulation.host_apdu_service");
                if (parser == null) {
                    throw new XmlPullParserException("No android.nfc.cardemulation.host_apdu_service meta-data");
                }
            } catch (PackageManager.NameNotFoundException e) {
                si = si3;
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
                }
                throw e;
            }
        } else {
            try {
                parser = si3.loadXmlMetaData(pm, "android.nfc.cardemulation.off_host_apdu_service");
                if (parser != null) {
                    extParser = si3.loadXmlMetaData(pm, NXP_NFC_EXT_META_DATA);
                    if (extParser == null) {
                        Log.d(TAG, "No com.nxp.nfc.extensions meta-data");
                    }
                    nfcSeExtParser4 = si3.loadXmlMetaData(pm, GSMA_EXT_META_DATA);
                    if (nfcSeExtParser4 == null) {
                        Log.d(TAG, "No com.gsma.services.nfc.extensions meta-data");
                    }
                } else {
                    si = si3;
                    try {
                        throw new XmlPullParserException("No android.nfc.cardemulation.off_host_apdu_service meta-data");
                    } catch (PackageManager.NameNotFoundException e2) {
                        throw new XmlPullParserException("Unable to create context for: " + si.packageName);
                    }
                }
            } catch (PackageManager.NameNotFoundException e3) {
                si = si3;
                throw new XmlPullParserException("Unable to create context for: " + si.packageName);
            } catch (Throwable th3) {
                e = th3;
                if (parser != null) {
                }
                throw e;
            }
        }
        try {
            int eventType2 = parser.getEventType();
            while (eventType2 != 2 && eventType2 != 1) {
                eventType2 = parser.next();
            }
            String tagName = parser.getName();
            if (onHost && !"host-apdu-service".equals(tagName)) {
                throw new XmlPullParserException("Meta-data does not start with <host-apdu-service> tag");
            } else if (onHost || "offhost-apdu-service".equals(tagName)) {
                Resources res = pm.getResourcesForApplication(si3.applicationInfo);
                AttributeSet attrs = Xml.asAttributeSet(parser);
                this.mStaticNxpAidGroups = new HashMap<>();
                this.mDynamicNxpAidGroups = new HashMap<>();
                for (Map.Entry<String, AidGroup> stringaidgroup : this.mStaticAidGroups.entrySet()) {
                    this.mStaticNxpAidGroups.put(stringaidgroup.getKey(), new NxpAidGroup(stringaidgroup.getValue()));
                }
                for (Iterator it = this.mDynamicAidGroups.entrySet().iterator(); it.hasNext(); it = it) {
                    Map.Entry<String, AidGroup> stringaidgroup2 = (Map.Entry) it.next();
                    this.mDynamicNxpAidGroups.put(stringaidgroup2.getKey(), new NxpAidGroup(stringaidgroup2.getValue()));
                }
                this.mNfcid2Groups = new ArrayList<>();
                this.mNfcid2CategoryToGroup = new HashMap<>();
                this.mNfcid2s = new ArrayList<>();
                int depth2 = parser.getDepth();
                NxpAidGroup.ApduPatternGroup currApduPatternGroup2 = null;
                Nfcid2Group currentNfcid2Group = null;
                while (true) {
                    int eventType3 = parser.next();
                    if (eventType3 == 3 && parser.getDepth() <= depth2) {
                        break;
                    }
                    if (eventType3 == 1) {
                        break;
                    }
                    String tagName2 = parser.getName();
                    if (!onHost) {
                        depth = depth2;
                        if (eventType3 == 2 && "apdu-pattern-group".equals(tagName2) && currApduPatternGroup2 == null) {
                            Log.e(TAG, "apdu-pattern-group");
                            TypedArray groupAttrs = res.obtainAttributes(attrs, R.styleable.ApduPatternGroup);
                            String groupDescription = groupAttrs.getString(0);
                            this.mStaticNxpAidGroups.get("other");
                            currApduPatternGroup2 = new NxpAidGroup.ApduPatternGroup(groupDescription);
                            groupAttrs.recycle();
                            depth2 = depth;
                        }
                    } else {
                        depth = depth2;
                    }
                    if (!onHost && eventType3 == 3 && "apdu-pattern-group".equals(tagName2) && currApduPatternGroup2 != null) {
                        if (currApduPatternGroup2.getApduPattern().size() > 0) {
                            this.mStaticNxpAidGroups.get("other").addApduGroup(currApduPatternGroup2);
                        }
                        Log.e(TAG, "apdu-pattern-group end");
                        currApduPatternGroup = currApduPatternGroup2;
                    } else if (!onHost && eventType3 == 2 && "apdu-pattern-filter".equals(tagName2) && currApduPatternGroup2 != null) {
                        currApduPatternGroup = currApduPatternGroup2;
                    } else if (eventType3 == 2 && "nfcid2-group".equals(tagName2) && currentNfcid2Group == null) {
                        TypedArray groupAttrs2 = res.obtainAttributes(attrs, R.styleable.AidGroup);
                        String groupDescription2 = groupAttrs2.getString(0);
                        String groupCategory = groupAttrs2.getString(1);
                        groupCategory = !"payment".equals(groupCategory) ? "other" : groupCategory;
                        Nfcid2Group currentNfcid2Group2 = this.mNfcid2CategoryToGroup.get(groupCategory);
                        if (currentNfcid2Group2 == null) {
                            currentNfcid2Group2 = new Nfcid2Group(groupCategory, groupDescription2);
                        } else if (!"other".equals(groupCategory)) {
                            Log.e(TAG, "Not allowing multiple nfcid2-groups in the " + groupCategory + " category");
                            currentNfcid2Group2 = null;
                        }
                        currentNfcid2Group = currentNfcid2Group2;
                        groupAttrs2.recycle();
                        depth2 = depth;
                        currApduPatternGroup2 = currApduPatternGroup2;
                    } else {
                        currApduPatternGroup = currApduPatternGroup2;
                        if (eventType3 == 3 && "nfcid2-group".equals(tagName2) && currentNfcid2Group != null) {
                            if (currentNfcid2Group.nfcid2s.size() <= 0) {
                                Log.e(TAG, "Not adding <nfcid2-group> with empty or invalid NFCID2s");
                            } else if (!this.mNfcid2CategoryToGroup.containsKey(currentNfcid2Group.category)) {
                                this.mNfcid2Groups.add(currentNfcid2Group);
                                this.mNfcid2CategoryToGroup.put(currentNfcid2Group.category, currentNfcid2Group);
                            }
                            currentNfcid2Group = null;
                            depth2 = depth;
                            currApduPatternGroup2 = currApduPatternGroup;
                        } else if (eventType3 == 2 && "nfcid2-filter".equals(tagName2) && currentNfcid2Group != null) {
                            String nfcid2 = parser.getAttributeValue(null, "name").toUpperCase();
                            String syscode = parser.getAttributeValue(null, "syscode").toUpperCase();
                            String optparam = parser.getAttributeValue(null, "optparam").toUpperCase();
                            if (!isValidNfcid2(nfcid2) || currentNfcid2Group.nfcid2s.size() != 0) {
                                Log.e(TAG, "Ignoring invalid or duplicate aid: " + nfcid2);
                            } else {
                                currentNfcid2Group.nfcid2s.add(nfcid2);
                                currentNfcid2Group.syscode.add(syscode);
                                currentNfcid2Group.optparam.add(optparam);
                                this.mNfcid2s.add(nfcid2);
                            }
                        }
                    }
                    depth2 = depth;
                    currApduPatternGroup2 = currApduPatternGroup;
                }
                parser.close();
                String str2 = "extensions";
                if (extParser != null) {
                    try {
                        int eventType4 = extParser.getEventType();
                        int depth3 = extParser.getDepth();
                        String seName2 = null;
                        int powerState = 0;
                        while (eventType != 2 && eventType != 1) {
                            try {
                                eventType4 = extParser.next();
                            } catch (Throwable th4) {
                                th = th4;
                                extParser.close();
                                throw th;
                            }
                        }
                        String tagName3 = extParser.getName();
                        if (str2.equals(tagName3)) {
                            String optParam = null;
                            String felicaId = null;
                            while (true) {
                                try {
                                    int eventType5 = extParser.next();
                                    str = str2;
                                    if (eventType5 == 3) {
                                        try {
                                            if (extParser.getDepth() <= depth3) {
                                                si2 = si3;
                                                nfcSeExtParser = nfcSeExtParser4;
                                                break;
                                            }
                                        } catch (Throwable th5) {
                                            th = th5;
                                            extParser.close();
                                            throw th;
                                        }
                                    }
                                    if (eventType5 == 1) {
                                        si2 = si3;
                                        nfcSeExtParser = nfcSeExtParser4;
                                        break;
                                    }
                                    String tagName4 = extParser.getName();
                                    if (eventType5 == 2 && "se-id".equals(tagName4)) {
                                        seName2 = extParser.getAttributeValue(null, "name");
                                        if (seName2 != null && (seName2.equalsIgnoreCase("eSE") || seName2.equalsIgnoreCase(SECURE_ELEMENT_UICC) || seName2.equalsIgnoreCase(SECURE_ELEMENT_UICC2))) {
                                            depth3 = depth3;
                                            eventType = eventType5;
                                            str2 = str;
                                        }
                                    } else if (eventType5 == 2 && "se-power-state".equals(tagName4)) {
                                        String powerName = extParser.getAttributeValue(null, "name");
                                        try {
                                            boolean powerValue = extParser.getAttributeValue(null, "value").equals("true");
                                            if (powerName.equalsIgnoreCase("SwitchOn") && powerValue) {
                                                powerState |= 1;
                                            } else if (powerName.equalsIgnoreCase("SwitchOff") && powerValue) {
                                                powerState |= 2;
                                            } else if (powerName.equalsIgnoreCase("BatteryOff") && powerValue) {
                                                powerState |= 4;
                                            }
                                            depth3 = depth3;
                                            nfcSeExtParser4 = nfcSeExtParser4;
                                            si3 = si3;
                                            eventType = eventType5;
                                            str2 = str;
                                        } catch (Throwable th6) {
                                            th = th6;
                                            extParser.close();
                                            throw th;
                                        }
                                    } else if (eventType5 != 2 || !"felica-id".equals(tagName4)) {
                                        depth3 = depth3;
                                        nfcSeExtParser4 = nfcSeExtParser4;
                                        si3 = si3;
                                        eventType = eventType5;
                                        str2 = str;
                                    } else {
                                        felicaId = extParser.getAttributeValue(null, "name");
                                        if (felicaId != null && felicaId.length() <= 10) {
                                            optParam = extParser.getAttributeValue(null, "opt-params");
                                            if (optParam.length() <= 8) {
                                                depth3 = depth3;
                                                nfcSeExtParser4 = nfcSeExtParser4;
                                                si3 = si3;
                                                eventType = eventType5;
                                                str2 = str;
                                            } else {
                                                throw new XmlPullParserException("Unsupported opt-params: " + optParam);
                                            }
                                        }
                                    }
                                } catch (Throwable th7) {
                                    th = th7;
                                    extParser.close();
                                    throw th;
                                }
                            }
                            throw new XmlPullParserException("Unsupported se name: " + seName2);
                        }
                        throw new XmlPullParserException("Meta-data does not start with <extensions> tag " + tagName3);
                    } catch (Throwable th8) {
                        th = th8;
                        extParser.close();
                        throw th;
                    }
                } else {
                    str = str2;
                    nfcSeExtParser = nfcSeExtParser4;
                    if (!onHost) {
                        String packageName = si3.packageName;
                        Log.d(TAG, "Off-Host service has no SE extension, packageName: " + packageName);
                        if (ESE_APP_PACKAGE_LIST.contains(packageName)) {
                            this.mSeExtension = new ESeInfo(1, 0);
                            Log.d(TAG, "SE extension not present, Setting offhost seID of carkey to eSE");
                        } else {
                            Log.d(TAG, "SE extension not present, Setting default offhost seID");
                            this.mSeExtension = new ESeInfo(2, 0);
                        }
                    } else {
                        this.mSeExtension = new ESeInfo(-1, 0);
                    }
                    this.mFelicaExtension = new FelicaInfo(null, null);
                }
                if (nfcSeExtParser != null) {
                    try {
                        int eventType6 = nfcSeExtParser.getEventType();
                        int depth4 = nfcSeExtParser.getDepth();
                        int i = 1;
                        this.mAidSupport = true;
                        while (eventType6 != 2 && eventType6 != i) {
                            try {
                                eventType6 = nfcSeExtParser.next();
                                i = 1;
                            } catch (Throwable th9) {
                                th = th9;
                                nfcSeExtParser2 = nfcSeExtParser;
                                nfcSeExtParser2.close();
                                throw th;
                            }
                        }
                        String tagName5 = nfcSeExtParser.getName();
                        if (str.equals(tagName5)) {
                            while (true) {
                                int eventType7 = nfcSeExtParser.next();
                                if (eventType7 == 3 && nfcSeExtParser.getDepth() <= depth4) {
                                    nfcSeExtParser3 = nfcSeExtParser;
                                    break;
                                }
                                if (eventType7 == 1) {
                                    nfcSeExtParser3 = nfcSeExtParser;
                                    break;
                                }
                                String tagName6 = nfcSeExtParser.getName();
                                if (eventType7 != 2 || !"se-id".equals(tagName6)) {
                                    nfcSeExtParser2 = nfcSeExtParser;
                                } else {
                                    nfcSeExtParser2 = nfcSeExtParser;
                                    try {
                                        seName = nfcSeExtParser2.getAttributeValue(null, "name");
                                        if (seName != null) {
                                            if (!seName.equalsIgnoreCase("eSE") && !seName.equalsIgnoreCase(SECURE_ELEMENT_UICC) && !seName.equalsIgnoreCase(SECURE_ELEMENT_UICC2) && !seName.equalsIgnoreCase(SECURE_ELEMENT_SIM)) {
                                                if (!seName.equalsIgnoreCase("SIM1")) {
                                                    break;
                                                }
                                            }
                                        } else {
                                            break;
                                        }
                                    } catch (Throwable th10) {
                                        th = th10;
                                        nfcSeExtParser2.close();
                                        throw th;
                                    }
                                }
                                if (eventType7 == 2 && "AID-based".equals(tagName6)) {
                                    this.mAidSupport = nfcSeExtParser2.getAttributeBooleanValue(0, true);
                                }
                                nfcSeExtParser = nfcSeExtParser2;
                            }
                            throw new XmlPullParserException("Unsupported se name: " + seName);
                        }
                        throw new XmlPullParserException("Meta-data does not start with <extensions> tag " + tagName5);
                    } catch (Throwable th11) {
                        th = th11;
                        nfcSeExtParser2 = nfcSeExtParser;
                        nfcSeExtParser2.close();
                        throw th;
                    }
                }
            } else {
                throw new XmlPullParserException("Meta-data does not start with <offhost-apdu-service> tag");
            }
        } catch (PackageManager.NameNotFoundException e4) {
            si = si3;
            throw new XmlPullParserException("Unable to create context for: " + si.packageName);
        } catch (Throwable th12) {
            e = th12;
            if (parser != null) {
                parser.close();
            }
            throw e;
        }
    }

    static ArrayList<AidGroup> nxpAidGroups2AidGroups(ArrayList<NxpAidGroup> nxpAidGroup) {
        ArrayList<AidGroup> aidGroups = new ArrayList<>();
        if (nxpAidGroup != null) {
            Iterator<NxpAidGroup> it = nxpAidGroup.iterator();
            while (it.hasNext()) {
                aidGroups.add(it.next().createAidGroup());
            }
        }
        return aidGroups;
    }

    public HashMap<String, NxpAidGroup> getStaticGroups() {
        return this.mStaticNxpAidGroups;
    }

    public void updateServiceDynamicAidGroup() {
        Log.i(TAG, "updateServiceDynamicAidGroup enter");
        NxpAidGroup nxpDynamicAidGroup = getDynamicNxpAidGroupForCategory("payment");
        if (nxpDynamicAidGroup == null) {
            Log.i(TAG, "updateServiceDynamicAidGroup, payment aidgroup is null");
            return;
        }
        List<String> listDynamicAids = nxpDynamicAidGroup.getAids();
        if (listDynamicAids == null || listDynamicAids.size() == 0) {
            Log.i(TAG, "updateServiceDynamicAidGroup, payment aid is null");
            return;
        }
        NxpAidGroup nxpAidGroup = new NxpAidGroup(listDynamicAids, "other", nxpDynamicAidGroup.getDescription());
        removeDynamicNxpAidGroupForCategory("payment");
        setOrReplaceDynamicNxpAidGroup(nxpAidGroup);
        Log.i(TAG, "updateServiceDynamicAidGroup exit");
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
        Iterator<NxpAidGroup> it = this.mDynamicNxpAidGroups.values().iterator();
        while (it.hasNext()) {
            it.next().writeAsXml(out);
        }
    }

    public ResolveInfo getResolveInfo() {
        return this.mService;
    }

    public ArrayList<String> getAids() {
        ArrayList<String> aids = new ArrayList<>();
        Iterator<NxpAidGroup> it = getNxpAidGroups().iterator();
        while (it.hasNext()) {
            aids.addAll(it.next().getAids());
        }
        return aids;
    }

    public ArrayList<NxpAidGroup> getNxpAidGroups() {
        ArrayList<NxpAidGroup> groups = new ArrayList<>();
        for (Map.Entry<String, NxpAidGroup> entry : this.mDynamicNxpAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        for (Map.Entry<String, NxpAidGroup> entry2 : this.mStaticNxpAidGroups.entrySet()) {
            if (!this.mDynamicNxpAidGroups.containsKey(entry2.getKey())) {
                groups.add(entry2.getValue());
            }
        }
        return groups;
    }

    public ApduServiceInfo createApduServiceInfo() {
        ApduServiceInfo apduServiceInfo;
        String str;
        String str2;
        if (IS_ST) {
            String description = getDescription();
            if (description == null) {
                description = this.mService.serviceInfo.name;
            }
            ResolveInfo resolveInfo = getResolveInfo();
            ArrayList<AidGroup> nxpAidGroups2AidGroups = nxpAidGroups2AidGroups(getStaticNxpAidGroups());
            ArrayList<AidGroup> nxpAidGroups2AidGroups2 = nxpAidGroups2AidGroups(getDynamicNxpAidGroups());
            boolean requiresUnlock = requiresUnlock();
            int bannerId = getBannerId();
            int uid = getUid();
            String settingsActivityName = getSettingsActivityName();
            if (isOnHost()) {
                str2 = null;
            } else {
                str2 = "false";
            }
            apduServiceInfo = new ApduServiceInfo(resolveInfo, description, nxpAidGroups2AidGroups, nxpAidGroups2AidGroups2, requiresUnlock, bannerId, uid, settingsActivityName, str2, this.mSeExtension.getSEName());
        } else {
            ResolveInfo resolveInfo2 = getResolveInfo();
            String description2 = getDescription();
            ArrayList<AidGroup> nxpAidGroups2AidGroups3 = nxpAidGroups2AidGroups(getStaticNxpAidGroups());
            ArrayList<AidGroup> nxpAidGroups2AidGroups4 = nxpAidGroups2AidGroups(getDynamicNxpAidGroups());
            boolean requiresUnlock2 = requiresUnlock();
            int bannerId2 = getBannerId();
            int uid2 = getUid();
            String settingsActivityName2 = getSettingsActivityName();
            if (isOnHost()) {
                str = null;
            } else {
                str = "false";
            }
            apduServiceInfo = new ApduServiceInfo(resolveInfo2, description2, nxpAidGroups2AidGroups3, nxpAidGroups2AidGroups4, requiresUnlock2, bannerId2, uid2, settingsActivityName2, str, this.mSeExtension.getSEName());
        }
        apduServiceInfo.setOtherCategoryServiceState(isServiceEnabled("other"));
        return apduServiceInfo;
    }

    public int getAidCacheSize(String category) {
        if (!"other".equals(category) || !hasCategory("other")) {
            return 0;
        }
        return getAidCacheSizeForCategory("other");
    }

    public int getAidCacheSizeForCategory(String category) {
        List<String> aids;
        ArrayList<NxpAidGroup> nxpAidGroups = new ArrayList<>();
        int aidCacheSize = 0;
        nxpAidGroups.addAll(getStaticNxpAidGroups());
        nxpAidGroups.addAll(getDynamicNxpAidGroups());
        if (nxpAidGroups.size() == 0) {
            return 0;
        }
        Iterator<NxpAidGroup> it = nxpAidGroups.iterator();
        while (it.hasNext()) {
            NxpAidGroup aidCache = it.next();
            if (!(!aidCache.getCategory().equals(category) || (aids = aidCache.getAids()) == null || aids.size() == 0)) {
                for (String aid : aids) {
                    int aidLen = aid.length();
                    if (aid.endsWith("*")) {
                        aidLen--;
                    }
                    aidCacheSize += aidLen >> 1;
                }
            }
        }
        return aidCacheSize;
    }

    public int geTotalAidNum(String category) {
        if (!"other".equals(category) || !hasCategory("other")) {
            return 0;
        }
        return getTotalAidNumCategory("other");
    }

    public boolean isNonAidBasedRoutingSupported() {
        return this.mAidSupport;
    }

    private int getTotalAidNumCategory(String category) {
        List<String> aids;
        ArrayList<NxpAidGroup> aidGroups = new ArrayList<>();
        int aidTotalNum = 0;
        aidGroups.addAll(getStaticNxpAidGroups());
        aidGroups.addAll(getDynamicNxpAidGroups());
        if (aidGroups.size() == 0) {
            return 0;
        }
        Iterator<NxpAidGroup> it = aidGroups.iterator();
        while (it.hasNext()) {
            NxpAidGroup aidCache = it.next();
            if (!(!aidCache.getCategory().equals(category) || (aids = aidCache.getAids()) == null || aids.size() == 0)) {
                for (String aid : aids) {
                    if (aid != null && aid.length() > 0) {
                        aidTotalNum++;
                    }
                }
            }
        }
        return aidTotalNum;
    }

    public ArrayList<NxpAidGroup> getStaticNxpAidGroups() {
        ArrayList<NxpAidGroup> groups = new ArrayList<>();
        for (Map.Entry<String, NxpAidGroup> entry : this.mStaticNxpAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        return groups;
    }

    public ArrayList<NxpAidGroup> getDynamicNxpAidGroups() {
        ArrayList<NxpAidGroup> groups = new ArrayList<>();
        for (Map.Entry<String, NxpAidGroup> entry : this.mDynamicNxpAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        return groups;
    }

    public ArrayList<String> getNfcid2s() {
        return this.mNfcid2s;
    }

    public ArrayList<Nfcid2Group> getNfcid2Groups() {
        return this.mNfcid2Groups;
    }

    public ESeInfo getSEInfo() {
        return this.mSeExtension;
    }

    public boolean getModifiable() {
        return this.mModifiable;
    }

    public Bitmap getBitmapBanner() {
        byte[] bArr = this.mByteArrayBanner;
        if (bArr == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
    }

    public void setOrReplaceDynamicNxpAidGroup(NxpAidGroup nxpAidGroup) {
        NxpApduServiceInfo.super.setOrReplaceDynamicAidGroup(nxpAidGroup);
        this.mDynamicNxpAidGroups.put(nxpAidGroup.getCategory(), nxpAidGroup);
    }

    public NxpAidGroup getDynamicNxpAidGroupForCategory(String category) {
        return this.mDynamicNxpAidGroups.get(category);
    }

    public boolean removeDynamicNxpAidGroupForCategory(String category) {
        NxpApduServiceInfo.super.removeDynamicAidGroupForCategory(category);
        return this.mDynamicNxpAidGroups.remove(category) != null;
    }

    public Drawable loadBanner(PackageManager pm) {
        try {
            Resources res = pm.getResourcesForApplication(this.mService.serviceInfo.packageName);
            if (this.mBannerResourceId == -1) {
                return new BitmapDrawable(getBitmapBanner());
            }
            return res.getDrawable(this.mBannerResourceId, null);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not load banner.");
            return null;
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e(TAG, "Could not load banner.");
            return null;
        }
    }

    public int getBannerId() {
        return this.mBannerResourceId;
    }

    static boolean isValidNfcid2(String nfcid2) {
        if (nfcid2 == null) {
            return false;
        }
        int nfcid2Length = nfcid2.length();
        if (nfcid2Length == 0 || nfcid2Length % 2 != 0) {
            Log.e(TAG, "AID " + nfcid2 + " is not correctly formatted.");
            return false;
        } else if (nfcid2Length == 16) {
            return true;
        } else {
            Log.e(TAG, "NFCID2 " + nfcid2 + " is not 8 bytes.");
            return false;
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder("ApduService: ");
        out.append(getComponent());
        out.append(", description: " + this.mDescription);
        out.append(", Static AID Groups: ");
        for (NxpAidGroup nxpAidGroup : this.mStaticNxpAidGroups.values()) {
            out.append(nxpAidGroup.toString());
        }
        out.append(", Dynamic AID Groups: ");
        for (NxpAidGroup nxpAidGroup2 : this.mDynamicNxpAidGroups.values()) {
            out.append(nxpAidGroup2.toString());
        }
        return out.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NxpApduServiceInfo)) {
            return false;
        }
        return ((NxpApduServiceInfo) o).getComponent().equals(getComponent());
    }

    public void writeToParcel(Parcel dest, int flags) {
        this.mService.writeToParcel(dest, flags);
        dest.writeString(this.mDescription);
        dest.writeInt(this.mOnHost ? 1 : 0);
        dest.writeInt(this.mStaticNxpAidGroups.size());
        if (this.mStaticNxpAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList(this.mStaticNxpAidGroups.values()));
        }
        dest.writeInt(this.mDynamicNxpAidGroups.size());
        if (this.mDynamicNxpAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList(this.mDynamicNxpAidGroups.values()));
        }
        dest.writeInt(this.mRequiresDeviceUnlock ? 1 : 0);
        dest.writeInt(this.mBannerResourceId);
        dest.writeInt(this.mUid);
        dest.writeString(this.mSettingsActivityName);
        this.mSeExtension.writeToParcel(dest, flags);
        dest.writeInt(this.mNfcid2Groups.size());
        if (this.mNfcid2Groups.size() > 0) {
            dest.writeTypedList(this.mNfcid2Groups);
        }
        dest.writeByteArray(this.mByteArrayBanner);
        dest.writeInt(this.mModifiable ? 1 : 0);
        dest.writeInt(this.mServiceState);
    }

    public boolean isServiceEnabled(String category) {
        int i;
        if (category != "other" || (i = this.mServiceState) == 1 || i == 3) {
            return true;
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

    public void updateServiceCommitStatus(String category, boolean commitStatus) {
        if (category == "other") {
            Log.d(TAG, "updateServiceCommitStatus:Description:" + this.mDescription + ":InternalState:" + this.mServiceState + ":commitStatus:" + commitStatus);
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
                return;
            }
            int i2 = this.mServiceState;
            this.mWasAccounted = i2 == 1 || i2 == 2;
            int i3 = this.mServiceState;
            if (i3 == 3) {
                this.mServiceState = 1;
            } else if (i3 == 2) {
                this.mServiceState = 0;
            }
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

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        NxpApduServiceInfo.super.dump(fd, pw, args);
        StringBuilder sb = new StringBuilder();
        sb.append("    Routing Destination: ");
        sb.append(this.mOnHost ? "host" : "secure element");
        pw.println(sb.toString());
        if (hasCategory("other")) {
            pw.println("    Service State: " + serviceStateToString(this.mServiceState));
        }
    }

    public static class Nfcid2Group implements Parcelable {
        public static final Parcelable.Creator<Nfcid2Group> CREATOR = new Parcelable.Creator<Nfcid2Group>() {
            /* class android.nfc.cardemulation.NxpApduServiceInfo.Nfcid2Group.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Nfcid2Group createFromParcel(Parcel source) {
                String category = source.readString();
                String description = source.readString();
                int syscodelistSize = source.readInt();
                ArrayList<String> syscodeList = new ArrayList<>();
                if (syscodelistSize > 0) {
                    source.readStringList(syscodeList);
                }
                int optparamlistSize = source.readInt();
                ArrayList<String> optparamList = new ArrayList<>();
                if (optparamlistSize > 0) {
                    source.readStringList(optparamList);
                }
                int nfcid2listSize = source.readInt();
                ArrayList<String> nfcid2List = new ArrayList<>();
                if (nfcid2listSize > 0) {
                    source.readStringList(nfcid2List);
                }
                return new Nfcid2Group(nfcid2List, syscodeList, optparamList, category, description);
            }

            @Override // android.os.Parcelable.Creator
            public Nfcid2Group[] newArray(int size) {
                return new Nfcid2Group[size];
            }
        };
        final String category;
        final String description;
        final ArrayList<String> nfcid2s;
        final ArrayList<String> optparam;
        final ArrayList<String> syscode;

        Nfcid2Group(ArrayList<String> nfcid2s2, ArrayList<String> syscode2, ArrayList<String> optparam2, String category2, String description2) {
            this.nfcid2s = nfcid2s2;
            this.category = category2;
            this.description = description2;
            this.syscode = syscode2;
            this.optparam = optparam2;
        }

        Nfcid2Group(String category2, String description2) {
            this.nfcid2s = new ArrayList<>();
            this.syscode = new ArrayList<>();
            this.optparam = new ArrayList<>();
            this.category = category2;
            this.description = description2;
        }

        public String getCategory() {
            return this.category;
        }

        public ArrayList<String> getNfcid2s() {
            return this.nfcid2s;
        }

        public String getSyscodeForNfcid2(String nfcid2) {
            int idx = this.nfcid2s.indexOf(nfcid2);
            if (idx != -1) {
                return this.syscode.get(idx);
            }
            return "";
        }

        public String getOptparamForNfcid2(String nfcid2) {
            int idx = this.nfcid2s.indexOf(nfcid2);
            if (idx != -1) {
                return this.optparam.get(idx);
            }
            return "";
        }

        public String toString() {
            StringBuilder out = new StringBuilder("Category: " + this.category + ", description: " + this.description + ", AIDs:");
            Iterator<String> it = this.nfcid2s.iterator();
            while (it.hasNext()) {
                out.append(it.next());
                out.append(", ");
            }
            return out.toString();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.category);
            dest.writeString(this.description);
            dest.writeInt(this.syscode.size());
            if (this.syscode.size() > 0) {
                dest.writeStringList(this.syscode);
            }
            dest.writeInt(this.optparam.size());
            if (this.optparam.size() > 0) {
                dest.writeStringList(this.optparam);
            }
            dest.writeInt(this.nfcid2s.size());
            if (this.nfcid2s.size() > 0) {
                dest.writeStringList(this.nfcid2s);
            }
        }
    }

    public static class ESeInfo implements Parcelable {
        public static final Parcelable.Creator<ESeInfo> CREATOR = new Parcelable.Creator<ESeInfo>() {
            /* class android.nfc.cardemulation.NxpApduServiceInfo.ESeInfo.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public ESeInfo createFromParcel(Parcel source) {
                return new ESeInfo(source.readInt(), source.readInt());
            }

            @Override // android.os.Parcelable.Creator
            public ESeInfo[] newArray(int size) {
                return new ESeInfo[size];
            }
        };
        final int powerState;
        final int seId;

        public ESeInfo(int seId2, int powerState2) {
            this.seId = seId2;
            this.powerState = powerState2;
        }

        public int getSeId() {
            return this.seId;
        }

        public String getSEName() {
            int i = this.seId;
            if (1 == i) {
                return "eSE";
            }
            if (2 == i) {
                return "SIM1";
            }
            if (4 == i) {
                return "SIM2";
            }
            return null;
        }

        public int getPowerState() {
            return this.powerState;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("seId: ");
            sb.append(this.seId);
            sb.append(",Power state: [switchOn: ");
            boolean z = true;
            sb.append((this.powerState & 1) != 0);
            sb.append(",switchOff: ");
            sb.append((this.powerState & 2) != 0);
            sb.append(",batteryOff: ");
            if ((this.powerState & 4) == 0) {
                z = false;
            }
            sb.append(z);
            sb.append("]");
            return new StringBuilder(sb.toString()).toString();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.seId);
            dest.writeInt(this.powerState);
        }
    }

    public static class FelicaInfo implements Parcelable {
        public static final Parcelable.Creator<FelicaInfo> CREATOR = new Parcelable.Creator<FelicaInfo>() {
            /* class android.nfc.cardemulation.NxpApduServiceInfo.FelicaInfo.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public FelicaInfo createFromParcel(Parcel source) {
                return new FelicaInfo(source.readString(), source.readString());
            }

            @Override // android.os.Parcelable.Creator
            public FelicaInfo[] newArray(int size) {
                return new FelicaInfo[size];
            }
        };
        final String felicaId;
        final String optParams;

        public FelicaInfo(String felica_id, String opt_params) {
            this.felicaId = felica_id;
            this.optParams = opt_params;
        }

        public String getFelicaId() {
            return this.felicaId;
        }

        public String getOptParams() {
            return this.optParams;
        }

        public String toString() {
            return new StringBuilder("felica id: " + this.felicaId + ",optional params: " + this.optParams).toString();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.felicaId);
            dest.writeString(this.optParams);
        }
    }

    public String getGsmaDescription() {
        String result = getDescription();
        String aidDescr = null;
        NxpAidGroup grp = this.mDynamicNxpAidGroups.get("other");
        if (grp == null) {
            NxpAidGroup grp2 = this.mStaticNxpAidGroups.get("other");
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

    public CharSequence loadLabel(PackageManager pm) {
        return this.mService.loadLabel(pm);
    }

    public CharSequence loadAppLabel(PackageManager pm) {
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(this.mService.resolvePackageName, 128));
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    public boolean hasCategory(String category) {
        return this.mStaticNxpAidGroups.containsKey(category) || this.mDynamicNxpAidGroups.containsKey(category);
    }

    public ComponentName getComponent() {
        return new ComponentName(this.mService.serviceInfo.packageName, this.mService.serviceInfo.name);
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

    public boolean isOnHost() {
        return this.mOnHost;
    }

    public boolean getWasAccounted() {
        return this.mWasAccounted;
    }
}
