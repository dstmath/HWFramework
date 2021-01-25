package android.net;

import android.net.apf.ApfCapabilities;
import android.os.Parcel;
import android.os.Parcelable;

public class ProvisioningConfigurationParcelable implements Parcelable {
    public static final Parcelable.Creator<ProvisioningConfigurationParcelable> CREATOR = new Parcelable.Creator<ProvisioningConfigurationParcelable>() {
        /* class android.net.ProvisioningConfigurationParcelable.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ProvisioningConfigurationParcelable createFromParcel(Parcel _aidl_source) {
            ProvisioningConfigurationParcelable _aidl_out = new ProvisioningConfigurationParcelable();
            _aidl_out.readFromParcel(_aidl_source);
            return _aidl_out;
        }

        @Override // android.os.Parcelable.Creator
        public ProvisioningConfigurationParcelable[] newArray(int _aidl_size) {
            return new ProvisioningConfigurationParcelable[_aidl_size];
        }
    };
    public ApfCapabilities apfCapabilities;
    public String displayName;
    public boolean enableIPv4;
    public boolean enableIPv6;
    public InitialConfigurationParcelable initialConfig;
    public int ipv6AddrGenMode;
    public Network network;
    public int provisioningTimeoutMs;
    public int requestedPreDhcpActionMs;
    public StaticIpConfiguration staticIpConfig;
    public boolean usingIpReachabilityMonitor;
    public boolean usingMultinetworkPolicyTracker;

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel _aidl_parcel, int _aidl_flag) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.writeInt(0);
        _aidl_parcel.writeInt(this.enableIPv4 ? 1 : 0);
        _aidl_parcel.writeInt(this.enableIPv6 ? 1 : 0);
        _aidl_parcel.writeInt(this.usingMultinetworkPolicyTracker ? 1 : 0);
        _aidl_parcel.writeInt(this.usingIpReachabilityMonitor ? 1 : 0);
        _aidl_parcel.writeInt(this.requestedPreDhcpActionMs);
        if (this.initialConfig != null) {
            _aidl_parcel.writeInt(1);
            this.initialConfig.writeToParcel(_aidl_parcel, 0);
        } else {
            _aidl_parcel.writeInt(0);
        }
        if (this.staticIpConfig != null) {
            _aidl_parcel.writeInt(1);
            this.staticIpConfig.writeToParcel(_aidl_parcel, 0);
        } else {
            _aidl_parcel.writeInt(0);
        }
        if (this.apfCapabilities != null) {
            _aidl_parcel.writeInt(1);
            this.apfCapabilities.writeToParcel(_aidl_parcel, 0);
        } else {
            _aidl_parcel.writeInt(0);
        }
        _aidl_parcel.writeInt(this.provisioningTimeoutMs);
        _aidl_parcel.writeInt(this.ipv6AddrGenMode);
        if (this.network != null) {
            _aidl_parcel.writeInt(1);
            this.network.writeToParcel(_aidl_parcel, 0);
        } else {
            _aidl_parcel.writeInt(0);
        }
        _aidl_parcel.writeString(this.displayName);
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
                boolean z = true;
                this.enableIPv4 = _aidl_parcel.readInt() != 0;
                if (_aidl_parcel.dataPosition() - _aidl_start_pos < _aidl_parcelable_size) {
                    this.enableIPv6 = _aidl_parcel.readInt() != 0;
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.usingMultinetworkPolicyTracker = _aidl_parcel.readInt() != 0;
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    if (_aidl_parcel.readInt() == 0) {
                        z = false;
                    }
                    this.usingIpReachabilityMonitor = z;
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.requestedPreDhcpActionMs = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    if (_aidl_parcel.readInt() != 0) {
                        this.initialConfig = InitialConfigurationParcelable.CREATOR.createFromParcel(_aidl_parcel);
                    } else {
                        this.initialConfig = null;
                    }
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    if (_aidl_parcel.readInt() != 0) {
                        this.staticIpConfig = (StaticIpConfiguration) StaticIpConfiguration.CREATOR.createFromParcel(_aidl_parcel);
                    } else {
                        this.staticIpConfig = null;
                    }
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    if (_aidl_parcel.readInt() != 0) {
                        this.apfCapabilities = (ApfCapabilities) ApfCapabilities.CREATOR.createFromParcel(_aidl_parcel);
                    } else {
                        this.apfCapabilities = null;
                    }
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.provisioningTimeoutMs = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.ipv6AddrGenMode = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    if (_aidl_parcel.readInt() != 0) {
                        this.network = (Network) Network.CREATOR.createFromParcel(_aidl_parcel);
                    } else {
                        this.network = null;
                    }
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.displayName = _aidl_parcel.readString();
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
