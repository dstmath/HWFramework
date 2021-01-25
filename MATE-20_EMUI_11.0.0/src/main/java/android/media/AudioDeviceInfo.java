package android.media;

import android.os.Build;
import android.util.SparseIntArray;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Iterator;
import java.util.Objects;
import java.util.TreeSet;

public final class AudioDeviceInfo {
    private static final SparseIntArray EXT_TO_INT_DEVICE_MAPPING = new SparseIntArray();
    private static final SparseIntArray INT_TO_EXT_DEVICE_MAPPING = new SparseIntArray();
    public static final int TYPE_AUX_LINE = 19;
    public static final int TYPE_BLUETOOTH_A2DP = 8;
    public static final int TYPE_BLUETOOTH_SCO = 7;
    public static final int TYPE_BUILTIN_EARPIECE = 1;
    public static final int TYPE_BUILTIN_MIC = 15;
    public static final int TYPE_BUILTIN_SPEAKER = 2;
    public static final int TYPE_BUS = 21;
    public static final int TYPE_DOCK = 13;
    public static final int TYPE_FM = 14;
    public static final int TYPE_FM_TUNER = 16;
    public static final int TYPE_HDMI = 9;
    public static final int TYPE_HDMI_ARC = 10;
    public static final int TYPE_HEARING_AID = 23;
    public static final int TYPE_IP = 20;
    public static final int TYPE_LINE_ANALOG = 5;
    public static final int TYPE_LINE_DIGITAL = 6;
    public static final int TYPE_MSDP = 24;
    public static final int TYPE_REMOTE_SUBMIX = 25;
    public static final int TYPE_TELEPHONY = 18;
    public static final int TYPE_TV_TUNER = 17;
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_USB_ACCESSORY = 12;
    public static final int TYPE_USB_DEVICE = 11;
    public static final int TYPE_USB_HEADSET = 22;
    public static final int TYPE_WIRED_HEADPHONES = 4;
    public static final int TYPE_WIRED_HEADSET = 3;
    private final AudioDevicePort mPort;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AudioDeviceTypeOut {
    }

    static boolean isValidAudioDeviceTypeOut(int type) {
        switch (type) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
                return true;
            case 15:
            case 16:
            case 17:
            default:
                return false;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(getPort(), ((AudioDeviceInfo) o).getPort());
    }

    public int hashCode() {
        return Objects.hash(getPort());
    }

    AudioDeviceInfo(AudioDevicePort port) {
        this.mPort = port;
    }

    public AudioDevicePort getPort() {
        return this.mPort;
    }

    public int getId() {
        return this.mPort.handle().id();
    }

    public CharSequence getProductName() {
        String portName = this.mPort.name();
        return portName.length() != 0 ? portName : Build.MODEL;
    }

    public String getAddress() {
        return this.mPort.address();
    }

    public boolean isSource() {
        return this.mPort.role() == 1;
    }

    public boolean isSink() {
        return this.mPort.role() == 2;
    }

    public int[] getSampleRates() {
        return this.mPort.samplingRates();
    }

    public int[] getChannelMasks() {
        return this.mPort.channelMasks();
    }

    public int[] getChannelIndexMasks() {
        return this.mPort.channelIndexMasks();
    }

    public int[] getChannelCounts() {
        int i;
        TreeSet<Integer> countSet = new TreeSet<>();
        int[] channelMasks = getChannelMasks();
        for (int mask : channelMasks) {
            if (isSink()) {
                i = AudioFormat.channelCountFromOutChannelMask(mask);
            } else {
                i = AudioFormat.channelCountFromInChannelMask(mask);
            }
            countSet.add(Integer.valueOf(i));
        }
        for (int index_mask : getChannelIndexMasks()) {
            countSet.add(Integer.valueOf(Integer.bitCount(index_mask)));
        }
        int[] counts = new int[countSet.size()];
        int index = 0;
        Iterator<Integer> it = countSet.iterator();
        while (it.hasNext()) {
            counts[index] = it.next().intValue();
            index++;
        }
        return counts;
    }

    public int[] getEncodings() {
        return AudioFormat.filterPublicFormats(this.mPort.formats());
    }

    public int getType() {
        return INT_TO_EXT_DEVICE_MAPPING.get(this.mPort.type(), 0);
    }

    public static int convertDeviceTypeToInternalDevice(int deviceType) {
        return EXT_TO_INT_DEVICE_MAPPING.get(deviceType, 0);
    }

    public static int convertInternalDeviceToDeviceType(int intDevice) {
        return INT_TO_EXT_DEVICE_MAPPING.get(intDevice, 0);
    }

