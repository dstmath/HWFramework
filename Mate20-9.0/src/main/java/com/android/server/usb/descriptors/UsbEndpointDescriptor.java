package com.android.server.usb.descriptors;

import android.hardware.usb.UsbEndpoint;
import com.android.server.usb.descriptors.report.ReportCanvas;

public class UsbEndpointDescriptor extends UsbDescriptor {
    private static final boolean DEBUG = false;
    public static final int DIRECTION_INPUT = -128;
    public static final int DIRECTION_OUTPUT = 0;
    public static final byte MASK_ATTRIBS_SYNCTYPE = 12;
    public static final int MASK_ATTRIBS_TRANSTYPE = 3;
    public static final int MASK_ATTRIBS_USEAGE = 48;
    public static final int MASK_ENDPOINT_ADDRESS = 15;
    public static final int MASK_ENDPOINT_DIRECTION = -128;
    public static final byte SYNCTYPE_ADAPTSYNC = 8;
    public static final byte SYNCTYPE_ASYNC = 4;
    public static final byte SYNCTYPE_NONE = 0;
    public static final byte SYNCTYPE_RESERVED = 12;
    private static final String TAG = "UsbEndpointDescriptor";
    public static final int TRANSTYPE_BULK = 2;
    public static final int TRANSTYPE_CONTROL = 0;
    public static final int TRANSTYPE_INTERRUPT = 3;
    public static final int TRANSTYPE_ISO = 1;
    public static final int USEAGE_DATA = 0;
    public static final int USEAGE_EXPLICIT = 32;
    public static final int USEAGE_FEEDBACK = 16;
    public static final int USEAGE_RESERVED = 48;
    private int mAttributes;
    private int mEndpointAddress;
    private int mInterval;
    private int mPacketSize;
    private byte mRefresh;
    private byte mSyncAddress;

    public UsbEndpointDescriptor(int length, byte type) {
        super(length, type);
        this.mHierarchyLevel = 4;
    }

    public int getEndpointAddress() {
        return this.mEndpointAddress;
    }

    public int getAttributes() {
        return this.mAttributes;
    }

    public int getPacketSize() {
        return this.mPacketSize;
    }

    public int getInterval() {
        return this.mInterval;
    }

    public byte getRefresh() {
        return this.mRefresh;
    }

    public byte getSyncAddress() {
        return this.mSyncAddress;
    }

    /* access modifiers changed from: package-private */
    public UsbEndpoint toAndroid(UsbDescriptorParser parser) {
        return new UsbEndpoint(this.mEndpointAddress, this.mAttributes, this.mPacketSize, this.mInterval);
    }

    public int parseRawDescriptors(ByteStream stream) {
        this.mEndpointAddress = stream.getUnsignedByte();
        this.mAttributes = stream.getUnsignedByte();
        this.mPacketSize = stream.unpackUsbShort();
        this.mInterval = stream.getUnsignedByte();
        if (this.mLength == 9) {
            this.mRefresh = stream.getByte();
            this.mSyncAddress = stream.getByte();
        }
        return this.mLength;
    }

    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.openList();
        int address = getEndpointAddress();
        StringBuilder sb = new StringBuilder();
        sb.append("Address: ");
        sb.append(ReportCanvas.getHexString(address & 15));
        sb.append((address & -128) == 0 ? " [out]" : " [in]");
        canvas.writeListItem(sb.toString());
        int attributes = getAttributes();
        canvas.openListItem();
        canvas.write("Attributes: " + ReportCanvas.getHexString(attributes) + " ");
        switch (attributes & 3) {
            case 0:
                canvas.write("Control");
                break;
            case 1:
                canvas.write("Iso");
                break;
            case 2:
                canvas.write("Bulk");
                break;
            case 3:
                canvas.write("Interrupt");
                break;
        }
        canvas.closeListItem();
        if ((attributes & 3) == 1) {
            canvas.openListItem();
            canvas.write("Aync: ");
            int i = attributes & 12;
            if (i == 0) {
                canvas.write("NONE");
            } else if (i == 4) {
                canvas.write("ASYNC");
            } else if (i == 8) {
                canvas.write("ADAPTIVE ASYNC");
            }
            canvas.closeListItem();
            canvas.openListItem();
            canvas.write("Useage: ");
            int i2 = attributes & 48;
            if (i2 == 0) {
                canvas.write("DATA");
            } else if (i2 == 16) {
                canvas.write("FEEDBACK");
            } else if (i2 == 32) {
                canvas.write("EXPLICIT FEEDBACK");
            } else if (i2 == 48) {
                canvas.write("RESERVED");
            }
            canvas.closeListItem();
        }
        canvas.writeListItem("Package Size: " + getPacketSize());
        canvas.writeListItem("Interval: " + getInterval());
        canvas.closeList();
    }
}
