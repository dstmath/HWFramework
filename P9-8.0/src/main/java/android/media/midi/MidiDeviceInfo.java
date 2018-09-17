package android.media.midi;

import android.net.ProxyInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public final class MidiDeviceInfo implements Parcelable {
    public static final Creator<MidiDeviceInfo> CREATOR = new Creator<MidiDeviceInfo>() {
        public MidiDeviceInfo createFromParcel(Parcel in) {
            int type = in.readInt();
            int id = in.readInt();
            int inputPortCount = in.readInt();
            int outputPortCount = in.readInt();
            String[] inputPortNames = in.createStringArray();
            String[] outputPortNames = in.createStringArray();
            boolean isPrivate = in.readInt() == 1;
            Bundle basicPropertiesIgnored = in.readBundle();
            return new MidiDeviceInfo(type, id, inputPortCount, outputPortCount, inputPortNames, outputPortNames, in.readBundle(), isPrivate);
        }

        public MidiDeviceInfo[] newArray(int size) {
            return new MidiDeviceInfo[size];
        }
    };
    public static final String PROPERTY_ALSA_CARD = "alsa_card";
    public static final String PROPERTY_ALSA_DEVICE = "alsa_device";
    public static final String PROPERTY_BLUETOOTH_DEVICE = "bluetooth_device";
    public static final String PROPERTY_MANUFACTURER = "manufacturer";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_PRODUCT = "product";
    public static final String PROPERTY_SERIAL_NUMBER = "serial_number";
    public static final String PROPERTY_SERVICE_INFO = "service_info";
    public static final String PROPERTY_USB_DEVICE = "usb_device";
    public static final String PROPERTY_VERSION = "version";
    private static final String TAG = "MidiDeviceInfo";
    public static final int TYPE_BLUETOOTH = 3;
    public static final int TYPE_USB = 1;
    public static final int TYPE_VIRTUAL = 2;
    private final int mId;
    private final int mInputPortCount;
    private final String[] mInputPortNames;
    private final boolean mIsPrivate;
    private final int mOutputPortCount;
    private final String[] mOutputPortNames;
    private final Bundle mProperties;
    private final int mType;

    public static final class PortInfo {
        public static final int TYPE_INPUT = 1;
        public static final int TYPE_OUTPUT = 2;
        private final String mName;
        private final int mPortNumber;
        private final int mPortType;

        PortInfo(int type, int portNumber, String name) {
            this.mPortType = type;
            this.mPortNumber = portNumber;
            if (name == null) {
                name = ProxyInfo.LOCAL_EXCL_LIST;
            }
            this.mName = name;
        }

        public int getType() {
            return this.mPortType;
        }

        public int getPortNumber() {
            return this.mPortNumber;
        }

        public String getName() {
            return this.mName;
        }
    }

    public MidiDeviceInfo(int type, int id, int numInputPorts, int numOutputPorts, String[] inputPortNames, String[] outputPortNames, Bundle properties, boolean isPrivate) {
        this.mType = type;
        this.mId = id;
        this.mInputPortCount = numInputPorts;
        this.mOutputPortCount = numOutputPorts;
        if (inputPortNames == null) {
            this.mInputPortNames = new String[numInputPorts];
        } else {
            this.mInputPortNames = inputPortNames;
        }
        if (outputPortNames == null) {
            this.mOutputPortNames = new String[numOutputPorts];
        } else {
            this.mOutputPortNames = outputPortNames;
        }
        this.mProperties = properties;
        this.mIsPrivate = isPrivate;
    }

    public int getType() {
        return this.mType;
    }

    public int getId() {
        return this.mId;
    }

    public int getInputPortCount() {
        return this.mInputPortCount;
    }

    public int getOutputPortCount() {
        return this.mOutputPortCount;
    }

    public PortInfo[] getPorts() {
        int index;
        PortInfo[] ports = new PortInfo[(this.mInputPortCount + this.mOutputPortCount)];
        int index2 = 0;
        int i = 0;
        while (i < this.mInputPortCount) {
            index = index2 + 1;
            ports[index2] = new PortInfo(1, i, this.mInputPortNames[i]);
            i++;
            index2 = index;
        }
        i = 0;
        while (i < this.mOutputPortCount) {
            index = index2 + 1;
            ports[index2] = new PortInfo(2, i, this.mOutputPortNames[i]);
            i++;
            index2 = index;
        }
        return ports;
    }

    public Bundle getProperties() {
        return this.mProperties;
    }

    public boolean isPrivate() {
        return this.mIsPrivate;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof MidiDeviceInfo)) {
            return false;
        }
        if (((MidiDeviceInfo) o).mId == this.mId) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.mId;
    }

    public String toString() {
        this.mProperties.getString(PROPERTY_NAME);
        return "MidiDeviceInfo[mType=" + this.mType + ",mInputPortCount=" + this.mInputPortCount + ",mOutputPortCount=" + this.mOutputPortCount + ",mProperties=" + this.mProperties + ",mIsPrivate=" + this.mIsPrivate;
    }

    public int describeContents() {
        return 0;
    }

    private Bundle getBasicProperties(String[] keys) {
        Bundle basicProperties = new Bundle();
        for (String key : keys) {
            Object val = this.mProperties.get(key);
            if (val != null) {
                if (val instanceof String) {
                    basicProperties.putString(key, (String) val);
                } else if (val instanceof Integer) {
                    basicProperties.putInt(key, ((Integer) val).intValue());
                } else {
                    Log.w(TAG, "Unsupported property type: " + val.getClass().getName());
                }
            }
        }
        return basicProperties;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mType);
        parcel.writeInt(this.mId);
        parcel.writeInt(this.mInputPortCount);
        parcel.writeInt(this.mOutputPortCount);
        parcel.writeStringArray(this.mInputPortNames);
        parcel.writeStringArray(this.mOutputPortNames);
        parcel.writeInt(this.mIsPrivate ? 1 : 0);
        parcel.writeBundle(getBasicProperties(new String[]{PROPERTY_NAME, PROPERTY_MANUFACTURER, PROPERTY_PRODUCT, "version", PROPERTY_SERIAL_NUMBER, PROPERTY_ALSA_CARD, PROPERTY_ALSA_DEVICE}));
        parcel.writeBundle(this.mProperties);
    }
}
