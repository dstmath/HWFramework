package com.huawei.dmsdpsdk2.devicevirtualization;

public class AudioProfile {
    private int channel;
    private int format;
    private int sampleRate;

    public AudioProfile(int sampleRate2, int channel2, int format2) {
        this.sampleRate = sampleRate2;
        this.channel = channel2;
        this.format = format2;
    }

    AudioProfile() {
        this(0, 0, 0);
    }

    public void setSampleRate(int sampleRate2) {
        this.sampleRate = sampleRate2;
    }

    public void setChannel(int channel2) {
        this.channel = channel2;
    }

    public void setFormat(int format2) {
        this.format = format2;
    }

    public int getSampleRate() {
        return this.sampleRate;
    }

    public int getChannel() {
        return this.channel;
    }

    public int getFormat() {
        return this.format;
    }

    public String toString() {
        return "{\"sampleRate\":" + this.sampleRate + ",\"channel\":" + this.channel + ",\"format\":" + this.format + "}";
    }
}
