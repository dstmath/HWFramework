package android.nfc.cardemulation;

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
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import com.nxp.nfc.NxpConstants;
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

public final class NxpApduServiceInfo extends ApduServiceInfo implements Parcelable {
    public static final Parcelable.Creator<NxpApduServiceInfo> CREATOR = new Parcelable.Creator<NxpApduServiceInfo>() {
        public NxpApduServiceInfo createFromParcel(Parcel source) {
            Parcel parcel = source;
            ResolveInfo info = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(parcel);
            String description = source.readString();
            boolean onHost = source.readInt() != 0;
            ArrayList<NxpAidGroup> staticNxpAidGroups = new ArrayList<>();
            if (source.readInt() > 0) {
                parcel.readTypedList(staticNxpAidGroups, NxpAidGroup.CREATOR);
            }
            ArrayList<NxpAidGroup> dynamicNxpAidGroups = new ArrayList<>();
            if (source.readInt() > 0) {
                parcel.readTypedList(dynamicNxpAidGroups, NxpAidGroup.CREATOR);
            }
            boolean requiresUnlock = source.readInt() != 0;
            int bannerResource = source.readInt();
            int uid = source.readInt();
            String settingsActivityName = source.readString();
            ESeInfo seExtension = ESeInfo.CREATOR.createFromParcel(parcel);
            ArrayList<NxpAidGroup> nfcid2Groups = new ArrayList<>();
            if (source.readInt() > 0) {
                parcel.readTypedList(nfcid2Groups, Nfcid2Group.CREATOR);
            }
            new byte[1][0] = 0;
            ArrayList<NxpAidGroup> arrayList = dynamicNxpAidGroups;
            ArrayList<NxpAidGroup> arrayList2 = staticNxpAidGroups;
            NxpApduServiceInfo service = new NxpApduServiceInfo(info, onHost, description, staticNxpAidGroups, dynamicNxpAidGroups, requiresUnlock, bannerResource, uid, settingsActivityName, seExtension, nfcid2Groups, source.createByteArray(), source.readInt() != 0);
            service.setServiceState("other", source.readInt());
            return service;
        }

        public NxpApduServiceInfo[] newArray(int size) {
            return new NxpApduServiceInfo[size];
        }
    };
    static final String GSMA_EXT_META_DATA = "com.gsma.services.nfc.extensions";
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

