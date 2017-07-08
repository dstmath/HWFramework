package android.content.pm;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class IntentFilterVerificationInfo implements Parcelable {
    private static final String ATTR_DOMAIN_NAME = "name";
    private static final String ATTR_PACKAGE_NAME = "packageName";
    private static final String ATTR_STATUS = "status";
    public static final Creator<IntentFilterVerificationInfo> CREATOR = null;
    private static final String TAG = null;
    private static final String TAG_DOMAIN = "domain";
    private ArraySet<String> mDomains;
    private int mMainStatus;
    private String mPackageName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.IntentFilterVerificationInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.IntentFilterVerificationInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.IntentFilterVerificationInfo.<clinit>():void");
    }

    public IntentFilterVerificationInfo() {
        this.mDomains = new ArraySet();
        this.mPackageName = null;
        this.mMainStatus = 0;
    }

    public IntentFilterVerificationInfo(String packageName, ArrayList<String> domains) {
        this.mDomains = new ArraySet();
        this.mPackageName = packageName;
        this.mDomains.addAll(domains);
        this.mMainStatus = 0;
    }

    public IntentFilterVerificationInfo(XmlPullParser parser) throws IOException, XmlPullParserException {
        this.mDomains = new ArraySet();
        readFromXml(parser);
    }

    public IntentFilterVerificationInfo(Parcel source) {
        this.mDomains = new ArraySet();
        readFromParcel(source);
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public int getStatus() {
        return this.mMainStatus;
    }

    public void setStatus(int s) {
        if (s < 0 || s > 3) {
            Log.w(TAG, "Trying to set a non supported status: " + s);
        } else {
            this.mMainStatus = s;
        }
    }

    public ArraySet<String> getDomains() {
        return this.mDomains;
    }

    public void setDomains(ArrayList<String> list) {
        this.mDomains = new ArraySet(list);
    }

    public String getDomainsString() {
        StringBuilder sb = new StringBuilder();
        for (String str : this.mDomains) {
            if (sb.length() > 0) {
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
            sb.append(str);
        }
        return sb.toString();
    }

    String getStringFromXml(XmlPullParser parser, String attribute, String defaultValue) {
        String value = parser.getAttributeValue(null, attribute);
        if (value != null) {
            return value;
        }
        Log.w(TAG, "Missing element under " + TAG + ": " + attribute + " at " + parser.getPositionDescription());
        return defaultValue;
    }

    int getIntFromXml(XmlPullParser parser, String attribute, int defaultValue) {
        String value = parser.getAttributeValue(null, attribute);
        if (!TextUtils.isEmpty(value)) {
            return Integer.parseInt(value);
        }
        Log.w(TAG, "Missing element under " + TAG + ": " + attribute + " at " + parser.getPositionDescription());
        return defaultValue;
    }

    public void readFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        this.mPackageName = getStringFromXml(parser, ATTR_PACKAGE_NAME, null);
        if (this.mPackageName == null) {
            Log.e(TAG, "Package name cannot be null!");
        }
        int status = getIntFromXml(parser, ATTR_STATUS, -1);
        if (status == -1) {
            Log.e(TAG, "Unknown status value: " + status);
        }
        this.mMainStatus = status;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(TAG_DOMAIN)) {
                    String name = getStringFromXml(parser, ATTR_DOMAIN_NAME, null);
                    if (!TextUtils.isEmpty(name)) {
                        this.mDomains.add(name);
                    }
                } else {
                    Log.w(TAG, "Unknown tag parsing IntentFilter: " + tagName);
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    public void writeToXml(XmlSerializer serializer) throws IOException {
        serializer.attribute(null, ATTR_PACKAGE_NAME, this.mPackageName);
        serializer.attribute(null, ATTR_STATUS, String.valueOf(this.mMainStatus));
        for (String str : this.mDomains) {
            serializer.startTag(null, TAG_DOMAIN);
            serializer.attribute(null, ATTR_DOMAIN_NAME, str);
            serializer.endTag(null, TAG_DOMAIN);
        }
    }

    public String getStatusString() {
        return getStatusStringFromValue((long) this.mMainStatus);
    }

    public static String getStatusStringFromValue(long val) {
        StringBuilder sb = new StringBuilder();
        switch ((int) (val >> 32)) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                sb.append("ask");
                break;
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                sb.append("always : ");
                sb.append(Long.toHexString(-1 & val));
                break;
            case Engine.DEFAULT_STREAM /*3*/:
                sb.append("never");
                break;
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                sb.append("always-ask");
                break;
            default:
                sb.append("undefined");
                break;
        }
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel source) {
        this.mPackageName = source.readString();
        this.mMainStatus = source.readInt();
        ArrayList<String> list = new ArrayList();
        source.readStringList(list);
        this.mDomains.addAll(list);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackageName);
        dest.writeInt(this.mMainStatus);
        dest.writeStringList(new ArrayList(this.mDomains));
    }
}
