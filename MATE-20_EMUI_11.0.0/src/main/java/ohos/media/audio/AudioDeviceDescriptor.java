package ohos.media.audio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import ohos.media.audio.AudioStreamInfo;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AudioDeviceDescriptor {
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioDeviceDescriptor.class);
    private String address;
    private int[] channelIndexMasks;
    private int[] channelMasks;
    private List<AudioStreamInfo.EncodingFormat> encodings;
    private int id;
    private String name;
    private DeviceRole role;
    private int[] samplingRates;
    private DeviceType type;

    public enum DeviceType {
        INVALID(0),
        EARPIECE(1),
        SPEAKER(2),
        WIRED_HEADSET(3),
        WIRED_HEADPHONES(4),
        ANALOG(5),
        DIGITAL(6),
        BLUETOOTH_SCO(7),
        BLUETOOTH_A2DP(8),
        HDMI(9),
        HDMI_ARC(10),
        USB_DEVICE(11),
        USB_ACCESSORY(12),
        DOCK(13),
        FM(14),
        MIC(15),
        FM_TUNER(16),
        TV_TUNER(17),
        TELEPHONY(18),
        AUXILIARY(19),
        IP(20),
        BUS(21),
        USB_HEADSET(22),
        HEARING_AID(23);
        
        private static final DeviceType[] DEVICE_TYPE_MAPPING = new DeviceType[24];
        private static final int MAX_VAULE = 24;
        private final int value;

        static {
            DeviceType[] values = values();
            for (DeviceType deviceType : values) {
                DEVICE_TYPE_MAPPING[deviceType.getValue()] = deviceType;
            }
        }

        private DeviceType(int i) {
            this.value = i;
        }

        public static DeviceType valueOf(int i) {
            if (i >= 0 && i < 24) {
                return DEVICE_TYPE_MAPPING[i];
            }
            AudioDeviceDescriptor.LOGGER.error("invalid input value for DeviceType, value=%{public}d", Integer.valueOf(i));
            return INVALID;
        }

        public int getValue() {
            return this.value;
        }
    }

    public enum DeviceRole {
        INPUT_DEVICE(1),
        OUTPUT_DEVICE(2);
        
        private final int value;

        private DeviceRole(int i) {
            this.value = i;
        }

        public static DeviceRole valueOf(int i) {
            DeviceRole[] values = values();
            for (DeviceRole deviceRole : values) {
                if (i == deviceRole.getValue()) {
                    return deviceRole;
                }
            }
            AudioDeviceDescriptor.LOGGER.error("invalid input value for DeviceRole, value=%{public}d", Integer.valueOf(i));
            return null;
        }

        public int getValue() {
            return this.value;
        }
    }

    public enum DeviceFlag {
        OUTPUT_DEVICES_FLAG(1),
        INPUT_DEVICES_FLAG(2),
        ALL_DEVICES_FLAG(3);
        
        private final int value;

        private DeviceFlag(int i) {
            this.value = i;
        }

        public static DeviceFlag valueOf(int i) {
            DeviceFlag[] values = values();
            for (DeviceFlag deviceFlag : values) {
                if (i == deviceFlag.getValue()) {
                    return deviceFlag;
                }
            }
            AudioDeviceDescriptor.LOGGER.error("invalid input value for DeviceFlag, value=%{public}d", Integer.valueOf(i));
            return null;
        }

        public int getValue() {
            return this.value;
        }
    }

    public AudioDeviceDescriptor(int i, String str, String str2, DeviceType deviceType, DeviceRole deviceRole, int[] iArr, int[] iArr2, int[] iArr3, List<AudioStreamInfo.EncodingFormat> list) {
        this.id = i;
        this.name = str;
        this.address = str2;
        this.type = deviceType;
        this.role = deviceRole;
        this.samplingRates = iArr;
        this.channelMasks = iArr2;
        this.channelIndexMasks = iArr3;
        this.encodings = list;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            LOGGER.debug("compare with different object", new Object[0]);
            return false;
        } else if (!(obj instanceof AudioDeviceDescriptor)) {
            LOGGER.debug("that object is not instance of AudioDeviceDescriptor", new Object[0]);
            return false;
        } else {
            AudioDeviceDescriptor audioDeviceDescriptor = (AudioDeviceDescriptor) obj;
            return this.id == audioDeviceDescriptor.id && this.name.equals(audioDeviceDescriptor.name) && this.address.equals(audioDeviceDescriptor.address) && this.type == audioDeviceDescriptor.type && this.role == audioDeviceDescriptor.role && this.encodings == audioDeviceDescriptor.encodings;
        }
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.id), this.name, this.address, this.type, this.role, this.samplingRates, this.channelMasks, this.channelIndexMasks, this.encodings);
    }

    public boolean isOutputDevice() {
        return this.role.getValue() == DeviceRole.OUTPUT_DEVICE.getValue();
    }

    public boolean isInputDevice() {
        return this.role.getValue() == DeviceRole.INPUT_DEVICE.getValue();
    }

    public int getId() {
        return this.id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String str) {
        this.address = str;
    }

    public DeviceType getType() {
        return this.type;
    }

    public void setType(DeviceType deviceType) {
        this.type = deviceType;
    }

    public DeviceRole getRole() {
        return this.role;
    }

    public void setRole(DeviceRole deviceRole) {
        this.role = deviceRole;
    }

    public int[] getSamplingRates() {
        return this.samplingRates;
    }

    public void setSamplingRates(int[] iArr) {
        this.samplingRates = iArr;
    }

    public int[] getChannelMasks() {
        return this.channelMasks;
    }

    public void setChannelMasks(int[] iArr) {
        this.channelMasks = iArr;
    }

    public int[] getChannelIndexMasks() {
        return this.channelIndexMasks;
    }

    public void setChannelIndexMasks(int[] iArr) {
        this.channelIndexMasks = iArr;
    }

    public List<AudioStreamInfo.EncodingFormat> getEncodings() {
        return this.encodings;
    }

    public void setEncodings(List<AudioStreamInfo.EncodingFormat> list) {
        this.encodings = new ArrayList(list);
    }

    public int[] getChannelCounts() {
        TreeSet treeSet = new TreeSet();
        int i = 0;
        for (int i2 : getChannelMasks()) {
            treeSet.add(Integer.valueOf(Integer.bitCount(i2)));
        }
        for (int i3 : getChannelIndexMasks()) {
            treeSet.add(Integer.valueOf(Integer.bitCount(i3)));
        }
        int[] iArr = new int[treeSet.size()];
        Iterator it = treeSet.iterator();
        while (it.hasNext()) {
            iArr[i] = ((Integer) it.next()).intValue();
            i++;
        }
        return iArr;
    }
}
