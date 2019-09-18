package android.hardware.usb;

import android.media.midi.MidiDeviceInfo;
import com.android.internal.util.dump.DualDumpOutputStream;
import java.io.IOException;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class AccessoryFilter {
    public final String mManufacturer;
    public final String mModel;
    public final String mVersion;

    public AccessoryFilter(String manufacturer, String model, String version) {
        this.mManufacturer = manufacturer;
        this.mModel = model;
        this.mVersion = version;
    }

    public AccessoryFilter(UsbAccessory accessory) {
        this.mManufacturer = accessory.getManufacturer();
        this.mModel = accessory.getModel();
        this.mVersion = accessory.getVersion();
    }

    public static AccessoryFilter read(XmlPullParser parser) throws XmlPullParserException, IOException {
        String manufacturer = null;
        String model = null;
        String version = null;
        int count = parser.getAttributeCount();
        for (int i = 0; i < count; i++) {
            String name = parser.getAttributeName(i);
            String value = parser.getAttributeValue(i);
            if (MidiDeviceInfo.PROPERTY_MANUFACTURER.equals(name)) {
                manufacturer = value;
            } else if ("model".equals(name)) {
                model = value;
            } else if ("version".equals(name)) {
                version = value;
            }
        }
        return new AccessoryFilter(manufacturer, model, version);
    }

    public void write(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, "usb-accessory");
        if (this.mManufacturer != null) {
            serializer.attribute(null, MidiDeviceInfo.PROPERTY_MANUFACTURER, this.mManufacturer);
        }
        if (this.mModel != null) {
            serializer.attribute(null, "model", this.mModel);
        }
        if (this.mVersion != null) {
            serializer.attribute(null, "version", this.mVersion);
        }
        serializer.endTag(null, "usb-accessory");
    }

    public boolean matches(UsbAccessory acc) {
        boolean z = false;
        if (this.mManufacturer != null && !acc.getManufacturer().equals(this.mManufacturer)) {
            return false;
        }
        if (this.mModel != null && !acc.getModel().equals(this.mModel)) {
            return false;
        }
        if (this.mVersion == null || acc.getVersion().equals(this.mVersion)) {
            z = true;
        }
        return z;
    }

    public boolean contains(AccessoryFilter accessory) {
        boolean z = false;
        if (this.mManufacturer != null && !Objects.equals(accessory.mManufacturer, this.mManufacturer)) {
            return false;
        }
        if (this.mModel != null && !Objects.equals(accessory.mModel, this.mModel)) {
            return false;
        }
        if (this.mVersion == null || Objects.equals(accessory.mVersion, this.mVersion)) {
            z = true;
        }
        return z;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (this.mManufacturer == null || this.mModel == null || this.mVersion == null) {
            return false;
        }
        if (obj instanceof AccessoryFilter) {
            AccessoryFilter filter = (AccessoryFilter) obj;
            if (this.mManufacturer.equals(filter.mManufacturer) && this.mModel.equals(filter.mModel) && this.mVersion.equals(filter.mVersion)) {
                z = true;
            }
            return z;
        } else if (!(obj instanceof UsbAccessory)) {
            return false;
        } else {
            UsbAccessory accessory = (UsbAccessory) obj;
            if (this.mManufacturer.equals(accessory.getManufacturer()) && this.mModel.equals(accessory.getModel()) && this.mVersion.equals(accessory.getVersion())) {
                z = true;
            }
            return z;
        }
    }

    public int hashCode() {
        int i;
        int i2 = 0;
        if (this.mManufacturer == null) {
            i = 0;
        } else {
            i = this.mManufacturer.hashCode();
        }
        int hashCode = i ^ (this.mModel == null ? 0 : this.mModel.hashCode());
        if (this.mVersion != null) {
            i2 = this.mVersion.hashCode();
        }
        return hashCode ^ i2;
    }

    public String toString() {
        return "AccessoryFilter[mManufacturer=\"" + this.mManufacturer + "\", mModel=\"" + this.mModel + "\", mVersion=\"" + this.mVersion + "\"]";
    }

    public void dump(DualDumpOutputStream dump, String idName, long id) {
        long token = dump.start(idName, id);
        dump.write(MidiDeviceInfo.PROPERTY_MANUFACTURER, 1138166333441L, this.mManufacturer);
        dump.write("model", 1138166333442L, this.mModel);
        dump.write("version", 1138166333443L, this.mVersion);
        dump.end(token);
    }
}
