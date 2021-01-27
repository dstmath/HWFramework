package com.android.server.usb.descriptors;

import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import com.android.server.usb.descriptors.report.ReportCanvas;
import com.android.server.usb.descriptors.report.UsbStrings;
import java.util.ArrayList;

public final class UsbDeviceDescriptor extends UsbDescriptor {
    private static final boolean DEBUG = false;
    private static final String TAG = "UsbDeviceDescriptor";
    public static final int USBSPEC_1_0 = 256;
    public static final int USBSPEC_1_1 = 272;
    public static final int USBSPEC_2_0 = 512;
    private ArrayList<UsbConfigDescriptor> mConfigDescriptors = new ArrayList<>();
    private int mDevClass;
    private int mDevSubClass;
    private int mDeviceRelease;
    private byte mMfgIndex;
    private byte mNumConfigs;
    private byte mPacketSize;
    private int mProductID;
    private byte mProductIndex;
    private int mProtocol;
    private byte mSerialIndex;
    private int mSpec;
    private int mVendorID;

    UsbDeviceDescriptor(int length, byte type) {
        super(length, type);
        this.mHierarchyLevel = 1;
    }

    public int getSpec() {
        return this.mSpec;
    }

    public int getDevClass() {
        return this.mDevClass;
    }

    public int getDevSubClass() {
        return this.mDevSubClass;
    }

    public int getProtocol() {
        return this.mProtocol;
    }

    public byte getPacketSize() {
        return this.mPacketSize;
    }

    public int getVendorID() {
        return this.mVendorID;
    }

    public int getProductID() {
        return this.mProductID;
    }

    public int getDeviceRelease() {
        return this.mDeviceRelease;
    }

    public String getDeviceReleaseString() {
        int i = this.mDeviceRelease;
        return String.format("%d.%d%d", Integer.valueOf((((i & 61440) >> 12) * 10) + ((i & 3840) >> 8)), Integer.valueOf((i & 240) >> 4), Integer.valueOf(i & 15));
    }

    public byte getMfgIndex() {
        return this.mMfgIndex;
    }

    public String getMfgString(UsbDescriptorParser p) {
        return p.getDescriptorString(this.mMfgIndex);
    }

    public byte getProductIndex() {
        return this.mProductIndex;
    }

    public String getProductString(UsbDescriptorParser p) {
        return p.getDescriptorString(this.mProductIndex);
    }

    public byte getSerialIndex() {
        return this.mSerialIndex;
    }

    public String getSerialString(UsbDescriptorParser p) {
        return p.getDescriptorString(this.mSerialIndex);
    }

    public byte getNumConfigs() {
        return this.mNumConfigs;
    }

    /* access modifiers changed from: package-private */
    public void addConfigDescriptor(UsbConfigDescriptor config) {
        this.mConfigDescriptors.add(config);
    }

    public UsbDevice.Builder toAndroid(UsbDescriptorParser parser) {
        String mfgName = getMfgString(parser);
        String prodName = getProductString(parser);
        String versionString = getDeviceReleaseString();
        String serialStr = getSerialString(parser);
        UsbConfiguration[] configs = new UsbConfiguration[this.mConfigDescriptors.size()];
        Log.d(TAG, "  " + configs.length + " configs");
        for (int index = 0; index < this.mConfigDescriptors.size(); index++) {
            configs[index] = this.mConfigDescriptors.get(index).toAndroid(parser);
        }
        return new UsbDevice.Builder(parser.getDeviceAddr(), this.mVendorID, this.mProductID, this.mDevClass, this.mDevSubClass, this.mProtocol, mfgName, prodName, versionString, configs, serialStr);
    }

    @Override // com.android.server.usb.descriptors.UsbDescriptor
    public int parseRawDescriptors(ByteStream stream) {
        this.mSpec = stream.unpackUsbShort();
        this.mDevClass = stream.getUnsignedByte();
        this.mDevSubClass = stream.getUnsignedByte();
        this.mProtocol = stream.getUnsignedByte();
        this.mPacketSize = stream.getByte();
        this.mVendorID = stream.unpackUsbShort();
        this.mProductID = stream.unpackUsbShort();
        this.mDeviceRelease = stream.unpackUsbShort();
        this.mMfgIndex = stream.getByte();
        this.mProductIndex = stream.getByte();
        this.mSerialIndex = stream.getByte();
        this.mNumConfigs = stream.getByte();
        return this.mLength;
    }

    @Override // com.android.server.usb.descriptors.UsbDescriptor, com.android.server.usb.descriptors.report.Reporting
    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.openList();
        int spec = getSpec();
        canvas.writeListItem("Spec: " + ReportCanvas.getBCDString(spec));
        int devClass = getDevClass();
        String classStr = UsbStrings.getClassName(devClass);
        int devSubClass = getDevSubClass();
        String subClasStr = UsbStrings.getClassName(devSubClass);
        canvas.writeListItem("Class " + devClass + ": " + classStr + " Subclass" + devSubClass + ": " + subClasStr);
        StringBuilder sb = new StringBuilder();
        sb.append("Vendor ID: ");
        sb.append(ReportCanvas.getHexString(getVendorID()));
        sb.append(" Product ID: ");
        sb.append(ReportCanvas.getHexString(getProductID()));
        sb.append(" Product Release: ");
        sb.append(ReportCanvas.getBCDString(getDeviceRelease()));
        canvas.writeListItem(sb.toString());
        UsbDescriptorParser parser = canvas.getParser();
        byte mfgIndex = getMfgIndex();
        String manufacturer = parser.getDescriptorString(mfgIndex);
        byte productIndex = getProductIndex();
        String product = parser.getDescriptorString(productIndex);
        canvas.writeListItem("Manufacturer " + ((int) mfgIndex) + ": " + manufacturer + " Product " + ((int) productIndex) + ": " + product);
        canvas.closeList();
    }
}
