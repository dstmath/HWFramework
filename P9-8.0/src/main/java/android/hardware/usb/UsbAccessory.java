package android.hardware.usb;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;

public class UsbAccessory implements Parcelable {
    public static final Creator<UsbAccessory> CREATOR = new Creator<UsbAccessory>() {
        public UsbAccessory createFromParcel(Parcel in) {
            return new UsbAccessory(in.readString(), in.readString(), in.readString(), in.readString(), in.readString(), in.readString());
        }

        public UsbAccessory[] newArray(int size) {
            return new UsbAccessory[size];
        }
    };
    public static final int DESCRIPTION_STRING = 2;
    public static final int MANUFACTURER_STRING = 0;
    public static final int MODEL_STRING = 1;
    public static final int SERIAL_STRING = 5;
    private static final String TAG = "UsbAccessory";
    public static final int URI_STRING = 4;
    public static final int VERSION_STRING = 3;
    private final String mDescription;
    private final String mManufacturer;
    private final String mModel;
    private final String mSerial;
    private final String mUri;
    private final String mVersion;

    public UsbAccessory(String manufacturer, String model, String description, String version, String uri, String serial) {
        this.mManufacturer = (String) Preconditions.checkNotNull(manufacturer);
        this.mModel = (String) Preconditions.checkNotNull(model);
        this.mDescription = description;
        this.mVersion = version;
        this.mUri = uri;
        this.mSerial = serial;
    }

    public UsbAccessory(String[] strings) {
        this(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]);
    }

    public String getManufacturer() {
        return this.mManufacturer;
    }

    public String getModel() {
        return this.mModel;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public String getVersion() {
        return this.mVersion;
    }

    public String getUri() {
        return this.mUri;
    }

    public String getSerial() {
        return this.mSerial;
    }

    private static boolean compare(String s1, String s2) {
        if (s1 != null) {
            return s1.equals(s2);
        }
        return s2 == null;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof UsbAccessory)) {
            return false;
        }
        UsbAccessory accessory = (UsbAccessory) obj;
        if (compare(this.mManufacturer, accessory.getManufacturer()) && compare(this.mModel, accessory.getModel()) && compare(this.mDescription, accessory.getDescription()) && compare(this.mVersion, accessory.getVersion()) && compare(this.mUri, accessory.getUri())) {
            z = compare(this.mSerial, accessory.getSerial());
        }
        return z;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = (this.mUri == null ? 0 : this.mUri.hashCode()) ^ (((this.mModel.hashCode() ^ this.mManufacturer.hashCode()) ^ (this.mDescription == null ? 0 : this.mDescription.hashCode())) ^ (this.mVersion == null ? 0 : this.mVersion.hashCode()));
        if (this.mSerial != null) {
            i = this.mSerial.hashCode();
        }
        return hashCode ^ i;
    }

    public String toString() {
        return "UsbAccessory[mManufacturer=" + this.mManufacturer + ", mModel=" + this.mModel + ", mDescription=" + this.mDescription + ", mVersion=" + this.mVersion + ", mUri=" + this.mUri + ", mSerial=" + this.mSerial + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mManufacturer);
        parcel.writeString(this.mModel);
        parcel.writeString(this.mDescription);
        parcel.writeString(this.mVersion);
        parcel.writeString(this.mUri);
        parcel.writeString(this.mSerial);
    }
}
