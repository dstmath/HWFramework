package com.android.future.usb;

public class UsbAccessory {
    private final String mDescription;
    private final String mManufacturer;
    private final String mModel;
    private final String mSerial;
    private final String mUri;
    private final String mVersion;

    UsbAccessory(android.hardware.usb.UsbAccessory accessory) {
        this.mManufacturer = accessory.getManufacturer();
        this.mModel = accessory.getModel();
        this.mDescription = accessory.getDescription();
        this.mVersion = accessory.getVersion();
        this.mUri = accessory.getUri();
        this.mSerial = accessory.getSerial();
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
        int hashCode = (this.mUri == null ? 0 : this.mUri.hashCode()) ^ ((((this.mModel == null ? 0 : this.mModel.hashCode()) ^ (this.mManufacturer == null ? 0 : this.mManufacturer.hashCode())) ^ (this.mDescription == null ? 0 : this.mDescription.hashCode())) ^ (this.mVersion == null ? 0 : this.mVersion.hashCode()));
        if (this.mSerial != null) {
            i = this.mSerial.hashCode();
        }
        return hashCode ^ i;
    }

    public String toString() {
        return "UsbAccessory[mManufacturer=" + this.mManufacturer + ", mModel=" + this.mModel + ", mDescription=" + this.mDescription + ", mVersion=" + this.mVersion + ", mUri=" + this.mUri + ", mSerial=" + this.mSerial + "]";
    }
}
