package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public class InterfaceConfigurationParcel implements Parcelable {
    public static final Parcelable.Creator<InterfaceConfigurationParcel> CREATOR = new Parcelable.Creator<InterfaceConfigurationParcel>() {
        /* class android.net.InterfaceConfigurationParcel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public InterfaceConfigurationParcel createFromParcel(Parcel _aidl_source) {
            InterfaceConfigurationParcel _aidl_out = new InterfaceConfigurationParcel();
            _aidl_out.readFromParcel(_aidl_source);
            return _aidl_out;
        }

        @Override // android.os.Parcelable.Creator
        public InterfaceConfigurationParcel[] newArray(int _aidl_size) {
            return new InterfaceConfigurationParcel[_aidl_size];
        }
    };
    public String[] flags;
    public String hwAddr;
    public String ifName;
    public String ipv4Addr;
    public int prefixLength;

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel _aidl_parcel, int _aidl_flag) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.writeInt(0);
        _aidl_parcel.writeString(this.ifName);
        _aidl_parcel.writeString(this.hwAddr);
        _aidl_parcel.writeString(this.ipv4Addr);
        _aidl_parcel.writeInt(this.prefixLength);
        _aidl_parcel.writeStringArray(this.flags);
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
                this.ifName = _aidl_parcel.readString();
                if (_aidl_parcel.dataPosition() - _aidl_start_pos < _aidl_parcelable_size) {
                    this.hwAddr = _aidl_parcel.readString();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.ipv4Addr = _aidl_parcel.readString();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.prefixLength = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.flags = _aidl_parcel.createStringArray();
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
