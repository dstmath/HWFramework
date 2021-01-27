package com.android.server.usb.descriptors;

import com.android.server.usb.descriptors.report.ReportCanvas;

public final class Usb10ACInputTerminal extends UsbACTerminal {
    private static final String TAG = "Usb10ACInputTerminal";
    private int mChannelConfig;
    private byte mChannelNames;
    private byte mNrChannels;
    private byte mTerminal;

    public Usb10ACInputTerminal(int length, byte type, byte subtype, int subclass) {
        super(length, type, subtype, subclass);
    }

    public byte getNrChannels() {
        return this.mNrChannels;
    }

    public int getChannelConfig() {
        return this.mChannelConfig;
    }

    public byte getChannelNames() {
        return this.mChannelNames;
    }

    public byte getTerminal() {
        return this.mTerminal;
    }

    @Override // com.android.server.usb.descriptors.UsbACTerminal, com.android.server.usb.descriptors.UsbDescriptor
    public int parseRawDescriptors(ByteStream stream) {
        super.parseRawDescriptors(stream);
        this.mNrChannels = stream.getByte();
        this.mChannelConfig = stream.unpackUsbShort();
        this.mChannelNames = stream.getByte();
        this.mTerminal = stream.getByte();
        return this.mLength;
    }

    @Override // com.android.server.usb.descriptors.UsbACTerminal, com.android.server.usb.descriptors.UsbACInterface, com.android.server.usb.descriptors.UsbDescriptor, com.android.server.usb.descriptors.report.Reporting
    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.openList();
        canvas.writeListItem("Associated Terminal: " + ReportCanvas.getHexString(getAssocTerminal()));
        canvas.writeListItem("" + ((int) getNrChannels()) + " Chans. Config: " + ReportCanvas.getHexString(getChannelConfig()));
        canvas.closeList();
    }
}
