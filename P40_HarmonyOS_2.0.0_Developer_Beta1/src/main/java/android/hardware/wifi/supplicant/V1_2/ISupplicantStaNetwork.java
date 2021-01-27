package android.hardware.wifi.supplicant.V1_2;

import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetworkCallback;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
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

public interface ISupplicantStaNetwork extends android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork {
    public static final String kInterfaceName = "android.hardware.wifi.supplicant@1.2::ISupplicantStaNetwork";

    @FunctionalInterface
    public interface getGroupCipher_1_2Callback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    @FunctionalInterface
    public interface getGroupMgmtCipherCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    @FunctionalInterface
    public interface getKeyMgmt_1_2Callback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    @FunctionalInterface
    public interface getPairwiseCipher_1_2Callback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    @FunctionalInterface
    public interface getSaePasswordCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    @FunctionalInterface
    public interface getSaePasswordIdCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    SupplicantStatus enableSuiteBEapOpenSslCiphers() throws RemoteException;

    SupplicantStatus enableTlsSuiteBEapPhase1Param(boolean z) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    void getGroupCipher_1_2(getGroupCipher_1_2Callback getgroupcipher_1_2callback) throws RemoteException;

    void getGroupMgmtCipher(getGroupMgmtCipherCallback getgroupmgmtciphercallback) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getKeyMgmt_1_2(getKeyMgmt_1_2Callback getkeymgmt_1_2callback) throws RemoteException;

    void getPairwiseCipher_1_2(getPairwiseCipher_1_2Callback getpairwisecipher_1_2callback) throws RemoteException;

    void getSaePassword(getSaePasswordCallback getsaepasswordcallback) throws RemoteException;

    void getSaePasswordId(getSaePasswordIdCallback getsaepasswordidcallback) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    SupplicantStatus setGroupCipher_1_2(int i) throws RemoteException;

    SupplicantStatus setGroupMgmtCipher(int i) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    SupplicantStatus setKeyMgmt_1_2(int i) throws RemoteException;

    SupplicantStatus setPairwiseCipher_1_2(int i) throws RemoteException;

    SupplicantStatus setSaePassword(String str) throws RemoteException;

    SupplicantStatus setSaePasswordId(String str) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
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

