package com.android.server.usb.descriptors;

import com.android.server.usb.descriptors.report.ReportCanvas;

public final class Usb20ASGeneral extends UsbACInterface {
    private static final String TAG = "Usb20ASGeneral";
    private int mChannelConfig;
    private byte mChannelNames;
    private byte mControls;
    private byte mFormatType;
    private int mFormats;
    private byte mNumChannels;
    private byte mTerminalLink;

    public Usb20ASGeneral(int length, byte type, byte subtype, int subclass) {
        super(length, type, subtype, subclass);
    }

    public byte getTerminalLink() {
        return this.mTerminalLink;
    }

    public byte getControls() {
        return this.mControls;
    }

    public byte getFormatType() {
        return this.mFormatType;
    }

    public int getFormats() {
        return this.mFormats;
    }

    public byte getNumChannels() {
        return this.mNumChannels;
    }

    public int getChannelConfig() {
        return this.mChannelConfig;
    }

    public byte getChannelNames() {
        return this.mChannelNames;
    }

    @Override // com.android.server.usb.descriptors.UsbDescriptor
    public int parseRawDescriptors(ByteStream stream) {
        this.mTerminalLink = stream.getByte();
        this.mControls = stream.getByte();
        this.mFormatType = stream.getByte();
        this.mFormats = stream.unpackUsbInt();
        this.mNumChannels = stream.getByte();
        this.mChannelConfig = stream.unpackUsbInt();
        this.mChannelNames = stream.getByte();
        return this.mLength;
    }

    @Override // com.android.server.usb.descriptors.UsbACInterface, com.android.server.usb.descriptors.UsbDescriptor, com.android.server.usb.descriptors.report.Reporting
    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.openList();
        canvas.writeListItem("Terminal Link: " + ((int) getTerminalLink()));
        canvas.writeListItem("Controls: " + ReportCanvas.getHexString(getControls()));
        canvas.writeListItem("Format Type: " + ReportCanvas.getHexString(getFormatType()));
        canvas.writeListItem("Formats: " + ReportCanvas.getHexString(getFormats()));
        canvas.writeListItem("Num Channels: " + ((int) getNumChannels()));
        canvas.writeListItem("Channel Config: " + ReportCanvas.getHexString(getChannelConfig()));
        canvas.writeListItem("Channel Names String ID: " + ((int) getChannelNames()));
        canvas.closeList();
    }
}
