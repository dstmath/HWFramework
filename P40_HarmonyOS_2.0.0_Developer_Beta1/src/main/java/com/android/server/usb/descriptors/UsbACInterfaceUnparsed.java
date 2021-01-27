package com.android.server.usb.descriptors;

public final class UsbACInterfaceUnparsed extends UsbACInterface {
    private static final String TAG = "UsbACInterfaceUnparsed";

    public UsbACInterfaceUnparsed(int length, byte type, byte subtype, int subClass) {
        super(length, type, subtype, subClass);
    }
}
