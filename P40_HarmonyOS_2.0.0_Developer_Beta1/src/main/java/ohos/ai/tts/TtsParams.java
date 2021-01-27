package ohos.ai.tts;

public class TtsParams {
    private String deviceId;
    private int deviceType = 0;
    private int pitch = 5;
    private int speaker = 0;
    private int speed = 5;
    private int volume = 11;

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
}
