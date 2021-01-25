package android.hardware.radio.V1_4;

import android.app.slice.Slice;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RadioFrequencyInfo {
    private byte hidl_d = 0;
    private Object hidl_o = null;

    public static final class hidl_discriminator {
        public static final byte channelNumber = 1;
        public static final byte range = 0;

        public static final String getName(byte value) {
            if (value == 0) {
                return Slice.SUBTYPE_RANGE;
            }
            if (value != 1) {
                return "Unknown";
            }
            return "channelNumber";
        }

        private hidl_discriminator() {
        }
    }

    public void range(int range) {
        this.hidl_d = 0;
        this.hidl_o = Integer.valueOf(range);
    }

    public int range() {
        if (this.hidl_d != 0) {
            Object obj = this.hidl_o;
            String className = obj != null ? obj.getClass().getName() : "null";
            throw new IllegalStateException("Read access to inactive union components is disallowed. Discriminator value is " + ((int) this.hidl_d) + " (corresponding to " + hidl_discriminator.getName(this.hidl_d) + "), and hidl_o is of type " + className + ".");
        }
        Object obj2 = this.hidl_o;
        if (obj2 == null || Integer.class.isInstance(obj2)) {
            return ((Integer) this.hidl_o).intValue();
        }
        throw new Error("Union is in a corrupted state.");
    }

    public void channelNumber(int channelNumber) {
        this.hidl_d = 1;
        this.hidl_o = Integer.valueOf(channelNumber);
    }

    public int channelNumber() {
        if (this.hidl_d != 1) {
            Object obj = this.hidl_o;
            String className = obj != null ? obj.getClass().getName() : "null";
            throw new IllegalStateException("Read access to inactive union components is disallowed. Discriminator value is " + ((int) this.hidl_d) + " (corresponding to " + hidl_discriminator.getName(this.hidl_d) + "), and hidl_o is of type " + className + ".");
        }
        Object obj2 = this.hidl_o;
        if (obj2 == null || Integer.class.isInstance(obj2)) {
            return ((Integer) this.hidl_o).intValue();
        }
        throw new Error("Union is in a corrupted state.");
    }

    public byte getDiscriminator() {
        return this.hidl_d;
    }

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RadioFrequencyInfo.class) {
            return false;
        }
        RadioFrequencyInfo other = (RadioFrequencyInfo) otherObject;
        if (this.hidl_d == other.hidl_d && HidlSupport.deepEquals(this.hidl_o, other.hidl_o)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.hidl_o)), Integer.valueOf(Objects.hashCode(Byte.valueOf(this.hidl_d))));
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        byte b = this.hidl_d;
        if (b == 0) {
            builder.append(".range = ");
            builder.append(FrequencyRange.toString(range()));
        } else if (b == 1) {
            builder.append(".channelNumber = ");
            builder.append(channelNumber());
        } else {
            throw new Error("Unknown union discriminator (value: " + ((int) this.hidl_d) + ").");
        }
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(8), 0);
    }

    public static final ArrayList<RadioFrequencyInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RadioFrequencyInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 8), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RadioFrequencyInfo _hidl_vec_element = new RadioFrequencyInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 8));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.hidl_d = _hidl_blob.getInt8(0 + _hidl_offset);
        byte b = this.hidl_d;
        if (b == 0) {
            this.hidl_o = Integer.valueOf(_hidl_blob.getInt32(4 + _hidl_offset));
        } else if (b == 1) {
            this.hidl_o = Integer.valueOf(_hidl_blob.getInt32(4 + _hidl_offset));
        } else {
            throw new IllegalStateException("Unknown union discriminator (value: " + ((int) this.hidl_d) + ").");
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(8);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RadioFrequencyInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 8);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 8));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt8(0 + _hidl_offset, this.hidl_d);
        byte b = this.hidl_d;
        if (b == 0) {
            _hidl_blob.putInt32(4 + _hidl_offset, range());
        } else if (b == 1) {
            _hidl_blob.putInt32(4 + _hidl_offset, channelNumber());
        } else {
            throw new Error("Unknown union discriminator (value: " + ((int) this.hidl_d) + ").");
        }
    }
}
