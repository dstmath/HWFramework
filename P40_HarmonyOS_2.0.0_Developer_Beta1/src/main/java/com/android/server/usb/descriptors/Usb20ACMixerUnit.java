package com.android.server.usb.descriptors;

public final class Usb20ACMixerUnit extends UsbACMixerUnit {
    private static final String TAG = "Usb20ACMixerUnit";
    private int mChanConfig;
    private byte mChanNames;
    private byte[] mControls;
    private byte mControlsMask;
    private byte mNameID;

    public Usb20ACMixerUnit(int length, byte type, byte subtype, int subClass) {
        super(length, type, subtype, subClass);
    }

    @Override // com.android.server.usb.descriptors.UsbACMixerUnit, com.android.server.usb.descriptors.UsbDescriptor
    public int parseRawDescriptors(ByteStream stream) {
        super.parseRawDescriptors(stream);
        this.mChanConfig = stream.unpackUsbInt();
        this.mChanNames = stream.getByte();
        int controlArraySize = calcControlArraySize(this.mNumInputs, this.mNumOutputs);
        this.mControls = new byte[controlArraySize];
        for (int index = 0; index < controlArraySize; index++) {
            this.mControls[index] = stream.getByte();
        }
        this.mControlsMask = stream.getByte();
        this.mNameID = stream.getByte();
        return this.mLength;
    }
}
