package android.hardware.wifi.supplicant.V1_0;

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

public interface ISupplicantStaIfaceCallback extends IBase {
    public static final String kInterfaceName = "android.hardware.wifi.supplicant@1.0::ISupplicantStaIfaceCallback";

    @Override // android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    void onAnqpQueryDone(byte[] bArr, AnqpData anqpData, Hs20AnqpData hs20AnqpData) throws RemoteException;

    void onAssociationRejected(byte[] bArr, int i, boolean z) throws RemoteException;

    void onAuthenticationTimeout(byte[] bArr) throws RemoteException;

    void onBssidChanged(byte b, byte[] bArr) throws RemoteException;

    void onDisconnected(byte[] bArr, boolean z, int i) throws RemoteException;

    void onEapFailure() throws RemoteException;

    void onExtRadioWorkStart(int i) throws RemoteException;

    void onExtRadioWorkTimeout(int i) throws RemoteException;

    void onHs20DeauthImminentNotice(byte[] bArr, int i, int i2, String str) throws RemoteException;

    void onHs20IconQueryDone(byte[] bArr, String str, ArrayList<Byte> arrayList) throws RemoteException;

    void onHs20SubscriptionRemediation(byte[] bArr, byte b, String str) throws RemoteException;

    void onNetworkAdded(int i) throws RemoteException;

    void onNetworkRemoved(int i) throws RemoteException;

    void onStateChanged(int i, byte[] bArr, int i2, ArrayList<Byte> arrayList) throws RemoteException;

    void onWpsEventFail(byte[] bArr, short s, short s2) throws RemoteException;

    void onWpsEventPbcOverlap() throws RemoteException;

