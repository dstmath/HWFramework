package com.android.server.wifi.hotspot2;

public class IconEvent {
    private final long mBSSID;
    private final String mFileName;
    private final int mSize;

    public IconEvent(long bssid, String fileName, int size) {
        this.mBSSID = bssid;
        this.mFileName = fileName;
        this.mSize = size;
    }

    public long getBSSID() {
        return this.mBSSID;
    }

    public String getFileName() {
        return this.mFileName;
    }

    public int getSize() {
        return this.mSize;
    }

    public String toString() {
        return "IconEvent: BSSID=" + String.format("%012x", new Object[]{Long.valueOf(this.mBSSID)}) + ", fileName='" + this.mFileName + '\'' + ", size=" + this.mSize;
    }
}
