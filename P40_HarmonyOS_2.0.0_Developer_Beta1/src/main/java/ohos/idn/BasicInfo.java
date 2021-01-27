package ohos.idn;

import java.util.Locale;
import ohos.annotation.SystemApi;
import ohos.media.camera.params.adapter.camera2ex.CameraMetadataEx;

@SystemApi
public class BasicInfo {
    private DeviceType deviceType;
    private String name;
    private String nodeId;

    public enum DeviceType {
        SET_TOP_BOX((byte) 3),
        SMART_SPEAKER((byte) 10),
        DESKTOP_PC((byte) 11),
        LAPTOP((byte) 12),
        SMART_PHONE((byte) 14),
        SMART_PAD(CameraMetadataEx.HUAWEI_EXPOSURE_2),
        THIRD_TV((byte) 46),
        SMART_WATCH((byte) 109),
        SMART_CAR((byte) -125),
        CHILDREN_WATCH((byte) -123),
        PROJECTOR((byte) -104),
        SMART_TV((byte) -100),
        THIRD_PHONE((byte) -99),
        THIRD_PAD((byte) -98),
        THIRD_LAPTOP((byte) -96),
        THIRD_SPEAKERS((byte) -89),
        LOUD_SPEAKER((byte) -82),
        SCREEN_THROWER((byte) -79),
        WHITEBOARD((byte) -78),
        LITE_HARMONY_L0((byte) -16),
        LITE_HARMONY_L1((byte) -15);
        
        private byte mValue;

        private DeviceType(byte b) {
            this.mValue = b;
        }

        public byte toByte() {
            return this.mValue;
        }

        public String toHexString() {
            return String.format(Locale.ENGLISH, "%03X", Byte.valueOf(this.mValue));
        }

        public static DeviceType fromByte(byte b) {
            DeviceType[] values = values();
            for (DeviceType deviceType : values) {
                if (deviceType.toByte() == b) {
                    return deviceType;
                }
            }
            return null;
        }
    }

    public BasicInfo(String str, DeviceType deviceType2, String str2) {
        this.name = str;
        this.deviceType = deviceType2;
        this.nodeId = str2;
    }

    public String getName() {
        return this.name;
    }

    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    public String getNodeId() {
        return this.nodeId;
    }

    public String toString() {
        return "BasicInfo{name='" + this.name + "', deviceType='" + this.deviceType + "', nodeId ='" + this.nodeId + "'}";
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BasicInfo)) {
            return false;
        }
        BasicInfo basicInfo = (BasicInfo) obj;
        String str = this.nodeId;
        if (str == null || !str.equals(basicInfo.getNodeId())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        String str = this.nodeId;
        if (str == null) {
            return 0;
        }
        return str.hashCode();
    }
}
