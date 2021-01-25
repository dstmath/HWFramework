package android.hardware.gnss.V1_0;

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
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.hidata.wavemapping.cons.Constant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public interface IGnssCallback extends IBase {
    public static final String kInterfaceName = "android.hardware.gnss@1.0::IGnssCallback";

    @Override // android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    void gnssAcquireWakelockCb() throws RemoteException;

    void gnssLocationCb(GnssLocation gnssLocation) throws RemoteException;

    void gnssNmeaCb(long j, String str) throws RemoteException;

    void gnssReleaseWakelockCb() throws RemoteException;

    void gnssRequestTimeCb() throws RemoteException;

    void gnssSetCapabilitesCb(int i) throws RemoteException;

    void gnssSetSystemInfoCb(GnssSystemInfo gnssSystemInfo) throws RemoteException;

    void gnssStatusCb(byte b) throws RemoteException;

    void gnssSvStatusCb(GnssSvStatus gnssSvStatus) throws RemoteException;

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

    @Override // android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IGnssCallback asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IGnssCallback)) {
            return (IGnssCallback) iface;
        }
        IGnssCallback proxy = new Proxy(binder);
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

    static IGnssCallback castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IGnssCallback getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IGnssCallback getService(boolean retry) throws RemoteException {
        return getService(AppActConstant.VALUE_DEFAULT, retry);
    }

    static IGnssCallback getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IGnssCallback getService() throws RemoteException {
        return getService(AppActConstant.VALUE_DEFAULT);
    }

    public static final class Capabilities {
        public static final int GEOFENCING = 32;
        public static final int MEASUREMENTS = 64;
        public static final int MSA = 4;
        public static final int MSB = 2;
        public static final int NAV_MESSAGES = 128;
        public static final int ON_DEMAND_TIME = 16;
        public static final int SCHEDULING = 1;
        public static final int SINGLE_SHOT = 8;

        public static final String toString(int o) {
            if (o == 1) {
                return "SCHEDULING";
            }
            if (o == 2) {
                return "MSB";
            }
            if (o == 4) {
                return "MSA";
            }
            if (o == 8) {
                return "SINGLE_SHOT";
            }
            if (o == 16) {
                return "ON_DEMAND_TIME";
            }
            if (o == 32) {
                return "GEOFENCING";
            }
            if (o == 64) {
                return "MEASUREMENTS";
            }
            if (o == 128) {
                return "NAV_MESSAGES";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("SCHEDULING");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("MSB");
                flipped |= 2;
            }
            if ((o & 4) == 4) {
                list.add("MSA");
                flipped |= 4;
            }
            if ((o & 8) == 8) {
                list.add("SINGLE_SHOT");
                flipped |= 8;
            }
            if ((o & 16) == 16) {
                list.add("ON_DEMAND_TIME");
                flipped |= 16;
            }
            if ((o & 32) == 32) {
                list.add("GEOFENCING");
                flipped |= 32;
            }
            if ((o & 64) == 64) {
                list.add("MEASUREMENTS");
                flipped |= 64;
            }
            if ((o & 128) == 128) {
                list.add("NAV_MESSAGES");
                flipped |= 128;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class GnssStatusValue {
        public static final byte ENGINE_OFF = 4;
        public static final byte ENGINE_ON = 3;
        public static final byte NONE = 0;
        public static final byte SESSION_BEGIN = 1;
        public static final byte SESSION_END = 2;

        public static final String toString(byte o) {
            if (o == 0) {
                return Constant.USERDB_APP_NAME_NONE;
            }
            if (o == 1) {
                return "SESSION_BEGIN";
            }
            if (o == 2) {
                return "SESSION_END";
            }
            if (o == 3) {
                return "ENGINE_ON";
            }
            if (o == 4) {
                return "ENGINE_OFF";
            }
            return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
        }

        public static final String dumpBitfield(byte o) {
            ArrayList<String> list = new ArrayList<>();
            byte flipped = 0;
            list.add(Constant.USERDB_APP_NAME_NONE);
            if ((o & 1) == 1) {
                list.add("SESSION_BEGIN");
                flipped = (byte) (0 | 1);
            }
            if ((o & 2) == 2) {
                list.add("SESSION_END");
                flipped = (byte) (flipped | 2);
            }
            if ((o & 3) == 3) {
                list.add("ENGINE_ON");
                flipped = (byte) (flipped | 3);
            }
            if ((o & 4) == 4) {
                list.add("ENGINE_OFF");
                flipped = (byte) (flipped | 4);
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
            }
            return String.join(" | ", list);
        }
    }

    public static final class GnssSvFlags {
        public static final byte HAS_ALMANAC_DATA = 2;
        public static final byte HAS_CARRIER_FREQUENCY = 8;
        public static final byte HAS_EPHEMERIS_DATA = 1;
        public static final byte NONE = 0;
        public static final byte USED_IN_FIX = 4;

        public static final String toString(byte o) {
            if (o == 0) {
                return Constant.USERDB_APP_NAME_NONE;
            }
            if (o == 1) {
                return "HAS_EPHEMERIS_DATA";
            }
            if (o == 2) {
                return "HAS_ALMANAC_DATA";
            }
            if (o == 4) {
                return "USED_IN_FIX";
            }
            if (o == 8) {
                return "HAS_CARRIER_FREQUENCY";
            }
            return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
        }

        public static final String dumpBitfield(byte o) {
            ArrayList<String> list = new ArrayList<>();
            byte flipped = 0;
            list.add(Constant.USERDB_APP_NAME_NONE);
            if ((o & 1) == 1) {
                list.add("HAS_EPHEMERIS_DATA");
                flipped = (byte) (0 | 1);
            }
            if ((o & 2) == 2) {
                list.add("HAS_ALMANAC_DATA");
                flipped = (byte) (flipped | 2);
            }
            if ((o & 4) == 4) {
                list.add("USED_IN_FIX");
                flipped = (byte) (flipped | 4);
            }
            if ((o & 8) == 8) {
                list.add("HAS_CARRIER_FREQUENCY");
                flipped = (byte) (flipped | 8);
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
            }
            return String.join(" | ", list);
        }
    }

    public static final class GnssSvInfo {
        public float azimuthDegrees;
        public float cN0Dbhz;
        public float carrierFrequencyHz;
        public byte constellation;
        public float elevationDegrees;
        public byte svFlag;
        public short svid;

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != GnssSvInfo.class) {
                return false;
            }
            GnssSvInfo other = (GnssSvInfo) otherObject;
            if (this.svid == other.svid && this.constellation == other.constellation && this.cN0Dbhz == other.cN0Dbhz && this.elevationDegrees == other.elevationDegrees && this.azimuthDegrees == other.azimuthDegrees && this.carrierFrequencyHz == other.carrierFrequencyHz && HidlSupport.deepEquals(Byte.valueOf(this.svFlag), Byte.valueOf(other.svFlag))) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.svid))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.constellation))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.cN0Dbhz))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.elevationDegrees))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.azimuthDegrees))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.carrierFrequencyHz))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.svFlag))));
        }

        public final String toString() {
            return "{.svid = " + ((int) this.svid) + ", .constellation = " + GnssConstellationType.toString(this.constellation) + ", .cN0Dbhz = " + this.cN0Dbhz + ", .elevationDegrees = " + this.elevationDegrees + ", .azimuthDegrees = " + this.azimuthDegrees + ", .carrierFrequencyHz = " + this.carrierFrequencyHz + ", .svFlag = " + GnssSvFlags.dumpBitfield(this.svFlag) + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
        }

        public static final ArrayList<GnssSvInfo> readVectorFromParcel(HwParcel parcel) {
            ArrayList<GnssSvInfo> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                GnssSvInfo _hidl_vec_element = new GnssSvInfo();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            this.svid = _hidl_blob.getInt16(0 + _hidl_offset);
            this.constellation = _hidl_blob.getInt8(2 + _hidl_offset);
            this.cN0Dbhz = _hidl_blob.getFloat(4 + _hidl_offset);
            this.elevationDegrees = _hidl_blob.getFloat(8 + _hidl_offset);
            this.azimuthDegrees = _hidl_blob.getFloat(12 + _hidl_offset);
            this.carrierFrequencyHz = _hidl_blob.getFloat(16 + _hidl_offset);
            this.svFlag = _hidl_blob.getInt8(20 + _hidl_offset);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(24);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<GnssSvInfo> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            _hidl_blob.putInt16(0 + _hidl_offset, this.svid);
            _hidl_blob.putInt8(2 + _hidl_offset, this.constellation);
            _hidl_blob.putFloat(4 + _hidl_offset, this.cN0Dbhz);
            _hidl_blob.putFloat(8 + _hidl_offset, this.elevationDegrees);
            _hidl_blob.putFloat(12 + _hidl_offset, this.azimuthDegrees);
            _hidl_blob.putFloat(16 + _hidl_offset, this.carrierFrequencyHz);
            _hidl_blob.putInt8(20 + _hidl_offset, this.svFlag);
        }
    }

    public static final class GnssSvStatus {
        public GnssSvInfo[] gnssSvList = new GnssSvInfo[64];
        public int numSvs;

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != GnssSvStatus.class) {
                return false;
            }
            GnssSvStatus other = (GnssSvStatus) otherObject;
            if (this.numSvs == other.numSvs && HidlSupport.deepEquals(this.gnssSvList, other.gnssSvList)) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.numSvs))), Integer.valueOf(HidlSupport.deepHashCode(this.gnssSvList)));
        }

        public final String toString() {
            return "{.numSvs = " + this.numSvs + ", .gnssSvList = " + Arrays.toString(this.gnssSvList) + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(1540), 0);
        }

        public static final ArrayList<GnssSvStatus> readVectorFromParcel(HwParcel parcel) {
            ArrayList<GnssSvStatus> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1540), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                GnssSvStatus _hidl_vec_element = new GnssSvStatus();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 1540));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            this.numSvs = _hidl_blob.getInt32(0 + _hidl_offset);
            long _hidl_array_offset_0 = 4 + _hidl_offset;
            for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 64; _hidl_index_0_0++) {
                this.gnssSvList[_hidl_index_0_0] = new GnssSvInfo();
                this.gnssSvList[_hidl_index_0_0].readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_array_offset_0);
                _hidl_array_offset_0 += 24;
            }
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(1540);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<GnssSvStatus> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 1540);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 1540));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            _hidl_blob.putInt32(0 + _hidl_offset, this.numSvs);
            long _hidl_array_offset_0 = 4 + _hidl_offset;
            for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 64; _hidl_index_0_0++) {
                this.gnssSvList[_hidl_index_0_0].writeEmbeddedToBlob(_hidl_blob, _hidl_array_offset_0);
                _hidl_array_offset_0 += 24;
            }
        }
    }

    public static final class GnssSystemInfo {
        public short yearOfHw;

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject != null && otherObject.getClass() == GnssSystemInfo.class && this.yearOfHw == ((GnssSystemInfo) otherObject).yearOfHw) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.yearOfHw))));
        }

        public final String toString() {
            return "{.yearOfHw = " + ((int) this.yearOfHw) + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(2), 0);
        }

        public static final ArrayList<GnssSystemInfo> readVectorFromParcel(HwParcel parcel) {
            ArrayList<GnssSystemInfo> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 2), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                GnssSystemInfo _hidl_vec_element = new GnssSystemInfo();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 2));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            this.yearOfHw = _hidl_blob.getInt16(0 + _hidl_offset);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(2);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<GnssSystemInfo> _hidl_vec) {
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
            _hidl_blob.putInt16(0 + _hidl_offset, this.yearOfHw);
        }
    }

    public static final class Proxy implements IGnssCallback {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of android.hardware.gnss@1.0::IGnssCallback]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback
        public void gnssLocationCb(GnssLocation location) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssCallback.kInterfaceName);
            location.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback
        public void gnssStatusCb(byte status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssCallback.kInterfaceName);
            _hidl_request.writeInt8(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback
        public void gnssSvStatusCb(GnssSvStatus svInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssCallback.kInterfaceName);
            svInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback
        public void gnssNmeaCb(long timestamp, String nmea) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssCallback.kInterfaceName);
            _hidl_request.writeInt64(timestamp);
            _hidl_request.writeString(nmea);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback
        public void gnssSetCapabilitesCb(int capabilities) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssCallback.kInterfaceName);
            _hidl_request.writeInt32(capabilities);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback
        public void gnssAcquireWakelockCb() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback
        public void gnssReleaseWakelockCb() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback
        public void gnssRequestTimeCb() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback
        public void gnssSetSystemInfoCb(GnssSystemInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssCallback.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IGnssCallback {
        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IGnssCallback.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IGnssCallback.kInterfaceName;
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{33, 22, 91, -114, 48, -60, -78, -43, 41, Byte.MIN_VALUE, -28, 114, -113, 102, 20, 32, -83, -63, 110, 56, -69, -25, 52, 118, -64, 107, 32, -123, -66, -112, -113, 76}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // android.hardware.gnss.V1_0.IGnssCallback, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IGnssCallback.kInterfaceName.equals(descriptor)) {
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
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssCallback.kInterfaceName);
                    GnssLocation location = new GnssLocation();
                    location.readFromParcel(_hidl_request);
                    gnssLocationCb(location);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 2:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssCallback.kInterfaceName);
                    gnssStatusCb(_hidl_request.readInt8());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 3:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssCallback.kInterfaceName);
                    GnssSvStatus svInfo = new GnssSvStatus();
                    svInfo.readFromParcel(_hidl_request);
                    gnssSvStatusCb(svInfo);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 4:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssCallback.kInterfaceName);
                    gnssNmeaCb(_hidl_request.readInt64(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 5:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssCallback.kInterfaceName);
                    gnssSetCapabilitesCb(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 6:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssCallback.kInterfaceName);
                    gnssAcquireWakelockCb();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 7:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssCallback.kInterfaceName);
                    gnssReleaseWakelockCb();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 8:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssCallback.kInterfaceName);
                    gnssRequestTimeCb();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 9:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssCallback.kInterfaceName);
                    GnssSystemInfo info = new GnssSystemInfo();
                    info.readFromParcel(_hidl_request);
                    gnssSetSystemInfoCb(info);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                default:
                    switch (_hidl_code) {
                        case 256067662:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        case 256921159:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
