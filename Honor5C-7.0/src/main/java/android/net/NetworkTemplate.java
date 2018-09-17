package android.net;

import android.net.wifi.WifiInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.service.notification.NotificationRankerService;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.BackupUtils;
import android.util.BackupUtils.BadVersionException;
import com.android.internal.util.ArrayUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class NetworkTemplate implements Parcelable {
    private static final int BACKUP_VERSION = 1;
    public static final Creator<NetworkTemplate> CREATOR = null;
    public static final int MATCH_BLUETOOTH = 8;
    public static final int MATCH_ETHERNET = 5;
    @Deprecated
    public static final int MATCH_MOBILE_3G_LOWER = 2;
    @Deprecated
    public static final int MATCH_MOBILE_4G = 3;
    public static final int MATCH_MOBILE_ALL = 1;
    public static final int MATCH_MOBILE_WILDCARD = 6;
    public static final int MATCH_PROXY = 9;
    public static final int MATCH_WIFI = 4;
    public static final int MATCH_WIFI_WILDCARD = 7;
    private static boolean sForceAllNetworkTypes;
    private final int mMatchRule;
    private final String[] mMatchSubscriberIds;
    private final String mNetworkId;
    private final String mSubscriberId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkTemplate.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.NetworkTemplate.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkTemplate.<clinit>():void");
    }

    public static void forceAllNetworkTypes() {
        sForceAllNetworkTypes = true;
    }

    public static NetworkTemplate buildTemplateMobileAll(String subscriberId) {
        return new NetworkTemplate(MATCH_MOBILE_ALL, subscriberId, null);
    }

    @Deprecated
    public static NetworkTemplate buildTemplateMobile3gLower(String subscriberId) {
        return new NetworkTemplate(MATCH_MOBILE_3G_LOWER, subscriberId, null);
    }

    @Deprecated
    public static NetworkTemplate buildTemplateMobile4g(String subscriberId) {
        return new NetworkTemplate(MATCH_MOBILE_4G, subscriberId, null);
    }

    public static NetworkTemplate buildTemplateMobileWildcard() {
        return new NetworkTemplate(MATCH_MOBILE_WILDCARD, null, null);
    }

    public static NetworkTemplate buildTemplateWifiWildcard() {
        return new NetworkTemplate(MATCH_WIFI_WILDCARD, null, null);
    }

    @Deprecated
    public static NetworkTemplate buildTemplateWifi() {
        return buildTemplateWifiWildcard();
    }

    public static NetworkTemplate buildTemplateWifi(String networkId) {
        return new NetworkTemplate(MATCH_WIFI, null, networkId);
    }

    public static NetworkTemplate buildTemplateEthernet() {
        return new NetworkTemplate(MATCH_ETHERNET, null, null);
    }

    public static NetworkTemplate buildTemplateBluetooth() {
        return new NetworkTemplate(MATCH_BLUETOOTH, null, null);
    }

    public static NetworkTemplate buildTemplateProxy() {
        return new NetworkTemplate(MATCH_PROXY, null, null);
    }

    public NetworkTemplate(int matchRule, String subscriberId, String networkId) {
        String[] strArr = new String[MATCH_MOBILE_ALL];
        strArr[0] = subscriberId;
        this(matchRule, subscriberId, strArr, networkId);
    }

    public NetworkTemplate(int matchRule, String subscriberId, String[] matchSubscriberIds, String networkId) {
        this.mMatchRule = matchRule;
        this.mSubscriberId = subscriberId;
        this.mMatchSubscriberIds = matchSubscriberIds;
        this.mNetworkId = networkId;
    }

    private NetworkTemplate(Parcel in) {
        this.mMatchRule = in.readInt();
        this.mSubscriberId = in.readString();
        this.mMatchSubscriberIds = in.createStringArray();
        this.mNetworkId = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mMatchRule);
        dest.writeString(this.mSubscriberId);
        dest.writeStringArray(this.mMatchSubscriberIds);
        dest.writeString(this.mNetworkId);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("NetworkTemplate: ");
        builder.append("matchRule=").append(getMatchRuleName(this.mMatchRule));
        if (this.mMatchSubscriberIds != null) {
            builder.append(", matchSubscriberIds=").append(Arrays.toString(NetworkIdentity.scrubSubscriberId(this.mMatchSubscriberIds)));
        }
        if (this.mNetworkId != null) {
            builder.append(", networkId=").append(this.mNetworkId);
        }
        return builder.toString();
    }

    public int hashCode() {
        Object[] objArr = new Object[MATCH_MOBILE_4G];
        objArr[0] = Integer.valueOf(this.mMatchRule);
        objArr[MATCH_MOBILE_ALL] = this.mSubscriberId;
        objArr[MATCH_MOBILE_3G_LOWER] = this.mNetworkId;
        return Objects.hash(objArr);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof NetworkTemplate)) {
            return false;
        }
        NetworkTemplate other = (NetworkTemplate) obj;
        if (this.mMatchRule == other.mMatchRule && Objects.equals(this.mSubscriberId, other.mSubscriberId)) {
            z = Objects.equals(this.mNetworkId, other.mNetworkId);
        }
        return z;
    }

    public boolean isMatchRuleMobile() {
        switch (this.mMatchRule) {
            case MATCH_MOBILE_ALL /*1*/:
            case MATCH_MOBILE_3G_LOWER /*2*/:
            case MATCH_MOBILE_4G /*3*/:
            case MATCH_MOBILE_WILDCARD /*6*/:
                return true;
            default:
                return false;
        }
    }

    public boolean isPersistable() {
        switch (this.mMatchRule) {
            case MATCH_MOBILE_WILDCARD /*6*/:
            case MATCH_WIFI_WILDCARD /*7*/:
                return false;
            default:
                return true;
        }
    }

    public int getMatchRule() {
        return this.mMatchRule;
    }

    public String getSubscriberId() {
        return this.mSubscriberId;
    }

    public String getNetworkId() {
        return this.mNetworkId;
    }

    public boolean matches(NetworkIdentity ident) {
        switch (this.mMatchRule) {
            case MATCH_MOBILE_ALL /*1*/:
                return matchesMobile(ident);
            case MATCH_MOBILE_3G_LOWER /*2*/:
                return matchesMobile3gLower(ident);
            case MATCH_MOBILE_4G /*3*/:
                return matchesMobile4g(ident);
            case MATCH_WIFI /*4*/:
                return matchesWifi(ident);
            case MATCH_ETHERNET /*5*/:
                return matchesEthernet(ident);
            case MATCH_MOBILE_WILDCARD /*6*/:
                return matchesMobileWildcard(ident);
            case MATCH_WIFI_WILDCARD /*7*/:
                return matchesWifiWildcard(ident);
            case MATCH_BLUETOOTH /*8*/:
                return matchesBluetooth(ident);
            case MATCH_PROXY /*9*/:
                return matchesProxy(ident);
            default:
                throw new IllegalArgumentException("unknown network template");
        }
    }

    private boolean matchesMobile(NetworkIdentity ident) {
        boolean z = false;
        if (ident.mType == MATCH_MOBILE_WILDCARD) {
            return true;
        }
        if ((sForceAllNetworkTypes || (ident.mType == 0 && ident.mMetered)) && !ArrayUtils.isEmpty(this.mMatchSubscriberIds)) {
            z = ArrayUtils.contains(this.mMatchSubscriberIds, ident.mSubscriberId);
        }
        return z;
    }

    @Deprecated
    private boolean matchesMobile3gLower(NetworkIdentity ident) {
        ensureSubtypeAvailable();
        if (ident.mType != MATCH_MOBILE_WILDCARD && matchesMobile(ident)) {
            switch (TelephonyManager.getNetworkClass(ident.mSubType)) {
                case TextToSpeech.SUCCESS /*0*/:
                case MATCH_MOBILE_ALL /*1*/:
                case MATCH_MOBILE_3G_LOWER /*2*/:
                    return true;
            }
        }
        return false;
    }

    @Deprecated
    private boolean matchesMobile4g(NetworkIdentity ident) {
        ensureSubtypeAvailable();
        if (ident.mType == MATCH_MOBILE_WILDCARD) {
            return true;
        }
        if (matchesMobile(ident)) {
            switch (TelephonyManager.getNetworkClass(ident.mSubType)) {
                case MATCH_MOBILE_4G /*3*/:
                    return true;
            }
        }
        return false;
    }

    private boolean matchesWifi(NetworkIdentity ident) {
        switch (ident.mType) {
            case MATCH_MOBILE_ALL /*1*/:
                return Objects.equals(WifiInfo.removeDoubleQuotes(this.mNetworkId), WifiInfo.removeDoubleQuotes(ident.mNetworkId));
            default:
                return false;
        }
    }

    private boolean matchesEthernet(NetworkIdentity ident) {
        if (ident.mType == MATCH_PROXY) {
            return true;
        }
        return false;
    }

    private boolean matchesMobileWildcard(NetworkIdentity ident) {
        boolean z = true;
        if (ident.mType == MATCH_MOBILE_WILDCARD) {
            return true;
        }
        if (!sForceAllNetworkTypes) {
            z = ident.mType == 0 ? ident.mMetered : false;
        }
        return z;
    }

    private boolean matchesWifiWildcard(NetworkIdentity ident) {
        switch (ident.mType) {
            case MATCH_MOBILE_ALL /*1*/:
            case NotificationRankerService.REASON_GROUP_OPTIMIZATION /*13*/:
                return true;
            default:
                return false;
        }
    }

    private boolean matchesBluetooth(NetworkIdentity ident) {
        if (ident.mType == MATCH_WIFI_WILDCARD) {
            return true;
        }
        return false;
    }

    private boolean matchesProxy(NetworkIdentity ident) {
        return ident.mType == 16;
    }

    private static String getMatchRuleName(int matchRule) {
        switch (matchRule) {
            case MATCH_MOBILE_ALL /*1*/:
                return "MOBILE_ALL";
            case MATCH_MOBILE_3G_LOWER /*2*/:
                return "MOBILE_3G_LOWER";
            case MATCH_MOBILE_4G /*3*/:
                return "MOBILE_4G";
            case MATCH_WIFI /*4*/:
                return "WIFI";
            case MATCH_ETHERNET /*5*/:
                return "ETHERNET";
            case MATCH_MOBILE_WILDCARD /*6*/:
                return "MOBILE_WILDCARD";
            case MATCH_WIFI_WILDCARD /*7*/:
                return "WIFI_WILDCARD";
            case MATCH_BLUETOOTH /*8*/:
                return "BLUETOOTH";
            case MATCH_PROXY /*9*/:
                return "PROXY";
            default:
                return "UNKNOWN";
        }
    }

    private static void ensureSubtypeAvailable() {
    }

    public static NetworkTemplate normalize(NetworkTemplate template, String[] merged) {
        if (template.isMatchRuleMobile() && ArrayUtils.contains(merged, template.mSubscriberId)) {
            return new NetworkTemplate(template.mMatchRule, merged[0], merged, template.mNetworkId);
        }
        return template;
    }

    public byte[] getBytesForBackup() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeInt(MATCH_MOBILE_ALL);
        out.writeInt(this.mMatchRule);
        BackupUtils.writeString(out, this.mSubscriberId);
        BackupUtils.writeString(out, this.mNetworkId);
        return baos.toByteArray();
    }

    public static NetworkTemplate getNetworkTemplateFromBackup(DataInputStream in) throws IOException, BadVersionException {
        int version = in.readInt();
        if (version >= MATCH_MOBILE_ALL && version <= MATCH_MOBILE_ALL) {
            return new NetworkTemplate(in.readInt(), BackupUtils.readString(in), BackupUtils.readString(in));
        }
        throw new BadVersionException("Unknown Backup Serialization Version");
    }
}
