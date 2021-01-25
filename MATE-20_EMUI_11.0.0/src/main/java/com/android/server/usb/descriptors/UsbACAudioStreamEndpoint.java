package com.android.server.usb.descriptors;

public class UsbACAudioStreamEndpoint extends UsbACEndpoint {
    private static final String TAG = "UsbACAudioStreamEndpoint";

    @Override // com.android.server.usb.descriptors.UsbACEndpoint
    public /* bridge */ /* synthetic */ int getSubclass() {
        return super.getSubclass();
    }

    @Override // com.android.server.usb.descriptors.UsbACEndpoint
    public /* bridge */ /* synthetic */ byte getSubtype() {
        return super.getSubtype();
    }

    public UsbACAudioStreamEndpoint(int length, byte type, int subclass) {
        super(length, type, subclass);
    }

    @Override // com.android.server.usb.descriptors.UsbACEndpoint, com.android.server.usb.descriptors.UsbDescriptor
    public int parseRawDescriptors(ByteStream stream) {
        super.parseRawDescriptors(stream);
        stream.advance(this.mLength - stream.getReadCount());
        return this.mLength;
    }
}
