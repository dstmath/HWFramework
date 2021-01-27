package com.android.server.usb.descriptors;

import com.android.server.usb.descriptors.report.ReportCanvas;
import com.android.server.usb.descriptors.report.UsbStrings;

public final class Usb10ASGeneral extends UsbACInterface {
    private static final String TAG = "Usb10ASGeneral";
    private byte mDelay;
    private int mFormatTag;
    private byte mTerminalLink;

    public Usb10ASGeneral(int length, byte type, byte subtype, int subclass) {
        super(length, type, subtype, subclass);
    }

    public byte getTerminalLink() {
        return this.mTerminalLink;
    }

    public byte getDelay() {
        return this.mDelay;
    }

    public int getFormatTag() {
        return this.mFormatTag;
    }

    @Override // com.android.server.usb.descriptors.UsbDescriptor
    public int parseRawDescriptors(ByteStream stream) {
        this.mTerminalLink = stream.getByte();
        this.mDelay = stream.getByte();
        this.mFormatTag = stream.unpackUsbShort();
        return this.mLength;
    }

    @Override // com.android.server.usb.descriptors.UsbACInterface, com.android.server.usb.descriptors.UsbDescriptor, com.android.server.usb.descriptors.report.Reporting
    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.openList();
        canvas.writeListItem("Delay: " + ((int) this.mDelay));
        canvas.writeListItem("Terminal Link: " + ((int) this.mTerminalLink));
        canvas.writeListItem("Format: " + UsbStrings.getAudioFormatName(this.mFormatTag) + " - " + ReportCanvas.getHexString(this.mFormatTag));
        canvas.closeList();
    }
}
