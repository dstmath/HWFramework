package com.android.server.usb.descriptors;

import com.android.server.usb.descriptors.report.ReportCanvas;

public final class Usb10ASFormatII extends UsbASFormat {
    private static final String TAG = "Usb10ASFormatII";
    private int mMaxBitRate;
    private byte mSamFreqType;
    private int[] mSampleRates;
    private int mSamplesPerFrame;

    public Usb10ASFormatII(int length, byte type, byte subtype, byte formatType, int subclass) {
        super(length, type, subtype, formatType, subclass);
    }

    public int getMaxBitRate() {
        return this.mMaxBitRate;
    }

    public int getSamplesPerFrame() {
        return this.mSamplesPerFrame;
    }

    public byte getSamFreqType() {
        return this.mSamFreqType;
    }

    public int[] getSampleRates() {
        return this.mSampleRates;
    }

    public int parseRawDescriptors(ByteStream stream) {
        this.mMaxBitRate = stream.unpackUsbShort();
        this.mSamplesPerFrame = stream.unpackUsbShort();
        this.mSamFreqType = stream.getByte();
        int numFreqs = this.mSamFreqType == 0 ? 2 : this.mSamFreqType;
        this.mSampleRates = new int[numFreqs];
        for (int index = 0; index < numFreqs; index++) {
            this.mSampleRates[index] = stream.unpackUsbTriple();
        }
        return this.mLength;
    }

    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.openList();
        canvas.writeListItem("Max Bit Rate: " + getMaxBitRate());
        canvas.writeListItem("Samples Per Frame: " + getMaxBitRate());
        byte sampleFreqType = getSamFreqType();
        int[] sampleRates = getSampleRates();
        canvas.writeListItem("Sample Freq Type: " + sampleFreqType);
        canvas.openList();
        if (sampleFreqType == 0) {
            canvas.writeListItem("min: " + sampleRates[0]);
            canvas.writeListItem("max: " + sampleRates[1]);
        } else {
            for (int index = 0; index < sampleFreqType; index++) {
                canvas.writeListItem(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + sampleRates[index]);
            }
        }
        canvas.closeList();
        canvas.closeList();
    }
}
