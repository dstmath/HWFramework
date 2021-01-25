package com.android.server.usb.descriptors;

public final class UsbInterfaceAssoc extends UsbDescriptor {
    private static final String TAG = "UsbInterfaceAssoc";
    private byte mFirstInterface;
    private byte mFunction;
    private byte mFunctionClass;
    private byte mFunctionProtocol;
    private byte mFunctionSubClass;
    private byte mInterfaceCount;

    public UsbInterfaceAssoc(int length, byte type) {
        super(length, type);
    }

    public byte getFirstInterface() {
        return this.mFirstInterface;
    }

    public byte getInterfaceCount() {
        return this.mInterfaceCount;
    }

    public byte getFunctionClass() {
        return this.mFunctionClass;
    }

    public byte getFunctionSubClass() {
        return this.mFunctionSubClass;
    }

    public byte getFunctionProtocol() {
        return this.mFunctionProtocol;
    }

    public byte getFunction() {
        return this.mFunction;
    }

    @Override // com.android.server.usb.descriptors.UsbDescriptor
    public int parseRawDescriptors(ByteStream stream) {
        this.mFirstInterface = stream.getByte();
        this.mInterfaceCount = stream.getByte();
        this.mFunctionClass = stream.getByte();
        this.mFunctionSubClass = stream.getByte();
        this.mFunctionProtocol = stream.getByte();
        this.mFunction = stream.getByte();
        return this.mLength;
    }
}
