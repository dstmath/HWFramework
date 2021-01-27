package android.hardware.wifi.hostapd.V1_0;

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

public interface IHostapd extends IBase {
    public static final String kInterfaceName = "android.hardware.wifi.hostapd@1.0::IHostapd";

    HostapdStatus addAccessPoint(IfaceParams ifaceParams, NetworkParams networkParams) throws RemoteException;

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

    @Override // android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    HostapdStatus removeAccessPoint(String str) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    void terminate() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IHostapd asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IHostapd)) {
            return (IHostapd) iface;
        }
        IHostapd proxy = new Proxy(binder);
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

    static IHostapd castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IHostapd getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IHostapd getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    static IHostapd getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IHostapd getService() throws RemoteException {
        return getService("default");
    }

    public static final class ParamSizeLimits {
        public static final int SSID_MAX_LEN_IN_BYTES = 32;
        public static final int WPA2_PSK_PASSPHRASE_MAX_LEN_IN_BYTES = 63;
        public static final int WPA2_PSK_PASSPHRASE_MIN_LEN_IN_BYTES = 8;

        public static final String toString(int o) {
            if (o == 32) {
                return "SSID_MAX_LEN_IN_BYTES";
            }
            if (o == 8) {
                return "WPA2_PSK_PASSPHRASE_MIN_LEN_IN_BYTES";
            }
            if (o == 63) {
                return "WPA2_PSK_PASSPHRASE_MAX_LEN_IN_BYTES";
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
                list.add("WPA2_PSK_PASSPHRASE_MIN_LEN_IN_BYTES");
                flipped |= 8;
            }
            if ((o & 63) == 63) {
                list.add("WPA2_PSK_PASSPHRASE_MAX_LEN_IN_BYTES");
                flipped |= 63;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class EncryptionType {
        public static final int NONE = 0;
        public static final int WPA = 1;
        public static final int WPA2 = 2;

        public static final String toString(int o) {
            if (o == 0) {
                return "NONE";
            }
            if (o == 1) {
                return "WPA";
            }
            if (o == 2) {
                return "WPA2";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            list.add("NONE");
            if ((o & 1) == 1) {
                list.add("WPA");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("WPA2");
                flipped |= 2;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class Band {
        public static final int BAND_2_4_GHZ = 0;
        public static final int BAND_5_GHZ = 1;
        public static final int BAND_ANY = 2;

        public static final String toString(int o) {
            if (o == 0) {
                return "BAND_2_4_GHZ";
            }
            if (o == 1) {
                return "BAND_5_GHZ";
            }
            if (o == 2) {
                return "BAND_ANY";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            list.add("BAND_2_4_GHZ");
            if ((o & 1) == 1) {
                list.add("BAND_5_GHZ");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("BAND_ANY");
                flipped |= 2;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class HwModeParams {
        public boolean enable80211AC;
        public boolean enable80211N;

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != HwModeParams.class) {
                return false;
            }
            HwModeParams other = (HwModeParams) otherObject;
            if (this.enable80211N == other.enable80211N && this.enable80211AC == other.enable80211AC) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.enable80211N))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.enable80211AC))));
        }

        public final String toString() {
            return "{.enable80211N = " + this.enable80211N + ", .enable80211AC = " + this.enable80211AC + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(2), 0);
        }

        public static final ArrayList<HwModeParams> readVectorFromParcel(HwParcel parcel) {
            ArrayList<HwModeParams> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 2), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                HwModeParams _hidl_vec_element = new HwModeParams();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 2));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            this.enable80211N = _hidl_blob.getBool(0 + _hidl_offset);
            this.enable80211AC = _hidl_blob.getBool(1 + _hidl_offset);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(2);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<HwModeParams> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 2);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 2));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            _hidl_blob.putBool(0 + _hidl_offset, this.enable80211N);
            _hidl_blob.putBool(1 + _hidl_offset, this.enable80211AC);
        }
    }

    public static final class ChannelParams {
        public boolean acsShouldExcludeDfs;
        public int band;
        public int channel;
        public boolean enableAcs;

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != ChannelParams.class) {
                return false;
            }
            ChannelParams other = (ChannelParams) otherObject;
            if (this.enableAcs == other.enableAcs && this.acsShouldExcludeDfs == other.acsShouldExcludeDfs && this.channel == other.channel && this.band == other.band) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.enableAcs))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.acsShouldExcludeDfs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.channel))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.band))));
        }

        public final String toString() {
            return "{.enableAcs = " + this.enableAcs + ", .acsShouldExcludeDfs = " + this.acsShouldExcludeDfs + ", .channel = " + this.channel + ", .band = " + Band.toString(this.band) + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(12), 0);
        }

        public static final ArrayList<ChannelParams> readVectorFromParcel(HwParcel parcel) {
            ArrayList<ChannelParams> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 12), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                ChannelParams _hidl_vec_element = new ChannelParams();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 12));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            this.enableAcs = _hidl_blob.getBool(0 + _hidl_offset);
            this.acsShouldExcludeDfs = _hidl_blob.getBool(1 + _hidl_offset);
            this.channel = _hidl_blob.getInt32(4 + _hidl_offset);
            this.band = _hidl_blob.getInt32(8 + _hidl_offset);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(12);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<ChannelParams> _hidl_vec) {
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
            _hidl_blob.putBool(0 + _hidl_offset, this.enableAcs);
            _hidl_blob.putBool(1 + _hidl_offset, this.acsShouldExcludeDfs);
            _hidl_blob.putInt32(4 + _hidl_offset, this.channel);
            _hidl_blob.putInt32(8 + _hidl_offset, this.band);
        }
    }

    public static final class IfaceParams {
        public ChannelParams channelParams = new ChannelParams();
        public HwModeParams hwModeParams = new HwModeParams();
        public String ifaceName = new String();

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != IfaceParams.class) {
                return false;
            }
            IfaceParams other = (IfaceParams) otherObject;
            if (HidlSupport.deepEquals(this.ifaceName, other.ifaceName) && HidlSupport.deepEquals(this.hwModeParams, other.hwModeParams) && HidlSupport.deepEquals(this.channelParams, other.channelParams)) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.ifaceName)), Integer.valueOf(HidlSupport.deepHashCode(this.hwModeParams)), Integer.valueOf(HidlSupport.deepHashCode(this.channelParams)));
        }

        public final String toString() {
            return "{.ifaceName = " + this.ifaceName + ", .hwModeParams = " + this.hwModeParams + ", .channelParams = " + this.channelParams + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
        }

        public static final ArrayList<IfaceParams> readVectorFromParcel(HwParcel parcel) {
            ArrayList<IfaceParams> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                IfaceParams _hidl_vec_element = new IfaceParams();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            this.ifaceName = _hidl_blob.getString(_hidl_offset + 0);
            parcel.readEmbeddedBuffer((long) (this.ifaceName.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
            this.hwModeParams.readEmbeddedFromParcel(parcel, _hidl_blob, 16 + _hidl_offset);
            this.channelParams.readEmbeddedFromParcel(parcel, _hidl_blob, 20 + _hidl_offset);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(32);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<IfaceParams> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            _hidl_blob.putString(0 + _hidl_offset, this.ifaceName);
            this.hwModeParams.writeEmbeddedToBlob(_hidl_blob, 16 + _hidl_offset);
            this.channelParams.writeEmbeddedToBlob(_hidl_blob, 20 + _hidl_offset);
        }
    }

    public static final class NetworkParams {
        public int encryptionType;
        public boolean isHidden;
        public String pskPassphrase = new String();
        public ArrayList<Byte> ssid = new ArrayList<>();

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != NetworkParams.class) {
                return false;
            }
            NetworkParams other = (NetworkParams) otherObject;
            if (HidlSupport.deepEquals(this.ssid, other.ssid) && this.isHidden == other.isHidden && this.encryptionType == other.encryptionType && HidlSupport.deepEquals(this.pskPassphrase, other.pskPassphrase)) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.ssid)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isHidden))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.encryptionType))), Integer.valueOf(HidlSupport.deepHashCode(this.pskPassphrase)));
        }

        public final String toString() {
            return "{.ssid = " + this.ssid + ", .isHidden = " + this.isHidden + ", .encryptionType = " + EncryptionType.toString(this.encryptionType) + ", .pskPassphrase = " + this.pskPassphrase + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
        }

        public static final ArrayList<NetworkParams> readVectorFromParcel(HwParcel parcel) {
            ArrayList<NetworkParams> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                NetworkParams _hidl_vec_element = new NetworkParams();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 0 + 8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, true);
            this.ssid.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                this.ssid.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
            }
            this.isHidden = _hidl_blob.getBool(_hidl_offset + 16);
            this.encryptionType = _hidl_blob.getInt32(_hidl_offset + 20);
            this.pskPassphrase = _hidl_blob.getString(_hidl_offset + 24);
            parcel.readEmbeddedBuffer((long) (this.pskPassphrase.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 24 + 0, false);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(40);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NetworkParams> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 40);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 40));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            int _hidl_vec_size = this.ssid.size();
            _hidl_blob.putInt32(_hidl_offset + 0 + 8, _hidl_vec_size);
            _hidl_blob.putBool(_hidl_offset + 0 + 12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                childBlob.putInt8((long) (_hidl_index_0 * 1), this.ssid.get(_hidl_index_0).byteValue());
            }
            _hidl_blob.putBlob(_hidl_offset + 0 + 0, childBlob);
            _hidl_blob.putBool(16 + _hidl_offset, this.isHidden);
            _hidl_blob.putInt32(20 + _hidl_offset, this.encryptionType);
            _hidl_blob.putString(24 + _hidl_offset, this.pskPassphrase);
        }
    }

    public static final class Proxy implements IHostapd {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of android.hardware.wifi.hostapd@1.0::IHostapd]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd
        public HostapdStatus addAccessPoint(IfaceParams ifaceParams, NetworkParams nwParams) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHostapd.kInterfaceName);
            ifaceParams.writeToParcel(_hidl_request);
            nwParams.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                HostapdStatus _hidl_out_status = new HostapdStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd
        public HostapdStatus removeAccessPoint(String ifaceName) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHostapd.kInterfaceName);
            _hidl_request.writeString(ifaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                HostapdStatus _hidl_out_status = new HostapdStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd
        public void terminate() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHostapd.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IHostapd {
        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IHostapd.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IHostapd.kInterfaceName;
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{-18, 8, 40, 13, -30, 28, -76, 30, 62, -62, 109, 110, -42, 54, -57, 1, -73, -9, 5, 22, -25, 31, -78, 47, 79, -26, 10, 19, -26, 3, -12, 6}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // android.hardware.wifi.hostapd.V1_0.IHostapd, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IHostapd.kInterfaceName.equals(descriptor)) {
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
            if (_hidl_code == 1) {
                if ((_hidl_flags & 1) == 0) {
                    _hidl_is_oneway2 = false;
                }
                if (_hidl_is_oneway2) {
                    _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                    _hidl_reply.send();
                    return;
                }
                _hidl_request.enforceInterface(IHostapd.kInterfaceName);
                IfaceParams ifaceParams = new IfaceParams();
                ifaceParams.readFromParcel(_hidl_request);
                NetworkParams nwParams = new NetworkParams();
                nwParams.readFromParcel(_hidl_request);
                HostapdStatus _hidl_out_status = addAccessPoint(ifaceParams, nwParams);
                _hidl_reply.writeStatus(0);
                _hidl_out_status.writeToParcel(_hidl_reply);
                _hidl_reply.send();
            } else if (_hidl_code == 2) {
                if ((_hidl_flags & 1) == 0) {
                    _hidl_is_oneway2 = false;
                }
                if (_hidl_is_oneway2) {
                    _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                    _hidl_reply.send();
                    return;
                }
                _hidl_request.enforceInterface(IHostapd.kInterfaceName);
                HostapdStatus _hidl_out_status2 = removeAccessPoint(_hidl_request.readString());
                _hidl_reply.writeStatus(0);
                _hidl_out_status2.writeToParcel(_hidl_reply);
                _hidl_reply.send();
            } else if (_hidl_code != 3) {
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
            } else {
                if ((_hidl_flags & 1) != 0) {
                    _hidl_is_oneway = true;
                }
                if (!_hidl_is_oneway) {
                    _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                    _hidl_reply.send();
                    return;
                }
                _hidl_request.enforceInterface(IHostapd.kInterfaceName);
                terminate();
            }
        }
    }
}
