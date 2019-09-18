package com.android.server.usb.descriptors;

import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import com.android.server.usb.descriptors.report.ReportCanvas;
import com.android.server.usb.descriptors.report.UsbStrings;
import java.util.ArrayList;

public class UsbInterfaceDescriptor extends UsbDescriptor {
    private static final boolean DEBUG = false;
    private static final String TAG = "UsbInterfaceDescriptor";
    protected byte mAlternateSetting;
    protected byte mDescrIndex;
    private ArrayList<UsbEndpointDescriptor> mEndpointDescriptors = new ArrayList<>();
    protected int mInterfaceNumber;
    protected byte mNumEndpoints;
    protected int mProtocol;
    protected int mUsbClass;
    protected int mUsbSubclass;

    UsbInterfaceDescriptor(int length, byte type) {
        super(length, type);
        this.mHierarchyLevel = 3;
    }

    public int parseRawDescriptors(ByteStream stream) {
        this.mInterfaceNumber = stream.getUnsignedByte();
        this.mAlternateSetting = stream.getByte();
        this.mNumEndpoints = stream.getByte();
        this.mUsbClass = stream.getUnsignedByte();
        this.mUsbSubclass = stream.getUnsignedByte();
        this.mProtocol = stream.getUnsignedByte();
        this.mDescrIndex = stream.getByte();
        return this.mLength;
    }

    public int getInterfaceNumber() {
        return this.mInterfaceNumber;
    }

    public byte getAlternateSetting() {
        return this.mAlternateSetting;
    }

    public byte getNumEndpoints() {
        return this.mNumEndpoints;
    }

    public int getUsbClass() {
        return this.mUsbClass;
    }

    public int getUsbSubclass() {
        return this.mUsbSubclass;
    }

    public int getProtocol() {
        return this.mProtocol;
    }

    public byte getDescrIndex() {
        return this.mDescrIndex;
    }

    /* access modifiers changed from: package-private */
    public void addEndpointDescriptor(UsbEndpointDescriptor endpoint) {
        this.mEndpointDescriptors.add(endpoint);
    }

    /* access modifiers changed from: package-private */
    public UsbInterface toAndroid(UsbDescriptorParser parser) {
        UsbInterface ntrface = new UsbInterface(this.mInterfaceNumber, this.mAlternateSetting, parser.getDescriptorString(this.mDescrIndex), this.mUsbClass, this.mUsbSubclass, this.mProtocol);
        UsbEndpoint[] endpoints = new UsbEndpoint[this.mEndpointDescriptors.size()];
        for (int index = 0; index < this.mEndpointDescriptors.size(); index++) {
            endpoints[index] = this.mEndpointDescriptors.get(index).toAndroid(parser);
        }
        ntrface.setEndpoints(endpoints);
        return ntrface;
    }

    public void report(ReportCanvas canvas) {
        super.report(canvas);
        int usbClass = getUsbClass();
        int usbSubclass = getUsbSubclass();
        int protocol = getProtocol();
        String className = UsbStrings.getClassName(usbClass);
        String subclassName = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        if (usbClass == 1) {
            subclassName = UsbStrings.getAudioSubclassName(usbSubclass);
        }
        canvas.openList();
        canvas.writeListItem("Interface #" + getInterfaceNumber());
        canvas.writeListItem("Class: " + ReportCanvas.getHexString(usbClass) + ": " + className);
        canvas.writeListItem("Subclass: " + ReportCanvas.getHexString(usbSubclass) + ": " + subclassName);
        canvas.writeListItem("Protocol: " + protocol + ": " + ReportCanvas.getHexString(protocol));
        StringBuilder sb = new StringBuilder();
        sb.append("Endpoints: ");
        sb.append(getNumEndpoints());
        canvas.writeListItem(sb.toString());
        canvas.closeList();
    }
}
