package com.android.server.usb.descriptors;

import com.android.server.usb.descriptors.report.ReportCanvas;

public final class Usb10ASFormatI extends UsbASFormat {
    private static final String TAG = "Usb10ASFormatI";
    private byte mBitResolution;
    private byte mNumChannels;
    private byte mSampleFreqType;
    private int[] mSampleRates;
    private byte mSubframeSize;

    public Usb10ASFormatI(int length, byte type, byte subtype, byte formatType, int subclass) {
        super(length, type, subtype, formatType, subclass);
    }

    public byte getNumChannels() {
        return this.mNumChannels;
    }

    public byte getSubframeSize() {
        return this.mSubframeSize;
    }

    public byte getBitResolution() {
        return this.mBitResolution;
    }

    public byte getSampleFreqType() {
        return this.mSampleFreqType;
    }

    public int[] getSampleRates() {
        return this.mSampleRates;
    }

    public int[] getBitDepths() {
        return new int[]{this.mBitResolution};
    }

    public int[] getChannelCounts() {
        return new int[]{this.mNumChannels};
    }

    public int parseRawDescriptors(ByteStream stream) {
        this.mNumChannels = stream.getByte();
        this.mSubframeSize = stream.getByte();
        this.mBitResolution = stream.getByte();
        this.mSampleFreqType = stream.getByte();
        int index = 0;
        if (this.mSampleFreqType != 0) {
            this.mSampleRates = new int[this.mSampleFreqType];
            while (true) {
                int index2 = index;
                if (index2 >= this.mSampleFreqType) {
                    break;
                }
                this.mSampleRates[index2] = stream.unpackUsbTriple();
                index = index2 + 1;
            }
        } else {
            this.mSampleRates = new int[2];
            this.mSampleRates[0] = stream.unpackUsbTriple();
            this.mSampleRates[1] = stream.unpackUsbTriple();
        }
        return this.mLength;
    }

    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.openList();
        canvas.writeListItem(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + getNumChannels() + " Channels.");
        StringBuilder sb = new StringBuilder();
        sb.append("Subframe Size: ");
        sb.append(getSubframeSize());
        canvas.writeListItem(sb.toString());
        canvas.writeListItem("Bit Resolution: " + getBitResolution());
        byte sampleFreqType = getSampleFreqType();
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
