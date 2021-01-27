package android.hardware.wifi.supplicant.V1_0;

import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.NativeHandle;
import android.os.RemoteException;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public interface ISupplicantStaNetwork extends ISupplicantNetwork {
    public static final String kInterfaceName = "android.hardware.wifi.supplicant@1.0::ISupplicantStaNetwork";

    @FunctionalInterface
    public interface getAuthAlgCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    @FunctionalInterface
    public interface getBssidCallback {
        void onValues(SupplicantStatus supplicantStatus, byte[] bArr);
    }

    @FunctionalInterface
    public interface getEapAltSubjectMatchCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    @FunctionalInterface
    public interface getEapAnonymousIdentityCallback {
        void onValues(SupplicantStatus supplicantStatus, ArrayList<Byte> arrayList);
    }

    @FunctionalInterface
    public interface getEapCACertCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    @FunctionalInterface
    public interface getEapCAPathCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    @FunctionalInterface
    public interface getEapClientCertCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    @FunctionalInterface
    public interface getEapDomainSuffixMatchCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    @FunctionalInterface
    public interface getEapEngineCallback {
        void onValues(SupplicantStatus supplicantStatus, boolean z);
    }

    @FunctionalInterface
    public interface getEapEngineIDCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    @FunctionalInterface
    public interface getEapIdentityCallback {
        void onValues(SupplicantStatus supplicantStatus, ArrayList<Byte> arrayList);
    }

    @FunctionalInterface
    public interface getEapMethodCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    @FunctionalInterface
    public interface getEapPasswordCallback {
        void onValues(SupplicantStatus supplicantStatus, ArrayList<Byte> arrayList);
    }

    @FunctionalInterface
    public interface getEapPhase2MethodCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    @FunctionalInterface
    public interface getEapPrivateKeyIdCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    @FunctionalInterface
    public interface getEapSubjectMatchCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    @FunctionalInterface
    public interface getGroupCipherCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    @FunctionalInterface
    public interface getIdStrCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    @FunctionalInterface
    public interface getKeyMgmtCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    @FunctionalInterface
    public interface getPairwiseCipherCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    @FunctionalInterface
    public interface getProtoCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    @FunctionalInterface
    public interface getPskCallback {
        void onValues(SupplicantStatus supplicantStatus, byte[] bArr);
    }

    @FunctionalInterface
    public interface getPskPassphraseCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    @FunctionalInterface
    public interface getRequirePmfCallback {
        void onValues(SupplicantStatus supplicantStatus, boolean z);
    }

    @FunctionalInterface
    public interface getScanSsidCallback {
        void onValues(SupplicantStatus supplicantStatus, boolean z);
    }

    @FunctionalInterface
    public interface getSsidCallback {
        void onValues(SupplicantStatus supplicantStatus, ArrayList<Byte> arrayList);
    }

    @FunctionalInterface
    public interface getWepKeyCallback {
        void onValues(SupplicantStatus supplicantStatus, ArrayList<Byte> arrayList);
    }

    @FunctionalInterface
    public interface getWepTxKeyIdxCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    @FunctionalInterface
    public interface getWpsNfcConfigurationTokenCallback {
        void onValues(SupplicantStatus supplicantStatus, ArrayList<Byte> arrayList);
    }

    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    SupplicantStatus disable() throws RemoteException;

    SupplicantStatus enable(boolean z) throws RemoteException;

    void getAuthAlg(getAuthAlgCallback getauthalgcallback) throws RemoteException;

    void getBssid(getBssidCallback getbssidcallback) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    void getEapAltSubjectMatch(getEapAltSubjectMatchCallback geteapaltsubjectmatchcallback) throws RemoteException;

    void getEapAnonymousIdentity(getEapAnonymousIdentityCallback geteapanonymousidentitycallback) throws RemoteException;

    void getEapCACert(getEapCACertCallback geteapcacertcallback) throws RemoteException;

    void getEapCAPath(getEapCAPathCallback geteapcapathcallback) throws RemoteException;

    void getEapClientCert(getEapClientCertCallback geteapclientcertcallback) throws RemoteException;

    void getEapDomainSuffixMatch(getEapDomainSuffixMatchCallback geteapdomainsuffixmatchcallback) throws RemoteException;

    void getEapEngine(getEapEngineCallback geteapenginecallback) throws RemoteException;

    void getEapEngineID(getEapEngineIDCallback geteapengineidcallback) throws RemoteException;

    void getEapIdentity(getEapIdentityCallback geteapidentitycallback) throws RemoteException;

    void getEapMethod(getEapMethodCallback geteapmethodcallback) throws RemoteException;

    void getEapPassword(getEapPasswordCallback geteappasswordcallback) throws RemoteException;

    void getEapPhase2Method(getEapPhase2MethodCallback geteapphase2methodcallback) throws RemoteException;

    void getEapPrivateKeyId(getEapPrivateKeyIdCallback geteapprivatekeyidcallback) throws RemoteException;

    void getEapSubjectMatch(getEapSubjectMatchCallback geteapsubjectmatchcallback) throws RemoteException;

    void getGroupCipher(getGroupCipherCallback getgroupciphercallback) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getIdStr(getIdStrCallback getidstrcallback) throws RemoteException;

    void getKeyMgmt(getKeyMgmtCallback getkeymgmtcallback) throws RemoteException;

    void getPairwiseCipher(getPairwiseCipherCallback getpairwiseciphercallback) throws RemoteException;

    void getProto(getProtoCallback getprotocallback) throws RemoteException;

    void getPsk(getPskCallback getpskcallback) throws RemoteException;

    void getPskPassphrase(getPskPassphraseCallback getpskpassphrasecallback) throws RemoteException;

    void getRequirePmf(getRequirePmfCallback getrequirepmfcallback) throws RemoteException;

    void getScanSsid(getScanSsidCallback getscanssidcallback) throws RemoteException;

    void getSsid(getSsidCallback getssidcallback) throws RemoteException;

    void getWepKey(int i, getWepKeyCallback getwepkeycallback) throws RemoteException;

    void getWepTxKeyIdx(getWepTxKeyIdxCallback getweptxkeyidxcallback) throws RemoteException;

    void getWpsNfcConfigurationToken(getWpsNfcConfigurationTokenCallback getwpsnfcconfigurationtokencallback) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    SupplicantStatus registerCallback(ISupplicantStaNetworkCallback iSupplicantStaNetworkCallback) throws RemoteException;

    SupplicantStatus select() throws RemoteException;

    SupplicantStatus sendNetworkEapIdentityResponse(ArrayList<Byte> arrayList) throws RemoteException;

    SupplicantStatus sendNetworkEapSimGsmAuthFailure() throws RemoteException;

    SupplicantStatus sendNetworkEapSimGsmAuthResponse(ArrayList<NetworkResponseEapSimGsmAuthParams> arrayList) throws RemoteException;

    SupplicantStatus sendNetworkEapSimUmtsAuthFailure() throws RemoteException;

    SupplicantStatus sendNetworkEapSimUmtsAuthResponse(NetworkResponseEapSimUmtsAuthParams networkResponseEapSimUmtsAuthParams) throws RemoteException;

    SupplicantStatus sendNetworkEapSimUmtsAutsResponse(byte[] bArr) throws RemoteException;

    SupplicantStatus setAuthAlg(int i) throws RemoteException;

    SupplicantStatus setBssid(byte[] bArr) throws RemoteException;

    SupplicantStatus setEapAltSubjectMatch(String str) throws RemoteException;

    SupplicantStatus setEapAnonymousIdentity(ArrayList<Byte> arrayList) throws RemoteException;

    SupplicantStatus setEapCACert(String str) throws RemoteException;

    SupplicantStatus setEapCAPath(String str) throws RemoteException;

    SupplicantStatus setEapClientCert(String str) throws RemoteException;

    SupplicantStatus setEapDomainSuffixMatch(String str) throws RemoteException;

    SupplicantStatus setEapEngine(boolean z) throws RemoteException;

    SupplicantStatus setEapEngineID(String str) throws RemoteException;

    SupplicantStatus setEapIdentity(ArrayList<Byte> arrayList) throws RemoteException;

    SupplicantStatus setEapMethod(int i) throws RemoteException;

    SupplicantStatus setEapPassword(ArrayList<Byte> arrayList) throws RemoteException;

    SupplicantStatus setEapPhase2Method(int i) throws RemoteException;

    SupplicantStatus setEapPrivateKeyId(String str) throws RemoteException;

    SupplicantStatus setEapSubjectMatch(String str) throws RemoteException;

    SupplicantStatus setGroupCipher(int i) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    SupplicantStatus setIdStr(String str) throws RemoteException;

    SupplicantStatus setKeyMgmt(int i) throws RemoteException;

    SupplicantStatus setPairwiseCipher(int i) throws RemoteException;

    SupplicantStatus setProactiveKeyCaching(boolean z) throws RemoteException;

    SupplicantStatus setProto(int i) throws RemoteException;

    SupplicantStatus setPsk(byte[] bArr) throws RemoteException;

    SupplicantStatus setPskPassphrase(String str) throws RemoteException;

    SupplicantStatus setRequirePmf(boolean z) throws RemoteException;

    SupplicantStatus setScanSsid(boolean z) throws RemoteException;

    SupplicantStatus setSsid(ArrayList<Byte> arrayList) throws RemoteException;

    SupplicantStatus setUpdateIdentifier(int i) throws RemoteException;

    SupplicantStatus setWepKey(int i, ArrayList<Byte> arrayList) throws RemoteException;

    SupplicantStatus setWepTxKeyIdx(int i) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static ISupplicantStaNetwork asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof ISupplicantStaNetwork)) {
            return (ISupplicantStaNetwork) iface;
        }
        ISupplicantStaNetwork proxy = new Proxy(binder);
        try {
            Iterator<String> it = proxy.interfaceChain().iterator();
            while (it.hasNext()) {
                if (it.next().equals(kInterfaceName)) {
                    return proxy;
                }
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    static ISupplicantStaNetwork castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static ISupplicantStaNetwork getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static ISupplicantStaNetwork getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    static ISupplicantStaNetwork getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static ISupplicantStaNetwork getService() throws RemoteException {
        return getService("default");
    }

    public static final class ParamSizeLimits {
        public static final int PSK_PASSPHRASE_MAX_LEN_IN_BYTES = 63;
        public static final int PSK_PASSPHRASE_MIN_LEN_IN_BYTES = 8;
        public static final int SSID_MAX_LEN_IN_BYTES = 32;
        public static final int WEP104_KEY_LEN_IN_BYTES = 13;
        public static final int WEP40_KEY_LEN_IN_BYTES = 5;
        public static final int WEP_KEYS_MAX_NUM = 4;

        public static final String toString(int o) {
            if (o == 32) {
                return "SSID_MAX_LEN_IN_BYTES";
            }
            if (o == 8) {
                return "PSK_PASSPHRASE_MIN_LEN_IN_BYTES";
            }
            if (o == 63) {
                return "PSK_PASSPHRASE_MAX_LEN_IN_BYTES";
            }
            if (o == 4) {
                return "WEP_KEYS_MAX_NUM";
            }
            if (o == 5) {
                return "WEP40_KEY_LEN_IN_BYTES";
            }
            if (o == 13) {
                return "WEP104_KEY_LEN_IN_BYTES";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 32) == 32) {
                list.add("SSID_MAX_LEN_IN_BYTES");
                flipped = 0 | 32;
            }
            if ((o & 8) == 8) {
                list.add("PSK_PASSPHRASE_MIN_LEN_IN_BYTES");
                flipped |= 8;
            }
            if ((o & 63) == 63) {
                list.add("PSK_PASSPHRASE_MAX_LEN_IN_BYTES");
                flipped |= 63;
            }
            if ((o & 4) == 4) {
                list.add("WEP_KEYS_MAX_NUM");
                flipped |= 4;
            }
            if ((o & 5) == 5) {
                list.add("WEP40_KEY_LEN_IN_BYTES");
                flipped |= 5;
            }
            if ((o & 13) == 13) {
                list.add("WEP104_KEY_LEN_IN_BYTES");
                flipped |= 13;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class KeyMgmtMask {
        public static final int FT_EAP = 32;
        public static final int FT_PSK = 64;
        public static final int IEEE8021X = 8;
        public static final int NONE = 4;
        public static final int OSEN = 32768;
        public static final int WPA_EAP = 1;
        public static final int WPA_PSK = 2;

        public static final String toString(int o) {
            if (o == 1) {
                return "WPA_EAP";
            }
            if (o == 2) {
                return "WPA_PSK";
            }
            if (o == 4) {
                return "NONE";
            }
            if (o == 8) {
                return "IEEE8021X";
            }
            if (o == 32) {
                return "FT_EAP";
            }
            if (o == 64) {
                return "FT_PSK";
            }
            if (o == 32768) {
                return "OSEN";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("WPA_EAP");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("WPA_PSK");
                flipped |= 2;
            }
            if ((o & 4) == 4) {
                list.add("NONE");
                flipped |= 4;
            }
            if ((o & 8) == 8) {
                list.add("IEEE8021X");
                flipped |= 8;
            }
            if ((o & 32) == 32) {
                list.add("FT_EAP");
                flipped |= 32;
            }
            if ((o & 64) == 64) {
                list.add("FT_PSK");
                flipped |= 64;
            }
            if ((o & 32768) == 32768) {
                list.add("OSEN");
                flipped |= 32768;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class ProtoMask {
        public static final int OSEN = 8;
        public static final int RSN = 2;
        public static final int WPA = 1;

        public static final String toString(int o) {
            if (o == 1) {
                return "WPA";
            }
            if (o == 2) {
                return "RSN";
            }
            if (o == 8) {
                return "OSEN";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("WPA");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("RSN");
                flipped |= 2;
            }
            if ((o & 8) == 8) {
                list.add("OSEN");
                flipped |= 8;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class AuthAlgMask {
        public static final int LEAP = 4;
        public static final int OPEN = 1;
        public static final int SHARED = 2;

        public static final String toString(int o) {
            if (o == 1) {
                return "OPEN";
            }
            if (o == 2) {
                return "SHARED";
            }
            if (o == 4) {
                return "LEAP";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("OPEN");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("SHARED");
                flipped |= 2;
            }
            if ((o & 4) == 4) {
                list.add("LEAP");
                flipped |= 4;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class GroupCipherMask {
        public static final int CCMP = 16;
        public static final int GTK_NOT_USED = 16384;
        public static final int TKIP = 8;
        public static final int WEP104 = 4;
        public static final int WEP40 = 2;

        public static final String toString(int o) {
            if (o == 2) {
                return "WEP40";
            }
            if (o == 4) {
                return "WEP104";
            }
            if (o == 8) {
                return "TKIP";
            }
            if (o == 16) {
                return "CCMP";
            }
            if (o == 16384) {
                return "GTK_NOT_USED";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 2) == 2) {
                list.add("WEP40");
                flipped = 0 | 2;
            }
            if ((o & 4) == 4) {
                list.add("WEP104");
                flipped |= 4;
            }
            if ((o & 8) == 8) {
                list.add("TKIP");
                flipped |= 8;
            }
            if ((o & 16) == 16) {
                list.add("CCMP");
                flipped |= 16;
            }
            if ((o & 16384) == 16384) {
                list.add("GTK_NOT_USED");
                flipped |= 16384;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class PairwiseCipherMask {
        public static final int CCMP = 16;
        public static final int NONE = 1;
        public static final int TKIP = 8;

        public static final String toString(int o) {
            if (o == 1) {
                return "NONE";
            }
            if (o == 8) {
                return "TKIP";
            }
            if (o == 16) {
                return "CCMP";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("NONE");
                flipped = 0 | 1;
            }
            if ((o & 8) == 8) {
                list.add("TKIP");
                flipped |= 8;
            }
            if ((o & 16) == 16) {
                list.add("CCMP");
                flipped |= 16;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class EapMethod {
        public static final int AKA = 5;
        public static final int AKA_PRIME = 6;
        public static final int PEAP = 0;
        public static final int PWD = 3;
        public static final int SIM = 4;
        public static final int TLS = 1;
        public static final int TTLS = 2;
        public static final int WFA_UNAUTH_TLS = 7;

        public static final String toString(int o) {
            if (o == 0) {
                return "PEAP";
            }
            if (o == 1) {
                return "TLS";
            }
            if (o == 2) {
                return "TTLS";
            }
            if (o == 3) {
                return "PWD";
            }
            if (o == 4) {
                return "SIM";
            }
            if (o == 5) {
                return "AKA";
            }
            if (o == 6) {
                return "AKA_PRIME";
            }
            if (o == 7) {
                return "WFA_UNAUTH_TLS";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            list.add("PEAP");
            if ((o & 1) == 1) {
                list.add("TLS");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("TTLS");
                flipped |= 2;
            }
            if ((o & 3) == 3) {
                list.add("PWD");
                flipped |= 3;
            }
            if ((o & 4) == 4) {
                list.add("SIM");
                flipped |= 4;
            }
            if ((o & 5) == 5) {
                list.add("AKA");
                flipped |= 5;
            }
            if ((o & 6) == 6) {
                list.add("AKA_PRIME");
                flipped |= 6;
            }
            if ((o & 7) == 7) {
                list.add("WFA_UNAUTH_TLS");
                flipped |= 7;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class EapPhase2Method {
        public static final int AKA = 6;
        public static final int AKA_PRIME = 7;
        public static final int GTC = 4;
        public static final int MSPAP = 2;
        public static final int MSPAPV2 = 3;
        public static final int NONE = 0;
        public static final int PAP = 1;
        public static final int SIM = 5;

        public static final String toString(int o) {
            if (o == 0) {
                return "NONE";
            }
            if (o == 1) {
                return "PAP";
            }
            if (o == 2) {
                return "MSPAP";
            }
            if (o == 3) {
                return "MSPAPV2";
            }
            if (o == 4) {
                return "GTC";
            }
            if (o == 5) {
                return "SIM";
            }
            if (o == 6) {
                return "AKA";
            }
            if (o == 7) {
                return "AKA_PRIME";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            list.add("NONE");
            if ((o & 1) == 1) {
                list.add("PAP");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("MSPAP");
                flipped |= 2;
            }
            if ((o & 3) == 3) {
                list.add("MSPAPV2");
                flipped |= 3;
            }
            if ((o & 4) == 4) {
                list.add("GTC");
                flipped |= 4;
            }
            if ((o & 5) == 5) {
                list.add("SIM");
                flipped |= 5;
            }
            if ((o & 6) == 6) {
                list.add("AKA");
                flipped |= 6;
            }
            if ((o & 7) == 7) {
                list.add("AKA_PRIME");
                flipped |= 7;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class NetworkResponseEapSimGsmAuthParams {
        public byte[] kc = new byte[8];
        public byte[] sres = new byte[4];

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != NetworkResponseEapSimGsmAuthParams.class) {
                return false;
            }
            NetworkResponseEapSimGsmAuthParams other = (NetworkResponseEapSimGsmAuthParams) otherObject;
            if (HidlSupport.deepEquals(this.kc, other.kc) && HidlSupport.deepEquals(this.sres, other.sres)) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.kc)), Integer.valueOf(HidlSupport.deepHashCode(this.sres)));
        }

        public final String toString() {
            return "{.kc = " + Arrays.toString(this.kc) + ", .sres = " + Arrays.toString(this.sres) + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(12), 0);
        }

        public static final ArrayList<NetworkResponseEapSimGsmAuthParams> readVectorFromParcel(HwParcel parcel) {
            ArrayList<NetworkResponseEapSimGsmAuthParams> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 12), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                NetworkResponseEapSimGsmAuthParams _hidl_vec_element = new NetworkResponseEapSimGsmAuthParams();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 12));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            _hidl_blob.copyToInt8Array(0 + _hidl_offset, this.kc, 8);
            _hidl_blob.copyToInt8Array(8 + _hidl_offset, this.sres, 4);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(12);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NetworkResponseEapSimGsmAuthParams> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 12);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 12));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            long _hidl_array_offset_0 = 0 + _hidl_offset;
            byte[] _hidl_array_item_0 = this.kc;
            if (_hidl_array_item_0 == null || _hidl_array_item_0.length != 8) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(_hidl_array_offset_0, _hidl_array_item_0);
            long _hidl_array_offset_02 = 8 + _hidl_offset;
            byte[] _hidl_array_item_02 = this.sres;
            if (_hidl_array_item_02 == null || _hidl_array_item_02.length != 4) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(_hidl_array_offset_02, _hidl_array_item_02);
        }
    }

    public static final class NetworkResponseEapSimUmtsAuthParams {
        public byte[] ck = new byte[16];
        public byte[] ik = new byte[16];
        public ArrayList<Byte> res = new ArrayList<>();

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != NetworkResponseEapSimUmtsAuthParams.class) {
                return false;
            }
            NetworkResponseEapSimUmtsAuthParams other = (NetworkResponseEapSimUmtsAuthParams) otherObject;
            if (HidlSupport.deepEquals(this.res, other.res) && HidlSupport.deepEquals(this.ik, other.ik) && HidlSupport.deepEquals(this.ck, other.ck)) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.res)), Integer.valueOf(HidlSupport.deepHashCode(this.ik)), Integer.valueOf(HidlSupport.deepHashCode(this.ck)));
        }

        public final String toString() {
            return "{.res = " + this.res + ", .ik = " + Arrays.toString(this.ik) + ", .ck = " + Arrays.toString(this.ck) + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
        }

        public static final ArrayList<NetworkResponseEapSimUmtsAuthParams> readVectorFromParcel(HwParcel parcel) {
            ArrayList<NetworkResponseEapSimUmtsAuthParams> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                NetworkResponseEapSimUmtsAuthParams _hidl_vec_element = new NetworkResponseEapSimUmtsAuthParams();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 0 + 8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, true);
            this.res.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                this.res.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
            }
            _hidl_blob.copyToInt8Array(_hidl_offset + 16, this.ik, 16);
            _hidl_blob.copyToInt8Array(_hidl_offset + 32, this.ck, 16);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(48);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NetworkResponseEapSimUmtsAuthParams> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            int _hidl_vec_size = this.res.size();
            _hidl_blob.putInt32(_hidl_offset + 0 + 8, _hidl_vec_size);
            _hidl_blob.putBool(_hidl_offset + 0 + 12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                childBlob.putInt8((long) (_hidl_index_0 * 1), this.res.get(_hidl_index_0).byteValue());
            }
            _hidl_blob.putBlob(_hidl_offset + 0 + 0, childBlob);
            long _hidl_array_offset_0 = 16 + _hidl_offset;
            byte[] _hidl_array_item_0 = this.ik;
            if (_hidl_array_item_0 == null || _hidl_array_item_0.length != 16) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(_hidl_array_offset_0, _hidl_array_item_0);
            long _hidl_array_offset_02 = 32 + _hidl_offset;
            byte[] _hidl_array_item_02 = this.ck;
            if (_hidl_array_item_02 == null || _hidl_array_item_02.length != 16) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(_hidl_array_offset_02, _hidl_array_item_02);
        }
    }

    public static final class Proxy implements ISupplicantStaNetwork {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of android.hardware.wifi.supplicant@1.0::ISupplicantStaNetwork]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork
        public void getId(ISupplicantNetwork.getIdCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork
        public void getInterfaceName(ISupplicantNetwork.getInterfaceNameCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork
        public void getType(ISupplicantNetwork.getTypeCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus registerCallback(ISupplicantStaNetworkCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setSsid(ArrayList<Byte> ssid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt8Vector(ssid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setBssid(byte[] bssid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setScanSsid(boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setKeyMgmt(int keyMgmtMask) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(keyMgmtMask);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setProto(int protoMask) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(protoMask);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setAuthAlg(int authAlgMask) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(authAlgMask);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setGroupCipher(int groupCipherMask) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(groupCipherMask);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setPairwiseCipher(int pairwiseCipherMask) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(pairwiseCipherMask);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setPskPassphrase(String psk) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(psk);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setPsk(byte[] psk) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(32);
            if (psk == null || psk.length != 32) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, psk);
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setWepKey(int keyIdx, ArrayList<Byte> wepKey) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(keyIdx);
            _hidl_request.writeInt8Vector(wepKey);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setWepTxKeyIdx(int keyIdx) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(keyIdx);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setRequirePmf(boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setEapMethod(int method) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(method);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setEapPhase2Method(int method) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(method);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setEapIdentity(ArrayList<Byte> identity) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt8Vector(identity);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setEapAnonymousIdentity(ArrayList<Byte> identity) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt8Vector(identity);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setEapPassword(ArrayList<Byte> password) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt8Vector(password);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setEapCACert(String path) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(path);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setEapCAPath(String path) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(path);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setEapClientCert(String path) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(path);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setEapPrivateKeyId(String id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setEapSubjectMatch(String match) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(match);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setEapAltSubjectMatch(String match) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(match);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setEapEngine(boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(29, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setEapEngineID(String id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(30, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setEapDomainSuffixMatch(String match) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(match);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(31, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setProactiveKeyCaching(boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(32, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setIdStr(String idStr) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(idStr);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(33, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus setUpdateIdentifier(int id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(34, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getSsid(getSsidCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(35, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getBssid(getBssidCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(36, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                byte[] _hidl_out_bssid = new byte[6];
                _hidl_reply.readBuffer(6).copyToInt8Array(0, _hidl_out_bssid, 6);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_bssid);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getScanSsid(getScanSsidCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(37, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readBool());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getKeyMgmt(getKeyMgmtCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(38, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getProto(getProtoCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(39, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getAuthAlg(getAuthAlgCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(40, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getGroupCipher(getGroupCipherCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(41, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getPairwiseCipher(getPairwiseCipherCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(42, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getPskPassphrase(getPskPassphraseCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(43, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getPsk(getPskCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(44, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                byte[] _hidl_out_psk = new byte[32];
                _hidl_reply.readBuffer(32).copyToInt8Array(0, _hidl_out_psk, 32);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_psk);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getWepKey(int keyIdx, getWepKeyCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(keyIdx);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(45, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getWepTxKeyIdx(getWepTxKeyIdxCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(46, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getRequirePmf(getRequirePmfCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(47, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readBool());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getEapMethod(getEapMethodCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(48, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getEapPhase2Method(getEapPhase2MethodCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(49, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getEapIdentity(getEapIdentityCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(50, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getEapAnonymousIdentity(getEapAnonymousIdentityCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(51, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getEapPassword(getEapPasswordCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(52, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getEapCACert(getEapCACertCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(53, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getEapCAPath(getEapCAPathCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(54, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getEapClientCert(getEapClientCertCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(55, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getEapPrivateKeyId(getEapPrivateKeyIdCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(56, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getEapSubjectMatch(getEapSubjectMatchCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(57, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getEapAltSubjectMatch(getEapAltSubjectMatchCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(58, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getEapEngine(getEapEngineCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(59, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readBool());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getEapEngineID(getEapEngineIDCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(60, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getEapDomainSuffixMatch(getEapDomainSuffixMatchCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(61, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getIdStr(getIdStrCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(62, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public void getWpsNfcConfigurationToken(getWpsNfcConfigurationTokenCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(63, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus enable(boolean noConnect) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeBool(noConnect);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(64, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus disable() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(65, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus select() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(66, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus sendNetworkEapSimGsmAuthResponse(ArrayList<NetworkResponseEapSimGsmAuthParams> params) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            NetworkResponseEapSimGsmAuthParams.writeVectorToParcel(_hidl_request, params);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(67, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus sendNetworkEapSimGsmAuthFailure() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(68, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus sendNetworkEapSimUmtsAuthResponse(NetworkResponseEapSimUmtsAuthParams params) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            params.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(69, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus sendNetworkEapSimUmtsAutsResponse(byte[] auts) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(14);
            if (auts == null || auts.length != 14) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, auts);
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(70, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus sendNetworkEapSimUmtsAuthFailure() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(71, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork
        public SupplicantStatus sendNetworkEapIdentityResponse(ArrayList<Byte> identity) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt8Vector(identity);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(72, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256067662, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readStringVector();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> options) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            _hidl_request.writeNativeHandle(fd);
            _hidl_request.writeStringVector(options);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256131655, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public String interfaceDescriptor() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256136003, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readString();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList<>();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                    byte[] _hidl_vec_element = new byte[32];
                    childBlob.copyToInt8Array((long) (_hidl_index_0 * 32), _hidl_vec_element, 32);
                    _hidl_out_hashchain.add(_hidl_vec_element);
                }
                return _hidl_out_hashchain;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public void setHALInstrumentation() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256462420, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public void ping() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256921159, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public DebugInfo getDebugInfo() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257049926, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                DebugInfo _hidl_out_info = new DebugInfo();
                _hidl_out_info.readFromParcel(_hidl_reply);
                return _hidl_out_info;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public void notifySyspropsChanged() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257120595, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements ISupplicantStaNetwork {
        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(ISupplicantStaNetwork.kInterfaceName, ISupplicantNetwork.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return ISupplicantStaNetwork.kInterfaceName;
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{-79, 46, -16, -67, -40, -92, -46, 71, -88, -90, -23, 96, -78, 39, -19, 50, 56, 63, 43, 2, 65, -11, 93, 103, -4, -22, 110, -1, 106, 103, 55, -6}, new byte[]{-51, -96, 16, 8, -64, 105, 34, -6, 55, -63, 33, 62, -101, -72, 49, -95, 9, -77, 23, 69, 50, Byte.MIN_VALUE, 86, 22, -5, 113, 97, -19, -60, 3, -122, 111}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (ISupplicantStaNetwork.kInterfaceName.equals(descriptor)) {
                return this;
            }
            return null;
        }

        public void registerAsService(String serviceName) throws RemoteException {
            registerService(serviceName);
        }

        public String toString() {
            return interfaceDescriptor() + "@Stub";
        }

        public void onTransact(int _hidl_code, HwParcel _hidl_request, final HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            boolean _hidl_is_oneway = false;
            boolean _hidl_is_oneway2 = true;
            switch (_hidl_code) {
                case 1:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantNetwork.kInterfaceName);
                    getId(new ISupplicantNetwork.getIdCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass1 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork.getIdCallback
                        public void onValues(SupplicantStatus status, int id) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(id);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 2:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantNetwork.kInterfaceName);
                    getInterfaceName(new ISupplicantNetwork.getInterfaceNameCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass2 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork.getInterfaceNameCallback
                        public void onValues(SupplicantStatus status, String name) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(name);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 3:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantNetwork.kInterfaceName);
                    getType(new ISupplicantNetwork.getTypeCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass3 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork.getTypeCallback
                        public void onValues(SupplicantStatus status, int type) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(type);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 4:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status = registerCallback(ISupplicantStaNetworkCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 5:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status2 = setSsid(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status2.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 6:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    byte[] bssid = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid, 6);
                    SupplicantStatus _hidl_out_status3 = setBssid(bssid);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status3.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 7:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status4 = setScanSsid(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status4.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 8:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status5 = setKeyMgmt(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status5.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 9:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status6 = setProto(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status6.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 10:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status7 = setAuthAlg(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status7.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 11:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status8 = setGroupCipher(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status8.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 12:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status9 = setPairwiseCipher(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status9.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 13:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status10 = setPskPassphrase(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status10.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 14:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    byte[] psk = new byte[32];
                    _hidl_request.readBuffer(32).copyToInt8Array(0, psk, 32);
                    SupplicantStatus _hidl_out_status11 = setPsk(psk);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status11.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 15:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status12 = setWepKey(_hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status12.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 16:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status13 = setWepTxKeyIdx(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status13.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 17:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status14 = setRequirePmf(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status14.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 18:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status15 = setEapMethod(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status15.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 19:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status16 = setEapPhase2Method(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status16.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 20:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status17 = setEapIdentity(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status17.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.ReasonCode.UNSUPPORTED_RSN_IE_VERSION /* 21 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status18 = setEapAnonymousIdentity(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status18.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 22:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status19 = setEapPassword(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status19.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 23:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status20 = setEapCACert(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status20.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 24:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status21 = setEapCAPath(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status21.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 25:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status22 = setEapClientCert(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status22.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.ReasonCode.TDLS_TEARDOWN_UNSPECIFIED /* 26 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status23 = setEapPrivateKeyId(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status23.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 27:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status24 = setEapSubjectMatch(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status24.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 28:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status25 = setEapAltSubjectMatch(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status25.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 29:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status26 = setEapEngine(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status26.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 30:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status27 = setEapEngineID(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status27.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 31:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status28 = setEapDomainSuffixMatch(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status28.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 32:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status29 = setProactiveKeyCaching(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status29.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 33:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status30 = setIdStr(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status30.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 34:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status31 = setUpdateIdentifier(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status31.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 35:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getSsid(new getSsidCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass4 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getSsidCallback
                        public void onValues(SupplicantStatus status, ArrayList<Byte> ssid) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt8Vector(ssid);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 36:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getBssid(new getBssidCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass5 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getBssidCallback
                        public void onValues(SupplicantStatus status, byte[] bssid) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            HwBlob _hidl_blob = new HwBlob(6);
                            if (bssid == null || bssid.length != 6) {
                                throw new IllegalArgumentException("Array element is not of the expected length");
                            }
                            _hidl_blob.putInt8Array(0, bssid);
                            _hidl_reply.writeBuffer(_hidl_blob);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 37:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getScanSsid(new getScanSsidCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass6 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getScanSsidCallback
                        public void onValues(SupplicantStatus status, boolean enabled) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeBool(enabled);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 38:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getKeyMgmt(new getKeyMgmtCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass7 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getKeyMgmtCallback
                        public void onValues(SupplicantStatus status, int keyMgmtMask) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(keyMgmtMask);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 39:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getProto(new getProtoCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass8 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getProtoCallback
                        public void onValues(SupplicantStatus status, int protoMask) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(protoMask);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 40:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getAuthAlg(new getAuthAlgCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass9 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getAuthAlgCallback
                        public void onValues(SupplicantStatus status, int authAlgMask) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(authAlgMask);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.GROUP_CIPHER_NOT_VALID /* 41 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getGroupCipher(new getGroupCipherCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass10 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getGroupCipherCallback
                        public void onValues(SupplicantStatus status, int groupCipherMask) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(groupCipherMask);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 42:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getPairwiseCipher(new getPairwiseCipherCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass11 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getPairwiseCipherCallback
                        public void onValues(SupplicantStatus status, int pairwiseCipherMask) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(pairwiseCipherMask);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 43:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getPskPassphrase(new getPskPassphraseCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass12 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getPskPassphraseCallback
                        public void onValues(SupplicantStatus status, String psk) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(psk);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.UNSUPPORTED_RSN_IE_VERSION /* 44 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getPsk(new getPskCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass13 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getPskCallback
                        public void onValues(SupplicantStatus status, byte[] psk) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            HwBlob _hidl_blob = new HwBlob(32);
                            if (psk == null || psk.length != 32) {
                                throw new IllegalArgumentException("Array element is not of the expected length");
                            }
                            _hidl_blob.putInt8Array(0, psk);
                            _hidl_reply.writeBuffer(_hidl_blob);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 45:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getWepKey(_hidl_request.readInt32(), new getWepKeyCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass14 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getWepKeyCallback
                        public void onValues(SupplicantStatus status, ArrayList<Byte> wepKey) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt8Vector(wepKey);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 46:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getWepTxKeyIdx(new getWepTxKeyIdxCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass15 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getWepTxKeyIdxCallback
                        public void onValues(SupplicantStatus status, int keyIdx) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(keyIdx);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 47:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getRequirePmf(new getRequirePmfCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass16 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getRequirePmfCallback
                        public void onValues(SupplicantStatus status, boolean enabled) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeBool(enabled);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 48:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getEapMethod(new getEapMethodCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass17 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapMethodCallback
                        public void onValues(SupplicantStatus status, int method) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(method);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 49:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getEapPhase2Method(new getEapPhase2MethodCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass18 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapPhase2MethodCallback
                        public void onValues(SupplicantStatus status, int method) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(method);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 50:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getEapIdentity(new getEapIdentityCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass19 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapIdentityCallback
                        public void onValues(SupplicantStatus status, ArrayList<Byte> identity) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt8Vector(identity);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 51:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getEapAnonymousIdentity(new getEapAnonymousIdentityCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass20 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapAnonymousIdentityCallback
                        public void onValues(SupplicantStatus status, ArrayList<Byte> identity) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt8Vector(identity);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 52:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getEapPassword(new getEapPasswordCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass21 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapPasswordCallback
                        public void onValues(SupplicantStatus status, ArrayList<Byte> password) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt8Vector(password);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 53:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getEapCACert(new getEapCACertCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass22 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapCACertCallback
                        public void onValues(SupplicantStatus status, String path) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(path);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 54:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getEapCAPath(new getEapCAPathCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass23 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapCAPathCallback
                        public void onValues(SupplicantStatus status, String path) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(path);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 55:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getEapClientCert(new getEapClientCertCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass24 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapClientCertCallback
                        public void onValues(SupplicantStatus status, String path) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(path);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 56:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getEapPrivateKeyId(new getEapPrivateKeyIdCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass25 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapPrivateKeyIdCallback
                        public void onValues(SupplicantStatus status, String id) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(id);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 57:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getEapSubjectMatch(new getEapSubjectMatchCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass26 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapSubjectMatchCallback
                        public void onValues(SupplicantStatus status, String match) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(match);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 58:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getEapAltSubjectMatch(new getEapAltSubjectMatchCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass27 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapAltSubjectMatchCallback
                        public void onValues(SupplicantStatus status, String match) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(match);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 59:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getEapEngine(new getEapEngineCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass28 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapEngineCallback
                        public void onValues(SupplicantStatus status, boolean enabled) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeBool(enabled);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 60:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getEapEngineID(new getEapEngineIDCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass29 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapEngineIDCallback
                        public void onValues(SupplicantStatus status, String id) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(id);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 61:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getEapDomainSuffixMatch(new getEapDomainSuffixMatchCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass30 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapDomainSuffixMatchCallback
                        public void onValues(SupplicantStatus status, String match) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(match);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 62:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getIdStr(new getIdStrCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass31 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getIdStrCallback
                        public void onValues(SupplicantStatus status, String idStr) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(idStr);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 63:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getWpsNfcConfigurationToken(new getWpsNfcConfigurationTokenCallback() {
                        /* class android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.Stub.AnonymousClass32 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getWpsNfcConfigurationTokenCallback
                        public void onValues(SupplicantStatus status, ArrayList<Byte> token) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt8Vector(token);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 64:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status32 = enable(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status32.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 65:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status33 = disable();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status33.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.ReasonCode.MESH_CHANNEL_SWITCH_UNSPECIFIED /* 66 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status34 = select();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status34.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.REQ_REFUSED_SSPN /* 67 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status35 = sendNetworkEapSimGsmAuthResponse(NetworkResponseEapSimGsmAuthParams.readVectorFromParcel(_hidl_request));
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status35.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.REQ_REFUSED_UNAUTH_ACCESS /* 68 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status36 = sendNetworkEapSimGsmAuthFailure();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status36.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 69:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    NetworkResponseEapSimUmtsAuthParams params = new NetworkResponseEapSimUmtsAuthParams();
                    params.readFromParcel(_hidl_request);
                    SupplicantStatus _hidl_out_status37 = sendNetworkEapSimUmtsAuthResponse(params);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status37.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 70:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    byte[] auts = new byte[14];
                    _hidl_request.readBuffer(14).copyToInt8Array(0, auts, 14);
                    SupplicantStatus _hidl_out_status38 = sendNetworkEapSimUmtsAutsResponse(auts);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status38.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 71:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status39 = sendNetworkEapSimUmtsAuthFailure();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status39.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.INVALID_RSNIE /* 72 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status40 = sendNetworkEapIdentityResponse(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status40.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                default:
                    switch (_hidl_code) {
                        case 256067662:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ArrayList<String> _hidl_out_descriptors = interfaceChain();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeStringVector(_hidl_out_descriptors);
                            _hidl_reply.send();
                            return;
                        case 256131655:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            debug(_hidl_request.readNativeHandle(), _hidl_request.readStringVector());
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 256136003:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            String _hidl_out_descriptor = interfaceDescriptor();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeString(_hidl_out_descriptor);
                            _hidl_reply.send();
                            return;
                        case 256398152:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                            _hidl_reply.writeStatus(0);
                            HwBlob _hidl_blob = new HwBlob(16);
                            int _hidl_vec_size = _hidl_out_hashchain.size();
                            _hidl_blob.putInt32(8, _hidl_vec_size);
                            _hidl_blob.putBool(12, false);
                            HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
                            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                                long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                                byte[] _hidl_array_item_1 = _hidl_out_hashchain.get(_hidl_index_0);
                                if (_hidl_array_item_1 == null || _hidl_array_item_1.length != 32) {
                                    throw new IllegalArgumentException("Array element is not of the expected length");
                                }
                                childBlob.putInt8Array(_hidl_array_offset_1, _hidl_array_item_1);
                            }
                            _hidl_blob.putBlob(0, childBlob);
                            _hidl_reply.writeBuffer(_hidl_blob);
                            _hidl_reply.send();
                            return;
                        case 256462420:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (!_hidl_is_oneway) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            setHALInstrumentation();
                            return;
                        case 256660548:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        case 256921159:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ping();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 257049926:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            DebugInfo _hidl_out_info = getDebugInfo();
                            _hidl_reply.writeStatus(0);
                            _hidl_out_info.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                            return;
                        case 257120595:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (!_hidl_is_oneway) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            notifySyspropsChanged();
                            return;
                        case 257250372:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        default:
                            return;
                    }
            }
        }
    }
}