    public static final class KeyMgmtMask {
        public static final int DPP = 8388608;
        public static final int FT_EAP = 32;
        public static final int FT_PSK = 64;
        public static final int IEEE8021X = 8;
        public static final int NONE = 4;
        public static final int OSEN = 32768;
        public static final int OWE = 4194304;
        public static final int SAE = 1024;
        public static final int SUITE_B_192 = 131072;
        public static final int WPA_EAP = 1;
        public static final int WPA_EAP_SHA256 = 128;
        public static final int WPA_PSK = 2;
        public static final int WPA_PSK_SHA256 = 256;

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
            if (o == 128) {
                return "WPA_EAP_SHA256";
            }
            if (o == 256) {
                return "WPA_PSK_SHA256";
            }
            if (o == 1024) {
                return "SAE";
            }
            if (o == 131072) {
                return "SUITE_B_192";
            }
            if (o == 4194304) {
                return "OWE";
            }
            if (o == 8388608) {
                return "DPP";
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
            if ((o & 128) == 128) {
                list.add("WPA_EAP_SHA256");
                flipped |= 128;
            }
            if ((o & 256) == 256) {
                list.add("WPA_PSK_SHA256");
                flipped |= 256;
            }
            if ((o & 1024) == 1024) {
                list.add("SAE");
                flipped |= 1024;
            }
            if ((o & 131072) == 131072) {
                list.add("SUITE_B_192");
                flipped |= 131072;
            }
            if ((o & 4194304) == 4194304) {
                list.add("OWE");
                flipped |= 4194304;
            }
            if ((o & 8388608) == 8388608) {
                list.add("DPP");
                flipped |= 8388608;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class PairwiseCipherMask {
        public static final int CCMP = 16;
        public static final int GCMP_256 = 256;
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
            if (o == 256) {
                return "GCMP_256";
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
            if ((o & 256) == 256) {
                list.add("GCMP_256");
                flipped |= 256;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class GroupCipherMask {
        public static final int CCMP = 16;
        public static final int GCMP_256 = 256;
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
            if (o == 256) {
                return "GCMP_256";
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
            if ((o & 256) == 256) {
                list.add("GCMP_256");
                flipped |= 256;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class GroupMgmtCipherMask {
        public static final int BIP_CMAC_256 = 8192;
        public static final int BIP_GMAC_128 = 2048;
        public static final int BIP_GMAC_256 = 4096;

        public static final String toString(int o) {
            if (o == 2048) {
                return "BIP_GMAC_128";
            }
            if (o == 4096) {
                return "BIP_GMAC_256";
            }
            if (o == 8192) {
                return "BIP_CMAC_256";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 2048) == 2048) {
                list.add("BIP_GMAC_128");
                flipped = 0 | 2048;
            }
            if ((o & 4096) == 4096) {
                list.add("BIP_GMAC_256");
                flipped |= 4096;
            }
            if ((o & 8192) == 8192) {
                list.add("BIP_CMAC_256");
                flipped |= 8192;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class Proxy implements ISupplicantStaNetwork {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of android.hardware.wifi.supplicant@1.2::ISupplicantStaNetwork]@Proxy";
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getSsid(ISupplicantStaNetwork.getSsidCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getBssid(ISupplicantStaNetwork.getBssidCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getScanSsid(ISupplicantStaNetwork.getScanSsidCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getKeyMgmt(ISupplicantStaNetwork.getKeyMgmtCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getProto(ISupplicantStaNetwork.getProtoCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getAuthAlg(ISupplicantStaNetwork.getAuthAlgCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getGroupCipher(ISupplicantStaNetwork.getGroupCipherCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getPairwiseCipher(ISupplicantStaNetwork.getPairwiseCipherCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getPskPassphrase(ISupplicantStaNetwork.getPskPassphraseCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getPsk(ISupplicantStaNetwork.getPskCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getWepKey(int keyIdx, ISupplicantStaNetwork.getWepKeyCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getWepTxKeyIdx(ISupplicantStaNetwork.getWepTxKeyIdxCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getRequirePmf(ISupplicantStaNetwork.getRequirePmfCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getEapMethod(ISupplicantStaNetwork.getEapMethodCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getEapPhase2Method(ISupplicantStaNetwork.getEapPhase2MethodCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getEapIdentity(ISupplicantStaNetwork.getEapIdentityCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getEapAnonymousIdentity(ISupplicantStaNetwork.getEapAnonymousIdentityCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getEapPassword(ISupplicantStaNetwork.getEapPasswordCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getEapCACert(ISupplicantStaNetwork.getEapCACertCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getEapCAPath(ISupplicantStaNetwork.getEapCAPathCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getEapClientCert(ISupplicantStaNetwork.getEapClientCertCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getEapPrivateKeyId(ISupplicantStaNetwork.getEapPrivateKeyIdCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getEapSubjectMatch(ISupplicantStaNetwork.getEapSubjectMatchCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getEapAltSubjectMatch(ISupplicantStaNetwork.getEapAltSubjectMatchCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getEapEngine(ISupplicantStaNetwork.getEapEngineCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getEapEngineID(ISupplicantStaNetwork.getEapEngineIDCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getEapDomainSuffixMatch(ISupplicantStaNetwork.getEapDomainSuffixMatchCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getIdStr(ISupplicantStaNetwork.getIdStrCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public void getWpsNfcConfigurationToken(ISupplicantStaNetwork.getWpsNfcConfigurationTokenCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public SupplicantStatus sendNetworkEapSimGsmAuthResponse(ArrayList<ISupplicantStaNetwork.NetworkResponseEapSimGsmAuthParams> params) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
            ISupplicantStaNetwork.NetworkResponseEapSimGsmAuthParams.writeVectorToParcel(_hidl_request, params);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
        public SupplicantStatus sendNetworkEapSimUmtsAuthResponse(ISupplicantStaNetwork.NetworkResponseEapSimUmtsAuthParams params) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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

        @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork
        public SupplicantStatus setEapEncryptedImsiIdentity(ArrayList<Byte> identity) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt8Vector(identity);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(73, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork
        public SupplicantStatus sendNetworkEapIdentityResponse_1_1(ArrayList<Byte> identity, ArrayList<Byte> encryptedIdentity) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt8Vector(identity);
            _hidl_request.writeInt8Vector(encryptedIdentity);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(74, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork
        public SupplicantStatus setKeyMgmt_1_2(int keyMgmtMask) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(keyMgmtMask);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(75, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork
        public void getKeyMgmt_1_2(getKeyMgmt_1_2Callback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(76, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork
        public SupplicantStatus setPairwiseCipher_1_2(int pairwiseCipherMask) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(pairwiseCipherMask);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(77, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork
        public void getPairwiseCipher_1_2(getPairwiseCipher_1_2Callback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(78, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork
        public SupplicantStatus setGroupCipher_1_2(int groupCipherMask) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(groupCipherMask);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(79, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork
        public void getGroupCipher_1_2(getGroupCipher_1_2Callback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(80, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork
        public SupplicantStatus setGroupMgmtCipher(int groupMgmtCipherMask) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(groupMgmtCipherMask);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(81, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork
        public void getGroupMgmtCipher(getGroupMgmtCipherCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(82, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork
        public SupplicantStatus enableTlsSuiteBEapPhase1Param(boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(83, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork
        public SupplicantStatus enableSuiteBEapOpenSslCiphers() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(84, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork
        public void getSaePassword(getSaePasswordCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(85, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork
        public void getSaePasswordId(getSaePasswordIdCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(86, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork
        public SupplicantStatus setSaePassword(String saePassword) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(saePassword);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(87, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork
        public SupplicantStatus setSaePasswordId(String saePasswordId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(saePasswordId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(88, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements ISupplicantStaNetwork {
        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(ISupplicantStaNetwork.kInterfaceName, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork.kInterfaceName, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName, ISupplicantNetwork.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return ISupplicantStaNetwork.kInterfaceName;
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{-17, -69, 6, 28, -106, -97, -87, 85, 61, 36, 61, -90, -18, 35, -72, 63, -27, -44, -86, 102, 58, 123, -120, -106, -83, -59, 46, 43, 1, 91, -62, -13}, new byte[]{16, -1, 47, -82, 81, 99, 70, -72, 97, 33, 54, -116, -27, 121, 13, 90, -52, -33, -53, 115, -104, 50, 70, -72, 19, -13, -44, -120, -74, 109, -76, 90}, new byte[]{-79, 46, -16, -67, -40, -92, -46, 71, -88, -90, -23, 96, -78, 39, -19, 50, 56, 63, 43, 2, 65, -11, 93, 103, -4, -22, 110, -1, 106, 103, 55, -6}, new byte[]{-51, -96, 16, 8, -64, 105, 34, -6, 55, -63, 33, 62, -101, -72, 49, -95, 9, -77, 23, 69, 50, Byte.MIN_VALUE, 86, 22, -5, 113, 97, -19, -60, 3, -122, 111}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork, android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
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
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass1 */

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
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass2 */

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
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass3 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getSsid(new ISupplicantStaNetwork.getSsidCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass4 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getBssid(new ISupplicantStaNetwork.getBssidCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass5 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getScanSsid(new ISupplicantStaNetwork.getScanSsidCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass6 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getKeyMgmt(new ISupplicantStaNetwork.getKeyMgmtCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass7 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getProto(new ISupplicantStaNetwork.getProtoCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass8 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getAuthAlg(new ISupplicantStaNetwork.getAuthAlgCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass9 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getGroupCipher(new ISupplicantStaNetwork.getGroupCipherCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass10 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getPairwiseCipher(new ISupplicantStaNetwork.getPairwiseCipherCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass11 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getPskPassphrase(new ISupplicantStaNetwork.getPskPassphraseCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass12 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getPsk(new ISupplicantStaNetwork.getPskCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass13 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getWepKey(_hidl_request.readInt32(), new ISupplicantStaNetwork.getWepKeyCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass14 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getWepTxKeyIdx(new ISupplicantStaNetwork.getWepTxKeyIdxCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass15 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getRequirePmf(new ISupplicantStaNetwork.getRequirePmfCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass16 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getEapMethod(new ISupplicantStaNetwork.getEapMethodCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass17 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getEapPhase2Method(new ISupplicantStaNetwork.getEapPhase2MethodCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass18 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getEapIdentity(new ISupplicantStaNetwork.getEapIdentityCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass19 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getEapAnonymousIdentity(new ISupplicantStaNetwork.getEapAnonymousIdentityCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass20 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getEapPassword(new ISupplicantStaNetwork.getEapPasswordCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass21 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getEapCACert(new ISupplicantStaNetwork.getEapCACertCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass22 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getEapCAPath(new ISupplicantStaNetwork.getEapCAPathCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass23 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getEapClientCert(new ISupplicantStaNetwork.getEapClientCertCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass24 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getEapPrivateKeyId(new ISupplicantStaNetwork.getEapPrivateKeyIdCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass25 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getEapSubjectMatch(new ISupplicantStaNetwork.getEapSubjectMatchCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass26 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getEapAltSubjectMatch(new ISupplicantStaNetwork.getEapAltSubjectMatchCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass27 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getEapEngine(new ISupplicantStaNetwork.getEapEngineCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass28 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getEapEngineID(new ISupplicantStaNetwork.getEapEngineIDCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass29 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getEapDomainSuffixMatch(new ISupplicantStaNetwork.getEapDomainSuffixMatchCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass30 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getIdStr(new ISupplicantStaNetwork.getIdStrCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass31 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    getWpsNfcConfigurationToken(new ISupplicantStaNetwork.getWpsNfcConfigurationTokenCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass32 */

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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status35 = sendNetworkEapSimGsmAuthResponse(ISupplicantStaNetwork.NetworkResponseEapSimGsmAuthParams.readVectorFromParcel(_hidl_request));
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    ISupplicantStaNetwork.NetworkResponseEapSimUmtsAuthParams params = new ISupplicantStaNetwork.NetworkResponseEapSimUmtsAuthParams();
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status40 = sendNetworkEapIdentityResponse(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status40.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.U_APSD_COEX_NOT_SUPPORTED /* 73 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status41 = setEapEncryptedImsiIdentity(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status41.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.U_APSD_COEX_MODE_NOT_SUPPORTED /* 74 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_1.ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status42 = sendNetworkEapIdentityResponse_1_1(_hidl_request.readInt8Vector(), _hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status42.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.BAD_INTERVAL_WITH_U_APSD_COEX /* 75 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status43 = setKeyMgmt_1_2(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status43.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.ANTI_CLOGGING_TOKEN_REQ /* 76 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getKeyMgmt_1_2(new getKeyMgmt_1_2Callback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass33 */

                        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.getKeyMgmt_1_2Callback
                        public void onValues(SupplicantStatus status, int keyMgmtMask) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(keyMgmtMask);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.FINITE_CYCLIC_GROUP_NOT_SUPPORTED /* 77 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status44 = setPairwiseCipher_1_2(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status44.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.CANNOT_FIND_ALT_TBTT /* 78 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getPairwiseCipher_1_2(new getPairwiseCipher_1_2Callback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass34 */

                        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.getPairwiseCipher_1_2Callback
                        public void onValues(SupplicantStatus status, int pairwiseCipherMask) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(pairwiseCipherMask);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.TRANSMISSION_FAILURE /* 79 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status45 = setGroupCipher_1_2(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status45.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 80:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getGroupCipher_1_2(new getGroupCipher_1_2Callback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass35 */

                        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.getGroupCipher_1_2Callback
                        public void onValues(SupplicantStatus status, int groupCipherMask) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(groupCipherMask);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.TCLAS_RESOURCES_EXCHAUSTED /* 81 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status46 = setGroupMgmtCipher(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status46.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.REJECTED_WITH_SUGGESTED_BSS_TRANSITION /* 82 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getGroupMgmtCipher(new getGroupMgmtCipherCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass36 */

                        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.getGroupMgmtCipherCallback
                        public void onValues(SupplicantStatus status, int groupMgmtCipherMask) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(groupMgmtCipherMask);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.REJECT_WITH_SCHEDULE /* 83 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status47 = enableTlsSuiteBEapPhase1Param(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status47.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.REJECT_NO_WAKEUP_SPECIFIED /* 84 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status48 = enableSuiteBEapOpenSslCiphers();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status48.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 85:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getSaePassword(new getSaePasswordCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass37 */

                        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.getSaePasswordCallback
                        public void onValues(SupplicantStatus status, String saePassword) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(saePassword);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.PENDING_ADMITTING_FST_SESSION /* 86 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    getSaePasswordId(new getSaePasswordIdCallback() {
                        /* class android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.Stub.AnonymousClass38 */

                        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaNetwork.getSaePasswordIdCallback
                        public void onValues(SupplicantStatus status, String saePasswordId) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(saePasswordId);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.PERFORMING_FST_NOW /* 87 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status49 = setSaePassword(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status49.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.StatusCode.PENDING_GAP_IN_BA_WINDOW /* 88 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status50 = setSaePasswordId(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status50.writeToParcel(_hidl_reply);
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
