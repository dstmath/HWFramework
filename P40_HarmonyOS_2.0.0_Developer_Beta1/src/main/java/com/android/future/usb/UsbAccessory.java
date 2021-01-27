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
        if (s1 == null) {
            return s2 == null;
        }
        return s1.equals(s2);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof UsbAccessory)) {
            return false;
        }
        UsbAccessory accessory = (UsbAccessory) obj;
        if (!compare(this.mManufacturer, accessory.getManufacturer()) || !compare(this.mModel, accessory.getModel()) || !compare(this.mDescription, accessory.getDescription()) || !compare(this.mVersion, accessory.getVersion()) || !compare(this.mUri, accessory.getUri()) || !compare(this.mSerial, accessory.getSerial())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        String str = this.mManufacturer;
        int i = 0;
        int hashCode = str == null ? 0 : str.hashCode();
        String str2 = this.mModel;
        int hashCode2 = hashCode ^ (str2 == null ? 0 : str2.hashCode());
        String str3 = this.mDescription;
        int hashCode3 = hashCode2 ^ (str3 == null ? 0 : str3.hashCode());
        String str4 = this.mVersion;
        int hashCode4 = hashCode3 ^ (str4 == null ? 0 : str4.hashCode());
        String str5 = this.mUri;
        int hashCode5 = hashCode4 ^ (str5 == null ? 0 : str5.hashCode());
        String str6 = this.mSerial;
        if (str6 != null) {
            i = str6.hashCode();
        }
        return hashCode5 ^ i;
    }

    public String toString() {
        return "UsbAccessory[mManufacturer=" + this.mManufacturer + ", mModel=" + this.mModel + ", mDescription=" + this.mDescription + ", mVersion=" + this.mVersion + ", mUri=" + this.mUri + ", mSerial=" + this.mSerial + "]";
    }
}