    void onWpsEventSuccess() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static ISupplicantStaIfaceCallback asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof ISupplicantStaIfaceCallback)) {
            return (ISupplicantStaIfaceCallback) iface;
        }
        ISupplicantStaIfaceCallback proxy = new Proxy(binder);
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

    static ISupplicantStaIfaceCallback castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static ISupplicantStaIfaceCallback getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static ISupplicantStaIfaceCallback getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    static ISupplicantStaIfaceCallback getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static ISupplicantStaIfaceCallback getService() throws RemoteException {
        return getService("default");
    }

    public static final class State {
        public static final int ASSOCIATED = 6;
        public static final int ASSOCIATING = 5;
        public static final int AUTHENTICATING = 4;
        public static final int COMPLETED = 9;
        public static final int DISCONNECTED = 0;
        public static final int FOURWAY_HANDSHAKE = 7;
        public static final int GROUP_HANDSHAKE = 8;
        public static final int IFACE_DISABLED = 1;
        public static final int INACTIVE = 2;
        public static final int SCANNING = 3;

        public static final String toString(int o) {
            if (o == 0) {
                return WifiCommonUtils.STATE_DISCONNECTED;
            }
            if (o == 1) {
                return "IFACE_DISABLED";
            }
            if (o == 2) {
                return "INACTIVE";
            }
            if (o == 3) {
                return "SCANNING";
            }
            if (o == 4) {
                return "AUTHENTICATING";
            }
            if (o == 5) {
                return "ASSOCIATING";
            }
            if (o == 6) {
                return "ASSOCIATED";
            }
            if (o == 7) {
                return "FOURWAY_HANDSHAKE";
            }
            if (o == 8) {
                return "GROUP_HANDSHAKE";
            }
            if (o == 9) {
                return "COMPLETED";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            list.add(WifiCommonUtils.STATE_DISCONNECTED);
            if ((o & 1) == 1) {
                list.add("IFACE_DISABLED");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("INACTIVE");
                flipped |= 2;
            }
            if ((o & 3) == 3) {
                list.add("SCANNING");
                flipped |= 3;
            }
            if ((o & 4) == 4) {
                list.add("AUTHENTICATING");
                flipped |= 4;
            }
            if ((o & 5) == 5) {
                list.add("ASSOCIATING");
                flipped |= 5;
            }
            if ((o & 6) == 6) {
                list.add("ASSOCIATED");
                flipped |= 6;
            }
            if ((o & 7) == 7) {
                list.add("FOURWAY_HANDSHAKE");
                flipped |= 7;
            }
            if ((o & 8) == 8) {
                list.add("GROUP_HANDSHAKE");
                flipped |= 8;
            }
            if ((o & 9) == 9) {
                list.add("COMPLETED");
                flipped |= 9;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class OsuMethod {
        public static final byte OMA_DM = 0;
        public static final byte SOAP_XML_SPP = 1;

        public static final String toString(byte o) {
            if (o == 0) {
                return "OMA_DM";
            }
            if (o == 1) {
                return "SOAP_XML_SPP";
            }
            return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
        }

        public static final String dumpBitfield(byte o) {
            ArrayList<String> list = new ArrayList<>();
            byte flipped = 0;
            list.add("OMA_DM");
            if ((o & 1) == 1) {
                list.add("SOAP_XML_SPP");
                flipped = (byte) (0 | 1);
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
            }
            return String.join(" | ", list);
        }
    }

    public static final class AnqpData {
        public ArrayList<Byte> anqp3gppCellularNetwork = new ArrayList<>();
        public ArrayList<Byte> domainName = new ArrayList<>();
        public ArrayList<Byte> ipAddrTypeAvailability = new ArrayList<>();
        public ArrayList<Byte> naiRealm = new ArrayList<>();
        public ArrayList<Byte> roamingConsortium = new ArrayList<>();
        public ArrayList<Byte> venueName = new ArrayList<>();

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != AnqpData.class) {
                return false;
            }
            AnqpData other = (AnqpData) otherObject;
            if (HidlSupport.deepEquals(this.venueName, other.venueName) && HidlSupport.deepEquals(this.roamingConsortium, other.roamingConsortium) && HidlSupport.deepEquals(this.ipAddrTypeAvailability, other.ipAddrTypeAvailability) && HidlSupport.deepEquals(this.naiRealm, other.naiRealm) && HidlSupport.deepEquals(this.anqp3gppCellularNetwork, other.anqp3gppCellularNetwork) && HidlSupport.deepEquals(this.domainName, other.domainName)) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.venueName)), Integer.valueOf(HidlSupport.deepHashCode(this.roamingConsortium)), Integer.valueOf(HidlSupport.deepHashCode(this.ipAddrTypeAvailability)), Integer.valueOf(HidlSupport.deepHashCode(this.naiRealm)), Integer.valueOf(HidlSupport.deepHashCode(this.anqp3gppCellularNetwork)), Integer.valueOf(HidlSupport.deepHashCode(this.domainName)));
        }

        public final String toString() {
            return "{.venueName = " + this.venueName + ", .roamingConsortium = " + this.roamingConsortium + ", .ipAddrTypeAvailability = " + this.ipAddrTypeAvailability + ", .naiRealm = " + this.naiRealm + ", .anqp3gppCellularNetwork = " + this.anqp3gppCellularNetwork + ", .domainName = " + this.domainName + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(96), 0);
        }

        public static final ArrayList<AnqpData> readVectorFromParcel(HwParcel parcel) {
            ArrayList<AnqpData> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 96), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                AnqpData _hidl_vec_element = new AnqpData();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 96));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 0 + 8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, true);
            this.venueName.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                this.venueName.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
            }
            int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 16 + 8);
            HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
            this.roamingConsortium.clear();
            for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
                this.roamingConsortium.add(Byte.valueOf(childBlob2.getInt8((long) (_hidl_index_02 * 1))));
            }
            int _hidl_vec_size3 = _hidl_blob.getInt32(_hidl_offset + 32 + 8);
            HwBlob childBlob3 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size3 * 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, true);
            this.ipAddrTypeAvailability.clear();
            for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
                this.ipAddrTypeAvailability.add(Byte.valueOf(childBlob3.getInt8((long) (_hidl_index_03 * 1))));
            }
            int _hidl_vec_size4 = _hidl_blob.getInt32(_hidl_offset + 48 + 8);
            HwBlob childBlob4 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size4 * 1), _hidl_blob.handle(), _hidl_offset + 48 + 0, true);
            this.naiRealm.clear();
            for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size4; _hidl_index_04++) {
                this.naiRealm.add(Byte.valueOf(childBlob4.getInt8((long) (_hidl_index_04 * 1))));
            }
            int _hidl_vec_size5 = _hidl_blob.getInt32(_hidl_offset + 64 + 8);
            HwBlob childBlob5 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size5 * 1), _hidl_blob.handle(), _hidl_offset + 64 + 0, true);
            this.anqp3gppCellularNetwork.clear();
            for (int _hidl_index_05 = 0; _hidl_index_05 < _hidl_vec_size5; _hidl_index_05++) {
                this.anqp3gppCellularNetwork.add(Byte.valueOf(childBlob5.getInt8((long) (_hidl_index_05 * 1))));
            }
            int _hidl_vec_size6 = _hidl_blob.getInt32(_hidl_offset + 80 + 8);
            HwBlob childBlob6 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size6 * 1), _hidl_blob.handle(), _hidl_offset + 80 + 0, true);
            this.domainName.clear();
            for (int _hidl_index_06 = 0; _hidl_index_06 < _hidl_vec_size6; _hidl_index_06++) {
                this.domainName.add(Byte.valueOf(childBlob6.getInt8((long) (_hidl_index_06 * 1))));
            }
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(96);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<AnqpData> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 96);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 96));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            int _hidl_vec_size = this.venueName.size();
            _hidl_blob.putInt32(_hidl_offset + 0 + 8, _hidl_vec_size);
            _hidl_blob.putBool(_hidl_offset + 0 + 12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                childBlob.putInt8((long) (_hidl_index_0 * 1), this.venueName.get(_hidl_index_0).byteValue());
            }
            _hidl_blob.putBlob(_hidl_offset + 0 + 0, childBlob);
            int _hidl_vec_size2 = this.roamingConsortium.size();
            _hidl_blob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size2);
            _hidl_blob.putBool(_hidl_offset + 16 + 12, false);
            HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 1);
            for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
                childBlob2.putInt8((long) (_hidl_index_02 * 1), this.roamingConsortium.get(_hidl_index_02).byteValue());
            }
            _hidl_blob.putBlob(_hidl_offset + 16 + 0, childBlob2);
            int _hidl_vec_size3 = this.ipAddrTypeAvailability.size();
            _hidl_blob.putInt32(_hidl_offset + 32 + 8, _hidl_vec_size3);
            _hidl_blob.putBool(_hidl_offset + 32 + 12, false);
            HwBlob childBlob3 = new HwBlob(_hidl_vec_size3 * 1);
            for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
                childBlob3.putInt8((long) (_hidl_index_03 * 1), this.ipAddrTypeAvailability.get(_hidl_index_03).byteValue());
            }
            _hidl_blob.putBlob(_hidl_offset + 32 + 0, childBlob3);
            int _hidl_vec_size4 = this.naiRealm.size();
            _hidl_blob.putInt32(_hidl_offset + 48 + 8, _hidl_vec_size4);
            _hidl_blob.putBool(_hidl_offset + 48 + 12, false);
            HwBlob childBlob4 = new HwBlob(_hidl_vec_size4 * 1);
            for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size4; _hidl_index_04++) {
                childBlob4.putInt8((long) (_hidl_index_04 * 1), this.naiRealm.get(_hidl_index_04).byteValue());
            }
            _hidl_blob.putBlob(_hidl_offset + 48 + 0, childBlob4);
            int _hidl_vec_size5 = this.anqp3gppCellularNetwork.size();
            _hidl_blob.putInt32(_hidl_offset + 64 + 8, _hidl_vec_size5);
            _hidl_blob.putBool(_hidl_offset + 64 + 12, false);
            HwBlob childBlob5 = new HwBlob(_hidl_vec_size5 * 1);
            for (int _hidl_index_05 = 0; _hidl_index_05 < _hidl_vec_size5; _hidl_index_05++) {
                childBlob5.putInt8((long) (_hidl_index_05 * 1), this.anqp3gppCellularNetwork.get(_hidl_index_05).byteValue());
            }
            _hidl_blob.putBlob(_hidl_offset + 64 + 0, childBlob5);
            int _hidl_vec_size6 = this.domainName.size();
            _hidl_blob.putInt32(_hidl_offset + 80 + 8, _hidl_vec_size6);
            _hidl_blob.putBool(_hidl_offset + 80 + 12, false);
            HwBlob childBlob6 = new HwBlob(_hidl_vec_size6 * 1);
            for (int _hidl_index_06 = 0; _hidl_index_06 < _hidl_vec_size6; _hidl_index_06++) {
                childBlob6.putInt8((long) (_hidl_index_06 * 1), this.domainName.get(_hidl_index_06).byteValue());
            }
            _hidl_blob.putBlob(_hidl_offset + 80 + 0, childBlob6);
        }
    }

    public static final class Hs20AnqpData {
        public ArrayList<Byte> connectionCapability = new ArrayList<>();
        public ArrayList<Byte> operatorFriendlyName = new ArrayList<>();
        public ArrayList<Byte> osuProvidersList = new ArrayList<>();
        public ArrayList<Byte> wanMetrics = new ArrayList<>();

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != Hs20AnqpData.class) {
                return false;
            }
            Hs20AnqpData other = (Hs20AnqpData) otherObject;
            if (HidlSupport.deepEquals(this.operatorFriendlyName, other.operatorFriendlyName) && HidlSupport.deepEquals(this.wanMetrics, other.wanMetrics) && HidlSupport.deepEquals(this.connectionCapability, other.connectionCapability) && HidlSupport.deepEquals(this.osuProvidersList, other.osuProvidersList)) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.operatorFriendlyName)), Integer.valueOf(HidlSupport.deepHashCode(this.wanMetrics)), Integer.valueOf(HidlSupport.deepHashCode(this.connectionCapability)), Integer.valueOf(HidlSupport.deepHashCode(this.osuProvidersList)));
        }

        public final String toString() {
            return "{.operatorFriendlyName = " + this.operatorFriendlyName + ", .wanMetrics = " + this.wanMetrics + ", .connectionCapability = " + this.connectionCapability + ", .osuProvidersList = " + this.osuProvidersList + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(64), 0);
        }

        public static final ArrayList<Hs20AnqpData> readVectorFromParcel(HwParcel parcel) {
            ArrayList<Hs20AnqpData> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 64), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                Hs20AnqpData _hidl_vec_element = new Hs20AnqpData();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 64));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 0 + 8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, true);
            this.operatorFriendlyName.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                this.operatorFriendlyName.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
            }
            int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 16 + 8);
            HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
            this.wanMetrics.clear();
            for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
                this.wanMetrics.add(Byte.valueOf(childBlob2.getInt8((long) (_hidl_index_02 * 1))));
            }
            int _hidl_vec_size3 = _hidl_blob.getInt32(_hidl_offset + 32 + 8);
            HwBlob childBlob3 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size3 * 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, true);
            this.connectionCapability.clear();
            for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
                this.connectionCapability.add(Byte.valueOf(childBlob3.getInt8((long) (_hidl_index_03 * 1))));
            }
            int _hidl_vec_size4 = _hidl_blob.getInt32(_hidl_offset + 48 + 8);
            HwBlob childBlob4 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size4 * 1), _hidl_blob.handle(), _hidl_offset + 48 + 0, true);
            this.osuProvidersList.clear();
            for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size4; _hidl_index_04++) {
                this.osuProvidersList.add(Byte.valueOf(childBlob4.getInt8((long) (_hidl_index_04 * 1))));
            }
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(64);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<Hs20AnqpData> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 64);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 64));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            int _hidl_vec_size = this.operatorFriendlyName.size();
            _hidl_blob.putInt32(_hidl_offset + 0 + 8, _hidl_vec_size);
            _hidl_blob.putBool(_hidl_offset + 0 + 12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                childBlob.putInt8((long) (_hidl_index_0 * 1), this.operatorFriendlyName.get(_hidl_index_0).byteValue());
            }
            _hidl_blob.putBlob(_hidl_offset + 0 + 0, childBlob);
            int _hidl_vec_size2 = this.wanMetrics.size();
            _hidl_blob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size2);
            _hidl_blob.putBool(_hidl_offset + 16 + 12, false);
            HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 1);
            for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
                childBlob2.putInt8((long) (_hidl_index_02 * 1), this.wanMetrics.get(_hidl_index_02).byteValue());
            }
            _hidl_blob.putBlob(_hidl_offset + 16 + 0, childBlob2);
            int _hidl_vec_size3 = this.connectionCapability.size();
            _hidl_blob.putInt32(_hidl_offset + 32 + 8, _hidl_vec_size3);
            _hidl_blob.putBool(_hidl_offset + 32 + 12, false);
            HwBlob childBlob3 = new HwBlob(_hidl_vec_size3 * 1);
            for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
                childBlob3.putInt8((long) (_hidl_index_03 * 1), this.connectionCapability.get(_hidl_index_03).byteValue());
            }
            _hidl_blob.putBlob(_hidl_offset + 32 + 0, childBlob3);
            int _hidl_vec_size4 = this.osuProvidersList.size();
            _hidl_blob.putInt32(_hidl_offset + 48 + 8, _hidl_vec_size4);
            _hidl_blob.putBool(_hidl_offset + 48 + 12, false);
            HwBlob childBlob4 = new HwBlob(_hidl_vec_size4 * 1);
            for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size4; _hidl_index_04++) {
                childBlob4.putInt8((long) (_hidl_index_04 * 1), this.osuProvidersList.get(_hidl_index_04).byteValue());
            }
            _hidl_blob.putBlob(_hidl_offset + 48 + 0, childBlob4);
        }
    }

    public static final class WpsConfigError {
        public static final short CHAN_24_NOT_SUPPORTED = 3;
        public static final short CHAN_50_NOT_SUPPORTED = 4;
        public static final short CHAN_60G_NOT_SUPPORTED = 19;
        public static final short DECRYPTION_CRC_FAILURE = 2;
        public static final short DEVICE_BUSY = 14;
        public static final short DEV_PASSWORD_AUTH_FAILURE = 18;
        public static final short FAILED_DHCP_CONFIG = 9;
        public static final short IP_ADDR_CONFLICT = 10;
        public static final short MSG_TIMEOUT = 16;
        public static final short MULTIPLE_PBC_DETECTED = 12;
        public static final short NETWORK_ASSOC_FAILURE = 7;
        public static final short NETWORK_AUTH_FAILURE = 6;
        public static final short NO_CONN_TO_REGISTRAR = 11;
        public static final short NO_DHCP_RESPONSE = 8;
        public static final short NO_ERROR = 0;
        public static final short OOB_IFACE_READ_ERROR = 1;
        public static final short PUBLIC_KEY_HASH_MISMATCH = 20;
        public static final short REG_SESS_TIMEOUT = 17;
        public static final short ROGUE_SUSPECTED = 13;
        public static final short SETUP_LOCKED = 15;
        public static final short SIGNAL_TOO_WEAK = 5;

        public static final String toString(short o) {
            if (o == 0) {
                return "NO_ERROR";
            }
            if (o == 1) {
                return "OOB_IFACE_READ_ERROR";
            }
            if (o == 2) {
                return "DECRYPTION_CRC_FAILURE";
            }
            if (o == 3) {
                return "CHAN_24_NOT_SUPPORTED";
            }
            if (o == 4) {
                return "CHAN_50_NOT_SUPPORTED";
            }
            if (o == 5) {
                return "SIGNAL_TOO_WEAK";
            }
            if (o == 6) {
                return "NETWORK_AUTH_FAILURE";
            }
            if (o == 7) {
                return "NETWORK_ASSOC_FAILURE";
            }
            if (o == 8) {
                return "NO_DHCP_RESPONSE";
            }
            if (o == 9) {
                return "FAILED_DHCP_CONFIG";
            }
            if (o == 10) {
                return "IP_ADDR_CONFLICT";
            }
            if (o == 11) {
                return "NO_CONN_TO_REGISTRAR";
            }
            if (o == 12) {
                return "MULTIPLE_PBC_DETECTED";
            }
            if (o == 13) {
                return "ROGUE_SUSPECTED";
            }
            if (o == 14) {
                return "DEVICE_BUSY";
            }
            if (o == 15) {
                return "SETUP_LOCKED";
            }
            if (o == 16) {
                return "MSG_TIMEOUT";
            }
            if (o == 17) {
                return "REG_SESS_TIMEOUT";
            }
            if (o == 18) {
                return "DEV_PASSWORD_AUTH_FAILURE";
            }
            if (o == 19) {
                return "CHAN_60G_NOT_SUPPORTED";
            }
            if (o == 20) {
                return "PUBLIC_KEY_HASH_MISMATCH";
            }
            return "0x" + Integer.toHexString(Short.toUnsignedInt(o));
        }

        public static final String dumpBitfield(short o) {
            ArrayList<String> list = new ArrayList<>();
            short flipped = 0;
            list.add("NO_ERROR");
            if ((o & 1) == 1) {
                list.add("OOB_IFACE_READ_ERROR");
                flipped = (short) (0 | 1);
            }
            if ((o & 2) == 2) {
                list.add("DECRYPTION_CRC_FAILURE");
                flipped = (short) (flipped | 2);
            }
            if ((o & 3) == 3) {
                list.add("CHAN_24_NOT_SUPPORTED");
                flipped = (short) (flipped | 3);
            }
            if ((o & 4) == 4) {
                list.add("CHAN_50_NOT_SUPPORTED");
                flipped = (short) (flipped | 4);
            }
            if ((o & 5) == 5) {
                list.add("SIGNAL_TOO_WEAK");
                flipped = (short) (flipped | 5);
            }
            if ((o & 6) == 6) {
                list.add("NETWORK_AUTH_FAILURE");
                flipped = (short) (flipped | 6);
            }
            if ((o & 7) == 7) {
                list.add("NETWORK_ASSOC_FAILURE");
                flipped = (short) (flipped | 7);
            }
            if ((o & 8) == 8) {
                list.add("NO_DHCP_RESPONSE");
                flipped = (short) (flipped | 8);
            }
            if ((o & 9) == 9) {
                list.add("FAILED_DHCP_CONFIG");
                flipped = (short) (flipped | 9);
            }
            if ((o & 10) == 10) {
                list.add("IP_ADDR_CONFLICT");
                flipped = (short) (flipped | 10);
            }
            if ((o & 11) == 11) {
                list.add("NO_CONN_TO_REGISTRAR");
                flipped = (short) (flipped | 11);
            }
            if ((o & 12) == 12) {
                list.add("MULTIPLE_PBC_DETECTED");
                flipped = (short) (flipped | 12);
            }
            if ((o & 13) == 13) {
                list.add("ROGUE_SUSPECTED");
                flipped = (short) (flipped | 13);
            }
            if ((o & 14) == 14) {
                list.add("DEVICE_BUSY");
                flipped = (short) (flipped | 14);
            }
            if ((o & 15) == 15) {
                list.add("SETUP_LOCKED");
                flipped = (short) (flipped | 15);
            }
            if ((o & 16) == 16) {
                list.add("MSG_TIMEOUT");
                flipped = (short) (flipped | 16);
            }
            if ((o & 17) == 17) {
                list.add("REG_SESS_TIMEOUT");
                flipped = (short) (flipped | 17);
            }
            if ((o & 18) == 18) {
                list.add("DEV_PASSWORD_AUTH_FAILURE");
                flipped = (short) (flipped | 18);
            }
            if ((o & 19) == 19) {
                list.add("CHAN_60G_NOT_SUPPORTED");
                flipped = (short) (flipped | 19);
            }
            if ((o & 20) == 20) {
                list.add("PUBLIC_KEY_HASH_MISMATCH");
                flipped = (short) (flipped | 20);
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString(Short.toUnsignedInt((short) ((~flipped) & o))));
            }
            return String.join(" | ", list);
        }
    }

    public static final class WpsErrorIndication {
        public static final short AUTH_FAILURE = 3;
        public static final short NO_ERROR = 0;
        public static final short SECURITY_TKIP_ONLY_PROHIBITED = 1;
        public static final short SECURITY_WEP_PROHIBITED = 2;

        public static final String toString(short o) {
            if (o == 0) {
                return "NO_ERROR";
            }
            if (o == 1) {
                return "SECURITY_TKIP_ONLY_PROHIBITED";
            }
            if (o == 2) {
                return "SECURITY_WEP_PROHIBITED";
            }
            if (o == 3) {
                return "AUTH_FAILURE";
            }
            return "0x" + Integer.toHexString(Short.toUnsignedInt(o));
        }

        public static final String dumpBitfield(short o) {
            ArrayList<String> list = new ArrayList<>();
            short flipped = 0;
            list.add("NO_ERROR");
            if ((o & 1) == 1) {
                list.add("SECURITY_TKIP_ONLY_PROHIBITED");
                flipped = (short) (0 | 1);
            }
            if ((o & 2) == 2) {
                list.add("SECURITY_WEP_PROHIBITED");
                flipped = (short) (flipped | 2);
            }
            if ((o & 3) == 3) {
                list.add("AUTH_FAILURE");
                flipped = (short) (flipped | 3);
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString(Short.toUnsignedInt((short) ((~flipped) & o))));
            }
            return String.join(" | ", list);
        }
    }

    public static final class StatusCode {
        public static final int ADV_SRV_UNREACHABLE = 65;
        public static final int AKMP_NOT_VALID = 43;
        public static final int ANTI_CLOGGING_TOKEN_REQ = 76;
        public static final int AP_UNABLE_TO_HANDLE_NEW_STA = 17;
        public static final int ASSOC_DENIED_LISTEN_INT_TOO_LARGE = 51;
        public static final int ASSOC_DENIED_NOSHORT = 19;
        public static final int ASSOC_DENIED_NO_HT = 27;
        public static final int ASSOC_DENIED_NO_PCO = 29;
        public static final int ASSOC_DENIED_NO_SHORT_SLOT_TIME = 25;
        public static final int ASSOC_DENIED_NO_VHT = 104;
        public static final int ASSOC_DENIED_RATES = 18;
        public static final int ASSOC_DENIED_UNSPEC = 12;
        public static final int ASSOC_REJECTED_TEMPORARILY = 30;
        public static final int AUTHORIZATION_DEENABLED = 107;
        public static final int AUTH_TIMEOUT = 16;
        public static final int BAD_INTERVAL_WITH_U_APSD_COEX = 75;
        public static final int CANNOT_FIND_ALT_TBTT = 78;
        public static final int CAPS_UNSUPPORTED = 10;
        public static final int CHALLENGE_FAIL = 15;
        public static final int CIPHER_REJECTED_PER_POLICY = 46;
        public static final int DENIED_DUE_TO_SPECTRUM_MANAGEMENT = 103;
        public static final int DENIED_INSUFFICIENT_BANDWIDTH = 33;
        public static final int DENIED_POOR_CHANNEL_CONDITIONS = 34;
        public static final int DENIED_QOS_NOT_SUPPORTED = 35;
        public static final int DENIED_WITH_SUGGESTED_BAND_AND_CHANNEL = 99;
        public static final int DEST_STA_NOT_PRESENT = 49;
        public static final int DEST_STA_NOT_QOS_STA = 50;
        public static final int DIRECT_LINK_NOT_ALLOWED = 48;
        public static final int ENABLEMENT_DENIED = 105;
        public static final int FILS_AUTHENTICATION_FAILURE = 112;
        public static final int FINITE_CYCLIC_GROUP_NOT_SUPPORTED = 77;
        public static final int GAS_ADV_PROTO_NOT_SUPPORTED = 59;
        public static final int GAS_RESP_LARGER_THAN_LIMIT = 63;
        public static final int GAS_RESP_NOT_RECEIVED = 61;
        public static final int GROUP_CIPHER_NOT_VALID = 41;
        public static final int INSUFFICIENT_TCLAS_PROCESSING_RESOURCES = 57;
        public static final int INVALID_FTIE = 55;
        public static final int INVALID_FT_ACTION_FRAME_COUNT = 52;
        public static final int INVALID_IE = 40;
        public static final int INVALID_MDIE = 54;
        public static final int INVALID_PARAMETERS = 38;
        public static final int INVALID_PMKID = 53;
        public static final int INVALID_RSNIE = 72;
        public static final int INVALID_RSN_IE_CAPAB = 45;
        public static final int MAF_LIMIT_EXCEEDED = 101;
        public static final int MCCAOP_RESERVATION_CONFLICT = 100;
        public static final int MCCA_TRACK_LIMIT_EXCEEDED = 102;
        public static final int NOT_IN_SAME_BSS = 7;
        public static final int NOT_SUPPORTED_AUTH_ALG = 13;
        public static final int NO_OUTSTANDING_GAS_REQ = 60;
        public static final int PAIRWISE_CIPHER_NOT_VALID = 42;
        public static final int PENDING_ADMITTING_FST_SESSION = 86;
        public static final int PENDING_GAP_IN_BA_WINDOW = 88;
        public static final int PERFORMING_FST_NOW = 87;
        public static final int PWR_CAPABILITY_NOT_VALID = 23;
        public static final int QUERY_RESP_OUTSTANDING = 95;
        public static final int R0KH_UNREACHABLE = 28;
        public static final int REASSOC_NO_ASSOC = 11;
        public static final int REFUSED_AP_OUT_OF_MEMORY = 93;
        public static final int REFUSED_EXTERNAL_REASON = 92;
        public static final int REJECTED_EMERGENCY_SERVICE_NOT_SUPPORTED = 94;
        public static final int REJECTED_WITH_SUGGESTED_BSS_TRANSITION = 82;
        public static final int REJECTED_WITH_SUGGESTED_CHANGES = 39;
        public static final int REJECT_DSE_BAND = 96;
        public static final int REJECT_NO_WAKEUP_SPECIFIED = 84;
        public static final int REJECT_U_PID_SETTING = 89;
        public static final int REJECT_WITH_SCHEDULE = 83;
        public static final int REQUESTED_TCLAS_NOT_SUPPORTED = 56;
        public static final int REQUEST_DECLINED = 37;
        public static final int REQ_REFUSED_HOME = 64;
        public static final int REQ_REFUSED_SSPN = 67;
        public static final int REQ_REFUSED_UNAUTH_ACCESS = 68;
        public static final int REQ_TCLAS_NOT_SUPPORTED = 80;
        public static final int RESTRICTION_FROM_AUTHORIZED_GDB = 106;
        public static final int ROBUST_MGMT_FRAME_POLICY_VIOLATION = 31;
        public static final int SECURITY_DISABLED = 5;
        public static final int SPEC_MGMT_REQUIRED = 22;
        public static final int STA_TIMED_OUT_WAITING_FOR_GAS_RESP = 62;
        public static final int SUCCESS = 0;
        public static final int SUCCESS_POWER_SAVE_MODE = 85;
        public static final int SUPPORTED_CHANNEL_NOT_VALID = 24;
        public static final int TCLAS_PROCESSING_TERMINATED = 97;
        public static final int TCLAS_RESOURCES_EXCHAUSTED = 81;
        public static final int TDLS_WAKEUP_ALTERNATE = 2;
        public static final int TDLS_WAKEUP_REJECT = 3;
        public static final int TRANSMISSION_FAILURE = 79;
        public static final int TRY_ANOTHER_BSS = 58;
        public static final int TS_NOT_CREATED = 47;
        public static final int TS_SCHEDULE_CONFLICT = 98;
        public static final int UNACCEPTABLE_LIFETIME = 6;
        public static final int UNKNOWN_AUTHENTICATION_SERVER = 113;
        public static final int UNKNOWN_AUTH_TRANSACTION = 14;
        public static final int UNSPECIFIED_FAILURE = 1;
        public static final int UNSPECIFIED_QOS_FAILURE = 32;
        public static final int UNSUPPORTED_RSN_IE_VERSION = 44;
        public static final int U_APSD_COEX_MODE_NOT_SUPPORTED = 74;
        public static final int U_APSD_COEX_NOT_SUPPORTED = 73;

        public static final String toString(int o) {
            if (o == 0) {
                return "SUCCESS";
            }
            if (o == 1) {
                return "UNSPECIFIED_FAILURE";
            }
            if (o == 2) {
                return "TDLS_WAKEUP_ALTERNATE";
            }
            if (o == 3) {
                return "TDLS_WAKEUP_REJECT";
            }
            if (o == 5) {
                return "SECURITY_DISABLED";
            }
            if (o == 6) {
                return "UNACCEPTABLE_LIFETIME";
            }
            if (o == 7) {
                return "NOT_IN_SAME_BSS";
            }
            if (o == 10) {
                return "CAPS_UNSUPPORTED";
            }
            if (o == 11) {
                return "REASSOC_NO_ASSOC";
            }
            if (o == 12) {
                return "ASSOC_DENIED_UNSPEC";
            }
            if (o == 13) {
                return "NOT_SUPPORTED_AUTH_ALG";
            }
            if (o == 14) {
                return "UNKNOWN_AUTH_TRANSACTION";
            }
            if (o == 15) {
                return "CHALLENGE_FAIL";
            }
            if (o == 16) {
                return "AUTH_TIMEOUT";
            }
            if (o == 17) {
                return "AP_UNABLE_TO_HANDLE_NEW_STA";
            }
            if (o == 18) {
                return "ASSOC_DENIED_RATES";
            }
            if (o == 19) {
                return "ASSOC_DENIED_NOSHORT";
            }
            if (o == 22) {
                return "SPEC_MGMT_REQUIRED";
            }
            if (o == 23) {
                return "PWR_CAPABILITY_NOT_VALID";
            }
            if (o == 24) {
                return "SUPPORTED_CHANNEL_NOT_VALID";
            }
            if (o == 25) {
                return "ASSOC_DENIED_NO_SHORT_SLOT_TIME";
            }
            if (o == 27) {
                return "ASSOC_DENIED_NO_HT";
            }
            if (o == 28) {
                return "R0KH_UNREACHABLE";
            }
            if (o == 29) {
                return "ASSOC_DENIED_NO_PCO";
            }
            if (o == 30) {
                return "ASSOC_REJECTED_TEMPORARILY";
            }
            if (o == 31) {
                return "ROBUST_MGMT_FRAME_POLICY_VIOLATION";
            }
            if (o == 32) {
                return "UNSPECIFIED_QOS_FAILURE";
            }
            if (o == 33) {
                return "DENIED_INSUFFICIENT_BANDWIDTH";
            }
            if (o == 34) {
                return "DENIED_POOR_CHANNEL_CONDITIONS";
            }
            if (o == 35) {
                return "DENIED_QOS_NOT_SUPPORTED";
            }
            if (o == 37) {
                return "REQUEST_DECLINED";
            }
            if (o == 38) {
                return "INVALID_PARAMETERS";
            }
            if (o == 39) {
                return "REJECTED_WITH_SUGGESTED_CHANGES";
            }
            if (o == 40) {
                return "INVALID_IE";
            }
            if (o == 41) {
                return "GROUP_CIPHER_NOT_VALID";
            }
            if (o == 42) {
                return "PAIRWISE_CIPHER_NOT_VALID";
            }
            if (o == 43) {
                return "AKMP_NOT_VALID";
            }
            if (o == 44) {
                return "UNSUPPORTED_RSN_IE_VERSION";
            }
            if (o == 45) {
                return "INVALID_RSN_IE_CAPAB";
            }
            if (o == 46) {
                return "CIPHER_REJECTED_PER_POLICY";
            }
            if (o == 47) {
                return "TS_NOT_CREATED";
            }
            if (o == 48) {
                return "DIRECT_LINK_NOT_ALLOWED";
            }
            if (o == 49) {
                return "DEST_STA_NOT_PRESENT";
            }
            if (o == 50) {
                return "DEST_STA_NOT_QOS_STA";
            }
            if (o == 51) {
                return "ASSOC_DENIED_LISTEN_INT_TOO_LARGE";
            }
            if (o == 52) {
                return "INVALID_FT_ACTION_FRAME_COUNT";
            }
            if (o == 53) {
                return "INVALID_PMKID";
            }
            if (o == 54) {
                return "INVALID_MDIE";
            }
            if (o == 55) {
                return "INVALID_FTIE";
            }
            if (o == 56) {
                return "REQUESTED_TCLAS_NOT_SUPPORTED";
            }
            if (o == 57) {
                return "INSUFFICIENT_TCLAS_PROCESSING_RESOURCES";
            }
            if (o == 58) {
                return "TRY_ANOTHER_BSS";
            }
            if (o == 59) {
                return "GAS_ADV_PROTO_NOT_SUPPORTED";
            }
            if (o == 60) {
                return "NO_OUTSTANDING_GAS_REQ";
            }
            if (o == 61) {
                return "GAS_RESP_NOT_RECEIVED";
            }
            if (o == 62) {
                return "STA_TIMED_OUT_WAITING_FOR_GAS_RESP";
            }
            if (o == 63) {
                return "GAS_RESP_LARGER_THAN_LIMIT";
            }
            if (o == 64) {
                return "REQ_REFUSED_HOME";
            }
            if (o == 65) {
                return "ADV_SRV_UNREACHABLE";
            }
            if (o == 67) {
                return "REQ_REFUSED_SSPN";
            }
            if (o == 68) {
                return "REQ_REFUSED_UNAUTH_ACCESS";
            }
            if (o == 72) {
                return "INVALID_RSNIE";
            }
            if (o == 73) {
                return "U_APSD_COEX_NOT_SUPPORTED";
            }
            if (o == 74) {
                return "U_APSD_COEX_MODE_NOT_SUPPORTED";
            }
            if (o == 75) {
                return "BAD_INTERVAL_WITH_U_APSD_COEX";
            }
            if (o == 76) {
                return "ANTI_CLOGGING_TOKEN_REQ";
            }
            if (o == 77) {
                return "FINITE_CYCLIC_GROUP_NOT_SUPPORTED";
            }
            if (o == 78) {
                return "CANNOT_FIND_ALT_TBTT";
            }
            if (o == 79) {
                return "TRANSMISSION_FAILURE";
            }
            if (o == 80) {
                return "REQ_TCLAS_NOT_SUPPORTED";
            }
            if (o == 81) {
                return "TCLAS_RESOURCES_EXCHAUSTED";
            }
            if (o == 82) {
                return "REJECTED_WITH_SUGGESTED_BSS_TRANSITION";
            }
            if (o == 83) {
                return "REJECT_WITH_SCHEDULE";
            }
            if (o == 84) {
                return "REJECT_NO_WAKEUP_SPECIFIED";
            }
            if (o == 85) {
                return "SUCCESS_POWER_SAVE_MODE";
            }
            if (o == 86) {
                return "PENDING_ADMITTING_FST_SESSION";
            }
            if (o == 87) {
                return "PERFORMING_FST_NOW";
            }
            if (o == 88) {
                return "PENDING_GAP_IN_BA_WINDOW";
            }
            if (o == 89) {
                return "REJECT_U_PID_SETTING";
            }
            if (o == 92) {
                return "REFUSED_EXTERNAL_REASON";
            }
            if (o == 93) {
                return "REFUSED_AP_OUT_OF_MEMORY";
            }
            if (o == 94) {
                return "REJECTED_EMERGENCY_SERVICE_NOT_SUPPORTED";
            }
            if (o == 95) {
                return "QUERY_RESP_OUTSTANDING";
            }
            if (o == 96) {
                return "REJECT_DSE_BAND";
            }
            if (o == 97) {
                return "TCLAS_PROCESSING_TERMINATED";
            }
            if (o == 98) {
                return "TS_SCHEDULE_CONFLICT";
            }
            if (o == 99) {
                return "DENIED_WITH_SUGGESTED_BAND_AND_CHANNEL";
            }
            if (o == 100) {
                return "MCCAOP_RESERVATION_CONFLICT";
            }
            if (o == 101) {
                return "MAF_LIMIT_EXCEEDED";
            }
            if (o == 102) {
                return "MCCA_TRACK_LIMIT_EXCEEDED";
            }
            if (o == 103) {
                return "DENIED_DUE_TO_SPECTRUM_MANAGEMENT";
            }
            if (o == 104) {
                return "ASSOC_DENIED_NO_VHT";
            }
            if (o == 105) {
                return "ENABLEMENT_DENIED";
            }
            if (o == 106) {
                return "RESTRICTION_FROM_AUTHORIZED_GDB";
            }
            if (o == 107) {
                return "AUTHORIZATION_DEENABLED";
            }
            if (o == 112) {
                return "FILS_AUTHENTICATION_FAILURE";
            }
            if (o == 113) {
                return "UNKNOWN_AUTHENTICATION_SERVER";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            list.add("SUCCESS");
            if ((o & 1) == 1) {
                list.add("UNSPECIFIED_FAILURE");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("TDLS_WAKEUP_ALTERNATE");
                flipped |= 2;
            }
            if ((o & 3) == 3) {
                list.add("TDLS_WAKEUP_REJECT");
                flipped |= 3;
            }
            if ((o & 5) == 5) {
                list.add("SECURITY_DISABLED");
                flipped |= 5;
            }
            if ((o & 6) == 6) {
                list.add("UNACCEPTABLE_LIFETIME");
                flipped |= 6;
            }
            if ((o & 7) == 7) {
                list.add("NOT_IN_SAME_BSS");
                flipped |= 7;
            }
            if ((o & 10) == 10) {
                list.add("CAPS_UNSUPPORTED");
                flipped |= 10;
            }
            if ((o & 11) == 11) {
                list.add("REASSOC_NO_ASSOC");
                flipped |= 11;
            }
            if ((o & 12) == 12) {
                list.add("ASSOC_DENIED_UNSPEC");
                flipped |= 12;
            }
            if ((o & 13) == 13) {
                list.add("NOT_SUPPORTED_AUTH_ALG");
                flipped |= 13;
            }
            if ((o & 14) == 14) {
                list.add("UNKNOWN_AUTH_TRANSACTION");
                flipped |= 14;
            }
            if ((o & 15) == 15) {
                list.add("CHALLENGE_FAIL");
                flipped |= 15;
            }
            if ((o & 16) == 16) {
                list.add("AUTH_TIMEOUT");
                flipped |= 16;
            }
            if ((o & 17) == 17) {
                list.add("AP_UNABLE_TO_HANDLE_NEW_STA");
                flipped |= 17;
            }
            if ((o & 18) == 18) {
                list.add("ASSOC_DENIED_RATES");
                flipped |= 18;
            }
            if ((o & 19) == 19) {
                list.add("ASSOC_DENIED_NOSHORT");
                flipped |= 19;
            }
            if ((o & 22) == 22) {
                list.add("SPEC_MGMT_REQUIRED");
                flipped |= 22;
            }
            if ((o & 23) == 23) {
                list.add("PWR_CAPABILITY_NOT_VALID");
                flipped |= 23;
            }
            if ((o & 24) == 24) {
                list.add("SUPPORTED_CHANNEL_NOT_VALID");
                flipped |= 24;
            }
            if ((o & 25) == 25) {
                list.add("ASSOC_DENIED_NO_SHORT_SLOT_TIME");
                flipped |= 25;
            }
            if ((o & 27) == 27) {
                list.add("ASSOC_DENIED_NO_HT");
                flipped |= 27;
            }
            if ((o & 28) == 28) {
                list.add("R0KH_UNREACHABLE");
                flipped |= 28;
            }
            if ((o & 29) == 29) {
                list.add("ASSOC_DENIED_NO_PCO");
                flipped |= 29;
            }
            if ((o & 30) == 30) {
                list.add("ASSOC_REJECTED_TEMPORARILY");
                flipped |= 30;
            }
            if ((o & 31) == 31) {
                list.add("ROBUST_MGMT_FRAME_POLICY_VIOLATION");
                flipped |= 31;
            }
            if ((o & 32) == 32) {
                list.add("UNSPECIFIED_QOS_FAILURE");
                flipped |= 32;
            }
            if ((o & 33) == 33) {
                list.add("DENIED_INSUFFICIENT_BANDWIDTH");
                flipped |= 33;
            }
            if ((o & 34) == 34) {
                list.add("DENIED_POOR_CHANNEL_CONDITIONS");
                flipped |= 34;
            }
            if ((o & 35) == 35) {
                list.add("DENIED_QOS_NOT_SUPPORTED");
                flipped |= 35;
            }
            if ((o & 37) == 37) {
                list.add("REQUEST_DECLINED");
                flipped |= 37;
            }
            if ((o & 38) == 38) {
                list.add("INVALID_PARAMETERS");
                flipped |= 38;
            }
            if ((o & 39) == 39) {
                list.add("REJECTED_WITH_SUGGESTED_CHANGES");
                flipped |= 39;
            }
            if ((o & 40) == 40) {
                list.add("INVALID_IE");
                flipped |= 40;
            }
            if ((o & 41) == 41) {
                list.add("GROUP_CIPHER_NOT_VALID");
                flipped |= 41;
            }
            if ((o & 42) == 42) {
                list.add("PAIRWISE_CIPHER_NOT_VALID");
                flipped |= 42;
            }
            if ((o & 43) == 43) {
                list.add("AKMP_NOT_VALID");
                flipped |= 43;
            }
            if ((o & 44) == 44) {
                list.add("UNSUPPORTED_RSN_IE_VERSION");
                flipped |= 44;
            }
            if ((o & 45) == 45) {
                list.add("INVALID_RSN_IE_CAPAB");
                flipped |= 45;
            }
            if ((o & 46) == 46) {
                list.add("CIPHER_REJECTED_PER_POLICY");
                flipped |= 46;
            }
            if ((o & 47) == 47) {
                list.add("TS_NOT_CREATED");
                flipped |= 47;
            }
            if ((o & 48) == 48) {
                list.add("DIRECT_LINK_NOT_ALLOWED");
                flipped |= 48;
            }
            if ((o & 49) == 49) {
                list.add("DEST_STA_NOT_PRESENT");
                flipped |= 49;
            }
            if ((o & 50) == 50) {
                list.add("DEST_STA_NOT_QOS_STA");
                flipped |= 50;
            }
            if ((o & 51) == 51) {
                list.add("ASSOC_DENIED_LISTEN_INT_TOO_LARGE");
                flipped |= 51;
            }
            if ((o & 52) == 52) {
                list.add("INVALID_FT_ACTION_FRAME_COUNT");
                flipped |= 52;
            }
            if ((o & 53) == 53) {
                list.add("INVALID_PMKID");
                flipped |= 53;
            }
            if ((o & 54) == 54) {
                list.add("INVALID_MDIE");
                flipped |= 54;
            }
            if ((o & 55) == 55) {
                list.add("INVALID_FTIE");
                flipped |= 55;
            }
            if ((o & 56) == 56) {
                list.add("REQUESTED_TCLAS_NOT_SUPPORTED");
                flipped |= 56;
            }
            if ((o & 57) == 57) {
                list.add("INSUFFICIENT_TCLAS_PROCESSING_RESOURCES");
                flipped |= 57;
            }
            if ((o & 58) == 58) {
                list.add("TRY_ANOTHER_BSS");
                flipped |= 58;
            }
            if ((o & 59) == 59) {
                list.add("GAS_ADV_PROTO_NOT_SUPPORTED");
                flipped |= 59;
            }
            if ((o & 60) == 60) {
                list.add("NO_OUTSTANDING_GAS_REQ");
                flipped |= 60;
            }
            if ((o & 61) == 61) {
                list.add("GAS_RESP_NOT_RECEIVED");
                flipped |= 61;
            }
            if ((o & 62) == 62) {
                list.add("STA_TIMED_OUT_WAITING_FOR_GAS_RESP");
                flipped |= 62;
            }
            if ((o & 63) == 63) {
                list.add("GAS_RESP_LARGER_THAN_LIMIT");
                flipped |= 63;
            }
            if ((o & 64) == 64) {
                list.add("REQ_REFUSED_HOME");
                flipped |= 64;
            }
            if ((o & 65) == 65) {
                list.add("ADV_SRV_UNREACHABLE");
                flipped |= 65;
            }
            if ((o & 67) == 67) {
                list.add("REQ_REFUSED_SSPN");
                flipped |= 67;
            }
            if ((o & 68) == 68) {
                list.add("REQ_REFUSED_UNAUTH_ACCESS");
                flipped |= 68;
            }
            if ((o & 72) == 72) {
                list.add("INVALID_RSNIE");
                flipped |= 72;
            }
            if ((o & 73) == 73) {
                list.add("U_APSD_COEX_NOT_SUPPORTED");
                flipped |= 73;
            }
            if ((o & 74) == 74) {
                list.add("U_APSD_COEX_MODE_NOT_SUPPORTED");
                flipped |= 74;
            }
            if ((o & 75) == 75) {
                list.add("BAD_INTERVAL_WITH_U_APSD_COEX");
                flipped |= 75;
            }
            if ((o & 76) == 76) {
                list.add("ANTI_CLOGGING_TOKEN_REQ");
                flipped |= 76;
            }
            if ((o & 77) == 77) {
                list.add("FINITE_CYCLIC_GROUP_NOT_SUPPORTED");
                flipped |= 77;
            }
            if ((o & 78) == 78) {
                list.add("CANNOT_FIND_ALT_TBTT");
                flipped |= 78;
            }
            if ((o & 79) == 79) {
                list.add("TRANSMISSION_FAILURE");
                flipped |= 79;
            }
            if ((o & 80) == 80) {
                list.add("REQ_TCLAS_NOT_SUPPORTED");
                flipped |= 80;
            }
            if ((o & 81) == 81) {
                list.add("TCLAS_RESOURCES_EXCHAUSTED");
                flipped |= 81;
            }
            if ((o & 82) == 82) {
                list.add("REJECTED_WITH_SUGGESTED_BSS_TRANSITION");
                flipped |= 82;
            }
            if ((o & 83) == 83) {
                list.add("REJECT_WITH_SCHEDULE");
                flipped |= 83;
            }
            if ((o & 84) == 84) {
                list.add("REJECT_NO_WAKEUP_SPECIFIED");
                flipped |= 84;
            }
            if ((o & 85) == 85) {
                list.add("SUCCESS_POWER_SAVE_MODE");
                flipped |= 85;
            }
            if ((o & 86) == 86) {
                list.add("PENDING_ADMITTING_FST_SESSION");
                flipped |= 86;
            }
            if ((o & 87) == 87) {
                list.add("PERFORMING_FST_NOW");
                flipped |= 87;
            }
            if ((o & 88) == 88) {
                list.add("PENDING_GAP_IN_BA_WINDOW");
                flipped |= 88;
            }
            if ((o & 89) == 89) {
                list.add("REJECT_U_PID_SETTING");
                flipped |= 89;
            }
            if ((o & 92) == 92) {
                list.add("REFUSED_EXTERNAL_REASON");
                flipped |= 92;
            }
            if ((o & 93) == 93) {
                list.add("REFUSED_AP_OUT_OF_MEMORY");
                flipped |= 93;
            }
            if ((o & 94) == 94) {
                list.add("REJECTED_EMERGENCY_SERVICE_NOT_SUPPORTED");
                flipped |= 94;
            }
            if ((o & 95) == 95) {
                list.add("QUERY_RESP_OUTSTANDING");
                flipped |= 95;
            }
            if ((o & 96) == 96) {
                list.add("REJECT_DSE_BAND");
                flipped |= 96;
            }
            if ((o & 97) == 97) {
                list.add("TCLAS_PROCESSING_TERMINATED");
                flipped |= 97;
            }
            if ((o & 98) == 98) {
                list.add("TS_SCHEDULE_CONFLICT");
                flipped |= 98;
            }
            if ((o & 99) == 99) {
                list.add("DENIED_WITH_SUGGESTED_BAND_AND_CHANNEL");
                flipped |= 99;
            }
            if ((o & 100) == 100) {
                list.add("MCCAOP_RESERVATION_CONFLICT");
                flipped |= 100;
            }
            if ((o & 101) == 101) {
                list.add("MAF_LIMIT_EXCEEDED");
                flipped |= 101;
            }
            if ((o & 102) == 102) {
                list.add("MCCA_TRACK_LIMIT_EXCEEDED");
                flipped |= 102;
            }
            if ((o & 103) == 103) {
                list.add("DENIED_DUE_TO_SPECTRUM_MANAGEMENT");
                flipped |= 103;
            }
            if ((o & ASSOC_DENIED_NO_VHT) == 104) {
                list.add("ASSOC_DENIED_NO_VHT");
                flipped |= ASSOC_DENIED_NO_VHT;
            }
            if ((o & ENABLEMENT_DENIED) == 105) {
                list.add("ENABLEMENT_DENIED");
                flipped |= ENABLEMENT_DENIED;
            }
            if ((o & RESTRICTION_FROM_AUTHORIZED_GDB) == 106) {
                list.add("RESTRICTION_FROM_AUTHORIZED_GDB");
                flipped |= RESTRICTION_FROM_AUTHORIZED_GDB;
            }
            if ((o & AUTHORIZATION_DEENABLED) == 107) {
                list.add("AUTHORIZATION_DEENABLED");
                flipped |= AUTHORIZATION_DEENABLED;
            }
            if ((o & FILS_AUTHENTICATION_FAILURE) == 112) {
                list.add("FILS_AUTHENTICATION_FAILURE");
                flipped |= FILS_AUTHENTICATION_FAILURE;
            }
            if ((o & UNKNOWN_AUTHENTICATION_SERVER) == 113) {
                list.add("UNKNOWN_AUTHENTICATION_SERVER");
                flipped |= UNKNOWN_AUTHENTICATION_SERVER;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class ReasonCode {
        public static final int AKMP_NOT_VALID = 20;
        public static final int AUTHORIZED_ACCESS_LIMIT_REACHED = 46;
        public static final int BAD_CIPHER_OR_AKM = 29;
        public static final int BSS_TRANSITION_DISASSOC = 12;
        public static final int CIPHER_SUITE_REJECTED = 24;
        public static final int CLASS2_FRAME_FROM_NONAUTH_STA = 6;
        public static final int CLASS3_FRAME_FROM_NONASSOC_STA = 7;
        public static final int DEAUTH_LEAVING = 3;
        public static final int DISASSOC_AP_BUSY = 5;
        public static final int DISASSOC_DUE_TO_INACTIVITY = 4;
        public static final int DISASSOC_LOW_ACK = 34;
        public static final int DISASSOC_STA_HAS_LEFT = 8;
        public static final int END_TS_BA_DLS = 37;
        public static final int EXCEEDED_TXOP = 35;
        public static final int EXTERNAL_SERVICE_REQUIREMENTS = 47;
        public static final int FOURWAY_HANDSHAKE_TIMEOUT = 15;
        public static final int GROUP_CIPHER_NOT_VALID = 18;
        public static final int GROUP_KEY_UPDATE_TIMEOUT = 16;
        public static final int IEEE_802_1X_AUTH_FAILED = 23;
        public static final int IE_IN_4WAY_DIFFERS = 17;
        public static final int INVALID_FTE = 51;
        public static final int INVALID_FT_ACTION_FRAME_COUNT = 48;
        public static final int INVALID_IE = 13;
        public static final int INVALID_MDE = 50;
        public static final int INVALID_PMKID = 49;
        public static final int INVALID_RSN_IE_CAPAB = 22;
        public static final int MAC_ADDRESS_ALREADY_EXISTS_IN_MBSS = 64;
        public static final int MESH_CHANNEL_SWITCH_REGULATORY_REQ = 65;
        public static final int MESH_CHANNEL_SWITCH_UNSPECIFIED = 66;
        public static final int MESH_CLOSE_RCVD = 55;
        public static final int MESH_CONFIG_POLICY_VIOLATION = 54;
        public static final int MESH_CONFIRM_TIMEOUT = 57;
        public static final int MESH_INCONSISTENT_PARAMS = 59;
        public static final int MESH_INVALID_GTK = 58;
        public static final int MESH_INVALID_SECURITY_CAP = 60;
        public static final int MESH_MAX_PEERS = 53;
        public static final int MESH_MAX_RETRIES = 56;
        public static final int MESH_PATH_ERROR_DEST_UNREACHABLE = 63;
        public static final int MESH_PATH_ERROR_NO_FORWARDING_INFO = 62;
        public static final int MESH_PATH_ERROR_NO_PROXY_INFO = 61;
        public static final int MESH_PEERING_CANCELLED = 52;
        public static final int MICHAEL_MIC_FAILURE = 14;
        public static final int NOT_AUTHORIZED_THIS_LOCATION = 30;
        public static final int NOT_ENOUGH_BANDWIDTH = 33;
        public static final int NO_SSP_ROAMING_AGREEMENT = 28;
        public static final int PAIRWISE_CIPHER_NOT_VALID = 19;
        public static final int PEERKEY_MISMATCH = 45;
        public static final int PREV_AUTH_NOT_VALID = 2;
        public static final int PWR_CAPABILITY_NOT_VALID = 10;
        public static final int SERVICE_CHANGE_PRECLUDES_TS = 31;
        public static final int SSP_REQUESTED_DISASSOC = 27;
        public static final int STA_LEAVING = 36;
        public static final int STA_REQ_ASSOC_WITHOUT_AUTH = 9;
        public static final int SUPPORTED_CHANNEL_NOT_VALID = 11;
        public static final int TDLS_TEARDOWN_UNREACHABLE = 25;
        public static final int TDLS_TEARDOWN_UNSPECIFIED = 26;
        public static final int TIMEOUT = 39;
        public static final int UNKNOWN_TS_BA = 38;
        public static final int UNSPECIFIED = 1;
        public static final int UNSPECIFIED_QOS_REASON = 32;
        public static final int UNSUPPORTED_RSN_IE_VERSION = 21;

        public static final String toString(int o) {
            if (o == 1) {
                return "UNSPECIFIED";
            }
            if (o == 2) {
                return "PREV_AUTH_NOT_VALID";
            }
            if (o == 3) {
                return "DEAUTH_LEAVING";
            }
            if (o == 4) {
                return "DISASSOC_DUE_TO_INACTIVITY";
            }
            if (o == 5) {
                return "DISASSOC_AP_BUSY";
            }
            if (o == 6) {
                return "CLASS2_FRAME_FROM_NONAUTH_STA";
            }
            if (o == 7) {
                return "CLASS3_FRAME_FROM_NONASSOC_STA";
            }
            if (o == 8) {
                return "DISASSOC_STA_HAS_LEFT";
            }
            if (o == 9) {
                return "STA_REQ_ASSOC_WITHOUT_AUTH";
            }
            if (o == 10) {
                return "PWR_CAPABILITY_NOT_VALID";
            }
            if (o == 11) {
                return "SUPPORTED_CHANNEL_NOT_VALID";
            }
            if (o == 12) {
                return "BSS_TRANSITION_DISASSOC";
            }
            if (o == 13) {
                return "INVALID_IE";
            }
            if (o == 14) {
                return "MICHAEL_MIC_FAILURE";
            }
            if (o == 15) {
                return "FOURWAY_HANDSHAKE_TIMEOUT";
            }
            if (o == 16) {
                return "GROUP_KEY_UPDATE_TIMEOUT";
            }
            if (o == 17) {
                return "IE_IN_4WAY_DIFFERS";
            }
            if (o == 18) {
                return "GROUP_CIPHER_NOT_VALID";
            }
            if (o == 19) {
                return "PAIRWISE_CIPHER_NOT_VALID";
            }
            if (o == 20) {
                return "AKMP_NOT_VALID";
            }
            if (o == 21) {
                return "UNSUPPORTED_RSN_IE_VERSION";
            }
            if (o == 22) {
                return "INVALID_RSN_IE_CAPAB";
            }
            if (o == 23) {
                return "IEEE_802_1X_AUTH_FAILED";
            }
            if (o == 24) {
                return "CIPHER_SUITE_REJECTED";
            }
            if (o == 25) {
                return "TDLS_TEARDOWN_UNREACHABLE";
            }
            if (o == 26) {
                return "TDLS_TEARDOWN_UNSPECIFIED";
            }
            if (o == 27) {
                return "SSP_REQUESTED_DISASSOC";
            }
            if (o == 28) {
                return "NO_SSP_ROAMING_AGREEMENT";
            }
            if (o == 29) {
                return "BAD_CIPHER_OR_AKM";
            }
            if (o == 30) {
                return "NOT_AUTHORIZED_THIS_LOCATION";
            }
            if (o == 31) {
                return "SERVICE_CHANGE_PRECLUDES_TS";
            }
            if (o == 32) {
                return "UNSPECIFIED_QOS_REASON";
            }
            if (o == 33) {
                return "NOT_ENOUGH_BANDWIDTH";
            }
            if (o == 34) {
                return "DISASSOC_LOW_ACK";
            }
            if (o == 35) {
                return "EXCEEDED_TXOP";
            }
            if (o == 36) {
                return "STA_LEAVING";
            }
            if (o == 37) {
                return "END_TS_BA_DLS";
            }
            if (o == 38) {
                return "UNKNOWN_TS_BA";
            }
            if (o == 39) {
                return "TIMEOUT";
            }
            if (o == 45) {
                return "PEERKEY_MISMATCH";
            }
            if (o == 46) {
                return "AUTHORIZED_ACCESS_LIMIT_REACHED";
            }
            if (o == 47) {
                return "EXTERNAL_SERVICE_REQUIREMENTS";
            }
            if (o == 48) {
                return "INVALID_FT_ACTION_FRAME_COUNT";
            }
            if (o == 49) {
                return "INVALID_PMKID";
            }
            if (o == 50) {
                return "INVALID_MDE";
            }
            if (o == 51) {
                return "INVALID_FTE";
            }
            if (o == 52) {
                return "MESH_PEERING_CANCELLED";
            }
            if (o == 53) {
                return "MESH_MAX_PEERS";
            }
            if (o == 54) {
                return "MESH_CONFIG_POLICY_VIOLATION";
            }
            if (o == 55) {
                return "MESH_CLOSE_RCVD";
            }
            if (o == 56) {
                return "MESH_MAX_RETRIES";
            }
            if (o == 57) {
                return "MESH_CONFIRM_TIMEOUT";
            }
            if (o == 58) {
                return "MESH_INVALID_GTK";
            }
            if (o == 59) {
                return "MESH_INCONSISTENT_PARAMS";
            }
            if (o == 60) {
                return "MESH_INVALID_SECURITY_CAP";
            }
            if (o == 61) {
                return "MESH_PATH_ERROR_NO_PROXY_INFO";
            }
            if (o == 62) {
                return "MESH_PATH_ERROR_NO_FORWARDING_INFO";
            }
            if (o == 63) {
                return "MESH_PATH_ERROR_DEST_UNREACHABLE";
            }
            if (o == 64) {
                return "MAC_ADDRESS_ALREADY_EXISTS_IN_MBSS";
            }
            if (o == 65) {
                return "MESH_CHANNEL_SWITCH_REGULATORY_REQ";
            }
            if (o == 66) {
                return "MESH_CHANNEL_SWITCH_UNSPECIFIED";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("UNSPECIFIED");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("PREV_AUTH_NOT_VALID");
                flipped |= 2;
            }
            if ((o & 3) == 3) {
                list.add("DEAUTH_LEAVING");
                flipped |= 3;
            }
            if ((o & 4) == 4) {
                list.add("DISASSOC_DUE_TO_INACTIVITY");
                flipped |= 4;
            }
            if ((o & 5) == 5) {
                list.add("DISASSOC_AP_BUSY");
                flipped |= 5;
            }
            if ((o & 6) == 6) {
                list.add("CLASS2_FRAME_FROM_NONAUTH_STA");
                flipped |= 6;
            }
            if ((o & 7) == 7) {
                list.add("CLASS3_FRAME_FROM_NONASSOC_STA");
                flipped |= 7;
            }
            if ((o & 8) == 8) {
                list.add("DISASSOC_STA_HAS_LEFT");
                flipped |= 8;
            }
            if ((o & 9) == 9) {
                list.add("STA_REQ_ASSOC_WITHOUT_AUTH");
                flipped |= 9;
            }
            if ((o & 10) == 10) {
                list.add("PWR_CAPABILITY_NOT_VALID");
                flipped |= 10;
            }
            if ((o & 11) == 11) {
                list.add("SUPPORTED_CHANNEL_NOT_VALID");
                flipped |= 11;
            }
            if ((o & 12) == 12) {
                list.add("BSS_TRANSITION_DISASSOC");
                flipped |= 12;
            }
            if ((o & 13) == 13) {
                list.add("INVALID_IE");
                flipped |= 13;
            }
            if ((o & 14) == 14) {
                list.add("MICHAEL_MIC_FAILURE");
                flipped |= 14;
            }
            if ((o & 15) == 15) {
                list.add("FOURWAY_HANDSHAKE_TIMEOUT");
                flipped |= 15;
            }
            if ((o & 16) == 16) {
                list.add("GROUP_KEY_UPDATE_TIMEOUT");
                flipped |= 16;
            }
            if ((o & 17) == 17) {
                list.add("IE_IN_4WAY_DIFFERS");
                flipped |= 17;
            }
            if ((o & 18) == 18) {
                list.add("GROUP_CIPHER_NOT_VALID");
                flipped |= 18;
            }
            if ((o & 19) == 19) {
                list.add("PAIRWISE_CIPHER_NOT_VALID");
                flipped |= 19;
            }
            if ((o & 20) == 20) {
                list.add("AKMP_NOT_VALID");
                flipped |= 20;
            }
            if ((o & 21) == 21) {
                list.add("UNSUPPORTED_RSN_IE_VERSION");
                flipped |= 21;
            }
            if ((o & 22) == 22) {
                list.add("INVALID_RSN_IE_CAPAB");
                flipped |= 22;
            }
            if ((o & 23) == 23) {
                list.add("IEEE_802_1X_AUTH_FAILED");
                flipped |= 23;
            }
            if ((o & 24) == 24) {
                list.add("CIPHER_SUITE_REJECTED");
                flipped |= 24;
            }
            if ((o & 25) == 25) {
                list.add("TDLS_TEARDOWN_UNREACHABLE");
                flipped |= 25;
            }
            if ((o & 26) == 26) {
                list.add("TDLS_TEARDOWN_UNSPECIFIED");
                flipped |= 26;
            }
            if ((o & 27) == 27) {
                list.add("SSP_REQUESTED_DISASSOC");
                flipped |= 27;
            }
            if ((o & 28) == 28) {
                list.add("NO_SSP_ROAMING_AGREEMENT");
                flipped |= 28;
            }
            if ((o & 29) == 29) {
                list.add("BAD_CIPHER_OR_AKM");
                flipped |= 29;
            }
            if ((o & 30) == 30) {
                list.add("NOT_AUTHORIZED_THIS_LOCATION");
                flipped |= 30;
            }
            if ((o & 31) == 31) {
                list.add("SERVICE_CHANGE_PRECLUDES_TS");
                flipped |= 31;
            }
            if ((o & 32) == 32) {
                list.add("UNSPECIFIED_QOS_REASON");
                flipped |= 32;
            }
            if ((o & 33) == 33) {
                list.add("NOT_ENOUGH_BANDWIDTH");
                flipped |= 33;
            }
            if ((o & 34) == 34) {
                list.add("DISASSOC_LOW_ACK");
                flipped |= 34;
            }
            if ((o & 35) == 35) {
                list.add("EXCEEDED_TXOP");
                flipped |= 35;
            }
            if ((o & 36) == 36) {
                list.add("STA_LEAVING");
                flipped |= 36;
            }
            if ((o & 37) == 37) {
                list.add("END_TS_BA_DLS");
                flipped |= 37;
            }
            if ((o & 38) == 38) {
                list.add("UNKNOWN_TS_BA");
                flipped |= 38;
            }
            if ((o & 39) == 39) {
                list.add("TIMEOUT");
                flipped |= 39;
            }
            if ((o & 45) == 45) {
                list.add("PEERKEY_MISMATCH");
                flipped |= 45;
            }
            if ((o & 46) == 46) {
                list.add("AUTHORIZED_ACCESS_LIMIT_REACHED");
                flipped |= 46;
            }
            if ((o & 47) == 47) {
                list.add("EXTERNAL_SERVICE_REQUIREMENTS");
                flipped |= 47;
            }
            if ((o & 48) == 48) {
                list.add("INVALID_FT_ACTION_FRAME_COUNT");
                flipped |= 48;
            }
            if ((o & 49) == 49) {
                list.add("INVALID_PMKID");
                flipped |= 49;
            }
            if ((o & 50) == 50) {
                list.add("INVALID_MDE");
                flipped |= 50;
            }
            if ((o & 51) == 51) {
                list.add("INVALID_FTE");
                flipped |= 51;
            }
            if ((o & 52) == 52) {
                list.add("MESH_PEERING_CANCELLED");
                flipped |= 52;
            }
            if ((o & 53) == 53) {
                list.add("MESH_MAX_PEERS");
                flipped |= 53;
            }
            if ((o & 54) == 54) {
                list.add("MESH_CONFIG_POLICY_VIOLATION");
                flipped |= 54;
            }
            if ((o & 55) == 55) {
                list.add("MESH_CLOSE_RCVD");
                flipped |= 55;
            }
            if ((o & 56) == 56) {
                list.add("MESH_MAX_RETRIES");
                flipped |= 56;
            }
            if ((o & 57) == 57) {
                list.add("MESH_CONFIRM_TIMEOUT");
                flipped |= 57;
            }
            if ((o & 58) == 58) {
                list.add("MESH_INVALID_GTK");
                flipped |= 58;
            }
            if ((o & 59) == 59) {
                list.add("MESH_INCONSISTENT_PARAMS");
                flipped |= 59;
            }
            if ((o & 60) == 60) {
                list.add("MESH_INVALID_SECURITY_CAP");
                flipped |= 60;
            }
            if ((o & 61) == 61) {
                list.add("MESH_PATH_ERROR_NO_PROXY_INFO");
                flipped |= 61;
            }
            if ((o & 62) == 62) {
                list.add("MESH_PATH_ERROR_NO_FORWARDING_INFO");
                flipped |= 62;
            }
            if ((o & 63) == 63) {
                list.add("MESH_PATH_ERROR_DEST_UNREACHABLE");
                flipped |= 63;
            }
            if ((o & 64) == 64) {
                list.add("MAC_ADDRESS_ALREADY_EXISTS_IN_MBSS");
                flipped |= 64;
            }
            if ((o & 65) == 65) {
                list.add("MESH_CHANNEL_SWITCH_REGULATORY_REQ");
                flipped |= 65;
            }
            if ((o & 66) == 66) {
                list.add("MESH_CHANNEL_SWITCH_UNSPECIFIED");
                flipped |= 66;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class BssidChangeReason {
        public static final byte ASSOC_COMPLETE = 1;
        public static final byte ASSOC_START = 0;
        public static final byte DISASSOC = 2;

        public static final String toString(byte o) {
            if (o == 0) {
                return "ASSOC_START";
            }
            if (o == 1) {
                return "ASSOC_COMPLETE";
            }
            if (o == 2) {
                return "DISASSOC";
            }
            return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
        }

        public static final String dumpBitfield(byte o) {
            ArrayList<String> list = new ArrayList<>();
            byte flipped = 0;
            list.add("ASSOC_START");
            if ((o & 1) == 1) {
                list.add("ASSOC_COMPLETE");
                flipped = (byte) (0 | 1);
            }
            if ((o & 2) == 2) {
                list.add("DISASSOC");
                flipped = (byte) (flipped | 2);
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
            }
            return String.join(" | ", list);
        }
    }

    public static final class Proxy implements ISupplicantStaIfaceCallback {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of android.hardware.wifi.supplicant@1.0::ISupplicantStaIfaceCallback]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onNetworkAdded(int id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onNetworkRemoved(int id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onStateChanged(int newState, byte[] bssid, int id, ArrayList<Byte> ssid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(newState);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeInt32(id);
            _hidl_request.writeInt8Vector(ssid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAnqpQueryDone(byte[] bssid, AnqpData data, Hs20AnqpData hs20Data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            data.writeToParcel(_hidl_request);
            hs20Data.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20IconQueryDone(byte[] bssid, String fileName, ArrayList<Byte> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeString(fileName);
            _hidl_request.writeInt8Vector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20SubscriptionRemediation(byte[] bssid, byte osuMethod, String url) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeInt8(osuMethod);
            _hidl_request.writeString(url);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20DeauthImminentNotice(byte[] bssid, int reasonCode, int reAuthDelayInSec, String url) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeInt32(reasonCode);
            _hidl_request.writeInt32(reAuthDelayInSec);
            _hidl_request.writeString(url);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onDisconnected(byte[] bssid, boolean locallyGenerated, int reasonCode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeBool(locallyGenerated);
            _hidl_request.writeInt32(reasonCode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAssociationRejected(byte[] bssid, int statusCode, boolean timedOut) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeInt32(statusCode);
            _hidl_request.writeBool(timedOut);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAuthenticationTimeout(byte[] bssid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onEapFailure() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onBssidChanged(byte reason, byte[] bssid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt8(reason);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventSuccess() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventFail(byte[] bssid, short configError, short errorInd) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeInt16(configError);
            _hidl_request.writeInt16(errorInd);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventPbcOverlap() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onExtRadioWorkStart(int id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onExtRadioWorkTimeout(int id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements ISupplicantStaIfaceCallback {
        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(ISupplicantStaIfaceCallback.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return ISupplicantStaIfaceCallback.kInterfaceName;
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{-41, -127, -56, -41, -25, -77, -2, 92, -54, -116, -10, -31, -40, Byte.MIN_VALUE, 110, 119, 9, -126, -82, 83, 88, -57, -127, 110, -43, 27, 15, 14, -62, 114, -25, 13}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (ISupplicantStaIfaceCallback.kInterfaceName.equals(descriptor)) {
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

        public void onTransact(int _hidl_code, HwParcel _hidl_request, HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            boolean _hidl_is_oneway = false;
            boolean _hidl_is_oneway2 = true;
            switch (_hidl_code) {
                case 1:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    onNetworkAdded(_hidl_request.readInt32());
                    return;
                case 2:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    onNetworkRemoved(_hidl_request.readInt32());
                    return;
                case 3:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    int newState = _hidl_request.readInt32();
                    byte[] bssid = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid, 6);
                    onStateChanged(newState, bssid, _hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    return;
                case 4:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid2 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid2, 6);
                    AnqpData data = new AnqpData();
                    data.readFromParcel(_hidl_request);
                    Hs20AnqpData hs20Data = new Hs20AnqpData();
                    hs20Data.readFromParcel(_hidl_request);
                    onAnqpQueryDone(bssid2, data, hs20Data);
                    return;
                case 5:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid3 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid3, 6);
                    onHs20IconQueryDone(bssid3, _hidl_request.readString(), _hidl_request.readInt8Vector());
                    return;
                case 6:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid4 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid4, 6);
                    onHs20SubscriptionRemediation(bssid4, _hidl_request.readInt8(), _hidl_request.readString());
                    return;
                case 7:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid5 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid5, 6);
                    onHs20DeauthImminentNotice(bssid5, _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 8:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid6 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid6, 6);
                    onDisconnected(bssid6, _hidl_request.readBool(), _hidl_request.readInt32());
                    return;
                case 9:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid7 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid7, 6);
                    onAssociationRejected(bssid7, _hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 10:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid8 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid8, 6);
                    onAuthenticationTimeout(bssid8);
                    return;
                case 11:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    onEapFailure();
                    return;
                case 12:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    byte reason = _hidl_request.readInt8();
                    byte[] bssid9 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid9, 6);
                    onBssidChanged(reason, bssid9);
                    return;
                case 13:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    onWpsEventSuccess();
                    return;
                case 14:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid10 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid10, 6);
                    onWpsEventFail(bssid10, _hidl_request.readInt16(), _hidl_request.readInt16());
                    return;
                case 15:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    onWpsEventPbcOverlap();
                    return;
                case 16:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    onExtRadioWorkStart(_hidl_request.readInt32());
                    return;
                case 17:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    onExtRadioWorkTimeout(_hidl_request.readInt32());
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
