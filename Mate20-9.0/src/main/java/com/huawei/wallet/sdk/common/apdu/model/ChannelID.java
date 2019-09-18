package com.huawei.wallet.sdk.common.apdu.model;

import android.nfc.tech.IsoDep;

public class ChannelID {
    private String aid;
    private int channelType = 0;
    private IsoDep isodep;
    private int mediaType = 0;

    public String getAid() {
        return this.aid;
    }

    public void setAid(String aid2) {
        this.aid = aid2;
    }

    public int getChannelType() {
        return this.channelType;
    }

    public void setChannelType(int channelType2) {
        this.channelType = channelType2;
    }

    public int getMediaType() {
        return this.mediaType;
    }

    public void setMediaType(int mediaType2) {
        this.mediaType = mediaType2;
    }

    public IsoDep getIsodep() {
        return this.isodep;
    }

    public void setIsodep(IsoDep isodep2) {
        this.isodep = isodep2;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelID channelID = (ChannelID) o;
        if (this.channelType == channelID.channelType && this.mediaType == channelID.mediaType && this.aid != null) {
            return this.aid.equals(channelID.aid);
        }
        return false;
    }

    public int hashCode() {
        return (31 * ((31 * (this.aid != null ? this.aid.hashCode() : 0)) + this.channelType)) + this.mediaType;
    }
}
