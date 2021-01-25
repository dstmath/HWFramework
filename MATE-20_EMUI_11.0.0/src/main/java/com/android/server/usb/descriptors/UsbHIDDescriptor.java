package com.android.server.usb.descriptors;

import com.android.server.usb.descriptors.report.ReportCanvas;

public final class UsbHIDDescriptor extends UsbDescriptor {
    private static final String TAG = "UsbHIDDescriptor";
    private byte mCountryCode;
    private int mDescriptorLen;
    private byte mDescriptorType;
    private byte mNumDescriptors;
    private int mRelease;

    public UsbHIDDescriptor(int length, byte type) {
        super(length, type);
        this.mHierarchyLevel = 3;
    }

    public int getRelease() {
        return this.mRelease;
    }

    public byte getCountryCode() {
        return this.mCountryCode;
    }

    public byte getNumDescriptors() {
        return this.mNumDescriptors;
    }

    public byte getDescriptorType() {
        return this.mDescriptorType;
    }

    public int getDescriptorLen() {
        return this.mDescriptorLen;
    }

    @Override // com.android.server.usb.descriptors.UsbDescriptor
    public int parseRawDescriptors(ByteStream stream) {
        this.mRelease = stream.unpackUsbShort();
        this.mCountryCode = stream.getByte();
        this.mNumDescriptors = stream.getByte();
        this.mDescriptorType = stream.getByte();
        this.mDescriptorLen = stream.unpackUsbShort();
        return this.mLength;
    }

    @Override // com.android.server.usb.descriptors.UsbDescriptor, com.android.server.usb.descriptors.report.Reporting
    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.openList();
        canvas.writeListItem("Spec: " + ReportCanvas.getBCDString(getRelease()));
        canvas.writeListItem("Type: " + ReportCanvas.getBCDString(getDescriptorType()));
        canvas.writeListItem("" + ((int) getNumDescriptors()) + " Descriptors Len: " + getDescriptorLen());
        canvas.closeList();
    }
}
