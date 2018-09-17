package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class IpAddrInfo {
    public final RilIpv4AddrInfo ipv4AddrInfo = new RilIpv4AddrInfo();
    public final RilIpv6AddrInfo ipv6AddrInfo = new RilIpv6AddrInfo();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != IpAddrInfo.class) {
            return false;
        }
        IpAddrInfo other = (IpAddrInfo) otherObject;
        return HidlSupport.deepEquals(this.ipv4AddrInfo, other.ipv4AddrInfo) && HidlSupport.deepEquals(this.ipv6AddrInfo, other.ipv6AddrInfo);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.ipv4AddrInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.ipv6AddrInfo))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".ipv4AddrInfo = ");
        builder.append(this.ipv4AddrInfo);
        builder.append(", .ipv6AddrInfo = ");
        builder.append(this.ipv6AddrInfo);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(120), 0);
    }

    public static final ArrayList<IpAddrInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<IpAddrInfo> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 120), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            IpAddrInfo _hidl_vec_element = new IpAddrInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 120));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.ipv4AddrInfo.readEmbeddedFromParcel(parcel, _hidl_blob, 0 + _hidl_offset);
        this.ipv6AddrInfo.readEmbeddedFromParcel(parcel, _hidl_blob, 24 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(120);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<IpAddrInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 120);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((IpAddrInfo) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 120));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        this.ipv4AddrInfo.writeEmbeddedToBlob(_hidl_blob, 0 + _hidl_offset);
        this.ipv6AddrInfo.writeEmbeddedToBlob(_hidl_blob, 24 + _hidl_offset);
    }
}
