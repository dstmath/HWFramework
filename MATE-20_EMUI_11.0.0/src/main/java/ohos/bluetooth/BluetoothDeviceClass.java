package ohos.bluetooth;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class BluetoothDeviceClass implements Sequenceable {
    private static final int DEVICE_MASK = 8188;
    private static final int MAJOR_CLASS_MASK = 7936;
    private static final int SERVICE_MASK = 16769024;
    private int mClassValue;

    public static class MajorClass {
        public static final int AUDIO_VIDEO = 1024;
        public static final int COMPUTER = 256;
        public static final int HEALTH = 2304;
        public static final int IMAGING = 1536;
        public static final int MISCELLANEOUS = 0;
        public static final int NETWORK_ACCESS = 768;
        public static final int PERIPHERAL = 1280;
        public static final int PHONE = 512;
        public static final int TOY = 2048;
        public static final int UNCATEGORIZED = 7936;
        public static final int WEARABLE = 1792;
    }

    public static class MajorMinorClass {
        public static final int AUDIO_VIDEO_CAMCORDER = 1076;
        public static final int AUDIO_VIDEO_CAR_AUDIO = 1056;
        public static final int AUDIO_VIDEO_HANDSFREE_DEVICE = 1032;
        public static final int AUDIO_VIDEO_HEADPHONES = 1048;
        public static final int AUDIO_VIDEO_HIFI_AUDIO_DEVICE = 1064;
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
        public static final int COMPUTER_DESKTOP_WORKSTATION = 260;
        public static final int COMPUTER_HANDHELD_PC_PDA = 272;
        public static final int COMPUTER_LAPTOP = 268;
        public static final int COMPUTER_PALM_SIZE_PC_PDA = 276;
        public static final int COMPUTER_SERVER_CLASS = 264;
        public static final int COMPUTER_TABLET = 284;
        public static final int COMPUTER_UNCATEGORIZED = 256;
        public static final int COMPUTER_WEARABLE = 280;
        public static final int HEALTH_ANKLE_PROSTHESIS = 2356;
        public static final int HEALTH_BLOOD_PRESSURE_MONITOR = 2308;
        public static final int HEALTH_BODY_COMPOSITION_ANALYZER = 2340;
        public static final int HEALTH_GENERIC_HEALTH_MANAGER = 2360;
        public static final int HEALTH_GLUCOSE_METER = 2320;
        public static final int HEALTH_HEALTH_DATA_DISPLAY = 2332;
        public static final int HEALTH_HEART_PULSE_RATE_MONITOR = 2328;
        public static final int HEALTH_KNEE_PROSTHESIS = 2352;
        public static final int HEALTH_MEDICATION_MONITOR = 2348;
        public static final int HEALTH_PEAK_FLOW_MONITOR = 2344;
        public static final int HEALTH_PERSONAL_MOBILITY_DEVICE = 2364;
        public static final int HEALTH_PULSE_OXIMETER = 2324;
        public static final int HEALTH_STEP_COUNTER = 2336;
        public static final int HEALTH_THERMOMETER = 2312;
        public static final int HEALTH_UNDEFINED = 2304;
        public static final int HEALTH_WEIGHING_SCALE = 2316;
        public static final int IMAGING_CAMERA = 1568;
        public static final int IMAGING_DISPLAY = 1552;
        public static final int IMAGING_PRINTER = 1664;
        public static final int IMAGING_SCANNER = 1600;
        public static final int PERIPHERAL_COMBO_KEYBOARD_POINTING_DEVICE = 1472;
        public static final int PERIPHERAL_KEYBOARD = 1344;
        public static final int PERIPHERAL_NON_KEYBOARD_NON_POINTING_DEVICE = 1280;
        public static final int PERIPHERAL_POINTING_DEVICE = 1408;
        public static final int PHONE_CELLULAR = 516;
        public static final int PHONE_COMMON_ISDN_ACCESS = 532;
        public static final int PHONE_CORDLESS = 520;
        public static final int PHONE_MODEM_OR_VOICE_GATEWAY = 528;
        public static final int PHONE_SMARTPHONE = 524;
        public static final int PHONE_UNCATEGORIZED = 512;
        public static final int TOY_CONTROLLER = 2064;
        public static final int TOY_DOLL_ACTION_FIGURE = 2060;
        public static final int TOY_GAME = 2068;
        public static final int TOY_ROBOT = 2052;
        public static final int TOY_VEHICLE = 2056;
        public static final int WEARABLE_GLASSES = 1812;
        public static final int WEARABLE_HELMET = 1808;
        public static final int WEARABLE_JACKET = 1804;
        public static final int WEARABLE_PAGER = 1800;
        public static final int WEARABLE_WRIST_WATCH = 1796;
    }

    public static class Service {
        public static final int AUDIO = 2097152;
        public static final int CAPTURING = 524288;
        public static final int INFORMATION = 8388608;
        public static final int LIMITED_DISCOVERABLE_MODE = 8192;
        public static final int NETWORKING = 131072;
        public static final int OBJECT_TRANSFER = 1048576;
        public static final int POSITIONING = 65536;
        public static final int RENDERING = 262144;
        public static final int TELEPHONY = 4194304;
    }

    BluetoothDeviceClass(int i) {
        this.mClassValue = i;
    }

    public int getMajorClass() {
        return this.mClassValue & 7936;
    }

    public boolean isServiceSupported(int i) {
        return ((this.mClassValue & SERVICE_MASK) & i) != 0;
    }

    public int getMajorMinorClass() {
        return this.mClassValue & DEVICE_MASK;
    }

    public int getClassOfDevice() {
        return this.mClassValue;
    }

    public boolean isProfileSupported(int i) {
        int majorMinorClass;
        int majorMinorClass2;
        int majorMinorClass3;
        if (i == 1) {
            return isServiceSupported(262144) || (majorMinorClass = getMajorMinorClass()) == 1028 || majorMinorClass == 1032 || majorMinorClass == 1056;
        }
        if (i != 18) {
            if (i == 3) {
                return isServiceSupported(262144) || (majorMinorClass2 = getMajorMinorClass()) == 1044 || majorMinorClass2 == 1048 || majorMinorClass2 == 1056 || majorMinorClass2 == 1064;
            }
            if (i == 4) {
                return isServiceSupported(524288) || (majorMinorClass3 = getMajorMinorClass()) == 1060 || majorMinorClass3 == 1064 || majorMinorClass3 == 1068;
            }
            if (i == 14) {
                return (getMajorClass() & 1280) == 1280;
            }
            if (i != 15) {
                return false;
            }
            if (isServiceSupported(131072)) {
                return true;
            }
            return (getMajorClass() & 768) == 768;
        } else if (isServiceSupported(1048576)) {
            return true;
        } else {
            switch (getMajorMinorClass()) {
                case 256:
                case 260:
                case 264:
                case 268:
                case 272:
                case 276:
                case 280:
                case 512:
                case MajorMinorClass.PHONE_CELLULAR /* 516 */:
                case MajorMinorClass.PHONE_CORDLESS /* 520 */:
                case MajorMinorClass.PHONE_SMARTPHONE /* 524 */:
                case MajorMinorClass.PHONE_MODEM_OR_VOICE_GATEWAY /* 528 */:
                case MajorMinorClass.PHONE_COMMON_ISDN_ACCESS /* 532 */:
                    return true;
                default:
                    return false;
            }
        }
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.mClassValue);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.mClassValue = parcel.readInt();
        return true;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BluetoothDeviceClass) || this.mClassValue != ((BluetoothDeviceClass) obj).mClassValue) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.mClassValue;
    }

    public String toString() {
        return Integer.toHexString(this.mClassValue);
    }
}
