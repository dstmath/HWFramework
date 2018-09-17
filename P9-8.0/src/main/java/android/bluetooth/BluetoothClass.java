package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public final class BluetoothClass implements Parcelable {
    public static final Creator<BluetoothClass> CREATOR = new Creator<BluetoothClass>() {
        public BluetoothClass createFromParcel(Parcel in) {
            return new BluetoothClass(in.readInt());
        }

        public BluetoothClass[] newArray(int size) {
            return new BluetoothClass[size];
        }
    };
    public static final int ERROR = -16777216;
    public static final int PROFILE_A2DP = 1;
    public static final int PROFILE_A2DP_SINK = 6;
    public static final int PROFILE_HEADSET = 0;
    public static final int PROFILE_HID = 3;
    public static final int PROFILE_NAP = 5;
    public static final int PROFILE_OPP = 2;
    public static final int PROFILE_PANU = 4;
    private static final String TAG = "BluetoothClass";
    private final int mClass;

    public static class Device {
        public static final int AUDIO_VIDEO_CAMCORDER = 1076;
        public static final int AUDIO_VIDEO_CAR_AUDIO = 1056;
        public static final int AUDIO_VIDEO_HANDSFREE = 1032;
        public static final int AUDIO_VIDEO_HEADPHONES = 1048;
        public static final int AUDIO_VIDEO_HIFI_AUDIO = 1064;
        public static final int AUDIO_VIDEO_LOUDSPEAKER = 1044;
        public static final int AUDIO_VIDEO_MICROPHONE = 1040;
        public static final int AUDIO_VIDEO_PORTABLE_AUDIO = 1052;
        public static final int AUDIO_VIDEO_SET_TOP_BOX = 1060;
        public static final int AUDIO_VIDEO_UNCATEGORIZED = 1024;
        public static final int AUDIO_VIDEO_VCR = 1068;
        public static final int AUDIO_VIDEO_VIDEO_CAMERA = 1072;
        public static final int AUDIO_VIDEO_VIDEO_CONFERENCING = 1088;
        public static final int AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER = 1084;
        public static final int AUDIO_VIDEO_VIDEO_GAMING_TOY = 1096;
        public static final int AUDIO_VIDEO_VIDEO_MONITOR = 1080;
        public static final int AUDIO_VIDEO_WEARABLE_HEADSET = 1028;
        private static final int BITMASK = 8188;
        public static final int COMPUTER_DESKTOP = 260;
        public static final int COMPUTER_HANDHELD_PC_PDA = 272;
        public static final int COMPUTER_LAPTOP = 268;
        public static final int COMPUTER_PALM_SIZE_PC_PDA = 276;
        public static final int COMPUTER_SERVER = 264;
        public static final int COMPUTER_UNCATEGORIZED = 256;
        public static final int COMPUTER_WEARABLE = 280;
        public static final int HEALTH_BLOOD_PRESSURE = 2308;
        public static final int HEALTH_DATA_DISPLAY = 2332;
        public static final int HEALTH_GLUCOSE = 2320;
        public static final int HEALTH_PULSE_OXIMETER = 2324;
        public static final int HEALTH_PULSE_RATE = 2328;
        public static final int HEALTH_THERMOMETER = 2312;
        public static final int HEALTH_UNCATEGORIZED = 2304;
        public static final int HEALTH_WEIGHING = 2316;
        public static final int PERIPHERAL_KEYBOARD = 1344;
        public static final int PERIPHERAL_KEYBOARD_POINTING = 1472;
        public static final int PERIPHERAL_NON_KEYBOARD_NON_POINTING = 1280;
        public static final int PERIPHERAL_POINTING = 1408;
        public static final int PHONE_CELLULAR = 516;
        public static final int PHONE_CORDLESS = 520;
        public static final int PHONE_ISDN = 532;
        public static final int PHONE_MODEM_OR_GATEWAY = 528;
        public static final int PHONE_SMART = 524;
        public static final int PHONE_UNCATEGORIZED = 512;
        public static final int TOY_CONTROLLER = 2064;
        public static final int TOY_DOLL_ACTION_FIGURE = 2060;
        public static final int TOY_GAME = 2068;
        public static final int TOY_ROBOT = 2052;
        public static final int TOY_UNCATEGORIZED = 2048;
        public static final int TOY_VEHICLE = 2056;
        public static final int WEARABLE_GLASSES = 1812;
        public static final int WEARABLE_HELMET = 1808;
        public static final int WEARABLE_JACKET = 1804;
        public static final int WEARABLE_PAGER = 1800;
        public static final int WEARABLE_UNCATEGORIZED = 1792;
        public static final int WEARABLE_WRIST_WATCH = 1796;

        public static class Major {
            public static final int AUDIO_VIDEO = 1024;
            private static final int BITMASK = 7936;
            public static final int COMPUTER = 256;
            public static final int HEALTH = 2304;
            public static final int IMAGING = 1536;
            public static final int MISC = 0;
            public static final int NETWORKING = 768;
            public static final int PERIPHERAL = 1280;
            public static final int PHONE = 512;
            public static final int TOY = 2048;
            public static final int UNCATEGORIZED = 7936;
            public static final int WEARABLE = 1792;
        }
    }

    public static final class Service {
        public static final int AUDIO = 2097152;
        private static final int BITMASK = 16769024;
        public static final int CAPTURE = 524288;
        public static final int INFORMATION = 8388608;
        public static final int LIMITED_DISCOVERABILITY = 8192;
        public static final int NETWORKING = 131072;
        public static final int OBJECT_TRANSFER = 1048576;
        public static final int POSITIONING = 65536;
        public static final int RENDER = 262144;
        public static final int TELEPHONY = 4194304;
    }

    public BluetoothClass(int classInt) {
        this.mClass = classInt;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof BluetoothClass)) {
            return false;
        }
        if (this.mClass == ((BluetoothClass) o).mClass) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.mClass;
    }

    public String toString() {
        return Integer.toHexString(this.mClass);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mClass);
    }

    public boolean hasService(int service) {
        return ((this.mClass & 16769024) & service) != 0;
    }

    public int getMajorDeviceClass() {
        return this.mClass & 7936;
    }

    public int getDeviceClass() {
        return this.mClass & 8188;
    }

    public boolean doesClassMatch(int profile) {
        boolean z = true;
        Log.i(TAG, "enableNoAutoConnect: " + profile);
        if (profile == 1) {
            if (hasService(262144)) {
                return true;
            }
            switch (getDeviceClass()) {
                case Device.AUDIO_VIDEO_LOUDSPEAKER /*1044*/:
                case Device.AUDIO_VIDEO_HEADPHONES /*1048*/:
                case Device.AUDIO_VIDEO_CAR_AUDIO /*1056*/:
                case Device.AUDIO_VIDEO_HIFI_AUDIO /*1064*/:
                    return true;
                default:
                    return false;
            }
        } else if (profile == 6) {
            if (hasService(524288)) {
                return true;
            }
            switch (getDeviceClass()) {
                case Device.AUDIO_VIDEO_SET_TOP_BOX /*1060*/:
                case Device.AUDIO_VIDEO_HIFI_AUDIO /*1064*/:
                case Device.AUDIO_VIDEO_VCR /*1068*/:
                    return true;
                default:
                    return false;
            }
        } else if (profile == 0) {
            if (hasService(262144)) {
                return true;
            }
            switch (getDeviceClass()) {
                case 1028:
                case 1032:
                case Device.AUDIO_VIDEO_CAR_AUDIO /*1056*/:
                    return true;
                default:
                    return false;
            }
        } else if (profile == 2) {
            if (hasService(1048576)) {
                return true;
            }
            switch (getDeviceClass()) {
                case 256:
                case 260:
                case 264:
                case 268:
                case 272:
                case Device.COMPUTER_PALM_SIZE_PC_PDA /*276*/:
                case Device.COMPUTER_WEARABLE /*280*/:
                case 512:
                case 516:
                case Device.PHONE_CORDLESS /*520*/:
                case Device.PHONE_SMART /*524*/:
                case Device.PHONE_MODEM_OR_GATEWAY /*528*/:
                case Device.PHONE_ISDN /*532*/:
                    return true;
                default:
                    return false;
            }
        } else if (profile == 3) {
            if ((getDeviceClass() & 1280) != 1280) {
                z = false;
            }
            return z;
        } else if (profile != 4 && profile != 5) {
            return false;
        } else {
            if (hasService(131072)) {
                return true;
            }
            if ((getDeviceClass() & 768) != 768) {
                z = false;
            }
            return z;
        }
    }
}
