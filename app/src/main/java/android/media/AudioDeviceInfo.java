package android.media;

import android.os.Build;
import android.util.SparseIntArray;
import java.util.TreeSet;

public final class AudioDeviceInfo {
    private static final SparseIntArray EXT_TO_INT_DEVICE_MAPPING = null;
    private static final SparseIntArray INT_TO_EXT_DEVICE_MAPPING = null;
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
    public static final int TYPE_IP = 20;
    public static final int TYPE_LINE_ANALOG = 5;
    public static final int TYPE_LINE_DIGITAL = 6;
    public static final int TYPE_TELEPHONY = 18;
    public static final int TYPE_TV_TUNER = 17;
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_USB_ACCESSORY = 12;
    public static final int TYPE_USB_DEVICE = 11;
    public static final int TYPE_WIRED_HEADPHONES = 4;
    public static final int TYPE_WIRED_HEADSET = 3;
    private final AudioDevicePort mPort;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.AudioDeviceInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.AudioDeviceInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.AudioDeviceInfo.<clinit>():void");
    }

    AudioDeviceInfo(AudioDevicePort port) {
        this.mPort = port;
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
        return this.mPort.role() == TYPE_BUILTIN_EARPIECE;
    }

    public boolean isSink() {
        return this.mPort.role() == TYPE_BUILTIN_SPEAKER;
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
        int channelCountFromOutChannelMask;
        TreeSet<Integer> countSet = new TreeSet();
        int[] channelMasks = getChannelMasks();
        int length = channelMasks.length;
        for (int i = TYPE_UNKNOWN; i < length; i += TYPE_BUILTIN_EARPIECE) {
            int mask = channelMasks[i];
            if (isSink()) {
                channelCountFromOutChannelMask = AudioFormat.channelCountFromOutChannelMask(mask);
            } else {
                channelCountFromOutChannelMask = AudioFormat.channelCountFromInChannelMask(mask);
            }
            countSet.add(Integer.valueOf(channelCountFromOutChannelMask));
        }
        int[] channelIndexMasks = getChannelIndexMasks();
        int length2 = channelIndexMasks.length;
        for (channelCountFromOutChannelMask = TYPE_UNKNOWN; channelCountFromOutChannelMask < length2; channelCountFromOutChannelMask += TYPE_BUILTIN_EARPIECE) {
            countSet.add(Integer.valueOf(Integer.bitCount(channelIndexMasks[channelCountFromOutChannelMask])));
        }
        int[] counts = new int[countSet.size()];
        int index = TYPE_UNKNOWN;
        for (Integer intValue : countSet) {
            int index2 = index + TYPE_BUILTIN_EARPIECE;
            counts[index] = intValue.intValue();
            index = index2;
        }
        return counts;
    }

    public int[] getEncodings() {
        return AudioFormat.filterPublicFormats(this.mPort.formats());
    }

    public int getType() {
        return INT_TO_EXT_DEVICE_MAPPING.get(this.mPort.type(), TYPE_UNKNOWN);
    }

    public static int convertDeviceTypeToInternalDevice(int deviceType) {
        return EXT_TO_INT_DEVICE_MAPPING.get(deviceType, TYPE_UNKNOWN);
    }

    public static int convertInternalDeviceToDeviceType(int intDevice) {
        return INT_TO_EXT_DEVICE_MAPPING.get(intDevice, TYPE_UNKNOWN);
    }
}
