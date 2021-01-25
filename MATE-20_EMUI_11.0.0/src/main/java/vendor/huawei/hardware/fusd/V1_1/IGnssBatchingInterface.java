package vendor.huawei.hardware.fusd.V1_1;

import android.hardware.gnss.V1_0.GnssLocation;
import android.hardware.gnss.V1_0.IGnssBatching;
import android.hardware.gnss.V1_0.IGnssBatchingCallback;
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

public interface IGnssBatchingInterface extends IGnssBatching {
    public static final String kInterfaceName = "vendor.huawei.hardware.fusd@1.1::IGnssBatchingInterface";

    @Override // android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    void getBatchedLocation(int i) throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    boolean injectLocation(GnssLocation gnssLocation) throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    void sendGnssStatus(int i) throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    boolean startBatching(int i, FlpBatchOpts flpBatchOpts) throws RemoteException;

    boolean stopBatching(int i) throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    boolean updateBatchingOptions(int i, FlpBatchOpts flpBatchOpts) throws RemoteException;

    static IGnssBatchingInterface asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IGnssBatchingInterface)) {
            return (IGnssBatchingInterface) iface;
        }
        IGnssBatchingInterface proxy = new Proxy(binder);
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

    static IGnssBatchingInterface castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IGnssBatchingInterface getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IGnssBatchingInterface getService(boolean retry) throws RemoteException {
        return getService(AppActConstant.VALUE_DEFAULT, retry);
    }

    static IGnssBatchingInterface getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IGnssBatchingInterface getService() throws RemoteException {
        return getService(AppActConstant.VALUE_DEFAULT);
    }

    public static final class FlpFlag {
        public static final byte WAKEUP_ON_FIFO_FULL = 1;
        public static final byte WAKEUP_ON_LOCATION_FIX = 2;

        public static final String toString(byte o) {
            if (o == 1) {
                return "WAKEUP_ON_FIFO_FULL";
            }
            if (o == 2) {
                return "WAKEUP_ON_LOCATION_FIX";
            }
            return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
        }

        public static final String dumpBitfield(byte o) {
            ArrayList<String> list = new ArrayList<>();
            byte flipped = 0;
            if ((o & 1) == 1) {
                list.add("WAKEUP_ON_FIFO_FULL");
                flipped = (byte) (0 | 1);
            }
            if ((o & 2) == 2) {
                list.add("WAKEUP_ON_LOCATION_FIX");
                flipped = (byte) (flipped | 2);
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
            }
            return String.join(" | ", list);
        }
    }

    public static final class FlpSource {
        public static final byte BLUETOOTH = 16;
        public static final byte CELL = 8;
        public static final byte GNSS = 1;
        public static final byte SENSORS = 4;
        public static final byte WIFI = 2;

        public static final String toString(byte o) {
            if (o == 1) {
                return "GNSS";
            }
            if (o == 2) {
                return Constant.USERDB_APP_NAME_WIFI;
            }
            if (o == 4) {
                return "SENSORS";
            }
            if (o == 8) {
                return "CELL";
            }
            if (o == 16) {
                return "BLUETOOTH";
            }
            return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
        }

        public static final String dumpBitfield(byte o) {
            ArrayList<String> list = new ArrayList<>();
            byte flipped = 0;
            if ((o & 1) == 1) {
                list.add("GNSS");
                flipped = (byte) (0 | 1);
            }
            if ((o & 2) == 2) {
                list.add(Constant.USERDB_APP_NAME_WIFI);
                flipped = (byte) (flipped | 2);
            }
            if ((o & 4) == 4) {
                list.add("SENSORS");
                flipped = (byte) (flipped | 4);
            }
            if ((o & 8) == 8) {
                list.add("CELL");
                flipped = (byte) (flipped | 8);
            }
            if ((o & BLUETOOTH) == 16) {
                list.add("BLUETOOTH");
                flipped = (byte) (flipped | BLUETOOTH);
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
            }
            return String.join(" | ", list);
        }
    }

    public static final class FlpBatchOpts {
        public int batch_distance;
        public byte flags;
        public double max_power_allocation_mW;
        public long period_ns;
        public float smallest_displacement_meters;
        public byte sources_to_use;

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != FlpBatchOpts.class) {
                return false;
            }
            FlpBatchOpts other = (FlpBatchOpts) otherObject;
            if (this.max_power_allocation_mW == other.max_power_allocation_mW && HidlSupport.deepEquals(Byte.valueOf(this.sources_to_use), Byte.valueOf(other.sources_to_use)) && HidlSupport.deepEquals(Byte.valueOf(this.flags), Byte.valueOf(other.flags)) && this.period_ns == other.period_ns && this.smallest_displacement_meters == other.smallest_displacement_meters && this.batch_distance == other.batch_distance) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Double.valueOf(this.max_power_allocation_mW))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.sources_to_use))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.flags))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.period_ns))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.smallest_displacement_meters))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.batch_distance))));
        }

        public final String toString() {
            return "{.max_power_allocation_mW = " + this.max_power_allocation_mW + ", .sources_to_use = " + FlpSource.dumpBitfield(this.sources_to_use) + ", .flags = " + FlpFlag.dumpBitfield(this.flags) + ", .period_ns = " + this.period_ns + ", .smallest_displacement_meters = " + this.smallest_displacement_meters + ", .batch_distance = " + this.batch_distance + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
        }

        public static final ArrayList<FlpBatchOpts> readVectorFromParcel(HwParcel parcel) {
            ArrayList<FlpBatchOpts> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                FlpBatchOpts _hidl_vec_element = new FlpBatchOpts();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            this.max_power_allocation_mW = _hidl_blob.getDouble(0 + _hidl_offset);
            this.sources_to_use = _hidl_blob.getInt8(8 + _hidl_offset);
            this.flags = _hidl_blob.getInt8(9 + _hidl_offset);
            this.period_ns = _hidl_blob.getInt64(16 + _hidl_offset);
            this.smallest_displacement_meters = _hidl_blob.getFloat(24 + _hidl_offset);
            this.batch_distance = _hidl_blob.getInt32(28 + _hidl_offset);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(32);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<FlpBatchOpts> _hidl_vec) {
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
            _hidl_blob.putDouble(0 + _hidl_offset, this.max_power_allocation_mW);
            _hidl_blob.putInt8(8 + _hidl_offset, this.sources_to_use);
            _hidl_blob.putInt8(9 + _hidl_offset, this.flags);
            _hidl_blob.putInt64(16 + _hidl_offset, this.period_ns);
            _hidl_blob.putFloat(24 + _hidl_offset, this.smallest_displacement_meters);
            _hidl_blob.putInt32(28 + _hidl_offset, this.batch_distance);
        }
    }

    public static final class Proxy implements IGnssBatchingInterface {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.huawei.hardware.fusd@1.1::IGnssBatchingInterface]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // android.hardware.gnss.V1_0.IGnssBatching
        public boolean init(IGnssBatchingCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssBatching.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssBatching
        public short getBatchSize() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssBatching.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt16();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssBatching
        public boolean start(IGnssBatching.Options options) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssBatching.kInterfaceName);
            options.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssBatching
        public void flush() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssBatching.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssBatching
        public boolean stop() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssBatching.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssBatching
        public void cleanup() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssBatching.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface
        public boolean startBatching(int id, FlpBatchOpts options) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssBatchingInterface.kInterfaceName);
            _hidl_request.writeInt32(id);
            options.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface
        public boolean stopBatching(int id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssBatchingInterface.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface
        public boolean updateBatchingOptions(int id, FlpBatchOpts new_options) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssBatchingInterface.kInterfaceName);
            _hidl_request.writeInt32(id);
            new_options.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface
        public void getBatchedLocation(int last_n_locations) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssBatchingInterface.kInterfaceName);
            _hidl_request.writeInt32(last_n_locations);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface
        public boolean injectLocation(GnssLocation location) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssBatchingInterface.kInterfaceName);
            location.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface
        public void sendGnssStatus(int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssBatchingInterface.kInterfaceName);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IGnssBatchingInterface {
        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IGnssBatchingInterface.kInterfaceName, IGnssBatching.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IGnssBatchingInterface.kInterfaceName;
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{30, -33, -88, 30, 100, 87, 63, 53, -36, -25, 1, 56, 69, -91, 119, 3, 6, 7, -40, -51, 55, 26, FlpSource.BLUETOOTH, 83, 37, 123, 61, -98, -113, 30, -120, -110}, new byte[]{-80, 92, -104, 60, -121, -61, 55, 110, 20, 82, 35, 104, -116, 59, 84, 27, 94, 17, -72, 39, -14, 17, -29, -115, 90, 49, -81, 28, -93, -94, -30, 34}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface, android.hardware.gnss.V1_0.IGnssBatching, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IGnssBatchingInterface.kInterfaceName.equals(descriptor)) {
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
                    _hidl_request.enforceInterface(IGnssBatching.kInterfaceName);
                    boolean _hidl_out_success = init(IGnssBatchingCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_success);
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
                    _hidl_request.enforceInterface(IGnssBatching.kInterfaceName);
                    short _hidl_out_batchSize = getBatchSize();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt16(_hidl_out_batchSize);
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
                    _hidl_request.enforceInterface(IGnssBatching.kInterfaceName);
                    IGnssBatching.Options options = new IGnssBatching.Options();
                    options.readFromParcel(_hidl_request);
                    boolean _hidl_out_success2 = start(options);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_success2);
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
                    _hidl_request.enforceInterface(IGnssBatching.kInterfaceName);
                    flush();
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
                    _hidl_request.enforceInterface(IGnssBatching.kInterfaceName);
                    boolean _hidl_out_success3 = stop();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_success3);
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
                    _hidl_request.enforceInterface(IGnssBatching.kInterfaceName);
                    cleanup();
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
                    _hidl_request.enforceInterface(IGnssBatchingInterface.kInterfaceName);
                    int id = _hidl_request.readInt32();
                    FlpBatchOpts options2 = new FlpBatchOpts();
                    options2.readFromParcel(_hidl_request);
                    boolean _hidl_out_success4 = startBatching(id, options2);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_success4);
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
                    _hidl_request.enforceInterface(IGnssBatchingInterface.kInterfaceName);
                    boolean _hidl_out_success5 = stopBatching(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_success5);
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
                    _hidl_request.enforceInterface(IGnssBatchingInterface.kInterfaceName);
                    int id2 = _hidl_request.readInt32();
                    FlpBatchOpts new_options = new FlpBatchOpts();
                    new_options.readFromParcel(_hidl_request);
                    boolean _hidl_out_success6 = updateBatchingOptions(id2, new_options);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_success6);
                    _hidl_reply.send();
                    return;
                case 10:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssBatchingInterface.kInterfaceName);
                    getBatchedLocation(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 11:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssBatchingInterface.kInterfaceName);
                    GnssLocation location = new GnssLocation();
                    location.readFromParcel(_hidl_request);
                    boolean _hidl_out_success7 = injectLocation(location);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_success7);
                    _hidl_reply.send();
                    return;
                case 12:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssBatchingInterface.kInterfaceName);
                    sendGnssStatus(_hidl_request.readInt32());
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
