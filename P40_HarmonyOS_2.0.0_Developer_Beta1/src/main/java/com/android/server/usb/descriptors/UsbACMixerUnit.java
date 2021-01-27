package com.android.server.usb.descriptors;

public class UsbACMixerUnit extends UsbACInterface {
    private static final String TAG = "UsbACMixerUnit";
    protected byte[] mInputIDs;
    protected byte mNumInputs;
    protected byte mNumOutputs;
    protected byte mUnitID;

    public UsbACMixerUnit(int length, byte type, byte subtype, int subClass) {
        super(length, type, subtype, subClass);
    }

    public byte getUnitID() {
        return this.mUnitID;
    }

    public byte getNumInputs() {
        return this.mNumInputs;
    }

    public byte[] getInputIDs() {
        return this.mInputIDs;
    }

    public byte getNumOutputs() {
        return this.mNumOutputs;
    }

    protected static int calcControlArraySize(int numInputs, int numOutputs) {
        return ((numInputs * numOutputs) + 7) / 8;
    }

    @Override // com.android.server.usb.descriptors.UsbDescriptor
    public int parseRawDescriptors(ByteStream stream) {
        this.mUnitID = stream.getByte();
        this.mNumInputs = stream.getByte();
        this.mInputIDs = new byte[this.mNumInputs];
        for (int input = 0; input < this.mNumInputs; input++) {
            this.mInputIDs[input] = stream.getByte();
        }
        this.mNumOutputs = stream.getByte();
        return this.mLength;
    }
}
