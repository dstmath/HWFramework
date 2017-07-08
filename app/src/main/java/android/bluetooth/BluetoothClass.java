package android.bluetooth;

import android.net.wifi.AnqpInformationElement;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.renderscript.Mesh.TriangleMeshBuilder;
import android.util.Log;

public final class BluetoothClass implements Parcelable {
    public static final Creator<BluetoothClass> CREATOR = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.bluetooth.BluetoothClass.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.bluetooth.BluetoothClass.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.BluetoothClass.<clinit>():void");
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
        return PROFILE_HEADSET;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mClass);
    }

    public boolean hasService(int service) {
        return ((this.mClass & 16769024) & service) != 0;
    }

    public int getMajorDeviceClass() {
        return this.mClass & GLES20.GL_VENDOR;
    }

    public int getDeviceClass() {
        return this.mClass & 8188;
    }

    public boolean doesClassMatch(int profile) {
        boolean z = true;
        Log.i(TAG, "enableNoAutoConnect: " + profile);
        if (profile == PROFILE_A2DP) {
            if (hasService(Root.FLAG_HAS_SETTINGS)) {
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
        } else if (profile == PROFILE_A2DP_SINK) {
            if (hasService(Root.FLAG_REMOVABLE_SD)) {
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
            if (hasService(Root.FLAG_HAS_SETTINGS)) {
                return true;
            }
            switch (getDeviceClass()) {
                case GLES20.GL_FRONT /*1028*/:
                case Process.PACKAGE_INFO_GID /*1032*/:
                case Device.AUDIO_VIDEO_CAR_AUDIO /*1056*/:
                    return true;
                default:
                    return false;
            }
        } else if (profile == PROFILE_OPP) {
            if (hasService(Root.FLAG_REMOVABLE_USB)) {
                return true;
            }
            switch (getDeviceClass()) {
                case TriangleMeshBuilder.TEXTURE_0 /*256*/:
                case GLES10.GL_ADD /*260*/:
                case AnqpInformationElement.ANQP_3GPP_NETWORK /*264*/:
                case AnqpInformationElement.ANQP_DOM_NAME /*268*/:
                case AnqpInformationElement.ANQP_NEIGHBOR_REPORT /*272*/:
                case Device.COMPUTER_PALM_SIZE_PC_PDA /*276*/:
                case Device.COMPUTER_WEARABLE /*280*/:
                case Document.FLAG_VIRTUAL_DOCUMENT /*512*/:
                case GLES20.GL_GREATER /*516*/:
                case Device.PHONE_CORDLESS /*520*/:
                case Device.PHONE_SMART /*524*/:
                case Device.PHONE_MODEM_OR_GATEWAY /*528*/:
                case Device.PHONE_ISDN /*532*/:
                    return true;
                default:
                    return false;
            }
        } else if (profile == PROFILE_HID) {
            if ((getDeviceClass() & GLES20.GL_INVALID_ENUM) != GLES20.GL_INVALID_ENUM) {
                z = false;
            }
            return z;
        } else if (profile != PROFILE_PANU && profile != PROFILE_NAP) {
            return false;
        } else {
            if (hasService(Root.FLAG_ADVANCED)) {
                return true;
            }
            if ((getDeviceClass() & GLES20.GL_SRC_COLOR) != GLES20.GL_SRC_COLOR) {
                z = false;
            }
            return z;
        }
    }
}
