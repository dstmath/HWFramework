package android.bluetooth;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.UUID;

public class BluetoothGattIncludedService implements Parcelable {
    public static final Creator<BluetoothGattIncludedService> CREATOR = new Creator<BluetoothGattIncludedService>() {
        public BluetoothGattIncludedService createFromParcel(Parcel in) {
            return new BluetoothGattIncludedService(in, null);
        }

        public BluetoothGattIncludedService[] newArray(int size) {
            return new BluetoothGattIncludedService[size];
        }
    };
    protected int mInstanceId;
    protected int mServiceType;
    protected UUID mUuid;

    /* synthetic */ BluetoothGattIncludedService(Parcel in, BluetoothGattIncludedService -this1) {
        this(in);
    }

    public BluetoothGattIncludedService(UUID uuid, int instanceId, int serviceType) {
        this.mUuid = uuid;
        this.mInstanceId = instanceId;
        this.mServiceType = serviceType;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(new ParcelUuid(this.mUuid), 0);
        out.writeInt(this.mInstanceId);
        out.writeInt(this.mServiceType);
    }

    private BluetoothGattIncludedService(Parcel in) {
        this.mUuid = ((ParcelUuid) in.readParcelable(null)).getUuid();
        this.mInstanceId = in.readInt();
        this.mServiceType = in.readInt();
    }

    public UUID getUuid() {
        return this.mUuid;
    }

    public int getInstanceId() {
        return this.mInstanceId;
    }

    public int getType() {
        return this.mServiceType;
    }
}
