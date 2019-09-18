package android.hardware.wifi.V1_0;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.RemoteException;
import com.android.server.wifi.HalDeviceManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public interface IWifiChip extends IBase {
    public static final String kInterfaceName = "android.hardware.wifi@1.0::IWifiChip";

    public static final class ChipCapabilityMask {
        public static final int DEBUG_ERROR_ALERTS = 128;
        public static final int DEBUG_HOST_WAKE_REASON_STATS = 64;
        public static final int DEBUG_MEMORY_DRIVER_DUMP = 2;
        public static final int DEBUG_MEMORY_FIRMWARE_DUMP = 1;
        public static final int DEBUG_RING_BUFFER_CONNECT_EVENT = 4;
        public static final int DEBUG_RING_BUFFER_POWER_EVENT = 8;
        public static final int DEBUG_RING_BUFFER_VENDOR_DATA = 32;
        public static final int DEBUG_RING_BUFFER_WAKELOCK_EVENT = 16;

        public static final String toString(int o) {
            if (o == 1) {
                return "DEBUG_MEMORY_FIRMWARE_DUMP";
            }
            if (o == 2) {
                return "DEBUG_MEMORY_DRIVER_DUMP";
            }
            if (o == 4) {
                return "DEBUG_RING_BUFFER_CONNECT_EVENT";
            }
            if (o == 8) {
                return "DEBUG_RING_BUFFER_POWER_EVENT";
            }
            if (o == 16) {
                return "DEBUG_RING_BUFFER_WAKELOCK_EVENT";
            }
            if (o == 32) {
                return "DEBUG_RING_BUFFER_VENDOR_DATA";
            }
            if (o == 64) {
                return "DEBUG_HOST_WAKE_REASON_STATS";
            }
            if (o == 128) {
                return "DEBUG_ERROR_ALERTS";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("DEBUG_MEMORY_FIRMWARE_DUMP");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("DEBUG_MEMORY_DRIVER_DUMP");
                flipped |= 2;
            }
            if ((o & 4) == 4) {
                list.add("DEBUG_RING_BUFFER_CONNECT_EVENT");
                flipped |= 4;
            }
            if ((o & 8) == 8) {
                list.add("DEBUG_RING_BUFFER_POWER_EVENT");
                flipped |= 8;
            }
            if ((o & 16) == 16) {
                list.add("DEBUG_RING_BUFFER_WAKELOCK_EVENT");
                flipped |= 16;
            }
            if ((o & 32) == 32) {
                list.add("DEBUG_RING_BUFFER_VENDOR_DATA");
                flipped |= 32;
            }
            if ((o & 64) == 64) {
                list.add("DEBUG_HOST_WAKE_REASON_STATS");
                flipped |= 64;
            }
            if ((o & 128) == 128) {
                list.add("DEBUG_ERROR_ALERTS");
                flipped |= 128;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class ChipDebugInfo {
        public String driverDescription = new String();
        public String firmwareDescription = new String();

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != ChipDebugInfo.class) {
                return false;
            }
            ChipDebugInfo other = (ChipDebugInfo) otherObject;
            if (HidlSupport.deepEquals(this.driverDescription, other.driverDescription) && HidlSupport.deepEquals(this.firmwareDescription, other.firmwareDescription)) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.driverDescription)), Integer.valueOf(HidlSupport.deepHashCode(this.firmwareDescription))});
        }

        public final String toString() {
            return "{" + ".driverDescription = " + this.driverDescription + ", .firmwareDescription = " + this.firmwareDescription + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
        }

        public static final ArrayList<ChipDebugInfo> readVectorFromParcel(HwParcel parcel) {
            ArrayList<ChipDebugInfo> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                ChipDebugInfo _hidl_vec_element = new ChipDebugInfo();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            HwBlob hwBlob = _hidl_blob;
            this.driverDescription = hwBlob.getString(_hidl_offset + 0);
            parcel.readEmbeddedBuffer((long) (this.driverDescription.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
            this.firmwareDescription = hwBlob.getString(_hidl_offset + 16);
            parcel.readEmbeddedBuffer((long) (this.firmwareDescription.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(32);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<ChipDebugInfo> _hidl_vec) {
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
            _hidl_blob.putString(0 + _hidl_offset, this.driverDescription);
            _hidl_blob.putString(16 + _hidl_offset, this.firmwareDescription);
        }
    }

    public static final class ChipIfaceCombination {
        public final ArrayList<ChipIfaceCombinationLimit> limits = new ArrayList<>();

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject != null && otherObject.getClass() == ChipIfaceCombination.class && HidlSupport.deepEquals(this.limits, ((ChipIfaceCombination) otherObject).limits)) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.limits))});
        }

        public final String toString() {
            return "{" + ".limits = " + this.limits + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(16), 0);
        }

        public static final ArrayList<ChipIfaceCombination> readVectorFromParcel(HwParcel parcel) {
            ArrayList<ChipIfaceCombination> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                ChipIfaceCombination _hidl_vec_element = new ChipIfaceCombination();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 16));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 0 + 8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), _hidl_offset + 0 + 0, true);
            this.limits.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                ChipIfaceCombinationLimit _hidl_vec_element = new ChipIfaceCombinationLimit();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
                this.limits.add(_hidl_vec_element);
            }
            HwParcel hwParcel = parcel;
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(16);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<ChipIfaceCombination> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 16);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 16));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            int _hidl_vec_size = this.limits.size();
            _hidl_blob.putInt32(_hidl_offset + 0 + 8, _hidl_vec_size);
            int _hidl_index_0 = 0;
            _hidl_blob.putBool(_hidl_offset + 0 + 12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
            while (true) {
                int _hidl_index_02 = _hidl_index_0;
                if (_hidl_index_02 < _hidl_vec_size) {
                    this.limits.get(_hidl_index_02).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_02 * 24));
                    _hidl_index_0 = _hidl_index_02 + 1;
                } else {
                    _hidl_blob.putBlob(_hidl_offset + 0 + 0, childBlob);
                    return;
                }
            }
        }
    }

    public static final class ChipIfaceCombinationLimit {
        public int maxIfaces;
        public final ArrayList<Integer> types = new ArrayList<>();

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != ChipIfaceCombinationLimit.class) {
                return false;
            }
            ChipIfaceCombinationLimit other = (ChipIfaceCombinationLimit) otherObject;
            if (HidlSupport.deepEquals(this.types, other.types) && this.maxIfaces == other.maxIfaces) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.types)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxIfaces)))});
        }

        public final String toString() {
            return "{" + ".types = " + this.types + ", .maxIfaces = " + this.maxIfaces + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
        }

        public static final ArrayList<ChipIfaceCombinationLimit> readVectorFromParcel(HwParcel parcel) {
            ArrayList<ChipIfaceCombinationLimit> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                ChipIfaceCombinationLimit _hidl_vec_element = new ChipIfaceCombinationLimit();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            HwBlob hwBlob = _hidl_blob;
            int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 0 + 8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), _hidl_offset + 0 + 0, true);
            this.types.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                this.types.add(Integer.valueOf(childBlob.getInt32((long) (_hidl_index_0 * 4))));
            }
            this.maxIfaces = hwBlob.getInt32(_hidl_offset + 16);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(24);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<ChipIfaceCombinationLimit> _hidl_vec) {
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
            int _hidl_vec_size = this.types.size();
            _hidl_blob.putInt32(_hidl_offset + 0 + 8, _hidl_vec_size);
            int _hidl_index_0 = 0;
            _hidl_blob.putBool(_hidl_offset + 0 + 12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 4);
            while (true) {
                int _hidl_index_02 = _hidl_index_0;
                if (_hidl_index_02 < _hidl_vec_size) {
                    childBlob.putInt32((long) (_hidl_index_02 * 4), this.types.get(_hidl_index_02).intValue());
                    _hidl_index_0 = _hidl_index_02 + 1;
                } else {
                    _hidl_blob.putBlob(_hidl_offset + 0 + 0, childBlob);
                    _hidl_blob.putInt32(16 + _hidl_offset, this.maxIfaces);
                    return;
                }
            }
        }
    }

    public static final class ChipMode {
        public final ArrayList<ChipIfaceCombination> availableCombinations = new ArrayList<>();
        public int id;

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != ChipMode.class) {
                return false;
            }
            ChipMode other = (ChipMode) otherObject;
            if (this.id == other.id && HidlSupport.deepEquals(this.availableCombinations, other.availableCombinations)) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.id))), Integer.valueOf(HidlSupport.deepHashCode(this.availableCombinations))});
        }

        public final String toString() {
            return "{" + ".id = " + this.id + ", .availableCombinations = " + this.availableCombinations + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
        }

        public static final ArrayList<ChipMode> readVectorFromParcel(HwParcel parcel) {
            ArrayList<ChipMode> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                ChipMode _hidl_vec_element = new ChipMode();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            HwBlob hwBlob = _hidl_blob;
            this.id = hwBlob.getInt32(_hidl_offset + 0);
            int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 8 + 8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
            this.availableCombinations.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                ChipIfaceCombination _hidl_vec_element = new ChipIfaceCombination();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 16));
                this.availableCombinations.add(_hidl_vec_element);
            }
            HwParcel hwParcel = parcel;
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(24);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<ChipMode> _hidl_vec) {
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
            _hidl_blob.putInt32(_hidl_offset + 0, this.id);
            int _hidl_vec_size = this.availableCombinations.size();
            _hidl_blob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
            int _hidl_index_0 = 0;
            _hidl_blob.putBool(_hidl_offset + 8 + 12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 16);
            while (true) {
                int _hidl_index_02 = _hidl_index_0;
                if (_hidl_index_02 < _hidl_vec_size) {
                    this.availableCombinations.get(_hidl_index_02).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_02 * 16));
                    _hidl_index_0 = _hidl_index_02 + 1;
                } else {
                    _hidl_blob.putBlob(8 + _hidl_offset + 0, childBlob);
                    return;
                }
            }
        }
    }

    public static final class Proxy implements IWifiChip {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of android.hardware.wifi@1.0::IWifiChip]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        public void getId(getIdCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public WifiStatus registerEventCallback(IWifiChipEventCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCapabilities(getCapabilitiesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getAvailableModes(getAvailableModesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, ChipMode.readVectorFromParcel(_hidl_reply));
            } finally {
                _hidl_reply.release();
            }
        }

        public WifiStatus configureChip(int modeId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeInt32(modeId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public void getMode(getModeCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public void requestChipDebugInfo(requestChipDebugInfoCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                ChipDebugInfo _hidl_out_chipDebugInfo = new ChipDebugInfo();
                _hidl_out_chipDebugInfo.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_chipDebugInfo);
            } finally {
                _hidl_reply.release();
            }
        }

        public void requestDriverDebugDump(requestDriverDebugDumpCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        public void requestFirmwareDebugDump(requestFirmwareDebugDumpCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        public void createApIface(createApIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiApIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        public void getApIfaceNames(getApIfaceNamesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readStringVector());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getApIface(String ifname, getApIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiApIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        public WifiStatus removeApIface(String ifname) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public void createNanIface(createNanIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiNanIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        public void getNanIfaceNames(getNanIfaceNamesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readStringVector());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getNanIface(String ifname, getNanIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiNanIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        public WifiStatus removeNanIface(String ifname) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public void createP2pIface(createP2pIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiP2pIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        public void getP2pIfaceNames(getP2pIfaceNamesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readStringVector());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getP2pIface(String ifname, getP2pIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiP2pIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        public WifiStatus removeP2pIface(String ifname) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public void createStaIface(createStaIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiStaIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        public void getStaIfaceNames(getStaIfaceNamesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readStringVector());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getStaIface(String ifname, getStaIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiStaIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        public WifiStatus removeStaIface(String ifname) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public void createRttController(IWifiIface boundIface, createRttControllerCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeStrongBinder(boundIface == null ? null : boundIface.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiRttController.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDebugRingBuffersStatus(getDebugRingBuffersStatusCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, WifiDebugRingBufferStatus.readVectorFromParcel(_hidl_reply));
            } finally {
                _hidl_reply.release();
            }
        }

        public WifiStatus startLoggingToDebugRingBuffer(String ringName, int verboseLevel, int maxIntervalInSec, int minDataSizeInBytes) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeString(ringName);
            _hidl_request.writeInt32(verboseLevel);
            _hidl_request.writeInt32(maxIntervalInSec);
            _hidl_request.writeInt32(minDataSizeInBytes);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public WifiStatus forceDumpToDebugRingBuffer(String ringName) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeString(ringName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(29, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public WifiStatus stopLoggingToDebugRingBuffer() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(30, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDebugHostWakeReasonStats(getDebugHostWakeReasonStatsCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(31, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                WifiDebugHostWakeReasonStats _hidl_out_stats = new WifiDebugHostWakeReasonStats();
                _hidl_out_stats.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_stats);
            } finally {
                _hidl_reply.release();
            }
        }

        public WifiStatus enableDebugErrorAlerts(boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(32, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

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

        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                int _hidl_index_0 = 0;
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList<>();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                while (true) {
                    int _hidl_index_02 = _hidl_index_0;
                    if (_hidl_index_02 >= _hidl_vec_size) {
                        return _hidl_out_hashchain;
                    }
                    byte[] _hidl_vec_element = new byte[32];
                    childBlob.copyToInt8Array((long) (_hidl_index_02 * 32), _hidl_vec_element, 32);
                    _hidl_out_hashchain.add(_hidl_vec_element);
                    _hidl_index_0 = _hidl_index_02 + 1;
                }
            } finally {
                _hidl_reply.release();
            }
        }

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

        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

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

        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IWifiChip {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(new String[]{IWifiChip.kInterfaceName, IBase.kInterfaceName}));
        }

        public final String interfaceDescriptor() {
            return IWifiChip.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[][]{new byte[]{-13, -18, -52, 72, -99, -21, 76, 116, -119, 47, 89, -21, 122, -37, 118, -112, 99, -67, 92, 53, 74, -63, 50, -74, 38, -91, -12, 43, 54, 61, 54, -68}, new byte[]{-67, -38, -74, 24, 77, 122, 52, 109, -90, -96, 125, -64, -126, -116, -15, -102, 105, 111, 76, -86, 54, 17, -59, 31, 46, 20, 86, 90, 20, -76, 15, -39}}));
        }

        public final void setHALInstrumentation() {
        }

        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        public final void ping() {
        }

        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IWifiChip.kInterfaceName.equals(descriptor)) {
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
            int _hidl_index_0 = 0;
            boolean _hidl_is_oneway = true;
            switch (_hidl_code) {
                case 1:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    getId(new getIdCallback() {
                        public void onValues(WifiStatus status, int id) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(id);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 2:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status = registerEventCallback(IWifiChipEventCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 3:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    getCapabilities(new getCapabilitiesCallback() {
                        public void onValues(WifiStatus status, int capabilities) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(capabilities);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 4:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    getAvailableModes(new getAvailableModesCallback() {
                        public void onValues(WifiStatus status, ArrayList<ChipMode> modes) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            ChipMode.writeVectorToParcel(_hidl_reply, modes);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 5:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status2 = configureChip(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status2.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 6:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    getMode(new getModeCallback() {
                        public void onValues(WifiStatus status, int modeId) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(modeId);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 7:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    requestChipDebugInfo(new requestChipDebugInfoCallback() {
                        public void onValues(WifiStatus status, ChipDebugInfo chipDebugInfo) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            chipDebugInfo.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 8:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    requestDriverDebugDump(new requestDriverDebugDumpCallback() {
                        public void onValues(WifiStatus status, ArrayList<Byte> blob) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt8Vector(blob);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 9:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    requestFirmwareDebugDump(new requestFirmwareDebugDumpCallback() {
                        public void onValues(WifiStatus status, ArrayList<Byte> blob) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt8Vector(blob);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 10:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    createApIface(new createApIfaceCallback() {
                        public void onValues(WifiStatus status, IWifiApIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 11:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    getApIfaceNames(new getApIfaceNamesCallback() {
                        public void onValues(WifiStatus status, ArrayList<String> ifnames) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStringVector(ifnames);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 12:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    getApIface(_hidl_request.readString(), new getApIfaceCallback() {
                        public void onValues(WifiStatus status, IWifiApIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 13:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status3 = removeApIface(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status3.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 14:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    createNanIface(new createNanIfaceCallback() {
                        public void onValues(WifiStatus status, IWifiNanIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 15:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    getNanIfaceNames(new getNanIfaceNamesCallback() {
                        public void onValues(WifiStatus status, ArrayList<String> ifnames) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStringVector(ifnames);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 16:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    getNanIface(_hidl_request.readString(), new getNanIfaceCallback() {
                        public void onValues(WifiStatus status, IWifiNanIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 17:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status4 = removeNanIface(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status4.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 18:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    createP2pIface(new createP2pIfaceCallback() {
                        public void onValues(WifiStatus status, IWifiP2pIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 19:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    getP2pIfaceNames(new getP2pIfaceNamesCallback() {
                        public void onValues(WifiStatus status, ArrayList<String> ifnames) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStringVector(ifnames);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 20:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    getP2pIface(_hidl_request.readString(), new getP2pIfaceCallback() {
                        public void onValues(WifiStatus status, IWifiP2pIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case ISupplicantStaIfaceCallback.ReasonCode.UNSUPPORTED_RSN_IE_VERSION /*21*/:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status5 = removeP2pIface(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status5.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 22:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    createStaIface(new createStaIfaceCallback() {
                        public void onValues(WifiStatus status, IWifiStaIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 23:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    getStaIfaceNames(new getStaIfaceNamesCallback() {
                        public void onValues(WifiStatus status, ArrayList<String> ifnames) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStringVector(ifnames);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 24:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    getStaIface(_hidl_request.readString(), new getStaIfaceCallback() {
                        public void onValues(WifiStatus status, IWifiStaIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 25:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status6 = removeStaIface(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status6.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.ReasonCode.TDLS_TEARDOWN_UNSPECIFIED /*26*/:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    createRttController(IWifiIface.asInterface(_hidl_request.readStrongBinder()), new createRttControllerCallback() {
                        public void onValues(WifiStatus status, IWifiRttController rtt) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(rtt == null ? null : rtt.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 27:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    getDebugRingBuffersStatus(new getDebugRingBuffersStatusCallback() {
                        public void onValues(WifiStatus status, ArrayList<WifiDebugRingBufferStatus> ringBuffers) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            WifiDebugRingBufferStatus.writeVectorToParcel(_hidl_reply, ringBuffers);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 28:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status7 = startLoggingToDebugRingBuffer(_hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status7.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 29:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status8 = forceDumpToDebugRingBuffer(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status8.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 30:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status9 = stopLoggingToDebugRingBuffer();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status9.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 31:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    getDebugHostWakeReasonStats(new getDebugHostWakeReasonStatsCallback() {
                        public void onValues(WifiStatus status, WifiDebugHostWakeReasonStats stats) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            stats.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 32:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status10 = enableDebugErrorAlerts(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status10.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                default:
                    switch (_hidl_code) {
                        case 256067662:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
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
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 256136003:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
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
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
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
                            while (_hidl_index_0 < _hidl_vec_size) {
                                childBlob.putInt8Array((long) (_hidl_index_0 * 32), _hidl_out_hashchain.get(_hidl_index_0));
                                _hidl_index_0++;
                            }
                            _hidl_blob.putBlob(0, childBlob);
                            _hidl_reply.writeBuffer(_hidl_blob);
                            _hidl_reply.send();
                            return;
                        case 256462420:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 1) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            setHALInstrumentation();
                            return;
                        case 256660548:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 0) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        case 256921159:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
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
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
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
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 1) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            notifySyspropsChanged();
                            return;
                        case 257250372:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 0) {
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

    @FunctionalInterface
    public interface createApIfaceCallback {
        void onValues(WifiStatus wifiStatus, IWifiApIface iWifiApIface);
    }

    @FunctionalInterface
    public interface createNanIfaceCallback {
        void onValues(WifiStatus wifiStatus, IWifiNanIface iWifiNanIface);
    }

    @FunctionalInterface
    public interface createP2pIfaceCallback {
        void onValues(WifiStatus wifiStatus, IWifiP2pIface iWifiP2pIface);
    }

    @FunctionalInterface
    public interface createRttControllerCallback {
        void onValues(WifiStatus wifiStatus, IWifiRttController iWifiRttController);
    }

    @FunctionalInterface
    public interface createStaIfaceCallback {
        void onValues(WifiStatus wifiStatus, IWifiStaIface iWifiStaIface);
    }

    @FunctionalInterface
    public interface getApIfaceCallback {
        void onValues(WifiStatus wifiStatus, IWifiApIface iWifiApIface);
    }

    @FunctionalInterface
    public interface getApIfaceNamesCallback {
        void onValues(WifiStatus wifiStatus, ArrayList<String> arrayList);
    }

    @FunctionalInterface
    public interface getAvailableModesCallback {
        void onValues(WifiStatus wifiStatus, ArrayList<ChipMode> arrayList);
    }

    @FunctionalInterface
    public interface getCapabilitiesCallback {
        void onValues(WifiStatus wifiStatus, int i);
    }

    @FunctionalInterface
    public interface getDebugHostWakeReasonStatsCallback {
        void onValues(WifiStatus wifiStatus, WifiDebugHostWakeReasonStats wifiDebugHostWakeReasonStats);
    }

    @FunctionalInterface
    public interface getDebugRingBuffersStatusCallback {
        void onValues(WifiStatus wifiStatus, ArrayList<WifiDebugRingBufferStatus> arrayList);
    }

    @FunctionalInterface
    public interface getIdCallback {
        void onValues(WifiStatus wifiStatus, int i);
    }

    @FunctionalInterface
    public interface getModeCallback {
        void onValues(WifiStatus wifiStatus, int i);
    }

    @FunctionalInterface
    public interface getNanIfaceCallback {
        void onValues(WifiStatus wifiStatus, IWifiNanIface iWifiNanIface);
    }

    @FunctionalInterface
    public interface getNanIfaceNamesCallback {
        void onValues(WifiStatus wifiStatus, ArrayList<String> arrayList);
    }

    @FunctionalInterface
    public interface getP2pIfaceCallback {
        void onValues(WifiStatus wifiStatus, IWifiP2pIface iWifiP2pIface);
    }

    @FunctionalInterface
    public interface getP2pIfaceNamesCallback {
        void onValues(WifiStatus wifiStatus, ArrayList<String> arrayList);
    }

    @FunctionalInterface
    public interface getStaIfaceCallback {
        void onValues(WifiStatus wifiStatus, IWifiStaIface iWifiStaIface);
    }

    @FunctionalInterface
    public interface getStaIfaceNamesCallback {
        void onValues(WifiStatus wifiStatus, ArrayList<String> arrayList);
    }

    @FunctionalInterface
    public interface requestChipDebugInfoCallback {
        void onValues(WifiStatus wifiStatus, ChipDebugInfo chipDebugInfo);
    }

    @FunctionalInterface
    public interface requestDriverDebugDumpCallback {
        void onValues(WifiStatus wifiStatus, ArrayList<Byte> arrayList);
    }

    @FunctionalInterface
    public interface requestFirmwareDebugDumpCallback {
        void onValues(WifiStatus wifiStatus, ArrayList<Byte> arrayList);
    }

    IHwBinder asBinder();

    WifiStatus configureChip(int i) throws RemoteException;

    void createApIface(createApIfaceCallback createapifacecallback) throws RemoteException;

    void createNanIface(createNanIfaceCallback createnanifacecallback) throws RemoteException;

    void createP2pIface(createP2pIfaceCallback createp2pifacecallback) throws RemoteException;

    void createRttController(IWifiIface iWifiIface, createRttControllerCallback createrttcontrollercallback) throws RemoteException;

    void createStaIface(createStaIfaceCallback createstaifacecallback) throws RemoteException;

    WifiStatus enableDebugErrorAlerts(boolean z) throws RemoteException;

    WifiStatus forceDumpToDebugRingBuffer(String str) throws RemoteException;

    void getApIface(String str, getApIfaceCallback getapifacecallback) throws RemoteException;

    void getApIfaceNames(getApIfaceNamesCallback getapifacenamescallback) throws RemoteException;

    void getAvailableModes(getAvailableModesCallback getavailablemodescallback) throws RemoteException;

    void getCapabilities(getCapabilitiesCallback getcapabilitiescallback) throws RemoteException;

    void getDebugHostWakeReasonStats(getDebugHostWakeReasonStatsCallback getdebughostwakereasonstatscallback) throws RemoteException;

    DebugInfo getDebugInfo() throws RemoteException;

    void getDebugRingBuffersStatus(getDebugRingBuffersStatusCallback getdebugringbuffersstatuscallback) throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getId(getIdCallback getidcallback) throws RemoteException;

    void getMode(getModeCallback getmodecallback) throws RemoteException;

    void getNanIface(String str, getNanIfaceCallback getnanifacecallback) throws RemoteException;

    void getNanIfaceNames(getNanIfaceNamesCallback getnanifacenamescallback) throws RemoteException;

    void getP2pIface(String str, getP2pIfaceCallback getp2pifacecallback) throws RemoteException;

    void getP2pIfaceNames(getP2pIfaceNamesCallback getp2pifacenamescallback) throws RemoteException;

    void getStaIface(String str, getStaIfaceCallback getstaifacecallback) throws RemoteException;

    void getStaIfaceNames(getStaIfaceNamesCallback getstaifacenamescallback) throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void ping() throws RemoteException;

    WifiStatus registerEventCallback(IWifiChipEventCallback iWifiChipEventCallback) throws RemoteException;

    WifiStatus removeApIface(String str) throws RemoteException;

    WifiStatus removeNanIface(String str) throws RemoteException;

    WifiStatus removeP2pIface(String str) throws RemoteException;

    WifiStatus removeStaIface(String str) throws RemoteException;

    void requestChipDebugInfo(requestChipDebugInfoCallback requestchipdebuginfocallback) throws RemoteException;

    void requestDriverDebugDump(requestDriverDebugDumpCallback requestdriverdebugdumpcallback) throws RemoteException;

    void requestFirmwareDebugDump(requestFirmwareDebugDumpCallback requestfirmwaredebugdumpcallback) throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    WifiStatus startLoggingToDebugRingBuffer(String str, int i, int i2, int i3) throws RemoteException;

    WifiStatus stopLoggingToDebugRingBuffer() throws RemoteException;

    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IWifiChip asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IWifiChip)) {
            return (IWifiChip) iface;
        }
        IWifiChip proxy = new Proxy(binder);
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

    static IWifiChip castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IWifiChip getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IWifiChip getService(boolean retry) throws RemoteException {
        return getService(HalDeviceManager.HAL_INSTANCE_NAME, retry);
    }

    static IWifiChip getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IWifiChip getService() throws RemoteException {
        return getService(HalDeviceManager.HAL_INSTANCE_NAME);
    }
}
