package com.android.server.usb.descriptors;

import com.android.server.usb.descriptors.report.ReportCanvas;

public final class Usb20ASFormatIII extends UsbASFormat {
    private static final String TAG = "Usb20ASFormatIII";
    private byte mBitResolution;
    private byte mSubslotSize;

    public Usb20ASFormatIII(int length, byte type, byte subtype, byte formatType, int subclass) {
        super(length, type, subtype, formatType, subclass);
    }

    public byte getSubslotSize() {
        return this.mSubslotSize;
    }

    public byte getBitResolution() {
        return this.mBitResolution;
    }

    public int parseRawDescriptors(ByteStream stream) {
        this.mSubslotSize = stream.getByte();
        this.mBitResolution = stream.getByte();
        return this.mLength;
    }

    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.openList();
        canvas.writeListItem("Subslot Size: " + getSubslotSize());
        canvas.writeListItem("Bit Resolution: " + getBitResolution());
        canvas.closeList();
    }
}
