package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public class InitialConfigurationParcelable implements Parcelable {
    public static final Parcelable.Creator<InitialConfigurationParcelable> CREATOR = new Parcelable.Creator<InitialConfigurationParcelable>() {
        /* class android.net.InitialConfigurationParcelable.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public InitialConfigurationParcelable createFromParcel(Parcel _aidl_source) {
            InitialConfigurationParcelable _aidl_out = new InitialConfigurationParcelable();
            _aidl_out.readFromParcel(_aidl_source);
            return _aidl_out;
        }

        @Override // android.os.Parcelable.Creator
        public InitialConfigurationParcelable[] newArray(int _aidl_size) {
            return new InitialConfigurationParcelable[_aidl_size];
        }
    };
    public IpPrefix[] directlyConnectedRoutes;
    public String[] dnsServers;
    public String gateway;
    public LinkAddress[] ipAddresses;

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel _aidl_parcel, int _aidl_flag) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.writeInt(0);
        _aidl_parcel.writeTypedArray(this.ipAddresses, 0);
        _aidl_parcel.writeTypedArray(this.directlyConnectedRoutes, 0);
        _aidl_parcel.writeStringArray(this.dnsServers);
        _aidl_parcel.writeString(this.gateway);
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
                this.ipAddresses = (LinkAddress[]) _aidl_parcel.createTypedArray(LinkAddress.CREATOR);
                if (_aidl_parcel.dataPosition() - _aidl_start_pos < _aidl_parcelable_size) {
                    this.directlyConnectedRoutes = (IpPrefix[]) _aidl_parcel.createTypedArray(IpPrefix.CREATOR);
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.dnsServers = _aidl_parcel.createStringArray();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.gateway = _aidl_parcel.readString();
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
