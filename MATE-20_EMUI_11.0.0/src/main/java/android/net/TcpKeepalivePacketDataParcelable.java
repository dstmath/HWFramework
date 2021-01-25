package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public class TcpKeepalivePacketDataParcelable implements Parcelable {
    public static final Parcelable.Creator<TcpKeepalivePacketDataParcelable> CREATOR = new Parcelable.Creator<TcpKeepalivePacketDataParcelable>() {
        /* class android.net.TcpKeepalivePacketDataParcelable.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TcpKeepalivePacketDataParcelable createFromParcel(Parcel _aidl_source) {
            TcpKeepalivePacketDataParcelable _aidl_out = new TcpKeepalivePacketDataParcelable();
            _aidl_out.readFromParcel(_aidl_source);
            return _aidl_out;
        }

        @Override // android.os.Parcelable.Creator
        public TcpKeepalivePacketDataParcelable[] newArray(int _aidl_size) {
            return new TcpKeepalivePacketDataParcelable[_aidl_size];
        }
    };
    public int ack;
    public byte[] dstAddress;
    public int dstPort;
    public int rcvWnd;
    public int rcvWndScale;
    public int seq;
    public byte[] srcAddress;
    public int srcPort;
    public int tos;
    public int ttl;

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel _aidl_parcel, int _aidl_flag) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.writeInt(0);
        _aidl_parcel.writeByteArray(this.srcAddress);
        _aidl_parcel.writeInt(this.srcPort);
        _aidl_parcel.writeByteArray(this.dstAddress);
        _aidl_parcel.writeInt(this.dstPort);
        _aidl_parcel.writeInt(this.seq);
        _aidl_parcel.writeInt(this.ack);
        _aidl_parcel.writeInt(this.rcvWnd);
        _aidl_parcel.writeInt(this.rcvWndScale);
        _aidl_parcel.writeInt(this.tos);
        _aidl_parcel.writeInt(this.ttl);
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
                        return;
                    }
                    this.seq = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.ack = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.rcvWnd = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.rcvWndScale = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.tos = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.ttl = _aidl_parcel.readInt();
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
