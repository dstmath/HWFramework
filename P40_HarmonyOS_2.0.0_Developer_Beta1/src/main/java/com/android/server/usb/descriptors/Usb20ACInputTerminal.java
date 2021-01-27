package com.android.server.usb.descriptors;

import com.android.server.usb.descriptors.report.ReportCanvas;

public final class Usb20ACInputTerminal extends UsbACTerminal {
    private static final String TAG = "Usb20ACInputTerminal";
    private int mChanConfig;
    private byte mChanNames;
    private byte mClkSourceID;
    private int mControls;
    private byte mNumChannels;
    private byte mTerminalName;

    public Usb20ACInputTerminal(int length, byte type, byte subtype, int subclass) {
        super(length, type, subtype, subclass);
    }

    public byte getClkSourceID() {
        return this.mClkSourceID;
    }

    public byte getNumChannels() {
        return this.mNumChannels;
    }

    public int getChanConfig() {
        return this.mChanConfig;
    }

    public int getControls() {
        return this.mControls;
    }

    @Override // com.android.server.usb.descriptors.UsbACTerminal, com.android.server.usb.descriptors.UsbDescriptor
    public int parseRawDescriptors(ByteStream stream) {
        super.parseRawDescriptors(stream);
        this.mClkSourceID = stream.getByte();
        this.mNumChannels = stream.getByte();
        this.mChanConfig = stream.unpackUsbInt();
        this.mChanNames = stream.getByte();
        this.mControls = stream.unpackUsbShort();
        this.mTerminalName = stream.getByte();
        return this.mLength;
    }

    @Override // com.android.server.usb.descriptors.UsbACTerminal, com.android.server.usb.descriptors.UsbACInterface, com.android.server.usb.descriptors.UsbDescriptor, com.android.server.usb.descriptors.report.Reporting
    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.openList();
        canvas.writeListItem("Clock Source: " + ((int) getClkSourceID()));
        canvas.writeListItem("" + ((int) getNumChannels()) + " Channels. Config: " + ReportCanvas.getHexString(getChanConfig()));
        canvas.closeList();
    }
}
