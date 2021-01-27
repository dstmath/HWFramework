package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public class NattKeepalivePacketDataParcelable implements Parcelable {
    public static final Parcelable.Creator<NattKeepalivePacketDataParcelable> CREATOR = new Parcelable.Creator<NattKeepalivePacketDataParcelable>() {
        /* class android.net.NattKeepalivePacketDataParcelable.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NattKeepalivePacketDataParcelable createFromParcel(Parcel _aidl_source) {
            NattKeepalivePacketDataParcelable _aidl_out = new NattKeepalivePacketDataParcelable();
            _aidl_out.readFromParcel(_aidl_source);
            return _aidl_out;
        }

        @Override // android.os.Parcelable.Creator
        public NattKeepalivePacketDataParcelable[] newArray(int _aidl_size) {
            return new NattKeepalivePacketDataParcelable[_aidl_size];
        }
    };
    public byte[] dstAddress;
    public int dstPort;
    public byte[] srcAddress;
    public int srcPort;

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel _aidl_parcel, int _aidl_flag) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.writeInt(0);
        _aidl_parcel.writeByteArray(this.srcAddress);
        _aidl_parcel.writeInt(this.srcPort);
        _aidl_parcel.writeByteArray(this.dstAddress);
        _aidl_parcel.writeInt(this.dstPort);
        int _aidl_end_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.setDataPosition(_aidl_start_pos);
        _aidl_parcel.writeInt(_aidl_end_pos - _aidl_start_pos);
        _aidl_parcel.setDataPosition(_aidl_end_pos);
    }

    public final void readFromParcel(Parcel _aidl_parcel) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        int _aidl_parcelable_size = _aidl_parcel.readInt();
        if (_aidl_parcelable_size >= 0) {
            try {
                this.srcAddress = _aidl_parcel.createByteArray();
                if (_aidl_parcel.dataPosition() - _aidl_start_pos < _aidl_parcelable_size) {
                    this.srcPort = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.dstAddress = _aidl_parcel.createByteArray();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.dstPort = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                    } else {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                    }
                }
            } finally {
                _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
            }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
