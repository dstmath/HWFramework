package com.android.server.usb.descriptors;

public final class UsbUnknown extends UsbDescriptor {
    static final String TAG = "UsbUnknown";

    public UsbUnknown(int length, byte type) {
        super(length, type);
    }
}
