package ohos.ai.tts;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class InitParams implements Sequenceable {
    private static final int DEFAULT_TTS_MODE = 2;
    private static final int DEFAULT_VALUE = -10000;
    private String accessKey = "defaultAccessKey";
    private int compressRate = -10000;
    private String deviceId;
    private String deviceName;
    private int deviceType = -10000;
    private String osVersion;
    private int pitch = -10000;
    private String romVersion;
    private String secretKey = "defaultSecretKey";
    private int speaker = -10000;
    private int speed = -10000;
    private int timeout = -10000;
    private int ttsMode = 2;
    private int volume = -10000;

    public InitParams() {
    }

    protected InitParams(Parcel parcel) {
        this.accessKey = parcel.readString();
        this.secretKey = parcel.readString();
        this.ttsMode = parcel.readInt();
        this.deviceId = parcel.readString();
        this.deviceType = parcel.readInt();
        this.compressRate = parcel.readInt();
        this.speed = parcel.readInt();
        this.volume = parcel.readInt();
        this.pitch = parcel.readInt();
        this.speaker = parcel.readInt();
        this.timeout = parcel.readInt();
        this.deviceName = parcel.readString();
        this.osVersion = parcel.readString();
        this.romVersion = parcel.readString();
    }

    public String getAccessKey() {
        return this.accessKey;
    }

    public void setAccessKey(String str) {
        this.accessKey = str;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public void setSecretKey(String str) {
        this.secretKey = str;
    }

    public int getTtsMode() {
        return this.ttsMode;
    }

    public void setTtsMode(int i) {
        this.ttsMode = i;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String str) {
        this.deviceId = str;
    }

    public int getDeviceType() {
        return this.deviceType;
    }

    public void setDeviceType(int i) {
        this.deviceType = i;
    }

    public int getCompressRate() {
        return this.compressRate;
    }

    public void setCompressRate(int i) {
        this.compressRate = i;
    }

    public int getSpeed() {
        return this.speed;
    }

    public void setSpeed(int i) {
        this.speed = i;
    }

    public int getVolume() {
        return this.volume;
    }

    public void setVolume(int i) {
        this.volume = i;
    }

    public int getPitch() {
        return this.pitch;
    }

    public void setPitch(int i) {
        this.pitch = i;
    }

    public int getSpeaker() {
        return this.speaker;
    }

    public void setSpeaker(int i) {
        this.speaker = i;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int i) {
        this.timeout = i;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String str) {
        this.deviceName = str;
    }

    public String getOsVersion() {
        return this.osVersion;
    }

    public void setOsVersion(String str) {
        this.osVersion = str;
    }

    public String getRomVersion() {
        return this.romVersion;
    }

    public void setRomVersion(String str) {
        this.romVersion = str;
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.accessKey);
        parcel.writeString(this.secretKey);
        parcel.writeInt(this.ttsMode);
        parcel.writeString(this.deviceId);
        parcel.writeInt(this.deviceType);
        parcel.writeInt(this.compressRate);
        parcel.writeInt(this.speed);
        parcel.writeInt(this.volume);
        parcel.writeInt(this.pitch);
        parcel.writeInt(this.speaker);
        parcel.writeInt(this.timeout);
        parcel.writeString(this.deviceName);
        parcel.writeString(this.osVersion);
        parcel.writeString(this.romVersion);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.accessKey = parcel.readString();
        this.secretKey = parcel.readString();
        this.ttsMode = parcel.readInt();
        this.deviceId = parcel.readString();
        this.deviceType = parcel.readInt();
        this.compressRate = parcel.readInt();
        this.speed = parcel.readInt();
        this.volume = parcel.readInt();
        this.pitch = parcel.readInt();
        this.speaker = parcel.readInt();
        this.timeout = parcel.readInt();
        this.deviceName = parcel.readString();
        this.osVersion = parcel.readString();
        this.romVersion = parcel.readString();
        return true;
    }
}