    static {
        INT_TO_EXT_DEVICE_MAPPING.put(1, 1);
        INT_TO_EXT_DEVICE_MAPPING.put(2, 2);
        INT_TO_EXT_DEVICE_MAPPING.put(4, 3);
        INT_TO_EXT_DEVICE_MAPPING.put(8, 4);
        INT_TO_EXT_DEVICE_MAPPING.put(16, 7);
        INT_TO_EXT_DEVICE_MAPPING.put(32, 7);
        INT_TO_EXT_DEVICE_MAPPING.put(64, 7);
        INT_TO_EXT_DEVICE_MAPPING.put(128, 8);
        INT_TO_EXT_DEVICE_MAPPING.put(256, 8);
        INT_TO_EXT_DEVICE_MAPPING.put(512, 8);
        INT_TO_EXT_DEVICE_MAPPING.put(1024, 9);
        INT_TO_EXT_DEVICE_MAPPING.put(2048, 13);
        INT_TO_EXT_DEVICE_MAPPING.put(4096, 13);
        INT_TO_EXT_DEVICE_MAPPING.put(8192, 12);
        INT_TO_EXT_DEVICE_MAPPING.put(16384, 11);
        INT_TO_EXT_DEVICE_MAPPING.put(536870912, 11);
        INT_TO_EXT_DEVICE_MAPPING.put(67108864, 22);
        INT_TO_EXT_DEVICE_MAPPING.put(65536, 18);
        INT_TO_EXT_DEVICE_MAPPING.put(131072, 5);
        INT_TO_EXT_DEVICE_MAPPING.put(262144, 10);
        INT_TO_EXT_DEVICE_MAPPING.put(524288, 6);
        INT_TO_EXT_DEVICE_MAPPING.put(1048576, 14);
        INT_TO_EXT_DEVICE_MAPPING.put(2097152, 19);
        INT_TO_EXT_DEVICE_MAPPING.put(8388608, 20);
        INT_TO_EXT_DEVICE_MAPPING.put(16777216, 21);
        INT_TO_EXT_DEVICE_MAPPING.put(134217728, 23);
        INT_TO_EXT_DEVICE_MAPPING.put(33554432, 24);
        INT_TO_EXT_DEVICE_MAPPING.put(32768, 25);
        INT_TO_EXT_DEVICE_MAPPING.put(-2147483644, 15);
        INT_TO_EXT_DEVICE_MAPPING.put(-2147483640, 7);
        INT_TO_EXT_DEVICE_MAPPING.put(-2147483632, 3);
        INT_TO_EXT_DEVICE_MAPPING.put(-2147483616, 9);
        INT_TO_EXT_DEVICE_MAPPING.put(-2147483584, 18);
        INT_TO_EXT_DEVICE_MAPPING.put(-2147483520, 15);
        INT_TO_EXT_DEVICE_MAPPING.put(-2147483136, 13);
        INT_TO_EXT_DEVICE_MAPPING.put(-2147482624, 13);
        INT_TO_EXT_DEVICE_MAPPING.put(-2147481600, 12);
        INT_TO_EXT_DEVICE_MAPPING.put(-2147479552, 11);
        INT_TO_EXT_DEVICE_MAPPING.put(-2145386496, 11);
        INT_TO_EXT_DEVICE_MAPPING.put(AudioSystem.DEVICE_IN_USB_HEADSET, 22);
        INT_TO_EXT_DEVICE_MAPPING.put(-2147475456, 16);
        INT_TO_EXT_DEVICE_MAPPING.put(-2147467264, 17);
        INT_TO_EXT_DEVICE_MAPPING.put(-2147450880, 5);
        INT_TO_EXT_DEVICE_MAPPING.put(-2147418112, 6);
        INT_TO_EXT_DEVICE_MAPPING.put(AudioSystem.DEVICE_IN_BLUETOOTH_A2DP, 8);
        INT_TO_EXT_DEVICE_MAPPING.put(AudioSystem.DEVICE_IN_IP, 20);
        INT_TO_EXT_DEVICE_MAPPING.put(AudioSystem.DEVICE_IN_BUS, 21);
        INT_TO_EXT_DEVICE_MAPPING.put(AudioSystem.DEVICE_IN_PROXY, 24);
        INT_TO_EXT_DEVICE_MAPPING.put(AudioSystem.DEVICE_IN_REMOTE_SUBMIX, 25);
        EXT_TO_INT_DEVICE_MAPPING.put(1, 1);
        EXT_TO_INT_DEVICE_MAPPING.put(2, 2);
        EXT_TO_INT_DEVICE_MAPPING.put(3, 4);
        EXT_TO_INT_DEVICE_MAPPING.put(4, 8);
        EXT_TO_INT_DEVICE_MAPPING.put(5, 131072);
        EXT_TO_INT_DEVICE_MAPPING.put(6, 524288);
        EXT_TO_INT_DEVICE_MAPPING.put(7, 16);
        EXT_TO_INT_DEVICE_MAPPING.put(8, 128);
        EXT_TO_INT_DEVICE_MAPPING.put(9, 1024);
        EXT_TO_INT_DEVICE_MAPPING.put(10, 262144);
        EXT_TO_INT_DEVICE_MAPPING.put(11, 16384);
        EXT_TO_INT_DEVICE_MAPPING.put(11, 536870912);
        EXT_TO_INT_DEVICE_MAPPING.put(22, 67108864);
        EXT_TO_INT_DEVICE_MAPPING.put(12, 8192);
        EXT_TO_INT_DEVICE_MAPPING.put(13, 2048);
        EXT_TO_INT_DEVICE_MAPPING.put(14, 1048576);
        EXT_TO_INT_DEVICE_MAPPING.put(15, -2147483644);
        EXT_TO_INT_DEVICE_MAPPING.put(16, -2147475456);
        EXT_TO_INT_DEVICE_MAPPING.put(17, -2147467264);
        EXT_TO_INT_DEVICE_MAPPING.put(18, 65536);
        EXT_TO_INT_DEVICE_MAPPING.put(19, 2097152);
        EXT_TO_INT_DEVICE_MAPPING.put(20, 8388608);
        EXT_TO_INT_DEVICE_MAPPING.put(21, 16777216);
        EXT_TO_INT_DEVICE_MAPPING.put(23, 134217728);
        EXT_TO_INT_DEVICE_MAPPING.put(24, 33554432);
        EXT_TO_INT_DEVICE_MAPPING.put(25, 32768);
    }
}
