package com.android.server.usb.descriptors;

import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbInterface;
import com.android.server.usb.descriptors.report.ReportCanvas;
import java.util.ArrayList;

public final class UsbConfigDescriptor extends UsbDescriptor {
    private static final boolean DEBUG = false;
    private static final String TAG = "UsbConfigDescriptor";
    private int mAttribs;
    private byte mConfigIndex;
    private int mConfigValue;
    private ArrayList<UsbInterfaceDescriptor> mInterfaceDescriptors = new ArrayList<>();
    private int mMaxPower;
    private byte mNumInterfaces;
    private int mTotalLength;

    UsbConfigDescriptor(int length, byte type) {
        super(length, type);
        this.mHierarchyLevel = 2;
    }

    public int getTotalLength() {
        return this.mTotalLength;
    }

    public byte getNumInterfaces() {
        return this.mNumInterfaces;
    }

    public int getConfigValue() {
        return this.mConfigValue;
    }

    public byte getConfigIndex() {
        return this.mConfigIndex;
    }

    public int getAttribs() {
        return this.mAttribs;
    }

    public int getMaxPower() {
        return this.mMaxPower;
    }

    /* access modifiers changed from: package-private */
    public void addInterfaceDescriptor(UsbInterfaceDescriptor interfaceDesc) {
        this.mInterfaceDescriptors.add(interfaceDesc);
    }

    /* access modifiers changed from: package-private */
    public UsbConfiguration toAndroid(UsbDescriptorParser parser) {
        UsbConfiguration config = new UsbConfiguration(this.mConfigValue, parser.getDescriptorString(this.mConfigIndex), this.mAttribs, this.mMaxPower);
        UsbInterface[] interfaces = new UsbInterface[this.mInterfaceDescriptors.size()];
        for (int index = 0; index < this.mInterfaceDescriptors.size(); index++) {
            interfaces[index] = this.mInterfaceDescriptors.get(index).toAndroid(parser);
        }
        config.setInterfaces(interfaces);
        return config;
    }

    public int parseRawDescriptors(ByteStream stream) {
        this.mTotalLength = stream.unpackUsbShort();
        this.mNumInterfaces = stream.getByte();
        this.mConfigValue = stream.getUnsignedByte();
        this.mConfigIndex = stream.getByte();
        this.mAttribs = stream.getUnsignedByte();
        this.mMaxPower = stream.getUnsignedByte();
        return this.mLength;
    }

    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.openList();
        canvas.writeListItem("Config # " + getConfigValue());
        canvas.writeListItem(getNumInterfaces() + " Interfaces.");
        canvas.writeListItem("Attributes: " + ReportCanvas.getHexString(getAttribs()));
        canvas.closeList();
    }
}
