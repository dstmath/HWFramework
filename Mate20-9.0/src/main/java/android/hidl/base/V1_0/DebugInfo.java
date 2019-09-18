package android.hidl.base.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class DebugInfo {
    public int arch;
    public int pid;
    public long ptr;

    public static final class Architecture {
        public static final int IS_32BIT = 2;
        public static final int IS_64BIT = 1;
        public static final int UNKNOWN = 0;

        public static final String toString(int o) {
            if (o == 0) {
                return "UNKNOWN";
            }
            if (o == 1) {
                return "IS_64BIT";
            }
            if (o == 2) {
                return "IS_32BIT";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            list.add("UNKNOWN");
            if ((o & 1) == 1) {
                list.add("IS_64BIT");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("IS_32BIT");
                flipped |= 2;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != DebugInfo.class) {
            return false;
        }
        DebugInfo other = (DebugInfo) otherObject;
        if (this.pid == other.pid && this.ptr == other.ptr && this.arch == other.arch) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.pid))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.ptr))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.arch)))});
    }

    public final String toString() {
        return "{" + ".pid = " + this.pid + ", .ptr = " + this.ptr + ", .arch = " + Architecture.toString(this.arch) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<DebugInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<DebugInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            DebugInfo _hidl_vec_element = new DebugInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.pid = _hidl_blob.getInt32(0 + _hidl_offset);
        this.ptr = _hidl_blob.getInt64(8 + _hidl_offset);
        this.arch = _hidl_blob.getInt32(16 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<DebugInfo> _hidl_vec) {
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
        _hidl_blob.putInt32(0 + _hidl_offset, this.pid);
        _hidl_blob.putInt64(8 + _hidl_offset, this.ptr);
        _hidl_blob.putInt32(16 + _hidl_offset, this.arch);
    }
}
