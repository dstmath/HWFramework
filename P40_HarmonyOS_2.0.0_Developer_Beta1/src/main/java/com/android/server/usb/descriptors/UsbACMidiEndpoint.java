package com.android.server.usb.descriptors;

import com.android.server.usb.descriptors.report.ReportCanvas;

public final class UsbACMidiEndpoint extends UsbACEndpoint {
    private static final String TAG = "UsbACMidiEndpoint";
    private byte[] mJackIds;
    private byte mNumJacks;

    @Override // com.android.server.usb.descriptors.UsbACEndpoint
    public /* bridge */ /* synthetic */ int getSubclass() {
        return super.getSubclass();
    }

    @Override // com.android.server.usb.descriptors.UsbACEndpoint
    public /* bridge */ /* synthetic */ byte getSubtype() {
        return super.getSubtype();
    }

    public UsbACMidiEndpoint(int length, byte type, int subclass) {
        super(length, type, subclass);
    }

    public byte getNumJacks() {
        return this.mNumJacks;
    }

    public byte[] getJackIds() {
        return this.mJackIds;
    }

    @Override // com.android.server.usb.descriptors.UsbACEndpoint, com.android.server.usb.descriptors.UsbDescriptor
    public int parseRawDescriptors(ByteStream stream) {
        super.parseRawDescriptors(stream);
        this.mNumJacks = stream.getByte();
        this.mJackIds = new byte[this.mNumJacks];
        for (int jack = 0; jack < this.mNumJacks; jack++) {
            this.mJackIds[jack] = stream.getByte();
        }
        return this.mLength;
    }

    @Override // com.android.server.usb.descriptors.UsbDescriptor, com.android.server.usb.descriptors.report.Reporting
    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.writeHeader(3, "AC Midi Endpoint: " + ReportCanvas.getHexString(getType()) + " Length: " + getLength());
        canvas.openList();
        canvas.writeListItem("" + ((int) getNumJacks()) + " Jacks.");
        canvas.closeList();
    }
}