    public static class ESeInfo implements Parcelable {
        public static final Parcelable.Creator<ESeInfo> CREATOR = new Parcelable.Creator<ESeInfo>() {
            public ESeInfo createFromParcel(Parcel source) {
                return new ESeInfo(source.readInt(), source.readInt());
            }

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
            public FelicaInfo createFromParcel(Parcel source) {
                return new FelicaInfo(source.readString(), source.readString());
            }

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

    public static class Nfcid2Group implements Parcelable {
        public static final Parcelable.Creator<Nfcid2Group> CREATOR = new Parcelable.Creator<Nfcid2Group>() {
            public Nfcid2Group createFromParcel(Parcel source) {
                Parcel parcel = source;
                String category = source.readString();
                String description = source.readString();
                int syscodelistSize = source.readInt();
                ArrayList<String> syscodeList = new ArrayList<>();
                if (syscodelistSize > 0) {
                    parcel.readStringList(syscodeList);
                }
                int optparamlistSize = source.readInt();
                ArrayList<String> optparamList = new ArrayList<>();
                if (optparamlistSize > 0) {
                    parcel.readStringList(optparamList);
                }
                int nfcid2listSize = source.readInt();
                ArrayList<String> nfcid2List = new ArrayList<>();
                if (nfcid2listSize > 0) {
                    parcel.readStringList(nfcid2List);
                }
                Nfcid2Group nfcid2Group = new Nfcid2Group(nfcid2List, syscodeList, optparamList, category, description);
                return nfcid2Group;
            }

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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public NxpApduServiceInfo(ResolveInfo info, boolean onHost, String description, ArrayList<NxpAidGroup> staticNxpAidGroups, ArrayList<NxpAidGroup> dynamicNxpAidGroups, boolean requiresUnlock, int bannerResource, int uid, String settingsActivityName, ESeInfo seExtension, ArrayList<Nfcid2Group> nfcid2Groups, byte[] banner, boolean modifiable) {
        super(info, onHost, description, nxpAidGroups2AidGroups(staticNxpAidGroups), nxpAidGroups2AidGroups(dynamicNxpAidGroups), requiresUnlock, bannerResource, uid, settingsActivityName);
        byte[] bArr = banner;
        this.mByteArrayBanner = null;
        this.mAidSupport = true;
        if (bArr != null) {
            this.mByteArrayBanner = bArr;
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    /* JADX WARNING: Code restructure failed: missing block: B:185:0x041f, code lost:
        throw new org.xmlpull.v1.XmlPullParserException("Unsupported felicaId: " + r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:187:0x0423, code lost:
        if (r4 == null) goto L_0x044d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:189:0x042d, code lost:
        if (r4.equals(SECURE_ELEMENT_ESE) == false) goto L_0x0431;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:190:0x042f, code lost:
        r5 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:192:0x0437, code lost:
        if (r4.equals(SECURE_ELEMENT_UICC) == false) goto L_0x043b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:193:0x0439, code lost:
        r5 = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:194:0x043b, code lost:
        r5 = 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:195:0x043c, code lost:
        r1.mSeExtension = new android.nfc.cardemulation.NxpApduServiceInfo.ESeInfo(r5, r6);
        android.util.Log.d(TAG, r1.mSeExtension.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:196:0x044d, code lost:
        r1.mSeExtension = new android.nfc.cardemulation.NxpApduServiceInfo.ESeInfo(-1, 0);
        android.util.Log.d(TAG, r1.mSeExtension.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:197:0x0461, code lost:
        if (r11 == null) goto L_0x0476;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:198:0x0463, code lost:
        r1.mFelicaExtension = new android.nfc.cardemulation.NxpApduServiceInfo.FelicaInfo(r11, r2);
        android.util.Log.d(TAG, r1.mFelicaExtension.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:199:0x0476, code lost:
        r1.mFelicaExtension = new android.nfc.cardemulation.NxpApduServiceInfo.FelicaInfo(null, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:303:?, code lost:
        return;
     */
    public NxpApduServiceInfo(PackageManager pm, ResolveInfo info, boolean onHost) throws XmlPullParserException, IOException {
        super(pm, info, onHost);
        int i;
        String seName;
        Nfcid2Group currentNfcid2Group;
        NxpAidGroup.ApduPatternGroup currApduPatternGroup;
        PackageManager packageManager = pm;
        this.mByteArrayBanner = null;
        this.mAidSupport = true;
        this.mModifiable = false;
        this.mServiceState = 2;
        ServiceInfo si = info.serviceInfo;
        XmlResourceParser parser = null;
        XmlResourceParser extParser = null;
        XmlResourceParser nfcSeExtParser = null;
        if (onHost) {
            try {
                parser = si.loadXmlMetaData(packageManager, "android.nfc.cardemulation.host_apdu_service");
                if (parser == null) {
                    throw new XmlPullParserException("No android.nfc.cardemulation.host_apdu_service meta-data");
                }
            } catch (PackageManager.NameNotFoundException e) {
                throw new XmlPullParserException("Unable to create context for: " + si.packageName);
            } catch (Throwable th) {
                if (parser != null) {
                    parser.close();
                }
                throw th;
            }
        } else {
            parser = si.loadXmlMetaData(packageManager, "android.nfc.cardemulation.off_host_apdu_service");
            if (parser != null) {
                extParser = si.loadXmlMetaData(packageManager, NXP_NFC_EXT_META_DATA);
                if (extParser == null) {
                    Log.d(TAG, "No com.nxp.nfc.extensions meta-data");
                }
                nfcSeExtParser = si.loadXmlMetaData(packageManager, GSMA_EXT_META_DATA);
                if (nfcSeExtParser == null) {
                    Log.d(TAG, "No com.gsma.services.nfc.extensions meta-data");
                }
            } else {
                throw new XmlPullParserException("No android.nfc.cardemulation.off_host_apdu_service meta-data");
            }
        }
        int eventType = parser.getEventType();
        while (eventType != 2 && eventType != 1) {
            eventType = parser.next();
        }
        String tagName = parser.getName();
        if (onHost) {
            if (!"host-apdu-service".equals(tagName)) {
                throw new XmlPullParserException("Meta-data does not start with <host-apdu-service> tag");
            }
        }
        if (!onHost) {
            if (!"offhost-apdu-service".equals(tagName)) {
                throw new XmlPullParserException("Meta-data does not start with <offhost-apdu-service> tag");
            }
        }
        Resources res = packageManager.getResourcesForApplication(si.applicationInfo);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        this.mStaticNxpAidGroups = new HashMap<>();
        this.mDynamicNxpAidGroups = new HashMap<>();
        Iterator it = this.mStaticAidGroups.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String category = (String) entry.getKey();
            HashMap<String, NxpAidGroup> hashMap = this.mStaticNxpAidGroups;
            Iterator it2 = it;
            AidGroup aidg = (AidGroup) entry.getValue();
            AidGroup aidGroup = aidg;
            hashMap.put(category, new NxpAidGroup(aidg));
            it = it2;
        }
        Iterator it3 = this.mDynamicAidGroups.entrySet().iterator();
        while (it3.hasNext()) {
            Map.Entry<String, AidGroup> stringaidgroup = (Map.Entry) it3.next();
            this.mDynamicNxpAidGroups.put(stringaidgroup.getKey(), new NxpAidGroup(stringaidgroup.getValue()));
            it3 = it3;
            PackageManager packageManager2 = pm;
        }
        this.mNfcid2Groups = new ArrayList<>();
        this.mNfcid2CategoryToGroup = new HashMap<>();
        this.mNfcid2s = new ArrayList<>();
        int depth = parser.getDepth();
        NxpAidGroup.ApduPatternGroup currApduPatternGroup2 = null;
        Nfcid2Group currentNfcid2Group2 = null;
        while (true) {
            int next = parser.next();
            int eventType2 = next;
            i = 3;
            if ((next != 3 || parser.getDepth() > depth) && eventType2 != 1) {
                String tagName2 = parser.getName();
                if (onHost || eventType2 != 2 || !"apdu-pattern-group".equals(tagName2) || currApduPatternGroup2 != null) {
                    int depth2 = depth;
                    if (!onHost && eventType2 == 3 && "apdu-pattern-group".equals(tagName2) && currApduPatternGroup2 != null) {
                        if (currApduPatternGroup2.getApduPattern().size() > 0) {
                            this.mStaticNxpAidGroups.get("other").addApduGroup(currApduPatternGroup2);
                        }
                        Log.e(TAG, "apdu-pattern-group end");
                    } else if (onHost || eventType2 != 2 || !"apdu-pattern-filter".equals(tagName2) || currApduPatternGroup2 == null) {
                        if (eventType2 == 2 && "nfcid2-group".equals(tagName2) && currentNfcid2Group2 == null) {
                            TypedArray groupAttrs = res.obtainAttributes(attrs, R.styleable.AidGroup);
                            String groupDescription = groupAttrs.getString(0);
                            String groupCategory = groupAttrs.getString(1);
                            groupCategory = !"payment".equals(groupCategory) ? "other" : groupCategory;
                            currentNfcid2Group2 = this.mNfcid2CategoryToGroup.get(groupCategory);
                            if (currentNfcid2Group2 == null) {
                                currApduPatternGroup = currApduPatternGroup2;
                                currentNfcid2Group2 = new Nfcid2Group(groupCategory, groupDescription);
                            } else if (!"other".equals(groupCategory)) {
                                Nfcid2Group nfcid2Group = currentNfcid2Group2;
                                StringBuilder sb = new StringBuilder();
                                currApduPatternGroup = currApduPatternGroup2;
                                sb.append("Not allowing multiple nfcid2-groups in the ");
                                sb.append(groupCategory);
                                sb.append(" category");
                                Log.e(TAG, sb.toString());
                                currentNfcid2Group2 = null;
                            } else {
                                currApduPatternGroup = currApduPatternGroup2;
                            }
                            groupAttrs.recycle();
                        } else {
                            currApduPatternGroup = currApduPatternGroup2;
                            if (eventType2 == 3 && "nfcid2-group".equals(tagName2) && currentNfcid2Group2 != null) {
                                if (currentNfcid2Group2.nfcid2s.size() <= 0) {
                                    Log.e(TAG, "Not adding <nfcid2-group> with empty or invalid NFCID2s");
                                } else if (!this.mNfcid2CategoryToGroup.containsKey(currentNfcid2Group2.category)) {
                                    this.mNfcid2Groups.add(currentNfcid2Group2);
                                    this.mNfcid2CategoryToGroup.put(currentNfcid2Group2.category, currentNfcid2Group2);
                                }
                                currentNfcid2Group2 = null;
                            } else if (eventType2 != 2 || !"nfcid2-filter".equals(tagName2) || currentNfcid2Group2 == null) {
                                currentNfcid2Group = currentNfcid2Group2;
                                depth = depth2;
                                currApduPatternGroup2 = currApduPatternGroup;
                                currentNfcid2Group2 = currentNfcid2Group;
                            } else {
                                String nfcid2 = parser.getAttributeValue(null, "name").toUpperCase();
                                String syscode = parser.getAttributeValue(null, "syscode").toUpperCase();
                                String optparam = parser.getAttributeValue(null, "optparam").toUpperCase();
                                if (!isValidNfcid2(nfcid2) || currentNfcid2Group2.nfcid2s.size() != 0) {
                                    StringBuilder sb2 = new StringBuilder();
                                    currentNfcid2Group = currentNfcid2Group2;
                                    sb2.append("Ignoring invalid or duplicate aid: ");
                                    sb2.append(nfcid2);
                                    Log.e(TAG, sb2.toString());
                                } else {
                                    currentNfcid2Group2.nfcid2s.add(nfcid2);
                                    currentNfcid2Group2.syscode.add(syscode);
                                    currentNfcid2Group2.optparam.add(optparam);
                                    this.mNfcid2s.add(nfcid2);
                                    currentNfcid2Group = currentNfcid2Group2;
                                }
                                depth = depth2;
                                currApduPatternGroup2 = currApduPatternGroup;
                                currentNfcid2Group2 = currentNfcid2Group;
                            }
                        }
                        depth = depth2;
                        currApduPatternGroup2 = currApduPatternGroup;
                    }
                    currentNfcid2Group = currentNfcid2Group2;
                    currApduPatternGroup = currApduPatternGroup2;
                    depth = depth2;
                    currApduPatternGroup2 = currApduPatternGroup;
                    currentNfcid2Group2 = currentNfcid2Group;
                } else {
                    Log.e(TAG, "apdu-pattern-group");
                    TypedArray groupAttrs2 = res.obtainAttributes(attrs, R.styleable.ApduPatternGroup);
                    int depth3 = depth;
                    NxpAidGroup nxpAidGroup = this.mStaticNxpAidGroups.get("other");
                    currApduPatternGroup2 = new NxpAidGroup.ApduPatternGroup(groupAttrs2.getString(0));
                    groupAttrs2.recycle();
                    depth = depth3;
                }
                ResolveInfo resolveInfo = info;
            }
        }
        if (parser != null) {
            parser.close();
        }
        if (extParser != null) {
            try {
                int eventType3 = extParser.getEventType();
                int depth4 = extParser.getDepth();
                String seName2 = null;
                int powerState = 0;
                String felicaId = null;
                int eventType4 = eventType3;
                String optParam = null;
                while (eventType4 != 2 && eventType4 != 1) {
                    eventType4 = extParser.next();
                }
                if ("extensions".equals(extParser.getName())) {
                    while (true) {
                        int next2 = extParser.next();
                        int eventType5 = next2;
                        if ((next2 != i || extParser.getDepth() > depth4) && eventType5 != 1) {
                            String tagName3 = extParser.getName();
                            if (eventType5 == 2 && "se-id".equals(tagName3)) {
                                seName2 = extParser.getAttributeValue(null, "name");
                                if (seName2 != null) {
                                    if (!seName2.equalsIgnoreCase(SECURE_ELEMENT_ESE) && !seName2.equalsIgnoreCase(SECURE_ELEMENT_UICC)) {
                                        if (!seName2.equalsIgnoreCase(SECURE_ELEMENT_UICC2)) {
                                            break;
                                        }
                                    }
                                } else {
                                    break;
                                }
                            } else if (eventType5 == 2 && "se-power-state".equals(tagName3)) {
                                String powerName = extParser.getAttributeValue(null, "name");
                                boolean powerValue = extParser.getAttributeValue(null, "value").equals("true");
                                if (powerName.equalsIgnoreCase("SwitchOn") && powerValue) {
                                    powerState |= 1;
                                } else if (powerName.equalsIgnoreCase("SwitchOff") && powerValue) {
                                    powerState |= 2;
                                } else if (powerName.equalsIgnoreCase("BatteryOff") && powerValue) {
                                    powerState |= 4;
                                }
                            } else if (eventType5 == 2 && "felica-id".equals(tagName3)) {
                                felicaId = extParser.getAttributeValue(null, "name");
                                if (felicaId == null || felicaId.length() > 10) {
                                } else {
                                    optParam = extParser.getAttributeValue(null, "opt-params");
                                    if (optParam.length() > 8) {
                                        throw new XmlPullParserException("Unsupported opt-params: " + optParam);
                                    }
                                }
                            }
                            i = 3;
                        }
                    }
                    throw new XmlPullParserException("Unsupported se name: " + seName2);
                }
                throw new XmlPullParserException("Meta-data does not start with <extensions> tag " + tagName);
            } finally {
                extParser.close();
            }
        } else {
            if (!onHost) {
                Log.e(TAG, "SE extension not present, Setting default offhost seID");
                this.mSeExtension = new ESeInfo(2, 0);
            } else {
                this.mSeExtension = new ESeInfo(-1, 0);
            }
            this.mFelicaExtension = new FelicaInfo(null, null);
        }
        if (nfcSeExtParser != null) {
            try {
                int eventType6 = nfcSeExtParser.getEventType();
                int depth5 = nfcSeExtParser.getDepth();
                int i2 = 1;
                this.mAidSupport = true;
                while (eventType6 != 2 && eventType6 != i2) {
                    eventType6 = nfcSeExtParser.next();
                    i2 = 1;
                }
                if ("extensions".equals(nfcSeExtParser.getName())) {
                    while (true) {
                        int next3 = nfcSeExtParser.next();
                        int eventType7 = next3;
                        if ((next3 != 3 || nfcSeExtParser.getDepth() > depth5) && eventType7 != 1) {
                            String tagName4 = nfcSeExtParser.getName();
                            if (eventType7 == 2 && "se-id".equals(tagName4)) {
                                seName = nfcSeExtParser.getAttributeValue(null, "name");
                                if (seName != null) {
                                    if (!seName.equalsIgnoreCase(SECURE_ELEMENT_ESE) && !seName.equalsIgnoreCase(SECURE_ELEMENT_UICC) && !seName.equalsIgnoreCase(SECURE_ELEMENT_UICC2) && !seName.equalsIgnoreCase(SECURE_ELEMENT_SIM)) {
                                        if (!seName.equalsIgnoreCase("SIM1")) {
                                            break;
                                        }
                                    }
                                } else {
                                    break;
                                }
                            }
                            if (eventType7 == 2 && "AID-based".equals(tagName4)) {
                                this.mAidSupport = nfcSeExtParser.getAttributeBooleanValue(0, true);
                            }
                        }
                    }
                    throw new XmlPullParserException("Unsupported se name: " + seName);
                }
                throw new XmlPullParserException("Meta-data does not start with <extensions> tag " + tagName);
            } finally {
                nfcSeExtParser.close();
            }
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
        ApduServiceInfo apduServiceInfo = new ApduServiceInfo(getResolveInfo(), isOnHost(), getDescription(), nxpAidGroups2AidGroups(getStaticNxpAidGroups()), nxpAidGroups2AidGroups(getDynamicNxpAidGroups()), requiresUnlock(), getBannerId(), getUid(), getSettingsActivityName());
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
            if (aidCache.getCategory().equals(category)) {
                List<String> aids = aidCache.getAids();
                if (!(aids == null || aids.size() == 0)) {
                    for (String aid : aids) {
                        int aidLen = aid.length();
                        if (aid.endsWith("*")) {
                            aidLen--;
                        }
                        aidCacheSize += aidLen >> 1;
                    }
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
            if (aidCache.getCategory().equals(category)) {
                List<String> aids = aidCache.getAids();
                if (!(aids == null || aids.size() == 0)) {
                    for (String aid : aids) {
                        if (aid != null && aid.length() > 0) {
                            aidTotalNum++;
                        }
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
        if (this.mByteArrayBanner == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(this.mByteArrayBanner, 0, this.mByteArrayBanner.length);
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
        Drawable banner;
        try {
            Resources res = pm.getResourcesForApplication(this.mService.serviceInfo.packageName);
            if (this.mBannerResourceId == -1) {
                banner = new BitmapDrawable(getBitmapBanner());
            } else {
                banner = res.getDrawable(this.mBannerResourceId, null);
            }
            return banner;
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
        if (category != "other" || this.mServiceState == 1 || this.mServiceState == 3) {
            return true;
        }
        return false;
    }

    public void enableService(String category, boolean flagEnable) {
        if (category == "other") {
            Log.d(TAG, "setServiceState:Description:" + this.mDescription + ":InternalState:" + this.mServiceState + ":flagEnable:" + flagEnable);
            if (!(this.mServiceState == 1 && flagEnable) && ((this.mServiceState != 0 || flagEnable) && ((this.mServiceState != 3 || flagEnable) && !(this.mServiceState == 2 && flagEnable)))) {
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
            if (commitStatus) {
                if (this.mServiceState == 3) {
                    this.mServiceState = 0;
                } else if (this.mServiceState == 2) {
                    this.mServiceState = 1;
                }
            } else if (this.mServiceState == 3) {
                this.mServiceState = 1;
            } else if (this.mServiceState == 2) {
                this.mServiceState = 0;
            }
        }
    }

    static String serviceStateToString(int state) {
        switch (state) {
            case 0:
                return "DISABLED";
            case 1:
                return "ENABLED";
            case 2:
                return "ENABLING";
            case NxpConstants.SERVICE_STATE_DISABLING:
                return "DISABLING";
            default:
                return "UNKNOWN";
        }
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
}
