package com.android.server.usb.descriptors;

import com.android.server.usb.descriptors.report.ReportCanvas;

public final class Usb20ASFormatI extends UsbASFormat {
    private static final String TAG = "Usb20ASFormatI";
    private byte mBitResolution;
    private byte mSubSlotSize;

    public Usb20ASFormatI(int length, byte type, byte subtype, byte formatType, int subclass) {
        super(length, type, subtype, formatType, subclass);
    }

    public byte getSubSlotSize() {
        return this.mSubSlotSize;
    }

    public byte getBitResolution() {
        return this.mBitResolution;
    }

    @Override // com.android.server.usb.descriptors.UsbDescriptor
    public int parseRawDescriptors(ByteStream stream) {
        this.mSubSlotSize = stream.getByte();
        this.mBitResolution = stream.getByte();
        return this.mLength;
    }

    @Override // com.android.server.usb.descriptors.UsbASFormat, com.android.server.usb.descriptors.UsbACInterface, com.android.server.usb.descriptors.UsbDescriptor, com.android.server.usb.descriptors.report.Reporting
    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.openList();
        canvas.writeListItem("Subslot Size: " + ((int) getSubSlotSize()));
        canvas.writeListItem("Bit Resolution: " + ((int) getBitResolution()));
        canvas.closeList();
    }
}
