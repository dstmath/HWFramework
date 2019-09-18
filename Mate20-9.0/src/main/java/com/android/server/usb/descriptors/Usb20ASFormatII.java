package com.android.server.usb.descriptors;

import com.android.server.usb.descriptors.report.ReportCanvas;

public final class Usb20ASFormatII extends UsbASFormat {
    private static final String TAG = "Usb20ASFormatII";
    private int mMaxBitRate;
    private int mSlotsPerFrame;

    public Usb20ASFormatII(int length, byte type, byte subtype, byte formatType, int subclass) {
        super(length, type, subtype, formatType, subclass);
    }

    public int getmaxBitRate() {
        return this.mMaxBitRate;
    }

    public int getSlotsPerFrame() {
        return this.mSlotsPerFrame;
    }

    public int parseRawDescriptors(ByteStream stream) {
        this.mMaxBitRate = stream.unpackUsbShort();
        this.mSlotsPerFrame = stream.unpackUsbShort();
        return this.mLength;
    }

    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.openList();
        canvas.writeListItem("Max Bit Rate: " + getmaxBitRate());
        canvas.writeListItem("slots Per Frame: " + getSlotsPerFrame());
        canvas.closeList();
    }
}
